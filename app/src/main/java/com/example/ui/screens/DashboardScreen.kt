package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Account
import com.example.data.model.AccountType
import com.example.data.model.Category
import com.example.data.model.CategoryType
import com.example.data.model.LanguageMode
import com.example.data.model.MonthlyBudget
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.data.model.TransactionWithDetails
import com.example.data.repository.AccountWithBalance
import com.example.data.repository.FinancialOverview
import com.example.ui.components.AutoHidingHeaderContainer
import com.example.ui.components.ClickableAmountText
import com.example.ui.components.DashboardBackupRestoreBanner
import com.example.ui.components.DoubleEntryFlowBadge
import com.example.ui.components.PopupCalculatorDialog
import com.example.ui.dialogs.AmountBreakdownDialog
import com.example.ui.dialogs.AmountDetailInfo
import com.example.ui.dialogs.BreakdownItem
import com.example.ui.dialogs.BreakdownTabInfo
import com.example.ui.dialogs.BudgetSummaryPreviewDialog
import com.example.ui.dialogs.DailySummaryDetailDialog
import com.example.ui.dialogs.DetectedBackupsListDialog
import com.example.ui.dialogs.FormulaStep
import com.example.ui.viewmodel.DetectedBackupInfo
import com.example.ui.screens.dashboard.BudgetSummaryCard
import com.example.ui.screens.dashboard.BudgetSummarySettingsDialog
import com.example.ui.screens.dashboard.CalendarSettingsDialog
import com.example.ui.screens.dashboard.CalendarSummaryCard
import com.example.ui.screens.dashboard.CustomizeDashboardCardsDialog
import com.example.ui.screens.dashboard.DailySummaryCard
import com.example.ui.screens.dashboard.DailySummarySettingsDialog
import com.example.ui.screens.dashboard.FavoriteAccountsCard
import com.example.ui.screens.dashboard.FavoriteAccountsSelectionDialog
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidExpenseContainer
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidIncomeContainer
import com.example.ui.theme.SolidOnExpenseContainer
import com.example.ui.theme.SolidOnIncomeContainer
import com.example.ui.theme.SolidPrimary
import com.example.ui.theme.SolidPrimaryContainer
import com.example.ui.theme.SolidTransfer
import com.example.util.BudgetChartShape
import com.example.util.BudgetSummaryType
import com.example.util.CalendarDisplayMode
import com.example.util.DailyChartType
import com.example.util.DailySummaryMode
import com.example.util.DailySummaryPeriod
import com.example.util.DashboardCardType
import com.example.util.AccountCalcConfig
import com.example.util.DecimalPrecision
import com.example.util.DashboardConfig
import com.example.util.DateUtils
import com.example.util.IconHelper
import com.example.util.LanguageHelper
import java.util.Calendar

enum class AccountFilterMode {
    CALCULATED,
    EXCLUDED,
    INACTIVE
}

@Composable
fun DashboardScreen(
    overview: FinancialOverview,
    accountsWithBalances: List<AccountWithBalance>,
    accountCalcConfig: AccountCalcConfig = AccountCalcConfig(),
    recentTransactions: List<TransactionWithDetails>,
    allCategories: List<Category> = emptyList(),
    monthlyBudgets: List<MonthlyBudget> = emptyList(),
    dashboardConfig: DashboardConfig,
    languageMode: LanguageMode,
    isDemoMode: Boolean = false,
    onOpenDrawer: () -> Unit = {},
    onExitDemoMode: () -> Unit = {},
    onAddTransactionClick: (TransactionType) -> Unit,
    onTransactionClick: (Transaction) -> Unit,
    onViewAllTransactionsClick: () -> Unit,
    onAccountClick: (Account) -> Unit = {},
    onToggleCardVisibility: (DashboardCardType, Boolean) -> Unit,
    onReorderCards: (fromIndex: Int, toIndex: Int) -> Unit,
    onUpdateDailySummarySettings: (DailySummaryMode, DailySummaryPeriod, DailyChartType, Boolean, Boolean, DecimalPrecision, Boolean, Boolean) -> Unit,
    onUpdateBudgetSummarySettings: (BudgetChartShape, BudgetSummaryType, Int, Boolean, Boolean) -> Unit,
    onUpdateCalendarSettings: (CalendarDisplayMode, Boolean, Boolean) -> Unit,
    onUpdateFavoriteAccounts: (Set<Long>) -> Unit,
    onResetDashboardDefaults: () -> Unit,
    detectedBackups: List<DetectedBackupInfo> = emptyList(),
    showRestoreBanner: Boolean = false,
    onRestoreDetectedBackup: (DetectedBackupInfo, Boolean) -> Unit = { _, _ -> },
    onDismissRestoreBanner: (String?) -> Unit = {},
    onScanBackups: () -> Unit = {}
) {
    var showStandAloneCalculator by remember { mutableStateOf(false) }
    var showCustomizeCardsDialog by remember { mutableStateOf(false) }
    var showDailySettingsDialog by remember { mutableStateOf(false) }
    var showBudgetSettingsDialog by remember { mutableStateOf(false) }
    var showCalendarSettingsDialog by remember { mutableStateOf(false) }
    var showFavoriteAccountsPicker by remember { mutableStateOf(false) }
    var showDailySummaryDetail by remember { mutableStateOf(false) }
    var dailySummarySelectedDayEpoch by remember { mutableStateOf<Long?>(null) }
    var showBudgetSummaryPreview by remember { mutableStateOf(false) }
    var showRestoreListDialog by remember { mutableStateOf(false) }

    // Navigation stack for clicking any amount anywhere on the dashboard to view formula and transactions breakdown
    val amountDetailStack = remember { mutableStateListOf<AmountDetailInfo>() }

    fun showAmountDetail(info: AmountDetailInfo) {
        amountDetailStack.add(info)
    }

    // Account calculation helpers matching Accounts Screen
    fun computeSubEffective(subItem: AccountWithBalance): Double {
        val setting = accountCalcConfig.getSetting(subItem.account.id)
        if (!setting.isIncluded) return 0.0
        return subItem.currentBalance + setting.adjustmentAmount
    }

    fun computeGroupEffective(groupItem: AccountWithBalance): Double {
        val groupSetting = accountCalcConfig.getSetting(groupItem.account.id)
        if (!groupSetting.isIncluded) return 0.0
        if (groupItem.subAccounts.isEmpty()) {
            return groupItem.currentBalance + groupSetting.adjustmentAmount
        }
        val activeSubs = groupItem.subAccounts.filter { it.account.isActive }
        val sumSubs = activeSubs.sumOf { computeSubEffective(it) }
        return sumSubs + groupSetting.adjustmentAmount
    }

    val activeAccounts = remember(accountsWithBalances) {
        accountsWithBalances.filter { it.account.isActive }
    }
    val inactiveAccounts = remember(accountsWithBalances) {
        accountsWithBalances.filter { !it.account.isActive }
    }

    // Calculated Totals (Active + Included + Adjustments)
    val calculatedAssets = remember(activeAccounts, accountCalcConfig) {
        activeAccounts.filter { it.account.type == AccountType.ASSET }.sumOf { computeGroupEffective(it) }
    }
    val calculatedLiabilities = remember(activeAccounts, accountCalcConfig) {
        activeAccounts.filter { it.account.type == AccountType.LIABILITY }.sumOf { Math.abs(computeGroupEffective(it)) }
    }
    val calculatedNetWorth = calculatedAssets - calculatedLiabilities

    // Helper data holder for flattened account breakdown items
    data class AccountItemHolder(
        val accountWithBal: AccountWithBalance,
        val displayName: String,
        val balance: Double,
        val effectiveBalance: Double,
        val note: String?,
        val iconName: String?,
        val isSubAccount: Boolean,
        val type: AccountType
    )

    // 1. Calculated Asset Items (Active & Included)
    val calculatedAssetHolders = remember(activeAccounts, accountCalcConfig, languageMode) {
        val list = mutableListOf<AccountItemHolder>()
        for (group in activeAccounts.filter { it.account.type == AccountType.ASSET }) {
            val groupSetting = accountCalcConfig.getSetting(group.account.id)
            if (group.subAccounts.isEmpty()) {
                if (groupSetting.isIncluded) {
                    val eff = computeGroupEffective(group)
                    val adjNote = if (groupSetting.adjustmentAmount != 0.0) {
                        val sign = if (groupSetting.adjustmentAmount > 0) "+" else ""
                        "Adj: $sign${LanguageHelper.formatCurrency(groupSetting.adjustmentAmount, languageMode)}"
                    } else null
                    list.add(
                        AccountItemHolder(
                            accountWithBal = group,
                            displayName = group.account.localizedName(languageMode),
                            balance = group.currentBalance,
                            effectiveBalance = eff,
                            note = adjNote,
                            iconName = group.account.iconName,
                            isSubAccount = false,
                            type = AccountType.ASSET
                        )
                    )
                }
            } else {
                if (groupSetting.isIncluded) {
                    val activeSubs = group.subAccounts.filter { it.account.isActive && accountCalcConfig.isIncluded(it.account.id) }
                    if (activeSubs.isNotEmpty()) {
                        val eff = computeGroupEffective(group)
                        val noteText = if (activeSubs.size > 1) {
                            "${activeSubs.size} ${if (languageMode == LanguageMode.BANGLA) "টি সাব-একাউন্ট" else "sub-accounts"}"
                        } else null
                        list.add(
                            AccountItemHolder(
                                accountWithBal = group,
                                displayName = group.account.localizedName(languageMode),
                                balance = activeSubs.sumOf { it.currentBalance },
                                effectiveBalance = eff,
                                note = noteText,
                                iconName = group.account.iconName,
                                isSubAccount = false,
                                type = AccountType.ASSET
                            )
                        )
                    }
                }
            }
        }
        list.sortedByDescending { it.effectiveBalance }
    }

    // 2. Excluded Asset Items (Active, but Excluded from calculations - supports partial exclusion at group level)
    val excludedAssetHolders = remember(accountsWithBalances, accountCalcConfig, languageMode) {
        val list = mutableListOf<AccountItemHolder>()
        for (group in accountsWithBalances.filter { it.account.type == AccountType.ASSET }) {
            val isGroupActive = group.account.isActive
            val isGroupIncluded = accountCalcConfig.isIncluded(group.account.id)

            if (group.subAccounts.isEmpty()) {
                if (isGroupActive && !isGroupIncluded) {
                    list.add(
                        AccountItemHolder(
                            accountWithBal = group,
                            displayName = group.account.localizedName(languageMode),
                            balance = group.currentBalance,
                            effectiveBalance = group.currentBalance,
                            note = if (languageMode == LanguageMode.BANGLA) "হিসাব থেকে বাদ দেওয়া" else "Excluded from calculation",
                            iconName = group.account.iconName,
                            isSubAccount = false,
                            type = AccountType.ASSET
                        )
                    )
                }
            } else {
                if (isGroupActive) {
                    val excludedSubs = group.subAccounts.filter { it.account.isActive && (!isGroupIncluded || !accountCalcConfig.isIncluded(it.account.id)) }
                    if (excludedSubs.isNotEmpty()) {
                        val excludedBal = excludedSubs.sumOf { it.currentBalance }
                        val noteText = if (!isGroupIncluded) {
                            if (languageMode == LanguageMode.BANGLA) "সম্পূর্ণ গ্রুপ বাদ দেওয়া" else "Entire group excluded"
                        } else {
                            "${excludedSubs.size} ${if (languageMode == LanguageMode.BANGLA) "টি সাব-একাউন্ট বাদ" else "sub-accounts excluded"}"
                        }
                        list.add(
                            AccountItemHolder(
                                accountWithBal = group,
                                displayName = group.account.localizedName(languageMode),
                                balance = excludedBal,
                                effectiveBalance = excludedBal,
                                note = noteText,
                                iconName = group.account.iconName,
                                isSubAccount = false,
                                type = AccountType.ASSET
                            )
                        )
                    }
                }
            }
        }
        list.sortedByDescending { it.balance }
    }

    // 3. Inactive Asset Items (Archived / Inactive - supports partial inactive at group level)
    val inactiveAssetHolders = remember(accountsWithBalances, languageMode) {
        val list = mutableListOf<AccountItemHolder>()
        for (group in accountsWithBalances.filter { it.account.type == AccountType.ASSET }) {
            if (!group.account.isActive) {
                val bal = if (group.subAccounts.isEmpty()) group.currentBalance else group.subAccounts.sumOf { it.currentBalance }
                list.add(
                    AccountItemHolder(
                        accountWithBal = group,
                        displayName = group.account.localizedName(languageMode),
                        balance = bal,
                        effectiveBalance = bal,
                        note = if (languageMode == LanguageMode.BANGLA) "নিষ্ক্রিয় একাউন্ট" else "Archived / Inactive",
                        iconName = group.account.iconName,
                        isSubAccount = false,
                        type = AccountType.ASSET
                    )
                )
            } else if (group.subAccounts.isNotEmpty()) {
                val inactiveSubs = group.subAccounts.filter { !it.account.isActive }
                if (inactiveSubs.isNotEmpty()) {
                    val inactiveBal = inactiveSubs.sumOf { it.currentBalance }
                    list.add(
                        AccountItemHolder(
                            accountWithBal = group,
                            displayName = group.account.localizedName(languageMode),
                            balance = inactiveBal,
                            effectiveBalance = inactiveBal,
                            note = "${inactiveSubs.size} ${if (languageMode == LanguageMode.BANGLA) "টি নিষ্ক্রিয় সাব-একাউন্ট" else "inactive sub-accounts"}",
                            iconName = group.account.iconName,
                            isSubAccount = false,
                            type = AccountType.ASSET
                        )
                    )
                }
            }
        }
        list.sortedByDescending { it.balance }
    }

    // 4. Calculated Liability Items (Active & Included)
    val calculatedLiabHolders = remember(activeAccounts, accountCalcConfig, languageMode) {
        val list = mutableListOf<AccountItemHolder>()
        for (group in activeAccounts.filter { it.account.type == AccountType.LIABILITY }) {
            val groupSetting = accountCalcConfig.getSetting(group.account.id)
            if (group.subAccounts.isEmpty()) {
                if (groupSetting.isIncluded) {
                    val eff = Math.abs(computeGroupEffective(group))
                    val adjNote = if (groupSetting.adjustmentAmount != 0.0) {
                        val sign = if (groupSetting.adjustmentAmount > 0) "+" else ""
                        "Adj: $sign${LanguageHelper.formatCurrency(groupSetting.adjustmentAmount, languageMode)}"
                    } else null
                    list.add(
                        AccountItemHolder(
                            accountWithBal = group,
                            displayName = group.account.localizedName(languageMode),
                            balance = Math.abs(group.currentBalance),
                            effectiveBalance = eff,
                            note = adjNote,
                            iconName = group.account.iconName,
                            isSubAccount = false,
                            type = AccountType.LIABILITY
                        )
                    )
                }
            } else {
                if (groupSetting.isIncluded) {
                    val activeSubs = group.subAccounts.filter { it.account.isActive && accountCalcConfig.isIncluded(it.account.id) }
                    if (activeSubs.isNotEmpty()) {
                        val eff = Math.abs(computeGroupEffective(group))
                        val noteText = if (activeSubs.size > 1) {
                            "${activeSubs.size} ${if (languageMode == LanguageMode.BANGLA) "টি সাব-একাউন্ট" else "sub-accounts"}"
                        } else null
                        list.add(
                            AccountItemHolder(
                                accountWithBal = group,
                                displayName = group.account.localizedName(languageMode),
                                balance = Math.abs(activeSubs.sumOf { it.currentBalance }),
                                effectiveBalance = eff,
                                note = noteText,
                                iconName = group.account.iconName,
                                isSubAccount = false,
                                type = AccountType.LIABILITY
                            )
                        )
                    }
                }
            }
        }
        list.sortedByDescending { it.effectiveBalance }
    }

    // 5. Excluded Liability Items (Active, but Excluded from calculations - supports partial exclusion at group level)
    val excludedLiabHolders = remember(accountsWithBalances, accountCalcConfig, languageMode) {
        val list = mutableListOf<AccountItemHolder>()
        for (group in accountsWithBalances.filter { it.account.type == AccountType.LIABILITY }) {
            val isGroupActive = group.account.isActive
            val isGroupIncluded = accountCalcConfig.isIncluded(group.account.id)

            if (group.subAccounts.isEmpty()) {
                if (isGroupActive && !isGroupIncluded) {
                    list.add(
                        AccountItemHolder(
                            accountWithBal = group,
                            displayName = group.account.localizedName(languageMode),
                            balance = Math.abs(group.currentBalance),
                            effectiveBalance = Math.abs(group.currentBalance),
                            note = if (languageMode == LanguageMode.BANGLA) "হিসাব থেকে বাদ দেওয়া" else "Excluded from calculation",
                            iconName = group.account.iconName,
                            isSubAccount = false,
                            type = AccountType.LIABILITY
                        )
                    )
                }
            } else {
                if (isGroupActive) {
                    val excludedSubs = group.subAccounts.filter { it.account.isActive && (!isGroupIncluded || !accountCalcConfig.isIncluded(it.account.id)) }
                    if (excludedSubs.isNotEmpty()) {
                        val excludedBal = Math.abs(excludedSubs.sumOf { it.currentBalance })
                        val noteText = if (!isGroupIncluded) {
                            if (languageMode == LanguageMode.BANGLA) "সম্পূর্ণ গ্রুপ বাদ দেওয়া" else "Entire group excluded"
                        } else {
                            "${excludedSubs.size} ${if (languageMode == LanguageMode.BANGLA) "টি সাব-একাউন্ট বাদ" else "sub-accounts excluded"}"
                        }
                        list.add(
                            AccountItemHolder(
                                accountWithBal = group,
                                displayName = group.account.localizedName(languageMode),
                                balance = excludedBal,
                                effectiveBalance = excludedBal,
                                note = noteText,
                                iconName = group.account.iconName,
                                isSubAccount = false,
                                type = AccountType.LIABILITY
                            )
                        )
                    }
                }
            }
        }
        list.sortedByDescending { it.balance }
    }

    // 6. Inactive Liability Items (Archived / Inactive - supports partial inactive at group level)
    val inactiveLiabHolders = remember(accountsWithBalances, languageMode) {
        val list = mutableListOf<AccountItemHolder>()
        for (group in accountsWithBalances.filter { it.account.type == AccountType.LIABILITY }) {
            if (!group.account.isActive) {
                val bal = Math.abs(if (group.subAccounts.isEmpty()) group.currentBalance else group.subAccounts.sumOf { it.currentBalance })
                list.add(
                    AccountItemHolder(
                        accountWithBal = group,
                        displayName = group.account.localizedName(languageMode),
                        balance = bal,
                        effectiveBalance = bal,
                        note = if (languageMode == LanguageMode.BANGLA) "নিষ্ক্রিয় দায়" else "Archived / Inactive",
                        iconName = group.account.iconName,
                        isSubAccount = false,
                        type = AccountType.LIABILITY
                    )
                )
            } else if (group.subAccounts.isNotEmpty()) {
                val inactiveSubs = group.subAccounts.filter { !it.account.isActive }
                if (inactiveSubs.isNotEmpty()) {
                    val inactiveBal = Math.abs(inactiveSubs.sumOf { it.currentBalance })
                    list.add(
                        AccountItemHolder(
                            accountWithBal = group,
                            displayName = group.account.localizedName(languageMode),
                            balance = inactiveBal,
                            effectiveBalance = inactiveBal,
                            note = "${inactiveSubs.size} ${if (languageMode == LanguageMode.BANGLA) "টি নিষ্ক্রিয় সাব-একাউন্ট" else "inactive sub-accounts"}",
                            iconName = group.account.iconName,
                            isSubAccount = false,
                            type = AccountType.LIABILITY
                        )
                    )
                }
            }
        }
        list.sortedByDescending { it.balance }
    }

    // Excluded and Inactive Totals
    val excludedAssets = remember(excludedAssetHolders) { excludedAssetHolders.sumOf { it.balance } }
    val excludedLiabilities = remember(excludedLiabHolders) { excludedLiabHolders.sumOf { it.balance } }
    val excludedNetWorth = excludedAssets - excludedLiabilities

    val inactiveAssets = remember(inactiveAssetHolders) { inactiveAssetHolders.sumOf { it.balance } }
    val inactiveLiabilities = remember(inactiveLiabHolders) { inactiveLiabHolders.sumOf { it.balance } }
    val inactiveNetWorth = inactiveAssets - inactiveLiabilities

    // Drill down helper for a list of transactions (e.g. Inflows or Outflows)
    fun openAccountTransactionsList(
        title: String,
        subtitle: String,
        txs: List<TransactionWithDetails>,
        totalAmount: Double,
        badgeColor: Color
    ) {
        showAmountDetail(
            AmountDetailInfo(
                title = title,
                subtitle = subtitle,
                totalAmount = totalAmount,
                formulaExplanation = if (languageMode == LanguageMode.BANGLA)
                    "মোট ${txs.size} টি সম্পর্কিত লেনদেনের তালিকা।"
                else
                    "List of ${txs.size} related transactions.",
                relatedTransactions = txs,
                customBadgeColor = badgeColor
            )
        )
    }

    fun openAccountBreakdown(
        accWithBal: AccountWithBalance,
        filterMode: AccountFilterMode = AccountFilterMode.CALCULATED
    ) {
        val acc = accWithBal.account
        val isGroupIncluded = accountCalcConfig.isIncluded(acc.id)
        val isGroupActive = acc.isActive

        // Filter subaccounts according to the source tab filterMode
        val relevantSubAccounts = if (accWithBal.subAccounts.isNotEmpty()) {
            when (filterMode) {
                AccountFilterMode.CALCULATED -> accWithBal.subAccounts.filter { 
                    it.account.isActive && isGroupActive && isGroupIncluded && accountCalcConfig.isIncluded(it.account.id) 
                }
                AccountFilterMode.EXCLUDED -> accWithBal.subAccounts.filter { 
                    it.account.isActive && isGroupActive && (!isGroupIncluded || !accountCalcConfig.isIncluded(it.account.id)) 
                }
                AccountFilterMode.INACTIVE -> if (!isGroupActive) accWithBal.subAccounts else accWithBal.subAccounts.filter { !it.account.isActive }
            }
        } else {
            emptyList()
        }

        val targetAccountIds = if (accWithBal.subAccounts.isNotEmpty()) {
            val ids = relevantSubAccounts.map { it.account.id }.toSet()
            if (ids.isNotEmpty()) ids else setOf(acc.id)
        } else {
            setOf(acc.id)
        }

        // All transactions related to this account or its filtered sub-accounts
        val accTxs = recentTransactions.filter { txItem ->
            val tx = txItem.transaction
            (tx.debitAccountId != null && targetAccountIds.contains(tx.debitAccountId)) ||
            (tx.creditAccountId != null && targetAccountIds.contains(tx.creditAccountId))
        }

        // Inflows (Deposits, incoming transfers, loans received)
        val inflowTxs = accTxs.filter { txItem ->
            val tx = txItem.transaction
            if (acc.type == AccountType.ASSET) {
                tx.debitAccountId != null && targetAccountIds.contains(tx.debitAccountId)
            } else {
                tx.creditAccountId != null && targetAccountIds.contains(tx.creditAccountId)
            }
        }

        // Outflows (Expenses paid, outgoing transfers, debt repayments)
        val outflowTxs = accTxs.filter { txItem ->
            val tx = txItem.transaction
            if (acc.type == AccountType.ASSET) {
                tx.creditAccountId != null && targetAccountIds.contains(tx.creditAccountId)
            } else {
                tx.debitAccountId != null && targetAccountIds.contains(tx.debitAccountId)
            }
        }

        val totalInflows = inflowTxs.sumOf { it.transaction.amount }
        val totalOutflows = outflowTxs.sumOf { it.transaction.amount }
        val accName = acc.localizedName(languageMode)

        val breakdownItems = mutableListOf<BreakdownItem>()

        // 1. Inflows breakdown item
        breakdownItems.add(
            BreakdownItem(
                name = if (languageMode == LanguageMode.BANGLA) "মোট ইনফ্লো / জমা" else "Total Inflow / Deposits",
                amount = totalInflows,
                iconName = "account_balance_wallet",
                color = SolidIncome,
                count = inflowTxs.size,
                note = if (languageMode == LanguageMode.BANGLA) "ট্যাপ করে সকল ইনফ্লো লেনদেন দেখুন" else "Tap to view incoming transactions",
                onClick = {
                    openAccountTransactionsList(
                        title = if (languageMode == LanguageMode.BANGLA) "$accName - ইনফ্লো তালিকা" else "$accName - Inflows",
                        subtitle = if (languageMode == LanguageMode.BANGLA) "মোট জমা ও আগমনী লেনদেনের বিস্তারিত" else "All incoming and deposit transactions",
                        txs = inflowTxs,
                        totalAmount = totalInflows,
                        badgeColor = SolidIncome
                    )
                }
            )
        )

        // 2. Outflows breakdown item
        breakdownItems.add(
            BreakdownItem(
                name = if (languageMode == LanguageMode.BANGLA) "মোট আউটফ্লো / খরচ" else "Total Outflow / Payments",
                amount = -totalOutflows,
                iconName = "credit_card",
                color = SolidExpense,
                count = outflowTxs.size,
                note = if (languageMode == LanguageMode.BANGLA) "ট্যাপ করে সকল আউটফ্লো লেনদেন দেখুন" else "Tap to view outgoing transactions",
                onClick = {
                    openAccountTransactionsList(
                        title = if (languageMode == LanguageMode.BANGLA) "$accName - আউটফ্লো তালিকা" else "$accName - Outflows",
                        subtitle = if (languageMode == LanguageMode.BANGLA) "মোট খরচ ও বহির্গামী লেনদেনের বিস্তারিত" else "All outgoing and payment transactions",
                        txs = outflowTxs,
                        totalAmount = totalOutflows,
                        badgeColor = SolidExpense
                    )
                }
            )
        )

        // 3. If parent group has sub-accounts, list relevant sub-accounts as drill-down items
        if (relevantSubAccounts.isNotEmpty()) {
            relevantSubAccounts.forEach { sub ->
                val subBal = if (filterMode == AccountFilterMode.CALCULATED) {
                    computeSubEffective(sub)
                } else {
                    sub.currentBalance
                }
                breakdownItems.add(
                    BreakdownItem(
                        name = sub.account.localizedName(languageMode),
                        amount = if (sub.account.type == AccountType.LIABILITY) -Math.abs(subBal) else subBal,
                        iconName = sub.account.iconName,
                        color = if (sub.account.type == AccountType.ASSET) SolidIncome else SolidExpense,
                        note = if (languageMode == LanguageMode.BANGLA) "সাব-একাউন্ট (ট্যাপ করে বিস্তারিত দেখুন)" else "Sub-account (tap for details)",
                        onClick = { openAccountBreakdown(sub, filterMode) }
                    )
                )
            }
        }

        val sortedBreakdownItems = breakdownItems.sortedByDescending { Math.abs(it.amount) }

        // Compute total amount for header display
        val displayAmount: Double = if (accWithBal.subAccounts.isEmpty()) {
            val raw = if (filterMode == AccountFilterMode.CALCULATED) computeGroupEffective(accWithBal) else accWithBal.currentBalance
            if (acc.type == AccountType.LIABILITY) Math.abs(raw) else raw
        } else {
            val sumSubs = if (filterMode == AccountFilterMode.CALCULATED) {
                relevantSubAccounts.sumOf { computeSubEffective(it) } + if (isGroupIncluded) accountCalcConfig.getAdjustment(acc.id) else 0.0
            } else {
                relevantSubAccounts.sumOf { it.currentBalance }
            }
            if (acc.type == AccountType.LIABILITY) Math.abs(sumSubs) else sumSubs
        }

        val filterSuffix = when (filterMode) {
            AccountFilterMode.CALCULATED -> if (languageMode == LanguageMode.BANGLA) " [হিসাবকৃত]" else " [Calculated]"
            AccountFilterMode.EXCLUDED -> if (languageMode == LanguageMode.BANGLA) " [বাদ দেওয়া]" else " [Excluded]"
            AccountFilterMode.INACTIVE -> if (languageMode == LanguageMode.BANGLA) " [নিষ্ক্রিয়]" else " [Inactive]"
        }

        showAmountDetail(
            AmountDetailInfo(
                title = "$accName$filterSuffix",
                subtitle = if (acc.type == AccountType.ASSET) (if (languageMode == LanguageMode.BANGLA) "সম্পদ একাউন্ট" else "Asset Account") else (if (languageMode == LanguageMode.BANGLA) "দায় একাউন্ট" else "Liability Account"),
                totalAmount = displayAmount,
                formulaExplanation = if (languageMode == LanguageMode.BANGLA)
                    "বর্তমান ব্যালেন্স = প্রারম্ভিক ব্যালেন্স + মোট জমা (ডেবিট) − মোট খরচ/উত্তোলন (ক্রেডিট)। বিস্তারিত দেখতে ইনফ্লো বা আউটফ্লোতে ট্যাপ করুন।"
                else
                    "Current Balance = Opening Balance + All Inflows (Debits) − All Outflows (Credits). Tap Inflows or Outflows to inspect transactions.",
                formulaSteps = emptyList(),
                relatedBreakdownItems = sortedBreakdownItems,
                relatedTransactions = emptyList(),
                customBadgeColor = when (filterMode) {
                    AccountFilterMode.CALCULATED -> if (acc.type == AccountType.ASSET) SolidIncome else SolidExpense
                    AccountFilterMode.EXCLUDED -> Color(0xFFF59E0B)
                    AccountFilterMode.INACTIVE -> Color.Gray
                }
            )
        )
    }

    // Helper functions for building calculation breakdowns with Calculated, Excluded, Inactive tabs
    fun openAssetsBreakdown(initialTab: Int = 0) {
        // Tab 1: Calculated (Active & Included accounts only)
        val calcBreakdownItems = calculatedAssetHolders.map { holder ->
            BreakdownItem(
                name = holder.displayName,
                amount = holder.effectiveBalance,
                percentage = if (calculatedAssets > 0) (holder.effectiveBalance / calculatedAssets) * 100.0 else 0.0,
                iconName = holder.iconName,
                color = SolidIncome,
                note = holder.note,
                onClick = { openAccountBreakdown(holder.accountWithBal, AccountFilterMode.CALCULATED) }
            )
        }.sortedByDescending { Math.abs(it.amount) }
        val calculatedTab = BreakdownTabInfo(
            title = if (languageMode == LanguageMode.BANGLA) "হিসাবকৃত" else "Calculated",
            totalAmount = calculatedAssets,
            subtitle = if (languageMode == LanguageMode.BANGLA) "সক্রিয় ও অন্তর্ভুক্ত সম্পদ একাউন্ট (সমন্বয় সহ)" else "Active & included asset accounts (with adjustments)",
            formulaExplanation = if (languageMode == LanguageMode.BANGLA)
                "শুধুমাত্র সক্রিয় এবং গণনায় অন্তর্ভুক্ত সম্পদ একাউন্টের ব্যালেন্স ও সমন্বয়ের সমষ্টি। বাদ দেওয়া ও নিষ্ক্রিয় একাউন্টগুলো এখানে যুক্ত করা হয়নি।"
            else
                "Sum of active and included asset account balances plus adjustments. Excluded and inactive accounts are removed.",
            items = calcBreakdownItems,
            transactions = emptyList(),
            customBadgeColor = SolidIncome,
            emptyMessage = if (languageMode == LanguageMode.BANGLA) "কোনো সক্রিয় ও অন্তর্ভুক্ত সম্পদ একাউন্ট পাওয়া যায়নি।" else "No active & included asset accounts found."
        )

        // Tab 2: Excluded (Active accounts excluded by user setting)
        val excludedBreakdownItems = excludedAssetHolders.map { holder ->
            BreakdownItem(
                name = holder.displayName,
                amount = holder.balance,
                percentage = if (excludedAssets > 0) (holder.balance / excludedAssets) * 100.0 else 0.0,
                iconName = holder.iconName,
                color = Color(0xFFF59E0B),
                note = holder.note,
                onClick = { openAccountBreakdown(holder.accountWithBal, AccountFilterMode.EXCLUDED) }
            )
        }.sortedByDescending { Math.abs(it.amount) }
        val excludedTab = BreakdownTabInfo(
            title = if (languageMode == LanguageMode.BANGLA) "বাদ দেওয়া" else "Excluded",
            totalAmount = excludedAssets,
            subtitle = if (languageMode == LanguageMode.BANGLA) "গণনা থেকে বাদ দেওয়া সম্পদ একাউন্ট" else "Active asset accounts excluded from calculations",
            formulaExplanation = if (languageMode == LanguageMode.BANGLA)
                "এই একাউন্টগুলো সক্রিয় হলেও একাউন্ট সেটিংস থেকে ক্যালকুলেশনে বাদ (Excluded) রাখা হয়েছে।"
            else
                "These accounts are active but toggled OFF from calculations in Accounts configuration.",
            items = excludedBreakdownItems,
            transactions = emptyList(),
            customBadgeColor = Color(0xFFF59E0B),
            emptyMessage = if (languageMode == LanguageMode.BANGLA) "গণনা থেকে বাদ দেওয়া কোনো সম্পদ একাউন্ট নেই।" else "No asset accounts are excluded from calculations."
        )

        // Tab 3: Inactive (All inactive asset accounts)
        val inactiveBreakdownItems = inactiveAssetHolders.map { holder ->
            BreakdownItem(
                name = holder.displayName,
                amount = holder.balance,
                percentage = if (inactiveAssets > 0) (holder.balance / inactiveAssets) * 100.0 else 0.0,
                iconName = holder.iconName,
                color = Color.Gray,
                note = holder.note,
                onClick = { openAccountBreakdown(holder.accountWithBal, AccountFilterMode.INACTIVE) }
            )
        }.sortedByDescending { Math.abs(it.amount) }
        val inactiveTab = BreakdownTabInfo(
            title = if (languageMode == LanguageMode.BANGLA) "নিষ্ক্রিয়" else "Inactive",
            totalAmount = inactiveAssets,
            subtitle = if (languageMode == LanguageMode.BANGLA) "সকল নিষ্ক্রিয় সম্পদ একাউন্ট" else "Archived asset accounts",
            formulaExplanation = if (languageMode == LanguageMode.BANGLA)
                "এই একাউন্টগুলো নিষ্ক্রিয় (Inactive) হিসেবে চিহ্নিত রয়েছে এবং মূল আর্থিক হিসেবে যোগ করা হয় না।"
            else
                "These asset accounts are marked as inactive and are excluded from main financial calculations.",
            items = inactiveBreakdownItems,
            transactions = emptyList(),
            customBadgeColor = Color.Gray,
            emptyMessage = if (languageMode == LanguageMode.BANGLA) "কোনো নিষ্ক্রিয় সম্পদ একাউন্ট নেই।" else "No inactive asset accounts found."
        )

        val allTabs = listOf(calculatedTab, excludedTab, inactiveTab)
        val activeTab = allTabs.getOrElse(initialTab) { calculatedTab }

        showAmountDetail(
            AmountDetailInfo(
                title = if (languageMode == LanguageMode.BANGLA) "সম্পদ হিসাব ও বিবরণ" else "Assets Breakdown",
                subtitle = if (languageMode == LanguageMode.BANGLA) "হিসাবকৃত, বাদ দেওয়া ও নিষ্ক্রিয় সম্পদ" else "Calculated, Excluded & Inactive Assets",
                totalAmount = activeTab.totalAmount,
                formulaExplanation = activeTab.formulaExplanation,
                relatedBreakdownItems = activeTab.items,
                relatedTransactions = emptyList(),
                tabs = allTabs,
                defaultTabIndex = initialTab,
                customBadgeColor = SolidIncome
            )
        )
    }

    fun openLiabilitiesBreakdown(initialTab: Int = 0) {
        // Tab 1: Calculated (Active & Included liability accounts only)
        val calcBreakdownItems = calculatedLiabHolders.map { holder ->
            BreakdownItem(
                name = holder.displayName,
                amount = holder.effectiveBalance,
                percentage = if (calculatedLiabilities > 0) (holder.effectiveBalance / calculatedLiabilities) * 100.0 else 0.0,
                iconName = holder.iconName,
                color = SolidExpense,
                note = holder.note,
                onClick = { openAccountBreakdown(holder.accountWithBal, AccountFilterMode.CALCULATED) }
            )
        }.sortedByDescending { Math.abs(it.amount) }
        val calculatedTab = BreakdownTabInfo(
            title = if (languageMode == LanguageMode.BANGLA) "হিসাবকৃত" else "Calculated",
            totalAmount = calculatedLiabilities,
            subtitle = if (languageMode == LanguageMode.BANGLA) "সক্রিয় ও অন্তর্ভুক্ত দায় ও ঋণ (সমন্বয় সহ)" else "Active & included liabilities (with adjustments)",
            formulaExplanation = if (languageMode == LanguageMode.BANGLA)
                "শুধুমাত্র সক্রিয় এবং গণনায় অন্তর্ভুক্ত দায় একাউন্টের ব্যালেন্স ও সমন্বয়ের সমষ্টি। বাদ দেওয়া ও নিষ্ক্রিয় একাউন্টগুলো বাদ দেওয়া হয়েছে।"
            else
                "Sum of active and included liability balances plus adjustments. Excluded and inactive accounts are removed.",
            items = calcBreakdownItems,
            transactions = emptyList(),
            customBadgeColor = SolidExpense,
            emptyMessage = if (languageMode == LanguageMode.BANGLA) "কোনো সক্রিয় ও অন্তর্ভুক্ত দায় একাউন্ট পাওয়া যায়নি।" else "No active & included liability accounts found."
        )

        // Tab 2: Excluded (Active liability accounts excluded by user setting)
        val excludedBreakdownItems = excludedLiabHolders.map { holder ->
            BreakdownItem(
                name = holder.displayName,
                amount = holder.balance,
                percentage = if (excludedLiabilities > 0) (holder.balance / excludedLiabilities) * 100.0 else 0.0,
                iconName = holder.iconName,
                color = Color(0xFFF59E0B),
                note = holder.note,
                onClick = { openAccountBreakdown(holder.accountWithBal, AccountFilterMode.EXCLUDED) }
            )
        }.sortedByDescending { Math.abs(it.amount) }
        val excludedTab = BreakdownTabInfo(
            title = if (languageMode == LanguageMode.BANGLA) "বাদ দেওয়া" else "Excluded",
            totalAmount = excludedLiabilities,
            subtitle = if (languageMode == LanguageMode.BANGLA) "গণনা থেকে বাদ দেওয়া দায় একাউন্ট" else "Active liability accounts excluded from calculations",
            formulaExplanation = if (languageMode == LanguageMode.BANGLA)
                "এই দায় একাউন্টগুলো সক্রিয় হলেও একাউন্ট সেটিংস থেকে ক্যালকুলেশনে বাদ রাখা হয়েছে।"
            else
                "These liability accounts are active but toggled OFF from calculations in Accounts configuration.",
            items = excludedBreakdownItems,
            transactions = emptyList(),
            customBadgeColor = Color(0xFFF59E0B),
            emptyMessage = if (languageMode == LanguageMode.BANGLA) "গণনা থেকে বাদ দেওয়া কোনো দায় একাউন্ট নেই।" else "No liability accounts are excluded from calculations."
        )

        // Tab 3: Inactive (All inactive liability accounts)
        val inactiveBreakdownItems = inactiveLiabHolders.map { holder ->
            BreakdownItem(
                name = holder.displayName,
                amount = holder.balance,
                percentage = if (inactiveLiabilities > 0) (holder.balance / inactiveLiabilities) * 100.0 else 0.0,
                iconName = holder.iconName,
                color = Color.Gray,
                note = holder.note,
                onClick = { openAccountBreakdown(holder.accountWithBal, AccountFilterMode.INACTIVE) }
            )
        }.sortedByDescending { Math.abs(it.amount) }
        val inactiveTab = BreakdownTabInfo(
            title = if (languageMode == LanguageMode.BANGLA) "নিষ্ক্রিয়" else "Inactive",
            totalAmount = inactiveLiabilities,
            subtitle = if (languageMode == LanguageMode.BANGLA) "সকল নিষ্ক্রিয় দায় ও ঋণ" else "Archived liability accounts",
            formulaExplanation = if (languageMode == LanguageMode.BANGLA)
                "এই দায় একাউন্টগুলো নিষ্ক্রিয় (Inactive) হিসেবে চিহ্নিত রয়েছে এবং মূল হিসেবে ধরা হয় না।"
            else
                "These liability accounts are marked as inactive and are excluded from main calculations.",
            items = inactiveBreakdownItems,
            transactions = emptyList(),
            customBadgeColor = Color.Gray,
            emptyMessage = if (languageMode == LanguageMode.BANGLA) "কোনো নিষ্ক্রিয় দায় একাউন্ট নেই।" else "No inactive liability accounts found."
        )

        val allTabs = listOf(calculatedTab, excludedTab, inactiveTab)
        val activeTab = allTabs.getOrElse(initialTab) { calculatedTab }

        showAmountDetail(
            AmountDetailInfo(
                title = if (languageMode == LanguageMode.BANGLA) "দায় ও ঋণ হিসাব" else "Liabilities Breakdown",
                subtitle = if (languageMode == LanguageMode.BANGLA) "হিসাবকৃত, বাদ দেওয়া ও নিষ্ক্রিয় দায়" else "Calculated, Excluded & Inactive Liabilities",
                totalAmount = activeTab.totalAmount,
                formulaExplanation = activeTab.formulaExplanation,
                relatedBreakdownItems = activeTab.items,
                relatedTransactions = emptyList(),
                tabs = allTabs,
                defaultTabIndex = initialTab,
                customBadgeColor = SolidExpense
            )
        )
    }

    fun openNetWorthBreakdown(initialTab: Int = 0) {
        // Tab 1: Calculated Net Worth
        val calcBreakdown = listOf(
            BreakdownItem(
                name = if (languageMode == LanguageMode.BANGLA) "হিসাবকৃত সম্পদ (Assets)" else "Calculated Assets",
                amount = calculatedAssets,
                percentage = if (calculatedAssets + calculatedLiabilities > 0) (calculatedAssets / (calculatedAssets + calculatedLiabilities)) * 100.0 else 100.0,
                iconName = "account_balance_wallet",
                color = SolidIncome,
                note = if (languageMode == LanguageMode.BANGLA) "সক্রিয় ও অন্তর্ভুক্ত একাউন্ট" else "Active & included accounts",
                onClick = { openAssetsBreakdown(0) }
            ),
            BreakdownItem(
                name = if (languageMode == LanguageMode.BANGLA) "হিসাবকৃত দায় (Liabilities)" else "Calculated Liabilities",
                amount = -calculatedLiabilities,
                percentage = if (calculatedAssets + calculatedLiabilities > 0) (calculatedLiabilities / (calculatedAssets + calculatedLiabilities)) * 100.0 else 0.0,
                iconName = "credit_card",
                color = SolidExpense,
                note = if (languageMode == LanguageMode.BANGLA) "সক্রিয় ও অন্তর্ভুক্ত দায়" else "Active & included liabilities",
                onClick = { openLiabilitiesBreakdown(0) }
            )
        ).sortedByDescending { Math.abs(it.amount) }
        val calculatedTab = BreakdownTabInfo(
            title = if (languageMode == LanguageMode.BANGLA) "হিসাবকৃত" else "Calculated",
            totalAmount = calculatedNetWorth,
            subtitle = if (languageMode == LanguageMode.BANGLA) "সক্রিয় ও অন্তর্ভুক্ত একাউন্টের নিট সম্পদ" else "Total Assets minus Total Liabilities (Active & Included)",
            formulaExplanation = if (languageMode == LanguageMode.BANGLA)
                "হিসাবকৃত নিট সম্পদ = সমন্বয়কৃত সক্রিয় সম্পদ (${LanguageHelper.formatCurrency(calculatedAssets, languageMode)}) - সমন্বয়কৃত সক্রিয় দায় (${LanguageHelper.formatCurrency(calculatedLiabilities, languageMode)})। বাদ দেওয়া ও নিষ্ক্রিয় একাউন্টগুলো এখানে অন্তর্ভুক্ত নয়।"
            else
                "Calculated Net Worth = Modified Active Assets (${LanguageHelper.formatCurrency(calculatedAssets, languageMode)}) - Modified Active Liabilities (${LanguageHelper.formatCurrency(calculatedLiabilities, languageMode)}). Excluded and inactive accounts are removed.",
            items = calcBreakdown,
            transactions = emptyList(),
            customBadgeColor = SolidPrimary
        )

        // Tab 2: Excluded Net Worth
        val excludedBreakdown = listOf(
            BreakdownItem(
                name = if (languageMode == LanguageMode.BANGLA) "বাদ দেওয়া সম্পদ (Excluded Assets)" else "Excluded Assets",
                amount = excludedAssets,
                percentage = if (excludedAssets + excludedLiabilities > 0) (excludedAssets / (excludedAssets + excludedLiabilities)) * 100.0 else 100.0,
                iconName = "account_balance_wallet",
                color = Color(0xFFF59E0B),
                onClick = { openAssetsBreakdown(1) }
            ),
            BreakdownItem(
                name = if (languageMode == LanguageMode.BANGLA) "বাদ দেওয়া দায় (Excluded Liabilities)" else "Excluded Liabilities",
                amount = -excludedLiabilities,
                percentage = if (excludedAssets + excludedLiabilities > 0) (excludedLiabilities / (excludedAssets + excludedLiabilities)) * 100.0 else 0.0,
                iconName = "credit_card",
                color = Color(0xFFF59E0B),
                onClick = { openLiabilitiesBreakdown(1) }
            )
        ).sortedByDescending { Math.abs(it.amount) }
        val excludedTab = BreakdownTabInfo(
            title = if (languageMode == LanguageMode.BANGLA) "বাদ দেওয়া" else "Excluded",
            totalAmount = excludedNetWorth,
            subtitle = if (languageMode == LanguageMode.BANGLA) "গণনা থেকে বাদ দেওয়া একাউন্টের নিট ব্যালেন্স" else "Excluded accounts net position",
            formulaExplanation = if (languageMode == LanguageMode.BANGLA)
                "বাদ দেওয়া নিট সম্পদ = বাদ দেওয়া সম্পদ (${LanguageHelper.formatCurrency(excludedAssets, languageMode)}) - বাদ দেওয়া দায় (${LanguageHelper.formatCurrency(excludedLiabilities, languageMode)})।"
            else
                "Excluded Net Worth = Excluded Assets (${LanguageHelper.formatCurrency(excludedAssets, languageMode)}) - Excluded Liabilities (${LanguageHelper.formatCurrency(excludedLiabilities, languageMode)}).",
            items = excludedBreakdown,
            transactions = emptyList(),
            customBadgeColor = Color(0xFFF59E0B),
            emptyMessage = if (languageMode == LanguageMode.BANGLA) "গণনা থেকে বাদ দেওয়া কোনো একাউন্ট নেই।" else "No accounts are excluded from calculations."
        )

        // Tab 3: Inactive Net Worth
        val inactiveBreakdown = listOf(
            BreakdownItem(
                name = if (languageMode == LanguageMode.BANGLA) "নিষ্ক্রিয় সম্পদ (Inactive Assets)" else "Inactive Assets",
                amount = inactiveAssets,
                percentage = if (inactiveAssets + inactiveLiabilities > 0) (inactiveAssets / (inactiveAssets + inactiveLiabilities)) * 100.0 else 100.0,
                iconName = "account_balance_wallet",
                color = Color.Gray,
                onClick = { openAssetsBreakdown(2) }
            ),
            BreakdownItem(
                name = if (languageMode == LanguageMode.BANGLA) "নিষ্ক্রিয় দায় (Inactive Liabilities)" else "Inactive Liabilities",
                amount = -inactiveLiabilities,
                percentage = if (inactiveAssets + inactiveLiabilities > 0) (inactiveLiabilities / (inactiveAssets + inactiveLiabilities)) * 100.0 else 0.0,
                iconName = "credit_card",
                color = Color.Gray,
                onClick = { openLiabilitiesBreakdown(2) }
            )
        ).sortedByDescending { Math.abs(it.amount) }
        val inactiveTab = BreakdownTabInfo(
            title = if (languageMode == LanguageMode.BANGLA) "নিষ্ক্রিয়" else "Inactive",
            totalAmount = inactiveNetWorth,
            subtitle = if (languageMode == LanguageMode.BANGLA) "সকল নিষ্ক্রিয় একাউন্টের নিট ব্যালেন্স" else "Archived accounts net position",
            formulaExplanation = if (languageMode == LanguageMode.BANGLA)
                "নিষ্ক্রিয় নিট সম্পদ = নিষ্ক্রিয় সম্পদ (${LanguageHelper.formatCurrency(inactiveAssets, languageMode)}) - নিষ্ক্রিয় দায় (${LanguageHelper.formatCurrency(inactiveLiabilities, languageMode)})।"
            else
                "Inactive Net Worth = Inactive Assets (${LanguageHelper.formatCurrency(inactiveAssets, languageMode)}) - Inactive Liabilities (${LanguageHelper.formatCurrency(inactiveLiabilities, languageMode)}).",
            items = inactiveBreakdown,
            transactions = emptyList(),
            customBadgeColor = Color.Gray,
            emptyMessage = if (languageMode == LanguageMode.BANGLA) "কোনো নিষ্ক্রিয় একাউন্ট নেই।" else "No inactive accounts found."
        )

        val allTabs = listOf(calculatedTab, excludedTab, inactiveTab)
        val activeTab = allTabs.getOrElse(initialTab) { calculatedTab }

        showAmountDetail(
            AmountDetailInfo(
                title = if (languageMode == LanguageMode.BANGLA) "নিট সম্পদ হিসাব" else "Net Worth Calculation",
                subtitle = if (languageMode == LanguageMode.BANGLA) "মোট সম্পদ থেকে মোট দায় বিয়োগ" else "Total Assets minus Total Liabilities",
                totalAmount = activeTab.totalAmount,
                formulaExplanation = activeTab.formulaExplanation,
                relatedBreakdownItems = activeTab.items,
                relatedTransactions = emptyList(),
                tabs = allTabs,
                defaultTabIndex = initialTab,
                statusTag = if (overview.isLedgerBalanced) "Balanced" else "Ledger Check"
            )
        )
    }

    fun openIncomeBreakdown() {
        val cal = Calendar.getInstance()
        val currentMonth = cal.get(Calendar.MONTH)
        val currentYear = cal.get(Calendar.YEAR)

        val monthlyIncomeTxs = recentTransactions.filter {
            if (it.transaction.type != TransactionType.INCOME) return@filter false
            val txCal = Calendar.getInstance().apply { timeInMillis = it.transaction.dateEpochMs }
            txCal.get(Calendar.MONTH) == currentMonth && txCal.get(Calendar.YEAR) == currentYear
        }

        val grouped = monthlyIncomeTxs.groupBy { it.category?.id }
        val breakdown = grouped.map { (catId, txList) ->
            val catName = txList.firstOrNull()?.category?.localizedName(languageMode) ?: if (languageMode == LanguageMode.BANGLA) "অন্যান্য আয়" else "Uncategorized Income"
            val totalCatAmt = txList.sumOf { it.transaction.amount }
            BreakdownItem(
                name = catName,
                amount = totalCatAmt,
                percentage = if (overview.monthlyIncome > 0) (totalCatAmt / overview.monthlyIncome) * 100.0 else 0.0,
                iconName = txList.firstOrNull()?.category?.iconName,
                color = SolidIncome,
                count = txList.size,
                note = if (languageMode == LanguageMode.BANGLA) "ট্যাপ করে লেনদেন দেখুন" else "Tap to view transactions",
                onClick = {
                    showAmountDetail(
                        AmountDetailInfo(
                            title = catName,
                            subtitle = if (languageMode == LanguageMode.BANGLA) "আয় ক্যাটাগরি লেনদেন" else "Income Category Transactions",
                            totalAmount = totalCatAmt,
                            formulaExplanation = if (languageMode == LanguageMode.BANGLA)
                                "চলতি মাসে '$catName' ক্যাটাগরিতে মোট ${txList.size} টি আয়ের লেনদেন সম্পন্ন হয়েছে।"
                            else
                                "Total of ${txList.size} income transactions recorded for '$catName' this month.",
                            formulaSteps = emptyList(),
                            relatedTransactions = txList,
                            customBadgeColor = SolidIncome
                        )
                    )
                }
            )
        }.sortedByDescending { Math.abs(it.amount) }

        showAmountDetail(
            AmountDetailInfo(
                title = if (languageMode == LanguageMode.BANGLA) "চলতি মাসের মোট আয়" else "Monthly Inflow & Income",
                subtitle = if (languageMode == LanguageMode.BANGLA) "ক্যাটাগরি অনুযায়ী আয় (বড় থেকে ছোট)" else "Income categories sorted from largest to smallest",
                totalAmount = overview.monthlyIncome,
                formulaExplanation = if (languageMode == LanguageMode.BANGLA)
                    "চলতি ক্যালেন্ডার মাসে প্রাপ্ত বেতন, ব্যবসা, বিনিয়োগ ও অন্যান্য সমস্ত আয়ের ক্যাটাগরিভিত্তিক বিবরণ। প্রতিটি ক্যাটাগরির বিস্তারিত লেনদেন দেখতে ট্যাপ করুন।"
                else
                    "Breakdown of all credited earnings and income streams for this month. Tap any category to view its related transactions.",
                formulaSteps = emptyList(),
                relatedBreakdownItems = breakdown,
                relatedTransactions = emptyList(),
                customBadgeColor = SolidIncome
            )
        )
    }

    fun openExpenseBreakdown() {
        val cal = Calendar.getInstance()
        val currentMonth = cal.get(Calendar.MONTH)
        val currentYear = cal.get(Calendar.YEAR)

        val monthlyExpenseTxs = recentTransactions.filter {
            if (it.transaction.type != TransactionType.EXPENSE) return@filter false
            val txCal = Calendar.getInstance().apply { timeInMillis = it.transaction.dateEpochMs }
            txCal.get(Calendar.MONTH) == currentMonth && txCal.get(Calendar.YEAR) == currentYear
        }

        val grouped = monthlyExpenseTxs.groupBy { it.category?.id }
        val breakdown = grouped.map { (catId, txList) ->
            val catName = txList.firstOrNull()?.category?.localizedName(languageMode) ?: if (languageMode == LanguageMode.BANGLA) "সাধারণ খরচ" else "Uncategorized Expenses"
            val totalCatAmt = txList.sumOf { it.transaction.amount }
            BreakdownItem(
                name = catName,
                amount = totalCatAmt,
                percentage = if (overview.monthlyExpense > 0) (totalCatAmt / overview.monthlyExpense) * 100.0 else 0.0,
                iconName = txList.firstOrNull()?.category?.iconName,
                color = SolidExpense,
                count = txList.size,
                note = if (languageMode == LanguageMode.BANGLA) "ট্যাপ করে লেনদেন দেখুন" else "Tap to view transactions",
                onClick = {
                    showAmountDetail(
                        AmountDetailInfo(
                            title = catName,
                            subtitle = if (languageMode == LanguageMode.BANGLA) "খরচ ক্যাটাগরি লেনদেন" else "Expense Category Transactions",
                            totalAmount = totalCatAmt,
                            formulaExplanation = if (languageMode == LanguageMode.BANGLA)
                                "চলতি মাসে '$catName' খাতে মোট ${txList.size} টি খরচের লেনদেন সম্পন্ন হয়েছে।"
                            else
                                "Total of ${txList.size} expense transactions recorded for '$catName' this month.",
                            formulaSteps = emptyList(),
                            relatedTransactions = txList,
                            customBadgeColor = SolidExpense
                        )
                    )
                }
            )
        }.sortedByDescending { Math.abs(it.amount) }

        showAmountDetail(
            AmountDetailInfo(
                title = if (languageMode == LanguageMode.BANGLA) "চলতি মাসের মোট খরচ" else "Monthly Expenses & Outflows",
                subtitle = if (languageMode == LanguageMode.BANGLA) "ক্যাটাগরি অনুযায়ী খরচ (বড় থেকে ছোট)" else "Expense categories sorted from largest to smallest",
                totalAmount = overview.monthlyExpense,
                formulaExplanation = if (languageMode == LanguageMode.BANGLA)
                    "চলতি ক্যালেন্ডার মাসে বিভিন্ন খাতে খরচকৃত টাকার ক্যাটাগরিভিত্তিক বিবরণ। প্রতিটি ক্যাটাগরির বিস্তারিত লেনদেন দেখতে ট্যাপ করুন।"
                else
                    "Breakdown of all expenses and outflows incurred this month across active categories. Tap any category to view its related transactions.",
                formulaSteps = emptyList(),
                relatedBreakdownItems = breakdown,
                relatedTransactions = emptyList(),
                customBadgeColor = SolidExpense
            )
        )
    }

    fun openNetSavingsBreakdown() {
        val cal = Calendar.getInstance()
        val currentMonth = cal.get(Calendar.MONTH)
        val currentYear = cal.get(Calendar.YEAR)

        val monthlyIncomeTxs = recentTransactions.filter {
            if (it.transaction.type != TransactionType.INCOME) return@filter false
            val txCal = Calendar.getInstance().apply { timeInMillis = it.transaction.dateEpochMs }
            txCal.get(Calendar.MONTH) == currentMonth && txCal.get(Calendar.YEAR) == currentYear
        }
        val monthlyExpenseTxs = recentTransactions.filter {
            if (it.transaction.type != TransactionType.EXPENSE) return@filter false
            val txCal = Calendar.getInstance().apply { timeInMillis = it.transaction.dateEpochMs }
            txCal.get(Calendar.MONTH) == currentMonth && txCal.get(Calendar.YEAR) == currentYear
        }

        val breakdown = listOf(
            BreakdownItem(
                name = if (languageMode == LanguageMode.BANGLA) "মোট মাসিক আয় (Income)" else "Total Monthly Income",
                amount = overview.monthlyIncome,
                iconName = "trending_up",
                color = SolidIncome,
                count = monthlyIncomeTxs.size,
                note = if (languageMode == LanguageMode.BANGLA) "বড় থেকে ছোট আয়ের ক্যাটাগরি দেখতে ট্যাপ করুন" else "Tap to view income categories (largest to smallest)",
                onClick = { openIncomeBreakdown() }
            ),
            BreakdownItem(
                name = if (languageMode == LanguageMode.BANGLA) "মোট মাসিক খরচ (Expenses)" else "Total Monthly Expenses",
                amount = -overview.monthlyExpense,
                iconName = "trending_down",
                color = SolidExpense,
                count = monthlyExpenseTxs.size,
                note = if (languageMode == LanguageMode.BANGLA) "বড় থেকে ছোট খরচের ক্যাটাগরি দেখতে ট্যাপ করুন" else "Tap to view expense categories (largest to smallest)",
                onClick = { openExpenseBreakdown() }
            )
        ).sortedByDescending { Math.abs(it.amount) }

        showAmountDetail(
            AmountDetailInfo(
                title = if (languageMode == LanguageMode.BANGLA) "মাসিক নিট আয় / সঞ্চয় হিসাব" else "Net Earnings & Cash Flow",
                subtitle = if (languageMode == LanguageMode.BANGLA) "আয় থেকে খরচ বিয়োগ" else "Monthly Income minus Monthly Expenses",
                totalAmount = overview.monthlyNetSavings,
                formulaExplanation = if (languageMode == LanguageMode.BANGLA)
                    "মাসিক নিট আয় বা ক্যাশ ফ্লো হলো এই মাসে সমস্ত আয়ের যোগফল থেকে সমস্ত খরচের বিয়োগফল। ক্যাটাগরি ও লেনদেন দেখতে নিচে ট্যাপ করুন।"
                else
                    "Net Earnings represents surplus funds retained from this month's earnings after deducting all expenses. Tap components to view category details and related transactions.",
                formulaSteps = emptyList(),
                relatedBreakdownItems = breakdown,
                relatedTransactions = emptyList(),
                statusTag = if (overview.monthlyNetSavings >= 0) "Surplus" else "Deficit"
            )
        )
    }

    fun openAdditionalCostBreakdown() {
        val cal = Calendar.getInstance()
        val currentMonth = cal.get(Calendar.MONTH)
        val currentYear = cal.get(Calendar.YEAR)

        val monthlyExpenseTxs = recentTransactions.filter {
            if (it.transaction.type != TransactionType.EXPENSE) return@filter false
            val txCal = Calendar.getInstance().apply { timeInMillis = it.transaction.dateEpochMs }
            txCal.get(Calendar.MONTH) == currentMonth && txCal.get(Calendar.YEAR) == currentYear
        }

        val expenseCategories = allCategories.filter { it.type == CategoryType.EXPENSE }
        val budgetMap = monthlyBudgets.associateBy { "${it.itemType}_${it.itemId}" }

        val expenseTxsByCat = mutableMapOf<Long, Double>()
        val expenseTxsListByCat = mutableMapOf<Long, MutableList<TransactionWithDetails>>()
        var uncategorizedSpent = 0.0
        val uncategorizedTxs = mutableListOf<TransactionWithDetails>()

        for (txItem in monthlyExpenseTxs) {
            val catId = txItem.transaction.subCategoryId ?: txItem.transaction.categoryId
            if (catId != null) {
                expenseTxsByCat[catId] = (expenseTxsByCat[catId] ?: 0.0) + txItem.transaction.amount
                expenseTxsListByCat.getOrPut(catId) { mutableListOf() }.add(txItem)
            } else {
                uncategorizedSpent += txItem.transaction.amount
                uncategorizedTxs.add(txItem)
            }
        }

        data class OverCostItem(
            val categoryName: String,
            val iconName: String?,
            val budgetLimit: Double,
            val actualSpent: Double,
            val excessAmount: Double,
            val isOverBudget: Boolean,
            val txs: List<TransactionWithDetails>
        )

        val costItems = mutableListOf<OverCostItem>()

        for (cat in expenseCategories.filter { it.parentId != null }) {
            val budgetEntry = budgetMap["EXPENSE_${cat.id}"]
            val isEnabled = budgetEntry?.isEnabled ?: (cat.budgetLimit > 0)
            val budgetLimit = if (isEnabled) (budgetEntry?.budgetedAmount ?: cat.budgetLimit) else 0.0
            val spent = expenseTxsByCat[cat.id] ?: 0.0
            val catTxs = expenseTxsListByCat[cat.id] ?: emptyList()

            if (budgetLimit > 0) {
                if (spent > budgetLimit) {
                    val over = spent - budgetLimit
                    costItems.add(
                        OverCostItem(
                            categoryName = cat.localizedName(languageMode),
                            iconName = cat.iconName,
                            budgetLimit = budgetLimit,
                            actualSpent = spent,
                            excessAmount = over,
                            isOverBudget = true,
                            txs = catTxs
                        )
                    )
                }
            } else {
                if (spent > 0) {
                    costItems.add(
                        OverCostItem(
                            categoryName = cat.localizedName(languageMode),
                            iconName = cat.iconName,
                            budgetLimit = 0.0,
                            actualSpent = spent,
                            excessAmount = spent,
                            isOverBudget = false,
                            txs = catTxs
                        )
                    )
                }
            }
        }

        if (uncategorizedSpent > 0) {
            costItems.add(
                OverCostItem(
                    categoryName = if (languageMode == LanguageMode.BANGLA) "ক্যাটাগরিবিহীন খরচ" else "Uncategorized Expenses",
                    iconName = "category",
                    budgetLimit = 0.0,
                    actualSpent = uncategorizedSpent,
                    excessAmount = uncategorizedSpent,
                    isOverBudget = false,
                    txs = uncategorizedTxs
                )
            )
        }

        val additionalCostItems = costItems.filter { !it.isOverBudget }.sortedByDescending { Math.abs(it.excessAmount) }
        val overBudgetItems = costItems.filter { it.isOverBudget }.sortedByDescending { Math.abs(it.excessAmount) }

        val totalAdditional = additionalCostItems.sumOf { it.excessAmount }
        val totalOverBudget = overBudgetItems.sumOf { it.excessAmount }

        fun mapToBreakdownItems(items: List<OverCostItem>, totalForTab: Double): List<BreakdownItem> {
            return items.map { item ->
                val note = if (item.isOverBudget) {
                    if (languageMode == LanguageMode.BANGLA)
                        "বাজেট ছিল ${LanguageHelper.formatCurrency(item.budgetLimit, languageMode)}, খরচ হয়েছে ${LanguageHelper.formatCurrency(item.actualSpent, languageMode)}"
                    else
                        "Budget was ${LanguageHelper.formatCurrency(item.budgetLimit, languageMode)}, spent ${LanguageHelper.formatCurrency(item.actualSpent, languageMode)}"
                } else {
                    if (languageMode == LanguageMode.BANGLA)
                        "বাজেট ছিল না, খরচ হয়েছে ${LanguageHelper.formatCurrency(item.actualSpent, languageMode)}"
                    else
                        "No budget set, spent ${LanguageHelper.formatCurrency(item.actualSpent, languageMode)}"
                }

                BreakdownItem(
                    name = item.categoryName,
                    amount = item.excessAmount,
                    percentage = if (totalForTab > 0) (item.excessAmount / totalForTab) * 100.0 else 0.0,
                    iconName = item.iconName,
                    color = SolidExpense,
                    count = item.txs.size,
                    note = note,
                    onClick = {
                        showAmountDetail(
                            AmountDetailInfo(
                                title = item.categoryName,
                                subtitle = if (item.isOverBudget) {
                                    if (languageMode == LanguageMode.BANGLA) "বাজেট অতিরিক্ত খরচ" else "Over-Budget Expenditure"
                                } else {
                                    if (languageMode == LanguageMode.BANGLA) "বাজেটবিহীন অতিরিক্ত খরচ" else "Unbudgeted Additional Cost"
                                },
                                totalAmount = item.excessAmount,
                                formulaExplanation = if (item.isOverBudget) {
                                    if (languageMode == LanguageMode.BANGLA)
                                        "বাজেট সীমা ছিল ${LanguageHelper.formatCurrency(item.budgetLimit, languageMode)}, প্রকৃত খরচ ${LanguageHelper.formatCurrency(item.actualSpent, languageMode)}। বাজেট অতিক্রম: ${LanguageHelper.formatCurrency(item.excessAmount, languageMode)}।"
                                    else
                                        "Monthly budget was ${LanguageHelper.formatCurrency(item.budgetLimit, languageMode)}, total spent is ${LanguageHelper.formatCurrency(item.actualSpent, languageMode)}. Excess amount is ${LanguageHelper.formatCurrency(item.excessAmount, languageMode)}."
                                } else {
                                    if (languageMode == LanguageMode.BANGLA)
                                        "এই ক্যাটাগরিতে কোনো মাসিক বাজেট নির্ধারিত ছিল না। মোট অতিরিক্ত খরচ: ${LanguageHelper.formatCurrency(item.excessAmount, languageMode)}।"
                                    else
                                        "No monthly budget was assigned to this category. Total additional spent is ${LanguageHelper.formatCurrency(item.excessAmount, languageMode)}."
                                },
                                formulaSteps = emptyList(),
                                relatedTransactions = item.txs,
                                customBadgeColor = SolidExpense,
                                statusTag = if (item.isOverBudget) (if (languageMode == LanguageMode.BANGLA) "বাজেট অতিক্রান্ত" else "Over Budget") else (if (languageMode == LanguageMode.BANGLA) "অতিরিক্ত" else "Additional")
                            )
                        )
                    }
                )
            }
        }

        val additionalTab = BreakdownTabInfo(
            title = if (languageMode == LanguageMode.BANGLA) "অতিরিক্ত খরচ" else "Additional Costs",
            totalAmount = totalAdditional,
            items = mapToBreakdownItems(additionalCostItems, totalAdditional),
            formulaExplanation = if (languageMode == LanguageMode.BANGLA)
                "যেসব খাতে এই মাসে কোনো বাজেট বরাদ্দ ছিল না, সেই খাতগুলোর মোট খরচ। বিস্তারিত লেনদেন দেখতে যেকোনো আইটেমে ট্যাপ করুন।"
            else
                "Expenditures in categories that have no monthly budget allocated. Tap any category to inspect contributing transactions.",
            emptyMessage = if (languageMode == LanguageMode.BANGLA) "কোন বাজেটবিহীন অতিরিক্ত খরচ নেই" else "No unbudgeted additional costs recorded."
        )

        val overBudgetTab = BreakdownTabInfo(
            title = if (languageMode == LanguageMode.BANGLA) "বাজেট অতিক্রান্ত" else "Over Budget",
            totalAmount = totalOverBudget,
            items = mapToBreakdownItems(overBudgetItems, totalOverBudget),
            formulaExplanation = if (languageMode == LanguageMode.BANGLA)
                "নির্ধারিত মাসিক বাজেট সীমার বেশি খরচের হিসাব। প্রতিটি খাতে বাজেট সীমা কত ছিল এবং কত টাকা অতিরিক্ত খরচ হয়েছে তা দেখতে ট্যাপ করুন।"
            else
                "Excess expenditures exceeding the assigned monthly category limits. Tap any category to inspect overspending transactions.",
            emptyMessage = if (languageMode == LanguageMode.BANGLA) "কোন বাজেট অতিক্রান্ত খরচ নেই" else "No over-budget expenses recorded."
        )

        showAmountDetail(
            AmountDetailInfo(
                title = if (languageMode == LanguageMode.BANGLA) "অতিরিক্ত ও ওভার-বাজেট খরচ" else "Additional & Over-Budget Cost",
                subtitle = if (languageMode == LanguageMode.BANGLA) "বাজেট অতিরিক্ত এবং বাজেটবিহীন খরচের হিসাব" else "Unbudgeted expenditures and budget limit overruns",
                totalAmount = overview.additionalCost,
                formulaExplanation = if (languageMode == LanguageMode.BANGLA)
                    "নির্ধারিত বাজেটের অতিরিক্ত খরচ এবং বাজেটবিহীন খাতের খরচের সমন্বিত হিসাব। আলাদা বিবরণ দেখতে উপরের ট্যাবগুলো ব্যবহার করুন।"
                else
                    "Overview of excess spending beyond budget and expenditures with no budget set. Switch tabs above to see Additional Costs vs Over Budget.",
                formulaSteps = emptyList(),
                tabs = listOf(additionalTab, overBudgetTab),
                relatedBreakdownItems = emptyList(),
                relatedTransactions = emptyList(),
                customBadgeColor = if (overview.additionalCost > 0) SolidExpense else SolidIncome
            )
        )
    }

    fun openRemainingExpensesBreakdown() {
        val cal = Calendar.getInstance()
        val currentMonth = cal.get(Calendar.MONTH)
        val currentYear = cal.get(Calendar.YEAR)

        val monthlyExpenseTxs = recentTransactions.filter {
            if (it.transaction.type != TransactionType.EXPENSE) return@filter false
            val txCal = Calendar.getInstance().apply { timeInMillis = it.transaction.dateEpochMs }
            txCal.get(Calendar.MONTH) == currentMonth && txCal.get(Calendar.YEAR) == currentYear
        }

        val expenseCategories = allCategories.filter { it.type == CategoryType.EXPENSE }
        val budgetMap = monthlyBudgets.associateBy { "${it.itemType}_${it.itemId}" }

        val expenseTxsByCat = mutableMapOf<Long, Double>()
        val expenseTxsListByCat = mutableMapOf<Long, MutableList<TransactionWithDetails>>()

        for (txItem in monthlyExpenseTxs) {
            val catId = txItem.transaction.subCategoryId ?: txItem.transaction.categoryId
            if (catId != null) {
                expenseTxsByCat[catId] = (expenseTxsByCat[catId] ?: 0.0) + txItem.transaction.amount
                expenseTxsListByCat.getOrPut(catId) { mutableListOf() }.add(txItem)
            }
        }

        data class RemainingBudgetItem(
            val categoryName: String,
            val iconName: String?,
            val budgetLimit: Double,
            val actualSpent: Double,
            val remainingAmount: Double,
            val txs: List<TransactionWithDetails>
        )

        val remainingBudgetItems = mutableListOf<RemainingBudgetItem>()

        val parentExpenseCatIdsWithChildren = expenseCategories
            .filter { it.parentId != null }
            .mapNotNull { it.parentId }
            .toSet()

        val activeBudgetedCategories = expenseCategories.filter {
            it.parentId != null || !parentExpenseCatIdsWithChildren.contains(it.id) || budgetMap.containsKey("EXPENSE_${it.id}")
        }

        for (cat in activeBudgetedCategories) {
            val budgetEntry = budgetMap["EXPENSE_${cat.id}"]
            val isEnabled = budgetEntry?.isEnabled ?: (cat.budgetLimit > 0)
            val budgetLimit = if (isEnabled) (budgetEntry?.budgetedAmount ?: cat.budgetLimit) else 0.0
            val spent = expenseTxsByCat[cat.id] ?: 0.0
            val catTxs = expenseTxsListByCat[cat.id] ?: emptyList()

            if (budgetLimit > 0 && spent < budgetLimit) {
                val remaining = budgetLimit - spent
                remainingBudgetItems.add(
                    RemainingBudgetItem(
                        categoryName = cat.localizedName(languageMode),
                        iconName = cat.iconName,
                        budgetLimit = budgetLimit,
                        actualSpent = spent,
                        remainingAmount = remaining,
                        txs = catTxs
                    )
                )
            }
        }

        val sortedRemaining = remainingBudgetItems.sortedByDescending { Math.abs(it.remainingAmount) }

        val breakdownItems = sortedRemaining.map { item ->
            val note = if (languageMode == LanguageMode.BANGLA)
                "বাজেট: ${LanguageHelper.formatCurrency(item.budgetLimit, languageMode)} | খরচ: ${LanguageHelper.formatCurrency(item.actualSpent, languageMode)}"
            else
                "Budget: ${LanguageHelper.formatCurrency(item.budgetLimit, languageMode)} | Spent: ${LanguageHelper.formatCurrency(item.actualSpent, languageMode)}"

            BreakdownItem(
                name = item.categoryName,
                amount = item.remainingAmount,
                percentage = if (overview.remainingExpenses > 0) (item.remainingAmount / overview.remainingExpenses) * 100.0 else 0.0,
                iconName = item.iconName,
                color = SolidPrimary,
                count = item.txs.size,
                note = note,
                onClick = {
                    showAmountDetail(
                        AmountDetailInfo(
                            title = item.categoryName,
                            subtitle = if (languageMode == LanguageMode.BANGLA) "অবশিষ্ট বাজেট ও সম্পর্কিত লেনদেন" else "Remaining Budget & Transactions",
                            totalAmount = item.remainingAmount,
                            formulaExplanation = if (languageMode == LanguageMode.BANGLA)
                                "বাজেট সীমা ${LanguageHelper.formatCurrency(item.budgetLimit, languageMode)} − প্রকৃত খরচ ${LanguageHelper.formatCurrency(item.actualSpent, languageMode)} = অবশিষ্ট ${LanguageHelper.formatCurrency(item.remainingAmount, languageMode)}।"
                            else
                                "Budget ${LanguageHelper.formatCurrency(item.budgetLimit, languageMode)} − Spent ${LanguageHelper.formatCurrency(item.actualSpent, languageMode)} = Remaining ${LanguageHelper.formatCurrency(item.remainingAmount, languageMode)}.",
                            formulaSteps = emptyList(),
                            relatedTransactions = item.txs,
                            customBadgeColor = SolidPrimary
                        )
                    )
                }
            )
        }

        showAmountDetail(
            AmountDetailInfo(
                title = if (languageMode == LanguageMode.BANGLA) "বাজেটের অবশিষ্ট খরচ" else "Remaining Budget Expenses",
                subtitle = if (languageMode == LanguageMode.BANGLA) "ক্যাটাগরি অনুযায়ী অবশিষ্ট বাজেট" else "Category-by-category remaining budget",
                totalAmount = overview.remainingExpenses,
                formulaExplanation = if (languageMode == LanguageMode.BANGLA)
                    "প্রতিটি ক্যাটাগরির জন্য পৃথকভাবে হিসাবকৃত (বাজেট সীমা − প্রকৃত খরচ, সর্বনিম্ন ০) অবশিষ্ট টাকার যোগফল। বিস্তারিত লেনদেন দেখতে প্রতিটি ক্যাটাগরিতে ট্যাপ করুন।"
                else
                    "Sum of remaining unspent balances across all active category budgets for this month. Tap any category to view its related transactions.",
                formulaSteps = emptyList(),
                relatedBreakdownItems = breakdownItems,
                relatedTransactions = emptyList()
            )
        )
    }

    fun openExpendableBreakdown() {
        val currentExpendable = calculatedAssets - calculatedLiabilities - overview.remainingExpenses
        val components = listOf(
            BreakdownItem(
                name = if (languageMode == LanguageMode.BANGLA) "হিসাবকৃত সম্পদ (Assets)" else "Calculated Assets",
                amount = calculatedAssets,
                iconName = "account_balance_wallet",
                color = SolidIncome,
                note = if (languageMode == LanguageMode.BANGLA) "সক্রিয় ও অন্তর্ভুক্ত সম্পদ" else "Active & included assets",
                onClick = { openAssetsBreakdown(0) }
            ),
            BreakdownItem(
                name = if (languageMode == LanguageMode.BANGLA) "হিসাবকৃত দায় (Liabilities)" else "Calculated Liabilities",
                amount = -calculatedLiabilities,
                iconName = "credit_card",
                color = SolidExpense,
                note = if (languageMode == LanguageMode.BANGLA) "সক্রিয় ও অন্তর্ভুক্ত দায়" else "Active & included liabilities",
                onClick = { openLiabilitiesBreakdown(0) }
            ),
            BreakdownItem(
                name = if (languageMode == LanguageMode.BANGLA) "বাজেটের অবশিষ্ট খরচ" else "Remaining Expenses",
                amount = -overview.remainingExpenses,
                iconName = "shopping_bag",
                color = SolidExpense,
                note = if (languageMode == LanguageMode.BANGLA) "বাজেটের বাকি খরচ" else "Remaining unspent budget",
                onClick = { openRemainingExpensesBreakdown() }
            )
        ).sortedByDescending { Math.abs(it.amount) }

        showAmountDetail(
            AmountDetailInfo(
                title = if (languageMode == LanguageMode.BANGLA) "খরচযোগ্য অবশিষ্ট অর্থের হিসাব" else "Expendable Funds Breakdown",
                subtitle = if (languageMode == LanguageMode.BANGLA) "সক্রিয় ও অন্তর্ভুক্ত সম্পদ − দায় − অবশিষ্ট বাজেট খরচ" else "Active & Included Assets − Liabilities − Remaining Expenses",
                totalAmount = currentExpendable,
                formulaExplanation = if (languageMode == LanguageMode.BANGLA)
                    "খরচযোগ্য অর্থ = সক্রিয় ও অন্তর্ভুক্ত সম্পদ (${LanguageHelper.formatCurrency(calculatedAssets, languageMode)}) − সক্রিয় ও অন্তর্ভুক্ত দায় (${LanguageHelper.formatCurrency(calculatedLiabilities, languageMode)}) − বাজেটের অবশিষ্ট খরচ (${LanguageHelper.formatCurrency(overview.remainingExpenses, languageMode)})।"
                else
                    "Expendable = Active & Included Assets (${LanguageHelper.formatCurrency(calculatedAssets, languageMode)}) − Active & Included Liabilities (${LanguageHelper.formatCurrency(calculatedLiabilities, languageMode)}) − Remaining Expenses (${LanguageHelper.formatCurrency(overview.remainingExpenses, languageMode)}).",
                formulaSteps = emptyList(),
                relatedBreakdownItems = components,
                relatedTransactions = emptyList(),
                statusTag = if (currentExpendable >= 0) "Safe" else "Deficit"
            )
        )
    }

    fun openExpectedExpendableBreakdown() {
        val currentExpendable = calculatedAssets - calculatedLiabilities - overview.remainingExpenses
        val currentExpected = currentExpendable + overview.potentialIncome
        val components = listOf(
            BreakdownItem(
                name = if (languageMode == LanguageMode.BANGLA) "বর্তমান খরচযোগ্য অর্থ (Expendable)" else "Current Expendable",
                amount = currentExpendable,
                iconName = "savings",
                color = if (currentExpendable >= 0) SolidIncome else SolidExpense,
                onClick = { openExpendableBreakdown() }
            ),
            BreakdownItem(
                name = if (languageMode == LanguageMode.BANGLA) "বাজেট থেকে সম্ভাব্য আয়" else "Expected Income from Budget",
                amount = overview.potentialIncome,
                iconName = "trending_up",
                color = SolidIncome,
                note = if (languageMode == LanguageMode.BANGLA) "চলতি মাসের অবশিষ্ট সম্ভাব্য আয়" else "Expected incoming funds this month"
            )
        ).sortedByDescending { Math.abs(it.amount) }

        showAmountDetail(
            AmountDetailInfo(
                title = if (languageMode == LanguageMode.BANGLA) "সম্ভাব্য খরচযোগ্য অর্থ" else "Expected Expendable Calculation",
                subtitle = if (languageMode == LanguageMode.BANGLA) "খরচযোগ্য অর্থ + বাজেট থেকে সম্ভাব্য আয়" else "Expendable + Expected Income from Budget",
                totalAmount = currentExpected,
                formulaExplanation = if (languageMode == LanguageMode.BANGLA)
                    "প্রত্যাশিত খরচযোগ্য অর্থ = খরচযোগ্য অর্থ (${LanguageHelper.formatCurrency(currentExpendable, languageMode)}) + বাজেট থেকে সম্ভাব্য আয় (${LanguageHelper.formatCurrency(overview.potentialIncome, languageMode)})।"
                else
                    "Expected Expendable = Expendable (${LanguageHelper.formatCurrency(currentExpendable, languageMode)}) + Expected Income from Budget (${LanguageHelper.formatCurrency(overview.potentialIncome, languageMode)}).",
                formulaSteps = emptyList(),
                relatedBreakdownItems = components,
                relatedTransactions = emptyList()
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_screen")
    ) {
        // Dashboard Top Header (Drawer Menu, App Branding, Demo Indicator)
        AutoHidingHeaderContainer {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onOpenDrawer,
                        modifier = Modifier.testTag("dashboard_menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Open Navigation Menu",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFFFD700),
                        shadowElevation = 1.dp,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "৳",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF4A3800)
                            )
                        }
                    }

                    Text(
                        text = LanguageHelper.getString("app_name", languageMode),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (isDemoMode) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        modifier = Modifier
                            .clickable { onExitDemoMode() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(MaterialTheme.colorScheme.tertiary, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "DEMO",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Restore Banner if previous backups detected
            if (showRestoreBanner && detectedBackups.isNotEmpty()) {
                item(key = "card_backup_restore_banner") {
                    DashboardBackupRestoreBanner(
                        visible = showRestoreBanner,
                        detectedBackups = detectedBackups,
                        languageMode = languageMode,
                        onOpenRestoreDialog = { showRestoreListDialog = true },
                        onDismiss = { onDismissRestoreBanner(detectedBackups.firstOrNull()?.fileId) }
                    )
                }
            }

            // Render Cards dynamically based on user configuration order and visibility
            dashboardConfig.cardOrder.forEach { cardType ->
                if (cardType in dashboardConfig.visibleCards) {
                    when (cardType) {
                        DashboardCardType.NET_WORTH -> {
                            item(key = "card_net_worth") {
                                RedesignedNetWorthCard(
                                    overview = overview,
                                    calculatedNetWorth = calculatedNetWorth,
                                    calculatedAssets = calculatedAssets,
                                    calculatedLiabilities = calculatedLiabilities,
                                    languageMode = languageMode,
                                    onNetWorthClick = { openNetWorthBreakdown(0) },
                                    onAssetsClick = { openAssetsBreakdown(0) },
                                    onLiabilitiesClick = { openLiabilitiesBreakdown(0) }
                                )
                            }
                        }

                        DashboardCardType.DAILY_SUMMARY -> {
                            item(key = "card_daily_summary") {
                                DailySummaryCard(
                                    transactions = recentTransactions,
                                    mode = dashboardConfig.dailySummaryMode,
                                    period = dashboardConfig.dailySummaryPeriod,
                                    chartType = dashboardConfig.dailyChartType,
                                    showValues = dashboardConfig.dailyShowValues,
                                    showAverages = dashboardConfig.dailyShowAverages,
                                    decimalPrecision = dashboardConfig.dailyDecimalPrecision,
                                    showCurrency = dashboardConfig.dailyShowCurrency,
                                    showCurrencySymbol = dashboardConfig.dailyShowCurrencySymbol,
                                    languageMode = languageMode,
                                    onModeChange = { newMode ->
                                        onUpdateDailySummarySettings(
                                            newMode,
                                            dashboardConfig.dailySummaryPeriod,
                                            dashboardConfig.dailyChartType,
                                            dashboardConfig.dailyShowValues,
                                            dashboardConfig.dailyShowAverages,
                                            dashboardConfig.dailyDecimalPrecision,
                                            dashboardConfig.dailyShowCurrency,
                                            dashboardConfig.dailyShowCurrencySymbol
                                        )
                                    },
                                    onPeriodChange = { newPeriod ->
                                        onUpdateDailySummarySettings(
                                            dashboardConfig.dailySummaryMode,
                                            newPeriod,
                                            dashboardConfig.dailyChartType,
                                            dashboardConfig.dailyShowValues,
                                            dashboardConfig.dailyShowAverages,
                                            dashboardConfig.dailyDecimalPrecision,
                                            dashboardConfig.dailyShowCurrency,
                                            dashboardConfig.dailyShowCurrencySymbol
                                        )
                                    },
                                    onOpenSettings = { showDailySettingsDialog = true },
                                    onCardClick = {
                                        dailySummarySelectedDayEpoch = null
                                        showDailySummaryDetail = true
                                    },
                                    onDayClick = { daySummary ->
                                        dailySummarySelectedDayEpoch = daySummary.dateEpochMs
                                        showDailySummaryDetail = true
                                    }
                                )
                            }
                        }

                        DashboardCardType.BUDGET_SUMMARY -> {
                            item(key = "card_budget_summary") {
                                BudgetSummaryCard(
                                    transactions = recentTransactions,
                                    allCategories = allCategories,
                                    monthlyBudgets = monthlyBudgets,
                                    chartShape = dashboardConfig.budgetChartShape,
                                    categoryType = dashboardConfig.budgetCategoryType,
                                    maxCategories = dashboardConfig.budgetMaxCategories,
                                    showPercentages = dashboardConfig.budgetShowPercentages,
                                    showTodayPace = dashboardConfig.budgetShowTodayPace,
                                    languageMode = languageMode,
                                    onCategoryTypeChange = { newType ->
                                        onUpdateBudgetSummarySettings(
                                            dashboardConfig.budgetChartShape,
                                            newType,
                                            dashboardConfig.budgetMaxCategories,
                                            dashboardConfig.budgetShowPercentages,
                                            dashboardConfig.budgetShowTodayPace
                                        )
                                    },
                                    onOpenSettings = { showBudgetSettingsDialog = true },
                                    onCardClick = { showBudgetSummaryPreview = true }
                                )
                            }
                        }

                        DashboardCardType.FAVORITE_ACCOUNTS -> {
                            item(key = "card_favorite_accounts") {
                                FavoriteAccountsCard(
                                    accountsWithBalances = accountsWithBalances,
                                    favoriteAccountIds = dashboardConfig.favoriteAccountIds,
                                    languageMode = languageMode,
                                    onOpenAccountPicker = { showFavoriteAccountsPicker = true },
                                    onAccountClick = { acc ->
                                        onAccountClick(acc)
                                    }
                                )
                            }
                        }

                        DashboardCardType.CALENDAR_VIEW -> {
                            item(key = "card_calendar_view") {
                                CalendarSummaryCard(
                                    transactions = recentTransactions,
                                    displayMode = dashboardConfig.calendarDisplayMode,
                                    showIncome = dashboardConfig.calendarShowIncome,
                                    showExpense = dashboardConfig.calendarShowExpense,
                                    languageMode = languageMode,
                                    onOpenSettings = { showCalendarSettingsDialog = true },
                                    onTransactionClick = onTransactionClick
                                )
                            }
                        }

                        DashboardCardType.CASH_FLOW -> {
                            item(key = "card_cash_flow") {
                                RedesignedCashFlowRow(
                                    overview = overview,
                                    languageMode = languageMode,
                                    onIncomeClick = { openIncomeBreakdown() },
                                    onExpenseClick = { openExpenseBreakdown() }
                                )
                            }
                        }

                        DashboardCardType.FINANCIAL_OVERVIEW -> {
                            item(key = "card_financial_overview") {
                                RedesignedFinancialOverviewCard(
                                    overview = overview,
                                    languageMode = languageMode,
                                    calculatedAssets = calculatedAssets,
                                    calculatedLiabilities = calculatedLiabilities,
                                    calculatedNetWorth = calculatedNetWorth,
                                    inactiveAssets = inactiveAssets,
                                    inactiveLiabilities = inactiveLiabilities,
                                    inactiveNetWorth = inactiveNetWorth,
                                    excludedAssets = excludedAssets,
                                    excludedLiabilities = excludedLiabilities,
                                    excludedNetWorth = excludedNetWorth,
                                    onExpendableClick = { openExpendableBreakdown() },
                                    onExpectedExpendableClick = { openExpectedExpendableBreakdown() },
                                    onAssetsClick = { tab -> openAssetsBreakdown(tab) },
                                    onLiabilitiesClick = { tab -> openLiabilitiesBreakdown(tab) },
                                    onRemainingExpensesClick = { openRemainingExpensesBreakdown() },
                                    onAdditionalCostClick = { openAdditionalCostBreakdown() },
                                    onNetWorthClick = { tab -> openNetWorthBreakdown(tab) },
                                    onNetEarningsClick = { openNetSavingsBreakdown() }
                                )
                            }
                        }

                        DashboardCardType.QUICK_ACTIONS -> {
                            item(key = "card_quick_actions") {
                                QuickActionsRow(
                                    languageMode = languageMode,
                                    onAddTransactionClick = onAddTransactionClick,
                                    onOpenCalculator = { showStandAloneCalculator = true }
                                )
                            }
                        }

                        DashboardCardType.RECENT_TRANSACTIONS -> {
                            item(key = "card_recent_transactions") {
                                RecentTransactionsCard(
                                    recentTransactions = recentTransactions,
                                    languageMode = languageMode,
                                    onTransactionClick = onTransactionClick,
                                    onViewAllClick = onViewAllTransactionsClick,
                                    onAmountClick = { txItem ->
                                        showAmountDetail(
                                            AmountDetailInfo(
                                                title = txItem.category?.localizedName(languageMode) ?: "Transaction",
                                                subtitle = DateUtils.formatDate(txItem.transaction.dateEpochMs, languageMode),
                                                totalAmount = txItem.transaction.amount,
                                                formulaExplanation = "Note: ${txItem.transaction.note.ifBlank { "N/A" }}\nPayee/Payer: ${txItem.transaction.payeeOrPayer.ifBlank { "N/A" }}",
                                                relatedTransactions = listOf(txItem)
                                            )
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Customize Cards Button
            item(key = "bottom_customize_cards_button") {
                OutlinedButton(
                    onClick = { showCustomizeCardsDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                ) {
                    Icon(
                        imageVector = Icons.Default.DashboardCustomize,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = LanguageHelper.getString("customize_cards", languageMode),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }

    // Modal Dialogs
    val activeAmountDetail = amountDetailStack.lastOrNull()
    if (activeAmountDetail != null) {
        AmountBreakdownDialog(
            info = activeAmountDetail,
            languageMode = languageMode,
            onDismiss = {
                if (amountDetailStack.isNotEmpty()) {
                    amountDetailStack.removeLastOrNull()
                }
            },
            onTransactionClick = { tx ->
                onTransactionClick(tx)
            },
            onAccountClick = { acc ->
                val found = accountsWithBalances.find { it.account.id == acc.id }
                    ?: activeAccounts.find { it.account.id == acc.id }
                    ?: inactiveAccounts.find { it.account.id == acc.id }
                if (found != null) {
                    val mode = if (!found.account.isActive) {
                        AccountFilterMode.INACTIVE
                    } else if (!accountCalcConfig.isIncluded(found.account.id)) {
                        AccountFilterMode.EXCLUDED
                    } else {
                        AccountFilterMode.CALCULATED
                    }
                    openAccountBreakdown(found, mode)
                } else {
                    onAccountClick(acc)
                }
            },
            canGoBack = amountDetailStack.size > 1,
            onBack = {
                if (amountDetailStack.isNotEmpty()) {
                    amountDetailStack.removeLastOrNull()
                }
            },
            onCloseAll = {
                amountDetailStack.clear()
            },
            onTabChanged = { tabIdx ->
                if (amountDetailStack.isNotEmpty()) {
                    val last = amountDetailStack.last()
                    amountDetailStack[amountDetailStack.lastIndex] = last.copy(defaultTabIndex = tabIdx)
                }
            }
        )
    }

    if (showCustomizeCardsDialog) {
        CustomizeDashboardCardsDialog(
            config = dashboardConfig,
            languageMode = languageMode,
            onDismiss = { showCustomizeCardsDialog = false },
            onToggleCard = onToggleCardVisibility,
            onMoveCard = onReorderCards,
            onResetDefaults = onResetDashboardDefaults
        )
    }

    if (showDailySettingsDialog) {
        DailySummarySettingsDialog(
            currentMode = dashboardConfig.dailySummaryMode,
            currentPeriod = dashboardConfig.dailySummaryPeriod,
            currentChartType = dashboardConfig.dailyChartType,
            currentShowValues = dashboardConfig.dailyShowValues,
            currentShowAverages = dashboardConfig.dailyShowAverages,
            currentDecimalPrecision = dashboardConfig.dailyDecimalPrecision,
            currentShowCurrency = dashboardConfig.dailyShowCurrency,
            currentShowCurrencySymbol = dashboardConfig.dailyShowCurrencySymbol,
            languageMode = languageMode,
            onDismiss = { showDailySettingsDialog = false },
            onSave = { m, p, ct, sv, sa, dp, sc, scs ->
                onUpdateDailySummarySettings(m, p, ct, sv, sa, dp, sc, scs)
                showDailySettingsDialog = false
            }
        )
    }

    if (showBudgetSettingsDialog) {
        BudgetSummarySettingsDialog(
            currentShape = dashboardConfig.budgetChartShape,
            currentCategoryType = dashboardConfig.budgetCategoryType,
            currentMaxCategories = dashboardConfig.budgetMaxCategories,
            currentShowPercentages = dashboardConfig.budgetShowPercentages,
            currentShowTodayPace = dashboardConfig.budgetShowTodayPace,
            languageMode = languageMode,
            onDismiss = { showBudgetSettingsDialog = false },
            onSave = { s, t, mc, sp, tp ->
                onUpdateBudgetSummarySettings(s, t, mc, sp, tp)
                showBudgetSettingsDialog = false
            }
        )
    }

    if (showCalendarSettingsDialog) {
        CalendarSettingsDialog(
            currentDisplayMode = dashboardConfig.calendarDisplayMode,
            currentShowIncome = dashboardConfig.calendarShowIncome,
            currentShowExpense = dashboardConfig.calendarShowExpense,
            languageMode = languageMode,
            onDismiss = { showCalendarSettingsDialog = false },
            onSave = { dm, si, se ->
                onUpdateCalendarSettings(dm, si, se)
                showCalendarSettingsDialog = false
            }
        )
    }

    if (showFavoriteAccountsPicker) {
        FavoriteAccountsSelectionDialog(
            allAccounts = accountsWithBalances,
            initialSelectedIds = dashboardConfig.favoriteAccountIds,
            languageMode = languageMode,
            onDismiss = { showFavoriteAccountsPicker = false },
            onSave = { selectedIds ->
                onUpdateFavoriteAccounts(selectedIds)
                showFavoriteAccountsPicker = false
            }
        )
    }

    if (showRestoreListDialog) {
        DetectedBackupsListDialog(
            detectedBackups = detectedBackups,
            isFirstLaunchPrompt = false,
            languageMode = languageMode,
            onScanAgain = onScanBackups,
            onSelectBackupToRestore = { backup, isMerge ->
                onRestoreDetectedBackup(backup, isMerge)
                showRestoreListDialog = false
            },
            onDismiss = { showRestoreListDialog = false }
        )
    }

    if (showStandAloneCalculator) {
        PopupCalculatorDialog(
            languageMode = languageMode,
            onDismiss = { showStandAloneCalculator = false },
            onValueConfirmed = { /* Standalone calculator */ }
        )
    }

    if (showBudgetSummaryPreview) {
        BudgetSummaryPreviewDialog(
            transactions = recentTransactions,
            allCategories = allCategories,
            monthlyBudgets = monthlyBudgets,
            accounts = accountsWithBalances.map { it.account },
            languageMode = languageMode,
            onDismiss = { showBudgetSummaryPreview = false },
            onTransactionClick = { txItem ->
                showBudgetSummaryPreview = false
                onTransactionClick(txItem.transaction)
            }
        )
    }

    if (showDailySummaryDetail) {
        DailySummaryDetailDialog(
            transactions = recentTransactions,
            allCategories = allCategories,
            accounts = accountsWithBalances.map { it.account },
            languageMode = languageMode,
            initialSelectedDateEpoch = dailySummarySelectedDayEpoch,
            initialChartType = dashboardConfig.dailyChartType,
            onChartTypeChange = { newType ->
                onUpdateDailySummarySettings(
                    dashboardConfig.dailySummaryMode,
                    dashboardConfig.dailySummaryPeriod,
                    newType,
                    dashboardConfig.dailyShowValues,
                    dashboardConfig.dailyShowAverages,
                    dashboardConfig.dailyDecimalPrecision,
                    dashboardConfig.dailyShowCurrency,
                    dashboardConfig.dailyShowCurrencySymbol
                )
            },
            onDismiss = {
                showDailySummaryDetail = false
                dailySummarySelectedDayEpoch = null
            },
            onTransactionClick = { txItem ->
                showDailySummaryDetail = false
                dailySummarySelectedDayEpoch = null
                onTransactionClick(txItem.transaction)
            },
            onAccountClick = { acc ->
                showDailySummaryDetail = false
                dailySummarySelectedDayEpoch = null
                onAccountClick(acc)
            }
        )
    }
}

/**
 * Redesigned Net Worth Hero Card with dynamic gradients, status chips, and clickable amount breakdowns.
 */
@Composable
private fun RedesignedNetWorthCard(
    overview: FinancialOverview,
    calculatedNetWorth: Double,
    calculatedAssets: Double,
    calculatedLiabilities: Double,
    languageMode: LanguageMode,
    onNetWorthClick: () -> Unit,
    onAssetsClick: () -> Unit,
    onLiabilitiesClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("net_worth_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            SolidPrimary.copy(alpha = 0.95f),
                            SolidPrimary
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Savings,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = LanguageHelper.getString("net_worth", languageMode),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (overview.isLedgerBalanced) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (overview.isLedgerBalanced) Color(0xFF6EE7B7) else Color(0xFFFCA5A5),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (overview.isLedgerBalanced) "Dr = Cr Balanced" else LanguageHelper.getString("unbalanced", languageMode),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Hero Clickable Net Worth Amount
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onNetWorthClick),
                    color = Color.White.copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = LanguageHelper.formatCurrency(calculatedNetWorth, languageMode),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = "View Calculation",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(12.dp))

                // Sub Assets & Liabilities clickable tags
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Assets Sub-pill
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(onClick = onAssetsClick),
                        color = Color.White.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF6EE7B7))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = LanguageHelper.getString("assets", languageMode),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                            Text(
                                text = LanguageHelper.formatCurrency(calculatedAssets, languageMode),
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Liabilities Sub-pill
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(onClick = onLiabilitiesClick),
                        color = Color.White.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFCA5A5))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = LanguageHelper.getString("liabilities", languageMode),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                            Text(
                                text = LanguageHelper.formatCurrency(calculatedLiabilities, languageMode),
                                fontSize = 12.5.sp,
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

/**
 * Redesigned Cash Flow Row with clickable Income and Expense cards.
 */
@Composable
private fun RedesignedCashFlowRow(
    overview: FinancialOverview,
    languageMode: LanguageMode,
    onIncomeClick: () -> Unit,
    onExpenseClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Monthly Income Card
        Surface(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .clickable(onClick = onIncomeClick),
            color = SolidIncomeContainer.copy(alpha = 0.85f),
            border = BorderStroke(1.dp, SolidIncome.copy(alpha = 0.35f))
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(SolidIncome.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                        contentDescription = null,
                        tint = SolidIncome,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = LanguageHelper.getString("income", languageMode),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SolidOnIncomeContainer.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = LanguageHelper.formatCurrency(overview.monthlyIncome, languageMode),
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SolidOnIncomeContainer
                    )
                }
            }
        }

        // Monthly Expense Card
        Surface(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .clickable(onClick = onExpenseClick),
            color = SolidExpenseContainer.copy(alpha = 0.85f),
            border = BorderStroke(1.dp, SolidExpense.copy(alpha = 0.35f))
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(SolidExpense.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                        contentDescription = null,
                        tint = SolidExpense,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = LanguageHelper.getString("expense", languageMode),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SolidOnExpenseContainer.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = LanguageHelper.formatCurrency(overview.monthlyExpense, languageMode),
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SolidOnExpenseContainer
                    )
                }
            }
        }
    }
}

/**
 * Redesigned Financial Overview Card and sub-cards with clickable amounts,
 * clear visual hierarchy, and instant formula access.
 */
@Composable
private fun RedesignedFinancialOverviewCard(
    overview: FinancialOverview,
    languageMode: LanguageMode,
    calculatedAssets: Double,
    calculatedLiabilities: Double,
    calculatedNetWorth: Double,
    inactiveAssets: Double,
    inactiveLiabilities: Double,
    inactiveNetWorth: Double,
    excludedAssets: Double,
    excludedLiabilities: Double,
    excludedNetWorth: Double,
    onExpendableClick: () -> Unit,
    onExpectedExpendableClick: () -> Unit,
    onAssetsClick: (initialTab: Int) -> Unit,
    onLiabilitiesClick: (initialTab: Int) -> Unit,
    onRemainingExpensesClick: () -> Unit,
    onAdditionalCostClick: () -> Unit,
    onNetWorthClick: (initialTab: Int) -> Unit,
    onNetEarningsClick: () -> Unit
) {
    var showFormulaBreakdown by remember { mutableStateOf(false) }

    val expendable = calculatedAssets - calculatedLiabilities - overview.remainingExpenses
    val expectedExpendable = expendable + overview.potentialIncome
    val areExpendablesEqual = Math.abs(expendable - expectedExpendable) < 0.001

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("expendable_overview_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Primary Expendable & Expected Expendable Sub-Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Expendable Hero Sub-card (occupies full row space when expected expendable is hidden)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (expendable >= 0) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f) else Color(0xFFFFEBEE),
                    border = BorderStroke(1.dp, if (expendable >= 0) SolidPrimary.copy(alpha = 0.3f) else Color(0xFFEF9A9A)),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onExpendableClick)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = LanguageHelper.getString("expendable", languageMode),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (expendable >= 0) MaterialTheme.colorScheme.onPrimaryContainer else Color(0xFFC62828),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (expendable >= 0) Color(0xFF10B981) else Color(0xFFEF4444)
                            ) {
                                Text(
                                    text = if (expendable >= 0) "Safe" else "Deficit",
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = LanguageHelper.formatCurrency(expendable, languageMode),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (expendable >= 0) MaterialTheme.colorScheme.onPrimaryContainer else Color(0xFFC62828)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = if (expendable >= 0) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f) else Color(0xFFC62828).copy(alpha = 0.6f),
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }

                // Expected Expendable Sub-card (Hidden when equal to expendable, shown when different)
                if (!areExpendablesEqual) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f)),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(onClick = onExpectedExpendableClick)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = LanguageHelper.getString("expected_expendable", languageMode),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = "+Income",
                                            fontSize = 8.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            maxLines = 1,
                                            softWrap = false,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                    Icon(
                                        imageVector = if (showFormulaBreakdown) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = "Toggle Formula",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clickable { showFormulaBreakdown = !showFormulaBreakdown }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = LanguageHelper.formatCurrency(expectedExpendable, languageMode),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f),
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
            Spacer(modifier = Modifier.height(8.dp))

            // 6 Financial Indicators Sub-Cards in 2x3 Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Sub-Card 1: Current Assets
                FinancialIndicatorItem(
                    label = LanguageHelper.getString("current_assets", languageMode),
                    amount = calculatedAssets,
                    icon = Icons.Default.AccountBalanceWallet,
                    iconTint = SolidIncome,
                    languageMode = languageMode,
                    onClick = { onAssetsClick(0) },
                    modifier = Modifier.weight(1f)
                )

                // Sub-Card 2: Liabilities
                val displayLiabilities = if (calculatedLiabilities > 0) -calculatedLiabilities else calculatedLiabilities
                FinancialIndicatorItem(
                    label = LanguageHelper.getString("liabilities", languageMode),
                    amount = displayLiabilities,
                    icon = Icons.Default.CreditCard,
                    iconTint = SolidExpense,
                    amountColor = if (displayLiabilities < 0) SolidExpense else MaterialTheme.colorScheme.onSurface,
                    languageMode = languageMode,
                    trendIndicator = if (overview.liabilitiesChange != 0.0) if (overview.liabilitiesChange > 0) "▲" else "▼" else null,
                    trendColor = if (overview.liabilitiesChange > 0) SolidExpense else SolidIncome,
                    onClick = { onLiabilitiesClick(0) },
                    modifier = Modifier.weight(1f)
                )

                // Sub-Card 3: Remaining Expenses
                FinancialIndicatorItem(
                    label = LanguageHelper.getString("remaining_expenses", languageMode),
                    amount = overview.remainingExpenses,
                    icon = Icons.Default.PieChart,
                    iconTint = Color(0xFF0284C7),
                    amountColor = Color(0xFF0284C7),
                    languageMode = languageMode,
                    onClick = onRemainingExpensesClick,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Sub-Card 4: Additional Cost
                FinancialIndicatorItem(
                    label = LanguageHelper.getString("additional_cost", languageMode),
                    amount = overview.additionalCost,
                    icon = Icons.Default.Warning,
                    iconTint = if (overview.additionalCost > 0) SolidExpense else MaterialTheme.colorScheme.outline,
                    amountColor = if (overview.additionalCost > 0) SolidExpense else MaterialTheme.colorScheme.onSurface,
                    languageMode = languageMode,
                    onClick = onAdditionalCostClick,
                    modifier = Modifier.weight(1f)
                )

                // Sub-Card 5: Net Worth
                FinancialIndicatorItem(
                    label = LanguageHelper.getString("net_worth", languageMode),
                    amount = calculatedNetWorth,
                    icon = Icons.Default.Savings,
                    iconTint = SolidPrimary,
                    amountColor = SolidPrimary,
                    languageMode = languageMode,
                    onClick = { onNetWorthClick(0) },
                    modifier = Modifier.weight(1f)
                )

                // Sub-Card 6: Net Earnings (Savings)
                FinancialIndicatorItem(
                    label = LanguageHelper.getString("net_earnings", languageMode),
                    amount = overview.monthlyNetSavings,
                    icon = Icons.Default.AccountBalance,
                    iconTint = if (overview.monthlyNetSavings >= 0) SolidIncome else SolidExpense,
                    amountColor = if (overview.monthlyNetSavings >= 0) SolidIncome else SolidExpense,
                    languageMode = languageMode,
                    onClick = onNetEarningsClick,
                    modifier = Modifier.weight(1f)
                )
            }

            // Expandable Calculation Walkthrough
            AnimatedVisibility(visible = showFormulaBreakdown) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                        .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)), RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = LanguageHelper.getString("expendable_breakdown", languageMode),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA)
                            "• সক্রিয় ও অন্তর্ভুক্ত সম্পদ = ${LanguageHelper.formatCurrency(calculatedAssets, languageMode)}"
                        else
                            "• Active & Included Assets = ${LanguageHelper.formatCurrency(calculatedAssets, languageMode)}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA)
                            "• সক্রিয় ও অন্তর্ভুক্ত দায় = ${LanguageHelper.formatCurrency(if (calculatedLiabilities > 0) -calculatedLiabilities else calculatedLiabilities, languageMode)}"
                        else
                            "• Active & Included Liabilities = ${LanguageHelper.formatCurrency(if (calculatedLiabilities > 0) -calculatedLiabilities else calculatedLiabilities, languageMode)}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA)
                            "• বাজেটের অবশিষ্ট খরচ = ${LanguageHelper.formatCurrency(overview.remainingExpenses, languageMode)}"
                        else
                            "• Remaining Budget Expenses = ${LanguageHelper.formatCurrency(overview.remainingExpenses, languageMode)}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA)
                            "• খরচযোগ্য অর্থ = সম্পদ − দায় − অবশিষ্ট খরচ = ${LanguageHelper.formatCurrency(expendable, languageMode)}"
                        else
                            "• Expendable = Assets − Liabilities − Remaining Expenses = ${LanguageHelper.formatCurrency(expendable, languageMode)}",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA)
                            "• বাজেট থেকে সম্ভাব্য আয় = ${LanguageHelper.formatCurrency(overview.potentialIncome, languageMode)}"
                        else
                            "• Expected Income from Budget = ${LanguageHelper.formatCurrency(overview.potentialIncome, languageMode)}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA)
                            "• প্রত্যাশিত খরচযোগ্য অর্থ = খরচযোগ্য অর্থ + সম্ভাব্য আয় = ${LanguageHelper.formatCurrency(expectedExpendable, languageMode)}"
                        else
                            "• Expected Expendable = Expendable + Expected Income = ${LanguageHelper.formatCurrency(expectedExpendable, languageMode)}",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

/**
 * Reusable clickable financial indicator tile for sub-cards
 */
@Composable
private fun FinancialIndicatorItem(
    label: String,
    amount: Double,
    icon: ImageVector,
    iconTint: Color,
    languageMode: LanguageMode,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    amountColor: Color = MaterialTheme.colorScheme.onSurface,
    trendIndicator: String? = null,
    trendColor: Color = MaterialTheme.colorScheme.outline
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = label,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (trendIndicator != null) {
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = trendIndicator,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = trendColor
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = LanguageHelper.formatCurrency(amount, languageMode),
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = amountColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun QuickActionsRow(
    languageMode: LanguageMode,
    onAddTransactionClick: (TransactionType) -> Unit,
    onOpenCalculator: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = { onAddTransactionClick(TransactionType.EXPENSE) },
            modifier = Modifier.weight(1f).height(44.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SolidExpense),
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(2.dp))
                Text(LanguageHelper.getString("expense", languageMode), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Button(
            onClick = { onAddTransactionClick(TransactionType.INCOME) },
            modifier = Modifier.weight(1f).height(44.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SolidIncome),
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(2.dp))
                Text(LanguageHelper.getString("income", languageMode), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Button(
            onClick = { onAddTransactionClick(TransactionType.TRANSFER) },
            modifier = Modifier.weight(1f).height(44.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SolidTransfer),
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(2.dp))
                Text(LanguageHelper.getString("transfer", languageMode), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Button(
            onClick = onOpenCalculator,
            modifier = Modifier.weight(1f).height(44.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SolidPrimary),
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(2.dp))
                Text(LanguageHelper.getString("calculator", languageMode), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun RecentTransactionsCard(
    recentTransactions: List<TransactionWithDetails>,
    languageMode: LanguageMode,
    onTransactionClick: (Transaction) -> Unit,
    onViewAllClick: () -> Unit,
    onAmountClick: ((TransactionWithDetails) -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = LanguageHelper.getString("recent_transactions", languageMode),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (recentTransactions.isNotEmpty()) {
                    Text(
                        text = LanguageHelper.getString("all_transactions", languageMode),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SolidPrimary,
                        modifier = Modifier.clickable { onViewAllClick() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (recentTransactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = LanguageHelper.getString("no_transactions", languageMode),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    recentTransactions.take(5).forEach { item ->
                        val tx = item.transaction
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onTransactionClick(tx) },
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    val iconColor = when (tx.type) {
                                        TransactionType.EXPENSE -> SolidExpense
                                        TransactionType.INCOME -> SolidIncome
                                        TransactionType.TRANSFER -> SolidTransfer
                                    }
                                    val iconBg = when (tx.type) {
                                        TransactionType.EXPENSE -> SolidExpenseContainer
                                        TransactionType.INCOME -> SolidIncomeContainer
                                        TransactionType.TRANSFER -> SolidPrimaryContainer
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(iconBg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        val icon = when (tx.type) {
                                            TransactionType.EXPENSE -> IconHelper.getIconByName(item.category?.iconName ?: "Category")
                                            TransactionType.INCOME -> IconHelper.getIconByName(item.category?.iconName ?: "Payments")
                                            TransactionType.TRANSFER -> Icons.Default.SwapHoriz
                                        }
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = iconColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Column {
                                        val title = when (tx.type) {
                                            TransactionType.EXPENSE -> item.subCategory?.localizedName(languageMode)
                                                ?: item.category?.localizedName(languageMode)
                                                ?: "Expense"
                                            TransactionType.INCOME -> item.subCategory?.localizedName(languageMode)
                                                ?: item.category?.localizedName(languageMode)
                                                ?: "Income"
                                            TransactionType.TRANSFER -> LanguageHelper.getString("transfer", languageMode)
                                        }
                                        Text(
                                            text = title,
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        Spacer(modifier = Modifier.height(2.dp))

                                        DoubleEntryFlowBadge(
                                            item = item,
                                            languageMode = languageMode
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    val isRevert = tx.amount < 0
                                    val isPositiveEffect = when (tx.type) {
                                        TransactionType.EXPENSE -> isRevert
                                        TransactionType.INCOME -> !isRevert
                                        TransactionType.TRANSFER -> false
                                    }
                                    val sign = when (tx.type) {
                                        TransactionType.TRANSFER -> ""
                                        else -> if (isPositiveEffect) "+" else "−"
                                    }
                                    val amtColor = when (tx.type) {
                                        TransactionType.TRANSFER -> SolidTransfer
                                        else -> if (isPositiveEffect) SolidIncome else SolidExpense
                                    }
                                    Text(
                                        text = "$sign${LanguageHelper.formatCurrency(Math.abs(tx.amount), languageMode)}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = amtColor,
                                        modifier = if (onAmountClick != null) Modifier.clickable { onAmountClick(item) } else Modifier
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = DateUtils.formatShortDate(tx.dateEpochMs, languageMode),
                                        fontSize = 10.sp,
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
