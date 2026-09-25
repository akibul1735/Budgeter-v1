package com.example.ui.dialogs

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.util.DisplayFormatPreferences
import com.example.util.IconHelper
import com.example.util.LanguageHelper

@Composable
internal fun AccountPickerModalDialog(
    accounts: List<Account>,
    allAccounts: List<Account>,
    txType: TransactionType,
    creditAccountId: Long?,
    debitAccountId: Long?,
    initialTarget: Int = 0, // 0 = Source / Expense, 1 = Destination / Income, 2 = Transfer Fee
    languageMode: LanguageMode,
    onAccountSelected: (Long?, Long?) -> Unit,
    onAddNewAccount: (Account) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedCredit by remember { mutableStateOf(creditAccountId) }
    var selectedDebit by remember { mutableStateOf(debitAccountId) }
    var transferTab by remember { mutableStateOf(if (initialTarget == 1) 1 else 0) } // 0 = Source (From), 1 = Destination (To)
    var searchQuery by remember { mutableStateOf("") }
    var showInlineCreateAccount by remember { mutableStateOf(false) }

    if (showInlineCreateAccount) {
        val parentAccounts = allAccounts.filter { it.parentId == null }
        QuickCreateAccountDialog(
            parentAccounts = parentAccounts,
            languageMode = languageMode,
            onDismiss = { showInlineCreateAccount = false },
            onAccountCreated = { newAcc ->
                onAddNewAccount(newAcc)
                if (txType == TransactionType.TRANSFER) {
                    if (initialTarget == 2) {
                        onAccountSelected(newAcc.id, null)
                    } else if (transferTab == 0) {
                        onAccountSelected(newAcc.id, selectedDebit)
                    } else {
                        onAccountSelected(selectedCredit, newAcc.id)
                    }
                } else if (txType == TransactionType.EXPENSE) {
                    onAccountSelected(newAcc.id, null)
                } else {
                    onAccountSelected(null, newAcc.id)
                }
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
                    if (txType == TransactionType.TRANSFER && initialTarget != 2) {
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
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "উৎস (হতে)" else "From (Source)",
                                        fontSize = 11.sp,
                                        color = SolidExpense,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = fromAcc?.localizedName(languageMode) ?: (if (languageMode == LanguageMode.BANGLA) "বাছাই করুন" else "Select"),
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
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "গন্তব্য (পর্যন্ত)" else "To (Destination)",
                                        fontSize = 11.sp,
                                        color = SolidIncome,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = toAcc?.localizedName(languageMode) ?: (if (languageMode == LanguageMode.BANGLA) "বাছাই করুন" else "Select"),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    } else if (initialTarget == 2) {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "ফি অ্যাকাউন্ট বাছাই করুন" else "Select Transfer Fee Account",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = SolidExpense,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
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
                                    if (initialTarget == 2) {
                                        onAccountSelected(acc.id, null)
                                    } else if (transferTab == 0) {
                                        selectedCredit = acc.id
                                        onAccountSelected(acc.id, selectedDebit)
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
                            val parentAccounts = allAccounts.filter { it.parentId == null && it.isActive }
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
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 10.dp, bottom = 12.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            IconHelper.AppIcon(
                                                iconName = parent.iconName,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = parent.localizedName(languageMode),
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
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
                                    color = MaterialTheme.colorScheme.primary,
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
                            Box(
                                modifier = Modifier.size(52.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                val isCustomOrDrawable = IconHelper.isDrawableIcon(acc.iconName) || IconHelper.isCustomIcon(acc.iconName)
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(if (isCustomOrDrawable) Color.Transparent else parseItemColor(acc.colorHex, Color(0xFF2563EB))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    IconHelper.AppIcon(
                                        iconName = acc.iconName,
                                        contentDescription = null,
                                        tint = if (isCustomOrDrawable) Color.Unspecified else Color.White,
                                        modifier = Modifier.size(if (isCustomOrDrawable) 40.dp else 26.dp)
                                    )
                                }
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
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
                                        IconHelper.AppIcon(
                                            iconName = parent.iconName,
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

