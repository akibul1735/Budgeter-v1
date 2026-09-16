package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
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
import com.example.data.model.CategoryType
import com.example.data.model.LanguageMode
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.data.model.TransactionWithDetails
import com.example.data.repository.AccountWithBalance
import com.example.data.repository.FinancialOverview
import com.example.ui.components.AppTabHeader
import com.example.ui.components.AutoHidingBottomContainer
import com.example.ui.components.BudgetDateRangePreset
import com.example.ui.components.BudgetFilterDialog
import com.example.ui.components.BudgetFilterState
import com.example.ui.components.ExportMenuButton
import com.example.ui.components.LocalHeaderScrollState
import com.example.ui.components.calculateBudgetFilterRanges
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.util.DateUtils
import com.example.util.IconHelper
import com.example.util.LanguageHelper
import com.example.util.NetEarningsExportGroup
import com.example.util.NetEarningsExportItem
import com.example.util.TabExportHelper
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt

private val CrimsonPink = Color(0xFFE91E63)
private val BrandBlueLight = Color(0xFF0284C7)
private val SlateText = Color(0xFF64748B)

private enum class NetEarningsHierarchyView {
    GROUPED,
    ONLY_GROUPS,
    ONLY_ITEMS
}

private enum class NetEarningsSort {
    AMOUNT_HIGH_TO_LOW,
    AMOUNT_LOW_TO_HIGH,
    PERCENTAGE_HIGH_TO_LOW,
    NAME_A_TO_Z
}

data class CategoryEarningsTrackingItem(
    val category: Category,
    val actualAmount: Double,
    val transactions: List<TransactionWithDetails>,
    val percentageShare: Double = 0.0
) {
    val percentageInt: Int get() = percentageShare.roundToInt()
}

data class CategoryGroupEarningsTracking(
    val parentCategory: Category?,
    val groupNameEn: String,
    val groupNameBn: String,
    val items: List<CategoryEarningsTrackingItem>,
    val percentageShare: Double = 0.0
) {
    val totalAmount: Double get() = items.sumOf { it.actualAmount }
    val percentageInt: Int get() = percentageShare.roundToInt()

    fun localizedGroupName(mode: LanguageMode): String {
        return when (mode) {
            LanguageMode.ENGLISH -> groupNameEn
            LanguageMode.BANGLA -> if (groupNameBn.isNotBlank()) groupNameBn else groupNameEn
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    overview: FinancialOverview,
    accountsWithBalances: List<AccountWithBalance> = emptyList(),
    transactions: List<TransactionWithDetails> = emptyList(),
    categories: List<Category> = emptyList(),
    allAccounts: List<Account> = emptyList(),
    languageMode: LanguageMode,
    onOpenDrawer: () -> Unit = {},
    onAddTransactionWithCategory: ((Category) -> Unit)? = null,
    onEditTransaction: ((Transaction) -> Unit)? = null,
    onAccountClick: ((Account) -> Unit)? = null
) {
    val context = LocalContext.current

    // Default to THIS MONTH
    val currentCal = remember { Calendar.getInstance() }
    var selectedYear by remember { mutableIntStateOf(currentCal.get(Calendar.YEAR)) }
    var selectedMonth by remember { mutableIntStateOf(currentCal.get(Calendar.MONTH) + 1) } // 1-12

    var activeTabMode by remember { mutableStateOf("EXPENSE") } // "EXPENSE" or "INCOME"
    var hierarchyView by remember { mutableStateOf(NetEarningsHierarchyView.GROUPED) }
    var sortOption by remember { mutableStateOf(NetEarningsSort.AMOUNT_HIGH_TO_LOW) }
    var searchQuery by remember { mutableStateOf("") }
    var showTimelineScreen by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var filterState by remember { mutableStateOf(BudgetFilterState(datePreset = BudgetDateRangePreset.THIS_MONTH)) }

    // Expanded groups map (default expanded)
    val expandedGroups = remember { mutableStateMapOf<String, Boolean>() }

    // TIMELINE NAVIGATION
    if (showTimelineScreen) {
        val allTx = remember(transactions) { transactions.map { it.transaction } }
        CategoryTimelineScreen(
            categories = categories,
            transactions = allTx,
            languageMode = languageMode,
            onBack = { showTimelineScreen = false }
        )
        return
    }

    // Extract labels from transactions for filter dialog
    val allLabels = remember(transactions) {
        transactions.flatMap { details ->
            val note = details.transaction.note
            val hashTags = Regex("#([\\w\\d_-]+)").findAll(note).map { it.value }.toList()
            val commaSeparated = if (note.contains(",")) note.split(",").map { it.trim() }.filter { it.startsWith("#") || it.length in 2..20 } else emptyList()
            (hashTags + commaSeparated).filter { it.isNotBlank() }
        }.distinct().sorted()
    }

    // Determine current start and end milliseconds for selected month / filter
    val budgetRangeResult = remember(selectedYear, selectedMonth, filterState, languageMode) {
        calculateBudgetFilterRanges(selectedYear, selectedMonth, filterState, languageMode)
    }
    val monthStartMs = budgetRangeResult.primaryRange.first
    val monthEndMs = budgetRangeResult.primaryRange.second
    val monthLabel = budgetRangeResult.primaryLabel

    // Transactions for this month only with active filters
    val monthTransactions = remember(transactions, monthStartMs, monthEndMs, filterState) {
        transactions.filter { details ->
            val tx = details.transaction
            val inDate = tx.dateEpochMs in monthStartMs..monthEndMs
            if (!inDate) return@filter false

            // Category filter
            if (filterState.selectedCategoryIds.isNotEmpty()) {
                val catId = tx.categoryId ?: details.category?.id ?: details.subCategory?.id
                if (catId == null || catId !in filterState.selectedCategoryIds) return@filter false
            }

            // Account filter
            if (filterState.selectedAccountIds.isNotEmpty()) {
                val accId = details.debitAccount?.id ?: details.creditAccount?.id ?: tx.debitAccountId ?: tx.creditAccountId
                if (accId == null || accId !in filterState.selectedAccountIds) return@filter false
            }

            // Status filter
            if (filterState.selectedStatusSet.isNotEmpty()) {
                if (tx.status !in filterState.selectedStatusSet) return@filter false
            }

            // Exclude zero amounts
            if (filterState.excludeZeroAmounts && tx.amount <= 0.0) return@filter false

            true
        }
    }

    val totalMonthIncome = remember(monthTransactions) {
        monthTransactions
            .filter { it.transaction.type == TransactionType.INCOME }
            .sumOf { it.transaction.amount }
    }

    val totalMonthExpense = remember(monthTransactions) {
        monthTransactions
            .filter { it.transaction.type == TransactionType.EXPENSE }
            .sumOf { it.transaction.amount }
    }

    val netSavings = remember(totalMonthIncome, totalMonthExpense) {
        totalMonthIncome - totalMonthExpense
    }

    // Categories Breakdown calculation
    val targetType = if (activeTabMode == "EXPENSE") CategoryType.EXPENSE else CategoryType.INCOME
    val targetTxType = if (activeTabMode == "EXPENSE") TransactionType.EXPENSE else TransactionType.INCOME
    val totalFlowAmount = if (activeTabMode == "EXPENSE") totalMonthExpense else totalMonthIncome

    val categoryGroups = remember(
        categories,
        monthTransactions,
        targetType,
        totalFlowAmount,
        searchQuery,
        sortOption,
        languageMode
    ) {
        val relevantCategories = categories.filter { it.type == targetType }
        val parentCategories = relevantCategories.filter { it.parentId == null }
        val childCategories = relevantCategories.filter { it.parentId != null }

        val flowTransactions = monthTransactions.filter { it.transaction.type == targetTxType }

        val groups = parentCategories.map { parent ->
            val children = childCategories.filter { it.parentId == parent.id }
            val items = children.map { child ->
                val childTx = flowTransactions.filter {
                    it.transaction.categoryId == child.id ||
                            it.category?.id == child.id ||
                            it.subCategory?.id == child.id
                }
                val actualAmt = childTx.sumOf { it.transaction.amount }
                val share = if (totalFlowAmount > 0) (actualAmt / totalFlowAmount) * 100.0 else 0.0
                CategoryEarningsTrackingItem(
                    category = child,
                    actualAmount = actualAmt,
                    transactions = childTx,
                    percentageShare = share
                )
            }

            // Also check transactions directly assigned to parent group if any
            val directParentTx = flowTransactions.filter {
                (it.transaction.categoryId == parent.id || it.category?.id == parent.id) &&
                        it.subCategory == null
            }
            val parentDirectAmt = directParentTx.sumOf { it.transaction.amount }

            val allItems = if (parentDirectAmt > 0) {
                items + CategoryEarningsTrackingItem(
                    category = parent.copy(
                        nameEn = "${parent.nameEn} (General)",
                        nameBn = "${parent.nameBn} (সাধারণ)"
                    ),
                    actualAmount = parentDirectAmt,
                    transactions = directParentTx,
                    percentageShare = if (totalFlowAmount > 0) (parentDirectAmt / totalFlowAmount) * 100.0 else 0.0
                )
            } else items

            val groupTotal = allItems.sumOf { it.actualAmount }
            val groupShare = if (totalFlowAmount > 0) (groupTotal / totalFlowAmount) * 100.0 else 0.0

            CategoryGroupEarningsTracking(
                parentCategory = parent,
                groupNameEn = parent.nameEn,
                groupNameBn = parent.nameBn,
                items = allItems,
                percentageShare = groupShare
            )
        }

        // Catch transactions in categories without a recognized parent group (Uncategorized)
        val parentIds = parentCategories.map { it.id }.toSet()
        val childIds = childCategories.map { it.id }.toSet()
        val orphanTx = flowTransactions.filter { tx ->
            val catId = tx.transaction.categoryId ?: tx.category?.id
            catId == null || (catId !in parentIds && catId !in childIds)
        }

        val allGroupsWithOrphans = if (orphanTx.isNotEmpty()) {
            val orphanAmt = orphanTx.sumOf { it.transaction.amount }
            val orphanShare = if (totalFlowAmount > 0) (orphanAmt / totalFlowAmount) * 100.0 else 0.0
            val orphanCat = Category(
                id = -999L,
                nameEn = "Uncategorized",
                nameBn = "শ্রেণীবিহীন",
                type = targetType,
                colorHex = "#94A3B8",
                iconName = "Category"
            )
            groups + CategoryGroupEarningsTracking(
                parentCategory = orphanCat,
                groupNameEn = "Uncategorized",
                groupNameBn = "শ্রেণীবিহীন",
                items = listOf(
                    CategoryEarningsTrackingItem(
                        category = orphanCat,
                        actualAmount = orphanAmt,
                        transactions = orphanTx,
                        percentageShare = orphanShare
                    )
                ),
                percentageShare = orphanShare
            )
        } else groups

        // Filter by search query if any
        val filtered = if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase()
            allGroupsWithOrphans.mapNotNull { grp ->
                val groupMatches = grp.groupNameEn.lowercase().contains(q) || grp.groupNameBn.lowercase().contains(q)
                val matchingItems = grp.items.filter { item ->
                    item.category.localizedName(languageMode).lowercase().contains(q) ||
                            item.category.nameEn.lowercase().contains(q) ||
                            item.category.nameBn.lowercase().contains(q)
                }
                if (groupMatches || matchingItems.isNotEmpty()) {
                    grp.copy(items = if (groupMatches && matchingItems.isEmpty()) grp.items else matchingItems)
                } else null
            }
        } else {
            allGroupsWithOrphans
        }

        // Apply sorting
        when (sortOption) {
            NetEarningsSort.AMOUNT_HIGH_TO_LOW -> filtered.sortedByDescending { it.totalAmount }
            NetEarningsSort.AMOUNT_LOW_TO_HIGH -> filtered.sortedBy { it.totalAmount }
            NetEarningsSort.PERCENTAGE_HIGH_TO_LOW -> filtered.sortedByDescending { it.percentageShare }
            NetEarningsSort.NAME_A_TO_Z -> filtered.sortedBy { it.localizedGroupName(languageMode) }
        }
    }

    val listState = rememberLazyListState()
    val isScrolled by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("reports_screen")
    ) {
        // 1. TOP APP BAR HEADER: Includes Search, Filter, Timeline, Export button at top bar right side as Budget tab
        AppTabHeader(
            title = LanguageHelper.getString("net_earnings", languageMode),
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            searchPlaceholder = if (languageMode == LanguageMode.BANGLA) "ক্যাটাগরি খুঁজুন..." else "Search categories...",
            showSearchButton = true,
            showFilterButton = true,
            isFilterActive = filterState.isFilterActive,
            onFilterClick = { showFilterDialog = true },
            showTimelineButton = true,
            onTimelineClick = { showTimelineScreen = true },
            onOpenDrawer = onOpenDrawer,
            actions = {
                ExportMenuButton(
                    languageMode = languageMode,
                    onExport = { format ->
                        fun buildExportList(type: CategoryType): List<NetEarningsExportGroup> {
                            val relCats = categories.filter { it.type == type }
                            val pCats = relCats.filter { it.parentId == null }
                            val cCats = relCats.filter { it.parentId != null }
                            val txList = monthTransactions.filter {
                                it.transaction.type == if (type == CategoryType.EXPENSE) TransactionType.EXPENSE else TransactionType.INCOME
                            }
                            val flowTotal = txList.sumOf { it.transaction.amount }

                            return pCats.map { p ->
                                val ch = cCats.filter { it.parentId == p.id }
                                val itms = ch.map { c ->
                                    val ctx = txList.filter {
                                        it.transaction.categoryId == c.id || it.category?.id == c.id || it.subCategory?.id == c.id
                                    }
                                    val amt = ctx.sumOf { it.transaction.amount }
                                    NetEarningsExportItem(
                                        categoryNameEn = c.nameEn,
                                        categoryNameBn = c.nameBn,
                                        amount = amt,
                                        percentageShare = if (flowTotal > 0) (amt / flowTotal) * 100.0 else 0.0,
                                        txCount = ctx.size
                                    )
                                }
                                val grpAmt = itms.sumOf { it.amount }
                                NetEarningsExportGroup(
                                    groupNameEn = p.nameEn,
                                    groupNameBn = p.nameBn,
                                    totalAmount = grpAmt,
                                    percentageShare = if (flowTotal > 0) (grpAmt / flowTotal) * 100.0 else 0.0,
                                    items = itms
                                )
                            }
                        }

                        val expenseExport = buildExportList(CategoryType.EXPENSE)
                        val incomeExport = buildExportList(CategoryType.INCOME)

                        TabExportHelper.exportNetEarnings(
                            context = context,
                            format = format,
                            periodLabel = monthLabel,
                            expenseGroups = expenseExport,
                            totalExpense = totalMonthExpense,
                            incomeGroups = incomeExport,
                            totalIncome = totalMonthIncome,
                            netEarnings = netSavings,
                            languageMode = languageMode
                        )
                    }
                )
            }
        )

        // 2. SCROLL-AWARE TOP FIXED SUMMARY CARD (Almost same layout as budget tab)
        if (!isScrolled) {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f)),
                shadowElevation = 2.5.dp,
                tonalElevation = 1.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .animateContentSize(animationSpec = tween(durationMillis = 200))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    // Top Row: Month Navigation (< Month Name >)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            ) {
                                IconButton(
                                    onClick = {
                                        if (selectedMonth == 1) {
                                            selectedMonth = 12
                                            selectedYear -= 1
                                        } else {
                                            selectedMonth -= 1
                                        }
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Previous Month",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable {
                                            DatePickerDialog(
                                                context,
                                                { _, y, m, _ ->
                                                    selectedYear = y
                                                    selectedMonth = m + 1
                                                },
                                                selectedYear,
                                                selectedMonth - 1,
                                                1
                                            ).show()
                                        }
                                        .padding(horizontal = 6.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        tint = BrandBlueLight,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = monthLabel,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        if (selectedMonth == 12) {
                                            selectedMonth = 1
                                            selectedYear += 1
                                        } else {
                                            selectedMonth += 1
                                        }
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = "Next Month",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }

                        // Current active mode badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = (if (activeTabMode == "EXPENSE") CrimsonPink else SolidIncome).copy(alpha = 0.12f),
                            border = BorderStroke(0.5.dp, (if (activeTabMode == "EXPENSE") CrimsonPink else SolidIncome).copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = if (activeTabMode == "EXPENSE") {
                                    if (languageMode == LanguageMode.BANGLA) "ব্যয় সমূহ" else "Expenses"
                                } else {
                                    if (languageMode == LanguageMode.BANGLA) "আয় সমূহ" else "Incomes"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (activeTabMode == "EXPENSE") CrimsonPink else SolidIncome,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 3-Metric Row: Total Income | Total Expense | Net Earnings (Savings)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Metric 1: Total Inflows / Income
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "মোট আয়" else "Total Income",
                                fontSize = 10.sp,
                                color = SolidIncome,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                text = LanguageHelper.formatCurrency(totalMonthIncome, languageMode),
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = SolidIncome,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "মাসিক প্রাপ্তি" else "Inflows",
                                fontSize = 8.5.sp,
                                color = SlateText,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }

                        VerticalDivider(
                            modifier = Modifier
                                .height(34.dp)
                                .padding(horizontal = 2.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )

                        // Metric 2: Total Outflows / Expense
                        Column(
                            modifier = Modifier.weight(1.35f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "মোট ব্যয়" else "Total Expense",
                                fontSize = 10.sp,
                                color = CrimsonPink,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                text = LanguageHelper.formatCurrency(totalMonthExpense, languageMode),
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = CrimsonPink,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            val expenseRatioInt = if (totalMonthIncome > 0) ((totalMonthExpense / totalMonthIncome) * 100).roundToInt() else 0
                            Text(
                                text = if (totalMonthIncome > 0) "$expenseRatioInt% ${if (languageMode == LanguageMode.BANGLA) "আয়ের ব্যয়িত" else "of income"}"
                                else if (languageMode == LanguageMode.BANGLA) "মাসিক খরচ" else "Outflows",
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = SlateText,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }

                        VerticalDivider(
                            modifier = Modifier
                                .height(34.dp)
                                .padding(horizontal = 2.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )

                        // Metric 3: Net Earnings (Savings / Surplus)
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val isSurplus = netSavings >= 0
                            val metricColor = if (isSurplus) SolidIncome else SolidExpense
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "নেট আয় (উদ্বৃত্ত)" else "Net Earnings",
                                fontSize = 10.sp,
                                color = metricColor,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(1.dp))
                            val prefix = if (isSurplus && netSavings > 0) "+" else ""
                            Text(
                                text = "$prefix${LanguageHelper.formatCurrency(netSavings, languageMode)}",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = metricColor,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            val savingsRateInt = if (totalMonthIncome > 0) ((netSavings / totalMonthIncome) * 100).roundToInt() else 0
                            Text(
                                text = if (totalMonthIncome > 0) {
                                    if (isSurplus) "$savingsRateInt% ${if (languageMode == LanguageMode.BANGLA) "সঞ্চিত" else "savings rate"}"
                                    else "$savingsRateInt% ${if (languageMode == LanguageMode.BANGLA) "ঘাটতি" else "deficit"}"
                                } else {
                                    if (isSurplus) if (languageMode == LanguageMode.BANGLA) "উদ্বৃত্ত" else "Surplus"
                                    else if (languageMode == LanguageMode.BANGLA) "ঘাটতি" else "Deficit"
                                },
                                fontSize = 8.5.sp,
                                color = if (isSurplus) SlateText else SolidExpense,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // INSTEAD OF PROGRESS BAR: Clear Flow Distribution Breakdown Chips
                    val expenseShare = if (totalMonthIncome > 0) (totalMonthExpense / totalMonthIncome) * 100.0 else 0.0
                    val savingsShare = if (totalMonthIncome > 0) (netSavings / totalMonthIncome) * 100.0 else 0.0

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Expense share chip
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(CrimsonPink)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                val expText = String.format(Locale.US, "%.1f", expenseShare)
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA)
                                        "ব্যয়: ${LanguageHelper.toBanglaDigits(expText)}%"
                                    else
                                        "Expense: $expText%",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CrimsonPink
                                )
                            }

                            // Savings / Deficit share chip
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (netSavings >= 0) SolidIncome else SolidExpense)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                val savText = String.format(Locale.US, "%.1f", kotlin.math.abs(savingsShare))
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) {
                                        if (netSavings >= 0) "উদ্বৃত্ত: ${LanguageHelper.toBanglaDigits(savText)}%"
                                        else "ঘাটতি: ${LanguageHelper.toBanglaDigits(savText)}%"
                                    } else {
                                        if (netSavings >= 0) "Surplus: $savText%"
                                        else "Deficit: $savText%"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (netSavings >= 0) SolidIncome else SolidExpense
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. QUICK CONTROLS: HIERARCHY TOGGLE & SORT CHIPS
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Hierarchy pills
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(
                    NetEarningsHierarchyView.GROUPED to if (languageMode == LanguageMode.BANGLA) "গ্রুপ ও আইটেম" else "Grouped",
                    NetEarningsHierarchyView.ONLY_GROUPS to if (languageMode == LanguageMode.BANGLA) "শুধুমাত্র গ্রুপ" else "Groups",
                    NetEarningsHierarchyView.ONLY_ITEMS to if (languageMode == LanguageMode.BANGLA) "শুধুমাত্র আইটেম" else "Items"
                ).forEach { (mode, label) ->
                    val isSelected = hierarchyView == mode
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = BorderStroke(0.5.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .clickable { hierarchyView = mode }
                            .padding(vertical = 2.dp)
                    ) {
                        Text(
                            text = label,
                            fontSize = 10.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // Sort Pill
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                modifier = Modifier.clickable {
                    sortOption = when (sortOption) {
                        NetEarningsSort.AMOUNT_HIGH_TO_LOW -> NetEarningsSort.PERCENTAGE_HIGH_TO_LOW
                        NetEarningsSort.PERCENTAGE_HIGH_TO_LOW -> NetEarningsSort.AMOUNT_LOW_TO_HIGH
                        NetEarningsSort.AMOUNT_LOW_TO_HIGH -> NetEarningsSort.NAME_A_TO_Z
                        NetEarningsSort.NAME_A_TO_Z -> NetEarningsSort.AMOUNT_HIGH_TO_LOW
                    }
                }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Sort",
                        tint = SlateText,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = when (sortOption) {
                            NetEarningsSort.AMOUNT_HIGH_TO_LOW -> if (languageMode == LanguageMode.BANGLA) "সর্বোচ্চ অর্থ" else "High Amount"
                            NetEarningsSort.AMOUNT_LOW_TO_HIGH -> if (languageMode == LanguageMode.BANGLA) "সর্বনিম্ন অর্থ" else "Low Amount"
                            NetEarningsSort.PERCENTAGE_HIGH_TO_LOW -> if (languageMode == LanguageMode.BANGLA) "সর্বোচ্চ হার" else "Highest %"
                            NetEarningsSort.NAME_A_TO_Z -> if (languageMode == LanguageMode.BANGLA) "নামানুসারে" else "Name A-Z"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 4. MAIN CATEGORY & GROUP LIST (Actual Amounts Only, No Budget / Remaining)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (categoryGroups.isEmpty() || categoryGroups.all { it.items.isEmpty() }) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Category,
                                contentDescription = null,
                                tint = SlateText.copy(alpha = 0.6f),
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "এই মাসে কোনো লেনদেন নেই" else "No transactions for this month",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA)
                                    "নতুন লেনদেন যুক্ত করতে নিচের '+' বাটনে ট্যাপ করুন।"
                                else
                                    "Tap '+' button below to record actual income or expenses for this month.",
                                fontSize = 12.sp,
                                color = SlateText,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 125.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    when (hierarchyView) {
                        NetEarningsHierarchyView.ONLY_ITEMS -> {
                            // Flat list of all items
                            val allItems = categoryGroups.flatMap { it.items }.let { list ->
                                when (sortOption) {
                                    NetEarningsSort.AMOUNT_HIGH_TO_LOW -> list.sortedByDescending { it.actualAmount }
                                    NetEarningsSort.AMOUNT_LOW_TO_HIGH -> list.sortedBy { it.actualAmount }
                                    NetEarningsSort.PERCENTAGE_HIGH_TO_LOW -> list.sortedByDescending { it.percentageShare }
                                    NetEarningsSort.NAME_A_TO_Z -> list.sortedBy { it.category.localizedName(languageMode) }
                                }
                            }
                            items(allItems, key = { "item_${it.category.id}" }) { item ->
                                NetEarningsCategoryRow(
                                    item = item,
                                    languageMode = languageMode,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    onClick = {
                                        if (item.transactions.isNotEmpty() && onEditTransaction != null) {
                                            onEditTransaction(item.transactions.first().transaction)
                                        } else if (onAddTransactionWithCategory != null) {
                                            onAddTransactionWithCategory(item.category)
                                        }
                                    }
                                )
                            }
                        }

                        NetEarningsHierarchyView.ONLY_GROUPS -> {
                            // Only Group Cards
                            items(categoryGroups, key = { "group_${it.parentCategory?.id ?: it.groupNameEn}" }) { group ->
                                NetEarningsGroupSection(
                                    group = group,
                                    isExpanded = false,
                                    languageMode = languageMode,
                                    onToggleExpand = {},
                                    onCategoryClick = { item ->
                                        if (item.transactions.isNotEmpty() && onEditTransaction != null) {
                                            onEditTransaction(item.transactions.first().transaction)
                                        } else if (onAddTransactionWithCategory != null) {
                                            onAddTransactionWithCategory(item.category)
                                        }
                                    }
                                )
                            }
                        }

                        NetEarningsHierarchyView.GROUPED -> {
                            // Full Group with Child Items
                            items(categoryGroups, key = { "group_${it.parentCategory?.id ?: it.groupNameEn}" }) { group ->
                                val groupId = group.parentCategory?.id?.toString() ?: group.groupNameEn
                                val isExpanded = expandedGroups[groupId] ?: true

                                NetEarningsGroupSection(
                                    group = group,
                                    isExpanded = isExpanded,
                                    languageMode = languageMode,
                                    onToggleExpand = { expandedGroups[groupId] = !isExpanded },
                                    onCategoryClick = { item ->
                                        if (item.transactions.isNotEmpty() && onEditTransaction != null) {
                                            onEditTransaction(item.transactions.first().transaction)
                                        } else if (onAddTransactionWithCategory != null) {
                                            onAddTransactionWithCategory(item.category)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // 5. PINNED BOTTOM CONTAINER: AUTO-HIDING EXPENSE / INCOME TOGGLE + SINGLE FAB ABOVE IT
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
                    // Single FAB above the toggle, aligned to End (identical placement to Budget tab)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        FloatingActionButton(
                            onClick = {
                                val defaultCat = categories.firstOrNull { it.type == targetType && it.parentId != null }
                                if (defaultCat != null && onAddTransactionWithCategory != null) {
                                    onAddTransactionWithCategory(defaultCat)
                                }
                            },
                            containerColor = if (activeTabMode == "EXPENSE") Color(0xFF2563EB) else SolidIncome,
                            contentColor = Color.White,
                            shape = CircleShape,
                            modifier = Modifier
                                .size(52.dp)
                                .testTag("net_earnings_add_fab")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = if (languageMode == LanguageMode.BANGLA) "নতুন লেনদেন" else "Add Transaction",
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    // Segmented Toggle: [ Expenses (ব্যয়) | Incomes (আয়) ]
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
                                    .testTag("net_earnings_mode_expense")
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
                                    .testTag("net_earnings_mode_income")
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
    }

    // BudgetFilterDialog for advanced filtering
    if (showFilterDialog) {
        BudgetFilterDialog(
            currentFilter = filterState,
            categories = categories,
            accounts = allAccounts,
            allLabels = allLabels,
            languageMode = languageMode,
            onDismiss = { showFilterDialog = false },
            onApply = { newFilter ->
                filterState = newFilter
                showFilterDialog = false
            }
        )
    }
}

/**
 * CategoryGroupSection for Net Earnings:
 * Replicates the exact unified container, border, background, typography,
 * padding, chevron indicator, and collapsible child rows from BudgetTrackingScreen.
 * No progress bars are shown; only actual amount and percentage share.
 */
@Composable
private fun NetEarningsGroupSection(
    group: CategoryGroupEarningsTracking,
    isExpanded: Boolean,
    languageMode: LanguageMode,
    onToggleExpand: () -> Unit,
    onCategoryClick: (CategoryEarningsTrackingItem) -> Unit
) {
    val groupColor = remember(group.parentCategory?.colorHex) {
        try {
            if (group.parentCategory?.colorHex != null) {
                IconHelper.parseColorHex(group.parentCategory.colorHex)
            } else {
                Color(0xFF64748B)
            }
        } catch (_: Exception) {
            Color(0xFF64748B)
        }
    }

    val groupIcon = remember(group.parentCategory?.iconName) {
        if (group.parentCategory?.iconName != null) {
            IconHelper.getIconByName(group.parentCategory.iconName)
        } else {
            IconHelper.getIconByName("Folder")
        }
    }

    val isLight = MaterialTheme.colorScheme.surface.luminance() > 0.5f
    val containerBgColor = if (isLight) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.32f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f)
    }
    val lucrativeBorder = if (isLight) {
        BorderStroke(1.7.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
    } else {
        BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))
    }
    val headerBgColor = if (isLight) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f)
    }

    val containerShape = RoundedCornerShape(16.dp)
    Surface(
        color = containerBgColor,
        shape = containerShape,
        border = lucrativeBorder,
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 3.dp)
            .clip(containerShape)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Group Header Banner (Clickable to toggle expand)
            Surface(
                color = headerBgColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 6.dp, end = 10.dp, top = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left: Group Icon + Group Name (15sp Bold) + Group Percentages
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Group Icon Badge (28dp)
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(groupColor.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = groupIcon,
                                contentDescription = null,
                                tint = groupColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(9.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            // Top Line: Group Name (15sp Bold) + Chevron
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = LanguageHelper.getLocalizedName(group.groupNameEn, group.groupNameBn, languageMode),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )

                                Spacer(modifier = Modifier.width(4.dp))

                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                                    modifier = Modifier
                                        .size(17.dp)
                                        .rotate(if (isExpanded) 0f else -90f)
                                )
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            // Bottom Line: Group Percentage badge
                            if (group.percentageShare > 0) {
                                Surface(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "${group.percentageShare.toInt()}% মোট" else "${group.percentageShare.toInt()}% of total",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = SlateText,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Right: Group Total Amount (13.5sp SemiBold)
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = LanguageHelper.formatCurrency(group.totalAmount, languageMode),
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.End,
                            maxLines = 1
                        )
                    }
                }
            }

            // Collapsible Children Rows inside the unified container with border
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 14.dp, end = 6.dp, top = 4.dp, bottom = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    group.items.forEach { item ->
                        NetEarningsCategoryRow(
                            item = item,
                            languageMode = languageMode,
                            onClick = { onCategoryClick(item) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * CategoryRow for Net Earnings:
 * Replicates the exact child category row styling from BudgetTrackingScreen.
 * Shows: Icon Badge (32dp), Category Name (13sp), Percentage Badge (9sp), and Actual Amount (12sp).
 * No progress bars are shown.
 */
@Composable
private fun NetEarningsCategoryRow(
    item: CategoryEarningsTrackingItem,
    languageMode: LanguageMode,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val parsedColor = remember(item.category.colorHex) {
        try {
            IconHelper.parseColorHex(item.category.colorHex)
        } catch (_: Exception) {
            Color(0xFF3B82F6)
        }
    }

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
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 7.dp)
        ) {
            // Main Row: Left Icon Badge + Title/Percentage + Right Amount
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
                        .background(parsedColor.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = IconHelper.getIconByName(item.category.iconName),
                        contentDescription = null,
                        tint = parsedColor,
                        modifier = Modifier.size(17.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Middle Column: Category Name (13sp) + Percentage Badge (9sp)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = LanguageHelper.getLocalizedName(item.category.nameEn, item.category.nameBn, languageMode),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(1.5.dp))

                    if (item.percentageShare > 0) {
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

                Spacer(modifier = Modifier.width(6.dp))

                // Right Column: Actual Amount (12sp)
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = LanguageHelper.formatCurrency(item.actualAmount, languageMode),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.End,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
