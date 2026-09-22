package com.example.ui.screens.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.LanguageMode
import com.example.ui.components.AppPermissionType
import com.example.ui.components.PermissionRationaleDialog
import com.example.ui.components.AppTabHeader
import com.example.util.LanguageHelper
import com.example.util.NotificationConfig
import com.example.util.NotificationHelper
import com.example.util.NotificationPreferences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsPage(
    languageMode: LanguageMode,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val notifPrefs = remember { NotificationPreferences.getInstance(context) }
    val notifConfig by notifPrefs.config.collectAsStateWithLifecycle()
    val isBangla = languageMode == LanguageMode.BANGLA

    var showTimePickerSheet by remember { mutableStateOf(false) }
    var showBillNoticeSheet by remember { mutableStateOf(false) }
    var testNotificationSent by remember { mutableStateOf(false) }
    var showNotificationRationaleDialog by remember { mutableStateOf(false) }

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

    val hour = notifConfig.dailyReminderHour
    val min = notifConfig.dailyReminderMinute
    val ampm = if (hour >= 12) "PM" else "AM"
    val displayHour = if (hour % 12 == 0) 12 else hour % 12
    val formattedTimeRaw = String.format("%02d:%02d %s", displayHour, min, ampm)
    val formattedTime = if (isBangla) LanguageHelper.toBanglaDigits(formattedTimeRaw) else formattedTimeRaw

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

            // 1. Live Notification Status Hero Preview Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isBangla) "নোটিফিকেশন স্ট্যাটাস" else "Notification Service Status",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = if (notifConfig.isDailyReminderEnabled) {
                                if (isBangla) "দৈনিক রিমাইন্ডার: $formattedTime" else "Daily Reminder: $formattedTime"
                            } else {
                                if (isBangla) "রিমাইন্ডার নিষ্ক্রিয়" else "Reminders Inactive"
                            },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (notifConfig.isBillReminderEnabled) {
                                if (isBangla) "বিল সতর্কতা: ${notifConfig.billReminderDaysInAdvance} দিন পূর্বে" else "Bill Due Alert: ${notifConfig.billReminderDaysInAdvance} days before"
                            } else {
                                if (isBangla) "বিল সতর্কতা বন্ধ" else "Bill alert disabled"
                            },
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (notifConfig.isDailyReminderEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsNone,
                                contentDescription = null,
                                tint = if (notifConfig.isDailyReminderEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (notifConfig.isDailyReminderEnabled) (if (isBangla) "সক্রিয়" else "Active") else (if (isBangla) "বন্ধ" else "Off"),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (notifConfig.isDailyReminderEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }

            // Permission Warning Card if needed
            if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                OutlinedCard(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isBangla) "নোটিফিকেশন অনুমতি প্রয়োজন" else "Notification Permission Needed",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = if (isBangla) "সময়মতো সতর্কবার্তা ও রিমাইন্ডার পেতে অনুমতি দিন।" else "Grant Android permission to receive reminder notifications.",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = { showNotificationRationaleDialog = true },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(if (isBangla) "অনুমতি দিন" else "Grant", fontSize = 12.sp)
                        }
                    }
                }
            }

            // 2. Section: Alert Configuration
            Text(
                text = if (isBangla) "রিমাইন্ডার ও অ্যালার্ট কনফিগারেশন" else "Reminders & Alerts Setup",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Daily Reminder Card
            OutlinedCard(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    if (notifConfig.isDailyReminderEnabled) {
                                        showTimePickerSheet = true
                                    }
                                }
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(38.dp)
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
                                    text = if (isBangla) "দৈনিক হিসাব রিমাইন্ডার" else "Daily Expense Reminder",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (notifConfig.isDailyReminderEnabled) {
                                        if (isBangla) "প্রতিদিন $formattedTime এ সতর্ক করবে (ট্যাপ করে পরিবর্তন করুন)" else "Every day at $formattedTime (Tap to edit)"
                                    } else {
                                        if (isBangla) "নিষ্ক্রিয়" else "Disabled"
                                    },
                                    fontSize = 12.sp,
                                    color = if (notifConfig.isDailyReminderEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Switch(
                            checked = notifConfig.isDailyReminderEnabled,
                            onCheckedChange = { enabled ->
                                notifPrefs.updateConfig { it.copy(isDailyReminderEnabled = enabled) }
                                if (enabled) {
                                    NotificationHelper.scheduleDailyReminder(context, notifConfig.dailyReminderHour, notifConfig.dailyReminderMinute)
                                    if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        showNotificationRationaleDialog = true
                                    }
                                } else {
                                    NotificationHelper.cancelDailyReminder(context)
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }

            // Recurring Bill Alerts Card
            OutlinedCard(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    if (notifConfig.isBillReminderEnabled) {
                                        showBillNoticeSheet = true
                                    }
                                }
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.ReceiptLong,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = if (isBangla) "বিল পরিশোধের আগাম সতর্কতা" else "Advance Bill Due Alerts",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (notifConfig.isBillReminderEnabled) {
                                        val d = notifConfig.billReminderDaysInAdvance
                                        if (d == 0) {
                                            if (isBangla) "বিলের দিন সতর্ক করবে (ট্যাপ করে পরিবর্তন করুন)" else "Alert on the due date (Tap to edit)"
                                        } else {
                                            if (isBangla) "${LanguageHelper.toBanglaDigits(d.toString())} দিন পূর্বে সতর্ক করবে (ট্যাপ করুন)" else "$d day(s) before due date (Tap to edit)"
                                        }
                                    } else {
                                        if (isBangla) "নিষ্ক্রিয়" else "Disabled"
                                    },
                                    fontSize = 12.sp,
                                    color = if (notifConfig.isBillReminderEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Switch(
                            checked = notifConfig.isBillReminderEnabled,
                            onCheckedChange = { enabled ->
                                notifPrefs.updateConfig { it.copy(isBillReminderEnabled = enabled) }
                                if (enabled && !hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    showNotificationRationaleDialog = true
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }

            // Test Notification Button
            OutlinedButton(
                onClick = {
                    NotificationHelper.sendTestNotification(context, isBangla = isBangla)
                    testNotificationSent = true
                },
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isBangla) "টেস্ট নোটিফিকেশন পাঠান" else "Send Test Notification Now",
                    fontSize = 12.5.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            AnimatedVisibility(visible = testNotificationSent) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isBangla) "টেস্ট নোটিফিকেশন পাঠানো হয়েছে। আপনার ডিভাইসের নোটিফিকেশন ট্রে চেক করুন।" else "Test notification triggered. Please check your notification tray.",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Modal Bottom Sheet: Reminder Time Picker
    if (showNotificationRationaleDialog) {
        PermissionRationaleDialog(
            permissionType = AppPermissionType.NOTIFICATION,
            languageMode = if (isBangla) LanguageMode.BANGLA else LanguageMode.ENGLISH,
            onConfirm = {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            },
            onDismiss = {
                showNotificationRationaleDialog = false
            }
        )
    }

    if (showTimePickerSheet) {
        ModalBottomSheet(
            onDismissRequest = { showTimePickerSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .padding(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isBangla) "দৈনিক রিমাইন্ডার সময় নির্ধারণ" else "Daily Reminder Time",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isBangla) "প্রতিদিন কোন সময়ে আপনাকে নোটিফিকেশন পাঠানো হবে" else "Select the exact time for daily alert",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    IconButton(onClick = { showTimePickerSheet = false }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                // Time Steppers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Hour Stepper
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(if (isBangla) "ঘণ্টা (Hour)" else "Hour", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedIconButton(
                                onClick = {
                                    val newHour = (notifConfig.dailyReminderHour - 1 + 24) % 24
                                    notifPrefs.updateConfig { it.copy(dailyReminderHour = newHour) }
                                    NotificationHelper.scheduleDailyReminder(context, newHour, notifConfig.dailyReminderMinute)
                                },
                                modifier = Modifier.size(36.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease Hour", modifier = Modifier.size(16.dp))
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                                modifier = Modifier.size(width = 54.dp, height = 40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    val hourVal = String.format("%02d", notifConfig.dailyReminderHour)
                                    Text(
                                        text = if (isBangla) LanguageHelper.toBanglaDigits(hourVal) else hourVal,
                                        fontSize = 18.sp,
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
                                modifier = Modifier.size(36.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Increase Hour", modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Text(":", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline)

                    // Minute Stepper
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(if (isBangla) "মিনিট (Minute)" else "Minute", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedIconButton(
                                onClick = {
                                    val newMin = (notifConfig.dailyReminderMinute - 5 + 60) % 60
                                    notifPrefs.updateConfig { it.copy(dailyReminderMinute = newMin) }
                                    NotificationHelper.scheduleDailyReminder(context, notifConfig.dailyReminderHour, newMin)
                                },
                                modifier = Modifier.size(36.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease Min", modifier = Modifier.size(16.dp))
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                                modifier = Modifier.size(width = 54.dp, height = 40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    val minVal = String.format("%02d", notifConfig.dailyReminderMinute)
                                    Text(
                                        text = if (isBangla) LanguageHelper.toBanglaDigits(minVal) else minVal,
                                        fontSize = 18.sp,
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
                                modifier = Modifier.size(36.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Increase Min", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                // Quick Presets
                Text(
                    text = if (isBangla) "জনপ্রিয় সময়ের প্রিসেট:" else "Popular Time Presets:",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
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
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }

                Button(
                    onClick = { showTimePickerSheet = false },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isBangla) "সম্পন্ন" else "Done", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Modal Bottom Sheet: Bill Advance Notice Days
    if (showBillNoticeSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBillNoticeSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .padding(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isBangla) "বিল সতর্কতার সময় নির্বাচন" else "Advance Due Notice Days",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isBangla) "বিলের শেষ তারিখের কত দিন পূর্বে নোটিফিকেশন পাবেন" else "Choose how many days prior to alert",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    IconButton(onClick = { showBillNoticeSheet = false }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                val dayOptions = listOf(
                    Pair(0, if (isBangla) "একই দিন (বিলের শেষ তারিখের সকালে)" else "Same Day (Morning of due date)"),
                    Pair(1, if (isBangla) "১ দিন পূর্বে" else "1 Day in advance"),
                    Pair(2, if (isBangla) "২ দিন পূর্বে" else "2 Days in advance"),
                    Pair(3, if (isBangla) "৩ দিন পূর্বে (প্রস্তাবিত)" else "3 Days in advance (Recommended)"),
                    Pair(7, if (isBangla) "১ সপ্তাহ (৭ দিন) পূর্বে" else "1 Week (7 Days) in advance"),
                    Pair(14, if (isBangla) "২ সপ্তাহ (১৪ দিন) পূর্বে" else "2 Weeks (14 Days) in advance")
                )

                dayOptions.forEach { (days, label) ->
                    val isSelected = notifConfig.billReminderDaysInAdvance == days

                    OutlinedCard(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(
                            if (isSelected) 1.5.dp else 1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                notifPrefs.updateConfig { it.copy(billReminderDaysInAdvance = days) }
                                showBillNoticeSheet = false
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = label,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
