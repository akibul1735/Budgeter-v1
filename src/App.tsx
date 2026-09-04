import React, { useState } from 'react';
import { BudgetProvider, useBudget } from './context/BudgetContext';
import { AppTab, Account, Category } from './types';
import { Header } from './components/Header';
import { Navigation } from './components/Navigation';
import { DashboardScreen } from './screens/DashboardScreen';
import { LedgerScreen } from './screens/LedgerScreen';
import { PaymentSourceScreen } from './screens/PaymentSourceScreen';
import { BalanceSheetScreen } from './screens/BalanceSheetScreen';
import { BudgetScreen } from './screens/BudgetScreen';
import { BudgetMakerScreen } from './screens/BudgetMakerScreen';
import { NetEarningsScreen } from './screens/NetEarningsScreen';
import { RemindersScreen } from './screens/RemindersScreen';
import { AccountsScreen } from './screens/AccountsScreen';
import { CategoriesScreen } from './screens/CategoriesScreen';
import { AccountCalculationScreen } from './screens/AccountCalculationScreen';
import { LabelsScreen } from './screens/LabelsScreen';
import { ItemsSummaryScreen } from './screens/ItemsSummaryScreen';
import { BackupSyncScreen } from './screens/BackupSyncScreen';
import { SettingsScreen } from './screens/SettingsScreen';

// Modals
import { AddTransactionModal } from './components/AddTransactionModal';
import { AddAccountModal } from './components/AddAccountModal';
import { AddCategoryModal } from './components/AddCategoryModal';
import { CalculatorModal } from './components/CalculatorModal';
import { TabCustomizerModal } from './components/TabCustomizerModal';
import { ThemeFontModal } from './components/ThemeFontModal';
import { AutofillSettingsModal } from './components/AutofillSettingsModal';
import { DashboardCustomizerModal } from './components/DashboardCustomizerModal';

const AppContent: React.FC = () => {
  const { currentTab, setCurrentTab } = useBudget();

  // Navigation Drawer state
  const [isDrawerOpen, setIsDrawerOpen] = useState(false);

  // Modals state
  const [isTransactionModalOpen, setIsTransactionModalOpen] = useState(false);
  const [editingTransactionId, setEditingTransactionId] = useState<number | null>(null);

  const [isAccountModalOpen, setIsAccountModalOpen] = useState(false);
  const [editingAccount, setEditingAccount] = useState<Account | null>(null);

  const [isCategoryModalOpen, setIsCategoryModalOpen] = useState(false);
  const [editingCategory, setEditingCategory] = useState<Category | null>(null);

  const [isCalculatorOpen, setIsCalculatorOpen] = useState(false);
  const [isTabCustomizerOpen, setIsTabCustomizerOpen] = useState(false);
  const [isThemeFontOpen, setIsThemeFontOpen] = useState(false);
  const [isAutofillOpen, setIsAutofillOpen] = useState(false);
  const [isDashboardCustomizerOpen, setIsDashboardCustomizerOpen] = useState(false);

  // Handlers
  const handleOpenNewTransaction = () => {
    setEditingTransactionId(null);
    setIsTransactionModalOpen(true);
  };

  const handleOpenEditTransaction = (txId: number) => {
    setEditingTransactionId(txId);
    setIsTransactionModalOpen(true);
  };

  const handleOpenNewAccount = () => {
    setEditingAccount(null);
    setIsAccountModalOpen(true);
  };

  const handleOpenEditAccount = (acc: Account) => {
    setEditingAccount(acc);
    setIsAccountModalOpen(true);
  };

  const handleOpenNewCategory = () => {
    setEditingCategory(null);
    setIsCategoryModalOpen(true);
  };

  const handleOpenEditCategory = (cat: Category) => {
    setEditingCategory(cat);
    setIsCategoryModalOpen(true);
  };

  const renderActiveScreen = () => {
    switch (currentTab) {
      case AppTab.DASHBOARD:
        return (
          <DashboardScreen
            onOpenNewTransaction={handleOpenNewTransaction}
            onOpenEditTransaction={handleOpenEditTransaction}
            onOpenAccountDetail={(accId) => {
              setCurrentTab(AppTab.ACCOUNTS);
            }}
          />
        );
      case AppTab.TRANSACTIONS:
        return (
          <LedgerScreen
            onOpenNewTransaction={handleOpenNewTransaction}
            onOpenEditTransaction={handleOpenEditTransaction}
          />
        );
      case AppTab.PAYMENT_SOURCE:
        return <PaymentSourceScreen />;
      case AppTab.BALANCE_SHEET:
        return <BalanceSheetScreen />;
      case AppTab.BUDGET:
        return (
          <BudgetScreen
            onOpenNewTransaction={handleOpenNewTransaction}
            onOpenNewCategory={handleOpenNewCategory}
          />
        );
      case AppTab.BUDGET_MAKER:
        return <BudgetMakerScreen />;
      case AppTab.NET_EARNINGS:
        return <NetEarningsScreen />;
      case AppTab.REMINDERS:
        return <RemindersScreen />;
      case AppTab.ACCOUNTS:
        return (
          <AccountsScreen
            onOpenNewAccount={handleOpenNewAccount}
            onOpenEditAccount={handleOpenEditAccount}
          />
        );
      case AppTab.CATEGORIES:
        return (
          <CategoriesScreen
            onOpenNewCategory={handleOpenNewCategory}
            onOpenEditCategory={handleOpenEditCategory}
          />
        );
      case AppTab.ACCOUNT_CALCULATION:
        return <AccountCalculationScreen />;
      case AppTab.LABELS:
        return <LabelsScreen />;
      case AppTab.ITEMS_SUMMARY:
        return <ItemsSummaryScreen />;
      case AppTab.BACKUP_SYNC:
        return <BackupSyncScreen />;
      case AppTab.SETTINGS:
        return (
          <SettingsScreen
            onOpenThemeFontModal={() => setIsThemeFontOpen(true)}
            onOpenTabCustomizer={() => setIsTabCustomizerOpen(true)}
            onOpenAutofillModal={() => setIsAutofillOpen(true)}
            onOpenDashboardCustomizer={() => setIsDashboardCustomizerOpen(true)}
          />
        );
      default:
        return <DashboardScreen onOpenNewTransaction={handleOpenNewTransaction} onOpenEditTransaction={handleOpenEditTransaction} onOpenAccountDetail={() => {}} />;
    }
  };

  return (
    <div className="min-h-screen bg-slate-100/60 flex flex-col text-slate-900 selection:bg-emerald-500 selection:text-white font-sans antialiased">
      {/* Top Header */}
      <Header
        onOpenMenu={() => setIsDrawerOpen(true)}
        onOpenCalculator={() => setIsCalculatorOpen(true)}
        onOpenNewTransaction={handleOpenNewTransaction}
        onOpenTabCustomizer={() => setIsTabCustomizerOpen(true)}
        onOpenThemeFont={() => setIsThemeFontOpen(true)}
      />

      {/* Main App Container */}
      <div className="flex-1 flex max-w-7xl w-full mx-auto px-3 sm:px-6 pt-4">
        {/* Navigation (Sidebar Drawer or Bottom/Top Tabs) */}
        <Navigation
          isDrawerOpen={isDrawerOpen}
          onCloseDrawer={() => setIsDrawerOpen(false)}
          onOpenTabCustomizer={() => setIsTabCustomizerOpen(true)}
        />

        {/* Content Area */}
        <main className="flex-1 w-full min-w-0 transition-all duration-200">
          {renderActiveScreen()}
        </main>
      </div>

      {/* Modals & Dialogs */}
      <AddTransactionModal
        isOpen={isTransactionModalOpen}
        onClose={() => setIsTransactionModalOpen(false)}
        transactionIdToEdit={editingTransactionId}
      />

      <AddAccountModal
        isOpen={isAccountModalOpen}
        onClose={() => setIsAccountModalOpen(false)}
        accountToEdit={editingAccount}
      />

      <AddCategoryModal
        isOpen={isCategoryModalOpen}
        onClose={() => setIsCategoryModalOpen(false)}
        categoryToEdit={editingCategory}
      />

      <CalculatorModal
        isOpen={isCalculatorOpen}
        onClose={() => setIsCalculatorOpen(false)}
      />

      <TabCustomizerModal
        isOpen={isTabCustomizerOpen}
        onClose={() => setIsTabCustomizerOpen(false)}
      />

      <ThemeFontModal
        isOpen={isThemeFontOpen}
        onClose={() => setIsThemeFontOpen(false)}
      />

      <AutofillSettingsModal
        isOpen={isAutofillOpen}
        onClose={() => setIsAutofillOpen(false)}
      />

      <DashboardCustomizerModal
        isOpen={isDashboardCustomizerOpen}
        onClose={() => setIsDashboardCustomizerOpen(false)}
      />
    </div>
  );
};

export default function App() {
  return (
    <BudgetProvider>
      <AppContent />
    </BudgetProvider>
  );
}
