package com.example.ui.dialogs

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.LanguageMode
import com.example.ui.theme.SolidPrimary
import com.example.util.NotificationConfig
import com.example.util.NotificationHelper
import com.example.util.NotificationPreferences

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PhoneNotificationDialog(
    languageMode: LanguageMode,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val notifPrefs = remember { NotificationPreferences.getInstance(context) }
    val notifConfig by notifPrefs.config.collectAsStateWithLifecycle()
    val isBangla = languageMode == LanguageMode.BANGLA
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
            Toast.makeText(
                context,
                if (isBangla) "নোটিফিকেশন অনুমতি প্রদান করা হয়েছে" else "Notification permission granted",
                Toast.LENGTH_SHORT
            ).show()
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

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxHeight(0.92f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(SolidPrimary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = SolidPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Text(
                            text = if (isBangla) "ফোন নোটিফিকেশন" else "Phone Notifications",
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isBangla) "দৈনিক হিসাব রিমাইন্ডার ও বিল সতর্কতা" else "Daily reminders & recurring bill alerts",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Permission Status Banner (if not granted on Android 13+)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column {
                                    Text(
                                        text = if (isBangla) "নোটিফিকেশন অনুমতি প্রয়োজন" else "Permission Required",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = if (isBangla) "রিমাইন্ডার পেতে সিস্টেম পারমিশন দিন" else "Allow notification permission to receive alerts",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text(if (isBangla) "অনুমতি দিন" else "Allow", fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Section 1: Daily Expense Reminder ("Notify to remember")
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (notifConfig.isDailyReminderEnabled)
                            SolidPrimary.copy(alpha = 0.08f)
                        else
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = if (notifConfig.isDailyReminderEnabled) SolidPrimary else MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(22.dp)
                                )
                                Column {
                                    Text(
                                        text = if (isBangla) "দৈনিক হিসাব রিমাইন্ডার" else "Daily Expense Reminder",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = if (isBangla) "প্রতিদিন আয়-ব্যয় রেকর্ড করতে নোটিফিকেশন" else "Notify to remember recording daily transactions",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }

                            Switch(
                                checked = notifConfig.isDailyReminderEnabled,
                                onCheckedChange = { enabled ->
                                    if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                    notifPrefs.updateConfig { it.copy(isDailyReminderEnabled = enabled) }
                                    if (enabled) {
                                        NotificationHelper.scheduleDailyReminder(
                                            context,
                                            notifConfig.dailyReminderHour,
                                            notifConfig.dailyReminderMinute
                                        )
                                    } else {
                                        NotificationHelper.cancelDailyReminder(context)
                                    }
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = SolidPrimary)
                            )
                        }

                        if (notifConfig.isDailyReminderEnabled) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                            Text(
                                text = if (isBangla) "রিমাইন্ডারের সময় নির্বাচন করুন" else "Reminder Time",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            val timePresets = listOf(
                                (18 to 0) to "6:00 PM",
                                (20 to 0) to "8:00 PM",
                                (21 to 0) to "9:00 PM",
                                (22 to 0) to "10:00 PM",
                                (23 to 0) to "11:00 PM"
                            )

                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                timePresets.forEach { (time, label) ->
                                    val (h, m) = time
                                    val isSelected = notifConfig.dailyReminderHour == h && notifConfig.dailyReminderMinute == m
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            notifPrefs.updateConfig {
                                                it.copy(dailyReminderHour = h, dailyReminderMinute = m)
                                            }
                                            NotificationHelper.scheduleDailyReminder(context, h, m)
                                        },
                                        label = { Text(label, fontSize = 12.sp) },
                                        leadingIcon = if (isSelected) {
                                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                        } else null,
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = SolidPrimary.copy(alpha = 0.15f),
                                            selectedLabelColor = SolidPrimary
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = notifConfig.customDailyMessage,
                                onValueChange = { msg ->
                                    notifPrefs.updateConfig { it.copy(customDailyMessage = msg) }
                                },
                                label = { Text(if (isBangla) "রিমাইন্ডার মেসেজ" else "Reminder Prompt Message", fontSize = 12.sp) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Section 2: Recurring Bills Reminder ("Notify for recurring bills")
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (notifConfig.isBillReminderEnabled)
                            SolidPrimary.copy(alpha = 0.08f)
                        else
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ReceiptLong,
                                    contentDescription = null,
                                    tint = if (notifConfig.isBillReminderEnabled) SolidPrimary else MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(22.dp)
                                )
                                Column {
                                    Text(
                                        text = if (isBangla) "পৌনঃপুনিক বিল সতর্কতা" else "Recurring Bills Due Alerts",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = if (isBangla) "বিল পরিশোধের তারিখের আগে নোটিফিকেশন পান" else "Notify in advance before recurring bill due dates",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }

                            Switch(
                                checked = notifConfig.isBillReminderEnabled,
                                onCheckedChange = { enabled ->
                                    if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                    notifPrefs.updateConfig { it.copy(isBillReminderEnabled = enabled) }
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = SolidPrimary)
                            )
                        }

                        if (notifConfig.isBillReminderEnabled) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                            Text(
                                text = if (isBangla) "কতদিন আগে সতর্কতা চান?" else "Notify How Many Days in Advance?",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            val daysPresets = listOf(
                                0 to (if (isBangla) "বিল পরিশোধের দিনে" else "On Due Day"),
                                1 to (if (isBangla) "১ দিন আগে" else "1 Day Before"),
                                2 to (if (isBangla) "২ দিন আগে" else "2 Days Before"),
                                3 to (if (isBangla) "৩ দিন আগে" else "3 Days Before")
                            )

                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                daysPresets.forEach { (days, label) ->
                                    val isSelected = notifConfig.billReminderDaysInAdvance == days
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            notifPrefs.updateConfig { it.copy(billReminderDaysInAdvance = days) }
                                        },
                                        label = { Text(label, fontSize = 12.sp) },
                                        leadingIcon = if (isSelected) {
                                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                        } else null,
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = SolidPrimary.copy(alpha = 0.15f),
                                            selectedLabelColor = SolidPrimary
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // Section 3: Test Notification Button
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isBangla) "নোটিফিকেশন পরীক্ষা করুন" else "Test Notification",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (isBangla) "তাত্ক্ষণিক একটি পরীক্ষামূলক নোটিফিকেশন পাঠান" else "Send an immediate test alert to verify delivery",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }

                        Button(
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    NotificationHelper.sendTestNotification(context, isBangla)
                                    Toast.makeText(
                                        context,
                                        if (isBangla) "টেস্ট নোটিফিকেশন পাঠানো হয়েছে" else "Test notification sent!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SolidPrimary)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isBangla) "টেস্ট করুন" else "Send Test", fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}
