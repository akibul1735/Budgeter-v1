package com.example.budgeter.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.budgeter.data.model.BudgetFilterState
import com.example.budgeter.data.model.Category
import com.example.budgeter.ui.theme.EmeraldGreenPrimary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BudgetMakerFilterDialog(
    currentFilter: BudgetFilterState,
    categories: List<Category>,
    currencySymbol: String = "৳",
    onDismiss: () -> Unit,
    onApplyFilter: (BudgetFilterState) -> Unit
) {
    var selectedCategoryIds by remember { mutableStateOf(currentFilter.selectedCategoryIds) }
    var onlyWithBudgetOrTarget by remember { mutableStateOf(currentFilter.onlyWithBudgetOrTarget) }
    var onlyWithoutBudgetOrTarget by remember { mutableStateOf(currentFilter.onlyWithoutBudgetOrTarget) }
    var onlyOverspentOrOverallocated by remember { mutableStateOf(currentFilter.onlyOverspentOrOverallocated) }
    var onlyUnderSpentOrUnderallocated by remember { mutableStateOf(currentFilter.onlyUnderSpentOrUnderallocated) }
    var onlyOnTrack by remember { mutableStateOf(currentFilter.onlyOnTrack) }
    var onlyTargetAchieved by remember { mutableStateOf(currentFilter.onlyTargetAchieved) }
    var onlyTargetPending by remember { mutableStateOf(currentFilter.onlyTargetPending) }

    var onlyWithActualActivity by remember { mutableStateOf(currentFilter.onlyWithActualActivity) }
    var onlyZeroActivity by remember { mutableStateOf(currentFilter.onlyZeroActivity) }

    var onlyPositiveBalance by remember { mutableStateOf(currentFilter.onlyPositiveBalance) }
    var onlyZeroBalance by remember { mutableStateOf(currentFilter.onlyZeroBalance) }
    var onlyNegativeBalance by remember { mutableStateOf(currentFilter.onlyNegativeBalance) }
    var onlyOutstandingDebt by remember { mutableStateOf(currentFilter.onlyOutstandingDebt) }
    var onlyClearedDebt by remember { mutableStateOf(currentFilter.onlyClearedDebt) }
    var excludeZeroAmounts by remember { mutableStateOf(currentFilter.excludeZeroAmounts) }

    var minAmountText by remember { mutableStateOf(currentFilter.minAmount?.toString() ?: "") }
    var maxAmountText by remember { mutableStateOf(currentFilter.maxAmount?.toString() ?: "") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("budget_filter_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Modal Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FilterAlt,
                            contentDescription = null,
                            tint = EmeraldGreenPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Budget & Target Filters",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    TextButton(
                        onClick = {
                            selectedCategoryIds = emptySet()
                            onlyWithBudgetOrTarget = false
                            onlyWithoutBudgetOrTarget = false
                            onlyOverspentOrOverallocated = false
                            onlyUnderSpentOrUnderallocated = false
                            onlyOnTrack = false
                            onlyTargetAchieved = false
                            onlyTargetPending = false
                            onlyWithActualActivity = false
                            onlyZeroActivity = false
                            onlyPositiveBalance = false
                            onlyZeroBalance = false
                            onlyNegativeBalance = false
                            onlyOutstandingDebt = false
                            onlyClearedDebt = false
                            excludeZeroAmounts = false
                            minAmountText = ""
                            maxAmountText = ""
                        },
                        modifier = Modifier.testTag("reset_budget_filters_button")
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
                    // 1. Budget & Target Status
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Budget / Target Status",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = onlyWithBudgetOrTarget,
                                onClick = { onlyWithBudgetOrTarget = !onlyWithBudgetOrTarget },
                                label = { Text("Has Budget/Target") }
                            )
                            FilterChip(
                                selected = onlyWithoutBudgetOrTarget,
                                onClick = { onlyWithoutBudgetOrTarget = !onlyWithoutBudgetOrTarget },
                                label = { Text("No Budget/Target") }
                            )
                            FilterChip(
                                selected = onlyOverspentOrOverallocated,
                                onClick = { onlyOverspentOrOverallocated = !onlyOverspentOrOverallocated },
                                label = { Text("Overspent / Overallocated") }
                            )
                            FilterChip(
                                selected = onlyUnderSpentOrUnderallocated,
                                onClick = { onlyUnderSpentOrUnderallocated = !onlyUnderSpentOrUnderallocated },
                                label = { Text("Underspent / Underallocated") }
                            )
                            FilterChip(
                                selected = onlyOnTrack,
                                onClick = { onlyOnTrack = !onlyOnTrack },
                                label = { Text("On Track (<= 100%)") }
                            )
                            FilterChip(
                                selected = onlyTargetAchieved,
                                onClick = { onlyTargetAchieved = !onlyTargetAchieved },
                                label = { Text("Target Achieved") }
                            )
                            FilterChip(
                                selected = onlyTargetPending,
                                onClick = { onlyTargetPending = !onlyTargetPending },
                                label = { Text("Target Pending") }
                            )
                        }
                    }

                    // 2. Realized Activity
                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "Realized Activity",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = onlyWithActualActivity,
                                onClick = { onlyWithActualActivity = !onlyWithActualActivity },
                                label = { Text("With Activity") }
                            )
                            FilterChip(
                                selected = onlyZeroActivity,
                                onClick = { onlyZeroActivity = !onlyZeroActivity },
                                label = { Text("Zero Activity") }
                            )
                        }
                    }

                    // 3. Balance & Debt Conditions
                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "Balance & Debt Conditions",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = onlyPositiveBalance,
                                onClick = { onlyPositiveBalance = !onlyPositiveBalance },
                                label = { Text("Positive Remaining") }
                            )
                            FilterChip(
                                selected = onlyZeroBalance,
                                onClick = { onlyZeroBalance = !onlyZeroBalance },
                                label = { Text("Zero Remaining") }
                            )
                            FilterChip(
                                selected = onlyNegativeBalance,
                                onClick = { onlyNegativeBalance = !onlyNegativeBalance },
                                label = { Text("Negative / Deficit") }
                            )
                            FilterChip(
                                selected = onlyOutstandingDebt,
                                onClick = { onlyOutstandingDebt = !onlyOutstandingDebt },
                                label = { Text("Outstanding Debt") }
                            )
                            FilterChip(
                                selected = onlyClearedDebt,
                                onClick = { onlyClearedDebt = !onlyClearedDebt },
                                label = { Text("Cleared Debt") }
                            )
                            FilterChip(
                                selected = excludeZeroAmounts,
                                onClick = { excludeZeroAmounts = !excludeZeroAmounts },
                                label = { Text("Exclude Zero Amounts") }
                            )
                        }
                    }

                    // 4. Amount Boundaries
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
                                label = { Text("Min Budget/Spent") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("budget_min_amount_input"),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = maxAmountText,
                                onValueChange = { maxAmountText = it },
                                label = { Text("Max Budget/Spent") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("budget_max_amount_input"),
                                singleLine = true
                            )
                        }
                    }

                    // 5. Category Selection
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

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                HorizontalDivider()

                // Action Buttons
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
                                selectedCategoryIds = selectedCategoryIds,
                                onlyWithBudgetOrTarget = onlyWithBudgetOrTarget,
                                onlyWithoutBudgetOrTarget = onlyWithoutBudgetOrTarget,
                                onlyOverspentOrOverallocated = onlyOverspentOrOverallocated,
                                onlyUnderSpentOrUnderallocated = onlyUnderSpentOrUnderallocated,
                                onlyOnTrack = onlyOnTrack,
                                onlyTargetAchieved = onlyTargetAchieved,
                                onlyTargetPending = onlyTargetPending,
                                onlyWithActualActivity = onlyWithActualActivity,
                                onlyZeroActivity = onlyZeroActivity,
                                onlyPositiveBalance = onlyPositiveBalance,
                                onlyZeroBalance = onlyZeroBalance,
                                onlyNegativeBalance = onlyNegativeBalance,
                                onlyOutstandingDebt = onlyOutstandingDebt,
                                onlyClearedDebt = onlyClearedDebt,
                                excludeZeroAmounts = excludeZeroAmounts,
                                minAmount = minVal,
                                maxAmount = maxVal
                            )
                            onApplyFilter(newFilter)
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("apply_budget_filter_button")
                    ) {
                        Text("Apply Filters")
                    }
                }
            }
        }
    }
}
