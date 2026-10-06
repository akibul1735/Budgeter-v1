package com.example.ui.components.filter.specs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.Color
import com.example.data.model.Account
import com.example.data.model.LanguageMode
import com.example.data.model.RequirementCalculationBasis
import com.example.ui.components.filter.FilterCondition
import com.example.ui.components.filter.FilterField
import com.example.ui.components.filter.FilterSpec
import com.example.ui.components.filter.FilterState
import com.example.ui.components.filter.FilterValue
import com.example.ui.components.filter.SelectItemOption
import com.example.ui.components.filter.SortOptionItem
import com.example.ui.screens.AccountStatusFilter
import com.example.ui.screens.AssignedItemSectionFilter
import com.example.ui.screens.AssignedItemSortOption
import com.example.ui.screens.AssignedItemStatusFilter
import com.example.ui.screens.MainPaymentSourceTab
import com.example.ui.screens.PaymentSourceSortOption

/**
 * Filter specification for PaymentSourceScreen.
 * Covers both Main Payment Source Tab and Assigned Items Tab, including
 * calculation basis, account status, section filters, item status, and sorting.
 */
object PaymentSourceFilterSpec {

    const val SPEC_KEY = "payment_source"

    // Field IDs
    const val FIELD_SEARCH = "search"
    const val FIELD_MAIN_TAB = "main_tab"
    const val FIELD_CALC_BASIS = "calc_basis"
    const val FIELD_SOURCE_STATUS = "source_status"
    const val FIELD_SOURCE_SORT = "source_sort"
    const val FIELD_ASSIGNED_SECTION = "assigned_section"
    const val FIELD_ASSIGNED_STATUS = "assigned_status"
    const val FIELD_ASSIGNED_SORT = "assigned_sort"
    const val FIELD_ASSIGNED_SOURCE_ACCOUNT = "assigned_source_account"

    // Tab condition IDs
    const val TAB_SOURCES = "sources"
    const val TAB_ASSIGNED = "assigned"

    // Calculation basis condition IDs
    const val BASIS_BUDGET = "budget"
    const val BASIS_REMAINING = "remaining"

    // Payment Source Status condition IDs
    const val STATUS_ALL = "all"
    const val STATUS_SHORTFALL = "shortfall"
    const val STATUS_SURPLUS = "surplus"

    // Payment Source Sort IDs
    const val SORT_SOURCE_DEFAULT = "default"
    const val SORT_SOURCE_BALANCE_DESC = "balance_desc"
    const val SORT_SOURCE_BALANCE_ASC = "balance_asc"
    const val SORT_SOURCE_REQUIRED_DESC = "required_desc"
    const val SORT_SOURCE_SHORTFALL_DESC = "shortfall_desc"
    const val SORT_SOURCE_NAME_ASC = "name_asc"

    // Assigned Items Section condition IDs
    const val SECTION_ALL = "all"
    const val SECTION_ONLY_ITEMS = "only_items"
    const val SECTION_OTHER_ACCOUNTS = "other_accounts"
    const val SECTION_EXPENSES = "expenses"
    const val SECTION_INCOMES = "incomes"

    // Assigned Items Status condition IDs
    const val ASSIGNED_STATUS_ALL = "all"
    const val ASSIGNED_STATUS_BUDGETED = "budgeted"
    const val ASSIGNED_STATUS_REMAINING = "remaining"
    const val ASSIGNED_STATUS_MOST_FREQUENT = "most_frequent"
    const val ASSIGNED_STATUS_SPLIT = "split"
    const val ASSIGNED_STATUS_UNASSIGNED = "unassigned"

    // Assigned Items Sort IDs
    const val SORT_ASSIGNED_DEFAULT = "default"
    const val SORT_ASSIGNED_AMOUNT_DESC = "amount_desc"
    const val SORT_ASSIGNED_AMOUNT_ASC = "amount_asc"
    const val SORT_ASSIGNED_REMAINING_DESC = "remaining_desc"
    const val SORT_ASSIGNED_NAME_AZ = "name_az"
    const val SORT_ASSIGNED_MOST_USED = "most_used"

    val SOURCE_SORT_OPTIONS: List<SortOptionItem<Any>> = listOf(
        SortOptionItem(SORT_SOURCE_DEFAULT, "Default Order", "পূর্বনির্ধারিত ক্রম"),
        SortOptionItem(SORT_SOURCE_BALANCE_DESC, "Balance: High → Low", "ব্যালেন্স: বেশি → কম"),
        SortOptionItem(SORT_SOURCE_BALANCE_ASC, "Balance: Low → High", "ব্যালেন্স: কম → বেশি"),
        SortOptionItem(SORT_SOURCE_REQUIRED_DESC, "Required: High → Low", "প্রয়োজনীয়: বেশি → কম"),
        SortOptionItem(SORT_SOURCE_SHORTFALL_DESC, "Shortfall: High → Low", "ঘাটতি: বেশি → কম"),
        SortOptionItem(SORT_SOURCE_NAME_ASC, "Name: A → Z", "নাম: ক → য়")
    )

    val ASSIGNED_SORT_OPTIONS: List<SortOptionItem<Any>> = listOf(
        SortOptionItem(SORT_ASSIGNED_DEFAULT, "Default Order", "পূর্বনির্ধারিত ক্রম"),
        SortOptionItem(SORT_ASSIGNED_AMOUNT_DESC, "Amount: High → Low", "পরিমাণ: বেশি → কম"),
        SortOptionItem(SORT_ASSIGNED_AMOUNT_ASC, "Amount: Low → High", "পরিমাণ: কম → বেশি"),
        SortOptionItem(SORT_ASSIGNED_REMAINING_DESC, "Remaining: High → Low", "অবশিষ্ট: বেশি → কম"),
        SortOptionItem(SORT_ASSIGNED_NAME_AZ, "Name: A → Z", "নাম: ক → য়"),
        SortOptionItem(SORT_ASSIGNED_MOST_USED, "Most Used", "সর্বাধিক ব্যবহৃত")
    )

    fun createSpec(
        paymentSourceAccounts: List<Account> = emptyList(),
        isAssignedTab: Boolean = false
    ): FilterSpec<Any> {
        val accountOptions = paymentSourceAccounts.map { acc ->
            SelectItemOption(
                id = acc.id.toString(),
                titleEn = acc.nameEn,
                titleBn = acc.nameBn,
                icon = Icons.Default.AccountBalance
            )
        }

        val fields = mutableListOf<FilterField<Any>>()

        // 1. Search Field
        fields.add(
            FilterField.SearchField(
                id = FIELD_SEARCH,
                titleEn = "Search",
                titleBn = "অনুসন্ধান",
                hintEn = "Search payment sources or items...",
                hintBn = "পেমেন্ট সোর্স বা আইটেম খুঁজুন...",
                icon = Icons.Default.Search
            )
        )

        // 2. Main Tab Selector
        fields.add(
            FilterField.ToggleGroupField(
                id = FIELD_MAIN_TAB,
                titleEn = "Main Tab",
                titleBn = "মূল ট্যাব",
                icon = Icons.Default.Tune,
                conditions = listOf(
                    FilterCondition(TAB_SOURCES, "Payment Sources", "পেমেন্ট সোর্স", icon = Icons.Default.AccountBalanceWallet, group = "tab"),
                    FilterCondition(TAB_ASSIGNED, "Assigned Items", "বরাদ্দকৃত আইটেম", icon = Icons.Default.CallSplit, group = "tab")
                )
            )
        )

        // 3. Calculation Basis
        fields.add(
            FilterField.ToggleGroupField(
                id = FIELD_CALC_BASIS,
                titleEn = "Calculation Basis",
                titleBn = "হিসাবের ভিত্তি",
                icon = Icons.Default.PieChart,
                conditions = listOf(
                    FilterCondition(BASIS_BUDGET, "Budget Based", "বাজেট ভিত্তিক", icon = Icons.Default.PieChart, group = "basis"),
                    FilterCondition(BASIS_REMAINING, "Remaining Based", "অবশিষ্ট ভিত্তিক", icon = Icons.Default.HourglassBottom, group = "basis")
                )
            )
        )

        if (!isAssignedTab) {
            // 4. Source Status Filter
            fields.add(
                FilterField.ToggleGroupField(
                    id = FIELD_SOURCE_STATUS,
                    titleEn = "Source Status",
                    titleBn = "সোর্স অবস্থা",
                    icon = Icons.Default.Tune,
                    conditions = listOf(
                        FilterCondition(STATUS_ALL, "All Sources", "সকল সোর্স", group = "source_status"),
                        FilterCondition(STATUS_SHORTFALL, "Shortfall Only", "ঘাটতি সোর্স", icon = Icons.Default.Warning, group = "source_status"),
                        FilterCondition(STATUS_SURPLUS, "Surplus Only", "উদ্বৃত্ত সোর্স", icon = Icons.Default.CheckCircle, group = "source_status")
                    )
                )
            )

            // 5. Source Sort
            fields.add(
                FilterField.SortField(
                    id = FIELD_SOURCE_SORT,
                    titleEn = "Sort Order",
                    titleBn = "সাজানোর ক্রম",
                    icon = Icons.Default.Sort,
                    options = SOURCE_SORT_OPTIONS,
                    defaultSortId = SORT_SOURCE_DEFAULT
                )
            )
        } else {
            // 6. Assigned Section Filter
            fields.add(
                FilterField.ToggleGroupField(
                    id = FIELD_ASSIGNED_SECTION,
                    titleEn = "Item Section",
                    titleBn = "আইটেম বিভাগ",
                    icon = Icons.AutoMirrored.Filled.List,
                    conditions = listOf(
                        FilterCondition(SECTION_ALL, "All Items", "সকল আইটেম", group = "section"),
                        FilterCondition(SECTION_ONLY_ITEMS, "Only Items", "শুধু আইটেম", icon = Icons.AutoMirrored.Filled.List, group = "section"),
                        FilterCondition(SECTION_OTHER_ACCOUNTS, "Other Accounts", "অন্যান্য অ্যাকাউন্ট", icon = Icons.Default.People, group = "section"),
                        FilterCondition(SECTION_EXPENSES, "Expenses", "ব্যয় ক্যাটাগরি", icon = Icons.Default.MonetizationOn, group = "section"),
                        FilterCondition(SECTION_INCOMES, "Incomes", "আয় ক্যাটাগরি", icon = Icons.Default.Category, group = "section")
                    )
                )
            )

            // 7. Assigned Status Filter
            fields.add(
                FilterField.ToggleGroupField(
                    id = FIELD_ASSIGNED_STATUS,
                    titleEn = "Item Status",
                    titleBn = "আইটেম অবস্থা",
                    icon = Icons.Default.Tune,
                    conditions = listOf(
                        FilterCondition(ASSIGNED_STATUS_ALL, "All Statuses", "সকল অবস্থা", group = "assigned_status"),
                        FilterCondition(ASSIGNED_STATUS_BUDGETED, "Budgeted / Due", "বাজেটকৃত / দেনা", group = "assigned_status"),
                        FilterCondition(ASSIGNED_STATUS_REMAINING, "Remaining", "অবশিষ্ট", group = "assigned_status"),
                        FilterCondition(ASSIGNED_STATUS_MOST_FREQUENT, "Most Used", "সর্বাধিক ব্যবহৃত", group = "assigned_status"),
                        FilterCondition(ASSIGNED_STATUS_SPLIT, "Split Across Sources", "একাধিক সোর্সে বিভক্ত", icon = Icons.Default.CallSplit, group = "assigned_status"),
                        FilterCondition(ASSIGNED_STATUS_UNASSIGNED, "Unassigned", "বরাদ্দহীন", icon = Icons.Default.Warning, group = "assigned_status")
                    )
                )
            )

            // 8. Filter by Payment Source Account (SelectField with isMultiSelect = false)
            if (accountOptions.isNotEmpty()) {
                fields.add(
                    FilterField.SelectField(
                        id = FIELD_ASSIGNED_SOURCE_ACCOUNT,
                        titleEn = "Filter by Source Account",
                        titleBn = "নির্দিষ্ট সোর্স অ্যাকাউন্ট ফিল্টার",
                        icon = Icons.Default.AccountBalance,
                        isMultiSelect = false,
                        items = accountOptions
                    )
                )
            }

            // 9. Assigned Sort
            fields.add(
                FilterField.SortField(
                    id = FIELD_ASSIGNED_SORT,
                    titleEn = "Sort Order",
                    titleBn = "সাজানোর ক্রম",
                    icon = Icons.Default.Sort,
                    options = ASSIGNED_SORT_OPTIONS,
                    defaultSortId = SORT_ASSIGNED_DEFAULT
                )
            )
        }

        return FilterSpec(
            key = SPEC_KEY,
            titleEn = "Filter & Sort Payment Source",
            titleBn = "পেমেন্ট সোর্স ফিল্টার ও সাজানো",
            accentColor = Color(0xFF0D9488), // Teal accent
            fields = fields
        )
    }

    // --- State Extraction Helpers ---

    fun getSearchQuery(state: FilterState): String {
        return (state[FIELD_SEARCH] as? FilterValue.Search)?.query ?: ""
    }

    fun getMainTab(state: FilterState): MainPaymentSourceTab {
        val active = (state[FIELD_MAIN_TAB] as? FilterValue.ToggleGroup)?.activeIds ?: emptySet()
        return if (active.contains(TAB_ASSIGNED)) {
            MainPaymentSourceTab.ASSIGNED_ITEMS
        } else {
            MainPaymentSourceTab.PAYMENT_SOURCES
        }
    }

    fun getCalcBasis(state: FilterState): RequirementCalculationBasis {
        val active = (state[FIELD_CALC_BASIS] as? FilterValue.ToggleGroup)?.activeIds ?: emptySet()
        return if (active.contains(BASIS_REMAINING)) {
            RequirementCalculationBasis.REMAINING_AMOUNT
        } else {
            RequirementCalculationBasis.BUDGET_AMOUNT
        }
    }

    fun getSourceStatus(state: FilterState): AccountStatusFilter {
        val active = (state[FIELD_SOURCE_STATUS] as? FilterValue.ToggleGroup)?.activeIds ?: emptySet()
        return when (active.firstOrNull()) {
            STATUS_SHORTFALL -> AccountStatusFilter.SHORTFALL_ONLY
            STATUS_SURPLUS -> AccountStatusFilter.SURPLUS_ONLY
            else -> AccountStatusFilter.ALL
        }
    }

    fun getSourceSort(state: FilterState): PaymentSourceSortOption {
        val sortId = (state[FIELD_SOURCE_SORT] as? FilterValue.Sort)?.sortId
        return when (sortId) {
            SORT_SOURCE_BALANCE_DESC -> PaymentSourceSortOption.BALANCE_DESC
            SORT_SOURCE_BALANCE_ASC -> PaymentSourceSortOption.BALANCE_ASC
            SORT_SOURCE_REQUIRED_DESC -> PaymentSourceSortOption.REQUIRED_DESC
            SORT_SOURCE_SHORTFALL_DESC -> PaymentSourceSortOption.SHORTFALL_DESC
            SORT_SOURCE_NAME_ASC -> PaymentSourceSortOption.NAME_ASC
            else -> PaymentSourceSortOption.DEFAULT
        }
    }

    fun getAssignedSection(state: FilterState): AssignedItemSectionFilter {
        val active = (state[FIELD_ASSIGNED_SECTION] as? FilterValue.ToggleGroup)?.activeIds ?: emptySet()
        return when (active.firstOrNull()) {
            SECTION_ONLY_ITEMS -> AssignedItemSectionFilter.ONLY_ITEMS
            SECTION_OTHER_ACCOUNTS -> AssignedItemSectionFilter.OTHER_ACCOUNTS
            SECTION_EXPENSES -> AssignedItemSectionFilter.EXPENSES
            SECTION_INCOMES -> AssignedItemSectionFilter.INCOMES
            else -> AssignedItemSectionFilter.ALL
        }
    }

    fun getAssignedStatus(state: FilterState): AssignedItemStatusFilter {
        val active = (state[FIELD_ASSIGNED_STATUS] as? FilterValue.ToggleGroup)?.activeIds ?: emptySet()
        return when (active.firstOrNull()) {
            ASSIGNED_STATUS_BUDGETED -> AssignedItemStatusFilter.BUDGETED_ONLY
            ASSIGNED_STATUS_REMAINING -> AssignedItemStatusFilter.REMAINING_ONLY
            ASSIGNED_STATUS_MOST_FREQUENT -> AssignedItemStatusFilter.MOST_FREQUENT
            ASSIGNED_STATUS_SPLIT -> AssignedItemStatusFilter.SPLIT_ONLY
            ASSIGNED_STATUS_UNASSIGNED -> AssignedItemStatusFilter.UNASSIGNED_ONLY
            else -> AssignedItemStatusFilter.ALL
        }
    }

    fun getAssignedSort(state: FilterState): AssignedItemSortOption {
        val sortId = (state[FIELD_ASSIGNED_SORT] as? FilterValue.Sort)?.sortId
        return when (sortId) {
            SORT_ASSIGNED_AMOUNT_DESC -> AssignedItemSortOption.BUDGET_DESC
            SORT_ASSIGNED_AMOUNT_ASC -> AssignedItemSortOption.BUDGET_ASC
            SORT_ASSIGNED_REMAINING_DESC -> AssignedItemSortOption.REMAINING_DESC
            SORT_ASSIGNED_NAME_AZ -> AssignedItemSortOption.NAME_ASC
            SORT_ASSIGNED_MOST_USED -> AssignedItemSortOption.MOST_USED
            else -> AssignedItemSortOption.DEFAULT
        }
    }

    fun getSelectedSourceAccountId(state: FilterState): Long? {
        val selId = when (val v = state[FIELD_ASSIGNED_SOURCE_ACCOUNT]) {
            is FilterValue.SingleSelect -> v.selectedId
            is FilterValue.Select -> v.selectedIds.firstOrNull()
            else -> null
        }
        return selId?.toLongOrNull()
    }
}
