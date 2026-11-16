package com.example.ui.components.filter.specs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.graphics.Color
import com.example.data.model.CategoryType
import com.example.ui.components.filter.FilterCondition
import com.example.ui.components.filter.FilterField
import com.example.ui.components.filter.FilterSpec
import com.example.ui.components.filter.FilterState
import com.example.ui.components.filter.FilterValue
import com.example.ui.components.filter.SortOptionItem
import com.example.ui.screens.CategoryActiveStatusFilter
import com.example.ui.screens.CategorySortFilter
import com.example.ui.screens.CategoryViewHierarchyFilter

/**
 * Filter specification for CategoriesScreen.
 */
object CategoriesFilterSpec {

    const val SPEC_KEY = "categories"

    // Field IDs
    const val FIELD_SEARCH = "search"
    const val FIELD_TYPE_MODE = "type_mode"
    const val FIELD_HIERARCHY = "hierarchy"
    const val FIELD_STATUS = "status"
    const val FIELD_EXCLUDE_ZERO = "exclude_zero"
    const val FIELD_SORT = "sort"

    // Type mode IDs
    const val TYPE_EXPENSE = "expense"
    const val TYPE_ALL = "all"
    const val TYPE_INCOME = "income"

    // Hierarchy IDs
    const val HIERARCHY_ALL = "all"
    const val HIERARCHY_ONLY_GROUPS = "only_groups"
    const val HIERARCHY_ONLY_CATEGORIES = "only_categories"

    // Status IDs
    const val STATUS_ALL = "all"
    const val STATUS_ACTIVE = "active"
    const val STATUS_INACTIVE = "inactive"

    // Exclude Zero Budget ID
    const val EXCLUDE_ZERO_ID = "exclude"

    fun createSpec(): FilterSpec<Any> {
        val sortOptions: List<SortOptionItem<Any>> = listOf(
            SortOptionItem("default", "Default Order", "স্বাভাবিক ক্রম"),
            SortOptionItem("budget_high_to_low", "Budget Limit: High → Low", "বাজেট সীমা: বেশি → কম"),
            SortOptionItem("budget_low_to_high", "Budget Limit: Low → High", "বাজেট সীমা: কম → বেশি"),
            SortOptionItem("most_used", "Most Used / Frequent", "সর্বাধিক ব্যবহৃত"),
            SortOptionItem("least_used", "Least Used", "কম ব্যবহৃত"),
            SortOptionItem("name_az", "Name: A → Z", "নাম: A → Z"),
            SortOptionItem("name_za", "Name: Z → A", "নাম: Z → A")
        )

        return FilterSpec(
            key = SPEC_KEY,
            titleEn = "Filter & Sort Categories",
            titleBn = "ক্যাটাগরি ফিল্টার ও সাজানো",
            accentColor = Color(0xFF10B981),
            fields = listOf(
                FilterField.SearchField(
                    id = FIELD_SEARCH,
                    titleEn = "Search",
                    titleBn = "অনুসন্ধান",
                    hintEn = "Search categories...",
                    hintBn = "ক্যাটাগরি খুঁজুন...",
                    icon = Icons.Default.Search
                ),
                FilterField.ToggleGroupField(
                    id = FIELD_TYPE_MODE,
                    titleEn = "Category Type",
                    titleBn = "ক্যাটাগরির ধরন",
                    icon = Icons.Default.Layers,
                    conditions = listOf(
                        FilterCondition(TYPE_EXPENSE, "Expenses", "ব্যয়", icon = Icons.Default.TrendingDown, group = "type_mode"),
                        FilterCondition(TYPE_ALL, "All", "সব", icon = Icons.Default.Layers, group = "type_mode"),
                        FilterCondition(TYPE_INCOME, "Incomes", "আয়", icon = Icons.Default.TrendingUp, group = "type_mode")
                    )
                ),
                FilterField.ToggleGroupField(
                    id = FIELD_HIERARCHY,
                    titleEn = "View Scope",
                    titleBn = "প্রদর্শনের পরিধি",
                    icon = Icons.Default.Layers,
                    conditions = listOf(
                        FilterCondition(HIERARCHY_ALL, "All", "সব", group = "hierarchy"),
                        FilterCondition(HIERARCHY_ONLY_GROUPS, "Groups", "গ্রুপ", group = "hierarchy"),
                        FilterCondition(HIERARCHY_ONLY_CATEGORIES, "Categories", "ক্যাটাগরি", group = "hierarchy")
                    )
                ),
                FilterField.ToggleGroupField(
                    id = FIELD_STATUS,
                    titleEn = "Category Status",
                    titleBn = "ক্যাটাগরির অবস্থা",
                    icon = Icons.Default.CheckCircle,
                    conditions = listOf(
                        FilterCondition(STATUS_ALL, "All Statuses", "সকল অবস্থা", group = "status"),
                        FilterCondition(STATUS_ACTIVE, "Active Only", "সক্রিয় শুধুমাত্র", icon = Icons.Default.CheckCircle, group = "status"),
                        FilterCondition(STATUS_INACTIVE, "Inactive Only", "নিষ্ক্রিয় শুধুমাত্র", icon = Icons.Default.VisibilityOff, group = "status")
                    )
                ),
                FilterField.ToggleGroupField(
                    id = FIELD_EXCLUDE_ZERO,
                    titleEn = "Zero Budget",
                    titleBn = "শূন্য বাজেট",
                    icon = Icons.Default.Check,
                    conditions = listOf(
                        FilterCondition(EXCLUDE_ZERO_ID, "Hide 0 Budget", "০ বাজেট লুকান", icon = Icons.Default.Check)
                    )
                ),
                FilterField.SortField(
                    id = FIELD_SORT,
                    titleEn = "Sort Order",
                    titleBn = "সাজানোর ক্রম",
                    icon = Icons.Default.Sort,
                    options = sortOptions,
                    defaultSortId = "default"
                )
            )
        )
    }

    fun getTypeFilter(state: FilterState): CategoryType? {
        val active = (state[FIELD_TYPE_MODE] as? FilterValue.ToggleGroup)?.activeIds ?: emptySet()
        return when (active.firstOrNull()) {
            TYPE_EXPENSE -> CategoryType.EXPENSE
            TYPE_INCOME -> CategoryType.INCOME
            TYPE_ALL -> null
            else -> null
        }
    }

    fun getHierarchyFilter(state: FilterState): CategoryViewHierarchyFilter {
        val active = (state[FIELD_HIERARCHY] as? FilterValue.ToggleGroup)?.activeIds ?: emptySet()
        return when (active.firstOrNull()) {
            HIERARCHY_ONLY_GROUPS -> CategoryViewHierarchyFilter.ONLY_GROUPS
            HIERARCHY_ONLY_CATEGORIES -> CategoryViewHierarchyFilter.ONLY_CATEGORIES
            else -> CategoryViewHierarchyFilter.ALL
        }
    }

    fun getStatusFilter(state: FilterState): CategoryActiveStatusFilter {
        val active = (state[FIELD_STATUS] as? FilterValue.ToggleGroup)?.activeIds ?: emptySet()
        return when (active.firstOrNull()) {
            STATUS_ACTIVE -> CategoryActiveStatusFilter.ACTIVE_ONLY
            STATUS_INACTIVE -> CategoryActiveStatusFilter.INACTIVE_ONLY
            else -> CategoryActiveStatusFilter.ALL
        }
    }

    fun getExcludeZeroBudget(state: FilterState): Boolean {
        val active = (state[FIELD_EXCLUDE_ZERO] as? FilterValue.ToggleGroup)?.activeIds ?: emptySet()
        return active.contains(EXCLUDE_ZERO_ID)
    }

    fun getSortFilter(state: FilterState): CategorySortFilter {
        val sortId = (state[FIELD_SORT] as? FilterValue.Sort)?.sortId ?: "default"
        return when (sortId) {
            "budget_high_to_low" -> CategorySortFilter.BUDGET_HIGH_TO_LOW
            "budget_low_to_high" -> CategorySortFilter.BUDGET_LOW_TO_HIGH
            "most_used" -> CategorySortFilter.MOST_USED
            "least_used" -> CategorySortFilter.LEAST_USED
            "name_az" -> CategorySortFilter.NAME_AZ
            "name_za" -> CategorySortFilter.NAME_ZA
            else -> CategorySortFilter.DEFAULT
        }
    }

    fun getSearchQuery(state: FilterState): String {
        return (state[FIELD_SEARCH] as? FilterValue.Search)?.query ?: ""
    }

    fun withType(state: FilterState, type: CategoryType?): FilterState {
        val typeId = when (type) {
            CategoryType.EXPENSE -> TYPE_EXPENSE
            CategoryType.INCOME -> TYPE_INCOME
            null -> TYPE_ALL
        }
        return state + (FIELD_TYPE_MODE to FilterValue.ToggleGroup(setOf(typeId)))
    }

    fun withHierarchy(state: FilterState, hierarchy: CategoryViewHierarchyFilter): FilterState {
        val id = when (hierarchy) {
            CategoryViewHierarchyFilter.ALL -> HIERARCHY_ALL
            CategoryViewHierarchyFilter.ONLY_GROUPS -> HIERARCHY_ONLY_GROUPS
            CategoryViewHierarchyFilter.ONLY_CATEGORIES -> HIERARCHY_ONLY_CATEGORIES
        }
        return state + (FIELD_HIERARCHY to FilterValue.ToggleGroup(setOf(id)))
    }

    fun withStatus(state: FilterState, status: CategoryActiveStatusFilter): FilterState {
        val id = when (status) {
            CategoryActiveStatusFilter.ALL -> STATUS_ALL
            CategoryActiveStatusFilter.ACTIVE_ONLY -> STATUS_ACTIVE
            CategoryActiveStatusFilter.INACTIVE_ONLY -> STATUS_INACTIVE
        }
        return state + (FIELD_STATUS to FilterValue.ToggleGroup(setOf(id)))
    }

    fun withExcludeZero(state: FilterState, excludeZero: Boolean): FilterState {
        val set = if (excludeZero) setOf(EXCLUDE_ZERO_ID) else emptySet()
        return state + (FIELD_EXCLUDE_ZERO to FilterValue.ToggleGroup(set))
    }

    fun withSort(state: FilterState, sort: CategorySortFilter): FilterState {
        return state + (FIELD_SORT to FilterValue.Sort(sort.name.lowercase()))
    }
}
