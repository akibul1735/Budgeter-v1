import React from 'react';
import { FolderTree, Plus, TrendingDown, TrendingUp } from 'lucide-react';
import { useBudget } from '../context/BudgetContext';
import { Category, CategoryType, LanguageMode } from '../types';
import { LanguageHelper } from '../utils/languageHelper';

interface CategoriesScreenProps {
  onOpenNewCategory: () => void;
  onOpenEditCategory: (cat: Category) => void;
}

export const CategoriesScreen: React.FC<CategoriesScreenProps> = ({
  onOpenNewCategory,
  onOpenEditCategory,
}) => {
  const { categories, languageMode } = useBudget();

  const expenseCategories = categories.filter((c) => c.type === CategoryType.EXPENSE);
  const incomeCategories = categories.filter((c) => c.type === CategoryType.INCOME);

  return (
    <div className="space-y-4 sm:space-y-6 pb-20">
      {/* Header Banner */}
      <div className="p-6 bg-white rounded-3xl border border-slate-200/80 shadow-xs flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-lg font-bold text-slate-900 flex items-center gap-2">
            <FolderTree className="w-5 h-5 text-emerald-600" />
            <span>{LanguageHelper.getString('categories', languageMode)}</span>
          </h2>
          <p className="text-xs text-slate-500">
            Categorize your cash inflows and budget caps for expenditures
          </p>
        </div>

        <button
          onClick={onOpenNewCategory}
          className="px-4 py-2 bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-xs rounded-2xl flex items-center gap-1.5 shadow-sm active:scale-95 transition-all"
        >
          <Plus className="w-4 h-4" />
          <span>Add Category</span>
        </button>
      </div>

      {/* Expense Categories */}
      <div className="space-y-3">
        <h3 className="text-xs font-bold text-slate-700 uppercase tracking-wider px-1 flex items-center gap-1.5">
          <TrendingDown className="w-4 h-4 text-rose-500" />
          <span>Expense Categories ({expenseCategories.length})</span>
        </h3>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
          {expenseCategories.map((cat) => (
            <div
              key={cat.id}
              onClick={() => onOpenEditCategory(cat)}
              className="p-4 bg-white rounded-2xl border border-slate-200/80 hover:border-amber-300 cursor-pointer transition-all shadow-2xs flex items-center justify-between"
            >
              <div className="flex items-center gap-3">
                <span
                  className="w-3.5 h-3.5 rounded-full"
                  style={{ backgroundColor: cat.colorHex || '#F59E0B' }}
                />
                <div>
                  <div className="font-bold text-slate-900 text-sm">
                    {LanguageHelper.getLocalizedName(cat.nameEn, cat.nameBn, languageMode)}
                  </div>
                  <div className="text-[11px] text-slate-400 mt-0.5">
                    Limit: {LanguageHelper.formatCurrency(cat.monthlyBudget, languageMode)}/mo
                  </div>
                </div>
              </div>

              <div className="text-right">
                <span className="text-[10px] font-bold px-2 py-0.5 bg-slate-100 text-slate-600 rounded-full">
                  Expense
                </span>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Income Categories */}
      <div className="space-y-3 pt-2">
        <h3 className="text-xs font-bold text-emerald-700 uppercase tracking-wider px-1 flex items-center gap-1.5">
          <TrendingUp className="w-4 h-4 text-emerald-600" />
          <span>Income Categories ({incomeCategories.length})</span>
        </h3>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
          {incomeCategories.map((cat) => (
            <div
              key={cat.id}
              onClick={() => onOpenEditCategory(cat)}
              className="p-4 bg-white rounded-2xl border border-slate-200/80 hover:border-emerald-300 cursor-pointer transition-all shadow-2xs flex items-center justify-between"
            >
              <div className="flex items-center gap-3">
                <span
                  className="w-3.5 h-3.5 rounded-full"
                  style={{ backgroundColor: cat.colorHex || '#10B981' }}
                />
                <div>
                  <div className="font-bold text-slate-900 text-sm">
                    {LanguageHelper.getLocalizedName(cat.nameEn, cat.nameBn, languageMode)}
                  </div>
                  <div className="text-[11px] text-slate-400 mt-0.5">
                    Expected: {LanguageHelper.formatCurrency(cat.monthlyBudget, languageMode)}/mo
                  </div>
                </div>
              </div>

              <div className="text-right">
                <span className="text-[10px] font-bold px-2 py-0.5 bg-emerald-50 text-emerald-700 rounded-full">
                  Income
                </span>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};
