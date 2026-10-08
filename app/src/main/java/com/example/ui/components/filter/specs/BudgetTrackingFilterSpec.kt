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
import androidx.compose.material.icons.filled.Tune
import com.example.data.model.Account
import com.example.data.model.Category
import com.example.data.model.LanguageMode
import com.example.data.model.TransactionStatus
import com.example.ui.components.BudgetDateRangePreset
import com.example.ui.components.BudgetComparisonPreset
import com.example.ui.components.BudgetRangeResult
import com.example.ui.components.BudgetSortOrder
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

    val DATE_PRESETS = listOf(
        FilterCondition<CategoryBudgetTrackingItem>(PRESET_THIS_MONTH, "This Month", "চলতি মাস", Icons.Default.CalendarMonth),
        FilterCondition<CategoryBudgetTrackingItem>(PRESET_LAST_MONTH, "Last Month", "গত মাস", Icons.Default.CalendarMonth),
        FilterCondition<CategoryBudgetTrackingItem>(PRESET_LAST_3_MONTHS, "Last 3 Months", "বিগত ৩ মাস", Icons.Default.CalendarMonth),
        FilterCondition<CategoryBudgetTrackingItem>(PRESET_LAST_6_MONTHS, "Last 6 Months", "বিগত ৬ মাস", Icons.Default.CalendarMonth),
        FilterCondition<CategoryBudgetTrackingItem>(PRESET_LAST_12_MONTHS, "Last 12 Months", "বিগত ১২ মাস", Icons.Default.CalendarMonth),
        FilterCondition<CategoryBudgetTrackingItem>(PRESET_YEAR_TO_DATE, "Year to Date", "বছরের শুরু থেকে", Icons.Default.CalendarMonth),
        FilterCondition<CategoryBudgetTrackingItem>(PRESET_SAME_MONTH_LAST_YEAR, "Same Month Last Year", "গত বছরের এই মাস", Icons.Default.CalendarMonth),
        FilterCondition<CategoryBudgetTrackingItem>(PRESET_ALL_TIME, "All Time", "সব সময়", Icons.Default.CalendarMonth),
        FilterCondition<CategoryBudgetTrackingItem>(PRESET_CUSTOM, "Custom Date Range", "নির্দিষ্ট সময়সীমা", Icons.Default.CalendarMonth)
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

    val COMPARISON_CONDITIONS = listOf(
        FilterCondition<CategoryBudgetTrackingItem>(COMP_NONE, "No Comparison", "তুলনাহীন", Icons.Default.CompareArrows),
        FilterCondition<CategoryBudgetTrackingItem>(COMP_SAME_DATE_PREV_MONTH, "Same Date Prev Month", "পূর্ববর্তী মাসের একই তারিখ", Icons.Default.CompareArrows),
        FilterCondition<CategoryBudgetTrackingItem>(COMP_SAME_DATE_PREV_YEAR, "Same Date Prev Year", "গত বছরের একই তারিখ", Icons.Default.CompareArrows),
        FilterCondition<CategoryBudgetTrackingItem>(COMP_LAST_MONTH, "Previous Full Month", "গত পুরো মাস", Icons.Default.CompareArrows),
        FilterCondition<CategoryBudgetTrackingItem>(COMP_SAME_MONTH_LAST_YEAR, "Same Month Last Year", "গত বছরের এই মাস", Icons.Default.CompareArrows),
        FilterCondition<CategoryBudgetTrackingItem>(COMP_LAST_3_MONTHS_AVG, "Previous 3 Months", "পূর্ববর্তী ৩ মাস", Icons.Default.CompareArrows),
        FilterCondition<CategoryBudgetTrackingItem>(COMP_LAST_YEAR, "Previous Full Year", "গত পুরো বছর", Icons.Default.CompareArrows),
        FilterCondition<CategoryBudgetTrackingItem>(COMP_CUSTOM, "Custom Date Range", "নির্দিষ্ট সময়সীমা", Icons.Default.CompareArrows)
    )

    // Flow Scope
    const val SCOPE_ALL = "scope_all"
    const val SCOPE_EXPENSE_ONLY = "scope_expense_only"
    const val SCOPE_INCOME_ONLY = "scope_income_only"

    val FLOW_SCOPE_CONDITIONS = listOf(
        FilterCondition<CategoryBudgetTrackingItem>(SCOPE_ALL, "All (Expense & Income)", "উভয় (ব্যয় ও আয়)", Icons.Default.FilterAlt),
        FilterCondition<CategoryBudgetTrackingItem>(SCOPE_EXPENSE_ONLY, "Expense Budgets", "ব্যয় বাজেট", Icons.Default.FilterAlt),
        FilterCondition<CategoryBudgetTrackingItem>(SCOPE_INCOME_ONLY, "Income Budgets", "আয় বাজেট", Icons.Default.FilterAlt)
    )

    // Hierarchy View
    const val HIERARCHY_GROUPED = "hierarchy_grouped"
    const val HIERARCHY_ONLY_GROUPS = "hierarchy_only_groups"
    const val HIERARCHY_WITHOUT_GROUPS = "hierarchy_without_groups"

    val HIERARCHY_CONDITIONS = listOf(
        FilterCondition<CategoryBudgetTrackingItem>(HIERARCHY_GROUPED, "Grouped Hierarchy", "গ্রুপ ও ক্যাটাগরি", Icons.Default.Tune),
        FilterCondition<CategoryBudgetTrackingItem>(HIERARCHY_ONLY_GROUPS, "Only Parent Groups", "শুধু প্রধান গ্রুপ", Icons.Default.Tune),
        FilterCondition<CategoryBudgetTrackingItem>(HIERARCHY_WITHOUT_GROUPS, "Flat List (Without Groups)", "গ্রুপ ছাড়া তালিকা", Icons.Default.Tune)
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

    val SORT_OPTIONS = listOf(
        SortOptionItem<CategoryBudgetTrackingItem>(SORT_DEFAULT, "Default Order", "ডিফল্ট ক্রম", Icons.AutoMirrored.Filled.Sort),
        SortOptionItem<CategoryBudgetTrackingItem>(SORT_AMOUNT_DESC, "Amount: High → Low", "পরিমাণ: বেশি → কম", Icons.AutoMirrored.Filled.Sort),
        SortOptionItem<CategoryBudgetTrackingItem>(SORT_AMOUNT_ASC, "Amount: Low → High", "পরিমাণ: কম → বেশি", Icons.AutoMirrored.Filled.Sort),
        SortOptionItem<CategoryBudgetTrackingItem>(SORT_SPENT_DESC, "Actual Spent: High → Low", "প্রকৃত ব্যয়: বেশি → কম", Icons.AutoMirrored.Filled.Sort),
        SortOptionItem<CategoryBudgetTrackingItem>(SORT_REMAINING_DESC, "Remaining: High → Low", "অবশিষ্ট: বেশি → কম", Icons.AutoMirrored.Filled.Sort),
        SortOptionItem<CategoryBudgetTrackingItem>(SORT_REMAINING_ASC, "Remaining: Low → High", "অবশিষ্ট: কম → বেশি", Icons.AutoMirrored.Filled.Sort),
        SortOptionItem<CategoryBudgetTrackingItem>(SORT_BUDGET_DESC, "Budget Limit: High → Low", "বাজেট সীমা: বেশি → কম", Icons.AutoMirrored.Filled.Sort),
        SortOptionItem<CategoryBudgetTrackingItem>(SORT_BUDGET_ASC, "Budget Limit: Low → High", "বাজেট সীমা: কম → বেশি", Icons.AutoMirrored.Filled.Sort),
        SortOptionItem<CategoryBudgetTrackingItem>(SORT_UTILIZATION_DESC, "Utilization %: High → Low", "ব্যবহারের হার: বেশি → কম", Icons.AutoMirrored.Filled.Sort),
        SortOptionItem<CategoryBudgetTrackingItem>(SORT_NAME_ASC, "Alphabetical: A → Z", "নাম: A → Z", Icons.AutoMirrored.Filled.Sort),
        SortOptionItem<CategoryBudgetTrackingItem>(SORT_NAME_DESC, "Alphabetical: Z → A", "নাম: Z → A", Icons.AutoMirrored.Filled.Sort)
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

    val TOGGLE_CONDITIONS = listOf(
        FilterCondition<CategoryBudgetTrackingItem>(TOGGLE_EXCLUDE_ZERO, "Exclude Zero Amounts", "শূন্য পরিমাণ বাদ দিন", Icons.Default.Tune),
        FilterCondition<CategoryBudgetTrackingItem>(TOGGLE_HIDE_EMPTY_GROUPS, "Hide Empty Groups", "খালি গ্রুপ লুকান", Icons.Default.Tune),
        FilterCondition<CategoryBudgetTrackingItem>(TOGGLE_ONLY_BUDGETED, "Only Budgeted Categories", "শুধু বাজেটযুক্ত ক্যাটাগরি", Icons.Default.Tune),
        FilterCondition<CategoryBudgetTrackingItem>(TOGGLE_ONLY_OVER_BUDGET, "Only Over Budget (Exceeded)", "শুধু বাজেট অতিক্রান্ত", Icons.Default.Tune),
        FilterCondition<CategoryBudgetTrackingItem>(TOGGLE_ONLY_REMAINING, "Only Categories with Remaining", "শুধু অবশিষ্ট ব্যালেন্স", Icons.Default.Tune),
        FilterCondition<CategoryBudgetTrackingItem>(TOGGLE_ONLY_ACTUAL, "Only with Actual Expenses", "শুধু প্রকৃত ব্যয়যুক্ত", Icons.Default.Tune),
        FilterCondition<CategoryBudgetTrackingItem>(TOGGLE_ACTIVE_3_MONTHS, "Active in Last 3 Months", "বিগত ৩ মাসে সক্রিয়", Icons.Default.Tune),
        FilterCondition<CategoryBudgetTrackingItem>(TOGGLE_EXPENSE_FIRST, "Expense Categories First", "ব্যয় ক্যাটাগরি প্রথমে", Icons.Default.Tune),
        FilterCondition<CategoryBudgetTrackingItem>(TOGGLE_DISPLAY_CURRENCY, "Display Currency Code", "মুদ্রা কোড প্রদর্শন", Icons.Default.Tune),
        FilterCondition<CategoryBudgetTrackingItem>(TOGGLE_DISPLAY_SYMBOL, "Display Currency Symbol", "মুদ্রা প্রতীক প্রদর্শন", Icons.Default.Tune)
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
                placeholderEn = "Search categories or notes...",
                placeholderBn = "ক্যাটাগরি বা নোট খুঁজুন...",
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
                presets = DATE_PRESETS,
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
                conditions = COMPARISON_CONDITIONS,
                isExclusive = true
            )
        )

        // 4. Flow Scope
        fields.add(
            FilterField.ToggleGroupField(
                id = FIELD_FLOW_SCOPE,
                titleEn = "Flow Scope",
                titleBn = "বাজেটের ধরন",
                icon = Icons.Default.FilterAlt,
                conditions = FLOW_SCOPE_CONDITIONS,
                isExclusive = true
            )
        )

        // 5. Hierarchy View
        fields.add(
            FilterField.ToggleGroupField(
                id = FIELD_HIERARCHY,
                titleEn = "Hierarchy View",
                titleBn = "বিন্যাস",
                icon = Icons.Default.Tune,
                conditions = HIERARCHY_CONDITIONS,
                isExclusive = true
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
                            val catId = item.category.id.toString()
                            val parentId = item.category.parentId?.toString()
                            selected.contains(catId) || (parentId != null && selected.contains(parentId))
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
                            item.transactions.any { tw ->
                                val accId = tw.debitAccount?.id ?: tw.creditAccount?.id ?: tw.transaction.debitAccountId ?: tw.transaction.creditAccountId
                                accId != null && selected.contains(accId.toString())
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
                            item.transactions.any { tw ->
                                selected.any { tag -> tw.transaction.note.contains(tag, ignoreCase = true) }
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
                        item.transactions.any { tw -> selected.contains(tw.transaction.status.name) }
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
                    val amt = if (item.budgetLimit > 0) item.budgetLimit else item.spentAmount
                    val passMin = min == null || amt >= min
                    val passMax = max == null || amt <= max
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
                options = SORT_OPTIONS,
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
                conditions = TOGGLE_CONDITIONS,
                isExclusive = false
            )
        )

        return FilterSpec(
            key = SPEC_KEY,
            titleEn = "Filter Budget Tracking",
            titleBn = "বাজেট ট্র্যাকিং ফিল্টার",
            fields = fields,
            countProvider = countProvider
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

        var comparisonRange: Pair<Long, Long>? = null
        var comparisonLabel: String? = null

        val compPreset = getComparisonPreset(state)
        if (compPreset != COMP_NONE) {
            val (compStart, compEnd, compLbl) = when (compPreset) {
                COMP_SAME_DATE_PREV_MONTH -> {
                    val calStart = Calendar.getInstance().apply {
                        timeInMillis = primaryStartMs
                        add(Calendar.MONTH, -1)
                    }
                    val calEnd = Calendar.getInstance().apply {
                        timeInMillis = primaryEndMs
                        add(Calendar.MONTH, -1)
                    }
                    val start = calStart.timeInMillis
                    val end = calEnd.timeInMillis
                    val label = if (preset == PRESET_THIS_MONTH) {
                        DateUtils.formatMonthYear(calStart.get(Calendar.YEAR), calStart.get(Calendar.MONTH) + 1, languageMode)
                    } else {
                        "${sdfShort.format(Date(start))} - ${sdfShort.format(Date(end))}"
                    }
                    Triple(start, end, label)
                }
                COMP_SAME_DATE_PREV_YEAR -> {
                    val calStart = Calendar.getInstance().apply {
                        timeInMillis = primaryStartMs
                        add(Calendar.YEAR, -1)
                    }
                    val calEnd = Calendar.getInstance().apply {
                        timeInMillis = primaryEndMs
                        add(Calendar.YEAR, -1)
                    }
                    val start = calStart.timeInMillis
                    val end = calEnd.timeInMillis
                    val label = if (preset == PRESET_THIS_MONTH) {
                        DateUtils.formatMonthYear(calStart.get(Calendar.YEAR), calStart.get(Calendar.MONTH) + 1, languageMode)
                    } else {
                        "${sdfShort.format(Date(start))} - ${sdfShort.format(Date(end))}"
                    }
                    Triple(start, end, label)
                }
                COMP_LAST_MONTH -> {
                    cal.set(Calendar.YEAR, year)
                    cal.set(Calendar.MONTH, month - 1)
                    cal.add(Calendar.MONTH, -1)
                    val prevYear = cal.get(Calendar.YEAR)
                    val prevMonth = cal.get(Calendar.MONTH) + 1
                    val start = DateUtils.getStartOfMonth(prevYear, prevMonth)
                    val end = DateUtils.getEndOfMonth(prevYear, prevMonth)
                    val label = DateUtils.formatMonthYear(prevYear, prevMonth, languageMode)
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
}
