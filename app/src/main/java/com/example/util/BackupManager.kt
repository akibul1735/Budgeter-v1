package com.example.util

import android.content.Context
import android.net.Uri
import com.example.data.local.AccountDao
import com.example.data.local.CategoryDao
import com.example.data.local.MonthlyBudgetDao
import com.example.data.local.RecurringBillDao
import com.example.data.local.TransactionDao
import com.example.data.model.Account
import com.example.data.model.Category
import com.example.data.model.MonthlyBudget
import com.example.data.model.RecurringBill
import com.example.data.model.Transaction
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BudgetBackupData(
    val version: Int = 3,
    val exportedAt: Long = System.currentTimeMillis(),
    val app: String = "Budgeter",
    val accounts: List<Account>,
    val categories: List<Category>,
    val transactions: List<Transaction>,
    val recurringBills: List<RecurringBill> = emptyList(),
    val monthlyBudgets: List<MonthlyBudget> = emptyList()
)

object BackupManager {

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
    private val adapter = moshi.adapter(BudgetBackupData::class.java)

    /**
     * Creates a JSON backup in the designated target directory, public Documents/Budgeter,
     * external app storage, and internal storage, ensuring the backup is readily visible to the user.
     */
    suspend fun createLocalBackupFile(
        context: Context,
        accountDao: AccountDao,
        categoryDao: CategoryDao,
        transactionDao: TransactionDao,
        recurringBillDao: RecurringBillDao,
        monthlyBudgetDao: MonthlyBudgetDao? = null,
        targetDirectory: String? = null
    ): File = withContext(Dispatchers.IO) {
        val backupData = BudgetBackupData(
            accounts = accountDao.getAllAccountsSnapshot(),
            categories = categoryDao.getAllCategoriesSnapshot(),
            transactions = transactionDao.getAllTransactionsSnapshot(),
            recurringBills = recurringBillDao.getAllBillsSnapshot(),
            monthlyBudgets = monthlyBudgetDao?.getAllBudgetsSnapshot() ?: emptyList()
        )
        val json = adapter.indent("  ").toJson(backupData)

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "Budgeter_Backup_$timeStamp.json"
        
        var primaryFile: File? = null

        // 1. Write to Custom Target Directory if specified
        if (!targetDirectory.isNullOrBlank()) {
            if (targetDirectory.startsWith("content://")) {
                try {
                    val treeUri = Uri.parse(targetDirectory)
                    val pickedDir = androidx.documentfile.provider.DocumentFile.fromTreeUri(context, treeUri)
                    if (pickedDir != null && pickedDir.exists() && pickedDir.canWrite()) {
                        val newDoc = pickedDir.createFile("application/json", fileName)
                        if (newDoc != null) {
                            context.contentResolver.openOutputStream(newDoc.uri)?.use { out ->
                                out.write(json.toByteArray(Charsets.UTF_8))
                                out.flush()
                            }
                            // Cache mirror for immediate local file representation
                            val cacheDir = File(context.cacheDir, "saf_backups").apply { if (!exists()) mkdirs() }
                            val mirror = File(cacheDir, fileName)
                            mirror.writeText(json)
                            primaryFile = mirror
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                try {
                    val customDir = File(targetDirectory)
                    if (!customDir.exists()) customDir.mkdirs()
                    if (customDir.exists() && customDir.canWrite()) {
                        val f = File(customDir, fileName)
                        f.writeText(json)
                        primaryFile = f
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        // 2. Write to MediaStore Documents / Budgeter (Works seamlessly on Android 10+ without storage permissions)
        try {
            writeBackupToMediaStoreDocuments(context, fileName, json)
        } catch (_: Exception) {}

        // 3. Write to Public Documents / Budgeter directory for direct file accessibility
        try {
            val publicDocs = File(android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOCUMENTS), "Budgeter")
            if (!publicDocs.exists()) publicDocs.mkdirs()
            if (publicDocs.exists() && publicDocs.canWrite()) {
                val f = File(publicDocs, fileName)
                f.writeText(json)
                if (primaryFile == null) primaryFile = f
            }
        } catch (_: Exception) {}

        // 4. Write to App External Files Dir (Device storage accessible in file managers)
        try {
            val extDir = File(context.getExternalFilesDir(null), "backups")
            if (!extDir.exists()) extDir.mkdirs()
            if (extDir.exists()) {
                val f = File(extDir, fileName)
                f.writeText(json)
                if (primaryFile == null) primaryFile = f
            }
        } catch (_: Exception) {}

        // 5. Always ensure a backup copy in App Internal Files Dir as ultimate safety net
        val internalDir = File(context.filesDir, "backups")
        if (!internalDir.exists()) internalDir.mkdirs()
        val internalFile = File(internalDir, fileName)
        internalFile.writeText(json)

        primaryFile ?: internalFile
    }

    /**
     * Saves backup JSON into MediaStore Documents/Budgeter (Scoped Storage compatible for Android 10+)
     */
    fun writeBackupToMediaStoreDocuments(context: Context, fileName: String, json: String): Uri? {
        return try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                val contentValues = android.content.ContentValues().apply {
                    put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/json")
                    put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_DOCUMENTS + "/Budgeter")
                    put(android.provider.MediaStore.MediaColumns.IS_PENDING, 1)
                }
                val collection = android.provider.MediaStore.Files.getContentUri(android.provider.MediaStore.VOLUME_EXTERNAL_PRIMARY)
                val itemUri = context.contentResolver.insert(collection, contentValues)
                if (itemUri != null) {
                    context.contentResolver.openOutputStream(itemUri)?.use { out ->
                        out.write(json.toByteArray(Charsets.UTF_8))
                        out.flush()
                    }
                    contentValues.clear()
                    contentValues.put(android.provider.MediaStore.MediaColumns.IS_PENDING, 0)
                    context.contentResolver.update(itemUri, contentValues, null, null)
                    itemUri
                } else null
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Exports backup data directly to a chosen user destination URI (SAF)
     */
    suspend fun exportBackupToUri(
        context: Context,
        uri: Uri,
        accountDao: AccountDao,
        categoryDao: CategoryDao,
        transactionDao: TransactionDao,
        recurringBillDao: RecurringBillDao,
        monthlyBudgetDao: MonthlyBudgetDao? = null
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val backupData = BudgetBackupData(
                accounts = accountDao.getAllAccountsSnapshot(),
                categories = categoryDao.getAllCategoriesSnapshot(),
                transactions = transactionDao.getAllTransactionsSnapshot(),
                recurringBills = recurringBillDao.getAllBillsSnapshot(),
                monthlyBudgets = monthlyBudgetDao?.getAllBudgetsSnapshot() ?: emptyList()
            )
            val json = adapter.indent("  ").toJson(backupData)

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(json.toByteArray())
                outputStream.flush()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Restores the database from a backup JSON string or URI
     */
    suspend fun restoreBackupFromUri(
        context: Context,
        uri: Uri,
        accountDao: AccountDao,
        categoryDao: CategoryDao,
        transactionDao: TransactionDao,
        recurringBillDao: RecurringBillDao,
        monthlyBudgetDao: MonthlyBudgetDao? = null
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val json = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader().readText()
            } ?: return@withContext Result.failure(Exception("Cannot open backup file"))

            val backupData = adapter.fromJson(json)
                ?: return@withContext Result.failure(Exception("Invalid backup file format"))

            // Replace all records safely
            transactionDao.deleteAll()
            recurringBillDao.deleteAll()
            monthlyBudgetDao?.deleteAll()
            categoryDao.deleteAll()
            accountDao.deleteAll()

            accountDao.insertAccounts(backupData.accounts)
            categoryDao.insertCategories(backupData.categories)
            transactionDao.insertTransactions(backupData.transactions)
            if (backupData.recurringBills.isNotEmpty()) {
                recurringBillDao.insertAll(backupData.recurringBills)
            }
            if (backupData.monthlyBudgets.isNotEmpty() && monthlyBudgetDao != null) {
                monthlyBudgetDao.upsertBudgets(backupData.monthlyBudgets)
            }

            val totalCount = backupData.transactions.size + backupData.accounts.size + backupData.categories.size + backupData.monthlyBudgets.size
            Result.success(totalCount)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * List all local auto/manual backups from custom directory, Documents/Budgeter,
     * Downloads/Budgeter, and default app storage locations.
     */
    fun listLocalBackups(context: Context, customDirectoryPath: String? = null): List<File> {
        val resultList = mutableListOf<File>()
        
        // 1. App internal backups
        val defaultDir = File(context.filesDir, "backups")
        if (defaultDir.exists()) {
            defaultDir.listFiles { file -> file.extension == "json" || file.extension == "db" }?.let {
                resultList.addAll(it)
            }
        }

        // 2. App external backups
        try {
            val extDir = File(context.getExternalFilesDir(null), "backups")
            if (extDir.exists()) {
                extDir.listFiles { file -> file.extension == "json" || file.extension == "db" }?.let {
                    resultList.addAll(it)
                }
            }
        } catch (_: Exception) {}

        // 3. Public Documents/Budgeter
        try {
            val docsDir = File(android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOCUMENTS), "Budgeter")
            if (docsDir.exists() && docsDir.isDirectory) {
                docsDir.listFiles { file -> file.extension == "json" || file.extension == "db" }?.let {
                    resultList.addAll(it)
                }
            }
        } catch (_: Exception) {}

        // 4. Public Downloads/Budgeter
        try {
            val dlDir = File(android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS), "Budgeter")
            if (dlDir.exists() && dlDir.isDirectory) {
                dlDir.listFiles { file -> file.extension == "json" || file.extension == "db" }?.let {
                    resultList.addAll(it)
                }
            }
        } catch (_: Exception) {}

        // 5. SAF Mirror backups
        try {
            val cacheDir = File(context.cacheDir, "saf_backups")
            if (cacheDir.exists()) {
                cacheDir.listFiles { file -> file.extension == "json" || file.extension == "db" }?.let {
                    resultList.addAll(it)
                }
            }
        } catch (_: Exception) {}

        // 6. Custom directory if provided
        if (!customDirectoryPath.isNullOrBlank()) {
            if (customDirectoryPath.startsWith("content://")) {
                try {
                    val treeUri = Uri.parse(customDirectoryPath)
                    val pickedDir = androidx.documentfile.provider.DocumentFile.fromTreeUri(context, treeUri)
                    if (pickedDir != null && pickedDir.exists() && pickedDir.isDirectory) {
                        pickedDir.listFiles().forEach { doc ->
                            if (doc.isFile && (doc.name?.endsWith(".json") == true || doc.name?.endsWith(".db") == true)) {
                                val name = doc.name ?: return@forEach
                                val cacheDir = File(context.cacheDir, "saf_backups").apply { if (!exists()) mkdirs() }
                                val tempFile = File(cacheDir, name)
                                if (!tempFile.exists() || tempFile.length() != doc.length()) {
                                    context.contentResolver.openInputStream(doc.uri)?.use { input ->
                                        tempFile.outputStream().use { output -> input.copyTo(output) }
                                    }
                                }
                                resultList.add(tempFile)
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                try {
                    val customDir = File(customDirectoryPath)
                    if (customDir.exists() && customDir.isDirectory) {
                        customDir.listFiles { file -> file.extension == "json" || file.extension == "db" }?.let {
                            resultList.addAll(it)
                        }
                    }
                } catch (_: Exception) {}
            }
        }
        
        return resultList.distinctBy { it.name }.sortedByDescending { it.lastModified() }
    }
}
