package com.example.ui.dialogs

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Account
import com.example.data.model.Category
import com.example.data.model.CategoryType
import com.example.data.model.LanguageMode
import com.example.data.model.RecurrencePeriod
import com.example.data.model.RecurringBill
import com.example.data.model.TransactionType
import com.example.ui.components.DatePickerModal
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidPrimary
import com.example.ui.theme.SolidTransfer
import com.example.util.DateUtils
import com.example.util.LanguageHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditRecurringBillDialog(
    accounts: List<Account>,
    categories: List<Category>,
    languageMode: LanguageMode,
    existingBill: RecurringBill? = null,
    onDismiss: () -> Unit,
    onSave: (RecurringBill) -> Unit,
    onDelete: ((RecurringBill) -> Unit)? = null
) {
    var title by remember { mutableStateOf(existingBill?.title ?: "") }
    var selectedType by remember { mutableStateOf(existingBill?.type ?: TransactionType.EXPENSE) }
    var amountText by remember { mutableStateOf(existingBill?.amount?.let { if (it % 1.0 == 0.0) it.toLong().toString() else it.toString() } ?: "") }
    var recurrence by remember { mutableStateOf(existingBill?.recurrencePeriod ?: RecurrencePeriod.MONTHLY) }
    var dueDateEpochMs by remember { mutableLongStateOf(existingBill?.nextDueDateEpochMs ?: System.currentTimeMillis()) }
    var payeeOrPayer by remember { mutableStateOf(existingBill?.payeeOrPayer ?: "") }
    var note by remember { mutableStateOf(existingBill?.note ?: "") }
    var isAutoRecord by remember { mutableStateOf(existingBill?.isAutoRecord ?: false) }

    var selectedDebitAccountId by remember { mutableStateOf(existingBill?.debitAccountId) }
    var selectedCreditAccountId by remember { mutableStateOf(existingBill?.creditAccountId) }
    var selectedCategoryId by remember { mutableStateOf(existingBill?.categoryId) }

    var showDatePicker by remember { mutableStateOf(false) }
    var accountDropdownExpanded by remember { mutableStateOf(false) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var recurrenceDropdownExpanded by remember { mutableStateOf(false) }

    val relevantCategories = categories.filter {
        if (selectedType == TransactionType.EXPENSE) it.type == CategoryType.EXPENSE else it.type == CategoryType.INCOME
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.EventRepeat,
                        contentDescription = null,
                        tint = SolidPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (existingBill == null) "New Recurring Bill" else "Edit Recurring Bill",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                }
                if (existingBill != null && onDelete != null) {
                    IconButton(onClick = { onDelete(existingBill); onDismiss() }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = SolidExpense)
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Type selector chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(TransactionType.EXPENSE, TransactionType.INCOME).forEach { type ->
                        val isSelected = selectedType == type
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedType = type
                                selectedCategoryId = null
                            },
                            label = {
                                Text(
                                    when (type) {
                                        TransactionType.EXPENSE -> "Expense Bill"
                                        TransactionType.INCOME -> "Recurring Income"
                                        else -> ""
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (type == TransactionType.EXPENSE) SolidExpense else SolidIncome,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Bill / Income Title (e.g. WiFi Bill, Salary)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Amount
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount / পরিমাণ (৳)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Recurrence period
                ExposedDropdownMenuBox(
                    expanded = recurrenceDropdownExpanded,
                    onExpandedChange = { recurrenceDropdownExpanded = !recurrenceDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = recurrence.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Repeat Frequency") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = recurrenceDropdownExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = recurrenceDropdownExpanded,
                        onDismissRequest = { recurrenceDropdownExpanded = false }
                    ) {
                        RecurrencePeriod.values().forEach { period ->
                            DropdownMenuItem(
                                text = { Text(period.name) },
                                onClick = {
                                    recurrence = period
                                    recurrenceDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Next Due Date
                Card(
                    onClick = { showDatePicker = true },
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = SolidPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Next Due Date", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            Text(DateUtils.formatDate(dueDateEpochMs, languageMode), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }
                }

                // Account selection
                ExposedDropdownMenuBox(
                    expanded = accountDropdownExpanded,
                    onExpandedChange = { accountDropdownExpanded = !accountDropdownExpanded }
                ) {
                    val acc = accounts.find { it.id == if (selectedType == TransactionType.EXPENSE) selectedCreditAccountId else selectedDebitAccountId }
                    OutlinedTextField(
                        value = acc?.let { LanguageHelper.getLocalizedName(it.nameEn, it.nameBn, languageMode) } ?: "Select Payment Account",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(if (selectedType == TransactionType.EXPENSE) "Paid From (Asset/Liability)" else "Deposit To (Asset)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountDropdownExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = accountDropdownExpanded,
                        onDismissRequest = { accountDropdownExpanded = false }
                    ) {
                        accounts.forEach { accountItem ->
                            DropdownMenuItem(
                                text = {
                                    Text(LanguageHelper.getLocalizedName(accountItem.nameEn, accountItem.nameBn, languageMode))
                                },
                                onClick = {
                                    if (selectedType == TransactionType.EXPENSE) {
                                        selectedCreditAccountId = accountItem.id
                                    } else {
                                        selectedDebitAccountId = accountItem.id
                                    }
                                    accountDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Category selection
                ExposedDropdownMenuBox(
                    expanded = categoryDropdownExpanded,
                    onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded }
                ) {
                    val cat = relevantCategories.find { it.id == selectedCategoryId }
                    OutlinedTextField(
                        value = cat?.let { LanguageHelper.getLocalizedName(it.nameEn, it.nameBn, languageMode) } ?: "Select Category",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false }
                    ) {
                        relevantCategories.forEach { categoryItem ->
                            DropdownMenuItem(
                                text = {
                                    Text(LanguageHelper.getLocalizedName(categoryItem.nameEn, categoryItem.nameBn, languageMode))
                                },
                                onClick = {
                                    selectedCategoryId = categoryItem.id
                                    categoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Payee / Note
                OutlinedTextField(
                    value = payeeOrPayer,
                    onValueChange = { payeeOrPayer = it },
                    label = { Text("Payee / Biller Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Notes (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Auto-record switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Auto-Record on Due Date", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                        Text("Automatically post transaction when due", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                    }
                    Switch(
                        checked = isAutoRecord,
                        onCheckedChange = { isAutoRecord = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = SolidPrimary)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (title.isNotBlank() && amt > 0) {
                        val bill = RecurringBill(
                            id = existingBill?.id ?: 0L,
                            title = title.trim(),
                            type = selectedType,
                            amount = amt,
                            recurrencePeriod = recurrence,
                            nextDueDateEpochMs = dueDateEpochMs,
                            debitAccountId = selectedDebitAccountId,
                            creditAccountId = selectedCreditAccountId,
                            categoryId = selectedCategoryId,
                            payeeOrPayer = payeeOrPayer.trim(),
                            note = note.trim(),
                            isAutoRecord = isAutoRecord,
                            isActive = true
                        )
                        onSave(bill)
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SolidPrimary)
            ) {
                Text("Save Bill")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )

    if (showDatePicker) {
        DatePickerModal(
            selectedDateEpochMs = dueDateEpochMs,
            languageMode = languageMode,
            onDateSelected = { selected ->
                dueDateEpochMs = selected
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}
