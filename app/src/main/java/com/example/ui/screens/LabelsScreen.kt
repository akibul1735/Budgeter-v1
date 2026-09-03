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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Search
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
import com.example.util.LanguageHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class LabelSortOption {
    SUM_AMOUNT_DESC,
    COUNT_DESC,
    NAME_ASC
}

enum class LabelDateFilterPreset {
    ALL_TIME,
    THIS_MONTH,
    LAST_MONTH,
    THIS_YEAR,
    LAST_30_DAYS,
    CUSTOM
}

data class AggregatedLabel(
    val labelName: String,
    val totalExpense: Double,
    val totalIncome: Double,
    val totalSum: Double,
    val transactionCount: Int,
    val transactions: List<TransactionWithDetails>,
    val percentageShare: Double = 0.0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabelsScreen(
    transactions: List<TransactionWithDetails>,
    languageMode: LanguageMode,
    onTransactionClick: (Transaction) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTypeFilter by remember { mutableStateOf<TransactionType?>(null) }
    var selectedPreset by remember { mutableStateOf(LabelDateFilterPreset.ALL_TIME) }
    var sortOption by remember { mutableStateOf(LabelSortOption.SUM_AMOUNT_DESC) }
    var minAmountFilter by remember { mutableDoubleStateOf(0.0) }
    var customStartDateMs by remember { mutableLongStateOf(0L) }
    var customEndDateMs by remember { mutableLongStateOf(System.currentTimeMillis()) }

    var showFilterDialog by remember { mutableStateOf(false) }
    var selectedDrilldownLabel by remember { mutableStateOf<AggregatedLabel?>(null) }
    var showCustomStartPicker by remember { mutableStateOf(false) }
    var showCustomEndPicker by remember { mutableStateOf(false) }

    // Date Bounds
    val (startEpochMs, endEpochMs) = remember(selectedPreset, customStartDateMs, customEndDateMs) {
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance()
        when (selectedPreset) {
            LabelDateFilterPreset.ALL_TIME -> Pair(0L, Long.MAX_VALUE)
            LabelDateFilterPreset.THIS_MONTH -> {
                cal.timeInMillis = now
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                Pair(cal.timeInMillis, now)
            }
            LabelDateFilterPreset.LAST_MONTH -> {
                cal.timeInMillis = now
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                val thisMonthStart = cal.timeInMillis
                cal.add(Calendar.MONTH, -1)
                Pair(cal.timeInMillis, thisMonthStart - 1L)
            }
            LabelDateFilterPreset.THIS_YEAR -> {
                cal.timeInMillis = now
                cal.set(Calendar.MONTH, Calendar.JANUARY)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                Pair(cal.timeInMillis, now)
            }
            LabelDateFilterPreset.LAST_30_DAYS -> Pair(now - (30L * 24L * 60L * 60L * 1000L), now)
            LabelDateFilterPreset.CUSTOM -> Pair(customStartDateMs, customEndDateMs)
        }
    }

    // Extract tags/labels from transaction notes & referenceNo
    val aggregatedLabels = remember(
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

        // Map: label string -> list of TransactionWithDetails
        val labelMap = mutableMapOf<String, MutableList<TransactionWithDetails>>()

        for (item in filteredTxs) {
            val tx = item.transaction
            val note = tx.note
            val ref = tx.referenceNo

            // Extract hashtag tokens
            val hashtagRegex = Regex("#[\\w\\u0980-\\u09FF]+")
            val foundTags = hashtagRegex.findAll("$note $ref").map { it.value }.toMutableSet()

            // If no hashtag found, and note is short (e.g. single word tag like "Gift", "Bonus", "Electric"), treat note as label
            if (foundTags.isEmpty() && note.isNotBlank()) {
                val cleanNote = note.trim()
                if (cleanNote.length <= 25 && !cleanNote.contains("\n")) {
                    foundTags.add("#$cleanNote")
                } else if (cleanNote.isNotBlank()) {
                    foundTags.add("#General")
                }
            } else if (foundTags.isEmpty()) {
                val catTag = item.category?.nameEn ?: "Untagged"
                foundTags.add("#$catTag")
            }

            for (tag in foundTags) {
                val normalizedTag = if (tag.startsWith("#")) tag else "#$tag"
                labelMap.getOrPut(normalizedTag) { mutableListOf() }.add(item)
            }
        }

        val totalTaggedExpense = filteredTxs.filter { it.transaction.type == TransactionType.EXPENSE }.sumOf { it.transaction.amount }

        val list = labelMap.map { (tagName, txList) ->
            val expenseSum = txList.filter { it.transaction.type == TransactionType.EXPENSE }.sumOf { it.transaction.amount }
            val incomeSum = txList.filter { it.transaction.type == TransactionType.INCOME }.sumOf { it.transaction.amount }
            val totalSum = expenseSum + incomeSum
            val share = if (totalTaggedExpense > 0 && expenseSum > 0) (expenseSum / totalTaggedExpense) * 100.0 else 0.0

            AggregatedLabel(
                labelName = tagName,
                totalExpense = expenseSum,
                totalIncome = incomeSum,
                totalSum = totalSum,
                transactionCount = txList.size,
                transactions = txList.sortedByDescending { it.transaction.dateEpochMs },
                percentageShare = share
            )
        }.filter { label ->
            label.totalSum >= minAmountFilter && (searchQuery.isBlank() || label.labelName.contains(searchQuery, ignoreCase = true))
        }

        when (sortOption) {
            LabelSortOption.SUM_AMOUNT_DESC -> list.sortedByDescending { it.totalSum }
            LabelSortOption.COUNT_DESC -> list.sortedByDescending { it.transactionCount }
            LabelSortOption.NAME_ASC -> list.sortedBy { it.labelName.lowercase() }
        }
    }

    val totalExpenseOverall = remember(aggregatedLabels) { aggregatedLabels.sumOf { it.totalExpense } }
    val totalIncomeOverall = remember(aggregatedLabels) { aggregatedLabels.sumOf { it.totalIncome } }

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
                        placeholder = { Text(if (languageMode == LanguageMode.BANGLA) "লেবেল / ট্যাগ খুঁজুন..." else "Search labels/tags...") },
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
                    LabelSummaryCard(
                        title = if (languageMode == LanguageMode.BANGLA) "সক্রিয় লেবেল" else "Active Labels",
                        value = "${aggregatedLabels.size}",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    LabelSummaryCard(
                        title = if (languageMode == LanguageMode.BANGLA) "ট্যাগকৃত খরচ" else "Tagged Expense",
                        value = "৳ ${LanguageHelper.formatCurrency(totalExpenseOverall, languageMode)}",
                        color = SolidExpense,
                        modifier = Modifier.weight(1.3f)
                    )
                    if (totalIncomeOverall > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        LabelSummaryCard(
                            title = if (languageMode == LanguageMode.BANGLA) "ট্যাগকৃত আয়" else "Tagged Income",
                            value = "৳ ${LanguageHelper.formatCurrency(totalIncomeOverall, languageMode)}",
                            color = SolidIncome,
                            modifier = Modifier.weight(1.3f)
                        )
                    }
                }
            }
        }

        // Labels List
        if (aggregatedLabels.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.LocalOffer,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "কোন লেবেল পাওয়া যায়নি (নোট এ #ট্যাগ ব্যবহার করুন)" else "No labels found (use #tags in transaction notes)",
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
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
                items(aggregatedLabels, key = { it.labelName }) { label ->
                    AggregatedLabelCard(
                        label = label,
                        languageMode = languageMode,
                        onClick = { selectedDrilldownLabel = label }
                    )
                }
            }
        }
    }

    // Drilldown Transactions Dialog
    selectedDrilldownLabel?.let { label ->
        AlertDialog(
            onDismissRequest = { selectedDrilldownLabel = null },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = label.labelName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${label.transactionCount} transactions • Sum: ৳ ${LanguageHelper.formatCurrency(label.totalSum, languageMode)}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { selectedDrilldownLabel = null }) {
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
                    items(label.transactions, key = { it.transaction.id }) { txDetails ->
                        val tx = txDetails.transaction
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    selectedDrilldownLabel = null
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
                                    val desc = if (tx.payeeOrPayer.isNotBlank()) "${tx.payeeOrPayer}: ${tx.note}" else tx.note
                                    if (desc.isNotBlank()) {
                                        Text(
                                            text = desc,
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
                TextButton(onClick = { selectedDrilldownLabel = null }) {
                    Text("Done")
                }
            }
        )
    }

    // Vast Filter Dialog
    if (showFilterDialog) {
        LabelsFilterDialog(
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
                        selectedPreset = LabelDateFilterPreset.CUSTOM
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
private fun LabelSummaryCard(
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
private fun AggregatedLabelCard(
    label: AggregatedLabel,
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
                // Label badge & count
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.padding(end = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Label,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = label.labelName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Text(
                        text = "${label.transactionCount} txs",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Sum Amount
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "৳ ${LanguageHelper.formatCurrency(label.totalSum, languageMode)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (label.totalExpense >= label.totalIncome) SolidExpense else SolidIncome
                    )
                    val avg = label.totalSum / label.transactionCount
                    Text(
                        text = "Avg: ৳ ${LanguageHelper.formatCurrency(avg, languageMode)}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            // Percentage Share Bar
            if (label.percentageShare > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LinearProgressIndicator(
                        progress = { (label.percentageShare / 100.0).toFloat().coerceIn(0f, 1f) },
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${String.format(Locale.US, "%.1f", label.percentageShare)}%",
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
fun LabelsFilterDialog(
    currentPreset: LabelDateFilterPreset,
    currentType: TransactionType?,
    currentSort: LabelSortOption,
    currentMinAmount: Double,
    languageMode: LanguageMode,
    onApply: (LabelDateFilterPreset, TransactionType?, LabelSortOption, Double) -> Unit,
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
                text = if (languageMode == LanguageMode.BANGLA) "লেবেল ফিল্টার ও সাজানো" else "Filter & Sort Labels",
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
                        selected = tempPreset == LabelDateFilterPreset.ALL_TIME,
                        onClick = { tempPreset = LabelDateFilterPreset.ALL_TIME },
                        label = { Text("All Time", fontSize = 10.sp) }
                    )
                    FilterChip(
                        selected = tempPreset == LabelDateFilterPreset.THIS_MONTH,
                        onClick = { tempPreset = LabelDateFilterPreset.THIS_MONTH },
                        label = { Text("This Month", fontSize = 10.sp) }
                    )
                    FilterChip(
                        selected = tempPreset == LabelDateFilterPreset.LAST_MONTH,
                        onClick = { tempPreset = LabelDateFilterPreset.LAST_MONTH },
                        label = { Text("Last Month", fontSize = 10.sp) }
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilterChip(
                        selected = tempPreset == LabelDateFilterPreset.THIS_YEAR,
                        onClick = { tempPreset = LabelDateFilterPreset.THIS_YEAR },
                        label = { Text("This Year", fontSize = 10.sp) }
                    )
                    FilterChip(
                        selected = tempPreset == LabelDateFilterPreset.LAST_30_DAYS,
                        onClick = { tempPreset = LabelDateFilterPreset.LAST_30_DAYS },
                        label = { Text("Last 30 Days", fontSize = 10.sp) }
                    )
                    FilterChip(
                        selected = tempPreset == LabelDateFilterPreset.CUSTOM,
                        onClick = {
                            tempPreset = LabelDateFilterPreset.CUSTOM
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
                        selected = tempSort == LabelSortOption.SUM_AMOUNT_DESC,
                        onClick = { tempSort = LabelSortOption.SUM_AMOUNT_DESC },
                        label = { Text("Highest Sum", fontSize = 10.sp) }
                    )
                    FilterChip(
                        selected = tempSort == LabelSortOption.COUNT_DESC,
                        onClick = { tempSort = LabelSortOption.COUNT_DESC },
                        label = { Text("Most Frequent", fontSize = 10.sp) }
                    )
                    FilterChip(
                        selected = tempSort == LabelSortOption.NAME_ASC,
                        onClick = { tempSort = LabelSortOption.NAME_ASC },
                        label = { Text("Name A-Z", fontSize = 10.sp) }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Minimum Amount Threshold
                OutlinedTextField(
                    value = tempMinStr,
                    onValueChange = { tempMinStr = it.filter { c -> c.isDigit() } },
                    label = { Text("Minimum Tagged Sum (৳)") },
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
