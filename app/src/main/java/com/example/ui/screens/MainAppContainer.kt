package com.example.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Palette
import androidx.compose.ui.draw.clip
import androidx.compose.ui.zIndex
import com.example.ui.theme.ThemePreferences
import com.example.ui.theme.ThemeMode
import com.example.ui.theme.ThemePalette
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
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
    PAYMENT_SOURCE,
    BALANCE_SHEET,
    ACCOUNTS,
    BUDGET,
    BUDGET_MAKER,
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
    AppTab.PAYMENT_SOURCE -> AppView.PAYMENT_SOURCE
    AppTab.BALANCE_SHEET -> AppView.BALANCE_SHEET
    AppTab.BUDGET -> AppView.BUDGET
    AppTab.NET_EARNINGS -> AppView.REPORTS
    AppTab.LABELS -> AppView.LABELS
    AppTab.ITEMS_SUMMARY -> AppView.ITEMS_SUMMARY
    AppTab.REMINDERS -> AppView.RECURRING_BILLS
}

fun AppView.toAppTab(): AppTab? = when (this) {
    AppView.DASHBOARD -> AppTab.MAIN
    AppView.LEDGER -> AppTab.TRANSACTIONS
    AppView.PAYMENT_SOURCE -> AppTab.PAYMENT_SOURCE
    AppView.BALANCE_SHEET -> AppTab.BALANCE_SHEET
    AppView.BUDGET -> AppTab.BUDGET
    AppView.REPORTS -> AppTab.NET_EARNINGS
    AppView.LABELS -> AppTab.LABELS
    AppView.ITEMS_SUMMARY -> AppTab.ITEMS_SUMMARY
    AppView.RECURRING_BILLS -> AppTab.REMINDERS
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
    viewModel: BudgetViewModel
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

    val headerScrollState = rememberHeaderScrollState()

    val visibleTabs = tabConfig.visibleTabs
    val isTabInVisibleTabs = visibleTabs.any { it.toAppView() == currentView }
    val currentTabIndex = visibleTabs.indexOfFirst { it.toAppView() == currentView }

    val pagerState = rememberPagerState(
        initialPage = if (currentTabIndex >= 0) currentTabIndex else 0,
        pageCount = { visibleTabs.size }
    )

    // Sync currentView when user swipes left/right between tabs (only when actively viewing visible tabs)
    LaunchedEffect(pagerState, isTabInVisibleTabs) {
        if (!isTabInVisibleTabs) return@LaunchedEffect
        snapshotFlow { pagerState.settledPage }.collect { page ->
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
                    pagerState.animateScrollToPage(tabIndex)
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
        AppView.SETTINGS
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
                                            currentView = currentView,
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
                                            currentView = currentView,
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
                                    beyondViewportPageCount = 1,
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
            languageMode = languageMode,
            existingTransaction = editingTransaction,
            onDismiss = { showAddTransactionSheet = false },
            onSave = { tx -> viewModel.saveTransaction(tx) },
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
            onCustomThemeAdded = { name, primary, secondary, income, expense ->
                viewModel.addCustomTheme(name, primary, secondary, income, expense)
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
    if (currentView in listOf(AppView.DASHBOARD, AppView.LEDGER, AppView.LABELS, AppView.ITEMS_SUMMARY, AppView.REPORTS)) {
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
    val backupConfig by viewModel.backupSettingsConfig.collectAsStateWithLifecycle()

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Drawer Header with Theme Color and Gold Coin Taka Emblem
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
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
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFFD700),
                            shadowElevation = 2.dp,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "৳",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF4A3800)
                                )
                            }
                        }
                        Text(
                            text = LanguageHelper.getString("app_name", languageMode),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "v3.6",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = LanguageHelper.getString("net_worth", languageMode),
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Text(
                    text = LanguageHelper.formatCurrency(netWorth, languageMode),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isBalanced) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (isBalanced) SolidIncome else SolidExpense,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isBalanced) "Dr = Cr Balanced" else "Unbalanced",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.22f))
                Spacer(modifier = Modifier.height(10.dp))

                // Day / Night / Auto Mode Toggle + Quick Theme Palette Button
                val context = androidx.compose.ui.platform.LocalContext.current
                val themeConfig by ThemePreferences.getInstance(context).themeConfig.collectAsStateWithLifecycle()
                var showQuickThemeDialog by remember { mutableStateOf(false) }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Day / Night / Auto Mode Toggle Pill
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.Black.copy(alpha = 0.22f),
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
                                color = if (isLight) Color.White else Color.Transparent,
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
                                        tint = if (isLight) Color(0xFFE65100) else Color.White.copy(alpha = 0.85f),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "দিন" else "Day",
                                        fontSize = 10.sp,
                                        fontWeight = if (isLight) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isLight) Color.Black else Color.White.copy(alpha = 0.9f)
                                    )
                                }
                            }

                            // Dark (Night)
                            val isDark = themeConfig.mode == ThemeMode.DARK || themeConfig.mode == ThemeMode.AMOLED_NIGHT
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isDark) Color.White else Color.Transparent,
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
                                        tint = if (isDark) Color(0xFF311B92) else Color.White.copy(alpha = 0.85f),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "রাত" else "Night",
                                        fontSize = 10.sp,
                                        fontWeight = if (isDark) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isDark) Color.Black else Color.White.copy(alpha = 0.9f)
                                    )
                                }
                            }

                            // Auto (System)
                            val isAuto = themeConfig.mode == ThemeMode.SYSTEM
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isAuto) Color.White else Color.Transparent,
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
                                        tint = if (isAuto) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.85f),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "অটো" else "Auto",
                                        fontSize = 10.sp,
                                        fontWeight = if (isAuto) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isAuto) Color.Black else Color.White.copy(alpha = 0.9f)
                                    )
                                }
                            }
                        }
                    }

                    // 2. Quick Theme Palette Button
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White.copy(alpha = 0.22f),
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
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "থিম" else "Theme",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                if (showQuickThemeDialog) {
                    QuickThemeDialog(
                        currentPalette = themeConfig.palette,
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

        // 0. Main Dashboard
        DrawerItemRow(
            title = if (languageMode == LanguageMode.BANGLA) "মূল ড্যাশবোর্ড" else "Main",
            icon = Icons.Default.Dashboard,
            iconTint = MaterialTheme.colorScheme.primary,
            isSelected = currentView == AppView.DASHBOARD,
            onClick = { onSelectView(AppView.DASHBOARD) }
        )

        // 1. Accounts
        DrawerItemRow(
            title = LanguageHelper.getString("accounts", languageMode),
            icon = Icons.Default.AccountBalanceWallet,
            iconTint = MaterialTheme.colorScheme.primary,
            badge = "$accountsCount",
            isSelected = currentView == AppView.ACCOUNTS,
            onClick = { onSelectView(AppView.ACCOUNTS) }
        )

        // 2. Categories
        DrawerItemRow(
            title = LanguageHelper.getString("categories", languageMode),
            icon = Icons.Default.Category,
            iconTint = SolidExpense,
            badge = "$categoriesCount",
            isSelected = currentView == AppView.CATEGORIES,
            onClick = { onSelectView(AppView.CATEGORIES) }
        )

        // 3. Budget Maker
        DrawerItemRow(
            title = LanguageHelper.getString("budget_maker", languageMode),
            icon = Icons.Default.Calculate,
            iconTint = MaterialTheme.colorScheme.primary,
            isSelected = currentView == AppView.BUDGET_MAKER,
            onClick = { onSelectView(AppView.BUDGET_MAKER) }
        )

        // 4. Payment Source
        DrawerItemRow(
            title = LanguageHelper.getString("payment_source", languageMode),
            icon = Icons.Default.Payments,
            iconTint = SolidTransfer,
            isSelected = currentView == AppView.PAYMENT_SOURCE,
            onClick = { onSelectView(AppView.PAYMENT_SOURCE) }
        )

        // 5. Settings
        DrawerItemRow(
            title = LanguageHelper.getString("settings", languageMode).ifEmpty { "Settings" },
            icon = Icons.Default.Settings,
            iconTint = MaterialTheme.colorScheme.primary,
            isSelected = currentView == AppView.SETTINGS,
            onClick = { onSelectView(AppView.SETTINGS) }
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp, horizontal = 16.dp))

        // 6. Quick Sync (with last timestamp)
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
                            fontWeight = FontWeight.Medium
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
            icon = { Icon(Icons.Default.Sync, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            selected = false,
            onClick = { viewModel.triggerQuickSync() },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
        )

        // 7. Trash
        DrawerItemRow(
            title = if (languageMode == LanguageMode.BANGLA) "ট্র্যাশ ও রিসাইকেল বিন" else "Trash",
            icon = Icons.Default.DeleteOutline,
            iconTint = if (trashedItems.isNotEmpty()) SolidExpense else MaterialTheme.colorScheme.outline,
            badge = if (trashedItems.isNotEmpty()) "${trashedItems.size}" else null,
            isSelected = currentView == AppView.TRASH,
            onClick = { onSelectView(AppView.TRASH) }
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
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
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
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
        icon = { Icon(icon, contentDescription = null, tint = iconTint) },
        selected = isSelected,
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
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
    onExecuteTransfer: (Account, Account, Double) -> Unit = { _, _, _ -> },
    onAddTransactionWithAccountAndType: (Account, TransactionType) -> Unit = { _, _ -> },
    onAddAccount: (Long?) -> Unit,
    onEditAccount: (Account) -> Unit,
    onAddCategory: (CategoryType, Long?) -> Unit,
    onEditCategory: (Category) -> Unit,
    onOpenTabCustomizer: () -> Unit = {},
    onOpenThemeFontSettings: () -> Unit = {},
    onOpenAutofillSettings: () -> Unit = {},
    onAccountClick: ((Account) -> Unit)? = null,
    dashboardConfig: com.example.util.DashboardConfig = com.example.util.DashboardConfig(),
    paymentSourceConfig: com.example.util.PaymentSourceConfig = com.example.util.PaymentSourceConfig()
) {
    when (currentView) {
        AppView.DASHBOARD -> DashboardScreen(
            overview = overview,
            accountsWithBalances = accountsWithBalances,
            recentTransactions = transactionsWithDetails,
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
            onAccountClick = { acc ->
                if (onAccountClick != null) onAccountClick(acc) else onEditAccount(acc)
            },
            onToggleCardVisibility = { card, visible -> viewModel.toggleDashboardCard(card, visible) },
            onReorderCards = { from, to -> viewModel.moveDashboardCard(from, to) },
            onUpdateDailySummarySettings = { m, p, sv, sa, dp, sc, scs -> viewModel.setDailySummarySettings(m, p, sv, sa, dp, sc, scs) },
            onUpdateBudgetSummarySettings = { s, t, mc, sp, tp -> viewModel.setBudgetSummarySettings(s, t, mc, sp, tp) },
            onUpdateCalendarSettings = { dm, si, se -> viewModel.setCalendarSettings(dm, si, se) },
            onUpdateFavoriteAccounts = { favs -> viewModel.setFavoriteAccounts(favs) },
            onResetDashboardDefaults = { viewModel.resetDashboardDefaults() },
            detectedBackups = viewModel.detectedBackups.collectAsStateWithLifecycle().value,
            showRestoreBanner = viewModel.showRestoreBanner.collectAsStateWithLifecycle().value,
            onRestoreDetectedBackup = { backup, isMerge -> viewModel.restoreDetectedBackup(backup, isMerge) },
            onDismissRestoreBanner = { backupId -> viewModel.dismissRestoreBanner(backupId) },
            onScanBackups = { viewModel.scanForPreviousBackups() }
        )
        AppView.LEDGER -> {
            val securityConfig = viewModel.securityConfig.collectAsStateWithLifecycle().value
            LedgerScreen(
                transactions = transactionsWithDetails,
                languageMode = languageMode,
                allCategories = allCategories,
                allAccounts = allAccounts,
                accountsWithBalances = accountsWithBalances,
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
            onAccountClick = onAccountClick
        )
        AppView.REPORTS -> ReportsScreen(
            overview = overview,
            accountsWithBalances = accountsWithBalances,
            transactions = transactionsWithDetails,
            categories = allCategories,
            languageMode = languageMode,
            onOpenDrawer = onOpenDrawer
        )
        AppView.LABELS -> LabelsScreen(
            transactions = transactionsWithDetails,
            accounts = allAccounts,
            languageMode = languageMode,
            onOpenDrawer = onOpenDrawer,
            onTransactionClick = onEditTransaction
        )
        AppView.ITEMS_SUMMARY -> ItemsScreen(
            transactions = transactionsWithDetails,
            accounts = allAccounts,
            languageMode = languageMode,
            onOpenDrawer = onOpenDrawer,
            onTransactionClick = onEditTransaction
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
            onToggleActiveStatus = { acc, active ->
                viewModel.saveAccount(acc.copy(isActive = active))
            },
            onToggleIncludeStatus = { acc, isIncluded ->
                viewModel.setAccountIncludeStatus(acc.id, isIncluded)
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
        AppView.PAYMENT_SOURCE -> LanguageHelper.getString("payment_source", languageMode)
        AppView.BALANCE_SHEET -> LanguageHelper.getString("balance_sheet", languageMode)
        AppView.BUDGET -> LanguageHelper.getString("budget", languageMode)
        AppView.BUDGET_MAKER -> LanguageHelper.getString("budget_maker", languageMode)
        AppView.CATEGORIES -> LanguageHelper.getString("categories", languageMode)
        AppView.REPORTS -> LanguageHelper.getString("net_earnings", languageMode)
        AppView.LABELS -> LanguageHelper.getString("labels", languageMode)
        AppView.ITEMS_SUMMARY -> LanguageHelper.getString("items_summary", languageMode)
        AppView.RECURRING_BILLS -> LanguageHelper.getString("reminders", languageMode)
        AppView.ACCOUNTS -> LanguageHelper.getString("accounts", languageMode)
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
    currentPalette: ThemePalette,
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
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThemePalette.values().forEach { palette ->
                    val isSelected = currentPalette == palette
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
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
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

