package com.example.util

import com.example.data.model.Account
import com.example.data.model.AccountRequirementAnalysis
import com.example.data.model.AccountRequirementItem
import com.example.data.model.AccountType
import com.example.data.model.BillStatus
import com.example.data.model.Category
import com.example.data.model.CategoryType
import com.example.data.model.FundAllocationSuggestion
import com.example.data.model.MonthlyBudget
import com.example.data.model.PaymentSourceAnalysisOverview
import com.example.data.model.RecurringBill
import com.example.data.model.RequirementCalculationBasis
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.data.model.TransactionWithDetails
import com.example.data.repository.AccountWithBalance

object PaymentSourceCalculator {

    fun calculateAnalysis(
        year: Int,
        month: Int,
        basis: RequirementCalculationBasis,
        allAccounts: List<Account>,
        accountsWithBalances: List<AccountWithBalance>,
        allCategories: List<Category>,
        monthlyBudgets: List<MonthlyBudget>,
        allTransactions: List<TransactionWithDetails>,
        recurringBills: List<RecurringBill> = emptyList()
    ): PaymentSourceAnalysisOverview {
        // Balance map for quick lookup
        val balanceMap = accountsWithBalances.associate { it.account.id to it.currentBalance }

        // Filter leaf accounts (sub-accounts or accounts where parentId != null or non-group)
        val validAccounts = allAccounts.filter { it.parentId != null && it.isActive && (it.type == AccountType.ASSET || it.type == AccountType.LIABILITY) }
            .ifEmpty { allAccounts.filter { it.isActive } }

        // Month boundary timestamps
        val startOfMonthMs = DateUtils.getStartOfMonth(year, month)
        val endOfMonthMs = DateUtils.getEndOfMonth(year, month)

        // Transactions in this month
        val monthTxs = allTransactions.filter { it.transaction.dateEpochMs in startOfMonthMs..endOfMonthMs }

        // Monthly budget entries
        val budgetMap = monthlyBudgets.associateBy { "${it.itemType}_${it.itemId}" }

        // Categories map
        val categoryMap = allCategories.associateBy { it.id }

        // Determine default/most-used account for each category
        // 1. First from current month transactions
        val catToAccountFromMonth = mutableMapOf<Long, Long>()
        for (tx in monthTxs) {
            val catId = tx.transaction.subCategoryId ?: tx.transaction.categoryId
            val accId = if (tx.transaction.type == TransactionType.EXPENSE) tx.transaction.creditAccountId else tx.transaction.debitAccountId
            if (catId != null && accId != null) {
                catToAccountFromMonth[catId] = accId
            }
        }

        // 2. From historical transactions if not in current month
        val catToAccountHistorical = mutableMapOf<Long, Long>()
        for (tx in allTransactions) {
            val catId = tx.transaction.subCategoryId ?: tx.transaction.categoryId
            val accId = if (tx.transaction.type == TransactionType.EXPENSE) tx.transaction.creditAccountId else tx.transaction.debitAccountId
            if (catId != null && accId != null && !catToAccountHistorical.containsKey(catId)) {
                catToAccountHistorical[catId] = accId
            }
        }

        // Primary default fallback account (first asset account like Cash or Bank)
        val defaultFallbackAccountId = validAccounts.firstOrNull { it.type == AccountType.ASSET }?.id ?: validAccounts.firstOrNull()?.id ?: 0L

        fun resolveAccountForCategory(catId: Long): Long {
            return catToAccountFromMonth[catId]
                ?: catToAccountHistorical[catId]
                ?: defaultFallbackAccountId
        }

        val accountAnalyses = validAccounts.map { account ->
            val accId = account.id
            val currentBal = balanceMap[accId] ?: 0.0

            // 1. Transactions for this account in the month
            val expenseTxsForAcc = monthTxs.filter {
                it.transaction.type == TransactionType.EXPENSE && it.transaction.creditAccountId == accId
            }
            val incomeTxsForAcc = monthTxs.filter {
                it.transaction.type == TransactionType.INCOME && it.transaction.debitAccountId == accId
            }

            // Group actual spent by category
            val spentByCat = mutableMapOf<Long, Double>()
            for (tx in expenseTxsForAcc) {
                val catId = tx.transaction.subCategoryId ?: tx.transaction.categoryId
                if (catId != null) {
                    spentByCat[catId] = (spentByCat[catId] ?: 0.0) + tx.transaction.amount
                }
            }

            // Group actual received by category
            val receivedByCat = mutableMapOf<Long, Double>()
            for (tx in incomeTxsForAcc) {
                val catId = tx.transaction.subCategoryId ?: tx.transaction.categoryId
                if (catId != null) {
                    receivedByCat[catId] = (receivedByCat[catId] ?: 0.0) + tx.transaction.amount
                }
            }

            // 2. Find expense categories mapped to this account
            val expenseCategories = allCategories.filter { it.type == CategoryType.EXPENSE && it.parentId != null }
            val itemizedExpenses = mutableListOf<AccountRequirementItem>()
            var totalExpenseReq = 0.0

            for (cat in expenseCategories) {
                val mappedAccId = resolveAccountForCategory(cat.id)
                val spentInThisAcc = spentByCat[cat.id] ?: 0.0
                val budgetEntry = budgetMap["EXPENSE_${cat.id}"]
                val isEnabled = budgetEntry?.isEnabled ?: (cat.budgetLimit > 0)
                val budgetLimit = if (isEnabled) (budgetEntry?.budgetedAmount ?: cat.budgetLimit) else 0.0

                // Only include if either:
                // a) this account is the mapped account for the category, OR
                // b) money was actually spent from this account for this category
                if (mappedAccId == accId || spentInThisAcc > 0) {
                    val originalBudget = if (mappedAccId == accId) budgetLimit else 0.0
                    val remaining = if (basis == RequirementCalculationBasis.REMAINING_AMOUNT) {
                        if (originalBudget > 0) maxOf(0.0, originalBudget - spentInThisAcc) else 0.0
                    } else {
                        maxOf(originalBudget, spentInThisAcc)
                    }

                    val requiredAmt = if (basis == RequirementCalculationBasis.BUDGET_AMOUNT) {
                        if (originalBudget > 0) originalBudget else spentInThisAcc
                    } else {
                        remaining
                    }

                    if (requiredAmt > 0 || spentInThisAcc > 0 || originalBudget > 0) {
                        itemizedExpenses.add(
                            AccountRequirementItem(
                                title = cat.nameEn,
                                amount = requiredAmt,
                                originalBudgetOrExpected = originalBudget,
                                actualSpentOrReceived = spentInThisAcc,
                                remaining = remaining,
                                isRecurring = false,
                                isExpense = true,
                                categoryId = cat.id,
                                iconName = cat.iconName,
                                colorHex = cat.colorHex
                            )
                        )
                        totalExpenseReq += requiredAmt
                    }
                }
            }

            // Recurring bills (Expenses)
            val accExpenseBills = recurringBills.filter {
                it.type == TransactionType.EXPENSE && it.creditAccountId == accId
            }
            for (bill in accExpenseBills) {
                val billCat = bill.categoryId?.let { categoryMap[it] }
                val isPending = bill.nextDueDateEpochMs in startOfMonthMs..endOfMonthMs
                val billAmt = bill.amount

                val requiredBillAmt = if (basis == RequirementCalculationBasis.BUDGET_AMOUNT) {
                    billAmt
                } else {
                    if (isPending) billAmt else 0.0
                }

                if (requiredBillAmt > 0) {
                    itemizedExpenses.add(
                        AccountRequirementItem(
                            title = bill.title,
                            amount = requiredBillAmt,
                            originalBudgetOrExpected = billAmt,
                            actualSpentOrReceived = if (isPending) 0.0 else billAmt,
                            remaining = requiredBillAmt,
                            isRecurring = true,
                            isExpense = true,
                            categoryId = bill.categoryId,
                            iconName = billCat?.iconName ?: "Alarm",
                            colorHex = billCat?.colorHex ?: "#F59E0B"
                        )
                    )
                    totalExpenseReq += requiredBillAmt
                }
            }

            // 3. Find income categories and expected income mapped to this account
            val incomeCategories = allCategories.filter { it.type == CategoryType.INCOME && it.parentId != null }
            val itemizedIncomes = mutableListOf<AccountRequirementItem>()
            var totalIncomeExp = 0.0

            for (cat in incomeCategories) {
                val mappedAccId = resolveAccountForCategory(cat.id)
                val receivedInThisAcc = receivedByCat[cat.id] ?: 0.0
                val budgetEntry = budgetMap["INCOME_${cat.id}"]
                val isEnabled = budgetEntry?.isEnabled ?: (cat.budgetLimit > 0)
                val budgetLimit = if (isEnabled) (budgetEntry?.budgetedAmount ?: cat.budgetLimit) else 0.0

                if (mappedAccId == accId || receivedInThisAcc > 0) {
                    val originalBudget = if (mappedAccId == accId) budgetLimit else 0.0
                    val remaining = if (basis == RequirementCalculationBasis.REMAINING_AMOUNT) {
                        if (originalBudget > 0) maxOf(0.0, originalBudget - receivedInThisAcc) else 0.0
                    } else {
                        maxOf(originalBudget, receivedInThisAcc)
                    }

                    val expectedAmt = if (basis == RequirementCalculationBasis.BUDGET_AMOUNT) {
                        if (originalBudget > 0) originalBudget else receivedInThisAcc
                    } else {
                        remaining
                    }

                    if (expectedAmt > 0 || receivedInThisAcc > 0 || originalBudget > 0) {
                        itemizedIncomes.add(
                            AccountRequirementItem(
                                title = cat.nameEn,
                                amount = expectedAmt,
                                originalBudgetOrExpected = originalBudget,
                                actualSpentOrReceived = receivedInThisAcc,
                                remaining = remaining,
                                isRecurring = false,
                                isExpense = false,
                                categoryId = cat.id,
                                iconName = cat.iconName,
                                colorHex = cat.colorHex
                            )
                        )
                        totalIncomeExp += expectedAmt
                    }
                }
            }

            // Recurring bills (Incomes)
            val accIncomeBills = recurringBills.filter {
                it.type == TransactionType.INCOME && it.debitAccountId == accId
            }
            for (bill in accIncomeBills) {
                val billCat = bill.categoryId?.let { categoryMap[it] }
                val isPending = bill.nextDueDateEpochMs in startOfMonthMs..endOfMonthMs
                val billAmt = bill.amount

                val expectedBillAmt = if (basis == RequirementCalculationBasis.BUDGET_AMOUNT) {
                    billAmt
                } else {
                    if (isPending) billAmt else 0.0
                }

                if (expectedBillAmt > 0) {
                    itemizedIncomes.add(
                        AccountRequirementItem(
                            title = bill.title,
                            amount = expectedBillAmt,
                            originalBudgetOrExpected = billAmt,
                            actualSpentOrReceived = if (isPending) 0.0 else billAmt,
                            remaining = expectedBillAmt,
                            isRecurring = true,
                            isExpense = false,
                            categoryId = bill.categoryId,
                            iconName = billCat?.iconName ?: "Alarm",
                            colorHex = billCat?.colorHex ?: "#10B981"
                        )
                    )
                    totalIncomeExp += expectedBillAmt
                }
            }

            val available = currentBal + totalIncomeExp
            val shortfall = if (available < totalExpenseReq) totalExpenseReq - available else 0.0
            val surplus = if (available > totalExpenseReq) available - totalExpenseReq else 0.0

            AccountRequirementAnalysis(
                account = account,
                currentBalance = currentBal,
                requiredExpenseAmount = totalExpenseReq,
                expectedIncomeAmount = totalIncomeExp,
                availableAmount = available,
                shortfall = shortfall,
                surplus = surplus,
                itemizedExpenses = itemizedExpenses.sortedByDescending { it.amount },
                itemizedIncomes = itemizedIncomes.sortedByDescending { it.amount }
            )
        }

        // Generate Fund Allocation Insights (Transfer Suggestions)
        val transferSuggestions = mutableListOf<FundAllocationSuggestion>()

        // Surplus accounts (mutable pool)
        val surplusPool = accountAnalyses.filter { it.isSurplus }
            .map { it.account to it.surplus }
            .toMutableList()

        // Shortfall accounts (mutable needs)
        val shortfallNeeds = accountAnalyses.filter { it.isShortfall }
            .map { it.account to it.shortfall }
            .toMutableList()

        for (i in shortfallNeeds.indices) {
            val (shortfallAcc, totalNeeded) = shortfallNeeds[i]
            var remainingNeed = totalNeeded

            for (j in surplusPool.indices) {
                if (remainingNeed <= 0.001) break
                val (surplusAcc, availableSurplus) = surplusPool[j]
                if (availableSurplus <= 0.001) continue

                val transferAmount = minOf(remainingNeed, availableSurplus)
                if (transferAmount > 0.01) {
                    transferSuggestions.add(
                        FundAllocationSuggestion(
                            fromAccount = surplusAcc,
                            toAccount = shortfallAcc,
                            transferAmount = transferAmount,
                            reasonEn = "Move ${LanguageHelper.formatCurrency(transferAmount, com.example.data.model.LanguageMode.ENGLISH)} from ${surplusAcc.nameEn} to ${shortfallAcc.nameEn} to cover shortfall",
                            reasonBn = "${shortfallAcc.nameBn.ifEmpty { shortfallAcc.nameEn }}-এর ঘাটতি মেটাতে ${surplusAcc.nameBn.ifEmpty { surplusAcc.nameEn }} থেকে ${LanguageHelper.formatCurrency(transferAmount, com.example.data.model.LanguageMode.BANGLA)} স্থানান্তর করুন"
                        )
                    )
                    remainingNeed -= transferAmount
                    surplusPool[j] = surplusAcc to (availableSurplus - transferAmount)
                }
            }
        }

        val totalReq = accountAnalyses.sumOf { it.requiredExpenseAmount }
        val totalAvail = accountAnalyses.sumOf { it.availableAmount }
        val totalShortfall = accountAnalyses.sumOf { it.shortfall }
        val totalSurplus = accountAnalyses.sumOf { it.surplus }
        val shortCount = accountAnalyses.count { it.isShortfall }
        val surpCount = accountAnalyses.count { it.isSurplus }

        return PaymentSourceAnalysisOverview(
            year = year,
            month = month,
            calculationBasis = basis,
            totalRequired = totalReq,
            totalAvailable = totalAvail,
            totalShortfall = totalShortfall,
            totalSurplus = totalSurplus,
            accountsNeedingFundsCount = shortCount,
            accountsWithSurplusCount = surpCount,
            accountAnalyses = accountAnalyses,
            transferSuggestions = transferSuggestions
        )
    }
}
