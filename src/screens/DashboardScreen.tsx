import React, { useState, useMemo } from 'react';
import {
  TrendingUp,
  TrendingDown,
  Scale,
  Wallet,
  Calendar as CalendarIcon,
  ChevronRight,
  ArrowRight,
  ShieldCheck,
  AlertTriangle,
  CreditCard,
  Plus,
  Zap,
  Info,
  HelpCircle,
} from 'lucide-react';
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
} from 'recharts';
import { useBudget } from '../context/BudgetContext';
import {
  AppTab,
  DashboardCardType,
  DailySummaryMode,
  BudgetChartShape,
  TransactionType,
  AccountType,
} from '../types';
import { LanguageHelper } from '../utils/languageHelper';

interface DashboardScreenProps {
  onOpenNewTransaction: () => void;
  onOpenEditTransaction: (txId: number) => void;
  onOpenAccountDetail: (accId: number) => void;
}

export const DashboardScreen: React.FC<DashboardScreenProps> = ({
  onOpenNewTransaction,
  onOpenEditTransaction,
  onOpenAccountDetail,
}) => {
  const {
    accounts,
    categories,
    transactions,
    financialOverview,
    dashboardConfig,
    languageMode,
    selectedYear,
    selectedMonth,
    accountBalances,
    paymentSourceAnalysis,
    setCurrentTab,
  } = useBudget();

  const [showFormulaBreakdown, setShowFormulaBreakdown] = useState(false);

  // Daily Chart Data for the month or last 7 days
  const dailyData = useMemo(() => {
    const daysInMonth = new Date(selectedYear, selectedMonth, 0).getDate();
    const dataMap: { [day: number]: { income: number; expense: number } } = {};

    for (let d = 1; d <= daysInMonth; d++) {
      dataMap[d] = { income: 0, expense: 0 };
    }

    transactions.forEach((tx) => {
      const d = new Date(tx.dateEpochMs);
      if (d.getFullYear() === selectedYear && d.getMonth() + 1 === selectedMonth) {
        const day = d.getDate();
        if (tx.type === TransactionType.INCOME) {
          dataMap[day].income += tx.amount;
        } else if (tx.type === TransactionType.EXPENSE) {
          dataMap[day].expense += tx.amount;
        }
      }
    });

    return Object.keys(dataMap).map((dayKey) => {
      const dayNum = Number(dayKey);
      return {
        day: `${dayNum}`,
        income: dataMap[dayNum].income,
        expense: dataMap[dayNum].expense,
      };
    });
  }, [transactions, selectedYear, selectedMonth]);

  // Budget Donut Chart Data
  const budgetChartData = useMemo(() => {
    const monthTransactions = transactions.filter((t) => {
      const d = new Date(t.dateEpochMs);
      return (
        d.getFullYear() === selectedYear &&
        d.getMonth() + 1 === selectedMonth &&
        t.type === TransactionType.EXPENSE &&
        t.categoryId
      );
    });

    const spendingPerCat = new Map<number, number>();
    monthTransactions.forEach((t) => {
      spendingPerCat.set(t.categoryId!, (spendingPerCat.get(t.categoryId!) || 0) + t.amount);
    });

    const expenseCategories = categories.filter((c) => c.type === 'EXPENSE');
    const items = expenseCategories.map((cat) => ({
      name: LanguageHelper.getLocalizedName(cat.nameEn, cat.nameBn, languageMode),
      value: spendingPerCat.get(cat.id) || 0,
      budget: cat.monthlyBudget,
      color: cat.colorHex || '#F59E0B',
    }));

    return items.filter((i) => i.value > 0);
  }, [categories, transactions, selectedYear, selectedMonth, languageMode]);

  // Calendar Heatmap Data
  const calendarDays = useMemo(() => {
    const firstDayIndex = new Date(selectedYear, selectedMonth - 1, 1).getDay(); // 0 = Sunday
    const totalDays = new Date(selectedYear, selectedMonth, 0).getDate();

    const days = [];
    // Leading blanks
    for (let i = 0; i < firstDayIndex; i++) {
      days.push({ day: 0, isCurrentMonth: false, income: 0, expense: 0 });
    }

    // Days of the month
    for (let d = 1; d <= totalDays; d++) {
      let income = 0;
      let expense = 0;
      transactions.forEach((tx) => {
        const dt = new Date(tx.dateEpochMs);
        if (
          dt.getFullYear() === selectedYear &&
          dt.getMonth() + 1 === selectedMonth &&
          dt.getDate() === d
        ) {
          if (tx.type === TransactionType.INCOME) income += tx.amount;
          if (tx.type === TransactionType.EXPENSE) expense += tx.amount;
        }
      });
      days.push({ day: d, isCurrentMonth: true, income, expense });
    }

    return days;
  }, [transactions, selectedYear, selectedMonth]);

  // Favorite Accounts
  const favoriteAccounts = useMemo(() => {
    return accounts.filter((a) => !a.isArchived).slice(0, 4);
  }, [accounts]);

  // Recent Transactions (top 5)
  const recentTransactions = useMemo(() => {
    return transactions.slice(0, 5);
  }, [transactions]);

  const enabledCards = dashboardConfig.cardsOrder.filter((c) =>
    dashboardConfig.enabledCards.includes(c)
  );

  return (
    <div className="space-y-4 sm:space-y-6 pb-20">
      {/* Top Banner: Financial Overview Summary Bar */}
      <section className="grid grid-cols-2 lg:grid-cols-4 gap-2.5 sm:gap-4">
        {/* Net Worth */}
        <div className="p-4 bg-white rounded-3xl border border-slate-200/80 shadow-xs flex flex-col justify-between">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-500 uppercase tracking-wider">
              {LanguageHelper.getString('net_worth', languageMode)}
            </span>
            <div className="w-7 h-7 rounded-xl bg-emerald-50 text-emerald-600 flex items-center justify-center">
              <Scale className="w-4 h-4" />
            </div>
          </div>
          <div className="mt-2">
            <div className="text-xl sm:text-2xl font-extrabold text-slate-900 font-mono tracking-tight">
              {LanguageHelper.formatCurrency(financialOverview.netWorth, languageMode)}
            </div>
            <div className="flex items-center gap-1.5 mt-1 text-[11px] font-medium text-emerald-600">
              <span>Assets: {LanguageHelper.formatCurrency(financialOverview.totalAssets, languageMode)}</span>
            </div>
          </div>
        </div>

        {/* Monthly Inflow / Income */}
        <div className="p-4 bg-white rounded-3xl border border-slate-200/80 shadow-xs flex flex-col justify-between">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-500 uppercase tracking-wider">
              {LanguageHelper.getString('incomes', languageMode)}
            </span>
            <div className="w-7 h-7 rounded-xl bg-emerald-50 text-emerald-600 flex items-center justify-center">
              <TrendingUp className="w-4 h-4" />
            </div>
          </div>
          <div className="mt-2">
            <div className="text-xl sm:text-2xl font-extrabold text-emerald-600 font-mono tracking-tight">
              {LanguageHelper.formatCurrency(financialOverview.monthlyIncome, languageMode)}
            </div>
            <div className="text-[11px] text-slate-500 mt-1">This calendar month</div>
          </div>
        </div>

        {/* Monthly Outflow / Expense */}
        <div className="p-4 bg-white rounded-3xl border border-slate-200/80 shadow-xs flex flex-col justify-between">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-500 uppercase tracking-wider">
              {LanguageHelper.getString('expenses', languageMode)}
            </span>
            <div className="w-7 h-7 rounded-xl bg-rose-50 text-rose-600 flex items-center justify-center">
              <TrendingDown className="w-4 h-4" />
            </div>
          </div>
          <div className="mt-2">
            <div className="text-xl sm:text-2xl font-extrabold text-rose-600 font-mono tracking-tight">
              {LanguageHelper.formatCurrency(financialOverview.monthlyExpense, languageMode)}
            </div>
            <div className="text-[11px] text-slate-500 mt-1">
              Net: {LanguageHelper.formatCurrency(financialOverview.netIncome, languageMode)}
            </div>
          </div>
        </div>

        {/* Payment Source Shortfall Status */}
        <div
          onClick={() => setCurrentTab(AppTab.PAYMENT_SOURCE)}
          className="p-4 bg-gradient-to-br from-slate-900 to-slate-800 text-white rounded-3xl shadow-xs flex flex-col justify-between cursor-pointer hover:shadow-md transition-all group"
        >
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-300 uppercase tracking-wider">
              Payment Source
            </span>
            <ArrowRight className="w-4 h-4 text-slate-400 group-hover:translate-x-1 transition-transform" />
          </div>
          <div className="mt-2">
            {paymentSourceAnalysis.netShortfall > 0 ? (
              <>
                <div className="text-xl sm:text-2xl font-extrabold text-rose-400 font-mono tracking-tight flex items-center gap-1.5">
                  <AlertTriangle className="w-5 h-5 text-rose-400" />
                  <span>{LanguageHelper.formatCurrency(paymentSourceAnalysis.netShortfall, languageMode)}</span>
                </div>
                <div className="text-[11px] text-rose-200 mt-1">Shortfall needs funding</div>
              </>
            ) : (
              <>
                <div className="text-xl sm:text-2xl font-extrabold text-emerald-400 font-mono tracking-tight flex items-center gap-1.5">
                  <ShieldCheck className="w-5 h-5 text-emerald-400" />
                  <span>Fully Funded</span>
                </div>
                <div className="text-[11px] text-emerald-200 mt-1">
                  Surplus: {LanguageHelper.formatCurrency(paymentSourceAnalysis.netSurplus, languageMode)}
                </div>
              </>
            )}
          </div>
        </div>
      </section>

      {/* Expendable & Expected Expendable Summary */}
      <section className="bg-white rounded-3xl border border-slate-200/80 shadow-xs p-4 sm:p-5">
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 sm:gap-4">
          {/* Card 1: Expendable */}
          <div
            onClick={() => setShowFormulaBreakdown((prev) => !prev)}
            className={`p-4 rounded-2xl border transition-all cursor-pointer ${
              financialOverview.expendable >= 0
                ? 'bg-emerald-50/50 border-emerald-200/70 hover:bg-emerald-50/80'
                : 'bg-rose-50/50 border-rose-200/70 hover:bg-rose-50/80'
            }`}
          >
            <div className="flex items-center justify-between gap-2">
              <span
                className={`text-xs font-bold truncate flex-1 min-w-0 ${
                  financialOverview.expendable >= 0 ? 'text-emerald-950' : 'text-rose-950'
                }`}
              >
                {LanguageHelper.getString('expendable', languageMode)}
              </span>
              <span
                className={`inline-flex items-center px-2 py-0.5 rounded-md text-[10px] font-extrabold tracking-wide whitespace-nowrap shrink-0 select-none ${
                  financialOverview.expendable >= 0
                    ? 'bg-emerald-600 text-white'
                    : 'bg-rose-600 text-white'
                }`}
              >
                {financialOverview.expendable >= 0 ? 'Safe' : 'Deficit'}
              </span>
            </div>

            <div className="mt-2.5 flex items-center gap-2">
              <span
                className={`text-xl sm:text-2xl font-extrabold font-mono tracking-tight ${
                  financialOverview.expendable >= 0 ? 'text-emerald-900' : 'text-rose-900'
                }`}
              >
                {LanguageHelper.formatCurrency(financialOverview.expendable, languageMode)}
              </span>
              <Info className="w-4 h-4 text-slate-400 shrink-0" />
            </div>
            <div className="text-[11px] text-slate-500 mt-1">Available minus committed budgets</div>
          </div>

          {/* Card 2: Expected Expendable */}
          <div
            onClick={() => setShowFormulaBreakdown((prev) => !prev)}
            className="p-4 rounded-2xl bg-sky-50/50 border border-sky-200/70 hover:bg-sky-50/80 transition-all cursor-pointer"
          >
            <div className="flex items-center justify-between gap-2">
              <span className="text-xs font-bold text-sky-950 truncate flex-1 min-w-0">
                {LanguageHelper.getString('expected_expendable', languageMode)}
              </span>
              <span className="inline-flex items-center px-2 py-0.5 rounded-md text-[10px] font-bold bg-sky-200/80 text-sky-900 whitespace-nowrap shrink-0 select-none">
                +Income
              </span>
            </div>

            <div className="mt-2.5 flex items-center gap-2">
              <span className="text-xl sm:text-2xl font-extrabold text-sky-900 font-mono tracking-tight">
                {LanguageHelper.formatCurrency(financialOverview.expectedExpendable, languageMode)}
              </span>
              <Info className="w-4 h-4 text-sky-500 shrink-0" />
            </div>
            <div className="text-[11px] text-slate-500 mt-1">Expendable + Expected Month Income</div>
          </div>
        </div>

        {/* Calculation Breakdown Walkthrough */}
        {showFormulaBreakdown && (
          <div className="mt-3.5 pt-3.5 border-t border-slate-200/80 bg-slate-50/70 rounded-xl p-3.5 text-xs space-y-2">
            <div className="font-bold text-slate-900 flex items-center gap-1.5">
              <HelpCircle className="w-3.5 h-3.5 text-emerald-600" />
              <span>{LanguageHelper.getString('expendable_breakdown', languageMode)}</span>
            </div>
            <div className="space-y-1 text-slate-600 font-mono text-[11px]">
              <div>
                • Available Money = Total Assets (
                <span className="font-bold text-slate-900">
                  {LanguageHelper.formatCurrency(financialOverview.availableMoney, languageMode)}
                </span>
                )
              </div>
              <div>
                • Total Expense Budget ={' '}
                <span className="font-bold text-slate-900">
                  {LanguageHelper.formatCurrency(financialOverview.totalExpenseBudget, languageMode)}
                </span>
              </div>
              <div>
                • Additional / Over-Budget Cost ={' '}
                <span className="font-bold text-slate-900">
                  {LanguageHelper.formatCurrency(financialOverview.additionalCost, languageMode)}
                </span>
              </div>
              <div className="text-emerald-700 font-bold bg-emerald-50/80 p-1.5 rounded-lg">
                • Expendable = Available − (Budget + Over-Budget) ={' '}
                {LanguageHelper.formatCurrency(financialOverview.expendable, languageMode)}
              </div>
              <div>
                • Potential Income ={' '}
                <span className="font-bold text-slate-900">
                  {LanguageHelper.formatCurrency(financialOverview.potentialIncome, languageMode)}
                </span>
              </div>
              <div className="text-sky-700 font-bold bg-sky-50/80 p-1.5 rounded-lg">
                • Expected Expendable = Expendable + Potential Income ={' '}
                {LanguageHelper.formatCurrency(financialOverview.expectedExpendable, languageMode)}
              </div>
            </div>
          </div>
        )}
      </section>

      {/* Render Configured Dashboard Cards */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-4 sm:gap-6">
        {enabledCards.map((card) => {
          switch (card) {
            case DashboardCardType.DAILY_SUMMARY:
              return (
                <div
                  key={card}
                  className="p-5 bg-white rounded-3xl border border-slate-200/80 shadow-xs flex flex-col"
                >
                  <div className="flex items-center justify-between mb-4">
                    <div>
                      <h3 className="font-bold text-slate-900 text-sm sm:text-base">
                        {LanguageHelper.getString('daily_summary', languageMode)}
                      </h3>
                      <p className="text-xs text-slate-500">Income vs Expense activity per day</p>
                    </div>
                  </div>

                  <div className="h-56 w-full">
                    <ResponsiveContainer width="100%" height="100%">
                      <BarChart data={dailyData} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                        <XAxis dataKey="day" tick={{ fontSize: 10 }} stroke="#94A3B8" />
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
                        <Bar dataKey="income" fill="#10B981" radius={[4, 4, 0, 0]} name="Income" />
                        <Bar dataKey="expense" fill="#EF4444" radius={[4, 4, 0, 0]} name="Expense" />
                      </BarChart>
                    </ResponsiveContainer>
                  </div>
                </div>
              );

            case DashboardCardType.BUDGET_SUMMARY:
              return (
                <div
                  key={card}
                  className="p-5 bg-white rounded-3xl border border-slate-200/80 shadow-xs flex flex-col"
                >
                  <div className="flex items-center justify-between mb-4">
                    <div>
                      <h3 className="font-bold text-slate-900 text-sm sm:text-base">
                        {LanguageHelper.getString('budget_summary', languageMode)}
                      </h3>
                      <p className="text-xs text-slate-500">Category spending breakdown</p>
                    </div>
                    <button
                      onClick={() => setCurrentTab(AppTab.BUDGET)}
                      className="text-xs text-emerald-600 font-semibold hover:underline flex items-center gap-0.5"
                    >
                      <span>View All</span>
                      <ChevronRight className="w-3.5 h-3.5" />
                    </button>
                  </div>

                  {budgetChartData.length === 0 ? (
                    <div className="flex-1 flex flex-col items-center justify-center text-slate-400 py-12 text-xs">
                      No expense data recorded this month.
                    </div>
                  ) : (
                    <div className="grid grid-cols-1 sm:grid-cols-2 items-center gap-4">
                      <div className="h-44 w-full">
                        <ResponsiveContainer width="100%" height="100%">
                          <PieChart>
                            <Pie
                              data={budgetChartData}
                              cx="50%"
                              cy="50%"
                              innerRadius={
                                dashboardConfig.budgetChartShape === BudgetChartShape.DONUT ? 45 : 0
                              }
                              outerRadius={65}
                              paddingAngle={3}
                              dataKey="value"
                            >
                              {budgetChartData.map((entry, index) => (
                                <Cell key={`cell-${index}`} fill={entry.color} />
                              ))}
                            </Pie>
                            <Tooltip
                              formatter={(val: number) =>
                                LanguageHelper.formatCurrency(val, languageMode)
                              }
                            />
                          </PieChart>
                        </ResponsiveContainer>
                      </div>

                      <div className="space-y-1.5 overflow-y-auto max-h-44 pr-1">
                        {budgetChartData.slice(0, 5).map((item, idx) => (
                          <div key={idx} className="flex items-center justify-between text-xs">
                            <div className="flex items-center gap-2 truncate max-w-[120px]">
                              <span
                                className="w-2.5 h-2.5 rounded-full shrink-0"
                                style={{ backgroundColor: item.color }}
                              />
                              <span className="text-slate-700 truncate">{item.name}</span>
                            </div>
                            <span className="font-semibold text-slate-900 font-mono">
                              {LanguageHelper.formatCurrency(item.value, languageMode)}
                            </span>
                          </div>
                        ))}
                      </div>
                    </div>
                  )}
                </div>
              );

            case DashboardCardType.FAVORITE_ACCOUNTS:
              return (
                <div
                  key={card}
                  className="p-5 bg-white rounded-3xl border border-slate-200/80 shadow-xs flex flex-col"
                >
                  <div className="flex items-center justify-between mb-4">
                    <div>
                      <h3 className="font-bold text-slate-900 text-sm sm:text-base">
                        {LanguageHelper.getString('favorite_accounts', languageMode)}
                      </h3>
                      <p className="text-xs text-slate-500">Live balance breakdown</p>
                    </div>
                    <button
                      onClick={() => setCurrentTab(AppTab.ACCOUNTS)}
                      className="text-xs text-emerald-600 font-semibold hover:underline flex items-center gap-0.5"
                    >
                      <span>Manage</span>
                      <ChevronRight className="w-3.5 h-3.5" />
                    </button>
                  </div>

                  <div className="grid grid-cols-2 gap-2.5">
                    {favoriteAccounts.map((acc) => {
                      const bal = accountBalances.get(acc.id) || acc.initialBalance;
                      return (
                        <div
                          key={acc.id}
                          onClick={() => onOpenAccountDetail(acc.id)}
                          className="p-3.5 bg-slate-50 hover:bg-slate-100/80 rounded-2xl border border-slate-200/70 cursor-pointer transition-all"
                        >
                          <div className="flex items-center justify-between">
                            <span
                              className="w-2 h-2 rounded-full"
                              style={{ backgroundColor: acc.colorHex || '#10B981' }}
                            />
                            <span className="text-[10px] uppercase font-bold text-slate-400">
                              {acc.type}
                            </span>
                          </div>
                          <div className="font-bold text-slate-800 text-xs sm:text-sm mt-2 truncate">
                            {LanguageHelper.getLocalizedName(acc.nameEn, acc.nameBn, languageMode)}
                          </div>
                          <div className="text-sm sm:text-base font-extrabold text-slate-900 font-mono mt-0.5">
                            {LanguageHelper.formatCurrency(bal, languageMode)}
                          </div>
                        </div>
                      );
                    })}
                  </div>
                </div>
              );

            case DashboardCardType.CALENDAR_SUMMARY:
              return (
                <div
                  key={card}
                  className="p-5 bg-white rounded-3xl border border-slate-200/80 shadow-xs flex flex-col"
                >
                  <div className="flex items-center justify-between mb-3">
                    <div>
                      <h3 className="font-bold text-slate-900 text-sm sm:text-base flex items-center gap-1.5">
                        <CalendarIcon className="w-4 h-4 text-emerald-600" />
                        <span>{LanguageHelper.getString('calendar_view', languageMode)}</span>
                      </h3>
                      <p className="text-xs text-slate-500">Monthly transaction calendar</p>
                    </div>
                  </div>

                  {/* Calendar Grid */}
                  <div className="grid grid-cols-7 gap-1 text-center">
                    {['S', 'M', 'T', 'W', 'T', 'F', 'S'].map((wd, i) => (
                      <div key={i} className="text-[11px] font-bold text-slate-400 py-1">
                        {wd}
                      </div>
                    ))}
                    {calendarDays.map((cd, idx) => {
                      const hasActivity = cd.income > 0 || cd.expense > 0;
                      return (
                        <div
                          key={idx}
                          className={`p-1 min-h-[38px] rounded-xl flex flex-col items-center justify-between text-xs transition-colors ${
                            !cd.isCurrentMonth
                              ? 'opacity-20'
                              : hasActivity
                              ? 'bg-slate-100 font-bold text-slate-900'
                              : 'text-slate-600 hover:bg-slate-50'
                          }`}
                        >
                          {cd.isCurrentMonth && <span>{cd.day}</span>}
                          {hasActivity && (
                            <div className="flex items-center gap-0.5 mt-0.5">
                              {cd.income > 0 && <span className="w-1.5 h-1.5 rounded-full bg-emerald-500" />}
                              {cd.expense > 0 && <span className="w-1.5 h-1.5 rounded-full bg-rose-500" />}
                            </div>
                          )}
                        </div>
                      );
                    })}
                  </div>
                </div>
              );

            case DashboardCardType.RECENT_TRANSACTIONS:
              return (
                <div
                  key={card}
                  className="p-5 bg-white rounded-3xl border border-slate-200/80 shadow-xs flex flex-col"
                >
                  <div className="flex items-center justify-between mb-3">
                    <div>
                      <h3 className="font-bold text-slate-900 text-sm sm:text-base">
                        {LanguageHelper.getString('recent_transactions', languageMode)}
                      </h3>
                      <p className="text-xs text-slate-500">Latest recorded journal entries</p>
                    </div>
                    <button
                      onClick={() => setCurrentTab(AppTab.TRANSACTIONS)}
                      className="text-xs text-emerald-600 font-semibold hover:underline flex items-center gap-0.5"
                    >
                      <span>Ledger</span>
                      <ChevronRight className="w-3.5 h-3.5" />
                    </button>
                  </div>

                  <div className="space-y-2 flex-1">
                    {recentTransactions.map((tx) => {
                      const isIncome = tx.type === TransactionType.INCOME;
                      const isTransfer = tx.type === TransactionType.TRANSFER;
                      return (
                        <div
                          key={tx.id}
                          onClick={() => onOpenEditTransaction(tx.id)}
                          className="p-2.5 bg-slate-50 hover:bg-slate-100/80 rounded-xl border border-slate-200/60 flex items-center justify-between text-xs cursor-pointer transition-colors"
                        >
                          <div className="flex items-center gap-2.5">
                            <div
                              className={`w-7 h-7 rounded-lg flex items-center justify-center font-bold text-xs ${
                                isIncome
                                  ? 'bg-emerald-100 text-emerald-700'
                                  : isTransfer
                                  ? 'bg-blue-100 text-blue-700'
                                  : 'bg-rose-100 text-rose-700'
                              }`}
                            >
                              {isIncome ? '↓' : isTransfer ? '⇄' : '↑'}
                            </div>
                            <div>
                              <div className="font-semibold text-slate-800 leading-tight">
                                {tx.payeePayer || tx.note || 'Transaction'}
                              </div>
                              <div className="text-[10px] text-slate-400">
                                {new Date(tx.dateEpochMs).toLocaleDateString()}
                              </div>
                            </div>
                          </div>

                          <div
                            className={`font-bold font-mono text-xs sm:text-sm ${
                              isIncome ? 'text-emerald-600' : isTransfer ? 'text-blue-600' : 'text-rose-600'
                            }`}
                          >
                            {isIncome ? '+' : isTransfer ? '' : '-'}
                            {LanguageHelper.formatCurrency(tx.amount, languageMode)}
                          </div>
                        </div>
                      );
                    })}
                  </div>
                </div>
              );

            case DashboardCardType.PAYMENT_SOURCE_PREVIEW:
              return (
                <div
                  key={card}
                  className="p-5 bg-white rounded-3xl border border-slate-200/80 shadow-xs flex flex-col"
                >
                  <div className="flex items-center justify-between mb-3">
                    <div>
                      <h3 className="font-bold text-slate-900 text-sm sm:text-base">
                        Fund Allocation & Transfer Insight
                      </h3>
                      <p className="text-xs text-slate-500">Smart recommendations to cover shortfalls</p>
                    </div>
                    <button
                      onClick={() => setCurrentTab(AppTab.PAYMENT_SOURCE)}
                      className="text-xs text-emerald-600 font-semibold hover:underline flex items-center gap-0.5"
                    >
                      <span>Analyze</span>
                      <ChevronRight className="w-3.5 h-3.5" />
                    </button>
                  </div>

                  {paymentSourceAnalysis.suggestions.length === 0 ? (
                    <div className="flex-1 flex flex-col items-center justify-center text-slate-500 py-8 text-xs bg-slate-50 rounded-2xl">
                      <ShieldCheck className="w-6 h-6 text-emerald-500 mb-1" />
                      <span>All accounts have adequate funds for this month's budget.</span>
                    </div>
                  ) : (
                    <div className="space-y-2">
                      {paymentSourceAnalysis.suggestions.map((s, idx) => (
                        <div
                          key={idx}
                          className="p-3 bg-amber-50/70 border border-amber-200/70 rounded-2xl flex items-center justify-between text-xs"
                        >
                          <div>
                            <div className="font-semibold text-amber-950">
                              Move from {s.fromAccount.nameEn} to {s.toAccount.nameEn}
                            </div>
                            <div className="text-[11px] text-amber-800/80 mt-0.5">
                              Recommended Amount: {LanguageHelper.formatCurrency(s.suggestedAmount, languageMode)}
                            </div>
                          </div>
                          <button
                            onClick={() => setCurrentTab(AppTab.PAYMENT_SOURCE)}
                            className="px-3 py-1.5 bg-amber-600 hover:bg-amber-700 text-white rounded-xl font-bold text-xs shadow-2xs"
                          >
                            Transfer
                          </button>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              );

            default:
              return null;
          }
        })}
      </div>
    </div>
  );
};
