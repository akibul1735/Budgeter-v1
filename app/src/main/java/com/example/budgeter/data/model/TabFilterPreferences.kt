package com.example.budgeter.data.model

enum class DatePreset {
    ALL,
    TODAY,
    THIS_WEEK,
    THIS_MONTH,
    LAST_MONTH,
    THIS_YEAR,
    CUSTOM
}

data class LedgerFilterState(
    val selectedTypeFilters: Set<TransactionType> = emptySet(),
    val selectedCategoryIds: Set<Long> = emptySet(),
    val selectedAccountIds: Set<Long> = emptySet(),
    val selectedLabels: Set<String> = emptySet(),
    val selectedStatuses: Set<String> = emptySet(),
    val datePreset: DatePreset = DatePreset.THIS_MONTH,
    val customStartDate: Long? = null,
    val customEndDate: Long? = null,
    val minAmount: Double? = null,
    val maxAmount: Double? = null,
    val searchQuery: String = ""
) {
    val isActive: Boolean
        get() = selectedTypeFilters.isNotEmpty() ||
                selectedCategoryIds.isNotEmpty() ||
                selectedAccountIds.isNotEmpty() ||
                selectedLabels.isNotEmpty() ||
                selectedStatuses.isNotEmpty() ||
                datePreset != DatePreset.THIS_MONTH ||
                minAmount != null ||
                maxAmount != null ||
                searchQuery.isNotBlank()

    val activeFilterCount: Int
        get() {
            var count = 0
            if (selectedTypeFilters.isNotEmpty()) count += selectedTypeFilters.size
            if (selectedCategoryIds.isNotEmpty()) count += selectedCategoryIds.size
            if (selectedAccountIds.isNotEmpty()) count += selectedAccountIds.size
            if (selectedLabels.isNotEmpty()) count += selectedLabels.size
            if (selectedStatuses.isNotEmpty()) count += selectedStatuses.size
            if (datePreset != DatePreset.THIS_MONTH) count += 1
            if (minAmount != null || maxAmount != null) count += 1
            if (searchQuery.isNotBlank()) count += 1
            return count
        }
}

data class BudgetFilterState(
    val selectedCategoryIds: Set<Long> = emptySet(),
    val onlyWithBudgetOrTarget: Boolean = false,
    val onlyWithoutBudgetOrTarget: Boolean = false,
    val onlyOverspentOrOverallocated: Boolean = false,
    val onlyUnderSpentOrUnderallocated: Boolean = false,
    val onlyOnTrack: Boolean = false,
    val onlyTargetAchieved: Boolean = false,
    val onlyTargetPending: Boolean = false,
    val onlyWithActualActivity: Boolean = false,
    val onlyZeroActivity: Boolean = false,
    val onlyPositiveBalance: Boolean = false,
    val onlyZeroBalance: Boolean = false,
    val onlyNegativeBalance: Boolean = false,
    val onlyOutstandingDebt: Boolean = false,
    val onlyClearedDebt: Boolean = false,
    val excludeZeroAmounts: Boolean = false,
    val minAmount: Double? = null,
    val maxAmount: Double? = null,
    val searchQuery: String = ""
) {
    val isActive: Boolean
        get() = selectedCategoryIds.isNotEmpty() ||
                onlyWithBudgetOrTarget ||
                onlyWithoutBudgetOrTarget ||
                onlyOverspentOrOverallocated ||
                onlyUnderSpentOrUnderallocated ||
                onlyOnTrack ||
                onlyTargetAchieved ||
                onlyTargetPending ||
                onlyWithActualActivity ||
                onlyZeroActivity ||
                onlyPositiveBalance ||
                onlyZeroBalance ||
                onlyNegativeBalance ||
                onlyOutstandingDebt ||
                onlyClearedDebt ||
                excludeZeroAmounts ||
                minAmount != null ||
                maxAmount != null ||
                searchQuery.isNotBlank()

    val activeFilterCount: Int
        get() {
            var count = 0
            if (selectedCategoryIds.isNotEmpty()) count += selectedCategoryIds.size
            if (onlyWithBudgetOrTarget) count++
            if (onlyWithoutBudgetOrTarget) count++
            if (onlyOverspentOrOverallocated) count++
            if (onlyUnderSpentOrUnderallocated) count++
            if (onlyOnTrack) count++
            if (onlyTargetAchieved) count++
            if (onlyTargetPending) count++
            if (onlyWithActualActivity) count++
            if (onlyZeroActivity) count++
            if (onlyPositiveBalance) count++
            if (onlyZeroBalance) count++
            if (onlyNegativeBalance) count++
            if (onlyOutstandingDebt) count++
            if (onlyClearedDebt) count++
            if (excludeZeroAmounts) count++
            if (minAmount != null || maxAmount != null) count++
            if (searchQuery.isNotBlank()) count++
            return count
        }
}
