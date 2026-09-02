package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
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
import com.example.data.repository.AccountWithBalance
import com.example.util.IconHelper
import com.example.util.LanguageHelper

@Composable
fun AccountsScreen(
    accountsWithBalances: List<AccountWithBalance>,
    languageMode: LanguageMode,
    onAddAccountClick: () -> Unit,
    onAddSubAccountClick: (Account) -> Unit,
    onEditAccountClick: (Account) -> Unit
) {
    var selectedTypeFilter by remember { mutableStateOf<AccountType?>(null) }
    val expandedMap = remember { mutableStateMapOf<Long, Boolean>() }

    val filteredList = remember(accountsWithBalances, selectedTypeFilter) {
        if (selectedTypeFilter == null) {
            accountsWithBalances
        } else {
            accountsWithBalances.filter { it.account.type == selectedTypeFilter }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("accounts_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Filter Row + Add Account button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = LanguageHelper.getString("accounts", languageMode),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Button(
                    onClick = onAddAccountClick,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("add_account_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(LanguageHelper.getString("add_account", languageMode), fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Filter Chips (All, Assets, Liabilities, Equity)
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
                    selected = selectedTypeFilter == AccountType.ASSET,
                    onClick = { selectedTypeFilter = AccountType.ASSET },
                    label = { Text(LanguageHelper.getString("assets", languageMode)) },
                    shape = RoundedCornerShape(10.dp)
                )
                FilterChip(
                    selected = selectedTypeFilter == AccountType.LIABILITY,
                    onClick = { selectedTypeFilter = AccountType.LIABILITY },
                    label = { Text(LanguageHelper.getString("liabilities", languageMode)) },
                    shape = RoundedCornerShape(10.dp)
                )
                FilterChip(
                    selected = selectedTypeFilter == AccountType.EQUITY,
                    onClick = { selectedTypeFilter = AccountType.EQUITY },
                    label = { Text(LanguageHelper.getString("equity", languageMode)) },
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        if (filteredList.isEmpty()) {
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
                            text = LanguageHelper.getString("no_accounts", languageMode),
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        } else {
            items(filteredList) { accItem ->
                val isExpanded = expandedMap[accItem.account.id] ?: true

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // Parent Account Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (accItem.subAccounts.isNotEmpty()) {
                                        expandedMap[accItem.account.id] = !isExpanded
                                    } else {
                                        onEditAccountClick(accItem.account)
                                    }
                                },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                val typeColor = when (accItem.account.type) {
                                    AccountType.ASSET -> Color(0xFF10B981)
                                    AccountType.LIABILITY -> MaterialTheme.colorScheme.error
                                    AccountType.EQUITY -> Color(0xFF8B5CF6)
                                    else -> MaterialTheme.colorScheme.primary
                                }

                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(typeColor.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = IconHelper.getIconByName(accItem.account.iconName),
                                        contentDescription = null,
                                        tint = typeColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = accItem.account.localizedName(languageMode),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = when (accItem.account.type) {
                                            AccountType.ASSET -> LanguageHelper.getString("assets", languageMode)
                                            AccountType.LIABILITY -> LanguageHelper.getString("liabilities", languageMode)
                                            AccountType.EQUITY -> LanguageHelper.getString("equity", languageMode)
                                            else -> ""
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = LanguageHelper.formatCurrency(accItem.currentBalance, languageMode),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                if (accItem.subAccounts.isNotEmpty()) {
                                    IconButton(
                                        onClick = { expandedMap[accItem.account.id] = !isExpanded },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = "Expand"
                                        )
                                    }
                                } else {
                                    IconButton(
                                        onClick = { onEditAccountClick(accItem.account) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.MoreVert,
                                            contentDescription = "Edit"
                                        )
                                    }
                                }
                            }
                        }

                        // Sub-accounts list
                        AnimatedVisibility(visible = isExpanded && accItem.subAccounts.isNotEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp, start = 16.dp)
                            ) {
                                accItem.subAccounts.forEach { subAccItem ->
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { onEditAccountClick(subAccItem.account) },
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                                Icon(
                                                    imageVector = IconHelper.getIconByName(subAccItem.account.iconName),
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = subAccItem.account.localizedName(languageMode),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Medium,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }

                                            Text(
                                                text = LanguageHelper.formatCurrency(subAccItem.currentBalance, languageMode),
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                // Quick Add Sub-Account button
                                Text(
                                    text = "+ ${LanguageHelper.getString("add_sub_account", languageMode)}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .clickable { onAddSubAccountClick(accItem.account) }
                                        .padding(vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
