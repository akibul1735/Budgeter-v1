package com.example.util

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.data.local.AccountDao
import com.example.data.local.BudgetAdjustmentDao
import com.example.data.local.CategoryDao
import com.example.data.local.MonthlyBudgetDao
import com.example.data.local.RecurringBillDao
import com.example.data.local.TransactionDao
import com.example.data.model.Account
import com.example.data.model.BudgetAdjustment
import com.example.data.model.Category
import com.example.data.model.MonthlyBudget
import com.example.data.model.RecurringBill
import com.example.data.model.Transaction
import com.example.ui.theme.AppThemeConfig
import com.example.ui.theme.ThemePreferences
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AppSettingsBackup(
    val theme: AppThemeConfig? = null,
    val currency: CurrencyConfig? = null,
    val displayFormat: DisplayFormatConfig? = null,
    val autofill: AutofillConfig? = null,
    val tabConfig: NavigationTabConfig? = null,
    val isDualSyncEnabled: Boolean? = null,
    val autoPhoneBackup: Boolean? = null,
    val scheduledHour: Int? = null,
    val scheduledMinute: Int? = null,
    val uploadAttachments: Boolean? = null,
    val autoSyncData: Boolean? = null,
    val autoSyncOnAppStart: Boolean? = null,
    val autoSyncOnAppClose: Boolean? = null,
    val wifiOnly: Boolean? = null,
    val primaryAutoSync: Boolean? = null,
    val primaryWifiOnly: Boolean? = null,
    val primaryFolderType: String? = null,
    val secondaryAutoSync: Boolean? = null,
    val secondaryWifiOnly: Boolean? = null,
    val secondaryFolderType: String? = null
)

data class BudgetBackupData(
    val version: Int = 4,
    val exportedAt: Long = System.currentTimeMillis(),
    val app: String = "Budgeter",
    val installationId: String? = null,
    val deviceName: String? = null,
    val appVersion: String = "3.6",
    val accounts: List<Account> = emptyList(),
    val categories: List<Category> = emptyList(),
    val transactions: List<Transaction> = emptyList(),
    val recurringBills: List<RecurringBill> = emptyList(),
    val monthlyBudgets: List<MonthlyBudget> = emptyList(),
    val budgetAdjustments: List<BudgetAdjustment> = emptyList(),
    val settings: AppSettingsBackup? = null
)

object BackupManager {

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
    private val adapter = moshi.adapter(BudgetBackupData::class.java)

    fun parseBackupData(json: String): BudgetBackupData? {
        return try {
            adapter.fromJson(json)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Captures current user settings and preferences across the entire application.
     */
    fun captureSettings(context: Context): AppSettingsBackup {
        return try {
            val themePrefs = ThemePreferences.getInstance(context)
            val currencyPrefs = CurrencyPreferences.getInstance(context)
            val displayPrefs = DisplayFormatPreferences.getInstance(context)
            val autofillPrefs = AutofillPreferences.getInstance(context)
            val tabPrefs = TabPreferences.getInstance(context)
            val backupPrefs = BackupPreferences.getInstance(context)
            val bConfig = backupPrefs.config.value

            AppSettingsBackup(
                theme = themePrefs.themeConfig.value,
                currency = currencyPrefs.config.value,
                displayFormat = displayPrefs.config.value,
                autofill = autofillPrefs.config.value,
                tabConfig = tabPrefs.config.value,
                isDualSyncEnabled = bConfig.isDualSyncEnabled,
                autoPhoneBackup = bConfig.isAutoPhoneBackupEnabled,
                scheduledHour = bConfig.scheduledBackupHour,
                scheduledMinute = bConfig.scheduledBackupMinute,
                uploadAttachments = bConfig.uploadAttachments,
                autoSyncData = bConfig.autoSyncData,
                autoSyncOnAppStart = bConfig.autoSyncOnAppStart,
                autoSyncOnAppClose = bConfig.autoSyncOnAppClose,
                wifiOnly = bConfig.wifiOnly,
                primaryAutoSync = bConfig.primaryAccount.autoSync,
                primaryWifiOnly = bConfig.primaryAccount.wifiOnly,
                primaryFolderType = bConfig.primaryAccount.driveFolderType,
                secondaryAutoSync = bConfig.secondaryAccount.autoSync,
                secondaryWifiOnly = bConfig.secondaryAccount.wifiOnly,
                secondaryFolderType = bConfig.secondaryAccount.driveFolderType
            )
        } catch (e: Exception) {
            e.printStackTrace()
            AppSettingsBackup()
        }
    }

    /**
     * Applies restored settings into preferences managers.
     */
    fun applySettings(context: Context, settings: AppSettingsBackup) {
        try {
            settings.theme?.let {
                ThemePreferences.getInstance(context).updateConfig(it)
            }
            settings.currency?.let {
                CurrencyPreferences.getInstance(context).updateConfig(it)
            }
            settings.displayFormat?.let {
                DisplayFormatPreferences.getInstance(context).updateConfig(it)
            }
            settings.autofill?.let {
                AutofillPreferences.getInstance(context).updateConfig(it)
            }
            settings.tabConfig?.let {
                TabPreferences.getInstance(context).updateConfig(it)
            }
            val backupPrefs = BackupPreferences.getInstance(context)
            var bConfig = backupPrefs.config.value
            settings.isDualSyncEnabled?.let { bConfig = bConfig.copy(isDualSyncEnabled = it) }
            settings.autoPhoneBackup?.let { bConfig = bConfig.copy(isAutoPhoneBackupEnabled = it) }
            settings.scheduledHour?.let { bConfig = bConfig.copy(scheduledBackupHour = it) }
            settings.scheduledMinute?.let { bConfig = bConfig.copy(scheduledBackupMinute = it) }
            settings.uploadAttachments?.let { bConfig = bConfig.copy(uploadAttachments = it) }
            settings.autoSyncData?.let { bConfig = bConfig.copy(autoSyncData = it) }
            settings.autoSyncOnAppStart?.let { bConfig = bConfig.copy(autoSyncOnAppStart = it) }
            settings.autoSyncOnAppClose?.let { bConfig = bConfig.copy(autoSyncOnAppClose = it) }
            settings.wifiOnly?.let { bConfig = bConfig.copy(wifiOnly = it) }

            var prim = bConfig.primaryAccount
            settings.primaryAutoSync?.let { prim = prim.copy(autoSync = it) }
            settings.primaryWifiOnly?.let { prim = prim.copy(wifiOnly = it) }
            settings.primaryFolderType?.let { prim = prim.copy(driveFolderType = it) }
            bConfig = bConfig.copy(primaryAccount = prim)

            var sec = bConfig.secondaryAccount
            settings.secondaryAutoSync?.let { sec = sec.copy(autoSync = it) }
            settings.secondaryWifiOnly?.let { sec = sec.copy(wifiOnly = it) }
            settings.secondaryFolderType?.let { sec = sec.copy(driveFolderType = it) }
            bConfig = bConfig.copy(secondaryAccount = sec)

            backupPrefs.updateConfig(bConfig)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Parses a backup file from a URI to preview contents.
     */
    fun parseBackupData(context: Context, uri: Uri): BudgetBackupData? {
        return try {
            val json = context.contentResolver.openInputStream(uri)?.use { it.bufferedReader().readText() }
            if (!json.isNullOrBlank()) adapter.fromJson(json) else null
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Parses a backup file from a File handle to preview contents.
     */
    fun parseBackupDataFromFile(file: File): BudgetBackupData? {
        return try {
            if (file.exists()) {
                val json = file.readText()
                adapter.fromJson(json)
            } else null
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Parses a backup file from a JSON string.
     */
    fun parseBackupDataFromJson(json: String): BudgetBackupData? {
        return try {
            adapter.fromJson(json)
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Creates a JSON backup in the user's chosen folder ONLY.
     * Prevents duplicate writes to multiple locations at a time.
     */
    suspend fun createLocalBackupFile(
        context: Context,
        accountDao: AccountDao,
        categoryDao: CategoryDao,
        transactionDao: TransactionDao,
        recurringBillDao: RecurringBillDao,
        monthlyBudgetDao: MonthlyBudgetDao? = null,
        budgetAdjustmentDao: BudgetAdjustmentDao? = null,
        targetDirectory: String? = null,
        includeSettings: Boolean = true
    ): File = withContext(Dispatchers.IO) {
        val settingsBackup = if (includeSettings) captureSettings(context) else null
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
            settings = settingsBackup
        )
        val json = adapter.indent("  ").toJson(backupData)

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "Budgeter_Backup_$timeStamp.json"

        var savedFile: File? = null

        // 1. If custom Target Directory is specified, write ONLY to that directory
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
                            // Store mirror file in saf_backups cache so we can return a valid File handle
                            val cacheDir = File(context.cacheDir, "saf_backups").apply { if (!exists()) mkdirs() }
                            val mirror = File(cacheDir, fileName)
                            mirror.writeText(json)
                            savedFile = mirror
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else if (targetDirectory.equals("internal", ignoreCase = true) || targetDirectory.contains("files/backups")) {
                val internalDir = File(context.filesDir, "backups").apply { if (!exists()) mkdirs() }
                val internalFile = File(internalDir, fileName)
                internalFile.writeText(json)
                savedFile = internalFile
            } else if (targetDirectory.equals("Documents/Budgeter", ignoreCase = true) || targetDirectory.startsWith("Documents")) {
                val mediaStoreUri = writeBackupToMediaStoreDocuments(context, fileName, json)
                if (mediaStoreUri != null) {
                    val cacheDir = File(context.cacheDir, "saf_backups").apply { if (!exists()) mkdirs() }
                    val mirror = File(cacheDir, fileName)
                    mirror.writeText(json)
                    savedFile = mirror
                } else {
                    try {
                        val publicDocs = File(android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOCUMENTS), "Budgeter")
                        if (!publicDocs.exists()) publicDocs.mkdirs()
                        if (publicDocs.exists() && publicDocs.canWrite()) {
                            val f = File(publicDocs, fileName)
                            f.writeText(json)
                            savedFile = f
                        }
                    } catch (_: Exception) {}
                }
            } else {
                try {
                    val targetDir = when {
                        targetDirectory.equals("Downloads/Budgeter", ignoreCase = true) || targetDirectory.startsWith("Downloads") -> {
                            File(android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS), "Budgeter")
                        }
                        else -> File(targetDirectory)
                    }
                    if (!targetDir.exists()) targetDir.mkdirs()
                    if (targetDir.exists() && targetDir.canWrite()) {
                        val f = File(targetDir, fileName)
                        f.writeText(json)
                        savedFile = f
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        // 2. Only if custom directory was NOT supplied or write failed, write to single default directory (Documents/Budgeter or internal)
        if (savedFile == null) {
            val mediaStoreUri = writeBackupToMediaStoreDocuments(context, fileName, json)
            if (mediaStoreUri != null) {
                val cacheDir = File(context.cacheDir, "saf_backups").apply { if (!exists()) mkdirs() }
                val mirror = File(cacheDir, fileName)
                mirror.writeText(json)
                savedFile = mirror
            } else {
                val internalDir = File(context.filesDir, "backups").apply { if (!exists()) mkdirs() }
                val internalFile = File(internalDir, fileName)
                internalFile.writeText(json)
                savedFile = internalFile
            }
        }

        // Prune old local backups to keep latest 5
        try {
            pruneLocalBackups(context, 5, targetDirectory)
        } catch (_: Exception) {}

        savedFile
    }

    /**
     * Retains only the most recent [maxKeep] local backups, deleting older ones.
     */
    fun pruneLocalBackups(context: Context, maxKeep: Int = 5, customDirectoryPath: String? = null) {
        try {
            val allLocal = listLocalBackups(context, customDirectoryPath)
            if (allLocal.size > maxKeep) {
                // listLocalBackups returns files sorted descending by modification time
                val toDelete = allLocal.drop(maxKeep)
                for (oldFile in toDelete) {
                    deleteLocalBackup(context, oldFile, customDirectoryPath)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
     * Saves an export file (CSV, HTML, JSON, etc.) into the user's selected local sync folder
     * inside the "Exports" subfolder.
     */
    fun saveExportToLocalFolder(
        context: Context,
        fileName: String,
        mimeType: String,
        content: String,
        targetDirectory: String? = null
    ): String? {
        val selectedDir = targetDirectory ?: BackupPreferences.getInstance(context).getLocalBackupDirectory()
        return try {
            if (selectedDir.startsWith("content://")) {
                try {
                    val treeUri = Uri.parse(selectedDir)
                    val pickedDir = androidx.documentfile.provider.DocumentFile.fromTreeUri(context, treeUri)
                    if (pickedDir != null && pickedDir.exists() && pickedDir.canWrite()) {
                        val exportsDir = pickedDir.findFile("Exports") ?: pickedDir.createDirectory("Exports")
                        if (exportsDir != null && exportsDir.exists() && exportsDir.canWrite()) {
                            val existing = exportsDir.findFile(fileName)
                            existing?.delete()
                            val newDoc = exportsDir.createFile(mimeType, fileName)
                            if (newDoc != null) {
                                context.contentResolver.openOutputStream(newDoc.uri)?.use { out ->
                                    out.write(content.toByteArray(Charsets.UTF_8))
                                    out.flush()
                                }
                                val folderName = pickedDir.name ?: "SyncFolder"
                                return "$folderName/Exports/$fileName"
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else if (selectedDir.equals("internal", ignoreCase = true) || selectedDir.contains("files/backups")) {
                val exportsDir = File(context.filesDir, "Exports").apply { if (!exists()) mkdirs() }
                val dest = File(exportsDir, fileName)
                dest.writeText(content)
                return "Internal/Exports/$fileName"
            } else if (selectedDir.equals("Documents/Budgeter", ignoreCase = true) || selectedDir.startsWith("Documents")) {
                var savedPath: String? = null
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    try {
                        val cv = android.content.ContentValues().apply {
                            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/Budgeter/Exports")
                            put(MediaStore.MediaColumns.IS_PENDING, 1)
                        }
                        val collection = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                        val itemUri = context.contentResolver.insert(collection, cv)
                        if (itemUri != null) {
                            context.contentResolver.openOutputStream(itemUri)?.use { out ->
                                out.write(content.toByteArray(Charsets.UTF_8))
                                out.flush()
                            }
                            cv.clear()
                            cv.put(MediaStore.MediaColumns.IS_PENDING, 0)
                            context.contentResolver.update(itemUri, cv, null, null)
                            savedPath = "Documents/Budgeter/Exports/$fileName"
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                if (savedPath == null) {
                    try {
                        val publicDocs = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "Budgeter/Exports")
                        if (!publicDocs.exists()) publicDocs.mkdirs()
                        if (publicDocs.exists() && publicDocs.canWrite()) {
                            val f = File(publicDocs, fileName)
                            f.writeText(content)
                            savedPath = "Documents/Budgeter/Exports/$fileName"
                        }
                    } catch (_: Exception) {}
                }
                if (savedPath != null) return savedPath
            } else if (selectedDir.equals("Downloads/Budgeter", ignoreCase = true) || selectedDir.startsWith("Downloads")) {
                var savedPath: String? = null
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    try {
                        val cv = android.content.ContentValues().apply {
                            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/Budgeter/Exports")
                            put(MediaStore.MediaColumns.IS_PENDING, 1)
                        }
                        val collection = MediaStore.Downloads.EXTERNAL_CONTENT_URI
                        val itemUri = context.contentResolver.insert(collection, cv)
                        if (itemUri != null) {
                            context.contentResolver.openOutputStream(itemUri)?.use { out ->
                                out.write(content.toByteArray(Charsets.UTF_8))
                                out.flush()
                            }
                            cv.clear()
                            cv.put(MediaStore.MediaColumns.IS_PENDING, 0)
                            context.contentResolver.update(itemUri, cv, null, null)
                            savedPath = "Downloads/Budgeter/Exports/$fileName"
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                if (savedPath == null) {
                    try {
                        val publicDownloads = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Budgeter/Exports")
                        if (!publicDownloads.exists()) publicDownloads.mkdirs()
                        if (publicDownloads.exists() && publicDownloads.canWrite()) {
                            val f = File(publicDownloads, fileName)
                            f.writeText(content)
                            savedPath = "Downloads/Budgeter/Exports/$fileName"
                        }
                    } catch (_: Exception) {}
                }
                if (savedPath != null) return savedPath
            } else {
                try {
                    val customDir = File(selectedDir, "Exports")
                    if (!customDir.exists()) customDir.mkdirs()
                    if (customDir.exists() && customDir.canWrite()) {
                        val f = File(customDir, fileName)
                        f.writeText(content)
                        return "${customDir.name}/$fileName"
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // Fallback to internal/Exports
            val fallbackDir = File(context.filesDir, "Exports").apply { if (!exists()) mkdirs() }
            val f = File(fallbackDir, fileName)
            f.writeText(content)
            "Internal/Exports/$fileName"
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
        monthlyBudgetDao: MonthlyBudgetDao? = null,
        budgetAdjustmentDao: BudgetAdjustmentDao? = null,
        includeSettings: Boolean = true
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val settingsBackup = if (includeSettings) captureSettings(context) else null
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
                settings = settingsBackup
            )
            val json = adapter.indent("  ").toJson(backupData)

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(json.toByteArray(Charsets.UTF_8))
                outputStream.flush()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Restores the database from a backup URI with selective restore options.
     */
    suspend fun restoreBackupFromUri(
        context: Context,
        uri: Uri,
        accountDao: AccountDao,
        categoryDao: CategoryDao,
        transactionDao: TransactionDao,
        recurringBillDao: RecurringBillDao,
        monthlyBudgetDao: MonthlyBudgetDao? = null,
        budgetAdjustmentDao: BudgetAdjustmentDao? = null,
        restoreData: Boolean = true,
        restoreSettings: Boolean = true
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val json = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader().readText()
            } ?: return@withContext Result.failure(Exception("Cannot open backup file"))

            restoreFromJson(
                context = context,
                json = json,
                accountDao = accountDao,
                categoryDao = categoryDao,
                transactionDao = transactionDao,
                recurringBillDao = recurringBillDao,
                monthlyBudgetDao = monthlyBudgetDao,
                budgetAdjustmentDao = budgetAdjustmentDao,
                restoreData = restoreData,
                restoreSettings = restoreSettings
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Restores the database from a JSON string with selective restore support.
     */
    suspend fun restoreFromJson(
        context: Context,
        json: String,
        accountDao: AccountDao,
        categoryDao: CategoryDao,
        transactionDao: TransactionDao,
        recurringBillDao: RecurringBillDao,
        monthlyBudgetDao: MonthlyBudgetDao? = null,
        budgetAdjustmentDao: BudgetAdjustmentDao? = null,
        restoreData: Boolean = true,
        restoreSettings: Boolean = true
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val backupData = adapter.fromJson(json)
                ?: return@withContext Result.failure(Exception("Invalid backup file format"))

            var recordsCount = 0

            if (restoreData) {
                transactionDao.deleteAll()
                recurringBillDao.deleteAll()
                monthlyBudgetDao?.deleteAll()
                budgetAdjustmentDao?.deleteAll()
                categoryDao.deleteAll()
                accountDao.deleteAll()

                if (backupData.accounts.isNotEmpty()) {
                    accountDao.insertAccounts(backupData.accounts)
                    recordsCount += backupData.accounts.size
                }
                if (backupData.categories.isNotEmpty()) {
                    categoryDao.insertCategories(backupData.categories)
                    recordsCount += backupData.categories.size
                }
                if (backupData.transactions.isNotEmpty()) {
                    transactionDao.insertTransactions(backupData.transactions)
                    recordsCount += backupData.transactions.size
                }
                if (backupData.recurringBills.isNotEmpty()) {
                    recurringBillDao.insertAll(backupData.recurringBills)
                    recordsCount += backupData.recurringBills.size
                }
                if (backupData.monthlyBudgets.isNotEmpty() && monthlyBudgetDao != null) {
                    monthlyBudgetDao.upsertBudgets(backupData.monthlyBudgets)
                    recordsCount += backupData.monthlyBudgets.size
                }
                if (backupData.budgetAdjustments.isNotEmpty() && budgetAdjustmentDao != null) {
                    budgetAdjustmentDao.insertAdjustments(backupData.budgetAdjustments)
                    recordsCount += backupData.budgetAdjustments.size
                }
            }

            if (restoreSettings && backupData.settings != null) {
                applySettings(context, backupData.settings)
                recordsCount += 1
            }

            Result.success(recordsCount)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Smartly merges backup data into existing database records without wiping existing data.
     * Prevents duplicate accounts, categories, transactions, bills, and budgets.
     */
    suspend fun mergeFromJson(
        context: Context,
        json: String,
        accountDao: AccountDao,
        categoryDao: CategoryDao,
        transactionDao: TransactionDao,
        recurringBillDao: RecurringBillDao,
        monthlyBudgetDao: MonthlyBudgetDao? = null,
        budgetAdjustmentDao: BudgetAdjustmentDao? = null,
        restoreSettings: Boolean = false
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val backupData = adapter.fromJson(json)
                ?: return@withContext Result.failure(Exception("Invalid backup file format"))

            var mergedCount = 0

            // 1. Existing Data Snapshot
            val existingAccounts = accountDao.getAllAccountsSnapshot()
            val existingCategories = categoryDao.getAllCategoriesSnapshot()
            val existingTransactions = transactionDao.getAllTransactionsSnapshot()
            val existingBills = recurringBillDao.getAllBillsSnapshot()
            val existingBudgets = monthlyBudgetDao?.getAllBudgetsSnapshot() ?: emptyList()
            val existingAdjustments = budgetAdjustmentDao?.getAllAdjustmentsSnapshot() ?: emptyList()

            // 2. Map or insert Accounts
            val accountIdMap = mutableMapOf<Long, Long>()
            val currentAccountNames = existingAccounts.associateBy { "${it.type}_${it.nameEn.trim().lowercase()}" }
            val currentAccountIds = existingAccounts.associateBy { it.id }

            for (acc in backupData.accounts) {
                val matchByName = currentAccountNames["${acc.type}_${acc.nameEn.trim().lowercase()}"]
                val matchById = currentAccountIds[acc.id]
                val existingTarget = matchByName ?: matchById
                if (existingTarget != null) {
                    accountIdMap[acc.id] = existingTarget.id
                } else {
                    val newId = accountDao.insertAccount(acc.copy(id = 0))
                    accountIdMap[acc.id] = newId
                    mergedCount++
                }
            }

            // 3. Map or insert Categories
            val categoryIdMap = mutableMapOf<Long, Long>()
            val currentCategoryNames = existingCategories.associateBy { "${it.type}_${it.nameEn.trim().lowercase()}" }
            val currentCategoryIds = existingCategories.associateBy { it.id }

            for (cat in backupData.categories) {
                val matchByName = currentCategoryNames["${cat.type}_${cat.nameEn.trim().lowercase()}"]
                val matchById = currentCategoryIds[cat.id]
                val existingTarget = matchByName ?: matchById
                if (existingTarget != null) {
                    categoryIdMap[cat.id] = existingTarget.id
                } else {
                    val mappedParentId = cat.parentId?.let { categoryIdMap[it] ?: it }
                    val newId = categoryDao.insertCategory(cat.copy(id = 0, parentId = mappedParentId))
                    categoryIdMap[cat.id] = newId
                    mergedCount++
                }
            }

            // 4. Merge Transactions
            val existingTxSignatures = existingTransactions.map {
                "${it.dateEpochMs}_${it.amount}_${it.type}_${it.note.trim()}_${it.debitAccountId}_${it.creditAccountId}"
            }.toMutableSet()

            val txToInsert = mutableListOf<Transaction>()
            for (tx in backupData.transactions) {
                val mappedDebit = tx.debitAccountId?.let { accountIdMap[it] ?: it }
                val mappedCredit = tx.creditAccountId?.let { accountIdMap[it] ?: it }
                val mappedCat = tx.categoryId?.let { categoryIdMap[it] ?: it }
                val mappedSubCat = tx.subCategoryId?.let { categoryIdMap[it] ?: it }
                val sig = "${tx.dateEpochMs}_${tx.amount}_${tx.type}_${tx.note.trim()}_${mappedDebit}_${mappedCredit}"
                if (!existingTxSignatures.contains(sig)) {
                    txToInsert.add(
                        tx.copy(
                            id = 0,
                            debitAccountId = mappedDebit,
                            creditAccountId = mappedCredit,
                            categoryId = mappedCat,
                            subCategoryId = mappedSubCat
                        )
                    )
                    existingTxSignatures.add(sig)
                }
            }
            if (txToInsert.isNotEmpty()) {
                transactionDao.insertTransactions(txToInsert)
                mergedCount += txToInsert.size
            }

            // 5. Merge Recurring Bills
            val existingBillTitles = existingBills.map { "${it.title.trim().lowercase()}_${it.amount}" }.toSet()
            val billsToInsert = mutableListOf<RecurringBill>()
            for (bill in backupData.recurringBills) {
                val key = "${bill.title.trim().lowercase()}_${bill.amount}"
                if (!existingBillTitles.contains(key)) {
                    val mappedDebit = bill.debitAccountId?.let { accountIdMap[it] ?: it }
                    val mappedCredit = bill.creditAccountId?.let { accountIdMap[it] ?: it }
                    val mappedCat = bill.categoryId?.let { categoryIdMap[it] ?: it }
                    val mappedSubCat = bill.subCategoryId?.let { categoryIdMap[it] ?: it }
                    billsToInsert.add(
                        bill.copy(
                            id = 0,
                            debitAccountId = mappedDebit,
                            creditAccountId = mappedCredit,
                            categoryId = mappedCat,
                            subCategoryId = mappedSubCat
                        )
                    )
                }
            }
            if (billsToInsert.isNotEmpty()) {
                recurringBillDao.insertAll(billsToInsert)
                mergedCount += billsToInsert.size
            }

            // 6. Merge Monthly Budgets & Adjustments
            if (monthlyBudgetDao != null && backupData.monthlyBudgets.isNotEmpty()) {
                val budgetsToUpsert = backupData.monthlyBudgets.map { b ->
                    val mappedItemId = when (b.itemType.uppercase()) {
                        "EXPENSE", "INCOME" -> categoryIdMap[b.itemId] ?: b.itemId
                        "ASSET", "LIABILITY" -> accountIdMap[b.itemId] ?: b.itemId
                        else -> b.itemId
                    }
                    b.copy(id = 0, itemId = mappedItemId)
                }
                monthlyBudgetDao.upsertBudgets(budgetsToUpsert)
                mergedCount += budgetsToUpsert.size
            }

            if (budgetAdjustmentDao != null && backupData.budgetAdjustments.isNotEmpty()) {
                val existingAdjKeys = existingAdjustments.map { "${it.year}_${it.month}_${it.itemType}_${it.itemId}_${it.note.trim()}" }.toSet()
                val adjToInsert = mutableListOf<BudgetAdjustment>()
                for (adj in backupData.budgetAdjustments) {
                    val mappedItemId = when (adj.itemType.uppercase()) {
                        "EXPENSE", "INCOME" -> categoryIdMap[adj.itemId] ?: adj.itemId
                        "ASSET", "LIABILITY" -> accountIdMap[adj.itemId] ?: adj.itemId
                        else -> adj.itemId
                    }
                    val key = "${adj.year}_${adj.month}_${adj.itemType}_${mappedItemId}_${adj.note.trim()}"
                    if (!existingAdjKeys.contains(key)) {
                        adjToInsert.add(adj.copy(id = 0, itemId = mappedItemId))
                    }
                }
                if (adjToInsert.isNotEmpty()) {
                    budgetAdjustmentDao.insertAdjustments(adjToInsert)
                    mergedCount += adjToInsert.size
                }
            }

            // 7. Settings (if requested)
            if (restoreSettings && backupData.settings != null) {
                applySettings(context, backupData.settings)
                mergedCount += 1
            }

            Result.success(mergedCount)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Lists local backups from the specific configured directory ONLY.
     */
    fun listLocalBackups(context: Context, customDirectoryPath: String? = null): List<File> {
        val resultList = mutableListOf<File>()

        if (!customDirectoryPath.isNullOrBlank()) {
            if (customDirectoryPath.startsWith("content://")) {
                try {
                    val treeUri = Uri.parse(customDirectoryPath)
                    val pickedDir = androidx.documentfile.provider.DocumentFile.fromTreeUri(context, treeUri)
                    if (pickedDir != null && pickedDir.exists() && pickedDir.isDirectory) {
                        val cacheDir = File(context.cacheDir, "saf_backups").apply { if (!exists()) mkdirs() }
                        pickedDir.listFiles().forEach { doc ->
                            if (doc.isFile && (doc.name?.endsWith(".json") == true || doc.name?.endsWith(".db") == true)) {
                                val name = doc.name ?: return@forEach
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
                return resultList.distinctBy { it.name }.sortedByDescending { it.lastModified() }
            } else if (customDirectoryPath.equals("internal", ignoreCase = true) || customDirectoryPath.contains("files/backups")) {
                val internalDir = File(context.filesDir, "backups")
                if (internalDir.exists() && internalDir.isDirectory) {
                    internalDir.listFiles { file -> file.extension == "json" || file.extension == "db" }?.let {
                        resultList.addAll(it)
                    }
                }
                return resultList.distinctBy { it.name }.sortedByDescending { it.lastModified() }
            } else {
                val targetDir = when {
                    customDirectoryPath.equals("Documents/Budgeter", ignoreCase = true) || customDirectoryPath.startsWith("Documents") -> {
                        File(android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOCUMENTS), "Budgeter")
                    }
                    customDirectoryPath.equals("Downloads/Budgeter", ignoreCase = true) || customDirectoryPath.startsWith("Downloads") -> {
                        File(android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS), "Budgeter")
                    }
                    else -> File(customDirectoryPath)
                }
                if (targetDir.exists() && targetDir.isDirectory) {
                    targetDir.listFiles { file -> file.extension == "json" || file.extension == "db" }?.let {
                        resultList.addAll(it)
                    }
                }
                // Also load MediaStore copies if Documents/Budgeter
                if (customDirectoryPath.equals("Documents/Budgeter", ignoreCase = true) || customDirectoryPath.startsWith("Documents")) {
                    val safCache = File(context.cacheDir, "saf_backups")
                    if (safCache.exists()) {
                        safCache.listFiles { file -> file.extension == "json" || file.extension == "db" }?.let {
                            resultList.addAll(it)
                        }
                    }
                }
                return resultList.distinctBy { it.name }.sortedByDescending { it.lastModified() }
            }
        }

        // Default: Documents/Budgeter or internal files/backups if custom not configured
        try {
            val docsDir = File(android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOCUMENTS), "Budgeter")
            if (docsDir.exists() && docsDir.isDirectory) {
                docsDir.listFiles { file -> file.extension == "json" || file.extension == "db" }?.let {
                    resultList.addAll(it)
                }
            }
        } catch (_: Exception) {}

        val safCache = File(context.cacheDir, "saf_backups")
        if (safCache.exists()) {
            safCache.listFiles { file -> file.extension == "json" || file.extension == "db" }?.let {
                resultList.addAll(it)
            }
        }

        val defaultDir = File(context.filesDir, "backups")
        if (defaultDir.exists()) {
            defaultDir.listFiles { file -> file.extension == "json" || file.extension == "db" }?.let {
                resultList.addAll(it)
            }
        }

        return resultList.distinctBy { it.name }.sortedByDescending { it.lastModified() }
    }

    /**
     * Formats human readable display name for the configured backup directory
     */
    fun formatDirectoryDisplayName(path: String?): String {
        if (path.isNullOrBlank()) return "Documents/Budgeter"
        if (path.equals("internal", ignoreCase = true) || path.contains("files/backups")) return "App Internal (files/backups)"
        if (path.startsWith("content://")) {
            return try {
                val decoded = Uri.decode(path)
                val treePart = decoded.substringAfter("/tree/").substringBefore("/document/")
                if (treePart.contains(":")) {
                    val rootType = if (treePart.startsWith("primary")) "Device Storage" else "SD Card"
                    val folder = treePart.substringAfter(":")
                    if (folder.isBlank()) rootType else "$rootType > $folder"
                } else {
                    "Custom Storage Folder (SAF)"
                }
            } catch (_: Exception) {
                "Custom Storage Folder (SAF)"
            }
        }
        return path
    }

    /**
     * Robustly deletes a local backup file across SAF Tree URI, MediaStore Documents,
     * external public folders, internal storage, and cache mirror.
     */
    fun deleteLocalBackup(context: Context, file: File, customDirectoryPath: String? = null): Boolean {
        var deleted = false
        try {
            // 1. If custom SAF tree URI
            if (!customDirectoryPath.isNullOrBlank() && customDirectoryPath.startsWith("content://")) {
                try {
                    val treeUri = Uri.parse(customDirectoryPath)
                    val pickedDir = androidx.documentfile.provider.DocumentFile.fromTreeUri(context, treeUri)
                    pickedDir?.listFiles()?.forEach { doc ->
                        if (doc.name == file.name) {
                            if (doc.delete()) {
                                deleted = true
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // 2. MediaStore Documents / Files query deletion (Android 10+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                try {
                    val collection = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                    val selection = "${MediaStore.MediaColumns.DISPLAY_NAME} = ?"
                    val selectionArgs = arrayOf(file.name)
                    val rows = context.contentResolver.delete(collection, selection, selectionArgs)
                    if (rows > 0) deleted = true
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // 3. Delete from public Documents / Downloads / Custom folder
            try {
                val publicDocs = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "Budgeter")
                val targetFileInDocs = File(publicDocs, file.name)
                if (targetFileInDocs.exists()) {
                    if (targetFileInDocs.delete()) deleted = true
                }
            } catch (_: Exception) {}

            try {
                val publicDownloads = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Budgeter")
                val targetFileInDownloads = File(publicDownloads, file.name)
                if (targetFileInDownloads.exists()) {
                    if (targetFileInDownloads.delete()) deleted = true
                }
            } catch (_: Exception) {}

            if (!customDirectoryPath.isNullOrBlank() && !customDirectoryPath.startsWith("content://") && !customDirectoryPath.equals("internal", ignoreCase = true)) {
                try {
                    val customDir = File(customDirectoryPath)
                    val targetInCustom = File(customDir, file.name)
                    if (targetInCustom.exists()) {
                        if (targetInCustom.delete()) deleted = true
                    }
                } catch (_: Exception) {}
            }

            // 4. Delete from internal files/backups
            try {
                val internalDir = File(context.filesDir, "backups")
                val targetInternal = File(internalDir, file.name)
                if (targetInternal.exists()) {
                    if (targetInternal.delete()) deleted = true
                }
            } catch (_: Exception) {}

            // 5. Delete from cache saf_backups
            try {
                val cacheDir = File(context.cacheDir, "saf_backups")
                val targetCache = File(cacheDir, file.name)
                if (targetCache.exists()) {
                    if (targetCache.delete()) deleted = true
                }
            } catch (_: Exception) {}

            // 6. Direct file deletion
            if (file.exists()) {
                if (file.delete()) deleted = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return deleted
    }
}
