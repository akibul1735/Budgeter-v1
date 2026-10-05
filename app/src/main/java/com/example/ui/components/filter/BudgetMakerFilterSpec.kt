package com.example.ui.components.filter

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Account
import com.example.data.model.AccountType
import com.example.data.model.Category
import com.example.data.model.CategoryType
import com.example.data.model.LanguageMode
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidPrimary
import com.example.util.DateUtils

private val AmberGold = Color(0xFFD97706)

/**
 * Filter state specifically crafted for Budget Maker tabs.
 * Maintained for direct interoperability with existing calculations.
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
 * Converts BudgetMakerTabFilter to the unified FilterState map.
 */
fun BudgetMakerTabFilter.toFilterState(tabIndex: Int = 1): FilterState {
    val map = mutableMapOf<String, FilterValue>()

    if (selectedItemIds.isNotEmpty()) {
        map["items"] = FilterValue.Select(selectedItemIds.map { it.toString() }.toSet())
    }

    if (minAmount != null || maxAmount != null) {
        map["amount_range"] = FilterValue.Range(min = minAmount, max = maxAmount)
    }

    val activeConditions = mutableSetOf<String>()
    if (onlyOverBudget) activeConditions.add("onlyOverBudget")
    if (onlyUnderBudgetRemaining) activeConditions.add("onlyUnderBudgetRemaining")
    if (onlyTargetAchieved) activeConditions.add("onlyTargetAchieved")
    if (onlyTargetPending) activeConditions.add("onlyTargetPending")
    if (onlyWithActualActivity) activeConditions.add("onlyWithActualActivity")
    if (onlyZeroActivity) activeConditions.add("onlyZeroActivity")
    if (onlyWithBudgetOrTarget) activeConditions.add("onlyWithBudgetOrTarget")
    if (onlyWithoutBudgetOrTarget) activeConditions.add("onlyWithoutBudgetOrTarget")
    if (onlyWithSuggestions) activeConditions.add("onlyWithSuggestions")
    if (onlyPositiveBalance) activeConditions.add("onlyPositiveBalance")
    if (onlyZeroBalance) activeConditions.add("onlyZeroBalance")
    if (onlyNegativeBalance) activeConditions.add("onlyNegativeBalance")
    if (onlyOutstandingDebt) activeConditions.add("onlyOutstandingDebt")
    if (onlyClearedDebt) activeConditions.add("onlyClearedDebt")

    if (activeConditions.isNotEmpty()) {
        map["conditions"] = FilterValue.ToggleGroup(activeConditions)
    }

    val displayToggles = mutableSetOf<String>()
    if (excludeZeroAmounts) displayToggles.add("excludeZeroAmounts")
    if (hideEmptyGroups) displayToggles.add("hideEmptyGroups")
    if (displayToggles.isNotEmpty()) {
        map["display_options"] = FilterValue.ToggleGroup(displayToggles)
    }

    return map
}

/**
 * Converts unified FilterState map back to BudgetMakerTabFilter.
 */
fun FilterState.toBudgetMakerTabFilter(): BudgetMakerTabFilter {
    val selectedIds = (this["items"] as? FilterValue.Select)?.selectedIds?.mapNotNull { it.toLongOrNull() }?.toSet() ?: emptySet()
    val amountRange = this["amount_range"] as? FilterValue.Range
    val conditions = (this["conditions"] as? FilterValue.ToggleGroup)?.activeIds ?: emptySet()
    val displayOptions = (this["display_options"] as? FilterValue.ToggleGroup)?.activeIds ?: emptySet()

    return BudgetMakerTabFilter(
        selectedItemIds = selectedIds,
        minAmount = amountRange?.min,
        maxAmount = amountRange?.max,
        onlyOverBudget = conditions.contains("onlyOverBudget"),
        onlyUnderBudgetRemaining = conditions.contains("onlyUnderBudgetRemaining"),
        onlyTargetAchieved = conditions.contains("onlyTargetAchieved"),
        onlyTargetPending = conditions.contains("onlyTargetPending"),
        onlyWithActualActivity = conditions.contains("onlyWithActualActivity"),
        onlyZeroActivity = conditions.contains("onlyZeroActivity"),
        onlyWithBudgetOrTarget = conditions.contains("onlyWithBudgetOrTarget"),
        onlyWithoutBudgetOrTarget = conditions.contains("onlyWithoutBudgetOrTarget"),
        onlyWithSuggestions = conditions.contains("onlyWithSuggestions"),
        onlyPositiveBalance = conditions.contains("onlyPositiveBalance"),
        onlyZeroBalance = conditions.contains("onlyZeroBalance"),
        onlyNegativeBalance = conditions.contains("onlyNegativeBalance"),
        onlyOutstandingDebt = conditions.contains("onlyOutstandingDebt"),
        onlyClearedDebt = conditions.contains("onlyClearedDebt"),
        excludeZeroAmounts = displayOptions.contains("excludeZeroAmounts"),
        hideEmptyGroups = displayOptions.contains("hideEmptyGroups")
    )
}

/**
 * Converts BudgetMakerDashboardFilter to the unified FilterState map.
 */
fun BudgetMakerDashboardFilter.toFilterState(): FilterState {
    val map = mutableMapOf<String, FilterValue>()
    val activeSections = mutableSetOf<String>()
    if (includeExpenses) activeSections.add("includeExpenses")
    if (includeIncomes) activeSections.add("includeIncomes")
    if (includeAssets) activeSections.add("includeAssets")
    if (includeLiabilities) activeSections.add("includeLiabilities")

    map["sections"] = FilterValue.ToggleGroup(activeSections)
    if (onlyNonZero) {
        map["onlyNonZero"] = FilterValue.BooleanVal(true)
    }
    return map
}

/**
 * Converts unified FilterState map back to BudgetMakerDashboardFilter.
 */
fun FilterState.toBudgetMakerDashboardFilter(): BudgetMakerDashboardFilter {
    val sections = (this["sections"] as? FilterValue.ToggleGroup)?.activeIds
    val onlyNonZeroVal = (this["onlyNonZero"] as? FilterValue.BooleanVal)?.value ?: false

    return if (sections == null) {
        BudgetMakerDashboardFilter(onlyNonZero = onlyNonZeroVal)
    } else {
        BudgetMakerDashboardFilter(
            includeExpenses = sections.contains("includeExpenses"),
            includeIncomes = sections.contains("includeIncomes"),
            includeAssets = sections.contains("includeAssets"),
            includeLiabilities = sections.contains("includeLiabilities"),
            onlyNonZero = onlyNonZeroVal
        )
    }
}

/**
 * Factory for building FilterSpec for any Budget Maker tab.
 */
object BudgetMakerFilterSpecs {

    fun createExpenseSpec(categories: List<Category>, monthName: String): FilterSpec<Category> {
        val parentMap = categories.filter { it.parentId == null }.associateBy { it.id }
        val childCategories = categories.filter { it.parentId != null }
        val standaloneCategories = categories.filter { cat -> cat.parentId == null && childCategories.none { it.parentId == cat.id } }

        val selectItems = mutableListOf<SelectItemOption>()
        childCategories.forEach { child ->
            val parentName = parentMap[child.parentId]?.nameEn ?: "Other"
            val parentNameBn = parentMap[child.parentId]?.nameBn ?: "অন্যান্য"
            selectItems.add(
                SelectItemOption(
                    id = child.id.toString(),
                    titleEn = child.nameEn,
                    titleBn = child.nameBn.ifBlank { child.nameEn },
                    groupKey = parentName,
                    groupTitleEn = parentName,
                    groupTitleBn = parentNameBn
                )
            )
        }
        standaloneCategories.forEach { st ->
            selectItems.add(
                SelectItemOption(
                    id = st.id.toString(),
                    titleEn = st.nameEn,
                    titleBn = st.nameBn.ifBlank { st.nameEn },
                    groupKey = "General",
                    groupTitleEn = "General",
                    groupTitleBn = "সাধারণ"
                )
            )
        }

        return FilterSpec(
            key = "budget_maker_expense",
            titleEn = "Expense Budget Filter",
            titleBn = "ব্যয় বাজেট ফিল্টার",
            subtitleEn = "$monthName • Filter expense categories & limits",
            subtitleBn = "$monthName • ব্যয় ক্যাটাগরি ও সীমা ফিল্টার",
            icon = Icons.Default.TrendingDown,
            accentColor = SolidExpense,
            fields = listOf(
                FilterField.SelectField(
                    id = "items",
                    titleEn = "Expense Categories & Groups",
                    titleBn = "ব্যয় ক্যাটাগরি ও গ্রুপ",
                    icon = Icons.Default.Category,
                    isMultiSelect = true,
                    isHierarchical = true,
                    items = selectItems,
                    predicate = { cat, ids -> cat.id.toString() in ids }
                ),
                FilterField.RangeField(
                    id = "amount_range",
                    titleEn = "Budget Limit Range (৳)",
                    titleBn = "বাজেট সীমা রেঞ্জ (৳)",
                    icon = Icons.Default.Tune,
                    prefix = "৳"
                ),
                FilterField.ToggleGroupField(
                    id = "conditions",
                    titleEn = "Expense Status & Limit Conditions",
                    titleBn = "ব্যয় স্থিতি ও সীমা শর্ত",
                    icon = Icons.Default.Tune,
                    conditions = listOf(
                        FilterCondition(
                            id = "onlyOverBudget",
                            titleEn = "Over-Budget Only (Spent > Budget)",
                            titleBn = "বাজেট অতিক্রম করেছে (Spent > Budget)",
                            subtitleEn = "Show categories exceeding budget limit",
                            subtitleBn = "যেসব ক্যাটাগরিতে বাজেট সীমার বেশি খরচ হয়েছে",
                            icon = Icons.Default.PriorityHigh,
                            group = "budget_status"
                        ),
                        FilterCondition(
                            id = "onlyUnderBudgetRemaining",
                            titleEn = "Remaining Surplus Only (Budget > Spent)",
                            titleBn = "বাজেট অবশিষ্ট আছে (Budget > Spent)",
                            subtitleEn = "Categories with remaining available funds",
                            subtitleBn = "বাজেট বরাদ্দ আছে এবং টাকা বাকি আছে",
                            icon = Icons.Default.Savings,
                            group = "budget_status"
                        ),
                        FilterCondition(
                            id = "onlyWithActualActivity",
                            titleEn = "Has Actual Spending (> ৳0)",
                            titleBn = "এই মাসে খরচ হয়েছে (Spent > ৳০)",
                            subtitleEn = "Active categories with expenses this month",
                            subtitleBn = "চলতি মাসে লেনদেন বা খরচ হয়েছে",
                            icon = Icons.Default.MonetizationOn,
                            group = "activity_level"
                        ),
                        FilterCondition(
                            id = "onlyZeroActivity",
                            titleEn = "Zero Spending This Month (৳0)",
                            titleBn = "কোনো খরচ হয়নি (৳০ Spent)",
                            subtitleEn = "Categories with zero actual spending",
                            subtitleBn = "এই মাসে কোনো লেনদেন নেই",
                            icon = Icons.Default.RemoveCircleOutline,
                            group = "activity_level"
                        ),
                        FilterCondition(
                            id = "onlyWithBudgetOrTarget",
                            titleEn = "Budget Limit Configured (> ৳0)",
                            titleBn = "বাজেট সেট করা আছে (> ৳০)",
                            subtitleEn = "Categories with explicit budget limit",
                            subtitleBn = "বাজেট মেকারে পরিমাণ বরাদ্দ দেওয়া হয়েছে",
                            icon = Icons.Default.Tune,
                            group = "budget_presence"
                        ),
                        FilterCondition(
                            id = "onlyWithoutBudgetOrTarget",
                            titleEn = "Unbudgeted Only (৳0)",
                            titleBn = "বাজেট ছাড়া (৳০)",
                            subtitleEn = "Categories with no budget allocated",
                            subtitleBn = "যেসব ক্যাটাগরিতে বাজেট বরাদ্দ নেই",
                            icon = Icons.Default.RadioButtonUnchecked,
                            group = "budget_presence"
                        ),
                        FilterCondition(
                            id = "onlyWithSuggestions",
                            titleEn = "Smart Suggestions Available",
                            titleBn = "স্মার্ট পরামর্শ উপলব্ধ",
                            subtitleEn = "Items with AI/historical suggestion baselines",
                            subtitleBn = "অতীতের ইতিহাস ও গড়ের ভিত্তিতে প্রস্তাবনা আছে",
                            icon = Icons.Default.Lightbulb
                        )
                    )
                ),
                FilterField.ToggleGroupField(
                    id = "display_options",
                    titleEn = "Display & Cleanliness Options",
                    titleBn = "প্রদর্শন ও সাজানোর অপশন",
                    icon = Icons.Default.FilterAlt,
                    conditions = listOf(
                        FilterCondition(
                            id = "excludeZeroAmounts",
                            titleEn = "Exclude Inactive (0 Budget & 0 Spent)",
                            titleBn = "নিষ্ক্রিয় বাদ দিন (০ বাজেট ও ০ খরচ)",
                            subtitleEn = "Hide categories with no budget and zero spending",
                            subtitleBn = "যাতে কোনো বাজেট বা খরচ নেই সেগুলো আড়াল করুন",
                            icon = Icons.Default.FilterAlt
                        ),
                        FilterCondition(
                            id = "hideEmptyGroups",
                            titleEn = "Hide Empty Parent Groups",
                            titleBn = "খালি গ্রুপ লুকান",
                            subtitleEn = "Don't display header cards for empty groups",
                            subtitleBn = "যেসব গ্রুপের সব ক্যাটাগরি ফাঁকা সেগুলো দেখাবেন না",
                            icon = Icons.Default.Category
                        )
                    )
                )
            )
        )
    }

    fun createIncomeSpec(categories: List<Category>, monthName: String): FilterSpec<Category> {
        val parentMap = categories.filter { it.parentId == null }.associateBy { it.id }
        val childCategories = categories.filter { it.parentId != null }
        val standaloneCategories = categories.filter { cat -> cat.parentId == null && childCategories.none { it.parentId == cat.id } }

        val selectItems = mutableListOf<SelectItemOption>()
        childCategories.forEach { child ->
            val parentName = parentMap[child.parentId]?.nameEn ?: "Other"
            val parentNameBn = parentMap[child.parentId]?.nameBn ?: "অন্যান্য"
            selectItems.add(
                SelectItemOption(
                    id = child.id.toString(),
                    titleEn = child.nameEn,
                    titleBn = child.nameBn.ifBlank { child.nameEn },
                    groupKey = parentName,
                    groupTitleEn = parentName,
                    groupTitleBn = parentNameBn
                )
            )
        }
        standaloneCategories.forEach { st ->
            selectItems.add(
                SelectItemOption(
                    id = st.id.toString(),
                    titleEn = st.nameEn,
                    titleBn = st.nameBn.ifBlank { st.nameEn },
                    groupKey = "General",
                    groupTitleEn = "General",
                    groupTitleBn = "সাধারণ"
                )
            )
        }

        return FilterSpec(
            key = "budget_maker_income",
            titleEn = "Income Target Filter",
            titleBn = "আয় লক্ষ্য ফিল্টার",
            subtitleEn = "$monthName • Filter income categories & targets",
            subtitleBn = "$monthName • আয় ক্যাটাগরি ও লক্ষ্য ফিল্টার",
            icon = Icons.Default.TrendingUp,
            accentColor = SolidIncome,
            fields = listOf(
                FilterField.SelectField(
                    id = "items",
                    titleEn = "Income Categories & Groups",
                    titleBn = "আয় ক্যাটাগরি ও গ্রুপ",
                    icon = Icons.Default.Category,
                    isMultiSelect = true,
                    isHierarchical = true,
                    items = selectItems,
                    predicate = { cat, ids -> cat.id.toString() in ids }
                ),
                FilterField.RangeField(
                    id = "amount_range",
                    titleEn = "Target Earnings Range (৳)",
                    titleBn = "আয়ের লক্ষ্য রেঞ্জ (৳)",
                    icon = Icons.Default.Tune,
                    prefix = "৳"
                ),
                FilterField.ToggleGroupField(
                    id = "conditions",
                    titleEn = "Income Achievement & Target Conditions",
                    titleBn = "আয় অর্জন ও লক্ষ্য শর্ত",
                    icon = Icons.Default.Tune,
                    conditions = listOf(
                        FilterCondition(
                            id = "onlyTargetAchieved",
                            titleEn = "Target Achieved (Earned ≥ Target)",
                            titleBn = "লক্ষ্য অর্জিত হয়েছে (Earned ≥ Target)",
                            subtitleEn = "Income targets reached or exceeded",
                            subtitleBn = "প্রত্যাশিত আয় পূর্ণ বা তার বেশি হয়েছে",
                            icon = Icons.Default.CheckCircle,
                            group = "target_status"
                        ),
                        FilterCondition(
                            id = "onlyTargetPending",
                            titleEn = "Target Pending (Earned < Target)",
                            titleBn = "লক্ষ্য অর্জন বাকি (Earned < Target)",
                            subtitleEn = "Income targets not yet fully reached",
                            subtitleBn = "লক্ষ্যমাত্রার চেয়ে কম আয় এসেছে",
                            icon = Icons.Default.TrendingUp,
                            group = "target_status"
                        ),
                        FilterCondition(
                            id = "onlyWithActualActivity",
                            titleEn = "Has Actual Earnings (> ৳0)",
                            titleBn = "এই মাসে আয় এসেছে (> ৳০)",
                            subtitleEn = "Income sources with active inflows",
                            subtitleBn = "চলতি মাসে কার্যকর আয় জমা হয়েছে",
                            icon = Icons.Default.MonetizationOn,
                            group = "activity_level"
                        ),
                        FilterCondition(
                            id = "onlyWithBudgetOrTarget",
                            titleEn = "Target Configured (> ৳0)",
                            titleBn = "টার্গেট নির্ধারণ করা আছে",
                            subtitleEn = "Categories with target earnings set",
                            subtitleBn = "বাজেট মেকারে আয়ের লক্ষ্য সেট করা আছে",
                            icon = Icons.Default.Tune,
                            group = "budget_presence"
                        ),
                        FilterCondition(
                            id = "onlyWithoutBudgetOrTarget",
                            titleEn = "No Target Set (৳0)",
                            titleBn = "টার্গেট নির্ধারণ ছাড়া",
                            subtitleEn = "Categories without planned income target",
                            subtitleBn = "আয়ের কোনো লক্ষ্য নির্ধারণ করা হয়নি",
                            icon = Icons.Default.RadioButtonUnchecked,
                            group = "budget_presence"
                        )
                    )
                ),
                FilterField.ToggleGroupField(
                    id = "display_options",
                    titleEn = "Display & Cleanliness Options",
                    titleBn = "প্রদর্শন ও সাজানোর অপশন",
                    icon = Icons.Default.FilterAlt,
                    conditions = listOf(
                        FilterCondition(
                            id = "excludeZeroAmounts",
                            titleEn = "Exclude Inactive (0 Target & 0 Earned)",
                            titleBn = "নিষ্ক্রিয় বাদ দিন (০ টার্গেট ও ০ আয়)",
                            subtitleEn = "Hide categories with no target and zero earnings",
                            subtitleBn = "যাতে কোনো লক্ষ্য বা আয় নেই সেগুলো আড়াল করুন",
                            icon = Icons.Default.FilterAlt
                        ),
                        FilterCondition(
                            id = "hideEmptyGroups",
                            titleEn = "Hide Empty Parent Groups",
                            titleBn = "খালি গ্রুপ লুকান",
                            subtitleEn = "Don't display header cards for empty groups",
                            subtitleBn = "যেসব গ্রুপের সব ক্যাটাগরি ফাঁকা সেগুলো দেখাবেন না",
                            icon = Icons.Default.Category
                        )
                    )
                )
            )
        )
    }

    fun createAssetSpec(accounts: List<Account>, monthName: String): FilterSpec<Account> {
        val parentMap = accounts.filter { it.parentId == null }.associateBy { it.id }
        val childAccounts = accounts.filter { it.parentId != null }
        val standaloneAccounts = accounts.filter { acc -> acc.parentId == null && childAccounts.none { it.parentId == acc.id } }

        val selectItems = mutableListOf<SelectItemOption>()
        childAccounts.forEach { child ->
            val parentName = parentMap[child.parentId]?.nameEn ?: "Other"
            val parentNameBn = parentMap[child.parentId]?.nameBn ?: "অন্যান্য"
            selectItems.add(
                SelectItemOption(
                    id = child.id.toString(),
                    titleEn = child.nameEn,
                    titleBn = child.nameBn.ifBlank { child.nameEn },
                    groupKey = parentName,
                    groupTitleEn = parentName,
                    groupTitleBn = parentNameBn
                )
            )
        }
        standaloneAccounts.forEach { st ->
            selectItems.add(
                SelectItemOption(
                    id = st.id.toString(),
                    titleEn = st.nameEn,
                    titleBn = st.nameBn.ifBlank { st.nameEn },
                    groupKey = "General Accounts",
                    groupTitleEn = "General Accounts",
                    groupTitleBn = "সাধারণ অ্যাকাউন্ট"
                )
            )
        }

        return FilterSpec(
            key = "budget_maker_asset",
            titleEn = "Asset Accounts Filter",
            titleBn = "সম্পদ একাউন্ট ফিল্টার",
            subtitleEn = "$monthName • Filter asset accounts & balances",
            subtitleBn = "$monthName • সম্পদ ও ব্যালেন্স ফিল্টার",
            icon = Icons.Default.AccountBalanceWallet,
            accentColor = SolidPrimary,
            fields = listOf(
                FilterField.SelectField(
                    id = "items",
                    titleEn = "Asset Accounts & Groups",
                    titleBn = "সম্পদ অ্যাকাউন্ট ও গ্রুপ",
                    icon = Icons.Default.AccountBalance,
                    isMultiSelect = true,
                    isHierarchical = true,
                    items = selectItems,
                    predicate = { acc, ids -> acc.id.toString() in ids }
                ),
                FilterField.RangeField(
                    id = "amount_range",
                    titleEn = "Balance Range (৳)",
                    titleBn = "ব্যালেন্স রেঞ্জ (৳)",
                    icon = Icons.Default.Tune,
                    prefix = "৳"
                ),
                FilterField.ToggleGroupField(
                    id = "conditions",
                    titleEn = "Asset Balance & Status Conditions",
                    titleBn = "সম্পদ ব্যালেন্স ও স্থিতি শর্ত",
                    icon = Icons.Default.Tune,
                    conditions = listOf(
                        FilterCondition(
                            id = "onlyPositiveBalance",
                            titleEn = "Positive Balance Only (> ৳0)",
                            titleBn = "ইতিবাচক ব্যালেন্স (> ৳০)",
                            subtitleEn = "Accounts holding positive balance",
                            subtitleBn = "যেসব একাউন্টে জমা টাকা আছে",
                            icon = Icons.Default.TrendingUp,
                            group = "balance_status"
                        ),
                        FilterCondition(
                            id = "onlyZeroBalance",
                            titleEn = "Zero Balance Only (৳0)",
                            titleBn = "শূন্য ব্যালেন্স (৳০)",
                            subtitleEn = "Accounts with empty or zero balance",
                            subtitleBn = "যেসব একাউন্টে বর্তমানে কোনো টাকা নেই",
                            icon = Icons.Default.AccountBalanceWallet,
                            group = "balance_status"
                        ),
                        FilterCondition(
                            id = "onlyWithBudgetOrTarget",
                            titleEn = "Target Balance Configured",
                            titleBn = "টার্গেট ব্যালেন্স সেট করা আছে",
                            subtitleEn = "Accounts with planned target balances",
                            subtitleBn = "নির্দিষ্ট ব্যালেন্স অর্জনের লক্ষ্য রয়েছে",
                            icon = Icons.Default.Tune,
                            group = "budget_presence"
                        ),
                        FilterCondition(
                            id = "onlyWithActualActivity",
                            titleEn = "Active with Monthly Transactions",
                            titleBn = "চলতি মাসে লেনদেন হয়েছে",
                            subtitleEn = "Accounts involved in transactions this month",
                            subtitleBn = "এই মাসে ডেবিট বা ক্রেডিট কার্যক্রম হয়েছে",
                            icon = Icons.Default.MonetizationOn
                        )
                    )
                ),
                FilterField.ToggleGroupField(
                    id = "display_options",
                    titleEn = "Display & Cleanliness Options",
                    titleBn = "প্রদর্শন ও সাজানোর অপশন",
                    icon = Icons.Default.FilterAlt,
                    conditions = listOf(
                        FilterCondition(
                            id = "excludeZeroAmounts",
                            titleEn = "Exclude Inactive (0 Balance & No Activity)",
                            titleBn = "নিষ্ক্রিয় বাদ দিন (০ ব্যালেন্স ও লেনদেনহীন)",
                            subtitleEn = "Hide zero balance accounts without transactions",
                            subtitleBn = "লেনদেনহীন শূন্য ব্যালেন্স অ্যাকাউন্ট আড়াল করুন",
                            icon = Icons.Default.FilterAlt
                        ),
                        FilterCondition(
                            id = "hideEmptyGroups",
                            titleEn = "Hide Empty Parent Groups",
                            titleBn = "খালি গ্রুপ লুকান",
                            subtitleEn = "Don't display header cards for empty groups",
                            subtitleBn = "যেসব গ্রুপের সব একাউন্ট ফাঁকা সেগুলো দেখাবেন না",
                            icon = Icons.Default.AccountBalance
                        )
                    )
                )
            )
        )
    }

    fun createLiabilitySpec(accounts: List<Account>, monthName: String): FilterSpec<Account> {
        val parentMap = accounts.filter { it.parentId == null }.associateBy { it.id }
        val childAccounts = accounts.filter { it.parentId != null }
        val standaloneAccounts = accounts.filter { acc -> acc.parentId == null && childAccounts.none { it.parentId == acc.id } }

        val selectItems = mutableListOf<SelectItemOption>()
        childAccounts.forEach { child ->
            val parentName = parentMap[child.parentId]?.nameEn ?: "Other"
            val parentNameBn = parentMap[child.parentId]?.nameBn ?: "অন্যান্য"
            selectItems.add(
                SelectItemOption(
                    id = child.id.toString(),
                    titleEn = child.nameEn,
                    titleBn = child.nameBn.ifBlank { child.nameEn },
                    groupKey = parentName,
                    groupTitleEn = parentName,
                    groupTitleBn = parentNameBn
                )
            )
        }
        standaloneAccounts.forEach { st ->
            selectItems.add(
                SelectItemOption(
                    id = st.id.toString(),
                    titleEn = st.nameEn,
                    titleBn = st.nameBn.ifBlank { st.nameEn },
                    groupKey = "General Accounts",
                    groupTitleEn = "General Accounts",
                    groupTitleBn = "সাধারণ অ্যাকাউন্ট"
                )
            )
        }

        return FilterSpec(
            key = "budget_maker_liability",
            titleEn = "Liabilities & Debt Filter",
            titleBn = "দায় ও দেনা ফিল্টার",
            subtitleEn = "$monthName • Filter debt & liability accounts",
            subtitleBn = "$monthName • ঋণ ও দেনা ফিল্টার",
            icon = Icons.Default.CreditCard,
            accentColor = AmberGold,
            fields = listOf(
                FilterField.SelectField(
                    id = "items",
                    titleEn = "Liability Accounts & Groups",
                    titleBn = "দায় অ্যাকাউন্ট ও গ্রুপ",
                    icon = Icons.Default.CreditCard,
                    isMultiSelect = true,
                    isHierarchical = true,
                    items = selectItems,
                    predicate = { acc, ids -> acc.id.toString() in ids }
                ),
                FilterField.RangeField(
                    id = "amount_range",
                    titleEn = "Debt Amount Range (৳)",
                    titleBn = "ঋণ পরিমাণ রেঞ্জ (৳)",
                    icon = Icons.Default.Tune,
                    prefix = "৳"
                ),
                FilterField.ToggleGroupField(
                    id = "conditions",
                    titleEn = "Liability & Debt Status Conditions",
                    titleBn = "দেনা ও ঋণ স্থিতি শর্ত",
                    icon = Icons.Default.Tune,
                    conditions = listOf(
                        FilterCondition(
                            id = "onlyOutstandingDebt",
                            titleEn = "Outstanding Debt Only (> ৳0)",
                            titleBn = "বকেয়া দেনা বা ঋণ আছে (> ৳০)",
                            subtitleEn = "Accounts with pending debt balance",
                            subtitleBn = "যেসব একাউন্টে ঋণ বা বকেয়া পরিশোধ বাকি",
                            icon = Icons.Default.CreditCard,
                            group = "debt_status"
                        ),
                        FilterCondition(
                            id = "onlyClearedDebt",
                            titleEn = "Cleared / Zero Debt (৳0)",
                            titleBn = "পরিশোধিত বা শূন্য ঋণ (৳০)",
                            subtitleEn = "Accounts with no outstanding debt",
                            subtitleBn = "কোনো সক্রিয় দেনা নেই",
                            icon = Icons.Default.CheckCircle,
                            group = "debt_status"
                        ),
                        FilterCondition(
                            id = "onlyWithBudgetOrTarget",
                            titleEn = "Debt Limit / Target Configured",
                            titleBn = "ঋণ সীমা বা পরিশোধ টার্গেট আছে",
                            subtitleEn = "Accounts with planned debt targets",
                            subtitleBn = "পরিশোধ বা সর্বোচ্চ সীমার লক্ষ্য নির্ধারণ করা আছে",
                            icon = Icons.Default.Tune,
                            group = "budget_presence"
                        ),
                        FilterCondition(
                            id = "onlyWithActualActivity",
                            titleEn = "Active with Monthly Transactions",
                            titleBn = "চলতি মাসে লেনদেন হয়েছে",
                            subtitleEn = "Accounts with debt activity this month",
                            subtitleBn = "এই মাসে ঋণ গ্রহণ বা পরিশোধের এন্ট্রি আছে",
                            icon = Icons.Default.MonetizationOn
                        )
                    )
                ),
                FilterField.ToggleGroupField(
                    id = "display_options",
                    titleEn = "Display & Cleanliness Options",
                    titleBn = "প্রদর্শন ও সাজানোর অপশন",
                    icon = Icons.Default.FilterAlt,
                    conditions = listOf(
                        FilterCondition(
                            id = "excludeZeroAmounts",
                            titleEn = "Exclude Inactive (0 Debt & No Activity)",
                            titleBn = "নিষ্ক্রিয় বাদ দিন (০ দেনা ও লেনদেনহীন)",
                            subtitleEn = "Hide zero debt accounts without transactions",
                            subtitleBn = "লেনদেনহীন শূন্য দেনা অ্যাকাউন্ট আড়াল করুন",
                            icon = Icons.Default.FilterAlt
                        ),
                        FilterCondition(
                            id = "hideEmptyGroups",
                            titleEn = "Hide Empty Parent Groups",
                            titleBn = "খালি গ্রুপ লুকান",
                            subtitleEn = "Don't display header cards for empty groups",
                            subtitleBn = "যেসব গ্রুপের সব একাউন্ট ফাঁকা সেগুলো দেখাবেন না",
                            icon = Icons.Default.CreditCard
                        )
                    )
                )
            )
        )
    }

    fun createDashboardSpec(monthName: String): FilterSpec<String> {
        return FilterSpec(
            key = "budget_maker_dashboard",
            titleEn = "BM Dashboard Filter",
            titleBn = "ড্যাশবোর্ড ফিল্টার",
            subtitleEn = "$monthName • Overview scope & active sections",
            subtitleBn = "$monthName • ওভারভিউ সেটিংস",
            icon = Icons.Default.Dashboard,
            accentColor = SolidPrimary,
            fields = listOf(
                FilterField.ToggleGroupField(
                    id = "sections",
                    titleEn = "Visible Budget Categories",
                    titleBn = "দৃশ্যমান বাজেট বিভাগসমূহ",
                    icon = Icons.Default.Dashboard,
                    conditions = listOf(
                        FilterCondition(
                            id = "includeExpenses",
                            titleEn = "Include Expenses Section",
                            titleBn = "ব্যয় বিভাগ অন্তর্ভুক্ত করুন",
                            subtitleEn = "Show monthly expense budget cards and calculations",
                            subtitleBn = "মাসিক ব্যয় বাজেট কার্ড ও হিসাব প্রদর্শন করুন",
                            icon = Icons.Default.TrendingDown,
                            predicate = { it == "includeExpenses" }
                        ),
                        FilterCondition(
                            id = "includeIncomes",
                            titleEn = "Include Incomes Section",
                            titleBn = "আয় বিভাগ অন্তর্ভুক্ত করুন",
                            subtitleEn = "Show monthly income target cards and calculations",
                            subtitleBn = "মাসিক আয় লক্ষ্যমাত্রা কার্ড ও হিসাব প্রদর্শন করুন",
                            icon = Icons.Default.TrendingUp,
                            predicate = { it == "includeIncomes" }
                        ),
                        FilterCondition(
                            id = "includeAssets",
                            titleEn = "Include Assets Section",
                            titleBn = "সম্পদ বিভাগ অন্তর্ভুক্ত করুন",
                            subtitleEn = "Show asset balance cards and net worth calculations",
                            subtitleBn = "সম্পদ ব্যালেন্স কার্ড ও নেট ওর্থ হিসাব প্রদর্শন করুন",
                            icon = Icons.Default.AccountBalanceWallet,
                            predicate = { it == "includeAssets" }
                        ),
                        FilterCondition(
                            id = "includeLiabilities",
                            titleEn = "Include Liabilities Section",
                            titleBn = "দায় ও দেনা বিভাগ অন্তর্ভুক্ত করুন",
                            subtitleEn = "Show debt cards and liability calculations",
                            subtitleBn = "দেনা কার্ড ও ঋণ হিসাব প্রদর্শন করুন",
                            icon = Icons.Default.CreditCard,
                            predicate = { it == "includeLiabilities" }
                        )
                    )
                ),
                FilterField.ToggleGroupField(
                    id = "cleanliness",
                    titleEn = "Display Cleanliness",
                    titleBn = "প্রদর্শন পরিচ্ছন্নতা",
                    icon = Icons.Default.FilterAlt,
                    conditions = listOf(
                        FilterCondition(
                            id = "onlyNonZero",
                            titleEn = "Show Only Active Non-Zero Sections",
                            titleBn = "শুধু সক্রিয় অ-শূন্য মোট দেখান",
                            subtitleEn = "Display only sections with non-zero financial figures",
                            subtitleBn = "যেসব সেকশনে সক্রিয় পরিমাণ আছে শুধু সেগুলো প্রদর্শন করুন",
                            icon = Icons.Default.FilterAlt
                        )
                    )
                )
            )
        )
    }
}

/**
 * Modern Unified Dialog adapter for Budget Maker, delegating to UnifiedFilterDialog with result count calculation.
 */
@Composable
fun BudgetMakerFilterDialog(
    tabIndex: Int,
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
    val monthNameStr = DateUtils.formatMonthYear(selectedYear, selectedMonth, languageMode)

    if (tabIndex == 0) {
        val spec = remember(monthNameStr) {
            BudgetMakerFilterSpecs.createDashboardSpec(monthNameStr)
        }
        val initialState = remember(dashboardFilter) {
            dashboardFilter.toFilterState()
        }
        UnifiedFilterDialog(
            spec = spec,
            initialState = initialState,
            countProvider = { state ->
                val df = state.toBudgetMakerDashboardFilter()
                var activeSections = 0
                if (df.includeExpenses) activeSections++
                if (df.includeIncomes) activeSections++
                if (df.includeAssets) activeSections++
                if (df.includeLiabilities) activeSections++
                activeSections
            },
            languageMode = languageMode,
            onApply = { newState ->
                val newFilter = newState.toBudgetMakerDashboardFilter()
                onApplyDashboardFilter(newFilter)
            },
            onDismiss = onDismiss
        )
    } else {
        val targetCategories = remember(tabIndex, categories) {
            when (tabIndex) {
                1 -> categories.filter { it.type == CategoryType.EXPENSE && it.isActive }
                2 -> categories.filter { it.type == CategoryType.INCOME && it.isActive }
                else -> emptyList()
            }
        }
        val targetAccounts = remember(tabIndex, accounts) {
            when (tabIndex) {
                3 -> accounts.filter { it.type == AccountType.ASSET && it.isActive }
                4 -> accounts.filter { it.type == AccountType.LIABILITY && it.isActive }
                else -> emptyList()
            }
        }

        when (tabIndex) {
            1 -> {
                val spec = remember(targetCategories, monthNameStr) {
                    BudgetMakerFilterSpecs.createExpenseSpec(targetCategories, monthNameStr)
                }
                val initialState = remember(currentTabFilter) { currentTabFilter.toFilterState(1) }
                UnifiedFilterDialog(
                    spec = spec,
                    initialState = initialState,
                    countProvider = { state ->
                        val tf = state.toBudgetMakerTabFilter()
                        targetCategories.count { cat ->
                            (tf.selectedItemIds.isEmpty() || cat.id in tf.selectedItemIds)
                        }
                    },
                    languageMode = languageMode,
                    onApply = { newState ->
                        onApplyTabFilter(newState.toBudgetMakerTabFilter())
                    },
                    onDismiss = onDismiss
                )
            }
            2 -> {
                val spec = remember(targetCategories, monthNameStr) {
                    BudgetMakerFilterSpecs.createIncomeSpec(targetCategories, monthNameStr)
                }
                val initialState = remember(currentTabFilter) { currentTabFilter.toFilterState(2) }
                UnifiedFilterDialog(
                    spec = spec,
                    initialState = initialState,
                    countProvider = { state ->
                        val tf = state.toBudgetMakerTabFilter()
                        targetCategories.count { cat ->
                            (tf.selectedItemIds.isEmpty() || cat.id in tf.selectedItemIds)
                        }
                    },
                    languageMode = languageMode,
                    onApply = { newState ->
                        onApplyTabFilter(newState.toBudgetMakerTabFilter())
                    },
                    onDismiss = onDismiss
                )
            }
            3 -> {
                val spec = remember(targetAccounts, monthNameStr) {
                    BudgetMakerFilterSpecs.createAssetSpec(targetAccounts, monthNameStr)
                }
                val initialState = remember(currentTabFilter) { currentTabFilter.toFilterState(3) }
                UnifiedFilterDialog(
                    spec = spec,
                    initialState = initialState,
                    countProvider = { state ->
                        val tf = state.toBudgetMakerTabFilter()
                        targetAccounts.count { acc ->
                            (tf.selectedItemIds.isEmpty() || acc.id in tf.selectedItemIds)
                        }
                    },
                    languageMode = languageMode,
                    onApply = { newState ->
                        onApplyTabFilter(newState.toBudgetMakerTabFilter())
                    },
                    onDismiss = onDismiss
                )
            }
            4 -> {
                val spec = remember(targetAccounts, monthNameStr) {
                    BudgetMakerFilterSpecs.createLiabilitySpec(targetAccounts, monthNameStr)
                }
                val initialState = remember(currentTabFilter) { currentTabFilter.toFilterState(4) }
                UnifiedFilterDialog(
                    spec = spec,
                    initialState = initialState,
                    countProvider = { state ->
                        val tf = state.toBudgetMakerTabFilter()
                        targetAccounts.count { acc ->
                            (tf.selectedItemIds.isEmpty() || acc.id in tf.selectedItemIds)
                        }
                    },
                    languageMode = languageMode,
                    onApply = { newState ->
                        onApplyTabFilter(newState.toBudgetMakerTabFilter())
                    },
                    onDismiss = onDismiss
                )
            }
        }
    }
}

/**
 * Unified Active Budget Maker Filter Bar strip displaying active chips from spec with per-chip remove and clear-all.
 */
@Composable
fun ActiveBudgetMakerFilterBar(
    tabIndex: Int,
    tabName: String,
    tabFilter: BudgetMakerTabFilter,
    dashboardFilter: BudgetMakerDashboardFilter? = null,
    categories: List<Category> = emptyList(),
    accounts: List<Account> = emptyList(),
    selectedYear: Int = 0,
    selectedMonth: Int = 0,
    onTabFilterChange: (BudgetMakerTabFilter) -> Unit,
    onDashboardFilterChange: ((BudgetMakerDashboardFilter) -> Unit)? = null,
    onOpenFilterDialog: () -> Unit,
    accentColor: Color,
    languageMode: LanguageMode,
    modifier: Modifier = Modifier
) {
    val monthNameStr = if (selectedYear > 0 && selectedMonth > 0) {
        DateUtils.formatMonthYear(selectedYear, selectedMonth, languageMode)
    } else {
        tabName
    }

    if (tabIndex == 0) {
        val dash = dashboardFilter ?: return
        if (!dash.isActive) return

        val spec = remember(monthNameStr, accentColor) {
            BudgetMakerFilterSpecs.createDashboardSpec(monthNameStr)
        }
        val state = remember(dash) { dash.toFilterState() }

        UnifiedActiveFilterBar(
            spec = spec,
            state = state,
            onFilterChange = { newState ->
                onDashboardFilterChange?.invoke(newState.toBudgetMakerDashboardFilter())
            },
            onOpenFilterDialog = onOpenFilterDialog,
            accentColor = accentColor,
            languageMode = languageMode,
            onClearAll = { onDashboardFilterChange?.invoke(BudgetMakerDashboardFilter()) },
            modifier = modifier
        )
    } else {
        if (!tabFilter.isActive) return

        val spec = remember(tabIndex, categories, accounts, monthNameStr, accentColor) {
            when (tabIndex) {
                1 -> BudgetMakerFilterSpecs.createExpenseSpec(categories.filter { it.type == CategoryType.EXPENSE && it.isActive }, monthNameStr)
                2 -> BudgetMakerFilterSpecs.createIncomeSpec(categories.filter { it.type == CategoryType.INCOME && it.isActive }, monthNameStr)
                3 -> BudgetMakerFilterSpecs.createAssetSpec(accounts.filter { it.type == AccountType.ASSET && it.isActive }, monthNameStr)
                4 -> BudgetMakerFilterSpecs.createLiabilitySpec(accounts.filter { it.type == AccountType.LIABILITY && it.isActive }, monthNameStr)
                else -> BudgetMakerFilterSpecs.createExpenseSpec(emptyList(), monthNameStr)
            }
        }

        val state = remember(tabFilter, tabIndex) { tabFilter.toFilterState(tabIndex) }

        UnifiedActiveFilterBar(
            spec = spec,
            state = state,
            onFilterChange = { newState ->
                onTabFilterChange(newState.toBudgetMakerTabFilter())
            },
            onOpenFilterDialog = onOpenFilterDialog,
            accentColor = accentColor,
            languageMode = languageMode,
            onClearAll = { onTabFilterChange(BudgetMakerTabFilter()) },
            modifier = modifier
        )
    }
}

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
    ActiveBudgetMakerFilterBar(
        tabIndex = 1,
        tabName = tabName,
        tabFilter = tabFilter,
        dashboardFilter = null,
        categories = emptyList(),
        accounts = emptyList(),
        onTabFilterChange = onFilterChange,
        onDashboardFilterChange = null,
        onOpenFilterDialog = onOpenFilterDialog,
        accentColor = accentColor,
        languageMode = languageMode,
        modifier = modifier
    )
}
