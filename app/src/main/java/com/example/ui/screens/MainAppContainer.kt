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
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Account
import com.example.data.model.AccountType
import com.example.data.model.Category
import com.example.data.model.CategoryType
import com.example.data.model.LanguageMode
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.ui.components.LanguageSelector
import com.example.ui.components.PopupCalculatorDialog
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
    TRASH
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
    val isAppLocked by viewModel.isAppLocked.collectAsStateWithLifecycle()
    val securityConfig by viewModel.securityConfig.collectAsStateWithLifecycle()

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

    // Dialog control states
    var showAddTransactionSheet by remember { mutableStateOf(false) }
    var editingTransaction by remember { mutableStateOf<Transaction?>(null) }
    var presetTxType by remember { mutableStateOf(TransactionType.EXPENSE) }

    var showAddAccountDialog by remember { mutableStateOf(false) }
    var editingAccount by remember { mutableStateOf<Account?>(null) }
    var presetAccountParentId by remember { mutableStateOf<Long?>(null) }

    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<Category?>(null) }
    var presetCategoryType by remember { mutableStateOf(CategoryType.EXPENSE) }
    var presetCategoryParentId by remember { mutableStateOf<Long?>(null) }

    var showGlobalCalculator by remember { mutableStateOf(false) }
    var showThemeFontSettings by remember { mutableStateOf(false) }
    var showAutofillSettingsDialog by remember { mutableStateOf(false) }
    var showTabCustomizationDialog by remember { mutableStateOf(false) }
    var showDashboardCustomizerDialog by remember { mutableStateOf(false) }

    val visibleTabs = tabConfig.visibleTabs
    val isTabInVisibleTabs = visibleTabs.any { it.toAppView() == currentView }
    val currentTabIndex = visibleTabs.indexOfFirst { it.toAppView() == currentView }

    val pagerState = rememberPagerState(
        initialPage = if (currentTabIndex >= 0) currentTabIndex else 0,
        pageCount = { visibleTabs.size }
    )

    // Sync pager when currentView changes programmatically
    LaunchedEffect(currentView, visibleTabs) {
        val targetIndex = visibleTabs.indexOfFirst { it.toAppView() == currentView }
        if (targetIndex >= 0 && pagerState.currentPage != targetIndex) {
            pagerState.animateScrollToPage(targetIndex)
        }
    }

    // Sync currentView when user swipes left/right between tabs
    LaunchedEffect(pagerState, visibleTabs) {
        snapshotFlow { pagerState.settledPage }.collect { page ->
            if (page in visibleTabs.indices) {
                val swipedTab = visibleTabs[page]
                val swipedView = swipedTab.toAppView()
                if (isTabInVisibleTabs && currentView != swipedView) {
                    currentView = swipedView
                }
            }
        }
    }

    val selectView: (AppView) -> Unit = { targetView ->
        if (currentView != targetView) {
            val tabIndex = visibleTabs.indexOfFirst { it.toAppView() == targetView }
            if (tabIndex >= 0) {
                scope.launch {
                    pagerState.animateScrollToPage(tabIndex)
                }
            }
            if (targetView == AppView.DASHBOARD) {
                viewHistory.clear()
                viewHistory.add(AppView.DASHBOARD)
            } else {
                viewHistory.add(targetView)
            }
            currentView = targetView
        }
    }

    val handleBackPress: () -> Unit = {
        when {
            showTabCustomizationDialog -> showTabCustomizationDialog = false
            showAddTransactionSheet -> showAddTransactionSheet = false
            showAddAccountDialog -> showAddAccountDialog = false
            showAddCategoryDialog -> showAddCategoryDialog = false
            showGlobalCalculator -> showGlobalCalculator = false
            showThemeFontSettings -> showThemeFontSettings = false
            showAutofillSettingsDialog -> showAutofillSettingsDialog = false
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

    val isSubView = currentView in listOf(AppView.EXPENSES, AppView.INCOME, AppView.BACKUP_SYNC, AppView.BUDGET_MAKER)

    // Window Width Adaptive Layout Container
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
                                TopNavigationBarRow(
                                    visibleTabs = tabConfig.visibleTabs,
                                    currentView = currentView,
                                    languageMode = languageMode,
                                    onSelectTab = { tab -> selectView(tab.toAppView()) }
                                )
                            }
                        },
                        bottomBar = {
                            // BOTTOM NAVIGATION TAB ROW (When Position is BOTTOM)
                            if (tabConfig.position == TabPosition.BOTTOM && !isSubView) {
                                BottomNavigationBarRow(
                                    visibleTabs = tabConfig.visibleTabs,
                                    currentView = currentView,
                                    languageMode = languageMode,
                                    onSelectTab = { tab -> selectView(tab.toAppView()) }
                                )
                            }
                        },
                        floatingActionButton = {
                            AppFab(
                                currentView = currentView,
                                onAddTransaction = {
                                    editingTransaction = null
                                    presetTxType = TransactionType.EXPENSE
                                    showAddTransactionSheet = true
                                },
                                onAddAccount = {
                                    editingAccount = null
                                    presetAccountParentId = null
                                    showAddAccountDialog = true
                                },
                                onAddCategory = { type ->
                                    editingCategory = null
                                    presetCategoryType = type
                                    presetCategoryParentId = null
                                    showAddCategoryDialog = true
                                }
                            )
                        }
                    ) { paddingValues ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues)
                        ) {
                            if (isTabInVisibleTabs && visibleTabs.isNotEmpty()) {
                                HorizontalPager(
                                    state = pagerState,
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
                                        dashboardConfig = dashboardConfig
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
                                    dashboardConfig = dashboardConfig
                                )
                            }
                        }
                    }
                }
            }

            WindowSizeClassType.MEDIUM -> {
                // FOLDABLE / SMALL TABLET LAYOUT: Navigation Rail + Main Content Area
                Row(modifier = Modifier.fillMaxSize()) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface,
                        header = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
                        floatingActionButton = {
                            AppFab(
                                currentView = currentView,
                                onAddTransaction = {
                                    editingTransaction = null
                                    presetTxType = TransactionType.EXPENSE
                                    showAddTransactionSheet = true
                                },
                                onAddAccount = {
                                    editingAccount = null
                                    presetAccountParentId = null
                                    showAddAccountDialog = true
                                },
                                onAddCategory = { type ->
                                    editingCategory = null
                                    presetCategoryType = type
                                    presetCategoryParentId = null
                                    showAddCategoryDialog = true
                                }
                            )
                        }
                    ) { padding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(padding)
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
                                dashboardConfig = dashboardConfig
                            )
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
                        floatingActionButton = {
                            AppFab(
                                currentView = currentView,
                                onAddTransaction = {
                                    editingTransaction = null
                                    presetTxType = TransactionType.EXPENSE
                                    showAddTransactionSheet = true
                                },
                                onAddAccount = {
                                    editingAccount = null
                                    presetAccountParentId = null
                                    showAddAccountDialog = true
                                },
                                onAddCategory = { type ->
                                    editingCategory = null
                                    presetCategoryType = type
                                    presetCategoryParentId = null
                                    showAddCategoryDialog = true
                                }
                            )
                        }
                    ) { padding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(padding)
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
                                dashboardConfig = dashboardConfig
                            )
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
            languageMode = languageMode,
            existingAccount = editingAccount,
            defaultGroupId = presetAccountParentId,
            securityConfig = securityConfig,
            onVerifyPin = { viewModel.verifySecurityPin(it) },
            onDismiss = { showAddAccountDialog = false },
            onSave = { acc -> viewModel.saveAccount(acc) },
            onDelete = { acc -> viewModel.deleteAccount(acc) }
        )
    }

    if (showAddCategoryDialog) {
        val parentCategories = allCategories.filter { it.parentId == null }
        AddEditCategoryDialog(
            parentCategories = parentCategories,
            languageMode = languageMode,
            existingCategory = editingCategory,
            defaultType = presetCategoryType,
            defaultParentId = presetCategoryParentId,
            securityConfig = securityConfig,
            onVerifyPin = { viewModel.verifySecurityPin(it) },
            onDismiss = { showAddCategoryDialog = false },
            onSave = { cat -> viewModel.saveCategory(cat) },
            onDelete = { cat -> viewModel.deleteCategory(cat) }
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
            onModeSelected = { viewModel.setThemeMode(it) },
            onColorIntensitySelected = { viewModel.setColorIntensity(it) },
            onDynamicColorToggled = { viewModel.setDynamicColor(it) },
            onFontPresetSelected = { viewModel.setFontPreset(it) },
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
    onAddTransaction: () -> Unit,
    onAddAccount: () -> Unit,
    onAddCategory: (CategoryType) -> Unit
) {
    if (currentView in listOf(AppView.DASHBOARD, AppView.LEDGER, AppView.LABELS, AppView.ITEMS_SUMMARY, AppView.BALANCE_SHEET, AppView.REPORTS)) {
        FloatingActionButton(
            onClick = onAddTransaction,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier.testTag("main_fab_add_tx")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Transaction")
        }
    } else if (currentView == AppView.ACCOUNTS) {
        FloatingActionButton(
            onClick = onAddAccount,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier.testTag("accounts_fab_add")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Account")
        }
    } else if (currentView in listOf(AppView.CATEGORIES, AppView.EXPENSES, AppView.INCOME)) {
        val catType = if (currentView == AppView.INCOME) CategoryType.INCOME else CategoryType.EXPENSE
        FloatingActionButton(
            onClick = { onAddCategory(catType) },
            containerColor = if (catType == CategoryType.EXPENSE) SolidExpense else SolidIncome,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier.testTag("categories_fab_add")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Category")
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

    val lastSyncFormatted = remember(backupConfig.lastSyncTimestamp, languageMode) {
        if (backupConfig.lastSyncTimestamp == 0L) {
            if (languageMode == LanguageMode.BANGLA) "কখনও নয়" else "Never"
        } else {
            val diff = System.currentTimeMillis() - backupConfig.lastSyncTimestamp
            if (diff < 60_000L) {
                if (languageMode == LanguageMode.BANGLA) "এইমাত্র" else "Just now"
            } else {
                com.example.util.DateUtils.formatDate(backupConfig.lastSyncTimestamp, languageMode)
            }
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
        }

        Spacer(modifier = Modifier.height(12.dp))

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
    dashboardConfig: com.example.util.DashboardConfig = com.example.util.DashboardConfig()
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
            onAccountClick = onEditAccount,
            onToggleCardVisibility = { card, visible -> viewModel.toggleDashboardCard(card, visible) },
            onReorderCards = { from, to -> viewModel.moveDashboardCard(from, to) },
            onUpdateDailySummarySettings = { m, p, sv, sa -> viewModel.setDailySummarySettings(m, p, sv, sa) },
            onUpdateBudgetSummarySettings = { s, t, mc, sp, tp -> viewModel.setBudgetSummarySettings(s, t, mc, sp, tp) },
            onUpdateCalendarSettings = { dm, si, se -> viewModel.setCalendarSettings(dm, si, se) },
            onUpdateFavoriteAccounts = { favs -> viewModel.setFavoriteAccounts(favs) },
            onResetDashboardDefaults = { viewModel.resetDashboardDefaults() }
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
                onUpdateTransactions = { txList -> viewModel.updateTransactions(txList) },
                onDeleteTransactions = { txList -> viewModel.deleteTransactions(txList) }
            )
        }
        AppView.BALANCE_SHEET -> BalanceSheetScreen(
            accounts = allAccounts,
            transactions = transactionsWithDetails.map { it.transaction },
            languageMode = languageMode,
            onOpenDrawer = onOpenDrawer,
            onAddAccountClick = { onAddAccount(null) },
            onAddSubAccountClick = { parent -> onAddAccount(parent.id) },
            onEditAccountClick = onEditAccount,
            onAddTransactionClick = { onAddTransactionWithType(TransactionType.EXPENSE) }
        )
        AppView.PAYMENT_SOURCE -> PaymentSourceScreen(
            allAccounts = allAccounts,
            accountsWithBalances = accountsWithBalances,
            allCategories = allCategories,
            monthlyBudgets = monthlyBudgets,
            allTransactions = transactionsWithDetails,
            recurringBills = recurringBills.map { it.bill },
            selectedYear = selectedBudgetYear,
            selectedMonth = selectedBudgetMonth,
            languageMode = languageMode,
            onOpenDrawer = onOpenDrawer,
            onPrevMonth = { viewModel.prevBudgetMonth() },
            onNextMonth = { viewModel.nextBudgetMonth() },
            onSetCurrentMonth = {
                val cal = java.util.Calendar.getInstance()
                viewModel.setBudgetYearMonth(cal.get(java.util.Calendar.YEAR), cal.get(java.util.Calendar.MONTH) + 1)
            },
            onExecuteTransfer = onExecuteTransfer,
            onAddTransactionWithAccount = onAddTransactionWithAccountAndType,
            onEditAccount = onEditAccount,
            onSaveCategoryAllocations = { categoryId, allocMap ->
                viewModel.saveCategoryAccountAllocations(categoryId, allocMap)
            }
        )
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
            onEditTransaction = onEditTransaction
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
            onEditTransaction = onEditTransaction,
            onAddTransactionWithCategory = onAddTransactionWithCategory,
            onAddTransactionWithAccount = onAddTransactionWithAccount
        )
        AppView.REPORTS -> ReportsScreen(
            overview = overview,
            accountsWithBalances = accountsWithBalances,
            languageMode = languageMode,
            onOpenDrawer = onOpenDrawer
        )
        AppView.LABELS -> LabelsScreen(
            transactions = transactionsWithDetails,
            languageMode = languageMode,
            onOpenDrawer = onOpenDrawer,
            onTransactionClick = onEditTransaction
        )
        AppView.ITEMS_SUMMARY -> ItemsScreen(
            transactions = transactionsWithDetails,
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
            onOpenDrawer = onOpenDrawer,
            onAddAccountClick = { onAddAccount(null) },
            onAddSubAccountClick = { parent -> onAddAccount(parent.id) },
            onEditAccountClick = onEditAccount,
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
            onOpenDrawer = onOpenDrawer,
            onAddCategoryClick = { type -> onAddCategory(type, null) },
            onAddSubCategoryClick = { parent -> onAddCategory(parent.type, parent.id) },
            onEditCategoryClick = onEditCategory,
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
            onOpenDrawer = onOpenDrawer,
            onAddCategoryClick = { type -> onAddCategory(type, null) },
            onAddSubCategoryClick = { parent -> onAddCategory(parent.type, parent.id) },
            onEditCategoryClick = onEditCategory,
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
            onOpenDrawer = onOpenDrawer,
            onAddCategoryClick = { type -> onAddCategory(type, null) },
            onAddSubCategoryClick = { parent -> onAddCategory(parent.type, parent.id) },
            onEditCategoryClick = onEditCategory,
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
            onBack = onBack,
            onNavigateToBackupSync = { onNavigate(AppView.BACKUP_SYNC) },
            onOpenTabCustomizer = onOpenTabCustomizer,
            onOpenThemeFontSettings = onOpenThemeFontSettings,
            onOpenAutofillSettings = onOpenAutofillSettings
        )
        AppView.TRASH -> TrashScreen(
            viewModel = viewModel,
            languageMode = languageMode,
            onBack = onBack
        )
    }
}

private fun getViewTitle(view: AppView, languageMode: LanguageMode): String {
    return when (view) {
        AppView.DASHBOARD -> LanguageHelper.getString("app_name", languageMode)
        AppView.LEDGER -> LanguageHelper.getString("transactions", languageMode)
        AppView.PAYMENT_SOURCE -> LanguageHelper.getString("payment_source_analysis", languageMode)
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
    }
}
