package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class AccountType(val displayName: String) {
    CHECKING("Checking"),
    SAVINGS("Savings"),
    CASH("Cash"),
    CREDIT_CARD("Credit Card"),
    INVESTMENT("Investment"),
    LOAN("Loan / Debt"),
    ASSET("Fixed Asset")
}

@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: AccountType,
    val balance: Double,
    val currency: String = "USD",
    val colorHex: String = "#1A73E8",
    val iconName: String = "AccountBalance",
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

enum class TransactionType {
    EXPENSE,
    INCOME,
    TRANSFER
}

enum class TransactionStatus {
    CLEARED,
    PENDING,
    RECONCILED
}

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: TransactionType,
    val iconName: String = "Category",
    val colorHex: String = "#3B82F6",
    val monthlyBudget: Double = 0.0,
    val isDefault: Boolean = false
)

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = Account::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("accountId"), Index("dateEpochMs"), Index("categoryId")]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Double,
    val type: TransactionType,
    val accountId: Long,
    val toAccountId: Long? = null, // For transfers
    val categoryId: Long? = null,
    val dateEpochMs: Long = System.currentTimeMillis(),
    val notes: String = "",
    val tags: String = "",
    val status: TransactionStatus = TransactionStatus.CLEARED,
    val isRecurring: Boolean = false
)

enum class BillFrequency(val displayName: String) {
    WEEKLY("Weekly"),
    BIWEEKLY("Bi-Weekly"),
    MONTHLY("Monthly"),
    YEARLY("Yearly")
}

@Entity(tableName = "recurring_bills")
data class RecurringBill(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Double,
    val categoryId: Long? = null,
    val accountId: Long,
    val frequency: BillFrequency = BillFrequency.MONTHLY,
    val nextDueDateEpochMs: Long,
    val isAutoPay: Boolean = false,
    val isActive: Boolean = true,
    val notes: String = ""
)

@Entity(tableName = "savings_goals")
data class SavingsGoal(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val targetDateEpochMs: Long = System.currentTimeMillis() + (90L * 24 * 3600 * 1000),
    val colorHex: String = "#10B981",
    val iconName: String = "Savings",
    val notes: String = ""
)

data class TransactionWithDetails(
    val transaction: Transaction,
    val accountName: String,
    val toAccountName: String? = null,
    val categoryName: String? = null,
    val categoryIcon: String? = null,
    val categoryColor: String? = null
)

data class CashflowSummary(
    val totalIncome: Double,
    val totalExpense: Double,
    val netSavings: Double,
    val savingsRate: Double
)

data class BalanceSheetSummary(
    val totalAssets: Double,
    val totalLiabilities: Double,
    val netWorth: Double
)

data class CategorySpend(
    val categoryId: Long,
    val categoryName: String,
    val categoryColor: String,
    val categoryIcon: String,
    val totalSpent: Double,
    val budgetAmount: Double,
    val percentageOfTotal: Float
)

data class AIInsight(
    val id: String,
    val title: String,
    val description: String,
    val type: InsightType,
    val amount: Double? = null,
    val actionText: String? = null
)

enum class InsightType {
    ALERT,
    POSITIVE,
    NEUTRAL,
    TIP
}
