package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Account
import com.example.data.model.AccountType
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts ORDER BY type ASC, parentId ASC, nameEn ASC")
    fun getAllAccounts(): Flow<List<Account>>

    @Query("SELECT * FROM accounts WHERE type = :type ORDER BY parentId ASC, nameEn ASC")
    fun getAccountsByType(type: AccountType): Flow<List<Account>>

    @Query("SELECT * FROM accounts WHERE parentId IS NULL ORDER BY type ASC, nameEn ASC")
    fun getParentAccounts(): Flow<List<Account>>

    @Query("SELECT * FROM accounts WHERE parentId = :parentId ORDER BY nameEn ASC")
    fun getSubAccounts(parentId: Long): Flow<List<Account>>

    @Query("SELECT * FROM accounts WHERE id = :id LIMIT 1")
    suspend fun getAccountById(id: Long): Account?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: Account): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccounts(accounts: List<Account>)

    @Update
    suspend fun updateAccount(account: Account)

    @Delete
    suspend fun deleteAccount(account: Account)

    @Query("DELETE FROM accounts WHERE id = :id")
    suspend fun deleteAccountById(id: Long)

    @Query("SELECT * FROM accounts")
    suspend fun getAllAccountsSnapshot(): List<Account>

    @Query("SELECT COUNT(*) FROM accounts")
    suspend fun getAccountCount(): Int

    @Query("DELETE FROM accounts")
    suspend fun deleteAll()
}
