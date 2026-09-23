package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ItemImageCache
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemImageCacheDao {
    @Query("SELECT * FROM item_image_cache ORDER BY itemName ASC")
    fun getAllCachedItemsFlow(): Flow<List<ItemImageCache>>

    @Query("SELECT * FROM item_image_cache")
    suspend fun getAllCachedItemsSnapshot(): List<ItemImageCache>

    @Query("SELECT * FROM item_image_cache WHERE normalizedItemName = :normalizedName LIMIT 1")
    suspend fun getByNormalizedName(normalizedName: String): ItemImageCache?

    @Query("SELECT * FROM item_image_cache WHERE normalizedItemName = :normalizedName LIMIT 1")
    fun getByNormalizedNameFlow(normalizedName: String): Flow<ItemImageCache?>

    @Query("SELECT * FROM item_image_cache WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ItemImageCache?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(item: ItemImageCache): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ItemImageCache>)

    @Update
    suspend fun update(item: ItemImageCache)

    @Delete
    suspend fun delete(item: ItemImageCache)

    @Query("DELETE FROM item_image_cache WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM item_image_cache WHERE normalizedItemName = :normalizedName")
    suspend fun deleteByNormalizedName(normalizedName: String)

    @Query("DELETE FROM item_image_cache")
    suspend fun deleteAll()
}
