import React, { useState, useMemo } from 'react';
import {
  Search,
  Filter,
  Download,
  Plus,
  TrendingDown,
  TrendingUp,
  ArrowLeftRight,
  CheckCircle2,
  AlertCircle,
  HelpCircle,
  XCircle,
  Calendar,
  Tag,
  ArrowUpDown,
} from 'lucide-react';
import { useBudget } from '../context/BudgetContext';
import {
  Transaction,
  TransactionType,
  TransactionStatus,
  LanguageMode,
} from '../types';
import { LanguageHelper } from '../utils/languageHelper';

interface LedgerScreenProps {
  onOpenNewTransaction: () => void;
  onOpenEditTransaction: (txId: number) => void;
}

export const LedgerScreen: React.FC<LedgerScreenProps> = ({
  onOpenNewTransaction,
  onOpenEditTransaction,
}) => {
  const {
    transactions,
    accounts,
    categories,
    languageMode,
    selectedYear,
    selectedMonth,
  } = useBudget();

  const [searchQuery, setSearchQuery] = useState<string>('');
  const [filterType, setFilterType] = useState<string>('ALL');
  const [filterAccountId, setFilterAccountId] = useState<string>('ALL');
  const [filterCategoryId, setFilterCategoryId] = useState<string>('ALL');
  const [filterStatus, setFilterStatus] = useState<string>('ALL');

  const filteredTransactions = useMemo(() => {
    return transactions.filter((tx) => {
      // Month/Year check
      const d = new Date(tx.dateEpochMs);
      const inMonth = d.getFullYear() === selectedYear && d.getMonth() + 1 === selectedMonth;

      if (!inMonth && filterType !== 'ALL_TIME') {
        // If user wants this month only
        return false;
      }

      // Type filter
      if (filterType !== 'ALL' && filterType !== 'ALL_TIME' && tx.type !== filterType) {
        return false;
      }

      // Account filter
      if (filterAccountId !== 'ALL') {
        const accId = Number(filterAccountId);
        if (tx.creditAccountId !== accId && tx.debitAccountId !== accId) {
          return false;
        }
      }

      // Category filter
      if (filterCategoryId !== 'ALL') {
        const catId = Number(filterCategoryId);
        if (tx.categoryId !== catId) {
          return false;
        }
      }

      // Status filter
      if (filterStatus !== 'ALL' && tx.status !== filterStatus) {
        return false;
      }

      // Search Query
      if (searchQuery.trim()) {
        const q = searchQuery.toLowerCase();
        const payeeMatch = tx.payeePayer?.toLowerCase().includes(q);
        const noteMatch = tx.note?.toLowerCase().includes(q);
        const tagMatch = tx.tags?.some((t) => t.toLowerCase().includes(q));
        const amountMatch = String(tx.amount).includes(q);
        if (!payeeMatch && !noteMatch && !tagMatch && !amountMatch) {
          return false;
        }
      }

      return true;
    });
  }, [
    transactions,
    selectedYear,
    selectedMonth,
    filterType,
    filterAccountId,
    filterCategoryId,
    filterStatus,
    searchQuery,
  ]);

  // Export to CSV
  const exportCsv = () => {
    const headers = [
      'ID',
      'Date',
      'Type',
      'Amount',
      'Payee/Payer',
      'Category',
      'Source (Credit)',
      'Destination (Debit)',
      'Status',
      'Note',
      'Tags',
    ];

    const rows = filteredTransactions.map((tx) => {
      const cat = categories.find((c) => c.id === tx.categoryId);
      const creditAcc = accounts.find((a) => a.id === tx.creditAccountId);
      const debitAcc = accounts.find((a) => a.id === tx.debitAccountId);

      return [
        tx.id,
        new Date(tx.dateEpochMs).toISOString().split('T')[0],
        tx.type,
        tx.amount,
        `"${(tx.payeePayer || '').replace(/"/g, '""')}"`,
        `"${(cat?.nameEn || '').replace(/"/g, '""')}"`,
        `"${(creditAcc?.nameEn || '').replace(/"/g, '""')}"`,
        `"${(debitAcc?.nameEn || '').replace(/"/g, '""')}"`,
        tx.status,
        `"${(tx.note || '').replace(/"/g, '""')}"`,
        `"${(tx.tags || []).join(';')}"`,
      ];
    });

    const csvContent =
      'data:text/csv;charset=utf-8,' +
      [headers.join(','), ...rows.map((e) => e.join(','))].join('\n');

    const encodedUri = encodeURI(csvContent);
    const link = document.createElement('a');
    link.setAttribute('href', encodedUri);
    link.setAttribute('download', `Budgeter_Ledger_${selectedYear}_${selectedMonth}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  const getStatusIcon = (status: TransactionStatus) => {
    switch (status) {
      case TransactionStatus.CLEARED:
        return <CheckCircle2 className="w-3.5 h-3.5 text-emerald-600" title="Cleared" />;
      case TransactionStatus.UNCLEARED:
        return <AlertCircle className="w-3.5 h-3.5 text-amber-500" title="Uncleared" />;
      case TransactionStatus.RECONCILED:
        return <CheckCircle2 className="w-3.5 h-3.5 text-blue-600" title="Reconciled" />;
      case TransactionStatus.VOID:
        return <XCircle className="w-3.5 h-3.5 text-rose-500" title="Void" />;
    }
  };

  return (
    <div className="space-y-4 pb-20">
      {/* Top Action Bar */}
      <div className="flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-2.5 bg-white p-4 rounded-3xl border border-slate-200/80 shadow-xs">
        {/* Search Input */}
        <div className="relative flex-1">
          <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            id="input-ledger-search"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="Search payee, memo, notes, or tags..."
            className="w-full pl-9 pr-4 py-2 bg-slate-50 border border-slate-200 rounded-2xl text-xs sm:text-sm text-slate-900 focus:bg-white focus:outline-none focus:ring-2 focus:ring-emerald-500/30"
          />
        </div>

        {/* Action Buttons */}
        <div className="flex items-center gap-2">
          <button
            onClick={exportCsv}
            className="px-3.5 py-2 bg-slate-100 hover:bg-slate-200 text-slate-700 font-semibold text-xs rounded-2xl flex items-center gap-1.5 transition-colors"
            title="Export CSV"
          >
            <Download className="w-3.5 h-3.5" />
            <span className="hidden sm:inline">Export CSV</span>
          </button>

          <button
            id="btn-add-tx-ledger"
            onClick={onOpenNewTransaction}
            className="px-4 py-2 bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-xs rounded-2xl flex items-center gap-1.5 shadow-sm active:scale-95 transition-all"
          >
            <Plus className="w-4 h-4" />
            <span>Add Transaction</span>
          </button>
        </div>
      </div>

      {/* Filter Row */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-2 bg-white p-3 rounded-2xl border border-slate-200/70 text-xs">
        {/* Type filter */}
        <select
          value={filterType}
          onChange={(e) => setFilterType(e.target.value)}
          className="p-2 bg-slate-50 border border-slate-200 rounded-xl text-slate-800 font-medium focus:outline-none"
        >
          <option value="ALL">This Month (All Types)</option>
          <option value="ALL_TIME">All Time (All Types)</option>
          <option value={TransactionType.EXPENSE}>Expenses Only</option>
          <option value={TransactionType.INCOME}>Incomes Only</option>
          <option value={TransactionType.TRANSFER}>Transfers Only</option>
        </select>

        {/* Account filter */}
        <select
          value={filterAccountId}
          onChange={(e) => setFilterAccountId(e.target.value)}
          className="p-2 bg-slate-50 border border-slate-200 rounded-xl text-slate-800 font-medium focus:outline-none"
        >
          <option value="ALL">All Accounts</option>
          {accounts.map((a) => (
            <option key={a.id} value={a.id}>
              {LanguageHelper.getLocalizedName(a.nameEn, a.nameBn, languageMode)}
            </option>
          ))}
        </select>

        {/* Category filter */}
        <select
          value={filterCategoryId}
          onChange={(e) => setFilterCategoryId(e.target.value)}
          className="p-2 bg-slate-50 border border-slate-200 rounded-xl text-slate-800 font-medium focus:outline-none"
        >
          <option value="ALL">All Categories</option>
          {categories.map((c) => (
            <option key={c.id} value={c.id}>
              {LanguageHelper.getLocalizedName(c.nameEn, c.nameBn, languageMode)}
            </option>
          ))}
        </select>

        {/* Status filter */}
        <select
          value={filterStatus}
          onChange={(e) => setFilterStatus(e.target.value)}
          className="p-2 bg-slate-50 border border-slate-200 rounded-xl text-slate-800 font-medium focus:outline-none"
        >
          <option value="ALL">All Statuses</option>
          <option value={TransactionStatus.CLEARED}>Cleared</option>
          <option value={TransactionStatus.UNCLEARED}>Uncleared</option>
          <option value={TransactionStatus.RECONCILED}>Reconciled</option>
          <option value={TransactionStatus.VOID}>Void</option>
        </select>
      </div>

      {/* Transaction List */}
      <div className="bg-white rounded-3xl border border-slate-200/80 shadow-xs overflow-hidden">
        <div className="px-5 py-3.5 bg-slate-50/70 border-b border-slate-100 flex items-center justify-between text-xs font-bold text-slate-500 uppercase tracking-wider">
          <span>Entries ({filteredTransactions.length})</span>
          <span>Amount & Flow</span>
        </div>

        {filteredTransactions.length === 0 ? (
          <div className="p-12 text-center text-slate-400 text-xs">
            {LanguageHelper.getString('no_transactions', languageMode)}
          </div>
        ) : (
          <div className="divide-y divide-slate-100">
            {filteredTransactions.map((tx) => {
              const isIncome = tx.type === TransactionType.INCOME;
              const isTransfer = tx.type === TransactionType.TRANSFER;
              const cat = categories.find((c) => c.id === tx.categoryId);
              const creditAcc = accounts.find((a) => a.id === tx.creditAccountId);
              const debitAcc = accounts.find((a) => a.id === tx.debitAccountId);

              return (
                <div
                  key={tx.id}
                  onClick={() => onOpenEditTransaction(tx.id)}
                  className="p-4 hover:bg-slate-50/90 cursor-pointer transition-colors flex items-center justify-between gap-3 text-xs"
                >
                  <div className="flex items-start gap-3">
                    <div
                      className={`w-9 h-9 rounded-2xl flex items-center justify-center font-bold text-sm shrink-0 mt-0.5 ${
                        isIncome
                          ? 'bg-emerald-100 text-emerald-700'
                          : isTransfer
                          ? 'bg-blue-100 text-blue-700'
                          : 'bg-rose-100 text-rose-700'
                      }`}
                    >
                      {isIncome ? <TrendingUp className="w-4 h-4" /> : isTransfer ? <ArrowLeftRight className="w-4 h-4" /> : <TrendingDown className="w-4 h-4" />}
                    </div>

                    <div className="space-y-0.5">
                      <div className="flex items-center gap-2">
                        <span className="font-bold text-slate-900 text-sm">
                          {tx.payeePayer || tx.note || 'Journal Entry'}
                        </span>
                        {getStatusIcon(tx.status)}
                      </div>

                      <div className="flex items-center gap-2 text-[11px] text-slate-500">
                        <span>{new Date(tx.dateEpochMs).toLocaleDateString()}</span>
                        {cat && (
                          <>
                            <span>•</span>
                            <span className="font-medium text-slate-700">
                              {LanguageHelper.getLocalizedName(cat.nameEn, cat.nameBn, languageMode)}
                            </span>
                          </>
                        )}
                      </div>

                      {/* Double-Entry Account Flow Description */}
                      <div className="text-[11px] text-slate-400 flex items-center gap-1 mt-0.5">
                        {isTransfer ? (
                          <span>
                            From {creditAcc?.nameEn || 'N/A'} → To {debitAcc?.nameEn || 'N/A'}
                          </span>
                        ) : isIncome ? (
                          <span>Credited to {debitAcc?.nameEn || 'General'}</span>
                        ) : (
                          <span>Debited from {creditAcc?.nameEn || 'General'}</span>
                        )}
                      </div>

                      {/* Tags */}
                      {tx.tags && tx.tags.length > 0 && (
                        <div className="flex items-center gap-1 pt-1 flex-wrap">
                          {tx.tags.map((tg, i) => (
                            <span
                              key={i}
                              className="px-1.5 py-0.5 bg-slate-100 text-slate-600 rounded-md text-[10px] font-medium"
                            >
                              #{tg}
                            </span>
                          ))}
                        </div>
                      )}
                    </div>
                  </div>

                  <div className="text-right shrink-0">
                    <div
                      className={`text-sm sm:text-base font-extrabold font-mono tracking-tight ${
                        isIncome
                          ? 'text-emerald-600'
                          : isTransfer
                          ? 'text-blue-600'
                          : 'text-rose-600'
                      }`}
                    >
                      {isIncome ? '+' : isTransfer ? '' : '-'}
                      {LanguageHelper.formatCurrency(tx.amount, languageMode)}
                    </div>
                    <div className="text-[10px] text-slate-400 capitalize">{tx.type.toLowerCase()}</div>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
};
