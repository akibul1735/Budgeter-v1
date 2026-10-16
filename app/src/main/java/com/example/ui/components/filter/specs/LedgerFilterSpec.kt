package com.example.ui.components.filter.specs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TableRows
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.ui.graphics.Color
import com.example.data.model.Account
import com.example.data.model.Category
import com.example.data.model.CategoryType
import com.example.data.model.LanguageMode
import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionType
import com.example.data.model.TransactionWithDetails
import com.example.ui.components.filter.DatePresetOption
import com.example.ui.components.filter.FilterCondition
import com.example.ui.components.filter.FilterField
import com.example.ui.components.filter.FilterSpec
import com.example.ui.components.filter.FilterState
import com.example.ui.components.filter.FilterValue
import com.example.ui.components.filter.SelectItemOption
import com.example.ui.components.filter.SortOptionItem
import com.example.ui.dialogs.computePresetDateBounds
import com.example.ui.screens.LedgerDatePreset
import com.example.ui.screens.LedgerRowStyle
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidPrimary
import com.example.ui.theme.SolidTransfer

/**
 * Universal 3-Layer Filter Specification for Transactions (LedgerScreen).
 */
object LedgerFilterSpec {

    const val SPEC_KEY = "ledger"

    // Field identifiers
    const val FIELD_SEARCH = "search"
    const val FIELD_TYPE = "type"
    const val FIELD_DATE = "date"
    const val FIELD_ACCOUNTS = "accounts"
    const val FIELD_CATEGORIES = "categories"
    const val FIELD_LABELS = "labels"
    const val FIELD_STATUSES = "statuses"
    const val FIELD_AMOUNT_RANGE = "amount_range"
    const val FIELD_SORT = "sort"
    const val FIELD_ROW_STYLE = "row_style"
    const val FIELD_SHOW_TOTALS = "show_totals"

    // Type Options
    const val TYPE_ALL = "all"
    const val TYPE_EXPENSE = "expense"
    const val TYPE_INCOME = "income"
    const val TYPE_TRANSFER = "transfer"

    // Date Preset Constants
    const val PRESET_THIS_MONTH = "this_month"
    const val PRESET_LAST_MONTH = "last_month"
    const val PRESET_LAST_12_MONTHS = "last_12_months"
    const val PRESET_ALL_TIME = "all_time"
    const val PRESET_TODAY = "today"
    const val PRESET_YESTERDAY = "yesterday"
    const val PRESET_THIS_WEEK = "this_week"
    const val PRESET_LAST_WEEK = "last_week"
    const val PRESET_THIS_QUARTER = "this_quarter"
    const val PRESET_THIS_YEAR = "this_year"
    const val PRESET_LAST_YEAR = "last_year"
    const val PRESET_SINCE_LAST_YEAR = "since_last_year"
    const val PRESET_CUSTOM = "custom"

    // Sort Options
    const val SORT_NEWEST_FIRST = "newest_first"
    const val SORT_OLDEST_FIRST = "oldest_first"
    const val SORT_AMOUNT_DESC = "amount_desc"
    const val SORT_AMOUNT_ASC = "amount_asc"

    // Row Styles
    const val STYLE_STANDARD = "standard"
    const val STYLE_COMPACT = "compact"
    const val STYLE_DETAILED = "detailed"

    // Date Presets Mapping
    val DATE_PRESETS: List<DatePresetOption> = listOf(
        DatePresetOption(PRESET_THIS_MONTH, "This Month", "চলতি মাস") {
            computePresetDateBounds(LedgerDatePreset.THIS_MONTH)
        },
        DatePresetOption(PRESET_LAST_MONTH, "Last Month", "গত মাস") {
            computePresetDateBounds(LedgerDatePreset.LAST_MONTH)
        },
        DatePresetOption(PRESET_LAST_12_MONTHS, "Last 12 Months", "বিগত ১২ মাস") {
            computePresetDateBounds(LedgerDatePreset.LAST_12_MONTHS)
        },
        DatePresetOption(PRESET_THIS_WEEK, "This Week", "চলতি সপ্তাহ") {
            computePresetDateBounds(LedgerDatePreset.THIS_WEEK)
        },
        DatePresetOption(PRESET_LAST_WEEK, "Last Week", "গত সপ্তাহ") {
            computePresetDateBounds(LedgerDatePreset.LAST_WEEK)
        },
        DatePresetOption(PRESET_TODAY, "Today", "আজকে") {
            computePresetDateBounds(LedgerDatePreset.TODAY)
        },
        DatePresetOption(PRESET_YESTERDAY, "Yesterday", "গতকাল") {
            computePresetDateBounds(LedgerDatePreset.YESTERDAY)
        },
        DatePresetOption(PRESET_THIS_QUARTER, "This Quarter", "চলতি ত্রৈমাসিক") {
            computePresetDateBounds(LedgerDatePreset.THIS_QUARTER)
        },
        DatePresetOption(PRESET_THIS_YEAR, "This Year", "চলতি বছর") {
            computePresetDateBounds(LedgerDatePreset.THIS_YEAR)
        },
        DatePresetOption(PRESET_LAST_YEAR, "Last Year", "গত বছর") {
            computePresetDateBounds(LedgerDatePreset.LAST_YEAR)
        },
        DatePresetOption(PRESET_SINCE_LAST_YEAR, "Since Last Year", "গত বছর থেকে") {
            computePresetDateBounds(LedgerDatePreset.SINCE_LAST_YEAR)
        },
        DatePresetOption(PRESET_ALL_TIME, "All Time", "সব সময়") {
            computePresetDateBounds(LedgerDatePreset.ALL_TIME)
        },
        DatePresetOption(PRESET_CUSTOM, "Custom Range", "কাস্টম সময়সীমা") {
            Pair(0L, System.currentTimeMillis())
        }
    )

    // Type Conditions
    val TYPE_CONDITIONS: List<FilterCondition<TransactionWithDetails>> = listOf(
        FilterCondition(TYPE_ALL, "All", "সব", group = "tx_type"),
        FilterCondition(TYPE_EXPENSE, "Expense", "ব্যয়", icon = Icons.Default.TrendingDown, group = "tx_type") {
            it.transaction.type == TransactionType.EXPENSE
        },
        FilterCondition(TYPE_INCOME, "Income", "আয়", icon = Icons.Default.TrendingUp, group = "tx_type") {
            it.transaction.type == TransactionType.INCOME
        },
        FilterCondition(TYPE_TRANSFER, "Transfer", "স্থানান্তর", icon = Icons.Default.SwapHoriz, group = "tx_type") {
            it.transaction.type == TransactionType.TRANSFER
        }
    )

    // Row Style Conditions
    val ROW_STYLE_CONDITIONS: List<FilterCondition<TransactionWithDetails>> = listOf(
        FilterCondition(STYLE_STANDARD, "Standard View", "সাধারণ ভিউ", icon = Icons.Default.TableRows, group = "row_style"),
        FilterCondition(STYLE_COMPACT, "Compact View", "কমপ্যাক্ট ভিউ", icon = Icons.Default.Tune, group = "row_style"),
        FilterCondition(STYLE_DETAILED, "Detailed View", "বিস্তারিত ভিউ", icon = Icons.Default.TableRows, group = "row_style")
    )

    // Sort Options
    val SORT_OPTIONS: List<SortOptionItem<TransactionWithDetails>> = listOf(
        SortOptionItem(SORT_NEWEST_FIRST, "Date: Newest First", "তারিখ: নতুন থেকে পুরানো", icon = Icons.AutoMirrored.Filled.Sort),
        SortOptionItem(SORT_OLDEST_FIRST, "Date: Oldest First", "তারিখ: পুরানো থেকে নতুন", icon = Icons.AutoMirrored.Filled.Sort),
        SortOptionItem(SORT_AMOUNT_DESC, "Amount: High to Low", "পরিমাণ: বেশি থেকে কম", icon = Icons.AutoMirrored.Filled.Sort),
        SortOptionItem(SORT_AMOUNT_ASC, "Amount: Low to High", "পরিমাণ: কম থেকে বেশি", icon = Icons.AutoMirrored.Filled.Sort)
    )

    // Status Options
    val STATUS_OPTIONS: List<SelectItemOption> = listOf(
        SelectItemOption(TransactionStatus.CLEARED.name, "Cleared", "নিষ্পন্ন", icon = Icons.Default.CheckCircle),
        SelectItemOption(TransactionStatus.RECONCILED.name, "Reconciled", "মিলিত", icon = Icons.Default.CheckCircle),
        SelectItemOption(TransactionStatus.VOID.name, "Void", "বাতিল", icon = Icons.Default.CheckCircle)
    )

    /**
     * Builds declarative FilterSpec for Transactions / Ledger tab.
     */
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
                subtitleEn = acc.accountNumber,
                subtitleBn = acc.accountNumber,
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

        // 1. Search Field
        fields.add(
            FilterField.SearchField(
                id = FIELD_SEARCH,
                titleEn = "Search",
                titleBn = "অনুসন্ধান",
                hintEn = "Search note, payee, reference...",
                hintBn = "নোট, পেয়ী বা রেফারেন্স খুঁজুন...",
                icon = Icons.Default.Search,
                predicate = { item, query ->
                    if (query.isBlank()) true else {
                        val q = query.trim().lowercase()
                        val tx = item.transaction
                        tx.note.lowercase().contains(q) ||
                                tx.payeeOrPayer.lowercase().contains(q) ||
                                tx.referenceNo.lowercase().contains(q) ||
                                (item.category?.nameEn?.lowercase()?.contains(q) == true) ||
                                (item.category?.nameBn?.lowercase()?.contains(q) == true) ||
                                (item.subCategory?.nameEn?.lowercase()?.contains(q) == true) ||
                                (item.subCategory?.nameBn?.lowercase()?.contains(q) == true) ||
                                (item.debitAccount?.nameEn?.lowercase()?.contains(q) == true) ||
                                (item.debitAccount?.nameBn?.lowercase()?.contains(q) == true) ||
                                (item.creditAccount?.nameEn?.lowercase()?.contains(q) == true) ||
                                (item.creditAccount?.nameBn?.lowercase()?.contains(q) == true)
                    }
                }
            )
        )

        // 2. Transaction Type
        fields.add(
            FilterField.ToggleGroupField(
                id = FIELD_TYPE,
                titleEn = "Transaction Type",
                titleBn = "লেনদেনের ধরন",
                icon = Icons.Default.SwapHoriz,
                conditions = TYPE_CONDITIONS,
                defaultConditionIds = setOf(TYPE_ALL)
            )
        )

        // 3. Date Range
        fields.add(
            FilterField.DateField(
                id = FIELD_DATE,
                titleEn = "Date Range",
                titleBn = "সময়সীমা",
                icon = Icons.Default.CalendarMonth,
                presets = DATE_PRESETS,
                defaultPresetId = PRESET_LAST_12_MONTHS,
                allowCustom = true,
                predicate = { item, startMs, endMs ->
                    item.transaction.dateEpochMs in startMs..endMs
                }
            )
        )

        // 4. Accounts Multi-Select
        if (accountOptions.isNotEmpty()) {
            fields.add(
                FilterField.SelectField(
                    id = FIELD_ACCOUNTS,
                    titleEn = "Accounts",
                    titleBn = "অ্যাকাউন্ট",
                    subtitleEn = "Filter by associated payment accounts",
                    subtitleBn = "নির্দিষ্ট হিসাব অনুযায়ী লেনদেন ফিল্টার করুন",
                    icon = Icons.Default.AccountBalance,
                    items = accountOptions,
                    isMultiSelect = true,
                    predicate = { item, selected ->
                        if (selected.isEmpty()) true else {
                            val dId = item.transaction.debitAccountId?.toString()
                            val cId = item.transaction.creditAccountId?.toString()
                            (dId != null && selected.contains(dId)) || (cId != null && selected.contains(cId))
                        }
                    }
                )
            )
        }

        // 5. Categories Multi-Select
        if (categoryOptions.isNotEmpty()) {
            fields.add(
                FilterField.SelectField(
                    id = FIELD_CATEGORIES,
                    titleEn = "Categories",
                    titleBn = "ক্যাটাগরি",
                    subtitleEn = "Filter by income or expense category",
                    subtitleBn = "ক্যাটাগরি অনুযায়ী লেনদেন দেখুন",
                    icon = Icons.Default.Category,
                    items = categoryOptions,
                    isMultiSelect = true,
                    predicate = { item, selected ->
                        if (selected.isEmpty()) true else {
                            val catId = item.transaction.categoryId?.toString()
                            val subCatId = item.transaction.subCategoryId?.toString()
                            (catId != null && selected.contains(catId)) || (subCatId != null && selected.contains(subCatId))
                        }
                    }
                )
            )
        }

        // 6. Labels & Tags
        if (labelOptions.isNotEmpty()) {
            fields.add(
                FilterField.SelectField(
                    id = FIELD_LABELS,
                    titleEn = "Labels & Tags",
                    titleBn = "লেবেল ও ট্যাগ",
                    subtitleEn = "Filter by tags in notes or reference",
                    subtitleBn = "ট্যাগ বা রেফারেন্স অনুযায়ী দেখুন",
                    icon = Icons.Default.Label,
                    items = labelOptions,
                    isMultiSelect = true,
                    predicate = { item, selected ->
                        if (selected.isEmpty()) true else {
                            val note = item.transaction.note
                            val ref = item.transaction.referenceNo
                            selected.any { tag -> note.contains(tag, ignoreCase = true) || ref.contains(tag, ignoreCase = true) }
                        }
                    }
                )
            )
        }

        // 7. Status Multi-Select
        fields.add(
            FilterField.SelectField(
                id = FIELD_STATUSES,
                titleEn = "Verification Status",
                titleBn = "যাচাই অবস্থা",
                subtitleEn = "Cleared, Reconciled, or Void",
                subtitleBn = "নিষ্পন্ন, মিলিত বা বাতিল লেনদেন",
                icon = Icons.Default.CheckCircle,
                items = STATUS_OPTIONS,
                isMultiSelect = true,
                predicate = { item, selected ->
                    if (selected.isEmpty()) true else {
                        selected.contains(item.transaction.status.name)
                    }
                }
            )
        )

        // 8. Amount Range Field
        fields.add(
            FilterField.RangeField(
                id = FIELD_AMOUNT_RANGE,
                titleEn = "Amount Range",
                titleBn = "পরিমাণ সীমা",
                prefix = "৳",
                minHintEn = "Min ৳",
                minHintBn = "সর্বনিম্ন ৳",
                maxHintEn = "Max ৳",
                maxHintBn = "সর্বোচ্চ ৳",
                predicate = { item, min, max ->
                    val amt = Math.abs(item.transaction.amount)
                    val passMin = min == null || amt >= min
                    val passMax = max == null || amt <= max
                    passMin && passMax
                }
            )
        )

        // 9. Sort Order Field
        fields.add(
            FilterField.SortField(
                id = FIELD_SORT,
                titleEn = "Sort Order",
                titleBn = "সাজানোর ক্রম",
                icon = Icons.AutoMirrored.Filled.Sort,
                options = SORT_OPTIONS,
                defaultSortId = SORT_NEWEST_FIRST
            )
        )

        // 10. Row Display Style
        fields.add(
            FilterField.ToggleGroupField(
                id = FIELD_ROW_STYLE,
                titleEn = "Row Display Style",
                titleBn = "তালিকার ডিসপ্লে ধরন",
                icon = Icons.Default.TableRows,
                conditions = ROW_STYLE_CONDITIONS,
                defaultConditionIds = setOf(STYLE_STANDARD)
            )
        )

        // 11. Show Total Amount
        fields.add(
            FilterField.BooleanField(
                id = FIELD_SHOW_TOTALS,
                titleEn = "Show Total Summary",
                titleBn = "মোট হিসাব প্রদর্শন করুন",
                subtitleEn = "Show summary aggregate pill on ledger screen",
                subtitleBn = "স্ক্রিনে ফিল্টারকৃত লেনদেনের মোট পরিমাণ দেখান",
                icon = Icons.Default.Tune,
                defaultValue = true
            )
        )

        return FilterSpec(
            key = SPEC_KEY,
            titleEn = "Transactions Filter",
            titleBn = "লেনদেন ফিল্টার",
            accentColor = SolidPrimary,
            fields = fields
        )
    }

    // -------------------------------------------------------------------------
    // Value Extractors
    // -------------------------------------------------------------------------

    fun getSearchQuery(state: FilterState): String {
        return (state[FIELD_SEARCH] as? FilterValue.Search)?.query ?: ""
    }

    fun getTransactionType(state: FilterState): TransactionType? {
        val activeIds = (state[FIELD_TYPE] as? FilterValue.ToggleGroup)?.activeIds ?: emptySet()
        return when {
            activeIds.contains(TYPE_EXPENSE) -> TransactionType.EXPENSE
            activeIds.contains(TYPE_INCOME) -> TransactionType.INCOME
            activeIds.contains(TYPE_TRANSFER) -> TransactionType.TRANSFER
            else -> null
        }
    }

    fun getDatePreset(state: FilterState): LedgerDatePreset {
        val dateVal = state[FIELD_DATE] as? FilterValue.Date
        val presetId = dateVal?.presetId ?: PRESET_LAST_12_MONTHS
        return when (presetId) {
            PRESET_THIS_MONTH -> LedgerDatePreset.THIS_MONTH
            PRESET_LAST_MONTH -> LedgerDatePreset.LAST_MONTH
            PRESET_LAST_12_MONTHS -> LedgerDatePreset.LAST_12_MONTHS
            PRESET_THIS_WEEK -> LedgerDatePreset.THIS_WEEK
            PRESET_LAST_WEEK -> LedgerDatePreset.LAST_WEEK
            PRESET_TODAY -> LedgerDatePreset.TODAY
            PRESET_YESTERDAY -> LedgerDatePreset.YESTERDAY
            PRESET_THIS_QUARTER -> LedgerDatePreset.THIS_QUARTER
            PRESET_THIS_YEAR -> LedgerDatePreset.THIS_YEAR
            PRESET_LAST_YEAR -> LedgerDatePreset.LAST_YEAR
            PRESET_SINCE_LAST_YEAR -> LedgerDatePreset.SINCE_LAST_YEAR
            PRESET_ALL_TIME -> LedgerDatePreset.ALL_TIME
            PRESET_CUSTOM -> LedgerDatePreset.CUSTOM
            else -> LedgerDatePreset.LAST_12_MONTHS
        }
    }

    fun resolveDateBounds(spec: FilterSpec<*>, state: FilterState, defaultStartMs: Long = 0L, defaultEndMs: Long = System.currentTimeMillis()): Pair<Long, Long> {
        val dateVal = state[FIELD_DATE] as? FilterValue.Date
        if (dateVal != null) {
            if (dateVal.presetId == PRESET_CUSTOM && dateVal.startMs != null && dateVal.endMs != null) {
                return Pair(dateVal.startMs, dateVal.endMs)
            }
            val dateField = spec.fields.filterIsInstance<FilterField.DateField<*>>().firstOrNull { it.id == FIELD_DATE }
            val preset = dateField?.presets?.firstOrNull { it.id == dateVal.presetId }
            if (preset != null) {
                return preset.calculateRange()
            }
        }
        val preset = getDatePreset(state)
        return computePresetDateBounds(preset)
    }

    fun getSelectedAccountIds(state: FilterState): Set<Long> {
        val sel = (state[FIELD_ACCOUNTS] as? FilterValue.Select)?.selectedIds ?: emptySet()
        return sel.mapNotNull { it.toLongOrNull() }.toSet()
    }

    fun getSelectedCategoryIds(state: FilterState): Set<Long> {
        val sel = (state[FIELD_CATEGORIES] as? FilterValue.Select)?.selectedIds ?: emptySet()
        return sel.mapNotNull { it.toLongOrNull() }.toSet()
    }

    fun getSelectedLabels(state: FilterState): Set<String> {
        return (state[FIELD_LABELS] as? FilterValue.Select)?.selectedIds ?: emptySet()
    }

    fun getSelectedStatuses(state: FilterState): Set<TransactionStatus> {
        val sel = (state[FIELD_STATUSES] as? FilterValue.Select)?.selectedIds ?: emptySet()
        return sel.mapNotNull {
            try { TransactionStatus.valueOf(it) } catch (_: Exception) { null }
        }.toSet()
    }

    fun getAmountRange(state: FilterState): Pair<Double?, Double?> {
        val range = state[FIELD_AMOUNT_RANGE] as? FilterValue.Range
        return Pair(range?.min, range?.max)
    }

    fun getSortOrder(state: FilterState): String {
        return (state[FIELD_SORT] as? FilterValue.Sort)?.sortId ?: SORT_NEWEST_FIRST
    }

    fun getRowStyle(state: FilterState): LedgerRowStyle {
        val activeIds = (state[FIELD_ROW_STYLE] as? FilterValue.ToggleGroup)?.activeIds ?: emptySet()
        return when {
            activeIds.contains(STYLE_COMPACT) -> LedgerRowStyle.COMPACT
            activeIds.contains(STYLE_DETAILED) -> LedgerRowStyle.DETAILED
            else -> LedgerRowStyle.STANDARD
        }
    }

    fun getShowTotals(state: FilterState): Boolean {
        return (state[FIELD_SHOW_TOTALS] as? FilterValue.BooleanVal)?.value ?: true
    }

    // -------------------------------------------------------------------------
    // Value Mutators / Setters
    // -------------------------------------------------------------------------

    fun withSearchQuery(state: FilterState, query: String): FilterState {
        return if (query.isBlank()) state - FIELD_SEARCH
        else state + (FIELD_SEARCH to FilterValue.Search(query))
    }

    fun withType(state: FilterState, type: TransactionType?): FilterState {
        return if (type == null) {
            state - FIELD_TYPE
        } else {
            val id = when (type) {
                TransactionType.EXPENSE -> TYPE_EXPENSE
                TransactionType.INCOME -> TYPE_INCOME
                TransactionType.TRANSFER -> TYPE_TRANSFER
            }
            state + (FIELD_TYPE to FilterValue.ToggleGroup(setOf(id)))
        }
    }

    fun withDatePreset(state: FilterState, preset: LedgerDatePreset): FilterState {
        if (preset == LedgerDatePreset.LAST_12_MONTHS) {
            return state - FIELD_DATE
        }
        val id = when (preset) {
            LedgerDatePreset.THIS_MONTH -> PRESET_THIS_MONTH
            LedgerDatePreset.LAST_MONTH -> PRESET_LAST_MONTH
            LedgerDatePreset.LAST_12_MONTHS -> PRESET_LAST_12_MONTHS
            LedgerDatePreset.THIS_WEEK -> PRESET_THIS_WEEK
            LedgerDatePreset.LAST_WEEK -> PRESET_LAST_WEEK
            LedgerDatePreset.TODAY -> PRESET_TODAY
            LedgerDatePreset.YESTERDAY -> PRESET_YESTERDAY
            LedgerDatePreset.THIS_QUARTER -> PRESET_THIS_QUARTER
            LedgerDatePreset.THIS_YEAR -> PRESET_THIS_YEAR
            LedgerDatePreset.LAST_YEAR -> PRESET_LAST_YEAR
            LedgerDatePreset.SINCE_LAST_YEAR -> PRESET_SINCE_LAST_YEAR
            LedgerDatePreset.ALL_TIME -> PRESET_ALL_TIME
            LedgerDatePreset.CUSTOM -> PRESET_CUSTOM
        }
        val bounds = computePresetDateBounds(preset)
        return state + (FIELD_DATE to FilterValue.Date(presetId = id, startMs = bounds.first, endMs = bounds.second))
    }

    fun withCustomDates(state: FilterState, startMs: Long, endMs: Long): FilterState {
        return state + (FIELD_DATE to FilterValue.Date(presetId = PRESET_CUSTOM, startMs = startMs, endMs = endMs))
    }

    fun withAccountIds(state: FilterState, ids: Set<Long>): FilterState {
        return if (ids.isEmpty()) state - FIELD_ACCOUNTS
        else state + (FIELD_ACCOUNTS to FilterValue.Select(ids.map { it.toString() }.toSet()))
    }

    fun withCategoryIds(state: FilterState, ids: Set<Long>): FilterState {
        return if (ids.isEmpty()) state - FIELD_CATEGORIES
        else state + (FIELD_CATEGORIES to FilterValue.Select(ids.map { it.toString() }.toSet()))
    }

    fun withStatuses(state: FilterState, statuses: Set<TransactionStatus>): FilterState {
        return if (statuses.isEmpty()) state - FIELD_STATUSES
        else state + (FIELD_STATUSES to FilterValue.Select(statuses.map { it.name }.toSet()))
    }

    fun withSortOrder(state: FilterState, sortId: String): FilterState {
        return if (sortId == SORT_NEWEST_FIRST) state - FIELD_SORT
        else state + (FIELD_SORT to FilterValue.Sort(sortId))
    }

    fun withRowStyle(state: FilterState, style: LedgerRowStyle): FilterState {
        return if (style == LedgerRowStyle.STANDARD) {
            state - FIELD_ROW_STYLE
        } else {
            val id = when (style) {
                LedgerRowStyle.COMPACT -> STYLE_COMPACT
                LedgerRowStyle.DETAILED -> STYLE_DETAILED
                LedgerRowStyle.STANDARD -> STYLE_STANDARD
            }
            state + (FIELD_ROW_STYLE to FilterValue.ToggleGroup(setOf(id)))
        }
    }
}
