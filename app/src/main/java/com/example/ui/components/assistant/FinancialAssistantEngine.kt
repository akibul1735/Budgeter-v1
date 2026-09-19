package com.example.ui.components.assistant

import com.example.data.model.Account
import com.example.data.model.AccountType
import com.example.data.model.Category
import com.example.data.model.LanguageMode
import com.example.data.model.MonthlyBudget
import com.example.data.model.TransactionType
import com.example.data.model.TransactionWithDetails
import com.example.data.repository.AccountWithBalance
import com.example.data.repository.FinancialOverview
import com.example.util.DateUtils
import com.example.util.LanguageHelper
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
    val metricsSummary: AssistantMetrics? = null,
    val accountCard: AssistantAccountCard? = null,
    val budgetCard: AssistantBudgetCard? = null
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

data class AssistantAccountCard(
    val accountName: String,
    val accountType: String,
    val currentBalance: Double,
    val totalIn: Double,
    val totalOut: Double,
    val transactionCount: Int,
    val lastActiveDate: String? = null
)

data class AssistantBudgetCard(
    val totalActiveBudgets: Int,
    val totalAllocated: Double,
    val totalSpent: Double,
    val nearLimitCount: Int,
    val exceededCount: Int,
    val items: List<AssistantBudgetItem> = emptyList()
)

data class AssistantBudgetItem(
    val categoryName: String,
    val spent: Double,
    val limit: Double,
    val percentage: Double
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
        val cleanQuery = query
            .replace("\"", "")
            .replace("'", "")
            .replace("“", "")
            .replace("”", "")
            .trim()
        val lower = cleanQuery.lowercase(Locale.ROOT)
        val isBn = languageMode == LanguageMode.BANGLA

        // 1. First priority: check if query specifies an actual Account (e.g. "RM Others", "Rocket", "Cash", etc.)
        val matchedAccount = findMatchingAccount(lower, allAccounts)
        if (matchedAccount != null) {
            val specifiedDays = extractDays(lower)
            return handleAccountOverviewOrActivity(
                account = matchedAccount,
                specifiedDays = specifiedDays,
                accountsWithBalances = accountsWithBalances,
                allTxs = transactions,
                isBn = isBn,
                languageMode = languageMode
            )
        }

        // 2. Interactive clarification when query mentions generic accounts without specifying which one
        if (isGenericAccountQuery(lower)) {
            val days = extractDays(lower) ?: 7
            val periodLabel = if (days == 7) {
                if (isBn) "বিগত ৭ দিনের" else "the past 7 days"
            } else {
                if (isBn) "বিগত $days দিনের" else "the past $days days"
            }

            val questionText = if (isBn) {
                "আপনি $periodLabel জন্য কোন অ্যাকাউন্টটি দেখতে চান? নিচে থেকে পছন্দ করুন:"
            } else {
                "Which account would you like to check for $periodLabel? Please select below:"
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

        // 3. Query for "all accounts in past X days / period"
        if (lower.contains("all accounts") || (lower.contains("all") && lower.contains("account"))) {
            val days = extractDays(lower) ?: 7
            return handleAllAccountsPeriodTransactions(days, transactions, allAccounts, isBn, languageMode)
        }

        // 4. RM Manager / Debt / Loan / Liability summary
        if (lower.contains("rm manager") || lower.contains("who owes me") || lower.contains("who do i owe") ||
            lower.contains("debt summary") || lower.contains("দেনা পাওনা") || lower.contains("কার কাছে কত পাবো") ||
            lower.contains("ঋণ কত") || lower.contains("ধার কত") || lower.contains("rm summary")
        ) {
            return handleRmSummaryQuery(allAccounts, accountsWithBalances, transactions, isBn, languageMode)
        }

        // 5. Personal Finance Advice & Savings Tips (50/30/20 rule, Emergency fund)
        if (lower.contains("saving tip") || lower.contains("save money") || lower.contains("50 30 20") ||
            lower.contains("50/30/20") || lower.contains("emergency fund") || lower.contains("সঞ্চয়") ||
            lower.contains("টাকা জমানো") || lower.contains("খরচ কমানো") || lower.contains("পরামর্শ")
        ) {
            return handleFinancialAdviceQuery(overview, lower, isBn, languageMode)
        }

        // 6. Net Worth & Balance queries
        if (lower.contains("net worth") || lower.contains("networth") || lower.contains("সম্পদ") ||
            lower.contains("total balance") || lower.contains("ব্যালেন্স") || lower.contains("balance") ||
            lower.contains("how much money") || lower.contains("টাকা আছে")
        ) {
            return handleNetWorthQuery(overview, accountsWithBalances, isBn, languageMode)
        }

        // 7. Monthly Expenses / Spending / Cash Flow
        if (lower.contains("spend this month") || lower.contains("monthly expense") || lower.contains("monthly spending") ||
            lower.contains("মাসিক খরচ") || lower.contains("এই মাসের খরচ") || lower.contains("cash flow") || lower.contains("ক্যাশ ফ্লো")
        ) {
            return handleMonthlySpendingQuery(transactions, isBn, languageMode)
        }

        // 8. Top Expenses / Categories
        if (lower.contains("top expense") || lower.contains("top spending") || lower.contains("top category") ||
            lower.contains("সর্বোচ্চ খরচ") || lower.contains("কোথায় খরচ") || lower.contains("where did my money go")
        ) {
            return handleTopExpensesQuery(transactions, allCategories, isBn, languageMode)
        }

        // 9. Budget Status & Limits
        if (lower.contains("budget") || lower.contains("বাজেট") || lower.contains("over budget") || lower.contains("limit")) {
            return handleBudgetStatusQuery(budgets, transactions, allCategories, isBn, languageMode)
        }

        // 10. Specific Category Query (e.g., "Food", "Shopping", "Transport", "Bills")
        val matchedCategory = findMatchingCategory(lower, allCategories)
        if (matchedCategory != null) {
            val days = extractDays(lower) ?: 30
            return handleCategorySpendingQuery(matchedCategory, days, transactions, isBn, languageMode)
        }

        // 11. Recent transactions query
        if (lower.contains("recent") || lower.contains("last transaction") || lower.contains("সাম্প্রতিক লেনদেন") || lower.contains("latest")) {
            return handleRecentTransactionsQuery(transactions, isBn, languageMode)
        }

        // 12. App Features Guide
        if (lower.contains("how to backup") || lower.contains("how to restore") || lower.contains("কীভাবে ব্যাকআপ") ||
            lower.contains("how to use rm") || lower.contains("how to add budget")
        ) {
            return handleAppGuideQuery(lower, isBn, languageMode)
        }

        // 13. Default / Fallback with smart suggestions
        val fallbackText = if (isBn) {
            "আমি আপনার অন-ডিভাইস অফলাইন ফাইন্যান্সিয়াল সহকারী। আপনি নির্দিষ্ট অ্যাকাউন্ট (যেমন RM Others, Rocket, Cash), বাজেট, খরচ, সঞ্চয় বা সম্পদ সম্পর্কে প্রশ্ন করতে পারেন। নিচে কয়েকটি উদাহরণ দেখুন:"
        } else {
            "I am your offline Financial Assistant. You can ask about accounts (e.g. RM Others, Rocket, Cash), budget health, spending categories, or saving strategies. Here are some questions to try:"
        }

        val defaultChips = listOf(
            AssistantChip(
                label = if (isBn) "🎯 বাজেটের অবস্থা" else "🎯 Budget Status",
                actionQuery = "Show my budget status"
            ),
            AssistantChip(
                label = if (isBn) "🏦 RM Others অ্যাকাউন্ট" else "🏦 RM Others Account",
                actionQuery = "tell me about RM Others"
            ),
            AssistantChip(
                label = if (isBn) "💰 মোট সম্পদ" else "💰 Total Net Worth",
                actionQuery = "What is my net worth"
            ),
            AssistantChip(
                label = if (isBn) "👥 আরএম ঋণ ও দেনা-পাওনা" else "👥 RM Debts & Loans",
                actionQuery = "Tell me about RM Manager"
            ),
            AssistantChip(
                label = if (isBn) "📊 চলতি মাসের খরচ" else "📊 This Month Expenses",
                actionQuery = "How much did I spend this month?"
            ),
            AssistantChip(
                label = if (isBn) "💡 সঞ্চয়ের পরামর্শ (৫০/৩০/২০)" else "💡 Savings Tips (50/30/20)",
                actionQuery = "Give me savings tips"
            )
        )

        return AssistantMessage(
            text = fallbackText,
            isUser = false,
            interactiveChips = defaultChips
        )
    }

    private fun isGenericAccountQuery(query: String): Boolean {
        val hasAccountWord = query.contains("account") || query.contains("অ্যাকাউন্ট") || query.contains("একাউন্ট")
        val hasTransactionWord = query.contains("transaction") || query.contains("লেনদেন") || query.contains("history")

        val specificKeywords = listOf(
            "rocket", "bkash", "nagad", "cash", "bank", "dbbl", "city bank", "brac", "upay",
            "rm others", "rm other", "rm", "আরএম", "all accounts", "all account", "সব অ্যাকাউন্ট"
        )
        val mentionsSpecific = specificKeywords.any { query.contains(it) }

        return (hasAccountWord || hasTransactionWord) && !mentionsSpecific &&
                (query.contains("any") || query.contains("কোনো") || query.contains("which") || query.contains("is there"))
    }

    private fun extractDays(query: String): Int? {
        val regex = Regex("""(\d+)\s*(day|days|দিন)""")
        val match = regex.find(query)
        if (match != null) {
            return match.groupValues[1].toIntOrNull()
        }
        if (query.contains("week") || query.contains("সপ্তাহ")) return 7
        if (query.contains("month") || query.contains("মাস")) return 30
        if (query.contains("year") || query.contains("বছর")) return 365
        return null
    }

    private fun findMatchingAccount(query: String, accounts: List<Account>): Account? {
        val cleanQuery = query.replace("\"", "").replace("'", "").lowercase(Locale.ROOT).trim()

        // 1. Direct exact name match
        val exact = accounts.firstOrNull {
            it.nameEn.equals(cleanQuery, ignoreCase = true) ||
            it.nameBn.equals(cleanQuery, ignoreCase = true)
        }
        if (exact != null) return exact

        // 2. Exact substring match for accounts whose full name is contained in the query.
        // CRITICAL: Sort by name length descending so "RM Others" (length 9) matches BEFORE "Others" (length 6) or "Other"
        val fullContainsMatches = accounts.filter { acc ->
            val en = acc.nameEn.lowercase(Locale.ROOT).trim()
            val bn = acc.nameBn.lowercase(Locale.ROOT).trim()
            (en.length >= 2 && cleanQuery.contains(en)) ||
            (bn.length >= 2 && cleanQuery.contains(bn))
        }.sortedByDescending { maxOf(it.nameEn.length, it.nameBn.length) }

        if (fullContainsMatches.isNotEmpty()) {
            return fullContainsMatches.first()
        }

        // 3. Special normalized alias rules for key accounts
        if (cleanQuery.contains("rm other") || cleanQuery.contains("rm others") || cleanQuery.contains("আরএম অন্যান্য")) {
            val rmOtherAcc = accounts.firstOrNull {
                it.nameEn.contains("RM Others", ignoreCase = true) ||
                it.nameBn.contains("আরএম অন্যান্য", ignoreCase = true) ||
                (it.nameEn.contains("RM", ignoreCase = true) && it.nameEn.contains("Other", ignoreCase = true))
            }
            if (rmOtherAcc != null) return rmOtherAcc
        }

        if (cleanQuery.contains("rocket") || cleanQuery.contains("রকেট")) {
            return accounts.firstOrNull { it.nameEn.contains("Rocket", ignoreCase = true) || it.nameBn.contains("রকেট") }
        }
        if (cleanQuery.contains("bkash") || cleanQuery.contains("বিকাশ")) {
            return accounts.firstOrNull { it.nameEn.contains("bKash", ignoreCase = true) || it.nameBn.contains("বিকাশ") }
        }
        if (cleanQuery.contains("nagad") || cleanQuery.contains("নগদ")) {
            return accounts.firstOrNull { it.nameEn.contains("Nagad", ignoreCase = true) || it.nameBn.contains("নগদ") }
        }
        if (cleanQuery.contains("cash") || cleanQuery.contains("ক্যাশ")) {
            return accounts.firstOrNull { it.nameEn.contains("Cash", ignoreCase = true) || it.nameBn.contains("ক্যাশ") }
        }

        // 4. Token-level matching (all words in account name are contained in query)
        val tokenMatches = accounts.filter { acc ->
            val words = acc.nameEn.lowercase(Locale.ROOT).split(" ").filter { it.length > 2 }
            words.isNotEmpty() && words.all { cleanQuery.contains(it) }
        }.sortedByDescending { it.nameEn.length }

        if (tokenMatches.isNotEmpty()) {
            return tokenMatches.first()
        }

        return null
    }

    private fun findMatchingCategory(query: String, categories: List<Category>): Category? {
        val cleanQuery = query.replace("\"", "").replace("'", "").lowercase(Locale.ROOT).trim()
        val direct = categories.filter { cat ->
            val enName = cat.nameEn.lowercase(Locale.ROOT).trim()
            val bnName = cat.nameBn.lowercase(Locale.ROOT).trim()
            (enName.isNotBlank() && cleanQuery.contains(enName)) ||
            (bnName.isNotBlank() && cleanQuery.contains(bnName))
        }.sortedByDescending { maxOf(it.nameEn.length, it.nameBn.length) }

        return direct.firstOrNull()
    }

    private fun handleAccountOverviewOrActivity(
        account: Account,
        specifiedDays: Int?,
        accountsWithBalances: List<AccountWithBalance>,
        allTxs: List<TransactionWithDetails>,
        isBn: Boolean,
        languageMode: LanguageMode
    ): AssistantMessage {
        val accName = account.localizedName(languageMode)
        val accWithBalance = accountsWithBalances.find { it.account.id == account.id }
        val currentBalance = accWithBalance?.currentBalance ?: 0.0

        // Find all transactions linked to this account
        val allAccountTxs = allTxs.filter { td ->
            val tx = td.transaction
            tx.debitAccountId == account.id || tx.creditAccountId == account.id
        }.sortedByDescending { it.transaction.dateEpochMs }

        var totalIn = 0.0
        var totalOut = 0.0
        allAccountTxs.forEach { td ->
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

        val lastTx = allAccountTxs.firstOrNull()
        val lastDateStr = lastTx?.let { DateUtils.formatDate(it.transaction.dateEpochMs, languageMode) }

        val accountCard = AssistantAccountCard(
            accountName = accName,
            accountType = account.type.name,
            currentBalance = currentBalance,
            totalIn = totalIn,
            totalOut = totalOut,
            transactionCount = allAccountTxs.size,
            lastActiveDate = lastDateStr
        )

        // Case 1: When user did NOT specify days (e.g. "tell me about rm others", "what is rm others", "rocket status")
        if (specifiedDays == null) {
            val balanceStr = LanguageHelper.formatCurrency(currentBalance, languageMode)
            val inStr = LanguageHelper.formatCurrency(totalIn, languageMode)
            val outStr = LanguageHelper.formatCurrency(totalOut, languageMode)

            val text = if (isBn) {
                val lastInfo = if (lastDateStr != null) "\n• সর্বশেষ লেনদেনের তারিখ: **$lastDateStr**" else ""
                "🏦 **$accName অ্যাকাউন্টের বিবরণ:**\n\n• অ্যাকাউন্টের ধরন: **${account.type.name}**\n• বর্তমান ব্যালেন্স: **$balanceStr**\n• সর্বমোট লেনদেন: **${allAccountTxs.size}টি**\n• মোট জমা / আগমন: **$inStr**\n• মোট খরচ / বহির্গমন: **$outStr**$lastInfo"
            } else {
                val lastInfo = if (lastDateStr != null) "\n• Last Activity Date: **$lastDateStr**" else ""
                "🏦 **$accName Account Overview:**\n\n• Account Type: **${account.type.name}**\n• Current Balance: **$balanceStr**\n• Total Recorded Transactions: **${allAccountTxs.size}**\n• Total Inflow: **$inStr**\n• Total Outflow: **$outStr**$lastInfo"
            }

            val chips = listOf(
                AssistantChip(
                    label = if (isBn) "বিগত ৭ দিনের লেনদেন" else "Past 7 days transactions",
                    actionQuery = "transactions for $accName in past 7 days"
                ),
                AssistantChip(
                    label = if (isBn) "বিগত ৩০ দিনের লেনদেন" else "Past 30 days transactions",
                    actionQuery = "transactions for $accName in past 30 days"
                ),
                AssistantChip(
                    label = if (isBn) "বাজেটের অবস্থা" else "Budget Status",
                    actionQuery = "Show my budget status"
                )
            )

            return AssistantMessage(
                text = text,
                isUser = false,
                interactiveChips = chips,
                transactionList = allAccountTxs.take(6),
                accountCard = accountCard
            )
        }

        // Case 2: User specifically requested transactions in past N days
        val now = System.currentTimeMillis()
        val startMs = now - (specifiedDays.toLong() * 24L * 60L * 60L * 1000L)
        val filteredTxs = allAccountTxs.filter { it.transaction.dateEpochMs in startMs..now }

        if (filteredTxs.isEmpty()) {
            val balanceStr = LanguageHelper.formatCurrency(currentBalance, languageMode)
            val text = if (isBn) {
                "বিগত $specifiedDays দিনে **$accName** অ্যাকাউন্টে কোনো লেনদেন পাওয়া যায়নি।\n\n• বর্তমান ব্যালেন্স: **$balanceStr**\n• সর্বমোট রেকর্ডকৃত লেনদেন: **${allAccountTxs.size}টি**"
            } else {
                "No transactions were found for your **$accName** account in the past $specifiedDays days.\n\n• Current Balance: **$balanceStr**\n• All-time Recorded Transactions: **${allAccountTxs.size}**"
            }

            val chips = listOf(
                AssistantChip(
                    label = if (isBn) "$accName ওভারভিউ" else "$accName Overview",
                    actionQuery = "tell me about $accName"
                ),
                AssistantChip(
                    label = if (isBn) "সকল অ্যাকাউন্ট" else "All Accounts",
                    actionQuery = "What is my net worth"
                )
            )

            return AssistantMessage(
                text = text,
                isUser = false,
                transactionList = allAccountTxs.take(4),
                accountCard = accountCard,
                interactiveChips = chips
            )
        }

        // Case 3: Filtered transactions found
        var filteredIn = 0.0
        var filteredOut = 0.0
        filteredTxs.forEach { td ->
            val tx = td.transaction
            when (tx.type) {
                TransactionType.INCOME -> if (tx.debitAccountId == account.id) filteredIn += tx.amount
                TransactionType.EXPENSE -> if (tx.creditAccountId == account.id) filteredOut += tx.amount
                TransactionType.TRANSFER -> {
                    if (tx.debitAccountId == account.id) filteredIn += tx.amount
                    if (tx.creditAccountId == account.id) filteredOut += tx.amount
                }
            }
        }

        val text = if (isBn) {
            "হ্যাঁ, বিগত $specifiedDays দিনে **$accName** অ্যাকাউন্টে মোট **${filteredTxs.size}টি** লেনদেন পাওয়া গেছে:"
        } else {
            "Found **${filteredTxs.size}** transaction${if (filteredTxs.size > 1) "s" else ""} for **$accName** in the past $specifiedDays days:"
        }

        val metrics = AssistantMetrics(
            title = accName,
            totalIn = filteredIn,
            totalOut = filteredOut,
            netAmount = filteredIn - filteredOut,
            count = filteredTxs.size
        )

        return AssistantMessage(
            text = text,
            isUser = false,
            transactionList = filteredTxs,
            metricsSummary = metrics,
            accountCard = accountCard
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

        if (matchedTxs.isEmpty()) {
            val emptyMsg = if (isBn) {
                "বিগত $days দিনে আপনার কোনো অ্যাকাউন্টে লেনদেন রেকর্ড হয়নি।"
            } else {
                "No transactions were recorded across all accounts in the past $days days."
            }
            return AssistantMessage(text = emptyMsg, isUser = false)
        }

        val totalIncome = matchedTxs.filter { it.transaction.type == TransactionType.INCOME }.sumOf { it.transaction.amount }
        val totalExpense = matchedTxs.filter { it.transaction.type == TransactionType.EXPENSE }.sumOf { it.transaction.amount }

        val text = if (isBn) {
            "বিগত $days দিনে সকল অ্যাকাউন্ট মিলিয়ে মোট **${matchedTxs.size}টি** লেনদেন হয়েছে:"
        } else {
            "Found **${matchedTxs.size}** transaction${if (matchedTxs.size > 1) "s" else ""} across all accounts in the past $days days:"
        }

        val metrics = AssistantMetrics(
            title = if (isBn) "সকল অ্যাকাউন্ট ($days দিন)" else "All Accounts ($days days)",
            totalIn = totalIncome,
            totalOut = totalExpense,
            netAmount = totalIncome - totalExpense,
            count = matchedTxs.size
        )

        return AssistantMessage(
            text = text,
            isUser = false,
            transactionList = matchedTxs.take(15),
            metricsSummary = metrics
        )
    }

    private fun handleRmSummaryQuery(
        accounts: List<Account>,
        accountsWithBalances: List<AccountWithBalance>,
        transactions: List<TransactionWithDetails>,
        isBn: Boolean,
        languageMode: LanguageMode
    ): AssistantMessage {
        val rmAccounts = accounts.filter {
            it.nameEn.contains("RM", ignoreCase = true) ||
            it.nameBn.contains("আরএম") ||
            it.type == AccountType.LIABILITY
        }

        val rmBalances = accountsWithBalances.filter { ab ->
            rmAccounts.any { it.id == ab.account.id }
        }

        val totalDebt = rmBalances.filter { it.currentBalance > 0 }.sumOf { it.currentBalance }
        val totalReceivable = rmBalances.filter { it.currentBalance < 0 }.sumOf { -it.currentBalance }

        val sb = StringBuilder()
        if (isBn) {
            sb.append("👥 **আরএম ম্যানেজার ও দেনা-পাওনা পর্যালোচনা:**\n\n")
            sb.append("• মোট দায় / ঋণ (Payables): **${LanguageHelper.formatCurrency(totalDebt, languageMode)}**\n")
            sb.append("• মোট পাওনা (Receivables): **${LanguageHelper.formatCurrency(totalReceivable, languageMode)}**\n")
            sb.append("• নিট জের: **${LanguageHelper.formatCurrency(totalDebt - totalReceivable, languageMode)}**\n\n")
            sb.append("📋 **আরএম অ্যাকাউন্ট তালিকা:**\n")
            rmBalances.take(5).forEach { ab ->
                val name = ab.account.localizedName(languageMode)
                val bal = LanguageHelper.formatCurrency(ab.currentBalance, languageMode)
                sb.append("• $name: **$bal**\n")
            }
        } else {
            sb.append("👥 **RM Manager & Debt Summary:**\n\n")
            sb.append("• Total Borrowed / Payables: **${LanguageHelper.formatCurrency(totalDebt, languageMode)}**\n")
            sb.append("• Total Lent / Receivables: **${LanguageHelper.formatCurrency(totalReceivable, languageMode)}**\n")
            sb.append("• Net RM Balance: **${LanguageHelper.formatCurrency(totalDebt - totalReceivable, languageMode)}**\n\n")
            sb.append("📋 **Active RM Accounts:**\n")
            rmBalances.take(5).forEach { ab ->
                val name = ab.account.localizedName(languageMode)
                val bal = LanguageHelper.formatCurrency(ab.currentBalance, languageMode)
                sb.append("• $name: **$bal**\n")
            }
        }

        val chips = listOf(
            AssistantChip(
                label = if (isBn) "RM Others অ্যাকাউন্ট" else "RM Others Account",
                actionQuery = "tell me about RM Others"
            ),
            AssistantChip(
                label = if (isBn) "বাজেটের অবস্থা" else "Budget Status",
                actionQuery = "Show my budget status"
            ),
            AssistantChip(
                label = if (isBn) "মোট সম্পদ" else "Total Net Worth",
                actionQuery = "What is my net worth"
            )
        )

        return AssistantMessage(
            text = sb.toString().trim(),
            isUser = false,
            interactiveChips = chips
        )
    }

    private fun handleFinancialAdviceQuery(
        overview: FinancialOverview,
        query: String,
        isBn: Boolean,
        languageMode: LanguageMode
    ): AssistantMessage {
        val income = overview.monthlyIncome
        val expenses = overview.monthlyExpense
        val savings = income - expenses
        val savingsRate = if (income > 0) (savings / income * 100).toInt() else 0

        val targetNeeds = income * 0.50
        val targetWants = income * 0.30
        val targetSavings = income * 0.20

        val sb = StringBuilder()
        if (isBn) {
            sb.append("💡 **ব্যক্তিগত অর্থায়ন ও ৫০/৩০/২০ সঞ্চয় গাইড:**\n\n")
            if (income > 0) {
                sb.append("আপনার চলতি মাসের আয়ের ভিত্তিতে আদর্শ বণ্টন:\n")
                sb.append("• ৫০% অপরিহার্য প্রয়োজন (Needs): **${LanguageHelper.formatCurrency(targetNeeds, languageMode)}**\n")
                sb.append("• ৩০% ইচ্ছা ও বিনোদন (Wants): **${LanguageHelper.formatCurrency(targetWants, languageMode)}**\n")
                sb.append("• ২০% সঞ্চয় ও ঋণ পরিশোধ (Savings): **${LanguageHelper.formatCurrency(targetSavings, languageMode)}**\n\n")
                sb.append("📊 **আপনার বর্তমান অবস্থা:**\n")
                sb.append("• চলতি মাসের সঞ্চয়ের হার: **$savingsRate%**\n")
                if (savingsRate >= 20) {
                    sb.append("✅ চমৎকার! আপনি আদর্শ ২০% সঞ্চয় লক্ষ্যমাত্রা পূরণ করছেন।\n")
                } else {
                    sb.append("⚠️ আপনার সঞ্চয়ের হার ২০% এর নিচে। কিছু খরচ কমিয়ে সঞ্চয় বাড়ানোর চেষ্টা করুন।\n")
                }
            } else {
                sb.append("• **৫০/৩০/২০ নিয়ম:** আয়ের ৫০% মৌলিক প্রয়োজন, ৩০% ইচ্ছা, এবং ২০% বাধ্যতামূলক সঞ্চয়ে রাখুন।\n")
                sb.append("• **জরুরি তহবিল:** অপ্রত্যাশিত খরচের জন্য অন্তত ৩-৬ মাসের মৌলিক খরচের টাকা আলাদা রাখুন।\n")
            }
        } else {
            sb.append("💡 **Personal Finance & 50/30/20 Savings Strategy:**\n\n")
            if (income > 0) {
                sb.append("Based on your recorded monthly income, here is your ideal budget breakdown:\n")
                sb.append("• 50% Essential Needs: **${LanguageHelper.formatCurrency(targetNeeds, languageMode)}**\n")
                sb.append("• 30% Wants & Lifestyle: **${LanguageHelper.formatCurrency(targetWants, languageMode)}**\n")
                sb.append("• 20% Savings & Debt Payoff: **${LanguageHelper.formatCurrency(targetSavings, languageMode)}**\n\n")
                sb.append("📊 **Your Current Performance:**\n")
                sb.append("• Current Savings Rate: **$savingsRate%**\n")
                if (savingsRate >= 20) {
                    sb.append("✅ Great job! You are meeting the 20% savings milestone.\n")
                } else {
                    sb.append("⚠️ Your savings rate is under 20%. Consider curbing discretionary expenses.\n")
                }
            } else {
                sb.append("• **50/30/20 Rule:** Allocate 50% to Needs, 30% to Wants, and 20% to Savings.\n")
                sb.append("• **Emergency Fund:** Maintain 3-6 months of essential living expenses in liquid accounts.\n")
            }
        }

        val chips = listOf(
            AssistantChip(
                label = if (isBn) "শীর্ষ খরচসমূহ" else "Top Expenses",
                actionQuery = "Show top spending categories"
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

    private fun handleAppGuideQuery(
        query: String,
        isBn: Boolean,
        languageMode: LanguageMode
    ): AssistantMessage {
        val text = if (query.contains("backup") || query.contains("ব্যাকআপ")) {
            if (isBn) {
                "💾 **ডাটা ব্যাকআপ নির্দেশিকা:**\n\n1. ড্রয়ার মেনু খুলে **Settings** এ যান।\n2. **Backup & Restore** অপশন সিলেক্ট করুন।\n3. **Create Backup Now** বাটনে ট্যাপ করলে আপনার সম্পূর্ণ হিসাবের একটি নিরাপদ এনক্রিপ্টেড ব্যাকআপ তৈরি হবে।"
            } else {
                "💾 **Data Backup Guide:**\n\n1. Open the navigation drawer and tap **Settings**.\n2. Tap **Backup & Restore**.\n3. Tap **Create Backup Now** to save a safe, offline snapshot of all your accounts and transactions."
            }
        } else {
            if (isBn) {
                "📱 **বাজেটার সহায়িকা:**\n\n• **লেনদেন যোগ:** নিচের '+' ফ্লোটিং বাটনে ট্যাপ করুন।\n• **বাজেট তৈরি:** ড্রয়ার থেকে Budget Maker এ যান।\n• **আরএম খাতা:** ঋণ ও ধারের নির্ভুল হিসাব রাখতে RM Manager ব্যবহার করুন।"
            } else {
                "📱 **Budgeter Quick Tips:**\n\n• **Add Transaction:** Tap the '+' button at bottom-right.\n• **Create Budgets:** Access Budget Maker from the drawer.\n• **RM Manager:** Use RM Manager to track loans, liabilities, and debts."
            }
        }

        return AssistantMessage(text = text, isUser = false)
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

        val topAccounts = accountsWithBalances
            .filter { it.account.isActive }
            .sortedByDescending { it.currentBalance }
            .take(5)

        val sb = StringBuilder()
        if (isBn) {
            sb.append("💰 **আপনার বর্তমান মোট আর্থিক স্থিতি:**\n\n")
            sb.append("• নিট সম্পদ (Net Worth): **$netWorthStr**\n")
            sb.append("• মোট পরিসম্পদ (Assets): **$assetsStr**\n")
            sb.append("• মোট দায় / ঋণ (Liabilities): **$liabilitiesStr**\n\n")
            sb.append("🏦 **প্রধান অ্যাকাউন্টসমূহের ব্যালেন্স:**\n")
            topAccounts.forEach { ab ->
                val accName = ab.account.localizedName(languageMode)
                val balStr = LanguageHelper.formatCurrency(ab.currentBalance, languageMode)
                sb.append("• $accName: **$balStr**\n")
            }
        } else {
            sb.append("💰 **Your Financial Standing:**\n\n")
            sb.append("• Net Worth: **$netWorthStr**\n")
            sb.append("• Total Assets: **$assetsStr**\n")
            sb.append("• Total Liabilities: **$liabilitiesStr**\n\n")
            sb.append("🏦 **Top Account Balances:**\n")
            topAccounts.forEach { ab ->
                val accName = ab.account.localizedName(languageMode)
                val balStr = LanguageHelper.formatCurrency(ab.currentBalance, languageMode)
                sb.append("• $accName: **$balStr**\n")
            }
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
            metricsSummary = AssistantMetrics(
                title = if (isBn) "আর্থিক বিবরণী" else "Balance Sheet",
                totalIn = overview.totalAssets,
                totalOut = overview.totalLiabilities,
                netAmount = overview.netWorth,
                count = accountsWithBalances.size
            ),
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
        val startOfMonth = cal.timeInMillis
        val now = System.currentTimeMillis()

        val monthlyTxs = allTxs.filter { it.transaction.dateEpochMs in startOfMonth..now }

        val totalIncome = monthlyTxs.filter { it.transaction.type == TransactionType.INCOME }.sumOf { it.transaction.amount }
        val totalExpense = monthlyTxs.filter { it.transaction.type == TransactionType.EXPENSE }.sumOf { it.transaction.amount }
        val netSurplus = totalIncome - totalExpense

        val expStr = LanguageHelper.formatCurrency(totalExpense, languageMode)
        val incStr = LanguageHelper.formatCurrency(totalIncome, languageMode)
        val netStr = LanguageHelper.formatCurrency(netSurplus, languageMode)

        val text = if (isBn) {
            "📊 **চলতি মাসের খরচের খতিয়ান:**\n\n• মোট খরচ: **$expStr**\n• মোট আয়: **$incStr**\n• নিট সঞ্চয়/উদ্বৃত্ত: **$netStr**\n• মোট লেনদেন: **${monthlyTxs.size}টি**"
        } else {
            "📊 **Current Month Spending Summary:**\n\n• Total Expenses: **$expStr**\n• Total Income: **$incStr**\n• Net Surplus / Savings: **$netStr**\n• Total Transactions: **${monthlyTxs.size}**"
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
        val budgetItems = mutableListOf<AssistantBudgetItem>()

        var totalAllocated = 0.0
        var totalSpent = 0.0

        currentMonthBudgets.forEach { b ->
            val cat = categoryMap[b.itemId]
            val catName = cat?.localizedName(languageMode) ?: (if (isBn) "ক্যাটাগরি #${b.itemId}" else "Category #${b.itemId}")
            val spent = categorySpending[b.itemId] ?: 0.0
            val limit = b.budgetedAmount

            totalAllocated += limit
            totalSpent += spent

            val pct = if (limit > 0) (spent / limit * 100) else 0.0
            budgetItems.add(
                AssistantBudgetItem(
                    categoryName = catName,
                    spent = spent,
                    limit = limit,
                    percentage = pct
                )
            )

            val spentStr = LanguageHelper.formatCurrency(spent, languageMode)
            val limitStr = LanguageHelper.formatCurrency(limit, languageMode)

            if (spent > limit) {
                overBudget.add("• $catName: $spentStr / $limitStr")
            } else if (limit > 0 && (spent / limit) >= 0.85) {
                nearBudget.add("• $catName: $spentStr / $limitStr (${pct.toInt()}%)")
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

        val budgetCard = AssistantBudgetCard(
            totalActiveBudgets = currentMonthBudgets.size,
            totalAllocated = totalAllocated,
            totalSpent = totalSpent,
            nearLimitCount = nearBudget.size,
            exceededCount = overBudget.size,
            items = budgetItems.sortedByDescending { it.percentage }
        )

        return AssistantMessage(
            text = sb.toString().trim(),
            isUser = false,
            budgetCard = budgetCard
        )
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

        val matchedTxs = allTxs.filter { td ->
            val tx = td.transaction
            val isCatMatch = tx.categoryId == category.id || td.category?.id == category.id
            isCatMatch && tx.dateEpochMs in startMs..now
        }.sortedByDescending { it.transaction.dateEpochMs }

        if (matchedTxs.isEmpty()) {
            val emptyMsg = if (isBn) {
                "বিগত $days দিনে **$catName** খাতে কোনো লেনদেন রেকর্ড হয়নি।"
            } else {
                "No transactions found under **$catName** in the past $days days."
            }
            return AssistantMessage(text = emptyMsg, isUser = false)
        }

        val totalAmount = matchedTxs.sumOf { it.transaction.amount }
        val amtStr = LanguageHelper.formatCurrency(totalAmount, languageMode)

        val headerText = if (isBn) {
            "বিগত $days দিনে **$catName** খাতে মোট **$amtStr** খরচ হয়েছে (${matchedTxs.size}টি লেনদেন):"
        } else {
            "In the past $days days, you spent **$amtStr** on **$catName** across ${matchedTxs.size} transaction${if (matchedTxs.size > 1) "s" else ""}:"
        }

        val metrics = AssistantMetrics(
            title = catName,
            totalOut = totalAmount,
            count = matchedTxs.size
        )

        return AssistantMessage(
            text = headerText,
            isUser = false,
            transactionList = matchedTxs,
            metricsSummary = metrics
        )
    }

    private fun handleRecentTransactionsQuery(
        allTxs: List<TransactionWithDetails>,
        isBn: Boolean,
        languageMode: LanguageMode
    ): AssistantMessage {
        val recent = allTxs.sortedByDescending { it.transaction.dateEpochMs }.take(8)

        if (recent.isEmpty()) {
            val emptyMsg = if (isBn) "এখনও কোনো লেনদেনের রেকর্ড পাওয়া যায়নি।" else "No transactions recorded yet."
            return AssistantMessage(text = emptyMsg, isUser = false)
        }

        val text = if (isBn) {
            "আপনার সাম্প্রতিক **${recent.size}টি** লেনদেনের তালিকা:"
        } else {
            "Here are your **${recent.size}** most recent transactions:"
        }

        return AssistantMessage(
            text = text,
            isUser = false,
            transactionList = recent
        )
    }
}
