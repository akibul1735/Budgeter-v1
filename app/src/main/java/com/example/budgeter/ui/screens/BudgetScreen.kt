package com.example.budgeter.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.budgeter.data.model.Budget
import com.example.budgeter.data.model.BudgetFilterState
import com.example.budgeter.data.model.Category
import com.example.budgeter.ui.components.*
import com.example.budgeter.ui.theme.*
import com.example.budgeter.viewmodel.BudgetEnvelopeUiModel
import com.example.budgeter.viewmodel.BudgeterViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    viewModel: BudgeterViewModel,
    onOpenAddBudget: (Category?) -> Unit,
    onEditBudget: (Budget, Category) -> Unit
) {
    val budgetEnvelopes by viewModel.budgetEnvelopes.collectAsStateWithLifecycle()
    val allCategories by viewModel.allCategories.collectAsStateWithLifecycle()
    val filterState by viewModel.budgetFilterState.collectAsStateWithLifecycle()
    val selectedMonthYear by viewModel.selectedMonthYear.collectAsStateWithLifecycle()
    val currencySymbol by viewModel.currencySymbol.collectAsStateWithLifecycle()

    var showFilterDialog by remember { mutableStateOf(false) }

    val totalAllocated = budgetEnvelopes.sumOf { it.allocatedAmount }
    val totalSpent = budgetEnvelopes.sumOf { it.spentAmount }
    val totalRemaining = totalAllocated - totalSpent
    val activeCount = filterState.activeFilterCount

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onOpenAddBudget(null) },
                containerColor = EmeraldGreenPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_budget_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Budget")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .testTag("budget_screen"),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Month Selector Bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            val next = changeMonth(selectedMonthYear, -1)
                            viewModel.setSelectedMonthYear(next)
                        }
                    ) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Month")
                    }

                    Text(
                        text = formatDisplayMonthFromStr(selectedMonthYear),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )

                    IconButton(
                        onClick = {
                            val next = changeMonth(selectedMonthYear, 1)
                            viewModel.setSelectedMonthYear(next)
                        }
                    ) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Next Month")
                    }
                }
            }

            // Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = EmeraldGreenPrimary)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Budget Allocated", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.8f))
                                Text(formatCurrency(totalAllocated, currencySymbol), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = Color.White)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Spent So Far", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.8f))
                                Text(formatCurrency(totalSpent, currencySymbol), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = if (totalSpent > totalAllocated && totalAllocated > 0) ExpenseRedContainer else EmeraldGreenPrimaryContainer)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        val totalProgress = if (totalAllocated > 0) (totalSpent / totalAllocated).toFloat() else 0f
                        LinearProgressIndicator(
                            progress = { totalProgress.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape),
                            color = if (totalSpent > totalAllocated && totalAllocated > 0) ExpenseRed else GoldenCrescentAccent,
                            trackColor = Color.White.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Remaining: ${formatCurrency(totalRemaining, currencySymbol)}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = Color.White
                            )
                            Text(
                                text = "${(totalProgress * 100).toInt()}% Used",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }

            // Filter Trigger & Indicator
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Envelopes & Categories (${budgetEnvelopes.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    OutlinedButton(
                        onClick = { showFilterDialog = true },
                        modifier = Modifier.testTag("open_budget_filter_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (activeCount > 0) "Filtered ($activeCount)" else "Filter")
                    }
                }
            }

            if (filterState.isActive) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(EmeraldGreenPrimaryContainer.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$activeCount status/activity filter(s) applied",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = EmeraldGreenOnPrimaryContainer
                        )
                        TextButton(
                            onClick = { viewModel.updateBudgetFilter(BudgetFilterState()) },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("Reset", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = ExpenseRed)
                        }
                    }
                }
            }

            // Envelopes List
            if (budgetEnvelopes.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Default.Savings,
                        title = "No budget envelopes match",
                        message = "Adjust your budget filters or tap the '+' button to assign a budget allocation.",
                        actionButton = {
                            Button(onClick = { viewModel.updateBudgetFilter(BudgetFilterState()) }) {
                                Text("Clear Filters")
                            }
                        }
                    )
                }
            } else {
                items(budgetEnvelopes, key = { it.category.id }) { item ->
                    EnvelopeCard(
                        model = item,
                        currencySymbol = currencySymbol,
                        onClick = {
                            if (item.budget != null) {
                                onEditBudget(item.budget, item.category)
                            } else {
                                onOpenAddBudget(item.category)
                            }
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }

    if (showFilterDialog) {
        BudgetMakerFilterDialog(
            currentFilter = filterState,
            categories = allCategories,
            currencySymbol = currencySymbol,
            onDismiss = { showFilterDialog = false },
            onApplyFilter = { newFilter ->
                viewModel.updateBudgetFilter(newFilter)
            }
        )
    }
}

@Composable
fun EnvelopeCard(
    model: BudgetEnvelopeUiModel,
    currencySymbol: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("envelope_card_${model.category.name.lowercase().replace(" ", "_")}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(parseColor(model.category.colorHex).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getCategoryIcon(model.category.iconName),
                            contentDescription = null,
                            tint = parseColor(model.category.colorHex),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = model.category.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        if (model.targetAmount > 0) {
                            Text(
                                text = "Target: ${formatCurrency(model.targetAmount, currencySymbol)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = GoldenCrescentAccent
                            )
                        }
                    }
                }

                // Status Badges
                if (model.isOverspent) {
                    AssistChip(
                        onClick = {},
                        label = { Text("Overspent", color = ExpenseRed, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = ExpenseRedContainer)
                    )
                } else if (model.isTargetAchieved) {
                    AssistChip(
                        onClick = {},
                        label = { Text("Achieved", color = EmeraldGreenPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = EmeraldGreenPrimaryContainer)
                    )
                } else if (model.allocatedAmount > 0) {
                    Text(
                        text = "${(model.progressPercent * 100).toInt()}%",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress Bar
            if (model.allocatedAmount > 0) {
                LinearProgressIndicator(
                    progress = { model.progressPercent },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                    color = if (model.isOverspent) ExpenseRed else parseColor(model.category.colorHex),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Spent: ${formatCurrency(model.spentAmount, currencySymbol)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (model.allocatedAmount > 0)
                        "Remaining: ${formatCurrency(model.remainingAmount, currencySymbol)}"
                    else "No Budget Set",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = if (model.remainingAmount < 0) ExpenseRed else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

fun formatDisplayMonthFromStr(monthYear: String): String {
    return try {
        val sdfIn = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val date = sdfIn.parse(monthYear) ?: Date()
        val sdfOut = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        sdfOut.format(date)
    } catch (e: Exception) {
        monthYear
    }
}

fun changeMonth(monthYear: String, diff: Int): String {
    val cal = Calendar.getInstance()
    val parts = monthYear.split("-")
    val year = parts.getOrNull(0)?.toIntOrNull() ?: cal.get(Calendar.YEAR)
    val month = (parts.getOrNull(1)?.toIntOrNull() ?: (cal.get(Calendar.MONTH) + 1)) - 1
    cal.set(year, month, 1)
    cal.add(Calendar.MONTH, diff)
    val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
    return sdf.format(cal.time)
}
