package com.example.util

import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.data.model.TransactionWithDetails

object TransactionLinkHelper {

    /**
     * Finds the linked transfer fee transaction (if any) for a given TRANSFER transaction.
     */
    fun findLinkedFeeTransaction(
        transferTx: Transaction,
        allTransactions: List<TransactionWithDetails>
    ): TransactionWithDetails? {
        if (transferTx.type != TransactionType.TRANSFER) return null

        val tagPattern = "[TransferTx:${transferTx.id}]"

        // 1. Direct tag match in note
        if (transferTx.id > 0L) {
            val byTag = allTransactions.firstOrNull { item ->
                val tx = item.transaction
                tx.type == TransactionType.EXPENSE && tx.note.contains(tagPattern)
            }
            if (byTag != null) return byTag
        }

        // 2. Exact or fuzzy match for transfer fee created alongside this transfer
        return allTransactions.firstOrNull { item ->
            val tx = item.transaction
            if (tx.type != TransactionType.EXPENSE) return@firstOrNull false

            val hasFeeTagOrLabel = tx.referenceNo.contains("Transfer Fee", ignoreCase = true) ||
                    tx.referenceNo.contains("Fee", ignoreCase = true) ||
                    tx.payeeOrPayer.contains("Transfer Fee", ignoreCase = true) ||
                    tx.payeeOrPayer.endsWith("(Fee)", ignoreCase = true) ||
                    tx.note.contains("Transfer fee", ignoreCase = true)

            if (!hasFeeTagOrLabel) return@firstOrNull false

            val timeDiff = Math.abs(tx.dateEpochMs - transferTx.dateEpochMs)
            val isSameAccount = tx.creditAccountId == transferTx.creditAccountId || tx.creditAccountId == transferTx.debitAccountId
            val isPayeeMatch = transferTx.payeeOrPayer.isNotBlank() && (
                    tx.payeeOrPayer.startsWith(transferTx.payeeOrPayer, ignoreCase = true) ||
                    tx.note.contains(transferTx.payeeOrPayer, ignoreCase = true)
            )

            timeDiff <= 180000L && (isSameAccount || isPayeeMatch)
        }
    }

    /**
     * Finds the base TRANSFER transaction linked to a Transfer Fee EXPENSE transaction.
     */
    fun findLinkedBaseTransferTransaction(
        feeTx: Transaction,
        allTransactions: List<TransactionWithDetails>
    ): TransactionWithDetails? {
        if (feeTx.type != TransactionType.EXPENSE) return null

        val hasFeeIndicator = feeTx.referenceNo.contains("Transfer Fee", ignoreCase = true) ||
                feeTx.referenceNo.contains("Fee", ignoreCase = true) ||
                feeTx.payeeOrPayer.contains("Transfer Fee", ignoreCase = true) ||
                feeTx.payeeOrPayer.endsWith("(Fee)", ignoreCase = true) ||
                feeTx.note.contains("Transfer fee", ignoreCase = true) ||
                feeTx.note.contains("[TransferTx:")

        if (!hasFeeIndicator) return null

        // 1. Tag match from note: [TransferTx:123]
        val match = Regex("\\[TransferTx:(\\d+)\\]").find(feeTx.note)
        if (match != null) {
            val transferId = match.groupValues[1].toLongOrNull()
            if (transferId != null) {
                val byId = allTransactions.firstOrNull { it.transaction.id == transferId }
                if (byId != null) return byId
            }
        }

        // 2. Fuzzy match
        return allTransactions.firstOrNull { item ->
            val tx = item.transaction
            if (tx.type != TransactionType.TRANSFER) return@firstOrNull false

            val timeDiff = Math.abs(tx.dateEpochMs - feeTx.dateEpochMs)
            val isSameAccount = feeTx.creditAccountId == tx.creditAccountId || feeTx.creditAccountId == tx.debitAccountId
            val isPayeeMatch = tx.payeeOrPayer.isNotBlank() && (
                    feeTx.payeeOrPayer.startsWith(tx.payeeOrPayer, ignoreCase = true) ||
                    feeTx.note.contains(tx.payeeOrPayer, ignoreCase = true)
            )

            timeDiff <= 180000L && (isSameAccount || isPayeeMatch)
        }
    }

    /**
     * Resolves the base transaction for view / details display.
     * If the clicked item is a Transfer Fee transaction, resolves to its base TRANSFER transaction.
     */
    fun resolveBaseTransactionForView(
        item: TransactionWithDetails,
        allTransactions: List<TransactionWithDetails>
    ): TransactionWithDetails {
        if (item.transaction.type == TransactionType.EXPENSE) {
            val baseTransfer = findLinkedBaseTransferTransaction(item.transaction, allTransactions)
            if (baseTransfer != null) {
                return baseTransfer
            }
        }
        return item
    }

    /**
     * Resolves the base Transaction object (for editing).
     */
    fun resolveBaseTransaction(
        tx: Transaction,
        allTransactions: List<TransactionWithDetails>
    ): Transaction {
        if (tx.type == TransactionType.EXPENSE) {
            val baseTransfer = findLinkedBaseTransferTransaction(tx, allTransactions)
            if (baseTransfer != null) {
                return baseTransfer.transaction
            }
        }
        return tx
    }
}
