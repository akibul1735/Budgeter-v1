import React, { useState, useEffect } from 'react';
import { X, FolderTree, Check } from 'lucide-react';
import { useBudget } from '../context/BudgetContext';
import { Category, CategoryType, LanguageMode, AccountType } from '../types';
import { LanguageHelper } from '../utils/languageHelper';

interface AddCategoryModalProps {
  isOpen: boolean;
  onClose: () => void;
  editingCategory?: Category | null;
  defaultType?: CategoryType;
}

export const AddCategoryModal: React.FC<AddCategoryModalProps> = ({
  isOpen,
  onClose,
  editingCategory,
  defaultType = CategoryType.EXPENSE,
}) => {
  const { accounts, categories, addCategory, updateCategory, deleteCategory, languageMode } = useBudget();

  const [nameEn, setNameEn] = useState<string>('');
  const [nameBn, setNameBn] = useState<string>('');
  const [type, setType] = useState<CategoryType>(defaultType);
  const [monthlyBudgetStr, setMonthlyBudgetStr] = useState<string>('0');
  const [colorHex, setColorHex] = useState<string>('#F59E0B');
  const [iconName, setIconName] = useState<string>('FolderTree');
  const [defaultAccountId, setDefaultAccountId] = useState<number | null>(null);
  const [parentId, setParentId] = useState<number | null>(null);

  useEffect(() => {
    if (editingCategory) {
      setNameEn(editingCategory.nameEn);
      setNameBn(editingCategory.nameBn || '');
      setType(editingCategory.type);
      setMonthlyBudgetStr(String(editingCategory.monthlyBudget || 0));
      setColorHex(editingCategory.colorHex || '#F59E0B');
      setIconName(editingCategory.iconName || 'FolderTree');
      setDefaultAccountId(editingCategory.defaultAccountId);
      setParentId(editingCategory.parentId || null);
    } else {
      setNameEn('');
      setNameBn('');
      setType(defaultType);
      setMonthlyBudgetStr('0');
      setColorHex('#F59E0B');
      setIconName('FolderTree');
      setParentId(null);
      const assetAccounts = accounts.filter((a) => a.type === AccountType.ASSET);
      setDefaultAccountId(assetAccounts[0]?.id || null);
    }
  }, [editingCategory, defaultType, accounts, isOpen]);

  if (!isOpen) return null;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!nameEn.trim()) {
      alert('Please enter a category name in English');
      return;
    }

    const budget = parseFloat(monthlyBudgetStr) || 0;

    if (editingCategory) {
      updateCategory({
        ...editingCategory,
        nameEn: nameEn.trim(),
        nameBn: nameBn.trim(),
        type,
        monthlyBudget: budget,
        colorHex,
        iconName,
        defaultAccountId,
        parentId,
      });
    } else {
      addCategory({
        nameEn: nameEn.trim(),
        nameBn: nameBn.trim(),
        type,
        iconName,
        colorHex,
        monthlyBudget: budget,
        sortOrder: categories.length + 1,
        parentId,
        isTaxDeductible: false,
        defaultAccountId,
        isCalculated: true,
      });
    }
    onClose();
  };

  const handleDelete = () => {
    if (editingCategory && confirm(`Are you sure you want to delete "${editingCategory.nameEn}"?`)) {
      deleteCategory(editingCategory.id);
      onClose();
    }
  };

  const colorPalette = [
    '#F59E0B',
    '#3B82F6',
    '#10B981',
    '#6366F1',
    '#EC4899',
    '#EF4444',
    '#8B5CF6',
    '#14B8A6',
    '#84CC16',
    '#06B6D4',
  ];

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 backdrop-blur-xs p-4 animate-in fade-in duration-150">
      <div className="w-full max-w-md bg-white rounded-3xl shadow-2xl border border-slate-200 overflow-hidden flex flex-col">
        {/* Header */}
        <div className="px-5 py-4 border-b border-slate-100 flex items-center justify-between bg-slate-50/70">
          <h2 className="font-bold text-slate-900 text-base">
            {editingCategory
              ? LanguageHelper.getString('edit', languageMode) + ' ' + LanguageHelper.getString('categories', languageMode)
              : LanguageHelper.getString('add_category', languageMode)}
          </h2>
          <button onClick={onClose} className="p-1.5 rounded-xl text-slate-400 hover:text-slate-700">
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit} className="p-5 space-y-4">
          {/* Category Type */}
          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              Category Type *
            </label>
            <div className="grid grid-cols-2 gap-2 bg-slate-100 p-1 rounded-2xl">
              <button
                type="button"
                id="btn-cat-type-expense"
                onClick={() => setType(CategoryType.EXPENSE)}
                className={`py-2 rounded-xl text-xs font-bold transition-all ${
                  type === CategoryType.EXPENSE
                    ? 'bg-rose-500 text-white shadow-xs'
                    : 'text-slate-600 hover:text-slate-900'
                }`}
              >
                {LanguageHelper.getString('expense', languageMode)}
              </button>

              <button
                type="button"
                id="btn-cat-type-income"
                onClick={() => setType(CategoryType.INCOME)}
                className={`py-2 rounded-xl text-xs font-bold transition-all ${
                  type === CategoryType.INCOME
                    ? 'bg-emerald-600 text-white shadow-xs'
                    : 'text-slate-600 hover:text-slate-900'
                }`}
              >
                {LanguageHelper.getString('income', languageMode)}
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
                id="input-cat-name-en"
                value={nameEn}
                onChange={(e) => setNameEn(e.target.value)}
                placeholder="e.g. Groceries, Entertainment, Salary"
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
                id="input-cat-name-bn"
                value={nameBn}
                onChange={(e) => setNameBn(e.target.value)}
                placeholder="যেমনঃ খাবার বাজার, বিনোদন, বেতন"
                className="w-full px-3.5 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm text-slate-900 focus:bg-white focus:outline-none focus:ring-2 focus:ring-emerald-500/30"
              />
            </div>
          </div>

          {/* Monthly Budget */}
          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              {LanguageHelper.getString('budget_limit', languageMode)}
            </label>
            <input
              type="number"
              step="any"
              id="input-cat-budget"
              value={monthlyBudgetStr}
              onChange={(e) => setMonthlyBudgetStr(e.target.value)}
              className="w-full px-3.5 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm font-bold text-slate-900 focus:bg-white focus:outline-none"
            />
          </div>

          {/* Default Assigned Account / Payment Source */}
          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              Default Payment Source (Account)
            </label>
            <select
              id="select-cat-default-account"
              value={defaultAccountId || ''}
              onChange={(e) => setDefaultAccountId(Number(e.target.value) || null)}
              className="w-full px-3.5 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm text-slate-900 focus:bg-white focus:outline-none"
            >
              <option value="">-- None / General --</option>
              {accounts.map((a) => (
                <option key={a.id} value={a.id}>
                  {LanguageHelper.getLocalizedName(a.nameEn, a.nameBn, languageMode)}
                </option>
              ))}
            </select>
          </div>

          {/* Color Palette */}
          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1.5">
              Category Color
            </label>
            <div className="flex items-center gap-2 flex-wrap">
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
            {editingCategory ? (
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
              id="btn-save-category"
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
