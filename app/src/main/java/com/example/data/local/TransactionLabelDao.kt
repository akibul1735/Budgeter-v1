package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.TransactionLabel
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionLabelDao {
    @Query("SELECT * FROM labels ORDER BY name ASC")
    fun getAllLabelsFlow(): Flow<List<TransactionLabel>>

    @Query("SELECT * FROM labels ORDER BY name ASC")
    suspend fun getAllLabels(): List<TransactionLabel>

    @Query("SELECT * FROM labels WHERE normalizedName = :normalizedName LIMIT 1")
    suspend fun getByNormalizedName(normalizedName: String): TransactionLabel?

    @Query("SELECT * FROM labels WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): TransactionLabel?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(label: TransactionLabel): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnore(labels: List<TransactionLabel>)

    @Update
    suspend fun update(label: TransactionLabel)

    @Delete
    suspend fun delete(label: TransactionLabel)

    @Query("DELETE FROM labels WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM labels WHERE normalizedName = :normalizedName")
    suspend fun deleteByNormalizedName(normalizedName: String)

    @Query("DELETE FROM labels")
    suspend fun deleteAll()
}
