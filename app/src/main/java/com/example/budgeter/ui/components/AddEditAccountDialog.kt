package com.example.budgeter.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.budgeter.data.model.Account

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditAccountDialog(
    accountToEdit: Account? = null,
    currencySymbol: String = "৳",
    onDismiss: () -> Unit,
    onSave: (Account) -> Unit,
    onDelete: ((Account) -> Unit)? = null
) {
    var name by remember { mutableStateOf(accountToEdit?.name ?: "") }
    var selectedType by remember { mutableStateOf(accountToEdit?.type ?: "BANK") }
    var balanceText by remember {
        mutableStateOf(accountToEdit?.currentBalance?.let { if (it % 1 == 0.0) it.toInt().toString() else it.toString() } ?: "")
    }
    var isDebt by remember { mutableStateOf(accountToEdit?.isDebt ?: false) }
    var debtLimitText by remember {
        mutableStateOf(accountToEdit?.debtLimit?.let { if (it > 0) (if (it % 1 == 0.0) it.toInt().toString() else it.toString()) else "" } ?: "")
    }
    var isError by remember { mutableStateOf(false) }

    val accountTypes = listOf("CASH", "BANK", "MOBILE_MONEY", "CREDIT_CARD", "SAVINGS", "LOAN")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .clip(RoundedCornerShape(24.dp))
                .testTag("add_edit_account_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (accountToEdit == null) "New Account" else "Edit Account",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider()

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = name,
                            onValueChange = {
                                name = it
                                isError = false
                            },
                            label = { Text("Account Name") },
                            placeholder = { Text("e.g. City Bank, bKash, Wallet") },
                            singleLine = true,
                            isError = isError,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("account_name_input")
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Account Type",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            accountTypes.forEach { type ->
                                val isSelected = selectedType == type
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedType = type
                                        if (type == "CREDIT_CARD" || type == "LOAN") {
                                            isDebt = true
                                        }
                                    },
                                    label = { Text(type.replace("_", " ")) }
                                )
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = balanceText,
                            onValueChange = { balanceText = it },
                            label = { Text("Starting / Current Balance ($currencySymbol)") },
                            prefix = { Text("$currencySymbol ", fontWeight = FontWeight.Bold) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("account_balance_input")
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Is Debt / Liability Account",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Switch(
                                checked = isDebt,
                                onCheckedChange = { isDebt = it }
                            )
                        }
                    }

                    if (isDebt) {
                        item {
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = debtLimitText,
                                onValueChange = { debtLimitText = it },
                                label = { Text("Credit / Loan Limit ($currencySymbol)") },
                                prefix = { Text("$currencySymbol ") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    if (accountToEdit != null && onDelete != null) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedButton(
                                onClick = {
                                    onDelete(accountToEdit)
                                    onDismiss()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Delete Account")
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                isError = true
                                return@Button
                            }
                            val balance = balanceText.toDoubleOrNull() ?: 0.0
                            val limit = debtLimitText.toDoubleOrNull() ?: 0.0
                            val account = (accountToEdit ?: Account(
                                name = name,
                                type = selectedType,
                                initialBalance = balance,
                                currentBalance = balance,
                                isDebt = isDebt,
                                debtLimit = limit
                            )).copy(
                                name = name,
                                type = selectedType,
                                currentBalance = balance,
                                isDebt = isDebt,
                                debtLimit = limit
                            )
                            onSave(account)
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_account_button")
                    ) {
                        Text("Save Account")
                    }
                }
            }
        }
    }
}
