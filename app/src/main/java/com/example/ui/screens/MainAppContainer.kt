package com.example.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Palette
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.ui.dialogs.AppUpdatePermissionDialog
import com.example.ui.theme.ThemePreferences
import com.example.ui.theme.ThemeMode
import com.example.ui.theme.ThemePalette
import com.example.util.PermissionHelper
import com.example.util.PermissionPreferences
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import com.example.ui.dialogs.DetectedBackupsListDialog
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.example.data.model.Account
import com.example.data.model.AccountType
import com.example.data.model.Category
import com.example.data.model.CategoryType
import com.example.data.model.LanguageMode
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.ui.components.AutoHidingBottomContainer
import com.example.ui.components.AutoHidingHeaderContainer
import com.example.ui.components.LanguageSelector
import com.example.ui.components.LocalHeaderScrollState
import com.example.ui.components.LocalSetTimelineActive
import com.example.ui.components.PopupCalculatorDialog
import com.example.ui.components.rememberHeaderScrollState
import com.example.ui.dialogs.AccountTransactionsDetailDialog
import com.example.ui.dialogs.AddEditAccountGroupOrCategoryDialog
import com.example.ui.dialogs.AddEditCategoryDialog
import com.example.ui.dialogs.AddEditTransactionSheet
import com.example.ui.dialogs.AutofillSettingsDialog
import com.example.ui.dialogs.TabCustomizationDialog
import com.example.ui.dialogs.ThemeFontSettingsDialog
import com.example.ui.screens.AppLockScreen
import com.example.ui.theme.AppThemeConfig
import com.example.ui.theme.ColorIntensity
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidTransfer
import com.example.ui.viewmodel.BudgetViewModel
import com.example.util.AppTab
import com.example.util.AutofillPreferences
import com.example.util.LanguageHelper
import com.example.util.NavigationTabConfig
import com.example.util.TabPosition
import kotlinx.coroutines.launch

enum class AppView {
    DASHBOARD,
    LEDGER,
    CASH_FLOW,
    PAYMENT_SOURCE,
    WISHLIST,
    BALANCE_SHEET,
    ACCOUNTS,
    RM_MANAGER,
    BUDGET,
    BUDGET_MAKER,
    SAVINGS_GOALS,
    CATEGORIES,
    EXPENSES,
    INCOME,
    REPORTS,
    LABELS,
    ITEMS_SUMMARY,
    RECURRING_BILLS,
    BACKUP_SYNC,
    SETTINGS,
    TRASH,
    RESET
}

fun AppTab.toAppView(): AppView = when (this) {
    AppTab.MAIN -> AppView.DASHBOARD
    AppTab.TRANSACTIONS -> AppView.LEDGER
    AppTab.CASH_FLOW -> AppView.CASH_FLOW
    AppTab.PAYMENT_SOURCE -> AppView.PAYMENT_SOURCE
    AppTab.BALANCE_SHEET -> AppView.BALANCE_SHEET
    AppTab.BUDGET -> AppView.BUDGET
    AppTab.SAVINGS_GOALS -> AppView.SAVINGS_GOALS
    AppTab.NET_EARNINGS -> AppView.REPORTS
    AppTab.LABELS -> AppView.LABELS
    AppTab.ITEMS_SUMMARY -> AppView.ITEMS_SUMMARY
    AppTab.REMINDERS -> AppView.RECURRING_BILLS
    AppTab.WISHLIST -> AppView.WISHLIST
}

fun AppView.toAppTab(): AppTab? = when (this) {
    AppView.DASHBOARD -> AppTab.MAIN
    AppView.LEDGER -> AppTab.TRANSACTIONS
    AppView.CASH_FLOW -> AppTab.CASH_FLOW
    AppView.PAYMENT_SOURCE -> AppTab.PAYMENT_SOURCE
    AppView.BALANCE_SHEET -> AppTab.BALANCE_SHEET
    AppView.BUDGET -> AppTab.BUDGET
    AppView.SAVINGS_GOALS -> AppTab.SAVINGS_GOALS
    AppView.REPORTS -> AppTab.NET_EARNINGS
    AppView.LABELS -> AppTab.LABELS
    AppView.ITEMS_SUMMARY -> AppTab.ITEMS_SUMMARY
    AppView.RECURRING_BILLS -> AppTab.REMINDERS
    AppView.WISHLIST -> AppTab.WISHLIST
    else -> null
}

enum class WindowSizeClassType {
    COMPACT,
    MEDIUM,
    EXPANDED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContainer(
    viewModel: BudgetViewModel,
    widgetAction: String? = null,
    onWidgetActionHandled: () -> Unit = {}
) {
    val languageMode by viewModel.languageMode.collectAsStateWithLifecycle()
    val themeConfig by viewModel.themeConfig.collectAsStateWithLifecycle()
    val tabConfig by viewModel.tabConfig.collectAsStateWithLifecycle()
    val dashboardConfig by viewModel.dashboardConfig.collectAsStateWithLifecycle()
    val overview by viewModel.financialOverview.collectAsStateWithLifecycle()
    val accountsWithBalances by viewModel.accountsWithBalances.collectAsStateWithLifecycle()
    val allAccounts by viewModel.allAccounts.collectAsStateWithLifecycle()
    val allCategories by viewModel.allCategories.collectAsStateWithLifecycle()
    val transactionsWithDetails by viewModel.transactionsWithDetails.collectAsStateWithLifecycle()
    val recurringBills by viewModel.recurringBillsWithDetails.collectAsStateWithLifecycle()
    val backupUiState by viewModel.backupUiState.collectAsStateWithLifecycle()
    val monthlyBudgets by viewModel.monthlyBudgets.collectAsStateWithLifecycle()
    val allMonthlyBudgets by viewModel.allMonthlyBudgets.collectAsStateWithLifecycle()
    val selectedBudgetYear by viewModel.selectedBudgetYear.collectAsStateWithLifecycle()
    val selectedBudgetMonth by viewModel.selectedBudgetMonth.collectAsStateWithLifecycle()
    val isDemoMode by viewModel.isDemoMode.collectAsStateWithLifecycle()
    val accountCalcConfig by viewModel.accountCalcConfig.collectAsStateWithLifecycle()
    val paymentSourceConfig by viewModel.paymentSourceConfig.collectAsStateWithLifecycle()
    val amountFormatConfig by viewModel.amountFormatConfig.collectAsStateWithLifecycle()
    val isAppLocked by viewModel.isAppLocked.collectAsStateWithLifecycle()
    val securityConfig by viewModel.securityConfig.collectAsStateWithLifecycle()
    val detectedBackups by viewModel.detectedBackups.collectAsStateWithLifecycle()
    val showRestoreBanner by viewModel.showRestoreBanner.collectAsStateWithLifecycle()
    val firstLaunchCheckDialogVisible by viewModel.firstLaunchCheckDialogVisible.collectAsStateWithLifecycle()
    val itemImageCacheMap by viewModel.itemImageCacheMap.collectAsStateWithLifecycle()

    val lifecycleOwner = LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> viewModel.onAppForegrounded()
                Lifecycle.Event.ON_STOP -> viewModel.onAppBackgrounded()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (isAppLocked && securityConfig.isAppLockEnabled && securityConfig.hasPin) {
        AppLockScreen(
            securityConfig = securityConfig,
            languageMode = languageMode,
            onVerifyPin = { viewModel.verifySecurityPin(it) },
            onVerifySecurityAnswer = { viewModel.verifySecurityAnswer(it) },
            onUnlockSuccess = { viewModel.unlockApp() },
            onResetPinAfterRecovery = { newPin ->
                viewModel.setSecurityPin(newPin)
            }
        )
        return
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val viewHistory = remember { mutableStateListOf(AppView.DASHBOARD) }
    var currentView by remember { mutableStateOf(AppView.DASHBOARD) }
    var lastBackPressTime by remember { mutableLongStateOf(0L) }
    var isTimelineActive by remember { mutableStateOf(false) }

    // Dialog control states
    var showAddTransactionSheet by remember { mutableStateOf(false) }
    var editingTransaction by remember { mutableStateOf<Transaction?>(null) }
    var presetTxType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var presetCategoryId by remember { mutableStateOf<Long?>(null) }
    var presetSubCategoryId by remember { mutableStateOf<Long?>(null) }
    var presetAmount by remember { mutableStateOf<Double?>(null) }
    var presetNote by remember { mutableStateOf<String?>(null) }
    var pendingPurchaseWishlistId by remember { mutableStateOf<Long?>(null) }

    fun openTransactionSheet(
        tx: Transaction? = null,
        type: TransactionType = TransactionType.EXPENSE,
        categoryId: Long? = null,
        subCategoryId: Long? = null,
        amount: Double? = null,
        note: String? = null,
        pendingWishlistId: Long? = null
    ) {
        editingTransaction = tx
        presetTxType = type
        presetCategoryId = categoryId
        presetSubCategoryId = subCategoryId
        presetAmount = amount
        presetNote = note
        pendingPurchaseWishlistId = pendingWishlistId
        showAddTransactionSheet = true
    }

    var showAddAccountDialog by remember { mutableStateOf(false) }
    var editingAccount by remember { mutableStateOf<Account?>(null) }
    var presetAccountParentId by remember { mutableStateOf<Long?>(null) }
    var selectedAccountForDetail by remember { mutableStateOf<Account?>(null) }

    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<Category?>(null) }
    var presetCategoryType by remember { mutableStateOf(CategoryType.EXPENSE) }
    var presetCategoryParentId by remember { mutableStateOf<Long?>(null) }

    var showGlobalCalculator by remember { mutableStateOf(false) }
    var showThemeFontSettings by remember { mutableStateOf(false) }
    var showAutofillSettingsDialog by remember { mutableStateOf(false) }
    var showTabCustomizationDialog by remember { mutableStateOf(false) }
    var showDashboardCustomizerDialog by remember { mutableStateOf(false) }

    LaunchedEffect(widgetAction) {
        if (widgetAction != null) {
            when (widgetAction) {
                com.example.widget.WidgetUpdateHelper.ACTION_ADD_EXPENSE -> {
                    presetTxType = TransactionType.EXPENSE
                    editingTransaction = null
                    showAddTransactionSheet = true
                }
                com.example.widget.WidgetUpdateHelper.ACTION_ADD_INCOME -> {
                    presetTxType = TransactionType.INCOME
                    editingTransaction = null
                    showAddTransactionSheet = true
                }
                com.example.widget.WidgetUpdateHelper.ACTION_ADD_TRANSFER -> {
                    presetTxType = TransactionType.TRANSFER
                    editingTransaction = null
                    showAddTransactionSheet = true
                }
                com.example.widget.WidgetUpdateHelper.ACTION_VIEW_GOALS -> {
                    currentView = AppView.SAVINGS_GOALS
                }
                com.example.widget.WidgetUpdateHelper.ACTION_VIEW_TRANSACTIONS -> {
                    currentView = AppView.LEDGER
                }
            }
            onWidgetActionHandled()
        }
    }

    val permissionPrefs = remember { PermissionPreferences.getInstance(context) }
    val permissionConfig by permissionPrefs.config.collectAsStateWithLifecycle()
    var showAppUpdatePermissionDialog by remember { mutableStateOf(false) }

    LaunchedEffect(permissionConfig) {
        val missing = PermissionHelper.getMissingRuntimePermissions(context)
        if (missing.isNotEmpty() &&
            !permissionConfig.hasCompletedUpdatePermissionPrompt &&
            !permissionConfig.isPermissionPromptDismissed
        ) {
            showAppUpdatePermissionDialog = true
        }
    }

    val headerScrollState = rememberHeaderScrollState()

    val visibleTabs = tabConfig.visibleTabs
    val isTabInVisibleTabs = visibleTabs.any { it.toAppView() == currentView }
    val currentTabIndex = visibleTabs.indexOfFirst { it.toAppView() == currentView }

    val pagerState = rememberPagerState(
        initialPage = if (currentTabIndex >= 0) currentTabIndex else 0,
        pageCount = { visibleTabs.size }
    )

    // Immediate active view for navigation bar during swipe gesture
    val activeNavView = if (isTabInVisibleTabs && pagerState.currentPage in visibleTabs.indices) {
        visibleTabs[pagerState.currentPage].toAppView()
    } else {
        currentView
    }

    // Sync currentView when user swipes left/right between tabs (only when actively viewing visible tabs)
    LaunchedEffect(pagerState, isTabInVisibleTabs) {
        if (!isTabInVisibleTabs) return@LaunchedEffect
        snapshotFlow { pagerState.currentPage }.collect { page ->
            if (isTabInVisibleTabs && page in visibleTabs.indices) {
                val swipedTab = visibleTabs.getOrNull(page) ?: return@collect
                val swipedView = swipedTab.toAppView()
                if (currentView != swipedView) {
                    headerScrollState.show()
                    currentView = swipedView
                    if (swipedView == AppView.DASHBOARD) {
                        viewHistory.clear()
                        viewHistory.add(AppView.DASHBOARD)
                    } else if (viewHistory.lastOrNull() != swipedView) {
                        viewHistory.add(swipedView)
                    }
                }
            }
        }
    }

    // Sync pager when currentView changes programmatically from drawer, back, or tab config change
    LaunchedEffect(currentView, visibleTabs) {
        val targetIndex = visibleTabs.indexOfFirst { it.toAppView() == currentView }
        if (targetIndex >= 0 && pagerState.currentPage != targetIndex && !pagerState.isScrollInProgress) {
            headerScrollState.show()
            pagerState.scrollToPage(targetIndex)
        }
    }

    val selectView: (AppView) -> Unit = { targetView ->
        headerScrollState.show()
        isTimelineActive = false
        if (currentView != targetView) {
            val tabIndex = visibleTabs.indexOfFirst { it.toAppView() == targetView }
            if (targetView == AppView.DASHBOARD) {
                viewHistory.clear()
                viewHistory.add(AppView.DASHBOARD)
            } else {
                viewHistory.add(targetView)
            }
            currentView = targetView
            if (tabIndex >= 0 && pagerState.currentPage != tabIndex) {
                scope.launch {
                    pagerState.scrollToPage(tabIndex)
                }
            }
        }
    }

    val handleBackPress: () -> Unit = {
        isTimelineActive = false
        when {
            showAddTransactionSheet -> {
                showAddTransactionSheet = false
                editingTransaction = null
            }
            showAppUpdatePermissionDialog -> {
                permissionPrefs.markPromptDismissed()
                showAppUpdatePermissionDialog = false
            }
            showTabCustomizationDialog -> showTabCustomizationDialog = false
            showAddAccountDialog -> showAddAccountDialog = false
            showAddCategoryDialog -> showAddCategoryDialog = false
            showGlobalCalculator -> showGlobalCalculator = false
            showThemeFontSettings -> showThemeFontSettings = false
            showAutofillSettingsDialog -> showAutofillSettingsDialog = false
            selectedAccountForDetail != null -> selectedAccountForDetail = null
            drawerState.isOpen -> scope.launch { drawerState.close() }
            viewHistory.size > 1 -> {
                viewHistory.removeAt(viewHistory.size - 1)
                currentView = viewHistory.last()
            }
            currentView != AppView.DASHBOARD -> {
                viewHistory.clear()
                viewHistory.add(AppView.DASHBOARD)
                currentView = AppView.DASHBOARD
            }
            else -> {
                val now = System.currentTimeMillis()
                if (now - lastBackPressTime < 2000L) {
                    (context as? Activity)?.finish()
                } else {
                    lastBackPressTime = now
                    val exitMsg = if (languageMode == LanguageMode.BANGLA) "বের হতে আবার ব্যাক চাপুন" else "Press back again to exit"
                    Toast.makeText(context, exitMsg, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Intercept hardware and gesture back presses
    BackHandler(enabled = true) {
        handleBackPress()
    }

    val isSubView = currentView in listOf(
        AppView.EXPENSES,
        AppView.INCOME,
        AppView.BACKUP_SYNC,
        AppView.BUDGET_MAKER,
        AppView.SETTINGS,
        AppView.RM_MANAGER,
        AppView.ACCOUNTS
    )

    // Window Width Adaptive Layout Container
    CompositionLocalProvider(
        LocalHeaderScrollState provides headerScrollState,
        LocalSetTimelineActive provides { active -> isTimelineActive = active }
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val windowSizeClass = when {
                maxWidth < 600.dp -> WindowSizeClassType.COMPACT
                maxWidth < 840.dp -> WindowSizeClassType.MEDIUM
                else -> WindowSizeClassType.EXPANDED
            }

            when (windowSizeClass) {
                WindowSizeClassType.COMPACT -> {
                    // PHONE LAYOUT: Modal Navigation Drawer + Top/Bottom Tab Bar based on User Preference
                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            ModalDrawerSheet(
                                drawerContainerColor = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.width(300.dp)
                            ) {
                                DrawerContent(
                                    viewModel = viewModel,
                                    currentView = currentView,
                                    onSelectView = {
                                        selectView(it)
                                        scope.launch { drawerState.close() }
                                    },
                                    accountsCount = allAccounts.size,
                                    categoriesCount = allCategories.size,
                                    netWorth = overview.netWorth,
                                    isBalanced = overview.isLedgerBalanced,
                                    languageMode = languageMode
                                )
                            }
                        }
                    ) {
                        Scaffold(
                            topBar = {
                                // TOP NAVIGATION TAB ROW (When Position is TOP)
                                if (tabConfig.position == TabPosition.TOP && !isSubView) {
                                    AutoHidingHeaderContainer(headerScrollState = headerScrollState) {
                                        TopNavigationBarRow(
                                            visibleTabs = tabConfig.visibleTabs,
                                            currentView = activeNavView,
                                            languageMode = languageMode,
                                            onSelectTab = { tab -> selectView(tab.toAppView()) }
                                        )
                                    }
                                }
                            },
                            bottomBar = {
                                // BOTTOM NAVIGATION TAB ROW (When Position is BOTTOM)
                                if (tabConfig.position == TabPosition.BOTTOM && !isSubView) {
                                    AutoHidingBottomContainer(headerScrollState = headerScrollState) {
                                        BottomNavigationBarRow(
                                            visibleTabs = tabConfig.visibleTabs,
                                            currentView = activeNavView,
                                            languageMode = languageMode,
                                            onSelectTab = { tab -> selectView(tab.toAppView()) }
                                        )
                                    }
                                }
                            },
                            floatingActionButtonPosition = FabPosition.End,
                            floatingActionButton = {
                                AppFab(
                                    currentView = currentView,
                                    onAddTransaction = {
                                        editingTransaction = null
                                        presetTxType = TransactionType.EXPENSE
                                        showAddTransactionSheet = true
                                    }
                                )
                            }
                        ) { paddingValues ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(paddingValues)
                                    .nestedScroll(headerScrollState.nestedScrollConnection)
                            ) {
                            if (isTabInVisibleTabs && visibleTabs.isNotEmpty()) {
                                HorizontalPager(
                                    state = pagerState,
                                    key = { page -> visibleTabs.getOrNull(page)?.name ?: page },
                                    beyondViewportPageCount = 0,
                                    userScrollEnabled = !isTimelineActive,
                                    modifier = Modifier.fillMaxSize()
                                ) { page ->
                                    val pageTab = visibleTabs.getOrNull(page)
                                    val pageView = pageTab?.toAppView() ?: AppView.DASHBOARD
                                    ScreenRouter(
                                        currentView = pageView,
                                        viewModel = viewModel,
                                        overview = overview,
                                        accountsWithBalances = accountsWithBalances,
                                        accountCalcConfig = accountCalcConfig,
                                        allAccounts = allAccounts,
                                        allCategories = allCategories,
                                        transactionsWithDetails = transactionsWithDetails,
                                        itemImageCacheMap = itemImageCacheMap,
                                        recurringBills = recurringBills,
                                        languageMode = languageMode,
                                        backupUiState = backupUiState,
                                        monthlyBudgets = monthlyBudgets,
                                        selectedBudgetYear = selectedBudgetYear,
                                        selectedBudgetMonth = selectedBudgetMonth,
                                        isDemoMode = isDemoMode,
                                        onOpenDrawer = { scope.launch { drawerState.open() } },
                                        onExitDemoMode = { viewModel.setDemoMode(false) },
                                        onBack = handleBackPress,
                                        onNavigate = { selectView(it) },
                                        onEditTransaction = { tx ->
                                            editingTransaction = tx
                                            showAddTransactionSheet = true
                                        },
                                        onAddTransactionWithType = { type ->
                                            presetTxType = type
                                            editingTransaction = null
                                            showAddTransactionSheet = true
                                        },
                                        onAddTransactionWithCategory = { cat ->
                                            editingTransaction = null
                                            presetTxType = if (cat.type == CategoryType.EXPENSE) TransactionType.EXPENSE else TransactionType.INCOME
                                            showAddTransactionSheet = true
                                        },
                                        onAddTransactionWithAccount = { acc ->
                                            editingTransaction = null
                                            presetTxType = if (acc.type == AccountType.LIABILITY) TransactionType.EXPENSE else TransactionType.INCOME
                                            showAddTransactionSheet = true
                                        },
                                        onAddTransactionPrefilled = { type, catId, subCatId, amt, note, wishId ->
                                            openTransactionSheet(
                                                type = type,
                                                categoryId = catId,
                                                subCategoryId = subCatId,
                                                amount = amt,
                                                note = note,
                                                pendingWishlistId = wishId
                                            )
                                        },
                                        onExecuteTransfer = { fromAcc, toAcc, amt ->
                                            presetTxType = TransactionType.TRANSFER
                                            editingTransaction = Transaction(
                                                type = TransactionType.TRANSFER,
                                                amount = amt,
                                                creditAccountId = fromAcc.id,
                                                debitAccountId = toAcc.id,
                                                dateEpochMs = System.currentTimeMillis(),
                                                note = "Payment source fund allocation"
                                            )
                                            showAddTransactionSheet = true
                                        },
                                        onExecuteRepayTransfer = { acc, label, defaultAmt ->
                                            presetTxType = TransactionType.TRANSFER
                                            val nameTitle = when {
                                                !label.isNullOrBlank() -> {
                                                    val clean = label.replace(Regex("""(?i)\s*Debit\s*$"""), "").trim()
                                                    "$clean Debit"
                                                }
                                                acc != null -> {
                                                    val rawName = acc.localizedName(languageMode).ifBlank { acc.nameEn }
                                                    val clean = rawName.replace(Regex("""(?i)\s*Debit\s*$"""), "").trim()
                                                    "$clean Debit"
                                                }
                                                else -> "RM Debit"
                                            }
                                            val targetDebitAccId = acc?.id ?: allAccounts.firstOrNull { com.example.util.RmManagerHelper.isExcludedAccount(it) }?.id
                                                ?: allAccounts.firstOrNull { it.nameEn.contains("RM Others", ignoreCase = true) || it.nameBn.contains("আরএম অন্যান্য", ignoreCase = true) }?.id
                                            editingTransaction = Transaction(
                                                type = TransactionType.TRANSFER,
                                                amount = if (defaultAmt > 0) defaultAmt else 0.0,
                                                creditAccountId = null,
                                                debitAccountId = targetDebitAccId,
                                                dateEpochMs = System.currentTimeMillis(),
                                                note = if (!label.isNullOrBlank()) "#$label" else "",
                                                referenceNo = label ?: "",
                                                payeeOrPayer = nameTitle
                                            )
                                            showAddTransactionSheet = true
                                        },
                                        onAddTransactionWithAccountAndType = { acc, txType ->
                                            presetTxType = txType
                                            editingTransaction = if (txType == TransactionType.EXPENSE) {
                                                Transaction(
                                                    type = txType,
                                                    amount = 0.0,
                                                    creditAccountId = acc.id,
                                                    dateEpochMs = System.currentTimeMillis()
                                                )
                                            } else {
                                                Transaction(
                                                    type = txType,
                                                    amount = 0.0,
                                                    debitAccountId = acc.id,
                                                    dateEpochMs = System.currentTimeMillis()
                                                )
                                            }
                                            showAddTransactionSheet = true
                                        },
                                        onAddAccount = { parentId ->
                                            editingAccount = null
                                            presetAccountParentId = parentId
                                            showAddAccountDialog = true
                                        },
                                        onEditAccount = { acc ->
                                            editingAccount = acc
                                            presetAccountParentId = acc.parentId
                                            showAddAccountDialog = true
                                        },
                                        onAddCategory = { type, parentId ->
                                            editingCategory = null
                                            presetCategoryType = type
                                            presetCategoryParentId = parentId
                                            showAddCategoryDialog = true
                                        },
                                        onEditCategory = { cat ->
                                            editingCategory = cat
                                            presetCategoryType = cat.type
                                            presetCategoryParentId = cat.parentId
                                            showAddCategoryDialog = true
                                        },
                                        onOpenTabCustomizer = { showTabCustomizationDialog = true },
                                        onOpenThemeFontSettings = { showThemeFontSettings = true },
                                        onOpenAutofillSettings = { showAutofillSettingsDialog = true },
                                        onAccountClick = { acc -> selectedAccountForDetail = acc },
                                        dashboardConfig = dashboardConfig,
                                        paymentSourceConfig = paymentSourceConfig
                                    )
                                }
                            } else {
                                ScreenRouter(
                                    currentView = currentView,
                                    viewModel = viewModel,
                                    overview = overview,
                                    accountsWithBalances = accountsWithBalances,
                                    accountCalcConfig = accountCalcConfig,
                                    allAccounts = allAccounts,
                                    allCategories = allCategories,
                                    transactionsWithDetails = transactionsWithDetails,
                                    itemImageCacheMap = itemImageCacheMap,
                                    recurringBills = recurringBills,
                                    languageMode = languageMode,
                                    backupUiState = backupUiState,
                                    monthlyBudgets = monthlyBudgets,
                                    selectedBudgetYear = selectedBudgetYear,
                                    selectedBudgetMonth = selectedBudgetMonth,
                                    isDemoMode = isDemoMode,
                                    onOpenDrawer = { scope.launch { drawerState.open() } },
                                    onExitDemoMode = { viewModel.setDemoMode(false) },
                                    onBack = handleBackPress,
                                    onNavigate = { selectView(it) },
                                    onEditTransaction = { tx ->
                                        editingTransaction = tx
                                        showAddTransactionSheet = true
                                    },
                                    onAddTransactionWithType = { type ->
                                        presetTxType = type
                                        editingTransaction = null
                                        showAddTransactionSheet = true
                                    },
                                    onAddTransactionWithCategory = { cat ->
                                        editingTransaction = null
                                        presetTxType = if (cat.type == CategoryType.EXPENSE) TransactionType.EXPENSE else TransactionType.INCOME
                                        showAddTransactionSheet = true
                                    },
                                    onAddTransactionWithAccount = { acc ->
                                        editingTransaction = null
                                        presetTxType = if (acc.type == AccountType.LIABILITY) TransactionType.EXPENSE else TransactionType.INCOME
                                        showAddTransactionSheet = true
                                    },
                                    onAddTransactionPrefilled = { type, catId, subCatId, amt, note, wishId ->
                                        openTransactionSheet(
                                            type = type,
                                            categoryId = catId,
                                            subCategoryId = subCatId,
                                            amount = amt,
                                            note = note,
                                            pendingWishlistId = wishId
                                        )
                                    },
                                    onExecuteTransfer = { fromAcc, toAcc, amt ->
                                        presetTxType = TransactionType.TRANSFER
                                        editingTransaction = Transaction(
                                            type = TransactionType.TRANSFER,
                                            amount = amt,
                                            creditAccountId = fromAcc.id,
                                            debitAccountId = toAcc.id,
                                            dateEpochMs = System.currentTimeMillis(),
                                            note = "Payment source fund allocation"
                                        )
                                        showAddTransactionSheet = true
                                    },
                                    onExecuteRepayTransfer = { acc, label, defaultAmt ->
                                        presetTxType = TransactionType.TRANSFER
                                        val nameTitle = when {
                                            !label.isNullOrBlank() -> {
                                                val clean = label.replace(Regex("""(?i)\s*Debit\s*$"""), "").trim()
                                                "$clean Debit"
                                            }
                                            acc != null -> {
                                                val rawName = acc.localizedName(languageMode).ifBlank { acc.nameEn }
                                                val clean = rawName.replace(Regex("""(?i)\s*Debit\s*$"""), "").trim()
                                                "$clean Debit"
                                            }
                                            else -> "RM Debit"
                                        }
                                        val targetDebitAccId = acc?.id ?: allAccounts.firstOrNull { com.example.util.RmManagerHelper.isExcludedAccount(it) }?.id
                                            ?: allAccounts.firstOrNull { it.nameEn.contains("RM Others", ignoreCase = true) || it.nameBn.contains("আরএম অন্যান্য", ignoreCase = true) }?.id
                                        editingTransaction = Transaction(
                                            type = TransactionType.TRANSFER,
                                            amount = if (defaultAmt > 0) defaultAmt else 0.0,
                                            creditAccountId = null,
                                            debitAccountId = targetDebitAccId,
                                            dateEpochMs = System.currentTimeMillis(),
                                            note = if (!label.isNullOrBlank()) "#$label" else "",
                                            referenceNo = label ?: "",
                                            payeeOrPayer = nameTitle
                                        )
                                        showAddTransactionSheet = true
                                    },
                                    onAddTransactionWithAccountAndType = { acc, txType ->
                                        presetTxType = txType
                                        editingTransaction = if (txType == TransactionType.EXPENSE) {
                                            Transaction(
                                                type = txType,
                                                amount = 0.0,
                                                creditAccountId = acc.id,
                                                dateEpochMs = System.currentTimeMillis()
                                            )
                                        } else {
                                            Transaction(
                                                type = txType,
                                                amount = 0.0,
                                                debitAccountId = acc.id,
                                                dateEpochMs = System.currentTimeMillis()
                                            )
                                        }
                                        showAddTransactionSheet = true
                                    },
                                    onAddAccount = { parentId ->
                                        editingAccount = null
                                        presetAccountParentId = parentId
                                        showAddAccountDialog = true
                                    },
                                    onEditAccount = { acc ->
                                        editingAccount = acc
                                        presetAccountParentId = acc.parentId
                                        showAddAccountDialog = true
                                    },
                                    onAddCategory = { type, parentId ->
                                        editingCategory = null
                                        presetCategoryType = type
                                        presetCategoryParentId = parentId
                                        showAddCategoryDialog = true
                                    },
                                    onEditCategory = { cat ->
                                        editingCategory = cat
                                        presetCategoryType = cat.type
                                        presetCategoryParentId = cat.parentId
                                        showAddCategoryDialog = true
                                    },
                                    onOpenTabCustomizer = { showTabCustomizationDialog = true },
                                    onOpenThemeFontSettings = { showThemeFontSettings = true },
                                    onOpenAutofillSettings = { showAutofillSettingsDialog = true },
                                    onAccountClick = { acc -> selectedAccountForDetail = acc },
                                    dashboardConfig = dashboardConfig,
                                    paymentSourceConfig = paymentSourceConfig
                                )
                            }
                        }
                    }
                }
            }

            WindowSizeClassType.MEDIUM -> {
                // FOLDABLE / SMALL TABLET LAYOUT: Modal Navigation Drawer + Navigation Rail + Main Content Area
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet(
                            drawerContainerColor = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.width(300.dp)
                        ) {
                            DrawerContent(
                                viewModel = viewModel,
                                currentView = currentView,
                                onSelectView = {
                                    selectView(it)
                                    scope.launch { drawerState.close() }
                                },
                                accountsCount = allAccounts.size,
                                categoriesCount = allCategories.size,
                                netWorth = overview.netWorth,
                                isBalanced = overview.isLedgerBalanced,
                                languageMode = languageMode
                            )
                        }
                    }
                ) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        NavigationRail(
                            containerColor = MaterialTheme.colorScheme.surface,
                            header = {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = MaterialTheme.colorScheme.onSurface)
                                    }
                                    IconButton(onClick = { showTabCustomizationDialog = true }) {
                                        Icon(Icons.Default.ViewCarousel, contentDescription = "Tabs", tint = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(onClick = { showThemeFontSettings = true }) {
                                        Icon(Icons.Default.Palette, contentDescription = "Theme", tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxHeight()
                        ) {
                        tabConfig.visibleTabs.forEach { tab ->
                            val view = tab.toAppView()
                            val isSelected = currentView == view
                            NavigationRailItem(
                                selected = isSelected,
                                onClick = { selectView(view) },
                                icon = { Icon(tab.icon, contentDescription = tab.getTitle(languageMode)) },
                                label = { Text(tab.getTitle(languageMode).take(6), fontSize = 10.sp) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                    indicatorColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }

                    Scaffold(
                        floatingActionButtonPosition = FabPosition.End,
                        floatingActionButton = {
                            AppFab(
                                currentView = currentView,
                                onAddTransaction = {
                                    editingTransaction = null
                                    presetTxType = TransactionType.EXPENSE
                                    showAddTransactionSheet = true
                                }
                            )
                        }
                    ) { padding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(padding)
                                .nestedScroll(headerScrollState.nestedScrollConnection)
                        ) {
                            ScreenRouter(
                                currentView = currentView,
                                viewModel = viewModel,
                                overview = overview,
                                accountsWithBalances = accountsWithBalances,
                                accountCalcConfig = accountCalcConfig,
                                allAccounts = allAccounts,
                                allCategories = allCategories,
                                transactionsWithDetails = transactionsWithDetails,
                                itemImageCacheMap = itemImageCacheMap,
                                recurringBills = recurringBills,
                                languageMode = languageMode,
                                backupUiState = backupUiState,
                                monthlyBudgets = monthlyBudgets,
                                selectedBudgetYear = selectedBudgetYear,
                                selectedBudgetMonth = selectedBudgetMonth,
                                isDemoMode = isDemoMode,
                                onOpenDrawer = { scope.launch { drawerState.open() } },
                                onExitDemoMode = { viewModel.setDemoMode(false) },
                                onBack = handleBackPress,
                                onNavigate = { selectView(it) },
                                onEditTransaction = { tx ->
                                    editingTransaction = tx
                                    showAddTransactionSheet = true
                                },
                                onAddTransactionWithType = { type ->
                                    presetTxType = type
                                    editingTransaction = null
                                    showAddTransactionSheet = true
                                },
                                onAddTransactionWithCategory = { cat ->
                                    editingTransaction = null
                                    presetTxType = if (cat.type == CategoryType.EXPENSE) TransactionType.EXPENSE else TransactionType.INCOME
                                    showAddTransactionSheet = true
                                },
                                onAddTransactionWithAccount = { acc ->
                                    editingTransaction = null
                                    presetTxType = if (acc.type == AccountType.LIABILITY) TransactionType.EXPENSE else TransactionType.INCOME
                                    showAddTransactionSheet = true
                                },
                                onAddTransactionPrefilled = { type, catId, subCatId, amt, note, wishId ->
                                    openTransactionSheet(
                                        type = type,
                                        categoryId = catId,
                                        subCategoryId = subCatId,
                                        amount = amt,
                                        note = note,
                                        pendingWishlistId = wishId
                                    )
                                },
                                onExecuteRepayTransfer = { acc, label, defaultAmt ->
                                    presetTxType = TransactionType.TRANSFER
                                    val nameTitle = when {
                                        !label.isNullOrBlank() -> {
                                            val clean = label.replace(Regex("""(?i)\s*Debit\s*$"""), "").trim()
                                            "$clean Debit"
                                        }
                                        acc != null -> {
                                            val rawName = acc.localizedName(languageMode).ifBlank { acc.nameEn }
                                            val clean = rawName.replace(Regex("""(?i)\s*Debit\s*$"""), "").trim()
                                            "$clean Debit"
                                        }
                                        else -> "RM Debit"
                                    }
                                    val targetDebitAccId = acc?.id ?: allAccounts.firstOrNull { com.example.util.RmManagerHelper.isExcludedAccount(it) }?.id
                                        ?: allAccounts.firstOrNull { it.nameEn.contains("RM Others", ignoreCase = true) || it.nameBn.contains("আরএম অন্যান্য", ignoreCase = true) }?.id
                                    editingTransaction = Transaction(
                                        type = TransactionType.TRANSFER,
                                        amount = if (defaultAmt > 0) defaultAmt else 0.0,
                                        creditAccountId = null,
                                        debitAccountId = targetDebitAccId,
                                        dateEpochMs = System.currentTimeMillis(),
                                        note = if (!label.isNullOrBlank()) "#$label" else "",
                                        referenceNo = label ?: "",
                                        payeeOrPayer = nameTitle
                                    )
                                    showAddTransactionSheet = true
                                },
                                onAddAccount = { parentId ->
                                    editingAccount = null
                                    presetAccountParentId = parentId
                                    showAddAccountDialog = true
                                },
                                onEditAccount = { acc ->
                                    editingAccount = acc
                                    presetAccountParentId = acc.parentId
                                    showAddAccountDialog = true
                                },
                                onAddCategory = { type, parentId ->
                                    editingCategory = null
                                    presetCategoryType = type
                                    presetCategoryParentId = parentId
                                    showAddCategoryDialog = true
                                },
                                onEditCategory = { cat ->
                                    editingCategory = cat
                                    presetCategoryType = cat.type
                                    presetCategoryParentId = cat.parentId
                                    showAddCategoryDialog = true
                                },
                                onOpenTabCustomizer = { showTabCustomizationDialog = true },
                                onOpenThemeFontSettings = { showThemeFontSettings = true },
                                onOpenAutofillSettings = { showAutofillSettingsDialog = true },
                                onAccountClick = { acc -> selectedAccountForDetail = acc },
                                dashboardConfig = dashboardConfig,
                                paymentSourceConfig = paymentSourceConfig
                            )
                        }
                    }
                }
            }
        }

            WindowSizeClassType.EXPANDED -> {
                // LARGE TABLET / DESKTOP LAYOUT: Permanent Navigation Drawer
                PermanentNavigationDrawer(
                    drawerContent = {
                        PermanentDrawerSheet(
                            drawerContainerColor = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.width(280.dp)
                        ) {
                            DrawerContent(
                                viewModel = viewModel,
                                currentView = currentView,
                                onSelectView = { selectView(it) },
                                accountsCount = allAccounts.size,
                                categoriesCount = allCategories.size,
                                netWorth = overview.netWorth,
                                isBalanced = overview.isLedgerBalanced,
                                languageMode = languageMode
                            )
                        }
                    }
                ) {
                    Scaffold(
                        floatingActionButtonPosition = FabPosition.End,
                        floatingActionButton = {
                            AppFab(
                                currentView = currentView,
                                onAddTransaction = {
                                    editingTransaction = null
                                    presetTxType = TransactionType.EXPENSE
                                    showAddTransactionSheet = true
                                }
                            )
                        }
                    ) { padding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(padding)
                                .nestedScroll(headerScrollState.nestedScrollConnection)
                        ) {
                            ScreenRouter(
                                currentView = currentView,
                                viewModel = viewModel,
                                overview = overview,
                                accountsWithBalances = accountsWithBalances,
                                accountCalcConfig = accountCalcConfig,
                                allAccounts = allAccounts,
                                allCategories = allCategories,
                                transactionsWithDetails = transactionsWithDetails,
                                itemImageCacheMap = itemImageCacheMap,
                                recurringBills = recurringBills,
                                languageMode = languageMode,
                                backupUiState = backupUiState,
                                monthlyBudgets = monthlyBudgets,
                                selectedBudgetYear = selectedBudgetYear,
                                selectedBudgetMonth = selectedBudgetMonth,
                                isDemoMode = isDemoMode,
                                onOpenDrawer = { scope.launch { drawerState.open() } },
                                onExitDemoMode = { viewModel.setDemoMode(false) },
                                onBack = handleBackPress,
                                onNavigate = { selectView(it) },
                                onEditTransaction = { tx ->
                                    editingTransaction = tx
                                    showAddTransactionSheet = true
                                },
                                onAddTransactionWithType = { type ->
                                    presetTxType = type
                                    editingTransaction = null
                                    showAddTransactionSheet = true
                                },
                                onAddTransactionWithCategory = { cat ->
                                    editingTransaction = null
                                    presetTxType = if (cat.type == CategoryType.EXPENSE) TransactionType.EXPENSE else TransactionType.INCOME
                                    showAddTransactionSheet = true
                                },
                                onAddTransactionWithAccount = { acc ->
                                    editingTransaction = null
                                    presetTxType = if (acc.type == AccountType.LIABILITY) TransactionType.EXPENSE else TransactionType.INCOME
                                    showAddTransactionSheet = true
                                },
                                onAddTransactionPrefilled = { type, catId, subCatId, amt, note, wishId ->
                                    openTransactionSheet(
                                        type = type,
                                        categoryId = catId,
                                        subCategoryId = subCatId,
                                        amount = amt,
                                        note = note,
                                        pendingWishlistId = wishId
                                    )
                                },
                                onExecuteTransfer = { fromAcc, toAcc, amt ->
                                    presetTxType = TransactionType.TRANSFER
                                    editingTransaction = Transaction(
                                        type = TransactionType.TRANSFER,
                                        amount = amt,
                                        creditAccountId = fromAcc.id,
                                        debitAccountId = toAcc.id,
                                        dateEpochMs = System.currentTimeMillis(),
                                        note = "Payment source fund allocation"
                                    )
                                    showAddTransactionSheet = true
                                },
                                onExecuteRepayTransfer = { acc, label, defaultAmt ->
                                    presetTxType = TransactionType.TRANSFER
                                    val nameTitle = when {
                                        !label.isNullOrBlank() -> {
                                            val clean = label.replace(Regex("""(?i)\s*Debit\s*$"""), "").trim()
                                            "$clean Debit"
                                        }
                                        acc != null -> {
                                            val rawName = acc.localizedName(languageMode).ifBlank { acc.nameEn }
                                            val clean = rawName.replace(Regex("""(?i)\s*Debit\s*$"""), "").trim()
                                            "$clean Debit"
                                        }
                                        else -> "RM Debit"
                                    }
                                    val targetDebitAccId = acc?.id ?: allAccounts.firstOrNull { com.example.util.RmManagerHelper.isExcludedAccount(it) }?.id
                                        ?: allAccounts.firstOrNull { it.nameEn.contains("RM Others", ignoreCase = true) || it.nameBn.contains("আরএম অন্যান্য", ignoreCase = true) }?.id
                                    editingTransaction = Transaction(
                                        type = TransactionType.TRANSFER,
                                        amount = if (defaultAmt > 0) defaultAmt else 0.0,
                                        creditAccountId = null,
                                        debitAccountId = targetDebitAccId,
                                        dateEpochMs = System.currentTimeMillis(),
                                        note = if (!label.isNullOrBlank()) "#$label" else "",
                                        referenceNo = label ?: "",
                                        payeeOrPayer = nameTitle
                                    )
                                    showAddTransactionSheet = true
                                },
                                onAddTransactionWithAccountAndType = { acc, txType ->
                                    presetTxType = txType
                                    editingTransaction = if (txType == TransactionType.EXPENSE) {
                                        Transaction(
                                            type = txType,
                                            amount = 0.0,
                                            creditAccountId = acc.id,
                                            dateEpochMs = System.currentTimeMillis()
                                        )
                                    } else {
                                        Transaction(
                                            type = txType,
                                            amount = 0.0,
                                            debitAccountId = acc.id,
                                            dateEpochMs = System.currentTimeMillis()
                                        )
                                    }
                                    showAddTransactionSheet = true
                                },
                                onAddAccount = { parentId ->
                                    editingAccount = null
                                    presetAccountParentId = parentId
                                    showAddAccountDialog = true
                                },
                                onEditAccount = { acc ->
                                    editingAccount = acc
                                    presetAccountParentId = acc.parentId
                                    showAddAccountDialog = true
                                },
                                onAddCategory = { type, parentId ->
                                    editingCategory = null
                                    presetCategoryType = type
                                    presetCategoryParentId = parentId
                                    showAddCategoryDialog = true
                                },
                                onEditCategory = { cat ->
                                    editingCategory = cat
                                    presetCategoryType = cat.type
                                    presetCategoryParentId = cat.parentId
                                    showAddCategoryDialog = true
                                },
                                onOpenTabCustomizer = { showTabCustomizationDialog = true },
                                onOpenThemeFontSettings = { showThemeFontSettings = true },
                                onOpenAutofillSettings = { showAutofillSettingsDialog = true },
                                onAccountClick = { acc -> selectedAccountForDetail = acc },
                                dashboardConfig = dashboardConfig,
                                paymentSourceConfig = paymentSourceConfig
                            )
                        }
                    }
                }
            }
        }
    }
}

    // Modal Dialogs
    if (showTabCustomizationDialog) {
        TabCustomizationDialog(
            config = tabConfig,
            languageMode = languageMode,
            onPositionChanged = { viewModel.setTabPosition(it) },
            onToggleTab = { tab, enabled -> viewModel.toggleTab(tab, enabled) },
            onReorderTab = { from, to -> viewModel.reorderTab(from, to) },
            onResetDefaults = { viewModel.resetTabDefaults() },
            onDismiss = { showTabCustomizationDialog = false }
        )
    }

    if (showAddTransactionSheet) {
        AddEditTransactionSheet(
            accounts = allAccounts,
            categories = allCategories,
            allTransactions = transactionsWithDetails,
            monthlyBudgets = allMonthlyBudgets,
            languageMode = languageMode,
            existingTransaction = editingTransaction,
            defaultType = presetTxType,
            initialCategoryId = presetCategoryId,
            initialSubCategoryId = presetSubCategoryId,
            initialAmount = presetAmount,
            initialNote = presetNote,
            onDismiss = {
                showAddTransactionSheet = false
                editingTransaction = null
                presetCategoryId = null
                presetSubCategoryId = null
                presetAmount = null
                presetNote = null
                pendingPurchaseWishlistId = null
            },
            onSave = { tx ->
                viewModel.saveTransaction(tx)
                if (tx.payeeOrPayer.isNotBlank()) {
                    viewModel.autoDiscoverAndCacheItemIcon(tx.payeeOrPayer, tx.categoryId, tx.subCategoryId)
                }
                pendingPurchaseWishlistId?.let { wishId ->
                    viewModel.toggleWishlistPurchased(wishId, true)
                    pendingPurchaseWishlistId = null
                }
                presetCategoryId = null
                presetSubCategoryId = null
                presetAmount = null
                presetNote = null
            },
            onSaveSplit = { splitGroupId, baseTx, items, existingTxs ->
                viewModel.saveSplitTransaction(splitGroupId, baseTx, items, existingTxs)
                if (baseTx.payeeOrPayer.isNotBlank()) {
                    viewModel.autoDiscoverAndCacheItemIcon(baseTx.payeeOrPayer, baseTx.categoryId, baseTx.subCategoryId)
                }
                items.forEach { splitItem ->
                    if (splitItem.payeeOrPayer.isNotBlank()) {
                        viewModel.autoDiscoverAndCacheItemIcon(splitItem.payeeOrPayer, splitItem.categoryId, splitItem.subCategoryId)
                    }
                }
                pendingPurchaseWishlistId?.let { wishId ->
                    viewModel.toggleWishlistPurchased(wishId, true)
                    pendingPurchaseWishlistId = null
                }
                presetCategoryId = null
                presetSubCategoryId = null
                presetAmount = null
                presetNote = null
            },
            onDelete = { tx -> viewModel.deleteTransaction(tx) },
            onAddNewCategory = { cat -> viewModel.saveCategory(cat) },
            onAddNewAccount = { acc -> viewModel.saveAccount(acc) }
        )
    }

    if (showAddAccountDialog) {
        val parentAccounts = allAccounts.filter { it.parentId == null }
        AddEditAccountGroupOrCategoryDialog(
            allGroups = parentAccounts,
            allAccounts = allAccounts,
            transactions = transactionsWithDetails,
            languageMode = languageMode,
            existingAccount = editingAccount,
            defaultGroupId = presetAccountParentId,
            securityConfig = securityConfig,
            onVerifyPin = { viewModel.verifySecurityPin(it) },
            onDismiss = { showAddAccountDialog = false },
            onSave = { acc -> viewModel.saveAccount(acc) },
            onDelete = { acc -> viewModel.deleteAccount(acc) },
            onDeleteWithStrategy = { acc, deleteTx, targetId, customMap ->
                viewModel.deleteAccountWithStrategy(acc, deleteTx, targetId, customMap)
            }
        )
    }

    if (showAddCategoryDialog) {
        val parentCategories = allCategories.filter { it.parentId == null }
        AddEditCategoryDialog(
            parentCategories = parentCategories,
            allCategories = allCategories,
            transactions = transactionsWithDetails,
            languageMode = languageMode,
            existingCategory = editingCategory,
            defaultType = presetCategoryType,
            defaultParentId = presetCategoryParentId,
            securityConfig = securityConfig,
            onVerifyPin = { viewModel.verifySecurityPin(it) },
            onDismiss = { showAddCategoryDialog = false },
            onSave = { cat -> viewModel.saveCategory(cat) },
            onDelete = { cat -> viewModel.deleteCategory(cat) },
            onDeleteWithStrategy = { cat, deleteTx, targetId, customMap ->
                viewModel.deleteCategoryWithStrategy(cat, deleteTx, targetId, customMap)
            }
        )
    }

    if (showGlobalCalculator) {
        PopupCalculatorDialog(
            languageMode = languageMode,
            onDismiss = { showGlobalCalculator = false },
            onValueConfirmed = { /* Confirmed */ }
        )
    }

    if (showThemeFontSettings) {
        ThemeFontSettingsDialog(
            themeConfig = themeConfig,
            languageMode = languageMode,
            onPaletteSelected = { viewModel.setThemePalette(it) },
            onCustomThemeSelected = { viewModel.selectCustomTheme(it) },
            onCustomThemeAdded = { name, primary, secondary, surface, header, income, expense, transfer, shade ->
                viewModel.addCustomTheme(name, primary, secondary, surface, header, income, expense, transfer, shade)
            },
            onCustomThemeUpdated = { viewModel.updateCustomTheme(it) },
            onCustomThemeDeleted = { viewModel.deleteCustomTheme(it) },
            onModeSelected = { viewModel.setThemeMode(it) },
            onColorIntensitySelected = { viewModel.setColorIntensity(it) },
            onDynamicColorToggled = { viewModel.setDynamicColor(it) },
            onFontPresetSelected = { viewModel.setFontPreset(it) },
            onCornerRadiusSelected = { viewModel.setThemeCornerRadius(it) },
            onFontScaleSelected = { viewModel.setThemeFontScale(it) },
            onSemanticPaletteSelected = { viewModel.setFinancialSemanticPalette(it) },
            onDarkSurfaceToneSelected = { viewModel.setDarkSurfaceTone(it) },
            onResetDefaults = { viewModel.resetThemePreferences() },
            onDismiss = { showThemeFontSettings = false }
        )
    }

    if (showAutofillSettingsDialog) {
        val currentContext = LocalContext.current
        val autofillPrefs = remember { AutofillPreferences.getInstance(currentContext) }
        val autofillConfig by autofillPrefs.config.collectAsState()
        AutofillSettingsDialog(
            config = autofillConfig,
            languageMode = languageMode,
            onConfigChange = { autofillPrefs.updateConfig(it) },
            onDismiss = { showAutofillSettingsDialog = false }
        )
    }

    if (showDashboardCustomizerDialog) {
        com.example.ui.screens.dashboard.CustomizeDashboardCardsDialog(
            config = dashboardConfig,
            languageMode = languageMode,
            onDismiss = { showDashboardCustomizerDialog = false },
            onToggleCard = { card, visible -> viewModel.toggleDashboardCard(card, visible) },
            onMoveCard = { from, to -> viewModel.moveDashboardCard(from, to) },
            onResetDefaults = { viewModel.resetDashboardDefaults() }
        )
    }

    if (showAppUpdatePermissionDialog) {
        AppUpdatePermissionDialog(
            languageMode = languageMode,
            onDismiss = { showAppUpdatePermissionDialog = false }
        )
    }

    if (selectedAccountForDetail != null) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(20f),
            color = MaterialTheme.colorScheme.background
        ) {
            AccountDetailScreen(
                account = selectedAccountForDetail!!,
                allAccounts = allAccounts,
                allCategories = allCategories,
                allTransactions = transactionsWithDetails,
                languageMode = languageMode,
                onDismiss = { selectedAccountForDetail = null },
                onEditTransaction = { tx ->
                    editingTransaction = tx
                    showAddTransactionSheet = true
                },
                onAddTransactionForAccount = { acc: Account ->
                    editingTransaction = null
                    presetTxType = if (acc.type == AccountType.LIABILITY) TransactionType.EXPENSE else TransactionType.INCOME
                    showAddTransactionSheet = true
                },
                onEditAccount = { acc: Account ->
                    editingAccount = acc
                    presetAccountParentId = acc.parentId
                    showAddAccountDialog = true
                },
                onSaveTransaction = { tx -> viewModel.saveTransaction(tx) },
                onUpdateTransactions = { txList -> viewModel.updateTransactions(txList) },
                onDeleteTransactions = { txList -> viewModel.deleteTransactions(txList) }
            )
        }
    }

    if (firstLaunchCheckDialogVisible && detectedBackups.isNotEmpty()) {
        DetectedBackupsListDialog(
            detectedBackups = detectedBackups,
            isFirstLaunchPrompt = true,
            languageMode = languageMode,
            onScanAgain = { viewModel.scanForPreviousBackups() },
            onSelectBackupToRestore = { backup, isMerge ->
                viewModel.restoreDetectedBackup(backup, isMerge)
                viewModel.dismissFirstLaunchDialog()
            },
            onDismiss = {
                viewModel.dismissFirstLaunchDialog()
            }
        )
    }
}

@Composable
private fun TopNavigationBarRow(
    visibleTabs: List<AppTab>,
    currentView: AppView,
    languageMode: LanguageMode,
    onSelectTab: (AppTab) -> Unit
) {
    val selectedIndex = visibleTabs.indexOfFirst { it.toAppView() == currentView }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        ScrollableTabRow(
            selectedTabIndex = if (selectedIndex >= 0) selectedIndex else 0,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            edgePadding = 8.dp,
            divider = {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            },
            indicator = { tabPositions ->
                if (selectedIndex in tabPositions.indices) {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                        color = MaterialTheme.colorScheme.primary,
                        height = 3.dp
                    )
                }
            }
        ) {
            visibleTabs.forEach { tab ->
                val isSelected = currentView == tab.toAppView()
                Tab(
                    selected = isSelected,
                    onClick = { onSelectTab(tab) },
                    icon = {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.getTitle(languageMode),
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    text = {
                        Text(
                            text = tab.getTitle(languageMode),
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1
                        )
                    },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.testTag("top_nav_${tab.id}")
                )
            }
        }
    }
}

@Composable
private fun BottomNavigationBarRow(
    visibleTabs: List<AppTab>,
    currentView: AppView,
    languageMode: LanguageMode,
    onSelectTab: (AppTab) -> Unit
) {
    if (visibleTabs.size <= 5) {
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp
        ) {
            visibleTabs.forEach { tab ->
                val view = tab.toAppView()
                val isSelected = currentView == view
                NavigationBarItem(
                    selected = isSelected,
                    onClick = { onSelectTab(tab) },
                    icon = { Icon(tab.icon, contentDescription = tab.getTitle(languageMode)) },
                    label = {
                        Text(
                            tab.getTitle(languageMode),
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("bottom_nav_${tab.id}")
                )
            }
        }
    } else {
        val selectedIndex = visibleTabs.indexOfFirst { it.toAppView() == currentView }
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            ScrollableTabRow(
                selectedTabIndex = if (selectedIndex >= 0) selectedIndex else 0,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                edgePadding = 8.dp,
                divider = {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                },
                indicator = { tabPositions ->
                    if (selectedIndex in tabPositions.indices) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                            color = MaterialTheme.colorScheme.primary,
                            height = 3.dp
                        )
                    }
                }
            ) {
                visibleTabs.forEach { tab ->
                    val isSelected = currentView == tab.toAppView()
                    Tab(
                        selected = isSelected,
                        onClick = { onSelectTab(tab) },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.getTitle(languageMode),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        text = {
                            Text(
                                text = tab.getTitle(languageMode),
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1
                            )
                        },
                        selectedContentColor = MaterialTheme.colorScheme.primary,
                        unselectedContentColor = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.testTag("bottom_nav_${tab.id}")
                    )
                }
            }
        }
    }
}

@Composable
private fun AppFab(
    currentView: AppView,
    onAddTransaction: () -> Unit
) {
    if (currentView in listOf(AppView.DASHBOARD, AppView.LEDGER)) {
        FloatingActionButton(
            onClick = onAddTransaction,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier.testTag("main_fab_add_tx")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Transaction")
        }
    }
}

@Composable
private fun DrawerContent(
    viewModel: BudgetViewModel,
    currentView: AppView,
    onSelectView: (AppView) -> Unit,
    accountsCount: Int,
    categoriesCount: Int,
    netWorth: Double,
    isBalanced: Boolean,
    languageMode: LanguageMode
) {
    val trashedItems by viewModel.trashedItems.collectAsStateWithLifecycle()
    val savingsSummary by viewModel.savingsSummary.collectAsStateWithLifecycle()
    val backupConfig by viewModel.backupSettingsConfig.collectAsStateWithLifecycle()
    val signedInAccount by viewModel.signedInGoogleAccount.collectAsStateWithLifecycle()
    val secondarySignedInAccount by viewModel.secondarySignedInGoogleAccount.collectAsStateWithLifecycle()
    val isDemoMode by viewModel.isDemoMode.collectAsStateWithLifecycle()
    val activeWishlistCount by viewModel.activeWishlistCount.collectAsStateWithLifecycle()

    // Determine active cloud drive account (prefer Drive 1, consider Drive 2 if Drive 1 is inactive)
    val drive1 = backupConfig.primaryAccount
    val drive2 = backupConfig.secondaryAccount

    val isDrive1Active = drive1.isLinked || drive1.email.isNotBlank() || drive1.displayName.isNotBlank() || signedInAccount != null
    val isDrive2Active = drive2.isLinked || drive2.email.isNotBlank() || drive2.displayName.isNotBlank() || secondarySignedInAccount != null

    val (activeDrive, activeGoogleAccount, isAnyDriveActive) = when {
        isDrive1Active -> Triple(drive1, signedInAccount, true)
        isDrive2Active -> Triple(drive2, secondarySignedInAccount, true)
        else -> Triple(drive1, null, false)
    }

    val driveProviderName = when {
        isAnyDriveActive -> activeDrive.provider.ifBlank { "Google Drive" }
        else -> drive1.provider.ifBlank { "Google Drive" }
    }

    val driveDisplayName = when {
        isAnyDriveActive -> {
            activeDrive.displayName.ifBlank {
                activeGoogleAccount?.displayName ?: if (languageMode == LanguageMode.BANGLA) "গুগল ড্রাইভ" else "Drive User"
            }
        }
        else -> {
            if (languageMode == LanguageMode.BANGLA) "একাউন্ট যুক্ত নেই" else "No Account Connected"
        }
    }

    val driveEmail = when {
        isAnyDriveActive -> {
            activeDrive.email.ifBlank {
                activeGoogleAccount?.email ?: ""
            }
        }
        else -> {
            if (languageMode == LanguageMode.BANGLA) "ড্রাইভ সিঙ্ক সেটআপ করুন" else "Tap to connect cloud backup"
        }
    }

    val drivePhotoUrl = when {
        isAnyDriveActive -> {
            activeDrive.photoUrl.ifBlank {
                activeGoogleAccount?.photoUrl?.toString() ?: ""
            }
        }
        else -> ""
    }

    val isCloudSyncConfigured = backupConfig.primaryAccount.isLinked || backupConfig.secondaryAccount.isLinked
    val effectiveLastSyncTime = if (isCloudSyncConfigured) {
        maxOf(backupConfig.primaryAccount.lastSyncTimestamp, backupConfig.secondaryAccount.lastSyncTimestamp, backupConfig.lastSyncTimestamp)
    } else 0L

    val lastSyncFormatted = remember(effectiveLastSyncTime, languageMode, isCloudSyncConfigured) {
        if (!isCloudSyncConfigured) {
            if (languageMode == LanguageMode.BANGLA) "কনফিগার করা হয়নি" else "Not configured"
        } else {
            com.example.util.DateUtils.formatSyncTimestamp(effectiveLastSyncTime, languageMode)
        }
    }

    val context = androidx.compose.ui.platform.LocalContext.current
    val themeConfig by ThemePreferences.getInstance(context).themeConfig.collectAsStateWithLifecycle()
    val isSystemDark = androidx.compose.foundation.isSystemInDarkTheme()
    val isDarkModeActive = when (themeConfig.mode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK, ThemeMode.AMOLED_NIGHT -> true
        ThemeMode.SYSTEM -> isSystemDark
    }

    val headerBgColor = if (isDarkModeActive) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        MaterialTheme.colorScheme.primary
    }

    val headerTextColor = if (isDarkModeActive) {
        MaterialTheme.colorScheme.onSurface
    } else {
        Color.White
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Drawer Header with Theme Color and Gold Coin Taka Emblem
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(headerBgColor)
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            shadowElevation = 2.dp,
                            color = Color.Transparent,
                            modifier = Modifier.size(30.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.app_icon_512),
                                contentDescription = LanguageHelper.getString("app_name", languageMode),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        }
                        Text(
                            text = LanguageHelper.getString("app_name", languageMode),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = headerTextColor
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = headerTextColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "v3.6",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = headerTextColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                HorizontalDivider(
                    color = headerTextColor.copy(alpha = 0.38f),
                    thickness = 2.dp,
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                // Profile Picture, Name, and Cloud Account Section (from Drive 1 / Drive 2)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp, horizontal = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Profile Avatar (Photo with Circle Shape or Fallback Avatar)
                    Surface(
                        shape = CircleShape,
                        color = if (isDarkModeActive) MaterialTheme.colorScheme.surface.copy(alpha = 0.55f) else Color.White.copy(alpha = 0.22f),
                        modifier = Modifier.size(46.dp)
                    ) {
                        if (drivePhotoUrl.isNotBlank()) {
                            AsyncImage(
                                model = drivePhotoUrl,
                                contentDescription = driveDisplayName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                            )
                        } else {
                            Box(contentAlignment = Alignment.Center) {
                                if (isAnyDriveActive && driveDisplayName.isNotBlank() && driveDisplayName.firstOrNull()?.isLetter() == true) {
                                    Text(
                                        text = driveDisplayName.take(1).uppercase(),
                                        fontSize = 19.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = headerTextColor
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.AccountCircle,
                                        contentDescription = null,
                                        tint = headerTextColor,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = driveProviderName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = headerTextColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = driveDisplayName,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = headerTextColor.copy(alpha = 0.95f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (driveEmail.isNotBlank()) {
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                text = driveEmail,
                                fontSize = 11.5.sp,
                                color = headerTextColor.copy(alpha = 0.80f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = headerTextColor.copy(alpha = 0.18f))
                Spacer(modifier = Modifier.height(10.dp))

                // Day / Night / Auto Mode Toggle + Quick Theme Palette Button
                var showQuickThemeDialog by remember { mutableStateOf(false) }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Day / Night / Auto Mode Toggle Pill
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isDarkModeActive) Color.Black.copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.22f),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(2.dp)
                        ) {
                            // Light (Day)
                            val isLight = themeConfig.mode == ThemeMode.LIGHT
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isLight) (if (isDarkModeActive) MaterialTheme.colorScheme.surface else Color.White) else Color.Transparent,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        ThemePreferences.getInstance(context).setMode(ThemeMode.LIGHT)
                                    }
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LightMode,
                                        contentDescription = "Day",
                                        tint = if (isLight) Color(0xFFE65100) else headerTextColor.copy(alpha = 0.85f),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "দিন" else "Day",
                                        fontSize = 10.sp,
                                        fontWeight = if (isLight) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isLight) (if (isDarkModeActive) MaterialTheme.colorScheme.onSurface else Color.Black) else headerTextColor.copy(alpha = 0.9f)
                                    )
                                }
                            }

                            // Dark (Night)
                            val isDark = themeConfig.mode == ThemeMode.DARK || themeConfig.mode == ThemeMode.AMOLED_NIGHT
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isDark) (if (isDarkModeActive) MaterialTheme.colorScheme.primaryContainer else Color.White) else Color.Transparent,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        ThemePreferences.getInstance(context).setMode(ThemeMode.DARK)
                                    }
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.DarkMode,
                                        contentDescription = "Night",
                                        tint = if (isDark) MaterialTheme.colorScheme.primary else headerTextColor.copy(alpha = 0.85f),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "রাত" else "Night",
                                        fontSize = 10.sp,
                                        fontWeight = if (isDark) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isDark) (if (isDarkModeActive) MaterialTheme.colorScheme.onPrimaryContainer else Color.Black) else headerTextColor.copy(alpha = 0.9f)
                                    )
                                }
                            }

                            // Auto (System)
                            val isAuto = themeConfig.mode == ThemeMode.SYSTEM
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isAuto) (if (isDarkModeActive) MaterialTheme.colorScheme.surface else Color.White) else Color.Transparent,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        ThemePreferences.getInstance(context).setMode(ThemeMode.SYSTEM)
                                    }
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.BrightnessAuto,
                                        contentDescription = "Auto",
                                        tint = if (isAuto) MaterialTheme.colorScheme.primary else headerTextColor.copy(alpha = 0.85f),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "অটো" else "Auto",
                                        fontSize = 10.sp,
                                        fontWeight = if (isAuto) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isAuto) (if (isDarkModeActive) MaterialTheme.colorScheme.onSurface else Color.Black) else headerTextColor.copy(alpha = 0.9f)
                                    )
                                }
                            }
                        }
                    }

                    // 2. Quick Theme Palette Button
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = headerTextColor.copy(alpha = 0.15f),
                        modifier = Modifier
                            .height(28.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { showQuickThemeDialog = true }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = "Theme",
                                tint = headerTextColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "থিম" else "Theme",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = headerTextColor
                            )
                        }
                    }
                }

                if (showQuickThemeDialog) {
                    QuickThemeDialog(
                        themeConfig = themeConfig,
                        languageMode = languageMode,
                        onDismiss = { showQuickThemeDialog = false },
                        onSelectPalette = {
                            ThemePreferences.getInstance(context).setPalette(it)
                            showQuickThemeDialog = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // State of open groups: "remain open until close" - groups persist their opened state
        // until explicitly toggled closed by user.
        var openGroups by rememberSaveable {
            mutableStateOf(
                listOfNotNull(
                    if (currentView in listOf(AppView.ACCOUNTS, AppView.RM_MANAGER, AppView.PAYMENT_SOURCE)) "accounts_sources" else null,
                    if (currentView in listOf(AppView.CATEGORIES, AppView.BUDGET_MAKER)) "categories_budget" else null,
                    if (currentView in listOf(AppView.SAVINGS_GOALS, AppView.WISHLIST)) "goals_wishlist" else null,
                    if (currentView == AppView.TRASH) "rest" else null
                )
            )
        }

        // Keep active group open if view changes from external navigation
        LaunchedEffect(currentView) {
            val activeGroup = when (currentView) {
                AppView.ACCOUNTS, AppView.RM_MANAGER, AppView.PAYMENT_SOURCE -> "accounts_sources"
                AppView.CATEGORIES, AppView.BUDGET_MAKER -> "categories_budget"
                AppView.SAVINGS_GOALS, AppView.WISHLIST -> "goals_wishlist"
                AppView.TRASH -> "rest"
                else -> null
            }
            if (activeGroup != null && activeGroup !in openGroups) {
                openGroups = openGroups + activeGroup
            }
        }

        val isAccountsOpen = "accounts_sources" in openGroups
        val isCategoriesOpen = "categories_budget" in openGroups
        val isGoalsOpen = "goals_wishlist" in openGroups
        val isRestOpen = "rest" in openGroups

        val toggleGroup: (String) -> Unit = { groupId ->
            openGroups = if (groupId in openGroups) openGroups - groupId else openGroups + groupId
        }

        // 1. Main Dashboard
        DrawerItemRow(
            title = if (languageMode == LanguageMode.BANGLA) "মূল ড্যাশবোর্ড" else "Main",
            icon = Icons.Default.Dashboard,
            iconTint = MaterialTheme.colorScheme.primary,
            isSelected = currentView == AppView.DASHBOARD,
            onClick = { onSelectView(AppView.DASHBOARD) }
        )

        Spacer(modifier = Modifier.height(2.dp))

        // 2. Accounts, RM Manager, Payment Source (Accounts & Sources)
        val hasActiveAccounts = currentView in listOf(AppView.ACCOUNTS, AppView.RM_MANAGER, AppView.PAYMENT_SOURCE)
        DrawerGroupHeader(
            title = if (languageMode == LanguageMode.BANGLA) "অ্যাকাউন্ট ও সোর্স" else "Accounts & Sources",
            icon = Icons.Default.AccountBalanceWallet,
            iconTint = MaterialTheme.colorScheme.primary,
            isOpen = isAccountsOpen,
            hasActiveChild = hasActiveAccounts,
            badge = "$accountsCount",
            onToggle = { toggleGroup("accounts_sources") },
            testTag = "drawer_group_accounts"
        )
        AnimatedVisibility(
            visible = isAccountsOpen,
            enter = expandVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
            exit = shrinkVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 18.dp, end = 4.dp, bottom = 2.dp)
            ) {
                DrawerChildItemRow(
                    title = LanguageHelper.getString("accounts", languageMode),
                    icon = Icons.Default.AccountBalanceWallet,
                    iconTint = MaterialTheme.colorScheme.primary,
                    badge = "$accountsCount",
                    isSelected = currentView == AppView.ACCOUNTS,
                    onClick = { onSelectView(AppView.ACCOUNTS) }
                )
                DrawerChildItemRow(
                    title = LanguageHelper.getString("rm_manager", languageMode).ifEmpty { "RM Manager" },
                    icon = Icons.Default.AccountBalance,
                    iconTint = MaterialTheme.colorScheme.primary,
                    isSelected = currentView == AppView.RM_MANAGER,
                    onClick = { onSelectView(AppView.RM_MANAGER) }
                )
                DrawerChildItemRow(
                    title = LanguageHelper.getString("payment_source", languageMode),
                    icon = Icons.Default.Payments,
                    iconTint = SolidTransfer,
                    isSelected = currentView == AppView.PAYMENT_SOURCE,
                    onClick = { onSelectView(AppView.PAYMENT_SOURCE) }
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // 3. Categories, Budget Maker (Categories & Budget)
        val hasActiveCategories = currentView in listOf(AppView.CATEGORIES, AppView.BUDGET_MAKER)
        DrawerGroupHeader(
            title = if (languageMode == LanguageMode.BANGLA) "ক্যাটাগরি ও বাজেট" else "Categories & Budget",
            icon = Icons.Default.Category,
            iconTint = SolidExpense,
            isOpen = isCategoriesOpen,
            hasActiveChild = hasActiveCategories,
            badge = "$categoriesCount",
            onToggle = { toggleGroup("categories_budget") },
            testTag = "drawer_group_categories"
        )
        AnimatedVisibility(
            visible = isCategoriesOpen,
            enter = expandVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
            exit = shrinkVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 18.dp, end = 4.dp, bottom = 2.dp)
            ) {
                DrawerChildItemRow(
                    title = LanguageHelper.getString("categories", languageMode),
                    icon = Icons.Default.Category,
                    iconTint = SolidExpense,
                    badge = "$categoriesCount",
                    isSelected = currentView == AppView.CATEGORIES,
                    onClick = { onSelectView(AppView.CATEGORIES) }
                )
                DrawerChildItemRow(
                    title = LanguageHelper.getString("budget_maker", languageMode),
                    icon = Icons.Default.Calculate,
                    iconTint = MaterialTheme.colorScheme.primary,
                    isSelected = currentView == AppView.BUDGET_MAKER,
                    onClick = { onSelectView(AppView.BUDGET_MAKER) }
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // 4. Savings Goals and Wishlist (Goals & Wishlist)
        val hasActiveGoals = currentView in listOf(AppView.SAVINGS_GOALS, AppView.WISHLIST)
        val goalsTotalCount = savingsSummary.activeGoalsCount + activeWishlistCount
        DrawerGroupHeader(
            title = if (languageMode == LanguageMode.BANGLA) "লক্ষ্য ও উইশলিস্ট" else "Goals & Wishlist",
            icon = Icons.Default.Savings,
            iconTint = MaterialTheme.colorScheme.primary,
            isOpen = isGoalsOpen,
            hasActiveChild = hasActiveGoals,
            badge = if (goalsTotalCount > 0) "$goalsTotalCount" else null,
            onToggle = { toggleGroup("goals_wishlist") },
            testTag = "drawer_group_goals"
        )
        AnimatedVisibility(
            visible = isGoalsOpen,
            enter = expandVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
            exit = shrinkVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 18.dp, end = 4.dp, bottom = 2.dp)
            ) {
                DrawerChildItemRow(
                    title = LanguageHelper.getString("savings_goals", languageMode).ifEmpty { "Savings Goals" },
                    icon = Icons.Default.Savings,
                    iconTint = MaterialTheme.colorScheme.primary,
                    badge = if (savingsSummary.activeGoalsCount > 0) "${savingsSummary.activeGoalsCount}" else null,
                    isSelected = currentView == AppView.SAVINGS_GOALS,
                    onClick = { onSelectView(AppView.SAVINGS_GOALS) }
                )
                DrawerChildItemRow(
                    title = LanguageHelper.getString("wishlist", languageMode).ifEmpty { "Wishlist" },
                    icon = Icons.Default.ShoppingBag,
                    iconTint = MaterialTheme.colorScheme.primary,
                    badge = if (activeWishlistCount > 0) "$activeWishlistCount" else null,
                    isSelected = currentView == AppView.WISHLIST,
                    onClick = { onSelectView(AppView.WISHLIST) }
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // 5. Settings
        DrawerItemRow(
            title = LanguageHelper.getString("settings", languageMode).ifEmpty { "Settings" },
            icon = Icons.Default.Settings,
            iconTint = MaterialTheme.colorScheme.primary,
            isSelected = currentView == AppView.SETTINGS,
            onClick = { onSelectView(AppView.SETTINGS) }
        )

        Spacer(modifier = Modifier.height(2.dp))

        // 6. Rest (Quick Sync, Demo, Trash)
        val hasActiveRest = currentView == AppView.TRASH || isDemoMode
        DrawerGroupHeader(
            title = if (languageMode == LanguageMode.BANGLA) "অন্যান্য ও টুলস" else "Rest & Tools",
            icon = Icons.Default.Tune,
            iconTint = MaterialTheme.colorScheme.outline,
            isOpen = isRestOpen,
            hasActiveChild = hasActiveRest,
            badge = if (trashedItems.isNotEmpty()) "${trashedItems.size}" else null,
            onToggle = { toggleGroup("rest") },
            testTag = "drawer_group_rest"
        )
        AnimatedVisibility(
            visible = isRestOpen,
            enter = expandVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
            exit = shrinkVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 18.dp, end = 4.dp, bottom = 2.dp)
            ) {
                // Quick Sync
                NavigationDrawerItem(
                    label = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "কুইক সিঙ্ক" else "Quick Sync",
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.5.sp
                                )
                                Text(
                                    text = "Last: $lastSyncFormatted",
                                    fontSize = 10.5.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                            Icon(
                                Icons.Default.Sync,
                                contentDescription = "Sync",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    icon = { Icon(Icons.Default.Sync, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) },
                    selected = false,
                    onClick = { viewModel.triggerQuickSync() },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 1.dp)
                )

                // Demo Mode Switch
                NavigationDrawerItem(
                    label = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f, fill = false)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "ডেমো" else "Demo",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.5.sp
                                    )
                                    if (isDemoMode) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                text = if (languageMode == LanguageMode.BANGLA) "চালু" else "ON",
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.tertiary,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = if (isDemoMode) {
                                        if (languageMode == LanguageMode.BANGLA) "নমুনা ডাটা • ব্যাকআপ হবে না" else "Sample data • No backup"
                                    } else {
                                        if (languageMode == LanguageMode.BANGLA) "নমুনা ডাটা দেখতে চালু করুন" else "Toggle on to explore"
                                    },
                                    fontSize = 10.5.sp,
                                    color = if (isDemoMode) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline
                                )
                            }
                            Switch(
                                checked = isDemoMode,
                                onCheckedChange = { viewModel.setDemoMode(it) },
                                modifier = Modifier
                                    .scale(0.8f)
                                    .testTag("drawer_demo_switch")
                            )
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Science,
                            contentDescription = "Demo Mode",
                            tint = if (isDemoMode) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    selected = isDemoMode,
                    onClick = { viewModel.setDemoMode(!isDemoMode) },
                    shape = RoundedCornerShape(10.dp),
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.28f),
                        unselectedContainerColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 1.dp)
                        .testTag("drawer_demo_item")
                )

                // Trash
                DrawerChildItemRow(
                    title = if (languageMode == LanguageMode.BANGLA) "ট্র্যাশ ও রিসাইকেল বিন" else "Trash",
                    icon = Icons.Default.DeleteOutline,
                    iconTint = if (trashedItems.isNotEmpty()) SolidExpense else MaterialTheme.colorScheme.outline,
                    badge = if (trashedItems.isNotEmpty()) "${trashedItems.size}" else null,
                    isSelected = currentView == AppView.TRASH,
                    onClick = { onSelectView(AppView.TRASH) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun DrawerGroupHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    isOpen: Boolean,
    hasActiveChild: Boolean = false,
    badge: String? = null,
    onToggle: () -> Unit,
    testTag: String = ""
) {
    val arrowRotation by animateFloatAsState(
        targetValue = if (isOpen) 180f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "arrow_rotation"
    )

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (hasActiveChild && !isOpen) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        } else {
            Color.Transparent
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onToggle)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .background(
                            color = if (hasActiveChild) iconTint.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (hasActiveChild) iconTint else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(17.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (hasActiveChild || isOpen) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (hasActiveChild) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (badge != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
                        Text(
                            text = badge,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isOpen) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier
                        .size(20.dp)
                        .rotate(arrowRotation)
                )
            }
        }
    }
}

@Composable
private fun DrawerChildItemRow(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    badge: String? = null,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        label = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 13.5.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
                if (badge != null) {
                    Text(
                        text = badge,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        },
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else iconTint,
                modifier = Modifier.size(18.dp)
            )
        },
        selected = isSelected,
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            selectedTextColor = MaterialTheme.colorScheme.primary,
            unselectedContainerColor = Color.Transparent
        ),
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 1.dp)
    )
}

@Composable
private fun DrawerItemRow(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    badge: String? = null,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        label = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                )
                if (badge != null) {
                    Text(
                        text = badge,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        },
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else iconTint,
                modifier = Modifier.size(20.dp)
            )
        },
        selected = isSelected,
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            selectedTextColor = MaterialTheme.colorScheme.primary
        ),
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
    )
}

@Composable
private fun ScreenRouter(
    currentView: AppView,
    viewModel: BudgetViewModel,
    overview: com.example.data.repository.FinancialOverview,
    accountsWithBalances: List<com.example.data.repository.AccountWithBalance>,
    accountCalcConfig: com.example.util.AccountCalcConfig,
    allAccounts: List<Account>,
    allCategories: List<Category>,
    transactionsWithDetails: List<com.example.data.model.TransactionWithDetails>,
    itemImageCacheMap: Map<String, com.example.data.model.ItemImageCache> = emptyMap(),
    recurringBills: List<com.example.data.model.RecurringBillWithDetails>,
    languageMode: LanguageMode,
    backupUiState: com.example.ui.viewmodel.BackupUiState,
    monthlyBudgets: List<com.example.data.model.MonthlyBudget>,
    selectedBudgetYear: Int,
    selectedBudgetMonth: Int,
    isDemoMode: Boolean = false,
    onOpenDrawer: () -> Unit = {},
    onExitDemoMode: () -> Unit = {},
    onBack: () -> Unit = {},
    onNavigate: (AppView) -> Unit,
    onEditTransaction: (Transaction) -> Unit,
    onAddTransactionWithType: (TransactionType) -> Unit,
    onAddTransactionWithCategory: (Category) -> Unit,
    onAddTransactionWithAccount: (Account) -> Unit,
    onAddTransactionPrefilled: (type: TransactionType, categoryId: Long?, subCategoryId: Long?, amount: Double?, note: String?, pendingWishlistId: Long?) -> Unit = { _, _, _, _, _, _ -> },
    onExecuteTransfer: (Account, Account, Double) -> Unit = { _, _, _ -> },
    onAddTransactionWithAccountAndType: (Account, TransactionType) -> Unit = { _, _ -> },
    onExecuteRepayTransfer: (Account?, String?, Double) -> Unit = { _, _, _ -> },
    onAddAccount: (Long?) -> Unit,
    onEditAccount: (Account) -> Unit,
    onAddCategory: (CategoryType, Long?) -> Unit,
    onEditCategory: (Category) -> Unit,
    onOpenTabCustomizer: () -> Unit = {},
    onOpenThemeFontSettings: () -> Unit = {},
    onOpenAutofillSettings: () -> Unit = {},
    onAccountClick: ((Account) -> Unit)? = null,
    dashboardConfig: com.example.util.DashboardConfig = com.example.util.DashboardConfig(),
    paymentSourceConfig: com.example.util.PaymentSourceConfig = com.example.util.PaymentSourceConfig(),
    initialBudgetSearchQuery: String? = null,
    initialAccountsFilter: AccountViewHierarchyFilter = AccountViewHierarchyFilter.ALL
) {
    when (currentView) {
        AppView.DASHBOARD -> DashboardScreen(
            overview = overview,
            accountsWithBalances = accountsWithBalances,
            accountCalcConfig = accountCalcConfig,
            recentTransactions = transactionsWithDetails,
            itemImageCacheMap = itemImageCacheMap,
            allCategories = allCategories,
            monthlyBudgets = monthlyBudgets,
            dashboardConfig = dashboardConfig,
            languageMode = languageMode,
            isDemoMode = isDemoMode,
            onOpenDrawer = onOpenDrawer,
            onExitDemoMode = onExitDemoMode,
            onAddTransactionClick = onAddTransactionWithType,
            onTransactionClick = onEditTransaction,
            onViewAllTransactionsClick = { onNavigate(AppView.LEDGER) },
            onNavigateToCashFlow = { onNavigate(AppView.CASH_FLOW) },
            onAccountClick = { acc ->
                if (onAccountClick != null) onAccountClick(acc) else onEditAccount(acc)
            },
            onToggleCardVisibility = { card, visible -> viewModel.toggleDashboardCard(card, visible) },
            onReorderCards = { from, to -> viewModel.moveDashboardCard(from, to) },
            onUpdateDailySummarySettings = { m, p, ct, sv, sa, dp, sc, scs -> viewModel.setDailySummarySettings(m, p, ct, sv, sa, dp, sc, scs) },
            onUpdateBudgetSummarySettings = { s, t, mc, sp, tp -> viewModel.setBudgetSummarySettings(s, t, mc, sp, tp) },
            onUpdateCalendarSettings = { dm, si, se -> viewModel.setCalendarSettings(dm, si, se) },
            onUpdateFavoriteAccounts = { favs -> viewModel.setFavoriteAccounts(favs) },
            onResetDashboardDefaults = { viewModel.resetDashboardDefaults() },
            detectedBackups = viewModel.detectedBackups.collectAsStateWithLifecycle().value,
            showRestoreBanner = viewModel.showRestoreBanner.collectAsStateWithLifecycle().value,
            onRestoreDetectedBackup = { backup, isMerge -> viewModel.restoreDetectedBackup(backup, isMerge) },
            onDismissRestoreBanner = { backupId -> viewModel.dismissRestoreBanner(backupId) },
            onScanBackups = { viewModel.scanForPreviousBackups() },
            syncLiveStatus = viewModel.syncLiveState.collectAsStateWithLifecycle().value,
            onTriggerSync = { viewModel.triggerInstantSync() }
        )
        AppView.LEDGER -> {
            val securityConfig = viewModel.securityConfig.collectAsStateWithLifecycle().value
            LedgerScreen(
                transactions = transactionsWithDetails,
                languageMode = languageMode,
                allCategories = allCategories,
                allAccounts = allAccounts,
                accountsWithBalances = accountsWithBalances,
                itemImageCacheMap = itemImageCacheMap,
                securityConfig = securityConfig,
                onOpenDrawer = onOpenDrawer,
                onVerifyPin = { viewModel.verifySecurityPin(it) },
                onAddTransactionClick = { onAddTransactionWithType(TransactionType.EXPENSE) },
                onTransactionClick = onEditTransaction,
                onAccountClick = onAccountClick,
                onUpdateTransactions = { txList -> viewModel.updateTransactions(txList) },
                onDeleteTransactions = { txList -> viewModel.deleteTransactions(txList) }
            )
        }
        AppView.CASH_FLOW -> {
            CashFlowScreen(
                transactionsWithDetails = transactionsWithDetails,
                allAccounts = allAccounts,
                accountsWithBalances = accountsWithBalances,
                allCategories = allCategories,
                itemImageCacheMap = itemImageCacheMap,
                languageMode = languageMode,
                onOpenDrawer = onOpenDrawer,
                onTransactionClick = onEditTransaction,
                onAddTransactionClick = { onAddTransactionWithType(TransactionType.EXPENSE) }
            )
        }
        AppView.BALANCE_SHEET -> {
            val rawTransactions = remember(transactionsWithDetails) { transactionsWithDetails.map { it.transaction } }
            BalanceSheetScreen(
                accounts = allAccounts,
                transactions = rawTransactions,
                languageMode = languageMode,
                onOpenDrawer = onOpenDrawer,
                onAddAccountClick = { onAddAccount(null) },
                onAddSubAccountClick = { parent -> onAddAccount(parent.id) },
                onEditAccountClick = onEditAccount,
                onAddTransactionClick = { onAddTransactionWithType(TransactionType.EXPENSE) },
                onAccountClick = onAccountClick,
                accountCalcConfig = accountCalcConfig,
                onToggleActiveStatus = { acc, active ->
                    viewModel.saveAccount(acc.copy(isActive = active))
                    if (acc.parentId == null) {
                        val childAccounts = allAccounts.filter { it.parentId == acc.id }
                        if (childAccounts.isNotEmpty()) {
                            viewModel.updateAccounts(childAccounts.map { it.copy(isActive = active) })
                        }
                    } else if (active) {
                        val parent = allAccounts.firstOrNull { it.id == acc.parentId }
                        if (parent != null && !parent.isActive) {
                            viewModel.saveAccount(parent.copy(isActive = true))
                        }
                    }
                },
                onToggleIncludeStatus = { acc, isIncluded ->
                    viewModel.setAccountIncludeStatus(acc.id, isIncluded)
                },
                onSaveCalculationSetting = { acc, isIncluded, adjustment ->
                    viewModel.setAccountCalcSetting(acc.id, isIncluded, adjustment)
                },
                onResetAccountCalculation = { acc ->
                    viewModel.resetAccountCalculation(acc.id)
                }
            )
        }
        AppView.PAYMENT_SOURCE -> {
            val rawBills = remember(recurringBills) { recurringBills.map { it.bill } }
            PaymentSourceScreen(
                allAccounts = allAccounts,
                accountsWithBalances = accountsWithBalances,
                allCategories = allCategories,
                monthlyBudgets = monthlyBudgets,
                allTransactions = transactionsWithDetails,
                recurringBills = rawBills,
                selectedYear = selectedBudgetYear,
                selectedMonth = selectedBudgetMonth,
                languageMode = languageMode,
                paymentSourceConfig = paymentSourceConfig,
                onOpenDrawer = onOpenDrawer,
                onPrevMonth = { viewModel.prevBudgetMonth() },
                onNextMonth = { viewModel.nextBudgetMonth() },
                onSetCurrentMonth = {
                    val cal = java.util.Calendar.getInstance()
                    viewModel.setBudgetYearMonth(cal.get(java.util.Calendar.YEAR), cal.get(java.util.Calendar.MONTH) + 1)
                },
                onExecuteTransfer = { fromId, toId, amt, note ->
                    val fromAcc = allAccounts.firstOrNull { it.id == fromId }
                    val toAcc = allAccounts.firstOrNull { it.id == toId }
                    if (fromAcc != null && toAcc != null) {
                        onExecuteTransfer(fromAcc, toAcc, amt)
                    }
                },
                onAddTransactionWithAccount = { accId, txType ->
                    val acc = allAccounts.firstOrNull { it.id == accId }
                    if (acc != null) {
                        onAddTransactionWithAccountAndType(acc, txType)
                    }
                },
                onEditAccount = onEditAccount,
                onSaveCategoryAllocations = { categoryId, allocMap ->
                    viewModel.saveCategoryAccountAllocations(categoryId, allocMap)
                },
                onSaveOtherAccountAllocations = { otherAccId, allocMap ->
                    viewModel.saveOtherAccountAllocations(otherAccId, allocMap)
                },
                onSetPaymentSourceAccountIds = { ids ->
                    viewModel.setPaymentSourceAccountIds(ids)
                },
                onSaveAccountObligation = { ob ->
                    viewModel.saveAccountObligation(ob)
                },
                onDeleteAccountObligation = { id ->
                    viewModel.deleteAccountObligation(id)
                },
                onAccountClick = { accId ->
                    val acc = allAccounts.firstOrNull { it.id == accId }
                    if (acc != null) {
                        if (onAccountClick != null) onAccountClick(acc) else onEditAccount(acc)
                    }
                }
            )
        }
        AppView.BUDGET -> BudgetTrackingScreen(
            viewModel = viewModel,
            allCategories = allCategories,
            allAccounts = allAccounts,
            accountsWithBalances = accountsWithBalances,
            transactionsWithDetails = transactionsWithDetails,
            monthlyBudgets = monthlyBudgets,
            selectedYear = selectedBudgetYear,
            selectedMonth = selectedBudgetMonth,
            languageMode = languageMode,
            onOpenDrawer = onOpenDrawer,
            onNavigateToBudgetMaker = { onNavigate(AppView.BUDGET_MAKER) },
            onAddTransactionWithCategory = onAddTransactionWithCategory,
            onEditTransaction = onEditTransaction,
            onAccountClick = onAccountClick
        )
        AppView.BUDGET_MAKER -> BudgetScreen(
            viewModel = viewModel,
            allCategories = allCategories,
            allAccounts = allAccounts,
            accountsWithBalances = accountsWithBalances,
            transactionsWithDetails = transactionsWithDetails,
            monthlyBudgets = monthlyBudgets,
            selectedYear = selectedBudgetYear,
            selectedMonth = selectedBudgetMonth,
            languageMode = languageMode,
            onOpenDrawer = onOpenDrawer,
            onBack = onBack,
            onEditTransaction = onEditTransaction,
            onAddTransactionWithCategory = onAddTransactionWithCategory,
            onAddTransactionWithAccount = onAddTransactionWithAccount,
            onAccountClick = onAccountClick,
            initialSearchQuery = initialBudgetSearchQuery ?: ""
        )
        AppView.SAVINGS_GOALS -> {
            val goalsWithDetails by viewModel.savingsGoalsWithDetails.collectAsStateWithLifecycle()
            val savingsSummary by viewModel.savingsSummary.collectAsStateWithLifecycle()
            SavingsGoalsScreen(
                goalsWithDetails = goalsWithDetails,
                savingsSummary = savingsSummary,
                accountsWithBalances = accountsWithBalances,
                languageMode = languageMode,
                onSaveGoal = { goal, allocs -> viewModel.saveSavingsGoal(goal, allocs) },
                onDeleteGoal = { id -> viewModel.deleteSavingsGoal(id) },
                onToggleCompleted = { id, isComp -> viewModel.toggleSavingsGoalCompleted(id, isComp) },
                onUpdateAllocation = { gId, aId, amt -> viewModel.updateGoalAllocation(gId, aId, amt) },
                onOpenDrawer = onOpenDrawer
            )
        }
        AppView.WISHLIST -> {
            val wishlistItemsWithDetails by viewModel.wishlistWithDetails.collectAsStateWithLifecycle()
            WishlistScreen(
                wishlistItemsWithDetails = wishlistItemsWithDetails,
                categories = allCategories,
                languageMode = languageMode,
                onSaveWishlistItem = { item -> viewModel.saveWishlistItem(item) },
                onDeleteWishlistItem = { id -> viewModel.deleteWishlistItem(id) },
                onTogglePurchased = { id, isPurchased -> viewModel.toggleWishlistPurchased(id, isPurchased) },
                onAddToBudget = { item, year, month, amt -> viewModel.addWishlistToMonthBudget(item, year, month, amt) },
                onConvertToGoal = { item, goalName, targetAmt, deadline, notes ->
                    viewModel.convertWishlistToSavingsGoal(item, deadline, goalName, targetAmt, notes)
                },
                onRecordPurchase = { item ->
                    onAddTransactionPrefilled(
                        TransactionType.EXPENSE,
                        item.categoryId,
                        item.subCategoryId,
                        if (item.estimatedAmount > 0.0) item.estimatedAmount else null,
                        item.title,
                        item.id
                    )
                },
                onOpenDrawer = onOpenDrawer
            )
        }
        AppView.REPORTS -> ReportsScreen(
            overview = overview,
            accountsWithBalances = accountsWithBalances,
            transactions = transactionsWithDetails,
            categories = allCategories,
            allAccounts = allAccounts,
            languageMode = languageMode,
            onOpenDrawer = onOpenDrawer,
            onAddTransactionWithCategory = onAddTransactionWithCategory,
            onEditTransaction = onEditTransaction,
            onAccountClick = onAccountClick
        )
        AppView.LABELS -> LabelsScreen(
            transactions = transactionsWithDetails,
            categories = allCategories,
            accounts = allAccounts,
            languageMode = languageMode,
            onOpenDrawer = onOpenDrawer,
            onTransactionClick = onEditTransaction,
            onAddTransactionClick = onAddTransactionWithType
        )
        AppView.ITEMS_SUMMARY -> ItemsScreen(
            transactions = transactionsWithDetails,
            categories = allCategories,
            accounts = allAccounts,
            languageMode = languageMode,
            onOpenDrawer = onOpenDrawer,
            onTransactionClick = onEditTransaction,
            onAddTransactionClick = onAddTransactionWithType
        )
        AppView.RECURRING_BILLS -> RecurringBillsScreen(
            viewModel = viewModel,
            bills = recurringBills,
            languageMode = languageMode,
            onOpenDrawer = onOpenDrawer
        )
        AppView.ACCOUNTS -> AccountsScreen(
            accountsWithBalances = accountsWithBalances,
            accountCalcConfig = accountCalcConfig,
            languageMode = languageMode,
            allTransactions = transactionsWithDetails,
            onOpenDrawer = onOpenDrawer,
            onAddAccountClick = { onAddAccount(null) },
            onAddSubAccountClick = { parent -> onAddAccount(parent.id) },
            onEditAccountClick = onEditAccount,
            onAccountClick = onAccountClick,
            initialHierarchyFilter = initialAccountsFilter,
            onToggleActiveStatus = { acc, active ->
                viewModel.saveAccount(acc.copy(isActive = active))
                if (acc.parentId == null) {
                    val childAccounts = allAccounts.filter { it.parentId == acc.id }
                    if (childAccounts.isNotEmpty()) {
                        viewModel.updateAccounts(childAccounts.map { it.copy(isActive = active) })
                    }
                } else if (active) {
                    val parent = allAccounts.firstOrNull { it.id == acc.parentId }
                    if (parent != null && !parent.isActive) {
                        viewModel.saveAccount(parent.copy(isActive = true))
                    }
                }
            },
            onToggleIncludeStatus = { acc, isIncluded ->
                viewModel.setAccountIncludeStatus(acc.id, isIncluded)
                if (acc.parentId == null) {
                    val childAccounts = allAccounts.filter { it.parentId == acc.id }
                    for (child in childAccounts) {
                        viewModel.setAccountIncludeStatus(child.id, isIncluded)
                    }
                }
            },
            onSaveCalculationSetting = { acc, isIncluded, adjustment ->
                viewModel.setAccountCalcSetting(acc.id, isIncluded, adjustment)
            },
            onResetAccountCalculation = { acc ->
                viewModel.resetAccountCalculation(acc.id)
            },
            onResetAllCalculations = {
                viewModel.resetAllAccountCalculations()
            },
            onUpdateAccounts = { updatedList ->
                viewModel.updateAccounts(updatedList)
            },
            onDeleteAccounts = { delList ->
                viewModel.deleteAccounts(delList)
            }
        )
        AppView.RM_MANAGER -> RmManagerScreen(
            allAccounts = allAccounts,
            allTransactions = transactionsWithDetails,
            languageMode = languageMode,
            onOpenDrawer = onOpenDrawer,
            onTransactionClick = onEditTransaction,
            onAddTransactionClick = onAddTransactionWithType,
            onExecuteRepayTransfer = onExecuteRepayTransfer,
            onSaveTransaction = { tx ->
                viewModel.saveTransaction(tx)
            },
            onUpdateTransactions = { txList ->
                viewModel.updateTransactions(txList)
            },
            onAddAccountClick = { onAddAccount(null) }
        )
        AppView.CATEGORIES -> CategoriesScreen(
            categories = allCategories,
            languageMode = languageMode,
            initialTab = 0,
            allTransactions = transactionsWithDetails,
            monthlyBudgets = monthlyBudgets,
            onOpenDrawer = onOpenDrawer,
            onAddCategoryClick = { type -> onAddCategory(type, null) },
            onAddSubCategoryClick = { parent -> onAddCategory(parent.type, parent.id) },
            onEditCategoryClick = onEditCategory,
            onToggleActiveStatus = { cat, active ->
                viewModel.saveCategory(cat.copy(isActive = active))
            },
            onUpdateCategories = { updatedList ->
                viewModel.updateCategories(updatedList)
            },
            onDeleteCategories = { delList ->
                viewModel.deleteCategories(delList)
            }
        )
        AppView.EXPENSES -> CategoriesScreen(
            categories = allCategories,
            languageMode = languageMode,
            initialTab = 0,
            allTransactions = transactionsWithDetails,
            monthlyBudgets = monthlyBudgets,
            onOpenDrawer = onOpenDrawer,
            onAddCategoryClick = { type -> onAddCategory(type, null) },
            onAddSubCategoryClick = { parent -> onAddCategory(parent.type, parent.id) },
            onEditCategoryClick = onEditCategory,
            onToggleActiveStatus = { cat, active ->
                viewModel.saveCategory(cat.copy(isActive = active))
            },
            onUpdateCategories = { updatedList ->
                viewModel.updateCategories(updatedList)
            },
            onDeleteCategories = { delList ->
                viewModel.deleteCategories(delList)
            }
        )
        AppView.INCOME -> CategoriesScreen(
            categories = allCategories,
            languageMode = languageMode,
            initialTab = 1,
            allTransactions = transactionsWithDetails,
            monthlyBudgets = monthlyBudgets,
            onOpenDrawer = onOpenDrawer,
            onAddCategoryClick = { type -> onAddCategory(type, null) },
            onAddSubCategoryClick = { parent -> onAddCategory(parent.type, parent.id) },
            onEditCategoryClick = onEditCategory,
            onToggleActiveStatus = { cat, active ->
                viewModel.saveCategory(cat.copy(isActive = active))
            },
            onUpdateCategories = { updatedList ->
                viewModel.updateCategories(updatedList)
            },
            onDeleteCategories = { delList ->
                viewModel.deleteCategories(delList)
            }
        )
        AppView.BACKUP_SYNC -> BackupSyncSettingsScreen(
            viewModel = viewModel,
            languageMode = languageMode,
            backupUiState = backupUiState,
            onNavigateToReset = { onNavigate(AppView.RESET) },
            onBack = onBack
        )
        AppView.SETTINGS -> SettingsScreen(
            viewModel = viewModel,
            languageMode = languageMode,
            onOpenDrawer = onOpenDrawer,
            onBack = onBack,
            onNavigateToBackupSync = { onNavigate(AppView.BACKUP_SYNC) },
            onNavigateToReset = { onNavigate(AppView.RESET) },
            onOpenTabCustomizer = onOpenTabCustomizer,
            onOpenThemeFontSettings = onOpenThemeFontSettings,
            onOpenAutofillSettings = onOpenAutofillSettings
        )
        AppView.TRASH -> TrashScreen(
            viewModel = viewModel,
            languageMode = languageMode,
            onBack = onBack
        )
        AppView.RESET -> ResetScreen(
            viewModel = viewModel,
            languageMode = languageMode,
            onOpenDrawer = onOpenDrawer,
            onBack = onBack
        )
    }
}

private fun getViewTitle(view: AppView, languageMode: LanguageMode): String {
    return when (view) {
        AppView.DASHBOARD -> LanguageHelper.getString("app_name", languageMode)
        AppView.LEDGER -> LanguageHelper.getString("transactions", languageMode)
        AppView.CASH_FLOW -> LanguageHelper.getString("cash_flow", languageMode).ifEmpty { "Cash Flow" }
        AppView.PAYMENT_SOURCE -> LanguageHelper.getString("payment_source", languageMode)
        AppView.WISHLIST -> LanguageHelper.getString("wishlist", languageMode).ifEmpty { "Wishlist" }
        AppView.BALANCE_SHEET -> LanguageHelper.getString("balance_sheet", languageMode)
        AppView.BUDGET -> LanguageHelper.getString("budget", languageMode)
        AppView.BUDGET_MAKER -> LanguageHelper.getString("budget_maker", languageMode)
        AppView.SAVINGS_GOALS -> LanguageHelper.getString("savings_goals", languageMode).ifEmpty { "Savings Goals" }
        AppView.CATEGORIES -> LanguageHelper.getString("categories", languageMode)
        AppView.REPORTS -> LanguageHelper.getString("net_earnings", languageMode)
        AppView.LABELS -> LanguageHelper.getString("labels", languageMode)
        AppView.ITEMS_SUMMARY -> LanguageHelper.getString("items_summary", languageMode)
        AppView.RECURRING_BILLS -> LanguageHelper.getString("reminders", languageMode)
        AppView.ACCOUNTS -> LanguageHelper.getString("accounts", languageMode)
        AppView.RM_MANAGER -> LanguageHelper.getString("rm_manager", languageMode).ifEmpty { "RM Manager" }
        AppView.EXPENSES -> LanguageHelper.getString("expenses", languageMode)
        AppView.INCOME -> LanguageHelper.getString("incomes", languageMode)
        AppView.BACKUP_SYNC -> "Backup, Restore & Sync"
        AppView.SETTINGS -> LanguageHelper.getString("settings", languageMode).ifEmpty { "Settings" }
        AppView.TRASH -> if (languageMode == LanguageMode.BANGLA) "ট্র্যাশ ও রিসাইকেল বিন" else "Trash & Recycle Bin"
        AppView.RESET -> if (languageMode == LanguageMode.BANGLA) "রিসেট ও ডিলিট" else "Reset & Wipe"
    }
}

@Composable
private fun QuickThemeDialog(
    themeConfig: AppThemeConfig,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onSelectPalette: (ThemePalette) -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "থিম নির্বাচন করুন" else "Select Color Theme",
                    fontSize = 18.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Day Themes Section
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "☀️ দিনের থিম (৩টি)" else "☀️ Day Themes (3)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                ThemePalette.entries.filter { !it.isNightTheme }.forEach { palette ->
                    val isSelected = if (themeConfig.mode == ThemeMode.SYSTEM) {
                        themeConfig.dayPalette == palette && themeConfig.customThemeId == null
                    } else {
                        themeConfig.palette == palette && themeConfig.customThemeId == null
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectPalette(palette) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = palette.primaryColor,
                                    shadowElevation = 2.dp,
                                    modifier = Modifier.size(24.dp)
                                ) {}
                                val name = if (languageMode == LanguageMode.BANGLA) palette.displayNameBn else palette.displayNameEn
                                Text(
                                    text = name,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Night Themes Section
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "🌙 রাতের থিম (২টি)" else "🌙 Night Themes (2)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                ThemePalette.entries.filter { it.isNightTheme }.forEach { palette ->
                    val isSelected = if (themeConfig.mode == ThemeMode.SYSTEM) {
                        themeConfig.nightPalette == palette && themeConfig.customThemeId == null
                    } else {
                        themeConfig.palette == palette && themeConfig.customThemeId == null
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectPalette(palette) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = palette.primaryColor,
                                    shadowElevation = 2.dp,
                                    modifier = Modifier.size(24.dp)
                                ) {}
                                val name = if (languageMode == LanguageMode.BANGLA) palette.displayNameBn else palette.displayNameEn
                                Text(
                                    text = name,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text(if (languageMode == LanguageMode.BANGLA) "বাতিল" else "Close")
            }
        }
    )
}

