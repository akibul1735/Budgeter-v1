package com.example.util

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.AccountType
import com.example.data.model.CategoryType
import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionType
import com.example.ui.components.BudgetDateRangePreset
import com.example.ui.components.NetEarningsFilterState
import com.example.ui.dialogs.AggregatedDatePreset
import com.example.ui.dialogs.AggregatedFilterState
import com.example.ui.dialogs.AggregatedSortOrder
import com.example.ui.dialogs.BudgetMakerDashboardFilter
import com.example.ui.dialogs.BudgetMakerTabFilter
import com.example.ui.screens.LedgerDatePreset
import com.example.ui.screens.LedgerRowStyle
import com.example.ui.dialogs.TransactionDisplaySettings
import com.example.ui.screens.AccountActiveStatusFilter
import com.example.ui.screens.AccountSortFilter
import com.example.ui.screens.AccountViewHierarchyFilter
import com.example.ui.screens.BudgetFilterOption
import com.example.ui.screens.BudgetSortOption
import com.example.ui.screens.CategorySortFilter
import com.example.ui.screens.CategoryViewHierarchyFilter
import com.example.ui.screens.CashFlowTabSection
import com.example.ui.screens.GoalFilterType
import com.example.ui.screens.NetEarningsHierarchyView
import com.example.ui.screens.NetEarningsSort
import com.example.ui.screens.WishlistFilterTab
import com.example.ui.screens.WishlistSort
import com.example.data.model.RequirementCalculationBasis
import com.example.ui.components.BudgetComparisonPreset
import com.example.ui.components.BudgetFilterState
import com.example.ui.components.BudgetSortOrder
import com.example.ui.screens.AccountStatusFilter
import com.example.ui.screens.AssignedItemSectionFilter
import com.example.ui.screens.AssignedItemSortOption
import com.example.ui.screens.AssignedItemStatusFilter
import com.example.ui.screens.BalanceSheetFilterState
import com.example.ui.screens.MainPaymentSourceTab
import com.example.ui.screens.PaymentSourceSortOption
import com.example.ui.screens.RmDetailViewMode
import com.example.util.BalanceSheetComparisonPreset
import com.example.util.BalanceSheetSortOrder
import com.example.util.RmManagerHelper
import org.json.JSONArray
import org.json.JSONObject

/**
 * TabFilterPreferences manages in-memory and persistent storage of filter, sort, search,
 * and display options across all tabs in the app.
 *
 * This ensures that whenever the user navigates between tabs or returns to a screen,
 * their selected filter and sort configurations are remembered and restored immediately.
 */
class TabFilterPreferences private constructor(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("budgeter_tab_filter_sort_prefs", Context.MODE_PRIVATE)

    // ==========================================
    // 1. LEDGER (TRANSACTIONS)
    // ==========================================
    var ledgerSearchQuery: String = prefs.getString(KEY_LEDGER_SEARCH, "") ?: ""
        set(value) {
            field = value
            prefs.edit().putString(KEY_LEDGER_SEARCH, value).apply()
        }

    var ledgerTypeFilter: TransactionType? = prefs.getString(KEY_LEDGER_TYPE, null)?.let {
        try { TransactionType.valueOf(it) } catch (_: Exception) { null }
    }
        set(value) {
            field = value
            if (value != null) prefs.edit().putString(KEY_LEDGER_TYPE, value.name).apply()
            else prefs.edit().remove(KEY_LEDGER_TYPE).apply()
        }

    var ledgerDatePreset: LedgerDatePreset = try {
        LedgerDatePreset.valueOf(prefs.getString(KEY_LEDGER_DATE_PRESET, LedgerDatePreset.LAST_12_MONTHS.name) ?: LedgerDatePreset.LAST_12_MONTHS.name)
    } catch (_: Exception) {
        LedgerDatePreset.LAST_12_MONTHS
    }
        set(value) {
            field = value
            prefs.edit().putString(KEY_LEDGER_DATE_PRESET, value.name).apply()
        }

    var ledgerCustomStartDateMs: Long = prefs.getLong(KEY_LEDGER_CUSTOM_START, 0L)
        set(value) {
            field = value
            prefs.edit().putLong(KEY_LEDGER_CUSTOM_START, value).apply()
        }

    var ledgerCustomEndDateMs: Long = prefs.getLong(KEY_LEDGER_CUSTOM_END, System.currentTimeMillis())
        set(value) {
            field = value
            prefs.edit().putLong(KEY_LEDGER_CUSTOM_END, value).apply()
        }

    var ledgerMinAmount: Double = java.lang.Double.longBitsToDouble(prefs.getLong(KEY_LEDGER_MIN_AMOUNT, java.lang.Double.doubleToRawLongBits(0.0)))
        set(value) {
            field = value
            prefs.edit().putLong(KEY_LEDGER_MIN_AMOUNT, java.lang.Double.doubleToRawLongBits(value)).apply()
        }

    var ledgerMaxAmount: Double = java.lang.Double.longBitsToDouble(prefs.getLong(KEY_LEDGER_MAX_AMOUNT, java.lang.Double.doubleToRawLongBits(Double.MAX_VALUE)))
        set(value) {
            field = value
            prefs.edit().putLong(KEY_LEDGER_MAX_AMOUNT, java.lang.Double.doubleToRawLongBits(value)).apply()
        }

    var ledgerCategoryId: Long? = if (prefs.contains(KEY_LEDGER_CAT_ID)) prefs.getLong(KEY_LEDGER_CAT_ID, -1L).takeIf { it != -1L } else null
        set(value) {
            field = value
            if (value != null) prefs.edit().putLong(KEY_LEDGER_CAT_ID, value).apply()
            else prefs.edit().remove(KEY_LEDGER_CAT_ID).apply()
        }

    var ledgerAccountId: Long? = if (prefs.contains(KEY_LEDGER_ACC_ID)) prefs.getLong(KEY_LEDGER_ACC_ID, -1L).takeIf { it != -1L } else null
        set(value) {
            field = value
            if (value != null) prefs.edit().putLong(KEY_LEDGER_ACC_ID, value).apply()
            else prefs.edit().remove(KEY_LEDGER_ACC_ID).apply()
        }

    var ledgerLabel: String? = prefs.getString(KEY_LEDGER_LABEL, null)
        set(value) {
            field = value
            if (value != null) prefs.edit().putString(KEY_LEDGER_LABEL, value).apply()
            else prefs.edit().remove(KEY_LEDGER_LABEL).apply()
        }

    var ledgerStatus: TransactionStatus? = prefs.getString(KEY_LEDGER_STATUS, null)?.let {
        try { TransactionStatus.valueOf(it) } catch (_: Exception) { null }
    }
        set(value) {
            field = value
            if (value != null) prefs.edit().putString(KEY_LEDGER_STATUS, value.name).apply()
            else prefs.edit().remove(KEY_LEDGER_STATUS).apply()
        }

    var ledgerRowStyle: LedgerRowStyle = try {
        LedgerRowStyle.valueOf(prefs.getString(KEY_LEDGER_ROW_STYLE, LedgerRowStyle.STANDARD.name) ?: LedgerRowStyle.STANDARD.name)
    } catch (_: Exception) {
        LedgerRowStyle.STANDARD
    }
        set(value) {
            field = value
            prefs.edit().putString(KEY_LEDGER_ROW_STYLE, value.name).apply()
        }

    var ledgerDisplaySettings: TransactionDisplaySettings = TransactionDisplaySettings(
        showTotalAmount = prefs.getBoolean(KEY_LEDGER_SHOW_TOTAL_AMOUNT, true),
        showTransfersInTotal = prefs.getBoolean(KEY_LEDGER_SHOW_TRANSFERS_TOTAL, false),
        showAllTransactionsForNewAccount = prefs.getBoolean(KEY_LEDGER_SHOW_ALL_NEW_ACCOUNT, false),
        showAccountBalance = prefs.getBoolean(KEY_LEDGER_SHOW_ACCOUNT_BALANCE, true),
        showOldestDateFirst = prefs.getBoolean(KEY_LEDGER_SHOW_OLDEST_FIRST, false)
    )
        set(value) {
            field = value
            prefs.edit()
                .putBoolean(KEY_LEDGER_SHOW_TOTAL_AMOUNT, value.showTotalAmount)
                .putBoolean(KEY_LEDGER_SHOW_TRANSFERS_TOTAL, value.showTransfersInTotal)
                .putBoolean(KEY_LEDGER_SHOW_ALL_NEW_ACCOUNT, value.showAllTransactionsForNewAccount)
                .putBoolean(KEY_LEDGER_SHOW_ACCOUNT_BALANCE, value.showAccountBalance)
                .putBoolean(KEY_LEDGER_SHOW_OLDEST_FIRST, value.showOldestDateFirst)
                .apply()
        }

    // ==========================================
    // 2. ACCOUNTS
    // ==========================================
    var accountsTypeFilter: AccountType? = prefs.getString(KEY_ACCOUNTS_TYPE, null)?.let {
        try { AccountType.valueOf(it) } catch (_: Exception) { null }
    }
        set(value) {
            field = value
            if (value != null) prefs.edit().putString(KEY_ACCOUNTS_TYPE, value.name).apply()
            else prefs.edit().remove(KEY_ACCOUNTS_TYPE).apply()
        }

    var accountsHierarchyFilter: AccountViewHierarchyFilter = try {
        AccountViewHierarchyFilter.valueOf(prefs.getString(KEY_ACCOUNTS_HIERARCHY, AccountViewHierarchyFilter.ALL.name) ?: AccountViewHierarchyFilter.ALL.name)
    } catch (_: Exception) {
        AccountViewHierarchyFilter.ALL
    }
        set(value) {
            field = value
            prefs.edit().putString(KEY_ACCOUNTS_HIERARCHY, value.name).apply()
        }

    var accountsSortFilter: AccountSortFilter = try {
        AccountSortFilter.valueOf(prefs.getString(KEY_ACCOUNTS_SORT, AccountSortFilter.DEFAULT.name) ?: AccountSortFilter.DEFAULT.name)
    } catch (_: Exception) {
        AccountSortFilter.DEFAULT
    }
        set(value) {
            field = value
            prefs.edit().putString(KEY_ACCOUNTS_SORT, value.name).apply()
        }

    var accountsStatusFilter: AccountActiveStatusFilter = try {
        AccountActiveStatusFilter.valueOf(prefs.getString(KEY_ACCOUNTS_STATUS, AccountActiveStatusFilter.ALL.name) ?: AccountActiveStatusFilter.ALL.name)
    } catch (_: Exception) {
        AccountActiveStatusFilter.ALL
    }
        set(value) {
            field = value
            prefs.edit().putString(KEY_ACCOUNTS_STATUS, value.name).apply()
        }

    var accountsExcludeZeroBalance: Boolean = prefs.getBoolean(KEY_ACCOUNTS_EXCLUDE_ZERO, false)
        set(value) {
            field = value
            prefs.edit().putBoolean(KEY_ACCOUNTS_EXCLUDE_ZERO, value).apply()
        }

    var accountsSearchQuery: String = prefs.getString(KEY_ACCOUNTS_SEARCH, "") ?: ""
        set(value) {
            field = value
            prefs.edit().putString(KEY_ACCOUNTS_SEARCH, value).apply()
        }

    // ==========================================
    // 3. CATEGORIES
    // ==========================================
    var categoriesTypeFilter: CategoryType? = prefs.getString(KEY_CATEGORIES_TYPE, CategoryType.EXPENSE.name)?.let {
        try { CategoryType.valueOf(it) } catch (_: Exception) { CategoryType.EXPENSE }
    }
        set(value) {
            field = value
            if (value != null) prefs.edit().putString(KEY_CATEGORIES_TYPE, value.name).apply()
            else prefs.edit().remove(KEY_CATEGORIES_TYPE).apply()
        }

    var categoriesHierarchyFilter: CategoryViewHierarchyFilter = try {
        CategoryViewHierarchyFilter.valueOf(prefs.getString(KEY_CATEGORIES_HIERARCHY, CategoryViewHierarchyFilter.ALL.name) ?: CategoryViewHierarchyFilter.ALL.name)
    } catch (_: Exception) {
        CategoryViewHierarchyFilter.ALL
    }
        set(value) {
            field = value
            prefs.edit().putString(KEY_CATEGORIES_HIERARCHY, value.name).apply()
        }

    var categoriesSortFilter: CategorySortFilter = try {
        CategorySortFilter.valueOf(prefs.getString(KEY_CATEGORIES_SORT, CategorySortFilter.DEFAULT.name) ?: CategorySortFilter.DEFAULT.name)
    } catch (_: Exception) {
        CategorySortFilter.DEFAULT
    }
        set(value) {
            field = value
            prefs.edit().putString(KEY_CATEGORIES_SORT, value.name).apply()
        }

    var categoriesSearchQuery: String = prefs.getString(KEY_CATEGORIES_SEARCH, "") ?: ""
        set(value) {
            field = value
            prefs.edit().putString(KEY_CATEGORIES_SEARCH, value).apply()
        }

    // ==========================================
    // 4. ITEMS SUMMARY
    // ==========================================
    var itemsTabMode: String = prefs.getString(KEY_ITEMS_TAB_MODE, "EXPENSE") ?: "EXPENSE"
        set(value) {
            field = value
            prefs.edit().putString(KEY_ITEMS_TAB_MODE, value).apply()
        }

    var itemsSearchQuery: String = prefs.getString(KEY_ITEMS_SEARCH, "") ?: ""
        set(value) {
            field = value
            prefs.edit().putString(KEY_ITEMS_SEARCH, value).apply()
        }

    var itemsDatePreset: AggregatedDatePreset = try {
        AggregatedDatePreset.valueOf(prefs.getString(KEY_ITEMS_DATE_PRESET, AggregatedDatePreset.THIS_MONTH.name) ?: AggregatedDatePreset.THIS_MONTH.name)
    } catch (_: Exception) {
        AggregatedDatePreset.THIS_MONTH
    }
        set(value) {
            field = value
            prefs.edit().putString(KEY_ITEMS_DATE_PRESET, value.name).apply()
        }

    var itemsSortOrder: AggregatedSortOrder = try {
        AggregatedSortOrder.valueOf(prefs.getString(KEY_ITEMS_SORT_ORDER, AggregatedSortOrder.AMOUNT_DESC.name) ?: AggregatedSortOrder.AMOUNT_DESC.name)
    } catch (_: Exception) {
        AggregatedSortOrder.AMOUNT_DESC
    }
        set(value) {
            field = value
            prefs.edit().putString(KEY_ITEMS_SORT_ORDER, value.name).apply()
        }

    // ==========================================
    // 5. LABELS
    // ==========================================
    var labelsTabMode: String = prefs.getString(KEY_LABELS_TAB_MODE, "EXPENSE") ?: "EXPENSE"
        set(value) {
            field = value
            prefs.edit().putString(KEY_LABELS_TAB_MODE, value).apply()
        }

    var labelsSearchQuery: String = prefs.getString(KEY_LABELS_SEARCH, "") ?: ""
        set(value) {
            field = value
            prefs.edit().putString(KEY_LABELS_SEARCH, value).apply()
        }

    var labelsDatePreset: AggregatedDatePreset = try {
        AggregatedDatePreset.valueOf(prefs.getString(KEY_LABELS_DATE_PRESET, AggregatedDatePreset.THIS_MONTH.name) ?: AggregatedDatePreset.THIS_MONTH.name)
    } catch (_: Exception) {
        AggregatedDatePreset.THIS_MONTH
    }
        set(value) {
            field = value
            prefs.edit().putString(KEY_LABELS_DATE_PRESET, value.name).apply()
        }

    var labelsSortOrder: AggregatedSortOrder = try {
        AggregatedSortOrder.valueOf(prefs.getString(KEY_LABELS_SORT_ORDER, AggregatedSortOrder.AMOUNT_DESC.name) ?: AggregatedSortOrder.AMOUNT_DESC.name)
    } catch (_: Exception) {
        AggregatedSortOrder.AMOUNT_DESC
    }
        set(value) {
            field = value
            prefs.edit().putString(KEY_LABELS_SORT_ORDER, value.name).apply()
        }

    // ==========================================
    // 6. SAVINGS GOALS
    // ==========================================
    var savingsGoalsFilter: GoalFilterType = try {
        GoalFilterType.valueOf(prefs.getString(KEY_GOALS_FILTER, GoalFilterType.ALL.name) ?: GoalFilterType.ALL.name)
    } catch (_: Exception) {
        GoalFilterType.ALL
    }
        set(value) {
            field = value
            prefs.edit().putString(KEY_GOALS_FILTER, value.name).apply()
        }

    var savingsGoalsSearchQuery: String = prefs.getString(KEY_GOALS_SEARCH, "") ?: ""
        set(value) {
            field = value
            prefs.edit().putString(KEY_GOALS_SEARCH, value).apply()
        }

    // ==========================================
    // 7. WISHLIST
    // ==========================================
    var wishlistTab: WishlistFilterTab = try {
        WishlistFilterTab.valueOf(prefs.getString(KEY_WISHLIST_TAB, WishlistFilterTab.ACTIVE.name) ?: WishlistFilterTab.ACTIVE.name)
    } catch (_: Exception) {
        WishlistFilterTab.ACTIVE
    }
        set(value) {
            field = value
            prefs.edit().putString(KEY_WISHLIST_TAB, value.name).apply()
        }

    var wishlistSort: WishlistSort = try {
        WishlistSort.valueOf(prefs.getString(KEY_WISHLIST_SORT, WishlistSort.PRIORITY.name) ?: WishlistSort.PRIORITY.name)
    } catch (_: Exception) {
        WishlistSort.PRIORITY
    }
        set(value) {
            field = value
            prefs.edit().putString(KEY_WISHLIST_SORT, value.name).apply()
        }

    var wishlistSearchQuery: String = prefs.getString(KEY_WISHLIST_SEARCH, "") ?: ""
        set(value) {
            field = value
            prefs.edit().putString(KEY_WISHLIST_SEARCH, value).apply()
        }

    // ==========================================
    // 8. REPORTS / NET EARNINGS
    // ==========================================
    var reportsTabMode: String = prefs.getString(KEY_REPORTS_TAB_MODE, "EXPENSE") ?: "EXPENSE"
        set(value) {
            field = value
            prefs.edit().putString(KEY_REPORTS_TAB_MODE, value).apply()
        }

    var reportsHierarchyView: NetEarningsHierarchyView = try {
        NetEarningsHierarchyView.valueOf(prefs.getString(KEY_REPORTS_HIERARCHY, NetEarningsHierarchyView.GROUPED.name) ?: NetEarningsHierarchyView.GROUPED.name)
    } catch (_: Exception) {
        NetEarningsHierarchyView.GROUPED
    }
        set(value) {
            field = value
            prefs.edit().putString(KEY_REPORTS_HIERARCHY, value.name).apply()
        }

    var reportsSortOption: NetEarningsSort = try {
        NetEarningsSort.valueOf(prefs.getString(KEY_REPORTS_SORT, NetEarningsSort.AMOUNT_HIGH_TO_LOW.name) ?: NetEarningsSort.AMOUNT_HIGH_TO_LOW.name)
    } catch (_: Exception) {
        NetEarningsSort.AMOUNT_HIGH_TO_LOW
    }
        set(value) {
            field = value
            prefs.edit().putString(KEY_REPORTS_SORT, value.name).apply()
        }

    var reportsDatePreset: BudgetDateRangePreset = try {
        BudgetDateRangePreset.valueOf(prefs.getString(KEY_REPORTS_DATE_PRESET, BudgetDateRangePreset.THIS_MONTH.name) ?: BudgetDateRangePreset.THIS_MONTH.name)
    } catch (_: Exception) {
        BudgetDateRangePreset.THIS_MONTH
    }
        set(value) {
            field = value
            prefs.edit().putString(KEY_REPORTS_DATE_PRESET, value.name).apply()
        }

    var reportsSearchQuery: String = prefs.getString(KEY_REPORTS_SEARCH, "") ?: ""
        set(value) {
            field = value
            prefs.edit().putString(KEY_REPORTS_SEARCH, value).apply()
        }

    // ==========================================
    // 9. TRASH
    // ==========================================
    var trashFilter: TrashItemType? = prefs.getString(KEY_TRASH_FILTER, null)?.let {
        try { TrashItemType.valueOf(it) } catch (_: Exception) { null }
    }
        set(value) {
            field = value
            if (value != null) prefs.edit().putString(KEY_TRASH_FILTER, value.name).apply()
            else prefs.edit().remove(KEY_TRASH_FILTER).apply()
        }

    // ==========================================
    // 10. CASH FLOW
    // ==========================================
    var cashFlowPreset: CashFlowPeriodPreset = try {
        CashFlowPeriodPreset.valueOf(prefs.getString(KEY_CASHFLOW_PRESET, CashFlowPeriodPreset.THIS_MONTH.name) ?: CashFlowPeriodPreset.THIS_MONTH.name)
    } catch (_: Exception) {
        CashFlowPeriodPreset.THIS_MONTH
    }
        set(value) {
            field = value
            prefs.edit().putString(KEY_CASHFLOW_PRESET, value.name).apply()
        }

    var cashFlowSection: CashFlowTabSection = try {
        CashFlowTabSection.valueOf(prefs.getString(KEY_CASHFLOW_SECTION, CashFlowTabSection.OVERVIEW.name) ?: CashFlowTabSection.OVERVIEW.name)
    } catch (_: Exception) {
        CashFlowTabSection.OVERVIEW
    }
        set(value) {
            field = value
            prefs.edit().putString(KEY_CASHFLOW_SECTION, value.name).apply()
        }

    var cashFlowSearchQuery: String = prefs.getString(KEY_CASHFLOW_SEARCH, "") ?: ""
        set(value) {
            field = value
            prefs.edit().putString(KEY_CASHFLOW_SEARCH, value).apply()
        }

    // ==========================================
    // 11. BUDGET MAKER
    // ==========================================
    var budgetSearchQuery: String = prefs.getString(KEY_BUDGET_SEARCH, "") ?: ""
        set(value) {
            field = value
            prefs.edit().putString(KEY_BUDGET_SEARCH, value).apply()
        }

    var budgetExpenseSort: BudgetSortOption = try {
        BudgetSortOption.valueOf(prefs.getString(KEY_BUDGET_EXPENSE_SORT, BudgetSortOption.DEFAULT.name) ?: BudgetSortOption.DEFAULT.name)
    } catch (_: Exception) {
        BudgetSortOption.DEFAULT
    }
        set(value) {
            field = value
            prefs.edit().putString(KEY_BUDGET_EXPENSE_SORT, value.name).apply()
        }

    var budgetIncomeSort: BudgetSortOption = try {
        BudgetSortOption.valueOf(prefs.getString(KEY_BUDGET_INCOME_SORT, BudgetSortOption.DEFAULT.name) ?: BudgetSortOption.DEFAULT.name)
    } catch (_: Exception) {
        BudgetSortOption.DEFAULT
    }
        set(value) {
            field = value
            prefs.edit().putString(KEY_BUDGET_INCOME_SORT, value.name).apply()
        }

    var budgetAssetSort: BudgetSortOption = try {
        BudgetSortOption.valueOf(prefs.getString(KEY_BUDGET_ASSET_SORT, BudgetSortOption.DEFAULT.name) ?: BudgetSortOption.DEFAULT.name)
    } catch (_: Exception) {
        BudgetSortOption.DEFAULT
    }
        set(value) {
            field = value
            prefs.edit().putString(KEY_BUDGET_ASSET_SORT, value.name).apply()
        }

    var budgetLiabilitySort: BudgetSortOption = try {
        BudgetSortOption.valueOf(prefs.getString(KEY_BUDGET_LIABILITY_SORT, BudgetSortOption.DEFAULT.name) ?: BudgetSortOption.DEFAULT.name)
    } catch (_: Exception) {
        BudgetSortOption.DEFAULT
    }
        set(value) {
            field = value
            prefs.edit().putString(KEY_BUDGET_LIABILITY_SORT, value.name).apply()
        }

    var budgetMakerSelectedTab: Int = prefs.getInt(KEY_BUDGET_MAKER_TAB, 0)
        set(value) {
            field = value
            prefs.edit().putInt(KEY_BUDGET_MAKER_TAB, value).apply()
        }

    var budgetExpenseQuickFilters: Set<BudgetFilterOption> = loadBudgetFilterOptions(KEY_BUDGET_EXPENSE_FILTERS)
        set(value) {
            field = value
            saveBudgetFilterOptions(KEY_BUDGET_EXPENSE_FILTERS, value)
        }

    var budgetIncomeQuickFilters: Set<BudgetFilterOption> = loadBudgetFilterOptions(KEY_BUDGET_INCOME_FILTERS)
        set(value) {
            field = value
            saveBudgetFilterOptions(KEY_BUDGET_INCOME_FILTERS, value)
        }

    var budgetAssetQuickFilters: Set<BudgetFilterOption> = loadBudgetFilterOptions(KEY_BUDGET_ASSET_FILTERS)
        set(value) {
            field = value
            saveBudgetFilterOptions(KEY_BUDGET_ASSET_FILTERS, value)
        }

    var budgetLiabilityQuickFilters: Set<BudgetFilterOption> = loadBudgetFilterOptions(KEY_BUDGET_LIABILITY_FILTERS)
        set(value) {
            field = value
            saveBudgetFilterOptions(KEY_BUDGET_LIABILITY_FILTERS, value)
        }

    // ==========================================
    // 12. RM MANAGER
    // ==========================================
    var rmSearchQuery: String = prefs.getString(KEY_RM_SEARCH, "") ?: ""
        set(value) {
            field = value
            prefs.edit().putString(KEY_RM_SEARCH, value).apply()
        }

    var rmFilterCategory: RmManagerHelper.RmFilterCategory = try {
        RmManagerHelper.RmFilterCategory.valueOf(prefs.getString(KEY_RM_CATEGORY, RmManagerHelper.RmFilterCategory.ALL.name) ?: RmManagerHelper.RmFilterCategory.ALL.name)
    } catch (_: Exception) {
        RmManagerHelper.RmFilterCategory.ALL
    }
        set(value) {
            field = value
            prefs.edit().putString(KEY_RM_CATEGORY, value.name).apply()
        }

    var rmSortOption: RmManagerHelper.RmSortOption = try {
        RmManagerHelper.RmSortOption.valueOf(prefs.getString(KEY_RM_SORT, RmManagerHelper.RmSortOption.HIGHEST_DUE.name) ?: RmManagerHelper.RmSortOption.HIGHEST_DUE.name)
    } catch (_: Exception) {
        RmManagerHelper.RmSortOption.HIGHEST_DUE
    }
        set(value) {
            field = value
            prefs.edit().putString(KEY_RM_SORT, value.name).apply()
        }

    var rmDetailViewMode: RmDetailViewMode = try {
        RmDetailViewMode.valueOf(prefs.getString(KEY_RM_DETAIL_MODE, RmDetailViewMode.KHATIAN_LEDGER.name) ?: RmDetailViewMode.KHATIAN_LEDGER.name)
    } catch (_: Exception) {
        RmDetailViewMode.KHATIAN_LEDGER
    }
        set(value) {
            field = value
            prefs.edit().putString(KEY_RM_DETAIL_MODE, value.name).apply()
        }

    var rmTimelineFilter: Int = prefs.getInt(KEY_RM_TIMELINE_FILTER, 0)
        set(value) {
            field = value
            prefs.edit().putInt(KEY_RM_TIMELINE_FILTER, value).apply()
        }

    // ==========================================
    // 13. PAYMENT SOURCE
    // ==========================================
    var paymentSourceTab: MainPaymentSourceTab = try {
        MainPaymentSourceTab.valueOf(prefs.getString(KEY_PAYMENT_SOURCE_TAB, MainPaymentSourceTab.PAYMENT_SOURCES.name) ?: MainPaymentSourceTab.PAYMENT_SOURCES.name)
    } catch (_: Exception) {
        MainPaymentSourceTab.PAYMENT_SOURCES
    }
        set(value) {
            field = value
            prefs.edit().putString(KEY_PAYMENT_SOURCE_TAB, value.name).apply()
        }

    var paymentSourceCalcBasis: RequirementCalculationBasis = try {
        RequirementCalculationBasis.valueOf(prefs.getString(KEY_PAYMENT_SOURCE_CALC_BASIS, RequirementCalculationBasis.BUDGET_AMOUNT.name) ?: RequirementCalculationBasis.BUDGET_AMOUNT.name)
    } catch (_: Exception) {
        RequirementCalculationBasis.BUDGET_AMOUNT
    }
        set(value) {
            field = value
            prefs.edit().putString(KEY_PAYMENT_SOURCE_CALC_BASIS, value.name).apply()
        }

    var paymentSourceAccountStatus: AccountStatusFilter = try {
        AccountStatusFilter.valueOf(prefs.getString(KEY_PAYMENT_SOURCE_ACCOUNT_STATUS, AccountStatusFilter.ALL.name) ?: AccountStatusFilter.ALL.name)
    } catch (_: Exception) {
        AccountStatusFilter.ALL
    }
        set(value) {
            field = value
            prefs.edit().putString(KEY_PAYMENT_SOURCE_ACCOUNT_STATUS, value.name).apply()
        }

    var paymentSourceSortOption: PaymentSourceSortOption = try {
        PaymentSourceSortOption.valueOf(prefs.getString(KEY_PAYMENT_SOURCE_SORT, PaymentSourceSortOption.DEFAULT.name) ?: PaymentSourceSortOption.DEFAULT.name)
    } catch (_: Exception) {
        PaymentSourceSortOption.DEFAULT
    }
        set(value) {
            field = value
            prefs.edit().putString(KEY_PAYMENT_SOURCE_SORT, value.name).apply()
        }

    var paymentSourceAssignedSection: AssignedItemSectionFilter = try {
        AssignedItemSectionFilter.valueOf(prefs.getString(KEY_PAYMENT_SOURCE_ASSIGNED_SECTION, AssignedItemSectionFilter.ALL.name) ?: AssignedItemSectionFilter.ALL.name)
    } catch (_: Exception) {
        AssignedItemSectionFilter.ALL
    }
        set(value) {
            field = value
            prefs.edit().putString(KEY_PAYMENT_SOURCE_ASSIGNED_SECTION, value.name).apply()
        }

    var paymentSourceAssignedStatus: AssignedItemStatusFilter = try {
        AssignedItemStatusFilter.valueOf(prefs.getString(KEY_PAYMENT_SOURCE_ASSIGNED_STATUS, AssignedItemStatusFilter.ALL.name) ?: AssignedItemStatusFilter.ALL.name)
    } catch (_: Exception) {
        AssignedItemStatusFilter.ALL
    }
        set(value) {
            field = value
            prefs.edit().putString(KEY_PAYMENT_SOURCE_ASSIGNED_STATUS, value.name).apply()
        }

    var paymentSourceAssignedSort: AssignedItemSortOption = try {
        AssignedItemSortOption.valueOf(prefs.getString(KEY_PAYMENT_SOURCE_ASSIGNED_SORT, AssignedItemSortOption.DEFAULT.name) ?: AssignedItemSortOption.DEFAULT.name)
    } catch (_: Exception) {
        AssignedItemSortOption.DEFAULT
    }
        set(value) {
            field = value
            prefs.edit().putString(KEY_PAYMENT_SOURCE_ASSIGNED_SORT, value.name).apply()
        }

    var paymentSourceSearchQuery: String = prefs.getString(KEY_PAYMENT_SOURCE_SEARCH, "") ?: ""
        set(value) {
            field = value
            prefs.edit().putString(KEY_PAYMENT_SOURCE_SEARCH, value).apply()
        }

    // ==========================================
    // 14. BALANCE SHEET
    // ==========================================
    var balanceSheetSearchQuery: String = prefs.getString(KEY_BS_SEARCH, "") ?: ""
        set(value) {
            field = value
            prefs.edit().putString(KEY_BS_SEARCH, value).apply()
        }

    var balanceSheetActiveTab: String = prefs.getString(KEY_BS_ACTIVE_TAB, "ASSETS") ?: "ASSETS"
        set(value) {
            field = value
            prefs.edit().putString(KEY_BS_ACTIVE_TAB, value).apply()
        }

    var balanceSheetFilterState: BalanceSheetFilterState = loadBalanceSheetFilter()
        set(value) {
            field = value
            saveBalanceSheetFilter(value)
        }

    var balanceSheetBaseDateMs: Long? = if (prefs.contains(KEY_BS_BASE_DATE)) prefs.getLong(KEY_BS_BASE_DATE, 0L) else null
        set(value) {
            field = value
            if (value != null) prefs.edit().putLong(KEY_BS_BASE_DATE, value).apply()
            else prefs.edit().remove(KEY_BS_BASE_DATE).apply()
        }

    var balanceSheetCompareDateMs: Long? = if (prefs.contains(KEY_BS_COMPARE_DATE)) prefs.getLong(KEY_BS_COMPARE_DATE, 0L) else null
        set(value) {
            field = value
            if (value != null) prefs.edit().putLong(KEY_BS_COMPARE_DATE, value).apply()
            else prefs.edit().remove(KEY_BS_COMPARE_DATE).apply()
        }

    // ==========================================
    // 15. BUDGET TRACKING
    // ==========================================
    var budgetTrackingSearchQuery: String = prefs.getString(KEY_BUDGET_TRACKING_SEARCH, "") ?: ""
        set(value) {
            field = value
            prefs.edit().putString(KEY_BUDGET_TRACKING_SEARCH, value).apply()
        }

    var budgetTrackingActiveTab: String = prefs.getString(KEY_BUDGET_TRACKING_TAB, "EXPENSE") ?: "EXPENSE"
        set(value) {
            field = value
            prefs.edit().putString(KEY_BUDGET_TRACKING_TAB, value).apply()
        }

    var budgetTrackingFilterState: BudgetFilterState = loadBudgetTrackingFilter()
        set(value) {
            field = value
            saveBudgetTrackingFilter(value)
        }

    // ==========================================
    // Serialization Helpers
    // ==========================================
    private fun loadBudgetFilterOptions(key: String): Set<BudgetFilterOption> {
        val raw = prefs.getString(key, null) ?: return emptySet()
        return try {
            val jsonArr = JSONArray(raw)
            val result = mutableSetOf<BudgetFilterOption>()
            for (i in 0 until jsonArr.length()) {
                result.add(BudgetFilterOption.valueOf(jsonArr.getString(i)))
            }
            result
        } catch (_: Exception) {
            emptySet()
        }
    }

    private fun saveBudgetFilterOptions(key: String, options: Set<BudgetFilterOption>) {
        val jsonArr = JSONArray()
        options.forEach { jsonArr.put(it.name) }
        prefs.edit().putString(key, jsonArr.toString()).apply()
    }

    private fun loadBalanceSheetFilter(): BalanceSheetFilterState {
        val raw = prefs.getString(KEY_BS_FILTER_STATE, null) ?: return BalanceSheetFilterState()
        return try {
            val obj = JSONObject(raw)
            val preset = try { BalanceSheetComparisonPreset.valueOf(obj.optString("preset", BalanceSheetComparisonPreset.THIS_MONTH.name)) } catch (_: Exception) { BalanceSheetComparisonPreset.THIS_MONTH }
            val customBase = if (obj.has("baseDate")) obj.getLong("baseDate") else null
            val customCompare = if (obj.has("compareDate")) obj.getLong("compareDate") else null
            val accIds = mutableSetOf<Long>()
            obj.optJSONArray("accountIds")?.let { arr ->
                for (i in 0 until arr.length()) accIds.add(arr.getLong(i))
            }
            val statusSet = mutableSetOf<TransactionStatus>()
            obj.optJSONArray("statuses")?.let { arr ->
                for (i in 0 until arr.length()) {
                    try { statusSet.add(TransactionStatus.valueOf(arr.getString(i))) } catch (_: Exception) {}
                }
            }
            val excludeZero = obj.optBoolean("excludeZero", true)
            val filterNonZero = obj.optBoolean("filterNonZero", false)
            val displayCurr = obj.optBoolean("displayCurr", true)
            val displayCurrSym = obj.optBoolean("displayCurrSym", true)
            val sortOrder = try { BalanceSheetSortOrder.valueOf(obj.optString("sortOrder", BalanceSheetSortOrder.AMOUNT_DESC.name)) } catch (_: Exception) { BalanceSheetSortOrder.AMOUNT_DESC }
            val showHidden = obj.optBoolean("showHidden", false)
            val onlyCurrent = obj.optBoolean("onlyCurrent", false)
            val onlyWithoutGroups = obj.optBoolean("onlyWithoutGroups", false)

            BalanceSheetFilterState(
                preset = preset,
                customBaseDateMs = customBase,
                customCompareDateMs = customCompare,
                selectedAccountIds = accIds,
                selectedStatusSet = statusSet,
                excludeZeroAmounts = excludeZero,
                filterNonZeroGroups = filterNonZero,
                displayCurrency = displayCurr,
                displayCurrencySymbol = displayCurrSym,
                sortOrder = sortOrder,
                showHiddenAccounts = showHidden,
                showOnlyCurrentBalance = onlyCurrent,
                showOnlyAccountsWithoutGroups = onlyWithoutGroups
            )
        } catch (_: Exception) {
            BalanceSheetFilterState()
        }
    }

    private fun saveBalanceSheetFilter(state: BalanceSheetFilterState) {
        try {
            val obj = JSONObject()
            obj.put("preset", state.preset.name)
            state.customBaseDateMs?.let { obj.put("baseDate", it) }
            state.customCompareDateMs?.let { obj.put("compareDate", it) }
            val accArr = JSONArray()
            state.selectedAccountIds.forEach { accArr.put(it) }
            obj.put("accountIds", accArr)
            val statusArr = JSONArray()
            state.selectedStatusSet.forEach { statusArr.put(it.name) }
            obj.put("statuses", statusArr)
            obj.put("excludeZero", state.excludeZeroAmounts)
            obj.put("filterNonZero", state.filterNonZeroGroups)
            obj.put("displayCurr", state.displayCurrency)
            obj.put("displayCurrSym", state.displayCurrencySymbol)
            obj.put("sortOrder", state.sortOrder.name)
            obj.put("showHidden", state.showHiddenAccounts)
            obj.put("onlyCurrent", state.showOnlyCurrentBalance)
            obj.put("onlyWithoutGroups", state.showOnlyAccountsWithoutGroups)
            prefs.edit().putString(KEY_BS_FILTER_STATE, obj.toString()).apply()
        } catch (_: Exception) {}
    }

    private fun loadBudgetTrackingFilter(): BudgetFilterState {
        val raw = prefs.getString(KEY_BUDGET_TRACKING_FILTER, null) ?: return BudgetFilterState()
        return try {
            val obj = JSONObject(raw)
            val datePreset = try { BudgetDateRangePreset.valueOf(obj.optString("datePreset", BudgetDateRangePreset.THIS_MONTH.name)) } catch (_: Exception) { BudgetDateRangePreset.THIS_MONTH }
            val customStart = if (obj.has("customStart")) obj.getLong("customStart") else null
            val customEnd = if (obj.has("customEnd")) obj.getLong("customEnd") else null
            val compEnabled = obj.optBoolean("compEnabled", false)
            val compPreset = try { BudgetComparisonPreset.valueOf(obj.optString("compPreset", BudgetComparisonPreset.LAST_MONTH.name)) } catch (_: Exception) { BudgetComparisonPreset.LAST_MONTH }
            val customCompStart = if (obj.has("customCompStart")) obj.getLong("customCompStart") else null
            val customCompEnd = if (obj.has("customCompEnd")) obj.getLong("customCompEnd") else null

            val catIds = mutableSetOf<Long>()
            obj.optJSONArray("catIds")?.let { arr -> for (i in 0 until arr.length()) catIds.add(arr.getLong(i)) }
            val accIds = mutableSetOf<Long>()
            obj.optJSONArray("accIds")?.let { arr -> for (i in 0 until arr.length()) accIds.add(arr.getLong(i)) }
            val labels = mutableSetOf<String>()
            obj.optJSONArray("labels")?.let { arr -> for (i in 0 until arr.length()) labels.add(arr.getString(i)) }
            val statusSet = mutableSetOf<TransactionStatus>()
            obj.optJSONArray("statuses")?.let { arr ->
                for (i in 0 until arr.length()) {
                    try { statusSet.add(TransactionStatus.valueOf(arr.getString(i))) } catch (_: Exception) {}
                }
            }

            val excludeZero = obj.optBoolean("excludeZero", true)
            val hideEmpty = obj.optBoolean("hideEmpty", true)
            val withoutGroups = obj.optBoolean("withoutGroups", false)
            val displayCurr = obj.optBoolean("displayCurr", true)
            val displayCurrSym = obj.optBoolean("displayCurrSym", true)
            val sortByAmt = obj.optBoolean("sortByAmt", true)
            val expenseFirst = obj.optBoolean("expenseFirst", true)
            val sortOrder = try { BudgetSortOrder.valueOf(obj.optString("sortOrder", BudgetSortOrder.AMOUNT_DESC.name)) } catch (_: Exception) { BudgetSortOrder.AMOUNT_DESC }
            val onlyRemaining = obj.optBoolean("onlyRemaining", false)
            val onlyActual = obj.optBoolean("onlyActual", false)
            val onlyBudgeted = obj.optBoolean("onlyBudgeted", false)
            val onlyOverBudget = obj.optBoolean("onlyOverBudget", false)
            val minAmt = if (obj.has("minAmt")) obj.getDouble("minAmt") else null
            val maxAmt = if (obj.has("maxAmt")) obj.getDouble("maxAmt") else null

            BudgetFilterState(
                datePreset = datePreset,
                customStartDateMs = customStart,
                customEndDateMs = customEnd,
                comparisonEnabled = compEnabled,
                comparisonPreset = compPreset,
                customCompareStartMs = customCompStart,
                customCompareEndMs = customCompEnd,
                selectedCategoryIds = catIds,
                selectedAccountIds = accIds,
                selectedLabels = labels,
                selectedStatusSet = statusSet,
                excludeZeroAmounts = excludeZero,
                hideEmptyGroups = hideEmpty,
                showOnlyCategoriesWithoutGroups = withoutGroups,
                displayCurrency = displayCurr,
                displayCurrencySymbol = displayCurrSym,
                sortByAmount = sortByAmt,
                showExpenseCategoriesFirst = expenseFirst,
                sortOrder = sortOrder,
                showOnlyRemainingBalance = onlyRemaining,
                showOnlyActual = onlyActual,
                filterOnlyBudgeted = onlyBudgeted,
                filterOnlyOverBudget = onlyOverBudget,
                minAmount = minAmt,
                maxAmount = maxAmt
            )
        } catch (_: Exception) {
            BudgetFilterState()
        }
    }

    private fun saveBudgetTrackingFilter(state: BudgetFilterState) {
        try {
            val obj = JSONObject()
            obj.put("datePreset", state.datePreset.name)
            state.customStartDateMs?.let { obj.put("customStart", it) }
            state.customEndDateMs?.let { obj.put("customEnd", it) }
            obj.put("compEnabled", state.comparisonEnabled)
            obj.put("compPreset", state.comparisonPreset.name)
            state.customCompareStartMs?.let { obj.put("customCompStart", it) }
            state.customCompareEndMs?.let { obj.put("customCompEnd", it) }

            val catArr = JSONArray()
            state.selectedCategoryIds.forEach { catArr.put(it) }
            obj.put("catIds", catArr)

            val accArr = JSONArray()
            state.selectedAccountIds.forEach { accArr.put(it) }
            obj.put("accIds", accArr)

            val labelArr = JSONArray()
            state.selectedLabels.forEach { labelArr.put(it) }
            obj.put("labels", labelArr)

            val statusArr = JSONArray()
            state.selectedStatusSet.forEach { statusArr.put(it.name) }
            obj.put("statuses", statusArr)

            obj.put("excludeZero", state.excludeZeroAmounts)
            obj.put("hideEmpty", state.hideEmptyGroups)
            obj.put("withoutGroups", state.showOnlyCategoriesWithoutGroups)
            obj.put("displayCurr", state.displayCurrency)
            obj.put("displayCurrSym", state.displayCurrencySymbol)
            obj.put("sortByAmt", state.sortByAmount)
            obj.put("expenseFirst", state.showExpenseCategoriesFirst)
            obj.put("sortOrder", state.sortOrder.name)
            obj.put("onlyRemaining", state.showOnlyRemainingBalance)
            obj.put("onlyActual", state.showOnlyActual)
            obj.put("onlyBudgeted", state.filterOnlyBudgeted)
            obj.put("onlyOverBudget", state.filterOnlyOverBudget)
            state.minAmount?.let { obj.put("minAmt", it) }
            state.maxAmount?.let { obj.put("maxAmt", it) }

            prefs.edit().putString(KEY_BUDGET_TRACKING_FILTER, obj.toString()).apply()
        } catch (_: Exception) {}
    }

    companion object {
        private const val KEY_LEDGER_SEARCH = "ledger_search"
        private const val KEY_LEDGER_TYPE = "ledger_type"
        private const val KEY_LEDGER_DATE_PRESET = "ledger_date_preset"
        private const val KEY_LEDGER_CUSTOM_START = "ledger_custom_start"
        private const val KEY_LEDGER_CUSTOM_END = "ledger_custom_end"
        private const val KEY_LEDGER_MIN_AMOUNT = "ledger_min_amount"
        private const val KEY_LEDGER_MAX_AMOUNT = "ledger_max_amount"
        private const val KEY_LEDGER_CAT_ID = "ledger_cat_id"
        private const val KEY_LEDGER_ACC_ID = "ledger_acc_id"
        private const val KEY_LEDGER_LABEL = "ledger_label"
        private const val KEY_LEDGER_STATUS = "ledger_status"
        private const val KEY_LEDGER_ROW_STYLE = "ledger_row_style"
        private const val KEY_LEDGER_SHOW_TOTAL_AMOUNT = "ledger_show_total_amount"
        private const val KEY_LEDGER_SHOW_TRANSFERS_TOTAL = "ledger_show_transfers_total"
        private const val KEY_LEDGER_SHOW_ALL_NEW_ACCOUNT = "ledger_show_all_new_account"
        private const val KEY_LEDGER_SHOW_ACCOUNT_BALANCE = "ledger_show_account_balance"
        private const val KEY_LEDGER_SHOW_OLDEST_FIRST = "ledger_show_oldest_first"

        private const val KEY_ACCOUNTS_TYPE = "accounts_type"
        private const val KEY_ACCOUNTS_HIERARCHY = "accounts_hierarchy"
        private const val KEY_ACCOUNTS_SORT = "accounts_sort"
        private const val KEY_ACCOUNTS_STATUS = "accounts_status"
        private const val KEY_ACCOUNTS_EXCLUDE_ZERO = "accounts_exclude_zero"
        private const val KEY_ACCOUNTS_SEARCH = "accounts_search"

        private const val KEY_CATEGORIES_TYPE = "categories_type"
        private const val KEY_CATEGORIES_HIERARCHY = "categories_hierarchy"
        private const val KEY_CATEGORIES_SORT = "categories_sort"
        private const val KEY_CATEGORIES_SEARCH = "categories_search"

        private const val KEY_ITEMS_TAB_MODE = "items_tab_mode"
        private const val KEY_ITEMS_SEARCH = "items_search"
        private const val KEY_ITEMS_DATE_PRESET = "items_date_preset"
        private const val KEY_ITEMS_SORT_ORDER = "items_sort_order"

        private const val KEY_LABELS_TAB_MODE = "labels_tab_mode"
        private const val KEY_LABELS_SEARCH = "labels_search"
        private const val KEY_LABELS_DATE_PRESET = "labels_date_preset"
        private const val KEY_LABELS_SORT_ORDER = "labels_sort_order"

        private const val KEY_GOALS_FILTER = "goals_filter"
        private const val KEY_GOALS_SEARCH = "goals_search"

        private const val KEY_WISHLIST_TAB = "wishlist_tab"
        private const val KEY_WISHLIST_SORT = "wishlist_sort"
        private const val KEY_WISHLIST_SEARCH = "wishlist_search"

        private const val KEY_REPORTS_TAB_MODE = "reports_tab_mode"
        private const val KEY_REPORTS_HIERARCHY = "reports_hierarchy"
        private const val KEY_REPORTS_SORT = "reports_sort"
        private const val KEY_REPORTS_DATE_PRESET = "reports_date_preset"
        private const val KEY_REPORTS_SEARCH = "reports_search"

        private const val KEY_TRASH_FILTER = "trash_filter"

        private const val KEY_CASHFLOW_PRESET = "cashflow_preset"
        private const val KEY_CASHFLOW_SECTION = "cashflow_section"
        private const val KEY_CASHFLOW_SEARCH = "cashflow_search"

        private const val KEY_BUDGET_SEARCH = "budget_search"
        private const val KEY_BUDGET_EXPENSE_SORT = "budget_expense_sort"
        private const val KEY_BUDGET_INCOME_SORT = "budget_income_sort"
        private const val KEY_BUDGET_ASSET_SORT = "budget_asset_sort"
        private const val KEY_BUDGET_LIABILITY_SORT = "budget_liability_sort"
        private const val KEY_BUDGET_MAKER_TAB = "budget_maker_tab"
        private const val KEY_BUDGET_EXPENSE_FILTERS = "budget_expense_filters"
        private const val KEY_BUDGET_INCOME_FILTERS = "budget_income_filters"
        private const val KEY_BUDGET_ASSET_FILTERS = "budget_asset_filters"
        private const val KEY_BUDGET_LIABILITY_FILTERS = "budget_liability_filters"

        private const val KEY_RM_SEARCH = "rm_search"
        private const val KEY_RM_CATEGORY = "rm_category"
        private const val KEY_RM_SORT = "rm_sort"
        private const val KEY_RM_DETAIL_MODE = "rm_detail_mode"
        private const val KEY_RM_TIMELINE_FILTER = "rm_timeline_filter"

        private const val KEY_PAYMENT_SOURCE_TAB = "payment_source_tab"
        private const val KEY_PAYMENT_SOURCE_CALC_BASIS = "payment_source_calc_basis"
        private const val KEY_PAYMENT_SOURCE_ACCOUNT_STATUS = "payment_source_account_status"
        private const val KEY_PAYMENT_SOURCE_SORT = "payment_source_sort"
        private const val KEY_PAYMENT_SOURCE_ASSIGNED_SECTION = "payment_source_assigned_section"
        private const val KEY_PAYMENT_SOURCE_ASSIGNED_STATUS = "payment_source_assigned_status"
        private const val KEY_PAYMENT_SOURCE_ASSIGNED_SORT = "payment_source_assigned_sort"
        private const val KEY_PAYMENT_SOURCE_SEARCH = "payment_source_search"

        private const val KEY_BS_SEARCH = "bs_search"
        private const val KEY_BS_ACTIVE_TAB = "bs_active_tab"
        private const val KEY_BS_FILTER_STATE = "bs_filter_state"
        private const val KEY_BS_BASE_DATE = "bs_base_date"
        private const val KEY_BS_COMPARE_DATE = "bs_compare_date"

        private const val KEY_BUDGET_TRACKING_SEARCH = "budget_tracking_search"
        private const val KEY_BUDGET_TRACKING_TAB = "budget_tracking_tab"
        private const val KEY_BUDGET_TRACKING_FILTER = "budget_tracking_filter"

        @Volatile
        private var instance: TabFilterPreferences? = null

        fun getInstance(context: Context): TabFilterPreferences {
            return instance ?: synchronized(this) {
                instance ?: TabFilterPreferences(context.applicationContext).also { instance = it }
            }
        }
    }
}
