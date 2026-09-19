package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "savings_goals")
data class SavingsGoal(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val nameBn: String = "",
    val targetAmount: Double,
    val targetDate: Long = 0L, // timestamp in millis (0L if no deadline)
    val colorHex: String = "#10B981", // default emerald
    val iconName: String = "Savings",
    val notes: String = "",
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "goal_allocations",
    foreignKeys = [
        ForeignKey(
            entity = SavingsGoal::class,
            parentColumns = ["id"],
            childColumns = ["goalId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Account::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("goalId"),
        Index("accountId")
    ]
)
data class GoalAllocation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val goalId: Long,
    val accountId: Long,
    val allocatedAmount: Double,
    val updatedAt: Long = System.currentTimeMillis()
)

data class GoalAllocationWithAccount(
    val allocation: GoalAllocation,
    val account: Account,
    val accountCurrentBalance: Double
) {
    // If account balance < allocatedAmount, allocation is impaired/in deficit
    val isFunded: Boolean
        get() = accountCurrentBalance >= allocation.allocatedAmount

    val actualFundedAmount: Double
        get() = minOf(allocation.allocatedAmount, maxOf(0.0, accountCurrentBalance))

    val deficitAmount: Double
        get() = maxOf(0.0, allocation.allocatedAmount - actualFundedAmount)
}

data class SavingsGoalWithDetails(
    val goal: SavingsGoal,
    val allocations: List<GoalAllocationWithAccount> = emptyList()
) {
    val totalAllocated: Double
        get() = allocations.sumOf { it.allocation.allocatedAmount }

    val effectiveSaved: Double
        get() = allocations.sumOf { it.actualFundedAmount }

    val totalDeficit: Double
        get() = allocations.sumOf { it.deficitAmount }

    val hasDeficit: Boolean
        get() = totalDeficit > 0.001

    val progressPercent: Float
        get() = if (goal.targetAmount > 0) {
            ((effectiveSaved / goal.targetAmount) * 100.0).coerceIn(0.0, 100.0).toFloat()
        } else 0f

    val remainingAmount: Double
        get() = maxOf(0.0, goal.targetAmount - effectiveSaved)

    val isTargetReached: Boolean
        get() = goal.targetAmount > 0 && effectiveSaved >= goal.targetAmount
}

data class SavingsSummary(
    val totalTarget: Double,
    val totalSaved: Double,
    val totalRemaining: Double,
    val totalDeficit: Double,
    val activeGoalsCount: Int,
    val completedGoalsCount: Int,
    val overallProgressPercent: Float
)
