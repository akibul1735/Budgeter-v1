package com.example.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.local.AccountDao
import com.example.data.local.CategoryDao
import com.example.data.local.TransactionDao
import com.example.data.model.Account
import com.example.data.model.AccountType
import com.example.data.model.Category
import com.example.data.model.CategoryType
import com.example.data.model.Transaction
import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionType
import com.example.data.model.TransactionWithDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class CsvColumn(val key: String, val header: String, val labelEn: String, val labelBn: String) {
    TYPE("type", "Type", "Type", "ধরন"),
    DATE("date", "Date", "Date (YYYY-MM-DD)", "তারিখ"),
    SET_TIME("set_time", "Set Time", "Set Time (HH:mm:ss)", "সময়"),
    NAME("name", "Name", "Name / Payee", "নাম / প্রাপক"),
    AMOUNT("amount", "Amount", "Amount", "পরিমাণ"),
    CURRENCY("currency", "Currency", "Currency", "মুদ্রা"),
    EXCHANGE_RATE("exchange_rate", "Exchange Rate", "Exchange Rate", "বিনিময় হার"),
    CATEGORY_GROUP("category_group", "Category Group", "Category Group", "ক্যাটাগরি গ্রুপ"),
    CATEGORY("category", "Category", "Category", "ক্যাটাগরি"),
    ACCOUNT_CLASS("account_class", "Account Class", "Account Class", "অ্যাকাউন্ট শ্রেণি"),
    ACCOUNT_GROUPS("account_groups", "Account Groups", "Account Groups", "অ্যাকাউন্ট গ্রুপ"),
    ACCOUNT("account", "Account", "Account", "অ্যাকাউন্ট"),
    NOTES("notes", "Notes", "Notes", "নোট"),
    LABELS("labels", "Labels", "Labels / Tags", "লেবেল"),
    STATUS("status", "Status", "Status", "স্ট্যাটাস")
}

enum class CsvExportDateRange(val labelEn: String, val labelBn: String) {
    ALL_TIME("All Time", "সব সময়"),
    THIS_MONTH("This Month", "এই মাস"),
    LAST_MONTH("Last Month", "গত মাস"),
    THIS_YEAR("This Year", "এই বছর"),
    LAST_30_DAYS("Last 30 Days", "গত ৩০ দিন"),
    LAST_90_DAYS("Last 90 Days", "গত ৯০ দিন"),
    CUSTOM_RANGE("Custom Range", "নির্দিষ্ট সময়সীমা")
}

data class CsvExportConfig(
    val dateRangeType: CsvExportDateRange = CsvExportDateRange.ALL_TIME,
    val customStartDate: Long = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000L),
    val customEndDate: Long = System.currentTimeMillis(),
    val selectedTypes: Set<TransactionType> = setOf(
        TransactionType.EXPENSE,
        TransactionType.INCOME,
        TransactionType.TRANSFER
    ),
    val selectedAccountIds: Set<Long>? = null, // null means all
    val selectedCategoryIds: Set<Long>? = null, // null means all
    val includedColumns: Set<CsvColumn> = CsvColumn.entries.toSet()
)

data class ParsedCsvRow(
    val rawLineNumber: Int,
    val type: TransactionType,
    val dateEpochMs: Long,
    val dateFormatted: String,
    val timeFormatted: String,
    val name: String,
    val amount: Double,
    val rawAmount: String,
    val currency: String,
    val exchangeRate: Double,
    val categoryGroup: String,
    val category: String,
    val accountClass: String = "",
    val accountGroup: String,
    val account: String,
    val notes: String,
    val labels: String,
    val status: String,
    val isDuplicate: Boolean = false,
    val isValid: Boolean = true,
    val errorMessage: String? = null
)

data class CsvImportPreview(
    val totalRows: Int,
    val validRows: Int,
    val duplicateRows: Int,
    val sampleRows: List<ParsedCsvRow>,
    val newCategoryGroups: List<String>,
    val newCategories: List<Pair<String, String>>, // Group -> Category
    val newAccountGroups: List<String>,
    val newAccounts: List<Pair<String, String>> // Group -> Account
)

data class CsvImportResult(
    val importedCount: Int,
    val createdCategoriesCount: Int,
    val createdAccountsCount: Int,
    val skippedDuplicatesCount: Int
)

object CsvManager {

    private val DATE_FORMATS = listOf(
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd HH:mm",
        "yyyy-MM-dd'T'HH:mm:ss",
        "yyyy-MM-dd'T'HH:mm:ss.SSS",
        "yyyy/MM/dd HH:mm:ss",
        "yyyy/MM/dd HH:mm",
        "dd/MM/yyyy HH:mm:ss",
        "dd/MM/yyyy HH:mm",
        "MM/dd/yyyy HH:mm:ss",
        "MM/dd/yyyy HH:mm",
        "dd-MM-yyyy HH:mm:ss",
        "dd-MM-yyyy HH:mm",
        "yyyy-MM-dd",
        "dd/MM/yyyy",
        "MM/dd/yyyy",
        "dd-MM-yyyy",
        "yyyy/MM/dd",
        "dd.MM.yyyy",
        "d/M/yyyy",
        "M/d/yyyy",
        "dd-MMM-yyyy",
        "dd MMM yyyy",
        "MMM dd, yyyy",
        "yyyyMMdd",
        "MM/dd/yy",
        "dd/MM/yy"
    )

    private val TIME_FORMATS = listOf(
        "HH:mm:ss",
        "HH:mm",
        "hh:mm:ss a",
        "hh:mm a",
        "h:mm a",
        "h:mm:ss a",
        "H:mm"
    )

    /**
     * Parses the CSV file for preview before committing to database
     */
    suspend fun parseCsvForPreview(
        context: Context,
        uri: Uri,
        accountDao: AccountDao,
        categoryDao: CategoryDao,
        transactionDao: TransactionDao
    ): Result<CsvImportPreview> = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext Result.failure(Exception("Cannot open CSV file"))

            val reader = BufferedReader(InputStreamReader(inputStream))
            val lines = reader.readLines()
            if (lines.isEmpty()) {
                return@withContext Result.failure(Exception("CSV file is empty"))
            }

            // Load snapshots of existing records
            val existingAccounts = accountDao.getAllAccountsSnapshot()
            val existingCategories = categoryDao.getAllCategoriesSnapshot()
            val existingTransactions = transactionDao.getAllTransactionsSnapshot()

            val existingAccNames = existingAccounts.map { it.nameEn.lowercase().trim() }.toSet()
            val existingCatNames = existingCategories.map { it.nameEn.lowercase().trim() }.toSet()

            // Header mapping
            val (headerMap, headerIndex) = detectHeaderMapping(lines)
            val dataLines = lines.drop(headerIndex + 1).filter { it.isNotBlank() }

            if (dataLines.isEmpty()) {
                return@withContext Result.failure(Exception("No transaction data rows found in CSV"))
            }

            val parsedRows = mutableListOf<ParsedCsvRow>()
            val newCatGroups = mutableSetOf<String>()
            val newCats = mutableSetOf<Pair<String, String>>()
            val newAccGroups = mutableSetOf<String>()
            val newAccs = mutableSetOf<Pair<String, String>>()

            var duplicateCount = 0

            dataLines.forEachIndexed { index, line ->
                val tokens = parseCsvLine(line)
                if (tokens.isEmpty()) return@forEachIndexed

                val row = parseRowFromTokens(tokens, headerMap, index + headerIndex + 2)
                if (!row.isValid) return@forEachIndexed

                // Check duplicates (same date within +/- 2 minutes, same rounded amount, same type, matching note/name)
                val isDup = existingTransactions.any { existing ->
                    existing.type == row.type &&
                            Math.abs(existing.amount - row.amount) < 0.01 &&
                            Math.abs(existing.dateEpochMs - row.dateEpochMs) < (2 * 60 * 1000L)
                }

                val finalRow = row.copy(isDuplicate = isDup)
                if (isDup) duplicateCount++
                parsedRows.add(finalRow)

                // Track new Category Groups & Categories
                if (row.categoryGroup.isNotBlank() && !existingCatNames.contains(row.categoryGroup.lowercase().trim())) {
                    newCatGroups.add(row.categoryGroup.trim())
                }
                if (row.category.isNotBlank() && !existingCatNames.contains(row.category.lowercase().trim())) {
                    newCats.add(Pair(row.categoryGroup.trim(), row.category.trim()))
                }

                // Track new Account Groups & Accounts
                if (row.accountGroup.isNotBlank() && !existingAccNames.contains(row.accountGroup.lowercase().trim())) {
                    newAccGroups.add(row.accountGroup.trim())
                }
                if (row.account.isNotBlank() && !existingAccNames.contains(row.account.lowercase().trim())) {
                    newAccs.add(Pair(row.accountGroup.trim(), row.account.trim()))
                }
            }

            val preview = CsvImportPreview(
                totalRows = dataLines.size,
                validRows = parsedRows.size,
                duplicateRows = duplicateCount,
                sampleRows = parsedRows.take(15),
                newCategoryGroups = newCatGroups.toList(),
                newCategories = newCats.toList(),
                newAccountGroups = newAccGroups.toList(),
                newAccounts = newAccs.toList()
            )

            Result.success(preview)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Executes the actual import into Room database
     */
    suspend fun executeImport(
        context: Context,
        uri: Uri,
        accountDao: AccountDao,
        categoryDao: CategoryDao,
        transactionDao: TransactionDao,
        skipDuplicates: Boolean = true,
        autoCreateEntities: Boolean = true
    ): Result<CsvImportResult> = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext Result.failure(Exception("Cannot open CSV file"))

            val reader = BufferedReader(InputStreamReader(inputStream))
            val lines = reader.readLines()
            if (lines.isEmpty()) {
                return@withContext Result.failure(Exception("CSV file is empty"))
            }

            val (headerMap, headerIndex) = detectHeaderMapping(lines)
            val dataLines = lines.drop(headerIndex + 1).filter { it.isNotBlank() }

            val accounts = accountDao.getAllAccountsSnapshot().toMutableList()
            val categories = categoryDao.getAllCategoriesSnapshot().toMutableList()
            val existingTransactions = transactionDao.getAllTransactionsSnapshot().toMutableList()

            // Ensure fallback default account
            val defaultAccount = accounts.firstOrNull { it.parentId != null } ?: accounts.firstOrNull()
                ?: run {
                    val acc = Account(
                        nameEn = "Default Cash",
                        nameBn = "ডিফল্ট ক্যাশ",
                        type = AccountType.ASSET,
                        iconName = "Wallet",
                        colorHex = "#10B981"
                    )
                    val id = accountDao.insertAccount(acc)
                    acc.copy(id = id).also { accounts.add(it) }
                }

            // Ensure fallback default expense category
            val defaultExpenseCat = categories.firstOrNull { it.type == CategoryType.EXPENSE && it.parentId != null }
                ?: categories.firstOrNull { it.type == CategoryType.EXPENSE }
                ?: run {
                    val cat = Category(
                        nameEn = "General Expense",
                        nameBn = "সাধারণ খরচ",
                        type = CategoryType.EXPENSE,
                        iconName = "Category",
                        colorHex = "#EF4444"
                    )
                    val id = categoryDao.insertCategory(cat)
                    cat.copy(id = id).also { categories.add(it) }
                }

            // Ensure fallback default income category
            val defaultIncomeCat = categories.firstOrNull { it.type == CategoryType.INCOME && it.parentId != null }
                ?: categories.firstOrNull { it.type == CategoryType.INCOME }
                ?: run {
                    val cat = Category(
                        nameEn = "General Income",
                        nameBn = "সাধারণ আয়",
                        type = CategoryType.INCOME,
                        iconName = "AttachMoney",
                        colorHex = "#10B981"
                    )
                    val id = categoryDao.insertCategory(cat)
                    cat.copy(id = id).also { categories.add(it) }
                }

            var importedCount = 0
            var createdCatCount = 0
            var createdAccCount = 0
            var skippedDupCount = 0

            val transactionsToInsert = mutableListOf<Transaction>()

            for ((index, line) in dataLines.withIndex()) {
                val tokens = parseCsvLine(line)
                if (tokens.isEmpty()) continue

                val row = parseRowFromTokens(tokens, headerMap, index + headerIndex + 2)
                if (!row.isValid || row.amount <= 0.0) continue

                // Check duplicate
                val isDup = existingTransactions.any { existing ->
                    existing.type == row.type &&
                            Math.abs(existing.amount - row.amount) < 0.01 &&
                            Math.abs(existing.dateEpochMs - row.dateEpochMs) < (2 * 60 * 1000L)
                }

                if (isDup && skipDuplicates) {
                    skippedDupCount++
                    continue
                }

                // 1. Resolve Account & Account Group
                var resolvedAccount = defaultAccount
                val isLiability = row.accountClass.lowercase().contains("liabilit") ||
                        row.accountClass.lowercase().contains("loan") ||
                        row.accountClass.lowercase().contains("credit") ||
                        row.accountClass.lowercase().contains("debt") ||
                        row.accountGroup.lowercase().contains("loan") ||
                        row.accountGroup.lowercase().contains("credit") ||
                        row.accountGroup.lowercase().contains("debt") ||
                        row.account.lowercase().contains("loan") ||
                        row.account.lowercase().contains("credit") ||
                        row.account.lowercase().contains("debt")
                val accType = if (isLiability) AccountType.LIABILITY else AccountType.ASSET

                if (row.account.isNotBlank() && autoCreateEntities) {
                    // Check if parent account group exists
                    var parentAccId: Long? = null
                    if (row.accountGroup.isNotBlank()) {
                        val parent = accounts.find {
                            it.parentId == null &&
                                    (it.nameEn.equals(row.accountGroup, ignoreCase = true) || it.nameBn.equals(row.accountGroup, ignoreCase = true))
                        }
                        if (parent != null) {
                            parentAccId = parent.id
                        } else {
                            val newParent = Account(
                                nameEn = row.accountGroup,
                                nameBn = row.accountGroup,
                                type = accType,
                                parentId = null,
                                iconName = if (isLiability) "CreditCard" else "AccountBalance",
                                colorHex = if (isLiability) "#EF4444" else "#1E56A0"
                            )
                            val newParentId = accountDao.insertAccount(newParent)
                            val savedParent = newParent.copy(id = newParentId)
                            accounts.add(savedParent)
                            parentAccId = newParentId
                            createdAccCount++
                        }
                    }

                    // Check if child account exists
                    val existingAcc = accounts.find {
                        (it.nameEn.equals(row.account, ignoreCase = true) || it.nameBn.equals(row.account, ignoreCase = true)) &&
                                (parentAccId == null || it.parentId == parentAccId || it.parentId == null)
                    }

                    if (existingAcc != null) {
                        resolvedAccount = existingAcc
                    } else {
                        val newAcc = Account(
                            nameEn = row.account,
                            nameBn = row.account,
                            type = accType,
                            parentId = parentAccId,
                            iconName = if (isLiability) "CreditCard" else "AccountBalance",
                            colorHex = if (isLiability) "#EF4444" else "#1E56A0"
                        )
                        val newAccId = accountDao.insertAccount(newAcc)
                        val savedAcc = newAcc.copy(id = newAccId)
                        accounts.add(savedAcc)
                        resolvedAccount = savedAcc
                        createdAccCount++
                    }
                } else if (row.account.isNotBlank()) {
                    resolvedAccount = accounts.find {
                        it.nameEn.equals(row.account, ignoreCase = true) || it.nameBn.equals(row.account, ignoreCase = true)
                    } ?: defaultAccount
                }

                // 2. Resolve Category & Category Group
                val isTransfer = row.type == TransactionType.TRANSFER
                val isTransferCategory = row.category.equals("(Transfer)", ignoreCase = true) ||
                        row.category.equals("Transfer", ignoreCase = true) ||
                        row.categoryGroup.equals("(Transfer)", ignoreCase = true) ||
                        row.categoryGroup.equals("Transfer", ignoreCase = true)

                var resolvedCategory: Category? = if (row.type == TransactionType.INCOME) defaultIncomeCat else defaultExpenseCat
                if (!isTransfer && !isTransferCategory && row.category.isNotBlank() && autoCreateEntities) {
                    var parentCatId: Long? = null
                    val catType = if (row.type == TransactionType.INCOME) CategoryType.INCOME else CategoryType.EXPENSE

                    if (row.categoryGroup.isNotBlank()) {
                        val parent = categories.find {
                            it.parentId == null && it.type == catType &&
                                    (it.nameEn.equals(row.categoryGroup, ignoreCase = true) || it.nameBn.equals(row.categoryGroup, ignoreCase = true))
                        }
                        if (parent != null) {
                            parentCatId = parent.id
                        } else {
                            val newParent = Category(
                                nameEn = row.categoryGroup,
                                nameBn = row.categoryGroup,
                                type = catType,
                                parentId = null,
                                iconName = "Category",
                                colorHex = if (catType == CategoryType.EXPENSE) "#EF4444" else "#10B981"
                            )
                            val newParentId = categoryDao.insertCategory(newParent)
                            val savedParent = newParent.copy(id = newParentId)
                            categories.add(savedParent)
                            parentCatId = newParentId
                            createdCatCount++
                        }
                    }

                    val existingCat = categories.find {
                        it.type == catType &&
                                (it.nameEn.equals(row.category, ignoreCase = true) || it.nameBn.equals(row.category, ignoreCase = true)) &&
                                (parentCatId == null || it.parentId == parentCatId || it.parentId == null)
                    }

                    if (existingCat != null) {
                        resolvedCategory = existingCat
                    } else {
                        val newCat = Category(
                            nameEn = row.category,
                            nameBn = row.category,
                            type = catType,
                            parentId = parentCatId,
                            iconName = "Category",
                            colorHex = if (catType == CategoryType.EXPENSE) "#F59E0B" else "#10B981"
                        )
                        val newCatId = categoryDao.insertCategory(newCat)
                        val savedCat = newCat.copy(id = newCatId)
                        categories.add(savedCat)
                        resolvedCategory = savedCat
                        createdCatCount++
                    }
                } else if (!isTransfer && !isTransferCategory && row.category.isNotBlank()) {
                    resolvedCategory = categories.find {
                        it.nameEn.equals(row.category, ignoreCase = true) || it.nameBn.equals(row.category, ignoreCase = true)
                    } ?: (if (row.type == TransactionType.INCOME) defaultIncomeCat else defaultExpenseCat)
                } else if (isTransfer || isTransferCategory) {
                    resolvedCategory = null
                }

                // 3. Resolve Transfers / Double Entry
                val rawAmt = parseAmount(row.rawAmount)
                val isOutflow = rawAmt < 0 || row.rawAmount.contains("(") || row.type == TransactionType.EXPENSE
                var sourceAcc: Account? = null
                var destAcc: Account? = null

                if (isTransfer) {
                    if (isOutflow) {
                        sourceAcc = resolvedAccount
                        if (row.name.isNotBlank() && !isTransferCategory) {
                            val counter = accounts.find {
                                it.nameEn.equals(row.name, ignoreCase = true) || it.nameBn.equals(row.name, ignoreCase = true)
                            }
                            if (counter != null) {
                                destAcc = counter
                            } else if (autoCreateEntities) {
                                val counterLiability = row.name.lowercase().contains("credit") || row.name.lowercase().contains("loan") || row.name.lowercase().contains("debt") || isLiability
                                val newCounter = Account(
                                    nameEn = row.name,
                                    nameBn = row.name,
                                    type = if (counterLiability) AccountType.LIABILITY else AccountType.ASSET,
                                    parentId = resolvedAccount.parentId,
                                    iconName = if (counterLiability) "CreditCard" else "AccountBalance",
                                    colorHex = if (counterLiability) "#EF4444" else "#1E56A0"
                                )
                                val newCounterId = accountDao.insertAccount(newCounter)
                                val savedCounter = newCounter.copy(id = newCounterId)
                                accounts.add(savedCounter)
                                createdAccCount++
                                destAcc = savedCounter
                            }
                        }
                    } else {
                        destAcc = resolvedAccount
                        if (row.name.isNotBlank() && !isTransferCategory) {
                            val counter = accounts.find {
                                it.nameEn.equals(row.name, ignoreCase = true) || it.nameBn.equals(row.name, ignoreCase = true)
                            }
                            if (counter != null) {
                                sourceAcc = counter
                            } else if (autoCreateEntities) {
                                val counterLiability = row.name.lowercase().contains("credit") || row.name.lowercase().contains("loan") || row.name.lowercase().contains("debt") || isLiability
                                val newCounter = Account(
                                    nameEn = row.name,
                                    nameBn = row.name,
                                    type = if (counterLiability) AccountType.LIABILITY else AccountType.ASSET,
                                    parentId = resolvedAccount.parentId,
                                    iconName = if (counterLiability) "CreditCard" else "AccountBalance",
                                    colorHex = if (counterLiability) "#EF4444" else "#1E56A0"
                                )
                                val newCounterId = accountDao.insertAccount(newCounter)
                                val savedCounter = newCounter.copy(id = newCounterId)
                                accounts.add(savedCounter)
                                createdAccCount++
                                sourceAcc = savedCounter
                            }
                        }
                    }
                }

                // 4. Format Notes & Payee
                val combinedNotes = buildString {
                    if (row.notes.isNotBlank()) append(row.notes)
                    if (row.labels.isNotBlank()) {
                        if (isNotEmpty()) append(" • ")
                        append("[Labels: ${row.labels}]")
                    }
                }

                val statusEnum = when {
                    row.status.lowercase().contains("reconciled") -> TransactionStatus.RECONCILED
                    row.status.lowercase().contains("cleared") -> TransactionStatus.CLEARED
                    row.status.lowercase().contains("void") -> TransactionStatus.VOID
                    else -> TransactionStatus.NONE
                }

                val tx = Transaction(
                    type = row.type,
                    amount = row.amount,
                    dateEpochMs = row.dateEpochMs,
                    debitAccountId = when {
                        isTransfer -> destAcc?.id
                        row.type == TransactionType.EXPENSE -> null
                        else -> resolvedAccount.id
                    },
                    creditAccountId = when {
                        isTransfer -> sourceAcc?.id
                        row.type == TransactionType.EXPENSE -> resolvedAccount.id
                        else -> null
                    },
                    categoryId = resolvedCategory?.id,
                    payeeOrPayer = row.name.ifEmpty { if (isTransfer) "Transfer" else "Imported" },
                    note = combinedNotes.ifEmpty { "Imported from CSV" },
                    status = statusEnum
                )

                transactionsToInsert.add(tx)
                existingTransactions.add(tx)
                importedCount++
            }

            if (transactionsToInsert.isNotEmpty()) {
                transactionDao.insertTransactions(transactionsToInsert)
            }

            Result.success(
                CsvImportResult(
                    importedCount = importedCount,
                    createdCategoriesCount = createdCatCount,
                    createdAccountsCount = createdAccCount,
                    skippedDuplicatesCount = skippedDupCount
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Generates CSV formatted string based on user's column and filter configuration
     */
    fun generateCsvString(
        transactions: List<TransactionWithDetails>,
        config: CsvExportConfig,
        currencyCode: String = "BDT"
    ): String {
        val filtered = filterTransactions(transactions, config)
        val sb = StringBuilder()

        // Write Header
        val activeColumns = CsvColumn.entries.filter { config.includedColumns.contains(it) }
        sb.append(activeColumns.joinToString(",") { escapeCsv(it.header) })
        sb.append("\n")

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.US)

        for (item in filtered) {
            val tx = item.transaction
            val date = Date(tx.dateEpochMs)

            val rowValues = activeColumns.map { col ->
                when (col) {
                    CsvColumn.TYPE -> tx.type.name
                    CsvColumn.DATE -> dateFormat.format(date)
                    CsvColumn.SET_TIME -> timeFormat.format(date)
                    CsvColumn.NAME -> tx.payeeOrPayer.ifEmpty { tx.note }
                    CsvColumn.AMOUNT -> String.format(Locale.US, "%.2f", tx.amount)
                    CsvColumn.CURRENCY -> currencyCode
                    CsvColumn.EXCHANGE_RATE -> "1.00"
                    CsvColumn.CATEGORY_GROUP -> item.category?.let { cat ->
                        if (cat.parentId != null) "Category Group" else cat.nameEn
                    } ?: ""
                    CsvColumn.CATEGORY -> item.category?.nameEn ?: ""
                    CsvColumn.ACCOUNT_CLASS -> {
                        val acc = if (tx.type == TransactionType.EXPENSE) item.creditAccount else item.debitAccount
                        if (acc?.type == AccountType.LIABILITY) "Liabilities" else "Assets"
                    }
                    CsvColumn.ACCOUNT_GROUPS -> {
                        val acc = if (tx.type == TransactionType.EXPENSE) item.creditAccount else item.debitAccount
                        if (acc?.parentId != null) "Accounts" else acc?.nameEn ?: ""
                    }
                    CsvColumn.ACCOUNT -> {
                        val acc = if (tx.type == TransactionType.EXPENSE) item.creditAccount else item.debitAccount
                        acc?.nameEn ?: ""
                    }
                    CsvColumn.NOTES -> tx.note
                    CsvColumn.LABELS -> ""
                    CsvColumn.STATUS -> tx.status.name
                }
            }

            sb.append(rowValues.joinToString(",") { escapeCsv(it) })
            sb.append("\n")
        }

        return sb.toString()
    }

    suspend fun exportCsvToUri(
        context: Context,
        uri: Uri,
        transactions: List<TransactionWithDetails>,
        config: CsvExportConfig,
        currencyCode: String = "BDT"
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val csvContent = generateCsvString(transactions, config, currencyCode)
            val outputStream = context.contentResolver.openOutputStream(uri) ?: return@withContext false
            val writer = OutputStreamWriter(outputStream)
            writer.write(csvContent)
            writer.flush()
            writer.close()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun exportCsvToCacheAndGetShareUri(
        context: Context,
        transactions: List<TransactionWithDetails>,
        config: CsvExportConfig,
        currencyCode: String = "BDT"
    ): Uri? = withContext(Dispatchers.IO) {
        try {
            val csvContent = generateCsvString(transactions, config, currencyCode)
            val dateStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val file = File(context.cacheDir, "Budgeter_Export_$dateStr.csv")
            val fos = FileOutputStream(file)
            fos.write(csvContent.toByteArray(Charsets.UTF_8))
            fos.flush()
            fos.close()

            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun filterTransactions(
        transactions: List<TransactionWithDetails>,
        config: CsvExportConfig
    ): List<TransactionWithDetails> {
        val (startTime, endTime) = computeDateRange(config)

        return transactions.filter { item ->
            val tx = item.transaction

            // Date Range
            val matchesDate = tx.dateEpochMs in startTime..endTime

            // Type
            val matchesType = config.selectedTypes.contains(tx.type)

            // Account
            val matchesAccount = if (config.selectedAccountIds == null) true else {
                val accId = if (tx.type == TransactionType.EXPENSE) tx.creditAccountId else tx.debitAccountId
                accId != null && config.selectedAccountIds.contains(accId)
            }

            // Category
            val matchesCategory = if (config.selectedCategoryIds == null) true else {
                tx.categoryId != null && config.selectedCategoryIds.contains(tx.categoryId)
            }

            matchesDate && matchesType && matchesAccount && matchesCategory
        }
    }

    fun computeDateRange(config: CsvExportConfig): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        return when (config.dateRangeType) {
            CsvExportDateRange.ALL_TIME -> Pair(0L, Long.MAX_VALUE)
            CsvExportDateRange.THIS_MONTH -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis

                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                val end = cal.timeInMillis
                Pair(start, end)
            }
            CsvExportDateRange.LAST_MONTH -> {
                cal.add(Calendar.MONTH, -1)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis

                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                val end = cal.timeInMillis
                Pair(start, end)
            }
            CsvExportDateRange.THIS_YEAR -> {
                cal.set(Calendar.DAY_OF_YEAR, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis

                cal.set(Calendar.DAY_OF_YEAR, cal.getActualMaximum(Calendar.DAY_OF_YEAR))
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                val end = cal.timeInMillis
                Pair(start, end)
            }
            CsvExportDateRange.LAST_30_DAYS -> {
                val end = System.currentTimeMillis()
                val start = end - (30L * 24 * 60 * 60 * 1000L)
                Pair(start, end)
            }
            CsvExportDateRange.LAST_90_DAYS -> {
                val end = System.currentTimeMillis()
                val start = end - (90L * 24 * 60 * 60 * 1000L)
                Pair(start, end)
            }
            CsvExportDateRange.CUSTOM_RANGE -> {
                Pair(config.customStartDate, config.customEndDate)
            }
        }
    }

    // Helper functions

    private fun detectHeaderMapping(lines: List<String>): Pair<Map<String, Int>, Int> {
        val headerMap = mutableMapOf<String, Int>()
        var headerIndex = 0

        for (i in 0 until minOf(5, lines.size)) {
            val tokens = parseCsvLine(lines[i]).map { it.lowercase().trim() }
            if (tokens.any { it.contains("type") || it.contains("date") || it.contains("amount") || it.contains("category") || it.contains("account") }) {
                headerIndex = i
                tokens.forEachIndexed { colIdx, colName ->
                    val cleanCol = colName.replace("_", " ").replace("-", " ")
                    when {
                        cleanCol.contains("category group") || cleanCol.contains("category groups") || cleanCol.contains("cat group") || cleanCol.contains("cat groups") || cleanCol.contains("parent category") -> headerMap["category_group"] = colIdx
                        cleanCol.contains("account class") || cleanCol.contains("acc class") || cleanCol == "class" || cleanCol.contains("account type") -> headerMap["account_class"] = colIdx
                        cleanCol.contains("account group") || cleanCol.contains("account groups") || cleanCol.contains("acc group") || cleanCol.contains("acc groups") || cleanCol.contains("parent account") -> headerMap["account_groups"] = colIdx
                        cleanCol == "set time" || cleanCol.contains("set time") || cleanCol.contains("tx time") || cleanCol == "time" -> headerMap["set_time"] = colIdx
                        cleanCol.contains("exchange rate") || cleanCol.contains("fx rate") || cleanCol == "rate" -> headerMap["exchange_rate"] = colIdx
                        cleanCol.contains("type") -> headerMap["type"] = colIdx
                        cleanCol.contains("date") -> headerMap["date"] = colIdx
                        cleanCol.contains("amount") || cleanCol.contains("value") || cleanCol.contains("total") -> headerMap["amount"] = colIdx
                        cleanCol.contains("currency") || cleanCol.contains("curr") -> headerMap["currency"] = colIdx
                        cleanCol.contains("category") || cleanCol.contains("cat") -> headerMap["category"] = colIdx
                        cleanCol.contains("account") || cleanCol.contains("acc") -> headerMap["account"] = colIdx
                        cleanCol.contains("note") || cleanCol.contains("memo") || cleanCol.contains("description") -> headerMap["notes"] = colIdx
                        cleanCol.contains("name") || cleanCol.contains("payee") || cleanCol.contains("payer") -> headerMap["name"] = colIdx
                        cleanCol.contains("label") || cleanCol.contains("tag") -> headerMap["labels"] = colIdx
                        cleanCol.contains("status") -> headerMap["status"] = colIdx
                    }
                }
                break
            }
        }
        return Pair(headerMap, headerIndex)
    }

    private fun parseRowFromTokens(
        tokens: List<String>,
        headerMap: Map<String, Int>,
        lineNo: Int
    ): ParsedCsvRow {
        fun get(key: String, defaultIdx: Int? = null): String {
            val idx = headerMap[key] ?: defaultIdx
            return if (idx != null && idx < tokens.size) tokens[idx].trim() else ""
        }

        val typeStr = get("type", 0)
        val dateStr = get("date", 1)
        val timeStr = get("set_time", 2)
        val nameStr = get("name", 3)
        val amountStr = get("amount", 4)
        val currStr = get("currency", 5).ifEmpty { "BDT" }
        val fxRateStr = get("exchange_rate", 6)
        val catGroupStr = get("category_group", 7)
        val catStr = get("category", 8)
        val accClassStr = get("account_class", 9)
        val accGroupStr = get("account_groups", 10)
        val accStr = get("account", 11)
        val notesStr = get("notes", 12)
        val labelsStr = get("labels", 13)
        val statusStr = get("status", 14)

        val rawAmount = parseAmount(amountStr)
        val isNegative = rawAmount < 0 || amountStr.contains("(") || typeStr.lowercase().contains("expense")
        val parsedFxRate = parseAmount(fxRateStr).let { if (it > 0) it else 1.0 }

        val finalAmount = Math.abs(rawAmount) * parsedFxRate

        val txType = when {
            typeStr.lowercase().contains("transfer") -> TransactionType.TRANSFER
            typeStr.lowercase().contains("income") -> TransactionType.INCOME
            typeStr.lowercase().contains("expense") -> TransactionType.EXPENSE
            isNegative -> TransactionType.EXPENSE
            else -> TransactionType.EXPENSE
        }

        val dateEpoch = parseDateTimeEpoch(dateStr, timeStr)
        val dateFormatted = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(dateEpoch))
        val timeFormatted = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date(dateEpoch))

        val isValid = dateEpoch > 0 && finalAmount > 0.0

        return ParsedCsvRow(
            rawLineNumber = lineNo,
            type = txType,
            dateEpochMs = dateEpoch,
            dateFormatted = dateFormatted,
            timeFormatted = timeFormatted,
            name = nameStr.ifEmpty { if (notesStr.isNotEmpty()) notesStr.take(30) else "Transaction" },
            amount = finalAmount,
            rawAmount = amountStr,
            currency = currStr,
            exchangeRate = parsedFxRate,
            categoryGroup = catGroupStr,
            category = catStr,
            accountClass = accClassStr,
            accountGroup = accGroupStr,
            account = accStr,
            notes = notesStr,
            labels = labelsStr,
            status = statusStr,
            isValid = isValid,
            errorMessage = if (!isValid) "Invalid amount or date" else null
        )
    }

    private fun parseCsvLine(line: String): List<String> {
        val tokens = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false

        val cleanLine = if (line.startsWith("\uFEFF")) line.substring(1) else line

        for (ch in cleanLine) {
            when {
                ch == '\"' -> inQuotes = !inQuotes
                (ch == ',' || ch == '\t') && !inQuotes -> {
                    tokens.add(sb.toString().trim().replace("\"", ""))
                    sb.clear()
                }
                else -> sb.append(ch)
            }
        }
        tokens.add(sb.toString().trim().replace("\"", ""))
        return tokens
    }

    private fun parseAmount(str: String): Double {
        if (str.isBlank()) return 0.0
        val cleaned = str.replace("$", "").replace("৳", "").replace("€", "").replace("£", "").replace("₹", "")
            .replace(",", "").replace(" ", "").trim()
        return try {
            if (cleaned.startsWith("(") && cleaned.endsWith(")")) {
                -cleaned.substring(1, cleaned.length - 1).toDouble()
            } else {
                cleaned.toDouble()
            }
        } catch (e: Exception) {
            0.0
        }
    }

    private fun parseDateTimeEpoch(dateStr: String, timeStr: String): Long {
        if (dateStr.isBlank()) return System.currentTimeMillis()

        var parsedDate: Date? = null
        for (pattern in DATE_FORMATS) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale.US)
                sdf.isLenient = true
                val d = sdf.parse(dateStr.trim())
                if (d != null) {
                    parsedDate = d
                    break
                }
            } catch (_: Exception) {}
        }

        if (parsedDate == null) return System.currentTimeMillis()

        val cal = Calendar.getInstance()
        cal.time = parsedDate

        if (timeStr.isNotBlank()) {
            for (timePattern in TIME_FORMATS) {
                try {
                    val sdf = SimpleDateFormat(timePattern, Locale.US)
                    sdf.isLenient = true
                    val t = sdf.parse(timeStr.trim())
                    if (t != null) {
                        val tCal = Calendar.getInstance()
                        tCal.time = t
                        cal.set(Calendar.HOUR_OF_DAY, tCal.get(Calendar.HOUR_OF_DAY))
                        cal.set(Calendar.MINUTE, tCal.get(Calendar.MINUTE))
                        cal.set(Calendar.SECOND, tCal.get(Calendar.SECOND))
                        break
                    }
                } catch (_: Exception) {}
            }
        }

        return cal.timeInMillis
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
    }
}
