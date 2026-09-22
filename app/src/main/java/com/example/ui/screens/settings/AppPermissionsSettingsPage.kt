package com.example.ui.screens.settings

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.data.model.LanguageMode
import com.example.ui.components.AppTabHeader
import com.example.util.NotificationHelper
import com.example.util.PermissionHelper
import com.example.util.PermissionPreferences

@Composable
fun AppPermissionsSettingsPage(
    languageMode: LanguageMode,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val isBangla = languageMode == LanguageMode.BANGLA
    val permissionPrefs = remember { PermissionPreferences.getInstance(context) }

    // Refresh trigger key when app resumes from background / settings
    var refreshKey by remember { mutableIntStateOf(0) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshKey++
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Read live permission status
    val isNotificationsGranted = remember(refreshKey) {
        PermissionHelper.areNotificationsGranted(context)
    }
    val isCameraGranted = remember(refreshKey) {
        PermissionHelper.isCameraGranted(context)
    }
    val isStorageGranted = remember(refreshKey) {
        PermissionHelper.isStorageGranted(context)
    }
    val canScheduleAlarms = remember(refreshKey) {
        PermissionHelper.canScheduleExactAlarms(context)
    }

    // Permission Launchers
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            NotificationHelper.createNotificationChannels(context)
        }
        permissionPrefs.markPromptCompleted()
        refreshKey++
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        permissionPrefs.markPromptCompleted()
        refreshKey++
    }

    val storagePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        permissionPrefs.markPromptCompleted()
        refreshKey++
    }

    val requestAllMissingLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (results[Manifest.permission.POST_NOTIFICATIONS] == true) {
            NotificationHelper.createNotificationChannels(context)
        }
        permissionPrefs.markPromptCompleted()
        refreshKey++
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AppTabHeader(
            title = if (isBangla) "অ্যাপের অনুমতিসমূহ" else "App Permissions",
            tabIcon = Icons.Default.Security,
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Overview Info Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isBangla) "অনুমতি ও গোপনীয়তা" else "Permissions & Privacy",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.5.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isBangla)
                                "বাজেটারের সকল ফিচার সক্রিয় রাখতে প্রয়োজনীয় সিস্টেম পারমিশন এখানে পরিচালনা করুন।"
                            else
                                "Manage Android system permissions required for Budgeter's features to function reliably.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Quick Status & Action
            val missingPermissions = PermissionHelper.getMissingRuntimePermissions(context)
            if (missingPermissions.isNotEmpty()) {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isBangla) "কিছু অনুমতি বাকি আছে" else "Permissions Need Attention",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.5.sp,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isBangla)
                                "সব ফিচার নির্বিঘ্নে ব্যবহারের জন্য অবশিষ্ট অনুমতিগুলো এক ক্লিকে সক্রিয় করতে পারেন।"
                            else
                                "Grant all missing permissions in one tap to unlock full feature functionality.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                requestAllMissingLauncher.launch(missingPermissions.toTypedArray())
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 10.dp)
                        ) {
                            Text(
                                text = if (isBangla) "বাকি সব অনুমতি দিন" else "Grant All Required Permissions",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            Text(
                text = if (isBangla) "ফিচারভিত্তিক পারমিশন স্ট্যাটাস" else "Feature Permissions",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )

            // 1. Notifications Item
            PermissionItemCard(
                icon = Icons.Default.NotificationsActive,
                title = if (isBangla) "নোটিফিকেশন (Notifications)" else "Notifications",
                description = if (isBangla)
                    "দৈনিক খরচ লেখার অ্যালার্ট, আসন্ন পুনরাবৃত্ত বিল ও বাজেট লিমিটের সতর্কতা পেতে।"
                else
                    "Alerts for daily transaction reminders, upcoming recurring bills, and budget limits.",
                isGranted = isNotificationsGranted,
                isBangla = isBangla,
                onRequest = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        PermissionHelper.openAppSystemSettings(context)
                    }
                },
                onManageSettings = {
                    PermissionHelper.openAppSystemSettings(context)
                }
            )

            // 2. Camera Item
            PermissionItemCard(
                icon = Icons.Default.CameraAlt,
                title = if (isBangla) "ক্যামেরা (Camera)" else "Camera",
                description = if (isBangla)
                    "কাগজের রসিদ, ইনভয়েস বা ভাউচারের ছবি তুলে ট্রানজ্যাকশনে যুক্ত করার জন্য।"
                else
                    "Capture physical receipts, invoices, and vouchers to attach directly to transactions.",
                isGranted = isCameraGranted,
                isBangla = isBangla,
                onRequest = {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                },
                onManageSettings = {
                    PermissionHelper.openAppSystemSettings(context)
                }
            )

            // 3. Exact Alarms Item (Android 12+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PermissionItemCard(
                    icon = Icons.Default.Alarm,
                    title = if (isBangla) "সঠিক সময়ের অ্যালার্ম (Exact Alarms)" else "Exact Alarms & Reminders",
                    description = if (isBangla)
                        "বিল পরিশোধের দিন নির্দিষ্ট সময়ে নোটিফিকেশন পৌঁছে দেওয়ার জন্য।"
                    else
                        "Enables exact on-time delivery for bill reminders and daily logging alerts.",
                    isGranted = canScheduleAlarms,
                    isBangla = isBangla,
                    onRequest = {
                        PermissionHelper.openExactAlarmSettings(context)
                    },
                    onManageSettings = {
                        PermissionHelper.openExactAlarmSettings(context)
                    }
                )
            }

            // 4. Local Storage Item (Android <= 32)
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
                PermissionItemCard(
                    icon = Icons.Default.Folder,
                    title = if (isBangla) "লোকাল স্টোরেজ (Storage)" else "Local Storage",
                    description = if (isBangla)
                        "ডিভাইসে অফলাইন এনক্রিপ্টেড ডাটাবেস ব্যাকআপ তৈরি ও রিস্টোর করার জন্য।"
                    else
                        "Export offline encrypted backups and restore financial data from device storage.",
                    isGranted = isStorageGranted,
                    isBangla = isBangla,
                    onRequest = {
                        storagePermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                            )
                        )
                    },
                    onManageSettings = {
                        PermissionHelper.openAppSystemSettings(context)
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(6.dp))

            // System App Info Button
            OutlinedButton(
                onClick = {
                    PermissionHelper.openAppSystemSettings(context)
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                contentPadding = PaddingValues(horizontal = 14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.OpenInNew,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isBangla) "অ্যান্ড্রয়েড সিস্টেম সেটিংস খুলুন" else "Open Android App Settings",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.5.sp
                )
            }

            // Recheck status button
            OutlinedButton(
                onClick = {
                    refreshKey++
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                contentPadding = PaddingValues(horizontal = 14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isBangla) "অনুমতি স্ট্যাটাস রিফ্রেশ করুন" else "Refresh Permission Status",
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PermissionItemCard(
    icon: ImageVector,
    title: String,
    description: String,
    isGranted: Boolean,
    isBangla: Boolean,
    onRequest: () -> Unit,
    onManageSettings: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            if (isGranted)
                                Color(0xFF10B981).copy(alpha = 0.12f)
                            else
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isGranted) Color(0xFF059669) else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        // Status Badge
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isGranted)
                                Color(0xFF10B981).copy(alpha = 0.15f)
                            else
                                Color(0xFFF59E0B).copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (isGranted) Color(0xFF059669) else Color(0xFFD97706),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isGranted) {
                                        if (isBangla) "অনুমোদিত" else "Granted"
                                    } else {
                                        if (isBangla) "অনুমোদিত নয়" else "Disabled"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isGranted) Color(0xFF059669) else Color(0xFFD97706)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = description,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isGranted) {
                    Button(
                        onClick = onRequest,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (isBangla) "অনুমতি দিন" else "Allow",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                OutlinedButton(
                    onClick = onManageSettings,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (isBangla) "ম্যানেজ করুন" else "Settings",
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
