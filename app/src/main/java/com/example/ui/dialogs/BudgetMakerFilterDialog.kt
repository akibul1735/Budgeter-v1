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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.mutableDoubleStateOf
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
import com.example.data.model.Account
import com.example.data.model.AccountType
import com.example.data.model.Category
import com.example.data.model.CategoryType
import com.example.data.model.LanguageMode
import com.example.ui.components.PopupCalculatorDialog
import com.example.ui.components.UnifiedFilterDialogContainer
import com.example.ui.components.UnifiedFilterFooter
import com.example.ui.components.UnifiedFilterHeader
import com.example.ui.components.UnifiedFilterSection
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
                (minAmount != null && minAmount > 0) ||
                (maxAmount != null && maxAmount > 0) ||
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
            if (selectedItemIds.isNotEmpty() || selectedGroupNames.isNotEmpty()) count += (selectedItemIds.size + selectedGroupNames.size)
            if ((minAmount != null && minAmount > 0) || (maxAmount != null && maxAmount > 0)) count++
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
 * Modern, tab-specific popup filter dialog for Budget Maker designed uniformly with Transaction Filter.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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

    var minAmountText by remember { mutableStateOf(if (currentTabFilter.minAmount != null && currentTabFilter.minAmount > 0) currentTabFilter.minAmount.toInt().toString() else "") }
    var maxAmountText by remember { mutableStateOf(if (currentTabFilter.maxAmount != null && currentTabFilter.maxAmount > 0) currentTabFilter.maxAmount.toInt().toString() else "") }

    var showCalculatorForMin by remember { mutableStateOf(false) }
    var showCalculatorForMax by remember { mutableStateOf(false) }
    var showItemPickerModal by remember { mutableStateOf(false) }

    // Tab attributes
    val (tabTitle, tabSubtitle, tabIcon, tabColor) = when (tabIndex) {
        0 -> Quadruple(
            if (isBangla) "ড্যাশবোর্ড ফিল্টার" else "Dashboard Filter",
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

    UnifiedFilterDialogContainer(
        onDismissRequest = onDismiss,
        testTag = "budget_maker_filter_dialog"
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            UnifiedFilterHeader(
                title = tabTitle,
                activeCount = activeCount,
                languageMode = languageMode,
                onReset = {
                    if (tabIndex == 0) {
                        tempDashboardFilter = BudgetMakerDashboardFilter()
                    } else {
                        tempTabFilter = BudgetMakerTabFilter()
                        minAmountText = ""
                        maxAmountText = ""
                    }
                    Toast.makeText(
                        context,
                        if (isBangla) "ফিল্টার রিসেট করা হয়েছে" else "Filters reset",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                onDismiss = onDismiss
            )

            // Scrollable Body
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (tabIndex == 0) {
                    // BM DASHBOARD FILTER
                    UnifiedFilterSection(
                        title = if (isBangla) "অন্তর্ভুক্ত বিভাগসমূহ" else "Included Sections",
                        icon = Icons.Default.Tune
                    ) {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                BudgetSettingToggleRow(
                                    title = if (isBangla) "ব্যয় বিভাগ প্রদর্শন" else "Include Expenses",
                                    checked = tempDashboardFilter.includeExpenses,
                                    onCheckedChange = { tempDashboardFilter = tempDashboardFilter.copy(includeExpenses = it) }
                                )
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                BudgetSettingToggleRow(
                                    title = if (isBangla) "আয় বিভাগ প্রদর্শন" else "Include Incomes",
                                    checked = tempDashboardFilter.includeIncomes,
                                    onCheckedChange = { tempDashboardFilter = tempDashboardFilter.copy(includeIncomes = it) }
                                )
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                BudgetSettingToggleRow(
                                    title = if (isBangla) "সম্পদ বিভাগ প্রদর্শন" else "Include Assets",
                                    checked = tempDashboardFilter.includeAssets,
                                    onCheckedChange = { tempDashboardFilter = tempDashboardFilter.copy(includeAssets = it) }
                                )
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                BudgetSettingToggleRow(
                                    title = if (isBangla) "দায় ও দেনা প্রদর্শন" else "Include Liabilities",
                                    checked = tempDashboardFilter.includeLiabilities,
                                    onCheckedChange = { tempDashboardFilter = tempDashboardFilter.copy(includeLiabilities = it) }
                                )
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                BudgetSettingToggleRow(
                                    title = if (isBangla) "শুধুমাত্র সক্রিয়/নন-জিরো এন্ট্রি" else "Only Non-Zero Entries",
                                    checked = tempDashboardFilter.onlyNonZero,
                                    onCheckedChange = { tempDashboardFilter = tempDashboardFilter.copy(onlyNonZero = it) }
                                )
                            }
                        }
                    }
                } else {
                    // TAB-SPECIFIC FILTER (1: Expense, 2: Income, 3: Asset, 4: Liability)

                    // 1. Specific Items / Categories Multi-Select Dropdown Row
                    val itemTypeLabel = when (tabIndex) {
                        1, 2 -> if (isBangla) "ক্যাটাগরি" else "Category"
                        else -> if (isBangla) "অ্যাকাউন্ট" else "Account"
                    }
                    val selectedCount = tempTabFilter.selectedItemIds.size + tempTabFilter.selectedGroupNames.size
                    val itemSummaryText = when {
                        selectedCount == 0 -> if (isBangla) "সকল $itemTypeLabel (কোনো ফিল্টার নেই)" else "All ${itemTypeLabel}s (No Filter)"
                        selectedCount == 1 -> {
                            if (tempTabFilter.selectedGroupNames.isNotEmpty()) {
                                tempTabFilter.selectedGroupNames.first()
                            } else {
                                val id = tempTabFilter.selectedItemIds.first()
                                if (tabIndex in 1..2) {
                                    categories.firstOrNull { it.id == id }?.localizedName(languageMode) ?: "1 $itemTypeLabel"
                                } else {
                                    accounts.firstOrNull { it.id == id }?.localizedName(languageMode) ?: "1 $itemTypeLabel"
                                }
                            }
                        }
                        else -> if (isBangla) "$selectedCount টি $itemTypeLabel নির্বাচিত" else "$selectedCount ${itemTypeLabel}s Selected"
                    }

                    BudgetFilterDropdownRow(
                        title = itemTypeLabel,
                        selectedValue = itemSummaryText,
                        icon = if (tabIndex in 1..2) Icons.Default.Category else Icons.Default.AccountBalance,
                        iconTint = tabColor,
                        isFiltered = selectedCount > 0,
                        onClear = {
                            tempTabFilter = tempTabFilter.copy(
                                selectedItemIds = emptySet(),
                                selectedGroupNames = emptySet()
                            )
                        },
                        onClick = { showItemPickerModal = true }
                    )

                    // 2. Amount Range (Min / Max with Calculator)
                    UnifiedFilterSection(
                        title = if (isBangla) "পরিমাণ রেঞ্জ (৳)" else "Amount Range (৳)",
                        icon = Icons.Default.Tune
                    ) {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Min Amount
                                    OutlinedTextField(
                                        value = minAmountText,
                                        onValueChange = {
                                            minAmountText = it.filter { c -> c.isDigit() || c == '.' }
                                            tempTabFilter = tempTabFilter.copy(minAmount = minAmountText.toDoubleOrNull())
                                        },
                                        label = { Text(if (isBangla) "সর্বনিম্ন (Min)" else "Min", fontSize = 11.sp) },
                                        placeholder = { Text("0.0", fontSize = 11.sp) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        shape = RoundedCornerShape(8.dp),
                                        trailingIcon = {
                                            if (minAmountText.isNotEmpty()) {
                                                IconButton(
                                                    onClick = {
                                                        minAmountText = ""
                                                        tempTabFilter = tempTabFilter.copy(minAmount = null)
                                                    },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(14.dp))
                                                }
                                            } else {
                                                IconButton(
                                                    onClick = { showCalculatorForMin = true },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(Icons.Default.Calculate, contentDescription = "Calc", tint = tabColor, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    )

                                    // Max Amount
                                    OutlinedTextField(
                                        value = maxAmountText,
                                        onValueChange = {
                                            maxAmountText = it.filter { c -> c.isDigit() || c == '.' }
                                            tempTabFilter = tempTabFilter.copy(maxAmount = maxAmountText.toDoubleOrNull())
                                        },
                                        label = { Text(if (isBangla) "সর্বোচ্চ (Max)" else "Max", fontSize = 11.sp) },
                                        placeholder = { Text("∞", fontSize = 11.sp) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        shape = RoundedCornerShape(8.dp),
                                        trailingIcon = {
                                            if (maxAmountText.isNotEmpty()) {
                                                IconButton(
                                                    onClick = {
                                                        maxAmountText = ""
                                                        tempTabFilter = tempTabFilter.copy(maxAmount = null)
                                                    },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(14.dp))
                                                }
                                            } else {
                                                IconButton(
                                                    onClick = { showCalculatorForMax = true },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(Icons.Default.Calculate, contentDescription = "Calc", tint = tabColor, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    // 3. Budget / Target Status Conditions
                    UnifiedFilterSection(
                        title = if (isBangla) "বাজেট ও লক্ষ্য অবস্থা" else "Budget & Target Status",
                        icon = Icons.Default.FilterList
                    ) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val isAllBudget = !tempTabFilter.onlyWithBudgetOrTarget && !tempTabFilter.onlyWithoutBudgetOrTarget
                            FilterChip(
                                selected = isAllBudget,
                                onClick = {
                                    tempTabFilter = tempTabFilter.copy(
                                        onlyWithBudgetOrTarget = false,
                                        onlyWithoutBudgetOrTarget = false
                                    )
                                },
                                label = { Text(if (isBangla) "সকল" else "All", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                shape = RoundedCornerShape(8.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = tabColor,
                                    selectedLabelColor = Color.White
                                )
                            )
                            FilterChip(
                                selected = tempTabFilter.onlyWithBudgetOrTarget,
                                onClick = {
                                    val now = !tempTabFilter.onlyWithBudgetOrTarget
                                    tempTabFilter = tempTabFilter.copy(
                                        onlyWithBudgetOrTarget = now,
                                        onlyWithoutBudgetOrTarget = if (now) false else tempTabFilter.onlyWithoutBudgetOrTarget
                                    )
                                },
                                label = { Text(if (isBangla) "বাজেট/লক্ষ্য সেট করা" else "Budgeted / Target Set", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                shape = RoundedCornerShape(8.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = tabColor,
                                    selectedLabelColor = Color.White
                                )
                            )
                            FilterChip(
                                selected = tempTabFilter.onlyWithoutBudgetOrTarget,
                                onClick = {
                                    val now = !tempTabFilter.onlyWithoutBudgetOrTarget
                                    tempTabFilter = tempTabFilter.copy(
                                        onlyWithoutBudgetOrTarget = now,
                                        onlyWithBudgetOrTarget = if (now) false else tempTabFilter.onlyWithBudgetOrTarget
                                    )
                                },
                                label = { Text(if (isBangla) "বাজেট নেই (০)" else "Unbudgeted (0)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                shape = RoundedCornerShape(8.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = tabColor,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    // 4. Activity & Performance Conditions
                    UnifiedFilterSection(
                        title = if (isBangla) "লেনদেন ও পারফরম্যান্স" else "Activity & Performance",
                        icon = Icons.Default.Tune
                    ) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            FilterChip(
                                selected = tempTabFilter.onlyWithActualActivity,
                                onClick = {
                                    val now = !tempTabFilter.onlyWithActualActivity
                                    tempTabFilter = tempTabFilter.copy(
                                        onlyWithActualActivity = now,
                                        onlyZeroActivity = if (now) false else tempTabFilter.onlyZeroActivity
                                    )
                                },
                                label = { Text(if (isBangla) "সক্রিয় লেনদেন রয়েছে" else "Has Active Transactions", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                shape = RoundedCornerShape(8.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SolidIncome,
                                    selectedLabelColor = Color.White
                                )
                            )
                            FilterChip(
                                selected = tempTabFilter.onlyZeroActivity,
                                onClick = {
                                    val now = !tempTabFilter.onlyZeroActivity
                                    tempTabFilter = tempTabFilter.copy(
                                        onlyZeroActivity = now,
                                        onlyWithActualActivity = if (now) false else tempTabFilter.onlyWithActualActivity
                                    )
                                },
                                label = { Text(if (isBangla) "কোনো লেনদেন নেই (০)" else "Zero Activity (0)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                shape = RoundedCornerShape(8.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.outline,
                                    selectedLabelColor = Color.White
                                )
                            )

                            if (tabIndex == 1 || tabIndex == 2) {
                                FilterChip(
                                    selected = tempTabFilter.onlyOverBudget,
                                    onClick = {
                                        val now = !tempTabFilter.onlyOverBudget
                                        tempTabFilter = tempTabFilter.copy(
                                            onlyOverBudget = now,
                                            onlyUnderBudgetRemaining = if (now) false else tempTabFilter.onlyUnderBudgetRemaining
                                        )
                                    },
                                    label = { Text(if (isBangla) "বাজেট অতিক্রান্ত (Over)" else "Over Budget / Exceeded", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SolidExpense,
                                        selectedLabelColor = Color.White
                                    )
                                )
                                FilterChip(
                                    selected = tempTabFilter.onlyUnderBudgetRemaining,
                                    onClick = {
                                        val now = !tempTabFilter.onlyUnderBudgetRemaining
                                        tempTabFilter = tempTabFilter.copy(
                                            onlyUnderBudgetRemaining = now,
                                            onlyOverBudget = if (now) false else tempTabFilter.onlyOverBudget
                                        )
                                    },
                                    label = { Text(if (isBangla) "অবশিষ্ট ব্যালেন্স আছে" else "Under Budget / Remaining", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SolidIncome,
                                        selectedLabelColor = Color.White
                                    )
                                )
                                FilterChip(
                                    selected = tempTabFilter.onlyTargetAchieved,
                                    onClick = {
                                        val now = !tempTabFilter.onlyTargetAchieved
                                        tempTabFilter = tempTabFilter.copy(
                                            onlyTargetAchieved = now,
                                            onlyTargetPending = if (now) false else tempTabFilter.onlyTargetPending
                                        )
                                    },
                                    label = { Text(if (isBangla) "লক্ষ্য অর্জিত" else "Target Achieved", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SolidIncome,
                                        selectedLabelColor = Color.White
                                    )
                                )
                                FilterChip(
                                    selected = tempTabFilter.onlyTargetPending,
                                    onClick = {
                                        val now = !tempTabFilter.onlyTargetPending
                                        tempTabFilter = tempTabFilter.copy(
                                            onlyTargetPending = now,
                                            onlyTargetAchieved = if (now) false else tempTabFilter.onlyTargetAchieved
                                        )
                                    },
                                    label = { Text(if (isBangla) "লক্ষ্য বাকি আছে" else "Target Pending", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = AmberGold,
                                        selectedLabelColor = Color.White
                                    )
                                )
                                FilterChip(
                                    selected = tempTabFilter.onlyWithSuggestions,
                                    onClick = {
                                        tempTabFilter = tempTabFilter.copy(onlyWithSuggestions = !tempTabFilter.onlyWithSuggestions)
                                    },
                                    label = { Text(if (isBangla) "স্মার্ট পরামর্শ রয়েছে" else "Has Smart Suggestions", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SolidPrimary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }

                            if (tabIndex == 3 || tabIndex == 4) {
                                FilterChip(
                                    selected = tempTabFilter.onlyPositiveBalance,
                                    onClick = {
                                        val now = !tempTabFilter.onlyPositiveBalance
                                        tempTabFilter = tempTabFilter.copy(
                                            onlyPositiveBalance = now,
                                            onlyZeroBalance = if (now) false else tempTabFilter.onlyZeroBalance,
                                            onlyNegativeBalance = if (now) false else tempTabFilter.onlyNegativeBalance
                                        )
                                    },
                                    label = { Text(if (isBangla) "পজিটিভ ব্যালেন্স (>০)" else "Positive Balance (>0)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SolidIncome,
                                        selectedLabelColor = Color.White
                                    )
                                )
                                FilterChip(
                                    selected = tempTabFilter.onlyZeroBalance,
                                    onClick = {
                                        val now = !tempTabFilter.onlyZeroBalance
                                        tempTabFilter = tempTabFilter.copy(
                                            onlyZeroBalance = now,
                                            onlyPositiveBalance = if (now) false else tempTabFilter.onlyPositiveBalance,
                                            onlyNegativeBalance = if (now) false else tempTabFilter.onlyNegativeBalance
                                        )
                                    },
                                    label = { Text(if (isBangla) "জিরো ব্যালেন্স (=০)" else "Zero Balance (=0)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.outline,
                                        selectedLabelColor = Color.White
                                    )
                                )
                                FilterChip(
                                    selected = tempTabFilter.onlyNegativeBalance,
                                    onClick = {
                                        val now = !tempTabFilter.onlyNegativeBalance
                                        tempTabFilter = tempTabFilter.copy(
                                            onlyNegativeBalance = now,
                                            onlyPositiveBalance = if (now) false else tempTabFilter.onlyPositiveBalance,
                                            onlyZeroBalance = if (now) false else tempTabFilter.onlyZeroBalance
                                        )
                                    },
                                    label = { Text(if (isBangla) "নেগেটিভ ব্যালেন্স (<০)" else "Negative Balance (<0)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SolidExpense,
                                        selectedLabelColor = Color.White
                                    )
                                )
                                if (tabIndex == 4) {
                                    FilterChip(
                                        selected = tempTabFilter.onlyOutstandingDebt,
                                        onClick = {
                                            val now = !tempTabFilter.onlyOutstandingDebt
                                            tempTabFilter = tempTabFilter.copy(
                                                onlyOutstandingDebt = now,
                                                onlyClearedDebt = if (now) false else tempTabFilter.onlyClearedDebt
                                            )
                                        },
                                        label = { Text(if (isBangla) "বকেয়া দেনা / ঋণ" else "Outstanding Debt", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = SolidExpense,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                    FilterChip(
                                        selected = tempTabFilter.onlyClearedDebt,
                                        onClick = {
                                            val now = !tempTabFilter.onlyClearedDebt
                                            tempTabFilter = tempTabFilter.copy(
                                                onlyClearedDebt = now,
                                                onlyOutstandingDebt = if (now) false else tempTabFilter.onlyOutstandingDebt
                                            )
                                        },
                                        label = { Text(if (isBangla) "পরিশোধিত ঋণ" else "Cleared Debt", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = SolidIncome,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // 5. Display Settings (Toggle Switches)
                    UnifiedFilterSection(
                        title = if (isBangla) "প্রদর্শন সেটিংস" else "Display Settings",
                        icon = Icons.Default.Tune
                    ) {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                BudgetSettingToggleRow(
                                    title = if (isBangla) "শূন্য বাজেট ও লেনদেন বাদ দিন" else "Exclude Zero Amounts (0 Budget & 0 Activity)",
                                    checked = tempTabFilter.excludeZeroAmounts,
                                    onCheckedChange = { tempTabFilter = tempTabFilter.copy(excludeZeroAmounts = it) }
                                )
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                BudgetSettingToggleRow(
                                    title = if (isBangla) "খালি গ্রুপ লুকান" else "Hide Empty Groups",
                                    checked = tempTabFilter.hideEmptyGroups,
                                    onCheckedChange = { tempTabFilter = tempTabFilter.copy(hideEmptyGroups = it) }
                                )
                            }
                        }
                    }
                }
            }

            // Footer Actions
            UnifiedFilterFooter(
                languageMode = languageMode,
                onReset = {
                    if (tabIndex == 0) {
                        tempDashboardFilter = BudgetMakerDashboardFilter()
                    } else {
                        tempTabFilter = BudgetMakerTabFilter()
                        minAmountText = ""
                        maxAmountText = ""
                    }
                },
                onDismiss = onDismiss,
                onApply = {
                    if (tabIndex == 0) {
                        onApplyDashboardFilter(tempDashboardFilter)
                    } else {
                        onApplyTabFilter(tempTabFilter)
                    }
                    onDismiss()
                }
            )
        }
    }

    // Modal Item Picker for Categories or Accounts
    if (showItemPickerModal) {
        val targetCategories = remember(categories, tabIndex) {
            when (tabIndex) {
                1 -> categories.filter { it.type == CategoryType.EXPENSE && it.isActive }
                2 -> categories.filter { it.type == CategoryType.INCOME && it.isActive }
                else -> emptyList()
            }
        }
        val targetAccounts = remember(accounts, tabIndex) {
            when (tabIndex) {
                3 -> accounts.filter { it.type == AccountType.ASSET && it.isActive }
                4 -> accounts.filter { it.type == AccountType.LIABILITY && it.isActive }
                else -> emptyList()
            }
        }

        BudgetFilterItemSelectModal(
            isCategory = tabIndex in 1..2,
            categories = targetCategories,
            accounts = targetAccounts,
            selectedItemIds = tempTabFilter.selectedItemIds,
            selectedGroupNames = tempTabFilter.selectedGroupNames,
            languageMode = languageMode,
            accentColor = tabColor,
            onDismiss = { showItemPickerModal = false },
            onApply = { ids, groups ->
                tempTabFilter = tempTabFilter.copy(
                    selectedItemIds = ids,
                    selectedGroupNames = groups
                )
                showItemPickerModal = false
            }
        )
    }

    // Calculator Modals
    if (showCalculatorForMin) {
        PopupCalculatorDialog(
            initialValue = minAmountText.toDoubleOrNull() ?: 0.0,
            languageMode = languageMode,
            onDismiss = { showCalculatorForMin = false },
            onValueConfirmed = { calculatedAmount: Double ->
                minAmountText = if (calculatedAmount > 0) calculatedAmount.toInt().toString() else ""
                tempTabFilter = tempTabFilter.copy(minAmount = if (calculatedAmount > 0) calculatedAmount else null)
                showCalculatorForMin = false
            }
        )
    }

    if (showCalculatorForMax) {
        PopupCalculatorDialog(
            initialValue = maxAmountText.toDoubleOrNull() ?: 0.0,
            languageMode = languageMode,
            onDismiss = { showCalculatorForMax = false },
            onValueConfirmed = { calculatedAmount: Double ->
                maxAmountText = if (calculatedAmount > 0) calculatedAmount.toInt().toString() else ""
                tempTabFilter = tempTabFilter.copy(maxAmount = if (calculatedAmount > 0) calculatedAmount else null)
                showCalculatorForMax = false
            }
        )
    }
}

/**
 * Dropdown Selector Row matching Transaction Filter design.
 */
@Composable
private fun BudgetFilterDropdownRow(
    title: String,
    selectedValue: String,
    icon: ImageVector,
    iconTint: Color = SolidPrimary,
    isFiltered: Boolean = false,
    onClear: (() -> Unit)? = null,
    onClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (isFiltered) iconTint.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            border = BorderStroke(1.dp, if (isFiltered) iconTint.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedValue,
                    fontSize = 13.sp,
                    fontWeight = if (isFiltered) FontWeight.Bold else FontWeight.Normal,
                    color = if (isFiltered) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isFiltered && onClear != null) {
                        IconButton(
                            onClick = onClear,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Dropdown",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Toggle Switch Row for Budget Settings.
 */
@Composable
private fun BudgetSettingToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.size(width = 44.dp, height = 24.dp),
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

/**
 * Modern Multi-Select Modal for Categories or Accounts in Budget Maker.
 */
@Composable
private fun BudgetFilterItemSelectModal(
    isCategory: Boolean,
    categories: List<Category>,
    accounts: List<Account>,
    selectedItemIds: Set<Long>,
    selectedGroupNames: Set<String>,
    languageMode: LanguageMode,
    accentColor: Color,
    onDismiss: () -> Unit,
    onApply: (Set<Long>, Set<String>) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var tempIds by remember { mutableStateOf(selectedItemIds) }
    var tempGroups by remember { mutableStateOf(selectedGroupNames) }

    val allItemIds = remember(categories, accounts, isCategory) {
        if (isCategory) categories.map { it.id }.toSet() else accounts.map { it.id }.toSet()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) {
                        if (isCategory) "ক্যাটাগরি নির্বাচন" else "অ্যাকাউন্ট নির্বাচন"
                    } else {
                        if (isCategory) "Select Categories" else "Select Accounts"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                if (tempIds.isNotEmpty() || tempGroups.isNotEmpty()) {
                    Surface(
                        shape = CircleShape,
                        color = accentColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "${tempIds.size + tempGroups.size}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            if (languageMode == LanguageMode.BANGLA) "খুঁজুন..." else "Search...",
                            fontSize = 12.sp
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = tempIds.isEmpty() && tempGroups.isEmpty(),
                        onClick = {
                            tempIds = emptySet()
                            tempGroups = emptySet()
                        },
                        label = { Text(if (languageMode == LanguageMode.BANGLA) "সকল (রিসেট)" else "All (Reset)", fontSize = 11.sp) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = tempIds.size == allItemIds.size && allItemIds.isNotEmpty(),
                        onClick = {
                            tempIds = allItemIds
                            tempGroups = emptySet()
                        },
                        label = { Text(if (languageMode == LanguageMode.BANGLA) "সব নির্বাচন" else "Select All", fontSize = 11.sp) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (isCategory) {
                        val parentCats = categories.filter { it.parentId == null }
                        parentCats.forEach { parent ->
                            val subCats = categories.filter { it.parentId == parent.id }
                            val matchesSearch = searchQuery.isBlank() ||
                                    parent.nameEn.contains(searchQuery, ignoreCase = true) ||
                                    parent.nameBn.contains(searchQuery, ignoreCase = true) ||
                                    subCats.any { it.nameEn.contains(searchQuery, ignoreCase = true) || it.nameBn.contains(searchQuery, ignoreCase = true) }

                            if (matchesSearch) {
                                item {
                                    val isParentSelected = parent.id in tempIds || parent.nameEn in tempGroups
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isParentSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                val subIds = subCats.map { it.id }.toSet()
                                                val allFamily = subIds + parent.id
                                                tempIds = if (isParentSelected) {
                                                    tempIds - allFamily
                                                } else {
                                                    tempIds + allFamily
                                                }
                                                tempGroups = tempGroups - parent.nameEn
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                IconHelper.AppIcon(
                                                    iconName = parent.iconName,
                                                    contentDescription = null,
                                                    tint = accentColor,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text(parent.localizedName(languageMode), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            }
                                            if (isParentSelected) {
                                                Icon(Icons.Default.Check, contentDescription = null, tint = accentColor, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }

                                subCats.forEach { sub ->
                                    if (searchQuery.isBlank() || sub.nameEn.contains(searchQuery, ignoreCase = true) || sub.nameBn.contains(searchQuery, ignoreCase = true)) {
                                        item {
                                            val isSubSelected = sub.id in tempIds
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = if (isSubSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        tempIds = if (isSubSelected) tempIds - sub.id else tempIds + sub.id
                                                    }
                                                    .padding(start = 18.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        IconHelper.AppIcon(
                                                            iconName = sub.iconName,
                                                            contentDescription = null,
                                                            tint = MaterialTheme.colorScheme.outline,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(sub.localizedName(languageMode), fontSize = 12.sp)
                                                    }
                                                    if (isSubSelected) {
                                                        Icon(Icons.Default.Check, contentDescription = null, tint = accentColor, modifier = Modifier.size(14.dp))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        val parentAccs = accounts.filter { it.parentId == null }
                        parentAccs.forEach { parent ->
                            val subAccs = accounts.filter { it.parentId == parent.id }
                            val matchesSearch = searchQuery.isBlank() ||
                                    parent.nameEn.contains(searchQuery, ignoreCase = true) ||
                                    parent.nameBn.contains(searchQuery, ignoreCase = true) ||
                                    subAccs.any { it.nameEn.contains(searchQuery, ignoreCase = true) || it.nameBn.contains(searchQuery, ignoreCase = true) }

                            if (matchesSearch) {
                                item {
                                    val isParentSelected = parent.id in tempIds || parent.nameEn in tempGroups
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isParentSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                val subIds = subAccs.map { it.id }.toSet()
                                                val allFamily = subIds + parent.id
                                                tempIds = if (isParentSelected) {
                                                    tempIds - allFamily
                                                } else {
                                                    tempIds + allFamily
                                                }
                                                tempGroups = tempGroups - parent.nameEn
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                IconHelper.AppIcon(
                                                    iconName = parent.iconName,
                                                    contentDescription = null,
                                                    tint = accentColor,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text(parent.localizedName(languageMode), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            }
                                            if (isParentSelected) {
                                                Icon(Icons.Default.Check, contentDescription = null, tint = accentColor, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }

                                subAccs.forEach { sub ->
                                    if (searchQuery.isBlank() || sub.nameEn.contains(searchQuery, ignoreCase = true) || sub.nameBn.contains(searchQuery, ignoreCase = true)) {
                                        item {
                                            val isSubSelected = sub.id in tempIds
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = if (isSubSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        tempIds = if (isSubSelected) tempIds - sub.id else tempIds + sub.id
                                                    }
                                                    .padding(start = 18.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        IconHelper.AppIcon(
                                                            iconName = sub.iconName,
                                                            contentDescription = null,
                                                            tint = MaterialTheme.colorScheme.outline,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(sub.localizedName(languageMode), fontSize = 12.sp)
                                                    }
                                                    if (isSubSelected) {
                                                        Icon(Icons.Default.Check, contentDescription = null, tint = accentColor, modifier = Modifier.size(14.dp))
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
        },
        confirmButton = {
            Button(
                onClick = { onApply(tempIds, tempGroups) },
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Text(if (languageMode == LanguageMode.BANGLA) "প্রয়োগ করুন" else "Done")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(if (languageMode == LanguageMode.BANGLA) "বাতিল" else "Cancel") }
        }
    )
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
                            text = "$tabName (${tabFilter.activeCount})",
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
