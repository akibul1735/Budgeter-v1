import { LanguageMode, CurrencyConfig, CurrencyDisplayMode, CurrencyItem } from '../types';

const bnDigits = ['০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯'];

export const POPULAR_CURRENCIES: CurrencyItem[] = [
  { code: 'BDT', symbol: '৳', nameEn: 'Bangladeshi Taka', nameBn: 'বাংলাদেশি টাকা' },
  { code: 'USD', symbol: '$', nameEn: 'US Dollar', nameBn: 'মার্কিন ডলার' },
  { code: 'EUR', symbol: '€', nameEn: 'Euro', nameBn: 'ইউরো' },
  { code: 'GBP', symbol: '£', nameEn: 'British Pound', nameBn: 'ব্রিটিশ পাউন্ড' },
  { code: 'INR', symbol: '₹', nameEn: 'Indian Rupee', nameBn: 'ভারতীয় রুপি' },
  { code: 'CAD', symbol: 'C$', nameEn: 'Canadian Dollar', nameBn: 'কানাডিয়ান ডলার' },
  { code: 'AUD', symbol: 'A$', nameEn: 'Australian Dollar', nameBn: 'অস্ট্রেলিয়ান ডলার' },
  { code: 'SAR', symbol: '﷼', nameEn: 'Saudi Riyal', nameBn: 'সৌদি রিয়াল' },
  { code: 'AED', symbol: 'د.إ', nameEn: 'UAE Dirham', nameBn: 'ইউএই দিরহাম' },
  { code: 'JPY', symbol: '¥', nameEn: 'Japanese Yen', nameBn: 'জাপানি ইয়েন' },
  { code: 'MYR', symbol: 'RM', nameEn: 'Malaysian Ringgit', nameBn: 'মালয়েশিয়ান রিঙ্গিত' },
  { code: 'SGD', symbol: 'S$', nameEn: 'Singapore Dollar', nameBn: 'সিঙ্গাপুর ডলার' },
];

export class LanguageHelper {
  private static activeCurrencyConfig: CurrencyConfig = {
    selectedCode: 'BDT',
    selectedSymbol: '৳',
    displayMode: CurrencyDisplayMode.SYMBOL_ONLY,
    customSymbol: '',
    customCode: '',
  };

  public static updateCurrencyConfig(config: CurrencyConfig): void {
    this.activeCurrencyConfig = config;
  }

  public static getActiveCurrencyConfig(): CurrencyConfig {
    return this.activeCurrencyConfig;
  }

  public static toBanglaDigits(input: string | number): string {
    const str = String(input);
    return str.replace(/[0-9]/g, (digit) => bnDigits[parseInt(digit, 10)]);
  }

  public static toEnglishDigits(input: string): string {
    return input.replace(/[০-৯]/g, (digit) => {
      const idx = bnDigits.indexOf(digit);
      return idx !== -1 ? String(idx) : digit;
    });
  }

  public static formatNumber(value: number, mode: LanguageMode, includeDecimals = true): string {
    const absVal = Math.abs(value);
    const formatted = absVal.toLocaleString('en-US', {
      minimumFractionDigits: includeDecimals ? 2 : 0,
      maximumFractionDigits: includeDecimals ? 2 : 0,
    });
    return mode === LanguageMode.BANGLA ? this.toBanglaDigits(formatted) : formatted;
  }

  public static formatCurrency(
    amount: number,
    mode: LanguageMode,
    overrideConfig?: Partial<CurrencyConfig>
  ): string {
    const config = { ...this.activeCurrencyConfig, ...overrideConfig };
    const symbol = config.customSymbol || config.selectedSymbol;
    const code = config.customCode || config.selectedCode;
    const displayMode = config.displayMode;

    const formattedNum = this.formatNumber(Math.abs(amount), mode);
    const sign = amount < 0 ? '-' : '';

    let prefix = '';
    switch (displayMode) {
      case CurrencyDisplayMode.SYMBOL_ONLY:
        prefix = symbol ? `${symbol} ` : '';
        break;
      case CurrencyDisplayMode.CODE_ONLY:
        prefix = `${code} `;
        break;
      case CurrencyDisplayMode.CODE_AND_SYMBOL:
        prefix = `${code} ${symbol} `;
        break;
      case CurrencyDisplayMode.NONE:
        prefix = '';
        break;
    }

    return `${sign}${prefix}${formattedNum}`;
  }

  public static getString(key: string, mode: LanguageMode): string {
    const entry = stringMap[key];
    if (!entry) return key;

    switch (mode) {
      case LanguageMode.ENGLISH:
        return entry.en;
      case LanguageMode.BANGLA:
        return entry.bn;
      case LanguageMode.BILINGUAL:
        return `${entry.en} / ${entry.bn}`;
    }
  }

  public static getLocalizedName(nameEn: string, nameBn: string, mode: LanguageMode): string {
    switch (mode) {
      case LanguageMode.ENGLISH:
        return nameEn;
      case LanguageMode.BANGLA:
        return nameBn || nameEn;
      case LanguageMode.BILINGUAL:
        return nameBn ? `${nameEn} / ${nameBn}` : nameEn;
    }
  }
}

interface TransEntry {
  en: string;
  bn: string;
}

const stringMap: Record<string, TransEntry> = {
  app_name: { en: 'Budgeter', bn: 'বাজেটার' },
  double_entry_bookkeeping: { en: 'Double-Entry Bookkeeping', bn: 'দ্বৈত দাখিলা হিসাব' },
  dashboard: { en: 'Dashboard', bn: 'ড্যাশবোর্ড' },
  accounts: { en: 'Accounts', bn: 'হিসাবসমূহ' },
  categories: { en: 'Categories', bn: 'ক্যাটাগরি' },
  ledger: { en: 'Ledger', bn: 'খতিয়ান' },
  reports: { en: 'Reports', bn: 'প্রতিবেদন' },
  net_worth: { en: 'Net Worth', bn: 'মোট সম্পদ' },
  total_assets: { en: 'Total Assets', bn: 'মোট পরিসম্পদ' },
  total_liabilities: { en: 'Total Liabilities', bn: 'মোট দায়' },
  income: { en: 'Income', bn: 'আয়' },
  expense: { en: 'Expense', bn: 'ব্যয়' },
  transfer: { en: 'Transfer', bn: 'স্থানান্তর' },
  expenses: { en: 'Expenses', bn: 'ব্যয়সমূহ' },
  incomes: { en: 'Incomes', bn: 'আয়সমূহ' },
  assets: { en: 'Assets', bn: 'সম্পদ' },
  liabilities: { en: 'Liabilities', bn: 'দায় ও ঋণ' },
  equity: { en: 'Equity', bn: 'মূলধন / ইকুইটি' },
  sub_accounts: { en: 'Sub-Accounts', bn: 'উপ-হিসাবসমূহ' },
  sub_categories: { en: 'Sub-Categories', bn: 'উপ-ক্যাটাগরি' },
  add_transaction: { en: 'Add Transaction', bn: 'লেনদেন যোগ করুন' },
  edit_transaction: { en: 'Edit Transaction', bn: 'লেনদেন সম্পাদনা' },
  add_account: { en: 'Add Account', bn: 'হিসাব যোগ করুন' },
  add_category: { en: 'Add Category', bn: 'ক্যাটাগরি যোগ করুন' },
  add_sub_account: { en: 'Add Sub-Account', bn: 'উপ-হিসাব যোগ করুন' },
  add_sub_category: { en: 'Add Sub-Category', bn: 'উপ-ক্যাটাগরি যোগ করুন' },
  debit_account: { en: 'Debit Account (Inflow/Asset)', bn: 'ডেবিট হিসাব (বৃদ্ধি/সম্পদ)' },
  credit_account: { en: 'Credit Account (Outflow/Payment)', bn: 'ক্রেডিট হিসাব (হ্রাস/পরিশোধ)' },
  source_account: { en: 'From Account (Source)', bn: 'উৎস হিসাব (হতে)' },
  destination_account: { en: 'To Account (Destination)', bn: 'গন্তব্য হিসাব (এ)' },
  amount: { en: 'Amount', bn: 'পরিমাণ' },
  date: { en: 'Date', bn: 'তারিখ' },
  notes: { en: 'Notes / Memo', bn: 'নোট / বিবরণ' },
  payee_payer: { en: 'Payee / Payer', bn: 'প্রাপক / প্রদানকারী' },
  calculator: { en: 'Calculator', bn: 'ক্যালকুলেটর' },
  quick_calc: { en: 'Quick Calculator', bn: 'দ্রুত ক্যালকুলেটর' },
  done: { en: 'Done', bn: 'সম্পন্ন' },
  cancel: { en: 'Cancel', bn: 'বাতিল' },
  save: { en: 'Save', bn: 'সংরক্ষণ' },
  delete: { en: 'Delete', bn: 'মুছুন' },
  edit: { en: 'Edit', bn: 'সম্পাদনা' },
  filter: { en: 'Filter', bn: 'ফিল্টার' },
  search: { en: 'Search', bn: 'অনুসন্ধান' },
  recent_transactions: { en: 'Recent Transactions', bn: 'সাম্প্রতিক লেনদেন' },
  all_transactions: { en: 'All Transactions', bn: 'সকল লেনদেন' },
  trial_balance: { en: 'Trial Balance', bn: 'রেওয়ামিল (Trial Balance)' },
  balance_sheet: { en: 'Balance Sheet', bn: 'উদ্বৃত্ত পত্র (Balance Sheet)' },
  income_statement: { en: 'Income & Expense Statement', bn: 'আয়-ব্যয় বিবরণী' },
  debit: { en: 'Debit (Dr)', bn: 'ডেবিট (Dr)' },
  credit: { en: 'Credit (Cr)', bn: 'ক্রেডিট (Cr)' },
  balance: { en: 'Balance', bn: 'জের / ব্যালেন্স' },
  balanced_ledger: { en: 'Double-Entry Balanced', bn: 'দ্বৈত দাখিলা সমন্বিত' },
  unbalanced: { en: 'Unbalanced', bn: 'অসমন্বিত' },
  today: { en: 'Today', bn: 'আজ' },
  yesterday: { en: 'Yesterday', bn: 'গতকাল' },
  this_month: { en: 'This Month', bn: 'চলতি মাস' },
  all_time: { en: 'All Time', bn: 'সর্বকাল' },
  select_category: { en: 'Select Category', bn: 'ক্যাটাগরি নির্বাচন করুন' },
  select_account: { en: 'Select Account', bn: 'হিসাব নির্বাচন করুন' },
  parent_account: { en: 'Parent Account', bn: 'মূল হিসাব' },
  parent_category: { en: 'Parent Category', bn: 'মূল ক্যাটাগরি' },
  name_en: { en: 'Name (English)', bn: 'নাম (ইংরেজি)' },
  name_bn: { en: 'Name (Bangla)', bn: 'নাম (বাংলা)' },
  initial_balance: { en: 'Opening Balance', bn: 'প্রারম্ভিক ব্যালেন্স' },
  budget_limit: { en: 'Monthly Budget Limit', bn: 'মাসিক বাজেট সীমা' },
  cashflow: { en: 'Cash Flow', bn: 'নগদ প্রবাহ' },
  financial_summary: { en: 'Financial Summary', bn: 'আর্থিক সারসংক্ষেপ' },
  no_transactions: { en: 'No transactions recorded yet.', bn: 'এখনো কোনো লেনদেন যুক্ত করা হয়নি।' },
  no_accounts: { en: 'No accounts found.', bn: 'কোনো হিসাব পাওয়া যায়নি।' },
  no_categories: { en: 'No categories found.', bn: 'কোনো ক্যাটাগরি পাওয়া যায়নি।' },
  transactions: { en: 'Transactions', bn: 'লেনদেনসমূহ' },
  main: { en: 'Main', bn: 'প্রধান' },
  budget: { en: 'Budget', bn: 'বাজেট' },
  budget_maker: { en: 'Budget Maker', bn: 'বাজেট মেকার' },
  categories_and_budget: { en: 'Categories & Budget', bn: 'ক্যাটাগরি ও বাজেট' },
  timeline: { en: 'Timeline', bn: 'টাইমলাইন' },
  over: { en: 'over', bn: 'অতিরিক্ত' },
  left_from: { en: 'left from', bn: 'অবশিষ্ট' },
  frequency_weekly: { en: 'Weekly', bn: 'সাপ্তাহিক' },
  frequency_bi_weekly: { en: 'Bi-weekly', bn: 'দ্বি-সাপ্তাহিক' },
  frequency_monthly: { en: 'Monthly', bn: 'মাসিক' },
  frequency_quarterly: { en: 'Quarterly', bn: 'ত্রৈমাসিক' },
  frequency_yearly: { en: 'Yearly', bn: 'বাৎসরিক' },
  prev_month: { en: 'Prev Month', bn: 'গত মাস' },
  frequent: { en: 'Frequent', bn: 'প্রচলিত' },
  average_3m: { en: '3-Mo Avg', bn: '৩ মাসের গড়' },
  split: { en: 'Split', bn: 'বিভাজন' },
  status: { en: 'Status', bn: 'অবস্থা' },
  none: { en: 'None', bn: 'কোনটি নয়' },
  label: { en: 'Label', bn: 'লেবেল' },
  schedule: { en: 'Schedule', bn: 'সময়সূচি' },
  cleared: { en: 'Cleared', bn: 'সম্পন্ন' },
  void: { en: 'Void', bn: 'বাতিল' },
  uncleared: { en: 'Uncleared', bn: 'অসম্পন্ন' },
  reconciled: { en: 'Reconciled', bn: 'মিলকরণ' },
  export_csv: { en: 'Export CSV', bn: 'CSV এক্সপোর্ট' },
  filter_transactions: { en: 'Filter Transactions', bn: 'লেনদেন ফিল্টার' },
  all: { en: 'All', bn: 'সকল' },
  autofill_settings: { en: 'Autofill Settings', bn: 'অটোফিল সেটিংস' },
  autofill_desc: { en: 'Choose which fields auto-populate from suggestions', bn: 'পরামর্শ নির্বাচন করলে যা স্বয়ংক্রিয় পূরণ হবে' },
  autofill_category: { en: 'Autofill Category', bn: 'ক্যাটাগরি অটোফিল' },
  autofill_account: { en: 'Autofill Account', bn: 'একাউন্ট অটোফিল' },
  autofill_amount: { en: 'Autofill Amount', bn: 'পরিমাণ অটোফিল' },
  autofill_notes: { en: 'Autofill Notes', bn: 'নোটস অটোফিল' },
  autofill_labels: { en: 'Autofill Label / Tag', bn: 'লেবেল অটোফিল' },
  language: { en: 'Language', bn: 'ভাষা' },
  english: { en: 'English', bn: 'ইংরেজি' },
  bangla: { en: 'বাংলা', bn: 'বাংলা' },
  bilingual: { en: 'Bilingual (Both)', bn: 'উভয় ভাষা (Bilingual)' },
  net_earnings: { en: 'Net Earnings', bn: 'নেট আয় ও লাভ' },
  labels: { en: 'Labels', bn: 'লেবেলসমূহ' },
  items_summary: { en: 'Items Summary', bn: 'আইটেম সামারি' },
  reminders: { en: 'Reminders', bn: 'রিমাইন্ডার ও বিল' },
  tab_customization: { en: 'Navigation Tabs', bn: 'ট্যাব কাস্টমাইজেশন' },
  tab_position: { en: 'Tab Position', bn: 'ট্যাব অবস্থান' },
  tab_position_top: { en: 'Top Bar', bn: 'উপরে' },
  tab_position_bottom: { en: 'Bottom Bar', bn: 'নিচে' },
  account_calculation: { en: 'Account Calculation', bn: 'অ্যাকাউন্ট হিসাব গণনা' },
  include_in_calculation: { en: 'Include in Calculation', bn: 'হিসাবে অন্তর্ভুক্ত করুন' },
  exclude_from_calculation: { en: 'Exclude from Calculation', bn: 'হিসাব থেকে বাদ দিন' },
  included: { en: 'Included', bn: 'যুক্ত' },
  excluded: { en: 'Excluded', bn: 'বাদ' },
  adjust_calculation: { en: 'Adjust Amount', bn: 'অ্যামাউন্ট অ্যাডজাস্ট' },
  effective_amount: { en: 'Effective Amount', bn: 'কার্যকর অ্যামাউন্ট' },
  actual_balance: { en: 'Original Balance', bn: 'মূল ব্যালেন্স' },
  adjustment_amount: { en: 'Adjustment', bn: 'অ্যাডজাস্টমেন্ট' },
  calculated_net_worth: { en: 'Calculated Net Worth', bn: 'কার্যকর মোট হিসাব (Net Worth)' },
  actual_net_worth: { en: 'Original Net Worth', bn: 'মূল মোট হিসাব' },
  calculated_assets: { en: 'Calculated Assets', bn: 'কার্যকর সম্পদ' },
  calculated_liabilities: { en: 'Calculated Liabilities', bn: 'কার্যকর দায়' },
  reset_calculation: { en: 'Reset All', bn: 'সব রিসেট' },
  expendable: { en: 'Expendable', bn: 'ব্যয়যোগ্য অর্থ' },
  expected_expendable: { en: 'Expected Expendable', bn: 'প্রত্যাশিত ব্যয়যোগ্য অর্থ' },
  budget_adjustment: { en: 'Budget Adjustment', bn: 'বাজেট সমন্বয়' },
  remaining_expenses: { en: 'Remaining Expenses', bn: 'অবশিষ্ট ব্যয়' },
  additional_cost: { en: 'Additional / Over Budget', bn: 'অতিরিক্ত খরচ' },
  potential_income: { en: 'Potential Income', bn: 'সম্ভাব্য আয়' },
  daily_summary: { en: 'Daily Summary', bn: 'দৈনিক সারসংক্ষেপ' },
  budget_summary: { en: 'Budget Summary', bn: 'বাজেট সারসংক্ষেপ' },
  favorite_accounts: { en: 'Favorite Accounts', bn: 'পছন্দের হিসাবসমূহ' },
  calendar_view: { en: 'Calendar View', bn: 'ক্যালেন্ডার ভিউ' },
  calendar_summary: { en: 'Calendar Summary', bn: 'ক্যালেন্ডার সারসংক্ষেপ' },
  customize_cards: { en: 'Customize Dashboard', bn: 'ড্যাশবোর্ড কাস্টমাইজ' },
  payment_source: { en: 'Payment Source', bn: 'পেমেন্ট সোর্স' },
  payment_source_analysis: { en: 'Payment Source Requirement Analysis', bn: 'পেমেন্ট সোর্স প্রয়োজনীয়তা বিশ্লেষণ' },
  payment_source_subtitle: { en: 'Account-based fund requirement & transfer insights', bn: 'অ্যাকাউন্টভিত্তিক তহবিল প্রয়োজনীয়তা ও স্থানান্তর বিশ্লেষণ' },
  calculation_basis: { en: 'Calculation Basis', bn: 'গণনার ভিত্তি' },
  budget_amount_basis: { en: 'Budget Amount', bn: 'পূর্ণ বাজেট ভিত্তিক' },
  remaining_amount_basis: { en: 'Remaining Amount', bn: 'অবশিষ্ট বাজেট ভিত্তিক' },
  required_amount: { en: 'Required Amount', bn: 'প্রয়োজনীয় অর্থ' },
  available_amount: { en: 'Available Amount', bn: 'উপলব্ধ অর্থ' },
  shortfall: { en: 'Shortfall', bn: 'ঘাটতি' },
  surplus: { en: 'Surplus', bn: 'উদ্বৃত্ত' },
  fund_allocation_insight: { en: 'Fund Allocation Insight', bn: 'তহবিল বণ্টন ও স্থানান্তর পরামর্শ' },
  fund_allocation_subtitle: { en: 'Smart transfer suggestions to cover account shortages', bn: 'অ্যাকাউন্টের ঘাটতি মেটাতে স্মার্ট স্থানান্তর পরামর্শ' },
  execute_transfer: { en: 'Execute Transfer', bn: 'স্থানান্তর করুন' },
  move_funds: { en: 'Move Funds', bn: 'তহবিল স্থানান্তর' },
  monthly_fund_summary: { en: 'Monthly Fund Summary', bn: 'মাসিক তহবিল সারসংক্ষেপ' },
  accounts_need_funding: { en: 'Accounts Need Funding', bn: 'তহবিল প্রয়োজন এমন হিসাব' },
  accounts_with_surplus: { en: 'Accounts with Surplus', bn: 'উদ্বৃত্ত হিসাব' },
  all_funded: { en: 'All Accounts Fully Funded', bn: 'সকল হিসাবে পর্যাপ্ত তহবিল আছে' },
  expand_breakdown: { en: 'View Itemized Breakdown', bn: 'বিস্তারিত বিবরণ দেখুন' },
  hide_breakdown: { en: 'Hide Breakdown', bn: 'বিবরণ লুকান' },
  itemized_expenses: { en: 'Assigned Expenses', bn: 'নির্ধারিত ব্যয়সমূহ' },
  itemized_incomes: { en: 'Expected Incomes', bn: 'প্রত্যাশিত আয়সমূহ' },
  current_balance: { en: 'Current Balance', bn: 'বর্তমান ব্যালেন্স' },
  filter_shortfall: { en: 'Shortfall Only', bn: 'শুধু ঘাটতি' },
  filter_surplus: { en: 'Surplus Only', bn: 'শুধু উদ্বৃত্ত' },
  filter_balanced: { en: 'Balanced', bn: 'ভারসাম্যপূর্ণ' },
  by_account: { en: 'By Account', bn: 'হিসাব ভিত্তিক' },
  by_category_split: { en: 'By Category & Splits', bn: 'ক্যাটাগরি ও বরাদ্দ' },
  split_expense_across_accounts: { en: 'Split Budget Across Accounts', bn: 'একাধিক হিসাবে বাজেট বণ্টন' },
  allocated_accounts: { en: 'Assigned Accounts', bn: 'বরাদ্দকৃত হিসাব' },
  unallocated: { en: 'Unallocated', bn: 'অবরাদ্দকৃত' },
  save_allocations: { en: 'Save Allocations', bn: 'বরাদ্দ সংরক্ষণ করুন' },
  total_category_budget: { en: 'Total Category Budget', bn: 'ক্যাটাগরির মোট বাজেট' },
  backup_sync: { en: 'Backup & Sync', bn: 'ব্যাকআপ ও সিঙ্ক' },
  settings: { en: 'Settings', bn: 'সেটিংস' },
};
