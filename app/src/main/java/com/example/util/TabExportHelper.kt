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
                val csv = buildTransactionsCsv(transactions, languageMode)
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
                    put("totalTransactions", transactions.size)
                    put("totalExpense", totalExpense)
                    put("totalIncome", totalIncome)
                    put("netBalance", netBalance)
                    put("transactions", JSONArray().apply {
                        transactions.forEach { tx ->
                            put(JSONObject().apply {
                                put("id", tx.transaction.id)
                                put("date", DateUtils.formatDate(tx.transaction.dateEpochMs, languageMode))
                                put("type", tx.transaction.type.name)
                                put("amount", tx.transaction.amount)
                                put("category", tx.category?.nameEn ?: "")
                                put("subCategory", tx.subCategory?.nameEn ?: "")
                                put("debitAccount", tx.debitAccount?.nameEn ?: "")
                                put("creditAccount", tx.creditAccount?.nameEn ?: "")
                                put("payeeOrPayer", tx.transaction.payeeOrPayer)
                                put("note", tx.transaction.note)
                                put("status", tx.transaction.status.name)
                            })
                        }
                    })
                }.toString(2)
                shareFile(context, "Transactions_$timeStamp.json", format.mimeType, json, title)
            }
        }
    }

    private fun buildTransactionsCsv(transactions: List<TransactionWithDetails>, languageMode: LanguageMode): String {
        val sb = StringBuilder()
        sb.append("Date,Type,Category,Sub-Category,Debit Account,Credit Account,Amount (BDT),Payee/Payer,Note,Status\n")
        for (tx in transactions) {
            val dateStr = DateUtils.formatDate(tx.transaction.dateEpochMs, languageMode)
            val typeStr = tx.transaction.type.name
            val cat = LanguageHelper.getLocalizedName(tx.category?.nameEn ?: "", tx.category?.nameBn ?: "", languageMode)
            val subCat = LanguageHelper.getLocalizedName(tx.subCategory?.nameEn ?: "", tx.subCategory?.nameBn ?: "", languageMode)
            val debitAcc = LanguageHelper.getLocalizedName(tx.debitAccount?.nameEn ?: "", tx.debitAccount?.nameBn ?: "", languageMode)
            val creditAcc = LanguageHelper.getLocalizedName(tx.creditAccount?.nameEn ?: "", tx.creditAccount?.nameBn ?: "", languageMode)
            val amt = String.format(Locale.US, "%.2f", tx.transaction.amount)
            val payee = tx.transaction.payeeOrPayer
            val note = tx.transaction.note
            val status = tx.transaction.status.name

            sb.append("${escapeCsv(dateStr)},${escapeCsv(typeStr)},${escapeCsv(cat)},${escapeCsv(subCat)},${escapeCsv(debitAcc)},${escapeCsv(creditAcc)},$amt,${escapeCsv(payee)},${escapeCsv(note)},${escapeCsv(status)}\n")
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
                        <th>${if (languageMode == LanguageMode.BANGLA) "তারিখ" else "Date"}</th>
                        <th>${if (languageMode == LanguageMode.BANGLA) "ধরন" else "Type"}</th>
                        <th>${if (languageMode == LanguageMode.BANGLA) "ক্যাটাগরি" else "Category"}</th>
                        <th>${if (languageMode == LanguageMode.BANGLA) "অ্যাকাউন্ট" else "Account"}</th>
                        <th style="text-align: right;">${if (languageMode == LanguageMode.BANGLA) "পরিমাণ" else "Amount"}</th>
                        <th>${if (languageMode == LanguageMode.BANGLA) "পেয়ী / গ্রহীতা" else "Payee"}</th>
                        <th>${if (languageMode == LanguageMode.BANGLA) "নোট" else "Note"}</th>
                    </tr>
                </thead>
                <tbody>
        """.trimIndent())

        for (tx in transactions) {
            val dateStr = DateUtils.formatDate(tx.transaction.dateEpochMs, languageMode)
            val cat = LanguageHelper.getLocalizedName(tx.category?.nameEn ?: "-", tx.category?.nameBn ?: "-", languageMode)
            val subCat = tx.subCategory?.let { " (${LanguageHelper.getLocalizedName(it.nameEn, it.nameBn, languageMode)})" } ?: ""
            
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
                    <td>$dateStr</td>
                    <td><span class="badge badge-${tx.transaction.type.name.lowercase()}">${tx.transaction.type.name}</span></td>
                    <td><strong>$cat</strong><span style="color:#64748b; font-size:10px;">$subCat</span></td>
                    <td>$accDisplay</td>
                    <td style="text-align: right;" class="$colorClass"><strong>৳ ${LanguageHelper.formatNumber(tx.transaction.amount, languageMode)}</strong></td>
                    <td>${escapeHtml(tx.transaction.payeeOrPayer.ifBlank { "-" })}</td>
                    <td>${escapeHtml(tx.transaction.note.ifBlank { "-" })}</td>
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
        languageMode: LanguageMode = LanguageMode.ENGLISH
    ) {
        val title = if (languageMode == LanguageMode.BANGLA) "বাজেট ট্র্যাকিং রিপোর্ট ($periodLabel)" else "Budget Tracking Report ($periodLabel)"
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val remaining = totalBudget - totalSpent

        when (format) {
            ExportFormat.PDF -> {
                val html = buildBudgetHtml(title, periodLabel, activeTabMode, groups, totalBudget, totalSpent, remaining, showComparison, languageMode)
                PdfPrintHelper.printHtml(context, "Budget_$timeStamp", html, isLandscape = false)
            }
            ExportFormat.CSV -> {
                val csv = buildBudgetCsv(periodLabel, activeTabMode, groups, showComparison, languageMode)
                shareFile(context, "Budget_$timeStamp.csv", format.mimeType, csv, title)
            }
            ExportFormat.HTML -> {
                val html = buildBudgetHtml(title, periodLabel, activeTabMode, groups, totalBudget, totalSpent, remaining, showComparison, languageMode)
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
                    put("groups", JSONArray().apply {
                        groups.forEach { grp ->
                            put(JSONObject().apply {
                                put("groupName", grp.groupNameEn)
                                put("totalBudget", grp.totalBudget)
                                put("totalSpent", grp.totalSpent)
                                put("categories", JSONArray().apply {
                                    grp.items.forEach { item ->
                                        put(JSONObject().apply {
                                            put("categoryName", item.category.nameEn)
                                            put("budget", item.budgetLimit)
                                            put("spent", item.spentAmount)
                                            put("remaining", item.budgetLimit - item.spentAmount)
                                            put("percentage", item.percentageInt)
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
        languageMode: LanguageMode
    ): String {
        val sb = StringBuilder()
        if (showComparison) {
            sb.append("Group,Category,Budget (BDT),Current Spent (BDT),Base Period Spent (BDT),Variance (BDT),% Spent\n")
        } else {
            sb.append("Group,Category,Budget (BDT),Actual Spent (BDT),Remaining (BDT),% Spent\n")
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
                    sb.append("${escapeCsv(groupName)},${escapeCsv(catName)},$budget,$spent,$baseSpent,$variance,${item.percentageInt}%\n")
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
        languageMode: LanguageMode
    ): String {
        val sb = StringBuilder()
        val percentOverall = if (totalBudget > 0) ((totalSpent / totalBudget) * 100).toInt() else 0

        sb.append("""
            <div class="summary-cards">
                <div class="card">
                    <div class="card-label">${if (languageMode == LanguageMode.BANGLA) "মোট বাজেট" else "Total Budget"}</div>
                    <div class="card-value text-primary">৳ ${LanguageHelper.formatNumber(totalBudget, languageMode)}</div>
                </div>
                <div class="card">
                    <div class="card-label">${if (languageMode == LanguageMode.BANGLA) "মোট ব্যয়িত" else "Total Spent"}</div>
                    <div class="card-value text-danger">৳ ${LanguageHelper.formatNumber(totalSpent, languageMode)}</div>
                </div>
                <div class="card">
                    <div class="card-label">${if (languageMode == LanguageMode.BANGLA) "অবশিষ্ট" else "Remaining"}</div>
                    <div class="card-value ${if (remaining >= 0) "text-success" else "text-danger"}">৳ ${LanguageHelper.formatNumber(remaining, languageMode)}</div>
                </div>
                <div class="card">
                    <div class="card-label">${if (languageMode == LanguageMode.BANGLA) "ব্যয়িত হার" else "Spent %"}</div>
                    <div class="card-value ${if (percentOverall > 100) "text-danger" else "text-primary"}">$percentOverall%</div>
                </div>
            </div>
            <table>
                <thead>
                    <tr>
                        <th>${if (languageMode == LanguageMode.BANGLA) "ক্যাটাগরি / গ্রুপ" else "Category / Group"}</th>
                        <th style="text-align: right;">${if (languageMode == LanguageMode.BANGLA) "বাজেট" else "Budget"}</th>
                        <th style="text-align: right;">${if (languageMode == LanguageMode.BANGLA) "ব্যয়িত" else "Spent"}</th>
                        <th style="text-align: right;">${if (showComparison) "পূর্ববর্তী" else "অবশিষ্ট"}</th>
                        <th style="text-align: right;">${if (languageMode == LanguageMode.BANGLA) "হার" else "%"}</th>
                    </tr>
                </thead>
                <tbody>
        """.trimIndent())

        for (grp in groups) {
            val grpName = LanguageHelper.getLocalizedName(grp.groupNameEn, grp.groupNameBn, languageMode)
            sb.append("""
                <tr class="group-row">
                    <td colspan="5"><strong>📁 $grpName</strong> <span style="float: right; color: #1e293b; font-weight: bold;">৳ ${LanguageHelper.formatNumber(grp.totalSpent, languageMode)}</span></td>
                </tr>
            """.trimIndent())

            for (item in grp.items) {
                val catName = LanguageHelper.getLocalizedName(item.category.nameEn, item.category.nameBn, languageMode)
                val isOver = item.isOverBudget
                val diffVal = if (showComparison) item.spentBaseAmount else (item.budgetLimit - item.spentAmount)

                sb.append("""
                    <tr>
                        <td style="padding-left: 24px;">• $catName</td>
                        <td style="text-align: right;">৳ ${LanguageHelper.formatNumber(item.budgetLimit, languageMode)}</td>
                        <td style="text-align: right; font-weight: 600;" class="${if (item.spentAmount > 0) "text-danger" else ""}">৳ ${LanguageHelper.formatNumber(item.spentAmount, languageMode)}</td>
                        <td style="text-align: right;" class="${if (!showComparison && diffVal < 0) "text-danger" else "text-success"}">৳ ${LanguageHelper.formatNumber(diffVal, languageMode)}</td>
                        <td style="text-align: right; font-weight: bold;" class="${if (isOver) "text-danger" else ""}">${item.percentageInt}%</td>
                    </tr>
                """.trimIndent())
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
        languageMode: LanguageMode = LanguageMode.ENGLISH
    ) {
        val title = if (languageMode == LanguageMode.BANGLA) "ব্যালেন্স শীট রিপোর্ট ($asOfDateLabel)" else "Balance Sheet Report ($asOfDateLabel)"
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())

        when (format) {
            ExportFormat.PDF -> {
                val html = buildBalanceSheetHtml(title, asOfDateLabel, assetGroups, totalAssets, liabilityGroups, totalLiabilities, netWorth, languageMode)
                PdfPrintHelper.printHtml(context, "BalanceSheet_$timeStamp", html, isLandscape = false)
            }
            ExportFormat.CSV -> {
                val csv = buildBalanceSheetCsv(asOfDateLabel, assetGroups, totalAssets, liabilityGroups, totalLiabilities, netWorth, languageMode)
                shareFile(context, "BalanceSheet_$timeStamp.csv", format.mimeType, csv, title)
            }
            ExportFormat.HTML -> {
                val html = buildBalanceSheetHtml(title, asOfDateLabel, assetGroups, totalAssets, liabilityGroups, totalLiabilities, netWorth, languageMode)
                shareFile(context, "BalanceSheet_$timeStamp.html", format.mimeType, html, title)
            }
            ExportFormat.JSON -> {
                val json = JSONObject().apply {
                    put("reportType", "BalanceSheet")
                    put("asOfDate", asOfDateLabel)
                    put("totalAssets", totalAssets)
                    put("totalLiabilities", totalLiabilities)
                    put("netWorth", netWorth)
                    put("assets", JSONArray().apply {
                        assetGroups.forEach { grp ->
                            put(JSONObject().apply {
                                put("groupName", grp.parentAccount.nameEn)
                                put("balance", grp.effectiveCurrentBalance)
                                put("subAccounts", JSONArray().apply {
                                    grp.subAccounts.forEach { sub ->
                                        put(JSONObject().apply {
                                            put("accountName", sub.account.nameEn)
                                            put("balance", sub.effectiveCurrentBalance)
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
                                put("subAccounts", JSONArray().apply {
                                    grp.subAccounts.forEach { sub ->
                                        put(JSONObject().apply {
                                            put("accountName", sub.account.nameEn)
                                            put("balance", sub.effectiveCurrentBalance)
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
        languageMode: LanguageMode
    ): String {
        val sb = StringBuilder()
        sb.append("Section,Group,Account,Balance (BDT)\n")

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
        languageMode: LanguageMode
    ): String {
        val sb = StringBuilder()
        sb.append("""
            <div class="summary-cards">
                <div class="card">
                    <div class="card-label">${if (languageMode == LanguageMode.BANGLA) "মোট সম্পদ" else "Total Assets"}</div>
                    <div class="card-value text-success">৳ ${LanguageHelper.formatNumber(totalAssets, languageMode)}</div>
                </div>
                <div class="card">
                    <div class="card-label">${if (languageMode == LanguageMode.BANGLA) "মোট দায়" else "Total Liabilities"}</div>
                    <div class="card-value text-danger">৳ ${LanguageHelper.formatNumber(totalLiabilities, languageMode)}</div>
                </div>
                <div class="card">
                    <div class="card-label">${if (languageMode == LanguageMode.BANGLA) "নেট সম্পদ" else "Net Worth"}</div>
                    <div class="card-value ${if (netWorth >= 0) "text-success" else "text-danger"}">৳ ${LanguageHelper.formatNumber(netWorth, languageMode)}</div>
                </div>
            </div>
            <table>
                <thead>
                    <tr>
                        <th>${if (languageMode == LanguageMode.BANGLA) "অ্যাকাউন্ট / গ্রুপ" else "Account / Group"}</th>
                        <th style="text-align: right;">${if (languageMode == LanguageMode.BANGLA) "ব্যালেন্স (BDT)" else "Balance (BDT)"}</th>
                    </tr>
                </thead>
                <tbody>
                    <tr class="section-assets">
                        <td colspan="2"><strong>${if (languageMode == LanguageMode.BANGLA) "সম্পদ (Assets)" else "Assets"} — ৳ ${LanguageHelper.formatNumber(totalAssets, languageMode)}</strong></td>
                    </tr>
        """.trimIndent())

        for (grp in assetGroups) {
            val grpName = LanguageHelper.getLocalizedName(grp.parentAccount.nameEn, grp.parentAccount.nameBn, languageMode)
            sb.append("""
                <tr class="group-row">
                    <td><strong>&gt; $grpName</strong></td>
                    <td style="text-align: right; font-weight: bold;">৳ ${LanguageHelper.formatNumber(grp.effectiveCurrentBalance, languageMode)}</td>
                </tr>
            """.trimIndent())
            for (sub in grp.subAccounts) {
                val subName = LanguageHelper.getLocalizedName(sub.account.nameEn, sub.account.nameBn, languageMode)
                sb.append("""
                    <tr>
                        <td style="padding-left: 24px;">• $subName</td>
                        <td style="text-align: right;">৳ ${LanguageHelper.formatNumber(sub.effectiveCurrentBalance, languageMode)}</td>
                    </tr>
                """.trimIndent())
            }
        }

        sb.append("""
                    <tr class="section-liabilities" style="margin-top: 10px;">
                        <td colspan="2"><strong>${if (languageMode == LanguageMode.BANGLA) "দায় (Liabilities)" else "Liabilities"} — ৳ ${LanguageHelper.formatNumber(totalLiabilities, languageMode)}</strong></td>
                    </tr>
        """.trimIndent())

        for (grp in liabilityGroups) {
            val grpName = LanguageHelper.getLocalizedName(grp.parentAccount.nameEn, grp.parentAccount.nameBn, languageMode)
            sb.append("""
                <tr class="group-row">
                    <td><strong>&gt; $grpName</strong></td>
                    <td style="text-align: right; font-weight: bold;">৳ ${LanguageHelper.formatNumber(grp.effectiveCurrentBalance, languageMode)}</td>
                </tr>
            """.trimIndent())
            for (sub in grp.subAccounts) {
                val subName = LanguageHelper.getLocalizedName(sub.account.nameEn, sub.account.nameBn, languageMode)
                sb.append("""
                    <tr>
                        <td style="padding-left: 24px;">• $subName</td>
                        <td style="text-align: right;">৳ ${LanguageHelper.formatNumber(sub.effectiveCurrentBalance, languageMode)}</td>
                    </tr>
                """.trimIndent())
            }
        }

        sb.append("""
                    <tr class="net-worth-row">
                        <td><strong>${if (languageMode == LanguageMode.BANGLA) "নেট সম্পদ (Net Worth)" else "Net Worth"}</strong></td>
                        <td style="text-align: right; font-size: 14px; font-weight: bold;" class="${if (netWorth >= 0) "text-success" else "text-danger"}">৳ ${LanguageHelper.formatNumber(netWorth, languageMode)}</td>
                    </tr>
                </tbody>
            </table>
        """.trimIndent())

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
                val csv = buildLabelsCsv(labels, languageMode)
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

    private fun buildLabelsCsv(labels: List<AggregatedLabel>, languageMode: LanguageMode): String {
        val sb = StringBuilder()
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
                val csv = buildItemsCsv(items, languageMode)
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

    private fun buildItemsCsv(items: List<AggregatedItem>, languageMode: LanguageMode): String {
        val sb = StringBuilder()
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
            Toast.makeText(context, "Export ready: $fileName", Toast.LENGTH_SHORT).show()
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
                        font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
                        margin: 24px;
                        color: #1e293b;
                        background: #ffffff;
                        line-height: 1.5;
                        font-size: 12px;
                    }
                    .header {
                        margin-bottom: 20px;
                        border-bottom: 2px solid #e2e8f0;
                        padding-bottom: 12px;
                    }
                    .app-badge {
                        display: inline-block;
                        background: #2563eb;
                        color: #ffffff;
                        font-weight: 700;
                        font-size: 11px;
                        padding: 3px 8px;
                        border-radius: 4px;
                        margin-bottom: 6px;
                        letter-spacing: 0.5px;
                    }
                    h1 {
                        font-size: 20px;
                        margin: 4px 0;
                        color: #0f172a;
                        font-weight: 800;
                    }
                    .subtitle {
                        font-size: 11px;
                        color: #64748b;
                    }
                    .summary-cards {
                        display: flex;
                        gap: 12px;
                        margin-bottom: 20px;
                        flex-wrap: wrap;
                    }
                    .card {
                        flex: 1;
                        min-width: 120px;
                        background: #f8fafc;
                        border: 1px solid #e2e8f0;
                        border-radius: 8px;
                        padding: 10px 14px;
                    }
                    .card-label {
                        font-size: 10px;
                        color: #64748b;
                        font-weight: 600;
                        text-transform: uppercase;
                        letter-spacing: 0.5px;
                        margin-bottom: 2px;
                    }
                    .card-value {
                        font-size: 15px;
                        font-weight: 700;
                        color: #0f172a;
                    }
                    table {
                        width: 100%;
                        border-collapse: collapse;
                        font-size: 11px;
                        margin-top: 8px;
                    }
                    th, td {
                        padding: 7px 10px;
                        border-bottom: 1px solid #e2e8f0;
                        text-align: left;
                    }
                    th {
                        background-color: #f1f5f9;
                        font-weight: 700;
                        color: #334155;
                        border-top: 1px solid #cbd5e1;
                        border-bottom: 2px solid #cbd5e1;
                        font-size: 10.5px;
                        text-transform: uppercase;
                    }
                    tr:nth-child(even):not(.group-row):not(.section-assets):not(.section-liabilities):not(.net-worth-row) {
                        background-color: #fafbfc;
                    }
                    .group-row {
                        background-color: #f8fafc;
                        font-weight: 700;
                        color: #1e40af;
                        border-top: 1px solid #e2e8f0;
                    }
                    .section-assets {
                        background-color: #dcfce7;
                        color: #15803d;
                        font-weight: 700;
                    }
                    .section-liabilities {
                        background-color: #fee2e2;
                        color: #b91c1c;
                        font-weight: 700;
                    }
                    .net-worth-row {
                        background-color: #f1f5f9;
                        font-weight: 800;
                        font-size: 12px;
                        border-top: 2px solid #94a3b8;
                        border-bottom: 2px solid #94a3b8;
                    }
                    .text-success { color: #16a34a; }
                    .text-danger { color: #dc2626; }
                    .text-primary { color: #2563eb; }
                    .badge {
                        display: inline-block;
                        padding: 2px 6px;
                        border-radius: 4px;
                        font-size: 9px;
                        font-weight: 700;
                        text-transform: uppercase;
                    }
                    .badge-expense { background: #fee2e2; color: #dc2626; }
                    .badge-income { background: #dcfce7; color: #16a34a; }
                    .badge-transfer { background: #dbeafe; color: #2563eb; }
                    .footer {
                        margin-top: 24px;
                        padding-top: 12px;
                        border-top: 1px solid #e2e8f0;
                        font-size: 10px;
                        color: #94a3b8;
                        display: flex;
                        justify-content: space-between;
                    }
                    @media print {
                        body { margin: 10px; }
                        .header { margin-bottom: 12px; }
                        th { background-color: #f1f5f9 !important; -webkit-print-color-adjust: exact; }
                        .section-assets { background-color: #dcfce7 !important; -webkit-print-color-adjust: exact; }
                        .section-liabilities { background-color: #fee2e2 !important; -webkit-print-color-adjust: exact; }
                        .net-worth-row { background-color: #f1f5f9 !important; -webkit-print-color-adjust: exact; }
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
                    <span>Printed/Exported on ${formatTimestamp(System.currentTimeMillis())}</span>
                </div>
            </body>
            </html>
        """.trimIndent()
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
