import React, { useMemo } from 'react';
import {
  TrendingUp,
  TrendingDown,
  Scale,
  Calendar,
  DollarSign,
  ArrowUpRight,
  ArrowDownRight,
} from 'lucide-react';
import {
  AreaChart,
  Area,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
  CartesianGrid,
} from 'recharts';
import { useBudget } from '../context/BudgetContext';
import { TransactionType, LanguageMode } from '../types';
import { LanguageHelper } from '../utils/languageHelper';

export const NetEarningsScreen: React.FC = () => {
  const { transactions, financialOverview, languageMode, selectedYear, selectedMonth } = useBudget();

  // 6-month historical trend
  const trendData = useMemo(() => {
    const data = [];
    for (let i = 5; i >= 0; i--) {
      const d = new Date(selectedYear, selectedMonth - 1 - i, 1);
      const y = d.getFullYear();
      const m = d.getMonth() + 1;
      const monthLabel = d.toLocaleString('default', { month: 'short' });

      let inc = 0;
      let exp = 0;

      transactions.forEach((tx) => {
        const txDate = new Date(tx.dateEpochMs);
        if (txDate.getFullYear() === y && txDate.getMonth() + 1 === m) {
          if (tx.type === TransactionType.INCOME) inc += tx.amount;
          if (tx.type === TransactionType.EXPENSE) exp += tx.amount;
        }
      });

      data.push({
        month: monthLabel,
        income: inc,
        expense: exp,
        net: inc - exp,
      });
    }
    return data;
  }, [transactions, selectedYear, selectedMonth]);

  const savingsRate =
    financialOverview.monthlyIncome > 0
      ? (financialOverview.netIncome / financialOverview.monthlyIncome) * 100
      : 0;

  return (
    <div className="space-y-4 sm:space-y-6 pb-20">
      {/* Top Stat Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        {/* Net Income */}
        <div className="p-5 bg-white rounded-3xl border border-slate-200/80 shadow-xs flex flex-col justify-between">
          <span className="text-xs font-semibold text-slate-500 uppercase tracking-wider">
            {LanguageHelper.getString('net_earnings', languageMode)}
          </span>
          <div className="mt-2">
            <div
              className={`text-2xl sm:text-3xl font-extrabold font-mono tracking-tight ${
                financialOverview.netIncome >= 0 ? 'text-emerald-600' : 'text-rose-600'
              }`}
            >
              {LanguageHelper.formatCurrency(financialOverview.netIncome, languageMode)}
            </div>
            <div className="flex items-center gap-1 mt-1 text-xs font-semibold text-slate-600">
              <span>Savings Rate: {savingsRate.toFixed(1)}%</span>
            </div>
          </div>
        </div>

        {/* Inflow */}
        <div className="p-5 bg-white rounded-3xl border border-slate-200/80 shadow-xs flex flex-col justify-between">
          <span className="text-xs font-semibold text-slate-500 uppercase tracking-wider">
            Gross Incomes
          </span>
          <div className="mt-2">
            <div className="text-2xl sm:text-3xl font-extrabold text-emerald-600 font-mono tracking-tight">
              {LanguageHelper.formatCurrency(financialOverview.monthlyIncome, languageMode)}
            </div>
            <div className="text-xs text-slate-400 mt-1">Total revenue collected</div>
          </div>
        </div>

        {/* Outflow */}
        <div className="p-5 bg-white rounded-3xl border border-slate-200/80 shadow-xs flex flex-col justify-between">
          <span className="text-xs font-semibold text-slate-500 uppercase tracking-wider">
            Total Expenditures
          </span>
          <div className="mt-2">
            <div className="text-2xl sm:text-3xl font-extrabold text-rose-600 font-mono tracking-tight">
              {LanguageHelper.formatCurrency(financialOverview.monthlyExpense, languageMode)}
            </div>
            <div className="text-xs text-slate-400 mt-1">Total spending incurred</div>
          </div>
        </div>
      </div>

      {/* 6-Month Trend Chart */}
      <div className="p-6 bg-white rounded-3xl border border-slate-200/80 shadow-xs space-y-4">
        <div>
          <h3 className="font-bold text-slate-900 text-base">Net Cash Flow History</h3>
          <p className="text-xs text-slate-500">Income vs Expense progression over the last 6 months</p>
        </div>

        <div className="h-64 w-full">
          <ResponsiveContainer width="100%" height="100%">
            <AreaChart data={trendData} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
              <defs>
                <linearGradient id="incomeGrad" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="#10B981" stopOpacity={0.4} />
                  <stop offset="95%" stopColor="#10B981" stopOpacity={0} />
                </linearGradient>
                <linearGradient id="expenseGrad" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="#EF4444" stopOpacity={0.4} />
                  <stop offset="95%" stopColor="#EF4444" stopOpacity={0} />
                </linearGradient>
              </defs>
              <CartesianGrid strokeDasharray="3 3" stroke="#F1F5F9" />
              <XAxis dataKey="month" tick={{ fontSize: 11 }} stroke="#94A3B8" />
              <YAxis tick={{ fontSize: 11 }} stroke="#94A3B8" />
              <Tooltip
                formatter={(val: number) => LanguageHelper.formatCurrency(val, languageMode)}
                contentStyle={{
                  borderRadius: '12px',
                  border: '1px solid #E2E8F0',
                  fontSize: '12px',
                }}
              />
              <Area
                type="monotone"
                dataKey="income"
                stroke="#10B981"
                strokeWidth={2}
                fillOpacity={1}
                fill="url(#incomeGrad)"
                name="Income"
              />
              <Area
                type="monotone"
                dataKey="expense"
                stroke="#EF4444"
                strokeWidth={2}
                fillOpacity={1}
                fill="url(#expenseGrad)"
                name="Expense"
              />
            </AreaChart>
          </ResponsiveContainer>
        </div>
      </div>
    </div>
  );
};
