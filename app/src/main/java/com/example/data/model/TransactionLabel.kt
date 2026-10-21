package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity representing a transaction label/tag with custom name, icon, and accent color.
 */
@Entity(
    tableName = "labels",
    indices = [
        Index(value = ["normalizedName"], unique = true),
        Index("name")
    ]
)
data class TransactionLabel(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val normalizedName: String = name.trim().lowercase(),
    val iconName: String = "Label",
    val colorHex: String = "#10B981",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
