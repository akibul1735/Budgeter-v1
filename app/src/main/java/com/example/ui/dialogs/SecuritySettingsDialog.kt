package com.example.ui.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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

/**
 * Material 3 Redesigned Password & Security Hub
 * - Clean 3-tab architecture: Lock & Auth, Protection Rules, Recovery & Privacy
 * - Recovery question is required when setting password the first time
 * - Recovery question is kept private and only displayed during "Forgot Password" flow
 * - Compact grouped controls, 1-tap timeout choice chips, and unified action guards
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
    onSetSecurityRecovery: (String, String) -> Unit,
    onVerifySecurityAnswer: (String) -> Boolean = { false }
) {
    val context = LocalContext.current
    val isBangla = languageMode == LanguageMode.BANGLA
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val biometricAvailability = remember { BiometricHelper.checkBiometricAvailability(context) }
    val isBiometricCapable = biometricAvailability == BiometricAvailability.AVAILABLE || biometricAvailability == BiometricAvailability.NONE_ENROLLED

    var selectedTab by remember { mutableIntStateOf(0) }

    // Dialog state controllers
    var showPinSetupDialog by remember { mutableStateOf(false) }
    var showChangePinDialog by remember { mutableStateOf(false) }
    var showForgotPinDialog by remember { mutableStateOf(false) }
    var showUpdateRecoveryDialog by remember { mutableStateOf(false) }

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
            // Header Section
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
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = SolidPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Text(
                            text = if (isBangla) "পাসওয়ার্ড ও নিরাপত্তা" else "Password & Security",
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (securityConfig.isAppLockEnabled && securityConfig.hasPin)
                                            Color(0xFF2E7D32)
                                        else
                                            Color(0xFFE65100)
                                    )
                            )
                            Text(
                                text = if (securityConfig.isAppLockEnabled && securityConfig.hasPin) {
                                    if (isBangla) "সুরক্ষা সক্রিয়" else "Protection Active"
                                } else {
                                    if (isBangla) "সুরক্ষা বন্ধ" else "Lock Disabled"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (securityConfig.isAppLockEnabled && securityConfig.hasPin)
                                    Color(0xFF2E7D32)
                                else
                                    Color(0xFFE65100)
                            )
                        }
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

            // Compact 3-Tab Segmented Row
            val tabs = listOf(
                if (isBangla) "লক ও অথ" else "Lock & Auth",
                if (isBangla) "সুরক্ষা নিয়মাবলী" else "Action Guards",
                if (isBangla) "রিকভারি ও গোপনীয়তা" else "Recovery & Privacy"
            )

            SecondaryTabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = SolidPrimary
            ) {
                tabs.forEachIndexed { index, label ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = label,
                                fontSize = 13.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // Tab Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                when (selectedTab) {
                    0 -> LockAndAuthTab(
                        securityConfig = securityConfig,
                        isBangla = isBangla,
                        isBiometricCapable = isBiometricCapable,
                        biometricAvailability = biometricAvailability,
                        onSetAppLockEnabled = { enabled ->
                            if (enabled && !securityConfig.hasPin) {
                                showPinSetupDialog = true
                            } else {
                                onSetAppLockEnabled(enabled)
                            }
                        },
                        onSetUpPin = { showPinSetupDialog = true },
                        onChangePin = { showChangePinDialog = true },
                        onForgotPin = { showForgotPinDialog = true },
                        onSetBiometricEnabled = onSetBiometricEnabled,
                        onSetLockTimeoutSeconds = onSetLockTimeoutSeconds
                    )
                    1 -> ProtectionRulesTab(
                        securityConfig = securityConfig,
                        isBangla = isBangla,
                        onSetRequireAuthForGroupDeletion = onSetRequireAuthForGroupDeletion,
                        onSetRequireAuthForMultiSelect = onSetRequireAuthForMultiSelect,
                        onSetRequireAuthForTrashClear = onSetRequireAuthForTrashClear,
                        onSetRequireAuthForBackupRestore = onSetRequireAuthForBackupRestore
                    )
                    2 -> RecoveryAndPrivacyTab(
                        securityConfig = securityConfig,
                        isBangla = isBangla,
                        onUpdateRecovery = {
                            if (securityConfig.hasPin) {
                                showUpdateRecoveryDialog = true
                            } else {
                                showPinSetupDialog = true
                            }
                        },
                        onForgotPin = { showForgotPinDialog = true }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // 1. First-Time Password & Recovery Setup Dialog
    // (MANDATORY: Asks for PIN AND Recovery Question + Answer together on first setup)
    if (showPinSetupDialog) {
        FirstTimePasswordSetupDialog(
            isBangla = isBangla,
            onDismiss = { showPinSetupDialog = false },
            onSave = { pin, question, answer ->
                onSetPin(pin)
                onSetSecurityRecovery(question, answer)
                onSetAppLockEnabled(true)
                showPinSetupDialog = false
            }
        )
    }

    // 2. Change PIN Dialog (with Forgot PIN fallback)
    if (showChangePinDialog) {
        ChangePinDialog(
            isBangla = isBangla,
            onDismiss = { showChangePinDialog = false },
            onVerifyCurrentPin = onVerifyPin,
            onSaveNewPin = { newPin ->
                onSetPin(newPin)
                showChangePinDialog = false
            },
            onForgotPinClick = {
                showChangePinDialog = false
                showForgotPinDialog = true
            }
        )
    }

    // 3. Forgot PIN & Security Question Reset Dialog
    // (Reveals recovery question ONLY here when user forgot PIN)
    if (showForgotPinDialog) {
        ForgotPinRecoveryDialog(
            securityConfig = securityConfig,
            isBangla = isBangla,
            onDismiss = { showForgotPinDialog = false },
            onVerifyAnswer = onVerifySecurityAnswer,
            onSaveNewPin = { newPin, newQuestion, newAnswer ->
                onSetPin(newPin)
                if (newQuestion.isNotBlank() && newAnswer.isNotBlank()) {
                    onSetSecurityRecovery(newQuestion, newAnswer)
                }
                onSetAppLockEnabled(true)
                showForgotPinDialog = false
            }
        )
    }

    // 4. Update Recovery Question Dialog (Protected by current PIN)
    if (showUpdateRecoveryDialog) {
        UpdateRecoveryQuestionDialog(
            securityConfig = securityConfig,
            isBangla = isBangla,
            onDismiss = { showUpdateRecoveryDialog = false },
            onVerifyCurrentPin = onVerifyPin,
            onSaveRecovery = { question, answer ->
                onSetSecurityRecovery(question, answer)
                showUpdateRecoveryDialog = false
            }
        )
    }
}

/**
 * Tab 0: Master App Lock & Credentials
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LockAndAuthTab(
    securityConfig: SecurityConfig,
    isBangla: Boolean,
    isBiometricCapable: Boolean,
    biometricAvailability: BiometricAvailability,
    onSetAppLockEnabled: (Boolean) -> Unit,
    onSetUpPin: () -> Unit,
    onChangePin: () -> Unit,
    onForgotPin: () -> Unit,
    onSetBiometricEnabled: (Boolean) -> Unit,
    onSetLockTimeoutSeconds: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Master Lock Card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (securityConfig.isAppLockEnabled)
                    SolidPrimary.copy(alpha = 0.08f)
                else
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (securityConfig.isAppLockEnabled) SolidPrimary.copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (securityConfig.isAppLockEnabled) Icons.Default.Lock else Icons.Default.LockOpen,
                            contentDescription = null,
                            tint = if (securityConfig.isAppLockEnabled) SolidPrimary else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = if (isBangla) "অ্যাপ লক সক্ষম করুন" else "Enable App Lock",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = if (isBangla) "অ্যাপ খোলার সময় পিন বা ফিঙ্গারপ্রিন্ট দিয়ে আনলক করুন" else "Lock app on launch and switching apps",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                Switch(
                    checked = securityConfig.isAppLockEnabled,
                    onCheckedChange = onSetAppLockEnabled,
                    colors = SwitchDefaults.colors(checkedThumbColor = SolidPrimary),
                    modifier = Modifier.testTag("switch_app_lock")
                )
            }
        }

        // Credentials & Authentication Group
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // PIN Tile
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
                            imageVector = Icons.Default.Pin,
                            contentDescription = null,
                            tint = SolidPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                text = if (isBangla) "পিন / পাসওয়ার্ড" else "PIN / Password",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (securityConfig.hasPin) {
                                    if (isBangla) "● ● ● ● (সংরক্ষিত ও সুরক্ষিত)" else "● ● ● ● (Active & Protected)"
                                } else {
                                    if (isBangla) "কোনো পিন সেট করা নেই" else "No PIN configured"
                                },
                                fontSize = 12.sp,
                                color = if (securityConfig.hasPin) SolidPrimary else MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    if (securityConfig.hasPin) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            TextButton(onClick = onForgotPin) {
                                Text(
                                    text = if (isBangla) "ভুলে গেছেন?" else "Forgot?",
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 12.sp
                                )
                            }
                            Button(
                                onClick = onChangePin,
                                colors = ButtonDefaults.buttonColors(containerColor = SolidPrimary.copy(alpha = 0.12f)),
                                elevation = ButtonDefaults.buttonElevation(0.dp)
                            ) {
                                Text(
                                    text = if (isBangla) "পরিবর্তন" else "Change",
                                    color = SolidPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        Button(
                            onClick = onSetUpPin,
                            colors = ButtonDefaults.buttonColors(containerColor = SolidPrimary)
                        ) {
                            Text(
                                text = if (isBangla) "সেট আপ" else "Set Up",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // Biometric Unlock Tile
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
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = null,
                            tint = if (isBiometricCapable) SolidPrimary else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = if (isBangla) "ফিঙ্গারপ্রিন্ট আনলক" else "Fingerprint Unlock",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = when (biometricAvailability) {
                                    BiometricAvailability.AVAILABLE ->
                                        if (isBangla) "ডিভাইস বায়োমেট্রিক সেন্সর প্রস্তুত" else "Biometric sensor ready"
                                    BiometricAvailability.NONE_ENROLLED ->
                                        if (isBangla) "ডিভাইস সেটিংসে ফিঙ্গারপ্রিন্ট যোগ করুন" else "Enroll fingerprint in device settings"
                                    else ->
                                        if (isBangla) "বায়োমেট্রিক সেন্সর অনুপলব্ধ" else "Sensor unavailable on this device"
                                },
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    Switch(
                        checked = securityConfig.isBiometricEnabled,
                        onCheckedChange = onSetBiometricEnabled,
                        enabled = isBiometricCapable && securityConfig.hasPin,
                        colors = SwitchDefaults.colors(checkedThumbColor = SolidPrimary),
                        modifier = Modifier.testTag("switch_biometric")
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // Auto-Lock Timeout with 1-Tap Chips
                Column(modifier = Modifier.fillMaxWidth()) {
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
                                text = if (isBangla) "স্বয়ংক্রিয় লক সময়" else "Auto-Lock Timeout",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (isBangla) "অ্যাপ ব্যাকগ্রাউন্ডে যাওয়ার কতক্ষণ পর লক হবে" else "When app re-locks after switching apps",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val timeoutOptions = listOf(
                        0 to (if (isBangla) "তাত্ক্ষণিক" else "Immediately"),
                        60 to (if (isBangla) "১ মিনিট" else "1 min"),
                        300 to (if (isBangla) "৫ মিনিট" else "5 min"),
                        900 to (if (isBangla) "১৫ মিনিট" else "15 min")
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        timeoutOptions.forEach { (seconds, label) ->
                            val isSelected = securityConfig.lockTimeoutSeconds == seconds
                            FilterChip(
                                selected = isSelected,
                                onClick = { onSetLockTimeoutSeconds(seconds) },
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
    }
}

/**
 * Tab 1: Granular Action Guards / Protection Rules
 */
@Composable
private fun ProtectionRulesTab(
    securityConfig: SecurityConfig,
    isBangla: Boolean,
    onSetRequireAuthForGroupDeletion: (Boolean) -> Unit,
    onSetRequireAuthForMultiSelect: (Boolean) -> Unit,
    onSetRequireAuthForTrashClear: (Boolean) -> Unit,
    onSetRequireAuthForBackupRestore: (Boolean) -> Unit
) {
    val activeRulesCount = listOf(
        securityConfig.requireAuthForGroupDeletion,
        securityConfig.requireAuthForMultiSelect,
        securityConfig.requireAuthForTrashClear,
        securityConfig.requireAuthForBackupRestore
    ).count { it }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Summary & Quick Preset Header
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
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
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = if (isBangla) "অ্যাকশন-লেভেল সিকিউরিটি গার্ড" else "Action-Level Security Guards",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = if (isBangla) "$activeRulesCount / ৪ টি সুরক্ষা নিয়ম সক্রিয়" else "$activeRulesCount of 4 security rules active",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TextButton(
                        onClick = {
                            val target = activeRulesCount < 4
                            onSetRequireAuthForGroupDeletion(target)
                            onSetRequireAuthForMultiSelect(target)
                            onSetRequireAuthForTrashClear(target)
                            onSetRequireAuthForBackupRestore(target)
                        }
                    ) {
                        Text(
                            text = if (activeRulesCount == 4) {
                                if (isBangla) "সব বন্ধ" else "Disable All"
                            } else {
                                if (isBangla) "সব চালু" else "Enable All"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SolidPrimary
                        )
                    }
                }
            }
        }

        // Grouped Action Guard Switches
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Rule 1: Group Deletion
                ActionGuardRow(
                    icon = Icons.Default.DeleteSweep,
                    title = if (isBangla) "গ্রুপ ডিলিট সুরক্ষা" else "Group Deletion Guard",
                    description = if (isBangla) "যেকোনো অ্যাকাউন্ট বা ক্যাটাগরি গ্রুপ ডিলিটে পাসওয়ার্ড/ফিঙ্গারপ্রিন্ট লাগবে" else "Require authentication before deleting account or category groups",
                    checked = securityConfig.requireAuthForGroupDeletion,
                    onCheckedChange = onSetRequireAuthForGroupDeletion,
                    testTag = "switch_group_deletion_auth"
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                // Rule 2: Multi-Select Changes & Delete
                ActionGuardRow(
                    icon = Icons.Default.Checklist,
                    title = if (isBangla) "মাল্টি-সিলেক্ট ও ব্যাচ অ্যাকশন" else "Multi-Select Batch Actions",
                    description = if (isBangla) "একসাথে একাধিক লেনদেন পরিবর্তন বা ব্যাচ ডিলিটে সুরক্ষা নিশ্চিত করুন" else "Require authentication when bulk modifying or deleting selected transactions",
                    checked = securityConfig.requireAuthForMultiSelect,
                    onCheckedChange = onSetRequireAuthForMultiSelect,
                    testTag = "switch_multi_select_auth"
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                // Rule 3: Trash Clear & Permanent Delete
                ActionGuardRow(
                    icon = Icons.Default.RestoreFromTrash,
                    title = if (isBangla) "ট্র্যাশ খালি ও স্থায়ী ডিলিট" else "Empty Trash & Permanent Purge",
                    description = if (isBangla) "ট্র্যাশ থেকে আইটেম স্থায়ীভাবে মুছে ফেলতে বা সম্পূর্ণ খালি করতে অথেনটিকেশন লাগবে" else "Require authentication when emptying trash or permanently purging records",
                    checked = securityConfig.requireAuthForTrashClear,
                    onCheckedChange = onSetRequireAuthForTrashClear,
                    testTag = "switch_trash_clear_auth"
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                // Rule 4: Backup Restore & Data Reset
                ActionGuardRow(
                    icon = Icons.Default.Backup,
                    title = if (isBangla) "ব্যাকআপ রিস্টোর ও ডেটা রিসেট" else "Backup Restore & Data Reset",
                    description = if (isBangla) "ব্যাকআপ ফাইল রিস্টোর বা ডেটা রিসেট করার সময় পাসওয়ার্ড লাগবে" else "Require authentication when restoring database archives or wiping data",
                    checked = securityConfig.requireAuthForBackupRestore,
                    onCheckedChange = onSetRequireAuthForBackupRestore,
                    testTag = "switch_backup_restore_auth"
                )
            }
        }
    }
}

@Composable
private fun ActionGuardRow(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
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
                imageVector = icon,
                contentDescription = null,
                tint = if (checked) SolidPrimary else MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(22.dp)
            )
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = SolidPrimary),
            modifier = Modifier.testTag(testTag)
        )
    }
}

/**
 * Tab 2: Recovery & Privacy
 * Notice: Recovery Question is NOT displayed out in the open here.
 * Only status is shown, and question is only revealed during Forgot PIN flow.
 */
@Composable
private fun RecoveryAndPrivacyTab(
    securityConfig: SecurityConfig,
    isBangla: Boolean,
    onUpdateRecovery: () -> Unit,
    onForgotPin: () -> Unit
) {
    val hasRecovery = securityConfig.securityAnswerHash.isNotBlank()

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Recovery Status Card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (hasRecovery) Color(0xFF2E7D32).copy(alpha = 0.15f)
                                else Color(0xFFE65100).copy(alpha = 0.15f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QuestionMark,
                            contentDescription = null,
                            tint = if (hasRecovery) Color(0xFF2E7D32) else Color(0xFFE65100),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isBangla) "সিকিউরিটি রিকভারি প্রশ্ন" else "Security Recovery Question",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = if (hasRecovery) {
                                if (isBangla) "সংরক্ষিত ও গোপন রাখা আছে ✓" else "Configured & Hidden for Privacy ✓"
                            } else {
                                if (isBangla) "এখনও রিকভারি প্রশ্ন সেট করা হয়নি" else "Not configured yet"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (hasRecovery) Color(0xFF2E7D32) else Color(0xFFE65100)
                        )
                    }
                }

                Text(
                    text = if (hasRecovery) {
                        if (isBangla)
                            "গোপনীয়তা ও নিরাপত্তার জন্য রিকভারি প্রশ্নটি সর্বদা স্ক্রিনে দৃশ্যমান থাকে না। পিন ভুলে গেলে লক স্ক্রিনে এটি প্রদর্শিত হবে।"
                        else
                            "For your privacy and security, the recovery question is never displayed on screen. It is only presented when resetting a forgotten PIN."
                    } else {
                        if (isBangla)
                            "পিন ভুলে গেলে অ্যাপ পুনরুদ্ধারের জন্য একটি রিকভারি প্রশ্ন ও গোপন উত্তর সেট করে রাখুন।"
                        else
                            "Set up a recovery question and secret answer so you can safely regain access if you forget your PIN."
                    },
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onUpdateRecovery,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = SolidPrimary)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (hasRecovery) {
                                if (isBangla) "প্রশ্ন পরিবর্তন করুন" else "Update Question"
                            } else {
                                if (isBangla) "এখনই সেট করুন" else "Set Up Now"
                            },
                            fontSize = 13.sp
                        )
                    }

                    if (hasRecovery) {
                        OutlinedButton(
                            onClick = onForgotPin,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = if (isBangla) "টেস্ট বা পিন রিসেট" else "Reset / Recover PIN",
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        // Privacy Architecture Info Callout
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.VerifiedUser,
                    contentDescription = null,
                    tint = SolidPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        text = if (isBangla) "নিরাপদ SHA-256 এনক্রিপশন" else "Secure SHA-256 Hashing",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = if (isBangla)
                            "আপনার পিন এবং রিকভারি প্রশ্নের গোপন উত্তর সরাসরি সংরক্ষণ করা হয় না; এগুলি ক্রিপ্টোগ্রাফিক হ্যাশ আকারে সংরক্ষিত থাকে।"
                        else
                            "Your PIN and recovery secret answer are never stored in plain text; they are one-way cryptographic SHA-256 hashes.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// DIALOGS
// -----------------------------------------------------------------------------------------

/**
 * Dialog 1: First-Time Password & Recovery Setup Dialog
 * Prompts for PIN + Confirm PIN + Security Question + Answer simultaneously!
 */
@Composable
private fun FirstTimePasswordSetupDialog(
    isBangla: Boolean,
    onDismiss: () -> Unit,
    onSave: (pin: String, question: String, answer: String) -> Unit
) {
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var secretAnswer by remember { mutableStateOf("") }
    var pinVisible by remember { mutableStateOf(false) }

    val presetQuestions = remember(isBangla) {
        if (isBangla) {
            listOf(
                "আপনার প্রথম বিদ্যালয়ের নাম কি?",
                "আপনার জন্ম কোন শহরে?",
                "আপনার শৈশবের ডাকনাম কি ছিল?",
                "আপনার প্রথম পোষা প্রাণীর নাম কি ছিল?",
                "আপনার প্রিয় শিক্ষকের নাম কি?"
            )
        } else {
            listOf(
                "What was the name of your first school?",
                "In what city were you born?",
                "What was your childhood nickname?",
                "What was the name of your first pet?",
                "What is the name of your favorite teacher?"
            )
        }
    }

    var selectedQuestion by remember { mutableStateOf(presetQuestions.first()) }
    var isCustomQuestion by remember { mutableStateOf(false) }
    var customQuestionText by remember { mutableStateOf("") }
    var dropdownExpanded by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Key, contentDescription = null, tint = SolidPrimary)
                Text(
                    text = if (isBangla) "নতুন পিন ও সিকিউরিটি রিকভারি" else "Create PIN & Recovery",
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
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (isBangla)
                        "অ্যাপ সুরক্ষিত রাখতে একটি ৪-৮ সংখ্যার পিন দিন এবং পিন ভুলে গেলে পুনরুদ্ধারের জন্য একটি গোপন প্রশ্ন সেট করুন:"
                    else
                        "Set a 4 to 8 digit PIN and select a recovery question to ensure you can recover access if forgotten:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // 1. PIN inputs
                OutlinedTextField(
                    value = newPin,
                    onValueChange = {
                        newPin = it.take(8)
                        errorMessage = ""
                    },
                    label = { Text(if (isBangla) "নতুন পিন (৪–৮ সংখ্যা)" else "New PIN (4–8 digits)") },
                    visualTransformation = if (pinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { pinVisible = !pinVisible }) {
                            Icon(
                                imageVector = if (pinVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null
                            )
                        }
                    }
                )

                OutlinedTextField(
                    value = confirmPin,
                    onValueChange = {
                        confirmPin = it.take(8)
                        errorMessage = ""
                    },
                    label = { Text(if (isBangla) "পিন নিশ্চিত করুন" else "Confirm PIN") },
                    visualTransformation = if (pinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // 2. Recovery Question Dropdown & Field
                Text(
                    text = if (isBangla) "রিকভারি প্রশ্ন নির্বাচন করুন" else "Select Recovery Question",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { dropdownExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isCustomQuestion) (if (isBangla) "নিজস্ব প্রশ্ন..." else "Custom Question...") else selectedQuestion,
                                maxLines = 1,
                                modifier = Modifier.weight(1f),
                                fontSize = 12.sp
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    }

                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.85f)
                    ) {
                        presetQuestions.forEach { question ->
                            DropdownMenuItem(
                                text = { Text(question, fontSize = 13.sp) },
                                onClick = {
                                    selectedQuestion = question
                                    isCustomQuestion = false
                                    dropdownExpanded = false
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text(if (isBangla) "অন্যান্য (নিজস্ব প্রশ্ন লিখুন)..." else "Custom Question...", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                            onClick = {
                                isCustomQuestion = true
                                dropdownExpanded = false
                            }
                        )
                    }
                }

                if (isCustomQuestion) {
                    OutlinedTextField(
                        value = customQuestionText,
                        onValueChange = { customQuestionText = it },
                        label = { Text(if (isBangla) "আপনার প্রশ্নটি লিখুন" else "Type your custom question") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // 3. Secret Answer Field
                OutlinedTextField(
                    value = secretAnswer,
                    onValueChange = {
                        secretAnswer = it
                        errorMessage = ""
                    },
                    label = { Text(if (isBangla) "গোপন উত্তর" else "Secret Answer") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage.isNotBlank()) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalQuestion = if (isCustomQuestion) customQuestionText.trim() else selectedQuestion
                    if (newPin.length < 4) {
                        errorMessage = if (isBangla) "পিন অন্তত ৪ সংখ্যার হতে হবে" else "PIN must be at least 4 digits"
                    } else if (newPin != confirmPin) {
                        errorMessage = if (isBangla) "উভয় পিন এক হতে হবে" else "PINs do not match"
                    } else if (finalQuestion.isBlank()) {
                        errorMessage = if (isBangla) "একটি রিকভারি প্রশ্ন নির্ধারণ করুন" else "Please select or type a recovery question"
                    } else if (secretAnswer.trim().length < 2) {
                        errorMessage = if (isBangla) "গোপন উত্তর অন্তত ২ অক্ষরের হতে হবে" else "Secret answer must be at least 2 characters"
                    } else {
                        onSave(newPin, finalQuestion, secretAnswer.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SolidPrimary)
            ) {
                Text(if (isBangla) "সংরক্ষণ ও সুরক্ষা চালু" else "Save & Enable")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(if (isBangla) "বাতিল" else "Cancel")
            }
        }
    )
}

/**
 * Dialog 2: Change PIN Dialog
 */
@Composable
private fun ChangePinDialog(
    isBangla: Boolean,
    onDismiss: () -> Unit,
    onVerifyCurrentPin: (String) -> Boolean,
    onSaveNewPin: (String) -> Unit,
    onForgotPinClick: () -> Unit
) {
    var currentPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isBangla) "পিন পরিবর্তন করুন" else "Change PIN",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = currentPin,
                    onValueChange = {
                        currentPin = it.take(8)
                        errorMessage = ""
                    },
                    label = { Text(if (isBangla) "বর্তমান পিন" else "Current PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onForgotPinClick) {
                        Text(
                            text = if (isBangla) "বর্তমান পিন ভুলে গেছেন?" else "Forgot Current PIN?",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                OutlinedTextField(
                    value = newPin,
                    onValueChange = {
                        newPin = it.take(8)
                        errorMessage = ""
                    },
                    label = { Text(if (isBangla) "নতুন পিন (৪–৮ সংখ্যা)" else "New PIN (4–8 digits)") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = confirmPin,
                    onValueChange = {
                        confirmPin = it.take(8)
                        errorMessage = ""
                    },
                    label = { Text(if (isBangla) "নতুন পিন নিশ্চিত করুন" else "Confirm New PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage.isNotBlank()) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (!onVerifyCurrentPin(currentPin)) {
                        errorMessage = if (isBangla) "বর্তমান পিন সঠিক নয়!" else "Current PIN is incorrect!"
                    } else if (newPin.length < 4) {
                        errorMessage = if (isBangla) "নতুন পিন অন্তত ৪ সংখ্যার হতে হবে" else "New PIN must be at least 4 digits"
                    } else if (newPin != confirmPin) {
                        errorMessage = if (isBangla) "নতুন পিন দুটি মিলছে না" else "New PINs do not match"
                    } else {
                        onSaveNewPin(newPin)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SolidPrimary)
            ) {
                Text(if (isBangla) "আপডেট করুন" else "Update PIN")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(if (isBangla) "বাতিল" else "Cancel")
            }
        }
    )
}

/**
 * Dialog 3: Forgot PIN Recovery Dialog
 * REVEALS RECOVERY QUESTION ONLY HERE when user forgets password!
 */
@Composable
private fun ForgotPinRecoveryDialog(
    securityConfig: SecurityConfig,
    isBangla: Boolean,
    onDismiss: () -> Unit,
    onVerifyAnswer: (String) -> Boolean,
    onSaveNewPin: (newPin: String, newQuestion: String, newAnswer: String) -> Unit
) {
    var secretAnswer by remember { mutableStateOf("") }
    var isAnswerVerified by remember { mutableStateOf(false) }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Security, contentDescription = null, tint = SolidPrimary)
                Text(
                    text = if (isBangla) "সিকিউরিটি রিকভারি ও পিন রিসেট" else "Reset PIN via Recovery",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (!isAnswerVerified) {
                    Text(
                        text = if (isBangla)
                            "পিন রিসেট করতে আপনার পূর্বনির্ধারিত সিকিউরিটি প্রশ্নের সঠিক উত্তর দিন:"
                        else
                            "Answer your security question below to reset your PIN:",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = if (isBangla) "সিকিউরিটি প্রশ্ন:" else "Security Question:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = securityConfig.securityQuestion.ifBlank {
                                    if (isBangla) "ডিফল্ট রিকভারি প্রশ্ন" else "Default recovery question"
                                },
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    OutlinedTextField(
                        value = secretAnswer,
                        onValueChange = {
                            secretAnswer = it
                            errorMessage = ""
                        },
                        label = { Text(if (isBangla) "গোপন উত্তর" else "Secret Answer") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text(
                        text = if (isBangla) "উত্তর যাচাই সফল হয়েছে! নতুন পিন নির্ধারণ করুন:" else "Answer verified! Enter your new PIN:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2E7D32)
                    )

                    OutlinedTextField(
                        value = newPin,
                        onValueChange = {
                            newPin = it.take(8)
                            errorMessage = ""
                        },
                        label = { Text(if (isBangla) "নতুন পিন (৪–৮ সংখ্যা)" else "New PIN (4–8 digits)") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = confirmPin,
                        onValueChange = {
                            confirmPin = it.take(8)
                            errorMessage = ""
                        },
                        label = { Text(if (isBangla) "নতুন পিন নিশ্চিত করুন" else "Confirm New PIN") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (errorMessage.isNotBlank()) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (!isAnswerVerified) {
                        if (securityConfig.securityAnswerHash.isBlank() || onVerifyAnswer(secretAnswer)) {
                            isAnswerVerified = true
                            errorMessage = ""
                        } else {
                            errorMessage = if (isBangla) "ভুল উত্তর! আবার চেষ্টা করুন।" else "Incorrect secret answer! Try again."
                        }
                    } else {
                        if (newPin.length < 4) {
                            errorMessage = if (isBangla) "পিন অন্তত ৪ সংখ্যার হতে হবে" else "PIN must be at least 4 digits"
                        } else if (newPin != confirmPin) {
                            errorMessage = if (isBangla) "পিন দুটি মিলছে না" else "PINs do not match"
                        } else {
                            onSaveNewPin(newPin, "", "")
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SolidPrimary)
            ) {
                Text(
                    text = if (!isAnswerVerified) {
                        if (isBangla) "উত্তর যাচাই করুন" else "Verify Answer"
                    } else {
                        if (isBangla) "নতুন পিন সংরক্ষণ করুন" else "Save & Unlock"
                    }
                )
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(if (isBangla) "বাতিল" else "Cancel")
            }
        }
    )
}

/**
 * Dialog 4: Update Recovery Question Dialog
 * Verifies Current PIN first, then allows updating question & secret answer.
 */
@Composable
private fun UpdateRecoveryQuestionDialog(
    securityConfig: SecurityConfig,
    isBangla: Boolean,
    onDismiss: () -> Unit,
    onVerifyCurrentPin: (String) -> Boolean,
    onSaveRecovery: (question: String, answer: String) -> Unit
) {
    var currentPin by remember { mutableStateOf("") }
    var isPinVerified by remember { mutableStateOf(!securityConfig.hasPin) }

    val presetQuestions = remember(isBangla) {
        if (isBangla) {
            listOf(
                "আপনার প্রথম বিদ্যালয়ের নাম কি?",
                "আপনার জন্ম কোন শহরে?",
                "আপনার শৈশবের ডাকনাম কি ছিল?",
                "আপনার প্রথম পোষা প্রাণীর নাম কি ছিল?",
                "আপনার প্রিয় শিক্ষকের নাম কি?"
            )
        } else {
            listOf(
                "What was the name of your first school?",
                "In what city were you born?",
                "What was your childhood nickname?",
                "What was the name of your first pet?",
                "What is the name of your favorite teacher?"
            )
        }
    }

    var selectedQuestion by remember { mutableStateOf(presetQuestions.first()) }
    var isCustomQuestion by remember { mutableStateOf(false) }
    var customQuestionText by remember { mutableStateOf("") }
    var secretAnswer by remember { mutableStateOf("") }
    var dropdownExpanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isBangla) "সিকিউরিটি প্রশ্ন আপডেট করুন" else "Update Recovery Question",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (!isPinVerified) {
                    Text(
                        text = if (isBangla) "রিকভারি প্রশ্ন পরিবর্তন করতে আপনার বর্তমান পিন দিন:" else "Enter your current PIN to modify security recovery:",
                        fontSize = 13.sp
                    )

                    OutlinedTextField(
                        value = currentPin,
                        onValueChange = {
                            currentPin = it.take(8)
                            errorMessage = ""
                        },
                        label = { Text(if (isBangla) "বর্তমান পিন" else "Current PIN") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text(
                        text = if (isBangla) "নতুন রিকভারি প্রশ্ন ও গোপন উত্তর নির্বাচন করুন:" else "Choose new recovery question and secret answer:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { dropdownExpanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isCustomQuestion) (if (isBangla) "নিজস্ব প্রশ্ন..." else "Custom Question...") else selectedQuestion,
                                    maxLines = 1,
                                    modifier = Modifier.weight(1f),
                                    fontSize = 12.sp
                                )
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }

                        DropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.85f)
                        ) {
                            presetQuestions.forEach { question ->
                                DropdownMenuItem(
                                    text = { Text(question, fontSize = 13.sp) },
                                    onClick = {
                                        selectedQuestion = question
                                        isCustomQuestion = false
                                        dropdownExpanded = false
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text(if (isBangla) "অন্যান্য (নিজস্ব প্রশ্ন লিখুন)..." else "Custom Question...", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                                onClick = {
                                    isCustomQuestion = true
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }

                    if (isCustomQuestion) {
                        OutlinedTextField(
                            value = customQuestionText,
                            onValueChange = { customQuestionText = it },
                            label = { Text(if (isBangla) "আপনার প্রশ্নটি লিখুন" else "Custom Question") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    OutlinedTextField(
                        value = secretAnswer,
                        onValueChange = {
                            secretAnswer = it
                            errorMessage = ""
                        },
                        label = { Text(if (isBangla) "নতুন গোপন উত্তর" else "New Secret Answer") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (errorMessage.isNotBlank()) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (!isPinVerified) {
                        if (onVerifyCurrentPin(currentPin)) {
                            isPinVerified = true
                            errorMessage = ""
                        } else {
                            errorMessage = if (isBangla) "বর্তমান পিন সঠিক নয়!" else "Current PIN is incorrect!"
                        }
                    } else {
                        val finalQuestion = if (isCustomQuestion) customQuestionText.trim() else selectedQuestion
                        if (finalQuestion.isBlank()) {
                            errorMessage = if (isBangla) "প্রশ্ন নির্ধারণ করুন" else "Please select or enter question"
                        } else if (secretAnswer.trim().length < 2) {
                            errorMessage = if (isBangla) "উত্তর অন্তত ২ অক্ষরের হতে হবে" else "Answer must be at least 2 characters"
                        } else {
                            onSaveRecovery(finalQuestion, secretAnswer.trim())
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SolidPrimary)
            ) {
                Text(
                    text = if (!isPinVerified) {
                        if (isBangla) "যাচাই করুন" else "Verify PIN"
                    } else {
                        if (isBangla) "সংরক্ষণ" else "Save Recovery"
                    }
                )
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(if (isBangla) "বাতিল" else "Cancel")
            }
        }
    )
}
