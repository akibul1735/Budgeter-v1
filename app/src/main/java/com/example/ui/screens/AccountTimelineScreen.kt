package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Account
import com.example.data.model.AccountType
import com.example.data.model.LanguageMode
import com.example.data.model.Transaction
import com.example.data.model.TransactionStatus
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.ui.components.ExportMenuButton
import com.example.ui.components.LocalSetTimelineActive
import com.example.util.AccountTimelineHelper
import com.example.util.AccountTimelineInterval
import com.example.util.AccountTimelineData
import com.example.util.LanguageHelper
import com.example.util.PdfPrintHelper
import com.example.util.TabExportHelper

data class AccountTimelineFilterState(
    val interval: AccountTimelineInterval = AccountTimelineInterval.PAST_12_MONTHS,
    val periodsCount: Int = 12,
    val selectedAccountIds: Set<Long> = emptySet(),
    val selectedStatusSet: Set<TransactionStatus> = emptySet(),
    val showHiddenAccounts: Boolean = false,
    val excludeZeroBalances: Boolean = false,
    val displayCurrency: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountTimelineScreen(
    accounts: List<Account>,
    transactions: List<Transaction>,
    languageMode: LanguageMode,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    // Signal container to disable horizontal pager swipe when timeline is active
    val setTimelineActive = LocalSetTimelineActive.current
    DisposableEffect(Unit) {
        setTimelineActive(true)
        onDispose {
            setTimelineActive(false)
        }
    }

    BackHandler(enabled = true) {
        onBack()
    }

    var filterState by remember { mutableStateOf(AccountTimelineFilterState()) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }

    // Expanded states for groups
    val expandedMap = remember { mutableStateMapOf<Long, Boolean>() }

    val timelineData = remember(
        accounts,
        transactions,
        filterState,
        languageMode
    ) {
        AccountTimelineHelper.calculateTimeline(
            accounts = accounts,
            transactions = transactions,
            interval = filterState.interval,
            periodsCount = filterState.periodsCount,
            selectedAccountIds = filterState.selectedAccountIds,
            selectedStatusSet = filterState.selectedStatusSet,
            showHiddenAccounts = filterState.showHiddenAccounts,
            excludeZeroBalances = filterState.excludeZeroBalances,
            languageMode = languageMode
        )
    }

    val horizontalScrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("account_timeline_screen")
    ) {
        // Top App Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("timeline_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "সময়ের সাথে অ্যাকাউন্টের ব্যালেন্স" else "Accounts Balances Over Time",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Action Icons (Help, Filter, Export with all options)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // Help Button (?)
                    IconButton(
                        onClick = { showHelpDialog = true },
                        modifier = Modifier.size(38.dp).testTag("timeline_help_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = "Help",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Filter Button
                    IconButton(
                        onClick = { showFilterDialog = true },
                        modifier = Modifier.size(38.dp).testTag("timeline_filter_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter",
                            tint = if (filterState.selectedAccountIds.isNotEmpty() || filterState.showHiddenAccounts || filterState.excludeZeroBalances)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Export Menu Button (PDF, CSV, HTML, JSON)
                    ExportMenuButton(
                        languageMode = languageMode,
                        onExport = { format ->
                            TabExportHelper.exportTimeline(
                                context = context,
                                format = format,
                                timelineData = timelineData,
                                displayCurrency = filterState.displayCurrency,
                                languageMode = languageMode
                            )
                        }
                    )
                }
            }
        }

        // Timeline Matrix Table with Frozen First Column (Account Names)
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            // 1. Header Row (Dates)
            item {
                TimelineDateHeaderRow(
                    periods = timelineData.periods,
                    scrollState = horizontalScrollState,
                    languageMode = languageMode
                )
            }

            // 2. ASSETS Header Row
            item {
                TimelineSectionHeaderRow(
                    title = if (languageMode == LanguageMode.BANGLA) "Assets (সম্পদ)" else "Assets",
                    totals = timelineData.totalAssetsByPeriod,
                    backgroundColor = Color(0xFFE8F5E9),
                    textColor = Color(0xFF2E7D32),
                    scrollState = horizontalScrollState,
                    displayCurrency = filterState.displayCurrency,
                    languageMode = languageMode
                )
            }

            // Asset Groups & Accounts
            items(timelineData.assetGroups, key = { "asset_${it.parentAccount.id}" }) { group ->
                val isExpanded = expandedMap[group.parentAccount.id] ?: true
                TimelineGroupItem(
                    group = group,
                    isExpanded = isExpanded,
                    scrollState = horizontalScrollState,
                    displayCurrency = filterState.displayCurrency,
                    languageMode = languageMode,
                    onToggleExpand = {
                        expandedMap[group.parentAccount.id] = !isExpanded
                    }
                )
            }

            // 3. LIABILITIES Header Row
            item {
                Spacer(modifier = Modifier.height(8.dp))
                TimelineSectionHeaderRow(
                    title = if (languageMode == LanguageMode.BANGLA) "Liabilities (দায়)" else "Liabilities",
                    totals = timelineData.totalLiabilitiesByPeriod,
                    backgroundColor = Color(0xFFFFEBEE),
                    textColor = Color(0xFFC62828),
                    scrollState = horizontalScrollState,
                    displayCurrency = filterState.displayCurrency,
                    languageMode = languageMode
                )
            }

            // Liability Groups & Accounts
            items(timelineData.liabilityGroups, key = { "liability_${it.parentAccount.id}" }) { group ->
                val isExpanded = expandedMap[group.parentAccount.id] ?: true
                TimelineGroupItem(
                    group = group,
                    isExpanded = isExpanded,
                    scrollState = horizontalScrollState,
                    displayCurrency = filterState.displayCurrency,
                    languageMode = languageMode,
                    onToggleExpand = {
                        expandedMap[group.parentAccount.id] = !isExpanded
                    }
                )
            }

            // 4. NET WORTH Summary Row
            item {
                Spacer(modifier = Modifier.height(12.dp))
                TimelineNetWorthHeaderRow(
                    totals = timelineData.netWorthByPeriod,
                    scrollState = horizontalScrollState,
                    displayCurrency = filterState.displayCurrency,
                    languageMode = languageMode
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Help Dialog
    if (showHelpDialog) {
        TimelineHelpDialog(
            languageMode = languageMode,
            onDismiss = { showHelpDialog = false }
        )
    }

    // Filter Dialog
    if (showFilterDialog) {
        TimelineFilterDialog(
            currentState = filterState,
            accounts = accounts,
            languageMode = languageMode,
            onDismiss = { showFilterDialog = false },
            onApply = { newFilter ->
                filterState = newFilter
                showFilterDialog = false
            }
        )
    }
}

@Composable
private fun TimelineDateHeaderRow(
    periods: List<com.example.util.TimelinePeriod>,
    scrollState: ScrollState,
    languageMode: LanguageMode
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Frozen Left Column: Header
        Box(
            modifier = Modifier
                .width(155.dp)
                .padding(start = 12.dp, end = 6.dp)
        ) {
            Text(
                text = if (languageMode == LanguageMode.BANGLA) "অ্যাকাউন্ট" else "Account",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Frozen Column Divider
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(20.dp)
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
        )

        // Horizontally Scrollable Period Columns
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(scrollState),
            verticalAlignment = Alignment.CenterVertically
        ) {
            periods.forEach { period ->
                Text(
                    text = period.shortLabel,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .width(115.dp)
                        .padding(end = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun TimelineSectionHeaderRow(
    title: String,
    totals: List<Double>,
    backgroundColor: Color,
    textColor: Color,
    scrollState: ScrollState,
    displayCurrency: Boolean,
    languageMode: LanguageMode
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Frozen Left Column: Section Title
        Box(
            modifier = Modifier
                .width(155.dp)
                .padding(start = 12.dp, end = 6.dp)
        ) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Frozen Column Divider
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(20.dp)
                .background(textColor.copy(alpha = 0.25f))
        )

        // Horizontally Scrollable Period Values
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(scrollState),
            verticalAlignment = Alignment.CenterVertically
        ) {
            totals.forEach { amount ->
                Text(
                    text = formatAmount(amount, displayCurrency, languageMode),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .width(115.dp)
                        .padding(end = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun TimelineGroupItem(
    group: com.example.util.TimelineGroup,
    isExpanded: Boolean,
    scrollState: ScrollState,
    displayCurrency: Boolean,
    languageMode: LanguageMode,
    onToggleExpand: () -> Unit
) {
    val groupName = if (languageMode == LanguageMode.BANGLA) group.parentAccount.nameBn else group.parentAccount.nameEn
    val typeColor = if (group.parentAccount.type == AccountType.ASSET) Color(0xFF2E7D32) else Color(0xFFC62828)

    Column(modifier = Modifier.fillMaxWidth()) {
        // Group Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (isExpanded) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                .clickable { onToggleExpand() }
                .padding(vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Frozen Left Column: Group Name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .width(155.dp)
                    .padding(start = 8.dp, end = 4.dp)
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandMore else Icons.Default.ChevronRight,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = typeColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = groupName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = typeColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Frozen Column Divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(20.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
            )

            // Horizontally Scrollable Group Values
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(scrollState),
                verticalAlignment = Alignment.CenterVertically
            ) {
                group.groupBalances.forEach { amount ->
                    Text(
                        text = formatAmount(amount, displayCurrency, languageMode),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = typeColor,
                        textAlign = TextAlign.End,
                        modifier = Modifier
                            .width(115.dp)
                            .padding(end = 12.dp)
                    )
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

        // Sub Accounts
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                group.subAccounts.forEach { sub ->
                    val subName = if (languageMode == LanguageMode.BANGLA) sub.account.nameBn else sub.account.nameEn
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Frozen Left Column: Sub Account Name
                        Box(
                            modifier = Modifier
                                .width(155.dp)
                                .padding(start = 24.dp, end = 6.dp)
                        ) {
                            Text(
                                text = subName,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Frozen Column Divider
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(16.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                        )

                        // Horizontally Scrollable Sub-Account Values
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .horizontalScroll(scrollState),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            sub.balances.forEach { amount ->
                                Text(
                                    text = formatAmount(amount, displayCurrency, languageMode),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier
                                        .width(115.dp)
                                        .padding(end = 12.dp)
                                )
                            }
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                }
            }
        }
    }
}

@Composable
private fun TimelineNetWorthHeaderRow(
    totals: List<Double>,
    scrollState: ScrollState,
    displayCurrency: Boolean,
    languageMode: LanguageMode
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f))
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Frozen Left Column: Net Worth Title
        Box(
            modifier = Modifier
                .width(155.dp)
                .padding(start = 12.dp, end = 6.dp)
        ) {
            Text(
                text = if (languageMode == LanguageMode.BANGLA) "নেট সম্পদ (Net Worth)" else "Net Worth",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Frozen Column Divider
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(20.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
        )

        // Horizontally Scrollable Net Worth Values
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(scrollState),
            verticalAlignment = Alignment.CenterVertically
        ) {
            totals.forEach { amount ->
                Text(
                    text = formatAmount(amount, displayCurrency, languageMode),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (amount >= 0) MaterialTheme.colorScheme.primary else SolidExpense,
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .width(115.dp)
                        .padding(end = 12.dp)
                )
            }
        }
    }
}

private fun formatAmount(amount: Double, displayCurrency: Boolean, languageMode: LanguageMode): String {
    val formatted = LanguageHelper.formatNumber(amount, languageMode)
    return if (displayCurrency) {
        val symbol = "৳ "
        "$symbol$formatted"
    } else {
        formatted
    }
}

@Composable
private fun TimelineHelpDialog(
    languageMode: LanguageMode,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.HelpOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "অ্যাকাউন্ট টাইমলাইন সহায়তা" else "Accounts Timeline Help",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = if (languageMode == LanguageMode.BANGLA)
                        "অ্যাকাউন্ট ব্যালেন্স ওভার টাইম আপনাকে বিগত দিন থেকে বর্তমান এবং ভবিষ্যতের প্রতিটি নির্দিষ্ট সময়কালে সমস্ত ব্যাংক, নগদ, মোবাইল ব্যাংকিং এবং ঋণের ব্যালেন্সের অগ্রগতি দেখতে সাহায্য করে।"
                    else
                        "Accounts Balances Over Time shows the exact snapshot of each account's balance, assets, liabilities, and net worth across periodic timeline columns (Monthly, Quarterly, Yearly).",
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (languageMode == LanguageMode.BANGLA)
                        "• ফিল্টার আইকন: সময়সীমা এবং অ্যাকাউন্ট বাছাই করুন।\n• প্রিন্ট আইকন: সম্পূর্ণ টাইমলাইন রিপোর্ট প্রিন্ট বা PDF আকারে সংরক্ষণ করুন।"
                    else
                        "• Filter Icon: Customize time intervals, periods count, and account visibility.\n• Print/PDF Icon: Generate a high-resolution printable report or PDF document.",
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text(if (languageMode == LanguageMode.BANGLA) "ঠিক আছে" else "Got It")
            }
        }
    )
}

@Composable
private fun TimelineFilterDialog(
    currentState: AccountTimelineFilterState,
    accounts: List<Account>,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onApply: (AccountTimelineFilterState) -> Unit
) {
    var selectedInterval by remember { mutableStateOf(currentState.interval) }
    var periodsCount by remember { mutableStateOf(currentState.periodsCount) }
    var excludeZeroBalances by remember { mutableStateOf(currentState.excludeZeroBalances) }
    var displayCurrency by remember { mutableStateOf(currentState.displayCurrency) }
    var showHiddenAccounts by remember { mutableStateOf(currentState.showHiddenAccounts) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "টাইমলাইন ফিল্টার" else "Timeline Filter",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Interval Selection
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "সময়কাল ইন্টারভাল" else "Time Interval",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    AccountTimelineInterval.values().forEach { interval ->
                        val isSelected = selectedInterval == interval
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedInterval = interval }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                val label = if (languageMode == LanguageMode.BANGLA) interval.titleBn else interval.titleEn
                                Text(
                                    text = label,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider()

                // Toggles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "শূন্য ব্যালেন্স বাদ দিন" else "Exclude Zero Balances",
                        fontSize = 13.sp
                    )
                    Switch(
                        checked = excludeZeroBalances,
                        onCheckedChange = { excludeZeroBalances = it }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "কারেন্সি চিহ্ন প্রদর্শন" else "Display Currency",
                        fontSize = 13.sp
                    )
                    Switch(
                        checked = displayCurrency,
                        onCheckedChange = { displayCurrency = it }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "লুকানো অ্যাকাউন্ট দেখান" else "Show Hidden Accounts",
                        fontSize = 13.sp
                    )
                    Switch(
                        checked = showHiddenAccounts,
                        onCheckedChange = { showHiddenAccounts = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onApply(
                        currentState.copy(
                            interval = selectedInterval,
                            periodsCount = periodsCount,
                            excludeZeroBalances = excludeZeroBalances,
                            displayCurrency = displayCurrency,
                            showHiddenAccounts = showHiddenAccounts
                        )
                    )
                }
            ) {
                Text(if (languageMode == LanguageMode.BANGLA) "প্রয়োগ করুন" else "Apply")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(if (languageMode == LanguageMode.BANGLA) "বাতিল" else "Cancel")
            }
        }
    )
}
