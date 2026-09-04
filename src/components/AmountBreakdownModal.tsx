import React from 'react';
import { X, TrendingDown, TrendingUp, ArrowRightLeft } from 'lucide-react';
import { useBudget } from '../context/BudgetContext';
import { LanguageHelper } from '../utils/languageHelper';
import { AccountRequirementItem } from '../types';

interface AmountBreakdownModalProps {
  isOpen: boolean;
  onClose: () => void;
  item: AccountRequirementItem | null;
}

export const AmountBreakdownModal: React.FC<AmountBreakdownModalProps> = ({
  isOpen,
  onClose,
  item,
}) => {
  const { languageMode } = useBudget();

  if (!isOpen || !item) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 backdrop-blur-xs p-4 animate-in fade-in duration-150">
      <div className="w-full max-w-lg bg-white rounded-3xl shadow-2xl border border-slate-200 overflow-hidden flex flex-col max-h-[85vh]">
        {/* Header */}
        <div className="px-5 py-4 border-b border-slate-100 flex items-center justify-between bg-slate-50/70">
          <div>
            <h2 className="font-bold text-slate-900 text-base">
              {LanguageHelper.getLocalizedName(item.account.nameEn, item.account.nameBn, languageMode)}
            </h2>
            <p className="text-xs text-slate-500">
              {LanguageHelper.getString('payment_source_analysis', languageMode)}
            </p>
          </div>
          <button onClick={onClose} className="p-1.5 rounded-xl text-slate-400 hover:text-slate-700">
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Content */}
        <div className="p-5 overflow-y-auto space-y-4">
          {/* Summary Stat Cards */}
          <div className="grid grid-cols-2 gap-2.5">
            <div className="p-3 bg-slate-50 rounded-2xl border border-slate-100">
              <div className="text-[11px] font-medium text-slate-500">
                {LanguageHelper.getString('current_balance', languageMode)}
              </div>
              <div className="text-base font-bold text-slate-900 font-mono mt-0.5">
                {LanguageHelper.formatCurrency(item.currentBalance, languageMode)}
              </div>
            </div>

            <div className="p-3 bg-slate-50 rounded-2xl border border-slate-100">
              <div className="text-[11px] font-medium text-slate-500">
                {LanguageHelper.getString('required_amount', languageMode)}
              </div>
              <div className="text-base font-bold text-slate-900 font-mono mt-0.5">
                {LanguageHelper.formatCurrency(item.requiredAmount, languageMode)}
              </div>
            </div>

            <div className="p-3 bg-rose-50/60 rounded-2xl border border-rose-100">
              <div className="text-[11px] font-medium text-rose-600">
                {LanguageHelper.getString('shortfall', languageMode)}
              </div>
              <div className="text-base font-bold text-rose-700 font-mono mt-0.5">
                {LanguageHelper.formatCurrency(item.shortfall, languageMode)}
              </div>
            </div>

            <div className="p-3 bg-emerald-50/60 rounded-2xl border border-emerald-100">
              <div className="text-[11px] font-medium text-emerald-600">
                {LanguageHelper.getString('surplus', languageMode)}
              </div>
              <div className="text-base font-bold text-emerald-700 font-mono mt-0.5">
                {LanguageHelper.formatCurrency(item.availableSurplus, languageMode)}
              </div>
            </div>
          </div>

          {/* Assigned Categories List */}
          <div>
            <h3 className="text-xs font-bold text-slate-800 uppercase tracking-wider mb-2">
              {LanguageHelper.getString('itemized_expenses', languageMode)}
            </h3>
            {item.assignedCategories.length === 0 ? (
              <p className="text-xs text-slate-400 py-3 text-center bg-slate-50 rounded-xl">
                No expense categories specifically assigned to this account.
              </p>
            ) : (
              <div className="space-y-2">
                {item.assignedCategories.map((c, idx) => (
                  <div
                    key={idx}
                    className="p-3 bg-slate-50 hover:bg-slate-100/80 rounded-xl border border-slate-200/70 flex items-center justify-between text-xs"
                  >
                    <div>
                      <div className="font-semibold text-slate-800">
                        {LanguageHelper.getLocalizedName(c.category.nameEn, c.category.nameBn, languageMode)}
                      </div>
                      <div className="text-[11px] text-slate-500 mt-0.5">
                        Spent: {LanguageHelper.formatCurrency(c.spent, languageMode)}
                      </div>
                    </div>

                    <div className="text-right">
                      <div className="font-bold text-slate-900 font-mono">
                        {LanguageHelper.formatCurrency(c.amount, languageMode)}
                      </div>
                      <div className="text-[10px] text-slate-400">Monthly Budget</div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        {/* Footer */}
        <div className="p-4 border-t border-slate-100 bg-slate-50 flex justify-end">
          <button
            onClick={onClose}
            className="px-5 py-2 rounded-xl bg-slate-800 hover:bg-slate-900 text-white font-bold text-xs shadow-xs"
          >
            {LanguageHelper.getString('done', languageMode)}
          </button>
        </div>
      </div>
    </div>
  );
};
