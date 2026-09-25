package com.example.budgeter.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.budgeter.data.model.*
import com.example.budgeter.ui.theme.EmeraldGreenPrimary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TransactionFilterDialog(
    currentFilter: LedgerFilterState,
    categories: List<Category>,
    accounts: List<Account>,
    availableLabels: List<String>,
    currencySymbol: String = "৳",
    onDismiss: () -> Unit,
    onApplyFilter: (LedgerFilterState) -> Unit
) {
    var selectedTypes by remember { mutableStateOf(currentFilter.selectedTypeFilters) }
    var selectedCategoryIds by remember { mutableStateOf(currentFilter.selectedCategoryIds) }
    var selectedAccountIds by remember { mutableStateOf(currentFilter.selectedAccountIds) }
    var selectedLabels by remember { mutableStateOf(currentFilter.selectedLabels) }
    var selectedStatuses by remember { mutableStateOf(currentFilter.selectedStatuses) }
    var datePreset by remember { mutableStateOf(currentFilter.datePreset) }
    var minAmountText by remember { mutableStateOf(currentFilter.minAmount?.toString() ?: "") }
    var maxAmountText by remember { mutableStateOf(currentFilter.maxAmount?.toString() ?: "") }

    val statusOptions = listOf("COMPLETED", "PENDING", "CLEARED")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("transaction_filter_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Top App Bar in Modal
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = null,
                            tint = EmeraldGreenPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Filter Transactions",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    TextButton(
                        onClick = {
                            selectedTypes = emptySet()
                            selectedCategoryIds = emptySet()
                            selectedAccountIds = emptySet()
                            selectedLabels = emptySet()
                            selectedStatuses = emptySet()
                            datePreset = DatePreset.THIS_MONTH
                            minAmountText = ""
                            maxAmountText = ""
                        },
                        modifier = Modifier.testTag("reset_all_filters_button")
                    ) {
                        Text("Reset All", color = MaterialTheme.colorScheme.error)
                    }
                }

                HorizontalDivider()

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 20.dp)
                ) {
                    // 1. Transaction Type Multi-Select
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Transaction Type",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TransactionType.values().forEach { type ->
                                val isSelected = selectedTypes.contains(type)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedTypes = if (isSelected) {
                                            selectedTypes - type
                                        } else {
                                            selectedTypes + type
                                        }
                                    },
                                    label = { Text(type.name) },
                                    leadingIcon = if (isSelected) {
                                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                    } else null,
                                    modifier = Modifier.testTag("filter_type_${type.name.lowercase()}")
                                )
                            }
                        }
                    }

                    // 2. Date Preset Selection
                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "Time Period",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DatePreset.values().forEach { preset ->
                                val isSelected = datePreset == preset
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { datePreset = preset },
                                    label = {
                                        Text(
                                            when (preset) {
                                                DatePreset.ALL -> "All Time"
                                                DatePreset.TODAY -> "Today"
                                                DatePreset.THIS_WEEK -> "This Week"
                                                DatePreset.THIS_MONTH -> "This Month"
                                                DatePreset.LAST_MONTH -> "Last Month"
                                                DatePreset.THIS_YEAR -> "This Year"
                                                DatePreset.CUSTOM -> "Custom"
                                            }
                                        )
                                    },
                                    leadingIcon = if (isSelected) {
                                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                    } else null
                                )
                            }
                        }
                    }

                    // 3. Amount Range
                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "Amount Range ($currencySymbol)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = minAmountText,
                                onValueChange = { minAmountText = it },
                                label = { Text("Min Amount") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("min_amount_input"),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = maxAmountText,
                                onValueChange = { maxAmountText = it },
                                label = { Text("Max Amount") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("max_amount_input"),
                                singleLine = true
                            )
                        }
                    }

                    // 4. Categories Multi-Select
                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Categories (${selectedCategoryIds.size}/${categories.size})",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Row {
                                TextButton(onClick = { selectedCategoryIds = categories.map { it.id }.toSet() }) {
                                    Text("All", style = MaterialTheme.typography.labelMedium)
                                }
                                TextButton(onClick = { selectedCategoryIds = emptySet() }) {
                                    Text("Clear", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            categories.forEach { cat ->
                                val isSelected = selectedCategoryIds.contains(cat.id)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedCategoryIds = if (isSelected) {
                                            selectedCategoryIds - cat.id
                                        } else {
                                            selectedCategoryIds + cat.id
                                        }
                                    },
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

                    // 5. Accounts Multi-Select
                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Accounts (${selectedAccountIds.size}/${accounts.size})",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Row {
                                TextButton(onClick = { selectedAccountIds = accounts.map { it.id }.toSet() }) {
                                    Text("All", style = MaterialTheme.typography.labelMedium)
                                }
                                TextButton(onClick = { selectedAccountIds = emptySet() }) {
                                    Text("Clear", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            accounts.forEach { acc ->
                                val isSelected = selectedAccountIds.contains(acc.id)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedAccountIds = if (isSelected) {
                                            selectedAccountIds - acc.id
                                        } else {
                                            selectedAccountIds + acc.id
                                        }
                                    },
                                    label = { Text(acc.name) },
                                    leadingIcon = if (isSelected) {
                                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                    } else null
                                )
                            }
                        }
                    }

                    // 6. Status Multi-Select
                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "Status",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            statusOptions.forEach { status ->
                                val isSelected = selectedStatuses.contains(status)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedStatuses = if (isSelected) {
                                            selectedStatuses - status
                                        } else {
                                            selectedStatuses + status
                                        }
                                    },
                                    label = { Text(status) },
                                    leadingIcon = if (isSelected) {
                                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                    } else null
                                )
                            }
                        }
                    }

                    // 7. Labels Multi-Select
                    if (availableLabels.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = "Labels",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                availableLabels.forEach { label ->
                                    val isSelected = selectedLabels.contains(label)
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            selectedLabels = if (isSelected) {
                                                selectedLabels - label
                                            } else {
                                                selectedLabels + label
                                            }
                                        },
                                        label = { Text("#$label") }
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                HorizontalDivider()

                // Bottom Action Buttons
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
                            val minVal = minAmountText.toDoubleOrNull()
                            val maxVal = maxAmountText.toDoubleOrNull()
                            val newFilter = currentFilter.copy(
                                selectedTypeFilters = selectedTypes,
                                selectedCategoryIds = selectedCategoryIds,
                                selectedAccountIds = selectedAccountIds,
                                selectedLabels = selectedLabels,
                                selectedStatuses = selectedStatuses,
                                datePreset = datePreset,
                                minAmount = minVal,
                                maxAmount = maxVal
                            )
                            onApplyFilter(newFilter)
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("apply_filter_button")
                    ) {
                        Text("Apply Filters")
                    }
                }
            }
        }
    }
}
