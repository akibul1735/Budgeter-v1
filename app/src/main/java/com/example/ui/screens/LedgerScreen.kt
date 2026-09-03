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
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
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
import com.example.data.repository.AccountWithBalance
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidExpenseContainer
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidIncomeContainer
import com.example.ui.theme.SolidPrimary
import com.example.ui.theme.SolidPrimaryContainer
import com.example.ui.theme.SolidTransfer
import com.example.util.DateUtils
import com.example.util.IconHelper
import com.example.util.LanguageHelper

@Composable
fun LedgerScreen(
    transactions: List<TransactionWithDetails>,
    languageMode: LanguageMode,
    accountsWithBalances: List<AccountWithBalance> = emptyList(),
    onAddTransactionClick: () -> Unit,
    onTransactionClick: (Transaction) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTypeFilter by remember { mutableStateOf<TransactionType?>(null) }
    var showSearchField by remember { mutableStateOf(false) }

    val accountBalanceMap: Map<Long, Double> = remember(accountsWithBalances) {
        accountsWithBalances.associate { it.account.id to it.currentBalance }
    }

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

    // Group transactions by calendar day (descending order)
    val groupedByDay = remember(filteredTransactions) {
        filteredTransactions.groupBy { DateUtils.getStartOfDay(it.transaction.dateEpochMs) }
            .toList()
            .sortedByDescending { it.first }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("ledger_screen"),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Search & Filter Header
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = LanguageHelper.getString("transactions", languageMode),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { showSearchField = !showSearchField },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Search",
                                tint = if (showSearchField || searchQuery.isNotEmpty()) SolidPrimary else MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }

                if (showSearchField || searchQuery.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(LanguageHelper.getString("search", languageMode), fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Type Filter Chips (All, Expense, Income, Transfer)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedTypeFilter == null,
                        onClick = { selectedTypeFilter = null },
                        label = { Text(LanguageHelper.getString("all", languageMode), fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        shape = RoundedCornerShape(8.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SolidPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = selectedTypeFilter == TransactionType.EXPENSE,
                        onClick = { selectedTypeFilter = if (selectedTypeFilter == TransactionType.EXPENSE) null else TransactionType.EXPENSE },
                        label = { Text(LanguageHelper.getString("expense", languageMode), fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        shape = RoundedCornerShape(8.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SolidExpense,
                            selectedLabelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = selectedTypeFilter == TransactionType.INCOME,
                        onClick = { selectedTypeFilter = if (selectedTypeFilter == TransactionType.INCOME) null else TransactionType.INCOME },
                        label = { Text(LanguageHelper.getString("income", languageMode), fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        shape = RoundedCornerShape(8.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SolidIncome,
                            selectedLabelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = selectedTypeFilter == TransactionType.TRANSFER,
                        onClick = { selectedTypeFilter = if (selectedTypeFilter == TransactionType.TRANSFER) null else TransactionType.TRANSFER },
                        label = { Text(LanguageHelper.getString("transfer", languageMode), fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        shape = RoundedCornerShape(8.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SolidTransfer,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        if (groupedByDay.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = LanguageHelper.getString("no_transactions", languageMode),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        } else {
            // Render Date Grouped Transactions (like Bluecoins structure)
            groupedByDay.forEach { (dayEpochMs, dayTxList) ->
                // Calculate Net Day Total (Income - Expense, Transfers don't affect net day total)
                val dayIncome = dayTxList.filter { it.transaction.type == TransactionType.INCOME }.sumOf { it.transaction.amount }
                val dayExpense = dayTxList.filter { it.transaction.type == TransactionType.EXPENSE }.sumOf { it.transaction.amount }
                val dayNet = dayIncome - dayExpense

                // Day Header
                item(key = "day_header_$dayEpochMs") {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 2.dp),
                        color = Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = DateUtils.formatDayHeader(dayEpochMs, languageMode),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            val netSign = if (dayNet > 0) "+" else if (dayNet < 0) "-" else ""
                            val netColor = if (dayNet > 0) SolidIncome else if (dayNet < 0) SolidExpense else MaterialTheme.colorScheme.outline
                            Text(
                                text = "$netSign${LanguageHelper.formatCurrency(kotlin.math.abs(dayNet), languageMode)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = netColor
                            )
                        }
                    }
                }

                // Day's Transaction Items Container
                item(key = "day_items_$dayEpochMs") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            dayTxList.forEachIndexed { index, item ->
                                val tx = item.transaction
                                TransactionRowItem(
                                    item = item,
                                    languageMode = languageMode,
                                    accountBalance = when (tx.type) {
                                        TransactionType.EXPENSE -> tx.creditAccountId?.let { accountBalanceMap[it] }
                                        TransactionType.INCOME -> tx.debitAccountId?.let { accountBalanceMap[it] }
                                        TransactionType.TRANSFER -> tx.debitAccountId?.let { accountBalanceMap[it] }
                                    },
                                    onClick = { onTransactionClick(tx) }
                                )

                                if (index < dayTxList.size - 1) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(start = 56.dp, end = 12.dp),
                                        thickness = 0.5.dp,
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionRowItem(
    item: TransactionWithDetails,
    languageMode: LanguageMode,
    accountBalance: Double?,
    onClick: () -> Unit
) {
    val tx = item.transaction

    val iconColor = when (tx.type) {
        TransactionType.EXPENSE -> SolidExpense
        TransactionType.INCOME -> SolidIncome
        TransactionType.TRANSFER -> SolidTransfer
    }
    val iconBg = when (tx.type) {
        TransactionType.EXPENSE -> SolidExpenseContainer
        TransactionType.INCOME -> SolidIncomeContainer
        TransactionType.TRANSFER -> SolidPrimaryContainer
    }

    // Determine primary title: If payee/payer is set, use it; otherwise category name
    val primaryTitle = if (tx.payeeOrPayer.isNotBlank()) {
        tx.payeeOrPayer
    } else {
        when (tx.type) {
            TransactionType.EXPENSE -> item.subCategory?.localizedName(languageMode)
                ?: item.category?.localizedName(languageMode)
                ?: LanguageHelper.getString("expense", languageMode)
            TransactionType.INCOME -> item.subCategory?.localizedName(languageMode)
                ?: item.category?.localizedName(languageMode)
                ?: LanguageHelper.getString("income", languageMode)
            TransactionType.TRANSFER -> LanguageHelper.getString("transfer", languageMode)
        }
    }

    // Determine subtitle (Category / Subcategory or Transfer flow)
    val subTitle = when (tx.type) {
        TransactionType.EXPENSE -> {
            val cat = item.category?.localizedName(languageMode) ?: ""
            val sub = item.subCategory?.localizedName(languageMode)
            if (sub != null && sub != primaryTitle) "$cat / $sub" else cat
        }
        TransactionType.INCOME -> {
            val cat = item.category?.localizedName(languageMode) ?: ""
            val sub = item.subCategory?.localizedName(languageMode)
            if (sub != null && sub != primaryTitle) "$cat / $sub" else cat
        }
        TransactionType.TRANSFER -> {
            "(${LanguageHelper.getString("transfer", languageMode)})"
        }
    }

    // Account display
    val accountDisplay = when (tx.type) {
        TransactionType.EXPENSE -> item.creditAccount?.localizedName(languageMode) ?: ""
        TransactionType.INCOME -> item.debitAccount?.localizedName(languageMode) ?: ""
        TransactionType.TRANSFER -> {
            val from = item.creditAccount?.localizedName(languageMode) ?: "Source"
            val to = item.debitAccount?.localizedName(languageMode) ?: "Dest"
            "$from ➔ $to"
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left: Avatar Icon
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconBg),
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
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Center: Title, Subtitle, Note
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = primaryTitle,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (subTitle.isNotBlank()) {
                    Text(
                        text = subTitle,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (tx.note.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 1.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notes,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = tx.note,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.outline,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Right: Amount and Account Info
        Column(horizontalAlignment = Alignment.End) {
            val sign = when (tx.type) {
                TransactionType.EXPENSE -> "-"
                TransactionType.INCOME -> "+"
                TransactionType.TRANSFER -> ""
            }
            val amtColor = when (tx.type) {
                TransactionType.EXPENSE -> SolidExpense
                TransactionType.INCOME -> SolidIncome
                TransactionType.TRANSFER -> SolidTransfer
            }

            Text(
                text = "$sign${LanguageHelper.formatCurrency(tx.amount, languageMode)}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = amtColor
            )

            Spacer(modifier = Modifier.height(2.dp))

            val accountLine = if (accountBalance != null && tx.type != TransactionType.TRANSFER) {
                "$accountDisplay  ${LanguageHelper.formatCurrency(accountBalance, languageMode)}"
            } else {
                accountDisplay
            }

            if (accountLine.isNotBlank()) {
                Text(
                    text = accountLine,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
