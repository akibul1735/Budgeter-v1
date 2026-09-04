import React from 'react';
import {
  LayoutDashboard,
  Receipt,
  CreditCard,
  Scale,
  PieChart,
  TrendingUp,
  Tags,
  FileSpreadsheet,
  BellRing,
  Building2,
  FolderTree,
  Sliders,
  Calculator,
  HardDriveDownload,
  Settings,
  X,
  PlusCircle,
  MoveRight,
  ShieldCheck,
  Zap,
} from 'lucide-react';
import { useBudget } from '../context/BudgetContext';
import { AppTab, TabPosition, LanguageMode } from '../types';
import { LanguageHelper } from '../utils/languageHelper';

interface NavigationProps {
  isSidebarOpen: boolean;
  onCloseSidebar: () => void;
  onOpenNewTransaction: () => void;
  onOpenNewAccount: () => void;
  onOpenNewCategory: () => void;
  onOpenTabCustomizer: () => void;
  onOpenAutofillModal: () => void;
}

export const getTabIcon = (tab: AppTab) => {
  switch (tab) {
    case AppTab.MAIN:
      return LayoutDashboard;
    case AppTab.TRANSACTIONS:
      return Receipt;
    case AppTab.PAYMENT_SOURCE:
      return CreditCard;
    case AppTab.BALANCE_SHEET:
      return Scale;
    case AppTab.BUDGET:
      return PieChart;
    case AppTab.NET_EARNINGS:
      return TrendingUp;
    case AppTab.LABELS:
      return Tags;
    case AppTab.ITEMS_SUMMARY:
      return FileSpreadsheet;
    case AppTab.REMINDERS:
      return BellRing;
    case AppTab.ACCOUNTS:
      return Building2;
    case AppTab.CATEGORIES:
      return FolderTree;
    case AppTab.BUDGET_MAKER:
      return Zap;
    case AppTab.ACCOUNT_CALC:
      return Calculator;
    case AppTab.BACKUP_SYNC:
      return HardDriveDownload;
    case AppTab.SETTINGS:
      return Settings;
    default:
      return LayoutDashboard;
  }
};

export const Navigation: React.FC<NavigationProps> = ({
  isSidebarOpen,
  onCloseSidebar,
  onOpenNewTransaction,
  onOpenNewAccount,
  onOpenNewCategory,
  onOpenTabCustomizer,
  onOpenAutofillModal,
}) => {
  const {
    currentTab,
    setCurrentTab,
    tabConfig,
    languageMode,
    accounts,
    categories,
    recurringBills,
    financialOverview,
  } = useBudget();

  const getTabLabel = (tab: AppTab) => {
    switch (tab) {
      case AppTab.MAIN:
        return LanguageHelper.getString('main', languageMode);
      case AppTab.TRANSACTIONS:
        return LanguageHelper.getString('transactions', languageMode);
      case AppTab.PAYMENT_SOURCE:
        return LanguageHelper.getString('payment_source', languageMode);
      case AppTab.BALANCE_SHEET:
        return LanguageHelper.getString('balance_sheet', languageMode);
      case AppTab.BUDGET:
        return LanguageHelper.getString('budget', languageMode);
      case AppTab.NET_EARNINGS:
        return LanguageHelper.getString('net_earnings', languageMode);
      case AppTab.LABELS:
        return LanguageHelper.getString('labels', languageMode);
      case AppTab.ITEMS_SUMMARY:
        return LanguageHelper.getString('items_summary', languageMode);
      case AppTab.REMINDERS:
        return LanguageHelper.getString('reminders', languageMode);
      case AppTab.ACCOUNTS:
        return LanguageHelper.getString('accounts', languageMode);
      case AppTab.CATEGORIES:
        return LanguageHelper.getString('categories', languageMode);
      case AppTab.BUDGET_MAKER:
        return LanguageHelper.getString('budget_maker', languageMode);
      case AppTab.ACCOUNT_CALC:
        return LanguageHelper.getString('account_calculation', languageMode);
      case AppTab.BACKUP_SYNC:
        return LanguageHelper.getString('backup_sync', languageMode);
      case AppTab.SETTINGS:
        return LanguageHelper.getString('settings', languageMode);
      default:
        return tab;
    }
  };

  const visibleTabs = tabConfig.allTabsOrder.filter((t) => tabConfig.enabledTabs.includes(t));

  const navItems = [
    { tab: AppTab.MAIN, section: 'core' },
    { tab: AppTab.TRANSACTIONS, section: 'core' },
    { tab: AppTab.PAYMENT_SOURCE, section: 'core' },
    { tab: AppTab.BALANCE_SHEET, section: 'analytics' },
    { tab: AppTab.BUDGET, section: 'analytics' },
    { tab: AppTab.BUDGET_MAKER, section: 'analytics' },
    { tab: AppTab.NET_EARNINGS, section: 'analytics' },
    { tab: AppTab.REMINDERS, section: 'manage', count: recurringBills.length },
    { tab: AppTab.ACCOUNTS, section: 'manage', count: accounts.length },
    { tab: AppTab.CATEGORIES, section: 'manage', count: categories.length },
    { tab: AppTab.LABELS, section: 'manage' },
    { tab: AppTab.ITEMS_SUMMARY, section: 'manage' },
    { tab: AppTab.ACCOUNT_CALC, section: 'tools' },
    { tab: AppTab.BACKUP_SYNC, section: 'tools' },
    { tab: AppTab.SETTINGS, section: 'tools' },
  ];

  return (
    <>
      {/* Mobile Drawer Overlay */}
      {isSidebarOpen && (
        <div
          className="fixed inset-0 z-40 bg-slate-900/50 backdrop-blur-xs lg:hidden transition-opacity"
          onClick={onCloseSidebar}
          aria-hidden="true"
        />
      )}

      {/* Drawer / Desktop Sidebar */}
      <aside
        id="app-sidebar"
        className={`fixed top-0 bottom-0 left-0 z-50 w-72 bg-white border-r border-slate-200/90 flex flex-col transition-transform duration-300 ease-in-out lg:translate-x-0 ${
          isSidebarOpen ? 'translate-x-0' : '-translate-x-full'
        }`}
      >
        {/* Brand Header */}
        <div className="p-4 border-b border-slate-100 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-9 h-9 rounded-xl bg-gradient-to-br from-amber-400 to-amber-600 flex items-center justify-center text-white font-bold text-lg shadow-sm">
              ৳
            </div>
            <div>
              <div className="font-bold text-slate-900 leading-tight">Budgeter</div>
              <div className="text-[11px] text-slate-500 font-medium">Double-Entry Financial Suite</div>
            </div>
          </div>
          <button
            onClick={onCloseSidebar}
            className="p-1.5 rounded-lg text-slate-400 hover:text-slate-600 hover:bg-slate-100 lg:hidden"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Quick Summary Pill */}
        <div className="p-3 mx-3 my-2 bg-slate-50 rounded-xl border border-slate-100 flex items-center justify-between text-xs">
          <div>
            <div className="text-slate-500 text-[10px] font-medium uppercase tracking-wider">Net Worth</div>
            <div className="font-bold text-slate-900">
              {LanguageHelper.formatCurrency(financialOverview.netWorth, languageMode)}
            </div>
          </div>
          <div className="text-right">
            <div className="text-slate-500 text-[10px] font-medium uppercase tracking-wider">Status</div>
            <div className="font-semibold text-emerald-600 flex items-center gap-1 justify-end">
              <ShieldCheck className="w-3.5 h-3.5" />
              <span>Balanced</span>
            </div>
          </div>
        </div>

        {/* Navigation List */}
        <div className="flex-1 overflow-y-auto px-3 py-2 space-y-1">
          {navItems.map((item) => {
            const Icon = getTabIcon(item.tab);
            const isSelected = currentTab === item.tab;
            return (
              <button
                key={item.tab}
                id={`nav-item-${item.tab.toLowerCase()}`}
                onClick={() => {
                  setCurrentTab(item.tab);
                  onCloseSidebar();
                }}
                className={`w-full flex items-center justify-between px-3 py-2 rounded-xl text-xs font-semibold transition-all ${
                  isSelected
                    ? 'bg-emerald-50 text-emerald-700 font-bold border border-emerald-200/60 shadow-xs'
                    : 'text-slate-600 hover:bg-slate-100/80 hover:text-slate-900'
                }`}
              >
                <div className="flex items-center gap-2.5">
                  <Icon className={`w-4 h-4 ${isSelected ? 'text-emerald-600' : 'text-slate-400'}`} />
                  <span>{getTabLabel(item.tab)}</span>
                </div>
                {item.count !== undefined && (
                  <span
                    className={`text-[10px] px-1.5 py-0.5 rounded-full ${
                      isSelected ? 'bg-emerald-200 text-emerald-800' : 'bg-slate-200/70 text-slate-600'
                    }`}
                  >
                    {item.count}
                  </span>
                )}
              </button>
            );
          })}
        </div>

        {/* Footer Quick Action Buttons */}
        <div className="p-3 border-t border-slate-100 space-y-1.5 bg-slate-50/50">
          <div className="grid grid-cols-2 gap-1.5">
            <button
              onClick={() => {
                onOpenNewAccount();
                onCloseSidebar();
              }}
              className="px-2 py-1.5 bg-white hover:bg-slate-100 border border-slate-200 rounded-lg text-slate-700 text-[11px] font-semibold flex items-center justify-center gap-1 shadow-2xs"
            >
              <PlusCircle className="w-3.5 h-3.5 text-blue-500" />
              <span>+ Account</span>
            </button>
            <button
              onClick={() => {
                onOpenNewCategory();
                onCloseSidebar();
              }}
              className="px-2 py-1.5 bg-white hover:bg-slate-100 border border-slate-200 rounded-lg text-slate-700 text-[11px] font-semibold flex items-center justify-center gap-1 shadow-2xs"
            >
              <PlusCircle className="w-3.5 h-3.5 text-amber-500" />
              <span>+ Category</span>
            </button>
          </div>

          <div className="flex items-center justify-between pt-1">
            <button
              onClick={() => {
                onOpenTabCustomizer();
                onCloseSidebar();
              }}
              className="text-[11px] text-slate-500 hover:text-emerald-700 flex items-center gap-1 font-medium"
            >
              <Sliders className="w-3 h-3" />
              <span>Customize Tabs</span>
            </button>
            <button
              onClick={() => {
                onOpenAutofillModal();
                onCloseSidebar();
              }}
              className="text-[11px] text-slate-500 hover:text-emerald-700 flex items-center gap-1 font-medium"
            >
              <Zap className="w-3 h-3" />
              <span>Autofill</span>
            </button>
          </div>
        </div>
      </aside>

      {/* Top Navigation Tab Bar (If Position is TOP) */}
      {tabConfig.position === TabPosition.TOP && (
        <nav
          id="top-nav-bar"
          className="bg-white border-b border-slate-200 overflow-x-auto scrollbar-none px-2 py-1 flex items-center gap-1.5"
        >
          {visibleTabs.map((tab) => {
            const Icon = getTabIcon(tab);
            const isSelected = currentTab === tab;
            return (
              <button
                key={tab}
                id={`top-tab-${tab.toLowerCase()}`}
                onClick={() => setCurrentTab(tab)}
                className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold whitespace-nowrap transition-all ${
                  isSelected
                    ? 'bg-emerald-600 text-white shadow-xs'
                    : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900'
                }`}
              >
                <Icon className="w-3.5 h-3.5" />
                <span>{getTabLabel(tab)}</span>
              </button>
            );
          })}
        </nav>
      )}

      {/* Bottom Navigation Tab Bar (If Position is BOTTOM) */}
      {tabConfig.position === TabPosition.BOTTOM && (
        <nav
          id="bottom-nav-bar"
          className="fixed bottom-0 left-0 right-0 z-30 bg-white/95 backdrop-blur-md border-t border-slate-200/90 lg:hidden px-2 py-1.5 flex items-center justify-around shadow-lg"
        >
          {visibleTabs.slice(0, 5).map((tab) => {
            const Icon = getTabIcon(tab);
            const isSelected = currentTab === tab;
            return (
              <button
                key={tab}
                id={`bottom-tab-${tab.toLowerCase()}`}
                onClick={() => setCurrentTab(tab)}
                className={`flex flex-col items-center justify-center p-1 rounded-xl transition-all ${
                  isSelected ? 'text-emerald-600 font-bold' : 'text-slate-400 hover:text-slate-700'
                }`}
              >
                <div
                  className={`p-1 rounded-xl transition-all ${
                    isSelected ? 'bg-emerald-50 scale-110' : ''
                  }`}
                >
                  <Icon className="w-5 h-5" />
                </div>
                <span className="text-[10px] mt-0.5 max-w-[64px] truncate leading-none">
                  {getTabLabel(tab)}
                </span>
              </button>
            );
          })}
        </nav>
      )}
    </>
  );
};
