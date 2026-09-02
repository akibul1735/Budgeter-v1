package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nameEn: String,
    val nameBn: String,
    val type: AccountType, // ASSET, LIABILITY, EQUITY
    val parentId: Long? = null, // null for top-level account, non-null for sub-account
    val initialBalance: Double = 0.0,
    val iconName: String = "AccountBalance",
    val colorHex: String = "#1E56A0",
    val accountNumber: String = "",
    val description: String = "",
    val isSystem: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun localizedName(mode: LanguageMode): String {
        return when (mode) {
            LanguageMode.ENGLISH -> nameEn
            LanguageMode.BANGLA -> if (nameBn.isNotBlank()) nameBn else nameEn
            LanguageMode.BILINGUAL -> if (nameBn.isNotBlank()) "$nameEn ($nameBn)" else nameEn
        }
    }
}
