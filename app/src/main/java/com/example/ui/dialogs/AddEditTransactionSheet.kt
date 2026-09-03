package com.example.ui.dialogs

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.NorthWest
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.PopupProperties
import coil.compose.AsyncImage
import com.example.data.model.Account
import com.example.data.model.AccountType
import com.example.data.model.Category
import com.example.data.model.CategoryType
import com.example.data.model.LanguageMode
import com.example.data.model.Transaction
import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionType
import com.example.data.model.TransactionWithDetails
import com.example.ui.components.DatePickerModal
import com.example.ui.components.PopupCalculatorDialog
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidExpenseContainer
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidIncomeContainer
import com.example.ui.theme.SolidPrimary
import com.example.ui.theme.SolidPrimaryContainer
import com.example.ui.theme.SolidTransfer
import com.example.util.AutofillConfig
import com.example.util.AutofillPreferences
import com.example.util.DateUtils
import com.example.util.IconHelper
import com.example.util.LanguageHelper

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddEditTransactionSheet(
    accounts: List<Account>,
    categories: List<Category>,
    allTransactions: List<TransactionWithDetails> = emptyList(),
    languageMode: LanguageMode,
    existingTransaction: Transaction? = null,
    onDismiss: () -> Unit,
    onSave: (Transaction) -> Unit,
    onDelete: ((Transaction) -> Unit)? = null,
    onAddNewCategory: ((Category) -> Unit)? = null,
    onAddNewAccount: ((Account) -> Unit)? = null
) {
    val context = LocalContext.current
    val autofillPrefs = remember { AutofillPreferences.getInstance(context) }
    val autofillConfig by autofillPrefs.config.collectAsState()

    var txType by remember {
        mutableStateOf(existingTransaction?.type ?: TransactionType.EXPENSE)
    }

    var amount by remember {
        mutableDoubleStateOf(existingTransaction?.amount ?: 0.0)
    }

    var amountText by remember {
        mutableStateOf(
            if (existingTransaction != null && existingTransaction.amount > 0.0) {
                if (existingTransaction.amount % 1.0 == 0.0) existingTransaction.amount.toLong().toString() else existingTransaction.amount.toString()
            } else ""
        )
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

    var attachmentUri by remember {
        mutableStateOf(existingTransaction?.attachmentUri ?: "")
    }

    var status by remember {
        mutableStateOf(existingTransaction?.status ?: TransactionStatus.NONE)
    }

    var showNameDropdown by remember { mutableStateOf(false) }
    var keepFormOpen by remember { mutableStateOf(false) }

    var isNameFocused by remember { mutableStateOf(false) }
    var isAmountFocused by remember { mutableStateOf(false) }
    var isNoteFocused by remember { mutableStateOf(false) }

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
    var showAutofillSettingsDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    // Attachment Picker
    val attachmentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            attachmentUri = uri.toString()
        }
    }

    // Payee suggestions from previous entries with partial matching
    val pastPayees = remember(allTransactions) {
        allTransactions.mapNotNull { it.transaction.payeeOrPayer.takeIf { p -> p.isNotBlank() } }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .map { it.key }
    }
    val payeeSuggestions = remember(payee, pastPayees) {
        val q = payee.trim()
        if (q.isEmpty()) {
            pastPayees.take(8)
        } else {
            pastPayees.filter { it.contains(q, ignoreCase = true) }.take(8)
        }
    }

    // Usable Accounts
    val usableAccounts = remember(accounts) {
        val activeList = accounts.filter { it.isActive }
        val parentsWithChildren = activeList.filter { it.parentId != null }.mapNotNull { it.parentId }.toSet()
        activeList.filter { it.id !in parentsWithChildren }
    }

    // Auto-select default accounts if empty
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

    val othersCategoryGroup = remember(categories, txType) {
        val targetType = when (txType) {
            TransactionType.EXPENSE -> CategoryType.EXPENSE
            TransactionType.INCOME -> CategoryType.INCOME
            TransactionType.TRANSFER -> null
        }
        if (targetType != null) {
            categories.firstOrNull { it.type == targetType && it.parentId == null && it.nameEn.equals("Others", ignoreCase = true) }
                ?: relevantCategories.firstOrNull()
        } else null
    }

    if (selectedCategoryId == null && txType != TransactionType.TRANSFER) {
        selectedCategoryId = othersCategoryGroup?.id ?: relevantCategories.firstOrNull()?.id
    }

    val subCategoriesForSelectedGroup = remember(categories, selectedCategoryId) {
        if (selectedCategoryId != null) {
            categories.filter { it.parentId == selectedCategoryId && it.isActive }
        } else emptyList()
    }

    if (selectedSubCategoryId == null && subCategoriesForSelectedGroup.isNotEmpty() && txType != TransactionType.TRANSFER) {
        selectedSubCategoryId = subCategoriesForSelectedGroup.firstOrNull { it.nameEn.equals("Others", ignoreCase = true) }?.id
            ?: subCategoriesForSelectedGroup.first().id
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

    // Function to apply autofill when a payee suggestion is tapped
    fun onSelectPayeeSuggestion(suggestedPayee: String) {
        payee = suggestedPayee
        val latestMatch = allTransactions.firstOrNull {
            it.transaction.payeeOrPayer.equals(suggestedPayee, ignoreCase = true)
        }?.transaction

        if (latestMatch != null) {
            if (autofillConfig.autofillCategory && latestMatch.categoryId != null) {
                txType = latestMatch.type
                selectedCategoryId = latestMatch.categoryId
                selectedSubCategoryId = latestMatch.subCategoryId
            }
            if (autofillConfig.autofillAccount) {
                when (latestMatch.type) {
                    TransactionType.EXPENSE -> creditAccountId = latestMatch.creditAccountId
                    TransactionType.INCOME -> debitAccountId = latestMatch.debitAccountId
                    TransactionType.TRANSFER -> {
                        creditAccountId = latestMatch.creditAccountId
                        debitAccountId = latestMatch.debitAccountId
                    }
                }
            }
            if (autofillConfig.autofillAmount && latestMatch.amount > 0) {
                amount = latestMatch.amount
                amountText = if (latestMatch.amount % 1.0 == 0.0) latestMatch.amount.toLong().toString() else latestMatch.amount.toString()
            }
            if (autofillConfig.autofillNotes && latestMatch.note.isNotBlank()) {
                note = latestMatch.note
            }
            if (autofillConfig.autofillLabel && latestMatch.referenceNo.isNotBlank()) {
                labelTag = latestMatch.referenceNo
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
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
                    .imePadding()
            ) {
                // Top App Bar
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
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
                            // Autofill Settings Shortcut Icon
                            IconButton(
                                onClick = { showAutofillSettingsDialog = true },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Autofill Settings",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            // "+1" button: saves current entry and clears previous form's entered data for new entry
                            Surface(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        if (amount > 0) {
                                            val fallbackGroup = categories.firstOrNull {
                                                val targetType = if (txType == TransactionType.EXPENSE) CategoryType.EXPENSE else CategoryType.INCOME
                                                it.type == targetType && it.parentId == null && it.nameEn.equals("Others", ignoreCase = true)
                                            } ?: relevantCategories.firstOrNull()

                                            val finalCategoryId = if (txType != TransactionType.TRANSFER) {
                                                selectedCategoryId ?: fallbackGroup?.id
                                            } else null

                                            val finalSubCategoryId = if (txType != TransactionType.TRANSFER) {
                                                selectedSubCategoryId ?: categories.firstOrNull { it.parentId == finalCategoryId }?.id
                                            } else null

                                            val tx = Transaction(
                                                id = 0,
                                                type = txType,
                                                amount = amount,
                                                dateEpochMs = selectedDateEpochMs,
                                                note = note.trim(),
                                                referenceNo = labelTag.trim(),
                                                payeeOrPayer = payee.trim(),
                                                attachmentUri = attachmentUri.trim(),
                                                status = status,
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
                                                categoryId = finalCategoryId,
                                                subCategoryId = finalSubCategoryId
                                            )
                                            onSave(tx)
                                            // Clear previous form's entered data and keep open for new entry
                                            amount = 0.0
                                            amountText = ""
                                            payee = ""
                                            note = ""
                                            labelTag = ""
                                            attachmentUri = ""
                                            status = TransactionStatus.NONE
                                            showNameDropdown = false
                                            Toast.makeText(
                                                context,
                                                if (languageMode == LanguageMode.BANGLA) "সংরক্ষণ করা হয়েছে! পরবর্তী এন্ট্রি দিন" else "Saved! Enter next transaction",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            Toast.makeText(
                                                context,
                                                if (languageMode == LanguageMode.BANGLA) "অনুগ্রহ করে টাকার পরিমাণ দিন" else "Please enter an amount first",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    },
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                                shape = CircleShape
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "+1",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    // 1. Title / Payee Name Field with Dropdown Suggestions & Partial Matching
                    Column(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = payee,
                            onValueChange = {
                                payee = it
                                showNameDropdown = it.isNotBlank()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("tx_payee_input")
                                .onFocusChanged { isNameFocused = it.isFocused },
                            placeholder = {
                                Text(
                                    text = LanguageHelper.getString("payee_payer", languageMode).ifEmpty { "Name / Payee" },
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            },
                            trailingIcon = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (payee.isNotBlank()) {
                                        IconButton(
                                            onClick = {
                                                payee = ""
                                                showNameDropdown = false
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Clear",
                                                tint = MaterialTheme.colorScheme.outline,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    IconButton(onClick = { attachmentPickerLauncher.launch("*/*") }) {
                                        Icon(
                                            Icons.Default.AttachFile,
                                            contentDescription = "Attach File/Image",
                                            tint = if (attachmentUri.isNotBlank()) SolidPrimary else MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Inline Dropdown Suggestions: Does not open a popup window, so keyboard NEVER closes!
                        if (showNameDropdown && payeeSuggestions.isNotEmpty()) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                tonalElevation = 6.dp,
                                shadowElevation = 4.dp,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    payeeSuggestions.take(5).forEach { suggestion ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    payee = suggestion
                                                    showNameDropdown = false
                                                    onSelectPayeeSuggestion(suggestion)
                                                }
                                                .padding(horizontal = 14.dp, vertical = 9.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.History,
                                                    contentDescription = null,
                                                    tint = SolidPrimary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text(
                                                    text = suggestion,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            Icon(
                                                imageVector = Icons.Default.NorthWest,
                                                contentDescription = "Autofill",
                                                tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Attachment Preview Bar if present
                    if (attachmentUri.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AttachFile,
                                        contentDescription = null,
                                        tint = SolidPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = LanguageHelper.getString("attachment", languageMode) + ": " + attachmentUri.substringAfterLast('/'),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                IconButton(
                                    onClick = { attachmentUri = "" },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 2. Date, Time & Schedule Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showDatePicker = true }
                                .padding(horizontal = 4.dp, vertical = 4.dp)
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
                                .padding(horizontal = 4.dp, vertical = 4.dp)
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

                    Spacer(modifier = Modifier.height(10.dp))

                    // 3. Amount Container Card
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.5.dp, typePrimaryColor.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
                        color = typeContainerColor.copy(alpha = 0.35f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Type sign circular indicator (clickable to toggle between - and +)
                            Surface(
                                shape = CircleShape,
                                color = typePrimaryColor,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        txType = when (txType) {
                                            TransactionType.EXPENSE -> TransactionType.INCOME
                                            TransactionType.INCOME -> TransactionType.EXPENSE
                                            TransactionType.TRANSFER -> TransactionType.EXPENSE
                                        }
                                        if (txType != TransactionType.TRANSFER) {
                                            val targetType = if (txType == TransactionType.EXPENSE) CategoryType.EXPENSE else CategoryType.INCOME
                                            val currentCat = categories.firstOrNull { it.id == selectedCategoryId }
                                            if (currentCat == null || currentCat.type != targetType) {
                                                val relevant = categories.filter { it.type == targetType && it.parentId == null && it.isActive }
                                                val defaultGroup = relevant.firstOrNull { it.nameEn.equals("Others", ignoreCase = true) } ?: relevant.firstOrNull()
                                                selectedCategoryId = defaultGroup?.id
                                                val subs = if (defaultGroup != null) categories.filter { it.parentId == defaultGroup.id && it.isActive } else emptyList()
                                                selectedSubCategoryId = subs.firstOrNull()?.id
                                            }
                                        }
                                    }
                                    .testTag("tx_sign_toggle_btn")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    val signText = when (txType) {
                                        TransactionType.EXPENSE -> "−"
                                        TransactionType.INCOME -> "+"
                                        TransactionType.TRANSFER -> "⇄"
                                    }
                                    Text(
                                        text = signText,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Direct Numeric Amount Input
                            BasicTextField(
                                value = amountText,
                                onValueChange = { input ->
                                    val clean = input.filter { it.isDigit() || it == '.' }
                                    if (clean.count { it == '.' } <= 1 && clean.length <= 12) {
                                        amountText = clean
                                        amount = clean.toDoubleOrNull() ?: 0.0
                                    }
                                },
                                textStyle = TextStyle(
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = typePrimaryColor
                                ),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal,
                                    imeAction = ImeAction.Done
                                ),
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("tx_amount_input")
                                    .onFocusChanged { isAmountFocused = it.isFocused },
                                decorationBox = { innerTextField ->
                                    Box(contentAlignment = Alignment.CenterStart) {
                                        if (amountText.isEmpty()) {
                                            Text(
                                                text = "0",
                                                fontSize = 26.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = typePrimaryColor.copy(alpha = 0.35f)
                                            )
                                        }
                                        innerTextField()
                                    }
                                }
                            )

                            // Calculator Icon Button
                            IconButton(
                                onClick = { showCalculator = true },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    Icons.Default.Calculate,
                                    contentDescription = "Calculator",
                                    tint = typePrimaryColor,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            // Currency Badge Pill
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = typePrimaryColor.copy(alpha = 0.15f)
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

                    Spacer(modifier = Modifier.height(14.dp))

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

                            // Row B: Account (Showing Account Group Name with Account)
                            val formatAccountWithGroup: (Account?) -> String = { acc ->
                                if (acc == null) {
                                    LanguageHelper.getString("select_account", languageMode)
                                } else {
                                    val parent = if (acc.parentId != null) accounts.firstOrNull { it.id == acc.parentId } else null
                                    if (parent != null) {
                                        "${parent.localizedName(languageMode)} / ${acc.localizedName(languageMode)}"
                                    } else {
                                        acc.localizedName(languageMode)
                                    }
                                }
                            }

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
                                    TransactionType.EXPENSE -> formatAccountWithGroup(selectedCreditAccount)
                                    TransactionType.INCOME -> formatAccountWithGroup(selectedDebitAccount)
                                    TransactionType.TRANSFER -> {
                                        val from = formatAccountWithGroup(selectedCreditAccount)
                                        val to = formatAccountWithGroup(selectedDebitAccount)
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

                            // Row D: Status (None / Cleared / Void / Reconciled - Default: None)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = when (status) {
                                            TransactionStatus.NONE -> Icons.Default.RadioButtonUnchecked
                                            TransactionStatus.CLEARED -> Icons.Default.CheckCircle
                                            TransactionStatus.VOID -> Icons.Default.Cancel
                                            TransactionStatus.RECONCILED -> Icons.Default.Lock
                                        },
                                        contentDescription = "Status",
                                        tint = when (status) {
                                            TransactionStatus.NONE -> MaterialTheme.colorScheme.outline
                                            TransactionStatus.CLEARED -> SolidIncome
                                            TransactionStatus.VOID -> SolidExpense
                                            TransactionStatus.RECONCILED -> SolidPrimary
                                        },
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
                                        val activeColor = when (st) {
                                            TransactionStatus.NONE -> MaterialTheme.colorScheme.outline
                                            TransactionStatus.CLEARED -> SolidIncome
                                            TransactionStatus.VOID -> SolidExpense
                                            TransactionStatus.RECONCILED -> SolidPrimary
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (isSel) activeColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                            border = if (isSel) BorderStroke(1.dp, activeColor) else null,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .clickable { status = st }
                                        ) {
                                            Text(
                                                text = when (st) {
                                                    TransactionStatus.NONE -> LanguageHelper.getString("none", languageMode)
                                                    TransactionStatus.CLEARED -> LanguageHelper.getString("cleared", languageMode)
                                                    TransactionStatus.VOID -> LanguageHelper.getString("void", languageMode)
                                                    TransactionStatus.RECONCILED -> LanguageHelper.getString("reconciled", languageMode)
                                                },
                                                fontSize = 10.sp,
                                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSel) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
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
                                        imageVector = Icons.AutoMirrored.Filled.Label,
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

                    Spacer(modifier = Modifier.height(14.dp))

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
                                .padding(4.dp)
                                .onFocusChanged { isNoteFocused = it.isFocused },
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

                // Bottom Action Bar: Type Selector Pills or Keyboard Accessory Toolbar (Pinned above Keyboard)
                val isKeyboardOpen = WindowInsets.isImeVisible || isNameFocused || isAmountFocused || isNoteFocused

                val executeSave: () -> Unit = {
                    if (amount > 0) {
                        val fallbackGroup = categories.firstOrNull {
                            val targetType = if (txType == TransactionType.EXPENSE) CategoryType.EXPENSE else CategoryType.INCOME
                            it.type == targetType && it.parentId == null && it.nameEn.equals("Others", ignoreCase = true)
                        } ?: relevantCategories.firstOrNull()

                        val finalCategoryId = if (txType != TransactionType.TRANSFER) {
                            selectedCategoryId ?: fallbackGroup?.id
                        } else null

                        val finalSubCategoryId = if (txType != TransactionType.TRANSFER) {
                            selectedSubCategoryId ?: categories.firstOrNull { it.parentId == finalCategoryId }?.id
                        } else null

                        val tx = Transaction(
                            id = existingTransaction?.id ?: 0,
                            type = txType,
                            amount = amount,
                            dateEpochMs = selectedDateEpochMs,
                            note = note.trim(),
                            referenceNo = labelTag.trim(),
                            payeeOrPayer = payee.trim(),
                            attachmentUri = attachmentUri.trim(),
                            status = status,
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
                            categoryId = finalCategoryId,
                            subCategoryId = finalSubCategoryId
                        )
                        onSave(tx)
                        if (keepFormOpen && existingTransaction == null) {
                            amount = 0.0
                            amountText = ""
                            note = ""
                            payee = ""
                            attachmentUri = ""
                        } else {
                            onDismiss()
                        }
                    } else {
                        Toast.makeText(
                            context,
                            if (languageMode == LanguageMode.BANGLA) "অনুগ্রহ করে টাকার পরিমাণ দিন" else "Please enter an amount",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isKeyboardOpen) {
                        // When keyboard is focused: show three buttons just above keyboard on right side: Expense icon, Income icon, Transfer icon, Save button
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End
                        ) {
                            // Expense icon button
                            Surface(
                                shape = CircleShape,
                                color = if (txType == TransactionType.EXPENSE) SolidExpense else SolidExpense.copy(alpha = 0.12f),
                                border = if (txType == TransactionType.EXPENSE) null else BorderStroke(1.dp, SolidExpense.copy(alpha = 0.35f)),
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        txType = TransactionType.EXPENSE
                                        val currentCat = categories.firstOrNull { it.id == selectedCategoryId }
                                        if (currentCat == null || currentCat.type != CategoryType.EXPENSE) {
                                            val relevant = categories.filter { it.type == CategoryType.EXPENSE && it.parentId == null && it.isActive }
                                            val defaultGroup = relevant.firstOrNull { it.nameEn.equals("Others", ignoreCase = true) } ?: relevant.firstOrNull()
                                            selectedCategoryId = defaultGroup?.id
                                            val subs = if (defaultGroup != null) categories.filter { it.parentId == defaultGroup.id && it.isActive } else emptyList()
                                            selectedSubCategoryId = subs.firstOrNull()?.id
                                        }
                                    }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDownward,
                                        contentDescription = "Expense",
                                        tint = if (txType == TransactionType.EXPENSE) Color.White else SolidExpense,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Income icon button
                            Surface(
                                shape = CircleShape,
                                color = if (txType == TransactionType.INCOME) SolidIncome else SolidIncome.copy(alpha = 0.12f),
                                border = if (txType == TransactionType.INCOME) null else BorderStroke(1.dp, SolidIncome.copy(alpha = 0.35f)),
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        txType = TransactionType.INCOME
                                        val currentCat = categories.firstOrNull { it.id == selectedCategoryId }
                                        if (currentCat == null || currentCat.type != CategoryType.INCOME) {
                                            val relevant = categories.filter { it.type == CategoryType.INCOME && it.parentId == null && it.isActive }
                                            val defaultGroup = relevant.firstOrNull { it.nameEn.equals("Others", ignoreCase = true) } ?: relevant.firstOrNull()
                                            selectedCategoryId = defaultGroup?.id
                                            val subs = if (defaultGroup != null) categories.filter { it.parentId == defaultGroup.id && it.isActive } else emptyList()
                                            selectedSubCategoryId = subs.firstOrNull()?.id
                                        }
                                    }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowUpward,
                                        contentDescription = "Income",
                                        tint = if (txType == TransactionType.INCOME) Color.White else SolidIncome,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Transfer icon button
                            Surface(
                                shape = CircleShape,
                                color = if (txType == TransactionType.TRANSFER) SolidTransfer else SolidTransfer.copy(alpha = 0.12f),
                                border = if (txType == TransactionType.TRANSFER) null else BorderStroke(1.dp, SolidTransfer.copy(alpha = 0.35f)),
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        txType = TransactionType.TRANSFER
                                        selectedCategoryId = null
                                        selectedSubCategoryId = null
                                    }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.SwapHoriz,
                                        contentDescription = "Transfer",
                                        tint = if (txType == TransactionType.TRANSFER) Color.White else SolidTransfer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            // Save button
                            FloatingActionButton(
                                onClick = executeSave,
                                containerColor = typePrimaryColor,
                                contentColor = Color.White,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .size(38.dp)
                                    .testTag("save_transaction_btn_keyboard")
                            ) {
                                Icon(Icons.Default.Check, contentDescription = "Save", modifier = Modifier.size(20.dp))
                            }
                        }
                    } else {
                        // Standard Bottom Action Bar: Type Selector Pills + Save FAB
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 8.dp),
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
                                    Triple(TransactionType.EXPENSE, "− " + (LanguageHelper.getString("expense", languageMode).ifEmpty { "Expense" }).uppercase(), SolidExpense),
                                    Triple(TransactionType.INCOME, "+ " + (LanguageHelper.getString("income", languageMode).ifEmpty { "Income" }).uppercase(), SolidIncome),
                                    Triple(TransactionType.TRANSFER, "⇄ " + (LanguageHelper.getString("transfer", languageMode).ifEmpty { "Transfer" }).uppercase(), SolidTransfer)
                                )

                                types.forEach { (type, label, color) ->
                                    val isSelected = txType == type
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) color else color.copy(alpha = 0.12f),
                                        border = if (isSelected) null else BorderStroke(1.dp, color.copy(alpha = 0.25f)),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable {
                                                txType = type
                                                if (type != TransactionType.TRANSFER) {
                                                    val targetType = if (type == TransactionType.EXPENSE) CategoryType.EXPENSE else CategoryType.INCOME
                                                    val relevant = categories.filter { it.type == targetType && it.parentId == null && it.isActive }
                                                    val othersCat = relevant.firstOrNull { it.nameEn.equals("Others", ignoreCase = true) } ?: relevant.firstOrNull()
                                                    selectedCategoryId = othersCat?.id
                                                    val subs = if (othersCat != null) categories.filter { it.parentId == othersCat.id && it.isActive } else emptyList()
                                                    selectedSubCategoryId = subs.firstOrNull { it.nameEn.equals("Others", ignoreCase = true) }?.id ?: subs.firstOrNull()?.id
                                                } else {
                                                    selectedCategoryId = null
                                                    selectedSubCategoryId = null
                                                }
                                            }
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else color,
                                            textAlign = TextAlign.Center,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 9.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            // Save Floating Action Button
                            FloatingActionButton(
                                onClick = executeSave,
                                containerColor = typePrimaryColor,
                                contentColor = Color.White,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .size(46.dp)
                                    .testTag("save_transaction_btn")
                            ) {
                                Icon(Icons.Default.Check, contentDescription = "Save", modifier = Modifier.size(24.dp))
                            }
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
                amountText = if (calculatedAmount % 1.0 == 0.0) calculatedAmount.toLong().toString() else calculatedAmount.toString()
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
            onAddNewCategory = { newCat ->
                onAddNewCategory?.invoke(newCat)
                selectedCategoryId = newCat.parentId ?: newCat.id
                selectedSubCategoryId = if (newCat.parentId != null) newCat.id else null
                showCategoryPickerModal = false
            },
            onDismiss = { showCategoryPickerModal = false }
        )
    }

    if (showAccountPickerModal) {
        AccountPickerModalDialog(
            accounts = usableAccounts,
            allAccounts = accounts,
            txType = txType,
            creditAccountId = creditAccountId,
            debitAccountId = debitAccountId,
            languageMode = languageMode,
            onAccountSelected = { sourceId, destId ->
                creditAccountId = sourceId
                debitAccountId = destId
                showAccountPickerModal = false
            },
            onAddNewAccount = { newAcc ->
                onAddNewAccount?.invoke(newAcc)
                if (txType == TransactionType.EXPENSE) creditAccountId = newAcc.id
                else debitAccountId = newAcc.id
                showAccountPickerModal = false
            },
            onDismiss = { showAccountPickerModal = false }
        )
    }

    if (showLabelDialog) {
        val existingLabels = remember(allTransactions) {
            allTransactions.flatMap { tx ->
                tx.transaction.referenceNo.split(",").map { it.trim() }.filter { it.isNotBlank() }
            }.distinct()
        }
        LabelPickerModalDialog(
            currentLabel = labelTag,
            existingLabels = existingLabels,
            languageMode = languageMode,
            onLabelSelected = {
                labelTag = it
                showLabelDialog = false
            },
            onDismiss = { showLabelDialog = false }
        )
    }

    if (showAutofillSettingsDialog) {
        AutofillSettingsDialog(
            config = autofillConfig,
            languageMode = languageMode,
            onConfigChange = { autofillPrefs.updateConfig(it) },
            onDismiss = { showAutofillSettingsDialog = false }
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

private fun parseItemColor(hex: String?, fallback: Color = Color(0xFFEA580C)): Color {
    if (hex.isNullOrBlank()) return fallback
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (_: Exception) {
        fallback
    }
}

@Composable
private fun CategoryPickerModalDialog(
    categories: List<Category>,
    txType: TransactionType,
    selectedCategoryId: Long?,
    selectedSubCategoryId: Long?,
    languageMode: LanguageMode,
    onCategorySelected: (Long, Long?) -> Unit,
    onAddNewCategory: (Category) -> Unit,
    onDismiss: () -> Unit
) {
    val targetType = when (txType) {
        TransactionType.EXPENSE -> CategoryType.EXPENSE
        TransactionType.INCOME -> CategoryType.INCOME
        TransactionType.TRANSFER -> CategoryType.EXPENSE
    }
    val activeCategories = remember(categories, targetType) {
        categories.filter { it.type == targetType && it.isActive }
    }
    val parentCategories = remember(activeCategories) {
        activeCategories.filter { it.parentId == null }
    }

    var searchQuery by remember { mutableStateOf("") }
    var showInlineCreateCategory by remember { mutableStateOf(false) }

    if (showInlineCreateCategory) {
        QuickCreateCategoryDialog(
            parentCategories = parentCategories,
            targetType = targetType,
            languageMode = languageMode,
            onDismiss = { showInlineCreateCategory = false },
            onCategoryCreated = {
                onAddNewCategory(it)
                showInlineCreateCategory = false
            }
        )
        return
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .clickable(enabled = false) {},
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Top Drag Handle
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .width(38.dp)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                            .align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Top Search Bar with Green "New" Button
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = 8.dp),
                                singleLine = true,
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                decorationBox = { innerTextField ->
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            text = LanguageHelper.getString("search", languageMode).ifEmpty { "Search" },
                                            color = MaterialTheme.colorScheme.outline,
                                            fontSize = 14.sp
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { searchQuery = "" },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tint = MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            // Green "New" Pill Button
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0xFF2E7D32),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { showInlineCreateCategory = true }
                            ) {
                                Text(
                                    text = "New",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Scrollable Category Groups Grid
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 14.dp)
                    ) {
                        val query = searchQuery.trim().lowercase()

                        if (query.isNotEmpty()) {
                            // Search Results: Only search subcategories under groups
                            val matchingCategories = activeCategories.filter { cat ->
                                cat.parentId != null && (
                                    cat.nameEn.lowercase().contains(query) ||
                                    cat.nameBn.lowercase().contains(query)
                                )
                            }
                            if (matchingCategories.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = LanguageHelper.getString("no_results_found", languageMode).ifEmpty { "No categories found" },
                                        color = MaterialTheme.colorScheme.outline,
                                        fontSize = 14.sp
                                    )
                                }
                            } else {
                                Text(
                                    text = "Search Results",
                                    color = Color(0xFF2E7D32),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    textAlign = TextAlign.Center
                                )
                                Category3ColumnGrid(
                                    items = matchingCategories,
                                    selectedCategoryId = selectedCategoryId,
                                    selectedSubCategoryId = selectedSubCategoryId,
                                    languageMode = languageMode,
                                    onItemClick = { cat ->
                                        if (cat.parentId != null) {
                                            onCategorySelected(cat.parentId, cat.id)
                                        }
                                    }
                                )
                            }
                        } else {
                            // Display by Groups (Group name appears only once at middle as group name; no duplicate group name in categories)
                            parentCategories.forEach { parent ->
                                val subCats = activeCategories
                                    .filter { it.parentId == parent.id }
                                    .filter { sub ->
                                        // Remove duplicate if identical to group name unless it's the sole subcategory
                                        !sub.nameEn.equals(parent.nameEn, ignoreCase = true) ||
                                        activeCategories.count { it.parentId == parent.id } == 1
                                    }

                                if (subCats.isNotEmpty()) {
                                    Text(
                                        text = "➤ ${parent.localizedName(languageMode)}",
                                        color = Color(0xFF2E7D32),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 10.dp, bottom = 12.dp),
                                        textAlign = TextAlign.Center
                                    )

                                    Category3ColumnGrid(
                                        items = subCats,
                                        selectedCategoryId = selectedCategoryId,
                                        selectedSubCategoryId = selectedSubCategoryId,
                                        languageMode = languageMode,
                                        onItemClick = { cat ->
                                            onCategorySelected(parent.id, cat.id)
                                        }
                                    )
                                    Spacer(modifier = Modifier.height(18.dp))
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
private fun Category3ColumnGrid(
    items: List<Category>,
    selectedCategoryId: Long?,
    selectedSubCategoryId: Long?,
    languageMode: LanguageMode,
    onItemClick: (Category) -> Unit
) {
    val rows = items.chunked(3)
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        rows.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                for (i in 0 until 3) {
                    if (i < rowItems.size) {
                        val cat = rowItems[i]
                        val isSelected = if (cat.parentId != null) {
                            selectedSubCategoryId == cat.id
                        } else {
                            selectedCategoryId == cat.id && selectedSubCategoryId == null
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onItemClick(cat) }
                                .padding(horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(contentAlignment = Alignment.TopEnd) {
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(CircleShape)
                                        .background(parseItemColor(cat.colorHex)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = IconHelper.getIconByName(cat.iconName),
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF2E7D32))
                                            .border(1.5.dp, Color.White, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = Color.White,
                                            modifier = Modifier.size(11.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = cat.localizedName(languageMode),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuickCreateCategoryDialog(
    parentCategories: List<Category>,
    targetType: CategoryType,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onCategoryCreated: (Category) -> Unit
) {
    var createMode by remember { mutableStateOf(if (parentCategories.isNotEmpty()) 0 else 1) } // 0 = Category under Group, 1 = New Group
    var nameEn by remember { mutableStateOf("") }
    var nameBn by remember { mutableStateOf("") }
    var selectedParentId by remember { mutableStateOf<Long?>(parentCategories.firstOrNull()?.id) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = if (createMode == 0) "Create Category" else "Create Category Group",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = createMode == 0,
                        onClick = { createMode = 0 },
                        label = { Text("Sub-Category", fontSize = 12.sp) }
                    )
                    FilterChip(
                        selected = createMode == 1,
                        onClick = { createMode = 1 },
                        label = { Text("New Group", fontSize = 12.sp) }
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = nameEn,
                    onValueChange = { nameEn = it },
                    label = { Text(if (createMode == 0) "Category Name (English)" else "Group Name (English)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = nameBn,
                    onValueChange = { nameBn = it },
                    label = { Text("Name (Bangla - Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (createMode == 0) {
                    if (parentCategories.isEmpty()) {
                        Text(
                            text = "No category groups exist yet. Please switch to 'New Group' first.",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    } else {
                        Text("Connect to Group (Required):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            parentCategories.forEach { parent ->
                                FilterChip(
                                    selected = selectedParentId == parent.id,
                                    onClick = { selectedParentId = parent.id },
                                    label = { Text(parent.localizedName(languageMode), fontSize = 12.sp) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = IconHelper.getIconByName(parent.iconName),
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            val canSave = nameEn.isNotBlank() && (createMode == 1 || (createMode == 0 && selectedParentId != null))
            Button(
                onClick = {
                    if (canSave) {
                        val parentCat = if (createMode == 0) parentCategories.firstOrNull { it.id == selectedParentId } else null
                        val newCat = Category(
                            nameEn = nameEn.trim(),
                            nameBn = nameBn.trim().ifEmpty { nameEn.trim() },
                            type = targetType,
                            parentId = if (createMode == 0) selectedParentId else null,
                            iconName = parentCat?.iconName ?: "Category",
                            colorHex = parentCat?.colorHex ?: "#2563EB"
                        )
                        onCategoryCreated(newCat)
                    }
                },
                enabled = canSave
            ) {
                Text(LanguageHelper.getString("save", languageMode))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(LanguageHelper.getString("cancel", languageMode))
            }
        }
    )
}

@Composable
private fun AccountPickerModalDialog(
    accounts: List<Account>,
    allAccounts: List<Account>,
    txType: TransactionType,
    creditAccountId: Long?,
    debitAccountId: Long?,
    languageMode: LanguageMode,
    onAccountSelected: (Long?, Long?) -> Unit,
    onAddNewAccount: (Account) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedCredit by remember { mutableStateOf(creditAccountId) }
    var selectedDebit by remember { mutableStateOf(debitAccountId) }
    var transferTab by remember { mutableStateOf(0) } // 0 = Source (From), 1 = Destination (To)
    var searchQuery by remember { mutableStateOf("") }
    var showInlineCreateAccount by remember { mutableStateOf(false) }

    if (showInlineCreateAccount) {
        val parentAccounts = allAccounts.filter { it.parentId == null }
        QuickCreateAccountDialog(
            parentAccounts = parentAccounts,
            languageMode = languageMode,
            onDismiss = { showInlineCreateAccount = false },
            onAccountCreated = {
                onAddNewAccount(it)
                showInlineCreateAccount = false
            }
        )
        return
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .clickable(enabled = false) {},
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Top Drag Handle
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .width(38.dp)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                            .align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Transfer Tabs (if transfer)
                    if (txType == TransactionType.TRANSFER) {
                        val fromAcc = accounts.firstOrNull { it.id == selectedCredit }
                        val toAcc = accounts.firstOrNull { it.id == selectedDebit }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { transferTab = 0 },
                                color = if (transferTab == 0) SolidExpense.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                border = if (transferTab == 0) BorderStroke(1.5.dp, SolidExpense) else null,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("From (Source)", fontSize = 11.sp, color = SolidExpense, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = fromAcc?.localizedName(languageMode) ?: "Select",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { transferTab = 1 },
                                color = if (transferTab == 1) SolidIncome.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                border = if (transferTab == 1) BorderStroke(1.5.dp, SolidIncome) else null,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("To (Destination)", fontSize = 11.sp, color = SolidIncome, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = toAcc?.localizedName(languageMode) ?: "Select",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Top Search Bar with Green "New" Button
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = 8.dp),
                                singleLine = true,
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                decorationBox = { innerTextField ->
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            text = LanguageHelper.getString("search", languageMode).ifEmpty { "Search" },
                                            color = MaterialTheme.colorScheme.outline,
                                            fontSize = 14.sp
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { searchQuery = "" },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tint = MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            // Green "New" Pill Button
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0xFF2E7D32),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { showInlineCreateAccount = true }
                            ) {
                                Text(
                                    text = "New",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Content Scrollable
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 14.dp)
                    ) {
                        val query = searchQuery.trim().lowercase()
                        val currentTargetSelectedId = when (txType) {
                            TransactionType.EXPENSE -> selectedCredit
                            TransactionType.INCOME -> selectedDebit
                            TransactionType.TRANSFER -> if (transferTab == 0) selectedCredit else selectedDebit
                        }

                        val onPickAccount: (Account) -> Unit = { acc ->
                            when (txType) {
                                TransactionType.EXPENSE -> {
                                    onAccountSelected(acc.id, null)
                                }
                                TransactionType.INCOME -> {
                                    onAccountSelected(null, acc.id)
                                }
                                TransactionType.TRANSFER -> {
                                    if (transferTab == 0) {
                                        selectedCredit = acc.id
                                        transferTab = 1
                                    } else {
                                        selectedDebit = acc.id
                                        onAccountSelected(selectedCredit, acc.id)
                                    }
                                }
                            }
                        }

                        if (query.isNotEmpty()) {
                            val filtered = accounts.filter {
                                it.nameEn.lowercase().contains(query) ||
                                        it.nameBn.lowercase().contains(query)
                            }
                            if (filtered.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = LanguageHelper.getString("no_results_found", languageMode).ifEmpty { "No accounts found" },
                                        color = MaterialTheme.colorScheme.outline,
                                        fontSize = 14.sp
                                    )
                                }
                            } else {
                                Text(
                                    text = "Search Results",
                                    color = Color(0xFF2E7D32),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    textAlign = TextAlign.Center
                                )
                                Account3ColumnGrid(
                                    items = filtered,
                                    allAccounts = allAccounts,
                                    selectedAccountId = currentTargetSelectedId,
                                    languageMode = languageMode,
                                    onItemClick = onPickAccount
                                )
                            }
                        } else {
                            // Group accounts by parent account or account type
                            val parentAccounts = allAccounts.filter { it.parentId == null }
                            parentAccounts.forEach { parent ->
                                val childAccounts = accounts.filter { it.parentId == parent.id }
                                val groupItems = if (childAccounts.isNotEmpty()) {
                                    childAccounts
                                } else if (accounts.any { it.id == parent.id }) {
                                    listOf(parent)
                                } else {
                                    emptyList()
                                }

                                if (groupItems.isNotEmpty()) {
                                    Text(
                                        text = "➤ ${parent.localizedName(languageMode)}",
                                        color = Color(0xFF2E7D32),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 10.dp, bottom = 12.dp),
                                        textAlign = TextAlign.Center
                                    )
                                    Account3ColumnGrid(
                                        items = groupItems,
                                        allAccounts = allAccounts,
                                        selectedAccountId = currentTargetSelectedId,
                                        languageMode = languageMode,
                                        onItemClick = onPickAccount
                                    )
                                    Spacer(modifier = Modifier.height(18.dp))
                                }
                            }

                            // Any standalone accounts without parent in group list
                            val parentIds = parentAccounts.map { it.id }.toSet()
                            val unassigned = accounts.filter { it.parentId == null && it.id !in parentIds }
                            if (unassigned.isNotEmpty()) {
                                Text(
                                    text = "Others",
                                    color = Color(0xFF2E7D32),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 10.dp, bottom = 12.dp),
                                    textAlign = TextAlign.Center
                                )
                                Account3ColumnGrid(
                                    items = unassigned,
                                    allAccounts = allAccounts,
                                    selectedAccountId = currentTargetSelectedId,
                                    languageMode = languageMode,
                                    onItemClick = onPickAccount
                                )
                                Spacer(modifier = Modifier.height(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Account3ColumnGrid(
    items: List<Account>,
    allAccounts: List<Account> = emptyList(),
    selectedAccountId: Long?,
    languageMode: LanguageMode,
    onItemClick: (Account) -> Unit
) {
    val rows = items.chunked(3)
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        rows.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                for (i in 0 until 3) {
                    if (i < rowItems.size) {
                        val acc = rowItems[i]
                        val isSelected = selectedAccountId == acc.id
                        val parentGroup = if (acc.parentId != null) allAccounts.firstOrNull { it.id == acc.parentId } else null

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onItemClick(acc) }
                                .padding(horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(contentAlignment = Alignment.TopEnd) {
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(CircleShape)
                                        .background(parseItemColor(acc.colorHex, Color(0xFF2563EB))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = IconHelper.getIconByName(acc.iconName),
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF2E7D32))
                                            .border(1.5.dp, Color.White, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = Color.White,
                                            modifier = Modifier.size(11.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = acc.localizedName(languageMode),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.fillMaxWidth()
                            )
                            if (parentGroup != null) {
                                Text(
                                    text = parentGroup.localizedName(languageMode),
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Normal,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuickCreateAccountDialog(
    parentAccounts: List<Account>,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onAccountCreated: (Account) -> Unit
) {
    var createMode by remember { mutableStateOf(if (parentAccounts.isNotEmpty()) 0 else 1) } // 0 = Sub-Account, 1 = Account Group
    var nameEn by remember { mutableStateOf("") }
    var nameBn by remember { mutableStateOf("") }
    var selectedParentId by remember { mutableStateOf<Long?>(parentAccounts.firstOrNull()?.id) }
    var newGroupType by remember { mutableStateOf(AccountType.ASSET) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = if (createMode == 0) "Create Account" else "Create Account Group",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = createMode == 0,
                        onClick = { createMode = 0 },
                        label = { Text("Sub-Account", fontSize = 12.sp) }
                    )
                    FilterChip(
                        selected = createMode == 1,
                        onClick = { createMode = 1 },
                        label = { Text("New Group", fontSize = 12.sp) }
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = nameEn,
                    onValueChange = { nameEn = it },
                    label = { Text(if (createMode == 0) "Account Name (English)" else "Account Group Name (English)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = nameBn,
                    onValueChange = { nameBn = it },
                    label = { Text("Name (Bangla - Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (createMode == 0) {
                    if (parentAccounts.isEmpty()) {
                        Text(
                            text = "No account groups exist yet. Please create a Group first.",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    } else {
                        Text("Connect to Account Group (Required):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            parentAccounts.forEach { parent ->
                                FilterChip(
                                    selected = selectedParentId == parent.id,
                                    onClick = { selectedParentId = parent.id },
                                    label = { Text(parent.localizedName(languageMode), fontSize = 12.sp) },
                                    leadingIcon = {
                                        Icon(
                                            IconHelper.getIconByName(parent.iconName),
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                )
                            }
                        }
                    }
                } else {
                    Text("Account Group Nature:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = newGroupType == AccountType.ASSET,
                            onClick = { newGroupType = AccountType.ASSET },
                            label = { Text("Asset / Wallet / Bank", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = newGroupType == AccountType.LIABILITY,
                            onClick = { newGroupType = AccountType.LIABILITY },
                            label = { Text("Liability / Loan / Card", fontSize = 11.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            val canSave = nameEn.isNotBlank() && (createMode == 1 || (createMode == 0 && selectedParentId != null))
            Button(
                onClick = {
                    if (canSave) {
                        val parentAcc = if (createMode == 0) parentAccounts.firstOrNull { it.id == selectedParentId } else null
                        val newAcc = Account(
                            nameEn = nameEn.trim(),
                            nameBn = nameBn.trim().ifEmpty { nameEn.trim() },
                            type = if (createMode == 0) (parentAcc?.type ?: AccountType.ASSET) else newGroupType,
                            parentId = if (createMode == 0) selectedParentId else null,
                            iconName = parentAcc?.iconName ?: (if (newGroupType == AccountType.ASSET) "AccountBalance" else "CreditCard"),
                            colorHex = parentAcc?.colorHex ?: (if (newGroupType == AccountType.ASSET) "#2563EB" else "#DC2626")
                        )
                        onAccountCreated(newAcc)
                    }
                },
                enabled = canSave
            ) {
                Text(LanguageHelper.getString("save", languageMode))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(LanguageHelper.getString("cancel", languageMode))
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LabelPickerModalDialog(
    currentLabel: String,
    existingLabels: List<String>,
    languageMode: LanguageMode,
    onLabelSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val initialSelected = remember(currentLabel) {
        currentLabel.split(",").map { it.trim() }.filter { it.isNotBlank() }.toSet()
    }
    val selectedLabels = remember { mutableStateListOf<String>().apply { addAll(initialSelected) } }
    val labelPool = remember { mutableStateListOf<String>().apply { addAll(existingLabels.distinct()) } }
    var inputQuery by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Label,
                        contentDescription = null,
                        tint = SolidPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = LanguageHelper.getString("label", languageMode),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
                if (selectedLabels.isNotEmpty()) {
                    TextButton(onClick = { selectedLabels.clear() }) {
                        Text(
                            text = LanguageHelper.getString("clear", languageMode).ifEmpty { "Clear" },
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Input Field for Searching or Adding New Label
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputQuery,
                        onValueChange = { inputQuery = it },
                        placeholder = { Text("Search or type new label...", fontSize = 13.sp) },
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                        },
                        trailingIcon = {
                            if (inputQuery.isNotEmpty()) {
                                IconButton(onClick = { inputQuery = "" }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val trimmed = inputQuery.trim()
                            if (trimmed.isNotBlank()) {
                                if (trimmed !in labelPool) {
                                    labelPool.add(0, trimmed)
                                }
                                if (trimmed !in selectedLabels) {
                                    selectedLabels.add(trimmed)
                                }
                                inputQuery = ""
                            }
                        },
                        enabled = inputQuery.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(52.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(LanguageHelper.getString("add", languageMode).ifEmpty { "Add" }, fontSize = 12.sp)
                    }
                }

                // Currently Selected Labels (Removable Chips)
                if (selectedLabels.isNotEmpty()) {
                    Text(
                        text = "Selected (${selectedLabels.size}):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SolidPrimary
                    )
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        selectedLabels.forEach { label ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                border = BorderStroke(1.dp, SolidPrimary.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { selectedLabels.remove(label) }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                ) {
                                    Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Remove",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth(),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    )
                }

                // All Previous / Available Labels
                val filteredPool = remember(labelPool, inputQuery) {
                    val q = inputQuery.trim().lowercase()
                    if (q.isEmpty()) labelPool else labelPool.filter { it.lowercase().contains(q) }
                }

                Text(
                    text = "Previous Labels (${filteredPool.size}):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (filteredPool.isEmpty()) {
                    Text(
                        text = if (inputQuery.isNotBlank()) "No existing label matching \"$inputQuery\". Click 'Add' to create it!" else "No previous labels found. Create one above!",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        filteredPool.forEach { label ->
                            val isSelected = label in selectedLabels
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    if (isSelected) {
                                        selectedLabels.remove(label)
                                    } else {
                                        selectedLabels.add(label)
                                    }
                                },
                                label = { Text(label, fontSize = 12.sp) },
                                leadingIcon = {
                                    if (isSelected) {
                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                    } else {
                                        Icon(Icons.AutoMirrored.Filled.Label, contentDescription = null, modifier = Modifier.size(14.dp))
                                    }
                                },
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onLabelSelected(selectedLabels.joinToString(", "))
                }
            ) {
                Text(LanguageHelper.getString("done", languageMode).ifEmpty { "Done" })
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(LanguageHelper.getString("cancel", languageMode))
            }
        }
    )
}
