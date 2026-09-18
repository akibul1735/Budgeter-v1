package com.example.util

import com.example.data.model.Account
import com.example.data.model.LanguageMode
import com.example.data.model.Transaction
import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionType
import com.example.data.model.TransactionWithDetails
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

/**
 * Helper and data models for RM (Resting Money) Manager.
 *
 * Core Concept:
 * - "RM" is matched as a standalone word/token in an Account name (e.g., "Parash RM", "Rupa Khala RM").
 * - Does NOT match RM inside words (e.g. "Farm", "Information", "Normal").
 * - RM accounts represent Resting Money (temporary liabilities) where funds are received/held and need repayment.
 * - Negative balance convention: Liabilities are represented as negative balances (e.g., -৳ 5,000) when debt increases.
 * - RM Others are grouped by Label/Person to track liability and repayment per label.
 * - Side-by-side Khatian (খতিয়ান - জের) format: Date, Particulars/Type, Debit (Dr), Credit (Cr), Balance (জের).
 */
object RmManagerHelper {

    // Regex to match "RM" as a standalone word/token (case-insensitive, whole word boundary)
    private val RM_TOKEN_REGEX = Regex("""(?i)\bRM\b""")
    private val RM_BN_TOKEN_REGEX = Regex("""(?i)(?:^|[\s_\-/#])(?:আরএম|RM)(?:[\s_\-/#]|$)""")

    /**
     * Checks if a name matches a filter keyword (strictly whole word boundary).
     */
    fun isNameMatching(name: String?, keyword: String): Boolean {
        if (name.isNullOrBlank() || keyword.isBlank()) return false
        val trimmed = keyword.trim()
        val regex = Regex("""(?i)\b${Regex.escape(trimmed)}\b""")
        return regex.containsMatchIn(name) || (name.equals(trimmed, ignoreCase = true))
    }

    /**
     * Checks if a name contains the standalone word/token "RM" or Bengali "আরএম".
     */
    fun isRmName(name: String?): Boolean {
        if (name.isNullOrBlank()) return false
        return RM_TOKEN_REGEX.containsMatchIn(name) || RM_BN_TOKEN_REGEX.containsMatchIn(name) || isNameMatching(name, "RM") || isNameMatching(name, "আরএম")
    }

    /**
     * Checks if an account is an included RM (Resting Money) account.
     */
    fun isRmAccount(
        account: Account,
        includeKeyword: String = "RM",
        excludeKeyword: String = "RM Others"
    ): Boolean {
        val matchesInclude = isNameMatching(account.nameEn, includeKeyword) ||
                isNameMatching(account.nameBn, includeKeyword) ||
                (includeKeyword.equals("RM", ignoreCase = true) && (isRmName(account.nameEn) || isRmName(account.nameBn)))
        if (!matchesInclude) return false
        if (excludeKeyword.isNotBlank()) {
            val matchesExclude = isNameMatching(account.nameEn, excludeKeyword) ||
                    isNameMatching(account.nameBn, excludeKeyword) ||
                    (excludeKeyword.equals("RM Others", ignoreCase = true) && (isNameMatching(account.nameEn, "RM Others") || isNameMatching(account.nameBn, "আরএম অন্যান্য")))
            if (matchesExclude) return false
        }
        return true
    }

    /**
     * Checks if an account matches the exclusion filter (e.g. "RM Others").
     */
    fun isExcludedAccount(
        account: Account,
        excludeKeyword: String = "RM Others"
    ): Boolean {
        if (excludeKeyword.isBlank()) return false
        val nameEn = account.nameEn.trim()
        val nameBn = account.nameBn.trim()
        return isNameMatching(nameEn, excludeKeyword) ||
                isNameMatching(nameBn, excludeKeyword) ||
                (excludeKeyword.equals("RM Others", ignoreCase = true) && (
                    nameEn.contains("RM Others", ignoreCase = true) ||
                    (nameEn.contains("Others", ignoreCase = true) && isRmName(nameEn)) ||
                    nameBn.contains("আরএম অন্যান্য", ignoreCase = true) ||
                    (nameBn.contains("অন্যান্য", ignoreCase = true) && isRmName(nameBn))
                ))
    }

    /**
     * Extract tags/labels from transaction referenceNo (label tag), note, or payee.
     * When fallbackToPayeeOrNote is false (default), explicit labels in referenceNo or hashtags (#tag) are extracted.
     * When fallbackToPayeeOrNote is true, payee or note is used as fallback.
     */
    fun extractLabelsFromTransaction(
        item: TransactionWithDetails,
        fallbackToPayeeOrNote: Boolean = false
    ): List<String> {
        val tx = item.transaction
        val note = tx.note.trim()
        val ref = tx.referenceNo.trim()
        val payee = tx.payeeOrPayer.trim()

        val labels = mutableSetOf<String>()

        // 1. Extract from referenceNo (which is the direct Label/Tag field in this app)
        if (ref.isNotBlank()) {
            val parts = ref.split(Regex("[,;]")).map { it.trim().removePrefix("#").trim() }.filter { it.isNotBlank() }
            for (p in parts) {
                val hashtagMatches = Regex("#[\\w\\u0980-\\u09FF]+").findAll(p).map { it.value.removePrefix("#").trim() }.toList()
                if (hashtagMatches.isNotEmpty()) {
                    labels.addAll(hashtagMatches)
                } else {
                    labels.add(p)
                }
            }
        }

        // 2. Extract hashtags (#tag) from note and payee
        val hashtagRegex = Regex("#[\\w\\u0980-\\u09FF]+")
        val matches = hashtagRegex.findAll("$note $payee")
        for (m in matches) {
            val clean = m.value.removePrefix("#").trim()
            if (clean.isNotBlank()) {
                labels.add(clean)
            }
        }

        // 3. Only fallback to payee or note when explicitly requested
        if (fallbackToPayeeOrNote && labels.isEmpty()) {
            if (payee.isNotBlank()) {
                val cleanPayee = payee.replace(Regex("""(?i)\b(Debit|Credit|Dr|Cr)\b"""), "").trim()
                if (cleanPayee.isNotBlank()) {
                    labels.add(cleanPayee)
                }
            }

            if (labels.isEmpty() && note.isNotBlank()) {
                val cleanNote = note.lines().firstOrNull()?.trim() ?: ""
                val strippedNote = cleanNote.replace(Regex("""(?i)\b(Debit|Credit|Dr|Cr)\b"""), "").trim()
                if (strippedNote.length in 1..30 && !strippedNote.startsWith("http")) {
                    labels.add(strippedNote.removePrefix("#"))
                }
            }
        }

        return labels.toList()
    }

    enum class RmRepaymentStatus {
        UNPAID,           // Full liability remaining
        PARTIALLY_REPAID, // Some repaid, some still remaining
        SETTLED           // Fully repaid (0 remaining)
    }

    enum class RmSortOption {
        HIGHEST_DUE,
        LOWEST_DUE,
        MOST_REPAID,
        NAME_AZ,
        RECENT_ACTIVITY
    }

    enum class RmFilterCategory {
        ALL,
        PENDING_ONLY,
        RM_ACCOUNTS,
        RM_OTHERS,
        SETTLED_ONLY
    }

    /**
     * Single transaction entry in RM breakdown.
     */
    data class RmTransactionItem(
        val transactionWithDetails: TransactionWithDetails,
        val isDebit: Boolean,          // True: Borrowed / Inflow / Liability Created. False: Repaid / Outflow / Liability Cleared
        val amount: Double,
        val displayAmount: Double,
        val displayName: String,
        val dateFormatted: String,
        val dateEpochMs: Long,
        val subtitle: String,
        val note: String,
        val categoryName: String,
        val isReconciled: Boolean,
        val runningBalanceJer: Double = 0.0
    )

    /**
     * Row entry for side-by-side Khatian (খতিয়ান - জের) Ledger table.
     */
    data class RmKhatianRow(
        val id: String,
        val dateFormatted: String,
        val miniDateFormatted: String = "",
        val dateEpochMs: Long,
        val particulars: String,
        val categoryOrType: String,
        val isDebit: Boolean,
        val debitAmount: Double?,   // Non-null for Dr (দেনা গ্রহণ / বৃদ্ধি)
        val creditAmount: Double?,  // Non-null for Cr (পরিশোধ / হ্রাস)
        val balanceJer: Double,     // Running balance (জের) after this entry (negative if liability remains)
        val isReconciled: Boolean,
        val transactionItem: RmTransactionItem? = null
    )

    /**
     * Breakdown group for an RM entity (Account or Label).
     */
    data class RmEntityBreakdown(
        val id: String,
        val name: String,
        val isAccount: Boolean,
        val account: Account? = null,
        val totalBorrowed: Double,     // Total Debit / Taken In (Liabilities created)
        val totalRepaid: Double,       // Total Credit / Repaid (Liabilities settled)
        val remainingLiability: Double,// Outstanding to repay = max(0, totalBorrowed - totalRepaid)
        val netBalance: Double,        // Negative balance (-৳ amount) if liability outstanding, 0 if settled, positive if overpaid
        val repaymentProgressPercent: Float, // 0f .. 1f
        val status: RmRepaymentStatus,
        val debitTransactions: List<RmTransactionItem>,
        val creditTransactions: List<RmTransactionItem>,
        val allTransactions: List<RmTransactionItem>,
        val khatianLedgerRows: List<RmKhatianRow>,
        val lastActivityEpochMs: Long
    )

    /**
     * Full aggregated data for RM Manager screen.
     */
    data class RmManagerScreenData(
        val allEntities: List<RmEntityBreakdown>,
        val rmAccounts: List<RmEntityBreakdown>,
        val rmOthers: List<RmEntityBreakdown>,
        val totalOutstandingLiability: Double,
        val totalNetLiabilityBalance: Double, // Negative total (-৳) representing collective liability
        val totalGrossBorrowed: Double,
        val totalGrossRepaid: Double,
        val overallRepaymentProgress: Float,
        val pendingCount: Int,
        val partiallyRepaidCount: Int,
        val settledCount: Int,
        val globalKhatianRows: List<RmKhatianRow>
    )

    /**
     * Formats date: "Aug 04, 2026 : Tue" or "Aug 04, 2026"
     */
    fun formatRmDate(epochMs: Long, languageMode: LanguageMode = LanguageMode.ENGLISH, includeDayOfWeek: Boolean = true): String {
        val pattern = if (includeDayOfWeek) "MMM dd, yyyy : EEE" else "MMM dd, yyyy"
        val sdf = SimpleDateFormat(pattern, Locale.US)
        return sdf.format(Date(epochMs))
    }

    /**
     * Formats compact mini date for ledger tables (e.g., "04/08/26")
     */
    fun formatMiniDate(epochMs: Long): String {
        if (epochMs <= 0L) return "Init"
        val sdf = SimpleDateFormat("dd/MM/yy", Locale.US)
        return sdf.format(Date(epochMs))
    }

    /**
     * Builds the complete RM Manager Screen Data.
     */
    fun computeRmManagerData(
        allAccounts: List<Account>,
        allTransactions: List<TransactionWithDetails>,
        searchQuery: String = "",
        filterCategory: RmFilterCategory = RmFilterCategory.ALL,
        sortOption: RmSortOption = RmSortOption.HIGHEST_DUE,
        languageMode: LanguageMode = LanguageMode.ENGLISH,
        includeKeyword: String = "RM",
        excludeKeyword: String = "RM Others"
    ): RmManagerScreenData {
        val rmAccountsList = allAccounts.filter { isRmAccount(it, includeKeyword, excludeKeyword) }
        val excludedAccountsList = allAccounts.filter { isExcludedAccount(it, excludeKeyword) }
        val excludedAccountIds = excludedAccountsList.map { it.id }.toSet()
        val rmAccountIds = rmAccountsList.map { it.id }.toSet()

        // 1. Process Included RM Accounts (e.g., "Rm Parash")
        val rmAccountBreakdowns = mutableListOf<RmEntityBreakdown>()

        for (acc in rmAccountsList) {
            val accTxs = allTransactions.filter { item ->
                val tx = item.transaction
                tx.status != TransactionStatus.VOID &&
                        (tx.debitAccountId == acc.id || tx.creditAccountId == acc.id)
            }

            val debitItems = mutableListOf<RmTransactionItem>()
            val creditItems = mutableListOf<RmTransactionItem>()
            var latestTimestamp = 0L

            for (item in accTxs) {
                val tx = item.transaction
                if (tx.dateEpochMs > latestTimestamp) latestTimestamp = tx.dateEpochMs
                val dateStr = formatRmDate(tx.dateEpochMs, languageMode)
                val accName = acc.localizedName(languageMode)
                val catName = item.category?.localizedName(languageMode) ?: item.transaction.type.name
                val isReconciled = tx.status == TransactionStatus.RECONCILED

                if (tx.creditAccountId == acc.id) {
                    // Credit on RM Account: Funds taken from RM account (Transfer to Cash or Expense paid) -> Liability created (Debit / Dr in RM Manager)
                    val nameStr = buildTransactionDisplayName(item, isDebit = true, defaultName = accName)
                    val debitItem = RmTransactionItem(
                        transactionWithDetails = item,
                        isDebit = true,
                        amount = tx.amount,
                        displayAmount = -tx.amount, // negative representation for liability increase
                        displayName = nameStr,
                        dateFormatted = dateStr,
                        dateEpochMs = tx.dateEpochMs,
                        subtitle = "($catName)",
                        note = tx.note,
                        categoryName = catName,
                        isReconciled = isReconciled
                    )
                    debitItems.add(debitItem)
                }

                if (tx.debitAccountId == acc.id) {
                    // Debit on RM Account: Funds returned to RM account (Repayment from Cash or Income deposited) -> Liability settled (Credit / Cr in RM Manager)
                    val nameStr = buildTransactionDisplayName(item, isDebit = false, defaultName = accName)
                    val creditItem = RmTransactionItem(
                        transactionWithDetails = item,
                        isDebit = false,
                        amount = tx.amount,
                        displayAmount = tx.amount,
                        displayName = nameStr,
                        dateFormatted = dateStr,
                        dateEpochMs = tx.dateEpochMs,
                        subtitle = "($catName)",
                        note = tx.note,
                        categoryName = catName,
                        isReconciled = isReconciled
                    )
                    creditItems.add(creditItem)
                }
            }

            val totalDr = debitItems.sumOf { it.amount } + (if (acc.initialBalance > 0 && debitItems.isEmpty() && creditItems.isEmpty()) acc.initialBalance else 0.0)
            val totalCr = creditItems.sumOf { it.amount }
            val remainingDue = if (totalDr >= totalCr) totalDr - totalCr else 0.0
            val netBal = totalCr - totalDr // Negative if remainingDue > 0 (e.g. -5000.0)

            val progress = when {
                totalDr <= 0.0 -> if (totalCr > 0) 1.0f else 0.0f
                totalCr >= totalDr -> 1.0f
                else -> (totalCr / totalDr).toFloat().coerceIn(0f, 1f)
            }

            val status = when {
                remainingDue <= 0.001 && (totalDr > 0 || totalCr > 0) -> RmRepaymentStatus.SETTLED
                totalCr > 0.0 && remainingDue > 0.0 -> RmRepaymentStatus.PARTIALLY_REPAID
                else -> RmRepaymentStatus.UNPAID
            }

            // Build Chronological Khatian / Ledger Rows
            val khatianRows = buildKhatianLedgerRows(
                initialBalance = acc.initialBalance,
                debitItems = debitItems,
                creditItems = creditItems,
                languageMode = languageMode
            )

            rmAccountBreakdowns.add(
                RmEntityBreakdown(
                    id = "acc_${acc.id}",
                    name = acc.localizedName(languageMode),
                    isAccount = true,
                    account = acc,
                    totalBorrowed = totalDr,
                    totalRepaid = totalCr,
                    remainingLiability = remainingDue,
                    netBalance = netBal,
                    repaymentProgressPercent = progress,
                    status = status,
                    debitTransactions = debitItems.sortedByDescending { it.dateEpochMs },
                    creditTransactions = creditItems.sortedByDescending { it.dateEpochMs },
                    allTransactions = (debitItems + creditItems).sortedByDescending { it.dateEpochMs },
                    khatianLedgerRows = khatianRows,
                    lastActivityEpochMs = latestTimestamp
                )
            )
        }

        // 2. Process Excluded Accounts (e.g. "RM Others") & Labels (e.g. "Sakib")
        // Transactions from excluded accounts are split by label so each person/label has their own card.
        val labelTransactionsMap = mutableMapOf<String, MutableList<TransactionWithDetails>>()

        for (item in allTransactions) {
            val tx = item.transaction
            if (tx.status == TransactionStatus.VOID) continue

            val isFromExcludedAcc = (tx.debitAccountId != null && tx.debitAccountId in excludedAccountIds) ||
                    (tx.creditAccountId != null && tx.creditAccountId in excludedAccountIds)
            val isFromRmAcc = (tx.debitAccountId != null && tx.debitAccountId in rmAccountIds) ||
                    (tx.creditAccountId != null && tx.creditAccountId in rmAccountIds)

            if (isFromExcludedAcc) {
                // If it belongs to an excluded account (e.g. RM Others), only include if it has explicit labels (#tag).
                // Those Rm Others that don't have labels shouldn't be included.
                val labels = extractLabelsFromTransaction(item, fallbackToPayeeOrNote = false)
                for (lbl in labels) {
                    labelTransactionsMap.getOrPut(lbl) { mutableListOf() }.add(item)
                }
            } else if (!isFromRmAcc) {
                // Standalone transactions outside RM accounts are ONLY included if explicitly tagged with #RM or #RestingMoney
                val explicitHashtags = extractLabelsFromTransaction(item, fallbackToPayeeOrNote = false)
                val rmExplicitTags = explicitHashtags.filter { tag ->
                    isNameMatching(tag, includeKeyword) || tag.equals("RestingMoney", ignoreCase = true) || tag.startsWith("RM_", ignoreCase = true)
                }
                for (lbl in rmExplicitTags) {
                    labelTransactionsMap.getOrPut(lbl) { mutableListOf() }.add(item)
                }
            }
        }

        val rmOthersBreakdowns = mutableListOf<RmEntityBreakdown>()

        for ((labelName, txList) in labelTransactionsMap) {
            val debitItems = mutableListOf<RmTransactionItem>()
            val creditItems = mutableListOf<RmTransactionItem>()
            var latestTimestamp = 0L

            for (item in txList) {
                val tx = item.transaction
                if (tx.dateEpochMs > latestTimestamp) latestTimestamp = tx.dateEpochMs
                val dateStr = formatRmDate(tx.dateEpochMs, languageMode)
                val catName = item.category?.localizedName(languageMode) ?: tx.type.name
                val isReconciled = tx.status == TransactionStatus.RECONCILED

                // Check if Debit or Credit for this label:
                // If from excluded account (e.g. RM Others):
                // - creditAccountId in excludedAccounts: Money transferred FROM RM Others to Cash / Expense paid -> Debit (Liability created / Borrowed)
                // - debitAccountId in excludedAccounts: Money transferred TO RM Others from Cash / Income deposited -> Credit (Liability settled / Repaid)
                val isDebit = when {
                    tx.creditAccountId != null && tx.creditAccountId in excludedAccountIds -> true
                    tx.debitAccountId != null && tx.debitAccountId in excludedAccountIds -> false
                    else -> isTransactionDebitForLabel(item, labelName)
                }

                if (isDebit) {
                    val displayName = buildTransactionDisplayName(item, isDebit = true, defaultName = labelName)
                    val debitItem = RmTransactionItem(
                        transactionWithDetails = item,
                        isDebit = true,
                        amount = tx.amount,
                        displayAmount = -tx.amount, // negative representation for liability increase
                        displayName = displayName,
                        dateFormatted = dateStr,
                        dateEpochMs = tx.dateEpochMs,
                        subtitle = "($catName)",
                        note = tx.note,
                        categoryName = catName,
                        isReconciled = isReconciled
                    )
                    debitItems.add(debitItem)
                } else {
                    val displayName = buildTransactionDisplayName(item, isDebit = false, defaultName = labelName)
                    val creditItem = RmTransactionItem(
                        transactionWithDetails = item,
                        isDebit = false,
                        amount = tx.amount,
                        displayAmount = tx.amount,
                        displayName = displayName,
                        dateFormatted = dateStr,
                        dateEpochMs = tx.dateEpochMs,
                        subtitle = "($catName)",
                        note = tx.note,
                        categoryName = catName,
                        isReconciled = isReconciled
                    )
                    creditItems.add(creditItem)
                }
            }

            val totalDr = debitItems.sumOf { it.amount }
            val totalCr = creditItems.sumOf { it.amount }
            val remainingDue = if (totalDr >= totalCr) totalDr - totalCr else 0.0
            val netBal = totalCr - totalDr

            val progress = when {
                totalDr <= 0.0 -> if (totalCr > 0) 1.0f else 0.0f
                totalCr >= totalDr -> 1.0f
                else -> (totalCr / totalDr).toFloat().coerceIn(0f, 1f)
            }

            val status = when {
                remainingDue <= 0.001 && (totalDr > 0 || totalCr > 0) -> RmRepaymentStatus.SETTLED
                totalCr > 0.0 && remainingDue > 0.0 -> RmRepaymentStatus.PARTIALLY_REPAID
                else -> RmRepaymentStatus.UNPAID
            }

            val khatianRows = buildKhatianLedgerRows(
                initialBalance = 0.0,
                debitItems = debitItems,
                creditItems = creditItems,
                languageMode = languageMode
            )

            val associatedExcludedAccount = txList.mapNotNull { item ->
                val tx = item.transaction
                if (tx.debitAccountId != null && tx.debitAccountId in excludedAccountIds) allAccounts.firstOrNull { it.id == tx.debitAccountId }
                else if (tx.creditAccountId != null && tx.creditAccountId in excludedAccountIds) allAccounts.firstOrNull { it.id == tx.creditAccountId }
                else null
            }.firstOrNull() ?: excludedAccountsList.firstOrNull()

            rmOthersBreakdowns.add(
                RmEntityBreakdown(
                    id = "label_$labelName",
                    name = labelName,
                    isAccount = false,
                    account = associatedExcludedAccount,
                    totalBorrowed = totalDr,
                    totalRepaid = totalCr,
                    remainingLiability = remainingDue,
                    netBalance = netBal,
                    repaymentProgressPercent = progress,
                    status = status,
                    debitTransactions = debitItems.sortedByDescending { it.dateEpochMs },
                    creditTransactions = creditItems.sortedByDescending { it.dateEpochMs },
                    allTransactions = (debitItems + creditItems).sortedByDescending { it.dateEpochMs },
                    khatianLedgerRows = khatianRows,
                    lastActivityEpochMs = latestTimestamp
                )
            )
        }

        val allCombined = rmAccountBreakdowns + rmOthersBreakdowns

        // Calculate Totals
        val totalGrossBorrowed = allCombined.sumOf { it.totalBorrowed }
        val totalGrossRepaid = allCombined.sumOf { it.totalRepaid }
        val totalOutstandingLiability = allCombined.sumOf { it.remainingLiability }
        val totalNetLiabilityBalance = -totalOutstandingLiability // Negative amount for liabilities
        val overallProgress = when {
            totalGrossBorrowed <= 0.0 -> if (totalGrossRepaid > 0) 1.0f else 0.0f
            totalGrossRepaid >= totalGrossBorrowed -> 1.0f
            else -> (totalGrossRepaid / totalGrossBorrowed).toFloat().coerceIn(0f, 1f)
        }

        val pendingCount = allCombined.count { it.status == RmRepaymentStatus.UNPAID }
        val partiallyRepaidCount = allCombined.count { it.status == RmRepaymentStatus.PARTIALLY_REPAID }
        val settledCount = allCombined.count { it.status == RmRepaymentStatus.SETTLED }

        // Filter by search
        val searchFiltered = if (searchQuery.isBlank()) {
            allCombined
        } else {
            allCombined.filter { entity ->
                entity.name.contains(searchQuery, ignoreCase = true) ||
                entity.allTransactions.any { it.note.contains(searchQuery, ignoreCase = true) || it.displayName.contains(searchQuery, ignoreCase = true) }
            }
        }

        // Filter by category
        val categoryFiltered = when (filterCategory) {
            RmFilterCategory.ALL -> searchFiltered
            RmFilterCategory.PENDING_ONLY -> searchFiltered.filter { it.remainingLiability > 0 }
            RmFilterCategory.RM_ACCOUNTS -> searchFiltered.filter { it.isAccount }
            RmFilterCategory.RM_OTHERS -> searchFiltered.filter { !it.isAccount }
            RmFilterCategory.SETTLED_ONLY -> searchFiltered.filter { it.status == RmRepaymentStatus.SETTLED }
        }

        // Apply Sorting
        val sortedList = when (sortOption) {
            RmSortOption.HIGHEST_DUE -> categoryFiltered.sortedByDescending { it.remainingLiability }
            RmSortOption.LOWEST_DUE -> categoryFiltered.sortedBy { it.remainingLiability }
            RmSortOption.MOST_REPAID -> categoryFiltered.sortedByDescending { it.totalRepaid }
            RmSortOption.NAME_AZ -> categoryFiltered.sortedBy { it.name.lowercase() }
            RmSortOption.RECENT_ACTIVITY -> categoryFiltered.sortedByDescending { it.lastActivityEpochMs }
        }

        val filteredAccounts = sortedList.filter { it.isAccount }
        val filteredOthers = sortedList.filter { !it.isAccount }

        // Generate Global Combined Khatian Rows for all RM items
        val allTxItems = sortedList.flatMap { it.allTransactions }
            .distinctBy { it.transactionWithDetails.transaction.id }
            .sortedBy { it.dateEpochMs }

        var globalRunningBal = 0.0
        val globalRows = mutableListOf<RmKhatianRow>()
        for (item in allTxItems) {
            if (item.isDebit) {
                globalRunningBal -= item.amount // Increases liability -> balance becomes more negative
                globalRows.add(
                    RmKhatianRow(
                        id = "gtx_${item.transactionWithDetails.transaction.id}_dr",
                        dateFormatted = item.dateFormatted,
                        dateEpochMs = item.dateEpochMs,
                        particulars = item.displayName,
                        categoryOrType = item.categoryName,
                        isDebit = true,
                        debitAmount = item.amount,
                        creditAmount = null,
                        balanceJer = globalRunningBal,
                        isReconciled = item.isReconciled,
                        transactionItem = item
                    )
                )
            } else {
                globalRunningBal += item.amount // Repayment -> reduces liability balance towards 0
                globalRows.add(
                    RmKhatianRow(
                        id = "gtx_${item.transactionWithDetails.transaction.id}_cr",
                        dateFormatted = item.dateFormatted,
                        dateEpochMs = item.dateEpochMs,
                        particulars = item.displayName,
                        categoryOrType = item.categoryName,
                        isDebit = false,
                        debitAmount = null,
                        creditAmount = item.amount,
                        balanceJer = globalRunningBal,
                        isReconciled = item.isReconciled,
                        transactionItem = item
                    )
                )
            }
        }

        return RmManagerScreenData(
            allEntities = sortedList,
            rmAccounts = filteredAccounts,
            rmOthers = filteredOthers,
            totalOutstandingLiability = totalOutstandingLiability,
            totalNetLiabilityBalance = totalNetLiabilityBalance,
            totalGrossBorrowed = totalGrossBorrowed,
            totalGrossRepaid = totalGrossRepaid,
            overallRepaymentProgress = overallProgress,
            pendingCount = pendingCount,
            partiallyRepaidCount = partiallyRepaidCount,
            settledCount = settledCount,
            globalKhatianRows = globalRows.sortedBy { it.dateEpochMs }
        )
    }

    /**
     * Builds chronological Khatian (খতিয়ান - জের) ledger rows with running balance.
     * Oldest first (chronological order) so running balance starts from opening/first entry down to latest.
     * Liability increases -> Balance goes negative (-৳)
     * Repayment occurs -> Balance increases towards 0
     */
    private fun buildKhatianLedgerRows(
        initialBalance: Double,
        debitItems: List<RmTransactionItem>,
        creditItems: List<RmTransactionItem>,
        languageMode: LanguageMode
    ): List<RmKhatianRow> {
        val rows = mutableListOf<RmKhatianRow>()
        var runningJer = 0.0

        // Initial Balance (Opening Jer) if any
        if (initialBalance > 0.0) {
            runningJer = -initialBalance // Initial liability
            rows.add(
                RmKhatianRow(
                    id = "opening_balance",
                    dateFormatted = LanguageHelper.getString("initial_balance_carried", languageMode),
                    miniDateFormatted = "Init",
                    dateEpochMs = 0L,
                    particulars = LanguageHelper.getString("initial_balance_carried", languageMode),
                    categoryOrType = "Opening",
                    isDebit = true,
                    debitAmount = initialBalance,
                    creditAmount = null,
                    balanceJer = runningJer,
                    isReconciled = true,
                    transactionItem = null
                )
            )
        }

        // Combine all items in chronological ascending order (oldest first)
        val allSortedChronological = (debitItems + creditItems).sortedWith(
            compareBy<RmTransactionItem> { it.dateEpochMs }
                .thenBy { it.transactionWithDetails.transaction.id }
        )

        for (item in allSortedChronological) {
            if (item.isDebit) {
                // Debit / Borrowed: Liability Increases -> Balance becomes more negative
                runningJer -= item.amount
                rows.add(
                    RmKhatianRow(
                        id = "row_${item.transactionWithDetails.transaction.id}_dr",
                        dateFormatted = item.dateFormatted,
                        miniDateFormatted = formatMiniDate(item.dateEpochMs),
                        dateEpochMs = item.dateEpochMs,
                        particulars = item.displayName,
                        categoryOrType = item.categoryName,
                        isDebit = true,
                        debitAmount = item.amount,
                        creditAmount = null,
                        balanceJer = runningJer,
                        isReconciled = item.isReconciled,
                        transactionItem = item
                    )
                )
            } else {
                // Credit / Repaid: Liability Decreases -> Balance becomes less negative / closer to 0
                runningJer += item.amount
                rows.add(
                    RmKhatianRow(
                        id = "row_${item.transactionWithDetails.transaction.id}_cr",
                        dateFormatted = item.dateFormatted,
                        miniDateFormatted = formatMiniDate(item.dateEpochMs),
                        dateEpochMs = item.dateEpochMs,
                        particulars = item.displayName,
                        categoryOrType = item.categoryName,
                        isDebit = false,
                        debitAmount = null,
                        creditAmount = item.amount,
                        balanceJer = runningJer,
                        isReconciled = item.isReconciled,
                        transactionItem = item
                    )
                )
            }
        }

        // Return oldest first (ascending chronological order)
        return rows.sortedBy { it.dateEpochMs }
    }

    /**
     * Determines whether a labeled transaction is classified as Debit or Credit for the label.
     */
    private fun isTransactionDebitForLabel(item: TransactionWithDetails, label: String): Boolean {
        val tx = item.transaction
        val note = tx.note.lowercase()
        val payee = tx.payeeOrPayer.lowercase()

        // If explicit debit / credit keyword in note or payee
        if (note.contains("debit") || payee.contains("debit") || note.contains("dr") || payee.contains("dr")) {
            return true
        }
        if (note.contains("credit") || payee.contains("credit") || note.contains("cr") || payee.contains("cr")) {
            return false
        }

        // Income -> Debit (Inflow / Taken In / Liability), Expense -> Credit (Repayment / Settled)
        return when (tx.type) {
            TransactionType.INCOME -> true
            TransactionType.EXPENSE -> false
            TransactionType.TRANSFER -> true
        }
    }

    /**
     * Formats display name with "Name Debit" or "Name Credit".
     */
    private fun buildTransactionDisplayName(
        item: TransactionWithDetails,
        isDebit: Boolean,
        defaultName: String
    ): String {
        val tx = item.transaction
        val note = tx.note.trim()
        val payee = tx.payeeOrPayer.trim()

        val suffix = if (isDebit) "Debit" else "Credit"

        val baseName = when {
            payee.isNotBlank() -> payee
            note.isNotBlank() && note.length <= 30 && !note.contains("\n") -> note.removePrefix("#")
            else -> defaultName
        }

        val cleanBase = baseName.replace(Regex("""(?i)\b(Debit|Credit)\b"""), "").trim()
        return if (cleanBase.isNotBlank()) "$cleanBase $suffix" else "$defaultName $suffix"
    }

    /**
     * Generates a clean text statement to share via SMS, WhatsApp, or Copy to Clipboard.
     */
    fun generateStatementText(entity: RmEntityBreakdown, languageMode: LanguageMode): String {
        val isBn = languageMode == LanguageMode.BANGLA
        val sb = StringBuilder()
        sb.append(if (isBn) "📋 রেস্টিং মানি ও খতিয়ান জের বিবরণী\n" else "📋 Resting Money & Khatian Ledger Statement\n")
        sb.append(if (isBn) "নাম / একাউন্ট: ${entity.name}\n" else "Name / Account: ${entity.name}\n")
        sb.append("----------------------------\n")
        sb.append(if (isBn) "মোট দায় / গৃহীত (Debit): ৳ ${formatAmount(entity.totalBorrowed)}\n" else "Total Liability / Borrowed (Dr): ৳ ${formatAmount(entity.totalBorrowed)}\n")
        sb.append(if (isBn) "মোট পরিশোধিত (Credit): ৳ ${formatAmount(entity.totalRepaid)}\n" else "Total Repaid / Settled (Cr): ৳ ${formatAmount(entity.totalRepaid)}\n")
        val netBalStr = formatSignedLiabilityAmount(entity.netBalance)
        sb.append(if (isBn) "বর্তমান জের (Net Balance): $netBalStr\n" else "Current Balance (Jer): $netBalStr\n")
        sb.append(if (isBn) "অবশিষ্ট বকেয়া দেনা: ৳ ${formatAmount(entity.remainingLiability)}\n" else "Outstanding Due: ৳ ${formatAmount(entity.remainingLiability)}\n")
        sb.append(if (isBn) "পরিশোধের অগ্রগতি: ${(entity.repaymentProgressPercent * 100).toInt()}%\n" else "Repayment Progress: ${(entity.repaymentProgressPercent * 100).toInt()}%\n")
        sb.append("----------------------------\n")
        sb.append(if (isBn) "খতিয়ান লেনদেন তালিকা (${entity.khatianLedgerRows.size} টি):\n" else "Khatian Ledger Entries (${entity.khatianLedgerRows.size}):\n")

        for (row in entity.khatianLedgerRows.take(12)) {
            val drStr = if (row.debitAmount != null) "Dr: ৳${formatAmount(row.debitAmount)}" else "—"
            val crStr = if (row.creditAmount != null) "Cr: ৳${formatAmount(row.creditAmount)}" else "—"
            val jerStr = "Jer: ${formatSignedLiabilityAmount(row.balanceJer)}"
            sb.append("• ${row.dateFormatted} | ${row.particulars} | $drStr | $crStr | $jerStr\n")
        }

        if (entity.khatianLedgerRows.size > 12) {
            sb.append(if (isBn) "... এবং আরও ${entity.khatianLedgerRows.size - 12} টি লেনদেন\n" else "... and ${entity.khatianLedgerRows.size - 12} more entries\n")
        }

        sb.append("\n" + (if (isBn) "Budgeter অ্যাপ থেকে প্রস্তুতকৃত।" else "Generated via Budgeter app."))
        return sb.toString()
    }

    fun formatAmount(amount: Double): String {
        val absVal = abs(amount)
        return if (absVal % 1.0 == 0.0) {
            String.format(Locale.US, "%,.1f", absVal)
        } else {
            String.format(Locale.US, "%,.2f", absVal)
        }
    }

    /**
     * Formats signed liability amounts:
     * e.g. -৳ 5,000.00 when liability exists (< 0)
     * e.g. ৳ 0.00 when settled
     * e.g. +৳ 500.00 when overpaid (> 0)
     */
    fun formatSignedLiabilityAmount(amount: Double): String {
        return when {
            amount < -0.001 -> "-৳ ${formatAmount(amount)}"
            amount > 0.001 -> "+৳ ${formatAmount(amount)}"
            else -> "৳ 0.0"
        }
    }
}
