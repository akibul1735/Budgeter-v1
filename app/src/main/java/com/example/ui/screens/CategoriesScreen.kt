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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Velocity
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
import com.example.ui.components.filter.FilterState
import com.example.ui.components.filter.FilterValue
import com.example.ui.components.filter.UnifiedFilterDialog
import com.example.ui.components.filter.activeFilterCount
import com.example.ui.components.filter.isActive
import com.example.ui.components.filter.specs.CategoriesFilterSpec
import com.example.ui.components.filter.withSearchQuery
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidPrimary
import com.example.util.FilterStore
import com.example.util.IconHelper
import com.example.util.LanguageHelper
import kotlin.math.abs
import kotlinx.coroutines.launch

private val SlateText = Color(0xFF64748B)

enum class CategoryViewHierarchyFilter {
    ALL,
    ONLY_GROUPS,
    ONLY_CATEGORIES
}

enum class CategoryActiveStatusFilter {
    ALL,
    ACTIVE_ONLY,
    INACTIVE_ONLY
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

internal object CategoriesListFilterHelper {
    private const val ZERO_EPSILON = 0.0001

    fun nextHierarchyFilter(current: CategoryViewHierarchyFilter): CategoryViewHierarchyFilter =
        when (current) {
            CategoryViewHierarchyFilter.ALL -> CategoryViewHierarchyFilter.ONLY_GROUPS
            CategoryViewHierarchyFilter.ONLY_GROUPS -> CategoryViewHierarchyFilter.ONLY_CATEGORIES
            CategoryViewHierarchyFilter.ONLY_CATEGORIES -> CategoryViewHierarchyFilter.ALL
        }

    fun nextStatusFilter(current: CategoryActiveStatusFilter): CategoryActiveStatusFilter =
        when (current) {
            CategoryActiveStatusFilter.ALL -> CategoryActiveStatusFilter.ACTIVE_ONLY
            CategoryActiveStatusFilter.ACTIVE_ONLY -> CategoryActiveStatusFilter.INACTIVE_ONLY
            CategoryActiveStatusFilter.INACTIVE_ONLY -> CategoryActiveStatusFilter.ALL
        }

    fun nextSortFilter(current: CategorySortFilter): CategorySortFilter =
        when (current) {
            CategorySortFilter.DEFAULT,
            CategorySortFilter.NAME_AZ -> CategorySortFilter.NAME_ZA
            CategorySortFilter.NAME_ZA -> CategorySortFilter.BUDGET_HIGH_TO_LOW
            CategorySortFilter.BUDGET_HIGH_TO_LOW -> CategorySortFilter.BUDGET_LOW_TO_HIGH
            CategorySortFilter.BUDGET_LOW_TO_HIGH,
            CategorySortFilter.MOST_USED,
            CategorySortFilter.LEAST_USED -> CategorySortFilter.NAME_AZ
        }

    fun effectiveSortFilter(sort: CategorySortFilter): CategorySortFilter =
        if (sort == CategorySortFilter.DEFAULT) CategorySortFilter.NAME_AZ else sort

    fun isZeroBudget(value: Double): Boolean = abs(value) < ZERO_EPSILON

    fun matchesSearch(cat: Category, query: String): Boolean {
        if (query.isBlank()) return true
        val clean = query.trim().lowercase()
        return cat.nameEn.lowercase().contains(clean) ||
            cat.nameBn.lowercase().contains(clean)
    }

    fun computeEffectiveGroupBudget(
        parent: Category,
        subCategoriesMap: Map<Long, List<Category>>,
        activeSubsOnly: Boolean = parent.isActive
    ): Double {
        val subs = subCategoriesMap[parent.id] ?: emptyList()
        if (subs.isEmpty()) return parent.budgetLimit
        val relevantSubs = if (activeSubsOnly) subs.filter { it.isActive } else subs
        return if (relevantSubs.isNotEmpty()) relevantSubs.sumOf { it.budgetLimit } else parent.budgetLimit
    }

    fun sortGroups(
        list: List<Category>,
        sortFilter: CategorySortFilter,
        languageMode: LanguageMode,
        subCategoriesMap: Map<Long, List<Category>>,
        groupUsageCount: (Category) -> Int
    ): List<Category> {
        return when (effectiveSortFilter(sortFilter)) {
            CategorySortFilter.DEFAULT -> list
            CategorySortFilter.BUDGET_HIGH_TO_LOW -> list.sortedByDescending {
                computeEffectiveGroupBudget(it, subCategoriesMap, activeSubsOnly = it.isActive)
            }
            CategorySortFilter.BUDGET_LOW_TO_HIGH -> list.sortedBy {
                computeEffectiveGroupBudget(it, subCategoriesMap, activeSubsOnly = it.isActive)
            }
            CategorySortFilter.MOST_USED -> list.sortedByDescending { groupUsageCount(it) }
            CategorySortFilter.LEAST_USED -> list.sortedBy { groupUsageCount(it) }
            CategorySortFilter.NAME_AZ -> list.sortedBy { it.localizedName(languageMode).lowercase() }
            CategorySortFilter.NAME_ZA -> list.sortedByDescending { it.localizedName(languageMode).lowercase() }
        }
    }

    fun sortSubCategories(
        list: List<Category>,
        sortFilter: CategorySortFilter,
        languageMode: LanguageMode,
        categoryUsageCount: (Long) -> Int
    ): List<Category> {
        return when (effectiveSortFilter(sortFilter)) {
            CategorySortFilter.DEFAULT -> list
            CategorySortFilter.BUDGET_HIGH_TO_LOW -> list.sortedByDescending { it.budgetLimit }
            CategorySortFilter.BUDGET_LOW_TO_HIGH -> list.sortedBy { it.budgetLimit }
            CategorySortFilter.MOST_USED -> list.sortedByDescending { categoryUsageCount(it.id) }
            CategorySortFilter.LEAST_USED -> list.sortedBy { categoryUsageCount(it.id) }
            CategorySortFilter.NAME_AZ -> list.sortedBy { it.localizedName(languageMode).lowercase() }
            CategorySortFilter.NAME_ZA -> list.sortedByDescending { it.localizedName(languageMode).lowercase() }
        }
    }

    fun sortFlattened(
        list: List<FlattenedCategoryItem>,
        sortFilter: CategorySortFilter,
        languageMode: LanguageMode
    ): List<FlattenedCategoryItem> {
        return when (effectiveSortFilter(sortFilter)) {
            CategorySortFilter.DEFAULT -> list
            CategorySortFilter.BUDGET_HIGH_TO_LOW -> list.sortedByDescending { it.budgetAmount }
            CategorySortFilter.BUDGET_LOW_TO_HIGH -> list.sortedBy { it.budgetAmount }
            CategorySortFilter.MOST_USED -> list.sortedByDescending { it.usageCount }
            CategorySortFilter.LEAST_USED -> list.sortedBy { it.usageCount }
            CategorySortFilter.NAME_AZ -> list.sortedBy { it.category.localizedName(languageMode).lowercase() }
            CategorySortFilter.NAME_ZA -> list.sortedByDescending { it.category.localizedName(languageMode).lowercase() }
        }
    }

    fun filterParentGroups(
        parentGroups: List<Category>,
        subCategoriesMap: Map<Long, List<Category>>,
        selectedTypeFilter: CategoryType?,
        hierarchyFilter: CategoryViewHierarchyFilter,
        excludeZeroBudget: Boolean,
        searchQuery: String,
        sortFilter: CategorySortFilter,
        languageMode: LanguageMode,
        groupUsageCount: (Category) -> Int,
        activeSubsOnly: Boolean
    ): List<Category> {
        val isOnlyGroups = hierarchyFilter == CategoryViewHierarchyFilter.ONLY_GROUPS
        val filtered = parentGroups.filter { parent ->
            if (selectedTypeFilter != null && parent.type != selectedTypeFilter) return@filter false

            val subs = subCategoriesMap[parent.id] ?: emptyList()
            val relevantSubs = if (activeSubsOnly) subs.filter { it.isActive } else subs

            val matchesSearch = if (searchQuery.isBlank()) {
                true
            } else {
                matchesSearch(parent, searchQuery) ||
                    (!isOnlyGroups && relevantSubs.any { matchesSearch(it, searchQuery) })
            }
            if (!matchesSearch) return@filter false

            if (!excludeZeroBudget) return@filter true

            if (isOnlyGroups || relevantSubs.isEmpty()) {
                val effectiveBudget = computeEffectiveGroupBudget(parent, subCategoriesMap, activeSubsOnly)
                !isZeroBudget(effectiveBudget)
            } else {
                val parentMatchedDirectly = searchQuery.isNotBlank() && matchesSearch(parent, searchQuery)
                val searchMatchedSubs = if (searchQuery.isBlank() || parentMatchedDirectly) {
                    relevantSubs
                } else {
                    relevantSubs.filter { matchesSearch(it, searchQuery) }
                }
                searchMatchedSubs.any { !isZeroBudget(it.budgetLimit) }
            }
        }
        return sortGroups(filtered, sortFilter, languageMode, subCategoriesMap, groupUsageCount)
    }

    fun filterFlattenedCategories(
        allCategories: List<Category>,
        subCategoriesMap: Map<Long, List<Category>>,
        activeStatusTarget: Boolean,
        selectedTypeFilter: CategoryType?,
        excludeZeroBudget: Boolean,
        searchQuery: String,
        sortFilter: CategorySortFilter,
        languageMode: LanguageMode,
        categoryUsageCount: (Long) -> Int
    ): List<FlattenedCategoryItem> {
        val items = mutableListOf<FlattenedCategoryItem>()
        if (activeStatusTarget) {
            val activeParents = allCategories.filter {
                it.isActive && it.parentId == null && (selectedTypeFilter == null || it.type == selectedTypeFilter)
            }
            for (parent in activeParents) {
                val activeSubs = (subCategoriesMap[parent.id] ?: emptyList()).filter { it.isActive }
                if (activeSubs.isEmpty()) {
                    if (!excludeZeroBudget || !isZeroBudget(parent.budgetLimit)) {
                        items.add(
                            FlattenedCategoryItem(
                                category = parent,
                                parentGroup = null,
                                budgetAmount = parent.budgetLimit,
                                usageCount = categoryUsageCount(parent.id)
                            )
                        )
                    }
                } else {
                    activeSubs.forEach { sub ->
                        if (!excludeZeroBudget || !isZeroBudget(sub.budgetLimit)) {
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
            }
        } else {
            val inactiveLeafs = allCategories.filter {
                !it.isActive && (selectedTypeFilter == null || it.type == selectedTypeFilter)
            }
            for (cat in inactiveLeafs) {
                if (!excludeZeroBudget || !isZeroBudget(cat.budgetLimit)) {
                    val parent = cat.parentId?.let { pId -> allCategories.firstOrNull { it.id == pId } }
                    items.add(
                        FlattenedCategoryItem(
                            category = cat,
                            parentGroup = parent,
                            budgetAmount = cat.budgetLimit,
                            usageCount = categoryUsageCount(cat.id)
                        )
                    )
                }
            }
        }

        val searched = if (searchQuery.isBlank()) {
            items
        } else {
            items.filter { item ->
                matchesSearch(item.category, searchQuery) ||
                    (item.parentGroup?.let { matchesSearch(it, searchQuery) } == true)
            }
        }

        return sortFlattened(searched, sortFilter, languageMode)
    }
}

@Composable
fun CategoriesScreen(
    categories: List<Category>,
    languageMode: LanguageMode,
    initialTab: Int? = null,
    allTransactions: List<TransactionWithDetails> = emptyList(),
    monthlyBudgets: List<MonthlyBudget> = emptyList(),
    onOpenDrawer: () -> Unit = {},
    onAddCategoryClick: (CategoryType) -> Unit,
    onAddSubCategoryClick: (Category) -> Unit,
    onEditCategoryClick: (Category) -> Unit,
    onToggleActiveStatus: ((Category, Boolean) -> Unit)? = null,
    onUpdateCategories: ((List<Category>) -> Unit)? = null,
    onDeleteCategories: ((List<Category>) -> Unit)? = null,
    onNavigateToLabels: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val filterStore = remember { FilterStore.getInstance(context) }

    val categoriesSpec = remember {
        com.example.ui.components.filter.specs.CategoriesFilterSpec.createSpec()
    }

    var filterState by remember {
        val loaded = filterStore.loadFilterState(com.example.ui.components.filter.specs.CategoriesFilterSpec.SPEC_KEY)
        val initial = if (loaded.isEmpty()) {
            val initialType = when (initialTab) {
                1 -> com.example.ui.components.filter.specs.CategoriesFilterSpec.TYPE_INCOME
                else -> com.example.ui.components.filter.specs.CategoriesFilterSpec.TYPE_EXPENSE
            }
            mapOf(
                com.example.ui.components.filter.specs.CategoriesFilterSpec.FIELD_TYPE_MODE to FilterValue.ToggleGroup(setOf(initialType)),
                com.example.ui.components.filter.specs.CategoriesFilterSpec.FIELD_HIERARCHY to FilterValue.ToggleGroup(setOf(com.example.ui.components.filter.specs.CategoriesFilterSpec.HIERARCHY_ALL)),
                com.example.ui.components.filter.specs.CategoriesFilterSpec.FIELD_SORT to FilterValue.Sort("default")
            )
        } else {
            loaded
        }
        mutableStateOf(initial)
    }

    val updateFilterState: (FilterState) -> Unit = { newState ->
        if (newState != filterState) {
            filterState = newState
            filterStore.saveFilterState(com.example.ui.components.filter.specs.CategoriesFilterSpec.SPEC_KEY, newState)
        }
    }

    var showFilterDialog by remember { mutableStateOf(false) }
    var isEditMode by remember { mutableStateOf(false) }

    val selectedTypeFilter = remember(filterState) {
        CategoriesFilterSpec.getTypeFilter(filterState)
    }
    val hierarchyFilter = remember(filterState) {
        CategoriesFilterSpec.getHierarchyFilter(filterState)
    }
    val statusFilter = remember(filterState) {
        CategoriesFilterSpec.getStatusFilter(filterState)
    }
    val excludeZeroBudget = remember(filterState) {
        CategoriesFilterSpec.getExcludeZeroBudget(filterState)
    }
    val sortFilter = remember(filterState) {
        CategoriesFilterSpec.getSortFilter(filterState)
    }
    val searchQuery = remember(filterState) {
        CategoriesFilterSpec.getSearchQuery(filterState)
    }

    val expandedMap = remember { mutableStateMapOf<Long, Boolean>() }

    LaunchedEffect(initialTab) {
        if (initialTab != null) {
            val targetType = when (initialTab) {
                1 -> CategoriesFilterSpec.TYPE_INCOME
                else -> CategoriesFilterSpec.TYPE_EXPENSE
            }
            val currentType = (filterState[CategoriesFilterSpec.FIELD_TYPE_MODE] as? FilterValue.ToggleGroup)?.activeIds?.firstOrNull()
            if (currentType != targetType) {
                updateFilterState(
                    filterState + (CategoriesFilterSpec.FIELD_TYPE_MODE to FilterValue.ToggleGroup(setOf(targetType)))
                )
            }
        }
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

    fun groupUsageCount(groupCat: Category): Int {
        val subs = subCategoriesMap[groupCat.id] ?: emptyList()
        return (categoryUsageMap[groupCat.id] ?: 0) + subs.sumOf { categoryUsageMap[it.id] ?: 0 }
    }

    val parentActiveCategories = remember(
        activeCategories,
        subCategoriesMap,
        selectedTypeFilter,
        hierarchyFilter,
        statusFilter,
        excludeZeroBudget,
        sortFilter,
        allTransactions,
        searchQuery,
        languageMode
    ) {
        if (statusFilter == CategoryActiveStatusFilter.INACTIVE_ONLY) {
            emptyList()
        } else {
            CategoriesListFilterHelper.filterParentGroups(
                parentGroups = activeCategories.filter { it.parentId == null },
                subCategoriesMap = subCategoriesMap,
                selectedTypeFilter = selectedTypeFilter,
                hierarchyFilter = hierarchyFilter,
                excludeZeroBudget = excludeZeroBudget,
                searchQuery = searchQuery,
                sortFilter = sortFilter,
                languageMode = languageMode,
                groupUsageCount = ::groupUsageCount,
                activeSubsOnly = true
            )
        }
    }

    val parentInactiveCategories = remember(
        inactiveCategories,
        subCategoriesMap,
        selectedTypeFilter,
        hierarchyFilter,
        statusFilter,
        excludeZeroBudget,
        sortFilter,
        allTransactions,
        searchQuery,
        languageMode
    ) {
        if (statusFilter == CategoryActiveStatusFilter.ACTIVE_ONLY) {
            emptyList()
        } else {
            CategoriesListFilterHelper.filterParentGroups(
                parentGroups = inactiveCategories.filter { it.parentId == null },
                subCategoriesMap = subCategoriesMap,
                selectedTypeFilter = selectedTypeFilter,
                hierarchyFilter = hierarchyFilter,
                excludeZeroBudget = excludeZeroBudget,
                searchQuery = searchQuery,
                sortFilter = sortFilter,
                languageMode = languageMode,
                groupUsageCount = ::groupUsageCount,
                activeSubsOnly = false
            )
        }
    }

    // Flattened Categories for ONLY_CATEGORIES mode
    val flattenedActiveCategories = remember(
        categories,
        subCategoriesMap,
        selectedTypeFilter,
        statusFilter,
        excludeZeroBudget,
        sortFilter,
        allTransactions,
        searchQuery,
        languageMode
    ) {
        if (statusFilter == CategoryActiveStatusFilter.INACTIVE_ONLY) {
            emptyList()
        } else {
            CategoriesListFilterHelper.filterFlattenedCategories(
                allCategories = categories,
                subCategoriesMap = subCategoriesMap,
                activeStatusTarget = true,
                selectedTypeFilter = selectedTypeFilter,
                excludeZeroBudget = excludeZeroBudget,
                searchQuery = searchQuery,
                sortFilter = sortFilter,
                languageMode = languageMode,
                categoryUsageCount = categoryUsageCount
            )
        }
    }

    val flattenedInactiveCategories = remember(
        categories,
        subCategoriesMap,
        selectedTypeFilter,
        statusFilter,
        excludeZeroBudget,
        sortFilter,
        allTransactions,
        searchQuery,
        languageMode
    ) {
        if (statusFilter == CategoryActiveStatusFilter.ACTIVE_ONLY) {
            emptyList()
        } else {
            CategoriesListFilterHelper.filterFlattenedCategories(
                allCategories = categories,
                subCategoriesMap = subCategoriesMap,
                activeStatusTarget = false,
                selectedTypeFilter = selectedTypeFilter,
                excludeZeroBudget = excludeZeroBudget,
                searchQuery = searchQuery,
                sortFilter = sortFilter,
                languageMode = languageMode,
                categoryUsageCount = categoryUsageCount
            )
        }
    }

    // Scroll state & Bottom Nav visibility
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var isBottomNavVisible by remember { mutableStateOf(true) }

    fun scrollToListTop() {
        coroutineScope.launch {
            listState.animateScrollToItem(0)
        }
    }

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
                searchQuery = searchQuery,
                onSearchQueryChange = { newQuery ->
                    updateFilterState(filterState.withSearchQuery(newQuery, CategoriesFilterSpec.FIELD_SEARCH))
                },
                searchPlaceholder = if (languageMode == LanguageMode.BANGLA) "ক্যাটাগরি খুঁজুন..." else "Search categories...",
                showSearchButton = true,
                showFilterButton = true,
                isFilterActive = categoriesSpec.isActive(filterState),
                activeFilterCount = categoriesSpec.activeFilterCount(filterState),
                onFilterClick = { showFilterDialog = true },
                onOpenDrawer = onOpenDrawer
            )

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 6.dp, bottom = 125.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // --- 1. Compact Net Earnings Top Card ---
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("categories_summary_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
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
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = LanguageHelper.formatCurrency(netEarnings, languageMode),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }

                                // Edit Mode Button on Top Card (activates/deactivates edit mode)
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isEditMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.dp, if (isEditMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
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
                                            tint = if (isEditMode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = if (isEditMode) (if (languageMode == LanguageMode.BANGLA) "সম্পন্ন" else "Done")
                                            else (if (languageMode == LanguageMode.BANGLA) "সম্পাদন" else "Edit"),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isEditMode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Middle Compact Row: Total Incomes vs Total Expenses with High-Contrast Badges
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Total Incomes
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(SolidIncome.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.TrendingUp,
                                                contentDescription = null,
                                                tint = SolidIncome,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = LanguageHelper.getString("incomes", languageMode),
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(1.dp))
                                            Text(
                                                text = LanguageHelper.formatCurrency(totalIncomeBudget, languageMode),
                                                color = SolidIncome,
                                                fontSize = 14.5.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }

                                // Total Expenses
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(SolidExpense.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.TrendingDown,
                                                contentDescription = null,
                                                tint = SolidExpense,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = LanguageHelper.getString("expenses", languageMode),
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(1.dp))
                                            Text(
                                                text = LanguageHelper.formatCurrency(totalExpenseBudget, languageMode),
                                                color = SolidExpense,
                                                fontSize = 14.5.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }

                        }
                    }
                }

                // --- 2. Compact 4-Item Cycling Filter & Sort Bar: Hierarchy -> Status -> Sort -> Hide 0 ---
                item {
                    CategoriesQuickFilterBar(
                        hierarchyFilter = hierarchyFilter,
                        statusFilter = statusFilter,
                        sortFilter = sortFilter,
                        excludeZeroBudget = excludeZeroBudget,
                        languageMode = languageMode,
                        onCycleHierarchy = {
                            val next = CategoriesListFilterHelper.nextHierarchyFilter(hierarchyFilter)
                            updateFilterState(CategoriesFilterSpec.withHierarchy(filterState, next))
                            scrollToListTop()
                        },
                        onCycleStatus = {
                            val next = CategoriesListFilterHelper.nextStatusFilter(statusFilter)
                            updateFilterState(CategoriesFilterSpec.withStatus(filterState, next))
                            scrollToListTop()
                        },
                        onCycleSort = {
                            val next = CategoriesListFilterHelper.nextSortFilter(sortFilter)
                            updateFilterState(CategoriesFilterSpec.withSort(filterState, next))
                            scrollToListTop()
                        },
                        onToggleHideZero = {
                            updateFilterState(CategoriesFilterSpec.withExcludeZero(filterState, !excludeZeroBudget))
                            scrollToListTop()
                        }
                    )
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
                            val searchFilteredSubs = if (searchQuery.isBlank()) {
                                subs
                            } else {
                                val parentMatches = CategoriesListFilterHelper.matchesSearch(parent, searchQuery)
                                if (parentMatches) subs else subs.filter { CategoriesListFilterHelper.matchesSearch(it, searchQuery) }
                            }
                            val displayedSubs = if (excludeZeroBudget) {
                                searchFilteredSubs.filter { !CategoriesListFilterHelper.isZeroBudget(it.budgetLimit) }
                            } else {
                                searchFilteredSubs
                            }
                            val sortedSubs = CategoriesListFilterHelper.sortSubCategories(displayedSubs, sortFilter, languageMode, categoryUsageCount)
                            val isExpanded = if (searchQuery.isNotBlank()) true else (expandedMap[parent.id] ?: true)

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
                                            val isParentCustom = IconHelper.isFullSurfaceIcon(parent.iconName)
                                            Box(
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isParentCustom) Color.Transparent else parentColor.copy(alpha = 0.16f))
                                                    .border(1.dp, if (isParentCustom) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f) else parentColor.copy(alpha = 0.35f), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                IconHelper.AppIcon(
                                                    iconName = parent.iconName,
                                                    fallbackName = parent.localizedName(languageMode),
                                                    contentDescription = parent.localizedName(languageMode),
                                                    tint = parentColor,
                                                    modifier = if (isParentCustom) Modifier.fillMaxSize() else Modifier.size(24.dp)
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(12.dp))

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
                                                                val isSubCustom = IconHelper.isFullSurfaceIcon(subCat.iconName)
                                                                Box(
                                                                    modifier = Modifier
                                                                        .size(28.dp)
                                                                        .clip(CircleShape)
                                                                        .background(if (isSubCustom) Color.Transparent else subColor.copy(alpha = 0.18f)),
                                                                    contentAlignment = Alignment.Center
                                                                ) {
                                                                    IconHelper.AppIcon(
                                                                        iconName = subCat.iconName,
                                                                        fallbackName = subCat.localizedName(languageMode),
                                                                        contentDescription = subCat.localizedName(languageMode),
                                                                        tint = subColor,
                                                                        modifier = if (isSubCustom) Modifier.fillMaxSize() else Modifier.size(17.dp)
                                                                    )
                                                                }
                                                                Spacer(modifier = Modifier.width(10.dp))
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
                            if (statusFilter != CategoryActiveStatusFilter.INACTIVE_ONLY || parentActiveCategories.isNotEmpty()) {
                                item {
                                    InactiveCategoryHeader(count = parentInactiveCategories.size)
                                }
                            }
                            items(parentInactiveCategories, key = { "inactive_${it.id}" }) { parent ->
                                val subs = (subCategoriesMap[parent.id] ?: emptyList())
                                val inactiveGroupBudget = CategoriesListFilterHelper.computeEffectiveGroupBudget(
                                    parent = parent,
                                    subCategoriesMap = subCategoriesMap,
                                    activeSubsOnly = false
                                )
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
                                            IconHelper.AppIcon(
                                                iconName = parent.iconName,
                                                fallbackName = parent.localizedName(languageMode),
                                                contentDescription = parent.localizedName(languageMode),
                                                tint = MaterialTheme.colorScheme.outline,
                                                modifier = Modifier.size(26.dp)
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
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

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (inactiveGroupBudget > 0) {
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                                                ) {
                                                    Text(
                                                        text = "Limit: ${LanguageHelper.formatCurrency(inactiveGroupBudget, languageMode)}",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = MaterialTheme.colorScheme.outline,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(6.dp))
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
                                .clickable {
                                    updateFilterState(
                                        filterState + (com.example.ui.components.filter.specs.CategoriesFilterSpec.FIELD_TYPE_MODE to FilterValue.ToggleGroup(setOf(com.example.ui.components.filter.specs.CategoriesFilterSpec.TYPE_EXPENSE)))
                                    )
                                }
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
                                .clickable {
                                    updateFilterState(
                                        filterState + (com.example.ui.components.filter.specs.CategoriesFilterSpec.FIELD_TYPE_MODE to FilterValue.ToggleGroup(setOf(com.example.ui.components.filter.specs.CategoriesFilterSpec.TYPE_ALL)))
                                    )
                                }
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
                                .clickable {
                                    updateFilterState(
                                        filterState + (com.example.ui.components.filter.specs.CategoriesFilterSpec.FIELD_TYPE_MODE to FilterValue.ToggleGroup(setOf(com.example.ui.components.filter.specs.CategoriesFilterSpec.TYPE_INCOME)))
                                    )
                                }
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

    // Filter & Sort Dialog
    if (showFilterDialog) {
        UnifiedFilterDialog(
            spec = categoriesSpec,
            initialState = filterState,
            countProvider = { draftState ->
                val draftType = CategoriesFilterSpec.getTypeFilter(draftState)
                val draftHierarchy = CategoriesFilterSpec.getHierarchyFilter(draftState)
                val draftStatus = CategoriesFilterSpec.getStatusFilter(draftState)
                val draftExcludeZero = CategoriesFilterSpec.getExcludeZeroBudget(draftState)
                val draftSearch = CategoriesFilterSpec.getSearchQuery(draftState)

                if (draftHierarchy == CategoryViewHierarchyFilter.ONLY_CATEGORIES) {
                    val activeCount = if (draftStatus == CategoryActiveStatusFilter.INACTIVE_ONLY) 0 else {
                        CategoriesListFilterHelper.filterFlattenedCategories(
                            allCategories = categories,
                            subCategoriesMap = subCategoriesMap,
                            activeStatusTarget = true,
                            selectedTypeFilter = draftType,
                            excludeZeroBudget = draftExcludeZero,
                            searchQuery = draftSearch,
                            sortFilter = sortFilter,
                            languageMode = languageMode,
                            categoryUsageCount = categoryUsageCount
                        ).size
                    }
                    val inactiveCount = if (draftStatus == CategoryActiveStatusFilter.ACTIVE_ONLY) 0 else {
                        CategoriesListFilterHelper.filterFlattenedCategories(
                            allCategories = categories,
                            subCategoriesMap = subCategoriesMap,
                            activeStatusTarget = false,
                            selectedTypeFilter = draftType,
                            excludeZeroBudget = draftExcludeZero,
                            searchQuery = draftSearch,
                            sortFilter = sortFilter,
                            languageMode = languageMode,
                            categoryUsageCount = categoryUsageCount
                        ).size
                    }
                    activeCount + inactiveCount
                } else {
                    val activeGroupCount = if (draftStatus == CategoryActiveStatusFilter.INACTIVE_ONLY) 0 else {
                        CategoriesListFilterHelper.filterParentGroups(
                            parentGroups = activeCategories.filter { it.parentId == null },
                            subCategoriesMap = subCategoriesMap,
                            selectedTypeFilter = draftType,
                            hierarchyFilter = draftHierarchy,
                            excludeZeroBudget = draftExcludeZero,
                            searchQuery = draftSearch,
                            sortFilter = sortFilter,
                            languageMode = languageMode,
                            groupUsageCount = ::groupUsageCount,
                            activeSubsOnly = true
                        ).size
                    }
                    val inactiveGroupCount = if (draftStatus == CategoryActiveStatusFilter.ACTIVE_ONLY) 0 else {
                        CategoriesListFilterHelper.filterParentGroups(
                            parentGroups = inactiveCategories.filter { it.parentId == null },
                            subCategoriesMap = subCategoriesMap,
                            selectedTypeFilter = draftType,
                            hierarchyFilter = draftHierarchy,
                            excludeZeroBudget = draftExcludeZero,
                            searchQuery = draftSearch,
                            sortFilter = sortFilter,
                            languageMode = languageMode,
                            groupUsageCount = ::groupUsageCount,
                            activeSubsOnly = false
                        ).size
                    }
                    activeGroupCount + inactiveGroupCount
                }
            },
            languageMode = languageMode,
            onDismiss = { showFilterDialog = false },
            onApply = { newFilter ->
                updateFilterState(newFilter)
                showFilterDialog = false
            }
        )
    }
}

@Composable
private fun CategoriesQuickFilterBar(
    hierarchyFilter: CategoryViewHierarchyFilter,
    statusFilter: CategoryActiveStatusFilter,
    sortFilter: CategorySortFilter,
    excludeZeroBudget: Boolean,
    languageMode: LanguageMode,
    onCycleHierarchy: () -> Unit,
    onCycleStatus: () -> Unit,
    onCycleSort: () -> Unit,
    onToggleHideZero: () -> Unit
) {
    val isBn = languageMode == LanguageMode.BANGLA

    val consumeHorizontalScroll = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                return Offset(available.x, 0f)
            }
            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                return Velocity(available.x, 0f)
            }
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val isCompactWidth = maxWidth < 360.dp

        Row(
            modifier = Modifier
                .widthIn(min = maxWidth)
                .nestedScroll(consumeHorizontalScroll)
                .horizontalScroll(rememberScrollState())
                .testTag("categories_quick_filter_bar"),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Hierarchy Cycle: All -> Groups -> Categories -> All
            val hierarchyIcon = when (hierarchyFilter) {
                CategoryViewHierarchyFilter.ONLY_GROUPS -> Icons.Default.Folder
                CategoryViewHierarchyFilter.ONLY_CATEGORIES -> Icons.Default.Category
                CategoryViewHierarchyFilter.ALL -> Icons.Default.Layers
            }
            val hierarchyLabel = when (hierarchyFilter) {
                CategoryViewHierarchyFilter.ONLY_GROUPS -> if (isBn) "গ্রুপ" else "Groups"
                CategoryViewHierarchyFilter.ONLY_CATEGORIES -> if (isBn) "ক্যাটাগরি" else "Categories"
                CategoryViewHierarchyFilter.ALL -> if (isBn) "সব" else "All"
            }
            CategoriesCompactPill(
                icon = hierarchyIcon,
                label = hierarchyLabel,
                isActive = hierarchyFilter != CategoryViewHierarchyFilter.ALL,
                isCompactWidth = isCompactWidth,
                modifier = Modifier.testTag("categories_hierarchy_cycle_button"),
                onClick = onCycleHierarchy
            )

            // 2. Status Cycle: All -> Active -> Inactive -> All
            val statusIcon = when (statusFilter) {
                CategoryActiveStatusFilter.ACTIVE_ONLY -> Icons.Default.CheckCircle
                CategoryActiveStatusFilter.INACTIVE_ONLY -> Icons.Default.VisibilityOff
                CategoryActiveStatusFilter.ALL -> Icons.Default.Tune
            }
            val statusLabel = when (statusFilter) {
                CategoryActiveStatusFilter.ACTIVE_ONLY -> if (isBn) "সক্রিয়" else "Active"
                CategoryActiveStatusFilter.INACTIVE_ONLY -> if (isBn) "নিষ্ক্রিয়" else "Inactive"
                CategoryActiveStatusFilter.ALL -> if (isBn) "সব" else "All"
            }
            CategoriesCompactPill(
                icon = statusIcon,
                label = statusLabel,
                isActive = statusFilter != CategoryActiveStatusFilter.ALL,
                isCompactWidth = isCompactWidth,
                modifier = Modifier.testTag("categories_status_cycle_button"),
                onClick = onCycleStatus
            )

            // 3. Short Sort Cycle: A -> Z -> Z -> A -> High -> Low -> Low -> High -> A -> Z
            val effectiveSort = CategoriesListFilterHelper.effectiveSortFilter(sortFilter)
            val sortIcon = when (effectiveSort) {
                CategorySortFilter.BUDGET_HIGH_TO_LOW -> Icons.Default.ArrowDownward
                CategorySortFilter.BUDGET_LOW_TO_HIGH -> Icons.Default.ArrowUpward
                else -> Icons.AutoMirrored.Filled.Sort
            }
            val sortLabel = when (effectiveSort) {
                CategorySortFilter.NAME_ZA -> "Z → A"
                CategorySortFilter.BUDGET_HIGH_TO_LOW -> if (isBn) "বেশি → কম" else "High → Low"
                CategorySortFilter.BUDGET_LOW_TO_HIGH -> if (isBn) "কম → বেশি" else "Low → High"
                CategorySortFilter.MOST_USED -> if (isBn) "বেশি ব্যবহৃত" else "Most Used"
                CategorySortFilter.LEAST_USED -> if (isBn) "কম ব্যবহৃত" else "Least Used"
                CategorySortFilter.NAME_AZ, CategorySortFilter.DEFAULT -> "A → Z"
            }
            CategoriesCompactPill(
                icon = sortIcon,
                label = sortLabel,
                isActive = effectiveSort != CategorySortFilter.NAME_AZ,
                isCompactWidth = isCompactWidth,
                modifier = Modifier.testTag("categories_sort_cycle_button"),
                onClick = onCycleSort
            )

            // 4. Hide 0 Toggle (Budget limit = 0)
            CategoriesCompactPill(
                icon = if (excludeZeroBudget) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                label = if (isBn) "০ লুকান" else "Hide 0",
                isActive = excludeZeroBudget,
                isCompactWidth = isCompactWidth,
                modifier = Modifier.testTag("categories_hide_zero_button"),
                onClick = onToggleHideZero
            )
        }
    }
}

@Composable
private fun CategoriesCompactPill(
    icon: ImageVector,
    label: String,
    isActive: Boolean = false,
    activeColor: Color = SolidPrimary,
    isCompactWidth: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bgColor = if (isActive) activeColor.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    val borderColor = if (isActive) activeColor.copy(alpha = 0.55f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
    val contentColor = if (isActive) activeColor else MaterialTheme.colorScheme.onSurface
    val horizontalPad = if (isCompactWidth) 6.dp else 8.dp
    val iconSize = if (isCompactWidth) 11.dp else 12.dp
    val fontSize = if (isCompactWidth) 10.sp else 10.5.sp

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = bgColor,
        border = BorderStroke(0.85.dp, borderColor),
        modifier = modifier
            .height(29.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = horizontalPad),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(iconSize)
            )
            Text(
                text = label,
                fontSize = fontSize,
                fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium,
                color = contentColor,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
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
                    IconHelper.AppIcon(
                        iconName = cat.iconName,
                        fallbackName = cat.localizedName(languageMode),
                        contentDescription = cat.localizedName(languageMode),
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
