package com.example.ui.components.filter.specs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.ui.graphics.Color
import com.example.data.model.SavingsGoalWithDetails
import com.example.ui.components.filter.FilterCondition
import com.example.ui.components.filter.FilterField
import com.example.ui.components.filter.FilterSpec
import com.example.ui.components.filter.FilterState
import com.example.ui.components.filter.FilterValue
import com.example.ui.components.filter.SortOptionItem
import com.example.ui.screens.GoalFilterType

/**
 * Filter specification for SavingsGoalsScreen.
 */
object SavingsGoalsFilterSpec {

    const val SPEC_KEY = "savings_goals"

    // Field IDs
    const val FIELD_SEARCH = "search"
    const val FIELD_STATUS = "status"
    const val FIELD_SORT = "sort"

    // Status condition IDs
    const val STATUS_ALL = "all"
    const val STATUS_ACTIVE = "active"
    const val STATUS_COMPLETED = "completed"
    const val STATUS_DEFICIT = "deficit"

    // Sort option IDs
    const val SORT_DEFAULT = "default"
    const val SORT_TARGET_DESC = "target_desc"
    const val SORT_TARGET_ASC = "target_asc"
    const val SORT_SAVED_DESC = "saved_desc"
    const val SORT_PROGRESS_DESC = "progress_desc"
    const val SORT_NAME_AZ = "name_az"

    fun createSpec(): FilterSpec<Any> {
        val sortOptions: List<SortOptionItem<Any>> = listOf(
            SortOptionItem(SORT_DEFAULT, "Default Order", "স্বাভাবিক ক্রম"),
            SortOptionItem(SORT_TARGET_DESC, "Target: High → Low", "লক্ষ্য: বেশি → কম"),
            SortOptionItem(SORT_TARGET_ASC, "Target: Low → High", "লক্ষ্য: কম → বেশি"),
            SortOptionItem(SORT_SAVED_DESC, "Saved: High → Low", "সঞ্চিত: বেশি → কম"),
            SortOptionItem(SORT_PROGRESS_DESC, "Progress: High → Low", "অগ্রগতি: বেশি → কম"),
            SortOptionItem(SORT_NAME_AZ, "Name: A to Z", "নাম: A থেকে Z")
        )

        return FilterSpec(
            key = SPEC_KEY,
            titleEn = "Filter & Sort Savings Goals",
            titleBn = "সঞ্চয় লক্ষ্য ফিল্টার ও সাজানো",
            accentColor = Color(0xFF10B981), // Emerald accent
            fields = listOf(
                FilterField.SearchField(
                    id = FIELD_SEARCH,
                    titleEn = "Search",
                    titleBn = "অনুসন্ধান",
                    hintEn = "Search savings goals...",
                    hintBn = "সঞ্চয় লক্ষ্য খুঁজুন...",
                    icon = Icons.Default.Search
                ),
                FilterField.ToggleGroupField(
                    id = FIELD_STATUS,
                    titleEn = "Status Scope",
                    titleBn = "অবস্থা",
                    icon = Icons.Default.Layers,
                    conditions = listOf(
                        FilterCondition(STATUS_ALL, "All", "সকল", group = "status"),
                        FilterCondition(STATUS_ACTIVE, "Active", "চলমান", icon = Icons.Default.PlayArrow, group = "status"),
                        FilterCondition(STATUS_COMPLETED, "Completed", "অর্জিত", icon = Icons.Default.CheckCircle, group = "status"),
                        FilterCondition(STATUS_DEFICIT, "Deficit", "ঘাটতি", icon = Icons.Default.ErrorOutline, group = "status")
                    )
                ),
                FilterField.SortField(
                    id = FIELD_SORT,
                    titleEn = "Sort Order",
                    titleBn = "সাজানোর ক্রম",
                    icon = Icons.Default.Sort,
                    options = sortOptions,
                    defaultSortId = SORT_DEFAULT
                )
            )
        )
    }

    fun getStatus(state: FilterState): GoalFilterType {
        val active = (state[FIELD_STATUS] as? FilterValue.ToggleGroup)?.activeIds ?: emptySet()
        return when (active.firstOrNull()) {
            STATUS_ACTIVE -> GoalFilterType.ACTIVE
            STATUS_COMPLETED -> GoalFilterType.COMPLETED
            STATUS_DEFICIT -> GoalFilterType.DEFICIT
            else -> GoalFilterType.ALL
        }
    }

    fun getSearchQuery(state: FilterState): String {
        return (state[FIELD_SEARCH] as? FilterValue.Search)?.query ?: ""
    }

    fun getSort(state: FilterState): String {
        return (state[FIELD_SORT] as? FilterValue.Sort)?.sortId ?: SORT_DEFAULT
    }

    fun filterGoals(
        goals: List<SavingsGoalWithDetails>,
        state: FilterState
    ): List<SavingsGoalWithDetails> {
        val status = getStatus(state)
        val query = getSearchQuery(state).trim()
        val sortId = getSort(state)

        var list = when (status) {
            GoalFilterType.ALL -> goals
            GoalFilterType.ACTIVE -> goals.filter { !it.goal.isCompleted }
            GoalFilterType.COMPLETED -> goals.filter { it.goal.isCompleted }
            GoalFilterType.DEFICIT -> goals.filter { it.hasDeficit }
        }

        if (query.isNotEmpty()) {
            list = list.filter {
                it.goal.name.contains(query, ignoreCase = true) ||
                it.goal.nameBn.contains(query, ignoreCase = true) ||
                it.goal.notes.contains(query, ignoreCase = true)
            }
        }

        return when (sortId) {
            SORT_TARGET_DESC -> list.sortedByDescending { it.goal.targetAmount }
            SORT_TARGET_ASC -> list.sortedBy { it.goal.targetAmount }
            SORT_SAVED_DESC -> list.sortedByDescending { it.effectiveSaved }
            SORT_PROGRESS_DESC -> list.sortedByDescending { it.progressPercent }
            SORT_NAME_AZ -> list.sortedBy { it.goal.name.lowercase() }
            else -> list
        }
    }
}
