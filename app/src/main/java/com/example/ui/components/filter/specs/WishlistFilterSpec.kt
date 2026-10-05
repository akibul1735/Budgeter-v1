package com.example.ui.components.filter.specs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.ui.graphics.Color
import com.example.data.model.Category
import com.example.data.model.WishlistItemWithCategory
import com.example.data.model.WishlistPriority
import com.example.data.model.WishlistTargetType
import com.example.ui.components.filter.FilterCondition
import com.example.ui.components.filter.FilterField
import com.example.ui.components.filter.FilterSpec
import com.example.ui.components.filter.FilterState
import com.example.ui.components.filter.FilterValue
import com.example.ui.components.filter.SelectItemOption
import com.example.ui.components.filter.SortOptionItem
import com.example.ui.screens.WishlistFilterTab
import com.example.ui.screens.WishlistSort

/**
 * Filter specification for WishlistScreen.
 */
object WishlistFilterSpec {

    const val SPEC_KEY = "wishlist"

    // Field IDs
    const val FIELD_SEARCH = "search"
    const val FIELD_TAB = "tab"
    const val FIELD_PRIORITY = "priority"
    const val FIELD_CATEGORIES = "categories"
    const val FIELD_SORT = "sort"

    // Tab condition IDs
    const val TAB_ACTIVE = "active"
    const val TAB_NEXT_MONTH = "next_month"
    const val TAB_PLANNED_MONTHS = "planned_months"
    const val TAB_SAVINGS_GOALS = "savings_goals"
    const val TAB_PURCHASED = "purchased"
    const val TAB_ALL = "all"

    // Priority condition IDs
    const val PRIORITY_HIGH = "high"
    const val PRIORITY_MEDIUM = "medium"
    const val PRIORITY_LOW = "low"

    // Sort IDs
    const val SORT_PRIORITY = "priority"
    const val SORT_AMOUNT_DESC = "amount_desc"
    const val SORT_AMOUNT_ASC = "amount_asc"
    const val SORT_RECENT = "recent"

    fun createSpec(categories: List<Category> = emptyList()): FilterSpec<Any> {
        val categoryOptions = categories.map { cat ->
            SelectItemOption(
                id = cat.id.toString(),
                titleEn = cat.nameEn,
                titleBn = cat.nameBn
            )
        }

        val sortOptions: List<SortOptionItem<Any>> = listOf(
            SortOptionItem(SORT_PRIORITY, "Priority (High to Low)", "অগ্রাধিকার: বেশি → কম"),
            SortOptionItem(SORT_AMOUNT_DESC, "Amount: High → Low", "পরিমাণ: বেশি → কম"),
            SortOptionItem(SORT_AMOUNT_ASC, "Amount: Low → High", "পরিমাণ: কম → বেশি"),
            SortOptionItem(SORT_RECENT, "Recently Added", "সাম্প্রতিক যোগ করা")
        )

        return FilterSpec(
            key = SPEC_KEY,
            titleEn = "Filter & Sort Wishlist",
            titleBn = "ইচ্ছেতালিকা ফিল্টার ও সাজানো",
            accentColor = Color(0xFF6366F1), // Indigo accent
            fields = listOf(
                FilterField.SearchField(
                    id = FIELD_SEARCH,
                    titleEn = "Search",
                    titleBn = "অনুসন্ধান",
                    hintEn = "Search wishlist...",
                    hintBn = "ইচ্ছেতালিকা খুঁজুন...",
                    icon = Icons.Default.Search
                ),
                FilterField.ToggleGroupField(
                    id = FIELD_TAB,
                    titleEn = "Status Scope",
                    titleBn = "অবস্থা",
                    icon = Icons.Default.Layers,
                    conditions = listOf(
                        FilterCondition(TAB_ACTIVE, "Active", "সক্রিয়", group = "tab"),
                        FilterCondition(TAB_NEXT_MONTH, "Next Month", "পরবর্তী মাস", group = "tab"),
                        FilterCondition(TAB_PLANNED_MONTHS, "Planned", "পরিকল্পিত", group = "tab"),
                        FilterCondition(TAB_SAVINGS_GOALS, "Goals", "লক্ষ্য", group = "tab"),
                        FilterCondition(TAB_PURCHASED, "Purchased", "ক্রয়কৃত", icon = Icons.Default.CheckCircle, group = "tab"),
                        FilterCondition(TAB_ALL, "All", "সকল", group = "tab")
                    )
                ),
                FilterField.SelectField(
                    id = FIELD_PRIORITY,
                    titleEn = "Priority",
                    titleBn = "অগ্রাধিকার",
                    icon = Icons.Default.Flag,
                    isMultiSelect = true,
                    items = listOf(
                        SelectItemOption(PRIORITY_HIGH, "High Priority", "উচ্চ অগ্রাধিকার"),
                        SelectItemOption(PRIORITY_MEDIUM, "Medium Priority", "মাঝারি অগ্রাধিকার"),
                        SelectItemOption(PRIORITY_LOW, "Low Priority", "স্বাভাবিক অগ্রাধিকার")
                    )
                ),
                FilterField.SelectField(
                    id = FIELD_CATEGORIES,
                    titleEn = "Categories",
                    titleBn = "ক্যাটাগরি",
                    icon = Icons.Default.Category,
                    isMultiSelect = true,
                    items = categoryOptions
                ),
                FilterField.SortField(
                    id = FIELD_SORT,
                    titleEn = "Sort Order",
                    titleBn = "সাজানোর ক্রম",
                    icon = Icons.Default.Sort,
                    options = sortOptions,
                    defaultSortId = SORT_PRIORITY
                )
            )
        )
    }

    fun getTab(state: FilterState): WishlistFilterTab {
        val active = (state[FIELD_TAB] as? FilterValue.ToggleGroup)?.activeIds ?: emptySet()
        return when (active.firstOrNull()) {
            TAB_NEXT_MONTH -> WishlistFilterTab.NEXT_MONTH
            TAB_PLANNED_MONTHS -> WishlistFilterTab.PLANNED_MONTHS
            TAB_SAVINGS_GOALS -> WishlistFilterTab.SAVINGS_GOALS
            TAB_PURCHASED -> WishlistFilterTab.PURCHASED
            TAB_ALL -> WishlistFilterTab.ALL
            else -> WishlistFilterTab.ACTIVE
        }
    }

    fun getSort(state: FilterState): WishlistSort {
        val sortId = (state[FIELD_SORT] as? FilterValue.Sort)?.sortId ?: SORT_PRIORITY
        return when (sortId) {
            SORT_AMOUNT_DESC -> WishlistSort.AMOUNT_DESC
            SORT_AMOUNT_ASC -> WishlistSort.AMOUNT_ASC
            SORT_RECENT -> WishlistSort.RECENT
            else -> WishlistSort.PRIORITY
        }
    }

    fun getSearchQuery(state: FilterState): String {
        return (state[FIELD_SEARCH] as? FilterValue.Search)?.query ?: ""
    }

    fun filterItems(
        items: List<WishlistItemWithCategory>,
        state: FilterState,
        nextYear: Int,
        nextMonth: Int
    ): List<WishlistItemWithCategory> {
        val selectedTab = getTab(state)
        val selectedSort = getSort(state)
        val searchQuery = getSearchQuery(state)

        // 1. Filter by Tab
        var list = when (selectedTab) {
            WishlistFilterTab.ACTIVE -> items.filter { !it.item.isPurchased }
            WishlistFilterTab.NEXT_MONTH -> items.filter {
                !it.item.isPurchased && (it.item.targetType == WishlistTargetType.NEXT_MONTH ||
                        (it.item.targetType == WishlistTargetType.SPECIFIC_MONTH && it.item.targetYear == nextYear && it.item.targetMonth == nextMonth))
            }
            WishlistFilterTab.PLANNED_MONTHS -> items.filter {
                !it.item.isPurchased && (it.item.targetType == WishlistTargetType.SPECIFIC_MONTH || it.item.targetType == WishlistTargetType.NEXT_MONTH)
            }
            WishlistFilterTab.SAVINGS_GOALS -> items.filter {
                !it.item.isPurchased && (it.item.targetType == WishlistTargetType.SAVINGS_GOAL || it.item.linkedGoalId != null)
            }
            WishlistFilterTab.PURCHASED -> items.filter { it.item.isPurchased }
            WishlistFilterTab.ALL -> items
        }

        // 2. Filter by Search Query
        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase()
            list = list.filter {
                it.item.title.lowercase().contains(q) ||
                        it.item.notes.lowercase().contains(q) ||
                        (it.category?.nameEn?.lowercase()?.contains(q) == true) ||
                        (it.category?.nameBn?.lowercase()?.contains(q) == true)
            }
        }

        // 3. Filter by Priority
        val priorityFilter = (state[FIELD_PRIORITY] as? FilterValue.Select)?.selectedIds ?: emptySet()
        if (priorityFilter.isNotEmpty()) {
            list = list.filter {
                it.item.priority.name.lowercase() in priorityFilter
            }
        }

        // 4. Filter by Categories
        val categoryFilter = (state[FIELD_CATEGORIES] as? FilterValue.Select)?.selectedIds ?: emptySet()
        if (categoryFilter.isNotEmpty()) {
            list = list.filter {
                it.item.categoryId != null && it.item.categoryId.toString() in categoryFilter
            }
        }

        // 5. Sort Items
        return when (selectedSort) {
            WishlistSort.PRIORITY -> list.sortedWith(
                compareBy<WishlistItemWithCategory> { it.item.isPurchased }
                    .thenBy {
                        when (it.item.priority) {
                            WishlistPriority.HIGH -> 1
                            WishlistPriority.MEDIUM -> 2
                            WishlistPriority.LOW -> 3
                        }
                    }
                    .thenByDescending { it.item.createdAt }
            )
            WishlistSort.AMOUNT_DESC -> list.sortedWith(
                compareBy<WishlistItemWithCategory> { it.item.isPurchased }
                    .thenByDescending { it.item.estimatedAmount }
            )
            WishlistSort.AMOUNT_ASC -> list.sortedWith(
                compareBy<WishlistItemWithCategory> { it.item.isPurchased }
                    .thenBy { it.item.estimatedAmount }
            )
            WishlistSort.RECENT -> list.sortedWith(
                compareBy<WishlistItemWithCategory> { it.item.isPurchased }
                    .thenByDescending { it.item.createdAt }
            )
        }
    }
}
