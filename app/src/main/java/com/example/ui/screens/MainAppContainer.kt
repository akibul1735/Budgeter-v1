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
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
    BACKUP_SYNC
}

fun AppTab.toAppView(): AppView = when (this) {
    AppTab.MAIN -> AppView.DASHBOARD
    AppTab.TRANSACTIONS -> AppView.LEDGER
    AppTab.BALANCE_SHEET -> AppView.BALANCE_SHEET
    AppTab.ACCOUNTS -> AppView.ACCOUNTS
    AppTab.BUDGET -> AppView.BUDGET
    AppTab.CATEGORIES -> AppView.CATEGORIES
    AppTab.NET_EARNINGS -> AppView.REPORTS
    AppTab.LABELS -> AppView.LABELS
    AppTab.ITEMS_SUMMARY -> AppView.ITEMS_SUMMARY
    AppTab.REMINDERS -> AppView.RECURRING_BILLS
}

fun AppView.toAppTab(): AppTab? = when (this) {
    AppView.DASHBOARD -> AppTab.MAIN
    AppView.LEDGER -> AppTab.TRANSACTIONS
    AppView.BALANCE_SHEET -> AppTab.BALANCE_SHEET
    AppView.ACCOUNTS -> AppTab.ACCOUNTS
    AppView.BUDGET -> AppTab.BUDGET
    AppView.CATEGORIES -> AppTab.CATEGORIES
    AppView.EXPENSES -> AppTab.CATEGORIES
    AppView.INCOME -> AppTab.CATEGORIES
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

    val selectView: (AppView) -> Unit = { targetView ->
        if (currentView != targetView) {
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
                                onOpenCalculator = {
                                    showGlobalCalculator = true
                                    scope.launch { drawerState.close() }
                                },
                                onOpenThemeFontSettings = {
                                    showThemeFontSettings = true
                                    scope.launch { drawerState.close() }
                                },
                                onOpenAutofillSettings = {
                                    showAutofillSettingsDialog = true
                                    scope.launch { drawerState.close() }
                                },
                                onOpenTabCustomizer = {
                                    showTabCustomizationDialog = true
                                    scope.launch { drawerState.close() }
                                },
                                accountsCount = allAccounts.size,
                                expensesCount = allCategories.count { it.type == CategoryType.EXPENSE && it.parentId == null },
                                incomeCount = allCategories.count { it.type == CategoryType.INCOME && it.parentId == null },
                                recurringCount = recurringBills.size,
                                budgetCount = monthlyBudgets.size,
                                netWorth = overview.netWorth,
                                isBalanced = overview.isLedgerBalanced,
                                languageMode = languageMode
                            )
                        }
                    }
                ) {
                    Scaffold(
                        topBar = {
                            Column {
                                TopAppBar(
                                    title = {
                                        Text(
                                            text = getViewTitle(currentView, languageMode),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp
                                        )
                                    },
                                    navigationIcon = {
                                        if (currentView != AppView.DASHBOARD || viewHistory.size > 1) {
                                            IconButton(onClick = handleBackPress) {
                                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                            }
                                        } else {
                                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                                            }
                                        }
                                    },
                                    actions = {
                                        if (isDemoMode) {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = MaterialTheme.colorScheme.tertiaryContainer,
                                                modifier = Modifier
                                                    .padding(end = 4.dp)
                                                    .clickable { viewModel.setDemoMode(false) }
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(6.dp)
                                                            .background(MaterialTheme.colorScheme.tertiary, CircleShape)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "DEMO",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                                    )
                                                }
                                            }
                                        }
                                        // Quick Tab Customization Button
                                        IconButton(
                                            onClick = { showTabCustomizationDialog = true },
                                            modifier = Modifier.testTag("btn_tab_customization")
                                        ) {
                                            Icon(
                                                Icons.Default.ViewCarousel,
                                                contentDescription = "Customize Navigation Tabs",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        IconButton(onClick = { showThemeFontSettings = true }) {
                                            Icon(
                                                Icons.Default.Palette,
                                                contentDescription = "Theme & Fonts",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        LanguageSelector(
                                            currentMode = languageMode,
                                            onModeSelected = { viewModel.setLanguageMode(it) }
                                        )
                                    },
                                    colors = TopAppBarDefaults.topAppBarColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    )
                                )

                                // TOP NAVIGATION TAB ROW (When Position is TOP)
                                if (tabConfig.position == TabPosition.TOP && !isSubView) {
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
                            ScreenRouter(
                                currentView = currentView,
                                viewModel = viewModel,
                                overview = overview,
                                accountsWithBalances = accountsWithBalances,
                                allAccounts = allAccounts,
                                allCategories = allCategories,
                                transactionsWithDetails = transactionsWithDetails,
                                recurringBills = recurringBills,
                                languageMode = languageMode,
                                backupUiState = backupUiState,
                                monthlyBudgets = monthlyBudgets,
                                selectedBudgetYear = selectedBudgetYear,
                                selectedBudgetMonth = selectedBudgetMonth,
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
                                }
                            )
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
                        topBar = {
                            TopAppBar(
                                title = { Text(getViewTitle(currentView, languageMode), fontWeight = FontWeight.Bold) },
                                navigationIcon = {
                                    if (currentView != AppView.DASHBOARD || viewHistory.size > 1) {
                                        IconButton(onClick = handleBackPress) {
                                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                        }
                                    }
                                },
                                actions = {
                                    IconButton(onClick = { showGlobalCalculator = true }) {
                                        Icon(Icons.Default.Calculate, contentDescription = "Calculator")
                                    }
                                    LanguageSelector(
                                        currentMode = languageMode,
                                        onModeSelected = { viewModel.setLanguageMode(it) }
                                    )
                                }
                            )
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
                                allAccounts = allAccounts,
                                allCategories = allCategories,
                                transactionsWithDetails = transactionsWithDetails,
                                recurringBills = recurringBills,
                                languageMode = languageMode,
                                backupUiState = backupUiState,
                                monthlyBudgets = monthlyBudgets,
                                selectedBudgetYear = selectedBudgetYear,
                                selectedBudgetMonth = selectedBudgetMonth,
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
                                }
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
                                onOpenCalculator = { showGlobalCalculator = true },
                                onOpenThemeFontSettings = { showThemeFontSettings = true },
                                onOpenAutofillSettings = { showAutofillSettingsDialog = true },
                                onOpenTabCustomizer = { showTabCustomizationDialog = true },
                                accountsCount = allAccounts.size,
                                expensesCount = allCategories.count { it.type == CategoryType.EXPENSE && it.parentId == null },
                                incomeCount = allCategories.count { it.type == CategoryType.INCOME && it.parentId == null },
                                recurringCount = recurringBills.size,
                                budgetCount = monthlyBudgets.size,
                                netWorth = overview.netWorth,
                                isBalanced = overview.isLedgerBalanced,
                                languageMode = languageMode
                            )
                        }
                    }
                ) {
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                title = { Text(getViewTitle(currentView, languageMode), fontWeight = FontWeight.Bold) },
                                navigationIcon = {
                                    if (currentView != AppView.DASHBOARD || viewHistory.size > 1) {
                                        IconButton(onClick = handleBackPress) {
                                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                        }
                                    }
                                },
                                actions = {
                                    IconButton(onClick = { showTabCustomizationDialog = true }) {
                                        Icon(Icons.Default.ViewCarousel, contentDescription = "Tabs", tint = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(onClick = { showGlobalCalculator = true }) {
                                        Icon(Icons.Default.Calculate, contentDescription = "Calculator")
                                    }
                                    IconButton(onClick = { showThemeFontSettings = true }) {
                                        Icon(Icons.Default.Palette, contentDescription = "Theme", tint = MaterialTheme.colorScheme.primary)
                                    }
                                    LanguageSelector(
                                        currentMode = languageMode,
                                        onModeSelected = { viewModel.setLanguageMode(it) }
                                    )
                                }
                            )
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
                                allAccounts = allAccounts,
                                allCategories = allCategories,
                                transactionsWithDetails = transactionsWithDetails,
                                recurringBills = recurringBills,
                                languageMode = languageMode,
                                backupUiState = backupUiState,
                                monthlyBudgets = monthlyBudgets,
                                selectedBudgetYear = selectedBudgetYear,
                                selectedBudgetMonth = selectedBudgetMonth,
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
                                }
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
    onOpenCalculator: () -> Unit,
    onOpenThemeFontSettings: () -> Unit,
    onOpenAutofillSettings: () -> Unit,
    onOpenTabCustomizer: () -> Unit,
    accountsCount: Int,
    expensesCount: Int,
    incomeCount: Int,
    recurringCount: Int,
    budgetCount: Int = 0,
    netWorth: Double,
    isBalanced: Boolean,
    languageMode: LanguageMode
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Drawer Header with Theme Color
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
                    Text(
                        text = LanguageHelper.getString("app_name", languageMode),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
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

        Text(
            text = "MAIN VIEWS & STATS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )

        // 1. Main Dashboard
        DrawerItemRow(
            title = LanguageHelper.getString("main", languageMode),
            icon = Icons.Default.Dashboard,
            iconTint = MaterialTheme.colorScheme.primary,
            isSelected = currentView == AppView.DASHBOARD,
            onClick = { onSelectView(AppView.DASHBOARD) }
        )

        // 2. Transactions
        DrawerItemRow(
            title = LanguageHelper.getString("transactions", languageMode),
            icon = Icons.AutoMirrored.Filled.ReceiptLong,
            iconTint = SolidTransfer,
            isSelected = currentView == AppView.LEDGER,
            onClick = { onSelectView(AppView.LEDGER) }
        )

        // 3. Balance Sheet (Stats)
        DrawerItemRow(
            title = LanguageHelper.getString("balance_sheet", languageMode),
            icon = Icons.Default.AccountBalance,
            iconTint = MaterialTheme.colorScheme.primary,
            badge = "$accountsCount",
            isSelected = currentView == AppView.BALANCE_SHEET,
            onClick = { onSelectView(AppView.BALANCE_SHEET) }
        )

        // 4. Accounts (Create & Manage)
        DrawerItemRow(
            title = LanguageHelper.getString("accounts", languageMode),
            icon = Icons.Default.AccountBalanceWallet,
            iconTint = MaterialTheme.colorScheme.primary,
            badge = "$accountsCount",
            isSelected = currentView == AppView.ACCOUNTS,
            onClick = { onSelectView(AppView.ACCOUNTS) }
        )

        // 5. Budget (Stats & Tracking)
        DrawerItemRow(
            title = LanguageHelper.getString("budget", languageMode),
            icon = Icons.Default.ShoppingBag,
            iconTint = MaterialTheme.colorScheme.primary,
            badge = if (budgetCount > 0) "$budgetCount" else null,
            isSelected = currentView == AppView.BUDGET,
            onClick = { onSelectView(AppView.BUDGET) }
        )

        // 6. Categories (Create & Manage)
        DrawerItemRow(
            title = LanguageHelper.getString("categories", languageMode),
            icon = Icons.Default.Category,
            iconTint = SolidExpense,
            badge = "${expensesCount + incomeCount}",
            isSelected = currentView == AppView.CATEGORIES,
            onClick = { onSelectView(AppView.CATEGORIES) }
        )

        // 7. Net Earnings (Reports & Stats)
        DrawerItemRow(
            title = LanguageHelper.getString("net_earnings", languageMode),
            icon = Icons.Default.Assignment,
            iconTint = MaterialTheme.colorScheme.primary,
            isSelected = currentView == AppView.REPORTS,
            onClick = { onSelectView(AppView.REPORTS) }
        )

        // 8. Labels
        DrawerItemRow(
            title = LanguageHelper.getString("labels", languageMode),
            icon = Icons.Default.Tag,
            iconTint = MaterialTheme.colorScheme.primary,
            isSelected = currentView == AppView.LABELS,
            onClick = { onSelectView(AppView.LABELS) }
        )

        // 9. Items Summary
        DrawerItemRow(
            title = LanguageHelper.getString("items_summary", languageMode),
            icon = Icons.Default.Bookmark,
            iconTint = MaterialTheme.colorScheme.primary,
            isSelected = currentView == AppView.ITEMS_SUMMARY,
            onClick = { onSelectView(AppView.ITEMS_SUMMARY) }
        )

        // 10. Reminders
        DrawerItemRow(
            title = LanguageHelper.getString("reminders", languageMode),
            icon = Icons.Default.Alarm,
            iconTint = MaterialTheme.colorScheme.primary,
            badge = "$recurringCount",
            isSelected = currentView == AppView.RECURRING_BILLS,
            onClick = { onSelectView(AppView.RECURRING_BILLS) }
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp, horizontal = 16.dp))

        Text(
            text = "MANAGEMENT & QUICK TOOLS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )

        DrawerItemRow(
            title = LanguageHelper.getString("budget_maker", languageMode),
            icon = Icons.Default.Calculate,
            iconTint = MaterialTheme.colorScheme.primary,
            isSelected = currentView == AppView.BUDGET_MAKER,
            onClick = { onSelectView(AppView.BUDGET_MAKER) }
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp, horizontal = 16.dp))

        // DEMO MODE ENVIRONMENT CONTROL
        val isDemoMode by viewModel.isDemoMode.collectAsStateWithLifecycle()
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isDemoMode) MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Demo Mode",
                            tint = if (isDemoMode) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "ডেমো মোড (নমুনা ডাটা)" else "Demo Mode (Sample Data)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isDemoMode) "Active (Real data isolated)" else "Off (Using real database)",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                    androidx.compose.material3.Switch(
                        checked = isDemoMode,
                        onCheckedChange = { viewModel.setDemoMode(it) }
                    )
                }

                if (isDemoMode) {
                    Spacer(modifier = Modifier.height(8.dp))
                    androidx.compose.material3.OutlinedButton(
                        onClick = { viewModel.resetDemoData() },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "নমুনা ডাটা পুনরায় লোড করুন" else "Reset Sample Demo Data",
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "PREFERENCES & TOOLS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )

        // TAB CUSTOMIZATION (TOP / BOTTOM & TAB TOGGLES)
        DrawerItemRow(
            title = LanguageHelper.getString("tab_customization", languageMode),
            icon = Icons.Default.ViewCarousel,
            iconTint = MaterialTheme.colorScheme.primary,
            isSelected = false,
            onClick = onOpenTabCustomizer
        )

        // Backup, Restore & Sync
        DrawerItemRow(
            title = "Backup, Restore & Sync",
            icon = Icons.Default.CloudSync,
            iconTint = MaterialTheme.colorScheme.primary,
            isSelected = currentView == AppView.BACKUP_SYNC,
            onClick = { onSelectView(AppView.BACKUP_SYNC) }
        )

        // Autofill Settings
        DrawerItemRow(
            title = LanguageHelper.getString("autofill_settings", languageMode),
            icon = Icons.Default.AutoAwesome,
            iconTint = MaterialTheme.colorScheme.primary,
            isSelected = false,
            onClick = onOpenAutofillSettings
        )

        // Theme & Font Settings
        DrawerItemRow(
            title = "Theme & Typography",
            icon = Icons.Default.Palette,
            iconTint = MaterialTheme.colorScheme.primary,
            isSelected = false,
            onClick = onOpenThemeFontSettings
        )

        // Calculator
        DrawerItemRow(
            title = "Popup Calculator",
            icon = Icons.Default.Calculate,
            iconTint = MaterialTheme.colorScheme.primary,
            isSelected = false,
            onClick = onOpenCalculator
        )

        // Quick Sync
        NavigationDrawerItem(
            label = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Quick JSON Sync")
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

        // Language Switcher
        NavigationDrawerItem(
            label = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = LanguageHelper.getString("language", languageMode))
                    Text(
                        text = when (languageMode) {
                            LanguageMode.ENGLISH -> "EN"
                            LanguageMode.BANGLA -> "বাং"
                            LanguageMode.BILINGUAL -> "Both"
                        },
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            icon = { Icon(Icons.Default.Translate, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            selected = false,
            onClick = {
                val nextMode = when (languageMode) {
                    LanguageMode.ENGLISH -> LanguageMode.BANGLA
                    LanguageMode.BANGLA -> LanguageMode.BILINGUAL
                    LanguageMode.BILINGUAL -> LanguageMode.ENGLISH
                }
                viewModel.setLanguageMode(nextMode)
            },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
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
    allAccounts: List<Account>,
    allCategories: List<Category>,
    transactionsWithDetails: List<com.example.data.model.TransactionWithDetails>,
    recurringBills: List<com.example.data.model.RecurringBillWithDetails>,
    languageMode: LanguageMode,
    backupUiState: com.example.ui.viewmodel.BackupUiState,
    monthlyBudgets: List<com.example.data.model.MonthlyBudget>,
    selectedBudgetYear: Int,
    selectedBudgetMonth: Int,
    onNavigate: (AppView) -> Unit,
    onEditTransaction: (Transaction) -> Unit,
    onAddTransactionWithType: (TransactionType) -> Unit,
    onAddTransactionWithCategory: (Category) -> Unit,
    onAddTransactionWithAccount: (Account) -> Unit,
    onAddAccount: (Long?) -> Unit,
    onEditAccount: (Account) -> Unit,
    onAddCategory: (CategoryType, Long?) -> Unit,
    onEditCategory: (Category) -> Unit
) {
    when (currentView) {
        AppView.DASHBOARD -> DashboardScreen(
            overview = overview,
            accountsWithBalances = accountsWithBalances,
            recentTransactions = transactionsWithDetails,
            languageMode = languageMode,
            onAddTransactionClick = onAddTransactionWithType,
            onTransactionClick = onEditTransaction,
            onViewAllTransactionsClick = { onNavigate(AppView.LEDGER) }
        )
        AppView.LEDGER -> LedgerScreen(
            transactions = transactionsWithDetails,
            languageMode = languageMode,
            allCategories = allCategories,
            allAccounts = allAccounts,
            accountsWithBalances = accountsWithBalances,
            onAddTransactionClick = { onAddTransactionWithType(TransactionType.EXPENSE) },
            onTransactionClick = onEditTransaction,
            onUpdateTransactions = { txList -> viewModel.updateTransactions(txList) },
            onDeleteTransactions = { txList -> viewModel.deleteTransactions(txList) }
        )
        AppView.BALANCE_SHEET -> BalanceSheetScreen(
            accounts = allAccounts,
            transactions = transactionsWithDetails.map { it.transaction },
            languageMode = languageMode,
            onAddAccountClick = { onAddAccount(null) },
            onAddSubAccountClick = { parent -> onAddAccount(parent.id) },
            onEditAccountClick = onEditAccount,
            onAddTransactionClick = { onAddTransactionWithType(TransactionType.EXPENSE) }
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
            languageMode = languageMode
        )
        AppView.LABELS -> LabelsScreen(
            transactions = transactionsWithDetails,
            languageMode = languageMode,
            onTransactionClick = onEditTransaction
        )
        AppView.ITEMS_SUMMARY -> ItemsScreen(
            transactions = transactionsWithDetails,
            languageMode = languageMode,
            onTransactionClick = onEditTransaction
        )
        AppView.RECURRING_BILLS -> RecurringBillsScreen(
            viewModel = viewModel,
            bills = recurringBills,
            languageMode = languageMode
        )
        AppView.ACCOUNTS -> AccountsScreen(
            accountsWithBalances = accountsWithBalances,
            languageMode = languageMode,
            onAddAccountClick = { onAddAccount(null) },
            onAddSubAccountClick = { parent -> onAddAccount(parent.id) },
            onEditAccountClick = onEditAccount,
            onToggleActiveStatus = { acc, active ->
                viewModel.saveAccount(acc.copy(isActive = active))
            }
        )
        AppView.CATEGORIES -> CategoriesScreen(
            categories = allCategories,
            languageMode = languageMode,
            initialTab = 0,
            onAddCategoryClick = { type -> onAddCategory(type, null) },
            onAddSubCategoryClick = { parent -> onAddCategory(parent.type, parent.id) },
            onEditCategoryClick = onEditCategory
        )
        AppView.EXPENSES -> CategoriesScreen(
            categories = allCategories,
            languageMode = languageMode,
            initialTab = 0,
            onAddCategoryClick = { type -> onAddCategory(type, null) },
            onAddSubCategoryClick = { parent -> onAddCategory(parent.type, parent.id) },
            onEditCategoryClick = onEditCategory
        )
        AppView.INCOME -> CategoriesScreen(
            categories = allCategories,
            languageMode = languageMode,
            initialTab = 1,
            onAddCategoryClick = { type -> onAddCategory(type, null) },
            onAddSubCategoryClick = { parent -> onAddCategory(parent.type, parent.id) },
            onEditCategoryClick = onEditCategory
        )
        AppView.BACKUP_SYNC -> BackupSyncSettingsScreen(
            viewModel = viewModel,
            languageMode = languageMode,
            backupUiState = backupUiState
        )
    }
}

private fun getViewTitle(view: AppView, languageMode: LanguageMode): String {
    return when (view) {
        AppView.DASHBOARD -> LanguageHelper.getString("app_name", languageMode)
        AppView.LEDGER -> LanguageHelper.getString("transactions", languageMode)
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
    }
}
