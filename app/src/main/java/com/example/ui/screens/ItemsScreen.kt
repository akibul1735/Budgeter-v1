package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
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
import com.example.data.model.ItemImageCache
import com.example.data.model.LanguageMode
import com.example.data.model.Transaction
import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionType
import com.example.data.model.TransactionWithDetails
import com.example.ui.components.AppTabHeader
import com.example.ui.components.AutoHidingBottomContainer
import com.example.ui.components.ExportMenuButton
import com.example.ui.components.LocalHeaderScrollState
import com.example.ui.components.filter.FilterState
import com.example.ui.components.filter.UnifiedActiveFilterBar
import com.example.ui.components.filter.UnifiedFilterDialog
import com.example.ui.components.filter.activeChips
import com.example.ui.components.filter.activeFilterCount
import com.example.ui.components.filter.isActive
import com.example.ui.components.filter.specs.ItemsFilterSpec
import com.example.ui.dialogs.AggregatedDatePreset
import com.example.ui.dialogs.AggregatedSortOrder
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidPrimary
import com.example.util.DateUtils
import com.example.util.FilterStore
import com.example.util.IconHelper
import com.example.util.ItemCacheHelper
import com.example.util.LanguageHelper
import androidx.compose.runtime.LaunchedEffect
import com.example.util.TabExportHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val CrimsonPink = Color(0xFFE91E63)
private val SlateText = Color(0xFF64748B)

typealias ItemSortOption = AggregatedSortOrder
typealias ItemDateFilterPreset = AggregatedDatePreset

data class AggregatedItem(
    val id: String,
    val name: String,
    val type: TransactionType,
    val categoryId: Long? = null,
    val groupName: String? = null,
    val iconName: String? = null,
    val colorHex: String? = null,
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
    categories: List<Category> = emptyList(),
    accounts: List<Account> = emptyList(),
    itemImageCacheMap: Map<String, ItemImageCache> = emptyMap(),
    languageMode: LanguageMode,
    onOpenDrawer: () -> Unit = {},
    onTransactionClick: (Transaction) -> Unit,
    onAddTransactionClick: ((TransactionType) -> Unit)? = null
) {
    val context = LocalContext.current
    val filterStore = remember { FilterStore.getInstance(context) }
    var filterState by remember {
        mutableStateOf<FilterState>(filterStore.loadFilterState(ItemsFilterSpec.SPEC_KEY))
    }
    var searchQuery by remember { mutableStateOf("") }
    var showFilterDialog by remember { mutableStateOf(false) }
    var showTimelineScreen by remember { mutableStateOf(false) }
    var selectedDrilldownItem by remember { mutableStateOf<AggregatedItem?>(null) }

    val updateFilterState: (FilterState) -> Unit = { newState ->
        filterState = newState
        filterStore.saveFilterState(ItemsFilterSpec.SPEC_KEY, newState)
    }

    val itemsSpec = remember(categories, accounts) {
        ItemsFilterSpec.createSpec(categories = categories, accounts = accounts)
    }

    if (showTimelineScreen) {
        ItemsTimelineScreen(
            transactions = transactions,
            languageMode = languageMode,
            onBack = { showTimelineScreen = false },
            onTransactionClick = onTransactionClick
        )
        return
    }

    // Compute Date Bounds from FilterState
    val (startEpochMs, endEpochMs) = remember(filterState, itemsSpec) {
        ItemsFilterSpec.resolveDateBounds(itemsSpec, filterState)
    }

    val activeTabMode = remember(filterState) { ItemsFilterSpec.getTypeModeId(filterState) }
    val currentTargetType = when (activeTabMode) {
        ItemsFilterSpec.MODE_EXPENSE -> TransactionType.EXPENSE
        ItemsFilterSpec.MODE_INCOME -> TransactionType.INCOME
        else -> null
    }
    val specificFlowType = remember(filterState) { ItemsFilterSpec.getTransactionType(filterState) }
    val effectiveType = specificFlowType ?: currentTargetType

    val selectedAccountIds = remember(filterState) { ItemsFilterSpec.getSelectedAccountIds(filterState) }
    val selectedCategoryIds = remember(filterState) { ItemsFilterSpec.getSelectedCategoryIds(filterState) }
    val selectedStatuses = remember(filterState) { ItemsFilterSpec.getSelectedStatuses(filterState) }
    val minAmount = remember(filterState) { ItemsFilterSpec.getMinAmount(filterState) }
    val maxAmount = remember(filterState) { ItemsFilterSpec.getMaxAmount(filterState) }
    val excludeZero = remember(filterState) { ItemsFilterSpec.getExcludeZero(filterState) }
    val sortOrder = remember(filterState) { ItemsFilterSpec.getSortOrder(filterState) }

    // Filter transactions and group by item/payee
    val aggregatedItems = remember(
        transactions,
        searchQuery,
        effectiveType,
        startEpochMs,
        endEpochMs,
        selectedAccountIds,
        selectedCategoryIds,
        selectedStatuses,
        minAmount,
        maxAmount,
        excludeZero,
        sortOrder,
        languageMode
    ) {
        val filteredTxs = transactions.filter { item ->
            val tx = item.transaction
            val matchesDate = tx.dateEpochMs in startEpochMs..endEpochMs
            val matchesType = effectiveType == null || tx.type == effectiveType
            val matchesAccount = selectedAccountIds.isEmpty() ||
                    (tx.debitAccountId != null && tx.debitAccountId in selectedAccountIds) ||
                    (tx.creditAccountId != null && tx.creditAccountId in selectedAccountIds)
            val matchesCategory = selectedCategoryIds.isEmpty() ||
                    (tx.categoryId != null && tx.categoryId in selectedCategoryIds) ||
                    (tx.subCategoryId != null && tx.subCategoryId in selectedCategoryIds)
            val matchesStatus = selectedStatuses.isEmpty() || tx.status in selectedStatuses

            matchesDate && matchesType && matchesAccount && matchesCategory && matchesStatus
        }

        val grouped = filteredTxs.groupBy { item ->
            val tx = item.transaction
            val payee = tx.payeeOrPayer.trim()
            if (payee.isNotBlank()) {
                "payee_${tx.type.name}_${tx.categoryId ?: 0}_${tx.subCategoryId ?: 0}_${payee.lowercase()}"
            } else {
                val catId = item.category?.id ?: 0L
                val parentId = item.category?.parentId ?: 0L
                "cat_${tx.type.name}_${parentId}_${catId}"
            }
        }

        val totalFlowAmount = filteredTxs.sumOf { it.transaction.amount }

        val list = grouped.map { (key, txList) ->
            val first = txList.first()
            val tx = first.transaction
            val payee = tx.payeeOrPayer.trim()
            val itemName = if (payee.isNotBlank()) {
                payee
            } else {
                if (languageMode == LanguageMode.BANGLA) {
                    first.category?.nameBn ?: first.category?.nameEn ?: "অন্যান্য (Other)"
                } else {
                    first.category?.nameEn ?: "Other"
                }
            }

            val groupName = if (first.subCategory != null) {
                val parent = first.category
                if (languageMode == LanguageMode.BANGLA) parent?.nameBn ?: parent?.nameEn else parent?.nameEn
            } else if (first.category?.parentId != null) {
                if (languageMode == LanguageMode.BANGLA) first.category?.nameBn ?: first.category?.nameEn else first.category?.nameEn
            } else null

            // Priority 1: Check custom cached icon for this item / payee
            val customCached = ItemCacheHelper.findCachedIcon(itemName, itemImageCacheMap)
                ?: (if (payee.isNotBlank()) ItemCacheHelper.findCachedIcon(payee, itemImageCacheMap) else null)
            // Priority 2: In-App icon store auto match for the item name or payee
            val matchedInApp = if (customCached == null) {
                IconHelper.findMatchingInAppIcon(itemName)
                    ?: (if (payee.isNotBlank()) IconHelper.findMatchingInAppIcon(payee) else null)
            } else null

            // Priority 3: Subcategory / Category icon or Transaction icon
            val iconName = customCached?.iconKey?.takeIf { it.isNotBlank() }
                ?: matchedInApp?.takeIf { it.isNotBlank() && it != "Category" && it != "Folder" }
                ?: first.subCategory?.iconName?.takeIf { it.isNotBlank() && it != "Category" && it != "Folder" }
                ?: first.category?.iconName?.takeIf { it.isNotBlank() && it != "Category" && it != "Folder" }
                ?: ItemCacheHelper.resolveTransactionIconName(first, itemImageCacheMap)

            val colorHex = first.subCategory?.colorHex?.takeIf { it.isNotBlank() }
                ?: first.category?.colorHex?.takeIf { it.isNotBlank() }

            val expenseSum = txList.filter { it.transaction.type == TransactionType.EXPENSE }.sumOf { it.transaction.amount }
            val incomeSum = txList.filter { it.transaction.type == TransactionType.INCOME }.sumOf { it.transaction.amount }
            val currentSum = if (effectiveType == TransactionType.EXPENSE) expenseSum else if (effectiveType == TransactionType.INCOME) incomeSum else (expenseSum + incomeSum)
            val latestDate = txList.maxOfOrNull { it.transaction.dateEpochMs } ?: 0L
            val share = if (totalFlowAmount > 0 && currentSum > 0) (currentSum / totalFlowAmount) * 100.0 else 0.0

            AggregatedItem(
                id = key,
                name = itemName,
                type = tx.type,
                categoryId = first.category?.id,
                groupName = groupName,
                iconName = iconName,
                colorHex = colorHex,
                totalExpense = expenseSum,
                totalIncome = incomeSum,
                transactionCount = txList.size,
                latestDateEpochMs = latestDate,
                transactions = txList.sortedByDescending { it.transaction.dateEpochMs },
                percentageShare = share
            )
        }.filter { item ->
            val totalAmt = if (effectiveType == TransactionType.EXPENSE) item.totalExpense else if (effectiveType == TransactionType.INCOME) item.totalIncome else (item.totalExpense + item.totalIncome)
            val matchesSearch = searchQuery.isBlank() ||
                    item.name.contains(searchQuery, ignoreCase = true) ||
                    (item.groupName != null && item.groupName.contains(searchQuery, ignoreCase = true))
            val matchesMin = minAmount == null || totalAmt >= minAmount
            val matchesMax = maxAmount == null || totalAmt <= maxAmount
            val matchesZero = !excludeZero || totalAmt > 0

            matchesSearch && matchesMin && matchesMax && matchesZero
        }

        val sortedList = when (sortOrder) {
            AggregatedSortOrder.DEFAULT,
            AggregatedSortOrder.AMOUNT_DESC -> list.sortedByDescending { if (effectiveType == TransactionType.EXPENSE) it.totalExpense else if (effectiveType == TransactionType.INCOME) it.totalIncome else (it.totalExpense + it.totalIncome) }
            AggregatedSortOrder.AMOUNT_ASC -> list.sortedBy { if (effectiveType == TransactionType.EXPENSE) it.totalExpense else if (effectiveType == TransactionType.INCOME) it.totalIncome else (it.totalExpense + it.totalIncome) }
            AggregatedSortOrder.COUNT_DESC -> list.sortedByDescending { it.transactionCount }
            AggregatedSortOrder.COUNT_ASC -> list.sortedBy { it.transactionCount }
            AggregatedSortOrder.AVG_DESC -> list.sortedByDescending {
                val amt = if (effectiveType == TransactionType.EXPENSE) it.totalExpense else if (effectiveType == TransactionType.INCOME) it.totalIncome else (it.totalExpense + it.totalIncome)
                if (it.transactionCount > 0) amt / it.transactionCount else 0.0
            }
            AggregatedSortOrder.AVG_ASC -> list.sortedBy {
                val amt = if (effectiveType == TransactionType.EXPENSE) it.totalExpense else if (effectiveType == TransactionType.INCOME) it.totalIncome else (it.totalExpense + it.totalIncome)
                if (it.transactionCount > 0) amt / it.transactionCount else 0.0
            }
            AggregatedSortOrder.NAME_ASC -> list.sortedBy { it.name.lowercase() }
            AggregatedSortOrder.NAME_DESC -> list.sortedByDescending { it.name.lowercase() }
            AggregatedSortOrder.RECENT_DATE -> list.sortedByDescending { it.latestDateEpochMs }
        }

        // In ALL mode, ensure Expenses are displayed first, then Income!
        if (effectiveType == null) {
            sortedList.sortedBy { if (it.type == TransactionType.EXPENSE) 0 else 1 }
        } else {
            sortedList
        }
    }

    val totalExpenseOverall = remember(aggregatedItems) {
        aggregatedItems.filter { it.type == TransactionType.EXPENSE }.sumOf { it.totalExpense }
    }
    val totalIncomeOverall = remember(aggregatedItems) {
        aggregatedItems.filter { it.type == TransactionType.INCOME }.sumOf { it.totalIncome }
    }
    val totalFlowOverall = remember(aggregatedItems, activeTabMode, totalExpenseOverall, totalIncomeOverall) {
        when (activeTabMode) {
            ItemsFilterSpec.MODE_EXPENSE -> totalExpenseOverall
            ItemsFilterSpec.MODE_INCOME -> totalIncomeOverall
            else -> totalExpenseOverall + totalIncomeOverall
        }
    }

    val activeFilterSummary = remember(filterState, searchQuery, languageMode, itemsSpec) {
        val chips = itemsSpec.activeChips(filterState, languageMode)
        val summary = chips.joinToString(" • ") { it.label }
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
                title = LanguageHelper.getString("items_summary", languageMode),
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                searchPlaceholder = if (languageMode == LanguageMode.BANGLA) "আইটেম খুঁজুন..." else "Search items...",
                showSearchButton = true,
                showFilterButton = true,
                isFilterActive = itemsSpec.isActive(filterState),
                activeFilterCount = itemsSpec.activeFilterCount(filterState),
                onFilterClick = { showFilterDialog = true },
                showTimelineButton = true,
                onTimelineClick = { showTimelineScreen = true },
                onOpenDrawer = onOpenDrawer,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp),
                actions = {
                    ExportMenuButton(
                        languageMode = languageMode,
                        onExport = { format ->
                            TabExportHelper.exportItems(
                                context = context,
                                format = format,
                                filterSubtitle = activeFilterSummary,
                                items = aggregatedItems,
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
                        val titleText = when (activeTabMode) {
                            "EXPENSE" -> if (languageMode == LanguageMode.BANGLA) "মোট ব্যয় (Items Expense)" else "Total Items Expense"
                            "INCOME" -> if (languageMode == LanguageMode.BANGLA) "মোট আয় (Items Income)" else "Total Items Income"
                            else -> if (languageMode == LanguageMode.BANGLA) "সর্বমোট ব্যয় ও আয়" else "Total Expenses & Income"
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
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "আইটেম" else "Items",
                                    fontSize = 9.5.sp,
                                    color = SlateText
                                )
                                Text(
                                    text = LanguageHelper.formatNumber(aggregatedItems.size.toDouble(), languageMode, false),
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
                            val totalTxCount = aggregatedItems.sumOf { it.transactionCount }
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

            // Unified Active Filter Bar
            UnifiedActiveFilterBar(
                spec = itemsSpec,
                state = filterState,
                onFilterChange = { updateFilterState(it) },
                languageMode = languageMode,
                onOpenFilterDialog = { showFilterDialog = true },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
            )

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
                    contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 6.dp, bottom = 125.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    items(aggregatedItems, key = { it.id }) { item ->
                        AggregatedItemCard(
                            item = item,
                            languageMode = languageMode,
                            onClick = { selectedDrilldownItem = item }
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
                            .testTag("items_add_fab")
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
                                .clickable { updateFilterState(ItemsFilterSpec.withTypeMode(filterState, ItemsFilterSpec.MODE_EXPENSE)) }
                                .testTag("items_mode_expense")
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
                                .clickable { updateFilterState(ItemsFilterSpec.withTypeMode(filterState, ItemsFilterSpec.MODE_ALL)) }
                                .testTag("items_mode_all")
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
                                .clickable { updateFilterState(ItemsFilterSpec.withTypeMode(filterState, ItemsFilterSpec.MODE_INCOME)) }
                                .testTag("items_mode_income")
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
    selectedDrilldownItem?.let { item ->
        AlertDialog(
            onDismissRequest = { selectedDrilldownItem = null },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        val isImage = IconHelper.isDrawableIcon(item.iconName) || IconHelper.isCustomIcon(item.iconName)
                        val isExp = item.type == TransactionType.EXPENSE
                        val parsedItemColor = item.colorHex?.takeIf { it.isNotBlank() }?.let {
                            try { IconHelper.parseColorHex(it) } catch (_: Exception) { null }
                        } ?: if (isExp) CrimsonPink else SolidIncome

                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(7.dp))
                                .background(if (isImage) Color.Transparent else parsedItemColor.copy(alpha = 0.12f))
                                .border(
                                    width = 0.65.dp,
                                    color = if (isImage) Color.Transparent else parsedItemColor.copy(alpha = 0.30f),
                                    shape = RoundedCornerShape(7.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            IconHelper.AppIcon(
                                iconName = item.iconName,
                                fallbackName = item.name,
                                contentDescription = item.name,
                                tint = if (isImage) Color.Unspecified else parsedItemColor,
                                modifier = Modifier.size(if (isImage) 32.dp else 22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${item.transactionCount} transactions • Avg: ${LanguageHelper.formatCurrency((item.totalExpense + item.totalIncome) / item.transactionCount, languageMode)}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ExportMenuButton(
                            languageMode = languageMode,
                            onExport = { format ->
                                TabExportHelper.exportTransactions(
                                    context = context,
                                    format = format,
                                    transactions = item.transactions,
                                    filterSummary = "Item / Payee: ${item.name}",
                                    languageMode = languageMode
                                )
                            }
                        )
                        IconButton(onClick = { selectedDrilldownItem = null }) {
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
                                        text = DateUtils.formatDate(tx.dateEpochMs, languageMode),
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
                                val isPositiveEffect = when (tx.type) {
                                    TransactionType.EXPENSE -> tx.amount < 0
                                    TransactionType.INCOME -> tx.amount >= 0
                                    TransactionType.TRANSFER -> false
                                }
                                val sign = if (isPositiveEffect) "+" else "−"
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
                TextButton(onClick = { selectedDrilldownItem = null }) {
                    Text("Done")
                }
            }
        )
    }

    // Unified Filter & Sort Dialog
    if (showFilterDialog) {
        UnifiedFilterDialog(
            spec = itemsSpec,
            initialState = filterState,
            languageMode = languageMode,
            onDismiss = { showFilterDialog = false },
            onApply = { newFilter ->
                updateFilterState(newFilter)
                showFilterDialog = false
            },
            countProvider = { state ->
                val (sMs, eMs) = ItemsFilterSpec.resolveDateBounds(itemsSpec, state)
                val tMode = ItemsFilterSpec.getTypeModeId(state)
                val targetT = when (tMode) {
                    ItemsFilterSpec.MODE_EXPENSE -> TransactionType.EXPENSE
                    ItemsFilterSpec.MODE_INCOME -> TransactionType.INCOME
                    else -> null
                }
                val specT = ItemsFilterSpec.getTransactionType(state)
                val effT = specT ?: targetT
                val accIds = ItemsFilterSpec.getSelectedAccountIds(state)
                val catIds = ItemsFilterSpec.getSelectedCategoryIds(state)
                val st = ItemsFilterSpec.getSelectedStatuses(state)

                transactions.count { item ->
                    val tx = item.transaction
                    val matchesDate = tx.dateEpochMs in sMs..eMs
                    val matchesType = effT == null || tx.type == effT
                    val matchesAccount = accIds.isEmpty() ||
                            (tx.debitAccountId != null && tx.debitAccountId in accIds) ||
                            (tx.creditAccountId != null && tx.creditAccountId in accIds)
                    val matchesCategory = catIds.isEmpty() ||
                            (tx.categoryId != null && tx.categoryId in catIds) ||
                            (tx.subCategoryId != null && tx.subCategoryId in catIds)
                    val matchesStatus = st.isEmpty() || tx.status in st
                    matchesDate && matchesType && matchesAccount && matchesCategory && matchesStatus
                }
            }
        )
    }
}

@Composable
private fun AggregatedItemCard(
    item: AggregatedItem,
    languageMode: LanguageMode,
    onClick: () -> Unit
) {
    val isExpense = item.type == TransactionType.EXPENSE
    val isImage = IconHelper.isDrawableIcon(item.iconName) || IconHelper.isCustomIcon(item.iconName)
    val parsedItemColor = item.colorHex?.takeIf { it.isNotBlank() }?.let {
        try { IconHelper.parseColorHex(it) } catch (_: Exception) { null }
    } ?: if (isExpense) CrimsonPink else SolidIncome

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
                // Left Icon Badge (25dp matching budget tab)
                Box(
                    modifier = Modifier
                        .size(25.dp)
                        .clip(RoundedCornerShape(5.5.dp))
                        .background(if (isImage) Color.Transparent else parsedItemColor.copy(alpha = 0.10f))
                        .border(
                            width = 0.65.dp,
                            color = if (isImage) Color.Transparent else parsedItemColor.copy(alpha = 0.28f),
                            shape = RoundedCornerShape(5.5.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    IconHelper.AppIcon(
                        iconName = item.iconName,
                        fallbackName = item.name,
                        contentDescription = item.name,
                        tint = if (isImage) Color.Unspecified else parsedItemColor,
                        modifier = Modifier.size(if (isImage) 25.dp else 20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Middle Column: Item Name (13sp) + Subtitle + Percentage Badge (9sp)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(1.5.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val subtitle = buildString {
                            if (!item.groupName.isNullOrBlank()) {
                                append(item.groupName)
                                append(" • ")
                            }
                            append("${item.transactionCount} ${if (languageMode == LanguageMode.BANGLA) "টি লেনদেন" else "txs"}")
                        }
                        Text(
                            text = subtitle,
                            fontSize = 10.5.sp,
                            color = SlateText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        if (item.percentageShare > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
                                shape = RoundedCornerShape(3.5.dp)
                            ) {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "${item.percentageShare.toInt()}% মোট" else "${item.percentageShare.toInt()}% of total",
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
                val totalAmt = if (isExpense) item.totalExpense else item.totalIncome
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = LanguageHelper.formatCurrency(totalAmt, languageMode),
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isExpense) CrimsonPink else SolidIncome,
                        textAlign = TextAlign.End,
                        maxLines = 1
                    )
                    val avg = if (item.transactionCount > 0) totalAmt / item.transactionCount else 0.0
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


