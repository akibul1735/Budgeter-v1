import React, { useState } from 'react';
import { HardDriveDownload, Upload, Download, RotateCcw, CheckCircle2, AlertTriangle } from 'lucide-react';
import { useBudget } from '../context/BudgetContext';
import { LanguageMode } from '../types';
import { LanguageHelper } from '../utils/languageHelper';

export const BackupSyncScreen: React.FC = () => {
  const { exportDataJson, importDataJson, resetToDefaults, languageMode } = useBudget();

  const [importStatus, setImportStatus] = useState<string | null>(null);

  const handleDownloadBackup = () => {
    const jsonStr = exportDataJson();
    const blob = new Blob([jsonStr], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `Budgeter_Backup_${new Date().toISOString().split('T')[0]}.json`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
  };

  const handleFileUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = (event) => {
      const content = event.target?.result as string;
      if (content) {
        const ok = importDataJson(content);
        if (ok) {
          setImportStatus('Backup restored successfully!');
        } else {
          setImportStatus('Failed to parse backup JSON file.');
        }
        setTimeout(() => setImportStatus(null), 4000);
      }
    };
    reader.readAsText(file);
  };

  const handleReset = () => {
    if (confirm('Warning: This will reset all accounts, transactions, and budgets to default seed state. Continue?')) {
      resetToDefaults();
      setImportStatus('Data reset to defaults successfully.');
      setTimeout(() => setImportStatus(null), 4000);
    }
  };

  return (
    <div className="space-y-4 sm:space-y-6 pb-20">
      {importStatus && (
        <div className="p-3 bg-emerald-50 border border-emerald-200 text-emerald-800 rounded-2xl text-xs font-semibold flex items-center gap-2">
          <CheckCircle2 className="w-4 h-4 text-emerald-600" />
          <span>{importStatus}</span>
        </div>
      )}

      {/* Header Banner */}
      <div className="p-6 bg-white rounded-3xl border border-slate-200/80 shadow-xs">
        <h2 className="text-lg font-bold text-slate-900 flex items-center gap-2">
          <HardDriveDownload className="w-5 h-5 text-emerald-600" />
          <span>{LanguageHelper.getString('backup_sync', languageMode)}</span>
        </h2>
        <p className="text-xs text-slate-500 mt-0.5">
          Export your complete double-entry database or restore previous backups
        </p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {/* Export Card */}
        <div className="p-6 bg-white rounded-3xl border border-slate-200/80 shadow-xs flex flex-col justify-between space-y-4">
          <div>
            <h3 className="font-bold text-slate-900 text-base flex items-center gap-2">
              <Download className="w-5 h-5 text-blue-600" />
              <span>Export JSON Backup</span>
            </h3>
            <p className="text-xs text-slate-500 mt-1">
              Save all accounts, journal transactions, budgets, and recurring rules to a portable JSON file.
            </p>
          </div>

          <button
            onClick={handleDownloadBackup}
            className="w-full py-3 bg-blue-600 hover:bg-blue-700 text-white font-bold text-xs rounded-2xl flex items-center justify-center gap-2 shadow-sm transition-all"
          >
            <Download className="w-4 h-4" />
            <span>Download Database Backup</span>
          </button>
        </div>

        {/* Import Card */}
        <div className="p-6 bg-white rounded-3xl border border-slate-200/80 shadow-xs flex flex-col justify-between space-y-4">
          <div>
            <h3 className="font-bold text-slate-900 text-base flex items-center gap-2">
              <Upload className="w-5 h-5 text-emerald-600" />
              <span>Restore from Backup</span>
            </h3>
            <p className="text-xs text-slate-500 mt-1">
              Upload a previously exported JSON backup file to restore your entire database.
            </p>
          </div>

          <label className="w-full py-3 bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-xs rounded-2xl flex items-center justify-center gap-2 shadow-sm transition-all cursor-pointer">
            <Upload className="w-4 h-4" />
            <span>Choose Backup File (.json)</span>
            <input type="file" accept=".json" onChange={handleFileUpload} className="hidden" />
          </label>
        </div>
      </div>

      {/* Danger Zone: Reset to Factory Defaults */}
      <div className="p-6 bg-rose-50/50 rounded-3xl border border-rose-200/80 space-y-3">
        <div className="flex items-center gap-2 text-rose-900 font-bold text-sm">
          <AlertTriangle className="w-4 h-4 text-rose-600" />
          <span>Reset to Factory Defaults</span>
        </div>
        <p className="text-xs text-rose-800/80">
          Erase all custom records and reload the default starter accounts and demo transactions.
        </p>
        <button
          onClick={handleReset}
          className="px-4 py-2 bg-rose-600 hover:bg-rose-700 text-white font-bold text-xs rounded-xl flex items-center gap-1.5 shadow-2xs transition-all"
        >
          <RotateCcw className="w-3.5 h-3.5" />
          <span>Reset All Data</span>
        </button>
      </div>
    </div>
  );
};
