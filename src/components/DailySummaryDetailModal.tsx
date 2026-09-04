import React, { useState, useMemo } from 'react';
import {
  X,
  Calendar as CalendarIcon,
  TrendingDown,
  TrendingUp,
  ChevronRight,
  ArrowUpDown,
  Filter,
} from 'lucide-react';
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
} from 'recharts';
import { useBudget } from '../context/BudgetContext';
import { TransactionType, LanguageMode } from '../types';
import { LanguageHelper } from '../utils/languageHelper';

interface DailySummaryDetailModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSelectTransaction?: (txId: number) => void;
}

export const DailySummaryDetailModal: React.FC<DailySummaryDetailModalProps> = ({
  isOpen,
  onClose,
  onSelectTransaction,
}) => {
  const {
    transactions,
    categories,
    accounts,
    languageMode,
    selectedYear,
    selectedMonth,
  } = useBudget();

  const [periodFilter, setPeriodFilter] = useState<'THIS_MONTH' | 'LAST_7_DAYS' | 'LAST_14_DAYS' | 'LAST_30_DAYS'>('THIS_MONTH');
  const [selectedDayKey, setSelectedDayKey] = useState<string | null>(null);

  // Compute daily items
  const dayItems = useMemo(() => {
    const daysInMonth = new Date(selectedYear, selectedMonth, 0).getDate();
    const result: Array<{
      dateKey: string;
      dayNum: number;
      dayLabel: string;
      fullDateStr: string;
      expense: number;
      income: number;
      transactions: typeof transactions;
    }> = [];

    let startDay = 1;
    let endDay = daysInMonth;

    if (periodFilter === 'LAST_7_DAYS') {
      const now = new Date();
      startDay = Math.max(1, now.getDate() - 6);
      endDay = now.getDate();
    } else if (periodFilter === 'LAST_14_DAYS') {
      const now = new Date();
      startDay = Math.max(1, now.getDate() - 13);
      endDay = now.getDate();
    }

    for (let d = startDay; d <= endDay; d++) {
      const dateObj = new Date(selectedYear, selectedMonth - 1, d);
      const dayLabel = dateObj.toLocaleDateString('en-US', { weekday: 'short' });
      const fullDateStr = dateObj.toLocaleDateString('en-US', {
        weekday: 'short',
        month: 'short',
        day: 'numeric',
        year: 'numeric',
      });

      const dayTxs = transactions.filter((t) => {
        const txDate = new Date(t.dateEpochMs);
        return (
          txDate.getFullYear() === selectedYear &&
          txDate.getMonth() + 1 === selectedMonth &&
          txDate.getDate() === d &&
          t.status !== 'VOID'
        );
      });

      const expense = dayTxs
        .filter((t) => t.type === TransactionType.EXPENSE)
        .reduce((sum, t) => sum + t.amount, 0);

      const income = dayTxs
        .filter((t) => t.type === TransactionType.INCOME)
        .reduce((sum, t) => sum + t.amount, 0);

      result.push({
        dateKey: `${selectedYear}-${selectedMonth}-${d}`,
        dayNum: d,
        dayLabel,
        fullDateStr,
        expense,
        income,
        transactions: dayTxs,
      });
    }

    return result;
  }, [transactions, periodFilter, selectedYear, selectedMonth]);

  const totalExpense = useMemo(
    () => dayItems.reduce((sum, d) => sum + d.expense, 0),
    [dayItems]
  );
  const totalIncome = useMemo(
    () => dayItems.reduce((sum, d) => sum + d.income, 0),
    [dayItems]
  );
  const dailyAvgExpense = totalExpense / Math.max(1, dayItems.length);

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-3 sm:p-4 bg-slate-900/60 backdrop-blur-xs animate-in fade-in duration-200">
      <div className="bg-white w-full max-w-3xl rounded-3xl shadow-2xl border border-slate-100 flex flex-col max-h-[90vh] overflow-hidden">
        {/* Header */}
        <div className="px-5 py-4 border-b border-slate-100 flex items-center justify-between bg-slate-50/50">
          <div className="flex items-center gap-2.5">
            <div className="w-9 h-9 rounded-2xl bg-sky-100 text-sky-700 flex items-center justify-center font-bold">
              <CalendarIcon className="w-5 h-5" />
            </div>
            <div>
              <h2 className="font-extrabold text-slate-900 text-base sm:text-lg">
                {LanguageHelper.getString('daily_summary', languageMode)} Breakdown
              </h2>
              <p className="text-xs text-slate-500">
                Day-by-day cash flow & transaction timeline
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-2 text-slate-400 hover:text-slate-700 hover:bg-slate-100 rounded-full transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Filter Toolbar */}
        <div className="px-5 py-3 border-b border-slate-100 bg-white flex items-center justify-between">
          <span className="text-xs font-bold text-slate-400 uppercase tracking-wider">
            Time Range
          </span>
          <select
            value={periodFilter}
            onChange={(e) => setPeriodFilter(e.target.value as any)}
            className="text-xs font-semibold bg-slate-50 border border-slate-200 rounded-xl px-3 py-1.5 focus:outline-none focus:ring-2 focus:ring-sky-500/20 text-slate-700"
          >
            <option value="THIS_MONTH">This Month</option>
            <option value="LAST_7_DAYS">Last 7 Days</option>
            <option value="LAST_14_DAYS">Last 14 Days</option>
          </select>
        </div>

        {/* Body */}
        <div className="flex-1 overflow-y-auto p-5 space-y-5">
          {/* Summary Cards */}
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
            <div className="p-3.5 rounded-2xl bg-rose-50/80 border border-rose-100">
              <span className="text-[10px] font-bold text-rose-700 uppercase tracking-wider">
                Total Expenses
              </span>
              <div className="text-xl font-black text-rose-950 font-mono mt-0.5">
                {LanguageHelper.formatCurrency(totalExpense, languageMode)}
              </div>
            </div>

            <div className="p-3.5 rounded-2xl bg-emerald-50/80 border border-emerald-100">
              <span className="text-[10px] font-bold text-emerald-700 uppercase tracking-wider">
                Total Income
              </span>
              <div className="text-xl font-black text-emerald-950 font-mono mt-0.5">
                {LanguageHelper.formatCurrency(totalIncome, languageMode)}
              </div>
            </div>

            <div className="p-3.5 rounded-2xl bg-slate-50 border border-slate-200/80">
              <span className="text-[10px] font-bold text-slate-500 uppercase tracking-wider">
                Daily Avg Expense
              </span>
              <div className="text-xl font-black text-slate-900 font-mono mt-0.5">
                {LanguageHelper.formatCurrency(dailyAvgExpense, languageMode)}
              </div>
            </div>
          </div>

          {/* Bar Chart View */}
          <div className="p-4 bg-slate-50/70 rounded-2xl border border-slate-200/70">
            <h4 className="text-xs font-bold text-slate-700 mb-3">Daily Expense & Inflow Trend</h4>
            <div className="h-44 w-full">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={dayItems} margin={{ top: 5, right: 5, left: -20, bottom: 0 }}>
                  <XAxis dataKey="dayNum" tick={{ fontSize: 10 }} stroke="#94A3B8" />
                  <YAxis tick={{ fontSize: 10 }} stroke="#94A3B8" />
                  <Tooltip
                    formatter={(val: number) =>
                      LanguageHelper.formatCurrency(val, languageMode)
                    }
                    contentStyle={{
                      borderRadius: '12px',
                      border: '1px solid #E2E8F0',
                      fontSize: '12px',
                    }}
                  />
                  <Bar dataKey="income" fill="#10B981" radius={[3, 3, 0, 0]} name="Income" />
                  <Bar dataKey="expense" fill="#F43F5E" radius={[3, 3, 0, 0]} name="Expense" />
                </BarChart>
              </ResponsiveContainer>
            </div>
          </div>

          {/* Chronological List of Days */}
          <div className="space-y-2">
            <h4 className="text-xs font-bold text-slate-700">Days Timeline</h4>
            <div className="space-y-2 max-h-72 overflow-y-auto pr-1">
              {[...dayItems].reverse().map((day) => {
                const isSelected = selectedDayKey === day.dateKey;
                return (
                  <div
                    key={day.dateKey}
                    className={`border rounded-2xl transition-all overflow-hidden ${
                      isSelected
                        ? 'border-sky-300 bg-sky-50/40'
                        : 'border-slate-200/70 bg-white hover:bg-slate-50/60'
                    }`}
                  >
                    <div
                      onClick={() =>
                        setSelectedDayKey(isSelected ? null : day.dateKey)
                      }
                      className="p-3 flex items-center justify-between cursor-pointer"
                    >
                      <div className="flex items-center gap-3">
                        <div className="w-8 h-8 rounded-xl bg-slate-100 font-bold text-slate-700 flex items-center justify-center text-xs">
                          {day.dayNum}
                        </div>
                        <div>
                          <p className="text-xs font-bold text-slate-900">
                            {day.fullDateStr}
                          </p>
                          <p className="text-[10px] text-slate-400">
                            {day.transactions.length} transactions
                          </p>
                        </div>
                      </div>

                      <div className="text-right flex items-center gap-3">
                        <div>
                          {day.expense > 0 && (
                            <p className="text-xs font-extrabold text-rose-600 font-mono">
                              -{LanguageHelper.formatCurrency(day.expense, languageMode)}
                            </p>
                          )}
                          {day.income > 0 && (
                            <p className="text-xs font-bold text-emerald-600 font-mono">
                              +{LanguageHelper.formatCurrency(day.income, languageMode)}
                            </p>
                          )}
                          {day.expense === 0 && day.income === 0 && (
                            <p className="text-xs text-slate-400">-</p>
                          )}
                        </div>
                        <ChevronRight
                          className={`w-4 h-4 text-slate-400 transition-transform ${
                            isSelected ? 'rotate-90' : ''
                          }`}
                        />
                      </div>
                    </div>

                    {/* Expandable transactions */}
                    {isSelected && day.transactions.length > 0 && (
                      <div className="border-t border-slate-200/60 bg-white p-3 space-y-1.5">
                        {day.transactions.map((tx) => {
                          const cat = categories.find((c) => c.id === tx.categoryId);
                          return (
                            <div
                              key={tx.id}
                              onClick={() => onSelectTransaction?.(tx.id)}
                              className="p-2 rounded-xl hover:bg-slate-50 flex items-center justify-between cursor-pointer text-xs"
                            >
                              <div className="flex items-center gap-2">
                                <span
                                  className={`w-2 h-2 rounded-full ${
                                    tx.type === TransactionType.EXPENSE
                                      ? 'bg-rose-500'
                                      : 'bg-emerald-500'
                                  }`}
                                />
                                <span className="font-semibold text-slate-800">
                                  {cat
                                    ? LanguageHelper.getLocalizedName(
                                        cat.nameEn,
                                        cat.nameBn,
                                        languageMode
                                      )
                                    : tx.description || 'Transaction'}
                                </span>
                              </div>
                              <span
                                className={`font-mono font-bold ${
                                  tx.type === TransactionType.EXPENSE
                                    ? 'text-rose-600'
                                    : 'text-emerald-600'
                                }`}
                              >
                                {LanguageHelper.formatCurrency(tx.amount, languageMode)}
                              </span>
                            </div>
                          );
                        })}
                      </div>
                    )}
                  </div>
                );
              })}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
