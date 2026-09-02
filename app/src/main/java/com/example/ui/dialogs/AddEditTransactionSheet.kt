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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.input.KeyboardType
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

enum class TransactionStatus {
    CLEARED,
    UNCLEARED,
    RECONCILED
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
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

    var payee by remember {
        mutableStateOf(existingTransaction?.payeeOrPayer ?: "")
    }

    var note by remember {
        mutableStateOf(existingTransaction?.note ?: "")
    }

    var labelTag by remember {
        mutableStateOf(existingTransaction?.referenceNo ?: "")
    }

    var status by remember {
        mutableStateOf(TransactionStatus.CLEARED)
    }

    var keepFormOpen by remember { mutableStateOf(false) }

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

    // Pickers & Modals
    var showCalculator by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showCategoryPickerModal by remember { mutableStateOf(false) }
    var showAccountPickerModal by remember { mutableStateOf(false) }
    var showLabelDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    // Usable Accounts
    val usableAccounts = remember(accounts) {
        val activeList = accounts.filter { it.isActive }
        val parentsWithChildren = activeList.filter { it.parentId != null }.mapNotNull { it.parentId }.toSet()
        activeList.filter { it.id !in parentsWithChildren }
    }

    // Auto-select defaults
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

    // Filter categories by type
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

    if (selectedCategoryId == null && relevantCategories.isNotEmpty() && txType != TransactionType.TRANSFER) {
        selectedCategoryId = relevantCategories.first().id
    }

    val selectedCategory = remember(categories, selectedCategoryId) {
        categories.firstOrNull { it.id == selectedCategoryId }
    }
    val selectedSubCategory = remember(categories, selectedSubCategoryId) {
        categories.firstOrNull { it.id == selectedSubCategoryId }
    }

    val selectedCreditAccount = remember(accounts, creditAccountId) {
        accounts.firstOrNull { it.id == creditAccountId }
    }
    val selectedDebitAccount = remember(accounts, debitAccountId) {
        accounts.firstOrNull { it.id == debitAccountId }
    }

    // Type Colors
    val typePrimaryColor = when (txType) {
        TransactionType.EXPENSE -> SolidExpense
        TransactionType.INCOME -> SolidIncome
        TransactionType.TRANSFER -> SolidTransfer
    }
    val typeContainerColor = when (txType) {
        TransactionType.EXPENSE -> SolidExpenseContainer
        TransactionType.INCOME -> SolidIncomeContainer
        TransactionType.TRANSFER -> SolidPrimaryContainer
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("add_transaction_dialog"),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // Top App Bar (Matching Bluecoins screenshot)
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (existingTransaction == null) {
                                    LanguageHelper.getString("add", languageMode).ifEmpty { "Add" }
                                } else {
                                    LanguageHelper.getString("edit", languageMode).ifEmpty { "Edit" }
                                },
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // "+1" consecutive entry mode toggle button
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { keepFormOpen = !keepFormOpen }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                color = if (keepFormOpen) SolidPrimaryContainer else Color.Transparent
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "+1",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (keepFormOpen) SolidPrimary else MaterialTheme.colorScheme.outline
                                    )
                                }
                            }

                            if (existingTransaction != null && onDelete != null) {
                                IconButton(onClick = { showDeleteConfirmDialog = true }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }

                // Main Form Content Scrollable
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // 1. Title / Payee Name Field with Attachment Icon on Right
                    OutlinedTextField(
                        value = payee,
                        onValueChange = { payee = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tx_payee_input"),
                        placeholder = {
                            Text(
                                text = "Name",
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { /* optional attachment indicator */ }) {
                                Icon(
                                    Icons.Default.AttachFile,
                                    contentDescription = "Attach",
                                    tint = MaterialTheme.colorScheme.outline
                                )
                            }
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 2. Date, Time & Schedule Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showDatePicker = true }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.CalendarMonth,
                                contentDescription = "Date",
                                tint = SolidPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = DateUtils.formatDateFull(selectedDateEpochMs, languageMode),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = DateUtils.formatTime(selectedDateEpochMs, languageMode),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showDatePicker = true }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "|  ${LanguageHelper.getString("schedule", languageMode)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SolidPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = SolidPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 3. Amount Container Card (Matching Screenshot)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.5.dp, typePrimaryColor.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                            .clickable { showCalculator = true },
                        color = typeContainerColor.copy(alpha = 0.4f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Type sign circular indicator
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(typePrimaryColor),
                                contentAlignment = Alignment.Center
                            ) {
                                val signText = when (txType) {
                                    TransactionType.EXPENSE -> "−"
                                    TransactionType.INCOME -> "+"
                                    TransactionType.TRANSFER -> "⇄"
                                }
                                Text(
                                    text = signText,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Large Numeric Amount Display
                            Text(
                                text = LanguageHelper.formatNumber(amount, languageMode),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = typePrimaryColor,
                                modifier = Modifier.weight(1f)
                            )

                            // Calculator Icon Button
                            IconButton(
                                onClick = { showCalculator = true },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.Default.Calculate,
                                    contentDescription = "Calculator",
                                    tint = typePrimaryColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            // Currency Badge Pill
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = typePrimaryColor.copy(alpha = 0.15f),
                                modifier = Modifier.padding(2.dp)
                            ) {
                                Text(
                                    text = "BDT",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = typePrimaryColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 4. Selector Rows (Category, Account, Split, Status, Label)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Row A: Category (For Expense & Income)
                            if (txType != TransactionType.TRANSFER) {
                                OptionRowItem(
                                    icon = {
                                        Icon(
                                            imageVector = IconHelper.getIconByName(selectedCategory?.iconName ?: "MoreHoriz"),
                                            contentDescription = "Category",
                                            tint = typePrimaryColor,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    title = if (selectedCategory != null) {
                                        val catName = selectedCategory.localizedName(languageMode)
                                        val subName = selectedSubCategory?.localizedName(languageMode)
                                        if (subName != null) "$catName / $subName" else catName
                                    } else {
                                        LanguageHelper.getString("select_category", languageMode)
                                    },
                                    onClick = { showCategoryPickerModal = true }
                                )

                                HorizontalDivider(
                                    modifier = Modifier.padding(start = 48.dp, end = 12.dp),
                                    thickness = 0.5.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                )
                            }

                            // Row B: Account
                            OptionRowItem(
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.CreditCard,
                                        contentDescription = "Account",
                                        tint = SolidPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                title = when (txType) {
                                    TransactionType.EXPENSE -> selectedCreditAccount?.localizedName(languageMode)
                                        ?: LanguageHelper.getString("select_account", languageMode)
                                    TransactionType.INCOME -> selectedDebitAccount?.localizedName(languageMode)
                                        ?: LanguageHelper.getString("select_account", languageMode)
                                    TransactionType.TRANSFER -> {
                                        val from = selectedCreditAccount?.localizedName(languageMode) ?: "Source"
                                        val to = selectedDebitAccount?.localizedName(languageMode) ?: "Dest"
                                        "$from ➔ $to"
                                    }
                                },
                                onClick = { showAccountPickerModal = true }
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(start = 48.dp, end = 12.dp),
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            )

                            // Row C: Split
                            OptionRowItem(
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.GridOn,
                                        contentDescription = "Split",
                                        tint = MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                title = LanguageHelper.getString("split", languageMode),
                                onClick = { /* Split indicator */ }
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(start = 48.dp, end = 12.dp),
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            )

                            // Row D: Status (Cleared / Uncleared)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (status == TransactionStatus.CLEARED) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                                        contentDescription = "Status",
                                        tint = if (status == TransactionStatus.CLEARED) SolidPrimary else MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Text(
                                        text = LanguageHelper.getString("status", languageMode),
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    TransactionStatus.values().forEach { st ->
                                        val isSel = status == st
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (isSel) SolidPrimaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .clickable { status = st }
                                        ) {
                                            Text(
                                                text = when (st) {
                                                    TransactionStatus.CLEARED -> LanguageHelper.getString("cleared", languageMode)
                                                    TransactionStatus.UNCLEARED -> LanguageHelper.getString("uncleared", languageMode)
                                                    TransactionStatus.RECONCILED -> LanguageHelper.getString("reconciled", languageMode)
                                                },
                                                fontSize = 10.sp,
                                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSel) SolidPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(
                                modifier = Modifier.padding(start = 48.dp, end = 12.dp),
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            )

                            // Row E: Label / Tags
                            OptionRowItem(
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Label,
                                        contentDescription = "Label",
                                        tint = MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                title = if (labelTag.isNotBlank()) labelTag else LanguageHelper.getString("label", languageMode),
                                onClick = { showLabelDialog = true }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 5. Note Card Box
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        OutlinedTextField(
                            value = note,
                            onValueChange = { note = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                            placeholder = {
                                Text(
                                    text = LanguageHelper.getString("notes", languageMode).ifEmpty { "Note" },
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            },
                            minLines = 3,
                            maxLines = 5,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(80.dp)) // Padding for bottom bar
                }

                // Bottom Action Bar: Type Selector Pills + Save FAB
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Segmented Type Pills: EXPENSE, INCOME, TRANSFER
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            val types = listOf(
                                Triple(TransactionType.EXPENSE, LanguageHelper.getString("expense", languageMode).uppercase(), SolidExpense),
                                Triple(TransactionType.INCOME, LanguageHelper.getString("income", languageMode).uppercase(), SolidIncome),
                                Triple(TransactionType.TRANSFER, LanguageHelper.getString("transfer", languageMode).uppercase(), SolidTransfer)
                            )

                            types.forEach { (type, label, color) ->
                                val isSelected = txType == type
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) color else color.copy(alpha = 0.12f),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable {
                                            txType = type
                                            selectedCategoryId = null
                                            selectedSubCategoryId = null
                                        }
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else color,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Save Floating Action Button
                        FloatingActionButton(
                            onClick = {
                                if (amount > 0) {
                                    val tx = Transaction(
                                        id = existingTransaction?.id ?: 0,
                                        type = txType,
                                        amount = amount,
                                        dateEpochMs = selectedDateEpochMs,
                                        note = note.trim(),
                                        referenceNo = labelTag.trim(),
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
                                    if (keepFormOpen && existingTransaction == null) {
                                        // Reset amount and notes for rapid sequential entry
                                        amount = 0.0
                                        note = ""
                                        payee = ""
                                    } else {
                                        onDismiss()
                                    }
                                }
                            },
                            containerColor = typePrimaryColor,
                            contentColor = Color.White,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("save_transaction_btn")
                        ) {
                            Icon(Icons.Default.Save, contentDescription = "Save", modifier = Modifier.size(22.dp))
                        }
                    }
                }
            }
        }
    }

    // Modal Pickers & Dialogs
    if (showCalculator) {
        PopupCalculatorDialog(
            initialValue = amount,
            languageMode = languageMode,
            onDismiss = { showCalculator = false },
            onValueConfirmed = { calculatedAmount ->
                amount = calculatedAmount
                showCalculator = false
            }
        )
    }

    if (showDatePicker) {
        DatePickerModal(
            selectedDateEpochMs = selectedDateEpochMs,
            languageMode = languageMode,
            onDateSelected = {
                selectedDateEpochMs = it
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }

    if (showCategoryPickerModal) {
        CategoryPickerModalDialog(
            categories = categories,
            txType = txType,
            selectedCategoryId = selectedCategoryId,
            selectedSubCategoryId = selectedSubCategoryId,
            languageMode = languageMode,
            onCategorySelected = { catId, subCatId ->
                selectedCategoryId = catId
                selectedSubCategoryId = subCatId
                showCategoryPickerModal = false
            },
            onDismiss = { showCategoryPickerModal = false }
        )
    }

    if (showAccountPickerModal) {
        AccountPickerModalDialog(
            accounts = usableAccounts,
            txType = txType,
            creditAccountId = creditAccountId,
            debitAccountId = debitAccountId,
            languageMode = languageMode,
            onAccountSelected = { sourceId, destId ->
                creditAccountId = sourceId
                debitAccountId = destId
                showAccountPickerModal = false
            },
            onDismiss = { showAccountPickerModal = false }
        )
    }

    if (showLabelDialog) {
        var tempLabel by remember { mutableStateOf(labelTag) }
        AlertDialog(
            onDismissRequest = { showLabelDialog = false },
            title = { Text(LanguageHelper.getString("label", languageMode)) },
            text = {
                OutlinedTextField(
                    value = tempLabel,
                    onValueChange = { tempLabel = it },
                    label = { Text("Label / Tag") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    labelTag = tempLabel
                    showLabelDialog = false
                }) {
                    Text(LanguageHelper.getString("save", languageMode))
                }
            },
            dismissButton = {
                TextButton(onClick = { showLabelDialog = false }) {
                    Text(LanguageHelper.getString("cancel", languageMode))
                }
            }
        )
    }

    if (showDeleteConfirmDialog && existingTransaction != null && onDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text(LanguageHelper.getString("delete", languageMode)) },
            text = { Text("Are you sure you want to delete this transaction?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete(existingTransaction)
                        showDeleteConfirmDialog = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(LanguageHelper.getString("delete", languageMode))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text(LanguageHelper.getString("cancel", languageMode))
                }
            }
        )
    }
}

@Composable
private fun OptionRowItem(
    icon: @Composable () -> Unit,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            icon()
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
        Icon(
            imageVector = Icons.Default.MoreHoriz,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(16.dp)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoryPickerModalDialog(
    categories: List<Category>,
    txType: TransactionType,
    selectedCategoryId: Long?,
    selectedSubCategoryId: Long?,
    languageMode: LanguageMode,
    onCategorySelected: (Long, Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val targetType = when (txType) {
        TransactionType.EXPENSE -> CategoryType.EXPENSE
        TransactionType.INCOME -> CategoryType.INCOME
        TransactionType.TRANSFER -> CategoryType.EXPENSE
    }
    val parentCategories = categories.filter { it.type == targetType && it.parentId == null && it.isActive }
    var expandedParentId by remember { mutableStateOf(selectedCategoryId ?: parentCategories.firstOrNull()?.id) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(LanguageHelper.getString("select_category_dialog", languageMode), fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                parentCategories.forEach { parent ->
                    val isSelected = expandedParentId == parent.id
                    val subCats = categories.filter { it.parentId == parent.id && it.isActive }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                expandedParentId = parent.id
                                if (subCats.isEmpty()) {
                                    onCategorySelected(parent.id, null)
                                }
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = IconHelper.getIconByName(parent.iconName),
                                        contentDescription = null,
                                        tint = SolidPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = parent.localizedName(languageMode),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                if (subCats.isEmpty()) {
                                    IconButton(onClick = { onCategorySelected(parent.id, null) }, modifier = Modifier.size(28.dp)) {
                                        Icon(Icons.Default.Check, contentDescription = "Select", tint = SolidPrimary)
                                    }
                                }
                            }

                            if (subCats.isNotEmpty() && isSelected) {
                                Spacer(modifier = Modifier.height(8.dp))
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    // Main category itself
                                    FilterChip(
                                        selected = selectedCategoryId == parent.id && selectedSubCategoryId == null,
                                        onClick = { onCategorySelected(parent.id, null) },
                                        label = { Text("Main / ${parent.localizedName(languageMode)}", fontSize = 11.sp) }
                                    )
                                    subCats.forEach { sub ->
                                        FilterChip(
                                            selected = selectedSubCategoryId == sub.id,
                                            onClick = { onCategorySelected(parent.id, sub.id) },
                                            label = { Text(sub.localizedName(languageMode), fontSize = 11.sp) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(LanguageHelper.getString("cancel", languageMode))
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AccountPickerModalDialog(
    accounts: List<Account>,
    txType: TransactionType,
    creditAccountId: Long?,
    debitAccountId: Long?,
    languageMode: LanguageMode,
    onAccountSelected: (Long?, Long?) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedCredit by remember { mutableStateOf(creditAccountId) }
    var selectedDebit by remember { mutableStateOf(debitAccountId) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(LanguageHelper.getString("select_account_dialog", languageMode), fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (txType == TransactionType.TRANSFER) {
                    Text("Source Account (From):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SolidExpense)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        accounts.forEach { acc ->
                            FilterChip(
                                selected = selectedCredit == acc.id,
                                onClick = { selectedCredit = acc.id },
                                label = { Text(acc.localizedName(languageMode), fontSize = 12.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Destination Account (To):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SolidIncome)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        accounts.forEach { acc ->
                            FilterChip(
                                selected = selectedDebit == acc.id,
                                onClick = { selectedDebit = acc.id },
                                label = { Text(acc.localizedName(languageMode), fontSize = 12.sp) }
                            )
                        }
                    }
                } else {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        accounts.forEach { acc ->
                            val isSelected = if (txType == TransactionType.EXPENSE) selectedCredit == acc.id else selectedDebit == acc.id
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    if (txType == TransactionType.EXPENSE) {
                                        selectedCredit = acc.id
                                        onAccountSelected(acc.id, null)
                                    } else {
                                        selectedDebit = acc.id
                                        onAccountSelected(null, acc.id)
                                    }
                                },
                                label = { Text(acc.localizedName(languageMode), fontSize = 12.sp) },
                                leadingIcon = {
                                    Icon(
                                        IconHelper.getIconByName(acc.iconName),
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (txType == TransactionType.TRANSFER) {
                Button(onClick = {
                    onAccountSelected(selectedCredit, selectedDebit)
                }) {
                    Text(LanguageHelper.getString("done", languageMode))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(LanguageHelper.getString("cancel", languageMode))
            }
        }
    )
}
