package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DriveFileMove
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.data.model.Account
import com.example.data.model.Category
import com.example.data.model.LanguageMode
import com.example.data.model.TransactionWithDetails
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidPrimary
import com.example.util.BiometricHelper
import com.example.util.IconHelper
import com.example.util.LanguageHelper
import com.example.util.SecurityConfig

private enum class DeleteTransactionAction {
    DELETE_TRANSACTIONS,
    MOVE_TO_ANOTHER
}

@Composable
fun AccountDeleteConfirmDialog(
    account: Account,
    allAccounts: List<Account>,
    transactions: List<TransactionWithDetails>,
    securityConfig: SecurityConfig = SecurityConfig(),
    languageMode: LanguageMode,
    onVerifyPin: ((String) -> Boolean)? = null,
    onConfirmDelete: (deleteTransactions: Boolean, targetAccountId: Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val isGroup = account.parentId == null
    val context = LocalContext.current
    val requiresAuth = isGroup && securityConfig.requireAuthForGroupDeletion && (securityConfig.hasPin || securityConfig.isBiometricEnabled)

    // Calculate affected account IDs
    val affectedAccountIds = remember(account, allAccounts) {
        if (isGroup) {
            val children = allAccounts.filter { it.parentId == account.id }
            (setOf(account.id) + children.map { it.id }).toSet()
        } else {
            setOf(account.id)
        }
    }

    // Count related transactions
    val relatedTxCount = remember(affectedAccountIds, transactions) {
        transactions.count { item ->
            val tx = item.transaction
            (tx.debitAccountId != null && tx.debitAccountId in affectedAccountIds) ||
                    (tx.creditAccountId != null && tx.creditAccountId in affectedAccountIds)
        }
    }

    // Target accounts available for moving (exclude self and children)
    val candidateTargetAccounts = remember(allAccounts, affectedAccountIds) {
        allAccounts.filter { it.id !in affectedAccountIds && it.parentId != null }
            .ifEmpty { allAccounts.filter { it.id !in affectedAccountIds } }
    }

    var selectedAction by remember {
        mutableStateOf(
            if (candidateTargetAccounts.isNotEmpty() && relatedTxCount > 0) {
                DeleteTransactionAction.MOVE_TO_ANOTHER
            } else {
                DeleteTransactionAction.DELETE_TRANSACTIONS
            }
        )
    }

    var selectedTargetAccountId by remember {
        mutableStateOf<Long?>(candidateTargetAccounts.firstOrNull()?.id)
    }

    var targetMenuExpanded by remember { mutableStateOf(false) }

    var inputPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf("") }

    fun triggerBiometric() {
        if (securityConfig.isBiometricEnabled && context is FragmentActivity) {
            BiometricHelper.showBiometricPrompt(
                activity = context,
                title = if (languageMode == LanguageMode.BANGLA) "অ্যাকাউন্ট ডিলিট নিশ্চিতকরণ" else "Confirm Account Deletion",
                subtitle = if (languageMode == LanguageMode.BANGLA) "ফিঙ্গারপ্রিন্ট দিয়ে যাচাই করুন: ${account.localizedName(languageMode)}" else "Verify fingerprint to delete: ${account.localizedName(languageMode)}",
                negativeButtonText = if (languageMode == LanguageMode.BANGLA) "পিন ব্যবহার করুন" else "Use PIN",
                onSuccess = {
                    val deleteTx = selectedAction == DeleteTransactionAction.DELETE_TRANSACTIONS
                    val targetId = if (selectedAction == DeleteTransactionAction.MOVE_TO_ANOTHER) selectedTargetAccountId else null
                    onConfirmDelete(deleteTx, targetId)
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

    val selectedTargetAccount = candidateTargetAccounts.firstOrNull { it.id == selectedTargetAccountId }

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
                text = if (languageMode == LanguageMode.BANGLA) {
                    if (isGroup) "অ্যাকাউন্ট গ্রুপ ডিলিট" else "অ্যাকাউন্ট ডিলিট"
                } else {
                    if (isGroup) "Delete Account Group" else "Delete Account"
                },
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) {
                        "আপনি কি নিশ্চিত যে '${account.localizedName(languageMode)}' ${if (isGroup) "গ্রুপটি ও এর সকল অ্যাকাউন্ট" else "অ্যাকাউন্টটি"} ডিলিট করতে চান?"
                    } else {
                        "Are you sure you want to delete '${account.localizedName(languageMode)}'${if (isGroup) " and all its sub-accounts" else ""}?"
                    },
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Related Transactions Info Card
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        val txText = if (languageMode == LanguageMode.BANGLA) {
                            if (relatedTxCount > 0) {
                                "সম্পর্কিত লেনদেন: ${LanguageHelper.toBanglaDigits(relatedTxCount.toString())} টি পাওয়া গেছে"
                            } else {
                                "কোনো সম্পর্কিত লেনদেন নেই"
                            }
                        } else {
                            if (relatedTxCount > 0) {
                                "Related transactions: $relatedTxCount found"
                            } else {
                                "No related transactions found"
                            }
                        }
                        Text(
                            text = txText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (relatedTxCount > 0) {
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "সম্পর্কিত লেনদেনের জন্য ব্যবস্থা বেছে নিন:" else "Choose action for related transactions:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Option 1: Move to another account
                    if (candidateTargetAccounts.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (selectedAction == DeleteTransactionAction.MOVE_TO_ANOTHER) SolidPrimary.copy(alpha = 0.08f) else Color.Transparent,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    1.dp,
                                    if (selectedAction == DeleteTransactionAction.MOVE_TO_ANOTHER) SolidPrimary else MaterialTheme.colorScheme.outlineVariant,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedAction = DeleteTransactionAction.MOVE_TO_ANOTHER }
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    RadioButton(
                                        selected = selectedAction == DeleteTransactionAction.MOVE_TO_ANOTHER,
                                        onClick = { selectedAction = DeleteTransactionAction.MOVE_TO_ANOTHER },
                                        colors = RadioButtonDefaults.colors(selectedColor = SolidPrimary)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (languageMode == LanguageMode.BANGLA) "অন্য অ্যাকাউন্টে স্থানান্তর করুন" else "Move to another account",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = if (languageMode == LanguageMode.BANGLA) "সকল লেনদেন অন্য অ্যাকাউন্টে যুক্ত হবে" else "Reassign all transactions to target account",
                                            fontSize = 11.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                if (selectedAction == DeleteTransactionAction.MOVE_TO_ANOTHER) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.surface,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .border(1.dp, SolidPrimary.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                                .clickable { targetMenuExpanded = true }
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.AutoMirrored.Filled.DriveFileMove,
                                                        contentDescription = null,
                                                        tint = SolidPrimary,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = selectedTargetAccount?.localizedName(languageMode)
                                                            ?: if (languageMode == LanguageMode.BANGLA) "অ্যাকাউন্ট নির্বাচন করুন" else "Select Target Account",
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }
                                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                            }
                                        }

                                        DropdownMenu(
                                            expanded = targetMenuExpanded,
                                            onDismissRequest = { targetMenuExpanded = false }
                                        ) {
                                            candidateTargetAccounts.forEach { candidate ->
                                                DropdownMenuItem(
                                                    text = {
                                                        Text(
                                                            text = candidate.localizedName(languageMode),
                                                            fontSize = 13.sp
                                                        )
                                                    },
                                                    leadingIcon = {
                                                        Icon(
                                                            imageVector = IconHelper.getIconByName(candidate.iconName),
                                                            contentDescription = null,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                    },
                                                    onClick = {
                                                        selectedTargetAccountId = candidate.id
                                                        targetMenuExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Option 2: Delete related transactions
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (selectedAction == DeleteTransactionAction.DELETE_TRANSACTIONS) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f) else Color.Transparent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.dp,
                                if (selectedAction == DeleteTransactionAction.DELETE_TRANSACTIONS) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { selectedAction = DeleteTransactionAction.DELETE_TRANSACTIONS }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp)
                        ) {
                            RadioButton(
                                selected = selectedAction == DeleteTransactionAction.DELETE_TRANSACTIONS,
                                onClick = { selectedAction = DeleteTransactionAction.DELETE_TRANSACTIONS },
                                colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.error)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "সম্পর্কিত লেনদেনসহ ডিলিট করুন" else "Delete related transactions",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "সকল সম্পর্কিত লেনদেন স্থায়ীভাবে মুছে যাবে" else "All associated transactions will be permanently deleted",
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Authentication for groups if required
                if (requiresAuth) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "নিশ্চিত করতে পিন বা পাসওয়ার্ড দিন:" else "Enter PIN or Password to confirm:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
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
                            if (onVerifyPin?.invoke(inputPin) == true) {
                                val deleteTx = selectedAction == DeleteTransactionAction.DELETE_TRANSACTIONS
                                val targetId = if (selectedAction == DeleteTransactionAction.MOVE_TO_ANOTHER) selectedTargetAccountId else null
                                onConfirmDelete(deleteTx, targetId)
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
            val isMoveDisabled = selectedAction == DeleteTransactionAction.MOVE_TO_ANOTHER && selectedTargetAccountId == null
            Button(
                onClick = {
                    if (requiresAuth) {
                        if (onVerifyPin?.invoke(inputPin) == true) {
                            val deleteTx = selectedAction == DeleteTransactionAction.DELETE_TRANSACTIONS
                            val targetId = if (selectedAction == DeleteTransactionAction.MOVE_TO_ANOTHER) selectedTargetAccountId else null
                            onConfirmDelete(deleteTx, targetId)
                        } else {
                            pinError = if (languageMode == LanguageMode.BANGLA) "ভুল পিন!" else "Incorrect PIN!"
                        }
                    } else {
                        val deleteTx = selectedAction == DeleteTransactionAction.DELETE_TRANSACTIONS
                        val targetId = if (selectedAction == DeleteTransactionAction.MOVE_TO_ANOTHER) selectedTargetAccountId else null
                        onConfirmDelete(deleteTx, targetId)
                    }
                },
                enabled = !isMoveDisabled,
                colors = ButtonDefaults.buttonColors(containerColor = SolidExpense)
            ) {
                Text(if (languageMode == LanguageMode.BANGLA) "ডিলিট করুন" else "Confirm Delete")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(if (languageMode == LanguageMode.BANGLA) "বাতিল" else "Cancel")
            }
        }
    )
}

@Composable
fun CategoryDeleteConfirmDialog(
    category: Category,
    allCategories: List<Category>,
    transactions: List<TransactionWithDetails>,
    securityConfig: SecurityConfig = SecurityConfig(),
    languageMode: LanguageMode,
    onVerifyPin: ((String) -> Boolean)? = null,
    onConfirmDelete: (deleteTransactions: Boolean, targetCategoryId: Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val isGroup = category.parentId == null
    val context = LocalContext.current
    val requiresAuth = isGroup && securityConfig.requireAuthForGroupDeletion && (securityConfig.hasPin || securityConfig.isBiometricEnabled)

    // Calculate affected category IDs
    val affectedCategoryIds = remember(category, allCategories) {
        if (isGroup) {
            val children = allCategories.filter { it.parentId == category.id }
            (setOf(category.id) + children.map { it.id }).toSet()
        } else {
            setOf(category.id)
        }
    }

    // Count related transactions
    val relatedTxCount = remember(affectedCategoryIds, transactions) {
        transactions.count { item ->
            val tx = item.transaction
            (tx.categoryId != null && tx.categoryId in affectedCategoryIds) ||
                    (tx.subCategoryId != null && tx.subCategoryId in affectedCategoryIds)
        }
    }

    // Target categories available for moving (same type EXPENSE/INCOME, exclude self and children)
    val candidateTargetCategories = remember(allCategories, affectedCategoryIds, category.type) {
        allCategories.filter { it.type == category.type && it.id !in affectedCategoryIds && it.parentId != null }
            .ifEmpty { allCategories.filter { it.type == category.type && it.id !in affectedCategoryIds } }
    }

    var selectedAction by remember {
        mutableStateOf(
            if (candidateTargetCategories.isNotEmpty() && relatedTxCount > 0) {
                DeleteTransactionAction.MOVE_TO_ANOTHER
            } else {
                DeleteTransactionAction.DELETE_TRANSACTIONS
            }
        )
    }

    var selectedTargetCategoryId by remember {
        mutableStateOf<Long?>(candidateTargetCategories.firstOrNull()?.id)
    }

    var targetMenuExpanded by remember { mutableStateOf(false) }

    var inputPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf("") }

    fun triggerBiometric() {
        if (securityConfig.isBiometricEnabled && context is FragmentActivity) {
            BiometricHelper.showBiometricPrompt(
                activity = context,
                title = if (languageMode == LanguageMode.BANGLA) "ক্যাটাগরি ডিলিট নিশ্চিতকরণ" else "Confirm Category Deletion",
                subtitle = if (languageMode == LanguageMode.BANGLA) "ফিঙ্গারপ্রিন্ট দিয়ে যাচাই করুন: ${category.localizedName(languageMode)}" else "Verify fingerprint to delete: ${category.localizedName(languageMode)}",
                negativeButtonText = if (languageMode == LanguageMode.BANGLA) "পিন ব্যবহার করুন" else "Use PIN",
                onSuccess = {
                    val deleteTx = selectedAction == DeleteTransactionAction.DELETE_TRANSACTIONS
                    val targetId = if (selectedAction == DeleteTransactionAction.MOVE_TO_ANOTHER) selectedTargetCategoryId else null
                    onConfirmDelete(deleteTx, targetId)
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

    val selectedTargetCategory = candidateTargetCategories.firstOrNull { it.id == selectedTargetCategoryId }

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
                text = if (languageMode == LanguageMode.BANGLA) {
                    if (isGroup) "ক্যাটাগরি গ্রুপ ডিলিট" else "ক্যাটাগরি ডিলিট"
                } else {
                    if (isGroup) "Delete Category Group" else "Delete Category"
                },
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) {
                        "আপনি কি নিশ্চিত যে '${category.localizedName(languageMode)}' ${if (isGroup) "গ্রুপটি ও এর সকল ক্যাটাগরি" else "ক্যাটাগরি"} ডিলিট করতে চান?"
                    } else {
                        "Are you sure you want to delete '${category.localizedName(languageMode)}'${if (isGroup) " and all its sub-categories" else ""}?"
                    },
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Related Transactions Info Card
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        val txText = if (languageMode == LanguageMode.BANGLA) {
                            if (relatedTxCount > 0) {
                                "সম্পর্কিত লেনদেন: ${LanguageHelper.toBanglaDigits(relatedTxCount.toString())} টি পাওয়া গেছে"
                            } else {
                                "কোনো সম্পর্কিত লেনদেন নেই"
                            }
                        } else {
                            if (relatedTxCount > 0) {
                                "Related transactions: $relatedTxCount found"
                            } else {
                                "No related transactions found"
                            }
                        }
                        Text(
                            text = txText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (relatedTxCount > 0) {
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "সম্পর্কিত লেনদেনের জন্য ব্যবস্থা বেছে নিন:" else "Choose action for related transactions:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Option 1: Move to another category
                    if (candidateTargetCategories.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (selectedAction == DeleteTransactionAction.MOVE_TO_ANOTHER) SolidPrimary.copy(alpha = 0.08f) else Color.Transparent,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    1.dp,
                                    if (selectedAction == DeleteTransactionAction.MOVE_TO_ANOTHER) SolidPrimary else MaterialTheme.colorScheme.outlineVariant,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedAction = DeleteTransactionAction.MOVE_TO_ANOTHER }
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    RadioButton(
                                        selected = selectedAction == DeleteTransactionAction.MOVE_TO_ANOTHER,
                                        onClick = { selectedAction = DeleteTransactionAction.MOVE_TO_ANOTHER },
                                        colors = RadioButtonDefaults.colors(selectedColor = SolidPrimary)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (languageMode == LanguageMode.BANGLA) "অন্য ক্যাটাগরিতে স্থানান্তর করুন" else "Move to another category",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = if (languageMode == LanguageMode.BANGLA) "সকল লেনদেন নতুন ক্যাটাগরিতে যুক্ত হবে" else "Reassign all transactions to target category",
                                            fontSize = 11.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                if (selectedAction == DeleteTransactionAction.MOVE_TO_ANOTHER) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.surface,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .border(1.dp, SolidPrimary.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                                .clickable { targetMenuExpanded = true }
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.AutoMirrored.Filled.DriveFileMove,
                                                        contentDescription = null,
                                                        tint = SolidPrimary,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = selectedTargetCategory?.localizedName(languageMode)
                                                            ?: if (languageMode == LanguageMode.BANGLA) "ক্যাটাগরি নির্বাচন করুন" else "Select Target Category",
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }
                                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                            }
                                        }

                                        DropdownMenu(
                                            expanded = targetMenuExpanded,
                                            onDismissRequest = { targetMenuExpanded = false }
                                        ) {
                                            candidateTargetCategories.forEach { candidate ->
                                                DropdownMenuItem(
                                                    text = {
                                                        Text(
                                                            text = candidate.localizedName(languageMode),
                                                            fontSize = 13.sp
                                                        )
                                                    },
                                                    leadingIcon = {
                                                        Icon(
                                                            imageVector = IconHelper.getIconByName(candidate.iconName),
                                                            contentDescription = null,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                    },
                                                    onClick = {
                                                        selectedTargetCategoryId = candidate.id
                                                        targetMenuExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Option 2: Delete related transactions
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (selectedAction == DeleteTransactionAction.DELETE_TRANSACTIONS) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f) else Color.Transparent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.dp,
                                if (selectedAction == DeleteTransactionAction.DELETE_TRANSACTIONS) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { selectedAction = DeleteTransactionAction.DELETE_TRANSACTIONS }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp)
                        ) {
                            RadioButton(
                                selected = selectedAction == DeleteTransactionAction.DELETE_TRANSACTIONS,
                                onClick = { selectedAction = DeleteTransactionAction.DELETE_TRANSACTIONS },
                                colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.error)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "সম্পর্কিত লেনদেনসহ ডিলিট করুন" else "Delete related transactions",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "সকল সম্পর্কিত লেনদেন স্থায়ীভাবে মুছে যাবে" else "All associated transactions will be permanently deleted",
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Authentication for groups if required
                if (requiresAuth) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "নিশ্চিত করতে পিন বা পাসওয়ার্ড দিন:" else "Enter PIN or Password to confirm:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
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
                            if (onVerifyPin?.invoke(inputPin) == true) {
                                val deleteTx = selectedAction == DeleteTransactionAction.DELETE_TRANSACTIONS
                                val targetId = if (selectedAction == DeleteTransactionAction.MOVE_TO_ANOTHER) selectedTargetCategoryId else null
                                onConfirmDelete(deleteTx, targetId)
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
            val isMoveDisabled = selectedAction == DeleteTransactionAction.MOVE_TO_ANOTHER && selectedTargetCategoryId == null
            Button(
                onClick = {
                    if (requiresAuth) {
                        if (onVerifyPin?.invoke(inputPin) == true) {
                            val deleteTx = selectedAction == DeleteTransactionAction.DELETE_TRANSACTIONS
                            val targetId = if (selectedAction == DeleteTransactionAction.MOVE_TO_ANOTHER) selectedTargetCategoryId else null
                            onConfirmDelete(deleteTx, targetId)
                        } else {
                            pinError = if (languageMode == LanguageMode.BANGLA) "ভুল পিন!" else "Incorrect PIN!"
                        }
                    } else {
                        val deleteTx = selectedAction == DeleteTransactionAction.DELETE_TRANSACTIONS
                        val targetId = if (selectedAction == DeleteTransactionAction.MOVE_TO_ANOTHER) selectedTargetCategoryId else null
                        onConfirmDelete(deleteTx, targetId)
                    }
                },
                enabled = !isMoveDisabled,
                colors = ButtonDefaults.buttonColors(containerColor = SolidExpense)
            ) {
                Text(if (languageMode == LanguageMode.BANGLA) "ডিলিট করুন" else "Confirm Delete")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(if (languageMode == LanguageMode.BANGLA) "বাতিল" else "Cancel")
            }
        }
    )
}
