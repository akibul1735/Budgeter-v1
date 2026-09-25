package com.example.budgeter.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String = "BANK", // CASH, BANK, MOBILE_MONEY, CREDIT_CARD, SAVINGS, LOAN
    val initialBalance: Double = 0.0,
    val currentBalance: Double = 0.0,
    val isDebt: Boolean = false,
    val debtLimit: Double = 0.0,
    val currency: String = "BDT",
    val colorHex: String = "#006C4C"
)
