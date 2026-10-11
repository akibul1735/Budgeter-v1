package com.example.ui.dialogs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.example.data.model.Account
import com.example.data.model.Category
import com.example.data.model.LanguageMode
import com.example.ui.components.filter.FilterState
import com.example.ui.components.filter.UnifiedFilterDialog
import com.example.ui.components.filter.specs.ItemsFilterSpec

/**
 * Items Summary Tab Filter Dialog.
 * Delegates to UnifiedFilterDialog with ItemsFilterSpec to deliver the exact same
 * compact, ergonomic, bottom-anchored modal popup experience as the Budget tab filter popup.
 */
@Composable
fun ItemsFilterDialog(
    initialState: FilterState,
    categories: List<Category> = emptyList(),
    accounts: List<Account> = emptyList(),
    countProvider: ((FilterState) -> Int)? = null,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onApply: (FilterState) -> Unit
) {
    val spec = remember(categories, accounts) {
        ItemsFilterSpec.createSpec(
            categories = categories,
            accounts = accounts
        )
    }

    UnifiedFilterDialog(
        spec = spec,
        initialState = initialState,
        countProvider = countProvider,
        languageMode = languageMode,
        onDismiss = onDismiss,
        onApply = onApply
    )
}
