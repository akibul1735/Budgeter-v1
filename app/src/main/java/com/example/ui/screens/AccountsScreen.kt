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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Account
import com.example.data.model.AccountType
import com.example.data.model.LanguageMode
import com.example.data.repository.AccountWithBalance
import com.example.ui.components.AppTabHeader
import com.example.ui.dialogs.AccountCalculationDialog
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidPrimary
import com.example.util.AccountCalcConfig
import com.example.util.IconHelper
import com.example.util.LanguageHelper

@Composable
fun AccountsScreen(
    accountsWithBalances: List<AccountWithBalance>,
    accountCalcConfig: AccountCalcConfig = AccountCalcConfig(),
    languageMode: LanguageMode,
    onOpenDrawer: () -> Unit = {},
    onAddAccountClick: () -> Unit,
    onAddSubAccountClick: (Account) -> Unit,
    onEditAccountClick: (Account) -> Unit,
    onAccountClick: ((Account) -> Unit)? = null,
    onToggleActiveStatus: ((Account, Boolean) -> Unit)? = null,
    onToggleIncludeStatus: ((Account, Boolean) -> Unit)? = null,
    onSaveCalculationSetting: ((Account, Boolean, Double) -> Unit)? = null,
    onResetAccountCalculation: ((Account) -> Unit)? = null,
    onResetAllCalculations: (() -> Unit)? = null,
    onUpdateAccounts: ((List<Account>) -> Unit)? = null,
    onDeleteAccounts: ((List<Account>) -> Unit)? = null
) {
    var isEditMode by remember { mutableStateOf(false) }
    var selectedTypeFilter by remember { mutableStateOf<AccountType?>(null) }
    val expandedMap = remember { mutableStateMapOf<Long, Boolean>() }

    // State for Adjust Calculation Dialog
    var calcDialogTarget by remember { mutableStateOf<Pair<Account, Double>?>(null) }

    // Separate active and inactive accounts
    val activeAccounts = remember(accountsWithBalances) {
        accountsWithBalances.filter { it.account.isActive }
    }

    val inactiveAccounts = remember(accountsWithBalances) {
        accountsWithBalances.filter { !it.account.isActive }
    }

    // Helper to compute effective balance for a sub-account
    fun computeEffectiveSubBalance(subItem: AccountWithBalance): Double {
        val setting = accountCalcConfig.getSetting(subItem.account.id)
        if (!setting.isIncluded) return 0.0
        return subItem.currentBalance + setting.adjustmentAmount
    }

    // Helper to compute effective balance for a group
    fun computeEffectiveGroupBalance(groupItem: AccountWithBalance): Double {
        val groupSetting = accountCalcConfig.getSetting(groupItem.account.id)
        if (!groupSetting.isIncluded) return 0.0

        if (groupItem.subAccounts.isEmpty()) {
            return groupItem.currentBalance + groupSetting.adjustmentAmount
        }

        val activeSubs = groupItem.subAccounts.filter { it.account.isActive }
        val sumSubs = activeSubs.sumOf { computeEffectiveSubBalance(it) }
        return sumSubs + groupSetting.adjustmentAmount
    }

    // Actual Totals (Unmodified)
    val actualTotalAssets = remember(activeAccounts) {
        activeAccounts.filter { it.account.type == AccountType.ASSET }.sumOf { it.currentBalance }
    }
    val actualTotalLiabilities = remember(activeAccounts) {
        activeAccounts.filter { it.account.type == AccountType.LIABILITY }.sumOf { it.currentBalance }
    }
    val actualNetWorth = actualTotalAssets - actualTotalLiabilities

    // Calculated Totals (Reflecting Include/Exclude & Adjustments)
    val calculatedTotalAssets = remember(activeAccounts, accountCalcConfig) {
        activeAccounts.filter { it.account.type == AccountType.ASSET }.sumOf { computeEffectiveGroupBalance(it) }
    }
    val calculatedTotalLiabilities = remember(activeAccounts, accountCalcConfig) {
        activeAccounts.filter { it.account.type == AccountType.LIABILITY }.sumOf { computeEffectiveGroupBalance(it) }
    }
    val calculatedNetWorth = calculatedTotalAssets - calculatedTotalLiabilities

    val displayedActiveList = remember(activeAccounts, selectedTypeFilter) {
        if (selectedTypeFilter == null) activeAccounts
        else activeAccounts.filter { it.account.type == selectedTypeFilter }
    }

    val displayedInactiveList = remember(inactiveAccounts, selectedTypeFilter) {
        if (selectedTypeFilter == null) inactiveAccounts
        else inactiveAccounts.filter { it.account.type == selectedTypeFilter }
    }

    val hasCustomizations = accountCalcConfig.hasAnyCustomizations

    // Show Calculation Adjustment Dialog if requested
    calcDialogTarget?.let { (account, actualBal) ->
        AccountCalculationDialog(
            account = account,
            actualBalance = actualBal,
            currentSetting = accountCalcConfig.getSetting(account.id),
            languageMode = languageMode,
            onDismiss = { calcDialogTarget = null },
            onSave = { isIncluded, adj ->
                onSaveCalculationSetting?.invoke(account, isIncluded, adj)
                calcDialogTarget = null
            },
            onReset = {
                onResetAccountCalculation?.invoke(account)
                calcDialogTarget = null
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("accounts_screen"),
        contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 6.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // --- Tab Header ---
        item {
            AppTabHeader(
                title = LanguageHelper.getString("accounts", languageMode),
                onOpenDrawer = onOpenDrawer
            )
        }

        // --- 1. Account Calculation Summary Card (Net Worth & Assets/Liabilities) ---
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (hasCustomizations) LanguageHelper.getString("calculated_net_worth", languageMode)
                                    else LanguageHelper.getString("net_worth", languageMode),
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                if (hasCustomizations) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color.White.copy(alpha = 0.25f))
                                            .padding(horizontal = 5.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = LanguageHelper.getString("calculation_adjusted", languageMode),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = LanguageHelper.formatCurrency(calculatedNetWorth, languageMode),
                                color = Color.White,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        // Reset button if customized
                        if (hasCustomizations && onResetAllCalculations != null) {
                            OutlinedButton(
                                onClick = onResetAllCalculations,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = Color.White.copy(alpha = 0.15f),
                                    contentColor = Color.White
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Icon(Icons.Default.RestartAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = LanguageHelper.getString("reset_calculation", languageMode),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    if (hasCustomizations) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${LanguageHelper.getString("actual_net_worth", languageMode)}: ${LanguageHelper.formatCurrency(actualNetWorth, languageMode)}",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.75f)
                        )
                    }

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
                                    text = if (hasCustomizations) LanguageHelper.getString("calculated_assets", languageMode)
                                    else LanguageHelper.getString("assets", languageMode),
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = LanguageHelper.formatCurrency(calculatedTotalAssets, languageMode),
                                color = SolidIncome,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (hasCustomizations && calculatedTotalAssets != actualTotalAssets) {
                                Text(
                                    text = "Orig: ${LanguageHelper.formatCurrency(actualTotalAssets, languageMode)}",
                                    fontSize = 10.sp,
                                    color = Color.White.copy(alpha = 0.65f)
                                )
                            }
                        }

                        // Divider line
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(36.dp)
                                .background(Color.White.copy(alpha = 0.2f))
                        )

                        // Liabilities
                        Column(horizontalAlignment = Alignment.End) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (hasCustomizations) LanguageHelper.getString("calculated_liabilities", languageMode)
                                    else LanguageHelper.getString("liabilities", languageMode),
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
                                text = LanguageHelper.formatCurrency(calculatedTotalLiabilities, languageMode),
                                color = SolidExpense,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (hasCustomizations && calculatedTotalLiabilities != actualTotalLiabilities) {
                                Text(
                                    text = "Orig: ${LanguageHelper.formatCurrency(actualTotalLiabilities, languageMode)}",
                                    fontSize = 10.sp,
                                    color = Color.White.copy(alpha = 0.65f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- 2. Action Bar: "Add Account" & "Status" ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Add Account Button
                Button(
                    onClick = onAddAccountClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SolidPrimary),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier.testTag("accounts_add_account_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Account",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = LanguageHelper.getString("add_account", languageMode),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }

                // Manage Active Status Mode
                OutlinedButton(
                    onClick = { isEditMode = !isEditMode },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isEditMode) SolidPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
                        contentColor = if (isEditMode) SolidPrimary else MaterialTheme.colorScheme.onSurface
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isEditMode) SolidPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("accounts_edit_mode_btn")
                ) {
                    Icon(
                        imageVector = if (isEditMode) Icons.Default.Check else Icons.Default.Edit,
                        contentDescription = "Edit",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isEditMode) "Done" else "Status",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
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
                    accountCalcConfig = accountCalcConfig,
                    effectiveBalance = computeEffectiveGroupBalance(groupItem),
                    isEditMode = isEditMode,
                    isExpanded = expandedMap[groupItem.account.id] ?: true,
                    languageMode = languageMode,
                    onToggleExpand = { expandedMap[groupItem.account.id] = !(expandedMap[groupItem.account.id] ?: true) },
                    onEditAccount = onEditAccountClick,
                    onAccountClick = onAccountClick,
                    onAddSubAccount = onAddSubAccountClick,
                    onToggleActiveStatus = onToggleActiveStatus,
                    onToggleIncludeStatus = onToggleIncludeStatus,
                    onRequestAdjustCalculation = { acc, bal -> calcDialogTarget = Pair(acc, bal) }
                )
            }
        }

        // --- 5. Inactive Accounts Section ---
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
                    accountCalcConfig = accountCalcConfig,
                    effectiveBalance = computeEffectiveGroupBalance(inactiveGroup),
                    isEditMode = isEditMode,
                    isExpanded = expandedMap[inactiveGroup.account.id] ?: false,
                    isInactiveSection = true,
                    languageMode = languageMode,
                    onToggleExpand = { expandedMap[inactiveGroup.account.id] = !(expandedMap[inactiveGroup.account.id] ?: false) },
                    onEditAccount = onEditAccountClick,
                    onAccountClick = onAccountClick,
                    onAddSubAccount = onAddSubAccountClick,
                    onToggleActiveStatus = onToggleActiveStatus,
                    onToggleIncludeStatus = onToggleIncludeStatus,
                    onRequestAdjustCalculation = { acc, bal -> calcDialogTarget = Pair(acc, bal) }
                )
            }
        }
    }
}

@Composable
fun AccountGroupCard(
    groupItem: AccountWithBalance,
    accountCalcConfig: AccountCalcConfig,
    effectiveBalance: Double,
    isEditMode: Boolean,
    isExpanded: Boolean,
    isInactiveSection: Boolean = false,
    languageMode: LanguageMode,
    onToggleExpand: () -> Unit,
    onEditAccount: (Account) -> Unit,
    onAccountClick: ((Account) -> Unit)? = null,
    onAddSubAccount: (Account) -> Unit,
    onToggleActiveStatus: ((Account, Boolean) -> Unit)?,
    onToggleIncludeStatus: ((Account, Boolean) -> Unit)?,
    onRequestAdjustCalculation: (Account, Double) -> Unit
) {
    val group = groupItem.account
    val groupSetting = accountCalcConfig.getSetting(group.id)
    val isIncluded = groupSetting.isIncluded
    val adjustment = groupSetting.adjustmentAmount
    val isAdjusted = adjustment != 0.0

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
            // Group Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (isEditMode) onEditAccount(group)
                        else if (onAccountClick != null) onAccountClick(group)
                        else onToggleExpand()
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
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

                    // Group Icon Box
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(typeColor.copy(alpha = if (isInactiveSection || !isIncluded) 0.08f else 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = IconHelper.getIconByName(group.iconName),
                            contentDescription = null,
                            tint = if (isInactiveSection || !isIncluded) MaterialTheme.colorScheme.outline else typeColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = group.localizedName(languageMode),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isInactiveSection || !isIncluded) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface,
                                maxLines = 2,
                                lineHeight = 17.sp,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
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
                            if (!isIncluded) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = LanguageHelper.getString("excluded", languageMode),
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

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (group.type == AccountType.ASSET) "Assets Group" else "Liabilities Group",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (isAdjusted && isIncluded) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "• Adj: ${if (adjustment > 0) "+" else ""}${LanguageHelper.formatCurrency(adjustment, languageMode)}",
                                    fontSize = 10.sp,
                                    color = SolidPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                // Balance display & Quick Calc Actions
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End) {
                        // Effective / Calculated Balance
                        Text(
                            text = LanguageHelper.formatCurrency(
                                if (isIncluded) effectiveBalance else groupItem.currentBalance,
                                languageMode
                            ),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textDecoration = if (!isIncluded) TextDecoration.LineThrough else TextDecoration.None,
                            color = if (isInactiveSection || !isIncluded) MaterialTheme.colorScheme.outline
                            else if (group.type == AccountType.LIABILITY) SolidExpense else SolidIncome
                        )

                        // Original Balance if adjusted
                        if (isAdjusted && isIncluded) {
                            Text(
                                text = "Base: ${LanguageHelper.formatCurrency(groupItem.currentBalance, languageMode)}",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Calculation Controls (Include / Exclude & Adjust)
                    if (!isEditMode && !isInactiveSection) {
                        IconButton(
                            onClick = { onToggleIncludeStatus?.invoke(group, !isIncluded) },
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("group_calc_toggle_${group.id}")
                        ) {
                            Icon(
                                imageVector = if (isIncluded) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (isIncluded) "Exclude from Calc" else "Include in Calc",
                                tint = if (isIncluded) SolidIncome else SolidExpense,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        IconButton(
                            onClick = { onRequestAdjustCalculation(group, groupItem.currentBalance) },
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("group_calc_adjust_${group.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Adjust Calculation",
                                tint = if (isAdjusted) SolidPrimary else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

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
                        .padding(top = 8.dp, start = if (isEditMode) 4.dp else 8.dp)
                ) {
                    val displayedSubAccounts = if (isEditMode || isInactiveSection) {
                        groupItem.subAccounts
                    } else {
                        groupItem.subAccounts.filter { it.account.isActive }
                    }

                    if (displayedSubAccounts.isNotEmpty()) {
                        displayedSubAccounts.forEach { subItem ->
                            val sub = subItem.account
                            val subSetting = accountCalcConfig.getSetting(sub.id)
                            val subIncluded = subSetting.isIncluded
                            val subAdjustment = subSetting.adjustmentAmount
                            val subAdjusted = subAdjustment != 0.0
                            val subEffectiveBal = if (subIncluded) subItem.currentBalance + subAdjustment else 0.0

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        if (isEditMode) onEditAccount(sub)
                                        else if (onAccountClick != null) onAccountClick(sub)
                                        else onRequestAdjustCalculation(sub, subItem.currentBalance)
                                    },
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (subIncluded) 0.45f else 0.2f)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
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
                                            tint = if (sub.isActive && subIncluded) SolidPrimary else MaterialTheme.colorScheme.outline,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = sub.localizedName(languageMode),
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = if (sub.isActive && subIncluded) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                                                    textDecoration = if (!subIncluded) TextDecoration.LineThrough else TextDecoration.None,
                                                    maxLines = 2,
                                                    lineHeight = 16.sp,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.weight(1f, fill = false)
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
                                                if (!subIncluded) {
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = LanguageHelper.getString("excluded", languageMode),
                                                        fontSize = 9.sp,
                                                        color = SolidExpense,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                            if (subAdjusted && subIncluded) {
                                                Text(
                                                    text = "Base: ${LanguageHelper.formatCurrency(subItem.currentBalance, languageMode)} (${if (subAdjustment > 0) "+" else ""}${LanguageHelper.formatCurrency(subAdjustment, languageMode)})",
                                                    fontSize = 10.sp,
                                                    color = SolidPrimary,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = LanguageHelper.formatCurrency(
                                                    if (subIncluded) subEffectiveBal else subItem.currentBalance,
                                                    languageMode
                                                ),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                textDecoration = if (!subIncluded) TextDecoration.LineThrough else TextDecoration.None,
                                                color = if (!sub.isActive || !subIncluded) MaterialTheme.colorScheme.outline
                                                else MaterialTheme.colorScheme.onSurface
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(4.dp))

                                        // Sub-Account Calculation Controls
                                        if (!isEditMode && !isInactiveSection) {
                                            IconButton(
                                                onClick = { onToggleIncludeStatus?.invoke(sub, !subIncluded) },
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .testTag("sub_calc_toggle_${sub.id}")
                                            ) {
                                                Icon(
                                                    imageVector = if (subIncluded) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                    contentDescription = if (subIncluded) "Exclude" else "Include",
                                                    tint = if (subIncluded) SolidIncome else SolidExpense,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }

                                            IconButton(
                                                onClick = { onRequestAdjustCalculation(sub, subItem.currentBalance) },
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .testTag("sub_calc_adjust_${sub.id}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Tune,
                                                    contentDescription = "Adjust Calculation",
                                                    tint = if (subAdjusted) SolidPrimary else MaterialTheme.colorScheme.outline,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }

                                        if (isEditMode) {
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
