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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.style.TextAlign
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
    EXCLUDED,
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

enum class AccountActiveStatusFilter {
    ALL,
    ACTIVE_ONLY,
    INACTIVE_ONLY
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
    initialHierarchyFilter: AccountViewHierarchyFilter = AccountViewHierarchyFilter.ALL,
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
    var hierarchyFilter by remember(initialHierarchyFilter) { mutableStateOf(initialHierarchyFilter) }
    var sortFilter by remember { mutableStateOf(AccountSortFilter.DEFAULT) }
    var statusFilter by remember { mutableStateOf(AccountActiveStatusFilter.ALL) }
    var excludeZeroBalance by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val expandedMap = remember { mutableStateMapOf<Long, Boolean>() }

    // State for Adjust Calculation Dialog
    var calcDialogTarget by remember { mutableStateOf<Pair<Account, Double>?>(null) }
    var showResetAllConfirmDialog by remember { mutableStateOf(false) }

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

    // Helper to compute actual balance for an account/group (strictly active only)
    fun computeActualGroupBalance(groupItem: AccountWithBalance): Double {
        if (!groupItem.account.isActive) return 0.0
        if (groupItem.subAccounts.isEmpty()) return groupItem.currentBalance
        return groupItem.subAccounts.filter { it.account.isActive }.sumOf { it.currentBalance }
    }

    // Actual Totals (Unmodified active accounts)
    val actualTotalAssets = remember(activeAccounts) {
        activeAccounts.filter { it.account.type == AccountType.ASSET }.sumOf { computeActualGroupBalance(it) }
    }
    val actualTotalLiabilities = remember(activeAccounts) {
        activeAccounts.filter { it.account.type == AccountType.LIABILITY }.sumOf { Math.abs(computeActualGroupBalance(it)) }
    }
    val actualNetWorth = actualTotalAssets - actualTotalLiabilities

    // Calculated Totals (Reflecting Include/Exclude & Adjustments)
    val calculatedTotalAssets = remember(activeAccounts, accountCalcConfig) {
        activeAccounts.filter { it.account.type == AccountType.ASSET }.sumOf { computeEffectiveGroupBalance(it) }
    }
    val calculatedTotalLiabilities = remember(activeAccounts, accountCalcConfig) {
        activeAccounts.filter { it.account.type == AccountType.LIABILITY }.sumOf { Math.abs(computeEffectiveGroupBalance(it)) }
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

    // Helper to check if an account / sub-account has 0 balance after adjustments
    fun isZeroBalance(accWithBalance: AccountWithBalance, effectiveBal: Double): Boolean {
        return Math.abs(effectiveBal) < 0.0001
    }

    // Helper to filter zero balance in a list of groups
    fun filterZeroBalanceGroups(groups: List<AccountWithBalance>): List<AccountWithBalance> {
        if (!excludeZeroBalance) return groups
        return groups.mapNotNull { group ->
            if (group.subAccounts.isEmpty()) {
                val effBal = computeEffectiveGroupBalance(group)
                if (isZeroBalance(group, effBal)) null else group
            } else {
                val nonZeroSubs = group.subAccounts.filter { sub ->
                    val effSubBal = computeEffectiveSubBalance(sub)
                    !isZeroBalance(sub, effSubBal)
                }
                val groupEffBal = computeEffectiveGroupBalance(group)
                val isGroupZero = Math.abs(groupEffBal) < 0.0001
                if (nonZeroSubs.isEmpty() && isGroupZero) {
                    null
                } else {
                    group.copy(subAccounts = nonZeroSubs)
                }
            }
        }
    }

    val displayedActiveGroups = remember(
        activeAccounts,
        selectedTypeFilter,
        sortFilter,
        statusFilter,
        excludeZeroBalance,
        accountCalcConfig,
        allTransactions,
        searchQuery,
        languageMode
    ) {
        if (statusFilter == AccountActiveStatusFilter.INACTIVE_ONLY) {
            emptyList()
        } else {
            val base = if (selectedTypeFilter == null) activeAccounts else activeAccounts.filter { it.account.type == selectedTypeFilter }
            val zeroFiltered = filterZeroBalanceGroups(base)
            val searchFiltered = if (searchQuery.isBlank()) zeroFiltered else zeroFiltered.mapNotNull { group ->
                val groupMatches = group.account.localizedName(languageMode).contains(searchQuery, ignoreCase = true) ||
                    group.account.nameEn.contains(searchQuery, ignoreCase = true) ||
                    group.account.nameBn.contains(searchQuery, ignoreCase = true)
                val matchingSubs = group.subAccounts.filter { sub ->
                    sub.account.localizedName(languageMode).contains(searchQuery, ignoreCase = true) ||
                    sub.account.nameEn.contains(searchQuery, ignoreCase = true) ||
                    sub.account.nameBn.contains(searchQuery, ignoreCase = true)
                }
                if (groupMatches) group
                else if (matchingSubs.isNotEmpty()) group.copy(subAccounts = matchingSubs)
                else null
            }
            sortGroups(searchFiltered)
        }
    }

    val displayedInactiveGroups = remember(
        inactiveAccounts,
        selectedTypeFilter,
        sortFilter,
        statusFilter,
        excludeZeroBalance,
        accountCalcConfig,
        allTransactions,
        searchQuery,
        languageMode
    ) {
        if (statusFilter == AccountActiveStatusFilter.ACTIVE_ONLY) {
            emptyList()
        } else {
            val base = if (selectedTypeFilter == null) inactiveAccounts else inactiveAccounts.filter { it.account.type == selectedTypeFilter }
            val zeroFiltered = filterZeroBalanceGroups(base)
            val searchFiltered = if (searchQuery.isBlank()) zeroFiltered else zeroFiltered.mapNotNull { group ->
                val groupMatches = group.account.localizedName(languageMode).contains(searchQuery, ignoreCase = true) ||
                    group.account.nameEn.contains(searchQuery, ignoreCase = true) ||
                    group.account.nameBn.contains(searchQuery, ignoreCase = true)
                val matchingSubs = group.subAccounts.filter { sub ->
                    sub.account.localizedName(languageMode).contains(searchQuery, ignoreCase = true) ||
                    sub.account.nameEn.contains(searchQuery, ignoreCase = true) ||
                    sub.account.nameBn.contains(searchQuery, ignoreCase = true)
                }
                if (groupMatches) group
                else if (matchingSubs.isNotEmpty()) group.copy(subAccounts = matchingSubs)
                else null
            }
            sortGroups(searchFiltered)
        }
    }

    // Excluded accounts view filter (both fully excluded and accounts with balance adjustments)
    val displayedExcludedGroups = remember(
        displayedActiveGroups,
        displayedInactiveGroups,
        accountCalcConfig,
        sortFilter,
        statusFilter
    ) {
        val baseGroups = when (statusFilter) {
            AccountActiveStatusFilter.ALL -> displayedActiveGroups + displayedInactiveGroups
            AccountActiveStatusFilter.ACTIVE_ONLY -> displayedActiveGroups
            AccountActiveStatusFilter.INACTIVE_ONLY -> displayedInactiveGroups
        }
        baseGroups.mapNotNull { groupItem ->
            val isGroupExcluded = !accountCalcConfig.isIncluded(groupItem.account.id)
            val isGroupAdjusted = accountCalcConfig.getSetting(groupItem.account.id).adjustmentAmount != 0.0
            val excludedSubs = groupItem.subAccounts.filter {
                !accountCalcConfig.isIncluded(it.account.id) || accountCalcConfig.getSetting(it.account.id).adjustmentAmount != 0.0
            }
            if (isGroupExcluded || isGroupAdjusted) {
                groupItem
            } else if (excludedSubs.isNotEmpty()) {
                groupItem.copy(subAccounts = sortSubAccounts(excludedSubs))
            } else {
                null
            }
        }
    }

    // Flattened Accounts for ONLY_ACCOUNTS mode
    val flattenedActiveAccounts = remember(
        activeAccounts,
        selectedTypeFilter,
        sortFilter,
        statusFilter,
        excludeZeroBalance,
        accountCalcConfig,
        allTransactions,
        searchQuery,
        languageMode
    ) {
        if (statusFilter == AccountActiveStatusFilter.INACTIVE_ONLY) {
            emptyList()
        } else {
            val items = mutableListOf<FlattenedAccountItem>()
            val filteredGroups = if (selectedTypeFilter == null) activeAccounts else activeAccounts.filter { it.account.type == selectedTypeFilter }
            for (group in filteredGroups) {
                if (group.subAccounts.isEmpty()) {
                    val eff = computeEffectiveGroupBalance(group)
                    if (!excludeZeroBalance || !isZeroBalance(group, eff)) {
                        items.add(
                            FlattenedAccountItem(
                                accountWithBalance = group,
                                parentGroup = null,
                                effectiveBalance = eff,
                                usageCount = accountUsageCount(group.account.id)
                            )
                        )
                    }
                } else {
                    val activeSubs = group.subAccounts.filter { it.account.isActive }
                    for (sub in activeSubs) {
                        val eff = computeEffectiveSubBalance(sub)
                        if (!excludeZeroBalance || !isZeroBalance(sub, eff)) {
                            items.add(
                                FlattenedAccountItem(
                                    accountWithBalance = sub,
                                    parentGroup = group.account,
                                    effectiveBalance = eff,
                                    usageCount = accountUsageCount(sub.account.id)
                                )
                            )
                        }
                    }
                }
            }
            val searchFiltered = if (searchQuery.isBlank()) items else items.filter {
                it.accountWithBalance.account.localizedName(languageMode).contains(searchQuery, ignoreCase = true) ||
                it.accountWithBalance.account.nameEn.contains(searchQuery, ignoreCase = true) ||
                it.accountWithBalance.account.nameBn.contains(searchQuery, ignoreCase = true)
            }
            when (sortFilter) {
                AccountSortFilter.DEFAULT -> searchFiltered
                AccountSortFilter.AMOUNT_HIGH_TO_LOW -> searchFiltered.sortedByDescending { it.effectiveBalance }
                AccountSortFilter.AMOUNT_LOW_TO_HIGH -> searchFiltered.sortedBy { it.effectiveBalance }
                AccountSortFilter.MOST_USED -> searchFiltered.sortedByDescending { it.usageCount }
                AccountSortFilter.LEAST_USED -> searchFiltered.sortedBy { it.usageCount }
                AccountSortFilter.NAME_AZ -> searchFiltered.sortedBy { it.accountWithBalance.account.localizedName(languageMode).lowercase() }
                AccountSortFilter.NAME_ZA -> searchFiltered.sortedByDescending { it.accountWithBalance.account.localizedName(languageMode).lowercase() }
            }
        }
    }

    val flattenedInactiveAccounts = remember(
        inactiveAccounts,
        selectedTypeFilter,
        sortFilter,
        statusFilter,
        excludeZeroBalance,
        accountCalcConfig,
        allTransactions,
        searchQuery,
        languageMode
    ) {
        if (statusFilter == AccountActiveStatusFilter.ACTIVE_ONLY) {
            emptyList()
        } else {
            val items = mutableListOf<FlattenedAccountItem>()
            val filteredGroups = if (selectedTypeFilter == null) inactiveAccounts else inactiveAccounts.filter { it.account.type == selectedTypeFilter }
            for (group in filteredGroups) {
                if (group.subAccounts.isEmpty()) {
                    val eff = computeEffectiveGroupBalance(group)
                    if (!excludeZeroBalance || !isZeroBalance(group, eff)) {
                        items.add(
                            FlattenedAccountItem(
                                accountWithBalance = group,
                                parentGroup = null,
                                effectiveBalance = eff,
                                usageCount = accountUsageCount(group.account.id)
                            )
                        )
                    }
                } else {
                    for (sub in group.subAccounts) {
                        val eff = computeEffectiveSubBalance(sub)
                        if (!excludeZeroBalance || !isZeroBalance(sub, eff)) {
                            items.add(
                                FlattenedAccountItem(
                                    accountWithBalance = sub,
                                    parentGroup = group.account,
                                    effectiveBalance = eff,
                                    usageCount = accountUsageCount(sub.account.id)
                                )
                            )
                        }
                    }
                }
            }
            val searchFiltered = if (searchQuery.isBlank()) items else items.filter {
                it.accountWithBalance.account.localizedName(languageMode).contains(searchQuery, ignoreCase = true) ||
                it.accountWithBalance.account.nameEn.contains(searchQuery, ignoreCase = true) ||
                it.accountWithBalance.account.nameBn.contains(searchQuery, ignoreCase = true)
            }
            when (sortFilter) {
                AccountSortFilter.DEFAULT -> searchFiltered
                AccountSortFilter.AMOUNT_HIGH_TO_LOW -> searchFiltered.sortedByDescending { it.effectiveBalance }
                AccountSortFilter.AMOUNT_LOW_TO_HIGH -> searchFiltered.sortedBy { it.effectiveBalance }
                AccountSortFilter.MOST_USED -> searchFiltered.sortedByDescending { it.usageCount }
                AccountSortFilter.LEAST_USED -> searchFiltered.sortedBy { it.usageCount }
                AccountSortFilter.NAME_AZ -> searchFiltered.sortedBy { it.accountWithBalance.account.localizedName(languageMode).lowercase() }
                AccountSortFilter.NAME_ZA -> searchFiltered.sortedByDescending { it.accountWithBalance.account.localizedName(languageMode).lowercase() }
            }
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

    if (showResetAllConfirmDialog && onResetAllCalculations != null) {
        AlertDialog(
            onDismissRequest = { showResetAllConfirmDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.RestartAlt,
                    contentDescription = null,
                    tint = SolidPrimary,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "সকল হিসাব রিসেট নিশ্চিতকরণ" else "Confirm Reset All Calculations",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = if (languageMode == LanguageMode.BANGLA)
                        "আপনি কি সকল অ্যাকাউন্টের কাস্টম ব্যালেন্স অ্যাডজাস্টমেন্ট ও অন্তর্ভুক্তি স্ট্যাটাস রিসেট করে স্বাভাবিক অবস্থায় ফিরিয়ে নিতে চান?"
                    else
                        "Are you sure you want to reset all account balance customizations and inclusion settings back to default?",
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResetAllCalculations()
                        showResetAllConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SolidPrimary)
                ) {
                    Text(if (languageMode == LanguageMode.BANGLA) "রিসেট করুন" else "Reset All")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showResetAllConfirmDialog = false }) {
                    Text(LanguageHelper.getString("cancel", languageMode))
                }
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
                onOpenDrawer = onOpenDrawer,
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                showSearchButton = true,
                searchPlaceholder = if (languageMode == LanguageMode.BANGLA) "অ্যাকাউন্ট খুঁজুন..." else "Search accounts..."
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
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            // Top Row: Net Worth & Status Action Button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (hasCustomizations) LanguageHelper.getString("calculated_net_worth", languageMode)
                                        else LanguageHelper.getString("net_worth", languageMode),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = LanguageHelper.formatCurrency(calculatedNetWorth, languageMode),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    if (hasCustomizations) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                                        ) {
                                            Text(
                                                text = "${LanguageHelper.getString("actual_net_worth", languageMode)}: ${LanguageHelper.formatCurrency(actualNetWorth, languageMode)}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                                            ) {
                                                Text(
                                                    text = LanguageHelper.getString("calculation_adjusted", languageMode),
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }
                                }

                                // Right Side: Status Mode Button and Reset Calculation Button below it
                                Column(
                                    horizontalAlignment = Alignment.End,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // Status Mode Button
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isEditMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(1.dp, if (isEditMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { isEditMode = !isEditMode }
                                            .testTag("net_worth_status_btn")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isEditMode) Icons.Default.Check else Icons.Default.Tune,
                                                contentDescription = "Status",
                                                tint = if (isEditMode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Text(
                                                text = if (isEditMode) (if (languageMode == LanguageMode.BANGLA) "সম্পন্ন" else "Done")
                                                else (if (languageMode == LanguageMode.BANGLA) "স্ট্যাটাস" else "Status"),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isEditMode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }

                                    // Reset calculation button placed directly below Status button
                                    if (hasCustomizations && onResetAllCalculations != null) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { showResetAllConfirmDialog = true }
                                                .testTag("net_worth_reset_all_btn")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.RestartAlt,
                                                    contentDescription = "Reset Calculations",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Text(
                                                    text = LanguageHelper.getString("reset_calculation", languageMode),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
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
                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
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
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
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
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                        )
                                    }
                                }

                                // Divider
                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(30.dp)
                                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                )

                                // Liabilities
                                Column(horizontalAlignment = Alignment.End) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = if (hasCustomizations) LanguageHelper.getString("calculated_liabilities", languageMode)
                                            else LanguageHelper.getString("liabilities", languageMode),
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
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
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
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
                                // View Scope Selector (All | Only Groups | Excluded | Only Accounts)
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
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
                                        selected = hierarchyFilter == AccountViewHierarchyFilter.EXCLUDED,
                                        label = if (languageMode == LanguageMode.BANGLA) "বর্জিত" else "Excluded",
                                        onClick = { hierarchyFilter = AccountViewHierarchyFilter.EXCLUDED }
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
                                    val hasActiveFilters = sortFilter != AccountSortFilter.DEFAULT ||
                                        statusFilter != AccountActiveStatusFilter.ALL ||
                                        excludeZeroBalance

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (hasActiveFilters) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
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
                                                imageVector = Icons.Default.FilterList,
                                                contentDescription = "Filter",
                                                tint = if (hasActiveFilters) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Text(
                                                text = getFilterButtonLabel(sortFilter, statusFilter, excludeZeroBalance, languageMode),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (hasActiveFilters) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }

                                    DropdownMenu(
                                        expanded = showFilterMenu,
                                        onDismissRequest = { showFilterMenu = false }
                                    ) {
                                        // 1. Exclude with 0 balance
                                        DropdownMenuItem(
                                            modifier = Modifier.testTag("filter_exclude_zero"),
                                            text = {
                                                Text(
                                                    text = LanguageHelper.getString("exclude_zero_balance", languageMode),
                                                    fontSize = 12.sp,
                                                    fontWeight = if (excludeZeroBalance) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (excludeZeroBalance) SolidPrimary else MaterialTheme.colorScheme.onSurface
                                                )
                                            },
                                            leadingIcon = {
                                                if (excludeZeroBalance) {
                                                    Icon(Icons.Default.Check, contentDescription = null, tint = SolidPrimary, modifier = Modifier.size(16.dp))
                                                } else {
                                                    Spacer(modifier = Modifier.size(16.dp))
                                                }
                                            },
                                            onClick = {
                                                excludeZeroBalance = !excludeZeroBalance
                                                showFilterMenu = false
                                            }
                                        )

                                        // 2. Inactive only
                                        DropdownMenuItem(
                                            modifier = Modifier.testTag("filter_inactive_only"),
                                            text = {
                                                Text(
                                                    text = LanguageHelper.getString("inactive_only", languageMode),
                                                    fontSize = 12.sp,
                                                    fontWeight = if (statusFilter == AccountActiveStatusFilter.INACTIVE_ONLY) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (statusFilter == AccountActiveStatusFilter.INACTIVE_ONLY) SolidPrimary else MaterialTheme.colorScheme.onSurface
                                                )
                                            },
                                            leadingIcon = {
                                                if (statusFilter == AccountActiveStatusFilter.INACTIVE_ONLY) {
                                                    Icon(Icons.Default.Check, contentDescription = null, tint = SolidPrimary, modifier = Modifier.size(16.dp))
                                                } else {
                                                    Spacer(modifier = Modifier.size(16.dp))
                                                }
                                            },
                                            onClick = {
                                                statusFilter = if (statusFilter == AccountActiveStatusFilter.INACTIVE_ONLY) AccountActiveStatusFilter.ALL else AccountActiveStatusFilter.INACTIVE_ONLY
                                                showFilterMenu = false
                                            }
                                        )

                                        // 3. Active only
                                        DropdownMenuItem(
                                            modifier = Modifier.testTag("filter_active_only"),
                                            text = {
                                                Text(
                                                    text = LanguageHelper.getString("active_only", languageMode),
                                                    fontSize = 12.sp,
                                                    fontWeight = if (statusFilter == AccountActiveStatusFilter.ACTIVE_ONLY) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (statusFilter == AccountActiveStatusFilter.ACTIVE_ONLY) SolidPrimary else MaterialTheme.colorScheme.onSurface
                                                )
                                            },
                                            leadingIcon = {
                                                if (statusFilter == AccountActiveStatusFilter.ACTIVE_ONLY) {
                                                    Icon(Icons.Default.Check, contentDescription = null, tint = SolidPrimary, modifier = Modifier.size(16.dp))
                                                } else {
                                                    Spacer(modifier = Modifier.size(16.dp))
                                                }
                                            },
                                            onClick = {
                                                statusFilter = if (statusFilter == AccountActiveStatusFilter.ACTIVE_ONLY) AccountActiveStatusFilter.ALL else AccountActiveStatusFilter.ACTIVE_ONLY
                                                showFilterMenu = false
                                            }
                                        )

                                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                                        // Sort options
                                        AccountSortFilter.values().forEach { filter ->
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        text = getSortFilterMenuLabel(filter, languageMode),
                                                        fontSize = 12.sp,
                                                        fontWeight = if (sortFilter == filter) FontWeight.Bold else FontWeight.Normal,
                                                        color = if (sortFilter == filter) SolidPrimary else MaterialTheme.colorScheme.onSurface
                                                    )
                                                },
                                                leadingIcon = {
                                                    if (sortFilter == filter) {
                                                        Icon(Icons.Default.Check, contentDescription = null, tint = SolidPrimary, modifier = Modifier.size(16.dp))
                                                    } else {
                                                        Spacer(modifier = Modifier.size(16.dp))
                                                    }
                                                },
                                                onClick = {
                                                    sortFilter = filter
                                                    showFilterMenu = false
                                                }
                                            )
                                        }

                                        if (hasActiveFilters) {
                                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                            DropdownMenuItem(
                                                modifier = Modifier.testTag("filter_clear_all"),
                                                text = {
                                                    Text(
                                                        text = LanguageHelper.getString("clear_filters", languageMode),
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = SlateText
                                                    )
                                                },
                                                leadingIcon = {
                                                    Icon(
                                                        imageVector = Icons.Default.Clear,
                                                        contentDescription = null,
                                                        tint = SlateText,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                },
                                                onClick = {
                                                    statusFilter = AccountActiveStatusFilter.ALL
                                                    excludeZeroBalance = false
                                                    sortFilter = AccountSortFilter.DEFAULT
                                                    showFilterMenu = false
                                                }
                                            )
                                        }

                                        if (hasCustomizations && onResetAllCalculations != null) {
                                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        text = LanguageHelper.getString("reset_calculation", languageMode),
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = MaterialTheme.colorScheme.error
                                                    )
                                                },
                                                leadingIcon = {
                                                    Icon(
                                                        imageVector = Icons.Default.RestartAlt,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.error,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                },
                                                onClick = {
                                                    showFilterMenu = false
                                                    showResetAllConfirmDialog = true
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
                if (hierarchyFilter == AccountViewHierarchyFilter.EXCLUDED) {
                    // Excluded Accounts Mode (both fully excluded and accounts with balance adjustments)
                    if (displayedExcludedGroups.isEmpty()) {
                        item {
                            EmptyExcludedCard(languageMode)
                        }
                    } else {
                        item {
                            val totalExcludedCount = remember(displayedExcludedGroups, accountCalcConfig) {
                                displayedExcludedGroups.sumOf { g ->
                                    (if (!accountCalcConfig.isIncluded(g.account.id) || accountCalcConfig.getSetting(g.account.id).adjustmentAmount != 0.0) 1 else 0) +
                                    g.subAccounts.count { !accountCalcConfig.isIncluded(it.account.id) || accountCalcConfig.getSetting(it.account.id).adjustmentAmount != 0.0 }
                                }
                            }
                            val totalExcludedSum = remember(displayedExcludedGroups, accountCalcConfig) {
                                displayedExcludedGroups.sumOf { g ->
                                    val gExcluded = if (!accountCalcConfig.isIncluded(g.account.id)) g.currentBalance
                                        else kotlin.math.abs(accountCalcConfig.getSetting(g.account.id).adjustmentAmount)
                                    val subsExcluded = g.subAccounts.sumOf { sub ->
                                        if (!accountCalcConfig.isIncluded(sub.account.id)) sub.currentBalance
                                        else kotlin.math.abs(accountCalcConfig.getSetting(sub.account.id).adjustmentAmount)
                                    }
                                    gExcluded + subsExcluded
                                }
                            }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.45f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.VisibilityOff,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = if (languageMode == LanguageMode.BANGLA) "বাদ দেওয়া / সমন্বিত হিসাব" else "Excluded & Adjusted",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.onTertiaryContainer
                                            )
                                            Text(
                                                text = if (languageMode == LanguageMode.BANGLA) "$totalExcludedCount টি অ্যাকাউন্ট বাদ রয়েছে" else "$totalExcludedCount accounts excluded",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                                            )
                                        }
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = if (languageMode == LanguageMode.BANGLA) "বাদ দেওয়া পরিমাণ" else "Excluded Amount",
                                            fontSize = 10.5.sp,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                                        )
                                        Text(
                                            text = LanguageHelper.formatCurrency(totalExcludedSum, languageMode),
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 14.sp,
                                            color = SolidExpense
                                        )
                                    }
                                }
                            }
                        }

                        items(displayedExcludedGroups, key = { "excl_${it.account.id}" }) { groupItem ->
                            val sortedGroupItem = remember(groupItem, sortFilter) {
                                groupItem.copy(subAccounts = sortSubAccounts(groupItem.subAccounts))
                            }
                            AccountGroupCard(
                                groupItem = sortedGroupItem,
                                accountCalcConfig = accountCalcConfig,
                                effectiveBalance = computeEffectiveGroupBalance(groupItem),
                                isEditMode = isEditMode,
                                isExpanded = expandedMap[groupItem.account.id] ?: true,
                                isOnlyGroupsView = false,
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
                } else if (hierarchyFilter == AccountViewHierarchyFilter.ONLY_ACCOUNTS) {
                    // Flattened Accounts Mode
                    if (flattenedActiveAccounts.isEmpty() && flattenedInactiveAccounts.isEmpty()) {
                        item {
                            EmptyAccountsCard(languageMode, statusFilter, excludeZeroBalance)
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
                            EmptyAccountsCard(languageMode, statusFilter, excludeZeroBalance)
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
            .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 7.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
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

private fun getFilterButtonLabel(
    sortFilter: AccountSortFilter,
    statusFilter: AccountActiveStatusFilter,
    excludeZeroBalance: Boolean,
    languageMode: LanguageMode
): String {
    val activeCount = (if (statusFilter != AccountActiveStatusFilter.ALL) 1 else 0) +
        (if (excludeZeroBalance) 1 else 0) +
        (if (sortFilter != AccountSortFilter.DEFAULT) 1 else 0)

    if (activeCount == 0) {
        return if (languageMode == LanguageMode.BANGLA) "ফিল্টার" else "Filter"
    }
    if (activeCount > 1) {
        return if (languageMode == LanguageMode.BANGLA) "ফিল্টার ($activeCount)" else "Filter ($activeCount)"
    }
    if (statusFilter == AccountActiveStatusFilter.ACTIVE_ONLY) {
        return if (languageMode == LanguageMode.BANGLA) "সক্রিয়" else "Active"
    }
    if (statusFilter == AccountActiveStatusFilter.INACTIVE_ONLY) {
        return if (languageMode == LanguageMode.BANGLA) "নিষ্ক্রিয়" else "Inactive"
    }
    if (excludeZeroBalance) {
        return if (languageMode == LanguageMode.BANGLA) "≠ ০ ব্যালেন্স" else "≠ 0 Balance"
    }
    return getSortFilterLabel(sortFilter, languageMode)
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
private fun EmptyAccountsCard(
    languageMode: LanguageMode,
    statusFilter: AccountActiveStatusFilter = AccountActiveStatusFilter.ALL,
    excludeZeroBalance: Boolean = false
) {
    val message = when {
        statusFilter == AccountActiveStatusFilter.INACTIVE_ONLY ->
            if (languageMode == LanguageMode.BANGLA) "কোনো নিষ্ক্রিয় অ্যাকাউন্ট নেই" else "No Inactive Accounts"
        statusFilter == AccountActiveStatusFilter.ACTIVE_ONLY ->
            if (languageMode == LanguageMode.BANGLA) "কোনো সক্রিয় অ্যাকাউন্ট নেই" else "No Active Accounts"
        excludeZeroBalance ->
            if (languageMode == LanguageMode.BANGLA) "০ ব্যালেন্স ছাড়া কোনো অ্যাকাউন্ট নেই" else "No Accounts with Non-Zero Balance"
        else -> LanguageHelper.getString("no_accounts", languageMode)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("empty_accounts_card"),
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
                text = message,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
private fun EmptyExcludedCard(languageMode: LanguageMode) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("empty_excluded_card"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Visibility,
                contentDescription = null,
                tint = SolidIncome,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = if (languageMode == LanguageMode.BANGLA) "কোনো বর্জিত অ্যাকাউন্ট নেই" else "No Excluded Accounts",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (languageMode == LanguageMode.BANGLA) "সব সক্রিয় অ্যাকাউন্ট নেট ওয়ার্থ ও হিসেবে অন্তর্ভুক্ত রয়েছে।" else "All active accounts are currently included in Net Worth and calculations.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center
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

@OptIn(ExperimentalLayoutApi::class)
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
                    IconHelper.AppIcon(
                        iconName = acc.iconName,
                        contentDescription = null,
                        tint = if (isInactiveSection || !isIncluded) MaterialTheme.colorScheme.outline else typeColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    FlowRow(
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = acc.localizedName(languageMode),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isInactiveSection || !isIncluded) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (!acc.isActive) {
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
                        } else if (isAdjusted) {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "সমন্বিত" else "Adjusted",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD97706),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFF59E0B).copy(alpha = 0.15f))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }

                    FlowRow(
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (item.parentGroup != null) {
                            Text(
                                text = item.parentGroup.localizedName(languageMode),
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(text = "•", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                        }
                        Text(
                            text = if (acc.type == AccountType.ASSET) "Asset" else "Liability",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            softWrap = false
                        )
                        if (item.usageCount > 0) {
                            Text(text = "•", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                            Text(
                                text = "${item.usageCount} ${if (languageMode == LanguageMode.BANGLA) "লেনদেন" else "txns"}",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                softWrap = false
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
                        Text(
                            text = "${if (languageMode == LanguageMode.BANGLA) "বাদ" else "Excl"}: ${LanguageHelper.formatCurrency(kotlin.math.abs(adjustment), languageMode)}",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SolidExpense
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

@OptIn(ExperimentalLayoutApi::class)
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
                        IconHelper.AppIcon(
                            iconName = group.iconName,
                            contentDescription = null,
                            tint = if (isInactiveSection || !isIncluded) MaterialTheme.colorScheme.outline else typeColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        FlowRow(
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = group.localizedName(languageMode),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isInactiveSection || !isIncluded) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface,
                                maxLines = 2,
                                lineHeight = 17.sp,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (!group.isActive) {
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
                            } else if (isAdjusted) {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "সমন্বিত" else "Adjusted",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD97706),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFFF59E0B).copy(alpha = 0.15f))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }

                        FlowRow(
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (group.type == AccountType.ASSET) "Assets Group" else "Liabilities Group",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                softWrap = false
                            )
                            if (groupItem.subAccounts.isNotEmpty() && isOnlyGroupsView) {
                                Text(
                                    text = "• ${groupItem.subAccounts.size} accounts",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.outline,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                            if (isAdjusted && isIncluded) {
                                Text(
                                    text = "• Adj: ${if (adjustment > 0) "+" else ""}${LanguageHelper.formatCurrency(adjustment, languageMode)}",
                                    fontSize = 10.sp,
                                    color = SolidPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    softWrap = false
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
                            Text(
                                text = "${if (languageMode == LanguageMode.BANGLA) "বাদ" else "Excl"}: ${LanguageHelper.formatCurrency(kotlin.math.abs(adjustment), languageMode)}",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SolidExpense
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

                                            IconHelper.AppIcon(
                                                iconName = sub.iconName,
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
                                                    } else if (subAdjusted) {
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(
                                                            text = if (languageMode == LanguageMode.BANGLA) "সমন্বিত" else "Adjusted",
                                                            fontSize = 9.sp,
                                                            color = Color(0xFFD97706),
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
                                                if (subAdjusted && subIncluded) {
                                                    Text(
                                                        text = "${if (languageMode == LanguageMode.BANGLA) "বাদ" else "Excl"}: ${LanguageHelper.formatCurrency(kotlin.math.abs(subAdjustment), languageMode)}",
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = SolidExpense
                                                    )
                                                }
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
