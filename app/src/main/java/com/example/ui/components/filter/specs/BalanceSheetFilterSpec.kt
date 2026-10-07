package com.example.ui.components.filter.specs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.graphics.Color
import com.example.data.model.Account
import com.example.data.model.LanguageMode
import com.example.data.model.TransactionStatus
import com.example.ui.components.filter.DatePresetOption
import com.example.ui.components.filter.FilterCondition
import com.example.ui.components.filter.FilterField
import com.example.ui.components.filter.FilterSpec
import com.example.ui.components.filter.FilterState
import com.example.ui.components.filter.FilterValue
import com.example.ui.components.filter.SelectItemOption
import com.example.ui.components.filter.SortOptionItem
import com.example.ui.theme.SolidPrimary
import com.example.util.BalanceSheetComparisonPreset
import com.example.util.BalanceSheetHelper
import com.example.util.BalanceSheetSortOrder

/**
 * Universal Filter Specification for BalanceSheetScreen.
 */
object BalanceSheetFilterSpec {
    const val SPEC_KEY = "balance_sheet"

    // Field IDs
    const val FIELD_SEARCH = "search"
    const val FIELD_DATE = "date"
    const val FIELD_ACCOUNTS = "accounts"
    const val FIELD_STATUSES = "statuses"
    const val FIELD_SORT = "sort"
    const val FIELD_EXCLUDE_ZERO = "exclude_zero"
    const val FIELD_FILTER_NON_ZERO_GROUPS = "filter_non_zero_groups"
    const val FIELD_SHOW_HIDDEN = "show_hidden"
    const val FIELD_ONLY_CURRENT = "only_current"
    const val FIELD_WITHOUT_GROUPS = "without_groups"
    const val FIELD_ACTIVE_TAB = "active_tab"

    // Tab Constants
    const val TAB_ALL = "all"
    const val TAB_ASSETS = "assets"
    const val TAB_LIABILITIES = "liabilities"

    // Date Preset IDs
    const val PRESET_THIS_MONTH = "this_month"
    const val PRESET_END_OF_LAST_MONTH = "end_of_last_month"
    const val PRESET_BEGINNING_OF_MONTH = "beginning_of_month"
    const val PRESET_LAST_12_MONTHS = "last_12_months"
    const val PRESET_PREVIOUS_MONTH = "previous_month"
    const val PRESET_BEGINNING_OF_YEAR = "beginning_of_year"
    const val PRESET_LAST_30_DAYS = "last_30_days"
    const val PRESET_TODAY = "today"
    const val PRESET_CUSTOM = "custom"

    // Sort IDs
    const val SORT_AMOUNT_DESC = "amount_desc"
    const val SORT_AMOUNT_ASC = "amount_asc"
    const val SORT_NAME_ASC = "name_asc"
    const val SORT_DEFAULT = "default"

    val DATE_PRESET_OPTIONS = listOf(
        DatePresetOption(
            id = PRESET_THIS_MONTH,
            titleEn = "This Month",
            titleBn = "চলতি মাস",
            calculateRange = { BalanceSheetHelper.getPresetDateRanges(BalanceSheetComparisonPreset.THIS_MONTH) }
        ),
        DatePresetOption(
            id = PRESET_END_OF_LAST_MONTH,
            titleEn = "End of Last Month",
            titleBn = "গত মাসের শেষ",
            calculateRange = { BalanceSheetHelper.getPresetDateRanges(BalanceSheetComparisonPreset.END_OF_LAST_MONTH) }
        ),
        DatePresetOption(
            id = PRESET_BEGINNING_OF_MONTH,
            titleEn = "Start of This Month",
            titleBn = "এই মাসের শুরু",
            calculateRange = { BalanceSheetHelper.getPresetDateRanges(BalanceSheetComparisonPreset.BEGINNING_OF_MONTH) }
        ),
        DatePresetOption(
            id = PRESET_LAST_12_MONTHS,
            titleEn = "Last 12 Months",
            titleBn = "গত ১২ মাস",
            calculateRange = { BalanceSheetHelper.getPresetDateRanges(BalanceSheetComparisonPreset.LAST_12_MONTHS) }
        ),
        DatePresetOption(
            id = PRESET_PREVIOUS_MONTH,
            titleEn = "Previous Month",
            titleBn = "পূর্ববর্তী মাস",
            calculateRange = { BalanceSheetHelper.getPresetDateRanges(BalanceSheetComparisonPreset.PREVIOUS_MONTH) }
        ),
        DatePresetOption(
            id = PRESET_BEGINNING_OF_YEAR,
            titleEn = "Start of Year",
            titleBn = "বছরের শুরু",
            calculateRange = { BalanceSheetHelper.getPresetDateRanges(BalanceSheetComparisonPreset.BEGINNING_OF_YEAR) }
        ),
        DatePresetOption(
            id = PRESET_LAST_30_DAYS,
            titleEn = "Last 30 Days",
            titleBn = "গত ৩০ দিন",
            calculateRange = { BalanceSheetHelper.getPresetDateRanges(BalanceSheetComparisonPreset.LAST_30_DAYS) }
        ),
        DatePresetOption(
            id = PRESET_TODAY,
            titleEn = "Today",
            titleBn = "আজকে",
            calculateRange = { BalanceSheetHelper.getPresetDateRanges(BalanceSheetComparisonPreset.TODAY) }
        ),
        DatePresetOption(
            id = PRESET_CUSTOM,
            titleEn = "Custom Date Range",
            titleBn = "কাস্টম সময়কাল",
            calculateRange = { BalanceSheetHelper.getPresetDateRanges(BalanceSheetComparisonPreset.CUSTOM) }
        )
    )

    val SORT_OPTIONS: List<SortOptionItem<Any>> = listOf(
        SortOptionItem(
            id = SORT_AMOUNT_DESC,
            titleEn = "Amount: High to Low",
            titleBn = "পরিমাণ: বেশি থেকে কম",
            icon = Icons.AutoMirrored.Filled.TrendingDown
        ),
        SortOptionItem(
            id = SORT_AMOUNT_ASC,
            titleEn = "Amount: Low to High",
            titleBn = "পরিমাণ: কম থেকে বেশি",
            icon = Icons.AutoMirrored.Filled.TrendingUp
        ),
        SortOptionItem(
            id = SORT_NAME_ASC,
            titleEn = "Name: A to Z",
            titleBn = "নাম: ক থেকে ঁ",
            icon = Icons.AutoMirrored.Filled.Sort
        ),
        SortOptionItem(
            id = SORT_DEFAULT,
            titleEn = "Default Order",
            titleBn = "ডিফল্ট ক্রম",
            icon = Icons.AutoMirrored.Filled.Sort
        )
    )

    val STATUS_OPTIONS = listOf(
        SelectItemOption("CLEARED", "Cleared", "সম্পন্ন", icon = Icons.Default.CheckCircle),
        SelectItemOption("RECONCILED", "Reconciled", "মিলিত", icon = Icons.Default.CheckCircle),
        SelectItemOption("VOID", "Void", "বাতিল")
    )

    fun createSpec(allAccounts: List<Account> = emptyList()): FilterSpec<Any> {
        val accountOptions = allAccounts.map { acc ->
            SelectItemOption(
                id = acc.id.toString(),
                titleEn = acc.nameEn,
                titleBn = acc.nameBn,
                subtitleEn = acc.accountNumber,
                subtitleBn = acc.accountNumber,
                icon = Icons.Default.AccountBalance
            )
        }

        return FilterSpec(
            key = SPEC_KEY,
            titleEn = "Filter & Compare Balance Sheet",
            titleBn = "ব্যালেন্স শিট ফিল্টার ও তুলনা",
            accentColor = SolidPrimary,
            fields = listOf(
                FilterField.SearchField(
                    id = FIELD_SEARCH,
                    titleEn = "Search Accounts",
                    titleBn = "অ্যাকাউন্ট খুঁজুন",
                    hintEn = "Search accounts...",
                    hintBn = "অ্যাকাউন্ট খুঁজুন...",
                    icon = Icons.Default.Search
                ),
                FilterField.ToggleGroupField(
                    id = FIELD_ACTIVE_TAB,
                    titleEn = "Balance Sheet Section",
                    titleBn = "ব্যালেন্স শিট সেকশন",
                    icon = Icons.Default.Layers,
                    conditions = listOf(
                        FilterCondition(TAB_ALL, "All", "সব", group = "bs_tab"),
                        FilterCondition(TAB_ASSETS, "Assets", "সম্পদ", icon = Icons.AutoMirrored.Filled.TrendingUp, group = "bs_tab"),
                        FilterCondition(TAB_LIABILITIES, "Liabilities", "দায়", icon = Icons.AutoMirrored.Filled.TrendingDown, group = "bs_tab")
                    )
                ),
                FilterField.DateField(
                    id = FIELD_DATE,
                    titleEn = "Comparison Timeline",
                    titleBn = "তুলনার সময়সীমা",
                    icon = Icons.Default.CalendarMonth,
                    presets = DATE_PRESET_OPTIONS,
                    defaultPresetId = PRESET_THIS_MONTH,
                    allowCustom = true
                ),
                FilterField.SelectField(
                    id = FIELD_ACCOUNTS,
                    titleEn = "Filter Accounts",
                    titleBn = "নির্দিষ্ট অ্যাকাউন্ট নির্বাচন",
                    subtitleEn = "Select accounts to include in balance sheet",
                    subtitleBn = "ব্যালেন্স শিটে অন্তর্ভুক্ত অ্যাকাউন্ট নির্বাচন করুন",
                    icon = Icons.Default.AccountBalance,
                    isMultiSelect = true,
                    items = accountOptions
                ),
                FilterField.SelectField(
                    id = FIELD_STATUSES,
                    titleEn = "Transaction Status",
                    titleBn = "লেনদেনের স্ট্যাটাস",
                    subtitleEn = "Include transactions by verification status",
                    subtitleBn = "ভেরিফিকেশন স্ট্যাটাস অনুযায়ী লেনদেন অন্তর্ভুক্ত করুন",
                    icon = Icons.Default.CheckCircle,
                    isMultiSelect = true,
                    items = STATUS_OPTIONS
                ),
                FilterField.SortField(
                    id = FIELD_SORT,
                    titleEn = "Sort Order",
                    titleBn = "সাজানোর ক্রম",
                    icon = Icons.AutoMirrored.Filled.Sort,
                    options = SORT_OPTIONS,
                    defaultSortId = SORT_AMOUNT_DESC
                ),
                FilterField.BooleanField(
                    id = FIELD_EXCLUDE_ZERO,
                    titleEn = "Exclude Zero Balance Accounts",
                    titleBn = "শূন্য ব্যালেন্সের অ্যাকাউন্ট বাদ দিন",
                    subtitleEn = "Hide accounts with ৳0 balance from the report",
                    subtitleBn = "০ টাকার ব্যালেন্স সম্পন্ন হিসাবগুলো লুকান",
                    icon = Icons.Default.Tune,
                    defaultValue = true
                ),
                FilterField.BooleanField(
                    id = FIELD_FILTER_NON_ZERO_GROUPS,
                    titleEn = "Hide Empty Groups",
                    titleBn = "খালি গ্রুপ লুকান",
                    subtitleEn = "Hide parent account groups having no balance change",
                    subtitleBn = "যেসব প্যারেন্ট গ্রুপের কোনো পরিবর্তন নেই তা লুকান",
                    icon = Icons.Default.VisibilityOff,
                    defaultValue = false
                ),
                FilterField.BooleanField(
                    id = FIELD_SHOW_HIDDEN,
                    titleEn = "Show Inactive Accounts",
                    titleBn = "নিষ্ক্রিয় অ্যাকাউন্ট দেখান",
                    subtitleEn = "Include archived and inactive accounts in calculations",
                    subtitleBn = "আর্কাইভ বা বন্ধ হওয়া হিসাব অন্তর্ভুক্ত করুন",
                    icon = Icons.Default.Visibility,
                    defaultValue = false
                ),
                FilterField.BooleanField(
                    id = FIELD_ONLY_CURRENT,
                    titleEn = "Current Balance Only",
                    titleBn = "শুধু বর্তমান ব্যালেন্স (তুলনাহীন)",
                    subtitleEn = "Show single current balance column without historical delta",
                    subtitleBn = "অতীতের তুলনা ছাড়া একক বর্তমান ব্যালেন্স কলাম প্রদর্শন করুন",
                    icon = Icons.Default.Layers,
                    defaultValue = false
                ),
                FilterField.BooleanField(
                    id = FIELD_WITHOUT_GROUPS,
                    titleEn = "Flatten Accounts (No Groups)",
                    titleBn = "গ্রুপ ছাড়া সমতল তালিকা",
                    subtitleEn = "Show all accounts without grouping structure",
                    subtitleBn = "প্যারেন্ট-সাব গ্রুপ বিন্যাস ছাড়াই হিসাবগুলো দেখান",
                    icon = Icons.Default.Layers,
                    defaultValue = false
                )
            )
        )
    }

    // Accessors

    fun getSearchQuery(state: FilterState): String {
        return (state[FIELD_SEARCH] as? FilterValue.Search)?.query ?: ""
    }

    fun getDatePreset(state: FilterState): BalanceSheetComparisonPreset {
        val presetId = (state[FIELD_DATE] as? FilterValue.Date)?.presetId ?: PRESET_THIS_MONTH
        return when (presetId) {
            PRESET_END_OF_LAST_MONTH -> BalanceSheetComparisonPreset.END_OF_LAST_MONTH
            PRESET_BEGINNING_OF_MONTH -> BalanceSheetComparisonPreset.BEGINNING_OF_MONTH
            PRESET_LAST_12_MONTHS -> BalanceSheetComparisonPreset.LAST_12_MONTHS
            PRESET_PREVIOUS_MONTH -> BalanceSheetComparisonPreset.PREVIOUS_MONTH
            PRESET_BEGINNING_OF_YEAR -> BalanceSheetComparisonPreset.BEGINNING_OF_YEAR
            PRESET_LAST_30_DAYS -> BalanceSheetComparisonPreset.LAST_30_DAYS
            PRESET_TODAY -> BalanceSheetComparisonPreset.TODAY
            PRESET_CUSTOM -> BalanceSheetComparisonPreset.CUSTOM
            else -> BalanceSheetComparisonPreset.THIS_MONTH
        }
    }

    fun resolveComparisonDates(
        spec: FilterSpec<*>,
        state: FilterState,
        defaultBaseMs: Long = 0L,
        defaultCompareMs: Long = 0L
    ): Pair<Long, Long> {
        val dateVal = state[FIELD_DATE] as? FilterValue.Date
        if (dateVal?.startMs != null && dateVal.endMs != null) {
            return Pair(dateVal.startMs, dateVal.endMs)
        }
        val preset = getDatePreset(state)
        if (preset == BalanceSheetComparisonPreset.CUSTOM && defaultBaseMs > 0 && defaultCompareMs > 0) {
            return Pair(defaultBaseMs, defaultCompareMs)
        }
        return BalanceSheetHelper.getPresetDateRanges(preset)
    }

    fun getSelectedAccountIds(state: FilterState): Set<Long> {
        val active = (state[FIELD_ACCOUNTS] as? FilterValue.Select)?.selectedIds ?: emptySet()
        return active.mapNotNull { it.toLongOrNull() }.toSet()
    }

    fun getSelectedStatuses(state: FilterState): Set<TransactionStatus> {
        val active = (state[FIELD_STATUSES] as? FilterValue.Select)?.selectedIds ?: emptySet()
        return active.mapNotNull {
            try { TransactionStatus.valueOf(it) } catch (_: Exception) { null }
        }.toSet()
    }

    fun getSortOrder(state: FilterState): BalanceSheetSortOrder {
        val sortId = (state[FIELD_SORT] as? FilterValue.Sort)?.sortId ?: SORT_AMOUNT_DESC
        return when (sortId) {
            SORT_AMOUNT_ASC -> BalanceSheetSortOrder.AMOUNT_ASC
            SORT_NAME_ASC -> BalanceSheetSortOrder.NAME_ASC
            SORT_DEFAULT -> BalanceSheetSortOrder.DEFAULT
            else -> BalanceSheetSortOrder.AMOUNT_DESC
        }
    }

    fun getExcludeZero(state: FilterState): Boolean {
        return (state[FIELD_EXCLUDE_ZERO] as? FilterValue.BooleanVal)?.value ?: true
    }

    fun getFilterNonZeroGroups(state: FilterState): Boolean {
        return (state[FIELD_FILTER_NON_ZERO_GROUPS] as? FilterValue.BooleanVal)?.value ?: false
    }

    fun getShowHidden(state: FilterState): Boolean {
        return (state[FIELD_SHOW_HIDDEN] as? FilterValue.BooleanVal)?.value ?: false
    }

    fun getShowOnlyCurrent(state: FilterState): Boolean {
        return (state[FIELD_ONLY_CURRENT] as? FilterValue.BooleanVal)?.value ?: false
    }

    fun getShowWithoutGroups(state: FilterState): Boolean {
        return (state[FIELD_WITHOUT_GROUPS] as? FilterValue.BooleanVal)?.value ?: false
    }

    fun getActiveTab(state: FilterState): String {
        val active = (state[FIELD_ACTIVE_TAB] as? FilterValue.ToggleGroup)?.activeIds ?: emptySet()
        return when {
            active.contains(TAB_ASSETS) -> TAB_ASSETS
            active.contains(TAB_LIABILITIES) -> TAB_LIABILITIES
            else -> TAB_ALL
        }
    }

    // Builders

    fun withSearchQuery(state: FilterState, query: String): FilterState {
        return if (query.isBlank()) state - FIELD_SEARCH
        else state + (FIELD_SEARCH to FilterValue.Search(query))
    }

    fun withPreset(state: FilterState, preset: BalanceSheetComparisonPreset): FilterState {
        val presetId = when (preset) {
            BalanceSheetComparisonPreset.END_OF_LAST_MONTH -> PRESET_END_OF_LAST_MONTH
            BalanceSheetComparisonPreset.BEGINNING_OF_MONTH -> PRESET_BEGINNING_OF_MONTH
            BalanceSheetComparisonPreset.LAST_12_MONTHS -> PRESET_LAST_12_MONTHS
            BalanceSheetComparisonPreset.PREVIOUS_MONTH -> PRESET_PREVIOUS_MONTH
            BalanceSheetComparisonPreset.BEGINNING_OF_YEAR -> PRESET_BEGINNING_OF_YEAR
            BalanceSheetComparisonPreset.LAST_30_DAYS -> PRESET_LAST_30_DAYS
            BalanceSheetComparisonPreset.TODAY -> PRESET_TODAY
            BalanceSheetComparisonPreset.CUSTOM -> PRESET_CUSTOM
            else -> PRESET_THIS_MONTH
        }
        val (base, compare) = BalanceSheetHelper.getPresetDateRanges(preset)
        return state + (FIELD_DATE to FilterValue.Date(presetId = presetId, startMs = base, endMs = compare))
    }

    fun withCustomDates(state: FilterState, baseMs: Long, compareMs: Long): FilterState {
        return state + (FIELD_DATE to FilterValue.Date(presetId = PRESET_CUSTOM, startMs = baseMs, endMs = compareMs))
    }

    fun withAccountIds(state: FilterState, ids: Set<Long>): FilterState {
        return if (ids.isEmpty()) state - FIELD_ACCOUNTS
        else state + (FIELD_ACCOUNTS to FilterValue.Select(ids.map { it.toString() }.toSet()))
    }

    fun withSortOrder(state: FilterState, sortOrder: BalanceSheetSortOrder): FilterState {
        val id = when (sortOrder) {
            BalanceSheetSortOrder.AMOUNT_ASC -> SORT_AMOUNT_ASC
            BalanceSheetSortOrder.NAME_ASC -> SORT_NAME_ASC
            BalanceSheetSortOrder.DEFAULT -> SORT_DEFAULT
            else -> SORT_AMOUNT_DESC
        }
        return state + (FIELD_SORT to FilterValue.Sort(id))
    }

    fun withExcludeZero(state: FilterState, exclude: Boolean): FilterState {
        return state + (FIELD_EXCLUDE_ZERO to FilterValue.BooleanVal(exclude))
    }

    fun withActiveTab(state: FilterState, tab: String): FilterState {
        return state + (FIELD_ACTIVE_TAB to FilterValue.ToggleGroup(setOf(tab)))
    }
}
