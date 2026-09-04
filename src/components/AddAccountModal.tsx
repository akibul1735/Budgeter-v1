import React, { useState, useEffect } from 'react';
import { X, Building2, Check, DollarSign } from 'lucide-react';
import { useBudget } from '../context/BudgetContext';
import { Account, AccountType, LanguageMode } from '../types';
import { LanguageHelper } from '../utils/languageHelper';

interface AddAccountModalProps {
  isOpen: boolean;
  onClose: () => void;
  editingAccount?: Account | null;
  defaultParentId?: number | null;
}

export const AddAccountModal: React.FC<AddAccountModalProps> = ({
  isOpen,
  onClose,
  editingAccount,
  defaultParentId,
}) => {
  const { accounts, addAccount, updateAccount, deleteAccount, languageMode } = useBudget();

  const [nameEn, setNameEn] = useState<string>('');
  const [nameBn, setNameBn] = useState<string>('');
  const [type, setType] = useState<AccountType>(AccountType.ASSET);
  const [initialBalanceStr, setInitialBalanceStr] = useState<string>('0');
  const [colorHex, setColorHex] = useState<string>('#3B82F6');
  const [iconName, setIconName] = useState<string>('Building2');
  const [parentId, setParentId] = useState<number | null>(null);

  useEffect(() => {
    if (editingAccount) {
      setNameEn(editingAccount.nameEn);
      setNameBn(editingAccount.nameBn || '');
      setType(editingAccount.type);
      setInitialBalanceStr(String(editingAccount.initialBalance));
      setColorHex(editingAccount.colorHex || '#3B82F6');
      setIconName(editingAccount.iconName || 'Building2');
      setParentId(editingAccount.parentId);
    } else {
      setNameEn('');
      setNameBn('');
      setType(AccountType.ASSET);
      setInitialBalanceStr('0');
      setColorHex('#3B82F6');
      setIconName('Building2');
      setParentId(defaultParentId || null);
    }
  }, [editingAccount, defaultParentId, isOpen]);

  if (!isOpen) return null;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!nameEn.trim()) {
      alert('Please enter an account name in English');
      return;
    }

    const initialBalance = parseFloat(initialBalanceStr) || 0;

    if (editingAccount) {
      updateAccount({
        ...editingAccount,
        nameEn: nameEn.trim(),
        nameBn: nameBn.trim(),
        type,
        initialBalance,
        colorHex,
        iconName,
        parentId,
      });
    } else {
      addAccount({
        nameEn: nameEn.trim(),
        nameBn: nameBn.trim(),
        type,
        initialBalance,
        colorHex,
        iconName,
        isArchived: false,
        sortOrder: accounts.length + 1,
        parentId,
        isCalculated: true,
        calculationAdjustment: 0,
      });
    }
    onClose();
  };

  const handleDelete = () => {
    if (editingAccount && confirm(`Are you sure you want to delete "${editingAccount.nameEn}"?`)) {
      deleteAccount(editingAccount.id);
      onClose();
    }
  };

  const colorPalette = [
    '#3B82F6',
    '#10B981',
    '#F59E0B',
    '#EF4444',
    '#8B5CF6',
    '#EC4899',
    '#06B6D4',
    '#64748B',
  ];

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 backdrop-blur-xs p-4 animate-in fade-in duration-150">
      <div className="w-full max-w-md bg-white rounded-3xl shadow-2xl border border-slate-200 overflow-hidden flex flex-col">
        {/* Header */}
        <div className="px-5 py-4 border-b border-slate-100 flex items-center justify-between bg-slate-50/70">
          <h2 className="font-bold text-slate-900 text-base">
            {editingAccount
              ? LanguageHelper.getString('edit', languageMode) + ' ' + LanguageHelper.getString('accounts', languageMode)
              : LanguageHelper.getString('add_account', languageMode)}
          </h2>
          <button onClick={onClose} className="p-1.5 rounded-xl text-slate-400 hover:text-slate-700">
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit} className="p-5 space-y-4">
          {/* Account Type Selector */}
          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              Account Type *
            </label>
            <div className="grid grid-cols-3 gap-2 bg-slate-100 p-1 rounded-2xl">
              <button
                type="button"
                id="btn-acc-type-asset"
                onClick={() => setType(AccountType.ASSET)}
                className={`py-2 rounded-xl text-xs font-bold transition-all ${
                  type === AccountType.ASSET
                    ? 'bg-emerald-600 text-white shadow-xs'
                    : 'text-slate-600 hover:text-slate-900'
                }`}
              >
                {LanguageHelper.getString('assets', languageMode)}
              </button>

              <button
                type="button"
                id="btn-acc-type-liability"
                onClick={() => setType(AccountType.LIABILITY)}
                className={`py-2 rounded-xl text-xs font-bold transition-all ${
                  type === AccountType.LIABILITY
                    ? 'bg-rose-500 text-white shadow-xs'
                    : 'text-slate-600 hover:text-slate-900'
                }`}
              >
                {LanguageHelper.getString('liabilities', languageMode)}
              </button>

              <button
                type="button"
                id="btn-acc-type-equity"
                onClick={() => setType(AccountType.EQUITY)}
                className={`py-2 rounded-xl text-xs font-bold transition-all ${
                  type === AccountType.EQUITY
                    ? 'bg-purple-600 text-white shadow-xs'
                    : 'text-slate-600 hover:text-slate-900'
                }`}
              >
                {LanguageHelper.getString('equity', languageMode)}
              </button>
            </div>
          </div>

          {/* Names */}
          <div className="space-y-2">
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">
                {LanguageHelper.getString('name_en', languageMode)} *
              </label>
              <input
                type="text"
                id="input-acc-name-en"
                value={nameEn}
                onChange={(e) => setNameEn(e.target.value)}
                placeholder="e.g. Dutch Bangla Bank, Cash Wallet"
                required
                className="w-full px-3.5 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm text-slate-900 focus:bg-white focus:outline-none focus:ring-2 focus:ring-emerald-500/30"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">
                {LanguageHelper.getString('name_bn', languageMode)}
              </label>
              <input
                type="text"
                id="input-acc-name-bn"
                value={nameBn}
                onChange={(e) => setNameBn(e.target.value)}
                placeholder="যেমনঃ ডাচ বাংলা ব্যাংক, নগদ ক্যাশ"
                className="w-full px-3.5 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm text-slate-900 focus:bg-white focus:outline-none focus:ring-2 focus:ring-emerald-500/30"
              />
            </div>
          </div>

          {/* Initial Balance */}
          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              {LanguageHelper.getString('initial_balance', languageMode)}
            </label>
            <input
              type="number"
              step="any"
              id="input-acc-initial-balance"
              value={initialBalanceStr}
              onChange={(e) => setInitialBalanceStr(e.target.value)}
              className="w-full px-3.5 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm font-bold text-slate-900 focus:bg-white focus:outline-none"
            />
          </div>

          {/* Color Palette */}
          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1.5">
              Account Accent Color
            </label>
            <div className="flex items-center gap-2">
              {colorPalette.map((color) => (
                <button
                  key={color}
                  type="button"
                  onClick={() => setColorHex(color)}
                  className={`w-7 h-7 rounded-full transition-transform ${
                    colorHex === color ? 'scale-125 ring-2 ring-slate-900 ring-offset-2' : 'hover:scale-110'
                  }`}
                  style={{ backgroundColor: color }}
                />
              ))}
            </div>
          </div>

          {/* Action Buttons */}
          <div className="pt-3 flex items-center justify-between border-t border-slate-100">
            {editingAccount ? (
              <button
                type="button"
                onClick={handleDelete}
                className="px-4 py-2 rounded-xl bg-rose-50 hover:bg-rose-100 text-rose-700 font-bold text-xs"
              >
                {LanguageHelper.getString('delete', languageMode)}
              </button>
            ) : (
              <button
                type="button"
                onClick={onClose}
                className="px-4 py-2 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-700 font-bold text-xs"
              >
                {LanguageHelper.getString('cancel', languageMode)}
              </button>
            )}

            <button
              type="submit"
              id="btn-save-account"
              className="px-6 py-2 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-xs flex items-center gap-1 shadow-sm active:scale-95 transition-all"
            >
              <Check className="w-4 h-4" />
              <span>{LanguageHelper.getString('save', languageMode)}</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
