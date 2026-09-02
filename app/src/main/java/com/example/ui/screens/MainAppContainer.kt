package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.ui.dialogs.AddEditTransactionSheet
import com.example.ui.dialogs.CurrencyPickerDialog
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.EmeraldIncome
import com.example.ui.viewmodel.FinanceViewModel

enum class MainNavTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    DASHBOARD("Dashboard", Icons.Default.Dashboard),
    TRANSACTIONS("Ledger", Icons.AutoMirrored.Filled.ReceiptLong),
    ACCOUNTS("Accounts", Icons.Default.AccountBalance),
    BUDGETS("Budgets", Icons.Default.PieChart),
    ANALYTICS("Analytics", Icons.AutoMirrored.Filled.ShowChart)
}

enum class SubScreen {
    NONE,
    BILLS,
    GOALS,
    INSIGHTS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContainer(
    viewModel: FinanceViewModel = viewModel()
) {
    var selectedTab by remember { mutableStateOf(MainNavTab.DASHBOARD) }
    var activeSubScreen by remember { mutableStateOf(SubScreen.NONE) }
    var showAddTransactionSheet by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }

    val currencyCode by viewModel.selectedCurrency.collectAsState()
    val accounts by viewModel.allAccounts.collectAsState()
    val categories by viewModel.allCategories.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (activeSubScreen != SubScreen.NONE) {
                        Text(
                            text = when (activeSubScreen) {
                                SubScreen.BILLS -> "Bills & Subscriptions"
                                SubScreen.GOALS -> "Savings & Goals"
                                SubScreen.INSIGHTS -> "AI Financial Advisor"
                                SubScreen.NONE -> ""
                            },
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(BluePrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Bluecoins",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = selectedTab.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    if (activeSubScreen != SubScreen.NONE) {
                        IconButton(onClick = { activeSubScreen = SubScreen.NONE }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                },
                actions = {
                    // Currency Picker Chip
                    AssistChip(
                        onClick = { showCurrencyDialog = true },
                        label = {
                            Text(
                                text = currencyCode,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.CurrencyExchange,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        modifier = Modifier.height(32.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    // AI Insights Button
                    IconButton(
                        onClick = { activeSubScreen = SubScreen.INSIGHTS }
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Smart Insights",
                            tint = BluePrimary
                        )
                    }

                    // More dropdown menu
                    Box {
                        IconButton(onClick = { showMoreMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More options"
                            )
                        }

                        DropdownMenu(
                            expanded = showMoreMenu,
                            onDismissRequest = { showMoreMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Bills & Subscriptions") },
                                onClick = {
                                    showMoreMenu = false
                                    activeSubScreen = SubScreen.BILLS
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.EventRepeat, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Savings Goals") },
                                onClick = {
                                    showMoreMenu = false
                                    activeSubScreen = SubScreen.GOALS
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Savings, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("AI Financial Advisor") },
                                onClick = {
                                    showMoreMenu = false
                                    activeSubScreen = SubScreen.INSIGHTS
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Change Currency ($currencyCode)") },
                                onClick = {
                                    showMoreMenu = false
                                    showCurrencyDialog = true
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Paid, contentDescription = null)
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            if (activeSubScreen == SubScreen.NONE) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                    windowInsets = WindowInsets.navigationBars
                ) {
                    MainNavTab.values().forEach { tab ->
                        val isSelected = selectedTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { selectedTab = tab },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.title
                                )
                            },
                            label = {
                                Text(
                                    text = tab.title,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (activeSubScreen == SubScreen.NONE && selectedTab != MainNavTab.ACCOUNTS) {
                FloatingActionButton(
                    onClick = { showAddTransactionSheet = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Transaction",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeSubScreen) {
                SubScreen.BILLS -> {
                    RecurringBillsScreen(viewModel = viewModel)
                }
                SubScreen.GOALS -> {
                    GoalsScreen(viewModel = viewModel)
                }
                SubScreen.INSIGHTS -> {
                    SmartInsightsScreen(
                        viewModel = viewModel,
                        onNavigateToBudgets = {
                            activeSubScreen = SubScreen.NONE
                            selectedTab = MainNavTab.BUDGETS
                        },
                        onNavigateToBills = {
                            activeSubScreen = SubScreen.BILLS
                        },
                        onNavigateToGoals = {
                            activeSubScreen = SubScreen.GOALS
                        }
                    )
                }
                SubScreen.NONE -> {
                    when (selectedTab) {
                        MainNavTab.DASHBOARD -> DashboardScreen(
                            viewModel = viewModel,
                            onNavigateToTransactions = { selectedTab = MainNavTab.TRANSACTIONS },
                            onNavigateToAccounts = { selectedTab = MainNavTab.ACCOUNTS },
                            onNavigateToBudgets = { selectedTab = MainNavTab.BUDGETS },
                            onNavigateToAnalytics = { selectedTab = MainNavTab.ANALYTICS },
                            onNavigateToBills = { activeSubScreen = SubScreen.BILLS },
                            onNavigateToGoals = { activeSubScreen = SubScreen.GOALS },
                            onNavigateToInsights = { activeSubScreen = SubScreen.INSIGHTS },
                            onOpenAddTransaction = { showAddTransactionSheet = true }
                        )
                        MainNavTab.TRANSACTIONS -> TransactionsScreen(
                            viewModel = viewModel,
                            onAddTransactionClick = { showAddTransactionSheet = true }
                        )
                        MainNavTab.ACCOUNTS -> AccountsScreen(
                            viewModel = viewModel
                        )
                        MainNavTab.BUDGETS -> BudgetsScreen(
                            viewModel = viewModel
                        )
                        MainNavTab.ANALYTICS -> AnalyticsScreen(
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }

    if (showAddTransactionSheet) {
        AddEditTransactionSheet(
            accounts = accounts,
            categories = categories,
            currencyCode = currencyCode,
            onDismiss = { showAddTransactionSheet = false },
            onSave = { title, amount, type, accId, toAccId, catId, dateMs, notes, tags, status ->
                viewModel.addTransaction(
                    title = title,
                    amount = amount,
                    type = type,
                    accountId = accId,
                    toAccountId = toAccId,
                    categoryId = catId,
                    dateEpochMs = dateMs,
                    notes = notes,
                    tags = tags,
                    status = status
                )
            }
        )
    }

    if (showCurrencyDialog) {
        CurrencyPickerDialog(
            currentCurrency = currencyCode,
            onCurrencySelected = { viewModel.setCurrency(it) },
            onDismiss = { showCurrencyDialog = false }
        )
    }
}
