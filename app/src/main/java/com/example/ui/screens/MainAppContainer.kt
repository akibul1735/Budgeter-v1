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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoAwesome
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
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Translate
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
import androidx.compose.material3.Surface
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
import com.example.ui.dialogs.ThemeFontSettingsDialog
import com.example.ui.theme.ColorIntensity
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidTransfer
import com.example.ui.viewmodel.BudgetViewModel
import com.example.util.AutofillPreferences
import com.example.util.LanguageHelper
import kotlinx.coroutines.launch

enum class AppView {
    DASHBOARD,
    LEDGER,
    BUDGET,
    BUDGET_MAKER,
    REPORTS,
    ACCOUNTS,
    EXPENSES,
    INCOME,
    RECURRING_BILLS,
    BACKUP_SYNC
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

    val bottomNavItems = listOf(
        Triple(LanguageHelper.getString("main", languageMode), Icons.Default.Dashboard, AppView.DASHBOARD),
        Triple(LanguageHelper.getString("transactions", languageMode), Icons.AutoMirrored.Filled.ReceiptLong, AppView.LEDGER),
        Triple(LanguageHelper.getString("budget", languageMode), Icons.Default.Assessment, AppView.BUDGET),
        Triple(LanguageHelper.getString("budget_maker", languageMode), Icons.Default.Calculate, AppView.BUDGET_MAKER),
        Triple(LanguageHelper.getString("balance_sheet", languageMode), Icons.Default.AccountBalance, AppView.ACCOUNTS)
    )

    val isSubView = currentView in listOf(AppView.REPORTS, AppView.ACCOUNTS, AppView.EXPENSES, AppView.INCOME, AppView.RECURRING_BILLS, AppView.BACKUP_SYNC)

    // Window Width Adaptive Layout Container
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val windowSizeClass = when {
            maxWidth < 600.dp -> WindowSizeClassType.COMPACT
            maxWidth < 840.dp -> WindowSizeClassType.MEDIUM
            else -> WindowSizeClassType.EXPANDED
        }

        when (windowSizeClass) {
            WindowSizeClassType.COMPACT -> {
                // PHONE LAYOUT: Modal Navigation Drawer + Scaffold + Bottom Nav
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
                        },
                        bottomBar = {
                            if (!isSubView) {
                                NavigationBar(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    tonalElevation = 4.dp
                                ) {
                                    bottomNavItems.forEach { (label, icon, view) ->
                                        val isSelected = currentView == view
                                        NavigationBarItem(
                                            selected = isSelected,
                                            onClick = { selectView(view) },
                                            icon = { Icon(icon, contentDescription = label) },
                                            label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                                indicatorColor = MaterialTheme.colorScheme.primary
                                            ),
                                            modifier = Modifier.testTag("nav_${view.name.lowercase()}")
                                        )
                                    }
                                }
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
                            IconButton(onClick = { showThemeFontSettings = true }) {
                                Icon(Icons.Default.Palette, contentDescription = "Theme", tint = MaterialTheme.colorScheme.primary)
                            }
                        },
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        AppView.values().forEach { view ->
                            val isSelected = currentView == view
                            val icon = when (view) {
                                AppView.DASHBOARD -> Icons.Default.Dashboard
                                AppView.LEDGER -> Icons.AutoMirrored.Filled.ReceiptLong
                                AppView.BUDGET -> Icons.Default.Assessment
                                AppView.BUDGET_MAKER -> Icons.Default.Calculate
                                AppView.REPORTS -> Icons.Default.Assessment
                                AppView.ACCOUNTS -> Icons.Default.AccountBalance
                                AppView.EXPENSES -> Icons.Default.Category
                                AppView.INCOME -> Icons.Default.Payments
                                AppView.RECURRING_BILLS -> Icons.Default.EventRepeat
                                AppView.BACKUP_SYNC -> Icons.Default.CloudSync
                            }
                            NavigationRailItem(
                                selected = isSelected,
                                onClick = { selectView(view) },
                                icon = { Icon(icon, contentDescription = view.name) },
                                label = { Text(view.name.take(4), fontSize = 10.sp) },
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
                // EXPANDED TABLET / DESKTOP LAYOUT: Permanent Navigation Drawer + Wide Canvas
                PermanentNavigationDrawer(
                    drawerContent = {
                        PermanentDrawerSheet(
                            modifier = Modifier.width(280.dp),
                            drawerContainerColor = MaterialTheme.colorScheme.surface
                        ) {
                            DrawerContent(
                                viewModel = viewModel,
                                currentView = currentView,
                                onSelectView = { selectView(it) },
                                onOpenCalculator = { showGlobalCalculator = true },
                                onOpenThemeFontSettings = { showThemeFontSettings = true },
                                onOpenAutofillSettings = { showAutofillSettingsDialog = true },
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
                                    IconButton(onClick = { showThemeFontSettings = true }) {
                                        Icon(Icons.Default.Palette, contentDescription = "Theme & Fonts", tint = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(onClick = { showGlobalCalculator = true }) {
                                        Icon(Icons.Default.Calculate, contentDescription = "Calculator", tint = MaterialTheme.colorScheme.primary)
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
private fun AppFab(
    currentView: AppView,
    onAddTransaction: () -> Unit,
    onAddAccount: () -> Unit,
    onAddCategory: (CategoryType) -> Unit
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
    } else if (currentView in listOf(AppView.EXPENSES, AppView.INCOME)) {
        val catType = if (currentView == AppView.EXPENSES) CategoryType.EXPENSE else CategoryType.INCOME
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
                            text = "v3.4",
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
            text = "MENU & MANAGEMENT",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )

        // Navigation Items
        DrawerItemRow(
            title = LanguageHelper.getString("dashboard", languageMode),
            icon = Icons.Default.Dashboard,
            iconTint = MaterialTheme.colorScheme.primary,
            isSelected = currentView == AppView.DASHBOARD,
            onClick = { onSelectView(AppView.DASHBOARD) }
        )

        DrawerItemRow(
            title = LanguageHelper.getString("transactions", languageMode),
            icon = Icons.AutoMirrored.Filled.ReceiptLong,
            iconTint = SolidTransfer,
            isSelected = currentView == AppView.LEDGER,
            onClick = { onSelectView(AppView.LEDGER) }
        )

        DrawerItemRow(
            title = LanguageHelper.getString("budget", languageMode),
            icon = Icons.Default.Assessment,
            iconTint = MaterialTheme.colorScheme.primary,
            badge = if (budgetCount > 0) "$budgetCount" else null,
            isSelected = currentView == AppView.BUDGET,
            onClick = { onSelectView(AppView.BUDGET) }
        )

        DrawerItemRow(
            title = LanguageHelper.getString("budget_maker", languageMode),
            icon = Icons.Default.Calculate,
            iconTint = MaterialTheme.colorScheme.primary,
            isSelected = currentView == AppView.BUDGET_MAKER,
            onClick = { onSelectView(AppView.BUDGET_MAKER) }
        )

        DrawerItemRow(
            title = LanguageHelper.getString("reports", languageMode),
            icon = Icons.Default.Assessment,
            iconTint = MaterialTheme.colorScheme.primary,
            isSelected = currentView == AppView.REPORTS,
            onClick = { onSelectView(AppView.REPORTS) }
        )

        DrawerItemRow(
            title = LanguageHelper.getString("accounts", languageMode),
            icon = Icons.Default.AccountBalance,
            iconTint = MaterialTheme.colorScheme.primary,
            badge = "$accountsCount",
            isSelected = currentView == AppView.ACCOUNTS,
            onClick = { onSelectView(AppView.ACCOUNTS) }
        )

        DrawerItemRow(
            title = LanguageHelper.getString("expenses", languageMode),
            icon = Icons.Default.Category,
            iconTint = SolidExpense,
            badge = "$expensesCount",
            isSelected = currentView == AppView.EXPENSES,
            onClick = { onSelectView(AppView.EXPENSES) }
        )

        DrawerItemRow(
            title = LanguageHelper.getString("incomes", languageMode),
            icon = Icons.Default.Payments,
            iconTint = SolidIncome,
            badge = "$incomeCount",
            isSelected = currentView == AppView.INCOME,
            onClick = { onSelectView(AppView.INCOME) }
        )

        DrawerItemRow(
            title = "Recurring & Bills",
            icon = Icons.Default.EventRepeat,
            iconTint = MaterialTheme.colorScheme.primary,
            badge = "$recurringCount",
            isSelected = currentView == AppView.RECURRING_BILLS,
            onClick = { onSelectView(AppView.RECURRING_BILLS) }
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
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "ডেমো ডাটা রিসেট করুন" else "Reset Sample Demo Data",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "TOOLS & STYLING",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )

        // Quick Sync in Menu with Last Sync Timestamp
        val backupConfig by viewModel.backupSettingsConfig.collectAsStateWithLifecycle()
        val lastSyncStr = if (backupConfig.lastSyncTimestamp > 0L) {
            java.text.SimpleDateFormat("dd MMM, hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(backupConfig.lastSyncTimestamp))
        } else "Never"

        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .clickable { viewModel.triggerQuickSync() }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = "Quick Sync",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Quick Sync",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Last: $lastSyncStr",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    Text(
                        text = "Sync",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }

        // 7 Themes & Fonts Customizer
        DrawerItemRow(
            title = if (languageMode == LanguageMode.BANGLA) "থিম ও ফন্ট কাস্টমাইজ" else "Theme & Font Styling",
            icon = Icons.Default.Palette,
            iconTint = MaterialTheme.colorScheme.primary,
            isSelected = false,
            onClick = onOpenThemeFontSettings
        )

        // Autofill Settings
        DrawerItemRow(
            title = LanguageHelper.getString("autofill_settings", languageMode),
            icon = Icons.Default.AutoAwesome,
            iconTint = MaterialTheme.colorScheme.primary,
            isSelected = false,
            onClick = onOpenAutofillSettings
        )

        // Backup, Restore & Sync
        DrawerItemRow(
            title = "Backup, Restore & Sync",
            icon = Icons.Default.CloudSync,
            iconTint = MaterialTheme.colorScheme.primary,
            isSelected = currentView == AppView.BACKUP_SYNC,
            onClick = { onSelectView(AppView.BACKUP_SYNC) }
        )

        // Calculator
        DrawerItemRow(
            title = LanguageHelper.getString("calculator", languageMode),
            icon = Icons.Default.Calculate,
            iconTint = MaterialTheme.colorScheme.primary,
            isSelected = false,
            onClick = onOpenCalculator
        )

        // Language toggle
        NavigationDrawerItem(
            label = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(LanguageHelper.getString("language", languageMode), fontWeight = FontWeight.Medium)
                    Text(
                        text = when (languageMode) {
                            LanguageMode.ENGLISH -> "EN"
                            LanguageMode.BANGLA -> "বাং"
                            LanguageMode.BILINGUAL -> "EN/বাং"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            icon = { Icon(Icons.Default.Translate, contentDescription = null) },
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
            accountsWithBalances = accountsWithBalances,
            onAddTransactionClick = { onAddTransactionWithType(TransactionType.EXPENSE) },
            onTransactionClick = onEditTransaction
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
        AppView.RECURRING_BILLS -> RecurringBillsScreen(
            viewModel = viewModel,
            bills = recurringBills,
            languageMode = languageMode
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
        AppView.BUDGET -> LanguageHelper.getString("budget", languageMode)
        AppView.BUDGET_MAKER -> LanguageHelper.getString("budget_maker", languageMode)
        AppView.REPORTS -> LanguageHelper.getString("reports", languageMode)
        AppView.ACCOUNTS -> LanguageHelper.getString("balance_sheet", languageMode)
        AppView.EXPENSES -> LanguageHelper.getString("expenses", languageMode)
        AppView.INCOME -> LanguageHelper.getString("incomes", languageMode)
        AppView.RECURRING_BILLS -> "Recurring Bills"
        AppView.BACKUP_SYNC -> "Backup, Restore & Sync"
    }
}
