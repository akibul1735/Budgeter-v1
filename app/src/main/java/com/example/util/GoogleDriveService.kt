package com.example.util

import android.content.Context
import com.example.data.local.AccountDao
import com.example.data.local.CategoryDao
import com.example.data.local.RecurringBillDao
import com.example.data.local.TransactionDao
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class GoogleDriveBackupFile(
    val id: String,
    val name: String,
    val modifiedTime: String,
    val size: Long,
    val location: DriveBackupLocation
)

enum class DriveBackupLocation {
    VISIBLE_APP_FOLDER,  // In user visible folder named "Budgeter"
    HIDDEN_APP_DATA      // In hidden appDataFolder
}

data class DriveBackupResult(
    val visibleFileId: String?,
    val appDataFileId: String?,
    val fileName: String
)

object GoogleDriveService {

    val SCOPE_DRIVE_FILE = Scope("https://www.googleapis.com/auth/drive.file")
    val SCOPE_DRIVE_APPDATA = Scope("https://www.googleapis.com/auth/drive.appdata")

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
     * Gets GoogleSignInClient configured with required Drive scopes
     */
    fun getGoogleSignInClient(context: Context): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(SCOPE_DRIVE_FILE, SCOPE_DRIVE_APPDATA)
            .build()
        return GoogleSignIn.getClient(context, gso)
    }

    /**
     * Gets the current signed-in Google account, or null if not signed in or scopes not granted
     */
    fun getSignedInAccount(context: Context): GoogleSignInAccount? {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        return if (account != null && GoogleSignIn.hasPermissions(account, SCOPE_DRIVE_FILE, SCOPE_DRIVE_APPDATA)) {
            account
        } else {
            account
        }
    }

    /**
     * Obtains an OAuth2 access token for the given account using GoogleAuthUtil
     */
    suspend fun getAccessToken(context: Context, account: GoogleSignInAccount): String? = withContext(Dispatchers.IO) {
        try {
            val scopeString = "oauth2:https://www.googleapis.com/auth/drive.file https://www.googleapis.com/auth/drive.appdata"
            val androidAccount = account.account ?: return@withContext null
            com.google.android.gms.auth.GoogleAuthUtil.getToken(context, androidAccount, scopeString)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Finds or creates the visible "Budgeter" folder in Google Drive
     */
    private suspend fun getOrCreateVisibleBudgeterFolder(accessToken: String): String? = withContext(Dispatchers.IO) {
        try {
            // Search for existing active folder named "Budgeter" in root
            val query = "name = 'Budgeter' and mimeType = 'application/vnd.google-apps.folder' and 'root' in parents and trashed = false"
            val searchUrl = "https://www.googleapis.com/drive/v3/files?q=${java.net.URLEncoder.encode(query, "UTF-8")}&spaces=drive&fields=files(id,name)"

            val searchReq = Request.Builder()
                .url(searchUrl)
                .addHeader("Authorization", "Bearer $accessToken")
                .get()
                .build()

            httpClient.newCall(searchReq).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: ""
                    val json = JSONObject(body)
                    val files = json.optJSONArray("files") ?: JSONArray()
                    if (files.length() > 0) {
                        return@withContext files.getJSONObject(0).getString("id")
                    }
                }
            }

            // Create "Budgeter" folder in Drive root
            val createUrl = "https://www.googleapis.com/drive/v3/files"
            val metadata = JSONObject().apply {
                put("name", "Budgeter")
                put("mimeType", "application/vnd.google-apps.folder")
                put("parents", JSONArray().put("root"))
            }

            val body = metadata.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val createReq = Request.Builder()
                .url(createUrl)
                .addHeader("Authorization", "Bearer $accessToken")
                .post(body)
                .build()

            httpClient.newCall(createReq).execute().use { response ->
                if (response.isSuccessful) {
                    val resJson = JSONObject(response.body?.string() ?: "")
                    return@withContext resJson.getString("id")
                }
            }
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Uploads the backup JSON data to BOTH:
     * 1. Visible folder "Budgeter" in Google Drive
     * 2. Hidden appDataFolder in Google Drive
     */
    suspend fun uploadBackupToDrive(
        context: Context,
        account: GoogleSignInAccount,
        accountDao: AccountDao,
        categoryDao: CategoryDao,
        transactionDao: TransactionDao,
        recurringBillDao: RecurringBillDao
    ): Result<DriveBackupResult> = withContext(Dispatchers.IO) {
        try {
            val accessToken = getAccessToken(context, account)
                ?: return@withContext Result.failure(Exception("Failed to obtain Google Drive access token. Please re-authenticate."))

            val backupData = BudgetBackupData(
                accounts = accountDao.getAllAccountsSnapshot(),
                categories = categoryDao.getAllCategoriesSnapshot(),
                transactions = transactionDao.getAllTransactionsSnapshot(),
                recurringBills = recurringBillDao.getAllBillsSnapshot()
            )
            val jsonContent = adapter.indent("  ").toJson(backupData)

            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "Budgeter_Backup_$timeStamp.json"

            // 1. Upload to Visible "Budgeter" Folder
            val folderId = getOrCreateVisibleBudgeterFolder(accessToken)
            val visibleFileId = uploadFile(
                accessToken = accessToken,
                fileName = fileName,
                jsonContent = jsonContent,
                parents = if (folderId != null) listOf(folderId) else listOf("root")
            )

            // 2. Upload to Hidden "appDataFolder"
            val appDataFileId = uploadFile(
                accessToken = accessToken,
                fileName = fileName,
                jsonContent = jsonContent,
                parents = listOf("appDataFolder")
            )

            Result.success(
                DriveBackupResult(
                    visibleFileId = visibleFileId,
                    appDataFileId = appDataFileId,
                    fileName = fileName
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    private fun uploadFile(
        accessToken: String,
        fileName: String,
        jsonContent: String,
        parents: List<String>
    ): String? {
        try {
            val metadata = JSONObject().apply {
                put("name", fileName)
                put("mimeType", "application/json")
                put("parents", JSONArray(parents))
            }

            val multipartBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "metadata",
                    null,
                    metadata.toString().toRequestBody("application/json; charset=UTF-8".toMediaType())
                )
                .addFormDataPart(
                    "file",
                    fileName,
                    jsonContent.toRequestBody("application/json; charset=UTF-8".toMediaType())
                )
                .build()

            val uploadUrl = "https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart"
            val request = Request.Builder()
                .url(uploadUrl)
                .addHeader("Authorization", "Bearer $accessToken")
                .post(multipartBody)
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val resJson = JSONObject(response.body?.string() ?: "")
                    return resJson.optString("id")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * Lists all backup files from both the visible folder and the hidden appData folder
     */
    suspend fun listDriveBackups(
        context: Context,
        account: GoogleSignInAccount
    ): Result<List<GoogleDriveBackupFile>> = withContext(Dispatchers.IO) {
        try {
            val accessToken = getAccessToken(context, account)
                ?: return@withContext Result.failure(Exception("Failed to acquire access token"))

            val list = mutableListOf<GoogleDriveBackupFile>()

            // 1. Fetch from visible folder "Budgeter"
            val visibleFolderId = getOrCreateVisibleBudgeterFolder(accessToken)
            if (visibleFolderId != null) {
                val query = "'$visibleFolderId' in parents and trashed = false and mimeType = 'application/json'"
                val url = "https://www.googleapis.com/drive/v3/files?q=${java.net.URLEncoder.encode(query, "UTF-8")}&spaces=drive&fields=files(id,name,modifiedTime,size)&orderBy=modifiedTime desc"

                val req = Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer $accessToken")
                    .get()
                    .build()

                httpClient.newCall(req).execute().use { response ->
                    if (response.isSuccessful) {
                        val body = response.body?.string() ?: ""
                        val json = JSONObject(body)
                        val files = json.optJSONArray("files") ?: JSONArray()
                        for (i in 0 until files.length()) {
                            val f = files.getJSONObject(i)
                            list.add(
                                GoogleDriveBackupFile(
                                    id = f.getString("id"),
                                    name = f.optString("name", "Budgeter_Backup.json"),
                                    modifiedTime = f.optString("modifiedTime", ""),
                                    size = f.optLong("size", 0L),
                                    location = DriveBackupLocation.VISIBLE_APP_FOLDER
                                )
                            )
                        }
                    }
                }
            }

            // 2. Fetch from hidden appDataFolder
            val appDataQuery = "trashed = false"
            val appDataUrl = "https://www.googleapis.com/drive/v3/files?spaces=appDataFolder&fields=files(id,name,modifiedTime,size)&orderBy=modifiedTime desc"

            val appDataReq = Request.Builder()
                .url(appDataUrl)
                .addHeader("Authorization", "Bearer $accessToken")
                .get()
                .build()

            httpClient.newCall(appDataReq).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: ""
                    val json = JSONObject(body)
                    val files = json.optJSONArray("files") ?: JSONArray()
                    for (i in 0 until files.length()) {
                        val f = files.getJSONObject(i)
                        list.add(
                            GoogleDriveBackupFile(
                                id = f.getString("id"),
                                name = f.optString("name", "Budgeter_Backup.json"),
                                modifiedTime = f.optString("modifiedTime", ""),
                                size = f.optLong("size", 0L),
                                location = DriveBackupLocation.HIDDEN_APP_DATA
                            )
                        )
                    }
                }
            }

            Result.success(list)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Downloads and restores a backup file from Google Drive (either visible or hidden location)
     */
    suspend fun restoreFromDriveFile(
        context: Context,
        account: GoogleSignInAccount,
        fileId: String,
        accountDao: AccountDao,
        categoryDao: CategoryDao,
        transactionDao: TransactionDao,
        recurringBillDao: RecurringBillDao
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val accessToken = getAccessToken(context, account)
                ?: return@withContext Result.failure(Exception("Failed to obtain access token"))

            val downloadUrl = "https://www.googleapis.com/drive/v3/files/$fileId?alt=media"
            val request = Request.Builder()
                .url(downloadUrl)
                .addHeader("Authorization", "Bearer $accessToken")
                .get()
                .build()

            val json = httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("Failed to download file from Google Drive: HTTP ${response.code}"))
                }
                response.body?.string() ?: return@withContext Result.failure(Exception("Empty file content"))
            }

            val backupData = adapter.fromJson(json)
                ?: return@withContext Result.failure(Exception("Invalid backup format"))

            // Clean & replace with restored records
            transactionDao.deleteAll()
            recurringBillDao.deleteAll()
            categoryDao.deleteAll()
            accountDao.deleteAll()

            accountDao.insertAccounts(backupData.accounts)
            categoryDao.insertCategories(backupData.categories)
            transactionDao.insertTransactions(backupData.transactions)
            if (backupData.recurringBills.isNotEmpty()) {
                recurringBillDao.insertAll(backupData.recurringBills)
            }

            val count = backupData.transactions.size + backupData.accounts.size + backupData.categories.size
            Result.success(count)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Deletes a backup file from Google Drive
     */
    suspend fun deleteDriveBackup(
        context: Context,
        account: GoogleSignInAccount,
        fileId: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val accessToken = getAccessToken(context, account) ?: return@withContext false
            val url = "https://www.googleapis.com/drive/v3/files/$fileId"
            val req = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $accessToken")
                .delete()
                .build()

            httpClient.newCall(req).execute().use { it.isSuccessful }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
