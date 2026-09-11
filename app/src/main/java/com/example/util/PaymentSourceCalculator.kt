package com.example.util

import com.example.data.model.Account
import com.example.data.model.AccountObligationAnalysis
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
        recurringBills: List<RecurringBill> = emptyList(),
        selectedPaymentSourceIds: Set<Long>? = null,
        accountObligations: List<AccountObligation> = emptyList()
    ): PaymentSourceAnalysisOverview {
        // Balance map for quick lookup
        val balanceMap = accountsWithBalances.associate { it.account.id to it.currentBalance }

        // Filter valid accounts: if user has explicit selectedPaymentSourceIds, strictly use them; otherwise use active leaf accounts
        val validAccounts = if (selectedPaymentSourceIds != null && selectedPaymentSourceIds.isNotEmpty()) {
            allAccounts.filter { selectedPaymentSourceIds.contains(it.id) && it.isActive }
        } else {
            allAccounts.filter { it.parentId != null && it.isActive && (it.type == AccountType.ASSET || it.type == AccountType.LIABILITY) }
                .ifEmpty { allAccounts.filter { it.isActive } }
        }
        val validAccountsMap = validAccounts.associateBy { it.id }

        // Month boundary timestamps
        val startOfMonthMs = DateUtils.getStartOfMonth(year, month)
        val endOfMonthMs = DateUtils.getEndOfMonth(year, month)

        // Transactions in this month
        val monthTxs = allTransactions.filter { it.transaction.dateEpochMs in startOfMonthMs..endOfMonthMs }

        // Monthly budget entries map: strictly for the requested year and month (or default if year==0/month==0)
        val monthBudgets = monthlyBudgets.filter { (it.year == 0 || it.year == year) && (it.month == 0 || it.month == month) }
        val budgetMap = monthBudgets.associateBy { "${it.itemType}_${it.itemId}" }

        // Explicit category-account allocation budgets from MonthlyBudget (itemType = "ALLOC_${categoryId}", itemId = accountId)
        val explicitAllocations = monthBudgets.filter {
            it.itemType.startsWith("ALLOC_") && it.isEnabled && it.budgetedAmount > 0
        }
        val allocationsByCatId = explicitAllocations.groupBy {
            it.itemType.removePrefix("ALLOC_").toLongOrNull() ?: 0L
        }

        // Categories map
        val categoryMap = allCategories.associateBy { it.id }

        // 1. Transaction spend / receive per (categoryId, accountId) in current month
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
            val fromMonth = catToAccountFromMonth[catId]
            if (fromMonth != null && validAccountsMap.containsKey(fromMonth)) return fromMonth
            val fromHist = catToAccountHistorical[catId]
            if (fromHist != null && validAccountsMap.containsKey(fromHist)) return fromHist
            return defaultFallbackAccountId
        }

        // 3. Build Account-Itemized Expenses & Incomes map
        val accountExpensesMap = mutableMapOf<Long, MutableList<AccountRequirementItem>>()
        val accountIncomesMap = mutableMapOf<Long, MutableList<AccountRequirementItem>>()

        validAccounts.forEach { acc ->
            accountExpensesMap[acc.id] = mutableListOf()
            accountIncomesMap[acc.id] = mutableListOf()
        }

        // --- EXPENSE CATEGORIES PROCESSING ---
        val parentExpenseCatIdsWithChildren = allCategories
            .filter { it.type == CategoryType.EXPENSE && it.parentId != null }
            .mapNotNull { it.parentId }
            .toSet()

        val expenseCategories = allCategories.filter {
            it.type == CategoryType.EXPENSE && it.isActive &&
            (it.parentId != null || !parentExpenseCatIdsWithChildren.contains(it.id))
        }.toMutableList()

        val directParentsExp = allCategories.filter {
            it.type == CategoryType.EXPENSE && it.parentId == null && parentExpenseCatIdsWithChildren.contains(it.id) &&
            (budgetMap.containsKey("EXPENSE_${it.id}") || monthTxs.any { tx -> tx.transaction.categoryId == it.id && tx.transaction.subCategoryId == null })
        }
        for (dp in directParentsExp) {
            if (expenseCategories.none { it.id == dp.id }) {
                expenseCategories.add(dp)
            }
        }

        val expenseAllocationsList = mutableListOf<CategoryAllocationAnalysis>()

        for (cat in expenseCategories) {
            val explicitCatAllocs = allocationsByCatId[cat.id] ?: emptyList()
            val totalExplicitBudget = explicitCatAllocs.sumOf { it.budgetedAmount }

            val budgetEntry = budgetMap["EXPENSE_${cat.id}"]
            val budgetMakerAmount = if (budgetEntry != null) {
                if (budgetEntry.isEnabled) budgetEntry.budgetedAmount else 0.0
            } else {
                cat.budgetLimit
            }

            val effectiveCategoryBudget = if (totalExplicitBudget > 0) {
                maxOf(budgetMakerAmount, totalExplicitBudget)
            } else {
                budgetMakerAmount
            }

            val totalCatActualSpent = monthTxs.filter {
                val matchesSub = it.transaction.subCategoryId == cat.id
                val matchesCat = it.transaction.categoryId == cat.id && it.transaction.subCategoryId == null
                (matchesSub || matchesCat) && it.transaction.type == TransactionType.EXPENSE
            }.sumOf { it.transaction.amount }

            val totalCatRemaining = maxOf(0.0, effectiveCategoryBudget - totalCatActualSpent)
            val catSplits = mutableListOf<CategoryAccountSplit>()

            if (explicitCatAllocs.isNotEmpty()) {
                val splitCount = explicitCatAllocs.size
                for (alloc in explicitCatAllocs) {
                    val accId = alloc.itemId
                    val acc = validAccountsMap[accId] ?: continue
                    val allocatedAmt = alloc.budgetedAmount
                    val spentInThisAcc = spentByCatAndAcc[cat.id to accId] ?: 0.0
                    val remainingInThisAcc = maxOf(0.0, allocatedAmt - spentInThisAcc)

                    val reqAmt = if (basis == RequirementCalculationBasis.BUDGET_AMOUNT) allocatedAmt else remainingInThisAcc

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
                                totalCategoryBudget = effectiveCategoryBudget,
                                splitAccountCount = splitCount
                            )
                        )
                    }

                    val pct = if (effectiveCategoryBudget > 0) (allocatedAmt / effectiveCategoryBudget * 100) else 0.0
                    catSplits.add(
                        CategoryAccountSplit(
                            account = acc,
                            allocatedAmount = allocatedAmt,
                            actualSpent = spentInThisAcc,
                            remaining = remainingInThisAcc,
                            percentageOfCategory = pct
                        )
                    )
                }

                // Extra spent in other accounts
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
                                totalCategoryBudget = effectiveCategoryBudget,
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
                    }
                }
            } else {
                val accountsWithSpendForCat = spentByCatAndAcc.keys.filter { it.first == cat.id }.map { it.second }.filter { validAccountsMap.containsKey(it) }.distinct()

                if (accountsWithSpendForCat.size > 1) {
                    val sumSpentInCat = accountsWithSpendForCat.sumOf { spentByCatAndAcc[cat.id to it] ?: 0.0 }
                    for (accId in accountsWithSpendForCat) {
                        val acc = validAccountsMap[accId] ?: continue
                        val spentInAcc = spentByCatAndAcc[cat.id to accId] ?: 0.0
                        val proportion = if (sumSpentInCat > 0) (spentInAcc / sumSpentInCat) else (1.0 / accountsWithSpendForCat.size)
                        val allocatedBudget = if (effectiveCategoryBudget > 0) (effectiveCategoryBudget * proportion) else 0.0
                        val remainingInAcc = maxOf(0.0, allocatedBudget - spentInAcc)
                        val reqAmt = if (basis == RequirementCalculationBasis.BUDGET_AMOUNT) (if (allocatedBudget > 0) allocatedBudget else spentInAcc) else remainingInAcc

                        accountExpensesMap[accId]?.add(
                            AccountRequirementItem(
                                title = cat.nameEn,
                                amount = reqAmt,
                                originalBudgetOrExpected = allocatedBudget,
                                actualSpentOrReceived = spentInAcc,
                                remaining = remainingInAcc,
                                isRecurring = false,
                                isExpense = true,
                                categoryId = cat.id,
                                iconName = cat.iconName,
                                colorHex = cat.colorHex,
                                isMultiAccountSplit = true,
                                totalCategoryBudget = effectiveCategoryBudget,
                                splitAccountCount = accountsWithSpendForCat.size
                            )
                        )
                        catSplits.add(
                            CategoryAccountSplit(
                                account = acc,
                                allocatedAmount = allocatedBudget,
                                actualSpent = spentInAcc,
                                remaining = remainingInAcc,
                                percentageOfCategory = proportion * 100
                            )
                        )
                    }
                } else {
                    val mappedAccId = accountsWithSpendForCat.firstOrNull() ?: resolveAccountForCategory(cat.id)
                    val mappedAcc = validAccountsMap[mappedAccId]
                    val spentInThisAcc = spentByCatAndAcc[cat.id to mappedAccId] ?: 0.0
                    val originalBudget = effectiveCategoryBudget
                    val remaining = maxOf(0.0, originalBudget - spentInThisAcc)

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
                                    allocatedAmount = originalBudget,
                                    actualSpent = spentInThisAcc,
                                    remaining = remaining,
                                    percentageOfCategory = 100.0
                                )
                            )
                        }
                    }
                }
            }

            if (effectiveCategoryBudget > 0 || totalCatActualSpent > 0 || catSplits.isNotEmpty()) {
                expenseAllocationsList.add(
                    CategoryAllocationAnalysis(
                        category = cat,
                        totalBudgetOrRequired = if (basis == RequirementCalculationBasis.BUDGET_AMOUNT) effectiveCategoryBudget else totalCatRemaining,
                        totalBudgeted = effectiveCategoryBudget,
                        totalActualSpent = totalCatActualSpent,
                        totalRemaining = totalCatRemaining,
                        accountSplits = catSplits.sortedByDescending { it.allocatedAmount },
                        isExpense = true
                    )
                )
            }
        }

        // Recurring Bills (Expenses)
        for (bill in recurringBills.filter { it.type == TransactionType.EXPENSE }) {
            val accId = bill.creditAccountId ?: continue
            if (!validAccountsMap.containsKey(accId)) continue
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

        // --- INCOME CATEGORIES PROCESSING ---
        val parentIncomeCatIdsWithChildren = allCategories
            .filter { it.type == CategoryType.INCOME && it.parentId != null }
            .mapNotNull { it.parentId }
            .toSet()

        val incomeCategories = allCategories.filter {
            it.type == CategoryType.INCOME && it.isActive &&
            (it.parentId != null || !parentIncomeCatIdsWithChildren.contains(it.id))
        }.ifEmpty { allCategories.filter { it.type == CategoryType.INCOME && it.isActive } }.toMutableList()

        val directParentsInc = allCategories.filter {
            it.type == CategoryType.INCOME && it.parentId == null && parentIncomeCatIdsWithChildren.contains(it.id) &&
            (budgetMap.containsKey("INCOME_${it.id}") || monthTxs.any { tx -> tx.transaction.categoryId == it.id && tx.transaction.subCategoryId == null })
        }
        for (dp in directParentsInc) {
            if (incomeCategories.none { it.id == dp.id }) {
                incomeCategories.add(dp)
            }
        }

        val incomeAllocationsList = mutableListOf<CategoryAllocationAnalysis>()

        for (cat in incomeCategories) {
            val explicitCatAllocs = allocationsByCatId[cat.id] ?: emptyList()
            val totalExplicitBudget = explicitCatAllocs.sumOf { it.budgetedAmount }

            val budgetEntry = budgetMap["INCOME_${cat.id}"]
            val budgetLimit = if (budgetEntry != null) {
                if (budgetEntry.isEnabled) budgetEntry.budgetedAmount else 0.0
            } else {
                cat.budgetLimit
            }

            val effectiveCategoryBudget = if (totalExplicitBudget > 0) {
                maxOf(budgetLimit, totalExplicitBudget)
            } else {
                budgetLimit
            }

            val totalCatActualReceived = monthTxs.filter {
                val matchesSub = it.transaction.subCategoryId == cat.id
                val matchesCat = it.transaction.categoryId == cat.id && it.transaction.subCategoryId == null
                (matchesSub || matchesCat) && it.transaction.type == TransactionType.INCOME
            }.sumOf { it.transaction.amount }

            val totalCatRemaining = maxOf(0.0, effectiveCategoryBudget - totalCatActualReceived)
            val incSplits = mutableListOf<CategoryAccountSplit>()

            if (explicitCatAllocs.isNotEmpty()) {
                val splitCount = explicitCatAllocs.size
                for (alloc in explicitCatAllocs) {
                    val accId = alloc.itemId
                    val acc = validAccountsMap[accId] ?: continue
                    val allocatedAmt = alloc.budgetedAmount
                    val receivedInThisAcc = receivedByCatAndAcc[cat.id to accId] ?: 0.0
                    val remainingInThisAcc = maxOf(0.0, allocatedAmt - receivedInThisAcc)
                    val expectedAmt = if (basis == RequirementCalculationBasis.BUDGET_AMOUNT) allocatedAmt else remainingInThisAcc

                    if (expectedAmt > 0 || receivedInThisAcc > 0 || allocatedAmt > 0) {
                        accountIncomesMap[accId]?.add(
                            AccountRequirementItem(
                                title = cat.nameEn,
                                amount = expectedAmt,
                                originalBudgetOrExpected = allocatedAmt,
                                actualSpentOrReceived = receivedInThisAcc,
                                remaining = remainingInThisAcc,
                                isRecurring = false,
                                isExpense = false,
                                categoryId = cat.id,
                                iconName = cat.iconName,
                                colorHex = cat.colorHex,
                                isMultiAccountSplit = splitCount > 1,
                                totalCategoryBudget = effectiveCategoryBudget,
                                splitAccountCount = splitCount
                            )
                        )
                    }

                    val pct = if (effectiveCategoryBudget > 0) (allocatedAmt / effectiveCategoryBudget * 100) else 0.0
                    incSplits.add(
                        CategoryAccountSplit(
                            account = acc,
                            allocatedAmount = allocatedAmt,
                            actualSpent = receivedInThisAcc,
                            remaining = remainingInThisAcc,
                            percentageOfCategory = pct
                        )
                    )
                }
            } else {
                val mappedAccId = resolveAccountForCategory(cat.id)
                val mappedAcc = validAccountsMap[mappedAccId]
                val receivedInThisAcc = receivedByCatAndAcc[cat.id to mappedAccId] ?: 0.0
                val originalBudget = effectiveCategoryBudget
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
                    if (mappedAcc != null) {
                        incSplits.add(
                            CategoryAccountSplit(
                                account = mappedAcc,
                                allocatedAmount = originalBudget,
                                actualSpent = receivedInThisAcc,
                                remaining = remaining,
                                percentageOfCategory = 100.0
                            )
                        )
                    }
                }
            }

            if (effectiveCategoryBudget > 0 || totalCatActualReceived > 0 || incSplits.isNotEmpty()) {
                incomeAllocationsList.add(
                    CategoryAllocationAnalysis(
                        category = cat,
                        totalBudgetOrRequired = if (basis == RequirementCalculationBasis.BUDGET_AMOUNT) effectiveCategoryBudget else totalCatRemaining,
                        totalBudgeted = effectiveCategoryBudget,
                        totalActualSpent = totalCatActualReceived,
                        totalRemaining = totalCatRemaining,
                        accountSplits = incSplits.sortedByDescending { it.allocatedAmount },
                        isExpense = false
                    )
                )
            }
        }

        // Recurring Bills (Incomes)
        for (bill in recurringBills.filter { it.type == TransactionType.INCOME }) {
            val accId = bill.debitAccountId ?: continue
            if (!validAccountsMap.containsKey(accId)) continue
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

        // --- OTHER ACCOUNTS ALLOCATION PROCESSING (Treated like Categories) ---
        val allAccountsMap = allAccounts.associateBy { it.id }
        val otherAccountAllocationsList = mutableListOf<com.example.data.model.OtherAccountAllocationAnalysis>()

        // Explicit other-account allocations from MonthlyBudget (itemType = "ALLOC_ACC_${otherAccountId}", itemId = paymentSourceAccountId)
        val explicitOtherAccAllocs = monthBudgets.filter {
            it.itemType.startsWith("ALLOC_ACC_") && it.isEnabled && it.budgetedAmount > 0
        }
        val allocationsByOtherAccId = explicitOtherAccAllocs.groupBy {
            it.itemType.removePrefix("ALLOC_ACC_").toLongOrNull() ?: 0L
        }

        // Transactions between other accounts and payment sources in this month
        val settledByOtherAccAndPaymentSource = mutableMapOf<Pair<Long, Long>, Double>() // Outflow from payment source to other account
        val receivedByOtherAccAndPaymentSource = mutableMapOf<Pair<Long, Long>, Double>() // Inflow to payment source from other account

        for (tx in monthTxs) {
            val fromAccId = tx.transaction.creditAccountId
            val toAccId = tx.transaction.debitAccountId
            if (fromAccId != null && toAccId != null) {
                if (validAccountsMap.containsKey(fromAccId) && !validAccountsMap.containsKey(toAccId)) {
                    val key = toAccId to fromAccId
                    settledByOtherAccAndPaymentSource[key] = (settledByOtherAccAndPaymentSource[key] ?: 0.0) + tx.transaction.amount
                }
                if (!validAccountsMap.containsKey(fromAccId) && validAccountsMap.containsKey(toAccId)) {
                    val key = fromAccId to toAccId
                    receivedByOtherAccAndPaymentSource[key] = (receivedByOtherAccAndPaymentSource[key] ?: 0.0) + tx.transaction.amount
                }
            }
        }

        // All non-payment-source active accounts (Other Accounts: Persons, Loans, Liabilities, Receivables)
        val otherAccounts = allAccounts.filter {
            !validAccountsMap.containsKey(it.id) && it.isActive &&
            (it.parentId != null || allAccounts.none { child -> child.parentId == it.id })
        }

        val accountObligationAnalyses = mutableListOf<AccountObligationAnalysis>()

        for (ob in accountObligations) {
            val sourceAcc = validAccountsMap[ob.sourceAccountId] ?: continue
            val targetAcc = allAccountsMap[ob.targetAccountId] ?: continue

            accountObligationAnalyses.add(
                AccountObligationAnalysis(
                    id = ob.id,
                    sourceAccount = sourceAcc,
                    targetAccount = targetAcc,
                    amount = ob.amount,
                    isExpense = ob.isExpense,
                    note = ob.note
                )
            )
        }

        for (otherAcc in otherAccounts) {
            val isExpense = otherAcc.type == AccountType.LIABILITY ||
                    accountObligations.any { it.targetAccountId == otherAcc.id && it.isExpense } ||
                    otherAcc.type == AccountType.EQUITY

            val explicitSplits = allocationsByOtherAccId[otherAcc.id] ?: emptyList()
            val obligationsForAcc = accountObligations.filter { it.targetAccountId == otherAcc.id }

            val totalExplicitBudget = explicitSplits.sumOf { it.budgetedAmount }
            val totalObligationBudget = obligationsForAcc.sumOf { it.amount }

            val currentBal = balanceMap[otherAcc.id] ?: 0.0
            val defaultBudget = if (isExpense) {
                if (currentBal < 0) -currentBal else if (otherAcc.type == AccountType.LIABILITY) Math.abs(currentBal) else 0.0
            } else {
                if (currentBal > 0) currentBal else 0.0
            }

            val effectiveBudget = when {
                totalExplicitBudget > 0 -> totalExplicitBudget
                totalObligationBudget > 0 -> totalObligationBudget
                else -> defaultBudget
            }

            val totalActualSettled = if (isExpense) {
                settledByOtherAccAndPaymentSource.filter { it.key.first == otherAcc.id }.values.sum()
            } else {
                receivedByOtherAccAndPaymentSource.filter { it.key.first == otherAcc.id }.values.sum()
            }

            val totalRemaining = if (basis == RequirementCalculationBasis.REMAINING_AMOUNT) {
                maxOf(0.0, effectiveBudget - totalActualSettled)
            } else {
                maxOf(effectiveBudget, totalActualSettled)
            }

            val accSplits = mutableListOf<CategoryAccountSplit>()

            if (explicitSplits.isNotEmpty()) {
                for (splitEntry in explicitSplits) {
                    val srcAcc = validAccountsMap[splitEntry.itemId] ?: continue
                    val allocatedAmt = splitEntry.budgetedAmount
                    val actualSettled = if (isExpense) {
                        settledByOtherAccAndPaymentSource[otherAcc.id to srcAcc.id] ?: 0.0
                    } else {
                        receivedByOtherAccAndPaymentSource[otherAcc.id to srcAcc.id] ?: 0.0
                    }
                    val remainingInSrc = if (basis == RequirementCalculationBasis.REMAINING_AMOUNT) {
                        maxOf(0.0, allocatedAmt - actualSettled)
                    } else {
                        maxOf(allocatedAmt, actualSettled)
                    }
                    val reqAmt = if (basis == RequirementCalculationBasis.BUDGET_AMOUNT) {
                        if (allocatedAmt > 0) allocatedAmt else actualSettled
                    } else {
                        remainingInSrc
                    }

                    if (reqAmt > 0 || actualSettled > 0 || allocatedAmt > 0) {
                        if (isExpense) {
                            accountExpensesMap[srcAcc.id]?.add(
                                AccountRequirementItem(
                                    title = otherAcc.nameEn,
                                    amount = reqAmt,
                                    originalBudgetOrExpected = allocatedAmt,
                                    actualSpentOrReceived = actualSettled,
                                    remaining = remainingInSrc,
                                    isRecurring = false,
                                    isExpense = true,
                                    iconName = otherAcc.iconName,
                                    colorHex = otherAcc.colorHex,
                                    isMultiAccountSplit = explicitSplits.size > 1,
                                    totalCategoryBudget = effectiveBudget,
                                    splitAccountCount = explicitSplits.size,
                                    isAccountObligation = true,
                                    linkedAccountId = otherAcc.id
                                )
                            )
                        } else {
                            accountIncomesMap[srcAcc.id]?.add(
                                AccountRequirementItem(
                                    title = otherAcc.nameEn,
                                    amount = reqAmt,
                                    originalBudgetOrExpected = allocatedAmt,
                                    actualSpentOrReceived = actualSettled,
                                    remaining = remainingInSrc,
                                    isRecurring = false,
                                    isExpense = false,
                                    iconName = otherAcc.iconName,
                                    colorHex = otherAcc.colorHex,
                                    isMultiAccountSplit = explicitSplits.size > 1,
                                    totalCategoryBudget = effectiveBudget,
                                    splitAccountCount = explicitSplits.size,
                                    isAccountObligation = true,
                                    linkedAccountId = otherAcc.id
                                )
                            )
                        }
                    }

                    val pct = if (effectiveBudget > 0) (allocatedAmt / effectiveBudget * 100) else 0.0
                    accSplits.add(
                        CategoryAccountSplit(
                            account = srcAcc,
                            allocatedAmount = allocatedAmt,
                            actualSpent = actualSettled,
                            remaining = remainingInSrc,
                            percentageOfCategory = pct
                        )
                    )
                }
            } else if (obligationsForAcc.isNotEmpty()) {
                for (ob in obligationsForAcc) {
                    val srcAcc = validAccountsMap[ob.sourceAccountId] ?: continue
                    val allocatedAmt = ob.amount
                    val actualSettled = if (isExpense) {
                        settledByOtherAccAndPaymentSource[otherAcc.id to srcAcc.id] ?: 0.0
                    } else {
                        receivedByOtherAccAndPaymentSource[otherAcc.id to srcAcc.id] ?: 0.0
                    }
                    val remainingInSrc = if (basis == RequirementCalculationBasis.REMAINING_AMOUNT) {
                        maxOf(0.0, allocatedAmt - actualSettled)
                    } else {
                        maxOf(allocatedAmt, actualSettled)
                    }
                    val reqAmt = if (basis == RequirementCalculationBasis.BUDGET_AMOUNT) {
                        if (allocatedAmt > 0) allocatedAmt else actualSettled
                    } else {
                        remainingInSrc
                    }

                    if (isExpense) {
                        accountExpensesMap[srcAcc.id]?.add(
                            AccountRequirementItem(
                                title = otherAcc.nameEn,
                                amount = reqAmt,
                                originalBudgetOrExpected = allocatedAmt,
                                actualSpentOrReceived = actualSettled,
                                remaining = remainingInSrc,
                                isRecurring = false,
                                isExpense = true,
                                iconName = otherAcc.iconName,
                                colorHex = otherAcc.colorHex,
                                isMultiAccountSplit = obligationsForAcc.size > 1,
                                totalCategoryBudget = effectiveBudget,
                                splitAccountCount = obligationsForAcc.size,
                                isAccountObligation = true,
                                linkedAccountId = otherAcc.id
                            )
                        )
                    } else {
                        accountIncomesMap[srcAcc.id]?.add(
                            AccountRequirementItem(
                                title = otherAcc.nameEn,
                                amount = reqAmt,
                                originalBudgetOrExpected = allocatedAmt,
                                actualSpentOrReceived = actualSettled,
                                remaining = remainingInSrc,
                                isRecurring = false,
                                isExpense = false,
                                iconName = otherAcc.iconName,
                                colorHex = otherAcc.colorHex,
                                isMultiAccountSplit = obligationsForAcc.size > 1,
                                totalCategoryBudget = effectiveBudget,
                                splitAccountCount = obligationsForAcc.size,
                                isAccountObligation = true,
                                linkedAccountId = otherAcc.id
                            )
                        )
                    }

                    val pct = if (effectiveBudget > 0) (allocatedAmt / effectiveBudget * 100) else 0.0
                    accSplits.add(
                        CategoryAccountSplit(
                            account = srcAcc,
                            allocatedAmount = allocatedAmt,
                            actualSpent = actualSettled,
                            remaining = remainingInSrc,
                            percentageOfCategory = pct
                        )
                    )
                }
            } else {
                // Find fallback payment source from history or default
                val defaultSourceAcc = validAccounts.firstOrNull { it.type == AccountType.ASSET } ?: validAccounts.firstOrNull()
                if (defaultSourceAcc != null && (effectiveBudget > 0 || totalActualSettled > 0)) {
                    val reqAmt = if (basis == RequirementCalculationBasis.BUDGET_AMOUNT) {
                        if (effectiveBudget > 0) effectiveBudget else totalActualSettled
                    } else {
                        totalRemaining
                    }

                    if (reqAmt > 0 || totalActualSettled > 0 || effectiveBudget > 0) {
                        if (isExpense) {
                            accountExpensesMap[defaultSourceAcc.id]?.add(
                                AccountRequirementItem(
                                    title = otherAcc.nameEn,
                                    amount = reqAmt,
                                    originalBudgetOrExpected = effectiveBudget,
                                    actualSpentOrReceived = totalActualSettled,
                                    remaining = totalRemaining,
                                    isRecurring = false,
                                    isExpense = true,
                                    iconName = otherAcc.iconName,
                                    colorHex = otherAcc.colorHex,
                                    isMultiAccountSplit = false,
                                    totalCategoryBudget = effectiveBudget,
                                    splitAccountCount = 1,
                                    isAccountObligation = true,
                                    linkedAccountId = otherAcc.id
                                )
                            )
                        } else {
                            accountIncomesMap[defaultSourceAcc.id]?.add(
                                AccountRequirementItem(
                                    title = otherAcc.nameEn,
                                    amount = reqAmt,
                                    originalBudgetOrExpected = effectiveBudget,
                                    actualSpentOrReceived = totalActualSettled,
                                    remaining = totalRemaining,
                                    isRecurring = false,
                                    isExpense = false,
                                    iconName = otherAcc.iconName,
                                    colorHex = otherAcc.colorHex,
                                    isMultiAccountSplit = false,
                                    totalCategoryBudget = effectiveBudget,
                                    splitAccountCount = 1,
                                    isAccountObligation = true,
                                    linkedAccountId = otherAcc.id
                                )
                            )
                        }
                    }

                    accSplits.add(
                        CategoryAccountSplit(
                            account = defaultSourceAcc,
                            allocatedAmount = effectiveBudget,
                            actualSpent = totalActualSettled,
                            remaining = totalRemaining,
                            percentageOfCategory = 100.0
                        )
                    )
                }
            }

            otherAccountAllocationsList.add(
                com.example.data.model.OtherAccountAllocationAnalysis(
                    account = otherAcc,
                    totalBudgetOrRequired = if (basis == RequirementCalculationBasis.BUDGET_AMOUNT) effectiveBudget else totalRemaining,
                    totalBudgeted = effectiveBudget,
                    totalActualSettled = totalActualSettled,
                    totalRemaining = totalRemaining,
                    accountSplits = accSplits.sortedByDescending { it.allocatedAmount },
                    isExpense = isExpense
                )
            )
        }

        // 4. Build AccountRequirementAnalysis for each valid account
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
            categoryAllocations = expenseAllocationsList.sortedByDescending { it.totalBudgetOrRequired },
            incomeAllocations = incomeAllocationsList.sortedByDescending { it.totalBudgetOrRequired },
            otherAccountAllocations = otherAccountAllocationsList.sortedByDescending { it.totalBudgetOrRequired },
            accountObligationAllocations = accountObligationAnalyses,
            transferSuggestions = transferSuggestions
        )
    }
}
