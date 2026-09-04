import React from 'react';
import {
  Menu,
  Calculator,
  Plus,
  ArrowLeft,
  ChevronLeft,
  ChevronRight,
  Sparkles,
  SlidersHorizontal,
  Palette,
  Eye,
} from 'lucide-react';
import { useBudget } from '../context/BudgetContext';
import { LanguageMode, AppTab } from '../types';
import { LanguageHelper } from '../utils/languageHelper';

interface HeaderProps {
  onOpenSidebar: () => void;
  onOpenCalculator: () => void;
  onOpenNewTransaction: () => void;
  onOpenCustomizer: () => void;
  onOpenThemeModal: () => void;
}

export const Header: React.FC<HeaderProps> = ({
  onOpenSidebar,
  onOpenCalculator,
  onOpenNewTransaction,
  onOpenCustomizer,
  onOpenThemeModal,
}) => {
  const {
    currentTab,
    setCurrentTab,
    languageMode,
    setLanguageMode,
    isDemoMode,
    toggleDemoMode,
    selectedYear,
    selectedMonth,
    prevMonth,
    nextMonth,
    financialOverview,
  } = useBudget();

  const getTitle = () => {
    switch (currentTab) {
      case AppTab.MAIN:
        return LanguageHelper.getString('dashboard', languageMode);
      case AppTab.TRANSACTIONS:
        return LanguageHelper.getString('ledger', languageMode);
      case AppTab.PAYMENT_SOURCE:
        return LanguageHelper.getString('payment_source', languageMode);
      case AppTab.BALANCE_SHEET:
        return LanguageHelper.getString('balance_sheet', languageMode);
      case AppTab.BUDGET:
        return LanguageHelper.getString('budget', languageMode);
      case AppTab.BUDGET_MAKER:
        return LanguageHelper.getString('budget_maker', languageMode);
      case AppTab.NET_EARNINGS:
        return LanguageHelper.getString('net_earnings', languageMode);
      case AppTab.REMINDERS:
        return LanguageHelper.getString('reminders', languageMode);
      case AppTab.ACCOUNTS:
        return LanguageHelper.getString('accounts', languageMode);
      case AppTab.CATEGORIES:
        return LanguageHelper.getString('categories', languageMode);
      case AppTab.LABELS:
        return LanguageHelper.getString('labels', languageMode);
      case AppTab.ITEMS_SUMMARY:
        return LanguageHelper.getString('items_summary', languageMode);
      case AppTab.ACCOUNT_CALC:
        return LanguageHelper.getString('account_calculation', languageMode);
      case AppTab.BACKUP_SYNC:
        return LanguageHelper.getString('backup_sync', languageMode);
      case AppTab.SETTINGS:
        return LanguageHelper.getString('settings', languageMode);
      default:
        return 'Budgeter';
    }
  };

  const monthName = new Date(selectedYear, selectedMonth - 1, 1).toLocaleString('default', {
    month: 'short',
    year: 'numeric',
  });

  return (
    <header className="sticky top-0 z-30 bg-white/95 backdrop-blur-md border-b border-slate-200/80 px-3 sm:px-6 py-2.5 transition-all">
      <div className="max-w-7xl mx-auto flex items-center justify-between gap-2">
        {/* Left Side: Menu toggle & Title */}
        <div className="flex items-center gap-2 sm:gap-3">
          <button
            id="btn-open-sidebar"
            onClick={onOpenSidebar}
            className="p-2 rounded-xl text-slate-700 hover:bg-slate-100 active:scale-95 transition-all lg:hidden"
            aria-label="Open Navigation Menu"
          >
            <Menu className="w-5 h-5" />
          </button>

          {currentTab !== AppTab.MAIN && (
            <button
              id="btn-back-to-dashboard"
              onClick={() => setCurrentTab(AppTab.MAIN)}
              className="p-2 rounded-xl text-slate-700 hover:bg-slate-100 active:scale-95 transition-all hidden sm:flex items-center"
              title="Back to Dashboard"
            >
              <ArrowLeft className="w-4 h-4" />
            </button>
          )}

          <div className="flex flex-col">
            <div className="flex items-center gap-2">
              <h1 className="text-base sm:text-lg font-bold tracking-tight text-slate-900 leading-tight">
                {getTitle()}
              </h1>
              {isDemoMode && (
                <span className="inline-flex items-center gap-1 text-[11px] font-semibold bg-amber-100 text-amber-800 px-2 py-0.5 rounded-full border border-amber-200">
                  <span className="w-1.5 h-1.5 rounded-full bg-amber-500 animate-pulse"></span>
                  DEMO
                </span>
              )}
            </div>
            <span className="text-[11px] text-slate-500 hidden sm:inline-block">
              {financialOverview.isLedgerBalanced ? (
                <span className="text-emerald-600 font-medium">✓ Double-Entry Balanced</span>
              ) : (
                <span className="text-rose-600 font-medium">⚠ Ledger Unbalanced</span>
              )}
            </span>
          </div>
        </div>

        {/* Center: Month/Year Selector */}
        <div className="flex items-center bg-slate-100/90 rounded-xl p-1 border border-slate-200/70 text-xs sm:text-sm font-medium text-slate-700">
          <button
            id="btn-prev-month"
            onClick={prevMonth}
            className="p-1 sm:p-1.5 hover:bg-white rounded-lg transition-colors text-slate-600 hover:text-slate-900"
            title="Previous Month"
          >
            <ChevronLeft className="w-4 h-4" />
          </button>
          <span className="px-2 sm:px-3 font-semibold text-slate-800 select-none whitespace-nowrap">
            {monthName}
          </span>
          <button
            id="btn-next-month"
            onClick={nextMonth}
            className="p-1 sm:p-1.5 hover:bg-white rounded-lg transition-colors text-slate-600 hover:text-slate-900"
            title="Next Month"
          >
            <ChevronRight className="w-4 h-4" />
          </button>
        </div>

        {/* Right Side: Quick Tools & Actions */}
        <div className="flex items-center gap-1 sm:gap-2">
          {/* Language Toggle */}
          <button
            id="btn-toggle-language"
            onClick={() => {
              if (languageMode === LanguageMode.ENGLISH) setLanguageMode(LanguageMode.BANGLA);
              else if (languageMode === LanguageMode.BANGLA) setLanguageMode(LanguageMode.BILINGUAL);
              else setLanguageMode(LanguageMode.ENGLISH);
            }}
            className="px-2.5 py-1.5 text-xs font-semibold rounded-xl bg-slate-100 hover:bg-slate-200/80 text-slate-700 border border-slate-200 transition-all flex items-center gap-1"
            title="Switch Language (English / বাংলা / Bilingual)"
          >
            <span>{languageMode === LanguageMode.ENGLISH ? 'EN' : languageMode === LanguageMode.BANGLA ? 'বাং' : 'EN/বাং'}</span>
          </button>

          {/* Calculator Quick Action */}
          <button
            id="btn-open-calculator"
            onClick={onOpenCalculator}
            className="p-2 rounded-xl text-slate-700 hover:bg-slate-100 border border-slate-200/80 active:scale-95 transition-all"
            title="Open Quick Calculator"
          >
            <Calculator className="w-4 h-4 text-emerald-600" />
          </button>

          {/* Theme / Currency Modal */}
          <button
            id="btn-open-theme-settings"
            onClick={onOpenThemeModal}
            className="p-2 rounded-xl text-slate-700 hover:bg-slate-100 border border-slate-200/80 active:scale-95 transition-all hidden md:flex"
            title="Customize Currency & Display"
          >
            <Palette className="w-4 h-4 text-blue-600" />
          </button>

          {/* Dashboard Customizer */}
          {currentTab === AppTab.MAIN && (
            <button
              id="btn-customize-dashboard"
              onClick={onOpenCustomizer}
              className="p-2 rounded-xl text-slate-700 hover:bg-slate-100 border border-slate-200/80 active:scale-95 transition-all hidden sm:flex"
              title="Customize Dashboard Cards"
            >
              <SlidersHorizontal className="w-4 h-4 text-purple-600" />
            </button>
          )}

          {/* New Transaction Primary Button */}
          <button
            id="btn-add-transaction-header"
            onClick={onOpenNewTransaction}
            className="flex items-center gap-1.5 px-3 py-1.5 bg-emerald-600 hover:bg-emerald-700 active:scale-95 text-white text-xs sm:text-sm font-semibold rounded-xl shadow-sm transition-all"
          >
            <Plus className="w-4 h-4" />
            <span className="hidden sm:inline">Add Entry</span>
          </button>
        </div>
      </div>
    </header>
  );
};
