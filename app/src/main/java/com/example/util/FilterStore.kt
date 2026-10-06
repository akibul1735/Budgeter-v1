package com.example.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.ui.components.filter.FilterState
import com.example.ui.components.filter.FilterValue
import org.json.JSONArray
import org.json.JSONObject

/**
 * FilterStore: Persistent JSON storage for FilterState per spec.key using SharedPreferences.
 * Includes automatic one-time migration hook from TabFilterPreferences.
 */
class FilterStore private constructor(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "unified_filter_store"
        const val KEY_MIGRATED_LABELS = "migrated_labels_v1"
        const val KEY_MIGRATED_CATEGORIES = "migrated_categories_v1"
        const val KEY_MIGRATED_WISHLIST = "migrated_wishlist_v1"
        const val KEY_MIGRATED_SAVINGS_GOALS = "migrated_savings_goals_v1"
        const val KEY_MIGRATED_PAYMENT_SOURCE = "migrated_payment_source_v1"
        const val KEY_MIGRATED_RM_MANAGER = "migrated_rm_manager_v1"
        const val KEY_MIGRATED_ACCOUNTS = "migrated_accounts_v1"
        const val KEY_MIGRATED_ITEMS = "migrated_items_v1"

        val VALID_LABELS_DATE_PRESETS = setOf(
            "this_month", "last_month", "this_week", "today", "yesterday",
            "last_30_days", "last_90_days", "this_year", "last_year", "all_time"
        )
        const val DEFAULT_LABELS_DATE_PRESET = "this_month"

        val VALID_LABELS_SORT_ORDERS = setOf(
            "amount_desc", "amount_asc", "count_desc", "count_asc",
            "avg_desc", "avg_asc", "name_asc", "name_desc", "recent_date"
        )
        const val DEFAULT_LABELS_SORT_ORDER = "amount_desc"

        val VALID_LABELS_TAB_MODES = setOf(
            com.example.ui.components.filter.specs.LabelsFilterSpec.MODE_EXPENSE,
            com.example.ui.components.filter.specs.LabelsFilterSpec.MODE_ALL,
            com.example.ui.components.filter.specs.LabelsFilterSpec.MODE_INCOME
        )
        const val DEFAULT_LABELS_TAB_MODE = com.example.ui.components.filter.specs.LabelsFilterSpec.MODE_ALL

        val VALID_LABELS_CATEGORY_SEGMENTS = setOf(
            com.example.ui.components.filter.specs.LabelsFilterSpec.SEGMENT_HASHTAGS,
            com.example.ui.components.filter.specs.LabelsFilterSpec.SEGMENT_NOTES,
            com.example.ui.components.filter.specs.LabelsFilterSpec.SEGMENT_PAYEES,
            com.example.ui.components.filter.specs.LabelsFilterSpec.SEGMENT_UNTAGGED,
            com.example.ui.components.filter.specs.LabelsFilterSpec.SEGMENT_ALL
        )
        const val DEFAULT_LABELS_CATEGORY_SEGMENT = com.example.ui.components.filter.specs.LabelsFilterSpec.SEGMENT_HASHTAGS

        val VALID_CATEGORIES_TYPES = setOf(
            com.example.ui.components.filter.specs.CategoriesFilterSpec.TYPE_EXPENSE,
            com.example.ui.components.filter.specs.CategoriesFilterSpec.TYPE_ALL,
            com.example.ui.components.filter.specs.CategoriesFilterSpec.TYPE_INCOME
        )
        const val DEFAULT_CATEGORIES_TYPE = com.example.ui.components.filter.specs.CategoriesFilterSpec.TYPE_EXPENSE

        val VALID_CATEGORIES_HIERARCHIES = setOf(
            com.example.ui.components.filter.specs.CategoriesFilterSpec.HIERARCHY_ALL,
            com.example.ui.components.filter.specs.CategoriesFilterSpec.HIERARCHY_ONLY_GROUPS,
            com.example.ui.components.filter.specs.CategoriesFilterSpec.HIERARCHY_ONLY_CATEGORIES
        )
        const val DEFAULT_CATEGORIES_HIERARCHY = com.example.ui.components.filter.specs.CategoriesFilterSpec.HIERARCHY_ALL

        val VALID_CATEGORIES_SORTS = setOf(
            "default",
            "budget_high_to_low",
            "budget_low_to_high",
            "most_used",
            "least_used",
            "name_az",
            "name_za"
        )
        const val DEFAULT_CATEGORIES_SORT = "default"

        fun mapLabelsDatePreset(preset: com.example.ui.dialogs.AggregatedDatePreset?): String {
            val id = preset?.name?.lowercase()
            return if (id != null && id in VALID_LABELS_DATE_PRESETS) id else DEFAULT_LABELS_DATE_PRESET
        }

        fun mapLabelsSortOrder(sort: com.example.ui.dialogs.AggregatedSortOrder?): String {
            val id = sort?.name?.lowercase()
            return if (id != null && id in VALID_LABELS_SORT_ORDERS) id else DEFAULT_LABELS_SORT_ORDER
        }

        fun mapLabelsTabMode(mode: String?): String {
            val id = mode?.lowercase()
            return if (id != null && id in VALID_LABELS_TAB_MODES) id else DEFAULT_LABELS_TAB_MODE
        }

        fun mapLabelsCategorySegment(segment: String?): String {
            val id = segment?.lowercase()
            return if (id != null && id in VALID_LABELS_CATEGORY_SEGMENTS) id else DEFAULT_LABELS_CATEGORY_SEGMENT
        }

        fun mapCategoriesType(type: com.example.data.model.CategoryType?): String {
            return when (type) {
                com.example.data.model.CategoryType.EXPENSE -> com.example.ui.components.filter.specs.CategoriesFilterSpec.TYPE_EXPENSE
                com.example.data.model.CategoryType.INCOME -> com.example.ui.components.filter.specs.CategoriesFilterSpec.TYPE_INCOME
                null -> com.example.ui.components.filter.specs.CategoriesFilterSpec.TYPE_ALL
            }
        }

        fun mapCategoriesHierarchy(hierarchy: com.example.ui.screens.CategoryViewHierarchyFilter?): String {
            val id = hierarchy?.name?.lowercase()
            return if (id != null && id in VALID_CATEGORIES_HIERARCHIES) id else DEFAULT_CATEGORIES_HIERARCHY
        }

        fun mapCategoriesSort(sort: com.example.ui.screens.CategorySortFilter?): String {
            val id = sort?.name?.lowercase()
            return if (id != null && id in VALID_CATEGORIES_SORTS) id else DEFAULT_CATEGORIES_SORT
        }

        val VALID_WISHLIST_TABS = setOf(
            com.example.ui.components.filter.specs.WishlistFilterSpec.TAB_ACTIVE,
            com.example.ui.components.filter.specs.WishlistFilterSpec.TAB_NEXT_MONTH,
            com.example.ui.components.filter.specs.WishlistFilterSpec.TAB_PLANNED_MONTHS,
            com.example.ui.components.filter.specs.WishlistFilterSpec.TAB_SAVINGS_GOALS,
            com.example.ui.components.filter.specs.WishlistFilterSpec.TAB_PURCHASED,
            com.example.ui.components.filter.specs.WishlistFilterSpec.TAB_ALL
        )
        const val DEFAULT_WISHLIST_TAB = com.example.ui.components.filter.specs.WishlistFilterSpec.TAB_ACTIVE

        val VALID_WISHLIST_SORTS = setOf(
            com.example.ui.components.filter.specs.WishlistFilterSpec.SORT_PRIORITY,
            com.example.ui.components.filter.specs.WishlistFilterSpec.SORT_AMOUNT_DESC,
            com.example.ui.components.filter.specs.WishlistFilterSpec.SORT_AMOUNT_ASC,
            com.example.ui.components.filter.specs.WishlistFilterSpec.SORT_RECENT
        )
        const val DEFAULT_WISHLIST_SORT = com.example.ui.components.filter.specs.WishlistFilterSpec.SORT_PRIORITY

        fun mapWishlistTab(tab: com.example.ui.screens.WishlistFilterTab?): String {
            val id = tab?.name?.lowercase()
            return if (id != null && id in VALID_WISHLIST_TABS) id else DEFAULT_WISHLIST_TAB
        }

        fun mapWishlistSort(sort: com.example.ui.screens.WishlistSort?): String {
            val id = sort?.name?.lowercase()
            return if (id != null && id in VALID_WISHLIST_SORTS) id else DEFAULT_WISHLIST_SORT
        }

        val VALID_SAVINGS_GOALS_STATUSES = setOf(
            com.example.ui.components.filter.specs.SavingsGoalsFilterSpec.STATUS_ALL,
            com.example.ui.components.filter.specs.SavingsGoalsFilterSpec.STATUS_ACTIVE,
            com.example.ui.components.filter.specs.SavingsGoalsFilterSpec.STATUS_COMPLETED,
            com.example.ui.components.filter.specs.SavingsGoalsFilterSpec.STATUS_DEFICIT
        )
        const val DEFAULT_SAVINGS_GOALS_STATUS = com.example.ui.components.filter.specs.SavingsGoalsFilterSpec.STATUS_ALL

        fun mapSavingsGoalsStatus(filter: com.example.ui.screens.GoalFilterType?): String {
            val id = filter?.name?.lowercase()
            return if (id != null && id in VALID_SAVINGS_GOALS_STATUSES) id else DEFAULT_SAVINGS_GOALS_STATUS
        }

        // Payment Source Mappings
        val VALID_PAYMENT_SOURCE_TABS = setOf(
            com.example.ui.components.filter.specs.PaymentSourceFilterSpec.TAB_SOURCES,
            com.example.ui.components.filter.specs.PaymentSourceFilterSpec.TAB_ASSIGNED
        )
        const val DEFAULT_PAYMENT_SOURCE_TAB = com.example.ui.components.filter.specs.PaymentSourceFilterSpec.TAB_SOURCES

        fun mapPaymentSourceTab(tab: com.example.ui.screens.MainPaymentSourceTab?): String {
            return when (tab) {
                com.example.ui.screens.MainPaymentSourceTab.ASSIGNED_ITEMS -> com.example.ui.components.filter.specs.PaymentSourceFilterSpec.TAB_ASSIGNED
                else -> com.example.ui.components.filter.specs.PaymentSourceFilterSpec.TAB_SOURCES
            }
        }

        val VALID_PAYMENT_SOURCE_CALC_BASES = setOf(
            com.example.ui.components.filter.specs.PaymentSourceFilterSpec.BASIS_BUDGET,
            com.example.ui.components.filter.specs.PaymentSourceFilterSpec.BASIS_REMAINING
        )
        const val DEFAULT_PAYMENT_SOURCE_CALC_BASIS = com.example.ui.components.filter.specs.PaymentSourceFilterSpec.BASIS_BUDGET

        fun mapPaymentSourceCalcBasis(basis: com.example.data.model.RequirementCalculationBasis?): String {
            return when (basis) {
                com.example.data.model.RequirementCalculationBasis.REMAINING_AMOUNT -> com.example.ui.components.filter.specs.PaymentSourceFilterSpec.BASIS_REMAINING
                else -> com.example.ui.components.filter.specs.PaymentSourceFilterSpec.BASIS_BUDGET
            }
        }

        val VALID_PAYMENT_SOURCE_STATUSES = setOf(
            com.example.ui.components.filter.specs.PaymentSourceFilterSpec.STATUS_ALL,
            com.example.ui.components.filter.specs.PaymentSourceFilterSpec.STATUS_SHORTFALL,
            com.example.ui.components.filter.specs.PaymentSourceFilterSpec.STATUS_SURPLUS
        )
        const val DEFAULT_PAYMENT_SOURCE_STATUS = com.example.ui.components.filter.specs.PaymentSourceFilterSpec.STATUS_ALL

        fun mapPaymentSourceStatus(status: com.example.ui.screens.AccountStatusFilter?): String {
            return when (status) {
                com.example.ui.screens.AccountStatusFilter.SHORTFALL_ONLY -> com.example.ui.components.filter.specs.PaymentSourceFilterSpec.STATUS_SHORTFALL
                com.example.ui.screens.AccountStatusFilter.SURPLUS_ONLY -> com.example.ui.components.filter.specs.PaymentSourceFilterSpec.STATUS_SURPLUS
                else -> com.example.ui.components.filter.specs.PaymentSourceFilterSpec.STATUS_ALL
            }
        }

        val VALID_PAYMENT_SOURCE_SORTS = setOf(
            com.example.ui.components.filter.specs.PaymentSourceFilterSpec.SORT_SOURCE_DEFAULT,
            com.example.ui.components.filter.specs.PaymentSourceFilterSpec.SORT_SOURCE_BALANCE_DESC,
            com.example.ui.components.filter.specs.PaymentSourceFilterSpec.SORT_SOURCE_BALANCE_ASC,
            com.example.ui.components.filter.specs.PaymentSourceFilterSpec.SORT_SOURCE_REQUIRED_DESC,
            com.example.ui.components.filter.specs.PaymentSourceFilterSpec.SORT_SOURCE_SHORTFALL_DESC,
            com.example.ui.components.filter.specs.PaymentSourceFilterSpec.SORT_SOURCE_NAME_ASC
        )
        const val DEFAULT_PAYMENT_SOURCE_SORT = com.example.ui.components.filter.specs.PaymentSourceFilterSpec.SORT_SOURCE_DEFAULT

        fun mapPaymentSourceSort(sort: com.example.ui.screens.PaymentSourceSortOption?): String {
            val id = sort?.name?.lowercase()
            return if (id != null && id in VALID_PAYMENT_SOURCE_SORTS) id else DEFAULT_PAYMENT_SOURCE_SORT
        }

        val VALID_ASSIGNED_SECTIONS = setOf(
            com.example.ui.components.filter.specs.PaymentSourceFilterSpec.SECTION_ALL,
            com.example.ui.components.filter.specs.PaymentSourceFilterSpec.SECTION_ONLY_ITEMS,
            com.example.ui.components.filter.specs.PaymentSourceFilterSpec.SECTION_OTHER_ACCOUNTS,
            com.example.ui.components.filter.specs.PaymentSourceFilterSpec.SECTION_EXPENSES,
            com.example.ui.components.filter.specs.PaymentSourceFilterSpec.SECTION_INCOMES
        )
        const val DEFAULT_ASSIGNED_SECTION = com.example.ui.components.filter.specs.PaymentSourceFilterSpec.SECTION_ALL

        fun mapAssignedSection(section: com.example.ui.screens.AssignedItemSectionFilter?): String {
            val id = section?.name?.lowercase()
            return if (id != null && id in VALID_ASSIGNED_SECTIONS) id else DEFAULT_ASSIGNED_SECTION
        }

        val VALID_ASSIGNED_STATUSES = setOf(
            com.example.ui.components.filter.specs.PaymentSourceFilterSpec.ASSIGNED_STATUS_ALL,
            com.example.ui.components.filter.specs.PaymentSourceFilterSpec.ASSIGNED_STATUS_BUDGETED,
            com.example.ui.components.filter.specs.PaymentSourceFilterSpec.ASSIGNED_STATUS_REMAINING,
            com.example.ui.components.filter.specs.PaymentSourceFilterSpec.ASSIGNED_STATUS_MOST_FREQUENT,
            com.example.ui.components.filter.specs.PaymentSourceFilterSpec.ASSIGNED_STATUS_SPLIT,
            com.example.ui.components.filter.specs.PaymentSourceFilterSpec.ASSIGNED_STATUS_UNASSIGNED
        )
        const val DEFAULT_ASSIGNED_STATUS = com.example.ui.components.filter.specs.PaymentSourceFilterSpec.ASSIGNED_STATUS_ALL

        fun mapAssignedStatus(status: com.example.ui.screens.AssignedItemStatusFilter?): String {
            val id = when (status) {
                com.example.ui.screens.AssignedItemStatusFilter.BUDGETED_ONLY -> com.example.ui.components.filter.specs.PaymentSourceFilterSpec.ASSIGNED_STATUS_BUDGETED
                com.example.ui.screens.AssignedItemStatusFilter.REMAINING_ONLY -> com.example.ui.components.filter.specs.PaymentSourceFilterSpec.ASSIGNED_STATUS_REMAINING
                com.example.ui.screens.AssignedItemStatusFilter.MOST_FREQUENT -> com.example.ui.components.filter.specs.PaymentSourceFilterSpec.ASSIGNED_STATUS_MOST_FREQUENT
                com.example.ui.screens.AssignedItemStatusFilter.SPLIT_ONLY -> com.example.ui.components.filter.specs.PaymentSourceFilterSpec.ASSIGNED_STATUS_SPLIT
                com.example.ui.screens.AssignedItemStatusFilter.UNASSIGNED_ONLY -> com.example.ui.components.filter.specs.PaymentSourceFilterSpec.ASSIGNED_STATUS_UNASSIGNED
                else -> com.example.ui.components.filter.specs.PaymentSourceFilterSpec.ASSIGNED_STATUS_ALL
            }
            return if (id in VALID_ASSIGNED_STATUSES) id else DEFAULT_ASSIGNED_STATUS
        }

        val VALID_ASSIGNED_SORTS = setOf(
            com.example.ui.components.filter.specs.PaymentSourceFilterSpec.SORT_ASSIGNED_DEFAULT,
            com.example.ui.components.filter.specs.PaymentSourceFilterSpec.SORT_ASSIGNED_AMOUNT_DESC,
            com.example.ui.components.filter.specs.PaymentSourceFilterSpec.SORT_ASSIGNED_AMOUNT_ASC,
            com.example.ui.components.filter.specs.PaymentSourceFilterSpec.SORT_ASSIGNED_REMAINING_DESC,
            com.example.ui.components.filter.specs.PaymentSourceFilterSpec.SORT_ASSIGNED_NAME_AZ,
            com.example.ui.components.filter.specs.PaymentSourceFilterSpec.SORT_ASSIGNED_MOST_USED
        )
        const val DEFAULT_ASSIGNED_SORT = com.example.ui.components.filter.specs.PaymentSourceFilterSpec.SORT_ASSIGNED_DEFAULT

        fun mapAssignedSort(sort: com.example.ui.screens.AssignedItemSortOption?): String {
            val id = sort?.name?.lowercase()
            return if (id != null && id in VALID_ASSIGNED_SORTS) id else DEFAULT_ASSIGNED_SORT
        }

        // RM Manager Mappings
        val VALID_RM_CATEGORIES = setOf(
            com.example.ui.components.filter.specs.RmManagerFilterSpec.CAT_ALL,
            com.example.ui.components.filter.specs.RmManagerFilterSpec.CAT_PENDING_ONLY,
            com.example.ui.components.filter.specs.RmManagerFilterSpec.CAT_RM_ACCOUNTS,
            com.example.ui.components.filter.specs.RmManagerFilterSpec.CAT_RM_OTHERS,
            com.example.ui.components.filter.specs.RmManagerFilterSpec.CAT_SETTLED_ONLY,
            com.example.ui.components.filter.specs.RmManagerFilterSpec.CAT_UNRECONCILED,
            com.example.ui.components.filter.specs.RmManagerFilterSpec.CAT_RECONCILED,
            com.example.ui.components.filter.specs.RmManagerFilterSpec.CAT_HIGH_LIABILITY,
            com.example.ui.components.filter.specs.RmManagerFilterSpec.CAT_RECENT_WEEK
        )
        const val DEFAULT_RM_CATEGORY = com.example.ui.components.filter.specs.RmManagerFilterSpec.CAT_ALL

        fun mapRmCategory(cat: com.example.util.RmManagerHelper.RmFilterCategory?): String {
            val id = cat?.name?.lowercase()
            return if (id != null && id in VALID_RM_CATEGORIES) id else DEFAULT_RM_CATEGORY
        }

        val VALID_RM_SORTS = setOf(
            com.example.ui.components.filter.specs.RmManagerFilterSpec.SORT_HIGHEST_DUE,
            com.example.ui.components.filter.specs.RmManagerFilterSpec.SORT_LOWEST_DUE,
            com.example.ui.components.filter.specs.RmManagerFilterSpec.SORT_MOST_REPAID,
            com.example.ui.components.filter.specs.RmManagerFilterSpec.SORT_NAME_AZ,
            com.example.ui.components.filter.specs.RmManagerFilterSpec.SORT_RECENT_ACTIVITY
        )
        const val DEFAULT_RM_SORT = com.example.ui.components.filter.specs.RmManagerFilterSpec.SORT_HIGHEST_DUE

        fun mapRmSort(sort: com.example.util.RmManagerHelper.RmSortOption?): String {
            val id = sort?.name?.lowercase()
            return if (id != null && id in VALID_RM_SORTS) id else DEFAULT_RM_SORT
        }

        // Accounts Mappings
        val VALID_ACCOUNTS_TYPES = setOf(
            com.example.ui.components.filter.specs.AccountsFilterSpec.TYPE_ALL,
            com.example.ui.components.filter.specs.AccountsFilterSpec.TYPE_ASSET,
            com.example.ui.components.filter.specs.AccountsFilterSpec.TYPE_LIABILITY
        )
        const val DEFAULT_ACCOUNTS_TYPE = com.example.ui.components.filter.specs.AccountsFilterSpec.TYPE_ALL

        fun mapAccountsType(type: com.example.data.model.AccountType?): String {
            return when (type) {
                com.example.data.model.AccountType.ASSET -> com.example.ui.components.filter.specs.AccountsFilterSpec.TYPE_ASSET
                com.example.data.model.AccountType.LIABILITY -> com.example.ui.components.filter.specs.AccountsFilterSpec.TYPE_LIABILITY
                else -> com.example.ui.components.filter.specs.AccountsFilterSpec.TYPE_ALL
            }
        }

        val VALID_ACCOUNTS_HIERARCHIES = setOf(
            com.example.ui.components.filter.specs.AccountsFilterSpec.HIERARCHY_ALL,
            com.example.ui.components.filter.specs.AccountsFilterSpec.HIERARCHY_ONLY_GROUPS,
            com.example.ui.components.filter.specs.AccountsFilterSpec.HIERARCHY_EXCLUDED,
            com.example.ui.components.filter.specs.AccountsFilterSpec.HIERARCHY_ONLY_ACCOUNTS
        )
        const val DEFAULT_ACCOUNTS_HIERARCHY = com.example.ui.components.filter.specs.AccountsFilterSpec.HIERARCHY_ALL

        fun mapAccountsHierarchy(hierarchy: com.example.ui.screens.AccountViewHierarchyFilter?): String {
            val id = hierarchy?.name?.lowercase()
            return if (id != null && id in VALID_ACCOUNTS_HIERARCHIES) id else DEFAULT_ACCOUNTS_HIERARCHY
        }

        val VALID_ACCOUNTS_STATUSES = setOf(
            com.example.ui.components.filter.specs.AccountsFilterSpec.STATUS_ALL,
            com.example.ui.components.filter.specs.AccountsFilterSpec.STATUS_ACTIVE,
            com.example.ui.components.filter.specs.AccountsFilterSpec.STATUS_INACTIVE
        )
        const val DEFAULT_ACCOUNTS_STATUS = com.example.ui.components.filter.specs.AccountsFilterSpec.STATUS_ALL

        fun mapAccountsStatus(status: com.example.ui.screens.AccountActiveStatusFilter?): String {
            return when (status) {
                com.example.ui.screens.AccountActiveStatusFilter.ACTIVE_ONLY -> com.example.ui.components.filter.specs.AccountsFilterSpec.STATUS_ACTIVE
                com.example.ui.screens.AccountActiveStatusFilter.INACTIVE_ONLY -> com.example.ui.components.filter.specs.AccountsFilterSpec.STATUS_INACTIVE
                else -> com.example.ui.components.filter.specs.AccountsFilterSpec.STATUS_ALL
            }
        }

        val VALID_ACCOUNTS_SORTS = setOf(
            com.example.ui.components.filter.specs.AccountsFilterSpec.SORT_DEFAULT,
            com.example.ui.components.filter.specs.AccountsFilterSpec.SORT_AMOUNT_DESC,
            com.example.ui.components.filter.specs.AccountsFilterSpec.SORT_AMOUNT_ASC,
            com.example.ui.components.filter.specs.AccountsFilterSpec.SORT_MOST_USED,
            com.example.ui.components.filter.specs.AccountsFilterSpec.SORT_LEAST_USED,
            com.example.ui.components.filter.specs.AccountsFilterSpec.SORT_NAME_AZ,
            com.example.ui.components.filter.specs.AccountsFilterSpec.SORT_NAME_ZA
        )
        const val DEFAULT_ACCOUNTS_SORT = com.example.ui.components.filter.specs.AccountsFilterSpec.SORT_DEFAULT

        fun mapAccountsSort(sort: com.example.ui.screens.AccountSortFilter?): String {
            return when (sort) {
                com.example.ui.screens.AccountSortFilter.AMOUNT_HIGH_TO_LOW -> com.example.ui.components.filter.specs.AccountsFilterSpec.SORT_AMOUNT_DESC
                com.example.ui.screens.AccountSortFilter.AMOUNT_LOW_TO_HIGH -> com.example.ui.components.filter.specs.AccountsFilterSpec.SORT_AMOUNT_ASC
                com.example.ui.screens.AccountSortFilter.MOST_USED -> com.example.ui.components.filter.specs.AccountsFilterSpec.SORT_MOST_USED
                com.example.ui.screens.AccountSortFilter.LEAST_USED -> com.example.ui.components.filter.specs.AccountsFilterSpec.SORT_LEAST_USED
                com.example.ui.screens.AccountSortFilter.NAME_AZ -> com.example.ui.components.filter.specs.AccountsFilterSpec.SORT_NAME_AZ
                com.example.ui.screens.AccountSortFilter.NAME_ZA -> com.example.ui.components.filter.specs.AccountsFilterSpec.SORT_NAME_ZA
                else -> com.example.ui.components.filter.specs.AccountsFilterSpec.SORT_DEFAULT
            }
        }

        // Items Summary Mappings
        val VALID_ITEMS_TAB_MODES = setOf(
            com.example.ui.components.filter.specs.ItemsFilterSpec.MODE_EXPENSE,
            com.example.ui.components.filter.specs.ItemsFilterSpec.MODE_ALL,
            com.example.ui.components.filter.specs.ItemsFilterSpec.MODE_INCOME
        )
        const val DEFAULT_ITEMS_TAB_MODE = com.example.ui.components.filter.specs.ItemsFilterSpec.MODE_ALL

        fun mapItemsTabMode(mode: String?): String {
            val id = mode?.lowercase()
            return if (id != null && id in VALID_ITEMS_TAB_MODES) id else DEFAULT_ITEMS_TAB_MODE
        }

        val VALID_ITEMS_DATE_PRESETS = setOf(
            "this_month", "last_month", "this_week", "today", "yesterday",
            "last_30_days", "last_90_days", "this_year", "last_year", "all_time"
        )
        const val DEFAULT_ITEMS_DATE_PRESET = "this_month"

        fun mapItemsDatePreset(preset: com.example.ui.dialogs.AggregatedDatePreset?): String {
            val id = preset?.name?.lowercase()
            return if (id != null && id in VALID_ITEMS_DATE_PRESETS) id else DEFAULT_ITEMS_DATE_PRESET
        }

        val VALID_ITEMS_SORT_ORDERS = setOf(
            com.example.ui.components.filter.specs.ItemsFilterSpec.SORT_AMOUNT_DESC,
            com.example.ui.components.filter.specs.ItemsFilterSpec.SORT_AMOUNT_ASC,
            com.example.ui.components.filter.specs.ItemsFilterSpec.SORT_COUNT_DESC,
            com.example.ui.components.filter.specs.ItemsFilterSpec.SORT_COUNT_ASC,
            com.example.ui.components.filter.specs.ItemsFilterSpec.SORT_AVG_DESC,
            com.example.ui.components.filter.specs.ItemsFilterSpec.SORT_AVG_ASC,
            com.example.ui.components.filter.specs.ItemsFilterSpec.SORT_NAME_ASC,
            com.example.ui.components.filter.specs.ItemsFilterSpec.SORT_NAME_DESC,
            com.example.ui.components.filter.specs.ItemsFilterSpec.SORT_RECENT_DATE,
            com.example.ui.components.filter.specs.ItemsFilterSpec.SORT_DEFAULT
        )
        const val DEFAULT_ITEMS_SORT_ORDER = com.example.ui.components.filter.specs.ItemsFilterSpec.SORT_AMOUNT_DESC

        fun mapItemsSortOrder(sort: com.example.ui.dialogs.AggregatedSortOrder?): String {
            return when (sort) {
                com.example.ui.dialogs.AggregatedSortOrder.AMOUNT_DESC -> com.example.ui.components.filter.specs.ItemsFilterSpec.SORT_AMOUNT_DESC
                com.example.ui.dialogs.AggregatedSortOrder.AMOUNT_ASC -> com.example.ui.components.filter.specs.ItemsFilterSpec.SORT_AMOUNT_ASC
                com.example.ui.dialogs.AggregatedSortOrder.COUNT_DESC -> com.example.ui.components.filter.specs.ItemsFilterSpec.SORT_COUNT_DESC
                com.example.ui.dialogs.AggregatedSortOrder.COUNT_ASC -> com.example.ui.components.filter.specs.ItemsFilterSpec.SORT_COUNT_ASC
                com.example.ui.dialogs.AggregatedSortOrder.AVG_DESC -> com.example.ui.components.filter.specs.ItemsFilterSpec.SORT_AVG_DESC
                com.example.ui.dialogs.AggregatedSortOrder.AVG_ASC -> com.example.ui.components.filter.specs.ItemsFilterSpec.SORT_AVG_ASC
                com.example.ui.dialogs.AggregatedSortOrder.NAME_ASC -> com.example.ui.components.filter.specs.ItemsFilterSpec.SORT_NAME_ASC
                com.example.ui.dialogs.AggregatedSortOrder.NAME_DESC -> com.example.ui.components.filter.specs.ItemsFilterSpec.SORT_NAME_DESC
                com.example.ui.dialogs.AggregatedSortOrder.RECENT_DATE -> com.example.ui.components.filter.specs.ItemsFilterSpec.SORT_RECENT_DATE
                com.example.ui.dialogs.AggregatedSortOrder.DEFAULT -> com.example.ui.components.filter.specs.ItemsFilterSpec.SORT_DEFAULT
                null -> DEFAULT_ITEMS_SORT_ORDER
            }
        }

        @Volatile
        private var instance: FilterStore? = null

        fun getInstance(context: Context): FilterStore {
            return instance ?: synchronized(this) {
                instance ?: FilterStore(context.applicationContext).also { instance = it }
            }
        }

        fun resetInstanceForTesting() {
            instance = null
        }
    }

    /**
     * Saves a FilterState to SharedPreferences as a JSON string for [specKey].
     * Note: FilterValue.Search is stripped so search queries are never persisted.
     */
    fun saveFilterState(specKey: String, state: FilterState) {
        val nonSearchState = state.filter { (_, v) -> v !is FilterValue.Search }
        if (nonSearchState.isEmpty()) {
            prefs.edit().remove("filter_$specKey").apply()
            return
        }

        try {
            val rootObj = JSONObject()
            for ((fieldId, value) in nonSearchState) {
                if (value is FilterValue.Search) continue
                val fieldObj = JSONObject()
                when (value) {
                    is FilterValue.Select -> {
                        fieldObj.put("type", "select")
                        val arr = JSONArray()
                        value.selectedIds.forEach { arr.put(it) }
                        fieldObj.put("selectedIds", arr)
                    }

                    is FilterValue.SingleSelect -> {
                        fieldObj.put("type", "single_select")
                        if (value.selectedId != null) {
                            fieldObj.put("selectedId", value.selectedId)
                        }
                    }

                    is FilterValue.Range -> {
                        fieldObj.put("type", "range")
                        if (value.min != null) fieldObj.put("min", value.min)
                        if (value.max != null) fieldObj.put("max", value.max)
                    }

                    is FilterValue.Date -> {
                        fieldObj.put("type", "date")
                        if (value.presetId != null) fieldObj.put("presetId", value.presetId)
                        if (value.startMs != null) fieldObj.put("startMs", value.startMs)
                        if (value.endMs != null) fieldObj.put("endMs", value.endMs)
                    }

                    is FilterValue.ToggleGroup -> {
                        fieldObj.put("type", "toggle_group")
                        val arr = JSONArray()
                        value.activeIds.forEach { arr.put(it) }
                        fieldObj.put("activeIds", arr)
                    }

                    is FilterValue.Sort -> {
                        fieldObj.put("type", "sort")
                        if (value.sortId != null) fieldObj.put("sortId", value.sortId)
                    }

                    is FilterValue.Search -> {
                        // Stripped: search queries are never persisted
                    }

                    is FilterValue.BooleanVal -> {
                        fieldObj.put("type", "boolean")
                        fieldObj.put("value", value.value)
                    }

                    is FilterValue.Custom -> {
                        fieldObj.put("type", "custom")
                        fieldObj.put("rawJson", value.rawJson)
                    }
                }
                rootObj.put(fieldId, fieldObj)
            }
            prefs.edit().putString("filter_$specKey", rootObj.toString()).commit()
        } catch (_: Exception) {
            // Graceful fallback
        }
    }

    /**
     * Loads a FilterState from SharedPreferences for [specKey].
     * Note: Search values are never restored from storage so stale searches never reappear.
     */
    fun loadFilterState(specKey: String): FilterState {
        val jsonStr = prefs.getString("filter_$specKey", null) ?: return emptyMap()
        val result = mutableMapOf<String, FilterValue>()

        try {
            val rootObj = JSONObject(jsonStr)
            val keys = rootObj.keys()
            while (keys.hasNext()) {
                val fieldId = keys.next()
                val fieldObj = rootObj.getJSONObject(fieldId)
                val type = fieldObj.optString("type", "")

                val value: FilterValue? = when (type) {
                    "select" -> {
                        val arr = fieldObj.optJSONArray("selectedIds")
                        val set = mutableSetOf<String>()
                        if (arr != null) {
                            for (i in 0 until arr.length()) {
                                set.add(arr.getString(i))
                            }
                        }
                        FilterValue.Select(set)
                    }

                    "single_select" -> {
                        val id = if (fieldObj.has("selectedId")) fieldObj.getString("selectedId") else null
                        FilterValue.SingleSelect(id)
                    }

                    "range" -> {
                        val min = if (fieldObj.has("min")) fieldObj.getDouble("min") else null
                        val max = if (fieldObj.has("max")) fieldObj.getDouble("max") else null
                        FilterValue.Range(min = min, max = max)
                    }

                    "date" -> {
                        val presetId = if (fieldObj.has("presetId")) fieldObj.getString("presetId") else null
                        val startMs = if (fieldObj.has("startMs")) fieldObj.getLong("startMs") else null
                        val endMs = if (fieldObj.has("endMs")) fieldObj.getLong("endMs") else null
                        FilterValue.Date(presetId = presetId, startMs = startMs, endMs = endMs)
                    }

                    "toggle_group" -> {
                        val arr = fieldObj.optJSONArray("activeIds")
                        val set = mutableSetOf<String>()
                        if (arr != null) {
                            for (i in 0 until arr.length()) {
                                set.add(arr.getString(i))
                            }
                        }
                        FilterValue.ToggleGroup(set)
                    }

                    "sort" -> {
                        val sortId = if (fieldObj.has("sortId")) fieldObj.getString("sortId") else null
                        FilterValue.Sort(sortId)
                    }

                    "search" -> null

                    "boolean" -> {
                        val b = fieldObj.optBoolean("value", false)
                        FilterValue.BooleanVal(b)
                    }

                    "custom" -> {
                        val raw = fieldObj.optString("rawJson", "")
                        FilterValue.Custom(raw)
                    }

                    else -> null
                }

                if (value != null && value !is FilterValue.Search) {
                    result[fieldId] = value
                }
            }
        } catch (_: Exception) {
            return emptyMap()
        }

        return result
    }

    /**
     * Clears saved state for [specKey].
     */
    fun clearFilterState(specKey: String) {
        prefs.edit().remove("filter_$specKey").commit()
    }

    /**
     * Clears all filter preferences and migration flags (used for deterministic testing).
     */
    fun clearAllForTesting() {
        prefs.edit().clear().commit()
    }

    /**
     * Clears migration flags (used for testing).
     */
    fun resetMigrationFlagsForTesting() {
        prefs.edit()
            .remove(KEY_MIGRATED_LABELS)
            .remove(KEY_MIGRATED_CATEGORIES)
            .remove(KEY_MIGRATED_WISHLIST)
            .remove(KEY_MIGRATED_SAVINGS_GOALS)
            .remove(KEY_MIGRATED_PAYMENT_SOURCE)
            .remove(KEY_MIGRATED_RM_MANAGER)
            .remove(KEY_MIGRATED_ACCOUNTS)
            .remove(KEY_MIGRATED_ITEMS)
            .remove("migrated_tab_filter_preferences_v1")
            .commit()
    }

    /**
     * One-time migration hook from TabFilterPreferences.
     * Uses separate per-screen flags (migrated_labels_v1, migrated_categories_v1, migrated_wishlist_v1, migrated_savings_goals_v1, migrated_payment_source_v1, migrated_rm_manager_v1).
     * Existing users who already have the old flag set still get their values migrated,
     * as long as no filter state exists yet.
     * Existing FilterStore state is never overwritten.
     * Search values are stripped so a stale search query is never persisted.
     */
    fun migrateFromTabFilterPreferences(tabFilterPrefs: TabFilterPreferences) {
        // 1. Migrate LabelsScreen saved preferences with per-screen flag
        try {
            if (!prefs.getBoolean(KEY_MIGRATED_LABELS, false)) {
                val labelsKey = "filter_${com.example.ui.components.filter.specs.LabelsFilterSpec.SPEC_KEY}"
                if (!prefs.contains(labelsKey)) {
                    val labelsState = mutableMapOf<String, FilterValue>()
                    labelsState[com.example.ui.components.filter.specs.LabelsFilterSpec.FIELD_SEGMENT] = FilterValue.ToggleGroup(
                        setOf(mapLabelsCategorySegment(tabFilterPrefs.labelsCategorySegment))
                    )
                    labelsState[com.example.ui.components.filter.specs.LabelsFilterSpec.FIELD_TYPE_MODE] = FilterValue.ToggleGroup(
                        setOf(mapLabelsTabMode(tabFilterPrefs.labelsTabMode))
                    )
                    labelsState[com.example.ui.components.filter.specs.LabelsFilterSpec.FIELD_DATE] = FilterValue.Date(
                        presetId = mapLabelsDatePreset(tabFilterPrefs.labelsDatePreset)
                    )
                    labelsState[com.example.ui.components.filter.specs.LabelsFilterSpec.FIELD_SORT] = FilterValue.Sort(
                        mapLabelsSortOrder(tabFilterPrefs.labelsSortOrder)
                    )
                    saveFilterState(com.example.ui.components.filter.specs.LabelsFilterSpec.SPEC_KEY, labelsState)
                }
                prefs.edit().putBoolean(KEY_MIGRATED_LABELS, true).commit()
            }
        } catch (e: Exception) {
            System.err.println("Labels migration failed: $e")
            e.printStackTrace()
        }

        // 2. Migrate CategoriesScreen saved preferences with per-screen flag
        try {
            if (!prefs.getBoolean(KEY_MIGRATED_CATEGORIES, false)) {
                val categoriesKey = "filter_${com.example.ui.components.filter.specs.CategoriesFilterSpec.SPEC_KEY}"
                if (!prefs.contains(categoriesKey)) {
                    val catState = mutableMapOf<String, FilterValue>()
                    val typeMode = mapCategoriesType(tabFilterPrefs.categoriesTypeFilter)
                    catState[com.example.ui.components.filter.specs.CategoriesFilterSpec.FIELD_TYPE_MODE] = FilterValue.ToggleGroup(setOf(typeMode))
                    catState[com.example.ui.components.filter.specs.CategoriesFilterSpec.FIELD_HIERARCHY] = FilterValue.ToggleGroup(
                        setOf(mapCategoriesHierarchy(tabFilterPrefs.categoriesHierarchyFilter))
                    )
                    catState[com.example.ui.components.filter.specs.CategoriesFilterSpec.FIELD_SORT] = FilterValue.Sort(
                        mapCategoriesSort(tabFilterPrefs.categoriesSortFilter)
                    )
                    saveFilterState(com.example.ui.components.filter.specs.CategoriesFilterSpec.SPEC_KEY, catState)
                }
                prefs.edit().putBoolean(KEY_MIGRATED_CATEGORIES, true).commit()
            }
        } catch (e: Exception) {
            Log.e("FilterStore", "Categories migration failed", e)
        }

        // 3. Migrate WishlistScreen saved preferences with per-screen flag
        try {
            if (!prefs.getBoolean(KEY_MIGRATED_WISHLIST, false)) {
                val wishlistKey = "filter_${com.example.ui.components.filter.specs.WishlistFilterSpec.SPEC_KEY}"
                if (!prefs.contains(wishlistKey)) {
                    val wishlistState = mutableMapOf<String, FilterValue>()
                    val tabId = mapWishlistTab(tabFilterPrefs.wishlistTab)
                    wishlistState[com.example.ui.components.filter.specs.WishlistFilterSpec.FIELD_TAB] = FilterValue.ToggleGroup(setOf(tabId))
                    val sortId = mapWishlistSort(tabFilterPrefs.wishlistSort)
                    wishlistState[com.example.ui.components.filter.specs.WishlistFilterSpec.FIELD_SORT] = FilterValue.Sort(sortId)
                    saveFilterState(com.example.ui.components.filter.specs.WishlistFilterSpec.SPEC_KEY, wishlistState)
                }
                prefs.edit().putBoolean(KEY_MIGRATED_WISHLIST, true).commit()
            }
        } catch (e: Exception) {
            System.err.println("Wishlist migration failed: $e")
            e.printStackTrace()
        }

        // 4. Migrate SavingsGoalsScreen saved preferences with per-screen flag
        try {
            if (!prefs.getBoolean(KEY_MIGRATED_SAVINGS_GOALS, false)) {
                val goalsKey = "filter_${com.example.ui.components.filter.specs.SavingsGoalsFilterSpec.SPEC_KEY}"
                if (!prefs.contains(goalsKey)) {
                    val goalsState = mutableMapOf<String, FilterValue>()
                    val statusId = mapSavingsGoalsStatus(tabFilterPrefs.savingsGoalsFilter)
                    goalsState[com.example.ui.components.filter.specs.SavingsGoalsFilterSpec.FIELD_STATUS] = FilterValue.ToggleGroup(setOf(statusId))
                    saveFilterState(com.example.ui.components.filter.specs.SavingsGoalsFilterSpec.SPEC_KEY, goalsState)
                }
                prefs.edit().putBoolean(KEY_MIGRATED_SAVINGS_GOALS, true).commit()
            }
        } catch (e: Exception) {
            Log.e("FilterStore", "Savings Goals migration failed", e)
        }

        // 5. Migrate PaymentSourceScreen saved preferences with per-screen flag
        try {
            if (!prefs.getBoolean(KEY_MIGRATED_PAYMENT_SOURCE, false)) {
                val psKey = "filter_${com.example.ui.components.filter.specs.PaymentSourceFilterSpec.SPEC_KEY}"
                if (!prefs.contains(psKey)) {
                    val psState = mutableMapOf<String, FilterValue>()
                    val mainTab = mapPaymentSourceTab(tabFilterPrefs.paymentSourceTab)
                    psState[com.example.ui.components.filter.specs.PaymentSourceFilterSpec.FIELD_MAIN_TAB] = FilterValue.ToggleGroup(setOf(mainTab))
                    val calcBasis = mapPaymentSourceCalcBasis(tabFilterPrefs.paymentSourceCalcBasis)
                    psState[com.example.ui.components.filter.specs.PaymentSourceFilterSpec.FIELD_CALC_BASIS] = FilterValue.ToggleGroup(setOf(calcBasis))
                    val sourceStatus = mapPaymentSourceStatus(tabFilterPrefs.paymentSourceAccountStatus)
                    psState[com.example.ui.components.filter.specs.PaymentSourceFilterSpec.FIELD_SOURCE_STATUS] = FilterValue.ToggleGroup(setOf(sourceStatus))
                    val sourceSort = mapPaymentSourceSort(tabFilterPrefs.paymentSourceSortOption)
                    psState[com.example.ui.components.filter.specs.PaymentSourceFilterSpec.FIELD_SOURCE_SORT] = FilterValue.Sort(sourceSort)
                    val assignedSection = mapAssignedSection(tabFilterPrefs.paymentSourceAssignedSection)
                    psState[com.example.ui.components.filter.specs.PaymentSourceFilterSpec.FIELD_ASSIGNED_SECTION] = FilterValue.ToggleGroup(setOf(assignedSection))
                    val assignedStatus = mapAssignedStatus(tabFilterPrefs.paymentSourceAssignedStatus)
                    psState[com.example.ui.components.filter.specs.PaymentSourceFilterSpec.FIELD_ASSIGNED_STATUS] = FilterValue.ToggleGroup(setOf(assignedStatus))
                    val assignedSort = mapAssignedSort(tabFilterPrefs.paymentSourceAssignedSort)
                    psState[com.example.ui.components.filter.specs.PaymentSourceFilterSpec.FIELD_ASSIGNED_SORT] = FilterValue.Sort(assignedSort)
                    saveFilterState(com.example.ui.components.filter.specs.PaymentSourceFilterSpec.SPEC_KEY, psState)
                }
                prefs.edit().putBoolean(KEY_MIGRATED_PAYMENT_SOURCE, true).commit()
            }
        } catch (e: Exception) {
            System.err.println("Payment Source migration failed: $e")
            e.printStackTrace()
        }

        // 6. Migrate RmManagerScreen saved preferences with per-screen flag
        try {
            if (!prefs.getBoolean(KEY_MIGRATED_RM_MANAGER, false)) {
                val rmKey = "filter_${com.example.ui.components.filter.specs.RmManagerFilterSpec.SPEC_KEY}"
                if (!prefs.contains(rmKey)) {
                    val rmState = mutableMapOf<String, FilterValue>()
                    val catId = mapRmCategory(tabFilterPrefs.rmFilterCategory)
                    rmState[com.example.ui.components.filter.specs.RmManagerFilterSpec.FIELD_CATEGORY] = FilterValue.ToggleGroup(setOf(catId))
                    val sortId = mapRmSort(tabFilterPrefs.rmSortOption)
                    rmState[com.example.ui.components.filter.specs.RmManagerFilterSpec.FIELD_SORT] = FilterValue.Sort(sortId)
                    saveFilterState(com.example.ui.components.filter.specs.RmManagerFilterSpec.SPEC_KEY, rmState)
                }
                prefs.edit().putBoolean(KEY_MIGRATED_RM_MANAGER, true).commit()
            }
        } catch (e: Exception) {
            System.err.println("RM Manager migration failed: $e")
            e.printStackTrace()
        }

        // 7. Migrate AccountsScreen saved preferences with per-screen flag
        try {
            if (!prefs.getBoolean(KEY_MIGRATED_ACCOUNTS, false)) {
                val accountsKey = "filter_${com.example.ui.components.filter.specs.AccountsFilterSpec.SPEC_KEY}"
                if (!prefs.contains(accountsKey)) {
                    val accState = mutableMapOf<String, FilterValue>()
                    val typeMode = mapAccountsType(tabFilterPrefs.accountsTypeFilter)
                    accState[com.example.ui.components.filter.specs.AccountsFilterSpec.FIELD_TYPE] = FilterValue.ToggleGroup(setOf(typeMode))
                    val hierarchyMode = mapAccountsHierarchy(tabFilterPrefs.accountsHierarchyFilter)
                    accState[com.example.ui.components.filter.specs.AccountsFilterSpec.FIELD_HIERARCHY] = FilterValue.ToggleGroup(setOf(hierarchyMode))
                    val statusMode = mapAccountsStatus(tabFilterPrefs.accountsStatusFilter)
                    accState[com.example.ui.components.filter.specs.AccountsFilterSpec.FIELD_STATUS] = FilterValue.ToggleGroup(setOf(statusMode))
                    if (tabFilterPrefs.accountsExcludeZeroBalance) {
                        accState[com.example.ui.components.filter.specs.AccountsFilterSpec.FIELD_EXCLUDE_ZERO] = FilterValue.ToggleGroup(
                            setOf(com.example.ui.components.filter.specs.AccountsFilterSpec.EXCLUDE_ZERO_ID)
                        )
                    }
                    val sortMode = mapAccountsSort(tabFilterPrefs.accountsSortFilter)
                    accState[com.example.ui.components.filter.specs.AccountsFilterSpec.FIELD_SORT] = FilterValue.Sort(sortMode)
                    saveFilterState(com.example.ui.components.filter.specs.AccountsFilterSpec.SPEC_KEY, accState)
                }
                prefs.edit().putBoolean(KEY_MIGRATED_ACCOUNTS, true).commit()
            }
        } catch (e: Exception) {
            System.err.println("Accounts migration failed: $e")
            e.printStackTrace()
        }

        // 8. Migrate ItemsScreen saved preferences with per-screen flag
        try {
            if (!prefs.getBoolean(KEY_MIGRATED_ITEMS, false)) {
                val itemsKey = "filter_${com.example.ui.components.filter.specs.ItemsFilterSpec.SPEC_KEY}"
                if (!prefs.contains(itemsKey)) {
                    val itemsState = mutableMapOf<String, FilterValue>()
                    val tabMode = mapItemsTabMode(tabFilterPrefs.itemsTabMode)
                    itemsState[com.example.ui.components.filter.specs.ItemsFilterSpec.FIELD_TYPE_MODE] = FilterValue.ToggleGroup(setOf(tabMode))
                    val datePreset = mapItemsDatePreset(tabFilterPrefs.itemsDatePreset)
                    itemsState[com.example.ui.components.filter.specs.ItemsFilterSpec.FIELD_DATE] = FilterValue.Date(presetId = datePreset)
                    val sortOrder = mapItemsSortOrder(tabFilterPrefs.itemsSortOrder)
                    itemsState[com.example.ui.components.filter.specs.ItemsFilterSpec.FIELD_SORT] = FilterValue.Sort(sortOrder)
                    saveFilterState(com.example.ui.components.filter.specs.ItemsFilterSpec.SPEC_KEY, itemsState)
                }
                prefs.edit().putBoolean(KEY_MIGRATED_ITEMS, true).commit()
            }
        } catch (e: Exception) {
            System.err.println("Items migration failed: $e")
            e.printStackTrace()
        }
    }
}
