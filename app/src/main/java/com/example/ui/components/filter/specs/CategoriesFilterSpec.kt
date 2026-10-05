package com.example.ui.components.filter.specs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.ui.graphics.Color
import com.example.data.model.CategoryType
import com.example.ui.components.filter.FilterCondition
import com.example.ui.components.filter.FilterField
import com.example.ui.components.filter.FilterSpec
import com.example.ui.components.filter.FilterState
import com.example.ui.components.filter.FilterValue
import com.example.ui.components.filter.SortOptionItem
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
    const val FIELD_SORT = "sort"

    // Type mode IDs
    const val TYPE_EXPENSE = "expense"
    const val TYPE_ALL = "all"
    const val TYPE_INCOME = "income"

    // Hierarchy IDs
    const val HIERARCHY_ALL = "all"
    const val HIERARCHY_ONLY_GROUPS = "only_groups"
    const val HIERARCHY_ONLY_CATEGORIES = "only_categories"

    fun createSpec(): FilterSpec<Any> {
        val sortOptions: List<SortOptionItem<Any>> = listOf(
            SortOptionItem("default", "Default Order", "স্বাভাবিক ক্রম"),
            SortOptionItem("budget_high_to_low", "Budget Limit: High to Low", "বাজেট সীমা: বেশি থেকে কম"),
            SortOptionItem("budget_low_to_high", "Budget Limit: Low to High", "বাজেট সীমা: কম থেকে বেশি"),
            SortOptionItem("most_used", "Most Used / Frequent", "সর্বাধিক ব্যবহৃত"),
            SortOptionItem("least_used", "Least Used", "কম ব্যবহৃত"),
            SortOptionItem("name_az", "Name: A to Z", "নাম: A থেকে Z"),
            SortOptionItem("name_za", "Name: Z to A", "নাম: Z থেকে A")
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
                        FilterCondition(HIERARCHY_ONLY_GROUPS, "Only Groups", "শুধুমাত্র গ্রুপ", group = "hierarchy"),
                        FilterCondition(HIERARCHY_ONLY_CATEGORIES, "Categories", "ক্যাটাগরি", group = "hierarchy")
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
}
