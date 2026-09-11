package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Account
import com.example.data.model.AccountType
import com.example.data.model.LanguageMode
import com.example.data.model.TransactionWithDetails
import com.example.data.repository.AccountWithBalance
import com.example.ui.components.AppTabHeader
import com.example.ui.components.AutoHidingBottomContainer
import com.example.ui.components.LocalHeaderScrollState
import com.example.ui.dialogs.AccountCalculationDialog
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidPrimary
import com.example.util.AccountCalcConfig
import com.example.util.IconHelper
import com.example.util.LanguageHelper

private val SlateText = Color(0xFF64748B)

enum class AccountViewHierarchyFilter {
    ALL,
    ONLY_GROUPS,
    ONLY_ACCOUNTS
}

enum class AccountSortFilter {
    DEFAULT,
    AMOUNT_HIGH_TO_LOW,
    AMOUNT_LOW_TO_HIGH,
    MOST_USED,
    LEAST_USED,
    NAME_AZ,
    NAME_ZA
}

data class FlattenedAccountItem(
    val accountWithBalance: AccountWithBalance,
    val parentGroup: Account?,
    val effectiveBalance: Double,
    val usageCount: Int
)

@Composable
fun AccountsScreen(
    accountsWithBalances: List<AccountWithBalance>,
    accountCalcConfig: AccountCalcConfig = AccountCalcConfig(),
    languageMode: LanguageMode,
    allTransactions: List<TransactionWithDetails> = emptyList(),
    onOpenDrawer: () -> Unit = {},
    onAddAccountClick: (() -> Unit)? = null,
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
    var hierarchyFilter by remember { mutableStateOf(AccountViewHierarchyFilter.ALL) }
    var sortFilter by remember { mutableStateOf(AccountSortFilter.DEFAULT) }
    val expandedMap = remember { mutableStateMapOf<Long, Boolean>() }

    // State for Adjust Calculation Dialog
    var calcDialogTarget by remember { mutableStateOf<Pair<Account, Double>?>(null) }

    // Usage Frequency Map from transactions
    val accountUsageMap = remember(allTransactions) {
        val map = mutableMapOf<Long, Int>()
        allTransactions.forEach { txDetail ->
            val tx = txDetail.transaction
            tx.debitAccountId?.let { id -> map[id] = (map[id] ?: 0) + 1 }
            tx.creditAccountId?.let { id -> map[id] = (map[id] ?: 0) + 1 }
        }
        map
    }

    val accountUsageCount: (Long) -> Int = { accountId ->
        accountUsageMap[accountId] ?: 0
    }

    val groupUsageCount: (AccountWithBalance) -> Int = { groupItem ->
        (accountUsageMap[groupItem.account.id] ?: 0) + groupItem.subAccounts.sumOf { accountUsageMap[it.account.id] ?: 0 }
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

    // Helper to sort a list of groups
    fun sortGroups(list: List<AccountWithBalance>): List<AccountWithBalance> {
        return when (sortFilter) {
            AccountSortFilter.DEFAULT -> list
            AccountSortFilter.AMOUNT_HIGH_TO_LOW -> list.sortedByDescending { computeEffectiveGroupBalance(it) }
            AccountSortFilter.AMOUNT_LOW_TO_HIGH -> list.sortedBy { computeEffectiveGroupBalance(it) }
            AccountSortFilter.MOST_USED -> list.sortedByDescending { groupUsageCount(it) }
            AccountSortFilter.LEAST_USED -> list.sortedBy { groupUsageCount(it) }
            AccountSortFilter.NAME_AZ -> list.sortedBy { it.account.localizedName(languageMode).lowercase() }
            AccountSortFilter.NAME_ZA -> list.sortedByDescending { it.account.localizedName(languageMode).lowercase() }
        }
    }

    // Helper to sort sub-accounts within a group
    fun sortSubAccounts(list: List<AccountWithBalance>): List<AccountWithBalance> {
        return when (sortFilter) {
            AccountSortFilter.DEFAULT -> list
            AccountSortFilter.AMOUNT_HIGH_TO_LOW -> list.sortedByDescending { computeEffectiveSubBalance(it) }
            AccountSortFilter.AMOUNT_LOW_TO_HIGH -> list.sortedBy { computeEffectiveSubBalance(it) }
            AccountSortFilter.MOST_USED -> list.sortedByDescending { accountUsageCount(it.account.id) }
            AccountSortFilter.LEAST_USED -> list.sortedBy { accountUsageCount(it.account.id) }
            AccountSortFilter.NAME_AZ -> list.sortedBy { it.account.localizedName(languageMode).lowercase() }
            AccountSortFilter.NAME_ZA -> list.sortedByDescending { it.account.localizedName(languageMode).lowercase() }
        }
    }

    val displayedActiveGroups = remember(activeAccounts, selectedTypeFilter, sortFilter, accountCalcConfig, allTransactions) {
        val base = if (selectedTypeFilter == null) activeAccounts else activeAccounts.filter { it.account.type == selectedTypeFilter }
        sortGroups(base)
    }

    val displayedInactiveGroups = remember(inactiveAccounts, selectedTypeFilter, sortFilter, accountCalcConfig, allTransactions) {
        val base = if (selectedTypeFilter == null) inactiveAccounts else inactiveAccounts.filter { it.account.type == selectedTypeFilter }
        sortGroups(base)
    }

    // Flattened Accounts for ONLY_ACCOUNTS mode
    val flattenedActiveAccounts = remember(activeAccounts, selectedTypeFilter, sortFilter, accountCalcConfig, allTransactions) {
        val items = mutableListOf<FlattenedAccountItem>()
        val filteredGroups = if (selectedTypeFilter == null) activeAccounts else activeAccounts.filter { it.account.type == selectedTypeFilter }
        for (group in filteredGroups) {
            if (group.subAccounts.isEmpty()) {
                items.add(
                    FlattenedAccountItem(
                        accountWithBalance = group,
                        parentGroup = null,
                        effectiveBalance = computeEffectiveGroupBalance(group),
                        usageCount = accountUsageCount(group.account.id)
                    )
                )
            } else {
                val activeSubs = group.subAccounts.filter { it.account.isActive }
                for (sub in activeSubs) {
                    items.add(
                        FlattenedAccountItem(
                            accountWithBalance = sub,
                            parentGroup = group.account,
                            effectiveBalance = computeEffectiveSubBalance(sub),
                            usageCount = accountUsageCount(sub.account.id)
                        )
                    )
                }
            }
        }
        when (sortFilter) {
            AccountSortFilter.DEFAULT -> items
            AccountSortFilter.AMOUNT_HIGH_TO_LOW -> items.sortedByDescending { it.effectiveBalance }
            AccountSortFilter.AMOUNT_LOW_TO_HIGH -> items.sortedBy { it.effectiveBalance }
            AccountSortFilter.MOST_USED -> items.sortedByDescending { it.usageCount }
            AccountSortFilter.LEAST_USED -> items.sortedBy { it.usageCount }
            AccountSortFilter.NAME_AZ -> items.sortedBy { it.accountWithBalance.account.localizedName(languageMode).lowercase() }
            AccountSortFilter.NAME_ZA -> items.sortedByDescending { it.accountWithBalance.account.localizedName(languageMode).lowercase() }
        }
    }

    val flattenedInactiveAccounts = remember(inactiveAccounts, selectedTypeFilter, sortFilter, accountCalcConfig, allTransactions) {
        val items = mutableListOf<FlattenedAccountItem>()
        val filteredGroups = if (selectedTypeFilter == null) inactiveAccounts else inactiveAccounts.filter { it.account.type == selectedTypeFilter }
        for (group in filteredGroups) {
            if (group.subAccounts.isEmpty()) {
                items.add(
                    FlattenedAccountItem(
                        accountWithBalance = group,
                        parentGroup = null,
                        effectiveBalance = computeEffectiveGroupBalance(group),
                        usageCount = accountUsageCount(group.account.id)
                    )
                )
            } else {
                for (sub in group.subAccounts) {
                    items.add(
                        FlattenedAccountItem(
                            accountWithBalance = sub,
                            parentGroup = group.account,
                            effectiveBalance = computeEffectiveSubBalance(sub),
                            usageCount = accountUsageCount(sub.account.id)
                        )
                    )
                }
            }
        }
        when (sortFilter) {
            AccountSortFilter.DEFAULT -> items
            AccountSortFilter.AMOUNT_HIGH_TO_LOW -> items.sortedByDescending { it.effectiveBalance }
            AccountSortFilter.AMOUNT_LOW_TO_HIGH -> items.sortedBy { it.effectiveBalance }
            AccountSortFilter.MOST_USED -> items.sortedByDescending { it.usageCount }
            AccountSortFilter.LEAST_USED -> items.sortedBy { it.usageCount }
            AccountSortFilter.NAME_AZ -> items.sortedBy { it.accountWithBalance.account.localizedName(languageMode).lowercase() }
            AccountSortFilter.NAME_ZA -> items.sortedByDescending { it.accountWithBalance.account.localizedName(languageMode).lowercase() }
        }
    }

    val hasCustomizations = accountCalcConfig.hasAnyCustomizations

    // Auto Hide/Show on scrolling for bottom buttons
    val listState = rememberLazyListState()
    var isBottomNavVisible by remember { mutableStateOf(true) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                if (delta < -8f) {
                    isBottomNavVisible = false
                } else if (delta > 8f) {
                    isBottomNavVisible = true
                }
                return Offset.Zero
            }
        }
    }

    val isAtTop by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
        }
    }

    LaunchedEffect(isAtTop) {
        if (isAtTop) {
            isBottomNavVisible = true
        }
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("accounts_screen")
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            AppTabHeader(
                title = LanguageHelper.getString("accounts", languageMode),
                onOpenDrawer = onOpenDrawer
            )

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .nestedScroll(nestedScrollConnection),
                contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 6.dp, bottom = 125.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // --- 1. Net Worth Card (with Status Button, View Scope Toggle, & Mini Filter) ---
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("net_worth_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SolidPrimary)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            // Top Row: Net Worth & Action Buttons (Status & Reset)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
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
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    if (hasCustomizations) {
                                        Text(
                                            text = "${LanguageHelper.getString("actual_net_worth", languageMode)}: ${LanguageHelper.formatCurrency(actualNetWorth, languageMode)}",
                                            fontSize = 11.sp,
                                            color = Color.White.copy(alpha = 0.75f)
                                        )
                                    }
                                }

                                // Status button and Reset button inside Net Worth Card
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // Status Mode Button
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isEditMode) Color.White else Color.White.copy(alpha = 0.18f),
                                        border = BorderStroke(1.dp, Color.White.copy(alpha = if (isEditMode) 0.9f else 0.4f)),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { isEditMode = !isEditMode }
                                            .testTag("net_worth_status_btn")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isEditMode) Icons.Default.Check else Icons.Default.Tune,
                                                contentDescription = "Status",
                                                tint = if (isEditMode) SolidPrimary else Color.White,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Text(
                                                text = if (isEditMode) (if (languageMode == LanguageMode.BANGLA) "সম্পন্ন" else "Done")
                                                else (if (languageMode == LanguageMode.BANGLA) "স্ট্যাটাস" else "Status"),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isEditMode) SolidPrimary else Color.White
                                            )
                                        }
                                    }

                                    // Reset calculation button
                                    if (hasCustomizations && onResetAllCalculations != null) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color.White.copy(alpha = 0.18f),
                                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { onResetAllCalculations() }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                                            ) {
                                                Icon(Icons.Default.RestartAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                                                Text(
                                                    text = LanguageHelper.getString("reset_calculation", languageMode),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Middle Solid Row: Assets vs Liabilities
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.Black.copy(alpha = 0.2f))
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Assets
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(7.dp)
                                                .clip(CircleShape)
                                                .background(SolidIncome)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = if (hasCustomizations) LanguageHelper.getString("calculated_assets", languageMode)
                                            else LanguageHelper.getString("assets", languageMode),
                                            color = Color.White.copy(alpha = 0.9f),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = LanguageHelper.formatCurrency(calculatedTotalAssets, languageMode),
                                        color = SolidIncome,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (hasCustomizations && calculatedTotalAssets != actualTotalAssets) {
                                        Text(
                                            text = "Orig: ${LanguageHelper.formatCurrency(actualTotalAssets, languageMode)}",
                                            fontSize = 9.sp,
                                            color = Color.White.copy(alpha = 0.65f)
                                        )
                                    }
                                }

                                // Divider
                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(30.dp)
                                        .background(Color.White.copy(alpha = 0.2f))
                                )

                                // Liabilities
                                Column(horizontalAlignment = Alignment.End) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = if (hasCustomizations) LanguageHelper.getString("calculated_liabilities", languageMode)
                                            else LanguageHelper.getString("liabilities", languageMode),
                                            color = Color.White.copy(alpha = 0.9f),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(7.dp)
                                                .clip(CircleShape)
                                                .background(SolidExpense)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = LanguageHelper.formatCurrency(calculatedTotalLiabilities, languageMode),
                                        color = SolidExpense,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (hasCustomizations && calculatedTotalLiabilities != actualTotalLiabilities) {
                                        Text(
                                            text = "Orig: ${LanguageHelper.formatCurrency(actualTotalLiabilities, languageMode)}",
                                            fontSize = 9.sp,
                                            color = Color.White.copy(alpha = 0.65f)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Bottom Controls in Net Worth Card: View Mode (All / Only Groups / Only Accounts) & Mini Filter Button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // View Scope Selector (All | Only Groups | Only Accounts)
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.Black.copy(alpha = 0.22f))
                                        .padding(2.dp),
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    ViewScopePill(
                                        selected = hierarchyFilter == AccountViewHierarchyFilter.ALL,
                                        label = if (languageMode == LanguageMode.BANGLA) "সব" else "All",
                                        onClick = { hierarchyFilter = AccountViewHierarchyFilter.ALL }
                                    )
                                    ViewScopePill(
                                        selected = hierarchyFilter == AccountViewHierarchyFilter.ONLY_GROUPS,
                                        label = if (languageMode == LanguageMode.BANGLA) "শুধুমাত্র গ্রুপ" else "Only Groups",
                                        onClick = { hierarchyFilter = AccountViewHierarchyFilter.ONLY_GROUPS }
                                    )
                                    ViewScopePill(
                                        selected = hierarchyFilter == AccountViewHierarchyFilter.ONLY_ACCOUNTS,
                                        label = if (languageMode == LanguageMode.BANGLA) "শুধুমাত্র অ্যাকাউন্ট" else "Only Accounts",
                                        onClick = { hierarchyFilter = AccountViewHierarchyFilter.ONLY_ACCOUNTS }
                                    )
                                }

                                // Mini Filter Button with Dropdown
                                Box {
                                    var showFilterMenu by remember { mutableStateOf(false) }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (sortFilter != AccountSortFilter.DEFAULT) Color.White else Color.Black.copy(alpha = 0.22f),
                                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { showFilterMenu = true }
                                            .testTag("net_worth_mini_filter_btn")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Sort,
                                                contentDescription = "Filter",
                                                tint = if (sortFilter != AccountSortFilter.DEFAULT) SolidPrimary else Color.White,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Text(
                                                text = getSortFilterLabel(sortFilter, languageMode),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (sortFilter != AccountSortFilter.DEFAULT) SolidPrimary else Color.White
                                            )
                                        }
                                    }

                                    DropdownMenu(
                                        expanded = showFilterMenu,
                                        onDismissRequest = { showFilterMenu = false }
                                    ) {
                                        AccountSortFilter.values().forEach { filter ->
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        text = getSortFilterMenuLabel(filter, languageMode),
                                                        fontSize = 12.sp,
                                                        fontWeight = if (sortFilter == filter) FontWeight.Bold else FontWeight.Normal
                                                    )
                                                },
                                                leadingIcon = {
                                                    if (sortFilter == filter) {
                                                        Icon(Icons.Default.Check, contentDescription = null, tint = SolidPrimary, modifier = Modifier.size(16.dp))
                                                    }
                                                },
                                                onClick = {
                                                    sortFilter = filter
                                                    showFilterMenu = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // --- 2. Accounts List Based on Hierarchy Filter ---
                if (hierarchyFilter == AccountViewHierarchyFilter.ONLY_ACCOUNTS) {
                    // Flattened Accounts Mode
                    if (flattenedActiveAccounts.isEmpty() && flattenedInactiveAccounts.isEmpty()) {
                        item {
                            EmptyAccountsCard(languageMode)
                        }
                    } else {
                        items(flattenedActiveAccounts, key = { "flat_${it.accountWithBalance.account.id}" }) { flatItem ->
                            SingleAccountCard(
                                item = flatItem,
                                accountCalcConfig = accountCalcConfig,
                                isEditMode = isEditMode,
                                languageMode = languageMode,
                                onEditAccount = onEditAccountClick,
                                onAccountClick = onAccountClick,
                                onToggleActiveStatus = onToggleActiveStatus,
                                onToggleIncludeStatus = onToggleIncludeStatus,
                                onRequestAdjustCalculation = { acc, bal -> calcDialogTarget = Pair(acc, bal) }
                            )
                        }

                        if (flattenedInactiveAccounts.isNotEmpty()) {
                            item {
                                InactiveHeader(count = flattenedInactiveAccounts.size)
                            }
                            items(flattenedInactiveAccounts, key = { "flat_inactive_${it.accountWithBalance.account.id}" }) { flatItem ->
                                SingleAccountCard(
                                    item = flatItem,
                                    accountCalcConfig = accountCalcConfig,
                                    isEditMode = isEditMode,
                                    isInactiveSection = true,
                                    languageMode = languageMode,
                                    onEditAccount = onEditAccountClick,
                                    onAccountClick = onAccountClick,
                                    onToggleActiveStatus = onToggleActiveStatus,
                                    onToggleIncludeStatus = onToggleIncludeStatus,
                                    onRequestAdjustCalculation = { acc, bal -> calcDialogTarget = Pair(acc, bal) }
                                )
                            }
                        }
                    }
                } else {
                    // ALL (Group + Sub-accounts) or ONLY_GROUPS
                    val isOnlyGroups = hierarchyFilter == AccountViewHierarchyFilter.ONLY_GROUPS
                    if (displayedActiveGroups.isEmpty() && displayedInactiveGroups.isEmpty()) {
                        item {
                            EmptyAccountsCard(languageMode)
                        }
                    } else {
                        items(displayedActiveGroups, key = { it.account.id }) { groupItem ->
                            val sortedGroupItem = remember(groupItem, sortFilter) {
                                if (isOnlyGroups) groupItem
                                else groupItem.copy(subAccounts = sortSubAccounts(groupItem.subAccounts))
                            }
                            AccountGroupCard(
                                groupItem = sortedGroupItem,
                                accountCalcConfig = accountCalcConfig,
                                effectiveBalance = computeEffectiveGroupBalance(groupItem),
                                isEditMode = isEditMode,
                                isExpanded = expandedMap[groupItem.account.id] ?: true,
                                isOnlyGroupsView = isOnlyGroups,
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

                        if (displayedInactiveGroups.isNotEmpty()) {
                            item {
                                InactiveHeader(count = displayedInactiveGroups.size)
                            }
                            items(displayedInactiveGroups, key = { "inactive_${it.account.id}" }) { inactiveGroup ->
                                val sortedInactiveGroup = remember(inactiveGroup, sortFilter) {
                                    if (isOnlyGroups) inactiveGroup
                                    else inactiveGroup.copy(subAccounts = sortSubAccounts(inactiveGroup.subAccounts))
                                }
                                AccountGroupCard(
                                    groupItem = sortedInactiveGroup,
                                    accountCalcConfig = accountCalcConfig,
                                    effectiveBalance = computeEffectiveGroupBalance(inactiveGroup),
                                    isEditMode = isEditMode,
                                    isExpanded = expandedMap[inactiveGroup.account.id] ?: false,
                                    isInactiveSection = true,
                                    isOnlyGroupsView = isOnlyGroups,
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
            }
        }

        // --- 3. BOTTOM PINNED CONTAINER: ASSET / ALL / LIABILITY TOGGLE + FAB ABOVE NAVIGATION TABS ---
        val headerScrollState = LocalHeaderScrollState.current
        AutoHidingBottomContainer(
            headerScrollState = headerScrollState,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // FAB button on the right above the toggle
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    FloatingActionButton(
                        onClick = { onAddAccountClick?.invoke() },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier
                            .size(52.dp)
                            .testTag("accounts_fab_add")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Account",
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                // Segmented Toggle: [ Assets (সম্পদ) | All (সব) | Liabilities (দায়) ]
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Assets Button
                        val isAsset = selectedTypeFilter == AccountType.ASSET
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isAsset) SolidIncome.copy(alpha = 0.16f) else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { selectedTypeFilter = AccountType.ASSET }
                                .testTag("acc_filter_assets")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    tint = if (isAsset) SolidIncome else SlateText,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = LanguageHelper.getString("assets", languageMode),
                                    fontSize = 12.sp,
                                    fontWeight = if (isAsset) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isAsset) SolidIncome else SlateText
                                )
                            }
                        }

                        // All Button
                        val isAll = selectedTypeFilter == null
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isAll) SolidPrimary.copy(alpha = 0.16f) else Color.Transparent,
                            modifier = Modifier
                                .weight(0.85f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { selectedTypeFilter = null }
                                .testTag("acc_filter_all")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Layers,
                                    contentDescription = null,
                                    tint = if (isAll) SolidPrimary else SlateText,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "সব" else "All",
                                    fontSize = 12.sp,
                                    fontWeight = if (isAll) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isAll) SolidPrimary else SlateText
                                )
                            }
                        }

                        // Liabilities Button
                        val isLiability = selectedTypeFilter == AccountType.LIABILITY
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isLiability) SolidExpense.copy(alpha = 0.16f) else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { selectedTypeFilter = AccountType.LIABILITY }
                                .testTag("acc_filter_liabilities")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TrendingDown,
                                    contentDescription = null,
                                    tint = if (isLiability) SolidExpense else SlateText,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = LanguageHelper.getString("liabilities", languageMode),
                                    fontSize = 12.sp,
                                    fontWeight = if (isLiability) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isLiability) SolidExpense else SlateText
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ViewScopePill(
    selected: Boolean,
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (selected) Color.White else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 7.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) SolidPrimary else Color.White.copy(alpha = 0.85f)
        )
    }
}

@Composable
private fun BottomFilterPill(
    selected: Boolean,
    label: String,
    icon: ImageVector,
    selectedColor: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (selected) selectedColor else Color.Transparent,
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun getSortFilterLabel(filter: AccountSortFilter, languageMode: LanguageMode): String {
    return when (filter) {
        AccountSortFilter.DEFAULT -> if (languageMode == LanguageMode.BANGLA) "ফিল্টার" else "Filter"
        AccountSortFilter.AMOUNT_HIGH_TO_LOW -> if (languageMode == LanguageMode.BANGLA) "পরিমাণ ↓" else "Amount ↓"
        AccountSortFilter.AMOUNT_LOW_TO_HIGH -> if (languageMode == LanguageMode.BANGLA) "পরিমাণ ↑" else "Amount ↑"
        AccountSortFilter.MOST_USED -> if (languageMode == LanguageMode.BANGLA) "বেশি ব্যবহৃত" else "Most Used"
        AccountSortFilter.LEAST_USED -> if (languageMode == LanguageMode.BANGLA) "কম ব্যবহৃত" else "Least Used"
        AccountSortFilter.NAME_AZ -> if (languageMode == LanguageMode.BANGLA) "নাম A-Z" else "Name A-Z"
        AccountSortFilter.NAME_ZA -> if (languageMode == LanguageMode.BANGLA) "নাম Z-A" else "Name Z-A"
    }
}

private fun getSortFilterMenuLabel(filter: AccountSortFilter, languageMode: LanguageMode): String {
    return when (filter) {
        AccountSortFilter.DEFAULT -> if (languageMode == LanguageMode.BANGLA) "স্বাভাবিক ক্রম" else "Default Order"
        AccountSortFilter.AMOUNT_HIGH_TO_LOW -> if (languageMode == LanguageMode.BANGLA) "টাকার পরিমাণ: বেশি থেকে কম" else "Amount: High to Low"
        AccountSortFilter.AMOUNT_LOW_TO_HIGH -> if (languageMode == LanguageMode.BANGLA) "টাকার পরিমাণ: কম থেকে বেশি" else "Amount: Low to High"
        AccountSortFilter.MOST_USED -> if (languageMode == LanguageMode.BANGLA) "সর্বাধিক ব্যবহৃত" else "Most Used / Frequent"
        AccountSortFilter.LEAST_USED -> if (languageMode == LanguageMode.BANGLA) "কম ব্যবহৃত" else "Least Used"
        AccountSortFilter.NAME_AZ -> if (languageMode == LanguageMode.BANGLA) "নাম: A থেকে Z" else "Name: A to Z"
        AccountSortFilter.NAME_ZA -> if (languageMode == LanguageMode.BANGLA) "নাম: Z থেকে A" else "Name: Z to A"
    }
}

@Composable
private fun EmptyAccountsCard(languageMode: LanguageMode) {
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

@Composable
private fun InactiveHeader(count: Int) {
    Spacer(modifier = Modifier.height(8.dp))
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
            text = "Inactive Accounts & Groups ($count)",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
fun SingleAccountCard(
    item: FlattenedAccountItem,
    accountCalcConfig: AccountCalcConfig,
    isEditMode: Boolean,
    isInactiveSection: Boolean = false,
    languageMode: LanguageMode,
    onEditAccount: (Account) -> Unit,
    onAccountClick: ((Account) -> Unit)? = null,
    onToggleActiveStatus: ((Account, Boolean) -> Unit)?,
    onToggleIncludeStatus: ((Account, Boolean) -> Unit)?,
    onRequestAdjustCalculation: (Account, Double) -> Unit
) {
    val accItem = item.accountWithBalance
    val acc = accItem.account
    val accSetting = accountCalcConfig.getSetting(acc.id)
    val isIncluded = accSetting.isIncluded
    val adjustment = accSetting.adjustmentAmount
    val isAdjusted = adjustment != 0.0

    val typeColor = when (acc.type) {
        AccountType.ASSET -> SolidIncome
        AccountType.LIABILITY -> SolidExpense
        else -> SolidPrimary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("single_account_${acc.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isInactiveSection) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isInactiveSection) 0.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    if (isEditMode) onEditAccount(acc)
                    else if (onAccountClick != null) onAccountClick(acc)
                    else onRequestAdjustCalculation(acc, accItem.currentBalance)
                }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                if (isEditMode) {
                    Switch(
                        checked = acc.isActive,
                        onCheckedChange = { active ->
                            onToggleActiveStatus?.invoke(acc, active)
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

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(typeColor.copy(alpha = if (isInactiveSection || !isIncluded) 0.08f else 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = IconHelper.getIconByName(acc.iconName),
                        contentDescription = null,
                        tint = if (isInactiveSection || !isIncluded) MaterialTheme.colorScheme.outline else typeColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = acc.localizedName(languageMode),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isInactiveSection || !isIncluded) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (!acc.isActive) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Inactive",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = SolidExpense,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(SolidExpense.copy(alpha = 0.12f))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                        if (!isIncluded) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = LanguageHelper.getString("excluded", languageMode),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = SolidExpense,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(SolidExpense.copy(alpha = 0.12f))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (item.parentGroup != null) {
                            Text(
                                text = item.parentGroup.localizedName(languageMode),
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                            Text(text = "•", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                        }
                        Text(
                            text = if (acc.type == AccountType.ASSET) "Asset" else "Liability",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (item.usageCount > 0) {
                            Text(text = "•", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                            Text(
                                text = "${item.usageCount} ${if (languageMode == LanguageMode.BANGLA) "লেনদেন" else "txns"}",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Balance & Controls
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = LanguageHelper.formatCurrency(
                            if (isIncluded) item.effectiveBalance else accItem.currentBalance,
                            languageMode
                        ),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textDecoration = if (!isIncluded) TextDecoration.LineThrough else TextDecoration.None,
                        color = if (isInactiveSection || !isIncluded) MaterialTheme.colorScheme.outline
                        else if (acc.type == AccountType.LIABILITY) SolidExpense else SolidIncome
                    )
                    if (isAdjusted && isIncluded) {
                        Text(
                            text = "Base: ${LanguageHelper.formatCurrency(accItem.currentBalance, languageMode)}",
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                if (!isEditMode && !isInactiveSection) {
                    IconButton(
                        onClick = { onToggleIncludeStatus?.invoke(acc, !isIncluded) },
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = if (isIncluded) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (isIncluded) "Exclude" else "Include",
                            tint = if (isIncluded) SolidIncome else SolidExpense,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    IconButton(
                        onClick = { onRequestAdjustCalculation(acc, accItem.currentBalance) },
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Adjust",
                            tint = if (isAdjusted) SolidPrimary else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }

                if (isEditMode) {
                    IconButton(
                        onClick = { onEditAccount(acc) },
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Account", modifier = Modifier.size(15.dp))
                    }
                }
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
    isOnlyGroupsView: Boolean = false,
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
                        else if (!isOnlyGroupsView) onToggleExpand()
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
                            if (groupItem.subAccounts.isNotEmpty() && isOnlyGroupsView) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "• ${groupItem.subAccounts.size} accounts",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
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
                    } else if (!isOnlyGroupsView) {
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

            // Sub-Accounts / Categories Under Group (only if not isOnlyGroupsView)
            if (!isOnlyGroupsView) {
                androidx.compose.animation.AnimatedVisibility(visible = isExpanded) {
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
}
