package com.example.util

import android.content.Context
import android.util.Base64
import com.example.data.local.AccountDao
import com.example.data.local.BudgetAdjustmentDao
import com.example.data.local.CategoryDao
import com.example.data.local.ItemImageCacheDao
import com.example.data.local.MonthlyBudgetDao
import com.example.data.local.RecurringBillDao
import com.example.data.local.SavingsGoalDao
import com.example.data.local.TransactionDao
import com.example.data.local.WishlistDao
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
import java.security.MessageDigest
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class DropboxAccountInfo(
    val email: String,
    val displayName: String
)

data class DropboxOAuthTokenResult(
    val driveIndex: Int,
    val appKey: String,
    val accessToken: String,
    val refreshToken: String,
    val expiresInSeconds: Long,
    val email: String,
    val displayName: String
)

object DropboxService {

    const val DEFAULT_APP_KEY = "budgeter_dropbox_app"
    private const val PREFS_TEMP_AUTH = "dropbox_auth_temp_prefs"
    private const val REDIRECT_URI = "budgeter://dropbox-auth"

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
     * Retrieves currently stored Dropbox access token for primary (1) or secondary (2) cloud account.
     */
    fun getValidAccessToken(context: Context, driveIndex: Int): String {
        val config = BackupPreferences.getInstance(context).config.value
        val account = if (driveIndex == 1) config.primaryAccount else config.secondaryAccount
        return account.accessToken.trim()
    }

    /**
     * Generates a PKCE-secured Dropbox OAuth 2.0 authorization URL.
     */
    fun generateAuthUrl(context: Context, appKey: String, driveIndex: Int): String {
        val verifier = generateCodeVerifier()
        val challenge = generateCodeChallenge(verifier)

        context.getSharedPreferences(PREFS_TEMP_AUTH, Context.MODE_PRIVATE).edit()
            .putString("code_verifier", verifier)
            .putString("app_key", appKey)
            .putInt("drive_index", driveIndex)
            .apply()

        return "https://www.dropbox.com/oauth2/authorize" +
                "?client_id=${appKey.trim()}" +
                "&response_type=code" +
                "&token_access_type=offline" +
                "&code_challenge=$challenge" +
                "&code_challenge_method=S256" +
                "&redirect_uri=$REDIRECT_URI" +
                "&state=$driveIndex"
    }

    private fun generateCodeVerifier(): String {
        val secureRandom = SecureRandom()
        val code = ByteArray(32)
        secureRandom.nextBytes(code)
        return Base64.encodeToString(code, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
    }

    private fun generateCodeChallenge(verifier: String): String {
        val bytes = verifier.toByteArray(Charsets.US_ASCII)
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return Base64.encodeToString(digest, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
    }

    /**
     * Exchanges an authorization code for access and refresh tokens.
     */
    suspend fun exchangeAuthCode(context: Context, code: String): Result<DropboxOAuthTokenResult> = withContext(Dispatchers.IO) {
        try {
            val prefs = context.getSharedPreferences(PREFS_TEMP_AUTH, Context.MODE_PRIVATE)
            val verifier = prefs.getString("code_verifier", "") ?: ""
            val appKey = prefs.getString("app_key", DEFAULT_APP_KEY) ?: DEFAULT_APP_KEY
            val driveIndex = prefs.getInt("drive_index", 1)

            val formBuilder = FormBody.Builder()
                .add("code", code)
                .add("grant_type", "authorization_code")
                .add("client_id", appKey)
                .add("redirect_uri", REDIRECT_URI)

            if (verifier.isNotBlank()) {
                formBuilder.add("code_verifier", verifier)
            }

            val request = Request.Builder()
                .url("https://api.dropboxapi.com/oauth2/token")
                .post(formBuilder.build())
                .build()

            val response = httpClient.newCall(request).execute()
            val respBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                val errorMsg = try {
                    val errJson = JSONObject(respBody)
                    errJson.optString("error_description", errJson.optString("error", "OAuth exchange failed"))
                } catch (_: Exception) {
                    "Dropbox token exchange failed: HTTP ${response.code}"
                }
                return@withContext Result.failure(Exception(errorMsg))
            }

            val tokenJson = JSONObject(respBody)
            val accessToken = tokenJson.getString("access_token")
            val refreshToken = tokenJson.optString("refresh_token", "")
            val expiresIn = tokenJson.optLong("expires_in", 14400L)

            // Retrieve current account profile
            val profileRes = testConnection(accessToken)
            val email = profileRes.getOrNull()?.email ?: "dropbox_user@budgeter.app"
            val displayName = profileRes.getOrNull()?.displayName ?: "Dropbox User"

            Result.success(
                DropboxOAuthTokenResult(
                    driveIndex = driveIndex,
                    appKey = appKey,
                    accessToken = accessToken,
                    refreshToken = refreshToken,
                    expiresInSeconds = expiresIn,
                    email = email,
                    displayName = displayName
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Verifies that the access token is active and fetches account identity.
     */
    suspend fun testConnection(token: String): Result<DropboxAccountInfo> = withContext(Dispatchers.IO) {
        try {
            val emptyBody = "null".toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url("https://api.dropboxapi.com/2/users/get_current_account")
                .addHeader("Authorization", "Bearer ${token.trim()}")
                .post(emptyBody)
                .build()

            httpClient.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("Dropbox connection failed: HTTP ${response.code}"))
                }
                val json = JSONObject(body)
                val email = json.optString("email", "")
                val nameObj = json.optJSONObject("name")
                val displayName = nameObj?.optString("display_name", email) ?: email
                Result.success(DropboxAccountInfo(email = email, displayName = displayName))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Lists all JSON backups stored in the specified Dropbox folder.
     */
    suspend fun listBackups(accessToken: String, folderPath: String = "/Budgeter"): Result<List<GoogleDriveBackupFile>> = withContext(Dispatchers.IO) {
        try {
            val normalizedPath = if (folderPath.startsWith("/")) folderPath else "/$folderPath"
            val payload = JSONObject().apply {
                put("path", if (normalizedPath == "/") "" else normalizedPath.trimEnd('/'))
                put("recursive", false)
                put("include_media_info", false)
                put("include_deleted", false)
            }
            val body = payload.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url("https://api.dropboxapi.com/2/files/list_folder")
                .addHeader("Authorization", "Bearer ${accessToken.trim()}")
                .post(body)
                .build()

            httpClient.newCall(request).execute().use { response ->
                val respString = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    if (respString.contains("path/not_found")) {
                        return@withContext Result.success(emptyList())
                    }
                    return@withContext Result.failure(Exception("Failed to list Dropbox folder: HTTP ${response.code}"))
                }

                val list = mutableListOf<GoogleDriveBackupFile>()
                val json = JSONObject(respString)
                val entries = json.optJSONArray("entries") ?: JSONArray()
                for (i in 0 until entries.length()) {
                    val entry = entries.getJSONObject(i)
                    val tag = entry.optString(".tag")
                    val name = entry.optString("name")
                    if (tag == "file" && name.endsWith(".json", ignoreCase = true)) {
                        list.add(
                            GoogleDriveBackupFile(
                                id = entry.optString("path_lower", entry.optString("id")),
                                name = name,
                                modifiedTime = entry.optString("server_modified", ""),
                                size = entry.optLong("size", 0L),
                                location = DriveBackupLocation.VISIBLE_APP_FOLDER
                            )
                        )
                    }
                }
                Result.success(list.sortedByDescending { it.modifiedTime })
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Uploads the backup JSON snapshot to Dropbox and keeps the 5 newest snapshots.
     */
    suspend fun uploadBackup(
        context: Context,
        accessToken: String,
        accountDao: AccountDao,
        categoryDao: CategoryDao,
        transactionDao: TransactionDao,
        recurringBillDao: RecurringBillDao,
        monthlyBudgetDao: MonthlyBudgetDao? = null,
        budgetAdjustmentDao: BudgetAdjustmentDao? = null,
        savingsGoalDao: SavingsGoalDao? = null,
        wishlistDao: WishlistDao? = null,
        itemImageCacheDao: ItemImageCacheDao? = null,
        folderPath: String = "/Budgeter",
        includeSettings: Boolean = true
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val bPrefs = BackupPreferences.getInstance(context)
            val backupData = BudgetBackupData(
                installationId = bPrefs.getInstallationId(),
                deviceName = bPrefs.getDeviceName(),
                accounts = accountDao.getAllAccountsSnapshot(),
                categories = categoryDao.getAllCategoriesSnapshot(),
                transactions = transactionDao.getAllTransactionsSnapshot(),
                recurringBills = recurringBillDao.getAllBillsSnapshot(),
                monthlyBudgets = monthlyBudgetDao?.getAllBudgetsSnapshot() ?: emptyList(),
                budgetAdjustments = budgetAdjustmentDao?.getAllAdjustmentsSnapshot() ?: emptyList(),
                savingsGoals = savingsGoalDao?.getAllGoalsSnapshot() ?: emptyList(),
                goalAllocations = savingsGoalDao?.getAllAllocationsSnapshot() ?: emptyList(),
                wishlistItems = wishlistDao?.getAllWishlistItemsSnapshot() ?: emptyList(),
                itemImageCaches = itemImageCacheDao?.getAllCachedItemsSnapshot() ?: emptyList(),
                customIcons = BackupManager.captureCustomIcons(context),
                settings = if (includeSettings) BackupManager.captureSettings(context) else null
            )
            val jsonContent = adapter.indent("  ").toJson(backupData)

            if (backupData.transactions.isEmpty()) {
                android.util.Log.w("DropboxService", "Local database has 0 transactions. Refusing to overwrite Dropbox sync file with empty data.")
                return@withContext Result.success(true)
            }

            val deviceName = bPrefs.getDeviceName().trim()
            val sanitizedDevice = deviceName.replace(Regex("[^a-zA-Z0-9_-]"), "_").ifBlank { "Device" }
            val fileName = "Budgeter_Sync_${sanitizedDevice}.json"
            val normalizedFolder = if (folderPath.startsWith("/")) folderPath.trimEnd('/') else "/${folderPath.trimEnd('/')}"
            val targetPath = "$normalizedFolder/$fileName"

            val apiArg = JSONObject().apply {
                put("path", targetPath)
                put("mode", "overwrite")
                put("autorename", false)
                put("mute", false)
            }.toString()

            val reqBody = jsonContent.toByteArray(Charsets.UTF_8).toRequestBody("application/octet-stream".toMediaType())
            val request = Request.Builder()
                .url("https://content.dropboxapi.com/2/files/upload")
                .addHeader("Authorization", "Bearer ${accessToken.trim()}")
                .addHeader("Dropbox-API-Arg", apiArg)
                .post(reqBody)
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val err = response.body?.string() ?: ""
                    return@withContext Result.failure(Exception("Dropbox upload failed: HTTP ${response.code} ($err)"))
                }
            }

            // Prune older snapshots to keep the latest 5
            try {
                pruneRollingBackups(accessToken, normalizedFolder, 5)
            } catch (_: Exception) {}

            Result.success(true)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    private suspend fun pruneRollingBackups(accessToken: String, folderPath: String, maxBackups: Int) {
        val filesRes = listBackups(accessToken, folderPath)
        val files = filesRes.getOrNull() ?: return
        if (files.size > maxBackups) {
            val toDelete = files.drop(maxBackups)
            for (f in toDelete) {
                deleteBackup(accessToken, f.id)
            }
        }
    }

    /**
     * Downloads file contents from Dropbox as a JSON string.
     */
    suspend fun downloadBackup(accessToken: String, pathOrId: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val apiArg = JSONObject().apply {
                put("path", pathOrId)
            }.toString()

            val emptyBody = ByteArray(0).toRequestBody(null)
            val request = Request.Builder()
                .url("https://content.dropboxapi.com/2/files/download")
                .addHeader("Authorization", "Bearer ${accessToken.trim()}")
                .addHeader("Dropbox-API-Arg", apiArg)
                .post(emptyBody)
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("Dropbox download failed: HTTP ${response.code}"))
                }
                val content = response.body?.string() ?: ""
                Result.success(content)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Restores records from a Dropbox backup file.
     */
    suspend fun restoreFromDropbox(
        context: Context,
        accessToken: String,
        pathOrId: String,
        accountDao: AccountDao,
        categoryDao: CategoryDao,
        transactionDao: TransactionDao,
        recurringBillDao: RecurringBillDao,
        monthlyBudgetDao: MonthlyBudgetDao? = null,
        budgetAdjustmentDao: BudgetAdjustmentDao? = null,
        savingsGoalDao: SavingsGoalDao? = null,
        wishlistDao: WishlistDao? = null,
        itemImageCacheDao: ItemImageCacheDao? = null,
        restoreData: Boolean = true,
        restoreSettings: Boolean = true
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val jsonResult = downloadBackup(accessToken, pathOrId)
            val json = jsonResult.getOrThrow()

            BackupManager.restoreFromJson(
                context = context,
                json = json,
                accountDao = accountDao,
                categoryDao = categoryDao,
                transactionDao = transactionDao,
                recurringBillDao = recurringBillDao,
                monthlyBudgetDao = monthlyBudgetDao,
                budgetAdjustmentDao = budgetAdjustmentDao,
                savingsGoalDao = savingsGoalDao,
                wishlistDao = wishlistDao,
                itemImageCacheDao = itemImageCacheDao,
                restoreData = restoreData,
                restoreSettings = restoreSettings
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Merges records from a Dropbox backup file without replacing existing data.
     */
    suspend fun mergeFromDropbox(
        context: Context,
        accessToken: String,
        pathOrId: String,
        accountDao: AccountDao,
        categoryDao: CategoryDao,
        transactionDao: TransactionDao,
        recurringBillDao: RecurringBillDao,
        monthlyBudgetDao: MonthlyBudgetDao? = null,
        budgetAdjustmentDao: BudgetAdjustmentDao? = null,
        savingsGoalDao: SavingsGoalDao? = null,
        wishlistDao: WishlistDao? = null,
        itemImageCacheDao: ItemImageCacheDao? = null,
        restoreSettings: Boolean = false
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val jsonResult = downloadBackup(accessToken, pathOrId)
            val json = jsonResult.getOrThrow()

            BackupManager.mergeFromJson(
                context = context,
                json = json,
                accountDao = accountDao,
                categoryDao = categoryDao,
                transactionDao = transactionDao,
                recurringBillDao = recurringBillDao,
                monthlyBudgetDao = monthlyBudgetDao,
                budgetAdjustmentDao = budgetAdjustmentDao,
                savingsGoalDao = savingsGoalDao,
                wishlistDao = wishlistDao,
                itemImageCacheDao = itemImageCacheDao,
                restoreSettings = restoreSettings
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Permanently deletes a backup file from Dropbox.
     */
    suspend fun deleteBackup(accessToken: String, pathOrId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val payload = JSONObject().apply {
                put("path", pathOrId)
            }
            val body = payload.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url("https://api.dropboxapi.com/2/files/delete_v2")
                .addHeader("Authorization", "Bearer ${accessToken.trim()}")
                .post(body)
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful && response.code != 404) {
                    return@withContext Result.failure(Exception("Failed to delete Dropbox file: HTTP ${response.code}"))
                }
                Result.success(Unit)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
