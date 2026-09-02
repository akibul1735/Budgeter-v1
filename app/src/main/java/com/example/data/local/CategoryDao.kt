package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Category
import com.example.data.model.CategoryType
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY type ASC, parentId ASC, nameEn ASC")
    fun getAllCategories(): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE type = :type ORDER BY parentId ASC, nameEn ASC")
    fun getCategoriesByType(type: CategoryType): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE type = :type AND parentId IS NULL ORDER BY nameEn ASC")
    fun getParentCategoriesByType(type: CategoryType): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE parentId = :parentId ORDER BY nameEn ASC")
    fun getSubCategories(parentId: Long): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE id = :id LIMIT 1")
    suspend fun getCategoryById(id: Long): Category?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<Category>)

    @Update
    suspend fun updateCategory(category: Category)

    @Delete
    suspend fun deleteCategory(category: Category)

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteCategoryById(id: Long)

    @Query("SELECT * FROM categories")
    suspend fun getAllCategoriesSnapshot(): List<Category>

    @Query("DELETE FROM categories")
    suspend fun deleteAll()
}
