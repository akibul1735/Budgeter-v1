package com.example.ui.dialogs

/**
 * Display Settings for Ledger screen transactions.
 */
data class TransactionDisplaySettings(
    val showTotalAmount: Boolean = true,
    val showTransfersInTotal: Boolean = false,
    val showAllTransactionsForNewAccount: Boolean = true,
    val showAccountBalance: Boolean = true,
    val showOldestDateFirst: Boolean = false
)
