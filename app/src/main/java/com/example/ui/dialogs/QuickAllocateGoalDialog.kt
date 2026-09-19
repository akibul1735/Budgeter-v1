package com.example.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Account
import com.example.data.model.AccountType
import com.example.data.model.LanguageMode
import com.example.data.model.SavingsGoalWithDetails
import com.example.data.repository.AccountWithBalance
import com.example.util.IconHelper
import com.example.util.LanguageHelper
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAllocateGoalDialog(
    goalWithDetails: SavingsGoalWithDetails,
    accountsWithBalances: List<AccountWithBalance>,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onSaveAllocation: (accountId: Long, amount: Double) -> Unit
) {
    // Flat list of ALL active accounts
    val allAvailableAccounts = remember(accountsWithBalances) {
        val list = mutableListOf<Pair<Account, Double>>()
        accountsWithBalances.forEach { parent ->
            if (parent.account.isActive) {
                if (parent.subAccounts.isEmpty()) {
                    list.add(parent.account to parent.currentBalance)
                } else {
                    parent.subAccounts.forEach { sub ->
                        if (sub.account.isActive) {
                            list.add(sub.account to sub.currentBalance)
                        }
                    }
                }
            }
        }
        list
    }

    var selectedAccountId by remember {
        mutableStateOf(
            goalWithDetails.allocations.firstOrNull()?.account?.id
                ?: allAvailableAccounts.firstOrNull()?.first?.id
                ?: 0L
        )
    }

    val currentAllocForSelected = remember(selectedAccountId, goalWithDetails) {
        goalWithDetails.allocations.firstOrNull { it.account.id == selectedAccountId }?.allocation?.allocatedAmount ?: 0.0
    }

    var amountStr by remember(selectedAccountId) {
        mutableStateOf(if (currentAllocForSelected > 0) String.format(Locale.US, "%.0f", currentAllocForSelected) else "")
    }

    var showAccountPickerDialog by remember { mutableStateOf(false) }

    val selectedAccountPair = allAvailableAccounts.firstOrNull { it.first.id == selectedAccountId }
    val selectedAccBalance = selectedAccountPair?.second ?: 0.0

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("quick_allocate_goal_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
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
                        Icon(
                            Icons.Default.Savings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = LanguageHelper.getString("quick_allocate", languageMode),
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = LanguageHelper.getLocalizedName(goalWithDetails.goal.name, goalWithDetails.goal.nameBn, languageMode),
                                fontSize = 12.5.sp,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Account Selection Button / Field (Opens Searchable Picker)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAccountPickerDialog = true }
                        .testTag("quick_allocate_account_picker_trigger")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = selectedAccountPair?.first?.let { IconHelper.getIconByName(it.iconName) } ?: Icons.Default.AccountBalance,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = LanguageHelper.getString("select_account", languageMode),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.outline,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = selectedAccountPair?.first?.let {
                                        LanguageHelper.getLocalizedName(it.nameEn, it.nameBn, languageMode)
                                    } ?: (if (languageMode == LanguageMode.BANGLA) "হিসাব নির্বাচন করুন" else "Select Account"),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search account",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Available Balance preview
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = LanguageHelper.getString("free_balance", languageMode),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = LanguageHelper.formatCurrency(selectedAccBalance, languageMode),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Allocation amount input
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text(LanguageHelper.getString("amount", languageMode)) },
                    placeholder = { Text("0") },
                    leadingIcon = {
                        Text(
                            LanguageHelper.activeCurrencyConfig.activeSymbol,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("allocate_amount_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                // Quick buttons (+1000, +5000, Max)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(1000.0, 5000.0, 10000.0).forEach { addAmt ->
                        FilledTonalButton(
                            onClick = {
                                val current = amountStr.toDoubleOrNull() ?: 0.0
                                amountStr = String.format(Locale.US, "%.0f", current + addAmt)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            Text(
                                text = "+${LanguageHelper.formatNumber(addAmt, languageMode, false)}",
                                fontSize = 11.sp
                            )
                        }
                    }
                    OutlinedButton(
                        onClick = {
                            if (selectedAccBalance > 0) {
                                amountStr = String.format(Locale.US, "%.0f", selectedAccBalance)
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Max", fontSize = 11.sp)
                    }
                }

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(LanguageHelper.getString("cancel", languageMode))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amt = amountStr.toDoubleOrNull() ?: 0.0
                            if (selectedAccountId > 0) {
                                onSaveAllocation(selectedAccountId, amt)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("save_quick_allocation_btn")
                    ) {
                        Text(LanguageHelper.getString("save", languageMode))
                    }
                }
            }
        }
    }

    if (showAccountPickerDialog) {
        SearchableAccountPickerDialog(
            accountsWithBalances = allAvailableAccounts,
            languageMode = languageMode,
            title = if (languageMode == LanguageMode.BANGLA) "হিসাব খুঁজুন ও নির্বাচন করুন" else "Search & Select Account",
            onAccountSelected = { acc, _ ->
                selectedAccountId = acc.id
                showAccountPickerDialog = false
            },
            onDismiss = { showAccountPickerDialog = false }
        )
    }
}
