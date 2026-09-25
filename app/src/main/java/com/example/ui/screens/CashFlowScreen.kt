package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Account
import com.example.data.model.Category
import com.example.data.model.LanguageMode
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.data.model.TransactionWithDetails
import com.example.data.repository.AccountWithBalance
import com.example.ui.components.AppTabHeader
import com.example.ui.screens.dashboard.AccountMultiSelectFilterDialog
import com.example.util.CashFlowAccountItem
import com.example.util.CashFlowCategoryItem
import com.example.util.CashFlowDailyPoint
import com.example.util.CashFlowHelper
import com.example.util.CashFlowPeriodBar
import com.example.util.CashFlowPeriodPreset
import com.example.util.CashFlowSummary
import com.example.util.DateUtils
import com.example.util.IconHelper
import androidx.compose.runtime.LaunchedEffect
import com.example.util.LanguageHelper
import com.example.util.TabFilterPreferences
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class CashFlowTabSection(val titleEn: String, val titleBn: String) {
    OVERVIEW("Overview & Charts", "সারসংক্ষেপ ও চার্ট"),
    STATEMENT("Statement", "প্রবাহ বিবরণী"),
    ACCOUNTS("Liquid Accounts", "নগদ ও ব্যাংক হিসাব"),
    TRANSACTIONS("Cash Entries", "নগদ লেনদেন");

    fun getTitle(mode: LanguageMode): String = if (mode == LanguageMode.BANGLA) titleBn else titleEn
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashFlowScreen(
    transactionsWithDetails: List<TransactionWithDetails>,
    allAccounts: List<Account>,
    accountsWithBalances: List<AccountWithBalance>,
    allCategories: List<Category>,
    itemImageCacheMap: Map<String, com.example.data.model.ItemImageCache> = emptyMap(),
    languageMode: LanguageMode,
    onOpenDrawer: () -> Unit,
    onTransactionClick: (Transaction) -> Unit,
    onAddTransactionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val tabFilterPrefs = remember { TabFilterPreferences.getInstance(context) }

    var selectedPreset by remember { mutableStateOf(tabFilterPrefs.cashFlowPreset) }
    var customStartMs by remember { mutableStateOf(0L) }
    var customEndMs by remember { mutableStateOf(0L) }
    var selectedSection by remember { mutableStateOf(tabFilterPrefs.cashFlowSection) }
    var searchQuery by remember { mutableStateOf(tabFilterPrefs.cashFlowSearchQuery) }
    var selectedTxFilter by remember { mutableStateOf<TransactionType?>(null) }
    var selectedAccountIds by remember { mutableStateOf<Set<Long>?>(null) }
    var showAccountFilterDialog by remember { mutableStateOf(false) }

    LaunchedEffect(selectedPreset, selectedSection, searchQuery) {
        tabFilterPrefs.cashFlowPreset = selectedPreset
        tabFilterPrefs.cashFlowSection = selectedSection
        tabFilterPrefs.cashFlowSearchQuery = searchQuery
    }

    // Date range resolution
    val dateRange = remember(selectedPreset, customStartMs, customEndMs) {
        CashFlowHelper.getDateRangeForPreset(selectedPreset, customStartMs, customEndMs)
    }

    // Cash flow calculation
    val summary = remember(transactionsWithDetails, allAccounts, allCategories, dateRange, selectedAccountIds, languageMode) {
        CashFlowHelper.calculateCashFlow(
            allTransactions = transactionsWithDetails,
            allAccounts = allAccounts,
            allCategories = allCategories,
            startMs = dateRange.first,
            endMs = dateRange.second,
            languageMode = languageMode,
            selectedAccountIds = selectedAccountIds
        )
    }

    // Filtered transaction list for Transactions tab
    val filteredTxs = remember(summary.transactions, searchQuery, selectedTxFilter) {
        summary.transactions.filter { tw ->
            val tx = tw.transaction
            val matchesType = selectedTxFilter == null || tx.type == selectedTxFilter
            val matchesSearch = if (searchQuery.isBlank()) true else {
                val q = searchQuery.trim().lowercase()
                tx.note.lowercase().contains(q) ||
                        tx.payeeOrPayer.lowercase().contains(q) ||
                        (tw.category?.nameEn?.lowercase()?.contains(q) == true) ||
                        (tw.category?.nameBn?.lowercase()?.contains(q) == true) ||
                        (tw.debitAccount?.nameEn?.lowercase()?.contains(q) == true) ||
                        (tw.creditAccount?.nameEn?.lowercase()?.contains(q) == true) ||
                        tx.amount.toString().contains(q)
            }
            matchesType && matchesSearch
        }
    }

    val positiveGreen = Color(0xFF2E7D32)
    val negativeRed = Color(0xFFD32F2F)
    val primaryBlue = Color(0xFF1976D2)
    val amberOrange = Color(0xFFF57C00)

    Scaffold(
        topBar = {
            AppTabHeader(
                title = if (languageMode == LanguageMode.BANGLA) "নগদ প্রবাহ" else "Cash Flow",
                tabIcon = Icons.AutoMirrored.Filled.TrendingUp,
                onOpenDrawer = onOpenDrawer,
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                searchPlaceholder = if (languageMode == LanguageMode.BANGLA) "লেনদেন খুঁজুন..." else "Search cash transactions...",
                showSearchButton = true
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTransactionClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier.testTag("cash_flow_fab_add")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction")
            }
        },
        modifier = modifier.fillMaxSize().testTag("cash_flow_screen")
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Period Filter Selector Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(CashFlowPeriodPreset.values()) { preset ->
                        val isSelected = selectedPreset == preset
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier
                                .clickable {
                                    if (preset == CashFlowPeriodPreset.CUSTOM) {
                                        val cal = Calendar.getInstance()
                                        DatePickerDialog(
                                            context,
                                            { _, y, m, d ->
                                                cal.set(y, m, d, 0, 0, 0)
                                                val start = cal.timeInMillis
                                                DatePickerDialog(
                                                    context,
                                                    { _, ey, em, ed ->
                                                        cal.set(ey, em, ed, 23, 59, 59)
                                                        val end = cal.timeInMillis
                                                        customStartMs = start
                                                        customEndMs = end
                                                        selectedPreset = CashFlowPeriodPreset.CUSTOM
                                                    },
                                                    y, m, d
                                                ).show()
                                            },
                                            cal.get(Calendar.YEAR),
                                            cal.get(Calendar.MONTH),
                                            cal.get(Calendar.DAY_OF_MONTH)
                                        ).show()
                                    } else {
                                        selectedPreset = preset
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (preset == CashFlowPeriodPreset.CUSTOM) {
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = preset.getTitle(languageMode),
                                    fontSize = 12.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Account Filter Selection Bar
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (selectedAccountIds != null && selectedAccountIds!!.isNotEmpty())
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    border = BorderStroke(
                        1.dp,
                        if (selectedAccountIds != null && selectedAccountIds!!.isNotEmpty())
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { showAccountFilterDialog = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalance,
                                contentDescription = null,
                                tint = if (selectedAccountIds != null && selectedAccountIds!!.isNotEmpty())
                                    MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Column {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "হিসাব নির্বাচন (Accounts Filter)" else "Cash Flow Accounts",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (selectedAccountIds == null || selectedAccountIds!!.isEmpty()) {
                                        if (languageMode == LanguageMode.BANGLA) "সকল হিসাব অন্তর্ভুক্ত (${LanguageHelper.toBanglaDigits(allAccounts.size.toString())}টি)" else "All Accounts Included (${allAccounts.size})"
                                    } else {
                                        if (languageMode == LanguageMode.BANGLA) "${LanguageHelper.toBanglaDigits(selectedAccountIds!!.size.toString())}টি হিসাব ফিল্টার করা হয়েছে" else "${selectedAccountIds!!.size} Accounts Filtered"
                                    },
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (selectedAccountIds != null && selectedAccountIds!!.isNotEmpty())
                                        MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "পরিবর্তন" else "Change",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // 2. Executive Hero Summary Card (Net Cash Flow + Inflow/Outflow Balance Flow)
            item {
                CashFlowHeroCard(
                    summary = summary,
                    languageMode = languageMode,
                    positiveColor = positiveGreen,
                    negativeColor = negativeRed
                )
            }

            // 3. Runway & Burn Rate KPI Card
            item {
                CashFlowRunwayCard(
                    summary = summary,
                    languageMode = languageMode,
                    accentColor = amberOrange
                )
            }

            // 4. Section Selector Tabs
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        CashFlowTabSection.values().forEach { section ->
                            val isSelected = selectedSection == section
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
                                shadowElevation = if (isSelected) 2.dp else 0.dp,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedSection = section }
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = section.getTitle(languageMode),
                                        fontSize = 11.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Section Specific Content
            when (selectedSection) {
                CashFlowTabSection.OVERVIEW -> {
                    // Periodic Inflow vs Outflow Bar Chart
                    item {
                        CashFlowPeriodicBarChartCard(
                            bars = summary.periodicBars,
                            languageMode = languageMode,
                            inflowColor = positiveGreen,
                            outflowColor = negativeRed
                        )
                    }

                    // Daily Liquidity Trajectory Curve Chart
                    item {
                        CashFlowDailyTrajectoryChartCard(
                            points = summary.dailyPoints,
                            languageMode = languageMode,
                            curveColor = primaryBlue
                        )
                    }

                    // Inflows Category Top List
                    item {
                        CashFlowCategoryListCard(
                            title = if (languageMode == LanguageMode.BANGLA) "নগদ আগমন খাতসমূহ (Inflows)" else "Cash Inflows by Category",
                            items = summary.inflowCategories,
                            totalAmount = summary.totalInflow,
                            languageMode = languageMode,
                            isPositive = true
                        )
                    }

                    // Outflows Category Top List
                    item {
                        CashFlowCategoryListCard(
                            title = if (languageMode == LanguageMode.BANGLA) "নগদ নির্গমন খাতসমূহ (Outflows)" else "Cash Outflows by Category",
                            items = summary.outflowCategories,
                            totalAmount = summary.totalOutflow,
                            languageMode = languageMode,
                            isPositive = false
                        )
                    }
                }

                CashFlowTabSection.STATEMENT -> {
                    item {
                        CashFlowStatementCard(
                            summary = summary,
                            languageMode = languageMode
                        )
                    }
                }

                CashFlowTabSection.ACCOUNTS -> {
                    item {
                        CashFlowAccountsCard(
                            accounts = summary.accountBreakdowns,
                            languageMode = languageMode
                        )
                    }
                }

                CashFlowTabSection.TRANSACTIONS -> {
                    item {
                        // Quick filter chips for transactions
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            FilterChip(
                                label = if (languageMode == LanguageMode.BANGLA) "সকল (${summary.transactions.size})" else "All (${summary.transactions.size})",
                                isSelected = selectedTxFilter == null,
                                onClick = { selectedTxFilter = null }
                            )
                            FilterChip(
                                label = if (languageMode == LanguageMode.BANGLA) "আয় / আগমন" else "Incomes",
                                isSelected = selectedTxFilter == TransactionType.INCOME,
                                onClick = { selectedTxFilter = TransactionType.INCOME }
                            )
                            FilterChip(
                                label = if (languageMode == LanguageMode.BANGLA) "ব্যয় / নির্গমন" else "Expenses",
                                isSelected = selectedTxFilter == TransactionType.EXPENSE,
                                onClick = { selectedTxFilter = TransactionType.EXPENSE }
                            )
                            FilterChip(
                                label = if (languageMode == LanguageMode.BANGLA) "স্থানান্তর / ঋণ" else "Transfers",
                                isSelected = selectedTxFilter == TransactionType.TRANSFER,
                                onClick = { selectedTxFilter = TransactionType.TRANSFER }
                            )
                        }
                    }

                    if (filteredTxs.isEmpty()) {
                        item {
                            EmptyCashFlowState(
                                message = if (languageMode == LanguageMode.BANGLA) "নির্বাচিত সময়সীমার মধ্যে কোনো নগদ লেনদেন মেলেনি" else "No cash transactions found for the selected criteria."
                            )
                        }
                    } else {
                        items(filteredTxs, key = { it.transaction.id }) { tw ->
                            CashFlowTransactionItem(
                                txWithDetails = tw,
                                itemImageCacheMap = itemImageCacheMap,
                                languageMode = languageMode,
                                onClick = { onTransactionClick(tw.transaction) }
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }

        if (showAccountFilterDialog) {
            AccountMultiSelectFilterDialog(
                allAccounts = allAccounts,
                selectedAccountIds = selectedAccountIds,
                languageMode = languageMode,
                onDismiss = { showAccountFilterDialog = false },
                onApply = { newSelected ->
                    selectedAccountIds = newSelected
                    showAccountFilterDialog = false
                }
            )
        }
    }
}

@Composable
fun FilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        ),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            fontSize = 11.5.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun CashFlowHeroCard(
    summary: CashFlowSummary,
    languageMode: LanguageMode,
    positiveColor: Color,
    negativeColor: Color
) {
    val isPositive = summary.netCashFlow >= 0
    val netColor = if (isPositive) positiveColor else negativeColor

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("cash_flow_hero_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "নিট নগদ প্রবাহ (Net Cash Flow)" else "Net Cash Flow",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = (if (isPositive) "+" else "") + LanguageHelper.formatCurrency(summary.netCashFlow, languageMode),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = netColor
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = netColor.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, netColor.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isPositive) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                            contentDescription = null,
                            tint = netColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (isPositive) {
                                if (languageMode == LanguageMode.BANGLA) "উদ্বৃত্ত (Surplus)" else "Positive Flow"
                            } else {
                                if (languageMode == LanguageMode.BANGLA) "ঘাটতি (Deficit)" else "Negative Flow"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = netColor
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            // 4-Step Cash Flow Sequence
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Opening
                CashFlowFlowStep(
                    title = if (languageMode == LanguageMode.BANGLA) "প্রারম্ভিক জের" else "Opening",
                    amount = summary.openingBalance,
                    languageMode = languageMode,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Inflow
                CashFlowFlowStep(
                    title = if (languageMode == LanguageMode.BANGLA) "নগদ আগমন (+)" else "Cash In (+)",
                    amount = summary.totalInflow,
                    languageMode = languageMode,
                    tint = positiveColor
                )

                // Outflow
                CashFlowFlowStep(
                    title = if (languageMode == LanguageMode.BANGLA) "নগদ নির্গমন (−)" else "Cash Out (−)",
                    amount = summary.totalOutflow,
                    languageMode = languageMode,
                    tint = negativeColor
                )

                // Closing
                CashFlowFlowStep(
                    title = if (languageMode == LanguageMode.BANGLA) "সমাপনী জের" else "Closing",
                    amount = summary.closingBalance,
                    languageMode = languageMode,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun CashFlowFlowStep(
    title: String,
    amount: Double,
    languageMode: LanguageMode,
    tint: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = title,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = LanguageHelper.formatCurrency(amount, languageMode),
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Bold,
            color = tint,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun CashFlowRunwayCard(
    summary: CashFlowSummary,
    languageMode: LanguageMode,
    accentColor: Color
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Daily Burn Rate
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "দৈনিক গড় ব্যয় হার" else "Daily Burn Rate",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = LanguageHelper.formatCurrency(summary.dailyAverageBurnRate, languageMode) + (if (languageMode == LanguageMode.BANGLA) "/দিন" else "/day"),
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Runway
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "নগদ রানওয়ে" else "Cash Runway",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val runwayStr = if (summary.runwayDays >= 999) {
                    if (languageMode == LanguageMode.BANGLA) "অফুরন্ত (>৩ বছর)" else "Infinite (>3 yrs)"
                } else if (summary.runwayDays >= 60) {
                    val months = String.format(Locale.US, "%.1f", summary.runwayMonths)
                    if (languageMode == LanguageMode.BANGLA) "${LanguageHelper.toBanglaDigits(months)} মাস (${LanguageHelper.toBanglaDigits(summary.runwayDays.toString())} দিন)" else "$months mos (${summary.runwayDays} days)"
                } else {
                    if (languageMode == LanguageMode.BANGLA) "${LanguageHelper.toBanglaDigits(summary.runwayDays.toString())} দিন" else "${summary.runwayDays} days"
                }

                Text(
                    text = runwayStr,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (summary.runwayDays < 30) Color(0xFFD32F2F) else Color(0xFF2E7D32)
                )
            }
        }
    }
}

@Composable
fun CashFlowPeriodicBarChartCard(
    bars: List<CashFlowPeriodBar>,
    languageMode: LanguageMode,
    inflowColor: Color,
    outflowColor: Color
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "পর্যায়ক্রমিক নগদ আগমন ও নির্গমন" else "Periodic Inflow vs Outflow",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Legend
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(inflowColor))
                        Text(if (languageMode == LanguageMode.BANGLA) "আগমন" else "In", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(outflowColor))
                        Text(if (languageMode == LanguageMode.BANGLA) "নির্গমন" else "Out", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            if (bars.isEmpty() || bars.all { it.inflow == 0.0 && it.outflow == 0.0 }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "চার্ট প্রদর্শনের জন্য পর্যাপ্ত তথ্য নেই" else "No activity to plot for this period",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                val maxVal = remember(bars) {
                    val m = bars.maxOfOrNull { Math.max(it.inflow, it.outflow) } ?: 100.0
                    if (m <= 0) 100.0 else m
                }

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    val barWidth = size.width / (bars.size * 2.5f)
                    val groupSpacing = size.width / bars.size
                    val chartHeight = size.height - 24.dp.toPx()

                    // Horizontal baseline
                    drawLine(
                        color = Color.LightGray.copy(alpha = 0.5f),
                        start = Offset(0f, chartHeight),
                        end = Offset(size.width, chartHeight),
                        strokeWidth = 1.dp.toPx()
                    )

                    bars.forEachIndexed { idx, bar ->
                        val groupCenter = idx * groupSpacing + groupSpacing / 2f

                        // Inflow bar
                        val inH = ((bar.inflow / maxVal) * chartHeight).toFloat().coerceIn(0f, chartHeight)
                        drawRoundRect(
                            color = inflowColor,
                            topLeft = Offset(groupCenter - barWidth - 2.dp.toPx(), chartHeight - inH),
                            size = Size(barWidth, inH),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )

                        // Outflow bar
                        val outH = ((bar.outflow / maxVal) * chartHeight).toFloat().coerceIn(0f, chartHeight)
                        drawRoundRect(
                            color = outflowColor,
                            topLeft = Offset(groupCenter + 2.dp.toPx(), chartHeight - outH),
                            size = Size(barWidth, outH),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )
                    }
                }

                // Bar Labels
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    bars.forEach { bar ->
                        Text(
                            text = bar.label,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CashFlowDailyTrajectoryChartCard(
    points: List<CashFlowDailyPoint>,
    languageMode: LanguageMode,
    curveColor: Color
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "দৈনিক নগদ উদ্বৃত্ত গতিপথ" else "Daily Liquidity Trajectory",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (points.isNotEmpty()) {
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "সর্বশেষ: " + LanguageHelper.formatCurrency(points.last().endOfDayBalance, languageMode) else "Latest: " + LanguageHelper.formatCurrency(points.last().endOfDayBalance, languageMode),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = curveColor
                    )
                }
            }

            if (points.size < 2) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "গতিপথ চার্টের জন্য অন্তত ২ দিনের তথ্য প্রয়োজন" else "At least 2 days needed for trajectory curve",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                val minBal = remember(points) { points.minOf { it.endOfDayBalance } }
                val maxBal = remember(points) { points.maxOf { it.endOfDayBalance } }
                val range = if (maxBal == minBal) 1.0 else maxBal - minBal

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .padding(horizontal = 4.dp, vertical = 4.dp)
                ) {
                    val w = size.width
                    val h = size.height - 12.dp.toPx()
                    val stepX = w / (points.size - 1)

                    val path = Path()
                    val fillPath = Path()

                    points.forEachIndexed { i, pt ->
                        val normY = (1f - ((pt.endOfDayBalance - minBal) / range).toFloat()).coerceIn(0f, 1f)
                        val x = i * stepX
                        val y = normY * h

                        if (i == 0) {
                            path.moveTo(x, y)
                            fillPath.moveTo(x, h)
                            fillPath.lineTo(x, y)
                        } else {
                            val prevX = (i - 1) * stepX
                            val prevNormY = (1f - ((points[i - 1].endOfDayBalance - minBal) / range).toFloat()).coerceIn(0f, 1f)
                            val prevY = prevNormY * h
                            val cx = (prevX + x) / 2f
                            path.cubicTo(cx, prevY, cx, y, x, y)
                            fillPath.cubicTo(cx, prevY, cx, y, x, y)
                        }

                        if (i == points.size - 1) {
                            fillPath.lineTo(x, h)
                            fillPath.close()
                        }
                    }

                    // Draw gradient fill
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(curveColor.copy(alpha = 0.25f), curveColor.copy(alpha = 0.02f)),
                            startY = 0f,
                            endY = h
                        )
                    )

                    // Draw curve line
                    drawPath(
                        path = path,
                        color = curveColor,
                        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Highlight last point
                    val lastX = (points.size - 1) * stepX
                    val lastNormY = (1f - ((points.last().endOfDayBalance - minBal) / range).toFloat()).coerceIn(0f, 1f)
                    val lastY = lastNormY * h
                    drawCircle(color = curveColor, radius = 4.dp.toPx(), center = Offset(lastX, lastY))
                    drawCircle(color = Color.White, radius = 2.dp.toPx(), center = Offset(lastX, lastY))
                }

                // Min and Max Labels
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "নূন্যতম: " + LanguageHelper.formatCurrency(minBal, languageMode) else "Min: " + LanguageHelper.formatCurrency(minBal, languageMode),
                        fontSize = 10.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "সর্বোচ্চ: " + LanguageHelper.formatCurrency(maxBal, languageMode) else "Max: " + LanguageHelper.formatCurrency(maxBal, languageMode),
                        fontSize = 10.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun CashFlowCategoryListCard(
    title: String,
    items: List<CashFlowCategoryItem>,
    totalAmount: Double,
    languageMode: LanguageMode,
    isPositive: Boolean
) {
    val defaultFallbackColor = if (isPositive) Color(0xFF4CAF50) else Color(0xFFF44336)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = LanguageHelper.formatCurrency(totalAmount, languageMode),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isPositive) Color(0xFF2E7D32) else Color(0xFFD32F2F)
                )
            }

            if (items.isEmpty()) {
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "কোনো তথ্য পাওয়া যায়নি" else "No category data for this period",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            } else {
                items.take(6).forEach { catItem ->
                    val catColor = try {
                        Color(android.graphics.Color.parseColor(catItem.colorHex))
                    } catch (_: Exception) {
                        defaultFallbackColor
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(catColor.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = IconHelper.getIconByName(catItem.iconName),
                                        contentDescription = null,
                                        tint = catColor,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                                Text(
                                    text = catItem.localizedName(languageMode),
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = (if (isPositive) "+" else "−") + LanguageHelper.formatCurrency(catItem.totalAmount, languageMode),
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPositive) Color(0xFF2E7D32) else Color(0xFFD32F2F)
                                )
                                Text(
                                    text = String.format(Locale.US, "%.1f%%", catItem.percentage) + " (${catItem.transactionCount})",
                                    fontSize = 10.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Progress bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth((catItem.percentage / 100.0).toFloat().coerceIn(0f, 1f))
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(catColor)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CashFlowStatementCard(
    summary: CashFlowSummary,
    languageMode: LanguageMode
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = if (languageMode == LanguageMode.BANGLA) "নগদ প্রবাহ বিবরণী (Cash Flow Statement)" else "Cash Flow Statement",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // 1. Operating Activities Section
            StatementSectionHeader(
                title = if (languageMode == LanguageMode.BANGLA) "১. পরিচালন কার্যক্রম (Operating Activities)" else "1. Cash from Operating Activities"
            )
            StatementRow(
                label = if (languageMode == LanguageMode.BANGLA) "নগদ আয় ও প্রাপ্তি" else "Operating Inflows (Incomes)",
                amount = summary.operatingInflow,
                isPositive = true,
                languageMode = languageMode
            )
            StatementRow(
                label = if (languageMode == LanguageMode.BANGLA) "দৈনন্দিন খরচ ও ব্যয়" else "Operating Outflows (Expenses)",
                amount = summary.operatingOutflow,
                isPositive = false,
                languageMode = languageMode
            )
            StatementSubtotalRow(
                label = if (languageMode == LanguageMode.BANGLA) "পরিচালন হতে নিট নগদ" else "Net Cash from Operating Activities",
                amount = summary.netOperatingFlow,
                languageMode = languageMode
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            // 2. Financing & Resting Money Section
            StatementSectionHeader(
                title = if (languageMode == LanguageMode.BANGLA) "২. অর্থায়ন ও ঋণ কার্যক্রম (Financing & RM Activities)" else "2. Cash from Financing & Debt (RM) Activities"
            )
            StatementRow(
                label = if (languageMode == LanguageMode.BANGLA) "ঋণ গ্রহণ / আরএম আগমন (+)" else "Debt Receipts / RM Inflows (+)",
                amount = summary.financingInflow,
                isPositive = true,
                languageMode = languageMode
            )
            StatementRow(
                label = if (languageMode == LanguageMode.BANGLA) "ঋণ পরিশোধ / ঋণ প্রদান (−)" else "Debt Repayments / RM Loans Given (−)",
                amount = summary.financingOutflow,
                isPositive = false,
                languageMode = languageMode
            )
            StatementSubtotalRow(
                label = if (languageMode == LanguageMode.BANGLA) "অর্থায়ন হতে নিট নগদ" else "Net Cash from Financing Activities",
                amount = summary.netFinancingFlow,
                languageMode = languageMode
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            // 3. Net Total & Reconciliation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "সর্বমোট নিট নগদ বৃদ্ধি / হ্রাস" else "Net Increase / (Decrease) in Cash",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = (if (summary.netCashFlow >= 0) "+" else "") + LanguageHelper.formatCurrency(summary.netCashFlow, languageMode),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (summary.netCashFlow >= 0) Color(0xFF2E7D32) else Color(0xFFD32F2F)
                )
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "শুরুর ব্যালেন্স" else "Opening Balance",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = LanguageHelper.formatCurrency(summary.openingBalance, languageMode),
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "সমাপনী ব্যালেন্স" else "Closing Balance",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = LanguageHelper.formatCurrency(summary.closingBalance, languageMode),
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatementSectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
fun StatementRow(
    label: String,
    amount: Double,
    isPositive: Boolean,
    languageMode: LanguageMode
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = (if (isPositive) "+" else "−") + LanguageHelper.formatCurrency(amount, languageMode),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = if (isPositive) Color(0xFF2E7D32) else Color(0xFFD32F2F)
        )
    }
}

@Composable
fun StatementSubtotalRow(
    label: String,
    amount: Double,
    languageMode: LanguageMode
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, top = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = (if (amount >= 0) "+" else "") + LanguageHelper.formatCurrency(amount, languageMode),
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Bold,
            color = if (amount >= 0) Color(0xFF2E7D32) else Color(0xFFD32F2F)
        )
    }
}

@Composable
fun CashFlowAccountsCard(
    accounts: List<CashFlowAccountItem>,
    languageMode: LanguageMode
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = if (languageMode == LanguageMode.BANGLA) "নগদ ও ব্যাংক হিসাবের পরিবর্তন" else "Liquid Account Balances & Changes",
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (accounts.isEmpty()) {
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "কোনো নগদ বা ব্যাংক হিসাব পাওয়া যায়নি" else "No asset accounts available",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                accounts.forEach { item ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalanceWallet,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = item.account.localizedName(languageMode),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Text(
                                    text = (if (item.netChange >= 0) "+" else "") + LanguageHelper.formatCurrency(item.netChange, languageMode),
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (item.netChange >= 0) Color(0xFF2E7D32) else Color(0xFFD32F2F)
                                )
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                AccountFlowMiniStat(
                                    label = if (languageMode == LanguageMode.BANGLA) "প্রারম্ভিক" else "Opening",
                                    amount = item.openingBalance,
                                    languageMode = languageMode
                                )
                                AccountFlowMiniStat(
                                    label = if (languageMode == LanguageMode.BANGLA) "আগমন (+)" else "In (+)",
                                    amount = item.totalInflow,
                                    languageMode = languageMode,
                                    color = Color(0xFF2E7D32)
                                )
                                AccountFlowMiniStat(
                                    label = if (languageMode == LanguageMode.BANGLA) "নির্গমন (−)" else "Out (−)",
                                    amount = item.totalOutflow,
                                    languageMode = languageMode,
                                    color = Color(0xFFD32F2F)
                                )
                                AccountFlowMiniStat(
                                    label = if (languageMode == LanguageMode.BANGLA) "সমাপনী" else "Closing",
                                    amount = item.closingBalance,
                                    languageMode = languageMode,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AccountFlowMiniStat(
    label: String,
    amount: Double,
    languageMode: LanguageMode,
    color: Color = Color.Unspecified
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 9.5.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = LanguageHelper.formatCurrency(amount, languageMode),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (color != Color.Unspecified) color else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun CashFlowTransactionItem(
    txWithDetails: TransactionWithDetails,
    itemImageCacheMap: Map<String, com.example.data.model.ItemImageCache> = emptyMap(),
    languageMode: LanguageMode,
    onClick: () -> Unit
) {
    val tx = txWithDetails.transaction
    val cat = txWithDetails.category
    val subCat = txWithDetails.subCategory

    val isPositive = when (tx.type) {
        TransactionType.INCOME -> tx.amount >= 0
        TransactionType.EXPENSE -> tx.amount < 0
        TransactionType.TRANSFER -> tx.debitAccountId != null
    }

    val displayAmount = Math.abs(tx.amount)
    val amountColor = if (isPositive) Color(0xFF2E7D32) else Color(0xFFD32F2F)
    val prefix = if (isPositive) "+" else "−"

    val sdf = remember { SimpleDateFormat("dd MMM, yyyy", Locale.US) }
    val rawDate = sdf.format(Date(tx.dateEpochMs))
    val dateText = if (languageMode == LanguageMode.BANGLA) LanguageHelper.toBanglaDigits(rawDate) else rawDate

    val catColor = try {
        Color(android.graphics.Color.parseColor(cat?.colorHex ?: "#1976D2"))
    } catch (_: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("cash_flow_tx_${tx.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                val iconName = com.example.util.ItemCacheHelper.resolveTransactionIconName(txWithDetails, itemImageCacheMap)
                val isImage = IconHelper.isDrawableIcon(iconName) || IconHelper.isCustomIcon(iconName)
                val dynamicBg = if (isImage) Color.Transparent else catColor.copy(alpha = 0.15f)

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(dynamicBg),
                    contentAlignment = Alignment.Center
                ) {
                    IconHelper.AppIcon(
                        iconName = iconName,
                        contentDescription = cat?.localizedName(languageMode),
                        tint = if (isImage) Color.Unspecified else catColor,
                        modifier = Modifier.size(if (isImage) 32.dp else 17.dp)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    val primaryTitle = tx.payeeOrPayer.ifBlank {
                        cat?.localizedName(languageMode) ?: (if (languageMode == LanguageMode.BANGLA) "অনির্ধারিত" else "Uncategorized")
                    }
                    Text(
                        text = primaryTitle,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = dateText,
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        val accName = txWithDetails.debitAccount?.localizedName(languageMode)
                            ?: txWithDetails.creditAccount?.localizedName(languageMode)
                        if (accName != null) {
                            Text(
                                text = "• $accName",
                                fontSize = 10.5.sp,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    if (tx.note.isNotBlank() || tx.payeeOrPayer.isNotBlank()) {
                        val subText = if (tx.payeeOrPayer.isNotBlank() && tx.note.isNotBlank()) {
                            "${tx.payeeOrPayer}: ${tx.note}"
                        } else {
                            tx.payeeOrPayer.ifBlank { tx.note }
                        }
                        Text(
                            text = subText,
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = prefix + LanguageHelper.formatCurrency(displayAmount, languageMode),
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = amountColor
                )
            }
        }
    }
}

@Composable
fun EmptyCashFlowState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SwapHoriz,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(44.dp)
            )
            Text(
                text = message,
                fontSize = 12.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
