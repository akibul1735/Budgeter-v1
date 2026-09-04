import React from 'react';
import {
  Building2,
  Plus,
  ArrowUpRight,
  ArrowDownRight,
  Calculator,
  Sliders,
} from 'lucide-react';
import { useBudget } from '../context/BudgetContext';
import { Account, AccountType, LanguageMode } from '../types';
import { LanguageHelper } from '../utils/languageHelper';

interface AccountsScreenProps {
  onOpenNewAccount: () => void;
  onOpenEditAccount: (acc: Account) => void;
}

export const AccountsScreen: React.FC<AccountsScreenProps> = ({
  onOpenNewAccount,
  onOpenEditAccount,
}) => {
  const {
    accountsWithBalances,
    financialOverview,
    languageMode,
    toggleAccountCalculation,
  } = useBudget();

  const assetAccounts = accountsWithBalances.filter(
    (a) => a.account.type === AccountType.ASSET && !a.account.isArchived
  );
  const liabilityAccounts = accountsWithBalances.filter(
    (a) => a.account.type === AccountType.LIABILITY && !a.account.isArchived
  );

  return (
    <div className="space-y-4 sm:space-y-6 pb-20">
      {/* Header Banner */}
      <div className="p-6 bg-white rounded-3xl border border-slate-200/80 shadow-xs flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-lg font-bold text-slate-900 flex items-center gap-2">
            <Building2 className="w-5 h-5 text-emerald-600" />
            <span>{LanguageHelper.getString('accounts', languageMode)}</span>
          </h2>
          <p className="text-xs text-slate-500">
            Manage your bank accounts, wallets, credit cards, and cash stores
          </p>
        </div>

        <button
          onClick={onOpenNewAccount}
          className="px-4 py-2 bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-xs rounded-2xl flex items-center gap-1.5 shadow-sm active:scale-95 transition-all"
        >
          <Plus className="w-4 h-4" />
          <span>Add Account</span>
        </button>
      </div>

      {/* Asset Accounts Grid */}
      <div className="space-y-3">
        <h3 className="text-xs font-bold text-slate-700 uppercase tracking-wider px-1">
          Asset Accounts & Wallets ({assetAccounts.length})
        </h3>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
          {assetAccounts.map((item) => (
            <div
              key={item.account.id}
              onClick={() => onOpenEditAccount(item.account)}
              className="p-4 bg-white rounded-2xl border border-slate-200/80 hover:border-emerald-300 cursor-pointer transition-all shadow-2xs flex items-center justify-between"
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
                  <div className="text-[11px] text-slate-400 mt-0.5">
                    In: +{LanguageHelper.formatCurrency(item.monthlyInflow, languageMode)} • Out: -
                    {LanguageHelper.formatCurrency(item.monthlyOutflow, languageMode)}
                  </div>
                </div>
              </div>

              <div className="text-right">
                <div className="font-extrabold font-mono text-sm text-slate-900">
                  {LanguageHelper.formatCurrency(item.currentBalance, languageMode)}
                </div>
                <div className="text-[10px] text-slate-400">Available</div>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Liability Accounts Grid */}
      <div className="space-y-3 pt-2">
        <h3 className="text-xs font-bold text-rose-700 uppercase tracking-wider px-1">
          Liabilities & Credit Lines ({liabilityAccounts.length})
        </h3>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
          {liabilityAccounts.map((item) => (
            <div
              key={item.account.id}
              onClick={() => onOpenEditAccount(item.account)}
              className="p-4 bg-white rounded-2xl border border-slate-200/80 hover:border-rose-300 cursor-pointer transition-all shadow-2xs flex items-center justify-between"
            >
              <div className="flex items-center gap-3">
                <span
                  className="w-3.5 h-3.5 rounded-full"
                  style={{ backgroundColor: item.account.colorHex || '#EF4444' }}
                />
                <div>
                  <div className="font-bold text-slate-900 text-sm">
                    {LanguageHelper.getLocalizedName(
                      item.account.nameEn,
                      item.account.nameBn,
                      languageMode
                    )}
                  </div>
                  <div className="text-[11px] text-slate-400 mt-0.5">Credit / Loan Liability</div>
                </div>
              </div>

              <div className="text-right">
                <div className="font-extrabold font-mono text-sm text-rose-600">
                  {LanguageHelper.formatCurrency(item.currentBalance, languageMode)}
                </div>
                <div className="text-[10px] text-slate-400">Outstanding Due</div>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};
