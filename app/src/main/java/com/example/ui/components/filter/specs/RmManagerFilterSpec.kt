package com.example.ui.components.filter.specs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.graphics.Color
import com.example.ui.components.filter.FilterCondition
import com.example.ui.components.filter.FilterField
import com.example.ui.components.filter.FilterSpec
import com.example.ui.components.filter.FilterState
import com.example.ui.components.filter.FilterValue
import com.example.ui.components.filter.SortOptionItem
import com.example.util.RmManagerHelper.RmFilterCategory
import com.example.util.RmManagerHelper.RmSortOption
import com.example.util.RmManagerHelper.RmSourceFilter
import com.example.util.RmManagerHelper.RmStatusFilter

/**
 * Filter specification for RmManagerScreen.
 * Covers orthogonal Source & Status cycles, Hide 0 balance toggle,
 * extra status qualifiers, 6-option sorting, and keyword search.
 */
object RmManagerFilterSpec {

    const val SPEC_KEY = "rm_manager"

    // Field IDs
    const val FIELD_SEARCH = "search"
    const val FIELD_SOURCE = "source"
    const val FIELD_STATUS = "status"
    const val FIELD_HIDE_ZERO = "hide_zero"
    const val FIELD_CATEGORY = "category"
    const val FIELD_SORT = "sort"

    // Source Condition IDs
    const val SOURCE_ALL = "all"
    const val SOURCE_RM_ACCOUNTS = "rm_accounts"
    const val SOURCE_RM_OTHERS = "rm_others"

    // Status Condition IDs
    const val STATUS_ALL = "all"
    const val STATUS_PENDING = "pending"
    const val STATUS_SETTLED = "settled"

    // Hide Zero Condition ID
    const val HIDE_ZERO_ID = "hide_zero"

    // Extra / Legacy Category Condition IDs
    const val CAT_ALL = "all"
    const val CAT_PENDING_ONLY = "pending_only"
    const val CAT_RM_ACCOUNTS = "rm_accounts"
    const val CAT_RM_OTHERS = "rm_others"
    const val CAT_SETTLED_ONLY = "settled_only"
    const val CAT_UNRECONCILED = "unreconciled"
    const val CAT_RECONCILED = "reconciled"
    const val CAT_HIGH_LIABILITY = "high_liability"
    const val CAT_RECENT_WEEK = "recent_week"

    // Sort IDs
    const val SORT_HIGHEST_DUE = "highest_due"
    const val SORT_LOWEST_DUE = "lowest_due"
    const val SORT_MOST_REPAID = "most_repaid"
    const val SORT_NAME_AZ = "name_az"
    const val SORT_NAME_ZA = "name_za"
    const val SORT_RECENT_ACTIVITY = "recent_activity"

    val SORT_OPTIONS: List<SortOptionItem<Any>> = listOf(
        SortOptionItem(SORT_HIGHEST_DUE, "Highest Due First", "সর্বোচ্চ বকেয়া আগে"),
        SortOptionItem(SORT_LOWEST_DUE, "Lowest Due First", "সর্বনিম্ন বকেয়া আগে"),
        SortOptionItem(SORT_MOST_REPAID, "Most Repaid First", "সর্বাধিক পরিশোধিত"),
        SortOptionItem(SORT_RECENT_ACTIVITY, "Recent Activity", "সাম্প্রতিক কার্যক্রম"),
        SortOptionItem(SORT_NAME_AZ, "Name (A → Z)", "নাম অনুযায়ী (A → Z)"),
        SortOptionItem(SORT_NAME_ZA, "Name (Z → A)", "নাম অনুযায়ী (Z → A)")
    )

    fun createSpec(): FilterSpec<Any> {
        return FilterSpec(
            key = SPEC_KEY,
            titleEn = "Filter & Sort RM Manager",
            titleBn = "আরএম ম্যানেজার ফিল্টার ও সাজানো",
            accentColor = Color(0xFFEF4444), // Crimson/Red accent for liability tracking
            fields = listOf(
                FilterField.SearchField(
                    id = FIELD_SEARCH,
                    titleEn = "Search",
                    titleBn = "অনুসন্ধান",
                    hintEn = "Search RM accounts, labels, notes...",
                    hintBn = "আরএম অ্যাকাউন্ট বা লেবেল খুঁজুন...",
                    icon = Icons.Default.Search
                ),
                FilterField.ToggleGroupField(
                    id = FIELD_SOURCE,
                    titleEn = "Source Scope",
                    titleBn = "উৎস",
                    icon = Icons.Default.Layers,
                    defaultConditionIds = setOf(SOURCE_ALL),
                    conditions = listOf(
                        FilterCondition(SOURCE_ALL, "All Sources", "সব উৎস", icon = Icons.Default.Layers, group = "source"),
                        FilterCondition(SOURCE_RM_ACCOUNTS, "RM Accounts", "আরএম অ্যাকাউন্ট", icon = Icons.Default.AccountBalance, group = "source"),
                        FilterCondition(SOURCE_RM_OTHERS, "RM Others", "আরএম অন্যান্য", icon = Icons.AutoMirrored.Filled.ReceiptLong, group = "source")
                    )
                ),
                FilterField.ToggleGroupField(
                    id = FIELD_STATUS,
                    titleEn = "Repayment Status",
                    titleBn = "পরিশোধের অবস্থা",
                    icon = Icons.Default.DoneAll,
                    defaultConditionIds = setOf(STATUS_ALL),
                    conditions = listOf(
                        FilterCondition(STATUS_ALL, "All Statuses", "সকল অবস্থা", icon = Icons.Default.DoneAll, group = "status"),
                        FilterCondition(STATUS_PENDING, "Pending Due", "বকেয়া", icon = Icons.AutoMirrored.Filled.TrendingDown, group = "status"),
                        FilterCondition(STATUS_SETTLED, "Settled", "পরিশোধিত", icon = Icons.Default.CheckCircle, group = "status")
                    )
                ),
                FilterField.ToggleGroupField(
                    id = FIELD_HIDE_ZERO,
                    titleEn = "Zero Balance",
                    titleBn = "শূন্য ব্যালেন্স",
                    icon = Icons.Default.VisibilityOff,
                    conditions = listOf(
                        FilterCondition(HIDE_ZERO_ID, "Hide 0 Balance", "০ ব্যালেন্স লুকান", icon = Icons.Default.Check)
                    )
                ),
                FilterField.ToggleGroupField(
                    id = FIELD_CATEGORY,
                    titleEn = "Additional Qualifiers",
                    titleBn = "অতিরিক্ত শর্তাবলী",
                    icon = Icons.Default.Tune,
                    defaultConditionIds = setOf(CAT_ALL),
                    conditions = listOf(
                        FilterCondition(CAT_ALL, "None", "কোনটিই নয়", icon = Icons.Default.DoneAll, group = "cat"),
                        FilterCondition(CAT_UNRECONCILED, "Unreconciled Only", "আমিলকৃত", icon = Icons.Default.History, group = "cat"),
                        FilterCondition(CAT_RECONCILED, "Reconciled Only", "মিলকৃত", icon = Icons.Default.Verified, group = "cat"),
                        FilterCondition(CAT_HIGH_LIABILITY, "High Due (> ৳10k)", "বড় দেনা (> ৳১০,০০০)", icon = Icons.Default.Payment, group = "cat"),
                        FilterCondition(CAT_RECENT_WEEK, "Active (Last 7 Days)", "গত ৭ দিনে সক্রিয়", icon = Icons.Default.Timeline, group = "cat"),
                        FilterCondition(CAT_PENDING_ONLY, "Pending Due Only", "বকেয়া দেনা", icon = Icons.AutoMirrored.Filled.TrendingDown, group = "cat"),
                        FilterCondition(CAT_RM_ACCOUNTS, "RM Accounts Only", "আরএম অ্যাকাউন্ট", icon = Icons.Default.AccountBalance, group = "cat"),
                        FilterCondition(CAT_RM_OTHERS, "RM Others Labels", "আরএম অন্যান্য লেবেল", icon = Icons.AutoMirrored.Filled.ReceiptLong, group = "cat"),
                        FilterCondition(CAT_SETTLED_ONLY, "Settled Only", "পরিশোধিত", icon = Icons.Default.CheckCircle, group = "cat")
                    )
                ),
                FilterField.SortField(
                    id = FIELD_SORT,
                    titleEn = "Sort Order",
                    titleBn = "সাজানোর ক্রম",
                    icon = Icons.Default.Sort,
                    options = SORT_OPTIONS,
                    defaultSortId = SORT_HIGHEST_DUE
                )
            )
        )
    }

    // --- State Extraction Helpers ---

    fun getSearchQuery(state: FilterState): String {
        return (state[FIELD_SEARCH] as? FilterValue.Search)?.query ?: ""
    }

    fun getSourceFilter(state: FilterState): RmSourceFilter {
        val active = (state[FIELD_SOURCE] as? FilterValue.ToggleGroup)?.activeIds
        if (active != null && active.isNotEmpty()) {
            return when (active.firstOrNull()) {
                SOURCE_RM_ACCOUNTS -> RmSourceFilter.RM_ACCOUNTS
                SOURCE_RM_OTHERS -> RmSourceFilter.RM_OTHERS
                else -> RmSourceFilter.ALL
            }
        }
        return when (getCategory(state)) {
            RmFilterCategory.RM_ACCOUNTS -> RmSourceFilter.RM_ACCOUNTS
            RmFilterCategory.RM_OTHERS -> RmSourceFilter.RM_OTHERS
            else -> RmSourceFilter.ALL
        }
    }

    fun getStatusFilter(state: FilterState): RmStatusFilter {
        val active = (state[FIELD_STATUS] as? FilterValue.ToggleGroup)?.activeIds
        if (active != null && active.isNotEmpty()) {
            return when (active.firstOrNull()) {
                STATUS_PENDING -> RmStatusFilter.PENDING
                STATUS_SETTLED -> RmStatusFilter.SETTLED
                else -> RmStatusFilter.ALL
            }
        }
        return when (getCategory(state)) {
            RmFilterCategory.PENDING_ONLY -> RmStatusFilter.PENDING
            RmFilterCategory.SETTLED_ONLY -> RmStatusFilter.SETTLED
            else -> RmStatusFilter.ALL
        }
    }

    fun getHideZero(state: FilterState): Boolean {
        val active = (state[FIELD_HIDE_ZERO] as? FilterValue.ToggleGroup)?.activeIds ?: emptySet()
        return HIDE_ZERO_ID in active
    }

    fun getCategory(state: FilterState): RmFilterCategory {
        val active = (state[FIELD_CATEGORY] as? FilterValue.ToggleGroup)?.activeIds ?: emptySet()
        return when (active.firstOrNull()) {
            CAT_PENDING_ONLY -> RmFilterCategory.PENDING_ONLY
            CAT_RM_ACCOUNTS -> RmFilterCategory.RM_ACCOUNTS
            CAT_RM_OTHERS -> RmFilterCategory.RM_OTHERS
            CAT_SETTLED_ONLY -> RmFilterCategory.SETTLED_ONLY
            CAT_UNRECONCILED -> RmFilterCategory.UNRECONCILED
            CAT_RECONCILED -> RmFilterCategory.RECONCILED
            CAT_HIGH_LIABILITY -> RmFilterCategory.HIGH_LIABILITY
            CAT_RECENT_WEEK -> RmFilterCategory.RECENT_WEEK
            else -> RmFilterCategory.ALL
        }
    }

    fun getSort(state: FilterState): RmSortOption {
        val sortId = (state[FIELD_SORT] as? FilterValue.Sort)?.sortId
        return when (sortId) {
            SORT_LOWEST_DUE -> RmSortOption.LOWEST_DUE
            SORT_MOST_REPAID -> RmSortOption.MOST_REPAID
            SORT_NAME_AZ -> RmSortOption.NAME_AZ
            SORT_NAME_ZA -> RmSortOption.NAME_ZA
            SORT_RECENT_ACTIVITY -> RmSortOption.RECENT_ACTIVITY
            else -> RmSortOption.HIGHEST_DUE
        }
    }

    // --- State Mutation Helpers ---

    fun withSourceFilter(state: FilterState, source: RmSourceFilter): FilterState {
        val id = when (source) {
            RmSourceFilter.RM_ACCOUNTS -> SOURCE_RM_ACCOUNTS
            RmSourceFilter.RM_OTHERS -> SOURCE_RM_OTHERS
            RmSourceFilter.ALL -> SOURCE_ALL
        }
        val newMap = state.toMutableMap()
        newMap[FIELD_SOURCE] = FilterValue.ToggleGroup(setOf(id))
        val legacyCat = getCategory(state)
        if (legacyCat == RmFilterCategory.RM_ACCOUNTS || legacyCat == RmFilterCategory.RM_OTHERS) {
            newMap[FIELD_CATEGORY] = FilterValue.ToggleGroup(setOf(CAT_ALL))
        }
        return newMap
    }

    fun withStatusFilter(state: FilterState, status: RmStatusFilter): FilterState {
        val id = when (status) {
            RmStatusFilter.PENDING -> STATUS_PENDING
            RmStatusFilter.SETTLED -> STATUS_SETTLED
            RmStatusFilter.ALL -> STATUS_ALL
        }
        val newMap = state.toMutableMap()
        newMap[FIELD_STATUS] = FilterValue.ToggleGroup(setOf(id))
        val legacyCat = getCategory(state)
        if (legacyCat == RmFilterCategory.PENDING_ONLY || legacyCat == RmFilterCategory.SETTLED_ONLY) {
            newMap[FIELD_CATEGORY] = FilterValue.ToggleGroup(setOf(CAT_ALL))
        }
        return newMap
    }

    fun withHideZero(state: FilterState, hideZero: Boolean): FilterState {
        val newMap = state.toMutableMap()
        if (hideZero) {
            newMap[FIELD_HIDE_ZERO] = FilterValue.ToggleGroup(setOf(HIDE_ZERO_ID))
        } else {
            newMap.remove(FIELD_HIDE_ZERO)
        }
        return newMap
    }

    fun withSort(state: FilterState, sort: RmSortOption): FilterState {
        val sortId = when (sort) {
            RmSortOption.LOWEST_DUE -> SORT_LOWEST_DUE
            RmSortOption.MOST_REPAID -> SORT_MOST_REPAID
            RmSortOption.NAME_AZ -> SORT_NAME_AZ
            RmSortOption.NAME_ZA -> SORT_NAME_ZA
            RmSortOption.RECENT_ACTIVITY -> SORT_RECENT_ACTIVITY
            RmSortOption.HIGHEST_DUE -> SORT_HIGHEST_DUE
        }
        val newMap = state.toMutableMap()
        newMap[FIELD_SORT] = FilterValue.Sort(sortId)
        return newMap
    }
}

