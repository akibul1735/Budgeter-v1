package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class RecurrencePeriod {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY
}

enum class BillStatus {
    PENDING,
    PAID,
    OVERDUE,
    SKIPPED
}

@Entity(
    tableName = "recurring_bills",
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
        Index("nextDueDateEpochMs")
    ]
)
data class RecurringBill(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val type: TransactionType,
    val amount: Double,
    val recurrencePeriod: RecurrencePeriod,
    val nextDueDateEpochMs: Long,
    val debitAccountId: Long? = null,
    val creditAccountId: Long? = null,
    val categoryId: Long? = null,
    val subCategoryId: Long? = null,
    val payeeOrPayer: String = "",
    val note: String = "",
    val isAutoRecord: Boolean = false,
    val isActive: Boolean = true,
    val status: BillStatus = BillStatus.PENDING,
    val lastRecordedDateEpochMs: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

data class RecurringBillWithDetails(
    val bill: RecurringBill,
    val debitAccount: Account? = null,
    val creditAccount: Account? = null,
    val category: Category? = null,
    val subCategory: Category? = null
)
