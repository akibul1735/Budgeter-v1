import React from 'react';
import { X, Check, DollarSign, Globe } from 'lucide-react';
import { useBudget } from '../context/BudgetContext';
import { CurrencyDisplayMode, LanguageMode } from '../types';
import { POPULAR_CURRENCIES, LanguageHelper } from '../utils/languageHelper';

interface ThemeFontModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export const ThemeFontModal: React.FC<ThemeFontModalProps> = ({ isOpen, onClose }) => {
  const {
    currencyConfig,
    setCurrencyConfig,
    languageMode,
    setLanguageMode,
    isDemoMode,
    toggleDemoMode,
  } = useBudget();

  if (!isOpen) return null;

  const selectCurrency = (code: string, symbol: string) => {
    setCurrencyConfig({
      ...currencyConfig,
      selectedCode: code,
      selectedSymbol: symbol,
    });
  };

  const setDisplayMode = (mode: CurrencyDisplayMode) => {
    setCurrencyConfig({
      ...currencyConfig,
      displayMode: mode,
    });
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 backdrop-blur-xs p-4 animate-in fade-in duration-150">
      <div className="w-full max-w-md bg-white rounded-3xl shadow-2xl border border-slate-200 overflow-hidden flex flex-col max-h-[85vh]">
        {/* Header */}
        <div className="px-5 py-4 border-b border-slate-100 flex items-center justify-between bg-slate-50/70">
          <div>
            <h2 className="font-bold text-slate-900 text-base">Display & Localization</h2>
            <p className="text-xs text-slate-500">Currency, symbols, and language mode</p>
          </div>
          <button onClick={onClose} className="p-1.5 rounded-xl text-slate-400 hover:text-slate-700">
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Content */}
        <div className="p-5 overflow-y-auto space-y-4">
          {/* Language Mode */}
          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1.5">
              Language / ভাষা
            </label>
            <div className="grid grid-cols-3 gap-1.5 bg-slate-100 p-1 rounded-2xl">
              {[
                { id: LanguageMode.ENGLISH, label: 'English' },
                { id: LanguageMode.BANGLA, label: 'বাংলা' },
                { id: LanguageMode.BILINGUAL, label: 'Bilingual' },
              ].map((lang) => (
                <button
                  key={lang.id}
                  type="button"
                  onClick={() => setLanguageMode(lang.id)}
                  className={`py-2 rounded-xl text-xs font-bold transition-all ${
                    languageMode === lang.id
                      ? 'bg-emerald-600 text-white shadow-xs'
                      : 'text-slate-600 hover:text-slate-900'
                  }`}
                >
                  {lang.label}
                </button>
              ))}
            </div>
          </div>

          {/* Currency Display Mode */}
          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1.5">
              Currency Format Mode
            </label>
            <div className="grid grid-cols-3 gap-1.5 bg-slate-100 p-1 rounded-2xl text-[11px] font-bold">
              {[
                { id: CurrencyDisplayMode.SYMBOL_ONLY, label: 'Symbol (৳ / $)' },
                { id: CurrencyDisplayMode.CODE_ONLY, label: 'Code (BDT / USD)' },
                { id: CurrencyDisplayMode.CODE_AND_SYMBOL, label: 'Both' },
              ].map((m) => (
                <button
                  key={m.id}
                  type="button"
                  onClick={() => setDisplayMode(m.id)}
                  className={`py-2 px-1 rounded-xl transition-all text-center ${
                    currencyConfig.displayMode === m.id
                      ? 'bg-blue-600 text-white shadow-xs'
                      : 'text-slate-600 hover:text-slate-900'
                  }`}
                >
                  {m.label}
                </button>
              ))}
            </div>
          </div>

          {/* Currency List */}
          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1.5">
              Select Default Currency
            </label>
            <div className="grid grid-cols-2 gap-2">
              {POPULAR_CURRENCIES.map((cur) => {
                const isSelected = currencyConfig.selectedCode === cur.code;
                return (
                  <button
                    key={cur.code}
                    type="button"
                    onClick={() => selectCurrency(cur.code, cur.symbol)}
                    className={`p-2.5 rounded-xl border flex items-center justify-between text-xs transition-all ${
                      isSelected
                        ? 'border-emerald-500 bg-emerald-50/70 text-emerald-950 font-bold ring-1 ring-emerald-500'
                        : 'border-slate-200 bg-white hover:bg-slate-50 text-slate-700 font-medium'
                    }`}
                  >
                    <div className="flex items-center gap-2">
                      <span className="w-6 h-6 rounded-lg bg-slate-100 flex items-center justify-center font-bold text-slate-800">
                        {cur.symbol}
                      </span>
                      <div className="text-left">
                        <div className="leading-tight">{cur.code}</div>
                        <div className="text-[10px] text-slate-400 truncate max-w-[80px]">
                          {cur.nameEn}
                        </div>
                      </div>
                    </div>
                    {isSelected && <Check className="w-4 h-4 text-emerald-600" />}
                  </button>
                );
              })}
            </div>
          </div>

          {/* Demo Mode Toggle */}
          <div className="pt-2 border-t border-slate-100">
            <label className="flex items-center justify-between p-3 rounded-xl bg-amber-50/50 border border-amber-200/60 cursor-pointer text-xs font-semibold text-amber-900">
              <div>
                <div>Demo & Presentation Mode</div>
                <div className="text-[10px] text-amber-700/80 font-normal">
                  Shows demo watermark and sample helper flows
                </div>
              </div>
              <input
                type="checkbox"
                checked={isDemoMode}
                onChange={toggleDemoMode}
                className="rounded border-amber-300 text-amber-600 focus:ring-amber-500 w-4 h-4"
              />
            </label>
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
