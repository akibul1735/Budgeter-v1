package com.example.ui.screens

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LanguageMode
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.data.model.TransactionWithDetails
import com.example.util.ExportFormat
import com.example.ui.components.ExportMenuButton
import com.example.ui.components.LocalSetTimelineActive
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidPrimary
import com.example.util.CategoryTimelineHelper
import com.example.util.CategoryTimelineInterval
import com.example.util.CategoryTimelinePeriod
import com.example.util.CategoryTimelineSortOrder
import com.example.util.DateUtils
import com.example.util.LanguageHelper
import com.example.util.TabExportHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ItemTimelineRow(
    val itemName: String,
    val type: TransactionType,
    val categoryName: String?,
    val amountsByPeriod: List<Double>,
    val totalAmount: Double = amountsByPeriod.sum(),
    val averageAmount: Double = if (amountsByPeriod.isNotEmpty()) totalAmount / amountsByPeriod.size else 0.0,
    val trendDelta: Double = if (amountsByPeriod.size >= 2) amountsByPeriod.last() - amountsByPeriod[amountsByPeriod.size - 2] else 0.0,
    val count: Int,
    val transactions: List<TransactionWithDetails>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemsTimelineScreen(
    transactions: List<TransactionWithDetails>,
    languageMode: LanguageMode,
    onBack: () -> Unit,
    onTransactionClick: ((Transaction) -> Unit)? = null
) {
    val context = LocalContext.current

    // Disable pager tab swipe when timeline is active
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

    var selectedInterval by remember { mutableStateOf(CategoryTimelineInterval.PAST_12_MONTHS) }
    var customStartDateMs by remember { mutableStateOf<Long?>(null) }
    var customEndDateMs by remember { mutableStateOf<Long?>(null) }
    var showCustomRangeDialog by remember { mutableStateOf(false) }

    var selectedTypeFilter by remember { mutableStateOf<TransactionType?>(null) } // null = All
    var selectedSortOrder by remember { mutableStateOf(CategoryTimelineSortOrder.AMOUNT_DESC) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    var selectedRowForDetails by remember { mutableStateOf<ItemTimelineRow?>(null) }

    // Generate timeline periods (default 12 months)
    val periods = remember(selectedInterval, customStartDateMs, customEndDateMs, languageMode) {
        CategoryTimelineHelper.generatePeriods(
            interval = selectedInterval,
            customPeriodsCount = 12,
            customStartDateMs = customStartDateMs,
            customEndDateMs = customEndDateMs,
            languageMode = languageMode
        )
    }

    // Helper to extract item name from a transaction
    fun extractItemName(item: TransactionWithDetails): String {
        val tx = item.transaction
        val payee = tx.payeeOrPayer.trim()
        return if (payee.isNotBlank()) payee else (item.category?.nameEn ?: "Item")
    }

    // Calculate timeline rows for each item
    val itemRows = remember(transactions, periods, selectedTypeFilter, searchQuery, selectedSortOrder) {
        if (periods.isEmpty()) return@remember emptyList()

        val minTime = periods.minOf { it.startEpochMs }
        val maxTime = periods.maxOf { it.endEpochMs }

        // Filter transactions within overall timeline range and type
        val eligibleTxs = transactions.filter { item ->
            val tx = item.transaction
            val withinDate = tx.dateEpochMs in minTime..maxTime
            val withinType = selectedTypeFilter == null || tx.type == selectedTypeFilter
            withinDate && withinType
        }

        // Map of item name -> all matching transactions
        val itemTxsMap = mutableMapOf<String, MutableList<TransactionWithDetails>>()
        for (item in eligibleTxs) {
            val name = extractItemName(item)
            itemTxsMap.getOrPut(name) { mutableListOf() }.add(item)
        }

        val rows = itemTxsMap.mapNotNull { (name, txList) ->
            if (searchQuery.isNotBlank() && !name.contains(searchQuery, ignoreCase = true)) {
                return@mapNotNull null
            }

            val periodAmounts = periods.map { period ->
                txList.filter { it.transaction.dateEpochMs in period.startEpochMs..period.endEpochMs }
                    .sumOf { it.transaction.amount }
            }

            val primaryType = txList.firstOrNull()?.transaction?.type ?: TransactionType.EXPENSE
            val catName = txList.firstOrNull()?.category?.let { if (languageMode == LanguageMode.BANGLA) it.nameBn else it.nameEn }

            ItemTimelineRow(
                itemName = name,
                type = primaryType,
                categoryName = catName,
                amountsByPeriod = periodAmounts,
                count = txList.size,
                transactions = txList
            )
        }

        // Apply sort
        when (selectedSortOrder) {
            CategoryTimelineSortOrder.AMOUNT_DESC -> rows.sortedByDescending { it.totalAmount }
            CategoryTimelineSortOrder.AMOUNT_ASC -> rows.sortedBy { it.totalAmount }
            CategoryTimelineSortOrder.NAME_ASC -> rows.sortedBy { it.itemName.lowercase() }
            CategoryTimelineSortOrder.DEFAULT -> rows.sortedByDescending { it.totalAmount }
        }
    }

    // Period totals
    val totalsByPeriod = remember(itemRows, periods.size) {
        List(periods.size) { colIdx ->
            itemRows.sumOf { it.amountsByPeriod.getOrElse(colIdx) { 0.0 } }
        }
    }
    val grandTotal = remember(totalsByPeriod) { totalsByPeriod.sum() }
    val avgPerPeriod = remember(totalsByPeriod) {
        if (totalsByPeriod.isNotEmpty()) grandTotal / totalsByPeriod.size else 0.0
    }

    val horizontalScrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("items_timeline_screen")
    ) {
        // --- Header Bar ---
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("items_timeline_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "আইটেম টাইমলাইন" else "Items Timeline",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        val periodSummaryText = if (periods.isNotEmpty()) {
                            val firstLbl = periods.first().shortLabel
                            val lastLbl = periods.last().shortLabel
                            "$firstLbl → $lastLbl (${periods.size} ${if (languageMode == LanguageMode.BANGLA) "মাস" else "periods"})"
                        } else {
                            if (languageMode == LanguageMode.BANGLA) selectedInterval.titleBn else selectedInterval.titleEn
                        }
                        Text(
                            text = periodSummaryText,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Search Button
                    IconButton(onClick = { isSearchActive = !isSearchActive }) {
                        Icon(
                            imageVector = if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "Search",
                            tint = if (isSearchActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Export Menu Button
                    ExportMenuButton(
                        onExport = { format ->
                            val header = listOf(if (languageMode == LanguageMode.BANGLA) "আইটেম" else "Item") +
                                    periods.map { it.shortLabel } +
                                    listOf(if (languageMode == LanguageMode.BANGLA) "মোট" else "Total", if (languageMode == LanguageMode.BANGLA) "গড়" else "Average")
                            val data = itemRows.map { row ->
                                listOf(row.itemName) +
                                        row.amountsByPeriod.map { if (it > 0) LanguageHelper.formatCurrency(it, languageMode) else "—" } +
                                        listOf(LanguageHelper.formatCurrency(row.totalAmount, languageMode), LanguageHelper.formatCurrency(row.averageAmount, languageMode))
                            }
                            TabExportHelper.shareTableAsFile(
                                context = context,
                                format = format,
                                title = if (languageMode == LanguageMode.BANGLA) "আইটেম টাইমলাইন রিপোর্ট" else "Items Timeline Report",
                                headers = header,
                                rows = data
                            )
                        },
                        languageMode = languageMode
                    )
                }

                // Search Box
                AnimatedVisibility(visible = isSearchActive) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, bottom = 4.dp),
                        placeholder = {
                            Text(
                                if (languageMode == LanguageMode.BANGLA) "আইটেম নাম দিয়ে খুঁজুন..." else "Search items...",
                                fontSize = 12.sp
                            )
                        },
                        singleLine = true,
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )
                }

                // Filter Chips Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(top = 4.dp, bottom = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Interval Presets
                    listOf(
                        CategoryTimelineInterval.PAST_12_MONTHS,
                        CategoryTimelineInterval.PAST_6_MONTHS,
                        CategoryTimelineInterval.PAST_3_MONTHS,
                        CategoryTimelineInterval.QUARTERLY,
                        CategoryTimelineInterval.YEARLY,
                        CategoryTimelineInterval.ALL_TIME,
                        CategoryTimelineInterval.CUSTOM
                    ).forEach { interval ->
                        val isSelected = selectedInterval == interval
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                if (interval == CategoryTimelineInterval.CUSTOM) {
                                    showCustomRangeDialog = true
                                } else {
                                    selectedInterval = interval
                                }
                            },
                            label = {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) interval.titleBn else interval.titleEn,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }

                    VerticalDivider(modifier = Modifier.height(20.dp), color = MaterialTheme.colorScheme.outlineVariant)

                    // Type Chips
                    FilterChip(
                        selected = selectedTypeFilter == null,
                        onClick = { selectedTypeFilter = null },
                        label = { Text(if (languageMode == LanguageMode.BANGLA) "সব" else "All", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = selectedTypeFilter == TransactionType.EXPENSE,
                        onClick = { selectedTypeFilter = TransactionType.EXPENSE },
                        label = { Text(if (languageMode == LanguageMode.BANGLA) "ব্যয়" else "Expense", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = selectedTypeFilter == TransactionType.INCOME,
                        onClick = { selectedTypeFilter = TransactionType.INCOME },
                        label = { Text(if (languageMode == LanguageMode.BANGLA) "আয়" else "Income", fontSize = 11.sp) }
                    )
                }
            }
        }

        // --- Custom Range Dialog ---
        if (showCustomRangeDialog) {
            val startDatePickerState = rememberDatePickerState(initialSelectedDateMillis = customStartDateMs ?: System.currentTimeMillis() - 365L * 24 * 3600 * 1000)
            val endDatePickerState = rememberDatePickerState(initialSelectedDateMillis = customEndDateMs ?: System.currentTimeMillis())
            var pickerStep by remember { mutableIntStateOf(0) }

            if (pickerStep == 0) {
                DatePickerDialog(
                    onDismissRequest = { showCustomRangeDialog = false },
                    confirmButton = {
                        TextButton(onClick = {
                            customStartDateMs = startDatePickerState.selectedDateMillis
                            pickerStep = 1
                        }) {
                            Text(if (languageMode == LanguageMode.BANGLA) "পরবর্তী (শেষ তারিখ)" else "Next (End Date)")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showCustomRangeDialog = false }) {
                            Text(LanguageHelper.getString("cancel", languageMode))
                        }
                    }
                ) {
                    DatePicker(state = startDatePickerState, title = {
                        Text(if (languageMode == LanguageMode.BANGLA) "শুরুর তারিখ নির্বাচন করুন" else "Select Start Date", modifier = Modifier.padding(16.dp))
                    })
                }
            } else {
                DatePickerDialog(
                    onDismissRequest = { showCustomRangeDialog = false },
                    confirmButton = {
                        TextButton(onClick = {
                            customEndDateMs = endDatePickerState.selectedDateMillis
                            selectedInterval = CategoryTimelineInterval.CUSTOM
                            showCustomRangeDialog = false
                        }) {
                            Text(LanguageHelper.getString("save", languageMode))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showCustomRangeDialog = false }) {
                            Text(LanguageHelper.getString("cancel", languageMode))
                        }
                    }
                ) {
                    DatePicker(state = endDatePickerState, title = {
                        Text(if (languageMode == LanguageMode.BANGLA) "শেষ তারিখ নির্বাচন করুন" else "Select End Date", modifier = Modifier.padding(16.dp))
                    })
                }
            }
        }

        // --- Detail Dialog for Tapped Item ---
        selectedRowForDetails?.let { row ->
            AlertDialog(
                onDismissRequest = { selectedRowForDetails = null },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = SolidPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(row.itemName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(if (languageMode == LanguageMode.BANGLA) "মোট লেনদেন:" else "Total Transactions:", fontSize = 12.sp)
                            Text("${row.count}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(if (languageMode == LanguageMode.BANGLA) "সর্বমোট টাকা:" else "Total Amount:", fontSize = 12.sp)
                            Text(LanguageHelper.formatCurrency(row.totalAmount, languageMode), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SolidPrimary)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            if (languageMode == LanguageMode.BANGLA) "লেনদেনসমূহ:" else "Recent Transactions:",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(row.transactions.take(20)) { item ->
                                val tx = item.transaction
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedRowForDetails = null
                                            onTransactionClick?.invoke(tx)
                                        },
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = if (tx.payeeOrPayer.isNotBlank()) tx.payeeOrPayer else (item.category?.nameEn ?: "Transaction"),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = DateUtils.formatDate(tx.dateEpochMs, languageMode),
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Text(
                                            text = LanguageHelper.formatCurrency(tx.amount, languageMode),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (tx.type == TransactionType.INCOME) SolidIncome else SolidExpense
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { selectedRowForDetails = null }) {
                        Text(LanguageHelper.getString("close", languageMode))
                    }
                }
            )
        }

        // --- Table: Items and month data columns scroll together as ONE horizontal section ---
        if (itemRows.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ShoppingBag,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "কোনো আইটেম পাওয়া যায়নি" else "No items found for this timeline",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        } else {
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                // Unified Horizontally Scrollable Container for the entire table
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .horizontalScroll(horizontalScrollState)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        // 1. Table Header
                        item {
                            Row(
                                modifier = Modifier
                                    .height(40.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Item Column Header
                                Box(
                                    modifier = Modifier
                                        .width(160.dp)
                                        .fillMaxHeight()
                                        .padding(horizontal = 8.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "আইটেম" else "Item",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                                // Period Column Headers
                                periods.forEach { period ->
                                    Box(
                                        modifier = Modifier
                                            .width(92.dp)
                                            .fillMaxHeight()
                                            .padding(horizontal = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = period.shortLabel,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                    VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                }

                                // Total Header
                                Box(
                                    modifier = Modifier
                                        .width(98.dp)
                                        .fillMaxHeight()
                                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
                                        .padding(horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "সর্বমোট" else "Total",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                                // Average Header
                                Box(
                                    modifier = Modifier
                                        .width(92.dp)
                                        .fillMaxHeight()
                                        .padding(horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "গড়/মাস" else "Avg/Period",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                                // Trend Header
                                Box(
                                    modifier = Modifier
                                        .width(72.dp)
                                        .fillMaxHeight()
                                        .padding(horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "ট্রেন্ড" else "Trend",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        }

                        // 2. Data Rows
                        items(itemRows) { row ->
                            Row(
                                modifier = Modifier
                                    .height(36.dp)
                                    .clickable { selectedRowForDetails = row },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Item Name Cell
                                Row(
                                    modifier = Modifier
                                        .width(160.dp)
                                        .fillMaxHeight()
                                        .padding(horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ShoppingBag,
                                        contentDescription = null,
                                        tint = if (row.type == TransactionType.INCOME) SolidIncome else SolidExpense,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = row.itemName,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                                // Period Values
                                row.amountsByPeriod.forEach { amount ->
                                    Box(
                                        modifier = Modifier
                                            .width(92.dp)
                                            .fillMaxHeight()
                                            .padding(horizontal = 6.dp),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Text(
                                            text = if (amount > 0) LanguageHelper.formatCurrency(amount, languageMode) else "—",
                                            fontSize = 10.5.sp,
                                            fontWeight = if (amount > 0) FontWeight.SemiBold else FontWeight.Normal,
                                            color = if (amount > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                                            maxLines = 1
                                        )
                                    }
                                    VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f))
                                }

                                // Total Column
                                Box(
                                    modifier = Modifier
                                        .width(98.dp)
                                        .fillMaxHeight()
                                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.12f))
                                        .padding(horizontal = 6.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Text(
                                        text = LanguageHelper.formatCurrency(row.totalAmount, languageMode),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (row.type == TransactionType.INCOME) SolidIncome else SolidExpense,
                                        maxLines = 1
                                    )
                                }
                                VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                                // Average Column
                                Box(
                                    modifier = Modifier
                                        .width(92.dp)
                                        .fillMaxHeight()
                                        .padding(horizontal = 6.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Text(
                                        text = LanguageHelper.formatCurrency(row.averageAmount, languageMode),
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                }
                                VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                                // Trend Column
                                Box(
                                    modifier = Modifier
                                        .width(72.dp)
                                        .fillMaxHeight()
                                        .padding(horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (row.trendDelta > 0) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                            contentDescription = "Up",
                                            tint = if (row.type == TransactionType.INCOME) SolidIncome else SolidExpense,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    } else if (row.trendDelta < 0) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                                            contentDescription = "Down",
                                            tint = if (row.type == TransactionType.INCOME) SolidExpense else SolidIncome,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    } else {
                                        Text("—", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                    }
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f))
                        }

                        // 3. Summary Row (Totals)
                        item {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
                            Row(
                                modifier = Modifier
                                    .height(38.dp)
                                    .background(SolidPrimary.copy(alpha = 0.08f)),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Title Cell
                                Box(
                                    modifier = Modifier
                                        .width(160.dp)
                                        .fillMaxHeight()
                                        .padding(horizontal = 8.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "সর্বমোট" else "Grand Total",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = SolidPrimary
                                    )
                                }
                                VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                                // Period Totals
                                totalsByPeriod.forEach { periodSum ->
                                    Box(
                                        modifier = Modifier
                                            .width(92.dp)
                                            .fillMaxHeight()
                                            .padding(horizontal = 6.dp),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Text(
                                            text = LanguageHelper.formatCurrency(periodSum, languageMode),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SolidPrimary,
                                            maxLines = 1
                                        )
                                    }
                                    VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                                }

                                // Grand Total
                                Box(
                                    modifier = Modifier
                                        .width(98.dp)
                                        .fillMaxHeight()
                                        .background(SolidPrimary.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Text(
                                        text = LanguageHelper.formatCurrency(grandTotal, languageMode),
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = SolidPrimary,
                                        maxLines = 1
                                    )
                                }
                                VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                                // Average
                                Box(
                                    modifier = Modifier
                                        .width(92.dp)
                                        .fillMaxHeight()
                                        .padding(horizontal = 6.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Text(
                                        text = LanguageHelper.formatCurrency(avgPerPeriod, languageMode),
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SolidPrimary,
                                        maxLines = 1
                                    )
                                }
                                VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                                Box(modifier = Modifier.width(72.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
