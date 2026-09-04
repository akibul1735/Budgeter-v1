import React, { createContext, useContext, useState, useEffect, useMemo, useCallback } from 'react';
import {
  Account,
  Category,
  Transaction,
  MonthlyBudget,
  RecurringBill,
  BudgetAdjustment,
  AccountCategoryAllocation,
  LanguageMode,
  CurrencyConfig,
  CurrencyDisplayMode,
  NavigationTabConfig,
  TabPosition,
  AppTab,
  DashboardConfig,
  DailySummaryMode,
  DailySummaryPeriod,
  BudgetChartShape,
  BudgetSummaryType,
  CalendarDisplayMode,
  DashboardCardType,
  AutofillConfig,
  TransactionType,
  HierarchyDisplayMode,
  FinancialOverview,
  AccountWithBalance,
  CategorySpending,
  RequirementCalculationBasis,
  PaymentSourceAnalysisOverview,
} from '../types';
import {
  DEFAULT_ACCOUNTS,
  DEFAULT_CATEGORIES,
  DEFAULT_TRANSACTIONS,
  DEFAULT_MONTHLY_BUDGETS,
  DEFAULT_RECURRING_BILLS,
} from '../utils/seedData';
import { LanguageHelper } from '../utils/languageHelper';
import { BalanceSheetHelper } from '../utils/balanceSheetHelper';
import { PaymentSourceCalculator } from '../utils/paymentSourceCalculator';

interface BudgetContextType {
  // State
  accounts: Account[];
  categories: Category[];
  transactions: Transaction[];
  monthlyBudgets: MonthlyBudget[];
  recurringBills: RecurringBill[];
  budgetAdjustments: BudgetAdjustment[];
  categoryAllocations: AccountCategoryAllocation[];
  languageMode: LanguageMode;
  currencyConfig: CurrencyConfig;
  tabConfig: NavigationTabConfig;
  dashboardConfig: DashboardConfig;
  autofillConfig: AutofillConfig;
  hierarchyDisplayMode: HierarchyDisplayMode;
  feeCategoryMemory: Record<string, number>;
  currentTab: AppTab;
  isDemoMode: boolean;
  selectedYear: number;
  selectedMonth: number;
  accountBalances: Map<number, number>;
  financialOverview: FinancialOverview;
  accountsWithBalances: AccountWithBalance[];
  categorySpending: CategorySpending[];
  paymentSourceAnalysis: PaymentSourceAnalysisOverview;
  calculationBasis: RequirementCalculationBasis;

  // View & Nav
  setCurrentTab: (tab: AppTab) => void;
  setSelectedYear: (year: number) => void;
  setSelectedMonth: (month: number) => void;
  prevMonth: () => void;
  nextMonth: () => void;
  setCurrentMonth: () => void;
  setCalculationBasis: (basis: RequirementCalculationBasis) => void;

  // Configuration Actions
  setLanguageMode: (mode: LanguageMode) => void;
  setCurrencyConfig: (config: CurrencyConfig) => void;
  setTabConfig: (config: NavigationTabConfig) => void;
  setDashboardConfig: (config: DashboardConfig) => void;
  setAutofillConfig: (config: AutofillConfig) => void;
  setHierarchyDisplayMode: (mode: HierarchyDisplayMode) => void;
  saveFeeCategoryPreference: (key: string, categoryId: number) => void;
  getRememberedFeeCategoryId: (key?: string) => number | null;
  toggleDemoMode: () => void;

  // Running balance
  getTransactionAccountRunningBalance: (txId: number, accountId: number) => number;

  // CRUD Transactions
  addTransaction: (tx: Omit<Transaction, 'id'>) => Transaction;
  addTransferWithFee: (
    transferTx: Omit<Transaction, 'id'>,
    feeAmount?: number,
    feeAccountId?: number | null,
    feeCategoryId?: number | null
  ) => { transfer: Transaction; fee?: Transaction };
  updateTransaction: (tx: Transaction) => void;
  deleteTransaction: (id: number) => void;

  // CRUD Accounts
  addAccount: (acc: Omit<Account, 'id'>) => Account;
  updateAccount: (acc: Account) => void;
  deleteAccount: (id: number) => void;
  toggleAccountCalculation: (id: number, isCalculated: boolean) => void;
  updateAccountAdjustment: (id: number, adjustment: number) => void;

  // CRUD Categories
  addCategory: (cat: Omit<Category, 'id'>) => Category;
  updateCategory: (cat: Category) => void;
  deleteCategory: (id: number) => void;

  // Budgets
  setMonthlyBudget: (categoryId: number, amount: number, year?: number, month?: number) => void;
  copyBudgetsFromPrevMonth: (sourceYear: number, sourceMonth: number, targetYear: number, targetMonth: number) => void;
  saveCategoryAllocations: (categoryId: number, allocations: { accountId: number; amount: number }[]) => void;

  // Recurring Bills
  addRecurringBill: (bill: Omit<RecurringBill, 'id'>) => RecurringBill;
  updateRecurringBill: (bill: RecurringBill) => void;
  deleteRecurringBill: (id: number) => void;
  markBillAsPaid: (billId: number) => void;

  // Budget Adjustments
  addBudgetAdjustment: (adj: Omit<BudgetAdjustment, 'id' | 'timestampEpochMs'>) => void;
  resetBudgetAdjustments: () => void;

  // Quick Transfer
  executeTransfer: (fromAccountId: number, toAccountId: number, amount: number, note?: string) => void;

  // Import / Export / Reset
  exportDataJson: () => string;
  importDataJson: (jsonStr: string) => boolean;
  resetToDefaults: () => void;
}

const BudgetContext = createContext<BudgetContextType | undefined>(undefined);

const STORAGE_KEYS = {
  ACCOUNTS: 'budgeter_accounts_v2',
  CATEGORIES: 'budgeter_categories_v2',
  TRANSACTIONS: 'budgeter_transactions_v2',
  MONTHLY_BUDGETS: 'budgeter_monthly_budgets_v2',
  RECURRING_BILLS: 'budgeter_recurring_bills_v2',
  BUDGET_ADJUSTMENTS: 'budgeter_budget_adjustments_v2',
  CATEGORY_ALLOCATIONS: 'budgeter_cat_allocations_v2',
  LANGUAGE: 'budgeter_lang_mode_v2',
  CURRENCY: 'budgeter_currency_cfg_v2',
  TABS: 'budgeter_tabs_cfg_v2',
  DASHBOARD: 'budgeter_dashboard_cfg_v2',
  AUTOFILL: 'budgeter_autofill_cfg_v2',
  DEMO_MODE: 'budgeter_demo_mode_v2',
  HIERARCHY_DISPLAY: 'budgeter_hierarchy_display_v2',
  FEE_MEMORY: 'budgeter_fee_cat_memory_v2',
};

export const BudgetProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const currentDate = new Date();
  const [selectedYear, setSelectedYear] = useState<number>(currentDate.getFullYear());
  const [selectedMonth, setSelectedMonth] = useState<number>(currentDate.getMonth() + 1);
  const [currentTab, setCurrentTab] = useState<AppTab>(AppTab.MAIN);
  const [calculationBasis, setCalculationBasis] = useState<RequirementCalculationBasis>(
    RequirementCalculationBasis.BUDGET_AMOUNT
  );

  // Load from local storage or fallback to defaults
  const [accounts, setAccounts] = useState<Account[]>(() => {
    try {
      const saved = localStorage.getItem(STORAGE_KEYS.ACCOUNTS);
      return saved ? JSON.parse(saved) : DEFAULT_ACCOUNTS;
    } catch {
      return DEFAULT_ACCOUNTS;
    }
  });

  const [categories, setCategories] = useState<Category[]>(() => {
    try {
      const saved = localStorage.getItem(STORAGE_KEYS.CATEGORIES);
      return saved ? JSON.parse(saved) : DEFAULT_CATEGORIES;
    } catch {
      return DEFAULT_CATEGORIES;
    }
  });

  const [transactions, setTransactions] = useState<Transaction[]>(() => {
    try {
      const saved = localStorage.getItem(STORAGE_KEYS.TRANSACTIONS);
      return saved ? JSON.parse(saved) : DEFAULT_TRANSACTIONS;
    } catch {
      return DEFAULT_TRANSACTIONS;
    }
  });

  const [monthlyBudgets, setMonthlyBudgets] = useState<MonthlyBudget[]>(() => {
    try {
      const saved = localStorage.getItem(STORAGE_KEYS.MONTHLY_BUDGETS);
      return saved ? JSON.parse(saved) : DEFAULT_MONTHLY_BUDGETS;
    } catch {
      return DEFAULT_MONTHLY_BUDGETS;
    }
  });

  const [recurringBills, setRecurringBills] = useState<RecurringBill[]>(() => {
    try {
      const saved = localStorage.getItem(STORAGE_KEYS.RECURRING_BILLS);
      return saved ? JSON.parse(saved) : DEFAULT_RECURRING_BILLS;
    } catch {
      return DEFAULT_RECURRING_BILLS;
    }
  });

  const [budgetAdjustments, setBudgetAdjustments] = useState<BudgetAdjustment[]>(() => {
    try {
      const saved = localStorage.getItem(STORAGE_KEYS.BUDGET_ADJUSTMENTS);
      return saved ? JSON.parse(saved) : [];
    } catch {
      return [];
    }
  });

  const [categoryAllocations, setCategoryAllocations] = useState<AccountCategoryAllocation[]>(() => {
    try {
      const saved = localStorage.getItem(STORAGE_KEYS.CATEGORY_ALLOCATIONS);
      return saved ? JSON.parse(saved) : [];
    } catch {
      return [];
    }
  });

  const [languageMode, setLanguageModeState] = useState<LanguageMode>(() => {
    try {
      const saved = localStorage.getItem(STORAGE_KEYS.LANGUAGE);
      return saved ? (saved as LanguageMode) : LanguageMode.ENGLISH;
    } catch {
      return LanguageMode.ENGLISH;
    }
  });

  const [currencyConfig, setCurrencyConfigState] = useState<CurrencyConfig>(() => {
    try {
      const saved = localStorage.getItem(STORAGE_KEYS.CURRENCY);
      return saved
        ? JSON.parse(saved)
        : {
            selectedCode: 'BDT',
            selectedSymbol: '৳',
            displayMode: CurrencyDisplayMode.SYMBOL_ONLY,
            customSymbol: '',
            customCode: '',
          };
    } catch {
      return {
        selectedCode: 'BDT',
        selectedSymbol: '৳',
        displayMode: CurrencyDisplayMode.SYMBOL_ONLY,
        customSymbol: '',
        customCode: '',
      };
    }
  });

  const [tabConfig, setTabConfigState] = useState<NavigationTabConfig>(() => {
    try {
      const saved = localStorage.getItem(STORAGE_KEYS.TABS);
      return saved
        ? JSON.parse(saved)
        : {
            position: TabPosition.BOTTOM,
            allTabsOrder: [
              AppTab.MAIN,
              AppTab.TRANSACTIONS,
              AppTab.PAYMENT_SOURCE,
              AppTab.BALANCE_SHEET,
              AppTab.BUDGET,
              AppTab.NET_EARNINGS,
              AppTab.REMINDERS,
              AppTab.ACCOUNTS,
              AppTab.CATEGORIES,
              AppTab.LABELS,
              AppTab.ITEMS_SUMMARY,
              AppTab.BUDGET_MAKER,
              AppTab.ACCOUNT_CALC,
              AppTab.BACKUP_SYNC,
              AppTab.SETTINGS,
            ],
            enabledTabs: [
              AppTab.MAIN,
              AppTab.TRANSACTIONS,
              AppTab.PAYMENT_SOURCE,
              AppTab.BALANCE_SHEET,
              AppTab.BUDGET,
              AppTab.NET_EARNINGS,
              AppTab.REMINDERS,
              AppTab.ACCOUNTS,
              AppTab.CATEGORIES,
            ],
          };
    } catch {
      return {
        position: TabPosition.BOTTOM,
        allTabsOrder: [
          AppTab.MAIN,
          AppTab.TRANSACTIONS,
          AppTab.PAYMENT_SOURCE,
          AppTab.BALANCE_SHEET,
          AppTab.BUDGET,
          AppTab.NET_EARNINGS,
          AppTab.REMINDERS,
          AppTab.ACCOUNTS,
          AppTab.CATEGORIES,
          AppTab.LABELS,
          AppTab.ITEMS_SUMMARY,
          AppTab.BUDGET_MAKER,
          AppTab.ACCOUNT_CALC,
          AppTab.BACKUP_SYNC,
          AppTab.SETTINGS,
        ],
        enabledTabs: [
          AppTab.MAIN,
          AppTab.TRANSACTIONS,
          AppTab.PAYMENT_SOURCE,
          AppTab.BALANCE_SHEET,
          AppTab.BUDGET,
          AppTab.NET_EARNINGS,
          AppTab.REMINDERS,
          AppTab.ACCOUNTS,
          AppTab.CATEGORIES,
        ],
      };
    }
  });

  const [dashboardConfig, setDashboardConfigState] = useState<DashboardConfig>(() => {
    try {
      const saved = localStorage.getItem(STORAGE_KEYS.DASHBOARD);
      return saved
        ? JSON.parse(saved)
        : {
            cardsOrder: [
              DashboardCardType.DAILY_SUMMARY,
              DashboardCardType.BUDGET_SUMMARY,
              DashboardCardType.FAVORITE_ACCOUNTS,
              DashboardCardType.CALENDAR_SUMMARY,
              DashboardCardType.RECENT_TRANSACTIONS,
              DashboardCardType.PAYMENT_SOURCE_PREVIEW,
            ],
            enabledCards: [
              DashboardCardType.DAILY_SUMMARY,
              DashboardCardType.BUDGET_SUMMARY,
              DashboardCardType.FAVORITE_ACCOUNTS,
              DashboardCardType.CALENDAR_SUMMARY,
              DashboardCardType.RECENT_TRANSACTIONS,
              DashboardCardType.PAYMENT_SOURCE_PREVIEW,
            ],
            dailySummaryMode: DailySummaryMode.BAR_CHART,
            dailySummaryPeriod: DailySummaryPeriod.LAST_7_DAYS,
            showValuesOnBars: true,
            showPeriodAverages: true,
            budgetChartShape: BudgetChartShape.DONUT,
            budgetSummaryType: BudgetSummaryType.ALL_CATEGORIES,
            topCategoriesCount: 5,
            showPercentages: true,
            showPaceMarker: true,
            calendarMode: CalendarDisplayMode.GRID,
            showIncomeBadges: true,
            showExpenseBadges: true,
            favoriteAccountIds: [1, 2, 3, 5],
          };
    } catch {
      return {
        cardsOrder: [
          DashboardCardType.DAILY_SUMMARY,
          DashboardCardType.BUDGET_SUMMARY,
          DashboardCardType.FAVORITE_ACCOUNTS,
          DashboardCardType.CALENDAR_SUMMARY,
          DashboardCardType.RECENT_TRANSACTIONS,
          DashboardCardType.PAYMENT_SOURCE_PREVIEW,
        ],
        enabledCards: [
          DashboardCardType.DAILY_SUMMARY,
          DashboardCardType.BUDGET_SUMMARY,
          DashboardCardType.FAVORITE_ACCOUNTS,
          DashboardCardType.CALENDAR_SUMMARY,
          DashboardCardType.RECENT_TRANSACTIONS,
          DashboardCardType.PAYMENT_SOURCE_PREVIEW,
        ],
        dailySummaryMode: DailySummaryMode.BAR_CHART,
        dailySummaryPeriod: DailySummaryPeriod.LAST_7_DAYS,
        showValuesOnBars: true,
        showPeriodAverages: true,
        budgetChartShape: BudgetChartShape.DONUT,
        budgetSummaryType: BudgetSummaryType.ALL_CATEGORIES,
        topCategoriesCount: 5,
        showPercentages: true,
        showPaceMarker: true,
        calendarMode: CalendarDisplayMode.GRID,
        showIncomeBadges: true,
        showExpenseBadges: true,
        favoriteAccountIds: [1, 2, 3, 5],
      };
    }
  });

  const [autofillConfig, setAutofillConfigState] = useState<AutofillConfig>(() => {
    try {
      const saved = localStorage.getItem(STORAGE_KEYS.AUTOFILL);
      return saved
        ? JSON.parse(saved)
        : {
            autofillCategory: true,
            autofillAccount: true,
            autofillAmount: true,
            autofillNotes: true,
            autofillLabels: true,
          };
    } catch {
      return {
        autofillCategory: true,
        autofillAccount: true,
        autofillAmount: true,
        autofillNotes: true,
        autofillLabels: true,
      };
    }
  });

  const [isDemoMode, setIsDemoMode] = useState<boolean>(() => {
    try {
      return localStorage.getItem(STORAGE_KEYS.DEMO_MODE) === 'true';
    } catch {
      return false;
    }
  });

  const [hierarchyDisplayMode, setHierarchyDisplayModeState] = useState<HierarchyDisplayMode>(() => {
    try {
      const saved = localStorage.getItem(STORAGE_KEYS.HIERARCHY_DISPLAY);
      return saved ? (saved as HierarchyDisplayMode) : HierarchyDisplayMode.DOUBLE_LINE;
    } catch {
      return HierarchyDisplayMode.DOUBLE_LINE;
    }
  });

  const [feeCategoryMemory, setFeeCategoryMemory] = useState<Record<string, number>>(() => {
    try {
      const saved = localStorage.getItem(STORAGE_KEYS.FEE_MEMORY);
      return saved ? JSON.parse(saved) : { default: 14 }; // 14 = Transfer Charges
    } catch {
      return { default: 14 };
    }
  });

  // Sync to localStorage
  useEffect(() => {
    localStorage.setItem(STORAGE_KEYS.HIERARCHY_DISPLAY, hierarchyDisplayMode);
  }, [hierarchyDisplayMode]);

  useEffect(() => {
    localStorage.setItem(STORAGE_KEYS.FEE_MEMORY, JSON.stringify(feeCategoryMemory));
  }, [feeCategoryMemory]);

  // Sync to localStorage
  useEffect(() => {
    localStorage.setItem(STORAGE_KEYS.ACCOUNTS, JSON.stringify(accounts));
  }, [accounts]);

  useEffect(() => {
    localStorage.setItem(STORAGE_KEYS.CATEGORIES, JSON.stringify(categories));
  }, [categories]);

  useEffect(() => {
    localStorage.setItem(STORAGE_KEYS.TRANSACTIONS, JSON.stringify(transactions));
  }, [transactions]);

  useEffect(() => {
    localStorage.setItem(STORAGE_KEYS.MONTHLY_BUDGETS, JSON.stringify(monthlyBudgets));
  }, [monthlyBudgets]);

  useEffect(() => {
    localStorage.setItem(STORAGE_KEYS.RECURRING_BILLS, JSON.stringify(recurringBills));
  }, [recurringBills]);

  useEffect(() => {
    localStorage.setItem(STORAGE_KEYS.BUDGET_ADJUSTMENTS, JSON.stringify(budgetAdjustments));
  }, [budgetAdjustments]);

  useEffect(() => {
    localStorage.setItem(STORAGE_KEYS.CATEGORY_ALLOCATIONS, JSON.stringify(categoryAllocations));
  }, [categoryAllocations]);

  useEffect(() => {
    LanguageHelper.updateCurrencyConfig(currencyConfig);
    localStorage.setItem(STORAGE_KEYS.CURRENCY, JSON.stringify(currencyConfig));
  }, [currencyConfig]);

  useEffect(() => {
    localStorage.setItem(STORAGE_KEYS.LANGUAGE, languageMode);
  }, [languageMode]);

  useEffect(() => {
    localStorage.setItem(STORAGE_KEYS.TABS, JSON.stringify(tabConfig));
  }, [tabConfig]);

  useEffect(() => {
    localStorage.setItem(STORAGE_KEYS.DASHBOARD, JSON.stringify(dashboardConfig));
  }, [dashboardConfig]);

  useEffect(() => {
    localStorage.setItem(STORAGE_KEYS.AUTOFILL, JSON.stringify(autofillConfig));
  }, [autofillConfig]);

  useEffect(() => {
    localStorage.setItem(STORAGE_KEYS.DEMO_MODE, isDemoMode ? 'true' : 'false');
  }, [isDemoMode]);

  // Derived Calculations
  const { accountBalances, overview: financialOverview } = useMemo(() => {
    return BalanceSheetHelper.calculateBalances(
      accounts,
      transactions,
      selectedYear,
      selectedMonth,
      categories,
      monthlyBudgets,
      budgetAdjustments
    );
  }, [accounts, transactions, selectedYear, selectedMonth, categories, monthlyBudgets, budgetAdjustments]);

  const accountsWithBalances: AccountWithBalance[] = useMemo(() => {
    return accounts.map((account) => {
      const curBal = accountBalances.get(account.id) || 0;
      const calcBal = account.isCalculated ? curBal + (account.calculationAdjustment || 0) : 0;

      // Calculate monthly in/out
      let inflow = 0;
      let outflow = 0;
      transactions.forEach((t) => {
        const d = new Date(t.dateEpochMs);
        if (d.getFullYear() === selectedYear && d.getMonth() + 1 === selectedMonth) {
          if (t.debitAccountId === account.id) inflow += t.amount;
          if (t.creditAccountId === account.id) outflow += t.amount;
        }
      });

      return {
        account,
        currentBalance: curBal,
        calculatedBalance: calcBal,
        monthlyInflow: inflow,
        monthlyOutflow: outflow,
      };
    });
  }, [accounts, accountBalances, transactions, selectedYear, selectedMonth]);

  const categorySpending = useMemo(() => {
    return BalanceSheetHelper.getCategorySpending(categories, transactions, selectedYear, selectedMonth);
  }, [categories, transactions, selectedYear, selectedMonth]);

  const paymentSourceAnalysis = useMemo(() => {
    return PaymentSourceCalculator.calculateOverview(
      accounts,
      accountBalances,
      categories,
      monthlyBudgets,
      transactions,
      recurringBills,
      selectedYear,
      selectedMonth,
      calculationBasis,
      categoryAllocations
    );
  }, [
    accounts,
    accountBalances,
    categories,
    monthlyBudgets,
    transactions,
    recurringBills,
    selectedYear,
    selectedMonth,
    calculationBasis,
    categoryAllocations,
  ]);

  // Actions
  const setLanguageMode = useCallback((mode: LanguageMode) => {
    setLanguageModeState(mode);
  }, []);

  const setCurrencyConfig = useCallback((config: CurrencyConfig) => {
    setCurrencyConfigState(config);
  }, []);

  const setTabConfig = useCallback((config: NavigationTabConfig) => {
    setTabConfigState(config);
  }, []);

  const setDashboardConfig = useCallback((config: DashboardConfig) => {
    setDashboardConfigState(config);
  }, []);

  const setAutofillConfig = useCallback((config: AutofillConfig) => {
    setAutofillConfigState(config);
  }, []);

  const setHierarchyDisplayMode = useCallback((mode: HierarchyDisplayMode) => {
    setHierarchyDisplayModeState(mode);
  }, []);

  const saveFeeCategoryPreference = useCallback((key: string, categoryId: number) => {
    setFeeCategoryMemory((prev) => ({
      ...prev,
      [key.toLowerCase().trim()]: categoryId,
      default: categoryId,
    }));
  }, []);

  const getRememberedFeeCategoryId = useCallback(
    (key?: string): number | null => {
      if (key && key.trim()) {
        const cleaned = key.toLowerCase().trim();
        if (feeCategoryMemory[cleaned]) {
          return feeCategoryMemory[cleaned];
        }
      }
      return feeCategoryMemory['default'] || 14; // Default to Transfer Charges
    },
    [feeCategoryMemory]
  );

  // Pre-calculate running balances for fast ledger queries
  const transactionAccountRunningMap = useMemo(() => {
    const map = new Map<string, number>();
    const balances = new Map<number, number>();
    accounts.forEach((acc) => balances.set(acc.id, acc.initialBalance));

    // Sort chronologically ascending to compute chronological running balance
    const sorted = [...transactions].sort((a, b) => a.dateEpochMs - b.dateEpochMs || a.id - b.id);
    for (const tx of sorted) {
      if (tx.creditAccountId) {
        const prev = balances.get(tx.creditAccountId) || 0;
        const next = prev - tx.amount;
        balances.set(tx.creditAccountId, next);
        map.set(`${tx.id}_${tx.creditAccountId}`, next);
      }
      if (tx.debitAccountId) {
        const prev = balances.get(tx.debitAccountId) || 0;
        const next = prev + tx.amount;
        balances.set(tx.debitAccountId, next);
        map.set(`${tx.id}_${tx.debitAccountId}`, next);
      }
    }
    return map;
  }, [accounts, transactions]);

  const getTransactionAccountRunningBalance = useCallback(
    (txId: number, accountId: number): number => {
      const val = transactionAccountRunningMap.get(`${txId}_${accountId}`);
      if (val !== undefined) return val;
      return accountBalances.get(accountId) || 0;
    },
    [transactionAccountRunningMap, accountBalances]
  );

  // Transfer with optional fee execution
  const addTransferWithFee = useCallback(
    (
      transferTxData: Omit<Transaction, 'id'>,
      feeAmount?: number,
      feeAccountId?: number | null,
      feeCategoryId?: number | null
    ): { transfer: Transaction; fee?: Transaction } => {
      const transferId = Date.now() + Math.floor(Math.random() * 1000);
      const newTransfer: Transaction = {
        ...transferTxData,
        id: transferId,
        feeAmount: feeAmount || 0,
        feeAccountId: feeAccountId || transferTxData.creditAccountId,
        feeCategoryId: feeCategoryId || null,
      };

      let newFeeTx: Transaction | undefined = undefined;
      const newTxList = [newTransfer];

      if (feeAmount && feeAmount > 0) {
        const feeId = transferId + 1;
        const feeSourceAcc = feeAccountId || transferTxData.creditAccountId;
        newFeeTx = {
          id: feeId,
          type: TransactionType.EXPENSE,
          amount: feeAmount,
          dateEpochMs: transferTxData.dateEpochMs,
          creditAccountId: feeSourceAcc,
          debitAccountId: null,
          categoryId: feeCategoryId || getRememberedFeeCategoryId() || 14,
          note: `Transfer Fee for ${transferTxData.payeePayer || 'Transfer'}`,
          payeePayer: `${transferTxData.payeePayer || 'Transfer'}(#)`,
          status: transferTxData.status,
          labelIds: transferTxData.labelIds || [],
          tags: ['fee', 'transfer-charges'],
          isTransferFee: true,
          linkedTransferId: transferId,
        };
        newTxList.unshift(newFeeTx); // Fee entry appears right beside transfer

        // Remember fee category preference
        if (feeCategoryId) {
          if (transferTxData.payeePayer) {
            saveFeeCategoryPreference(transferTxData.payeePayer, feeCategoryId);
          }
          saveFeeCategoryPreference('default', feeCategoryId);
        }
      }

      setTransactions((prev) => [...newTxList, ...prev]);
      return { transfer: newTransfer, fee: newFeeTx };
    },
    [getRememberedFeeCategoryId, saveFeeCategoryPreference]
  );

  const toggleDemoMode = useCallback(() => {
    setIsDemoMode((prev) => !prev);
  }, []);

  const prevMonth = useCallback(() => {
    setSelectedMonth((prev) => {
      if (prev === 1) {
        setSelectedYear((y) => y - 1);
        return 12;
      }
      return prev - 1;
    });
  }, []);

  const nextMonth = useCallback(() => {
    setSelectedMonth((prev) => {
      if (prev === 12) {
        setSelectedYear((y) => y + 1);
        return 1;
      }
      return prev + 1;
    });
  }, []);

  const setCurrentMonth = useCallback(() => {
    const d = new Date();
    setSelectedYear(d.getFullYear());
    setSelectedMonth(d.getMonth() + 1);
  }, []);

  // Transactions CRUD
  const addTransaction = useCallback((txData: Omit<Transaction, 'id'>): Transaction => {
    const newTx: Transaction = {
      ...txData,
      id: Date.now() + Math.floor(Math.random() * 1000),
    };
    setTransactions((prev) => [newTx, ...prev]);
    return newTx;
  }, []);

  const updateTransaction = useCallback((tx: Transaction) => {
    setTransactions((prev) => prev.map((item) => (item.id === tx.id ? tx : item)));
  }, []);

  const deleteTransaction = useCallback((id: number) => {
    setTransactions((prev) => prev.filter((item) => item.id !== id));
  }, []);

  // Accounts CRUD
  const addAccount = useCallback((accData: Omit<Account, 'id'>): Account => {
    const newAcc: Account = {
      ...accData,
      id: Date.now() + Math.floor(Math.random() * 1000),
    };
    setAccounts((prev) => [...prev, newAcc]);
    return newAcc;
  }, []);

  const updateAccount = useCallback((acc: Account) => {
    setAccounts((prev) => prev.map((item) => (item.id === acc.id ? acc : item)));
  }, []);

  const deleteAccount = useCallback((id: number) => {
    setAccounts((prev) => prev.filter((item) => item.id !== id));
  }, []);

  const toggleAccountCalculation = useCallback((id: number, isCalculated: boolean) => {
    setAccounts((prev) =>
      prev.map((item) => (item.id === id ? { ...item, isCalculated } : item))
    );
  }, []);

  const updateAccountAdjustment = useCallback((id: number, adjustment: number) => {
    setAccounts((prev) =>
      prev.map((item) => (item.id === id ? { ...item, calculationAdjustment: adjustment } : item))
    );
  }, []);

  // Categories CRUD
  const addCategory = useCallback((catData: Omit<Category, 'id'>): Category => {
    const newCat: Category = {
      ...catData,
      id: Date.now() + Math.floor(Math.random() * 1000),
    };
    setCategories((prev) => [...prev, newCat]);
    return newCat;
  }, []);

  const updateCategory = useCallback((cat: Category) => {
    setCategories((prev) => prev.map((item) => (item.id === cat.id ? cat : item)));
  }, []);

  const deleteCategory = useCallback((id: number) => {
    setCategories((prev) => prev.filter((item) => item.id !== id));
  }, []);

  // Monthly Budgets
  const setMonthlyBudget = useCallback(
    (categoryId: number, amount: number, year?: number, month?: number) => {
      const y = year ?? selectedYear;
      const m = month ?? selectedMonth;

      setMonthlyBudgets((prev) => {
        const existingIdx = prev.findIndex(
          (b) => b.categoryId === categoryId && b.year === y && b.month === m
        );
        if (existingIdx !== -1) {
          const copy = [...prev];
          copy[existingIdx] = { ...copy[existingIdx], budgetAmount: amount };
          return copy;
        } else {
          return [
            ...prev,
            {
              id: Date.now() + Math.floor(Math.random() * 1000),
              categoryId,
              year: y,
              month: m,
              budgetAmount: amount,
              isRolloverEnabled: true,
            },
          ];
        }
      });
    },
    [selectedYear, selectedMonth]
  );

  const copyBudgetsFromPrevMonth = useCallback(
    (sourceYear: number, sourceMonth: number, targetYear: number, targetMonth: number) => {
      const sourceBudgets = monthlyBudgets.filter(
        (b) => b.year === sourceYear && b.month === sourceMonth
      );

      setMonthlyBudgets((prev) => {
        const filtered = prev.filter(
          (b) => !(b.year === targetYear && b.month === targetMonth)
        );
        const copied = sourceBudgets.map((b) => ({
          ...b,
          id: Date.now() + Math.floor(Math.random() * 10000),
          year: targetYear,
          month: targetMonth,
        }));
        return [...filtered, ...copied];
      });
    },
    [monthlyBudgets]
  );

  const saveCategoryAllocations = useCallback(
    (categoryId: number, allocations: { accountId: number; amount: number }[]) => {
      setCategoryAllocations((prev) => {
        const otherAllocs = prev.filter(
          (a) =>
            !(
              a.categoryId === categoryId &&
              a.year === selectedYear &&
              a.month === selectedMonth
            )
        );
        const newAllocs: AccountCategoryAllocation[] = allocations.map((alloc) => ({
          id: Date.now() + Math.floor(Math.random() * 10000),
          categoryId,
          accountId: alloc.accountId,
          allocatedAmount: alloc.amount,
          year: selectedYear,
          month: selectedMonth,
        }));
        return [...otherAllocs, ...newAllocs];
      });
    },
    [selectedYear, selectedMonth]
  );

  // Recurring Bills CRUD
  const addRecurringBill = useCallback((billData: Omit<RecurringBill, 'id'>): RecurringBill => {
    const newBill: RecurringBill = {
      ...billData,
      id: Date.now() + Math.floor(Math.random() * 1000),
    };
    setRecurringBills((prev) => [...prev, newBill]);
    return newBill;
  }, []);

  const updateRecurringBill = useCallback((bill: RecurringBill) => {
    setRecurringBills((prev) => prev.map((b) => (b.id === bill.id ? bill : b)));
  }, []);

  const deleteRecurringBill = useCallback((id: number) => {
    setRecurringBills((prev) => prev.filter((b) => b.id !== id));
  }, []);

  const markBillAsPaid = useCallback(
    (billId: number) => {
      const bill = recurringBills.find((b) => b.id === billId);
      if (!bill) return;

      // Add double-entry expense transaction
      addTransaction({
        type: TransactionType.EXPENSE,
        amount: bill.amount,
        dateEpochMs: Date.now(),
        creditAccountId: bill.accountId,
        debitAccountId: null,
        categoryId: bill.categoryId,
        note: `Recurring Bill Paid: ${bill.title}`,
        payeePayer: bill.title,
        status: TransactionStatus.CLEARED,
        labelIds: [],
        tags: ['recurring_bill'],
      });

      // Advance next due date by 1 month
      const curDate = new Date(bill.nextDueDateEpochMs);
      curDate.setMonth(curDate.getMonth() + 1);

      updateRecurringBill({
        ...bill,
        nextDueDateEpochMs: curDate.getTime(),
      });
    },
    [recurringBills, addTransaction, updateRecurringBill]
  );

  // Budget Adjustments
  const addBudgetAdjustment = useCallback(
    (adjData: Omit<BudgetAdjustment, 'id' | 'timestampEpochMs'>) => {
      const newAdj: BudgetAdjustment = {
        ...adjData,
        id: Date.now(),
        timestampEpochMs: Date.now(),
      };
      setBudgetAdjustments((prev) => [newAdj, ...prev]);
    },
    []
  );

  const resetBudgetAdjustments = useCallback(() => {
    setBudgetAdjustments([]);
  }, []);

  // Quick Transfer
  const executeTransfer = useCallback(
    (fromAccountId: number, toAccountId: number, amount: number, note = 'Fund Transfer') => {
      addTransaction({
        type: TransactionType.TRANSFER,
        amount,
        dateEpochMs: Date.now(),
        creditAccountId: fromAccountId,
        debitAccountId: toAccountId,
        categoryId: null,
        note,
        payeePayer: 'Account Transfer',
        status: TransactionStatus.CLEARED,
        labelIds: [],
        tags: ['transfer'],
      });
    },
    [addTransaction]
  );

  // Export / Import / Reset
  const exportDataJson = useCallback(() => {
    const data = {
      version: '1.0',
      exportDate: new Date().toISOString(),
      accounts,
      categories,
      transactions,
      monthlyBudgets,
      recurringBills,
      budgetAdjustments,
      categoryAllocations,
    };
    return JSON.stringify(data, null, 2);
  }, [
    accounts,
    categories,
    transactions,
    monthlyBudgets,
    recurringBills,
    budgetAdjustments,
    categoryAllocations,
  ]);

  const importDataJson = useCallback((jsonStr: string): boolean => {
    try {
      const data = JSON.parse(jsonStr);
      if (data.accounts && Array.isArray(data.accounts)) setAccounts(data.accounts);
      if (data.categories && Array.isArray(data.categories)) setCategories(data.categories);
      if (data.transactions && Array.isArray(data.transactions)) setTransactions(data.transactions);
      if (data.monthlyBudgets && Array.isArray(data.monthlyBudgets)) setMonthlyBudgets(data.monthlyBudgets);
      if (data.recurringBills && Array.isArray(data.recurringBills)) setRecurringBills(data.recurringBills);
      return true;
    } catch (e) {
      console.error('Import failed', e);
      return false;
    }
  }, []);

  const resetToDefaults = useCallback(() => {
    setAccounts(DEFAULT_ACCOUNTS);
    setCategories(DEFAULT_CATEGORIES);
    setTransactions(DEFAULT_TRANSACTIONS);
    setMonthlyBudgets(DEFAULT_MONTHLY_BUDGETS);
    setRecurringBills(DEFAULT_RECURRING_BILLS);
    setBudgetAdjustments([]);
    setCategoryAllocations([]);
  }, []);

  return (
    <BudgetContext.Provider
      value={{
        accounts,
        categories,
        transactions,
        monthlyBudgets,
        recurringBills,
        budgetAdjustments,
        categoryAllocations,
        languageMode,
        currencyConfig,
        tabConfig,
        dashboardConfig,
        autofillConfig,
        hierarchyDisplayMode,
        feeCategoryMemory,
        currentTab,
        isDemoMode,
        selectedYear,
        selectedMonth,
        accountBalances,
        financialOverview,
        accountsWithBalances,
        categorySpending,
        paymentSourceAnalysis,
        calculationBasis,
        setCurrentTab,
        setSelectedYear,
        setSelectedMonth,
        prevMonth,
        nextMonth,
        setCurrentMonth,
        setCalculationBasis,
        setLanguageMode,
        setCurrencyConfig,
        setTabConfig,
        setDashboardConfig,
        setAutofillConfig,
        setHierarchyDisplayMode,
        saveFeeCategoryPreference,
        getRememberedFeeCategoryId,
        toggleDemoMode,
        getTransactionAccountRunningBalance,
        addTransaction,
        addTransferWithFee,
        updateTransaction,
        deleteTransaction,
        addAccount,
        updateAccount,
        deleteAccount,
        toggleAccountCalculation,
        updateAccountAdjustment,
        addCategory,
        updateCategory,
        deleteCategory,
        setMonthlyBudget,
        copyBudgetsFromPrevMonth,
        saveCategoryAllocations,
        addRecurringBill,
        updateRecurringBill,
        deleteRecurringBill,
        markBillAsPaid,
        addBudgetAdjustment,
        resetBudgetAdjustments,
        executeTransfer,
        exportDataJson,
        importDataJson,
        resetToDefaults,
      }}
    >
      {children}
    </BudgetContext.Provider>
  );
};

export const useBudget = (): BudgetContextType => {
  const context = useContext(BudgetContext);
  if (!context) {
    throw new Error('useBudget must be used within a BudgetProvider');
  }
  return context;
};
