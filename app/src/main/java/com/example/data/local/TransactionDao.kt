package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY dateEpochMs DESC, id DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE debitAccountId = :accountId OR creditAccountId = :accountId ORDER BY dateEpochMs DESC, id DESC")
    fun getTransactionsForAccount(accountId: Long): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE categoryId = :categoryId OR subCategoryId = :categoryId ORDER BY dateEpochMs DESC, id DESC")
    fun getTransactionsForCategory(categoryId: Long): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE dateEpochMs BETWEEN :startTime AND :endTime ORDER BY dateEpochMs DESC, id DESC")
    fun getTransactionsBetween(startTime: Long, endTime: Long): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY dateEpochMs DESC, id DESC")
    fun getTransactionsByType(type: TransactionType): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: Long): Transaction?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<Transaction>)

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'EXPENSE' AND dateEpochMs BETWEEN :startTime AND :endTime")
    fun getTotalExpenseBetween(startTime: Long, endTime: Long): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'INCOME' AND dateEpochMs BETWEEN :startTime AND :endTime")
    fun getTotalIncomeBetween(startTime: Long, endTime: Long): Flow<Double?>

    @Query("SELECT * FROM transactions")
    suspend fun getAllTransactionsSnapshot(): List<Transaction>

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()
}
