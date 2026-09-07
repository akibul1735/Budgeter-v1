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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.util.LanguageHelper
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
                .padding(horizontal = 14.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(modifier = Modifier.height(2.dp))

            // Permission status card
            if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isBangla) "নোটিফিকেশন অনুমতি প্রয়োজন" else "Notifications Permission Required",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = if (isBangla) "সময়মতো সতর্কবার্তা পেতে অনুমতি দিন।" else "Grant permission to receive reminder notifications.",
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
                            Text(if (isBangla) "অনুমতি দিন" else "Grant", fontSize = 11.sp)
                        }
                    }
                }
            }

            // Daily Expense Reminder Section
            Text(
                text = if (isBangla) "দৈনিক হিসাব রিমাইন্ডার" else "Daily Expense Reminder",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                                    text = if (isBangla) "প্রতিদিনের হিসাব লিপিবদ্ধের তাগিদ" else "Daily Expense Logging Alert",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                val hour = notifConfig.dailyReminderHour
                                val min = notifConfig.dailyReminderMinute
                                val ampm = if (hour >= 12) "PM" else "AM"
                                val displayHour = if (hour % 12 == 0) 12 else hour % 12
                                val formattedTime = String.format("%02d:%02d %s (%02d:%02d)", displayHour, min, ampm, hour, min)
                                Text(
                                    text = if (notifConfig.isDailyReminderEnabled)
                                        if (isBangla) LanguageHelper.toBanglaDigits(formattedTime) else formattedTime
                                    else
                                        if (isBangla) "নিষ্ক্রিয়" else "Disabled",
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
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = SolidPrimary)
                        )
                    }

                    if (notifConfig.isDailyReminderEnabled) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                        // Interactive Customizable Time Picker Card
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = if (isBangla) "কাস্টমাইজড রিমাইন্ডার সময়" else "Custom Reminder Time",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                // Hour & Minute Stepper Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Hour Adjuster
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(if (isBangla) "ঘণ্টা (Hour)" else "Hour", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            OutlinedIconButton(
                                                onClick = {
                                                    val newHour = (notifConfig.dailyReminderHour - 1 + 24) % 24
                                                    notifPrefs.updateConfig { it.copy(dailyReminderHour = newHour) }
                                                    NotificationHelper.scheduleDailyReminder(context, newHour, notifConfig.dailyReminderMinute)
                                                },
                                                modifier = Modifier.size(32.dp),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Icon(Icons.Default.Remove, contentDescription = "Decrease Hour", modifier = Modifier.size(16.dp))
                                            }

                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = MaterialTheme.colorScheme.surface,
                                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                                modifier = Modifier.size(width = 44.dp, height = 36.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    val hourVal = String.format("%02d", notifConfig.dailyReminderHour)
                                                    Text(
                                                        text = if (isBangla) LanguageHelper.toBanglaDigits(hourVal) else hourVal,
                                                        fontSize = 15.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }

                                            OutlinedIconButton(
                                                onClick = {
                                                    val newHour = (notifConfig.dailyReminderHour + 1) % 24
                                                    notifPrefs.updateConfig { it.copy(dailyReminderHour = newHour) }
                                                    NotificationHelper.scheduleDailyReminder(context, newHour, notifConfig.dailyReminderMinute)
                                                },
                                                modifier = Modifier.size(32.dp),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Icon(Icons.Default.Add, contentDescription = "Increase Hour", modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }

                                    Text(":", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline)

                                    // Minute Adjuster
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(if (isBangla) "মিনিট (Minute)" else "Minute", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            OutlinedIconButton(
                                                onClick = {
                                                    val newMin = (notifConfig.dailyReminderMinute - 5 + 60) % 60
                                                    notifPrefs.updateConfig { it.copy(dailyReminderMinute = newMin) }
                                                    NotificationHelper.scheduleDailyReminder(context, notifConfig.dailyReminderHour, newMin)
                                                },
                                                modifier = Modifier.size(32.dp),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Icon(Icons.Default.Remove, contentDescription = "Decrease Min", modifier = Modifier.size(16.dp))
                                            }

                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = MaterialTheme.colorScheme.surface,
                                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                                modifier = Modifier.size(width = 44.dp, height = 36.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    val minVal = String.format("%02d", notifConfig.dailyReminderMinute)
                                                    Text(
                                                        text = if (isBangla) LanguageHelper.toBanglaDigits(minVal) else minVal,
                                                        fontSize = 15.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }

                                            OutlinedIconButton(
                                                onClick = {
                                                    val newMin = (notifConfig.dailyReminderMinute + 5) % 60
                                                    notifPrefs.updateConfig { it.copy(dailyReminderMinute = newMin) }
                                                    NotificationHelper.scheduleDailyReminder(context, notifConfig.dailyReminderHour, newMin)
                                                },
                                                modifier = Modifier.size(32.dp),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Icon(Icons.Default.Add, contentDescription = "Increase Min", modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }

                                // Quick presets
                                Text(
                                    text = if (isBangla) "জনপ্রিয় সময়ের প্রিসেট:" else "Popular Time Presets:",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    val presets = listOf(
                                        Triple(8, 0, "8:00 AM"),
                                        Triple(14, 0, "2:00 PM"),
                                        Triple(20, 0, "8:00 PM"),
                                        Triple(21, 30, "9:30 PM"),
                                        Triple(22, 0, "10:00 PM")
                                    )
                                    presets.forEach { (h, m, label) ->
                                        val isSelected = notifConfig.dailyReminderHour == h && notifConfig.dailyReminderMinute == m
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = {
                                                notifPrefs.updateConfig { it.copy(dailyReminderHour = h, dailyReminderMinute = m) }
                                                NotificationHelper.scheduleDailyReminder(context, h, m)
                                            },
                                            label = {
                                                Text(
                                                    text = if (isBangla) LanguageHelper.toBanglaDigits(label) else label,
                                                    fontSize = 9.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                )
                                            },
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.weight(1f),
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                                selectedLabelColor = MaterialTheme.colorScheme.primary
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Recurring Bill Alerts (Highly Customizable Days Notice)
            Text(
                text = if (isBangla) "বিল পরিশোধের সতর্কতা" else "Recurring Bill Due Alerts",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                                    text = if (isBangla) "বিল পরিশোধের আগাম বার্তা" else "Advance Bill Due Notification",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (notifConfig.isBillReminderEnabled) {
                                        val d = notifConfig.billReminderDaysInAdvance
                                        if (d == 0) {
                                            if (isBangla) "বিল পরিশোধের একই দিন সতর্ক করবে" else "Alert on the due date"
                                        } else {
                                            if (isBangla) "${LanguageHelper.toBanglaDigits(d.toString())} দিন পূর্বে সতর্ক করবে" else "Alert $d day(s) before due date"
                                        }
                                    } else {
                                        if (isBangla) "নিষ্ক্রিয়" else "Disabled"
                                    },
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
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = SolidPrimary)
                        )
                    }

                    if (notifConfig.isBillReminderEnabled) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isBangla) "কত দিন পূর্বে সতর্কতা পাবেন?" else "Advance Notice (Days Before):",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    // Stepper for custom days from 0 to 30
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        OutlinedIconButton(
                                            onClick = {
                                                val current = notifConfig.billReminderDaysInAdvance
                                                if (current > 0) {
                                                    notifPrefs.updateConfig { it.copy(billReminderDaysInAdvance = current - 1) }
                                                }
                                            },
                                            modifier = Modifier.size(30.dp),
                                            shape = RoundedCornerShape(6.dp),
                                            enabled = notifConfig.billReminderDaysInAdvance > 0
                                        ) {
                                            Icon(Icons.Default.Remove, contentDescription = "Decrease Days", modifier = Modifier.size(14.dp))
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = MaterialTheme.colorScheme.surface,
                                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                            modifier = Modifier.size(width = 54.dp, height = 30.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                val d = notifConfig.billReminderDaysInAdvance
                                                val dLabel = if (d == 0) (if (isBangla) "০ দিন" else "0 d") else (if (isBangla) "${LanguageHelper.toBanglaDigits(d.toString())} দিন" else "$d d")
                                                Text(
                                                    text = dLabel,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }

                                        OutlinedIconButton(
                                            onClick = {
                                                val current = notifConfig.billReminderDaysInAdvance
                                                if (current < 30) {
                                                    notifPrefs.updateConfig { it.copy(billReminderDaysInAdvance = current + 1) }
                                                }
                                            },
                                            modifier = Modifier.size(30.dp),
                                            shape = RoundedCornerShape(6.dp),
                                            enabled = notifConfig.billReminderDaysInAdvance < 30
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = "Increase Days", modifier = Modifier.size(14.dp))
                                        }
                                    }
                                }

                                // Quick preset day chips
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    val dayOptions = listOf(
                                        Pair(0, if (isBangla) "একই দিন" else "Same Day"),
                                        Pair(1, if (isBangla) "১ দিন" else "1 Day"),
                                        Pair(2, if (isBangla) "২ দিন" else "2 Days"),
                                        Pair(3, if (isBangla) "৩ দিন" else "3 Days"),
                                        Pair(7, if (isBangla) "৭ দিন" else "7 Days"),
                                        Pair(14, if (isBangla) "১৪ দিন" else "14 Days")
                                    )
                                    dayOptions.forEach { (days, label) ->
                                        val isSelected = notifConfig.billReminderDaysInAdvance == days
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = {
                                                notifPrefs.updateConfig { it.copy(billReminderDaysInAdvance = days) }
                                            },
                                            label = {
                                                Text(
                                                    text = label,
                                                    fontSize = 9.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                )
                                            },
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.weight(1f),
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                                selectedLabelColor = MaterialTheme.colorScheme.primary
                                            )
                                        )
                                    }
                                }
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
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
