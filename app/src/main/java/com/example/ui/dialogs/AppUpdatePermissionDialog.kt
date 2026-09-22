package com.example.ui.dialogs

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.LanguageMode
import com.example.util.NotificationHelper
import com.example.util.PermissionHelper
import com.example.util.PermissionPreferences

@Composable
fun AppUpdatePermissionDialog(
    languageMode: LanguageMode,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isBangla = languageMode == LanguageMode.BANGLA
    val permissionPrefs = PermissionPreferences.getInstance(context)

    val missingPermissions = PermissionHelper.getMissingRuntimePermissions(context)

    val multiplePermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        // Mark prompt completed so we remember the user's action
        permissionPrefs.markPromptCompleted()

        // If notifications were granted, initialize channels & schedule reminders
        if (results[android.Manifest.permission.POST_NOTIFICATIONS] == true ||
            PermissionHelper.areNotificationsGranted(context)
        ) {
            NotificationHelper.createNotificationChannels(context)
        }

        onDismiss()
    }

    Dialog(
        onDismissRequest = {
            permissionPrefs.markPromptDismissed()
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Icon
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Title
                Text(
                    text = if (isBangla) "অ্যাপের ফিচার ব্যবহারে অনুমতি" else "Enable App Permissions",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Subtitle
                Text(
                    text = if (isBangla)
                        "বাজেটারের সকল সুবিধা নির্বিঘ্নে ব্যবহারের জন্য প্রয়োজনীয় অনুমতি সক্রিয় করুন।"
                    else
                        "Grant the required permissions to ensure all Budgeter features function seamlessly on your device.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Feature Permission Explanations
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    !PermissionHelper.areNotificationsGranted(context)
                ) {
                    PermissionFeatureCard(
                        icon = Icons.Default.NotificationsActive,
                        title = if (isBangla) "নোটিফিকেশন ও অ্যালার্ট" else "Notifications & Alerts",
                        description = if (isBangla)
                            "দৈনিক খরচ লেখার স্মরণিকা, বিলের তারিখ ও বাজেট লিমিটের সতর্কতা পেতে।"
                        else
                            "Timely reminders to record expenses, due recurring bills, and budget limit alerts."
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                if (!PermissionHelper.isCameraGranted(context)) {
                    PermissionFeatureCard(
                        icon = Icons.Default.CameraAlt,
                        title = if (isBangla) "ক্যামেরা (রসিদ স্ক্যান)" else "Camera (Receipts)",
                        description = if (isBangla)
                            "ইনভয়েস বা ভাউচারের সরাসরি ছবি তুলে ট্রানজ্যাকশনে সংযুক্ত করতে।"
                        else
                            "Snap and attach physical invoices, receipts, and vouchers directly to transactions."
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2 &&
                    !PermissionHelper.isStorageGranted(context)
                ) {
                    PermissionFeatureCard(
                        icon = Icons.Default.Folder,
                        title = if (isBangla) "লোকাল স্টোরেজ ব্যাকআপ" else "Local Storage Backup",
                        description = if (isBangla)
                            "ডিভাইসে অফলাইন ডাটাবেস ব্যাকআপ সংরক্ষণ ও রিস্টোর করতে।"
                        else
                            "Save offline database backups and restore records from local storage."
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Primary Action Button: Grant Permissions
                Button(
                    onClick = {
                        if (missingPermissions.isNotEmpty()) {
                            multiplePermissionsLauncher.launch(missingPermissions.toTypedArray())
                        } else {
                            permissionPrefs.markPromptCompleted()
                            onDismiss()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = if (isBangla) "অনুমতি দিন (Allow)" else "Grant Permissions",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Dismiss Button: Later
                TextButton(
                    onClick = {
                        permissionPrefs.markPromptDismissed()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isBangla) "পরে মনে করিয়ে দিন" else "Not Now",
                        fontSize = 13.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionFeatureCard(
    icon: ImageVector,
    title: String,
    description: String
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.5.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 15.sp
                )
            }
        }
    }
}
