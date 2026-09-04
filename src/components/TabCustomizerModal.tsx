import React from 'react';
import { X, Check, ArrowUp, ArrowDown, Eye, EyeOff, Layout } from 'lucide-react';
import { useBudget } from '../context/BudgetContext';
import { AppTab, TabPosition, LanguageMode } from '../types';
import { LanguageHelper } from '../utils/languageHelper';
import { getTabIcon } from './Navigation';

interface TabCustomizerModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export const TabCustomizerModal: React.FC<TabCustomizerModalProps> = ({ isOpen, onClose }) => {
  const { tabConfig, setTabConfig, languageMode } = useBudget();

  if (!isOpen) return null;

  const toggleTab = (tab: AppTab) => {
    // Keep at least 2 tabs enabled
    if (tabConfig.enabledTabs.includes(tab)) {
      if (tabConfig.enabledTabs.length <= 2) {
        alert('You must keep at least two navigation tabs active.');
        return;
      }
      setTabConfig({
        ...tabConfig,
        enabledTabs: tabConfig.enabledTabs.filter((t) => t !== tab),
      });
    } else {
      setTabConfig({
        ...tabConfig,
        enabledTabs: [...tabConfig.enabledTabs, tab],
      });
    }
  };

  const moveTab = (index: number, direction: 'up' | 'down') => {
    const newOrder = [...tabConfig.allTabsOrder];
    const targetIdx = direction === 'up' ? index - 1 : index + 1;
    if (targetIdx < 0 || targetIdx >= newOrder.length) return;

    const temp = newOrder[index];
    newOrder[index] = newOrder[targetIdx];
    newOrder[targetIdx] = temp;

    setTabConfig({
      ...tabConfig,
      allTabsOrder: newOrder,
    });
  };

  const setPosition = (pos: TabPosition) => {
    setTabConfig({
      ...tabConfig,
      position: pos,
    });
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 backdrop-blur-xs p-4 animate-in fade-in duration-150">
      <div className="w-full max-w-md bg-white rounded-3xl shadow-2xl border border-slate-200 overflow-hidden flex flex-col max-h-[85vh]">
        {/* Header */}
        <div className="px-5 py-4 border-b border-slate-100 flex items-center justify-between bg-slate-50/70">
          <div>
            <h2 className="font-bold text-slate-900 text-base">
              {LanguageHelper.getString('tab_customization', languageMode)}
            </h2>
            <p className="text-xs text-slate-500">Configure visible tabs & navigation position</p>
          </div>
          <button onClick={onClose} className="p-1.5 rounded-xl text-slate-400 hover:text-slate-700">
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Content */}
        <div className="p-5 overflow-y-auto space-y-4">
          {/* Position Selector */}
          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1.5">
              {LanguageHelper.getString('tab_position', languageMode)}
            </label>
            <div className="grid grid-cols-2 gap-2 bg-slate-100 p-1 rounded-2xl">
              <button
                type="button"
                onClick={() => setPosition(TabPosition.BOTTOM)}
                className={`py-2 rounded-xl text-xs font-bold transition-all ${
                  tabConfig.position === TabPosition.BOTTOM
                    ? 'bg-emerald-600 text-white shadow-xs'
                    : 'text-slate-600 hover:text-slate-900'
                }`}
              >
                {LanguageHelper.getString('tab_position_bottom', languageMode)}
              </button>

              <button
                type="button"
                onClick={() => setPosition(TabPosition.TOP)}
                className={`py-2 rounded-xl text-xs font-bold transition-all ${
                  tabConfig.position === TabPosition.TOP
                    ? 'bg-emerald-600 text-white shadow-xs'
                    : 'text-slate-600 hover:text-slate-900'
                }`}
              >
                {LanguageHelper.getString('tab_position_top', languageMode)}
              </button>
            </div>
          </div>

          {/* Tab Reordering and Visibility */}
          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-2">
              Tab Visibility & Order
            </label>
            <div className="space-y-1.5">
              {tabConfig.allTabsOrder.map((tab, idx) => {
                const Icon = getTabIcon(tab);
                const isEnabled = tabConfig.enabledTabs.includes(tab);
                return (
                  <div
                    key={tab}
                    className={`p-2.5 rounded-xl border flex items-center justify-between text-xs transition-colors ${
                      isEnabled ? 'bg-white border-slate-200' : 'bg-slate-50 border-slate-200/60 opacity-60'
                    }`}
                  >
                    <div className="flex items-center gap-2.5">
                      <Icon className="w-4 h-4 text-slate-600" />
                      <span className="font-semibold text-slate-800">{tab}</span>
                    </div>

                    <div className="flex items-center gap-1">
                      <button
                        onClick={() => moveTab(idx, 'up')}
                        disabled={idx === 0}
                        className="p-1 text-slate-400 hover:text-slate-700 disabled:opacity-30"
                        title="Move Up"
                      >
                        <ArrowUp className="w-3.5 h-3.5" />
                      </button>
                      <button
                        onClick={() => moveTab(idx, 'down')}
                        disabled={idx === tabConfig.allTabsOrder.length - 1}
                        className="p-1 text-slate-400 hover:text-slate-700 disabled:opacity-30"
                        title="Move Down"
                      >
                        <ArrowDown className="w-3.5 h-3.5" />
                      </button>
                      <button
                        onClick={() => toggleTab(tab)}
                        className={`p-1 rounded-lg ml-1 ${
                          isEnabled ? 'text-emerald-600 hover:bg-emerald-50' : 'text-slate-400 hover:bg-slate-200'
                        }`}
                        title={isEnabled ? 'Hide Tab' : 'Show Tab'}
                      >
                        {isEnabled ? <Eye className="w-4 h-4" /> : <EyeOff className="w-4 h-4" />}
                      </button>
                    </div>
                  </div>
                );
              })}
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
