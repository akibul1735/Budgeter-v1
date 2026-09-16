package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.graphics.luminance
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
import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionType
import com.example.data.model.TransactionWithDetails
import com.example.ui.components.AppTabHeader
import com.example.ui.components.AutoHidingBottomContainer
import com.example.ui.components.ExportMenuButton
import com.example.ui.components.LocalHeaderScrollState
import com.example.ui.dialogs.AggregatedDatePreset
import com.example.ui.dialogs.AggregatedFilterDialog
import com.example.ui.dialogs.AggregatedFilterState
import com.example.ui.dialogs.AggregatedSortOrder
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidPrimary
import com.example.util.DateUtils
import com.example.util.LanguageHelper
import com.example.util.TabExportHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val CrimsonPink = Color(0xFFE91E63)
private val SlateText = Color(0xFF64748B)

typealias LabelSortOption = AggregatedSortOrder
typealias LabelDateFilterPreset = AggregatedDatePreset

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
    categories: List<Category> = emptyList(),
    accounts: List<Account> = emptyList(),
    languageMode: LanguageMode,
    onOpenDrawer: () -> Unit = {},
    onTransactionClick: (Transaction) -> Unit,
    onAddTransactionClick: ((TransactionType) -> Unit)? = null
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var activeTabMode by remember { mutableStateOf("EXPENSE") }
    var filterState by remember { mutableStateOf(AggregatedFilterState()) }

    var showFilterDialog by remember { mutableStateOf(false) }
    var showTimelineScreen by remember { mutableStateOf(false) }
    var selectedDrilldownLabel by remember { mutableStateOf<AggregatedLabel?>(null) }

    if (showTimelineScreen) {
        LabelsTimelineScreen(
            transactions = transactions,
            languageMode = languageMode,
            onBack = { showTimelineScreen = false },
            onTransactionClick = onTransactionClick
        )
        return
    }

    // Date Bounds
    val (startEpochMs, endEpochMs) = remember(filterState.datePreset, filterState.customStartDateMs, filterState.customEndDateMs) {
        filterState.calculateDateRange()
    }

    val currentTargetType = if (activeTabMode == "EXPENSE") TransactionType.EXPENSE else TransactionType.INCOME
    val effectiveType = filterState.transactionType ?: currentTargetType

    // Extract tags/labels from transaction notes & referenceNo
    val aggregatedLabels = remember(
        transactions,
        searchQuery,
        effectiveType,
        startEpochMs,
        endEpochMs,
        filterState
    ) {
        val filteredTxs = transactions.filter { item ->
            val tx = item.transaction
            val matchesDate = tx.dateEpochMs in startEpochMs..endEpochMs
            val matchesType = tx.type == effectiveType
            val matchesAccount = filterState.selectedAccountIds.isEmpty() ||
                    (tx.debitAccountId != null && tx.debitAccountId in filterState.selectedAccountIds) ||
                    (tx.creditAccountId != null && tx.creditAccountId in filterState.selectedAccountIds)
            val matchesCategory = filterState.selectedCategoryIds.isEmpty() ||
                    (tx.categoryId != null && tx.categoryId in filterState.selectedCategoryIds) ||
                    (tx.subCategoryId != null && tx.subCategoryId in filterState.selectedCategoryIds)
            val matchesStatus = filterState.selectedStatuses.isEmpty() || tx.status in filterState.selectedStatuses

            matchesDate && matchesType && matchesAccount && matchesCategory && matchesStatus
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

            // If no hashtag found, and note is short, treat note as label
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

        val totalTaggedFlow = filteredTxs.sumOf { it.transaction.amount }

        val list = labelMap.map { (tagName, txList) ->
            val expenseSum = txList.filter { it.transaction.type == TransactionType.EXPENSE }.sumOf { it.transaction.amount }
            val incomeSum = txList.filter { it.transaction.type == TransactionType.INCOME }.sumOf { it.transaction.amount }
            val currentSum = if (effectiveType == TransactionType.EXPENSE) expenseSum else incomeSum
            val totalSum = expenseSum + incomeSum
            val share = if (totalTaggedFlow > 0 && currentSum > 0) (currentSum / totalTaggedFlow) * 100.0 else 0.0

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
            val totalAmt = if (effectiveType == TransactionType.EXPENSE) label.totalExpense else label.totalIncome
            val matchesSearch = searchQuery.isBlank() || label.labelName.contains(searchQuery, ignoreCase = true)
            val matchesMin = filterState.minAmount == null || totalAmt >= filterState.minAmount!!
            val matchesMax = filterState.maxAmount == null || totalAmt <= filterState.maxAmount!!
            val matchesZero = !filterState.excludeZeroAmounts || totalAmt > 0

            matchesSearch && matchesMin && matchesMax && matchesZero
        }

        when (filterState.sortOrder) {
            AggregatedSortOrder.AMOUNT_DESC -> list.sortedByDescending { if (effectiveType == TransactionType.EXPENSE) it.totalExpense else it.totalIncome }
            AggregatedSortOrder.AMOUNT_ASC -> list.sortedBy { if (effectiveType == TransactionType.EXPENSE) it.totalExpense else it.totalIncome }
            AggregatedSortOrder.COUNT_DESC -> list.sortedByDescending { it.transactionCount }
            AggregatedSortOrder.COUNT_ASC -> list.sortedBy { it.transactionCount }
            AggregatedSortOrder.AVG_DESC -> list.sortedByDescending {
                val amt = if (effectiveType == TransactionType.EXPENSE) it.totalExpense else it.totalIncome
                if (it.transactionCount > 0) amt / it.transactionCount else 0.0
            }
            AggregatedSortOrder.AVG_ASC -> list.sortedBy {
                val amt = if (effectiveType == TransactionType.EXPENSE) it.totalExpense else it.totalIncome
                if (it.transactionCount > 0) amt / it.transactionCount else 0.0
            }
            AggregatedSortOrder.NAME_ASC -> list.sortedBy { it.labelName.lowercase() }
            AggregatedSortOrder.NAME_DESC -> list.sortedByDescending { it.labelName.lowercase() }
            AggregatedSortOrder.RECENT_DATE -> list.sortedByDescending { it.transactions.firstOrNull()?.transaction?.dateEpochMs ?: 0L }
        }
    }

    val totalFlowOverall = remember(aggregatedLabels, activeTabMode) {
        aggregatedLabels.sumOf { if (activeTabMode == "EXPENSE") it.totalExpense else it.totalIncome }
    }

    val activeFilterSummary = remember(filterState, searchQuery, languageMode) {
        val summary = filterState.buildFilterSummary(languageMode)
        if (searchQuery.isNotBlank()) {
            if (summary.isNotBlank()) "$summary • \"${searchQuery.trim()}\"" else "\"${searchQuery.trim()}\""
        } else {
            summary
        }
    }

    val isLight = MaterialTheme.colorScheme.surface.luminance() > 0.5f

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            AppTabHeader(
                title = LanguageHelper.getString("labels", languageMode),
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                searchPlaceholder = if (languageMode == LanguageMode.BANGLA) "লেবেল / ট্যাগ খুঁজুন..." else "Search labels/tags...",
                showSearchButton = true,
                showFilterButton = true,
                isFilterActive = filterState.isFilterActive,
                activeFilterCount = filterState.activeFilterCount,
                onFilterClick = { showFilterDialog = true },
                showTimelineButton = true,
                onTimelineClick = { showTimelineScreen = true },
                onOpenDrawer = onOpenDrawer,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp),
                actions = {
                    ExportMenuButton(
                        languageMode = languageMode,
                        onExport = { format ->
                            TabExportHelper.exportLabels(
                                context = context,
                                format = format,
                                filterSubtitle = activeFilterSummary,
                                labels = aggregatedLabels,
                                languageMode = languageMode
                            )
                        }
                    )
                }
            )

            // Net Earnings Style Summary Card
            Surface(
                color = if (isLight) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (activeTabMode == "EXPENSE") {
                                if (languageMode == LanguageMode.BANGLA) "মোট ব্যয় (Labels Expense)" else "Total Labels Expense"
                            } else {
                                if (languageMode == LanguageMode.BANGLA) "মোট আয় (Labels Income)" else "Total Labels Income"
                            },
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = SlateText
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "৳ ${LanguageHelper.formatCurrency(totalFlowOverall, languageMode)}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (activeTabMode == "EXPENSE") CrimsonPink else SolidIncome
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                            border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "লেবেল" else "Labels",
                                    fontSize = 9.5.sp,
                                    color = SlateText
                                )
                                Text(
                                    text = LanguageHelper.formatNumber(aggregatedLabels.size.toDouble(), languageMode, false),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                            border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        ) {
                            val totalTxCount = aggregatedLabels.sumOf { it.transactionCount }
                            Column(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "লেনদেন" else "Txns",
                                    fontSize = 9.5.sp,
                                    color = SlateText
                                )
                                Text(
                                    text = LanguageHelper.formatNumber(totalTxCount.toDouble(), languageMode, false),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
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
                    contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 6.dp, bottom = 125.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
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

        // Pinned Auto-Hiding Bottom Container with Segmented Toggle & Single Docked FAB
        val headerScrollState = LocalHeaderScrollState.current
        AutoHidingBottomContainer(
            headerScrollState = headerScrollState,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Single FAB above toggle, aligned to End
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    FloatingActionButton(
                        onClick = {
                            val targetType = if (activeTabMode == "EXPENSE") TransactionType.EXPENSE else TransactionType.INCOME
                            onAddTransactionClick?.invoke(targetType)
                        },
                        containerColor = if (activeTabMode == "EXPENSE") Color(0xFF2563EB) else SolidIncome,
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier
                            .size(52.dp)
                            .testTag("labels_add_fab")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = if (languageMode == LanguageMode.BANGLA) "নতুন লেনদেন" else "Add Transaction",
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                // Segmented Toggle: [ Expenses (ব্যয়) | Income (আয়) ]
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Expense Button
                        val isExpense = activeTabMode == "EXPENSE"
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isExpense) CrimsonPink.copy(alpha = 0.16f) else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { activeTabMode = "EXPENSE" }
                                .testTag("labels_mode_expense")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                                    contentDescription = null,
                                    tint = if (isExpense) CrimsonPink else SlateText,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "ব্যয় (Expenses)" else "Expenses",
                                    fontSize = 13.sp,
                                    fontWeight = if (isExpense) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isExpense) CrimsonPink else SlateText
                                )
                            }
                        }

                        // Income Button
                        val isIncome = activeTabMode == "INCOME"
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isIncome) SolidIncome.copy(alpha = 0.16f) else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { activeTabMode = "INCOME" }
                                .testTag("labels_mode_income")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                    contentDescription = null,
                                    tint = if (isIncome) SolidIncome else SlateText,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "আয় (Income)" else "Income",
                                    fontSize = 13.sp,
                                    fontWeight = if (isIncome) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isIncome) SolidIncome else SlateText
                                )
                            }
                        }
                    }
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
                    Column(modifier = Modifier.weight(1f)) {
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ExportMenuButton(
                            languageMode = languageMode,
                            onExport = { format ->
                                TabExportHelper.exportTransactions(
                                    context = context,
                                    format = format,
                                    transactions = label.transactions,
                                    filterSummary = "Label: ${label.labelName}",
                                    languageMode = languageMode
                                )
                            }
                        )
                        IconButton(onClick = { selectedDrilldownLabel = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
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
                                        text = DateUtils.formatDate(tx.dateEpochMs, languageMode),
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
                                val isPositiveEffect = when (tx.type) {
                                    TransactionType.EXPENSE -> tx.amount < 0
                                    TransactionType.INCOME -> tx.amount >= 0
                                    TransactionType.TRANSFER -> false
                                }
                                val sign = if (isPositiveEffect) "+" else "-"
                                Text(
                                    text = "$sign${LanguageHelper.formatCurrency(Math.abs(tx.amount), languageMode)}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPositiveEffect) SolidIncome else SolidExpense
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

    // Filter & Sort Dialog
    if (showFilterDialog) {
        AggregatedFilterDialog(
            title = if (languageMode == LanguageMode.BANGLA) "লেবেল ফিল্টার ও সাজানো" else "Filter & Sort Labels",
            currentState = filterState,
            categories = categories,
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
private fun AggregatedLabelCard(
    label: AggregatedLabel,
    languageMode: LanguageMode,
    onClick: () -> Unit
) {
    val isExpense = label.totalExpense >= label.totalIncome
    val typeColor = if (isExpense) CrimsonPink else SolidIncome

    val isLight = MaterialTheme.colorScheme.surface.luminance() > 0.5f
    val cardBgColor = if (isLight) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    }
    val cardBorder = if (isLight) {
        BorderStroke(1.3.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    } else {
        BorderStroke(1.1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
    }

    val cardShape = RoundedCornerShape(11.dp)
    Surface(
        color = cardBgColor,
        shape = cardShape,
        border = cardBorder,
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(cardShape)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 7.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left Icon Badge (32dp)
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(typeColor.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Label,
                        contentDescription = null,
                        tint = typeColor,
                        modifier = Modifier.size(17.dp)
                    )
                }

                Spacer(modifier = Modifier.width(9.dp))

                // Middle Column: Label Name (13sp) + Subtitle + Percentage Badge (9sp)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = label.labelName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(1.5.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val subtitle = "${label.transactionCount} ${if (languageMode == LanguageMode.BANGLA) "টি লেনদেন" else "txs"}"
                        Text(
                            text = subtitle,
                            fontSize = 10.5.sp,
                            color = SlateText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        if (label.percentageShare > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
                                shape = RoundedCornerShape(3.5.dp)
                            ) {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "${label.percentageShare.toInt()}% মোট" else "${label.percentageShare.toInt()}% of total",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = SlateText,
                                    modifier = Modifier.padding(horizontal = 3.5.dp, vertical = 0.5.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Right Column: Actual Amount (13.5sp) + Average (10sp)
                val totalAmt = if (isExpense) label.totalExpense else label.totalIncome
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "৳ ${LanguageHelper.formatCurrency(totalAmt, languageMode)}",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = typeColor,
                        textAlign = TextAlign.End,
                        maxLines = 1
                    )
                    val avg = if (label.transactionCount > 0) totalAmt / label.transactionCount else 0.0
                    Text(
                        text = "Avg: ৳ ${LanguageHelper.formatCurrency(avg, languageMode)}",
                        fontSize = 10.sp,
                        color = SlateText,
                        textAlign = TextAlign.End,
                        maxLines = 1
                    )
                }
            }
        }
    }
}


