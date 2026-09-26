package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.model.AccountType
import com.example.data.model.TransactionType
import com.example.util.CurrencyPreferences
import com.example.util.WidgetPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object WidgetUpdateHelper {

    const val ACTION_ADD_EXPENSE = "com.example.budgeter.ACTION_ADD_EXPENSE"
    const val ACTION_ADD_INCOME = "com.example.budgeter.ACTION_ADD_INCOME"
    const val ACTION_ADD_TRANSFER = "com.example.budgeter.ACTION_ADD_TRANSFER"
    const val ACTION_ADD_TRANSACTION = "com.example.budgeter.ACTION_ADD_TRANSACTION"
    const val ACTION_ADD_WISHLIST = "com.example.budgeter.ACTION_ADD_WISHLIST"
    const val ACTION_VIEW_GOALS = "com.example.budgeter.ACTION_VIEW_GOALS"
    const val ACTION_VIEW_TRANSACTIONS = "com.example.budgeter.ACTION_VIEW_TRANSACTIONS"
    const val ACTION_REFRESH_WIDGETS = "com.example.budgeter.ACTION_REFRESH_WIDGETS"
    const val ACTION_TOGGLE_PRIVACY = "com.example.budgeter.ACTION_TOGGLE_PRIVACY"

    private val amountFormat = DecimalFormat("#,##0.00")
    private val compactFormat = DecimalFormat("#,##0")

    fun updateAllWidgets(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val appWidgetManager = AppWidgetManager.getInstance(context) ?: return@launch
                val db = AppDatabase.getDatabase(context)

                val widgetPrefs = WidgetPreferences.getInstance(context)
                val isPrivacy = widgetPrefs.isPrivacyEnabled()
                val currencySymbol = CurrencyPreferences.getInstance(context).config.value.activeSymbol

                // 1. Calculate Balances & Monthly Stats
                val allAccounts = db.accountDao().getAllAccountsSnapshot()
                val allTransactions = db.transactionDao().getAllTransactionsSnapshot()

                var netBalance = 0.0
                allAccounts.filter { it.isActive && it.parentId == null }.forEach { account ->
                    val accInitial = account.initialBalance
                    val credits = allTransactions.filter { it.creditAccountId == account.id }.sumOf { it.amount }
                    val debits = allTransactions.filter { it.debitAccountId == account.id }.sumOf { it.amount }
                    val accBal = when (account.type) {
                        AccountType.LIABILITY -> accInitial + debits - credits
                        else -> accInitial + credits - debits
                    }
                    netBalance += accBal
                }

                // Current month calculation
                val cal = Calendar.getInstance()
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val monthStart = cal.timeInMillis

                val currentYear = cal.get(Calendar.YEAR)
                val currentMonth = cal.get(Calendar.MONTH) + 1 // 1-indexed

                val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                val todayDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
                val daysRemaining = (daysInMonth - todayDay + 1).coerceAtLeast(1)

                val monthTxs = allTransactions.filter { it.dateEpochMs >= monthStart }
                val monthIncome = monthTxs.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
                val monthExpense = monthTxs.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

                // 2. Monthly Budget calculations
                val monthlyBudgets = db.monthlyBudgetDao().getBudgetsForMonthSnapshot(currentYear, currentMonth)
                val totalBudget = monthlyBudgets.sumOf { it.budgetedAmount }
                val budgetRemaining = (totalBudget - monthExpense).coerceAtLeast(0.0)
                val safeToSpendPerDay = budgetRemaining / daysRemaining

                // 3. Top Active Savings Goal
                val activeGoals = db.savingsGoalDao().getAllGoalsSnapshot().filter { !it.isCompleted }
                val topGoal = activeGoals.firstOrNull()
                val goalSavedAmount = if (topGoal != null) {
                    db.savingsGoalDao().getAllocationsForGoalSync(topGoal.id).sumOf { it.allocatedAmount }
                } else 0.0

                // 4. Push updates to widgets
                updateQuickActionWidgets(context, appWidgetManager, isPrivacy, currencySymbol, netBalance, monthIncome, monthExpense)
                updateQuickAddWidgets(context, appWidgetManager)
                updateQuickAddTransactionWidgets(context, appWidgetManager)
                updateQuickAddWishlistWidgets(context, appWidgetManager)
                updateBudgetMeterWidgets(context, appWidgetManager, currencySymbol, currentYear, currentMonth, totalBudget, monthExpense, safeToSpendPerDay)
                updateSavingsGoalWidgets(context, appWidgetManager, currencySymbol, topGoal, goalSavedAmount)
                updateRecentTransactionsWidgets(context, appWidgetManager)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun updateQuickActionWidgets(
        context: Context,
        manager: AppWidgetManager,
        isPrivacy: Boolean,
        currencySymbol: String,
        netBalance: Double,
        monthIncome: Double,
        monthExpense: Double
    ) {
        val componentName = ComponentName(context, QuickActionWidgetProvider::class.java)
        val ids = manager.getAppWidgetIds(componentName)
        if (ids.isEmpty()) return

        for (appWidgetId in ids) {
            val views = RemoteViews(context.packageName, R.layout.widget_quick_action)

            // Balance
            val balanceText = if (isPrivacy) "••••••" else "$currencySymbol ${amountFormat.format(netBalance)}"
            views.setTextViewText(R.id.tv_total_balance, balanceText)

            // Income / Expense
            val incomeText = if (isPrivacy) "+$currencySymbol •••" else "+$currencySymbol ${compactFormat.format(monthIncome)}"
            val expenseText = if (isPrivacy) "-$currencySymbol •••" else "-$currencySymbol ${compactFormat.format(monthExpense)}"
            views.setTextViewText(R.id.tv_month_income, incomeText)
            views.setTextViewText(R.id.tv_month_expense, expenseText)

            // Intents
            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val openAppPending = PendingIntent.getActivity(
                context, 100, openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, openAppPending)

            // Privacy toggle
            val privacyIntent = Intent(context, QuickActionWidgetProvider::class.java).apply {
                action = ACTION_TOGGLE_PRIVACY
            }
            val privacyPending = PendingIntent.getBroadcast(
                context, 101, privacyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_toggle_privacy, privacyPending)

            // Refresh
            val refreshIntent = Intent(context, QuickActionWidgetProvider::class.java).apply {
                action = ACTION_REFRESH_WIDGETS
            }
            val refreshPending = PendingIntent.getBroadcast(
                context, 102, refreshIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_refresh, refreshPending)

            // Add Expense
            val expenseIntent = Intent(context, MainActivity::class.java).apply {
                action = ACTION_ADD_EXPENSE
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val expensePending = PendingIntent.getActivity(
                context, 103, expenseIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_action_expense, expensePending)

            // Add Income
            val incomeIntent = Intent(context, MainActivity::class.java).apply {
                action = ACTION_ADD_INCOME
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val incomePending = PendingIntent.getActivity(
                context, 104, incomeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_action_income, incomePending)

            // Add Transfer
            val transferIntent = Intent(context, MainActivity::class.java).apply {
                action = ACTION_ADD_TRANSFER
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val transferPending = PendingIntent.getActivity(
                context, 105, transferIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_action_transfer, transferPending)

            // Add Wishlist
            val wishlistIntent = Intent(context, MainActivity::class.java).apply {
                action = ACTION_ADD_WISHLIST
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val wishlistPending = PendingIntent.getActivity(
                context, 106, wishlistIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_action_wishlist, wishlistPending)

            manager.updateAppWidget(appWidgetId, views)
        }
    }

    private fun updateQuickAddWidgets(context: Context, manager: AppWidgetManager) {
        val componentName = ComponentName(context, QuickAddWidgetProvider::class.java)
        val ids = manager.getAppWidgetIds(componentName)
        if (ids.isEmpty()) return

        for (appWidgetId in ids) {
            val views = RemoteViews(context.packageName, R.layout.widget_quick_add)

            // Open app on header click
            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val openAppPending = PendingIntent.getActivity(
                context, 200, openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_quick_add_root, openAppPending)

            // Primary Quick Add Transaction
            val txIntent = Intent(context, MainActivity::class.java).apply {
                action = ACTION_ADD_TRANSACTION
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val txPending = PendingIntent.getActivity(
                context, 201, txIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_quick_add_transaction, txPending)

            // Primary Quick Add Wishlist
            val wishIntent = Intent(context, MainActivity::class.java).apply {
                action = ACTION_ADD_WISHLIST
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val wishPending = PendingIntent.getActivity(
                context, 202, wishIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_quick_add_wishlist, wishPending)

            // Sub-button: + Expense
            val expenseIntent = Intent(context, MainActivity::class.java).apply {
                action = ACTION_ADD_EXPENSE
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val expensePending = PendingIntent.getActivity(
                context, 203, expenseIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_sub_add_expense, expensePending)

            // Sub-button: + Income
            val incomeIntent = Intent(context, MainActivity::class.java).apply {
                action = ACTION_ADD_INCOME
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val incomePending = PendingIntent.getActivity(
                context, 204, incomeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_sub_add_income, incomePending)

            // Sub-button: + Wishlist
            views.setOnClickPendingIntent(R.id.btn_sub_add_wishlist, wishPending)

            manager.updateAppWidget(appWidgetId, views)
        }
    }

    private fun updateQuickAddTransactionWidgets(context: Context, manager: AppWidgetManager) {
        val componentName = ComponentName(context, QuickAddTransactionWidgetProvider::class.java)
        val ids = manager.getAppWidgetIds(componentName)
        if (ids.isEmpty()) return

        for (appWidgetId in ids) {
            val views = RemoteViews(context.packageName, R.layout.widget_quick_add_transaction)

            val txIntent = Intent(context, MainActivity::class.java).apply {
                action = ACTION_ADD_TRANSACTION
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val txPending = PendingIntent.getActivity(
                context, 210, txIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root_add_transaction, txPending)
            views.setOnClickPendingIntent(R.id.btn_add_transaction_icon, txPending)

            manager.updateAppWidget(appWidgetId, views)
        }
    }

    private fun updateQuickAddWishlistWidgets(context: Context, manager: AppWidgetManager) {
        val componentName = ComponentName(context, QuickAddWishlistWidgetProvider::class.java)
        val ids = manager.getAppWidgetIds(componentName)
        if (ids.isEmpty()) return

        for (appWidgetId in ids) {
            val views = RemoteViews(context.packageName, R.layout.widget_quick_add_wishlist)

            val wishIntent = Intent(context, MainActivity::class.java).apply {
                action = ACTION_ADD_WISHLIST
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val wishPending = PendingIntent.getActivity(
                context, 220, wishIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root_add_wishlist, wishPending)

            manager.updateAppWidget(appWidgetId, views)
        }
    }

    private fun updateBudgetMeterWidgets(
        context: Context,
        manager: AppWidgetManager,
        currencySymbol: String,
        year: Int,
        month: Int,
        totalBudget: Double,
        spent: Double,
        safeToSpend: Double
    ) {
        val componentName = ComponentName(context, BudgetMeterWidgetProvider::class.java)
        val ids = manager.getAppWidgetIds(componentName)
        if (ids.isEmpty()) return

        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
        }
        val monthName = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time).uppercase()

        val percent = if (totalBudget > 0.0) {
            ((spent / totalBudget) * 100.0).toInt().coerceAtLeast(0)
        } else 0

        val (statusText, statusColor) = when {
            totalBudget <= 0.0 -> "No Budget" to 0xFF94A3B8.toInt()
            percent > 100 -> "Over Budget" to 0xFFEF4444.toInt()
            percent >= 85 -> "Warning" to 0xFFF59E0B.toInt()
            else -> "On Track" to 0xFF10B981.toInt()
        }

        for (appWidgetId in ids) {
            val views = RemoteViews(context.packageName, R.layout.widget_budget_meter)
            views.setTextViewText(R.id.tv_budget_month, monthName)
            views.setTextViewText(R.id.tv_budget_status, statusText)
            views.setTextColor(R.id.tv_budget_status, statusColor)

            views.setTextViewText(R.id.tv_budget_spent, "$currencySymbol ${compactFormat.format(spent)}")
            views.setTextViewText(R.id.tv_budget_total, "/ $currencySymbol ${compactFormat.format(totalBudget)}")
            views.setTextViewText(R.id.tv_budget_percent, "$percent%")
            views.setProgressBar(R.id.pb_budget_progress, 100, percent.coerceAtMost(100), false)

            views.setTextViewText(R.id.tv_safe_to_spend, "$currencySymbol ${compactFormat.format(safeToSpend)} / day")

            // Intents
            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val openAppPending = PendingIntent.getActivity(
                context, 200, openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_budget_root, openAppPending)

            val refreshIntent = Intent(context, BudgetMeterWidgetProvider::class.java).apply {
                action = ACTION_REFRESH_WIDGETS
            }
            val refreshPending = PendingIntent.getBroadcast(
                context, 201, refreshIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_budget_refresh, refreshPending)

            val addIntent = Intent(context, MainActivity::class.java).apply {
                action = ACTION_ADD_EXPENSE
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val addPending = PendingIntent.getActivity(
                context, 202, addIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_budget_add, addPending)

            manager.updateAppWidget(appWidgetId, views)
        }
    }

    private fun updateSavingsGoalWidgets(
        context: Context,
        manager: AppWidgetManager,
        currencySymbol: String,
        goal: com.example.data.model.SavingsGoal?,
        savedAmount: Double
    ) {
        val componentName = ComponentName(context, SavingsGoalWidgetProvider::class.java)
        val ids = manager.getAppWidgetIds(componentName)
        if (ids.isEmpty()) return

        for (appWidgetId in ids) {
            val views = RemoteViews(context.packageName, R.layout.widget_savings_goal)

            if (goal != null) {
                views.setTextViewText(R.id.tv_goal_title, goal.name)
                views.setTextViewText(R.id.tv_goal_saved, "$currencySymbol ${compactFormat.format(savedAmount)}")
                views.setTextViewText(R.id.tv_goal_target, "/ $currencySymbol ${compactFormat.format(goal.targetAmount)}")

                val percent = if (goal.targetAmount > 0.0) {
                    ((savedAmount / goal.targetAmount) * 100.0).toInt().coerceAtLeast(0)
                } else 0
                views.setTextViewText(R.id.tv_goal_percent, "$percent%")
                views.setProgressBar(R.id.pb_goal_progress, 100, percent.coerceAtMost(100), false)

                val remaining = (goal.targetAmount - savedAmount).coerceAtLeast(0.0)
                views.setTextViewText(R.id.tv_goal_remaining, "$currencySymbol ${compactFormat.format(remaining)} to go")
            } else {
                views.setTextViewText(R.id.tv_goal_title, "No Active Goal")
                views.setTextViewText(R.id.tv_goal_saved, "$currencySymbol 0")
                views.setTextViewText(R.id.tv_goal_target, "/ $currencySymbol 0")
                views.setTextViewText(R.id.tv_goal_percent, "0%")
                views.setProgressBar(R.id.pb_goal_progress, 100, 0, false)
                views.setTextViewText(R.id.tv_goal_remaining, "Create a goal in Budgeter")
            }

            // Click root -> open goals in app
            val openGoalsIntent = Intent(context, MainActivity::class.java).apply {
                action = ACTION_VIEW_GOALS
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val openGoalsPending = PendingIntent.getActivity(
                context, 300, openGoalsIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_goal_root, openGoalsPending)

            val refreshIntent = Intent(context, SavingsGoalWidgetProvider::class.java).apply {
                action = ACTION_REFRESH_WIDGETS
            }
            val refreshPending = PendingIntent.getBroadcast(
                context, 301, refreshIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_goal_refresh, refreshPending)

            manager.updateAppWidget(appWidgetId, views)
        }
    }

    private fun updateRecentTransactionsWidgets(
        context: Context,
        manager: AppWidgetManager
    ) {
        val componentName = ComponentName(context, RecentTransactionsWidgetProvider::class.java)
        val ids = manager.getAppWidgetIds(componentName)
        if (ids.isEmpty()) return

        for (appWidgetId in ids) {
            val views = RemoteViews(context.packageName, R.layout.widget_recent_transactions)

            // Set up RemoteViewsService intent for the ListView
            val serviceIntent = Intent(context, RecentTransactionsRemoteViewsService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            views.setRemoteAdapter(R.id.lv_recent_transactions, serviceIntent)
            views.setEmptyView(R.id.lv_recent_transactions, R.id.tv_empty_transactions)

            // Template PendingIntent for list items
            val itemIntent = Intent(context, MainActivity::class.java).apply {
                action = ACTION_VIEW_TRANSACTIONS
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val itemPendingIntent = PendingIntent.getActivity(
                context, 400, itemIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
            views.setPendingIntentTemplate(R.id.lv_recent_transactions, itemPendingIntent)

            // Add button
            val addIntent = Intent(context, MainActivity::class.java).apply {
                action = ACTION_ADD_EXPENSE
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val addPending = PendingIntent.getActivity(
                context, 401, addIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_recent_add, addPending)

            // Refresh button
            val refreshIntent = Intent(context, RecentTransactionsWidgetProvider::class.java).apply {
                action = ACTION_REFRESH_WIDGETS
            }
            val refreshPending = PendingIntent.getBroadcast(
                context, 402, refreshIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_recent_refresh, refreshPending)

            manager.updateAppWidget(appWidgetId, views)
            manager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.lv_recent_transactions)
        }
    }
}
