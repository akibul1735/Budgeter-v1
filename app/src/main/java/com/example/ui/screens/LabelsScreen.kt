package com.example.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.LabelOff
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.util.IconHelper
import com.example.util.LanguageHelper
import com.example.util.TabExportHelper
import com.example.util.TabFilterPreferences

private val CrimsonPink = Color(0xFFE91E63)
private val SlateText = Color(0xFF64748B)

typealias LabelSortOption = AggregatedSortOrder
typealias LabelDateFilterPreset = AggregatedDatePreset

enum class LabelCategorySegment {
    HASHTAGS,  // 🏷️ Explicit Labels & #Hashtags
    NOTES,     // 📝 Notes
    PAYEES,    // 👤 Payee / Payer
    UNTAGGED,  // ⚠️ Unlabeled
    ALL        // 🌐 All
}

data class AggregatedLabel(
    val labelName: String,
    val segmentType: LabelCategorySegment = LabelCategorySegment.ALL,
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
    val tabFilterPrefs = remember { TabFilterPreferences.getInstance(context) }

    var searchQuery by remember { mutableStateOf(tabFilterPrefs.labelsSearchQuery) }
    var activeTabMode by remember { mutableStateOf(tabFilterPrefs.labelsTabMode) }
    var selectedSegment by remember {
        mutableStateOf(
            try {
                LabelCategorySegment.valueOf(tabFilterPrefs.labelsCategorySegment)
            } catch (_: Exception) {
                LabelCategorySegment.HASHTAGS
            }
        )
    }

    var filterState by remember {
        mutableStateOf(
            AggregatedFilterState(
                datePreset = tabFilterPrefs.labelsDatePreset,
                sortOrder = tabFilterPrefs.labelsSortOrder
            )
        )
    }

    LaunchedEffect(searchQuery, activeTabMode, selectedSegment, filterState) {
        tabFilterPrefs.labelsSearchQuery = searchQuery
        tabFilterPrefs.labelsTabMode = activeTabMode
        tabFilterPrefs.labelsCategorySegment = selectedSegment.name
        tabFilterPrefs.labelsDatePreset = filterState.datePreset
        tabFilterPrefs.labelsSortOrder = filterState.sortOrder
    }

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

    val currentTargetType = when (activeTabMode) {
        "EXPENSE" -> TransactionType.EXPENSE
        "INCOME" -> TransactionType.INCOME
        else -> null
    }
    val effectiveType = filterState.transactionType ?: currentTargetType

    // 1. Filter Transactions by Date, Type, Account, Category, Status
    val filteredTxs = remember(
        transactions,
        effectiveType,
        startEpochMs,
        endEpochMs,
        filterState
    ) {
        transactions.filter { item ->
            val tx = item.transaction
            val matchesDate = tx.dateEpochMs in startEpochMs..endEpochMs
            val matchesType = effectiveType == null || tx.type == effectiveType
            val matchesAccount = filterState.selectedAccountIds.isEmpty() ||
                    (tx.debitAccountId != null && tx.debitAccountId in filterState.selectedAccountIds) ||
                    (tx.creditAccountId != null && tx.creditAccountId in filterState.selectedAccountIds)
            val matchesCategory = filterState.selectedCategoryIds.isEmpty() ||
                    (tx.categoryId != null && tx.categoryId in filterState.selectedCategoryIds) ||
                    (tx.subCategoryId != null && tx.subCategoryId in filterState.selectedCategoryIds)
            val matchesStatus = filterState.selectedStatuses.isEmpty() || tx.status in filterState.selectedStatuses

            matchesDate && matchesType && matchesAccount && matchesCategory && matchesStatus
        }
    }

    // 2. Segment Maps Calculation
    val (hashtagMap, noteMap, payeeMap, untaggedList, allMap) = remember(filteredTxs, languageMode) {
        val hashtags = mutableMapOf<String, MutableList<TransactionWithDetails>>()
        val notes = mutableMapOf<String, MutableList<TransactionWithDetails>>()
        val payees = mutableMapOf<String, MutableList<TransactionWithDetails>>()
        val untagged = mutableListOf<TransactionWithDetails>()
        val all = mutableMapOf<String, MutableList<TransactionWithDetails>>()

        val hashtagRegex = Regex("#[\\w\\u0980-\\u09FF]+")

        for (item in filteredTxs) {
            val tx = item.transaction
            val note = tx.note.trim()
            val ref = tx.referenceNo.trim()
            val payee = tx.payeeOrPayer.trim()

            // 1. Extract explicit hashtags from Note and Reference
            val explicitTags = hashtagRegex.findAll("$note $ref").map { it.value }.toMutableSet()
            if (ref.isNotBlank() && !ref.startsWith("#") && !ref.contains("TXN-") && !ref.contains("REC-")) {
                ref.split(",").map { it.trim() }.filter { it.isNotBlank() }.forEach { tag ->
                    explicitTags.add(if (tag.startsWith("#")) tag else "#$tag")
                }
            }

            var hasExplicitTag = false
            for (tag in explicitTags) {
                val normalizedTag = if (tag.startsWith("#")) tag else "#$tag"
                hashtags.getOrPut(normalizedTag) { mutableListOf() }.add(item)
                all.getOrPut(normalizedTag) { mutableListOf() }.add(item)
                hasExplicitTag = true
            }

            // 2. Extract note without hashtags
            val noteWithoutTags = hashtagRegex.replace(note, "").trim()
            if (noteWithoutTags.isNotBlank()) {
                notes.getOrPut(noteWithoutTags) { mutableListOf() }.add(item)
                if (!hasExplicitTag) {
                    all.getOrPut(noteWithoutTags) { mutableListOf() }.add(item)
                }
            }

            // 3. Extract Payee / Payer
            if (payee.isNotBlank()) {
                payees.getOrPut(payee) { mutableListOf() }.add(item)
            }

            // 4. Untagged
            if (!hasExplicitTag && noteWithoutTags.isBlank()) {
                untagged.add(item)
                val catTag = item.category?.localizedName(languageMode) ?: (if (languageMode == LanguageMode.BANGLA) "লেবেলহীন" else "Untagged")
                all.getOrPut(catTag) { mutableListOf() }.add(item)
            }
        }

        SegmentData(hashtags, notes, payees, untagged, all)
    }

    val totalTaggedFlow = filteredTxs.sumOf { it.transaction.amount }

    // Helper to build sorted aggregated label list
    fun buildAggregatedList(
        map: Map<String, List<TransactionWithDetails>>,
        segment: LabelCategorySegment
    ): List<AggregatedLabel> {
        val raw = map.map { (tagName, txList) ->
            val expenseSum = txList.filter { it.transaction.type == TransactionType.EXPENSE }.sumOf { it.transaction.amount }
            val incomeSum = txList.filter { it.transaction.type == TransactionType.INCOME }.sumOf { it.transaction.amount }
            val currentSum = if (effectiveType == TransactionType.EXPENSE) expenseSum else if (effectiveType == TransactionType.INCOME) incomeSum else (expenseSum + incomeSum)
            val totalSum = expenseSum + incomeSum
            val share = if (totalTaggedFlow > 0 && currentSum > 0) (currentSum / totalTaggedFlow) * 100.0 else 0.0

            AggregatedLabel(
                labelName = tagName,
                segmentType = segment,
                totalExpense = expenseSum,
                totalIncome = incomeSum,
                totalSum = totalSum,
                transactionCount = txList.size,
                transactions = txList.sortedByDescending { it.transaction.dateEpochMs },
                percentageShare = share
            )
        }.filter { label ->
            val totalAmt = if (effectiveType == TransactionType.EXPENSE) label.totalExpense else if (effectiveType == TransactionType.INCOME) label.totalIncome else (label.totalExpense + label.totalIncome)
            val matchesSearch = searchQuery.isBlank() || label.labelName.contains(searchQuery, ignoreCase = true)
            val matchesMin = filterState.minAmount == null || totalAmt >= filterState.minAmount!!
            val matchesMax = filterState.maxAmount == null || totalAmt <= filterState.maxAmount!!
            val matchesZero = !filterState.excludeZeroAmounts || totalAmt > 0

            matchesSearch && matchesMin && matchesMax && matchesZero
        }

        val sortedList = when (filterState.sortOrder) {
            AggregatedSortOrder.DEFAULT,
            AggregatedSortOrder.AMOUNT_DESC -> raw.sortedByDescending { if (effectiveType == TransactionType.EXPENSE) it.totalExpense else if (effectiveType == TransactionType.INCOME) it.totalIncome else (it.totalExpense + it.totalIncome) }
            AggregatedSortOrder.AMOUNT_ASC -> raw.sortedBy { if (effectiveType == TransactionType.EXPENSE) it.totalExpense else if (effectiveType == TransactionType.INCOME) it.totalIncome else (it.totalExpense + it.totalIncome) }
            AggregatedSortOrder.COUNT_DESC -> raw.sortedByDescending { it.transactionCount }
            AggregatedSortOrder.COUNT_ASC -> raw.sortedBy { it.transactionCount }
            AggregatedSortOrder.AVG_DESC -> raw.sortedByDescending {
                val amt = if (effectiveType == TransactionType.EXPENSE) it.totalExpense else if (effectiveType == TransactionType.INCOME) it.totalIncome else (it.totalExpense + it.totalIncome)
                if (it.transactionCount > 0) amt / it.transactionCount else 0.0
            }
            AggregatedSortOrder.AVG_ASC -> raw.sortedBy {
                val amt = if (effectiveType == TransactionType.EXPENSE) it.totalExpense else if (effectiveType == TransactionType.INCOME) it.totalIncome else (it.totalExpense + it.totalIncome)
                if (it.transactionCount > 0) amt / it.transactionCount else 0.0
            }
            AggregatedSortOrder.NAME_ASC -> raw.sortedBy { it.labelName.lowercase() }
            AggregatedSortOrder.NAME_DESC -> raw.sortedByDescending { it.labelName.lowercase() }
            AggregatedSortOrder.RECENT_DATE -> raw.sortedByDescending { it.transactions.firstOrNull()?.transaction?.dateEpochMs ?: 0L }
        }

        // In ALL mode, display Expense labels first, then Income labels!
        return if (effectiveType == null) {
            sortedList.sortedBy { if (it.totalExpense > 0 && it.totalIncome == 0.0) 0 else if (it.totalExpense >= it.totalIncome) 0 else 1 }
        } else {
            sortedList
        }
    }

    val displayedAggregatedLabels = remember(
        selectedSegment,
        hashtagMap,
        noteMap,
        payeeMap,
        allMap,
        searchQuery,
        filterState,
        effectiveType,
        totalTaggedFlow
    ) {
        when (selectedSegment) {
            LabelCategorySegment.HASHTAGS -> buildAggregatedList(hashtagMap, LabelCategorySegment.HASHTAGS)
            LabelCategorySegment.NOTES -> buildAggregatedList(noteMap, LabelCategorySegment.NOTES)
            LabelCategorySegment.PAYEES -> buildAggregatedList(payeeMap, LabelCategorySegment.PAYEES)
            LabelCategorySegment.ALL -> buildAggregatedList(allMap, LabelCategorySegment.ALL)
            LabelCategorySegment.UNTAGGED -> emptyList()
        }
    }

    val filteredUntaggedTxs = remember(untaggedList, searchQuery, filterState, effectiveType) {
        val list = untaggedList.filter { item ->
            val tx = item.transaction
            val catName = item.category?.localizedName(languageMode) ?: ""
            val matchesSearch = searchQuery.isBlank() || catName.contains(searchQuery, ignoreCase = true) || tx.amount.toString().contains(searchQuery)
            val matchesMin = filterState.minAmount == null || tx.amount >= filterState.minAmount!!
            val matchesMax = filterState.maxAmount == null || tx.amount <= filterState.maxAmount!!
            val matchesZero = !filterState.excludeZeroAmounts || tx.amount > 0

            matchesSearch && matchesMin && matchesMax && matchesZero
        }.sortedByDescending { it.transaction.dateEpochMs }

        if (effectiveType == null) {
            list.sortedBy { if (it.transaction.type == TransactionType.EXPENSE) 0 else 1 }
        } else {
            list
        }
    }

    val totalExpenseOverall = remember(displayedAggregatedLabels, filteredUntaggedTxs, selectedSegment) {
        if (selectedSegment == LabelCategorySegment.UNTAGGED) {
            filteredUntaggedTxs.filter { it.transaction.type == TransactionType.EXPENSE }.sumOf { it.transaction.amount }
        } else {
            displayedAggregatedLabels.sumOf { it.totalExpense }
        }
    }
    val totalIncomeOverall = remember(displayedAggregatedLabels, filteredUntaggedTxs, selectedSegment) {
        if (selectedSegment == LabelCategorySegment.UNTAGGED) {
            filteredUntaggedTxs.filter { it.transaction.type == TransactionType.INCOME }.sumOf { it.transaction.amount }
        } else {
            displayedAggregatedLabels.sumOf { it.totalIncome }
        }
    }
    val totalFlowOverall = remember(selectedSegment, activeTabMode, totalExpenseOverall, totalIncomeOverall) {
        when (activeTabMode) {
            "EXPENSE" -> totalExpenseOverall
            "INCOME" -> totalIncomeOverall
            else -> totalExpenseOverall + totalIncomeOverall
        }
    }

    val activeFilterSummary = remember(filterState, searchQuery, languageMode, selectedSegment) {
        val segLabel = when (selectedSegment) {
            LabelCategorySegment.HASHTAGS -> if (languageMode == LanguageMode.BANGLA) "হ্যাশট্যাগ ও লেবেল" else "Labels & Hashtags"
            LabelCategorySegment.NOTES -> if (languageMode == LanguageMode.BANGLA) "নোট" else "Notes"
            LabelCategorySegment.PAYEES -> if (languageMode == LanguageMode.BANGLA) "প্রাপক / প্রদানকারী" else "Payees"
            LabelCategorySegment.UNTAGGED -> if (languageMode == LanguageMode.BANGLA) "লেবেলহীন" else "Unlabeled"
            LabelCategorySegment.ALL -> if (languageMode == LanguageMode.BANGLA) "সকল" else "All"
        }
        val summary = filterState.buildFilterSummary(languageMode)
        val base = "$segLabel • $summary"
        if (searchQuery.isNotBlank()) {
            "$base • \"${searchQuery.trim()}\""
        } else {
            base
        }
    }

    val isLight = MaterialTheme.colorScheme.surface.luminance() > 0.5f

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            AppTabHeader(
                title = LanguageHelper.getString("labels", languageMode),
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                searchPlaceholder = if (languageMode == LanguageMode.BANGLA) "লেবেল / নোট খুঁজুন..." else "Search labels/notes...",
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
                            if (selectedSegment == LabelCategorySegment.UNTAGGED) {
                                TabExportHelper.exportTransactions(
                                    context = context,
                                    format = format,
                                    transactions = filteredUntaggedTxs,
                                    filterSummary = activeFilterSummary,
                                    languageMode = languageMode
                                )
                            } else {
                                TabExportHelper.exportLabels(
                                    context = context,
                                    format = format,
                                    filterSubtitle = activeFilterSummary,
                                    labels = displayedAggregatedLabels,
                                    languageMode = languageMode
                                )
                            }
                        }
                    )
                }
            )

            // Segmented Filter Pills Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 3.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Hashtags
                SegmentPill(
                    icon = Icons.Default.Tag,
                    label = if (languageMode == LanguageMode.BANGLA) "হ্যাশট্যাগ ও লেবেল" else "Hashtags",
                    count = hashtagMap.size,
                    isSelected = selectedSegment == LabelCategorySegment.HASHTAGS,
                    languageMode = languageMode,
                    onClick = { selectedSegment = LabelCategorySegment.HASHTAGS }
                )

                // 2. Notes
                SegmentPill(
                    icon = Icons.Default.Description,
                    label = if (languageMode == LanguageMode.BANGLA) "নোট (Notes)" else "Notes",
                    count = noteMap.size,
                    isSelected = selectedSegment == LabelCategorySegment.NOTES,
                    languageMode = languageMode,
                    onClick = { selectedSegment = LabelCategorySegment.NOTES }
                )

                // 3. Payees
                SegmentPill(
                    icon = Icons.Default.Person,
                    label = if (languageMode == LanguageMode.BANGLA) "প্রাপক / ব্যক্তি" else "Payees",
                    count = payeeMap.size,
                    isSelected = selectedSegment == LabelCategorySegment.PAYEES,
                    languageMode = languageMode,
                    onClick = { selectedSegment = LabelCategorySegment.PAYEES }
                )

                // 4. Untagged
                SegmentPill(
                    icon = Icons.Default.LabelOff,
                    label = if (languageMode == LanguageMode.BANGLA) "লেবেলহীন" else "Unlabeled",
                    count = untaggedList.size,
                    isSelected = selectedSegment == LabelCategorySegment.UNTAGGED,
                    languageMode = languageMode,
                    isAlertStyle = untaggedList.isNotEmpty(),
                    onClick = { selectedSegment = LabelCategorySegment.UNTAGGED }
                )

                // 5. All
                SegmentPill(
                    icon = Icons.Default.Label,
                    label = if (languageMode == LanguageMode.BANGLA) "সকল (All)" else "All",
                    count = allMap.size,
                    isSelected = selectedSegment == LabelCategorySegment.ALL,
                    languageMode = languageMode,
                    onClick = { selectedSegment = LabelCategorySegment.ALL }
                )
            }

            // Summary Card
            Surface(
                color = if (isLight) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        val titleText = when (selectedSegment) {
                            LabelCategorySegment.HASHTAGS -> when (activeTabMode) {
                                "EXPENSE" -> if (languageMode == LanguageMode.BANGLA) "মোট হ্যাশট্যাগ ব্যয়" else "Hashtags Expense"
                                "INCOME" -> if (languageMode == LanguageMode.BANGLA) "মোট হ্যাশট্যাগ আয়" else "Hashtags Income"
                                else -> if (languageMode == LanguageMode.BANGLA) "সর্বমোট হ্যাশট্যাগ প্রবাহ" else "Total Hashtag Flow"
                            }
                            LabelCategorySegment.NOTES -> when (activeTabMode) {
                                "EXPENSE" -> if (languageMode == LanguageMode.BANGLA) "মোট নোট ব্যয়" else "Notes Expense"
                                "INCOME" -> if (languageMode == LanguageMode.BANGLA) "মোট নোট আয়" else "Notes Income"
                                else -> if (languageMode == LanguageMode.BANGLA) "সর্বমোট নোট প্রবাহ" else "Total Note Flow"
                            }
                            LabelCategorySegment.PAYEES -> when (activeTabMode) {
                                "EXPENSE" -> if (languageMode == LanguageMode.BANGLA) "মোট প্রাপক ব্যয়" else "Payees Expense"
                                "INCOME" -> if (languageMode == LanguageMode.BANGLA) "মোট প্রাপক আয়" else "Payees Income"
                                else -> if (languageMode == LanguageMode.BANGLA) "সর্বমোট প্রাপক লেনদেন" else "Total Payee Flow"
                            }
                            LabelCategorySegment.UNTAGGED -> when (activeTabMode) {
                                "EXPENSE" -> if (languageMode == LanguageMode.BANGLA) "লেবেলহীন ব্যয়" else "Unlabeled Expense"
                                "INCOME" -> if (languageMode == LanguageMode.BANGLA) "লেবেলহীন আয়" else "Unlabeled Income"
                                else -> if (languageMode == LanguageMode.BANGLA) "লেবেলহীন সর্বমোট" else "Total Unlabeled"
                            }
                            LabelCategorySegment.ALL -> when (activeTabMode) {
                                "EXPENSE" -> if (languageMode == LanguageMode.BANGLA) "মোট সর্বমোট ব্যয়" else "Total Labels Expense"
                                "INCOME" -> if (languageMode == LanguageMode.BANGLA) "মোট সর্বমোট আয়" else "Total Labels Income"
                                else -> if (languageMode == LanguageMode.BANGLA) "সর্বমোট লেবেল প্রবাহ" else "Total Label Flow"
                            }
                        }
                        Text(
                            text = titleText,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = SlateText
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        if (activeTabMode == "ALL") {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = LanguageHelper.formatCurrency(totalExpenseOverall, languageMode),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CrimsonPink
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "•",
                                    fontSize = 14.sp,
                                    color = SlateText
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = LanguageHelper.formatCurrency(totalIncomeOverall, languageMode),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SolidIncome
                                )
                            }
                        } else {
                            Text(
                                text = LanguageHelper.formatCurrency(totalFlowOverall, languageMode),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (activeTabMode == "EXPENSE") CrimsonPink else SolidIncome
                            )
                        }
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
                                val countLabel = when (selectedSegment) {
                                    LabelCategorySegment.HASHTAGS -> if (languageMode == LanguageMode.BANGLA) "ট্যাগ" else "Tags"
                                    LabelCategorySegment.NOTES -> if (languageMode == LanguageMode.BANGLA) "নোট" else "Notes"
                                    LabelCategorySegment.PAYEES -> if (languageMode == LanguageMode.BANGLA) "প্রাপক" else "Payees"
                                    LabelCategorySegment.UNTAGGED -> if (languageMode == LanguageMode.BANGLA) "লেনদেন" else "Txns"
                                    LabelCategorySegment.ALL -> if (languageMode == LanguageMode.BANGLA) "আইটেম" else "Items"
                                }
                                val countVal = if (selectedSegment == LabelCategorySegment.UNTAGGED) filteredUntaggedTxs.size else displayedAggregatedLabels.size
                                Text(
                                    text = countLabel,
                                    fontSize = 9.5.sp,
                                    color = SlateText
                                )
                                Text(
                                    text = LanguageHelper.formatNumber(countVal.toDouble(), languageMode, false),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        if (selectedSegment != LabelCategorySegment.UNTAGGED) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            ) {
                                val totalTxCount = displayedAggregatedLabels.sumOf { it.transactionCount }
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
            }

            // Main Content Area
            if (selectedSegment == LabelCategorySegment.UNTAGGED) {
                // Untagged Transactions List
                if (filteredUntaggedTxs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = SolidIncome,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "সব লেনদেনে লেবেল বা নোট যুক্ত করা আছে! 🎉" else "All transactions have labels or notes! 🎉",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "কোনো লেবেলহীন লেনদেন নেই" else "No untagged transactions found for this period.",
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 125.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        items(filteredUntaggedTxs, key = { it.transaction.id }) { item ->
                            UntaggedTransactionRow(
                                item = item,
                                languageMode = languageMode,
                                onTagClick = { onTransactionClick(item.transaction) }
                            )
                        }
                    }
                }
            } else {
                // Aggregated Labels List
                if (displayedAggregatedLabels.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = when (selectedSegment) {
                                    LabelCategorySegment.HASHTAGS -> Icons.Default.Tag
                                    LabelCategorySegment.NOTES -> Icons.Default.Description
                                    LabelCategorySegment.PAYEES -> Icons.Default.Person
                                    else -> Icons.Default.LocalOffer
                                },
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            val emptyMsg = when (selectedSegment) {
                                LabelCategorySegment.HASHTAGS -> if (languageMode == LanguageMode.BANGLA) "কোনো হ্যাশট্যাগ পাওয়া যায়নি (নোট এ #ট্যাগ ব্যবহার করুন)" else "No hashtags found (use #tags in transaction notes or form)"
                                LabelCategorySegment.NOTES -> if (languageMode == LanguageMode.BANGLA) "কোনো নোটযুক্ত লেনদেন নেই" else "No transactions with notes found"
                                LabelCategorySegment.PAYEES -> if (languageMode == LanguageMode.BANGLA) "কোনো প্রাপক/প্রদানকারী পাওয়া যায়নি" else "No payees/payers entered in transactions"
                                else -> if (languageMode == LanguageMode.BANGLA) "কোনো লেবেল পাওয়া যায়নি" else "No labels found"
                            }
                            Text(
                                text = emptyMsg,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 125.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        items(displayedAggregatedLabels, key = { it.labelName }) { label ->
                            AggregatedLabelCard(
                                label = label,
                                languageMode = languageMode,
                                onClick = { selectedDrilldownLabel = label }
                            )
                        }
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
                            val targetType = if (activeTabMode == "INCOME") TransactionType.INCOME else TransactionType.EXPENSE
                            onAddTransactionClick?.invoke(targetType)
                        },
                        containerColor = if (activeTabMode == "INCOME") SolidIncome else Color(0xFF2563EB),
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

                // Segmented Toggle: [ Expenses (ব্যয়) | All (সকল) | Income (আয়) ]
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
                        // 1. Expense Button (Left)
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
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "ব্যয়" else "Expenses",
                                    fontSize = 12.5.sp,
                                    fontWeight = if (isExpense) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isExpense) CrimsonPink else SlateText
                                )
                            }
                        }

                        // 2. All Button (Middle)
                        val isAll = activeTabMode == "ALL"
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isAll) SolidPrimary.copy(alpha = 0.16f) else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { activeTabMode = "ALL" }
                                .testTag("labels_mode_all")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FilterList,
                                    contentDescription = null,
                                    tint = if (isAll) SolidPrimary else SlateText,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "সকল (All)" else "All",
                                    fontSize = 12.5.sp,
                                    fontWeight = if (isAll) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isAll) SolidPrimary else SlateText
                                )
                            }
                        }

                        // 3. Income Button (Right)
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
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "আয়" else "Income",
                                    fontSize = 12.5.sp,
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
                            text = "${label.transactionCount} ${if (languageMode == LanguageMode.BANGLA) "টি লেনদেন" else "transactions"} • ${LanguageHelper.formatCurrency(label.totalSum, languageMode)}",
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
                    Text(LanguageHelper.getString("done", languageMode).ifEmpty { "Done" })
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

private data class SegmentData(
    val hashtags: Map<String, List<TransactionWithDetails>>,
    val notes: Map<String, List<TransactionWithDetails>>,
    val payees: Map<String, List<TransactionWithDetails>>,
    val untagged: List<TransactionWithDetails>,
    val all: Map<String, List<TransactionWithDetails>>
)

@Composable
private fun SegmentPill(
    icon: ImageVector,
    label: String,
    count: Int,
    isSelected: Boolean,
    languageMode: LanguageMode,
    isAlertStyle: Boolean = false,
    onClick: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val activeColor = if (isAlertStyle && !isSelected && count > 0) Color(0xFFE65100) else primaryColor
    val isLight = MaterialTheme.colorScheme.surface.luminance() > 0.5f

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (isSelected) primaryColor else if (isLight) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(
            1.dp,
            if (isSelected) primaryColor else if (isAlertStyle && count > 0) Color(0xFFFFB74D) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        ),
        shadowElevation = if (isSelected) 2.dp else 0.dp,
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else activeColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = label,
                fontSize = 11.5.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
            )
            if (count > 0) {
                Spacer(modifier = Modifier.width(5.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) Color.White.copy(alpha = 0.25f) else (if (isAlertStyle) Color(0xFFFFE0B2) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f))
                ) {
                    Text(
                        text = LanguageHelper.formatNumber(count.toDouble(), languageMode, false),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else (if (isAlertStyle) Color(0xFFE65100) else MaterialTheme.colorScheme.onSurfaceVariant),
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                    )
                }
            }
        }
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

    val iconVector = when (label.segmentType) {
        LabelCategorySegment.HASHTAGS -> Icons.Default.Tag
        LabelCategorySegment.NOTES -> Icons.Default.Description
        LabelCategorySegment.PAYEES -> Icons.Default.Person
        else -> Icons.Default.Label
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
                        imageVector = iconVector,
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
                        text = LanguageHelper.formatCurrency(totalAmt, languageMode),
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = typeColor,
                        textAlign = TextAlign.End,
                        maxLines = 1
                    )
                    val avg = if (label.transactionCount > 0) totalAmt / label.transactionCount else 0.0
                    Text(
                        text = "Avg: ${LanguageHelper.formatCurrency(avg, languageMode)}",
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

@Composable
private fun UntaggedTransactionRow(
    item: TransactionWithDetails,
    languageMode: LanguageMode,
    onTagClick: () -> Unit
) {
    val tx = item.transaction
    val isExpense = tx.type == TransactionType.EXPENSE
    val typeColor = if (isExpense) CrimsonPink else SolidIncome
    val isLight = MaterialTheme.colorScheme.surface.luminance() > 0.5f

    val cardBgColor = if (isLight) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    val cardBorder = BorderStroke(1.1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    val cardShape = RoundedCornerShape(11.dp)

    Surface(
        color = cardBgColor,
        shape = cardShape,
        border = cardBorder,
        modifier = Modifier
            .fillMaxWidth()
            .clip(cardShape)
            .clickable { onTagClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Category Icon Badge
            val catIcon = item.category?.iconName ?: "Category"
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(typeColor.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                IconHelper.AppIcon(
                    iconName = item.category?.iconName,
                    fallbackName = item.category?.nameEn,
                    contentDescription = item.category?.nameEn,
                    tint = typeColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(9.dp))

            // Middle Column: Category Name + Date & Account
            Column(modifier = Modifier.weight(1f)) {
                val title = item.category?.localizedName(languageMode) ?: (if (languageMode == LanguageMode.BANGLA) "লেনদেন" else "Transaction")
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                val dateStr = DateUtils.formatDate(tx.dateEpochMs, languageMode)
                val accName = if (isExpense) item.creditAccount?.localizedName(languageMode) else item.debitAccount?.localizedName(languageMode)
                val subtitle = if (accName != null) "$dateStr • $accName" else dateStr
                Text(
                    text = subtitle,
                    fontSize = 10.5.sp,
                    color = SlateText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Right Column: Amount + "+ Tag" Chip Button
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = LanguageHelper.formatCurrency(tx.amount, languageMode),
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = typeColor
                )
                Spacer(modifier = Modifier.height(3.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SolidPrimary.copy(alpha = 0.12f),
                    border = BorderStroke(0.8.dp, SolidPrimary.copy(alpha = 0.35f)),
                    modifier = Modifier.clickable { onTagClick() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = SolidPrimary,
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "ট্যাগ দিন" else "Add Tag",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = SolidPrimary
                        )
                    }
                }
            }
        }
    }
}
