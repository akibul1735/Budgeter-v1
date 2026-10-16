package com.example.ui.components.filter.specs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.LabelOff
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Tag
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
import com.example.util.DateUtils
import java.util.Calendar

/**
 * Filter specification for LabelsScreen.
 */
object LabelsFilterSpec {

    const val SPEC_KEY = "labels"

    // Field IDs
    const val FIELD_SEARCH = "search"
    const val FIELD_SEGMENT = "segment"
    const val FIELD_TYPE_MODE = "type_mode"
    const val FIELD_DATE = "date"
    const val FIELD_TRANSACTION_TYPE = "transaction_type"
    const val FIELD_ACCOUNTS = "accounts"
    const val FIELD_CATEGORIES = "categories"
    const val FIELD_STATUSES = "statuses"
    const val FIELD_AMOUNT_RANGE = "amount_range"
    const val FIELD_EXCLUDE_ZERO = "exclude_zero"
    const val FIELD_SORT = "sort"

    // Segment condition IDs
    const val SEGMENT_HASHTAGS = "hashtags"
    const val SEGMENT_NOTES = "notes"
    const val SEGMENT_PAYEES = "payees"
    const val SEGMENT_UNTAGGED = "untagged"
    const val SEGMENT_ALL = "all"

    // Tab mode condition IDs
    const val MODE_EXPENSE = "expense"
    const val MODE_ALL = "all"
    const val MODE_INCOME = "income"

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
            SelectItemOption("COMPLETED", "Completed", "সম্পন্ন"),
            SelectItemOption("PENDING", "Pending", "অপেক্ষারত"),
            SelectItemOption("CANCELLED", "Cancelled", "বাতিল")
        )

        val sortOptions: List<SortOptionItem<Any>> = listOf(
            SortOptionItem("amount_desc", "Amount: High → Low", "পরিমাণ: বেশি → কম"),
            SortOptionItem("amount_asc", "Amount: Low → High", "পরিমাণ: কম → বেশি"),
            SortOptionItem("count_desc", "Count: Most Frequent", "লেনদেন: বেশি → কম"),
            SortOptionItem("count_asc", "Count: Least Frequent", "লেনদেন: কম → বেশি"),
            SortOptionItem("avg_desc", "Average: High → Low", "গড়: বেশি → কম"),
            SortOptionItem("avg_asc", "Average: Low → High", "গড়: কম → বেশি"),
            SortOptionItem("name_asc", "Name: A → Z", "নাম: A → Z"),
            SortOptionItem("name_desc", "Name: Z → A", "নাম: Z → A"),
            SortOptionItem("recent_date", "Recent Activity", "সাম্প্রতিক লেনদেন")
        )

        return FilterSpec(
            key = SPEC_KEY,
            titleEn = "Labels Filter",
            titleBn = "লেবেল ফিল্টার",
            subtitleEn = "Tags, Notes & Payee Classifications",
            subtitleBn = "ট্যাগ, নোট ও প্রাপক বিন্যাস",
            icon = Icons.Default.Label,
            accentColor = Color(0xFFE91E63),
            fields = listOf(
                FilterField.SearchField(
                    id = FIELD_SEARCH,
                    titleEn = "Search",
                    titleBn = "অনুসন্ধান",
                    hintEn = "Search labels/notes...",
                    hintBn = "লেবেল / নোট খুঁজুন...",
                    icon = Icons.Default.Search
                ),
                FilterField.ToggleGroupField(
                    id = FIELD_SEGMENT,
                    titleEn = "Label Category",
                    titleBn = "লেবেল বিভাগ",
                    icon = Icons.Default.Tag,
                    conditions = listOf(
                        FilterCondition(SEGMENT_HASHTAGS, "Hashtags", "হ্যাশট্যাগ ও লেবেল", icon = Icons.Default.Tag, group = "segment"),
                        FilterCondition(SEGMENT_NOTES, "Notes", "নোট (Notes)", icon = Icons.Default.Description, group = "segment"),
                        FilterCondition(SEGMENT_PAYEES, "Payees", "প্রাপক / ব্যক্তি", icon = Icons.Default.Person, group = "segment"),
                        FilterCondition(SEGMENT_UNTAGGED, "Unlabeled", "লেবেলহীন", icon = Icons.Default.LabelOff, group = "segment"),
                        FilterCondition(SEGMENT_ALL, "All", "সকল (All)", icon = Icons.Default.Label, group = "segment")
                    ),
                    defaultConditionIds = setOf(SEGMENT_HASHTAGS)
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
                    ),
                    defaultConditionIds = setOf(MODE_ALL)
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
                    titleEn = "Display Preferences",
                    titleBn = "প্রদর্শন পছন্দসমূহ",
                    icon = Icons.Default.Tune,
                    conditions = listOf(
                        FilterCondition("exclude", "Exclude Zero Amounts", "শূন্য পরিমাণ বাদ দিন", icon = Icons.Default.Tune)
                    )
                ),
                FilterField.SortField(
                    id = FIELD_SORT,
                    titleEn = "Sort Order",
                    titleBn = "সাজানোর ক্রম",
                    icon = Icons.Default.Sort,
                    options = sortOptions,
                    defaultSortId = "amount_desc"
                )
            )
        )
    }

    /**
     * Resolves start and end epoch milliseconds from FilterState.
     */
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

    /**
     * Helper to read selected segment string (hashtags, notes, payees, untagged, all).
     */
    fun getSelectedSegmentId(state: FilterState): String {
        val active = (state[FIELD_SEGMENT] as? FilterValue.ToggleGroup)?.activeIds ?: emptySet()
        return active.firstOrNull() ?: SEGMENT_HASHTAGS
    }

    /**
     * Helper to read selected type mode string (expense, all, income).
     */
    fun getTypeModeId(state: FilterState): String {
        val active = (state[FIELD_TYPE_MODE] as? FilterValue.ToggleGroup)?.activeIds ?: emptySet()
        return active.firstOrNull() ?: MODE_ALL
    }

    /**
     * Helper to read search query.
     */
    fun getSearchQuery(state: FilterState): String {
        return (state[FIELD_SEARCH] as? FilterValue.Search)?.query ?: ""
    }

    /**
     * Helper to read sort option ID.
     */
    fun getSortOrderId(state: FilterState): String {
        return (state[FIELD_SORT] as? FilterValue.Sort)?.sortId ?: "amount_desc"
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
            try {
                when (it) {
                    "COMPLETED" -> TransactionStatus.CLEARED
                    "PENDING" -> TransactionStatus.VOID
                    "CANCELLED" -> TransactionStatus.VOID
                    else -> TransactionStatus.valueOf(it)
                }
            } catch (_: Exception) { null }
        }.toSet()
    }

    fun getSelectedStatusNames(state: FilterState): Set<String> {
        return (state[FIELD_STATUSES] as? FilterValue.Select)?.selectedIds ?: emptySet()
    }

    fun getMinAmount(state: FilterState): Double? {
        return (state[FIELD_AMOUNT_RANGE] as? FilterValue.Range)?.min
    }

    fun getMaxAmount(state: FilterState): Double? {
        return (state[FIELD_AMOUNT_RANGE] as? FilterValue.Range)?.max
    }

    fun getExcludeZero(state: FilterState): Boolean {
        val active = (state[FIELD_EXCLUDE_ZERO] as? FilterValue.ToggleGroup)?.activeIds ?: emptySet()
        return active.contains("exclude")
    }

    fun withSearchQuery(state: FilterState, query: String): FilterState {
        return if (query.isBlank()) {
            state - FIELD_SEARCH
        } else {
            state + (FIELD_SEARCH to FilterValue.Search(query))
        }
    }

    fun withSegment(state: FilterState, segmentId: String): FilterState {
        return if (segmentId == SEGMENT_HASHTAGS) {
            state - FIELD_SEGMENT
        } else {
            state + (FIELD_SEGMENT to FilterValue.ToggleGroup(setOf(segmentId)))
        }
    }

    fun withTypeMode(state: FilterState, mode: String): FilterState {
        val normalized = mode.lowercase()
        return if (normalized == MODE_ALL || normalized !in listOf(MODE_EXPENSE, MODE_ALL, MODE_INCOME)) {
            state - FIELD_TYPE_MODE
        } else {
            state + (FIELD_TYPE_MODE to FilterValue.ToggleGroup(setOf(normalized)))
        }
    }

    fun withSortOrderId(state: FilterState, sortId: String): FilterState {
        return if (sortId == "amount_desc") {
            state - FIELD_SORT
        } else {
            state + (FIELD_SORT to FilterValue.Sort(sortId))
        }
    }

    fun withTransactionType(state: FilterState, type: TransactionType?): FilterState {
        return if (type == null) state - FIELD_TRANSACTION_TYPE else state + (FIELD_TRANSACTION_TYPE to FilterValue.SingleSelect(type.name))
    }

    fun withAccountIds(state: FilterState, ids: Set<Long>): FilterState {
        return if (ids.isEmpty()) state - FIELD_ACCOUNTS else state + (FIELD_ACCOUNTS to FilterValue.Select(ids.map { it.toString() }.toSet()))
    }

    fun withCategoryIds(state: FilterState, ids: Set<Long>): FilterState {
        return if (ids.isEmpty()) state - FIELD_CATEGORIES else state + (FIELD_CATEGORIES to FilterValue.Select(ids.map { it.toString() }.toSet()))
    }

    fun withStatuses(state: FilterState, statuses: Set<String>): FilterState {
        return if (statuses.isEmpty()) state - FIELD_STATUSES else state + (FIELD_STATUSES to FilterValue.Select(statuses))
    }

    fun withAmountRange(state: FilterState, min: Double?, max: Double?): FilterState {
        return if (min == null && max == null) state - FIELD_AMOUNT_RANGE else state + (FIELD_AMOUNT_RANGE to FilterValue.Range(min = min, max = max))
    }

    fun withDatePreset(state: FilterState, presetId: String): FilterState {
        return if (presetId == "this_month") {
            state - FIELD_DATE
        } else {
            state + (FIELD_DATE to FilterValue.Date(presetId = presetId))
        }
    }

    fun withCustomDates(state: FilterState, startMs: Long, endMs: Long): FilterState {
        return state + (FIELD_DATE to FilterValue.Date(presetId = "custom", startMs = startMs, endMs = endMs))
    }

    fun withExcludeZero(state: FilterState, exclude: Boolean): FilterState {
        return if (exclude) {
            state + (FIELD_EXCLUDE_ZERO to FilterValue.ToggleGroup(setOf("exclude")))
        } else {
            state - FIELD_EXCLUDE_ZERO
        }
    }

    fun activeFilterCount(state: FilterState): Int {
        var count = 0
        if (getSearchQuery(state).isNotBlank()) count++
        if (getSelectedSegmentId(state) != SEGMENT_HASHTAGS) count++
        if (getTypeModeId(state) != MODE_ALL) count++
        val dateVal = state[FIELD_DATE] as? FilterValue.Date
        if ((dateVal?.presetId != null && dateVal.presetId != "this_month") || dateVal?.startMs != null) count++
        if (getSelectedAccountIds(state).isNotEmpty()) count++
        if (getSelectedCategoryIds(state).isNotEmpty()) count++
        if (getSelectedStatusNames(state).isNotEmpty()) count++
        if (getMinAmount(state) != null || getMaxAmount(state) != null) count++
        if (getExcludeZero(state)) count++
        val sortId = (state[FIELD_SORT] as? FilterValue.Sort)?.sortId ?: "amount_desc"
        if (sortId != "amount_desc") count++
        return count
    }
}
