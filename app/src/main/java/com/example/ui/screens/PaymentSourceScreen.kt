package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoveToInbox
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Account
import com.example.data.model.AccountRequirementAnalysis
import com.example.data.model.AccountRequirementItem
import com.example.data.model.Category
import com.example.data.model.FundAllocationSuggestion
import com.example.data.model.LanguageMode
import com.example.data.model.MonthlyBudget
import com.example.data.model.PaymentSourceAnalysisOverview
import com.example.data.model.RecurringBill
import com.example.data.model.RequirementCalculationBasis
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.data.model.TransactionWithDetails
import com.example.data.repository.AccountWithBalance
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidPrimary
import com.example.ui.theme.SolidTransfer
import com.example.util.DateUtils
import com.example.util.IconHelper
import com.example.util.LanguageHelper
import com.example.util.PaymentSourceCalculator
import java.util.Calendar

private enum class AccountFilter {
    ALL,
    SHORTFALL_ONLY,
    SURPLUS_ONLY,
    BALANCED
}

@Composable
fun PaymentSourceScreen(
    allAccounts: List<Account>,
    accountsWithBalances: List<AccountWithBalance>,
    allCategories: List<Category>,
    monthlyBudgets: List<MonthlyBudget>,
    allTransactions: List<TransactionWithDetails>,
    recurringBills: List<RecurringBill>,
    selectedYear: Int,
    selectedMonth: Int,
    languageMode: LanguageMode,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSetCurrentMonth: () -> Unit,
    onExecuteTransfer: (fromAccount: Account, toAccount: Account, amount: Double) -> Unit,
    onAddTransactionWithAccount: (Account, TransactionType) -> Unit,
    onEditAccount: (Account) -> Unit
) {
    var calculationBasis by remember { mutableStateOf(RequirementCalculationBasis.BUDGET_AMOUNT) }
    var accountFilter by remember { mutableStateOf(AccountFilter.ALL) }
    var expandedAccountIds by remember { mutableStateOf(setOf<Long>()) }
    var showHelpDialog by remember { mutableStateOf(false) }

    val analysisOverview = remember(
        selectedYear,
        selectedMonth,
        calculationBasis,
        allAccounts,
        accountsWithBalances,
        allCategories,
        monthlyBudgets,
        allTransactions,
        recurringBills
    ) {
        PaymentSourceCalculator.calculateAnalysis(
            year = selectedYear,
            month = selectedMonth,
            basis = calculationBasis,
            allAccounts = allAccounts,
            accountsWithBalances = accountsWithBalances,
            allCategories = allCategories,
            monthlyBudgets = monthlyBudgets,
            allTransactions = allTransactions,
            recurringBills = recurringBills
        )
    }

    val filteredAnalyses = remember(analysisOverview, accountFilter) {
        when (accountFilter) {
            AccountFilter.ALL -> analysisOverview.accountAnalyses
            AccountFilter.SHORTFALL_ONLY -> analysisOverview.accountAnalyses.filter { it.isShortfall }
            AccountFilter.SURPLUS_ONLY -> analysisOverview.accountAnalyses.filter { it.isSurplus }
            AccountFilter.BALANCED -> analysisOverview.accountAnalyses.filter { it.isBalanced }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("payment_source_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Month Navigation Header
        item {
            MonthNavigationCard(
                year = selectedYear,
                month = selectedMonth,
                languageMode = languageMode,
                onPrev = onPrevMonth,
                onNext = onNextMonth,
                onToday = onSetCurrentMonth,
                onHelp = { showHelpDialog = true }
            )
        }

        // 2. Calculation Basis Switcher (Budget vs Remaining)
        item {
            CalculationBasisSelector(
                selectedBasis = calculationBasis,
                languageMode = languageMode,
                onSelectBasis = { calculationBasis = it }
            )
        }

        // 3. Hero Summary & Answer Card
        item {
            PaymentSourceSummaryCard(
                overview = analysisOverview,
                languageMode = languageMode
            )
        }

        // 4. Fund Allocation Insights (Smart Transfer Recommendations)
        item {
            FundAllocationInsightCard(
                suggestions = analysisOverview.transferSuggestions,
                accountsNeedingFundsCount = analysisOverview.accountsNeedingFundsCount,
                totalShortfall = analysisOverview.totalShortfall,
                totalSurplus = analysisOverview.totalSurplus,
                languageMode = languageMode,
                onExecuteTransfer = onExecuteTransfer
            )
        }

        // 5. Account Filter Bar & Header
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = LanguageHelper.getString("payment_source", languageMode),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${filteredAnalyses.size} ${if (languageMode == LanguageMode.BANGLA) "হিসাব" else "Accounts"}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = accountFilter == AccountFilter.ALL,
                        onClick = { accountFilter = AccountFilter.ALL },
                        label = { Text(LanguageHelper.getString("filter_all", languageMode), fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                    FilterChip(
                        selected = accountFilter == AccountFilter.SHORTFALL_ONLY,
                        onClick = { accountFilter = AccountFilter.SHORTFALL_ONLY },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(SolidExpense))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${LanguageHelper.getString("filter_shortfall", languageMode)} (${analysisOverview.accountsNeedingFundsCount})",
                                    fontSize = 12.sp
                                )
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SolidExpense.copy(alpha = 0.15f),
                            selectedLabelColor = SolidExpense
                        )
                    )
                    FilterChip(
                        selected = accountFilter == AccountFilter.SURPLUS_ONLY,
                        onClick = { accountFilter = AccountFilter.SURPLUS_ONLY },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(SolidIncome))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${LanguageHelper.getString("filter_surplus", languageMode)} (${analysisOverview.accountsWithSurplusCount})",
                                    fontSize = 12.sp
                                )
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SolidIncome.copy(alpha = 0.15f),
                            selectedLabelColor = SolidIncome
                        )
                    )
                }
            }
        }

        // 6. Account Requirement Analysis Cards
        if (filteredAnalyses.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = LanguageHelper.getString("no_accounts_match", languageMode),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.outline,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(filteredAnalyses, key = { it.account.id }) { analysis ->
                val isExpanded = expandedAccountIds.contains(analysis.account.id)
                AccountRequirementCard(
                    analysis = analysis,
                    isExpanded = isExpanded,
                    languageMode = languageMode,
                    onToggleExpand = {
                        expandedAccountIds = if (isExpanded) {
                            expandedAccountIds - analysis.account.id
                        } else {
                            expandedAccountIds + analysis.account.id
                        }
                    },
                    onFundAccount = {
                        // Find surplus account with highest funds to transfer
                        val bestSurplus = analysisOverview.accountAnalyses.filter { it.isSurplus && it.account.id != analysis.account.id }
                            .maxByOrNull { it.surplus }
                        if (bestSurplus != null) {
                            val amountToMove = minOf(analysis.shortfall, bestSurplus.surplus)
                            onExecuteTransfer(bestSurplus.account, analysis.account, amountToMove)
                        } else {
                            onAddTransactionWithAccount(analysis.account, TransactionType.INCOME)
                        }
                    },
                    onAddExpense = { onAddTransactionWithAccount(analysis.account, TransactionType.EXPENSE) },
                    onAddIncome = { onAddTransactionWithAccount(analysis.account, TransactionType.INCOME) },
                    onAccountClick = { onEditAccount(analysis.account) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showHelpDialog) {
        PaymentSourceHelpDialog(
            languageMode = languageMode,
            onDismiss = { showHelpDialog = false }
        )
    }
}

@Composable
private fun MonthNavigationCard(
    year: Int,
    month: Int,
    languageMode: LanguageMode,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onToday: () -> Unit,
    onHelp: () -> Unit
) {
    val monthName = DateUtils.getMonthName(month, languageMode)
    val yearStr = if (languageMode == LanguageMode.BANGLA) LanguageHelper.toBanglaDigits(year.toString()) else year.toString()

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPrev) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Previous Month",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.clickable { onToday() }
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$monthName $yearStr",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onHelp) {
                    Icon(
                        imageVector = Icons.Default.HelpOutline,
                        contentDescription = "Help",
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
                IconButton(onClick = onNext) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next Month",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun CalculationBasisSelector(
    selectedBasis: RequirementCalculationBasis,
    languageMode: LanguageMode,
    onSelectBasis: (RequirementCalculationBasis) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = LanguageHelper.getString("calculation_basis", languageMode),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RequirementCalculationBasis.values().forEach { basis ->
                    val isSelected = selectedBasis == basis
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onSelectBasis(basis) }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = basis.getTitle(languageMode),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = selectedBasis.getDescription(languageMode),
                fontSize = 11.5.sp,
                color = MaterialTheme.colorScheme.outline,
                lineHeight = 15.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

@Composable
private fun PaymentSourceSummaryCard(
    overview: PaymentSourceAnalysisOverview,
    languageMode: LanguageMode
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Savings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = LanguageHelper.getString("monthly_fund_summary", languageMode),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (overview.isOverallSurplus) SolidIncome.copy(alpha = 0.15f) else SolidExpense.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (overview.isOverallSurplus) {
                            "+${LanguageHelper.formatCurrency(overview.netStatus, languageMode)}"
                        } else {
                            "-${LanguageHelper.formatCurrency(overview.totalShortfall, languageMode)}"
                        },
                        color = if (overview.isOverallSurplus) SolidIncome else SolidExpense,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3-column stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryMetricBox(
                    title = LanguageHelper.getString("required_amount", languageMode),
                    amount = overview.totalRequired,
                    color = SolidExpense,
                    languageMode = languageMode,
                    modifier = Modifier.weight(1f)
                )
                SummaryMetricBox(
                    title = LanguageHelper.getString("available_amount", languageMode),
                    amount = overview.totalAvailable,
                    color = MaterialTheme.colorScheme.primary,
                    languageMode = languageMode,
                    modifier = Modifier.weight(1f)
                )
                SummaryMetricBox(
                    title = if (overview.isOverallSurplus) LanguageHelper.getString("surplus", languageMode) else LanguageHelper.getString("shortfall", languageMode),
                    amount = if (overview.isOverallSurplus) overview.totalSurplus else overview.totalShortfall,
                    color = if (overview.isOverallSurplus) SolidIncome else SolidExpense,
                    languageMode = languageMode,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Final answer highlight
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = if (overview.accountsNeedingFundsCount == 0) Icons.Default.CheckCircle else Icons.Default.Info,
                        contentDescription = null,
                        tint = if (overview.accountsNeedingFundsCount == 0) SolidIncome else SolidTransfer,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = overview.getSummaryAnswer(languageMode),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryMetricBox(
    title: String,
    amount: Double,
    color: Color,
    languageMode: LanguageMode,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 10.5.sp,
                color = MaterialTheme.colorScheme.outline,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = LanguageHelper.formatCurrency(amount, languageMode),
                fontWeight = FontWeight.Bold,
                fontSize = 13.5.sp,
                color = color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun FundAllocationInsightCard(
    suggestions: List<FundAllocationSuggestion>,
    accountsNeedingFundsCount: Int,
    totalShortfall: Double,
    totalSurplus: Double,
    languageMode: LanguageMode,
    onExecuteTransfer: (fromAccount: Account, toAccount: Account, amount: Double) -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(SolidTransfer.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = null,
                        tint = SolidTransfer,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = LanguageHelper.getString("fund_allocation_insight", languageMode),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.5.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = LanguageHelper.getString("fund_allocation_subtitle", languageMode),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (suggestions.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    suggestions.forEach { suggestion ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = BorderStroke(1.dp, SolidTransfer.copy(alpha = 0.25f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Flow Route
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AccountPill(
                                            name = suggestion.fromAccount.localizedName(languageMode),
                                            color = SolidIncome
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = null,
                                            tint = SolidTransfer,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        AccountPill(
                                            name = suggestion.toAccount.localizedName(languageMode),
                                            color = SolidExpense
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            onExecuteTransfer(
                                                suggestion.fromAccount,
                                                suggestion.toAccount,
                                                suggestion.transferAmount
                                            )
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = SolidTransfer
                                        ),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.SyncAlt,
                                            contentDescription = null,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${LanguageHelper.getString("move", languageMode)} ${LanguageHelper.formatCurrency(suggestion.transferAmount, languageMode)}",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = suggestion.getReason(languageMode),
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            } else if (accountsNeedingFundsCount == 0) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SolidIncome.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SolidIncome,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = LanguageHelper.getString("all_funded", languageMode),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = SolidIncome
                        )
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SolidExpense.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = SolidExpense,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) {
                                "উদ্বৃত্ত হিসাবগুলোতে পর্যাপ্ত তহবিল নেই। ঘাটতি পূরণের জন্য নতুন তহবিল বা আয় জমা করতে হবে।"
                            } else {
                                "Surplus accounts do not have enough funds to cover shortfalls. External funding is required."
                            },
                            fontSize = 12.sp,
                            color = SolidExpense
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AccountPill(name: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Text(
            text = name,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun AccountRequirementCard(
    analysis: AccountRequirementAnalysis,
    isExpanded: Boolean,
    languageMode: LanguageMode,
    onToggleExpand: () -> Unit,
    onFundAccount: () -> Unit,
    onAddExpense: () -> Unit,
    onAddIncome: () -> Unit,
    onAccountClick: () -> Unit
) {
    val accountColor = try {
        Color(android.graphics.Color.parseColor(analysis.account.colorHex))
    } catch (_: Exception) {
        MaterialTheme.colorScheme.primary
    }

    val statusColor = when {
        analysis.isShortfall -> SolidExpense
        analysis.isSurplus -> SolidIncome
        else -> SolidPrimary
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.2.dp,
            if (analysis.isShortfall) SolidExpense.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Row: Icon, Account Name, Balance
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onAccountClick() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(accountColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = IconHelper.getIconByName(analysis.account.iconName),
                            contentDescription = null,
                            tint = accountColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = analysis.account.localizedName(languageMode),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${LanguageHelper.getString("current_balance", languageMode)}: ",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Text(
                                text = LanguageHelper.formatCurrency(analysis.currentBalance, languageMode),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Coverage Pill
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, statusColor.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = if (analysis.requiredExpenseAmount > 0) {
                            val pct = (analysis.availableAmount / analysis.requiredExpenseAmount * 100).toInt()
                            if (languageMode == LanguageMode.BANGLA) "${LanguageHelper.toBanglaDigits(pct.toString())}% কভার" else "$pct% Covered"
                        } else {
                            if (languageMode == LanguageMode.BANGLA) "১০০% প্রস্তুত" else "100% Ready"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Funding Progress Bar
            LinearProgressIndicator(
                progress = { (analysis.fundingCoverageRatio / 1.0f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = statusColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Required vs Available Figures
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = LanguageHelper.getString("required_amount", languageMode),
                        fontSize = 10.5.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = LanguageHelper.formatCurrency(analysis.requiredExpenseAmount, languageMode),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        color = SolidExpense
                    )
                }

                Box(
                    modifier = Modifier
                        .height(26.dp)
                        .width(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp)
                ) {
                    Text(
                        text = LanguageHelper.getString("available_amount", languageMode),
                        fontSize = 10.5.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = LanguageHelper.formatCurrency(analysis.availableAmount, languageMode),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Needed Result Banner (Prompt examples format: "Need ৳500 more in bKash", "৳500 surplus in bKash")
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = statusColor.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, statusColor.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when {
                                analysis.isShortfall -> Icons.Default.ErrorOutline
                                analysis.isSurplus -> Icons.Default.Savings
                                else -> Icons.Default.CheckCircle
                            },
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = analysis.getActionMessage(languageMode),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp,
                            color = statusColor,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (analysis.isShortfall) {
                        Button(
                            onClick = onFundAccount,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SolidExpense),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "ফান্ড করুন" else "Fund",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Expand / Collapse Itemized Breakdown Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isExpanded) {
                        LanguageHelper.getString("hide_breakdown", languageMode)
                    } else {
                        LanguageHelper.getString("expand_breakdown", languageMode)
                    },
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    // 1. Assigned Expenses
                    Text(
                        text = LanguageHelper.getString("itemized_expenses", languageMode),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = SolidExpense
                    )

                    if (analysis.itemizedExpenses.isEmpty()) {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "কোনো নির্ধারিত ব্যয় নেই" else "No expenses assigned to this account",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    } else {
                        analysis.itemizedExpenses.forEach { item ->
                            ItemizedRow(item = item, languageMode = languageMode)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // 2. Expected Incomes
                    Text(
                        text = LanguageHelper.getString("itemized_incomes", languageMode),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = SolidIncome
                    )

                    if (analysis.itemizedIncomes.isEmpty()) {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "কোনো প্রত্যাশিত আয় নেই" else "No expected income assigned to this account",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    } else {
                        analysis.itemizedIncomes.forEach { item ->
                            ItemizedRow(item = item, languageMode = languageMode)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Quick Actions Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onAddExpense,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = LanguageHelper.getString("expense", languageMode),
                                fontSize = 11.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(
                            onClick = onAddIncome,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = LanguageHelper.getString("income", languageMode),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemizedRow(item: AccountRequirementItem, languageMode: LanguageMode) {
    val itemColor = try {
        Color(android.graphics.Color.parseColor(item.colorHex))
    } catch (_: Exception) {
        if (item.isExpense) SolidExpense else SolidIncome
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(itemColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = IconHelper.getIconByName(item.iconName),
                    contentDescription = null,
                    tint = itemColor,
                    modifier = Modifier.size(13.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.title,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (item.isRecurring) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "বিল" else "Bill",
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
                if (item.originalBudgetOrExpected > 0 && item.actualSpentOrReceived > 0) {
                    Text(
                        text = "${if (languageMode == LanguageMode.BANGLA) "বাজেট" else "Budget"}: ${LanguageHelper.formatCurrency(item.originalBudgetOrExpected, languageMode)} • ${if (languageMode == LanguageMode.BANGLA) "সম্পন্ন" else "Actual"}: ${LanguageHelper.formatCurrency(item.actualSpentOrReceived, languageMode)}",
                        fontSize = 9.5.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }

        Text(
            text = LanguageHelper.formatCurrency(item.amount, languageMode),
            fontWeight = FontWeight.Bold,
            fontSize = 12.5.sp,
            color = if (item.isExpense) SolidExpense else SolidIncome
        )
    }
}

@Composable
private fun PaymentSourceHelpDialog(
    languageMode: LanguageMode,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Payments,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = LanguageHelper.getString("payment_source_analysis", languageMode),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = if (languageMode == LanguageMode.BANGLA) {
                        "পেমেন্ট সোর্স অ্যানালাইসিসের মূল উদ্দেশ্য হলো প্রতিটি অ্যাকাউন্টে ঠিক কত টাকা মজুদ রাখা প্রয়োজন তা নির্ধারণ করা:\n\n" +
                                "• প্রয়োজনীয় অর্থ (Required): ওই অ্যাকাউন্টে নির্ধারিত খরচ বা বাজেটের মোট পরিমাণ।\n" +
                                "• উপলব্ধ অর্থ (Available): অ্যাকাউন্টের বর্তমান ব্যালেন্স + ওই অ্যাকাউন্টে প্রত্যাশিত মোট আয়।\n" +
                                "• ঘাটতি (Shortfall): অ্যাকাউন্টে আর কত টাকা অতিরিক্ত প্রয়োজন।\n" +
                                "• উদ্বৃত্ত (Surplus): খরচের পর অ্যাকাউন্টে আর কত টাকা বাড়তি থাকবে।\n\n" +
                                "তহবিল স্থানান্তর (Fund Allocation Insight):\n" +
                                "কোনো অ্যাকাউন্টে উদ্বৃত্ত এবং অন্যটিতে ঘাটতি থাকলে, সিস্টেম সরাসরি 'স্থানান্তর' করার স্মার্ট পরামর্শ দেয় যাতে কোনো পেমেন্ট আটকে না যায়।"
                    } else {
                        "The primary purpose of Payment Source Analysis is to determine how much money must be available in each account for this month:\n\n" +
                                "• Required Amount: Total expenses & bills assigned to this account.\n" +
                                "• Available Amount: Current balance + expected income coming into this account.\n" +
                                "• Shortfall: How much more money is needed in this account.\n" +
                                "• Surplus: How much extra money will remain in this account.\n\n" +
                                "Fund Allocation Insight:\n" +
                                "If one account has a surplus and another has a shortage, the system suggests smart transfers (e.g. Move ৳500 from Bank to bKash) with one-click transfer execution."
                    },
                    fontSize = 12.5.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(LanguageHelper.getString("apply", languageMode))
                }
            }
        }
    }
}
