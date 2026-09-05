package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Account
import com.example.data.model.AccountType
import com.example.data.model.LanguageMode
import com.example.data.model.Transaction
import com.example.ui.components.AppTabHeader
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidPrimary
import com.example.util.BalanceSheetAccountRow
import com.example.util.BalanceSheetComparisonData
import com.example.util.BalanceSheetComparisonPreset
import com.example.util.BalanceSheetGroup
import com.example.util.BalanceSheetHelper
import com.example.util.IconHelper
import com.example.util.LanguageHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BalanceSheetScreen(
    accounts: List<Account>,
    transactions: List<Transaction>,
    languageMode: LanguageMode,
    onOpenDrawer: () -> Unit = {},
    onAddAccountClick: () -> Unit,
    onAddSubAccountClick: (Account) -> Unit,
    onEditAccountClick: (Account) -> Unit,
    onAddTransactionClick: () -> Unit,
    onAccountClick: ((Account) -> Unit)? = null
) {
    val context = LocalContext.current

    // Timeline and Comparison state
    var selectedPreset by remember { mutableStateOf(BalanceSheetComparisonPreset.END_OF_LAST_MONTH) }
    var baseDateMs by remember {
        val (base, _) = BalanceSheetHelper.getPresetDateRanges(BalanceSheetComparisonPreset.END_OF_LAST_MONTH)
        mutableStateOf(base)
    }
    var compareDateMs by remember {
        val (_, compare) = BalanceSheetHelper.getPresetDateRanges(BalanceSheetComparisonPreset.END_OF_LAST_MONTH)
        mutableStateOf(compare)
    }

    // Vast Filter State
    var showFilterDialog by remember { mutableStateOf(false) }
    var filterAccountType by remember { mutableStateOf<AccountType?>(null) } // null = All
    var activeOnlyFilter by remember { mutableStateOf(true) }
    var hideZeroBalanceFilter by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var isEditMode by remember { mutableStateOf(false) }

    // Date Pickers for Custom Mode
    var showBaseDatePicker by remember { mutableStateOf(false) }
    var showCompareDatePicker by remember { mutableStateOf(false) }

    // Collapsed/Expanded parent account rows
    val expandedMap = remember { mutableStateMapOf<Long, Boolean>() }

    // Calculate balance sheet data
    val balanceSheetData = remember(
        accounts,
        transactions,
        baseDateMs,
        compareDateMs,
        selectedPreset,
        activeOnlyFilter,
        hideZeroBalanceFilter,
        searchQuery
    ) {
        BalanceSheetHelper.calculateBalanceSheet(
            accounts = accounts,
            transactions = transactions,
            baseDateEpochMs = baseDateMs,
            compareDateEpochMs = compareDateMs,
            preset = selectedPreset,
            activeOnly = activeOnlyFilter,
            hideZeroBalance = hideZeroBalanceFilter,
            searchQuery = searchQuery
        )
    }

    val currency = "BDT"

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 6.dp, bottom = 88.dp)
        ) {
            // Tab Header
            item {
                AppTabHeader(
                    title = LanguageHelper.getString("balance_sheet", languageMode),
                    onOpenDrawer = onOpenDrawer
                )
            }

            // 1. Top Net Worth Card
            item {
                NetWorthSummaryCard(
                    data = balanceSheetData,
                    currency = currency,
                    languageMode = languageMode
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            // 2. Timeline Control & Date Columns Bar
            item {
                TimelineComparisonHeader(
                    preset = selectedPreset,
                    baseDateLabel = balanceSheetData.baseDateLabel,
                    compareDateLabel = balanceSheetData.compareDateLabel,
                    onSelectPreset = { preset ->
                        selectedPreset = preset
                        if (preset != BalanceSheetComparisonPreset.CUSTOM) {
                            val (b, c) = BalanceSheetHelper.getPresetDateRanges(preset)
                            baseDateMs = b
                            compareDateMs = c
                        } else {
                            showBaseDatePicker = true
                        }
                    },
                    onOpenFilter = { showFilterDialog = true },
                    onToggleEditMode = { isEditMode = !isEditMode },
                    isEditMode = isEditMode
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            // 3. ASSETS Section
            if (filterAccountType == null || filterAccountType == AccountType.ASSET) {
                item {
                    SectionHeader(
                        title = if (languageMode == LanguageMode.BANGLA) "সম্পদ (ASSETS)" else "ASSETS",
                        baseAmount = balanceSheetData.totalAssetsBase,
                        currentAmount = balanceSheetData.totalAssetsCurrent,
                        currency = currency,
                        headerColor = MaterialTheme.colorScheme.primary,
                        languageMode = languageMode
                    )
                }

                if (balanceSheetData.assetGroups.isEmpty()) {
                    item {
                        EmptySectionPlaceholder(
                            message = if (languageMode == LanguageMode.BANGLA) "কোন সম্পদ অ্যাকাউন্ট নেই" else "No asset accounts found"
                        )
                    }
                } else {
                    items(balanceSheetData.assetGroups, key = { it.parentAccount.id }) { group ->
                        val isExpanded = expandedMap[group.parentAccount.id] ?: true
                        BalanceSheetGroupItem(
                            group = group,
                            isExpanded = isExpanded,
                            isEditMode = isEditMode,
                            currency = currency,
                            languageMode = languageMode,
                            onToggleExpand = {
                                expandedMap[group.parentAccount.id] = !isExpanded
                            },
                            onAddSubAccount = { onAddSubAccountClick(group.parentAccount) },
                            onEditAccount = { onEditAccountClick(group.parentAccount) },
                            onEditSubAccount = { onEditAccountClick(it) },
                            onAccountClick = onAccountClick
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // 4. LIABILITIES Section
            if (filterAccountType == null || filterAccountType == AccountType.LIABILITY) {
                item {
                    SectionHeader(
                        title = if (languageMode == LanguageMode.BANGLA) "দায় (LIABILITIES)" else "LIABILITIES",
                        baseAmount = balanceSheetData.totalLiabilitiesBase,
                        currentAmount = balanceSheetData.totalLiabilitiesCurrent,
                        currency = currency,
                        headerColor = SolidExpense,
                        languageMode = languageMode
                    )
                }

                if (balanceSheetData.liabilityGroups.isEmpty()) {
                    item {
                        EmptySectionPlaceholder(
                            message = if (languageMode == LanguageMode.BANGLA) "কোন দায় অ্যাকাউন্ট নেই" else "No liability accounts found"
                        )
                    }
                } else {
                    items(balanceSheetData.liabilityGroups, key = { it.parentAccount.id }) { group ->
                        val isExpanded = expandedMap[group.parentAccount.id] ?: true
                        BalanceSheetGroupItem(
                            group = group,
                            isExpanded = isExpanded,
                            isEditMode = isEditMode,
                            currency = currency,
                            languageMode = languageMode,
                            onToggleExpand = {
                                expandedMap[group.parentAccount.id] = !isExpanded
                            },
                            onAddSubAccount = { onAddSubAccountClick(group.parentAccount) },
                            onEditAccount = { onEditAccountClick(group.parentAccount) },
                            onEditSubAccount = { onEditAccountClick(it) },
                            onAccountClick = onAccountClick
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        // Action Buttons: Quick Add Account and Add Transaction
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                onClick = onAddAccountClick,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer,
                shadowElevation = 4.dp,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.AccountBalance,
                        contentDescription = "New Account",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Surface(
                onClick = onAddTransactionClick,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                shadowElevation = 6.dp,
                modifier = Modifier.size(54.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Transaction",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }

    // Vast Filter Dialog
    if (showFilterDialog) {
        BalanceSheetFilterDialog(
            currentPreset = selectedPreset,
            accountType = filterAccountType,
            activeOnly = activeOnlyFilter,
            hideZeroBalance = hideZeroBalanceFilter,
            searchQuery = searchQuery,
            languageMode = languageMode,
            onApply = { newPreset, newType, newActiveOnly, newHideZero, newQuery ->
                selectedPreset = newPreset
                if (newPreset != BalanceSheetComparisonPreset.CUSTOM) {
                    val (b, c) = BalanceSheetHelper.getPresetDateRanges(newPreset)
                    baseDateMs = b
                    compareDateMs = c
                }
                filterAccountType = newType
                activeOnlyFilter = newActiveOnly
                hideZeroBalanceFilter = newHideZero
                searchQuery = newQuery
                showFilterDialog = false
            },
            onSelectCustomDates = {
                showFilterDialog = false
                showBaseDatePicker = true
            },
            onDismiss = { showFilterDialog = false }
        )
    }

    // Custom Date Range Pickers
    if (showBaseDatePicker) {
        val dateState = rememberDatePickerState(initialSelectedDateMillis = baseDateMs)
        DatePickerDialog(
            onDismissRequest = { showBaseDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dateState.selectedDateMillis?.let {
                        baseDateMs = it
                        showBaseDatePicker = false
                        showCompareDatePicker = true
                    }
                }) {
                    Text("Next: Select Second Date")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBaseDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(
                state = dateState,
                title = { Text("Select Base Comparison Date", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold) }
            )
        }
    }

    if (showCompareDatePicker) {
        val dateState = rememberDatePickerState(initialSelectedDateMillis = compareDateMs)
        DatePickerDialog(
            onDismissRequest = { showCompareDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dateState.selectedDateMillis?.let {
                        compareDateMs = it
                        selectedPreset = BalanceSheetComparisonPreset.CUSTOM
                        showCompareDatePicker = false
                    }
                }) {
                    Text("Apply Comparison")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCompareDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(
                state = dateState,
                title = { Text("Select Compare Target Date", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold) }
            )
        }
    }
}

@Composable
private fun NetWorthSummaryCard(
    data: BalanceSheetComparisonData,
    currency: String,
    languageMode: LanguageMode
) {
    val isPositive = data.netWorthDelta >= 0
    val deltaPercent = if (Math.abs(data.netWorthBase) > 0.001) {
        (data.netWorthDelta / Math.abs(data.netWorthBase)) * 100.0
    } else 0.0

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "মোট সম্পদ (নেট ওর্থ)" else "Total Net Worth",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                    Text(
                        text = LanguageHelper.formatCurrency(data.netWorthCurrent, languageMode),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (data.netWorthCurrent >= 0) MaterialTheme.colorScheme.onSurface else SolidExpense
                    )
                }

                // Growth badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isPositive) SolidIncome.copy(alpha = 0.15f) else SolidExpense.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                            contentDescription = null,
                            tint = if (isPositive) SolidIncome else SolidExpense,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        val sign = if (isPositive) "+" else ""
                        Text(
                            text = "$sign${String.format(Locale.US, "%.1f", deltaPercent)}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isPositive) SolidIncome else SolidExpense
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            // Assets and Liabilities row comparison
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Total Assets column
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "মোট সম্পদ" else "Total Assets",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = LanguageHelper.formatCurrency(data.totalAssetsCurrent, languageMode),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Prev: ${LanguageHelper.formatCurrency(data.totalAssetsBase, languageMode)}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                // Total Liabilities column
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "মোট দায়" else "Total Liabilities",
                        fontSize = 11.sp,
                        color = SolidExpense,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = LanguageHelper.formatCurrency(data.totalLiabilitiesCurrent, languageMode),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SolidExpense
                    )
                    Text(
                        text = "Prev: ${LanguageHelper.formatCurrency(data.totalLiabilitiesBase, languageMode)}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
private fun TimelineComparisonHeader(
    preset: BalanceSheetComparisonPreset,
    baseDateLabel: String,
    compareDateLabel: String,
    onSelectPreset: (BalanceSheetComparisonPreset) -> Unit,
    onOpenFilter: () -> Unit,
    onToggleEditMode: () -> Unit,
    isEditMode: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Timeline indicator with chart icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onOpenFilter() }
            ) {
                Icon(
                    imageVector = Icons.Default.Timeline,
                    contentDescription = "Timeline",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Timeline",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Preset capsule button
            Surface(
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.clickable { onOpenFilter() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    val presetName = when (preset) {
                        BalanceSheetComparisonPreset.END_OF_LAST_MONTH -> "End of Last Month"
                        BalanceSheetComparisonPreset.BEGINNING_OF_MONTH -> "Beginning of Month"
                        BalanceSheetComparisonPreset.PREVIOUS_MONTH -> "Previous Month"
                        BalanceSheetComparisonPreset.BEGINNING_OF_YEAR -> "Beginning of Year"
                        BalanceSheetComparisonPreset.LAST_30_DAYS -> "Last 30 Days"
                        BalanceSheetComparisonPreset.CUSTOM -> "Custom Timeframe"
                    }
                    Text(
                        text = presetName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Quick actions
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onOpenFilter, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filter",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = onToggleEditMode, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = if (isEditMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Dual Date Column Sub-Headers
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = baseDateLabel,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                modifier = Modifier.weight(1f)
            )
            Text(
                text = compareDateLabel,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.End,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    }
}

@Composable
private fun SectionHeader(
    title: String,
    baseAmount: Double,
    currentAmount: Double,
    currency: String,
    headerColor: Color,
    languageMode: LanguageMode = LanguageMode.ENGLISH
) {
    val delta = currentAmount - baseAmount
    val isUp = delta > 0.001
    val isDown = delta < -0.001

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = headerColor,
            letterSpacing = 0.5.sp
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = LanguageHelper.formatCurrency(baseAmount, languageMode),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = LanguageHelper.formatCurrency(currentAmount, languageMode),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(2.dp))
                ChangeIndicator(delta = delta)
            }
        }
    }
}

@Composable
private fun BalanceSheetGroupItem(
    group: BalanceSheetGroup,
    isExpanded: Boolean,
    isEditMode: Boolean,
    currency: String,
    languageMode: LanguageMode,
    onToggleExpand: () -> Unit,
    onAddSubAccount: () -> Unit,
    onEditAccount: () -> Unit,
    onEditSubAccount: (Account) -> Unit,
    onAccountClick: ((Account) -> Unit)? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Parent Account Row (e.g., Cash 45% ৳890.00 ৳5,205.00 ▲)
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color.Transparent,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Expand/collapse icon
                IconButton(
                    onClick = onToggleExpand,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(2.dp))

                // Account Name with Group Icon Indicator - Clickable to view transactions
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .clickable {
                            if (isEditMode) onEditAccount() else onAccountClick?.invoke(group.parentAccount)
                        }
                        .padding(vertical = 2.dp, horizontal = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = IconHelper.getIconByName(group.parentAccount.iconName),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) group.parentAccount.nameBn else group.parentAccount.nameEn,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 2,
                        lineHeight = 16.sp,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    // % Share badge
                    if (group.percentageShare > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = "${group.percentageShare.toInt()}%",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Comparative Amounts
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = LanguageHelper.formatCurrency(group.baseBalance, languageMode),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = LanguageHelper.formatCurrency(group.currentBalance, languageMode),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        ChangeIndicator(delta = group.delta)
                    }

                    if (isEditMode) {
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(onClick = onEditAccount, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }

        // Sub Accounts Nested Card
        AnimatedVisibility(
            visible = isExpanded && group.subAccounts.isNotEmpty(),
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, top = 2.dp, bottom = 4.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                    group.subAccounts.forEachIndexed { index, subRow ->
                        if (index > 0) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                        SubAccountRowItem(
                            row = subRow,
                            isEditMode = isEditMode,
                            currency = currency,
                            languageMode = languageMode,
                            onEdit = { onEditSubAccount(subRow.account) },
                            onAccountClick = onAccountClick
                        )
                    }

                    if (isEditMode) {
                        Spacer(modifier = Modifier.height(4.dp))
                        TextButton(
                            onClick = onAddSubAccount,
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "সাব-অ্যাকাউন্ট যোগ" else "Add Sub-Account",
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
private fun SubAccountRowItem(
    row: BalanceSheetAccountRow,
    isEditMode: Boolean,
    currency: String,
    languageMode: LanguageMode,
    onEdit: () -> Unit,
    onAccountClick: ((Account) -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .clickable {
                if (isEditMode) onEdit() else onAccountClick?.invoke(row.account)
            }
            .padding(vertical = 4.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Account Name & % badge
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (languageMode == LanguageMode.BANGLA) row.account.nameBn else row.account.nameEn,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                lineHeight = 15.sp,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )

            if (row.percentageShare > 0) {
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    shape = RoundedCornerShape(3.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = "${row.percentageShare.toInt()}%",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Base & Current Balances
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = LanguageHelper.formatCurrency(row.baseBalance, languageMode),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = LanguageHelper.formatCurrency(row.currentBalance, languageMode),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(2.dp))
                ChangeIndicator(delta = row.delta)
            }

            if (isEditMode) {
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(onClick = onEdit, modifier = Modifier.size(20.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(12.dp))
                }
            }
        }
    }
}

@Composable
private fun ChangeIndicator(delta: Double) {
    when {
        delta > 0.001 -> {
            Text(
                text = "▲",
                fontSize = 9.sp,
                color = SolidIncome,
                fontWeight = FontWeight.Bold
            )
        }
        delta < -0.001 -> {
            Text(
                text = "▼",
                fontSize = 9.sp,
                color = SolidExpense,
                fontWeight = FontWeight.Bold
            )
        }
        else -> {
            Text(
                text = "—",
                fontSize = 9.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun EmptySectionPlaceholder(message: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = message,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.padding(12.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun BalanceSheetFilterDialog(
    currentPreset: BalanceSheetComparisonPreset,
    accountType: AccountType?,
    activeOnly: Boolean,
    hideZeroBalance: Boolean,
    searchQuery: String,
    languageMode: LanguageMode,
    onApply: (BalanceSheetComparisonPreset, AccountType?, Boolean, Boolean, String) -> Unit,
    onSelectCustomDates: () -> Unit,
    onDismiss: () -> Unit
) {
    var tempPreset by remember { mutableStateOf(currentPreset) }
    var tempType by remember { mutableStateOf(accountType) }
    var tempActiveOnly by remember { mutableStateOf(activeOnly) }
    var tempHideZero by remember { mutableStateOf(hideZeroBalance) }
    var tempQuery by remember { mutableStateOf(searchQuery) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (languageMode == LanguageMode.BANGLA) "ব্যালেন্স শিট ফিল্টার" else "Balance Sheet Filters",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Search Input
                OutlinedTextField(
                    value = tempQuery,
                    onValueChange = { tempQuery = it },
                    label = { Text("Search account...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (tempQuery.isNotBlank()) {
                            IconButton(onClick = { tempQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Timeframe Comparison Presets
                Text(
                    text = "Comparison Timeframe:",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = tempPreset == BalanceSheetComparisonPreset.END_OF_LAST_MONTH,
                            onClick = { tempPreset = BalanceSheetComparisonPreset.END_OF_LAST_MONTH },
                            label = { Text("End of Last Month", fontSize = 10.sp) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = tempPreset == BalanceSheetComparisonPreset.BEGINNING_OF_MONTH,
                            onClick = { tempPreset = BalanceSheetComparisonPreset.BEGINNING_OF_MONTH },
                            label = { Text("Start of Month", fontSize = 10.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = tempPreset == BalanceSheetComparisonPreset.PREVIOUS_MONTH,
                            onClick = { tempPreset = BalanceSheetComparisonPreset.PREVIOUS_MONTH },
                            label = { Text("Prev Month", fontSize = 10.sp) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = tempPreset == BalanceSheetComparisonPreset.BEGINNING_OF_YEAR,
                            onClick = { tempPreset = BalanceSheetComparisonPreset.BEGINNING_OF_YEAR },
                            label = { Text("Start of Year", fontSize = 10.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = tempPreset == BalanceSheetComparisonPreset.LAST_30_DAYS,
                            onClick = { tempPreset = BalanceSheetComparisonPreset.LAST_30_DAYS },
                            label = { Text("Last 30 Days", fontSize = 10.sp) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = tempPreset == BalanceSheetComparisonPreset.CUSTOM,
                            onClick = {
                                tempPreset = BalanceSheetComparisonPreset.CUSTOM
                                onSelectCustomDates()
                            },
                            label = { Text("Custom Dates 📅", fontSize = 10.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Account Type Filter
                Text(
                    text = "Account Types:",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = tempType == null,
                        onClick = { tempType = null },
                        label = { Text("All", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = tempType == AccountType.ASSET,
                        onClick = { tempType = AccountType.ASSET },
                        label = { Text("Assets Only", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = tempType == AccountType.LIABILITY,
                        onClick = { tempType = AccountType.LIABILITY },
                        label = { Text("Liabilities Only", fontSize = 11.sp) }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Toggles: Active Only & Hide Zero Balance
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Active Accounts Only", fontSize = 12.sp)
                    Switch(
                        checked = tempActiveOnly,
                        onCheckedChange = { tempActiveOnly = it }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Hide Zero Balance", fontSize = 12.sp)
                    Switch(
                        checked = tempHideZero,
                        onCheckedChange = { tempHideZero = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onApply(tempPreset, tempType, tempActiveOnly, tempHideZero, tempQuery)
            }) {
                Text("Apply Filters")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
