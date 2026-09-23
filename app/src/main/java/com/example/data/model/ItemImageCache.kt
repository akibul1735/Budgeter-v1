package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class ImageCacheSource {
    AUTO_SELECTED,
    USER_SELECTED,
    CUSTOM
}

@Entity(
    tableName = "item_image_cache",
    indices = [
        Index(value = ["normalizedItemName"], unique = true),
        Index("itemName")
    ]
)
data class ItemImageCache(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val itemName: String,
    val normalizedItemName: String,
    val iconKey: String,
    val source: ImageCacheSource = ImageCacheSource.AUTO_SELECTED,
    val sourceTitle: String = "",
    val originalQuery: String = "",
    val lastUpdated: Long = System.currentTimeMillis()
)
