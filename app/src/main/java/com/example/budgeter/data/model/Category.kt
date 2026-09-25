package com.example.budgeter.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val iconName: String = "category",
    val colorHex: String = "#006C4C",
    val isExpense: Boolean = true,
    val isIncome: Boolean = false
)
