package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidPrimary
import com.example.util.IconHelper
import com.example.util.LanguageHelper

@Composable
fun AccountsScreen(
    accountsWithBalances: List<AccountWithBalance>,
    languageMode: LanguageMode,
    onAddAccountClick: () -> Unit,
    onAddSubAccountClick: (Account) -> Unit,
    onEditAccountClick: (Account) -> Unit,
    onToggleActiveStatus: ((Account, Boolean) -> Unit)? = null
) {
    var isEditMode by remember { mutableStateOf(false) }
    var selectedTypeFilter by remember { mutableStateOf<AccountType?>(null) }
    val expandedMap = remember { mutableStateMapOf<Long, Boolean>() }

    // Separate active and inactive accounts
    val activeAccounts = remember(accountsWithBalances) {
        accountsWithBalances.filter { it.account.isActive }
    }

    val inactiveAccounts = remember(accountsWithBalances) {
        accountsWithBalances.filter { !it.account.isActive }
    }

    // Totals calculation
    val totalAssets = remember(activeAccounts) {
        activeAccounts.filter { it.account.type == AccountType.ASSET }.sumOf { it.currentBalance }
    }
    val totalLiabilities = remember(activeAccounts) {
        activeAccounts.filter { it.account.type == AccountType.LIABILITY }.sumOf { it.currentBalance }
    }
    val netWorth = totalAssets - totalLiabilities

    val displayedActiveList = remember(activeAccounts, selectedTypeFilter) {
        if (selectedTypeFilter == null) activeAccounts
        else activeAccounts.filter { it.account.type == selectedTypeFilter }
    }

    val displayedInactiveList = remember(inactiveAccounts, selectedTypeFilter) {
        if (selectedTypeFilter == null) inactiveAccounts
        else inactiveAccounts.filter { it.account.type == selectedTypeFilter }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("accounts_screen"),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // --- 1. Net Worth Card (Net Worth: Assets - Liabilities) ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("net_worth_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SolidPrimary)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    Text(
                        text = "Net Worth",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = LanguageHelper.formatCurrency(netWorth, languageMode),
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Solid Row: Assets vs Liabilities
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.Black.copy(alpha = 0.2f))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Assets
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(SolidIncome)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Assets",
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = LanguageHelper.formatCurrency(totalAssets, languageMode),
                                color = SolidIncome,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Divider line
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(32.dp)
                                .background(Color.White.copy(alpha = 0.2f))
                        )

                        // Liabilities
                        Column(horizontalAlignment = Alignment.End) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Liabilities",
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(SolidExpense)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = LanguageHelper.formatCurrency(totalLiabilities, languageMode),
                                color = SolidExpense,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // --- 2. Action Bar: "Edit Button" ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Edit Button (toggles active/inactive controls)
                OutlinedButton(
                    onClick = { isEditMode = !isEditMode },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isEditMode) SolidPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
                        contentColor = if (isEditMode) SolidPrimary else MaterialTheme.colorScheme.onSurface
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isEditMode) SolidPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("accounts_edit_mode_btn")
                ) {
                    Icon(
                        imageVector = if (isEditMode) Icons.Default.Check else Icons.Default.Edit,
                        contentDescription = "Edit",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isEditMode) "Done" else "Edit Accounts",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // --- 3. Filter Chips (All, Assets, Liabilities) ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedTypeFilter == null,
                    onClick = { selectedTypeFilter = null },
                    label = { Text("All Groups", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    shape = RoundedCornerShape(8.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SolidPrimary,
                        selectedLabelColor = Color.White
                    )
                )
                FilterChip(
                    selected = selectedTypeFilter == AccountType.ASSET,
                    onClick = { selectedTypeFilter = AccountType.ASSET },
                    label = { Text("Assets", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    shape = RoundedCornerShape(8.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SolidIncome,
                        selectedLabelColor = Color.White
                    )
                )
                FilterChip(
                    selected = selectedTypeFilter == AccountType.LIABILITY,
                    onClick = { selectedTypeFilter = AccountType.LIABILITY },
                    label = { Text("Liabilities", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    shape = RoundedCornerShape(8.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SolidExpense,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        // --- 4. Active Groups & Categories List ---
        if (displayedActiveList.isEmpty() && displayedInactiveList.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = LanguageHelper.getString("no_accounts", languageMode),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        } else {
            items(displayedActiveList, key = { it.account.id }) { groupItem ->
                AccountGroupCard(
                    groupItem = groupItem,
                    isEditMode = isEditMode,
                    isExpanded = expandedMap[groupItem.account.id] ?: true,
                    languageMode = languageMode,
                    onToggleExpand = { expandedMap[groupItem.account.id] = !(expandedMap[groupItem.account.id] ?: true) },
                    onEditAccount = onEditAccountClick,
                    onAddSubAccount = onAddSubAccountClick,
                    onToggleActiveStatus = onToggleActiveStatus
                )
            }
        }

        // --- 5. Inactive Accounts Section (Show inactive accounts separately by types, groups) ---
        if (displayedInactiveList.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.VisibilityOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Inactive Accounts & Groups (${displayedInactiveList.size})",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            items(displayedInactiveList, key = { "inactive_${it.account.id}" }) { inactiveGroup ->
                AccountGroupCard(
                    groupItem = inactiveGroup,
                    isEditMode = isEditMode,
                    isExpanded = expandedMap[inactiveGroup.account.id] ?: false,
                    isInactiveSection = true,
                    languageMode = languageMode,
                    onToggleExpand = { expandedMap[inactiveGroup.account.id] = !(expandedMap[inactiveGroup.account.id] ?: false) },
                    onEditAccount = onEditAccountClick,
                    onAddSubAccount = onAddSubAccountClick,
                    onToggleActiveStatus = onToggleActiveStatus
                )
            }
        }
    }
}

@Composable
fun AccountGroupCard(
    groupItem: AccountWithBalance,
    isEditMode: Boolean,
    isExpanded: Boolean,
    isInactiveSection: Boolean = false,
    languageMode: LanguageMode,
    onToggleExpand: () -> Unit,
    onEditAccount: (Account) -> Unit,
    onAddSubAccount: (Account) -> Unit,
    onToggleActiveStatus: ((Account, Boolean) -> Unit)?
) {
    val group = groupItem.account
    val typeColor = when (group.type) {
        AccountType.ASSET -> SolidIncome
        AccountType.LIABILITY -> SolidExpense
        else -> SolidPrimary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("account_group_${group.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isInactiveSection) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isInactiveSection) 0.dp else 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Group Row: [Active Switch/Icon] [Group Name] : [Total Group Amount]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (isEditMode) onEditAccount(group)
                        else onToggleExpand()
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Edit Mode: Active/Inactive Button on Left Side
                    if (isEditMode) {
                        Switch(
                            checked = group.isActive,
                            onCheckedChange = { active ->
                                onToggleActiveStatus?.invoke(group, active)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = SolidIncome,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = MaterialTheme.colorScheme.outline
                            ),
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }

                    // Group Icon Box (Solid Colored)
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(typeColor.copy(alpha = if (isInactiveSection) 0.08f else 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = IconHelper.getIconByName(group.iconName),
                            contentDescription = null,
                            tint = if (isInactiveSection) MaterialTheme.colorScheme.outline else typeColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = group.localizedName(languageMode),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isInactiveSection) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (!group.isActive) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Inactive",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SolidExpense,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(SolidExpense.copy(alpha = 0.12f))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = if (group.type == AccountType.ASSET) "Assets Group" else "Liabilities Group",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Total Group Amount + Expand/Edit icon
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = LanguageHelper.formatCurrency(groupItem.currentBalance, languageMode),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isInactiveSection) MaterialTheme.colorScheme.onSurfaceVariant
                        else if (group.type == AccountType.LIABILITY) SolidExpense else SolidIncome
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    if (isEditMode) {
                        IconButton(
                            onClick = { onEditAccount(group) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Group", modifier = Modifier.size(16.dp))
                        }
                    } else {
                        IconButton(
                            onClick = onToggleExpand,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "Expand",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Sub-Accounts / Categories Under Group
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, start = if (isEditMode) 8.dp else 12.dp)
                ) {
                    // Active Categories or Inactive Categories
                    val displayedSubAccounts = if (isEditMode || isInactiveSection) {
                        groupItem.subAccounts
                    } else {
                        groupItem.subAccounts.filter { it.account.isActive }
                    }

                    if (displayedSubAccounts.isNotEmpty()) {
                        displayedSubAccounts.forEach { subItem ->
                            val sub = subItem.account
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onEditAccount(sub) },
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        // Edit Mode: Sub-account Active/Inactive switch
                                        if (isEditMode) {
                                            Switch(
                                                checked = sub.isActive,
                                                onCheckedChange = { active ->
                                                    onToggleActiveStatus?.invoke(sub, active)
                                                },
                                                colors = SwitchDefaults.colors(
                                                    checkedThumbColor = Color.White,
                                                    checkedTrackColor = SolidIncome,
                                                    uncheckedThumbColor = Color.White,
                                                    uncheckedTrackColor = MaterialTheme.colorScheme.outline
                                                ),
                                                modifier = Modifier.padding(end = 6.dp)
                                            )
                                        }

                                        Icon(
                                            imageVector = IconHelper.getIconByName(sub.iconName),
                                            contentDescription = null,
                                            tint = if (sub.isActive) SolidPrimary else MaterialTheme.colorScheme.outline,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "- ${sub.localizedName(languageMode)}",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = if (sub.isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                if (!sub.isActive) {
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "Inactive",
                                                        fontSize = 9.sp,
                                                        color = SolidExpense,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = LanguageHelper.formatCurrency(subItem.currentBalance, languageMode),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (sub.isActive) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.outline
                                        )

                                        if (isEditMode) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            IconButton(
                                                onClick = { onEditAccount(sub) },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(Icons.Default.Edit, contentDescription = "Edit Sub", modifier = Modifier.size(14.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else if (groupItem.subAccounts.isEmpty()) {
                        Text(
                            text = "No sub-categories yet",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(vertical = 4.dp, horizontal = 6.dp)
                        )
                    }

                    // + Add Sub-Category Button
                    Text(
                        text = "+ Add Category to ${group.localizedName(languageMode)}",
                        fontSize = 12.sp,
                        color = SolidPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { onAddSubAccount(group) }
                            .padding(vertical = 8.dp, horizontal = 4.dp)
                    )
                }
            }
        }
    }
}
