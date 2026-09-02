package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LanguageMode
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.data.model.TransactionWithDetails
import com.example.ui.components.DoubleEntryFlowBadge
import com.example.util.DateUtils
import com.example.util.IconHelper
import com.example.util.LanguageHelper

@Composable
fun LedgerScreen(
    transactions: List<TransactionWithDetails>,
    languageMode: LanguageMode,
    onAddTransactionClick: () -> Unit,
    onTransactionClick: (Transaction) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTypeFilter by remember { mutableStateOf<TransactionType?>(null) }

    val filteredTransactions = remember(transactions, searchQuery, selectedTypeFilter) {
        transactions.filter { item ->
            val matchesType = selectedTypeFilter == null || item.transaction.type == selectedTypeFilter
            val matchesSearch = if (searchQuery.isBlank()) true else {
                val query = searchQuery.trim().lowercase()
                item.transaction.note.lowercase().contains(query) ||
                        item.transaction.payeeOrPayer.lowercase().contains(query) ||
                        (item.category?.nameEn?.lowercase()?.contains(query) == true) ||
                        (item.category?.nameBn?.lowercase()?.contains(query) == true) ||
                        (item.subCategory?.nameEn?.lowercase()?.contains(query) == true) ||
                        (item.subCategory?.nameBn?.lowercase()?.contains(query) == true) ||
                        (item.debitAccount?.nameEn?.lowercase()?.contains(query) == true) ||
                        (item.creditAccount?.nameEn?.lowercase()?.contains(query) == true)
            }
            matchesType && matchesSearch
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("ledger_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Title & Add Transaction button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = LanguageHelper.getString("ledger", languageMode),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Button(
                    onClick = onAddTransactionClick,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("ledger_add_tx_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(LanguageHelper.getString("add_transaction", languageMode), fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(LanguageHelper.getString("search", languageMode)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )
        }

        // Type Filter Chips (All, Expense, Income, Transfer)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedTypeFilter == null,
                    onClick = { selectedTypeFilter = null },
                    label = { Text("All") },
                    shape = RoundedCornerShape(10.dp)
                )
                FilterChip(
                    selected = selectedTypeFilter == TransactionType.EXPENSE,
                    onClick = { selectedTypeFilter = TransactionType.EXPENSE },
                    label = { Text(LanguageHelper.getString("expense", languageMode)) },
                    shape = RoundedCornerShape(10.dp)
                )
                FilterChip(
                    selected = selectedTypeFilter == TransactionType.INCOME,
                    onClick = { selectedTypeFilter = TransactionType.INCOME },
                    label = { Text(LanguageHelper.getString("income", languageMode)) },
                    shape = RoundedCornerShape(10.dp)
                )
                FilterChip(
                    selected = selectedTypeFilter == TransactionType.TRANSFER,
                    onClick = { selectedTypeFilter = TransactionType.TRANSFER },
                    label = { Text(LanguageHelper.getString("transfer", languageMode)) },
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        if (filteredTransactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = LanguageHelper.getString("no_transactions", languageMode),
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        } else {
            items(filteredTransactions) { item ->
                val tx = item.transaction
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onTransactionClick(tx) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                val iconColor = when (tx.type) {
                                    TransactionType.EXPENSE -> MaterialTheme.colorScheme.error
                                    TransactionType.INCOME -> Color(0xFF10B981)
                                    TransactionType.TRANSFER -> MaterialTheme.colorScheme.primary
                                }

                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(iconColor.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val icon = when (tx.type) {
                                        TransactionType.EXPENSE -> IconHelper.getIconByName(item.category?.iconName ?: "Category")
                                        TransactionType.INCOME -> IconHelper.getIconByName(item.category?.iconName ?: "Payments")
                                        TransactionType.TRANSFER -> Icons.Default.SwapHoriz
                                    }
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = iconColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    val title = when (tx.type) {
                                        TransactionType.EXPENSE -> item.subCategory?.localizedName(languageMode)
                                            ?: item.category?.localizedName(languageMode)
                                            ?: "Expense"
                                        TransactionType.INCOME -> item.subCategory?.localizedName(languageMode)
                                            ?: item.category?.localizedName(languageMode)
                                            ?: "Income"
                                        TransactionType.TRANSFER -> LanguageHelper.getString("transfer", languageMode)
                                    }
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    if (tx.note.isNotBlank()) {
                                        Text(
                                            text = tx.note,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.outline,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                val sign = when (tx.type) {
                                    TransactionType.EXPENSE -> "-"
                                    TransactionType.INCOME -> "+"
                                    TransactionType.TRANSFER -> ""
                                }
                                val amtColor = when (tx.type) {
                                    TransactionType.EXPENSE -> MaterialTheme.colorScheme.error
                                    TransactionType.INCOME -> Color(0xFF10B981)
                                    TransactionType.TRANSFER -> MaterialTheme.colorScheme.primary
                                }
                                Text(
                                    text = "$sign${LanguageHelper.formatCurrency(tx.amount, languageMode)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = amtColor
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = DateUtils.formatDate(tx.dateEpochMs, languageMode),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Double Entry Badge
                        DoubleEntryFlowBadge(item = item, languageMode = languageMode)
                    }
                }
            }
        }
    }
}
