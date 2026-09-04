package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Account
import com.example.data.model.Category
import com.example.data.model.LanguageMode
import com.example.data.model.MonthlyBudget
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.data.model.TransactionWithDetails
import com.example.data.repository.AccountWithBalance
import com.example.data.repository.FinancialOverview
import com.example.ui.components.DoubleEntryFlowBadge
import com.example.ui.components.PopupCalculatorDialog
import com.example.ui.screens.dashboard.BudgetSummaryCard
import com.example.ui.screens.dashboard.BudgetSummarySettingsDialog
import com.example.ui.screens.dashboard.CalendarSettingsDialog
import com.example.ui.screens.dashboard.CalendarSummaryCard
import com.example.ui.screens.dashboard.CustomizeDashboardCardsDialog
import com.example.ui.screens.dashboard.DailySummaryCard
import com.example.ui.screens.dashboard.DailySummarySettingsDialog
import com.example.ui.screens.dashboard.FavoriteAccountsCard
import com.example.ui.screens.dashboard.FavoriteAccountsSelectionDialog
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidExpenseContainer
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidIncomeContainer
import com.example.ui.theme.SolidOnExpenseContainer
import com.example.ui.theme.SolidOnIncomeContainer
import com.example.ui.theme.SolidPrimary
import com.example.ui.theme.SolidPrimaryContainer
import com.example.ui.theme.SolidTransfer
import com.example.util.BudgetChartShape
import com.example.util.BudgetSummaryType
import com.example.util.CalendarDisplayMode
import com.example.util.DailySummaryMode
import com.example.util.DailySummaryPeriod
import com.example.util.DashboardCardType
import com.example.util.DashboardConfig
import com.example.util.DateUtils
import com.example.util.IconHelper
import com.example.util.LanguageHelper

@Composable
fun DashboardScreen(
    overview: FinancialOverview,
    accountsWithBalances: List<AccountWithBalance>,
    recentTransactions: List<TransactionWithDetails>,
    allCategories: List<Category> = emptyList(),
    monthlyBudgets: List<MonthlyBudget> = emptyList(),
    dashboardConfig: DashboardConfig,
    languageMode: LanguageMode,
    onAddTransactionClick: (TransactionType) -> Unit,
    onTransactionClick: (Transaction) -> Unit,
    onViewAllTransactionsClick: () -> Unit,
    onAccountClick: (Account) -> Unit = {},
    onToggleCardVisibility: (DashboardCardType, Boolean) -> Unit,
    onReorderCards: (fromIndex: Int, toIndex: Int) -> Unit,
    onUpdateDailySummarySettings: (DailySummaryMode, DailySummaryPeriod, Boolean, Boolean) -> Unit,
    onUpdateBudgetSummarySettings: (BudgetChartShape, BudgetSummaryType, Int, Boolean, Boolean) -> Unit,
    onUpdateCalendarSettings: (CalendarDisplayMode, Boolean, Boolean) -> Unit,
    onUpdateFavoriteAccounts: (Set<Long>) -> Unit,
    onResetDashboardDefaults: () -> Unit
) {
    var showStandAloneCalculator by remember { mutableStateOf(false) }
    var showCustomizeCardsDialog by remember { mutableStateOf(false) }
    var showDailySettingsDialog by remember { mutableStateOf(false) }
    var showBudgetSettingsDialog by remember { mutableStateOf(false) }
    var showCalendarSettingsDialog by remember { mutableStateOf(false) }
    var showFavoriteAccountsPicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_screen")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Render Cards dynamically based on user configuration order and visibility
            dashboardConfig.cardOrder.forEach { cardType ->
                if (cardType in dashboardConfig.visibleCards) {
                    when (cardType) {
                        DashboardCardType.NET_WORTH -> {
                            item(key = "card_net_worth") {
                                NetWorthDashboardCard(
                                    overview = overview,
                                    languageMode = languageMode
                                )
                            }
                        }

                        DashboardCardType.DAILY_SUMMARY -> {
                            item(key = "card_daily_summary") {
                                DailySummaryCard(
                                    transactions = recentTransactions,
                                    mode = dashboardConfig.dailySummaryMode,
                                    period = dashboardConfig.dailySummaryPeriod,
                                    showValues = dashboardConfig.dailyShowValues,
                                    showAverages = dashboardConfig.dailyShowAverages,
                                    languageMode = languageMode,
                                    onModeChange = { newMode ->
                                        onUpdateDailySummarySettings(
                                            newMode,
                                            dashboardConfig.dailySummaryPeriod,
                                            dashboardConfig.dailyShowValues,
                                            dashboardConfig.dailyShowAverages
                                        )
                                    },
                                    onPeriodChange = { newPeriod ->
                                        onUpdateDailySummarySettings(
                                            dashboardConfig.dailySummaryMode,
                                            newPeriod,
                                            dashboardConfig.dailyShowValues,
                                            dashboardConfig.dailyShowAverages
                                        )
                                    },
                                    onOpenSettings = { showDailySettingsDialog = true }
                                )
                            }
                        }

                        DashboardCardType.BUDGET_SUMMARY -> {
                            item(key = "card_budget_summary") {
                                BudgetSummaryCard(
                                    transactions = recentTransactions,
                                    allCategories = allCategories,
                                    monthlyBudgets = monthlyBudgets,
                                    chartShape = dashboardConfig.budgetChartShape,
                                    categoryType = dashboardConfig.budgetCategoryType,
                                    maxCategories = dashboardConfig.budgetMaxCategories,
                                    showPercentages = dashboardConfig.budgetShowPercentages,
                                    showTodayPace = dashboardConfig.budgetShowTodayPace,
                                    languageMode = languageMode,
                                    onCategoryTypeChange = { newType ->
                                        onUpdateBudgetSummarySettings(
                                            dashboardConfig.budgetChartShape,
                                            newType,
                                            dashboardConfig.budgetMaxCategories,
                                            dashboardConfig.budgetShowPercentages,
                                            dashboardConfig.budgetShowTodayPace
                                        )
                                    },
                                    onOpenSettings = { showBudgetSettingsDialog = true }
                                )
                            }
                        }

                        DashboardCardType.FAVORITE_ACCOUNTS -> {
                            item(key = "card_favorite_accounts") {
                                FavoriteAccountsCard(
                                    accountsWithBalances = accountsWithBalances,
                                    favoriteAccountIds = dashboardConfig.favoriteAccountIds,
                                    languageMode = languageMode,
                                    onOpenAccountPicker = { showFavoriteAccountsPicker = true },
                                    onAccountClick = onAccountClick
                                )
                            }
                        }

                        DashboardCardType.CALENDAR_VIEW -> {
                            item(key = "card_calendar_view") {
                                CalendarSummaryCard(
                                    transactions = recentTransactions,
                                    displayMode = dashboardConfig.calendarDisplayMode,
                                    showIncome = dashboardConfig.calendarShowIncome,
                                    showExpense = dashboardConfig.calendarShowExpense,
                                    languageMode = languageMode,
                                    onOpenSettings = { showCalendarSettingsDialog = true },
                                    onTransactionClick = onTransactionClick
                                )
                            }
                        }

                        DashboardCardType.CASH_FLOW -> {
                            item(key = "card_cash_flow") {
                                CashFlowRow(
                                    overview = overview,
                                    languageMode = languageMode
                                )
                            }
                        }

                        DashboardCardType.FINANCIAL_OVERVIEW -> {
                            item(key = "card_financial_overview") {
                                FinancialOverviewCard(
                                    overview = overview,
                                    languageMode = languageMode
                                )
                            }
                        }

                        DashboardCardType.QUICK_ACTIONS -> {
                            item(key = "card_quick_actions") {
                                QuickActionsRow(
                                    languageMode = languageMode,
                                    onAddTransactionClick = onAddTransactionClick,
                                    onOpenCalculator = { showStandAloneCalculator = true }
                                )
                            }
                        }

                        DashboardCardType.RECENT_TRANSACTIONS -> {
                            item(key = "card_recent_transactions") {
                                RecentTransactionsCard(
                                    recentTransactions = recentTransactions,
                                    languageMode = languageMode,
                                    onTransactionClick = onTransactionClick,
                                    onViewAllClick = onViewAllTransactionsClick
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Customize Cards Button
            item(key = "bottom_customize_cards_button") {
                OutlinedButton(
                    onClick = { showCustomizeCardsDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                ) {
                    Icon(
                        imageVector = Icons.Default.DashboardCustomize,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = LanguageHelper.getString("customize_cards", languageMode),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }

    // Modal Dialogs for Chart Settings and Customization
    if (showCustomizeCardsDialog) {
        CustomizeDashboardCardsDialog(
            config = dashboardConfig,
            languageMode = languageMode,
            onDismiss = { showCustomizeCardsDialog = false },
            onToggleCard = onToggleCardVisibility,
            onMoveCard = onReorderCards,
            onResetDefaults = onResetDashboardDefaults
        )
    }

    if (showDailySettingsDialog) {
        DailySummarySettingsDialog(
            currentMode = dashboardConfig.dailySummaryMode,
            currentPeriod = dashboardConfig.dailySummaryPeriod,
            currentShowValues = dashboardConfig.dailyShowValues,
            currentShowAverages = dashboardConfig.dailyShowAverages,
            languageMode = languageMode,
            onDismiss = { showDailySettingsDialog = false },
            onSave = { m, p, sv, sa ->
                onUpdateDailySummarySettings(m, p, sv, sa)
                showDailySettingsDialog = false
            }
        )
    }

    if (showBudgetSettingsDialog) {
        BudgetSummarySettingsDialog(
            currentShape = dashboardConfig.budgetChartShape,
            currentCategoryType = dashboardConfig.budgetCategoryType,
            currentMaxCategories = dashboardConfig.budgetMaxCategories,
            currentShowPercentages = dashboardConfig.budgetShowPercentages,
            currentShowTodayPace = dashboardConfig.budgetShowTodayPace,
            languageMode = languageMode,
            onDismiss = { showBudgetSettingsDialog = false },
            onSave = { s, t, mc, sp, tp ->
                onUpdateBudgetSummarySettings(s, t, mc, sp, tp)
                showBudgetSettingsDialog = false
            }
        )
    }

    if (showCalendarSettingsDialog) {
        CalendarSettingsDialog(
            currentDisplayMode = dashboardConfig.calendarDisplayMode,
            currentShowIncome = dashboardConfig.calendarShowIncome,
            currentShowExpense = dashboardConfig.calendarShowExpense,
            languageMode = languageMode,
            onDismiss = { showCalendarSettingsDialog = false },
            onSave = { dm, si, se ->
                onUpdateCalendarSettings(dm, si, se)
                showCalendarSettingsDialog = false
            }
        )
    }

    if (showFavoriteAccountsPicker) {
        FavoriteAccountsSelectionDialog(
            allAccounts = accountsWithBalances,
            initialSelectedIds = dashboardConfig.favoriteAccountIds,
            languageMode = languageMode,
            onDismiss = { showFavoriteAccountsPicker = false },
            onSave = { selectedIds ->
                onUpdateFavoriteAccounts(selectedIds)
                showFavoriteAccountsPicker = false
            }
        )
    }

    if (showStandAloneCalculator) {
        PopupCalculatorDialog(
            languageMode = languageMode,
            onDismiss = { showStandAloneCalculator = false },
            onValueConfirmed = { /* Standalone calculator */ }
        )
    }
}

@Composable
private fun NetWorthDashboardCard(
    overview: FinancialOverview,
    languageMode: LanguageMode
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("net_worth_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SolidPrimary)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = LanguageHelper.getString("net_worth", languageMode),
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.85f)
                )

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (overview.isLedgerBalanced) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (overview.isLedgerBalanced) Color(0xFF6EE7B7) else Color(0xFFFCA5A5),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (overview.isLedgerBalanced) "Dr = Cr Balanced" else LanguageHelper.getString("unbalanced", languageMode),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = LanguageHelper.formatCurrency(overview.netWorth, languageMode),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF6EE7B7))
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "${LanguageHelper.getString("assets", languageMode)}: ${LanguageHelper.formatCurrency(overview.totalAssets, languageMode)}",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFCA5A5))
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "${LanguageHelper.getString("liabilities", languageMode)}: ${LanguageHelper.formatCurrency(overview.totalLiabilities, languageMode)}",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CashFlowRow(
    overview: FinancialOverview,
    languageMode: LanguageMode
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Monthly Income
        Surface(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp)),
            color = SolidIncomeContainer
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                    contentDescription = null,
                    tint = SolidIncome,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = LanguageHelper.getString("income", languageMode),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SolidOnIncomeContainer
                    )
                    Text(
                        text = LanguageHelper.formatCurrency(overview.monthlyIncome, languageMode),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SolidOnIncomeContainer
                    )
                }
            }
        }

        // Monthly Expense
        Surface(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp)),
            color = SolidExpenseContainer
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                    contentDescription = null,
                    tint = SolidExpense,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = LanguageHelper.getString("expense", languageMode),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SolidOnExpenseContainer
                    )
                    Text(
                        text = LanguageHelper.formatCurrency(overview.monthlyExpense, languageMode),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SolidOnExpenseContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun FinancialOverviewCard(
    overview: FinancialOverview,
    languageMode: LanguageMode
) {
    var showFormulaBreakdown by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("expendable_overview_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Payments,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = LanguageHelper.getString("financial_overview", languageMode),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(
                    onClick = { showFormulaBreakdown = !showFormulaBreakdown },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (showFormulaBreakdown) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Toggle Formula",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Primary Expendable Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (overview.expendable >= 0) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f) else Color(0xFFFFEBEE),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = LanguageHelper.getString("expendable", languageMode),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (overview.expendable >= 0) MaterialTheme.colorScheme.onPrimaryContainer else Color(0xFFC62828)
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (overview.expendable >= 0) Color(0xFF2E7D32) else Color(0xFFC62828)
                            ) {
                                Text(
                                    text = if (overview.expendable >= 0) "Safe" else "Deficit",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = LanguageHelper.formatCurrency(overview.expendable, languageMode),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (overview.expendable >= 0) MaterialTheme.colorScheme.onPrimaryContainer else Color(0xFFC62828)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = LanguageHelper.getString("expected_expendable", languageMode),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = LanguageHelper.formatCurrency(overview.expectedExpendable, languageMode),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(10.dp))

            // 6 Financial Indicators Grid
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = LanguageHelper.getString("current_assets", languageMode),
                        fontSize = 10.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = LanguageHelper.formatCurrency(overview.totalAssets, languageMode),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = LanguageHelper.getString("liabilities", languageMode),
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (overview.liabilitiesChange != 0.0) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (overview.liabilitiesChange > 0) "▲" else "▼",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (overview.liabilitiesChange > 0) Color(0xFFE53935) else Color(0xFF43A047)
                            )
                        }
                    }
                    Text(
                        text = LanguageHelper.formatCurrency(overview.totalLiabilities, languageMode),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = LanguageHelper.getString("remaining_expenses", languageMode),
                        fontSize = 10.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = LanguageHelper.formatCurrency(overview.remainingExpenses, languageMode),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E88E5)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = LanguageHelper.getString("additional_cost", languageMode),
                        fontSize = 10.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = LanguageHelper.formatCurrency(overview.additionalCost, languageMode),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (overview.additionalCost > 0) Color(0xFFE53935) else MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = LanguageHelper.getString("net_worth", languageMode),
                        fontSize = 10.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = LanguageHelper.formatCurrency(overview.netWorth, languageMode),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = LanguageHelper.getString("net_earnings", languageMode),
                        fontSize = 10.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = LanguageHelper.formatCurrency(overview.monthlyNetSavings, languageMode),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (overview.monthlyNetSavings >= 0) Color(0xFF43A047) else Color(0xFFE53935)
                    )
                }
            }

            AnimatedVisibility(visible = showFormulaBreakdown) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(10.dp)
                ) {
                    Text(
                        text = LanguageHelper.getString("expendable_breakdown", languageMode),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• Available Money = Total Assets (${LanguageHelper.formatCurrency(overview.availableMoney, languageMode)})",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "• Total Expense Budget = ${LanguageHelper.formatCurrency(overview.totalExpenseBudget, languageMode)}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "• Additional / Over-Budget Cost = ${LanguageHelper.formatCurrency(overview.additionalCost, languageMode)}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "• Expendable = ${LanguageHelper.formatCurrency(overview.availableMoney, languageMode)} - (${LanguageHelper.formatCurrency(overview.totalExpenseBudget, languageMode)} + ${LanguageHelper.formatCurrency(overview.additionalCost, languageMode)}) = ${LanguageHelper.formatCurrency(overview.expendable, languageMode)}",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• Potential Income = ${LanguageHelper.formatCurrency(overview.potentialIncome, languageMode)}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "• Expected Expendable = Expendable + Potential Income = ${LanguageHelper.formatCurrency(overview.expectedExpendable, languageMode)}",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActionsRow(
    languageMode: LanguageMode,
    onAddTransactionClick: (TransactionType) -> Unit,
    onOpenCalculator: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = { onAddTransactionClick(TransactionType.EXPENSE) },
            modifier = Modifier.weight(1f).height(44.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SolidExpense),
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(2.dp))
                Text(LanguageHelper.getString("expense", languageMode), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Button(
            onClick = { onAddTransactionClick(TransactionType.INCOME) },
            modifier = Modifier.weight(1f).height(44.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SolidIncome),
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(2.dp))
                Text(LanguageHelper.getString("income", languageMode), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Button(
            onClick = { onAddTransactionClick(TransactionType.TRANSFER) },
            modifier = Modifier.weight(1f).height(44.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SolidTransfer),
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(2.dp))
                Text(LanguageHelper.getString("transfer", languageMode), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Button(
            onClick = onOpenCalculator,
            modifier = Modifier.weight(1f).height(44.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SolidPrimary),
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(2.dp))
                Text(LanguageHelper.getString("calculator", languageMode), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun RecentTransactionsCard(
    recentTransactions: List<TransactionWithDetails>,
    languageMode: LanguageMode,
    onTransactionClick: (Transaction) -> Unit,
    onViewAllClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = LanguageHelper.getString("recent_transactions", languageMode),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (recentTransactions.isNotEmpty()) {
                    Text(
                        text = LanguageHelper.getString("all_transactions", languageMode),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SolidPrimary,
                        modifier = Modifier.clickable { onViewAllClick() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (recentTransactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = LanguageHelper.getString("no_transactions", languageMode),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    recentTransactions.take(5).forEach { item ->
                        val tx = item.transaction
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onTransactionClick(tx) },
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    val iconColor = when (tx.type) {
                                        TransactionType.EXPENSE -> SolidExpense
                                        TransactionType.INCOME -> SolidIncome
                                        TransactionType.TRANSFER -> SolidTransfer
                                    }
                                    val iconBg = when (tx.type) {
                                        TransactionType.EXPENSE -> SolidExpenseContainer
                                        TransactionType.INCOME -> SolidIncomeContainer
                                        TransactionType.TRANSFER -> SolidPrimaryContainer
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(iconBg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        val icon = when (tx.type) {
                                            TransactionType.EXPENSE -> IconHelper.getIconByName(item.category?.iconName ?: "Category")
                                            TransactionType.INCOME -> IconHelper.getIconByName(item.category?.iconName ?: "Payments")
                                            TransactionType.TRANSFER -> Icons.Default.SwapHoriz
                                        }
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = iconColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Column {
                                        val title = when (tx.type) {
                                            TransactionType.EXPENSE -> item.subCategory?.localizedName(languageMode)
                                                ?: item.category?.localizedName(languageMode)
                                                ?: "Expense"
                                            TransactionType.INCOME -> item.subCategory?.localizedName(languageMode)
                                                ?: item.category?.localizedName(languageMode)
                                                ?: "Income"
                                            TransactionType.TRANSFER -> LanguageHelper.getString("transfer", languageMode)
                                        }
                                        Text(
                                            text = title,
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        Spacer(modifier = Modifier.height(2.dp))

                                        DoubleEntryFlowBadge(
                                            item = item,
                                            languageMode = languageMode
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    val sign = when (tx.type) {
                                        TransactionType.EXPENSE -> "-"
                                        TransactionType.INCOME -> "+"
                                        TransactionType.TRANSFER -> ""
                                    }
                                    val amtColor = when (tx.type) {
                                        TransactionType.EXPENSE -> SolidExpense
                                        TransactionType.INCOME -> SolidIncome
                                        TransactionType.TRANSFER -> SolidTransfer
                                    }
                                    Text(
                                        text = "$sign${LanguageHelper.formatCurrency(tx.amount, languageMode)}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = amtColor
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = DateUtils.formatShortDate(tx.dateEpochMs, languageMode),
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
