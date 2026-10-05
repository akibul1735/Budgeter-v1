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
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.runtime.produceState
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
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidPrimary
import com.example.ui.theme.SolidTransfer
import com.example.util.DateUtils
import com.example.util.IconHelper
import com.example.util.ItemCacheHelper
import com.example.util.LanguageHelper
import com.example.util.SearchMatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

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
    open val iconName: String? = null

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
        override val iconTint: Color?,
        override val iconName: String? = resolvedIconName
    ) : MasterSearchResult()

    data class AccountItem(
        val account: Account,
        val balanceFormatted: String,
        override val id: String = "acc_${account.id}",
        override val title: String,
        override val subtitle: String?,
        override val groupTag: String,
        override val iconVector: ImageVector,
        override val iconTint: Color?,
        override val iconName: String? = account.iconName
    ) : MasterSearchResult()

    data class CategoryItem(
        val category: Category,
        val isSubCategory: Boolean,
        override val id: String = "cat_${category.id}",
        override val title: String,
        override val subtitle: String?,
        override val groupTag: String,
        override val iconVector: ImageVector,
        override val iconTint: Color?,
        override val iconName: String? = category.iconName
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
        override val iconTint: Color?,
        override val iconName: String? = null
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
        override val iconTint: Color?,
        override val iconName: String? = goalWithDetails.goal.iconName
    ) : MasterSearchResult()

    data class RecurringBillItem(
        val billWithDetails: RecurringBillWithDetails,
        val amountFormatted: String,
        override val id: String = "bill_${billWithDetails.bill.id}",
        override val title: String,
        override val subtitle: String?,
        override val groupTag: String,
        override val iconVector: ImageVector,
        override val iconTint: Color?,
        override val iconName: String? = billWithDetails.category?.iconName ?: "ReceiptLong"
    ) : MasterSearchResult()

    data class WishlistResultItem(
        val wishlistItem: WishlistItemWithCategory,
        val amountFormatted: String,
        override val id: String = "wish_${wishlistItem.item.id}",
        override val title: String,
        override val subtitle: String?,
        override val groupTag: String,
        override val iconVector: ImageVector,
        override val iconTint: Color?,
        override val iconName: String? = wishlistItem.category?.iconName
    ) : MasterSearchResult()

    data class NavigationActionItem(
        val targetView: AppView? = null,
        val targetSettingsSubPage: SettingsSubPage? = null,
        val targetSettingsTab: Int? = null,
        val highlightKey: String? = null,
        val actionType: String = "",
        val keywords: List<String> = emptyList(),
        override val id: String,
        override val title: String,
        override val subtitle: String?,
        override val groupTag: String,
        override val iconVector: ImageVector,
        override val iconTint: Color? = null,
        override val iconName: String? = null
    ) : MasterSearchResult()
}

private data class IndexedEntity(
    val result: MasterSearchResult,
    val fields: List<SearchMatcher.SearchField>,
    val dateEpochMs: Long = 0L,
    val secondarySortKey: String = result.title,
    val categoryFilter: SearchFilterCategory
)

private data class SearchGroupResult(
    val groupTag: String,
    val items: List<MasterSearchResult>,
    val topScore: Double
)

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
    onNavigateToSettingsSubPage: (SettingsSubPage, Int?, String?) -> Unit = { _, _, _ -> }
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(SearchFilterCategory.ALL) }
    var expandedGroups by remember { mutableStateOf(setOf<String>()) }
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

    // Precomputed action catalog with bilingual keywords
    val actionCatalog = remember(languageMode, primaryColor, tertiaryColor, errorColor) {
        listOf(
            // Settings & Defaults
            MasterSearchResult.NavigationActionItem(
                id = "set_default_names",
                title = if (languageMode == LanguageMode.BANGLA) "ডিফল্ট নাম / নামহীন লেনদেন মোড" else "Default Payee Names / Unnamed Payee",
                subtitle = if (languageMode == LanguageMode.BANGLA) "ব্যক্তির নামহীন লেনদেনে স্বয়ংক্রিয় ডিফল্ট নাম বসানো (যেমন অন্যান্য বা কাস্টম)" else "Configure default fallback name for transactions without payee",
                groupTag = if (languageMode == LanguageMode.BANGLA) "সেটিংস ও ডিফল্ট" else "SETTINGS & DEFAULTS",
                iconVector = Icons.Default.EditNote,
                iconTint = primaryColor,
                targetSettingsSubPage = SettingsSubPage.TRANSACTION_SETUP,
                targetSettingsTab = 1,
                keywords = listOf("default", "default names", "default name", "default payee", "unnamed payee", "unnamed", "payee name", "payee", "নাম", "ডিফল্ট নাম", "ডিফল্ট পেয়ি", "নামহীন", "অন্যান্য")
            ),
            MasterSearchResult.NavigationActionItem(
                id = "set_custom_default_name",
                title = if (languageMode == LanguageMode.BANGLA) "কাস্টম ডিফল্ট নাম" else "Custom Default Payee Name",
                subtitle = if (languageMode == LanguageMode.BANGLA) "ডিফল্ট পেয়ি নাম নিজের পছন্দমতো লিখুন (যেমন বিবিধ, অন্যান্য)" else "Set your own custom default payee label for transactions",
                groupTag = if (languageMode == LanguageMode.BANGLA) "সেটিংস ও ডিফল্ট" else "SETTINGS & DEFAULTS",
                iconVector = Icons.Default.EditNote,
                iconTint = primaryColor,
                targetSettingsSubPage = SettingsSubPage.TRANSACTION_SETUP,
                targetSettingsTab = 1,
                keywords = listOf("default", "default name", "custom default", "custom default payee", "custom payee", "others", "ডিফল্ট", "ডিফল্ট নাম", "কাস্টম ডিফল্ট", "অন্যান্য", "নাম")
            ),
            MasterSearchResult.NavigationActionItem(
                id = "set_default_account",
                title = if (languageMode == LanguageMode.BANGLA) "ডিফল্ট অ্যাকাউন্ট নির্বাচন" else "Default Pre-selected Account",
                subtitle = if (languageMode == LanguageMode.BANGLA) "নতুন লেনদেন তৈরির সময় যে অ্যাকাউন্ট স্বয়ংক্রিয়ভাবে নির্বাচিত থাকবে" else "Pre-selected account for new transaction entries",
                groupTag = if (languageMode == LanguageMode.BANGLA) "সেটিংস ও ডিফল্ট" else "SETTINGS & DEFAULTS",
                iconVector = Icons.Default.AccountBalance,
                iconTint = primaryColor,
                targetSettingsSubPage = SettingsSubPage.TRANSACTION_SETUP,
                targetSettingsTab = 1,
                keywords = listOf("default", "default account", "preselected account", "initial account", "ডিফল্ট", "ডিফল্ট অ্যাকাউন্ট", "অ্যাকাউন্ট")
            ),
            MasterSearchResult.NavigationActionItem(
                id = "set_default_income_cat",
                title = if (languageMode == LanguageMode.BANGLA) "ডিফল্ট আয় ক্যাটাগরি" else "Default Income Category",
                subtitle = if (languageMode == LanguageMode.BANGLA) "নতুন আয় এন্ট্রি করার সময় যে ক্যাটাগরি ডিফল্টভাবে বসবে" else "Pre-selected category for new income entries",
                groupTag = if (languageMode == LanguageMode.BANGLA) "সেটিংস ও ডিফল্ট" else "SETTINGS & DEFAULTS",
                iconVector = Icons.Default.Category,
                iconTint = SolidIncome,
                targetSettingsSubPage = SettingsSubPage.TRANSACTION_SETUP,
                targetSettingsTab = 1,
                keywords = listOf("default", "default income", "income category", "default category", "ডিফল্ট", "ডিফল্ট আয়", "ক্যাটাগরি")
            ),
            MasterSearchResult.NavigationActionItem(
                id = "set_default_expense_cat",
                title = if (languageMode == LanguageMode.BANGLA) "ডিফল্ট ব্যয় ক্যাটাগরি" else "Default Expense Category",
                subtitle = if (languageMode == LanguageMode.BANGLA) "নতুন ব্যয় এন্ট্রি করার সময় যে ক্যাটাগরি ডিফল্টভাবে বসবে" else "Pre-selected category for new expense entries",
                groupTag = if (languageMode == LanguageMode.BANGLA) "সেটিংস ও ডিফল্ট" else "SETTINGS & DEFAULTS",
                iconVector = Icons.Default.Category,
                iconTint = SolidExpense,
                targetSettingsSubPage = SettingsSubPage.TRANSACTION_SETUP,
                targetSettingsTab = 1,
                keywords = listOf("default", "default expense", "expense category", "default category", "ডিফল্ট", "ডিফল্ট ব্যয়", "ডিফল্ট খরচ", "ক্যাটাগরি")
            ),
            MasterSearchResult.NavigationActionItem(
                id = "set_defaults_display_tab",
                title = if (languageMode == LanguageMode.BANGLA) "ডিফল্ট ও প্রদর্শন সেটিংস" else "Defaults & Display Settings",
                subtitle = if (languageMode == LanguageMode.BANGLA) "ডিফল্ট অ্যাকাউন্ট, ক্যাটাগরি, দ্রুত মোড ও প্রদর্শন পছন্দ" else "Pre-selected accounts, categories, quick mode & display defaults",
                groupTag = if (languageMode == LanguageMode.BANGLA) "সেটিংস ও ডিফল্ট" else "SETTINGS & DEFAULTS",
                iconVector = Icons.Default.Tune,
                iconTint = primaryColor,
                targetSettingsSubPage = SettingsSubPage.TRANSACTION_SETUP,
                targetSettingsTab = 1,
                keywords = listOf("default", "defaults", "defaults and display", "display settings", "ডিফল্ট", "ডিফল্ট ও প্রদর্শন", "প্রদর্শন")
            ),
            MasterSearchResult.NavigationActionItem(
                id = "set_default_tx_type",
                title = if (languageMode == LanguageMode.BANGLA) "ডিফল্ট লেনদেন প্রকার" else "Default Transaction Type",
                subtitle = if (languageMode == LanguageMode.BANGLA) "এন্ট্রি ওপেন করার সময় ব্যয়, আয় নাকি ট্রান্সফার খুলবে" else "Initial tab when adding transaction (Expense, Income, or Transfer)",
                groupTag = if (languageMode == LanguageMode.BANGLA) "সেটিংস ও ডিফল্ট" else "SETTINGS & DEFAULTS",
                iconVector = Icons.Default.SwapHoriz,
                iconTint = primaryColor,
                targetSettingsSubPage = SettingsSubPage.TRANSACTION_SETUP,
                targetSettingsTab = 1,
                keywords = listOf("default", "default type", "transaction type", "expense", "income", "transfer", "ব্যয়", "আয়", "ট্রান্সফার", "ডিফল্ট টাইপ")
            ),
            MasterSearchResult.NavigationActionItem(
                id = "set_quick_tx_mode",
                title = if (languageMode == LanguageMode.BANGLA) "দ্রুত লেনদেন মোড" else "Quick Transaction Mode",
                subtitle = if (languageMode == LanguageMode.BANGLA) "শীট বন্ধ না করে ১ ট্যাপে দ্রুত লেনদেন সেভ করার সুবিধা" else "Save transactions in 1 tap without closing the sheet",
                groupTag = if (languageMode == LanguageMode.BANGLA) "সেটিংস ও ডিফল্ট" else "SETTINGS & DEFAULTS",
                iconVector = Icons.Default.AddCircleOutline,
                iconTint = primaryColor,
                targetSettingsSubPage = SettingsSubPage.TRANSACTION_SETUP,
                targetSettingsTab = 1,
                keywords = listOf("quick transaction", "quick mode", "fast entry", "1 tap", "দ্রুত লেনদেন", "১ ট্যাপ")
            ),
            MasterSearchResult.NavigationActionItem(
                id = "set_autofocus_amount",
                title = if (languageMode == LanguageMode.BANGLA) "পরিমাণ ফিল্ডে অটো-ফোকাস" else "Auto-Focus Amount Keyboard",
                subtitle = if (languageMode == LanguageMode.BANGLA) "লেনদেন তৈরির সময় স্বয়ংক্রিয়ভাবে টাকার অঙ্কে কিবোর্ড চালু" else "Automatically focus amount field and show numeric keyboard",
                groupTag = if (languageMode == LanguageMode.BANGLA) "সেটিংস ও ডিফল্ট" else "SETTINGS & DEFAULTS",
                iconVector = Icons.Default.Numbers,
                iconTint = primaryColor,
                targetSettingsSubPage = SettingsSubPage.TRANSACTION_SETUP,
                targetSettingsTab = 1,
                keywords = listOf("auto focus", "focus amount", "keyboard focus", "অটো ফোকাস", "কিবোর্ড", "টাকার অঙ্ক")
            ),
            MasterSearchResult.NavigationActionItem(
                id = "set_input_calendar",
                title = if (languageMode == LanguageMode.BANGLA) "লেনদেন ইনপুট ও ক্যালেন্ডার" else "Transaction Input & Calendar",
                subtitle = if (languageMode == LanguageMode.BANGLA) "দ্রুত তারিখ চয়ন, ১-ট্যাপ নির্বাচন ও কিবোর্ড আচরণ" else "Fast date presets, 1-tap auto-selection & keyboard behavior",
                groupTag = if (languageMode == LanguageMode.BANGLA) "সেটিংস ও ডিফল্ট" else "SETTINGS & DEFAULTS",
                iconVector = Icons.Default.CalendarToday,
                iconTint = primaryColor,
                targetSettingsSubPage = SettingsSubPage.TRANSACTION_SETUP,
                targetSettingsTab = 0,
                keywords = listOf("input", "calendar picker", "date picker", "transaction input", "ইনপুট", "তারিখ নির্বাচন", "ক্যালেন্ডার")
            ),
            MasterSearchResult.NavigationActionItem(
                id = "set_repeat_entry",
                title = if (languageMode == LanguageMode.BANGLA) "+১ ধারাবাহিক এন্ট্রি মোড" else "+1 Repeat Continuous Entry",
                subtitle = if (languageMode == LanguageMode.BANGLA) "একের পর এক দ্রুত লেনদেন এন্ট্রি করার সুবিধা" else "Keep form open for rapid consecutive transactions",
                groupTag = if (languageMode == LanguageMode.BANGLA) "সেটিংস ও ডিফল্ট" else "SETTINGS & DEFAULTS",
                iconVector = Icons.Default.AddCircleOutline,
                iconTint = primaryColor,
                targetSettingsSubPage = SettingsSubPage.TRANSACTION_SETUP,
                targetSettingsTab = 2,
                keywords = listOf("repeat entry", "continuous entry", "+1 entry", "ধারাবাহিক", "একটানা এন্ট্রি", "ধারাবাহিক এন্ট্রি")
            ),
            MasterSearchResult.NavigationActionItem(
                id = "set_payment_sources",
                title = if (languageMode == LanguageMode.BANGLA) "পেমেন্ট মাধ্যম কনফিগারেশন" else "Payment Sources Setup",
                subtitle = if (languageMode == LanguageMode.BANGLA) "ক্যাশ, ব্যাংক ও মোবাইল ব্যাংকিং (বিকাশ/নগদ) নির্বাচন" else "Customize Cash, Bank, and Mobile Banking accounts",
                groupTag = if (languageMode == LanguageMode.BANGLA) "সেটিংস ও ডিফল্ট" else "SETTINGS & DEFAULTS",
                iconVector = Icons.Default.Payments,
                iconTint = primaryColor,
                targetSettingsSubPage = SettingsSubPage.PAYMENT_SOURCES,
                keywords = listOf("payment sources", "payment source", "cash", "bank", "mfs", "bkash", "nagad", "rocket", "পেমেন্ট মাধ্যম", "পেমেন্ট সোর্স", "ক্যাশ", "ব্যাংক", "বিকাশ", "নগদ")
            ),
            MasterSearchResult.NavigationActionItem(
                id = "set_smart_autofill",
                title = if (languageMode == LanguageMode.BANGLA) "স্মার্ট অটোফিল সেটিংস" else "Smart Autofill Rules",
                subtitle = if (languageMode == LanguageMode.BANGLA) "লেনদেন রুল ও ক্যাটাগরি স্বয়ংক্রিয় নির্ধারণ" else "Auto-categorize transactions & payee keyword rules",
                groupTag = if (languageMode == LanguageMode.BANGLA) "সেটিংস ও ডিফল্ট" else "SETTINGS & DEFAULTS",
                iconVector = Icons.Default.Tune,
                iconTint = primaryColor,
                targetSettingsSubPage = SettingsSubPage.SMART_AUTOFILL,
                keywords = listOf("autofill", "smart autofill", "rules", "auto categorize", "payee", "অটোফিল", "স্মার্ট অটোফিল", "রুল")
            ),
            MasterSearchResult.NavigationActionItem(
                id = "set_payee_memory",
                title = if (languageMode == LanguageMode.BANGLA) "পেয়ি মেমোরি ও অটো সাজেস্ট" else "Payee Memory & Suggestion",
                subtitle = if (languageMode == LanguageMode.BANGLA) "পূর্বে ব্যবহৃত পেয়ি ও অ্যাকাউন্ট স্মরণ রাখা" else "Remember previously used payees and last used accounts",
                groupTag = if (languageMode == LanguageMode.BANGLA) "সেটিংস ও ডিফল্ট" else "SETTINGS & DEFAULTS",
                iconVector = Icons.Default.Tune,
                iconTint = primaryColor,
                targetSettingsSubPage = SettingsSubPage.SMART_AUTOFILL,
                keywords = listOf("payee memory", "payee suggestion", "autofill payee", "পেয়ি মেমোরি", "সাজেস্ট")
            ),

            // Appearance & Themes
            MasterSearchResult.NavigationActionItem(
                id = "set_themes",
                title = if (languageMode == LanguageMode.BANGLA) "থিম ও রূপরেখা কাস্টমাইজেশন" else "Themes & Appearance",
                subtitle = if (languageMode == LanguageMode.BANGLA) "ডার্ক মোড, কালার প্যালেট ও ফ্রেম" else "Dark/Light mode, Emerald palette & M3 dynamic",
                groupTag = if (languageMode == LanguageMode.BANGLA) "সেটিংস ও রূপরেখা" else "SETTINGS & THEMES",
                iconVector = Icons.Default.Palette,
                iconTint = primaryColor,
                targetSettingsSubPage = SettingsSubPage.THEMES,
                keywords = listOf("theme", "themes", "appearance", "dark mode", "light mode", "color", "palette", "font", "থিম", "ডার্ক মোড", "লাইট মোড", "রং", "প্যালেট", "ফন্ট")
            ),
            MasterSearchResult.NavigationActionItem(
                id = "set_dark_mode",
                title = if (languageMode == LanguageMode.BANGLA) "ডার্ক মোড / লাইট মোড" else "Dark Mode / Light Mode",
                subtitle = if (languageMode == LanguageMode.BANGLA) "সিস্টেম, ডার্ক মোড অথবা লাইট মোড পছন্দ" else "Switch System, Pure Dark, or Pure Light mode",
                groupTag = if (languageMode == LanguageMode.BANGLA) "সেটিংস ও রূপরেখা" else "SETTINGS & THEMES",
                iconVector = Icons.Default.Palette,
                iconTint = primaryColor,
                targetSettingsSubPage = SettingsSubPage.THEMES,
                keywords = listOf("dark mode", "light mode", "system mode", "night mode", "mode", "ডার্ক মোড", "লাইট মোড", "নাইট মোড")
            ),
            MasterSearchResult.NavigationActionItem(
                id = "set_color_palette",
                title = if (languageMode == LanguageMode.BANGLA) "কালার প্যালেট ও থিম স্টাইল" else "Color Palette & Accents",
                subtitle = if (languageMode == LanguageMode.BANGLA) "অবসিডিয়ান, এমেরাল্ড গোল্ড ও ম্যাটেরিয়াল ইউ" else "Obsidian Slate, Emerald Fintech, Royal Gold & Material You",
                groupTag = if (languageMode == LanguageMode.BANGLA) "সেটিংস ও রূপরেখা" else "SETTINGS & THEMES",
                iconVector = Icons.Default.Palette,
                iconTint = primaryColor,
                targetSettingsSubPage = SettingsSubPage.THEMES,
                keywords = listOf("palette", "colors", "emerald", "gold", "slate", "কালার", "প্যালেট", "এমেরাল্ড")
            ),
            MasterSearchResult.NavigationActionItem(
                id = "set_currency",
                title = if (languageMode == LanguageMode.BANGLA) "মুদ্রা ও কারেন্সি কনফিগারেশন" else "Currency Setup",
                subtitle = if (languageMode == LanguageMode.BANGLA) "টাকা (৳), ডলার ($) বা কাস্টম কারেন্সি কোড ও প্রতীক" else "Taka (৳), Dollar ($) & custom currency symbol placement",
                groupTag = if (languageMode == LanguageMode.BANGLA) "ফরম্যাট ও মুদ্রা" else "REGIONAL & FORMATTING",
                iconVector = Icons.Default.CurrencyExchange,
                iconTint = primaryColor,
                targetSettingsSubPage = SettingsSubPage.CURRENCY,
                keywords = listOf("currency", "taka", "টাকা", "bdt", "usd", "dollar", "symbol", "মুদ্রা", "প্রতীক", "কোড")
            ),
            MasterSearchResult.NavigationActionItem(
                id = "set_amount_format",
                title = if (languageMode == LanguageMode.BANGLA) "টাকার কমা ও ডেসিমাল ফরম্যাট" else "Amount Format & Comma Separator",
                subtitle = if (languageMode == LanguageMode.BANGLA) "ভারতীয়/বাংলাদেশি লাখ-কোটি (১,০০,০০০) বনাম আন্তর্জাতিক মিলিয়ন" else "Lakh/Crore vs International Million separators & decimals",
                groupTag = if (languageMode == LanguageMode.BANGLA) "ফরম্যাট ও মুদ্রা" else "REGIONAL & FORMATTING",
                iconVector = Icons.Default.Numbers,
                iconTint = primaryColor,
                targetSettingsSubPage = SettingsSubPage.AMOUNT_FORMAT,
                keywords = listOf("amount format", "comma", "separator", "decimal", "lakh", "crore", "কমা", "সেপারেটর", "দশমিক", "লাখ", "কোটি")
            ),
            MasterSearchResult.NavigationActionItem(
                id = "set_date_time",
                title = if (languageMode == LanguageMode.BANGLA) "তারিখ ও সময় সেটিংস" else "Date & Time Settings",
                subtitle = if (languageMode == LanguageMode.BANGLA) "তারিখের প্যাটার্ন (DD/MM/YYYY) ও সপ্তাহের শুরুর দিন" else "Date format patterns & first day of week",
                groupTag = if (languageMode == LanguageMode.BANGLA) "ফরম্যাট ও মুদ্রা" else "REGIONAL & FORMATTING",
                iconVector = Icons.Default.CalendarToday,
                iconTint = primaryColor,
                targetSettingsSubPage = SettingsSubPage.DATE_TIME,
                keywords = listOf("date", "time", "date format", "pattern", "first day", "তারিখ", "সময়", "প্যাটার্ন", "সপ্তাহ")
            ),
            MasterSearchResult.NavigationActionItem(
                id = "set_language",
                title = if (languageMode == LanguageMode.BANGLA) "ভাষা পছন্দ (বাংলা / English)" else "Language Preference (Bangla / English)",
                subtitle = if (languageMode == LanguageMode.BANGLA) "অ্যাপের ডিসপ্লে ভাষা পরিবর্তন করুন" else "Switch application UI language",
                groupTag = if (languageMode == LanguageMode.BANGLA) "ফরম্যাট ও মুদ্রা" else "REGIONAL & FORMATTING",
                iconVector = Icons.Default.Language,
                iconTint = primaryColor,
                targetSettingsSubPage = SettingsSubPage.LANGUAGE,
                keywords = listOf("language", "english", "bangla", "ভাষা", "বাংলা", "ইংরেজি", "locale")
            ),
            MasterSearchResult.NavigationActionItem(
                id = "set_security_lock",
                title = if (languageMode == LanguageMode.BANGLA) "পাসওয়ার্ড, পিন ও ফিঙ্গারপ্রিন্ট" else "PIN, Biometric & App Lock",
                subtitle = if (languageMode == LanguageMode.BANGLA) "অ্যাপ সুরক্ষা, বায়োমেট্রিক ও সংবেদনশীল একশন নিশ্চিতকরণ" else "Secure app with PIN, fingerprint & action confirmation",
                groupTag = if (languageMode == LanguageMode.BANGLA) "নিরাপত্তা ও সুরক্ষা" else "SECURITY & LOCK",
                iconVector = Icons.Default.Fingerprint,
                iconTint = primaryColor,
                targetSettingsSubPage = SettingsSubPage.SECURITY,
                keywords = listOf("pin", "fingerprint", "biometric", "password", "lock", "security", "পিন", "ফিঙ্গারপ্রিন্ট", "বায়োমেট্রিক", "পাসওয়ার্ড", "লক", "নিরাপত্তা")
            ),
            MasterSearchResult.NavigationActionItem(
                id = "set_notifications",
                title = if (languageMode == LanguageMode.BANGLA) "ফোন নোটিফিকেশন ও অ্যালার্ম" else "Phone Notifications & Reminders",
                subtitle = if (languageMode == LanguageMode.BANGLA) "দৈনিক খরচ এন্ট্রি রিমাইন্ডার ও বিল সতর্কবার্তা" else "Daily expense tracking reminders & recurring bill alarms",
                groupTag = if (languageMode == LanguageMode.BANGLA) "নিরাপত্তা ও সুরক্ষা" else "NOTIFICATIONS",
                iconVector = Icons.Default.NotificationsNone,
                iconTint = primaryColor,
                targetSettingsSubPage = SettingsSubPage.NOTIFICATIONS,
                keywords = listOf("notification", "reminder", "alarm", "bill alert", "daily reminder", "নোটিফিকেশন", "রিমাইন্ডার", "অ্যালার্ম")
            ),
            MasterSearchResult.NavigationActionItem(
                id = "set_backup_sync",
                title = if (languageMode == LanguageMode.BANGLA) "ক্লাউড সিঙ্ক ও ব্যাকআপ" else "Backup & Cloud Sync",
                subtitle = if (languageMode == LanguageMode.BANGLA) "গুগল ড্রাইভ, ড্রপবক্স ও লোকাল ব্যাকআপ রিস্টোর" else "Google Drive, Dropbox & Local backup restore",
                groupTag = if (languageMode == LanguageMode.BANGLA) "ডাটা ও ব্যাকআপ" else "DATA & BACKUP",
                iconVector = Icons.Default.CloudSync,
                iconTint = primaryColor,
                targetView = AppView.BACKUP_SYNC,
                keywords = listOf("backup", "sync", "cloud", "google drive", "drive", "dropbox", "local", "restore", "ব্যাকআপ", "সিঙ্ক", "ড্রাইভ", "রিস্টোর")
            ),
            MasterSearchResult.NavigationActionItem(
                id = "set_archive_prune",
                title = if (languageMode == LanguageMode.BANGLA) "আর্কাইভ ও ডাটা প্রুনিং" else "Archive & Data Pruning",
                subtitle = if (languageMode == LanguageMode.BANGLA) "ব্যালেন্স ঠিক রেখে পুরাতন লেনদেন আর্কাইভ ও প্রুন" else "Fiscal year roll-forward & encrypted archive",
                groupTag = if (languageMode == LanguageMode.BANGLA) "ডাটা ও ব্যাকআপ" else "DATA & BACKUP",
                iconVector = Icons.Default.Archive,
                iconTint = primaryColor,
                targetView = AppView.ARCHIVE_PRUNE,
                keywords = listOf("archive", "prune", "fiscal year", "shrink db", "roll-forward", "আর্কাইভ", "প্রুন", "পুরাতন ডাটা")
            ),
            MasterSearchResult.NavigationActionItem(
                id = "set_reset",
                title = if (languageMode == LanguageMode.BANGLA) "রিসেট ও ক্লিন ডাটা" else "Reset & Wipe Data",
                subtitle = if (languageMode == LanguageMode.BANGLA) "লেনদেন মোছা বা ফ্যাক্টরি ক্লিন রিসেট" else "Clear transactions or factory reset application",
                groupTag = if (languageMode == LanguageMode.BANGLA) "ডাটা ও ব্যাকআপ" else "DATA & BACKUP",
                iconVector = Icons.Default.DeleteSweep,
                iconTint = errorColor,
                targetView = AppView.RESET,
                keywords = listOf("reset", "wipe", "delete", "factory reset", "clear transactions", "রিসেট", "মুছুন", "পরিষ্কার")
            ),
            MasterSearchResult.NavigationActionItem(
                id = "set_faq",
                title = if (languageMode == LanguageMode.BANGLA) "সহায়তা ও প্রশ্নোত্তর (FAQ)" else "User Guide & FAQ",
                subtitle = if (languageMode == LanguageMode.BANGLA) "ব্যবহার নির্দেশিকা ও সাধারণ জিজ্ঞাসার উত্তর" else "Answers to common questions and tutorial guides",
                groupTag = if (languageMode == LanguageMode.BANGLA) "সহায়তা ও তথ্য" else "SUPPORT & ABOUT",
                iconVector = Icons.Default.HelpOutline,
                iconTint = primaryColor,
                targetSettingsSubPage = SettingsSubPage.FAQ,
                keywords = listOf("faq", "help", "guide", "user guide", "tutorial", "support", "সহায়তা", "প্রশ্নোত্তর", "গাইড")
            ),
            MasterSearchResult.NavigationActionItem(
                id = "set_about",
                title = if (languageMode == LanguageMode.BANGLA) "অ্যাপ সংস্করণ ও তথ্য" else "App Version & About",
                subtitle = if (languageMode == LanguageMode.BANGLA) "বাজেটার অ্যাপ ভার্সন ও ডেভেলপমেন্ট তথ্য" else "Budgeter Release (100% Offline-First Fintech)",
                groupTag = if (languageMode == LanguageMode.BANGLA) "সহায়তা ও তথ্য" else "SUPPORT & ABOUT",
                iconVector = Icons.Default.Info,
                iconTint = primaryColor,
                targetSettingsSubPage = SettingsSubPage.ABOUT,
                keywords = listOf("about", "version", "developer", "release", "update", "সম্পর্কে", "ভার্সন", "রিলিজ")
            ),

            // Navigation and Tools
            MasterSearchResult.NavigationActionItem(
                id = "nav_dashboard",
                title = if (languageMode == LanguageMode.BANGLA) "ড্যাশবোর্ড ও আর্থিক সারসংক্ষেপ" else "Dashboard & Overview",
                subtitle = if (languageMode == LanguageMode.BANGLA) "মোট সম্পদ, খরচযোগ্য অর্থ ও চার্ট" else "Net worth, expendable balance & summaries",
                groupTag = if (languageMode == LanguageMode.BANGLA) "নেভিগেশন ও টুলস" else "APPS & TOOLS",
                iconVector = Icons.Default.PieChart,
                iconTint = primaryColor,
                targetView = AppView.DASHBOARD,
                keywords = listOf("dashboard", "overview", "net worth", "home", "ড্যাশবোর্ড", "মোট সম্পদ")
            ),
            MasterSearchResult.NavigationActionItem(
                id = "nav_ledger",
                title = if (languageMode == LanguageMode.BANGLA) "লেনদেন ও খতিয়ান" else "Ledger & Transactions",
                subtitle = if (languageMode == LanguageMode.BANGLA) "সকল আয় ও ব্যয়ের তালিকা ও ফিল্টার" else "View and filter all transactions statement",
                groupTag = if (languageMode == LanguageMode.BANGLA) "নেভিগেশন ও টুলস" else "APPS & TOOLS",
                iconVector = Icons.Default.Receipt,
                iconTint = primaryColor,
                targetView = AppView.LEDGER,
                keywords = listOf("ledger", "transactions", "statement", "history", "লেজার", "লেনদেন", "বিবরণী")
            ),
            MasterSearchResult.NavigationActionItem(
                id = "nav_cash_flow",
                title = if (languageMode == LanguageMode.BANGLA) "ক্যাশ ফ্লো ও প্রবাহ বিশ্লেষণ" else "Cash Flow & Analytics",
                subtitle = if (languageMode == LanguageMode.BANGLA) "মাসিক আয়, ব্যয় ও নিট সঞ্চয় ট্রেন্ড" else "Inflow, outflow & net cash flow trends",
                groupTag = if (languageMode == LanguageMode.BANGLA) "নেভিগেশন ও টুলস" else "APPS & TOOLS",
                iconVector = Icons.AutoMirrored.Filled.TrendingUp,
                iconTint = SolidIncome,
                targetView = AppView.CASH_FLOW,
                keywords = listOf("cash flow", "analytics", "analysis", "trend", "inflow", "outflow", "ক্যাশ ফ্লো", "প্রবাহ", "ট্রেন্ড")
            ),
            MasterSearchResult.NavigationActionItem(
                id = "nav_balance_sheet",
                title = if (languageMode == LanguageMode.BANGLA) "ব্যালেন্স শিট ও আর্থিক অবস্থান" else "Balance Sheet & Financial Position",
                subtitle = if (languageMode == LanguageMode.BANGLA) "মোট সম্পদ, দায় ও সমন্বিত ব্যালেন্সের হিসাব" else "Assets, liabilities, adjusted balances & net worth breakdown",
                groupTag = if (languageMode == LanguageMode.BANGLA) "নেভিগেশন ও টুলস" else "APPS & TOOLS",
                iconVector = Icons.Default.TableChart,
                iconTint = primaryColor,
                targetView = AppView.BALANCE_SHEET,
                keywords = listOf("balance sheet", "balance", "net worth", "assets", "liabilities", "statement", "ব্যালেন্স শিট", "সম্পদ", "দায়")
            ),
            MasterSearchResult.NavigationActionItem(
                id = "nav_accounts",
                title = if (languageMode == LanguageMode.BANGLA) "অ্যাকাউন্ট ও ফিন্যান্সিয়াল গ্রুপ" else "Accounts & Financial Groups",
                subtitle = if (languageMode == LanguageMode.BANGLA) "ব্যাংক, ক্যাশ, বিকাশ ও ওয়ালেট ব্যবস্থাপনা" else "Manage banks, mobile wallets, cash & group balances",
                groupTag = if (languageMode == LanguageMode.BANGLA) "নেভিগেশন ও টুলস" else "APPS & TOOLS",
                iconVector = Icons.Default.AccountBalance,
                iconTint = primaryColor,
                targetView = AppView.ACCOUNTS,
                keywords = listOf("accounts", "account", "bank", "wallet", "mfs", "অ্যাকাউন্ট", "ব্যাংক", "ওয়ালেট")
            ),
            MasterSearchResult.NavigationActionItem(
                id = "nav_categories",
                title = if (languageMode == LanguageMode.BANGLA) "ক্যাটাগরি ও সাব-ক্যাটাগরি" else "Categories & Sub-Categories",
                subtitle = if (languageMode == LanguageMode.BANGLA) "আয় ও ব্যয়ের খাত, আইকন ও কালার ট্যাগ" else "Income & expense categories, icons & color tags",
                groupTag = if (languageMode == LanguageMode.BANGLA) "নেভিগেশন ও টুলস" else "APPS & TOOLS",
                iconVector = Icons.Default.Category,
                iconTint = primaryColor,
                targetView = AppView.CATEGORIES,
                keywords = listOf("categories", "category", "subcategories", "expense category", "income category", "ক্যাটাগরি", "খাত")
            ),
            MasterSearchResult.NavigationActionItem(
                id = "nav_budget",
                title = if (languageMode == LanguageMode.BANGLA) "মাসিক বাজেট ও লিমিট" else "Monthly Budgets & Category Limits",
                subtitle = if (languageMode == LanguageMode.BANGLA) "মাসিক খরচের বাজেট নির্ধারণ ও ট্র্যাকিং" else "Set spending limits and track real-time utilization",
                groupTag = if (languageMode == LanguageMode.BANGLA) "নেভিগেশন ও টুলস" else "APPS & TOOLS",
                iconVector = Icons.Default.PieChart,
                iconTint = primaryColor,
                targetView = AppView.BUDGET,
                keywords = listOf("budget", "budgets", "monthly budget", "spending limit", "limit", "বাজেট", "লিমিট")
            ),
            MasterSearchResult.NavigationActionItem(
                id = "nav_savings_goals",
                title = if (languageMode == LanguageMode.BANGLA) "সঞ্চয় লক্ষ্য ও মাইলস্টোন" else "Savings Goals & Milestones",
                subtitle = if (languageMode == LanguageMode.BANGLA) "ভবিষ্যত লক্ষ্য ও জরুরি তহবিলের অগ্রগতি" else "Track targets, emergency funds & target deadlines",
                groupTag = if (languageMode == LanguageMode.BANGLA) "নেভিগেশন ও টুলস" else "APPS & TOOLS",
                iconVector = Icons.Default.Savings,
                iconTint = SolidIncome,
                targetView = AppView.SAVINGS_GOALS,
                keywords = listOf("savings", "savings goals", "goals", "target", "emergency fund", "সঞ্চয়", "লক্ষ্য")
            ),
            MasterSearchResult.NavigationActionItem(
                id = "nav_recurring_bills",
                title = if (languageMode == LanguageMode.BANGLA) "পুনরাবৃত্ত বিল ও সাবস্ক্রিপশন" else "Recurring Bills & Subscriptions",
                subtitle = if (languageMode == LanguageMode.BANGLA) "বিদ্যুৎ, বাড়ি ভাড়া ও নিয়মিত মাসিক বিল" else "Utility bills, rent, internet & recurring expenses",
                groupTag = if (languageMode == LanguageMode.BANGLA) "নেভিগেশন ও টুলস" else "APPS & TOOLS",
                iconVector = Icons.Default.EventRepeat,
                iconTint = SolidExpense,
                targetView = AppView.RECURRING_BILLS,
                keywords = listOf("recurring bills", "bills", "subscriptions", "bill", "বিল", "সাবস্ক্রিপশন")
            ),
            MasterSearchResult.NavigationActionItem(
                id = "nav_wishlist",
                title = if (languageMode == LanguageMode.BANGLA) "উইশলিস্ট ও কাঙ্ক্ষিত কেনাকাটা" else "Wishlist & Planned Purchases",
                subtitle = if (languageMode == LanguageMode.BANGLA) "ভবিষ্যতে কেনাকাটার তালিকা ও পরিকল্পনা" else "Plan upcoming purchases and convert directly to transactions",
                groupTag = if (languageMode == LanguageMode.BANGLA) "নেভিগেশন ও টুলস" else "APPS & TOOLS",
                iconVector = Icons.Default.Favorite,
                iconTint = SolidPrimary,
                targetView = AppView.WISHLIST,
                keywords = listOf("wishlist", "wish", "planned purchase", "shopping", "উইশলিস্ট", "কেনাকাটা")
            ),
            MasterSearchResult.NavigationActionItem(
                id = "nav_rm_manager",
                title = if (languageMode == LanguageMode.BANGLA) "ঋণ ও দেনা-পাওনা (RM Manager)" else "Loans & Debts (RM Manager)",
                subtitle = if (languageMode == LanguageMode.BANGLA) "ধার দেওয়া ও নেওয়া টাকার সঠিক হিসাব" else "Track borrowings, lendings & repayments",
                groupTag = if (languageMode == LanguageMode.BANGLA) "নেভিগেশন ও টুলস" else "APPS & TOOLS",
                iconVector = Icons.Default.Handshake,
                iconTint = SolidExpense,
                targetView = AppView.RM_MANAGER,
                keywords = listOf("rm manager", "loan", "debt", "borrow", "lend", "ধার", "দেনা", "পাওনা")
            ),
            MasterSearchResult.NavigationActionItem(
                id = "nav_trash",
                title = if (languageMode == LanguageMode.BANGLA) "ট্র্যাশ ও রিসাইকেল বিন" else "Trash & Recycle Bin",
                subtitle = if (languageMode == LanguageMode.BANGLA) "মুছে ফেলা লেনদেন পুনরুদ্ধার বা চিরতরে মুছে ফেলা" else "Restore or permanently delete removed transactions",
                groupTag = if (languageMode == LanguageMode.BANGLA) "নেভিগেশন ও টুলস" else "APPS & TOOLS",
                iconVector = Icons.Default.DeleteSweep,
                iconTint = errorColor,
                targetView = AppView.TRASH,
                keywords = listOf("trash", "recycle bin", "deleted", "restore", "ট্র্যাশ", "রিসাইকেল বিন")
            ),
            MasterSearchResult.NavigationActionItem(
                id = "tool_calculator",
                title = if (languageMode == LanguageMode.BANGLA) "পপআপ ক্যালকুলেটর" else "Standalone Calculator",
                subtitle = if (languageMode == LanguageMode.BANGLA) "দ্রুত হিসাব ও গাণিতিক হিসেব" else "Quick arithmetic & math calculations",
                groupTag = if (languageMode == LanguageMode.BANGLA) "নেভিগেশন ও টুলস" else "APPS & TOOLS",
                iconVector = Icons.Default.Calculate,
                iconTint = tertiaryColor,
                actionType = "CALCULATOR",
                keywords = listOf("calculator", "calc", "math", "ক্যালকুলেটর", "হিসাব", "গণনা")
            ),
            MasterSearchResult.NavigationActionItem(
                id = "tool_export",
                title = if (languageMode == LanguageMode.BANGLA) "এক্সেল ও সিএসভি এক্সপোর্ট" else "Export to Excel & CSV",
                subtitle = if (languageMode == LanguageMode.BANGLA) "লেনদেন ও ব্যালেন্স শিট এক্সেল ফাইলে এক্সপোর্ট করুন" else "Export transactions, accounts, and balance sheet to Excel/CSV",
                groupTag = if (languageMode == LanguageMode.BANGLA) "নেভিগেশন ও টুলস" else "APPS & TOOLS",
                iconVector = Icons.Default.FormatListBulleted,
                iconTint = primaryColor,
                targetView = AppView.LEDGER,
                keywords = listOf("export", "excel", "csv", "sheet", "report", "statement", "এক্সপোর্ট", "এক্সেল")
            )
        )
    }

    // Precompute normalized search indexes for all entities
    val indexedEntities = remember(
        transactions,
        allAccounts,
        accountsWithBalances,
        allCategories,
        monthlyBudgets,
        recurringBills,
        savingsGoals,
        wishlistItems,
        actionCatalog,
        languageMode,
        itemImageCacheMap
    ) {
        val list = mutableListOf<IndexedEntity>()

        // 1. Transactions Index
        transactions.forEach { item ->
            val tx = item.transaction
            val catNameEn = item.category?.nameEn.orEmpty()
            val catNameBn = item.category?.nameBn.orEmpty()
            val subCatNameEn = item.subCategory?.nameEn.orEmpty()
            val subCatNameBn = item.subCategory?.nameBn.orEmpty()
            val debitAccEn = item.debitAccount?.nameEn.orEmpty()
            val debitAccBn = item.debitAccount?.nameBn.orEmpty()
            val creditAccEn = item.creditAccount?.nameEn.orEmpty()
            val creditAccBn = item.creditAccount?.nameBn.orEmpty()
            val payee = tx.payeeOrPayer.orEmpty()
            val note = tx.note.orEmpty()
            val ref = tx.referenceNo.orEmpty()
            val amtStr = tx.amount.toString()
            val amtFormatted = LanguageHelper.formatCurrency(tx.amount, languageMode)
            val dateKeywords = SearchMatcher.generateDateKeywords(tx.dateEpochMs)

            val typeWords = when (tx.type) {
                TransactionType.EXPENSE -> "expense ব্যয় খরচ debit"
                TransactionType.INCOME -> "income আয় আয় credit"
                TransactionType.TRANSFER -> "transfer ট্রান্সফার স্থানান্তর"
            }

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

            val searchFields = listOf(
                SearchMatcher.SearchField(payee, SearchMatcher.FieldWeight.PRIMARY.weight),
                SearchMatcher.SearchField(titleText, SearchMatcher.FieldWeight.PRIMARY.weight),
                SearchMatcher.SearchField(note, SearchMatcher.FieldWeight.SECONDARY.weight),
                SearchMatcher.SearchField(ref, SearchMatcher.FieldWeight.SECONDARY.weight),
                SearchMatcher.SearchField("$catNameEn $catNameBn $subCatNameEn $subCatNameBn", SearchMatcher.FieldWeight.SECONDARY.weight),
                SearchMatcher.SearchField("$debitAccEn $debitAccBn $creditAccEn $creditAccBn", SearchMatcher.FieldWeight.SECONDARY.weight),
                SearchMatcher.SearchField("$amtStr $amtFormatted", SearchMatcher.FieldWeight.SECONDARY.weight),
                SearchMatcher.SearchField(dateKeywords, SearchMatcher.FieldWeight.SECONDARY.weight),
                SearchMatcher.SearchField(typeWords, SearchMatcher.FieldWeight.KEYWORD.weight)
            )

            val result = MasterSearchResult.TransactionItem(
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

            list.add(
                IndexedEntity(
                    result = result,
                    fields = searchFields,
                    dateEpochMs = tx.dateEpochMs,
                    secondarySortKey = titleText,
                    categoryFilter = SearchFilterCategory.TRANSACTIONS
                )
            )
        }

        // 2. Accounts Index
        allAccounts.forEach { acc ->
            val matchedBalance = accountsWithBalances.find { it.account.id == acc.id }?.currentBalance ?: 0.0
            val typeLabel = if (acc.type == AccountType.ASSET) {
                if (languageMode == LanguageMode.BANGLA) "সম্পদ একাউন্ট" else "Asset Account"
            } else {
                if (languageMode == LanguageMode.BANGLA) "দায় / ঋণ" else "Liability Account"
            }
            val balanceStr = LanguageHelper.formatCurrency(matchedBalance, languageMode)
            val keywords = "${acc.nameEn} ${acc.nameBn} ${acc.description} ${acc.accountNumber} ${acc.type.name} account wallet ব্যাংক বিকাশ নগদ রকেট একাউন্ট ওয়ালেট"

            val searchFields = listOf(
                SearchMatcher.SearchField(acc.localizedName(languageMode), SearchMatcher.FieldWeight.PRIMARY.weight),
                SearchMatcher.SearchField("${acc.nameEn} ${acc.nameBn}", SearchMatcher.FieldWeight.PRIMARY.weight),
                SearchMatcher.SearchField(keywords, SearchMatcher.FieldWeight.KEYWORD.weight),
                SearchMatcher.SearchField("${acc.accountNumber} ${acc.description} $balanceStr", SearchMatcher.FieldWeight.SECONDARY.weight)
            )

            val result = MasterSearchResult.AccountItem(
                account = acc,
                balanceFormatted = balanceStr,
                title = acc.localizedName(languageMode),
                subtitle = "$typeLabel • $balanceStr",
                groupTag = if (languageMode == LanguageMode.BANGLA) "একাউন্ট ও ওয়ালেট" else "ACCOUNTS & WALLETS",
                iconVector = IconHelper.getIconByName(acc.iconName),
                iconTint = if (acc.type == AccountType.ASSET) SolidIncome else SolidExpense
            )

            list.add(
                IndexedEntity(
                    result = result,
                    fields = searchFields,
                    dateEpochMs = 0L,
                    secondarySortKey = acc.localizedName(languageMode),
                    categoryFilter = SearchFilterCategory.ACCOUNTS
                )
            )
        }

        // 3. Categories Index
        allCategories.forEach { cat ->
            val isSub = cat.parentId != null
            val typeLabel = if (cat.type == CategoryType.EXPENSE) {
                if (languageMode == LanguageMode.BANGLA) "ব্যয় বিভাগ" else "Expense Category"
            } else {
                if (languageMode == LanguageMode.BANGLA) "আয় বিভাগ" else "Income Category"
            }
            val parentCat = if (isSub) allCategories.find { it.id == cat.parentId } else null
            val parentNote = if (parentCat != null) " (${parentCat.localizedName(languageMode)})" else ""

            val typeWords = if (cat.type == CategoryType.EXPENSE) "expense ব্যয় খরচ category ক্যাটাগরি" else "income আয় আয় category ক্যাটাগরি"

            val searchFields = listOf(
                SearchMatcher.SearchField(cat.localizedName(languageMode), SearchMatcher.FieldWeight.PRIMARY.weight),
                SearchMatcher.SearchField("${cat.nameEn} ${cat.nameBn}", SearchMatcher.FieldWeight.PRIMARY.weight),
                SearchMatcher.SearchField(typeWords, SearchMatcher.FieldWeight.KEYWORD.weight),
                SearchMatcher.SearchField(parentCat?.localizedName(languageMode).orEmpty(), SearchMatcher.FieldWeight.SECONDARY.weight)
            )

            val result = MasterSearchResult.CategoryItem(
                category = cat,
                isSubCategory = isSub,
                title = cat.localizedName(languageMode),
                subtitle = "$typeLabel$parentNote",
                groupTag = if (languageMode == LanguageMode.BANGLA) "ক্যাটাগরি" else "CATEGORIES",
                iconVector = IconHelper.getIconByName(cat.iconName),
                iconTint = if (cat.type == CategoryType.EXPENSE) SolidExpense else SolidIncome
            )

            list.add(
                IndexedEntity(
                    result = result,
                    fields = searchFields,
                    dateEpochMs = 0L,
                    secondarySortKey = cat.localizedName(languageMode),
                    categoryFilter = SearchFilterCategory.CATEGORIES
                )
            )
        }

        // 4. Budgets Index
        monthlyBudgets.forEach { budget ->
            val matchedCat = allCategories.find { it.id == budget.itemId }
            val catName = matchedCat?.localizedName(languageMode) ?: "Category #${budget.itemId}"
            val spent = transactions.filter {
                it.category?.id == budget.itemId && it.transaction.type == TransactionType.EXPENSE
            }.sumOf { it.transaction.amount }
            val remaining = (budget.budgetedAmount - spent).coerceAtLeast(0.0)
            val progress = if (budget.budgetedAmount > 0) (spent / budget.budgetedAmount).toFloat().coerceIn(0f, 1f) else 0f

            val budgetedStr = LanguageHelper.formatCurrency(budget.budgetedAmount, languageMode)
            val remainingStr = LanguageHelper.formatCurrency(remaining, languageMode)
            val spentStr = LanguageHelper.formatCurrency(spent, languageMode)

            val searchFields = listOf(
                SearchMatcher.SearchField(catName, SearchMatcher.FieldWeight.PRIMARY.weight),
                SearchMatcher.SearchField("budget monthly limit বাজেট লিমিট খরচের সীমা", SearchMatcher.FieldWeight.KEYWORD.weight),
                SearchMatcher.SearchField("$budgetedStr $spentStr $remainingStr", SearchMatcher.FieldWeight.SECONDARY.weight)
            )

            val result = MasterSearchResult.BudgetItem(
                monthlyBudget = budget,
                categoryName = catName,
                budgetedFormatted = budgetedStr,
                spentFormatted = spentStr,
                remainingFormatted = remainingStr,
                progressPercent = progress,
                isOverBudget = spent > budget.budgetedAmount,
                title = catName,
                subtitle = if (languageMode == LanguageMode.BANGLA)
                    "বাজেট: $budgetedStr • অবশিষ্ট: $remainingStr"
                else
                    "Limit: $budgetedStr • Rem: $remainingStr",
                groupTag = if (languageMode == LanguageMode.BANGLA) "বাজেট ও লিমিট" else "BUDGETS & LIMITS",
                iconVector = IconHelper.getIconByName(matchedCat?.iconName ?: "PieChart"),
                iconTint = if (spent > budget.budgetedAmount) SolidExpense else SolidPrimary
            )

            list.add(
                IndexedEntity(
                    result = result,
                    fields = searchFields,
                    dateEpochMs = 0L,
                    secondarySortKey = catName,
                    categoryFilter = SearchFilterCategory.BUDGETS
                )
            )
        }

        // 5. Savings Goals, Recurring Bills, Wishlist
        savingsGoals.forEach { goal ->
            val progress = (goal.progressPercent / 100f).coerceIn(0f, 1f)
            val targetStr = LanguageHelper.formatCurrency(goal.goal.targetAmount, languageMode)
            val savedStr = LanguageHelper.formatCurrency(goal.effectiveSaved, languageMode)
            val goalTitle = if (languageMode == LanguageMode.BANGLA && goal.goal.nameBn.isNotBlank()) goal.goal.nameBn else goal.goal.name

            val searchFields = listOf(
                SearchMatcher.SearchField(goalTitle, SearchMatcher.FieldWeight.PRIMARY.weight),
                SearchMatcher.SearchField("${goal.goal.name} ${goal.goal.nameBn}", SearchMatcher.FieldWeight.PRIMARY.weight),
                SearchMatcher.SearchField("goal savings milestone সঞ্চয় লক্ষ্য ফান্ড", SearchMatcher.FieldWeight.KEYWORD.weight),
                SearchMatcher.SearchField("${goal.goal.notes} $targetStr $savedStr", SearchMatcher.FieldWeight.SECONDARY.weight)
            )

            val result = MasterSearchResult.SavingsGoalItem(
                goalWithDetails = goal,
                targetFormatted = targetStr,
                savedFormatted = savedStr,
                progressPercent = progress,
                title = goalTitle,
                subtitle = if (languageMode == LanguageMode.BANGLA)
                    "সঞ্চয় লক্ষ্য: $targetStr (${goal.progressPercent.toInt()}%)"
                else
                    "Goal: $targetStr (${goal.progressPercent.toInt()}% saved)",
                groupTag = if (languageMode == LanguageMode.BANGLA) "সঞ্চয় লক্ষ্য" else "SAVINGS GOALS",
                iconVector = IconHelper.getIconByName(goal.goal.iconName),
                iconTint = SolidIncome
            )

            list.add(
                IndexedEntity(
                    result = result,
                    fields = searchFields,
                    dateEpochMs = goal.goal.targetDate,
                    secondarySortKey = goalTitle,
                    categoryFilter = SearchFilterCategory.GOALS_AND_BILLS
                )
            )
        }

        recurringBills.forEach { bill ->
            val billTitle = bill.bill.title.ifBlank { bill.bill.payeeOrPayer }
            val amtStr = LanguageHelper.formatCurrency(bill.bill.amount, languageMode)

            val searchFields = listOf(
                SearchMatcher.SearchField(billTitle, SearchMatcher.FieldWeight.PRIMARY.weight),
                SearchMatcher.SearchField("${bill.bill.payeeOrPayer} ${bill.bill.title}", SearchMatcher.FieldWeight.PRIMARY.weight),
                SearchMatcher.SearchField("bill subscription recurring বিদ্যুৎ বিল গ্যাস সাবস্ক্রিপশন", SearchMatcher.FieldWeight.KEYWORD.weight),
                SearchMatcher.SearchField("${bill.bill.note} $amtStr ${bill.bill.recurrencePeriod.name}", SearchMatcher.FieldWeight.SECONDARY.weight)
            )

            val result = MasterSearchResult.RecurringBillItem(
                billWithDetails = bill,
                amountFormatted = amtStr,
                title = billTitle,
                subtitle = "${bill.bill.recurrencePeriod.name} • $amtStr",
                groupTag = if (languageMode == LanguageMode.BANGLA) "পুনরাবৃত্ত বিল" else "RECURRING BILLS",
                iconVector = Icons.Default.EventRepeat,
                iconTint = SolidExpense
            )

            list.add(
                IndexedEntity(
                    result = result,
                    fields = searchFields,
                    dateEpochMs = 0L,
                    secondarySortKey = billTitle,
                    categoryFilter = SearchFilterCategory.GOALS_AND_BILLS
                )
            )
        }

        wishlistItems.forEach { wish ->
            val amtStr = LanguageHelper.formatCurrency(wish.item.estimatedAmount, languageMode)
            val catName = wish.category?.localizedName(languageMode).orEmpty()

            val searchFields = listOf(
                SearchMatcher.SearchField(wish.item.title, SearchMatcher.FieldWeight.PRIMARY.weight),
                SearchMatcher.SearchField("wishlist shopping planned কেনাকাটা উইশলিস্ট পরিকল্পনা", SearchMatcher.FieldWeight.KEYWORD.weight),
                SearchMatcher.SearchField("${wish.item.notes} $amtStr $catName", SearchMatcher.FieldWeight.SECONDARY.weight)
            )

            val result = MasterSearchResult.WishlistResultItem(
                wishlistItem = wish,
                amountFormatted = amtStr,
                title = wish.item.title,
                subtitle = if (wish.category != null)
                    "$catName • $amtStr"
                else
                    amtStr,
                groupTag = if (languageMode == LanguageMode.BANGLA) "উইশলিস্ট" else "WISHLIST",
                iconVector = Icons.Default.Favorite,
                iconTint = SolidPrimary
            )

            list.add(
                IndexedEntity(
                    result = result,
                    fields = searchFields,
                    dateEpochMs = 0L,
                    secondarySortKey = wish.item.title,
                    categoryFilter = SearchFilterCategory.GOALS_AND_BILLS
                )
            )
        }

        // 6. Actions & Settings Navigation Index
        actionCatalog.forEach { action ->
            val searchFields = listOf(
                SearchMatcher.SearchField(action.title, SearchMatcher.FieldWeight.PRIMARY.weight),
                SearchMatcher.SearchField(action.keywords.joinToString(" "), SearchMatcher.FieldWeight.KEYWORD.weight),
                SearchMatcher.SearchField(action.subtitle.orEmpty(), SearchMatcher.FieldWeight.SECONDARY.weight)
            )

            list.add(
                IndexedEntity(
                    result = action,
                    fields = searchFields,
                    dateEpochMs = 0L,
                    secondarySortKey = action.title,
                    categoryFilter = SearchFilterCategory.SETTINGS_AND_TOOLS
                )
            )
        }

        list
    }

    var isSearching by remember { mutableStateOf(false) }

    // Debounced and ranked search computation on Dispatchers.Default
    val searchResultsState = produceState(
        initialValue = emptyList<SearchGroupResult>(),
        key1 = searchQuery,
        key2 = selectedCategory,
        key3 = indexedEntities
    ) {
        val trimmedQuery = searchQuery.trim()
        if (trimmedQuery.isEmpty()) {
            isSearching = false
            value = emptyList()
            return@produceState
        }

        val startTime = System.currentTimeMillis()
        delay(200) // 200 ms debounce

        val tokens = SearchMatcher.tokenize(trimmedQuery)
        if (tokens.isEmpty()) {
            isSearching = false
            value = emptyList()
            return@produceState
        }

        if (System.currentTimeMillis() - startTime > 150) {
            isSearching = true
        }

        val grouped = withContext(Dispatchers.Default) {
            val matchedItems = mutableListOf<SearchMatcher.MatchResult<MasterSearchResult>>()

            for (indexed in indexedEntities) {
                if (selectedCategory != SearchFilterCategory.ALL && indexed.categoryFilter != selectedCategory) {
                    continue
                }

                val score = SearchMatcher.computeScore(tokens, indexed.fields)
                if (score > 0.0) {
                    matchedItems.add(
                        SearchMatcher.MatchResult(
                            item = indexed.result,
                            score = score,
                            dateEpochMs = indexed.dateEpochMs,
                            secondarySortKey = indexed.secondarySortKey
                        )
                    )
                }
            }

            // Sort matched items by score descending, then date descending (recency), then alphabetical
            matchedItems.sortWith(
                compareByDescending<SearchMatcher.MatchResult<MasterSearchResult>> { it.score }
                    .thenByDescending { it.dateEpochMs }
                    .thenBy { it.secondarySortKey }
            )

            // Group by groupTag and sort groups by the highest topScore
            val groupsMap = matchedItems.groupBy { it.item.groupTag }
            groupsMap.map { (groupTag, itemsList) ->
                val topScore = itemsList.maxOfOrNull { it.score } ?: 0.0
                SearchGroupResult(
                    groupTag = groupTag,
                    items = itemsList.map { it.item },
                    topScore = topScore
                )
            }.sortedByDescending { it.topScore }
        }

        isSearching = false
        value = grouped
    }

    val groupedResults = searchResultsState.value
    val totalMatches = remember(groupedResults) { groupedResults.sumOf { it.items.size } }

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

                        // Progress bar indicator for long searches
                        AnimatedVisibility(visible = isSearching, enter = fadeIn(), exit = fadeOut()) {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(2.dp)
                                    .padding(top = 4.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
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
                    // Initial Quick Search Recommendations & Recent Transactions
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
                                    searchQuery = "",
                                    onClick = {
                                        onDismiss()
                                        onTransactionClick(tx)
                                    }
                                )
                            }
                        }
                    }
                } else if (groupedResults.isEmpty() && !isSearching) {
                    // No Results Found - Friendly Empty State with suggestions
                    val normQuery = SearchMatcher.normalize(searchQuery)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
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
                                text = if (languageMode == LanguageMode.BANGLA) "\"$normQuery\" এর জন্য কিছু পাওয়া যায়নি" else "No results found for \"$normQuery\"",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA)
                                    "পরামর্শ: শব্দ সংখ্যা কমিয়ে খুঁজুন, বানান পরীক্ষা করুন বা 'সকল' ক্যাটাগরি নির্বাচন করুন।"
                                else
                                    "Suggestions: Try fewer words, check for typos, or switch filter to 'All'.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                            if (selectedCategory != SearchFilterCategory.ALL) {
                                TextButton(onClick = { selectedCategory = SearchFilterCategory.ALL }) {
                                    Text(if (languageMode == LanguageMode.BANGLA) "সকল ক্যাটাগরিতে খুঁজুন" else "Search across All categories")
                                }
                            }
                        }
                    }
                } else {
                    // Search Results List Grouped cleanly by highest group score
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        groupedResults.forEach { group ->
                            val isExpanded = expandedGroups.contains(group.groupTag)
                            val visibleItems = if (isExpanded) group.items else group.items.take(30)
                            val hasMore = group.items.size > 30

                            item(key = "header_${group.groupTag}") {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 10.dp, bottom = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = group.groupTag,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "${group.items.size}",
                                        fontSize = 11.5.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }

                            items(visibleItems, key = { it.id }) { result ->
                                MasterSearchResultRow(
                                    result = result,
                                    searchQuery = searchQuery,
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
                                                    onNavigateToSettingsSubPage(
                                                        result.targetSettingsSubPage,
                                                        result.targetSettingsTab,
                                                        result.highlightKey
                                                    )
                                                } else if (result.targetView != null) {
                                                    onNavigate(result.targetView)
                                                }
                                            }
                                        }
                                    }
                                )
                            }

                            if (hasMore) {
                                item(key = "expand_${group.groupTag}") {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                expandedGroups = if (isExpanded) {
                                                    expandedGroups - group.groupTag
                                                } else {
                                                    expandedGroups + group.groupTag
                                                }
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp, vertical = 10.dp),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = if (isExpanded) {
                                                    if (languageMode == LanguageMode.BANGLA) "সংক্ষেপ করুন" else "Show less"
                                                } else {
                                                    if (languageMode == LanguageMode.BANGLA) "সবগুলো (${group.items.size} টি) দেখুন" else "Show all ${group.items.size}"
                                                },
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
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

@Composable
private fun MasterSearchResultRow(
    result: MasterSearchResult,
    searchQuery: String,
    onClick: () -> Unit
) {
    val highlightColor = MaterialTheme.colorScheme.primary
    val highlightedTitle = remember(result.title, searchQuery, highlightColor) {
        SearchMatcher.highlight(
            text = result.title,
            query = searchQuery,
            highlightColor = highlightColor,
            highlightWeight = FontWeight.Bold
        )
    }

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
                    IconHelper.AppIcon(
                        iconName = result.iconName,
                        fallbackName = result.title,
                        contentDescription = result.title,
                        tint = result.iconTint ?: MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = highlightedTitle,
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
