package com.example.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Account
import com.example.data.model.AccountType
import com.example.data.model.LanguageMode
import com.example.util.LanguageHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditAccountDialog(
    parentAccounts: List<Account>,
    languageMode: LanguageMode,
    existingAccount: Account? = null,
    defaultParentId: Long? = null,
    onDismiss: () -> Unit,
    onSave: (Account) -> Unit,
    onDelete: ((Account) -> Unit)? = null
) {
    var nameEn by remember { mutableStateOf(existingAccount?.nameEn ?: "") }
    var nameBn by remember { mutableStateOf(existingAccount?.nameBn ?: "") }
    var accountType by remember { mutableStateOf(existingAccount?.type ?: AccountType.ASSET) }
    var parentId by remember { mutableStateOf(existingAccount?.parentId ?: defaultParentId) }
    var initialBalanceText by remember {
        mutableStateOf(existingAccount?.initialBalance?.toString() ?: "0.0")
    }
    var isSubAccount by remember { mutableStateOf(parentId != null) }
    var parentDropdownExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth().testTag("add_account_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (existingAccount == null) {
                            if (isSubAccount) LanguageHelper.getString("add_sub_account", languageMode)
                            else LanguageHelper.getString("add_account", languageMode)
                        } else {
                            LanguageHelper.getString("edit", languageMode)
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Account Type Chips (Asset, Liability, Equity)
                Text(
                    text = "Type",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val types = listOf(
                        AccountType.ASSET to LanguageHelper.getString("assets", languageMode),
                        AccountType.LIABILITY to LanguageHelper.getString("liabilities", languageMode),
                        AccountType.EQUITY to LanguageHelper.getString("equity", languageMode)
                    )
                    types.forEach { (type, label) ->
                        FilterChip(
                            selected = accountType == type,
                            onClick = { accountType = type },
                            label = { Text(label) },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Toggle Main vs Sub-Account
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = !isSubAccount,
                        onClick = {
                            isSubAccount = false
                            parentId = null
                        },
                        label = { Text(LanguageHelper.getString("parent_account", languageMode)) },
                        shape = RoundedCornerShape(8.dp)
                    )
                    FilterChip(
                        selected = isSubAccount,
                        onClick = {
                            isSubAccount = true
                            if (parentId == null && parentAccounts.isNotEmpty()) {
                                parentId = parentAccounts.firstOrNull { it.type == accountType }?.id ?: parentAccounts.first().id
                            }
                        },
                        label = { Text(LanguageHelper.getString("sub_accounts", languageMode)) },
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                // Parent Account Dropdown (if sub-account)
                if (isSubAccount) {
                    Spacer(modifier = Modifier.height(12.dp))
                    val filteredParents = parentAccounts.filter { it.type == accountType }
                    val selectedParent = filteredParents.firstOrNull { it.id == parentId }

                    ExposedDropdownMenuBox(
                        expanded = parentDropdownExpanded,
                        onExpandedChange = { parentDropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedParent?.localizedName(languageMode) ?: LanguageHelper.getString("select_account", languageMode),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(LanguageHelper.getString("parent_account", languageMode)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = parentDropdownExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = parentDropdownExpanded,
                            onDismissRequest = { parentDropdownExpanded = false }
                        ) {
                            filteredParents.forEach { parent ->
                                DropdownMenuItem(
                                    text = { Text(parent.localizedName(languageMode)) },
                                    onClick = {
                                        parentId = parent.id
                                        parentDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Name (English)
                OutlinedTextField(
                    value = nameEn,
                    onValueChange = { nameEn = it },
                    label = { Text(LanguageHelper.getString("name_en", languageMode)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Name (Bangla)
                OutlinedTextField(
                    value = nameBn,
                    onValueChange = { nameBn = it },
                    label = { Text(LanguageHelper.getString("name_bn", languageMode)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Opening Balance
                OutlinedTextField(
                    value = initialBalanceText,
                    onValueChange = { initialBalanceText = it },
                    label = { Text(LanguageHelper.getString("initial_balance", languageMode)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (existingAccount != null && onDelete != null && !existingAccount.isSystem) {
                        Button(
                            onClick = {
                                onDelete(existingAccount)
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(0.7f)
                        ) {
                            Text(LanguageHelper.getString("delete", languageMode))
                        }
                    }

                    Button(
                        onClick = {
                            val parsedBal = initialBalanceText.toDoubleOrNull() ?: 0.0
                            val account = Account(
                                id = existingAccount?.id ?: 0,
                                nameEn = nameEn.ifBlank { nameBn },
                                nameBn = nameBn.ifBlank { nameEn },
                                type = accountType,
                                parentId = if (isSubAccount) parentId else null,
                                initialBalance = parsedBal,
                                iconName = when (accountType) {
                                    AccountType.ASSET -> if (isSubAccount) "Wallet" else "AccountBalance"
                                    AccountType.LIABILITY -> "CreditCard"
                                    AccountType.EQUITY -> "AccountBalanceWallet"
                                    else -> "AccountBalance"
                                },
                                colorHex = when (accountType) {
                                    AccountType.ASSET -> "#10B981"
                                    AccountType.LIABILITY -> "#EF4444"
                                    AccountType.EQUITY -> "#8B5CF6"
                                    else -> "#1E56A0"
                                }
                            )
                            onSave(account)
                            onDismiss()
                        },
                        enabled = nameEn.isNotBlank() || nameBn.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).testTag("save_account_btn")
                    ) {
                        Text(LanguageHelper.getString("save", languageMode), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
