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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.budgeter.data.model.Budget
import com.example.budgeter.data.model.Category

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditBudgetDialog(
    budgetToEdit: Budget? = null,
    defaultCategoryId: Long? = null,
    categories: List<Category>,
    currentMonthYear: String,
    currencySymbol: String = "৳",
    onDismiss: () -> Unit,
    onSave: (Budget) -> Unit,
    onDelete: ((Budget) -> Unit)? = null
) {
    var selectedCategoryId by remember {
        mutableStateOf(budgetToEdit?.categoryId ?: defaultCategoryId ?: categories.firstOrNull { it.isExpense }?.id ?: 1L)
    }
    var allocatedText by remember {
        mutableStateOf(budgetToEdit?.allocatedAmount?.let { if (it % 1 == 0.0) it.toInt().toString() else it.toString() } ?: "")
    }
    var targetText by remember {
        mutableStateOf(budgetToEdit?.targetAmount?.let { if (it > 0) (if (it % 1 == 0.0) it.toInt().toString() else it.toString()) else "" } ?: "")
    }
    var notes by remember { mutableStateOf(budgetToEdit?.notes ?: "") }
    var isError by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .clip(RoundedCornerShape(24.dp))
                .testTag("add_edit_budget_dialog"),
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
                        text = if (budgetToEdit == null) "Allocate Budget" else "Edit Budget",
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
                    // Category Selection
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Category",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        val expenseCats = categories.filter { it.isExpense }
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            expenseCats.forEach { cat ->
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

                    // Allocated Amount
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = allocatedText,
                            onValueChange = {
                                allocatedText = it
                                isError = false
                            },
                            label = { Text("Allocated Amount for Month ($currencySymbol)") },
                            prefix = { Text("$currencySymbol ", fontWeight = FontWeight.Bold) },
                            isError = isError,
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("budget_allocated_input"),
                            textStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    // Target Amount
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = targetText,
                            onValueChange = { targetText = it },
                            label = { Text("Target Goal (Optional) ($currencySymbol)") },
                            placeholder = { Text("e.g. 15000") },
                            prefix = { Text("$currencySymbol ") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("budget_target_input")
                        )
                    }

                    // Notes
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Budget Note (Optional)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Delete button if editing
                    if (budgetToEdit != null && onDelete != null) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedButton(
                                onClick = {
                                    onDelete(budgetToEdit)
                                    onDismiss()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Remove Budget Allocation")
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
                            val allocated = allocatedText.toDoubleOrNull()
                            if (allocated == null || allocated < 0) {
                                isError = true
                                return@Button
                            }
                            val target = targetText.toDoubleOrNull() ?: 0.0
                            val budget = (budgetToEdit ?: Budget(
                                categoryId = selectedCategoryId,
                                allocatedAmount = allocated,
                                targetAmount = target,
                                monthYear = currentMonthYear
                            )).copy(
                                categoryId = selectedCategoryId,
                                allocatedAmount = allocated,
                                targetAmount = target,
                                monthYear = currentMonthYear,
                                notes = notes
                            )
                            onSave(budget)
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_budget_button")
                    ) {
                        Text("Save Budget")
                    }
                }
            }
        }
    }
}
