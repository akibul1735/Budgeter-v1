package com.example.ui.dialogs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.example.data.model.Account
import com.example.data.model.LanguageMode
import com.example.ui.components.filter.FilterState
import com.example.ui.components.filter.UnifiedFilterDialog
import com.example.ui.components.filter.specs.BalanceSheetFilterSpec

/**
 * Balance Sheet Tab Filter Dialog.
 * Delegates to UnifiedFilterDialog with BalanceSheetFilterSpec to deliver the exact same
 * compact, ergonomic, bottom-anchored modal popup experience as the Budget tab filter popup.
 */
@Composable
fun BalanceSheetFilterDialog(
    initialState: FilterState,
    accounts: List<Account>,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onApply: (FilterState) -> Unit
) {
    val spec = remember(accounts) {
        BalanceSheetFilterSpec.createSpec(accounts)
    }

    UnifiedFilterDialog(
        spec = spec,
        initialState = initialState,
        items = accounts,
        languageMode = languageMode,
        onDismiss = onDismiss,
        onApply = onApply
    )
}
