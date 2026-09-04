import React, { useMemo } from 'react';
import { Tag, TrendingDown, TrendingUp } from 'lucide-react';
import { useBudget } from '../context/BudgetContext';
import { LanguageMode, TransactionType } from '../types';
import { LanguageHelper } from '../utils/languageHelper';

export const LabelsScreen: React.FC = () => {
  const { transactions, languageMode } = useBudget();

  const tagsData = useMemo(() => {
    const map = new Map<string, { count: number; totalExpense: number; totalIncome: number }>();

    transactions.forEach((tx) => {
      if (tx.tags && tx.tags.length > 0) {
        tx.tags.forEach((tag) => {
          const clean = tag.trim().toLowerCase();
          if (!clean) return;
          const cur = map.get(clean) || { count: 0, totalExpense: 0, totalIncome: 0 };
          cur.count += 1;
          if (tx.type === TransactionType.EXPENSE) cur.totalExpense += tx.amount;
          if (tx.type === TransactionType.INCOME) cur.totalIncome += tx.amount;
          map.set(clean, cur);
        });
      }
    });

    return Array.from(map.entries()).map(([tag, data]) => ({
      tag,
      ...data,
    }));
  }, [transactions]);

  return (
    <div className="space-y-4 sm:space-y-6 pb-20">
      <div className="p-6 bg-white rounded-3xl border border-slate-200/80 shadow-xs">
        <h2 className="text-lg font-bold text-slate-900 flex items-center gap-2">
          <Tag className="w-5 h-5 text-emerald-600" />
          <span>{LanguageHelper.getString('labels', languageMode)}</span>
        </h2>
        <p className="text-xs text-slate-500 mt-0.5">
          Spending & cashflow organized by custom tags and labels
        </p>
      </div>

      {tagsData.length === 0 ? (
        <div className="p-12 text-center text-slate-400 bg-white rounded-3xl border border-slate-200/80 text-xs">
          No tags found on recorded transactions. Add tags like #groceries or #reimbursable when logging entries!
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3">
          {tagsData.map((item) => (
            <div
              key={item.tag}
              className="p-4 bg-white rounded-2xl border border-slate-200/80 shadow-2xs space-y-2 text-xs"
            >
              <div className="flex items-center justify-between">
                <span className="font-bold text-slate-900 text-sm">#{item.tag}</span>
                <span className="text-[11px] px-2 py-0.5 bg-slate-100 rounded-full text-slate-600 font-semibold">
                  {item.count} entries
                </span>
              </div>

              <div className="grid grid-cols-2 gap-2 pt-1 border-t border-slate-100">
                <div>
                  <div className="text-[10px] text-slate-400">Total Spent</div>
                  <div className="font-bold text-rose-600 font-mono">
                    {LanguageHelper.formatCurrency(item.totalExpense, languageMode)}
                  </div>
                </div>
                <div>
                  <div className="text-[10px] text-slate-400">Total Income</div>
                  <div className="font-bold text-emerald-600 font-mono">
                    {LanguageHelper.formatCurrency(item.totalIncome, languageMode)}
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
