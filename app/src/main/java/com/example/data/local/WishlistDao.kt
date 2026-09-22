package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.WishlistItem
import kotlinx.coroutines.flow.Flow

@Dao
interface WishlistDao {
    @Query("SELECT * FROM wishlist_items ORDER BY isPurchased ASC, CASE priority WHEN 'HIGH' THEN 1 WHEN 'MEDIUM' THEN 2 WHEN 'LOW' THEN 3 ELSE 4 END, createdAt DESC")
    fun getAllWishlistItems(): Flow<List<WishlistItem>>

    @Query("SELECT * FROM wishlist_items WHERE isPurchased = 0 ORDER BY CASE priority WHEN 'HIGH' THEN 1 WHEN 'MEDIUM' THEN 2 WHEN 'LOW' THEN 3 ELSE 4 END, createdAt DESC")
    fun getActiveWishlistItems(): Flow<List<WishlistItem>>

    @Query("SELECT * FROM wishlist_items")
    suspend fun getAllWishlistItemsSnapshot(): List<WishlistItem>

    @Query("SELECT * FROM wishlist_items WHERE id = :id")
    suspend fun getWishlistItemById(id: Long): WishlistItem?

    @Query("SELECT * FROM wishlist_items WHERE isPurchased = 0 AND ((targetType = 'NEXT_MONTH') OR (targetType = 'SPECIFIC_MONTH' AND targetYear = :year AND targetMonth = :month))")
    fun getWishlistItemsForMonth(year: Int, month: Int): Flow<List<WishlistItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWishlistItem(item: WishlistItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWishlistItems(items: List<WishlistItem>)

    @Update
    suspend fun updateWishlistItem(item: WishlistItem)

    @Delete
    suspend fun deleteWishlistItem(item: WishlistItem)

    @Query("DELETE FROM wishlist_items WHERE id = :id")
    suspend fun deleteWishlistItemById(id: Long)

    @Query("DELETE FROM wishlist_items")
    suspend fun deleteAllWishlistItems()
}
