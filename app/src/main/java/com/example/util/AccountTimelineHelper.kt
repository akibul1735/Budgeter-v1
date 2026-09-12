package com.example.util

import com.example.data.model.Account
import com.example.data.model.AccountType
import com.example.data.model.LanguageMode
import com.example.data.model.Transaction
import com.example.data.model.TransactionStatus
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class AccountTimelineInterval(val titleEn: String, val titleBn: String) {
    MONTHLY("Monthly (6 Months)", "মাসিক (৬ মাস)"),
    PAST_3_MONTHS("Past 3 Months", "গত ৩ মাস"),
    PAST_12_MONTHS("Past 12 Months", "গত ১২ মাস"),
    QUARTERLY("Quarterly (4 Quarters)", "ত্রৈমাসিক (৪ কোয়ার্টার)"),
    YEARLY("Yearly", "বাৎসরিক"),
    ALL_TIME("All Time", "সর্বমোট সময়কাল")
}

data class TimelinePeriod(
    val epochMs: Long,
    val label: String,
    val shortLabel: String
)

data class TimelineAccountRow(
    val account: Account,
    val balances: List<Double>
)

data class TimelineGroup(
    val parentAccount: Account,
    val groupBalances: List<Double>,
    val subAccounts: List<TimelineAccountRow>
)

data class AccountTimelineData(
    val interval: AccountTimelineInterval,
    val periods: List<TimelinePeriod>,
    val assetGroups: List<TimelineGroup>,
    val liabilityGroups: List<TimelineGroup>,
    val totalAssetsByPeriod: List<Double>,
    val totalLiabilitiesByPeriod: List<Double>,
    val netWorthByPeriod: List<Double>
)

object AccountTimelineHelper {

    fun generatePeriods(
        interval: AccountTimelineInterval,
        customPeriodsCount: Int = 6,
        languageMode: LanguageMode = LanguageMode.ENGLISH
    ): List<TimelinePeriod> {
        val periods = mutableListOf<TimelinePeriod>()
        val cal = Calendar.getInstance()
        val now = System.currentTimeMillis()
        val shortSdf = SimpleDateFormat("M/d/yy", Locale.US)
        val fullSdf = SimpleDateFormat("dd MMM yyyy", Locale.US)

        when (interval) {
            AccountTimelineInterval.PAST_3_MONTHS -> {
                generateMonthlyPeriods(periods, count = 3, cal, shortSdf, fullSdf, languageMode)
            }
            AccountTimelineInterval.MONTHLY -> {
                generateMonthlyPeriods(periods, count = customPeriodsCount.coerceIn(2, 12), cal, shortSdf, fullSdf, languageMode)
            }
            AccountTimelineInterval.PAST_12_MONTHS -> {
                generateMonthlyPeriods(periods, count = 12, cal, shortSdf, fullSdf, languageMode)
            }
            AccountTimelineInterval.QUARTERLY -> {
                // 4 Quarters
                for (i in 3 downTo 0) {
                    val c = Calendar.getInstance()
                    c.add(Calendar.MONTH, -i * 3)
                    // Move to end of current month
                    c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH))
                    c.set(Calendar.HOUR_OF_DAY, 23)
                    c.set(Calendar.MINUTE, 59)
                    c.set(Calendar.SECOND, 59)
                    c.set(Calendar.MILLISECOND, 999)
                    val ms = c.timeInMillis
                    val shortLbl = shortSdf.format(Date(ms))
                    val fullLbl = fullSdf.format(Date(ms))
                    periods.add(TimelinePeriod(ms, fullLbl, shortLbl))
                }
            }
            AccountTimelineInterval.YEARLY -> {
                // Past 4 years
                for (i in 3 downTo 0) {
                    val c = Calendar.getInstance()
                    c.add(Calendar.YEAR, -i)
                    c.set(Calendar.MONTH, Calendar.DECEMBER)
                    c.set(Calendar.DAY_OF_MONTH, 31)
                    c.set(Calendar.HOUR_OF_DAY, 23)
                    c.set(Calendar.MINUTE, 59)
                    c.set(Calendar.SECOND, 59)
                    c.set(Calendar.MILLISECOND, 999)
                    val ms = if (i == 0) now else c.timeInMillis
                    val shortLbl = shortSdf.format(Date(ms))
                    val fullLbl = fullSdf.format(Date(ms))
                    periods.add(TimelinePeriod(ms, fullLbl, shortLbl))
                }
            }
            AccountTimelineInterval.ALL_TIME -> {
                generateMonthlyPeriods(periods, count = 6, cal, shortSdf, fullSdf, languageMode)
            }
        }
        return periods
    }

    private fun generateMonthlyPeriods(
        periods: MutableList<TimelinePeriod>,
        count: Int,
        cal: Calendar,
        shortSdf: SimpleDateFormat,
        fullSdf: SimpleDateFormat,
        languageMode: LanguageMode
    ) {
        val now = System.currentTimeMillis()
        for (i in (count - 1) downTo 0) {
            val c = Calendar.getInstance()
            c.add(Calendar.MONTH, -i)
            c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH))
            c.set(Calendar.HOUR_OF_DAY, 23)
            c.set(Calendar.MINUTE, 59)
            c.set(Calendar.SECOND, 59)
            c.set(Calendar.MILLISECOND, 999)

            val ms = c.timeInMillis
            val shortLbl = shortSdf.format(Date(ms))
            val fullLbl = fullSdf.format(Date(ms))
            val formattedShort = if (languageMode == LanguageMode.BANGLA) LanguageHelper.toBanglaDigits(shortLbl) else shortLbl
            val formattedFull = if (languageMode == LanguageMode.BANGLA) DateUtils.formatDate(ms, languageMode) else fullLbl
            periods.add(TimelinePeriod(ms, formattedFull, formattedShort))
        }
    }

    fun calculateTimeline(
        accounts: List<Account>,
        transactions: List<Transaction>,
        interval: AccountTimelineInterval = AccountTimelineInterval.MONTHLY,
        periodsCount: Int = 4,
        selectedAccountIds: Set<Long> = emptySet(),
        selectedStatusSet: Set<TransactionStatus> = emptySet(),
        showHiddenAccounts: Boolean = false,
        excludeZeroBalances: Boolean = false,
        languageMode: LanguageMode = LanguageMode.ENGLISH
    ): AccountTimelineData {
        val periods = generatePeriods(interval, periodsCount, languageMode)

        val validTransactions = if (selectedStatusSet.isNotEmpty()) {
            transactions.filter { it.status in selectedStatusSet }
        } else {
            transactions
        }

        // Calculate debits and credits for each period
        // periodIndex -> (accountId -> totalDr/totalCr)
        val debitsPerPeriod = List(periods.size) { mutableMapOf<Long, Double>() }
        val creditsPerPeriod = List(periods.size) { mutableMapOf<Long, Double>() }

        for (tx in validTransactions) {
            for (pIdx in periods.indices) {
                if (tx.dateEpochMs <= periods[pIdx].epochMs) {
                    tx.debitAccountId?.let {
                        debitsPerPeriod[pIdx][it] = (debitsPerPeriod[pIdx][it] ?: 0.0) + tx.amount
                    }
                    tx.creditAccountId?.let {
                        creditsPerPeriod[pIdx][it] = (creditsPerPeriod[pIdx][it] ?: 0.0) + tx.amount
                    }
                }
            }
        }

        fun getAccountBalanceAtPeriod(acc: Account, pIdx: Int): Double {
            val dr = debitsPerPeriod[pIdx][acc.id] ?: 0.0
            val cr = creditsPerPeriod[pIdx][acc.id] ?: 0.0
            return when (acc.type) {
                AccountType.ASSET, AccountType.EXPENSE -> acc.initialBalance + (dr - cr)
                AccountType.LIABILITY -> -(acc.initialBalance + (cr - dr))
                AccountType.EQUITY, AccountType.INCOME -> acc.initialBalance + (cr - dr)
            }
        }

        val filteredAccounts = accounts.filter { acc ->
            val matchesHidden = if (showHiddenAccounts) true else acc.isActive
            val matchesSelected = if (selectedAccountIds.isEmpty()) true else {
                selectedAccountIds.contains(acc.id) || (acc.parentId != null && selectedAccountIds.contains(acc.parentId))
            }
            matchesHidden && matchesSelected
        }

        val parentAccounts = filteredAccounts.filter { it.parentId == null }
        val subAccountsByParent = filteredAccounts.filter { it.parentId != null }.groupBy { it.parentId!! }

        fun buildTimelineGroups(type: AccountType): List<TimelineGroup> {
            val parents = parentAccounts.filter { it.type == type }
            val groups = mutableListOf<TimelineGroup>()

            for (parent in parents) {
                val subs = subAccountsByParent[parent.id] ?: emptyList()
                val subRows = subs.map { sub ->
                    val balances = periods.indices.map { pIdx -> getAccountBalanceAtPeriod(sub, pIdx) }
                    TimelineAccountRow(account = sub, balances = balances)
                }

                val parentBalances = periods.indices.map { pIdx -> getAccountBalanceAtPeriod(parent, pIdx) }
                val totalGroupBalances = periods.indices.map { pIdx ->
                    val sumSubs = subRows.sumOf { it.balances[pIdx] }
                    parentBalances[pIdx] + sumSubs
                }

                // If exclude zero balances, check if all period balances are 0
                if (excludeZeroBalances) {
                    val allZero = totalGroupBalances.all { Math.abs(it) < 0.001 } && subRows.all { it.balances.all { b -> Math.abs(b) < 0.001 } }
                    if (allZero) continue
                }

                groups.add(
                    TimelineGroup(
                        parentAccount = parent,
                        groupBalances = totalGroupBalances,
                        subAccounts = subRows
                    )
                )
            }
            return groups
        }

        val assetGroups = buildTimelineGroups(AccountType.ASSET)
        val liabilityGroups = buildTimelineGroups(AccountType.LIABILITY)

        val totalAssetsByPeriod = periods.indices.map { pIdx ->
            assetGroups.sumOf { it.groupBalances[pIdx] }
        }

        val totalLiabilitiesByPeriod = periods.indices.map { pIdx ->
            liabilityGroups.sumOf { Math.abs(it.groupBalances[pIdx]) }
        }

        val netWorthByPeriod = periods.indices.map { pIdx ->
            totalAssetsByPeriod[pIdx] - totalLiabilitiesByPeriod[pIdx]
        }

        return AccountTimelineData(
            interval = interval,
            periods = periods,
            assetGroups = assetGroups,
            liabilityGroups = liabilityGroups,
            totalAssetsByPeriod = totalAssetsByPeriod,
            totalLiabilitiesByPeriod = totalLiabilitiesByPeriod,
            netWorthByPeriod = netWorthByPeriod
        )
    }
}
