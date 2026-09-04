package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.BudgetAdjustment
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetAdjustmentDao {

    @Query("SELECT * FROM budget_adjustments WHERE year = :year AND month = :month ORDER BY timestamp DESC")
    fun getAdjustmentsForMonth(year: Int, month: Int): Flow<List<BudgetAdjustment>>

    @Query("SELECT * FROM budget_adjustments WHERE year = :year AND month = :month AND itemType = :itemType AND itemId = :itemId ORDER BY timestamp DESC")
    fun getAdjustmentsForItem(year: Int, month: Int, itemType: String, itemId: Long): Flow<List<BudgetAdjustment>>

    @Query("SELECT * FROM budget_adjustments WHERE year = :year AND month = :month ORDER BY timestamp DESC")
    suspend fun getAdjustmentsForMonthSnapshot(year: Int, month: Int): List<BudgetAdjustment>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdjustment(adjustment: BudgetAdjustment): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdjustments(adjustments: List<BudgetAdjustment>)

    @Query("DELETE FROM budget_adjustments WHERE year = :year AND month = :month AND itemType = :itemType AND itemId = :itemId")
    suspend fun deleteAdjustmentsForItem(year: Int, month: Int, itemType: String, itemId: Long)

    @Query("DELETE FROM budget_adjustments")
    suspend fun deleteAll()
}
