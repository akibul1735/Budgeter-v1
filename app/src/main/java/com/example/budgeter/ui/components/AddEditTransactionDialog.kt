package com.example.budgeter.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.budgeter.data.model.Account
import com.example.budgeter.data.model.Category
import com.example.budgeter.data.model.Transaction
import com.example.budgeter.data.model.TransactionType
import com.example.budgeter.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditTransactionDialog(
    transactionToEdit: Transaction? = null,
    initialType: TransactionType = TransactionType.EXPENSE,
    categories: List<Category>,
    accounts: List<Account>,
    currencySymbol: String = "৳",
    onDismiss: () -> Unit,
    onSave: (Transaction) -> Unit,
    onDelete: ((Transaction) -> Unit)? = null
) {
    var type by remember { mutableStateOf(transactionToEdit?.type ?: initialType) }
    var title by remember { mutableStateOf(transactionToEdit?.title ?: "") }
    var amountText by remember {
        mutableStateOf(
            if (transactionToEdit != null) {
                if (transactionToEdit.amount % 1 == 0.0) transactionToEdit.amount.toInt().toString()
                else transactionToEdit.amount.toString()
            } else ""
        )
    }
    var selectedCategoryId by remember {
        mutableStateOf(
            transactionToEdit?.categoryId ?: categories.firstOrNull {
                if (type == TransactionType.EXPENSE) it.isExpense else it.isIncome
            }?.id
        )
    }
    var selectedAccountId by remember {
        mutableStateOf(transactionToEdit?.accountId ?: accounts.firstOrNull()?.id ?: 1L)
    }
    var selectedDestAccountId by remember {
        mutableStateOf(
            transactionToEdit?.destinationAccountId ?: accounts.firstOrNull { it.id != selectedAccountId }?.id
        )
    }
    var note by remember { mutableStateOf(transactionToEdit?.note ?: "") }
    var labels by remember { mutableStateOf(transactionToEdit?.labels ?: "") }
    var status by remember { mutableStateOf(transactionToEdit?.status ?: "COMPLETED") }
    var isError by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("add_edit_transaction_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (transactionToEdit == null) "New Transaction" else "Edit Transaction",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider()

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 20.dp)
                ) {
                    // Type selector
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TransactionType.values().forEach { t ->
                                val isSelected = type == t
                                val btnColor = when (t) {
                                    TransactionType.EXPENSE -> ExpenseRed
                                    TransactionType.INCOME -> IncomeGreen
                                    TransactionType.TRANSFER -> TransferBlue
                                }
                                Button(
                                    onClick = {
                                        type = t
                                        // auto update category selection to relevant type
                                        if (t != TransactionType.TRANSFER) {
                                            val validCat = categories.firstOrNull {
                                                if (t == TransactionType.EXPENSE) it.isExpense else it.isIncome
                                            }
                                            if (validCat != null) selectedCategoryId = validCat.id
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("type_btn_${t.name.lowercase()}"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSelected) btnColor else MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                ) {
                                    Text(
                                        text = t.name,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }
                    }

                    // Amount input
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = amountText,
                            onValueChange = {
                                amountText = it
                                isError = false
                            },
                            label = { Text("Amount ($currencySymbol)") },
                            prefix = { Text("$currencySymbol ", fontWeight = FontWeight.Bold) },
                            isError = isError,
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("transaction_amount_input"),
                            textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        if (isError) {
                            Text(
                                text = "Please enter a valid amount",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                            )
                        }
                    }

                    // Title
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Title / Description") },
                            placeholder = { Text(if (type == TransactionType.TRANSFER) "Transfer to savings" else "Grocery, Dinner, Salary, etc.") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("transaction_title_input")
                        )
                    }

                    // Category (if not transfer)
                    if (type != TransactionType.TRANSFER) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Category",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            val filteredCategories = categories.filter {
                                if (type == TransactionType.EXPENSE) it.isExpense else it.isIncome
                            }
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                filteredCategories.forEach { cat ->
                                    val isSelected = selectedCategoryId == cat.id
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedCategoryId = cat.id },
                                        label = { Text(cat.name) },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = getCategoryIcon(cat.iconName),
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                                tint = parseColor(cat.colorHex)
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Account Selection
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (type == TransactionType.TRANSFER) "Source Account (From)" else "Paid With / Deposited To",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            accounts.forEach { acc ->
                                val isSelected = selectedAccountId == acc.id
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedAccountId = acc.id },
                                    label = { Text("${acc.name} (${formatCurrency(acc.currentBalance, currencySymbol)})") }
                                )
                            }
                        }
                    }

                    // Destination Account for Transfer
                    if (type == TransactionType.TRANSFER) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Destination Account (To)",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                accounts.filter { it.id != selectedAccountId }.forEach { acc ->
                                    val isSelected = selectedDestAccountId == acc.id
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedDestAccountId = acc.id },
                                        label = { Text("${acc.name} (${formatCurrency(acc.currentBalance, currencySymbol)})") }
                                    )
                                }
                            }
                        }
                    }

                    // Labels & Tags
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = labels,
                            onValueChange = { labels = it },
                            label = { Text("Labels / Tags (comma separated)") },
                            placeholder = { Text("e.g. Vacation, Office, Personal") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Notes
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = note,
                            onValueChange = { note = it },
                            label = { Text("Notes (Optional)") },
                            maxLines = 3,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Delete button if editing
                    if (transactionToEdit != null && onDelete != null) {
                        item {
                            Spacer(modifier = Modifier.height(20.dp))
                            OutlinedButton(
                                onClick = {
                                    onDelete(transactionToEdit)
                                    onDismiss()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Delete Transaction")
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                HorizontalDivider()

                // Actions
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
                            val amount = amountText.toDoubleOrNull()
                            if (amount == null || amount <= 0) {
                                isError = true
                                return@Button
                            }
                            val effectiveTitle = if (title.isNotBlank()) title else when (type) {
                                TransactionType.EXPENSE -> "Expense"
                                TransactionType.INCOME -> "Income"
                                TransactionType.TRANSFER -> "Transfer"
                            }
                            val updatedTx = (transactionToEdit ?: Transaction(
                                title = effectiveTitle,
                                amount = amount,
                                type = type,
                                categoryId = if (type == TransactionType.TRANSFER) null else selectedCategoryId,
                                accountId = selectedAccountId,
                                destinationAccountId = if (type == TransactionType.TRANSFER) selectedDestAccountId else null
                            )).copy(
                                title = effectiveTitle,
                                amount = amount,
                                type = type,
                                categoryId = if (type == TransactionType.TRANSFER) null else selectedCategoryId,
                                accountId = selectedAccountId,
                                destinationAccountId = if (type == TransactionType.TRANSFER) selectedDestAccountId else null,
                                note = note,
                                labels = labels,
                                status = status
                            )
                            onSave(updatedTx)
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_transaction_button")
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}
