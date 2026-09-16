package com.example.data.model

/**
 * Represents a single line item within a split transaction.
 */
data class TransactionSplitItem(
    val id: Long = 0L,
    val categoryId: Long? = null,
    val subCategoryId: Long? = null,
    val amount: Double = 0.0,
    val note: String = ""
)
