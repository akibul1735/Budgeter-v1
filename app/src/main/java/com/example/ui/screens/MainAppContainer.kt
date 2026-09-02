package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Account
import com.example.data.model.Category
import com.example.data.model.CategoryType
import com.example.data.model.LanguageMode
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.ui.components.LanguageSelector
import com.example.ui.components.PopupCalculatorDialog
import com.example.ui.dialogs.AddEditAccountDialog
import com.example.ui.dialogs.AddEditCategoryDialog
import com.example.ui.dialogs.AddEditTransactionSheet
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidPrimary
import com.example.ui.viewmodel.BudgetViewModel
import com.example.util.LanguageHelper
import kotlinx.coroutines.launch

enum class AppView {
    DASHBOARD,
    LEDGER,
    REPORTS,
    ACCOUNTS,
    EXPENSES,
    INCOME
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContainer(
    viewModel: BudgetViewModel
) {
    val languageMode by viewModel.languageMode.collectAsStateWithLifecycle()
    val overview by viewModel.financialOverview.collectAsStateWithLifecycle()
    val accountsWithBalances by viewModel.accountsWithBalances.collectAsStateWithLifecycle()
    val allAccounts by viewModel.allAccounts.collectAsStateWithLifecycle()
    val allCategories by viewModel.allCategories.collectAsStateWithLifecycle()
    val transactionsWithDetails by viewModel.transactionsWithDetails.collectAsStateWithLifecycle()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var currentView by remember { mutableStateOf(AppView.DASHBOARD) }

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

    val bottomNavItems = listOf(
        Triple(LanguageHelper.getString("dashboard", languageMode), Icons.Default.Dashboard, AppView.DASHBOARD),
        Triple(LanguageHelper.getString("ledger", languageMode), Icons.Default.ReceiptLong, AppView.LEDGER),
        Triple(LanguageHelper.getString("reports", languageMode), Icons.Default.Assessment, AppView.REPORTS)
    )

    val isSubView = currentView in listOf(AppView.ACCOUNTS, AppView.EXPENSES, AppView.INCOME)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.width(300.dp)
            ) {
                // Modern Solid Drawer Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SolidPrimary)
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
                                    text = "v2.0",
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
                            text = LanguageHelper.formatCurrency(overview.netWorth, languageMode),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Double entry balance status
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (overview.isLedgerBalanced) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (overview.isLedgerBalanced) SolidIncome else SolidExpense,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (overview.isLedgerBalanced) "Dr = Cr Balanced" else "Unbalanced",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.9f),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Menu Section: Management (Expenses, Income, Accounts)
                Text(
                    text = "MENU & MANAGEMENT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )

                // 1. Accounts
                NavigationDrawerItem(
                    label = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = LanguageHelper.getString("accounts", languageMode),
                                fontWeight = if (currentView == AppView.ACCOUNTS) FontWeight.Bold else FontWeight.Medium
                            )
                            Text(
                                text = "${allAccounts.size}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    },
                    icon = { Icon(Icons.Default.AccountBalance, contentDescription = null, tint = SolidPrimary) },
                    selected = currentView == AppView.ACCOUNTS,
                    onClick = {
                        currentView = AppView.ACCOUNTS
                        scope.launch { drawerState.close() }
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = SolidPrimary.copy(alpha = 0.12f),
                        selectedTextColor = SolidPrimary
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                )

                // 2. Expenses
                NavigationDrawerItem(
                    label = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = LanguageHelper.getString("expenses", languageMode),
                                fontWeight = if (currentView == AppView.EXPENSES) FontWeight.Bold else FontWeight.Medium
                            )
                            Text(
                                text = "${allCategories.count { it.type == CategoryType.EXPENSE && it.parentId == null }}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    },
                    icon = { Icon(Icons.Default.Category, contentDescription = null, tint = SolidExpense) },
                    selected = currentView == AppView.EXPENSES,
                    onClick = {
                        currentView = AppView.EXPENSES
                        scope.launch { drawerState.close() }
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = SolidExpense.copy(alpha = 0.12f),
                        selectedTextColor = SolidExpense
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                )

                // 3. Income
                NavigationDrawerItem(
                    label = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = LanguageHelper.getString("incomes", languageMode),
                                fontWeight = if (currentView == AppView.INCOME) FontWeight.Bold else FontWeight.Medium
                            )
                            Text(
                                text = "${allCategories.count { it.type == CategoryType.INCOME && it.parentId == null }}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    },
                    icon = { Icon(Icons.Default.Payments, contentDescription = null, tint = SolidIncome) },
                    selected = currentView == AppView.INCOME,
                    onClick = {
                        currentView = AppView.INCOME
                        scope.launch { drawerState.close() }
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = SolidIncome.copy(alpha = 0.12f),
                        selectedTextColor = SolidIncome
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp, horizontal = 16.dp))

                // Menu Section: Tools & Preferences
                Text(
                    text = "TOOLS & SETTINGS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )

                // Quick Calculator
                NavigationDrawerItem(
                    label = { Text(LanguageHelper.getString("calculator", languageMode), fontWeight = FontWeight.Medium) },
                    icon = { Icon(Icons.Default.Calculate, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    selected = false,
                    onClick = {
                        showGlobalCalculator = true
                        scope.launch { drawerState.close() }
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                )

                // Language quick toggle
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
                                color = SolidPrimary
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
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = when (currentView) {
                                AppView.DASHBOARD -> LanguageHelper.getString("app_name", languageMode)
                                AppView.LEDGER -> LanguageHelper.getString("ledger", languageMode)
                                AppView.REPORTS -> LanguageHelper.getString("reports", languageMode)
                                AppView.ACCOUNTS -> LanguageHelper.getString("accounts", languageMode)
                                AppView.EXPENSES -> LanguageHelper.getString("expenses", languageMode)
                                AppView.INCOME -> LanguageHelper.getString("incomes", languageMode)
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    },
                    navigationIcon = {
                        if (isSubView) {
                            IconButton(onClick = { currentView = AppView.DASHBOARD }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        } else {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                        }
                    },
                    actions = {
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
                                onClick = { currentView = view },
                                icon = { Icon(icon, contentDescription = label) },
                                label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.White,
                                    selectedTextColor = SolidPrimary,
                                    indicatorColor = SolidPrimary
                                ),
                                modifier = Modifier.testTag("nav_${view.name.lowercase()}")
                            )
                        }
                    }
                }
            },
            floatingActionButton = {
                if (currentView in listOf(AppView.DASHBOARD, AppView.LEDGER)) {
                    FloatingActionButton(
                        onClick = {
                            editingTransaction = null
                            presetTxType = TransactionType.EXPENSE
                            showAddTransactionSheet = true
                        },
                        containerColor = SolidPrimary,
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier.testTag("main_fab_add_tx")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Transaction")
                    }
                } else if (currentView == AppView.ACCOUNTS) {
                    FloatingActionButton(
                        onClick = {
                            editingAccount = null
                            presetAccountParentId = null
                            showAddAccountDialog = true
                        },
                        containerColor = SolidPrimary,
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier.testTag("accounts_fab_add")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Account")
                    }
                } else if (currentView in listOf(AppView.EXPENSES, AppView.INCOME)) {
                    val catType = if (currentView == AppView.EXPENSES) CategoryType.EXPENSE else CategoryType.INCOME
                    FloatingActionButton(
                        onClick = {
                            editingCategory = null
                            presetCategoryType = catType
                            presetCategoryParentId = null
                            showAddCategoryDialog = true
                        },
                        containerColor = if (catType == CategoryType.EXPENSE) SolidExpense else SolidIncome,
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier.testTag("categories_fab_add")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Category")
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (currentView) {
                    AppView.DASHBOARD -> DashboardScreen(
                        overview = overview,
                        accountsWithBalances = accountsWithBalances,
                        recentTransactions = transactionsWithDetails,
                        languageMode = languageMode,
                        onAddTransactionClick = { type ->
                            presetTxType = type
                            editingTransaction = null
                            showAddTransactionSheet = true
                        },
                        onTransactionClick = { tx ->
                            editingTransaction = tx
                            showAddTransactionSheet = true
                        },
                        onViewAllTransactionsClick = {
                            currentView = AppView.LEDGER
                        }
                    )
                    AppView.LEDGER -> LedgerScreen(
                        transactions = transactionsWithDetails,
                        languageMode = languageMode,
                        onAddTransactionClick = {
                            editingTransaction = null
                            presetTxType = TransactionType.EXPENSE
                            showAddTransactionSheet = true
                        },
                        onTransactionClick = { tx ->
                            editingTransaction = tx
                            showAddTransactionSheet = true
                        }
                    )
                    AppView.REPORTS -> ReportsScreen(
                        overview = overview,
                        accountsWithBalances = accountsWithBalances,
                        languageMode = languageMode
                    )
                    AppView.ACCOUNTS -> AccountsScreen(
                        accountsWithBalances = accountsWithBalances,
                        languageMode = languageMode,
                        onAddAccountClick = {
                            editingAccount = null
                            presetAccountParentId = null
                            showAddAccountDialog = true
                        },
                        onAddSubAccountClick = { parent ->
                            editingAccount = null
                            presetAccountParentId = parent.id
                            showAddAccountDialog = true
                        },
                        onEditAccountClick = { acc ->
                            editingAccount = acc
                            presetAccountParentId = acc.parentId
                            showAddAccountDialog = true
                        }
                    )
                    AppView.EXPENSES -> CategoriesScreen(
                        categories = allCategories,
                        languageMode = languageMode,
                        initialTab = 0,
                        onAddCategoryClick = { type ->
                            editingCategory = null
                            presetCategoryType = type
                            presetCategoryParentId = null
                            showAddCategoryDialog = true
                        },
                        onAddSubCategoryClick = { parent ->
                            editingCategory = null
                            presetCategoryType = parent.type
                            presetCategoryParentId = parent.id
                            showAddCategoryDialog = true
                        },
                        onEditCategoryClick = { cat ->
                            editingCategory = cat
                            presetCategoryType = cat.type
                            presetCategoryParentId = cat.parentId
                            showAddCategoryDialog = true
                        }
                    )
                    AppView.INCOME -> CategoriesScreen(
                        categories = allCategories,
                        languageMode = languageMode,
                        initialTab = 1,
                        onAddCategoryClick = { type ->
                            editingCategory = null
                            presetCategoryType = type
                            presetCategoryParentId = null
                            showAddCategoryDialog = true
                        },
                        onAddSubCategoryClick = { parent ->
                            editingCategory = null
                            presetCategoryType = parent.type
                            presetCategoryParentId = parent.id
                            showAddCategoryDialog = true
                        },
                        onEditCategoryClick = { cat ->
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

    // Modal Dialogs
    if (showAddTransactionSheet) {
        AddEditTransactionSheet(
            accounts = allAccounts,
            categories = allCategories,
            languageMode = languageMode,
            existingTransaction = editingTransaction,
            onDismiss = { showAddTransactionSheet = false },
            onSave = { tx -> viewModel.saveTransaction(tx) },
            onDelete = { tx -> viewModel.deleteTransaction(tx) }
        )
    }

    if (showAddAccountDialog) {
        val parentAccounts = allAccounts.filter { it.parentId == null }
        AddEditAccountDialog(
            parentAccounts = parentAccounts,
            languageMode = languageMode,
            existingAccount = editingAccount,
            defaultParentId = presetAccountParentId,
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
            onValueConfirmed = { /* Calculator confirmed */ }
        )
    }
}
