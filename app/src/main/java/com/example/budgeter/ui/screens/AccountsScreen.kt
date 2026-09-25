package com.example.budgeter.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.budgeter.data.model.Account
import com.example.budgeter.ui.components.formatCurrency
import com.example.budgeter.ui.theme.*
import com.example.budgeter.viewmodel.BudgeterViewModel

@Composable
fun AccountsScreen(
    viewModel: BudgeterViewModel,
    onOpenAddAccount: () -> Unit,
    onEditAccount: (Account) -> Unit
) {
    val allAccounts by viewModel.allAccounts.collectAsStateWithLifecycle()
    val currencySymbol by viewModel.currencySymbol.collectAsStateWithLifecycle()

    val totalAssets = allAccounts.filter { !it.isDebt }.sumOf { it.currentBalance }
    val totalDebt = allAccounts.filter { it.isDebt }.sumOf { it.currentBalance }
    val netWorth = totalAssets - totalDebt

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onOpenAddAccount,
                containerColor = EmeraldGreenPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_account_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Account")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .testTag("accounts_screen"),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Net Worth Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = EmeraldGreenPrimary)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Net Balance",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formatCurrency(netWorth, currencySymbol),
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Total Assets", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                                Text(formatCurrency(totalAssets, currencySymbol), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = EmeraldGreenPrimaryContainer)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Total Liabilities / Debt", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                                Text(formatCurrency(totalDebt, currencySymbol), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = ExpenseRedContainer)
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "All Wallets & Accounts (${allAccounts.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            items(allAccounts, key = { it.id }) { acc ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onEditAccount(acc) }
                        .testTag("account_card_${acc.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (acc.type) {
                                            "CASH" -> EmeraldGreenPrimary.copy(alpha = 0.15f)
                                            "MOBILE_MONEY" -> Color(0xFFE91E63).copy(alpha = 0.15f)
                                            "CREDIT_CARD", "LOAN" -> ExpenseRed.copy(alpha = 0.15f)
                                            else -> TransferBlue.copy(alpha = 0.15f)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (acc.type) {
                                        "CASH" -> Icons.Default.Payments
                                        "MOBILE_MONEY" -> Icons.Default.PhoneAndroid
                                        "CREDIT_CARD" -> Icons.Default.CreditCard
                                        "SAVINGS" -> Icons.Default.Savings
                                        "LOAN" -> Icons.Default.MoneyOff
                                        else -> Icons.Default.AccountBalance
                                    },
                                    contentDescription = null,
                                    tint = when (acc.type) {
                                        "CASH" -> EmeraldGreenPrimary
                                        "MOBILE_MONEY" -> Color(0xFFE91E63)
                                        "CREDIT_CARD", "LOAN" -> ExpenseRed
                                        else -> TransferBlue
                                    },
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = acc.name,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    text = acc.type.replace("_", " "),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = formatCurrency(acc.currentBalance, currencySymbol),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (acc.isDebt) ExpenseRed else MaterialTheme.colorScheme.primary
                            )
                            if (acc.isDebt && acc.debtLimit > 0) {
                                Text(
                                    text = "Limit: ${formatCurrency(acc.debtLimit, currencySymbol)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}
