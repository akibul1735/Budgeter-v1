package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
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
import com.example.data.repository.AccountWithBalance
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidPrimary
import com.example.ui.theme.SolidTransfer
import com.example.util.AccountObligation
import com.example.util.DateUtils
import com.example.util.IconHelper
import com.example.util.LanguageHelper
import java.util.Calendar

private data class AccountGroupWithChildren(
    val group: Account?,
    val accounts: List<Account>
)

@Composable
internal fun PaymentSourceSelectorDialog(
    allAccounts: List<Account>,
    accountsWithBalances: List<AccountWithBalance>,
    initialSelectedIds: Set<Long>,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onSave: (Set<Long>) -> Unit
) {
    var selectedIds by remember { mutableStateOf(initialSelectedIds) }
    var searchQuery by remember { mutableStateOf("") }

    val balanceMap = remember(accountsWithBalances) {
        accountsWithBalances.associate { it.account.id to it.currentBalance }
    }

    // 1. Group structure
    val childAccountsByParent = remember(allAccounts) {
        allAccounts.filter { it.parentId != null }.groupBy { it.parentId!! }
    }
    val parentGroups = remember(allAccounts) {
        allAccounts.filter { it.parentId == null }
    }

    // Selectable accounts are sub-accounts, or parent accounts that have NO sub-accounts
    val allSelectableAccounts = remember(allAccounts, childAccountsByParent) {
        allAccounts.filter { it.parentId != null || !childAccountsByParent.containsKey(it.id) }
    }

    val q = searchQuery.trim().lowercase()

    // Filter groups and their child accounts
    val displayedGroups = remember(allAccounts, childAccountsByParent, parentGroups, q) {
        val list = mutableListOf<AccountGroupWithChildren>()
        for (parent in parentGroups) {
            val children = childAccountsByParent[parent.id] ?: emptyList()
            if (children.isNotEmpty()) {
                val matchingChildren = if (q.isBlank()) children else children.filter {
                    it.nameEn.lowercase().contains(q) || it.nameBn.lowercase().contains(q) || it.accountNumber.lowercase().contains(q)
                }
                if (matchingChildren.isNotEmpty()) {
                    list.add(AccountGroupWithChildren(parent, matchingChildren))
                }
            } else {
                // Standalone parent account with no children - selectable on its own
                if (q.isBlank() || parent.nameEn.lowercase().contains(q) || parent.nameBn.lowercase().contains(q)) {
                    list.add(AccountGroupWithChildren(null, listOf(parent)))
                }
            }
        }
        // Handle orphaned sub-accounts if any
        val orphaned = allAccounts.filter { it.parentId != null && parentGroups.none { p -> p.id == it.parentId } }
        if (orphaned.isNotEmpty()) {
            val matchingOrphaned = if (q.isBlank()) orphaned else orphaned.filter {
                it.nameEn.lowercase().contains(q) || it.nameBn.lowercase().contains(q)
            }
            if (matchingOrphaned.isNotEmpty()) {
                list.add(AccountGroupWithChildren(null, matchingOrphaned))
            }
        }
        list
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.88f)
                .padding(8.dp)
                .testTag("dialog_select_payment_sources"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "পেমেন্ট সোর্স একাউন্ট নির্বাচন" else "Select Payment Source Accounts",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "শুধুমাত্র নির্বাচিত একাউন্টগুলো পেমেন্ট সোর্সে ব্যবহৃত হবে।" else "Only selected accounts will appear as payment sources.",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Select All & Unselect All Quick Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            selectedIds = allSelectableAccounts.map { it.id }.toSet()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp), tint = SolidPrimary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "সব নির্বাচন" else "Select All",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            selectedIds = emptySet()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp), tint = SolidExpense)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "সব বাতিল" else "Unselect All",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Search Box
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(if (languageMode == LanguageMode.BANGLA) "একাউন্ট খুঁজুন..." else "Search accounts...", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Grouped Account List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (displayedGroups.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "কোনো একাউন্ট পাওয়া যায়নি" else "No accounts found",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    } else {
                        displayedGroups.forEach { groupItem ->
                            val group = groupItem.group
                            val accounts = groupItem.accounts
                            val groupSelectedCount = accounts.count { selectedIds.contains(it.id) }
                            val allGroupSelected = accounts.isNotEmpty() && groupSelectedCount == accounts.size

                            item(key = "group_header_${group?.id ?: "standalone"}") {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                            if (group != null) {
                                                Icon(
                                                    imageVector = IconHelper.getIconByName(group.iconName),
                                                    contentDescription = null,
                                                    tint = SolidPrimary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = group.localizedName(languageMode),
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Default.AccountBalanceWallet,
                                                    contentDescription = null,
                                                    tint = SolidPrimary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = if (languageMode == LanguageMode.BANGLA) "অন্যান্য / প্রধান হিসাব" else "General Accounts",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = SolidPrimary.copy(alpha = 0.12f)
                                            ) {
                                                Text(
                                                    text = "$groupSelectedCount/${accounts.size}",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = SolidPrimary,
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                                )
                                            }
                                        }

                                        // Quick Group Toggle Action
                                        if (accounts.size > 1) {
                                            Text(
                                                text = if (allGroupSelected) {
                                                    if (languageMode == LanguageMode.BANGLA) "সব বাতিল" else "Clear All"
                                                } else {
                                                    if (languageMode == LanguageMode.BANGLA) "গ্রুপের সব নির্বাচন" else "Select Group"
                                                },
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = SolidPrimary,
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .clickable {
                                                        val groupIds = accounts.map { it.id }.toSet()
                                                        selectedIds = if (allGroupSelected) {
                                                            selectedIds - groupIds
                                                        } else {
                                                            selectedIds + groupIds
                                                        }
                                                    }
                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Sub-accounts in this group (selectable)
                            items(accounts, key = { "sub_acc_${it.id}" }) { acc ->
                                val isChecked = selectedIds.contains(acc.id)
                                val balance = balanceMap[acc.id] ?: 0.0
                                val isImg = IconHelper.isDrawableIcon(acc.iconName) || IconHelper.isCustomIcon(acc.iconName)

                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 6.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            selectedIds = if (isChecked) selectedIds - acc.id else selectedIds + acc.id
                                        },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isChecked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                                    border = if (isChecked) BorderStroke(1.dp, SolidPrimary.copy(alpha = 0.4f)) else null
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                            Checkbox(
                                                checked = isChecked,
                                                onCheckedChange = { checked ->
                                                    selectedIds = if (checked) selectedIds + acc.id else selectedIds - acc.id
                                                },
                                                colors = CheckboxDefaults.colors(checkedColor = SolidPrimary)
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isImg) Color.Transparent else SolidPrimary.copy(alpha = 0.12f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                IconHelper.AppIcon(
                                                    iconName = acc.iconName,
                                                    contentDescription = null,
                                                    tint = SolidPrimary,
                                                    modifier = Modifier.size(if (isImg) 28.dp else 15.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = acc.localizedName(languageMode),
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    Text(
                                                        text = LanguageHelper.formatCurrency(balance, languageMode),
                                                        fontSize = 10.sp,
                                                        color = MaterialTheme.colorScheme.outline
                                                    )
                                                    if (acc.accountNumber.isNotBlank()) {
                                                        Text(
                                                            text = "• ${acc.accountNumber}",
                                                            fontSize = 9.sp,
                                                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.8f)
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
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom bar with count and actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${selectedIds.size} / ${allSelectableAccounts.size} ${if (languageMode == LanguageMode.BANGLA) "টি নির্বাচিত" else "Accounts Selected"}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row {
                        OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(8.dp)) {
                            Text(if (languageMode == LanguageMode.BANGLA) "বাতিল" else "Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { onSave(selectedIds) },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SolidPrimary)
                        ) {
                            Text(if (languageMode == LanguageMode.BANGLA) "সংরক্ষণ" else "Save")
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// DIALOG: LINK ACCOUNT OBLIGATION (PAYABLE / RECEIVABLE)
// -----------------------------------------------------------------------------

@Composable
internal fun LinkAccountObligationDialog(
    paymentSourceAccounts: List<Account>,
    allAccounts: List<Account>,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onSave: (AccountObligation) -> Unit,
    initialSourceAccountId: Long? = null
) {
    var selectedSourceId by remember {
        mutableStateOf(initialSourceAccountId ?: paymentSourceAccounts.firstOrNull()?.id ?: 0L)
    }
    var selectedTargetId by remember { mutableStateOf(allAccounts.firstOrNull { it.id != selectedSourceId }?.id ?: 0L) }
    var amountText by remember { mutableStateOf("") }
    var isExpense by remember { mutableStateOf(true) } // true: Pay Payable; false: Receive Inflow
    var noteText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("dialog_link_obligation"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "অন্য একাউন্টের দেনা / পাওনা লিংক করুন" else "Link Account Payable / Receivable",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "যেমন: ব্যাংক থেকে ক্রেডিট কার্ড বিল বা দেনা পরিশোধের ব্যালেন্স ট্র্যাকিং।" else "e.g. Track paying loan/credit card due from your bank account.",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.outline
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Type Toggle: Pay vs Receive
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = isExpense,
                        onClick = { isExpense = true },
                        label = { Text(if (languageMode == LanguageMode.BANGLA) "দেনা পরিশোধ (Pay)" else "Pay Liability", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = !isExpense,
                        onClick = { isExpense = false },
                        label = { Text(if (languageMode == LanguageMode.BANGLA) "পাওনা আদায় (Receive)" else "Receive Due", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Source Account Dropdown
                Text(text = "Payment Source Account:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                LazyColumn(modifier = Modifier.height(80.dp)) {
                    items(paymentSourceAccounts) { acc ->
                        val isSelected = acc.id == selectedSourceId
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                                .clickable { selectedSourceId = acc.id },
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSelected) SolidPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ) {
                            Text(text = acc.localizedName(languageMode), fontSize = 11.sp, modifier = Modifier.padding(6.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Target Account Dropdown
                Text(text = "Target Account (Payable / Due):", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                LazyColumn(modifier = Modifier.height(80.dp)) {
                    items(allAccounts.filter { it.id != selectedSourceId }) { acc ->
                        val isSelected = acc.id == selectedTargetId
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                                .clickable { selectedTargetId = acc.id },
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSelected) SolidTransfer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ) {
                            Text(text = acc.localizedName(languageMode), fontSize = 11.sp, modifier = Modifier.padding(6.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Amount Field
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount", fontSize = 11.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Note Field
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Note (Optional)", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(8.dp)) {
                        Text(if (languageMode == LanguageMode.BANGLA) "বাতিল" else "Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amt = amountText.toDoubleOrNull() ?: 0.0
                            if (amt > 0 && selectedSourceId > 0 && selectedTargetId > 0) {
                                onSave(
                                    AccountObligation(
                                        id = java.util.UUID.randomUUID().toString(),
                                        sourceAccountId = selectedSourceId,
                                        targetAccountId = selectedTargetId,
                                        amount = amt,
                                        isExpense = isExpense,
                                        note = noteText
                                    )
                                )
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SolidPrimary)
                    ) {
                        Text(if (languageMode == LanguageMode.BANGLA) "সংরক্ষণ" else "Save")
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// DIALOG: TRANSFER EXECUTION CONFIRMATION
// -----------------------------------------------------------------------------

@Composable
internal fun TransferConfirmationDialog(
    suggestion: FundAllocationSuggestion,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onConfirm: (note: String) -> Unit
) {
    var note by remember { mutableStateOf("Payment Source Balancing Transfer") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "ফান্ড ট্রান্সফার নিশ্চিতকরণ" else "Confirm Fund Transfer",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = suggestion.getReason(languageMode),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(8.dp)) {
                        Text(if (languageMode == LanguageMode.BANGLA) "বাতিল" else "Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(note) },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SolidTransfer)
                    ) {
                        Text(if (languageMode == LanguageMode.BANGLA) "ট্রান্সফার সম্পন্ন করুন" else "Execute Transfer")
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// DIALOG: ASSIGN ITEM CHOICE (OTHER ACCOUNT / INCOME / EXPENSE)
// -----------------------------------------------------------------------------

@Composable
internal fun AssignItemChoiceDialog(
    account: Account,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onSelectOtherAccount: () -> Unit,
    onSelectIncome: () -> Unit,
    onSelectExpense: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("dialog_assign_item_choice"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "আইটেম বরাদ্দ করুন" else "Assign Item to Source",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = account.localizedName(languageMode),
                            fontSize = 12.sp,
                            color = SolidPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(20.dp))
                    }
                }

                Text(
                    text = if (languageMode == LanguageMode.BANGLA)
                        "এই পেমেন্ট সোর্স একাউন্টে কোন ধরনের আইটেম বরাদ্দ বা লিংক করতে চান?"
                    else
                        "Select the type of item you want to assign or link to this payment source:",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Choice 1: Other Account (দেনা / পাওনা)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSelectOtherAccount() }
                        .testTag("choice_assign_other_account"),
                    shape = RoundedCornerShape(12.dp),
                    color = SolidTransfer.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, SolidTransfer.copy(alpha = 0.25f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SolidTransfer.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.People, contentDescription = null, tint = SolidTransfer, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "অন্যান্য একাউন্ট (Other Account)" else "Other Account",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "দেনা, ঋণ, ক্রেডিট কার্ড বিল বা স্থানান্তর লিংক করুন" else "Link loan, credit card, or payable obligation to this account",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = SolidTransfer, modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Choice 2: Income (আয়)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSelectIncome() }
                        .testTag("choice_assign_income"),
                    shape = RoundedCornerShape(12.dp),
                    color = SolidIncome.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, SolidIncome.copy(alpha = 0.25f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SolidIncome.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.TrendingUp, contentDescription = null, tint = SolidIncome, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "আয় (Income)" else "Income",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "প্রত্যাশিত আয় ক্যাটাগরি এই একাউন্টে বরাদ্দ করুন" else "Assign expected income categories to this account",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = SolidIncome, modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Choice 3: Expense (ব্যয়)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSelectExpense() }
                        .testTag("choice_assign_expense"),
                    shape = RoundedCornerShape(12.dp),
                    color = SolidExpense.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, SolidExpense.copy(alpha = 0.25f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SolidExpense.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = SolidExpense, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "ব্যয় (Expense)" else "Expense",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "বাজেটকৃত ব্যয় ক্যাটাগরি এই একাউন্টে বরাদ্দ বা স্প্লিট করুন" else "Assign or split budgeted expense categories to this account",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = SolidExpense, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// DIALOG: SELECT CATEGORY FOR ACCOUNT ASSIGNMENT
// -----------------------------------------------------------------------------

@Composable
internal fun SelectCategoryForAccountDialog(
    targetAccount: Account,
    availableCategories: List<Category>,
    currentAllocations: List<CategoryAllocationAnalysis>,
    isExpense: Boolean,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onCategorySelected: (CategoryAllocationAnalysis) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val q = searchQuery.trim().lowercase()

    val filtered = remember(availableCategories, q, isExpense) {
        val list = availableCategories.filter { if (isExpense) it.type == CategoryType.EXPENSE else it.type == CategoryType.INCOME }
        if (q.isEmpty()) list
        else list.filter {
            it.nameEn.lowercase().contains(q) || it.nameBn.lowercase().contains(q)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.80f)
                .padding(8.dp)
                .testTag("dialog_select_category_for_account"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isExpense) {
                                if (languageMode == LanguageMode.BANGLA) "ব্যয় ক্যাটাগরি বরাদ্দ করুন" else "Assign Expense Category"
                            } else {
                                if (languageMode == LanguageMode.BANGLA) "আয় ক্যাটাগরি বরাদ্দ করুন" else "Assign Income Category"
                            },
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${if (languageMode == LanguageMode.BANGLA) "টার্গেট একাউন্ট" else "Target Account"}: ${targetAccount.localizedName(languageMode)}",
                            fontSize = 11.sp,
                            color = SolidPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(20.dp))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "ক্যাটাগরি খুঁজুন..." else "Search category...",
                            fontSize = 12.sp
                        )
                    },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (filtered.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "কোনো ক্যাটাগরি পাওয়া যায়নি" else "No categories found",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    } else {
                        items(filtered, key = { it.id }) { cat ->
                            val existingAlloc = currentAllocations.find { it.category.id == cat.id }
                            val catColor = remember(cat.colorHex) {
                                try {
                                    Color(android.graphics.Color.parseColor(cat.colorHex))
                                } catch (e: Exception) {
                                    if (isExpense) SolidExpense else SolidIncome
                                }
                            }

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        val targetAlloc = if (existingAlloc != null) {
                                            existingAlloc.copy(
                                                accountSplits = listOf(
                                                    CategoryAccountSplit(
                                                        account = targetAccount,
                                                        allocatedAmount = existingAlloc.totalBudgeted,
                                                        percentageOfCategory = 100.0
                                                    )
                                                )
                                            )
                                        } else {
                                            CategoryAllocationAnalysis(
                                                category = cat,
                                                totalBudgetOrRequired = cat.budgetLimit,
                                                totalBudgeted = cat.budgetLimit,
                                                totalActualSpent = 0.0,
                                                totalRemaining = cat.budgetLimit,
                                                accountSplits = listOf(
                                                    CategoryAccountSplit(
                                                        account = targetAccount,
                                                        allocatedAmount = cat.budgetLimit,
                                                        percentageOfCategory = 100.0
                                                    )
                                                ),
                                                isMultiAccount = false,
                                                isExpense = isExpense
                                            )
                                        }
                                        onCategorySelected(targetAlloc)
                                    },
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(catColor.copy(alpha = 0.16f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = IconHelper.getIconByName(cat.iconName),
                                                contentDescription = null,
                                                tint = catColor,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column {
                                            Text(
                                                text = cat.localizedName(languageMode),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            if (existingAlloc != null) {
                                                Text(
                                                    text = "${if (languageMode == LanguageMode.BANGLA) "বাজেট" else "Budget"}: ${LanguageHelper.formatCurrency(existingAlloc.totalBudgeted, languageMode)} • ${if (existingAlloc.accountSplits.isNotEmpty()) (if (languageMode == LanguageMode.BANGLA) "বরাদ্দকৃত" else "Assigned") else (if (languageMode == LanguageMode.BANGLA) "অনির্ধারিত" else "Unassigned")}",
                                                    fontSize = 10.sp,
                                                    color = if (existingAlloc.accountSplits.isNotEmpty()) SolidIncome else SolidExpense
                                                )
                                            } else {
                                                Text(
                                                    text = if (languageMode == LanguageMode.BANGLA) "নতুন বরাদ্দ" else "New assignment",
                                                    fontSize = 10.sp,
                                                    color = MaterialTheme.colorScheme.outline
                                                )
                                            }
                                        }
                                    }

                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Select",
                                        tint = SolidPrimary,
                                        modifier = Modifier.size(18.dp)
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

// -----------------------------------------------------------------------------
// DIALOG: SUGGESTED FUND TRANSFERS MODAL
// -----------------------------------------------------------------------------

@Composable
internal fun SuggestedFundTransfersDialog(
    transferSuggestions: List<FundAllocationSuggestion>,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onExecuteTransfer: (FundAllocationSuggestion) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.70f)
                .padding(8.dp)
                .testTag("dialog_suggested_fund_transfers"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.SyncAlt, contentDescription = null, tint = SolidTransfer, modifier = Modifier.size(18.dp))
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "প্রস্তাবিত ফান্ড ট্রান্সফার" else "Suggested Fund Transfers",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (transferSuggestions.isNotEmpty()) SolidExpense else SolidIncome
                        ) {
                            Text(
                                text = "${transferSuggestions.size}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(18.dp))
                    }
                }

                Text(
                    text = if (languageMode == LanguageMode.BANGLA)
                        "উদ্বৃত্ত একাউন্ট থেকে ঘাটতিযুক্ত একাউন্টে স্বয়ংক্রিয় ব্যালেন্স সমন্বয়ের প্রস্তাবনা।"
                    else
                        "Recommended balance reallocations from surplus sources to fund shortfalls.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (transferSuggestions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SolidIncome, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "সকল সোর্স পর্যাপ্ত ফান্ডেড। কোনো ট্রান্সফারের প্রয়োজন নেই।" else "All payment sources are funded. No transfers needed.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(transferSuggestions) { suggestion ->
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                border = BorderStroke(1.dp, SolidTransfer.copy(alpha = 0.25f))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = suggestion.fromAccount.localizedName(languageMode),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = SolidIncome
                                            )
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp).padding(horizontal = 2.dp),
                                                tint = MaterialTheme.colorScheme.outline
                                            )
                                            Text(
                                                text = suggestion.toAccount.localizedName(languageMode),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = SolidExpense
                                            )
                                        }

                                        Text(
                                            text = LanguageHelper.formatCurrency(suggestion.transferAmount, languageMode),
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 13.sp,
                                            color = SolidTransfer
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = suggestion.getReason(languageMode),
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                        Button(
                                            onClick = { onExecuteTransfer(suggestion) },
                                            shape = RoundedCornerShape(6.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = SolidTransfer),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                        ) {
                                            Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (languageMode == LanguageMode.BANGLA) "ট্রান্সফার" else "Transfer",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(8.dp)) {
                        Text(if (languageMode == LanguageMode.BANGLA) "বন্ধ করুন" else "Close", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

