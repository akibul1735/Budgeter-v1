package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.Category
import com.example.data.model.CategorySpend
import com.example.ui.components.EmptyStatePlaceholder
import com.example.ui.theme.EmeraldIncome
import com.example.ui.theme.CrimsonExpense
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BluePrimary
import com.example.ui.viewmodel.FinanceViewModel
import com.example.util.Formatters
import com.example.util.IconHelper
import java.util.Calendar

@Composable
fun BudgetsScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val currencyCode by viewModel.selectedCurrency.collectAsState()
    val categorySpends by viewModel.categorySpends.collectAsState()
    val allCategories by viewModel.allCategories.collectAsState()

    var editingCategory by remember { mutableStateOf<Category?>(null) }

    // Budget math
    val totalBudget = categorySpends.filter { it.budgetAmount > 0 }.sumOf { it.budgetAmount }
    val totalSpentInBudgeted = categorySpends.filter { it.budgetAmount > 0 }.sumOf { it.totalSpent }
    val remainingBudget = totalBudget - totalSpentInBudgeted
    val overallProgress = if (totalBudget > 0) (totalSpentInBudgeted / totalBudget).toFloat() else 0f

    // Days remaining in month calculation
    val cal = Calendar.getInstance()
    val currentDay = cal.get(Calendar.DAY_OF_MONTH)
    val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val daysLeft = (maxDays - currentDay).coerceAtLeast(1)
    val dailyPacing = if (remainingBudget > 0) remainingBudget / daysLeft else 0.0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Overall Budget Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Monthly Budget Health",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${daysLeft} days left",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Progress Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(14.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(overallProgress.coerceIn(0f, 1f))
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(7.dp))
                                .background(
                                    when {
                                        overallProgress > 1f -> CrimsonExpense
                                        overallProgress > 0.8f -> AmberWarning
                                        else -> EmeraldIncome
                                    }
                                )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Spent so far",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = Formatters.formatCurrency(totalSpentInBudgeted, currencyCode),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Safe Daily Spend",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${Formatters.formatCurrency(dailyPacing, currencyCode)}/day",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldIncome
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Remaining",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = Formatters.formatCurrency(remainingBudget, currencyCode),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (remainingBudget >= 0) EmeraldIncome else CrimsonExpense
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Category Budgets & Limits",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (categorySpends.isEmpty()) {
            item {
                EmptyStatePlaceholder(
                    title = "No expense categories",
                    message = "Add expense categories to track your monthly budgets.",
                    icon = Icons.Default.Category
                )
            }
        } else {
            items(categorySpends) { spend ->
                val catColor = IconHelper.parseColor(spend.categoryColor)
                val hasBudget = spend.budgetAmount > 0
                val ratio = if (hasBudget) (spend.totalSpent / spend.budgetAmount).toFloat() else 0f
                val remaining = spend.budgetAmount - spend.totalSpent

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            val cat = allCategories.find { it.id == spend.categoryId }
                            if (cat != null) editingCategory = cat
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(catColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = IconHelper.getIconByName(spend.categoryIcon),
                                    contentDescription = null,
                                    tint = catColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = spend.categoryName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (hasBudget) {
                                        "${Formatters.formatCurrency(spend.totalSpent, currencyCode)} of ${Formatters.formatCurrency(spend.budgetAmount, currencyCode)}"
                                    } else {
                                        "Spent: ${Formatters.formatCurrency(spend.totalSpent, currencyCode)} (No limit set)"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (hasBudget) {
                                Text(
                                    text = if (remaining >= 0) "${Formatters.formatCurrency(remaining, currencyCode)} left" else "${Formatters.formatCurrency(-remaining, currencyCode)} over",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (remaining >= 0) EmeraldIncome else CrimsonExpense
                                )
                            } else {
                                TextButton(
                                    onClick = {
                                        val cat = allCategories.find { it.id == spend.categoryId }
                                        if (cat != null) editingCategory = cat
                                    }
                                ) {
                                    Text("Set Limit")
                                }
                            }
                        }

                        if (hasBudget) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(ratio.coerceIn(0f, 1f))
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            when {
                                                ratio > 1f -> CrimsonExpense
                                                ratio > 0.8f -> AmberWarning
                                                else -> catColor
                                            }
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Edit Budget Limit Dialog
    editingCategory?.let { cat ->
        var budgetText by remember { mutableStateOf(if (cat.monthlyBudget > 0) String.format("%.2f", cat.monthlyBudget) else "") }

        AlertDialog(
            onDismissRequest = { editingCategory = null },
            title = { Text("Budget Limit: ${cat.name}", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Set the maximum amount you plan to spend per month for this category.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = budgetText,
                        onValueChange = { budgetText = it },
                        label = { Text("Monthly Limit ($)") },
                        placeholder = { Text("e.g. 500.00") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = budgetText.toDoubleOrNull() ?: 0.0
                        viewModel.updateCategory(cat.copy(monthlyBudget = amount))
                        editingCategory = null
                    }
                ) {
                    Text("Save Limit")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingCategory = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
