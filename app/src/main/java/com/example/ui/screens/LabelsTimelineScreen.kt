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
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.UnfoldLess
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
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
import com.example.util.TimelineViewOption
import com.example.util.TimelineViewOptionsTabRow

data class LabelTimelineRow(
    val labelName: String,
    val groupName: String,
    val amountsByPeriod: List<Double>,
    val totalAmount: Double = amountsByPeriod.sum(),
    val averageAmount: Double = if (amountsByPeriod.isNotEmpty()) totalAmount / amountsByPeriod.size else 0.0,
    val trendDelta: Double = if (amountsByPeriod.size >= 2) amountsByPeriod.last() - amountsByPeriod[amountsByPeriod.size - 2] else 0.0,
    val count: Int,
    val transactions: List<TransactionWithDetails>
)

data class LabelTimelineGroup(
    val groupName: String,
    val labels: List<LabelTimelineRow>,
    val amountsByPeriod: List<Double>,
    val totalAmount: Double = amountsByPeriod.sum(),
    val averageAmount: Double = if (amountsByPeriod.isNotEmpty()) totalAmount / amountsByPeriod.size else 0.0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabelsTimelineScreen(
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

    var viewOption by remember { mutableStateOf(TimelineViewOption.ALL) } // All / Groups / Items
    var selectedTypeFilter by remember { mutableStateOf<TransactionType?>(null) } // null = All
    var selectedSortOrder by remember { mutableStateOf(CategoryTimelineSortOrder.AMOUNT_DESC) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    val expandedGroupMap = remember { mutableStateMapOf<String, Boolean>() }
    var selectedRowForDetails by remember { mutableStateOf<LabelTimelineRow?>(null) }

    // Generate timeline periods
    val periods = remember(selectedInterval, customStartDateMs, customEndDateMs, languageMode) {
        CategoryTimelineHelper.generatePeriods(
            interval = selectedInterval,
            customPeriodsCount = 12,
            customStartDateMs = customStartDateMs,
            customEndDateMs = customEndDateMs,
            languageMode = languageMode
        )
    }

    // Helper to extract labels from a transaction
    fun extractLabels(item: TransactionWithDetails): Set<String> {
        val tx = item.transaction
        val note = tx.note
        val ref = tx.referenceNo
        val hashtagRegex = Regex("#[\\w\\u0980-\\u09FF]+")
        val foundTags = hashtagRegex.findAll("$note $ref").map { it.value }.toMutableSet()
        if (foundTags.isEmpty() && note.isNotBlank()) {
            val cleanNote = note.trim()
            if (cleanNote.length <= 25 && !cleanNote.contains("\n")) {
                foundTags.add("#$cleanNote")
            } else if (cleanNote.isNotBlank()) {
                foundTags.add("#General")
            }
        } else if (foundTags.isEmpty()) {
            val catTag = item.category?.localizedName(languageMode) ?: (if (languageMode == LanguageMode.BANGLA) "অন্যান্য" else "Untagged")
            foundTags.add("#$catTag")
        }
        return foundTags
    }

    // Calculate timeline rows for each label
    val labelRows = remember(transactions, periods, selectedTypeFilter, searchQuery, selectedSortOrder) {
        if (periods.isEmpty()) return@remember emptyList()

        val labelMap = mutableMapOf<String, MutableList<TransactionWithDetails>>()

        transactions.forEach { item ->
            val tx = item.transaction
            if (selectedTypeFilter != null && tx.type != selectedTypeFilter) return@forEach

            val labels = extractLabels(item)
            labels.forEach { label ->
                if (searchQuery.isBlank() || label.contains(searchQuery, ignoreCase = true)) {
                    labelMap.getOrPut(label) { mutableListOf() }.add(item)
                }
            }
        }

        val rows = labelMap.mapNotNull { (label, txList) ->
            val amounts = periods.map { period ->
                txList.filter { it.transaction.dateEpochMs in period.startEpochMs..period.endEpochMs }
                    .sumOf { it.transaction.amount }
            }

            val total = amounts.sum()
            if (total == 0.0) return@mapNotNull null

            // Determine label group (by prefix or category)
            val groupName = if (label.contains("/")) {
                label.substringBefore("/")
            } else {
                txList.firstOrNull()?.category?.localizedName(languageMode) ?: (if (languageMode == LanguageMode.BANGLA) "সাধারণ ট্যাগ" else "General Tags")
            }

            LabelTimelineRow(
                labelName = label,
                groupName = groupName,
                amountsByPeriod = amounts,
                totalAmount = total,
                count = txList.size,
                transactions = txList.sortedByDescending { it.transaction.dateEpochMs }
            )
        }

        when (selectedSortOrder) {
            CategoryTimelineSortOrder.AMOUNT_DESC -> rows.sortedByDescending { it.totalAmount }
            CategoryTimelineSortOrder.AMOUNT_ASC -> rows.sortedBy { it.totalAmount }
            CategoryTimelineSortOrder.NAME_ASC -> rows.sortedBy { it.labelName }
            else -> rows.sortedByDescending { it.totalAmount }
        }
    }

    // Group labels for GROUPS / ALL view
    val labelGroups = remember(labelRows, periods) {
        if (periods.isEmpty() || labelRows.isEmpty()) return@remember emptyList()

        labelRows.groupBy { it.groupName }
            .map { (grpName, labelsInGrp) ->
                val grpAmounts = periods.indices.map { pIdx ->
                    labelsInGrp.sumOf { it.amountsByPeriod.getOrElse(pIdx) { 0.0 } }
                }
                LabelTimelineGroup(
                    groupName = grpName,
                    labels = labelsInGrp,
                    amountsByPeriod = grpAmounts,
                    totalAmount = grpAmounts.sum()
                )
            }.sortedByDescending { it.totalAmount }
    }

    // Top Summary Card: Tagged Income | Tagged Expenses | Difference
    val topTotals = remember(transactions, periods) {
        if (periods.isEmpty()) {
            Triple(0.0, 0.0, 0.0)
        } else {
            val minStart = periods.minOfOrNull { it.startEpochMs } ?: 0L
            val maxEnd = periods.maxOfOrNull { it.endEpochMs } ?: Long.MAX_VALUE
            val validTxs = transactions.filter { it.transaction.dateEpochMs in minStart..maxEnd }
            val taggedIncome = validTxs.filter { it.transaction.type == TransactionType.INCOME }.sumOf { it.transaction.amount }
            val taggedExpenses = validTxs.filter { it.transaction.type == TransactionType.EXPENSE }.sumOf { it.transaction.amount }
            val diff = taggedIncome - taggedExpenses
            Triple(taggedIncome, taggedExpenses, diff)
        }
    }

    val taggedIncome = topTotals.first
    val taggedExpenses = topTotals.second
    val difference = topTotals.third

    val totalsByPeriod = remember(labelRows, periods) {
        periods.indices.map { pIdx ->
            labelRows.sumOf { it.amountsByPeriod.getOrElse(pIdx) { 0.0 } }
        }
    }
    val grandTotal = totalsByPeriod.sum()
    val avgPerPeriod = if (periods.isNotEmpty()) grandTotal / periods.size else 0.0

    val horizontalScrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("labels_timeline_screen")
    ) {
        // --- 1. Top Header Bar ---
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
                            .testTag("labels_timeline_back_btn")
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
                            text = if (languageMode == LanguageMode.BANGLA) "লেবেল টাইমলাইন" else "Labels Timeline",
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
                            val header = listOf(if (languageMode == LanguageMode.BANGLA) "লেবেল" else "Label") +
                                    periods.map { it.shortLabel } +
                                    listOf(if (languageMode == LanguageMode.BANGLA) "মোট" else "Total", if (languageMode == LanguageMode.BANGLA) "গড়" else "Average")
                            val data = labelRows.map { row ->
                                listOf(row.labelName) +
                                        row.amountsByPeriod.map { if (it > 0) LanguageHelper.formatCurrency(it, languageMode) else "—" } +
                                        listOf(LanguageHelper.formatCurrency(row.totalAmount, languageMode), LanguageHelper.formatCurrency(row.averageAmount, languageMode))
                            }
                            TabExportHelper.shareTableAsFile(
                                context = context,
                                format = format,
                                title = if (languageMode == LanguageMode.BANGLA) "লেবেল টাইমলাইন রিপোর্ট" else "Labels Timeline Report",
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
                                if (languageMode == LanguageMode.BANGLA) "লেবেল দিয়ে খুঁজুন..." else "Search labels...",
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

                // Date-Range Interval Preset Chips Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(top = 4.dp, bottom = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
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

                    // Type Chips: All, Expense, Income
                    FilterChip(
                        selected = selectedTypeFilter == null,
                        onClick = { selectedTypeFilter = null },
                        label = { Text(if (languageMode == LanguageMode.BANGLA) "সব প্রকার" else "All Types", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = selectedTypeFilter == TransactionType.INCOME,
                        onClick = { selectedTypeFilter = TransactionType.INCOME },
                        label = { Text(if (languageMode == LanguageMode.BANGLA) "ট্যাগড আয়" else "Tagged Income", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = selectedTypeFilter == TransactionType.EXPENSE,
                        onClick = { selectedTypeFilter = TransactionType.EXPENSE },
                        label = { Text(if (languageMode == LanguageMode.BANGLA) "ট্যাগড ব্যয়" else "Tagged Expense", fontSize = 11.sp) }
                    )
                }
            }
        }

        // --- 2. Top Summary Cards: Tagged Income | Tagged Expenses | Difference ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Tagged Income
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, SolidIncome.copy(alpha = 0.25f))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(SolidIncome))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "ট্যাগড আয়" else "Tagged Income",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = LanguageHelper.formatCurrency(taggedIncome, languageMode),
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = SolidIncome,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Tagged Expenses
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, SolidExpense.copy(alpha = 0.25f))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(SolidExpense))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "ট্যাগড ব্যয়" else "Tagged Expenses",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = LanguageHelper.formatCurrency(taggedExpenses, languageMode),
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = SolidExpense,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Difference
            val isDiffPositive = difference >= 0
            val diffColor = if (isDiffPositive) SolidIncome else SolidExpense
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, SolidPrimary.copy(alpha = 0.25f))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(SolidPrimary))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "পার্থক্য" else "Difference",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = (if (isDiffPositive) "+" else "") + LanguageHelper.formatCurrency(difference, languageMode),
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = diffColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // --- 3. View Options Tab Row (Groups / Items / All) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TimelineViewOptionsTabRow(
                selectedOption = viewOption,
                onOptionSelected = { viewOption = it },
                languageMode = languageMode,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(6.dp))

            if (viewOption == TimelineViewOption.ALL) {
                val areAllExpanded = labelGroups.isNotEmpty() && labelGroups.all { grp ->
                    expandedGroupMap[grp.groupName] != false
                }

                IconButton(
                    onClick = {
                        val targetState = !areAllExpanded
                        labelGroups.forEach { grp ->
                            expandedGroupMap[grp.groupName] = targetState
                        }
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (areAllExpanded) Icons.Default.UnfoldLess else Icons.Default.UnfoldMore,
                        contentDescription = if (areAllExpanded) "Collapse All" else "Expand All",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // --- Custom Range Dialog ---
        if (showCustomRangeDialog) {
            val startDatePickerState = rememberDatePickerState(initialSelectedDateMillis = customStartDateMs ?: (System.currentTimeMillis() - 365L * 24 * 3600 * 1000))
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

        // --- Detail Dialog for Tapped Label ---
        selectedRowForDetails?.let { row ->
            AlertDialog(
                onDismissRequest = { selectedRowForDetails = null },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Label, contentDescription = null, tint = SolidPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(row.labelName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
                                                text = if (tx.payeeOrPayer.isNotBlank()) tx.payeeOrPayer else (item.category?.localizedName(languageMode) ?: "Transaction"),
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

        // --- Table with FROZEN FIRST COLUMN (Label/Group Name) and scrollable periods ---
        if (labelRows.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Label,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "কোনো লেবেল পাওয়া যায়নি" else "No labels found for this timeline",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        } else {
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // 1. Table Header (Frozen first column + scrollable periods)
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Frozen Column Header
                            Box(
                                modifier = Modifier
                                    .width(155.dp)
                                    .fillMaxHeight()
                                    .padding(horizontal = 8.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "লেবেল / গ্রুপ" else "Label / Group",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                            // Scrollable Period Headers
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .horizontalScroll(horizontalScrollState),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
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
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }

                    // 2. Data Rows based on ViewOption
                    if (viewOption == TimelineViewOption.ITEMS) {
                        items(labelRows) { row ->
                            LabelTableRow(
                                row = row,
                                horizontalScrollState = horizontalScrollState,
                                languageMode = languageMode,
                                isIndented = false,
                                onClick = { selectedRowForDetails = row }
                            )
                        }
                    } else {
                        labelGroups.forEach { grp ->
                            val isExpanded = expandedGroupMap[grp.groupName] != false

                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(38.dp)
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Frozen Group Header
                                    Row(
                                        modifier = Modifier
                                            .width(155.dp)
                                            .fillMaxHeight()
                                            .clickable(enabled = viewOption == TimelineViewOption.ALL) {
                                                expandedGroupMap[grp.groupName] = !isExpanded
                                            }
                                            .padding(horizontal = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (viewOption == TimelineViewOption.ALL) {
                                            Icon(
                                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.width(2.dp))
                                        }
                                        Text(
                                            text = grp.groupName,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                                    // Scrollable period totals for group
                                    Row(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .horizontalScroll(horizontalScrollState),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        grp.amountsByPeriod.forEach { amount ->
                                            Box(
                                                modifier = Modifier
                                                    .width(92.dp)
                                                    .fillMaxHeight()
                                                    .padding(horizontal = 6.dp),
                                                contentAlignment = Alignment.CenterEnd
                                            ) {
                                                Text(
                                                    text = if (amount > 0) LanguageHelper.formatCurrency(amount, languageMode) else "—",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = SolidPrimary,
                                                    maxLines = 1
                                                )
                                            }
                                            VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                                        }

                                        // Total
                                        Box(
                                            modifier = Modifier
                                                .width(98.dp)
                                                .fillMaxHeight()
                                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f))
                                                .padding(horizontal = 6.dp),
                                            contentAlignment = Alignment.CenterEnd
                                        ) {
                                            Text(
                                                text = LanguageHelper.formatCurrency(grp.totalAmount, languageMode),
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
                                                text = LanguageHelper.formatCurrency(grp.averageAmount, languageMode),
                                                fontSize = 10.5.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1
                                            )
                                        }
                                        VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                                        Box(modifier = Modifier.width(72.dp))
                                    }
                                }
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                            }

                            // Sub-items if in ALL mode and expanded
                            if (viewOption == TimelineViewOption.ALL && isExpanded) {
                                items(grp.labels) { row ->
                                    LabelTableRow(
                                        row = row,
                                        horizontalScrollState = horizontalScrollState,
                                        languageMode = languageMode,
                                        isIndented = true,
                                        onClick = { selectedRowForDetails = row }
                                    )
                                }
                            }
                        }
                    }

                    // 3. Grand Summary Row
                    item {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .background(SolidPrimary.copy(alpha = 0.08f)),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Title Cell (Frozen)
                            Box(
                                modifier = Modifier
                                    .width(155.dp)
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

                            // Period Totals (Scrollable)
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .horizontalScroll(horizontalScrollState),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
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

@Composable
private fun LabelTableRow(
    row: LabelTimelineRow,
    horizontalScrollState: androidx.compose.foundation.ScrollState,
    languageMode: LanguageMode,
    isIndented: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Label Name Cell (Frozen)
        Row(
            modifier = Modifier
                .width(155.dp)
                .fillMaxHeight()
                .padding(start = if (isIndented) 16.dp else 8.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Label,
                contentDescription = null,
                tint = SolidPrimary,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = row.labelName,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

        // Period Values (Scrollable)
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .horizontalScroll(horizontalScrollState),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
                    color = SolidPrimary,
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
                        tint = SolidIncome,
                        modifier = Modifier.size(15.dp)
                    )
                } else if (row.trendDelta < 0) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                        contentDescription = "Down",
                        tint = SolidExpense,
                        modifier = Modifier.size(15.dp)
                    )
                } else {
                    Text("—", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                }
            }
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f))
}
