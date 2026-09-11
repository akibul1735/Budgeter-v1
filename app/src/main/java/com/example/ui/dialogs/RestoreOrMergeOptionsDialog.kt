package com.example.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Merge
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LanguageMode
import com.example.ui.viewmodel.DetectedBackupInfo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RestoreOrMergeOptionsDialog(
    backupTitle: String,
    backupSubtitle: String? = null,
    backupInfo: DetectedBackupInfo? = null,
    languageMode: LanguageMode,
    onMerge: () -> Unit,
    onRestoreReplace: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedMode by remember { mutableStateOf("MERGE") } // "MERGE" or "REPLACE"
    val isBangla = languageMode == LanguageMode.BANGLA

    val formattedDate = remember(backupInfo) {
        if (backupInfo != null && backupInfo.timestamp > 0L) {
            try {
                SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(backupInfo.timestamp))
            } catch (_: Exception) {
                backupSubtitle ?: ""
            }
        } else {
            backupSubtitle ?: ""
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("restore_or_merge_dialog"),
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.CloudDone,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = if (isBangla) "ব্যাকআপ রিস্টোর বা মার্জ করুন" else "Restore or Merge Backup",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isBangla) "ডাটা নিরাপদভাবে সংযুক্ত করুন" else "Safely apply previous data",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Backup snapshot summary card
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = backupTitle,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (formattedDate.isNotBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = formattedDate,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (backupInfo?.sourceProvider != null) {
                            Text(
                                text = "${if (isBangla) "উৎস:" else "Source:"} ${backupInfo.sourceProvider}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }

                        // Installation ID and Device badges
                        if (!backupInfo?.installationId.isNullOrBlank() || !backupInfo?.deviceName.isNullOrBlank()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                backupInfo?.deviceName?.takeIf { it.isNotBlank() }?.let { dev ->
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Devices, contentDescription = null, modifier = Modifier.size(11.dp), tint = MaterialTheme.colorScheme.primary)
                                            Spacer(Modifier.width(3.dp))
                                            Text(dev, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }

                                backupInfo?.installationId?.takeIf { it.isNotBlank() }?.let { id ->
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Fingerprint, contentDescription = null, modifier = Modifier.size(11.dp), tint = MaterialTheme.colorScheme.secondary)
                                            Spacer(Modifier.width(3.dp))
                                            Text("ID: ${id.take(8)}", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.secondary)
                                        }
                                    }
                                }
                            }
                        }

                        // Counts summary if available
                        if (backupInfo?.accountsCount != null || backupInfo?.transactionsCount != null) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                backupInfo.accountsCount?.let { accs ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.outline)
                                        Spacer(Modifier.width(4.dp))
                                        Text("$accs ${if (isBangla) "একাউন্ট" else "Accounts"}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                    }
                                }
                                backupInfo.transactionsCount?.let { txs ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.outline)
                                        Spacer(Modifier.width(4.dp))
                                        Text("$txs ${if (isBangla) "লেনদেন" else "Transactions"}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                    }
                                }
                            }
                        }
                    }
                }

                Text(
                    text = if (isBangla) "কিভাবে পুনরুদ্ধার করতে চান নির্বাচন করুন:" else "Select restore method:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Option 1: MERGE (Smart Merge)
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (selectedMode == "MERGE") MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        if (selectedMode == "MERGE") 1.5.dp else 1.dp,
                        if (selectedMode == "MERGE") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedMode = "MERGE" }
                        .testTag("option_merge_backup")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        RadioButton(
                            selected = selectedMode == "MERGE",
                            onClick = { selectedMode = "MERGE" }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isBangla) "স্মার্ট মার্জ (প্রস্তাবিত)" else "Smart Merge (Recommended)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedMode == "MERGE") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = if (isBangla)
                                    "বর্তমান কোনো ডাটা মুছে যাবে না। ব্যাকআপের হিসাব ও লেনদেনগুলো বিদ্যমান ডেটার সাথে সমন্বয় করা হবে।"
                                else
                                    "Keep existing local records and merge backup entries. Missing categories, accounts, and transactions will be safely combined.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Option 2: REPLACE (Full Restore)
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (selectedMode == "REPLACE") MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        if (selectedMode == "REPLACE") 1.5.dp else 1.dp,
                        if (selectedMode == "REPLACE") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedMode = "REPLACE" }
                        .testTag("option_replace_backup")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        RadioButton(
                            selected = selectedMode == "REPLACE",
                            onClick = { selectedMode = "REPLACE" }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isBangla) "সম্পূর্ণ প্রতিস্থাপন (Replace All)" else "Replace All (Full Restore)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedMode == "REPLACE") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = if (isBangla)
                                    "বর্তমান ডেটাবেস সম্পূর্ণ মুছে ফেলে শুধুমাত্র এই ব্যাকআপের ডেটা স্থাপন করবে।"
                                else
                                    "Completely overwrite the current database with this backup snapshot. Any un-backed-up local changes will be replaced.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedMode == "MERGE") {
                        onMerge()
                    } else {
                        onRestoreReplace()
                    }
                },
                shape = RoundedCornerShape(10.dp),
                colors = if (selectedMode == "MERGE") {
                    ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                } else {
                    ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                },
                modifier = Modifier.testTag("dialog_confirm_restore_button")
            ) {
                Icon(
                    imageVector = if (selectedMode == "MERGE") Icons.Default.Merge else Icons.Default.Restore,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (selectedMode == "MERGE") {
                        if (isBangla) "মার্জ করুন" else "Merge Backup"
                    } else {
                        if (isBangla) "প্রতিস্থাপন করুন" else "Replace All"
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("dialog_dismiss_restore_button")
            ) {
                Text(if (isBangla) "বাতিল" else "Cancel")
            }
        }
    )
}
