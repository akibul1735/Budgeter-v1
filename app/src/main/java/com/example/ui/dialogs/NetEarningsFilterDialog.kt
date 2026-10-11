package com.example.ui.dialogs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.example.data.model.Account
import com.example.data.model.Category
import com.example.data.model.LanguageMode
import com.example.data.model.TransactionWithDetails
import com.example.ui.components.filter.FilterState
import com.example.ui.components.filter.UnifiedFilterDialog
import com.example.ui.components.filter.specs.NetEarningsFilterSpec

/**
 * Net Earnings Tab Filter Dialog.
 * Delegates to UnifiedFilterDialog with NetEarningsFilterSpec to deliver the exact same
 * compact, ergonomic, bottom-anchored modal popup experience as the Budget tab filter popup.
 */
@Composable
fun NetEarningsFilterDialog(
    initialState: FilterState,
    categories: List<Category> = emptyList(),
    accounts: List<Account> = emptyList(),
    labels: List<String> = emptyList(),
    transactions: List<TransactionWithDetails> = emptyList(),
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onApply: (FilterState) -> Unit
) {
    val spec = remember(categories, accounts, labels) {
        NetEarningsFilterSpec.createSpec(
            accounts = accounts,
            categories = categories,
            labels = labels
        )
    }

    UnifiedFilterDialog(
        spec = spec,
        initialState = initialState,
        items = transactions,
        languageMode = languageMode,
        onDismiss = onDismiss,
        onApply = onApply
    )
}
