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

data class ColumnMapping(
    val csvHeaderIndex: Int,
    val csvHeaderName: String,
    val targetAppColumnKey: String? // e.g. "type", "date", "amount", "category", "account", "notes", etc.
)

data class UnsupportedRow(
    val lineNumber: Int,
    val rawTokens: List<String>,
    val reason: String,
    val suggestion: String,
    val candidateRow: ParsedCsvRow? = null
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
    val errorMessage: String? = null,
    val suggestion: String? = null
)

data class CsvImportPreview(
    val totalRows: Int,
    val validRows: Int,
    val duplicateRows: Int,
    val unsupportedRowsCount: Int,
    val columnMappings: List<ColumnMapping>,
    val rawHeaders: List<String>,
    val detectedHeaderMap: Map<String, Int>,
    val sampleRows: List<ParsedCsvRow>,
    val unsupportedRows: List<UnsupportedRow>,
    val newCategoryGroups: List<String>,
    val newCategories: List<Pair<String, String>>, // Group -> Category
    val newAccountGroups: List<String>,
    val newAccounts: List<Pair<String, String>> // Group -> Account
)

data class CsvImportResult(
    val importedCount: Int,
    val createdCategoriesCount: Int,
    val createdAccountsCount: Int,
    val skippedDuplicatesCount: Int,
    val unsupportedCount: Int = 0,
    val unsupportedDetails: List<UnsupportedRow> = emptyList()
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
        "yyyy.MM.dd",
        "d/M/yyyy",
        "M/d/yyyy",
        "d-M-yyyy",
        "dd-MMM-yyyy",
        "dd MMM yyyy",
        "dd MMMM yyyy",
        "MMM dd, yyyy",
        "MMMM dd, yyyy",
        "dd-MMM-yy",
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

    internal fun normalizeNumerals(str: String): String {
        return str.map { ch ->
            when (ch) {
                '০' -> '0'; '১' -> '1'; '২' -> '2'; '৩' -> '3'; '৪' -> '4'
                '৫' -> '5'; '৬' -> '6'; '৭' -> '7'; '৮' -> '8'; '৯' -> '9'
                else -> ch
            }
        }.joinToString("")
    }

    internal fun cleanCategoryGroupName(group: String): String {
        var cleaned = group.trim()
        val prefixes = listOf("➤", "►", "•", "→", ">", "★", "▪", "■", "✔", "▶")
        for (prefix in prefixes) {
            if (cleaned.startsWith(prefix)) {
                cleaned = cleaned.removePrefix(prefix).trim()
            }
        }
        return if (cleaned.isNotEmpty()) cleaned else group.trim()
    }

    /**
     * Parses the CSV file for preview before committing to database
     */
    suspend fun parseCsvForPreview(
        context: Context,
        uri: Uri,
        accountDao: AccountDao,
        categoryDao: CategoryDao,
        transactionDao: TransactionDao,
        customHeaderMap: Map<String, Int>? = null
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

            // Header mapping detection
            val (detectedMap, headerIndex, rawHeaders) = detectHeaderMapping(lines)
            val effectiveHeaderMap = customHeaderMap ?: detectedMap
            val dataLines = lines.drop(headerIndex + 1).filter { it.isNotBlank() }

            if (dataLines.isEmpty()) {
                return@withContext Result.failure(Exception("No transaction data rows found in CSV"))
            }

            // Build ColumnMapping list
            val columnMappings = rawHeaders.mapIndexed { idx, headerTitle ->
                val matchedKey = effectiveHeaderMap.entries.firstOrNull { it.value == idx }?.key
                ColumnMapping(
                    csvHeaderIndex = idx,
                    csvHeaderName = headerTitle,
                    targetAppColumnKey = matchedKey
                )
            }

            val parsedRows = mutableListOf<ParsedCsvRow>()
            val unsupportedRows = mutableListOf<UnsupportedRow>()
            val newCatGroups = mutableSetOf<String>()
            val newCats = mutableSetOf<Pair<String, String>>()
            val newAccGroups = mutableSetOf<String>()
            val newAccs = mutableSetOf<Pair<String, String>>()

            var duplicateCount = 0

            dataLines.forEachIndexed { index, line ->
                val lineNo = index + headerIndex + 2
                val tokens = parseCsvLine(line)
                if (tokens.isEmpty()) {
                    unsupportedRows.add(
                        UnsupportedRow(
                            lineNumber = lineNo,
                            rawTokens = emptyList(),
                            reason = "Empty row",
                            suggestion = "Remove empty lines or check line endings."
                        )
                    )
                    return@forEachIndexed
                }

                val row = parseRowFromTokens(tokens, effectiveHeaderMap, lineNo)
                if (!row.isValid) {
                    val rawTokens = tokens.take(8)
                    val reason = row.errorMessage ?: "Validation failed"
                    val suggestion = row.suggestion ?: "Verify that Date and Amount columns are mapped correctly."
                    val candidate = buildCandidateRow(tokens, effectiveHeaderMap, lineNo, row)
                    unsupportedRows.add(
                        UnsupportedRow(
                            lineNumber = lineNo,
                            rawTokens = rawTokens,
                            reason = reason,
                            suggestion = suggestion,
                            candidateRow = candidate
                        )
                    )
                    return@forEachIndexed
                }

                // Check duplicates (same date within +/- 2 minutes, same rounded amount, same type, matching note/name)
                val isDup = existingTransactions.any { existing ->
                    existing.type == row.type &&
                            Math.abs(existing.amount - row.amount) < 0.01 &&
                            Math.abs(existing.dateEpochMs - row.dateEpochMs) < (2 * 60 * 1000L)
                }

                val finalRow = row.copy(isDuplicate = isDup)
                if (isDup) duplicateCount++
                parsedRows.add(finalRow)

                val cleanedCatGroup = cleanCategoryGroupName(row.categoryGroup)
                val isNewAccountCat = row.category.equals("(New Account)", ignoreCase = true) ||
                        row.category.equals("New Account", ignoreCase = true) ||
                        cleanedCatGroup.equals("(New Account)", ignoreCase = true) ||
                        cleanedCatGroup.equals("New Account", ignoreCase = true)
                val isTransferCat = isNewAccountCat ||
                        row.category.equals("(Transfer)", ignoreCase = true) ||
                        row.category.equals("Transfer", ignoreCase = true) ||
                        cleanedCatGroup.equals("(Transfer)", ignoreCase = true) ||
                        cleanedCatGroup.equals("Transfer", ignoreCase = true)

                // Track new Category Groups & Categories (skip Transfer markers)
                if (!isTransferCat && row.type != TransactionType.TRANSFER) {
                    if (cleanedCatGroup.isNotBlank() && !existingCatNames.contains(cleanedCatGroup.lowercase())) {
                        newCatGroups.add(cleanedCatGroup)
                    }
                    if (row.category.isNotBlank() && !existingCatNames.contains(row.category.lowercase().trim())) {
                        newCats.add(Pair(cleanedCatGroup, row.category.trim()))
                    }
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
                unsupportedRowsCount = unsupportedRows.size,
                columnMappings = columnMappings,
                rawHeaders = rawHeaders,
                detectedHeaderMap = effectiveHeaderMap,
                sampleRows = parsedRows.sortedByDescending { it.dateEpochMs }.take(15),
                unsupportedRows = unsupportedRows.take(20),
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
        autoCreateEntities: Boolean = true,
        customHeaderMap: Map<String, Int>? = null,
        repairedRows: List<ParsedCsvRow> = emptyList(),
        autoRepairUnsupported: Boolean = false
    ): Result<CsvImportResult> = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext Result.failure(Exception("Cannot open CSV file"))

            val reader = BufferedReader(InputStreamReader(inputStream))
            val lines = reader.readLines()
            if (lines.isEmpty()) {
                return@withContext Result.failure(Exception("CSV file is empty"))
            }

            val (detectedMap, headerIndex, _) = detectHeaderMapping(lines)
            val headerMap = customHeaderMap ?: detectedMap
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
            val unsupportedList = mutableListOf<UnsupportedRow>()

            // Helper container for two-pass transfer pairing
            data class ResolvedImportItem(
                val row: ParsedCsvRow,
                val resolvedAccount: Account,
                val resolvedCategory: Category?,
                val isOutflow: Boolean,
                var isProcessed: Boolean = false
            )

            val resolvedItems = mutableListOf<ResolvedImportItem>()

            for ((index, line) in dataLines.withIndex()) {
                val lineNo = index + headerIndex + 2
                val tokens = parseCsvLine(line)
                if (tokens.isEmpty()) {
                    unsupportedList.add(
                        UnsupportedRow(
                            lineNumber = lineNo,
                            rawTokens = emptyList(),
                            reason = "Empty row",
                            suggestion = "Remove empty rows from CSV."
                        )
                    )
                    continue
                }

                val repairedMatch = repairedRows.find { it.rawLineNumber == lineNo }
                val row = if (repairedMatch != null && repairedMatch.isValid) {
                    repairedMatch
                } else {
                    val parsed = parseRowFromTokens(tokens, headerMap, lineNo)
                    if ((!parsed.isValid || Math.abs(parsed.amount) <= 0.0) && autoRepairUnsupported) {
                        buildCandidateRow(tokens, headerMap, lineNo, parsed)
                    } else {
                        parsed
                    }
                }

                if (!row.isValid || Math.abs(row.amount) <= 0.0) {
                    val rawTokens = tokens.take(8)
                    unsupportedList.add(
                        UnsupportedRow(
                            lineNumber = lineNo,
                            rawTokens = rawTokens,
                            reason = row.errorMessage ?: "Invalid row data",
                            suggestion = row.suggestion ?: "Check date/amount columns in Column Mapping.",
                            candidateRow = buildCandidateRow(tokens, headerMap, lineNo, row)
                        )
                    )
                    continue
                }

                // Check duplicate against DB
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
                val accClass = row.accountClass.lowercase()
                val accGrp = row.accountGroup.lowercase()
                val accNm = row.account.lowercase()
                val isLiability = accClass.contains("liabilit") || accClass.contains("loan") ||
                        accClass.contains("credit") || accClass.contains("debt") || accClass.contains("payable") ||
                        accGrp.contains("liabilit") || accGrp.contains("loan") || accGrp.contains("credit") ||
                        accGrp.contains("debt") || accGrp.contains("payable") ||
                        accNm.contains("loan") || accNm.contains("credit") || accNm.contains("debt") || accNm.contains("payable")
                val accType = if (isLiability) AccountType.LIABILITY else AccountType.ASSET

                val targetAccName = row.account.trim().ifEmpty { row.accountGroup.trim() }
                val targetAccGroupName = row.accountGroup.trim()

                if (targetAccName.isNotBlank() && autoCreateEntities) {
                    var parentAccId: Long? = null
                    if (targetAccGroupName.isNotBlank() && !targetAccGroupName.equals(targetAccName, ignoreCase = true)) {
                        val parent = accounts.find {
                            it.parentId == null &&
                                    (it.nameEn.equals(targetAccGroupName, ignoreCase = true) || it.nameBn.equals(targetAccGroupName, ignoreCase = true))
                        }
                        if (parent != null) {
                            parentAccId = parent.id
                        } else {
                            val newParent = Account(
                                nameEn = targetAccGroupName,
                                nameBn = targetAccGroupName,
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

                    val existingAcc = accounts.find {
                        (it.nameEn.equals(targetAccName, ignoreCase = true) || it.nameBn.equals(targetAccName, ignoreCase = true)) &&
                                (parentAccId == null || it.parentId == parentAccId || it.parentId == null)
                    }

                    if (existingAcc != null) {
                        resolvedAccount = existingAcc
                    } else {
                        val newAcc = Account(
                            nameEn = targetAccName,
                            nameBn = targetAccName,
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
                } else if (targetAccName.isNotBlank()) {
                    resolvedAccount = accounts.find {
                        it.nameEn.equals(targetAccName, ignoreCase = true) || it.nameBn.equals(targetAccName, ignoreCase = true)
                    } ?: defaultAccount
                }

                // 2. Resolve Category & Category Group
                val isTransfer = row.type == TransactionType.TRANSFER
                val cleanedCatGroup = cleanCategoryGroupName(row.categoryGroup)
                val isTransferCategory = row.category.equals("(Transfer)", ignoreCase = true) ||
                        row.category.equals("Transfer", ignoreCase = true) ||
                        cleanedCatGroup.equals("(Transfer)", ignoreCase = true) ||
                        cleanedCatGroup.equals("Transfer", ignoreCase = true)

                var resolvedCategory: Category? = if (row.type == TransactionType.INCOME) defaultIncomeCat else defaultExpenseCat
                if (!isTransfer && !isTransferCategory && row.category.isNotBlank() && autoCreateEntities) {
                    var parentCatId: Long? = null
                    val catType = if (row.type == TransactionType.INCOME) CategoryType.INCOME else CategoryType.EXPENSE

                    if (cleanedCatGroup.isNotBlank() && !cleanedCatGroup.equals(row.category.trim(), ignoreCase = true)) {
                        val parent = categories.find {
                            it.parentId == null && it.type == catType &&
                                    (it.nameEn.equals(cleanedCatGroup, ignoreCase = true) || it.nameBn.equals(cleanedCatGroup, ignoreCase = true))
                        }
                        if (parent != null) {
                            parentCatId = parent.id
                        } else {
                            val newParent = Category(
                                nameEn = cleanedCatGroup,
                                nameBn = cleanedCatGroup,
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
                            nameEn = row.category.trim(),
                            nameBn = row.category.trim(),
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

                val rawAmt = parseAmount(row.rawAmount)
                val isOutflow = rawAmt < 0 || row.rawAmount.contains("(") || row.type == TransactionType.EXPENSE

                resolvedItems.add(
                    ResolvedImportItem(
                        row = row,
                        resolvedAccount = resolvedAccount,
                        resolvedCategory = resolvedCategory,
                        isOutflow = isOutflow
                    )
                )
            }

            // 3. Assemble and merge transactions
            val transactionsToInsert = mutableListOf<Transaction>()

            fun formatNotes(r: ParsedCsvRow): String = buildString {
                if (r.notes.isNotBlank()) append(r.notes)
                if (r.labels.isNotBlank()) {
                    if (isNotEmpty()) append(" • ")
                    append("[Labels: ${r.labels}]")
                }
            }

            fun parseStatusEnum(statusStr: String): TransactionStatus = when {
                statusStr.lowercase().contains("reconciled") -> TransactionStatus.RECONCILED
                statusStr.lowercase().contains("cleared") -> TransactionStatus.CLEARED
                statusStr.lowercase().contains("void") -> TransactionStatus.VOID
                else -> TransactionStatus.NONE
            }

            for (i in 0 until resolvedItems.size) {
                val itemA = resolvedItems[i]
                if (itemA.isProcessed) continue

                if (itemA.row.type == TransactionType.TRANSFER) {
                    // Try to find a matching paired transfer in the batch
                    var pairedIndex = -1
                    for (j in (i + 1) until resolvedItems.size) {
                        val itemB = resolvedItems[j]
                        if (!itemB.isProcessed && itemB.row.type == TransactionType.TRANSFER) {
                            val sameAmount = Math.abs(itemA.row.amount - itemB.row.amount) < 0.01
                            val closeTime = Math.abs(itemA.row.dateEpochMs - itemB.row.dateEpochMs) <= 60000L
                            val oppositeFlow = itemA.isOutflow != itemB.isOutflow
                            val differentAccs = itemA.resolvedAccount.id != itemB.resolvedAccount.id

                            if (sameAmount && closeTime && oppositeFlow && differentAccs) {
                                pairedIndex = j
                                break
                            }
                        }
                    }

                    if (pairedIndex != -1) {
                        val itemB = resolvedItems[pairedIndex]
                        val outflowItem = if (itemA.isOutflow) itemA else itemB
                        val inflowItem = if (itemA.isOutflow) itemB else itemA

                        val combinedName = when {
                            outflowItem.row.name.isNotBlank() && !outflowItem.row.name.equals("Transfer", ignoreCase = true) -> outflowItem.row.name
                            inflowItem.row.name.isNotBlank() && !inflowItem.row.name.equals("Transfer", ignoreCase = true) -> inflowItem.row.name
                            else -> "Transfer"
                        }

                        val notesA = formatNotes(outflowItem.row)
                        val notesB = formatNotes(inflowItem.row)
                        val combinedNotes = when {
                            notesA.isNotBlank() && notesB.isNotBlank() && notesA != notesB -> "$notesA • $notesB"
                            notesA.isNotBlank() -> notesA
                            notesB.isNotBlank() -> notesB
                            else -> "Imported Transfer"
                        }

                        val stStr = if (outflowItem.row.status.isNotBlank()) outflowItem.row.status else inflowItem.row.status

                        val tx = Transaction(
                            type = TransactionType.TRANSFER,
                            amount = outflowItem.row.amount,
                            dateEpochMs = outflowItem.row.dateEpochMs,
                            debitAccountId = inflowItem.resolvedAccount.id, // Destination account
                            creditAccountId = outflowItem.resolvedAccount.id, // Source account
                            categoryId = null,
                            payeeOrPayer = combinedName,
                            note = combinedNotes,
                            status = parseStatusEnum(stStr)
                        )
                        transactionsToInsert.add(tx)
                        existingTransactions.add(tx)
                        importedCount++

                        itemA.isProcessed = true
                        itemB.isProcessed = true
                    } else {
                        // Unpaired standalone transfer row (including Starting Balance / Opening Balance)
                        var sourceAcc: Account? = null
                        var destAcc: Account? = null

                        val isStartingBalance = itemA.row.category.contains("New Account", ignoreCase = true) ||
                                itemA.row.categoryGroup.contains("New Account", ignoreCase = true) ||
                                itemA.row.name.contains("Starting Balance", ignoreCase = true) ||
                                itemA.row.name.contains("Opening Balance", ignoreCase = true) ||
                                itemA.row.notes.contains("Starting Balance", ignoreCase = true) ||
                                itemA.row.notes.contains("Opening Balance", ignoreCase = true)

                        if (isStartingBalance) {
                            var equityAcc = accounts.find { it.type == AccountType.EQUITY || it.nameEn.contains("Opening Balance", ignoreCase = true) }
                            if (equityAcc == null) {
                                val newEquity = Account(
                                    nameEn = "Owner's Equity / Opening Balance",
                                    nameBn = "মালিকানা স্বত্ব / প্রারম্ভিক উদ্বৃত্ত",
                                    type = AccountType.EQUITY,
                                    iconName = "AccountBalanceWallet",
                                    colorHex = "#8B5CF6",
                                    isSystem = true
                                )
                                val eqId = accountDao.insertAccount(newEquity)
                                equityAcc = newEquity.copy(id = eqId)
                                accounts.add(equityAcc)
                            }

                            if (itemA.resolvedAccount.type == AccountType.LIABILITY) {
                                destAcc = equityAcc
                                sourceAcc = itemA.resolvedAccount
                            } else {
                                destAcc = itemA.resolvedAccount
                                sourceAcc = equityAcc
                            }
                        } else if (itemA.isOutflow) {
                            sourceAcc = itemA.resolvedAccount
                            if (itemA.row.name.isNotBlank()) {
                                destAcc = accounts.find {
                                    it.nameEn.equals(itemA.row.name, ignoreCase = true) || it.nameBn.equals(itemA.row.name, ignoreCase = true)
                                }
                            }
                        } else {
                            destAcc = itemA.resolvedAccount
                            if (itemA.row.name.isNotBlank()) {
                                sourceAcc = accounts.find {
                                    it.nameEn.equals(itemA.row.name, ignoreCase = true) || it.nameBn.equals(itemA.row.name, ignoreCase = true)
                                }
                            }
                        }

                        val tx = Transaction(
                            type = TransactionType.TRANSFER,
                            amount = itemA.row.amount,
                            dateEpochMs = itemA.row.dateEpochMs,
                            debitAccountId = destAcc?.id,
                            creditAccountId = sourceAcc?.id,
                            categoryId = null,
                            payeeOrPayer = itemA.row.name.ifEmpty { if (isStartingBalance) "Starting Balance" else "Transfer" },
                            note = formatNotes(itemA.row).ifEmpty { if (isStartingBalance) "Starting Balance" else "Imported Transfer" },
                            status = if (isStartingBalance && itemA.row.status.isBlank()) TransactionStatus.RECONCILED else parseStatusEnum(itemA.row.status)
                        )
                        transactionsToInsert.add(tx)
                        existingTransactions.add(tx)
                        importedCount++
                        itemA.isProcessed = true
                    }
                } else {
                    // Non-transfer: EXPENSE or INCOME
                    val tx = Transaction(
                        type = itemA.row.type,
                        amount = itemA.row.amount,
                        dateEpochMs = itemA.row.dateEpochMs,
                        debitAccountId = if (itemA.row.type == TransactionType.INCOME) itemA.resolvedAccount.id else null,
                        creditAccountId = if (itemA.row.type == TransactionType.EXPENSE) itemA.resolvedAccount.id else null,
                        categoryId = itemA.resolvedCategory?.id,
                        payeeOrPayer = itemA.row.name.ifEmpty { if (itemA.row.type == TransactionType.INCOME) "Income" else "Expense" },
                        note = formatNotes(itemA.row).ifEmpty { "Imported from CSV" },
                        status = parseStatusEnum(itemA.row.status)
                    )
                    transactionsToInsert.add(tx)
                    existingTransactions.add(tx)
                    importedCount++
                    itemA.isProcessed = true
                }
            }

            if (transactionsToInsert.isNotEmpty()) {
                transactionDao.insertTransactions(transactionsToInsert)
            }

            Result.success(
                CsvImportResult(
                    importedCount = importedCount,
                    createdCategoriesCount = createdCatCount,
                    createdAccountsCount = createdAccCount,
                    skippedDuplicatesCount = skippedDupCount,
                    unsupportedCount = unsupportedList.size,
                    unsupportedDetails = unsupportedList.take(20)
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
            val fileName = "Budgeter_Export_$dateStr.csv"

            // Save to selected local sync folder in "Exports" subfolder
            BackupManager.saveExportToLocalFolder(
                context = context,
                fileName = fileName,
                mimeType = "text/csv",
                content = csvContent
            )

            val file = File(context.cacheDir, fileName)
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

    internal fun detectHeaderMapping(lines: List<String>): Triple<Map<String, Int>, Int, List<String>> {
        val headerMap = mutableMapOf<String, Int>()
        var headerIndex = 0
        var rawHeaders = emptyList<String>()

        for (i in 0 until minOf(5, lines.size)) {
            val rawTokens = parseCsvLine(lines[i])
            val tokens = rawTokens.map { it.lowercase().trim() }
            if (tokens.any { it.contains("type") || it.contains("date") || it.contains("amount") || it.contains("category") || it.contains("account") }) {
                headerIndex = i
                rawHeaders = rawTokens
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
        if (rawHeaders.isEmpty() && lines.isNotEmpty()) {
            rawHeaders = parseCsvLine(lines[0])
        }
        return Triple(headerMap, headerIndex, rawHeaders)
    }

    internal fun parseRowFromTokens(
        tokens: List<String>,
        headerMap: Map<String, Int>,
        lineNo: Int
    ): ParsedCsvRow {
        fun get(key: String, defaultIdx: Int? = null): String {
            val idx = headerMap[key] ?: defaultIdx
            return if (idx != null && idx >= 0 && idx < tokens.size) tokens[idx].trim() else ""
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
        val isStartingBalance = typeStr.lowercase().contains("starting") ||
                typeStr.lowercase().contains("opening") ||
                typeStr.lowercase().contains("initial balance") ||
                catStr.lowercase().contains("new account") ||
                catGroupStr.lowercase().contains("new account") ||
                notesStr.lowercase().contains("starting balance") ||
                notesStr.lowercase().contains("opening balance")

        val isExplicitExpense = typeStr.lowercase().contains("expense")
        val isExplicitIncome = typeStr.lowercase().contains("income")
        val isExplicitTransfer = typeStr.lowercase().contains("transfer")

        val hasNegativeNotation = rawAmount < 0 || amountStr.contains("(") || amountStr.trim().startsWith("-")
        val hasPositiveNotation = rawAmount > 0 && !hasNegativeNotation

        val isRevertedExpense = isExplicitExpense && hasPositiveNotation
        val isRevertedIncome = isExplicitIncome && hasNegativeNotation
        val isReverted = isRevertedExpense || isRevertedIncome

        val isNegative = !isStartingBalance && (hasNegativeNotation || isExplicitExpense)
        val parsedFxRate = parseAmount(fxRateStr).let { if (it > 0) it else 1.0 }

        val absConvertedAmount = Math.abs(rawAmount) * parsedFxRate
        val finalAmount = if (isReverted) -absConvertedAmount else absConvertedAmount

        val txType = when {
            isStartingBalance -> TransactionType.TRANSFER
            isExplicitTransfer -> TransactionType.TRANSFER
            isExplicitIncome -> TransactionType.INCOME
            isExplicitExpense -> TransactionType.EXPENSE
            isNegative -> TransactionType.EXPENSE
            else -> TransactionType.EXPENSE
        }

        val dateEpoch = parseDateTimeEpoch(dateStr, timeStr)
        val dateFormatted = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(dateEpoch))
        val timeFormatted = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date(dateEpoch))

        var errorReason: String? = null
        var suggestion: String? = null

        if (absConvertedAmount <= 0.0 && amountStr.isBlank()) {
            errorReason = "Amount column is empty"
            suggestion = "Map the 'Amount' field to the correct CSV column in Column Mapping."
        } else if (absConvertedAmount <= 0.0) {
            errorReason = "Invalid amount value: '$amountStr'"
            suggestion = "Verify amount contains numbers (e.g. 500, 120.50). Currency symbols and commas are auto-stripped."
        } else if (dateEpoch <= 0L && dateStr.isBlank()) {
            errorReason = "Date column is empty"
            suggestion = "Map the 'Date' field to the date column in your CSV file."
        } else if (dateEpoch <= 0L) {
            errorReason = "Unrecognized date format: '$dateStr'"
            suggestion = "Use supported formats like YYYY-MM-DD, DD/MM/YYYY, MM/DD/YYYY, or DD-MMM-YYYY."
        }

        val isValid = dateEpoch > 0 && absConvertedAmount > 0.0

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
            errorMessage = errorReason,
            suggestion = suggestion
        )
    }

    internal fun scanTokensForAnyAmount(tokens: List<String>): Double {
        for (token in tokens) {
            val amt = parseAmount(token)
            if (amt > 0.0) return amt
        }
        return 0.0
    }

    internal fun buildCandidateRow(
        tokens: List<String>,
        headerMap: Map<String, Int>,
        lineNo: Int,
        baseRow: ParsedCsvRow
    ): ParsedCsvRow {
        val fallbackDate = if (baseRow.dateEpochMs > 0L) baseRow.dateEpochMs else System.currentTimeMillis()
        val scannedAmt = if (Math.abs(baseRow.amount) > 0.0) baseRow.amount else scanTokensForAnyAmount(tokens).let { if (it > 0.0) it else 100.0 }
        val dateFormatted = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(fallbackDate))
        val timeFormatted = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date(fallbackDate))

        val guessedName = if (baseRow.name.isNotBlank() && baseRow.name != "Transaction") {
            baseRow.name
        } else {
            tokens.firstOrNull { it.isNotBlank() && parseAmount(it) == 0.0 && it.length in 2..50 } ?: "Transaction (Repaired)"
        }

        return baseRow.copy(
            dateEpochMs = fallbackDate,
            dateFormatted = dateFormatted,
            timeFormatted = timeFormatted,
            amount = scannedAmt,
            rawAmount = String.format(Locale.US, "%.2f", scannedAmt),
            name = guessedName,
            category = baseRow.category.ifBlank { "General Expense" },
            account = baseRow.account.ifBlank { "Cash" },
            isValid = true,
            errorMessage = null,
            suggestion = "Auto-repaired using fallback values."
        )
    }

    private fun detectDelimiter(line: String): Char {
        var commas = 0
        var semicolons = 0
        var tabs = 0
        var inQuotes = false

        for (ch in line) {
            if (ch == '\"') inQuotes = !inQuotes
            else if (!inQuotes) {
                when (ch) {
                    ',' -> commas++
                    ';' -> semicolons++
                    '\t' -> tabs++
                }
            }
        }

        return when {
            semicolons > commas && semicolons > tabs -> ';'
            tabs > commas && tabs > semicolons -> '\t'
            else -> ','
        }
    }

    internal fun parseCsvLine(line: String): List<String> {
        val tokens = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false

        val cleanLine = if (line.startsWith("\uFEFF")) line.substring(1) else line
        if (cleanLine.isBlank()) return emptyList()

        val delimiter = detectDelimiter(cleanLine)

        for (ch in cleanLine) {
            when {
                ch == '\"' -> inQuotes = !inQuotes
                ch == delimiter && !inQuotes -> {
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
        val normalized = normalizeNumerals(str)
        val cleaned = normalized.replace("$", "").replace("৳", "").replace("€", "").replace("£", "").replace("₹", "")
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

        val normalizedDate = normalizeNumerals(dateStr.trim())
        var parsedDate: Date? = null
        for (pattern in DATE_FORMATS) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale.US)
                sdf.isLenient = true
                val d = sdf.parse(normalizedDate)
                if (d != null) {
                    parsedDate = d
                    break
                }
            } catch (_: Exception) {}
        }

        if (parsedDate == null) return -1L

        val cal = Calendar.getInstance()
        cal.time = parsedDate

        if (timeStr.isNotBlank()) {
            val normalizedTime = normalizeNumerals(timeStr.trim())
            for (timePattern in TIME_FORMATS) {
                try {
                    val sdf = SimpleDateFormat(timePattern, Locale.US)
                    sdf.isLenient = true
                    val t = sdf.parse(normalizedTime)
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
