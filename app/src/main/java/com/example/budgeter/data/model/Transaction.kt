package com.example.budgeter.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType {
    EXPENSE,
    INCOME,
    TRANSFER
}

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Double,
    val type: TransactionType,
    val categoryId: Long?,
    val accountId: Long,
    val destinationAccountId: Long? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = "",
    val labels: String = "", // Comma-separated labels e.g. "Personal, Urgent"
    val status: String = "COMPLETED" // COMPLETED, PENDING, CLEARED
)
