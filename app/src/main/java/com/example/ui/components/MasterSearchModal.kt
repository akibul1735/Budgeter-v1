package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Account
import com.example.data.model.AccountType
import com.example.data.model.Category
import com.example.data.model.CategoryType
import com.example.data.model.ItemImageCache
import com.example.data.model.LanguageMode
import com.example.data.model.MonthlyBudget
import com.example.data.model.RecurringBillWithDetails
import com.example.data.model.SavingsGoalWithDetails
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.data.model.TransactionWithDetails
import com.example.data.model.WishlistItemWithCategory
import com.example.data.repository.AccountWithBalance
import com.example.ui.screens.AppView
import com.example.ui.screens.SettingsSubPage
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidExpenseContainer
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidIncomeContainer
import com.example.ui.theme.SolidPrimary
import com.example.ui.theme.SolidTransfer
import com.example.util.DateUtils
import com.example.util.IconHelper
import com.example.util.ItemCacheHelper
import com.example.util.LanguageHelper
import kotlinx.coroutines.delay

enum class SearchFilterCategory {
    ALL,
    TRANSACTIONS,
    ACCOUNTS,
    CATEGORIES,
    BUDGETS,
    GOALS_AND_BILLS,
    SETTINGS_AND_TOOLS
}

sealed class MasterSearchResult {
    abstract val id: String
    abstract val title: String
    abstract val subtitle: String?
    abstract val groupTag: String
    abstract val iconVector: ImageVector
    abstract val iconTint: Color?

    data class TransactionItem(
        val transactionWithDetails: TransactionWithDetails,
        val formattedAmount: String,
        val formattedDate: String,
        val resolvedIconName: String,
        override val id: String = "tx_${transactionWithDetails.transaction.id}",
        override val title: String,
        override val subtitle: String?,
        override val groupTag: String,
        override val iconVector: ImageVector,
        override val iconTint: Color?
    ) : MasterSearchResult()

    data class AccountItem(
        val account: Account,
        val balanceFormatted: String,
        override val id: String = "acc_${account.id}",
        override val title: String,
        override val subtitle: String?,
        override val groupTag: String,
        override val iconVector: ImageVector,
        override val iconTint: Color?
    ) : MasterSearchResult()

    data class CategoryItem(
        val category: Category,
        val isSubCategory: Boolean,
        override val id: String = "cat_${category.id}",
        override val title: String,
        override val subtitle: String?,
        override val groupTag: String,
        override val iconVector: ImageVector,
        override val iconTint: Color?
    ) : MasterSearchResult()

    data class BudgetItem(
        val monthlyBudget: MonthlyBudget,
        val categoryName: String,
        val budgetedFormatted: String,
        val spentFormatted: String,
        val remainingFormatted: String,
        val progressPercent: Float,
        val isOverBudget: Boolean,
        override val id: String = "budget_${monthlyBudget.id}",
        override val title: String,
        override val subtitle: String?,
        override val groupTag: String,
        override val iconVector: ImageVector,
        override val iconTint: Color?
    ) : MasterSearchResult()

    data class SavingsGoalItem(
        val goalWithDetails: SavingsGoalWithDetails,
        val targetFormatted: String,
        val savedFormatted: String,
        val progressPercent: Float,
        override val id: String = "goal_${goalWithDetails.goal.id}",
        override val title: String,
        override val subtitle: String?,
        override val groupTag: String,
        override val iconVector: ImageVector,
        override val iconTint: Color?
    ) : MasterSearchResult()

    data class RecurringBillItem(
        val billWithDetails: RecurringBillWithDetails,
        val amountFormatted: String,
        override val id: String = "bill_${billWithDetails.bill.id}",
        override val title: String,
        override val subtitle: String?,
        override val groupTag: String,
        override val iconVector: ImageVector,
        override val iconTint: Color?
    ) : MasterSearchResult()

    data class WishlistResultItem(
        val wishlistItem: WishlistItemWithCategory,
        val amountFormatted: String,
        override val id: String = "wish_${wishlistItem.item.id}",
        override val title: String,
        override val subtitle: String?,
        override val groupTag: String,
        override val iconVector: ImageVector,
        override val iconTint: Color?
    ) : MasterSearchResult()

    data class NavigationActionItem(
        val targetView: AppView? = null,
        val targetSettingsSubPage: SettingsSubPage? = null,
        val actionType: String = "",
        val keywords: List<String> = emptyList(),
        override val id: String,
        override val title: String,
        override val subtitle: String?,
        override val groupTag: String,
        override val iconVector: ImageVector,
        override val iconTint: Color? = null
    ) : MasterSearchResult()
}

@Composable
fun MasterSearchModal(
    languageMode: LanguageMode,
    transactions: List<TransactionWithDetails>,
    allAccounts: List<Account>,
    accountsWithBalances: List<AccountWithBalance>,
    allCategories: List<Category>,
    monthlyBudgets: List<MonthlyBudget>,
    recurringBills: List<RecurringBillWithDetails> = emptyList(),
    savingsGoals: List<SavingsGoalWithDetails> = emptyList(),
    wishlistItems: List<WishlistItemWithCategory> = emptyList(),
    itemImageCacheMap: Map<String, ItemImageCache> = emptyMap(),
    onDismiss: () -> Unit,
    onNavigate: (AppView) -> Unit,
    onTransactionClick: (Transaction) -> Unit,
    onAccountClick: (Account) -> Unit,
    onOpenCalculator: () -> Unit = {},
    onNavigateToSettingsSubPage: (SettingsSubPage) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(SearchFilterCategory.ALL) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val errorColor = MaterialTheme.colorScheme.error

    LaunchedEffect(Unit) {
        delay(150)
        try {
            focusRequester.requestFocus()
            keyboardController?.show()
        } catch (_: Exception) {}
    }

    // Index and Search across all entities
    val searchResults = remember(
        searchQuery,
        selectedCategory,
        languageMode,
        transactions,
        allAccounts,
        accountsWithBalances,
        allCategories,
        monthlyBudgets,
        recurringBills,
        savingsGoals,
        wishlistItems
    ) {
        val query = searchQuery.trim().lowercase(java.util.Locale.ROOT)
        if (query.isEmpty()) {
            emptyList()
        } else {
            val results = mutableListOf<MasterSearchResult>()

            // 1. Transactions & Items
            if (selectedCategory == SearchFilterCategory.ALL || selectedCategory == SearchFilterCategory.TRANSACTIONS) {
                transactions.filter { item ->
                    val tx = item.transaction
                    val catNameEn = item.category?.nameEn.orEmpty().lowercase()
                    val catNameBn = item.category?.nameBn.orEmpty().lowercase()
                    val subCatNameEn = item.subCategory?.nameEn.orEmpty().lowercase()
                    val subCatNameBn = item.subCategory?.nameBn.orEmpty().lowercase()
                    val debitAccEn = item.debitAccount?.nameEn.orEmpty().lowercase()
                    val debitAccBn = item.debitAccount?.nameBn.orEmpty().lowercase()
                    val creditAccEn = item.creditAccount?.nameEn.orEmpty().lowercase()
                    val creditAccBn = item.creditAccount?.nameBn.orEmpty().lowercase()
                    val payee = tx.payeeOrPayer.orEmpty().lowercase()
                    val note = tx.note.orEmpty().lowercase()
                    val ref = tx.referenceNo.orEmpty().lowercase()
                    val amtStr = tx.amount.toString()

                    note.contains(query) ||
                    payee.contains(query) ||
                    ref.contains(query) ||
                    catNameEn.contains(query) ||
                    catNameBn.contains(query) ||
                    subCatNameEn.contains(query) ||
                    subCatNameBn.contains(query) ||
                    debitAccEn.contains(query) ||
                    debitAccBn.contains(query) ||
                    creditAccEn.contains(query) ||
                    creditAccBn.contains(query) ||
                    amtStr.contains(query)
                }.take(30).forEach { item ->
                    val tx = item.transaction
                    val titleText = when {
                        !tx.payeeOrPayer.isNullOrBlank() -> tx.payeeOrPayer
                        !tx.note.isNullOrBlank() -> tx.note.take(40)
                        item.subCategory != null -> item.subCategory.localizedName(languageMode)
                        item.category != null -> item.category.localizedName(languageMode)
                        else -> tx.type.name
                    }
                    val iconName = ItemCacheHelper.resolveTransactionIconName(
                        transaction = tx,
                        category = item.category,
                        subCategory = item.subCategory,
                        cacheMap = itemImageCacheMap
                    )
                    val iconVector = IconHelper.getIconByName(iconName)
                    val tint = when (tx.type) {
                        TransactionType.EXPENSE -> SolidExpense
                        TransactionType.INCOME -> SolidIncome
                        TransactionType.TRANSFER -> SolidTransfer
                    }
                    val formattedAmt = (if (tx.type == TransactionType.INCOME) "+" else if (tx.type == TransactionType.EXPENSE) "-" else "") +
                            LanguageHelper.formatCurrency(tx.amount, languageMode)
                    val catDisplay = item.subCategory?.localizedName(languageMode)
                        ?: item.category?.localizedName(languageMode)
                        ?: tx.type.name
                    val subtitleText = "${DateUtils.formatDate(tx.dateEpochMs, languageMode)} • $catDisplay"

                    results.add(
                        MasterSearchResult.TransactionItem(
                            transactionWithDetails = item,
                            formattedAmount = formattedAmt,
                            formattedDate = DateUtils.formatDate(tx.dateEpochMs, languageMode),
                            resolvedIconName = iconName,
                            title = titleText,
                            subtitle = subtitleText,
                            groupTag = if (languageMode == LanguageMode.BANGLA) "লেনদেন" else "TRANSACTIONS",
                            iconVector = iconVector,
                            iconTint = tint
                        )
                    )
                }
            }

            // 2. Accounts & Wallets
            if (selectedCategory == SearchFilterCategory.ALL || selectedCategory == SearchFilterCategory.ACCOUNTS) {
                allAccounts.filter { acc ->
                    acc.nameEn.lowercase().contains(query) ||
                    acc.nameBn.lowercase().contains(query) ||
                    acc.description.lowercase().contains(query) ||
                    acc.accountNumber.lowercase().contains(query) ||
                    acc.type.name.lowercase().contains(query)
                }.forEach { acc ->
                    val matchedBalance = accountsWithBalances.find { it.account.id == acc.id }?.currentBalance ?: 0.0
                    val typeLabel = if (acc.type == AccountType.ASSET) {
                        if (languageMode == LanguageMode.BANGLA) "সম্পদ একাউন্ট" else "Asset Account"
                    } else {
                        if (languageMode == LanguageMode.BANGLA) "দায় / ঋণ" else "Liability Account"
                    }
                    results.add(
                        MasterSearchResult.AccountItem(
                            account = acc,
                            balanceFormatted = LanguageHelper.formatCurrency(matchedBalance, languageMode),
                            title = acc.localizedName(languageMode),
                            subtitle = "$typeLabel • ${LanguageHelper.formatCurrency(matchedBalance, languageMode)}",
                            groupTag = if (languageMode == LanguageMode.BANGLA) "একাউন্ট ও ওয়ালেট" else "ACCOUNTS & WALLETS",
                            iconVector = IconHelper.getIconByName(acc.iconName),
                            iconTint = if (acc.type == AccountType.ASSET) SolidIncome else SolidExpense
                        )
                    )
                }
            }

            // 3. Categories & Subcategories
            if (selectedCategory == SearchFilterCategory.ALL || selectedCategory == SearchFilterCategory.CATEGORIES) {
                allCategories.filter { cat ->
                    cat.nameEn.lowercase().contains(query) ||
                    cat.nameBn.lowercase().contains(query) ||
                    cat.type.name.lowercase().contains(query)
                }.forEach { cat ->
                    val isSub = cat.parentId != null
                    val typeLabel = if (cat.type == CategoryType.EXPENSE) {
                        if (languageMode == LanguageMode.BANGLA) "ব্যয় বিভাগ" else "Expense Category"
                    } else {
                        if (languageMode == LanguageMode.BANGLA) "আয় বিভাগ" else "Income Category"
                    }
                    val parentCat = if (isSub) allCategories.find { it.id == cat.parentId } else null
                    val parentNote = if (parentCat != null) " (${parentCat.localizedName(languageMode)})" else ""

                    results.add(
                        MasterSearchResult.CategoryItem(
                            category = cat,
                            isSubCategory = isSub,
                            title = cat.localizedName(languageMode),
                            subtitle = "$typeLabel$parentNote",
                            groupTag = if (languageMode == LanguageMode.BANGLA) "ক্যাটাগরি" else "CATEGORIES",
                            iconVector = IconHelper.getIconByName(cat.iconName),
                            iconTint = if (cat.type == CategoryType.EXPENSE) SolidExpense else SolidIncome
                        )
                    )
                }
            }

            // 4. Budgets & Limits
            if (selectedCategory == SearchFilterCategory.ALL || selectedCategory == SearchFilterCategory.BUDGETS) {
                monthlyBudgets.forEach { budget ->
                    val matchedCat = allCategories.find { it.id == budget.itemId }
                    val catName = matchedCat?.localizedName(languageMode) ?: "Category #${budget.itemId}"
                    if (catName.lowercase().contains(query) || "budget".contains(query) || "বাজেট".contains(query)) {
                        val spent = transactions.filter {
                            it.category?.id == budget.itemId && it.transaction.type == TransactionType.EXPENSE
                        }.sumOf { it.transaction.amount }
                        val remaining = (budget.budgetedAmount - spent).coerceAtLeast(0.0)
                        val progress = if (budget.budgetedAmount > 0) (spent / budget.budgetedAmount).toFloat().coerceIn(0f, 1f) else 0f

                        results.add(
                            MasterSearchResult.BudgetItem(
                                monthlyBudget = budget,
                                categoryName = catName,
                                budgetedFormatted = LanguageHelper.formatCurrency(budget.budgetedAmount, languageMode),
                                spentFormatted = LanguageHelper.formatCurrency(spent, languageMode),
                                remainingFormatted = LanguageHelper.formatCurrency(remaining, languageMode),
                                progressPercent = progress,
                                isOverBudget = spent > budget.budgetedAmount,
                                title = catName,
                                subtitle = if (languageMode == LanguageMode.BANGLA)
                                    "বাজেট: ${LanguageHelper.formatCurrency(budget.budgetedAmount, languageMode)} • অবশিষ্ট: ${LanguageHelper.formatCurrency(remaining, languageMode)}"
                                else
                                    "Limit: ${LanguageHelper.formatCurrency(budget.budgetedAmount, languageMode)} • Rem: ${LanguageHelper.formatCurrency(remaining, languageMode)}",
                                groupTag = if (languageMode == LanguageMode.BANGLA) "বাজেট ও লিমিট" else "BUDGETS & LIMITS",
                                iconVector = IconHelper.getIconByName(matchedCat?.iconName ?: "PieChart"),
                                iconTint = if (spent > budget.budgetedAmount) SolidExpense else SolidPrimary
                            )
                        )
                    }
                }
            }

            // 5. Goals, Bills & Wishlist
            if (selectedCategory == SearchFilterCategory.ALL || selectedCategory == SearchFilterCategory.GOALS_AND_BILLS) {
                // Goals
                savingsGoals.filter {
                    it.goal.name.lowercase().contains(query) ||
                    it.goal.nameBn.lowercase().contains(query) ||
                    it.goal.notes.lowercase().contains(query) ||
                    "goal".contains(query) || "সঞ্চয়".contains(query)
                }.forEach { goal ->
                    val progress = (goal.progressPercent / 100f).coerceIn(0f, 1f)
                    results.add(
                        MasterSearchResult.SavingsGoalItem(
                            goalWithDetails = goal,
                            targetFormatted = LanguageHelper.formatCurrency(goal.goal.targetAmount, languageMode),
                            savedFormatted = LanguageHelper.formatCurrency(goal.effectiveSaved, languageMode),
                            progressPercent = progress,
                            title = if (languageMode == LanguageMode.BANGLA && goal.goal.nameBn.isNotBlank()) goal.goal.nameBn else goal.goal.name,
                            subtitle = if (languageMode == LanguageMode.BANGLA)
                                "সঞ্চয় লক্ষ্য: ${LanguageHelper.formatCurrency(goal.goal.targetAmount, languageMode)} (${goal.progressPercent.toInt()}%)"
                            else
                                "Goal: ${LanguageHelper.formatCurrency(goal.goal.targetAmount, languageMode)} (${goal.progressPercent.toInt()}% saved)",
                            groupTag = if (languageMode == LanguageMode.BANGLA) "সঞ্চয় লক্ষ্য" else "SAVINGS GOALS",
                            iconVector = IconHelper.getIconByName(goal.goal.iconName),
                            iconTint = SolidIncome
                        )
                    )
                }

                // Bills
                recurringBills.filter {
                    it.bill.title.lowercase().contains(query) ||
                    it.bill.payeeOrPayer.lowercase().contains(query) ||
                    it.bill.note.lowercase().contains(query) ||
                    "bill".contains(query) || "বিল".contains(query)
                }.forEach { bill ->
                    val billTitle = bill.bill.title.ifBlank { bill.bill.payeeOrPayer }
                    results.add(
                        MasterSearchResult.RecurringBillItem(
                            billWithDetails = bill,
                            amountFormatted = LanguageHelper.formatCurrency(bill.bill.amount, languageMode),
                            title = billTitle,
                            subtitle = "${bill.bill.recurrencePeriod.name} • ${LanguageHelper.formatCurrency(bill.bill.amount, languageMode)}",
                            groupTag = if (languageMode == LanguageMode.BANGLA) "পুনরাবৃত্ত বিল" else "RECURRING BILLS",
                            iconVector = Icons.Default.EventRepeat,
                            iconTint = SolidExpense
                        )
                    )
                }

                // Wishlist
                wishlistItems.filter {
                    it.item.title.lowercase().contains(query) ||
                    it.item.notes.lowercase().contains(query) ||
                    "wishlist".contains(query) || "উইশলিস্ট".contains(query)
                }.forEach { wish ->
                    results.add(
                        MasterSearchResult.WishlistResultItem(
                            wishlistItem = wish,
                            amountFormatted = LanguageHelper.formatCurrency(wish.item.estimatedAmount, languageMode),
                            title = wish.item.title,
                            subtitle = if (wish.category != null)
                                "${wish.category.localizedName(languageMode)} • ${LanguageHelper.formatCurrency(wish.item.estimatedAmount, languageMode)}"
                            else
                                LanguageHelper.formatCurrency(wish.item.estimatedAmount, languageMode),
                            groupTag = if (languageMode == LanguageMode.BANGLA) "উইশলিস্ট" else "WISHLIST",
                            iconVector = Icons.Default.Favorite,
                            iconTint = SolidPrimary
                        )
                    )
                }
            }

            // 6. Settings, Tools & Reports Shortcuts
            if (selectedCategory == SearchFilterCategory.ALL || selectedCategory == SearchFilterCategory.SETTINGS_AND_TOOLS) {
                val actionCatalog = listOf(
                    MasterSearchResult.NavigationActionItem(
                        id = "nav_dashboard",
                        title = if (languageMode == LanguageMode.BANGLA) "ড্যাশবোর্ড ও আর্থিক সারসংক্ষেপ" else "Dashboard & Overview",
                        subtitle = if (languageMode == LanguageMode.BANGLA) "মোট সম্পদ, খরচযোগ্য অর্থ ও চার্ট" else "Net worth, expendable balance & summaries",
                        groupTag = if (languageMode == LanguageMode.BANGLA) "নেভিগেশন ও টুলস" else "APPS & TOOLS",
                        iconVector = Icons.Default.PieChart,
                        iconTint = primaryColor,
                        targetView = AppView.DASHBOARD,
                        keywords = listOf("dashboard", "ড্যাশবোর্ড", "overview", "net worth", "মোট সম্পদ", "home")
                    ),
                    MasterSearchResult.NavigationActionItem(
                        id = "nav_ledger",
                        title = if (languageMode == LanguageMode.BANGLA) "লেনদেন ও খতিয়ান" else "Ledger & Transactions",
                        subtitle = if (languageMode == LanguageMode.BANGLA) "সকল আয় ও ব্যয়ের তালিকা" else "View and filter all transactions",
                        groupTag = if (languageMode == LanguageMode.BANGLA) "নেভিগেশন ও টুলস" else "APPS & TOOLS",
                        iconVector = Icons.Default.Receipt,
                        iconTint = primaryColor,
                        targetView = AppView.LEDGER,
                        keywords = listOf("ledger", "লেজার", "transactions", "লেনদেন", "statement", "হিসাব")
                    ),
                    MasterSearchResult.NavigationActionItem(
                        id = "nav_cash_flow",
                        title = if (languageMode == LanguageMode.BANGLA) "ক্যাশ ফ্লো ও প্রবাহ বিশ্লেষণ" else "Cash Flow & Analytics",
                        subtitle = if (languageMode == LanguageMode.BANGLA) "মাসিক আয়, ব্যয় ও নিট সঞ্চয় ট্রেন্ড" else "Inflow, outflow & net cash flow trends",
                        groupTag = if (languageMode == LanguageMode.BANGLA) "নেভিগেশন ও টুলস" else "APPS & TOOLS",
                        iconVector = Icons.AutoMirrored.Filled.TrendingUp,
                        iconTint = SolidIncome,
                        targetView = AppView.CASH_FLOW,
                        keywords = listOf("cash flow", "ক্যাশ ফ্লো", "analytics", "analysis", "trend", "রিপোর্ট", "analytics")
                    ),
                    MasterSearchResult.NavigationActionItem(
                        id = "tool_calculator",
                        title = if (languageMode == LanguageMode.BANGLA) "পপআপ ক্যালকুলেটর" else "Standalone Calculator",
                        subtitle = if (languageMode == LanguageMode.BANGLA) "দ্রুত হিসাব ও গাণিতিক হিসেব" else "Quick arithmetic & math calculations",
                        groupTag = if (languageMode == LanguageMode.BANGLA) "নেভিগেশন ও টুলস" else "APPS & TOOLS",
                        iconVector = Icons.Default.Calculate,
                        iconTint = tertiaryColor,
                        actionType = "CALCULATOR",
                        keywords = listOf("calculator", "ক্যালকুলেটর", "math", "হিসাব", "হিসাবকারী", "calc")
                    ),
                    MasterSearchResult.NavigationActionItem(
                        id = "nav_rm_manager",
                        title = if (languageMode == LanguageMode.BANGLA) "ঋণ ও দেনা-পাওনা (RM Manager)" else "Loans & Debts (RM Manager)",
                        subtitle = if (languageMode == LanguageMode.BANGLA) "ধার দেওয়া ও নেওয়া টাকার সঠিক হিসাব" else "Track borrowings, lendings & repayments",
                        groupTag = if (languageMode == LanguageMode.BANGLA) "নেভিগেশন ও টুলস" else "APPS & TOOLS",
                        iconVector = Icons.Default.Handshake,
                        iconTint = SolidExpense,
                        targetView = AppView.RM_MANAGER,
                        keywords = listOf("rm manager", "loan", "debt", "ধার", "দেনা", "পাওনা", "borrow", "lend")
                    ),
                    MasterSearchResult.NavigationActionItem(
                        id = "set_themes",
                        title = if (languageMode == LanguageMode.BANGLA) "থিম ও রূপরেখা কাস্টমাইজেশন" else "Themes & Appearance",
                        subtitle = if (languageMode == LanguageMode.BANGLA) "ডার্ক মোড, কালার প্যালেট ও ফ্রেম" else "Dark/Light mode, Emerald palette & M3 dynamic",
                        groupTag = if (languageMode == LanguageMode.BANGLA) "সেটিংস অপশন" else "SETTINGS",
                        iconVector = Icons.Default.Palette,
                        iconTint = primaryColor,
                        targetSettingsSubPage = SettingsSubPage.THEMES,
                        keywords = listOf("theme", "থিম", "appearance", "dark mode", "ডার্ক মোড", "color", "রং", "font")
                    ),
                    MasterSearchResult.NavigationActionItem(
                        id = "set_backup_sync",
                        title = if (languageMode == LanguageMode.BANGLA) "ক্লাউড সিঙ্ক ও ব্যাকআপ" else "Backup & Cloud Sync",
                        subtitle = if (languageMode == LanguageMode.BANGLA) "গুগল ড্রাইভ, ড্রপবক্স ও লোকাল ব্যাকআপ" else "Google Drive, Dropbox & Local backup restore",
                        groupTag = if (languageMode == LanguageMode.BANGLA) "সেটিংস অপশন" else "SETTINGS",
                        iconVector = Icons.Default.CloudSync,
                        iconTint = primaryColor,
                        targetView = AppView.BACKUP_SYNC,
                        keywords = listOf("backup", "ব্যাকআপ", "sync", "সিঙ্ক", "google drive", "drive", "dropbox", "ড্রাইভ", "restore")
                    ),
                    MasterSearchResult.NavigationActionItem(
                        id = "set_currency",
                        title = if (languageMode == LanguageMode.BANGLA) "মুদ্রা পছন্দ (BDT ৳ / USD $)" else "Currency Setup",
                        subtitle = if (languageMode == LanguageMode.BANGLA) "মুদ্রা কোড, প্রতীক ও অবস্থান" else "Change currency code, symbol & placement",
                        groupTag = if (languageMode == LanguageMode.BANGLA) "সেটিংস অপশন" else "SETTINGS",
                        iconVector = Icons.Default.MonetizationOn,
                        iconTint = primaryColor,
                        targetSettingsSubPage = SettingsSubPage.CURRENCY,
                        keywords = listOf("currency", "মুদ্রা", "taka", "টাকা", "bdt", "dollar", "usd", "symbol", "প্রতীক")
                    ),
                    MasterSearchResult.NavigationActionItem(
                        id = "set_security",
                        title = if (languageMode == LanguageMode.BANGLA) "নিরাপত্তা, পিন ও ফিঙ্গারপ্রিন্ট" else "Security & App Lock",
                        subtitle = if (languageMode == LanguageMode.BANGLA) "অ্যাপ লক, পিন সুরক্ষা ও বায়োমেট্রিক" else "PIN code, biometric lock & delete protection",
                        groupTag = if (languageMode == LanguageMode.BANGLA) "সেটিংস অপশন" else "SETTINGS",
                        iconVector = Icons.Default.Fingerprint,
                        iconTint = primaryColor,
                        targetSettingsSubPage = SettingsSubPage.SECURITY,
                        keywords = listOf("security", "নিরাপত্তা", "pin", "পিন", "lock", "লক", "fingerprint", "ফিঙ্গারপ্রিন্ট", "biometric", "পাসওয়ার্ড")
                    ),
                    MasterSearchResult.NavigationActionItem(
                        id = "set_widgets",
                        title = if (languageMode == LanguageMode.BANGLA) "হোম স্ক্রিন উইজেট" else "Home Screen Widgets",
                        subtitle = if (languageMode == LanguageMode.BANGLA) "কুইক অ্যাকশন ও লাইভ বাজেট মিটার উইজেট" else "Pin live budget meter & quick entry widgets",
                        groupTag = if (languageMode == LanguageMode.BANGLA) "সেটিংস অপশন" else "SETTINGS",
                        iconVector = Icons.Default.Widgets,
                        iconTint = primaryColor,
                        targetSettingsSubPage = SettingsSubPage.WIDGETS,
                        keywords = listOf("widget", "উইজেট", "home screen", "মিটার", "shortcut")
                    ),
                    MasterSearchResult.NavigationActionItem(
                        id = "set_icon_cache",
                        title = if (languageMode == LanguageMode.BANGLA) "আইটেম আইকন লাইব্রেরি ও ক্যাশ" else "Item Icons & Cache",
                        subtitle = if (languageMode == LanguageMode.BANGLA) "ইন-অ্যাপ আইকন স্টোর, ক্রপ ও ক্যাশ মেমোরি ক্লিন" else "Vast icon store, image cropper & clean cache",
                        groupTag = if (languageMode == LanguageMode.BANGLA) "সেটিংস অপশন" else "SETTINGS",
                        iconVector = Icons.Default.Category,
                        iconTint = primaryColor,
                        targetSettingsSubPage = SettingsSubPage.ICON_IMAGE_CACHE,
                        keywords = listOf("icon", "আইকন", "cache", "ক্যাশ", "crop", "ছবি", "library")
                    ),
                    MasterSearchResult.NavigationActionItem(
                        id = "set_navigation_tabs",
                        title = if (languageMode == LanguageMode.BANGLA) "ট্যাব বার কাস্টমাইজেশন" else "Navigation Tabs Bar",
                        subtitle = if (languageMode == LanguageMode.BANGLA) "নিচের ট্যাব লুকানো/দেখানো ও সাজানো" else "Reorder & toggle visible navigation tabs",
                        groupTag = if (languageMode == LanguageMode.BANGLA) "সেটিংস অপশন" else "SETTINGS",
                        iconVector = Icons.Default.ViewCarousel,
                        iconTint = primaryColor,
                        targetSettingsSubPage = SettingsSubPage.NAVIGATION_TABS,
                        keywords = listOf("tab", "ট্যাব", "navigation", "নেভিগেশন", "bottom bar", "ট্যাব বার")
                    ),
                    MasterSearchResult.NavigationActionItem(
                        id = "set_archive_prune",
                        title = if (languageMode == LanguageMode.BANGLA) "আর্কাইভ ও ডাটা প্রুনিং" else "Archive & Data Prune",
                        subtitle = if (languageMode == LanguageMode.BANGLA) "ব্যালেন্স ঠিক রেখে পুরাতন লেনদেন আর্কাইভ" else "Fiscal year roll-forward & encrypted archive",
                        groupTag = if (languageMode == LanguageMode.BANGLA) "সেটিংস অপশন" else "SETTINGS",
                        iconVector = Icons.Default.Archive,
                        iconTint = primaryColor,
                        targetView = AppView.ARCHIVE_PRUNE,
                        keywords = listOf("archive", "আর্কাইভ", "prune", "প্রুন", "fiscal year", "পুরাতন ডাটা")
                    ),
                    MasterSearchResult.NavigationActionItem(
                        id = "set_reset",
                        title = if (languageMode == LanguageMode.BANGLA) "রিসেট ও ক্লিন ডাটা" else "Reset & Wipe Data",
                        subtitle = if (languageMode == LanguageMode.BANGLA) "লেনদেন মোছা বা ফ্যাক্টরি রিসেট" else "Clear transactions or reset application",
                        groupTag = if (languageMode == LanguageMode.BANGLA) "সেটিংস অপশন" else "SETTINGS",
                        iconVector = Icons.Default.DeleteSweep,
                        iconTint = errorColor,
                        targetView = AppView.RESET,
                        keywords = listOf("reset", "রিসেট", "delete", "মুছুন", "wipe", "factory reset", "পরিষ্কার")
                    )
                )

                actionCatalog.filter { item ->
                    item.title.lowercase().contains(query) ||
                    item.subtitle?.lowercase()?.contains(query) == true ||
                    item.keywords.any { it.contains(query) }
                }.forEach {
                    results.add(it)
                }
            }

            results
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .imePadding(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Search Bar Header
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(26.dp)
                                )
                                .padding(horizontal = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 8.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA)
                                            "অ্যাপে যেকোনো কিছু খুঁজুন (লেনদেন, একাউন্ট, বাজেট, সেটিংস)..."
                                        else
                                            "Search anything (Transactions, Accounts, Budgets, Settings)...",
                                        style = TextStyle(
                                            fontSize = 14.5.sp,
                                            color = MaterialTheme.colorScheme.outline
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                BasicTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .focusRequester(focusRequester)
                                        .testTag("master_search_input_field"),
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        fontSize = 15.5.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                    keyboardActions = KeyboardActions(
                                        onSearch = {
                                            focusManager.clearFocus()
                                            keyboardController?.hide()
                                        }
                                    )
                                )
                            }

                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { searchQuery = "" },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        // Category Filter Chips Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            SearchFilterCategory.values().forEach { cat ->
                                val label = when (cat) {
                                    SearchFilterCategory.ALL -> if (languageMode == LanguageMode.BANGLA) "সকল" else "All"
                                    SearchFilterCategory.TRANSACTIONS -> if (languageMode == LanguageMode.BANGLA) "লেনদেন" else "Transactions"
                                    SearchFilterCategory.ACCOUNTS -> if (languageMode == LanguageMode.BANGLA) "একাউন্ট" else "Accounts"
                                    SearchFilterCategory.CATEGORIES -> if (languageMode == LanguageMode.BANGLA) "ক্যাটাগরি" else "Categories"
                                    SearchFilterCategory.BUDGETS -> if (languageMode == LanguageMode.BANGLA) "বাজেট" else "Budgets"
                                    SearchFilterCategory.GOALS_AND_BILLS -> if (languageMode == LanguageMode.BANGLA) "লক্ষ্য ও বিল" else "Goals & Bills"
                                    SearchFilterCategory.SETTINGS_AND_TOOLS -> if (languageMode == LanguageMode.BANGLA) "সেটিংস ও টুলস" else "Settings & Tools"
                                }
                                val isSelected = selectedCategory == cat
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedCategory = cat },
                                    label = {
                                        Text(
                                            text = label,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }
                        }
                    }
                }

                // Search Results or Empty State
                if (searchQuery.isBlank()) {
                    // Initial Quick Search Recommendations
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "দ্রুত খুঁজে নিন" else "QUICK SHORTCUTS",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    val quickTags = listOf(
                                        "bKash" to Icons.Default.AccountBalanceWallet,
                                        "Nagad" to Icons.Default.AccountBalanceWallet,
                                        "Bank" to Icons.Default.AccountBalance,
                                        "Food" to Icons.Default.Category,
                                        "Shopping" to Icons.Default.Category,
                                        "Backup" to Icons.Default.CloudSync,
                                        "Themes" to Icons.Default.Palette,
                                        "PIN" to Icons.Default.Fingerprint,
                                        "Calculator" to Icons.Default.Calculate
                                    )
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "জনপ্রিয় অনুসন্ধান:" else "Popular searches:",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        quickTags.forEach { (tag, icon) ->
                                            Surface(
                                                shape = RoundedCornerShape(20.dp),
                                                color = MaterialTheme.colorScheme.surface,
                                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                                modifier = Modifier.clickable { searchQuery = tag }
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                                                    Text(text = tag, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Recent Transactions Preview
                        if (transactions.isNotEmpty()) {
                            item {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "সাম্প্রতিক লেনদেন" else "RECENT TRANSACTIONS",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            items(transactions.take(8)) { item ->
                                val tx = item.transaction
                                val titleText = when {
                                    !tx.payeeOrPayer.isNullOrBlank() -> tx.payeeOrPayer
                                    !tx.note.isNullOrBlank() -> tx.note.take(40)
                                    item.subCategory != null -> item.subCategory.localizedName(languageMode)
                                    item.category != null -> item.category.localizedName(languageMode)
                                    else -> tx.type.name
                                }
                                val iconName = ItemCacheHelper.resolveTransactionIconName(
                                    transaction = tx,
                                    category = item.category,
                                    subCategory = item.subCategory,
                                    cacheMap = itemImageCacheMap
                                )
                                val iconVector = IconHelper.getIconByName(iconName)
                                val tint = when (tx.type) {
                                    TransactionType.EXPENSE -> SolidExpense
                                    TransactionType.INCOME -> SolidIncome
                                    TransactionType.TRANSFER -> SolidTransfer
                                }
                                val formattedAmt = (if (tx.type == TransactionType.INCOME) "+" else if (tx.type == TransactionType.EXPENSE) "-" else "") +
                                        LanguageHelper.formatCurrency(tx.amount, languageMode)
                                val catDisplay = item.subCategory?.localizedName(languageMode)
                                    ?: item.category?.localizedName(languageMode)
                                    ?: tx.type.name

                                MasterSearchResultRow(
                                    result = MasterSearchResult.TransactionItem(
                                        transactionWithDetails = item,
                                        formattedAmount = formattedAmt,
                                        formattedDate = DateUtils.formatDate(tx.dateEpochMs, languageMode),
                                        resolvedIconName = iconName,
                                        title = titleText,
                                        subtitle = "${DateUtils.formatDate(tx.dateEpochMs, languageMode)} • $catDisplay",
                                        groupTag = if (languageMode == LanguageMode.BANGLA) "লেনদেন" else "TRANSACTIONS",
                                        iconVector = iconVector,
                                        iconTint = tint
                                    ),
                                    onClick = {
                                        onDismiss()
                                        onTransactionClick(tx)
                                    }
                                )
                            }
                        }
                    }
                } else if (searchResults.isEmpty()) {
                    // No Results Found
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.size(64.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "\"$searchQuery\" এর জন্য কিছু পাওয়া যায়নি" else "No results found for \"$searchQuery\"",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA)
                                    "বানান পরীক্ষা করুন বা অন্য ফিল্টার ক্যাটাগরি চেষ্টা করুন।"
                                else
                                    "Check spelling or try searching across All categories.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                            if (selectedCategory != SearchFilterCategory.ALL) {
                                TextButton(onClick = { selectedCategory = SearchFilterCategory.ALL }) {
                                    Text(if (languageMode == LanguageMode.BANGLA) "সকল ক্যাটাগরিতে খুঁজুন" else "Search in All")
                                }
                            }
                        }
                    }
                } else {
                    // Search Results List Grouped cleanly
                    val groupedResults = remember(searchResults) {
                        searchResults.groupBy { it.groupTag }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        groupedResults.forEach { (groupTag, itemsList) ->
                            item(key = "header_$groupTag") {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 10.dp, bottom = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = groupTag,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "${itemsList.size}",
                                        fontSize = 11.5.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }

                            items(itemsList, key = { it.id }) { result ->
                                MasterSearchResultRow(
                                    result = result,
                                    onClick = {
                                        onDismiss()
                                        when (result) {
                                            is MasterSearchResult.TransactionItem -> {
                                                onTransactionClick(result.transactionWithDetails.transaction)
                                            }
                                            is MasterSearchResult.AccountItem -> {
                                                onAccountClick(result.account)
                                            }
                                            is MasterSearchResult.CategoryItem -> {
                                                onNavigate(AppView.CATEGORIES)
                                            }
                                            is MasterSearchResult.BudgetItem -> {
                                                onNavigate(AppView.BUDGET)
                                            }
                                            is MasterSearchResult.SavingsGoalItem -> {
                                                onNavigate(AppView.SAVINGS_GOALS)
                                            }
                                            is MasterSearchResult.RecurringBillItem -> {
                                                onNavigate(AppView.RECURRING_BILLS)
                                            }
                                            is MasterSearchResult.WishlistResultItem -> {
                                                onNavigate(AppView.WISHLIST)
                                            }
                                            is MasterSearchResult.NavigationActionItem -> {
                                                if (result.actionType == "CALCULATOR") {
                                                    onOpenCalculator()
                                                } else if (result.targetSettingsSubPage != null) {
                                                    onNavigateToSettingsSubPage(result.targetSettingsSubPage)
                                                } else if (result.targetView != null) {
                                                    onNavigate(result.targetView)
                                                }
                                            }
                                        }
                                    }
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
private fun MasterSearchResultRow(
    result: MasterSearchResult,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = result.iconTint?.copy(alpha = 0.12f) ?: MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = result.iconVector,
                        contentDescription = null,
                        tint = result.iconTint ?: MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = result.title,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!result.subtitle.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = result.subtitle!!,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // For Budget or Goal: Progress bar
                if (result is MasterSearchResult.BudgetItem) {
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { result.progressPercent },
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(4.dp)
                            .clip(CircleShape),
                        color = if (result.isOverBudget) SolidExpense else SolidPrimary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                } else if (result is MasterSearchResult.SavingsGoalItem) {
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { result.progressPercent },
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(4.dp)
                            .clip(CircleShape),
                        color = SolidIncome,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }

            // Right side amount / badge
            when (result) {
                is MasterSearchResult.TransactionItem -> {
                    Text(
                        text = result.formattedAmount,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = result.iconTint ?: MaterialTheme.colorScheme.onSurface
                    )
                }
                is MasterSearchResult.AccountItem -> {
                    Text(
                        text = result.balanceFormatted,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = result.iconTint ?: MaterialTheme.colorScheme.onSurface
                    )
                }
                is MasterSearchResult.BudgetItem -> {
                    Text(
                        text = result.budgetedFormatted,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                is MasterSearchResult.SavingsGoalItem -> {
                    Text(
                        text = result.targetFormatted,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SolidIncome
                    )
                }
                is MasterSearchResult.RecurringBillItem -> {
                    Text(
                        text = result.amountFormatted,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = SolidExpense
                    )
                }
                is MasterSearchResult.WishlistResultItem -> {
                    Text(
                        text = result.amountFormatted,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = SolidPrimary
                    )
                }
                is MasterSearchResult.NavigationActionItem -> {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.padding(start = 6.dp)
                    ) {
                        Text(
                            text = "Open",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
                else -> {}
            }
        }
    }
}
