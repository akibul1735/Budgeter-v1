package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.window.Dialog
import com.example.data.model.Account
import com.example.data.model.AccountType
import com.example.data.model.LanguageMode
import com.example.data.repository.AccountWithBalance
import com.example.ui.components.IconPickerModal
import com.example.ui.dialogs.AccountCalculationDialog
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidPrimary
import com.example.util.AccountCalcConfig
import com.example.util.IconHelper
import com.example.util.LanguageHelper

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AccountsScreen(
    accountsWithBalances: List<AccountWithBalance>,
    accountCalcConfig: AccountCalcConfig = AccountCalcConfig(),
    languageMode: LanguageMode,
    onAddAccountClick: () -> Unit,
    onAddSubAccountClick: (Account) -> Unit,
    onEditAccountClick: (Account) -> Unit,
    onToggleActiveStatus: ((Account, Boolean) -> Unit)? = null,
    onToggleIncludeStatus: ((Account, Boolean) -> Unit)? = null,
    onSaveCalculationSetting: ((Account, Boolean, Double) -> Unit)? = null,
    onResetAccountCalculation: ((Account) -> Unit)? = null,
    onResetAllCalculations: (() -> Unit)? = null,
    onUpdateAccounts: ((List<Account>) -> Unit)? = null,
    onDeleteAccounts: ((List<Account>) -> Unit)? = null
) {
    var isEditMode by remember { mutableStateOf(false) }
    var isSelectionMode by remember { mutableStateOf(false) }
    val selectedAccountIds = remember { mutableStateListOf<Long>() }

    var selectedTypeFilter by remember { mutableStateOf<AccountType?>(null) }
    val expandedMap = remember { mutableStateMapOf<Long, Boolean>() }

    // State for Adjust Calculation Dialog
    var calcDialogTarget by remember { mutableStateOf<Pair<Account, Double>?>(null) }

    // Batch Edit / Delete Dialog states
    var showBatchEditDialog by remember { mutableStateOf(false) }
    var showBatchDeleteDialog by remember { mutableStateOf(false) }

    // Flatten all accounts for selection mapping
    val allAccountsFlat = remember(accountsWithBalances) {
        val list = mutableListOf<Account>()
        accountsWithBalances.forEach { grp ->
            list.add(grp.account)
            grp.subAccounts.forEach { sub -> list.add(sub.account) }
        }
        list
    }

    val selectedAccounts = remember(allAccountsFlat, selectedAccountIds.toList()) {
        allAccountsFlat.filter { it.id in selectedAccountIds }
    }

    // Top-level parent groups for reparenting
    val allParentGroups = remember(accountsWithBalances) {
        accountsWithBalances.map { it.account }
    }

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

    fun toggleSelection(id: Long) {
        if (selectedAccountIds.contains(id)) {
            selectedAccountIds.remove(id)
        } else {
            selectedAccountIds.add(id)
        }
    }

    fun selectAll() {
        selectedAccountIds.clear()
        selectedAccountIds.addAll(allAccountsFlat.map { it.id })
    }

    fun deselectAll() {
        selectedAccountIds.clear()
    }

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

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("accounts_screen"),
            contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 14.dp, bottom = if (isSelectionMode) 88.dp else 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
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

            // --- 2. Action Bar: "Add Account", "Select / Multi", "Manage Active" ---
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
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
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

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        // Multi-select toggle button
                        FilterChip(
                            selected = isSelectionMode,
                            onClick = {
                                isSelectionMode = !isSelectionMode
                                if (!isSelectionMode) {
                                    selectedAccountIds.clear()
                                }
                            },
                            label = {
                                Text(
                                    text = if (isSelectionMode) "Done" else "Select",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (isSelectionMode) Icons.Default.CheckCircle else Icons.Default.SelectAll,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp)
                                )
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SolidPrimary,
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White
                            )
                        )

                        // Manage Active Switch
                        if (!isSelectionMode) {
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
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
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
                }
            }

            // Selection Controls bar when in selection mode
            if (isSelectionMode) {
                item {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${selectedAccountIds.size} / ${allAccountsFlat.size} selected",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextButton(
                                    onClick = { selectAll() },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("Select All", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                                TextButton(
                                    onClick = { deselectAll() },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("Deselect", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
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
                        isSelectionMode = isSelectionMode,
                        selectedAccountIds = selectedAccountIds,
                        onToggleSelection = { toggleSelection(it) },
                        isExpanded = expandedMap[groupItem.account.id] ?: true,
                        languageMode = languageMode,
                        onToggleExpand = { expandedMap[groupItem.account.id] = !(expandedMap[groupItem.account.id] ?: true) },
                        onEditAccount = onEditAccountClick,
                        onAddSubAccount = onAddSubAccountClick,
                        onToggleActiveStatus = onToggleActiveStatus,
                        onToggleIncludeStatus = onToggleIncludeStatus,
                        onRequestAdjustCalculation = { acc, bal -> calcDialogTarget = Pair(acc, bal) },
                        onRequestStartSelection = { id ->
                            if (!isSelectionMode) isSelectionMode = true
                            toggleSelection(id)
                        }
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
                        isSelectionMode = isSelectionMode,
                        selectedAccountIds = selectedAccountIds,
                        onToggleSelection = { toggleSelection(it) },
                        isExpanded = expandedMap[inactiveGroup.account.id] ?: false,
                        isInactiveSection = true,
                        languageMode = languageMode,
                        onToggleExpand = { expandedMap[inactiveGroup.account.id] = !(expandedMap[inactiveGroup.account.id] ?: false) },
                        onEditAccount = onEditAccountClick,
                        onAddSubAccount = onAddSubAccountClick,
                        onToggleActiveStatus = onToggleActiveStatus,
                        onToggleIncludeStatus = onToggleIncludeStatus,
                        onRequestAdjustCalculation = { acc, bal -> calcDialogTarget = Pair(acc, bal) },
                        onRequestStartSelection = { id ->
                            if (!isSelectionMode) isSelectionMode = true
                            toggleSelection(id)
                        }
                    )
                }
            }
        }

        // Floating Batch Action Toolbar at Bottom when accounts are selected
        if (isSelectionMode && selectedAccountIds.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp),
                tonalElevation = 8.dp,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${selectedAccountIds.size} Selected",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Batch Account Actions",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { showBatchEditDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SolidPrimary),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Edit / Icons", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { showBatchDeleteDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Delete", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Batch Edit Dialog
    if (showBatchEditDialog && selectedAccounts.isNotEmpty() && onUpdateAccounts != null) {
        AccountBatchEditDialog(
            selectedAccounts = selectedAccounts,
            allParentGroups = allParentGroups,
            languageMode = languageMode,
            onDismiss = { showBatchEditDialog = false },
            onApply = { updatedList ->
                onUpdateAccounts(updatedList)
                showBatchEditDialog = false
                selectedAccountIds.clear()
                isSelectionMode = false
            }
        )
    }

    // Batch Delete Dialog
    if (showBatchDeleteDialog && selectedAccounts.isNotEmpty() && onDeleteAccounts != null) {
        AlertDialog(
            onDismissRequest = { showBatchDeleteDialog = false },
            title = { Text("Delete ${selectedAccounts.size} Accounts/Groups?") },
            text = {
                Text(
                    "Are you sure you want to delete these ${selectedAccounts.size} selected accounts/groups? All sub-accounts and transactions linked to these accounts will be affected."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteAccounts(selectedAccounts)
                        showBatchDeleteDialog = false
                        selectedAccountIds.clear()
                        isSelectionMode = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete All Selected")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBatchDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AccountGroupCard(
    groupItem: AccountWithBalance,
    accountCalcConfig: AccountCalcConfig,
    effectiveBalance: Double,
    isEditMode: Boolean,
    isSelectionMode: Boolean = false,
    selectedAccountIds: List<Long> = emptyList(),
    onToggleSelection: (Long) -> Unit = {},
    isExpanded: Boolean,
    isInactiveSection: Boolean = false,
    languageMode: LanguageMode,
    onToggleExpand: () -> Unit,
    onEditAccount: (Account) -> Unit,
    onAddSubAccount: (Account) -> Unit,
    onToggleActiveStatus: ((Account, Boolean) -> Unit)?,
    onToggleIncludeStatus: ((Account, Boolean) -> Unit)?,
    onRequestAdjustCalculation: (Account, Double) -> Unit,
    onRequestStartSelection: (Long) -> Unit = {}
) {
    val group = groupItem.account
    val isGroupSelected = selectedAccountIds.contains(group.id)
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
            containerColor = if (isGroupSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
            else if (isInactiveSection) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            else MaterialTheme.colorScheme.surface
        ),
        border = if (isGroupSelected) androidx.compose.foundation.BorderStroke(1.5.dp, SolidPrimary) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isInactiveSection) 0.dp else 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Group Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = {
                            if (isSelectionMode) onToggleSelection(group.id)
                            else if (isEditMode) onEditAccount(group)
                            else onToggleExpand()
                        },
                        onLongClick = {
                            onRequestStartSelection(group.id)
                        }
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    if (isSelectionMode) {
                        Checkbox(
                            checked = isGroupSelected,
                            onCheckedChange = { onToggleSelection(group.id) },
                            colors = CheckboxDefaults.colors(checkedColor = SolidPrimary),
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    } else if (isEditMode) {
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

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = group.localizedName(languageMode),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isInactiveSection || !isIncluded) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface,
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
                    if (!isEditMode && !isSelectionMode && !isInactiveSection) {
                        // Include/Exclude Toggle Button
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

                        // Adjust Amount Button
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

                    if (isEditMode && !isSelectionMode) {
                        IconButton(
                            onClick = { onEditAccount(group) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Group", modifier = Modifier.size(16.dp))
                        }
                    } else if (!isSelectionMode) {
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
            AnimatedVisibility(visible = isExpanded || isSelectionMode) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, start = if (isSelectionMode) 18.dp else if (isEditMode) 4.dp else 8.dp)
                ) {
                    val displayedSubAccounts = if (isEditMode || isSelectionMode || isInactiveSection) {
                        groupItem.subAccounts
                    } else {
                        groupItem.subAccounts.filter { it.account.isActive }
                    }

                    if (displayedSubAccounts.isNotEmpty()) {
                        displayedSubAccounts.forEach { subItem ->
                            val sub = subItem.account
                            val isSubSelected = selectedAccountIds.contains(sub.id)
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
                                    .combinedClickable(
                                        onClick = {
                                            if (isSelectionMode) onToggleSelection(sub.id)
                                            else if (isEditMode) onEditAccount(sub)
                                            else onRequestAdjustCalculation(sub, subItem.currentBalance)
                                        },
                                        onLongClick = {
                                            onRequestStartSelection(sub.id)
                                        }
                                    ),
                                color = if (isSubSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (subIncluded) 0.45f else 0.2f),
                                border = if (isSubSelected) androidx.compose.foundation.BorderStroke(1.dp, SolidPrimary) else null
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
                                        if (isSelectionMode) {
                                            Checkbox(
                                                checked = isSubSelected,
                                                onCheckedChange = { onToggleSelection(sub.id) },
                                                colors = CheckboxDefaults.colors(checkedColor = SolidPrimary),
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                        } else if (isEditMode) {
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
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = sub.localizedName(languageMode),
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = if (sub.isActive && subIncluded) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                                                    textDecoration = if (!subIncluded) TextDecoration.LineThrough else TextDecoration.None,
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
                                        if (!isEditMode && !isSelectionMode && !isInactiveSection) {
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

                                        if (isEditMode && !isSelectionMode) {
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

                    if (!isSelectionMode) {
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
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountBatchEditDialog(
    selectedAccounts: List<Account>,
    allParentGroups: List<Account>,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onApply: (List<Account>) -> Unit
) {
    var selectedIcon by remember { mutableStateOf<String?>(null) }
    var selectedType by remember { mutableStateOf<AccountType?>(null) }
    var targetGroupId by remember { mutableStateOf<Long?>(null) }
    var changeParentGroup by remember { mutableStateOf(false) }
    var makeTopLevelGroup by remember { mutableStateOf(false) }
    var activeStatus by remember { mutableStateOf<Boolean?>(null) }
    var showIconPicker by remember { mutableStateOf(false) }
    var groupDropdownExpanded by remember { mutableStateOf(false) }

    val isSingle = selectedAccounts.size == 1
    val singleAcc = selectedAccounts.firstOrNull()
    var singleNameEn by remember { mutableStateOf(singleAcc?.nameEn ?: "") }
    var singleNameBn by remember { mutableStateOf(singleAcc?.nameBn ?: "") }
    var singleBalanceText by remember { mutableStateOf(singleAcc?.initialBalance?.toString() ?: "0.0") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isSingle) "Edit Account" else "Batch Edit (${selectedAccounts.size} Accounts)",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Modify properties for selected items",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // If single item, show names and balance
                if (isSingle) {
                    OutlinedTextField(
                        value = singleNameEn,
                        onValueChange = { singleNameEn = it },
                        label = { Text("Name (English)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = singleNameBn,
                        onValueChange = { singleNameBn = it },
                        label = { Text("Name (Bangla)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = singleBalanceText,
                        onValueChange = { singleBalanceText = it },
                        label = { Text("Initial Balance (৳)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Change Icon section
                Text(
                    text = "Set Icon",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showIconPicker = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(SolidPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = IconHelper.getIconByName(selectedIcon ?: selectedAccounts.first().iconName),
                                    contentDescription = null,
                                    tint = SolidPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = selectedIcon?.let { "Icon: $it" } ?: "Keep current icons (tap to change)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Text("Change", fontSize = 11.sp, color = SolidPrimary, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Account Type Section
                Text(
                    text = "Account Type",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedType == null,
                        onClick = { selectedType = null },
                        label = { Text("No Change", fontSize = 11.sp) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedType == AccountType.ASSET,
                        onClick = { selectedType = AccountType.ASSET },
                        label = { Text("Asset", fontSize = 11.sp) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedType == AccountType.LIABILITY,
                        onClick = { selectedType = AccountType.LIABILITY },
                        label = { Text("Liability", fontSize = 11.sp) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Reparenting / Group Assignment
                Text(
                    text = "Group Assignment",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = !changeParentGroup && !makeTopLevelGroup,
                        onClick = {
                            changeParentGroup = false
                            makeTopLevelGroup = false
                        },
                        label = { Text("No Change", fontSize = 11.sp) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = makeTopLevelGroup,
                        onClick = {
                            makeTopLevelGroup = true
                            changeParentGroup = false
                            targetGroupId = null
                        },
                        label = { Text("Make Group", fontSize = 11.sp) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = changeParentGroup,
                        onClick = {
                            changeParentGroup = true
                            makeTopLevelGroup = false
                            if (targetGroupId == null && allParentGroups.isNotEmpty()) {
                                targetGroupId = allParentGroups.first().id
                            }
                        },
                        label = { Text("Move Group", fontSize = 11.sp) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                if (changeParentGroup) {
                    Spacer(modifier = Modifier.height(8.dp))
                    val selectedTargetGroup = allParentGroups.firstOrNull { it.id == targetGroupId }

                    ExposedDropdownMenuBox(
                        expanded = groupDropdownExpanded,
                        onExpandedChange = { groupDropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedTargetGroup?.let { "${it.localizedName(languageMode)} (${if (it.type == AccountType.ASSET) "Assets" else "Liabilities"})" } ?: "Select Group",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Target Group") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = groupDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                            shape = RoundedCornerShape(12.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = groupDropdownExpanded,
                            onDismissRequest = { groupDropdownExpanded = false }
                        ) {
                            allParentGroups.forEach { grp ->
                                DropdownMenuItem(
                                    leadingIcon = {
                                        Icon(
                                            imageVector = IconHelper.getIconByName(grp.iconName),
                                            contentDescription = null,
                                            tint = SolidPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    text = { Text(grp.localizedName(languageMode)) },
                                    onClick = {
                                        targetGroupId = grp.id
                                        groupDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Active / Inactive status batch update
                Text(
                    text = "Active Status",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = activeStatus == null,
                        onClick = { activeStatus = null },
                        label = { Text("No Change", fontSize = 11.sp) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = activeStatus == true,
                        onClick = { activeStatus = true },
                        label = { Text("Active", fontSize = 11.sp) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = activeStatus == false,
                        onClick = { activeStatus = false },
                        label = { Text("Inactive", fontSize = 11.sp) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Apply button
                Button(
                    onClick = {
                        val modifiedList = selectedAccounts.map { acc ->
                            var updated = acc
                            if (isSingle) {
                                updated = updated.copy(
                                    nameEn = singleNameEn.ifBlank { acc.nameEn },
                                    nameBn = singleNameBn.ifBlank { acc.nameBn },
                                    initialBalance = singleBalanceText.toDoubleOrNull() ?: acc.initialBalance
                                )
                            }
                            if (selectedIcon != null) {
                                updated = updated.copy(iconName = selectedIcon!!)
                            }
                            if (selectedType != null) {
                                updated = updated.copy(type = selectedType!!)
                            }
                            if (makeTopLevelGroup) {
                                updated = updated.copy(parentId = null)
                            } else if (changeParentGroup && targetGroupId != null) {
                                updated = updated.copy(parentId = targetGroupId)
                            }
                            if (activeStatus != null) {
                                updated = updated.copy(isActive = activeStatus!!)
                            }
                            updated
                        }
                        onApply(modifiedList)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SolidPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Apply Changes to Selected", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showIconPicker) {
        IconPickerModal(
            selectedIconName = selectedIcon ?: selectedAccounts.first().iconName,
            onIconSelected = { icon ->
                selectedIcon = icon
                showIconPicker = false
            },
            onDismiss = { showIconPicker = false }
        )
    }
}
