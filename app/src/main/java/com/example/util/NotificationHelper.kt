package com.example.util

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.receiver.ReminderReceiver
import java.util.Calendar

object NotificationHelper {
    const val CHANNEL_ID_DAILY = "daily_expense_reminder_channel"
    const val CHANNEL_ID_BILLS = "recurring_bills_reminder_channel"
    const val CHANNEL_ID_BUDGET = "budget_alert_channel"

    const val NOTIFICATION_ID_DAILY = 1001
    const val NOTIFICATION_ID_BILL_BASE = 2000
    const val NOTIFICATION_ID_BUDGET = 3001
    const val NOTIFICATION_ID_TEST = 9999

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val dailyChannel = NotificationChannel(
                CHANNEL_ID_DAILY,
                "Daily Expense Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders to log daily expenses and income"
                enableVibration(true)
            }

            val billsChannel = NotificationChannel(
                CHANNEL_ID_BILLS,
                "Recurring Bill Due Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for upcoming and due recurring bills"
                enableVibration(true)
            }

            val budgetChannel = NotificationChannel(
                CHANNEL_ID_BUDGET,
                "Budget Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Alerts when approaching category budget limits"
                enableVibration(true)
            }

            notificationManager.createNotificationChannels(
                listOf(dailyChannel, billsChannel, budgetChannel)
            )
        }
    }

    fun sendTestNotification(context: Context, isBangla: Boolean) {
        createNotificationChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (isBangla) "বাজেটার নোটিফিকেশন প্রস্তুত! 🔔" else "Budgeter Notification Active! 🔔"
        val message = if (isBangla)
            "আপনার ফোন নোটিফিকেশন সঠিকভাবে কনফিগার করা হয়েছে।"
        else
            "Your notifications are successfully set up and active."

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_DAILY)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_TEST, builder.build())
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }

    fun sendDailyReminder(context: Context, customMessage: String, isBangla: Boolean) {
        createNotificationChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (isBangla) "আজকের খরচের হিসাব রাখুন 📝" else "Record Today's Expenses 📝"
        val body = customMessage.ifBlank {
            if (isBangla) "বাজেট নিয়ন্ত্রণে রাখতে আজকের সব আয়-ব্যয় লিখে রাখুন।"
            else "Log today's transactions to keep your budget on track!"
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_DAILY)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_DAILY, builder.build())
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }

    fun sendBillReminder(
        context: Context,
        billId: Long,
        billTitle: String,
        amountFormatted: String,
        dueText: String,
        isBangla: Boolean
    ) {
        createNotificationChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            billId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (isBangla) "আসন্ন বিল পরিশোধের রিমাইন্ডার 💳" else "Upcoming Bill Due 💳"
        val message = if (isBangla) {
            "$billTitle ($amountFormatted) - $dueText"
        } else {
            "$billTitle ($amountFormatted) is $dueText"
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_BILLS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            val notificationId = (NOTIFICATION_ID_BILL_BASE + (billId % 1000)).toInt()
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }

    fun scheduleDailyReminder(context: Context, hour: Int, minute: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_DAILY_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            100,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            // Alarm permission fallback
        }
    }

    fun cancelDailyReminder(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_DAILY_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            100,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
