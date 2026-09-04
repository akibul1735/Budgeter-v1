import React, { useState } from 'react';
import {
  Zap,
  Copy,
  Save,
  RotateCcw,
  CheckCircle2,
  Building2,
  FolderTree,
} from 'lucide-react';
import { useBudget } from '../context/BudgetContext';
import { CategoryType, LanguageMode } from '../types';
import { LanguageHelper } from '../utils/languageHelper';

export const BudgetMakerScreen: React.FC = () => {
  const {
    categories,
    monthlyBudgets,
    setMonthlyBudget,
    copyBudgetsFromPrevMonth,
    selectedYear,
    selectedMonth,
    languageMode,
  } = useBudget();

  const [budgetValues, setBudgetValues] = useState<{ [catId: number]: string }>(() => {
    const map: { [catId: number]: string } = {};
    categories.forEach((cat) => {
      const match = monthlyBudgets.find(
        (b) => b.categoryId === cat.id && b.year === selectedYear && b.month === selectedMonth
      );
      map[cat.id] = String(match ? match.budgetAmount : cat.monthlyBudget || 0);
    });
    return map;
  });

  const [savedSuccess, setSavedSuccess] = useState(false);

  const expenseCategories = categories.filter((c) => c.type === CategoryType.EXPENSE);

  const handleSaveAll = () => {
    Object.keys(budgetValues).forEach((catIdStr) => {
      const catId = Number(catIdStr);
      const val = parseFloat(budgetValues[catId]) || 0;
      setMonthlyBudget(catId, val, selectedYear, selectedMonth);
    });
    setSavedSuccess(true);
    setTimeout(() => setSavedSuccess(false), 3000);
  };

  const handleCopyPrev = () => {
    const prevM = selectedMonth === 1 ? 12 : selectedMonth - 1;
    const prevY = selectedMonth === 1 ? selectedYear - 1 : selectedYear;
    copyBudgetsFromPrevMonth(prevY, prevM, selectedYear, selectedMonth);
    setSavedSuccess(true);
    setTimeout(() => setSavedSuccess(false), 3000);
  };

  const totalPlanned = Object.values(budgetValues).reduce(
    (sum, val) => sum + (parseFloat(val) || 0),
    0
  );

  return (
    <div className="space-y-4 sm:space-y-6 pb-20">
      {/* Success banner */}
      {savedSuccess && (
        <div className="p-3 bg-emerald-50 border border-emerald-200 text-emerald-800 rounded-2xl text-xs font-semibold flex items-center gap-2 animate-in fade-in">
          <CheckCircle2 className="w-4 h-4 text-emerald-600" />
          <span>Budgets saved successfully for this month!</span>
        </div>
      )}

      {/* Header Banner */}
      <div className="p-6 bg-white rounded-3xl border border-slate-200/80 shadow-xs flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-lg font-bold text-slate-900 flex items-center gap-2">
            <Zap className="w-5 h-5 text-amber-500" />
            <span>{LanguageHelper.getString('budget_maker', languageMode)}</span>
          </h2>
          <p className="text-xs text-slate-500">
            Quickly adjust and allocate spending limits for categories
          </p>
        </div>

        <div className="flex items-center gap-2">
          <button
            onClick={handleCopyPrev}
            className="px-3.5 py-2 bg-slate-100 hover:bg-slate-200 text-slate-700 font-semibold text-xs rounded-2xl flex items-center gap-1.5 transition-colors"
          >
            <Copy className="w-3.5 h-3.5" />
            <span>Copy Prev Month</span>
          </button>

          <button
            onClick={handleSaveAll}
            className="px-5 py-2 bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-xs rounded-2xl flex items-center gap-1.5 shadow-sm active:scale-95 transition-all"
          >
            <Save className="w-4 h-4" />
            <span>Save All Budgets</span>
          </button>
        </div>
      </div>

      {/* Total planned footer card */}
      <div className="p-4 bg-gradient-to-r from-slate-900 to-slate-800 text-white rounded-3xl flex items-center justify-between">
        <span className="text-xs font-semibold text-slate-300">Total Monthly Budget Planned</span>
        <span className="text-xl font-extrabold font-mono text-emerald-400">
          {LanguageHelper.formatCurrency(totalPlanned, languageMode)}
        </span>
      </div>

      {/* Categories Budget Inputs Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
        {expenseCategories.map((cat) => (
          <div
            key={cat.id}
            className="p-4 bg-white rounded-2xl border border-slate-200/80 flex items-center justify-between gap-3 text-xs shadow-2xs"
          >
            <div className="flex items-center gap-2.5">
              <span
                className="w-3 h-3 rounded-full"
                style={{ backgroundColor: cat.colorHex || '#F59E0B' }}
              />
              <div>
                <div className="font-bold text-slate-900">
                  {LanguageHelper.getLocalizedName(cat.nameEn, cat.nameBn, languageMode)}
                </div>
                <div className="text-[10px] text-slate-400">
                  Default: {LanguageHelper.formatCurrency(cat.monthlyBudget, languageMode)}
                </div>
              </div>
            </div>

            <div className="flex items-center gap-1.5 w-36">
              <input
                type="number"
                step="any"
                value={budgetValues[cat.id] || ''}
                onChange={(e) =>
                  setBudgetValues({
                    ...budgetValues,
                    [cat.id]: e.target.value,
                  })
                }
                placeholder="0"
                className="w-full px-3 py-1.5 bg-slate-50 border border-slate-200 rounded-xl text-xs font-bold text-slate-900 focus:bg-white focus:outline-none focus:ring-1 focus:ring-emerald-500 font-mono text-right"
              />
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};
