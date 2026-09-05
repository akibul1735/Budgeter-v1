package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Account
import com.example.data.model.Category
import com.example.data.model.CategoryType
import com.example.data.model.LanguageMode
import com.example.data.model.MonthlyBudget
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.data.model.TransactionWithDetails
import com.example.data.repository.AccountWithBalance
import com.example.ui.components.AppTabHeader
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidPrimary
import com.example.ui.viewmodel.BudgetViewModel
import com.example.util.DateUtils
import com.example.util.IconHelper
import com.example.util.LanguageHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt

private val CrimsonPink = Color(0xFFE91E63)
private val AlertRed = Color(0xFFF43F5E)
private val BrandBlueLight = Color(0xFF0284C7)
private val DeepNavyBlue = Color(0xFF0369A1)
private val SoftCyan = Color(0xFF38BDF8)
private val SlateText = Color(0xFF64748B)

data class CategoryBudgetTrackingItem(
    val category: Category,
    val spentAmount: Double,
    val budgetLimit: Double,
    val isEnabled: Boolean,
    val transactions: List<TransactionWithDetails>
) {
    val hasBudget: Boolean get() = budgetLimit > 0 && isEnabled
    val progressRatio: Float get() = if (budgetLimit > 0) (spentAmount / budgetLimit).toFloat() else 0f
    val percentageInt: Int get() = if (budgetLimit > 0) ((spentAmount / budgetLimit) * 100).roundToInt() else 0
    val isOverBudget: Boolean get() = hasBudget && spentAmount > budgetLimit
    val diffAmount: Double get() = kotlin.math.abs(budgetLimit - spentAmount)
}

data class CategoryGroupBudgetTracking(
    val parentCategory: Category?,
    val groupNameEn: String,
    val groupNameBn: String,
    val items: List<CategoryBudgetTrackingItem>
) {
    val totalSpent: Double get() = items.sumOf { it.spentAmount }
    val totalBudget: Double get() = items.filter { it.hasBudget }.sumOf { it.budgetLimit }
    val hasBudget: Boolean get() = totalBudget > 0
    val progressRatio: Float get() = if (totalBudget > 0) (totalSpent / totalBudget).toFloat() else 0f
    val percentageInt: Int get() = if (totalBudget > 0) ((totalSpent / totalBudget) * 100).roundToInt() else 0
    val isOverBudget: Boolean get() = hasBudget && totalSpent > totalBudget
    val diffAmount: Double get() = kotlin.math.abs(totalBudget - totalSpent)
}

/**
 * Budget Screen
 * Displays tracking, progress bars with | TODAY marker, collapsible groups,
 * and category-level budget statuses as shown in the screenshot.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetTrackingScreen(
    viewModel: BudgetViewModel,
    allCategories: List<Category>,
    allAccounts: List<Account>,
    accountsWithBalances: List<AccountWithBalance>,
    transactionsWithDetails: List<TransactionWithDetails>,
    monthlyBudgets: List<MonthlyBudget>,
    selectedYear: Int,
    selectedMonth: Int,
    languageMode: LanguageMode,
    onOpenDrawer: () -> Unit = {},
    onNavigateToBudgetMaker: () -> Unit,
    onAddTransactionWithCategory: (Category) -> Unit,
    onEditTransaction: (Transaction) -> Unit
) {
    var showMonthPicker by remember { mutableStateOf(false) }
    var showTimelineMenu by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var filterOnlyBudgeted by remember { mutableStateOf(false) }
    var filterOnlyOverBudget by remember { mutableStateOf(false) }
    var selectedCategoryForDetail by remember { mutableStateOf<CategoryBudgetTrackingItem?>(null) }
    var activeTabMode by remember { mutableStateOf("EXPENSE") } // "EXPENSE" or "INCOME"

    // Group expanded state map (default true: expanded)
    val expandedGroups = remember { mutableStateMapOf<String, Boolean>() }

    // Calculate month boundary timestamps
    val startOfMonthMs = remember(selectedYear, selectedMonth) {
        DateUtils.getStartOfMonth(selectedYear, selectedMonth)
    }
    val endOfMonthMs = remember(selectedYear, selectedMonth) {
        DateUtils.getEndOfMonth(selectedYear, selectedMonth)
    }

    // Filter transactions for selected month
    val monthTransactions = remember(transactionsWithDetails, startOfMonthMs, endOfMonthMs) {
        transactionsWithDetails.filter { it.transaction.dateEpochMs in startOfMonthMs..endOfMonthMs }
    }

    // Map monthly budget items: "itemType_itemId" -> MonthlyBudget
    val budgetMap = remember(monthlyBudgets) {
        monthlyBudgets.associateBy { "${it.itemType}_${it.itemId}" }
    }

    // Format date range string: e.g. "9/1/26 - 9/30/26"
    val dateRangeString = remember(selectedYear, selectedMonth) {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, selectedYear)
        cal.set(Calendar.MONTH, selectedMonth - 1)
        val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val shortYear = selectedYear % 100
        "$selectedMonth/1/$shortYear - $selectedMonth/$maxDay/$shortYear"
    }

    // Calculate TODAY marker position (0.0 to 1.0)
    val todayPaceRatio = remember(selectedYear, selectedMonth) {
        val now = Calendar.getInstance()
        val currentYear = now.get(Calendar.YEAR)
        val currentMonth = now.get(Calendar.MONTH) + 1
        val currentDay = now.get(Calendar.DAY_OF_MONTH)

        when {
            selectedYear < currentYear || (selectedYear == currentYear && selectedMonth < currentMonth) -> 1.0f // Past month: 100%
            selectedYear > currentYear || (selectedYear == currentYear && selectedMonth > currentMonth) -> 0.0f // Future month: 0%
            else -> {
                val cal = Calendar.getInstance()
                cal.set(Calendar.YEAR, selectedYear)
                cal.set(Calendar.MONTH, selectedMonth - 1)
                val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                (currentDay.toFloat() / maxDays.toFloat()).coerceIn(0.01f, 0.99f)
            }
        }
    }

    // Parent categories & child items
    val parentCatMap = remember(allCategories) {
        allCategories.filter { it.parentId == null }.associateBy { it.id }
    }

    val targetCatType = if (activeTabMode == "EXPENSE") CategoryType.EXPENSE else CategoryType.INCOME
    val itemTypeKey = if (activeTabMode == "EXPENSE") "EXPENSE" else "INCOME"

    // Grouping calculations
    val categoryGroups = remember(
        allCategories,
        monthTransactions,
        budgetMap,
        parentCatMap,
        targetCatType,
        itemTypeKey,
        searchQuery,
        filterOnlyBudgeted,
        filterOnlyOverBudget
    ) {
        val relevantCategories = allCategories.filter { it.type == targetCatType }
        val parentCategories = relevantCategories.filter { it.parentId == null }
        val childCategories = relevantCategories.filter { it.parentId != null }

        // Group children by parentId
        val groupedByParent = childCategories.groupBy { it.parentId }

        val resultList = mutableListOf<CategoryGroupBudgetTracking>()

        // 1. Process regular parent categories with subcategories or standalone parent categories
        parentCategories.forEach { parent ->
            val children = groupedByParent[parent.id] ?: emptyList()
            if (children.isNotEmpty()) {
                val trackingItems = children.map { cat ->
                    val catTxs = monthTransactions.filter {
                        it.transaction.categoryId == cat.id || it.transaction.subCategoryId == cat.id
                    }
                    val spent = catTxs.sumOf { it.transaction.amount }
                    val budgetEntry = budgetMap["${itemTypeKey}_${cat.id}"]
                    val budgetLimit = budgetEntry?.budgetedAmount ?: cat.budgetLimit
                    val isEnabled = budgetEntry?.isEnabled ?: true

                    CategoryBudgetTrackingItem(
                        category = cat,
                        spentAmount = spent,
                        budgetLimit = budgetLimit,
                        isEnabled = isEnabled,
                        transactions = catTxs
                    )
                }.filter { item ->
                    val matchesSearch = searchQuery.isEmpty() ||
                            item.category.nameEn.contains(searchQuery, ignoreCase = true) ||
                            item.category.nameBn.contains(searchQuery, ignoreCase = true)
                    val matchesBudgeted = !filterOnlyBudgeted || item.hasBudget
                    val matchesOver = !filterOnlyOverBudget || item.isOverBudget
                    matchesSearch && matchesBudgeted && matchesOver
                }

                if (trackingItems.isNotEmpty() || (searchQuery.isEmpty() && !filterOnlyBudgeted && !filterOnlyOverBudget)) {
                    resultList.add(
                        CategoryGroupBudgetTracking(
                            parentCategory = parent,
                            groupNameEn = parent.nameEn,
                            groupNameBn = parent.nameBn,
                            items = trackingItems
                        )
                    )
                }
            } else {
                // Parent category with no sub-categories (standalone)
                val catTxs = monthTransactions.filter {
                    it.transaction.categoryId == parent.id || it.transaction.subCategoryId == parent.id
                }
                val spent = catTxs.sumOf { it.transaction.amount }
                val budgetEntry = budgetMap["${itemTypeKey}_${parent.id}"]
                val budgetLimit = budgetEntry?.budgetedAmount ?: parent.budgetLimit
                val isEnabled = budgetEntry?.isEnabled ?: true

                val singleItem = CategoryBudgetTrackingItem(
                    category = parent,
                    spentAmount = spent,
                    budgetLimit = budgetLimit,
                    isEnabled = isEnabled,
                    transactions = catTxs
                )

                val matchesSearch = searchQuery.isEmpty() ||
                        parent.nameEn.contains(searchQuery, ignoreCase = true) ||
                        parent.nameBn.contains(searchQuery, ignoreCase = true)
                val matchesBudgeted = !filterOnlyBudgeted || singleItem.hasBudget
                val matchesOver = !filterOnlyOverBudget || singleItem.isOverBudget

                if (matchesSearch && matchesBudgeted && matchesOver) {
                    resultList.add(
                        CategoryGroupBudgetTracking(
                            parentCategory = parent,
                            groupNameEn = parent.nameEn,
                            groupNameBn = parent.nameBn,
                            items = listOf(singleItem)
                        )
                    )
                }
            }
        }

        // 2. Process orphaned child categories whose parent is not in parentCategories
        val orphanedChildren = childCategories.filter { child -> parentCategories.none { it.id == child.parentId } }
        if (orphanedChildren.isNotEmpty()) {
            val orphanItems = orphanedChildren.map { cat ->
                val catTxs = monthTransactions.filter {
                    it.transaction.categoryId == cat.id || it.transaction.subCategoryId == cat.id
                }
                val spent = catTxs.sumOf { it.transaction.amount }
                val budgetEntry = budgetMap["${itemTypeKey}_${cat.id}"]
                val budgetLimit = budgetEntry?.budgetedAmount ?: cat.budgetLimit
                val isEnabled = budgetEntry?.isEnabled ?: true

                CategoryBudgetTrackingItem(
                    category = cat,
                    spentAmount = spent,
                    budgetLimit = budgetLimit,
                    isEnabled = isEnabled,
                    transactions = catTxs
                )
            }.filter { item ->
                val matchesSearch = searchQuery.isEmpty() ||
                        item.category.nameEn.contains(searchQuery, ignoreCase = true) ||
                        item.category.nameBn.contains(searchQuery, ignoreCase = true)
                val matchesBudgeted = !filterOnlyBudgeted || item.hasBudget
                val matchesOver = !filterOnlyOverBudget || item.isOverBudget
                matchesSearch && matchesBudgeted && matchesOver
            }

            if (orphanItems.isNotEmpty()) {
                resultList.add(
                    CategoryGroupBudgetTracking(
                        parentCategory = null,
                        groupNameEn = "Others",
                        groupNameBn = "অন্যান্য",
                        items = orphanItems
                    )
                )
            }
        }

        resultList
    }

    // Overall Totals
    val totalFlowSpent = remember(categoryGroups) {
        categoryGroups.sumOf { it.totalSpent }
    }
    val totalFlowBudget = remember(categoryGroups) {
        categoryGroups.sumOf { it.totalBudget }
    }
    val overallPercentage = remember(totalFlowSpent, totalFlowBudget) {
        if (totalFlowBudget > 0) ((totalFlowSpent / totalFlowBudget) * 100).roundToInt() else 0
    }
    val isOverallOver = totalFlowBudget > 0 && totalFlowSpent > totalFlowBudget
    val overallDiff = kotlin.math.abs(totalFlowBudget - totalFlowSpent)

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .testTag("budget_tracking_screen"),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // --- TAB HEADER ---
            item {
                AppTabHeader(
                    title = LanguageHelper.getString("budget", languageMode),
                    onOpenDrawer = onOpenDrawer
                )
            }

            // 1. TIMELINE & DATE RANGE HEADER
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: Timeline Chip
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showTimelineMenu = true }
                                .padding(vertical = 4.dp, horizontal = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                contentDescription = null,
                                tint = BrandBlueLight,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = LanguageHelper.getString("timeline", languageMode),
                                color = BrandBlueLight,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Right: Date Range with Month switchers
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IconButton(
                                onClick = { viewModel.prevBudgetMonth() },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Previous Month",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Text(
                                text = dateRangeString,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { showMonthPicker = true }
                                    .padding(horizontal = 4.dp, vertical = 4.dp)
                            )

                            IconButton(
                                onClick = { viewModel.nextBudgetMonth() },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Next Month",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 2. EXPENSE / INCOME SUMMARY CARD (Exactly matching screenshot hero banner)
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Title Row: EXPENSE (Blue) + Total Spent (Red/BDT)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (activeTabMode == "EXPENSE") "EXPENSE" else "INCOME",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = BrandBlueLight,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "BDT ${LanguageHelper.formatNumber(totalFlowSpent, languageMode)}",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (activeTabMode == "EXPENSE") CrimsonPink else SolidIncome
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Stats Row: Percentage (258%) + Difference status (BDT 6,225.00 over 3,950)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (totalFlowBudget > 0) "$overallPercentage%" else "0%",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            val diffText = if (totalFlowBudget > 0) {
                                if (isOverallOver) {
                                    "BDT ${LanguageHelper.formatNumber(overallDiff, languageMode)} over ${LanguageHelper.formatNumber(totalFlowBudget, languageMode, false)}"
                                } else {
                                    "BDT ${LanguageHelper.formatNumber(overallDiff, languageMode)} left from ${LanguageHelper.formatNumber(totalFlowBudget, languageMode, false)}"
                                }
                            } else {
                                "No budget configured"
                            }

                            Text(
                                text = diffText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isOverallOver) CrimsonPink else SlateText
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Full Width Continuous Progress Bar with TODAY Marker
                        BudgetProgressBarWithTodayMarker(
                            progressRatio = if (totalFlowBudget > 0) (totalFlowSpent / totalFlowBudget).toFloat() else 0f,
                            todayPaceRatio = todayPaceRatio,
                            isOverBudget = isOverallOver,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    thickness = 0.8.dp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            // 3. CATEGORY GROUPS & ITEMS (Collapsible Groups matching screenshot)
            if (categoryGroups.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp, bottom = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Assessment,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No categories or budgets found.",
                            color = MaterialTheme.colorScheme.outline,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = onNavigateToBudgetMaker,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Open Budget Maker")
                        }
                    }
                }
            } else {
                items(
                    items = categoryGroups,
                    key = { "${it.parentCategory?.id ?: "others"}_${it.groupNameEn}_${it.groupNameBn}" }
                ) { group ->
                    val groupKey = "${group.parentCategory?.id ?: "others"}_${group.groupNameEn}_${group.groupNameBn}"
                    val isExpanded = expandedGroups[groupKey] ?: true

                    CategoryGroupSection(
                        group = group,
                        isExpanded = isExpanded,
                        todayPaceRatio = todayPaceRatio,
                        languageMode = languageMode,
                        onToggleExpand = {
                            expandedGroups[groupKey] = !isExpanded
                        },
                        onCategoryClick = { trackingItem ->
                            selectedCategoryForDetail = trackingItem
                        }
                    )
                }
            }
        }

        // 4. FLOATING ACTION BUTTONS (Floating Shopping Bag / Budget Maker & Quick Add +)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Floating Budget Maker Shortcut Button
            Surface(
                shape = CircleShape,
                color = BrandBlueLight,
                shadowElevation = 6.dp,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .clickable { onNavigateToBudgetMaker() }
                    .testTag("budget_maker_fab")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.ShoppingBag,
                        contentDescription = "Budget Maker",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Main Add Transaction FAB
            FloatingActionButton(
                onClick = {
                    val defaultCat = allCategories.firstOrNull { it.type == targetCatType && it.parentId != null }
                    if (defaultCat != null) onAddTransactionWithCategory(defaultCat)
                    else onNavigateToBudgetMaker()
                },
                containerColor = Color(0xFF2563EB),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .size(56.dp)
                    .testTag("budget_add_tx_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction", modifier = Modifier.size(28.dp))
            }
        }
    }

    // DETAIL MODAL: Shows transactions in selected period for clicked category
    selectedCategoryForDetail?.let { detailItem ->
        CategoryTransactionsDetailDialog(
            item = detailItem,
            languageMode = languageMode,
            onDismiss = { selectedCategoryForDetail = null },
            onEditTransaction = { tx ->
                selectedCategoryForDetail = null
                onEditTransaction(tx)
            },
            onAddTransaction = {
                selectedCategoryForDetail = null
                onAddTransactionWithCategory(detailItem.category)
            },
            onOpenBudgetMaker = {
                selectedCategoryForDetail = null
                onNavigateToBudgetMaker()
            }
        )
    }

    // MONTH PICKER DIALOG
    if (showMonthPicker) {
        MonthPickerDropdownDialog(
            currentYear = selectedYear,
            currentMonth = selectedMonth,
            onDismiss = { showMonthPicker = false },
            onSelect = { y, m ->
                viewModel.setBudgetYearMonth(y, m)
                showMonthPicker = false
            }
        )
    }
}

/**
 * Category Group Section with Blue Chevron, Group Name, Total Spent,
 * optional Group-level progress bar, and collapsible child rows.
 */
@Composable
private fun CategoryGroupSection(
    group: CategoryGroupBudgetTracking,
    isExpanded: Boolean,
    todayPaceRatio: Float,
    languageMode: LanguageMode,
    onToggleExpand: () -> Unit,
    onCategoryClick: (CategoryBudgetTrackingItem) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        // Group Header Row
        Surface(
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleExpand() }
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left: Blue Circle Chevron + Arrow Symbol + Group Name
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        // Blue circular chevron toggle
                        Surface(
                            shape = CircleShape,
                            color = BrandBlueLight,
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                                contentDescription = if (isExpanded) "Collapse" else "Expand",
                                tint = Color.White,
                                modifier = Modifier.padding(2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Group Indicating Icon Badge
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(BrandBlueLight.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = IconHelper.getIconByName(group.parentCategory?.iconName ?: "Category"),
                                contentDescription = null,
                                tint = BrandBlueLight,
                                modifier = Modifier.size(15.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = LanguageHelper.getLocalizedName(group.groupNameEn, group.groupNameBn, languageMode),
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandBlueLight,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Right: Group Total Spent Amount
                    Text(
                        text = "BDT ${LanguageHelper.formatNumber(group.totalSpent, languageMode)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (group.totalSpent > 0) CrimsonPink else SlateText
                    )
                }

                // If group has an overall budget, show group-level progress bar and stats
                if (group.hasBudget) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${group.percentageInt}%",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        val diffText = if (group.isOverBudget) {
                            "BDT ${LanguageHelper.formatNumber(group.diffAmount, languageMode)} over ${LanguageHelper.formatNumber(group.totalBudget, languageMode, false)}"
                        } else {
                            "BDT ${LanguageHelper.formatNumber(group.diffAmount, languageMode)} left from ${LanguageHelper.formatNumber(group.totalBudget, languageMode, false)}"
                        }

                        Text(
                            text = diffText,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (group.isOverBudget) CrimsonPink else SlateText
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    BudgetProgressBarWithTodayMarker(
                        progressRatio = group.progressRatio,
                        todayPaceRatio = todayPaceRatio,
                        isOverBudget = group.isOverBudget,
                        barHeight = 5.dp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Collapsible Children Rows
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                group.items.forEach { item ->
                    CategoryRow(
                        item = item,
                        todayPaceRatio = todayPaceRatio,
                        languageMode = languageMode,
                        onClick = { onCategoryClick(item) }
                    )
                }
            }
        }
    }
}

/**
 * Individual Category Row matching screenshot:
 * Avatar icon (colored circle), category name, actual spent amount on right,
 * and if budgeted, % progress, remaining text, and progress bar with | TODAY marker.
 */
@Composable
private fun CategoryRow(
    item: CategoryBudgetTrackingItem,
    todayPaceRatio: Float,
    languageMode: LanguageMode,
    onClick: () -> Unit
) {
    val parsedColor = remember(item.category.colorHex) {
        try {
            IconHelper.parseColorHex(item.category.colorHex)
        } catch (_: Exception) {
            Color(0xFFE91E63)
        }
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 7.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Main Line: Category Icon + Name + Spent Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    // Circle Category Avatar
                    Surface(
                        shape = CircleShape,
                        color = parsedColor,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = IconHelper.getIconByName(item.category.iconName),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Category Name
                    Text(
                        text = LanguageHelper.getLocalizedName(item.category.nameEn, item.category.nameBn, languageMode),
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Spent Amount
                Text(
                    text = "BDT ${LanguageHelper.formatNumber(item.spentAmount, languageMode)}",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (item.spentAmount > 0) CrimsonPink else MaterialTheme.colorScheme.onSurface
                )
            }

            // Budget Sub-row (if category has an active budget limit)
            if (item.hasBudget) {
                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${item.percentageInt}%",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    val diffText = if (item.isOverBudget) {
                        "BDT ${LanguageHelper.formatNumber(item.diffAmount, languageMode)} over ${LanguageHelper.formatNumber(item.budgetLimit, languageMode, false)}"
                    } else {
                        "BDT ${LanguageHelper.formatNumber(item.diffAmount, languageMode)} left from ${LanguageHelper.formatNumber(item.budgetLimit, languageMode, false)}"
                    }

                    Text(
                        text = diffText,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (item.isOverBudget) CrimsonPink else SlateText
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                BudgetProgressBarWithTodayMarker(
                    progressRatio = item.progressRatio,
                    todayPaceRatio = todayPaceRatio,
                    isOverBudget = item.isOverBudget,
                    barHeight = 5.dp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Custom Budget Progress Bar with the "| TODAY" vertical tick marker.
 * Replicates the screenshot's dual-tone visual bar and pace indicator.
 */
@Composable
private fun BudgetProgressBarWithTodayMarker(
    progressRatio: Float,
    todayPaceRatio: Float,
    isOverBudget: Boolean,
    modifier: Modifier = Modifier,
    barHeight: androidx.compose.ui.unit.Dp = 6.dp
) {
    val barColor = if (isOverBudget) CrimsonPink else SoftCyan
    val clampedProgress = progressRatio.coerceIn(0f, 1f)

    Column(modifier = modifier) {
        // Track and Progress Fill
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
                .clip(RoundedCornerShape(3.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
        ) {
            val totalWidth = maxWidth
            if (clampedProgress > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(clampedProgress)
                        .height(barHeight)
                        .clip(RoundedCornerShape(3.dp))
                        .background(barColor)
                )
            }
        }

        // TODAY Marker Row underneath the progress bar
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(18.dp)
        ) {
            val totalWidth = maxWidth
            val markerFraction = todayPaceRatio.coerceIn(0.02f, 0.90f)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .offset(x = totalWidth * markerFraction)
            ) {
                // Vertical tick line
                Box(
                    modifier = Modifier
                        .width(1.5.dp)
                        .height(7.dp)
                        .background(BrandBlueLight)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "TODAY",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandBlueLight,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

/**
 * Dialog displaying all transactions recorded for a specific category in the selected period.
 */
@Composable
private fun CategoryTransactionsDetailDialog(
    item: CategoryBudgetTrackingItem,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onEditTransaction: (Transaction) -> Unit,
    onAddTransaction: () -> Unit,
    onOpenBudgetMaker: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = LanguageHelper.getLocalizedName(item.category.nameEn, item.category.nameBn, languageMode),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Total Spent: BDT ${LanguageHelper.formatNumber(item.spentAmount, languageMode)}",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CrimsonPink
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                if (item.hasBudget) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Budget: BDT ${LanguageHelper.formatNumber(item.budgetLimit, languageMode)}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${item.percentageInt}% used",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (item.isOverBudget) CrimsonPink else BrandBlueLight
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Transactions in this period (${item.transactions.size}):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.outline
                )

                Spacer(modifier = Modifier.height(6.dp))

                if (item.transactions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No transactions found in this period.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(item.transactions) { txDetails ->
                            val tx = txDetails.transaction
                            val dateStr = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(tx.dateEpochMs)

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onEditTransaction(tx) }
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (tx.note.isNotEmpty()) tx.note else "Expense",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = dateStr,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                    Text(
                                        text = LanguageHelper.formatCurrency(tx.amount, languageMode),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CrimsonPink
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onOpenBudgetMaker,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Set Budget", fontSize = 12.sp)
                    }
                    Button(
                        onClick = onAddTransaction,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlueLight),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("+ Add Entry", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

/**
 * Month and Year picker dialog for selecting active tracking period.
 */
@Composable
private fun MonthPickerDropdownDialog(
    currentYear: Int,
    currentMonth: Int,
    onDismiss: () -> Unit,
    onSelect: (Int, Int) -> Unit
) {
    var year by remember { mutableIntStateOf(currentYear) }
    val months = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { year-- }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Prev Year")
                    }
                    Text(
                        text = "$year",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { year++ }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Year")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 3x4 Month Grid
                for (row in 0 until 4) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (col in 0 until 3) {
                            val monthIdx = row * 3 + col
                            val monthNum = monthIdx + 1
                            val isSelected = year == currentYear && monthNum == currentMonth

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) BrandBlueLight else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onSelect(year, monthNum) }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = months[monthIdx].take(3),
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Close")
                }
            }
        }
    }
}
