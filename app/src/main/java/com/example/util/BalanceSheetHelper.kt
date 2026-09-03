package com.example.util

import com.example.data.model.Account
import com.example.data.model.AccountType
import com.example.data.model.Transaction
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class BalanceSheetComparisonPreset {
    END_OF_LAST_MONTH,
    BEGINNING_OF_MONTH,
    PREVIOUS_MONTH,
    BEGINNING_OF_YEAR,
    LAST_30_DAYS,
    CUSTOM
}

data class BalanceSheetAccountRow(
    val account: Account,
    val baseBalance: Double,
    val currentBalance: Double,
    val percentageShare: Double,
    val delta: Double = currentBalance - baseBalance
)

data class BalanceSheetGroup(
    val parentAccount: Account,
    val baseBalance: Double,
    val currentBalance: Double,
    val percentageShare: Double,
    val subAccounts: List<BalanceSheetAccountRow>,
    val delta: Double = currentBalance - baseBalance
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
        activeOnly: Boolean = true,
        hideZeroBalance: Boolean = false,
        searchQuery: String = ""
    ): BalanceSheetComparisonData {
        val dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.US)
        val baseDateLabel = dateFormat.format(Date(baseDateEpochMs))
        val compareDateLabel = dateFormat.format(Date(compareDateEpochMs))

        // Pre-calculate debits and credits up to both timestamps
        val baseDebits = mutableMapOf<Long, Double>()
        val baseCredits = mutableMapOf<Long, Double>()
        val currDebits = mutableMapOf<Long, Double>()
        val currCredits = mutableMapOf<Long, Double>()

        for (tx in transactions) {
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
            (!activeOnly || acc.isActive) &&
            (searchQuery.isBlank() || acc.nameEn.contains(searchQuery, ignoreCase = true) || acc.nameBn.contains(searchQuery, ignoreCase = true))
        }

        val parentAccounts = filteredAccounts.filter { it.parentId == null }
        val subAccountsByParent = filteredAccounts.filter { it.parentId != null }.groupBy { it.parentId!! }

        // Compute balances for all accounts
        val baseBalanceMap = filteredAccounts.associate { it.id to getAccountBalance(it, true) }
        val currBalanceMap = filteredAccounts.associate { it.id to getAccountBalance(it, false) }

        fun buildGroups(accountType: AccountType): List<BalanceSheetGroup> {
            val parents = parentAccounts.filter { it.type == accountType }
            val groups = mutableListOf<BalanceSheetGroup>()

            for (parent in parents) {
                val subs = subAccountsByParent[parent.id] ?: emptyList()
                val subRows = subs.map { sub ->
                    val baseBal = baseBalanceMap[sub.id] ?: 0.0
                    val currBal = currBalanceMap[sub.id] ?: 0.0
                    BalanceSheetAccountRow(
                        account = sub,
                        baseBalance = baseBal,
                        currentBalance = currBal,
                        percentageShare = 0.0 // computed below
                    )
                }.filter { !hideZeroBalance || Math.abs(it.baseBalance) > 0.001 || Math.abs(it.currentBalance) > 0.001 }

                val parentBaseBal = if (subRows.isNotEmpty()) subRows.sumOf { it.baseBalance } else (baseBalanceMap[parent.id] ?: 0.0)
                val parentCurrBal = if (subRows.isNotEmpty()) subRows.sumOf { it.currentBalance } else (currBalanceMap[parent.id] ?: 0.0)

                if (!hideZeroBalance || Math.abs(parentBaseBal) > 0.001 || Math.abs(parentCurrBal) > 0.001 || subRows.isNotEmpty()) {
                    groups.add(
                        BalanceSheetGroup(
                            parentAccount = parent,
                            baseBalance = parentBaseBal,
                            currentBalance = parentCurrBal,
                            percentageShare = 0.0, // computed below
                            subAccounts = subRows
                        )
                    )
                }
            }
            return groups
        }

        val rawAssetGroups = buildGroups(AccountType.ASSET)
        val rawLiabilityGroups = buildGroups(AccountType.LIABILITY)

        val totalAssetsBase = rawAssetGroups.sumOf { it.baseBalance }
        val totalAssetsCurrent = rawAssetGroups.sumOf { it.currentBalance }

        val totalLiabilitiesBase = rawLiabilityGroups.sumOf { it.baseBalance }
        val totalLiabilitiesCurrent = rawLiabilityGroups.sumOf { it.currentBalance }

        val netWorthBase = totalAssetsBase - totalLiabilitiesBase
        val netWorthCurrent = totalAssetsCurrent - totalLiabilitiesCurrent
        val netWorthDelta = netWorthCurrent - netWorthBase

        // Compute percentage shares
        val assetGroups = rawAssetGroups.map { group ->
            val groupShare = if (totalAssetsCurrent > 0) (group.currentBalance / totalAssetsCurrent) * 100.0 else 0.0
            val updatedSubs = group.subAccounts.map { sub ->
                val subShare = if (group.currentBalance > 0) (sub.currentBalance / group.currentBalance) * 100.0 else 0.0
                sub.copy(percentageShare = subShare.coerceAtLeast(0.0))
            }
            group.copy(
                percentageShare = groupShare.coerceAtLeast(0.0),
                subAccounts = updatedSubs
            )
        }

        val liabilityGroups = rawLiabilityGroups.map { group ->
            val groupShare = if (totalLiabilitiesCurrent > 0) (group.currentBalance / totalLiabilitiesCurrent) * 100.0 else 0.0
            val updatedSubs = group.subAccounts.map { sub ->
                val subShare = if (group.currentBalance > 0) (sub.currentBalance / group.currentBalance) * 100.0 else 0.0
                sub.copy(percentageShare = subShare.coerceAtLeast(0.0))
            }
            group.copy(
                percentageShare = groupShare.coerceAtLeast(0.0),
                subAccounts = updatedSubs
            )
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
