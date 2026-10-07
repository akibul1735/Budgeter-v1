package com.example.ui.components.filter.specs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.ui.graphics.Color
import com.example.data.model.Account
import com.example.data.model.LanguageMode
import com.example.data.model.TransactionType
import com.example.ui.components.filter.DatePresetOption
import com.example.ui.components.filter.FilterCondition
import com.example.ui.components.filter.FilterField
import com.example.ui.components.filter.FilterSpec
import com.example.ui.components.filter.FilterState
import com.example.ui.components.filter.FilterValue
import com.example.ui.components.filter.SelectItemOption
import com.example.ui.screens.CashFlowTabSection
import com.example.util.CashFlowHelper
import com.example.util.CashFlowPeriodPreset

/**
 * Universal Filter Specification for CashFlowScreen (Cash Flow / Net Liquidity).
 */
object CashFlowFilterSpec {
    const val SPEC_KEY = "cash_flow"

    // Field IDs
    const val FIELD_SEARCH = "search"
    const val FIELD_DATE = "date"
    const val FIELD_SECTION = "section"
    const val FIELD_ACCOUNTS = "accounts"
    const val FIELD_TX_TYPE = "tx_type"

    // Section IDs
    const val SEC_OVERVIEW = "overview"
    const val SEC_STATEMENT = "statement"
    const val SEC_ACCOUNTS = "accounts"
    const val SEC_TRANSACTIONS = "transactions"

    // Preset IDs
    const val PRESET_THIS_MONTH = "this_month"
    const val PRESET_LAST_MONTH = "last_month"
    const val PRESET_LAST_3_MONTHS = "last_3_months"
    const val PRESET_THIS_YEAR = "this_year"
    const val PRESET_ALL_TIME = "all_time"
    const val PRESET_CUSTOM = "custom"

    // Tx Type IDs
    const val TYPE_ALL = "all"
    const val TYPE_INCOME = "income"
    const val TYPE_EXPENSE = "expense"
    const val TYPE_TRANSFER = "transfer"

    val DATE_PRESETS = listOf(
        DatePresetOption(
            id = PRESET_THIS_MONTH,
            titleEn = "This Month",
            titleBn = "চলতি মাস",
            calculateRange = { CashFlowHelper.getDateRangeForPreset(CashFlowPeriodPreset.THIS_MONTH) }
        ),
        DatePresetOption(
            id = PRESET_LAST_MONTH,
            titleEn = "Last Month",
            titleBn = "গত মাস",
            calculateRange = { CashFlowHelper.getDateRangeForPreset(CashFlowPeriodPreset.LAST_MONTH) }
        ),
        DatePresetOption(
            id = PRESET_LAST_3_MONTHS,
            titleEn = "Last 3 Months",
            titleBn = "গত ৩ মাস",
            calculateRange = { CashFlowHelper.getDateRangeForPreset(CashFlowPeriodPreset.LAST_3_MONTHS) }
        ),
        DatePresetOption(
            id = PRESET_THIS_YEAR,
            titleEn = "This Year",
            titleBn = "চলতি বছর",
            calculateRange = { CashFlowHelper.getDateRangeForPreset(CashFlowPeriodPreset.THIS_YEAR) }
        ),
        DatePresetOption(
            id = PRESET_ALL_TIME,
            titleEn = "All Time",
            titleBn = "সর্বকাল",
            calculateRange = { CashFlowHelper.getDateRangeForPreset(CashFlowPeriodPreset.ALL_TIME) }
        ),
        DatePresetOption(
            id = PRESET_CUSTOM,
            titleEn = "Custom Range",
            titleBn = "কাস্টম রেঞ্জ",
            calculateRange = { CashFlowHelper.getDateRangeForPreset(CashFlowPeriodPreset.CUSTOM) }
        )
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
            titleEn = "Filter Cash Flow",
            titleBn = "নগদ প্রবাহ ফিল্টার",
            accentColor = Color(0xFF2E7D32), // Emerald/Positive Green
            fields = listOf(
                FilterField.SearchField(
                    id = FIELD_SEARCH,
                    titleEn = "Search Entries",
                    titleBn = "লেনদেন অনুসন্ধান",
                    hintEn = "Search cash transactions...",
                    hintBn = "নগদ লেনদেন খুঁজুন...",
                    icon = Icons.Default.Search
                ),
                FilterField.DateField(
                    id = FIELD_DATE,
                    titleEn = "Time Period",
                    titleBn = "সময়কাল",
                    icon = Icons.Default.CalendarMonth,
                    presets = DATE_PRESETS,
                    defaultPresetId = PRESET_THIS_MONTH,
                    allowCustom = true
                ),
                FilterField.ToggleGroupField(
                    id = FIELD_SECTION,
                    titleEn = "Cash Flow Tab",
                    titleBn = "নগদ প্রবাহ ট্যাব",
                    icon = Icons.Default.Layers,
                    conditions = listOf(
                        FilterCondition(SEC_OVERVIEW, "Overview & Charts", "সারসংক্ষেপ ও চার্ট", group = "cf_sec"),
                        FilterCondition(SEC_STATEMENT, "Statement", "প্রবাহ বিবরণী", group = "cf_sec"),
                        FilterCondition(SEC_ACCOUNTS, "Liquid Accounts", "নগদ ও ব্যাংক হিসাব", icon = Icons.Default.AccountBalance, group = "cf_sec"),
                        FilterCondition(SEC_TRANSACTIONS, "Cash Entries", "নগদ লেনদেন", icon = Icons.Default.FilterList, group = "cf_sec")
                    )
                ),
                FilterField.SelectField(
                    id = FIELD_ACCOUNTS,
                    titleEn = "Filter Accounts",
                    titleBn = "হিসাব নির্বাচন",
                    subtitleEn = "Select liquid accounts for cash flow calculation",
                    subtitleBn = "নগদ প্রবাহে অন্তর্ভুক্ত হিসাবসমূহ নির্বাচন করুন",
                    icon = Icons.Default.AccountBalance,
                    isMultiSelect = true,
                    items = accountOptions
                ),
                FilterField.ToggleGroupField(
                    id = FIELD_TX_TYPE,
                    titleEn = "Transaction Type Filter",
                    titleBn = "লেনদেনের ধরন",
                    subtitleEn = "Filter entries in the Transactions view",
                    subtitleBn = "লেনদেন তালিকায় নির্দিষ্ট ধরনের লেনদেন দেখুন",
                    icon = Icons.Default.FilterList,
                    conditions = listOf(
                        FilterCondition(TYPE_ALL, "All", "সকল", group = "tx_type"),
                        FilterCondition(TYPE_INCOME, "Incomes", "আয় / আগমন", icon = Icons.AutoMirrored.Filled.TrendingUp, group = "tx_type"),
                        FilterCondition(TYPE_EXPENSE, "Expenses", "ব্যয় / নির্গমন", icon = Icons.AutoMirrored.Filled.TrendingDown, group = "tx_type"),
                        FilterCondition(TYPE_TRANSFER, "Transfers", "স্থানান্তর / ঋণ", icon = Icons.Default.SwapHoriz, group = "tx_type")
                    )
                )
            )
        )
    }

    // Accessors

    fun getSearchQuery(state: FilterState): String {
        return (state[FIELD_SEARCH] as? FilterValue.Search)?.query ?: ""
    }

    fun getPeriodPreset(state: FilterState): CashFlowPeriodPreset {
        val presetId = (state[FIELD_DATE] as? FilterValue.Date)?.presetId ?: PRESET_THIS_MONTH
        return when (presetId) {
            PRESET_LAST_MONTH -> CashFlowPeriodPreset.LAST_MONTH
            PRESET_LAST_3_MONTHS -> CashFlowPeriodPreset.LAST_3_MONTHS
            PRESET_THIS_YEAR -> CashFlowPeriodPreset.THIS_YEAR
            PRESET_ALL_TIME -> CashFlowPeriodPreset.ALL_TIME
            PRESET_CUSTOM -> CashFlowPeriodPreset.CUSTOM
            else -> CashFlowPeriodPreset.THIS_MONTH
        }
    }

    fun resolveDateRange(
        spec: FilterSpec<*>,
        state: FilterState,
        fallbackStartMs: Long = 0L,
        fallbackEndMs: Long = 0L
    ): Pair<Long, Long> {
        val dateVal = state[FIELD_DATE] as? FilterValue.Date
        if (dateVal?.startMs != null && dateVal.endMs != null && dateVal.startMs > 0 && dateVal.endMs > 0) {
            return Pair(dateVal.startMs, dateVal.endMs)
        }
        val preset = getPeriodPreset(state)
        return CashFlowHelper.getDateRangeForPreset(preset, fallbackStartMs, fallbackEndMs)
    }

    fun getSection(state: FilterState): CashFlowTabSection {
        val active = (state[FIELD_SECTION] as? FilterValue.ToggleGroup)?.activeIds ?: emptySet()
        return when {
            active.contains(SEC_STATEMENT) -> CashFlowTabSection.STATEMENT
            active.contains(SEC_ACCOUNTS) -> CashFlowTabSection.ACCOUNTS
            active.contains(SEC_TRANSACTIONS) -> CashFlowTabSection.TRANSACTIONS
            else -> CashFlowTabSection.OVERVIEW
        }
    }

    fun getSelectedAccountIds(state: FilterState): Set<Long>? {
        val select = state[FIELD_ACCOUNTS] as? FilterValue.Select ?: return null
        if (select.selectedIds.isEmpty()) return null
        return select.selectedIds.mapNotNull { it.toLongOrNull() }.toSet()
    }

    fun getTxType(state: FilterState): TransactionType? {
        val active = (state[FIELD_TX_TYPE] as? FilterValue.ToggleGroup)?.activeIds ?: emptySet()
        return when {
            active.contains(TYPE_INCOME) -> TransactionType.INCOME
            active.contains(TYPE_EXPENSE) -> TransactionType.EXPENSE
            active.contains(TYPE_TRANSFER) -> TransactionType.TRANSFER
            else -> null
        }
    }

    // Builders

    fun withSearchQuery(state: FilterState, query: String): FilterState {
        return if (query.isBlank()) state - FIELD_SEARCH
        else state + (FIELD_SEARCH to FilterValue.Search(query))
    }

    fun withPeriodPreset(state: FilterState, preset: CashFlowPeriodPreset): FilterState {
        val presetId = when (preset) {
            CashFlowPeriodPreset.LAST_MONTH -> PRESET_LAST_MONTH
            CashFlowPeriodPreset.LAST_3_MONTHS -> PRESET_LAST_3_MONTHS
            CashFlowPeriodPreset.THIS_YEAR -> PRESET_THIS_YEAR
            CashFlowPeriodPreset.ALL_TIME -> PRESET_ALL_TIME
            CashFlowPeriodPreset.CUSTOM -> PRESET_CUSTOM
            else -> PRESET_THIS_MONTH
        }
        val (start, end) = CashFlowHelper.getDateRangeForPreset(preset)
        return state + (FIELD_DATE to FilterValue.Date(presetId = presetId, startMs = start, endMs = end))
    }

    fun withCustomDates(state: FilterState, startMs: Long, endMs: Long): FilterState {
        return state + (FIELD_DATE to FilterValue.Date(presetId = PRESET_CUSTOM, startMs = startMs, endMs = endMs))
    }

    fun withSection(state: FilterState, section: CashFlowTabSection): FilterState {
        val secId = when (section) {
            CashFlowTabSection.STATEMENT -> SEC_STATEMENT
            CashFlowTabSection.ACCOUNTS -> SEC_ACCOUNTS
            CashFlowTabSection.TRANSACTIONS -> SEC_TRANSACTIONS
            else -> SEC_OVERVIEW
        }
        return state + (FIELD_SECTION to FilterValue.ToggleGroup(setOf(secId)))
    }

    fun withAccountIds(state: FilterState, ids: Set<Long>?): FilterState {
        return if (ids == null || ids.isEmpty()) state - FIELD_ACCOUNTS
        else state + (FIELD_ACCOUNTS to FilterValue.Select(ids.map { it.toString() }.toSet()))
    }

    fun withTxType(state: FilterState, txType: TransactionType?): FilterState {
        val typeId = when (txType) {
            TransactionType.INCOME -> TYPE_INCOME
            TransactionType.EXPENSE -> TYPE_EXPENSE
            TransactionType.TRANSFER -> TYPE_TRANSFER
            else -> TYPE_ALL
        }
        return state + (FIELD_TX_TYPE to FilterValue.ToggleGroup(setOf(typeId)))
    }
}
