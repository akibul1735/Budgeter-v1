import React, { useState, useMemo } from 'react';
import {
  X,
  PieChart as PieIcon,
  Table as TableIcon,
  ChevronRight,
  Filter,
  ArrowUpDown,
  Calendar,
  Layers,
  Sparkles,
  TrendingDown,
  TrendingUp,
} from 'lucide-react';
import {
  PieChart,
  Pie,
  Cell,
  Tooltip,
  ResponsiveContainer,
  Sector,
} from 'recharts';
import { useBudget } from '../context/BudgetContext';
import { TransactionType, LanguageMode } from '../types';
import { LanguageHelper } from '../utils/languageHelper';

interface BudgetSummaryPreviewModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSelectTransaction?: (txId: number) => void;
}

const PALETTE = [
  '#F43F5E',
  '#0EA5E9',
  '#10B981',
  '#F59E0B',
  '#8B5CF6',
  '#EC4899',
  '#06B6D4',
  '#84CC16',
  '#6366F1',
  '#D97706',
  '#14B8A6',
  '#64748B',
];

export const BudgetSummaryPreviewModal: React.FC<BudgetSummaryPreviewModalProps> = ({
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

  const [filterType, setFilterType] = useState<TransactionType>(TransactionType.EXPENSE);
  const [dateRange, setDateRange] = useState<'THIS_MONTH' | 'LAST_MONTH' | 'LAST_30_DAYS' | 'ALL_TIME'>('THIS_MONTH');
  const [groupBy, setGroupBy] = useState<'CATEGORY' | 'PARENT' | 'ACCOUNT'>('CATEGORY');
  const [viewMode, setViewMode] = useState<'CHART' | 'TABLE'>('CHART');
  const [selectedSliceIndex, setSelectedSliceIndex] = useState<number | null>(null);

  // Filter transactions
  const filteredTxs = useMemo(() => {
    const now = new Date();
    return transactions.filter((tx) => {
      if (tx.status === 'VOID') return false;
      if (tx.type !== filterType) return false;

      const txDate = new Date(tx.dateEpochMs);
      if (dateRange === 'THIS_MONTH') {
        return (
          txDate.getFullYear() === selectedYear &&
          txDate.getMonth() + 1 === selectedMonth
        );
      } else if (dateRange === 'LAST_MONTH') {
        const lastMonthDate = new Date(selectedYear, selectedMonth - 2, 1);
        return (
          txDate.getFullYear() === lastMonthDate.getFullYear() &&
          txDate.getMonth() === lastMonthDate.getMonth()
        );
      } else if (dateRange === 'LAST_30_DAYS') {
        const thirtyDaysAgo = new Date();
        thirtyDaysAgo.setDate(thirtyDaysAgo.getDate() - 30);
        return txDate >= thirtyDaysAgo;
      }
      return true;
    });
  }, [transactions, filterType, dateRange, selectedYear, selectedMonth]);

  // Aggregate by chosen grouping
  const summaryItems = useMemo(() => {
    const totalAmount = filteredTxs.reduce((sum, tx) => sum + tx.amount, 0);

    if (groupBy === 'ACCOUNT') {
      const map = new Map<number, { name: string; amount: number; count: number }>();
      filteredTxs.forEach((tx) => {
        const accId = tx.type === TransactionType.INCOME ? tx.creditAccountId : tx.debitAccountId;
        const acc = accounts.find((a) => a.id === accId);
        const name = acc
          ? LanguageHelper.getLocalizedName(acc.nameEn, acc.nameBn, languageMode)
          : 'Other Account';
        const curr = map.get(accId || 0) || { name, amount: 0, count: 0 };
        curr.amount += tx.amount;
        curr.count += 1;
        map.set(accId || 0, curr);
      });

      return Array.from(map.entries())
        .map(([id, val], index) => ({
          id,
          name: val.name,
          amount: val.amount,
          count: val.count,
          percentage: totalAmount > 0 ? (val.amount / totalAmount) * 100 : 0,
          color: PALETTE[index % PALETTE.length],
        }))
        .sort((a, b) => b.amount - a.amount);
    }

    // Category / Parent grouping
    const map = new Map<number, { name: string; amount: number; count: number; color?: string }>();
    filteredTxs.forEach((tx) => {
      const cat = categories.find((c) => c.id === tx.categoryId);
      const catId = cat ? cat.id : 0;
      const catName = cat
        ? LanguageHelper.getLocalizedName(cat.nameEn, cat.nameBn, languageMode)
        : 'Uncategorized';
      const curr = map.get(catId) || {
        name: catName,
        amount: 0,
        count: 0,
        color: cat?.colorHex,
      };
      curr.amount += tx.amount;
      curr.count += 1;
      map.set(catId, curr);
    });

    return Array.from(map.entries())
      .map(([id, val], index) => ({
        id,
        name: val.name,
        amount: val.amount,
        count: val.count,
        percentage: totalAmount > 0 ? (val.amount / totalAmount) * 100 : 0,
        color: val.color || PALETTE[index % PALETTE.length],
      }))
      .sort((a, b) => b.amount - a.amount);
  }, [filteredTxs, groupBy, accounts, categories, languageMode]);

  const totalSum = useMemo(
    () => summaryItems.reduce((acc, item) => acc + item.amount, 0),
    [summaryItems]
  );

  const selectedItem = selectedSliceIndex !== null && summaryItems[selectedSliceIndex]
    ? summaryItems[selectedSliceIndex]
    : null;

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-3 sm:p-4 bg-slate-900/60 backdrop-blur-xs animate-in fade-in duration-200">
      <div className="bg-white w-full max-w-3xl rounded-3xl shadow-2xl border border-slate-100 flex flex-col max-h-[90vh] overflow-hidden">
        {/* Header */}
        <div className="px-5 py-4 border-b border-slate-100 flex items-center justify-between bg-slate-50/50">
          <div className="flex items-center gap-2.5">
            <div className="w-9 h-9 rounded-2xl bg-emerald-100 text-emerald-700 flex items-center justify-center font-bold">
              <PieIcon className="w-5 h-5" />
            </div>
            <div>
              <h2 className="font-extrabold text-slate-900 text-base sm:text-lg">
                {LanguageHelper.getString('budget_summary', languageMode)} Preview
              </h2>
              <p className="text-xs text-slate-500">
                Detailed category spending & breakdown insights
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

        {/* Filter Controls Bar */}
        <div className="px-5 py-3 border-b border-slate-100 bg-white grid grid-cols-2 sm:grid-cols-4 gap-2.5">
          {/* Type Filter */}
          <div>
            <label className="text-[10px] font-bold text-slate-400 uppercase tracking-wider block mb-1">
              Type
            </label>
            <select
              value={filterType}
              onChange={(e) => setFilterType(e.target.value as TransactionType)}
              className="w-full text-xs font-semibold bg-slate-50 border border-slate-200 rounded-xl px-2.5 py-1.5 focus:outline-none focus:ring-2 focus:ring-emerald-500/20 text-slate-700"
            >
              <option value={TransactionType.EXPENSE}>Expenses</option>
              <option value={TransactionType.INCOME}>Income</option>
            </select>
          </div>

          {/* Date Range */}
          <div>
            <label className="text-[10px] font-bold text-slate-400 uppercase tracking-wider block mb-1">
              Period
            </label>
            <select
              value={dateRange}
              onChange={(e) => setDateRange(e.target.value as any)}
              className="w-full text-xs font-semibold bg-slate-50 border border-slate-200 rounded-xl px-2.5 py-1.5 focus:outline-none focus:ring-2 focus:ring-emerald-500/20 text-slate-700"
            >
              <option value="THIS_MONTH">This Month</option>
              <option value="LAST_MONTH">Last Month</option>
              <option value="LAST_30_DAYS">Last 30 Days</option>
              <option value="ALL_TIME">All Time</option>
            </select>
          </div>

          {/* Grouping */}
          <div>
            <label className="text-[10px] font-bold text-slate-400 uppercase tracking-wider block mb-1">
              Group By
            </label>
            <select
              value={groupBy}
              onChange={(e) => setGroupBy(e.target.value as any)}
              className="w-full text-xs font-semibold bg-slate-50 border border-slate-200 rounded-xl px-2.5 py-1.5 focus:outline-none focus:ring-2 focus:ring-emerald-500/20 text-slate-700"
            >
              <option value="CATEGORY">Category</option>
              <option value="ACCOUNT">Account</option>
            </select>
          </div>

          {/* View Mode Toggle */}
          <div>
            <label className="text-[10px] font-bold text-slate-400 uppercase tracking-wider block mb-1">
              View
            </label>
            <div className="flex bg-slate-100 p-0.5 rounded-xl">
              <button
                onClick={() => setViewMode('CHART')}
                className={`flex-1 py-1 text-xs font-bold rounded-lg transition-all flex items-center justify-center gap-1 ${
                  viewMode === 'CHART'
                    ? 'bg-white text-emerald-600 shadow-xs'
                    : 'text-slate-500 hover:text-slate-800'
                }`}
              >
                <PieIcon className="w-3.5 h-3.5" />
                <span>Chart</span>
              </button>
              <button
                onClick={() => setViewMode('TABLE')}
                className={`flex-1 py-1 text-xs font-bold rounded-lg transition-all flex items-center justify-center gap-1 ${
                  viewMode === 'TABLE'
                    ? 'bg-white text-emerald-600 shadow-xs'
                    : 'text-slate-500 hover:text-slate-800'
                }`}
              >
                <TableIcon className="w-3.5 h-3.5" />
                <span>Table</span>
              </button>
            </div>
          </div>
        </div>

        {/* Content Body */}
        <div className="flex-1 overflow-y-auto p-5 space-y-5">
          {/* Total Banner */}
          <div className="p-4 rounded-2xl bg-gradient-to-r from-emerald-50 to-teal-50 border border-emerald-100/80 flex items-center justify-between">
            <div>
              <span className="text-xs font-bold text-emerald-800 uppercase tracking-wider">
                Total {filterType === TransactionType.EXPENSE ? 'Expenditure' : 'Inflow'}
              </span>
              <div className="text-2xl font-black text-slate-900 font-mono mt-0.5">
                {LanguageHelper.formatCurrency(totalSum, languageMode)}
              </div>
            </div>
            <div className="text-right">
              <span className="text-xs text-slate-500 font-medium">
                {summaryItems.length} categories • {filteredTxs.length} transactions
              </span>
            </div>
          </div>

          {summaryItems.length === 0 ? (
            <div className="py-16 text-center text-slate-400 text-sm">
              No transactions recorded for the selected filter and period.
            </div>
          ) : viewMode === 'CHART' ? (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6 items-center">
              {/* Donut Chart */}
              <div className="h-64 relative flex items-center justify-center">
                <ResponsiveContainer width="100%" height="100%">
                  <PieChart>
                    <Pie
                      data={summaryItems}
                      cx="50%"
                      cy="50%"
                      innerRadius={60}
                      outerRadius={85}
                      paddingAngle={3}
                      dataKey="amount"
                      onClick={(_, index) =>
                        setSelectedSliceIndex(selectedSliceIndex === index ? null : index)
                      }
                    >
                      {summaryItems.map((entry, index) => (
                        <Cell
                          key={`cell-${index}`}
                          fill={entry.color}
                          stroke={selectedSliceIndex === index ? '#000' : '#fff'}
                          strokeWidth={selectedSliceIndex === index ? 2 : 1}
                        />
                      ))}
                    </Pie>
                    <Tooltip
                      formatter={(val: number) =>
                        LanguageHelper.formatCurrency(val, languageMode)
                      }
                    />
                  </PieChart>
                </ResponsiveContainer>
                {/* Center text */}
                <div className="absolute inset-0 flex flex-col items-center justify-center pointer-events-none">
                  <span className="text-[10px] uppercase font-bold text-slate-400">
                    {selectedItem ? selectedItem.name : 'TOTAL'}
                  </span>
                  <span className="text-sm font-black text-slate-800 font-mono">
                    {selectedItem
                      ? `${selectedItem.percentage.toFixed(1)}%`
                      : LanguageHelper.formatCurrency(totalSum, languageMode)}
                  </span>
                </div>
              </div>

              {/* Breakdown List */}
              <div className="space-y-2 max-h-64 overflow-y-auto pr-1">
                {summaryItems.map((item, index) => {
                  const isSelected = selectedSliceIndex === index;
                  return (
                    <div
                      key={item.id}
                      onClick={() =>
                        setSelectedSliceIndex(isSelected ? null : index)
                      }
                      className={`p-2.5 rounded-xl border flex items-center justify-between cursor-pointer transition-all ${
                        isSelected
                          ? 'bg-emerald-50/70 border-emerald-300 shadow-xs'
                          : 'bg-slate-50/60 border-slate-200/60 hover:bg-slate-100/70'
                      }`}
                    >
                      <div className="flex items-center gap-2.5 truncate max-w-[170px]">
                        <span
                          className="w-3 h-3 rounded-full shrink-0"
                          style={{ backgroundColor: item.color }}
                        />
                        <div className="truncate">
                          <p className="text-xs font-bold text-slate-800 truncate">
                            {item.name}
                          </p>
                          <p className="text-[10px] text-slate-400">
                            {item.count} txs • {item.percentage.toFixed(1)}%
                          </p>
                        </div>
                      </div>

                      <div className="text-right">
                        <p className="text-xs font-extrabold text-slate-900 font-mono">
                          {LanguageHelper.formatCurrency(item.amount, languageMode)}
                        </p>
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          ) : (
            /* Table View */
            <div className="border border-slate-200/80 rounded-2xl overflow-hidden">
              <table className="w-full text-left text-xs">
                <thead className="bg-slate-50 border-b border-slate-200 font-bold text-slate-500 uppercase text-[10px]">
                  <tr>
                    <th className="px-4 py-2.5">Category</th>
                    <th className="px-4 py-2.5 text-center">Txs</th>
                    <th className="px-4 py-2.5 text-right">Share</th>
                    <th className="px-4 py-2.5 text-right">Amount</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {summaryItems.map((item) => (
                    <tr key={item.id} className="hover:bg-slate-50/80 transition-colors">
                      <td className="px-4 py-2.5 font-bold text-slate-800 flex items-center gap-2">
                        <span
                          className="w-2.5 h-2.5 rounded-full"
                          style={{ backgroundColor: item.color }}
                        />
                        {item.name}
                      </td>
                      <td className="px-4 py-2.5 text-center text-slate-500">
                        {item.count}
                      </td>
                      <td className="px-4 py-2.5 text-right font-medium text-slate-600">
                        {item.percentage.toFixed(1)}%
                      </td>
                      <td className="px-4 py-2.5 text-right font-extrabold text-slate-900 font-mono">
                        {LanguageHelper.formatCurrency(item.amount, languageMode)}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
