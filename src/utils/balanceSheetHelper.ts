import {
  Account,
  AccountType,
  Category,
  CategoryType,
  FinancialOverview,
  Transaction,
  TransactionType,
  TransactionStatus,
} from '../types';

export class BalanceSheetHelper {
  public static calculateBalances(
    accounts: Account[],
    transactions: Transaction[],
    selectedYear?: number,
    selectedMonth?: number
  ): {
    accountBalances: Map<number, number>;
    overview: FinancialOverview;
  } {
    const balanceMap = new Map<number, number>();

    // Initialize with initial balances
    accounts.forEach((acc) => {
      balanceMap.set(acc.id, acc.initialBalance || 0);
    });

    let totalDebits = 0;
    let totalCredits = 0;
    let monthlyIncome = 0;
    let monthlyExpense = 0;

    const now = new Date();
    const targetYear = selectedYear ?? now.getFullYear();
    const targetMonth = selectedMonth ?? now.getMonth() + 1;

    // Process all non-void transactions
    transactions
      .filter((t) => t.status !== TransactionStatus.VOID)
      .forEach((tx) => {
        const txDate = new Date(tx.dateEpochMs);
        const isSelectedMonth =
          txDate.getFullYear() === targetYear && txDate.getMonth() + 1 === targetMonth;

        if (tx.type === TransactionType.EXPENSE) {
          totalDebits += tx.amount;
          totalCredits += tx.amount;

          if (isSelectedMonth) {
            monthlyExpense += tx.amount;
          }

          // Outflow from Credit Account
          if (tx.creditAccountId && balanceMap.has(tx.creditAccountId)) {
            const acc = accounts.find((a) => a.id === tx.creditAccountId);
            const cur = balanceMap.get(tx.creditAccountId)!;
            // For Asset, expense reduces balance; for Liability (e.g. credit card charge), increases liability balance
            if (acc?.type === AccountType.LIABILITY) {
              balanceMap.set(tx.creditAccountId, cur + tx.amount);
            } else {
              balanceMap.set(tx.creditAccountId, cur - tx.amount);
            }
          }
        } else if (tx.type === TransactionType.INCOME) {
          totalDebits += tx.amount;
          totalCredits += tx.amount;

          if (isSelectedMonth) {
            monthlyIncome += tx.amount;
          }

          // Inflow to Debit Account
          if (tx.debitAccountId && balanceMap.has(tx.debitAccountId)) {
            const acc = accounts.find((a) => a.id === tx.debitAccountId);
            const cur = balanceMap.get(tx.debitAccountId)!;
            // For Asset, income increases balance; for Liability (e.g. debt repayment), reduces liability
            if (acc?.type === AccountType.LIABILITY) {
              balanceMap.set(tx.debitAccountId, Math.max(0, cur - tx.amount));
            } else {
              balanceMap.set(tx.debitAccountId, cur + tx.amount);
            }
          }
        } else if (tx.type === TransactionType.TRANSFER) {
          totalDebits += tx.amount;
          totalCredits += tx.amount;

          // Outflow from Credit Account
          if (tx.creditAccountId && balanceMap.has(tx.creditAccountId)) {
            const accFrom = accounts.find((a) => a.id === tx.creditAccountId);
            const cur = balanceMap.get(tx.creditAccountId)!;
            if (accFrom?.type === AccountType.LIABILITY) {
              balanceMap.set(tx.creditAccountId, cur + tx.amount);
            } else {
              balanceMap.set(tx.creditAccountId, cur - tx.amount);
            }
          }

          // Inflow to Debit Account
          if (tx.debitAccountId && balanceMap.has(tx.debitAccountId)) {
            const accTo = accounts.find((a) => a.id === tx.debitAccountId);
            const cur = balanceMap.get(tx.debitAccountId)!;
            if (accTo?.type === AccountType.LIABILITY) {
              balanceMap.set(tx.debitAccountId, Math.max(0, cur - tx.amount));
            } else {
              balanceMap.set(tx.debitAccountId, cur + tx.amount);
            }
          }
        }
      });

    // Sum Assets vs Liabilities
    let totalAssets = 0;
    let totalLiabilities = 0;

    accounts.forEach((acc) => {
      if (!acc.isArchived) {
        const bal = balanceMap.get(acc.id) || 0;
        if (acc.type === AccountType.ASSET) {
          totalAssets += bal;
        } else if (acc.type === AccountType.LIABILITY) {
          totalLiabilities += bal;
        }
      }
    });

    const netWorth = totalAssets - totalLiabilities;
    const isLedgerBalanced = Math.abs(totalDebits - totalCredits) < 0.01;

    const overview: FinancialOverview = {
      totalAssets,
      totalLiabilities,
      netWorth,
      monthlyIncome,
      monthlyExpense,
      netIncome: monthlyIncome - monthlyExpense,
      isLedgerBalanced,
      totalDebits,
      totalCredits,
      expendableCash: Math.max(0, totalAssets - totalLiabilities),
      committedBudget: 0,
    };

    return {
      accountBalances: balanceMap,
      overview,
    };
  }

  public static getCategorySpending(
    categories: Category[],
    transactions: Transaction[],
    year: number,
    month: number
  ) {
    const monthTransactions = transactions.filter((t) => {
      const d = new Date(t.dateEpochMs);
      return (
        t.status !== TransactionStatus.VOID &&
        d.getFullYear() === year &&
        d.getMonth() + 1 === month
      );
    });

    return categories.map((cat) => {
      const spent = monthTransactions
        .filter((t) => t.categoryId === cat.id && t.type === TransactionType.EXPENSE)
        .reduce((sum, t) => sum + t.amount, 0);

      const budget = cat.monthlyBudget || 0;
      const remaining = budget - spent;
      const percentage = budget > 0 ? (spent / budget) * 100 : 0;

      return {
        category: cat,
        spentAmount: spent,
        budgetAmount: budget,
        remainingAmount: remaining,
        percentageSpent: percentage,
        isOverBudget: budget > 0 && spent > budget,
      };
    });
  }
}
