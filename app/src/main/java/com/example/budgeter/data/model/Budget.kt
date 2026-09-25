package com.example.budgeter.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryId: Long,
    val allocatedAmount: Double,
    val targetAmount: Double = 0.0,
    val period: String = "MONTHLY", // MONTHLY, WEEKLY, YEARLY, CUSTOM
    val monthYear: String, // e.g. "2026-09"
    val rolloverAmount: Double = 0.0,
    val notes: String = ""
)
