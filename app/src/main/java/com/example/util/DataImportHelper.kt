package com.example.util

import android.content.Context
import android.net.Uri
import com.example.data.local.AccountDao
import com.example.data.local.CategoryDao
import com.example.data.local.TransactionDao
import com.example.data.model.Account
import com.example.data.model.AccountType
import com.example.data.model.Category
import com.example.data.model.CategoryType
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DataImportHelper {

    /**
     * Parses standard CSV / Excel exported transaction spreadsheets
     */
    suspend fun importCsv(
        context: Context,
        uri: Uri,
        accountDao: AccountDao,
        categoryDao: CategoryDao,
        transactionDao: TransactionDao
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext Result.failure(Exception("Cannot open CSV file"))

            val reader = BufferedReader(InputStreamReader(inputStream))
            val lines = reader.readLines()
            if (lines.isEmpty()) {
                return@withContext Result.failure(Exception("CSV file is empty"))
            }

            // Existing accounts & categories cache
            val accounts = accountDao.getAllAccountsSnapshot().toMutableList()
            val categories = categoryDao.getAllCategoriesSnapshot().toMutableList()

            // Find or ensure default account
            val defaultAccount = accounts.firstOrNull { it.parentId != null } ?: accounts.firstOrNull()
                ?: run {
                    val acc = Account(nameEn = "Default Cash", nameBn = "ডিফল্ট ক্যাশ", type = AccountType.ASSET, iconName = "Wallet", colorHex = "#10B981")
                    val id = accountDao.insertAccount(acc)
                    acc.copy(id = id).also { accounts.add(it) }
                }

            // Find or ensure default expense category
            val defaultExpenseCat = categories.firstOrNull { it.type == CategoryType.EXPENSE }
                ?: run {
                    val cat = Category(nameEn = "General Expense", nameBn = "সাধারণ খরচ", type = CategoryType.EXPENSE, iconName = "Category", colorHex = "#EF4444")
                    val id = categoryDao.insertCategory(cat)
                    cat.copy(id = id).also { categories.add(it) }
                }

            var importedCount = 0
            val headerIndexMap = mutableMapOf<String, Int>()

            // Identify header row
            var startIndex = 0
            for (i in 0 until minOf(5, lines.size)) {
                val row = parseCsvLine(lines[i])
                val lowerRow = row.map { it.lowercase(Locale.ROOT).trim() }
                if (lowerRow.any { it.contains("date") || it.contains("amount") || it.contains("payee") || it.contains("category") }) {
                    lowerRow.forEachIndexed { colIdx, colName ->
                        when {
                            colName.contains("date") -> headerIndexMap["date"] = colIdx
                            colName.contains("amount") || colName.contains("value") -> headerIndexMap["amount"] = colIdx
                            colName.contains("payee") || colName.contains("description") || colName.contains("name") -> headerIndexMap["payee"] = colIdx
                            colName.contains("category") -> headerIndexMap["category"] = colIdx
                            colName.contains("account") -> headerIndexMap["account"] = colIdx
                            colName.contains("type") -> headerIndexMap["type"] = colIdx
                            colName.contains("note") || colName.contains("memo") -> headerIndexMap["note"] = colIdx
                        }
                    }
                    startIndex = i + 1
                    break
                }
            }

            for (i in startIndex until lines.size) {
                val line = lines[i].trim()
                if (line.isEmpty()) continue
                val tokens = parseCsvLine(line)
                if (tokens.isEmpty()) continue

                val dateStr = headerIndexMap["date"]?.let { tokens.getOrNull(it) } ?: tokens.getOrNull(0) ?: ""
                val amountStr = headerIndexMap["amount"]?.let { tokens.getOrNull(it) } ?: tokens.getOrNull(1) ?: "0"
                val payeeStr = headerIndexMap["payee"]?.let { tokens.getOrNull(it) } ?: tokens.getOrNull(2) ?: ""
                val catStr = headerIndexMap["category"]?.let { tokens.getOrNull(it) } ?: ""
                val accStr = headerIndexMap["account"]?.let { tokens.getOrNull(it) } ?: ""
                val typeStr = headerIndexMap["type"]?.let { tokens.getOrNull(it) } ?: ""
                val noteStr = headerIndexMap["note"]?.let { tokens.getOrNull(it) } ?: ""

                val parsedAmount = parseAmount(amountStr)
                if (parsedAmount == 0.0) continue

                val isNegative = parsedAmount < 0 || amountStr.contains("(") || typeStr.lowercase().contains("expense")
                val absAmount = Math.abs(parsedAmount)

                val txType = when {
                    typeStr.lowercase().contains("transfer") -> TransactionType.TRANSFER
                    typeStr.lowercase().contains("income") -> TransactionType.INCOME
                    isNegative -> TransactionType.EXPENSE
                    else -> TransactionType.INCOME
                }

                val dateEpoch = parseDateEpoch(dateStr)

                // Match account
                val targetAccount = if (accStr.isNotEmpty()) {
                    accounts.find { it.nameEn.equals(accStr, ignoreCase = true) || it.nameBn.equals(accStr, ignoreCase = true) }
                        ?: run {
                            val newAcc = Account(nameEn = accStr, nameBn = accStr, type = AccountType.ASSET, iconName = "AccountBalance", colorHex = "#1E56A0")
                            val newId = accountDao.insertAccount(newAcc)
                            newAcc.copy(id = newId).also { accounts.add(it) }
                        }
                } else defaultAccount

                // Match category
                val targetCategory = if (catStr.isNotEmpty()) {
                    categories.find { it.nameEn.equals(catStr, ignoreCase = true) || it.nameBn.equals(catStr, ignoreCase = true) }
                        ?: run {
                            val newCatType = if (txType == TransactionType.INCOME) CategoryType.INCOME else CategoryType.EXPENSE
                            val newCat = Category(nameEn = catStr, nameBn = catStr, type = newCatType, iconName = "Category", colorHex = "#F59E0B")
                            val newId = categoryDao.insertCategory(newCat)
                            newCat.copy(id = newId).also { categories.add(it) }
                        }
                } else defaultExpenseCat

                val tx = Transaction(
                    type = txType,
                    amount = absAmount,
                    dateEpochMs = dateEpoch,
                    debitAccountId = if (txType == TransactionType.EXPENSE) null else targetAccount.id,
                    creditAccountId = if (txType == TransactionType.EXPENSE) targetAccount.id else null,
                    categoryId = targetCategory.id,
                    payeeOrPayer = payeeStr.ifEmpty { "Imported" },
                    note = if (noteStr.isNotEmpty()) noteStr else "Imported from Excel CSV"
                )

                transactionDao.insertTransaction(tx)
                importedCount++
            }

            Result.success(importedCount)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Parses Quicken Interchange Format (.qif) files
     */
    suspend fun importQif(
        context: Context,
        uri: Uri,
        accountDao: AccountDao,
        categoryDao: CategoryDao,
        transactionDao: TransactionDao
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext Result.failure(Exception("Cannot open QIF file"))

            val reader = BufferedReader(InputStreamReader(inputStream))
            val lines = reader.readLines()
            if (lines.isEmpty()) {
                return@withContext Result.failure(Exception("QIF file is empty"))
            }

            val accounts = accountDao.getAllAccountsSnapshot().toMutableList()
            val categories = categoryDao.getAllCategoriesSnapshot().toMutableList()

            val defaultAccount = accounts.firstOrNull { it.parentId != null } ?: accounts.firstOrNull()
                ?: run {
                    val acc = Account(nameEn = "QIF Account", nameBn = "QIF হিসাব", type = AccountType.ASSET, iconName = "AccountBalance", colorHex = "#1E56A0")
                    val id = accountDao.insertAccount(acc)
                    acc.copy(id = id).also { accounts.add(it) }
                }

            var importedCount = 0
            var currentAccount = defaultAccount

            var curDateStr = ""
            var curAmount = 0.0
            var curPayee = ""
            var curMemo = ""
            var curCategory = ""

            for (line in lines) {
                val trimmed = line.trim()
                if (trimmed.isEmpty()) continue

                if (trimmed.startsWith("!Account")) {
                    // Account header follows
                    continue
                }

                if (trimmed.startsWith("!Type:")) {
                    // Start of transactions section
                    continue
                }

                when (trimmed[0]) {
                    'D' -> curDateStr = trimmed.substring(1).trim()
                    'T', 'U' -> curAmount = parseAmount(trimmed.substring(1).trim())
                    'P' -> curPayee = trimmed.substring(1).trim()
                    'M' -> curMemo = trimmed.substring(1).trim()
                    'L' -> curCategory = trimmed.substring(1).trim()
                    'N' -> {
                        // Account name or check number
                        if (trimmed.length > 1 && !trimmed.substring(1).all { it.isDigit() }) {
                            val accName = trimmed.substring(1).trim()
                            currentAccount = accounts.find { it.nameEn.equals(accName, ignoreCase = true) }
                                ?: run {
                                    val newAcc = Account(nameEn = accName, nameBn = accName, type = AccountType.ASSET, iconName = "AccountBalance", colorHex = "#10B981")
                                    val id = accountDao.insertAccount(newAcc)
                                    newAcc.copy(id = id).also { accounts.add(it) }
                                }
                        }
                    }
                    '^' -> {
                        // End of record
                        if (curAmount != 0.0 || curPayee.isNotEmpty()) {
                            val isExpense = curAmount < 0
                            val absAmount = Math.abs(curAmount)
                            val txType = if (isExpense) TransactionType.EXPENSE else TransactionType.INCOME
                            val dateEpoch = parseDateEpoch(curDateStr)

                            val category = if (curCategory.isNotEmpty()) {
                                categories.find { it.nameEn.equals(curCategory, ignoreCase = true) }
                                    ?: run {
                                        val newCat = Category(
                                            nameEn = curCategory,
                                            nameBn = curCategory,
                                            type = if (isExpense) CategoryType.EXPENSE else CategoryType.INCOME,
                                            iconName = "Category",
                                            colorHex = if (isExpense) "#EF4444" else "#10B981"
                                        )
                                        val id = categoryDao.insertCategory(newCat)
                                        newCat.copy(id = id).also { categories.add(it) }
                                    }
                            } else null

                            val tx = Transaction(
                                type = txType,
                                amount = if (absAmount > 0) absAmount else 1.0,
                                dateEpochMs = dateEpoch,
                                debitAccountId = if (txType == TransactionType.EXPENSE) null else currentAccount.id,
                                creditAccountId = if (txType == TransactionType.EXPENSE) currentAccount.id else null,
                                categoryId = category?.id,
                                payeeOrPayer = curPayee.ifEmpty { "QIF Record" },
                                note = if (curMemo.isNotEmpty()) curMemo else "Imported from QIF"
                            )

                            transactionDao.insertTransaction(tx)
                            importedCount++
                        }

                        // Reset current record state
                        curDateStr = ""
                        curAmount = 0.0
                        curPayee = ""
                        curMemo = ""
                        curCategory = ""
                    }
                }
            }

            Result.success(importedCount)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    private fun parseCsvLine(line: String): List<String> {
        val tokens = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false

        for (ch in line) {
            when {
                ch == '\"' -> inQuotes = !inQuotes
                ch == ',' && !inQuotes -> {
                    tokens.add(sb.toString().trim().replace("\"", ""))
                    sb.clear()
                }
                else -> sb.append(ch)
            }
        }
        tokens.add(sb.toString().trim().replace("\"", ""))
        return tokens
    }

    private fun parseAmount(str: String): Double {
        val cleaned = str.replace("$", "").replace("৳", "").replace("€", "").replace("£", "")
            .replace(",", "").replace(" ", "").trim()
        return try {
            if (cleaned.startsWith("(") && cleaned.endsWith(")")) {
                -cleaned.substring(1, cleaned.length - 1).toDouble()
            } else {
                cleaned.toDouble()
            }
        } catch (e: Exception) {
            0.0
        }
    }

    private fun parseDateEpoch(dateStr: String): Long {
        if (dateStr.isBlank()) return System.currentTimeMillis()
        val patterns = listOf(
            "yyyy-MM-dd",
            "dd/MM/yyyy",
            "MM/dd/yyyy",
            "dd-MM-yyyy",
            "yyyy/MM/dd",
            "dd-MMM-yyyy",
            "dd MMM yyyy",
            "yyyyMMdd",
            "MM/dd/yy",
            "dd/MM/yy"
        )
        for (pattern in patterns) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale.US)
                val date = sdf.parse(dateStr.trim())
                if (date != null) return date.time
            } catch (_: Exception) {}
        }
        return System.currentTimeMillis()
    }
}
