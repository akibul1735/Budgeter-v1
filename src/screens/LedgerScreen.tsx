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
  Building2,
  FolderTree,
  Receipt,
  FileText,
} from 'lucide-react';
import { useBudget } from '../context/BudgetContext';
import {
  Transaction,
  TransactionType,
  TransactionStatus,
  LanguageMode,
  HierarchyDisplayMode,
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
    hierarchyDisplayMode,
    getTransactionAccountRunningBalance,
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
        return false;
      }

      // Type filter
      if (filterType !== 'ALL' && filterType !== 'ALL_TIME' && tx.type !== filterType) {
        return false;
      }

      // Account filter
      if (filterAccountId !== 'ALL') {
        const accId = Number(filterAccountId);
        if (tx.creditAccountId !== accId && tx.debitAccountId !== accId && tx.feeAccountId !== accId) {
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
      'Payee/Name',
      'Group_Category',
      'Source_Account',
      'Destination_Account',
      'Account_Balance_After',
      'Status',
      'Note',
      'Tags',
    ];

    const rows = filteredTransactions.map((tx) => {
      const cat = categories.find((c) => c.id === tx.categoryId);
      const catHierarchy = cat ? LanguageHelper.getCategoryHierarchy(cat, categories, languageMode) : null;
      const creditAcc = accounts.find((a) => a.id === tx.creditAccountId);
      const debitAcc = accounts.find((a) => a.id === tx.debitAccountId);
      const primaryAccId = tx.creditAccountId || tx.debitAccountId || 0;
      const postBalance = getTransactionAccountRunningBalance(tx.id, primaryAccId);

      return [
        tx.id,
        new Date(tx.dateEpochMs).toISOString().split('T')[0],
        tx.type,
        tx.amount,
        `"${(tx.payeePayer || tx.note || 'Journal Entry').replace(/"/g, '""')}"`,
        `"${(catHierarchy?.singleLine || '').replace(/"/g, '""')}"`,
        `"${(creditAcc?.nameEn || '').replace(/"/g, '""')}"`,
        `"${(debitAcc?.nameEn || '').replace(/"/g, '""')}"`,
        postBalance,
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

      {/* Transaction List with Redesigned Transaction Cards */}
      <div className="bg-white rounded-3xl border border-slate-200/80 shadow-xs overflow-hidden">
        <div className="px-5 py-3.5 bg-slate-50/70 border-b border-slate-100 flex items-center justify-between text-xs font-bold text-slate-500 uppercase tracking-wider">
          <span>Entries ({filteredTransactions.length})</span>
          <span>Amount & Account Balance</span>
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
              const isFee = !!tx.isTransferFee;

              // Entity Lookups
              const cat = categories.find((c) => c.id === tx.categoryId);
              const catHierarchy = cat
                ? LanguageHelper.getCategoryHierarchy(cat, categories, languageMode)
                : null;

              const creditAcc = accounts.find((a) => a.id === tx.creditAccountId);
              const debitAcc = accounts.find((a) => a.id === tx.debitAccountId);

              // Hierarchy for Accounts
              const creditAccHierarchy = creditAcc
                ? LanguageHelper.getAccountHierarchy(creditAcc, accounts, languageMode)
                : null;
              const debitAccHierarchy = debitAcc
                ? LanguageHelper.getAccountHierarchy(debitAcc, accounts, languageMode)
                : null;

              // Primary Account for Balance Display
              const displayAccountId = isIncome
                ? tx.debitAccountId
                : tx.creditAccountId || tx.debitAccountId;

              const displayAccount = accounts.find((a) => a.id === displayAccountId);
              const runningBalance = displayAccountId
                ? getTransactionAccountRunningBalance(tx.id, displayAccountId)
                : 0;

              // Transaction Name (Left Top)
              const transactionName =
                tx.payeePayer ||
                (isTransfer
                  ? 'Account Transfer'
                  : isIncome
                  ? 'Income Receipt'
                  : tx.note || 'Journal Entry');

              // Category Hierarchy formatting (Left Below)
              let categoryDisplayNode: React.ReactNode = null;
              if (cat && catHierarchy) {
                if (hierarchyDisplayMode === HierarchyDisplayMode.DOUBLE_LINE) {
                  categoryDisplayNode = (
                    <div className="text-[11px] leading-tight">
                      <span className="text-slate-400 font-medium">&gt;{catHierarchy.groupName}</span>{' '}
                      <span className="font-semibold text-slate-700">{catHierarchy.categoryName}</span>
                    </div>
                  );
                } else {
                  categoryDisplayNode = (
                    <div className="text-[11px] font-semibold text-slate-700 truncate">
                      {catHierarchy.singleLine}
                    </div>
                  );
                }
              } else if (isTransfer) {
                if (hierarchyDisplayMode === HierarchyDisplayMode.DOUBLE_LINE) {
                  categoryDisplayNode = (
                    <div className="text-[11px] text-slate-600 font-medium flex items-center gap-1">
                      <span>&gt;{creditAccHierarchy?.accountName || 'Source'}</span>
                      <ArrowLeftRight className="w-3 h-3 text-blue-500 shrink-0" />
                      <span>&gt;{debitAccHierarchy?.accountName || 'Dest'}</span>
                    </div>
                  );
                } else {
                  categoryDisplayNode = (
                    <div className="text-[11px] text-slate-600 font-semibold truncate">
                      {creditAccHierarchy?.singleLine || 'Source'} &gt; {debitAccHierarchy?.singleLine || 'Dest'}
                    </div>
                  );
                }
              }

              // Account Name & Balance (Right Below)
              const accountDisplayName = displayAccount
                ? LanguageHelper.getLocalizedName(displayAccount.nameEn, displayAccount.nameBn, languageMode)
                : 'Account';

              return (
                <div
                  key={tx.id}
                  onClick={() => onOpenEditTransaction(tx.id)}
                  className="p-4 hover:bg-slate-50/90 cursor-pointer transition-colors flex items-start justify-between gap-3 text-xs"
                >
                  {/* Left Section: Name, Categories, Labels (Round Shape) & Notes */}
                  <div className="min-w-0 flex-1 space-y-1.5">
                    {/* Left Top: Name + Status Icon */}
                    <div className="flex items-center gap-2">
                      <span className="font-bold text-slate-900 text-sm sm:text-base truncate">
                        {transactionName}
                      </span>
                      {getStatusIcon(tx.status)}
                      {isFee && (
                        <span className="px-1.5 py-0.5 bg-amber-100 text-amber-800 rounded-md text-[10px] font-bold">
                          Fee
                        </span>
                      )}
                    </div>

                    {/* Left Below: Categories ("Groups > Category") */}
                    {categoryDisplayNode && (
                      <div className="flex items-center gap-1.5">
                        <FolderTree className="w-3.5 h-3.5 text-slate-400 shrink-0" />
                        {categoryDisplayNode}
                      </div>
                    )}

                    {/* Below Categories: Labels (in a round shape) on left, and just right notes if any */}
                    <div className="flex items-center gap-2 flex-wrap pt-0.5">
                      {/* Round shape Labels */}
                      {tx.tags && tx.tags.length > 0 && (
                        <div className="flex items-center gap-1.5 flex-wrap">
                          {tx.tags.map((tag, idx) => (
                            <span
                              key={idx}
                              className="px-2.5 py-0.5 bg-slate-100 hover:bg-slate-200 text-slate-700 rounded-full text-[11px] font-medium border border-slate-200/80 shadow-2xs"
                            >
                              {tag}
                            </span>
                          ))}
                        </div>
                      )}

                      {/* Notes (just right of labels) */}
                      {tx.note && (
                        <div className="flex items-center gap-1 text-[11px] text-slate-500 italic truncate max-w-xs">
                          <FileText className="w-3 h-3 text-slate-400 shrink-0" />
                          <span className="truncate">{tx.note}</span>
                        </div>
                      )}
                    </div>
                  </div>

                  {/* Right Section: Right Top: Amount (with color & sign), Right Below: Account name and Amount of this account (after this transaction) */}
                  <div className="text-right shrink-0 min-w-[110px] sm:min-w-[140px] space-y-1">
                    {/* Right Top: Amount (with color and sign) */}
                    <div
                      className={`text-sm sm:text-base font-extrabold font-mono tracking-tight ${
                        isIncome
                          ? 'text-emerald-600'
                          : isTransfer
                          ? 'text-blue-600'
                          : isFee
                          ? 'text-amber-600'
                          : 'text-rose-600'
                      }`}
                    >
                      {isIncome ? '+' : isTransfer ? '' : '-'}
                      {LanguageHelper.formatCurrency(tx.amount, languageMode)}
                    </div>

                    {/* Right Below: Account Name and Post-Transaction Balance */}
                    <div className="text-[11px] text-slate-500 leading-tight">
                      <div className="font-medium text-slate-700 truncate max-w-[130px] sm:max-w-[160px] ml-auto">
                        {accountDisplayName}
                      </div>
                      <div className="font-mono text-[10px] text-slate-400">
                        {LanguageHelper.formatCurrency(runningBalance, languageMode)}
                      </div>
                    </div>
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
