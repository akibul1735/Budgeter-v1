import React, { useState } from 'react';
import {
  Scale,
  Building2,
  CreditCard,
  TrendingUp,
  TrendingDown,
  ShieldCheck,
  AlertTriangle,
  Download,
} from 'lucide-react';
import { useBudget } from '../context/BudgetContext';
import { AccountType, LanguageMode } from '../types';
import { LanguageHelper } from '../utils/languageHelper';

export const BalanceSheetScreen: React.FC = () => {
  const {
    accounts,
    accountBalances,
    financialOverview,
    languageMode,
    selectedYear,
    selectedMonth,
  } = useBudget();

  const [activeTab, setActiveTab] = useState<'BALANCE_SHEET' | 'TRIAL_BALANCE'>('BALANCE_SHEET');

  const assetAccounts = accounts.filter((a) => a.type === AccountType.ASSET && !a.isArchived);
  const liabilityAccounts = accounts.filter((a) => a.type === AccountType.LIABILITY && !a.isArchived);
  const equityAccounts = accounts.filter((a) => a.type === AccountType.EQUITY && !a.isArchived);

  return (
    <div className="space-y-4 sm:space-y-6 pb-20">
      {/* View Switcher */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-1.5 bg-white p-1 rounded-2xl border border-slate-200 shadow-2xs text-xs font-semibold">
          <button
            onClick={() => setActiveTab('BALANCE_SHEET')}
            className={`px-4 py-2 rounded-xl transition-all ${
              activeTab === 'BALANCE_SHEET'
                ? 'bg-emerald-600 text-white shadow-xs'
                : 'text-slate-600 hover:text-slate-900'
            }`}
          >
            {LanguageHelper.getString('balance_sheet', languageMode)}
          </button>
          <button
            onClick={() => setActiveTab('TRIAL_BALANCE')}
            className={`px-4 py-2 rounded-xl transition-all ${
              activeTab === 'TRIAL_BALANCE'
                ? 'bg-emerald-600 text-white shadow-xs'
                : 'text-slate-600 hover:text-slate-900'
            }`}
          >
            {LanguageHelper.getString('trial_balance', languageMode)}
          </button>
        </div>

        {/* Ledger Integrity Badge */}
        <div
          className={`px-3 py-1.5 rounded-2xl text-xs font-bold flex items-center gap-1.5 border ${
            financialOverview.isLedgerBalanced
              ? 'bg-emerald-50 text-emerald-800 border-emerald-200'
              : 'bg-rose-50 text-rose-800 border-rose-200'
          }`}
        >
          {financialOverview.isLedgerBalanced ? (
            <>
              <ShieldCheck className="w-4 h-4 text-emerald-600" />
              <span>{LanguageHelper.getString('balanced_ledger', languageMode)}</span>
            </>
          ) : (
            <>
              <AlertTriangle className="w-4 h-4 text-rose-600" />
              <span>{LanguageHelper.getString('unbalanced', languageMode)}</span>
            </>
          )}
        </div>
      </div>

      {activeTab === 'BALANCE_SHEET' ? (
        <div className="space-y-6">
          {/* Net Worth Hero Card */}
          <div className="p-6 bg-gradient-to-br from-slate-900 to-slate-800 text-white rounded-3xl shadow-sm">
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
              <div>
                <span className="text-xs font-semibold uppercase tracking-wider text-slate-400">
                  {LanguageHelper.getString('net_worth', languageMode)}
                </span>
                <div className="text-3xl sm:text-4xl font-extrabold font-mono tracking-tight mt-1 text-emerald-400">
                  {LanguageHelper.formatCurrency(financialOverview.netWorth, languageMode)}
                </div>
                <p className="text-xs text-slate-300 mt-1">
                  Assets ({LanguageHelper.formatCurrency(financialOverview.totalAssets, languageMode)}) -
                  Liabilities ({LanguageHelper.formatCurrency(financialOverview.totalLiabilities, languageMode)})
                </p>
              </div>

              <div className="grid grid-cols-2 gap-3 text-xs bg-slate-800/80 p-3 rounded-2xl border border-slate-700">
                <div>
                  <div className="text-slate-400 font-medium">Monthly Inflow</div>
                  <div className="font-bold text-emerald-400 font-mono mt-0.5">
                    +{LanguageHelper.formatCurrency(financialOverview.monthlyIncome, languageMode)}
                  </div>
                </div>
                <div>
                  <div className="text-slate-400 font-medium">Monthly Outflow</div>
                  <div className="font-bold text-rose-400 font-mono mt-0.5">
                    -{LanguageHelper.formatCurrency(financialOverview.monthlyExpense, languageMode)}
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* Assets & Liabilities Side by Side */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 sm:gap-6">
            {/* Assets */}
            <div className="bg-white rounded-3xl border border-slate-200/80 shadow-xs overflow-hidden">
              <div className="p-4 bg-emerald-50/70 border-b border-emerald-100 flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <Building2 className="w-5 h-5 text-emerald-600" />
                  <h3 className="font-bold text-slate-900 text-sm">
                    {LanguageHelper.getString('assets', languageMode)}
                  </h3>
                </div>
                <span className="font-extrabold font-mono text-emerald-700 text-base">
                  {LanguageHelper.formatCurrency(financialOverview.totalAssets, languageMode)}
                </span>
              </div>

              <div className="p-4 divide-y divide-slate-100">
                {assetAccounts.map((acc) => {
                  const bal = accountBalances.get(acc.id) || 0;
                  return (
                    <div key={acc.id} className="py-3 flex items-center justify-between text-xs">
                      <div className="flex items-center gap-2.5">
                        <span
                          className="w-2.5 h-2.5 rounded-full"
                          style={{ backgroundColor: acc.colorHex || '#10B981' }}
                        />
                        <div>
                          <div className="font-semibold text-slate-800">
                            {LanguageHelper.getLocalizedName(acc.nameEn, acc.nameBn, languageMode)}
                          </div>
                          <div className="text-[10px] text-slate-400">
                            Initial: {LanguageHelper.formatCurrency(acc.initialBalance, languageMode)}
                          </div>
                        </div>
                      </div>
                      <span className="font-bold font-mono text-slate-900 text-sm">
                        {LanguageHelper.formatCurrency(bal, languageMode)}
                      </span>
                    </div>
                  );
                })}
              </div>
            </div>

            {/* Liabilities */}
            <div className="bg-white rounded-3xl border border-slate-200/80 shadow-xs overflow-hidden">
              <div className="p-4 bg-rose-50/70 border-b border-rose-100 flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <CreditCard className="w-5 h-5 text-rose-600" />
                  <h3 className="font-bold text-slate-900 text-sm">
                    {LanguageHelper.getString('liabilities', languageMode)}
                  </h3>
                </div>
                <span className="font-extrabold font-mono text-rose-700 text-base">
                  {LanguageHelper.formatCurrency(financialOverview.totalLiabilities, languageMode)}
                </span>
              </div>

              <div className="p-4 divide-y divide-slate-100">
                {liabilityAccounts.length === 0 ? (
                  <div className="py-8 text-center text-slate-400 text-xs">
                    No liability accounts registered.
                  </div>
                ) : (
                  liabilityAccounts.map((acc) => {
                    const bal = accountBalances.get(acc.id) || 0;
                    return (
                      <div key={acc.id} className="py-3 flex items-center justify-between text-xs">
                        <div className="flex items-center gap-2.5">
                          <span
                            className="w-2.5 h-2.5 rounded-full"
                            style={{ backgroundColor: acc.colorHex || '#EF4444' }}
                          />
                          <div>
                            <div className="font-semibold text-slate-800">
                              {LanguageHelper.getLocalizedName(acc.nameEn, acc.nameBn, languageMode)}
                            </div>
                            <div className="text-[10px] text-slate-400">
                              Initial: {LanguageHelper.formatCurrency(acc.initialBalance, languageMode)}
                            </div>
                          </div>
                        </div>
                        <span className="font-bold font-mono text-rose-600 text-sm">
                          {LanguageHelper.formatCurrency(bal, languageMode)}
                        </span>
                      </div>
                    );
                  })
                )}
              </div>
            </div>
          </div>
        </div>
      ) : (
        /* Trial Balance Table */
        <div className="bg-white rounded-3xl border border-slate-200/80 shadow-xs overflow-hidden">
          <div className="p-4 bg-slate-50 border-b border-slate-200 flex items-center justify-between text-xs font-bold text-slate-600 uppercase tracking-wider">
            <span>Account Title</span>
            <div className="flex items-center gap-8 pr-4">
              <span>Debit (Dr)</span>
              <span>Credit (Cr)</span>
            </div>
          </div>

          <div className="divide-y divide-slate-100 text-xs">
            {accounts.map((acc) => {
              const bal = accountBalances.get(acc.id) || 0;
              const isDebit = acc.type === AccountType.ASSET;
              return (
                <div key={acc.id} className="p-4 flex items-center justify-between hover:bg-slate-50/80">
                  <div className="flex items-center gap-2">
                    <span
                      className="w-2.5 h-2.5 rounded-full"
                      style={{ backgroundColor: acc.colorHex || '#3B82F6' }}
                    />
                    <span className="font-semibold text-slate-800">
                      {LanguageHelper.getLocalizedName(acc.nameEn, acc.nameBn, languageMode)}
                    </span>
                    <span className="text-[10px] text-slate-400">({acc.type})</span>
                  </div>

                  <div className="flex items-center gap-12 font-mono font-bold text-slate-900 pr-2">
                    <span className="w-24 text-right">
                      {isDebit ? LanguageHelper.formatCurrency(bal, languageMode) : '-'}
                    </span>
                    <span className="w-24 text-right">
                      {!isDebit ? LanguageHelper.formatCurrency(bal, languageMode) : '-'}
                    </span>
                  </div>
                </div>
              );
            })}
          </div>

          {/* Trial Balance Footer Sum */}
          <div className="p-4 bg-slate-100 border-t border-slate-200 flex items-center justify-between text-xs font-extrabold text-slate-900">
            <span>Total Journal Debits & Credits</span>
            <div className="flex items-center gap-12 font-mono pr-2">
              <span className="w-24 text-right text-emerald-700">
                {LanguageHelper.formatCurrency(financialOverview.totalDebits, languageMode)}
              </span>
              <span className="w-24 text-right text-emerald-700">
                {LanguageHelper.formatCurrency(financialOverview.totalCredits, languageMode)}
              </span>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
