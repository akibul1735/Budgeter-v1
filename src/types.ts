export enum LanguageMode {
  ENGLISH = 'ENGLISH',
  BANGLA = 'BANGLA',
  BILINGUAL = 'BILINGUAL',
}

export enum CurrencyDisplayMode {
  SYMBOL_ONLY = 'SYMBOL_ONLY',
  CODE_ONLY = 'CODE_ONLY',
  CODE_AND_SYMBOL = 'CODE_AND_SYMBOL',
  NONE = 'NONE',
}

export interface CurrencyItem {
  code: string;
  symbol: string;
  nameEn: string;
  nameBn: string;
}

export interface CurrencyConfig {
  selectedCode: string;
  selectedSymbol: string;
  displayMode: CurrencyDisplayMode;
  customSymbol: string;
  customCode: string;
}

export enum AccountType {
  ASSET = 'ASSET',
  LIABILITY = 'LIABILITY',
  EQUITY = 'EQUITY',
}

export interface Account {
  id: number;
  nameEn: string;
  nameBn: string;
  type: AccountType;
  initialBalance: number;
  iconName: string;
  colorHex: string;
  isArchived: boolean;
  sortOrder: number;
  parentId: number | null;
  accountRole?: string;
  isCalculated: boolean;
  calculationAdjustment: number;
}

export enum CategoryType {
  EXPENSE = 'EXPENSE',
  INCOME = 'INCOME',
}

export interface Category {
  id: number;
  nameEn: string;
  nameBn: string;
  type: CategoryType;
  iconName: string;
  colorHex: string;
  monthlyBudget: number;
  sortOrder: number;
  parentId: number | null;
  isTaxDeductible: boolean;
  defaultAccountId: number | null;
  isCalculated: boolean;
  isAssigned?: boolean;
}

export enum TransactionType {
  EXPENSE = 'EXPENSE',
  INCOME = 'INCOME',
  TRANSFER = 'TRANSFER',
}

export enum TransactionStatus {
  CLEARED = 'CLEARED',
  UNCLEARED = 'UNCLEARED',
  VOID = 'VOID',
  RECONCILED = 'RECONCILED',
}

export interface Transaction {
  id: number;
  type: TransactionType;
  amount: number;
  dateEpochMs: number;
  creditAccountId: number | null; // Account money goes OUT of (Source in Expense / Transfer)
  debitAccountId: number | null;  // Account money goes INTO (Destination in Income / Transfer)
  categoryId: number | null;      // Required for Expense / Income
  note: string;
  payeePayer: string;
  status: TransactionStatus;
  labelIds: number[];
  tags: string[];
  isCalculationAdjusted?: boolean;
  attachmentUri?: string;
}

export interface TransactionWithDetails {
  transaction: Transaction;
  category?: Category;
  creditAccount?: Account;
  debitAccount?: Account;
}

export interface MonthlyBudget {
  id: number;
  categoryId: number;
  year: number;
  month: number; // 1-12
  budgetAmount: number;
  isRolloverEnabled: boolean;
}

export enum BillFrequency {
  WEEKLY = 'WEEKLY',
  BI_WEEKLY = 'BI_WEEKLY',
  MONTHLY = 'MONTHLY',
  QUARTERLY = 'QUARTERLY',
  YEARLY = 'YEARLY',
}

export interface RecurringBill {
  id: number;
  title: string;
  amount: number;
  frequency: BillFrequency;
  nextDueDateEpochMs: number;
  categoryId: number;
  accountId: number;
  isAutoPaid: boolean;
  isActive: boolean;
  reminderDaysBefore: number;
}

export interface BudgetAdjustment {
  id: number;
  year: number;
  month: number;
  note: string;
  timestampEpochMs: number;
  netWorthAdjustment: number;
  committedExpensesAdjustment: number;
  availableFundAdjustment: number;
  potentialIncomeAdjustment: number;
}

export interface AccountCategoryAllocation {
  id: number;
  categoryId: number;
  accountId: number;
  allocatedAmount: number;
  year: number;
  month: number;
}

export enum AppTab {
  MAIN = 'MAIN',
  TRANSACTIONS = 'TRANSACTIONS',
  PAYMENT_SOURCE = 'PAYMENT_SOURCE',
  BALANCE_SHEET = 'BALANCE_SHEET',
  BUDGET = 'BUDGET',
  NET_EARNINGS = 'NET_EARNINGS',
  LABELS = 'LABELS',
  ITEMS_SUMMARY = 'ITEMS_SUMMARY',
  REMINDERS = 'REMINDERS',
  ACCOUNTS = 'ACCOUNTS',
  CATEGORIES = 'CATEGORIES',
  BUDGET_MAKER = 'BUDGET_MAKER',
  BACKUP_SYNC = 'BACKUP_SYNC',
  ACCOUNT_CALC = 'ACCOUNT_CALC',
  SETTINGS = 'SETTINGS',
}

export enum TabPosition {
  TOP = 'TOP',
  BOTTOM = 'BOTTOM',
}

export interface NavigationTabConfig {
  position: TabPosition;
  allTabsOrder: AppTab[];
  enabledTabs: AppTab[];
}

export interface FinancialOverview {
  totalAssets: number;
  totalLiabilities: number;
  netWorth: number;
  monthlyIncome: number;
  monthlyExpense: number;
  netIncome: number;
  isLedgerBalanced: boolean;
  totalDebits: number;
  totalCredits: number;
  expendableCash: number;
  committedBudget: number;
  expendable: number;
  expectedExpendable: number;
  availableMoney: number;
  totalExpenseBudget: number;
  additionalCost: number;
  potentialIncome: number;
}

export interface AccountWithBalance {
  account: Account;
  currentBalance: number;
  calculatedBalance: number;
  monthlyInflow: number;
  monthlyOutflow: number;
}

export interface CategorySpending {
  category: Category;
  spentAmount: number;
  budgetAmount: number;
  remainingAmount: number;
  percentageSpent: number;
  isOverBudget: boolean;
}

export enum RequirementCalculationBasis {
  BUDGET_AMOUNT = 'BUDGET_AMOUNT',
  REMAINING_AMOUNT = 'REMAINING_AMOUNT',
}

export interface AccountRequirementItem {
  account: Account;
  currentBalance: number;
  assignedBudgetExpense: number;
  spentActualExpense: number;
  expectedIncome: number;
  actualIncomeReceived: number;
  requiredAmount: number;
  availableSurplus: number;
  shortfall: number;
  assignedCategories: { category: Category; amount: number; spent: number }[];
}

export interface FundAllocationSuggestion {
  fromAccount: Account;
  toAccount: Account;
  suggestedAmount: number;
  reason: string;
}

export interface PaymentSourceAnalysisOverview {
  totalRequired: number;
  totalAvailable: number;
  netShortfall: number;
  netSurplus: number;
  accountsAnalysis: AccountRequirementItem[];
  suggestions: FundAllocationSuggestion[];
}

export enum DailySummaryMode {
  BAR_CHART = 'BAR_CHART',
  LIST = 'LIST',
  COMPACT = 'COMPACT',
}

export enum DailySummaryPeriod {
  LAST_7_DAYS = 'LAST_7_DAYS',
  LAST_14_DAYS = 'LAST_14_DAYS',
  THIS_MONTH = 'THIS_MONTH',
}

export enum BudgetChartShape {
  PIE = 'PIE',
  DONUT = 'DONUT',
  BARS = 'BARS',
}

export enum BudgetSummaryType {
  ALL_CATEGORIES = 'ALL_CATEGORIES',
  TOP_5_EXPENSES = 'TOP_5_EXPENSES',
  OVER_BUDGET_ONLY = 'OVER_BUDGET_ONLY',
}

export enum CalendarDisplayMode {
  GRID = 'GRID',
  AGENDA = 'AGENDA',
}

export enum DashboardCardType {
  DAILY_SUMMARY = 'DAILY_SUMMARY',
  BUDGET_SUMMARY = 'BUDGET_SUMMARY',
  FAVORITE_ACCOUNTS = 'FAVORITE_ACCOUNTS',
  CALENDAR_SUMMARY = 'CALENDAR_SUMMARY',
  RECENT_TRANSACTIONS = 'RECENT_TRANSACTIONS',
  PAYMENT_SOURCE_PREVIEW = 'PAYMENT_SOURCE_PREVIEW',
}

export interface DashboardConfig {
  cardsOrder: DashboardCardType[];
  enabledCards: DashboardCardType[];
  dailySummaryMode: DailySummaryMode;
  dailySummaryPeriod: DailySummaryPeriod;
  showValuesOnBars: boolean;
  showPeriodAverages: boolean;
  budgetChartShape: BudgetChartShape;
  budgetSummaryType: BudgetSummaryType;
  topCategoriesCount: number;
  showPercentages: boolean;
  showPaceMarker: boolean;
  calendarMode: CalendarDisplayMode;
  showIncomeBadges: boolean;
  showExpenseBadges: boolean;
  favoriteAccountIds: number[];
}

export interface AutofillConfig {
  autofillCategory: boolean;
  autofillAccount: boolean;
  autofillAmount: boolean;
  autofillNotes: boolean;
  autofillLabels: boolean;
}
