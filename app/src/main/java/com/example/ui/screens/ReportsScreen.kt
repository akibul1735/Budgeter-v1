package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccountType
import com.example.data.model.LanguageMode
import com.example.data.repository.AccountWithBalance
import com.example.data.repository.FinancialOverview
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidIncomeContainer
import com.example.ui.theme.SolidPrimary
import com.example.util.LanguageHelper

@Composable
fun ReportsScreen(
    overview: FinancialOverview,
    accountsWithBalances: List<AccountWithBalance>,
    languageMode: LanguageMode
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("reports_screen"),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Top Header
        item {
            Text(
                text = LanguageHelper.getString("reports", languageMode),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Tabs: Trial Balance, Balance Sheet, Income Statement
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                contentColor = SolidPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = SolidPrimary
                    )
                },
                modifier = Modifier.clip(RoundedCornerShape(10.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Trial Balance", fontSize = 11.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Balance Sheet", fontSize = 11.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Income & Expense", fontSize = 11.sp, fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Medium) }
                )
            }
        }

        when (selectedTab) {
            0 -> {
                // 1. TRIAL BALANCE TAB
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = LanguageHelper.getString("trial_balance", languageMode),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (overview.isLedgerBalanced) SolidIncomeContainer else MaterialTheme.colorScheme.errorContainer
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (overview.isLedgerBalanced) Icons.Default.CheckCircle else Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = if (overview.isLedgerBalanced) SolidIncome else SolidExpense,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (overview.isLedgerBalanced) "Dr = Cr Balanced" else "Unbalanced",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (overview.isLedgerBalanced) SolidIncome else SolidExpense
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Total Debits vs Credits
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = LanguageHelper.getString("debit", languageMode),
                                        fontSize = 11.sp,
                                        color = SolidPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = LanguageHelper.formatCurrency(overview.totalDebits, languageMode),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = LanguageHelper.getString("credit", languageMode),
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.outline,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = LanguageHelper.formatCurrency(overview.totalCredits, languageMode),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // 2. BALANCE SHEET TAB (Assets vs Liabilities & Equity)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = LanguageHelper.getString("balance_sheet", languageMode),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Total Assets
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(LanguageHelper.getString("total_assets", languageMode), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Text(LanguageHelper.formatCurrency(overview.totalAssets, languageMode), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SolidIncome)
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Total Liabilities
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(LanguageHelper.getString("total_liabilities", languageMode), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Text(LanguageHelper.formatCurrency(overview.totalLiabilities, languageMode), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SolidExpense)
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(6.dp))

                            // Net Worth / Equity
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(LanguageHelper.getString("net_worth", languageMode), fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = SolidPrimary)
                                Text(LanguageHelper.formatCurrency(overview.netWorth, languageMode), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = SolidPrimary)
                            }
                        }
                    }
                }
            }

            2 -> {
                // 3. INCOME STATEMENT TAB
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = LanguageHelper.getString("income_statement", languageMode),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(LanguageHelper.getString("incomes", languageMode), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Text(LanguageHelper.formatCurrency(overview.monthlyIncome, languageMode), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SolidIncome)
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(LanguageHelper.getString("expenses", languageMode), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Text(LanguageHelper.formatCurrency(overview.monthlyExpense, languageMode), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SolidExpense)
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Net Savings / উদ্বৃত্ত", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = SolidPrimary)
                                val netColor = if (overview.monthlyNetSavings >= 0) SolidIncome else SolidExpense
                                Text(LanguageHelper.formatCurrency(overview.monthlyNetSavings, languageMode), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = netColor)
                            }
                        }
                    }
                }
            }
        }
    }
}
