package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LanguageMode
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.data.model.TransactionWithDetails
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidPrimary
import com.example.util.DateUtils
import com.example.util.LanguageHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class ItemSortOption {
    AMOUNT_DESC,
    COUNT_DESC,
    NAME_ASC
}

enum class ItemDateFilterPreset {
    ALL_TIME,
    THIS_MONTH,
    LAST_MONTH,
    THIS_YEAR,
    LAST_30_DAYS,
    CUSTOM
}

data class AggregatedItem(
    val name: String,
    val totalExpense: Double,
    val totalIncome: Double,
    val transactionCount: Int,
    val latestDateEpochMs: Long,
    val transactions: List<TransactionWithDetails>,
    val percentageShare: Double = 0.0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemsScreen(
    transactions: List<TransactionWithDetails>,
    languageMode: LanguageMode,
    onTransactionClick: (Transaction) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTypeFilter by remember { mutableStateOf<TransactionType?>(null) }
    var selectedPreset by remember { mutableStateOf(ItemDateFilterPreset.ALL_TIME) }
    var sortOption by remember { mutableStateOf(ItemSortOption.AMOUNT_DESC) }
    var minAmountFilter by remember { mutableDoubleStateOf(0.0) }
    var customStartDateMs by remember { mutableLongStateOf(0L) }
    var customEndDateMs by remember { mutableLongStateOf(System.currentTimeMillis()) }

    var showFilterDialog by remember { mutableStateOf(false) }
    var selectedDrilldownItem by remember { mutableStateOf<AggregatedItem?>(null) }
    var showCustomStartPicker by remember { mutableStateOf(false) }
    var showCustomEndPicker by remember { mutableStateOf(false) }

    // Compute Date Bounds
    val (startEpochMs, endEpochMs) = remember(selectedPreset, customStartDateMs, customEndDateMs) {
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance()
        when (selectedPreset) {
            ItemDateFilterPreset.ALL_TIME -> Pair(0L, Long.MAX_VALUE)
            ItemDateFilterPreset.THIS_MONTH -> {
                cal.timeInMillis = now
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                Pair(cal.timeInMillis, now)
            }
            ItemDateFilterPreset.LAST_MONTH -> {
                cal.timeInMillis = now
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                val thisMonthStart = cal.timeInMillis
                cal.add(Calendar.MONTH, -1)
                Pair(cal.timeInMillis, thisMonthStart - 1L)
            }
            ItemDateFilterPreset.THIS_YEAR -> {
                cal.timeInMillis = now
                cal.set(Calendar.MONTH, Calendar.JANUARY)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                Pair(cal.timeInMillis, now)
            }
            ItemDateFilterPreset.LAST_30_DAYS -> Pair(now - (30L * 24L * 60L * 60L * 1000L), now)
            ItemDateFilterPreset.CUSTOM -> Pair(customStartDateMs, customEndDateMs)
        }
    }

    // Filter transactions and group by item/payee
    val aggregatedItems = remember(
        transactions,
        searchQuery,
        selectedTypeFilter,
        startEpochMs,
        endEpochMs,
        sortOption,
        minAmountFilter
    ) {
        val filteredTxs = transactions.filter { item ->
            val tx = item.transaction
            val matchesDate = tx.dateEpochMs in startEpochMs..endEpochMs
            val matchesType = selectedTypeFilter == null || tx.type == selectedTypeFilter
            matchesDate && matchesType
        }

        val grouped = filteredTxs.groupBy { item ->
            val payee = item.transaction.payeeOrPayer.trim()
            if (payee.isNotBlank()) payee else {
                val catName = if (languageMode == LanguageMode.BANGLA) {
                    item.category?.nameBn ?: item.category?.nameEn ?: "অন্যান্য (Other)"
                } else {
                    item.category?.nameEn ?: "Other"
                }
                catName
            }
        }

        val totalAllExpense = filteredTxs.filter { it.transaction.type == TransactionType.EXPENSE }.sumOf { it.transaction.amount }

        val list = grouped.map { (itemName, txList) ->
            val expenseSum = txList.filter { it.transaction.type == TransactionType.EXPENSE }.sumOf { it.transaction.amount }
            val incomeSum = txList.filter { it.transaction.type == TransactionType.INCOME }.sumOf { it.transaction.amount }
            val latestDate = txList.maxOfOrNull { it.transaction.dateEpochMs } ?: 0L
            val share = if (totalAllExpense > 0 && expenseSum > 0) (expenseSum / totalAllExpense) * 100.0 else 0.0

            AggregatedItem(
                name = itemName,
                totalExpense = expenseSum,
                totalIncome = incomeSum,
                transactionCount = txList.size,
                latestDateEpochMs = latestDate,
                transactions = txList.sortedByDescending { it.transaction.dateEpochMs },
                percentageShare = share
            )
        }.filter { item ->
            val totalAmt = item.totalExpense + item.totalIncome
            totalAmt >= minAmountFilter && (searchQuery.isBlank() || item.name.contains(searchQuery, ignoreCase = true))
        }

        when (sortOption) {
            ItemSortOption.AMOUNT_DESC -> list.sortedByDescending { it.totalExpense + it.totalIncome }
            ItemSortOption.COUNT_DESC -> list.sortedByDescending { it.transactionCount }
            ItemSortOption.NAME_ASC -> list.sortedBy { it.name.lowercase() }
        }
    }

    val totalExpenseOverall = remember(aggregatedItems) { aggregatedItems.sumOf { it.totalExpense } }
    val totalIncomeOverall = remember(aggregatedItems) { aggregatedItems.sumOf { it.totalIncome } }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top Search & Vast Filter Action Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text(if (languageMode == LanguageMode.BANGLA) "আইটেম খুঁজুন..." else "Search items...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Vast Filter Button
                    Surface(
                        onClick = { showFilterDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(50.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Filter",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Summary Overview Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ItemSummaryCard(
                        title = if (languageMode == LanguageMode.BANGLA) "মোট আইটেম" else "Total Items",
                        value = "${aggregatedItems.size}",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    ItemSummaryCard(
                        title = if (languageMode == LanguageMode.BANGLA) "মোট খরচ" else "Total Expense",
                        value = "৳ ${LanguageHelper.formatCurrency(totalExpenseOverall, languageMode)}",
                        color = SolidExpense,
                        modifier = Modifier.weight(1.3f)
                    )
                    if (totalIncomeOverall > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        ItemSummaryCard(
                            title = if (languageMode == LanguageMode.BANGLA) "মোট আয়" else "Total Income",
                            value = "৳ ${LanguageHelper.formatCurrency(totalIncomeOverall, languageMode)}",
                            color = SolidIncome,
                            modifier = Modifier.weight(1.3f)
                        )
                    }
                }
            }
        }

        // Items List
        if (aggregatedItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Inventory2,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "কোন আইটেম বা লেনদেন পাওয়া যায়নি" else "No items or transactions match criteria",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(aggregatedItems, key = { it.name }) { item ->
                    AggregatedItemCard(
                        item = item,
                        languageMode = languageMode,
                        onClick = { selectedDrilldownItem = item }
                    )
                }
            }
        }
    }

    // Drilldown Transactions Dialog
    selectedDrilldownItem?.let { item ->
        AlertDialog(
            onDismissRequest = { selectedDrilldownItem = null },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = item.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${item.transactionCount} transactions • Avg: ৳ ${LanguageHelper.formatCurrency((item.totalExpense + item.totalIncome) / item.transactionCount, languageMode)}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { selectedDrilldownItem = null }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                ) {
                    items(item.transactions, key = { it.transaction.id }) { txDetails ->
                        val tx = txDetails.transaction
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    selectedDrilldownItem = null
                                    onTransactionClick(tx)
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()).format(Date(tx.dateEpochMs)),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    if (tx.note.isNotBlank()) {
                                        Text(
                                            text = tx.note,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                val isExp = tx.type == TransactionType.EXPENSE
                                Text(
                                    text = "${if (isExp) "-" else "+"}৳ ${LanguageHelper.formatCurrency(tx.amount, languageMode)}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isExp) SolidExpense else SolidIncome
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedDrilldownItem = null }) {
                    Text("Done")
                }
            }
        )
    }

    // Vast Filter Dialog
    if (showFilterDialog) {
        ItemsFilterDialog(
            currentPreset = selectedPreset,
            currentType = selectedTypeFilter,
            currentSort = sortOption,
            currentMinAmount = minAmountFilter,
            languageMode = languageMode,
            onApply = { newPreset, newType, newSort, newMin ->
                selectedPreset = newPreset
                selectedTypeFilter = newType
                sortOption = newSort
                minAmountFilter = newMin
                showFilterDialog = false
            },
            onSelectCustomRange = {
                showFilterDialog = false
                showCustomStartPicker = true
            },
            onDismiss = { showFilterDialog = false }
        )
    }

    // Custom Date Range Pickers
    if (showCustomStartPicker) {
        val dateState = rememberDatePickerState()
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
        val dateState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showCustomEndPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dateState.selectedDateMillis?.let {
                        customEndDateMs = it + (24L * 60L * 60L * 1000L - 1L)
                        selectedPreset = ItemDateFilterPreset.CUSTOM
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
private fun ItemSummaryCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.1f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AggregatedItemCard(
    item: AggregatedItem,
    languageMode: LanguageMode,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Item Name & Count
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.ShoppingBag,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = item.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${item.transactionCount} ${if (languageMode == LanguageMode.BANGLA) "টি লেনদেন" else "transactions"}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }

                // Total Amount
                Column(horizontalAlignment = Alignment.End) {
                    if (item.totalExpense > 0) {
                        Text(
                            text = "৳ ${LanguageHelper.formatCurrency(item.totalExpense, languageMode)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = SolidExpense
                        )
                    }
                    if (item.totalIncome > 0) {
                        Text(
                            text = "+৳ ${LanguageHelper.formatCurrency(item.totalIncome, languageMode)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = SolidIncome
                        )
                    }
                    val avg = (item.totalExpense + item.totalIncome) / item.transactionCount
                    Text(
                        text = "Avg: ৳ ${LanguageHelper.formatCurrency(avg, languageMode)}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            // Percentage Share Bar if expense exists
            if (item.percentageShare > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LinearProgressIndicator(
                        progress = { (item.percentageShare / 100.0).toFloat().coerceIn(0f, 1f) },
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${String.format(Locale.US, "%.1f", item.percentageShare)}%",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun ItemsFilterDialog(
    currentPreset: ItemDateFilterPreset,
    currentType: TransactionType?,
    currentSort: ItemSortOption,
    currentMinAmount: Double,
    languageMode: LanguageMode,
    onApply: (ItemDateFilterPreset, TransactionType?, ItemSortOption, Double) -> Unit,
    onSelectCustomRange: () -> Unit,
    onDismiss: () -> Unit
) {
    var tempPreset by remember { mutableStateOf(currentPreset) }
    var tempType by remember { mutableStateOf(currentType) }
    var tempSort by remember { mutableStateOf(currentSort) }
    var tempMinStr by remember { mutableStateOf(if (currentMinAmount > 0) currentMinAmount.toInt().toString() else "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (languageMode == LanguageMode.BANGLA) "আইটেম ফিল্টার ও সাজানো" else "Filter & Sort Items",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Timeframe Presets
                Text("Date Period:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilterChip(
                        selected = tempPreset == ItemDateFilterPreset.ALL_TIME,
                        onClick = { tempPreset = ItemDateFilterPreset.ALL_TIME },
                        label = { Text("All Time", fontSize = 10.sp) }
                    )
                    FilterChip(
                        selected = tempPreset == ItemDateFilterPreset.THIS_MONTH,
                        onClick = { tempPreset = ItemDateFilterPreset.THIS_MONTH },
                        label = { Text("This Month", fontSize = 10.sp) }
                    )
                    FilterChip(
                        selected = tempPreset == ItemDateFilterPreset.LAST_MONTH,
                        onClick = { tempPreset = ItemDateFilterPreset.LAST_MONTH },
                        label = { Text("Last Month", fontSize = 10.sp) }
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilterChip(
                        selected = tempPreset == ItemDateFilterPreset.THIS_YEAR,
                        onClick = { tempPreset = ItemDateFilterPreset.THIS_YEAR },
                        label = { Text("This Year", fontSize = 10.sp) }
                    )
                    FilterChip(
                        selected = tempPreset == ItemDateFilterPreset.LAST_30_DAYS,
                        onClick = { tempPreset = ItemDateFilterPreset.LAST_30_DAYS },
                        label = { Text("Last 30 Days", fontSize = 10.sp) }
                    )
                    FilterChip(
                        selected = tempPreset == ItemDateFilterPreset.CUSTOM,
                        onClick = {
                            tempPreset = ItemDateFilterPreset.CUSTOM
                            onSelectCustomRange()
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
                        label = { Text("Expenses Only", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = tempType == TransactionType.INCOME,
                        onClick = { tempType = TransactionType.INCOME },
                        label = { Text("Incomes Only", fontSize = 11.sp) }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Sort Options
                Text("Sort Order:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = tempSort == ItemSortOption.AMOUNT_DESC,
                        onClick = { tempSort = ItemSortOption.AMOUNT_DESC },
                        label = { Text("Highest Amount", fontSize = 10.sp) }
                    )
                    FilterChip(
                        selected = tempSort == ItemSortOption.COUNT_DESC,
                        onClick = { tempSort = ItemSortOption.COUNT_DESC },
                        label = { Text("Most Frequent", fontSize = 10.sp) }
                    )
                    FilterChip(
                        selected = tempSort == ItemSortOption.NAME_ASC,
                        onClick = { tempSort = ItemSortOption.NAME_ASC },
                        label = { Text("Name A-Z", fontSize = 10.sp) }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Minimum Amount Threshold
                OutlinedTextField(
                    value = tempMinStr,
                    onValueChange = { tempMinStr = it.filter { c -> c.isDigit() } },
                    label = { Text("Minimum Amount (৳)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val minAmt = tempMinStr.toDoubleOrNull() ?: 0.0
                onApply(tempPreset, tempType, tempSort, minAmt)
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
