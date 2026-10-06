package com.example.ui.components.filter.specs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Verified
import androidx.compose.ui.graphics.Color
import com.example.ui.components.filter.FilterCondition
import com.example.ui.components.filter.FilterField
import com.example.ui.components.filter.FilterSpec
import com.example.ui.components.filter.FilterState
import com.example.ui.components.filter.FilterValue
import com.example.ui.components.filter.SortOptionItem
import com.example.util.RmManagerHelper.RmFilterCategory
import com.example.util.RmManagerHelper.RmSortOption

/**
 * Filter specification for RmManagerScreen.
 * Covers RM entity status categories, sorting options, and keyword search.
 */
object RmManagerFilterSpec {

    const val SPEC_KEY = "rm_manager"

    // Field IDs
    const val FIELD_SEARCH = "search"
    const val FIELD_CATEGORY = "category"
    const val FIELD_SORT = "sort"

    // Category Condition IDs
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
    const val SORT_RECENT_ACTIVITY = "recent_activity"

    val SORT_OPTIONS: List<SortOptionItem<Any>> = listOf(
        SortOptionItem(SORT_HIGHEST_DUE, "Highest Due First", "সর্বোচ্চ বকেয়া আগে"),
        SortOptionItem(SORT_LOWEST_DUE, "Lowest Due First", "সর্বনিম্ন বকেয়া আগে"),
        SortOptionItem(SORT_MOST_REPAID, "Most Repaid First", "সর্বাধিক পরিশোধিত"),
        SortOptionItem(SORT_NAME_AZ, "Name (A to Z)", "নাম অনুযায়ী (ক-য়)"),
        SortOptionItem(SORT_RECENT_ACTIVITY, "Recent Activity", "সাম্প্রতিক কার্যক্রম")
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
                    id = FIELD_CATEGORY,
                    titleEn = "Status Category",
                    titleBn = "অবস্থা বিভাগ",
                    icon = Icons.Default.Tune,
                    conditions = listOf(
                        FilterCondition(CAT_ALL, "All Entities", "সকল আইটেম", icon = Icons.Default.DoneAll, group = "cat"),
                        FilterCondition(CAT_PENDING_ONLY, "Pending Due Only", "বকেয়া দেনা", icon = Icons.AutoMirrored.Filled.TrendingDown, group = "cat"),
                        FilterCondition(CAT_RM_ACCOUNTS, "RM Accounts Only", "আরএম অ্যাকাউন্ট", icon = Icons.Default.AccountBalance, group = "cat"),
                        FilterCondition(CAT_RM_OTHERS, "RM Others Labels", "আরএম অন্যান্য লেবেল", icon = Icons.AutoMirrored.Filled.ReceiptLong, group = "cat"),
                        FilterCondition(CAT_SETTLED_ONLY, "Settled Only", "পরিশোধিত", icon = Icons.Default.CheckCircle, group = "cat"),
                        FilterCondition(CAT_UNRECONCILED, "Unreconciled Only", "আমিলকৃত", icon = Icons.Default.History, group = "cat"),
                        FilterCondition(CAT_RECONCILED, "Reconciled Only", "মিলকৃত", icon = Icons.Default.Verified, group = "cat"),
                        FilterCondition(CAT_HIGH_LIABILITY, "High Due (> ৳10k)", "বড় দেনা (> ৳১০,০০০)", icon = Icons.Default.Payment, group = "cat"),
                        FilterCondition(CAT_RECENT_WEEK, "Active (Last 7 Days)", "গত ৭ দিনে সক্রিয়", icon = Icons.Default.Timeline, group = "cat")
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
            SORT_RECENT_ACTIVITY -> RmSortOption.RECENT_ACTIVITY
            else -> RmSortOption.HIGHEST_DUE
        }
    }
}
