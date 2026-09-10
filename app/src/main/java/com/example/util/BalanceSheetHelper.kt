package com.example.util

import com.example.data.model.Account
import com.example.data.model.AccountType
import com.example.data.model.LanguageMode
import com.example.data.model.Transaction
import com.example.data.model.TransactionStatus
import java.util.Calendar

enum class BalanceSheetComparisonPreset(val titleEn: String, val titleBn: String) {
    END_OF_LAST_MONTH("End of Last Month", "গত মাসের শেষ"),
    BEGINNING_OF_MONTH("Start of This Month", "এই মাসের শুরু"),
    PREVIOUS_MONTH("Previous Month", "পূর্ববর্তী মাস"),
    BEGINNING_OF_YEAR("Start of Year", "বছরের শুরু"),
    LAST_30_DAYS("Last 30 Days", "গত ৩০ দিন"),
    TODAY("Today", "আজকে"),
    CUSTOM("Custom Date Range", "কাস্টম সময়কাল")
}

enum class BalanceSheetSortOrder(val titleEn: String, val titleBn: String) {
    DEFAULT("Default Order", "ডিফল্ট ক্রম"),
    AMOUNT_DESC("Amount: High to Low", "পরিমাণ: বেশি থেকে কম"),
    AMOUNT_ASC("Amount: Low to High", "পরিমাণ: কম থেকে বেশি"),
    NAME_ASC("Name: A to Z", "নাম: ক থেকে ঁ")
}

data class BalanceSheetAccountRow(
    val account: Account,
    val baseBalance: Double,
    val currentBalance: Double,
    val percentageShare: Double,
    val delta: Double = currentBalance - baseBalance,
    val isIncludedInCalc: Boolean = true,
    val adjustmentAmount: Double = 0.0,
    val effectiveBaseBalance: Double = baseBalance,
    val effectiveCurrentBalance: Double = currentBalance
)

data class BalanceSheetGroup(
    val parentAccount: Account,
    val baseBalance: Double,
    val currentBalance: Double,
    val percentageShare: Double,
    val subAccounts: List<BalanceSheetAccountRow>,
    val delta: Double = currentBalance - baseBalance,
    val isIncludedInCalc: Boolean = true,
    val adjustmentAmount: Double = 0.0,
    val effectiveBaseBalance: Double = baseBalance,
    val effectiveCurrentBalance: Double = currentBalance
)

data class BalanceSheetComparisonData(
    val preset: BalanceSheetComparisonPreset,
    val baseDateEpochMs: Long,
    val compareDateEpochMs: Long,
    val baseDateLabel: String,
    val compareDateLabel: String,
    val totalAssetsBase: Double,
    val totalAssetsCurrent: Double,
    val totalLiabilitiesBase: Double,
    val totalLiabilitiesCurrent: Double,
    val netWorthBase: Double,
    val netWorthCurrent: Double,
    val netWorthDelta: Double,
    val assetGroups: List<BalanceSheetGroup>,
    val liabilityGroups: List<BalanceSheetGroup>
)

object BalanceSheetHelper {

    fun getPresetDateRanges(preset: BalanceSheetComparisonPreset): Pair<Long, Long> {
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance()

        return when (preset) {
            BalanceSheetComparisonPreset.END_OF_LAST_MONTH -> {
                // End of last month (23:59:59 of last day of previous month) vs Now
                cal.timeInMillis = now
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val base = cal.timeInMillis - 1000L // last millisecond of prev month
                Pair(base, now)
            }
            BalanceSheetComparisonPreset.BEGINNING_OF_MONTH -> {
                cal.timeInMillis = now
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val base = cal.timeInMillis
                Pair(base, now)
            }
            BalanceSheetComparisonPreset.PREVIOUS_MONTH -> {
                // Two full months ago vs End of last month
                cal.timeInMillis = now
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val endOfLastMonth = cal.timeInMillis - 1000L
                cal.add(Calendar.MONTH, -1)
                val endOfTwoMonthsAgo = cal.timeInMillis - 1000L
                Pair(endOfTwoMonthsAgo, endOfLastMonth)
            }
            BalanceSheetComparisonPreset.BEGINNING_OF_YEAR -> {
                cal.timeInMillis = now
                cal.set(Calendar.MONTH, Calendar.JANUARY)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val base = cal.timeInMillis - 1000L
                Pair(base, now)
            }
            BalanceSheetComparisonPreset.LAST_30_DAYS -> {
                val thirtyDaysAgo = now - (30L * 24L * 60L * 60L * 1000L)
                Pair(thirtyDaysAgo, now)
            }
            BalanceSheetComparisonPreset.TODAY -> {
                cal.timeInMillis = now
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val base = cal.timeInMillis
                Pair(base, now)
            }
            BalanceSheetComparisonPreset.CUSTOM -> {
                Pair(now - (30L * 24L * 60L * 60L * 1000L), now)
            }
        }
    }

    fun calculateBalanceSheet(
        accounts: List<Account>,
        transactions: List<Transaction>,
        baseDateEpochMs: Long,
        compareDateEpochMs: Long,
        preset: BalanceSheetComparisonPreset = BalanceSheetComparisonPreset.END_OF_LAST_MONTH,
        selectedAccountIds: Set<Long> = emptySet(),
        selectedStatusSet: Set<TransactionStatus> = emptySet(),
        activeOnly: Boolean = true,
        showHiddenAccounts: Boolean = false,
        excludeZeroAmounts: Boolean = true,
        filterNonZeroGroups: Boolean = false,
        sortOrder: BalanceSheetSortOrder = BalanceSheetSortOrder.AMOUNT_DESC,
        searchQuery: String = "",
        accountCalcConfig: AccountCalcConfig = AccountCalcConfig(),
        showOnlyAccountsWithoutGroups: Boolean = false,
        languageMode: LanguageMode = LanguageMode.ENGLISH
    ): BalanceSheetComparisonData {
        val baseDateLabel = DateUtils.formatDate(baseDateEpochMs, languageMode)
        val compareDateLabel = DateUtils.formatDate(compareDateEpochMs, languageMode)

        // Filter transactions if status filter is active
        val validTransactions = if (selectedStatusSet.isNotEmpty()) {
            transactions.filter { it.status in selectedStatusSet }
        } else {
            transactions
        }

        // Pre-calculate debits and credits up to both timestamps
        val baseDebits = mutableMapOf<Long, Double>()
        val baseCredits = mutableMapOf<Long, Double>()
        val currDebits = mutableMapOf<Long, Double>()
        val currCredits = mutableMapOf<Long, Double>()

        for (tx in validTransactions) {
            if (tx.dateEpochMs <= baseDateEpochMs) {
                tx.debitAccountId?.let { baseDebits[it] = (baseDebits[it] ?: 0.0) + tx.amount }
                tx.creditAccountId?.let { baseCredits[it] = (baseCredits[it] ?: 0.0) + tx.amount }
            }
            if (tx.dateEpochMs <= compareDateEpochMs) {
                tx.debitAccountId?.let { currDebits[it] = (currDebits[it] ?: 0.0) + tx.amount }
                tx.creditAccountId?.let { currCredits[it] = (currCredits[it] ?: 0.0) + tx.amount }
            }
        }

        fun getAccountBalance(acc: Account, isBase: Boolean): Double {
            val dr = if (isBase) (baseDebits[acc.id] ?: 0.0) else (currDebits[acc.id] ?: 0.0)
            val cr = if (isBase) (baseCredits[acc.id] ?: 0.0) else (currCredits[acc.id] ?: 0.0)
            return when (acc.type) {
                AccountType.ASSET, AccountType.EXPENSE -> acc.initialBalance + (dr - cr)
                AccountType.LIABILITY, AccountType.EQUITY, AccountType.INCOME -> acc.initialBalance + (cr - dr)
            }
        }

        val filteredAccounts = accounts.filter { acc ->
            val matchesHidden = if (showHiddenAccounts) true else (if (activeOnly) acc.isActive else true)
            val matchesSearch = (searchQuery.isBlank() || acc.nameEn.contains(searchQuery, ignoreCase = true) || acc.nameBn.contains(searchQuery, ignoreCase = true))
            val matchesSelected = if (selectedAccountIds.isEmpty()) true else {
                selectedAccountIds.contains(acc.id) || (acc.parentId != null && selectedAccountIds.contains(acc.parentId))
            }
            matchesHidden && matchesSearch && matchesSelected
        }

        val parentAccounts = filteredAccounts.filter { it.parentId == null }
        val subAccountsByParent = filteredAccounts.filter { it.parentId != null }.groupBy { it.parentId!! }

        // Compute balances for all accounts
        val baseBalanceMap = filteredAccounts.associate { it.id to getAccountBalance(it, true) }
        val currBalanceMap = filteredAccounts.associate { it.id to getAccountBalance(it, false) }

        fun buildGroups(accountType: AccountType): List<BalanceSheetGroup> {
            val typeAccounts = filteredAccounts.filter { it.type == accountType }
            val groups = mutableListOf<BalanceSheetGroup>()

            if (showOnlyAccountsWithoutGroups) {
                for (acc in typeAccounts) {
                    val subs = subAccountsByParent[acc.id] ?: emptyList()
                    val isParentWithSubs = acc.parentId == null && subs.isNotEmpty()

                    val baseBal = baseBalanceMap[acc.id] ?: 0.0
                    val currBal = currBalanceMap[acc.id] ?: 0.0

                    // If account is a parent with subaccounts, only include it if it has its own direct balance
                    if (isParentWithSubs && Math.abs(baseBal) < 0.001 && Math.abs(currBal) < 0.001) {
                        continue
                    }

                    if (excludeZeroAmounts && Math.abs(baseBal) < 0.001 && Math.abs(currBal) < 0.001) {
                        continue
                    }

                    val isInc = accountCalcConfig.isIncluded(acc.id)
                    val adj = accountCalcConfig.getAdjustment(acc.id)
                    val effBase = if (!isInc) 0.0 else (baseBal + adj)
                    val effCurr = if (!isInc) 0.0 else (currBal + adj)

                    if (filterNonZeroGroups && Math.abs(effCurr) < 0.001 && Math.abs(effBase) < 0.001) {
                        continue
                    }

                    groups.add(
                        BalanceSheetGroup(
                            parentAccount = acc,
                            baseBalance = baseBal,
                            currentBalance = currBal,
                            percentageShare = 0.0,
                            subAccounts = emptyList(),
                            isIncludedInCalc = isInc,
                            adjustmentAmount = adj,
                            effectiveBaseBalance = effBase,
                            effectiveCurrentBalance = effCurr
                        )
                    )
                }
                return groups
            }

            val parents = parentAccounts.filter { it.type == accountType }

            for (parent in parents) {
                val subs = subAccountsByParent[parent.id] ?: emptyList()
                val subRows = subs.map { sub ->
                    val baseBal = baseBalanceMap[sub.id] ?: 0.0
                    val currBal = currBalanceMap[sub.id] ?: 0.0
                    val isSubInc = accountCalcConfig.isIncluded(sub.id)
                    val subAdj = accountCalcConfig.getAdjustment(sub.id)
                    val effBase = if (!isSubInc) 0.0 else (baseBal + subAdj)
                    val effCurr = if (!isSubInc) 0.0 else (currBal + subAdj)
                    BalanceSheetAccountRow(
                        account = sub,
                        baseBalance = baseBal,
                        currentBalance = currBal,
                        percentageShare = 0.0, // computed below
                        isIncludedInCalc = isSubInc,
                        adjustmentAmount = subAdj,
                        effectiveBaseBalance = effBase,
                        effectiveCurrentBalance = effCurr
                    )
                }.filter { !excludeZeroAmounts || Math.abs(it.baseBalance) > 0.001 || Math.abs(it.currentBalance) > 0.001 }

                val isParentInc = accountCalcConfig.isIncluded(parent.id)
                val parentAdj = accountCalcConfig.getAdjustment(parent.id)
                val parentBaseBal = if (subRows.isNotEmpty()) subRows.sumOf { it.baseBalance } else (baseBalanceMap[parent.id] ?: 0.0)
                val parentCurrBal = if (subRows.isNotEmpty()) subRows.sumOf { it.currentBalance } else (currBalanceMap[parent.id] ?: 0.0)

                val effectiveGroupBase = if (!isParentInc) 0.0 else if (subRows.isNotEmpty()) subRows.sumOf { it.effectiveBaseBalance } + parentAdj else parentBaseBal + parentAdj
                val effectiveGroupCurr = if (!isParentInc) 0.0 else if (subRows.isNotEmpty()) subRows.sumOf { it.effectiveCurrentBalance } + parentAdj else parentCurrBal + parentAdj

                if (!excludeZeroAmounts || Math.abs(parentBaseBal) > 0.001 || Math.abs(parentCurrBal) > 0.001 || subRows.isNotEmpty()) {
                    if (filterNonZeroGroups && Math.abs(effectiveGroupCurr) < 0.001 && Math.abs(effectiveGroupBase) < 0.001) {
                        // Skip non-zero group when filterNonZeroGroups is enabled
                    } else {
                        groups.add(
                            BalanceSheetGroup(
                                parentAccount = parent,
                                baseBalance = parentBaseBal,
                                currentBalance = parentCurrBal,
                                percentageShare = 0.0, // computed below
                                subAccounts = subRows,
                                isIncludedInCalc = isParentInc,
                                adjustmentAmount = parentAdj,
                                effectiveBaseBalance = effectiveGroupBase,
                                effectiveCurrentBalance = effectiveGroupCurr
                            )
                        )
                    }
                }
            }
            return groups
        }

        val rawAssetGroups = buildGroups(AccountType.ASSET)
        val rawLiabilityGroups = buildGroups(AccountType.LIABILITY)

        val totalAssetsBase = rawAssetGroups.sumOf { it.effectiveBaseBalance }
        val totalAssetsCurrent = rawAssetGroups.sumOf { it.effectiveCurrentBalance }

        val totalLiabilitiesBase = rawLiabilityGroups.sumOf { it.effectiveBaseBalance }
        val totalLiabilitiesCurrent = rawLiabilityGroups.sumOf { it.effectiveCurrentBalance }

        val netWorthBase = totalAssetsBase - totalLiabilitiesBase
        val netWorthCurrent = totalAssetsCurrent - totalLiabilitiesCurrent
        val netWorthDelta = netWorthCurrent - netWorthBase

        // Compute percentage shares
        val rawMappedAssets = rawAssetGroups.map { group ->
            val groupShare = if (totalAssetsCurrent > 0 && group.isIncludedInCalc) (group.effectiveCurrentBalance / totalAssetsCurrent) * 100.0 else 0.0
            val updatedSubs = group.subAccounts.map { sub ->
                val subShare = if (group.effectiveCurrentBalance > 0 && sub.isIncludedInCalc) (sub.effectiveCurrentBalance / group.effectiveCurrentBalance) * 100.0 else 0.0
                sub.copy(percentageShare = subShare.coerceAtLeast(0.0))
            }
            group.copy(
                percentageShare = groupShare.coerceAtLeast(0.0),
                subAccounts = updatedSubs
            )
        }

        val rawMappedLiabilities = rawLiabilityGroups.map { group ->
            val groupShare = if (totalLiabilitiesCurrent > 0 && group.isIncludedInCalc) (group.effectiveCurrentBalance / totalLiabilitiesCurrent) * 100.0 else 0.0
            val updatedSubs = group.subAccounts.map { sub ->
                val subShare = if (group.effectiveCurrentBalance > 0 && sub.isIncludedInCalc) (sub.effectiveCurrentBalance / group.effectiveCurrentBalance) * 100.0 else 0.0
                sub.copy(percentageShare = subShare.coerceAtLeast(0.0))
            }
            group.copy(
                percentageShare = groupShare.coerceAtLeast(0.0),
                subAccounts = updatedSubs
            )
        }

        // Apply Sorting by Amount or Name
        val assetGroups = when (sortOrder) {
            BalanceSheetSortOrder.DEFAULT -> rawMappedAssets
            BalanceSheetSortOrder.AMOUNT_DESC -> rawMappedAssets.sortedByDescending { it.effectiveCurrentBalance }
            BalanceSheetSortOrder.AMOUNT_ASC -> rawMappedAssets.sortedBy { it.effectiveCurrentBalance }
            BalanceSheetSortOrder.NAME_ASC -> rawMappedAssets.sortedBy { it.parentAccount.nameEn.lowercase() }
        }.map { group ->
            val sortedSubs = when (sortOrder) {
                BalanceSheetSortOrder.DEFAULT -> group.subAccounts
                BalanceSheetSortOrder.AMOUNT_DESC -> group.subAccounts.sortedByDescending { it.effectiveCurrentBalance }
                BalanceSheetSortOrder.AMOUNT_ASC -> group.subAccounts.sortedBy { it.effectiveCurrentBalance }
                BalanceSheetSortOrder.NAME_ASC -> group.subAccounts.sortedBy { it.account.nameEn.lowercase() }
            }
            group.copy(subAccounts = sortedSubs)
        }

        val liabilityGroups = when (sortOrder) {
            BalanceSheetSortOrder.DEFAULT -> rawMappedLiabilities
            BalanceSheetSortOrder.AMOUNT_DESC -> rawMappedLiabilities.sortedByDescending { it.effectiveCurrentBalance }
            BalanceSheetSortOrder.AMOUNT_ASC -> rawMappedLiabilities.sortedBy { it.effectiveCurrentBalance }
            BalanceSheetSortOrder.NAME_ASC -> rawMappedLiabilities.sortedBy { it.parentAccount.nameEn.lowercase() }
        }.map { group ->
            val sortedSubs = when (sortOrder) {
                BalanceSheetSortOrder.DEFAULT -> group.subAccounts
                BalanceSheetSortOrder.AMOUNT_DESC -> group.subAccounts.sortedByDescending { it.effectiveCurrentBalance }
                BalanceSheetSortOrder.AMOUNT_ASC -> group.subAccounts.sortedBy { it.effectiveCurrentBalance }
                BalanceSheetSortOrder.NAME_ASC -> group.subAccounts.sortedBy { it.account.nameEn.lowercase() }
            }
            group.copy(subAccounts = sortedSubs)
        }

        return BalanceSheetComparisonData(
            preset = preset,
            baseDateEpochMs = baseDateEpochMs,
            compareDateEpochMs = compareDateEpochMs,
            baseDateLabel = baseDateLabel,
            compareDateLabel = compareDateLabel,
            totalAssetsBase = totalAssetsBase,
            totalAssetsCurrent = totalAssetsCurrent,
            totalLiabilitiesBase = totalLiabilitiesBase,
            totalLiabilitiesCurrent = totalLiabilitiesCurrent,
            netWorthBase = netWorthBase,
            netWorthCurrent = netWorthCurrent,
            netWorthDelta = netWorthDelta,
            assetGroups = assetGroups,
            liabilityGroups = liabilityGroups
        )
    }
}
