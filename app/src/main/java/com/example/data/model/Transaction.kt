package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Double-Entry Transaction representation:
 *
 * For EXPENSE:
 *   - Debit: Expense Category / Sub-Category (via categoryId, subCategoryId)
 *   - Credit: Asset or Liability Account (creditAccountId)
 *
 * For INCOME:
 *   - Debit: Asset Account (debitAccountId)
 *   - Credit: Income Category / Sub-Category (via categoryId, subCategoryId)
 *
 * For TRANSFER:
 *   - Debit: Destination Asset/Liability Account (debitAccountId)
 *   - Credit: Source Asset/Liability Account (creditAccountId)
 */
@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = Account::class,
            parentColumns = ["id"],
            childColumns = ["debitAccountId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = Account::class,
            parentColumns = ["id"],
            childColumns = ["creditAccountId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("debitAccountId"),
        Index("creditAccountId"),
        Index("categoryId"),
        Index("dateEpochMs")
    ]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: TransactionType,
    val amount: Double,
    val dateEpochMs: Long,
    val note: String = "",
    val referenceNo: String = "",
    val debitAccountId: Long? = null,
    val creditAccountId: Long? = null,
    val categoryId: Long? = null,
    val subCategoryId: Long? = null,
    val payeeOrPayer: String = "",
    val attachmentUri: String = "",
    val status: TransactionStatus = TransactionStatus.NONE,
    val createdAt: Long = System.currentTimeMillis()
)

data class TransactionWithDetails(
    val transaction: Transaction,
    val debitAccount: Account? = null,
    val creditAccount: Account? = null,
    val category: Category? = null,
    val subCategory: Category? = null
)
