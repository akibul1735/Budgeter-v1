import React, { useState, useEffect, useMemo } from 'react';
import {
  X,
  TrendingDown,
  TrendingUp,
  ArrowLeftRight,
  ArrowDown,
  ArrowUp,
  Calendar,
  Tag,
  FileText,
  Building2,
  FolderTree,
  Calculator,
  Plus,
  Check,
  Sparkles,
  Receipt,
  Percent,
} from 'lucide-react';
import { useBudget } from '../context/BudgetContext';
import {
  Transaction,
  TransactionType,
  TransactionStatus,
  AccountType,
  CategoryType,
  LanguageMode,
  HierarchyDisplayMode,
} from '../types';
import { LanguageHelper } from '../utils/languageHelper';
import { CalculatorModal } from './CalculatorModal';
import { HierarchyPicker } from './HierarchyPicker';

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
    addTransferWithFee,
    updateTransaction,
    deleteTransaction,
    languageMode,
    autofillConfig,
    hierarchyDisplayMode,
    getRememberedFeeCategoryId,
    saveFeeCategoryPreference,
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
  const [calcTarget, setCalcTarget] = useState<'amount' | 'fee'>('amount');

  // Transfer Fee States
  const [feeAmountStr, setFeeAmountStr] = useState<string>('');
  const [feeAccountId, setFeeAccountId] = useState<number | null>(null);
  const [feeCategoryId, setFeeCategoryId] = useState<number | null>(null);
  const [hasTransferFee, setHasTransferFee] = useState<boolean>(false);

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

      if (editingTransaction.feeAmount && editingTransaction.feeAmount > 0) {
        setHasTransferFee(true);
        setFeeAmountStr(String(editingTransaction.feeAmount));
        setFeeAccountId(editingTransaction.feeAccountId || editingTransaction.creditAccountId);
        setFeeCategoryId(editingTransaction.feeCategoryId || getRememberedFeeCategoryId(editingTransaction.payeePayer));
      } else {
        setHasTransferFee(false);
        setFeeAmountStr('');
        setFeeAccountId(editingTransaction.creditAccountId);
        setFeeCategoryId(getRememberedFeeCategoryId(editingTransaction.payeePayer));
      }
    } else {
      setType(defaultType);
      setAmountStr('');
      setDateStr(new Date().toISOString().split('T')[0]);
      setNote('');
      setPayeePayer('');
      setStatus(TransactionStatus.CLEARED);
      setTagsInput('');
      setHasTransferFee(false);
      setFeeAmountStr('');

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
        const sourceAccId = defaultAccountId || assets[0]?.id || null;
        const destAccId = assets[1]?.id || assets[0]?.id || null;
        setCreditAccountId(sourceAccId);
        setDebitAccountId(destAccId);
        setCategoryId(null);
        setFeeAccountId(sourceAccId);
        setFeeCategoryId(getRememberedFeeCategoryId());
      }
    }
  }, [editingTransaction, defaultType, defaultAccountId, defaultCategoryId, accounts, categories, isOpen, getRememberedFeeCategoryId]);

  // Update fee account default whenever source account changes
  useEffect(() => {
    if (type === TransactionType.TRANSFER && !feeAccountId) {
      setFeeAccountId(creditAccountId);
    }
  }, [creditAccountId, type, feeAccountId]);

  // When payeePayer changes in transfer mode, auto-fill remembered fee category
  useEffect(() => {
    if (type === TransactionType.TRANSFER && payeePayer) {
      const remembered = getRememberedFeeCategoryId(payeePayer);
      if (remembered) {
        setFeeCategoryId(remembered);
      }
    }
  }, [payeePayer, type, getRememberedFeeCategoryId]);

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

  const handleSwapAccounts = () => {
    const temp = creditAccountId;
    setCreditAccountId(debitAccountId);
    setDebitAccountId(temp);
    if (feeAccountId === creditAccountId) {
      setFeeAccountId(debitAccountId);
    }
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

    const parsedFee = parseFloat(feeAmountStr);
    const hasValidFee = hasTransferFee && !isNaN(parsedFee) && parsedFee > 0;

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
      feeAmount: hasValidFee ? parsedFee : 0,
      feeAccountId: hasValidFee ? feeAccountId || creditAccountId : null,
      feeCategoryId: hasValidFee ? feeCategoryId : null,
    };

    if (editingTransaction) {
      updateTransaction({
        ...editingTransaction,
        ...txData,
      });
      if (hasValidFee && feeCategoryId) {
        if (payeePayer) saveFeeCategoryPreference(payeePayer, feeCategoryId);
        saveFeeCategoryPreference('default', feeCategoryId);
      }
      onClose();
    } else {
      if (type === TransactionType.TRANSFER && hasValidFee) {
        addTransferWithFee(
          txData,
          parsedFee,
          feeAccountId || creditAccountId,
          feeCategoryId || getRememberedFeeCategoryId() || 14
        );
      } else {
        addTransaction(txData);
      }

      if (keepOpen) {
        setAmountStr('');
        setFeeAmountStr('');
        setHasTransferFee(false);
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

  const expenseCategories = categories.filter((c) => c.type === CategoryType.EXPENSE);

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
                  onClick={() => {
                    setCalcTarget('amount');
                    setShowCalc(true);
                  }}
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
                {type === TransactionType.TRANSFER
                  ? 'Transfer Title / Payee (e.g. bKash to Bank)'
                  : LanguageHelper.getString('payee_payer', languageMode)}
              </label>
              <input
                type="text"
                id="input-tx-payee"
                value={payeePayer}
                onChange={(e) => setPayeePayer(e.target.value)}
                placeholder={
                  type === TransactionType.TRANSFER
                    ? 'e.g. Self Transfer, Sonali Bank, Friend'
                    : 'e.g. Grocery store, Employer, Landlord'
                }
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

            {/* Category (for Expense & Income) with Two-Line / One-Line Display */}
            {type !== TransactionType.TRANSFER && (
              <div>
                <HierarchyPicker
                  id="select-tx-category"
                  label={LanguageHelper.getString('select_category', languageMode)}
                  selectedId={categoryId}
                  items={filteredCategories}
                  allGroups={categories}
                  onChange={(id) => setCategoryId(id)}
                  hierarchyMode={hierarchyDisplayMode}
                  languageMode={languageMode}
                  placeholder="-- Select Category --"
                  required
                />
              </div>
            )}

            {/* Accounts Selectors with Two-Line / One-Line Display */}
            {type === TransactionType.TRANSFER ? (
              /* Dedicated Transfer Account Selection with Swap button */
              <div className="space-y-3 p-3.5 bg-slate-50/80 border border-slate-200/80 rounded-2xl">
                <div className="relative space-y-3">
                  {/* Source Account (Money Out) */}
                  <HierarchyPicker
                    id="select-tx-source-account"
                    label={LanguageHelper.getString('source_account', languageMode)}
                    selectedId={creditAccountId}
                    items={accounts}
                    allGroups={accounts}
                    onChange={(id) => {
                      setCreditAccountId(id);
                      if (!hasTransferFee || !feeAccountId) {
                        setFeeAccountId(id);
                      }
                    }}
                    hierarchyMode={hierarchyDisplayMode}
                    languageMode={languageMode}
                    placeholder="-- Source Account --"
                    isAccount
                    required
                    icon={<ArrowDown className="w-4 h-4 text-rose-500" />}
                  />

                  {/* Swap Button */}
                  <div className="flex justify-center -my-2 relative z-10">
                    <button
                      type="button"
                      onClick={handleSwapAccounts}
                      className="p-1.5 bg-white hover:bg-slate-100 border border-slate-300 rounded-full shadow-xs text-slate-600 hover:text-slate-900 transition-transform active:scale-90"
                      title="Swap Source and Destination"
                    >
                      <ArrowLeftRight className="w-3.5 h-3.5" />
                    </button>
                  </div>

                  {/* Destination Account (Money In) */}
                  <HierarchyPicker
                    id="select-tx-dest-account"
                    label={LanguageHelper.getString('destination_account', languageMode)}
                    selectedId={debitAccountId}
                    items={accounts}
                    allGroups={accounts}
                    onChange={(id) => setDebitAccountId(id)}
                    hierarchyMode={hierarchyDisplayMode}
                    languageMode={languageMode}
                    placeholder="-- Destination Account --"
                    isAccount
                    required
                    icon={<ArrowUp className="w-4 h-4 text-emerald-600" />}
                  />
                </div>
              </div>
            ) : (
              /* Single Mode Accounts */
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                {type !== TransactionType.INCOME && (
                  <HierarchyPicker
                    id="select-tx-credit-account"
                    label={LanguageHelper.getString('credit_account', languageMode)}
                    selectedId={creditAccountId}
                    items={accounts}
                    allGroups={accounts}
                    onChange={(id) => setCreditAccountId(id)}
                    hierarchyMode={hierarchyDisplayMode}
                    languageMode={languageMode}
                    placeholder="-- Credit Account --"
                    isAccount
                    required
                  />
                )}

                {type !== TransactionType.EXPENSE && (
                  <HierarchyPicker
                    id="select-tx-debit-account"
                    label={LanguageHelper.getString('debit_account', languageMode)}
                    selectedId={debitAccountId}
                    items={accounts}
                    allGroups={accounts}
                    onChange={(id) => setDebitAccountId(id)}
                    hierarchyMode={hierarchyDisplayMode}
                    languageMode={languageMode}
                    placeholder="-- Debit Account --"
                    isAccount
                    required
                  />
                )}
              </div>
            )}

            {/* Transfer Fee Section (Special Feature for Transfer Mode) */}
            {type === TransactionType.TRANSFER && (
              <div className="border border-blue-100 bg-blue-50/50 rounded-2xl p-3.5 space-y-3">
                <div className="flex items-center justify-between">
                  <label className="flex items-center gap-2 cursor-pointer">
                    <input
                      type="checkbox"
                      id="checkbox-transfer-fee"
                      checked={hasTransferFee}
                      onChange={(e) => {
                        setHasTransferFee(e.target.checked);
                        if (e.target.checked && !feeAccountId) {
                          setFeeAccountId(creditAccountId);
                        }
                        if (e.target.checked && !feeCategoryId) {
                          setFeeCategoryId(getRememberedFeeCategoryId(payeePayer));
                        }
                      }}
                      className="rounded border-blue-300 text-blue-600 focus:ring-blue-500"
                    />
                    <span className="text-xs font-bold text-blue-900 flex items-center gap-1.5">
                      <Receipt className="w-3.5 h-3.5 text-blue-600" />
                      Add Transfer Fee / Charge
                    </span>
                  </label>
                  {hasTransferFee && (
                    <span className="text-[11px] font-medium text-blue-600">
                      Logged as separate Expense
                    </span>
                  )}
                </div>

                {hasTransferFee && (
                  <div className="space-y-3 pt-1 animate-in fade-in duration-100">
                    {/* Fee Amount with Calculator */}
                    <div>
                      <label className="block text-xs font-semibold text-slate-700 mb-1">
                        Fee Amount *
                      </label>
                      <div className="relative flex items-center">
                        <input
                          type="number"
                          step="any"
                          id="input-tx-fee-amount"
                          value={feeAmountStr}
                          onChange={(e) => setFeeAmountStr(e.target.value)}
                          placeholder="e.g. 10 or 1.85%"
                          required={hasTransferFee}
                          className="w-full pl-3 pr-10 py-2 bg-white border border-blue-200 rounded-xl text-sm font-bold text-slate-900 focus:outline-none focus:ring-2 focus:ring-blue-500/30"
                        />
                        <button
                          type="button"
                          onClick={() => {
                            setCalcTarget('fee');
                            setShowCalc(true);
                          }}
                          className="absolute right-1.5 p-1.5 bg-blue-100 hover:bg-blue-200 text-blue-800 rounded-lg"
                          title="Calculate Fee"
                        >
                          <Calculator className="w-3.5 h-3.5" />
                        </button>
                      </div>
                    </div>

                    {/* Fee Account & Fee Category */}
                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5">
                      <HierarchyPicker
                        id="select-fee-account"
                        label="Fee Account (Default: Source)"
                        selectedId={feeAccountId || creditAccountId}
                        items={accounts}
                        allGroups={accounts}
                        onChange={(id) => setFeeAccountId(id)}
                        hierarchyMode={hierarchyDisplayMode}
                        languageMode={languageMode}
                        placeholder="-- Fee Source Account --"
                        isAccount
                      />

                      <HierarchyPicker
                        id="select-fee-category"
                        label="Fee Expense Category"
                        selectedId={feeCategoryId || getRememberedFeeCategoryId(payeePayer)}
                        items={expenseCategories}
                        allGroups={categories}
                        onChange={(id) => {
                          setFeeCategoryId(id);
                          if (payeePayer) {
                            saveFeeCategoryPreference(payeePayer, id);
                          }
                          saveFeeCategoryPreference('default', id);
                        }}
                        hierarchyMode={hierarchyDisplayMode}
                        languageMode={languageMode}
                        placeholder="-- Fee Category --"
                      />
                    </div>
                  </div>
                )}
              </div>
            )}

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
                Tags / Labels (Comma separated)
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
        initialValue={
          calcTarget === 'amount'
            ? parseFloat(amountStr) || 0
            : parseFloat(feeAmountStr) || 0
        }
        onApplyResult={(val) => {
          if (calcTarget === 'amount') {
            setAmountStr(String(val));
          } else {
            setFeeAmountStr(String(val));
          }
        }}
      />
    </>
  );
};
