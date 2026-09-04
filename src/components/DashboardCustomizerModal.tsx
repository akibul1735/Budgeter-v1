import React from 'react';
import { X, Check, ArrowUp, ArrowDown, Eye, EyeOff } from 'lucide-react';
import { useBudget } from '../context/BudgetContext';
import {
  DashboardCardType,
  DailySummaryMode,
  DailySummaryPeriod,
  BudgetChartShape,
  LanguageMode,
} from '../types';
import { LanguageHelper } from '../utils/languageHelper';

interface DashboardCustomizerModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export const DashboardCustomizerModal: React.FC<DashboardCustomizerModalProps> = ({
  isOpen,
  onClose,
}) => {
  const { dashboardConfig, setDashboardConfig, languageMode } = useBudget();

  if (!isOpen) return null;

  const toggleCard = (card: DashboardCardType) => {
    if (dashboardConfig.enabledCards.includes(card)) {
      if (dashboardConfig.enabledCards.length <= 1) return;
      setDashboardConfig({
        ...dashboardConfig,
        enabledCards: dashboardConfig.enabledCards.filter((c) => c !== card),
      });
    } else {
      setDashboardConfig({
        ...dashboardConfig,
        enabledCards: [...dashboardConfig.enabledCards, card],
      });
    }
  };

  const moveCard = (index: number, direction: 'up' | 'down') => {
    const newOrder = [...dashboardConfig.cardsOrder];
    const targetIdx = direction === 'up' ? index - 1 : index + 1;
    if (targetIdx < 0 || targetIdx >= newOrder.length) return;

    const temp = newOrder[index];
    newOrder[index] = newOrder[targetIdx];
    newOrder[targetIdx] = temp;

    setDashboardConfig({
      ...dashboardConfig,
      cardsOrder: newOrder,
    });
  };

  const getCardLabel = (card: DashboardCardType) => {
    switch (card) {
      case DashboardCardType.DAILY_SUMMARY:
        return 'Daily Cashflow Summary & Bar Charts';
      case DashboardCardType.BUDGET_SUMMARY:
        return 'Category Budget & Donut Charts';
      case DashboardCardType.FAVORITE_ACCOUNTS:
        return 'Pinned Accounts & Balance Snapshot';
      case DashboardCardType.CALENDAR_SUMMARY:
        return 'Monthly Calendar Activity Heatmap';
      case DashboardCardType.RECENT_TRANSACTIONS:
        return 'Recent Ledger Entries';
      case DashboardCardType.PAYMENT_SOURCE_PREVIEW:
        return 'Payment Source Shortfall / Surplus Card';
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 backdrop-blur-xs p-4 animate-in fade-in duration-150">
      <div className="w-full max-w-md bg-white rounded-3xl shadow-2xl border border-slate-200 overflow-hidden flex flex-col max-h-[85vh]">
        {/* Header */}
        <div className="px-5 py-4 border-b border-slate-100 flex items-center justify-between bg-slate-50/70">
          <div>
            <h2 className="font-bold text-slate-900 text-base">
              {LanguageHelper.getString('customize_cards', languageMode)}
            </h2>
            <p className="text-xs text-slate-500">Enable, disable, or reorder dashboard widgets</p>
          </div>
          <button onClick={onClose} className="p-1.5 rounded-xl text-slate-400 hover:text-slate-700">
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Content */}
        <div className="p-5 overflow-y-auto space-y-4">
          {/* Card Reordering */}
          <div className="space-y-2">
            {dashboardConfig.cardsOrder.map((card, idx) => {
              const isEnabled = dashboardConfig.enabledCards.includes(card);
              return (
                <div
                  key={card}
                  className={`p-3 rounded-2xl border flex items-center justify-between text-xs transition-colors ${
                    isEnabled ? 'bg-white border-slate-200 shadow-2xs' : 'bg-slate-50 border-slate-200/50 opacity-60'
                  }`}
                >
                  <span className="font-semibold text-slate-800">{getCardLabel(card)}</span>

                  <div className="flex items-center gap-1">
                    <button
                      onClick={() => moveCard(idx, 'up')}
                      disabled={idx === 0}
                      className="p-1 text-slate-400 hover:text-slate-700 disabled:opacity-30"
                    >
                      <ArrowUp className="w-3.5 h-3.5" />
                    </button>
                    <button
                      onClick={() => moveCard(idx, 'down')}
                      disabled={idx === dashboardConfig.cardsOrder.length - 1}
                      className="p-1 text-slate-400 hover:text-slate-700 disabled:opacity-30"
                    >
                      <ArrowDown className="w-3.5 h-3.5" />
                    </button>
                    <button
                      onClick={() => toggleCard(card)}
                      className={`p-1 rounded-lg ml-1 ${
                        isEnabled ? 'text-emerald-600 hover:bg-emerald-50' : 'text-slate-400 hover:bg-slate-200'
                      }`}
                    >
                      {isEnabled ? <Eye className="w-4 h-4" /> : <EyeOff className="w-4 h-4" />}
                    </button>
                  </div>
                </div>
              );
            })}
          </div>

          {/* Quick Chart Options */}
          <div className="pt-2 border-t border-slate-100 space-y-3">
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">
                Budget Chart Display
              </label>
              <div className="grid grid-cols-2 gap-2 bg-slate-100 p-1 rounded-xl text-xs font-bold">
                <button
                  onClick={() =>
                    setDashboardConfig({
                      ...dashboardConfig,
                      budgetChartShape: BudgetChartShape.DONUT,
                    })
                  }
                  className={`py-1.5 rounded-lg transition-all ${
                    dashboardConfig.budgetChartShape === BudgetChartShape.DONUT
                      ? 'bg-white text-slate-900 shadow-2xs'
                      : 'text-slate-600'
                  }`}
                >
                  Donut Ring
                </button>
                <button
                  onClick={() =>
                    setDashboardConfig({
                      ...dashboardConfig,
                      budgetChartShape: BudgetChartShape.PIE,
                    })
                  }
                  className={`py-1.5 rounded-lg transition-all ${
                    dashboardConfig.budgetChartShape === BudgetChartShape.PIE
                      ? 'bg-white text-slate-900 shadow-2xs'
                      : 'text-slate-600'
                  }`}
                >
                  Solid Pie
                </button>
              </div>
            </div>
          </div>
        </div>

        {/* Footer */}
        <div className="p-4 border-t border-slate-100 bg-slate-50 flex justify-end">
          <button
            onClick={onClose}
            className="px-6 py-2 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-xs shadow-xs"
          >
            {LanguageHelper.getString('done', languageMode)}
          </button>
        </div>
      </div>
    </div>
  );
};
