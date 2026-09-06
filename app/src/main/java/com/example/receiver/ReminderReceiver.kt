package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.local.AppDatabase
import com.example.data.model.LanguageMode
import com.example.ui.theme.ThemePreferences
import com.example.util.CurrencyPreferences
import com.example.util.NotificationHelper
import com.example.util.NotificationPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        val notifPrefs = NotificationPreferences.getInstance(context)
        val notifConfig = notifPrefs.config.value
        val appPrefs = context.getSharedPreferences("budget_app_prefs", Context.MODE_PRIVATE)
        val langSaved = appPrefs.getString("app_language_mode", LanguageMode.ENGLISH.name) ?: LanguageMode.ENGLISH.name
        val languageMode = try { LanguageMode.valueOf(langSaved) } catch (_: Exception) { LanguageMode.ENGLISH }

        when (action) {
            ACTION_DAILY_REMINDER -> {
                if (notifConfig.isDailyReminderEnabled) {
                    NotificationHelper.sendDailyReminder(
                        context = context,
                        customMessage = notifConfig.customDailyMessage,
                        isBangla = languageMode == LanguageMode.BANGLA
                    )
                    // Reschedule for next day
                    NotificationHelper.scheduleDailyReminder(
                        context,
                        notifConfig.dailyReminderHour,
                        notifConfig.dailyReminderMinute
                    )
                }

                // Also check recurring bills if enabled
                if (notifConfig.isBillReminderEnabled) {
                    checkAndNotifyRecurringBills(context, notifConfig.billReminderDaysInAdvance, languageMode)
                }
            }

            ACTION_BILL_REMINDER -> {
                if (notifConfig.isBillReminderEnabled) {
                    checkAndNotifyRecurringBills(context, notifConfig.billReminderDaysInAdvance, languageMode)
                }
            }

            Intent.ACTION_BOOT_COMPLETED -> {
                if (notifConfig.isDailyReminderEnabled) {
                    NotificationHelper.scheduleDailyReminder(
                        context,
                        notifConfig.dailyReminderHour,
                        notifConfig.dailyReminderMinute
                    )
                }
            }
        }
    }

    private fun checkAndNotifyRecurringBills(
        context: Context,
        daysInAdvance: Int,
        languageMode: LanguageMode
    ) {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            try {
                val appPrefs = context.getSharedPreferences("budget_app_prefs", Context.MODE_PRIVATE)
                val isDemoMode = appPrefs.getBoolean("app_demo_mode", true)
                val db = AppDatabase.getDatabase(context, scope, isDemoMode)
                val allBills = db.recurringBillDao().getAllBillsSnapshot()
                val activeBills = allBills.filter { it.isActive }
                val isBangla = languageMode == LanguageMode.BANGLA
                val currencySymbol = CurrencyPreferences.getInstance(context).config.value.activeSymbol

                val now = System.currentTimeMillis()
                val oneDayMs = 24 * 60 * 60 * 1000L

                activeBills.forEach { bill ->
                    val diffDays = ((bill.nextDueDateEpochMs - now) / oneDayMs).toInt()

                    if (diffDays in 0..daysInAdvance) {
                        val dueText = when (diffDays) {
                            0 -> if (isBangla) "আজই পরিশোধের শেষ সময়!" else "due today!"
                            1 -> if (isBangla) "আগামীকাল পরিশোধের সময়" else "due tomorrow"
                            else -> if (isBangla) "$diffDays দিন পর পরিশোধের সময়" else "due in $diffDays days"
                        }
                        val formattedAmount = "$currencySymbol ${"%.2f".format(bill.amount)}"

                        NotificationHelper.sendBillReminder(
                            context = context,
                            billId = bill.id,
                            billTitle = bill.title,
                            amountFormatted = formattedAmount,
                            dueText = dueText,
                            isBangla = isBangla
                        )
                    }
                }
            } catch (e: Exception) {
                // Database query error handling
            }
        }
    }

    companion object {
        const val ACTION_DAILY_REMINDER = "com.example.budgeter.ACTION_DAILY_REMINDER"
        const val ACTION_BILL_REMINDER = "com.example.budgeter.ACTION_BILL_REMINDER"
    }
}
