package com.example.ui.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.data.model.Account
import com.example.data.model.Category
import com.example.data.model.LanguageMode
import com.example.data.model.TransactionType
import com.example.data.model.TransactionWithDetails
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidPrimary
import com.example.util.BiometricHelper
import com.example.util.DateUtils
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
    onConfirmDelete: (deleteTransactions: Boolean, targetAccountId: Long?, transactionTargetMap: Map<Long, Long>?) -> Unit,
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

    // Filter related transactions
    val relatedTransactions = remember(affectedAccountIds, transactions) {
        transactions.filter { item ->
            val tx = item.transaction
            (tx.debitAccountId != null && tx.debitAccountId in affectedAccountIds) ||
                    (tx.creditAccountId != null && tx.creditAccountId in affectedAccountIds)
        }
    }
    val relatedTxCount = relatedTransactions.size

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

    // Per-transaction target account overrides (transactionId -> targetAccountId)
    val customTxAccountMap = remember { mutableStateMapOf<Long, Long>() }
    var showTxListExpanded by remember { mutableStateOf(false) }

    var targetMenuExpanded by remember { mutableStateOf(false) }
    var inputPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf("") }

    fun buildFinalTargetMap(): Map<Long, Long>? {
        if (selectedAction != DeleteTransactionAction.MOVE_TO_ANOTHER) return null
        val defaultTarget = selectedTargetAccountId ?: candidateTargetAccounts.firstOrNull()?.id ?: return null
        return relatedTransactions.associate { it.transaction.id to (customTxAccountMap[it.transaction.id] ?: defaultTarget) }
    }

    fun executeDelete() {
        val deleteTx = selectedAction == DeleteTransactionAction.DELETE_TRANSACTIONS
        val targetId = if (selectedAction == DeleteTransactionAction.MOVE_TO_ANOTHER) (selectedTargetAccountId ?: candidateTargetAccounts.firstOrNull()?.id) else null
        val targetMap = buildFinalTargetMap()
        onConfirmDelete(deleteTx, targetId, targetMap)
    }

    fun triggerBiometric() {
        if (securityConfig.isBiometricEnabled && context is FragmentActivity) {
            BiometricHelper.showBiometricPrompt(
                activity = context,
                title = if (languageMode == LanguageMode.BANGLA) "অ্যাকাউন্ট ডিলিট নিশ্চিতকরণ" else "Confirm Account Deletion",
                subtitle = if (languageMode == LanguageMode.BANGLA) "ফিঙ্গারপ্রিন্ট দিয়ে যাচাই করুন: ${account.localizedName(languageMode)}" else "Verify fingerprint to delete: ${account.localizedName(languageMode)}",
                negativeButtonText = if (languageMode == LanguageMode.BANGLA) "পিন ব্যবহার করুন" else "Use PIN",
                onSuccess = {
                    executeDelete()
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

                // Related Transactions Info Card & List
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
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
                                        "সম্পর্কিত লেনদেন: ${LanguageHelper.toBanglaDigits(relatedTxCount.toString())} টি"
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

                            if (relatedTxCount > 0) {
                                TextButton(
                                    onClick = { showTxListExpanded = !showTxListExpanded },
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text(
                                        text = if (showTxListExpanded) {
                                            if (languageMode == LanguageMode.BANGLA) "লুকান" else "Hide"
                                        } else {
                                            if (languageMode == LanguageMode.BANGLA) "দেখুন ও ভাগ করুন" else "View / Assign"
                                        },
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SolidPrimary
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Icon(
                                        imageVector = if (showTxListExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = null,
                                        tint = SolidPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        // Expandable list of related transactions with individual target selectors
                        AnimatedVisibility(
                            visible = showTxListExpanded && relatedTxCount > 0,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                            ) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                Spacer(modifier = Modifier.height(6.dp))

                                if (selectedAction == DeleteTransactionAction.MOVE_TO_ANOTHER && candidateTargetAccounts.isNotEmpty()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (languageMode == LanguageMode.BANGLA) "প্রতিটি লেনদেনের জন্য আলাদা অ্যাকাউন্ট নির্বাচন করুন:" else "Assign target account per transaction:",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (customTxAccountMap.isNotEmpty()) {
                                            TextButton(
                                                onClick = { customTxAccountMap.clear() },
                                                modifier = Modifier.height(26.dp)
                                            ) {
                                                Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text(
                                                    text = if (languageMode == LanguageMode.BANGLA) "রিসেট" else "Reset",
                                                    fontSize = 10.5.sp
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                }

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 220.dp)
                                        .verticalScroll(rememberScrollState()),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    relatedTransactions.forEach { txItem ->
                                        val tx = txItem.transaction
                                        val assignedTargetId = customTxAccountMap[tx.id] ?: selectedTargetAccountId ?: candidateTargetAccounts.firstOrNull()?.id
                                        val assignedAccount = candidateTargetAccounts.firstOrNull { it.id == assignedTargetId }
                                        val isCustomized = customTxAccountMap.containsKey(tx.id)
                                        var itemMenuExpanded by remember { mutableStateOf(false) }

                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.surface,
                                            border = androidx.compose.foundation.BorderStroke(
                                                1.dp,
                                                if (isCustomized) SolidPrimary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                                            ),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(8.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = tx.note.ifBlank {
                                                                if (languageMode == LanguageMode.BANGLA) "বিবরণ নেই" else "No Note"
                                                            },
                                                            fontSize = 12.5.sp,
                                                            fontWeight = FontWeight.SemiBold,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                        Text(
                                                            text = DateUtils.formatDate(tx.dateEpochMs, languageMode),
                                                            fontSize = 11.sp,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                    val amountColor = when (tx.type) {
                                                        TransactionType.INCOME -> SolidIncome
                                                        TransactionType.EXPENSE -> SolidExpense
                                                        TransactionType.TRANSFER -> SolidPrimary
                                                    }
                                                    Text(
                                                        text = "${if (tx.type == TransactionType.EXPENSE) "-" else if (tx.type == TransactionType.INCOME) "+" else ""}${String.format("%.2f", tx.amount)}",
                                                        fontSize = 12.5.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = amountColor
                                                    )
                                                }

                                                if (selectedAction == DeleteTransactionAction.MOVE_TO_ANOTHER && candidateTargetAccounts.isNotEmpty()) {
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Box {
                                                        AssistChip(
                                                            onClick = { itemMenuExpanded = true },
                                                            label = {
                                                                Text(
                                                                    text = "${if (languageMode == LanguageMode.BANGLA) "স্থানান্তর ➔ " else "Move ➔ "}${assignedAccount?.localizedName(languageMode) ?: ""}${if (isCustomized) " (কাস্টম)" else ""}",
                                                                    fontSize = 11.sp,
                                                                    fontWeight = if (isCustomized) FontWeight.Bold else FontWeight.Normal
                                                                )
                                                            },
                                                            trailingIcon = {
                                                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(14.dp))
                                                            },
                                                            colors = AssistChipDefaults.assistChipColors(
                                                                containerColor = if (isCustomized) SolidPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                                                labelColor = if (isCustomized) SolidPrimary else MaterialTheme.colorScheme.onSurface
                                                            ),
                                                            shape = RoundedCornerShape(6.dp),
                                                            modifier = Modifier.height(28.dp)
                                                        )

                                                        DropdownMenu(
                                                            expanded = itemMenuExpanded,
                                                            onDismissRequest = { itemMenuExpanded = false }
                                                        ) {
                                                            candidateTargetAccounts.forEach { candidate ->
                                                                DropdownMenuItem(
                                                                    text = {
                                                                        Text(
                                                                            text = candidate.localizedName(languageMode),
                                                                            fontSize = 12.sp,
                                                                            fontWeight = if (candidate.id == assignedTargetId) FontWeight.Bold else FontWeight.Normal
                                                                        )
                                                                    },
                                                                    leadingIcon = {
                                                                        Icon(
                                                                            imageVector = IconHelper.getIconByName(candidate.iconName),
                                                                            contentDescription = null,
                                                                            modifier = Modifier.size(16.dp)
                                                                        )
                                                                    },
                                                                    onClick = {
                                                                        customTxAccountMap[tx.id] = candidate.id
                                                                        itemMenuExpanded = false
                                                                    }
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
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
                                            text = if (languageMode == LanguageMode.BANGLA) "ডিফল্ট বা নির্দিষ্ট অ্যাকাউন্টে লেনদেন স্থানান্তর করুন" else "Reassign transactions to default or custom accounts",
                                            fontSize = 11.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                if (selectedAction == DeleteTransactionAction.MOVE_TO_ANOTHER) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "মূল / ডিফল্ট টার্গেট অ্যাকাউন্ট:" else "Default Target Account:",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
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
                                executeDelete()
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
                            executeDelete()
                        } else {
                            pinError = if (languageMode == LanguageMode.BANGLA) "ভুল পিন!" else "Incorrect PIN!"
                        }
                    } else {
                        executeDelete()
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
    onConfirmDelete: (deleteTransactions: Boolean, targetCategoryId: Long?, transactionTargetMap: Map<Long, Long>?) -> Unit,
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

    // Filter related transactions
    val relatedTransactions = remember(affectedCategoryIds, transactions) {
        transactions.filter { item ->
            val tx = item.transaction
            (tx.categoryId != null && tx.categoryId in affectedCategoryIds) ||
                    (tx.subCategoryId != null && tx.subCategoryId in affectedCategoryIds)
        }
    }
    val relatedTxCount = relatedTransactions.size

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

    // Per-transaction target category overrides (transactionId -> targetCategoryId)
    val customTxCategoryMap = remember { mutableStateMapOf<Long, Long>() }
    var showTxListExpanded by remember { mutableStateOf(false) }

    var targetMenuExpanded by remember { mutableStateOf(false) }
    var inputPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf("") }

    fun buildFinalTargetMap(): Map<Long, Long>? {
        if (selectedAction != DeleteTransactionAction.MOVE_TO_ANOTHER) return null
        val defaultTarget = selectedTargetCategoryId ?: candidateTargetCategories.firstOrNull()?.id ?: return null
        return relatedTransactions.associate { it.transaction.id to (customTxCategoryMap[it.transaction.id] ?: defaultTarget) }
    }

    fun executeDelete() {
        val deleteTx = selectedAction == DeleteTransactionAction.DELETE_TRANSACTIONS
        val targetId = if (selectedAction == DeleteTransactionAction.MOVE_TO_ANOTHER) (selectedTargetCategoryId ?: candidateTargetCategories.firstOrNull()?.id) else null
        val targetMap = buildFinalTargetMap()
        onConfirmDelete(deleteTx, targetId, targetMap)
    }

    fun triggerBiometric() {
        if (securityConfig.isBiometricEnabled && context is FragmentActivity) {
            BiometricHelper.showBiometricPrompt(
                activity = context,
                title = if (languageMode == LanguageMode.BANGLA) "ক্যাটাগরি ডিলিট নিশ্চিতকরণ" else "Confirm Category Deletion",
                subtitle = if (languageMode == LanguageMode.BANGLA) "ফিঙ্গারপ্রিন্ট দিয়ে যাচাই করুন: ${category.localizedName(languageMode)}" else "Verify fingerprint to delete: ${category.localizedName(languageMode)}",
                negativeButtonText = if (languageMode == LanguageMode.BANGLA) "পিন ব্যবহার করুন" else "Use PIN",
                onSuccess = {
                    executeDelete()
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

                // Related Transactions Info Card & List
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
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
                                        "সম্পর্কিত লেনদেন: ${LanguageHelper.toBanglaDigits(relatedTxCount.toString())} টি"
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

                            if (relatedTxCount > 0) {
                                TextButton(
                                    onClick = { showTxListExpanded = !showTxListExpanded },
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text(
                                        text = if (showTxListExpanded) {
                                            if (languageMode == LanguageMode.BANGLA) "লুকান" else "Hide"
                                        } else {
                                            if (languageMode == LanguageMode.BANGLA) "দেখুন ও ভাগ করুন" else "View / Assign"
                                        },
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SolidPrimary
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Icon(
                                        imageVector = if (showTxListExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = null,
                                        tint = SolidPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        // Expandable list of related transactions with individual target category selectors
                        AnimatedVisibility(
                            visible = showTxListExpanded && relatedTxCount > 0,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                            ) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                Spacer(modifier = Modifier.height(6.dp))

                                if (selectedAction == DeleteTransactionAction.MOVE_TO_ANOTHER && candidateTargetCategories.isNotEmpty()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (languageMode == LanguageMode.BANGLA) "প্রতিটি লেনদেনের জন্য আলাদা ক্যাটাগরি বেছে নিন:" else "Assign target category per transaction:",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (customTxCategoryMap.isNotEmpty()) {
                                            TextButton(
                                                onClick = { customTxCategoryMap.clear() },
                                                modifier = Modifier.height(26.dp)
                                            ) {
                                                Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text(
                                                    text = if (languageMode == LanguageMode.BANGLA) "রিসেট" else "Reset",
                                                    fontSize = 10.5.sp
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                }

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 220.dp)
                                        .verticalScroll(rememberScrollState()),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    relatedTransactions.forEach { txItem ->
                                        val tx = txItem.transaction
                                        val assignedTargetId = customTxCategoryMap[tx.id] ?: selectedTargetCategoryId ?: candidateTargetCategories.firstOrNull()?.id
                                        val assignedCategory = candidateTargetCategories.firstOrNull { it.id == assignedTargetId }
                                        val isCustomized = customTxCategoryMap.containsKey(tx.id)
                                        var itemMenuExpanded by remember { mutableStateOf(false) }

                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.surface,
                                            border = androidx.compose.foundation.BorderStroke(
                                                1.dp,
                                                if (isCustomized) SolidPrimary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                                            ),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(8.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = tx.note.ifBlank {
                                                                if (languageMode == LanguageMode.BANGLA) "বিবরণ নেই" else "No Note"
                                                            },
                                                            fontSize = 12.5.sp,
                                                            fontWeight = FontWeight.SemiBold,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                        Text(
                                                            text = DateUtils.formatDate(tx.dateEpochMs, languageMode),
                                                            fontSize = 11.sp,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                    val amountColor = when (tx.type) {
                                                        TransactionType.INCOME -> SolidIncome
                                                        TransactionType.EXPENSE -> SolidExpense
                                                        TransactionType.TRANSFER -> SolidPrimary
                                                    }
                                                    Text(
                                                        text = "${if (tx.type == TransactionType.EXPENSE) "-" else if (tx.type == TransactionType.INCOME) "+" else ""}${String.format("%.2f", tx.amount)}",
                                                        fontSize = 12.5.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = amountColor
                                                    )
                                                }

                                                if (selectedAction == DeleteTransactionAction.MOVE_TO_ANOTHER && candidateTargetCategories.isNotEmpty()) {
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Box {
                                                        AssistChip(
                                                            onClick = { itemMenuExpanded = true },
                                                            label = {
                                                                Text(
                                                                    text = "${if (languageMode == LanguageMode.BANGLA) "স্থানান্তর ➔ " else "Move ➔ "}${assignedCategory?.localizedName(languageMode) ?: ""}${if (isCustomized) " (কাস্টম)" else ""}",
                                                                    fontSize = 11.sp,
                                                                    fontWeight = if (isCustomized) FontWeight.Bold else FontWeight.Normal
                                                                )
                                                            },
                                                            trailingIcon = {
                                                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(14.dp))
                                                            },
                                                            colors = AssistChipDefaults.assistChipColors(
                                                                containerColor = if (isCustomized) SolidPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                                                labelColor = if (isCustomized) SolidPrimary else MaterialTheme.colorScheme.onSurface
                                                            ),
                                                            shape = RoundedCornerShape(6.dp),
                                                            modifier = Modifier.height(28.dp)
                                                        )

                                                        DropdownMenu(
                                                            expanded = itemMenuExpanded,
                                                            onDismissRequest = { itemMenuExpanded = false }
                                                        ) {
                                                            candidateTargetCategories.forEach { candidate ->
                                                                DropdownMenuItem(
                                                                    text = {
                                                                        Text(
                                                                            text = candidate.localizedName(languageMode),
                                                                            fontSize = 12.sp,
                                                                            fontWeight = if (candidate.id == assignedTargetId) FontWeight.Bold else FontWeight.Normal
                                                                        )
                                                                    },
                                                                    leadingIcon = {
                                                                        Icon(
                                                                            imageVector = IconHelper.getIconByName(candidate.iconName),
                                                                            contentDescription = null,
                                                                            modifier = Modifier.size(16.dp)
                                                                        )
                                                                    },
                                                                    onClick = {
                                                                        customTxCategoryMap[tx.id] = candidate.id
                                                                        itemMenuExpanded = false
                                                                    }
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
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
                                            text = if (languageMode == LanguageMode.BANGLA) "ডিফল্ট বা নির্দিষ্ট ক্যাটাগরিতে লেনদেন স্থানান্তর করুন" else "Reassign transactions to default or custom categories",
                                            fontSize = 11.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                if (selectedAction == DeleteTransactionAction.MOVE_TO_ANOTHER) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "মূল / ডিফল্ট টার্গেট ক্যাটাগরি:" else "Default Target Category:",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
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
                                executeDelete()
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
                            executeDelete()
                        } else {
                            pinError = if (languageMode == LanguageMode.BANGLA) "ভুল পিন!" else "Incorrect PIN!"
                        }
                    } else {
                        executeDelete()
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
