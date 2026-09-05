package com.example.ui.dialogs

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.data.model.LanguageMode
import com.example.ui.theme.SolidPrimary
import com.example.util.BiometricHelper
import com.example.util.SecurityConfig

/**
 * Universal Security Action Authentication Dialog supporting PIN/Password and Fingerprint
 * for protected actions: Group Deletion, Multi-Select Batch Changes & Delete, Empty Trash, Backup Restore, etc.
 */
@Composable
fun SecurityAuthDialog(
    title: String,
    message: String,
    confirmButtonText: String = "",
    isDestructive: Boolean = false,
    requiresAuth: Boolean,
    securityConfig: SecurityConfig,
    languageMode: LanguageMode,
    onVerifyPin: (String) -> Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val effectiveRequiresAuth = requiresAuth && (securityConfig.hasPin || securityConfig.isBiometricEnabled)

    var inputPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf("") }

    fun triggerBiometric() {
        if (securityConfig.isBiometricEnabled && context is FragmentActivity) {
            BiometricHelper.showBiometricPrompt(
                activity = context,
                title = title,
                subtitle = message,
                negativeButtonText = if (languageMode == LanguageMode.BANGLA) "পাসওয়ার্ড ব্যবহার করুন" else "Use Password / PIN",
                onSuccess = {
                    onConfirm()
                },
                onError = { _, err ->
                    pinError = err
                }
            )
        }
    }

    LaunchedEffect(Unit) {
        if (effectiveRequiresAuth && securityConfig.isBiometricEnabled) {
            triggerBiometric()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (effectiveRequiresAuth) SolidPrimary.copy(alpha = 0.12f)
                        else if (isDestructive) MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
                        else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (effectiveRequiresAuth) Icons.Default.Lock else if (isDestructive) Icons.Default.Warning else Icons.Default.Lock,
                    contentDescription = null,
                    tint = if (effectiveRequiresAuth) SolidPrimary else if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = message,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (effectiveRequiresAuth) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "নিশ্চিত করতে পাসওয়ার্ড বা পিন দিন:" else "Enter Password or PIN to confirm:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = inputPin,
                        onValueChange = {
                            inputPin = it
                            pinError = ""
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(if (languageMode == LanguageMode.BANGLA) "পাসওয়ার্ড / পিন লিখুন" else "Enter Password / PIN")
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (onVerifyPin(inputPin)) {
                                    onConfirm()
                                } else {
                                    pinError = if (languageMode == LanguageMode.BANGLA) "ভুল পিন বা পাসওয়ার্ড!" else "Incorrect PIN or Password!"
                                }
                            }
                        ),
                        isError = pinError.isNotBlank(),
                        supportingText = {
                            if (pinError.isNotBlank()) {
                                Text(pinError, color = MaterialTheme.colorScheme.error)
                            }
                        },
                        singleLine = true,
                        trailingIcon = {
                            if (securityConfig.isBiometricEnabled && context is FragmentActivity) {
                                IconButton(onClick = { triggerBiometric() }) {
                                    Icon(
                                        imageVector = Icons.Default.Fingerprint,
                                        contentDescription = "Fingerprint Auth",
                                        tint = SolidPrimary
                                    )
                                }
                            }
                        }
                    )

                    if (securityConfig.isBiometricEnabled && context is FragmentActivity) {
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedButton(
                            onClick = { triggerBiometric() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = null,
                                tint = SolidPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "ফিঙ্গারপ্রিন্ট দিয়ে আনলক করুন" else "Authenticate with Fingerprint",
                                color = SolidPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            val resolvedConfirmText = if (confirmButtonText.isNotBlank()) {
                confirmButtonText
            } else if (isDestructive) {
                if (languageMode == LanguageMode.BANGLA) "মুছে ফেলুন" else "Delete"
            } else {
                if (languageMode == LanguageMode.BANGLA) "নিশ্চিত করুন" else "Confirm"
            }

            Button(
                onClick = {
                    if (effectiveRequiresAuth) {
                        if (onVerifyPin(inputPin)) {
                            onConfirm()
                        } else {
                            pinError = if (languageMode == LanguageMode.BANGLA) "ভুল পিন বা পাসওয়ার্ড!" else "Incorrect PIN or Password!"
                        }
                    } else {
                        onConfirm()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDestructive) MaterialTheme.colorScheme.error else SolidPrimary
                )
            ) {
                Text(
                    text = resolvedConfirmText,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (languageMode == LanguageMode.BANGLA) "বাতিল" else "Cancel")
            }
        }
    )
}
