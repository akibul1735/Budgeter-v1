import React, { useMemo } from 'react';
import { FileSpreadsheet, Search, TrendingDown, TrendingUp } from 'lucide-react';
import { useBudget } from '../context/BudgetContext';
import { LanguageMode, TransactionType } from '../types';
import { LanguageHelper } from '../utils/languageHelper';

export const ItemsSummaryScreen: React.FC = () => {
  const { transactions, languageMode } = useBudget();

  const payeesData = useMemo(() => {
    const map = new Map<string, { count: number; totalExpense: number; totalIncome: number }>();

    transactions.forEach((tx) => {
      const payee = tx.payeePayer?.trim();
      if (!payee) return;

      const cur = map.get(payee) || { count: 0, totalExpense: 0, totalIncome: 0 };
      cur.count += 1;
      if (tx.type === TransactionType.EXPENSE) cur.totalExpense += tx.amount;
      if (tx.type === TransactionType.INCOME) cur.totalIncome += tx.amount;
      map.set(payee, cur);
    });

    return Array.from(map.entries()).map(([payee, data]) => ({
      payee,
      ...data,
    }));
  }, [transactions]);

  return (
    <div className="space-y-4 sm:space-y-6 pb-20">
      <div className="p-6 bg-white rounded-3xl border border-slate-200/80 shadow-xs">
        <h2 className="text-lg font-bold text-slate-900 flex items-center gap-2">
          <FileSpreadsheet className="w-5 h-5 text-emerald-600" />
          <span>{LanguageHelper.getString('items_summary', languageMode)}</span>
        </h2>
        <p className="text-xs text-slate-500 mt-0.5">
          Breakdown by frequent Payees, Vendors, and Income Sources
        </p>
      </div>

      <div className="bg-white rounded-3xl border border-slate-200/80 shadow-xs overflow-hidden">
        <div className="p-4 bg-slate-50 border-b border-slate-200 flex items-center justify-between text-xs font-bold text-slate-600 uppercase tracking-wider">
          <span>Payee / Vendor</span>
          <div className="flex items-center gap-8 pr-4">
            <span>Expenses</span>
            <span>Incomes</span>
          </div>
        </div>

        <div className="divide-y divide-slate-100 text-xs">
          {payeesData.map((item) => (
            <div key={item.payee} className="p-4 flex items-center justify-between hover:bg-slate-50/80">
              <div>
                <div className="font-semibold text-slate-800">{item.payee}</div>
                <div className="text-[10px] text-slate-400">{item.count} transactions</div>
              </div>

              <div className="flex items-center gap-8 font-mono font-bold pr-2">
                <span className="w-24 text-right text-rose-600">
                  {item.totalExpense > 0 ? LanguageHelper.formatCurrency(item.totalExpense, languageMode) : '-'}
                </span>
                <span className="w-24 text-right text-emerald-600">
                  {item.totalIncome > 0 ? LanguageHelper.formatCurrency(item.totalIncome, languageMode) : '-'}
                </span>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};
