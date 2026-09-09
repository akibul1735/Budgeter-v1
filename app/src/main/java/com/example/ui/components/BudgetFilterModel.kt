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
 * Date range presets for Budget filtering.
 */
enum class BudgetDateRangePreset(val labelEn: String, val labelBn: String) {
    THIS_MONTH("This Month", "চলতি মাস"),
    LAST_MONTH("Last Month", "গত মাস"),
    LAST_3_MONTHS("Last 3 Months", "গত ৩ মাস"),
    LAST_6_MONTHS("Last 6 Months", "গত ৬ মাস"),
    YEAR_TO_DATE("Year to Date", "বছরের শুরু থেকে"),
    SAME_MONTH_LAST_YEAR("Same Month Last Year", "গত বছরের এই মাস"),
    ALL_TIME("All Time", "সব সময়"),
    CUSTOM("Custom Range", "নির্দিষ্ট সময়সীমা")
}

/**
 * Comparison baseline presets for Budget tracking.
 */
enum class BudgetComparisonPreset(val titleEn: String, val titleBn: String) {
    LAST_MONTH("Previous Month", "গত মাস"),
    SAME_MONTH_LAST_YEAR("Same Month Last Year", "গত বছরের একই মাস"),
    LAST_3_MONTHS_AVG("Last 3 Months", "গত ৩ মাস"),
    CUSTOM("Custom Baseline", "কাস্টম বেসলাইন")
}

/**
 * Sort options for Budget items and transactions.
 */
enum class BudgetSortOrder(val titleEn: String, val titleBn: String) {
    DEFAULT("Default Order", "ডিফল্ট ক্রম"),
    AMOUNT_DESC("Amount: High → Low", "পরিমাণ: বেশি → কম"),
    AMOUNT_ASC("Amount: Low → High", "পরিমাণ: কম → বেশি"),
    BUDGET_DESC("Budget Limit: High → Low", "বাজেট সীমা: বেশি → কম"),
    BUDGET_ASC("Budget Limit: Low → High", "বাজেট সীমা: কম → বেশি"),
    SPENT_DESC("Actual Spent: High → Low", "ব্যয়: বেশি → কম"),
    UTILIZATION_DESC("Utilization %: High → Low", "ব্যবহারের হার: বেশি → কম"),
    NAME_ASC("Alphabetical: A → Z", "নাম: A → Z")
}

/**
 * Comprehensive filter state for Budget tab and Budget Maker.
 */
data class BudgetFilterState(
    // 1. Date Range
    val datePreset: BudgetDateRangePreset = BudgetDateRangePreset.THIS_MONTH,
    val customStartDateMs: Long? = null,
    val customEndDateMs: Long? = null,

    // 2. Comparison
    val comparisonEnabled: Boolean = false,
    val comparisonPreset: BudgetComparisonPreset = BudgetComparisonPreset.LAST_MONTH,
    val customCompareStartMs: Long? = null,
    val customCompareEndMs: Long? = null,

    // 3. Category & Account Multi-select
    val selectedCategoryIds: Set<Long> = emptySet(),
    val selectedAccountIds: Set<Long> = emptySet(),

    // 4. Labels & Status Multi-select
    val selectedLabels: Set<String> = emptySet(),
    val selectedStatusSet: Set<TransactionStatus> = emptySet(),

    // 5. Exclude Zero Amounts & Hide Empty Groups
    val excludeZeroAmounts: Boolean = false,
    val hideEmptyGroups: Boolean = true,

    // 6. Display Currency & Symbol
    val displayCurrency: Boolean = true,
    val displayCurrencySymbol: Boolean = true,

    // 7. Sort Options & Category order
    val sortByAmount: Boolean = false,
    val showExpenseCategoriesFirst: Boolean = true,
    val sortOrder: BudgetSortOrder = BudgetSortOrder.DEFAULT,

    // 8. Other Useful Filters
    val filterOnlyBudgeted: Boolean = false,
    val filterOnlyOverBudget: Boolean = false,
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
                selectedCategoryIds.isNotEmpty() ||
                selectedAccountIds.isNotEmpty() ||
                selectedLabels.isNotEmpty() ||
                selectedStatusSet.isNotEmpty() ||
                excludeZeroAmounts ||
                !displayCurrency ||
                !displayCurrencySymbol ||
                sortByAmount ||
                !showExpenseCategoriesFirst ||
                sortOrder != BudgetSortOrder.DEFAULT ||
                filterOnlyBudgeted ||
                filterOnlyOverBudget ||
                minAmount != null ||
                maxAmount != null

    val activeFilterCount: Int
        get() {
            var count = 0
            if (datePreset != BudgetDateRangePreset.THIS_MONTH || customStartDateMs != null) count++
            if (comparisonEnabled) count++
            if (selectedCategoryIds.isNotEmpty()) count++
            if (selectedAccountIds.isNotEmpty()) count++
            if (selectedLabels.isNotEmpty()) count++
            if (selectedStatusSet.isNotEmpty()) count++
            if (excludeZeroAmounts) count++
            if (!displayCurrency || !displayCurrencySymbol) count++
            if (sortByAmount || !showExpenseCategoriesFirst || sortOrder != BudgetSortOrder.DEFAULT) count++
            if (filterOnlyBudgeted || filterOnlyOverBudget) count++
            if (minAmount != null || maxAmount != null) count++
            return count
        }
}

/**
 * Calculates date bounds and label descriptions for current and comparison ranges.
 */
fun calculateBudgetFilterRanges(
    year: Int,
    month: Int,
    filterState: BudgetFilterState,
    languageMode: LanguageMode
): BudgetRangeResult {
    val cal = Calendar.getInstance()
    val sdf = SimpleDateFormat("MMM yyyy", Locale.getDefault())
    val sdfShort = SimpleDateFormat("dd MMM", Locale.getDefault())

    // 1. Determine Primary Date Range
    val (primaryStartMs, primaryEndMs, primaryLabel) = when (filterState.datePreset) {
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
            val y = cal.get(Calendar.YEAR)
            val m = cal.get(Calendar.MONTH) + 1
            val start = DateUtils.getStartOfMonth(y, m)
            val end = DateUtils.getEndOfMonth(y, m)
            val label = DateUtils.formatMonthYear(y, m, languageMode)
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
            val label = if (languageMode == LanguageMode.BANGLA) "বিগত ৩ মাস" else "Last 3 Months"
            Triple(start, end, label)
        }
        BudgetDateRangePreset.LAST_6_MONTHS -> {
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month - 1)
            val end = DateUtils.getEndOfMonth(year, month)
            cal.add(Calendar.MONTH, -5)
            val startY = cal.get(Calendar.YEAR)
            val startM = cal.get(Calendar.MONTH) + 1
            val start = DateUtils.getStartOfMonth(startY, startM)
            val label = if (languageMode == LanguageMode.BANGLA) "বিগত ৬ মাস" else "Last 6 Months"
            Triple(start, end, label)
        }
        BudgetDateRangePreset.YEAR_TO_DATE -> {
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, Calendar.JANUARY)
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val start = cal.timeInMillis
            val end = DateUtils.getEndOfMonth(year, month)
            val label = if (languageMode == LanguageMode.BANGLA) "$year এর শুরু থেকে" else "YTD $year"
            Triple(start, end, label)
        }
        BudgetDateRangePreset.SAME_MONTH_LAST_YEAR -> {
            val prevYear = year - 1
            val start = DateUtils.getStartOfMonth(prevYear, month)
            val end = DateUtils.getEndOfMonth(prevYear, month)
            val label = DateUtils.formatMonthYear(prevYear, month, languageMode)
            Triple(start, end, label)
        }
        BudgetDateRangePreset.ALL_TIME -> {
            val label = if (languageMode == LanguageMode.BANGLA) "সব সময়" else "All Time"
            Triple(0L, Long.MAX_VALUE, label)
        }
        BudgetDateRangePreset.CUSTOM -> {
            val start = filterState.customStartDateMs ?: DateUtils.getStartOfMonth(year, month)
            val end = filterState.customEndDateMs ?: DateUtils.getEndOfMonth(year, month)
            val label = "${sdfShort.format(Date(start))} - ${sdfShort.format(Date(end))}"
            Triple(start, end, label)
        }
    }

    // 2. Determine Comparison Range (if comparison enabled)
    val comparisonResult = if (!filterState.comparisonEnabled) {
        null
    } else {
        when (filterState.comparisonPreset) {
            BudgetComparisonPreset.LAST_MONTH -> {
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, month - 1)
                cal.add(Calendar.MONTH, -1)
                val prevYear = cal.get(Calendar.YEAR)
                val prevMonth = cal.get(Calendar.MONTH) + 1
                val start = DateUtils.getStartOfMonth(prevYear, prevMonth)
                val end = DateUtils.getEndOfMonth(prevYear, prevMonth)
                val label = DateUtils.formatMonthYear(prevYear, prevMonth, languageMode)
                Pair(Pair(start, end), label)
            }
            BudgetComparisonPreset.SAME_MONTH_LAST_YEAR -> {
                val prevYear = year - 1
                val start = DateUtils.getStartOfMonth(prevYear, month)
                val end = DateUtils.getEndOfMonth(prevYear, month)
                val label = DateUtils.formatMonthYear(prevYear, month, languageMode)
                Pair(Pair(start, end), label)
            }
            BudgetComparisonPreset.LAST_3_MONTHS_AVG -> {
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, month - 1)
                cal.add(Calendar.MONTH, -3)
                val prevStart = DateUtils.getStartOfMonth(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
                cal.add(Calendar.MONTH, 2)
                val prevEnd = DateUtils.getEndOfMonth(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
                val label = if (languageMode == LanguageMode.BANGLA) "পূর্ববর্তী ৩ মাস" else "Prev 3 Months"
                Pair(Pair(prevStart, prevEnd), label)
            }
            BudgetComparisonPreset.CUSTOM -> {
                val start = filterState.customCompareStartMs ?: (primaryStartMs - (30L * 24 * 60 * 60 * 1000L))
                val end = filterState.customCompareEndMs ?: (primaryEndMs - (30L * 24 * 60 * 60 * 1000L))
                val label = "${sdfShort.format(Date(start))} - ${sdfShort.format(Date(end))}"
                Pair(Pair(start, end), label)
            }
        }
    }

    return BudgetRangeResult(
        primaryRange = Pair(primaryStartMs, primaryEndMs),
        primaryLabel = primaryLabel,
        compareRange = comparisonResult?.first,
        compareLabel = comparisonResult?.second ?: ""
    )
}

data class BudgetRangeResult(
    val primaryRange: Pair<Long, Long>,
    val primaryLabel: String,
    val compareRange: Pair<Long, Long>?,
    val compareLabel: String
)

/**
 * Format budget amounts according to active filter currency display preferences.
 */
fun formatBudgetAmount(
    amount: Double,
    filterState: BudgetFilterState,
    languageMode: LanguageMode,
    includeDecimals: Boolean = true
): String {
    if (!filterState.displayCurrency) {
        return LanguageHelper.formatNumber(amount, languageMode, includeDecimals)
    }
    val config = LanguageHelper.activeCurrencyConfig
    val displayMode = if (filterState.displayCurrencySymbol) {
        config.displayMode
    } else {
        com.example.util.CurrencyDisplayMode.CODE_ONLY
    }
    return LanguageHelper.formatCurrency(
        amount = amount,
        mode = languageMode,
        overrideDisplayMode = displayMode
    )
}
