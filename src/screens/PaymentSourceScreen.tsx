import React, { useState } from 'react';
import {
  CreditCard,
  TrendingDown,
  TrendingUp,
  AlertTriangle,
  ShieldCheck,
  ArrowRightLeft,
  ChevronDown,
  ChevronUp,
  Split,
  Plus,
  Info,
  CheckCircle2,
} from 'lucide-react';
import { useBudget } from '../context/BudgetContext';
import {
  RequirementCalculationBasis,
  AccountRequirementItem,
  FundAllocationSuggestion,
  LanguageMode,
} from '../types';
import { LanguageHelper } from '../utils/languageHelper';
import { AmountBreakdownModal } from '../components/AmountBreakdownModal';

export const PaymentSourceScreen: React.FC = () => {
  const {
    paymentSourceAnalysis,
    calculationBasis,
    setCalculationBasis,
    languageMode,
    executeTransfer,
    accounts,
    categories,
  } = useBudget();

  const [selectedBreakdownItem, setSelectedBreakdownItem] = useState<AccountRequirementItem | null>(null);
  const [filterMode, setFilterMode] = useState<'ALL' | 'SHORTFALL' | 'SURPLUS'>('ALL');
  const [transferSuccessMsg, setTransferSuccessMsg] = useState<string | null>(null);

  const filteredAccounts = paymentSourceAnalysis.accountsAnalysis.filter((acc) => {
    if (filterMode === 'SHORTFALL') return acc.shortfall > 0;
    if (filterMode === 'SURPLUS') return acc.availableSurplus > 0;
    return true;
  });

  const handleExecuteTransfer = (s: FundAllocationSuggestion) => {
    executeTransfer(
      s.fromAccount.id,
      s.toAccount.id,
      s.suggestedAmount,
      `Auto-balanced fund allocation: ${s.fromAccount.nameEn} → ${s.toAccount.nameEn}`
    );
    setTransferSuccessMsg(
      `Transferred ${LanguageHelper.formatCurrency(s.suggestedAmount, languageMode)} from ${s.fromAccount.nameEn} to ${s.toAccount.nameEn} successfully!`
    );
    setTimeout(() => setTransferSuccessMsg(null), 4000);
  };

  return (
    <div className="space-y-4 sm:space-y-6 pb-20">
      {/* Success banner */}
      {transferSuccessMsg && (
        <div className="p-3 bg-emerald-50 border border-emerald-200 text-emerald-800 rounded-2xl text-xs font-semibold flex items-center gap-2 animate-in fade-in">
          <CheckCircle2 className="w-4 h-4 text-emerald-600" />
          <span>{transferSuccessMsg}</span>
        </div>
      )}

      {/* Header Banner */}
      <div className="p-5 bg-gradient-to-r from-slate-900 to-slate-800 text-white rounded-3xl shadow-sm space-y-4">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
          <div>
            <h2 className="text-lg font-bold">
              {LanguageHelper.getString('payment_source_analysis', languageMode)}
            </h2>
            <p className="text-xs text-slate-300">
              {LanguageHelper.getString('payment_source_subtitle', languageMode)}
            </p>
          </div>

          {/* Basis Switcher */}
          <div className="flex items-center bg-slate-800/90 p-1 rounded-2xl border border-slate-700 text-xs font-semibold">
            <button
              onClick={() => setCalculationBasis(RequirementCalculationBasis.BUDGET_AMOUNT)}
              className={`px-3 py-1.5 rounded-xl transition-all ${
                calculationBasis === RequirementCalculationBasis.BUDGET_AMOUNT
                  ? 'bg-emerald-600 text-white shadow-xs'
                  : 'text-slate-300 hover:text-white'
              }`}
            >
              {LanguageHelper.getString('budget_amount_basis', languageMode)}
            </button>
            <button
              onClick={() => setCalculationBasis(RequirementCalculationBasis.REMAINING_AMOUNT)}
              className={`px-3 py-1.5 rounded-xl transition-all ${
                calculationBasis === RequirementCalculationBasis.REMAINING_AMOUNT
                  ? 'bg-emerald-600 text-white shadow-xs'
                  : 'text-slate-300 hover:text-white'
              }`}
            >
              {LanguageHelper.getString('remaining_amount_basis', languageMode)}
            </button>
          </div>
        </div>

        {/* Global Summary Stats */}
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-2.5 pt-2 border-t border-slate-700/80">
          <div>
            <span className="text-[11px] text-slate-400 font-medium">
              {LanguageHelper.getString('available_amount', languageMode)}
            </span>
            <div className="text-base sm:text-lg font-extrabold font-mono text-white">
              {LanguageHelper.formatCurrency(paymentSourceAnalysis.totalAvailable, languageMode)}
            </div>
          </div>

          <div>
            <span className="text-[11px] text-slate-400 font-medium">
              {LanguageHelper.getString('required_amount', languageMode)}
            </span>
            <div className="text-base sm:text-lg font-extrabold font-mono text-white">
              {LanguageHelper.formatCurrency(paymentSourceAnalysis.totalRequired, languageMode)}
            </div>
          </div>

          <div>
            <span className="text-[11px] text-rose-300 font-medium">
              {LanguageHelper.getString('shortfall', languageMode)}
            </span>
            <div className="text-base sm:text-lg font-extrabold font-mono text-rose-400">
              {LanguageHelper.formatCurrency(paymentSourceAnalysis.netShortfall, languageMode)}
            </div>
          </div>

          <div>
            <span className="text-[11px] text-emerald-300 font-medium">
              {LanguageHelper.getString('surplus', languageMode)}
            </span>
            <div className="text-base sm:text-lg font-extrabold font-mono text-emerald-400">
              {LanguageHelper.formatCurrency(paymentSourceAnalysis.netSurplus, languageMode)}
            </div>
          </div>
        </div>
      </div>

      {/* Smart Fund Allocation Transfer Recommendations */}
      {paymentSourceAnalysis.suggestions.length > 0 && (
        <div className="p-5 bg-amber-50/60 rounded-3xl border border-amber-200/80 shadow-xs space-y-3">
          <div className="flex items-center gap-2 text-amber-900 font-bold text-sm">
            <ArrowRightLeft className="w-4 h-4 text-amber-600" />
            <span>{LanguageHelper.getString('fund_allocation_insight', languageMode)}</span>
          </div>
          <p className="text-xs text-amber-800/80">
            {LanguageHelper.getString('fund_allocation_subtitle', languageMode)}
          </p>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-3 pt-1">
            {paymentSourceAnalysis.suggestions.map((s, idx) => (
              <div
                key={idx}
                className="p-3.5 bg-white rounded-2xl border border-amber-200/80 flex items-center justify-between gap-3 text-xs shadow-2xs"
              >
                <div>
                  <div className="font-bold text-slate-900">
                    From {s.fromAccount.nameEn} → {s.toAccount.nameEn}
                  </div>
                  <div className="text-emerald-700 font-extrabold font-mono text-sm mt-0.5">
                    {LanguageHelper.formatCurrency(s.suggestedAmount, languageMode)}
                  </div>
                  <div className="text-[10px] text-slate-500 mt-0.5">{s.reason}</div>
                </div>

                <button
                  onClick={() => handleExecuteTransfer(s)}
                  className="px-3.5 py-2 bg-emerald-600 hover:bg-emerald-700 text-white font-bold rounded-xl text-xs flex items-center gap-1 shrink-0 active:scale-95 transition-all shadow-xs"
                >
                  <ArrowRightLeft className="w-3.5 h-3.5" />
                  <span>Transfer</span>
                </button>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Filter Tabs */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-1.5 bg-slate-100 p-1 rounded-2xl text-xs font-semibold text-slate-700">
          <button
            onClick={() => setFilterMode('ALL')}
            className={`px-3 py-1.5 rounded-xl transition-all ${
              filterMode === 'ALL' ? 'bg-white text-slate-900 shadow-2xs' : ''
            }`}
          >
            All Accounts ({paymentSourceAnalysis.accountsAnalysis.length})
          </button>
          <button
            onClick={() => setFilterMode('SHORTFALL')}
            className={`px-3 py-1.5 rounded-xl transition-all ${
              filterMode === 'SHORTFALL' ? 'bg-white text-rose-700 shadow-2xs' : ''
            }`}
          >
            Shortfall Only
          </button>
          <button
            onClick={() => setFilterMode('SURPLUS')}
            className={`px-3 py-1.5 rounded-xl transition-all ${
              filterMode === 'SURPLUS' ? 'bg-white text-emerald-700 shadow-2xs' : ''
            }`}
          >
            Surplus Only
          </button>
        </div>
      </div>

      {/* Accounts List Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {filteredAccounts.map((item) => {
          const isShortfall = item.shortfall > 0;
          return (
            <div
              key={item.account.id}
              className="p-5 bg-white rounded-3xl border border-slate-200/80 shadow-xs flex flex-col justify-between space-y-4"
            >
              <div>
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <span
                      className="w-3 h-3 rounded-full"
                      style={{ backgroundColor: item.account.colorHex || '#10B981' }}
                    />
                    <h3 className="font-bold text-slate-900 text-sm sm:text-base">
                      {LanguageHelper.getLocalizedName(
                        item.account.nameEn,
                        item.account.nameBn,
                        languageMode
                      )}
                    </h3>
                  </div>

                  <span
                    className={`px-2.5 py-1 rounded-full text-[11px] font-bold ${
                      isShortfall
                        ? 'bg-rose-100 text-rose-700 border border-rose-200'
                        : 'bg-emerald-100 text-emerald-700 border border-emerald-200'
                    }`}
                  >
                    {isShortfall ? 'Deficit Shortfall' : 'Fully Funded'}
                  </span>
                </div>

                <div className="grid grid-cols-3 gap-2 mt-4 text-xs">
                  <div className="p-2.5 bg-slate-50 rounded-xl">
                    <div className="text-slate-500 text-[10px]">Current Balance</div>
                    <div className="font-bold text-slate-900 font-mono mt-0.5">
                      {LanguageHelper.formatCurrency(item.currentBalance, languageMode)}
                    </div>
                  </div>

                  <div className="p-2.5 bg-slate-50 rounded-xl">
                    <div className="text-slate-500 text-[10px]">Assigned Need</div>
                    <div className="font-bold text-slate-900 font-mono mt-0.5">
                      {LanguageHelper.formatCurrency(item.requiredAmount, languageMode)}
                    </div>
                  </div>

                  <div className={`p-2.5 rounded-xl ${isShortfall ? 'bg-rose-50' : 'bg-emerald-50'}`}>
                    <div className={isShortfall ? 'text-rose-600 text-[10px]' : 'text-emerald-600 text-[10px]'}>
                      {isShortfall ? 'Shortfall' : 'Surplus'}
                    </div>
                    <div
                      className={`font-bold font-mono mt-0.5 ${
                        isShortfall ? 'text-rose-700' : 'text-emerald-700'
                      }`}
                    >
                      {LanguageHelper.formatCurrency(
                        isShortfall ? item.shortfall : item.availableSurplus,
                        languageMode
                      )}
                    </div>
                  </div>
                </div>

                {/* Assigned Categories snippet */}
                <div className="mt-3">
                  <div className="text-[11px] font-semibold text-slate-500 mb-1">
                    Assigned Categories ({item.assignedCategories.length})
                  </div>
                  <div className="flex items-center gap-1.5 flex-wrap">
                    {item.assignedCategories.slice(0, 3).map((c, i) => (
                      <span
                        key={i}
                        className="px-2 py-0.5 bg-slate-100 text-slate-700 rounded-lg text-[10px] font-medium"
                      >
                        {c.category.nameEn}: {LanguageHelper.formatCurrency(c.amount, languageMode)}
                      </span>
                    ))}
                    {item.assignedCategories.length > 3 && (
                      <span className="text-[10px] text-slate-400">
                        +{item.assignedCategories.length - 3} more
                      </span>
                    )}
                  </div>
                </div>
              </div>

              {/* Card Footer Breakdown Trigger */}
              <div className="pt-3 border-t border-slate-100 flex items-center justify-between">
                <button
                  onClick={() => setSelectedBreakdownItem(item)}
                  className="text-xs text-emerald-600 font-semibold hover:underline flex items-center gap-1"
                >
                  <Info className="w-3.5 h-3.5" />
                  <span>{LanguageHelper.getString('expand_breakdown', languageMode)}</span>
                </button>
              </div>
            </div>
          );
        })}
      </div>

      {/* Itemized Modal Breakdown */}
      <AmountBreakdownModal
        isOpen={!!selectedBreakdownItem}
        onClose={() => setSelectedBreakdownItem(null)}
        item={selectedBreakdownItem}
      />
    </div>
  );
};
