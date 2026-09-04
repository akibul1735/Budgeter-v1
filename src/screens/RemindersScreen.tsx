import React, { useState } from 'react';
import {
  BellRing,
  Plus,
  CheckCircle2,
  Calendar,
  AlertCircle,
  X,
  Trash2,
} from 'lucide-react';
import { useBudget } from '../context/BudgetContext';
import { BillFrequency, RecurringBill, LanguageMode } from '../types';
import { LanguageHelper } from '../utils/languageHelper';

export const RemindersScreen: React.FC = () => {
  const {
    recurringBills,
    addRecurringBill,
    deleteRecurringBill,
    markBillAsPaid,
    accounts,
    categories,
    languageMode,
  } = useBudget();

  const [showAddModal, setShowAddModal] = useState(false);
  const [title, setTitle] = useState('');
  const [amountStr, setAmountStr] = useState('');
  const [frequency, setFrequency] = useState<BillFrequency>(BillFrequency.MONTHLY);
  const [nextDueDate, setNextDueDate] = useState(() => new Date().toISOString().split('T')[0]);
  const [accountId, setAccountId] = useState<number | null>(null);
  const [categoryId, setCategoryId] = useState<number | null>(null);
  const [isAutoPaid, setIsAutoPaid] = useState(false);

  const handleAddBill = (e: React.FormEvent) => {
    e.preventDefault();
    const amount = parseFloat(amountStr);
    if (!title.trim() || isNaN(amount) || amount <= 0) {
      alert('Please provide a valid bill title and amount');
      return;
    }

    addRecurringBill({
      title: title.trim(),
      amount,
      frequency,
      nextDueDateEpochMs: new Date(nextDueDate).getTime() || Date.now(),
      accountId,
      categoryId,
      isAutoPaid,
      isActive: true,
      reminderDaysBefore: 3,
    });

    setTitle('');
    setAmountStr('');
    setShowAddModal(false);
  };

  return (
    <div className="space-y-4 sm:space-y-6 pb-20">
      {/* Header Banner */}
      <div className="p-6 bg-white rounded-3xl border border-slate-200/80 shadow-xs flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-lg font-bold text-slate-900 flex items-center gap-2">
            <BellRing className="w-5 h-5 text-emerald-600" />
            <span>{LanguageHelper.getString('reminders', languageMode)}</span>
          </h2>
          <p className="text-xs text-slate-500">
            Keep track of recurring bills, dues, and automated payment triggers
          </p>
        </div>

        <button
          onClick={() => setShowAddModal(true)}
          className="px-4 py-2 bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-xs rounded-2xl flex items-center gap-1.5 shadow-sm active:scale-95 transition-all"
        >
          <Plus className="w-4 h-4" />
          <span>New Reminder</span>
        </button>
      </div>

      {/* Bill List */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {recurringBills.map((bill) => {
          const dueDate = new Date(bill.nextDueDateEpochMs);
          const isOverdue = dueDate.getTime() < Date.now();
          const acc = accounts.find((a) => a.id === bill.accountId);
          const cat = categories.find((c) => c.id === bill.categoryId);

          return (
            <div
              key={bill.id}
              className="p-5 bg-white rounded-3xl border border-slate-200/80 shadow-xs flex flex-col justify-between space-y-4"
            >
              <div>
                <div className="flex items-center justify-between">
                  <span className="font-bold text-slate-900 text-sm">{bill.title}</span>
                  <span className="text-base font-extrabold font-mono text-slate-900">
                    {LanguageHelper.formatCurrency(bill.amount, languageMode)}
                  </span>
                </div>

                <div className="mt-2 space-y-1 text-xs text-slate-500">
                  <div className="flex items-center gap-1.5">
                    <Calendar className="w-3.5 h-3.5 text-slate-400" />
                    <span className={isOverdue ? 'text-rose-600 font-bold' : ''}>
                      Due: {dueDate.toLocaleDateString()} ({bill.frequency.toLowerCase()})
                    </span>
                  </div>

                  {acc && <div>Account: {acc.nameEn}</div>}
                  {cat && <div>Category: {cat.nameEn}</div>}
                </div>
              </div>

              {/* Actions */}
              <div className="pt-3 border-t border-slate-100 flex items-center justify-between">
                <button
                  onClick={() => deleteRecurringBill(bill.id)}
                  className="text-rose-500 hover:text-rose-700 p-1 rounded-lg"
                  title="Delete Reminder"
                >
                  <Trash2 className="w-4 h-4" />
                </button>

                <button
                  onClick={() => markBillAsPaid(bill.id)}
                  className="px-4 py-2 bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-xs rounded-xl flex items-center gap-1.5 shadow-2xs active:scale-95 transition-all"
                >
                  <CheckCircle2 className="w-4 h-4" />
                  <span>Mark as Paid</span>
                </button>
              </div>
            </div>
          );
        })}
      </div>

      {/* Add Reminder Modal */}
      {showAddModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 backdrop-blur-xs p-4 animate-in fade-in duration-150">
          <div className="w-full max-w-md bg-white rounded-3xl shadow-2xl border border-slate-200 overflow-hidden flex flex-col">
            <div className="px-5 py-4 border-b border-slate-100 flex items-center justify-between bg-slate-50/70">
              <h3 className="font-bold text-slate-900 text-sm">Add Recurring Bill</h3>
              <button onClick={() => setShowAddModal(false)} className="p-1 text-slate-400 hover:text-slate-700">
                <X className="w-5 h-5" />
              </button>
            </div>

            <form onSubmit={handleAddBill} className="p-5 space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">Title *</label>
                <input
                  type="text"
                  value={title}
                  onChange={(e) => setTitle(e.target.value)}
                  placeholder="e.g. WiFi Bill, Netflix, House Rent"
                  required
                  className="w-full px-3.5 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-xs text-slate-900 focus:bg-white focus:outline-none focus:ring-1 focus:ring-emerald-500"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">Amount *</label>
                <input
                  type="number"
                  step="any"
                  value={amountStr}
                  onChange={(e) => setAmountStr(e.target.value)}
                  placeholder="0.00"
                  required
                  className="w-full px-3.5 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-xs font-bold text-slate-900 focus:bg-white focus:outline-none"
                />
              </div>

              <div className="grid grid-cols-2 gap-2">
                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">Frequency</label>
                  <select
                    value={frequency}
                    onChange={(e) => setFrequency(e.target.value as BillFrequency)}
                    className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl text-xs text-slate-900"
                  >
                    <option value={BillFrequency.WEEKLY}>Weekly</option>
                    <option value={BillFrequency.BI_WEEKLY}>Bi-Weekly</option>
                    <option value={BillFrequency.MONTHLY}>Monthly</option>
                    <option value={BillFrequency.QUARTERLY}>Quarterly</option>
                    <option value={BillFrequency.YEARLY}>Yearly</option>
                  </select>
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">Next Due Date</label>
                  <input
                    type="date"
                    value={nextDueDate}
                    onChange={(e) => setNextDueDate(e.target.value)}
                    className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl text-xs text-slate-900"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">Payment Source Account</label>
                <select
                  value={accountId || ''}
                  onChange={(e) => setAccountId(Number(e.target.value) || null)}
                  className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl text-xs text-slate-900"
                >
                  <option value="">-- Choose Account --</option>
                  {accounts.map((a) => (
                    <option key={a.id} value={a.id}>
                      {a.nameEn}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">Category</label>
                <select
                  value={categoryId || ''}
                  onChange={(e) => setCategoryId(Number(e.target.value) || null)}
                  className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl text-xs text-slate-900"
                >
                  <option value="">-- Choose Category --</option>
                  {categories.map((c) => (
                    <option key={c.id} value={c.id}>
                      {c.nameEn}
                    </option>
                  ))}
                </select>
              </div>

              <div className="pt-3 flex justify-end gap-2 border-t border-slate-100">
                <button
                  type="button"
                  onClick={() => setShowAddModal(false)}
                  className="px-4 py-2 bg-slate-100 hover:bg-slate-200 rounded-xl text-xs font-bold text-slate-700"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-5 py-2 bg-emerald-600 hover:bg-emerald-700 rounded-xl text-xs font-bold text-white shadow-xs"
                >
                  Save Reminder
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
