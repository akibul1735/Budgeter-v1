package com.example.util

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class NotificationConfig(
    val isDailyReminderEnabled: Boolean = true,
    val dailyReminderHour: Int = 21, // 9:00 PM
    val dailyReminderMinute: Int = 0,
    val isBillReminderEnabled: Boolean = true,
    val billReminderDaysInAdvance: Int = 1, // 1 day before
    val billReminderHour: Int = 9, // 9:00 AM
    val isBudgetAlertEnabled: Boolean = true,
    val customDailyMessage: String = "Don't forget to record today's expenses and income in Budgeter!"
)

class NotificationPreferences private constructor(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("budgeter_notification_prefs", Context.MODE_PRIVATE)

    private val _config = MutableStateFlow(loadConfig())
    val config: StateFlow<NotificationConfig> = _config.asStateFlow()

    private fun loadConfig(): NotificationConfig {
        return NotificationConfig(
            isDailyReminderEnabled = prefs.getBoolean(KEY_DAILY_REMINDER_ENABLED, true),
            dailyReminderHour = prefs.getInt(KEY_DAILY_REMINDER_HOUR, 21),
            dailyReminderMinute = prefs.getInt(KEY_DAILY_REMINDER_MINUTE, 0),
            isBillReminderEnabled = prefs.getBoolean(KEY_BILL_REMINDER_ENABLED, true),
            billReminderDaysInAdvance = prefs.getInt(KEY_BILL_REMINDER_DAYS_IN_ADVANCE, 1),
            billReminderHour = prefs.getInt(KEY_BILL_REMINDER_HOUR, 9),
            isBudgetAlertEnabled = prefs.getBoolean(KEY_BUDGET_ALERT_ENABLED, true),
            customDailyMessage = prefs.getString(
                KEY_CUSTOM_DAILY_MESSAGE,
                "Don't forget to record today's expenses and income in Budgeter!"
            ) ?: "Don't forget to record today's expenses and income in Budgeter!"
        )
    }

    fun updateConfig(update: (NotificationConfig) -> NotificationConfig) {
        val newConfig = update(_config.value)
        prefs.edit()
            .putBoolean(KEY_DAILY_REMINDER_ENABLED, newConfig.isDailyReminderEnabled)
            .putInt(KEY_DAILY_REMINDER_HOUR, newConfig.dailyReminderHour)
            .putInt(KEY_DAILY_REMINDER_MINUTE, newConfig.dailyReminderMinute)
            .putBoolean(KEY_BILL_REMINDER_ENABLED, newConfig.isBillReminderEnabled)
            .putInt(KEY_BILL_REMINDER_DAYS_IN_ADVANCE, newConfig.billReminderDaysInAdvance)
            .putInt(KEY_BILL_REMINDER_HOUR, newConfig.billReminderHour)
            .putBoolean(KEY_BUDGET_ALERT_ENABLED, newConfig.isBudgetAlertEnabled)
            .putString(KEY_CUSTOM_DAILY_MESSAGE, newConfig.customDailyMessage)
            .apply()
        _config.value = newConfig
    }

    companion object {
        private const val KEY_DAILY_REMINDER_ENABLED = "daily_reminder_enabled"
        private const val KEY_DAILY_REMINDER_HOUR = "daily_reminder_hour"
        private const val KEY_DAILY_REMINDER_MINUTE = "daily_reminder_minute"
        private const val KEY_BILL_REMINDER_ENABLED = "bill_reminder_enabled"
        private const val KEY_BILL_REMINDER_DAYS_IN_ADVANCE = "bill_reminder_days_in_advance"
        private const val KEY_BILL_REMINDER_HOUR = "bill_reminder_hour"
        private const val KEY_BUDGET_ALERT_ENABLED = "budget_alert_enabled"
        private const val KEY_CUSTOM_DAILY_MESSAGE = "custom_daily_message"

        @Volatile
        private var instance: NotificationPreferences? = null

        fun getInstance(context: Context): NotificationPreferences {
            return instance ?: synchronized(this) {
                instance ?: NotificationPreferences(context).also { instance = it }
            }
        }
    }
}
