import React, { useState, useEffect, useMemo } from 'react';
import {
  X,
  TrendingDown,
  TrendingUp,
  ArrowLeftRight,
  Calendar,
  Tag,
  FileText,
  Building2,
  FolderTree,
  Calculator,
  Plus,
  Check,
  Sparkles,
} from 'lucide-react';
import { useBudget } from '../context/BudgetContext';
import {
  Transaction,
  TransactionType,
  TransactionStatus,
  AccountType,
  CategoryType,
  LanguageMode,
} from '../types';
import { LanguageHelper } from '../utils/languageHelper';
import { CalculatorModal } from './CalculatorModal';

interface AddTransactionModalProps {
  isOpen: boolean;
  onClose: () => void;
  editingTransaction?: Transaction | null;
  defaultType?: TransactionType;
  defaultAccountId?: number | null;
  defaultCategoryId?: number | null;
}

export const AddTransactionModal: React.FC<AddTransactionModalProps> = ({
  isOpen,
  onClose,
  editingTransaction,
  defaultType = TransactionType.EXPENSE,
  defaultAccountId,
  defaultCategoryId,
}) => {
  const {
    accounts,
    categories,
    transactions,
    addTransaction,
    updateTransaction,
    deleteTransaction,
    languageMode,
    autofillConfig,
  } = useBudget();

  const [type, setType] = useState<TransactionType>(defaultType);
  const [amountStr, setAmountStr] = useState<string>('');
  const [dateStr, setDateStr] = useState<string>(() => new Date().toISOString().split('T')[0]);
  const [creditAccountId, setCreditAccountId] = useState<number | null>(null);
  const [debitAccountId, setDebitAccountId] = useState<number | null>(null);
  const [categoryId, setCategoryId] = useState<number | null>(null);
  const [note, setNote] = useState<string>('');
  const [payeePayer, setPayeePayer] = useState<string>('');
  const [status, setStatus] = useState<TransactionStatus>(TransactionStatus.CLEARED);
  const [tagsInput, setTagsInput] = useState<string>('');
  const [keepOpen, setKeepOpen] = useState<boolean>(false);
  const [showCalc, setShowCalc] = useState<boolean>(false);

  // Initialize or Reset form
  useEffect(() => {
    if (editingTransaction) {
      setType(editingTransaction.type);
      setAmountStr(String(editingTransaction.amount));
      setDateStr(new Date(editingTransaction.dateEpochMs).toISOString().split('T')[0]);
      setCreditAccountId(editingTransaction.creditAccountId);
      setDebitAccountId(editingTransaction.debitAccountId);
      setCategoryId(editingTransaction.categoryId);
      setNote(editingTransaction.note || '');
      setPayeePayer(editingTransaction.payeePayer || '');
      setStatus(editingTransaction.status);
      setTagsInput(editingTransaction.tags ? editingTransaction.tags.join(', ') : '');
    } else {
      setType(defaultType);
      setAmountStr('');
      setDateStr(new Date().toISOString().split('T')[0]);
      setNote('');
      setPayeePayer('');
      setStatus(TransactionStatus.CLEARED);
      setTagsInput('');

      // Smart Defaults based on type
      if (defaultType === TransactionType.EXPENSE) {
        setCreditAccountId(defaultAccountId || accounts.find((a) => a.type === AccountType.ASSET)?.id || null);
        setDebitAccountId(null);
        setCategoryId(defaultCategoryId || categories.find((c) => c.type === CategoryType.EXPENSE)?.id || null);
      } else if (defaultType === TransactionType.INCOME) {
        setCreditAccountId(null);
        setDebitAccountId(defaultAccountId || accounts.find((a) => a.type === AccountType.ASSET)?.id || null);
        setCategoryId(defaultCategoryId || categories.find((c) => c.type === CategoryType.INCOME)?.id || null);
      } else {
        const assets = accounts.filter((a) => a.type === AccountType.ASSET);
        setCreditAccountId(assets[0]?.id || null);
        setDebitAccountId(assets[1]?.id || assets[0]?.id || null);
        setCategoryId(null);
      }
    }
  }, [editingTransaction, defaultType, defaultAccountId, defaultCategoryId, accounts, categories, isOpen]);

  // Autofill suggestions based on Payee/Payer
  const pastSuggestions = useMemo(() => {
    if (!payeePayer.trim() || editingTransaction) return [];
    const lower = payeePayer.toLowerCase().trim();
    const seen = new Set<string>();
    const matches: Transaction[] = [];

    for (const t of transactions) {
      if (t.payeePayer && t.payeePayer.toLowerCase().includes(lower)) {
        const key = `${t.payeePayer}-${t.categoryId}-${t.creditAccountId}-${t.debitAccountId}`;
        if (!seen.has(key)) {
          seen.add(key);
          matches.push(t);
          if (matches.length >= 3) break;
        }
      }
    }
    return matches;
  }, [payeePayer, transactions, editingTransaction]);

  const applySuggestion = (tx: Transaction) => {
    if (autofillConfig.autofillAmount && tx.amount > 0) setAmountStr(String(tx.amount));
    if (autofillConfig.autofillCategory && tx.categoryId) setCategoryId(tx.categoryId);
    if (autofillConfig.autofillAccount) {
      if (tx.creditAccountId) setCreditAccountId(tx.creditAccountId);
      if (tx.debitAccountId) setDebitAccountId(tx.debitAccountId);
    }
    if (autofillConfig.autofillNotes && tx.note) setNote(tx.note);
    if (autofillConfig.autofillLabels && tx.tags) setTagsInput(tx.tags.join(', '));
    setPayeePayer(tx.payeePayer);
  };

  if (!isOpen) return null;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const parsedAmount = parseFloat(amountStr);
    if (isNaN(parsedAmount) || parsedAmount <= 0) {
      alert('Please enter a valid positive amount.');
      return;
    }

    const epochDate = new Date(dateStr).getTime() || Date.now();
    const tagList = tagsInput
      .split(',')
      .map((t) => t.trim())
      .filter(Boolean);

    const txData = {
      type,
      amount: parsedAmount,
      dateEpochMs: epochDate,
      creditAccountId: type === TransactionType.INCOME ? null : creditAccountId,
      debitAccountId: type === TransactionType.EXPENSE ? null : debitAccountId,
      categoryId: type === TransactionType.TRANSFER ? null : categoryId,
      note,
      payeePayer,
      status,
      labelIds: [],
      tags: tagList,
    };

    if (editingTransaction) {
      updateTransaction({
        ...editingTransaction,
        ...txData,
      });
      onClose();
    } else {
      addTransaction(txData);
      if (keepOpen) {
        setAmountStr('');
        setNote('');
        setPayeePayer('');
      } else {
        onClose();
      }
    }
  };

  const handleDelete = () => {
    if (editingTransaction && confirm('Are you sure you want to delete this transaction?')) {
      deleteTransaction(editingTransaction.id);
      onClose();
    }
  };

  const filteredCategories = categories.filter((c) =>
    type === TransactionType.EXPENSE ? c.type === CategoryType.EXPENSE : c.type === CategoryType.INCOME
  );

  return (
    <>
      <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 backdrop-blur-xs p-3 sm:p-4 overflow-y-auto animate-in fade-in duration-150">
        <div className="w-full max-w-lg bg-white rounded-3xl shadow-2xl border border-slate-200 overflow-hidden my-auto flex flex-col max-h-[92vh]">
          {/* Top Bar */}
          <div className="px-5 py-4 border-b border-slate-100 flex items-center justify-between bg-slate-50/70">
            <h2 className="font-bold text-slate-900 text-base">
              {editingTransaction
                ? LanguageHelper.getString('edit_transaction', languageMode)
                : LanguageHelper.getString('add_transaction', languageMode)}
            </h2>
            <button
              onClick={onClose}
              className="p-1.5 rounded-xl text-slate-400 hover:text-slate-700 hover:bg-slate-200/60"
            >
              <X className="w-5 h-5" />
            </button>
          </div>

          {/* Form Content */}
          <form onSubmit={handleSubmit} className="p-5 overflow-y-auto space-y-4 flex-1">
            {/* Transaction Type Tabs */}
            <div className="grid grid-cols-3 gap-1.5 bg-slate-100 p-1 rounded-2xl">
              <button
                type="button"
                id="btn-tx-type-expense"
                onClick={() => setType(TransactionType.EXPENSE)}
                className={`flex items-center justify-center gap-1.5 py-2 rounded-xl text-xs font-bold transition-all ${
                  type === TransactionType.EXPENSE
                    ? 'bg-rose-500 text-white shadow-xs'
                    : 'text-slate-600 hover:text-slate-900'
                }`}
              >
                <TrendingDown className="w-4 h-4" />
                <span>{LanguageHelper.getString('expense', languageMode)}</span>
              </button>

              <button
                type="button"
                id="btn-tx-type-income"
                onClick={() => setType(TransactionType.INCOME)}
                className={`flex items-center justify-center gap-1.5 py-2 rounded-xl text-xs font-bold transition-all ${
                  type === TransactionType.INCOME
                    ? 'bg-emerald-600 text-white shadow-xs'
                    : 'text-slate-600 hover:text-slate-900'
                }`}
              >
                <TrendingUp className="w-4 h-4" />
                <span>{LanguageHelper.getString('income', languageMode)}</span>
              </button>

              <button
                type="button"
                id="btn-tx-type-transfer"
                onClick={() => setType(TransactionType.TRANSFER)}
                className={`flex items-center justify-center gap-1.5 py-2 rounded-xl text-xs font-bold transition-all ${
                  type === TransactionType.TRANSFER
                    ? 'bg-blue-600 text-white shadow-xs'
                    : 'text-slate-600 hover:text-slate-900'
                }`}
              >
                <ArrowLeftRight className="w-4 h-4" />
                <span>{LanguageHelper.getString('transfer', languageMode)}</span>
              </button>
            </div>

            {/* Amount Field with Quick Calculator Button */}
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">
                {LanguageHelper.getString('amount', languageMode)} *
              </label>
              <div className="relative flex items-center">
                <input
                  type="number"
                  step="any"
                  id="input-tx-amount"
                  value={amountStr}
                  onChange={(e) => setAmountStr(e.target.value)}
                  placeholder="0.00"
                  required
                  autoFocus
                  className="w-full pl-4 pr-12 py-3 bg-slate-50 border border-slate-200 rounded-2xl text-xl font-bold text-slate-900 focus:bg-white focus:outline-none focus:ring-2 focus:ring-emerald-500/30 focus:border-emerald-500 transition-all font-mono"
                />
                <button
                  type="button"
                  onClick={() => setShowCalc(true)}
                  className="absolute right-2 p-2 bg-emerald-50 hover:bg-emerald-100 text-emerald-700 rounded-xl transition-colors"
                  title="Open Calculator"
                >
                  <Calculator className="w-4 h-4" />
                </button>
              </div>
            </div>

            {/* Payee / Payer & Autofill Suggestions */}
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">
                {LanguageHelper.getString('payee_payer', languageMode)}
              </label>
              <input
                type="text"
                id="input-tx-payee"
                value={payeePayer}
                onChange={(e) => setPayeePayer(e.target.value)}
                placeholder="e.g., Grocery store, Employer, Landlord"
                className="w-full px-3.5 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm text-slate-900 focus:bg-white focus:outline-none focus:ring-2 focus:ring-emerald-500/30 focus:border-emerald-500"
              />

              {/* Suggestions Pill List */}
              {pastSuggestions.length > 0 && (
                <div className="mt-2 flex items-center gap-1.5 flex-wrap">
                  <span className="text-[11px] font-medium text-slate-400 flex items-center gap-1">
                    <Sparkles className="w-3 h-3 text-amber-500" />
                    Auto-fill:
                  </span>
                  {pastSuggestions.map((s, idx) => (
                    <button
                      key={idx}
                      type="button"
                      onClick={() => applySuggestion(s)}
                      className="px-2 py-1 bg-amber-50 hover:bg-amber-100 border border-amber-200 text-amber-900 rounded-lg text-[11px] font-medium transition-all"
                    >
                      {s.payeePayer} (৳{s.amount})
                    </button>
                  ))}
                </div>
              )}
            </div>

            {/* Category (for Expense & Income) */}
            {type !== TransactionType.TRANSFER && (
              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">
                  {LanguageHelper.getString('select_category', languageMode)} *
                </label>
                <select
                  id="select-tx-category"
                  value={categoryId || ''}
                  onChange={(e) => setCategoryId(Number(e.target.value) || null)}
                  required
                  className="w-full px-3.5 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm text-slate-900 focus:bg-white focus:outline-none focus:ring-2 focus:ring-emerald-500/30 focus:border-emerald-500"
                >
                  <option value="">-- Choose Category --</option>
                  {filteredCategories.map((c) => (
                    <option key={c.id} value={c.id}>
                      {LanguageHelper.getLocalizedName(c.nameEn, c.nameBn, languageMode)}
                    </option>
                  ))}
                </select>
              </div>
            )}

            {/* Accounts Selectors (Double-Entry Flow) */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              {/* Credit Account (Money Out / Source) */}
              {type !== TransactionType.INCOME && (
                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">
                    {type === TransactionType.TRANSFER
                      ? LanguageHelper.getString('source_account', languageMode)
                      : LanguageHelper.getString('credit_account', languageMode)}{' '}
                    *
                  </label>
                  <select
                    id="select-tx-credit-account"
                    value={creditAccountId || ''}
                    onChange={(e) => setCreditAccountId(Number(e.target.value) || null)}
                    required
                    className="w-full px-3 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-xs sm:text-sm text-slate-900 focus:bg-white focus:outline-none focus:ring-2 focus:ring-emerald-500/30 focus:border-emerald-500"
                  >
                    <option value="">-- Source Account --</option>
                    {accounts.map((a) => (
                      <option key={a.id} value={a.id}>
                        {LanguageHelper.getLocalizedName(a.nameEn, a.nameBn, languageMode)} ({a.type})
                      </option>
                    ))}
                  </select>
                </div>
              )}

              {/* Debit Account (Money In / Destination) */}
              {type !== TransactionType.EXPENSE && (
                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">
                    {type === TransactionType.TRANSFER
                      ? LanguageHelper.getString('destination_account', languageMode)
                      : LanguageHelper.getString('debit_account', languageMode)}{' '}
                    *
                  </label>
                  <select
                    id="select-tx-debit-account"
                    value={debitAccountId || ''}
                    onChange={(e) => setDebitAccountId(Number(e.target.value) || null)}
                    required
                    className="w-full px-3 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-xs sm:text-sm text-slate-900 focus:bg-white focus:outline-none focus:ring-2 focus:ring-emerald-500/30 focus:border-emerald-500"
                  >
                    <option value="">-- Destination Account --</option>
                    {accounts.map((a) => (
                      <option key={a.id} value={a.id}>
                        {LanguageHelper.getLocalizedName(a.nameEn, a.nameBn, languageMode)} ({a.type})
                      </option>
                    ))}
                  </select>
                </div>
              )}
            </div>

            {/* Date & Status */}
            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">
                  {LanguageHelper.getString('date', languageMode)}
                </label>
                <input
                  type="date"
                  id="input-tx-date"
                  value={dateStr}
                  onChange={(e) => setDateStr(e.target.value)}
                  className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl text-xs text-slate-900 focus:bg-white focus:outline-none"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">
                  {LanguageHelper.getString('status', languageMode)}
                </label>
                <select
                  id="select-tx-status"
                  value={status}
                  onChange={(e) => setStatus(e.target.value as TransactionStatus)}
                  className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl text-xs text-slate-900 focus:bg-white focus:outline-none"
                >
                  <option value={TransactionStatus.CLEARED}>Cleared</option>
                  <option value={TransactionStatus.UNCLEARED}>Uncleared</option>
                  <option value={TransactionStatus.RECONCILED}>Reconciled</option>
                  <option value={TransactionStatus.VOID}>Void</option>
                </select>
              </div>
            </div>

            {/* Notes & Tags */}
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">
                {LanguageHelper.getString('notes', languageMode)}
              </label>
              <textarea
                id="input-tx-notes"
                value={note}
                onChange={(e) => setNote(e.target.value)}
                rows={2}
                placeholder="Memo or breakdown details..."
                className="w-full px-3.5 py-2 bg-slate-50 border border-slate-200 rounded-xl text-xs text-slate-900 focus:bg-white focus:outline-none focus:ring-2 focus:ring-emerald-500/30"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">
                Tags (Comma separated)
              </label>
              <input
                type="text"
                id="input-tx-tags"
                value={tagsInput}
                onChange={(e) => setTagsInput(e.target.value)}
                placeholder="e.g. food, weekend, reimbursable"
                className="w-full px-3.5 py-2 bg-slate-50 border border-slate-200 rounded-xl text-xs text-slate-900 focus:bg-white focus:outline-none"
              />
            </div>

            {/* Continuous Adding Checkbox */}
            {!editingTransaction && (
              <label className="flex items-center gap-2 text-xs font-medium text-slate-700 cursor-pointer pt-1">
                <input
                  type="checkbox"
                  checked={keepOpen}
                  onChange={(e) => setKeepOpen(e.target.checked)}
                  className="rounded border-slate-300 text-emerald-600 focus:ring-emerald-500"
                />
                <span>{LanguageHelper.getString('quick_add_consecutive', languageMode)}</span>
              </label>
            )}

            {/* Action Buttons */}
            <div className="pt-3 flex items-center justify-between gap-2 border-t border-slate-100">
              {editingTransaction ? (
                <button
                  type="button"
                  id="btn-delete-tx"
                  onClick={handleDelete}
                  className="px-4 py-2.5 rounded-xl bg-rose-50 hover:bg-rose-100 text-rose-700 font-bold text-xs transition-colors"
                >
                  {LanguageHelper.getString('delete', languageMode)}
                </button>
              ) : (
                <button
                  type="button"
                  onClick={onClose}
                  className="px-4 py-2.5 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-700 font-bold text-xs transition-colors"
                >
                  {LanguageHelper.getString('cancel', languageMode)}
                </button>
              )}

              <button
                type="submit"
                id="btn-save-tx"
                className="px-6 py-2.5 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-xs flex items-center gap-1.5 shadow-sm active:scale-95 transition-all"
              >
                <Check className="w-4 h-4" />
                <span>{LanguageHelper.getString('save', languageMode)}</span>
              </button>
            </div>
          </form>
        </div>
      </div>

      {/* Embedded Calculator Dialog */}
      <CalculatorModal
        isOpen={showCalc}
        onClose={() => setShowCalc(false)}
        initialValue={parseFloat(amountStr) || 0}
        onApplyResult={(val) => setAmountStr(String(val))}
      />
    </>
  );
};
