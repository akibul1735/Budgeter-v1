package com.example.ui.screens.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.LanguageMode
import com.example.ui.components.AppTabHeader
import com.example.ui.theme.SolidPrimary
import com.example.util.NotificationConfig
import com.example.util.NotificationHelper
import com.example.util.NotificationPreferences

@Composable
fun NotificationSettingsPage(
    languageMode: LanguageMode,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val notifPrefs = remember { NotificationPreferences.getInstance(context) }
    val notifConfig by notifPrefs.config.collectAsStateWithLifecycle()
    val isBangla = languageMode == LanguageMode.BANGLA

    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        if (isGranted) {
            NotificationHelper.createNotificationChannels(context)
            if (notifConfig.isDailyReminderEnabled) {
                NotificationHelper.scheduleDailyReminder(
                    context,
                    notifConfig.dailyReminderHour,
                    notifConfig.dailyReminderMinute
                )
            }
        }
    }

    var testNotificationSent by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("notification_settings_page")
    ) {
        AppTabHeader(
            title = if (isBangla) "ফোন নোটিফিকেশন" else "Phone Notifications",
            tabIcon = Icons.Default.NotificationsNone,
            onBack = onBack,
            autoHideOnScroll = false
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Permission status card
            if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isBangla) "নোটিফিকেশন অনুমতি নেই" else "Notifications Permission Required",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = if (isBangla) "সময়মতো সতর্কবার্তা ও রিমাইন্ডার পেতে অনুমতি দিন।" else "Grant permission to receive reminder notifications.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(if (isBangla) "অনুমতি দিন" else "Grant", fontSize = 12.sp)
                        }
                    }
                }
            }

            // Daily Expense Reminder Section
            Text(
                text = if (isBangla) "দৈনিক হিসাব রিমাইন্ডার" else "Daily Expense Reminder",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Alarm,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = if (isBangla) "প্রতিদিন মনে করিয়ে দেওয়া" else "Daily Reminder",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (notifConfig.isDailyReminderEnabled)
                                        String.format("%02d:%02d", notifConfig.dailyReminderHour, notifConfig.dailyReminderMinute)
                                    else
                                        if (isBangla) "বন্ধ" else "Disabled",
                                    fontSize = 11.sp,
                                    color = if (notifConfig.isDailyReminderEnabled) SolidPrimary else MaterialTheme.colorScheme.outline
                                )
                            }
                        }

                        Switch(
                            checked = notifConfig.isDailyReminderEnabled,
                            onCheckedChange = { enabled ->
                                notifPrefs.updateConfig { it.copy(isDailyReminderEnabled = enabled) }
                                if (enabled) {
                                    NotificationHelper.scheduleDailyReminder(
                                        context,
                                        notifConfig.dailyReminderHour,
                                        notifConfig.dailyReminderMinute
                                    )
                                    if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                } else {
                                    NotificationHelper.cancelDailyReminder(context)
                                }
                            }
                        )
                    }

                    if (notifConfig.isDailyReminderEnabled) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                        Text(
                            text = if (isBangla) "রিমাইন্ডারের সময় নির্বাচন করুন" else "Select Reminder Time",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val presetHours = listOf(
                                Pair(20, "8:00 PM (২০:০০)"),
                                Pair(21, "9:00 PM (২১:০০)"),
                                Pair(22, "10:00 PM (২২:০০)")
                            )
                            presetHours.forEach { (hour, label) ->
                                val isSelected = notifConfig.dailyReminderHour == hour && notifConfig.dailyReminderMinute == 0
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        notifPrefs.updateConfig {
                                            it.copy(dailyReminderHour = hour, dailyReminderMinute = 0)
                                        }
                                        NotificationHelper.scheduleDailyReminder(context, hour, 0)
                                    },
                                    label = { Text(label.take(7), fontSize = 11.sp) },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // Recurring Bill Alerts
            Text(
                text = if (isBangla) "বিল পরিশোধের সতর্কতা" else "Recurring Bill Due Alerts",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.ReceiptLong,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = if (isBangla) "বিল সতর্কবার্তা" else "Bill Due Notification",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (notifConfig.isBillReminderEnabled)
                                        if (isBangla) "${notifConfig.billReminderDaysInAdvance} দিন পূর্বে সতর্ক করবে" else "Alert ${notifConfig.billReminderDaysInAdvance} day(s) before due date"
                                    else
                                        if (isBangla) "নিষ্ক্রিয়" else "Disabled",
                                    fontSize = 11.sp,
                                    color = if (notifConfig.isBillReminderEnabled) SolidPrimary else MaterialTheme.colorScheme.outline
                                )
                            }
                        }

                        Switch(
                            checked = notifConfig.isBillReminderEnabled,
                            onCheckedChange = { enabled ->
                                notifPrefs.updateConfig { it.copy(isBillReminderEnabled = enabled) }
                                if (enabled && !hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            }
                        )
                    }

                    if (notifConfig.isBillReminderEnabled) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                        Text(
                            text = if (isBangla) "কত দিন পূর্বে সতর্কবার্তা পাবেন?" else "Advance Warning Notice",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val dayOptions = listOf(
                                Pair(0, if (isBangla) "একই দিন" else "Same Day"),
                                Pair(1, if (isBangla) "১ দিন আগে" else "1 Day Before"),
                                Pair(2, if (isBangla) "২ দিন আগে" else "2 Days Before"),
                                Pair(3, if (isBangla) "৩ দিন আগে" else "3 Days Before")
                            )
                            dayOptions.forEach { (days, label) ->
                                val isSelected = notifConfig.billReminderDaysInAdvance == days
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        notifPrefs.updateConfig { it.copy(billReminderDaysInAdvance = days) }
                                    },
                                    label = { Text(label, fontSize = 10.sp) },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // Test Notification Button
            OutlinedButton(
                onClick = {
                    NotificationHelper.sendTestNotification(context, isBangla = isBangla)
                    testNotificationSent = true
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (isBangla) "টেস্ট নোটিফিকেশন পাঠান" else "Send Test Notification Now", fontSize = 12.sp)
            }

            if (testNotificationSent) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isBangla) "টেস্ট নোটিফিকেশন পাঠানো হয়েছে। আপনার ডিভাইসের নোটিফিকেশন প্যানেল চেক করুন।" else "Test notification sent. Check your device notification tray.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
