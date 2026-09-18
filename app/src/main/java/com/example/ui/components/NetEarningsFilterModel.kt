package com.example.ui.components

import com.example.data.model.LanguageMode
import com.example.data.model.TransactionStatus
import com.example.util.DateUtils
import com.example.util.LanguageHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Cash flow scope filter specific to Net Earnings calculation.
 */
enum class NetEarningsFlowScope(val labelEn: String, val labelBn: String) {
    ALL("All Cash Flow (Income & Expense)", "উভয় (আয় ও ব্যয়)"),
    SURPLUS_ONLY("Surplus Only (Income > Expense)", "শুধু উদ্বৃত্ত (লাভ)"),
    DEFICIT_ONLY("Deficit Only (Expense > Income)", "শুধু ঘাটতি (ক্ষতি)"),
    INCOME_ONLY("Incomes Only", "শুধু আয়"),
    EXPENSE_ONLY("Expenses Only", "শুধু ব্যয়")
}

/**
 * Sort options specific to Net Earnings.
 */
enum class NetEarningsSortOrder(val titleEn: String, val titleBn: String) {
    DEFAULT("Default Order", "ডিফল্ট ক্রম"),
    NET_DESC("Net Surplus: High → Low", "নিট উদ্বৃত্ত: বেশি → কম"),
    NET_ASC("Net Deficit: Low → High", "নিট আয়: কম → বেশি"),
    AMOUNT_DESC("Total Flow: High → Low", "মোট লেনদেন: বেশি → কম"),
    AMOUNT_ASC("Total Flow: Low → High", "মোট লেনদেন: কম → বেশি"),
    NAME_ASC("Alphabetical: A → Z", "নাম: A → Z"),
    TXN_COUNT_DESC("Most Transactions", "সর্বাধিক লেনদেন")
}

/**
 * Filter state tailored specifically for the Net Earnings tab.
 */
data class NetEarningsFilterState(
    // 1. Date Range
    val datePreset: BudgetDateRangePreset = BudgetDateRangePreset.THIS_MONTH,
    val customStartDateMs: Long? = null,
    val customEndDateMs: Long? = null,

    // 2. Comparison
    val comparisonEnabled: Boolean = false,
    val comparisonPreset: BudgetComparisonPreset = BudgetComparisonPreset.LAST_MONTH,
    val customCompareStartMs: Long? = null,
    val customCompareEndMs: Long? = null,

    // 3. Flow & Net Earnings Specific Settings
    val flowScope: NetEarningsFlowScope = NetEarningsFlowScope.ALL,
    val includeTransfers: Boolean = false,

    // 4. Category & Account Multi-select
    val selectedCategoryIds: Set<Long> = emptySet(),
    val selectedAccountIds: Set<Long> = emptySet(),

    // 5. Labels & Status Multi-select
    val selectedLabels: Set<String> = emptySet(),
    val selectedStatusSet: Set<TransactionStatus> = emptySet(),

    // 6. Exclude Zero Amounts & Structure Controls
    val excludeZeroAmounts: Boolean = true,
    val hideEmptyGroups: Boolean = true,
    val showOnlyCategoriesWithoutGroups: Boolean = false,

    // 7. Display Settings
    val displayCurrency: Boolean = true,
    val displayCurrencySymbol: Boolean = true,

    // 8. Sort Options
    val sortOrder: NetEarningsSortOrder = NetEarningsSortOrder.NET_DESC,

    // 9. Threshold / Limits
    val minAmount: Double? = null,
    val maxAmount: Double? = null
) {
    val isFilterActive: Boolean
        get() = datePreset != BudgetDateRangePreset.THIS_MONTH ||
                customStartDateMs != null ||
                customEndDateMs != null ||
                comparisonEnabled ||
                customCompareStartMs != null ||
                customCompareEndMs != null ||
                flowScope != NetEarningsFlowScope.ALL ||
                includeTransfers ||
                selectedCategoryIds.isNotEmpty() ||
                selectedAccountIds.isNotEmpty() ||
                selectedLabels.isNotEmpty() ||
                selectedStatusSet.isNotEmpty() ||
                !excludeZeroAmounts ||
                showOnlyCategoriesWithoutGroups ||
                !displayCurrency ||
                !displayCurrencySymbol ||
                (sortOrder != NetEarningsSortOrder.DEFAULT && sortOrder != NetEarningsSortOrder.NET_DESC) ||
                minAmount != null ||
                maxAmount != null

    val activeFilterCount: Int
        get() {
            var count = 0
            if (datePreset != BudgetDateRangePreset.THIS_MONTH || customStartDateMs != null) count++
            if (comparisonEnabled) count++
            if (flowScope != NetEarningsFlowScope.ALL) count++
            if (includeTransfers) count++
            if (selectedCategoryIds.isNotEmpty()) count++
            if (selectedAccountIds.isNotEmpty()) count++
            if (selectedLabels.isNotEmpty()) count++
            if (selectedStatusSet.isNotEmpty()) count++
            if (!excludeZeroAmounts) count++
            if (showOnlyCategoriesWithoutGroups) count++
            if (!displayCurrency || !displayCurrencySymbol) count++
            if (sortOrder != NetEarningsSortOrder.DEFAULT && sortOrder != NetEarningsSortOrder.NET_DESC) count++
            if (minAmount != null || maxAmount != null) count++
            return count
        }
}

/**
 * Calculates date bounds and label descriptions for current and comparison ranges for Net Earnings.
 */
fun calculateNetEarningsFilterRanges(
    year: Int,
    month: Int,
    filterState: NetEarningsFilterState,
    languageMode: LanguageMode
): BudgetRangeResult {
    val cal = Calendar.getInstance()
    val sdf = SimpleDateFormat("MMM yyyy", Locale.getDefault())
    val sdfShort = SimpleDateFormat("dd MMM", Locale.getDefault())

    val (primaryStartMs, primaryEndMs, primaryLabel) = when (filterState.datePreset) {
        BudgetDateRangePreset.LAST_12_MONTHS -> {
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month - 1)
            val end = DateUtils.getEndOfMonth(year, month)
            cal.add(Calendar.MONTH, -11)
            val startY = cal.get(Calendar.YEAR)
            val startM = cal.get(Calendar.MONTH) + 1
            val start = DateUtils.getStartOfMonth(startY, startM)
            Triple(start, end, if (languageMode == LanguageMode.BANGLA) "গত ১২ মাস" else "Last 12 Months")
        }
        BudgetDateRangePreset.THIS_MONTH -> {
            val start = DateUtils.getStartOfMonth(year, month)
            val end = DateUtils.getEndOfMonth(year, month)
            val label = DateUtils.formatMonthYear(year, month, languageMode)
            Triple(start, end, label)
        }
        BudgetDateRangePreset.LAST_MONTH -> {
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month - 1)
            cal.add(Calendar.MONTH, -1)
            val prevY = cal.get(Calendar.YEAR)
            val prevM = cal.get(Calendar.MONTH) + 1
            val start = DateUtils.getStartOfMonth(prevY, prevM)
            val end = DateUtils.getEndOfMonth(prevY, prevM)
            val label = DateUtils.formatMonthYear(prevY, prevM, languageMode)
            Triple(start, end, label)
        }
        BudgetDateRangePreset.LAST_3_MONTHS -> {
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month - 1)
            val end = DateUtils.getEndOfMonth(year, month)
            cal.add(Calendar.MONTH, -2)
            val startY = cal.get(Calendar.YEAR)
            val startM = cal.get(Calendar.MONTH) + 1
            val start = DateUtils.getStartOfMonth(startY, startM)
            Triple(start, end, if (languageMode == LanguageMode.BANGLA) "গত ৩ মাস" else "Last 3 Months")
        }
        BudgetDateRangePreset.LAST_6_MONTHS -> {
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month - 1)
            val end = DateUtils.getEndOfMonth(year, month)
            cal.add(Calendar.MONTH, -5)
            val startY = cal.get(Calendar.YEAR)
            val startM = cal.get(Calendar.MONTH) + 1
            val start = DateUtils.getStartOfMonth(startY, startM)
            Triple(start, end, if (languageMode == LanguageMode.BANGLA) "গত ৬ মাস" else "Last 6 Months")
        }
        BudgetDateRangePreset.YEAR_TO_DATE -> {
            val start = DateUtils.getStartOfMonth(year, 1)
            val end = DateUtils.getEndOfMonth(year, month)
            Triple(start, end, if (languageMode == LanguageMode.BANGLA) "$year এর শুরু থেকে" else "YTD $year")
        }
        BudgetDateRangePreset.SAME_MONTH_LAST_YEAR -> {
            val prevY = year - 1
            val start = DateUtils.getStartOfMonth(prevY, month)
            val end = DateUtils.getEndOfMonth(prevY, month)
            val label = DateUtils.formatMonthYear(prevY, month, languageMode)
            Triple(start, end, label)
        }
        BudgetDateRangePreset.ALL_TIME -> {
            Triple(0L, Long.MAX_VALUE, if (languageMode == LanguageMode.BANGLA) "সব সময়" else "All Time")
        }
        BudgetDateRangePreset.CUSTOM -> {
            val s = filterState.customStartDateMs ?: DateUtils.getStartOfMonth(year, month)
            val e = filterState.customEndDateMs ?: DateUtils.getEndOfMonth(year, month)
            val lbl = "${sdfShort.format(Date(s))} - ${sdfShort.format(Date(e))}"
            Triple(s, e, lbl)
        }
    }

    var comparisonRange: Pair<Long, Long>? = null
    var comparisonLabel: String? = null

    if (filterState.comparisonEnabled) {
        val (compStart, compEnd, compLbl) = when (filterState.comparisonPreset) {
            BudgetComparisonPreset.SAME_DATE_PREV_MONTH -> {
                cal.timeInMillis = primaryStartMs
                cal.add(Calendar.MONTH, -1)
                val s = cal.timeInMillis
                cal.timeInMillis = primaryEndMs
                cal.add(Calendar.MONTH, -1)
                val e = cal.timeInMillis
                Triple(s, e, if (languageMode == LanguageMode.BANGLA) "পূর্ববর্তী মাস" else "Prev Month")
            }
            BudgetComparisonPreset.LAST_MONTH -> {
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, month - 1)
                cal.add(Calendar.MONTH, -1)
                val prevY = cal.get(Calendar.YEAR)
                val prevM = cal.get(Calendar.MONTH) + 1
                val s = DateUtils.getStartOfMonth(prevY, prevM)
                val e = DateUtils.getEndOfMonth(prevY, prevM)
                val lbl = DateUtils.formatMonthYear(prevY, prevM, languageMode)
                Triple(s, e, lbl)
            }
            BudgetComparisonPreset.SAME_DATE_PREV_YEAR,
            BudgetComparisonPreset.SAME_MONTH_LAST_YEAR -> {
                val prevY = year - 1
                val s = DateUtils.getStartOfMonth(prevY, month)
                val e = DateUtils.getEndOfMonth(prevY, month)
                val lbl = DateUtils.formatMonthYear(prevY, month, languageMode)
                Triple(s, e, lbl)
            }
            BudgetComparisonPreset.LAST_3_MONTHS_AVG -> {
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, month - 1)
                cal.add(Calendar.MONTH, -1)
                val end = DateUtils.getEndOfMonth(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
                cal.add(Calendar.MONTH, -2)
                val start = DateUtils.getStartOfMonth(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
                Triple(start, end, if (languageMode == LanguageMode.BANGLA) "পূর্ববর্তী ৩ মাস" else "Prev 3 Months")
            }
            BudgetComparisonPreset.LAST_YEAR -> {
                val prevY = year - 1
                val s = DateUtils.getStartOfMonth(prevY, 1)
                val e = DateUtils.getEndOfMonth(prevY, 12)
                Triple(s, e, if (languageMode == LanguageMode.BANGLA) "গত বছর ($prevY)" else "Last Year ($prevY)")
            }
            BudgetComparisonPreset.CUSTOM -> {
                val s = filterState.customCompareStartMs ?: primaryStartMs
                val e = filterState.customCompareEndMs ?: primaryEndMs
                val lbl = "${sdfShort.format(Date(s))} - ${sdfShort.format(Date(e))}"
                Triple(s, e, lbl)
            }
        }
        comparisonRange = Pair(compStart, compEnd)
        comparisonLabel = compLbl
    }

    return BudgetRangeResult(
        primaryRange = Pair(primaryStartMs, primaryEndMs),
        primaryLabel = primaryLabel,
        compareRange = comparisonRange,
        compareLabel = comparisonLabel ?: ""
    )
}
