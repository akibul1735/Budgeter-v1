import React from 'react';
import {
  PieChart as PieChartIcon,
  AlertCircle,
  TrendingDown,
  ChevronRight,
  Plus,
  Sliders,
  Zap,
} from 'lucide-react';
import { useBudget } from '../context/BudgetContext';
import { CategoryType, AppTab, LanguageMode } from '../types';
import { LanguageHelper } from '../utils/languageHelper';

interface BudgetScreenProps {
  onOpenNewTransaction: () => void;
  onOpenNewCategory: () => void;
}

export const BudgetScreen: React.FC<BudgetScreenProps> = ({
  onOpenNewTransaction,
  onOpenNewCategory,
}) => {
  const {
    categorySpending,
    languageMode,
    selectedYear,
    selectedMonth,
    setCurrentTab,
  } = useBudget();

  const expenseBudgets = categorySpending.filter((c) => c.category.type === CategoryType.EXPENSE);

  let totalBudget = 0;
  let totalSpent = 0;

  expenseBudgets.forEach((b) => {
    totalBudget += b.budgetAmount;
    totalSpent += b.spentAmount;
  });

  const totalRemaining = totalBudget - totalSpent;
  const overallPercentage = totalBudget > 0 ? (totalSpent / totalBudget) * 100 : 0;

  return (
    <div className="space-y-4 sm:space-y-6 pb-20">
      {/* Top Banner: Overall Budget Summary */}
      <div className="p-6 bg-white rounded-3xl border border-slate-200/80 shadow-xs space-y-4">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
          <div>
            <h2 className="text-lg font-bold text-slate-900">
              {LanguageHelper.getString('budget_summary', languageMode)}
            </h2>
            <p className="text-xs text-slate-500">Monthly expense caps and live spending progress</p>
          </div>

          <div className="flex items-center gap-2">
            <button
              onClick={() => setCurrentTab(AppTab.BUDGET_MAKER)}
              className="px-3.5 py-2 bg-slate-100 hover:bg-slate-200 text-slate-700 font-semibold text-xs rounded-2xl flex items-center gap-1.5 transition-colors"
            >
              <Zap className="w-4 h-4 text-amber-500" />
              <span>{LanguageHelper.getString('budget_maker', languageMode)}</span>
            </button>

            <button
              onClick={onOpenNewCategory}
              className="px-3.5 py-2 bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-xs rounded-2xl flex items-center gap-1 shadow-sm active:scale-95 transition-all"
            >
              <Plus className="w-4 h-4" />
              <span>Add Budget</span>
            </button>
          </div>
        </div>

        {/* Global Progress Bar */}
        <div className="space-y-2 pt-2">
          <div className="flex items-center justify-between text-xs font-bold">
            <span className="text-slate-600">
              Spent: {LanguageHelper.formatCurrency(totalSpent, languageMode)}
            </span>
            <span className={totalRemaining >= 0 ? 'text-emerald-600' : 'text-rose-600'}>
              {totalRemaining >= 0 ? 'Remaining' : 'Over Budget'}:{' '}
              {LanguageHelper.formatCurrency(Math.abs(totalRemaining), languageMode)}
            </span>
          </div>

          <div className="w-full h-3.5 bg-slate-100 rounded-full overflow-hidden p-0.5">
            <div
              className={`h-full rounded-full transition-all duration-500 ${
                overallPercentage > 100 ? 'bg-rose-500' : 'bg-emerald-500'
              }`}
              style={{ width: `${Math.min(100, overallPercentage)}%` }}
            />
          </div>

          <div className="flex justify-between text-[11px] text-slate-400">
            <span>0%</span>
            <span>Total Limit: {LanguageHelper.formatCurrency(totalBudget, languageMode)}</span>
            <span>{overallPercentage.toFixed(1)}%</span>
          </div>
        </div>
      </div>

      {/* Category Budgets Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {expenseBudgets.map((item) => {
          const isOver = item.isOverBudget;
          return (
            <div
              key={item.category.id}
              className="p-5 bg-white rounded-3xl border border-slate-200/80 shadow-xs space-y-3"
            >
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2.5">
                  <span
                    className="w-3 h-3 rounded-full"
                    style={{ backgroundColor: item.category.colorHex || '#F59E0B' }}
                  />
                  <div>
                    <h3 className="font-bold text-slate-900 text-sm">
                      {LanguageHelper.getLocalizedName(
                        item.category.nameEn,
                        item.category.nameBn,
                        languageMode
                      )}
                    </h3>
                    <div className="text-[10px] text-slate-400">
                      Budget: {LanguageHelper.formatCurrency(item.budgetAmount, languageMode)}
                    </div>
                  </div>
                </div>

                <div className="text-right">
                  <div
                    className={`font-extrabold font-mono text-sm ${
                      isOver ? 'text-rose-600' : 'text-slate-900'
                    }`}
                  >
                    {LanguageHelper.formatCurrency(item.spentAmount, languageMode)}
                  </div>
                  <span
                    className={`inline-block text-[10px] font-bold px-2 py-0.5 rounded-full ${
                      isOver ? 'bg-rose-100 text-rose-700' : 'bg-slate-100 text-slate-700'
                    }`}
                  >
                    {item.percentageSpent.toFixed(0)}%
                  </span>
                </div>
              </div>

              {/* Progress Bar */}
              <div className="w-full h-2.5 bg-slate-100 rounded-full overflow-hidden">
                <div
                  className={`h-full rounded-full transition-all duration-500 ${
                    isOver ? 'bg-rose-500' : 'bg-emerald-500'
                  }`}
                  style={{ width: `${Math.min(100, item.percentageSpent)}%` }}
                />
              </div>

              {/* Status footer */}
              <div className="flex items-center justify-between text-[11px]">
                <span className="text-slate-500">
                  {isOver ? (
                    <span className="text-rose-600 font-semibold flex items-center gap-1">
                      <AlertCircle className="w-3 h-3" />
                      Over by {LanguageHelper.formatCurrency(Math.abs(item.remainingAmount), languageMode)}
                    </span>
                  ) : (
                    <span className="text-emerald-600 font-semibold">
                      {LanguageHelper.formatCurrency(item.remainingAmount, languageMode)} remaining
                    </span>
                  )}
                </span>

                <button
                  onClick={onOpenNewTransaction}
                  className="text-emerald-600 hover:text-emerald-800 font-semibold text-[11px]"
                >
                  + Add Expense
                </button>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};
