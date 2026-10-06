package com.example.ui.components.filter.specs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.graphics.Color
import com.example.data.model.AccountType
import com.example.data.model.LanguageMode
import com.example.ui.components.filter.FilterCondition
import com.example.ui.components.filter.FilterField
import com.example.ui.components.filter.FilterSpec
import com.example.ui.components.filter.FilterState
import com.example.ui.components.filter.FilterValue
import com.example.ui.components.filter.SortOptionItem
import com.example.ui.components.filter.UnifiedFilterChipItem
import com.example.ui.screens.AccountActiveStatusFilter
import com.example.ui.screens.AccountSortFilter
import com.example.ui.screens.AccountViewHierarchyFilter

/**
 * Filter specification for AccountsScreen.
 */
object AccountsFilterSpec {

    const val SPEC_KEY = "accounts"

    // Field IDs
    const val FIELD_SEARCH = "search"
    const val FIELD_TYPE = "type"
    const val FIELD_HIERARCHY = "hierarchy"
    const val FIELD_STATUS = "status"
    const val FIELD_EXCLUDE_ZERO = "exclude_zero"
    const val FIELD_SORT = "sort"

    // Type condition IDs
    const val TYPE_ALL = "all"
    const val TYPE_ASSET = "asset"
    const val TYPE_LIABILITY = "liability"

    // Hierarchy condition IDs
    const val HIERARCHY_ALL = "all"
    const val HIERARCHY_ONLY_GROUPS = "only_groups"
    const val HIERARCHY_EXCLUDED = "excluded"
    const val HIERARCHY_ONLY_ACCOUNTS = "only_accounts"

    // Status condition IDs
    const val STATUS_ALL = "all"
    const val STATUS_ACTIVE = "active"
    const val STATUS_INACTIVE = "inactive"

    // Exclude Zero condition ID
    const val EXCLUDE_ZERO_ID = "exclude"

    // Sort IDs
    const val SORT_DEFAULT = "default"
    const val SORT_AMOUNT_DESC = "amount_desc"
    const val SORT_AMOUNT_ASC = "amount_asc"
    const val SORT_MOST_USED = "most_used"
    const val SORT_LEAST_USED = "least_used"
    const val SORT_NAME_AZ = "name_az"
    const val SORT_NAME_ZA = "name_za"

    val SORT_OPTIONS: List<SortOptionItem<Any>> = listOf(
        SortOptionItem(SORT_DEFAULT, "Default Order", "পূর্বনির্ধারিত ক্রম"),
        SortOptionItem(SORT_AMOUNT_DESC, "Balance: High → Low", "ব্যালেন্স: বেশি → কম"),
        SortOptionItem(SORT_AMOUNT_ASC, "Balance: Low → High", "ব্যালেন্স: কম → বেশি"),
        SortOptionItem(SORT_MOST_USED, "Most Used", "সর্বাধিক ব্যবহৃত"),
        SortOptionItem(SORT_LEAST_USED, "Least Used", "সর্বনিম্ন ব্যবহৃত"),
        SortOptionItem(SORT_NAME_AZ, "Name: A → Z", "নাম: ক → য়"),
        SortOptionItem(SORT_NAME_ZA, "Name: Z → A", "নাম: য় → ক")
    )

    fun createSpec(): FilterSpec<Any> {
        return FilterSpec(
            key = SPEC_KEY,
            titleEn = "Filter & Sort Accounts",
            titleBn = "অ্যাকাউন্ট ফিল্টার ও সাজানো",
            accentColor = Color(0xFF10B981), // Emerald
            fields = listOf(
                FilterField.SearchField(
                    id = FIELD_SEARCH,
                    titleEn = "Search",
                    titleBn = "অনুসন্ধান",
                    hintEn = "Search accounts...",
                    hintBn = "অ্যাকাউন্ট খুঁজুন...",
                    icon = Icons.Default.Search
                ),
                FilterField.ToggleGroupField(
                    id = FIELD_TYPE,
                    titleEn = "Account Nature",
                    titleBn = "অ্যাকাউন্টের প্রকৃতি",
                    icon = Icons.Default.AccountBalance,
                    conditions = listOf(
                        FilterCondition(TYPE_ALL, "All Types", "সকল প্রকার", icon = Icons.Default.AccountBalance, group = "type"),
                        FilterCondition(TYPE_ASSET, "Assets", "সম্পদ (Assets)", icon = Icons.AutoMirrored.Filled.TrendingUp, group = "type"),
                        FilterCondition(TYPE_LIABILITY, "Liabilities", "দায় (Liabilities)", icon = Icons.AutoMirrored.Filled.TrendingDown, group = "type")
                    )
                ),
                FilterField.ToggleGroupField(
                    id = FIELD_HIERARCHY,
                    titleEn = "View Scope",
                    titleBn = "প্রদর্শন পরিধি",
                    icon = Icons.Default.Layers,
                    conditions = listOf(
                        FilterCondition(HIERARCHY_ALL, "All", "সব", icon = Icons.Default.Layers, group = "hierarchy"),
                        FilterCondition(HIERARCHY_ONLY_GROUPS, "Only Groups", "শুধুমাত্র গ্রুপ", icon = Icons.Default.Folder, group = "hierarchy"),
                        FilterCondition(HIERARCHY_EXCLUDED, "Excluded", "বর্জিত", icon = Icons.Default.VisibilityOff, group = "hierarchy"),
                        FilterCondition(HIERARCHY_ONLY_ACCOUNTS, "Only Accounts", "শুধুমাত্র অ্যাকাউন্ট", icon = Icons.Default.AccountBalance, group = "hierarchy")
                    )
                ),
                FilterField.ToggleGroupField(
                    id = FIELD_STATUS,
                    titleEn = "Account Status",
                    titleBn = "অ্যাকাউন্টের অবস্থা",
                    icon = Icons.Default.CheckCircle,
                    conditions = listOf(
                        FilterCondition(STATUS_ALL, "All Statuses", "সকল অবস্থা", group = "status"),
                        FilterCondition(STATUS_ACTIVE, "Active Only", "সক্রিয় শুধুমাত্র", icon = Icons.Default.CheckCircle, group = "status"),
                        FilterCondition(STATUS_INACTIVE, "Inactive Only", "নিষ্ক্রিয় শুধুমাত্র", icon = Icons.Default.VisibilityOff, group = "status")
                    )
                ),
                FilterField.ToggleGroupField(
                    id = FIELD_EXCLUDE_ZERO,
                    titleEn = "Zero Balance",
                    titleBn = "শূন্য ব্যালেন্স",
                    icon = Icons.Default.Check,
                    conditions = listOf(
                        FilterCondition(EXCLUDE_ZERO_ID, "Exclude Zero Balance", "শূন্য ব্যালেন্স বাদ দিন", icon = Icons.Default.Check)
                    )
                ),
                FilterField.SortField(
                    id = FIELD_SORT,
                    titleEn = "Sort Order",
                    titleBn = "সাজানোর ক্রম",
                    icon = Icons.Default.Sort,
                    options = SORT_OPTIONS,
                    defaultSortId = SORT_DEFAULT
                )
            )
        )
    }

    // Helper Getters

    fun getType(state: FilterState): AccountType? {
        val active = (state[FIELD_TYPE] as? FilterValue.ToggleGroup)?.activeIds ?: emptySet()
        return when (active.firstOrNull()) {
            TYPE_ASSET -> AccountType.ASSET
            TYPE_LIABILITY -> AccountType.LIABILITY
            else -> null
        }
    }

    fun getHierarchy(state: FilterState): AccountViewHierarchyFilter {
        val active = (state[FIELD_HIERARCHY] as? FilterValue.ToggleGroup)?.activeIds ?: emptySet()
        return when (active.firstOrNull()) {
            HIERARCHY_ONLY_GROUPS -> AccountViewHierarchyFilter.ONLY_GROUPS
            HIERARCHY_EXCLUDED -> AccountViewHierarchyFilter.EXCLUDED
            HIERARCHY_ONLY_ACCOUNTS -> AccountViewHierarchyFilter.ONLY_ACCOUNTS
            else -> AccountViewHierarchyFilter.ALL
        }
    }

    fun getStatus(state: FilterState): AccountActiveStatusFilter {
        val active = (state[FIELD_STATUS] as? FilterValue.ToggleGroup)?.activeIds ?: emptySet()
        return when (active.firstOrNull()) {
            STATUS_ACTIVE -> AccountActiveStatusFilter.ACTIVE_ONLY
            STATUS_INACTIVE -> AccountActiveStatusFilter.INACTIVE_ONLY
            else -> AccountActiveStatusFilter.ALL
        }
    }

    fun getExcludeZeroBalance(state: FilterState): Boolean {
        val active = (state[FIELD_EXCLUDE_ZERO] as? FilterValue.ToggleGroup)?.activeIds ?: emptySet()
        return active.contains(EXCLUDE_ZERO_ID)
    }

    fun getSort(state: FilterState): AccountSortFilter {
        val sortId = (state[FIELD_SORT] as? FilterValue.Sort)?.sortId ?: SORT_DEFAULT
        return when (sortId) {
            SORT_AMOUNT_DESC -> AccountSortFilter.AMOUNT_HIGH_TO_LOW
            SORT_AMOUNT_ASC -> AccountSortFilter.AMOUNT_LOW_TO_HIGH
            SORT_MOST_USED -> AccountSortFilter.MOST_USED
            SORT_LEAST_USED -> AccountSortFilter.LEAST_USED
            SORT_NAME_AZ -> AccountSortFilter.NAME_AZ
            SORT_NAME_ZA -> AccountSortFilter.NAME_ZA
            else -> AccountSortFilter.DEFAULT
        }
    }

    fun getSearchQuery(state: FilterState): String {
        return (state[FIELD_SEARCH] as? FilterValue.Search)?.query ?: ""
    }

    // Helper Setters / State Builders

    fun withType(state: FilterState, type: AccountType?): FilterState {
        val typeId = when (type) {
            AccountType.ASSET -> TYPE_ASSET
            AccountType.LIABILITY -> TYPE_LIABILITY
            else -> TYPE_ALL
        }
        return state + (FIELD_TYPE to FilterValue.ToggleGroup(setOf(typeId)))
    }

    fun withHierarchy(state: FilterState, hierarchy: AccountViewHierarchyFilter): FilterState {
        val hierarchyId = when (hierarchy) {
            AccountViewHierarchyFilter.ONLY_GROUPS -> HIERARCHY_ONLY_GROUPS
            AccountViewHierarchyFilter.EXCLUDED -> HIERARCHY_EXCLUDED
            AccountViewHierarchyFilter.ONLY_ACCOUNTS -> HIERARCHY_ONLY_ACCOUNTS
            AccountViewHierarchyFilter.ALL -> HIERARCHY_ALL
        }
        return state + (FIELD_HIERARCHY to FilterValue.ToggleGroup(setOf(hierarchyId)))
    }

    fun withStatus(state: FilterState, status: AccountActiveStatusFilter): FilterState {
        val statusId = when (status) {
            AccountActiveStatusFilter.ACTIVE_ONLY -> STATUS_ACTIVE
            AccountActiveStatusFilter.INACTIVE_ONLY -> STATUS_INACTIVE
            AccountActiveStatusFilter.ALL -> STATUS_ALL
        }
        return state + (FIELD_STATUS to FilterValue.ToggleGroup(setOf(statusId)))
    }

    fun withExcludeZero(state: FilterState, excludeZero: Boolean): FilterState {
        val set = if (excludeZero) setOf(EXCLUDE_ZERO_ID) else emptySet()
        return state + (FIELD_EXCLUDE_ZERO to FilterValue.ToggleGroup(set))
    }

    fun withSort(state: FilterState, sort: AccountSortFilter): FilterState {
        val sortId = when (sort) {
            AccountSortFilter.AMOUNT_HIGH_TO_LOW -> SORT_AMOUNT_DESC
            AccountSortFilter.AMOUNT_LOW_TO_HIGH -> SORT_AMOUNT_ASC
            AccountSortFilter.MOST_USED -> SORT_MOST_USED
            AccountSortFilter.LEAST_USED -> SORT_LEAST_USED
            AccountSortFilter.NAME_AZ -> SORT_NAME_AZ
            AccountSortFilter.NAME_ZA -> SORT_NAME_ZA
            AccountSortFilter.DEFAULT -> SORT_DEFAULT
        }
        return state + (FIELD_SORT to FilterValue.Sort(sortId))
    }

    fun withSearchQuery(state: FilterState, query: String): FilterState {
        return if (query.isBlank()) {
            state - FIELD_SEARCH
        } else {
            state + (FIELD_SEARCH to FilterValue.Search(query))
        }
    }

    fun getSortMenuLabel(sort: AccountSortFilter, languageMode: LanguageMode): String {
        return when (sort) {
            AccountSortFilter.DEFAULT -> if (languageMode == LanguageMode.BANGLA) "ডিফল্ট ক্রম" else "Default"
            AccountSortFilter.AMOUNT_HIGH_TO_LOW -> if (languageMode == LanguageMode.BANGLA) "ব্যালেন্স: বেশি থেকে কম" else "Balance (High to Low)"
            AccountSortFilter.AMOUNT_LOW_TO_HIGH -> if (languageMode == LanguageMode.BANGLA) "ব্যালেন্স: কম থেকে বেশি" else "Balance (Low to High)"
            AccountSortFilter.MOST_USED -> if (languageMode == LanguageMode.BANGLA) "সর্বাধিক ব্যবহৃত" else "Most Used"
            AccountSortFilter.LEAST_USED -> if (languageMode == LanguageMode.BANGLA) "সর্বনিম্ন ব্যবহৃত" else "Least Used"
            AccountSortFilter.NAME_AZ -> if (languageMode == LanguageMode.BANGLA) "নাম (ক থেকে য়)" else "Name (A to Z)"
            AccountSortFilter.NAME_ZA -> if (languageMode == LanguageMode.BANGLA) "নাম (য় থেকে ক)" else "Name (Z to A)"
        }
    }
}
