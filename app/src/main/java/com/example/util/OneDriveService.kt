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

data class OneDriveAccountInfo(
    val id: String,
    val displayName: String,
    val email: String
)

object OneDriveService {

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
     * Verifies the Microsoft Graph access token and fetches the user's profile details
     */
    suspend fun testConnection(accessToken: String): Result<OneDriveAccountInfo> = withContext(Dispatchers.IO) {
        if (accessToken.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("OneDrive Access Token cannot be blank."))
        }
        try {
            val request = Request.Builder()
                .url("https://graph.microsoft.com/v1.0/me")
                .addHeader("Authorization", "Bearer ${accessToken.trim()}")
                .get()
                .build()

            httpClient.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("Microsoft OneDrive authentication failed (${response.code}): $bodyStr"))
                }
                val json = JSONObject(bodyStr)
                val id = json.optString("id", "")
                val displayName = json.optString("displayName", "")
                val email = json.optString("mail", json.optString("userPrincipalName", displayName))

                Result.success(OneDriveAccountInfo(id, displayName.ifEmpty { email }, email))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Uploads a complete snapshot of the database to Microsoft OneDrive root:/Budgeter folder
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
        folderName: String = "Budgeter",
        includeSettings: Boolean = true
    ): Result<GoogleDriveBackupFile> = withContext(Dispatchers.IO) {
        if (accessToken.isBlank()) {
            return@withContext Result.failure(IllegalStateException("No OneDrive access token provided."))
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
            val cleanFolder = folderName.trim().removePrefix("/").removeSuffix("/").ifEmpty { "Budgeter" }

            val uploadUrl = "https://graph.microsoft.com/v1.0/me/drive/root:/$cleanFolder/$fileName:/content"

            val request = Request.Builder()
                .url(uploadUrl)
                .addHeader("Authorization", "Bearer ${accessToken.trim()}")
                .addHeader("Content-Type", "application/json; charset=UTF-8")
                .put(jsonContent.toRequestBody("application/json; charset=UTF-8".toMediaType()))
                .build()

            httpClient.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("OneDrive upload failed (${response.code}): $bodyStr"))
                }
                val resObj = JSONObject(bodyStr)
                val fileId = resObj.optString("id", "")
                val name = resObj.optString("name", fileName)
                val modified = resObj.optString("lastModifiedDateTime", timeStamp)
                val size = resObj.optLong("size", jsonContent.length.toLong())

                Result.success(
                    GoogleDriveBackupFile(
                        id = fileId,
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
     * Lists all backup json files from the Microsoft OneDrive root:/Budgeter folder
     */
    suspend fun listBackups(
        accessToken: String,
        folderName: String = "Budgeter"
    ): Result<List<GoogleDriveBackupFile>> = withContext(Dispatchers.IO) {
        if (accessToken.isBlank()) {
            return@withContext Result.success(emptyList())
        }
        try {
            val cleanFolder = folderName.trim().removePrefix("/").removeSuffix("/").ifEmpty { "Budgeter" }
            val listUrl = "https://graph.microsoft.com/v1.0/me/drive/root:/$cleanFolder:/children"

            val request = Request.Builder()
                .url(listUrl)
                .addHeader("Authorization", "Bearer ${accessToken.trim()}")
                .get()
                .build()

            httpClient.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    // Folder doesn't exist yet (404) -> return empty list
                    if (response.code == 404) {
                        return@withContext Result.success(emptyList())
                    }
                    return@withContext Result.failure(Exception("OneDrive list failed (${response.code}): $bodyStr"))
                }

                val json = JSONObject(bodyStr)
                val values = json.optJSONArray("value") ?: JSONArray()
                val list = mutableListOf<GoogleDriveBackupFile>()

                for (i in 0 until values.length()) {
                    val item = values.optJSONObject(i) ?: continue
                    val name = item.optString("name")
                    val hasFile = item.has("file")
                    if (hasFile && name.endsWith(".json", ignoreCase = true)) {
                        val id = item.optString("id")
                        val size = item.optLong("size", 0L)
                        val modified = item.optString("lastModifiedDateTime", "")
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
     * Downloads a backup file from Microsoft OneDrive and returns its JSON content
     */
    suspend fun downloadBackup(
        accessToken: String,
        fileId: String
    ): Result<String> = withContext(Dispatchers.IO) {
        if (accessToken.isBlank()) {
            return@withContext Result.failure(IllegalStateException("No OneDrive access token provided."))
        }
        try {
            val url = "https://graph.microsoft.com/v1.0/me/drive/items/$fileId/content"

            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer ${accessToken.trim()}")
                .get()
                .build()

            httpClient.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("OneDrive download failed (${response.code}): $bodyStr"))
                }
                Result.success(bodyStr)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Deletes a backup file from Microsoft OneDrive
     */
    suspend fun deleteBackup(
        accessToken: String,
        fileId: String
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        if (accessToken.isBlank()) {
            return@withContext Result.failure(IllegalStateException("No OneDrive access token provided."))
        }
        try {
            val url = "https://graph.microsoft.com/v1.0/me/drive/items/$fileId"

            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer ${accessToken.trim()}")
                .delete()
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful && response.code != 204) {
                    val bodyStr = response.body?.string() ?: ""
                    return@withContext Result.failure(Exception("OneDrive delete failed (${response.code}): $bodyStr"))
                }
                Result.success(true)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
