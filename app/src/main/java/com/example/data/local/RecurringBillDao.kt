package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.BillStatus
import com.example.data.model.RecurringBill
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringBillDao {

    @Query("SELECT * FROM recurring_bills ORDER BY nextDueDateEpochMs ASC")
    fun getAllBills(): Flow<List<RecurringBill>>

    @Query("SELECT * FROM recurring_bills WHERE isActive = 1 ORDER BY nextDueDateEpochMs ASC")
    fun getActiveBills(): Flow<List<RecurringBill>>

    @Query("SELECT * FROM recurring_bills WHERE id = :id LIMIT 1")
    suspend fun getBillById(id: Long): RecurringBill?

    @Query("SELECT * FROM recurring_bills WHERE isActive = 1 AND nextDueDateEpochMs <= :currentTimeMs")
    suspend fun getDueBills(currentTimeMs: Long): List<RecurringBill>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBill(bill: RecurringBill): Long

    @Update
    suspend fun updateBill(bill: RecurringBill)

    @Delete
    suspend fun deleteBill(bill: RecurringBill)

    @Query("UPDATE recurring_bills SET nextDueDateEpochMs = :nextDue, status = :newStatus, lastRecordedDateEpochMs = :recordedTime WHERE id = :id")
    suspend fun updateBillDueDate(id: Long, nextDue: Long, newStatus: BillStatus, recordedTime: Long)

    @Query("SELECT * FROM recurring_bills")
    suspend fun getAllBillsSnapshot(): List<RecurringBill>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(bills: List<RecurringBill>)

    @Query("DELETE FROM recurring_bills")
    suspend fun deleteAll()
}
