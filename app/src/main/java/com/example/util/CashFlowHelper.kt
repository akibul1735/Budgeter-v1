package com.example.util

import com.example.data.model.Account
import com.example.data.model.AccountType
import com.example.data.model.Category
import com.example.data.model.CategoryType
import com.example.data.model.LanguageMode
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.data.model.TransactionWithDetails
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class CashFlowPeriodPreset(val titleEn: String, val titleBn: String) {
    THIS_MONTH("This Month", "চলতি মাস"),
    LAST_MONTH("Last Month", "গত মাস"),
    LAST_3_MONTHS("Last 3 Months", "গত ৩ মাস"),
    THIS_YEAR("This Year", "চলতি বছর"),
    ALL_TIME("All Time", "সর্বকাল"),
    CUSTOM("Custom", "কাস্টম রেঞ্জ");

    fun getTitle(mode: LanguageMode): String = if (mode == LanguageMode.BANGLA) titleBn else titleEn
}

data class CashFlowCategoryItem(
    val categoryId: Long?,
    val nameEn: String,
    val nameBn: String,
    val iconName: String,
    val colorHex: String,
    val totalAmount: Double,
    val percentage: Double,
    val transactionCount: Int
) {
    fun localizedName(mode: LanguageMode): String = if (mode == LanguageMode.BANGLA && nameBn.isNotBlank()) nameBn else nameEn
}

data class CashFlowAccountItem(
    val account: Account,
    val openingBalance: Double,
    val totalInflow: Double,
    val totalOutflow: Double,
    val closingBalance: Double,
    val netChange: Double
)

data class CashFlowPeriodBar(
    val label: String,
    val startMs: Long,
    val endMs: Long,
    val inflow: Double,
    val outflow: Double,
    val net: Double
)

data class CashFlowDailyPoint(
    val dateEpochMs: Long,
    val dayLabel: String,
    val dailyInflow: Double,
    val dailyOutflow: Double,
    val endOfDayBalance: Double
)

data class CashFlowSummary(
    val startMs: Long,
    val endMs: Long,
    val openingBalance: Double,
    val totalInflow: Double,
    val totalOutflow: Double,
    val netCashFlow: Double,
    val closingBalance: Double,
    val operatingInflow: Double,
    val operatingOutflow: Double,
    val netOperatingFlow: Double,
    val financingInflow: Double,
    val financingOutflow: Double,
    val netFinancingFlow: Double,
    val dailyAverageBurnRate: Double,
    val dailyAverageInflowRate: Double,
    val runwayDays: Int,
    val runwayMonths: Double,
    val inflowCategories: List<CashFlowCategoryItem>,
    val outflowCategories: List<CashFlowCategoryItem>,
    val accountBreakdowns: List<CashFlowAccountItem>,
    val periodicBars: List<CashFlowPeriodBar>,
    val dailyPoints: List<CashFlowDailyPoint>,
    val transactions: List<TransactionWithDetails>
)

object CashFlowHelper {

    fun getDateRangeForPreset(preset: CashFlowPeriodPreset, customStartMs: Long = 0, customEndMs: Long = 0): Pair<Long, Long> {
        val cal = Calendar.getInstance(DateUtils.getActiveTimeZone())
        return when (preset) {
            CashFlowPeriodPreset.THIS_MONTH -> {
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
            CashFlowPeriodPreset.LAST_MONTH -> {
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
            CashFlowPeriodPreset.LAST_3_MONTHS -> {
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                val end = cal.timeInMillis

                cal.add(Calendar.MONTH, -3)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                Pair(start, end)
            }
            CashFlowPeriodPreset.THIS_YEAR -> {
                cal.set(Calendar.DAY_OF_YEAR, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis

                cal.set(Calendar.MONTH, Calendar.DECEMBER)
                cal.set(Calendar.DAY_OF_MONTH, 31)
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                val end = cal.timeInMillis
                Pair(start, end)
            }
            CashFlowPeriodPreset.ALL_TIME -> {
                Pair(0L, Long.MAX_VALUE)
            }
            CashFlowPeriodPreset.CUSTOM -> {
                val s = if (customStartMs > 0) customStartMs else 0L
                val e = if (customEndMs > 0) customEndMs else System.currentTimeMillis()
                Pair(s, e)
            }
        }
    }

    fun calculateCashFlow(
        allTransactions: List<TransactionWithDetails>,
        allAccounts: List<Account>,
        allCategories: List<Category>,
        startMs: Long,
        endMs: Long,
        languageMode: LanguageMode,
        selectedAccountIds: Set<Long>? = null
    ): CashFlowSummary {
        val targetAccounts = when {
            selectedAccountIds != null && selectedAccountIds.isNotEmpty() ->
                allAccounts.filter { selectedAccountIds.contains(it.id) }
            allAccounts.any { it.type == AccountType.ASSET } ->
                allAccounts.filter { it.type == AccountType.ASSET }
            else -> allAccounts
        }
        val targetAccountIds = targetAccounts.map { it.id }.toSet()

        // 1. Calculate opening balance for each target account before startMs
        val accountOpeningMap = mutableMapOf<Long, Double>()
        for (acc in targetAccounts) {
            accountOpeningMap[acc.id] = acc.initialBalance
        }

        val priorTransactions = allTransactions.filter { it.transaction.dateEpochMs < startMs }
        for (tw in priorTransactions) {
            val tx = tw.transaction
            when (tx.type) {
                TransactionType.INCOME -> {
                    val debitId = tx.debitAccountId
                    if (debitId != null && targetAccountIds.contains(debitId)) {
                        accountOpeningMap[debitId] = (accountOpeningMap[debitId] ?: 0.0) + tx.amount
                    }
                }
                TransactionType.EXPENSE -> {
                    val creditId = tx.creditAccountId
                    if (creditId != null && targetAccountIds.contains(creditId)) {
                        accountOpeningMap[creditId] = (accountOpeningMap[creditId] ?: 0.0) - tx.amount
                    }
                }
                TransactionType.TRANSFER -> {
                    val creditId = tx.creditAccountId
                    val debitId = tx.debitAccountId
                    if (creditId != null && targetAccountIds.contains(creditId)) {
                        accountOpeningMap[creditId] = (accountOpeningMap[creditId] ?: 0.0) - tx.amount
                    }
                    if (debitId != null && targetAccountIds.contains(debitId)) {
                        accountOpeningMap[debitId] = (accountOpeningMap[debitId] ?: 0.0) + tx.amount
                    }
                }
            }
        }

        val totalOpeningBalance = targetAccounts.sumOf { accountOpeningMap[it.id] ?: 0.0 }

        // 2. Filter transactions in the active period
        val periodTransactions = allTransactions
            .filter { it.transaction.dateEpochMs in startMs..endMs }
            .sortedBy { it.transaction.dateEpochMs }

        var operatingInflow = 0.0
        var operatingOutflow = 0.0
        var financingInflow = 0.0
        var financingOutflow = 0.0

        val accountInflowMap = mutableMapOf<Long, Double>()
        val accountOutflowMap = mutableMapOf<Long, Double>()

        val inflowCategoryMap = mutableMapOf<Long, Pair<Category?, Double>>()
        val outflowCategoryMap = mutableMapOf<Long, Pair<Category?, Double>>()
        val inflowCategoryCountMap = mutableMapOf<Long, Int>()
        val outflowCategoryCountMap = mutableMapOf<Long, Int>()

        val relevantTransactions = mutableListOf<TransactionWithDetails>()

        for (tw in periodTransactions) {
            val tx = tw.transaction
            val cat = tw.category
            val debitAcc = tw.debitAccount
            val creditAcc = tw.creditAccount

            var isRelevantToCash = false

            when (tx.type) {
                TransactionType.INCOME -> {
                    val isLiquidDebit = targetAccountIds.isEmpty() || tx.debitAccountId == null || targetAccountIds.contains(tx.debitAccountId)
                    if (isLiquidDebit) {
                        isRelevantToCash = true
                        if (tx.amount >= 0) {
                            operatingInflow += tx.amount
                            tx.debitAccountId?.let {
                                accountInflowMap[it] = (accountInflowMap[it] ?: 0.0) + tx.amount
                            }
                            val catId = tx.categoryId ?: 0L
                            val current = inflowCategoryMap[catId]?.second ?: 0.0
                            inflowCategoryMap[catId] = Pair(cat, current + tx.amount)
                            inflowCategoryCountMap[catId] = (inflowCategoryCountMap[catId] ?: 0) + 1
                        } else {
                            // Negative income treated as deduction
                            operatingOutflow += Math.abs(tx.amount)
                            tx.debitAccountId?.let {
                                accountOutflowMap[it] = (accountOutflowMap[it] ?: 0.0) + Math.abs(tx.amount)
                            }
                        }
                    }
                }
                TransactionType.EXPENSE -> {
                    val isLiquidCredit = targetAccountIds.isEmpty() || tx.creditAccountId == null || targetAccountIds.contains(tx.creditAccountId)
                    if (isLiquidCredit) {
                        isRelevantToCash = true
                        if (tx.amount >= 0) {
                            operatingOutflow += tx.amount
                            tx.creditAccountId?.let {
                                accountOutflowMap[it] = (accountOutflowMap[it] ?: 0.0) + tx.amount
                            }
                            val catId = tx.categoryId ?: 0L
                            val current = outflowCategoryMap[catId]?.second ?: 0.0
                            outflowCategoryMap[catId] = Pair(cat, current + tx.amount)
                            outflowCategoryCountMap[catId] = (outflowCategoryCountMap[catId] ?: 0) + 1
                        } else {
                            // Negative expense treated as refund / inflow
                            operatingInflow += Math.abs(tx.amount)
                            tx.creditAccountId?.let {
                                accountInflowMap[it] = (accountInflowMap[it] ?: 0.0) + Math.abs(tx.amount)
                            }
                        }
                    }
                }
                TransactionType.TRANSFER -> {
                    val isCreditAsset = tx.creditAccountId != null && (targetAccountIds.isEmpty() || targetAccountIds.contains(tx.creditAccountId))
                    val isDebitAsset = tx.debitAccountId != null && (targetAccountIds.isEmpty() || targetAccountIds.contains(tx.debitAccountId))

                    if (isCreditAsset && isDebitAsset) {
                        // Internal cash transfer between two liquid accounts
                        isRelevantToCash = true
                        tx.creditAccountId?.let {
                            accountOutflowMap[it] = (accountOutflowMap[it] ?: 0.0) + tx.amount
                        }
                        tx.debitAccountId?.let {
                            accountInflowMap[it] = (accountInflowMap[it] ?: 0.0) + tx.amount
                        }
                    } else if (isCreditAsset && !isDebitAsset) {
                        // Funds leaving liquid asset to liability or external (Financing Outflow / Debt pay / Loan given)
                        isRelevantToCash = true
                        financingOutflow += tx.amount
                        tx.creditAccountId?.let {
                            accountOutflowMap[it] = (accountOutflowMap[it] ?: 0.0) + tx.amount
                        }
                    } else if (!isCreditAsset && isDebitAsset) {
                        // Funds entering liquid asset from liability / loan receipt (Financing Inflow)
                        isRelevantToCash = true
                        financingInflow += tx.amount
                        tx.debitAccountId?.let {
                            accountInflowMap[it] = (accountInflowMap[it] ?: 0.0) + tx.amount
                        }
                    }
                }
            }

            if (isRelevantToCash) {
                relevantTransactions.add(tw)
            }
        }

        val totalInflow = operatingInflow + financingInflow
        val totalOutflow = operatingOutflow + financingOutflow
        val netCashFlow = totalInflow - totalOutflow
        val closingBalance = totalOpeningBalance + netCashFlow
        val netOperatingFlow = operatingInflow - operatingOutflow
        val netFinancingFlow = financingInflow - financingOutflow

        // 3. Category Breakdown lists
        val inflowCategories = inflowCategoryMap.map { (catId, pair) ->
            val cat = pair.first
            val amt = pair.second
            val pct = if (totalInflow > 0) (amt / totalInflow) * 100.0 else 0.0
            CashFlowCategoryItem(
                categoryId = if (catId != 0L) catId else null,
                nameEn = cat?.nameEn ?: if (catId == 0L) "Uncategorized Income" else "Other",
                nameBn = cat?.nameBn ?: if (catId == 0L) "অনির্ধারিত আয়" else "অন্যান্য",
                iconName = cat?.iconName ?: "Payments",
                colorHex = cat?.colorHex ?: "#4CAF50",
                totalAmount = amt,
                percentage = pct,
                transactionCount = inflowCategoryCountMap[catId] ?: 0
            )
        }.sortedByDescending { it.totalAmount }

        val outflowCategories = outflowCategoryMap.map { (catId, pair) ->
            val cat = pair.first
            val amt = pair.second
            val pct = if (totalOutflow > 0) (amt / totalOutflow) * 100.0 else 0.0
            CashFlowCategoryItem(
                categoryId = if (catId != 0L) catId else null,
                nameEn = cat?.nameEn ?: if (catId == 0L) "Uncategorized Expense" else "Other",
                nameBn = cat?.nameBn ?: if (catId == 0L) "অনির্ধারিত ব্যয়" else "অন্যান্য",
                iconName = cat?.iconName ?: "ShoppingBag",
                colorHex = cat?.colorHex ?: "#F44336",
                totalAmount = amt,
                percentage = pct,
                transactionCount = outflowCategoryCountMap[catId] ?: 0
            )
        }.sortedByDescending { it.totalAmount }

        // 4. Account Breakdown list
        val accountBreakdowns = targetAccounts.map { acc ->
            val op = accountOpeningMap[acc.id] ?: 0.0
            val inf = accountInflowMap[acc.id] ?: 0.0
            val out = accountOutflowMap[acc.id] ?: 0.0
            val cl = op + inf - out
            CashFlowAccountItem(
                account = acc,
                openingBalance = op,
                totalInflow = inf,
                totalOutflow = out,
                closingBalance = cl,
                netChange = inf - out
            )
        }.sortedByDescending { it.closingBalance }

        // 5. Burn rate & Runway calculation
        val daysInPeriod = Math.max(1, ((Math.min(endMs, System.currentTimeMillis()) - startMs) / (1000L * 60 * 60 * 24)).toInt() + 1)
        val dailyAverageBurnRate = if (daysInPeriod > 0) totalOutflow / daysInPeriod else 0.0
        val dailyAverageInflowRate = if (daysInPeriod > 0) totalInflow / daysInPeriod else 0.0

        val runwayDays = if (dailyAverageBurnRate > 0 && closingBalance > 0) {
            (closingBalance / dailyAverageBurnRate).toInt()
        } else if (closingBalance > 0 && totalOutflow == 0.0) {
            999
        } else {
            0
        }
        val runwayMonths = runwayDays / 30.0

        // 6. Periodic Bars & Trajectory Points
        val periodicBars = buildPeriodicBars(relevantTransactions, targetAccountIds, startMs, endMs, languageMode)
        val dailyPoints = buildDailyPoints(relevantTransactions, targetAccountIds, totalOpeningBalance, startMs, endMs, languageMode)

        return CashFlowSummary(
            startMs = startMs,
            endMs = endMs,
            openingBalance = totalOpeningBalance,
            totalInflow = totalInflow,
            totalOutflow = totalOutflow,
            netCashFlow = netCashFlow,
            closingBalance = closingBalance,
            operatingInflow = operatingInflow,
            operatingOutflow = operatingOutflow,
            netOperatingFlow = netOperatingFlow,
            financingInflow = financingInflow,
            financingOutflow = financingOutflow,
            netFinancingFlow = netFinancingFlow,
            dailyAverageBurnRate = dailyAverageBurnRate,
            dailyAverageInflowRate = dailyAverageInflowRate,
            runwayDays = runwayDays,
            runwayMonths = runwayMonths,
            inflowCategories = inflowCategories,
            outflowCategories = outflowCategories,
            accountBreakdowns = accountBreakdowns,
            periodicBars = periodicBars,
            dailyPoints = dailyPoints,
            transactions = relevantTransactions.sortedByDescending { it.transaction.dateEpochMs }
        )
    }

    private fun buildPeriodicBars(
        transactions: List<TransactionWithDetails>,
        assetAccountIds: Set<Long>,
        startMs: Long,
        endMs: Long,
        languageMode: LanguageMode
    ): List<CashFlowPeriodBar> {
        val spanDays = ((endMs - startMs) / (1000L * 60 * 60 * 24)).toInt()
        val cal = Calendar.getInstance(DateUtils.getActiveTimeZone())

        val bars = mutableListOf<CashFlowPeriodBar>()

        if (spanDays <= 35) {
            // Group by Week or 5-day intervals
            val intervalMs = 7L * 24 * 60 * 60 * 1000L
            var curStart = startMs
            var weekIdx = 1
            while (curStart < endMs && weekIdx <= 6) {
                val curEnd = Math.min(curStart + intervalMs - 1, endMs)
                var inf = 0.0
                var out = 0.0

                for (tw in transactions) {
                    val tx = tw.transaction
                    if (tx.dateEpochMs in curStart..curEnd) {
                        when (tx.type) {
                            TransactionType.INCOME -> if (tx.amount >= 0) inf += tx.amount else out += Math.abs(tx.amount)
                            TransactionType.EXPENSE -> if (tx.amount >= 0) out += tx.amount else inf += Math.abs(tx.amount)
                            TransactionType.TRANSFER -> {
                                val isCreditAsset = tx.creditAccountId != null && assetAccountIds.contains(tx.creditAccountId)
                                val isDebitAsset = tx.debitAccountId != null && assetAccountIds.contains(tx.debitAccountId)
                                if (isCreditAsset && !isDebitAsset) out += tx.amount
                                if (!isCreditAsset && isDebitAsset) inf += tx.amount
                            }
                        }
                    }
                }

                val label = if (languageMode == LanguageMode.BANGLA) "সপ্তাহ $weekIdx" else "Wk $weekIdx"
                bars.add(CashFlowPeriodBar(label, curStart, curEnd, inf, out, inf - out))
                curStart = curEnd + 1
                weekIdx++
            }
        } else {
            // Group by Month
            cal.timeInMillis = startMs
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)

            val shortMonthsEn = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
            val shortMonthsBn = arrayOf("জানু", "ফেব্রু", "মার্চ", "এপ্রিল", "মে", "জুন", "জুলাই", "আগস্ট", "সেপ্টে", "অক্টো", "নভে", "ডিসে")

            while (cal.timeInMillis < endMs) {
                val mStart = Math.max(cal.timeInMillis, startMs)
                val mMonth = cal.get(Calendar.MONTH)
                val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                cal.set(Calendar.DAY_OF_MONTH, maxDay)
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                val mEnd = Math.min(cal.timeInMillis, endMs)

                var inf = 0.0
                var out = 0.0

                for (tw in transactions) {
                    val tx = tw.transaction
                    if (tx.dateEpochMs in mStart..mEnd) {
                        when (tx.type) {
                            TransactionType.INCOME -> if (tx.amount >= 0) inf += tx.amount else out += Math.abs(tx.amount)
                            TransactionType.EXPENSE -> if (tx.amount >= 0) out += tx.amount else inf += Math.abs(tx.amount)
                            TransactionType.TRANSFER -> {
                                val isCreditAsset = tx.creditAccountId != null && assetAccountIds.contains(tx.creditAccountId)
                                val isDebitAsset = tx.debitAccountId != null && assetAccountIds.contains(tx.debitAccountId)
                                if (isCreditAsset && !isDebitAsset) out += tx.amount
                                if (!isCreditAsset && isDebitAsset) inf += tx.amount
                            }
                        }
                    }
                }

                val label = if (languageMode == LanguageMode.BANGLA) shortMonthsBn[mMonth] else shortMonthsEn[mMonth]
                bars.add(CashFlowPeriodBar(label, mStart, mEnd, inf, out, inf - out))

                cal.add(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
            }
        }

        return bars
    }

    private fun buildDailyPoints(
        transactions: List<TransactionWithDetails>,
        assetAccountIds: Set<Long>,
        openingBalance: Double,
        startMs: Long,
        endMs: Long,
        languageMode: LanguageMode
    ): List<CashFlowDailyPoint> {
        val points = mutableListOf<CashFlowDailyPoint>()
        val cal = Calendar.getInstance(DateUtils.getActiveTimeZone())
        val effectiveEndMs = Math.min(endMs, System.currentTimeMillis())

        cal.timeInMillis = startMs
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        var runningBalance = openingBalance
        val sdf = SimpleDateFormat("dd MMM", Locale.US)

        while (cal.timeInMillis <= effectiveEndMs) {
            val dayStart = cal.timeInMillis
            cal.set(Calendar.HOUR_OF_DAY, 23)
            cal.set(Calendar.MINUTE, 59)
            cal.set(Calendar.SECOND, 59)
            cal.set(Calendar.MILLISECOND, 999)
            val dayEnd = cal.timeInMillis

            var dayInflow = 0.0
            var dayOutflow = 0.0

            for (tw in transactions) {
                val tx = tw.transaction
                if (tx.dateEpochMs in dayStart..dayEnd) {
                    when (tx.type) {
                        TransactionType.INCOME -> if (tx.amount >= 0) dayInflow += tx.amount else dayOutflow += Math.abs(tx.amount)
                        TransactionType.EXPENSE -> if (tx.amount >= 0) dayOutflow += tx.amount else dayInflow += Math.abs(tx.amount)
                        TransactionType.TRANSFER -> {
                            val isCreditAsset = tx.creditAccountId != null && assetAccountIds.contains(tx.creditAccountId)
                            val isDebitAsset = tx.debitAccountId != null && assetAccountIds.contains(tx.debitAccountId)
                            if (isCreditAsset && !isDebitAsset) dayOutflow += tx.amount
                            if (!isCreditAsset && isDebitAsset) dayInflow += tx.amount
                        }
                    }
                }
            }

            runningBalance += (dayInflow - dayOutflow)

            val rawLabel = sdf.format(Date(dayStart))
            val dayLabel = if (languageMode == LanguageMode.BANGLA) LanguageHelper.toBanglaDigits(rawLabel) else rawLabel

            points.add(
                CashFlowDailyPoint(
                    dateEpochMs = dayStart,
                    dayLabel = dayLabel,
                    dailyInflow = dayInflow,
                    dailyOutflow = dayOutflow,
                    endOfDayBalance = runningBalance
                )
            )

            cal.add(Calendar.DAY_OF_MONTH, 1)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
        }

        return points
    }
}
