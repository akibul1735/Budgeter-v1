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
import com.example.util.AccountCalcConfig
import com.example.util.DateUtils
import com.example.util.LanguageHelper
import java.util.Calendar
import java.util.Locale
import java.util.UUID

/**
 * Action triggered by assistant card or chip to navigate to a specific page or filter in Budgeter.
 */
data class AssistantAction(
    val label: String,
    val destination: String,
    val accountId: Long? = null,
    val categoryId: Long? = null,
    val searchQuery: String? = null,
    val iconName: String? = null
)

/**
 * Visual card displaying status, spending, and limit for a specific category like Hair Cut, Food, etc.
 */
data class AssistantCategoryBudgetCard(
    val categoryName: String,
    val categoryType: String,
    val budgetLimit: Double,
    val spentAmount: Double,
    val remainingAmount: Double,
    val percentage: Double,
    val isOverBudget: Boolean,
    val hasBudget: Boolean
)

/**
 * Visual card displaying excluded accounts summary and details.
 */
data class AssistantExcludedAccountsCard(
    val totalExcludedCount: Int,
    val totalExcludedBalance: Double,
    val items: List<AssistantExcludedItem> = emptyList()
)

data class AssistantExcludedItem(
    val accountName: String,
    val accountType: String,
    val balance: Double,
    val reason: String
)

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
    val budgetCard: AssistantBudgetCard? = null,
    val categoryBudgetCard: AssistantCategoryBudgetCard? = null,
    val excludedAccountsCard: AssistantExcludedAccountsCard? = null,
    val primaryAction: AssistantAction? = null,
    val secondaryActions: List<AssistantAction> = emptyList()
)

data class AssistantChip(
    val label: String,
    val actionQuery: String = "",
    val navigationAction: AssistantAction? = null,
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
        budgets: List<MonthlyBudget>,
        accountCalcConfig: AccountCalcConfig = AccountCalcConfig()
    ): AssistantMessage {
        val cleanQuery = query
            .replace("\"", "")
            .replace("'", "")
            .replace("“", "")
            .replace("”", "")
            .trim()
        val lower = cleanQuery.lowercase(Locale.ROOT)
        val isBn = languageMode == LanguageMode.BANGLA

        // 0. Direct navigation intents (e.g. "go to budget maker", "open excluded accounts", etc.)
        val directNav = detectDirectNavigation(lower, isBn)
        if (directNav != null) {
            return directNav
        }

        // 1. Excluded accounts query (e.g. "excluded account", "which accounts are excluded", "বাদ দেওয়া একাউন্ট", etc.)
        if (isExcludedAccountsQuery(lower)) {
            return handleExcludedAccountsQuery(
                allAccounts = allAccounts,
                accountsWithBalances = accountsWithBalances,
                accountCalcConfig = accountCalcConfig,
                isBn = isBn,
                languageMode = languageMode
            )
        }

        // 2. Specific Category Budget query (e.g. "hair cut budget", "food budget", "wifi budget", "shopping budget")
        if (isCategoryBudgetQuery(cleanQuery, allCategories)) {
            return handleCategoryBudgetQuery(
                query = cleanQuery,
                lower = lower,
                categories = allCategories,
                budgets = budgets,
                transactions = transactions,
                isBn = isBn,
                languageMode = languageMode
            )
        }

        // 3. First priority: check if query specifies an actual Account (e.g. "RM Others", "Rocket", "Cash", etc.)
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

        // 4. Interactive clarification when query mentions generic accounts without specifying which one
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

        // 5. Query for "all accounts in past X days / period"
        if (lower.contains("all accounts") || (lower.contains("all") && lower.contains("account"))) {
            val days = extractDays(lower) ?: 7
            return handleAllAccountsPeriodTransactions(days, transactions, allAccounts, isBn, languageMode)
        }

        // 6. RM Manager / Debt / Loan / Liability summary
        if (lower.contains("rm manager") || lower.contains("who owes me") || lower.contains("who do i owe") ||
            lower.contains("debt summary") || lower.contains("দেনা পাওনা") || lower.contains("কার কাছে কত পাবো") ||
            lower.contains("ঋণ কত") || lower.contains("ধার কত") || lower.contains("rm summary")
        ) {
            return handleRmSummaryQuery(allAccounts, accountsWithBalances, transactions, isBn, languageMode)
        }

        // 7. Personal Finance Advice & Savings Tips (50/30/20 rule, Emergency fund)
        if (lower.contains("saving tip") || lower.contains("save money") || lower.contains("50 30 20") ||
            lower.contains("50/30/20") || lower.contains("emergency fund") || lower.contains("সঞ্চয়") ||
            lower.contains("টাকা জমানো") || lower.contains("খরচ কমানো") || lower.contains("পরামর্শ")
        ) {
            return handleFinancialAdviceQuery(overview, lower, isBn, languageMode)
        }

        // 8. Net Worth & Balance queries
        if (lower.contains("net worth") || lower.contains("networth") || lower.contains("সম্পদ") ||
            lower.contains("total balance") || lower.contains("ব্যালেন্স") || lower.contains("balance") ||
            lower.contains("how much money") || lower.contains("টাকা আছে")
        ) {
            return handleNetWorthQuery(overview, accountsWithBalances, isBn, languageMode)
        }

        // 9. Monthly Expenses / Spending / Cash Flow
        if (lower.contains("spend this month") || lower.contains("monthly expense") || lower.contains("monthly spending") ||
            lower.contains("মাসিক খরচ") || lower.contains("এই মাসের খরচ") || lower.contains("cash flow") || lower.contains("ক্যাশ ফ্লো")
        ) {
            return handleMonthlySpendingQuery(transactions, isBn, languageMode)
        }

        // 10. Top Expenses / Categories
        if (lower.contains("top expense") || lower.contains("top spending") || lower.contains("top category") ||
            lower.contains("সর্বোচ্চ খরচ") || lower.contains("কোথায় খরচ") || lower.contains("where did my money go")
        ) {
            return handleTopExpensesQuery(transactions, allCategories, isBn, languageMode)
        }

        // 11. Budget Status & Limits
        if (lower.contains("budget") || lower.contains("বাজেট") || lower.contains("over budget") || lower.contains("limit")) {
            return handleBudgetStatusQuery(budgets, transactions, allCategories, isBn, languageMode)
        }

        // 12. Specific Category Query (e.g., "Food", "Shopping", "Transport", "Bills")
        val matchedCategory = findMatchingCategory(lower, allCategories)
        if (matchedCategory != null) {
            val days = extractDays(lower) ?: 30
            return handleCategorySpendingQuery(matchedCategory, days, transactions, isBn, languageMode)
        }

        // 13. Recent transactions query
        if (lower.contains("recent") || lower.contains("last transaction") || lower.contains("সাম্প্রতিক লেনদেন") || lower.contains("latest")) {
            return handleRecentTransactionsQuery(transactions, isBn, languageMode)
        }

        // 14. App Features Guide
        if (lower.contains("how to backup") || lower.contains("how to restore") || lower.contains("কীভাবে ব্যাকআপ") ||
            lower.contains("how to use rm") || lower.contains("how to add budget")
        ) {
            return handleAppGuideQuery(lower, isBn, languageMode)
        }

        // 15. Default / Fallback with smart suggestions
        val fallbackText = if (isBn) {
            "আমি আপনার অন-ডিভাইস অফলাইন ফাইন্যান্সিয়াল সহকারী। আপনি নির্দিষ্ট অ্যাকাউন্ট (যেমন RM Others, Rocket, Cash), বাজেট, খরচ, সঞ্চয় বা সম্পদ সম্পর্কে প্রশ্ন করতে পারেন। নিচে কয়েকটি উদাহরণ দেখুন:"
        } else {
            "I am your offline Financial Assistant. You can ask about accounts (e.g. RM Others, Rocket, Cash), budget health, spending categories, or saving strategies. Here are some questions to try:"
        }

        val defaultChips = listOf(
            AssistantChip(
                label = if (isBn) "🛡️ বাদ দেওয়া অ্যাকাউন্ট" else "🛡️ Excluded Accounts",
                actionQuery = "show excluded accounts"
            ),
            AssistantChip(
                label = if (isBn) "💇 চুল কাটার বাজেট" else "💇 Hair Cut Budget",
                actionQuery = "hair cut budget"
            ),
            AssistantChip(
                label = if (isBn) "🎯 বাজেটের অবস্থা" else "🎯 Budget Status",
                actionQuery = "Show my budget status"
            ),
            AssistantChip(
                label = if (isBn) "🏦 RM Others একাউন্ট" else "🏦 RM Others Account",
                actionQuery = "tell me about RM Others"
            ),
            AssistantChip(
                label = if (isBn) "💰 মোট সম্পদ" else "💰 Total Net Worth",
                actionQuery = "What is my net worth"
            ),
            AssistantChip(
                label = if (isBn) "👥 আরএম ঋণ ও দেনা-পাওনা" else "👥 RM Debts & Loans",
                actionQuery = "Tell me about RM Manager"
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
                accountCard = accountCard,
                primaryAction = AssistantAction(
                    label = if (isBn) "$accName বিস্তারিত দেখুন" else "View $accName Details",
                    destination = "ACCOUNT_DETAIL",
                    accountId = account.id
                ),
                secondaryActions = listOf(
                    AssistantAction(
                        label = if (isBn) "সকল অ্যাকাউন্ট" else "Go to Accounts",
                        destination = "ACCOUNTS"
                    )
                )
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
                interactiveChips = chips,
                primaryAction = AssistantAction(
                    label = if (isBn) "$accName বিস্তারিত দেখুন" else "View $accName Details",
                    destination = "ACCOUNT_DETAIL",
                    accountId = account.id
                ),
                secondaryActions = listOf(
                    AssistantAction(
                        label = if (isBn) "সকল অ্যাকাউন্ট" else "Go to Accounts",
                        destination = "ACCOUNTS"
                    )
                )
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
            accountCard = accountCard,
            primaryAction = AssistantAction(
                label = if (isBn) "$accName বিস্তারিত দেখুন" else "View $accName Details",
                destination = "ACCOUNT_DETAIL",
                accountId = account.id
            ),
            secondaryActions = listOf(
                AssistantAction(
                    label = if (isBn) "সকল অ্যাকাউন্ট" else "Go to Accounts",
                    destination = "ACCOUNTS"
                )
            )
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
            primaryAction = AssistantAction(
                label = if (isBn) "আরএম ম্যানেজার খুলুন" else "Open RM Manager",
                destination = "RM_MANAGER"
            ),
            secondaryActions = listOf(
                AssistantAction(
                    label = if (isBn) "বাদ দেওয়া অ্যাকাউন্ট" else "Excluded Accounts",
                    destination = "ACCOUNTS_EXCLUDED"
                )
            ),
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
            budgetCard = budgetCard,
            primaryAction = AssistantAction(
                label = if (isBn) "বাজেট মেকার খুলুন" else "Open Budget Maker",
                destination = "BUDGET_MAKER"
            ),
            secondaryActions = listOf(
                AssistantAction(
                    label = if (isBn) "বাজেট ট্র্যাকিং" else "Budget Tracking",
                    destination = "BUDGET"
                )
            ),
            interactiveChips = listOf(
                AssistantChip(
                    label = if (isBn) "বাজেট মেকার" else "Budget Maker",
                    actionQuery = "go to budget maker",
                    navigationAction = AssistantAction(
                        label = if (isBn) "বাজেট মেকার" else "Budget Maker",
                        destination = "BUDGET_MAKER"
                    )
                ),
                AssistantChip(
                    label = if (isBn) "💇 চুল কাটার বাজেট" else "💇 Hair Cut Budget",
                    actionQuery = "hair cut budget"
                )
            )
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

    private fun detectDirectNavigation(lower: String, isBn: Boolean): AssistantMessage? {
        val isNavQuery = lower.startsWith("go to") || lower.startsWith("open") || lower.startsWith("take me to") ||
            lower.contains("পেজে যাও") || lower.contains("খুলো") || lower.contains("খোলো") ||
            lower.contains("দেখাও") || lower.contains("screen") || lower.contains("page")

        if (!isNavQuery) return null

        if (lower.contains("budget maker") || lower.contains("বাজেট মেকার")) {
            val subject = if (lower.contains("hair cut") || lower.contains("haircut") || lower.contains("চুল কাটা")) "Hair Cut" else ""
            val text = if (isBn) {
                "সরাসরি **বাজেট মেকার (Budget Maker)** পেজে যেতে নিচের বাটনে ট্যাপ করুন:"
            } else {
                "Tap below to open **Budget Maker**${if (subject.isNotEmpty()) " for $subject" else ""}:"
            }
            return AssistantMessage(
                text = text,
                isUser = false,
                primaryAction = AssistantAction(
                    label = if (isBn) "বাজেট মেকার খুলুন" else "Open Budget Maker",
                    destination = "BUDGET_MAKER",
                    searchQuery = subject
                )
            )
        }

        if (lower.contains("excluded") || lower.contains("বাদ দেওয়া") || lower.contains("বাদ দেওয়া")) {
            val text = if (isBn) {
                "**বাদ দেওয়া অ্যাকাউন্ট (Excluded Accounts)** দেখতে নিচের বাটনে ট্যাপ করুন:"
            } else {
                "Tap below to view **Excluded Accounts** in the Accounts screen:"
            }
            return AssistantMessage(
                text = text,
                isUser = false,
                primaryAction = AssistantAction(
                    label = if (isBn) "বাদ দেওয়া অ্যাকাউন্ট দেখুন" else "Go to Excluded Accounts",
                    destination = "ACCOUNTS_EXCLUDED"
                )
            )
        }

        if (lower.contains("accounts") || lower.contains("অ্যাকাউন্ট")) {
            val text = if (isBn) {
                "**অ্যাকাউন্টস (Accounts)** স্ক্রিন খুলতে নিচের বাটনে ট্যাপ করুন:"
            } else {
                "Tap below to open the **Accounts** screen:"
            }
            return AssistantMessage(
                text = text,
                isUser = false,
                primaryAction = AssistantAction(
                    label = if (isBn) "অ্যাকাউন্টস স্ক্রিন খুলুন" else "Open Accounts",
                    destination = "ACCOUNTS"
                )
            )
        }

        if (lower.contains("rm manager") || lower.contains("আরএম ম্যানেজার") || lower.contains("debt") || lower.contains("দেনা পাওনা")) {
            val text = if (isBn) {
                "**আরএম ম্যানেজার (RM Manager)** খুলতে নিচের বাটনে ট্যাপ করুন:"
            } else {
                "Tap below to open **RM Manager**:"
            }
            return AssistantMessage(
                text = text,
                isUser = false,
                primaryAction = AssistantAction(
                    label = if (isBn) "আরএম ম্যানেজার খুলুন" else "Open RM Manager",
                    destination = "RM_MANAGER"
                )
            )
        }

        if (lower.contains("ledger") || lower.contains("transactions") || lower.contains("লেনদেন খাতা")) {
            val text = if (isBn) {
                "**লেনদেন খাতা (Transactions Ledger)** দেখতে নিচের বাটনে ট্যাপ করুন:"
            } else {
                "Tap below to open the **Transactions Ledger**:"
            }
            return AssistantMessage(
                text = text,
                isUser = false,
                primaryAction = AssistantAction(
                    label = if (isBn) "লেনদেন খাতা খুলুন" else "Open Transactions Ledger",
                    destination = "LEDGER"
                )
            )
        }

        if (lower.contains("balance sheet") || lower.contains("ব্যালেন্স শিট")) {
            val text = if (isBn) {
                "**ব্যালেন্স শিট (Balance Sheet)** দেখতে নিচের বাটনে ট্যাপ করুন:"
            } else {
                "Tap below to view the **Balance Sheet**:"
            }
            return AssistantMessage(
                text = text,
                isUser = false,
                primaryAction = AssistantAction(
                    label = if (isBn) "ব্যালেন্স শিট খুলুন" else "Open Balance Sheet",
                    destination = "BALANCE_SHEET"
                )
            )
        }

        if (lower.contains("cash flow") || lower.contains("ক্যাশ ফ্লো")) {
            val text = if (isBn) {
                "**ক্যাশ ফ্লো (Cash Flow)** বিবরণী দেখতে নিচের বাটনে ট্যাপ করুন:"
            } else {
                "Tap below to view **Cash Flow**:"
            }
            return AssistantMessage(
                text = text,
                isUser = false,
                primaryAction = AssistantAction(
                    label = if (isBn) "ক্যাশ ফ্লো খুলুন" else "Open Cash Flow",
                    destination = "CASH_FLOW"
                )
            )
        }

        if (lower.contains("report") || lower.contains("রিপোর্ট") || lower.contains("net earnings")) {
            val text = if (isBn) {
                "**আর্থিক প্রতিবেদন ও বিশ্লেষণ (Reports)** দেখতে নিচের বাটনে ট্যাপ করুন:"
            } else {
                "Tap below to open **Financial Reports & Analytics**:"
            }
            return AssistantMessage(
                text = text,
                isUser = false,
                primaryAction = AssistantAction(
                    label = if (isBn) "রিপোর্ট পেজ খুলুন" else "Open Reports",
                    destination = "REPORTS"
                )
            )
        }

        if (lower.contains("category") || lower.contains("categories") || lower.contains("ক্যাটাগরি")) {
            val text = if (isBn) {
                "**ক্যাটাগরি (Categories)** পরিচালনা করতে নিচের বাটনে ট্যাপ করুন:"
            } else {
                "Tap below to open **Categories**:"
            }
            return AssistantMessage(
                text = text,
                isUser = false,
                primaryAction = AssistantAction(
                    label = if (isBn) "ক্যাটাগরি পেজ খুলুন" else "Open Categories",
                    destination = "CATEGORIES"
                )
            )
        }

        return null
    }

    private fun isExcludedAccountsQuery(lower: String): Boolean {
        return lower.contains("excluded account") ||
            lower.contains("excluded accounts") ||
            lower.contains("excluded") ||
            lower.contains("exclude account") ||
            lower.contains("বাদ দেওয়া অ্যাকাউন্ট") ||
            lower.contains("বাদ দেওয়া একাউন্ট") ||
            lower.contains("বাদ দেওয়া হিসাব") ||
            lower.contains("বাদ দেওয়া হিসাব") ||
            lower.contains("বাদ দেওয়া একাউন্ট") ||
            lower.contains("বাদ রাখা") ||
            lower.contains("এক্সক্লুডেড") ||
            (lower.contains("account") && lower.contains("excluded")) ||
            (lower.contains("একাউন্ট") && lower.contains("বাদ"))
    }

    private fun handleExcludedAccountsQuery(
        allAccounts: List<Account>,
        accountsWithBalances: List<AccountWithBalance>,
        accountCalcConfig: AccountCalcConfig,
        isBn: Boolean,
        languageMode: LanguageMode
    ): AssistantMessage {
        val excludedAccounts = allAccounts.filter { acc ->
            !accountCalcConfig.isIncluded(acc.id)
        }

        val excludedItems = excludedAccounts.map { acc ->
            val bal = accountsWithBalances.find { it.account.id == acc.id }?.currentBalance ?: 0.0
            AssistantExcludedItem(
                accountName = acc.localizedName(languageMode),
                accountType = acc.type.name,
                balance = bal,
                reason = if (isBn) "মোট সম্পদ ও ব্যালেন্স শিট গণনা থেকে বাদ দেওয়া হয়েছে" else "Excluded from Net Worth & Balance Sheet calculations"
            )
        }

        val totalExcludedBalance = excludedItems.sumOf { it.balance }
        val balStr = LanguageHelper.formatCurrency(totalExcludedBalance, languageMode)

        val sb = StringBuilder()
        if (isBn) {
            sb.append("🛡️ **বাদ দেওয়া অ্যাকাউন্ট (Excluded Accounts) পর্যালোচনা:**\n\n")
            if (excludedAccounts.isEmpty()) {
                sb.append("আপনার অ্যাপে বর্তমানে কোনো অ্যাকাউন্ট গণনা থেকে বাদ রাখা হয়নি। সকল অ্যাকাউন্টের ব্যালেন্স আপনার নিট সম্পদে যুক্ত হচ্ছে।\n\n")
                sb.append("• কোনো অ্যাকাউন্ট (যেমন আরএম খাতা বা বিশেষ দায়) নিট সম্পদ থেকে আলাদা রাখতে চাইলে Accounts স্ক্রিন থেকে তা বাদ দিতে পারেন।")
            } else {
                sb.append("বর্তমানে **${excludedAccounts.size}টি** অ্যাকাউন্ট আপনার মূল হিসাব ও মোট সম্পদ (Net Worth) থেকে বাদ রাখা হয়েছে।\n")
                sb.append("• বাদ দেওয়া মোট ব্যালেন্স: **$balStr**\n\n")
                sb.append("📋 **বাদ দেওয়া অ্যাকাউন্টসমূহের তালিকা:**\n")
                excludedItems.forEach { item ->
                    val itemBal = LanguageHelper.formatCurrency(item.balance, languageMode)
                    sb.append("• **${item.accountName}** (${item.accountType}): **$itemBal**\n")
                }
                sb.append("\n💡 এই অ্যাকাউন্টগুলোর লেনদেন ইতিহাস সুরক্ষিত থাকে, কিন্তু আপনার প্রকৃত ব্যবহারযোগ্য অর্থ প্রদর্শনের স্বার্থে মোট সম্পদে এদের অন্তর্ভুক্ত করা হয় না।")
            }
        } else {
            sb.append("🛡️ **Excluded Accounts Overview:**\n\n")
            if (excludedAccounts.isEmpty()) {
                sb.append("You currently do not have any excluded accounts configured in Budgeter. All your account balances are factored into your Net Worth.\n\n")
                sb.append("• To exclude an account (such as loans, debts, or tracking-only accounts) from your Net Worth, you can toggle it from the Accounts screen.")
            } else {
                sb.append("You currently have **${excludedAccounts.size}** account${if (excludedAccounts.size > 1) "s" else ""} excluded from your main Net Worth calculations.\n")
                sb.append("• Total Excluded Balance: **$balStr**\n\n")
                sb.append("📋 **Excluded Accounts List:**\n")
                excludedItems.forEach { item ->
                    val itemBal = LanguageHelper.formatCurrency(item.balance, languageMode)
                    sb.append("• **${item.accountName}** (${item.accountType}): **$itemBal**\n")
                }
                sb.append("\n💡 Excluded accounts retain their complete transaction ledger history, but their balances are omitted from your Net Worth so you always see your true spendable liquidity.")
            }
        }

        val card = AssistantExcludedAccountsCard(
            totalExcludedCount = excludedAccounts.size,
            totalExcludedBalance = totalExcludedBalance,
            items = excludedItems
        )

        val chips = listOf(
            AssistantChip(
                label = if (isBn) "🛡️ বাদ দেওয়া অ্যাকাউন্টে যান" else "🛡️ Go to Excluded Accounts",
                actionQuery = "go to excluded accounts",
                navigationAction = AssistantAction(
                    label = if (isBn) "বাদ দেওয়া অ্যাকাউন্ট দেখুন" else "View Excluded Accounts",
                    destination = "ACCOUNTS_EXCLUDED"
                )
            ),
            AssistantChip(
                label = if (isBn) "💰 মোট সম্পদ" else "💰 Net Worth",
                actionQuery = "What is my net worth"
            ),
            AssistantChip(
                label = if (isBn) "🏦 সকল অ্যাকাউন্ট" else "🏦 All Accounts",
                actionQuery = "go to accounts page"
            )
        )

        return AssistantMessage(
            text = sb.toString().trim(),
            isUser = false,
            excludedAccountsCard = card,
            primaryAction = AssistantAction(
                label = if (isBn) "বাদ দেওয়া অ্যাকাউন্ট স্ক্রিন খুলুন" else "Go to Excluded Accounts",
                destination = "ACCOUNTS_EXCLUDED"
            ),
            secondaryActions = listOf(
                AssistantAction(
                    label = if (isBn) "সকল অ্যাকাউন্ট" else "View All Accounts",
                    destination = "ACCOUNTS"
                )
            ),
            interactiveChips = chips
        )
    }

    private fun isCategoryBudgetQuery(cleanQuery: String, categories: List<Category>): Boolean {
        val q = cleanQuery.lowercase(Locale.ROOT)
        // Explicit check for Hair Cut / Grooming
        if (q.contains("hair cut") || q.contains("haircut") || q.contains("চুল কাটা") || q.contains("চুল কাটার")) {
            return true
        }

        val hasBudgetKeyword = q.contains("budget") || q.contains("বাজেট")
        if (!hasBudgetKeyword) return false

        // Exclude purely generic overview queries
        val genericPhrases = listOf(
            "budget status", "budget overview", "show budget", "my budget", "all budget",
            "বাজেটের অবস্থা", "বাজেট দেখাও", "বাজেট পর্যালোচনা", "চলতি মাসের বাজেট",
            "over budget", "budget limit", "budget limits"
        )
        if (genericPhrases.any { q == it || q == "$it?" || q == "show $it" }) {
            return false
        }

        return true
    }

    private fun extractBudgetCategorySubject(query: String, categories: List<Category>): Pair<Category?, String> {
        val lower = query.lowercase(Locale.ROOT).trim()

        // 1. Direct match with existing categories
        val matchedCat = findMatchingCategory(lower, categories)
        if (matchedCat != null) {
            return Pair(matchedCat, matchedCat.nameEn)
        }

        // 2. Hair Cut / Grooming
        if (lower.contains("hair cut") || lower.contains("haircut") || lower.contains("চুল কাটা") || lower.contains("চুল কাটার")) {
            val hairCat = categories.firstOrNull {
                it.nameEn.contains("hair", ignoreCase = true) ||
                it.nameEn.contains("groom", ignoreCase = true) ||
                it.nameEn.contains("salon", ignoreCase = true) ||
                it.nameBn.contains("চুল", ignoreCase = true)
            }
            return Pair(hairCat, "Hair Cut")
        }

        // 3. Clean up query to extract potential category name
        var cleanSubject = lower
            .replace("can i go to that budget maker page of", "")
            .replace("can i go to budget maker page of", "")
            .replace("go to that budget maker page of", "")
            .replace("go to budget maker page of", "")
            .replace("open budget maker for", "")
            .replace("how much is", "")
            .replace("what is my", "")
            .replace("tell me about", "")
            .replace("check", "")
            .replace("show me", "")
            .replace("show", "")
            .replace("monthly budget", "")
            .replace("budget status", "")
            .replace("budget maker", "")
            .replace("budget", "")
            .replace("বাজেট মেকার পেজে যাও", "")
            .replace("বাজেট মেকারে যাও", "")
            .replace("এর বাজেট কত", "")
            .replace("বাজেট কত", "")
            .replace("বাজেট দেখাও", "")
            .replace("বাজেট", "")
            .replace("?", "")
            .replace("\"", "")
            .trim()

        if (cleanSubject.isBlank()) {
            cleanSubject = "Budget"
        } else {
            cleanSubject = cleanSubject.split(" ").filter { it.isNotBlank() }
                .joinToString(" ") { it.replaceFirstChar { ch -> ch.uppercaseChar() } }
        }

        return Pair(null, cleanSubject)
    }

    private fun handleCategoryBudgetQuery(
        query: String,
        lower: String,
        categories: List<Category>,
        budgets: List<MonthlyBudget>,
        transactions: List<TransactionWithDetails>,
        isBn: Boolean,
        languageMode: LanguageMode
    ): AssistantMessage {
        val (matchedCat, subjectName) = extractBudgetCategorySubject(lower, categories)

        val cal = Calendar.getInstance()
        val currentYear = cal.get(Calendar.YEAR)
        val currentMonth = cal.get(Calendar.MONTH) + 1

        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val monthStartMs = cal.timeInMillis

        cal.add(Calendar.MONTH, 1)
        val monthEndMs = cal.timeInMillis

        if (matchedCat != null) {
            val catName = matchedCat.localizedName(languageMode)
            val budgetEntry = budgets.find {
                it.itemId == matchedCat.id && it.year == currentYear && it.month == currentMonth
            }

            val limit = budgetEntry?.budgetedAmount ?: matchedCat.budgetLimit ?: 0.0
            val catTxs = transactions.filter { td ->
                val tx = td.transaction
                val isCat = tx.categoryId == matchedCat.id || td.category?.id == matchedCat.id || td.subCategory?.id == matchedCat.id
                val inMonth = tx.dateEpochMs in monthStartMs until monthEndMs
                isCat && inMonth && tx.type == TransactionType.EXPENSE
            }.sortedByDescending { it.transaction.dateEpochMs }

            val totalSpent = catTxs.sumOf { it.transaction.amount }
            val remaining = (limit - totalSpent).coerceAtLeast(0.0)
            val percentage = if (limit > 0) (totalSpent / limit * 100) else 0.0
            val isOverBudget = limit > 0 && totalSpent > limit

            val spentStr = LanguageHelper.formatCurrency(totalSpent, languageMode)
            val limitStr = LanguageHelper.formatCurrency(limit, languageMode)
            val remStr = LanguageHelper.formatCurrency(remaining, languageMode)

            val sb = StringBuilder()
            if (isBn) {
                sb.append("🎯 **$catName বাজেট বিবরণ:**\n\n")
                if (limit > 0) {
                    sb.append("• চলতি মাসের বাজেট সীমা: **$limitStr**\n")
                    sb.append("• এ পর্যন্ত খরচ হয়েছে: **$spentStr** (${percentage.toInt()}%)\n")
                    sb.append("• অবশিষ্ট বরাদ্দ: **$remStr**\n\n")
                    if (isOverBudget) {
                        val overAmt = LanguageHelper.formatCurrency(totalSpent - limit, languageMode)
                        sb.append("⚠️ **সতর্কতা:** আপনি বাজেট সীমা অতিক্রম করেছেন! অতিরিক্ত খরচ: **$overAmt**\n")
                    } else if (percentage >= 85) {
                        sb.append("⚠️ **সতর্কতা:** আপনি নির্ধারিত বাজেটের ৮৫% এর বেশি খরচ করে ফেলেছেন।\n")
                    } else {
                        sb.append("✅ আপনার খরচ নির্ধারিত বাজেটের নিয়ন্ত্রণে রয়েছে।\n")
                    }
                } else {
                    sb.append("• এ পর্যন্ত চলতি মাসে খরচ: **$spentStr** (${catTxs.size}টি লেনদেন)\n")
                    sb.append("• বাজেট সীমা: **নির্ধারিত নেই**\n\n")
                    sb.append("💡 আপনি **বাজেট মেকার (Budget Maker)** এ গিয়ে এই ক্যাটাগরির জন্য একটি মাসিক সীমা নির্ধারণ করতে পারেন।")
                }
            } else {
                sb.append("🎯 **$catName Budget Status:**\n\n")
                if (limit > 0) {
                    sb.append("• Monthly Budget Limit: **$limitStr**\n")
                    sb.append("• Spent This Month: **$spentStr** (${percentage.toInt()}%)\n")
                    sb.append("• Remaining Balance: **$remStr**\n\n")
                    if (isOverBudget) {
                        val overAmt = LanguageHelper.formatCurrency(totalSpent - limit, languageMode)
                        sb.append("⚠️ **Warning:** Budget exceeded! You are **$overAmt** over limit.\n")
                    } else if (percentage >= 85) {
                        sb.append("⚠️ **Notice:** You have utilized over 85% of this budget limit.\n")
                    } else {
                        sb.append("✅ Spending is well within your planned monthly limit.\n")
                    }
                } else {
                    sb.append("• Spent This Month: **$spentStr** (${catTxs.size} transaction${if (catTxs.size > 1) "s" else ""})\n")
                    sb.append("• Monthly Limit: **Not Set**\n\n")
                    sb.append("💡 You can set a monthly spending limit for this category directly in **Budget Maker**.")
                }
            }

            val card = AssistantCategoryBudgetCard(
                categoryName = catName,
                categoryType = matchedCat.type.name,
                budgetLimit = limit,
                spentAmount = totalSpent,
                remainingAmount = remaining,
                percentage = percentage,
                isOverBudget = isOverBudget,
                hasBudget = limit > 0
            )

            val chips = listOf(
                AssistantChip(
                    label = if (isBn) "বাজেট মেকারে যান" else "Go to Budget Maker",
                    actionQuery = "go to budget maker page of ${matchedCat.nameEn}",
                    navigationAction = AssistantAction(
                        label = if (isBn) "বাজেট মেকারে দেখুন" else "Go to ${matchedCat.nameEn} in Budget Maker",
                        destination = "BUDGET_MAKER",
                        categoryId = matchedCat.id,
                        searchQuery = matchedCat.nameEn
                    )
                ),
                AssistantChip(
                    label = if (isBn) "বাজেটের অবস্থা" else "Overall Budget",
                    actionQuery = "Show my budget status"
                )
            )

            return AssistantMessage(
                text = sb.toString().trim(),
                isUser = false,
                categoryBudgetCard = card,
                primaryAction = AssistantAction(
                    label = if (isBn) "$catName বাজেট মেকারে দেখুন" else "Go to $catName in Budget Maker",
                    destination = "BUDGET_MAKER",
                    categoryId = matchedCat.id,
                    searchQuery = matchedCat.nameEn
                ),
                secondaryActions = listOf(
                    AssistantAction(
                        label = if (isBn) "বাজেট ট্র্যাকিং পেজ" else "View Budget Tracking",
                        destination = "BUDGET"
                    )
                ),
                transactionList = catTxs.take(4),
                interactiveChips = chips
            )
        } else {
            // No matching category in DB yet (e.g. "Hair Cut" when not created yet)
            val displaySubject = subjectName.ifBlank { "Hair Cut" }

            val sb = StringBuilder()
            if (isBn) {
                sb.append("🎯 **$displaySubject বাজেট:**\n\n")
                sb.append("আপনার অ্যাপে বর্তমানে '**$displaySubject**' নামে কোনো ক্যাটাগরি বা বাজেট পাওয়া যায়নি।\n\n")
                sb.append("• চলতি মাসের রেকর্ডকৃত খরচ: **৳০.০০**\n")
                sb.append("• বাজেট সীমা: **নির্ধারিত নেই**\n\n")
                sb.append("💡 আপনি নিচের বাটনে ট্যাপ করে সরাসরি **বাজেট মেকার (Budget Maker)** পেজে গিয়ে **$displaySubject** এর জন্য নতুন বাজেট ও ক্যাটাগরি সেট করতে পারেন।")
            } else {
                sb.append("🎯 **$displaySubject Budget:**\n\n")
                sb.append("No existing category or budget named '**$displaySubject**' was found in your app.\n\n")
                sb.append("• Spent This Month: **৳0.00**\n")
                sb.append("• Budget Limit: **Not Configured**\n\n")
                sb.append("💡 Tap the button below to jump straight to the **Budget Maker** page and set up a budget or category for **$displaySubject**.")
            }

            val card = AssistantCategoryBudgetCard(
                categoryName = displaySubject,
                categoryType = "Expense",
                budgetLimit = 0.0,
                spentAmount = 0.0,
                remainingAmount = 0.0,
                percentage = 0.0,
                isOverBudget = false,
                hasBudget = false
            )

            val chips = listOf(
                AssistantChip(
                    label = if (isBn) "বাজেট মেকারে যান ($displaySubject)" else "Go to Budget Maker ($displaySubject)",
                    actionQuery = "go to budget maker",
                    navigationAction = AssistantAction(
                        label = if (isBn) "বাজেট মেকারে যান ($displaySubject)" else "Go to Budget Maker ($displaySubject)",
                        destination = "BUDGET_MAKER",
                        searchQuery = displaySubject
                    )
                ),
                AssistantChip(
                    label = if (isBn) "ক্যাটাগরি পেজে যান" else "Go to Categories",
                    actionQuery = "go to categories screen",
                    navigationAction = AssistantAction(
                        label = if (isBn) "ক্যাটাগরি পেজ খুলুন" else "Open Categories",
                        destination = "CATEGORIES"
                    )
                ),
                AssistantChip(
                    label = if (isBn) "বাজেটের অবস্থা" else "Overall Budget",
                    actionQuery = "Show my budget status"
                )
            )

            return AssistantMessage(
                text = sb.toString().trim(),
                isUser = false,
                categoryBudgetCard = card,
                primaryAction = AssistantAction(
                    label = if (isBn) "বাজেট মেকারে যান ($displaySubject)" else "Go to Budget Maker ($displaySubject)",
                    destination = "BUDGET_MAKER",
                    searchQuery = displaySubject
                ),
                secondaryActions = listOf(
                    AssistantAction(
                        label = if (isBn) "ক্যাটাগরি পেজে যান" else "Go to Categories",
                        destination = "CATEGORIES"
                    )
                ),
                interactiveChips = chips
            )
        }
    }
}
