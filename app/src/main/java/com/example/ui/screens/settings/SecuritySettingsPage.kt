package com.example.ui.screens.settings

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
import androidx.compose.material3.OutlinedCard
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
    onSetRequireAuthForBackupDeletion: (Boolean) -> Unit = {},
    onSetLockTimeoutSeconds: (Int) -> Unit,
    onSetSecurityRecovery: (String, String) -> Unit,
    onVerifySecurityAnswer: (String) -> Boolean,
    onBack: () -> Unit
) {
    val isBangla = languageMode == LanguageMode.BANGLA
    val presetQuestions = if (isBangla) PRESET_QUESTIONS_BN else PRESET_QUESTIONS_EN

    var activeDialog by remember { mutableStateOf(SecurityDialogType.NONE) }

    // Initial PIN Setup Form State
    var initialPinInput by remember { mutableStateOf("") }
    var initialPinConfirmInput by remember { mutableStateOf("") }
    var initialQuestionInput by remember { mutableStateOf(presetQuestions.first()) }
    var initialAnswerInput by remember { mutableStateOf("") }
    var initialSetupError by remember { mutableStateOf<String?>(null) }
    var initialSetupQuestionExpanded by remember { mutableStateOf(false) }

    // Success Banner Message
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
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(modifier = Modifier.height(2.dp))

            // Feedback Banner
            if (feedbackMessage != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                feedbackMessage ?: "",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        IconButton(onClick = { feedbackMessage = null }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Status Overview Card
            OutlinedCard(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = if (securityConfig.isAppLockEnabled && securityConfig.hasPin)
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                    else
                        MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(
                    1.dp,
                    if (securityConfig.isAppLockEnabled && securityConfig.hasPin)
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    else
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
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
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (securityConfig.isAppLockEnabled && securityConfig.hasPin) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (securityConfig.isAppLockEnabled && securityConfig.hasPin) Icons.Default.Shield else Icons.Default.LockOpen,
                                    contentDescription = null,
                                    tint = if (securityConfig.isAppLockEnabled && securityConfig.hasPin) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isBangla) "অ্যাপ সুরক্ষা লক" else "Master App Lock",
                                fontSize = 15.sp,
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
                                fontSize = 12.sp,
                                color = if (securityConfig.isAppLockEnabled && securityConfig.hasPin) SolidPrimary else MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    Switch(
                        checked = securityConfig.isAppLockEnabled,
                        onCheckedChange = { enableRequested ->
                            if (enableRequested) {
                                if (!securityConfig.hasPin) {
                                    feedbackMessage = if (isBangla) "অনুগ্রহ করে প্রথমে নিচে পিন কোড সেট করুন।" else "Please setup a PIN below first."
                                } else {
                                    onSetAppLockEnabled(true)
                                    feedbackMessage = if (isBangla) "অ্যাপ লক সক্রিয় করা হয়েছে।" else "App Lock activated."
                                }
                            } else {
                                if (securityConfig.hasPin) {
                                    activeDialog = SecurityDialogType.DISABLE_APP_LOCK
                                } else {
                                    onSetAppLockEnabled(false)
                                }
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }

            // SECTION 1: PIN Setup & Management
            Text(
                text = if (isBangla) "পিন কোড সুরক্ষা" else "PIN Code Security",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            if (!securityConfig.hasPin) {
                OutlinedCard(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.Key, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                            Text(
                                text = if (isBangla) "নতুন পিন ও সিকিউরিটি রিকভারি সেট করুন" else "Create New PIN & Recovery Question",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = if (isBangla)
                                "আপনার গোপনীয় হিসাব সুরক্ষিত রাখতে ৪ থেকে ৬ সংখ্যার পিন এবং ভুলে গেলে পুনরুদ্ধারের জন্য একটি গোপন প্রশ্ন সেট করুন।"
                            else
                                "Protect your financial privacy with a 4-6 digit PIN, and setup a recovery question in case you forget it.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = initialPinInput,
                                onValueChange = {
                                    if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                                        initialPinInput = it
                                        initialSetupError = null
                                    }
                                },
                                label = { Text(if (isBangla) "নতুন পিন" else "New PIN", fontSize = 11.sp) },
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
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
                                label = { Text(if (isBangla) "নিশ্চিত করুন" else "Confirm PIN", fontSize = 11.sp) },
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                        Text(
                            text = if (isBangla) "রিকভারি সিকিউরিটি প্রশ্ন ও উত্তর:" else "Recovery Security Question & Answer:",
                            fontSize = 12.sp,
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
                                label = { Text(if (isBangla) "সিকিউরিটি প্রশ্ন" else "Security Question", fontSize = 11.sp) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = initialSetupQuestionExpanded) },
                                shape = RoundedCornerShape(10.dp),
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
                            label = { Text(if (isBangla) "প্রশ্নের উত্তর" else "Secret Answer", fontSize = 11.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (initialSetupError != null) {
                            Text(text = initialSetupError ?: "", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
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
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (isBangla) "পিন ও সুরক্ষা সংরক্ষণ করুন" else "Save PIN & Enable Protection", fontSize = 13.sp)
                        }
                    }
                }
            } else {
                OutlinedCard(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f).padding(end = 8.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Key, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                    }
                                }
                                Column {
                                    Text(
                                        text = if (isBangla) "পিন কোড সক্রিয়" else "PIN Code Active",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        softWrap = true
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
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.LockReset, contentDescription = null, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(if (isBangla) "পরিবর্তন" else "Change", fontSize = 11.sp)
                                }

                                OutlinedButton(
                                    onClick = { activeDialog = SecurityDialogType.REMOVE_PIN },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.KeyOff, contentDescription = null, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
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
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedCard(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Biometric Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
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
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (isBangla) "ডিভাইসের বায়োমেট্রিক সেন্সর ব্যবহার" else "Use device fingerprint sensor",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }

                        Switch(
                            checked = securityConfig.isBiometricEnabled,
                            onCheckedChange = { onSetBiometricEnabled(it) },
                            enabled = securityConfig.isAppLockEnabled && securityConfig.hasPin,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    // Auto Lock Timeout
                    Text(
                        text = if (isBangla) "অটো-লক সময়সীমা:" else "Auto-Lock Timeout:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val timeouts = listOf(
                            Pair(0, if (isBangla) "তাত্ক্ষণিক" else "Immediate"),
                            Pair(30, if (isBangla) "৩০ সে." else "30s"),
                            Pair(60, if (isBangla) "১ মিনিট" else "1m"),
                            Pair(300, if (isBangla) "৫ মিনিট" else "5m")
                        )
                        timeouts.forEach { (seconds, label) ->
                            val isSelected = securityConfig.lockTimeoutSeconds == seconds
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onSetLockTimeoutSeconds(seconds) }
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        softWrap = false,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // SECTION 3: Security Recovery
            Text(
                text = if (isBangla) "সিকিউরিটি রিকভারি প্রশ্ন" else "Security Recovery Question",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedCard(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f).padding(end = 8.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.HelpOutline, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                }
                            }
                            Column(modifier = Modifier.weight(1f, fill = false)) {
                                Text(
                                    text = if (securityConfig.hasRecoveryQuestion)
                                        if (isBangla) "রিকভারি প্রশ্ন কনফিগার করা আছে" else "Recovery Question Active"
                                    else
                                        if (isBangla) "কোনো রিকভারি প্রশ্ন সেট করা নেই" else "No Recovery Question Configured",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    softWrap = true
                                )
                                Text(
                                    text = if (securityConfig.hasRecoveryQuestion)
                                        securityConfig.securityQuestion
                                    else
                                        if (isBangla) "পিন ভুলে গেলে পুনরুদ্ধারের জন্য প্রশ্ন সেট করুন" else "Set question to recover if you forget PIN",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.outline,
                                    maxLines = 2,
                                    softWrap = true
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
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(if (isBangla) "আপডেট" else "Update", fontSize = 12.sp)
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
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    softWrap = true
                                )
                            }
                        }
                    }
                }
            }

            // SECTION 4: Critical Actions Authentication Toggles
            Text(
                text = if (isBangla) "সংবেদনশীল কাজের জন্য অতিরিক্ত যাচাইকরণ" else "Require PIN for Sensitive Operations",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedCard(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isBangla) "অ্যাকাউন্ট গ্রুপ ডিলিট" else "Account Group Deletion",
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f).padding(end = 8.dp),
                            softWrap = true
                        )
                        Switch(
                            checked = securityConfig.requireAuthForGroupDeletion,
                            onCheckedChange = { onSetRequireAuthForGroupDeletion(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isBangla) "একাধিক লেনদেন বাল্ক ডিলিট" else "Bulk Multi-Select Deletion",
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f).padding(end = 8.dp),
                            softWrap = true
                        )
                        Switch(
                            checked = securityConfig.requireAuthForMultiSelect,
                            onCheckedChange = { onSetRequireAuthForMultiSelect(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isBangla) "ট্র্যাশ চিরতরে খালি করা" else "Empty Recycle Bin / Trash",
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f).padding(end = 8.dp),
                            softWrap = true
                        )
                        Switch(
                            checked = securityConfig.requireAuthForTrashClear,
                            onCheckedChange = { onSetRequireAuthForTrashClear(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isBangla) "গুগল ড্রাইভ ব্যাকআপ রিস্টোর" else "Google Drive Restore",
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f).padding(end = 8.dp),
                            softWrap = true
                        )
                        Switch(
                            checked = securityConfig.requireAuthForBackupRestore,
                            onCheckedChange = { onSetRequireAuthForBackupRestore(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text(if (isBangla) "ব্যাকআপ ফাইল ডিলিট" else "Delete Backup Files", fontSize = 13.sp)
                            Text(
                                text = if (isBangla) "লোকাল বা ড্রাইভ ব্যাকআপ মুছতে পিন/ফিঙ্গারপ্রিন্ট যাচাই" else "Require PIN/fingerprint to delete local or Drive backups",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        Switch(
                            checked = securityConfig.requireAuthForBackupDeletion,
                            onCheckedChange = { onSetRequireAuthForBackupDeletion(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // =========================================================================
    // MODAL DIALOGS
    // =========================================================================

    // 1. CHANGE PIN DIALOG
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

    // 2. REMOVE PIN DIALOG
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

    // 3. DISABLE APP LOCK DIALOG
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

    // 4. UPDATE SECURITY RECOVERY QUESTION DIALOG
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
    var step by remember { mutableStateOf(1) }
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
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    TextButton(onClick = onForgotPassword) {
                        Text(if (isBangla) "পিন ভুলে গেছেন?" else "Forgot PIN?", fontSize = 12.sp)
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
                        shape = RoundedCornerShape(10.dp),
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
                        shape = RoundedCornerShape(10.dp),
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
                shape = RoundedCornerShape(10.dp)
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
                    shape = RoundedCornerShape(10.dp),
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
                shape = RoundedCornerShape(10.dp)
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
                    shape = RoundedCornerShape(10.dp),
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
                shape = RoundedCornerShape(10.dp)
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
    var step by remember { mutableStateOf(1) }
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
                        shape = RoundedCornerShape(10.dp),
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
                            label = { Text(if (isBangla) "সিকিউরিটি প্রশ্ন" else "Security Question", fontSize = 11.sp) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = questionExpanded) },
                            shape = RoundedCornerShape(10.dp),
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
                        shape = RoundedCornerShape(10.dp),
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
                shape = RoundedCornerShape(10.dp)
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
    var step by remember { mutableStateOf(1) }
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
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = securityQuestion,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(12.dp)
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
                        shape = RoundedCornerShape(10.dp),
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
                        shape = RoundedCornerShape(10.dp),
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
                        shape = RoundedCornerShape(10.dp),
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
                shape = RoundedCornerShape(10.dp)
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
