package com.example.util

import com.example.data.model.Account
import com.example.data.model.AccountType
import com.example.data.model.Category
import com.example.data.model.LanguageMode
import com.example.data.model.TransactionType
import com.example.data.model.TransactionWithDetails
import java.util.Calendar

data class MonthlyFinancialPoint(
    val year: Int,
    val month: Int, // 0-based: 0 = Jan, 11 = Dec
    val monthNameEn: String,
    val monthNameBn: String,
    val monthShortEn: String,
    val monthShortBn: String,
    val startEpochMs: Long,
    val endEpochMs: Long,
    val income: Double = 0.0,
    val expense: Double = 0.0,
    val netEarnings: Double = 0.0,
    val cashInflow: Double = 0.0,
    val cashOutflow: Double = 0.0,
    val netCashFlow: Double = 0.0,
    val assets: Double = 0.0,
    val liabilities: Double = 0.0,
    val netWorth: Double = 0.0
) {
    fun getMonthLabel(languageMode: LanguageMode): String {
        return if (languageMode == LanguageMode.BANGLA) monthShortBn else monthShortEn
    }

    fun getMonthFullLabel(languageMode: LanguageMode): String {
        return if (languageMode == LanguageMode.BANGLA) monthNameBn else monthNameEn
    }
}

object DashboardChartUtils {

    private val banglaShortMonths = arrayOf(
        "জানু", "ফেব্রু", "মার্চ", "এপ্রিল", "মে", "জুন",
        "জুলাই", "আগস্ট", "সেপ্টে", "অক্টো", "নভে", "ডিসে"
    )

    private val englishShortMonths = arrayOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )

    private val banglaFullMonths = arrayOf(
        "জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন",
        "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর"
    )

    private val englishFullMonths = arrayOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    /**
     * Generates a chronological list of monthly points for the past [monthCount] months including current month.
     */
    fun computeMonthlyPoints(
        monthCount: Int,
        transactions: List<TransactionWithDetails>,
        allAccounts: List<Account>,
        allCategories: List<Category>,
        currentTotalAssets: Double,
        currentTotalLiabilities: Double,
        languageMode: LanguageMode
    ): List<MonthlyFinancialPoint> {
        val count = monthCount.coerceIn(2, 24)
        val cal = DateUtils.getCalendar()
        val nowMs = cal.timeInMillis

        // Build list of months from (count - 1) months ago up to current month
        val monthsList = mutableListOf<Pair<Int, Int>>() // Pair(year, month)
        val tempCal = DateUtils.getCalendar()
        for (i in (count - 1) downTo 0) {
            tempCal.timeInMillis = nowMs
            tempCal.add(Calendar.MONTH, -i)
            monthsList.add(Pair(tempCal.get(Calendar.YEAR), tempCal.get(Calendar.MONTH)))
        }

        val points = mutableListOf<MonthlyFinancialPoint>()

        for ((year, month) in monthsList) {
            val startCal = DateUtils.getCalendar().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startMs = startCal.timeInMillis

            val endCal = DateUtils.getCalendar().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, startCal.getActualMaximum(Calendar.DAY_OF_MONTH))
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }
            val endMs = endCal.timeInMillis

            // Filter transactions in this month
            val monthTx = transactions.filter {
                val t = it.transaction.dateEpochMs
                t in startMs..endMs
            }

            val income = monthTx.filter { it.transaction.type == TransactionType.INCOME }
                .sumOf { it.transaction.amount }
            val expense = monthTx.filter { it.transaction.type == TransactionType.EXPENSE }
                .sumOf { it.transaction.amount }
            val netEarnings = income - expense

            // Cash Flow
            val cashFlowSummary = CashFlowHelper.calculateCashFlow(
                allTransactions = transactions,
                allAccounts = allAccounts,
                allCategories = allCategories,
                startMs = startMs,
                endMs = endMs,
                languageMode = languageMode
            )

            // Net Worth as of this month's end
            // Transactions that happened after this month's end
            val futureTx = transactions.filter {
                it.transaction.dateEpochMs > endMs
            }

            var netAssetChangeAfter = 0.0
            var netLiabilityChangeAfter = 0.0

            for (txItem in futureTx) {
                val tx = txItem.transaction
                val debitAcc = txItem.debitAccount
                val creditAcc = txItem.creditAccount

                when (tx.type) {
                    TransactionType.INCOME -> {
                        if (creditAcc?.type == AccountType.ASSET) {
                            netAssetChangeAfter += tx.amount
                        } else if (creditAcc?.type == AccountType.LIABILITY) {
                            netLiabilityChangeAfter -= tx.amount
                        }
                    }
                    TransactionType.EXPENSE -> {
                        if (debitAcc?.type == AccountType.ASSET) {
                            netAssetChangeAfter -= tx.amount
                        } else if (debitAcc?.type == AccountType.LIABILITY) {
                            netLiabilityChangeAfter += tx.amount
                        }
                    }
                    TransactionType.TRANSFER -> {
                        if (debitAcc?.type == AccountType.ASSET) {
                            netAssetChangeAfter -= tx.amount
                        } else if (debitAcc?.type == AccountType.LIABILITY) {
                            netLiabilityChangeAfter += tx.amount
                        }

                        if (creditAcc?.type == AccountType.ASSET) {
                            netAssetChangeAfter += tx.amount
                        } else if (creditAcc?.type == AccountType.LIABILITY) {
                            netLiabilityChangeAfter -= tx.amount
                        }
                    }
                }
            }

            val historicalAssets = (currentTotalAssets - netAssetChangeAfter).coerceAtLeast(0.0)
            val historicalLiabilities = (currentTotalLiabilities - netLiabilityChangeAfter).coerceAtLeast(0.0)
            val historicalNetWorth = historicalAssets - historicalLiabilities

            val mShortEn = englishShortMonths.getOrElse(month) { "M${month + 1}" }
            val mShortBn = banglaShortMonths.getOrElse(month) { "মাস ${month + 1}" }
            val mFullEn = englishFullMonths.getOrElse(month) { "Month ${month + 1}" }
            val mFullBn = banglaFullMonths.getOrElse(month) { "মাস ${month + 1}" }

            points.add(
                MonthlyFinancialPoint(
                    year = year,
                    month = month,
                    monthNameEn = mFullEn,
                    monthNameBn = mFullBn,
                    monthShortEn = mShortEn,
                    monthShortBn = mShortBn,
                    startEpochMs = startMs,
                    endEpochMs = endMs,
                    income = income,
                    expense = expense,
                    netEarnings = netEarnings,
                    cashInflow = cashFlowSummary.totalInflow,
                    cashOutflow = cashFlowSummary.totalOutflow,
                    netCashFlow = cashFlowSummary.netCashFlow,
                    assets = historicalAssets,
                    liabilities = historicalLiabilities,
                    netWorth = historicalNetWorth
                )
            )
        }

        return points
    }

    /**
     * Computes nice rounded step values for Y-axis scaling.
     */
    fun computeNiceScale(maxValue: Double, stepsCount: Int = 4): List<Double> {
        val max = if (maxValue <= 0.0) 1000.0 else maxValue
        val rawStep = max / stepsCount
        val magnitude = Math.pow(10.0, Math.floor(Math.log10(rawStep)))
        val residual = rawStep / magnitude
        val niceStep = when {
            residual <= 1.0 -> 1.0 * magnitude
            residual <= 2.0 -> 2.0 * magnitude
            residual <= 2.5 -> 2.5 * magnitude
            residual <= 5.0 -> 5.0 * magnitude
            else -> 10.0 * magnitude
        }

        val scale = mutableListOf<Double>()
        var current = 0.0
        while (current <= max || scale.size <= stepsCount) {
            scale.add(current)
            current += niceStep
            if (scale.size > 8) break
        }
        return scale
    }

    fun formatCompactAmount(value: Double, languageMode: LanguageMode): String {
        val absVal = Math.abs(value)
        val formatted = when {
            absVal >= 10000000 -> String.format(java.util.Locale.US, "%.1fCr", value / 10000000.0)
            absVal >= 100000 -> String.format(java.util.Locale.US, "%.1fL", value / 100000.0)
            absVal >= 1000 -> {
                if (absVal >= 10000 && absVal % 1000 == 0.0) {
                    String.format(java.util.Locale.US, "%.0fk", value / 1000.0)
                } else {
                    String.format(java.util.Locale.US, "%,.0f", value)
                }
            }
            else -> String.format(java.util.Locale.US, "%,.0f", value)
        }
        return if (languageMode == LanguageMode.BANGLA) LanguageHelper.toBanglaDigits(formatted) else formatted
    }
}
