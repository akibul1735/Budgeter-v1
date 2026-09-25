package com.example.budgeter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.budgeter.data.model.*
import com.example.budgeter.ui.components.*
import com.example.budgeter.ui.screens.*
import com.example.budgeter.ui.theme.BudgeterTheme
import com.example.budgeter.ui.theme.EmeraldGreenPrimary
import com.example.budgeter.viewmodel.BudgeterViewModel
import com.example.budgeter.viewmodel.BudgeterViewModelFactory

enum class NavigationTab(val label: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home),
    LEDGER("Ledger", Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong),
    BUDGETS("Budgets", Icons.Filled.Savings, Icons.Outlined.Savings),
    ACCOUNTS("Accounts", Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet),
    ANALYTICS("Analytics", Icons.Filled.PieChart, Icons.Outlined.PieChart),
    SETTINGS("Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
}

class MainActivity : ComponentActivity() {
    private val viewModel: BudgeterViewModel by viewModels {
        val app = application as BudgeterApp
        BudgeterViewModelFactory(app.repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            BudgeterTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(viewModel: BudgeterViewModel) {
    var currentTab by remember { mutableStateOf(NavigationTab.HOME) }

    // Dialog states
    var showAddEditTxDialog by remember { mutableStateOf(false) }
    var txToEdit by remember { mutableStateOf<Transaction?>(null) }
    var initialTxType by remember { mutableStateOf(TransactionType.EXPENSE) }

    var showAddEditBudgetDialog by remember { mutableStateOf(false) }
    var budgetToEdit by remember { mutableStateOf<Budget?>(null) }
    var budgetCategoryToAssign by remember { mutableStateOf<Category?>(null) }

    var showAddEditAccountDialog by remember { mutableStateOf(false) }
    var accountToEdit by remember { mutableStateOf<Account?>(null) }

    val allCategories by viewModel.allCategories.collectAsStateWithLifecycle()
    val allAccounts by viewModel.allAccounts.collectAsStateWithLifecycle()
    val selectedMonthYear by viewModel.selectedMonthYear.collectAsStateWithLifecycle()
    val currencySymbol by viewModel.currencySymbol.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = currentTab.label,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = EmeraldGreenPrimary
                ),
                actions = {
                    if (currentTab == NavigationTab.HOME || currentTab == NavigationTab.LEDGER) {
                        IconButton(
                            onClick = {
                                txToEdit = null
                                initialTxType = TransactionType.EXPENSE
                                showAddEditTxDialog = true
                            },
                            modifier = Modifier.testTag("top_add_transaction_btn")
                        ) {
                            Icon(Icons.Default.AddCircle, contentDescription = "Add Transaction", tint = EmeraldGreenPrimary)
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationTab.values().forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.label
                            )
                        },
                        label = { Text(tab.label, style = MaterialTheme.typography.labelSmall) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = EmeraldGreenPrimary,
                            selectedTextColor = EmeraldGreenPrimary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (currentTab) {
                NavigationTab.HOME -> {
                    HomeScreen(
                        viewModel = viewModel,
                        onNavigateToLedger = { currentTab = NavigationTab.LEDGER },
                        onNavigateToBudgets = { currentTab = NavigationTab.BUDGETS },
                        onOpenAddTransaction = { type ->
                            txToEdit = null
                            initialTxType = type
                            showAddEditTxDialog = true
                        },
                        onSelectTransaction = { tx ->
                            txToEdit = tx
                            showAddEditTxDialog = true
                        }
                    )
                }
                NavigationTab.LEDGER -> {
                    LedgerScreen(
                        viewModel = viewModel,
                        onOpenAddTransaction = {
                            txToEdit = null
                            initialTxType = TransactionType.EXPENSE
                            showAddEditTxDialog = true
                        },
                        onSelectTransaction = { tx ->
                            txToEdit = tx
                            showAddEditTxDialog = true
                        }
                    )
                }
                NavigationTab.BUDGETS -> {
                    BudgetScreen(
                        viewModel = viewModel,
                        onOpenAddBudget = { category ->
                            budgetToEdit = null
                            budgetCategoryToAssign = category
                            showAddEditBudgetDialog = true
                        },
                        onEditBudget = { budget, category ->
                            budgetToEdit = budget
                            budgetCategoryToAssign = category
                            showAddEditBudgetDialog = true
                        }
                    )
                }
                NavigationTab.ACCOUNTS -> {
                    AccountsScreen(
                        viewModel = viewModel,
                        onOpenAddAccount = {
                            accountToEdit = null
                            showAddEditAccountDialog = true
                        },
                        onEditAccount = { acc ->
                            accountToEdit = acc
                            showAddEditAccountDialog = true
                        }
                    )
                }
                NavigationTab.ANALYTICS -> {
                    AnalyticsScreen(viewModel = viewModel)
                }
                NavigationTab.SETTINGS -> {
                    SettingsScreen(viewModel = viewModel)
                }
            }
        }
    }

    // Dialogs
    if (showAddEditTxDialog) {
        AddEditTransactionDialog(
            transactionToEdit = txToEdit,
            initialType = initialTxType,
            categories = allCategories,
            accounts = allAccounts,
            currencySymbol = currencySymbol,
            onDismiss = { showAddEditTxDialog = false },
            onSave = { tx ->
                viewModel.saveTransaction(tx)
            },
            onDelete = { tx ->
                viewModel.deleteTransaction(tx)
            }
        )
    }

    if (showAddEditBudgetDialog) {
        AddEditBudgetDialog(
            budgetToEdit = budgetToEdit,
            defaultCategoryId = budgetCategoryToAssign?.id,
            categories = allCategories,
            currentMonthYear = selectedMonthYear,
            currencySymbol = currencySymbol,
            onDismiss = { showAddEditBudgetDialog = false },
            onSave = { budget ->
                viewModel.saveBudget(budget)
            },
            onDelete = { budget ->
                viewModel.deleteBudget(budget)
            }
        )
    }

    if (showAddEditAccountDialog) {
        AddEditAccountDialog(
            accountToEdit = accountToEdit,
            currencySymbol = currencySymbol,
            onDismiss = { showAddEditAccountDialog = false },
            onSave = { acc ->
                viewModel.saveAccount(acc)
            },
            onDelete = { acc ->
                viewModel.deleteAccount(acc)
            }
        )
    }
}
