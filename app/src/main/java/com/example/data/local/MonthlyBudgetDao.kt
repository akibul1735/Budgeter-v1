package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.MonthlyBudget
import kotlinx.coroutines.flow.Flow

@Dao
interface MonthlyBudgetDao {

    @Query("SELECT * FROM monthly_budgets WHERE year = :year AND month = :month")
    fun getBudgetsForMonth(year: Int, month: Int): Flow<List<MonthlyBudget>>

    @Query("SELECT * FROM monthly_budgets WHERE year = :year AND month = :month AND itemType = :itemType AND itemId = :itemId LIMIT 1")
    suspend fun getBudget(year: Int, month: Int, itemType: String, itemId: Long): MonthlyBudget?

    @Query("SELECT * FROM monthly_budgets WHERE year = :year AND month = :month")
    suspend fun getBudgetsForMonthSnapshot(year: Int, month: Int): List<MonthlyBudget>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBudget(budget: MonthlyBudget): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBudgets(budgets: List<MonthlyBudget>)

    @Query("DELETE FROM monthly_budgets WHERE year = :year AND month = :month AND itemType = :itemType AND itemId = :itemId")
    suspend fun deleteBudget(year: Int, month: Int, itemType: String, itemId: Long)

    @Query("SELECT * FROM monthly_budgets")
    suspend fun getAllBudgetsSnapshot(): List<MonthlyBudget>

    @Query("DELETE FROM monthly_budgets")
    suspend fun deleteAll()
}
