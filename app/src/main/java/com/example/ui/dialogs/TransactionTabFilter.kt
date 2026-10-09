package com.example.ui.dialogs

import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionType
import com.example.ui.screens.LedgerDatePreset
import com.example.ui.screens.LedgerRowStyle

/**
 * Filter and view configuration state for Transactions (Ledger) tab.
 * Follows the design model of BudgetMakerTabFilter with tailored transaction controls.
 */
data class TransactionTabFilter(
    // 1. Transaction Types (Multi or single selection: Expense, Income, Transfer)
    val selectedTypes: Set<TransactionType> = emptySet(),

    // 2. Date Range
    val datePreset: LedgerDatePreset = LedgerDatePreset.LAST_12_MONTHS,
    val customStartDateMs: Long = 0L,
    val customEndDateMs: Long = System.currentTimeMillis(),

    // 3. Amount Bounds (Min / Max)
    val minAmount: Double? = null,
    val maxAmount: Double? = null,

    // 4. Categories & Groups
    val selectedCategoryIds: Set<Long> = emptySet(),
    val selectedCategoryGroupNames: Set<String> = emptySet(),

    // 5. Accounts & Groups (Debit or Credit)
    val selectedAccountIds: Set<Long> = emptySet(),
    val selectedAccountGroupNames: Set<String> = emptySet(),

    // 6. Transaction Status
    val selectedStatuses: Set<TransactionStatus> = emptySet(),

    // 7. Labels & Notes
    val labelOrReference: String? = null,

    // 8. Transaction Special Conditions
    val onlyWithReceiptOrNote: Boolean = false,
    val onlyWithoutNote: Boolean = false,
    val onlyReversals: Boolean = false,
    val onlyHighValue: Boolean = false, // e.g. >= 10,000 ৳

    // 9. Display & Layout Preferences
    val rowStyle: LedgerRowStyle = LedgerRowStyle.STANDARD,
    val displaySettings: TransactionDisplaySettings = TransactionDisplaySettings()
) {
    val isActive: Boolean
        get() = selectedTypes.isNotEmpty() ||
                datePreset != LedgerDatePreset.LAST_12_MONTHS ||
                minAmount != null ||
                maxAmount != null ||
                selectedCategoryIds.isNotEmpty() ||
                selectedCategoryGroupNames.isNotEmpty() ||
                selectedAccountIds.isNotEmpty() ||
                selectedAccountGroupNames.isNotEmpty() ||
                selectedStatuses.isNotEmpty() ||
                !labelOrReference.isNullOrBlank() ||
                onlyWithReceiptOrNote ||
                onlyWithoutNote ||
                onlyReversals ||
                onlyHighValue

    val activeCount: Int
        get() {
            var count = 0
            if (selectedTypes.isNotEmpty()) count++
            if (datePreset != LedgerDatePreset.LAST_12_MONTHS) count++
            if (minAmount != null || maxAmount != null) count++
            if (selectedCategoryIds.isNotEmpty() || selectedCategoryGroupNames.isNotEmpty()) count++
            if (selectedAccountIds.isNotEmpty() || selectedAccountGroupNames.isNotEmpty()) count++
            if (selectedStatuses.isNotEmpty()) count++
            if (!labelOrReference.isNullOrBlank()) count++
            if (onlyWithReceiptOrNote || onlyWithoutNote) count++
            if (onlyReversals) count++
            if (onlyHighValue) count++
            return count
        }
}
