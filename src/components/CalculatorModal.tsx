import React, { useState } from 'react';
import { X, Delete, Check, RotateCcw, Copy } from 'lucide-react';
import { useBudget } from '../context/BudgetContext';
import { LanguageHelper } from '../utils/languageHelper';

interface CalculatorModalProps {
  isOpen: boolean;
  onClose: () => void;
  onApplyResult?: (value: number) => void;
  initialValue?: number;
}

export const CalculatorModal: React.FC<CalculatorModalProps> = ({
  isOpen,
  onClose,
  onApplyResult,
  initialValue = 0,
}) => {
  const { languageMode } = useBudget();
  const [expression, setExpression] = useState<string>(initialValue > 0 ? String(initialValue) : '');
  const [history, setHistory] = useState<string[]>([]);

  if (!isOpen) return null;

  const handleDigit = (digit: string) => {
    setExpression((prev) => prev + digit);
  };

  const handleOperator = (op: string) => {
    if (!expression) return;
    const lastChar = expression.slice(-1);
    if (['+', '-', '*', '/', '%'].includes(lastChar)) {
      setExpression((prev) => prev.slice(0, -1) + op);
    } else {
      setExpression((prev) => prev + op);
    }
  };

  const handleClear = () => {
    setExpression('');
  };

  const handleBackspace = () => {
    setExpression((prev) => prev.slice(0, -1));
  };

  const evaluateExpression = (): number => {
    try {
      if (!expression) return 0;
      // Sanitize expression
      const cleanExpr = expression.replace(/[^0-9+\-*/.%()]/g, '');
      // Evaluate safely
      const result = Function(`'use strict'; return (${cleanExpr})`)();
      return isFinite(result) ? Number(result.toFixed(2)) : 0;
    } catch {
      return 0;
    }
  };

  const handleEquals = () => {
    if (!expression) return;
    const res = evaluateExpression();
    setHistory((prev) => [`${expression} = ${res}`, ...prev.slice(0, 4)]);
    setExpression(String(res));
  };

  const handleDone = () => {
    const res = evaluateExpression();
    if (onApplyResult) {
      onApplyResult(res);
    }
    onClose();
  };

  const currentResult = evaluateExpression();

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 backdrop-blur-xs p-4 animate-in fade-in duration-150">
      <div className="w-full max-w-sm bg-white rounded-3xl shadow-2xl border border-slate-200 overflow-hidden flex flex-col">
        {/* Header */}
        <div className="flex items-center justify-between px-5 py-3.5 bg-slate-50 border-b border-slate-100">
          <h2 className="font-bold text-slate-800 text-sm flex items-center gap-2">
            <span>🧮</span>
            <span>{LanguageHelper.getString('quick_calc', languageMode)}</span>
          </h2>
          <button
            onClick={onClose}
            className="p-1 rounded-lg text-slate-400 hover:text-slate-700 hover:bg-slate-200/50"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Display screen */}
        <div className="p-5 bg-slate-900 text-white flex flex-col justify-end items-end min-h-[100px]">
          {history.length > 0 && (
            <div className="text-slate-400 text-xs font-mono mb-1 truncate max-w-full">
              {history[0]}
            </div>
          )}
          <div className="text-slate-300 text-sm font-mono tracking-wider overflow-x-auto max-w-full whitespace-nowrap">
            {expression || '0'}
          </div>
          <div className="text-3xl font-bold font-mono tracking-tight text-emerald-400 mt-1">
            {LanguageHelper.formatNumber(currentResult, languageMode)}
          </div>
        </div>

        {/* Keypad */}
        <div className="p-4 grid grid-cols-4 gap-2 bg-slate-50">
          <button
            onClick={handleClear}
            className="p-3.5 rounded-2xl bg-rose-100 hover:bg-rose-200 text-rose-700 font-bold text-sm active:scale-95 transition-all"
          >
            C
          </button>
          <button
            onClick={handleBackspace}
            className="p-3.5 rounded-2xl bg-slate-200 hover:bg-slate-300 text-slate-700 font-bold flex items-center justify-center active:scale-95 transition-all"
          >
            <Delete className="w-4 h-4" />
          </button>
          <button
            onClick={() => handleOperator('%')}
            className="p-3.5 rounded-2xl bg-slate-200 hover:bg-slate-300 text-slate-700 font-bold text-sm active:scale-95 transition-all"
          >
            %
          </button>
          <button
            onClick={() => handleOperator('/')}
            className="p-3.5 rounded-2xl bg-amber-500 hover:bg-amber-600 text-white font-bold text-base active:scale-95 transition-all"
          >
            ÷
          </button>

          {['7', '8', '9'].map((d) => (
            <button
              key={d}
              onClick={() => handleDigit(d)}
              className="p-3.5 rounded-2xl bg-white hover:bg-slate-100 text-slate-800 font-bold text-base shadow-2xs active:scale-95 transition-all"
            >
              {languageMode === 'BANGLA' ? LanguageHelper.toBanglaDigits(d) : d}
            </button>
          ))}
          <button
            onClick={() => handleOperator('*')}
            className="p-3.5 rounded-2xl bg-amber-500 hover:bg-amber-600 text-white font-bold text-base active:scale-95 transition-all"
          >
            ×
          </button>

          {['4', '5', '6'].map((d) => (
            <button
              key={d}
              onClick={() => handleDigit(d)}
              className="p-3.5 rounded-2xl bg-white hover:bg-slate-100 text-slate-800 font-bold text-base shadow-2xs active:scale-95 transition-all"
            >
              {languageMode === 'BANGLA' ? LanguageHelper.toBanglaDigits(d) : d}
            </button>
          ))}
          <button
            onClick={() => handleOperator('-')}
            className="p-3.5 rounded-2xl bg-amber-500 hover:bg-amber-600 text-white font-bold text-base active:scale-95 transition-all"
          >
            −
          </button>

          {['1', '2', '3'].map((d) => (
            <button
              key={d}
              onClick={() => handleDigit(d)}
              className="p-3.5 rounded-2xl bg-white hover:bg-slate-100 text-slate-800 font-bold text-base shadow-2xs active:scale-95 transition-all"
            >
              {languageMode === 'BANGLA' ? LanguageHelper.toBanglaDigits(d) : d}
            </button>
          ))}
          <button
            onClick={() => handleOperator('+')}
            className="p-3.5 rounded-2xl bg-amber-500 hover:bg-amber-600 text-white font-bold text-base active:scale-95 transition-all"
          >
            +
          </button>

          <button
            onClick={() => handleDigit('0')}
            className="p-3.5 rounded-2xl bg-white hover:bg-slate-100 text-slate-800 font-bold text-base shadow-2xs active:scale-95 transition-all"
          >
            {languageMode === 'BANGLA' ? LanguageHelper.toBanglaDigits('0') : '0'}
          </button>
          <button
            onClick={() => handleDigit('.')}
            className="p-3.5 rounded-2xl bg-white hover:bg-slate-100 text-slate-800 font-bold text-base shadow-2xs active:scale-95 transition-all"
          >
            .
          </button>
          <button
            onClick={handleEquals}
            className="p-3.5 rounded-2xl bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-base active:scale-95 transition-all"
          >
            =
          </button>
          <button
            onClick={handleDone}
            className="p-3.5 rounded-2xl bg-emerald-700 hover:bg-emerald-800 text-white font-bold text-xs flex items-center justify-center gap-1 active:scale-95 transition-all"
          >
            <Check className="w-4 h-4" />
            <span>Use</span>
          </button>
        </div>
      </div>
    </div>
  );
};
