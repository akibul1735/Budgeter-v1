package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Category
import com.example.data.model.CategoryType
import com.example.data.model.LanguageMode
import com.example.data.model.MonthlyBudget
import com.example.data.model.TransactionWithDetails
import com.example.ui.components.AppTabHeader
import com.example.ui.components.AutoHidingBottomContainer
import com.example.ui.components.LocalHeaderScrollState
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidPrimary
import com.example.util.IconHelper
import com.example.util.LanguageHelper

private val SlateText = Color(0xFF64748B)

enum class CategoryViewHierarchyFilter {
    ALL,
    ONLY_GROUPS,
    ONLY_CATEGORIES
}

enum class CategorySortFilter {
    DEFAULT,
    BUDGET_HIGH_TO_LOW,
    BUDGET_LOW_TO_HIGH,
    MOST_USED,
    LEAST_USED,
    NAME_AZ,
    NAME_ZA
}

data class FlattenedCategoryItem(
    val category: Category,
    val parentGroup: Category?,
    val budgetAmount: Double,
    val usageCount: Int
)

@Composable
fun CategoriesScreen(
    categories: List<Category>,
    languageMode: LanguageMode,
    initialTab: Int = 0,
    allTransactions: List<TransactionWithDetails> = emptyList(),
    monthlyBudgets: List<MonthlyBudget> = emptyList(),
    onOpenDrawer: () -> Unit = {},
    onAddCategoryClick: (CategoryType) -> Unit,
    onAddSubCategoryClick: (Category) -> Unit,
    onEditCategoryClick: (Category) -> Unit,
    onToggleActiveStatus: ((Category, Boolean) -> Unit)? = null,
    onUpdateCategories: ((List<Category>) -> Unit)? = null,
    onDeleteCategories: ((List<Category>) -> Unit)? = null
) {
    var isEditMode by remember { mutableStateOf(false) }
    var selectedTypeFilter by remember {
        mutableStateOf<CategoryType?>(
            if (initialTab == 1) CategoryType.INCOME else CategoryType.EXPENSE
        )
    }
    var hierarchyFilter by remember { mutableStateOf(CategoryViewHierarchyFilter.ALL) }
    var sortFilter by remember { mutableStateOf(CategorySortFilter.DEFAULT) }
    val expandedMap = remember { mutableStateMapOf<Long, Boolean>() }

    LaunchedEffect(initialTab) {
        selectedTypeFilter = if (initialTab == 1) CategoryType.INCOME else CategoryType.EXPENSE
    }

    // Usage Frequency Map from transactions
    val categoryUsageMap = remember(allTransactions) {
        val map = mutableMapOf<Long, Int>()
        allTransactions.forEach { txDetail ->
            val tx = txDetail.transaction
            tx.categoryId?.let { id -> map[id] = (map[id] ?: 0) + 1 }
            tx.subCategoryId?.let { id -> map[id] = (map[id] ?: 0) + 1 }
        }
        map
    }

    val categoryUsageCount: (Long) -> Int = { categoryId ->
        categoryUsageMap[categoryId] ?: 0
    }

    val activeCategories = remember(categories) {
        categories.filter { it.isActive }
    }

    val inactiveCategories = remember(categories) {
        categories.filter { !it.isActive }
    }

    // Calculations for Totals & Net Earnings
    val totalExpenseBudget = remember(activeCategories, monthlyBudgets) {
        val expenseLeafs = activeCategories.filter { it.type == CategoryType.EXPENSE && it.parentId != null }
            .ifEmpty { activeCategories.filter { it.type == CategoryType.EXPENSE } }
        expenseLeafs.sumOf { it.budgetLimit }
    }

    val totalIncomeBudget = remember(activeCategories, monthlyBudgets) {
        val incomeLeafs = activeCategories.filter { it.type == CategoryType.INCOME && it.parentId != null }
            .ifEmpty { activeCategories.filter { it.type == CategoryType.INCOME } }
        incomeLeafs.sumOf { it.budgetLimit }
    }

    val netEarnings = totalIncomeBudget - totalExpenseBudget

    // Group calculation helpers
    val subCategoriesMap = remember(categories) {
        categories.filter { it.parentId != null }.groupBy { it.parentId!! }
    }

    fun computeEffectiveCategoryBudget(cat: Category): Double {
        val subs = subCategoriesMap[cat.id] ?: emptyList()
        return if (subs.isNotEmpty()) {
            subs.filter { it.isActive }.sumOf { it.budgetLimit }
        } else {
            cat.budgetLimit
        }
    }

    fun groupUsageCount(groupCat: Category): Int {
        val subs = subCategoriesMap[groupCat.id] ?: emptyList()
        return (categoryUsageMap[groupCat.id] ?: 0) + subs.sumOf { categoryUsageMap[it.id] ?: 0 }
    }

    // Sorting logic
    fun sortGroups(list: List<Category>): List<Category> {
        return when (sortFilter) {
            CategorySortFilter.DEFAULT -> list
            CategorySortFilter.BUDGET_HIGH_TO_LOW -> list.sortedByDescending { computeEffectiveCategoryBudget(it) }
            CategorySortFilter.BUDGET_LOW_TO_HIGH -> list.sortedBy { computeEffectiveCategoryBudget(it) }
            CategorySortFilter.MOST_USED -> list.sortedByDescending { groupUsageCount(it) }
            CategorySortFilter.LEAST_USED -> list.sortedBy { groupUsageCount(it) }
            CategorySortFilter.NAME_AZ -> list.sortedBy { it.localizedName(languageMode).lowercase() }
            CategorySortFilter.NAME_ZA -> list.sortedByDescending { it.localizedName(languageMode).lowercase() }
        }
    }

    fun sortSubCategories(list: List<Category>): List<Category> {
        return when (sortFilter) {
            CategorySortFilter.DEFAULT -> list
            CategorySortFilter.BUDGET_HIGH_TO_LOW -> list.sortedByDescending { it.budgetLimit }
            CategorySortFilter.BUDGET_LOW_TO_HIGH -> list.sortedBy { it.budgetLimit }
            CategorySortFilter.MOST_USED -> list.sortedByDescending { categoryUsageCount(it.id) }
            CategorySortFilter.LEAST_USED -> list.sortedBy { categoryUsageCount(it.id) }
            CategorySortFilter.NAME_AZ -> list.sortedBy { it.localizedName(languageMode).lowercase() }
            CategorySortFilter.NAME_ZA -> list.sortedByDescending { it.localizedName(languageMode).lowercase() }
        }
    }

    val parentActiveCategories = remember(activeCategories, selectedTypeFilter, sortFilter, allTransactions) {
        val base = activeCategories.filter { it.parentId == null && (selectedTypeFilter == null || it.type == selectedTypeFilter) }
        sortGroups(base)
    }

    val parentInactiveCategories = remember(inactiveCategories, selectedTypeFilter, sortFilter, allTransactions) {
        val base = inactiveCategories.filter { it.parentId == null && (selectedTypeFilter == null || it.type == selectedTypeFilter) }
        sortGroups(base)
    }

    // Flattened Categories for ONLY_CATEGORIES mode
    val flattenedActiveCategories = remember(activeCategories, selectedTypeFilter, sortFilter, allTransactions) {
        val items = mutableListOf<FlattenedCategoryItem>()
        val filteredParents = activeCategories.filter { it.parentId == null && (selectedTypeFilter == null || it.type == selectedTypeFilter) }
        for (parent in filteredParents) {
            val subs = (subCategoriesMap[parent.id] ?: emptyList()).filter { it.isActive }
            if (subs.isEmpty()) {
                items.add(
                    FlattenedCategoryItem(
                        category = parent,
                        parentGroup = null,
                        budgetAmount = parent.budgetLimit,
                        usageCount = categoryUsageCount(parent.id)
                    )
                )
            } else {
                subs.forEach { sub ->
                    items.add(
                        FlattenedCategoryItem(
                            category = sub,
                            parentGroup = parent,
                            budgetAmount = sub.budgetLimit,
                            usageCount = categoryUsageCount(sub.id)
                        )
                    )
                }
            }
        }

        when (sortFilter) {
            CategorySortFilter.DEFAULT -> items
            CategorySortFilter.BUDGET_HIGH_TO_LOW -> items.sortedByDescending { it.budgetAmount }
            CategorySortFilter.BUDGET_LOW_TO_HIGH -> items.sortedBy { it.budgetAmount }
            CategorySortFilter.MOST_USED -> items.sortedByDescending { it.usageCount }
            CategorySortFilter.LEAST_USED -> items.sortedBy { it.usageCount }
            CategorySortFilter.NAME_AZ -> items.sortedBy { it.category.localizedName(languageMode).lowercase() }
            CategorySortFilter.NAME_ZA -> items.sortedByDescending { it.category.localizedName(languageMode).lowercase() }
        }
    }

    val flattenedInactiveCategories = remember(inactiveCategories, selectedTypeFilter, sortFilter, allTransactions) {
        val items = mutableListOf<FlattenedCategoryItem>()
        val filteredInactive = inactiveCategories.filter { selectedTypeFilter == null || it.type == selectedTypeFilter }
        for (cat in filteredInactive) {
            val parent = cat.parentId?.let { pId -> categories.firstOrNull { it.id == pId } }
            items.add(
                FlattenedCategoryItem(
                    category = cat,
                    parentGroup = parent,
                    budgetAmount = cat.budgetLimit,
                    usageCount = categoryUsageCount(cat.id)
                )
            )
        }
        items
    }

    // Scroll state & Bottom Nav visibility
    val listState = rememberLazyListState()
    var isBottomNavVisible by remember { mutableStateOf(true) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y < -15f) {
                    isBottomNavVisible = false
                } else if (available.y > 15f) {
                    isBottomNavVisible = true
                }
                return Offset.Zero
            }
        }
    }

    val currentAddType = selectedTypeFilter ?: CategoryType.EXPENSE

    Box(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection)
            .testTag("categories_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            AppTabHeader(
                title = LanguageHelper.getString("categories", languageMode),
                onOpenDrawer = onOpenDrawer
            )

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 125.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // --- 1. Compact Net Earnings Top Card ---
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("categories_summary_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SolidPrimary)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp)
                        ) {
                            // Top Row: Net Earnings & Edit Mode Toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = LanguageHelper.getString("net_earnings", languageMode),
                                        color = Color.White.copy(alpha = 0.9f),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = LanguageHelper.formatCurrency(netEarnings, languageMode),
                                        color = Color.White,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }

                                // Edit Mode Button on Top Card (activates/deactivates edit mode)
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isEditMode) Color.White else Color.White.copy(alpha = 0.18f),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = if (isEditMode) 0.9f else 0.4f)),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { isEditMode = !isEditMode }
                                        .testTag("categories_edit_mode_btn")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isEditMode) Icons.Default.Check else Icons.Default.Edit,
                                            contentDescription = "Edit Mode",
                                            tint = if (isEditMode) SolidPrimary else Color.White,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = if (isEditMode) (if (languageMode == LanguageMode.BANGLA) "সম্পন্ন" else "Done")
                                            else (if (languageMode == LanguageMode.BANGLA) "সম্পাদন" else "Edit"),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isEditMode) SolidPrimary else Color.White
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Middle Compact Row: Total Incomes vs Total Expenses
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.Black.copy(alpha = 0.2f))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Total Incomes
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(7.dp)
                                                .clip(CircleShape)
                                                .background(SolidIncome)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = LanguageHelper.getString("incomes", languageMode),
                                            color = Color.White.copy(alpha = 0.9f),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(1.dp))
                                    Text(
                                        text = LanguageHelper.formatCurrency(totalIncomeBudget, languageMode),
                                        color = SolidIncome,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                // Divider
                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(26.dp)
                                        .background(Color.White.copy(alpha = 0.2f))
                                )

                                // Total Expenses
                                Column(horizontalAlignment = Alignment.End) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = LanguageHelper.getString("expenses", languageMode),
                                            color = Color.White.copy(alpha = 0.9f),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(7.dp)
                                                .clip(CircleShape)
                                                .background(SolidExpense)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(1.dp))
                                    Text(
                                        text = LanguageHelper.formatCurrency(totalExpenseBudget, languageMode),
                                        color = SolidExpense,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Bottom Controls: Hierarchy Scope Pill & Mini Sort Filter
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // View Scope Selector (All | Only Groups | Only Categories)
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.Black.copy(alpha = 0.22f))
                                        .padding(2.dp),
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    ScopePill(
                                        selected = hierarchyFilter == CategoryViewHierarchyFilter.ALL,
                                        label = if (languageMode == LanguageMode.BANGLA) "সব" else "All",
                                        onClick = { hierarchyFilter = CategoryViewHierarchyFilter.ALL }
                                    )
                                    ScopePill(
                                        selected = hierarchyFilter == CategoryViewHierarchyFilter.ONLY_GROUPS,
                                        label = if (languageMode == LanguageMode.BANGLA) "শুধুমাত্র গ্রুপ" else "Only Groups",
                                        onClick = { hierarchyFilter = CategoryViewHierarchyFilter.ONLY_GROUPS }
                                    )
                                    ScopePill(
                                        selected = hierarchyFilter == CategoryViewHierarchyFilter.ONLY_CATEGORIES,
                                        label = if (languageMode == LanguageMode.BANGLA) "ক্যাটাগরি" else "Categories",
                                        onClick = { hierarchyFilter = CategoryViewHierarchyFilter.ONLY_CATEGORIES }
                                    )
                                }

                                // Mini Sort Filter
                                Box {
                                    var showFilterMenu by remember { mutableStateOf(false) }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (sortFilter != CategorySortFilter.DEFAULT) Color.White else Color.Black.copy(alpha = 0.22f),
                                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { showFilterMenu = true }
                                            .testTag("categories_sort_btn")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Sort,
                                                contentDescription = "Sort",
                                                tint = if (sortFilter != CategorySortFilter.DEFAULT) SolidPrimary else Color.White,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Text(
                                                text = getCategorySortLabel(sortFilter, languageMode),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (sortFilter != CategorySortFilter.DEFAULT) SolidPrimary else Color.White
                                            )
                                        }
                                    }

                                    DropdownMenu(
                                        expanded = showFilterMenu,
                                        onDismissRequest = { showFilterMenu = false }
                                    ) {
                                        CategorySortFilter.values().forEach { filter ->
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        text = getCategorySortMenuLabel(filter, languageMode),
                                                        fontSize = 12.sp,
                                                        fontWeight = if (sortFilter == filter) FontWeight.Bold else FontWeight.Normal
                                                    )
                                                },
                                                leadingIcon = {
                                                    if (sortFilter == filter) {
                                                        Icon(Icons.Default.Check, contentDescription = null, tint = SolidPrimary, modifier = Modifier.size(16.dp))
                                                    }
                                                },
                                                onClick = {
                                                    sortFilter = filter
                                                    showFilterMenu = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // --- 2. Categories List Based on Hierarchy Scope ---
                if (hierarchyFilter == CategoryViewHierarchyFilter.ONLY_CATEGORIES) {
                    // Flattened Single Categories Mode
                    if (flattenedActiveCategories.isEmpty() && flattenedInactiveCategories.isEmpty()) {
                        item {
                            EmptyCategoriesCard(languageMode)
                        }
                    } else {
                        items(flattenedActiveCategories, key = { "flat_${it.category.id}" }) { flatItem ->
                            SingleCategoryCard(
                                item = flatItem,
                                isEditMode = isEditMode,
                                languageMode = languageMode,
                                onEditCategory = onEditCategoryClick,
                                onToggleActiveStatus = onToggleActiveStatus
                            )
                        }

                        if (flattenedInactiveCategories.isNotEmpty()) {
                            item {
                                InactiveCategoryHeader(count = flattenedInactiveCategories.size)
                            }
                            items(flattenedInactiveCategories, key = { "flat_inactive_${it.category.id}" }) { flatItem ->
                                SingleCategoryCard(
                                    item = flatItem,
                                    isEditMode = isEditMode,
                                    isInactive = true,
                                    languageMode = languageMode,
                                    onEditCategory = onEditCategoryClick,
                                    onToggleActiveStatus = onToggleActiveStatus
                                )
                            }
                        }
                    }
                } else {
                    // ALL (Group + Sub-categories) or ONLY_GROUPS
                    val isOnlyGroups = hierarchyFilter == CategoryViewHierarchyFilter.ONLY_GROUPS
                    if (parentActiveCategories.isEmpty() && parentInactiveCategories.isEmpty()) {
                        item {
                            EmptyCategoriesCard(languageMode)
                        }
                    } else {
                        items(parentActiveCategories, key = { it.id }) { parent ->
                            val subs = (subCategoriesMap[parent.id] ?: emptyList()).filter { it.isActive }
                            val sortedSubs = sortSubCategories(subs)
                            val isExpanded = expandedMap[parent.id] ?: true

                            val parentColor = remember(parent.colorHex) {
                                try {
                                    Color(android.graphics.Color.parseColor(parent.colorHex))
                                } catch (_: Exception) {
                                    if (parent.type == CategoryType.EXPENSE) SolidExpense else SolidIncome
                                }
                            }

                            val groupBudget = if (subs.isNotEmpty()) subs.sumOf { it.budgetLimit } else parent.budgetLimit

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        if (subs.isNotEmpty() && !isOnlyGroups) {
                                            expandedMap[parent.id] = !isExpanded
                                        } else {
                                            onEditCategoryClick(parent)
                                        }
                                    },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    // Parent Group Header
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(38.dp)
                                                    .clip(CircleShape)
                                                    .background(parentColor.copy(alpha = 0.16f))
                                                    .border(1.dp, parentColor.copy(alpha = 0.35f), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = IconHelper.getIconByName(parent.iconName),
                                                    contentDescription = null,
                                                    tint = parentColor,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(10.dp))

                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text(
                                                        text = parent.localizedName(languageMode),
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        modifier = Modifier.weight(1f, fill = false)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Surface(
                                                        shape = RoundedCornerShape(4.dp),
                                                        color = parentColor.copy(alpha = 0.12f)
                                                    ) {
                                                        Text(
                                                            text = if (parent.type == CategoryType.EXPENSE) "Expense Group" else "Income Group",
                                                            fontSize = 8.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = parentColor,
                                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                        )
                                                    }
                                                }

                                                if (subs.isNotEmpty()) {
                                                    Text(
                                                        text = "${subs.size} ${LanguageHelper.getString("sub_categories", languageMode)}",
                                                        fontSize = 10.sp,
                                                        color = MaterialTheme.colorScheme.outline
                                                    )
                                                }
                                            }
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (groupBudget > 0) {
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                                                ) {
                                                    Text(
                                                        text = "Limit: ${LanguageHelper.formatCurrency(groupBudget, languageMode)}",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }

                                            if (subs.isNotEmpty() && !isOnlyGroups) {
                                                IconButton(
                                                    onClick = { expandedMap[parent.id] = !isExpanded },
                                                    modifier = Modifier.size(30.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                        contentDescription = "Expand",
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }

                                            // Edit Button: HIDDEN BY DEFAULT, VISIBLE ONLY IN EDIT MODE
                                            if (isEditMode) {
                                                IconButton(
                                                    onClick = { onEditCategoryClick(parent) },
                                                    modifier = Modifier.size(30.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Edit,
                                                        contentDescription = "Edit",
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Sub-categories list (hidden if isOnlyGroups)
                                    if (!isOnlyGroups) {
                                        AnimatedVisibility(visible = isExpanded && sortedSubs.isNotEmpty()) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(top = 8.dp, start = 8.dp)
                                            ) {
                                                sortedSubs.forEach { subCat ->
                                                    val subColor = remember(subCat.colorHex) {
                                                        try {
                                                            Color(android.graphics.Color.parseColor(subCat.colorHex))
                                                        } catch (_: Exception) {
                                                            parentColor
                                                        }
                                                    }

                                                    Surface(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(vertical = 2.dp)
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .clickable { onEditCategoryClick(subCat) },
                                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                                    ) {
                                                        Row(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .padding(horizontal = 8.dp, vertical = 7.dp),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.SpaceBetween
                                                        ) {
                                                            Row(
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                modifier = Modifier.weight(1f)
                                                            ) {
                                                                Box(
                                                                    modifier = Modifier
                                                                        .size(24.dp)
                                                                        .clip(CircleShape)
                                                                        .background(subColor.copy(alpha = 0.18f)),
                                                                    contentAlignment = Alignment.Center
                                                                ) {
                                                                    Icon(
                                                                        imageVector = IconHelper.getIconByName(subCat.iconName),
                                                                        contentDescription = null,
                                                                        tint = subColor,
                                                                        modifier = Modifier.size(14.dp)
                                                                    )
                                                                }
                                                                Spacer(modifier = Modifier.width(8.dp))
                                                                Text(
                                                                    text = subCat.localizedName(languageMode),
                                                                    fontSize = 12.sp,
                                                                    fontWeight = FontWeight.Medium,
                                                                    maxLines = 1,
                                                                    overflow = TextOverflow.Ellipsis,
                                                                    modifier = Modifier.weight(1f, fill = false)
                                                                )
                                                            }

                                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                                if (subCat.budgetLimit > 0) {
                                                                    Text(
                                                                        text = LanguageHelper.formatCurrency(subCat.budgetLimit, languageMode),
                                                                        fontSize = 11.sp,
                                                                        fontWeight = FontWeight.SemiBold,
                                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                                    )
                                                                    Spacer(modifier = Modifier.width(6.dp))
                                                                }

                                                                // Edit button: HIDDEN BY DEFAULT, VISIBLE ONLY IN EDIT MODE
                                                                if (isEditMode) {
                                                                    Icon(
                                                                        imageVector = Icons.Default.Edit,
                                                                        contentDescription = "Edit",
                                                                        tint = MaterialTheme.colorScheme.primary,
                                                                        modifier = Modifier
                                                                            .size(15.dp)
                                                                            .clickable { onEditCategoryClick(subCat) }
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }
                                                }

                                                // Quick Add Sub-Category button
                                                Text(
                                                    text = "+ ${LanguageHelper.getString("add_sub_category", languageMode)}",
                                                    fontSize = 11.sp,
                                                    color = SolidPrimary,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier
                                                        .clickable { onAddSubCategoryClick(parent) }
                                                        .padding(vertical = 6.dp, horizontal = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Inactive Categories Section
                        if (parentInactiveCategories.isNotEmpty()) {
                            item {
                                InactiveCategoryHeader(count = parentInactiveCategories.size)
                            }
                            items(parentInactiveCategories, key = { "inactive_${it.id}" }) { parent ->
                                val subs = (subCategoriesMap[parent.id] ?: emptyList())
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { onEditCategoryClick(parent) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                            Icon(
                                                imageVector = IconHelper.getIconByName(parent.iconName),
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.outline,
                                                modifier = Modifier.size(22.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = parent.localizedName(languageMode),
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = MaterialTheme.colorScheme.outline
                                                )
                                                if (subs.isNotEmpty()) {
                                                    Text(
                                                        text = "${subs.size} sub-categories (Inactive)",
                                                        fontSize = 10.sp,
                                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
                                                    )
                                                }
                                            }
                                        }

                                        if (isEditMode) {
                                            IconButton(
                                                onClick = { onEditCategoryClick(parent) },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = "Edit",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 3. BOTTOM PINNED CONTAINER: EXPENSE / ALL / INCOME TOGGLE + FAB ABOVE NAVIGATION TABS ---
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
                // FAB button on the right above the toggle
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    val addType = selectedTypeFilter ?: CategoryType.EXPENSE
                    FloatingActionButton(
                        onClick = { onAddCategoryClick(addType) },
                        containerColor = if (addType == CategoryType.EXPENSE) SolidExpense else SolidIncome,
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier
                            .size(52.dp)
                            .testTag("categories_fab_add")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Category",
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                // Segmented Toggle: [ Expenses (ব্যয়) | All (সব) | Incomes (আয়) ]
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
                        // Expenses Button
                        val isExpense = selectedTypeFilter == CategoryType.EXPENSE
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isExpense) SolidExpense.copy(alpha = 0.16f) else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { selectedTypeFilter = CategoryType.EXPENSE }
                                .testTag("cat_filter_expenses")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TrendingDown,
                                    contentDescription = null,
                                    tint = if (isExpense) SolidExpense else SlateText,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = LanguageHelper.getString("expenses", languageMode),
                                    fontSize = 12.sp,
                                    fontWeight = if (isExpense) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isExpense) SolidExpense else SlateText
                                )
                            }
                        }

                        // All Button
                        val isAll = selectedTypeFilter == null
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isAll) SolidPrimary.copy(alpha = 0.16f) else Color.Transparent,
                            modifier = Modifier
                                .weight(0.85f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { selectedTypeFilter = null }
                                .testTag("cat_filter_all")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Layers,
                                    contentDescription = null,
                                    tint = if (isAll) SolidPrimary else SlateText,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "সব" else "All",
                                    fontSize = 12.sp,
                                    fontWeight = if (isAll) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isAll) SolidPrimary else SlateText
                                )
                            }
                        }

                        // Incomes Button
                        val isIncome = selectedTypeFilter == CategoryType.INCOME
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isIncome) SolidIncome.copy(alpha = 0.16f) else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { selectedTypeFilter = CategoryType.INCOME }
                                .testTag("cat_filter_incomes")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    tint = if (isIncome) SolidIncome else SlateText,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = LanguageHelper.getString("incomes", languageMode),
                                    fontSize = 12.sp,
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

@Composable
private fun ScopePill(
    selected: Boolean,
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (selected) Color.White else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 7.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) SolidPrimary else Color.White.copy(alpha = 0.85f)
        )
    }
}

@Composable
private fun CategoryBottomFilterPill(
    selected: Boolean,
    label: String,
    icon: ImageVector,
    selectedColor: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (selected) selectedColor else Color.Transparent,
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SingleCategoryCard(
    item: FlattenedCategoryItem,
    isEditMode: Boolean,
    isInactive: Boolean = false,
    languageMode: LanguageMode,
    onEditCategory: (Category) -> Unit,
    onToggleActiveStatus: ((Category, Boolean) -> Unit)? = null
) {
    val cat = item.category
    val catColor = remember(cat.colorHex) {
        try {
            Color(android.graphics.Color.parseColor(cat.colorHex))
        } catch (_: Exception) {
            if (cat.type == CategoryType.EXPENSE) SolidExpense else SolidIncome
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onEditCategory(cat) },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isInactive) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isInactive) 0.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(if (isInactive) Color.Gray.copy(alpha = 0.2f) else catColor.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = IconHelper.getIconByName(cat.iconName),
                        contentDescription = null,
                        tint = if (isInactive) MaterialTheme.colorScheme.outline else catColor,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = cat.localizedName(languageMode),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isInactive) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (item.parentGroup != null) {
                        Text(
                            text = "Group: ${item.parentGroup.localizedName(languageMode)}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (cat.budgetLimit > 0) {
                    Text(
                        text = LanguageHelper.formatCurrency(cat.budgetLimit, languageMode),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isInactive) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                // Edit Button: HIDDEN BY DEFAULT, VISIBLE ONLY IN EDIT MODE
                if (isEditMode) {
                    IconButton(
                        onClick = { onEditCategory(cat) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyCategoriesCard(languageMode: LanguageMode) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = LanguageHelper.getString("no_categories", languageMode),
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun InactiveCategoryHeader(count: Int) {
    Spacer(modifier = Modifier.height(6.dp))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.VisibilityOff,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "Inactive Categories & Groups ($count)",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

private fun getCategorySortLabel(filter: CategorySortFilter, languageMode: LanguageMode): String {
    return when (filter) {
        CategorySortFilter.DEFAULT -> if (languageMode == LanguageMode.BANGLA) "ফিল্টার" else "Filter"
        CategorySortFilter.BUDGET_HIGH_TO_LOW -> if (languageMode == LanguageMode.BANGLA) "বাজেট ↓" else "Budget ↓"
        CategorySortFilter.BUDGET_LOW_TO_HIGH -> if (languageMode == LanguageMode.BANGLA) "বাজেট ↑" else "Budget ↑"
        CategorySortFilter.MOST_USED -> if (languageMode == LanguageMode.BANGLA) "বেশি ব্যবহৃত" else "Most Used"
        CategorySortFilter.LEAST_USED -> if (languageMode == LanguageMode.BANGLA) "কম ব্যবহৃত" else "Least Used"
        CategorySortFilter.NAME_AZ -> if (languageMode == LanguageMode.BANGLA) "নাম A-Z" else "Name A-Z"
        CategorySortFilter.NAME_ZA -> if (languageMode == LanguageMode.BANGLA) "নাম Z-A" else "Name Z-A"
    }
}

private fun getCategorySortMenuLabel(filter: CategorySortFilter, languageMode: LanguageMode): String {
    return when (filter) {
        CategorySortFilter.DEFAULT -> if (languageMode == LanguageMode.BANGLA) "স্বাভাবিক ক্রম" else "Default Order"
        CategorySortFilter.BUDGET_HIGH_TO_LOW -> if (languageMode == LanguageMode.BANGLA) "বাজেট সীমা: বেশি থেকে কম" else "Budget Limit: High to Low"
        CategorySortFilter.BUDGET_LOW_TO_HIGH -> if (languageMode == LanguageMode.BANGLA) "বাজেট সীমা: কম থেকে বেশি" else "Budget Limit: Low to High"
        CategorySortFilter.MOST_USED -> if (languageMode == LanguageMode.BANGLA) "সর্বাধিক ব্যবহৃত" else "Most Used / Frequent"
        CategorySortFilter.LEAST_USED -> if (languageMode == LanguageMode.BANGLA) "কম ব্যবহৃত" else "Least Used"
        CategorySortFilter.NAME_AZ -> if (languageMode == LanguageMode.BANGLA) "নাম: A থেকে Z" else "Name: A to Z"
        CategorySortFilter.NAME_ZA -> if (languageMode == LanguageMode.BANGLA) "নাম: Z থেকে A" else "Name: Z to A"
    }
}
