package com.example.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Account
import com.example.data.model.Category
import com.example.data.model.CategoryType
import com.example.data.model.LanguageMode
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.ui.components.LanguageSelector
import com.example.ui.dialogs.AddEditAccountDialog
import com.example.ui.dialogs.AddEditCategoryDialog
import com.example.ui.dialogs.AddEditTransactionSheet
import com.example.ui.viewmodel.BudgetViewModel
import com.example.util.LanguageHelper

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

    var selectedNavIndex by remember { mutableIntStateOf(0) }

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

    val navItems = listOf(
        Triple(LanguageHelper.getString("dashboard", languageMode), Icons.Default.Dashboard, 0),
        Triple(LanguageHelper.getString("accounts", languageMode), Icons.Default.AccountBalance, 1),
        Triple(LanguageHelper.getString("categories", languageMode), Icons.Default.Category, 2),
        Triple(LanguageHelper.getString("ledger", languageMode), Icons.Default.ReceiptLong, 3),
        Triple(LanguageHelper.getString("reports", languageMode), Icons.Default.Assessment, 4)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = LanguageHelper.getString("app_name", languageMode),
                        fontWeight = FontWeight.Bold
                    )
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
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                navItems.forEach { (label, icon, index) ->
                    NavigationBarItem(
                        selected = selectedNavIndex == index,
                        onClick = { selectedNavIndex = index },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label, maxLines = 1) },
                        modifier = Modifier.testTag("nav_item_$index")
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingTransaction = null
                    presetTxType = TransactionType.EXPENSE
                    showAddTransactionSheet = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("main_fab_add_tx")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedNavIndex) {
                0 -> DashboardScreen(
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
                        selectedNavIndex = 3 // Switch to Ledger tab
                    }
                )
                1 -> AccountsScreen(
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
                2 -> CategoriesScreen(
                    categories = allCategories,
                    languageMode = languageMode,
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
                3 -> LedgerScreen(
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
                4 -> ReportsScreen(
                    overview = overview,
                    accountsWithBalances = accountsWithBalances,
                    languageMode = languageMode
                )
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
}
