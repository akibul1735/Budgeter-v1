package com.example.ui.screens.settings

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LanguageMode
import com.example.ui.components.AppTabHeader
import com.example.ui.theme.SolidPrimary
import com.example.util.SecurityConfig

@Composable
fun SecuritySettingsPage(
    securityConfig: SecurityConfig,
    languageMode: LanguageMode,
    onSetAppLockEnabled: (Boolean) -> Unit,
    onSetPin: (String) -> Unit,
    onVerifyPin: (String) -> Boolean,
    onSetBiometricEnabled: (Boolean) -> Unit,
    onSetRequireAuthForGroupDeletion: (Boolean) -> Unit,
    onSetRequireAuthForMultiSelect: (Boolean) -> Unit,
    onSetRequireAuthForTrashClear: (Boolean) -> Unit,
    onSetRequireAuthForBackupRestore: (Boolean) -> Unit,
    onSetLockTimeoutSeconds: (Int) -> Unit,
    onSetSecurityRecovery: (String, String) -> Unit,
    onVerifySecurityAnswer: (String) -> Boolean,
    onBack: () -> Unit
) {
    val isBangla = languageMode == LanguageMode.BANGLA

    var pinInput by remember { mutableStateOf("") }
    var pinConfirmInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }
    var showPinSetupSection by remember { mutableStateOf(!securityConfig.hasPin) }

    var recoveryQuestion by remember(securityConfig) { mutableStateOf(securityConfig.securityQuestion) }
    var recoveryAnswer by remember { mutableStateOf("") }
    var recoverySavedFeedback by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("security_settings_page")
    ) {
        AppTabHeader(
            title = if (isBangla) "পাসওয়ার্ড ও সিকিউরিটি" else "Password & Security",
            tabIcon = Icons.Default.Fingerprint,
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

            // Master App Lock Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (securityConfig.isAppLockEnabled)
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                    else
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (securityConfig.isAppLockEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = if (isBangla) "অ্যাপ সুরক্ষা লক" else "Master App Lock",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (securityConfig.isAppLockEnabled)
                                    if (isBangla) "অ্যাপটি পিন/ফিঙ্গারপ্রিন্ট দ্বারা সুরক্ষিত" else "Protected with PIN & Biometrics"
                                else
                                    if (isBangla) "সুরক্ষা বন্ধ রয়েছে" else "Protection disabled",
                                fontSize = 11.sp,
                                color = if (securityConfig.isAppLockEnabled) SolidPrimary else MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    Switch(
                        checked = securityConfig.isAppLockEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled && !securityConfig.hasPin) {
                                showPinSetupSection = true
                            }
                            onSetAppLockEnabled(enabled)
                        }
                    )
                }
            }

            // PIN Management Section
            Text(
                text = if (isBangla) "পিন কোড কনফিগারেশন" else "PIN Code Setup",
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
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Key, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Text(
                                text = if (securityConfig.hasPin)
                                    if (isBangla) "পিন কোড সেট করা আছে" else "PIN Code Configured"
                                else
                                    if (isBangla) "কোনো পিন সেট করা নেই" else "No PIN Configured",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        if (securityConfig.hasPin) {
                            OutlinedButton(
                                onClick = { showPinSetupSection = !showPinSetupSection },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(if (showPinSetupSection) (if (isBangla) "বাতিল" else "Cancel") else (if (isBangla) "পরিবর্তন" else "Change"), fontSize = 11.sp)
                            }
                        }
                    }

                    if (showPinSetupSection || !securityConfig.hasPin) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                        OutlinedTextField(
                            value = pinInput,
                            onValueChange = {
                                if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                                    pinInput = it
                                    pinError = null
                                }
                            },
                            label = { Text(if (isBangla) "নতুন ৪-৬ সংখ্যার পিন" else "New 4-6 Digit PIN", fontSize = 11.sp) },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = pinConfirmInput,
                            onValueChange = {
                                if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                                    pinConfirmInput = it
                                    pinError = null
                                }
                            },
                            label = { Text(if (isBangla) "পিন নিশ্চিত করুন" else "Confirm PIN", fontSize = 11.sp) },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (pinError != null) {
                            Text(text = pinError ?: "", color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                if (pinInput.length < 4) {
                                    pinError = if (isBangla) "পিন কমপক্ষে ৪ সংখ্যার হতে হবে।" else "PIN must be at least 4 digits."
                                } else if (pinInput != pinConfirmInput) {
                                    pinError = if (isBangla) "পিন দুটি মিলছে না।" else "PINs do not match."
                                } else {
                                    onSetPin(pinInput)
                                    pinInput = ""
                                    pinConfirmInput = ""
                                    showPinSetupSection = false
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (isBangla) "পিন সংরক্ষণ করুন" else "Save PIN", fontSize = 12.sp)
                        }
                    }
                }
            }

            // Biometric Fingerprint Switch
            Text(
                text = if (isBangla) "বায়োমেট্রিক ও ফিঙ্গারপ্রিন্ট" else "Biometrics & Fingerprint",
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
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
                                    imageVector = Icons.Default.Fingerprint,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = if (isBangla) "ফিঙ্গারপ্রিন্ট আনলক" else "Fingerprint Unlock",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (isBangla) "ডিভাইসের ফিঙ্গারপ্রিন্ট সেন্সর ব্যবহার করুন" else "Use device fingerprint sensor to unlock",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    Switch(
                        checked = securityConfig.isBiometricEnabled,
                        onCheckedChange = { onSetBiometricEnabled(it) },
                        enabled = securityConfig.isAppLockEnabled
                    )
                }
            }

            // Lock Timeout
            Text(
                text = if (isBangla) "অটো-লক সময়সীমা" else "Auto-Lock Timeout",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val timeouts = listOf(
                    Pair(0, if (isBangla) "তাত্ক্ষণিক" else "Immediate"),
                    Pair(30, if (isBangla) "৩০ সেকেন্ড" else "30 sec"),
                    Pair(60, if (isBangla) "১ মিনিট" else "1 min"),
                    Pair(300, if (isBangla) "৫ মিনিট" else "5 min")
                )
                timeouts.forEach { (seconds, label) ->
                    val isSelected = securityConfig.lockTimeoutSeconds == seconds
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSetLockTimeoutSeconds(seconds) },
                        label = { Text(label, fontSize = 10.sp) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Sensitive Actions Authentication Toggles
            Text(
                text = if (isBangla) "সংবেদনশীল কাজের জন্য অতিরিক্ত যাচাইকরণ" else "Require PIN for Critical Actions",
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
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(if (isBangla) "অ্যাকাউন্ট গ্রুপ ডিলিট" else "Account Group Deletion", fontSize = 12.sp)
                        Switch(
                            checked = securityConfig.requireAuthForGroupDeletion,
                            onCheckedChange = { onSetRequireAuthForGroupDeletion(it) }
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(if (isBangla) "একাধিক লেনদেন বাল্ক ডিলিট" else "Bulk Multi-Select Deletion", fontSize = 12.sp)
                        Switch(
                            checked = securityConfig.requireAuthForMultiSelect,
                            onCheckedChange = { onSetRequireAuthForMultiSelect(it) }
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(if (isBangla) "ট্র্যাশ চিরতরে খালি করা" else "Empty Recycle Bin / Trash", fontSize = 12.sp)
                        Switch(
                            checked = securityConfig.requireAuthForTrashClear,
                            onCheckedChange = { onSetRequireAuthForTrashClear(it) }
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(if (isBangla) "গুগল ড্রাইভ ব্যাকআপ রিস্টোর" else "Google Drive Restore", fontSize = 12.sp)
                        Switch(
                            checked = securityConfig.requireAuthForBackupRestore,
                            onCheckedChange = { onSetRequireAuthForBackupRestore(it) }
                        )
                    }
                }
            }

            // Security Recovery Question
            Text(
                text = if (isBangla) "সিকিউরিটি রিকভারি প্রশ্ন" else "Security Recovery Question",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (isBangla) "পিন ভুলে গেলে এই প্রশ্নের উত্তরের মাধ্যমে আনলক করতে পারবেন।" else "In case you forget your PIN, you can reset it by answering this question.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )

                    OutlinedTextField(
                        value = recoveryQuestion,
                        onValueChange = { recoveryQuestion = it },
                        label = { Text(if (isBangla) "প্রশ্ন (যেমন: প্রিয় শিক্ষকের নাম কি?)" else "Question (e.g. Favorite teacher?)", fontSize = 11.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = recoveryAnswer,
                        onValueChange = { recoveryAnswer = it },
                        label = { Text(if (isBangla) "উত্তর" else "Answer", fontSize = 11.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Button(
                            onClick = {
                                onSetSecurityRecovery(recoveryQuestion, recoveryAnswer)
                                recoverySavedFeedback = true
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(if (isBangla) "সংরক্ষণ করুন" else "Save Recovery Info", fontSize = 11.sp)
                        }
                    }

                    if (recoverySavedFeedback) {
                        Text(
                            text = if (isBangla) "রিকভারি তথ্য সংরক্ষিত হয়েছে।" else "Recovery info saved successfully.",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
