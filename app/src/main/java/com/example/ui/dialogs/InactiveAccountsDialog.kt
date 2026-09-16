package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.data.model.Account
import com.example.data.model.AccountType
import com.example.data.model.LanguageMode
import com.example.data.model.Transaction
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidPrimary
import com.example.util.IconHelper
import com.example.util.LanguageHelper

@Composable
fun InactiveAccountsDialog(
    allAccounts: List<Account>,
    transactions: List<Transaction>,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onToggleActiveStatus: (Account, Boolean) -> Unit,
    onEditAccountClick: ((Account) -> Unit)? = null,
    onAccountClick: ((Account) -> Unit)? = null
) {
    var searchQuery by remember { mutableStateOf("") }

    // Map parent accounts
    val parentMap = remember(allAccounts) {
        allAccounts.associateBy { it.id }
    }

    // Helper to calculate current balance
    fun getAccountBalance(acc: Account): Double {
        var dr = 0.0
        var cr = 0.0
        for (tx in transactions) {
            if (tx.debitAccountId == acc.id) dr += tx.amount
            if (tx.creditAccountId == acc.id) cr += tx.amount
        }
        return when (acc.type) {
            AccountType.ASSET, AccountType.EXPENSE -> acc.initialBalance + (dr - cr)
            AccountType.LIABILITY -> -(acc.initialBalance + (cr - dr))
            AccountType.EQUITY, AccountType.INCOME -> acc.initialBalance + (cr - dr)
        }
    }

    // Filter inactive accounts
    val inactiveAccounts = remember(allAccounts, searchQuery) {
        allAccounts.filter { acc ->
            val isInactive = !acc.isActive
            val matchesSearch = searchQuery.isBlank() ||
                acc.nameEn.contains(searchQuery, ignoreCase = true) ||
                acc.nameBn.contains(searchQuery, ignoreCase = true)
            isInactive && matchesSearch
        }
    }

    val totalInactiveCount = remember(allAccounts) {
        allAccounts.count { !it.isActive }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("inactive_accounts_dialog"),
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PauseCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "নিষ্ক্রিয় অ্যাকাউন্ট" else "Inactive Accounts",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "$totalInactiveCount টি অ্যাকাউন্ট নিষ্ক্রিয় আছে" else "$totalInactiveCount inactive accounts",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
            ) {
                if (totalInactiveCount > 0) {
                    // Search box
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        placeholder = {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "অ্যাকাউন্ট খুঁজুন..." else "Search inactive accounts...",
                                fontSize = 13.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        )
                    )

                    // Action Bar: Activate All
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "পুনরায় সক্রিয় করুন:" else "Reactivate account:",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(
                            onClick = {
                                allAccounts.filter { !it.isActive }.forEach { acc ->
                                    onToggleActiveStatus(acc, true)
                                }
                            }
                        ) {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "সবগুলো সক্রিয় করুন" else "Activate All",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                if (inactiveAccounts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(SolidIncome.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = SolidIncome,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "কোনো নিষ্ক্রিয় অ্যাকাউন্ট নেই" else "No Inactive Accounts",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "আপনার সকল অ্যাকাউন্ট বর্তমানে সক্রিয় আছে।" else "All your accounts are currently active.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(inactiveAccounts, key = { it.id }) { acc ->
                            val currentBalance = getAccountBalance(acc)
                            val parentAcc = acc.parentId?.let { parentMap[it] }
                            val iconVector = IconHelper.getIconByName(acc.iconName)
                            val accColor = IconHelper.parseColorHex(acc.colorHex, MaterialTheme.colorScheme.primary)

                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (onAccountClick != null) {
                                            onDismiss()
                                            onAccountClick(acc)
                                        } else if (onEditAccountClick != null) {
                                            onDismiss()
                                            onEditAccountClick(acc)
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Account Icon
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(accColor.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = iconVector,
                                            contentDescription = null,
                                            tint = accColor,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    // Account Name and Group info
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = if (languageMode == LanguageMode.BANGLA) acc.nameBn else acc.nameEn,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        if (parentAcc != null) {
                                            Text(
                                                text = "${if (languageMode == LanguageMode.BANGLA) "গ্রুপ" else "Group"}: ${if (languageMode == LanguageMode.BANGLA) parentAcc.nameBn else parentAcc.nameEn}",
                                                fontSize = 10.5.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        // Balance & Type tag
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(top = 2.dp)
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = when (acc.type) {
                                                    AccountType.ASSET -> SolidIncome.copy(alpha = 0.12f)
                                                    AccountType.LIABILITY -> SolidExpense.copy(alpha = 0.12f)
                                                    else -> SolidPrimary.copy(alpha = 0.12f)
                                                }
                                            ) {
                                                Text(
                                                    text = when (acc.type) {
                                                        AccountType.ASSET -> if (languageMode == LanguageMode.BANGLA) "সম্পদ" else "ASSET"
                                                        AccountType.LIABILITY -> if (languageMode == LanguageMode.BANGLA) "দায়" else "LIABILITY"
                                                        else -> acc.type.name
                                                    },
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = when (acc.type) {
                                                        AccountType.ASSET -> SolidIncome
                                                        AccountType.LIABILITY -> SolidExpense
                                                        else -> SolidPrimary
                                                    },
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = LanguageHelper.formatCurrency(currentBalance, languageMode),
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (currentBalance >= 0) MaterialTheme.colorScheme.onSurface else SolidExpense
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    // Switch to reactivate
                                    Switch(
                                        checked = false, // It is currently inactive
                                        onCheckedChange = { isChecked ->
                                            if (isChecked) {
                                                onToggleActiveStatus(acc, true)
                                            }
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = SolidIncome,
                                            checkedTrackColor = SolidIncome.copy(alpha = 0.4f)
                                        ),
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "বন্ধ করুন" else "Close",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    )
}
