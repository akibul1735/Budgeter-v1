package com.example.ui.components.assistant

import com.example.data.model.Account
import com.example.data.model.Category
import com.example.data.model.LanguageMode
import com.example.data.model.MonthlyBudget
import com.example.data.model.TransactionType
import com.example.data.model.TransactionWithDetails
import com.example.data.repository.AccountWithBalance
import com.example.data.repository.FinancialOverview
import com.example.util.DateUtils
import com.example.util.LanguageHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

/**
 * Chat message model for Budgeter Assistant.
 */
data class AssistantMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val interactiveChips: List<AssistantChip> = emptyList(),
    val transactionList: List<TransactionWithDetails> = emptyList(),
    val metricsSummary: AssistantMetrics? = null
)

data class AssistantChip(
    val label: String,
    val actionQuery: String,
    val iconName: String? = null
)

data class AssistantMetrics(
    val title: String,
    val totalIn: Double? = null,
    val totalOut: Double? = null,
    val netAmount: Double? = null,
    val count: Int = 0
)

/**
 * Intelligent, 100% offline query & reasoning engine for Budgeter.
 * Operates purely on read-only in-memory data, ensuring zero risk to existing records.
 */
object FinancialAssistantEngine {

    fun processQuery(
        query: String,
        languageMode: LanguageMode,
        allAccounts: List<Account>,
        accountsWithBalances: List<AccountWithBalance>,
        allCategories: List<Category>,
        transactions: List<TransactionWithDetails>,
        overview: FinancialOverview,
        budgets: List<MonthlyBudget>
    ): AssistantMessage {
        val trimmed = query.trim()
        val lower = trimmed.lowercase(Locale.ROOT)
        val isBn = languageMode == LanguageMode.BANGLA

        // 1. Check for interactive clarification case:
        // When user asks "is there any account related transactions in past 7 days" or mentions account without specifying which one
        if (isGenericAccountQuery(lower)) {
            val days = extractDays(lower) ?: 7
            val periodLabel = if (days == 7) {
                if (isBn) "বিগত ৭ দিনের" else "the past 7 days"
            } else {
                if (isBn) "বিগত $days দিনের" else "the past $days days"
            }

            val questionText = if (isBn) {
                "আপনি $periodLabel জন্য কোন অ্যাকাউন্টটি দেখতে চান?"
            } else {
                "Which account would you like to check for $periodLabel?"
            }

            val chips = mutableListOf<AssistantChip>()
            chips.add(
                AssistantChip(
                    label = if (isBn) "সকল অ্যাকাউন্ট" else "All Accounts",
                    actionQuery = "transactions for all accounts in past $days days"
                )
            )

            // Add chips for top active accounts
            allAccounts.filter { it.isActive }.take(6).forEach { account ->
                val accName = account.localizedName(languageMode)
                chips.add(
                    AssistantChip(
                        label = accName,
                        actionQuery = "transactions for ${account.nameEn} in past $days days"
                    )
                )
            }

            return AssistantMessage(
                text = questionText,
                isUser = false,
                interactiveChips = chips
            )
        }

        // 2. Query for "all accounts in past X days / period"
        if (lower.contains("all accounts") || (lower.contains("all") && lower.contains("account"))) {
            val days = extractDays(lower) ?: 7
            return handleAllAccountsPeriodTransactions(days, transactions, allAccounts, isBn, languageMode)
        }

        // 3. Query for a SPECIFIC account (e.g. "rocket", "bkash", "cash", "bank", etc.)
        val matchedAccount = findMatchingAccount(lower, allAccounts)
        if (matchedAccount != null) {
            val days = extractDays(lower) ?: 7
            return handleSpecificAccountTransactions(matchedAccount, days, transactions, isBn, languageMode)
        }

        // 4. Net Worth & Balance queries
        if (lower.contains("net worth") || lower.contains("networth") || lower.contains("সম্পদ") ||
            lower.contains("total balance") || lower.contains("ব্যালেন্স") || lower.contains("balance") ||
            lower.contains("how much money") || lower.contains("টাকা আছে")
        ) {
            return handleNetWorthQuery(overview, accountsWithBalances, isBn, languageMode)
        }

        // 5. Monthly Expenses / Spending / Cash Flow
        if (lower.contains("spend this month") || lower.contains("monthly expense") || lower.contains("monthly spending") ||
            lower.contains("মাসিক খরচ") || lower.contains("এই মাসের খরচ") || lower.contains("cash flow") || lower.contains("ক্যাশ ফ্লো")
        ) {
            return handleMonthlySpendingQuery(transactions, isBn, languageMode)
        }

        // 6. Top Expenses / Categories
        if (lower.contains("top expense") || lower.contains("top spending") || lower.contains("top category") ||
            lower.contains("সর্বোচ্চ খরচ") || lower.contains("কোথায় খরচ") || lower.contains("where did my money go")
        ) {
            return handleTopExpensesQuery(transactions, allCategories, isBn, languageMode)
        }

        // 7. Budget Status & Limits
        if (lower.contains("budget") || lower.contains("বাজেট") || lower.contains("over budget")) {
            return handleBudgetStatusQuery(budgets, transactions, allCategories, isBn, languageMode)
        }

        // 8. Specific Category Query (e.g., "Food", "Shopping", "Transport", "Bills")
        val matchedCategory = findMatchingCategory(lower, allCategories)
        if (matchedCategory != null) {
            val days = extractDays(lower) ?: 30
            return handleCategorySpendingQuery(matchedCategory, days, transactions, isBn, languageMode)
        }

        // 9. Recent transactions query
        if (lower.contains("recent") || lower.contains("last transaction") || lower.contains("সাম্প্রতিক লেনদেন") || lower.contains("latest")) {
            return handleRecentTransactionsQuery(transactions, isBn, languageMode)
        }

        // 10. Default / Fallback with smart suggestions
        val fallbackText = if (isBn) {
            "আমি আপনার ফাইন্যান্সিয়াল সহকারী। আপনি নির্দিষ্ট অ্যাকাউন্ট, সময়কাল, বাজেট বা ব্যালেন্স সম্পর্কে যে কোনো প্রশ্ন করতে পারেন। নিচে কয়েকটি উদাহরণ দেখুন:"
        } else {
            "I am your offline Financial Assistant. You can ask about account transactions, date periods, budgets, net worth, or spending categories. Here are some examples to try:"
        }

        val defaultChips = listOf(
            AssistantChip(
                label = if (isBn) "রকেট বিগত ৭ দিন" else "Rocket in past 7 days",
                actionQuery = "Is there any Rocket account related transactions in past 7 days"
            ),
            AssistantChip(
                label = if (isBn) "অ্যাকাউন্ট লেনদেন (৭ দিন)" else "Account transactions (7 days)",
                actionQuery = "Is there any account related transactions in past 7 days"
            ),
            AssistantChip(
                label = if (isBn) "মোট সম্পদ ও ব্যালেন্স" else "Net worth & Balances",
                actionQuery = "What is my current net worth and balance?"
            ),
            AssistantChip(
                label = if (isBn) "চলতি মাসের খরচ" else "This month's expenses",
                actionQuery = "How much did I spend this month?"
            ),
            AssistantChip(
                label = if (isBn) "শীর্ষ খরচসমূহ" else "Top spending categories",
                actionQuery = "Show top spending categories"
            ),
            AssistantChip(
                label = if (isBn) "বাজেটের অবস্থা" else "Budget status",
                actionQuery = "Show my budget status"
            )
        )

        return AssistantMessage(
            text = fallbackText,
            isUser = false,
            interactiveChips = defaultChips
        )
    }

    private fun isGenericAccountQuery(query: String): Boolean {
        // e.g. "is there any account related transactions in past 7 days"
        // Notice it has "account" but doesn't mention specific account names like "rocket", "bkash", etc.
        val hasAccountWord = query.contains("account") || query.contains("অ্যাকাউন্ট")
        val hasTransactionWord = query.contains("transaction") || query.contains("লেনদেন") || query.contains("history")

        val specificKeywords = listOf(
            "rocket", "bkash", "nagad", "cash", "bank", "dbbl", "city bank", "brac", "upay",
            "all accounts", "all account", "সব অ্যাকাউন্ট"
        )
        val mentionsSpecific = specificKeywords.any { query.contains(it) }

        return (hasAccountWord || hasTransactionWord) && !mentionsSpecific && (query.contains("any") || query.contains("কোনো") || query.contains("which"))
    }

    private fun extractDays(query: String): Int? {
        val regex = Regex("""(\d+)\s*(day|days|দিন)""")
        val match = regex.find(query)
        if (match != null) {
            return match.groupValues[1].toIntOrNull()
        }
        if (query.contains("today") || query.contains("আজ")) return 1
        if (query.contains("yesterday") || query.contains("গতকাল")) return 2
        if (query.contains("week") || query.contains("সপ্তাহ")) return 7
        if (query.contains("month") || query.contains("মাস")) return 30
        return null
    }

    private fun findMatchingAccount(query: String, accounts: List<Account>): Account? {
        for (acc in accounts) {
            val enName = acc.nameEn.lowercase(Locale.ROOT)
            val bnName = acc.nameBn.lowercase(Locale.ROOT)
            if (enName.isNotBlank() && (query.contains(enName) || enName.split(" ").any { it.length > 2 && query.contains(it) })) {
                return acc
            }
            if (bnName.isNotBlank() && query.contains(bnName)) {
                return acc
            }
        }
        // Common alias checks
        if (query.contains("rocket") || query.contains("রকেট")) {
            return accounts.find { it.nameEn.contains("Rocket", ignoreCase = true) || it.nameBn.contains("রকেট") }
        }
        if (query.contains("bkash") || query.contains("বিকাশ")) {
            return accounts.find { it.nameEn.contains("bKash", ignoreCase = true) || it.nameBn.contains("বিকাশ") }
        }
        if (query.contains("nagad") || query.contains("নগদ")) {
            return accounts.find { it.nameEn.contains("Nagad", ignoreCase = true) || it.nameBn.contains("নগদ") }
        }
        if (query.contains("cash") || query.contains("নগদ টাকা") || query.contains("ক্যাশ")) {
            return accounts.find { it.nameEn.contains("Cash", ignoreCase = true) || it.nameBn.contains("ক্যাশ") }
        }
        return null
    }

    private fun findMatchingCategory(query: String, categories: List<Category>): Category? {
        for (cat in categories) {
            val enName = cat.nameEn.lowercase(Locale.ROOT)
            val bnName = cat.nameBn.lowercase(Locale.ROOT)
            if (enName.isNotBlank() && query.contains(enName)) return cat
            if (bnName.isNotBlank() && query.contains(bnName)) return cat
        }
        return null
    }

    private fun handleSpecificAccountTransactions(
        account: Account,
        days: Int,
        allTxs: List<TransactionWithDetails>,
        isBn: Boolean,
        languageMode: LanguageMode
    ): AssistantMessage {
        val now = System.currentTimeMillis()
        val startMs = now - (days.toLong() * 24L * 60L * 60L * 1000L)
        val accName = account.localizedName(languageMode)

        // Find transactions linked to this account
        val matchedTxs = allTxs.filter { td ->
            val tx = td.transaction
            val isLinked = tx.debitAccountId == account.id || tx.creditAccountId == account.id
            isLinked && tx.dateEpochMs in startMs..now
        }.sortedByDescending { it.transaction.dateEpochMs }

        if (matchedTxs.isEmpty()) {
            // Find most recent transaction if any
            val pastTx = allTxs.filter { td ->
                val tx = td.transaction
                tx.debitAccountId == account.id || tx.creditAccountId == account.id
            }.maxByOrNull { it.transaction.dateEpochMs }

            val pastNote = if (pastTx != null) {
                val pastDate = DateUtils.formatDate(pastTx.transaction.dateEpochMs, languageMode)
                val pastAmt = LanguageHelper.formatCurrency(pastTx.transaction.amount, languageMode)
                if (isBn) "\n*(সর্বশেষ লেনদেন হয়েছিল $pastDate তারিখে: $pastAmt)*"
                else "\n*(Last recorded transaction was on $pastDate: $pastAmt)*"
            } else ""

            val emptyMsg = if (isBn) {
                "বিগত $days দিনে **$accName** অ্যাকাউন্টে কোনো লেনদেন পাওয়া যায়নি।$pastNote"
            } else {
                "No transactions were found for your **$accName** account in the past $days days.$pastNote"
            }

            return AssistantMessage(
                text = emptyMsg,
                isUser = false,
                transactionList = emptyList()
            )
        }

        var totalIn = 0.0
        var totalOut = 0.0

        matchedTxs.forEach { td ->
            val tx = td.transaction
            when (tx.type) {
                TransactionType.INCOME -> {
                    if (tx.debitAccountId == account.id) totalIn += tx.amount
                }
                TransactionType.EXPENSE -> {
                    if (tx.creditAccountId == account.id) totalOut += tx.amount
                }
                TransactionType.TRANSFER -> {
                    if (tx.debitAccountId == account.id) totalIn += tx.amount
                    if (tx.creditAccountId == account.id) totalOut += tx.amount
                }
            }
        }

        val net = totalIn - totalOut

        val headerText = if (isBn) {
            "হ্যাঁ, বিগত $days দিনে **$accName** অ্যাকাউন্টে মোট **${matchedTxs.size}টি** লেনদেন পাওয়া গেছে:"
        } else {
            "Yes, you have **${matchedTxs.size}** transaction${if (matchedTxs.size > 1) "s" else ""} for **$accName** in the past $days days:"
        }

        val metrics = AssistantMetrics(
            title = accName,
            totalIn = totalIn,
            totalOut = totalOut,
            netAmount = net,
            count = matchedTxs.size
        )

        return AssistantMessage(
            text = headerText,
            isUser = false,
            transactionList = matchedTxs,
            metricsSummary = metrics
        )
    }

    private fun handleAllAccountsPeriodTransactions(
        days: Int,
        allTxs: List<TransactionWithDetails>,
        accounts: List<Account>,
        isBn: Boolean,
        languageMode: LanguageMode
    ): AssistantMessage {
        val now = System.currentTimeMillis()
        val startMs = now - (days.toLong() * 24L * 60L * 60L * 1000L)

        val matchedTxs = allTxs.filter { it.transaction.dateEpochMs in startMs..now }
            .sortedByDescending { it.transaction.dateEpochMs }

        var totalIn = 0.0
        var totalOut = 0.0
        matchedTxs.forEach { td ->
            when (td.transaction.type) {
                TransactionType.INCOME -> totalIn += td.transaction.amount
                TransactionType.EXPENSE -> totalOut += td.transaction.amount
                TransactionType.TRANSFER -> {}
            }
        }

        val net = totalIn - totalOut

        val summaryText = if (isBn) {
            "বিগত $days দিনে সকল অ্যাকাউন্ট মিলিয়ে মোট **${matchedTxs.size}টি** লেনদেন পাওয়া গেছে।"
        } else {
            "Found **${matchedTxs.size}** transactions across all accounts in the past $days days."
        }

        // Account-wise breakdown chips for easy drilldown
        val chips = accounts.filter { it.isActive }.take(6).map { acc ->
            AssistantChip(
                label = acc.localizedName(languageMode),
                actionQuery = "transactions for ${acc.nameEn} in past $days days"
            )
        }

        return AssistantMessage(
            text = summaryText,
            isUser = false,
            transactionList = matchedTxs.take(15),
            metricsSummary = AssistantMetrics(
                title = if (isBn) "সকল অ্যাকাউন্ট" else "All Accounts",
                totalIn = totalIn,
                totalOut = totalOut,
                netAmount = net,
                count = matchedTxs.size
            ),
            interactiveChips = chips
        )
    }

    private fun handleNetWorthQuery(
        overview: FinancialOverview,
        accountsWithBalances: List<AccountWithBalance>,
        isBn: Boolean,
        languageMode: LanguageMode
    ): AssistantMessage {
        val netWorthStr = LanguageHelper.formatCurrency(overview.netWorth, languageMode)
        val assetsStr = LanguageHelper.formatCurrency(overview.totalAssets, languageMode)
        val liabilitiesStr = LanguageHelper.formatCurrency(overview.totalLiabilities, languageMode)

        val sb = StringBuilder()
        if (isBn) {
            sb.append("📊 **আপনার বর্তমান মোট আর্থিক অবস্থা:**\n\n")
            sb.append("• নিট সম্পদ (Net Worth): **$netWorthStr**\n")
            sb.append("• মোট সম্পদ (Assets): **$assetsStr**\n")
            sb.append("• মোট দায়/ঋণ (Liabilities): **$liabilitiesStr**\n\n")
            sb.append("**প্রধান অ্যাকাউন্টসমূহের ব্যালেন্স:**\n")
        } else {
            sb.append("📊 **Your Current Financial Overview:**\n\n")
            sb.append("• Net Worth: **$netWorthStr**\n")
            sb.append("• Total Assets: **$assetsStr**\n")
            sb.append("• Total Liabilities: **$liabilitiesStr**\n\n")
            sb.append("**Active Account Balances:**\n")
        }

        accountsWithBalances.take(5).forEach { ab ->
            val balStr = LanguageHelper.formatCurrency(ab.currentBalance, languageMode)
            val name = ab.account.localizedName(languageMode)
            sb.append("• $name: **$balStr**\n")
        }

        val chips = listOf(
            AssistantChip(
                label = if (isBn) "চলতি মাসের খরচ" else "This Month's Spending",
                actionQuery = "How much did I spend this month?"
            ),
            AssistantChip(
                label = if (isBn) "বাজেটের অবস্থা" else "Budget Status",
                actionQuery = "Show my budget status"
            )
        )

        return AssistantMessage(
            text = sb.toString().trim(),
            isUser = false,
            interactiveChips = chips
        )
    }

    private fun handleMonthlySpendingQuery(
        allTxs: List<TransactionWithDetails>,
        isBn: Boolean,
        languageMode: LanguageMode
    ): AssistantMessage {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfMonth = cal.timeInMillis
        val now = System.currentTimeMillis()

        val monthlyTxs = allTxs.filter { it.transaction.dateEpochMs in startOfMonth..now }
        var totalExpense = 0.0
        var totalIncome = 0.0

        monthlyTxs.forEach { td ->
            when (td.transaction.type) {
                TransactionType.EXPENSE -> totalExpense += td.transaction.amount
                TransactionType.INCOME -> totalIncome += td.transaction.amount
                TransactionType.TRANSFER -> {}
            }
        }

        val netSurplus = totalIncome - totalExpense
        val expStr = LanguageHelper.formatCurrency(totalExpense, languageMode)
        val incStr = LanguageHelper.formatCurrency(totalIncome, languageMode)
        val netStr = LanguageHelper.formatCurrency(netSurplus, languageMode)

        val text = if (isBn) {
            "📅 **চলতি মাসের মোট আর্থিক হিসাব:**\n\n" +
                    "• মোট ব্যয়: **$expStr**\n" +
                    "• মোট আয়: **$incStr**\n" +
                    "• নিট উদ্বৃত্ত/সঞ্চয়: **$netStr**"
        } else {
            "📅 **Current Month's Financial Summary:**\n\n" +
                    "• Total Expenses: **$expStr**\n" +
                    "• Total Income: **$incStr**\n" +
                    "• Net Surplus/Savings: **$netStr**"
        }

        val chips = listOf(
            AssistantChip(
                label = if (isBn) "শীর্ষ খরচসমূহ" else "Top Expenses",
                actionQuery = "Show top spending categories"
            ),
            AssistantChip(
                label = if (isBn) "বাজেট পরীক্ষা" else "Check Budgets",
                actionQuery = "Show my budget status"
            )
        )

        return AssistantMessage(
            text = text,
            isUser = false,
            metricsSummary = AssistantMetrics(
                title = if (isBn) "চলতি মাস" else "Current Month",
                totalIn = totalIncome,
                totalOut = totalExpense,
                netAmount = netSurplus,
                count = monthlyTxs.size
            ),
            interactiveChips = chips
        )
    }

    private fun handleTopExpensesQuery(
        allTxs: List<TransactionWithDetails>,
        categories: List<Category>,
        isBn: Boolean,
        languageMode: LanguageMode
    ): AssistantMessage {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        val startOfMonth = cal.timeInMillis
        val now = System.currentTimeMillis()

        val expenses = allTxs.filter {
            it.transaction.type == TransactionType.EXPENSE && it.transaction.dateEpochMs in startOfMonth..now
        }

        if (expenses.isEmpty()) {
            val msg = if (isBn) "চলতি মাসে এখনও কোনো ব্যয়ের রেকর্ড নেই।" else "No expenses recorded for the current month yet."
            return AssistantMessage(text = msg, isUser = false)
        }

        val categoryMap = categories.associateBy { it.id }
        val spendByCategory = mutableMapOf<String, Double>()

        expenses.forEach { td ->
            val cat = td.category ?: td.transaction.categoryId?.let { categoryMap[it] }
            val catName = cat?.localizedName(languageMode) ?: (if (isBn) "অন্যান্য" else "Uncategorized")
            spendByCategory[catName] = (spendByCategory[catName] ?: 0.0) + td.transaction.amount
        }

        val sorted = spendByCategory.entries.sortedByDescending { it.value }.take(5)
        val totalExp = expenses.sumOf { it.transaction.amount }

        val sb = StringBuilder()
        if (isBn) {
            sb.append("🏆 **চলতি মাসের শীর্ষ খরচের খাতসমূহ (মোট: ${LanguageHelper.formatCurrency(totalExp, languageMode)}):**\n\n")
        } else {
            sb.append("🏆 **Top Expense Categories This Month (Total: ${LanguageHelper.formatCurrency(totalExp, languageMode)}):**\n\n")
        }

        sorted.forEachIndexed { idx, entry ->
            val pct = if (totalExp > 0) (entry.value / totalExp * 100).toInt() else 0
            val amtStr = LanguageHelper.formatCurrency(entry.value, languageMode)
            sb.append("${idx + 1}. **${entry.key}**: $amtStr ($pct%)\n")
        }

        return AssistantMessage(
            text = sb.toString().trim(),
            isUser = false,
            interactiveChips = listOf(
                AssistantChip(
                    label = if (isBn) "বাজেটের অবস্থা" else "Budget Status",
                    actionQuery = "Show my budget status"
                )
            )
        )
    }

    private fun handleBudgetStatusQuery(
        budgets: List<MonthlyBudget>,
        transactions: List<TransactionWithDetails>,
        categories: List<Category>,
        isBn: Boolean,
        languageMode: LanguageMode
    ): AssistantMessage {
        val cal = Calendar.getInstance()
        val currentYear = cal.get(Calendar.YEAR)
        val currentMonth = cal.get(Calendar.MONTH) + 1 // 1-based

        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        val startOfMonth = cal.timeInMillis

        // Filter budgets for current month
        val currentMonthBudgets = budgets.filter {
            it.isEnabled && it.budgetedAmount > 0 && it.year == currentYear && it.month == currentMonth
        }

        if (currentMonthBudgets.isEmpty()) {
            val emptyMsg = if (isBn) {
                "চলতি মাসে কোনো বাজেট সক্রিয় নেই। আপনি বাজেট ট্যাব থেকে নতুন বাজেট যোগ করতে পারেন।"
            } else {
                "No active budgets are configured for this month. You can set monthly budgets from the Budgets tab."
            }
            return AssistantMessage(text = emptyMsg, isUser = false)
        }

        val categoryMap = categories.associateBy { it.id }
        val expensesThisMonth = transactions.filter {
            it.transaction.type == TransactionType.EXPENSE && it.transaction.dateEpochMs >= startOfMonth
        }

        val categorySpending = mutableMapOf<Long, Double>()
        expensesThisMonth.forEach { td ->
            val catId = td.category?.id ?: td.transaction.categoryId
            if (catId != null) {
                categorySpending[catId] = (categorySpending[catId] ?: 0.0) + td.transaction.amount
            }
        }

        val overBudget = mutableListOf<String>()
        val nearBudget = mutableListOf<String>()

        currentMonthBudgets.forEach { b ->
            val cat = categoryMap[b.itemId]
            val catName = cat?.localizedName(languageMode) ?: (if (isBn) "ক্যাটাগরি #${b.itemId}" else "Category #${b.itemId}")
            val spent = categorySpending[b.itemId] ?: 0.0
            val limit = b.budgetedAmount

            val spentStr = LanguageHelper.formatCurrency(spent, languageMode)
            val limitStr = LanguageHelper.formatCurrency(limit, languageMode)

            if (spent > limit) {
                overBudget.add("• $catName: $spentStr / $limitStr")
            } else if (limit > 0 && (spent / limit) >= 0.85) {
                val pct = (spent / limit * 100).toInt()
                nearBudget.add("• $catName: $spentStr / $limitStr ($pct%)")
            }
        }

        val sb = StringBuilder()
        if (isBn) {
            sb.append("🎯 **চলতি মাসের বাজেট পর্যালোচনা:**\n\n")
            sb.append("• মোট সক্রিয় বাজেট: **${currentMonthBudgets.size}টি**\n")
            if (overBudget.isNotEmpty()) {
                sb.append("\n⚠️ **সীমা অতিক্রমকারী বাজেট (${overBudget.size}টি):**\n")
                overBudget.forEach { sb.append("  $it (অতিরিক্ত)\n") }
            } else {
                sb.append("✅ কোনো বাজেট সীমা অতিক্রম করেনি।\n")
            }

            if (nearBudget.isNotEmpty()) {
                sb.append("\n⚠️ **সতর্কতামূলক বাজেট (৮৫%+ ব্যবহৃত):**\n")
                nearBudget.forEach { sb.append("  $it\n") }
            }
        } else {
            sb.append("🎯 **Current Month Budget Status:**\n\n")
            sb.append("• Total Active Budgets: **${currentMonthBudgets.size}**\n")
            if (overBudget.isNotEmpty()) {
                sb.append("\n⚠️ **Over Budget (${overBudget.size}):**\n")
                overBudget.forEach { sb.append("  $it (Exceeded)\n") }
            } else {
                sb.append("✅ Great job! None of your active budgets are exceeded.\n")
            }

            if (nearBudget.isNotEmpty()) {
                sb.append("\n⚠️ **Near Limit Warning (85%+ spent):**\n")
                nearBudget.forEach { sb.append("  $it\n") }
            }
        }

        return AssistantMessage(text = sb.toString().trim(), isUser = false)
    }

    private fun handleCategorySpendingQuery(
        category: Category,
        days: Int,
        allTxs: List<TransactionWithDetails>,
        isBn: Boolean,
        languageMode: LanguageMode
    ): AssistantMessage {
        val now = System.currentTimeMillis()
        val startMs = now - (days.toLong() * 24L * 60L * 60L * 1000L)
        val catName = category.localizedName(languageMode)

        val matched = allTxs.filter { td ->
            val matchesCategory = td.transaction.categoryId == category.id || td.category?.id == category.id
            matchesCategory && td.transaction.dateEpochMs in startMs..now
        }.sortedByDescending { it.transaction.dateEpochMs }

        val total = matched.sumOf { it.transaction.amount }
        val totalStr = LanguageHelper.formatCurrency(total, languageMode)

        if (matched.isEmpty()) {
            val emptyMsg = if (isBn) {
                "বিগত $days দিনে **$catName** খাতে কোনো লেনদেন পাওয়া যায়নি।"
            } else {
                "No transactions found for category **$catName** in the past $days days."
            }
            return AssistantMessage(text = emptyMsg, isUser = false)
        }

        val text = if (isBn) {
            "বিগত $days দিনে **$catName** খাতে মোট ব্যয়: **$totalStr** (${matched.size}টি লেনদেন)"
        } else {
            "Total spending for **$catName** in the past $days days: **$totalStr** (${matched.size} transactions)"
        }

        return AssistantMessage(
            text = text,
            isUser = false,
            transactionList = matched
        )
    }

    private fun handleRecentTransactionsQuery(
        allTxs: List<TransactionWithDetails>,
        isBn: Boolean,
        languageMode: LanguageMode
    ): AssistantMessage {
        val recent = allTxs.sortedByDescending { it.transaction.dateEpochMs }.take(6)
        if (recent.isEmpty()) {
            val msg = if (isBn) "এখনও কোনো লেনদেন পাওয়া যায়নি।" else "No transactions recorded yet."
            return AssistantMessage(text = msg, isUser = false)
        }

        val text = if (isBn) {
            "সর্বশেষ রেকর্ডকৃত লেনদেনসমূহ:"
        } else {
            "Here are your latest recorded transactions:"
        }

        return AssistantMessage(
            text = text,
            isUser = false,
            transactionList = recent
        )
    }
}
