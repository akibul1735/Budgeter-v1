package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.TransactionType
import com.example.ui.components.EmptyStatePlaceholder
import com.example.ui.components.TransactionRowItem
import com.example.ui.theme.EmeraldIncome
import com.example.ui.theme.CrimsonExpense
import com.example.ui.viewmodel.DateFilterOption
import com.example.ui.viewmodel.FinanceViewModel
import com.example.util.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    viewModel: FinanceViewModel,
    onAddTransactionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currencyCode by viewModel.selectedCurrency.collectAsState()
    val filterState by viewModel.filterState.collectAsState()
    val transactions by viewModel.filteredTransactions.collectAsState()
    val accounts by viewModel.allAccounts.collectAsState()

    var showAccountFilterMenu by remember { mutableStateOf(false) }

    // Summary of filtered results
    val totalIncome = transactions.filter { it.transaction.type == TransactionType.INCOME }.sumOf { it.transaction.amount }
    val totalExpense = transactions.filter { it.transaction.type == TransactionType.EXPENSE }.sumOf { it.transaction.amount }
    val netAmount = totalIncome - totalExpense

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Search Bar & CSV Export
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = filterState.searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = { Text("Search transactions, notes, tags...") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                },
                trailingIcon = {
                    if (filterState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(1f)
            )

            // Export CSV button
            IconButton(
                onClick = {
                    val csvData = viewModel.exportTransactionsCsv()
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, csvData)
                        putExtra(Intent.EXTRA_TITLE, "Bluecoins_Transactions.csv")
                        type = "text/csv"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, "Export Transactions CSV")
                    context.startActivity(shareIntent)
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
            ) {
                Icon(
                    imageVector = Icons.Default.FileDownload,
                    contentDescription = "Export CSV",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Filter Chips Row (Type & Date)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Type filters
            item {
                FilterChip(
                    selected = filterState.selectedType == null,
                    onClick = { viewModel.updateTypeFilter(null) },
                    label = { Text("All Types") }
                )
            }
            item {
                FilterChip(
                    selected = filterState.selectedType == TransactionType.EXPENSE,
                    onClick = { viewModel.updateTypeFilter(TransactionType.EXPENSE) },
                    label = { Text("Expenses") }
                )
            }
            item {
                FilterChip(
                    selected = filterState.selectedType == TransactionType.INCOME,
                    onClick = { viewModel.updateTypeFilter(TransactionType.INCOME) },
                    label = { Text("Income") }
                )
            }
            item {
                FilterChip(
                    selected = filterState.selectedType == TransactionType.TRANSFER,
                    onClick = { viewModel.updateTypeFilter(TransactionType.TRANSFER) },
                    label = { Text("Transfers") }
                )
            }

            // Date Range
            items(DateFilterOption.values()) { option ->
                FilterChip(
                    selected = filterState.dateFilter == option,
                    onClick = { viewModel.updateDateFilter(option) },
                    label = { Text(option.label) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Filter Summary Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${transactions.size} records",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "+${Formatters.formatCurrency(totalIncome, currencyCode)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldIncome
                    )
                    Text(
                        text = "-${Formatters.formatCurrency(totalExpense, currencyCode)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = CrimsonExpense
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Transactions List grouped by date
        if (transactions.isEmpty()) {
            EmptyStatePlaceholder(
                title = "No transactions found",
                message = "Try changing your filters or add a new transaction.",
                icon = Icons.Default.FilterAltOff
            )
        } else {
            val grouped = transactions.groupBy { Formatters.formatRelativeDate(it.transaction.dateEpochMs) }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                grouped.forEach { (dateHeader, itemsInGroup) ->
                    item {
                        Text(
                            text = dateHeader,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                        )
                    }

                    items(itemsInGroup, key = { it.transaction.id }) { item ->
                        TransactionRowItem(
                            item = item,
                            currencyCode = currencyCode,
                            onDelete = { viewModel.deleteTransaction(item.transaction) }
                        )
                    }
                }
            }
        }
    }
}
