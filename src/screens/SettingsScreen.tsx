import React from 'react';
import {
  Settings as SettingsIcon,
  Globe,
  DollarSign,
  Palette,
  Sliders,
  Bell,
  HardDriveDownload,
  Info,
  Layers,
  Check,
  AlignLeft,
  Columns,
} from 'lucide-react';
import { useBudget } from '../context/BudgetContext';
import { LanguageMode, CurrencyDisplayMode, HierarchyDisplayMode } from '../types';
import { LanguageHelper } from '../utils/languageHelper';

interface SettingsScreenProps {
  onOpenThemeFontModal: () => void;
  onOpenTabCustomizer: () => void;
  onOpenAutofillModal: () => void;
  onOpenDashboardCustomizer: () => void;
}

export const SettingsScreen: React.FC<SettingsScreenProps> = ({
  onOpenThemeFontModal,
  onOpenTabCustomizer,
  onOpenAutofillModal,
  onOpenDashboardCustomizer,
}) => {
  const {
    languageMode,
    currencyConfig,
    dashboardConfig,
    hierarchyDisplayMode,
    setHierarchyDisplayMode,
  } = useBudget();

  return (
    <div className="space-y-4 sm:space-y-6 pb-20">
      {/* Header Banner */}
      <div className="p-6 bg-white rounded-3xl border border-slate-200/80 shadow-xs">
        <h2 className="text-lg font-bold text-slate-900 flex items-center gap-2">
          <SettingsIcon className="w-5 h-5 text-emerald-600" />
          <span>{LanguageHelper.getString('settings', languageMode)}</span>
        </h2>
        <p className="text-xs text-slate-500 mt-0.5">
          Configure preferences, currencies, localized modes, and display customization
        </p>
      </div>

      {/* Category & Account Display Layout Preference (New Feature) */}
      <div className="p-6 bg-white rounded-3xl border border-slate-200/80 shadow-xs space-y-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2.5 text-slate-900 font-bold text-sm">
            <div className="w-8 h-8 rounded-xl bg-emerald-50 text-emerald-700 flex items-center justify-center">
              <Layers className="w-4 h-4" />
            </div>
            <div>
              <div>Category & Account Hierarchy Display</div>
              <div className="text-xs text-slate-500 font-normal">
                Choose how group names and category/account titles are formatted across cards & forms
              </div>
            </div>
          </div>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 pt-1">
          {/* Double Line Option (Default) */}
          <button
            type="button"
            id="btn-hierarchy-double-line"
            onClick={() => setHierarchyDisplayMode(HierarchyDisplayMode.DOUBLE_LINE)}
            className={`p-4 rounded-2xl border text-left transition-all relative ${
              hierarchyDisplayMode === HierarchyDisplayMode.DOUBLE_LINE
                ? 'border-emerald-500 bg-emerald-50/50 ring-2 ring-emerald-500/20'
                : 'border-slate-200 hover:border-slate-300 bg-slate-50/50'
            }`}
          >
            <div className="flex items-start justify-between">
              <div>
                <div className="text-xs font-bold text-slate-900 flex items-center gap-1.5">
                  <span>Double Lines (Default)</span>
                  <span className="text-[10px] px-1.5 py-0.5 bg-emerald-100 text-emerald-800 rounded font-semibold">
                    Recommended
                  </span>
                </div>
                <div className="text-[11px] text-slate-500 mt-0.5">
                  Group above, Item below
                </div>
              </div>
              {hierarchyDisplayMode === HierarchyDisplayMode.DOUBLE_LINE && (
                <div className="w-5 h-5 rounded-full bg-emerald-600 text-white flex items-center justify-center shrink-0">
                  <Check className="w-3 h-3" />
                </div>
              )}
            </div>

            {/* Live Visual Sample */}
            <div className="mt-3 p-2.5 bg-white rounded-xl border border-slate-200/80 text-xs">
              <div className="text-[10px] text-slate-500 font-medium leading-none">&gt;Food & Dining</div>
              <div className="text-xs font-bold text-slate-900 mt-0.5">Groceries & Supermarket</div>
            </div>
          </button>

          {/* Single Line Option */}
          <button
            type="button"
            id="btn-hierarchy-single-line"
            onClick={() => setHierarchyDisplayMode(HierarchyDisplayMode.SINGLE_LINE)}
            className={`p-4 rounded-2xl border text-left transition-all relative ${
              hierarchyDisplayMode === HierarchyDisplayMode.SINGLE_LINE
                ? 'border-emerald-500 bg-emerald-50/50 ring-2 ring-emerald-500/20'
                : 'border-slate-200 hover:border-slate-300 bg-slate-50/50'
            }`}
          >
            <div className="flex items-start justify-between">
              <div>
                <div className="text-xs font-bold text-slate-900">
                  Single Line
                </div>
                <div className="text-[11px] text-slate-500 mt-0.5">
                  Group &gt; Category inline
                </div>
              </div>
              {hierarchyDisplayMode === HierarchyDisplayMode.SINGLE_LINE && (
                <div className="w-5 h-5 rounded-full bg-emerald-600 text-white flex items-center justify-center shrink-0">
                  <Check className="w-3 h-3" />
                </div>
              )}
            </div>

            {/* Live Visual Sample */}
            <div className="mt-3 p-2.5 bg-white rounded-xl border border-slate-200/80 text-xs flex items-center">
              <span className="text-xs font-bold text-slate-900 truncate">
                Food & Dining &gt; Groceries & Supermarket
              </span>
            </div>
          </button>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {/* Language & Currency Card */}
        <div
          onClick={onOpenThemeFontModal}
          className="p-5 bg-white rounded-3xl border border-slate-200/80 hover:border-emerald-300 shadow-xs cursor-pointer transition-all space-y-3"
        >
          <div className="flex items-center gap-2 text-slate-900 font-bold text-sm">
            <Globe className="w-4 h-4 text-emerald-600" />
            <span>Language & Currency</span>
          </div>
          <div className="text-xs text-slate-500">
            Current Language: <strong className="text-slate-800 capitalize">{languageMode.toLowerCase()}</strong>
            <br />
            Currency: <strong className="text-slate-800">{currencyConfig.selectedSymbol} ({currencyConfig.selectedCode})</strong>
          </div>
        </div>

        {/* Dashboard Widgets Customizer */}
        <div
          onClick={onOpenDashboardCustomizer}
          className="p-5 bg-white rounded-3xl border border-slate-200/80 hover:border-emerald-300 shadow-xs cursor-pointer transition-all space-y-3"
        >
          <div className="flex items-center gap-2 text-slate-900 font-bold text-sm">
            <Palette className="w-4 h-4 text-emerald-600" />
            <span>Dashboard Widgets</span>
          </div>
          <div className="text-xs text-slate-500">
            {dashboardConfig.enabledCards.length} active widgets enabled. Click to reorder or toggle cards.
          </div>
        </div>

        {/* Navigation Tabs Customizer */}
        <div
          onClick={onOpenTabCustomizer}
          className="p-5 bg-white rounded-3xl border border-slate-200/80 hover:border-emerald-300 shadow-xs cursor-pointer transition-all space-y-3"
        >
          <div className="flex items-center gap-2 text-slate-900 font-bold text-sm">
            <Sliders className="w-4 h-4 text-emerald-600" />
            <span>Navigation Customizer</span>
          </div>
          <div className="text-xs text-slate-500">
            Switch between Drawer Sidebar and Top/Bottom Tab Bar. Pin your favorite tabs.
          </div>
        </div>

        {/* Transaction Autofill Rules */}
        <div
          onClick={onOpenAutofillModal}
          className="p-5 bg-white rounded-3xl border border-slate-200/80 hover:border-emerald-300 shadow-xs cursor-pointer transition-all space-y-3"
        >
          <div className="flex items-center gap-2 text-slate-900 font-bold text-sm">
            <Info className="w-4 h-4 text-emerald-600" />
            <span>Autofill & Smart Rules</span>
          </div>
          <div className="text-xs text-slate-500">
            Configure smart suggestions for account and category based on transaction title.
          </div>
        </div>
      </div>

      {/* App Info & About */}
      <div className="p-6 bg-slate-50 rounded-3xl border border-slate-200/80 text-xs text-slate-600 space-y-1.5">
        <div className="font-bold text-slate-800 text-sm flex items-center gap-2">
          <div className="w-5 h-5 rounded-full bg-amber-400 text-slate-950 font-bold flex items-center justify-center text-[10px]">
            ৳
          </div>
          <span>Budgeter v1.0.0</span>
        </div>
        <p>Complete Double-Entry Ledger, Fund Requirements & Smart Budgeting System</p>
        <p className="text-[11px] text-slate-400 pt-1">
          Designed with offline-first client storage & double-entry mathematical integrity.
        </p>
      </div>
    </div>
  );
};

