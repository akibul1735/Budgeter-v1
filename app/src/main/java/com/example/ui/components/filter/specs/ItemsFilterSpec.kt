package com.example.ui.components.filter.specs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.ui.graphics.Color
import com.example.data.model.Account
import com.example.data.model.Category
import com.example.data.model.CategoryType
import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionType
import com.example.ui.components.filter.DatePresetOption
import com.example.ui.components.filter.FilterCondition
import com.example.ui.components.filter.FilterField
import com.example.ui.components.filter.FilterSpec
import com.example.ui.components.filter.FilterState
import com.example.ui.components.filter.FilterValue
import com.example.ui.components.filter.SelectItemOption
import com.example.ui.components.filter.SortOptionItem
import com.example.ui.dialogs.AggregatedDatePreset
import com.example.ui.dialogs.AggregatedSortOrder
import com.example.util.DateUtils
import java.util.Calendar

/**
 * Filter specification for ItemsScreen (Item Summary).
 */
object ItemsFilterSpec {

    const val SPEC_KEY = "items"

    // Field IDs
    const val FIELD_SEARCH = "search"
    const val FIELD_TYPE_MODE = "type_mode"
    const val FIELD_DATE = "date"
    const val FIELD_TRANSACTION_TYPE = "transaction_type"
    const val FIELD_ACCOUNTS = "accounts"
    const val FIELD_CATEGORIES = "categories"
    const val FIELD_STATUSES = "statuses"
    const val FIELD_AMOUNT_RANGE = "amount_range"
    const val FIELD_EXCLUDE_ZERO = "exclude_zero"
    const val FIELD_SORT = "sort"

    // Type mode condition IDs
    const val MODE_EXPENSE = "expense"
    const val MODE_ALL = "all"
    const val MODE_INCOME = "income"

    // Exclude Zero condition ID
    const val EXCLUDE_ZERO_ID = "exclude"

    // Sort IDs
    const val SORT_DEFAULT = "default"
    const val SORT_AMOUNT_DESC = "amount_desc"
    const val SORT_AMOUNT_ASC = "amount_asc"
    const val SORT_COUNT_DESC = "count_desc"
    const val SORT_COUNT_ASC = "count_asc"
    const val SORT_AVG_DESC = "avg_desc"
    const val SORT_AVG_ASC = "avg_asc"
    const val SORT_NAME_ASC = "name_asc"
    const val SORT_NAME_DESC = "name_desc"
    const val SORT_RECENT_DATE = "recent_date"

    val SORT_OPTIONS: List<SortOptionItem<Any>> = listOf(
        SortOptionItem(SORT_AMOUNT_DESC, "Amount: High → Low", "পরিমাণ: বেশি → কম"),
        SortOptionItem(SORT_AMOUNT_ASC, "Amount: Low → High", "পরিমাণ: কম → বেশি"),
        SortOptionItem(SORT_COUNT_DESC, "Count: Most Frequent", "লেনদেন: বেশি → কম"),
        SortOptionItem(SORT_COUNT_ASC, "Count: Least Frequent", "লেনদেন: কম → বেশি"),
        SortOptionItem(SORT_AVG_DESC, "Average: High → Low", "গড়: বেশি → কম"),
        SortOptionItem(SORT_AVG_ASC, "Average: Low → High", "গড়: কম → বেশি"),
        SortOptionItem(SORT_NAME_ASC, "Name: A → Z", "নাম: A → Z"),
        SortOptionItem(SORT_NAME_DESC, "Name: Z → A", "নাম: Z → A"),
        SortOptionItem(SORT_RECENT_DATE, "Recent Activity", "সাম্প্রতিক লেনদেন"),
        SortOptionItem(SORT_DEFAULT, "Default Order", "পূর্বনির্ধারিত ক্রম")
    )

    fun createSpec(
        categories: List<Category> = emptyList(),
        accounts: List<Account> = emptyList()
    ): FilterSpec<Any> {
        val datePresets = listOf(
            DatePresetOption(
                id = "this_month",
                titleEn = "This Month",
                titleBn = "চলতি মাস",
                calculateRange = {
                    val cal = Calendar.getInstance()
                    val year = cal.get(Calendar.YEAR)
                    val month = cal.get(Calendar.MONTH) + 1
                    Pair(DateUtils.getStartOfMonth(year, month), DateUtils.getEndOfMonth(year, month))
                }
            ),
            DatePresetOption(
                id = "last_month",
                titleEn = "Last Month",
                titleBn = "গত মাস",
                calculateRange = {
                    val cal = Calendar.getInstance().apply {
                        set(Calendar.DAY_OF_MONTH, 1)
                        add(Calendar.MONTH, -1)
                    }
                    val year = cal.get(Calendar.YEAR)
                    val month = cal.get(Calendar.MONTH) + 1
                    Pair(DateUtils.getStartOfMonth(year, month), DateUtils.getEndOfMonth(year, month))
                }
            ),
            DatePresetOption(
                id = "this_week",
                titleEn = "This Week",
                titleBn = "এই সপ্তাহ",
                calculateRange = {
                    val now = System.currentTimeMillis()
                    val cal = Calendar.getInstance().apply {
                        timeInMillis = now
                        firstDayOfWeek = DateUtils.activeFirstDayOfWeek
                        set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
                    }
                    val start = DateUtils.getStartOfDay(cal.timeInMillis)
                    val end = start + (7L * 24L * 60L * 60L * 1000L) - 1L
                    Pair(start, end)
                }
            ),
            DatePresetOption(
                id = "today",
                titleEn = "Today",
                titleBn = "আজকে",
                calculateRange = {
                    val now = System.currentTimeMillis()
                    Pair(DateUtils.getStartOfDay(now), DateUtils.getEndOfDay(now))
                }
            ),
            DatePresetOption(
                id = "yesterday",
                titleEn = "Yesterday",
                titleBn = "গতকাল",
                calculateRange = {
                    val cal = Calendar.getInstance().apply {
                        add(Calendar.DAY_OF_YEAR, -1)
                    }
                    val ms = cal.timeInMillis
                    Pair(DateUtils.getStartOfDay(ms), DateUtils.getEndOfDay(ms))
                }
            ),
            DatePresetOption(
                id = "last_30_days",
                titleEn = "Last 30 Days",
                titleBn = "গত ৩০ দিন",
                calculateRange = {
                    val now = System.currentTimeMillis()
                    val endOfToday = DateUtils.getStartOfDay(now) + 86400000L - 1L
                    Pair(now - (30L * 24L * 60L * 60L * 1000L), endOfToday)
                }
            ),
            DatePresetOption(
                id = "last_90_days",
                titleEn = "Last 90 Days",
                titleBn = "গত ৯০ দিন",
                calculateRange = {
                    val now = System.currentTimeMillis()
                    val endOfToday = DateUtils.getStartOfDay(now) + 86400000L - 1L
                    Pair(now - (90L * 24L * 60L * 60L * 1000L), endOfToday)
                }
            ),
            DatePresetOption(
                id = "this_year",
                titleEn = "This Year",
                titleBn = "এই বছর",
                calculateRange = {
                    val year = Calendar.getInstance().get(Calendar.YEAR)
                    Pair(DateUtils.getStartOfMonth(year, 1), DateUtils.getEndOfMonth(year, 12))
                }
            ),
            DatePresetOption(
                id = "last_year",
                titleEn = "Last Year",
                titleBn = "গত বছর",
                calculateRange = {
                    val year = Calendar.getInstance().get(Calendar.YEAR) - 1
                    Pair(DateUtils.getStartOfMonth(year, 1), DateUtils.getEndOfMonth(year, 12))
                }
            ),
            DatePresetOption(
                id = "all_time",
                titleEn = "All Time",
                titleBn = "সব সময়",
                calculateRange = { Pair(0L, Long.MAX_VALUE) }
            )
        )

        val accountOptions = accounts.map { acc ->
            SelectItemOption(
                id = acc.id.toString(),
                titleEn = acc.nameEn,
                titleBn = acc.nameBn,
                subtitleEn = acc.accountNumber,
                subtitleBn = acc.accountNumber
            )
        }

        val categoryOptions = categories.map { cat ->
            SelectItemOption(
                id = cat.id.toString(),
                titleEn = cat.nameEn,
                titleBn = cat.nameBn,
                groupKey = if (cat.type == CategoryType.EXPENSE) "expense" else "income",
                groupTitleEn = if (cat.type == CategoryType.EXPENSE) "Expense Categories" else "Income Categories",
                groupTitleBn = if (cat.type == CategoryType.EXPENSE) "ব্যয় ক্যাটাগরি" else "আয় ক্যাটাগরি"
            )
        }

        val statusOptions = listOf(
            SelectItemOption("CLEARED", "Cleared", "সম্পন্ন"),
            SelectItemOption("RECONCILED", "Reconciled", "মিলিত"),
            SelectItemOption("VOID", "Void", "বাতিল")
        )

        return FilterSpec(
            key = SPEC_KEY,
            titleEn = "Filter & Sort Items",
            titleBn = "আইটেম ফিল্টার ও সাজানো",
            accentColor = Color(0xFFE91E63), // Crimson Pink
            fields = listOf(
                FilterField.SearchField(
                    id = FIELD_SEARCH,
                    titleEn = "Search",
                    titleBn = "অনুসন্ধান",
                    hintEn = "Search items/payees...",
                    hintBn = "আইটেম / প্রাপক খুঁজুন...",
                    icon = Icons.Default.Search
                ),
                FilterField.ToggleGroupField(
                    id = FIELD_TYPE_MODE,
                    titleEn = "Transaction Type Scope",
                    titleBn = "লেনদেনের প্রকৃতি",
                    icon = Icons.Default.FilterList,
                    conditions = listOf(
                        FilterCondition(MODE_EXPENSE, "Expenses", "ব্যয়", icon = Icons.AutoMirrored.Filled.TrendingDown, group = "type_mode"),
                        FilterCondition(MODE_ALL, "All", "সকল", icon = Icons.Default.FilterList, group = "type_mode"),
                        FilterCondition(MODE_INCOME, "Income", "আয়", icon = Icons.AutoMirrored.Filled.TrendingUp, group = "type_mode")
                    )
                ),
                FilterField.DateField(
                    id = FIELD_DATE,
                    titleEn = "Date Range",
                    titleBn = "সময়সীমা",
                    icon = Icons.Default.CalendarMonth,
                    presets = datePresets,
                    defaultPresetId = "this_month",
                    allowCustom = true
                ),
                FilterField.SelectField(
                    id = FIELD_TRANSACTION_TYPE,
                    titleEn = "Specific Flow",
                    titleBn = "নির্দিষ্ট প্রবাহ",
                    icon = Icons.Default.Payments,
                    isMultiSelect = false,
                    items = listOf(
                        SelectItemOption("EXPENSE", "Expense", "ব্যয়"),
                        SelectItemOption("INCOME", "Income", "আয়"),
                        SelectItemOption("TRANSFER", "Transfer", "স্থানান্তর")
                    )
                ),
                FilterField.SelectField(
                    id = FIELD_ACCOUNTS,
                    titleEn = "Accounts",
                    titleBn = "একাউন্ট",
                    icon = Icons.Default.AccountBalance,
                    isMultiSelect = true,
                    items = accountOptions
                ),
                FilterField.SelectField(
                    id = FIELD_CATEGORIES,
                    titleEn = "Categories",
                    titleBn = "ক্যাটাগরি",
                    icon = Icons.Default.Category,
                    isMultiSelect = true,
                    isHierarchical = true,
                    items = categoryOptions
                ),
                FilterField.SelectField(
                    id = FIELD_STATUSES,
                    titleEn = "Status",
                    titleBn = "স্ট্যাটাস",
                    icon = Icons.Default.CheckCircle,
                    isMultiSelect = true,
                    items = statusOptions
                ),
                FilterField.RangeField(
                    id = FIELD_AMOUNT_RANGE,
                    titleEn = "Amount Range",
                    titleBn = "পরিমাণ সীমা",
                    icon = Icons.Default.Payments,
                    prefix = "৳"
                ),
                FilterField.ToggleGroupField(
                    id = FIELD_EXCLUDE_ZERO,
                    titleEn = "Zero Amounts",
                    titleBn = "শূন্য পরিমাণ",
                    icon = Icons.Default.Tune,
                    conditions = listOf(
                        FilterCondition(EXCLUDE_ZERO_ID, "Exclude Zero Amounts", "শূন্য পরিমাণ বাদ দিন", icon = Icons.Default.Tune)
                    )
                ),
                FilterField.SortField(
                    id = FIELD_SORT,
                    titleEn = "Sort Order",
                    titleBn = "সাজানোর ক্রম",
                    icon = Icons.AutoMirrored.Filled.Sort,
                    options = SORT_OPTIONS,
                    defaultSortId = SORT_AMOUNT_DESC
                )
            )
        )
    }

    // Helper Getters

    fun getTypeModeId(state: FilterState): String {
        val active = (state[FIELD_TYPE_MODE] as? FilterValue.ToggleGroup)?.activeIds ?: emptySet()
        return active.firstOrNull() ?: MODE_ALL
    }

    fun resolveDateBounds(spec: FilterSpec<Any>, state: FilterState): Pair<Long, Long> {
        val dateVal = state[FIELD_DATE] as? FilterValue.Date
        if (dateVal?.startMs != null && dateVal.endMs != null) {
            return Pair(dateVal.startMs, dateVal.endMs)
        }
        val dateField = spec.fields.firstOrNull { it.id == FIELD_DATE } as? FilterField.DateField<Any>
        val presetId = dateVal?.presetId ?: dateField?.defaultPresetId ?: "this_month"
        val preset = dateField?.presets?.firstOrNull { it.id == presetId }
        return preset?.calculateRange?.invoke() ?: Pair(0L, Long.MAX_VALUE)
    }

    fun getTransactionType(state: FilterState): TransactionType? {
        val selId = when (val v = state[FIELD_TRANSACTION_TYPE]) {
            is FilterValue.SingleSelect -> v.selectedId
            is FilterValue.Select -> v.selectedIds.firstOrNull()
            else -> null
        }
        return selId?.let {
            try { TransactionType.valueOf(it) } catch (_: Exception) { null }
        }
    }

    fun getSelectedAccountIds(state: FilterState): Set<Long> {
        val active = (state[FIELD_ACCOUNTS] as? FilterValue.Select)?.selectedIds ?: emptySet()
        return active.mapNotNull { it.toLongOrNull() }.toSet()
    }

    fun getSelectedCategoryIds(state: FilterState): Set<Long> {
        val active = (state[FIELD_CATEGORIES] as? FilterValue.Select)?.selectedIds ?: emptySet()
        return active.mapNotNull { it.toLongOrNull() }.toSet()
    }

    fun getSelectedStatuses(state: FilterState): Set<TransactionStatus> {
        val active = (state[FIELD_STATUSES] as? FilterValue.Select)?.selectedIds ?: emptySet()
        return active.mapNotNull {
            try { TransactionStatus.valueOf(it) } catch (_: Exception) { null }
        }.toSet()
    }

    fun getMinAmount(state: FilterState): Double? {
        return (state[FIELD_AMOUNT_RANGE] as? FilterValue.Range)?.min
    }

    fun getMaxAmount(state: FilterState): Double? {
        return (state[FIELD_AMOUNT_RANGE] as? FilterValue.Range)?.max
    }

    fun getExcludeZero(state: FilterState): Boolean {
        val active = (state[FIELD_EXCLUDE_ZERO] as? FilterValue.ToggleGroup)?.activeIds ?: emptySet()
        return active.contains(EXCLUDE_ZERO_ID)
    }

    fun getSortOrder(state: FilterState): AggregatedSortOrder {
        val sortId = (state[FIELD_SORT] as? FilterValue.Sort)?.sortId ?: SORT_AMOUNT_DESC
        return when (sortId) {
            SORT_AMOUNT_ASC -> AggregatedSortOrder.AMOUNT_ASC
            SORT_COUNT_DESC -> AggregatedSortOrder.COUNT_DESC
            SORT_COUNT_ASC -> AggregatedSortOrder.COUNT_ASC
            SORT_AVG_DESC -> AggregatedSortOrder.AVG_DESC
            SORT_AVG_ASC -> AggregatedSortOrder.AVG_ASC
            SORT_NAME_ASC -> AggregatedSortOrder.NAME_ASC
            SORT_NAME_DESC -> AggregatedSortOrder.NAME_DESC
            SORT_RECENT_DATE -> AggregatedSortOrder.RECENT_DATE
            SORT_DEFAULT -> AggregatedSortOrder.DEFAULT
            else -> AggregatedSortOrder.AMOUNT_DESC
        }
    }

    fun getSearchQuery(state: FilterState): String {
        return (state[FIELD_SEARCH] as? FilterValue.Search)?.query ?: ""
    }

    // Helper State Builders

    fun withTypeMode(state: FilterState, mode: String): FilterState {
        val normalized = mode.lowercase()
        val validMode = if (normalized in listOf(MODE_EXPENSE, MODE_ALL, MODE_INCOME)) normalized else MODE_ALL
        return state + (FIELD_TYPE_MODE to FilterValue.ToggleGroup(setOf(validMode)))
    }

    fun withSearchQuery(state: FilterState, query: String): FilterState {
        return if (query.isBlank()) {
            state - FIELD_SEARCH
        } else {
            state + (FIELD_SEARCH to FilterValue.Search(query))
        }
    }

    fun withSortOrder(state: FilterState, sortOrder: AggregatedSortOrder): FilterState {
        val sortId = when (sortOrder) {
            AggregatedSortOrder.AMOUNT_ASC -> SORT_AMOUNT_ASC
            AggregatedSortOrder.COUNT_DESC -> SORT_COUNT_DESC
            AggregatedSortOrder.COUNT_ASC -> SORT_COUNT_ASC
            AggregatedSortOrder.AVG_DESC -> SORT_AVG_DESC
            AggregatedSortOrder.AVG_ASC -> SORT_AVG_ASC
            AggregatedSortOrder.NAME_ASC -> SORT_NAME_ASC
            AggregatedSortOrder.NAME_DESC -> SORT_NAME_DESC
            AggregatedSortOrder.RECENT_DATE -> SORT_RECENT_DATE
            AggregatedSortOrder.DEFAULT -> SORT_DEFAULT
            AggregatedSortOrder.AMOUNT_DESC -> SORT_AMOUNT_DESC
        }
        return state + (FIELD_SORT to FilterValue.Sort(sortId))
    }
}
