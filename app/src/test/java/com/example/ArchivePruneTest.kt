package com.example

import com.example.data.model.Account
import com.example.data.model.AccountType
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.util.ArchiveManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ArchivePruneTest {

    @Test
    fun testMathematicalRollForward_AssetAccount() {
        // Asset Account with starting balance 10,000
        val assetAcc = Account(id = 1L, nameEn = "Bank Account", nameBn = "ব্যাংক অ্যাকাউন্ট", type = AccountType.ASSET, initialBalance = 10000.0)

        // Historical transactions:
        // +5000 Income (Debit assetAcc)
        // -2000 Expense (Credit assetAcc)
        // -1000 Transfer Out (Credit assetAcc)
        val t1 = Transaction(id = 101, type = TransactionType.INCOME, amount = 5000.0, dateEpochMs = 1000L, debitAccountId = 1L)
        val t2 = Transaction(id = 102, type = TransactionType.EXPENSE, amount = 2000.0, dateEpochMs = 2000L, creditAccountId = 1L)
        val t3 = Transaction(id = 103, type = TransactionType.TRANSFER, amount = 1000.0, dateEpochMs = 3000L, creditAccountId = 1L, debitAccountId = 2L)

        // Active transaction (after archive window):
        // -500 Expense
        val tActive = Transaction(id = 104, type = TransactionType.EXPENSE, amount = 500.0, dateEpochMs = 9000L, creditAccountId = 1L)

        // Pre-Archive total balance:
        // Initial + (Dr - Cr) = 10,000 + (5000 - 3500) = 11,500
        val allTxs = listOf(t1, t2, t3, tActive)
        val drAll = allTxs.filter { it.debitAccountId == 1L }.sumOf { it.amount }
        val crAll = allTxs.filter { it.creditAccountId == 1L }.sumOf { it.amount }
        val preArchiveBal = assetAcc.initialBalance + (drAll - crAll)
        assertEquals(11500.0, preArchiveBal, 0.001)

        // Archived range: t1, t2, t3
        val archivedTxs = listOf(t1, t2, t3)
        val drArchived = archivedTxs.filter { it.debitAccountId == 1L }.sumOf { it.amount }
        val crArchived = archivedTxs.filter { it.creditAccountId == 1L }.sumOf { it.amount }
        val netArchivedImpact = drArchived - crArchived // 5000 - 3000 = +2000

        // New Opening Balance after pruning
        val newInitialBalance = assetAcc.initialBalance + netArchivedImpact
        assertEquals(12000.0, newInitialBalance, 0.001)

        // Post-Archive active balance (only tActive remains in DB):
        val remainingTxs = listOf(tActive)
        val drRem = remainingTxs.filter { it.debitAccountId == 1L }.sumOf { it.amount }
        val crRem = remainingTxs.filter { it.creditAccountId == 1L }.sumOf { it.amount }
        val postArchiveBal = newInitialBalance + (drRem - crRem)

        // Balance MUST be 100% identical
        assertEquals(preArchiveBal, postArchiveBal, 0.001)
        assertEquals(11500.0, postArchiveBal, 0.001)
    }

    @Test
    fun testMathematicalRollForward_LiabilityAccount() {
        // Credit Card Liability with initial balance 0
        val creditCard = Account(id = 2L, nameEn = "Credit Card", nameBn = "ক্রেডিট কার্ড", type = AccountType.LIABILITY, initialBalance = 0.0)

        // Historical transactions:
        // Swipe 3000 (Credit liability)
        // Pay 1000 (Debit liability)
        val t1 = Transaction(id = 201, type = TransactionType.EXPENSE, amount = 3000.0, dateEpochMs = 1000L, creditAccountId = 2L)
        val t2 = Transaction(id = 202, type = TransactionType.TRANSFER, amount = 1000.0, dateEpochMs = 2000L, debitAccountId = 2L)

        // Active transaction: Swipe 500
        val tActive = Transaction(id = 203, type = TransactionType.EXPENSE, amount = 500.0, dateEpochMs = 8000L, creditAccountId = 2L)

        val allTxs = listOf(t1, t2, tActive)
        val drAll = allTxs.filter { it.debitAccountId == 2L }.sumOf { it.amount }
        val crAll = allTxs.filter { it.creditAccountId == 2L }.sumOf { it.amount }
        val preBal = -(creditCard.initialBalance + (crAll - drAll))
        assertEquals(-2500.0, preBal, 0.001)

        // Archived range: t1, t2
        val archivedTxs = listOf(t1, t2)
        val drArchived = archivedTxs.filter { it.debitAccountId == 2L }.sumOf { it.amount }
        val crArchived = archivedTxs.filter { it.creditAccountId == 2L }.sumOf { it.amount }
        val netArchivedImpact = crArchived - drArchived // 3000 - 1000 = +2000

        val newInitialBalance = creditCard.initialBalance + netArchivedImpact
        assertEquals(2000.0, newInitialBalance, 0.001)

        val remainingTxs = listOf(tActive)
        val drRem = remainingTxs.filter { it.debitAccountId == 2L }.sumOf { it.amount }
        val crRem = remainingTxs.filter { it.creditAccountId == 2L }.sumOf { it.amount }
        val postBal = -(newInitialBalance + (crRem - drRem))

        // Balance MUST be 100% identical
        assertEquals(preBal, postBal, 0.001)
        assertEquals(-2500.0, postBal, 0.001)
    }

    @Test
    fun testEncryptionAndDecryptionIntegrity() {
        val testData = "Test payload for archive encryption and decryption"
        val password = "StrongUserPassword123!"

        val enc = ArchiveManager.encryptPayload(testData, password)
        val dec = ArchiveManager.decryptPayload(enc, password)

        assertEquals(testData, dec)
    }
}
