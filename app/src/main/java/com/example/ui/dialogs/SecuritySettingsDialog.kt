package com.example.ui.dialogs

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LanguageMode
import com.example.ui.theme.SolidPrimary
import com.example.util.BiometricAvailability
import com.example.util.BiometricHelper
import com.example.util.SecurityConfig

@Composable
fun SecuritySettingsDialog(
    securityConfig: SecurityConfig,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onSetAppLockEnabled: (Boolean) -> Unit,
    onSetPin: (String) -> Unit,
    onVerifyPin: (String) -> Boolean,
    onSetBiometricEnabled: (Boolean) -> Unit,
    onSetRequireAuthForGroupDeletion: (Boolean) -> Unit,
    onSetRequireAuthForMultiSelect: (Boolean) -> Unit = {},
    onSetRequireAuthForTrashClear: (Boolean) -> Unit = {},
    onSetRequireAuthForBackupRestore: (Boolean) -> Unit = {},
    onSetLockTimeoutSeconds: (Int) -> Unit,
    onSetSecurityRecovery: (String, String) -> Unit
) {
    val context = LocalContext.current
    val biometricAvailability = remember { BiometricHelper.checkBiometricAvailability(context) }
    val isBiometricCapable = biometricAvailability == BiometricAvailability.AVAILABLE || biometricAvailability == BiometricAvailability.NONE_ENROLLED

    var showPinSetupDialog by remember { mutableStateOf(false) }
    var showChangePinDialog by remember { mutableStateOf(false) }
    var showTimeoutDialog by remember { mutableStateOf(false) }
    var showRecoverySetupDialog by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SolidPrimary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = SolidPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "পাসওয়ার্ড ও ফিঙ্গারপ্রিন্ট সিকিউরিটি" else "Password & Fingerprint Security",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. App Lock Switch
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "অ্যাপ লক সক্ষম করুন" else "Enable App Lock",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "পিন বা ফিঙ্গারপ্রিন্ট দিয়ে অ্যাপ সুরক্ষিত রাখুন" else "Protect app with PIN, Password or Fingerprint",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        Switch(
                            checked = securityConfig.isAppLockEnabled,
                            onCheckedChange = { enabled ->
                                if (enabled && !securityConfig.hasPin) {
                                    showPinSetupDialog = true
                                } else {
                                    onSetAppLockEnabled(enabled)
                                }
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = SolidPrimary),
                            modifier = Modifier.testTag("switch_app_lock")
                        )
                    }
                }

                // 2. PIN / Password Management
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Pin,
                                    contentDescription = null,
                                    tint = SolidPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "পিন / পাসওয়ার্ড" else "PIN / Password",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = if (securityConfig.hasPin) {
                                            if (languageMode == LanguageMode.BANGLA) "পিন সেট করা আছে (সুরক্ষিত)" else "PIN is set (Active)"
                                        } else {
                                            if (languageMode == LanguageMode.BANGLA) "কোনো পিন সেট করা নেই" else "No PIN set"
                                        },
                                        fontSize = 12.sp,
                                        color = if (securityConfig.hasPin) SolidPrimary else MaterialTheme.colorScheme.outline
                                    )
                                }
                            }

                            TextButton(
                                onClick = {
                                    if (securityConfig.hasPin) {
                                        showChangePinDialog = true
                                    } else {
                                        showPinSetupDialog = true
                                    }
                                }
                            ) {
                                Text(
                                    text = if (securityConfig.hasPin) {
                                        if (languageMode == LanguageMode.BANGLA) "পরিবর্তন" else "Change"
                                    } else {
                                        if (languageMode == LanguageMode.BANGLA) "সেট করুন" else "Set Up"
                                    },
                                    color = SolidPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // 3. Biometric / Fingerprint Unlock
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = null,
                                tint = SolidPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                            Column {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "ফিঙ্গারপ্রিন্ট আনলক" else "Fingerprint Unlock",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = when (biometricAvailability) {
                                        BiometricAvailability.AVAILABLE -> if (languageMode == LanguageMode.BANGLA) "ডিভাইস বায়োমেট্রিক সমর্থিত" else "Biometric sensor ready"
                                        BiometricAvailability.NONE_ENROLLED -> if (languageMode == LanguageMode.BANGLA) "ডিভাইস সেটিংসে ফিঙ্গারপ্রিন্ট যোগ করুন" else "Enroll fingerprint in device settings"
                                        else -> if (languageMode == LanguageMode.BANGLA) "বায়োমেট্রিক সেন্সর অনুপলব্ধ" else "Biometric unavailable on device"
                                    },
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }

                        Switch(
                            checked = securityConfig.isBiometricEnabled,
                            onCheckedChange = { onSetBiometricEnabled(it) },
                            enabled = isBiometricCapable,
                            colors = SwitchDefaults.colors(checkedThumbColor = SolidPrimary),
                            modifier = Modifier.testTag("switch_biometric")
                        )
                    }
                }

                // 4. Group Deletion Protection Rule
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = SolidPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "গ্রুপ ডিলিটে পাসওয়ার্ড প্রয়োজন" else "Group Delete Auth",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA)
                                            "যেকোনো গ্রুপ (একাউন্ট/ক্যাটাগরি গ্রুপ) ডিলিট করতে পাসওয়ার্ড বা ফিঙ্গারপ্রিন্ট লাগবে।"
                                        else
                                            "Require PIN/Fingerprint when deleting account or category groups.",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }

                            Switch(
                                checked = securityConfig.requireAuthForGroupDeletion,
                                onCheckedChange = { onSetRequireAuthForGroupDeletion(it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = SolidPrimary),
                                modifier = Modifier.testTag("switch_group_deletion_auth")
                            )
                        }
                    }
                }

                // 5. Multi-Select Changes & Delete Protection Rule
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Checklist,
                                    contentDescription = null,
                                    tint = SolidPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "মাল্টি-সিলেক্ট পরিবর্তন বা ডিলিট সুরক্ষা" else "Multi-Select Changes & Delete Auth",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA)
                                            "একসাথে একাধিক লেনদেন পরিবর্তন বা ব্যাচ ডিলিটে পাসওয়ার্ড বা ফিঙ্গারপ্রিন্ট লাগবে।"
                                        else
                                            "Require PIN/Fingerprint when batch updating or batch deleting selected items.",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }

                            Switch(
                                checked = securityConfig.requireAuthForMultiSelect,
                                onCheckedChange = { onSetRequireAuthForMultiSelect(it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = SolidPrimary),
                                modifier = Modifier.testTag("switch_multi_select_auth")
                            )
                        }
                    }
                }

                // 6. Trash Empty & Permanent Delete Protection
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RestoreFromTrash,
                                    contentDescription = null,
                                    tint = SolidPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "ট্র্যাশ খালি ও স্থায়ী ডিলিট সুরক্ষা" else "Empty Trash & Permanent Delete Auth",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA)
                                            "ট্র্যাশ থেকে আইটেম স্থায়ীভাবে মুছে ফেলতে বা ট্র্যাশ খালি করতে অথেনটিকেশন লাগবে।"
                                        else
                                            "Require PIN/Fingerprint when emptying trash or permanently purging records.",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }

                            Switch(
                                checked = securityConfig.requireAuthForTrashClear,
                                onCheckedChange = { onSetRequireAuthForTrashClear(it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = SolidPrimary),
                                modifier = Modifier.testTag("switch_trash_clear_auth")
                            )
                        }
                    }
                }

                // 7. Backup Restore & Data Reset Protection
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Backup,
                                    contentDescription = null,
                                    tint = SolidPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "ব্যাকআপ রিস্টোর ও রিসেট সুরক্ষা" else "Backup Restore & Reset Auth",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA)
                                            "ব্যাকআপ ফাইল রিস্টোর বা ডেটা রিসেট করার সময় পাসওয়ার্ড বা ফিঙ্গারপ্রিন্ট লাগবে।"
                                        else
                                            "Require PIN/Fingerprint when restoring backups or wiping app database.",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }

                            Switch(
                                checked = securityConfig.requireAuthForBackupRestore,
                                onCheckedChange = { onSetRequireAuthForBackupRestore(it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = SolidPrimary),
                                modifier = Modifier.testTag("switch_backup_restore_auth")
                            )
                        }
                    }
                }

                // 5. Lock Timeout
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showTimeoutDialog = true }
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = SolidPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "স্বয়ংক্রিয় লক সময়" else "Auto-Lock Timeout",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = when (securityConfig.lockTimeoutSeconds) {
                                        0 -> if (languageMode == LanguageMode.BANGLA) "তাত্ক্ষণিক (অ্যাপ ছাড়লেই)" else "Immediately"
                                        60 -> if (languageMode == LanguageMode.BANGLA) "১ মিনিট পর" else "After 1 minute"
                                        300 -> if (languageMode == LanguageMode.BANGLA) "৫ মিনিট পর" else "After 5 minutes"
                                        900 -> if (languageMode == LanguageMode.BANGLA) "১৫ মিনিট পর" else "After 15 minutes"
                                        else -> "${securityConfig.lockTimeoutSeconds / 60} min"
                                    },
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }

                        TextButton(onClick = { showTimeoutDialog = true }) {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "পরিবর্তন" else "Change",
                                color = SolidPrimary,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                // 6. Security Recovery Question
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showRecoverySetupDialog = true }
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.QuestionMark,
                                contentDescription = null,
                                tint = SolidPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "রিকভারি সিকিউরিটি প্রশ্ন" else "Recovery Question",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = if (securityConfig.securityAnswerHash.isNotBlank()) {
                                        securityConfig.securityQuestion
                                    } else {
                                        if (languageMode == LanguageMode.BANGLA) "পিন ভুলে গেলে রিকভারির জন্য প্রশ্ন সেট করুন" else "Set up question for PIN recovery"
                                    },
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }

                        TextButton(onClick = { showRecoverySetupDialog = true }) {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "সেট আপ" else "Edit",
                                color = SolidPrimary,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = SolidPrimary)
            ) {
                Text(if (languageMode == LanguageMode.BANGLA) "সম্পন্ন" else "Done")
            }
        }
    )

    // Set Up PIN Dialog
    if (showPinSetupDialog) {
        var newPin by remember { mutableStateOf("") }
        var confirmPin by remember { mutableStateOf("") }
        var pinError by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showPinSetupDialog = false },
            title = {
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "নতুন পিন সেট করুন" else "Set Up App PIN",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "৪ থেকে ৮ সংখ্যার পিন বা পাসওয়ার্ড দিন:" else "Enter a 4 to 8 digit PIN or Password:",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newPin,
                        onValueChange = {
                            newPin = it.take(8)
                            pinError = ""
                        },
                        label = { Text(if (languageMode == LanguageMode.BANGLA) "নতুন পিন" else "New PIN") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = confirmPin,
                        onValueChange = {
                            confirmPin = it.take(8)
                            pinError = ""
                        },
                        label = { Text(if (languageMode == LanguageMode.BANGLA) "পিন নিশ্চিত করুন" else "Confirm PIN") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (pinError.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = pinError, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPin.length < 4) {
                            pinError = if (languageMode == LanguageMode.BANGLA) "পিন অন্তত ৪ সংখ্যার হতে হবে" else "PIN must be at least 4 digits"
                        } else if (newPin != confirmPin) {
                            pinError = if (languageMode == LanguageMode.BANGLA) "উভয় পিন এক হতে হবে" else "PINs do not match"
                        } else {
                            onSetPin(newPin)
                            onSetAppLockEnabled(true)
                            showPinSetupDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SolidPrimary)
                ) {
                    Text(if (languageMode == LanguageMode.BANGLA) "সংরক্ষণ" else "Save PIN")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showPinSetupDialog = false }) {
                    Text(if (languageMode == LanguageMode.BANGLA) "বাতিল" else "Cancel")
                }
            }
        )
    }

    // Change PIN Dialog
    if (showChangePinDialog) {
        var currentPin by remember { mutableStateOf("") }
        var newPin by remember { mutableStateOf("") }
        var confirmPin by remember { mutableStateOf("") }
        var errorMsg by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showChangePinDialog = false },
            title = {
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "পিন পরিবর্তন করুন" else "Change PIN",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = currentPin,
                        onValueChange = {
                            currentPin = it.take(8)
                            errorMsg = ""
                        },
                        label = { Text(if (languageMode == LanguageMode.BANGLA) "বর্তমান পিন" else "Current PIN") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newPin,
                        onValueChange = {
                            newPin = it.take(8)
                            errorMsg = ""
                        },
                        label = { Text(if (languageMode == LanguageMode.BANGLA) "নতুন পিন" else "New PIN") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = confirmPin,
                        onValueChange = {
                            confirmPin = it.take(8)
                            errorMsg = ""
                        },
                        label = { Text(if (languageMode == LanguageMode.BANGLA) "নতুন পিন নিশ্চিত করুন" else "Confirm New PIN") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (errorMsg.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = errorMsg, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (!onVerifyPin(currentPin)) {
                            errorMsg = if (languageMode == LanguageMode.BANGLA) "বর্তমান পিন ভুল!" else "Current PIN is incorrect!"
                        } else if (newPin.length < 4) {
                            errorMsg = if (languageMode == LanguageMode.BANGLA) "নতুন পিন অন্তত ৪ সংখ্যার হতে হবে" else "New PIN must be at least 4 digits"
                        } else if (newPin != confirmPin) {
                            errorMsg = if (languageMode == LanguageMode.BANGLA) "নতুন পিন দুটি মিলছে না" else "New PINs do not match"
                        } else {
                            onSetPin(newPin)
                            showChangePinDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SolidPrimary)
                ) {
                    Text(if (languageMode == LanguageMode.BANGLA) "আপডেট করুন" else "Update PIN")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showChangePinDialog = false }) {
                    Text(if (languageMode == LanguageMode.BANGLA) "বাতিল" else "Cancel")
                }
            }
        )
    }

    // Timeout Selector Dialog
    if (showTimeoutDialog) {
        val timeoutOptions = listOf(
            0 to (if (languageMode == LanguageMode.BANGLA) "তাত্ক্ষণিক (Immediately)" else "Immediately"),
            60 to (if (languageMode == LanguageMode.BANGLA) "১ মিনিট পর (1 Minute)" else "After 1 Minute"),
            300 to (if (languageMode == LanguageMode.BANGLA) "৫ মিনিট পর (5 Minutes)" else "After 5 Minutes"),
            900 to (if (languageMode == LanguageMode.BANGLA) "১৫ মিনিট পর (15 Minutes)" else "After 15 Minutes")
        )

        AlertDialog(
            onDismissRequest = { showTimeoutDialog = false },
            title = {
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "লক টাইমআউট নির্বাচন" else "Select Auto-Lock Timeout",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    timeoutOptions.forEach { (seconds, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSetLockTimeoutSeconds(seconds)
                                    showTimeoutDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = securityConfig.lockTimeoutSeconds == seconds,
                                onClick = {
                                    onSetLockTimeoutSeconds(seconds)
                                    showTimeoutDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = label, fontSize = 14.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTimeoutDialog = false }) {
                    Text(if (languageMode == LanguageMode.BANGLA) "বন্ধ" else "Close")
                }
            }
        )
    }

    // Recovery Question Setup Dialog
    if (showRecoverySetupDialog) {
        var question by remember { mutableStateOf(securityConfig.securityQuestion) }
        var answer by remember { mutableStateOf("") }
        var error by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showRecoverySetupDialog = false },
            title = {
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "সিকিউরিটি রিকভারি সেট করুন" else "Setup Security Recovery",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "পিন ভুলে গেলে এই প্রশ্নের উত্তর দিয়ে রিকভার করা যাবে:" else "If you forget your PIN, this answer will allow you to reset it:",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = question,
                        onValueChange = { question = it },
                        label = { Text(if (languageMode == LanguageMode.BANGLA) "সিকিউরিটি প্রশ্ন" else "Security Question") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = answer,
                        onValueChange = {
                            answer = it
                            error = ""
                        },
                        label = { Text(if (languageMode == LanguageMode.BANGLA) "গোপন উত্তর" else "Secret Answer") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (error.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = error, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (question.isBlank() || answer.isBlank()) {
                            error = if (languageMode == LanguageMode.BANGLA) "প্রশ্ন এবং উত্তর উভয়ই পূরণ করুন" else "Please enter both question and answer"
                        } else {
                            onSetSecurityRecovery(question, answer)
                            showRecoverySetupDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SolidPrimary)
                ) {
                    Text(if (languageMode == LanguageMode.BANGLA) "সংরক্ষণ" else "Save Recovery")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showRecoverySetupDialog = false }) {
                    Text(if (languageMode == LanguageMode.BANGLA) "বাতিল" else "Cancel")
                }
            }
        )
    }
}
