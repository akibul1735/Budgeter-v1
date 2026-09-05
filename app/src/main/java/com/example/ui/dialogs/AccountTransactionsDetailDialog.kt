package com.example.ui.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Account
import com.example.data.model.AccountType
import com.example.data.model.LanguageMode
import com.example.data.model.Transaction
import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionType
import com.example.data.model.TransactionWithDetails
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidPrimary
import com.example.ui.theme.SolidTransfer
import com.example.util.DateUtils
import com.example.util.IconHelper
import com.example.util.LanguageHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private enum class AccountTxFilter(val labelEn: String, val labelBn: String) {
    ALL("All", "সকল"),
    THIS_MONTH("This Month", "চলতি মাস"),
    EXPENSE("Expense", "খরচ"),
    INCOME("Income", "আয়"),
    TRANSFER("Transfer", "স্থানান্তর")
}

@Composable
fun AccountTransactionsDetailDialog(
    account: Account,
    allAccounts: List<Account>,
    allTransactions: List<TransactionWithDetails>,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onEditTransaction: (Transaction) -> Unit,
    onAddTransactionForAccount: ((Account) -> Unit)? = null,
    onEditAccount: ((Account) -> Unit)? = null
) {
    var selectedFilter by remember { mutableStateOf(AccountTxFilter.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var showSearchBar by remember { mutableStateOf(false) }

    // Find all sub-account IDs if this is a parent group
    val subAccountIds = remember(account, allAccounts) {
        allAccounts.filter { it.parentId == account.id }.map { it.id }.toSet()
    }
    val allRelevantAccountIds = remember(account, subAccountIds) {
        subAccountIds + account.id
    }

    // Filter transactions relevant to this account (debit or credit)
    val accountTransactions = remember(account, allRelevantAccountIds, allTransactions) {
        allTransactions.filter { item ->
            val tx = item.transaction
            tx.status != TransactionStatus.VOID &&
                    (allRelevantAccountIds.contains(tx.debitAccountId) ||
                     allRelevantAccountIds.contains(tx.creditAccountId))
        }.sortedByDescending { it.transaction.dateEpochMs }
    }

    // Compute monthly boundaries
    val cal = Calendar.getInstance()
    val thisMonthStart = remember { DateUtils.getStartOfMonth(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1) }
    val thisMonthEnd = remember { DateUtils.getEndOfMonth(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1) }

    // Filter by tab and search
    val displayedTransactions = remember(accountTransactions, selectedFilter, searchQuery, thisMonthStart, thisMonthEnd) {
        var list = accountTransactions

        when (selectedFilter) {
            AccountTxFilter.ALL -> {}
            AccountTxFilter.THIS_MONTH -> {
                list = list.filter { it.transaction.dateEpochMs in thisMonthStart..thisMonthEnd }
            }
            AccountTxFilter.EXPENSE -> {
                list = list.filter { it.transaction.type == TransactionType.EXPENSE }
            }
            AccountTxFilter.INCOME -> {
                list = list.filter { it.transaction.type == TransactionType.INCOME }
            }
            AccountTxFilter.TRANSFER -> {
                list = list.filter { it.transaction.type == TransactionType.TRANSFER }
            }
        }

        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase(Locale.getDefault())
            list = list.filter { item ->
                item.transaction.note.lowercase(Locale.getDefault()).contains(q) ||
                item.transaction.payeeOrPayer.lowercase(Locale.getDefault()).contains(q) ||
                (item.category?.nameEn?.lowercase(Locale.getDefault())?.contains(q) == true) ||
                (item.category?.nameBn?.lowercase(Locale.getDefault())?.contains(q) == true) ||
                (item.debitAccount?.nameEn?.lowercase(Locale.getDefault())?.contains(q) == true) ||
                (item.creditAccount?.nameEn?.lowercase(Locale.getDefault())?.contains(q) == true)
            }
        }

        list
    }

    // Statistics: Total Inflow (Debits to Asset or Income), Total Outflow (Credits from Asset or Expense)
    val totalInflow = remember(accountTransactions, account) {
        accountTransactions.sumOf { item ->
            val tx = item.transaction
            val isDebit = allRelevantAccountIds.contains(tx.debitAccountId)
            val isCredit = allRelevantAccountIds.contains(tx.creditAccountId)

            if (account.type == AccountType.ASSET) {
                // Debit increases asset (Inflow: Income or Transfer In)
                if (isDebit) tx.amount else 0.0
            } else {
                // For Liability: Credit increases liability (Borrowed / New Debt), Debit decreases (Repaid)
                if (isCredit) tx.amount else 0.0
            }
        }
    }

    val totalOutflow = remember(accountTransactions, account) {
        accountTransactions.sumOf { item ->
            val tx = item.transaction
            val isDebit = allRelevantAccountIds.contains(tx.debitAccountId)
            val isCredit = allRelevantAccountIds.contains(tx.creditAccountId)

            if (account.type == AccountType.ASSET) {
                // Credit decreases asset (Outflow: Expense or Transfer Out)
                if (isCredit) tx.amount else 0.0
            } else {
                // For Liability: Debit decreases liability (Debt Payment)
                if (isDebit) tx.amount else 0.0
            }
        }
    }

    val typeColor = when (account.type) {
        AccountType.ASSET -> SolidIncome
        AccountType.LIABILITY -> SolidExpense
        else -> SolidPrimary
    }

    val accountLocalizedName = account.localizedName(languageMode)
    val sdf = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .padding(vertical = 12.dp)
                .testTag("account_transactions_detail_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp, vertical = 16.dp)
            ) {
                // 1. Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(typeColor.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = IconHelper.getIconByName(account.iconName),
                                contentDescription = null,
                                tint = typeColor,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = accountLocalizedName,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (subAccountIds.isNotEmpty()) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                    ) {
                                        Text(
                                            text = "${subAccountIds.size + 1} Accounts",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = if (account.type == AccountType.ASSET) "Asset Account" else "Liability Account",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                Text(text = "•", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                Text(
                                    text = "${accountTransactions.size} Transactions",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SolidPrimary
                                )
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { showSearchBar = !showSearchBar },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = if (showSearchBar || searchQuery.isNotEmpty()) SolidPrimary else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        if (onEditAccount != null) {
                            IconButton(
                                onClick = {
                                    onDismiss()
                                    onEditAccount(account)
                                },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Account",
                                    tint = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 2. Summary Card: Total Inflow vs Outflow
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Inflow
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(SolidIncome.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                    contentDescription = null,
                                    tint = SolidIncome,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (account.type == AccountType.ASSET) "Total Received" else "Total Borrowed",
                                    fontSize = 10.5.sp,
                                    color = MaterialTheme.colorScheme.outline,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = LanguageHelper.formatCurrency(totalInflow, languageMode),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SolidIncome
                                )
                            }
                        }

                        // Vertical Divider
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(32.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        )

                        // Outflow
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(SolidExpense.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                                    contentDescription = null,
                                    tint = SolidExpense,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (account.type == AccountType.ASSET) "Total Spent / Out" else "Total Paid Off",
                                    fontSize = 10.5.sp,
                                    color = MaterialTheme.colorScheme.outline,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = LanguageHelper.formatCurrency(totalOutflow, languageMode),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SolidExpense
                                )
                            }
                        }
                    }
                }

                // Search Bar (expandable)
                AnimatedVisibility(visible = showSearchBar) {
                    Column {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search by note, payee, category...", fontSize = 12.sp) },
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(14.dp))
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SolidPrimary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 3. Filter Chips Row
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(AccountTxFilter.values()) { filter ->
                        val selected = selectedFilter == filter
                        FilterChip(
                            selected = selected,
                            onClick = { selectedFilter = filter },
                            label = {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) filter.labelBn else filter.labelEn,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SolidPrimary,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.height(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 4. Transaction List
                if (displayedTransactions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = if (searchQuery.isNotBlank()) "No matching transactions found."
                                else "No transactions recorded for this account yet.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.outline,
                                textAlign = TextAlign.Center
                            )
                            if (onAddTransactionForAccount != null && searchQuery.isBlank()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        onDismiss()
                                        onAddTransactionForAccount(account)
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SolidPrimary),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add First Transaction", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(displayedTransactions, key = { it.transaction.id }) { item ->
                            val tx = item.transaction
                            val isDebit = allRelevantAccountIds.contains(tx.debitAccountId)
                            val isCredit = allRelevantAccountIds.contains(tx.creditAccountId)

                            // Determine Inflow / Outflow display
                            val isPositive = when (tx.type) {
                                TransactionType.INCOME -> true
                                TransactionType.EXPENSE -> false
                                TransactionType.TRANSFER -> isDebit // If this account is destination (Debit), it's + incoming
                            }

                            val amountPrefix = if (tx.type == TransactionType.TRANSFER) {
                                if (isDebit) "+ " else "- "
                            } else if (tx.type == TransactionType.INCOME) "+ " else "- "

                            val txColor = when {
                                tx.type == TransactionType.TRANSFER -> SolidTransfer
                                isPositive -> SolidIncome
                                else -> SolidExpense
                            }

                            val dateStr = sdf.format(Date(tx.dateEpochMs))

                            // Main Title: Note or Category / Transfer Info
                            val mainTitle = when (tx.type) {
                                TransactionType.TRANSFER -> {
                                    val srcName = item.creditAccount?.localizedName(languageMode) ?: "Account"
                                    val destName = item.debitAccount?.localizedName(languageMode) ?: "Account"
                                    "$srcName ➔ $destName"
                                }
                                else -> {
                                    if (tx.note.isNotBlank()) tx.note
                                    else if (item.category != null) item.category.localizedName(languageMode)
                                    else if (tx.type == TransactionType.INCOME) "Income" else "Expense"
                                }
                            }

                            val subSubtitle = when (tx.type) {
                                TransactionType.TRANSFER -> {
                                    if (tx.note.isNotBlank()) tx.note else "Account Transfer"
                                }
                                else -> {
                                    val catStr = item.category?.localizedName(languageMode) ?: ""
                                    val payeeStr = tx.payeeOrPayer
                                    if (catStr.isNotBlank() && payeeStr.isNotBlank()) "$catStr • $payeeStr"
                                    else if (catStr.isNotBlank()) catStr
                                    else payeeStr
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        onDismiss()
                                        onEditTransaction(tx)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    // Left: Type Icon & Details
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(txColor.copy(alpha = 0.12f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = when (tx.type) {
                                                    TransactionType.TRANSFER -> Icons.Default.SwapHoriz
                                                    TransactionType.INCOME -> Icons.AutoMirrored.Filled.TrendingUp
                                                    TransactionType.EXPENSE -> Icons.AutoMirrored.Filled.TrendingDown
                                                },
                                                contentDescription = null,
                                                tint = txColor,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = mainTitle,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            if (subSubtitle.isNotBlank()) {
                                                Text(
                                                    text = subSubtitle,
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            Text(
                                                text = dateStr,
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    // Right: Amount & Status
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "$amountPrefix${LanguageHelper.formatCurrency(tx.amount, languageMode)}",
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = txColor
                                        )
                                        if (tx.status == TransactionStatus.CLEARED || tx.status == TransactionStatus.RECONCILED) {
                                            Text(
                                                text = tx.status.name.lowercase(Locale.getDefault())
                                                    .replaceFirstChar { it.uppercase() },
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = SolidIncome
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(10.dp))

                // 5. Footer Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("Close", fontSize = 12.5.sp)
                    }

                    if (onAddTransactionForAccount != null) {
                        Button(
                            onClick = {
                                onDismiss()
                                onAddTransactionForAccount(account)
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SolidPrimary),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add Transaction", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
