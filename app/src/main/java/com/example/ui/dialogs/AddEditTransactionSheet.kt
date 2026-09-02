package com.example.ui.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Account
import com.example.data.model.Category
import com.example.data.model.CategoryType
import com.example.data.model.LanguageMode
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.ui.components.DatePickerModal
import com.example.ui.components.PopupCalculatorDialog
import com.example.util.DateUtils
import com.example.util.IconHelper
import com.example.util.LanguageHelper

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditTransactionSheet(
    accounts: List<Account>,
    categories: List<Category>,
    languageMode: LanguageMode,
    existingTransaction: Transaction? = null,
    onDismiss: () -> Unit,
    onSave: (Transaction) -> Unit,
    onDelete: ((Transaction) -> Unit)? = null
) {
    var txType by remember {
        mutableStateOf(existingTransaction?.type ?: TransactionType.EXPENSE)
    }

    var amount by remember {
        mutableDoubleStateOf(existingTransaction?.amount ?: 0.0)
    }

    var selectedDateEpochMs by remember {
        mutableLongStateOf(existingTransaction?.dateEpochMs ?: System.currentTimeMillis())
    }

    var note by remember {
        mutableStateOf(existingTransaction?.note ?: "")
    }

    var payee by remember {
        mutableStateOf(existingTransaction?.payeeOrPayer ?: "")
    }

    // Double-entry Accounts
    var debitAccountId by remember {
        mutableStateOf(existingTransaction?.debitAccountId)
    }

    var creditAccountId by remember {
        mutableStateOf(existingTransaction?.creditAccountId)
    }

    // Categories
    var selectedCategoryId by remember {
        mutableStateOf(existingTransaction?.categoryId)
    }

    var selectedSubCategoryId by remember {
        mutableStateOf(existingTransaction?.subCategoryId)
    }

    // Modal dialogs
    var showCalculator by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    // Filter categories by type and active status
    val relevantCategories = remember(categories, txType) {
        val targetType = when (txType) {
            TransactionType.EXPENSE -> CategoryType.EXPENSE
            TransactionType.INCOME -> CategoryType.INCOME
            TransactionType.TRANSFER -> null
        }
        if (targetType != null) {
            categories.filter { it.type == targetType && it.parentId == null && it.isActive }
        } else emptyList()
    }

    // Auto-select first category if none selected
    if (selectedCategoryId == null && relevantCategories.isNotEmpty()) {
        selectedCategoryId = relevantCategories.first().id
    }

    // Subcategories of selected category
    val relevantSubCategories = remember(categories, selectedCategoryId) {
        if (selectedCategoryId != null) {
            categories.filter { it.parentId == selectedCategoryId && it.isActive }
        } else emptyList()
    }

    // Usable accounts (prefer sub-accounts / child accounts if present, else parent accounts, active only)
    val usableAccounts = remember(accounts) {
        val activeList = accounts.filter { it.isActive }
        val parentsWithChildren = activeList.filter { it.parentId != null }.mapNotNull { it.parentId }.toSet()
        activeList.filter { it.id !in parentsWithChildren }
    }

    // Auto-select default accounts
    if (usableAccounts.isNotEmpty()) {
        if (txType == TransactionType.EXPENSE && creditAccountId == null) {
            creditAccountId = usableAccounts.firstOrNull()?.id
        } else if (txType == TransactionType.INCOME && debitAccountId == null) {
            debitAccountId = usableAccounts.firstOrNull()?.id
        } else if (txType == TransactionType.TRANSFER) {
            if (creditAccountId == null) creditAccountId = usableAccounts.firstOrNull()?.id
            if (debitAccountId == null && usableAccounts.size > 1) debitAccountId = usableAccounts.getOrNull(1)?.id
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("add_transaction_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Top Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (existingTransaction == null) {
                            LanguageHelper.getString("add_transaction", languageMode)
                        } else {
                            LanguageHelper.getString("edit_transaction", languageMode)
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Type Tabs (Expense, Income, Transfer)
                val tabs = listOf(
                    TransactionType.EXPENSE to LanguageHelper.getString("expense", languageMode),
                    TransactionType.INCOME to LanguageHelper.getString("income", languageMode),
                    TransactionType.TRANSFER to LanguageHelper.getString("transfer", languageMode)
                )
                val selectedTabIndex = tabs.indexOfFirst { it.first == txType }.coerceAtLeast(0)

                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = when (txType) {
                                TransactionType.EXPENSE -> MaterialTheme.colorScheme.error
                                TransactionType.INCOME -> Color(0xFF10B981)
                                TransactionType.TRANSFER -> MaterialTheme.colorScheme.primary
                            }
                        )
                    },
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    tabs.forEachIndexed { index, pair ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = {
                                txType = pair.first
                                selectedCategoryId = null
                                selectedSubCategoryId = null
                            },
                            text = {
                                Text(
                                    text = pair.second,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Amount Field with Quick Calculator Button
                Text(
                    text = LanguageHelper.getString("amount", languageMode),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
                            .clickable { showCalculator = true }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = LanguageHelper.formatCurrency(amount, languageMode),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = when (txType) {
                                    TransactionType.EXPENSE -> MaterialTheme.colorScheme.error
                                    TransactionType.INCOME -> Color(0xFF10B981)
                                    TransactionType.TRANSFER -> MaterialTheme.colorScheme.primary
                                }
                            )
                            Text(
                                text = "TAP TO CALC",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    Button(
                        onClick = { showCalculator = true },
                        modifier = Modifier
                            .height(56.dp)
                            .testTag("open_calculator_btn"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(Icons.Default.Calculate, contentDescription = "Calculator", modifier = Modifier.size(24.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Date Picker Trigger
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                        .clickable { showDatePicker = true }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Date",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "${LanguageHelper.getString("date", languageMode)}: ${DateUtils.formatDate(selectedDateEpochMs, languageMode)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(
                        text = LanguageHelper.getString("edit", languageMode),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Main Categories & Sub-Categories (For Expense & Income)
                if (txType != TransactionType.TRANSFER) {
                    Text(
                        text = LanguageHelper.getString("categories", languageMode),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        relevantCategories.forEach { cat ->
                            val isSelected = selectedCategoryId == cat.id
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedCategoryId = cat.id
                                    selectedSubCategoryId = null
                                },
                                label = { Text(cat.localizedName(languageMode), fontSize = 12.sp) },
                                leadingIcon = {
                                    Icon(
                                        IconHelper.getIconByName(cat.iconName),
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    // Sub-categories if available
                    if (relevantSubCategories.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = LanguageHelper.getString("sub_categories", languageMode),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            relevantSubCategories.forEach { subCat ->
                                val isSelected = selectedSubCategoryId == subCat.id
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedSubCategoryId = if (isSelected) null else subCat.id
                                    },
                                    label = { Text(subCat.localizedName(languageMode), fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Account Selection according to Double-Entry
                when (txType) {
                    TransactionType.EXPENSE -> {
                        Text(
                            text = LanguageHelper.getString("credit_account", languageMode),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            usableAccounts.forEach { acc ->
                                val isSelected = creditAccountId == acc.id
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { creditAccountId = acc.id },
                                    label = { Text(acc.localizedName(languageMode), fontSize = 12.sp) },
                                    leadingIcon = {
                                        Icon(
                                            IconHelper.getIconByName(acc.iconName),
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }
                    }
                    TransactionType.INCOME -> {
                        Text(
                            text = LanguageHelper.getString("debit_account", languageMode),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            usableAccounts.forEach { acc ->
                                val isSelected = debitAccountId == acc.id
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { debitAccountId = acc.id },
                                    label = { Text(acc.localizedName(languageMode), fontSize = 12.sp) },
                                    leadingIcon = {
                                        Icon(
                                            IconHelper.getIconByName(acc.iconName),
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }
                    }
                    TransactionType.TRANSFER -> {
                        // Source Account (From)
                        Text(
                            text = LanguageHelper.getString("source_account", languageMode),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            usableAccounts.forEach { acc ->
                                val isSelected = creditAccountId == acc.id
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { creditAccountId = acc.id },
                                    label = { Text(acc.localizedName(languageMode), fontSize = 12.sp) },
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Destination Account (To)
                        Text(
                            text = LanguageHelper.getString("destination_account", languageMode),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            usableAccounts.forEach { acc ->
                                val isSelected = debitAccountId == acc.id
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { debitAccountId = acc.id },
                                    label = { Text(acc.localizedName(languageMode), fontSize = 12.sp) },
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Notes Field
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(LanguageHelper.getString("notes", languageMode)) },
                    leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Payee/Payer Field
                OutlinedTextField(
                    value = payee,
                    onValueChange = { payee = it },
                    label = { Text(LanguageHelper.getString("payee_payer", languageMode)) },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (existingTransaction != null && onDelete != null) {
                        Button(
                            onClick = {
                                onDelete(existingTransaction)
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(0.7f)
                        ) {
                            Text(LanguageHelper.getString("delete", languageMode))
                        }
                    }

                    Button(
                        onClick = {
                            if (amount > 0) {
                                val tx = Transaction(
                                    id = existingTransaction?.id ?: 0,
                                    type = txType,
                                    amount = amount,
                                    dateEpochMs = selectedDateEpochMs,
                                    note = note.trim(),
                                    payeeOrPayer = payee.trim(),
                                    debitAccountId = when (txType) {
                                        TransactionType.EXPENSE -> null
                                        TransactionType.INCOME -> debitAccountId
                                        TransactionType.TRANSFER -> debitAccountId
                                    },
                                    creditAccountId = when (txType) {
                                        TransactionType.EXPENSE -> creditAccountId
                                        TransactionType.INCOME -> null
                                        TransactionType.TRANSFER -> creditAccountId
                                    },
                                    categoryId = if (txType != TransactionType.TRANSFER) selectedCategoryId else null,
                                    subCategoryId = if (txType != TransactionType.TRANSFER) selectedSubCategoryId else null
                                )
                                onSave(tx)
                                onDismiss()
                            }
                        },
                        enabled = amount > 0,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_transaction_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = LanguageHelper.getString("save", languageMode),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    if (showCalculator) {
        PopupCalculatorDialog(
            initialValue = amount,
            languageMode = languageMode,
            onDismiss = { showCalculator = false },
            onValueConfirmed = { calculatedAmount ->
                amount = calculatedAmount
            }
        )
    }

    if (showDatePicker) {
        DatePickerModal(
            selectedDateEpochMs = selectedDateEpochMs,
            languageMode = languageMode,
            onDateSelected = { selectedDateEpochMs = it },
            onDismiss = { showDatePicker = false }
        )
    }
}
