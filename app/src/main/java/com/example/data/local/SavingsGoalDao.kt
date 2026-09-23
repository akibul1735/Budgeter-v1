package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.GoalAllocation
import com.example.data.model.SavingsGoal
import kotlinx.coroutines.flow.Flow

@Dao
interface SavingsGoalDao {
    @Query("SELECT * FROM savings_goals ORDER BY isCompleted ASC, createdAt DESC")
    fun getAllGoals(): Flow<List<SavingsGoal>>

    @Query("SELECT * FROM savings_goals ORDER BY isCompleted ASC, createdAt DESC")
    suspend fun getAllGoalsSnapshot(): List<SavingsGoal>

    @Query("SELECT * FROM savings_goals WHERE id = :id")
    suspend fun getGoalById(id: Long): SavingsGoal?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: SavingsGoal): Long

    @Update
    suspend fun updateGoal(goal: SavingsGoal)

    @Delete
    suspend fun deleteGoal(goal: SavingsGoal)

    @Query("DELETE FROM savings_goals WHERE id = :id")
    suspend fun deleteGoalById(id: Long)

    @Query("UPDATE savings_goals SET isCompleted = :isCompleted, updatedAt = :updatedAt WHERE id = :goalId")
    suspend fun setGoalCompleted(goalId: Long, isCompleted: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM savings_goals")
    suspend fun deleteAllGoals()

    // Allocations
    @Query("SELECT * FROM goal_allocations")
    fun getAllAllocations(): Flow<List<GoalAllocation>>

    @Query("SELECT * FROM goal_allocations")
    suspend fun getAllAllocationsSnapshot(): List<GoalAllocation>

    @Query("SELECT * FROM goal_allocations WHERE goalId = :goalId")
    fun getAllocationsForGoal(goalId: Long): Flow<List<GoalAllocation>>

    @Query("SELECT * FROM goal_allocations WHERE goalId = :goalId")
    suspend fun getAllocationsForGoalSync(goalId: Long): List<GoalAllocation>

    @Query("SELECT * FROM goal_allocations WHERE accountId = :accountId")
    fun getAllocationsForAccount(accountId: Long): Flow<List<GoalAllocation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllocation(allocation: GoalAllocation): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllocations(allocations: List<GoalAllocation>)

    @Update
    suspend fun updateAllocation(allocation: GoalAllocation)

    @Delete
    suspend fun deleteAllocation(allocation: GoalAllocation)

    @Query("DELETE FROM goal_allocations WHERE goalId = :goalId")
    suspend fun deleteAllocationsForGoal(goalId: Long)

    @Query("DELETE FROM goal_allocations WHERE goalId = :goalId AND accountId = :accountId")
    suspend fun deleteAllocationForGoalAndAccount(goalId: Long, accountId: Long)

    @Query("DELETE FROM goal_allocations WHERE id = :id")
    suspend fun deleteAllocationById(id: Long)

    @Query("DELETE FROM goal_allocations")
    suspend fun deleteAllAllocations()
}
