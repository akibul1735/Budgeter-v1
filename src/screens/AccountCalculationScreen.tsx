import React from 'react';
import { Calculator, RotateCcw, Check, Eye, EyeOff } from 'lucide-react';
import { useBudget } from '../context/BudgetContext';
import { LanguageMode } from '../types';
import { LanguageHelper } from '../utils/languageHelper';

export const AccountCalculationScreen: React.FC = () => {
  const {
    accountsWithBalances,
    financialOverview,
    languageMode,
    toggleAccountCalculation,
    updateAccountAdjustment,
  } = useBudget();

  let effectiveAssets = 0;
  let effectiveLiabilities = 0;

  accountsWithBalances.forEach((a) => {
    if (a.account.isCalculated) {
      if (a.account.type === 'ASSET') effectiveAssets += a.calculatedBalance;
      if (a.account.type === 'LIABILITY') effectiveLiabilities += a.calculatedBalance;
    }
  });

  const effectiveNetWorth = effectiveAssets - effectiveLiabilities;

  return (
    <div className="space-y-4 sm:space-y-6 pb-20">
      {/* Header Banner */}
      <div className="p-6 bg-white rounded-3xl border border-slate-200/80 shadow-xs space-y-4">
        <div>
          <h2 className="text-lg font-bold text-slate-900 flex items-center gap-2">
            <Calculator className="w-5 h-5 text-emerald-600" />
            <span>{LanguageHelper.getString('account_calculation', languageMode)}</span>
          </h2>
          <p className="text-xs text-slate-500">
            Exclude specific accounts from total Net Worth calculations or apply temporary adjustments
          </p>
        </div>

        {/* Calculated vs Actual Stats */}
        <div className="grid grid-cols-2 gap-3 pt-2">
          <div className="p-4 bg-emerald-50/70 border border-emerald-200/70 rounded-2xl">
            <span className="text-xs font-semibold text-emerald-800">
              {LanguageHelper.getString('calculated_net_worth', languageMode)}
            </span>
            <div className="text-2xl font-extrabold font-mono text-emerald-950 mt-1">
              {LanguageHelper.formatCurrency(effectiveNetWorth, languageMode)}
            </div>
          </div>

          <div className="p-4 bg-slate-50 border border-slate-200 rounded-2xl">
            <span className="text-xs font-semibold text-slate-600">
              {LanguageHelper.getString('actual_net_worth', languageMode)}
            </span>
            <div className="text-2xl font-extrabold font-mono text-slate-900 mt-1">
              {LanguageHelper.formatCurrency(financialOverview.netWorth, languageMode)}
            </div>
          </div>
        </div>
      </div>

      {/* Account List with inclusion toggle & adjustments */}
      <div className="space-y-3">
        {accountsWithBalances.map((item) => (
          <div
            key={item.account.id}
            className={`p-4 bg-white rounded-2xl border transition-all shadow-2xs flex flex-col sm:flex-row sm:items-center justify-between gap-3 ${
              item.account.isCalculated ? 'border-slate-200' : 'border-slate-200/60 opacity-60 bg-slate-50/60'
            }`}
          >
            <div className="flex items-center gap-3">
              <span
                className="w-3.5 h-3.5 rounded-full"
                style={{ backgroundColor: item.account.colorHex || '#10B981' }}
              />
              <div>
                <div className="font-bold text-slate-900 text-sm">
                  {LanguageHelper.getLocalizedName(
                    item.account.nameEn,
                    item.account.nameBn,
                    languageMode
                  )}
                </div>
                <div className="text-[11px] text-slate-500">
                  Actual: {LanguageHelper.formatCurrency(item.currentBalance, languageMode)}
                </div>
              </div>
            </div>

            <div className="flex items-center gap-4 justify-between sm:justify-end">
              {/* Adjustment Field */}
              <div className="flex items-center gap-1.5 text-xs">
                <span className="text-slate-400">Adj:</span>
                <input
                  type="number"
                  step="any"
                  value={item.account.calculationAdjustment || 0}
                  onChange={(e) =>
                    updateAccountAdjustment(item.account.id, parseFloat(e.target.value) || 0)
                  }
                  className="w-24 px-2 py-1 bg-slate-50 border border-slate-200 rounded-lg text-xs font-bold text-right font-mono"
                />
              </div>

              {/* Toggle Switch */}
              <button
                onClick={() => toggleAccountCalculation(item.account.id, !item.account.isCalculated)}
                className={`px-3 py-1.5 rounded-xl text-xs font-bold flex items-center gap-1 transition-all ${
                  item.account.isCalculated
                    ? 'bg-emerald-100 text-emerald-800'
                    : 'bg-slate-200 text-slate-600'
                }`}
              >
                {item.account.isCalculated ? (
                  <>
                    <Eye className="w-3.5 h-3.5" />
                    <span>Included</span>
                  </>
                ) : (
                  <>
                    <EyeOff className="w-3.5 h-3.5" />
                    <span>Excluded</span>
                  </>
                )}
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};
