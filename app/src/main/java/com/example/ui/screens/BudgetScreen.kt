package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import kotlinx.coroutines.delay
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.VerticalAlignTop
import androidx.compose.material.icons.filled.VerticalAlignBottom
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.window.Dialog
import com.example.data.model.Account
import com.example.data.model.AccountType
import com.example.data.model.Category
import com.example.data.model.CategoryType
import com.example.data.model.LanguageMode
import com.example.data.model.MonthlyBudget
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.data.model.TransactionWithDetails
import com.example.data.repository.AccountWithBalance
import com.example.ui.components.AppTabHeader
import com.example.ui.components.BudgetSortOrder
import com.example.ui.components.PopupCalculatorDialog
import com.example.ui.dialogs.ActiveBudgetMakerFilterBar
import com.example.ui.dialogs.AmountBreakdownDialog
import com.example.ui.dialogs.AmountDetailInfo
import com.example.ui.dialogs.BreakdownItem
import com.example.ui.dialogs.BreakdownTabInfo
import com.example.ui.dialogs.BudgetMakerDashboardFilter
import com.example.ui.dialogs.BudgetMakerFilterDialog
import com.example.ui.dialogs.BudgetMakerTabFilter
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidPrimary
import com.example.ui.viewmodel.BudgetViewModel
import com.example.util.AccountCalcConfig
import com.example.util.DateUtils
import com.example.util.IconHelper
import com.example.util.LanguageHelper
import com.example.util.TabPosition
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.math.roundToInt

private val AmberGold = Color(0xFFD97706)
private val BrandBlue = Color(0xFF1976D2)
private val BrandOrange = Color(0xFFF4511E)
private val DarkEmerald = Color(0xFF0F766E)

/**
 * Frequency options for budgeting.
 * Stored database values are always normalized in terms of monthly amounts.
 */
enum class BudgetFrequency(val labelKey: String, val defaultLabel: String, val monthsFactor: Double) {
    WEEKLY("frequency_weekly", "Weekly", 52.0 / 12.0),
    BI_WEEKLY("frequency_bi_weekly", "Bi-weekly", 26.0 / 12.0),
    MONTHLY("frequency_monthly", "Monthly", 1.0),
    QUARTERLY("frequency_quarterly", "Quarterly", 1.0 / 3.0),
    YEARLY("frequency_yearly", "Yearly", 1.0 / 12.0);

    fun toMonthly(amountInThisFreq: Double): Double = amountInThisFreq * monthsFactor
    fun fromMonthly(monthlyAmount: Double): Double = if (monthsFactor > 0.0) monthlyAmount / monthsFactor else monthlyAmount

    fun localizedName(languageMode: LanguageMode): String =
        LanguageHelper.getString(labelKey, languageMode).ifEmpty { defaultLabel }
}

/**
 * Type-specific filter options for Budget Maker.
 */
/**
 * Filter groups to organize budget filters by similarities.
 */
enum class BudgetFilterGroup(val titleEn: String, val titleBn: String) {
    GENERAL("General", "সাধারণ"),
    BUDGET_STATUS("Budget Status", "বাজেট স্থিতি"),
    ACTIVITY("Activity", "কার্যক্রম"),
    VIEW_HIERARCHY("Structure", "গঠন");

    fun getTitle(languageMode: LanguageMode): String =
        if (languageMode == LanguageMode.BANGLA) titleBn else titleEn
}

/**
 * Filter options for Budget Maker categorized by logical similarity.
 */
enum class BudgetFilterOption(val group: BudgetFilterGroup) {
    ALL(BudgetFilterGroup.GENERAL),
    ONLY_REMAINING(BudgetFilterGroup.GENERAL),
    BUDGETED_ONLY(BudgetFilterGroup.BUDGET_STATUS),
    UNBUDGETED(BudgetFilterGroup.BUDGET_STATUS),
    ACTIVE_ONLY(BudgetFilterGroup.ACTIVITY),
    ACTIVE_3_MONTHS(BudgetFilterGroup.ACTIVITY),
    FREQ_ACTIVE(BudgetFilterGroup.ACTIVITY),
    FREQ_BUDGETED(BudgetFilterGroup.ACTIVITY),
    ONLY_GROUPS(BudgetFilterGroup.VIEW_HIERARCHY),
    ONLY_CATEGORIES(BudgetFilterGroup.VIEW_HIERARCHY);

    fun getTitle(itemType: String, languageMode: LanguageMode): String {
        val isBn = languageMode == LanguageMode.BANGLA
        val isAccount = itemType == "ASSET" || itemType == "LIABILITY"
        return when (this) {
            ALL -> if (isBn) "সব" else "All"
            ONLY_REMAINING -> if (isBn) "শুধু অবশিষ্ট" else "Only Remaining"
            BUDGETED_ONLY -> when (itemType) {
                "ASSET", "LIABILITY" -> if (isBn) "শুধু লক্ষ্য" else "Targeted Only"
                else -> if (isBn) "শুধু বাজেট" else "Budgeted Only"
            }
            UNBUDGETED -> when (itemType) {
                "ASSET", "LIABILITY" -> if (isBn) "লক্ষ্য ছাড়া" else "No Target"
                else -> if (isBn) "বাজেট ছাড়া" else "Unbudgeted"
            }
            ACTIVE_ONLY -> when (itemType) {
                "EXPENSE" -> if (isBn) "শুধু খরচ হওয়া" else "Expensed Only"
                "INCOME" -> if (isBn) "শুধু অর্জিত আয়" else "Earned Only"
                "ASSET" -> if (isBn) "ব্যালেন্স আছে" else "Has Balance"
                "LIABILITY" -> if (isBn) "দেনা বা ঋণ আছে" else "Has Debt"
                else -> if (isBn) "লেনদেন আছে" else "Active Only"
            }
            ACTIVE_3_MONTHS -> if (isBn) "সক্রিয় (৩ মাস)" else "Active (3 Months)"
            FREQ_ACTIVE -> when (itemType) {
                "EXPENSE" -> if (isBn) "নিয়মিত খরচ" else "Frequently Expensed"
                "INCOME" -> if (isBn) "নিয়মিত প্রাপ্ত আয়" else "Frequently Received"
                "ASSET" -> if (isBn) "নিয়মিত লেনদেন" else "Frequently Active"
                "LIABILITY" -> if (isBn) "নিয়মিত দেনা/লেনদেন" else "Frequently Active"
                else -> if (isBn) "নিয়মিত ব্যবহৃত" else "Frequently Active"
            }
            FREQ_BUDGETED -> when (itemType) {
                "ASSET", "LIABILITY" -> if (isBn) "নিয়মিত লক্ষ্য" else "Frequently Targeted"
                else -> if (isBn) "নিয়মিত বাজেট" else "Frequently Budgeted"
            }
            ONLY_GROUPS -> if (isBn) "শুধু গ্রুপ" else "Only Groups"
            ONLY_CATEGORIES -> if (isAccount) (if (isBn) "শুধু একাউন্ট" else "Only Accounts") else (if (isBn) "শুধু ক্যাটাগরি" else "Only Categories")
        }
    }
}

/**
 * Type-specific sort options for Budget Maker.
 */
enum class BudgetSortOption {
    DEFAULT,
    BUDGET_DESC,
    BUDGET_ASC,
    ACTUAL_DESC,
    FREQUENCY_DESC,
    NAME_ASC;

    fun getTitle(itemType: String, languageMode: LanguageMode): String {
        val isBn = languageMode == LanguageMode.BANGLA
        return when (this) {
            DEFAULT -> if (isBn) "ডিফল্ট ক্রম" else "Default Order"
            BUDGET_DESC -> when (itemType) {
                "ASSET", "LIABILITY" -> if (isBn) "লক্ষ্য: বেশি → কম" else "Target: High → Low"
                else -> if (isBn) "বাজেট: বেশি → কম" else "Budget: High → Low"
            }
            BUDGET_ASC -> when (itemType) {
                "ASSET", "LIABILITY" -> if (isBn) "লক্ষ্য: কম → বেশি" else "Target: Low → High"
                else -> if (isBn) "বাজেট: কম → বেশি" else "Budget: Low → High"
            }
            ACTUAL_DESC -> when (itemType) {
                "EXPENSE" -> if (isBn) "খরচ: বেশি → কম" else "Spent: High → Low"
                "INCOME" -> if (isBn) "আয়: বেশি → কম" else "Earned: High → Low"
                "ASSET" -> if (isBn) "ব্যালেন্স: বেশি → কম" else "Balance: High → Low"
                "LIABILITY" -> if (isBn) "দেনা: বেশি → কম" else "Debt: High → Low"
                else -> if (isBn) "প্রকৃত: বেশি → কম" else "Actual: High → Low"
            }
            FREQUENCY_DESC -> if (isBn) "লেনদেন: সর্বোচ্চ" else "Most Active"
            NAME_ASC -> if (isBn) "নাম: A → Z" else "Alphabetical: A → Z"
        }
    }
}

data class BudgetTargetItem(
    val id: Long,
    val nameEn: String,
    val nameBn: String,
    val groupName: String,
    val iconName: String,
    val colorHex: String,
    val itemType: String, // "EXPENSE", "INCOME", "ASSET", "LIABILITY"
    val defaultLimit: Double = 0.0,
    val groupIconName: String = "Category",
    val groupColorHex: String = "#6B7280"
)

data class BudgetSuggestionOption(
    val index: Int,
    val amountMonthly: Double,
    val pretext: String,
    val label: String = ""
)

data class EnhancedBudgetItem(
    val item: BudgetTargetItem,
    val currentBudget: Double,
    val isEnabled: Boolean,
    val actualSpent: Double,
    val manualBaseline: Double,
    val txCount: Int,
    val isFrequentlyBudgeted: Boolean,
    val isFrequentlyActive: Boolean,
    val is3MonthsActive: Boolean = false,
    val firstDayBalance: Double = 0.0,
    val suggestions: List<BudgetSuggestionOption>,
    val savedBudget: MonthlyBudget?
)

private data class BudgetAccountItemHolder(
    val accountWithBal: AccountWithBalance,
    val displayName: String,
    val balance: Double,
    val effectiveBalance: Double,
    val note: String?,
    val iconName: String?,
    val isSubAccount: Boolean,
    val type: AccountType
)

@Composable
fun BudgetScreen(
    viewModel: BudgetViewModel,
    allCategories: List<Category>,
    allAccounts: List<Account>,
    accountsWithBalances: List<AccountWithBalance>,
    transactionsWithDetails: List<TransactionWithDetails>,
    monthlyBudgets: List<MonthlyBudget>,
    selectedYear: Int,
    selectedMonth: Int,
    languageMode: LanguageMode,
    initialTab: Int = 0, // Default to BM Dashboard
    initialSearchQuery: String = "",
    onOpenDrawer: () -> Unit = {},
    onBack: (() -> Unit)? = null,
    onEditTransaction: (Transaction) -> Unit,
    onAddTransactionWithCategory: (Category) -> Unit,
    onAddTransactionWithAccount: (Account) -> Unit,
    onAccountClick: ((Account) -> Unit)? = null
) {
    // Tabs: 0 -> BM Dashboard, 1 -> Expenses, 2 -> Incomes, 3 -> Assets, 4 -> Liabilities
    var selectedTab by remember(initialTab) { mutableIntStateOf(initialTab) }
    val budgetMakerTabPosition by viewModel.budgetMakerTabPosition.collectAsStateWithLifecycle()
    val tabsAtTop = budgetMakerTabPosition == TabPosition.TOP
    var showTabSettingsMenu by remember { mutableStateOf(false) }
    var showCopyPreviousConfirmDialog by remember { mutableStateOf(false) }
    var showCopyPasteDialog by remember { mutableStateOf(false) }

    var globalExpenseFrequency by remember { mutableStateOf(BudgetFrequency.MONTHLY) }
    var globalIncomeFrequency by remember { mutableStateOf(BudgetFrequency.MONTHLY) }
    var showMonthYearPicker by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showQuickActionSheet by remember { mutableStateOf(false) }

    // Search and Filter State
    var isSearchActive by remember(initialSearchQuery) { mutableStateOf(initialSearchQuery.isNotBlank()) }
    var searchQuery by remember(initialSearchQuery) { mutableStateOf(initialSearchQuery) }
    val searchFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(isSearchActive) {
        if (isSearchActive) {
            delay(100)
            try {
                searchFocusRequester.requestFocus()
                keyboardController?.show()
            } catch (_: Exception) {}
        }
    }
    var showFilterDialog by remember { mutableStateOf(false) }

    // Tab-specific filters and sorting for Budget Maker (Independent per tab)
    var expenseFilter by remember { mutableStateOf(BudgetMakerTabFilter()) }
    var incomeFilter by remember { mutableStateOf(BudgetMakerTabFilter()) }
    var assetFilter by remember { mutableStateOf(BudgetMakerTabFilter()) }
    var liabilityFilter by remember { mutableStateOf(BudgetMakerTabFilter()) }
    var dashboardFilter by remember { mutableStateOf(BudgetMakerDashboardFilter()) }

    var expenseSelectedFilters by remember { mutableStateOf<Set<BudgetFilterOption>>(emptySet()) }
    var incomeSelectedFilters by remember { mutableStateOf<Set<BudgetFilterOption>>(emptySet()) }
    var assetSelectedFilters by remember { mutableStateOf<Set<BudgetFilterOption>>(emptySet()) }
    var liabilitySelectedFilters by remember { mutableStateOf<Set<BudgetFilterOption>>(emptySet()) }

    var expenseSelectedSort by remember { mutableStateOf(BudgetSortOption.DEFAULT) }
    var incomeSelectedSort by remember { mutableStateOf(BudgetSortOption.DEFAULT) }
    var assetSelectedSort by remember { mutableStateOf(BudgetSortOption.DEFAULT) }
    var liabilitySelectedSort by remember { mutableStateOf(BudgetSortOption.DEFAULT) }

    val currentTabFilter = when (selectedTab) {
        1 -> expenseFilter
        2 -> incomeFilter
        3 -> assetFilter
        4 -> liabilityFilter
        else -> BudgetMakerTabFilter()
    }
    val currentTabQuickFilterCount = when (selectedTab) {
        1 -> expenseSelectedFilters.count { it != BudgetFilterOption.ALL }
        2 -> incomeSelectedFilters.count { it != BudgetFilterOption.ALL }
        3 -> assetSelectedFilters.count { it != BudgetFilterOption.ALL }
        4 -> liabilitySelectedFilters.count { it != BudgetFilterOption.ALL }
        else -> 0
    }
    val currentTabActiveCount = if (selectedTab == 0) dashboardFilter.activeCount else (currentTabFilter.activeCount + currentTabQuickFilterCount)
    val isCurrentTabFilterActive = if (selectedTab == 0) dashboardFilter.isActive else (currentTabFilter.isActive || currentTabQuickFilterCount > 0)

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Pager state for swipeable tab navigation
    val pagerState = rememberPagerState(initialPage = selectedTab, pageCount = { 5 })
    androidx.compose.runtime.LaunchedEffect(selectedTab) {
        if (pagerState.currentPage != selectedTab) {
            pagerState.animateScrollToPage(selectedTab)
        }
    }
    androidx.compose.runtime.LaunchedEffect(pagerState.currentPage) {
        if (selectedTab != pagerState.currentPage) {
            selectedTab = pagerState.currentPage
        }
    }

    // Calculate month boundary timestamps
    val startOfMonthMs = remember(selectedYear, selectedMonth) {
        DateUtils.getStartOfMonth(selectedYear, selectedMonth)
    }
    val endOfMonthMs = remember(selectedYear, selectedMonth) {
        DateUtils.getEndOfMonth(selectedYear, selectedMonth)
    }

    // Filter transactions for selected month
    val monthTransactions = remember(transactionsWithDetails, startOfMonthMs, endOfMonthMs) {
        transactionsWithDetails.filter { it.transaction.dateEpochMs in startOfMonthMs..endOfMonthMs }
    }

    // Map monthly budget items: "itemType_itemId" -> MonthlyBudget
    val budgetMap = remember(monthlyBudgets) {
        monthlyBudgets.associateBy { "${it.itemType}_${it.itemId}" }
    }

    // Prepare categorized items
    val parentCatMap = remember(allCategories) {
        allCategories.filter { it.parentId == null }.associateBy { it.id }
    }
    val parentAccMap = remember(allAccounts) {
        allAccounts.filter { it.parentId == null }.associateBy { it.id }
    }

    val parentExpenseCatIdsWithChildren = remember(allCategories) {
        allCategories.filter { it.type == CategoryType.EXPENSE && it.parentId != null }.mapNotNull { it.parentId }.toSet()
    }
    val parentIncomeCatIdsWithChildren = remember(allCategories) {
        allCategories.filter { it.type == CategoryType.INCOME && it.parentId != null }.mapNotNull { it.parentId }.toSet()
    }
    val parentAssetAccIdsWithChildren = remember(allAccounts) {
        allAccounts.filter { it.type == AccountType.ASSET && it.parentId != null }.mapNotNull { it.parentId }.toSet()
    }
    val parentLiabilityAccIdsWithChildren = remember(allAccounts) {
        allAccounts.filter { it.type == AccountType.LIABILITY && it.parentId != null }.mapNotNull { it.parentId }.toSet()
    }

    // 1. EXPENSES
    val expenseItems = remember(allCategories, parentCatMap, parentExpenseCatIdsWithChildren) {
        allCategories.filter {
            it.type == CategoryType.EXPENSE && it.isActive &&
            (it.parentId != null || !parentExpenseCatIdsWithChildren.contains(it.id))
        }.map { cat ->
            val parent = if (cat.parentId != null) parentCatMap[cat.parentId] else null
            val group = parent?.nameEn ?: "General Expenses"
            val groupIcon = parent?.iconName ?: "Category"
            val groupColor = parent?.colorHex ?: "#EA580C"
            BudgetTargetItem(
                id = cat.id,
                nameEn = cat.nameEn,
                nameBn = cat.nameBn,
                groupName = group,
                iconName = cat.iconName,
                colorHex = cat.colorHex,
                itemType = "EXPENSE",
                defaultLimit = cat.budgetLimit,
                groupIconName = groupIcon,
                groupColorHex = groupColor
            )
        }
    }

    // 2. LIABILITIES
    val liabilityItems = remember(allAccounts, parentAccMap, parentLiabilityAccIdsWithChildren) {
        allAccounts.filter {
            it.type == AccountType.LIABILITY && it.isActive &&
            (it.parentId != null || !parentLiabilityAccIdsWithChildren.contains(it.id))
        }.map { acc ->
            val parent = if (acc.parentId != null) parentAccMap[acc.parentId] else null
            val group = parent?.nameEn ?: "Loans & Liabilities"
            val groupIcon = parent?.iconName ?: "AccountBalance"
            val groupColor = parent?.colorHex ?: "#DC2626"
            BudgetTargetItem(
                id = acc.id,
                nameEn = acc.nameEn,
                nameBn = acc.nameBn,
                groupName = group,
                iconName = acc.iconName,
                colorHex = acc.colorHex,
                itemType = "LIABILITY",
                defaultLimit = 0.0,
                groupIconName = groupIcon,
                groupColorHex = groupColor
            )
        }
    }

    // 3. INCOMES
    val incomeItems = remember(allCategories, parentCatMap, parentIncomeCatIdsWithChildren) {
        allCategories.filter {
            it.type == CategoryType.INCOME && it.isActive &&
            (it.parentId != null || !parentIncomeCatIdsWithChildren.contains(it.id))
        }.map { cat ->
            val parent = if (cat.parentId != null) parentCatMap[cat.parentId] else null
            val group = parent?.nameEn ?: "General Incomes"
            val groupIcon = parent?.iconName ?: "MonetizationOn"
            val groupColor = parent?.colorHex ?: "#16A34A"
            BudgetTargetItem(
                id = cat.id,
                nameEn = cat.nameEn,
                nameBn = cat.nameBn,
                groupName = group,
                iconName = cat.iconName,
                colorHex = cat.colorHex,
                itemType = "INCOME",
                defaultLimit = cat.budgetLimit,
                groupIconName = groupIcon,
                groupColorHex = groupColor
            )
        }
    }

    // 4. ASSETS
    val assetItems = remember(allAccounts, parentAccMap, parentAssetAccIdsWithChildren) {
        allAccounts.filter {
            it.type == AccountType.ASSET && it.isActive &&
            (it.parentId != null || !parentAssetAccIdsWithChildren.contains(it.id))
        }.map { acc ->
            val parent = if (acc.parentId != null) parentAccMap[acc.parentId] else null
            val group = parent?.nameEn ?: "Cash & Accounts"
            val groupIcon = parent?.iconName ?: "AccountBalanceWallet"
            val groupColor = parent?.colorHex ?: "#2563EB"
            BudgetTargetItem(
                id = acc.id,
                nameEn = acc.nameEn,
                nameBn = acc.nameBn,
                groupName = group,
                iconName = acc.iconName,
                colorHex = acc.colorHex,
                itemType = "ASSET",
                defaultLimit = 0.0,
                groupIconName = groupIcon,
                groupColorHex = groupColor
            )
        }
    }

    // Calculate totals for dashboard & subheaders
    fun calculateBudgetTotal(items: List<BudgetTargetItem>): Double {
        return items.sumOf { item ->
            val saved = budgetMap["${item.itemType}_${item.id}"]
            if (saved != null && saved.isEnabled) {
                saved.budgetedAmount
            } else {
                0.0
            }
        }
    }

    val totalExpensesBudget = remember(expenseItems, budgetMap) { calculateBudgetTotal(expenseItems) }
    val totalLiabilitiesBudget = remember(liabilityItems, budgetMap) { calculateBudgetTotal(liabilityItems) }
    val totalOutflowsBudget = totalExpensesBudget + totalLiabilitiesBudget

    val totalIncomesBudget = remember(incomeItems, budgetMap) { calculateBudgetTotal(incomeItems) }
    val totalAssetsBudget = remember(assetItems, budgetMap) { calculateBudgetTotal(assetItems) }
    val totalInflowsBudget = totalIncomesBudget + totalAssetsBudget

    val overview by viewModel.financialOverview.collectAsStateWithLifecycle()
    val runningExpendable = overview.expendable

    // Amount Detail Navigation Stack for interactive breakdowns
    val amountDetailStack = remember { mutableStateListOf<AmountDetailInfo>() }
    fun showAmountDetail(info: AmountDetailInfo) {
        amountDetailStack.add(info)
    }

    // Intercept hardware/gesture back press when an AmountDetailDialog is open
    BackHandler(enabled = amountDetailStack.isNotEmpty()) {
        amountDetailStack.removeLastOrNull()
    }

    val accountCalcConfig by viewModel.accountCalcConfig.collectAsStateWithLifecycle()

    fun computeSubEffective(subItem: AccountWithBalance): Double {
        val subSetting = accountCalcConfig.getSetting(subItem.account.id)
        return if (subSetting.isIncluded) subItem.currentBalance + subSetting.adjustmentAmount else 0.0
    }

    fun computeGroupEffective(groupItem: AccountWithBalance): Double {
        val groupSetting = accountCalcConfig.getSetting(groupItem.account.id)
        if (!groupSetting.isIncluded) return 0.0
        if (groupItem.subAccounts.isEmpty()) {
            return groupItem.currentBalance + groupSetting.adjustmentAmount
        }
        val activeSubs = groupItem.subAccounts.filter { it.account.isActive }
        val sumSubs = activeSubs.sumOf { computeSubEffective(it) }
        return sumSubs + groupSetting.adjustmentAmount
    }

    val activeAccounts = remember(accountsWithBalances) {
        accountsWithBalances.filter { it.account.isActive }
    }
    val inactiveAccounts = remember(accountsWithBalances) {
        accountsWithBalances.filter { !it.account.isActive }
    }

    val calculatedAssets = remember(activeAccounts, accountCalcConfig) {
        activeAccounts.filter { it.account.type == AccountType.ASSET }.sumOf { computeGroupEffective(it) }
    }
    val calculatedLiabilities = remember(activeAccounts, accountCalcConfig) {
        activeAccounts.filter { it.account.type == AccountType.LIABILITY }.sumOf { Math.abs(computeGroupEffective(it)) }
    }

    val calculatedAssetHolders = remember(activeAccounts, accountCalcConfig, languageMode) {
        val list = mutableListOf<BudgetAccountItemHolder>()
        for (group in activeAccounts.filter { it.account.type == AccountType.ASSET }) {
            val groupSetting = accountCalcConfig.getSetting(group.account.id)
            if (group.subAccounts.isEmpty()) {
                if (groupSetting.isIncluded) {
                    val eff = computeGroupEffective(group)
                    val adjNote = if (groupSetting.adjustmentAmount != 0.0) {
                        val sign = if (groupSetting.adjustmentAmount > 0) "+" else ""
                        "Adj: $sign${LanguageHelper.formatCurrency(groupSetting.adjustmentAmount, languageMode)}"
                    } else null
                    list.add(
                        BudgetAccountItemHolder(
                            accountWithBal = group,
                            displayName = group.account.localizedName(languageMode),
                            balance = group.currentBalance,
                            effectiveBalance = eff,
                            note = adjNote,
                            iconName = group.account.iconName,
                            isSubAccount = false,
                            type = AccountType.ASSET
                        )
                    )
                }
            } else {
                if (groupSetting.isIncluded) {
                    val activeSubs = group.subAccounts.filter { it.account.isActive && accountCalcConfig.isIncluded(it.account.id) }
                    if (activeSubs.isNotEmpty()) {
                        val eff = computeGroupEffective(group)
                        val noteText = if (activeSubs.size > 1) {
                            "${activeSubs.size} ${if (languageMode == LanguageMode.BANGLA) "টি সাব-একাউন্ট" else "sub-accounts"}"
                        } else null
                        list.add(
                            BudgetAccountItemHolder(
                                accountWithBal = group,
                                displayName = group.account.localizedName(languageMode),
                                balance = group.currentBalance,
                                effectiveBalance = eff,
                                note = noteText,
                                iconName = group.account.iconName,
                                isSubAccount = false,
                                type = AccountType.ASSET
                            )
                        )
                    }
                }
            }
        }
        list.sortedByDescending { it.effectiveBalance }
    }

    val excludedAssetHolders = remember(activeAccounts, accountCalcConfig, languageMode) {
        val list = mutableListOf<BudgetAccountItemHolder>()
        for (group in activeAccounts.filter { it.account.type == AccountType.ASSET }) {
            val isGroupIncluded = accountCalcConfig.isIncluded(group.account.id)
            val isGroupActive = group.account.isActive
            if (group.subAccounts.isEmpty()) {
                if (isGroupActive && !isGroupIncluded) {
                    list.add(
                        BudgetAccountItemHolder(
                            accountWithBal = group,
                            displayName = group.account.localizedName(languageMode),
                            balance = group.currentBalance,
                            effectiveBalance = group.currentBalance,
                            note = if (languageMode == LanguageMode.BANGLA) "গণনা থেকে বাদ" else "Excluded from calc",
                            iconName = group.account.iconName,
                            isSubAccount = false,
                            type = AccountType.ASSET
                        )
                    )
                }
            } else {
                if (isGroupActive) {
                    val excludedSubs = group.subAccounts.filter { it.account.isActive && (!isGroupIncluded || !accountCalcConfig.isIncluded(it.account.id)) }
                    if (excludedSubs.isNotEmpty()) {
                        val excludedBal = excludedSubs.sumOf { it.currentBalance }
                        val noteText = if (!isGroupIncluded) {
                            if (languageMode == LanguageMode.BANGLA) "সম্পূর্ণ গ্রুপ বাদ দেওয়া" else "Entire group excluded"
                        } else {
                            "${excludedSubs.size} ${if (languageMode == LanguageMode.BANGLA) "টি সাব-একাউন্ট বাদ" else "sub-accounts excluded"}"
                        }
                        list.add(
                            BudgetAccountItemHolder(
                                accountWithBal = group,
                                displayName = group.account.localizedName(languageMode),
                                balance = excludedBal,
                                effectiveBalance = excludedBal,
                                note = noteText,
                                iconName = group.account.iconName,
                                isSubAccount = false,
                                type = AccountType.ASSET
                            )
                        )
                    }
                }
            }
        }
        list.sortedByDescending { it.balance }
    }

    val inactiveAssetHolders = remember(accountsWithBalances, languageMode) {
        val list = mutableListOf<BudgetAccountItemHolder>()
        for (group in accountsWithBalances.filter { it.account.type == AccountType.ASSET }) {
            if (!group.account.isActive) {
                val bal = if (group.subAccounts.isEmpty()) group.currentBalance else group.subAccounts.sumOf { it.currentBalance }
                list.add(
                    BudgetAccountItemHolder(
                        accountWithBal = group,
                        displayName = group.account.localizedName(languageMode),
                        balance = bal,
                        effectiveBalance = bal,
                        note = if (languageMode == LanguageMode.BANGLA) "নিষ্ক্রিয় সম্পদ" else "Archived / Inactive",
                        iconName = group.account.iconName,
                        isSubAccount = false,
                        type = AccountType.ASSET
                    )
                )
            } else if (group.subAccounts.isNotEmpty()) {
                val inactiveSubs = group.subAccounts.filter { !it.account.isActive }
                if (inactiveSubs.isNotEmpty()) {
                    val inactiveBal = inactiveSubs.sumOf { it.currentBalance }
                    list.add(
                        BudgetAccountItemHolder(
                            accountWithBal = group,
                            displayName = group.account.localizedName(languageMode),
                            balance = inactiveBal,
                            effectiveBalance = inactiveBal,
                            note = "${inactiveSubs.size} ${if (languageMode == LanguageMode.BANGLA) "টি নিষ্ক্রিয় সাব-একাউন্ট" else "inactive sub-accounts"}",
                            iconName = group.account.iconName,
                            isSubAccount = false,
                            type = AccountType.ASSET
                        )
                    )
                }
            }
        }
        list.sortedByDescending { it.balance }
    }

    val calculatedLiabHolders = remember(activeAccounts, accountCalcConfig, languageMode) {
        val list = mutableListOf<BudgetAccountItemHolder>()
        for (group in activeAccounts.filter { it.account.type == AccountType.LIABILITY }) {
            val groupSetting = accountCalcConfig.getSetting(group.account.id)
            if (group.subAccounts.isEmpty()) {
                if (groupSetting.isIncluded) {
                    val eff = Math.abs(computeGroupEffective(group))
                    val adjNote = if (groupSetting.adjustmentAmount != 0.0) {
                        val sign = if (groupSetting.adjustmentAmount > 0) "+" else ""
                        "Adj: $sign${LanguageHelper.formatCurrency(groupSetting.adjustmentAmount, languageMode)}"
                    } else null
                    list.add(
                        BudgetAccountItemHolder(
                            accountWithBal = group,
                            displayName = group.account.localizedName(languageMode),
                            balance = Math.abs(group.currentBalance),
                            effectiveBalance = eff,
                            note = adjNote,
                            iconName = group.account.iconName,
                            isSubAccount = false,
                            type = AccountType.LIABILITY
                        )
                    )
                }
            } else {
                if (groupSetting.isIncluded) {
                    val activeSubs = group.subAccounts.filter { it.account.isActive && accountCalcConfig.isIncluded(it.account.id) }
                    if (activeSubs.isNotEmpty()) {
                        val eff = Math.abs(computeGroupEffective(group))
                        val noteText = if (activeSubs.size > 1) {
                            "${activeSubs.size} ${if (languageMode == LanguageMode.BANGLA) "টি সাব-একাউন্ট" else "sub-accounts"}"
                        } else null
                        list.add(
                            BudgetAccountItemHolder(
                                accountWithBal = group,
                                displayName = group.account.localizedName(languageMode),
                                balance = Math.abs(group.currentBalance),
                                effectiveBalance = eff,
                                note = noteText,
                                iconName = group.account.iconName,
                                isSubAccount = false,
                                type = AccountType.LIABILITY
                            )
                        )
                    }
                }
            }
        }
        list.sortedByDescending { it.effectiveBalance }
    }

    val excludedLiabHolders = remember(activeAccounts, accountCalcConfig, languageMode) {
        val list = mutableListOf<BudgetAccountItemHolder>()
        for (group in activeAccounts.filter { it.account.type == AccountType.LIABILITY }) {
            val isGroupIncluded = accountCalcConfig.isIncluded(group.account.id)
            val isGroupActive = group.account.isActive
            if (group.subAccounts.isEmpty()) {
                if (isGroupActive && !isGroupIncluded) {
                    list.add(
                        BudgetAccountItemHolder(
                            accountWithBal = group,
                            displayName = group.account.localizedName(languageMode),
                            balance = Math.abs(group.currentBalance),
                            effectiveBalance = Math.abs(group.currentBalance),
                            note = if (languageMode == LanguageMode.BANGLA) "গণনা থেকে বাদ" else "Excluded from calc",
                            iconName = group.account.iconName,
                            isSubAccount = false,
                            type = AccountType.LIABILITY
                        )
                    )
                }
            } else {
                if (isGroupActive) {
                    val excludedSubs = group.subAccounts.filter { it.account.isActive && (!isGroupIncluded || !accountCalcConfig.isIncluded(it.account.id)) }
                    if (excludedSubs.isNotEmpty()) {
                        val excludedBal = Math.abs(excludedSubs.sumOf { it.currentBalance })
                        val noteText = if (!isGroupIncluded) {
                            if (languageMode == LanguageMode.BANGLA) "সম্পূর্ণ গ্রুপ বাদ দেওয়া" else "Entire group excluded"
                        } else {
                            "${excludedSubs.size} ${if (languageMode == LanguageMode.BANGLA) "টি সাব-একাউন্ট বাদ" else "sub-accounts excluded"}"
                        }
                        list.add(
                            BudgetAccountItemHolder(
                                accountWithBal = group,
                                displayName = group.account.localizedName(languageMode),
                                balance = excludedBal,
                                effectiveBalance = excludedBal,
                                note = noteText,
                                iconName = group.account.iconName,
                                isSubAccount = false,
                                type = AccountType.LIABILITY
                            )
                        )
                    }
                }
            }
        }
        list.sortedByDescending { it.balance }
    }

    val inactiveLiabHolders = remember(accountsWithBalances, languageMode) {
        val list = mutableListOf<BudgetAccountItemHolder>()
        for (group in accountsWithBalances.filter { it.account.type == AccountType.LIABILITY }) {
            if (!group.account.isActive) {
                val bal = Math.abs(if (group.subAccounts.isEmpty()) group.currentBalance else group.subAccounts.sumOf { it.currentBalance })
                list.add(
                    BudgetAccountItemHolder(
                        accountWithBal = group,
                        displayName = group.account.localizedName(languageMode),
                        balance = bal,
                        effectiveBalance = bal,
                        note = if (languageMode == LanguageMode.BANGLA) "নিষ্ক্রিয় দায়" else "Archived / Inactive",
                        iconName = group.account.iconName,
                        isSubAccount = false,
                        type = AccountType.LIABILITY
                    )
                )
            } else if (group.subAccounts.isNotEmpty()) {
                val inactiveSubs = group.subAccounts.filter { !it.account.isActive }
                if (inactiveSubs.isNotEmpty()) {
                    val inactiveBal = Math.abs(inactiveSubs.sumOf { it.currentBalance })
                    list.add(
                        BudgetAccountItemHolder(
                            accountWithBal = group,
                            displayName = group.account.localizedName(languageMode),
                            balance = inactiveBal,
                            effectiveBalance = inactiveBal,
                            note = "${inactiveSubs.size} ${if (languageMode == LanguageMode.BANGLA) "টি নিষ্ক্রিয় সাব-একাউন্ট" else "inactive sub-accounts"}",
                            iconName = group.account.iconName,
                            isSubAccount = false,
                            type = AccountType.LIABILITY
                        )
                    )
                }
            }
        }
        list.sortedByDescending { it.balance }
    }

    val excludedAssets = remember(excludedAssetHolders) { excludedAssetHolders.sumOf { it.balance } }
    val excludedLiabilities = remember(excludedLiabHolders) { excludedLiabHolders.sumOf { it.balance } }
    val inactiveAssets = remember(inactiveAssetHolders) { inactiveAssetHolders.sumOf { it.balance } }
    val inactiveLiabilities = remember(inactiveLiabHolders) { inactiveLiabHolders.sumOf { it.balance } }

    fun openAccountTransactionsList(
        title: String,
        subtitle: String,
        txs: List<TransactionWithDetails>,
        totalAmount: Double,
        badgeColor: Color
    ) {
        showAmountDetail(
            AmountDetailInfo(
                title = title,
                subtitle = subtitle,
                totalAmount = totalAmount,
                formulaExplanation = if (languageMode == LanguageMode.BANGLA)
                    "মোট ${txs.size} টি সম্পর্কিত লেনদেনের তালিকা।"
                else
                    "List of ${txs.size} related transactions.",
                relatedTransactions = txs,
                customBadgeColor = badgeColor
            )
        )
    }

    fun openAccountBreakdown(
        accWithBal: AccountWithBalance,
        filterMode: AccountFilterMode = AccountFilterMode.CALCULATED
    ) {
        val acc = accWithBal.account
        val isGroupIncluded = accountCalcConfig.isIncluded(acc.id)
        val isGroupActive = acc.isActive

        val relevantSubAccounts = if (accWithBal.subAccounts.isNotEmpty()) {
            when (filterMode) {
                AccountFilterMode.CALCULATED -> accWithBal.subAccounts.filter { 
                    it.account.isActive && isGroupActive && isGroupIncluded && accountCalcConfig.isIncluded(it.account.id) 
                }
                AccountFilterMode.EXCLUDED -> accWithBal.subAccounts.filter { 
                    it.account.isActive && isGroupActive && (!isGroupIncluded || !accountCalcConfig.isIncluded(it.account.id)) 
                }
                AccountFilterMode.INACTIVE -> if (!isGroupActive) accWithBal.subAccounts else accWithBal.subAccounts.filter { !it.account.isActive }
            }
        } else {
            emptyList()
        }

        val targetAccountIds = if (accWithBal.subAccounts.isNotEmpty()) {
            val ids = relevantSubAccounts.map { it.account.id }.toSet()
            if (ids.isNotEmpty()) ids else setOf(acc.id)
        } else {
            setOf(acc.id)
        }

        val accTxs = transactionsWithDetails.filter { txItem ->
            val tx = txItem.transaction
            (tx.debitAccountId != null && targetAccountIds.contains(tx.debitAccountId)) ||
            (tx.creditAccountId != null && targetAccountIds.contains(tx.creditAccountId))
        }

        val inflowTxs = accTxs.filter { txItem ->
            val tx = txItem.transaction
            if (acc.type == AccountType.ASSET) {
                tx.debitAccountId != null && targetAccountIds.contains(tx.debitAccountId)
            } else {
                tx.creditAccountId != null && targetAccountIds.contains(tx.creditAccountId)
            }
        }

        val outflowTxs = accTxs.filter { txItem ->
            val tx = txItem.transaction
            if (acc.type == AccountType.ASSET) {
                tx.creditAccountId != null && targetAccountIds.contains(tx.creditAccountId)
            } else {
                tx.debitAccountId != null && targetAccountIds.contains(tx.debitAccountId)
            }
        }

        val totalInflows = inflowTxs.sumOf { it.transaction.amount }
        val totalOutflows = outflowTxs.sumOf { it.transaction.amount }
        val accName = acc.localizedName(languageMode)

        val breakdownItems = mutableListOf<BreakdownItem>()

        breakdownItems.add(
            BreakdownItem(
                name = if (languageMode == LanguageMode.BANGLA) "মোট ইনফ্লো / জমা" else "Total Inflow / Deposits",
                amount = totalInflows,
                iconName = "account_balance_wallet",
                color = SolidIncome,
                count = inflowTxs.size,
                note = if (languageMode == LanguageMode.BANGLA) "ট্যাপ করে সকল ইনফ্লো লেনদেন দেখুন" else "Tap to view incoming transactions",
                onClick = {
                    openAccountTransactionsList(
                        title = if (languageMode == LanguageMode.BANGLA) "$accName - ইনফ্লো তালিকা" else "$accName - Inflows",
                        subtitle = if (languageMode == LanguageMode.BANGLA) "মোট জমা ও আগমনী লেনদেনের বিস্তারিত" else "All incoming and deposit transactions",
                        txs = inflowTxs,
                        totalAmount = totalInflows,
                        badgeColor = SolidIncome
                    )
                }
            )
        )

        breakdownItems.add(
            BreakdownItem(
                name = if (languageMode == LanguageMode.BANGLA) "মোট আউটফ্লো / খরচ" else "Total Outflow / Payments",
                amount = -totalOutflows,
                iconName = "credit_card",
                color = SolidExpense,
                count = outflowTxs.size,
                note = if (languageMode == LanguageMode.BANGLA) "ট্যাপ করে সকল আউটফ্লো লেনদেন দেখুন" else "Tap to view outgoing transactions",
                onClick = {
                    openAccountTransactionsList(
                        title = if (languageMode == LanguageMode.BANGLA) "$accName - আউটফ্লো তালিকা" else "$accName - Outflows",
                        subtitle = if (languageMode == LanguageMode.BANGLA) "মোট খরচ ও বহির্গমন লেনদেনের বিস্তারিত" else "All outgoing transactions and expenses",
                        txs = outflowTxs,
                        totalAmount = -totalOutflows,
                        badgeColor = SolidExpense
                    )
                }
            )
        )

        if (relevantSubAccounts.isNotEmpty()) {
            relevantSubAccounts.forEach { sub ->
                val subBal = if (acc.type == AccountType.ASSET) sub.currentBalance else Math.abs(sub.currentBalance)
                val subSetting = accountCalcConfig.getSetting(sub.account.id)
                val subAdjNote = if (subSetting.adjustmentAmount != 0.0) {
                    val sign = if (subSetting.adjustmentAmount > 0) "+" else ""
                    "Adj: $sign${LanguageHelper.formatCurrency(subSetting.adjustmentAmount, languageMode)}"
                } else null

                val subTxs = transactionsWithDetails.filter { txItem ->
                    val tx = txItem.transaction
                    tx.debitAccountId == sub.account.id || tx.creditAccountId == sub.account.id
                }

                breakdownItems.add(
                    BreakdownItem(
                        name = sub.account.localizedName(languageMode),
                        amount = subBal,
                        iconName = sub.account.iconName,
                        color = if (acc.type == AccountType.ASSET) SolidIncome else SolidExpense,
                        count = subTxs.size,
                        note = subAdjNote,
                        onClick = {
                            openAccountTransactionsList(
                                title = sub.account.localizedName(languageMode),
                                subtitle = if (languageMode == LanguageMode.BANGLA) "সাব-একাউন্ট লেনদেনের বিস্তারিত" else "Sub-account transaction records",
                                txs = subTxs,
                                totalAmount = subBal,
                                badgeColor = if (acc.type == AccountType.ASSET) SolidIncome else SolidExpense
                            )
                        }
                    )
                )
            }
        }

        val effBal = if (acc.type == AccountType.ASSET) computeGroupEffective(accWithBal) else Math.abs(computeGroupEffective(accWithBal))
        val subtitleModeText = when (filterMode) {
            AccountFilterMode.CALCULATED -> if (languageMode == LanguageMode.BANGLA) "হিসাবকৃত একাউন্ট বিবরণ" else "Calculated Account Breakdown"
            AccountFilterMode.EXCLUDED -> if (languageMode == LanguageMode.BANGLA) "বাদ দেওয়া একাউন্ট বিবরণ" else "Excluded Account Breakdown"
            AccountFilterMode.INACTIVE -> if (languageMode == LanguageMode.BANGLA) "নিষ্ক্রিয় একাউন্ট বিবরণ" else "Inactive Account Breakdown"
        }

        showAmountDetail(
            AmountDetailInfo(
                title = accName,
                subtitle = subtitleModeText,
                totalAmount = effBal,
                formulaExplanation = if (languageMode == LanguageMode.BANGLA)
                    "মোট ${accTxs.size} টি লেনদেন এবং ${relevantSubAccounts.size} টি সাব-একাউন্ট সমন্বিত ব্যালেন্স।"
                else
                    "Effective balance including ${accTxs.size} transactions and ${relevantSubAccounts.size} sub-accounts.",
                relatedBreakdownItems = breakdownItems,
                relatedTransactions = accTxs,
                customBadgeColor = if (acc.type == AccountType.ASSET) SolidIncome else SolidExpense
            )
        )
    }

    fun openAssetsBreakdown(initialTab: Int = 0) {
        val calcBreakdownItems = calculatedAssetHolders.map { holder ->
            BreakdownItem(
                name = holder.displayName,
                amount = holder.effectiveBalance,
                percentage = if (calculatedAssets > 0) (holder.effectiveBalance / calculatedAssets) * 100.0 else 0.0,
                iconName = holder.iconName,
                color = SolidIncome,
                note = holder.note,
                onClick = { openAccountBreakdown(holder.accountWithBal, AccountFilterMode.CALCULATED) }
            )
        }.sortedByDescending { Math.abs(it.amount) }
        val calculatedTab = BreakdownTabInfo(
            title = if (languageMode == LanguageMode.BANGLA) "হিসাবকৃত" else "Calculated",
            totalAmount = calculatedAssets,
            subtitle = if (languageMode == LanguageMode.BANGLA) "সক্রিয় ও অন্তর্ভুক্ত সম্পদ একাউন্ট (সমন্বয় সহ)" else "Active & included asset accounts (with adjustments)",
            formulaExplanation = if (languageMode == LanguageMode.BANGLA)
                "শুধুমাত্র সক্রিয় এবং গণনায় অন্তর্ভুক্ত সম্পদ একাউন্টের ব্যালেন্স ও সমন্বয়ের সমষ্টি। বাদ দেওয়া ও নিষ্ক্রিয় একাউন্টগুলো এখানে যুক্ত করা হয়নি।"
            else
                "Sum of active and included asset account balances plus adjustments. Excluded and inactive accounts are removed.",
            items = calcBreakdownItems,
            transactions = emptyList(),
            customBadgeColor = SolidIncome,
            emptyMessage = if (languageMode == LanguageMode.BANGLA) "কোনো সক্রিয় ও অন্তর্ভুক্ত সম্পদ একাউন্ট পাওয়া যায়নি।" else "No active & included asset accounts found."
        )

        val excludedBreakdownItems = excludedAssetHolders.map { holder ->
            BreakdownItem(
                name = holder.displayName,
                amount = holder.balance,
                percentage = if (excludedAssets > 0) (holder.balance / excludedAssets) * 100.0 else 0.0,
                iconName = holder.iconName,
                color = Color(0xFFF59E0B),
                note = holder.note,
                onClick = { openAccountBreakdown(holder.accountWithBal, AccountFilterMode.EXCLUDED) }
            )
        }.sortedByDescending { Math.abs(it.amount) }
        val excludedTab = BreakdownTabInfo(
            title = if (languageMode == LanguageMode.BANGLA) "বাদ দেওয়া" else "Excluded",
            totalAmount = excludedAssets,
            subtitle = if (languageMode == LanguageMode.BANGLA) "গণনা থেকে বাদ দেওয়া সম্পদ একাউন্ট" else "Active asset accounts excluded from calculations",
            formulaExplanation = if (languageMode == LanguageMode.BANGLA)
                "এই একাউন্টগুলো সক্রিয় হলেও একাউন্ট সেটিংস থেকে ক্যালকুলেশনে বাদ (Excluded) রাখা হয়েছে।"
            else
                "These accounts are active but toggled OFF from calculations in Accounts configuration.",
            items = excludedBreakdownItems,
            transactions = emptyList(),
            customBadgeColor = Color(0xFFF59E0B),
            emptyMessage = if (languageMode == LanguageMode.BANGLA) "গণনা থেকে বাদ দেওয়া কোনো সম্পদ একাউন্ট নেই।" else "No asset accounts are excluded from calculations."
        )

        val inactiveBreakdownItems = inactiveAssetHolders.map { holder ->
            BreakdownItem(
                name = holder.displayName,
                amount = holder.balance,
                percentage = if (inactiveAssets > 0) (holder.balance / inactiveAssets) * 100.0 else 0.0,
                iconName = holder.iconName,
                color = Color.Gray,
                note = holder.note,
                onClick = { openAccountBreakdown(holder.accountWithBal, AccountFilterMode.INACTIVE) }
            )
        }.sortedByDescending { Math.abs(it.amount) }
        val inactiveTab = BreakdownTabInfo(
            title = if (languageMode == LanguageMode.BANGLA) "নিষ্ক্রিয়" else "Inactive",
            totalAmount = inactiveAssets,
            subtitle = if (languageMode == LanguageMode.BANGLA) "সকল নিষ্ক্রিয় সম্পদ একাউন্ট" else "Archived asset accounts",
            formulaExplanation = if (languageMode == LanguageMode.BANGLA)
                "এই একাউন্টগুলো নিষ্ক্রিয় (Inactive) হিসেবে চিহ্নিত রয়েছে এবং মূল আর্থিক হিসেবে যোগ করা হয় না।"
            else
                "These asset accounts are marked as inactive and are excluded from main financial calculations.",
            items = inactiveBreakdownItems,
            transactions = emptyList(),
            customBadgeColor = Color.Gray,
            emptyMessage = if (languageMode == LanguageMode.BANGLA) "কোনো নিষ্ক্রিয় সম্পদ একাউন্ট নেই।" else "No inactive asset accounts found."
        )

        val allTabs = listOf(calculatedTab, excludedTab, inactiveTab)
        val activeTab = allTabs.getOrElse(initialTab) { calculatedTab }

        showAmountDetail(
            AmountDetailInfo(
                title = if (languageMode == LanguageMode.BANGLA) "সম্পদ হিসাব ও বিবরণ" else "Assets Breakdown",
                subtitle = if (languageMode == LanguageMode.BANGLA) "হিসাবকৃত, বাদ দেওয়া ও নিষ্ক্রিয় সম্পদ" else "Calculated, Excluded & Inactive Assets",
                totalAmount = activeTab.totalAmount,
                formulaExplanation = activeTab.formulaExplanation,
                relatedBreakdownItems = activeTab.items,
                relatedTransactions = emptyList(),
                tabs = allTabs,
                defaultTabIndex = initialTab,
                customBadgeColor = SolidIncome
            )
        )
    }

    fun openLiabilitiesBreakdown(initialTab: Int = 0) {
        val calcBreakdownItems = calculatedLiabHolders.map { holder ->
            BreakdownItem(
                name = holder.displayName,
                amount = holder.effectiveBalance,
                percentage = if (calculatedLiabilities > 0) (holder.effectiveBalance / calculatedLiabilities) * 100.0 else 0.0,
                iconName = holder.iconName,
                color = SolidExpense,
                note = holder.note,
                onClick = { openAccountBreakdown(holder.accountWithBal, AccountFilterMode.CALCULATED) }
            )
        }.sortedByDescending { Math.abs(it.amount) }
        val calculatedTab = BreakdownTabInfo(
            title = if (languageMode == LanguageMode.BANGLA) "হিসাবকৃত" else "Calculated",
            totalAmount = calculatedLiabilities,
            subtitle = if (languageMode == LanguageMode.BANGLA) "সক্রিয় ও অন্তর্ভুক্ত দায় ও ঋণ (সমন্বয় সহ)" else "Active & included liabilities (with adjustments)",
            formulaExplanation = if (languageMode == LanguageMode.BANGLA)
                "শুধুমাত্র সক্রিয় এবং গণনায় অন্তর্ভুক্ত দায় একাউন্টের ব্যালেন্স ও সমন্বয়ের সমষ্টি। বাদ দেওয়া ও নিষ্ক্রিয় একাউন্টগুলো বাদ দেওয়া হয়েছে।"
            else
                "Sum of active and included liability balances plus adjustments. Excluded and inactive accounts are removed.",
            items = calcBreakdownItems,
            transactions = emptyList(),
            customBadgeColor = SolidExpense,
            emptyMessage = if (languageMode == LanguageMode.BANGLA) "কোনো সক্রিয় ও অন্তর্ভুক্ত দায় একাউন্ট পাওয়া যায়নি।" else "No active & included liability accounts found."
        )

        val excludedBreakdownItems = excludedLiabHolders.map { holder ->
            BreakdownItem(
                name = holder.displayName,
                amount = holder.balance,
                percentage = if (excludedLiabilities > 0) (holder.balance / excludedLiabilities) * 100.0 else 0.0,
                iconName = holder.iconName,
                color = Color(0xFFF59E0B),
                note = holder.note,
                onClick = { openAccountBreakdown(holder.accountWithBal, AccountFilterMode.EXCLUDED) }
            )
        }.sortedByDescending { Math.abs(it.amount) }
        val excludedTab = BreakdownTabInfo(
            title = if (languageMode == LanguageMode.BANGLA) "বাদ দেওয়া" else "Excluded",
            totalAmount = excludedLiabilities,
            subtitle = if (languageMode == LanguageMode.BANGLA) "গণনা থেকে বাদ দেওয়া দায় একাউন্ট" else "Active liability accounts excluded from calculations",
            formulaExplanation = if (languageMode == LanguageMode.BANGLA)
                "এই দায় একাউন্টগুলো সক্রিয় হলেও একাউন্ট সেটিংস থেকে ক্যালকুলেশনে বাদ রাখা হয়েছে।"
            else
                "These liability accounts are active but toggled OFF from calculations in Accounts configuration.",
            items = excludedBreakdownItems,
            transactions = emptyList(),
            customBadgeColor = Color(0xFFF59E0B),
            emptyMessage = if (languageMode == LanguageMode.BANGLA) "গণনা থেকে বাদ দেওয়া কোনো দায় একাউন্ট নেই।" else "No liability accounts are excluded from calculations."
        )

        val inactiveBreakdownItems = inactiveLiabHolders.map { holder ->
            BreakdownItem(
                name = holder.displayName,
                amount = holder.balance,
                percentage = if (inactiveLiabilities > 0) (holder.balance / inactiveLiabilities) * 100.0 else 0.0,
                iconName = holder.iconName,
                color = Color.Gray,
                note = holder.note,
                onClick = { openAccountBreakdown(holder.accountWithBal, AccountFilterMode.INACTIVE) }
            )
        }.sortedByDescending { Math.abs(it.amount) }
        val inactiveTab = BreakdownTabInfo(
            title = if (languageMode == LanguageMode.BANGLA) "নিষ্ক্রিয়" else "Inactive",
            totalAmount = inactiveLiabilities,
            subtitle = if (languageMode == LanguageMode.BANGLA) "সকল নিষ্ক্রিয় দায় ও ঋণ" else "Archived liability accounts",
            formulaExplanation = if (languageMode == LanguageMode.BANGLA)
                "এই দায় একাউন্টগুলো নিষ্ক্রিয় (Inactive) হিসেবে চিহ্নিত রয়েছে এবং মূল হিসেবে ধরা হয় না।"
            else
                "These liability accounts are marked as inactive and are excluded from main calculations.",
            items = inactiveBreakdownItems,
            transactions = emptyList(),
            customBadgeColor = Color.Gray,
            emptyMessage = if (languageMode == LanguageMode.BANGLA) "কোনো নিষ্ক্রিয় দায় একাউন্ট নেই।" else "No inactive liability accounts found."
        )

        val allTabs = listOf(calculatedTab, excludedTab, inactiveTab)
        val activeTab = allTabs.getOrElse(initialTab) { calculatedTab }

        showAmountDetail(
            AmountDetailInfo(
                title = if (languageMode == LanguageMode.BANGLA) "দায় ও ঋণ হিসাব" else "Liabilities Breakdown",
                subtitle = if (languageMode == LanguageMode.BANGLA) "হিসাবকৃত, বাদ দেওয়া ও নিষ্ক্রিয় দায়" else "Calculated, Excluded & Inactive Liabilities",
                totalAmount = activeTab.totalAmount,
                formulaExplanation = activeTab.formulaExplanation,
                relatedBreakdownItems = activeTab.items,
                relatedTransactions = emptyList(),
                tabs = allTabs,
                defaultTabIndex = initialTab,
                customBadgeColor = SolidExpense
            )
        )
    }

    fun openRemainingExpensesBreakdown() {
        val nowCal = Calendar.getInstance()
        val curStartMs = DateUtils.getStartOfMonth(nowCal.get(Calendar.YEAR), nowCal.get(Calendar.MONTH) + 1)
        val curEndMs = DateUtils.getEndOfMonth(nowCal.get(Calendar.YEAR), nowCal.get(Calendar.MONTH) + 1)

        val monthlyExpenseTxs = transactionsWithDetails.filter {
            it.transaction.type == TransactionType.EXPENSE &&
            it.transaction.dateEpochMs in curStartMs..curEndMs
        }

        val expenseCategories = allCategories.filter { it.type == CategoryType.EXPENSE }
        val bMap = monthlyBudgets.associateBy { "${it.itemType}_${it.itemId}" }

        val expenseTxsByCat = mutableMapOf<Long, Double>()
        val expenseTxsListByCat = mutableMapOf<Long, MutableList<TransactionWithDetails>>()

        for (txItem in monthlyExpenseTxs) {
            val catId = txItem.transaction.subCategoryId ?: txItem.transaction.categoryId
            if (catId != null) {
                expenseTxsByCat[catId] = (expenseTxsByCat[catId] ?: 0.0) + txItem.transaction.amount
                expenseTxsListByCat.getOrPut(catId) { mutableListOf() }.add(txItem)
            }
        }

        data class RemainingBudgetItem(
            val categoryName: String,
            val iconName: String?,
            val budgetLimit: Double,
            val actualSpent: Double,
            val remainingAmount: Double,
            val txs: List<TransactionWithDetails>
        )

        val remainingBudgetItems = mutableListOf<RemainingBudgetItem>()

        val parentExpenseCatIdsWithChildren = expenseCategories
            .filter { it.parentId != null }
            .mapNotNull { it.parentId }
            .toSet()

        val activeBudgetedCategories = expenseCategories.filter {
            it.parentId != null || !parentExpenseCatIdsWithChildren.contains(it.id) || bMap.containsKey("EXPENSE_${it.id}")
        }

        for (cat in activeBudgetedCategories) {
            val budgetEntry = bMap["EXPENSE_${cat.id}"]
            val isEnabled = budgetEntry?.isEnabled ?: (cat.budgetLimit > 0)
            val budgetLimit = if (isEnabled) (budgetEntry?.budgetedAmount ?: cat.budgetLimit) else 0.0
            val spent = expenseTxsByCat[cat.id] ?: 0.0
            val catTxs = expenseTxsListByCat[cat.id] ?: emptyList()

            if (budgetLimit > 0 && spent < budgetLimit) {
                val remaining = budgetLimit - spent
                remainingBudgetItems.add(
                    RemainingBudgetItem(
                        categoryName = cat.localizedName(languageMode),
                        iconName = cat.iconName,
                        budgetLimit = budgetLimit,
                        actualSpent = spent,
                        remainingAmount = remaining,
                        txs = catTxs
                    )
                )
            }
        }

        val sortedRemaining = remainingBudgetItems.sortedByDescending { Math.abs(it.remainingAmount) }

        val breakdownItems = sortedRemaining.map { item ->
            val note = if (languageMode == LanguageMode.BANGLA)
                "বাজেট: ${LanguageHelper.formatCurrency(item.budgetLimit, languageMode)} | খরচ: ${LanguageHelper.formatCurrency(item.actualSpent, languageMode)}"
            else
                "Budget: ${LanguageHelper.formatCurrency(item.budgetLimit, languageMode)} | Spent: ${LanguageHelper.formatCurrency(item.actualSpent, languageMode)}"

            BreakdownItem(
                name = item.categoryName,
                amount = item.remainingAmount,
                percentage = if (overview.remainingExpenses > 0) (item.remainingAmount / overview.remainingExpenses) * 100.0 else 0.0,
                iconName = item.iconName,
                color = SolidPrimary,
                count = item.txs.size,
                note = note,
                onClick = {
                    showAmountDetail(
                        AmountDetailInfo(
                            title = item.categoryName,
                            subtitle = if (languageMode == LanguageMode.BANGLA) "অবশিষ্ট বাজেট ও সম্পর্কিত লেনদেন" else "Remaining Budget & Transactions",
                            totalAmount = item.remainingAmount,
                            formulaExplanation = if (languageMode == LanguageMode.BANGLA)
                                "বাজেট সীমা ${LanguageHelper.formatCurrency(item.budgetLimit, languageMode)} − প্রকৃত খরচ ${LanguageHelper.formatCurrency(item.actualSpent, languageMode)} = অবশিষ্ট ${LanguageHelper.formatCurrency(item.remainingAmount, languageMode)}।"
                            else
                                "Budget ${LanguageHelper.formatCurrency(item.budgetLimit, languageMode)} − Spent ${LanguageHelper.formatCurrency(item.actualSpent, languageMode)} = Remaining ${LanguageHelper.formatCurrency(item.remainingAmount, languageMode)}.",
                            formulaSteps = emptyList(),
                            relatedTransactions = item.txs,
                            customBadgeColor = SolidPrimary
                        )
                    )
                }
            )
        }

        showAmountDetail(
            AmountDetailInfo(
                title = if (languageMode == LanguageMode.BANGLA) "বাজেটের অবশিষ্ট খরচ" else "Remaining Budget Expenses",
                subtitle = if (languageMode == LanguageMode.BANGLA) "ক্যাটাগরি অনুযায়ী অবশিষ্ট বাজেট" else "Category-by-category remaining budget",
                totalAmount = overview.remainingExpenses,
                formulaExplanation = if (languageMode == LanguageMode.BANGLA)
                    "প্রতিটি ক্যাটাগরির জন্য পৃথকভাবে হিসাবকৃত (বাজেট সীমা − প্রকৃত খরচ, সর্বনিম্ন ০) অবশিষ্ট টাকার যোগফল। বিস্তারিত লেনদেন দেখতে প্রতিটি ক্যাটাগরিতে ট্যাপ করুন।"
                else
                    "Sum of remaining unspent balances across all active category budgets for this month. Tap any category to view its related transactions.",
                formulaSteps = emptyList(),
                relatedBreakdownItems = breakdownItems,
                relatedTransactions = emptyList()
            )
        )
    }

    fun openExpendableBreakdown() {
        val currentExpendable = calculatedAssets - calculatedLiabilities - overview.remainingExpenses
        val components = listOf(
            BreakdownItem(
                name = if (languageMode == LanguageMode.BANGLA) "হিসাবকৃত সম্পদ (Assets)" else "Calculated Assets",
                amount = calculatedAssets,
                iconName = "account_balance_wallet",
                color = SolidIncome,
                note = if (languageMode == LanguageMode.BANGLA) "সক্রিয় ও অন্তর্ভুক্ত সম্পদ" else "Active & included assets",
                onClick = { openAssetsBreakdown(0) }
            ),
            BreakdownItem(
                name = if (languageMode == LanguageMode.BANGLA) "হিসাবকৃত দায় (Liabilities)" else "Calculated Liabilities",
                amount = -calculatedLiabilities,
                iconName = "credit_card",
                color = SolidExpense,
                note = if (languageMode == LanguageMode.BANGLA) "সক্রিয় ও অন্তর্ভুক্ত দায়" else "Active & included liabilities",
                onClick = { openLiabilitiesBreakdown(0) }
            ),
            BreakdownItem(
                name = if (languageMode == LanguageMode.BANGLA) "বাজেটের অবশিষ্ট খরচ" else "Remaining Expenses",
                amount = -overview.remainingExpenses,
                iconName = "shopping_bag",
                color = SolidExpense,
                note = if (languageMode == LanguageMode.BANGLA) "বাজেটের বাকি খরচ" else "Remaining unspent budget",
                onClick = { openRemainingExpensesBreakdown() }
            )
        ).sortedByDescending { Math.abs(it.amount) }

        showAmountDetail(
            AmountDetailInfo(
                title = if (languageMode == LanguageMode.BANGLA) "খরচযোগ্য অবশিষ্ট অর্থের হিসাব" else "Expendable Funds Breakdown",
                subtitle = if (languageMode == LanguageMode.BANGLA) "সক্রিয় ও অন্তর্ভুক্ত সম্পদ − দায় − অবশিষ্ট বাজেট খরচ" else "Active & Included Assets − Liabilities − Remaining Expenses",
                totalAmount = currentExpendable,
                formulaExplanation = if (languageMode == LanguageMode.BANGLA)
                    "খরচযোগ্য অর্থ = সক্রিয় ও অন্তর্ভুক্ত সম্পদ (${LanguageHelper.formatCurrency(calculatedAssets, languageMode)}) − সক্রিয় ও অন্তর্ভুক্ত দায় (${LanguageHelper.formatCurrency(calculatedLiabilities, languageMode)}) − বাজেটের অবশিষ্ট খরচ (${LanguageHelper.formatCurrency(overview.remainingExpenses, languageMode)})।"
                else
                    "Expendable = Active & Included Assets (${LanguageHelper.formatCurrency(calculatedAssets, languageMode)}) − Active & Included Liabilities (${LanguageHelper.formatCurrency(calculatedLiabilities, languageMode)}) − Remaining Expenses (${LanguageHelper.formatCurrency(overview.remainingExpenses, languageMode)}).",
                formulaSteps = emptyList(),
                relatedBreakdownItems = components,
                relatedTransactions = emptyList(),
                statusTag = if (currentExpendable >= 0) "Safe" else "Deficit"
            )
        )
    }

    val handleBudgetItemClick: (BudgetTargetItem) -> Unit = { item ->
        if (item.itemType == "ASSET" || item.itemType == "LIABILITY") {
            val account = allAccounts.firstOrNull { it.id == item.id }
            if (account != null) {
                onAccountClick?.invoke(account)
            }
        }
    }

    // Tabs definition matching user requested order: BM Dashboard, Expenses, Incomes, Assets, Liabilities
    val tabLabels = listOf(
        if (languageMode == LanguageMode.BANGLA) "বিএম ড্যাশবোর্ড" else "BM Dashboard",
        if (languageMode == LanguageMode.BANGLA) "ব্যয়" else "Expenses",
        if (languageMode == LanguageMode.BANGLA) "আয়" else "Incomes",
        if (languageMode == LanguageMode.BANGLA) "সম্পদ" else "Assets",
        if (languageMode == LanguageMode.BANGLA) "দায়" else "Liabilities"
    )

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            if (!tabsAtTop) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp,
                    shadowElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    BudgetTabBar(
                        selectedTab = selectedTab,
                        tabLabels = tabLabels,
                        onTabSelected = { selectedTab = it }
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("budget_screen")
        ) {
            // Top Bar with Menu/Back and "Budget Maker" Label on First Row, and Settings on Right
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // First Row: Menu/Back + "Budget Maker" Label (Left), Search + Settings (Right)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Left: Menu Button and Budget Maker Title
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            IconButton(
                                onClick = onOpenDrawer,
                                modifier = Modifier.size(36.dp).testTag("budget_menu_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Menu",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "বাজেট মেকার" else "Budget Maker",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                        }

                        // Right: Search, Filter, and Settings (with Copy & Paste moved inside Settings)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    isSearchActive = !isSearchActive
                                    if (!isSearchActive) {
                                        searchQuery = ""
                                        keyboardController?.hide()
                                    }
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Menu-based Filter button with active badge for current tab
                            IconButton(
                                onClick = { showFilterDialog = true },
                                modifier = Modifier.size(36.dp).testTag("budget_filter_btn")
                            ) {
                                if (isCurrentTabFilterActive) {
                                    androidx.compose.material3.BadgedBox(
                                        badge = {
                                            androidx.compose.material3.Badge(
                                                containerColor = when (selectedTab) {
                                                    1 -> SolidExpense
                                                    2 -> SolidIncome
                                                    3 -> SolidPrimary
                                                    4 -> AmberGold
                                                    else -> MaterialTheme.colorScheme.primary
                                                },
                                                contentColor = Color.White
                                            ) {
                                                Text(text = "$currentTabActiveCount", fontSize = 9.sp)
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.FilterList,
                                            contentDescription = "Filter",
                                            tint = when (selectedTab) {
                                                1 -> SolidExpense
                                                2 -> SolidIncome
                                                3 -> SolidPrimary
                                                4 -> AmberGold
                                                else -> MaterialTheme.colorScheme.primary
                                            },
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.FilterList,
                                        contentDescription = "Filter",
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Box {
                                IconButton(
                                    onClick = { showTabSettingsMenu = true },
                                    modifier = Modifier.size(36.dp).testTag("budget_tab_settings_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "Settings",
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                DropdownMenu(
                                    expanded = showTabSettingsMenu,
                                    onDismissRequest = { showTabSettingsMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = if (languageMode == LanguageMode.BANGLA) "বাজেট কপি ও পেস্ট" else "Copy & Paste Budgets",
                                                fontSize = 13.5.sp
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                                        },
                                        onClick = {
                                            showTabSettingsMenu = false
                                            showCopyPasteDialog = true
                                        }
                                    )

                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = if (languageMode == LanguageMode.BANGLA) "ট্যাব উপরে দেখান" else "Tabs at Top",
                                                fontWeight = if (tabsAtTop) FontWeight.Bold else FontWeight.Normal,
                                                color = if (tabsAtTop) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(Icons.Default.VerticalAlignTop, contentDescription = null, modifier = Modifier.size(18.dp))
                                        },
                                        trailingIcon = {
                                            if (tabsAtTop) {
                                                Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                            }
                                        },
                                        onClick = {
                                            viewModel.setBudgetMakerTabPosition(TabPosition.TOP)
                                            showTabSettingsMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = if (languageMode == LanguageMode.BANGLA) "ট্যাব নিচে দেখান" else "Tabs at Bottom",
                                                fontWeight = if (!tabsAtTop) FontWeight.Bold else FontWeight.Normal,
                                                color = if (!tabsAtTop) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(Icons.Default.VerticalAlignBottom, contentDescription = null, modifier = Modifier.size(18.dp))
                                        },
                                        trailingIcon = {
                                            if (!tabsAtTop) {
                                                Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                            }
                                        },
                                        onClick = {
                                            viewModel.setBudgetMakerTabPosition(TabPosition.BOTTOM)
                                            showTabSettingsMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Second Row: Month name and selector (under the top bar first row)
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 5.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                                modifier = Modifier.clip(RoundedCornerShape(20.dp))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    IconButton(
                                        onClick = { viewModel.prevBudgetMonth() },
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Previous Month",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { showMonthYearPicker = true }
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.CalendarMonth,
                                            contentDescription = null,
                                            modifier = Modifier.size(15.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = DateUtils.formatMonthYear(selectedYear, selectedMonth, languageMode),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.5.sp,
                                            color = MaterialTheme.colorScheme.primary,
                                            maxLines = 1
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Icon(
                                            Icons.Default.KeyboardArrowDown,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    IconButton(
                                        onClick = { viewModel.nextBudgetMonth() },
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = "Next Month",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Active Filters Summary Indicator Strip for current tab
                    AnimatedVisibility(visible = isCurrentTabFilterActive) {
                        val tabName = when (selectedTab) {
                            0 -> if (languageMode == LanguageMode.BANGLA) "ড্যাশবোর্ড" else "Dashboard"
                            1 -> if (languageMode == LanguageMode.BANGLA) "ব্যয়" else "Expenses"
                            2 -> if (languageMode == LanguageMode.BANGLA) "আয়" else "Incomes"
                            3 -> if (languageMode == LanguageMode.BANGLA) "সম্পদ" else "Assets"
                            4 -> if (languageMode == LanguageMode.BANGLA) "দায়" else "Liabilities"
                            else -> "Tab"
                        }
                        val tabColor = when (selectedTab) {
                            0 -> MaterialTheme.colorScheme.primary
                            1 -> SolidExpense
                            2 -> SolidIncome
                            3 -> SolidPrimary
                            4 -> AmberGold
                            else -> MaterialTheme.colorScheme.primary
                        }

                        if (selectedTab == 0) {
                            Surface(
                                color = tabColor.copy(alpha = 0.10f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(0.8.dp, tabColor.copy(alpha = 0.30f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 3.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { showFilterDialog = true }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.FilterList,
                                            contentDescription = null,
                                            tint = tabColor,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Text(
                                            text = if (languageMode == LanguageMode.BANGLA)
                                                "ড্যাশবোর্ড ফিল্টার সক্রিয় (${dashboardFilter.activeCount}টি নিয়ম)"
                                            else
                                                "Dashboard Filter Active (${dashboardFilter.activeCount} rules applied)",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                                        onClick = { dashboardFilter = BudgetMakerDashboardFilter() },
                                        modifier = Modifier.size(22.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Clear Filters",
                                                tint = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            ActiveBudgetMakerFilterBar(
                                tabName = tabName,
                                tabFilter = currentTabFilter,
                                onFilterChange = { newFilter ->
                                    when (selectedTab) {
                                        1 -> expenseFilter = newFilter
                                        2 -> incomeFilter = newFilter
                                        3 -> assetFilter = newFilter
                                        4 -> liabilityFilter = newFilter
                                    }
                                },
                                onOpenFilterDialog = { showFilterDialog = true },
                                accentColor = tabColor,
                                languageMode = languageMode,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            // Animated Search Input Field
            AnimatedVisibility(
                visible = isSearchActive,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                                BasicTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Normal
                                    ),
                                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                    decorationBox = { innerTextField ->
                                        if (searchQuery.isEmpty()) {
                                            Text(
                                                text = if (languageMode == LanguageMode.BANGLA) "ক্যাটাগরি বা একাউন্ট খুঁজুন..." else "Search category, account, or group...",
                                                color = MaterialTheme.colorScheme.outline,
                                                fontSize = 13.sp
                                            )
                                        }
                                        innerTextField()
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .focusRequester(searchFocusRequester)
                                )
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = { searchQuery = "" },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Clear",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // If tabs are configured at Top, render TabRow here
            if (tabsAtTop) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    BudgetTabBar(
                        selectedTab = selectedTab,
                        tabLabels = tabLabels,
                        onTabSelected = { selectedTab = it }
                    )
                }
            }

            // Tab Content Views with Swipeable HorizontalPager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                beyondViewportPageCount = 1
            ) { pageIndex ->
                when (pageIndex) {
                    0 -> {
                        // TAB 0: BM DASHBOARD
                        BudgetDashboardView(
                            year = selectedYear,
                            month = selectedMonth,
                            languageMode = languageMode,
                            totalExpenses = totalExpensesBudget,
                            totalLiabilities = totalLiabilitiesBudget,
                            totalOutflows = totalOutflowsBudget,
                            totalIncomes = totalIncomesBudget,
                            totalAssets = totalAssetsBudget,
                            totalInflows = totalInflowsBudget,
                            runningExpendable = runningExpendable,
                            onNavigateToTab = { tabIdx ->
                                when (tabIdx) {
                                    1 -> {
                                        expenseSelectedFilters = setOf(BudgetFilterOption.BUDGETED_ONLY, BudgetFilterOption.ONLY_CATEGORIES)
                                        expenseSelectedSort = BudgetSortOption.BUDGET_DESC
                                        selectedTab = 1
                                    }
                                    2 -> {
                                        incomeSelectedFilters = setOf(BudgetFilterOption.BUDGETED_ONLY, BudgetFilterOption.ONLY_CATEGORIES)
                                        incomeSelectedSort = BudgetSortOption.BUDGET_DESC
                                        selectedTab = 2
                                    }
                                    3 -> {
                                        assetSelectedFilters = setOf(BudgetFilterOption.BUDGETED_ONLY, BudgetFilterOption.ONLY_CATEGORIES)
                                        assetSelectedSort = BudgetSortOption.BUDGET_DESC
                                        selectedTab = 3
                                    }
                                    4 -> {
                                        liabilitySelectedFilters = setOf(BudgetFilterOption.BUDGETED_ONLY, BudgetFilterOption.ONLY_CATEGORIES)
                                        liabilitySelectedSort = BudgetSortOption.BUDGET_DESC
                                        selectedTab = 4
                                    }
                                    else -> selectedTab = tabIdx
                                }
                            },
                            onRunningExpendableClick = { openExpendableBreakdown() }
                        )
                    }
                    1 -> {
                        // TAB 1: EXPENSES
                        CategoriesBudgetEntryView(
                            title = LanguageHelper.getString("expenses", languageMode),
                            items = expenseItems,
                            monthlyBudgets = monthlyBudgets,
                            allTransactions = transactionsWithDetails,
                            allAccounts = allAccounts,
                            accountsWithBalances = accountsWithBalances,
                            selectedYear = selectedYear,
                            selectedMonth = selectedMonth,
                            languageMode = languageMode,
                            sectionColor = SolidExpense,
                            isPeriodicFlow = true,
                            globalFrequency = globalExpenseFrequency,
                            onGlobalFrequencyChange = { globalExpenseFrequency = it },
                            totalBudgetAmount = totalExpensesBudget,
                            searchQuery = searchQuery,
                            tabFilter = expenseFilter,
                            selectedFilters = expenseSelectedFilters,
                            onFilterToggle = { opt ->
                                expenseSelectedFilters = toggleFilterOption(expenseSelectedFilters, opt)
                            },
                            selectedSort = expenseSelectedSort,
                            onSortChange = { expenseSelectedSort = it },
                            onMonthClick = { showMonthYearPicker = true },
                            onPrevMonth = { viewModel.prevBudgetMonth() },
                            onNextMonth = { viewModel.nextBudgetMonth() },
                            onCopyPrevious = {
                                showCopyPreviousConfirmDialog = true
                            },
                            onItemClick = handleBudgetItemClick,
                            onSaveBudget = { item, amount, enabled ->
                                viewModel.saveMonthlyBudget(
                                    itemType = item.itemType,
                                    itemId = item.id,
                                    amount = amount,
                                    isEnabled = enabled
                                )
                            },
                            onSaveMultiple = { budgets ->
                                viewModel.saveMultipleMonthlyBudgets(budgets)
                            }
                        )
                    }
                    2 -> {
                        // TAB 2: INCOMES
                        CategoriesBudgetEntryView(
                            title = LanguageHelper.getString("incomes", languageMode),
                            items = incomeItems,
                            monthlyBudgets = monthlyBudgets,
                            allTransactions = transactionsWithDetails,
                            allAccounts = allAccounts,
                            accountsWithBalances = accountsWithBalances,
                            selectedYear = selectedYear,
                            selectedMonth = selectedMonth,
                            languageMode = languageMode,
                            sectionColor = SolidIncome,
                            isPeriodicFlow = true,
                            globalFrequency = globalIncomeFrequency,
                            onGlobalFrequencyChange = { globalIncomeFrequency = it },
                            totalBudgetAmount = totalIncomesBudget,
                            searchQuery = searchQuery,
                            tabFilter = incomeFilter,
                            selectedFilters = incomeSelectedFilters,
                            onFilterToggle = { opt ->
                                incomeSelectedFilters = toggleFilterOption(incomeSelectedFilters, opt)
                            },
                            selectedSort = incomeSelectedSort,
                            onSortChange = { incomeSelectedSort = it },
                            onMonthClick = { showMonthYearPicker = true },
                            onPrevMonth = { viewModel.prevBudgetMonth() },
                            onNextMonth = { viewModel.nextBudgetMonth() },
                            onCopyPrevious = {
                                showCopyPreviousConfirmDialog = true
                            },
                            onItemClick = handleBudgetItemClick,
                            onSaveBudget = { item, amount, enabled ->
                                viewModel.saveMonthlyBudget(
                                    itemType = item.itemType,
                                    itemId = item.id,
                                    amount = amount,
                                    isEnabled = enabled
                                )
                            },
                            onSaveMultiple = { budgets ->
                                viewModel.saveMultipleMonthlyBudgets(budgets)
                            }
                        )
                    }
                    3 -> {
                        // TAB 3: ASSETS
                        CategoriesBudgetEntryView(
                            title = LanguageHelper.getString("assets", languageMode),
                            items = assetItems,
                            monthlyBudgets = monthlyBudgets,
                            allTransactions = transactionsWithDetails,
                            allAccounts = allAccounts,
                            accountsWithBalances = accountsWithBalances,
                            selectedYear = selectedYear,
                            selectedMonth = selectedMonth,
                            languageMode = languageMode,
                            sectionColor = SolidPrimary,
                            isPeriodicFlow = false,
                            globalFrequency = BudgetFrequency.MONTHLY,
                            onGlobalFrequencyChange = {},
                            totalBudgetAmount = totalAssetsBudget,
                            searchQuery = searchQuery,
                            tabFilter = assetFilter,
                            selectedFilters = assetSelectedFilters,
                            onFilterToggle = { opt ->
                                assetSelectedFilters = toggleFilterOption(assetSelectedFilters, opt)
                            },
                            selectedSort = assetSelectedSort,
                            onSortChange = { assetSelectedSort = it },
                            onMonthClick = { showMonthYearPicker = true },
                            onPrevMonth = { viewModel.prevBudgetMonth() },
                            onNextMonth = { viewModel.nextBudgetMonth() },
                            onCopyPrevious = {
                                showCopyPreviousConfirmDialog = true
                            },
                            onItemClick = handleBudgetItemClick,
                            onSaveBudget = { item, amount, enabled ->
                                viewModel.saveMonthlyBudget(
                                    itemType = item.itemType,
                                    itemId = item.id,
                                    amount = amount,
                                    isEnabled = enabled
                                )
                            },
                            onSaveMultiple = { budgets ->
                                viewModel.saveMultipleMonthlyBudgets(budgets)
                            }
                        )
                    }
                    4 -> {
                        // TAB 4: LIABILITIES
                        CategoriesBudgetEntryView(
                            title = LanguageHelper.getString("liabilities", languageMode),
                            items = liabilityItems,
                            monthlyBudgets = monthlyBudgets,
                            allTransactions = transactionsWithDetails,
                            allAccounts = allAccounts,
                            accountsWithBalances = accountsWithBalances,
                            selectedYear = selectedYear,
                            selectedMonth = selectedMonth,
                            languageMode = languageMode,
                            sectionColor = AmberGold,
                            isPeriodicFlow = false,
                            globalFrequency = BudgetFrequency.MONTHLY,
                            onGlobalFrequencyChange = {},
                            totalBudgetAmount = totalLiabilitiesBudget,
                            searchQuery = searchQuery,
                            tabFilter = liabilityFilter,
                            selectedFilters = liabilitySelectedFilters,
                            onFilterToggle = { opt ->
                                liabilitySelectedFilters = toggleFilterOption(liabilitySelectedFilters, opt)
                            },
                            selectedSort = liabilitySelectedSort,
                            onSortChange = { liabilitySelectedSort = it },
                            onMonthClick = { showMonthYearPicker = true },
                            onPrevMonth = { viewModel.prevBudgetMonth() },
                            onNextMonth = { viewModel.nextBudgetMonth() },
                            onCopyPrevious = {
                                showCopyPreviousConfirmDialog = true
                            },
                            onItemClick = handleBudgetItemClick,
                            onSaveBudget = { item, amount, enabled ->
                                viewModel.saveMonthlyBudget(
                                    itemType = item.itemType,
                                    itemId = item.id,
                                    amount = amount,
                                    isEnabled = enabled
                                )
                            },
                            onSaveMultiple = { budgets ->
                                viewModel.saveMultipleMonthlyBudgets(budgets)
                            }
                        )
                    }
                }
            }
        }
    }

    // Month & Year Picker Dialog
    if (showMonthYearPicker) {
        MonthYearPickerDialog(
            currentYear = selectedYear,
            currentMonth = selectedMonth,
            languageMode = languageMode,
            onDismiss = { showMonthYearPicker = false },
            onConfirm = { y, m ->
                viewModel.setBudgetYearMonth(y, m)
                showMonthYearPicker = false
            }
        )
    }

    // Tab-specific Budget Maker Filter Dialog
    if (showFilterDialog) {
        BudgetMakerFilterDialog(
            tabIndex = selectedTab,
            currentTabFilter = currentTabFilter,
            dashboardFilter = dashboardFilter,
            categories = allCategories,
            accounts = allAccounts,
            selectedYear = selectedYear,
            selectedMonth = selectedMonth,
            languageMode = languageMode,
            onDismiss = { showFilterDialog = false },
            onApplyTabFilter = { newTabFilter ->
                when (selectedTab) {
                    1 -> expenseFilter = newTabFilter
                    2 -> incomeFilter = newTabFilter
                    3 -> assetFilter = newTabFilter
                    4 -> liabilityFilter = newTabFilter
                }
                showFilterDialog = false
            },
            onApplyDashboardFilter = { newDashFilter ->
                dashboardFilter = newDashFilter
                showFilterDialog = false
            }
        )
    }

    // Help Dialog
    if (showHelpDialog) {
        BudgetHelpDialog(onDismiss = { showHelpDialog = false })
    }

    // Quick Action FAB Dialog
    if (showQuickActionSheet) {
        Dialog(onDismissRequest = { showQuickActionSheet = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Budget Actions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Manage budgets for ${DateUtils.formatMonthYear(selectedYear, selectedMonth, languageMode)}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline
                    )

                    HorizontalDivider()

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showQuickActionSheet = false
                                showCopyPreviousConfirmDialog = true
                            }
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = BrandBlue)
                            Column {
                                Text("Copy from Previous Month", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text("Duplicate all active categories and limits", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showQuickActionSheet = false
                                showCopyPasteDialog = true
                            }
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Default.ContentPaste, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Column {
                                Text("Copy & Paste Target Month", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text("Copy budgets between any past or future months", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showQuickActionSheet = false
                                scope.launch {
                                    snackbarHostState.showSnackbar("All budgets are saved automatically")
                                }
                            }
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Default.AssignmentTurnedIn, contentDescription = null, tint = SolidIncome)
                            Column {
                                Text("Auto-Save Active", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text("Every budget modification saves immediately", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        OutlinedButton(onClick = { showQuickActionSheet = false }) {
                            Text("Close")
                        }
                    }
                }
            }
        }
    }

    // Confirmation Dialog for Copy from Previous Month
    if (showCopyPreviousConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showCopyPreviousConfirmDialog = false },
            title = {
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "আগের মাসের বাজেট কপি করবেন?" else "Copy from Previous Month?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                val prevCal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, selectedYear)
                    set(Calendar.MONTH, selectedMonth - 1)
                    add(Calendar.MONTH, -1)
                }
                val prevYear = prevCal.get(Calendar.YEAR)
                val prevMonth = prevCal.get(Calendar.MONTH) + 1
                val prevMonthStr = DateUtils.formatMonthYear(prevYear, prevMonth, languageMode)
                val currentMonthStr = DateUtils.formatMonthYear(selectedYear, selectedMonth, languageMode)

                Text(
                    text = if (languageMode == LanguageMode.BANGLA) {
                        "$prevMonthStr এর সমস্ত সেট করা বাজেট $currentMonthStr এ কপি হয়ে প্রতিস্থাপিত হবে। আপনি কি নিশ্চিত?"
                    } else {
                        "All active budgets from $prevMonthStr will be copied and applied to $currentMonthStr. Are you sure you want to proceed?"
                    },
                    fontSize = 13.5.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.copyBudgetsFromPreviousMonth()
                        showCopyPreviousConfirmDialog = false
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                if (languageMode == LanguageMode.BANGLA) "আগের মাসের বাজেট সফলভাবে কপি হয়েছে" else "Copied previous month's budgets successfully"
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(if (languageMode == LanguageMode.BANGLA) "হ্যাঁ, কপি করুন" else "Yes, Copy")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showCopyPreviousConfirmDialog = false }) {
                    Text(if (languageMode == LanguageMode.BANGLA) "বাতিল" else "Cancel")
                }
            }
        )
    }

    // Copy & Paste Dialog for any Source/Target Months
    if (showCopyPasteDialog) {
        CopyPasteBudgetDialog(
            currentYear = selectedYear,
            currentMonth = selectedMonth,
            languageMode = languageMode,
            onDismiss = { showCopyPasteDialog = false },
            onConfirm = { fromY, fromM, toY, toM ->
                viewModel.copyBudgets(fromY, fromM, toY, toM)
                showCopyPasteDialog = false
                scope.launch {
                    val fromStr = DateUtils.formatMonthYear(fromY, fromM, languageMode)
                    val toStr = DateUtils.formatMonthYear(toY, toM, languageMode)
                    snackbarHostState.showSnackbar(
                        if (languageMode == LanguageMode.BANGLA) "$fromStr থেকে $toStr এ বাজেট কপি হয়েছে" else "Budgets copied from $fromStr to $toStr"
                    )
                }
            }
        )
    }

    // Modal Amount Breakdown Dialog Stack
    val activeAmountDetail = amountDetailStack.lastOrNull()
    if (activeAmountDetail != null) {
        AmountBreakdownDialog(
            info = activeAmountDetail,
            languageMode = languageMode,
            onDismiss = {
                if (amountDetailStack.isNotEmpty()) {
                    amountDetailStack.removeLastOrNull()
                }
            },
            onTransactionClick = { tx ->
                onEditTransaction(tx)
            },
            onAccountClick = { acc ->
                val found = accountsWithBalances.find { it.account.id == acc.id }
                    ?: activeAccounts.find { it.account.id == acc.id }
                    ?: inactiveAccounts.find { it.account.id == acc.id }
                if (found != null) {
                    val mode = if (!found.account.isActive) {
                        AccountFilterMode.INACTIVE
                    } else if (!accountCalcConfig.isIncluded(found.account.id)) {
                        AccountFilterMode.EXCLUDED
                    } else {
                        AccountFilterMode.CALCULATED
                    }
                    openAccountBreakdown(found, mode)
                } else {
                    onAccountClick?.invoke(acc)
                }
            },
            canGoBack = amountDetailStack.size > 1,
            onBack = {
                if (amountDetailStack.isNotEmpty()) {
                    amountDetailStack.removeLastOrNull()
                }
            },
            onCloseAll = {
                amountDetailStack.clear()
            },
            onTabChanged = { tabIdx ->
                if (amountDetailStack.isNotEmpty()) {
                    val last = amountDetailStack.last()
                    amountDetailStack[amountDetailStack.lastIndex] = last.copy(defaultTabIndex = tabIdx)
                }
            }
        )
    }
}

@Composable
private fun BudgetTabBar(
    selectedTab: Int,
    tabLabels: List<String>,
    onTabSelected: (Int) -> Unit
) {
    ScrollableTabRow(
        selectedTabIndex = selectedTab,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary,
        edgePadding = 12.dp,
        divider = {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
        },
        indicator = { tabPositions ->
            if (selectedTab in tabPositions.indices) {
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = when (selectedTab) {
                        0 -> MaterialTheme.colorScheme.primary
                        1 -> SolidExpense
                        2 -> SolidIncome
                        3 -> SolidPrimary
                        4 -> AmberGold
                        else -> MaterialTheme.colorScheme.primary
                    },
                    height = 2.5.dp
                )
            }
        }
    ) {
        tabLabels.forEachIndexed { index, label ->
            val isSelected = selectedTab == index
            val activeColor = when (index) {
                0 -> MaterialTheme.colorScheme.primary
                1 -> SolidExpense
                2 -> SolidIncome
                3 -> SolidPrimary
                4 -> AmberGold
                else -> MaterialTheme.colorScheme.primary
            }

            Tab(
                selected = isSelected,
                onClick = { onTabSelected(index) },
                text = {
                    Text(
                        text = label,
                        fontSize = 13.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                    )
                },
                modifier = Modifier.testTag("budget_tab_$index")
            )
        }
    }
}

@Composable
private fun CopyPasteBudgetDialog(
    currentYear: Int,
    currentMonth: Int,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onConfirm: (fromYear: Int, fromMonth: Int, toYear: Int, toMonth: Int) -> Unit
) {
    var fromYear by remember { mutableIntStateOf(currentYear) }
    var fromMonth by remember {
        val prev = if (currentMonth > 1) currentMonth - 1 else 12
        mutableIntStateOf(prev)
    }
    var toYear by remember { mutableIntStateOf(currentYear) }
    var toMonth by remember { mutableIntStateOf(currentMonth) }
    var showConfirmPrompt by remember { mutableStateOf(false) }

    if (showConfirmPrompt) {
        AlertDialog(
            onDismissRequest = { showConfirmPrompt = false },
            title = {
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "কপি নিশ্চিত করুন" else "Confirm Budget Copy",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                val fromStr = DateUtils.formatMonthYear(fromYear, fromMonth, languageMode)
                val toStr = DateUtils.formatMonthYear(toYear, toMonth, languageMode)
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) {
                        "$fromStr এর বাজেট $toStr এ কপি এবং সংরক্ষণ করা হবে। এগিয়ে যেতে চান?"
                    } else {
                        "Budgets from $fromStr will overwrite existing targets for $toStr. Do you want to proceed?"
                    },
                    fontSize = 13.5.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmPrompt = false
                        onConfirm(fromYear, fromMonth, toYear, toMonth)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(if (languageMode == LanguageMode.BANGLA) "কপি ও সেভ" else "Confirm & Copy")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showConfirmPrompt = false }) {
                    Text(if (languageMode == LanguageMode.BANGLA) "বাতিল" else "Cancel")
                }
            }
        )
    } else {
        Dialog(onDismissRequest = onDismiss) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.ContentPaste,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "বাজেট কপি ও পেস্ট" else "Copy & Paste Budgets",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) {
                            "একটি নির্দিষ্ট মাসের বাজেট অন্য যেকোনো (অতীত বা ভবিষ্যৎ) মাসে কপি করুন:"
                        } else {
                            "Select source month to copy from and target month to apply to:"
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    HorizontalDivider()

                    // Source Month Selection
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "উৎস মাস (কপি হবে যেখান থেকে):" else "Source Month (Copy From):",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            IconButton(onClick = {
                                if (fromMonth > 1) fromMonth-- else { fromMonth = 12; fromYear-- }
                            }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Prev")
                            }
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = DateUtils.formatMonthYear(fromYear, fromMonth, languageMode),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 10.dp)
                                )
                            }
                            IconButton(onClick = {
                                if (fromMonth < 12) fromMonth++ else { fromMonth = 1; fromYear++ }
                            }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next")
                            }
                        }
                    }

                    // Target Month Selection
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "টার্গেট মাস (যেখানে প্রয়োগ হবে):" else "Target Month (Paste To):",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            IconButton(onClick = {
                                if (toMonth > 1) toMonth-- else { toMonth = 12; toYear-- }
                            }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Prev")
                            }
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = DateUtils.formatMonthYear(toYear, toMonth, languageMode),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 10.dp)
                                )
                            }
                            IconButton(onClick = {
                                if (toMonth < 12) toMonth++ else { toMonth = 1; toYear++ }
                            }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next")
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                    ) {
                        OutlinedButton(onClick = onDismiss) {
                            Text(if (languageMode == LanguageMode.BANGLA) "বাতিল" else "Cancel")
                        }
                        Button(
                            onClick = { showConfirmPrompt = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text(if (languageMode == LanguageMode.BANGLA) "কপি করুন" else "Copy Budgets")
                        }
                    }
                }
            }
        }
    }
}

/**
 * Helper to toggle a BudgetFilterOption in a set of active filters (Cumulative/AND logic).
 */
private fun toggleFilterOption(current: Set<BudgetFilterOption>, opt: BudgetFilterOption): Set<BudgetFilterOption> {
    return if (opt == BudgetFilterOption.ALL) {
        emptySet()
    } else {
        val updated = current.filter { it != BudgetFilterOption.ALL }.toMutableSet()
        if (updated.contains(opt)) {
            updated.remove(opt)
        } else {
            updated.add(opt)
        }
        updated
    }
}

/**
 * Modern, Compact Categories & Budget Entry Screen View.
 * Matches user's screenshot layout:
 * - Group Header: ➤ Group Name [ Aggregate Total ]
 * - Item Row:
 *   - Category Name e.g. MotorCycle (M&R) with Icon on left; 💰 Active Budget on right + Checkbox
 *   - Subline: Budget= 1000 • Actual= 20 • Manual= 1000
 *   - Action / Suggestion Checkmark Circles: [✓] Prev Month  [✓] Frequent 1  [✓] 3-Mo Avg  [✎ Calc]
 *   - Filter & Sort Toolbar for quick prioritization.
 */
@Composable
private fun CategoriesBudgetEntryView(
    title: String,
    items: List<BudgetTargetItem>,
    monthlyBudgets: List<MonthlyBudget>,
    allTransactions: List<TransactionWithDetails>,
    allAccounts: List<Account> = emptyList(),
    accountsWithBalances: List<AccountWithBalance> = emptyList(),
    selectedYear: Int,
    selectedMonth: Int,
    languageMode: LanguageMode,
    sectionColor: Color,
    isPeriodicFlow: Boolean,
    globalFrequency: BudgetFrequency,
    onGlobalFrequencyChange: (BudgetFrequency) -> Unit,
    totalBudgetAmount: Double,
    searchQuery: String,
    tabFilter: BudgetMakerTabFilter = BudgetMakerTabFilter(),
    selectedFilters: Set<BudgetFilterOption> = emptySet(),
    onFilterToggle: (BudgetFilterOption) -> Unit,
    selectedSort: BudgetSortOption,
    onSortChange: (BudgetSortOption) -> Unit,
    onMonthClick: () -> Unit,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onCopyPrevious: () -> Unit,
    onItemClick: ((BudgetTargetItem) -> Unit)? = null,
    onSaveBudget: (BudgetTargetItem, Double, Boolean) -> Unit,
    onSaveMultiple: (List<MonthlyBudget>) -> Unit
) {
    val budgetMap = remember(monthlyBudgets) {
        monthlyBudgets.associateBy { "${it.itemType}_${it.itemId}" }
    }

    // Account Balance Map from Balance Sheet (includes parents and children)
    val accountBalanceMap = remember(accountsWithBalances) {
        val map = mutableMapOf<Long, Double>()
        accountsWithBalances.forEach { parentWithBal ->
            map[parentWithBal.account.id] = parentWithBal.currentBalance
            parentWithBal.subAccounts.forEach { subWithBal ->
                map[subWithBal.account.id] = subWithBal.currentBalance
            }
        }
        map
    }

    // Calculate month boundary timestamps
    val startOfMonthMs = remember(selectedYear, selectedMonth) {
        DateUtils.getStartOfMonth(selectedYear, selectedMonth)
    }
    val endOfMonthMs = remember(selectedYear, selectedMonth) {
        DateUtils.getEndOfMonth(selectedYear, selectedMonth)
    }

    // Month 1st day balance calculation map for all accounts
    val firstDayBalanceMap = remember(allAccounts, allTransactions, startOfMonthMs) {
        val startDebitSums = mutableMapOf<Long, Double>()
        val startCreditSums = mutableMapOf<Long, Double>()
        for (item in allTransactions) {
            val tx = item.transaction
            if (tx.dateEpochMs < startOfMonthMs) {
                tx.debitAccountId?.let { id ->
                    startDebitSums[id] = (startDebitSums[id] ?: 0.0) + tx.amount
                }
                tx.creditAccountId?.let { id ->
                    startCreditSums[id] = (startCreditSums[id] ?: 0.0) + tx.amount
                }
            }
        }

        fun calcBalanceAtStart(acc: Account): Double {
            val dr = startDebitSums[acc.id] ?: 0.0
            val cr = startCreditSums[acc.id] ?: 0.0
            return when (acc.type) {
                AccountType.ASSET, AccountType.EXPENSE -> acc.initialBalance + (dr - cr)
                AccountType.LIABILITY -> -(acc.initialBalance + (cr - dr))
                AccountType.EQUITY, AccountType.INCOME -> acc.initialBalance + (cr - dr)
            }
        }

        val subAccountMap = allAccounts.filter { it.parentId != null }.groupBy { it.parentId!! }
        val map = mutableMapOf<Long, Double>()
        for (acc in allAccounts) {
            if (acc.parentId == null) {
                val subs = subAccountMap[acc.id] ?: emptyList()
                if (subs.isNotEmpty()) {
                    val total = calcBalanceAtStart(acc) + subs.sumOf { calcBalanceAtStart(it) }
                    map[acc.id] = total
                } else {
                    map[acc.id] = calcBalanceAtStart(acc)
                }
            } else {
                map[acc.id] = calcBalanceAtStart(acc)
            }
        }
        map
    }

    // 3 Months boundary for Active filter
    val threeMonthsAgoMs = remember(selectedYear, selectedMonth) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, selectedYear)
            set(Calendar.MONTH, selectedMonth - 1)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.MONTH, -2) // 3 months window
        }
        cal.timeInMillis
    }

    // Build Enhanced items with pre-calculated insights, actuals, and suggestions
    val enhancedItems = remember(items, monthlyBudgets, allTransactions, accountsWithBalances, firstDayBalanceMap, selectedYear, selectedMonth, threeMonthsAgoMs) {
        items.map { item ->
            val saved = budgetMap["${item.itemType}_${item.id}"]
            val currentAmt = saved?.budgetedAmount ?: item.defaultLimit
            val isEnabled = saved?.isEnabled ?: false

            val isAssetOrLiability = item.itemType == "ASSET" || item.itemType == "LIABILITY"
            val actualAmt: Double
            val firstDayBal: Double
            val txCount: Int

            if (isAssetOrLiability) {
                // For Assets & Liabilities, actual amount is the actual balance from the balance sheet
                actualAmt = accountBalanceMap[item.id] ?: 0.0
                firstDayBal = firstDayBalanceMap[item.id] ?: 0.0
                txCount = allTransactions.count {
                    it.transaction.debitAccountId == item.id || it.transaction.creditAccountId == item.id
                }
            } else {
                firstDayBal = 0.0
                // Calculate actual transactions for this specific item in current month
                val itemTxs = allTransactions.filter {
                    it.transaction.categoryId == item.id || it.transaction.subCategoryId == item.id ||
                    it.transaction.debitAccountId == item.id || it.transaction.creditAccountId == item.id
                }
                val currentMonthTxs = itemTxs.filter { it.transaction.dateEpochMs in startOfMonthMs..endOfMonthMs }
                actualAmt = currentMonthTxs.sumOf { it.transaction.amount }
                txCount = itemTxs.size
            }

            // Check transactions in last 3 months
            val has3MonthsTx = allTransactions.any {
                it.transaction.dateEpochMs in threeMonthsAgoMs..endOfMonthMs &&
                (it.transaction.categoryId == item.id || it.transaction.subCategoryId == item.id ||
                 it.transaction.debitAccountId == item.id || it.transaction.creditAccountId == item.id)
            }
            val is3MoActive = has3MonthsTx || (isAssetOrLiability && (actualAmt != 0.0 || txCount > 0))

            // Check if frequently budgeted in past months
            val pastBudgetCount = monthlyBudgets.count {
                it.itemId == item.id && it.itemType == item.itemType && it.budgetedAmount > 0.0
            }
            val isFreqBudgeted = pastBudgetCount > 0 || (saved?.budgetedAmount ?: 0.0) > 0.0
            val isFreqActive = txCount > 0 || (isAssetOrLiability && actualAmt != 0.0)

            val suggestions = calculateSuggestionsForItem(
                itemId = item.id,
                itemType = item.itemType,
                defaultLimit = item.defaultLimit,
                balanceSheetBalance = if (isAssetOrLiability) actualAmt else 0.0,
                firstDayBalance = if (isAssetOrLiability) firstDayBal else 0.0,
                allTransactions = allTransactions,
                selectedYear = selectedYear,
                selectedMonth = selectedMonth,
                monthlyBudgets = monthlyBudgets
            )

            EnhancedBudgetItem(
                item = item,
                currentBudget = currentAmt,
                isEnabled = isEnabled,
                actualSpent = actualAmt,
                manualBaseline = item.defaultLimit,
                txCount = txCount,
                isFrequentlyBudgeted = isFreqBudgeted,
                isFrequentlyActive = isFreqActive,
                is3MonthsActive = is3MoActive,
                firstDayBalance = firstDayBal,
                suggestions = suggestions,
                savedBudget = saved
            )
        }
    }

    val sectionItemType = items.firstOrNull()?.itemType ?: "EXPENSE"

    // Apply Filter & Search
    val filteredItems = remember(enhancedItems, searchQuery, selectedFilters, selectedSort, sectionItemType, tabFilter) {
        var list = enhancedItems

        // 1. Search Query
        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase()
            list = list.filter {
                it.item.nameEn.lowercase().contains(q) ||
                it.item.nameBn.lowercase().contains(q) ||
                it.item.groupName.lowercase().contains(q)
            }
        }

        // 2. Budget Maker Tab Filter - Category / Account selection
        if (tabFilter.selectedItemIds.isNotEmpty()) {
            list = list.filter { it.item.id in tabFilter.selectedItemIds }
        }
        if (tabFilter.selectedGroupNames.isNotEmpty()) {
            list = list.filter { it.item.groupName in tabFilter.selectedGroupNames }
        }

        // 3. Amount Range
        if (tabFilter.minAmount != null && tabFilter.minAmount > 0.0) {
            list = list.filter {
                if (sectionItemType == "ASSET" || sectionItemType == "LIABILITY") {
                    it.actualSpent >= tabFilter.minAmount || it.currentBudget >= tabFilter.minAmount
                } else {
                    it.currentBudget >= tabFilter.minAmount || it.actualSpent >= tabFilter.minAmount
                }
            }
        }
        if (tabFilter.maxAmount != null && tabFilter.maxAmount > 0.0) {
            list = list.filter {
                if (sectionItemType == "ASSET" || sectionItemType == "LIABILITY") {
                    it.actualSpent <= tabFilter.maxAmount || (it.currentBudget > 0 && it.currentBudget <= tabFilter.maxAmount)
                } else {
                    it.currentBudget <= tabFilter.maxAmount || (it.actualSpent > 0 && it.actualSpent <= tabFilter.maxAmount)
                }
            }
        }

        // 4. Status and Condition filters (cumulative AND logic)
        if (tabFilter.onlyWithBudgetOrTarget) {
            list = list.filter { it.currentBudget > 0.0 }
        }
        if (tabFilter.onlyWithoutBudgetOrTarget) {
            list = list.filter { it.currentBudget <= 0.0 }
        }
        if (tabFilter.onlyWithActualActivity) {
            list = list.filter {
                if (sectionItemType == "ASSET" || sectionItemType == "LIABILITY") {
                    it.actualSpent != 0.0 || it.txCount > 0
                } else {
                    it.actualSpent > 0.0
                }
            }
        }
        if (tabFilter.onlyZeroActivity) {
            list = list.filter {
                if (sectionItemType == "ASSET" || sectionItemType == "LIABILITY") {
                    it.actualSpent == 0.0 && it.txCount == 0
                } else {
                    it.actualSpent == 0.0
                }
            }
        }
        if (tabFilter.onlyOverBudget) {
            list = list.filter { it.actualSpent > it.currentBudget && it.currentBudget > 0.0 }
        }
        if (tabFilter.onlyUnderBudgetRemaining) {
            list = list.filter { it.currentBudget > it.actualSpent && it.currentBudget > 0.0 }
        }
        if (tabFilter.onlyTargetAchieved) {
            list = list.filter { it.actualSpent >= it.currentBudget && it.currentBudget > 0.0 }
        }
        if (tabFilter.onlyTargetPending) {
            list = list.filter { it.actualSpent < it.currentBudget }
        }
        if (tabFilter.onlyWithSuggestions) {
            list = list.filter { it.suggestions.isNotEmpty() }
        }
        if (tabFilter.onlyPositiveBalance) {
            list = list.filter { it.actualSpent > 0.0 }
        }
        if (tabFilter.onlyZeroBalance) {
            list = list.filter { it.actualSpent == 0.0 }
        }
        if (tabFilter.onlyNegativeBalance) {
            list = list.filter { it.actualSpent < 0.0 }
        }
        if (tabFilter.onlyOutstandingDebt) {
            list = list.filter { it.actualSpent > 0.0 }
        }
        if (tabFilter.onlyClearedDebt) {
            list = list.filter { it.actualSpent == 0.0 }
        }
        if (tabFilter.excludeZeroAmounts) {
            list = list.filter { it.currentBudget > 0.0 || it.actualSpent != 0.0 || it.txCount > 0 }
        }

        // 5. Filter Category Selection (Cumulative/AND logic across multiple active chips)
        if (selectedFilters.isNotEmpty() && !selectedFilters.contains(BudgetFilterOption.ALL)) {
            if (BudgetFilterOption.ONLY_REMAINING in selectedFilters) {
                list = list.filter {
                    it.currentBudget > 0.0 && (it.currentBudget - it.actualSpent) > 0.0
                }
            }
            if (BudgetFilterOption.BUDGETED_ONLY in selectedFilters) {
                list = list.filter { it.currentBudget > 0.0 }
            }
            if (BudgetFilterOption.UNBUDGETED in selectedFilters) {
                list = list.filter { it.currentBudget <= 0.0 }
            }
            if (BudgetFilterOption.ACTIVE_ONLY in selectedFilters) {
                list = when (sectionItemType) {
                    "EXPENSE", "INCOME" -> list.filter { it.actualSpent > 0.0 }
                    "ASSET", "LIABILITY" -> list.filter { it.actualSpent != 0.0 || it.txCount > 0 }
                    else -> list.filter { it.actualSpent > 0.0 }
                }
            }
            if (BudgetFilterOption.ACTIVE_3_MONTHS in selectedFilters) {
                list = list.filter { it.is3MonthsActive }
            }
            if (BudgetFilterOption.FREQ_BUDGETED in selectedFilters) {
                list = list.filter { it.isFrequentlyBudgeted }
            }
            if (BudgetFilterOption.FREQ_ACTIVE in selectedFilters) {
                list = list.filter { it.isFrequentlyActive }
            }
        }

        // 6. Sorting
        when (selectedSort) {
            BudgetSortOption.DEFAULT -> list
            BudgetSortOption.BUDGET_DESC -> list.sortedByDescending { it.currentBudget }
            BudgetSortOption.BUDGET_ASC -> list.sortedBy { it.currentBudget }
            BudgetSortOption.ACTUAL_DESC -> list.sortedByDescending { it.actualSpent }
            BudgetSortOption.FREQUENCY_DESC -> list.sortedByDescending { it.txCount }
            BudgetSortOption.NAME_ASC -> list.sortedBy { it.item.nameEn.lowercase() }
        }
    }

    // Group items by parent group name
    val groupedItems = remember(filteredItems, selectedSort, tabFilter.hideEmptyGroups) {
        val groups = filteredItems.groupBy { it.item.groupName }
        if (tabFilter.hideEmptyGroups) {
            groups.filter { entry -> entry.value.any { it.currentBudget > 0.0 || it.actualSpent != 0.0 } }
        } else {
            groups
        }
    }

    var showGlobalFreqDropdown by remember { mutableStateOf(false) }
    var showSortDropdown by remember { mutableStateOf(false) }

    val displayedTotal = if (isPeriodicFlow) {
        globalFrequency.fromMonthly(totalBudgetAmount)
    } else {
        totalBudgetAmount
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Modern, Compact Header Card (Month is already in top row)
        Surface(
            tonalElevation = 2.dp,
            shadowElevation = 1.dp,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 7.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Tier 1: Frequency Selector / Target Balances (Left) & Total Target + Quick Actions (Right)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isPeriodicFlow) {
                        Box {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { showGlobalFreqDropdown = true }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = globalFrequency.localizedName(languageMode),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Icon(
                                        Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Select Frequency",
                                        modifier = Modifier.size(15.dp),
                                        tint = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = showGlobalFreqDropdown,
                                onDismissRequest = { showGlobalFreqDropdown = false }
                            ) {
                                BudgetFrequency.values().forEach { freq ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = freq.localizedName(languageMode),
                                                fontWeight = if (freq == globalFrequency) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 13.sp
                                            )
                                        },
                                        onClick = {
                                            onGlobalFrequencyChange(freq)
                                            showGlobalFreqDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "টার্গেট ব্যালেন্স" else "Target Balances",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }

                    // Total Target Display and Action Icons
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "মোট:" else "Total:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Text(
                                text = LanguageHelper.formatCurrency(displayedTotal, languageMode),
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = sectionColor,
                                maxLines = 1
                            )
                        }

                        // Quick copy previous month
                        IconButton(
                            onClick = onCopyPrevious,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Default.ContentCopy,
                                contentDescription = "Copy from Previous Month",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Tier 2: Filter & Sort Bar (Horizontal Chips & Sort Selector)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Filter Chips Scrollable Row (Grouped by type of similarity)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        BudgetFilterGroup.values().forEach { group ->
                            val optionsInGroup = BudgetFilterOption.values().filter { it.group == group }
                            val hasSelectedInGroup = optionsInGroup.any { opt ->
                                if (opt == BudgetFilterOption.ALL) (selectedFilters.isEmpty() || BudgetFilterOption.ALL in selectedFilters) else opt in selectedFilters
                            }

                            item(key = "group_header_${group.name}") {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (hasSelectedInGroup) sectionColor.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.padding(end = 2.dp)
                                ) {
                                    Text(
                                        text = group.getTitle(languageMode),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (hasSelectedInGroup) sectionColor else MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            items(optionsInGroup, key = { "filter_opt_${it.name}" }) { opt ->
                                val isSelected = if (opt == BudgetFilterOption.ALL) {
                                    selectedFilters.isEmpty() || BudgetFilterOption.ALL in selectedFilters
                                } else {
                                    opt in selectedFilters
                                }
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onFilterToggle(opt) },
                                    label = {
                                        Text(
                                            text = opt.getTitle(sectionItemType, languageMode),
                                            fontSize = 11.5.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = sectionColor.copy(alpha = 0.15f),
                                        selectedLabelColor = sectionColor,
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    border = BorderStroke(
                                        0.6.dp,
                                        if (isSelected) sectionColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                    ),
                                    modifier = Modifier.height(30.dp)
                                )
                            }

                            item(key = "group_sep_${group.name}") {
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Sort Dropdown Button
                    Box {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            border = BorderStroke(0.6.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showSortDropdown = true }
                                .padding(horizontal = 7.dp, vertical = 5.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sort,
                                    contentDescription = "Sort",
                                    tint = sectionColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = if (selectedSort == BudgetSortOption.DEFAULT) {
                                        if (languageMode == LanguageMode.BANGLA) "সাজান" else "Sort"
                                    } else {
                                        selectedSort.getTitle(sectionItemType, languageMode).take(12)
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showSortDropdown,
                            onDismissRequest = { showSortDropdown = false }
                        ) {
                            BudgetSortOption.values().forEach { sortOpt ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = sortOpt.getTitle(sectionItemType, languageMode),
                                            fontWeight = if (sortOpt == selectedSort) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 12.5.sp,
                                            color = if (sortOpt == selectedSort) sectionColor else MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    onClick = {
                                        onSortChange(sortOpt)
                                        showSortDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.30f))

        // Empty state when search or filter matches nothing
        if (filteredItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "কোনো তথ্য পাওয়া যায়নি" else "No items match the active filter",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "ফিল্টার পরিবর্তন করুন বা সার্চ মুছুন" else "Try switching filters or clearing the search query",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Button(
                        onClick = {
                            onFilterToggle(BudgetFilterOption.ALL)
                            onSortChange(BudgetSortOption.DEFAULT)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = sectionColor)
                    ) {
                        Text(if (languageMode == LanguageMode.BANGLA) "ফিল্টার রিসেট করুন" else "Reset Filters")
                    }
                }
            }
        } else {
            // Categories & Items List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("budget_entry_list_$title"),
                contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (BudgetFilterOption.ONLY_CATEGORIES in selectedFilters) {
                    // Only Categories / Accounts view: flat list without group headers
                    items(filteredItems, key = { "flat_item_${it.item.id}" }) { enhancedItem ->
                        BudgetItemRow(
                            enhancedItem = enhancedItem,
                            isPeriodicFlow = isPeriodicFlow,
                            globalFrequency = globalFrequency,
                            languageMode = languageMode,
                            sectionColor = sectionColor,
                            onItemClick = onItemClick,
                            onSaveBudget = { amtMonthly, isEnabled ->
                                onSaveBudget(enhancedItem.item, amtMonthly, isEnabled)
                            }
                        )
                    }
                } else {
                    groupedItems.forEach { (groupName, catEnhancedList) ->
                        // Group Header Card: ➤ Group Name [ 1,000 ]
                        item(key = "group_$groupName") {
                            val allGroupEnabled = catEnhancedList.isNotEmpty() && catEnhancedList.all { it.isEnabled }

                            val groupSelectedMonthlyTotal = catEnhancedList.filter { it.isEnabled }.sumOf { it.currentBudget }

                            val displayedGroupTotal = if (isPeriodicFlow) {
                                globalFrequency.fromMonthly(groupSelectedMonthlyTotal)
                            } else {
                                groupSelectedMonthlyTotal
                            }

                            val firstItem = catEnhancedList.firstOrNull()?.item
                            val groupIcon = firstItem?.groupIconName ?: "Category"
                            val groupColor = firstItem?.groupColorHex ?: "#6B7280"

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.50f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp, bottom = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Left: Group Icon + Group Title + Count
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.weight(1f, fill = false)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = IconHelper.parseColorHex(groupColor).copy(alpha = 0.16f),
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                IconHelper.AppIcon(
                                                    iconName = groupIcon,
                                                    contentDescription = groupName,
                                                    tint = IconHelper.parseColorHex(groupColor),
                                                    modifier = Modifier.size(15.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = groupName,
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    // Right: Group Total Pill + Checkbox
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                            border = BorderStroke(0.6.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                            modifier = Modifier.padding(vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = formatCompactCurrency(displayedGroupTotal, languageMode),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                                maxLines = 1
                                            )
                                        }

                                        Checkbox(
                                            checked = allGroupEnabled,
                                            onCheckedChange = { _ ->
                                                val newEnabledState = !allGroupEnabled
                                                val updatedBudgets = catEnhancedList.map { item ->
                                                    MonthlyBudget(
                                                        year = selectedYear,
                                                        month = selectedMonth,
                                                        itemType = item.item.itemType,
                                                        itemId = item.item.id,
                                                        budgetedAmount = item.currentBudget,
                                                        isEnabled = newEnabledState,
                                                        updatedAt = System.currentTimeMillis()
                                                    )
                                                }
                                                onSaveMultiple(updatedBudgets)
                                            },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = sectionColor,
                                                uncheckedColor = MaterialTheme.colorScheme.outline
                                            ),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Category Items (unless ONLY_GROUPS filter is selected)
                        if (BudgetFilterOption.ONLY_GROUPS !in selectedFilters) {
                            items(catEnhancedList, key = { "item_${it.item.id}" }) { enhancedItem ->
                                BudgetItemRow(
                                    enhancedItem = enhancedItem,
                                    isPeriodicFlow = isPeriodicFlow,
                                    globalFrequency = globalFrequency,
                                    languageMode = languageMode,
                                    sectionColor = sectionColor,
                                    onItemClick = onItemClick,
                                    onSaveBudget = { amtMonthly, isEnabled ->
                                        onSaveBudget(enhancedItem.item, amtMonthly, isEnabled)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Redesigned Individual Budget Item Row matching user screenshot:
 *
 * Tier 1 (Identity & Active Amount):
 * - Category Name e.g. "MotorCycle (M&R)" on left.
 * - Active budget amount with wallet icon e.g. "৳ 1,000" (or 0) in bold theme color on right.
 * - Enable/Disable checkbox.
 *
 * Tier 2 (Insights Subline):
 * - "Budget= 1000 • Actual= 20 • Manual= 1000" (or "Actual (Balance Sheet)= ৳... • Target= ৳...")
 *
 * Tier 3 (Quick Suggestions & Actions):
 * - Direct Suggestion Amount Buttons: [Prev] [Freq] [Avg] or [Actual] [Prev]
 * - Manual calculator edit button (opens main app PopupCalculatorDialog).
 */
@Composable
private fun BudgetItemRow(
    enhancedItem: EnhancedBudgetItem,
    isPeriodicFlow: Boolean,
    globalFrequency: BudgetFrequency,
    languageMode: LanguageMode,
    sectionColor: Color,
    onItemClick: ((BudgetTargetItem) -> Unit)? = null,
    onSaveBudget: (amountMonthly: Double, isEnabled: Boolean) -> Unit
) {
    val item = enhancedItem.item
    val savedBudget = enhancedItem.savedBudget
    val suggestions = enhancedItem.suggestions

    var itemFrequency by remember(globalFrequency) { mutableStateOf(globalFrequency) }
    var showFreqDropdown by remember { mutableStateOf(false) }
    var showPopupCalculator by remember { mutableStateOf(false) }

    val isEnabled = enhancedItem.isEnabled
    val currentMonthlyAmt = enhancedItem.currentBudget
    val actualSpent = enhancedItem.actualSpent
    val manualBaseline = enhancedItem.manualBaseline

    val displayedCurrentAmt = if (isPeriodicFlow) {
        itemFrequency.fromMonthly(currentMonthlyAmt)
    } else {
        currentMonthlyAmt
    }

    // Determine active suggestion index
    val activeIndex = remember(currentMonthlyAmt, suggestions) {
        val found = suggestions.indexOfFirst { kotlin.math.abs(currentMonthlyAmt - it.amountMonthly) < 0.5 }
        if (found >= 0) found else -1
    }

    val parsedColor = remember(item.colorHex) {
        try {
            IconHelper.parseColorHex(item.colorHex)
        } catch (_: Exception) {
            sectionColor
        }
    }

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(0.7.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp)
            .testTag("budget_item_${item.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // TIER 1: Category Name on left, Active Budget Amount + Checkbox on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Icon + Name
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onItemClick?.invoke(item) }
                ) {
                    Surface(
                        shape = CircleShape,
                        color = parsedColor.copy(alpha = 0.12f),
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = IconHelper.getIconByName(item.iconName),
                                contentDescription = null,
                                tint = parsedColor,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }

                    Text(
                        text = LanguageHelper.getLocalizedName(item.nameEn, item.nameBn, languageMode),
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        color = if (isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Right: Active Budget Display (with wallet icon) + Checkbox
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Clickable Active Amount
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { showPopupCalculator = true }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        if (displayedCurrentAmt > 0) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = BrandBlue,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = formatCompactCurrency(displayedCurrentAmt, languageMode),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = BrandBlue
                            )
                        } else {
                            Text(
                                text = "0",
                                fontWeight = FontWeight.Normal,
                                fontSize = 13.5.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    Checkbox(
                        checked = isEnabled,
                        onCheckedChange = { newState ->
                            onSaveBudget(currentMonthlyAmt, newState)
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = if (item.itemType == "EXPENSE") SolidExpense else sectionColor,
                            uncheckedColor = MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // TIER 2: Insights Subtitle (Type-specific contextual naming)
            val sublineText = when (item.itemType) {
                "INCOME" -> buildString {
                    if (currentMonthlyAmt > 0) {
                        append("Target= ${formatCompactCurrency(currentMonthlyAmt, languageMode)} • ")
                    }
                    append("Earned= ${formatCompactCurrency(actualSpent, languageMode)}")
                    if (manualBaseline > 0 && manualBaseline != currentMonthlyAmt) {
                        append(" • Default= ${formatCompactCurrency(manualBaseline, languageMode)}")
                    }
                }
                "EXPENSE" -> buildString {
                    if (currentMonthlyAmt > 0) {
                        append("Budget= ${formatCompactCurrency(currentMonthlyAmt, languageMode)} • ")
                    }
                    append("Spent= ${formatCompactCurrency(actualSpent, languageMode)}")
                    if (manualBaseline > 0 && manualBaseline != currentMonthlyAmt) {
                        append(" • Limit= ${formatCompactCurrency(manualBaseline, languageMode)}")
                    }
                }
                "ASSET" -> buildString {
                    append("Actual= ${formatCompactCurrency(actualSpent, languageMode)}")
                    if (enhancedItem.firstDayBalance != 0.0) {
                        append(" • 1st Day= ${formatCompactCurrency(enhancedItem.firstDayBalance, languageMode)}")
                    }
                    if (currentMonthlyAmt > 0) {
                        append(" • Target= ${formatCompactCurrency(currentMonthlyAmt, languageMode)}")
                    }
                }
                "LIABILITY" -> buildString {
                    append("Actual= ${formatCompactCurrency(actualSpent, languageMode)}")
                    if (enhancedItem.firstDayBalance != 0.0) {
                        append(" • 1st Day= ${formatCompactCurrency(enhancedItem.firstDayBalance, languageMode)}")
                    }
                    if (currentMonthlyAmt > 0) {
                        append(" • Target= ${formatCompactCurrency(currentMonthlyAmt, languageMode)}")
                    }
                }
                else -> buildString {
                    if (currentMonthlyAmt > 0) {
                        append("Budget= ${formatCompactCurrency(currentMonthlyAmt, languageMode)} • ")
                    }
                    append("Actual= ${formatCompactCurrency(actualSpent, languageMode)}")
                }
            }

            Text(
                text = sublineText,
                fontSize = 11.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // TIER 3: Direct Amount Suggestion Buttons + Manual Option (Horizontally Swipeable)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Frequency selector pill (For periodic flows) or Asset/Liability label
                if (isPeriodicFlow) {
                    Box(modifier = Modifier.padding(end = 4.dp)) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            border = BorderStroke(0.6.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { showFreqDropdown = true }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.5.dp)
                            ) {
                                Text(
                                    text = itemFrequency.localizedName(languageMode),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                                Icon(
                                    Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    modifier = Modifier.size(11.dp),
                                    tint = MaterialTheme.colorScheme.outline
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showFreqDropdown,
                            onDismissRequest = { showFreqDropdown = false }
                        ) {
                            BudgetFrequency.values().forEach { freq ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = freq.localizedName(languageMode),
                                            fontWeight = if (freq == itemFrequency) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 12.sp
                                        )
                                    },
                                    onClick = {
                                        itemFrequency = freq
                                        showFreqDropdown = false
                                    }
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        text = if (item.itemType == "ASSET") "Asset" else "Liability",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }

                // Right: Horizontally Swipeable Amount Suggestion Buttons & Manual Option
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    suggestions.forEach { sugg ->
                        val isSuggestionActive = kotlin.math.abs(currentMonthlyAmt - sugg.amountMonthly) < 0.5 && (currentMonthlyAmt > 0.0 || (sugg.amountMonthly == 0.0 && currentMonthlyAmt == 0.0 && isEnabled))
                        val formattedAmount = formatCompactCurrency(sugg.amountMonthly, languageMode)

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSuggestionActive) sectionColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.50f),
                            border = BorderStroke(
                                0.7.dp,
                                if (isSuggestionActive) sectionColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable {
                                    onSaveBudget(sugg.amountMonthly, true)
                                }
                                .testTag("sugg_btn_${item.id}_${sugg.index}")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                modifier = Modifier.padding(horizontal = 4.5.dp, vertical = 2.5.dp)
                            ) {
                                if (sugg.label.isNotEmpty()) {
                                    Text(
                                        text = "${sugg.label}:",
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSuggestionActive) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.outline
                                    )
                                }
                                Text(
                                    text = formattedAmount,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSuggestionActive) FontWeight.ExtraBold else FontWeight.SemiBold,
                                    color = if (isSuggestionActive) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // Manual Option Button
                    val isManualCustom = activeIndex == -1 && currentMonthlyAmt > 0.0
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isManualCustom) sectionColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        border = BorderStroke(
                            0.7.dp,
                            if (isManualCustom) sectionColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { showPopupCalculator = true }
                            .testTag("manual_entry_btn_${item.id}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            modifier = Modifier.padding(horizontal = 4.5.dp, vertical = 2.5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Manual Option",
                                tint = if (isManualCustom) sectionColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(10.dp)
                            )
                            Text(
                                text = "Manual",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isManualCustom) sectionColor else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    // Main App Popup Calculator Dialog
    if (showPopupCalculator) {
        PopupCalculatorDialog(
            initialValue = displayedCurrentAmt,
            languageMode = languageMode,
            onDismiss = { showPopupCalculator = false },
            onValueConfirmed = { enteredAmt ->
                val monthlyToSave = if (isPeriodicFlow) {
                    itemFrequency.toMonthly(enteredAmt)
                } else {
                    enteredAmt
                }
                onSaveBudget(monthlyToSave, true)
                showPopupCalculator = false
            }
        )
    }
}

/**
 * Informative Guide Dialog.
 */
@Composable
private fun BudgetHelpDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Budget Maker & Insights Guide",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BrandBlue
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider()

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "• Quick Suggestions Checkmarks:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Tap any checkmark circle to instantly set your budget based on Previous Month actuals, Frequent spend patterns, or 3-Month averages.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "• Smart Filters & Sorting:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Filter by Frequently Budgeted or Frequently Expensed categories, or sort by Budget Amount and Transaction Frequency for effortless planning.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "• Frequency Conversions:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Set budgets as Weekly, Bi-weekly, Monthly, Quarterly, or Yearly. All amounts are automatically converted and saved to standard monthly figures.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(onClick = onDismiss) {
                        Text("Got It")
                    }
                }
            }
        }
    }
}

/**
 * Calculates smart budget suggestions for any item:
 * 1. Previous Month Budget (PB)
 * 2. Previous Month Expensed / Actual (PE)
 * 3. 3 Frequent suggestions (F1, F2, F3)
 */
internal fun calculateSuggestionsForItem(
    itemId: Long,
    itemType: String,
    defaultLimit: Double,
    balanceSheetBalance: Double = 0.0,
    firstDayBalance: Double = 0.0,
    allTransactions: List<TransactionWithDetails>,
    selectedYear: Int,
    selectedMonth: Int,
    monthlyBudgets: List<MonthlyBudget>
): List<BudgetSuggestionOption> {
    var prevYear = selectedYear
    var prevMonth = selectedMonth - 1
    if (prevMonth < 1) {
        prevMonth = 12
        prevYear -= 1
    }

    val prevSaved = monthlyBudgets.find { it.year == prevYear && it.month == prevMonth && it.itemType == itemType && it.itemId == itemId }
    val prevBudgetAmt = prevSaved?.budgetedAmount ?: 0.0

    val prevMonthStart = DateUtils.getStartOfMonth(prevYear, prevMonth)
    val prevMonthEnd = DateUtils.getEndOfMonth(prevYear, prevMonth)

    val prevMonthTxs = allTransactions.filter {
        it.transaction.dateEpochMs in prevMonthStart..prevMonthEnd &&
                (it.transaction.categoryId == itemId || it.transaction.subCategoryId == itemId ||
                 it.transaction.debitAccountId == itemId || it.transaction.creditAccountId == itemId)
    }
    val prevExpensedAmt = prevMonthTxs.sumOf { it.transaction.amount }

    val allItemTxs = allTransactions.filter {
        it.transaction.categoryId == itemId || it.transaction.subCategoryId == itemId ||
                it.transaction.debitAccountId == itemId || it.transaction.creditAccountId == itemId
    }

    val list = mutableListOf<BudgetSuggestionOption>()
    var nextIndex = 0

    if (itemType == "ASSET" || itemType == "LIABILITY") {
        // ASSETS & LIABILITIES: Show Actual Amount and 1st Day balance from balance sheet as suggestions
        if (balanceSheetBalance != 0.0) {
            list.add(BudgetSuggestionOption(nextIndex++, balanceSheetBalance, "Actual", "Actual"))
        }
        if (firstDayBalance != 0.0 && firstDayBalance != balanceSheetBalance) {
            list.add(BudgetSuggestionOption(nextIndex++, firstDayBalance, "1st Day", "1st Day"))
        }
        return list
    }

    // EXPENSES & INCOMES: Previous Month Budget (PB), Previous Month Expensed (PE), Category Default Limit (Limit)
    if (prevBudgetAmt > 0.0) {
        list.add(BudgetSuggestionOption(nextIndex++, prevBudgetAmt, "Previous Budget", "PB"))
    }
    if (prevExpensedAmt > 0.0) {
        list.add(BudgetSuggestionOption(nextIndex++, prevExpensedAmt, "Previous Expensed", "PE"))
    }
    if (defaultLimit > 0.0 && defaultLimit != prevBudgetAmt) {
        list.add(BudgetSuggestionOption(nextIndex++, defaultLimit, "Default Limit", "Limit"))
    }

    // Only compute frequent amounts if REAL transactions exist!
    if (allItemTxs.isNotEmpty()) {
        val cal = Calendar.getInstance()
        val monthlyTotals = allItemTxs.groupBy {
            cal.timeInMillis = it.transaction.dateEpochMs
            "${cal.get(Calendar.YEAR)}_${cal.get(Calendar.MONTH)}"
        }.values.map { txList -> txList.sumOf { it.transaction.amount } }.filter { it > 0.0 }

        val freqMonthlyTotals = monthlyTotals.groupingBy { it }.eachCount().toList().sortedByDescending { it.second }.map { it.first }

        // Also check distinct individual transaction amounts if monthly totals are limited
        val individualAmounts = allItemTxs.map { it.transaction.amount }.filter { it > 0.0 }
        val freqIndividualAmounts = individualAmounts.groupingBy { it }.eachCount().toList().sortedByDescending { it.second }.map { it.first }

        val existingAmounts = list.map { it.amountMonthly }.toMutableSet()
        val topFreqs = (freqMonthlyTotals + freqIndividualAmounts).distinct().filter { it > 0.0 && it !in existingAmounts }.take(3)

        topFreqs.forEachIndexed { fIdx, amt ->
            list.add(BudgetSuggestionOption(nextIndex++, amt, "Frequent ${fIdx + 1}", "F${fIdx + 1}"))
            existingAmounts.add(amt)
        }
    }

    return list
}

/**
 * Clean compact currency formatting.
 */
private fun formatCompactCurrency(amount: Double, languageMode: LanguageMode): String {
    return if (amount % 1.0 == 0.0) {
        val whole = amount.toLong().toString()
        val numStr = if (languageMode == LanguageMode.BANGLA) LanguageHelper.toBanglaDigits(whole) else whole
        "৳$numStr"
    } else {
        LanguageHelper.formatCurrency(amount, languageMode)
    }
}

/* -------------------------------------------------------------
   TAB 0: BM DASHBOARD & INSIGHTS
   ------------------------------------------------------------- */
@Composable
private fun BudgetDashboardView(
    year: Int,
    month: Int,
    languageMode: LanguageMode,
    totalExpenses: Double,
    totalLiabilities: Double,
    totalOutflows: Double,
    totalIncomes: Double,
    totalAssets: Double,
    totalInflows: Double,
    runningExpendable: Double,
    onNavigateToTab: (Int) -> Unit,
    onRunningExpendableClick: () -> Unit = {}
) {
    var includeRunningExpendable by rememberSaveable { mutableStateOf(false) }

    // Formula: assets + incomes - expenses - liabilities (+ expendable if toggled on)
    val baseSurplus = (totalAssets + totalIncomes) - (totalExpenses + totalLiabilities)
    val finalSurplus = if (includeRunningExpendable) baseSurplus + runningExpendable else baseSurplus
    val isSurplus = finalSurplus >= 0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("budget_dashboard_view"),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Top KPI Banner Header (Without Actual switcher)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "বাজেট ড্যাশবোর্ড ও অন্তর্দৃষ্টি" else "Budget Overview & Analytics",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = DateUtils.formatMonthYear(year, month, languageMode),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (includeRunningExpendable) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, if (includeRunningExpendable) MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "ব্যয়যোগ্য" else "Expendable",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (includeRunningExpendable) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Switch(
                            checked = includeRunningExpendable,
                            onCheckedChange = { includeRunningExpendable = it },
                            modifier = Modifier.testTag("header_toggle_running_expendable"),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onSecondary,
                                checkedTrackColor = MaterialTheme.colorScheme.secondary
                            )
                        )
                    }
                }
            }
        }

        // Summary KPI Card
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (includeRunningExpendable) {
                                    if (languageMode == LanguageMode.BANGLA) "পরিকল্পিত ব্যালেন্স (ব্যয়যোগ্য সহ)" else "Planned Balance (with Expendable)"
                                } else {
                                    if (languageMode == LanguageMode.BANGLA) "পরিকল্পিত উদ্বৃত্ত / ঘাটতি" else "Planned Surplus / Deficit"
                                },
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.outline,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = (if (isSurplus) "+" else "") + LanguageHelper.formatCurrency(finalSurplus, languageMode),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isSurplus) SolidIncome else SolidExpense
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = (if (isSurplus) SolidIncome else SolidExpense).copy(alpha = 0.14f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isSurplus) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                                    contentDescription = null,
                                    tint = if (isSurplus) SolidIncome else SolidExpense,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Formula Breakdown Banner
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (includeRunningExpendable) {
                                "Assets (${LanguageHelper.formatCurrency(totalAssets, languageMode)}) + Incomes (${LanguageHelper.formatCurrency(totalIncomes, languageMode)}) − Expenses (${LanguageHelper.formatCurrency(totalExpenses, languageMode)}) − Liabilities (${LanguageHelper.formatCurrency(totalLiabilities, languageMode)}) + Expendable (${LanguageHelper.formatCurrency(runningExpendable, languageMode)})"
                            } else {
                                "Assets (${LanguageHelper.formatCurrency(totalAssets, languageMode)}) + Incomes (${LanguageHelper.formatCurrency(totalIncomes, languageMode)}) − Expenses (${LanguageHelper.formatCurrency(totalExpenses, languageMode)}) − Liabilities (${LanguageHelper.formatCurrency(totalLiabilities, languageMode)})"
                            },
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Inflows vs Outflows Split
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "মোট পরিকল্পিত আন্তঃপ্রবাহ" else "Total Target Inflows",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Text(
                                text = LanguageHelper.formatCurrency(totalInflows, languageMode),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.5.sp,
                                color = SolidIncome
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "মোট পরিকল্পিত বহিঃপ্রবাহ" else "Total Target Outflows",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Text(
                                text = LanguageHelper.formatCurrency(totalOutflows, languageMode),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.5.sp,
                                color = SolidExpense
                            )
                        }
                    }
                }
            }
        }

        // Section Title: Budget Allocation Options
        item {
            Text(
                text = if (languageMode == LanguageMode.BANGLA) "বাজেট বিভাজন ও বিকল্প" else "Budget Allocation & Options",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Row 1: Expenses & Incomes Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Expenses Card (Tab 1)
                CategorySummaryCard(
                    title = if (languageMode == LanguageMode.BANGLA) "ব্যয়" else "Expenses",
                    amount = totalExpenses,
                    color = SolidExpense,
                    icon = Icons.Default.RemoveCircleOutline,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToTab(1) },
                    languageMode = languageMode
                )

                // Incomes Card (Tab 2)
                CategorySummaryCard(
                    title = if (languageMode == LanguageMode.BANGLA) "আয়" else "Incomes",
                    amount = totalIncomes,
                    color = SolidIncome,
                    icon = Icons.Default.AddCircleOutline,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToTab(2) },
                    languageMode = languageMode
                )
            }
        }

        // Row 2: Assets & Liabilities Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Assets Card (Tab 3)
                CategorySummaryCard(
                    title = if (languageMode == LanguageMode.BANGLA) "সম্পদ" else "Assets",
                    amount = totalAssets,
                    color = SolidPrimary,
                    icon = Icons.Default.AccountBalance,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToTab(3) },
                    languageMode = languageMode
                )

                // Liabilities Card (Tab 4)
                CategorySummaryCard(
                    title = if (languageMode == LanguageMode.BANGLA) "দায়" else "Liabilities",
                    amount = totalLiabilities,
                    color = AmberGold,
                    icon = Icons.Default.CreditCard,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToTab(4) },
                    languageMode = languageMode
                )
            }
        }

        // Option 5: Running Expendable Card with Switch
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (includeRunningExpendable) {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (includeRunningExpendable) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                    } else {
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                    }
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onRunningExpendableClick() }
                    .testTag("card_running_expendable")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (includeRunningExpendable) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = if (includeRunningExpendable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "চলমান ব্যয়যোগ্য অর্থ" else "Running Expendable",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (includeRunningExpendable) {
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    }
                                ) {
                                    Text(
                                        text = if (includeRunningExpendable) {
                                            if (languageMode == LanguageMode.BANGLA) "যোগ হচ্ছে (+)" else "Added (+)"
                                        } else {
                                            if (languageMode == LanguageMode.BANGLA) "বাদ" else "Excluded"
                                        },
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (includeRunningExpendable) {
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.outline
                                        },
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = LanguageHelper.formatCurrency(runningExpendable, languageMode),
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (runningExpendable >= 0) SolidIncome else SolidExpense
                            )
                            Text(
                                text = if (includeRunningExpendable) {
                                    if (languageMode == LanguageMode.BANGLA) "বাজেট সূত্রে যোগ করা হয়েছে" else "Included in planned balance formula"
                                } else {
                                    if (languageMode == LanguageMode.BANGLA) "বাজেট হিসাব থেকে বাদ দেওয়া হয়েছে" else "Excluded from formula calculation"
                                },
                                fontSize = 10.5.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    Switch(
                        checked = includeRunningExpendable,
                        onCheckedChange = { includeRunningExpendable = it },
                        modifier = Modifier.testTag("switch_running_expendable"),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }
        }

        // Actionable Insights Card
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                ),
                border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = AmberGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "স্মার্ট বাজেট পরামর্শ ও সূত্র" else "Smart Budget Formula & Insights",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) {
                            "• বর্তমান সূত্র: সম্পদ + আয় − ব্যয় − দায়" + (if (includeRunningExpendable) " + চলমান ব্যয়যোগ্য" else "")
                        } else {
                            "• Current Formula: Assets + Incomes − Expenses − Liabilities" + (if (includeRunningExpendable) " + Running Expendable" else "")
                        },
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "• Swipe horizontally across suggestion pills (PB, PE, F1/F2/F3) to set your plan instantly.",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "• Tap 'Manual' on any item to open the calculator and specify a custom budget amount.",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun CategorySummaryCard(
    title: String,
    amount: Double,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    languageMode: LanguageMode
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = color.copy(alpha = 0.12f),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                    }
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Go",
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(14.dp)
                )
            }

            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = LanguageHelper.formatCurrency(amount, languageMode),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )

            Text(
                text = if (languageMode == LanguageMode.BANGLA) "বাজেট লক্ষ্য" else "Budget Target",
                fontSize = 10.5.sp,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

/**
 * Clean Month & Year Picker Dialog.
 */
@Composable
private fun MonthYearPickerDialog(
    currentYear: Int,
    currentMonth: Int,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onConfirm: (year: Int, month: Int) -> Unit
) {
    var selectedYear by remember { mutableIntStateOf(currentYear) }
    var selectedMonth by remember { mutableIntStateOf(currentMonth) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Select Budget Period",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Year Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { selectedYear -= 1 }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Prev Year")
                    }
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) LanguageHelper.toBanglaDigits(selectedYear.toString()) else selectedYear.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = { selectedYear += 1 }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Year")
                    }
                }

                HorizontalDivider()

                // Month Grid (4 rows x 3 columns)
                val months = (1..12).toList()
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    months.chunked(3).forEach { rowMonths ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowMonths.forEach { m ->
                                val isSelected = m == selectedMonth
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { selectedMonth = m }
                                        .padding(vertical = 10.dp)
                                ) {
                                    Text(
                                        text = DateUtils.getMonthName(m, languageMode).take(3),
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { onConfirm(selectedYear, selectedMonth) }) {
                        Text("Select")
                    }
                }
            }
        }
    }
}
