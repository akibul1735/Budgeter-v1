package com.example.util

import android.content.Context
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
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class DropboxAccountInfo(
    val accountId: String,
    val displayName: String,
    val email: String
)

object DropboxService {

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

            val timeStamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US).format(Date())
            val fileName = "budgeter_backup_$timeStamp.json"
            val cleanFolder = if (folderPath.startsWith("/")) folderPath else "/$folderPath"
            val targetPath = if (cleanFolder == "/" || cleanFolder.isEmpty()) "/$fileName" else "$cleanFolder/$fileName"

            val argJson = JSONObject().apply {
                put("path", targetPath)
                put("mode", "overwrite")
                put("autorename", false)
                put("mute", false)
                put("strict_conflict", false)
            }.toString()

            val request = Request.Builder()
                .url("https://content.dropboxapi.com/2/files/upload")
                .addHeader("Authorization", "Bearer ${accessToken.trim()}")
                .addHeader("Dropbox-API-Arg", argJson)
                .addHeader("Content-Type", "application/octet-stream")
                .post(jsonContent.toRequestBody("application/octet-stream".toMediaType()))
                .build()

            httpClient.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("Dropbox upload failed (${response.code}): $bodyStr"))
                }
                val resObj = JSONObject(bodyStr)
                val fileId = resObj.optString("id", "")
                val name = resObj.optString("name", fileName)
                val modified = resObj.optString("client_modified", timeStamp)
                val size = resObj.optLong("size", jsonContent.length.toLong())

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
                    // If folder doesn't exist yet, return empty list gracefully
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
