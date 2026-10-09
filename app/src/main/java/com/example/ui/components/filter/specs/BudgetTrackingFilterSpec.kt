package com.example.ui.components.filter.specs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import com.example.data.model.Account
import com.example.data.model.Category
import com.example.data.model.LanguageMode
import com.example.data.model.TransactionStatus
import com.example.ui.components.BudgetDateRangePreset
import com.example.ui.components.BudgetComparisonPreset
import com.example.ui.components.BudgetFilterState
import com.example.ui.components.BudgetRangeResult
import com.example.ui.components.BudgetSortOrder
import com.example.ui.components.filter.DatePresetOption
import com.example.ui.components.filter.FilterCondition
import com.example.ui.components.filter.FilterField
import com.example.ui.components.filter.FilterSpec
import com.example.ui.components.filter.FilterState
import com.example.ui.components.filter.FilterValue
import com.example.ui.components.filter.SelectItemOption
import com.example.ui.components.filter.SortOptionItem
import com.example.ui.screens.CategoryBudgetTrackingItem
import com.example.util.DateUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Filter Specification for the Budget Tracking screen.
 * Defines date range presets, baseline comparison modes, flow scope (Expense/Income),
 * hierarchy display, categories/accounts/labels/statuses multi-selection, amount ranges,
 * sort orders, and display/calculation toggles.
 */
object BudgetTrackingFilterSpec {

    const val SPEC_KEY = "budget_tracking"

    // Field IDs
    const val FIELD_SEARCH = "search"
    const val FIELD_DATE = "date"
    const val FIELD_COMPARISON = "comparison"
    const val FIELD_FLOW_SCOPE = "flow_scope"
    const val FIELD_HIERARCHY = "hierarchy"
    const val FIELD_CATEGORIES = "categories"
    const val FIELD_ACCOUNTS = "accounts"
    const val FIELD_LABELS = "labels"
    const val FIELD_STATUSES = "statuses"
    const val FIELD_AMOUNT_RANGE = "amount_range"
    const val FIELD_SORT = "sort"
    const val FIELD_TOGGLES = "toggles"

    // Date Range Presets (Ids)
    const val PRESET_THIS_MONTH = "this_month"
    const val PRESET_LAST_MONTH = "last_month"
    const val PRESET_LAST_3_MONTHS = "last_3_months"
    const val PRESET_LAST_6_MONTHS = "last_6_months"
    const val PRESET_LAST_12_MONTHS = "last_12_months"
    const val PRESET_YEAR_TO_DATE = "year_to_date"
    const val PRESET_SAME_MONTH_LAST_YEAR = "same_month_last_year"
    const val PRESET_ALL_TIME = "all_time"
    const val PRESET_CUSTOM = "custom"

    val DATE_PRESET_OPTIONS = listOf(
        DatePresetOption(PRESET_THIS_MONTH, "This Month", "চলতি মাস") {
            val cal = Calendar.getInstance()
            val y = cal.get(Calendar.YEAR)
            val m = cal.get(Calendar.MONTH) + 1
            Pair(DateUtils.getStartOfMonth(y, m), DateUtils.getEndOfMonth(y, m))
        },
        DatePresetOption(PRESET_LAST_MONTH, "Last Month", "গত মাস") {
            val cal = Calendar.getInstance()
            cal.add(Calendar.MONTH, -1)
            val y = cal.get(Calendar.YEAR)
            val m = cal.get(Calendar.MONTH) + 1
            Pair(DateUtils.getStartOfMonth(y, m), DateUtils.getEndOfMonth(y, m))
        },
        DatePresetOption(PRESET_LAST_3_MONTHS, "Last 3 Months", "বিগত ৩ মাস") {
            val cal = Calendar.getInstance()
            val end = DateUtils.getEndOfMonth(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
            cal.add(Calendar.MONTH, -2)
            val start = DateUtils.getStartOfMonth(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
            Pair(start, end)
        },
        DatePresetOption(PRESET_LAST_6_MONTHS, "Last 6 Months", "বিগত ৬ মাস") {
            val cal = Calendar.getInstance()
            val end = DateUtils.getEndOfMonth(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
            cal.add(Calendar.MONTH, -5)
            val start = DateUtils.getStartOfMonth(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
            Pair(start, end)
        },
        DatePresetOption(PRESET_LAST_12_MONTHS, "Last 12 Months", "বিগত ১২ মাস") {
            val cal = Calendar.getInstance()
            val end = DateUtils.getEndOfMonth(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
            cal.add(Calendar.MONTH, -11)
            val start = DateUtils.getStartOfMonth(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
            Pair(start, end)
        },
        DatePresetOption(PRESET_YEAR_TO_DATE, "Year to Date", "বছরের শুরু থেকে") {
            val cal = Calendar.getInstance()
            val y = cal.get(Calendar.YEAR)
            val m = cal.get(Calendar.MONTH) + 1
            Pair(DateUtils.getStartOfMonth(y, 1), DateUtils.getEndOfMonth(y, m))
        },
        DatePresetOption(PRESET_SAME_MONTH_LAST_YEAR, "Same Month Last Year", "গত বছরের এই মাস") {
            val cal = Calendar.getInstance()
            val y = cal.get(Calendar.YEAR) - 1
            val m = cal.get(Calendar.MONTH) + 1
            Pair(DateUtils.getStartOfMonth(y, m), DateUtils.getEndOfMonth(y, m))
        },
        DatePresetOption(PRESET_ALL_TIME, "All Time", "সব সময়") {
            Pair(0L, Long.MAX_VALUE)
        },
        DatePresetOption(PRESET_CUSTOM, "Custom Date Range", "নির্দিষ্ট সময়সীমা") {
            Pair(0L, 0L)
        }
    )

    // Comparison Presets
    const val COMP_NONE = "comp_none"
    const val COMP_SAME_DATE_PREV_MONTH = "comp_same_date_prev_month"
    const val COMP_SAME_DATE_PREV_YEAR = "comp_same_date_prev_year"
    const val COMP_LAST_MONTH = "comp_last_month"
    const val COMP_SAME_MONTH_LAST_YEAR = "comp_same_month_last_year"
    const val COMP_LAST_3_MONTHS_AVG = "comp_last_3_months_avg"
    const val COMP_LAST_YEAR = "comp_last_year"
    const val COMP_CUSTOM = "comp_custom"

    val COMPARISON_CONDITIONS: List<FilterCondition<CategoryBudgetTrackingItem>> = listOf(
        FilterCondition(COMP_NONE, "No Comparison", "তুলনাহীন", icon = Icons.Default.CompareArrows, group = "comp"),
        FilterCondition(COMP_SAME_DATE_PREV_MONTH, "Same Date Prev Month", "পূর্ববর্তী মাসের একই তারিখ", icon = Icons.Default.CompareArrows, group = "comp"),
        FilterCondition(COMP_SAME_DATE_PREV_YEAR, "Same Date Prev Year", "গত বছরের একই তারিখ", icon = Icons.Default.CompareArrows, group = "comp"),
        FilterCondition(COMP_LAST_MONTH, "Previous Full Month", "গত পুরো মাস", icon = Icons.Default.CompareArrows, group = "comp"),
        FilterCondition(COMP_SAME_MONTH_LAST_YEAR, "Same Month Last Year", "গত বছরের এই মাস", icon = Icons.Default.CompareArrows, group = "comp"),
        FilterCondition(COMP_LAST_3_MONTHS_AVG, "Previous 3 Months", "পূর্ববর্তী ৩ মাস", icon = Icons.Default.CompareArrows, group = "comp"),
        FilterCondition(COMP_LAST_YEAR, "Previous Full Year", "গত পুরো বছর", icon = Icons.Default.CompareArrows, group = "comp"),
        FilterCondition(COMP_CUSTOM, "Custom Date Range", "নির্দিষ্ট সময়সীমা", icon = Icons.Default.CompareArrows, group = "comp")
    )

    // Flow Scope
    const val SCOPE_ALL = "scope_all"
    const val SCOPE_EXPENSE_ONLY = "scope_expense_only"
    const val SCOPE_INCOME_ONLY = "scope_income_only"

    val FLOW_SCOPE_CONDITIONS: List<FilterCondition<CategoryBudgetTrackingItem>> = listOf(
        FilterCondition(SCOPE_ALL, "All (Expense & Income)", "উভয় (ব্যয় ও আয়)", icon = Icons.Default.FilterAlt, group = "flow"),
        FilterCondition(SCOPE_EXPENSE_ONLY, "Expense Budgets", "ব্যয় বাজেট", icon = Icons.Default.FilterAlt, group = "flow"),
        FilterCondition(SCOPE_INCOME_ONLY, "Income Budgets", "আয় বাজেট", icon = Icons.Default.FilterAlt, group = "flow")
    )

    // Hierarchy View
    const val HIERARCHY_GROUPED = "hierarchy_grouped"
    const val HIERARCHY_ONLY_GROUPS = "hierarchy_only_groups"
    const val HIERARCHY_WITHOUT_GROUPS = "hierarchy_without_groups"

    val HIERARCHY_CONDITIONS: List<FilterCondition<CategoryBudgetTrackingItem>> = listOf(
        FilterCondition(HIERARCHY_GROUPED, "Grouped Hierarchy", "গ্রুপ ও ক্যাটাগরি", icon = Icons.Default.Tune, group = "hierarchy"),
        FilterCondition(HIERARCHY_ONLY_GROUPS, "Only Parent Groups", "শুধু প্রধান গ্রুপ", icon = Icons.Default.Tune, group = "hierarchy"),
        FilterCondition(HIERARCHY_WITHOUT_GROUPS, "Flat List (Without Groups)", "গ্রুপ ছাড়া তালিকা", icon = Icons.Default.Tune, group = "hierarchy")
    )

    // Verification Statuses
    val STATUS_OPTIONS = listOf(
        SelectItemOption(TransactionStatus.CLEARED.name, "Cleared", "নিষ্পন্ন", icon = Icons.Default.CheckCircle),
        SelectItemOption(TransactionStatus.RECONCILED.name, "Reconciled", "মিলিত", icon = Icons.Default.CheckCircle),
        SelectItemOption(TransactionStatus.VOID.name, "Void", "বাতিল", icon = Icons.Default.CheckCircle)
    )

    // Sort Options
    const val SORT_DEFAULT = "default"
    const val SORT_AMOUNT_DESC = "amount_desc"
    const val SORT_AMOUNT_ASC = "amount_asc"
    const val SORT_SPENT_DESC = "spent_desc"
    const val SORT_REMAINING_DESC = "remaining_desc"
    const val SORT_REMAINING_ASC = "remaining_asc"
    const val SORT_BUDGET_DESC = "budget_desc"
    const val SORT_BUDGET_ASC = "budget_asc"
    const val SORT_UTILIZATION_DESC = "utilization_desc"
    const val SORT_NAME_ASC = "name_asc"
    const val SORT_NAME_DESC = "name_desc"

    val SORT_OPTIONS: List<SortOptionItem<CategoryBudgetTrackingItem>> = listOf(
        SortOptionItem(SORT_DEFAULT, "Default Order", "ডিফল্ট ক্রম", comparator = null, icon = Icons.AutoMirrored.Filled.Sort),
        SortOptionItem(SORT_AMOUNT_DESC, "Amount: High → Low", "পরিমাণ: বেশি → কম", comparator = null, icon = Icons.AutoMirrored.Filled.Sort),
        SortOptionItem(SORT_AMOUNT_ASC, "Amount: Low → High", "পরিমাণ: কম → বেশি", comparator = null, icon = Icons.AutoMirrored.Filled.Sort),
        SortOptionItem(SORT_SPENT_DESC, "Actual Spent: High → Low", "প্রকৃত ব্যয়: বেশি → কম", comparator = null, icon = Icons.AutoMirrored.Filled.Sort),
        SortOptionItem(SORT_REMAINING_DESC, "Remaining: High → Low", "অবশিষ্ট: বেশি → কম", comparator = null, icon = Icons.AutoMirrored.Filled.Sort),
        SortOptionItem(SORT_REMAINING_ASC, "Remaining: Low → High", "অবশিষ্ট: কম → বেশি", comparator = null, icon = Icons.AutoMirrored.Filled.Sort),
        SortOptionItem(SORT_BUDGET_DESC, "Budget Limit: High → Low", "বাজেট সীমা: বেশি → কম", comparator = null, icon = Icons.AutoMirrored.Filled.Sort),
        SortOptionItem(SORT_BUDGET_ASC, "Budget Limit: Low → High", "বাজেট সীমা: কম → বেশি", comparator = null, icon = Icons.AutoMirrored.Filled.Sort),
        SortOptionItem(SORT_UTILIZATION_DESC, "Utilization %: High → Low", "ব্যবহারের হার: বেশি → কম", comparator = null, icon = Icons.AutoMirrored.Filled.Sort),
        SortOptionItem(SORT_NAME_ASC, "Alphabetical: A → Z", "নাম: A → Z", comparator = null, icon = Icons.AutoMirrored.Filled.Sort),
        SortOptionItem(SORT_NAME_DESC, "Alphabetical: Z → A", "নাম: Z → A", comparator = null, icon = Icons.AutoMirrored.Filled.Sort)
    )

    // Toggles
    const val TOGGLE_EXCLUDE_ZERO = "exclude_zero"
    const val TOGGLE_HIDE_EMPTY_GROUPS = "hide_empty_groups"
    const val TOGGLE_ONLY_BUDGETED = "only_budgeted"
    const val TOGGLE_ONLY_OVER_BUDGET = "only_over_budget"
    const val TOGGLE_ONLY_REMAINING = "only_remaining"
    const val TOGGLE_ONLY_ACTUAL = "only_actual"
    const val TOGGLE_ACTIVE_3_MONTHS = "active_3_months"
    const val TOGGLE_EXPENSE_FIRST = "expense_first"
    const val TOGGLE_DISPLAY_CURRENCY = "display_currency"
    const val TOGGLE_DISPLAY_SYMBOL = "display_symbol"

    val TOGGLE_CONDITIONS: List<FilterCondition<CategoryBudgetTrackingItem>> = listOf(
        FilterCondition(TOGGLE_EXCLUDE_ZERO, "Exclude Zero Amounts", "শূন্য পরিমাণ বাদ দিন", icon = Icons.Default.Tune),
        FilterCondition(TOGGLE_HIDE_EMPTY_GROUPS, "Hide Empty Groups", "খালি গ্রুপ লুকান", icon = Icons.Default.Tune),
        FilterCondition(TOGGLE_ONLY_BUDGETED, "Only Budgeted Categories", "শুধু বাজেটযুক্ত ক্যাটাগরি", icon = Icons.Default.Tune),
        FilterCondition(TOGGLE_ONLY_OVER_BUDGET, "Only Over Budget (Exceeded)", "শুধু বাজেট অতিক্রান্ত", icon = Icons.Default.Tune),
        FilterCondition(TOGGLE_ONLY_REMAINING, "Only Categories with Remaining", "শুধু অবশিষ্ট ব্যালেন্স", icon = Icons.Default.Tune),
        FilterCondition(TOGGLE_ONLY_ACTUAL, "Only with Actual Expenses", "শুধু প্রকৃত ব্যয়যুক্ত", icon = Icons.Default.Tune),
        FilterCondition(TOGGLE_ACTIVE_3_MONTHS, "Active in Last 3 Months", "বিগত ৩ মাসে সক্রিয়", icon = Icons.Default.Tune),
        FilterCondition(TOGGLE_EXPENSE_FIRST, "Expense Categories First", "ব্যয় ক্যাটাগরি প্রথমে", icon = Icons.Default.Tune),
        FilterCondition(TOGGLE_DISPLAY_CURRENCY, "Display Currency Code", "মুদ্রা কোড প্রদর্শন", icon = Icons.Default.Tune),
        FilterCondition(TOGGLE_DISPLAY_SYMBOL, "Display Currency Symbol", "মুদ্রা প্রতীক প্রদর্শন", icon = Icons.Default.Tune)
    )

    fun createSpec(
        accounts: List<Account> = emptyList(),
        categories: List<Category> = emptyList(),
        labels: List<String> = emptyList(),
        countProvider: ((FilterState) -> Int)? = null
    ): FilterSpec<CategoryBudgetTrackingItem> {
        val accountOptions = accounts.map { acc ->
            SelectItemOption(
                id = acc.id.toString(),
                titleEn = acc.nameEn,
                titleBn = acc.nameBn,
                subtitleEn = if (acc.type == com.example.data.model.AccountType.ASSET) "Asset" else "Liability",
                subtitleBn = if (acc.type == com.example.data.model.AccountType.ASSET) "সম্পদ" else "দায়",
                icon = Icons.Default.AccountBalance
            )
        }

        val categoryOptions = categories.map { cat ->
            SelectItemOption(
                id = cat.id.toString(),
                titleEn = cat.nameEn,
                titleBn = cat.nameBn,
                subtitleEn = if (cat.type == com.example.data.model.CategoryType.EXPENSE) "Expense" else "Income",
                subtitleBn = if (cat.type == com.example.data.model.CategoryType.EXPENSE) "ব্যয়" else "আয়",
                icon = Icons.Default.Category
            )
        }

        val labelOptions = labels.map { lbl ->
            SelectItemOption(
                id = lbl,
                titleEn = lbl,
                titleBn = lbl,
                icon = Icons.Default.Label
            )
        }

        val fields = mutableListOf<FilterField<CategoryBudgetTrackingItem>>()

        // 1. Search Field
        fields.add(
            FilterField.SearchField(
                id = FIELD_SEARCH,
                titleEn = "Search",
                titleBn = "অনুসন্ধান",
                hintEn = "Search categories or notes...",
                hintBn = "ক্যাটাগরি বা নোট খুঁজুন...",
                icon = Icons.Default.Search,
                predicate = { item, query ->
                    if (query.isBlank()) true else {
                        val q = query.trim().lowercase()
                        item.category.nameEn.lowercase().contains(q) ||
                                item.category.nameBn.lowercase().contains(q) ||
                                item.transactions.any { it.transaction.note.lowercase().contains(q) }
                    }
                }
            )
        )

        // 2. Date Range Field
        fields.add(
            FilterField.DateField(
                id = FIELD_DATE,
                titleEn = "Date Range",
                titleBn = "সময়সীমা",
                icon = Icons.Default.CalendarMonth,
                presets = DATE_PRESET_OPTIONS,
                defaultPresetId = PRESET_THIS_MONTH
            )
        )

        // 3. Comparison Preset
        fields.add(
            FilterField.ToggleGroupField(
                id = FIELD_COMPARISON,
                titleEn = "Baseline Comparison",
                titleBn = "তুলনামূলক ভিত্তি",
                icon = Icons.Default.CompareArrows,
                conditions = COMPARISON_CONDITIONS.map { cond ->
                    FilterCondition<CategoryBudgetTrackingItem>(
                        id = cond.id,
                        titleEn = cond.titleEn,
                        titleBn = cond.titleBn,
                        icon = cond.icon,
                        group = cond.group
                    )
                }
            )
        )

        // 4. Flow Scope
        fields.add(
            FilterField.ToggleGroupField(
                id = FIELD_FLOW_SCOPE,
                titleEn = "Flow Scope",
                titleBn = "বাজেটের ধরন",
                icon = Icons.Default.FilterAlt,
                conditions = FLOW_SCOPE_CONDITIONS.map { cond ->
                    FilterCondition<CategoryBudgetTrackingItem>(
                        id = cond.id,
                        titleEn = cond.titleEn,
                        titleBn = cond.titleBn,
                        icon = cond.icon,
                        group = cond.group
                    )
                }
            )
        )

        // 5. Hierarchy View
        fields.add(
            FilterField.ToggleGroupField(
                id = FIELD_HIERARCHY,
                titleEn = "Hierarchy View",
                titleBn = "বিন্যাস",
                icon = Icons.Default.Tune,
                conditions = HIERARCHY_CONDITIONS.map { cond ->
                    FilterCondition<CategoryBudgetTrackingItem>(
                        id = cond.id,
                        titleEn = cond.titleEn,
                        titleBn = cond.titleBn,
                        icon = cond.icon,
                        group = cond.group
                    )
                }
            )
        )

        // 6. Categories Selection
        if (categoryOptions.isNotEmpty()) {
            fields.add(
                FilterField.SelectField(
                    id = FIELD_CATEGORIES,
                    titleEn = "Categories",
                    titleBn = "ক্যাটাগরি",
                    icon = Icons.Default.Category,
                    items = categoryOptions,
                    isMultiSelect = true,
                    predicate = { item, selected ->
                        if (selected.isEmpty()) true else {
                            selected.contains(item.category.id.toString()) ||
                                    (item.category.parentId != null && selected.contains(item.category.parentId.toString()))
                        }
                    }
                )
            )
        }

        // 7. Accounts Selection
        if (accountOptions.isNotEmpty()) {
            fields.add(
                FilterField.SelectField(
                    id = FIELD_ACCOUNTS,
                    titleEn = "Accounts",
                    titleBn = "অ্যাকাউন্ট",
                    icon = Icons.Default.AccountBalance,
                    items = accountOptions,
                    isMultiSelect = true,
                    predicate = { item, selected ->
                        if (selected.isEmpty()) true else {
                            item.transactions.any { tx ->
                                val d = tx.transaction.debitAccountId?.toString()
                                val c = tx.transaction.creditAccountId?.toString()
                                (d != null && selected.contains(d)) || (c != null && selected.contains(c))
                            }
                        }
                    }
                )
            )
        }

        // 8. Labels Selection
        if (labelOptions.isNotEmpty()) {
            fields.add(
                FilterField.SelectField(
                    id = FIELD_LABELS,
                    titleEn = "Labels & Tags",
                    titleBn = "লেবেল ও ট্যাগ",
                    icon = Icons.Default.Label,
                    items = labelOptions,
                    isMultiSelect = true,
                    predicate = { item, selected ->
                        if (selected.isEmpty()) true else {
                            item.transactions.any { tx ->
                                selected.any { tx.transaction.note.contains(it, ignoreCase = true) }
                            }
                        }
                    }
                )
            )
        }

        // 9. Transaction Status
        fields.add(
            FilterField.SelectField(
                id = FIELD_STATUSES,
                titleEn = "Verification Status",
                titleBn = "যাচাই অবস্থা",
                icon = Icons.Default.CheckCircle,
                items = STATUS_OPTIONS,
                isMultiSelect = true,
                predicate = { item, selected ->
                    if (selected.isEmpty()) true else {
                        item.transactions.any { selected.contains(it.transaction.status.name) }
                    }
                }
            )
        )

        // 10. Amount Range
        fields.add(
            FilterField.RangeField(
                id = FIELD_AMOUNT_RANGE,
                titleEn = "Amount Range",
                titleBn = "পরিমাণ সীমা",
                predicate = { item, min, max ->
                    val amt = item.spentAmount
                    val passMin = min == null || (amt >= min || (item.hasBudget && item.budgetLimit >= min))
                    val passMax = max == null || (amt <= max || (item.hasBudget && item.budgetLimit <= max))
                    passMin && passMax
                }
            )
        )

        // 11. Sort Field
        fields.add(
            FilterField.SortField(
                id = FIELD_SORT,
                titleEn = "Sort Order",
                titleBn = "সাজানোর ক্রম",
                icon = Icons.AutoMirrored.Filled.Sort,
                options = SORT_OPTIONS.map { opt ->
                    SortOptionItem<CategoryBudgetTrackingItem>(
                        id = opt.id,
                        titleEn = opt.titleEn,
                        titleBn = opt.titleBn,
                        icon = opt.icon
                    )
                },
                defaultSortId = SORT_AMOUNT_DESC
            )
        )

        // 12. Toggles
        fields.add(
            FilterField.ToggleGroupField(
                id = FIELD_TOGGLES,
                titleEn = "Display & Calculation Options",
                titleBn = "প্রদর্শন ও গণনা অপশন",
                icon = Icons.Default.Tune,
                conditions = TOGGLE_CONDITIONS.map { cond ->
                    FilterCondition<CategoryBudgetTrackingItem>(
                        id = cond.id,
                        titleEn = cond.titleEn,
                        titleBn = cond.titleBn,
                        icon = cond.icon
                    )
                }
            )
        )

        return FilterSpec(
            key = SPEC_KEY,
            titleEn = "Filter Budget Tracking",
            titleBn = "বাজেট ট্র্যাকিং ফিল্টার",
            fields = fields
        )
    }

    // Accessors
    fun getDatePreset(state: FilterState): String {
        return (state[FIELD_DATE] as? FilterValue.Date)?.presetId ?: PRESET_THIS_MONTH
    }

    fun getCustomStartDate(state: FilterState): Long? {
        return (state[FIELD_DATE] as? FilterValue.Date)?.startMs
    }

    fun getCustomEndDate(state: FilterState): Long? {
        return (state[FIELD_DATE] as? FilterValue.Date)?.endMs
    }

    fun getComparisonPreset(state: FilterState): String {
        return (state[FIELD_COMPARISON] as? FilterValue.ToggleGroup)?.activeIds?.firstOrNull() ?: COMP_NONE
    }

    fun isComparisonEnabled(state: FilterState): Boolean {
        val comp = getComparisonPreset(state)
        return comp != COMP_NONE
    }

    fun getFlowScope(state: FilterState): String {
        return (state[FIELD_FLOW_SCOPE] as? FilterValue.ToggleGroup)?.activeIds?.firstOrNull() ?: SCOPE_ALL
    }

    fun getHierarchyView(state: FilterState): String {
        return (state[FIELD_HIERARCHY] as? FilterValue.ToggleGroup)?.activeIds?.firstOrNull() ?: HIERARCHY_GROUPED
    }

    fun getSelectedCategoryIds(state: FilterState): Set<Long> {
        val ids = (state[FIELD_CATEGORIES] as? FilterValue.Select)?.selectedIds ?: emptySet()
        return ids.mapNotNull { it.toLongOrNull() }.toSet()
    }

    fun getSelectedAccountIds(state: FilterState): Set<Long> {
        val ids = (state[FIELD_ACCOUNTS] as? FilterValue.Select)?.selectedIds ?: emptySet()
        return ids.mapNotNull { it.toLongOrNull() }.toSet()
    }

    fun getSelectedLabels(state: FilterState): Set<String> {
        return (state[FIELD_LABELS] as? FilterValue.Select)?.selectedIds ?: emptySet()
    }

    fun getSelectedStatuses(state: FilterState): Set<TransactionStatus> {
        val names = (state[FIELD_STATUSES] as? FilterValue.Select)?.selectedIds ?: emptySet()
        return names.mapNotNull { name ->
            try { TransactionStatus.valueOf(name) } catch (_: Exception) { null }
        }.toSet()
    }

    fun getMinAmount(state: FilterState): Double? {
        return (state[FIELD_AMOUNT_RANGE] as? FilterValue.Range)?.min
    }

    fun getMaxAmount(state: FilterState): Double? {
        return (state[FIELD_AMOUNT_RANGE] as? FilterValue.Range)?.max
    }

    fun getSortOrder(state: FilterState): String {
        return (state[FIELD_SORT] as? FilterValue.Sort)?.sortId ?: SORT_AMOUNT_DESC
    }

    fun getActiveToggles(state: FilterState): Set<String> {
        return (state[FIELD_TOGGLES] as? FilterValue.ToggleGroup)?.activeIds
            ?: setOf(TOGGLE_EXCLUDE_ZERO, TOGGLE_HIDE_EMPTY_GROUPS, TOGGLE_EXPENSE_FIRST, TOGGLE_DISPLAY_CURRENCY, TOGGLE_DISPLAY_SYMBOL)
    }

    fun getExcludeZero(state: FilterState): Boolean {
        return getActiveToggles(state).contains(TOGGLE_EXCLUDE_ZERO)
    }

    fun getHideEmptyGroups(state: FilterState): Boolean {
        return getActiveToggles(state).contains(TOGGLE_HIDE_EMPTY_GROUPS)
    }

    fun getOnlyBudgeted(state: FilterState): Boolean {
        return getActiveToggles(state).contains(TOGGLE_ONLY_BUDGETED)
    }

    fun getOnlyOverBudget(state: FilterState): Boolean {
        return getActiveToggles(state).contains(TOGGLE_ONLY_OVER_BUDGET)
    }

    fun getOnlyRemaining(state: FilterState): Boolean {
        return getActiveToggles(state).contains(TOGGLE_ONLY_REMAINING)
    }

    fun getOnlyActual(state: FilterState): Boolean {
        return getActiveToggles(state).contains(TOGGLE_ONLY_ACTUAL)
    }

    fun getActive3Months(state: FilterState): Boolean {
        return getActiveToggles(state).contains(TOGGLE_ACTIVE_3_MONTHS)
    }

    fun getExpenseFirst(state: FilterState): Boolean {
        return getActiveToggles(state).contains(TOGGLE_EXPENSE_FIRST)
    }

    fun getDisplayCurrency(state: FilterState): Boolean {
        return getActiveToggles(state).contains(TOGGLE_DISPLAY_CURRENCY)
    }

    fun getDisplayCurrencySymbol(state: FilterState): Boolean {
        return getActiveToggles(state).contains(TOGGLE_DISPLAY_SYMBOL)
    }

    fun getShowOnlyGroups(state: FilterState): Boolean {
        return getHierarchyView(state) == HIERARCHY_ONLY_GROUPS
    }

    fun getShowWithoutGroups(state: FilterState): Boolean {
        return getHierarchyView(state) == HIERARCHY_WITHOUT_GROUPS
    }

    fun getSearchQuery(state: FilterState): String {
        return (state[FIELD_SEARCH] as? FilterValue.Search)?.query ?: ""
    }

    // Date Bounds & Comparison Calculation
    fun calculateRanges(
        year: Int,
        month: Int,
        state: FilterState,
        languageMode: LanguageMode
    ): BudgetRangeResult {
        val cal = Calendar.getInstance()
        val sdfShort = SimpleDateFormat("dd MMM", Locale.getDefault())

        val preset = getDatePreset(state)
        val customStart = getCustomStartDate(state)
        val customEnd = getCustomEndDate(state)

        val (primaryStartMs, primaryEndMs, primaryLabel) = when (preset) {
            PRESET_LAST_12_MONTHS -> {
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, month - 1)
                val end = DateUtils.getEndOfMonth(year, month)
                cal.add(Calendar.MONTH, -11)
                val startY = cal.get(Calendar.YEAR)
                val startM = cal.get(Calendar.MONTH) + 1
                val start = DateUtils.getStartOfMonth(startY, startM)
                val label = if (languageMode == LanguageMode.BANGLA) "বিগত ১২ মাস" else "Last 12 Months"
                Triple(start, end, label)
            }
            PRESET_THIS_MONTH -> {
                val start = DateUtils.getStartOfMonth(year, month)
                val end = DateUtils.getEndOfMonth(year, month)
                val label = DateUtils.formatMonthYear(year, month, languageMode)
                Triple(start, end, label)
            }
            PRESET_LAST_MONTH -> {
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
            PRESET_LAST_3_MONTHS -> {
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
            PRESET_LAST_6_MONTHS -> {
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
            PRESET_YEAR_TO_DATE -> {
                val start = DateUtils.getStartOfMonth(year, 1)
                val end = DateUtils.getEndOfMonth(year, month)
                val label = if (languageMode == LanguageMode.BANGLA) "$year এর শুরু থেকে" else "YTD $year"
                Triple(start, end, label)
            }
            PRESET_SAME_MONTH_LAST_YEAR -> {
                val prevYear = year - 1
                val start = DateUtils.getStartOfMonth(prevYear, month)
                val end = DateUtils.getEndOfMonth(prevYear, month)
                val label = DateUtils.formatMonthYear(prevYear, month, languageMode)
                Triple(start, end, label)
            }
            PRESET_ALL_TIME -> {
                val label = if (languageMode == LanguageMode.BANGLA) "সব সময়" else "All Time"
                Triple(0L, Long.MAX_VALUE, label)
            }
            PRESET_CUSTOM -> {
                val start = customStart ?: DateUtils.getStartOfMonth(year, month)
                val end = customEnd ?: DateUtils.getEndOfMonth(year, month)
                val label = "${sdfShort.format(Date(start))} - ${sdfShort.format(Date(end))}"
                Triple(start, end, label)
            }
            else -> {
                val start = DateUtils.getStartOfMonth(year, month)
                val end = DateUtils.getEndOfMonth(year, month)
                val label = DateUtils.formatMonthYear(year, month, languageMode)
                Triple(start, end, label)
            }
        }

        // 2. Determine Comparison Date Range (If enabled)
        val compPreset = getComparisonPreset(state)
        var comparisonRange: Pair<Long, Long>? = null
        var comparisonLabel: String? = null

        if (compPreset != COMP_NONE) {
            val (compStart, compEnd, compLbl) = when (compPreset) {
                COMP_SAME_DATE_PREV_MONTH -> {
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
                COMP_SAME_DATE_PREV_YEAR -> {
                    val prevYear = year - 1
                    val start = DateUtils.getStartOfMonth(prevYear, month)
                    val end = DateUtils.getEndOfMonth(prevYear, month)
                    val label = DateUtils.formatMonthYear(prevYear, month, languageMode)
                    Triple(start, end, label)
                }
                COMP_LAST_MONTH -> {
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
                COMP_SAME_MONTH_LAST_YEAR -> {
                    val prevYear = year - 1
                    val start = DateUtils.getStartOfMonth(prevYear, month)
                    val end = DateUtils.getEndOfMonth(prevYear, month)
                    val label = DateUtils.formatMonthYear(prevYear, month, languageMode)
                    Triple(start, end, label)
                }
                COMP_LAST_3_MONTHS_AVG -> {
                    cal.set(Calendar.YEAR, year)
                    cal.set(Calendar.MONTH, month - 1)
                    cal.add(Calendar.MONTH, -3)
                    val prevStart = DateUtils.getStartOfMonth(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
                    cal.add(Calendar.MONTH, 2)
                    val prevEnd = DateUtils.getEndOfMonth(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
                    val label = if (languageMode == LanguageMode.BANGLA) "পূর্ববর্তী ৩ মাস" else "Prev 3 Months"
                    Triple(prevStart, prevEnd, label)
                }
                COMP_LAST_YEAR -> {
                    val prevYear = year - 1
                    val start = DateUtils.getStartOfMonth(prevYear, 1)
                    val end = DateUtils.getEndOfMonth(prevYear, 12)
                    val label = if (languageMode == LanguageMode.BANGLA) "$prevYear সাল" else "$prevYear"
                    Triple(start, end, label)
                }
                COMP_CUSTOM -> {
                    val start = customStart ?: (primaryStartMs - (30L * 24 * 60 * 60 * 1000L))
                    val end = customEnd ?: (primaryEndMs - (30L * 24 * 60 * 60 * 1000L))
                    val label = "${sdfShort.format(Date(start))} - ${sdfShort.format(Date(end))}"
                    Triple(start, end, label)
                }
                else -> {
                    Triple(primaryStartMs, primaryEndMs, "")
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

    fun toBudgetFilterState(state: FilterState): BudgetFilterState {
        val presetStr = getDatePreset(state)
        val datePreset = when (presetStr) {
            PRESET_LAST_12_MONTHS -> BudgetDateRangePreset.LAST_12_MONTHS
            PRESET_THIS_MONTH -> BudgetDateRangePreset.THIS_MONTH
            PRESET_LAST_MONTH -> BudgetDateRangePreset.LAST_MONTH
            PRESET_LAST_3_MONTHS -> BudgetDateRangePreset.LAST_3_MONTHS
            PRESET_LAST_6_MONTHS -> BudgetDateRangePreset.LAST_6_MONTHS
            PRESET_YEAR_TO_DATE -> BudgetDateRangePreset.YEAR_TO_DATE
            PRESET_SAME_MONTH_LAST_YEAR -> BudgetDateRangePreset.SAME_MONTH_LAST_YEAR
            PRESET_ALL_TIME -> BudgetDateRangePreset.ALL_TIME
            PRESET_CUSTOM -> BudgetDateRangePreset.CUSTOM
            else -> BudgetDateRangePreset.THIS_MONTH
        }

        val compPresetStr = getComparisonPreset(state)
        val comparisonEnabled = compPresetStr != COMP_NONE
        val compPreset = when (compPresetStr) {
            COMP_SAME_DATE_PREV_MONTH -> BudgetComparisonPreset.SAME_DATE_PREV_MONTH
            COMP_SAME_DATE_PREV_YEAR -> BudgetComparisonPreset.SAME_DATE_PREV_YEAR
            COMP_LAST_MONTH -> BudgetComparisonPreset.LAST_MONTH
            COMP_SAME_MONTH_LAST_YEAR -> BudgetComparisonPreset.SAME_MONTH_LAST_YEAR
            COMP_LAST_3_MONTHS_AVG -> BudgetComparisonPreset.LAST_3_MONTHS_AVG
            COMP_LAST_YEAR -> BudgetComparisonPreset.LAST_YEAR
            COMP_CUSTOM -> BudgetComparisonPreset.CUSTOM
            else -> BudgetComparisonPreset.LAST_MONTH
        }

        val sortId = getSortOrder(state)
        val sortOrder = when (sortId) {
            SORT_DEFAULT -> BudgetSortOrder.DEFAULT
            SORT_AMOUNT_ASC -> BudgetSortOrder.AMOUNT_ASC
            SORT_SPENT_DESC -> BudgetSortOrder.SPENT_DESC
            SORT_REMAINING_DESC -> BudgetSortOrder.REMAINING_DESC
            SORT_REMAINING_ASC -> BudgetSortOrder.REMAINING_ASC
            SORT_BUDGET_DESC -> BudgetSortOrder.BUDGET_DESC
            SORT_BUDGET_ASC -> BudgetSortOrder.BUDGET_ASC
            SORT_UTILIZATION_DESC -> BudgetSortOrder.UTILIZATION_DESC
            SORT_NAME_ASC -> BudgetSortOrder.NAME_ASC
            SORT_NAME_DESC -> BudgetSortOrder.NAME_DESC
            else -> BudgetSortOrder.AMOUNT_DESC
        }

        val hierarchy = getHierarchyView(state)
        val onlyGroups = hierarchy == HIERARCHY_ONLY_GROUPS
        val withoutGroups = hierarchy == HIERARCHY_WITHOUT_GROUPS

        return BudgetFilterState(
            datePreset = datePreset,
            customStartDateMs = getCustomStartDate(state),
            customEndDateMs = getCustomEndDate(state),
            comparisonEnabled = comparisonEnabled,
            comparisonPreset = compPreset,
            selectedCategoryIds = getSelectedCategoryIds(state),
            selectedAccountIds = getSelectedAccountIds(state),
            selectedLabels = getSelectedLabels(state),
            selectedStatusSet = getSelectedStatuses(state),
            excludeZeroAmounts = getExcludeZero(state),
            hideEmptyGroups = getHideEmptyGroups(state),
            showOnlyCategoriesWithoutGroups = withoutGroups,
            showOnlyGroups = onlyGroups,
            displayCurrency = getDisplayCurrency(state),
            displayCurrencySymbol = getDisplayCurrencySymbol(state),
            sortByAmount = sortOrder == BudgetSortOrder.AMOUNT_DESC || sortOrder == BudgetSortOrder.AMOUNT_ASC,
            showExpenseCategoriesFirst = getExpenseFirst(state),
            sortOrder = sortOrder,
            showOnlyRemainingBalance = getOnlyRemaining(state),
            showOnlyActual = getOnlyActual(state),
            filterOnlyBudgeted = getOnlyBudgeted(state),
            filterOnlyOverBudget = getOnlyOverBudget(state),
            filterActive3Months = getActive3Months(state),
            minAmount = getMinAmount(state),
            maxAmount = getMaxAmount(state)
        )
    }

    fun fromBudgetFilterState(old: BudgetFilterState): FilterState {
        val state = mutableMapOf<String, FilterValue>()

        val presetId = when (old.datePreset) {
            BudgetDateRangePreset.LAST_12_MONTHS -> PRESET_LAST_12_MONTHS
            BudgetDateRangePreset.THIS_MONTH -> PRESET_THIS_MONTH
            BudgetDateRangePreset.LAST_MONTH -> PRESET_LAST_MONTH
            BudgetDateRangePreset.LAST_3_MONTHS -> PRESET_LAST_3_MONTHS
            BudgetDateRangePreset.LAST_6_MONTHS -> PRESET_LAST_6_MONTHS
            BudgetDateRangePreset.YEAR_TO_DATE -> PRESET_YEAR_TO_DATE
            BudgetDateRangePreset.SAME_MONTH_LAST_YEAR -> PRESET_SAME_MONTH_LAST_YEAR
            BudgetDateRangePreset.ALL_TIME -> PRESET_ALL_TIME
            BudgetDateRangePreset.CUSTOM -> PRESET_CUSTOM
        }
        state[FIELD_DATE] = FilterValue.Date(
            presetId = presetId,
            startMs = old.customStartDateMs,
            endMs = old.customEndDateMs
        )

        val compId = if (old.comparisonEnabled) {
            when (old.comparisonPreset) {
                BudgetComparisonPreset.SAME_DATE_PREV_MONTH -> COMP_SAME_DATE_PREV_MONTH
                BudgetComparisonPreset.SAME_DATE_PREV_YEAR -> COMP_SAME_DATE_PREV_YEAR
                BudgetComparisonPreset.LAST_MONTH -> COMP_LAST_MONTH
                BudgetComparisonPreset.SAME_MONTH_LAST_YEAR -> COMP_SAME_MONTH_LAST_YEAR
                BudgetComparisonPreset.LAST_3_MONTHS_AVG -> COMP_LAST_3_MONTHS_AVG
                BudgetComparisonPreset.LAST_YEAR -> COMP_LAST_YEAR
                BudgetComparisonPreset.CUSTOM -> COMP_CUSTOM
            }
        } else {
            COMP_NONE
        }
        state[FIELD_COMPARISON] = FilterValue.ToggleGroup(setOf(compId))

        val hierarchyId = when {
            old.showOnlyGroups -> HIERARCHY_ONLY_GROUPS
            old.showOnlyCategoriesWithoutGroups -> HIERARCHY_WITHOUT_GROUPS
            else -> HIERARCHY_GROUPED
        }
        state[FIELD_HIERARCHY] = FilterValue.ToggleGroup(setOf(hierarchyId))

        if (old.selectedCategoryIds.isNotEmpty()) {
            state[FIELD_CATEGORIES] = FilterValue.Select(old.selectedCategoryIds.map { it.toString() }.toSet())
        }
        if (old.selectedAccountIds.isNotEmpty()) {
            state[FIELD_ACCOUNTS] = FilterValue.Select(old.selectedAccountIds.map { it.toString() }.toSet())
        }
        if (old.selectedLabels.isNotEmpty()) {
            state[FIELD_LABELS] = FilterValue.Select(old.selectedLabels)
        }
        if (old.selectedStatusSet.isNotEmpty()) {
            state[FIELD_STATUSES] = FilterValue.Select(old.selectedStatusSet.map { it.name }.toSet())
        }

        if (old.minAmount != null || old.maxAmount != null) {
            state[FIELD_AMOUNT_RANGE] = FilterValue.Range(min = old.minAmount, max = old.maxAmount)
        }

        val sortId = when (old.sortOrder) {
            BudgetSortOrder.DEFAULT -> SORT_DEFAULT
            BudgetSortOrder.AMOUNT_DESC -> SORT_AMOUNT_DESC
            BudgetSortOrder.AMOUNT_ASC -> SORT_AMOUNT_ASC
            BudgetSortOrder.SPENT_DESC -> SORT_SPENT_DESC
            BudgetSortOrder.REMAINING_DESC -> SORT_REMAINING_DESC
            BudgetSortOrder.REMAINING_ASC -> SORT_REMAINING_ASC
            BudgetSortOrder.BUDGET_DESC -> SORT_BUDGET_DESC
            BudgetSortOrder.BUDGET_ASC -> SORT_BUDGET_ASC
            BudgetSortOrder.UTILIZATION_DESC -> SORT_UTILIZATION_DESC
            BudgetSortOrder.NAME_ASC -> SORT_NAME_ASC
            BudgetSortOrder.NAME_DESC -> SORT_NAME_DESC
        }
        state[FIELD_SORT] = FilterValue.Sort(sortId)

        val toggles = mutableSetOf<String>()
        if (old.excludeZeroAmounts) toggles.add(TOGGLE_EXCLUDE_ZERO)
        if (old.hideEmptyGroups) toggles.add(TOGGLE_HIDE_EMPTY_GROUPS)
        if (old.filterOnlyBudgeted) toggles.add(TOGGLE_ONLY_BUDGETED)
        if (old.filterOnlyOverBudget) toggles.add(TOGGLE_ONLY_OVER_BUDGET)
        if (old.showOnlyRemainingBalance) toggles.add(TOGGLE_ONLY_REMAINING)
        if (old.showOnlyActual) toggles.add(TOGGLE_ONLY_ACTUAL)
        if (old.filterActive3Months) toggles.add(TOGGLE_ACTIVE_3_MONTHS)
        if (old.showExpenseCategoriesFirst) toggles.add(TOGGLE_EXPENSE_FIRST)
        if (old.displayCurrency) toggles.add(TOGGLE_DISPLAY_CURRENCY)
        if (old.displayCurrencySymbol) toggles.add(TOGGLE_DISPLAY_SYMBOL)
        state[FIELD_TOGGLES] = FilterValue.ToggleGroup(toggles)

        return state
    }
}
