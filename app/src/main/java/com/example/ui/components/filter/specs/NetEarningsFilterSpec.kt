package com.example.ui.components.filter.specs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.ui.graphics.Color
import com.example.data.model.Account
import com.example.data.model.Category
import com.example.data.model.CategoryType
import com.example.data.model.LanguageMode
import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionWithDetails
import com.example.ui.components.BudgetRangeResult
import com.example.ui.components.filter.DatePresetOption
import com.example.ui.components.filter.FilterCondition
import com.example.ui.components.filter.FilterField
import com.example.ui.components.filter.FilterSpec
import com.example.ui.components.filter.FilterState
import com.example.ui.components.filter.FilterValue
import com.example.ui.components.filter.SelectItemOption
import com.example.ui.components.filter.SortOptionItem
import com.example.util.DateUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Unified Filter Specification for Net Earnings.
 * Shared across ReportsScreen, DashboardNetEarningsCard, CategoryTimelineScreen, and DashboardScreen.
 */
object NetEarningsFilterSpec {

    const val SPEC_KEY = "net_earnings"

    // Field identifiers
    const val FIELD_SEARCH = "search"
    const val FIELD_DATE = "date"
    const val FIELD_COMPARISON = "comparison"
    const val FIELD_FLOW_SCOPE = "flow_scope"
    const val FIELD_HIERARCHY = "hierarchy"
    const val FIELD_ACCOUNTS = "accounts"
    const val FIELD_CATEGORIES = "categories"
    const val FIELD_LABELS = "labels"
    const val FIELD_STATUSES = "statuses"
    const val FIELD_AMOUNT_RANGE = "amount_range"
    const val FIELD_SORT = "sort"
    const val FIELD_TOGGLES = "toggles"

    // Date Presets
    const val PRESET_THIS_MONTH = "this_month"
    const val PRESET_LAST_MONTH = "last_month"
    const val PRESET_LAST_3_MONTHS = "last_3_months"
    const val PRESET_LAST_6_MONTHS = "last_6_months"
    const val PRESET_LAST_12_MONTHS = "last_12_months"
    const val PRESET_YEAR_TO_DATE = "year_to_date"
    const val PRESET_SAME_MONTH_LAST_YEAR = "same_month_last_year"
    const val PRESET_ALL_TIME = "all_time"
    const val PRESET_CUSTOM = "custom"

    // Comparison Presets
    const val COMP_NONE = "none"
    const val COMP_SAME_DATE_PREV_MONTH = "same_date_prev_month"
    const val COMP_LAST_MONTH = "last_month"
    const val COMP_SAME_MONTH_LAST_YEAR = "same_month_last_year"
    const val COMP_LAST_3_MONTHS_AVG = "last_3_months_avg"
    const val COMP_LAST_YEAR = "last_year"
    const val COMP_CUSTOM = "custom"

    // Flow Scopes
    const val SCOPE_ALL = "all"
    const val SCOPE_SURPLUS_ONLY = "surplus_only"
    const val SCOPE_DEFICIT_ONLY = "deficit_only"
    const val SCOPE_INCOME_ONLY = "income_only"
    const val SCOPE_EXPENSE_ONLY = "expense_only"

    // Hierarchy Views
    const val HIERARCHY_GROUPED = "grouped"
    const val HIERARCHY_ONLY_GROUPS = "only_groups"
    const val HIERARCHY_ONLY_ITEMS = "only_items"

    // Sort Options
    const val SORT_NET_DESC = "net_desc"
    const val SORT_NET_ASC = "net_asc"
    const val SORT_AMOUNT_DESC = "amount_desc"
    const val SORT_AMOUNT_ASC = "amount_asc"
    const val SORT_PERCENTAGE_DESC = "percentage_desc"
    const val SORT_NAME_ASC = "name_asc"
    const val SORT_TXN_COUNT_DESC = "txn_count_desc"
    const val SORT_DEFAULT = "default"

    // Toggle Options
    const val TOGGLE_INCLUDE_TRANSFERS = "include_transfers"
    const val TOGGLE_EXCLUDE_ZERO = "exclude_zero"
    const val TOGGLE_HIDE_EMPTY_GROUPS = "hide_empty_groups"
    const val TOGGLE_WITHOUT_GROUPS = "without_groups"
    const val TOGGLE_DISPLAY_CURRENCY = "display_currency"
    const val TOGGLE_DISPLAY_SYMBOL = "display_symbol"

    // Presets for DateField
    val DATE_PRESETS: List<DatePresetOption> = listOf(
        DatePresetOption(PRESET_THIS_MONTH, "This Month", "চলতি মাস") {
            val cal = Calendar.getInstance()
            val y = cal.get(Calendar.YEAR)
            val m = cal.get(Calendar.MONTH) + 1
            Pair(DateUtils.getStartOfMonth(y, m), DateUtils.getEndOfMonth(y, m))
        },
        DatePresetOption(PRESET_LAST_MONTH, "Last Month", "গত মাস") {
            val cal = Calendar.getInstance().apply { add(Calendar.MONTH, -1) }
            val y = cal.get(Calendar.YEAR)
            val m = cal.get(Calendar.MONTH) + 1
            Pair(DateUtils.getStartOfMonth(y, m), DateUtils.getEndOfMonth(y, m))
        },
        DatePresetOption(PRESET_LAST_3_MONTHS, "Last 3 Months", "গত ৩ মাস") {
            val cal = Calendar.getInstance()
            val y = cal.get(Calendar.YEAR)
            val m = cal.get(Calendar.MONTH) + 1
            val end = DateUtils.getEndOfMonth(y, m)
            cal.add(Calendar.MONTH, -2)
            val start = DateUtils.getStartOfMonth(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
            Pair(start, end)
        },
        DatePresetOption(PRESET_LAST_6_MONTHS, "Last 6 Months", "গত ৬ মাস") {
            val cal = Calendar.getInstance()
            val y = cal.get(Calendar.YEAR)
            val m = cal.get(Calendar.MONTH) + 1
            val end = DateUtils.getEndOfMonth(y, m)
            cal.add(Calendar.MONTH, -5)
            val start = DateUtils.getStartOfMonth(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
            Pair(start, end)
        },
        DatePresetOption(PRESET_LAST_12_MONTHS, "Last 12 Months", "গত ১২ মাস") {
            val cal = Calendar.getInstance()
            val y = cal.get(Calendar.YEAR)
            val m = cal.get(Calendar.MONTH) + 1
            val end = DateUtils.getEndOfMonth(y, m)
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
        DatePresetOption(PRESET_SAME_MONTH_LAST_YEAR, "Same Month Last Year", "গত বছরের একই মাস") {
            val cal = Calendar.getInstance()
            val y = cal.get(Calendar.YEAR) - 1
            val m = cal.get(Calendar.MONTH) + 1
            Pair(DateUtils.getStartOfMonth(y, m), DateUtils.getEndOfMonth(y, m))
        },
        DatePresetOption(PRESET_ALL_TIME, "All Time", "সব সময়") {
            Pair(0L, Long.MAX_VALUE)
        },
        DatePresetOption(PRESET_CUSTOM, "Custom Range", "কাস্টম সময়সীমা") {
            Pair(0L, 0L)
        }
    )

    // Flow Scope Conditions
    val FLOW_SCOPE_CONDITIONS: List<FilterCondition<Any>> = listOf(
        FilterCondition(SCOPE_ALL, "All Cash Flow", "উভয় (আয় ও ব্যয়)", group = "flow"),
        FilterCondition(SCOPE_SURPLUS_ONLY, "Surplus (Income > Expense)", "শুধু উদ্বৃত্ত (লাভ)", icon = Icons.Default.TrendingUp, group = "flow"),
        FilterCondition(SCOPE_DEFICIT_ONLY, "Deficit (Expense > Income)", "শুধু ঘাটতি (ক্ষতি)", icon = Icons.Default.TrendingDown, group = "flow"),
        FilterCondition(SCOPE_INCOME_ONLY, "Incomes Only", "শুধু আয়", icon = Icons.Default.TrendingUp, group = "flow"),
        FilterCondition(SCOPE_EXPENSE_ONLY, "Expenses Only", "শুধু ব্যয়", icon = Icons.Default.TrendingDown, group = "flow")
    )

    // Comparison Conditions
    val COMPARISON_CONDITIONS: List<FilterCondition<Any>> = listOf(
        FilterCondition(COMP_NONE, "No Comparison", "তুলনা বন্ধ", group = "comp"),
        FilterCondition(COMP_SAME_DATE_PREV_MONTH, "Prev Month (Same Dates)", "পূর্ববর্তী মাস (একই তারিখ)", group = "comp"),
        FilterCondition(COMP_LAST_MONTH, "Last Month (Full)", "গত পুরো মাস", group = "comp"),
        FilterCondition(COMP_SAME_MONTH_LAST_YEAR, "Same Month Last Year", "গত বছরের একই মাস", group = "comp"),
        FilterCondition(COMP_LAST_3_MONTHS_AVG, "Last 3 Months Avg", "গত ৩ মাসের গড়", group = "comp"),
        FilterCondition(COMP_LAST_YEAR, "Last Year (Full)", "গত পুরো বছর", group = "comp"),
        FilterCondition(COMP_CUSTOM, "Custom Comparison", "কাস্টম তুলনা", group = "comp")
    )

    // Hierarchy Conditions
    val HIERARCHY_CONDITIONS: List<FilterCondition<Any>> = listOf(
        FilterCondition(HIERARCHY_GROUPED, "Grouped View", "গ্রুপ ও আইটেম", group = "hierarchy"),
        FilterCondition(HIERARCHY_ONLY_GROUPS, "Groups Only", "শুধুমাত্র গ্রুপ", group = "hierarchy"),
        FilterCondition(HIERARCHY_ONLY_ITEMS, "Items Only", "শুধুমাত্র আইটেম", group = "hierarchy")
    )

    // Toggle Conditions
    val TOGGLE_CONDITIONS: List<FilterCondition<Any>> = listOf(
        FilterCondition(TOGGLE_INCLUDE_TRANSFERS, "Include Transfers", "স্থানান্তর অন্তর্ভুক্ত", icon = Icons.Default.SwapHoriz),
        FilterCondition(TOGGLE_EXCLUDE_ZERO, "Exclude Zero Amounts", "শূন্য পরিমাণ বাদ"),
        FilterCondition(TOGGLE_HIDE_EMPTY_GROUPS, "Hide Empty Groups", "খালি গ্রুপ লুকান"),
        FilterCondition(TOGGLE_WITHOUT_GROUPS, "Only Without Groups", "গ্রুপহীন ক্যাটাগরি"),
        FilterCondition(TOGGLE_DISPLAY_CURRENCY, "Show Currency", "মুদ্রা প্রদর্শন"),
        FilterCondition(TOGGLE_DISPLAY_SYMBOL, "Show Currency Symbol", "মুদ্রার প্রতীক প্রদর্শন")
    )

    // Sort Options
    val SORT_OPTIONS: List<SortOptionItem<Any>> = listOf(
        SortOptionItem(SORT_NET_DESC, "Net Surplus: High → Low", "নিট উদ্বৃত্ত: বেশি → কম", comparator = null, icon = Icons.AutoMirrored.Filled.Sort),
        SortOptionItem(SORT_NET_ASC, "Net Deficit: Low → High", "নিট আয়: কম → বেশি", comparator = null, icon = Icons.AutoMirrored.Filled.Sort),
        SortOptionItem(SORT_AMOUNT_DESC, "Total Flow: High → Low", "মোট লেনদেন: বেশি → কম", comparator = null, icon = Icons.AutoMirrored.Filled.Sort),
        SortOptionItem(SORT_AMOUNT_ASC, "Total Flow: Low → High", "মোট লেনদেন: কম → বেশি", comparator = null, icon = Icons.AutoMirrored.Filled.Sort),
        SortOptionItem(SORT_PERCENTAGE_DESC, "Highest % Share", "সর্বোচ্চ হার", comparator = null, icon = Icons.AutoMirrored.Filled.Sort),
        SortOptionItem(SORT_NAME_ASC, "Alphabetical: A → Z", "নাম: A → Z", comparator = null, icon = Icons.AutoMirrored.Filled.Sort),
        SortOptionItem(SORT_TXN_COUNT_DESC, "Most Transactions", "সর্বাধিক লেনদেন", comparator = null, icon = Icons.AutoMirrored.Filled.Sort),
        SortOptionItem(SORT_DEFAULT, "Default Order", "পূর্বনির্ধারিত ক্রম", comparator = null, icon = Icons.AutoMirrored.Filled.Sort)
    )

    // Status Options
    val STATUS_OPTIONS: List<SelectItemOption> = listOf(
        SelectItemOption(TransactionStatus.CLEARED.name, "Cleared", "নিষ্পন্ন", icon = Icons.Default.CheckCircle),
        SelectItemOption(TransactionStatus.RECONCILED.name, "Reconciled", "মিলিত", icon = Icons.Default.CheckCircle),
        SelectItemOption(TransactionStatus.VOID.name, "Void", "বাতিল", icon = Icons.Default.CheckCircle)
    )

    fun createSpec(
        accounts: List<Account> = emptyList(),
        categories: List<Category> = emptyList(),
        labels: List<String> = emptyList()
    ): FilterSpec<TransactionWithDetails> {
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
                subtitleEn = if (cat.type == CategoryType.INCOME) "Income" else "Expense",
                subtitleBn = if (cat.type == CategoryType.INCOME) "আয়" else "ব্যয়",
                color = cat.colorHex?.let { try { Color(android.graphics.Color.parseColor(it)) } catch (_: Exception) { null } },
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

        val fields = mutableListOf<FilterField<TransactionWithDetails>>()

        // 1. Search Query
        fields.add(
            FilterField.SearchField(
                id = FIELD_SEARCH,
                titleEn = "Search",
                titleBn = "অনুসন্ধান",
                hintEn = "Search notes, payees, categories...",
                hintBn = "নোট, প্রাপক, ক্যাটাগরি খুঁজুন...",
                icon = Icons.Default.Search,
                predicate = { tw, query ->
                    val q = query.trim().lowercase()
                    if (q.isEmpty()) true else {
                        val tx = tw.transaction
                        tx.note.lowercase().contains(q) ||
                                tx.payeeOrPayer.lowercase().contains(q) ||
                                (tw.category?.nameEn?.lowercase()?.contains(q) == true) ||
                                (tw.category?.nameBn?.lowercase()?.contains(q) == true) ||
                                (tw.debitAccount?.nameEn?.lowercase()?.contains(q) == true) ||
                                (tw.creditAccount?.nameEn?.lowercase()?.contains(q) == true) ||
                                tx.amount.toString().contains(q)
                    }
                }
            )
        )

        // 2. Date Presets & Custom
        fields.add(
            FilterField.DateField(
                id = FIELD_DATE,
                titleEn = "Time Period",
                titleBn = "সময়সীমা",
                icon = Icons.Default.DateRange,
                presets = DATE_PRESETS,
                defaultPresetId = PRESET_THIS_MONTH,
                allowCustom = true
            )
        )

        // 3. Comparison Mode
        fields.add(
            FilterField.ToggleGroupField(
                id = FIELD_COMPARISON,
                titleEn = "Comparison Range",
                titleBn = "তুলনার সময়সীমা",
                icon = Icons.Default.CompareArrows,
                conditions = COMPARISON_CONDITIONS.map { cond ->
                    FilterCondition<TransactionWithDetails>(
                        id = cond.id,
                        titleEn = cond.titleEn,
                        titleBn = cond.titleBn,
                        icon = cond.icon,
                        group = cond.group
                    )
                },
            )
        )

        // 4. Flow Scope
        fields.add(
            FilterField.ToggleGroupField(
                id = FIELD_FLOW_SCOPE,
                titleEn = "Flow Scope",
                titleBn = "প্রবাহের ধরন",
                icon = Icons.Default.Tune,
                conditions = FLOW_SCOPE_CONDITIONS.map { cond ->
                    FilterCondition<TransactionWithDetails>(
                        id = cond.id,
                        titleEn = cond.titleEn,
                        titleBn = cond.titleBn,
                        icon = cond.icon,
                        group = cond.group,
                        predicate = { tw ->
                            val tx = tw.transaction
                            when (cond.id) {
                                SCOPE_INCOME_ONLY -> tx.type == com.example.data.model.TransactionType.INCOME
                                SCOPE_EXPENSE_ONLY -> tx.type == com.example.data.model.TransactionType.EXPENSE
                                else -> true
                            }
                        }
                    )
                },
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
                    FilterCondition<TransactionWithDetails>(
                        id = cond.id,
                        titleEn = cond.titleEn,
                        titleBn = cond.titleBn,
                        icon = cond.icon,
                        group = cond.group
                    )
                },
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
                    predicate = { tw, selected ->
                        if (selected.isEmpty()) true else {
                            val catId = tw.transaction.categoryId ?: tw.category?.id ?: tw.subCategory?.id
                            catId != null && selected.contains(catId.toString())
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
                    predicate = { tw, selected ->
                        if (selected.isEmpty()) true else {
                            val accId = tw.debitAccount?.id ?: tw.creditAccount?.id ?: tw.transaction.debitAccountId ?: tw.transaction.creditAccountId
                            accId != null && selected.contains(accId.toString())
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
                    predicate = { tw, selected ->
                        if (selected.isEmpty()) true else {
                            val note = tw.transaction.note
                            selected.any { note.contains(it, ignoreCase = true) }
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
                predicate = { tw, selected ->
                    if (selected.isEmpty()) true else {
                        selected.contains(tw.transaction.status.name)
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
                predicate = { tw, min, max ->
                    val amt = tw.transaction.amount
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
                options = SORT_OPTIONS.map { opt ->
                    SortOptionItem<TransactionWithDetails>(
                        id = opt.id,
                        titleEn = opt.titleEn,
                        titleBn = opt.titleBn,
                        icon = opt.icon
                    )
                },
                defaultSortId = SORT_NET_DESC
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
                    FilterCondition<TransactionWithDetails>(
                        id = cond.id,
                        titleEn = cond.titleEn,
                        titleBn = cond.titleBn,
                        icon = cond.icon
                    )
                },
            )
        )

        return FilterSpec(
            key = SPEC_KEY,
            titleEn = "Filter Net Earnings",
            titleBn = "নিট আয় ফিল্টার",
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
        return (state[FIELD_SORT] as? FilterValue.Sort)?.sortId ?: SORT_NET_DESC
    }

    fun getActiveToggles(state: FilterState): Set<String> {
        return (state[FIELD_TOGGLES] as? FilterValue.ToggleGroup)?.activeIds
            ?: setOf(TOGGLE_EXCLUDE_ZERO, TOGGLE_HIDE_EMPTY_GROUPS, TOGGLE_DISPLAY_CURRENCY, TOGGLE_DISPLAY_SYMBOL)
    }

    fun getIncludeTransfers(state: FilterState): Boolean {
        return getActiveToggles(state).contains(TOGGLE_INCLUDE_TRANSFERS)
    }

    fun getExcludeZero(state: FilterState): Boolean {
        return getActiveToggles(state).contains(TOGGLE_EXCLUDE_ZERO)
    }

    fun getHideEmptyGroups(state: FilterState): Boolean {
        return getActiveToggles(state).contains(TOGGLE_HIDE_EMPTY_GROUPS)
    }

    fun getShowWithoutGroups(state: FilterState): Boolean {
        return getActiveToggles(state).contains(TOGGLE_WITHOUT_GROUPS)
    }

    fun getDisplayCurrency(state: FilterState): Boolean {
        return getActiveToggles(state).contains(TOGGLE_DISPLAY_CURRENCY)
    }

    fun getDisplayCurrencySymbol(state: FilterState): Boolean {
        return getActiveToggles(state).contains(TOGGLE_DISPLAY_SYMBOL)
    }

    fun getSearchQuery(state: FilterState): String {
        return (state[FIELD_SEARCH] as? FilterValue.Search)?.query ?: ""
    }

    // Builders / State Updaters
    fun withDatePreset(state: FilterState, presetId: String): FilterState {
        return state + (FIELD_DATE to FilterValue.Date(presetId = presetId))
    }

    fun withCustomDates(state: FilterState, startMs: Long, endMs: Long): FilterState {
        return state + (FIELD_DATE to FilterValue.Date(
            presetId = PRESET_CUSTOM,
            startMs = startMs,
            endMs = endMs
        ))
    }

    fun withComparison(state: FilterState, compId: String): FilterState {
        return state + (FIELD_COMPARISON to FilterValue.ToggleGroup(setOf(compId)))
    }

    fun withFlowScope(state: FilterState, scopeId: String): FilterState {
        return state + (FIELD_FLOW_SCOPE to FilterValue.ToggleGroup(setOf(scopeId)))
    }

    fun withHierarchyView(state: FilterState, hierarchyId: String): FilterState {
        return state + (FIELD_HIERARCHY to FilterValue.ToggleGroup(setOf(hierarchyId)))
    }

    fun withCategoryIds(state: FilterState, catIds: Set<Long>): FilterState {
        return state + (FIELD_CATEGORIES to FilterValue.Select(catIds.map { it.toString() }.toSet()))
    }

    fun withAccountIds(state: FilterState, accIds: Set<Long>): FilterState {
        return state + (FIELD_ACCOUNTS to FilterValue.Select(accIds.map { it.toString() }.toSet()))
    }

    fun withSortOrder(state: FilterState, sortId: String): FilterState {
        return state + (FIELD_SORT to FilterValue.Sort(sortId))
    }

    fun withSearchQuery(state: FilterState, query: String): FilterState {
        return state + (FIELD_SEARCH to FilterValue.Search(query))
    }

    fun withToggle(state: FilterState, toggleId: String, enable: Boolean): FilterState {
        val current = getActiveToggles(state).toMutableSet()
        if (enable) current.add(toggleId) else current.remove(toggleId)
        return state + (FIELD_TOGGLES to FilterValue.ToggleGroup(current))
    }

    /**
     * Resolves date bounds and labels for primary and comparison ranges.
     */
    fun calculateRanges(
        year: Int,
        month: Int,
        state: FilterState,
        languageMode: LanguageMode
    ): BudgetRangeResult {
        val cal = Calendar.getInstance()
        val sdfShort = SimpleDateFormat("dd MMM", Locale.getDefault())

        val datePreset = getDatePreset(state)
        val customStart = getCustomStartDate(state)
        val customEnd = getCustomEndDate(state)

        val (primaryStartMs, primaryEndMs, primaryLabel) = when (datePreset) {
            PRESET_LAST_12_MONTHS -> {
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, month - 1)
                val end = DateUtils.getEndOfMonth(year, month)
                cal.add(Calendar.MONTH, -11)
                val startY = cal.get(Calendar.YEAR)
                val startM = cal.get(Calendar.MONTH) + 1
                val start = DateUtils.getStartOfMonth(startY, startM)
                Triple(start, end, if (languageMode == LanguageMode.BANGLA) "গত ১২ মাস" else "Last 12 Months")
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
                val prevY = cal.get(Calendar.YEAR)
                val prevM = cal.get(Calendar.MONTH) + 1
                val start = DateUtils.getStartOfMonth(prevY, prevM)
                val end = DateUtils.getEndOfMonth(prevY, prevM)
                val label = DateUtils.formatMonthYear(prevY, prevM, languageMode)
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
                Triple(start, end, if (languageMode == LanguageMode.BANGLA) "গত ৩ মাস" else "Last 3 Months")
            }
            PRESET_LAST_6_MONTHS -> {
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, month - 1)
                val end = DateUtils.getEndOfMonth(year, month)
                cal.add(Calendar.MONTH, -5)
                val startY = cal.get(Calendar.YEAR)
                val startM = cal.get(Calendar.MONTH) + 1
                val start = DateUtils.getStartOfMonth(startY, startM)
                Triple(start, end, if (languageMode == LanguageMode.BANGLA) "গত ৬ মাস" else "Last 6 Months")
            }
            PRESET_YEAR_TO_DATE -> {
                val start = DateUtils.getStartOfMonth(year, 1)
                val end = DateUtils.getEndOfMonth(year, month)
                Triple(start, end, if (languageMode == LanguageMode.BANGLA) "$year এর শুরু থেকে" else "YTD $year")
            }
            PRESET_SAME_MONTH_LAST_YEAR -> {
                val prevY = year - 1
                val start = DateUtils.getStartOfMonth(prevY, month)
                val end = DateUtils.getEndOfMonth(prevY, month)
                val label = DateUtils.formatMonthYear(prevY, month, languageMode)
                Triple(start, end, label)
            }
            PRESET_ALL_TIME -> {
                Triple(0L, Long.MAX_VALUE, if (languageMode == LanguageMode.BANGLA) "সব সময়" else "All Time")
            }
            PRESET_CUSTOM -> {
                val s = customStart ?: DateUtils.getStartOfMonth(year, month)
                val e = customEnd ?: DateUtils.getEndOfMonth(year, month)
                val lbl = "${sdfShort.format(Date(s))} - ${sdfShort.format(Date(e))}"
                Triple(s, e, lbl)
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
                    cal.timeInMillis = primaryStartMs
                    cal.add(Calendar.MONTH, -1)
                    val s = cal.timeInMillis
                    cal.timeInMillis = primaryEndMs
                    cal.add(Calendar.MONTH, -1)
                    val e = cal.timeInMillis
                    Triple(s, e, if (languageMode == LanguageMode.BANGLA) "পূর্ববর্তী মাস" else "Prev Month")
                }
                COMP_LAST_MONTH -> {
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
                COMP_SAME_MONTH_LAST_YEAR -> {
                    val prevY = year - 1
                    val s = DateUtils.getStartOfMonth(prevY, month)
                    val e = DateUtils.getEndOfMonth(prevY, month)
                    val lbl = DateUtils.formatMonthYear(prevY, month, languageMode)
                    Triple(s, e, lbl)
                }
                COMP_LAST_3_MONTHS_AVG -> {
                    cal.set(Calendar.YEAR, year)
                    cal.set(Calendar.MONTH, month - 1)
                    cal.add(Calendar.MONTH, -1)
                    val end = DateUtils.getEndOfMonth(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
                    cal.add(Calendar.MONTH, -2)
                    val start = DateUtils.getStartOfMonth(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
                    Triple(start, end, if (languageMode == LanguageMode.BANGLA) "পূর্ববর্তী ৩ মাস" else "Prev 3 Months")
                }
                COMP_LAST_YEAR -> {
                    val prevY = year - 1
                    val s = DateUtils.getStartOfMonth(prevY, 1)
                    val e = DateUtils.getEndOfMonth(prevY, 12)
                    Triple(s, e, if (languageMode == LanguageMode.BANGLA) "গত বছর ($prevY)" else "Last Year ($prevY)")
                }
                COMP_CUSTOM -> {
                    val s = customStart ?: primaryStartMs
                    val e = customEnd ?: primaryEndMs
                    val lbl = "${sdfShort.format(Date(s))} - ${sdfShort.format(Date(e))}"
                    Triple(s, e, lbl)
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
