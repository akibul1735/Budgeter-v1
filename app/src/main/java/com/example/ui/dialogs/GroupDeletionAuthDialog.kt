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
import androidx.compose.material.icons.filled.Delete
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
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidPrimary
import com.example.util.BiometricHelper
import com.example.util.SecurityConfig

/**
 * Handles group deletion with mandatory authentication (PIN/Password or Fingerprint)
 * if security is configured, or standard confirmation if security is not enabled.
 */
@Composable
fun GroupDeletionAuthDialog(
    groupName: String,
    subItemCount: Int = 0,
    securityConfig: SecurityConfig,
    languageMode: LanguageMode,
    onVerifyPin: (String) -> Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val requiresAuth = securityConfig.requireAuthForGroupDeletion && (securityConfig.hasPin || securityConfig.isBiometricEnabled)

    var inputPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf("") }

    fun triggerBiometric() {
        if (securityConfig.isBiometricEnabled && context is FragmentActivity) {
            BiometricHelper.showBiometricPrompt(
                activity = context,
                title = if (languageMode == LanguageMode.BANGLA) "গ্রুপ ডিলিট নিশ্চিতকরণ" else "Confirm Group Deletion",
                subtitle = if (languageMode == LanguageMode.BANGLA) "ফিঙ্গারপ্রিন্ট দিয়ে যাচাই করুন: $groupName" else "Verify fingerprint to delete: $groupName",
                negativeButtonText = if (languageMode == LanguageMode.BANGLA) "পিন ব্যবহার করুন" else "Use PIN",
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
        if (requiresAuth && securityConfig.isBiometricEnabled) {
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
                    .background(if (requiresAuth) SolidPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.error.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (requiresAuth) Icons.Default.Lock else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (requiresAuth) SolidPrimary else MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        title = {
            Text(
                text = if (requiresAuth) {
                    if (languageMode == LanguageMode.BANGLA) "গ্রুপ ডিলিটে পাসওয়ার্ড প্রয়োজন" else "Authentication Required"
                } else {
                    if (languageMode == LanguageMode.BANGLA) "গ্রুপ ডিলিট নিশ্চিত করুন" else "Delete Group"
                },
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                val groupText = if (languageMode == LanguageMode.BANGLA) {
                    "আপনি কি নিশ্চিত যে আপনি '$groupName' গ্রুপটি${if (subItemCount > 0) " এবং এর ভেতরের $subItemCount টি আইটেম" else ""} ডিলিট করতে চান?"
                } else {
                    "Are you sure you want to delete the group '$groupName'${if (subItemCount > 0) " and its $subItemCount item(s)" else ""}?"
                }

                Text(
                    text = groupText,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (requiresAuth) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "নিশ্চিত করতে পিন বা পাসওয়ার্ড দিন:" else "Enter PIN or Password to confirm:",
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
                        label = { Text(if (languageMode == LanguageMode.BANGLA) "পিন / পাসওয়ার্ড" else "PIN / Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            if (onVerifyPin(inputPin)) {
                                onConfirm()
                            } else {
                                pinError = if (languageMode == LanguageMode.BANGLA) "ভুল পিন!" else "Incorrect PIN!"
                            }
                        }),
                        trailingIcon = {
                            if (securityConfig.isBiometricEnabled) {
                                IconButton(onClick = { triggerBiometric() }) {
                                    Icon(
                                        imageVector = Icons.Default.Fingerprint,
                                        contentDescription = "Fingerprint",
                                        tint = SolidPrimary
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (pinError.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = pinError,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (requiresAuth) {
                        if (onVerifyPin(inputPin)) {
                            onConfirm()
                        } else {
                            pinError = if (languageMode == LanguageMode.BANGLA) "ভুল পিন!" else "Incorrect PIN!"
                        }
                    } else {
                        onConfirm()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = SolidExpense
                )
            ) {
                Text(if (languageMode == LanguageMode.BANGLA) "ডিলিট করুন" else "Delete Group")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(if (languageMode == LanguageMode.BANGLA) "বাতিল" else "Cancel")
            }
        }
    )
}

/**
 * Standard confirmation dialog for non-group single items (Transactions, Single sub-items, Bills, Labels).
 * Does NOT require PIN or biometric password as per user specification.
 */
@Composable
fun SimpleDeleteConfirmationDialog(
    itemName: String,
    itemTypeLabel: String = "",
    languageMode: LanguageMode,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        title = {
            Text(
                text = if (languageMode == LanguageMode.BANGLA) {
                    if (itemTypeLabel.isNotBlank()) "$itemTypeLabel ডিলিট করুন" else "ডিলিট নিশ্চিতকরণ"
                } else {
                    if (itemTypeLabel.isNotBlank()) "Delete $itemTypeLabel" else "Confirm Deletion"
                },
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
        },
        text = {
            val msg = if (languageMode == LanguageMode.BANGLA) {
                "আপনি কি নিশ্চিত যে আপনি '$itemName' ডিলিট করতে চান?"
            } else {
                "Are you sure you want to delete '$itemName'?"
            }
            Text(
                text = msg,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = SolidExpense)
            ) {
                Text(if (languageMode == LanguageMode.BANGLA) "ডিলিট" else "Delete")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(if (languageMode == LanguageMode.BANGLA) "বাতিল" else "Cancel")
            }
        }
    )
}
