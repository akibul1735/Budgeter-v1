package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class WishlistPriority {
    HIGH,
    MEDIUM,
    LOW
}

enum class WishlistTargetType {
    NEXT_MONTH,
    SPECIFIC_MONTH,
    SAVINGS_GOAL,
    SOMEDAY
}

@Entity(
    tableName = "wishlist_items",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("categoryId")
    ]
)
data class WishlistItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val estimatedAmount: Double,
    val categoryId: Long? = null,
    val subCategoryId: Long? = null,
    val targetType: WishlistTargetType = WishlistTargetType.NEXT_MONTH,
    val targetYear: Int? = null,
    val targetMonth: Int? = null, // 1-12
    val priority: WishlistPriority = WishlistPriority.MEDIUM,
    val notes: String = "",
    val url: String = "",
    val isPurchased: Boolean = false,
    val isAddedToBudget: Boolean = false,
    val linkedGoalId: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class WishlistItemWithCategory(
    val item: WishlistItem,
    val category: Category? = null,
    val subCategory: Category? = null,
    val linkedGoal: SavingsGoal? = null
)
