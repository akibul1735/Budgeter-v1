package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Stores month-by-month budgeted amounts for categories and accounts.
 *
 * itemType can be:
 * - "EXPENSE" (itemId refers to categoryId)
 * - "INCOME" (itemId refers to categoryId)
 * - "ASSET" (itemId refers to accountId)
 * - "LIABILITY" (itemId refers to accountId)
 */
@Entity(
    tableName = "monthly_budgets",
    indices = [
        Index(value = ["year", "month", "itemType", "itemId"], unique = true)
    ]
)
data class MonthlyBudget(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val year: Int,
    val month: Int, // 1 to 12
    val itemType: String,
    val itemId: Long,
    val budgetedAmount: Double = 0.0,
    val previousAmount: Double = 0.0,
    val isEnabled: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis()
)
