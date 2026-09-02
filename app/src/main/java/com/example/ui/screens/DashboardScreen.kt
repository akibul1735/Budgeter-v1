package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Account
import com.example.data.model.BalanceSheetSummary
import com.example.data.model.CashflowSummary
import com.example.data.model.CategorySpend
import com.example.data.model.TransactionType
import com.example.data.model.TransactionWithDetails
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.FinanceViewModel
import com.example.util.Formatters
import com.example.util.IconHelper

@Composable
fun DashboardScreen(
    viewModel: FinanceViewModel,
    onNavigateToTransactions: () -> Unit,
    onNavigateToAccounts: () -> Unit,
    onNavigateToBudgets: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToBills: () -> Unit,
    onNavigateToGoals: () -> Unit,
    onNavigateToInsights: () -> Unit,
    onOpenAddTransaction: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currencyCode by viewModel.selectedCurrency.collectAsState()
    val balanceSheet by viewModel.balanceSheet.collectAsState()
    val cashflow by viewModel.cashflow.collectAsState()
    val accounts by viewModel.allAccounts.collectAsState()
    val categorySpends by viewModel.categorySpends.collectAsState()
    val recentTransactions by viewModel.allTransactionsWithDetails.collectAsState()
    val insights by viewModel.dynamicInsights.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Hero Net Worth Card
        item {
            NetWorthHeroCard(
                balanceSheet = balanceSheet,
                currencyCode = currencyCode,
                onViewAccounts = onNavigateToAccounts
            )
        }

        // 2. AI Smart Insight Banner
        item {
            insights.firstOrNull()?.let { insight ->
                InsightBannerCard(
                    insight = insight,
                    onActionClick = {
                        when (insight.type) {
                            com.example.data.model.InsightType.ALERT -> onNavigateToBudgets()
                            com.example.data.model.InsightType.NEUTRAL -> onNavigateToBills()
                            com.example.data.model.InsightType.TIP -> onNavigateToGoals()
                            com.example.data.model.InsightType.POSITIVE -> onNavigateToGoals()
                        }
                    }
                )
            }
        }

        // 3. Quick 1-Tap Action Presets
        item {
            QuickPresetsSection(
                onPresetClick = { title, amount, type, catMatch ->
                    viewModel.quickAddPreset(title, amount, type, catMatch)
                }
            )
        }

        // 4. Monthly Cashflow Metric Card
        item {
            MonthlyCashflowCard(
                cashflow = cashflow,
                currencyCode = currencyCode,
                onViewAnalytics = onNavigateToAnalytics
            )
        }

        // 5. Account Balances Preview (Horizontal Carousel)
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Accounts & Wallets",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = onNavigateToAccounts) {
                        Text("See All (${accounts.size})", fontWeight = FontWeight.SemiBold)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(accounts.take(6)) { acc ->
                        AccountMiniCard(
                            account = acc,
                            currencyCode = currencyCode,
                            onClick = onNavigateToAccounts
                        )
                    }
                }
            }
        }

        // 6. Category Spending Donut Chart Preview
        if (categorySpends.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Monthly Spending",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            TextButton(onClick = onNavigateToBudgets) {
                                Text("Budgets", fontWeight = FontWeight.SemiBold)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        CategoryDonutChart(
                            spends = categorySpends,
                            currencyCode = currencyCode
                        )
                    }
                }
            }
        }

        // 7. Recent Transactions List
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Transactions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onNavigateToTransactions) {
                    Text("View All", fontWeight = FontWeight.SemiBold)
                }
            }
        }

        if (recentTransactions.isEmpty()) {
            item {
                EmptyStatePlaceholder(
                    title = "No transactions yet",
                    message = "Tap the + button to record your first expense or income.",
                    icon = Icons.Default.ReceiptLong
                )
            }
        } else {
            items(recentTransactions.take(5)) { item ->
                TransactionRowItem(
                    item = item,
                    currencyCode = currencyCode,
                    onDelete = { viewModel.deleteTransaction(item.transaction) }
                )
            }
        }
    }
}

@Composable
private fun NetWorthHeroCard(
    balanceSheet: BalanceSheetSummary,
    currencyCode: String,
    onViewAccounts: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .clickable { onViewAccounts() },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF0F3B77), Color(0xFF1E56A0), Color(0xFF16697A))
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(EmeraldIncome)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "TOTAL NET WORTH",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.8f),
                            letterSpacing = 1.sp
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = Formatters.formatCurrency(balanceSheet.netWorth, currencyCode),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Assets vs Liabilities row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.12f))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Total Assets",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Text(
                            text = Formatters.formatCurrency(balanceSheet.totalAssets, currencyCode),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF86EFAC)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(32.dp)
                            .background(Color.White.copy(alpha = 0.2f))
                    )

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Total Liabilities",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Text(
                            text = Formatters.formatCurrency(balanceSheet.totalLiabilities, currencyCode),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFCA5A5)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthlyCashflowCard(
    cashflow: CashflowSummary,
    currencyCode: String,
    onViewAnalytics: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { onViewAnalytics() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Cashflow This Month",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Savings: ${String.format("%.0f", cashflow.savingsRate)}%",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (cashflow.savingsRate >= 0) EmeraldIncome else CrimsonExpense
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Income
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(EmeraldIncomeBg)
                        .padding(12.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = null,
                                tint = EmeraldIncome,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Income",
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldIncome,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = Formatters.formatCurrency(cashflow.totalIncome, currencyCode),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF065F46)
                        )
                    }
                }

                // Expense
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CrimsonExpenseBg)
                        .padding(12.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = null,
                                tint = CrimsonExpense,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Expense",
                                style = MaterialTheme.typography.labelSmall,
                                color = CrimsonExpense,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = Formatters.formatCurrency(cashflow.totalExpense, currencyCode),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF991B1B)
                        )
                    }
                }

                // Net
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "Net Saved",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = Formatters.formatCurrency(cashflow.netSavings, currencyCode),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (cashflow.netSavings >= 0) EmeraldIncome else CrimsonExpense
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickPresetsSection(
    onPresetClick: (title: String, amount: Double, type: TransactionType, categoryMatch: String) -> Unit
) {
    Column {
        Text(
            text = "Quick 1-Tap Log",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickPresetButton(
                icon = Icons.Default.LocalCafe,
                label = "Coffee ($6)",
                color = Color(0xFFF97316),
                modifier = Modifier.weight(1f)
            ) {
                onPresetClick("Coffee & Cafe", 6.50, TransactionType.EXPENSE, "Dining")
            }

            QuickPresetButton(
                icon = Icons.Default.ShoppingCart,
                label = "Groceries ($50)",
                color = Color(0xFF10B981),
                modifier = Modifier.weight(1f)
            ) {
                onPresetClick("Grocery Run", 50.00, TransactionType.EXPENSE, "Groceries")
            }

            QuickPresetButton(
                icon = Icons.Default.LocalGasStation,
                label = "Fuel ($45)",
                color = Color(0xFF06B6D4),
                modifier = Modifier.weight(1f)
            ) {
                onPresetClick("Gas Station", 45.00, TransactionType.EXPENSE, "Transport")
            }

            QuickPresetButton(
                icon = Icons.Default.Restaurant,
                label = "Dining ($25)",
                color = Color(0xFFEC4899),
                modifier = Modifier.weight(1f)
            ) {
                onPresetClick("Lunch / Dining", 25.00, TransactionType.EXPENSE, "Dining")
            }
        }
    }
}

@Composable
private fun QuickPresetButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun AccountMiniCard(
    account: Account,
    currencyCode: String,
    onClick: () -> Unit
) {
    val accColor = IconHelper.parseColor(account.colorHex)
    Card(
        modifier = Modifier
            .width(150.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(accColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = IconHelper.getIconByName(account.iconName),
                        contentDescription = null,
                        tint = accColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = account.name,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = Formatters.formatCurrency(account.balance, currencyCode),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (account.balance < 0) CrimsonExpense else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = account.type.displayName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
