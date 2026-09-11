package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.LanguageMode
import com.example.data.model.TransactionType
import com.example.data.model.TransactionWithDetails
import com.example.ui.screens.AggregatedItem
import com.example.ui.screens.AggregatedLabel
import com.example.ui.screens.CategoryGroupBudgetTracking
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ExportFormat(val labelEn: String, val labelBn: String, val ext: String, val mimeType: String) {
    PDF("PDF Document", "পিডিএফ ডকুমেন্ট", "pdf", "application/pdf"),
    CSV("CSV Spreadsheet", "সিএসভি স্প্রেডশীট", "csv", "text/csv"),
    HTML("HTML Webpage", "এইচটিএমএল রিপোর্ট", "html", "text/html"),
    JSON("JSON Data", "জেসন ডেটা", "json", "application/json")
}

object TabExportHelper {

    private fun formatTimestamp(epochMs: Long): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(epochMs))
    }

    // ==========================================
    // 1. TRANSACTIONS TAB EXPORT
    // ==========================================
    fun exportTransactions(
        context: Context,
        format: ExportFormat,
        transactions: List<TransactionWithDetails>,
        filterSummary: String = "",
        languageMode: LanguageMode = LanguageMode.ENGLISH
    ) {
        val title = if (languageMode == LanguageMode.BANGLA) "লেনদেন রিপোর্ট" else "Transactions Report"
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val dateDisplay = DateUtils.formatDate(System.currentTimeMillis(), languageMode)

        val totalExpense = transactions.filter { it.transaction.type == TransactionType.EXPENSE }.sumOf { it.transaction.amount }
        val totalIncome = transactions.filter { it.transaction.type == TransactionType.INCOME }.sumOf { it.transaction.amount }
        val netBalance = totalIncome - totalExpense

        when (format) {
            ExportFormat.PDF -> {
                val html = buildTransactionsHtml(title, dateDisplay, filterSummary, transactions, totalExpense, totalIncome, netBalance, languageMode)
                PdfPrintHelper.printHtml(context, "Transactions_$timeStamp", html, isLandscape = true)
            }
            ExportFormat.CSV -> {
                val csv = buildTransactionsCsv(transactions, filterSummary, languageMode)
                shareFile(context, "Transactions_$timeStamp.csv", format.mimeType, csv, title)
            }
            ExportFormat.HTML -> {
                val html = buildTransactionsHtml(title, dateDisplay, filterSummary, transactions, totalExpense, totalIncome, netBalance, languageMode)
                shareFile(context, "Transactions_$timeStamp.html", format.mimeType, html, title)
            }
            ExportFormat.JSON -> {
                val json = JSONObject().apply {
                    put("reportType", "Transactions")
                    put("generatedAt", formatTimestamp(System.currentTimeMillis()))
                    if (filterSummary.isNotBlank()) put("filter", filterSummary)
                    put("totalTransactions", transactions.size)
                    put("totalExpense", totalExpense)
                    put("totalIncome", totalIncome)
                    put("netBalance", netBalance)
                    put("transactions", JSONArray().apply {
                        transactions.forEach { tx ->
                            val timeStr = SimpleDateFormat("hh:mm a", Locale.US).format(Date(tx.transaction.dateEpochMs))
                            val catGroup = if (tx.subCategory != null) {
                                tx.category?.nameEn ?: ""
                            } else if (tx.category?.parentId != null) {
                                "Sub-Category"
                            } else {
                                tx.category?.nameEn ?: ""
                            }
                            val categoryName = tx.subCategory?.nameEn ?: (tx.category?.nameEn ?: "")
                            val accGroup = when (tx.transaction.type) {
                                TransactionType.EXPENSE -> tx.creditAccount?.let { if (it.parentId != null) "Sub-Account" else it.type.name } ?: ""
                                TransactionType.INCOME -> tx.debitAccount?.let { if (it.parentId != null) "Sub-Account" else it.type.name } ?: ""
                                TransactionType.TRANSFER -> "Transfer"
                            }
                            val accName = when (tx.transaction.type) {
                                TransactionType.EXPENSE -> tx.creditAccount?.nameEn ?: ""
                                TransactionType.INCOME -> tx.debitAccount?.nameEn ?: ""
                                TransactionType.TRANSFER -> "${tx.creditAccount?.nameEn ?: ""} → ${tx.debitAccount?.nameEn ?: ""}"
                            }

                            put(JSONObject().apply {
                                put("id", tx.transaction.id)
                                put("name", tx.transaction.payeeOrPayer)
                                put("date", DateUtils.formatDate(tx.transaction.dateEpochMs, languageMode))
                                put("time", timeStr)
                                put("amount", tx.transaction.amount)
                                put("type", tx.transaction.type.name)
                                put("categoryGroup", catGroup)
                                put("category", categoryName)
                                put("accountGroup", accGroup)
                                put("account", accName)
                                put("debitAccount", tx.debitAccount?.nameEn ?: "")
                                put("creditAccount", tx.creditAccount?.nameEn ?: "")
                                put("labels", tx.transaction.referenceNo)
                                put("notes", tx.transaction.note)
                                put("status", tx.transaction.status.name)
                            })
                        }
                    })
                }.toString(2)
                shareFile(context, "Transactions_$timeStamp.json", format.mimeType, json, title)
            }
        }
    }

    private fun buildTransactionsCsv(
        transactions: List<TransactionWithDetails>,
        filterSummary: String,
        languageMode: LanguageMode
    ): String {
        val sb = StringBuilder()
        if (filterSummary.isNotBlank()) {
            sb.append("# Filter: ${escapeCsv(filterSummary)}\n")
        }
        sb.append("Name,Date,Time,Amount (BDT),Type,Category Group,Category,Account Group,Account,Debit Account,Credit Account,Labels,Notes,Status\n")
        for (tx in transactions) {
            val nameStr = tx.transaction.payeeOrPayer
            val dateStr = DateUtils.formatDate(tx.transaction.dateEpochMs, languageMode)
            val timeStr = SimpleDateFormat("hh:mm a", Locale.US).format(Date(tx.transaction.dateEpochMs))
            val amt = String.format(Locale.US, "%.2f", tx.transaction.amount)
            val typeStr = tx.transaction.type.name

            val catGroup = if (tx.subCategory != null) {
                LanguageHelper.getLocalizedName(tx.category?.nameEn ?: "", tx.category?.nameBn ?: "", languageMode)
            } else if (tx.category?.parentId != null) {
                if (languageMode == LanguageMode.BANGLA) "সাব-ক্যাটাগরি" else "Sub-Category"
            } else {
                LanguageHelper.getLocalizedName(tx.category?.nameEn ?: "", tx.category?.nameBn ?: "", languageMode)
            }
            val catName = if (tx.subCategory != null) {
                LanguageHelper.getLocalizedName(tx.subCategory.nameEn, tx.subCategory.nameBn, languageMode)
            } else {
                LanguageHelper.getLocalizedName(tx.category?.nameEn ?: "", tx.category?.nameBn ?: "", languageMode)
            }

            val accGroup = when (tx.transaction.type) {
                TransactionType.EXPENSE -> tx.creditAccount?.let {
                    if (it.parentId != null) (if (languageMode == LanguageMode.BANGLA) "সাব-অ্যাকাউন্ট" else "Sub-Account")
                    else (if (languageMode == LanguageMode.BANGLA) "সম্পদ" else it.type.name)
                } ?: ""
                TransactionType.INCOME -> tx.debitAccount?.let {
                    if (it.parentId != null) (if (languageMode == LanguageMode.BANGLA) "সাব-অ্যাকাউন্ট" else "Sub-Account")
                    else (if (languageMode == LanguageMode.BANGLA) "সম্পদ" else it.type.name)
                } ?: ""
                TransactionType.TRANSFER -> if (languageMode == LanguageMode.BANGLA) "স্থানান্তর" else "Transfer"
            }

            val accName = when (tx.transaction.type) {
                TransactionType.EXPENSE -> LanguageHelper.getLocalizedName(tx.creditAccount?.nameEn ?: "", tx.creditAccount?.nameBn ?: "", languageMode)
                TransactionType.INCOME -> LanguageHelper.getLocalizedName(tx.debitAccount?.nameEn ?: "", tx.debitAccount?.nameBn ?: "", languageMode)
                TransactionType.TRANSFER -> {
                    val from = LanguageHelper.getLocalizedName(tx.creditAccount?.nameEn ?: "", tx.creditAccount?.nameBn ?: "", languageMode)
                    val to = LanguageHelper.getLocalizedName(tx.debitAccount?.nameEn ?: "", tx.debitAccount?.nameBn ?: "", languageMode)
                    "$from → $to"
                }
            }

            val debitAcc = LanguageHelper.getLocalizedName(tx.debitAccount?.nameEn ?: "", tx.debitAccount?.nameBn ?: "", languageMode)
            val creditAcc = LanguageHelper.getLocalizedName(tx.creditAccount?.nameEn ?: "", tx.creditAccount?.nameBn ?: "", languageMode)
            val labels = tx.transaction.referenceNo
            val notes = tx.transaction.note
            val status = tx.transaction.status.name

            sb.append("${escapeCsv(nameStr)},${escapeCsv(dateStr)},${escapeCsv(timeStr)},$amt,${escapeCsv(typeStr)},${escapeCsv(catGroup)},${escapeCsv(catName)},${escapeCsv(accGroup)},${escapeCsv(accName)},${escapeCsv(debitAcc)},${escapeCsv(creditAcc)},${escapeCsv(labels)},${escapeCsv(notes)},${escapeCsv(status)}\n")
        }
        return sb.toString()
    }

    private fun buildTransactionsHtml(
        title: String,
        dateDisplay: String,
        filterSummary: String,
        transactions: List<TransactionWithDetails>,
        totalExpense: Double,
        totalIncome: Double,
        netBalance: Double,
        languageMode: LanguageMode
    ): String {
        val sb = StringBuilder()
        sb.append("""
            <div class="summary-cards">
                <div class="card">
                    <div class="card-label">${if (languageMode == LanguageMode.BANGLA) "মোট ব্যয়" else "Total Expense"}</div>
                    <div class="card-value text-danger">৳ ${LanguageHelper.formatNumber(totalExpense, languageMode)}</div>
                </div>
                <div class="card">
                    <div class="card-label">${if (languageMode == LanguageMode.BANGLA) "মোট আয়" else "Total Income"}</div>
                    <div class="card-value text-success">৳ ${LanguageHelper.formatNumber(totalIncome, languageMode)}</div>
                </div>
                <div class="card">
                    <div class="card-label">${if (languageMode == LanguageMode.BANGLA) "নেট ব্যালেন্স" else "Net Balance"}</div>
                    <div class="card-value ${if (netBalance >= 0) "text-success" else "text-danger"}">৳ ${LanguageHelper.formatNumber(netBalance, languageMode)}</div>
                </div>
                <div class="card">
                    <div class="card-label">${if (languageMode == LanguageMode.BANGLA) "মোট লেনদেন" else "Transactions"}</div>
                    <div class="card-value">${transactions.size}</div>
                </div>
            </div>
            <table>
                <thead>
                    <tr>
                        <th>${if (languageMode == LanguageMode.BANGLA) "নাম / পেয়ী" else "Name (Payee)"}</th>
                        <th>${if (languageMode == LanguageMode.BANGLA) "তারিখ ও সময়" else "Date & Time"}</th>
                        <th>${if (languageMode == LanguageMode.BANGLA) "ধরন" else "Type"}</th>
                        <th style="text-align: right;">${if (languageMode == LanguageMode.BANGLA) "পরিমাণ" else "Amount"}</th>
                        <th>${if (languageMode == LanguageMode.BANGLA) "গ্রুপ ও ক্যাটাগরি" else "Category Group & Category"}</th>
                        <th>${if (languageMode == LanguageMode.BANGLA) "অ্যাকাউন্ট গ্রুপ ও অ্যাকাউন্ট" else "Account Group & Account"}</th>
                        <th>${if (languageMode == LanguageMode.BANGLA) "লেবেল" else "Labels"}</th>
                        <th>${if (languageMode == LanguageMode.BANGLA) "নোট" else "Notes"}</th>
                        <th>${if (languageMode == LanguageMode.BANGLA) "স্ট্যাটাস" else "Status"}</th>
                    </tr>
                </thead>
                <tbody>
        """.trimIndent())

        for (tx in transactions) {
            val dateStr = DateUtils.formatDate(tx.transaction.dateEpochMs, languageMode)
            val timeStr = SimpleDateFormat("hh:mm a", Locale.US).format(Date(tx.transaction.dateEpochMs))
            val cat = LanguageHelper.getLocalizedName(tx.category?.nameEn ?: "-", tx.category?.nameBn ?: "-", languageMode)
            val subCat = tx.subCategory?.let { " (${LanguageHelper.getLocalizedName(it.nameEn, it.nameBn, languageMode)})" } ?: ""

            val catGroup = if (tx.subCategory != null) {
                LanguageHelper.getLocalizedName(tx.category?.nameEn ?: "", tx.category?.nameBn ?: "", languageMode)
            } else if (tx.category?.parentId != null) {
                if (languageMode == LanguageMode.BANGLA) "সাব-ক্যাটাগরি" else "Sub-Category"
            } else {
                LanguageHelper.getLocalizedName(tx.category?.nameEn ?: "", tx.category?.nameBn ?: "", languageMode)
            }
            val catDisplay = if (tx.subCategory != null) {
                LanguageHelper.getLocalizedName(tx.subCategory.nameEn, tx.subCategory.nameBn, languageMode)
            } else {
                LanguageHelper.getLocalizedName(tx.category?.nameEn ?: "-", tx.category?.nameBn ?: "-", languageMode)
            }

            val accGroup = when (tx.transaction.type) {
                TransactionType.EXPENSE -> tx.creditAccount?.let {
                    if (it.parentId != null) (if (languageMode == LanguageMode.BANGLA) "সাব-অ্যাকাউন্ট" else "Sub-Account")
                    else (if (languageMode == LanguageMode.BANGLA) "সম্পদ" else it.type.name)
                } ?: ""
                TransactionType.INCOME -> tx.debitAccount?.let {
                    if (it.parentId != null) (if (languageMode == LanguageMode.BANGLA) "সাব-অ্যাকাউন্ট" else "Sub-Account")
                    else (if (languageMode == LanguageMode.BANGLA) "সম্পদ" else it.type.name)
                } ?: ""
                TransactionType.TRANSFER -> if (languageMode == LanguageMode.BANGLA) "স্থানান্তর" else "Transfer"
            }

            val accDisplay = when (tx.transaction.type) {
                TransactionType.EXPENSE -> LanguageHelper.getLocalizedName(tx.creditAccount?.nameEn ?: "-", tx.creditAccount?.nameBn ?: "-", languageMode)
                TransactionType.INCOME -> LanguageHelper.getLocalizedName(tx.debitAccount?.nameEn ?: "-", tx.debitAccount?.nameBn ?: "-", languageMode)
                TransactionType.TRANSFER -> {
                    val from = LanguageHelper.getLocalizedName(tx.creditAccount?.nameEn ?: "-", tx.creditAccount?.nameBn ?: "-", languageMode)
                    val to = LanguageHelper.getLocalizedName(tx.debitAccount?.nameEn ?: "-", tx.debitAccount?.nameBn ?: "-", languageMode)
                    "$from → $to"
                }
            }

            val colorClass = when (tx.transaction.type) {
                TransactionType.EXPENSE -> "text-danger"
                TransactionType.INCOME -> "text-success"
                TransactionType.TRANSFER -> "text-primary"
            }

            sb.append("""
                <tr>
                    <td><strong>${escapeHtml(tx.transaction.payeeOrPayer.ifBlank { "-" })}</strong></td>
                    <td>$dateStr<br><span style="color:#64748b; font-size:10px;">$timeStr</span></td>
                    <td><span class="badge badge-${tx.transaction.type.name.lowercase()}">${tx.transaction.type.name}</span></td>
                    <td style="text-align: right;" class="$colorClass"><strong>৳ ${LanguageHelper.formatNumber(tx.transaction.amount, languageMode)}</strong></td>
                    <td><span style="color:#64748b; font-size:11px;">$catGroup</span><br><strong>$catDisplay</strong></td>
                    <td><span style="color:#64748b; font-size:11px;">$accGroup</span><br>$accDisplay</td>
                    <td>${escapeHtml(tx.transaction.referenceNo.ifBlank { "-" })}</td>
                    <td>${escapeHtml(tx.transaction.note.ifBlank { "-" })}</td>
                    <td><span style="font-size:11px; color:#475569;">${tx.transaction.status.name}</span></td>
                </tr>
            """.trimIndent())
        }

        sb.append("</tbody></table>")
        return buildBaseHtml(title, "$dateDisplay ${if (filterSummary.isNotEmpty()) "• $filterSummary" else ""}", sb.toString())
    }

    // ==========================================
    // 2. BUDGET TAB EXPORT
    // ==========================================
    fun exportBudget(
        context: Context,
        format: ExportFormat,
        periodLabel: String,
        activeTabMode: String,
        groups: List<CategoryGroupBudgetTracking>,
        totalBudget: Double,
        totalSpent: Double,
        showComparison: Boolean = false,
        baseDateLabel: String = "",
        compareDateLabel: String = "",
        totalSpentBase: Double = 0.0,
        totalIncomeBudget: Double = 0.0,
        totalIncomeActual: Double = 0.0,
        totalExpenseBudget: Double = 0.0,
        totalExpenseActual: Double = 0.0,
        netWorth: Double = 0.0,
        languageMode: LanguageMode = LanguageMode.ENGLISH
    ) {
        val title = if (languageMode == LanguageMode.BANGLA) {
            if (showComparison && baseDateLabel.isNotBlank()) "বাজেট তুলনামূলক রিপোর্ট ($compareDateLabel বনাম $baseDateLabel)"
            else "বাজেট ট্র্যাকিং রিপোর্ট ($periodLabel)"
        } else {
            if (showComparison && baseDateLabel.isNotBlank()) "Budget Comparison Report ($compareDateLabel vs $baseDateLabel)"
            else "Budget Tracking Report ($periodLabel)"
        }
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val remaining = totalBudget - totalSpent
        val netSavings = totalIncomeActual - totalExpenseActual
        val deltaSpent = totalSpent - totalSpentBase
        val deltaPercent = if (totalSpentBase > 0.001) (deltaSpent / totalSpentBase) * 100.0 else 0.0

        when (format) {
            ExportFormat.PDF -> {
                val html = buildBudgetHtml(
                    title, periodLabel, activeTabMode, groups, totalBudget, totalSpent, remaining,
                    showComparison, baseDateLabel, compareDateLabel, totalSpentBase, deltaSpent, deltaPercent,
                    totalIncomeBudget, totalIncomeActual, totalExpenseBudget, totalExpenseActual,
                    netSavings, netWorth, languageMode
                )
                PdfPrintHelper.printHtml(context, "Budget_$timeStamp", html, isLandscape = showComparison)
            }
            ExportFormat.CSV -> {
                val csv = buildBudgetCsv(
                    periodLabel, activeTabMode, groups, showComparison,
                    baseDateLabel, compareDateLabel, totalSpentBase, deltaSpent, deltaPercent,
                    totalIncomeBudget, totalIncomeActual, totalExpenseBudget, totalExpenseActual,
                    netSavings, netWorth, languageMode
                )
                shareFile(context, "Budget_$timeStamp.csv", format.mimeType, csv, title)
            }
            ExportFormat.HTML -> {
                val html = buildBudgetHtml(
                    title, periodLabel, activeTabMode, groups, totalBudget, totalSpent, remaining,
                    showComparison, baseDateLabel, compareDateLabel, totalSpentBase, deltaSpent, deltaPercent,
                    totalIncomeBudget, totalIncomeActual, totalExpenseBudget, totalExpenseActual,
                    netSavings, netWorth, languageMode
                )
                shareFile(context, "Budget_$timeStamp.html", format.mimeType, html, title)
            }
            ExportFormat.JSON -> {
                val json = JSONObject().apply {
                    put("reportType", "BudgetTracking")
                    put("period", periodLabel)
                    put("tabMode", activeTabMode)
                    put("totalBudget", totalBudget)
                    put("totalSpent", totalSpent)
                    put("remaining", remaining)
                    put("comparisonEnabled", showComparison)
                    if (showComparison) {
                        put("basePeriod", baseDateLabel)
                        put("comparePeriod", compareDateLabel)
                        put("totalSpentBase", totalSpentBase)
                        put("deltaSpent", deltaSpent)
                        put("deltaPercent", deltaPercent)
                    }
                    put("income", JSONObject().apply {
                        put("budget", totalIncomeBudget)
                        put("actual", totalIncomeActual)
                    })
                    put("expense", JSONObject().apply {
                        put("budget", totalExpenseBudget)
                        put("actual", totalExpenseActual)
                    })
                    put("netSavings", netSavings)
                    put("netWorth", netWorth)
                    put("groups", JSONArray().apply {
                        groups.forEach { grp ->
                            put(JSONObject().apply {
                                put("groupName", grp.groupNameEn)
                                put("totalBudget", grp.totalBudget)
                                put("totalSpentCurrent", grp.totalSpent)
                                if (showComparison) {
                                    put("totalSpentBase", grp.totalSpentBase)
                                    put("deltaSpent", grp.deltaSpent)
                                    put("deltaPercent", grp.deltaPercent)
                                }
                                put("categories", JSONArray().apply {
                                    grp.items.forEach { item ->
                                        put(JSONObject().apply {
                                            put("categoryName", item.category.nameEn)
                                            put("budget", item.budgetLimit)
                                            put("spentCurrent", item.spentAmount)
                                            put("remaining", item.budgetLimit - item.spentAmount)
                                            put("budgetUsagePercent", item.percentageInt)
                                            if (showComparison) {
                                                put("spentBase", item.spentBaseAmount)
                                                put("deltaSpent", item.deltaSpent)
                                                put("deltaPercent", item.deltaPercent)
                                            }
                                        })
                                    }
                                })
                            })
                        }
                    })
                }.toString(2)
                shareFile(context, "Budget_$timeStamp.json", format.mimeType, json, title)
            }
        }
    }

    private fun buildBudgetCsv(
        periodLabel: String,
        activeTabMode: String,
        groups: List<CategoryGroupBudgetTracking>,
        showComparison: Boolean,
        baseDateLabel: String,
        compareDateLabel: String,
        totalSpentBase: Double,
        deltaSpent: Double,
        deltaPercent: Double,
        totalIncomeBudget: Double,
        totalIncomeActual: Double,
        totalExpenseBudget: Double,
        totalExpenseActual: Double,
        netSavings: Double,
        netWorth: Double,
        languageMode: LanguageMode
    ): String {
        val sb = StringBuilder()
        if (periodLabel.isNotBlank()) {
            sb.append("# Filter / Period: ${escapeCsv(periodLabel)}\n")
        }
        sb.append("# Net Worth: BDT ${String.format(Locale.US, "%.2f", netWorth)}\n")
        sb.append("# Total Income: BDT ${String.format(Locale.US, "%.2f", totalIncomeActual)} (Budget: BDT ${String.format(Locale.US, "%.2f", totalIncomeBudget)})\n")
        sb.append("# Total Expense: BDT ${String.format(Locale.US, "%.2f", totalExpenseActual)} (Budget: BDT ${String.format(Locale.US, "%.2f", totalExpenseBudget)})\n")
        sb.append("# Net Savings / Flow: BDT ${String.format(Locale.US, "%.2f", netSavings)}\n")

        if (showComparison) {
            val baseLbl = baseDateLabel.ifBlank { "Base Period" }
            val currLbl = compareDateLabel.ifBlank { "Current Period" }
            sb.append("Group,Category,Budget Limit (BDT),Current Spent - $currLbl (BDT),Base Spent - $baseLbl (BDT),Variance (BDT),Change (%),Budget Usage (%)\n")
        } else {
            sb.append("Group,Category,Budget Limit (BDT),Actual Spent (BDT),Remaining (BDT),Budget Usage (%)\n")
        }

        for (group in groups) {
            val groupName = LanguageHelper.getLocalizedName(group.groupNameEn, group.groupNameBn, languageMode)
            for (item in group.items) {
                val catName = LanguageHelper.getLocalizedName(item.category.nameEn, item.category.nameBn, languageMode)
                val budget = String.format(Locale.US, "%.2f", item.budgetLimit)
                val spent = String.format(Locale.US, "%.2f", item.spentAmount)
                if (showComparison) {
                    val baseSpent = String.format(Locale.US, "%.2f", item.spentBaseAmount)
                    val variance = String.format(Locale.US, "%.2f", item.deltaSpent)
                    val pctChange = if (Math.abs(item.spentBaseAmount) > 0.001) String.format(Locale.US, "%.1f%%", item.deltaPercent) else "-"
                    sb.append("${escapeCsv(groupName)},${escapeCsv(catName)},$budget,$spent,$baseSpent,$variance,$pctChange,${item.percentageInt}%\n")
                } else {
                    val remaining = String.format(Locale.US, "%.2f", item.budgetLimit - item.spentAmount)
                    sb.append("${escapeCsv(groupName)},${escapeCsv(catName)},$budget,$spent,$remaining,${item.percentageInt}%\n")
                }
            }
        }
        return sb.toString()
    }

    private fun buildBudgetHtml(
        title: String,
        periodLabel: String,
        activeTabMode: String,
        groups: List<CategoryGroupBudgetTracking>,
        totalBudget: Double,
        totalSpent: Double,
        remaining: Double,
        showComparison: Boolean,
        baseDateLabel: String,
        compareDateLabel: String,
        totalSpentBase: Double,
        deltaSpent: Double,
        deltaPercent: Double,
        totalIncomeBudget: Double,
        totalIncomeActual: Double,
        totalExpenseBudget: Double,
        totalExpenseActual: Double,
        netSavings: Double,
        netWorth: Double,
        languageMode: LanguageMode
    ): String {
        val sb = StringBuilder()
        val percentOverall = if (totalBudget > 0) ((totalSpent / totalBudget) * 100).toInt() else 0

        if (showComparison) {
            val currLbl = compareDateLabel.ifBlank { "Current" }
            val baseLbl = baseDateLabel.ifBlank { "Base" }
            val deltaClass = if (deltaSpent <= 0) "badge-positive" else "badge-negative"
            val deltaArrow = if (deltaSpent > 0) "▲" else if (deltaSpent < 0) "▼" else "—"
            val deltaSign = if (deltaSpent > 0) "+" else ""

            sb.append("""
                <div class="summary-cards">
                    <div class="card card-primary">
                        <div class="card-label">${if (languageMode == LanguageMode.BANGLA) "মোট বাজেট" else "Total Budget"}</div>
                        <div class="card-value text-primary">৳ ${LanguageHelper.formatNumber(totalBudget, languageMode)}</div>
                        <div class="card-sub">${if (languageMode == LanguageMode.BANGLA) "সর্বমোট বরাদ্দ" else "Allocated Limit"}</div>
                    </div>
                    <div class="card card-danger">
                        <div class="card-label">${if (languageMode == LanguageMode.BANGLA) "বর্তমান ব্যয় ($currLbl)" else "Current Spent ($currLbl)"}</div>
                        <div class="card-value text-danger">৳ ${LanguageHelper.formatNumber(totalSpent, languageMode)}</div>
                        <div class="card-sub">${if (languageMode == LanguageMode.BANGLA) "ব্যবহার: " else "Usage: "}<strong>$percentOverall%</strong></div>
                    </div>
                    <div class="card card-neutral">
                        <div class="card-label">${if (languageMode == LanguageMode.BANGLA) "পূর্ববর্তী ব্যয় ($baseLbl)" else "Base Period ($baseLbl)"}</div>
                        <div class="card-value text-neutral">৳ ${LanguageHelper.formatNumber(totalSpentBase, languageMode)}</div>
                        <div class="card-sub">${if (languageMode == LanguageMode.BANGLA) "ভিত্তি সময়কাল" else "Baseline Spending"}</div>
                    </div>
                    <div class="card ${if (deltaSpent <= 0) "card-success" else "card-danger"}">
                        <div class="card-label">${if (languageMode == LanguageMode.BANGLA) "পার্থক্য ও পরিবর্তন" else "Variance & Growth"}</div>
                        <div class="card-value ${if (deltaSpent <= 0) "text-success" else "text-danger"}">$deltaSign৳ ${LanguageHelper.formatNumber(deltaSpent, languageMode)}</div>
                        <div style="margin-top: 4px;"><span class="badge $deltaClass">$deltaSign${String.format(Locale.US, "%.1f", deltaPercent)}% $deltaArrow</span></div>
                    </div>
                </div>

                <table>
                    <thead>
                        <tr>
                            <th style="width: 25%;">${if (languageMode == LanguageMode.BANGLA) "ক্যাটাগরি গ্রুপ ও আইটেম" else "Category Group & Item"}</th>
                            <th style="text-align: right;">${if (languageMode == LanguageMode.BANGLA) "বাজেট" else "Budget"}</th>
                            <th style="text-align: right;">${if (languageMode == LanguageMode.BANGLA) "পূর্ববর্তী ($baseLbl)" else "Base ($baseLbl)"}</th>
                            <th style="text-align: right;">${if (languageMode == LanguageMode.BANGLA) "বর্তমান ($currLbl)" else "Current ($currLbl)"}</th>
                            <th style="text-align: right;">${if (languageMode == LanguageMode.BANGLA) "পার্থক্য" else "Variance"}</th>
                            <th style="text-align: right;">${if (languageMode == LanguageMode.BANGLA) "পরিবর্তন %" else "Change %"}</th>
                            <th style="width: 18%;">${if (languageMode == LanguageMode.BANGLA) "বাজেট ব্যবহার" else "Budget Utilization"}</th>
                        </tr>
                    </thead>
                    <tbody>
            """.trimIndent())

            for (grp in groups) {
                val grpName = LanguageHelper.getLocalizedName(grp.groupNameEn, grp.groupNameBn, languageMode)
                val grpDelta = grp.deltaSpent
                val grpDeltaSign = if (grpDelta > 0) "+" else ""
                val grpDeltaClass = if (grpDelta <= 0) "text-success" else "text-danger"
                val grpPctClass = if (grpDelta <= 0) "badge-positive" else "badge-negative"
                val grpArrow = if (grpDelta > 0) "▲" else if (grpDelta < 0) "▼" else "—"

                sb.append("""
                    <tr class="group-row">
                        <td><strong>📁 $grpName</strong></td>
                        <td style="text-align: right; font-weight: bold;">৳ ${LanguageHelper.formatNumber(grp.totalBudget, languageMode)}</td>
                        <td style="text-align: right; font-weight: bold;">৳ ${LanguageHelper.formatNumber(grp.totalSpentBase, languageMode)}</td>
                        <td style="text-align: right; font-weight: bold; color: #dc2626;">৳ ${LanguageHelper.formatNumber(grp.totalSpent, languageMode)}</td>
                        <td style="text-align: right; font-weight: bold;" class="$grpDeltaClass">$grpDeltaSign৳ ${LanguageHelper.formatNumber(grpDelta, languageMode)}</td>
                        <td style="text-align: right;"><span class="badge $grpPctClass">$grpDeltaSign${String.format(Locale.US, "%.1f", grp.deltaPercent)}% $grpArrow</span></td>
                        <td>
                            <div class="progress-container">
                                <div class="progress-bar"><div class="progress-fill ${if (grp.percentageInt > 100) "fill-red" else if (grp.percentageInt > 85) "fill-amber" else "fill-green"}" style="width: ${grp.percentageInt.coerceAtMost(100)}%;"></div></div>
                                <span class="progress-text">${grp.percentageInt}%</span>
                            </div>
                        </td>
                    </tr>
                """.trimIndent())

                for (item in grp.items) {
                    val catName = LanguageHelper.getLocalizedName(item.category.nameEn, item.category.nameBn, languageMode)
                    val itmDelta = item.deltaSpent
                    val itmDeltaSign = if (itmDelta > 0) "+" else ""
                    val itmDeltaClass = if (itmDelta <= 0) "text-success" else "text-danger"
                    val itmPctClass = if (itmDelta <= 0) "badge-positive" else "badge-negative"
                    val itmArrow = if (itmDelta > 0) "▲" else if (itmDelta < 0) "▼" else "—"

                    sb.append("""
                        <tr>
                            <td style="padding-left: 24px;">• <strong>$catName</strong></td>
                            <td style="text-align: right;">৳ ${LanguageHelper.formatNumber(item.budgetLimit, languageMode)}</td>
                            <td style="text-align: right; color: #64748b;">৳ ${LanguageHelper.formatNumber(item.spentBaseAmount, languageMode)}</td>
                            <td style="text-align: right; font-weight: 600;" class="${if (item.spentAmount > 0) "text-danger" else ""}">৳ ${LanguageHelper.formatNumber(item.spentAmount, languageMode)}</td>
                            <td style="text-align: right; font-weight: 600;" class="$itmDeltaClass">$itmDeltaSign৳ ${LanguageHelper.formatNumber(itmDelta, languageMode)}</td>
                            <td style="text-align: right;"><span class="badge $itmPctClass">$itmDeltaSign${String.format(Locale.US, "%.1f", item.deltaPercent)}% $itmArrow</span></td>
                            <td>
                                <div class="progress-container">
                                    <div class="progress-bar"><div class="progress-fill ${if (item.percentageInt > 100) "fill-red" else if (item.percentageInt > 85) "fill-amber" else "fill-green"}" style="width: ${item.percentageInt.coerceAtMost(100)}%;"></div></div>
                                    <span class="progress-text">${item.percentageInt}%</span>
                                </div>
                            </td>
                        </tr>
                    """.trimIndent())
                }
            }
        } else {
            sb.append("""
                <div class="summary-cards">
                    <div class="card card-primary">
                        <div class="card-label">${if (languageMode == LanguageMode.BANGLA) "মোট বাজেট" else "Total Budget"}</div>
                        <div class="card-value text-primary">৳ ${LanguageHelper.formatNumber(totalBudget, languageMode)}</div>
                        <div class="card-sub">${if (languageMode == LanguageMode.BANGLA) "বরাদ্দকৃত তহবিল" else "Total Allocated"}</div>
                    </div>
                    <div class="card card-danger">
                        <div class="card-label">${if (languageMode == LanguageMode.BANGLA) "মোট ব্যয়িত" else "Total Spent"}</div>
                        <div class="card-value text-danger">৳ ${LanguageHelper.formatNumber(totalSpent, languageMode)}</div>
                        <div class="card-sub">${if (languageMode == LanguageMode.BANGLA) "ব্যয় হার: " else "Usage: "}<strong>$percentOverall%</strong></div>
                    </div>
                    <div class="card ${if (remaining >= 0) "card-success" else "card-danger"}">
                        <div class="card-label">${if (languageMode == LanguageMode.BANGLA) "অবশিষ্ট বাজেট" else "Remaining"}</div>
                        <div class="card-value ${if (remaining >= 0) "text-success" else "text-danger"}">৳ ${LanguageHelper.formatNumber(remaining, languageMode)}</div>
                        <div class="card-sub">${if (remaining >= 0) (if (languageMode == LanguageMode.BANGLA) "বাজেটের মধ্যে আছে" else "Within Limit") else (if (languageMode == LanguageMode.BANGLA) "বাজেট অতিক্রান্ত" else "Over Limit")}</div>
                    </div>
                    <div class="card card-neutral">
                        <div class="card-label">${if (languageMode == LanguageMode.BANGLA) "ব্যয়িত অনুপাত" else "Spent Ratio"}</div>
                        <div class="card-value text-neutral">$percentOverall%</div>
                        <div style="margin-top:4px;">
                            <div class="progress-bar"><div class="progress-fill ${if (percentOverall > 100) "fill-red" else if (percentOverall > 85) "fill-amber" else "fill-green"}" style="width: ${percentOverall.coerceAtMost(100)}%;"></div></div>
                        </div>
                    </div>
                </div>
                <table>
                    <thead>
                        <tr>
                            <th style="width: 35%;">${if (languageMode == LanguageMode.BANGLA) "ক্যাটাগরি / গ্রুপ" else "Category / Group"}</th>
                            <th style="text-align: right;">${if (languageMode == LanguageMode.BANGLA) "বাজেট" else "Budget"}</th>
                            <th style="text-align: right;">${if (languageMode == LanguageMode.BANGLA) "ব্যয়িত" else "Spent"}</th>
                            <th style="text-align: right;">${if (languageMode == LanguageMode.BANGLA) "অবশিষ্ট" else "Remaining"}</th>
                            <th style="width: 25%;">${if (languageMode == LanguageMode.BANGLA) "ব্যয়িত অগ্রগতি" else "Progress & %"}</th>
                        </tr>
                    </thead>
                    <tbody>
            """.trimIndent())

            for (grp in groups) {
                val grpName = LanguageHelper.getLocalizedName(grp.groupNameEn, grp.groupNameBn, languageMode)
                val grpRem = grp.totalBudget - grp.totalSpent
                sb.append("""
                    <tr class="group-row">
                        <td><strong>📁 $grpName</strong></td>
                        <td style="text-align: right; font-weight: bold;">৳ ${LanguageHelper.formatNumber(grp.totalBudget, languageMode)}</td>
                        <td style="text-align: right; font-weight: bold; color: #dc2626;">৳ ${LanguageHelper.formatNumber(grp.totalSpent, languageMode)}</td>
                        <td style="text-align: right; font-weight: bold;" class="${if (grpRem >= 0) "text-success" else "text-danger"}">৳ ${LanguageHelper.formatNumber(grpRem, languageMode)}</td>
                        <td>
                            <div class="progress-container">
                                <div class="progress-bar"><div class="progress-fill ${if (grp.percentageInt > 100) "fill-red" else if (grp.percentageInt > 85) "fill-amber" else "fill-green"}" style="width: ${grp.percentageInt.coerceAtMost(100)}%;"></div></div>
                                <span class="progress-text">${grp.percentageInt}%</span>
                            </div>
                        </td>
                    </tr>
                """.trimIndent())

                for (item in grp.items) {
                    val catName = LanguageHelper.getLocalizedName(item.category.nameEn, item.category.nameBn, languageMode)
                    val itmRem = item.budgetLimit - item.spentAmount

                    sb.append("""
                        <tr>
                            <td style="padding-left: 24px;">• <strong>$catName</strong></td>
                            <td style="text-align: right;">৳ ${LanguageHelper.formatNumber(item.budgetLimit, languageMode)}</td>
                            <td style="text-align: right; font-weight: 600;" class="${if (item.spentAmount > 0) "text-danger" else ""}">৳ ${LanguageHelper.formatNumber(item.spentAmount, languageMode)}</td>
                            <td style="text-align: right;" class="${if (itmRem >= 0) "text-success" else "text-danger"}">৳ ${LanguageHelper.formatNumber(itmRem, languageMode)}</td>
                            <td>
                                <div class="progress-container">
                                    <div class="progress-bar"><div class="progress-fill ${if (item.percentageInt > 100) "fill-red" else if (item.percentageInt > 85) "fill-amber" else "fill-green"}" style="width: ${item.percentageInt.coerceAtMost(100)}%;"></div></div>
                                    <span class="progress-text">${item.percentageInt}%</span>
                                </div>
                            </td>
                        </tr>
                    """.trimIndent())
                }
            }
        }

        sb.append("</tbody></table>")
        return buildBaseHtml(title, "$periodLabel • ${DateUtils.formatDate(System.currentTimeMillis(), languageMode)}", sb.toString())
    }

    // ==========================================
    // 3. BALANCE SHEET TAB EXPORT
    // ==========================================
    fun exportBalanceSheet(
        context: Context,
        format: ExportFormat,
        asOfDateLabel: String,
        assetGroups: List<BalanceSheetGroup>,
        totalAssets: Double,
        liabilityGroups: List<BalanceSheetGroup>,
        totalLiabilities: Double,
        netWorth: Double,
        comparisonEnabled: Boolean = false,
        baseDateLabel: String = "",
        compareDateLabel: String = asOfDateLabel,
        totalAssetsBase: Double = 0.0,
        totalLiabilitiesBase: Double = 0.0,
        netWorthBase: Double = 0.0,
        netWorthDelta: Double = 0.0,
        languageMode: LanguageMode = LanguageMode.ENGLISH
    ) {
        val title = if (languageMode == LanguageMode.BANGLA) {
            if (comparisonEnabled) "ব্যালেন্স শীট তুলনামূলক রিপোর্ট ($compareDateLabel বনাম $baseDateLabel)"
            else "ব্যালেন্স শীট রিপোর্ট ($asOfDateLabel)"
        } else {
            if (comparisonEnabled) "Balance Sheet Comparison Report ($compareDateLabel vs $baseDateLabel)"
            else "Balance Sheet Report ($asOfDateLabel)"
        }
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())

        when (format) {
            ExportFormat.PDF -> {
                val html = buildBalanceSheetHtml(
                    title, asOfDateLabel, assetGroups, totalAssets, liabilityGroups, totalLiabilities, netWorth,
                    comparisonEnabled, baseDateLabel, compareDateLabel, totalAssetsBase, totalLiabilitiesBase,
                    netWorthBase, netWorthDelta, languageMode
                )
                PdfPrintHelper.printHtml(context, "BalanceSheet_$timeStamp", html, isLandscape = comparisonEnabled)
            }
            ExportFormat.CSV -> {
                val csv = buildBalanceSheetCsv(
                    asOfDateLabel, assetGroups, totalAssets, liabilityGroups, totalLiabilities, netWorth,
                    comparisonEnabled, baseDateLabel, compareDateLabel, totalAssetsBase, totalLiabilitiesBase,
                    netWorthBase, netWorthDelta, languageMode
                )
                shareFile(context, "BalanceSheet_$timeStamp.csv", format.mimeType, csv, title)
            }
            ExportFormat.HTML -> {
                val html = buildBalanceSheetHtml(
                    title, asOfDateLabel, assetGroups, totalAssets, liabilityGroups, totalLiabilities, netWorth,
                    comparisonEnabled, baseDateLabel, compareDateLabel, totalAssetsBase, totalLiabilitiesBase,
                    netWorthBase, netWorthDelta, languageMode
                )
                shareFile(context, "BalanceSheet_$timeStamp.html", format.mimeType, html, title)
            }
            ExportFormat.JSON -> {
                val json = JSONObject().apply {
                    put("reportType", "BalanceSheet")
                    put("asOfDate", asOfDateLabel)
                    put("comparisonEnabled", comparisonEnabled)
                    if (comparisonEnabled) {
                        put("basePeriod", baseDateLabel)
                        put("comparePeriod", compareDateLabel)
                        put("totalAssetsBase", totalAssetsBase)
                        put("totalAssetsCurrent", totalAssets)
                        put("assetsDelta", totalAssets - totalAssetsBase)
                        put("totalLiabilitiesBase", totalLiabilitiesBase)
                        put("totalLiabilitiesCurrent", totalLiabilities)
                        put("liabilitiesDelta", totalLiabilities - totalLiabilitiesBase)
                        put("netWorthBase", netWorthBase)
                        put("netWorthCurrent", netWorth)
                        put("netWorthDelta", netWorthDelta)
                    } else {
                        put("totalAssets", totalAssets)
                        put("totalLiabilities", totalLiabilities)
                        put("netWorth", netWorth)
                    }
                    put("assets", JSONArray().apply {
                        assetGroups.forEach { grp ->
                            put(JSONObject().apply {
                                put("groupName", grp.parentAccount.nameEn)
                                put("balance", grp.effectiveCurrentBalance)
                                if (comparisonEnabled) {
                                    val grpDelta = grp.effectiveCurrentBalance - grp.effectiveBaseBalance
                                    val grpPct = if (Math.abs(grp.effectiveBaseBalance) > 0.001) (grpDelta / Math.abs(grp.effectiveBaseBalance)) * 100.0 else 0.0
                                    put("baseBalance", grp.effectiveBaseBalance)
                                    put("delta", grpDelta)
                                    put("deltaPercent", grpPct)
                                }
                                put("subAccounts", JSONArray().apply {
                                    grp.subAccounts.forEach { sub ->
                                        put(JSONObject().apply {
                                            put("accountName", sub.account.nameEn)
                                            put("balance", sub.effectiveCurrentBalance)
                                            if (comparisonEnabled) {
                                                val subDelta = sub.effectiveCurrentBalance - sub.effectiveBaseBalance
                                                val subPct = if (Math.abs(sub.effectiveBaseBalance) > 0.001) (subDelta / Math.abs(sub.effectiveBaseBalance)) * 100.0 else 0.0
                                                put("baseBalance", sub.effectiveBaseBalance)
                                                put("delta", subDelta)
                                                put("deltaPercent", subPct)
                                            }
                                        })
                                    }
                                })
                            })
                        }
                    })
                    put("liabilities", JSONArray().apply {
                        liabilityGroups.forEach { grp ->
                            put(JSONObject().apply {
                                put("groupName", grp.parentAccount.nameEn)
                                put("balance", grp.effectiveCurrentBalance)
                                if (comparisonEnabled) {
                                    val grpDelta = grp.effectiveCurrentBalance - grp.effectiveBaseBalance
                                    val grpPct = if (Math.abs(grp.effectiveBaseBalance) > 0.001) (grpDelta / Math.abs(grp.effectiveBaseBalance)) * 100.0 else 0.0
                                    put("baseBalance", grp.effectiveBaseBalance)
                                    put("delta", grpDelta)
                                    put("deltaPercent", grpPct)
                                }
                                put("subAccounts", JSONArray().apply {
                                    grp.subAccounts.forEach { sub ->
                                        put(JSONObject().apply {
                                            put("accountName", sub.account.nameEn)
                                            put("balance", sub.effectiveCurrentBalance)
                                            if (comparisonEnabled) {
                                                val subDelta = sub.effectiveCurrentBalance - sub.effectiveBaseBalance
                                                val subPct = if (Math.abs(sub.effectiveBaseBalance) > 0.001) (subDelta / Math.abs(sub.effectiveBaseBalance)) * 100.0 else 0.0
                                                put("baseBalance", sub.effectiveBaseBalance)
                                                put("delta", subDelta)
                                                put("deltaPercent", subPct)
                                            }
                                        })
                                    }
                                })
                            })
                        }
                    })
                }.toString(2)
                shareFile(context, "BalanceSheet_$timeStamp.json", format.mimeType, json, title)
            }
        }
    }

    private fun buildBalanceSheetCsv(
        asOfDateLabel: String,
        assetGroups: List<BalanceSheetGroup>,
        totalAssets: Double,
        liabilityGroups: List<BalanceSheetGroup>,
        totalLiabilities: Double,
        netWorth: Double,
        comparisonEnabled: Boolean,
        baseDateLabel: String,
        compareDateLabel: String,
        totalAssetsBase: Double,
        totalLiabilitiesBase: Double,
        netWorthBase: Double,
        netWorthDelta: Double,
        languageMode: LanguageMode
    ): String {
        val sb = StringBuilder()
        if (asOfDateLabel.isNotBlank()) {
            sb.append("# Filter / Period: ${escapeCsv(asOfDateLabel)}\n")
        }

        if (comparisonEnabled) {
            val baseLbl = baseDateLabel.ifBlank { "Base Period" }
            val currLbl = compareDateLabel.ifBlank { "Current Period" }
            sb.append("Section,Account Group,Sub-Account,Base Balance - $baseLbl (BDT),Current Balance - $currLbl (BDT),Variance (BDT),Change (%)\n")

            for (grp in assetGroups) {
                val grpName = LanguageHelper.getLocalizedName(grp.parentAccount.nameEn, grp.parentAccount.nameBn, languageMode)
                for (sub in grp.subAccounts) {
                    val subName = LanguageHelper.getLocalizedName(sub.account.nameEn, sub.account.nameBn, languageMode)
                    val baseAmt = sub.effectiveBaseBalance
                    val currAmt = sub.effectiveCurrentBalance
                    val delta = currAmt - baseAmt
                    val pct = if (Math.abs(baseAmt) > 0.001) String.format(Locale.US, "%.1f%%", (delta / Math.abs(baseAmt)) * 100.0) else "-"
                    sb.append("Assets,${escapeCsv(grpName)},${escapeCsv(subName)},${String.format(Locale.US, "%.2f", baseAmt)},${String.format(Locale.US, "%.2f", currAmt)},${String.format(Locale.US, "%.2f", delta)},$pct\n")
                }
            }
            val assetsDelta = totalAssets - totalAssetsBase
            val assetsPct = if (Math.abs(totalAssetsBase) > 0.001) String.format(Locale.US, "%.1f%%", (assetsDelta / Math.abs(totalAssetsBase)) * 100.0) else "-"
            sb.append("Assets,TOTAL ASSETS,,${String.format(Locale.US, "%.2f", totalAssetsBase)},${String.format(Locale.US, "%.2f", totalAssets)},${String.format(Locale.US, "%.2f", assetsDelta)},$assetsPct\n")

            for (grp in liabilityGroups) {
                val grpName = LanguageHelper.getLocalizedName(grp.parentAccount.nameEn, grp.parentAccount.nameBn, languageMode)
                for (sub in grp.subAccounts) {
                    val subName = LanguageHelper.getLocalizedName(sub.account.nameEn, sub.account.nameBn, languageMode)
                    val baseAmt = sub.effectiveBaseBalance
                    val currAmt = sub.effectiveCurrentBalance
                    val delta = currAmt - baseAmt
                    val pct = if (Math.abs(baseAmt) > 0.001) String.format(Locale.US, "%.1f%%", (delta / Math.abs(baseAmt)) * 100.0) else "-"
                    sb.append("Liabilities,${escapeCsv(grpName)},${escapeCsv(subName)},${String.format(Locale.US, "%.2f", baseAmt)},${String.format(Locale.US, "%.2f", currAmt)},${String.format(Locale.US, "%.2f", delta)},$pct\n")
                }
            }
            val liabDelta = totalLiabilities - totalLiabilitiesBase
            val liabPct = if (Math.abs(totalLiabilitiesBase) > 0.001) String.format(Locale.US, "%.1f%%", (liabDelta / Math.abs(totalLiabilitiesBase)) * 100.0) else "-"
            sb.append("Liabilities,TOTAL LIABILITIES,,${String.format(Locale.US, "%.2f", totalLiabilitiesBase)},${String.format(Locale.US, "%.2f", totalLiabilities)},${String.format(Locale.US, "%.2f", liabDelta)},$liabPct\n")

            val nwPct = if (Math.abs(netWorthBase) > 0.001) String.format(Locale.US, "%.1f%%", (netWorthDelta / Math.abs(netWorthBase)) * 100.0) else "-"
            sb.append("Net Worth,NET WORTH,,${String.format(Locale.US, "%.2f", netWorthBase)},${String.format(Locale.US, "%.2f", netWorth)},${String.format(Locale.US, "%.2f", netWorthDelta)},$nwPct\n")
        } else {
            sb.append("Section,Account Group,Sub-Account,Balance (BDT)\n")

            for (grp in assetGroups) {
                val grpName = LanguageHelper.getLocalizedName(grp.parentAccount.nameEn, grp.parentAccount.nameBn, languageMode)
                for (sub in grp.subAccounts) {
                    val subName = LanguageHelper.getLocalizedName(sub.account.nameEn, sub.account.nameBn, languageMode)
                    sb.append("Assets,${escapeCsv(grpName)},${escapeCsv(subName)},${String.format(Locale.US, "%.2f", sub.effectiveCurrentBalance)}\n")
                }
            }
            sb.append("Assets,TOTAL ASSETS,,${String.format(Locale.US, "%.2f", totalAssets)}\n")

            for (grp in liabilityGroups) {
                val grpName = LanguageHelper.getLocalizedName(grp.parentAccount.nameEn, grp.parentAccount.nameBn, languageMode)
                for (sub in grp.subAccounts) {
                    val subName = LanguageHelper.getLocalizedName(sub.account.nameEn, sub.account.nameBn, languageMode)
                    sb.append("Liabilities,${escapeCsv(grpName)},${escapeCsv(subName)},${String.format(Locale.US, "%.2f", sub.effectiveCurrentBalance)}\n")
                }
            }
            sb.append("Liabilities,TOTAL LIABILITIES,,${String.format(Locale.US, "%.2f", totalLiabilities)}\n")
            sb.append("Net Worth,NET WORTH,,${String.format(Locale.US, "%.2f", netWorth)}\n")
        }

        return sb.toString()
    }

    private fun buildBalanceSheetHtml(
        title: String,
        asOfDateLabel: String,
        assetGroups: List<BalanceSheetGroup>,
        totalAssets: Double,
        liabilityGroups: List<BalanceSheetGroup>,
        totalLiabilities: Double,
        netWorth: Double,
        comparisonEnabled: Boolean,
        baseDateLabel: String,
        compareDateLabel: String,
        totalAssetsBase: Double,
        totalLiabilitiesBase: Double,
        netWorthBase: Double,
        netWorthDelta: Double,
        languageMode: LanguageMode
    ): String {
        val sb = StringBuilder()
        val assetsDelta = totalAssets - totalAssetsBase
        val liabDelta = totalLiabilities - totalLiabilitiesBase
        val assetsPct = if (Math.abs(totalAssetsBase) > 0.001) (assetsDelta / Math.abs(totalAssetsBase)) * 100.0 else 0.0
        val liabPct = if (Math.abs(totalLiabilitiesBase) > 0.001) (liabDelta / Math.abs(totalLiabilitiesBase)) * 100.0 else 0.0
        val nwPct = if (Math.abs(netWorthBase) > 0.001) (netWorthDelta / Math.abs(netWorthBase)) * 100.0 else 0.0

        if (comparisonEnabled) {
            val currLbl = compareDateLabel.ifBlank { "Current" }
            val baseLbl = baseDateLabel.ifBlank { "Base" }
            val assetsDeltaSign = if (assetsDelta > 0) "+" else ""
            val liabDeltaSign = if (liabDelta > 0) "+" else ""
            val nwDeltaSign = if (netWorthDelta > 0) "+" else ""

            val assetsBadgeClass = if (assetsDelta >= 0) "badge-positive" else "badge-negative"
            val assetsArrow = if (assetsDelta > 0) "▲" else if (assetsDelta < 0) "▼" else "—"
            val liabBadgeClass = if (liabDelta <= 0) "badge-positive" else "badge-negative"
            val liabArrow = if (liabDelta > 0) "▲" else if (liabDelta < 0) "▼" else "—"
            val nwBadgeClass = if (netWorthDelta >= 0) "badge-positive" else "badge-negative"
            val nwArrow = if (netWorthDelta > 0) "▲" else if (netWorthDelta < 0) "▼" else "—"

            sb.append("""
                <div class="summary-cards">
                    <div class="card card-success">
                        <div class="card-label">${if (languageMode == LanguageMode.BANGLA) "মোট সম্পদ ($currLbl)" else "Total Assets ($currLbl)"}</div>
                        <div class="card-value text-success">৳ ${LanguageHelper.formatNumber(totalAssets, languageMode)}</div>
                        <div class="card-sub">${if (languageMode == LanguageMode.BANGLA) "পূর্বে: " else "Base: "}৳ ${LanguageHelper.formatNumber(totalAssetsBase, languageMode)}</div>
                        <div style="margin-top: 4px;"><span class="badge $assetsBadgeClass">$assetsDeltaSign${String.format(Locale.US, "%.1f", assetsPct)}% $assetsArrow ($assetsDeltaSign৳ ${LanguageHelper.formatNumber(assetsDelta, languageMode)})</span></div>
                    </div>
                    <div class="card card-danger">
                        <div class="card-label">${if (languageMode == LanguageMode.BANGLA) "মোট দায় ($currLbl)" else "Total Liabilities ($currLbl)"}</div>
                        <div class="card-value text-danger">৳ ${LanguageHelper.formatNumber(totalLiabilities, languageMode)}</div>
                        <div class="card-sub">${if (languageMode == LanguageMode.BANGLA) "পূর্বে: " else "Base: "}৳ ${LanguageHelper.formatNumber(totalLiabilitiesBase, languageMode)}</div>
                        <div style="margin-top: 4px;"><span class="badge $liabBadgeClass">$liabDeltaSign${String.format(Locale.US, "%.1f", liabPct)}% $liabArrow ($liabDeltaSign৳ ${LanguageHelper.formatNumber(liabDelta, languageMode)})</span></div>
                    </div>
                    <div class="card ${if (netWorth >= 0) "card-primary" else "card-danger"}">
                        <div class="card-label">${if (languageMode == LanguageMode.BANGLA) "নেট সম্পদ ($currLbl)" else "Net Worth ($currLbl)"}</div>
                        <div class="card-value ${if (netWorth >= 0) "text-primary" else "text-danger"}">৳ ${LanguageHelper.formatNumber(netWorth, languageMode)}</div>
                        <div class="card-sub">${if (languageMode == LanguageMode.BANGLA) "পূর্বে: " else "Base: "}৳ ${LanguageHelper.formatNumber(netWorthBase, languageMode)}</div>
                        <div style="margin-top: 4px;"><span class="badge $nwBadgeClass">$nwDeltaSign${String.format(Locale.US, "%.1f", nwPct)}% $nwArrow ($nwDeltaSign৳ ${LanguageHelper.formatNumber(netWorthDelta, languageMode)})</span></div>
                    </div>
                </div>

                <table>
                    <thead>
                        <tr>
                            <th style="width: 32%;">${if (languageMode == LanguageMode.BANGLA) "অ্যাকাউন্ট গ্রুপ ও সাব-অ্যাকাউন্ট" else "Account Group & Sub-Account"}</th>
                            <th style="text-align: right;">${if (languageMode == LanguageMode.BANGLA) "পূর্ববর্তী ব্যালেন্স ($baseLbl)" else "Base Balance ($baseLbl)"}</th>
                            <th style="text-align: right;">${if (languageMode == LanguageMode.BANGLA) "বর্তমান ব্যালেন্স ($currLbl)" else "Current Balance ($currLbl)"}</th>
                            <th style="text-align: right;">${if (languageMode == LanguageMode.BANGLA) "পার্থক্য" else "Variance"}</th>
                            <th style="text-align: right; width: 14%;">${if (languageMode == LanguageMode.BANGLA) "পরিবর্তন %" else "Change %"}</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr class="section-assets">
                            <td colspan="5"><strong>💎 ${if (languageMode == LanguageMode.BANGLA) "সম্পদ (Assets)" else "Assets"} — ৳ ${LanguageHelper.formatNumber(totalAssets, languageMode)}</strong></td>
                        </tr>
            """.trimIndent())

            for (grp in assetGroups) {
                val grpName = LanguageHelper.getLocalizedName(grp.parentAccount.nameEn, grp.parentAccount.nameBn, languageMode)
                val grpDelta = grp.effectiveCurrentBalance - grp.effectiveBaseBalance
                val grpDeltaSign = if (grpDelta > 0) "+" else ""
                val grpDeltaClass = if (grpDelta >= 0) "text-success" else "text-danger"
                val grpBadgeClass = if (grpDelta >= 0) "badge-positive" else "badge-negative"
                val grpArrow = if (grpDelta > 0) "▲" else if (grpDelta < 0) "▼" else "—"
                val grpPctStr = if (Math.abs(grp.effectiveBaseBalance) > 0.001) String.format(Locale.US, "%.1f%%", (grpDelta / Math.abs(grp.effectiveBaseBalance)) * 100.0) else "-"

                sb.append("""
                    <tr class="group-row">
                        <td><strong>📁 $grpName</strong></td>
                        <td style="text-align: right; font-weight: bold;">৳ ${LanguageHelper.formatNumber(grp.effectiveBaseBalance, languageMode)}</td>
                        <td style="text-align: right; font-weight: bold;">৳ ${LanguageHelper.formatNumber(grp.effectiveCurrentBalance, languageMode)}</td>
                        <td style="text-align: right; font-weight: bold;" class="$grpDeltaClass">$grpDeltaSign৳ ${LanguageHelper.formatNumber(grpDelta, languageMode)}</td>
                        <td style="text-align: right;"><span class="badge $grpBadgeClass">$grpDeltaSign$grpPctStr $grpArrow</span></td>
                    </tr>
                """.trimIndent())
                for (sub in grp.subAccounts) {
                    val subName = LanguageHelper.getLocalizedName(sub.account.nameEn, sub.account.nameBn, languageMode)
                    val subDelta = sub.effectiveCurrentBalance - sub.effectiveBaseBalance
                    val subDeltaSign = if (subDelta > 0) "+" else ""
                    val subDeltaClass = if (subDelta >= 0) "text-success" else "text-danger"
                    val subBadgeClass = if (subDelta >= 0) "badge-positive" else "badge-negative"
                    val subArrow = if (subDelta > 0) "▲" else if (subDelta < 0) "▼" else "—"
                    val subPctStr = if (Math.abs(sub.effectiveBaseBalance) > 0.001) String.format(Locale.US, "%.1f%%", (subDelta / Math.abs(sub.effectiveBaseBalance)) * 100.0) else "-"

                    sb.append("""
                        <tr>
                            <td style="padding-left: 24px;">↳ $subName</td>
                            <td style="text-align: right; color: #64748b;">৳ ${LanguageHelper.formatNumber(sub.effectiveBaseBalance, languageMode)}</td>
                            <td style="text-align: right; font-weight: 600;">৳ ${LanguageHelper.formatNumber(sub.effectiveCurrentBalance, languageMode)}</td>
                            <td style="text-align: right;" class="$subDeltaClass">$subDeltaSign৳ ${LanguageHelper.formatNumber(subDelta, languageMode)}</td>
                            <td style="text-align: right;"><span class="badge $subBadgeClass">$subDeltaSign$subPctStr $subArrow</span></td>
                        </tr>
                    """.trimIndent())
                }
            }

            sb.append("""
                        <tr class="section-liabilities" style="margin-top: 10px;">
                            <td colspan="5"><strong>💳 ${if (languageMode == LanguageMode.BANGLA) "দায় (Liabilities)" else "Liabilities"} — ৳ ${LanguageHelper.formatNumber(totalLiabilities, languageMode)}</strong></td>
                        </tr>
            """.trimIndent())

            for (grp in liabilityGroups) {
                val grpName = LanguageHelper.getLocalizedName(grp.parentAccount.nameEn, grp.parentAccount.nameBn, languageMode)
                val grpDelta = grp.effectiveCurrentBalance - grp.effectiveBaseBalance
                val grpDeltaSign = if (grpDelta > 0) "+" else ""
                val grpDeltaClass = if (grpDelta <= 0) "text-success" else "text-danger"
                val grpBadgeClass = if (grpDelta <= 0) "badge-positive" else "badge-negative"
                val grpArrow = if (grpDelta > 0) "▲" else if (grpDelta < 0) "▼" else "—"
                val grpPctStr = if (Math.abs(grp.effectiveBaseBalance) > 0.001) String.format(Locale.US, "%.1f%%", (grpDelta / Math.abs(grp.effectiveBaseBalance)) * 100.0) else "-"

                sb.append("""
                    <tr class="group-row">
                        <td><strong>📁 $grpName</strong></td>
                        <td style="text-align: right; font-weight: bold;">৳ ${LanguageHelper.formatNumber(grp.effectiveBaseBalance, languageMode)}</td>
                        <td style="text-align: right; font-weight: bold;">৳ ${LanguageHelper.formatNumber(grp.effectiveCurrentBalance, languageMode)}</td>
                        <td style="text-align: right; font-weight: bold;" class="$grpDeltaClass">$grpDeltaSign৳ ${LanguageHelper.formatNumber(grpDelta, languageMode)}</td>
                        <td style="text-align: right;"><span class="badge $grpBadgeClass">$grpDeltaSign$grpPctStr $grpArrow</span></td>
                    </tr>
                """.trimIndent())
                for (sub in grp.subAccounts) {
                    val subName = LanguageHelper.getLocalizedName(sub.account.nameEn, sub.account.nameBn, languageMode)
                    val subDelta = sub.effectiveCurrentBalance - sub.effectiveBaseBalance
                    val subDeltaSign = if (subDelta > 0) "+" else ""
                    val subDeltaClass = if (subDelta <= 0) "text-success" else "text-danger"
                    val subBadgeClass = if (subDelta <= 0) "badge-positive" else "badge-negative"
                    val subArrow = if (subDelta > 0) "▲" else if (subDelta < 0) "▼" else "—"
                    val subPctStr = if (Math.abs(sub.effectiveBaseBalance) > 0.001) String.format(Locale.US, "%.1f%%", (subDelta / Math.abs(sub.effectiveBaseBalance)) * 100.0) else "-"

                    sb.append("""
                        <tr>
                            <td style="padding-left: 24px;">↳ $subName</td>
                            <td style="text-align: right; color: #64748b;">৳ ${LanguageHelper.formatNumber(sub.effectiveBaseBalance, languageMode)}</td>
                            <td style="text-align: right; font-weight: 600;">৳ ${LanguageHelper.formatNumber(sub.effectiveCurrentBalance, languageMode)}</td>
                            <td style="text-align: right;" class="$subDeltaClass">$subDeltaSign৳ ${LanguageHelper.formatNumber(subDelta, languageMode)}</td>
                            <td style="text-align: right;"><span class="badge $subBadgeClass">$subDeltaSign$subPctStr $subArrow</span></td>
                        </tr>
                    """.trimIndent())
                }
            }

            val nwDeltaSignStr = if (netWorthDelta > 0) "+" else ""
            val nwDeltaClassStr = if (netWorthDelta >= 0) "text-success" else "text-danger"
            val nwBadgeClassStr = if (netWorthDelta >= 0) "badge-positive" else "badge-negative"
            val nwArrowStr = if (netWorthDelta > 0) "▲" else if (netWorthDelta < 0) "▼" else "—"

            sb.append("""
                        <tr class="net-worth-row">
                            <td><strong>👑 ${if (languageMode == LanguageMode.BANGLA) "নেট সম্পদ (Net Worth)" else "Net Worth"}</strong></td>
                            <td style="text-align: right; font-weight: bold;">৳ ${LanguageHelper.formatNumber(netWorthBase, languageMode)}</td>
                            <td style="text-align: right; font-weight: bold;">৳ ${LanguageHelper.formatNumber(netWorth, languageMode)}</td>
                            <td style="text-align: right; font-weight: bold;" class="$nwDeltaClassStr">$nwDeltaSignStr৳ ${LanguageHelper.formatNumber(netWorthDelta, languageMode)}</td>
                            <td style="text-align: right;"><span class="badge $nwBadgeClassStr">$nwDeltaSignStr${String.format(Locale.US, "%.1f", nwPct)}% $nwArrowStr</span></td>
                        </tr>
                    </tbody>
                </table>
            """.trimIndent())
        } else {
            sb.append("""
                <div class="summary-cards">
                    <div class="card card-success">
                        <div class="card-label">${if (languageMode == LanguageMode.BANGLA) "মোট সম্পদ" else "Total Assets"}</div>
                        <div class="card-value text-success">৳ ${LanguageHelper.formatNumber(totalAssets, languageMode)}</div>
                        <div class="card-sub">${if (languageMode == LanguageMode.BANGLA) "সর্বমোট সম্পত্তি" else "Total Holdings"}</div>
                    </div>
                    <div class="card card-danger">
                        <div class="card-label">${if (languageMode == LanguageMode.BANGLA) "মোট দায়" else "Total Liabilities"}</div>
                        <div class="card-value text-danger">৳ ${LanguageHelper.formatNumber(totalLiabilities, languageMode)}</div>
                        <div class="card-sub">${if (languageMode == LanguageMode.BANGLA) "সর্বমোট দেনা" else "Total Obligations"}</div>
                    </div>
                    <div class="card ${if (netWorth >= 0) "card-primary" else "card-danger"}">
                        <div class="card-label">${if (languageMode == LanguageMode.BANGLA) "নেট সম্পদ" else "Net Worth"}</div>
                        <div class="card-value ${if (netWorth >= 0) "text-primary" else "text-danger"}">৳ ${LanguageHelper.formatNumber(netWorth, languageMode)}</div>
                        <div class="card-sub">${if (netWorth >= 0) (if (languageMode == LanguageMode.BANGLA) "উদ্বৃত্ত স্থিতি" else "Positive Net Worth") else (if (languageMode == LanguageMode.BANGLA) "ঘাটতি স্থিতি" else "Deficit")}</div>
                    </div>
                </div>
                <table>
                    <thead>
                        <tr>
                            <th style="width: 65%;">${if (languageMode == LanguageMode.BANGLA) "অ্যাকাউন্ট গ্রুপ ও সাব-অ্যাকাউন্ট" else "Account Group & Sub-Account"}</th>
                            <th style="text-align: right;">${if (languageMode == LanguageMode.BANGLA) "ব্যালেন্স (BDT)" else "Balance (BDT)"}</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr class="section-assets">
                            <td colspan="2"><strong>💎 ${if (languageMode == LanguageMode.BANGLA) "সম্পদ (Assets)" else "Assets"} — ৳ ${LanguageHelper.formatNumber(totalAssets, languageMode)}</strong></td>
                        </tr>
            """.trimIndent())

            for (grp in assetGroups) {
                val grpName = LanguageHelper.getLocalizedName(grp.parentAccount.nameEn, grp.parentAccount.nameBn, languageMode)
                sb.append("""
                    <tr class="group-row">
                        <td><strong>📁 $grpName</strong></td>
                        <td style="text-align: right; font-weight: bold;">৳ ${LanguageHelper.formatNumber(grp.effectiveCurrentBalance, languageMode)}</td>
                    </tr>
                """.trimIndent())
                for (sub in grp.subAccounts) {
                    val subName = LanguageHelper.getLocalizedName(sub.account.nameEn, sub.account.nameBn, languageMode)
                    sb.append("""
                        <tr>
                            <td style="padding-left: 24px;">↳ $subName</td>
                            <td style="text-align: right; font-weight: 600;">৳ ${LanguageHelper.formatNumber(sub.effectiveCurrentBalance, languageMode)}</td>
                        </tr>
                    """.trimIndent())
                }
            }

            sb.append("""
                        <tr class="section-liabilities" style="margin-top: 10px;">
                            <td colspan="2"><strong>💳 ${if (languageMode == LanguageMode.BANGLA) "দায় (Liabilities)" else "Liabilities"} — ৳ ${LanguageHelper.formatNumber(totalLiabilities, languageMode)}</strong></td>
                        </tr>
            """.trimIndent())

            for (grp in liabilityGroups) {
                val grpName = LanguageHelper.getLocalizedName(grp.parentAccount.nameEn, grp.parentAccount.nameBn, languageMode)
                sb.append("""
                    <tr class="group-row">
                        <td><strong>📁 $grpName</strong></td>
                        <td style="text-align: right; font-weight: bold;">৳ ${LanguageHelper.formatNumber(grp.effectiveCurrentBalance, languageMode)}</td>
                    </tr>
                """.trimIndent())
                for (sub in grp.subAccounts) {
                    val subName = LanguageHelper.getLocalizedName(sub.account.nameEn, sub.account.nameBn, languageMode)
                    sb.append("""
                        <tr>
                            <td style="padding-left: 24px;">↳ $subName</td>
                            <td style="text-align: right; font-weight: 600;">৳ ${LanguageHelper.formatNumber(sub.effectiveCurrentBalance, languageMode)}</td>
                        </tr>
                    """.trimIndent())
                }
            }

            sb.append("""
                        <tr class="net-worth-row">
                            <td><strong>👑 ${if (languageMode == LanguageMode.BANGLA) "নেট সম্পদ (Net Worth)" else "Net Worth"}</strong></td>
                            <td style="text-align: right; font-weight: bold; ${if (netWorth >= 0) "color: #16a34a;" else "color: #dc2626;"}">৳ ${LanguageHelper.formatNumber(netWorth, languageMode)}</td>
                        </tr>
                    </tbody>
                </table>
            """.trimIndent())
        }

        return buildBaseHtml(title, "$asOfDateLabel • ${DateUtils.formatDate(System.currentTimeMillis(), languageMode)}", sb.toString())
    }

    // ==========================================
    // 4. LABELS TAB EXPORT
    // ==========================================
    fun exportLabels(
        context: Context,
        format: ExportFormat,
        filterSubtitle: String,
        labels: List<AggregatedLabel>,
        languageMode: LanguageMode = LanguageMode.ENGLISH
    ) {
        val title = if (languageMode == LanguageMode.BANGLA) "লেবেল সারসংক্ষেপ রিপোর্ট" else "Labels Summary Report"
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val dateDisplay = DateUtils.formatDate(System.currentTimeMillis(), languageMode)
        val totalExp = labels.sumOf { it.totalExpense }
        val totalInc = labels.sumOf { it.totalIncome }

        when (format) {
            ExportFormat.PDF -> {
                val html = buildLabelsHtml(title, dateDisplay, filterSubtitle, labels, totalExp, totalInc, languageMode)
                PdfPrintHelper.printHtml(context, "Labels_$timeStamp", html, isLandscape = false)
            }
            ExportFormat.CSV -> {
                val csv = buildLabelsCsv(labels, filterSubtitle, languageMode)
                shareFile(context, "Labels_$timeStamp.csv", format.mimeType, csv, title)
            }
            ExportFormat.HTML -> {
                val html = buildLabelsHtml(title, dateDisplay, filterSubtitle, labels, totalExp, totalInc, languageMode)
                shareFile(context, "Labels_$timeStamp.html", format.mimeType, html, title)
            }
            ExportFormat.JSON -> {
                val json = JSONObject().apply {
                    put("reportType", "LabelsSummary")
                    put("generatedAt", formatTimestamp(System.currentTimeMillis()))
                    if (filterSubtitle.isNotBlank()) put("filter", filterSubtitle)
                    put("totalLabels", labels.size)
                    put("totalExpense", totalExp)
                    put("totalIncome", totalInc)
                    put("labels", JSONArray().apply {
                        labels.forEach { lbl ->
                            put(JSONObject().apply {
                                put("label", lbl.labelName)
                                put("expense", lbl.totalExpense)
                                put("income", lbl.totalIncome)
                                put("sum", lbl.totalSum)
                                put("count", lbl.transactionCount)
                                put("sharePercent", lbl.percentageShare)
                            })
                        }
                    })
                }.toString(2)
                shareFile(context, "Labels_$timeStamp.json", format.mimeType, json, title)
            }
        }
    }

    private fun buildLabelsCsv(
        labels: List<AggregatedLabel>,
        filterSubtitle: String,
        languageMode: LanguageMode
    ): String {
        val sb = StringBuilder()
        if (filterSubtitle.isNotBlank()) {
            sb.append("# Filter: ${escapeCsv(filterSubtitle)}\n")
        }
        sb.append("Label Name,Total Expense (BDT),Total Income (BDT),Total Net (BDT),Transaction Count,% Share\n")
        for (lbl in labels) {
            val exp = String.format(Locale.US, "%.2f", lbl.totalExpense)
            val inc = String.format(Locale.US, "%.2f", lbl.totalIncome)
            val net = String.format(Locale.US, "%.2f", lbl.totalSum)
            val share = String.format(Locale.US, "%.1f", lbl.percentageShare)
            sb.append("${escapeCsv(lbl.labelName)},$exp,$inc,$net,${lbl.transactionCount},$share%\n")
        }
        return sb.toString()
    }

    private fun buildLabelsHtml(
        title: String,
        dateDisplay: String,
        filterSubtitle: String,
        labels: List<AggregatedLabel>,
        totalExpense: Double,
        totalIncome: Double,
        languageMode: LanguageMode
    ): String {
        val sb = StringBuilder()
        sb.append("""
            <div class="summary-cards">
                <div class="card">
                    <div class="card-label">${if (languageMode == LanguageMode.BANGLA) "মোট লেবেল" else "Total Labels"}</div>
                    <div class="card-value text-primary">${labels.size}</div>
                </div>
                <div class="card">
                    <div class="card-label">${if (languageMode == LanguageMode.BANGLA) "মোট ব্যয়" else "Total Expense"}</div>
                    <div class="card-value text-danger">৳ ${LanguageHelper.formatNumber(totalExpense, languageMode)}</div>
                </div>
                <div class="card">
                    <div class="card-label">${if (languageMode == LanguageMode.BANGLA) "মোট আয়" else "Total Income"}</div>
                    <div class="card-value text-success">৳ ${LanguageHelper.formatNumber(totalIncome, languageMode)}</div>
                </div>
            </div>
            <table>
                <thead>
                    <tr>
                        <th>${if (languageMode == LanguageMode.BANGLA) "লেবেল নাম" else "Label Name"}</th>
                        <th style="text-align: right;">${if (languageMode == LanguageMode.BANGLA) "মোট ব্যয়" else "Total Expense"}</th>
                        <th style="text-align: right;">${if (languageMode == LanguageMode.BANGLA) "মোট আয়" else "Total Income"}</th>
                        <th style="text-align: right;">${if (languageMode == LanguageMode.BANGLA) "লেনদেন সংখ্যা" else "Tx Count"}</th>
                        <th style="text-align: right;">${if (languageMode == LanguageMode.BANGLA) "শেয়ার %" else "Share %"}</th>
                    </tr>
                </thead>
                <tbody>
        """.trimIndent())

        for (lbl in labels) {
            sb.append("""
                <tr>
                    <td><strong>🏷️ ${escapeHtml(lbl.labelName)}</strong></td>
                    <td style="text-align: right;" class="${if (lbl.totalExpense > 0) "text-danger" else ""}">৳ ${LanguageHelper.formatNumber(lbl.totalExpense, languageMode)}</td>
                    <td style="text-align: right;" class="${if (lbl.totalIncome > 0) "text-success" else ""}">৳ ${LanguageHelper.formatNumber(lbl.totalIncome, languageMode)}</td>
                    <td style="text-align: right;">${lbl.transactionCount}</td>
                    <td style="text-align: right; font-weight: 600;">${String.format(Locale.US, "%.1f", lbl.percentageShare)}%</td>
                </tr>
            """.trimIndent())
        }

        sb.append("</tbody></table>")
        return buildBaseHtml(title, "$dateDisplay ${if (filterSubtitle.isNotEmpty()) "• $filterSubtitle" else ""}", sb.toString())
    }

    // ==========================================
    // 5. ITEMS SUMMARY TAB EXPORT
    // ==========================================
    fun exportItems(
        context: Context,
        format: ExportFormat,
        filterSubtitle: String,
        items: List<AggregatedItem>,
        languageMode: LanguageMode = LanguageMode.ENGLISH
    ) {
        val title = if (languageMode == LanguageMode.BANGLA) "আইটেম সারসংক্ষেপ রিপোর্ট" else "Items Summary Report"
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val dateDisplay = DateUtils.formatDate(System.currentTimeMillis(), languageMode)
        val totalExp = items.sumOf { it.totalExpense }
        val totalInc = items.sumOf { it.totalIncome }

        when (format) {
            ExportFormat.PDF -> {
                val html = buildItemsHtml(title, dateDisplay, filterSubtitle, items, totalExp, totalInc, languageMode)
                PdfPrintHelper.printHtml(context, "Items_$timeStamp", html, isLandscape = false)
            }
            ExportFormat.CSV -> {
                val csv = buildItemsCsv(items, filterSubtitle, languageMode)
                shareFile(context, "Items_$timeStamp.csv", format.mimeType, csv, title)
            }
            ExportFormat.HTML -> {
                val html = buildItemsHtml(title, dateDisplay, filterSubtitle, items, totalExp, totalInc, languageMode)
                shareFile(context, "Items_$timeStamp.html", format.mimeType, html, title)
            }
            ExportFormat.JSON -> {
                val json = JSONObject().apply {
                    put("reportType", "ItemsSummary")
                    put("generatedAt", formatTimestamp(System.currentTimeMillis()))
                    if (filterSubtitle.isNotBlank()) put("filter", filterSubtitle)
                    put("totalItems", items.size)
                    put("totalExpense", totalExp)
                    put("totalIncome", totalInc)
                    put("items", JSONArray().apply {
                        items.forEach { itm ->
                            put(JSONObject().apply {
                                put("name", itm.name)
                                put("expense", itm.totalExpense)
                                put("income", itm.totalIncome)
                                put("count", itm.transactionCount)
                                put("latestDate", DateUtils.formatDate(itm.latestDateEpochMs, languageMode))
                                put("sharePercent", itm.percentageShare)
                            })
                        }
                    })
                }.toString(2)
                shareFile(context, "Items_$timeStamp.json", format.mimeType, json, title)
            }
        }
    }

    private fun buildItemsCsv(
        items: List<AggregatedItem>,
        filterSubtitle: String,
        languageMode: LanguageMode
    ): String {
        val sb = StringBuilder()
        if (filterSubtitle.isNotBlank()) {
            sb.append("# Filter: ${escapeCsv(filterSubtitle)}\n")
        }
        sb.append("Item Name,Total Expense (BDT),Total Income (BDT),Transaction Count,Latest Date,% Share\n")
        for (itm in items) {
            val exp = String.format(Locale.US, "%.2f", itm.totalExpense)
            val inc = String.format(Locale.US, "%.2f", itm.totalIncome)
            val latest = DateUtils.formatDate(itm.latestDateEpochMs, languageMode)
            val share = String.format(Locale.US, "%.1f", itm.percentageShare)
            sb.append("${escapeCsv(itm.name)},$exp,$inc,${itm.transactionCount},${escapeCsv(latest)},$share%\n")
        }
        return sb.toString()
    }

    private fun buildItemsHtml(
        title: String,
        dateDisplay: String,
        filterSubtitle: String,
        items: List<AggregatedItem>,
        totalExpense: Double,
        totalIncome: Double,
        languageMode: LanguageMode
    ): String {
        val sb = StringBuilder()
        sb.append("""
            <div class="summary-cards">
                <div class="card">
                    <div class="card-label">${if (languageMode == LanguageMode.BANGLA) "মোট আইটেম" else "Total Items"}</div>
                    <div class="card-value text-primary">${items.size}</div>
                </div>
                <div class="card">
                    <div class="card-label">${if (languageMode == LanguageMode.BANGLA) "মোট ব্যয়" else "Total Expense"}</div>
                    <div class="card-value text-danger">৳ ${LanguageHelper.formatNumber(totalExpense, languageMode)}</div>
                </div>
                <div class="card">
                    <div class="card-label">${if (languageMode == LanguageMode.BANGLA) "মোট আয়" else "Total Income"}</div>
                    <div class="card-value text-success">৳ ${LanguageHelper.formatNumber(totalIncome, languageMode)}</div>
                </div>
            </div>
            <table>
                <thead>
                    <tr>
                        <th>${if (languageMode == LanguageMode.BANGLA) "আইটেম নাম" else "Item Name"}</th>
                        <th style="text-align: right;">${if (languageMode == LanguageMode.BANGLA) "মোট ব্যয়" else "Total Expense"}</th>
                        <th style="text-align: right;">${if (languageMode == LanguageMode.BANGLA) "মোট আয়" else "Total Income"}</th>
                        <th style="text-align: right;">${if (languageMode == LanguageMode.BANGLA) "লেনদেন সংখ্যা" else "Tx Count"}</th>
                        <th>${if (languageMode == LanguageMode.BANGLA) "সর্বশেষ লেনদেন" else "Latest Date"}</th>
                        <th style="text-align: right;">${if (languageMode == LanguageMode.BANGLA) "শেয়ার %" else "Share %"}</th>
                    </tr>
                </thead>
                <tbody>
        """.trimIndent())

        for (itm in items) {
            val dateStr = DateUtils.formatDate(itm.latestDateEpochMs, languageMode)
            sb.append("""
                <tr>
                    <td><strong>📦 ${escapeHtml(itm.name)}</strong></td>
                    <td style="text-align: right;" class="${if (itm.totalExpense > 0) "text-danger" else ""}">৳ ${LanguageHelper.formatNumber(itm.totalExpense, languageMode)}</td>
                    <td style="text-align: right;" class="${if (itm.totalIncome > 0) "text-success" else ""}">৳ ${LanguageHelper.formatNumber(itm.totalIncome, languageMode)}</td>
                    <td style="text-align: right;">${itm.transactionCount}</td>
                    <td>$dateStr</td>
                    <td style="text-align: right; font-weight: 600;">${String.format(Locale.US, "%.1f", itm.percentageShare)}%</td>
                </tr>
            """.trimIndent())
        }

        sb.append("</tbody></table>")
        return buildBaseHtml(title, "$dateDisplay ${if (filterSubtitle.isNotEmpty()) "• $filterSubtitle" else ""}", sb.toString())
    }

    // ==========================================
    // SHARED FILE EXPORT & STYLING ENGINE
    // ==========================================
    fun shareFile(
        context: Context,
        fileName: String,
        mimeType: String,
        content: String,
        chooserTitle: String
    ) {
        try {
            // 1. Save directly into the selected local sync folder under "Exports" subfolder
            val savedLocation = BackupManager.saveExportToLocalFolder(
                context = context,
                fileName = fileName,
                mimeType = mimeType,
                content = content
            )

            // 2. Prepare cached copy for Android Share Sheet
            val file = File(context.cacheDir, fileName)
            val fos = FileOutputStream(file)
            fos.write(content.toByteArray(Charsets.UTF_8))
            fos.flush()
            fos.close()

            val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, chooserTitle)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, chooserTitle))

            val feedback = if (savedLocation != null) {
                "Saved to $savedLocation"
            } else {
                "Export ready: $fileName"
            }
            Toast.makeText(context, feedback, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Export error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    fun buildBaseHtml(title: String, subtitle: String, contentHtml: String): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    * { box-sizing: border-box; }
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
                        margin: 28px;
                        color: #0f172a;
                        background: #ffffff;
                        line-height: 1.5;
                        font-size: 12px;
                    }
                    .header {
                        margin-bottom: 24px;
                        border-bottom: 2px solid #e2e8f0;
                        padding-bottom: 16px;
                    }
                    .app-badge {
                        display: inline-block;
                        background: linear-gradient(135deg, #1e3a8a, #2563eb);
                        color: #ffffff;
                        font-weight: 800;
                        font-size: 11px;
                        padding: 4px 10px;
                        border-radius: 6px;
                        margin-bottom: 8px;
                        letter-spacing: 1px;
                    }
                    h1 {
                        font-size: 22px;
                        margin: 4px 0 6px 0;
                        color: #0f172a;
                        font-weight: 800;
                        letter-spacing: -0.3px;
                    }
                    .subtitle {
                        font-size: 12px;
                        color: #64748b;
                        font-weight: 500;
                    }
                    .summary-cards {
                        display: flex;
                        gap: 14px;
                        margin-bottom: 24px;
                        flex-wrap: wrap;
                    }
                    .card {
                        flex: 1;
                        min-width: 140px;
                        background: #f8fafc;
                        border: 1px solid #e2e8f0;
                        border-radius: 10px;
                        padding: 12px 16px;
                        box-shadow: 0 1px 3px rgba(0,0,0,0.04);
                    }
                    .card-primary {
                        background: #eff6ff;
                        border-color: #bfdbfe;
                    }
                    .card-success {
                        background: #f0fdf4;
                        border-color: #bbf7d0;
                    }
                    .card-danger {
                        background: #fef2f2;
                        border-color: #fecaca;
                    }
                    .card-warning {
                        background: #fffbeb;
                        border-color: #fde68a;
                    }
                    .card-label {
                        font-size: 10px;
                        color: #475569;
                        font-weight: 700;
                        text-transform: uppercase;
                        letter-spacing: 0.6px;
                        margin-bottom: 4px;
                    }
                    .card-value {
                        font-size: 17px;
                        font-weight: 800;
                        color: #0f172a;
                        letter-spacing: -0.2px;
                    }
                    .card-sub {
                        font-size: 10.5px;
                        color: #64748b;
                        margin-top: 3px;
                    }
                    table {
                        width: 100%;
                        border-collapse: collapse;
                        font-size: 11.5px;
                        margin-top: 10px;
                        background: #ffffff;
                    }
                    th, td {
                        padding: 8px 12px;
                        border-bottom: 1px solid #e2e8f0;
                        text-align: left;
                    }
                    th {
                        background-color: #f1f5f9;
                        font-weight: 700;
                        color: #1e293b;
                        border-top: 1px solid #cbd5e1;
                        border-bottom: 2px solid #cbd5e1;
                        font-size: 10.5px;
                        text-transform: uppercase;
                        letter-spacing: 0.5px;
                    }
                    tr:nth-child(even):not(.group-row):not(.section-assets):not(.section-liabilities):not(.net-worth-row):not(.budget-over-row) {
                        background-color: #fafbfc;
                    }
                    .group-row {
                        background-color: #f8fafc;
                        font-weight: 700;
                        color: #1e3a8a;
                        border-top: 1.5px solid #cbd5e1;
                    }
                    .section-assets {
                        background-color: #dcfce7;
                        color: #15803d;
                        font-weight: 700;
                        font-size: 12px;
                    }
                    .section-liabilities {
                        background-color: #fee2e2;
                        color: #b91c1c;
                        font-weight: 700;
                        font-size: 12px;
                    }
                    .net-worth-row {
                        background-color: #f1f5f9;
                        font-weight: 800;
                        font-size: 13px;
                        border-top: 2px solid #64748b;
                        border-bottom: 2px solid #64748b;
                    }
                    .budget-over-row {
                        background-color: #fef2f2;
                    }
                    .text-success { color: #15803d; }
                    .text-danger { color: #dc2626; }
                    .text-primary { color: #2563eb; }
                    .text-warning { color: #d97706; }
                    .text-muted { color: #64748b; }
                    .badge {
                        display: inline-block;
                        padding: 3px 8px;
                        border-radius: 12px;
                        font-size: 10px;
                        font-weight: 700;
                        white-space: nowrap;
                    }
                    .badge-positive { background: #dcfce7; color: #15803d; }
                    .badge-negative { background: #fee2e2; color: #b91c1c; }
                    .badge-neutral { background: #f1f5f9; color: #475569; }
                    .badge-expense { background: #fee2e2; color: #dc2626; }
                    .badge-income { background: #dcfce7; color: #16a34a; }
                    .badge-transfer { background: #dbeafe; color: #2563eb; }
                    .badge-over { background: #fee2e2; color: #dc2626; font-weight: 800; }
                    .badge-safe { background: #dcfce7; color: #15803d; }
                    .badge-warning { background: #fef3c7; color: #b45309; }
                    .progress-container {
                        width: 100%;
                        background-color: #e2e8f0;
                        border-radius: 4px;
                        height: 7px;
                        overflow: hidden;
                        margin-top: 4px;
                    }
                    .progress-fill {
                        height: 100%;
                        border-radius: 4px;
                    }
                    .fill-green { background-color: #22c55e; }
                    .fill-amber { background-color: #f59e0b; }
                    .fill-red { background-color: #ef4444; }
                    .footer {
                        margin-top: 28px;
                        padding-top: 14px;
                        border-top: 1px solid #e2e8f0;
                        font-size: 10.5px;
                        color: #94a3b8;
                        display: flex;
                        justify-content: space-between;
                    }
                    @media print {
                        body { margin: 12px; }
                        .header { margin-bottom: 14px; }
                        th { background-color: #f1f5f9 !important; -webkit-print-color-adjust: exact; print-color-adjust: exact; }
                        .section-assets { background-color: #dcfce7 !important; -webkit-print-color-adjust: exact; print-color-adjust: exact; }
                        .section-liabilities { background-color: #fee2e2 !important; -webkit-print-color-adjust: exact; print-color-adjust: exact; }
                        .net-worth-row { background-color: #f1f5f9 !important; -webkit-print-color-adjust: exact; print-color-adjust: exact; }
                        .card { border: 1px solid #cbd5e1 !important; }
                    }
                </style>
            </head>
            <body>
                <div class="header">
                    <div class="app-badge">BUDGETER</div>
                    <h1>$title</h1>
                    <div class="subtitle">$subtitle</div>
                </div>
                $contentHtml
                <div class="footer">
                    <span>Budgeter • Personal Finance Management</span>
                    <span>Generated on ${formatTimestamp(System.currentTimeMillis())}</span>
                </div>
            </body>
            </html>
        """.trimIndent()
    }

    // ==========================================
    // 6. TIMELINE (ACCOUNTS OVER TIME) EXPORT
    // ==========================================
    fun exportTimeline(
        context: Context,
        format: ExportFormat,
        timelineData: AccountTimelineData,
        displayCurrency: Boolean = true,
        languageMode: LanguageMode = LanguageMode.ENGLISH
    ) {
        val title = if (languageMode == LanguageMode.BANGLA) "সময়ের সাথে অ্যাকাউন্টের ব্যালেন্স" else "Accounts Balances Over Time"
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())

        when (format) {
            ExportFormat.PDF -> {
                PdfPrintHelper.printTimelineReport(context, timelineData, displayCurrency, languageMode)
            }
            ExportFormat.CSV -> {
                val csv = buildTimelineCsv(timelineData, displayCurrency, languageMode)
                shareFile(context, "Timeline_$timeStamp.csv", format.mimeType, csv, title)
            }
            ExportFormat.HTML -> {
                val html = PdfPrintHelper.buildTimelineHtml(timelineData, displayCurrency, languageMode)
                shareFile(context, "Timeline_$timeStamp.html", format.mimeType, html, title)
            }
            ExportFormat.JSON -> {
                val json = buildTimelineJson(timelineData, languageMode)
                shareFile(context, "Timeline_$timeStamp.json", format.mimeType, json, title)
            }
        }
    }

    private fun buildTimelineCsv(
        data: AccountTimelineData,
        displayCurrency: Boolean,
        languageMode: LanguageMode
    ): String {
        val sb = StringBuilder()
        val currencyHeader = if (displayCurrency) " (BDT)" else ""
        sb.append("Account").append(currencyHeader)
        for (p in data.periods) {
            sb.append(",").append(escapeCsv(p.shortLabel))
        }
        sb.append("\n")

        // Assets
        val assetsLabel = if (languageMode == LanguageMode.BANGLA) "Assets (সম্পদ)" else "Assets"
        sb.append(escapeCsv(assetsLabel))
        for (amt in data.totalAssetsByPeriod) {
            sb.append(",").append(String.format(Locale.US, "%.2f", amt))
        }
        sb.append("\n")

        for (group in data.assetGroups) {
            val groupName = if (languageMode == LanguageMode.BANGLA) group.parentAccount.nameBn else group.parentAccount.nameEn
            sb.append(escapeCsv("> $groupName"))
            for (amt in group.groupBalances) {
                sb.append(",").append(String.format(Locale.US, "%.2f", amt))
            }
            sb.append("\n")

            for (sub in group.subAccounts) {
                val subName = if (languageMode == LanguageMode.BANGLA) sub.account.nameBn else sub.account.nameEn
                sb.append(escapeCsv("  $subName"))
                for (amt in sub.balances) {
                    sb.append(",").append(String.format(Locale.US, "%.2f", amt))
                }
                sb.append("\n")
            }
        }

        // Liabilities
        val liabilitiesLabel = if (languageMode == LanguageMode.BANGLA) "Liabilities (দায়)" else "Liabilities"
        sb.append(escapeCsv(liabilitiesLabel))
        for (amt in data.totalLiabilitiesByPeriod) {
            sb.append(",").append(String.format(Locale.US, "%.2f", amt))
        }
        sb.append("\n")

        for (group in data.liabilityGroups) {
            val groupName = if (languageMode == LanguageMode.BANGLA) group.parentAccount.nameBn else group.parentAccount.nameEn
            sb.append(escapeCsv("> $groupName"))
            for (amt in group.groupBalances) {
                sb.append(",").append(String.format(Locale.US, "%.2f", amt))
            }
            sb.append("\n")

            for (sub in group.subAccounts) {
                val subName = if (languageMode == LanguageMode.BANGLA) sub.account.nameBn else sub.account.nameEn
                sb.append(escapeCsv("  $subName"))
                for (amt in sub.balances) {
                    sb.append(",").append(String.format(Locale.US, "%.2f", amt))
                }
                sb.append("\n")
            }
        }

        // Net Worth
        val netWorthLabel = if (languageMode == LanguageMode.BANGLA) "Net Worth (মোট সম্পদ)" else "Net Worth"
        sb.append(escapeCsv(netWorthLabel))
        for (amt in data.netWorthByPeriod) {
            sb.append(",").append(String.format(Locale.US, "%.2f", amt))
        }
        sb.append("\n")

        return sb.toString()
    }

    private fun buildTimelineJson(
        data: AccountTimelineData,
        languageMode: LanguageMode
    ): String {
        return JSONObject().apply {
            put("reportType", "AccountsTimeline")
            put("generatedAt", formatTimestamp(System.currentTimeMillis()))
            put("periods", JSONArray().apply {
                data.periods.forEach { put(it.shortLabel) }
            })
            put("totalAssets", JSONArray().apply {
                data.totalAssetsByPeriod.forEach { put(it) }
            })
            put("totalLiabilities", JSONArray().apply {
                data.totalLiabilitiesByPeriod.forEach { put(it) }
            })
            put("netWorth", JSONArray().apply {
                data.netWorthByPeriod.forEach { put(it) }
            })
            put("assetGroups", JSONArray().apply {
                data.assetGroups.forEach { group ->
                    val groupName = if (languageMode == LanguageMode.BANGLA) group.parentAccount.nameBn else group.parentAccount.nameEn
                    put(JSONObject().apply {
                        put("groupName", groupName)
                        put("balances", JSONArray().apply { group.groupBalances.forEach { put(it) } })
                        put("subAccounts", JSONArray().apply {
                            group.subAccounts.forEach { sub ->
                                val subName = if (languageMode == LanguageMode.BANGLA) sub.account.nameBn else sub.account.nameEn
                                put(JSONObject().apply {
                                    put("accountName", subName)
                                    put("balances", JSONArray().apply { sub.balances.forEach { put(it) } })
                                })
                            }
                        })
                    })
                }
            })
            put("liabilityGroups", JSONArray().apply {
                data.liabilityGroups.forEach { group ->
                    val groupName = if (languageMode == LanguageMode.BANGLA) group.parentAccount.nameBn else group.parentAccount.nameEn
                    put(JSONObject().apply {
                        put("groupName", groupName)
                        put("balances", JSONArray().apply { group.groupBalances.forEach { put(it) } })
                        put("subAccounts", JSONArray().apply {
                            group.subAccounts.forEach { sub ->
                                val subName = if (languageMode == LanguageMode.BANGLA) sub.account.nameBn else sub.account.nameEn
                                put(JSONObject().apply {
                                    put("accountName", subName)
                                    put("balances", JSONArray().apply { sub.balances.forEach { put(it) } })
                                })
                            }
                        })
                    })
                }
            })
        }.toString(2)
    }

    private fun escapeCsv(value: String): String {
        var str = value.replace("\"", "\"\"")
        if (str.contains(",") || str.contains("\n") || str.contains("\"")) {
            str = "\"$str\""
        }
        return str
    }

    private fun escapeHtml(value: String): String {
        return value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }
}
