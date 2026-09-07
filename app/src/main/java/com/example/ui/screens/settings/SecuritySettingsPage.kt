package com.example.ui.screens.settings

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.KeyOff
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LanguageMode
import com.example.ui.components.AppTabHeader
import com.example.ui.theme.SolidPrimary
import com.example.util.SecurityConfig

private val PRESET_QUESTIONS_EN = listOf(
    "What was the name of your first school?",
    "What is your favorite food or dish?",
    "What city was your mother born in?",
    "What is the name of your favorite childhood pet?",
    "What was the model of your first phone?"
)

private val PRESET_QUESTIONS_BN = listOf(
    "আপনার প্রথম বিদ্যালয়ের নাম কি ছিল?",
    "আপনার প্রিয় খাবার কোনটি?",
    "আপনার মায়ের জন্মস্থান কোন শহরে?",
    "আপনার শৈশবের প্রিয় পোষা প্রাণীর নাম কি?",
    "আপনার ব্যবহৃত প্রথম ফোনের মডেল কোনটি ছিল?"
)

private enum class SecurityDialogType {
    NONE,
    CHANGE_PIN,
    REMOVE_PIN,
    UPDATE_RECOVERY,
    FORGOT_PIN_RESET,
    DISABLE_APP_LOCK
}

@OptIn(ExperimentalMaterial3Api::class)
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
    val presetQuestions = if (isBangla) PRESET_QUESTIONS_BN else PRESET_QUESTIONS_EN

    var activeDialog by remember { mutableStateOf(SecurityDialogType.NONE) }

    // Initial PIN Setup Form State (when no PIN exists)
    var initialPinInput by remember { mutableStateOf("") }
    var initialPinConfirmInput by remember { mutableStateOf("") }
    var initialQuestionInput by remember { mutableStateOf(presetQuestions.first()) }
    var initialAnswerInput by remember { mutableStateOf("") }
    var initialSetupError by remember { mutableStateOf<String?>(null) }
    var initialSetupQuestionExpanded by remember { mutableStateOf(false) }

    // Success Toast / Banner Message
    var feedbackMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("security_settings_page")
    ) {
        AppTabHeader(
            title = if (isBangla) "পাসওয়ার্ড ও সিকিউরিটি" else "Password & Security",
            tabIcon = Icons.Default.Shield,
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

            // Feedback Banner
            if (feedbackMessage != null) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Text(feedbackMessage ?: "", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                        }
                        IconButton(onClick = { feedbackMessage = null }, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss", modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }

            // Status Overview Card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (securityConfig.isAppLockEnabled && securityConfig.hasPin)
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    else
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
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
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (securityConfig.isAppLockEnabled && securityConfig.hasPin) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (securityConfig.isAppLockEnabled && securityConfig.hasPin) Icons.Default.Shield else Icons.Default.LockOpen,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = if (isBangla) "অ্যাপ সুরক্ষা লক" else "Master App Lock",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (securityConfig.isAppLockEnabled && securityConfig.hasPin)
                                    if (isBangla) "পিন ও বায়োমেট্রিক দ্বারা সক্রিয়" else "Secured with PIN & Biometrics"
                                else if (securityConfig.hasPin)
                                    if (isBangla) "পিন কনফিগার করা আছে (লক নিষ্ক্রিয়)" else "PIN configured (Lock is turned off)"
                                else
                                    if (isBangla) "কোনো পিন সেট করা নেই" else "No PIN configured",
                                fontSize = 11.sp,
                                color = if (securityConfig.isAppLockEnabled && securityConfig.hasPin) SolidPrimary else MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    Switch(
                        checked = securityConfig.isAppLockEnabled,
                        onCheckedChange = { enableRequested ->
                            if (enableRequested) {
                                if (!securityConfig.hasPin) {
                                    // User needs to setup PIN first
                                    feedbackMessage = if (isBangla) "অনুগ্রহ করে প্রথমে নিচে পিন কোড সেট করুন।" else "Please setup a PIN below first."
                                } else {
                                    onSetAppLockEnabled(true)
                                    feedbackMessage = if (isBangla) "অ্যাপ লক সক্রিয় করা হয়েছে।" else "App Lock activated."
                                }
                            } else {
                                if (securityConfig.hasPin) {
                                    // Require PIN verification before disabling lock
                                    activeDialog = SecurityDialogType.DISABLE_APP_LOCK
                                } else {
                                    onSetAppLockEnabled(false)
                                }
                            }
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = SolidPrimary)
                    )
                }
            }

            // SECTION 1: PIN Setup & Management
            Text(
                text = if (isBangla) "পিন কোড সুরক্ষা" else "PIN Code Security",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            if (!securityConfig.hasPin) {
                // Initial PIN Setup Form Card (Mandates PIN + Confirm + Security Question & Answer together)
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Key, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Text(
                                text = if (isBangla) "নতুন পিন ও সিকিউরিটি রিকভারি সেট করুন" else "Create New PIN & Recovery Question",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = if (isBangla)
                                "আপনার গোপনীয় হিসাব সুরক্ষিত রাখতে ৪ থেকে ৬ সংখ্যার পিন এবং ভুলে গেলে পুনরুদ্ধারের জন্য একটি গোপন প্রশ্ন সেট করুন।"
                            else
                                "Protect your financial privacy with a 4-6 digit PIN, and setup a recovery question in case you forget it.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = initialPinInput,
                                onValueChange = {
                                    if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                                        initialPinInput = it
                                        initialSetupError = null
                                    }
                                },
                                label = { Text(if (isBangla) "নতুন পিন" else "New PIN", fontSize = 10.sp) },
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = initialPinConfirmInput,
                                onValueChange = {
                                    if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                                        initialPinConfirmInput = it
                                        initialSetupError = null
                                    }
                                },
                                label = { Text(if (isBangla) "নিশ্চিত করুন" else "Confirm PIN", fontSize = 10.sp) },
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                        Text(
                            text = if (isBangla) "রিকভারি সিকিউরিটি প্রশ্ন ও উত্তর:" else "Recovery Security Question & Answer:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        ExposedDropdownMenuBox(
                            expanded = initialSetupQuestionExpanded,
                            onExpandedChange = { initialSetupQuestionExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = initialQuestionInput,
                                onValueChange = { initialQuestionInput = it },
                                label = { Text(if (isBangla) "সিকিউরিটি প্রশ্ন" else "Security Question", fontSize = 10.sp) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = initialSetupQuestionExpanded) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = initialSetupQuestionExpanded,
                                onDismissRequest = { initialSetupQuestionExpanded = false }
                            ) {
                                presetQuestions.forEach { q ->
                                    DropdownMenuItem(
                                        text = { Text(q, fontSize = 12.sp) },
                                        onClick = {
                                            initialQuestionInput = q
                                            initialSetupQuestionExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = initialAnswerInput,
                            onValueChange = {
                                initialAnswerInput = it
                                initialSetupError = null
                            },
                            label = { Text(if (isBangla) "প্রশ্নের উত্তর" else "Secret Answer", fontSize = 10.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (initialSetupError != null) {
                            Text(text = initialSetupError ?: "", color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                when {
                                    initialPinInput.length < 4 -> {
                                        initialSetupError = if (isBangla) "পিন কমপক্ষে ৪ সংখ্যার হতে হবে।" else "PIN must be at least 4 digits."
                                    }
                                    initialPinInput != initialPinConfirmInput -> {
                                        initialSetupError = if (isBangla) "পিন দুটি মিলছে না।" else "PINs do not match."
                                    }
                                    initialAnswerInput.trim().length < 2 -> {
                                        initialSetupError = if (isBangla) "অনুগ্রহ করে সিকিউরিটি প্রশ্নের সঠিক উত্তর লিখুন।" else "Please provide an answer to the security question."
                                    }
                                    else -> {
                                        onSetPin(initialPinInput)
                                        onSetSecurityRecovery(initialQuestionInput, initialAnswerInput.trim())
                                        onSetAppLockEnabled(true)
                                        initialPinInput = ""
                                        initialPinConfirmInput = ""
                                        initialAnswerInput = ""
                                        feedbackMessage = if (isBangla) "পিন ও সিকিউরিটি রিকভারি সফলভাবে সংরক্ষিত হয়েছে!" else "PIN and Security Recovery configured successfully!"
                                    }
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (isBangla) "পিন ও সুরক্ষা সংরক্ষণ করুন" else "Save PIN & Enable Protection", fontSize = 12.sp)
                        }
                    }
                }
            } else {
                // PIN is already configured -> Clean Management Card with Protected Actions
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Key, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Column {
                                    Text(
                                        text = if (isBangla) "পিন কোড সক্রিয়" else "PIN Code Active",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "••••••",
                                        fontSize = 12.sp,
                                        letterSpacing = 2.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedButton(
                                    onClick = { activeDialog = SecurityDialogType.CHANGE_PIN },
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Icon(Icons.Default.LockReset, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (isBangla) "পরিবর্তন" else "Change", fontSize = 11.sp)
                                }

                                OutlinedButton(
                                    onClick = { activeDialog = SecurityDialogType.REMOVE_PIN },
                                    shape = RoundedCornerShape(6.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Icon(Icons.Default.KeyOff, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (isBangla) "মুছুন" else "Remove", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }

            // SECTION 2: Biometrics & Auto-Lock
            Text(
                text = if (isBangla) "বায়োমেট্রিক ও অটো-লক" else "Biometrics & Auto-Lock",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Biometric Switch
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
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Fingerprint,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
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
                                    text = if (isBangla) "ডিভাইসের বায়োমেট্রিক সেন্সর ব্যবহার" else "Use device fingerprint sensor",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }

                        Switch(
                            checked = securityConfig.isBiometricEnabled,
                            onCheckedChange = { onSetBiometricEnabled(it) },
                            enabled = securityConfig.isAppLockEnabled && securityConfig.hasPin,
                            colors = SwitchDefaults.colors(checkedThumbColor = SolidPrimary)
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    // Auto Lock Timeout
                    Text(
                        text = if (isBangla) "অটো-লক সময়সীমা:" else "Auto-Lock Timeout:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val timeouts = listOf(
                            Pair(0, if (isBangla) "তাত্ক্ষণিক" else "Immediate"),
                            Pair(30, if (isBangla) "৩০ সে." else "30s"),
                            Pair(60, if (isBangla) "১ মিনিট" else "1m"),
                            Pair(300, if (isBangla) "৫ মিনিট" else "5m")
                        )
                        timeouts.forEach { (seconds, label) ->
                            val isSelected = securityConfig.lockTimeoutSeconds == seconds
                            FilterChip(
                                selected = isSelected,
                                onClick = { onSetLockTimeoutSeconds(seconds) },
                                label = { Text(label, fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // SECTION 3: Security Recovery (Protected Card)
            Text(
                text = if (isBangla) "সিকিউরিটি রিকভারি প্রশ্ন" else "Security Recovery Question",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.HelpOutline, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Column {
                                Text(
                                    text = if (securityConfig.hasRecoveryQuestion)
                                        if (isBangla) "রিকভারি প্রশ্ন কনফিগার করা আছে" else "Recovery Question Active"
                                    else
                                        if (isBangla) "কোনো রিকভারি প্রশ্ন সেট করা নেই" else "No Recovery Question Configured",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (securityConfig.hasRecoveryQuestion)
                                        securityConfig.securityQuestion
                                    else
                                        if (isBangla) "পিন ভুলে গেলে পুনরুদ্ধারের জন্য প্রশ্ন সেট করুন" else "Set question to recover if you forget PIN",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.outline,
                                    maxLines = 1
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                if (securityConfig.hasPin) {
                                    activeDialog = SecurityDialogType.UPDATE_RECOVERY
                                } else {
                                    feedbackMessage = if (isBangla) "প্রথমে পিন কোড সেট করুন।" else "Please setup PIN code first."
                                }
                            },
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(if (isBangla) "আপডেট" else "Update", fontSize = 11.sp)
                        }
                    }

                    if (securityConfig.hasPin && securityConfig.hasRecoveryQuestion) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = { activeDialog = SecurityDialogType.FORGOT_PIN_RESET }
                            ) {
                                Text(
                                    text = if (isBangla) "পিন ভুলে গেছেন? প্রশ্নের মাধ্যমে রিসেট করুন" else "Forgot PIN? Reset with Security Question",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            // SECTION 4: Critical Actions Authentication Toggles
            Text(
                text = if (isBangla) "সংবেদনশীল কাজের জন্য অতিরিক্ত যাচাইকরণ" else "Require PIN for Sensitive Operations",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(if (isBangla) "অ্যাকাউন্ট গ্রুপ ডিলিট" else "Account Group Deletion", fontSize = 12.sp)
                        Switch(
                            checked = securityConfig.requireAuthForGroupDeletion,
                            onCheckedChange = { onSetRequireAuthForGroupDeletion(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = SolidPrimary)
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
                            onCheckedChange = { onSetRequireAuthForMultiSelect(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = SolidPrimary)
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
                            onCheckedChange = { onSetRequireAuthForTrashClear(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = SolidPrimary)
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
                            onCheckedChange = { onSetRequireAuthForBackupRestore(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = SolidPrimary)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // =========================================================================
    // MODAL DIALOGS: AUTHENTICATED PIN CHANGE / REMOVAL / RECOVERY
    // =========================================================================

    // 1. CHANGE PIN DIALOG (Requires Current PIN -> Then Enter New PIN)
    if (activeDialog == SecurityDialogType.CHANGE_PIN) {
        ChangePinDialog(
            isBangla = isBangla,
            onVerifyPin = onVerifyPin,
            onSaveNewPin = { newPin ->
                onSetPin(newPin)
                activeDialog = SecurityDialogType.NONE
                feedbackMessage = if (isBangla) "পিন সফলভাবে পরিবর্তন করা হয়েছে।" else "PIN changed successfully."
            },
            onForgotPassword = {
                activeDialog = SecurityDialogType.FORGOT_PIN_RESET
            },
            onDismiss = { activeDialog = SecurityDialogType.NONE }
        )
    }

    // 2. REMOVE PIN DIALOG (Requires Current PIN)
    if (activeDialog == SecurityDialogType.REMOVE_PIN) {
        RemovePinDialog(
            isBangla = isBangla,
            onVerifyPin = onVerifyPin,
            onConfirmRemove = {
                onSetPin("")
                onSetAppLockEnabled(false)
                activeDialog = SecurityDialogType.NONE
                feedbackMessage = if (isBangla) "পিন মুছে ফেলা হয়েছে এবং সুরক্ষা বন্ধ করা হয়েছে।" else "PIN removed and App Lock disabled."
            },
            onDismiss = { activeDialog = SecurityDialogType.NONE }
        )
    }

    // 3. DISABLE APP LOCK DIALOG (Requires Current PIN)
    if (activeDialog == SecurityDialogType.DISABLE_APP_LOCK) {
        DisableAppLockDialog(
            isBangla = isBangla,
            onVerifyPin = onVerifyPin,
            onConfirmDisable = {
                onSetAppLockEnabled(false)
                activeDialog = SecurityDialogType.NONE
                feedbackMessage = if (isBangla) "অ্যাপ লক নিষ্ক্রিয় করা হয়েছে।" else "App Lock disabled."
            },
            onDismiss = { activeDialog = SecurityDialogType.NONE }
        )
    }

    // 4. UPDATE SECURITY RECOVERY QUESTION DIALOG (Requires Current PIN first!)
    if (activeDialog == SecurityDialogType.UPDATE_RECOVERY) {
        UpdateRecoveryQuestionDialog(
            isBangla = isBangla,
            presetQuestions = presetQuestions,
            currentQuestion = securityConfig.securityQuestion,
            onVerifyPin = onVerifyPin,
            onSaveRecovery = { q, a ->
                onSetSecurityRecovery(q, a)
                activeDialog = SecurityDialogType.NONE
                feedbackMessage = if (isBangla) "সিকিউরিটি রিকভারি প্রশ্ন আপডেট হয়েছে।" else "Security recovery question updated."
            },
            onDismiss = { activeDialog = SecurityDialogType.NONE }
        )
    }

    // 5. FORGOT PIN / RECOVERY RESET DIALOG
    if (activeDialog == SecurityDialogType.FORGOT_PIN_RESET) {
        ForgotPinResetDialog(
            isBangla = isBangla,
            securityQuestion = securityConfig.securityQuestion,
            onVerifySecurityAnswer = onVerifySecurityAnswer,
            onSaveNewPin = { newPin ->
                onSetPin(newPin)
                onSetAppLockEnabled(true)
                activeDialog = SecurityDialogType.NONE
                feedbackMessage = if (isBangla) "নতুন পিন সফলভাবে সংরক্ষিত হয়েছে!" else "New PIN reset and saved successfully!"
            },
            onDismiss = { activeDialog = SecurityDialogType.NONE }
        )
    }
}

// -----------------------------------------------------------------------------
// HELPER DIALOG COMPONENTS
// -----------------------------------------------------------------------------

@Composable
private fun ChangePinDialog(
    isBangla: Boolean,
    onVerifyPin: (String) -> Boolean,
    onSaveNewPin: (String) -> Unit,
    onForgotPassword: () -> Unit,
    onDismiss: () -> Unit
) {
    var step by remember { mutableStateOf(1) } // 1 = Enter Current PIN, 2 = Enter New PIN
    var currentPinInput by remember { mutableStateOf("") }
    var newPinInput by remember { mutableStateOf("") }
    var newPinConfirmInput by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (step == 1)
                    (if (isBangla) "বর্তমান পিন যাচাই করুন" else "Verify Current PIN")
                else
                    (if (isBangla) "নতুন পিন সেট করুন" else "Set New PIN"),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (step == 1) {
                    Text(
                        text = if (isBangla) "পিন পরিবর্তন করতে অনুগ্রহ করে বর্তমান পিন লিখুন।" else "Please enter your current PIN to proceed.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    OutlinedTextField(
                        value = currentPinInput,
                        onValueChange = {
                            if (it.length <= 6 && it.all { c -> c.isDigit() }) {
                                currentPinInput = it
                                errorText = null
                            }
                        },
                        label = { Text(if (isBangla) "বর্তমান পিন" else "Current PIN", fontSize = 11.sp) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    TextButton(onClick = onForgotPassword) {
                        Text(if (isBangla) "পিন ভুলে গেছেন?" else "Forgot PIN?", fontSize = 11.sp)
                    }
                } else {
                    OutlinedTextField(
                        value = newPinInput,
                        onValueChange = {
                            if (it.length <= 6 && it.all { c -> c.isDigit() }) {
                                newPinInput = it
                                errorText = null
                            }
                        },
                        label = { Text(if (isBangla) "নতুন ৪-৬ সংখ্যার পিন" else "New 4-6 Digit PIN", fontSize = 11.sp) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newPinConfirmInput,
                        onValueChange = {
                            if (it.length <= 6 && it.all { c -> c.isDigit() }) {
                                newPinConfirmInput = it
                                errorText = null
                            }
                        },
                        label = { Text(if (isBangla) "নতুন পিন নিশ্চিত করুন" else "Confirm New PIN", fontSize = 11.sp) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (errorText != null) {
                    Text(errorText ?: "", color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (step == 1) {
                        if (onVerifyPin(currentPinInput)) {
                            step = 2
                            errorText = null
                        } else {
                            errorText = if (isBangla) "ভুল বর্তমান পিন।" else "Incorrect current PIN."
                        }
                    } else {
                        if (newPinInput.length < 4) {
                            errorText = if (isBangla) "পিন কমপক্ষে ৪ সংখ্যার হতে হবে।" else "PIN must be at least 4 digits."
                        } else if (newPinInput != newPinConfirmInput) {
                            errorText = if (isBangla) "নতুন পিন দুটি মিলছে না।" else "PINs do not match."
                        } else {
                            onSaveNewPin(newPinInput)
                        }
                    }
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (step == 1)
                        (if (isBangla) "যাচাই করুন" else "Verify")
                    else
                        (if (isBangla) "সংরক্ষণ" else "Save"),
                    fontSize = 12.sp
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBangla) "বাতিল" else "Cancel", fontSize = 12.sp)
            }
        }
    )
}

@Composable
private fun RemovePinDialog(
    isBangla: Boolean,
    onVerifyPin: (String) -> Boolean,
    onConfirmRemove: () -> Unit,
    onDismiss: () -> Unit
) {
    var pinInput by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isBangla) "পিন সুরক্ষা মুছবেন?" else "Remove PIN Protection?", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (isBangla) "পিন মুছে ফেললে অ্যাপ লক এবং সংবেদনশীল সুরক্ষা নিষ্ক্রিয় হয়ে যাবে। নিশ্চিত করতে বর্তমান পিন দিন:" else "Removing the PIN will disable App Lock protection. Enter current PIN to confirm:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.outline
                )
                OutlinedTextField(
                    value = pinInput,
                    onValueChange = {
                        if (it.length <= 6 && it.all { c -> c.isDigit() }) {
                            pinInput = it
                            errorText = null
                        }
                    },
                    label = { Text(if (isBangla) "বর্তমান পিন" else "Current PIN", fontSize = 11.sp) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                if (errorText != null) {
                    Text(errorText ?: "", color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (onVerifyPin(pinInput)) {
                        onConfirmRemove()
                    } else {
                        errorText = if (isBangla) "ভুল পিন কোড।" else "Incorrect PIN code."
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (isBangla) "মুছে ফেলুন" else "Remove PIN", fontSize = 12.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBangla) "বাতিল" else "Cancel", fontSize = 12.sp)
            }
        }
    )
}

@Composable
private fun DisableAppLockDialog(
    isBangla: Boolean,
    onVerifyPin: (String) -> Boolean,
    onConfirmDisable: () -> Unit,
    onDismiss: () -> Unit
) {
    var pinInput by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isBangla) "অ্যাপ লক বন্ধ করুন" else "Turn Off App Lock", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (isBangla) "অ্যাপ লক নিষ্ক্রিয় করতে বর্তমান পিন কোড প্রদান করুন:" else "Enter your current PIN to turn off App Lock:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.outline
                )
                OutlinedTextField(
                    value = pinInput,
                    onValueChange = {
                        if (it.length <= 6 && it.all { c -> c.isDigit() }) {
                            pinInput = it
                            errorText = null
                        }
                    },
                    label = { Text(if (isBangla) "বর্তমান পিন" else "Current PIN", fontSize = 11.sp) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                if (errorText != null) {
                    Text(errorText ?: "", color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (onVerifyPin(pinInput)) {
                        onConfirmDisable()
                    } else {
                        errorText = if (isBangla) "ভুল পিন কোড।" else "Incorrect PIN code."
                    }
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (isBangla) "নিষ্ক্রিয় করুন" else "Disable", fontSize = 12.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBangla) "বাতিল" else "Cancel", fontSize = 12.sp)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UpdateRecoveryQuestionDialog(
    isBangla: Boolean,
    presetQuestions: List<String>,
    currentQuestion: String,
    onVerifyPin: (String) -> Boolean,
    onSaveRecovery: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var step by remember { mutableStateOf(1) } // 1 = Enter Current PIN, 2 = Set Question & Answer
    var pinInput by remember { mutableStateOf("") }
    var selectedQuestion by remember { mutableStateOf(currentQuestion.ifBlank { presetQuestions.first() }) }
    var answerInput by remember { mutableStateOf("") }
    var questionExpanded by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (step == 1)
                    (if (isBangla) "বর্তমান পিন যাচাই" else "Verify Current PIN")
                else
                    (if (isBangla) "রিকভারি প্রশ্ন আপডেট" else "Update Recovery Question"),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (step == 1) {
                    Text(
                        text = if (isBangla) "সিকিউরিটি প্রশ্ন পরিবর্তন করতে বর্তমান পিন প্রদান করুন:" else "Enter your current PIN to edit the recovery question:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = {
                            if (it.length <= 6 && it.all { c -> c.isDigit() }) {
                                pinInput = it
                                errorText = null
                            }
                        },
                        label = { Text(if (isBangla) "বর্তমান পিন" else "Current PIN", fontSize = 11.sp) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    ExposedDropdownMenuBox(
                        expanded = questionExpanded,
                        onExpandedChange = { questionExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedQuestion,
                            onValueChange = { selectedQuestion = it },
                            label = { Text(if (isBangla) "সিকিউরিটি প্রশ্ন" else "Security Question", fontSize = 10.sp) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = questionExpanded) },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = questionExpanded,
                            onDismissRequest = { questionExpanded = false }
                        ) {
                            presetQuestions.forEach { q ->
                                DropdownMenuItem(
                                    text = { Text(q, fontSize = 12.sp) },
                                    onClick = {
                                        selectedQuestion = q
                                        questionExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = answerInput,
                        onValueChange = {
                            answerInput = it
                            errorText = null
                        },
                        label = { Text(if (isBangla) "নতুন গোপন উত্তর" else "New Secret Answer", fontSize = 11.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (errorText != null) {
                    Text(errorText ?: "", color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (step == 1) {
                        if (onVerifyPin(pinInput)) {
                            step = 2
                            errorText = null
                        } else {
                            errorText = if (isBangla) "ভুল পিন কোড।" else "Incorrect PIN code."
                        }
                    } else {
                        if (answerInput.trim().length < 2) {
                            errorText = if (isBangla) "সঠিক উত্তর লিখুন।" else "Please enter a valid answer."
                        } else {
                            onSaveRecovery(selectedQuestion, answerInput.trim())
                        }
                    }
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (step == 1) (if (isBangla) "যাচাই" else "Verify") else (if (isBangla) "সংরক্ষণ" else "Save"), fontSize = 12.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBangla) "বাতিল" else "Cancel", fontSize = 12.sp)
            }
        }
    )
}

@Composable
private fun ForgotPinResetDialog(
    isBangla: Boolean,
    securityQuestion: String,
    onVerifySecurityAnswer: (String) -> Boolean,
    onSaveNewPin: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var step by remember { mutableStateOf(1) } // 1 = Answer Question, 2 = Enter New PIN
    var answerInput by remember { mutableStateOf("") }
    var newPinInput by remember { mutableStateOf("") }
    var newPinConfirmInput by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (step == 1)
                    (if (isBangla) "সিকিউরিটি রিকভারি" else "Security Recovery")
                else
                    (if (isBangla) "নতুন পিন নির্ধারণ" else "Reset New PIN"),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (step == 1) {
                    Text(
                        text = if (isBangla) "পিন রিসেট করতে পূর্বনির্ধারিত প্রশ্নের উত্তর দিন:" else "Answer your security question to reset your PIN:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = securityQuestion,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(10.dp)
                        )
                    }

                    OutlinedTextField(
                        value = answerInput,
                        onValueChange = {
                            answerInput = it
                            errorText = null
                        },
                        label = { Text(if (isBangla) "উত্তর" else "Answer", fontSize = 11.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    OutlinedTextField(
                        value = newPinInput,
                        onValueChange = {
                            if (it.length <= 6 && it.all { c -> c.isDigit() }) {
                                newPinInput = it
                                errorText = null
                            }
                        },
                        label = { Text(if (isBangla) "নতুন ৪-৬ সংখ্যার পিন" else "New 4-6 Digit PIN", fontSize = 11.sp) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newPinConfirmInput,
                        onValueChange = {
                            if (it.length <= 6 && it.all { c -> c.isDigit() }) {
                                newPinConfirmInput = it
                                errorText = null
                            }
                        },
                        label = { Text(if (isBangla) "নতুন পিন নিশ্চিত করুন" else "Confirm New PIN", fontSize = 11.sp) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (errorText != null) {
                    Text(errorText ?: "", color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (step == 1) {
                        if (onVerifySecurityAnswer(answerInput)) {
                            step = 2
                            errorText = null
                        } else {
                            errorText = if (isBangla) "ভুল উত্তর। পুনরায় চেষ্টা করুন।" else "Incorrect answer. Please try again."
                        }
                    } else {
                        if (newPinInput.length < 4) {
                            errorText = if (isBangla) "পিন কমপক্ষে ৪ সংখ্যার হতে হবে।" else "PIN must be at least 4 digits."
                        } else if (newPinInput != newPinConfirmInput) {
                            errorText = if (isBangla) "পিন দুটি মিলছে না।" else "PINs do not match."
                        } else {
                            onSaveNewPin(newPinInput)
                        }
                    }
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (step == 1) (if (isBangla) "যাচাই করুন" else "Verify") else (if (isBangla) "সংরক্ষণ" else "Save PIN"), fontSize = 12.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBangla) "বাতিল" else "Cancel", fontSize = 12.sp)
            }
        }
    )
}
