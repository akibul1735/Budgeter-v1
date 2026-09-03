package com.example.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableLongStateOf
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
import com.example.data.model.LanguageMode
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.data.model.TransactionWithDetails
import com.example.data.repository.AccountWithBalance
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidExpenseContainer
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidIncomeContainer
import com.example.ui.theme.SolidPrimary
import com.example.ui.theme.SolidPrimaryContainer
import com.example.ui.theme.SolidTransfer
import com.example.util.DateUtils
import com.example.util.IconHelper
import com.example.util.LanguageHelper
import java.util.Calendar

enum class LedgerDatePreset {
    ALL_TIME,
    TODAY,
    THIS_WEEK,
    THIS_MONTH,
    LAST_MONTH,
    THIS_YEAR,
    CUSTOM
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerScreen(
    transactions: List<TransactionWithDetails>,
    languageMode: LanguageMode,
    accountsWithBalances: List<AccountWithBalance> = emptyList(),
    onAddTransactionClick: () -> Unit,
    onTransactionClick: (Transaction) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTypeFilter by remember { mutableStateOf<TransactionType?>(null) }
    var showSearchField by remember { mutableStateOf(false) }
    var selectedDatePreset by remember { mutableStateOf(LedgerDatePreset.ALL_TIME) }
    var minAmountFilter by remember { mutableDoubleStateOf(0.0) }
    var maxAmountFilter by remember { mutableDoubleStateOf(Double.MAX_VALUE) }
    var customStartDateMs by remember { mutableLongStateOf(0L) }
    var customEndDateMs by remember { mutableLongStateOf(System.currentTimeMillis()) }

    var showFilterDialog by remember { mutableStateOf(false) }
    var showCustomStartPicker by remember { mutableStateOf(false) }
    var showCustomEndPicker by remember { mutableStateOf(false) }

    val accountBalanceMap: Map<Long, Double> = remember(accountsWithBalances) {
        accountsWithBalances.associate { it.account.id to it.currentBalance }
    }

    // Compute Date Bounds
    val (startEpochMs, endEpochMs) = remember(selectedDatePreset, customStartDateMs, customEndDateMs) {
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance()
        when (selectedDatePreset) {
            LedgerDatePreset.ALL_TIME -> Pair(0L, Long.MAX_VALUE)
            LedgerDatePreset.TODAY -> {
                val start = DateUtils.getStartOfDay(now)
                Pair(start, start + 86400000L - 1L)
            }
            LedgerDatePreset.THIS_WEEK -> {
                cal.timeInMillis = now
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                val start = DateUtils.getStartOfDay(cal.timeInMillis)
                Pair(start, now)
            }
            LedgerDatePreset.THIS_MONTH -> {
                cal.timeInMillis = now
                cal.set(Calendar.DAY_OF_MONTH, 1)
                val start = DateUtils.getStartOfDay(cal.timeInMillis)
                Pair(start, now)
            }
            LedgerDatePreset.LAST_MONTH -> {
                cal.timeInMillis = now
                cal.set(Calendar.DAY_OF_MONTH, 1)
                val thisMonthStart = DateUtils.getStartOfDay(cal.timeInMillis)
                cal.add(Calendar.MONTH, -1)
                val lastMonthStart = DateUtils.getStartOfDay(cal.timeInMillis)
                Pair(lastMonthStart, thisMonthStart - 1L)
            }
            LedgerDatePreset.THIS_YEAR -> {
                cal.timeInMillis = now
                cal.set(Calendar.MONTH, Calendar.JANUARY)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                val start = DateUtils.getStartOfDay(cal.timeInMillis)
                Pair(start, now)
            }
            LedgerDatePreset.CUSTOM -> Pair(customStartDateMs, customEndDateMs)
        }
    }

    val filteredTransactions = remember(
        transactions,
        searchQuery,
        selectedTypeFilter,
        startEpochMs,
        endEpochMs,
        minAmountFilter,
        maxAmountFilter
    ) {
        transactions.filter { item ->
            val tx = item.transaction
            val matchesType = selectedTypeFilter == null || tx.type == selectedTypeFilter
            val matchesDate = tx.dateEpochMs in startEpochMs..endEpochMs
            val matchesAmount = tx.amount >= minAmountFilter && tx.amount <= maxAmountFilter
            val matchesSearch = if (searchQuery.isBlank()) true else {
                val query = searchQuery.trim().lowercase()
                tx.note.lowercase().contains(query) ||
                        tx.payeeOrPayer.lowercase().contains(query) ||
                        (item.category?.nameEn?.lowercase()?.contains(query) == true) ||
                        (item.category?.nameBn?.lowercase()?.contains(query) == true) ||
                        (item.subCategory?.nameEn?.lowercase()?.contains(query) == true) ||
                        (item.subCategory?.nameBn?.lowercase()?.contains(query) == true) ||
                        (item.debitAccount?.nameEn?.lowercase()?.contains(query) == true) ||
                        (item.creditAccount?.nameEn?.lowercase()?.contains(query) == true)
            }
            matchesType && matchesDate && matchesAmount && matchesSearch
        }
    }

    // Group transactions by calendar day (descending order)
    val groupedByDay = remember(filteredTransactions) {
        filteredTransactions.groupBy { DateUtils.getStartOfDay(it.transaction.dateEpochMs) }
            .toList()
            .sortedByDescending { it.first }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("ledger_screen"),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Search & Filter Header
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = LanguageHelper.getString("transactions", languageMode),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { showSearchField = !showSearchField },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Search",
                                tint = if (showSearchField || searchQuery.isNotEmpty()) SolidPrimary else MaterialTheme.colorScheme.outline
                            )
                        }
                        IconButton(
                            onClick = { showFilterDialog = true },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.Tune,
                                contentDescription = "Vast Filter",
                                tint = if (selectedDatePreset != LedgerDatePreset.ALL_TIME || minAmountFilter > 0 || maxAmountFilter < Double.MAX_VALUE) SolidPrimary else MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }

                if (showSearchField || searchQuery.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(LanguageHelper.getString("search", languageMode), fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Type Filter Chips (All, Expense, Income, Transfer)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedTypeFilter == null,
                        onClick = { selectedTypeFilter = null },
                        label = { Text(LanguageHelper.getString("all", languageMode), fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        shape = RoundedCornerShape(8.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SolidPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = selectedTypeFilter == TransactionType.EXPENSE,
                        onClick = { selectedTypeFilter = if (selectedTypeFilter == TransactionType.EXPENSE) null else TransactionType.EXPENSE },
                        label = { Text(LanguageHelper.getString("expense", languageMode), fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        shape = RoundedCornerShape(8.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SolidExpense,
                            selectedLabelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = selectedTypeFilter == TransactionType.INCOME,
                        onClick = { selectedTypeFilter = if (selectedTypeFilter == TransactionType.INCOME) null else TransactionType.INCOME },
                        label = { Text(LanguageHelper.getString("income", languageMode), fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        shape = RoundedCornerShape(8.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SolidIncome,
                            selectedLabelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = selectedTypeFilter == TransactionType.TRANSFER,
                        onClick = { selectedTypeFilter = if (selectedTypeFilter == TransactionType.TRANSFER) null else TransactionType.TRANSFER },
                        label = { Text(LanguageHelper.getString("transfer", languageMode), fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        shape = RoundedCornerShape(8.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SolidTransfer,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        if (groupedByDay.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = LanguageHelper.getString("no_transactions", languageMode),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        } else {
            // Render Date Grouped Transactions (like Bluecoins structure)
            groupedByDay.forEach { (dayEpochMs, dayTxList) ->
                // Calculate Net Day Total (Income - Expense, Transfers don't affect net day total)
                val dayIncome = dayTxList.filter { it.transaction.type == TransactionType.INCOME }.sumOf { it.transaction.amount }
                val dayExpense = dayTxList.filter { it.transaction.type == TransactionType.EXPENSE }.sumOf { it.transaction.amount }
                val dayNet = dayIncome - dayExpense

                // Day Header
                item(key = "day_header_$dayEpochMs") {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 2.dp),
                        color = Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = DateUtils.formatDayHeader(dayEpochMs, languageMode),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            val netSign = if (dayNet > 0) "+" else if (dayNet < 0) "-" else ""
                            val netColor = if (dayNet > 0) SolidIncome else if (dayNet < 0) SolidExpense else MaterialTheme.colorScheme.outline
                            Text(
                                text = "$netSign${LanguageHelper.formatCurrency(kotlin.math.abs(dayNet), languageMode)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = netColor
                            )
                        }
                    }
                }

                // Day's Transaction Items Container
                item(key = "day_items_$dayEpochMs") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            dayTxList.forEachIndexed { index, item ->
                                val tx = item.transaction
                                TransactionRowItem(
                                    item = item,
                                    languageMode = languageMode,
                                    accountBalance = when (tx.type) {
                                        TransactionType.EXPENSE -> tx.creditAccountId?.let { accountBalanceMap[it] }
                                        TransactionType.INCOME -> tx.debitAccountId?.let { accountBalanceMap[it] }
                                        TransactionType.TRANSFER -> tx.debitAccountId?.let { accountBalanceMap[it] }
                                    },
                                    onClick = { onTransactionClick(tx) }
                                )

                                if (index < dayTxList.size - 1) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(start = 56.dp, end = 12.dp),
                                        thickness = 0.5.dp,
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Vast Filter Dialog
    if (showFilterDialog) {
        TransactionsFilterDialog(
            currentPreset = selectedDatePreset,
            currentType = selectedTypeFilter,
            currentMinAmount = minAmountFilter,
            currentMaxAmount = if (maxAmountFilter == Double.MAX_VALUE) 0.0 else maxAmountFilter,
            languageMode = languageMode,
            onApply = { newPreset, newType, newMin, newMax ->
                selectedDatePreset = newPreset
                selectedTypeFilter = newType
                minAmountFilter = newMin
                maxAmountFilter = if (newMax > 0) newMax else Double.MAX_VALUE
                showFilterDialog = false
            },
            onSelectCustomDates = {
                showFilterDialog = false
                showCustomStartPicker = true
            },
            onDismiss = { showFilterDialog = false }
        )
    }

    // Custom Date Range Pickers
    if (showCustomStartPicker) {
        val dateState = rememberDatePickerState(initialSelectedDateMillis = customStartDateMs)
        DatePickerDialog(
            onDismissRequest = { showCustomStartPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dateState.selectedDateMillis?.let {
                        customStartDateMs = it
                        showCustomStartPicker = false
                        showCustomEndPicker = true
                    }
                }) {
                    Text("Next: End Date")
                }
            },
            dismissButton = { TextButton(onClick = { showCustomStartPicker = false }) { Text("Cancel") } }
        ) {
            DatePicker(state = dateState, title = { Text("Select Start Date", modifier = Modifier.padding(16.dp)) })
        }
    }

    if (showCustomEndPicker) {
        val dateState = rememberDatePickerState(initialSelectedDateMillis = customEndDateMs)
        DatePickerDialog(
            onDismissRequest = { showCustomEndPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dateState.selectedDateMillis?.let {
                        customEndDateMs = it + (24L * 60L * 60L * 1000L - 1L)
                        selectedDatePreset = LedgerDatePreset.CUSTOM
                        showCustomEndPicker = false
                    }
                }) {
                    Text("Apply Range")
                }
            },
            dismissButton = { TextButton(onClick = { showCustomEndPicker = false }) { Text("Cancel") } }
        ) {
            DatePicker(state = dateState, title = { Text("Select End Date", modifier = Modifier.padding(16.dp)) })
        }
    }
}

@Composable
fun TransactionsFilterDialog(
    currentPreset: LedgerDatePreset,
    currentType: TransactionType?,
    currentMinAmount: Double,
    currentMaxAmount: Double,
    languageMode: LanguageMode,
    onApply: (LedgerDatePreset, TransactionType?, Double, Double) -> Unit,
    onSelectCustomDates: () -> Unit,
    onDismiss: () -> Unit
) {
    var tempPreset by remember { mutableStateOf(currentPreset) }
    var tempType by remember { mutableStateOf(currentType) }
    var tempMinStr by remember { mutableStateOf(if (currentMinAmount > 0) currentMinAmount.toInt().toString() else "") }
    var tempMaxStr by remember { mutableStateOf(if (currentMaxAmount > 0) currentMaxAmount.toInt().toString() else "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (languageMode == LanguageMode.BANGLA) "লেনদেন ফিল্টার ও সাজানো" else "Filter Transactions",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Date Period Presets
                Text("Date Period:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilterChip(
                        selected = tempPreset == LedgerDatePreset.ALL_TIME,
                        onClick = { tempPreset = LedgerDatePreset.ALL_TIME },
                        label = { Text("All Time", fontSize = 10.sp) }
                    )
                    FilterChip(
                        selected = tempPreset == LedgerDatePreset.TODAY,
                        onClick = { tempPreset = LedgerDatePreset.TODAY },
                        label = { Text("Today", fontSize = 10.sp) }
                    )
                    FilterChip(
                        selected = tempPreset == LedgerDatePreset.THIS_WEEK,
                        onClick = { tempPreset = LedgerDatePreset.THIS_WEEK },
                        label = { Text("This Week", fontSize = 10.sp) }
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilterChip(
                        selected = tempPreset == LedgerDatePreset.THIS_MONTH,
                        onClick = { tempPreset = LedgerDatePreset.THIS_MONTH },
                        label = { Text("This Month", fontSize = 10.sp) }
                    )
                    FilterChip(
                        selected = tempPreset == LedgerDatePreset.LAST_MONTH,
                        onClick = { tempPreset = LedgerDatePreset.LAST_MONTH },
                        label = { Text("Last Month", fontSize = 10.sp) }
                    )
                    FilterChip(
                        selected = tempPreset == LedgerDatePreset.CUSTOM,
                        onClick = {
                            tempPreset = LedgerDatePreset.CUSTOM
                            onSelectCustomDates()
                        },
                        label = { Text("Custom 📅", fontSize = 10.sp) }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Transaction Type
                Text("Transaction Type:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = tempType == null,
                        onClick = { tempType = null },
                        label = { Text("All", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = tempType == TransactionType.EXPENSE,
                        onClick = { tempType = TransactionType.EXPENSE },
                        label = { Text("Expense", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = tempType == TransactionType.INCOME,
                        onClick = { tempType = TransactionType.INCOME },
                        label = { Text("Income", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = tempType == TransactionType.TRANSFER,
                        onClick = { tempType = TransactionType.TRANSFER },
                        label = { Text("Transfer", fontSize = 11.sp) }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Amount Range
                Text("Amount Range (৳):", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = tempMinStr,
                        onValueChange = { tempMinStr = it.filter { c -> c.isDigit() } },
                        label = { Text("Min ৳", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = tempMaxStr,
                        onValueChange = { tempMaxStr = it.filter { c -> c.isDigit() } },
                        label = { Text("Max ৳", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val minAmt = tempMinStr.toDoubleOrNull() ?: 0.0
                val maxAmt = tempMaxStr.toDoubleOrNull() ?: 0.0
                onApply(tempPreset, tempType, minAmt, maxAmt)
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

@Composable
private fun TransactionRowItem(
    item: TransactionWithDetails,
    languageMode: LanguageMode,
    accountBalance: Double?,
    onClick: () -> Unit
) {
    val tx = item.transaction

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

    // Determine primary title: If payee/payer is set, use it; otherwise category name
    val primaryTitle = if (tx.payeeOrPayer.isNotBlank()) {
        tx.payeeOrPayer
    } else {
        when (tx.type) {
            TransactionType.EXPENSE -> item.subCategory?.localizedName(languageMode)
                ?: item.category?.localizedName(languageMode)
                ?: LanguageHelper.getString("expense", languageMode)
            TransactionType.INCOME -> item.subCategory?.localizedName(languageMode)
                ?: item.category?.localizedName(languageMode)
                ?: LanguageHelper.getString("income", languageMode)
            TransactionType.TRANSFER -> LanguageHelper.getString("transfer", languageMode)
        }
    }

    // Determine subtitle (Category / Subcategory or Transfer flow)
    val subTitle = when (tx.type) {
        TransactionType.EXPENSE -> {
            val cat = item.category?.localizedName(languageMode) ?: ""
            val sub = item.subCategory?.localizedName(languageMode)
            if (sub != null && sub != primaryTitle) "$cat / $sub" else cat
        }
        TransactionType.INCOME -> {
            val cat = item.category?.localizedName(languageMode) ?: ""
            val sub = item.subCategory?.localizedName(languageMode)
            if (sub != null && sub != primaryTitle) "$cat / $sub" else cat
        }
        TransactionType.TRANSFER -> {
            "(${LanguageHelper.getString("transfer", languageMode)})"
        }
    }

    // Account display
    val accountDisplay = when (tx.type) {
        TransactionType.EXPENSE -> item.creditAccount?.localizedName(languageMode) ?: ""
        TransactionType.INCOME -> item.debitAccount?.localizedName(languageMode) ?: ""
        TransactionType.TRANSFER -> {
            val from = item.creditAccount?.localizedName(languageMode) ?: "Source"
            val to = item.debitAccount?.localizedName(languageMode) ?: "Dest"
            "$from ➔ $to"
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left: Avatar Icon
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
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
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Center: Title, Subtitle, Note
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = primaryTitle,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (subTitle.isNotBlank()) {
                    Text(
                        text = subTitle,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (tx.note.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 1.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Notes,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = tx.note,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.outline,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Right: Amount and Account Info
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

            val accountLine = if (accountBalance != null && tx.type != TransactionType.TRANSFER) {
                "$accountDisplay  ${LanguageHelper.formatCurrency(accountBalance, languageMode)}"
            } else {
                accountDisplay
            }

            if (accountLine.isNotBlank()) {
                Text(
                    text = accountLine,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
