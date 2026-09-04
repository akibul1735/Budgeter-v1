import {
  Account,
  AccountType,
  Category,
  CategoryType,
  MonthlyBudget,
  PaymentSourceAnalysisOverview,
  RecurringBill,
  RequirementCalculationBasis,
  Transaction,
  TransactionType,
  AccountRequirementItem,
  FundAllocationSuggestion,
  AccountCategoryAllocation,
} from '../types';

export class PaymentSourceCalculator {
  public static calculateOverview(
    accounts: Account[],
    accountBalances: Map<number, number>,
    categories: Category[],
    budgets: MonthlyBudget[],
    transactions: Transaction[],
    _recurringBills: RecurringBill[],
    year: number,
    month: number,
    basis: RequirementCalculationBasis,
    customAllocations: AccountCategoryAllocation[] = []
  ): PaymentSourceAnalysisOverview {
    const assetAccounts = accounts.filter((a) => a.type === AccountType.ASSET && !a.isArchived);
    const categoryMap = new Map<number, Category>(categories.map((c) => [c.id, c]));
    const budgetMap = new Map<number, number>();

    budgets
      .filter((b) => b.year === year && b.month === month)
      .forEach((b) => {
        budgetMap.set(b.categoryId, b.budgetAmount);
      });

    // Fallback to category default monthly budget if no specific MonthlyBudget record exists
    categories.forEach((c) => {
      if (!budgetMap.has(c.id) && c.monthlyBudget > 0) {
        budgetMap.set(c.id, c.monthlyBudget);
      }
    });

    // Monthly transactions
    const monthTransactions = transactions.filter((t) => {
      const d = new Date(t.dateEpochMs);
      return d.getFullYear() === year && d.getMonth() + 1 === month;
    });

    // Sum actual spent per category in this month
    const categorySpentMap = new Map<number, number>();
    const categoryIncomeMap = new Map<number, number>();

    monthTransactions.forEach((tx) => {
      if (tx.categoryId) {
        if (tx.type === TransactionType.EXPENSE) {
          categorySpentMap.set(tx.categoryId, (categorySpentMap.get(tx.categoryId) || 0) + tx.amount);
        } else if (tx.type === TransactionType.INCOME) {
          categoryIncomeMap.set(tx.categoryId, (categoryIncomeMap.get(tx.categoryId) || 0) + tx.amount);
        }
      }
    });

    const accountItems: AccountRequirementItem[] = assetAccounts.map((account) => {
      const currentBal = accountBalances.get(account.id) || account.initialBalance;

      // Find categories assigned to this account
      // Either via custom allocations or category.defaultAccountId
      const assignedCategoriesList: { category: Category; amount: number; spent: number }[] = [];

      let totalAssignedExpenseBudget = 0;
      let totalSpentExpense = 0;
      let totalExpectedIncome = 0;
      let totalActualIncome = 0;

      categories.forEach((cat) => {
        const fullBudget = budgetMap.get(cat.id) || 0;
        const actualSpent = categorySpentMap.get(cat.id) || 0;

        // Check if there are custom allocations for this category
        const allocations = customAllocations.filter(
          (a) => a.categoryId === cat.id && a.year === year && a.month === month
        );

        let allocatedBudgetForAccount = 0;
        let isForThisAccount = false;

        if (allocations.length > 0) {
          const matchingAlloc = allocations.find((a) => a.accountId === account.id);
          if (matchingAlloc) {
            allocatedBudgetForAccount = matchingAlloc.allocatedAmount;
            isForThisAccount = true;
          }
        } else {
          // Default account assignment
          if (cat.defaultAccountId === account.id || (!cat.defaultAccountId && account.id === assetAccounts[0]?.id)) {
            allocatedBudgetForAccount = fullBudget;
            isForThisAccount = true;
          }
        }

        if (isForThisAccount) {
          if (cat.type === CategoryType.EXPENSE) {
            totalAssignedExpenseBudget += allocatedBudgetForAccount;
            totalSpentExpense += actualSpent;
            assignedCategoriesList.push({
              category: cat,
              amount: allocatedBudgetForAccount,
              spent: actualSpent,
            });
          } else if (cat.type === CategoryType.INCOME) {
            totalExpectedIncome += allocatedBudgetForAccount;
            totalActualIncome += actualSpent;
          }
        }
      });

      // Requirement calculation based on basis:
      // BUDGET_AMOUNT: full budget requirement
      // REMAINING_AMOUNT: remaining unspent budget requirement (max(0, budget - spent))
      const requiredExpense =
        basis === RequirementCalculationBasis.BUDGET_AMOUNT
          ? totalAssignedExpenseBudget
          : Math.max(0, totalAssignedExpenseBudget - totalSpentExpense);

      const netNeed = requiredExpense;
      const surplus = Math.max(0, currentBal - netNeed);
      const shortfall = Math.max(0, netNeed - currentBal);

      return {
        account,
        currentBalance: currentBal,
        assignedBudgetExpense: totalAssignedExpenseBudget,
        spentActualExpense: totalSpentExpense,
        expectedIncome: totalExpectedIncome,
        actualIncomeReceived: totalActualIncome,
        requiredAmount: netNeed,
        availableSurplus: surplus,
        shortfall: shortfall,
        assignedCategories: assignedCategoriesList,
      };
    });

    let totalRequired = 0;
    let totalAvailable = 0;
    let netShortfall = 0;
    let netSurplus = 0;

    accountItems.forEach((item) => {
      totalRequired += item.requiredAmount;
      totalAvailable += item.currentBalance;
      netShortfall += item.shortfall;
      netSurplus += item.availableSurplus;
    });

    // Smart Transfer Allocation Recommendations (Cover shortfalls using accounts with available surplus)
    const suggestions: FundAllocationSuggestion[] = [];
    const surplusPool = accountItems
      .filter((a) => a.availableSurplus > 0)
      .map((a) => ({ account: a.account, remainingSurplus: a.availableSurplus }));

    const shortfallQueue = accountItems
      .filter((a) => a.shortfall > 0)
      .map((a) => ({ account: a.account, remainingShortfall: a.shortfall }));

    for (const deficit of shortfallQueue) {
      for (const fund of surplusPool) {
        if (deficit.remainingShortfall <= 0) break;
        if (fund.remainingSurplus <= 0) continue;

        const transferAmt = Math.min(deficit.remainingShortfall, fund.remainingSurplus);
        if (transferAmt > 0) {
          suggestions.push({
            fromAccount: fund.account,
            toAccount: deficit.account,
            suggestedAmount: transferAmt,
            reason: `Transfer from ${fund.account.nameEn} to cover ${deficit.account.nameEn} budget requirement`,
          });
          fund.remainingSurplus -= transferAmt;
          deficit.remainingShortfall -= transferAmt;
        }
      }
    }

    return {
      totalRequired,
      totalAvailable,
      netShortfall,
      netSurplus,
      accountsAnalysis: accountItems,
      suggestions,
    };
  }
}
