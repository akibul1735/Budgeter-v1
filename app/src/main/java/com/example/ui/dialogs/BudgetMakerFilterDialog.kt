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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.example.ui.screens.BudgetSortOption
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidPrimary
import com.example.util.DateUtils
import com.example.util.IconHelper

private val AmberGold = Color(0xFFD97706)

/**
 * Filter state specifically crafted for Budget Maker tabs.
 * Supports multi-field selection with multiple values per field.
 */
data class BudgetMakerTabFilter(
    val selectedItemIds: Set<Long> = emptySet(),
    val selectedGroupNames: Set<String> = emptySet(),
    val minAmount: Double? = null,
    val maxAmount: Double? = null,

    // Budget & Target Status (Multiple values can be selected)
    val onlyWithBudgetOrTarget: Boolean = false,
    val onlyWithoutBudgetOrTarget: Boolean = false,
    val onlyOverBudget: Boolean = false,
    val onlyUnderBudgetRemaining: Boolean = false,
    val onlyTargetAchieved: Boolean = false,
    val onlyTargetPending: Boolean = false,

    // Activity & Performance (Multiple values can be selected)
    val onlyWithActualActivity: Boolean = false,
    val onlyZeroActivity: Boolean = false,
    val onlyActive3Months: Boolean = false,
    val onlyFrequentlyActive: Boolean = false,
    val onlyFrequentlyBudgeted: Boolean = false,
    val onlyWithSuggestions: Boolean = false,

    // Asset & Liability Specific Conditions
    val onlyPositiveBalance: Boolean = false,
    val onlyZeroBalance: Boolean = false,
    val onlyNegativeBalance: Boolean = false,
    val onlyOutstandingDebt: Boolean = false,
    val onlyClearedDebt: Boolean = false,

    // Display & Cleanliness Toggles
    val excludeZeroAmounts: Boolean = false,
    val hideEmptyGroups: Boolean = false,
    val amountFocus: Boolean = false
) {
    val isActive: Boolean
        get() = selectedItemIds.isNotEmpty() ||
                selectedGroupNames.isNotEmpty() ||
                (minAmount != null && minAmount > 0) ||
                (maxAmount != null && maxAmount > 0) ||
                onlyWithBudgetOrTarget ||
                onlyWithoutBudgetOrTarget ||
                onlyOverBudget ||
                onlyUnderBudgetRemaining ||
                onlyTargetAchieved ||
                onlyTargetPending ||
                onlyWithActualActivity ||
                onlyZeroActivity ||
                onlyActive3Months ||
                onlyFrequentlyActive ||
                onlyFrequentlyBudgeted ||
                onlyWithSuggestions ||
                onlyPositiveBalance ||
                onlyZeroBalance ||
                onlyNegativeBalance ||
                onlyOutstandingDebt ||
                onlyClearedDebt ||
                excludeZeroAmounts ||
                hideEmptyGroups ||
                amountFocus

    val activeCount: Int
        get() {
            var count = 0
            if (selectedItemIds.isNotEmpty() || selectedGroupNames.isNotEmpty()) {
                count += (selectedItemIds.size + selectedGroupNames.size)
            }
            if ((minAmount != null && minAmount > 0) || (maxAmount != null && maxAmount > 0)) count++
            if (onlyWithBudgetOrTarget) count++
            if (onlyWithoutBudgetOrTarget) count++
            if (onlyOverBudget) count++
            if (onlyUnderBudgetRemaining) count++
            if (onlyTargetAchieved) count++
            if (onlyTargetPending) count++
            if (onlyWithActualActivity) count++
            if (onlyZeroActivity) count++
            if (onlyActive3Months) count++
            if (onlyFrequentlyActive) count++
            if (onlyFrequentlyBudgeted) count++
            if (onlyWithSuggestions) count++
            if (onlyPositiveBalance) count++
            if (onlyZeroBalance) count++
            if (onlyNegativeBalance) count++
            if (onlyOutstandingDebt) count++
            if (onlyClearedDebt) count++
            if (excludeZeroAmounts) count++
            if (hideEmptyGroups) count++
            if (amountFocus) count++
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
 * Supports multi-field selection with multiple values per field,
 * quick preview chips with direct (x) removal, calculator popup, and integrated sort options.
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
    currentSort: BudgetSortOption = BudgetSortOption.DEFAULT,
    onSortChange: ((BudgetSortOption) -> Unit)? = null,
    onDismiss: () -> Unit,
    onApplyTabFilter: (BudgetMakerTabFilter) -> Unit,
    onApplyDashboardFilter: (BudgetMakerDashboardFilter) -> Unit
) {
    val context = LocalContext.current
    val isBangla = languageMode == LanguageMode.BANGLA
    val monthNameStr = DateUtils.formatMonthYear(selectedYear, selectedMonth, languageMode)

    var tempTabFilter by remember { mutableStateOf(currentTabFilter) }
    var tempDashboardFilter by remember { mutableStateOf(dashboardFilter) }
    var tempSort by remember { mutableStateOf(currentSort) }

    var minAmountText by remember {
        mutableStateOf(
            if (currentTabFilter.minAmount != null && currentTabFilter.minAmount > 0)
                currentTabFilter.minAmount.toInt().toString()
            else ""
        )
    }
    var maxAmountText by remember {
        mutableStateOf(
            if (currentTabFilter.maxAmount != null && currentTabFilter.maxAmount > 0)
                currentTabFilter.maxAmount.toInt().toString()
            else ""
        )
    }

    var showCalculatorForMin by remember { mutableStateOf(false) }
    var showCalculatorForMax by remember { mutableStateOf(false) }
    var showItemPickerModal by remember { mutableStateOf(false) }
    var showSortDropdown by remember { mutableStateOf(false) }

    // Tab attributes
    val (tabTitle, tabSubtitle, tabIcon, tabColor) = when (tabIndex) {
        0 -> Quadruple(
            if (isBangla) "ড্যাশবোর্ড ফিল্টার" else "Dashboard Filter",
            if (isBangla) "$monthNameStr • ওভারভিউ পরিধি" else "$monthNameStr • Overview scope",
            Icons.Default.Dashboard,
            MaterialTheme.colorScheme.primary
        )
        1 -> Quadruple(
            if (isBangla) "ব্যয় বাজেট ফিল্টার" else "Expense Budget Filter",
            if (isBangla) "$monthNameStr • ব্যয় ক্যাটাগরি, সীমা ও পারফরম্যান্স" else "$monthNameStr • Filter expense categories, limits & activity",
            Icons.Default.TrendingDown,
            SolidExpense
        )
        2 -> Quadruple(
            if (isBangla) "আয় লক্ষ্য ফিল্টার" else "Income Target Filter",
            if (isBangla) "$monthNameStr • আয় ক্যাটাগরি, লক্ষ্য ও অগ্রগতি" else "$monthNameStr • Filter income categories, targets & progress",
            Icons.Default.TrendingUp,
            SolidIncome
        )
        3 -> Quadruple(
            if (isBangla) "সম্পদ অ্যাকাউন্ট ফিল্টার" else "Asset Accounts Filter",
            if (isBangla) "$monthNameStr • সম্পদ অ্যাকাউন্ট, ব্যালেন্স ও স্থিতি" else "$monthNameStr • Filter asset accounts & balances",
            Icons.Default.AccountBalanceWallet,
            SolidPrimary
        )
        4 -> Quadruple(
            if (isBangla) "দায় ও ঋণ ফিল্টার" else "Liabilities & Debt Filter",
            if (isBangla) "$monthNameStr • ঋণ ও দেনা অ্যাকাউন্ট ফিল্টার" else "$monthNameStr • Filter debt & liability accounts",
            Icons.Default.CreditCard,
            AmberGold
        )
        else -> Quadruple("Budget Filter", monthNameStr, Icons.Default.FilterList, MaterialTheme.colorScheme.primary)
    }

    val activeCount = if (tabIndex == 0) tempDashboardFilter.activeCount else tempTabFilter.activeCount

    val sectionItemType = when (tabIndex) {
        1 -> "EXPENSE"
        2 -> "INCOME"
        3 -> "ASSET"
        4 -> "LIABILITY"
        else -> "EXPENSE"
    }

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
                        tempSort = BudgetSortOption.DEFAULT
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
                    // BM DASHBOARD FILTER (Tab 0)
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
                    // TAB-SPECIFIC MULTI-FIELD, MULTI-VALUE FILTERS (Tabs 1..4)

                    // ── 1. Field 1: Specific Categories / Accounts / Groups (Multi-Select) ──
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

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        BudgetFilterDropdownRow(
                            title = if (tabIndex in 1..2) (if (isBangla) "ক্যাটাগরি বা গ্রুপ নির্বাচন" else "Categories & Groups") else (if (isBangla) "অ্যাকাউন্ট বা গ্রুপ নির্বাচন" else "Accounts & Groups"),
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

                        // Removable selected chips flow row
                        if (selectedCount > 0) {
                            FlowRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 2.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // Groups chips
                                tempTabFilter.selectedGroupNames.forEach { groupName ->
                                    RemovableItemChip(
                                        label = "📁 $groupName",
                                        accentColor = tabColor,
                                        onRemove = {
                                            tempTabFilter = tempTabFilter.copy(
                                                selectedGroupNames = tempTabFilter.selectedGroupNames - groupName
                                            )
                                        }
                                    )
                                }

                                // Individual item chips
                                tempTabFilter.selectedItemIds.forEach { id ->
                                    val name = if (tabIndex in 1..2) {
                                        categories.firstOrNull { it.id == id }?.localizedName(languageMode) ?: "#$id"
                                    } else {
                                        accounts.firstOrNull { it.id == id }?.localizedName(languageMode) ?: "#$id"
                                    }
                                    RemovableItemChip(
                                        label = name,
                                        accentColor = tabColor,
                                        onRemove = {
                                            tempTabFilter = tempTabFilter.copy(
                                                selectedItemIds = tempTabFilter.selectedItemIds - id
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // ── 2. Field 2: Amount Range (৳) with Calculator & Quick Presets ──
                    UnifiedFilterSection(
                        title = if (isBangla) "পরিমাণ সীমা (৳)" else "Amount Range (৳)",
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

                                // Quick Amount Presets
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    val presets = listOf(
                                        "> 0" to 0.01,
                                        "> 1K" to 1000.0,
                                        "> 5K" to 5000.0,
                                        "> 10K" to 10000.0,
                                        "> 50K" to 50000.0
                                    )
                                    presets.forEach { (label, minVal) ->
                                        val isSelected = tempTabFilter.minAmount == minVal
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = {
                                                if (isSelected) {
                                                    minAmountText = ""
                                                    tempTabFilter = tempTabFilter.copy(minAmount = null)
                                                } else {
                                                    minAmountText = minVal.toInt().toString()
                                                    tempTabFilter = tempTabFilter.copy(minAmount = minVal)
                                                }
                                            },
                                            label = { Text(label, fontSize = 10.5.sp) },
                                            shape = RoundedCornerShape(6.dp),
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = tabColor.copy(alpha = 0.18f),
                                                selectedLabelColor = tabColor
                                            ),
                                            modifier = Modifier.height(28.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // ── 3. Field 3: Budget & Target Status (Multi-Select Supported) ──
                    UnifiedFilterSection(
                        title = if (isBangla) "বাজেট ও লক্ষ্য স্থিতি (একাধিক নির্বাচনযোগ্য)" else "Budget & Target Status (Multi-Select)",
                        icon = Icons.Default.FilterList
                    ) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val hasAnyStatus = tempTabFilter.onlyWithBudgetOrTarget ||
                                    tempTabFilter.onlyWithoutBudgetOrTarget ||
                                    tempTabFilter.onlyOverBudget ||
                                    tempTabFilter.onlyUnderBudgetRemaining ||
                                    tempTabFilter.onlyTargetAchieved ||
                                    tempTabFilter.onlyTargetPending

                            FilterChip(
                                selected = !hasAnyStatus,
                                onClick = {
                                    tempTabFilter = tempTabFilter.copy(
                                        onlyWithBudgetOrTarget = false,
                                        onlyWithoutBudgetOrTarget = false,
                                        onlyOverBudget = false,
                                        onlyUnderBudgetRemaining = false,
                                        onlyTargetAchieved = false,
                                        onlyTargetPending = false
                                    )
                                },
                                label = { Text(if (isBangla) "সকল স্থিতি" else "All Status", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                shape = RoundedCornerShape(8.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = tabColor,
                                    selectedLabelColor = Color.White
                                )
                            )

                            FilterChip(
                                selected = tempTabFilter.onlyWithBudgetOrTarget,
                                onClick = {
                                    tempTabFilter = tempTabFilter.copy(onlyWithBudgetOrTarget = !tempTabFilter.onlyWithBudgetOrTarget)
                                },
                                label = { Text(if (isBangla) "বাজেট/লক্ষ্য সেট করা" else "Budget / Target Set", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                shape = RoundedCornerShape(8.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = tabColor,
                                    selectedLabelColor = Color.White
                                )
                            )

                            FilterChip(
                                selected = tempTabFilter.onlyWithoutBudgetOrTarget,
                                onClick = {
                                    tempTabFilter = tempTabFilter.copy(onlyWithoutBudgetOrTarget = !tempTabFilter.onlyWithoutBudgetOrTarget)
                                },
                                label = { Text(if (isBangla) "বাজেট নেই (০)" else "Unbudgeted (0)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                shape = RoundedCornerShape(8.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = tabColor,
                                    selectedLabelColor = Color.White
                                )
                            )

                            if (tabIndex == 1 || tabIndex == 2) {
                                FilterChip(
                                    selected = tempTabFilter.onlyOverBudget,
                                    onClick = {
                                        tempTabFilter = tempTabFilter.copy(onlyOverBudget = !tempTabFilter.onlyOverBudget)
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
                                        tempTabFilter = tempTabFilter.copy(onlyUnderBudgetRemaining = !tempTabFilter.onlyUnderBudgetRemaining)
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
                                        tempTabFilter = tempTabFilter.copy(onlyTargetAchieved = !tempTabFilter.onlyTargetAchieved)
                                    },
                                    label = { Text(if (isBangla) "লক্ষ্য অর্জিত (Met)" else "Target Achieved", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SolidIncome,
                                        selectedLabelColor = Color.White
                                    )
                                )

                                FilterChip(
                                    selected = tempTabFilter.onlyTargetPending,
                                    onClick = {
                                        tempTabFilter = tempTabFilter.copy(onlyTargetPending = !tempTabFilter.onlyTargetPending)
                                    },
                                    label = { Text(if (isBangla) "লক্ষ্য বাকি (Pending)" else "Target Pending", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = AmberGold,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }

                    // ── 4. Field 4: Activity & History Conditions (Multi-Select Supported) ──
                    UnifiedFilterSection(
                        title = if (isBangla) "লেনদেন ও পারফরম্যান্স (একাধিক নির্বাচনযোগ্য)" else "Activity & Performance (Multi-Select)",
                        icon = Icons.Default.Tune
                    ) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val hasAnyActivity = tempTabFilter.onlyWithActualActivity ||
                                    tempTabFilter.onlyZeroActivity ||
                                    tempTabFilter.onlyActive3Months ||
                                    tempTabFilter.onlyFrequentlyActive ||
                                    tempTabFilter.onlyFrequentlyBudgeted ||
                                    tempTabFilter.onlyWithSuggestions ||
                                    tempTabFilter.onlyPositiveBalance ||
                                    tempTabFilter.onlyZeroBalance ||
                                    tempTabFilter.onlyNegativeBalance ||
                                    tempTabFilter.onlyOutstandingDebt ||
                                    tempTabFilter.onlyClearedDebt

                            FilterChip(
                                selected = !hasAnyActivity,
                                onClick = {
                                    tempTabFilter = tempTabFilter.copy(
                                        onlyWithActualActivity = false,
                                        onlyZeroActivity = false,
                                        onlyActive3Months = false,
                                        onlyFrequentlyActive = false,
                                        onlyFrequentlyBudgeted = false,
                                        onlyWithSuggestions = false,
                                        onlyPositiveBalance = false,
                                        onlyZeroBalance = false,
                                        onlyNegativeBalance = false,
                                        onlyOutstandingDebt = false,
                                        onlyClearedDebt = false
                                    )
                                },
                                label = { Text(if (isBangla) "সকল কার্যকলাপ" else "All Activity", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                shape = RoundedCornerShape(8.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = tabColor,
                                    selectedLabelColor = Color.White
                                )
                            )

                            FilterChip(
                                selected = tempTabFilter.onlyWithActualActivity,
                                onClick = {
                                    tempTabFilter = tempTabFilter.copy(onlyWithActualActivity = !tempTabFilter.onlyWithActualActivity)
                                },
                                label = { Text(if (isBangla) "সক্রিয় লেনদেন রয়েছে" else "Has Active Activity", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                shape = RoundedCornerShape(8.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SolidIncome,
                                    selectedLabelColor = Color.White
                                )
                            )

                            FilterChip(
                                selected = tempTabFilter.onlyZeroActivity,
                                onClick = {
                                    tempTabFilter = tempTabFilter.copy(onlyZeroActivity = !tempTabFilter.onlyZeroActivity)
                                },
                                label = { Text(if (isBangla) "কোনো লেনদেন নেই (০)" else "Zero Activity (0)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                shape = RoundedCornerShape(8.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.outline,
                                    selectedLabelColor = Color.White
                                )
                            )

                            FilterChip(
                                selected = tempTabFilter.onlyActive3Months,
                                onClick = {
                                    tempTabFilter = tempTabFilter.copy(onlyActive3Months = !tempTabFilter.onlyActive3Months)
                                },
                                label = { Text(if (isBangla) "সক্রিয় (গত ৩ মাস)" else "Active (Last 3 Months)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                shape = RoundedCornerShape(8.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = tabColor,
                                    selectedLabelColor = Color.White
                                )
                            )

                            FilterChip(
                                selected = tempTabFilter.onlyFrequentlyActive,
                                onClick = {
                                    tempTabFilter = tempTabFilter.copy(onlyFrequentlyActive = !tempTabFilter.onlyFrequentlyActive)
                                },
                                label = { Text(if (isBangla) "নিয়মিত লেনদেন" else "Frequently Active", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                shape = RoundedCornerShape(8.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = tabColor,
                                    selectedLabelColor = Color.White
                                )
                            )

                            FilterChip(
                                selected = tempTabFilter.onlyFrequentlyBudgeted,
                                onClick = {
                                    tempTabFilter = tempTabFilter.copy(onlyFrequentlyBudgeted = !tempTabFilter.onlyFrequentlyBudgeted)
                                },
                                label = { Text(if (isBangla) "নিয়মিত বাজেটকৃত" else "Frequently Budgeted", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                shape = RoundedCornerShape(8.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = tabColor,
                                    selectedLabelColor = Color.White
                                )
                            )

                            if (tabIndex == 1 || tabIndex == 2) {
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
                                        tempTabFilter = tempTabFilter.copy(onlyPositiveBalance = !tempTabFilter.onlyPositiveBalance)
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
                                        tempTabFilter = tempTabFilter.copy(onlyZeroBalance = !tempTabFilter.onlyZeroBalance)
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
                                        tempTabFilter = tempTabFilter.copy(onlyNegativeBalance = !tempTabFilter.onlyNegativeBalance)
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
                                            tempTabFilter = tempTabFilter.copy(onlyOutstandingDebt = !tempTabFilter.onlyOutstandingDebt)
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
                                            tempTabFilter = tempTabFilter.copy(onlyClearedDebt = !tempTabFilter.onlyClearedDebt)
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

                    // ── 5. Field 5: View & Cleanliness Settings ──
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
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                BudgetSettingToggleRow(
                                    title = if (isBangla) "অ্যামাউন্ট ফোকাস (প্রগ্রেস বার লুকান)" else "Amount focus (hides progress bar)",
                                    checked = tempTabFilter.amountFocus,
                                    onCheckedChange = { tempTabFilter = tempTabFilter.copy(amountFocus = it) }
                                )
                            }
                        }
                    }

                    // ── 6. Field 6: Sort By Options inside Dialog ──
                    if (onSortChange != null) {
                        UnifiedFilterSection(
                            title = if (isBangla) "সাজানোর ক্রম (Sort By)" else "Sort By",
                            icon = Icons.Default.Sort
                        ) {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        BudgetSortOption.values().forEach { sortOpt ->
                                            val isSelected = tempSort == sortOpt
                                            FilterChip(
                                                selected = isSelected,
                                                onClick = {
                                                    tempSort = sortOpt
                                                },
                                                label = {
                                                    Text(
                                                        text = sortOpt.getTitle(sectionItemType, languageMode),
                                                        fontSize = 11.sp,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                    )
                                                },
                                                shape = RoundedCornerShape(8.dp),
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = tabColor,
                                                    selectedLabelColor = Color.White
                                                )
                                            )
                                        }
                                    }
                                }
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
                        tempSort = BudgetSortOption.DEFAULT
                    }
                },
                onDismiss = onDismiss,
                onApply = {
                    if (tabIndex == 0) {
                        onApplyDashboardFilter(tempDashboardFilter)
                    } else {
                        onSortChange?.invoke(tempSort)
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
 * Compact removable item chip for selected category/account.
 */
@Composable
private fun RemovableItemChip(
    label: String,
    accentColor: Color,
    onRemove: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = accentColor.copy(alpha = 0.12f),
        border = BorderStroke(0.8.dp, accentColor.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(start = 8.dp, end = 4.dp, top = 3.dp, bottom = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(18.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove",
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(12.dp)
                )
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
                        if (isCategory) "ক্যাটাগরি বা গ্রুপ নির্বাচন" else "অ্যাকাউন্ট বা গ্রুপ নির্বাচন"
                    } else {
                        if (isCategory) "Select Categories & Groups" else "Select Accounts & Groups"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.5.sp
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
                    .height(390.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            if (languageMode == LanguageMode.BANGLA) "খুঁজুন (নাম বা গ্রুপ)..." else "Search name or group...",
                            fontSize = 12.sp
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(14.dp))
                            }
                        } else {
                            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    },
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
                    text = if (isBangla) "সক্রিয় ফিল্টার প্রয়োগ করা হয়েছে" else "Active Filters Applied",
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
