package com.example.util

import com.example.data.model.Account
import com.example.data.model.AccountRequirementAnalysis
import com.example.data.model.AccountRequirementItem
import com.example.data.model.AccountType
import com.example.data.model.Category
import com.example.data.model.CategoryAccountSplit
import com.example.data.model.CategoryAllocationAnalysis
import com.example.data.model.CategoryType
import com.example.data.model.FundAllocationSuggestion
import com.example.data.model.MonthlyBudget
import com.example.data.model.PaymentSourceAnalysisOverview
import com.example.data.model.RecurringBill
import com.example.data.model.RequirementCalculationBasis
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
        val validAccountsMap = validAccounts.associateBy { it.id }

        // Month boundary timestamps
        val startOfMonthMs = DateUtils.getStartOfMonth(year, month)
        val endOfMonthMs = DateUtils.getEndOfMonth(year, month)

        // Transactions in this month
        val monthTxs = allTransactions.filter { it.transaction.dateEpochMs in startOfMonthMs..endOfMonthMs }

        // Monthly budget entries map
        val budgetMap = monthlyBudgets.associateBy { "${it.itemType}_${it.itemId}" }

        // Explicit category-account allocation budgets from MonthlyBudget (itemType = "ALLOC_${categoryId}", itemId = accountId)
        val explicitAllocations = monthlyBudgets.filter {
            it.itemType.startsWith("ALLOC_") && it.isEnabled && it.budgetedAmount > 0
        }
        val allocationsByCatId = explicitAllocations.groupBy {
            it.itemType.removePrefix("ALLOC_").toLongOrNull() ?: 0L
        }

        // Categories map
        val categoryMap = allCategories.associateBy { it.id }

        // 1. Transaction spend per (categoryId, accountId) in current month
        val spentByCatAndAcc = mutableMapOf<Pair<Long, Long>, Double>()
        val receivedByCatAndAcc = mutableMapOf<Pair<Long, Long>, Double>()

        for (tx in monthTxs) {
            val catId = tx.transaction.subCategoryId ?: tx.transaction.categoryId
            if (catId != null) {
                if (tx.transaction.type == TransactionType.EXPENSE && tx.transaction.creditAccountId != null) {
                    val key = catId to tx.transaction.creditAccountId
                    spentByCatAndAcc[key] = (spentByCatAndAcc[key] ?: 0.0) + tx.transaction.amount
                } else if (tx.transaction.type == TransactionType.INCOME && tx.transaction.debitAccountId != null) {
                    val key = catId to tx.transaction.debitAccountId
                    receivedByCatAndAcc[key] = (receivedByCatAndAcc[key] ?: 0.0) + tx.transaction.amount
                }
            }
        }

        // 2. Historical account resolution fallback
        val catToAccountFromMonth = mutableMapOf<Long, Long>()
        for (tx in monthTxs) {
            val catId = tx.transaction.subCategoryId ?: tx.transaction.categoryId
            val accId = if (tx.transaction.type == TransactionType.EXPENSE) tx.transaction.creditAccountId else tx.transaction.debitAccountId
            if (catId != null && accId != null) {
                catToAccountFromMonth[catId] = accId
            }
        }

        val catToAccountHistorical = mutableMapOf<Long, Long>()
        for (tx in allTransactions) {
            val catId = tx.transaction.subCategoryId ?: tx.transaction.categoryId
            val accId = if (tx.transaction.type == TransactionType.EXPENSE) tx.transaction.creditAccountId else tx.transaction.debitAccountId
            if (catId != null && accId != null && !catToAccountHistorical.containsKey(catId)) {
                catToAccountHistorical[catId] = accId
            }
        }

        val defaultFallbackAccountId = validAccounts.firstOrNull { it.type == AccountType.ASSET }?.id
            ?: validAccounts.firstOrNull()?.id ?: 0L

        fun resolveAccountForCategory(catId: Long): Long {
            return catToAccountFromMonth[catId]
                ?: catToAccountHistorical[catId]
                ?: defaultFallbackAccountId
        }

        // 3. Build Account-Itemized Expenses & Incomes map
        val accountExpensesMap = mutableMapOf<Long, MutableList<AccountRequirementItem>>()
        val accountIncomesMap = mutableMapOf<Long, MutableList<AccountRequirementItem>>()

        validAccounts.forEach { acc ->
            accountExpensesMap[acc.id] = mutableListOf()
            accountIncomesMap[acc.id] = mutableListOf()
        }

        // Expense Categories Processing
        val expenseCategories = allCategories.filter { it.type == CategoryType.EXPENSE && it.parentId != null }
            .ifEmpty { allCategories.filter { it.type == CategoryType.EXPENSE } }

        val categoryAllocationsList = mutableListOf<CategoryAllocationAnalysis>()

        for (cat in expenseCategories) {
            val explicitCatAllocs = allocationsByCatId[cat.id] ?: emptyList()
            val budgetEntry = budgetMap["EXPENSE_${cat.id}"]
            val isGeneralBudgetEnabled = budgetEntry?.isEnabled ?: (cat.budgetLimit > 0)
            val generalBudgetLimit = if (isGeneralBudgetEnabled) (budgetEntry?.budgetedAmount ?: cat.budgetLimit) else 0.0

            val catSplits = mutableListOf<CategoryAccountSplit>()
            var totalCatBudgetOrReq = 0.0
            var totalCatActualSpent = 0.0

            if (explicitCatAllocs.isNotEmpty()) {
                // Multi-Account / Explicit Allocations exist for this category
                val splitCount = explicitCatAllocs.size
                val totalExplicitBudget = explicitCatAllocs.sumOf { it.budgetedAmount }

                for (alloc in explicitCatAllocs) {
                    val accId = alloc.itemId
                    val acc = validAccountsMap[accId] ?: continue
                    val allocatedAmt = alloc.budgetedAmount
                    val spentInThisAcc = spentByCatAndAcc[cat.id to accId] ?: 0.0
                    val remainingInThisAcc = maxOf(0.0, allocatedAmt - spentInThisAcc)

                    val reqAmt = if (basis == RequirementCalculationBasis.BUDGET_AMOUNT) {
                        allocatedAmt
                    } else {
                        remainingInThisAcc
                    }

                    if (reqAmt > 0 || spentInThisAcc > 0 || allocatedAmt > 0) {
                        accountExpensesMap[accId]?.add(
                            AccountRequirementItem(
                                title = cat.nameEn,
                                amount = reqAmt,
                                originalBudgetOrExpected = allocatedAmt,
                                actualSpentOrReceived = spentInThisAcc,
                                remaining = remainingInThisAcc,
                                isRecurring = false,
                                isExpense = true,
                                categoryId = cat.id,
                                iconName = cat.iconName,
                                colorHex = cat.colorHex,
                                isMultiAccountSplit = splitCount > 1,
                                totalCategoryBudget = totalExplicitBudget,
                                splitAccountCount = splitCount
                            )
                        )
                    }

                    val pct = if (totalExplicitBudget > 0) (allocatedAmt / totalExplicitBudget * 100) else 0.0
                    catSplits.add(
                        CategoryAccountSplit(
                            account = acc,
                            allocatedAmount = allocatedAmt,
                            actualSpent = spentInThisAcc,
                            remaining = remainingInThisAcc,
                            percentageOfCategory = pct
                        )
                    )
                    totalCatBudgetOrReq += allocatedAmt
                    totalCatActualSpent += spentInThisAcc
                }

                // Also check if money was spent in an account that wasn't in explicit allocations
                val otherAccsWithSpend = spentByCatAndAcc.keys.filter { it.first == cat.id && explicitCatAllocs.none { a -> a.itemId == it.second } }
                for ((_, otherAccId) in otherAccsWithSpend) {
                    val otherAcc = validAccountsMap[otherAccId] ?: continue
                    val extraSpent = spentByCatAndAcc[cat.id to otherAccId] ?: 0.0
                    if (extraSpent > 0) {
                        accountExpensesMap[otherAccId]?.add(
                            AccountRequirementItem(
                                title = cat.nameEn,
                                amount = extraSpent,
                                originalBudgetOrExpected = 0.0,
                                actualSpentOrReceived = extraSpent,
                                remaining = 0.0,
                                isRecurring = false,
                                isExpense = true,
                                categoryId = cat.id,
                                iconName = cat.iconName,
                                colorHex = cat.colorHex,
                                isMultiAccountSplit = true,
                                totalCategoryBudget = totalExplicitBudget,
                                splitAccountCount = splitCount + otherAccsWithSpend.size
                            )
                        )
                        catSplits.add(
                            CategoryAccountSplit(
                                account = otherAcc,
                                allocatedAmount = 0.0,
                                actualSpent = extraSpent,
                                remaining = 0.0,
                                percentageOfCategory = 0.0
                            )
                        )
                        totalCatActualSpent += extraSpent
                    }
                }
            } else {
                // No explicit allocation: fallback to resolved account or actual spend accounts
                val accountsWithSpendForCat = spentByCatAndAcc.keys.filter { it.first == cat.id }.map { it.second }.distinct()

                if (accountsWithSpendForCat.size > 1) {
                    // Category was paid across multiple accounts during the month
                    val totalSpentForCat = accountsWithSpendForCat.sumOf { spentByCatAndAcc[cat.id to it] ?: 0.0 }
                    for (accId in accountsWithSpendForCat) {
                        val acc = validAccountsMap[accId] ?: continue
                        val spentInAcc = spentByCatAndAcc[cat.id to accId] ?: 0.0
                        val reqAmt = spentInAcc
                        accountExpensesMap[accId]?.add(
                            AccountRequirementItem(
                                title = cat.nameEn,
                                amount = reqAmt,
                                originalBudgetOrExpected = spentInAcc,
                                actualSpentOrReceived = spentInAcc,
                                remaining = 0.0,
                                isRecurring = false,
                                isExpense = true,
                                categoryId = cat.id,
                                iconName = cat.iconName,
                                colorHex = cat.colorHex,
                                isMultiAccountSplit = true,
                                totalCategoryBudget = maxOf(generalBudgetLimit, totalSpentForCat),
                                splitAccountCount = accountsWithSpendForCat.size
                            )
                        )
                        catSplits.add(
                            CategoryAccountSplit(
                                account = acc,
                                allocatedAmount = spentInAcc,
                                actualSpent = spentInAcc,
                                remaining = 0.0,
                                percentageOfCategory = if (totalSpentForCat > 0) (spentInAcc / totalSpentForCat * 100) else 0.0
                            )
                        )
                        totalCatBudgetOrReq += spentInAcc
                        totalCatActualSpent += spentInAcc
                    }
                } else {
                    // Single mapped account
                    val mappedAccId = accountsWithSpendForCat.firstOrNull() ?: resolveAccountForCategory(cat.id)
                    val mappedAcc = validAccountsMap[mappedAccId]
                    val spentInThisAcc = spentByCatAndAcc[cat.id to mappedAccId] ?: 0.0
                    val originalBudget = generalBudgetLimit
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
                        accountExpensesMap[mappedAccId]?.add(
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
                                colorHex = cat.colorHex,
                                isMultiAccountSplit = false,
                                totalCategoryBudget = originalBudget,
                                splitAccountCount = 1
                            )
                        )
                        if (mappedAcc != null) {
                            catSplits.add(
                                CategoryAccountSplit(
                                    account = mappedAcc,
                                    allocatedAmount = if (originalBudget > 0) originalBudget else spentInThisAcc,
                                    actualSpent = spentInThisAcc,
                                    remaining = remaining,
                                    percentageOfCategory = 100.0
                                )
                            )
                        }
                        totalCatBudgetOrReq = if (originalBudget > 0) originalBudget else spentInThisAcc
                        totalCatActualSpent = spentInThisAcc
                    }
                }
            }

            if (totalCatBudgetOrReq > 0 || totalCatActualSpent > 0 || catSplits.isNotEmpty()) {
                categoryAllocationsList.add(
                    CategoryAllocationAnalysis(
                        category = cat,
                        totalBudgetOrRequired = totalCatBudgetOrReq,
                        totalActualSpent = totalCatActualSpent,
                        totalRemaining = maxOf(0.0, totalCatBudgetOrReq - totalCatActualSpent),
                        accountSplits = catSplits.sortedByDescending { it.allocatedAmount }
                    )
                )
            }
        }

        // Recurring Bills (Expenses)
        for (bill in recurringBills.filter { it.type == TransactionType.EXPENSE }) {
            val accId = bill.creditAccountId ?: continue
            val billCat = bill.categoryId?.let { categoryMap[it] }
            val isPending = bill.nextDueDateEpochMs in startOfMonthMs..endOfMonthMs
            val billAmt = bill.amount

            val requiredBillAmt = if (basis == RequirementCalculationBasis.BUDGET_AMOUNT) {
                billAmt
            } else {
                if (isPending) billAmt else 0.0
            }

            if (requiredBillAmt > 0) {
                accountExpensesMap[accId]?.add(
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
            }
        }

        // Income Categories Processing
        val incomeCategories = allCategories.filter { it.type == CategoryType.INCOME && it.parentId != null }
            .ifEmpty { allCategories.filter { it.type == CategoryType.INCOME } }

        for (cat in incomeCategories) {
            val mappedAccId = resolveAccountForCategory(cat.id)
            val receivedInThisAcc = receivedByCatAndAcc[cat.id to mappedAccId] ?: 0.0
            val budgetEntry = budgetMap["INCOME_${cat.id}"]
            val isEnabled = budgetEntry?.isEnabled ?: (cat.budgetLimit > 0)
            val budgetLimit = if (isEnabled) (budgetEntry?.budgetedAmount ?: cat.budgetLimit) else 0.0

            val originalBudget = budgetLimit
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
                accountIncomesMap[mappedAccId]?.add(
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
            }
        }

        // Recurring Bills (Incomes)
        for (bill in recurringBills.filter { it.type == TransactionType.INCOME }) {
            val accId = bill.debitAccountId ?: continue
            val billCat = bill.categoryId?.let { categoryMap[it] }
            val isPending = bill.nextDueDateEpochMs in startOfMonthMs..endOfMonthMs
            val billAmt = bill.amount

            val expectedBillAmt = if (basis == RequirementCalculationBasis.BUDGET_AMOUNT) {
                billAmt
            } else {
                if (isPending) billAmt else 0.0
            }

            if (expectedBillAmt > 0) {
                accountIncomesMap[accId]?.add(
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
            }
        }

        // 4. Build AccountRequirementAnalysis for each account
        val accountAnalyses = validAccounts.map { account ->
            val accId = account.id
            val currentBal = balanceMap[accId] ?: 0.0
            val itemizedExpenses = accountExpensesMap[accId] ?: emptyList()
            val itemizedIncomes = accountIncomesMap[accId] ?: emptyList()

            val totalExpenseReq = itemizedExpenses.sumOf { it.amount }
            val totalIncomeExp = itemizedIncomes.sumOf { it.amount }

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

        // 5. Generate Fund Allocation Insights (Transfer Suggestions)
        val transferSuggestions = mutableListOf<FundAllocationSuggestion>()
        val surplusPool = accountAnalyses.filter { it.isSurplus }
            .map { it.account to it.surplus }
            .toMutableList()

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
            categoryAllocations = categoryAllocationsList.sortedByDescending { it.totalBudgetOrRequired },
            transferSuggestions = transferSuggestions
        )
    }
}
