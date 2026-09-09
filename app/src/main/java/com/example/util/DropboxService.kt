package com.example.util

import android.content.Context
import android.util.Base64
import com.example.data.local.AccountDao
import com.example.data.local.BudgetAdjustmentDao
import com.example.data.local.CategoryDao
import com.example.data.local.MonthlyBudgetDao
import com.example.data.local.RecurringBillDao
import com.example.data.local.TransactionDao
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.security.MessageDigest
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class DropboxAccountInfo(
    val accountId: String,
    val displayName: String,
    val email: String
)

data class DropboxAuthResult(
    val accessToken: String,
    val refreshToken: String,
    val expiresInSeconds: Long,
    val accountId: String,
    val displayName: String,
    val email: String,
    val driveIndex: Int,
    val appKey: String
)

object DropboxService {

    const val DEFAULT_APP_KEY = "53zjk836mlseuhk"
    const val REDIRECT_URI = "budgeter://dropbox-auth"
    private const val PREFS_AUTH_PENDING = "dropbox_auth_pending_prefs"
    private const val KEY_PENDING_VERIFIER = "pending_code_verifier"
    private const val KEY_PENDING_APP_KEY = "pending_app_key"
    private const val KEY_PENDING_DRIVE_INDEX = "pending_drive_index"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
    private val adapter = moshi.adapter(BudgetBackupData::class.java)

    /**
     * Generates a cryptographically random PKCE code verifier
     */
    private fun generateCodeVerifier(): String {
        val bytes = ByteArray(32)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
    }

    /**
     * Generates a SHA-256 code challenge from the code verifier
     */
    private fun generateCodeChallenge(verifier: String): String {
        val bytes = verifier.toByteArray(Charsets.US_ASCII)
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return Base64.encodeToString(digest, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
    }

    /**
     * Generates the OAuth 2.0 PKCE Authorization URL for the browser flow
     */
    fun generateAuthUrl(context: Context, appKey: String = DEFAULT_APP_KEY, driveIndex: Int): String {
        val effectiveAppKey = appKey.trim().ifEmpty { DEFAULT_APP_KEY }
        val verifier = generateCodeVerifier()
        val challenge = generateCodeChallenge(verifier)

        // Store PKCE state in SharedPreferences to survive process death/backgrounding
        context.getSharedPreferences(PREFS_AUTH_PENDING, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_PENDING_VERIFIER, verifier)
            .putString(KEY_PENDING_APP_KEY, effectiveAppKey)
            .putInt(KEY_PENDING_DRIVE_INDEX, driveIndex)
            .apply()

        val encodedRedirect = URLEncoder.encode(REDIRECT_URI, "UTF-8")
        return "https://www.dropbox.com/oauth2/authorize?client_id=$effectiveAppKey&response_type=code&code_challenge=$challenge&code_challenge_method=S256&redirect_uri=$encodedRedirect&token_access_type=offline"
    }

    /**
     * Exchanges the authorization code received from the browser redirect for tokens
     */
    suspend fun exchangeAuthCode(context: Context, authCode: String): Result<DropboxAuthResult> = withContext(Dispatchers.IO) {
        try {
            val prefs = context.getSharedPreferences(PREFS_AUTH_PENDING, Context.MODE_PRIVATE)
            val verifier = prefs.getString(KEY_PENDING_VERIFIER, "") ?: ""
            val appKey = prefs.getString(KEY_PENDING_APP_KEY, "") ?: ""
            val driveIndex = prefs.getInt(KEY_PENDING_DRIVE_INDEX, 1)

            if (verifier.isBlank() || appKey.isBlank()) {
                return@withContext Result.failure(IllegalStateException("No pending Dropbox authentication session found. Please try logging in again."))
            }

            val formBody = FormBody.Builder()
                .add("code", authCode.trim())
                .add("grant_type", "authorization_code")
                .add("client_id", appKey)
                .add("code_verifier", verifier)
                .add("redirect_uri", REDIRECT_URI)
                .build()

            val request = Request.Builder()
                .url("https://api.dropboxapi.com/oauth2/token")
                .post(formBody)
                .build()

            val tokenResponse = httpClient.newCall(request).execute()
            val tokenBody = tokenResponse.body?.string() ?: ""

            if (!tokenResponse.isSuccessful) {
                return@withContext Result.failure(Exception("Dropbox token exchange failed (${tokenResponse.code}): $tokenBody"))
            }

            val tokenJson = JSONObject(tokenBody)
            val accessToken = tokenJson.optString("access_token", "")
            val refreshToken = tokenJson.optString("refresh_token", "")
            val expiresIn = tokenJson.optLong("expires_in", 14400L)
            val accountId = tokenJson.optString("account_id", "")
            val uid = tokenJson.optString("uid", "")

            if (accessToken.isBlank()) {
                return@withContext Result.failure(Exception("Dropbox did not return an access token."))
            }

            // Fetch user profile details
            val userRes = testConnection(accessToken)
            val displayName = userRes.getOrNull()?.displayName ?: (if (uid.isNotBlank()) "Dropbox User ($uid)" else "Dropbox User")
            val email = userRes.getOrNull()?.email ?: ""

            // Clear pending session
            prefs.edit().clear().apply()

            Result.success(
                DropboxAuthResult(
                    accessToken = accessToken,
                    refreshToken = refreshToken,
                    expiresInSeconds = expiresIn,
                    accountId = accountId.ifBlank { uid },
                    displayName = displayName,
                    email = email,
                    driveIndex = driveIndex,
                    appKey = appKey
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Refreshes the short-lived access token using the stored refresh token
     */
    suspend fun refreshAccessToken(context: Context, driveIndex: Int): Result<String> = withContext(Dispatchers.IO) {
        try {
            val backupPrefs = BackupPreferences.getInstance(context)
            val account = if (driveIndex == 1) backupPrefs.config.value.primaryAccount else backupPrefs.config.value.secondaryAccount
            val appKey = account.appKey.trim().ifEmpty { DEFAULT_APP_KEY }
            val refreshToken = account.refreshToken.trim()

            if (refreshToken.isBlank()) {
                return@withContext Result.failure(IllegalStateException("No Dropbox refresh token available."))
            }

            val formBody = FormBody.Builder()
                .add("grant_type", "refresh_token")
                .add("refresh_token", refreshToken)
                .add("client_id", appKey)
                .build()

            val request = Request.Builder()
                .url("https://api.dropboxapi.com/oauth2/token")
                .post(formBody)
                .build()

            val response = httpClient.newCall(request).execute()
            val bodyStr = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Dropbox token refresh failed (${response.code}): $bodyStr"))
            }

            val json = JSONObject(bodyStr)
            val newAccessToken = json.optString("access_token", "")
            val expiresIn = json.optLong("expires_in", 14400L)

            if (newAccessToken.isNotBlank()) {
                backupPrefs.updateDropboxAccessToken(driveIndex, newAccessToken, expiresIn)
                Result.success(newAccessToken)
            } else {
                Result.failure(Exception("Failed to extract new access token from Dropbox refresh response."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Retrieves a valid access token, automatically refreshing if close to expiry
     */
    suspend fun getValidAccessToken(context: Context, driveIndex: Int): String {
        val backupPrefs = BackupPreferences.getInstance(context)
        val account = if (driveIndex == 1) backupPrefs.config.value.primaryAccount else backupPrefs.config.value.secondaryAccount
        
        val expiresAt = account.tokenExpiresAt
        val isExpiringSoon = expiresAt > 0L && System.currentTimeMillis() >= (expiresAt - 5 * 60 * 1000L)

        if (isExpiringSoon && account.refreshToken.isNotBlank() && account.appKey.isNotBlank()) {
            val refreshResult = refreshAccessToken(context, driveIndex)
            if (refreshResult.isSuccess) {
                return refreshResult.getOrNull() ?: account.accessToken
            }
        }
        return account.accessToken
    }

    /**
     * Verifies the Dropbox access token and fetches the user's account details
     */
    suspend fun testConnection(accessToken: String): Result<DropboxAccountInfo> = withContext(Dispatchers.IO) {
        if (accessToken.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Dropbox Access Token cannot be blank."))
        }
        try {
            val request = Request.Builder()
                .url("https://api.dropboxapi.com/2/users/get_current_account")
                .addHeader("Authorization", "Bearer ${accessToken.trim()}")
                .post("null".toRequestBody("application/json".toMediaType()))
                .build()

            httpClient.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("Dropbox authentication failed (${response.code}): $bodyStr"))
                }
                val json = JSONObject(bodyStr)
                val accountId = json.optString("account_id", "")
                val email = json.optString("email", "")
                val nameObj = json.optJSONObject("name")
                val displayName = nameObj?.optString("display_name", email) ?: email

                Result.success(DropboxAccountInfo(accountId, displayName, email))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Uploads a complete snapshot of the database to Dropbox /Budgeter folder
     */
    suspend fun uploadBackup(
        context: Context,
        accessToken: String,
        accountDao: AccountDao,
        categoryDao: CategoryDao,
        transactionDao: TransactionDao,
        recurringBillDao: RecurringBillDao,
        monthlyBudgetDao: MonthlyBudgetDao,
        budgetAdjustmentDao: BudgetAdjustmentDao,
        folderPath: String = "/Budgeter",
        includeSettings: Boolean = true
    ): Result<GoogleDriveBackupFile> = withContext(Dispatchers.IO) {
        if (accessToken.isBlank()) {
            return@withContext Result.failure(IllegalStateException("No Dropbox access token provided."))
        }
        try {
            val settingsBackup = if (includeSettings) BackupManager.captureSettings(context) else null
            val backupData = BudgetBackupData(
                accounts = accountDao.getAllAccountsSnapshot(),
                categories = categoryDao.getAllCategoriesSnapshot(),
                transactions = transactionDao.getAllTransactionsSnapshot(),
                recurringBills = recurringBillDao.getAllBillsSnapshot(),
                monthlyBudgets = monthlyBudgetDao.getAllBudgetsSnapshot(),
                budgetAdjustments = budgetAdjustmentDao.getAllAdjustmentsSnapshot(),
                settings = settingsBackup
            )
            val jsonContent = adapter.indent("  ").toJson(backupData)

            val fileName = "budgeter_sync_data.json"
            val cleanFolder = if (folderPath.startsWith("/")) folderPath else "/$folderPath"
            val targetPath = if (cleanFolder == "/" || cleanFolder.isEmpty()) "/$fileName" else "$cleanFolder/$fileName"

            val argJson = JSONObject().apply {
                put("path", targetPath)
                put("mode", "overwrite")
                put("autorename", false)
                put("mute", false)
                put("strict_conflict", false)
            }.toString()

            val fileBytes = jsonContent.toByteArray(Charsets.UTF_8)
            val requestBody = fileBytes.toRequestBody("application/octet-stream".toMediaType())

            val request = Request.Builder()
                .url("https://content.dropboxapi.com/2/files/upload")
                .addHeader("Authorization", "Bearer ${accessToken.trim()}")
                .addHeader("Dropbox-API-Arg", argJson)
                .post(requestBody)
                .build()

            httpClient.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("Dropbox upload failed (${response.code}): $bodyStr"))
                }
                val resObj = JSONObject(bodyStr)
                val fileId = resObj.optString("id", "")
                val name = resObj.optString("name", fileName)
                val modified = resObj.optString("client_modified", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date()))
                val size = resObj.optLong("size", jsonContent.length.toLong())

                // Clean up any legacy older backup files in the folder so ONLY the single sync file remains
                try {
                    val listBody = JSONObject().apply {
                        put("path", if (cleanFolder == "/" || cleanFolder.isEmpty()) "" else cleanFolder)
                        put("recursive", false)
                    }.toString()
                    val listReq = Request.Builder()
                        .url("https://api.dropboxapi.com/2/files/list_folder")
                        .addHeader("Authorization", "Bearer ${accessToken.trim()}")
                        .addHeader("Content-Type", "application/json")
                        .post(listBody.toRequestBody("application/json".toMediaType()))
                        .build()
                    httpClient.newCall(listReq).execute().use { lResp ->
                        if (lResp.isSuccessful) {
                            val lJson = JSONObject(lResp.body?.string() ?: "")
                            val entries = lJson.optJSONArray("entries") ?: JSONArray()
                            for (i in 0 until entries.length()) {
                                val item = entries.optJSONObject(i) ?: continue
                                val itemName = item.optString("name")
                                val itemPath = item.optString("path_lower")
                                if (item.optString(".tag") == "file" && itemName != fileName && itemName.endsWith(".json", ignoreCase = true)) {
                                    val delBody = JSONObject().apply { put("path", itemPath) }.toString()
                                    val delReq = Request.Builder()
                                        .url("https://api.dropboxapi.com/2/files/delete_v2")
                                        .addHeader("Authorization", "Bearer ${accessToken.trim()}")
                                        .addHeader("Content-Type", "application/json")
                                        .post(delBody.toRequestBody("application/json".toMediaType()))
                                        .build()
                                    httpClient.newCall(delReq).execute().close()
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Ignore background cleanup errors
                }

                Result.success(
                    GoogleDriveBackupFile(
                        id = if (fileId.isNotBlank()) fileId else targetPath,
                        name = name,
                        modifiedTime = modified,
                        size = size,
                        location = DriveBackupLocation.VISIBLE_APP_FOLDER
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Lists all backup json files from the Dropbox /Budgeter folder
     */
    suspend fun listBackups(
        accessToken: String,
        folderPath: String = "/Budgeter"
    ): Result<List<GoogleDriveBackupFile>> = withContext(Dispatchers.IO) {
        if (accessToken.isBlank()) {
            return@withContext Result.success(emptyList())
        }
        try {
            val cleanFolder = if (folderPath.startsWith("/")) folderPath else "/$folderPath"
            val bodyJson = JSONObject().apply {
                put("path", if (cleanFolder == "/" || cleanFolder.isEmpty()) "" else cleanFolder)
                put("recursive", false)
                put("include_media_info", false)
                put("include_deleted", false)
            }.toString()

            val request = Request.Builder()
                .url("https://api.dropboxapi.com/2/files/list_folder")
                .addHeader("Authorization", "Bearer ${accessToken.trim()}")
                .addHeader("Content-Type", "application/json")
                .post(bodyJson.toRequestBody("application/json".toMediaType()))
                .build()

            httpClient.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    if (bodyStr.contains("path/not_found") || response.code == 409) {
                        return@withContext Result.success(emptyList())
                    }
                    return@withContext Result.failure(Exception("Dropbox list failed (${response.code}): $bodyStr"))
                }

                val json = JSONObject(bodyStr)
                val entries = json.optJSONArray("entries") ?: JSONArray()
                val list = mutableListOf<GoogleDriveBackupFile>()

                for (i in 0 until entries.length()) {
                    val entry = entries.optJSONObject(i) ?: continue
                    val tag = entry.optString(".tag")
                    val name = entry.optString("name")
                    if (tag == "file" && name.endsWith(".json", ignoreCase = true)) {
                        val id = entry.optString("id", entry.optString("path_display", ""))
                        val size = entry.optLong("size", 0L)
                        val modified = entry.optString("client_modified", entry.optString("server_modified", ""))
                        list.add(
                            GoogleDriveBackupFile(
                                id = id,
                                name = name,
                                modifiedTime = modified,
                                size = size,
                                location = DriveBackupLocation.VISIBLE_APP_FOLDER
                            )
                        )
                    }
                }
                list.sortByDescending { it.modifiedTime }
                Result.success(list)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Downloads a backup file from Dropbox and returns its JSON string content
     */
    suspend fun downloadBackup(
        accessToken: String,
        pathOrId: String
    ): Result<String> = withContext(Dispatchers.IO) {
        if (accessToken.isBlank()) {
            return@withContext Result.failure(IllegalStateException("No Dropbox access token provided."))
        }
        try {
            val argJson = JSONObject().apply {
                put("path", pathOrId)
            }.toString()

            val request = Request.Builder()
                .url("https://content.dropboxapi.com/2/files/download")
                .addHeader("Authorization", "Bearer ${accessToken.trim()}")
                .addHeader("Dropbox-API-Arg", argJson)
                .post("".toRequestBody(null))
                .build()

            httpClient.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("Dropbox download failed (${response.code}): $bodyStr"))
                }
                Result.success(bodyStr)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Deletes a backup file from Dropbox
     */
    suspend fun deleteBackup(
        accessToken: String,
        pathOrId: String
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        if (accessToken.isBlank()) {
            return@withContext Result.failure(IllegalStateException("No Dropbox access token provided."))
        }
        try {
            val bodyJson = JSONObject().apply {
                put("path", pathOrId)
            }.toString()

            val request = Request.Builder()
                .url("https://api.dropboxapi.com/2/files/delete_v2")
                .addHeader("Authorization", "Bearer ${accessToken.trim()}")
                .addHeader("Content-Type", "application/json")
                .post(bodyJson.toRequestBody("application/json".toMediaType()))
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val bodyStr = response.body?.string() ?: ""
                    return@withContext Result.failure(Exception("Dropbox delete failed (${response.code}): $bodyStr"))
                }
                Result.success(true)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
