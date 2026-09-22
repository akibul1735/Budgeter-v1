package com.example.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Account
import com.example.data.model.LanguageMode
import com.example.data.repository.AccountWithBalance
import com.example.util.IconHelper
import com.example.util.LanguageHelper
import java.util.Locale

data class AccountGroupDisplayItem(
    val groupAccount: Account,
    val groupName: String,
    val accounts: List<Pair<Account, Double>>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchableAccountPickerDialog(
    accountsWithBalances: List<AccountWithBalance>,
    languageMode: LanguageMode,
    title: String = if (languageMode == LanguageMode.BANGLA) "হিসাব নির্বাচন করুন" else "Select Account",
    excludedAccountIds: Set<Long> = emptySet(),
    onAccountSelected: (Account, Double) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val rawGroups = remember(accountsWithBalances, excludedAccountIds, languageMode) {
        accountsWithBalances.mapNotNull { parentItem ->
            if (!parentItem.account.isActive) return@mapNotNull null
            if (parentItem.subAccounts.isEmpty()) {
                if (parentItem.account.id in excludedAccountIds) return@mapNotNull null
                AccountGroupDisplayItem(
                    groupAccount = parentItem.account,
                    groupName = parentItem.account.localizedName(languageMode),
                    accounts = listOf(parentItem.account to parentItem.currentBalance)
                )
            } else {
                val validSubs = parentItem.subAccounts.filter {
                    it.account.isActive && it.account.id !in excludedAccountIds
                }.map { it.account to it.currentBalance }
                if (validSubs.isEmpty()) return@mapNotNull null
                AccountGroupDisplayItem(
                    groupAccount = parentItem.account,
                    groupName = parentItem.account.localizedName(languageMode),
                    accounts = validSubs
                )
            }
        }
    }

    val filteredGroups = remember(rawGroups, searchQuery, languageMode) {
        if (searchQuery.isBlank()) {
            rawGroups
        } else {
            val q = searchQuery.trim().lowercase(Locale.getDefault())
            rawGroups.mapNotNull { group ->
                val groupMatches = group.groupName.lowercase(Locale.getDefault()).contains(q) ||
                    group.groupAccount.nameEn.lowercase(Locale.getDefault()).contains(q) ||
                    group.groupAccount.nameBn.lowercase(Locale.getDefault()).contains(q)

                val matchingAccounts = group.accounts.filter { (acc, _) ->
                    groupMatches ||
                        acc.nameEn.lowercase(Locale.getDefault()).contains(q) ||
                        acc.nameBn.lowercase(Locale.getDefault()).contains(q) ||
                        acc.type.name.lowercase(Locale.getDefault()).contains(q) ||
                        acc.accountNumber.lowercase(Locale.getDefault()).contains(q) ||
                        acc.localizedName(languageMode).lowercase(Locale.getDefault()).contains(q)
                }

                if (matchingAccounts.isNotEmpty()) {
                    group.copy(accounts = matchingAccounts)
                } else null
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .heightIn(max = 580.dp)
                .testTag("searchable_account_picker_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(20.dp))
                    }
                }

                // Search Bar Input
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "হিসাব বা গ্রুপ খুঁজুন..." else "Search accounts or groups...",
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search", modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("account_search_input")
                )

                // Accounts List or Empty Search Indicator
                if (filteredGroups.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "কোনো হিসাব মেলেনি" else "No matching accounts found",
                            color = MaterialTheme.colorScheme.outline,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                    ) {
                        items(filteredGroups, key = { it.groupAccount.id }) { groupItem ->
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Group Name Header (NON-SELECTABLE)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 6.dp, bottom = 2.dp, start = 4.dp, end = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                        modifier = Modifier.size(26.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            IconHelper.AppIcon(
                                                iconName = groupItem.groupAccount.iconName,
                                                modifier = Modifier.size(17.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                    Text(
                                        text = groupItem.groupName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    HorizontalDivider(
                                        modifier = Modifier.weight(1f),
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                                    )
                                }

                                // Indented Account Item: ONLY accounts are selectable
                                groupItem.accounts.forEach { (acc, bal) ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 16.dp) // Indented under group name
                                            .clickable {
                                                onAccountSelected(acc, bal)
                                            }
                                            .testTag("account_suggestion_item_${acc.id}")
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 9.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                val isCustomOrDrawable = IconHelper.isDrawableIcon(acc.iconName) || IconHelper.isCustomIcon(acc.iconName)
                                                Surface(
                                                    shape = CircleShape,
                                                    color = if (isCustomOrDrawable) Color.Transparent else MaterialTheme.colorScheme.primaryContainer,
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        IconHelper.AppIcon(
                                                            iconName = acc.iconName,
                                                            modifier = Modifier.size(if (isCustomOrDrawable) 32.dp else 22.dp),
                                                            tint = MaterialTheme.colorScheme.primary
                                                        )
                                                    }
                                                }
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = acc.localizedName(languageMode),
                                                        fontWeight = FontWeight.SemiBold,
                                                        fontSize = 13.sp,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    val subLabel = if (acc.accountNumber.isNotBlank()) {
                                                        "${acc.type.name.replace("_", " ")} • ${acc.accountNumber}"
                                                    } else {
                                                        acc.type.name.replace("_", " ")
                                                    }
                                                    Text(
                                                        text = subLabel,
                                                        fontSize = 10.5.sp,
                                                        color = MaterialTheme.colorScheme.outline,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.width(8.dp))

                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(
                                                    text = LanguageHelper.formatCurrency(bal, languageMode),
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (bal < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                                    maxLines = 1
                                                )
                                                Text(
                                                    text = if (languageMode == LanguageMode.BANGLA) "ব্যালেন্স" else "Balance",
                                                    fontSize = 9.5.sp,
                                                    color = MaterialTheme.colorScheme.outline
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
    }
}

/**
 * Convenience overload for flat account lists to preserve backward compatibility.
 */
@Composable
fun SearchableAccountPickerDialog(
    flatAccounts: List<Pair<Account, Double>>,
    languageMode: LanguageMode,
    title: String = if (languageMode == LanguageMode.BANGLA) "হিসাব নির্বাচন করুন" else "Select Account",
    onAccountSelected: (Account, Double) -> Unit,
    onDismiss: () -> Unit
) {
    // Group flat accounts by AccountType or parent
    val grouped = remember(flatAccounts, languageMode) {
        flatAccounts.groupBy { it.first.type }.map { (type, accList) ->
            AccountWithBalance(
                account = Account(
                    id = type.ordinal.toLong() + 10000L,
                    nameEn = type.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                    nameBn = when (type) {
                        com.example.data.model.AccountType.ASSET -> "সম্পদ হিসাব"
                        com.example.data.model.AccountType.LIABILITY -> "দায় হিসাব"
                        com.example.data.model.AccountType.EQUITY -> "মালিকানাস্বত্ব"
                        else -> type.name
                    },
                    type = type,
                    iconName = if (type == com.example.data.model.AccountType.ASSET) "AccountBalance" else "CreditCard"
                ),
                currentBalance = accList.sumOf { it.second },
                subAccounts = accList.map { (acc, bal) ->
                    AccountWithBalance(
                        account = acc,
                        currentBalance = bal
                    )
                }
            )
        }
    }

    SearchableAccountPickerDialog(
        accountsWithBalances = grouped,
        languageMode = languageMode,
        title = title,
        onAccountSelected = onAccountSelected,
        onDismiss = onDismiss
    )
}
