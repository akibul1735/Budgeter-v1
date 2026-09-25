package com.example.budgeter.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.budgeter.data.model.*
import com.example.budgeter.ui.components.*
import com.example.budgeter.ui.theme.*
import com.example.budgeter.viewmodel.BudgeterViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerScreen(
    viewModel: BudgeterViewModel,
    onOpenAddTransaction: () -> Unit,
    onSelectTransaction: (Transaction) -> Unit
) {
    val filteredTransactions by viewModel.filteredTransactions.collectAsStateWithLifecycle()
    val allCategories by viewModel.allCategories.collectAsStateWithLifecycle()
    val allAccounts by viewModel.allAccounts.collectAsStateWithLifecycle()
    val filterState by viewModel.ledgerFilterState.collectAsStateWithLifecycle()
    val filteredTotals by viewModel.filteredTotals.collectAsStateWithLifecycle()
    val availableLabels by viewModel.availableLabels.collectAsStateWithLifecycle()
    val currencySymbol by viewModel.currencySymbol.collectAsStateWithLifecycle()

    var showFilterDialog by remember { mutableStateOf(false) }

    val activeCount = filterState.activeFilterCount

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                // Search Bar & Filter Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = filterState.searchQuery,
                        onValueChange = { viewModel.updateLedgerFilter(filterState.copy(searchQuery = it)) },
                        placeholder = { Text("Search transactions, notes, labels...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = if (filterState.searchQuery.isNotBlank()) {
                            {
                                IconButton(onClick = { viewModel.updateLedgerFilter(filterState.copy(searchQuery = "")) }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear search")
                                }
                            }
                        } else null,
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("ledger_search_input")
                    )

                    // Filter Button with Badge
                    BadgedBox(
                        badge = {
                            if (activeCount > 0) {
                                Badge(
                                    containerColor = EmeraldGreenPrimary,
                                    contentColor = Color.White
                                ) {
                                    Text(activeCount.toString())
                                }
                            }
                        }
                    ) {
                        FilledTonalIconButton(
                            onClick = { showFilterDialog = true },
                            modifier = Modifier.testTag("open_filter_dialog_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Filter",
                                tint = if (activeCount > 0) EmeraldGreenPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Quick Multi-Type Filter Chips row
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(TransactionType.values()) { type ->
                        val isSelected = filterState.selectedTypeFilters.contains(type)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                val currentSet = filterState.selectedTypeFilters
                                val newSet = if (isSelected) currentSet - type else currentSet + type
                                viewModel.updateLedgerFilter(filterState.copy(selectedTypeFilters = newSet))
                            },
                            label = { Text(type.name) },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            modifier = Modifier.testTag("ledger_type_chip_${type.name.lowercase()}")
                        )
                    }

                    // Time period badge chip
                    item {
                        SuggestionChip(
                            onClick = { showFilterDialog = true },
                            label = {
                                Text(
                                    when (filterState.datePreset) {
                                        DatePreset.ALL -> "All Time"
                                        DatePreset.TODAY -> "Today"
                                        DatePreset.THIS_WEEK -> "This Week"
                                        DatePreset.THIS_MONTH -> "This Month"
                                        DatePreset.LAST_MONTH -> "Last Month"
                                        DatePreset.THIS_YEAR -> "This Year"
                                        DatePreset.CUSTOM -> "Custom Date"
                                    }
                                )
                            },
                            icon = { Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        )
                    }
                }

                // Active Filter Indicator Bar
                if (filterState.isActive) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(EmeraldGreenPrimaryContainer.copy(alpha = 0.4f))
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$activeCount filter(s) active • ${filteredTransactions.size} results",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = EmeraldGreenOnPrimaryContainer
                        )
                        TextButton(
                            onClick = { viewModel.updateLedgerFilter(LedgerFilterState()) },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("Reset All", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = ExpenseRed)
                        }
                    }
                }

                // Filtered Summary Totals Strip
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Income", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(formatCurrency(filteredTotals.totalIncome, currencySymbol), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = IncomeGreen)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Expense", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(formatCurrency(filteredTotals.totalExpense, currencySymbol), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = ExpenseRed)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Net", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                formatCurrency(filteredTotals.netBalance, currencySymbol),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (filteredTotals.netBalance >= 0) IncomeGreen else ExpenseRed
                            )
                        }
                    }
                }

                HorizontalDivider()
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onOpenAddTransaction,
                containerColor = EmeraldGreenPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_transaction_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction")
            }
        }
    ) { innerPadding ->
        if (filteredTransactions.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.FilterListOff,
                title = "No matching transactions",
                message = "Try clearing or adjusting your multi-select filters and search terms.",
                actionButton = {
                    Button(onClick = { viewModel.updateLedgerFilter(LedgerFilterState()) }) {
                        Text("Clear All Filters")
                    }
                },
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredTransactions, key = { it.id }) { tx ->
                    val category = allCategories.firstOrNull { it.id == tx.categoryId }
                    val account = allAccounts.firstOrNull { it.id == tx.accountId }
                    val destAccount = if (tx.destinationAccountId != null) allAccounts.firstOrNull { it.id == tx.destinationAccountId } else null

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectTransaction(tx) }
                            .testTag("transaction_item_${tx.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when (tx.type) {
                                                TransactionType.EXPENSE -> ExpenseRedContainer
                                                TransactionType.INCOME -> IncomeGreenContainer
                                                TransactionType.TRANSFER -> TransferBlueContainer
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (tx.type == TransactionType.TRANSFER) Icons.Default.SyncAlt
                                        else getCategoryIcon(category?.iconName ?: "category"),
                                        contentDescription = null,
                                        tint = when (tx.type) {
                                            TransactionType.EXPENSE -> ExpenseRed
                                            TransactionType.INCOME -> IncomeGreen
                                            TransactionType.TRANSFER -> TransferBlue
                                        },
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = tx.title,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (tx.type == TransactionType.TRANSFER) "${account?.name ?: "Account"} → ${destAccount?.name ?: "Account"}"
                                        else "${category?.name ?: "General"} • ${account?.name ?: "Account"}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (tx.labels.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = tx.labels.split(",").joinToString(" ") { "#${it.trim()}" },
                                            style = MaterialTheme.typography.labelSmall,
                                            color = EmeraldGreenPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = when (tx.type) {
                                        TransactionType.EXPENSE -> "-${formatCurrency(tx.amount, currencySymbol)}"
                                        TransactionType.INCOME -> "+${formatCurrency(tx.amount, currencySymbol)}"
                                        TransactionType.TRANSFER -> formatCurrency(tx.amount, currencySymbol)
                                    },
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = when (tx.type) {
                                        TransactionType.EXPENSE -> ExpenseRed
                                        TransactionType.INCOME -> IncomeGreen
                                        TransactionType.TRANSFER -> TransferBlue
                                    }
                                )
                                Text(
                                    text = formatDate(tx.timestamp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showFilterDialog) {
        TransactionFilterDialog(
            currentFilter = filterState,
            categories = allCategories,
            accounts = allAccounts,
            availableLabels = availableLabels,
            currencySymbol = currencySymbol,
            onDismiss = { showFilterDialog = false },
            onApplyFilter = { newFilter ->
                viewModel.updateLedgerFilter(newFilter)
            }
        )
    }
}
