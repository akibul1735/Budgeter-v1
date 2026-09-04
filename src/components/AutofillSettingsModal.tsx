import React from 'react';
import { X, Check, Zap } from 'lucide-react';
import { useBudget } from '../context/BudgetContext';
import { LanguageHelper } from '../utils/languageHelper';

interface AutofillSettingsModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export const AutofillSettingsModal: React.FC<AutofillSettingsModalProps> = ({ isOpen, onClose }) => {
  const { autofillConfig, setAutofillConfig, languageMode } = useBudget();

  if (!isOpen) return null;

  const toggleField = (field: keyof typeof autofillConfig) => {
    setAutofillConfig({
      ...autofillConfig,
      [field]: !autofillConfig[field],
    });
  };

  const fields = [
    { key: 'autofillCategory' as const, label: LanguageHelper.getString('autofill_category', languageMode) },
    { key: 'autofillAccount' as const, label: LanguageHelper.getString('autofill_account', languageMode) },
    { key: 'autofillAmount' as const, label: LanguageHelper.getString('autofill_amount', languageMode) },
    { key: 'autofillNotes' as const, label: LanguageHelper.getString('autofill_notes', languageMode) },
    { key: 'autofillLabels' as const, label: LanguageHelper.getString('autofill_labels', languageMode) },
  ];

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 backdrop-blur-xs p-4 animate-in fade-in duration-150">
      <div className="w-full max-w-sm bg-white rounded-3xl shadow-2xl border border-slate-200 overflow-hidden flex flex-col">
        {/* Header */}
        <div className="px-5 py-4 border-b border-slate-100 flex items-center justify-between bg-slate-50/70">
          <div className="flex items-center gap-2">
            <Zap className="w-4 h-4 text-amber-500" />
            <h2 className="font-bold text-slate-900 text-sm">
              {LanguageHelper.getString('autofill_settings', languageMode)}
            </h2>
          </div>
          <button onClick={onClose} className="p-1.5 rounded-xl text-slate-400 hover:text-slate-700">
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Content */}
        <div className="p-5 space-y-3">
          <p className="text-xs text-slate-500 leading-relaxed">
            {LanguageHelper.getString('autofill_desc', languageMode)}
          </p>

          <div className="space-y-2 pt-1">
            {fields.map((f) => (
              <label
                key={f.key}
                className="flex items-center justify-between p-3 rounded-xl bg-slate-50 hover:bg-slate-100/80 border border-slate-200/60 cursor-pointer text-xs font-semibold text-slate-800 transition-colors"
              >
                <span>{f.label}</span>
                <input
                  type="checkbox"
                  checked={autofillConfig[f.key]}
                  onChange={() => toggleField(f.key)}
                  className="rounded border-slate-300 text-emerald-600 focus:ring-emerald-500 w-4 h-4"
                />
              </label>
            ))}
          </div>
        </div>

        {/* Footer */}
        <div className="p-4 border-t border-slate-100 bg-slate-50 flex justify-end">
          <button
            onClick={onClose}
            className="px-6 py-2 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-xs shadow-xs"
          >
            {LanguageHelper.getString('done', languageMode)}
          </button>
        </div>
      </div>
    </div>
  );
};
