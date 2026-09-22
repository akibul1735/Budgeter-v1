package com.example.ui.components

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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.LanguageMode

enum class AppPermissionType {
    NOTIFICATION,
    CAMERA,
    PHOTOS_MEDIA,
    STORAGE
}

@Composable
fun PermissionRationaleDialog(
    permissionType: AppPermissionType,
    languageMode: LanguageMode,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val isBangla = languageMode == LanguageMode.BANGLA

    val (icon, title, explanation, featurePoints) = when (permissionType) {
        AppPermissionType.NOTIFICATION -> Quadruple(
            Icons.Default.NotificationsActive,
            if (isBangla) "নোটিফিকেশন অনুমতি প্রয়োজন" else "Notification Permission Needed",
            if (isBangla)
                "বাজেটার আপনাকে সঠিক সময়ে গুরুত্বপূর্ণ আর্থিক তথ্য মনে করিয়ে দিতে ডিভাইসের নোটিফিকেশন ব্যবহার করে।"
            else
                "Budgeter uses device notifications to keep you informed about your financial health, budget limits, and deadlines.",
            listOf(
                if (isBangla) "ধার্য তারিখের আগে পুনরাবৃত্ত বিলের (Recurring Bills) সতর্কতা" else "Advance due date alerts for recurring bills",
                if (isBangla) "দৈনিক বাজেট ও খরচ এন্ট্রির স্মরণিকা" else "Daily reminders to record daily transactions",
                if (isBangla) "নির্ধারিত ব্যাকআপ সফলতার স্ট্যাটাস" else "Scheduled backup success notifications"
            )
        )
        AppPermissionType.CAMERA -> Quadruple(
            Icons.Default.CameraAlt,
            if (isBangla) "ক্যামেরা ব্যবহারের অনুমতি" else "Camera Permission Needed",
            if (isBangla)
                "খরচের রসিদ, ইনভয়েস বা ভাউচারের সরাসরি ছবি তুলে ট্রানজ্যাকশনে সংযুক্ত করার জন্য ক্যামেরা অনুমতি প্রয়োজন।"
            else
                "Budgeter needs camera access so you can snap and attach physical receipts, invoices, or vouchers directly to transactions.",
            listOf(
                if (isBangla) "ইনস্ট্যান্ট রসিদ ও বিল স্ক্যান বা ফটো তোলা" else "Snap instant photos of paper receipts & bills",
                if (isBangla) "ট্রানজ্যাকশনের সাথে ভাউচার সংযুক্ত রাখা" else "Keep payment proof securely attached with records",
                if (isBangla) "কাস্টম আইকন বা ক্যাটাগরির ছবি ক্যাপচার" else "Capture photos for custom icons and entities"
            )
        )
        AppPermissionType.PHOTOS_MEDIA -> Quadruple(
            Icons.Default.PhotoLibrary,
            if (isBangla) "ফটো ও মিডিয়া অ্যাক্সেস" else "Photos & Media Access",
            if (isBangla)
                "গ্যালারি থেকে রসিদের ছবি বা কাস্টম ক্যাটাগরি ও অ্যাকাউন্টের আইকন নির্বাচন করার জন্য ফটো ও মিডিয়া অনুমতি প্রয়োজন।"
            else
                "Budgeter needs photos and media access so you can pick receipt photos, invoices, and custom category icons from your library.",
            listOf(
                if (isBangla) "ডিভাইস থেকে পূর্বের রসিদের ছবি নির্বাচন" else "Select existing receipt images and payment slips",
                if (isBangla) "কাস্টম ক্যাটাগরি ও অ্যাকাউন্ট আইকন স্থাপন" else "Import custom artwork for categories and accounts",
                if (isBangla) "ছবি ক্রপ এবং রোটেট টুল ব্যবহারে সহায়তা" else "Full interactive image cropping and rotation"
            )
        )
        AppPermissionType.STORAGE -> Quadruple(
            Icons.Default.Folder,
            if (isBangla) "স্টোরেজ ব্যবহারের অনুমতি" else "Storage Access Needed",
            if (isBangla)
                "আপনার স্থানীয় ব্যাকআপ ফাইল ডিভাইসের স্টোরেজে সংরক্ষণ এবং পূর্বের ব্যাকআপ থেকে ডেটা রিস্টোর করার জন্য স্টোরেজ অ্যাক্সেস প্রয়োজন।"
            else
                "Budgeter needs storage access to safely export database backup files locally and restore previous financial backups.",
            listOf(
                if (isBangla) "ডিভাইসের মেমরিতে অফলাইন ব্যাকআপ ফাইল তৈরি" else "Create offline encrypted database backups locally",
                if (isBangla) "পূর্বে সংরক্ষিত ব্যাকআপ থেকে ডেটা পুনরুদ্ধার" else "Restore transaction records from device storage",
                if (isBangla) "রিপোর্ট ও স্টেটমেন্ট ফাইল সংরক্ষণ" else "Save reports and financial statements"
            )
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Circular icon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = explanation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Feature points bullet list
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        featurePoints.forEach { point ->
                            Row(
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "•",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = point,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (isBangla) "এখন না" else "Not Now")
                    }

                    Button(
                        onClick = {
                            onDismiss()
                            onConfirm()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (isBangla) "অনুমতি দিন" else "Allow")
                    }
                }
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
