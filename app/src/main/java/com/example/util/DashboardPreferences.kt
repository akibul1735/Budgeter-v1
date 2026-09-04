package com.example.util

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.data.model.LanguageMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class DashboardCardType(
    val id: String,
    val defaultTitleEn: String,
    val defaultTitleBn: String
) {
    DAILY_SUMMARY("daily_summary", "Daily Summary", "দৈনিক সারসংক্ষেপ"),
    BUDGET_SUMMARY("budget_summary", "Budget Summary", "বাজেট সারসংক্ষেপ"),
    FAVORITE_ACCOUNTS("favorite_accounts", "Favorite Accounts", "পছন্দের অ্যাকাউন্ট"),
    CALENDAR_VIEW("calendar_view", "Calendar View", "ক্যালেন্ডার ভিউ"),
    NET_WORTH("net_worth", "Net Worth Card", "নেট ওয়ার্থ কার্ড"),
    CASH_FLOW("cash_flow", "Cash Flow (Income/Expense)", "ক্যাশ ফ্লো"),
    FINANCIAL_OVERVIEW("financial_overview", "Financial Overview & Expendable", "আর্থিক বিবরণ ও অতিরিক্ত খরচ"),
    QUICK_ACTIONS("quick_actions", "Quick Action Buttons", "দ্রুত অ্যাকশন বাটন"),
    RECENT_TRANSACTIONS("recent_transactions", "Recent Transactions", "সাম্প্রতিক লেনদেন");

    val icon: ImageVector
        get() = when (this) {
            DAILY_SUMMARY -> Icons.Default.BarChart
            BUDGET_SUMMARY -> Icons.Default.PieChart
            FAVORITE_ACCOUNTS -> Icons.Default.Star
            CALENDAR_VIEW -> Icons.Default.CalendarMonth
            NET_WORTH -> Icons.Default.AccountBalance
            CASH_FLOW -> Icons.AutoMirrored.Filled.TrendingUp
            FINANCIAL_OVERVIEW -> Icons.Default.Payments
            QUICK_ACTIONS -> Icons.Default.TouchApp
            RECENT_TRANSACTIONS -> Icons.Default.Receipt
        }

    fun getTitle(languageMode: LanguageMode): String {
        return when (languageMode) {
            LanguageMode.ENGLISH -> defaultTitleEn
            LanguageMode.BANGLA -> defaultTitleBn
            LanguageMode.BILINGUAL -> "$defaultTitleEn / $defaultTitleBn"
        }
    }
}

enum class DailySummaryMode(val labelEn: String, val labelBn: String) {
    EXPENSE("Expense", "খরচ"),
    INCOME("Income", "আয়"),
    BOTH("Both (Income & Expense)", "উভয় (আয় ও খরচ)");

    fun getLabel(languageMode: LanguageMode): String {
        return when (languageMode) {
            LanguageMode.ENGLISH -> labelEn
            LanguageMode.BANGLA -> labelBn
            LanguageMode.BILINGUAL -> "$labelEn / $labelBn"
        }
    }
}

enum class DailySummaryPeriod(val labelEn: String, val labelBn: String, val days: Int) {
    LAST_7_DAYS("Last 7 Days", "গত ৭ দিন", 7),
    LAST_14_DAYS("Last 14 Days", "গত ১৪ দিন", 14),
    LAST_30_DAYS("Last 30 Days", "গত ৩০ দিন", 30),
    THIS_MONTH("This Month", "চলতি মাস", 0);

    fun getLabel(languageMode: LanguageMode): String {
        return when (languageMode) {
            LanguageMode.ENGLISH -> labelEn
            LanguageMode.BANGLA -> labelBn
            LanguageMode.BILINGUAL -> "$labelEn / $labelBn"
        }
    }
}

enum class BudgetSummaryType(val labelEn: String, val labelBn: String) {
    EXPENSE("Expense", "খরচ"),
    INCOME("Income", "আয়"),
    ALL("All Categories", "সব ক্যাটাগরি");

    fun getLabel(languageMode: LanguageMode): String {
        return when (languageMode) {
            LanguageMode.ENGLISH -> labelEn
            LanguageMode.BANGLA -> labelBn
            LanguageMode.BILINGUAL -> "$labelEn / $labelBn"
        }
    }
}

enum class BudgetChartShape(val labelEn: String, val labelBn: String) {
    DONUT("Donut Chart", "ডোনাট চার্ট"),
    PIE("Pie Chart", "পাই চার্ট"),
    BAR("Horizontal Bars", "অনুভূমিক বার");

    fun getLabel(languageMode: LanguageMode): String {
        return when (languageMode) {
            LanguageMode.ENGLISH -> labelEn
            LanguageMode.BANGLA -> labelBn
            LanguageMode.BILINGUAL -> "$labelEn / $labelBn"
        }
    }
}

enum class CalendarDisplayMode(val labelEn: String, val labelBn: String) {
    AMOUNTS("Show Amounts", "টাকার পরিমাণ দেখান"),
    DOTS("Color Dots Only", "শুধু রঙিন ডট দেখান");

    fun getLabel(languageMode: LanguageMode): String {
        return when (languageMode) {
            LanguageMode.ENGLISH -> labelEn
            LanguageMode.BANGLA -> labelBn
            LanguageMode.BILINGUAL -> "$labelEn / $labelBn"
        }
    }
}

data class DashboardConfig(
    val cardOrder: List<DashboardCardType> = listOf(
        DashboardCardType.DAILY_SUMMARY,
        DashboardCardType.BUDGET_SUMMARY,
        DashboardCardType.FAVORITE_ACCOUNTS,
        DashboardCardType.CALENDAR_VIEW,
        DashboardCardType.NET_WORTH,
        DashboardCardType.CASH_FLOW,
        DashboardCardType.FINANCIAL_OVERVIEW,
        DashboardCardType.QUICK_ACTIONS,
        DashboardCardType.RECENT_TRANSACTIONS
    ),
    val visibleCards: Set<DashboardCardType> = setOf(
        DashboardCardType.DAILY_SUMMARY,
        DashboardCardType.BUDGET_SUMMARY,
        DashboardCardType.FAVORITE_ACCOUNTS,
        DashboardCardType.CALENDAR_VIEW,
        DashboardCardType.NET_WORTH,
        DashboardCardType.CASH_FLOW,
        DashboardCardType.FINANCIAL_OVERVIEW,
        DashboardCardType.QUICK_ACTIONS,
        DashboardCardType.RECENT_TRANSACTIONS
    ),
    val dailySummaryMode: DailySummaryMode = DailySummaryMode.EXPENSE,
    val dailySummaryPeriod: DailySummaryPeriod = DailySummaryPeriod.LAST_7_DAYS,
    val dailyShowValues: Boolean = true,
    val dailyShowAverages: Boolean = true,
    val budgetChartShape: BudgetChartShape = BudgetChartShape.DONUT,
    val budgetCategoryType: BudgetSummaryType = BudgetSummaryType.EXPENSE,
    val budgetMaxCategories: Int = 6,
    val budgetShowPercentages: Boolean = true,
    val budgetShowTodayPace: Boolean = true,
    val favoriteAccountIds: Set<Long> = emptySet(),
    val calendarDisplayMode: CalendarDisplayMode = CalendarDisplayMode.AMOUNTS,
    val calendarShowIncome: Boolean = true,
    val calendarShowExpense: Boolean = true
)

class DashboardPreferences private constructor(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("budgeter_dashboard_prefs", Context.MODE_PRIVATE)

    private val _config = MutableStateFlow(loadConfig())
    val config: StateFlow<DashboardConfig> = _config.asStateFlow()

    private fun loadConfig(): DashboardConfig {
        val defaultCards = listOf(
            DashboardCardType.DAILY_SUMMARY,
            DashboardCardType.BUDGET_SUMMARY,
            DashboardCardType.FAVORITE_ACCOUNTS,
            DashboardCardType.CALENDAR_VIEW,
            DashboardCardType.NET_WORTH,
            DashboardCardType.CASH_FLOW,
            DashboardCardType.FINANCIAL_OVERVIEW,
            DashboardCardType.QUICK_ACTIONS,
            DashboardCardType.RECENT_TRANSACTIONS
        )

        val orderStr = prefs.getString(KEY_CARD_ORDER, null)
        val cardOrder = if (!orderStr.isNullOrEmpty()) {
            val loaded = orderStr.split(",").mapNotNull { id ->
                DashboardCardType.values().find { it.id == id }
            }
            val missing = defaultCards.filter { it !in loaded }
            loaded + missing
        } else {
            defaultCards
        }

        val visibleStr = prefs.getString(KEY_VISIBLE_CARDS, null)
        val visibleCards = if (!visibleStr.isNullOrEmpty()) {
            visibleStr.split(",").mapNotNull { id ->
                DashboardCardType.values().find { it.id == id }
            }.toSet()
        } else {
            defaultCards.toSet()
        }

        val dailyModeStr = prefs.getString(KEY_DAILY_MODE, DailySummaryMode.EXPENSE.name)
        val dailyMode = runCatching { DailySummaryMode.valueOf(dailyModeStr ?: "") }.getOrDefault(DailySummaryMode.EXPENSE)

        val dailyPeriodStr = prefs.getString(KEY_DAILY_PERIOD, DailySummaryPeriod.LAST_7_DAYS.name)
        val dailyPeriod = runCatching { DailySummaryPeriod.valueOf(dailyPeriodStr ?: "") }.getOrDefault(DailySummaryPeriod.LAST_7_DAYS)

        val dailyShowValues = prefs.getBoolean(KEY_DAILY_SHOW_VALUES, true)
        val dailyShowAverages = prefs.getBoolean(KEY_DAILY_SHOW_AVERAGES, true)

        val budgetShapeStr = prefs.getString(KEY_BUDGET_SHAPE, BudgetChartShape.DONUT.name)
        val budgetShape = runCatching { BudgetChartShape.valueOf(budgetShapeStr ?: "") }.getOrDefault(BudgetChartShape.DONUT)

        val budgetCatTypeStr = prefs.getString(KEY_BUDGET_CAT_TYPE, BudgetSummaryType.EXPENSE.name)
        val budgetCatType = runCatching { BudgetSummaryType.valueOf(budgetCatTypeStr ?: "") }.getOrDefault(BudgetSummaryType.EXPENSE)

        val budgetMaxCats = prefs.getInt(KEY_BUDGET_MAX_CATS, 6)
        val budgetShowPercentages = prefs.getBoolean(KEY_BUDGET_SHOW_PERCENTAGES, true)
        val budgetShowTodayPace = prefs.getBoolean(KEY_BUDGET_SHOW_TODAY_PACE, true)

        val favIdsStr = prefs.getString(KEY_FAVORITE_ACCOUNTS, null)
        val favIds = if (!favIdsStr.isNullOrEmpty()) {
            favIdsStr.split(",").mapNotNull { it.toLongOrNull() }.toSet()
        } else {
            emptySet()
        }

        val calModeStr = prefs.getString(KEY_CALENDAR_MODE, CalendarDisplayMode.AMOUNTS.name)
        val calMode = runCatching { CalendarDisplayMode.valueOf(calModeStr ?: "") }.getOrDefault(CalendarDisplayMode.AMOUNTS)

        val calShowIncome = prefs.getBoolean(KEY_CALENDAR_SHOW_INCOME, true)
        val calShowExpense = prefs.getBoolean(KEY_CALENDAR_SHOW_EXPENSE, true)

        return DashboardConfig(
            cardOrder = cardOrder,
            visibleCards = visibleCards,
            dailySummaryMode = dailyMode,
            dailySummaryPeriod = dailyPeriod,
            dailyShowValues = dailyShowValues,
            dailyShowAverages = dailyShowAverages,
            budgetChartShape = budgetShape,
            budgetCategoryType = budgetCatType,
            budgetMaxCategories = budgetMaxCats,
            budgetShowPercentages = budgetShowPercentages,
            budgetShowTodayPace = budgetShowTodayPace,
            favoriteAccountIds = favIds,
            calendarDisplayMode = calMode,
            calendarShowIncome = calShowIncome,
            calendarShowExpense = calShowExpense
        )
    }

    private fun saveConfig(newConfig: DashboardConfig) {
        prefs.edit()
            .putString(KEY_CARD_ORDER, newConfig.cardOrder.joinToString(",") { it.id })
            .putString(KEY_VISIBLE_CARDS, newConfig.visibleCards.joinToString(",") { it.id })
            .putString(KEY_DAILY_MODE, newConfig.dailySummaryMode.name)
            .putString(KEY_DAILY_PERIOD, newConfig.dailySummaryPeriod.name)
            .putBoolean(KEY_DAILY_SHOW_VALUES, newConfig.dailyShowValues)
            .putBoolean(KEY_DAILY_SHOW_AVERAGES, newConfig.dailyShowAverages)
            .putString(KEY_BUDGET_SHAPE, newConfig.budgetChartShape.name)
            .putString(KEY_BUDGET_CAT_TYPE, newConfig.budgetCategoryType.name)
            .putInt(KEY_BUDGET_MAX_CATS, newConfig.budgetMaxCategories)
            .putBoolean(KEY_BUDGET_SHOW_PERCENTAGES, newConfig.budgetShowPercentages)
            .putBoolean(KEY_BUDGET_SHOW_TODAY_PACE, newConfig.budgetShowTodayPace)
            .putString(KEY_FAVORITE_ACCOUNTS, newConfig.favoriteAccountIds.joinToString(","))
            .putString(KEY_CALENDAR_MODE, newConfig.calendarDisplayMode.name)
            .putBoolean(KEY_CALENDAR_SHOW_INCOME, newConfig.calendarShowIncome)
            .putBoolean(KEY_CALENDAR_SHOW_EXPENSE, newConfig.calendarShowExpense)
            .apply()
        _config.value = newConfig
    }

    fun toggleCardVisibility(card: DashboardCardType, visible: Boolean) {
        val current = _config.value
        val newVisible = if (visible) {
            current.visibleCards + card
        } else {
            // Keep at least 1 card visible
            if (current.visibleCards.size > 1) current.visibleCards - card else current.visibleCards
        }
        saveConfig(current.copy(visibleCards = newVisible))
    }

    fun reorderCards(newOrder: List<DashboardCardType>) {
        saveConfig(_config.value.copy(cardOrder = newOrder))
    }

    fun moveCard(fromIndex: Int, toIndex: Int) {
        val currentOrder = _config.value.cardOrder.toMutableList()
        if (fromIndex in currentOrder.indices && toIndex in currentOrder.indices) {
            val item = currentOrder.removeAt(fromIndex)
            currentOrder.add(toIndex, item)
            saveConfig(_config.value.copy(cardOrder = currentOrder))
        }
    }

    fun setDailySummarySettings(
        mode: DailySummaryMode = _config.value.dailySummaryMode,
        period: DailySummaryPeriod = _config.value.dailySummaryPeriod,
        showValues: Boolean = _config.value.dailyShowValues,
        showAverages: Boolean = _config.value.dailyShowAverages
    ) {
        saveConfig(
            _config.value.copy(
                dailySummaryMode = mode,
                dailySummaryPeriod = period,
                dailyShowValues = showValues,
                dailyShowAverages = showAverages
            )
        )
    }

    fun setBudgetSummarySettings(
        shape: BudgetChartShape = _config.value.budgetChartShape,
        categoryType: BudgetSummaryType = _config.value.budgetCategoryType,
        maxCategories: Int = _config.value.budgetMaxCategories,
        showPercentages: Boolean = _config.value.budgetShowPercentages,
        showTodayPace: Boolean = _config.value.budgetShowTodayPace
    ) {
        saveConfig(
            _config.value.copy(
                budgetChartShape = shape,
                budgetCategoryType = categoryType,
                budgetMaxCategories = maxCategories,
                budgetShowPercentages = showPercentages,
                budgetShowTodayPace = showTodayPace
            )
        )
    }

    fun setFavoriteAccounts(accountIds: Set<Long>) {
        saveConfig(_config.value.copy(favoriteAccountIds = accountIds))
    }

    fun toggleFavoriteAccount(accountId: Long) {
        val current = _config.value.favoriteAccountIds
        val newFavs = if (accountId in current) current - accountId else current + accountId
        saveConfig(_config.value.copy(favoriteAccountIds = newFavs))
    }

    fun setCalendarSettings(
        mode: CalendarDisplayMode = _config.value.calendarDisplayMode,
        showIncome: Boolean = _config.value.calendarShowIncome,
        showExpense: Boolean = _config.value.calendarShowExpense
    ) {
        saveConfig(
            _config.value.copy(
                calendarDisplayMode = mode,
                calendarShowIncome = showIncome,
                calendarShowExpense = showExpense
            )
        )
    }

    fun resetToDefaults() {
        val defaultConfig = DashboardConfig()
        saveConfig(defaultConfig)
    }

    companion object {
        private const val KEY_CARD_ORDER = "dashboard_card_order"
        private const val KEY_VISIBLE_CARDS = "dashboard_visible_cards"
        private const val KEY_DAILY_MODE = "dashboard_daily_mode"
        private const val KEY_DAILY_PERIOD = "dashboard_daily_period"
        private const val KEY_DAILY_SHOW_VALUES = "dashboard_daily_show_values"
        private const val KEY_DAILY_SHOW_AVERAGES = "dashboard_daily_show_averages"
        private const val KEY_BUDGET_SHAPE = "dashboard_budget_shape"
        private const val KEY_BUDGET_CAT_TYPE = "dashboard_budget_cat_type"
        private const val KEY_BUDGET_MAX_CATS = "dashboard_budget_max_cats"
        private const val KEY_BUDGET_SHOW_PERCENTAGES = "dashboard_budget_show_percentages"
        private const val KEY_BUDGET_SHOW_TODAY_PACE = "dashboard_budget_show_today_pace"
        private const val KEY_FAVORITE_ACCOUNTS = "dashboard_favorite_accounts"
        private const val KEY_CALENDAR_MODE = "dashboard_calendar_mode"
        private const val KEY_CALENDAR_SHOW_INCOME = "dashboard_calendar_show_income"
        private const val KEY_CALENDAR_SHOW_EXPENSE = "dashboard_calendar_show_expense"

        @Volatile
        private var instance: DashboardPreferences? = null

        fun getInstance(context: Context): DashboardPreferences {
            return instance ?: synchronized(this) {
                instance ?: DashboardPreferences(context).also { instance = it }
            }
        }
    }
}
