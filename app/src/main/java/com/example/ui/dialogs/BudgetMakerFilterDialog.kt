package com.example.ui.dialogs

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Account
import com.example.data.model.AccountType
import com.example.data.model.Category
import com.example.data.model.CategoryType
import com.example.data.model.LanguageMode
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidPrimary
import com.example.util.DateUtils
import com.example.util.IconHelper

private val AmberGold = Color(0xFFD97706)

/**
 * Filter state specifically crafted for Budget Maker tabs.
 * Independent and context-aware for Expenses, Incomes, Assets, and Liabilities.
 */
data class BudgetMakerTabFilter(
    val selectedItemIds: Set<Long> = emptySet(),
    val selectedGroupNames: Set<String> = emptySet(),
    val minAmount: Double? = null,
    val maxAmount: Double? = null,

    // Expense & Income Conditions
    val onlyWithBudgetOrTarget: Boolean = false,
    val onlyWithoutBudgetOrTarget: Boolean = false,
    val onlyWithActualActivity: Boolean = false,
    val onlyZeroActivity: Boolean = false,
    val onlyOverBudget: Boolean = false,
    val onlyUnderBudgetRemaining: Boolean = false,
    val onlyTargetAchieved: Boolean = false,
    val onlyTargetPending: Boolean = false,
    val onlyWithSuggestions: Boolean = false,

    // Asset & Liability Conditions
    val onlyPositiveBalance: Boolean = false,
    val onlyZeroBalance: Boolean = false,
    val onlyNegativeBalance: Boolean = false,
    val onlyOutstandingDebt: Boolean = false,
    val onlyClearedDebt: Boolean = false,

    // Display & Cleanliness Toggles
    val excludeZeroAmounts: Boolean = false,
    val hideEmptyGroups: Boolean = false
) {
    val isActive: Boolean
        get() = selectedItemIds.isNotEmpty() ||
                selectedGroupNames.isNotEmpty() ||
                minAmount != null ||
                maxAmount != null ||
                onlyWithBudgetOrTarget ||
                onlyWithoutBudgetOrTarget ||
                onlyWithActualActivity ||
                onlyZeroActivity ||
                onlyOverBudget ||
                onlyUnderBudgetRemaining ||
                onlyTargetAchieved ||
                onlyTargetPending ||
                onlyWithSuggestions ||
                onlyPositiveBalance ||
                onlyZeroBalance ||
                onlyNegativeBalance ||
                onlyOutstandingDebt ||
                onlyClearedDebt ||
                excludeZeroAmounts ||
                hideEmptyGroups

    val activeCount: Int
        get() {
            var count = 0
            if (selectedItemIds.isNotEmpty() || selectedGroupNames.isNotEmpty()) count++
            if (minAmount != null || maxAmount != null) count++
            if (onlyWithBudgetOrTarget || onlyWithoutBudgetOrTarget) count++
            if (onlyWithActualActivity || onlyZeroActivity) count++
            if (onlyOverBudget || onlyUnderBudgetRemaining) count++
            if (onlyTargetAchieved || onlyTargetPending) count++
            if (onlyWithSuggestions) count++
            if (onlyPositiveBalance || onlyZeroBalance || onlyNegativeBalance) count++
            if (onlyOutstandingDebt || onlyClearedDebt) count++
            if (excludeZeroAmounts) count++
            if (hideEmptyGroups) count++
            return count
        }
}

/**
 * Filter state for BM Dashboard (Overview tab).
 */
data class BudgetMakerDashboardFilter(
    val includeExpenses: Boolean = true,
    val includeIncomes: Boolean = true,
    val includeAssets: Boolean = true,
    val includeLiabilities: Boolean = true,
    val onlyNonZero: Boolean = false
) {
    val isActive: Boolean
        get() = !includeExpenses || !includeIncomes || !includeAssets || !includeLiabilities || onlyNonZero

    val activeCount: Int
        get() {
            var count = 0
            if (!includeExpenses || !includeIncomes || !includeAssets || !includeLiabilities) count++
            if (onlyNonZero) count++
            return count
        }
}

/**
 * Modern, tab-specific popup filter dialog for Budget Maker.
 * Excludes unnecessary comparison/tracking options and delivers tailored controls for the active tab.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BudgetMakerFilterDialog(
    tabIndex: Int, // 0: Dashboard, 1: Expenses, 2: Incomes, 3: Assets, 4: Liabilities
    currentTabFilter: BudgetMakerTabFilter,
    dashboardFilter: BudgetMakerDashboardFilter,
    categories: List<Category>,
    accounts: List<Account>,
    selectedYear: Int,
    selectedMonth: Int,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onApplyTabFilter: (BudgetMakerTabFilter) -> Unit,
    onApplyDashboardFilter: (BudgetMakerDashboardFilter) -> Unit
) {
    val context = LocalContext.current
    val isBangla = languageMode == LanguageMode.BANGLA
    val monthNameStr = DateUtils.formatMonthYear(selectedYear, selectedMonth, languageMode)

    var tempTabFilter by remember { mutableStateOf(currentTabFilter) }
    var tempDashboardFilter by remember { mutableStateOf(dashboardFilter) }

    // Tab attributes
    val (tabTitle, tabSubtitle, tabIcon, tabColor) = when (tabIndex) {
        0 -> Quadruple(
            if (isBangla) "ড্যাশবোর্ড ফিল্টার" else "BM Dashboard Filter",
            if (isBangla) "$monthNameStr • ওভারভিউ সেটিংস" else "$monthNameStr • Overview scope",
            Icons.Default.Dashboard,
            MaterialTheme.colorScheme.primary
        )
        1 -> Quadruple(
            if (isBangla) "ব্যয় বাজেট ফিল্টার" else "Expense Budget Filter",
            if (isBangla) "$monthNameStr • ব্যয় ক্যাটাগরি ও সীমা ফিল্টার" else "$monthNameStr • Filter expense categories & limits",
            Icons.Default.TrendingDown,
            SolidExpense
        )
        2 -> Quadruple(
            if (isBangla) "আয় লক্ষ্য ফিল্টার" else "Income Target Filter",
            if (isBangla) "$monthNameStr • আয় ক্যাটাগরি ও লক্ষ্য ফিল্টার" else "$monthNameStr • Filter income categories & targets",
            Icons.Default.TrendingUp,
            SolidIncome
        )
        3 -> Quadruple(
            if (isBangla) "সম্পদ একাউন্ট ফিল্টার" else "Asset Accounts Filter",
            if (isBangla) "$monthNameStr • সম্পদ ও ব্যালেন্স ফিল্টার" else "$monthNameStr • Filter asset accounts & balances",
            Icons.Default.AccountBalanceWallet,
            SolidPrimary
        )
        4 -> Quadruple(
            if (isBangla) "দায় ও দেনা ফিল্টার" else "Liabilities & Debt Filter",
            if (isBangla) "$monthNameStr • ঋণ ও দেনা ফিল্টার" else "$monthNameStr • Filter debt & liability accounts",
            Icons.Default.CreditCard,
            AmberGold
        )
        else -> Quadruple("Budget Filter", monthNameStr, Icons.Default.FilterList, MaterialTheme.colorScheme.primary)
    }

    val activeCount = if (tabIndex == 0) tempDashboardFilter.activeCount else tempTabFilter.activeCount

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .testTag("budget_maker_filter_dialog")
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Surface(
                    color = tabColor.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = tabColor.copy(alpha = 0.20f),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = tabIcon,
                                        contentDescription = null,
                                        tint = tabColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = tabTitle,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (activeCount > 0) {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = tabColor,
                                            modifier = Modifier.padding(start = 2.dp)
                                        ) {
                                            Text(
                                                text = "$activeCount",
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = tabSubtitle,
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.outline,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            TextButton(
                                onClick = {
                                    if (tabIndex == 0) {
                                        tempDashboardFilter = BudgetMakerDashboardFilter()
                                    } else {
                                        tempTabFilter = BudgetMakerTabFilter()
                                    }
                                    Toast.makeText(
                                        context,
                                        if (isBangla) "ফিল্টার রিসেট করা হয়েছে" else "Filters reset",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            ) {
                                Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = if (isBangla) "রিসেট" else "Reset",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

                // Scrollable Body
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    when (tabIndex) {
                        0 -> {
                            // BM DASHBOARD FILTER
                            DashboardFilterSection(
                                filter = tempDashboardFilter,
                                onFilterChange = { tempDashboardFilter = it },
                                isBangla = isBangla
                            )
                        }
                        1 -> {
                            // EXPENSES FILTER
                            val expenseCats = remember(categories) {
                                categories.filter { it.type == CategoryType.EXPENSE && it.isActive }
                            }
                            CategoryTabFilterSection(
                                sectionTitle = if (isBangla) "ব্যয় ক্যাটাগরি ও গ্রুপ" else "Expense Categories & Groups",
                                sectionSubtitle = if (isBangla) "নির্দিষ্ট ক্যাটাগরি বা গ্রুপ নির্বাচন করুন" else "Select specific categories or groups",
                                categories = expenseCats,
                                selectedItemIds = tempTabFilter.selectedItemIds,
                                selectedGroupNames = tempTabFilter.selectedGroupNames,
                                onItemIdsChange = { tempTabFilter = tempTabFilter.copy(selectedItemIds = it) },
                                onGroupNamesChange = { tempTabFilter = tempTabFilter.copy(selectedGroupNames = it) },
                                accentColor = tabColor,
                                isBangla = isBangla
                            )

                            AmountRangeCard(
                                title = if (isBangla) "বাজেট সীমা রেঞ্জ (৳)" else "Budget Limit Range (৳)",
                                minAmount = tempTabFilter.minAmount,
                                maxAmount = tempTabFilter.maxAmount,
                                onMinChange = { tempTabFilter = tempTabFilter.copy(minAmount = it) },
                                onMaxChange = { tempTabFilter = tempTabFilter.copy(maxAmount = it) },
                                isBangla = isBangla,
                                accentColor = tabColor
                            )

                            ExpenseConditionsCard(
                                filter = tempTabFilter,
                                onFilterChange = { tempTabFilter = it },
                                isBangla = isBangla,
                                accentColor = tabColor
                            )

                            DisplayOptionsCard(
                                excludeZero = tempTabFilter.excludeZeroAmounts,
                                hideEmptyGroups = tempTabFilter.hideEmptyGroups,
                                onExcludeZeroChange = { tempTabFilter = tempTabFilter.copy(excludeZeroAmounts = it) },
                                onHideEmptyGroupsChange = { tempTabFilter = tempTabFilter.copy(hideEmptyGroups = it) },
                                isBangla = isBangla,
                                accentColor = tabColor
                            )
                        }
                        2 -> {
                            // INCOMES FILTER
                            val incomeCats = remember(categories) {
                                categories.filter { it.type == CategoryType.INCOME && it.isActive }
                            }
                            CategoryTabFilterSection(
                                sectionTitle = if (isBangla) "আয় ক্যাটাগরি ও গ্রুপ" else "Income Categories & Groups",
                                sectionSubtitle = if (isBangla) "নির্দিষ্ট আয়ের উৎস বা গ্রুপ নির্বাচন করুন" else "Select specific income sources or groups",
                                categories = incomeCats,
                                selectedItemIds = tempTabFilter.selectedItemIds,
                                selectedGroupNames = tempTabFilter.selectedGroupNames,
                                onItemIdsChange = { tempTabFilter = tempTabFilter.copy(selectedItemIds = it) },
                                onGroupNamesChange = { tempTabFilter = tempTabFilter.copy(selectedGroupNames = it) },
                                accentColor = tabColor,
                                isBangla = isBangla
                            )

                            AmountRangeCard(
                                title = if (isBangla) "আয় লক্ষ্য রেঞ্জ (৳)" else "Income Target Range (৳)",
                                minAmount = tempTabFilter.minAmount,
                                maxAmount = tempTabFilter.maxAmount,
                                onMinChange = { tempTabFilter = tempTabFilter.copy(minAmount = it) },
                                onMaxChange = { tempTabFilter = tempTabFilter.copy(maxAmount = it) },
                                isBangla = isBangla,
                                accentColor = tabColor
                            )

                            IncomeConditionsCard(
                                filter = tempTabFilter,
                                onFilterChange = { tempTabFilter = it },
                                isBangla = isBangla,
                                accentColor = tabColor
                            )

                            DisplayOptionsCard(
                                excludeZero = tempTabFilter.excludeZeroAmounts,
                                hideEmptyGroups = tempTabFilter.hideEmptyGroups,
                                onExcludeZeroChange = { tempTabFilter = tempTabFilter.copy(excludeZeroAmounts = it) },
                                onHideEmptyGroupsChange = { tempTabFilter = tempTabFilter.copy(hideEmptyGroups = it) },
                                isBangla = isBangla,
                                accentColor = tabColor
                            )
                        }
                        3 -> {
                            // ASSETS FILTER
                            val assetAccounts = remember(accounts) {
                                accounts.filter { it.type == AccountType.ASSET && it.isActive }
                            }
                            AccountTabFilterSection(
                                sectionTitle = if (isBangla) "সম্পদ একাউন্ট ও ধরন" else "Asset Accounts & Types",
                                sectionSubtitle = if (isBangla) "নির্দিষ্ট ব্যাংক, ক্যাশ বা ওয়ালেট একাউন্ট নির্বাচন করুন" else "Select specific banks, wallets, or asset accounts",
                                accounts = assetAccounts,
                                selectedItemIds = tempTabFilter.selectedItemIds,
                                selectedGroupNames = tempTabFilter.selectedGroupNames,
                                onItemIdsChange = { tempTabFilter = tempTabFilter.copy(selectedItemIds = it) },
                                onGroupNamesChange = { tempTabFilter = tempTabFilter.copy(selectedGroupNames = it) },
                                accentColor = tabColor,
                                isBangla = isBangla
                            )

                            AmountRangeCard(
                                title = if (isBangla) "ব্যালেন্স / লক্ষ্য রেঞ্জ (৳)" else "Balance / Target Range (৳)",
                                minAmount = tempTabFilter.minAmount,
                                maxAmount = tempTabFilter.maxAmount,
                                onMinChange = { tempTabFilter = tempTabFilter.copy(minAmount = it) },
                                onMaxChange = { tempTabFilter = tempTabFilter.copy(maxAmount = it) },
                                isBangla = isBangla,
                                accentColor = tabColor
                            )

                            AssetConditionsCard(
                                filter = tempTabFilter,
                                onFilterChange = { tempTabFilter = it },
                                isBangla = isBangla,
                                accentColor = tabColor
                            )

                            DisplayOptionsCard(
                                excludeZero = tempTabFilter.excludeZeroAmounts,
                                hideEmptyGroups = tempTabFilter.hideEmptyGroups,
                                onExcludeZeroChange = { tempTabFilter = tempTabFilter.copy(excludeZeroAmounts = it) },
                                onHideEmptyGroupsChange = { tempTabFilter = tempTabFilter.copy(hideEmptyGroups = it) },
                                isBangla = isBangla,
                                accentColor = tabColor
                            )
                        }
                        4 -> {
                            // LIABILITIES FILTER
                            val liabilityAccounts = remember(accounts) {
                                accounts.filter { it.type == AccountType.LIABILITY && it.isActive }
                            }
                            AccountTabFilterSection(
                                sectionTitle = if (isBangla) "দায় ও দেনা একাউন্ট" else "Liability & Debt Accounts",
                                sectionSubtitle = if (isBangla) "ক্রেডিট কার্ড, লোন বা দেনা একাউন্ট নির্বাচন করুন" else "Select credit cards, loans, or payable accounts",
                                accounts = liabilityAccounts,
                                selectedItemIds = tempTabFilter.selectedItemIds,
                                selectedGroupNames = tempTabFilter.selectedGroupNames,
                                onItemIdsChange = { tempTabFilter = tempTabFilter.copy(selectedItemIds = it) },
                                onGroupNamesChange = { tempTabFilter = tempTabFilter.copy(selectedGroupNames = it) },
                                accentColor = tabColor,
                                isBangla = isBangla
                            )

                            AmountRangeCard(
                                title = if (isBangla) "দেনা পরিমাণ রেঞ্জ (৳)" else "Debt Amount Range (৳)",
                                minAmount = tempTabFilter.minAmount,
                                maxAmount = tempTabFilter.maxAmount,
                                onMinChange = { tempTabFilter = tempTabFilter.copy(minAmount = it) },
                                onMaxChange = { tempTabFilter = tempTabFilter.copy(maxAmount = it) },
                                isBangla = isBangla,
                                accentColor = tabColor
                            )

                            LiabilityConditionsCard(
                                filter = tempTabFilter,
                                onFilterChange = { tempTabFilter = it },
                                isBangla = isBangla,
                                accentColor = tabColor
                            )

                            DisplayOptionsCard(
                                excludeZero = tempTabFilter.excludeZeroAmounts,
                                hideEmptyGroups = tempTabFilter.hideEmptyGroups,
                                onExcludeZeroChange = { tempTabFilter = tempTabFilter.copy(excludeZeroAmounts = it) },
                                onHideEmptyGroupsChange = { tempTabFilter = tempTabFilter.copy(hideEmptyGroups = it) },
                                isBangla = isBangla,
                                accentColor = tabColor
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

                // Footer Actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (isBangla) "বাতিল" else "Cancel")
                    }

                    Button(
                        onClick = {
                            if (tabIndex == 0) {
                                onApplyDashboardFilter(tempDashboardFilter)
                            } else {
                                onApplyTabFilter(tempTabFilter)
                            }
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = tabColor),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text(
                                text = if (isBangla) "ফিল্টার প্রয়োগ করুন" else "Apply Filter",
                                fontWeight = FontWeight.Bold
                            )
                            if (activeCount > 0) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color.White.copy(alpha = 0.25f),
                                    modifier = Modifier.padding(start = 2.dp)
                                ) {
                                    Text(
                                        text = "$activeCount",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
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

/**
 * Category & Group multi-selection section.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoryTabFilterSection(
    sectionTitle: String,
    sectionSubtitle: String,
    categories: List<Category>,
    selectedItemIds: Set<Long>,
    selectedGroupNames: Set<String>,
    onItemIdsChange: (Set<Long>) -> Unit,
    onGroupNamesChange: (Set<String>) -> Unit,
    accentColor: Color,
    isBangla: Boolean
) {
    var searchQuery by remember { mutableStateOf("") }
    val parentMap = remember(categories) {
        categories.filter { it.parentId == null }.associateBy { it.id }
    }
    val childCategories = remember(categories) {
        categories.filter { it.parentId != null }
    }
    val standaloneCategories = remember(categories) {
        val parentIdsWithChildren = childCategories.mapNotNull { it.parentId }.toSet()
        categories.filter { it.parentId == null && !parentIdsWithChildren.contains(it.id) }
    }

    // Grouping
    val groupToItemsMap = remember(categories, parentMap, childCategories, standaloneCategories) {
        val map = mutableMapOf<String, MutableList<Category>>()
        childCategories.forEach { child ->
            val parentName = parentMap[child.parentId]?.nameEn ?: "Other"
            map.getOrPut(parentName) { mutableListOf() }.add(child)
        }
        standaloneCategories.forEach { standalone ->
            map.getOrPut("General") { mutableListOf() }.add(standalone)
        }
        map
    }

    val filteredGroups = remember(groupToItemsMap, searchQuery) {
        if (searchQuery.isBlank()) {
            groupToItemsMap
        } else {
            val q = searchQuery.trim().lowercase()
            groupToItemsMap.mapValues { entry ->
                entry.value.filter { cat ->
                    cat.nameEn.lowercase().contains(q) ||
                    cat.nameBn.lowercase().contains(q) ||
                    entry.key.lowercase().contains(q)
                }
            }.filter { it.value.isNotEmpty() }
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = sectionTitle, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = MaterialTheme.colorScheme.onSurface)
                    Text(text = sectionSubtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (selectedItemIds.isNotEmpty() || selectedGroupNames.isNotEmpty()) {
                        TextButton(
                            onClick = {
                                onItemIdsChange(emptySet())
                                onGroupNamesChange(emptySet())
                            }
                        ) {
                            Text(if (isBangla) "সাফ করুন" else "Clear", fontSize = 11.5.sp, color = accentColor)
                        }
                    }
                }
            }

            // Search input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(if (isBangla) "ক্যাটাগরি বা গ্রুপ খুঁজুন..." else "Search category or group...", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(14.dp))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentColor,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
            )

            // Category Groups with expandable items
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                filteredGroups.forEach { (groupName, items) ->
                    val isGroupSelected = selectedGroupNames.contains(groupName)
                    val allItemsSelected = items.isNotEmpty() && items.all { selectedItemIds.contains(it.id) }
                    val someItemsSelected = items.any { selectedItemIds.contains(it.id) }
                    var isExpanded by remember { mutableStateOf(false) }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            0.7.dp,
                            if (isGroupSelected || someItemsSelected) accentColor.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { isExpanded = !isExpanded }
                                ) {
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = groupName,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.5.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                    ) {
                                        Text(
                                            text = "${items.size}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.outline,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }

                                Checkbox(
                                    checked = isGroupSelected || allItemsSelected,
                                    onCheckedChange = { checked ->
                                        val newGroupSet = selectedGroupNames.toMutableSet()
                                        val newItemSet = selectedItemIds.toMutableSet()
                                        if (checked) {
                                            newGroupSet.add(groupName)
                                            items.forEach { newItemSet.add(it.id) }
                                        } else {
                                            newGroupSet.remove(groupName)
                                            items.forEach { newItemSet.remove(it.id) }
                                        }
                                        onGroupNamesChange(newGroupSet)
                                        onItemIdsChange(newItemSet)
                                    },
                                    colors = CheckboxDefaults.colors(checkedColor = accentColor),
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            // Subcategories chips when expanded
                            AnimatedVisibility(visible = isExpanded) {
                                Column(modifier = Modifier.padding(top = 8.dp, start = 8.dp)) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        items.forEach { cat ->
                                            val isCatSelected = selectedItemIds.contains(cat.id)
                                            val catName = if (isBangla && cat.nameBn.isNotBlank()) cat.nameBn else cat.nameEn
                                            FilterChip(
                                                selected = isCatSelected,
                                                onClick = {
                                                    val newSet = selectedItemIds.toMutableSet()
                                                    if (isCatSelected) newSet.remove(cat.id) else newSet.add(cat.id)
                                                    onItemIdsChange(newSet)
                                                },
                                                label = { Text(catName, fontSize = 11.5.sp) },
                                                leadingIcon = {
                                                    if (cat.iconName.isNotBlank()) {
                                                        Icon(
                                                            imageVector = IconHelper.getIconByName(cat.iconName),
                                                            contentDescription = null,
                                                            modifier = Modifier.size(13.dp)
                                                        )
                                                    }
                                                },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = accentColor.copy(alpha = 0.15f),
                                                    selectedLabelColor = accentColor,
                                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                                ),
                                                border = BorderStroke(
                                                    0.6.dp,
                                                    if (isCatSelected) accentColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                                ),
                                                modifier = Modifier.height(28.dp)
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

/**
 * Account & Group multi-selection section for Assets and Liabilities.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AccountTabFilterSection(
    sectionTitle: String,
    sectionSubtitle: String,
    accounts: List<Account>,
    selectedItemIds: Set<Long>,
    selectedGroupNames: Set<String>,
    onItemIdsChange: (Set<Long>) -> Unit,
    onGroupNamesChange: (Set<String>) -> Unit,
    accentColor: Color,
    isBangla: Boolean
) {
    var searchQuery by remember { mutableStateOf("") }
    val parentMap = remember(accounts) {
        accounts.filter { it.parentId == null }.associateBy { it.id }
    }
    val childAccounts = remember(accounts) {
        accounts.filter { it.parentId != null }
    }
    val standaloneAccounts = remember(accounts) {
        val parentIdsWithChildren = childAccounts.mapNotNull { it.parentId }.toSet()
        accounts.filter { it.parentId == null && !parentIdsWithChildren.contains(it.id) }
    }

    val groupToItemsMap = remember(accounts, parentMap, childAccounts, standaloneAccounts) {
        val map = mutableMapOf<String, MutableList<Account>>()
        childAccounts.forEach { child ->
            val parentName = parentMap[child.parentId]?.nameEn ?: "Other"
            map.getOrPut(parentName) { mutableListOf() }.add(child)
        }
        standaloneAccounts.forEach { standalone ->
            map.getOrPut("General Accounts") { mutableListOf() }.add(standalone)
        }
        map
    }

    val filteredGroups = remember(groupToItemsMap, searchQuery) {
        if (searchQuery.isBlank()) {
            groupToItemsMap
        } else {
            val q = searchQuery.trim().lowercase()
            groupToItemsMap.mapValues { entry ->
                entry.value.filter { acc ->
                    acc.nameEn.lowercase().contains(q) ||
                    acc.nameBn.lowercase().contains(q) ||
                    entry.key.lowercase().contains(q)
                }
            }.filter { it.value.isNotEmpty() }
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = sectionTitle, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = MaterialTheme.colorScheme.onSurface)
                    Text(text = sectionSubtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                }

                if (selectedItemIds.isNotEmpty() || selectedGroupNames.isNotEmpty()) {
                    TextButton(
                        onClick = {
                            onItemIdsChange(emptySet())
                            onGroupNamesChange(emptySet())
                        }
                    ) {
                        Text(if (isBangla) "সাফ করুন" else "Clear", fontSize = 11.5.sp, color = accentColor)
                    }
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(if (isBangla) "একাউন্ট বা গ্রুপ খুঁজুন..." else "Search account or group...", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(14.dp))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentColor,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                filteredGroups.forEach { (groupName, items) ->
                    val isGroupSelected = selectedGroupNames.contains(groupName)
                    val allItemsSelected = items.isNotEmpty() && items.all { selectedItemIds.contains(it.id) }
                    val someItemsSelected = items.any { selectedItemIds.contains(it.id) }
                    var isExpanded by remember { mutableStateOf(false) }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            0.7.dp,
                            if (isGroupSelected || someItemsSelected) accentColor.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { isExpanded = !isExpanded }
                                ) {
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = groupName,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.5.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                    ) {
                                        Text(
                                            text = "${items.size}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.outline,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }

                                Checkbox(
                                    checked = isGroupSelected || allItemsSelected,
                                    onCheckedChange = { checked ->
                                        val newGroupSet = selectedGroupNames.toMutableSet()
                                        val newItemSet = selectedItemIds.toMutableSet()
                                        if (checked) {
                                            newGroupSet.add(groupName)
                                            items.forEach { newItemSet.add(it.id) }
                                        } else {
                                            newGroupSet.remove(groupName)
                                            items.forEach { newItemSet.remove(it.id) }
                                        }
                                        onGroupNamesChange(newGroupSet)
                                        onItemIdsChange(newItemSet)
                                    },
                                    colors = CheckboxDefaults.colors(checkedColor = accentColor),
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            AnimatedVisibility(visible = isExpanded) {
                                Column(modifier = Modifier.padding(top = 8.dp, start = 8.dp)) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        items.forEach { acc ->
                                            val isAccSelected = selectedItemIds.contains(acc.id)
                                            val accName = if (isBangla && acc.nameBn.isNotBlank()) acc.nameBn else acc.nameEn
                                            FilterChip(
                                                selected = isAccSelected,
                                                onClick = {
                                                    val newSet = selectedItemIds.toMutableSet()
                                                    if (isAccSelected) newSet.remove(acc.id) else newSet.add(acc.id)
                                                    onItemIdsChange(newSet)
                                                },
                                                label = { Text(accName, fontSize = 11.5.sp) },
                                                leadingIcon = {
                                                    if (acc.iconName.isNotBlank()) {
                                                        Icon(
                                                            imageVector = IconHelper.getIconByName(acc.iconName),
                                                            contentDescription = null,
                                                            modifier = Modifier.size(13.dp)
                                                        )
                                                    }
                                                },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = accentColor.copy(alpha = 0.15f),
                                                    selectedLabelColor = accentColor,
                                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                                ),
                                                border = BorderStroke(
                                                    0.6.dp,
                                                    if (isAccSelected) accentColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                                ),
                                                modifier = Modifier.height(28.dp)
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

/**
 * Amount Range Card with presets.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AmountRangeCard(
    title: String,
    minAmount: Double?,
    maxAmount: Double?,
    onMinChange: (Double?) -> Unit,
    onMaxChange: (Double?) -> Unit,
    isBangla: Boolean,
    accentColor: Color
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = MaterialTheme.colorScheme.onSurface)
                if (minAmount != null || maxAmount != null) {
                    TextButton(onClick = {
                        onMinChange(null)
                        onMaxChange(null)
                    }) {
                        Text(if (isBangla) "সাফ করুন" else "Clear", fontSize = 11.5.sp, color = accentColor)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = minAmount?.toInt()?.toString() ?: "",
                    onValueChange = { onMinChange(it.toDoubleOrNull()) },
                    label = { Text(if (isBangla) "সর্বনিম্ন ৳" else "Min ৳", fontSize = 11.5.sp) },
                    placeholder = { Text("0", fontSize = 12.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                )

                OutlinedTextField(
                    value = maxAmount?.toInt()?.toString() ?: "",
                    onValueChange = { onMaxChange(it.toDoubleOrNull()) },
                    label = { Text(if (isBangla) "সর্বোচ্চ ৳" else "Max ৳", fontSize = 11.5.sp) },
                    placeholder = { Text("50000", fontSize = 12.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                )
            }

            // Quick preset chips
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val presets = listOf(
                    Triple(0.0, 1000.0, if (isBangla) "< ১,০০০ ৳" else "< 1k ৳"),
                    Triple(1000.0, 5000.0, if (isBangla) "১k - ৫k ৳" else "1k - 5k ৳"),
                    Triple(5000.0, 20000.0, if (isBangla) "৫k - ২০k ৳" else "5k - 20k ৳"),
                    Triple(20000.0, 100000.0, if (isBangla) "২০k - ১০০k ৳" else "20k - 100k ৳"),
                    Triple(100000.0, null, if (isBangla) "> ১০০k ৳" else "> 100k ৳")
                )

                presets.forEach { (min, max, label) ->
                    val isSelected = minAmount == min && maxAmount == max
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) accentColor.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(0.6.dp, if (isSelected) accentColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .clickable {
                                if (isSelected) {
                                    onMinChange(null)
                                    onMaxChange(null)
                                } else {
                                    onMinChange(min)
                                    onMaxChange(max)
                                }
                            }
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Expense specific conditions card.
 */
@Composable
private fun ExpenseConditionsCard(
    filter: BudgetMakerTabFilter,
    onFilterChange: (BudgetMakerTabFilter) -> Unit,
    isBangla: Boolean,
    accentColor: Color
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = if (isBangla) "ব্যয় স্থিতি ও সীমা শর্ত" else "Expense Status & Limit Conditions",
                fontWeight = FontWeight.Bold,
                fontSize = 13.5.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            ConditionRowItem(
                title = if (isBangla) "বাজেট অতিক্রম করেছে (Spent > Budget)" else "Over-Budget Only (Spent > Budget)",
                subtitle = if (isBangla) "যেসব ক্যাটাগরিতে বাজেট সীমার বেশি খরচ হয়েছে" else "Show categories exceeding budget limit",
                checked = filter.onlyOverBudget,
                onCheckedChange = { onFilterChange(filter.copy(onlyOverBudget = it, onlyUnderBudgetRemaining = false)) },
                accentColor = accentColor
            )

            ConditionRowItem(
                title = if (isBangla) "বাজেট অবশিষ্ট আছে (Budget > Spent)" else "Remaining Surplus Only (Budget > Spent)",
                subtitle = if (isBangla) "বাজেট বরাদ্দ আছে এবং টাকা বাকি আছে" else "Categories with remaining available funds",
                checked = filter.onlyUnderBudgetRemaining,
                onCheckedChange = { onFilterChange(filter.copy(onlyUnderBudgetRemaining = it, onlyOverBudget = false)) },
                accentColor = accentColor
            )

            ConditionRowItem(
                title = if (isBangla) "এই মাসে খরচ হয়েছে (Spent > ৳০)" else "Has Actual Spending (> ৳0)",
                subtitle = if (isBangla) "চলতি মাসে লেনদেন বা খরচ হয়েছে" else "Active categories with expenses this month",
                checked = filter.onlyWithActualActivity,
                onCheckedChange = { onFilterChange(filter.copy(onlyWithActualActivity = it, onlyZeroActivity = false)) },
                accentColor = accentColor
            )

            ConditionRowItem(
                title = if (isBangla) "কোনো খরচ হয়নি (৳০ Spent)" else "Zero Spending This Month (৳0)",
                subtitle = if (isBangla) "এই মাসে কোনো লেনদেন নেই" else "Categories with zero actual spending",
                checked = filter.onlyZeroActivity,
                onCheckedChange = { onFilterChange(filter.copy(onlyZeroActivity = it, onlyWithActualActivity = false)) },
                accentColor = accentColor
            )

            ConditionRowItem(
                title = if (isBangla) "বাজেট সেট করা আছে (> ৳০)" else "Budget Limit Configured (> ৳0)",
                subtitle = if (isBangla) "বাজেট মেকারে পরিমাণ বরাদ্দ দেওয়া হয়েছে" else "Categories with explicit budget limit",
                checked = filter.onlyWithBudgetOrTarget,
                onCheckedChange = { onFilterChange(filter.copy(onlyWithBudgetOrTarget = it, onlyWithoutBudgetOrTarget = false)) },
                accentColor = accentColor
            )

            ConditionRowItem(
                title = if (isBangla) "বাজেট ছাড়া (৳০)" else "Unbudgeted Only (৳0)",
                subtitle = if (isBangla) "যেসব ক্যাটাগরিতে বাজেট বরাদ্দ নেই" else "Categories with no budget allocated",
                checked = filter.onlyWithoutBudgetOrTarget,
                onCheckedChange = { onFilterChange(filter.copy(onlyWithoutBudgetOrTarget = it, onlyWithBudgetOrTarget = false)) },
                accentColor = accentColor
            )

            ConditionRowItem(
                title = if (isBangla) "স্মার্ট পরামর্শ উপলব্ধ" else "Smart Suggestions Available",
                subtitle = if (isBangla) "অতীতের ইতিহাস ও গড়ের ভিত্তিতে প্রস্তাবনা আছে" else "Items with AI/historical suggestion baselines",
                checked = filter.onlyWithSuggestions,
                onCheckedChange = { onFilterChange(filter.copy(onlyWithSuggestions = it)) },
                accentColor = accentColor
            )
        }
    }
}

/**
 * Income specific conditions card.
 */
@Composable
private fun IncomeConditionsCard(
    filter: BudgetMakerTabFilter,
    onFilterChange: (BudgetMakerTabFilter) -> Unit,
    isBangla: Boolean,
    accentColor: Color
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = if (isBangla) "আয় অর্জন ও লক্ষ্য শর্ত" else "Income Achievement & Target Conditions",
                fontWeight = FontWeight.Bold,
                fontSize = 13.5.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            ConditionRowItem(
                title = if (isBangla) "লক্ষ্য অর্জিত হয়েছে (Earned ≥ Target)" else "Target Achieved (Earned ≥ Target)",
                subtitle = if (isBangla) "প্রত্যাশিত আয় পূর্ণ বা তার বেশি হয়েছে" else "Income targets reached or exceeded",
                checked = filter.onlyTargetAchieved,
                onCheckedChange = { onFilterChange(filter.copy(onlyTargetAchieved = it, onlyTargetPending = false)) },
                accentColor = accentColor
            )

            ConditionRowItem(
                title = if (isBangla) "লক্ষ্য অর্জন বাকি (Earned < Target)" else "Target Pending (Earned < Target)",
                subtitle = if (isBangla) "লক্ষ্যমাত্রার চেয়ে কম আয় এসেছে" else "Income targets not yet fully reached",
                checked = filter.onlyTargetPending,
                onCheckedChange = { onFilterChange(filter.copy(onlyTargetPending = it, onlyTargetAchieved = false)) },
                accentColor = accentColor
            )

            ConditionRowItem(
                title = if (isBangla) "এই মাসে আয় এসেছে (> ৳০)" else "Has Actual Earnings (> ৳0)",
                subtitle = if (isBangla) "চলতি মাসে কার্যকর আয় জমা হয়েছে" else "Income sources with active inflows",
                checked = filter.onlyWithActualActivity,
                onCheckedChange = { onFilterChange(filter.copy(onlyWithActualActivity = it, onlyZeroActivity = false)) },
                accentColor = accentColor
            )

            ConditionRowItem(
                title = if (isBangla) "টার্গেট নির্ধারণ করা আছে" else "Target Configured (> ৳0)",
                subtitle = if (isBangla) "বাজেট মেকারে আয়ের লক্ষ্য সেট করা আছে" else "Categories with target earnings set",
                checked = filter.onlyWithBudgetOrTarget,
                onCheckedChange = { onFilterChange(filter.copy(onlyWithBudgetOrTarget = it, onlyWithoutBudgetOrTarget = false)) },
                accentColor = accentColor
            )

            ConditionRowItem(
                title = if (isBangla) "টার্গেট নির্ধারণ ছাড়া" else "No Target Set (৳0)",
                subtitle = if (isBangla) "আয়ের কোনো লক্ষ্য নির্ধারণ করা হয়নি" else "Categories without planned income target",
                checked = filter.onlyWithoutBudgetOrTarget,
                onCheckedChange = { onFilterChange(filter.copy(onlyWithoutBudgetOrTarget = it, onlyWithBudgetOrTarget = false)) },
                accentColor = accentColor
            )
        }
    }
}

/**
 * Asset specific conditions card.
 */
@Composable
private fun AssetConditionsCard(
    filter: BudgetMakerTabFilter,
    onFilterChange: (BudgetMakerTabFilter) -> Unit,
    isBangla: Boolean,
    accentColor: Color
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = if (isBangla) "সম্পদ ব্যালেন্স ও স্থিতি শর্ত" else "Asset Balance & Status Conditions",
                fontWeight = FontWeight.Bold,
                fontSize = 13.5.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            ConditionRowItem(
                title = if (isBangla) "ইতিবাচক ব্যালেন্স (> ৳০)" else "Positive Balance Only (> ৳0)",
                subtitle = if (isBangla) "যেসব একাউন্টে জমা টাকা আছে" else "Accounts holding positive balance",
                checked = filter.onlyPositiveBalance,
                onCheckedChange = { onFilterChange(filter.copy(onlyPositiveBalance = it, onlyZeroBalance = false)) },
                accentColor = accentColor
            )

            ConditionRowItem(
                title = if (isBangla) "শূন্য ব্যালেন্স (৳০)" else "Zero Balance Only (৳0)",
                subtitle = if (isBangla) "যেসব একাউন্টে বর্তমানে কোনো টাকা নেই" else "Accounts with empty or zero balance",
                checked = filter.onlyZeroBalance,
                onCheckedChange = { onFilterChange(filter.copy(onlyZeroBalance = it, onlyPositiveBalance = false)) },
                accentColor = accentColor
            )

            ConditionRowItem(
                title = if (isBangla) "টার্গেট ব্যালেন্স সেট করা আছে" else "Target Balance Configured",
                subtitle = if (isBangla) "নির্দিষ্ট ব্যালেন্স অর্জনের লক্ষ্য রয়েছে" else "Accounts with planned target balances",
                checked = filter.onlyWithBudgetOrTarget,
                onCheckedChange = { onFilterChange(filter.copy(onlyWithBudgetOrTarget = it, onlyWithoutBudgetOrTarget = false)) },
                accentColor = accentColor
            )

            ConditionRowItem(
                title = if (isBangla) "চলতি মাসে লেনদেন হয়েছে" else "Active with Monthly Transactions",
                subtitle = if (isBangla) "এই মাসে ডেবিট বা ক্রেডিট কার্যক্রম হয়েছে" else "Accounts involved in transactions this month",
                checked = filter.onlyWithActualActivity,
                onCheckedChange = { onFilterChange(filter.copy(onlyWithActualActivity = it)) },
                accentColor = accentColor
            )
        }
    }
}

/**
 * Liability specific conditions card.
 */
@Composable
private fun LiabilityConditionsCard(
    filter: BudgetMakerTabFilter,
    onFilterChange: (BudgetMakerTabFilter) -> Unit,
    isBangla: Boolean,
    accentColor: Color
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = if (isBangla) "দেনা ও ঋণ স্থিতি শর্ত" else "Liability & Debt Status Conditions",
                fontWeight = FontWeight.Bold,
                fontSize = 13.5.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            ConditionRowItem(
                title = if (isBangla) "বকেয়া দেনা বা ঋণ আছে (> ৳০)" else "Outstanding Debt Only (> ৳0)",
                subtitle = if (isBangla) "যেসব একাউন্টে ঋণ বা বকেয়া পরিশোধ বাকি" else "Accounts with pending debt balance",
                checked = filter.onlyOutstandingDebt,
                onCheckedChange = { onFilterChange(filter.copy(onlyOutstandingDebt = it, onlyClearedDebt = false)) },
                accentColor = accentColor
            )

            ConditionRowItem(
                title = if (isBangla) "পরিশোধিত বা শূন্য ঋণ (৳০)" else "Cleared / Zero Debt (৳0)",
                subtitle = if (isBangla) "কোনো সক্রিয় দেনা নেই" else "Accounts with no outstanding debt",
                checked = filter.onlyClearedDebt,
                onCheckedChange = { onFilterChange(filter.copy(onlyClearedDebt = it, onlyOutstandingDebt = false)) },
                accentColor = accentColor
            )

            ConditionRowItem(
                title = if (isBangla) "ঋণ সীমা বা পরিশোধ টার্গেট আছে" else "Debt Limit / Target Configured",
                subtitle = if (isBangla) "পরিশোধ বা সর্বোচ্চ সীমার লক্ষ্য নির্ধারণ করা আছে" else "Accounts with planned debt targets",
                checked = filter.onlyWithBudgetOrTarget,
                onCheckedChange = { onFilterChange(filter.copy(onlyWithBudgetOrTarget = it, onlyWithoutBudgetOrTarget = false)) },
                accentColor = accentColor
            )

            ConditionRowItem(
                title = if (isBangla) "চলতি মাসে লেনদেন হয়েছে" else "Active with Monthly Transactions",
                subtitle = if (isBangla) "এই মাসে ঋণ গ্রহণ বা পরিশোধের এন্ট্রি আছে" else "Accounts with debt activity this month",
                checked = filter.onlyWithActualActivity,
                onCheckedChange = { onFilterChange(filter.copy(onlyWithActualActivity = it)) },
                accentColor = accentColor
            )
        }
    }
}

/**
 * Display options card.
 */
@Composable
private fun DisplayOptionsCard(
    excludeZero: Boolean,
    hideEmptyGroups: Boolean,
    onExcludeZeroChange: (Boolean) -> Unit,
    onHideEmptyGroupsChange: (Boolean) -> Unit,
    isBangla: Boolean,
    accentColor: Color
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = if (isBangla) "প্রদর্শন পছন্দসমূহ" else "Display Preferences",
                fontWeight = FontWeight.Bold,
                fontSize = 13.5.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            ConditionRowItem(
                title = if (isBangla) "শূন্য আইটেম বাদ দিন (০ বাজেট ও ০ প্রকৃত)" else "Exclude Zero Items (0 Budget & 0 Actual)",
                subtitle = if (isBangla) "যেসব আইটেমে কোনো বাজেট এবং খরচ/আয় নেই সেগুলো লুকান" else "Hide items with neither budget nor activity",
                checked = excludeZero,
                onCheckedChange = onExcludeZeroChange,
                accentColor = accentColor
            )

            ConditionRowItem(
                title = if (isBangla) "খালি গ্রুপ লুকান" else "Hide Empty Groups",
                subtitle = if (isBangla) "যেসব গ্রুপে কোনো কার্যকর আইটেম নেই সেগুলো লুকান" else "Omit groups with no active items",
                checked = hideEmptyGroups,
                onCheckedChange = onHideEmptyGroupsChange,
                accentColor = accentColor
            )
        }
    }
}

/**
 * Dashboard Filter Section for Tab 0.
 */
@Composable
private fun DashboardFilterSection(
    filter: BudgetMakerDashboardFilter,
    onFilterChange: (BudgetMakerDashboardFilter) -> Unit,
    isBangla: Boolean
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = if (isBangla) "ড্যাশবোর্ড ওভারভিউ সুযোগ" else "Dashboard Overview Scope",
                fontWeight = FontWeight.Bold,
                fontSize = 13.5.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (isBangla) "ড্যাশবোর্ড সারাংশে কোন বিভাগগুলো অন্তর্ভুক্ত থাকবে তা নির্বাচন করুন:" else "Choose which financial sections to include in the overview summary:",
                fontSize = 11.5.sp,
                color = MaterialTheme.colorScheme.outline
            )

            ConditionRowItem(
                title = if (isBangla) "ব্যয় সারাংশ অন্তর্ভুক্ত করুন" else "Include Expenses Summary",
                subtitle = if (isBangla) "মোট পরিকল্পিত ও প্রকৃত ব্যয় হিসাব করুন" else "Include planned and actual expenses",
                checked = filter.includeExpenses,
                onCheckedChange = { onFilterChange(filter.copy(includeExpenses = it)) },
                accentColor = SolidExpense
            )

            ConditionRowItem(
                title = if (isBangla) "আয় সারাংশ অন্তর্ভুক্ত করুন" else "Include Incomes Summary",
                subtitle = if (isBangla) "মোট পরিকল্পিত ও প্রকৃত আয় হিসাব করুন" else "Include planned and actual incomes",
                checked = filter.includeIncomes,
                onCheckedChange = { onFilterChange(filter.copy(includeIncomes = it)) },
                accentColor = SolidIncome
            )

            ConditionRowItem(
                title = if (isBangla) "সম্পদ সারাংশ অন্তর্ভুক্ত করুন" else "Include Assets Summary",
                subtitle = if (isBangla) "টার্গেট ও বর্তমান ব্যালেন্স অন্তর্ভুক্ত করুন" else "Include asset target and live balance",
                checked = filter.includeAssets,
                onCheckedChange = { onFilterChange(filter.copy(includeAssets = it)) },
                accentColor = SolidPrimary
            )

            ConditionRowItem(
                title = if (isBangla) "দায় সারাংশ অন্তর্ভুক্ত করুন" else "Include Liabilities Summary",
                subtitle = if (isBangla) "ঋণ ও দেনার টার্গেট অন্তর্ভুক্ত করুন" else "Include liabilities and debt targets",
                checked = filter.includeLiabilities,
                onCheckedChange = { onFilterChange(filter.copy(includeLiabilities = it)) },
                accentColor = AmberGold
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

            ConditionRowItem(
                title = if (isBangla) "শুধু সক্রিয় অ-শূন্য মোট দেখান" else "Show Only Active Non-Zero Sections",
                subtitle = if (isBangla) "যেসব সেকশনে সক্রিয় পরিমাণ আছে শুধু সেগুলো প্রদর্শন করুন" else "Display only sections with non-zero financial figures",
                checked = filter.onlyNonZero,
                onCheckedChange = { onFilterChange(filter.copy(onlyNonZero = it)) },
                accentColor = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * Reusable condition row with title, description, and Switch/Checkbox.
 */
@Composable
private fun ConditionRowItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    accentColor: Color
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (checked) accentColor.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            0.6.dp,
            if (checked) accentColor.copy(alpha = 0.45f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onCheckedChange(!checked) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = if (checked) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 12.5.sp,
                    color = if (checked) accentColor else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 10.5.sp,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Checkbox(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(checkedColor = accentColor),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * Active Budget Maker Filter Bar strip displayed on screen.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ActiveBudgetMakerFilterBar(
    tabName: String,
    tabFilter: BudgetMakerTabFilter,
    onFilterChange: (BudgetMakerTabFilter) -> Unit,
    onOpenFilterDialog: () -> Unit,
    accentColor: Color,
    languageMode: LanguageMode,
    modifier: Modifier = Modifier
) {
    if (!tabFilter.isActive) return

    val isBangla = languageMode == LanguageMode.BANGLA

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = accentColor.copy(alpha = 0.10f),
        border = BorderStroke(0.8.dp, accentColor.copy(alpha = 0.30f)),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onOpenFilterDialog() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = accentColor
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(Icons.Default.FilterAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                        Text(
                            text = if (isBangla) "$tabName (${tabFilter.activeCount})" else "$tabName (${tabFilter.activeCount})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Text(
                    text = if (isBangla) "সক্রিয় ফিল্টার নিয়ম" else "Active Filter Rules Applied",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                onClick = { onFilterChange(BudgetMakerTabFilter()) },
                modifier = Modifier.size(22.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear Filters",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
