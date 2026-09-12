package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccountType
import com.example.data.model.Category
import com.example.data.model.LanguageMode
import com.example.data.model.TransactionType
import com.example.data.model.TransactionWithDetails
import com.example.data.repository.AccountWithBalance
import com.example.data.repository.FinancialOverview
import com.example.ui.components.AppTabHeader
import com.example.ui.components.BudgetDateRangePreset
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidIncomeContainer
import com.example.ui.theme.SolidPrimary
import com.example.util.DateUtils
import com.example.util.LanguageHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun ReportsScreen(
    overview: FinancialOverview,
    accountsWithBalances: List<AccountWithBalance>,
    transactions: List<TransactionWithDetails> = emptyList(),
    categories: List<Category> = emptyList(),
    languageMode: LanguageMode,
    onOpenDrawer: () -> Unit = {}
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var showTimelineScreen by remember { mutableStateOf(false) }

    // Date range filter state - default to THIS_MONTH
    var selectedPreset by remember { mutableStateOf(BudgetDateRangePreset.THIS_MONTH) }
    var customStartDateMs by remember { mutableStateOf<Long?>(null) }
    var customEndDateMs by remember { mutableStateOf<Long?>(null) }

    // TIMELINE NAVIGATION
    if (showTimelineScreen) {
        val allTx = remember(transactions) { transactions.map { it.transaction } }
        CategoryTimelineScreen(
            categories = categories,
            transactions = allTx,
            languageMode = languageMode,
            onBack = { showTimelineScreen = false }
        )
        return
    }

    // Determine current start and end milliseconds
    val (startDateMs, endDateMs, dateLabel) = remember(selectedPreset, customStartDateMs, customEndDateMs, languageMode) {
        val cal = Calendar.getInstance()
        val now = System.currentTimeMillis()
        val sdf = SimpleDateFormat("MMM yyyy", Locale.getDefault())
        when (selectedPreset) {
            BudgetDateRangePreset.LAST_12_MONTHS -> {
                cal.timeInMillis = now
                val end = cal.timeInMillis
                cal.add(Calendar.MONTH, -12)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                val lbl = if (languageMode == LanguageMode.BANGLA) "বিগত ১২ মাস" else "Last 12 Months"
                Triple(start, end, "$lbl (${sdf.format(Date(start))} – ${sdf.format(Date(end))})")
            }
            BudgetDateRangePreset.THIS_MONTH -> {
                cal.timeInMillis = now
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                val end = cal.timeInMillis
                val lbl = if (languageMode == LanguageMode.BANGLA) "চলতি মাস" else "This Month"
                Triple(start, end, "$lbl (${sdf.format(Date(start))})")
            }
            BudgetDateRangePreset.LAST_MONTH -> {
                cal.timeInMillis = now
                cal.add(Calendar.MONTH, -1)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                val end = cal.timeInMillis
                val lbl = if (languageMode == LanguageMode.BANGLA) "গত মাস" else "Last Month"
                Triple(start, end, "$lbl (${sdf.format(Date(start))})")
            }
            BudgetDateRangePreset.LAST_3_MONTHS -> {
                cal.timeInMillis = now
                val end = cal.timeInMillis
                cal.add(Calendar.MONTH, -3)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                val lbl = if (languageMode == LanguageMode.BANGLA) "বিগত ৩ মাস" else "Last 3 Months"
                Triple(start, end, "$lbl (${sdf.format(Date(start))} – ${sdf.format(Date(end))})")
            }
            BudgetDateRangePreset.LAST_6_MONTHS -> {
                cal.timeInMillis = now
                val end = cal.timeInMillis
                cal.add(Calendar.MONTH, -6)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                val lbl = if (languageMode == LanguageMode.BANGLA) "বিগত ৬ মাস" else "Last 6 Months"
                Triple(start, end, "$lbl (${sdf.format(Date(start))} – ${sdf.format(Date(end))})")
            }
            BudgetDateRangePreset.YEAR_TO_DATE -> {
                cal.timeInMillis = now
                val end = cal.timeInMillis
                cal.set(Calendar.DAY_OF_YEAR, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                val lbl = if (languageMode == LanguageMode.BANGLA) "বছরের শুরু থেকে" else "Year to Date"
                Triple(start, end, "$lbl (${sdf.format(Date(start))} – ${sdf.format(Date(end))})")
            }
            BudgetDateRangePreset.ALL_TIME -> {
                Triple(0L, Long.MAX_VALUE, if (languageMode == LanguageMode.BANGLA) "সর্বকালীন" else "All Time")
            }
            BudgetDateRangePreset.CUSTOM -> {
                val start = customStartDateMs ?: 0L
                val end = customEndDateMs ?: Long.MAX_VALUE
                val shortSdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                val lbl = "${shortSdf.format(Date(start))} – ${shortSdf.format(Date(end))}"
                Triple(start, end, lbl)
            }
            else -> {
                Triple(0L, Long.MAX_VALUE, "All Time")
            }
        }
    }

    // Dynamic metrics based on selected date range
    val periodTransactions = remember(transactions, startDateMs, endDateMs) {
        if (transactions.isEmpty()) emptyList()
        else transactions.filter { it.transaction.dateEpochMs in startDateMs..endDateMs }
    }

    val periodIncome = remember(periodTransactions, overview, transactions.isEmpty()) {
        if (transactions.isEmpty()) overview.monthlyIncome
        else periodTransactions.filter { it.transaction.type == TransactionType.INCOME }.sumOf { it.transaction.amount }
    }

    val periodExpense = remember(periodTransactions, overview, transactions.isEmpty()) {
        if (transactions.isEmpty()) overview.monthlyExpense
        else periodTransactions.filter { it.transaction.type == TransactionType.EXPENSE }.sumOf { it.transaction.amount }
    }

    val periodNetSavings = remember(periodIncome, periodExpense) {
        periodIncome - periodExpense
    }

    val periodDebits = remember(periodTransactions, overview, transactions.isEmpty()) {
        if (transactions.isEmpty()) overview.totalDebits
        else periodIncome
    }

    val periodCredits = remember(periodTransactions, overview, transactions.isEmpty()) {
        if (transactions.isEmpty()) overview.totalCredits
        else periodExpense
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("reports_screen")
    ) {
        AppTabHeader(
            title = LanguageHelper.getString("net_earnings", languageMode),
            showTimelineButton = true,
            onTimelineClick = { showTimelineScreen = true },
            onOpenDrawer = onOpenDrawer
        )

        // Date Range Selector Chips Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                .padding(vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(
                    BudgetDateRangePreset.LAST_12_MONTHS,
                    BudgetDateRangePreset.THIS_MONTH,
                    BudgetDateRangePreset.LAST_MONTH,
                    BudgetDateRangePreset.LAST_3_MONTHS,
                    BudgetDateRangePreset.LAST_6_MONTHS,
                    BudgetDateRangePreset.YEAR_TO_DATE,
                    BudgetDateRangePreset.ALL_TIME,
                    BudgetDateRangePreset.CUSTOM
                ).forEach { preset ->
                    val isSelected = selectedPreset == preset
                    val label = when (preset) {
                        BudgetDateRangePreset.LAST_12_MONTHS -> if (languageMode == LanguageMode.BANGLA) "গত ১২ মাস" else "Last 12 Months"
                        BudgetDateRangePreset.THIS_MONTH -> if (languageMode == LanguageMode.BANGLA) "চলতি মাস" else "This Month"
                        BudgetDateRangePreset.LAST_MONTH -> if (languageMode == LanguageMode.BANGLA) "গত মাস" else "Last Month"
                        BudgetDateRangePreset.LAST_3_MONTHS -> if (languageMode == LanguageMode.BANGLA) "গত ৩ মাস" else "Last 3 Months"
                        BudgetDateRangePreset.LAST_6_MONTHS -> if (languageMode == LanguageMode.BANGLA) "গত ৬ মাস" else "Last 6 Months"
                        BudgetDateRangePreset.YEAR_TO_DATE -> if (languageMode == LanguageMode.BANGLA) "বছরের শুরু থেকে" else "Year to Date"
                        BudgetDateRangePreset.ALL_TIME -> if (languageMode == LanguageMode.BANGLA) "সব সময়" else "All Time"
                        BudgetDateRangePreset.CUSTOM -> if (languageMode == LanguageMode.BANGLA) "নির্দিষ্ট সময়" else "Custom"
                        else -> preset.name
                    }

                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (preset == BudgetDateRangePreset.CUSTOM) {
                                // Launch date picker for custom range
                                val c = Calendar.getInstance()
                                DatePickerDialog(
                                    context,
                                    { _, y1, m1, d1 ->
                                        val calStart = Calendar.getInstance().apply {
                                            set(y1, m1, d1, 0, 0, 0)
                                            set(Calendar.MILLISECOND, 0)
                                        }
                                        DatePickerDialog(
                                            context,
                                            { _, y2, m2, d2 ->
                                                val calEnd = Calendar.getInstance().apply {
                                                    set(y2, m2, d2, 23, 59, 59)
                                                    set(Calendar.MILLISECOND, 999)
                                                }
                                                customStartDateMs = calStart.timeInMillis
                                                customEndDateMs = calEnd.timeInMillis
                                                selectedPreset = BudgetDateRangePreset.CUSTOM
                                            },
                                            c.get(Calendar.YEAR),
                                            c.get(Calendar.MONTH),
                                            c.get(Calendar.DAY_OF_MONTH)
                                        ).show()
                                    },
                                    c.get(Calendar.YEAR),
                                    c.get(Calendar.MONTH),
                                    c.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            } else {
                                selectedPreset = preset
                            }
                        },
                        label = {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SolidPrimary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            // Active Date Range Label Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    modifier = Modifier.size(13.dp),
                    tint = SolidPrimary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = dateLabel,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 6.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
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
                        text = { Text(LanguageHelper.getString("trial_balance", languageMode), fontSize = 11.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text(LanguageHelper.getString("balance_sheet", languageMode), fontSize = 11.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text(LanguageHelper.getString("income_statement", languageMode), fontSize = 11.sp, fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Medium) }
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
                                            text = LanguageHelper.formatCurrency(periodDebits, languageMode),
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
                                            text = LanguageHelper.formatCurrency(periodCredits, languageMode),
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
                                    text = "${LanguageHelper.getString("income_statement", languageMode)} ($dateLabel)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(LanguageHelper.getString("incomes", languageMode), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    Text(LanguageHelper.formatCurrency(periodIncome, languageMode), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SolidIncome)
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(LanguageHelper.getString("expenses", languageMode), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    Text(LanguageHelper.formatCurrency(periodExpense, languageMode), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SolidExpense)
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                HorizontalDivider()
                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(if (languageMode == LanguageMode.BANGLA) "নিট উদ্বৃত্ত / নিট সঞ্চয়" else "Net Earnings / Savings", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = SolidPrimary)
                                    val netColor = if (periodNetSavings >= 0) SolidIncome else SolidExpense
                                    Text(LanguageHelper.formatCurrency(periodNetSavings, languageMode), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = netColor)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
