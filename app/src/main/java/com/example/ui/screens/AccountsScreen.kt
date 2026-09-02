package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Account
import com.example.data.model.AccountType
import com.example.ui.components.AccountCardItem
import com.example.ui.components.EmptyStatePlaceholder
import com.example.ui.dialogs.AddEditAccountDialog
import com.example.ui.theme.EmeraldIncome
import com.example.ui.theme.CrimsonExpense
import com.example.ui.theme.BluePrimary
import com.example.ui.viewmodel.FinanceViewModel
import com.example.util.Formatters

@Composable
fun AccountsScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val currencyCode by viewModel.selectedCurrency.collectAsState()
    val accounts by viewModel.allAccounts.collectAsState()
    val balanceSheet by viewModel.balanceSheet.collectAsState()

    var showAddAccountDialog by remember { mutableStateOf(false) }

    val bankAccounts = accounts.filter {
        it.type == AccountType.CHECKING || it.type == AccountType.SAVINGS || it.type == AccountType.CASH
    }
    val liabilityAccounts = accounts.filter {
        it.type == AccountType.CREDIT_CARD || it.type == AccountType.LOAN
    }
    val investmentAccounts = accounts.filter {
        it.type == AccountType.INVESTMENT || it.type == AccountType.ASSET
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddAccountDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Account") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Balance Sheet Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    )
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Balance Sheet Statement",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Total Assets",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = Formatters.formatCurrency(balanceSheet.totalAssets, currencyCode),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldIncome
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Total Liabilities",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = Formatters.formatCurrency(balanceSheet.totalLiabilities, currencyCode),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = CrimsonExpense
                                )
                            }
                        }

                        Divider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Net Equity / Net Worth",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = Formatters.formatCurrency(balanceSheet.netWorth, currencyCode),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // 1. Bank & Cash Accounts
            item {
                SectionHeader(
                    title = "Bank & Cash",
                    count = bankAccounts.size,
                    total = bankAccounts.sumOf { it.balance },
                    currencyCode = currencyCode
                )
            }
            if (bankAccounts.isEmpty()) {
                item {
                    Text(
                        text = "No bank accounts added",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            } else {
                items(bankAccounts) { acc ->
                    AccountCardItem(
                        account = acc,
                        currencyCode = currencyCode,
                        onClick = {}
                    )
                }
            }

            // 2. Credit Cards & Debts
            item {
                SectionHeader(
                    title = "Credit Cards & Loans",
                    count = liabilityAccounts.size,
                    total = liabilityAccounts.sumOf { it.balance },
                    currencyCode = currencyCode
                )
            }
            if (liabilityAccounts.isEmpty()) {
                item {
                    Text(
                        text = "No liabilities or credit cards",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            } else {
                items(liabilityAccounts) { acc ->
                    AccountCardItem(
                        account = acc,
                        currencyCode = currencyCode,
                        onClick = {}
                    )
                }
            }

            // 3. Investments & Assets
            item {
                SectionHeader(
                    title = "Investments & Portfolio",
                    count = investmentAccounts.size,
                    total = investmentAccounts.sumOf { it.balance },
                    currencyCode = currencyCode
                )
            }
            if (investmentAccounts.isEmpty()) {
                item {
                    Text(
                        text = "No investment portfolios recorded",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            } else {
                items(investmentAccounts) { acc ->
                    AccountCardItem(
                        account = acc,
                        currencyCode = currencyCode,
                        onClick = {}
                    )
                }
            }
        }
    }

    if (showAddAccountDialog) {
        AddEditAccountDialog(
            onDismiss = { showAddAccountDialog = false },
            onSave = { name, type, balance, colorHex, iconName ->
                viewModel.addAccount(name, type, balance, currencyCode, colorHex, iconName)
            }
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    count: Int,
    total: Double,
    currencyCode: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$title ($count)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = Formatters.formatCurrency(total, currencyCode),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = if (total < 0) CrimsonExpense else MaterialTheme.colorScheme.onSurface
        )
    }
}
