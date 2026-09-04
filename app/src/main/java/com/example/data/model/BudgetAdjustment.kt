package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Tracks the historical adjustments made to monthly budgets,
 * preserving previous budgets, adjusted budgets, difference, and timestamps.
 */
@Entity(
    tableName = "budget_adjustments",
    indices = [
        Index(value = ["year", "month", "itemType", "itemId"])
    ]
)
data class BudgetAdjustment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val year: Int,
    val month: Int,
    val itemType: String, // "EXPENSE", "INCOME", "ASSET", "LIABILITY"
    val itemId: Long,
    val previousAmount: Double,
    val adjustedAmount: Double,
    val difference: Double = adjustedAmount - previousAmount,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
