package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Account
import com.example.data.model.AccountType
import com.example.data.model.Category
import com.example.data.model.CategoryType
import com.example.data.model.LanguageMode
import com.example.data.model.MonthlyBudget
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.data.model.TransactionWithDetails
import com.example.data.repository.AccountWithBalance
import com.example.data.repository.FinancialOverview
import com.example.ui.components.ClickableAmountText
import com.example.ui.components.DoubleEntryFlowBadge
import com.example.ui.components.PopupCalculatorDialog
import com.example.ui.dialogs.AmountBreakdownDialog
import com.example.ui.dialogs.AmountDetailInfo
import com.example.ui.dialogs.BreakdownItem
import com.example.ui.dialogs.BudgetSummaryPreviewDialog
import com.example.ui.dialogs.DailySummaryDetailDialog
import com.example.ui.dialogs.FormulaStep
import com.example.ui.screens.dashboard.BudgetSummaryCard
import com.example.ui.screens.dashboard.BudgetSummarySettingsDialog
import com.example.ui.screens.dashboard.CalendarSettingsDialog
import com.example.ui.screens.dashboard.CalendarSummaryCard
import com.example.ui.screens.dashboard.CustomizeDashboardCardsDialog
import com.example.ui.screens.dashboard.DailySummaryCard
import com.example.ui.screens.dashboard.DailySummarySettingsDialog
import com.example.ui.screens.dashboard.FavoriteAccountsCard
import com.example.ui.screens.dashboard.FavoriteAccountsSelectionDialog
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidExpenseContainer
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidIncomeContainer
import com.example.ui.theme.SolidOnExpenseContainer
import com.example.ui.theme.SolidOnIncomeContainer
import com.example.ui.theme.SolidPrimary
import com.example.ui.theme.SolidPrimaryContainer
import com.example.ui.theme.SolidTransfer
import com.example.util.BudgetChartShape
import com.example.util.BudgetSummaryType
import com.example.util.CalendarDisplayMode
import com.example.util.DailySummaryMode
import com.example.util.DailySummaryPeriod
import com.example.util.DashboardCardType
import com.example.util.DashboardConfig
import com.example.util.DateUtils
import com.example.util.IconHelper
import com.example.util.LanguageHelper
import java.util.Calendar

@Composable
fun DashboardScreen(
    overview: FinancialOverview,
    accountsWithBalances: List<AccountWithBalance>,
    recentTransactions: List<TransactionWithDetails>,
    allCategories: List<Category> = emptyList(),
    monthlyBudgets: List<MonthlyBudget> = emptyList(),
    dashboardConfig: DashboardConfig,
    languageMode: LanguageMode,
    isDemoMode: Boolean = false,
    onOpenDrawer: () -> Unit = {},
    onExitDemoMode: () -> Unit = {},
    onAddTransactionClick: (TransactionType) -> Unit,
    onTransactionClick: (Transaction) -> Unit,
    onViewAllTransactionsClick: () -> Unit,
    onAccountClick: (Account) -> Unit = {},
    onToggleCardVisibility: (DashboardCardType, Boolean) -> Unit,
    onReorderCards: (fromIndex: Int, toIndex: Int) -> Unit,
    onUpdateDailySummarySettings: (DailySummaryMode, DailySummaryPeriod, Boolean, Boolean) -> Unit,
    onUpdateBudgetSummarySettings: (BudgetChartShape, BudgetSummaryType, Int, Boolean, Boolean) -> Unit,
    onUpdateCalendarSettings: (CalendarDisplayMode, Boolean, Boolean) -> Unit,
    onUpdateFavoriteAccounts: (Set<Long>) -> Unit,
    onResetDashboardDefaults: () -> Unit
) {
    var showStandAloneCalculator by remember { mutableStateOf(false) }
    var showCustomizeCardsDialog by remember { mutableStateOf(false) }
    var showDailySettingsDialog by remember { mutableStateOf(false) }
    var showBudgetSettingsDialog by remember { mutableStateOf(false) }
    var showCalendarSettingsDialog by remember { mutableStateOf(false) }
    var showFavoriteAccountsPicker by remember { mutableStateOf(false) }
    var showDailySummaryDetail by remember { mutableStateOf(false) }
    var showBudgetSummaryPreview by remember { mutableStateOf(false) }

    // State for clicking any amount anywhere on the dashboard to view formula and transactions breakdown
    var activeAmountDetail by remember { mutableStateOf<AmountDetailInfo?>(null) }

    // Helper functions for building calculation breakdowns
    fun openNetWorthBreakdown() {
        val assetAccounts = accountsWithBalances.filter { it.account.type == AccountType.ASSET }
        val liabilityAccounts = accountsWithBalances.filter { it.account.type == AccountType.LIABILITY }

        val steps = listOf(
            FormulaStep(
                label = if (languageMode == LanguageMode.BANGLA) "মোট সম্পদ (ক্যাশ, ব্যাংক, ওয়ালেট)" else "Total Assets (Cash, Bank, Wallets)",
                amount = overview.totalAssets,
                operator = "+",
                note = "${assetAccounts.size} accounts"
            ),
            FormulaStep(
                label = if (languageMode == LanguageMode.BANGLA) "মোট দায় (ক্রেডিট কার্ড, ঋণ, বকেয়া)" else "Total Liabilities (Cards, Loans, Payables)",
                amount = overview.totalLiabilities,
                operator = "−",
                note = "${liabilityAccounts.size} accounts"
            ),
            FormulaStep(
                label = if (languageMode == LanguageMode.BANGLA) "নিট সম্পদ (সম্পদ − দায়)" else "Net Worth (Assets − Liabilities)",
                amount = overview.netWorth,
                operator = "=",
                isHighlighted = true
            )
        )

        val breakdown = accountsWithBalances.map { accItem ->
            val isAsset = accItem.account.type == AccountType.ASSET
            BreakdownItem(
                name = accItem.account.localizedName(languageMode) + if (isAsset) " (Asset)" else " (Liability)",
                amount = accItem.currentBalance,
                iconName = accItem.account.iconName,
                color = if (isAsset) SolidIncome else SolidExpense
            )
        }

        activeAmountDetail = AmountDetailInfo(
            title = if (languageMode == LanguageMode.BANGLA) "নিট সম্পদ হিসাব" else "Net Worth Calculation",
            subtitle = if (languageMode == LanguageMode.BANGLA) "মোট সম্পদ থেকে মোট দায় বিয়োগ" else "Total Assets minus Total Liabilities",
            totalAmount = overview.netWorth,
            formulaExplanation = if (languageMode == LanguageMode.BANGLA)
                "নিট সম্পদ হলো আপনার সমস্ত সম্পদ (নগদ টাকা, ব্যাংক একাউন্ট, মোবাইল ব্যালেন্স) থেকে সমস্ত দেনা বা দায়ের (ঋণ, ক্রেডিট কার্ড) বিয়োগফল।"
            else
                "Net Worth measures your total financial health: the value of everything you own (Assets) minus everything you owe (Liabilities).",
            formulaSteps = steps,
            relatedBreakdownItems = breakdown,
            relatedTransactions = recentTransactions,
            statusTag = if (overview.isLedgerBalanced) "Balanced" else "Ledger Check"
        )
    }

    fun openAssetsBreakdown() {
        val assetAccounts = accountsWithBalances.filter { it.account.type == AccountType.ASSET }
        val steps = assetAccounts.map {
            FormulaStep(
                label = it.account.localizedName(languageMode),
                amount = it.currentBalance,
                operator = "+"
            )
        }
        val breakdown = assetAccounts.map {
            BreakdownItem(
                name = it.account.localizedName(languageMode),
                amount = it.currentBalance,
                percentage = if (overview.totalAssets > 0) (it.currentBalance / overview.totalAssets) * 100.0 else 0.0,
                iconName = it.account.iconName,
                color = SolidIncome
            )
        }
        val assetTxs = recentTransactions.filter { txItem ->
            val debitIsAsset = assetAccounts.any { it.account.id == txItem.transaction.debitAccountId }
            val creditIsAsset = assetAccounts.any { it.account.id == txItem.transaction.creditAccountId }
            debitIsAsset || creditIsAsset
        }

        activeAmountDetail = AmountDetailInfo(
            title = if (languageMode == LanguageMode.BANGLA) "মোট সম্পদ ও একাউন্ট ব্যালেন্স" else "Total Assets & Accounts",
            subtitle = if (languageMode == LanguageMode.BANGLA) "সমস্ত অ্যাসেট একাউন্টের সমষ্টি" else "Sum of all active asset accounts",
            totalAmount = overview.totalAssets,
            formulaExplanation = if (languageMode == LanguageMode.BANGLA)
                "আপনার ক্যাশ, ব্যাংক ও মোবাইল ওয়ালেটের মোট বর্তমান ব্যালেন্সের যোগফল।"
            else
                "Combined liquid and fixed balances across all cash, bank accounts, savings, and mobile wallets.",
            formulaSteps = steps,
            relatedBreakdownItems = breakdown,
            relatedTransactions = assetTxs
        )
    }

    fun openLiabilitiesBreakdown() {
        val liabilityAccounts = accountsWithBalances.filter { it.account.type == AccountType.LIABILITY }
        val steps = liabilityAccounts.map {
            FormulaStep(
                label = it.account.localizedName(languageMode),
                amount = it.currentBalance,
                operator = "+"
            )
        }
        val breakdown = liabilityAccounts.map {
            BreakdownItem(
                name = it.account.localizedName(languageMode),
                amount = it.currentBalance,
                iconName = it.account.iconName,
                color = SolidExpense
            )
        }
        val liabilityTxs = recentTransactions.filter { txItem ->
            val debitIsLiab = liabilityAccounts.any { it.account.id == txItem.transaction.debitAccountId }
            val creditIsLiab = liabilityAccounts.any { it.account.id == txItem.transaction.creditAccountId }
            debitIsLiab || creditIsLiab
        }

        activeAmountDetail = AmountDetailInfo(
            title = if (languageMode == LanguageMode.BANGLA) "মোট দায় ও ঋণ" else "Total Liabilities & Debts",
            subtitle = if (languageMode == LanguageMode.BANGLA) "ক্রেডিট কার্ড, ঋণ ও বকেয়ার সমষ্টি" else "Sum of all liability obligations",
            totalAmount = overview.totalLiabilities,
            formulaExplanation = if (languageMode == LanguageMode.BANGLA)
                "আপনার ক্রেডিট কার্ডের খরচ, ব্যক্তিগত ঋণ ও অন্যান্য বকেয়া দায়ের যোগফল।"
            else
                "Total unpaid balances across credit cards, loans, payables, and debts.",
            formulaSteps = steps,
            relatedBreakdownItems = breakdown,
            relatedTransactions = liabilityTxs,
            customBadgeColor = SolidExpense
        )
    }

    fun openIncomeBreakdown() {
        val cal = Calendar.getInstance()
        val currentMonth = cal.get(Calendar.MONTH)
        val currentYear = cal.get(Calendar.YEAR)

        val monthlyIncomeTxs = recentTransactions.filter {
            if (it.transaction.type != TransactionType.INCOME) return@filter false
            val txCal = Calendar.getInstance().apply { timeInMillis = it.transaction.dateEpochMs }
            txCal.get(Calendar.MONTH) == currentMonth && txCal.get(Calendar.YEAR) == currentYear
        }

        val grouped = monthlyIncomeTxs.groupBy { it.category?.id }
        val breakdown = grouped.map { (catId, txList) ->
            val catName = txList.firstOrNull()?.category?.localizedName(languageMode) ?: if (languageMode == LanguageMode.BANGLA) "অন্যান্য" else "Uncategorized"
            val totalCatAmt = txList.sumOf { it.transaction.amount }
            BreakdownItem(
                name = catName,
                amount = totalCatAmt,
                percentage = if (overview.monthlyIncome > 0) (totalCatAmt / overview.monthlyIncome) * 100.0 else 0.0,
                iconName = txList.firstOrNull()?.category?.iconName,
                color = SolidIncome,
                count = txList.size
            )
        }

        val steps = breakdown.map {
            FormulaStep(
                label = it.name,
                amount = it.amount,
                operator = "+",
                note = "${it.count} tx"
            )
        }

        activeAmountDetail = AmountDetailInfo(
            title = if (languageMode == LanguageMode.BANGLA) "চলতি মাসের মোট আয়" else "Monthly Inflow & Income",
            subtitle = if (languageMode == LanguageMode.BANGLA) "এই মাসের সমস্ত প্রাপ্ত আয়" else "Sum of all income transactions this month",
            totalAmount = overview.monthlyIncome,
            formulaExplanation = if (languageMode == LanguageMode.BANGLA)
                "চলতি ক্যালেন্ডার মাসে প্রাপ্ত বেতন, ব্যবসা, বিনিয়োগ ও অন্যান্য সমস্ত আয়ের যোগফল।"
            else
                "Total income credited from salary, business profits, investments, gifts, and returns this month.",
            formulaSteps = steps,
            relatedBreakdownItems = breakdown,
            relatedTransactions = monthlyIncomeTxs,
            customBadgeColor = SolidIncome
        )
    }

    fun openExpenseBreakdown() {
        val cal = Calendar.getInstance()
        val currentMonth = cal.get(Calendar.MONTH)
        val currentYear = cal.get(Calendar.YEAR)

        val monthlyExpenseTxs = recentTransactions.filter {
            if (it.transaction.type != TransactionType.EXPENSE) return@filter false
            val txCal = Calendar.getInstance().apply { timeInMillis = it.transaction.dateEpochMs }
            txCal.get(Calendar.MONTH) == currentMonth && txCal.get(Calendar.YEAR) == currentYear
        }

        val grouped = monthlyExpenseTxs.groupBy { it.category?.id }
        val breakdown = grouped.map { (catId, txList) ->
            val catName = txList.firstOrNull()?.category?.localizedName(languageMode) ?: if (languageMode == LanguageMode.BANGLA) "সাধারণ খরচ" else "Uncategorized"
            val totalCatAmt = txList.sumOf { it.transaction.amount }
            BreakdownItem(
                name = catName,
                amount = totalCatAmt,
                percentage = if (overview.monthlyExpense > 0) (totalCatAmt / overview.monthlyExpense) * 100.0 else 0.0,
                iconName = txList.firstOrNull()?.category?.iconName,
                color = SolidExpense,
                count = txList.size
            )
        }

        val steps = breakdown.map {
            FormulaStep(
                label = it.name,
                amount = it.amount,
                operator = "+",
                note = "${it.count} tx"
            )
        }

        activeAmountDetail = AmountDetailInfo(
            title = if (languageMode == LanguageMode.BANGLA) "চলতি মাসের মোট খরচ" else "Monthly Expenses & Outflows",
            subtitle = if (languageMode == LanguageMode.BANGLA) "এই মাসের সমস্ত খরচ" else "Sum of all expenses spent this month",
            totalAmount = overview.monthlyExpense,
            formulaExplanation = if (languageMode == LanguageMode.BANGLA)
                "চলতি ক্যালেন্ডার মাসে বিভিন্ন খাতে খরচকৃত সমস্ত টাকার যোগফল।"
            else
                "Total money spent across utility bills, groceries, rent, transit, and daily purchases this month.",
            formulaSteps = steps,
            relatedBreakdownItems = breakdown,
            relatedTransactions = monthlyExpenseTxs,
            customBadgeColor = SolidExpense
        )
    }

    fun openNetSavingsBreakdown() {
        val steps = listOf(
            FormulaStep(
                label = if (languageMode == LanguageMode.BANGLA) "মাসিক আয় (Inflow)" else "Monthly Income",
                amount = overview.monthlyIncome,
                operator = "+"
            ),
            FormulaStep(
                label = if (languageMode == LanguageMode.BANGLA) "মাসিক খরচ (Outflow)" else "Monthly Expenses",
                amount = overview.monthlyExpense,
                operator = "−"
            ),
            FormulaStep(
                label = if (languageMode == LanguageMode.BANGLA) "নিট ক্যাশ ফ্লো / সঞ্চয়" else "Net Cash Flow (Savings)",
                amount = overview.monthlyNetSavings,
                operator = "=",
                isHighlighted = true
            )
        )

        activeAmountDetail = AmountDetailInfo(
            title = if (languageMode == LanguageMode.BANGLA) "মাসিক নিট সঞ্চয় হিসাব" else "Monthly Net Savings Breakdown",
            subtitle = if (languageMode == LanguageMode.BANGLA) "আয় থেকে খরচ বিয়োগ" else "Monthly Income minus Monthly Expenses",
            totalAmount = overview.monthlyNetSavings,
            formulaExplanation = if (languageMode == LanguageMode.BANGLA)
                "মাসিক নিট সঞ্চয় বা ক্যাশ ফ্লো নির্দেশ করে এই মাসে আপনি খরচের পর কত টাকা বৃদ্ধি বা হ্রাস করেছেন।"
            else
                "Net savings represents surplus funds retained from this month's earnings after paying all expenses.",
            formulaSteps = steps,
            relatedTransactions = recentTransactions,
            statusTag = if (overview.monthlyNetSavings >= 0) "Surplus" else "Deficit"
        )
    }

    fun openExpendableBreakdown() {
        val steps = listOf(
            FormulaStep(
                label = if (languageMode == LanguageMode.BANGLA) "উপলব্ধ অর্থ (মোট সম্পদ)" else "Available Money (Total Assets)",
                amount = overview.availableMoney,
                operator = "+"
            ),
            FormulaStep(
                label = if (languageMode == LanguageMode.BANGLA) "বাজেটকৃত অবশিষ্ট খরচ" else "Budgeted Expense Commitment",
                amount = overview.totalExpenseBudget,
                operator = "−"
            ),
            FormulaStep(
                label = if (languageMode == LanguageMode.BANGLA) "বাজেট বহির্ভূত অতিরিক্ত খরচ" else "Additional / Over-Budget Cost",
                amount = overview.additionalCost,
                operator = "−"
            ),
            FormulaStep(
                label = if (languageMode == LanguageMode.BANGLA) "নিরাপদ খরচযোগ্য অর্থ" else "Safe Expendable Balance",
                amount = overview.expendable,
                operator = "=",
                isHighlighted = true
            )
        )

        activeAmountDetail = AmountDetailInfo(
            title = if (languageMode == LanguageMode.BANGLA) "খরচযোগ্য অবশিষ্ট অর্থের হিসাব" else "Expendable Funds Formula",
            subtitle = if (languageMode == LanguageMode.BANGLA) "উপলব্ধ সম্পদ − (বাজেট + অতিরিক্ত খরচ)" else "Available Money − (Total Budget + Over-budget Cost)",
            totalAmount = overview.expendable,
            formulaExplanation = if (languageMode == LanguageMode.BANGLA)
                "খরচযোগ্য অর্থ হলো আপনার মোট উপলব্ধ তহবিল থেকে এই মাসের বাকি থাকা বাজেট ও অতিরিক্ত খরচ বাদ দেয়ার পর যে টাকা নিশ্চিন্তে খরচ করা যাবে।"
            else
                "Expendable represents the safe disposable amount available right now without jeopardizing monthly budgeted commitments or entering a deficit.",
            formulaSteps = steps,
            relatedTransactions = recentTransactions,
            statusTag = if (overview.expendable >= 0) "Safe" else "Deficit"
        )
    }

    fun openExpectedExpendableBreakdown() {
        val steps = listOf(
            FormulaStep(
                label = if (languageMode == LanguageMode.BANGLA) "বর্তমান খরচযোগ্য অর্থ" else "Current Expendable",
                amount = overview.expendable,
                operator = "+"
            ),
            FormulaStep(
                label = if (languageMode == LanguageMode.BANGLA) "প্রত্যাশিত বাকি আয়" else "Projected Remaining Income",
                amount = overview.potentialIncome,
                operator = "+"
            ),
            FormulaStep(
                label = if (languageMode == LanguageMode.BANGLA) "সম্ভাব্য মোট খরচযোগ্য অর্থ" else "Expected Expendable",
                amount = overview.expectedExpendable,
                operator = "=",
                isHighlighted = true
            )
        )

        activeAmountDetail = AmountDetailInfo(
            title = if (languageMode == LanguageMode.BANGLA) "সম্ভাব্য খরচযোগ্য অর্থ" else "Expected Expendable Calculation",
            subtitle = if (languageMode == LanguageMode.BANGLA) "খরচযোগ্য অর্থ + সম্ভাব্য আয়" else "Current Expendable + Potential Income",
            totalAmount = overview.expectedExpendable,
            formulaExplanation = if (languageMode == LanguageMode.BANGLA)
                "এই মাসের প্রত্যাশিত বাকি আয় পাওয়া গেলে আপনার মোট কত টাকা খরচ করার সামর্থ্য থাকবে।"
            else
                "Estimated safe disposable funds once all projected monthly income is collected.",
            formulaSteps = steps,
            relatedTransactions = recentTransactions
        )
    }

    fun openRemainingExpensesBreakdown() {
        val steps = listOf(
            FormulaStep(
                label = if (languageMode == LanguageMode.BANGLA) "মোট মাসিক বাজেট" else "Total Monthly Budget",
                amount = overview.totalExpenseBudget,
                operator = "+"
            ),
            FormulaStep(
                label = if (languageMode == LanguageMode.BANGLA) "বাজেটের অন্তর্ভুক্ত খরচ" else "Budgeted Spending Used",
                amount = (overview.totalExpenseBudget - overview.remainingExpenses).coerceAtLeast(0.0),
                operator = "−"
            ),
            FormulaStep(
                label = if (languageMode == LanguageMode.BANGLA) "বাকি থাকা বাজেট" else "Remaining Expenses",
                amount = overview.remainingExpenses,
                operator = "=",
                isHighlighted = true
            )
        )

        activeAmountDetail = AmountDetailInfo(
            title = if (languageMode == LanguageMode.BANGLA) "বাজেটের অবশিষ্ট খরচ" else "Remaining Budget Expenses",
            subtitle = if (languageMode == LanguageMode.BANGLA) "চলতি মাসে বাজেটের বাকি টাকা" else "Unspent budgeted allocations for this month",
            totalAmount = overview.remainingExpenses,
            formulaExplanation = if (languageMode == LanguageMode.BANGLA)
                "চলতি মাসে বিভিন্ন ক্যাটাগরিতে বরাদ্দকৃত বাজেটের মধ্যে যে টাকা এখনও খরচ করা বাকি আছে।"
            else
                "Sum of remaining unspent balances across all active category budgets for this month.",
            formulaSteps = steps,
            relatedTransactions = recentTransactions
        )
    }

    fun openAdditionalCostBreakdown() {
        activeAmountDetail = AmountDetailInfo(
            title = if (languageMode == LanguageMode.BANGLA) "অতিরিক্ত / ওভার-বাজেট খরচ" else "Additional / Over-Budget Cost",
            subtitle = if (languageMode == LanguageMode.BANGLA) "বাজেট সীমার অতিরিক্ত খরচ" else "Expenses exceeding category budget limits",
            totalAmount = overview.additionalCost,
            formulaExplanation = if (languageMode == LanguageMode.BANGLA)
                "নির্ধারিত বাজেটের চেয়ে যেসব ক্যাটাগরিতে বেশি খরচ করা হয়েছে তার যোগফল।"
            else
                "Total excess spending that crossed the monthly budget limits in over-budget categories.",
            formulaSteps = listOf(
                FormulaStep(
                    label = if (languageMode == LanguageMode.BANGLA) "অতিরিক্ত খরচ" else "Over-Budget Spending",
                    amount = overview.additionalCost,
                    operator = "="
                )
            ),
            relatedTransactions = recentTransactions,
            customBadgeColor = if (overview.additionalCost > 0) SolidExpense else SolidIncome
        )
    }

    fun openAccountBreakdown(accWithBal: AccountWithBalance) {
        val acc = accWithBal.account
        val accTxs = recentTransactions.filter {
            it.transaction.debitAccountId == acc.id || it.transaction.creditAccountId == acc.id
        }

        val totalDebits = accTxs.filter { it.transaction.debitAccountId == acc.id }.sumOf { it.transaction.amount }
        val totalCredits = accTxs.filter { it.transaction.creditAccountId == acc.id }.sumOf { it.transaction.amount }

        val steps = listOf(
            FormulaStep(
                label = if (languageMode == LanguageMode.BANGLA) "প্রারম্ভিক ব্যালেন্স" else "Opening Balance",
                amount = acc.initialBalance,
                operator = "+"
            ),
            FormulaStep(
                label = if (languageMode == LanguageMode.BANGLA) "মোট জমা / ইনফ্লো (+)" else "Total Inflow / Deposits (+)",
                amount = totalDebits,
                operator = "+"
            ),
            FormulaStep(
                label = if (languageMode == LanguageMode.BANGLA) "মোট খরচ / আউটফ্লো (–)" else "Total Outflow / Payments (–)",
                amount = totalCredits,
                operator = "−"
            ),
            FormulaStep(
                label = if (languageMode == LanguageMode.BANGLA) "বর্তমান ব্যালেন্স" else "Current Balance",
                amount = accWithBal.currentBalance,
                operator = "=",
                isHighlighted = true
            )
        )

        activeAmountDetail = AmountDetailInfo(
            title = acc.localizedName(languageMode),
            subtitle = if (acc.type == AccountType.ASSET) "Asset Account" else "Liability Account",
            totalAmount = accWithBal.currentBalance,
            formulaExplanation = if (languageMode == LanguageMode.BANGLA)
                "বর্তমান ব্যালেন্স = প্রারম্ভিক ব্যালেন্স + মোট জমা (ডেবিট) − মোট খরচ/উত্তোলন (ক্রেডিট)"
            else
                "Current Balance = Opening Balance + All Inflows (Debits) − All Outflows (Credits)",
            formulaSteps = steps,
            relatedTransactions = accTxs
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_screen")
    ) {
        // Dashboard Top Header (Drawer Menu, App Branding, Demo Indicator)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onOpenDrawer,
                    modifier = Modifier.testTag("dashboard_menu_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Open Navigation Menu",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = Color(0xFFFFD700),
                    shadowElevation = 1.dp,
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "৳",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF4A3800)
                        )
                    }
                }

                Text(
                    text = LanguageHelper.getString("app_name", languageMode),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (isDemoMode) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    modifier = Modifier
                        .clickable { onExitDemoMode() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(MaterialTheme.colorScheme.tertiary, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "DEMO",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Render Cards dynamically based on user configuration order and visibility
            dashboardConfig.cardOrder.forEach { cardType ->
                if (cardType in dashboardConfig.visibleCards) {
                    when (cardType) {
                        DashboardCardType.NET_WORTH -> {
                            item(key = "card_net_worth") {
                                RedesignedNetWorthCard(
                                    overview = overview,
                                    languageMode = languageMode,
                                    onNetWorthClick = { openNetWorthBreakdown() },
                                    onAssetsClick = { openAssetsBreakdown() },
                                    onLiabilitiesClick = { openLiabilitiesBreakdown() }
                                )
                            }
                        }

                        DashboardCardType.DAILY_SUMMARY -> {
                            item(key = "card_daily_summary") {
                                DailySummaryCard(
                                    transactions = recentTransactions,
                                    mode = dashboardConfig.dailySummaryMode,
                                    period = dashboardConfig.dailySummaryPeriod,
                                    showValues = dashboardConfig.dailyShowValues,
                                    showAverages = dashboardConfig.dailyShowAverages,
                                    languageMode = languageMode,
                                    onModeChange = { newMode ->
                                        onUpdateDailySummarySettings(
                                            newMode,
                                            dashboardConfig.dailySummaryPeriod,
                                            dashboardConfig.dailyShowValues,
                                            dashboardConfig.dailyShowAverages
                                        )
                                    },
                                    onPeriodChange = { newPeriod ->
                                        onUpdateDailySummarySettings(
                                            dashboardConfig.dailySummaryMode,
                                            newPeriod,
                                            dashboardConfig.dailyShowValues,
                                            dashboardConfig.dailyShowAverages
                                        )
                                    },
                                    onOpenSettings = { showDailySettingsDialog = true },
                                    onCardClick = { showDailySummaryDetail = true },
                                    onDayClick = { daySummary ->
                                        val calTarget = Calendar.getInstance().apply { timeInMillis = daySummary.dateEpochMs }
                                        val dayTxs = recentTransactions.filter {
                                            val calTx = Calendar.getInstance().apply { timeInMillis = it.transaction.dateEpochMs }
                                            calTx.get(Calendar.YEAR) == calTarget.get(Calendar.YEAR) &&
                                                    calTx.get(Calendar.DAY_OF_YEAR) == calTarget.get(Calendar.DAY_OF_YEAR)
                                        }
                                        activeAmountDetail = AmountDetailInfo(
                                            title = daySummary.fullDateLabel,
                                            subtitle = if (languageMode == LanguageMode.BANGLA) "দৈনিক লেনদেনের সারাংশ" else "Daily summary breakdown",
                                            totalAmount = daySummary.expense,
                                            formulaExplanation = "Expense: ${LanguageHelper.formatCurrency(daySummary.expense, languageMode)} | Income: ${LanguageHelper.formatCurrency(daySummary.income, languageMode)}",
                                            formulaSteps = listOf(
                                                FormulaStep("Income", daySummary.income, "+"),
                                                FormulaStep("Expense", daySummary.expense, "−"),
                                                FormulaStep("Net Day Flow", daySummary.income - daySummary.expense, "=")
                                            ),
                                            relatedTransactions = dayTxs
                                        )
                                    }
                                )
                            }
                        }

                        DashboardCardType.BUDGET_SUMMARY -> {
                            item(key = "card_budget_summary") {
                                BudgetSummaryCard(
                                    transactions = recentTransactions,
                                    allCategories = allCategories,
                                    monthlyBudgets = monthlyBudgets,
                                    chartShape = dashboardConfig.budgetChartShape,
                                    categoryType = dashboardConfig.budgetCategoryType,
                                    maxCategories = dashboardConfig.budgetMaxCategories,
                                    showPercentages = dashboardConfig.budgetShowPercentages,
                                    showTodayPace = dashboardConfig.budgetShowTodayPace,
                                    languageMode = languageMode,
                                    onCategoryTypeChange = { newType ->
                                        onUpdateBudgetSummarySettings(
                                            dashboardConfig.budgetChartShape,
                                            newType,
                                            dashboardConfig.budgetMaxCategories,
                                            dashboardConfig.budgetShowPercentages,
                                            dashboardConfig.budgetShowTodayPace
                                        )
                                    },
                                    onOpenSettings = { showBudgetSettingsDialog = true },
                                    onCardClick = { showBudgetSummaryPreview = true }
                                )
                            }
                        }

                        DashboardCardType.FAVORITE_ACCOUNTS -> {
                            item(key = "card_favorite_accounts") {
                                FavoriteAccountsCard(
                                    accountsWithBalances = accountsWithBalances,
                                    favoriteAccountIds = dashboardConfig.favoriteAccountIds,
                                    languageMode = languageMode,
                                    onOpenAccountPicker = { showFavoriteAccountsPicker = true },
                                    onAccountClick = { acc ->
                                        val item = accountsWithBalances.firstOrNull { it.account.id == acc.id }
                                        if (item != null) {
                                            openAccountBreakdown(item)
                                        } else {
                                            onAccountClick(acc)
                                        }
                                    }
                                )
                            }
                        }

                        DashboardCardType.CALENDAR_VIEW -> {
                            item(key = "card_calendar_view") {
                                CalendarSummaryCard(
                                    transactions = recentTransactions,
                                    displayMode = dashboardConfig.calendarDisplayMode,
                                    showIncome = dashboardConfig.calendarShowIncome,
                                    showExpense = dashboardConfig.calendarShowExpense,
                                    languageMode = languageMode,
                                    onOpenSettings = { showCalendarSettingsDialog = true },
                                    onTransactionClick = onTransactionClick
                                )
                            }
                        }

                        DashboardCardType.CASH_FLOW -> {
                            item(key = "card_cash_flow") {
                                RedesignedCashFlowRow(
                                    overview = overview,
                                    languageMode = languageMode,
                                    onIncomeClick = { openIncomeBreakdown() },
                                    onExpenseClick = { openExpenseBreakdown() }
                                )
                            }
                        }

                        DashboardCardType.FINANCIAL_OVERVIEW -> {
                            item(key = "card_financial_overview") {
                                RedesignedFinancialOverviewCard(
                                    overview = overview,
                                    languageMode = languageMode,
                                    onExpendableClick = { openExpendableBreakdown() },
                                    onExpectedExpendableClick = { openExpectedExpendableBreakdown() },
                                    onAssetsClick = { openAssetsBreakdown() },
                                    onLiabilitiesClick = { openLiabilitiesBreakdown() },
                                    onRemainingExpensesClick = { openRemainingExpensesBreakdown() },
                                    onAdditionalCostClick = { openAdditionalCostBreakdown() },
                                    onNetWorthClick = { openNetWorthBreakdown() },
                                    onNetEarningsClick = { openNetSavingsBreakdown() }
                                )
                            }
                        }

                        DashboardCardType.QUICK_ACTIONS -> {
                            item(key = "card_quick_actions") {
                                QuickActionsRow(
                                    languageMode = languageMode,
                                    onAddTransactionClick = onAddTransactionClick,
                                    onOpenCalculator = { showStandAloneCalculator = true }
                                )
                            }
                        }

                        DashboardCardType.RECENT_TRANSACTIONS -> {
                            item(key = "card_recent_transactions") {
                                RecentTransactionsCard(
                                    recentTransactions = recentTransactions,
                                    languageMode = languageMode,
                                    onTransactionClick = onTransactionClick,
                                    onViewAllClick = onViewAllTransactionsClick,
                                    onAmountClick = { txItem ->
                                        activeAmountDetail = AmountDetailInfo(
                                            title = txItem.category?.localizedName(languageMode) ?: "Transaction",
                                            subtitle = DateUtils.formatDate(txItem.transaction.dateEpochMs, languageMode),
                                            totalAmount = txItem.transaction.amount,
                                            formulaExplanation = "Note: ${txItem.transaction.note.ifBlank { "N/A" }}\nPayee/Payer: ${txItem.transaction.payeeOrPayer.ifBlank { "N/A" }}",
                                            relatedTransactions = listOf(txItem)
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Customize Cards Button
            item(key = "bottom_customize_cards_button") {
                OutlinedButton(
                    onClick = { showCustomizeCardsDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                ) {
                    Icon(
                        imageVector = Icons.Default.DashboardCustomize,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = LanguageHelper.getString("customize_cards", languageMode),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }

    // Modal Dialogs
    if (activeAmountDetail != null) {
        AmountBreakdownDialog(
            info = activeAmountDetail!!,
            languageMode = languageMode,
            onDismiss = { activeAmountDetail = null },
            onTransactionClick = { tx ->
                activeAmountDetail = null
                onTransactionClick(tx)
            }
        )
    }

    if (showCustomizeCardsDialog) {
        CustomizeDashboardCardsDialog(
            config = dashboardConfig,
            languageMode = languageMode,
            onDismiss = { showCustomizeCardsDialog = false },
            onToggleCard = onToggleCardVisibility,
            onMoveCard = onReorderCards,
            onResetDefaults = onResetDashboardDefaults
        )
    }

    if (showDailySettingsDialog) {
        DailySummarySettingsDialog(
            currentMode = dashboardConfig.dailySummaryMode,
            currentPeriod = dashboardConfig.dailySummaryPeriod,
            currentShowValues = dashboardConfig.dailyShowValues,
            currentShowAverages = dashboardConfig.dailyShowAverages,
            languageMode = languageMode,
            onDismiss = { showDailySettingsDialog = false },
            onSave = { m, p, sv, sa ->
                onUpdateDailySummarySettings(m, p, sv, sa)
                showDailySettingsDialog = false
            }
        )
    }

    if (showBudgetSettingsDialog) {
        BudgetSummarySettingsDialog(
            currentShape = dashboardConfig.budgetChartShape,
            currentCategoryType = dashboardConfig.budgetCategoryType,
            currentMaxCategories = dashboardConfig.budgetMaxCategories,
            currentShowPercentages = dashboardConfig.budgetShowPercentages,
            currentShowTodayPace = dashboardConfig.budgetShowTodayPace,
            languageMode = languageMode,
            onDismiss = { showBudgetSettingsDialog = false },
            onSave = { s, t, mc, sp, tp ->
                onUpdateBudgetSummarySettings(s, t, mc, sp, tp)
                showBudgetSettingsDialog = false
            }
        )
    }

    if (showCalendarSettingsDialog) {
        CalendarSettingsDialog(
            currentDisplayMode = dashboardConfig.calendarDisplayMode,
            currentShowIncome = dashboardConfig.calendarShowIncome,
            currentShowExpense = dashboardConfig.calendarShowExpense,
            languageMode = languageMode,
            onDismiss = { showCalendarSettingsDialog = false },
            onSave = { dm, si, se ->
                onUpdateCalendarSettings(dm, si, se)
                showCalendarSettingsDialog = false
            }
        )
    }

    if (showFavoriteAccountsPicker) {
        FavoriteAccountsSelectionDialog(
            allAccounts = accountsWithBalances,
            initialSelectedIds = dashboardConfig.favoriteAccountIds,
            languageMode = languageMode,
            onDismiss = { showFavoriteAccountsPicker = false },
            onSave = { selectedIds ->
                onUpdateFavoriteAccounts(selectedIds)
                showFavoriteAccountsPicker = false
            }
        )
    }

    if (showStandAloneCalculator) {
        PopupCalculatorDialog(
            languageMode = languageMode,
            onDismiss = { showStandAloneCalculator = false },
            onValueConfirmed = { /* Standalone calculator */ }
        )
    }

    if (showBudgetSummaryPreview) {
        BudgetSummaryPreviewDialog(
            transactions = recentTransactions,
            allCategories = allCategories,
            monthlyBudgets = monthlyBudgets,
            accounts = accountsWithBalances.map { it.account },
            languageMode = languageMode,
            onDismiss = { showBudgetSummaryPreview = false },
            onTransactionClick = { txItem ->
                showBudgetSummaryPreview = false
                onTransactionClick(txItem.transaction)
            }
        )
    }

    if (showDailySummaryDetail) {
        DailySummaryDetailDialog(
            transactions = recentTransactions,
            languageMode = languageMode,
            onDismiss = { showDailySummaryDetail = false },
            onTransactionClick = { txItem ->
                showDailySummaryDetail = false
                onTransactionClick(txItem.transaction)
            }
        )
    }
}

/**
 * Redesigned Net Worth Hero Card with dynamic gradients, status chips, and clickable amount breakdowns.
 */
@Composable
private fun RedesignedNetWorthCard(
    overview: FinancialOverview,
    languageMode: LanguageMode,
    onNetWorthClick: () -> Unit,
    onAssetsClick: () -> Unit,
    onLiabilitiesClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("net_worth_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            SolidPrimary.copy(alpha = 0.95f),
                            SolidPrimary
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Savings,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = LanguageHelper.getString("net_worth", languageMode),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (overview.isLedgerBalanced) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (overview.isLedgerBalanced) Color(0xFF6EE7B7) else Color(0xFFFCA5A5),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (overview.isLedgerBalanced) "Dr = Cr Balanced" else LanguageHelper.getString("unbalanced", languageMode),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Hero Clickable Net Worth Amount
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onNetWorthClick),
                    color = Color.White.copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = LanguageHelper.formatCurrency(overview.netWorth, languageMode),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = "View Calculation",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(12.dp))

                // Sub Assets & Liabilities clickable tags
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Assets Sub-pill
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(onClick = onAssetsClick),
                        color = Color.White.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF6EE7B7))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = LanguageHelper.getString("assets", languageMode),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                            Text(
                                text = LanguageHelper.formatCurrency(overview.totalAssets, languageMode),
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Liabilities Sub-pill
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(onClick = onLiabilitiesClick),
                        color = Color.White.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFCA5A5))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = LanguageHelper.getString("liabilities", languageMode),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                            Text(
                                text = LanguageHelper.formatCurrency(overview.totalLiabilities, languageMode),
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Redesigned Cash Flow Row with clickable Income and Expense cards.
 */
@Composable
private fun RedesignedCashFlowRow(
    overview: FinancialOverview,
    languageMode: LanguageMode,
    onIncomeClick: () -> Unit,
    onExpenseClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Monthly Income Card
        Surface(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .clickable(onClick = onIncomeClick),
            color = SolidIncomeContainer.copy(alpha = 0.85f),
            border = BorderStroke(1.dp, SolidIncome.copy(alpha = 0.35f))
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(SolidIncome.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                        contentDescription = null,
                        tint = SolidIncome,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = LanguageHelper.getString("income", languageMode),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SolidOnIncomeContainer.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = LanguageHelper.formatCurrency(overview.monthlyIncome, languageMode),
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SolidOnIncomeContainer
                    )
                }
            }
        }

        // Monthly Expense Card
        Surface(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .clickable(onClick = onExpenseClick),
            color = SolidExpenseContainer.copy(alpha = 0.85f),
            border = BorderStroke(1.dp, SolidExpense.copy(alpha = 0.35f))
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(SolidExpense.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                        contentDescription = null,
                        tint = SolidExpense,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = LanguageHelper.getString("expense", languageMode),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SolidOnExpenseContainer.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = LanguageHelper.formatCurrency(overview.monthlyExpense, languageMode),
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SolidOnExpenseContainer
                    )
                }
            }
        }
    }
}

/**
 * Redesigned Financial Overview Card and sub-cards with clickable amounts,
 * clear visual hierarchy, and instant formula access.
 */
@Composable
private fun RedesignedFinancialOverviewCard(
    overview: FinancialOverview,
    languageMode: LanguageMode,
    onExpendableClick: () -> Unit,
    onExpectedExpendableClick: () -> Unit,
    onAssetsClick: () -> Unit,
    onLiabilitiesClick: () -> Unit,
    onRemainingExpensesClick: () -> Unit,
    onAdditionalCostClick: () -> Unit,
    onNetWorthClick: () -> Unit,
    onNetEarningsClick: () -> Unit
) {
    var showFormulaBreakdown by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("expendable_overview_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Card Title Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SolidPrimary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = null,
                            tint = SolidPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = LanguageHelper.getString("financial_overview", languageMode),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "ট্যাপ করে বিস্তারিত হিসাব দেখুন" else "Tap any amount to view calculation breakdown",
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                IconButton(
                    onClick = { showFormulaBreakdown = !showFormulaBreakdown },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (showFormulaBreakdown) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Toggle Formula",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Primary Expendable & Expected Expendable Sub-Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Expendable Hero Sub-card
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (overview.expendable >= 0) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f) else Color(0xFFFFEBEE),
                    border = BorderStroke(1.dp, if (overview.expendable >= 0) SolidPrimary.copy(alpha = 0.3f) else Color(0xFFEF9A9A)),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(onClick = onExpendableClick)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = LanguageHelper.getString("expendable", languageMode),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (overview.expendable >= 0) MaterialTheme.colorScheme.onPrimaryContainer else Color(0xFFC62828),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (overview.expendable >= 0) Color(0xFF10B981) else Color(0xFFEF4444)
                            ) {
                                Text(
                                    text = if (overview.expendable >= 0) "Safe" else "Deficit",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = LanguageHelper.formatCurrency(overview.expendable, languageMode),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (overview.expendable >= 0) MaterialTheme.colorScheme.onPrimaryContainer else Color(0xFFC62828)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = if (overview.expendable >= 0) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f) else Color(0xFFC62828).copy(alpha = 0.6f),
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }

                // Expected Expendable Sub-card
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f)),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(onClick = onExpectedExpendableClick)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = LanguageHelper.getString("expected_expendable", languageMode),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "+Income",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = LanguageHelper.formatCurrency(overview.expectedExpendable, languageMode),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f),
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
            Spacer(modifier = Modifier.height(12.dp))

            // 6 Financial Indicators Sub-Cards in 2x3 Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Sub-Card 1: Current Assets
                FinancialIndicatorItem(
                    label = LanguageHelper.getString("current_assets", languageMode),
                    amount = overview.totalAssets,
                    icon = Icons.Default.AccountBalanceWallet,
                    iconTint = SolidIncome,
                    languageMode = languageMode,
                    onClick = onAssetsClick,
                    modifier = Modifier.weight(1f)
                )

                // Sub-Card 2: Liabilities
                FinancialIndicatorItem(
                    label = LanguageHelper.getString("liabilities", languageMode),
                    amount = overview.totalLiabilities,
                    icon = Icons.Default.CreditCard,
                    iconTint = SolidExpense,
                    languageMode = languageMode,
                    trendIndicator = if (overview.liabilitiesChange != 0.0) if (overview.liabilitiesChange > 0) "▲" else "▼" else null,
                    trendColor = if (overview.liabilitiesChange > 0) SolidExpense else SolidIncome,
                    onClick = onLiabilitiesClick,
                    modifier = Modifier.weight(1f)
                )

                // Sub-Card 3: Remaining Expenses
                FinancialIndicatorItem(
                    label = LanguageHelper.getString("remaining_expenses", languageMode),
                    amount = overview.remainingExpenses,
                    icon = Icons.Default.PieChart,
                    iconTint = Color(0xFF0284C7),
                    amountColor = Color(0xFF0284C7),
                    languageMode = languageMode,
                    onClick = onRemainingExpensesClick,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Sub-Card 4: Additional Cost
                FinancialIndicatorItem(
                    label = LanguageHelper.getString("additional_cost", languageMode),
                    amount = overview.additionalCost,
                    icon = Icons.Default.Warning,
                    iconTint = if (overview.additionalCost > 0) SolidExpense else MaterialTheme.colorScheme.outline,
                    amountColor = if (overview.additionalCost > 0) SolidExpense else MaterialTheme.colorScheme.onSurface,
                    languageMode = languageMode,
                    onClick = onAdditionalCostClick,
                    modifier = Modifier.weight(1f)
                )

                // Sub-Card 5: Net Worth
                FinancialIndicatorItem(
                    label = LanguageHelper.getString("net_worth", languageMode),
                    amount = overview.netWorth,
                    icon = Icons.Default.Savings,
                    iconTint = SolidPrimary,
                    amountColor = SolidPrimary,
                    languageMode = languageMode,
                    onClick = onNetWorthClick,
                    modifier = Modifier.weight(1f)
                )

                // Sub-Card 6: Net Earnings (Savings)
                FinancialIndicatorItem(
                    label = LanguageHelper.getString("net_earnings", languageMode),
                    amount = overview.monthlyNetSavings,
                    icon = Icons.Default.AccountBalance,
                    iconTint = if (overview.monthlyNetSavings >= 0) SolidIncome else SolidExpense,
                    amountColor = if (overview.monthlyNetSavings >= 0) SolidIncome else SolidExpense,
                    languageMode = languageMode,
                    onClick = onNetEarningsClick,
                    modifier = Modifier.weight(1f)
                )
            }

            // Expandable Calculation Walkthrough
            AnimatedVisibility(visible = showFormulaBreakdown) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                        .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = LanguageHelper.getString("expendable_breakdown", languageMode),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Available Money = Total Assets (${LanguageHelper.formatCurrency(overview.availableMoney, languageMode)})",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "• Total Expense Budget = ${LanguageHelper.formatCurrency(overview.totalExpenseBudget, languageMode)}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "• Additional / Over-Budget Cost = ${LanguageHelper.formatCurrency(overview.additionalCost, languageMode)}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "• Expendable = ${LanguageHelper.formatCurrency(overview.availableMoney, languageMode)} − (${LanguageHelper.formatCurrency(overview.totalExpenseBudget, languageMode)} + ${LanguageHelper.formatCurrency(overview.additionalCost, languageMode)}) = ${LanguageHelper.formatCurrency(overview.expendable, languageMode)}",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• Potential Income = ${LanguageHelper.formatCurrency(overview.potentialIncome, languageMode)}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "• Expected Expendable = Expendable + Potential Income = ${LanguageHelper.formatCurrency(overview.expectedExpendable, languageMode)}",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

/**
 * Reusable clickable financial indicator tile for sub-cards
 */
@Composable
private fun FinancialIndicatorItem(
    label: String,
    amount: Double,
    icon: ImageVector,
    iconTint: Color,
    languageMode: LanguageMode,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    amountColor: Color = MaterialTheme.colorScheme.onSurface,
    trendIndicator: String? = null,
    trendColor: Color = MaterialTheme.colorScheme.outline
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 9.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(13.dp)
                )
                if (trendIndicator != null) {
                    Text(
                        text = trendIndicator,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = trendColor
                    )
                }
            }
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                lineHeight = 12.sp,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = LanguageHelper.formatCurrency(amount, languageMode),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = amountColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun QuickActionsRow(
    languageMode: LanguageMode,
    onAddTransactionClick: (TransactionType) -> Unit,
    onOpenCalculator: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = { onAddTransactionClick(TransactionType.EXPENSE) },
            modifier = Modifier.weight(1f).height(44.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SolidExpense),
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(2.dp))
                Text(LanguageHelper.getString("expense", languageMode), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Button(
            onClick = { onAddTransactionClick(TransactionType.INCOME) },
            modifier = Modifier.weight(1f).height(44.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SolidIncome),
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(2.dp))
                Text(LanguageHelper.getString("income", languageMode), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Button(
            onClick = { onAddTransactionClick(TransactionType.TRANSFER) },
            modifier = Modifier.weight(1f).height(44.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SolidTransfer),
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(2.dp))
                Text(LanguageHelper.getString("transfer", languageMode), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Button(
            onClick = onOpenCalculator,
            modifier = Modifier.weight(1f).height(44.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SolidPrimary),
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(2.dp))
                Text(LanguageHelper.getString("calculator", languageMode), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun RecentTransactionsCard(
    recentTransactions: List<TransactionWithDetails>,
    languageMode: LanguageMode,
    onTransactionClick: (Transaction) -> Unit,
    onViewAllClick: () -> Unit,
    onAmountClick: ((TransactionWithDetails) -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = LanguageHelper.getString("recent_transactions", languageMode),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (recentTransactions.isNotEmpty()) {
                    Text(
                        text = LanguageHelper.getString("all_transactions", languageMode),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SolidPrimary,
                        modifier = Modifier.clickable { onViewAllClick() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (recentTransactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = LanguageHelper.getString("no_transactions", languageMode),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    recentTransactions.take(5).forEach { item ->
                        val tx = item.transaction
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onTransactionClick(tx) },
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    val iconColor = when (tx.type) {
                                        TransactionType.EXPENSE -> SolidExpense
                                        TransactionType.INCOME -> SolidIncome
                                        TransactionType.TRANSFER -> SolidTransfer
                                    }
                                    val iconBg = when (tx.type) {
                                        TransactionType.EXPENSE -> SolidExpenseContainer
                                        TransactionType.INCOME -> SolidIncomeContainer
                                        TransactionType.TRANSFER -> SolidPrimaryContainer
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(iconBg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        val icon = when (tx.type) {
                                            TransactionType.EXPENSE -> IconHelper.getIconByName(item.category?.iconName ?: "Category")
                                            TransactionType.INCOME -> IconHelper.getIconByName(item.category?.iconName ?: "Payments")
                                            TransactionType.TRANSFER -> Icons.Default.SwapHoriz
                                        }
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = iconColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Column {
                                        val title = when (tx.type) {
                                            TransactionType.EXPENSE -> item.subCategory?.localizedName(languageMode)
                                                ?: item.category?.localizedName(languageMode)
                                                ?: "Expense"
                                            TransactionType.INCOME -> item.subCategory?.localizedName(languageMode)
                                                ?: item.category?.localizedName(languageMode)
                                                ?: "Income"
                                            TransactionType.TRANSFER -> LanguageHelper.getString("transfer", languageMode)
                                        }
                                        Text(
                                            text = title,
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        Spacer(modifier = Modifier.height(2.dp))

                                        DoubleEntryFlowBadge(
                                            item = item,
                                            languageMode = languageMode
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    val sign = when (tx.type) {
                                        TransactionType.EXPENSE -> "−"
                                        TransactionType.INCOME -> "+"
                                        TransactionType.TRANSFER -> ""
                                    }
                                    val amtColor = when (tx.type) {
                                        TransactionType.EXPENSE -> SolidExpense
                                        TransactionType.INCOME -> SolidIncome
                                        TransactionType.TRANSFER -> SolidTransfer
                                    }
                                    Text(
                                        text = "$sign${LanguageHelper.formatCurrency(tx.amount, languageMode)}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = amtColor,
                                        modifier = if (onAmountClick != null) Modifier.clickable { onAmountClick(item) } else Modifier
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = DateUtils.formatShortDate(tx.dateEpochMs, languageMode),
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
