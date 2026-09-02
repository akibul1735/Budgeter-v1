package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nameEn: String,
    val nameBn: String,
    val type: CategoryType, // EXPENSE or INCOME
    val parentId: Long? = null, // null for main category, non-null for sub-category
    val iconName: String = "Category",
    val colorHex: String = "#EF4444",
    val budgetLimit: Double = 0.0,
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
