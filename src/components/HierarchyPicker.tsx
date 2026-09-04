import React, { useState, useRef, useEffect } from 'react';
import { ChevronDown, Search, Check, Tag, Building2, FolderTree } from 'lucide-react';
import { HierarchyDisplayMode, LanguageMode } from '../types';
import { LanguageHelper } from '../utils/languageHelper';

export interface HierarchyItem {
  id: number;
  nameEn: string;
  nameBn?: string;
  parentId?: number | null;
  accountRole?: string;
  type?: string;
  iconName?: string;
  colorHex?: string;
}

interface HierarchyPickerProps {
  id?: string;
  label: string;
  selectedId: number | null;
  items: HierarchyItem[];
  allGroups?: HierarchyItem[];
  onChange: (id: number) => void;
  hierarchyMode: HierarchyDisplayMode;
  languageMode: LanguageMode;
  placeholder?: string;
  isAccount?: boolean;
  required?: boolean;
  icon?: React.ReactNode;
}

export const HierarchyPicker: React.FC<HierarchyPickerProps> = ({
  id,
  label,
  selectedId,
  items,
  allGroups = [],
  onChange,
  hierarchyMode,
  languageMode,
  placeholder = '-- Select --',
  isAccount = false,
  required = false,
  icon,
}) => {
  const [isOpen, setIsOpen] = useState<boolean>(false);
  const [searchQuery, setSearchQuery] = useState<string>('');
  const dropdownRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        setIsOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const selectedItem = items.find((i) => i.id === selectedId);

  const getDisplayDetails = (item: HierarchyItem) => {
    if (isAccount) {
      const hierarchy = LanguageHelper.getAccountHierarchy(item, allGroups.length > 0 ? allGroups : items, languageMode);
      return hierarchy;
    } else {
      const hierarchy = LanguageHelper.getCategoryHierarchy(item, allGroups.length > 0 ? allGroups : items, languageMode);
      return hierarchy;
    }
  };

  const selectedDetails = selectedItem ? getDisplayDetails(selectedItem) : null;

  const filteredItems = items.filter((item) => {
    if (!searchQuery.trim()) return true;
    const q = searchQuery.toLowerCase();
    const details = getDisplayDetails(item);
    return (
      details.groupName.toLowerCase().includes(q) ||
      (isAccount ? details.accountName : details.categoryName).toLowerCase().includes(q) ||
      item.nameEn.toLowerCase().includes(q) ||
      (item.nameBn && item.nameBn.toLowerCase().includes(q))
    );
  });

  return (
    <div className="relative" ref={dropdownRef}>
      <label className="block text-xs font-semibold text-slate-700 mb-1">
        {label} {required && <span className="text-rose-500">*</span>}
      </label>

      {/* Trigger Button */}
      <button
        type="button"
        id={id}
        onClick={() => setIsOpen((prev) => !prev)}
        className="w-full text-left px-3 py-2 bg-slate-50 hover:bg-slate-100/80 border border-slate-200 rounded-2xl flex items-center justify-between gap-2 transition-all focus:outline-none focus:ring-2 focus:ring-emerald-500/30"
      >
        <div className="flex items-center gap-2.5 min-w-0 flex-1">
          {icon ? (
            <div className="w-7 h-7 rounded-xl bg-white border border-slate-200/80 flex items-center justify-center shrink-0 text-slate-700">
              {icon}
            </div>
          ) : (
            <div
              className="w-7 h-7 rounded-xl flex items-center justify-center shrink-0 text-white font-bold text-xs"
              style={{ backgroundColor: selectedItem?.colorHex || '#64748b' }}
            >
              {isAccount ? (
                <Building2 className="w-3.5 h-3.5" />
              ) : (
                <FolderTree className="w-3.5 h-3.5" />
              )}
            </div>
          )}

          <div className="min-w-0 flex-1">
            {selectedItem && selectedDetails ? (
              hierarchyMode === HierarchyDisplayMode.DOUBLE_LINE ? (
                <div className="leading-tight">
                  <div className="text-[11px] font-medium text-slate-600 flex items-center gap-0.5 truncate">
                    <span>&gt;{selectedDetails.groupName}</span>
                  </div>
                  <div className="text-xs sm:text-sm font-bold text-slate-900 truncate">
                    {isAccount ? selectedDetails.accountName : selectedDetails.categoryName}
                  </div>
                </div>
              ) : (
                <div className="text-xs sm:text-sm font-bold text-slate-900 truncate">
                  {selectedDetails.singleLine}
                </div>
              )
            ) : (
              <span className="text-xs sm:text-sm text-slate-400 font-medium">{placeholder}</span>
            )}
          </div>
        </div>

        <ChevronDown
          className={`w-4 h-4 text-slate-400 shrink-0 transition-transform ${
            isOpen ? 'rotate-180' : ''
          }`}
        />
      </button>

      {/* Dropdown Menu */}
      {isOpen && (
        <div className="absolute z-50 left-0 right-0 top-full mt-1.5 bg-white border border-slate-200 rounded-2xl shadow-xl overflow-hidden max-h-72 flex flex-col animate-in fade-in zoom-in-95 duration-100">
          {/* Search Box */}
          <div className="p-2 border-b border-slate-100 bg-slate-50/80">
            <div className="relative">
              <Search className="w-3.5 h-3.5 text-slate-400 absolute left-2.5 top-1/2 -translate-y-1/2" />
              <input
                type="text"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                placeholder="Search..."
                className="w-full pl-8 pr-3 py-1.5 bg-white border border-slate-200 rounded-xl text-xs text-slate-900 focus:outline-none focus:ring-1 focus:ring-emerald-500"
                autoFocus
              />
            </div>
          </div>

          {/* List Items */}
          <div className="overflow-y-auto flex-1 p-1 space-y-0.5 divide-y divide-slate-50">
            {filteredItems.length === 0 ? (
              <div className="p-4 text-center text-xs text-slate-400">No items found</div>
            ) : (
              filteredItems.map((item) => {
                const isSelected = item.id === selectedId;
                const details = getDisplayDetails(item);

                return (
                  <button
                    key={item.id}
                    type="button"
                    onClick={() => {
                      onChange(item.id);
                      setIsOpen(false);
                      setSearchQuery('');
                    }}
                    className={`w-full text-left p-2 rounded-xl flex items-center justify-between gap-2 transition-colors ${
                      isSelected
                        ? 'bg-emerald-50 text-emerald-950 font-medium'
                        : 'hover:bg-slate-50 text-slate-800'
                    }`}
                  >
                    <div className="flex items-center gap-2.5 min-w-0 flex-1">
                      <div
                        className="w-6 h-6 rounded-lg flex items-center justify-center shrink-0 text-white text-[10px] font-bold"
                        style={{ backgroundColor: item.colorHex || '#64748b' }}
                      >
                        {isAccount ? <Building2 className="w-3 h-3" /> : <Tag className="w-3 h-3" />}
                      </div>

                      <div className="min-w-0 flex-1">
                        {hierarchyMode === HierarchyDisplayMode.DOUBLE_LINE ? (
                          <div>
                            <div className="text-[10px] font-medium text-slate-600 truncate">
                              &gt;{details.groupName}
                            </div>
                            <div className="text-xs font-bold text-slate-900 truncate">
                              {isAccount ? details.accountName : details.categoryName}
                            </div>
                          </div>
                        ) : (
                          <div className="text-xs font-semibold text-slate-900 truncate">
                            {details.singleLine}
                          </div>
                        )}
                      </div>
                    </div>

                    {isSelected && <Check className="w-4 h-4 text-emerald-600 shrink-0" />}
                  </button>
                );
              })
            )}
          </div>
        </div>
      )}
    </div>
  );
};
