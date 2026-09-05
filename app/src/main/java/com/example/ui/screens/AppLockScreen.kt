package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.data.model.LanguageMode
import com.example.ui.theme.SolidPrimary
import com.example.util.BiometricHelper
import com.example.util.SecurityConfig
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun AppLockScreen(
    securityConfig: SecurityConfig,
    languageMode: LanguageMode,
    onVerifyPin: (String) -> Boolean,
    onVerifySecurityAnswer: (String) -> Boolean,
    onUnlockSuccess: () -> Unit,
    onResetPinAfterRecovery: (String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var enteredPin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isAlphaPasswordMode by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var showForgotDialog by remember { mutableStateOf(false) }

    // Shake animation for wrong PIN
    val shakeOffset = remember { Animatable(0f) }

    fun triggerShake() {
        coroutineScope.launch {
            shakeOffset.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 400
                    0f at 0
                    -20f at 50
                    20f at 100
                    -15f at 150
                    15f at 200
                    -10f at 250
                    10f at 300
                    -5f at 350
                    0f at 400
                }
            )
        }
    }

    fun triggerBiometrics() {
        if (securityConfig.isBiometricEnabled && context is FragmentActivity) {
            BiometricHelper.showBiometricPrompt(
                activity = context,
                title = if (languageMode == LanguageMode.BANGLA) "বাজেটার আনলক করুন" else "Unlock Budgeter",
                subtitle = if (languageMode == LanguageMode.BANGLA) "ফিঙ্গারপ্রিন্ট ব্যবহার করুন" else "Authenticate using fingerprint",
                negativeButtonText = if (languageMode == LanguageMode.BANGLA) "পিন ব্যবহার করুন" else "Use PIN",
                onSuccess = {
                    onUnlockSuccess()
                },
                onError = { _, err ->
                    errorMessage = err
                },
                onNegativeClick = {
                    // Fallback to keypad
                }
            )
        }
    }

    // Auto-prompt biometrics when screen appears
    LaunchedEffect(Unit) {
        if (securityConfig.isBiometricEnabled) {
            triggerBiometrics()
        }
    }

    fun submitPin(pinToTest: String) {
        if (onVerifyPin(pinToTest)) {
            isError = false
            onUnlockSuccess()
        } else {
            isError = true
            errorMessage = if (languageMode == LanguageMode.BANGLA) "ভুল পিন বা পাসওয়ার্ড!" else "Incorrect PIN or Password!"
            triggerShake()
            coroutineScope.launch {
                kotlinx.coroutines.delay(800)
                enteredPin = ""
                isError = false
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Section: Gold emblem & app title
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .shadow(12.dp, CircleShape)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFFFD54F),
                                    Color(0xFFFFB300),
                                    Color(0xFFFFA000)
                                )
                            )
                        )
                        .border(3.dp, Color(0xFFFFF8E1), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "৳",
                        fontSize = 42.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF422300)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Budgeter",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "অ্যাপটি লক করা আছে" else "App is Locked",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            // PIN Dots / Password Input with Shake Animation
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .offset { IntOffset(shakeOffset.value.roundToInt(), 0) }
                    .padding(vertical = 12.dp)
            ) {
                if (!isAlphaPasswordMode) {
                    // PIN Dots
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val maxDots = maxOf(4, enteredPin.length)
                        for (i in 0 until maxDots) {
                            val isFilled = i < enteredPin.length
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isError -> MaterialTheme.colorScheme.error
                                            isFilled -> SolidPrimary
                                            else -> MaterialTheme.colorScheme.surfaceVariant
                                        }
                                    )
                                    .border(
                                        width = 1.5.dp,
                                        color = if (isError) MaterialTheme.colorScheme.error else SolidPrimary.copy(alpha = 0.5f),
                                        shape = CircleShape
                                    )
                            )
                        }
                    }
                } else {
                    // Alphanumeric Password field
                    OutlinedTextField(
                        value = enteredPin,
                        onValueChange = {
                            enteredPin = it
                            isError = false
                        },
                        label = { Text(if (languageMode == LanguageMode.BANGLA) "পাসওয়ার্ড দিন" else "Enter Password") },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { submitPin(enteredPin) }),
                        singleLine = true,
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { submitPin(enteredPin) },
                        modifier = Modifier.fillMaxWidth(0.6f),
                        colors = ButtonDefaults.buttonColors(containerColor = SolidPrimary)
                    ) {
                        Text(if (languageMode == LanguageMode.BANGLA) "আনলক" else "Unlock")
                    }
                }

                if (errorMessage.isNotBlank() && isError) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Keypad or Bottom Controls
            if (!isAlphaPasswordMode) {
                NumericKeypad(
                    onDigitClick = { digit ->
                        if (enteredPin.length < 8) {
                            val newPin = enteredPin + digit
                            enteredPin = newPin
                            isError = false
                            // Auto-check if matches standard 4 or 6 digit
                            if (newPin.length >= 4 && onVerifyPin(newPin)) {
                                onUnlockSuccess()
                            }
                        }
                    },
                    onDeleteClick = {
                        if (enteredPin.isNotEmpty()) {
                            enteredPin = enteredPin.dropLast(1)
                            isError = false
                        }
                    },
                    onBiometricClick = {
                        triggerBiometrics()
                    },
                    showBiometricButton = securityConfig.isBiometricEnabled
                )
            }

            // Footer actions: Forgot PIN & Mode Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { showForgotDialog = true }) {
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "পিন ভুলে গেছেন?" else "Forgot PIN?",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                TextButton(onClick = {
                    isAlphaPasswordMode = !isAlphaPasswordMode
                    enteredPin = ""
                    isError = false
                }) {
                    Text(
                        text = if (isAlphaPasswordMode) {
                            if (languageMode == LanguageMode.BANGLA) "নম্বর কিপ্যাড" else "Numeric Keypad"
                        } else {
                            if (languageMode == LanguageMode.BANGLA) "পাসওয়ার্ড ব্যবহার করুন" else "Use Password"
                        },
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }

    // Security Recovery Dialog
    if (showForgotDialog) {
        var recoveryAnswer by remember { mutableStateOf("") }
        var recoveryError by remember { mutableStateOf("") }
        var isAnswerVerified by remember { mutableStateOf(false) }
        var newPinInput by remember { mutableStateOf("") }
        var confirmPinInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showForgotDialog = false },
            title = {
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "সিকিউরিটি রিকভারি" else "Security Recovery",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (!isAnswerVerified) {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "নিচের সিকিউরিটি প্রশ্নের উত্তর দিন:" else "Answer your security question to reset PIN:",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = securityConfig.securityQuestion,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = recoveryAnswer,
                            onValueChange = {
                                recoveryAnswer = it
                                recoveryError = ""
                            },
                            label = { Text(if (languageMode == LanguageMode.BANGLA) "উত্তর" else "Your Answer") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "নতুন ৪-সংখ্যার পিন দিন:" else "Enter your new 4-digit PIN:",
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newPinInput,
                            onValueChange = { newPinInput = it.take(8) },
                            label = { Text("New PIN") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = confirmPinInput,
                            onValueChange = { confirmPinInput = it.take(8) },
                            label = { Text("Confirm PIN") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (recoveryError.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = recoveryError, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (!isAnswerVerified) {
                            if (securityConfig.securityAnswerHash.isBlank() || onVerifySecurityAnswer(recoveryAnswer)) {
                                isAnswerVerified = true
                                recoveryError = ""
                            } else {
                                recoveryError = if (languageMode == LanguageMode.BANGLA) "ভুল উত্তর! আবার চেষ্টা করুন।" else "Incorrect answer! Please try again."
                            }
                        } else {
                            if (newPinInput.length < 4) {
                                recoveryError = if (languageMode == LanguageMode.BANGLA) "পিন অন্তত ৪ সংখ্যার হতে হবে" else "PIN must be at least 4 digits"
                            } else if (newPinInput != confirmPinInput) {
                                recoveryError = if (languageMode == LanguageMode.BANGLA) "পিন দুটি মিলছে না" else "PINs do not match"
                            } else {
                                onResetPinAfterRecovery(newPinInput)
                                showForgotDialog = false
                                onUnlockSuccess()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SolidPrimary)
                ) {
                    Text(if (!isAnswerVerified) (if (languageMode == LanguageMode.BANGLA) "যাচাই করুন" else "Verify") else (if (languageMode == LanguageMode.BANGLA) "পিন সংরক্ষণ করুন" else "Save & Unlock"))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showForgotDialog = false }) {
                    Text(if (languageMode == LanguageMode.BANGLA) "বাতিল" else "Cancel")
                }
            }
        )
    }
}

@Composable
private fun NumericKeypad(
    onDigitClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onBiometricClick: () -> Unit,
    showBiometricButton: Boolean
) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("bio", "0", "del")
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        for (row in rows) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (item in row) {
                    when (item) {
                        "bio" -> {
                            if (showBiometricButton) {
                                KeypadButton(
                                    onClick = onBiometricClick,
                                    modifier = Modifier.testTag("keypad_bio")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Fingerprint,
                                        contentDescription = "Fingerprint",
                                        tint = SolidPrimary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.size(72.dp))
                            }
                        }
                        "del" -> {
                            KeypadButton(
                                onClick = onDeleteClick,
                                modifier = Modifier.testTag("keypad_del")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                                    contentDescription = "Backspace",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        else -> {
                            KeypadButton(
                                onClick = { onDigitClick(item) },
                                modifier = Modifier.testTag("keypad_$item")
                            ) {
                                Text(
                                    text = item,
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KeypadButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, radius = 36.dp),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
