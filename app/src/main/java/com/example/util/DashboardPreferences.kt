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
    NET_EARNINGS("net_earnings", "Net Earnings", "নেট আয় ও ব্যয়"),
    CASH_FLOW("cash_flow", "Cash Flow", "নগদ প্রবাহ (Cash Flow)"),
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
            NET_EARNINGS -> Icons.Default.BarChart
            CASH_FLOW -> Icons.AutoMirrored.Filled.TrendingUp
            FINANCIAL_OVERVIEW -> Icons.Default.Payments
            QUICK_ACTIONS -> Icons.Default.TouchApp
            RECENT_TRANSACTIONS -> Icons.Default.Receipt
        }

    fun getTitle(languageMode: LanguageMode): String {
        return when (languageMode) {
            LanguageMode.ENGLISH -> defaultTitleEn
            LanguageMode.BANGLA -> defaultTitleBn
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
        }
    }
}

enum class DailyChartType(val labelEn: String, val labelBn: String) {
    BAR("Bar", "বার"),
    LINE("Line", "লাইন"),
    AREA("Area", "এরিয়া"),
    STEPPED("Stepped", "স্টেপড");

    fun getLabel(languageMode: LanguageMode): String {
        return when (languageMode) {
            LanguageMode.ENGLISH -> labelEn
            LanguageMode.BANGLA -> labelBn
        }
    }
}

enum class DailySummaryPeriod(val labelEn: String, val labelBn: String, val days: Int) {
    LAST_7_DAYS("7 Days", "৭ দিন", 7),
    LAST_14_DAYS("14 Days", "১৪ দিন", 14),
    LAST_1_MONTH("1 Month", "১ মাস", 30),
    LAST_2_MONTHS("2 Months", "২ মাস", 60),
    LAST_3_MONTHS("3 Months", "৩ মাস", 90),
    QUARTER("Quarter", "ত্রৈমাসিক", 90),
    HALF_YEAR("Half Year", "অর্ধবছর", 180),
    ONE_YEAR("1 Year", "১ বছর", 365),
    CUSTOM("Custom", "কাস্টম", 0);

    companion object {
        fun fromString(name: String?): DailySummaryPeriod {
            if (name == null) return LAST_7_DAYS
            return when (name) {
                "LAST_30_DAYS", "THIS_MONTH" -> LAST_1_MONTH
                else -> runCatching { valueOf(name) }.getOrDefault(LAST_7_DAYS)
            }
        }
    }

    fun getLabel(languageMode: LanguageMode): String {
        return when (languageMode) {
            LanguageMode.ENGLISH -> labelEn
            LanguageMode.BANGLA -> labelBn
        }
    }
}

enum class BudgetSummaryPeriod(val labelEn: String, val labelBn: String, val days: Int) {
    LAST_7_DAYS("7 Days", "৭ দিন", 7),
    LAST_14_DAYS("14 Days", "১৪ দিন", 14),
    LAST_1_MONTH("1 Month", "১ মাস", 30),
    LAST_2_MONTHS("2 Months", "২ মাস", 60),
    LAST_3_MONTHS("3 Months", "৩ মাস", 90),
    QUARTER("Quarter", "ত্রৈমাসিক", 90),
    HALF_YEAR("Half Year", "অর্ধবছর", 180),
    ONE_YEAR("1 Year", "১ বছর", 365),
    CUSTOM("Custom", "কাস্টম", 0);

    companion object {
        fun fromString(name: String?): BudgetSummaryPeriod {
            if (name == null) return LAST_1_MONTH
            return when (name) {
                "LAST_30_DAYS", "THIS_MONTH" -> LAST_1_MONTH
                else -> runCatching { valueOf(name) }.getOrDefault(LAST_1_MONTH)
            }
        }
    }

    fun getLabel(languageMode: LanguageMode): String {
        return when (languageMode) {
            LanguageMode.ENGLISH -> labelEn
            LanguageMode.BANGLA -> labelBn
        }
    }
}

enum class DecimalPrecision(val labelEn: String, val labelBn: String, val decimals: Int) {
    OFF("Off (Integer)", "বন্ধ (পূর্ণসংখ্যা)", 0),
    ONE_DIGIT("1 Digit", "১ দশমিক স্থান", 1),
    TWO_DIGITS("2 Digits", "২ দশমিক স্থান", 2);

    fun getLabel(languageMode: LanguageMode): String {
        return when (languageMode) {
            LanguageMode.ENGLISH -> labelEn
            LanguageMode.BANGLA -> labelBn
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
        }
    }
}

enum class BudgetChartShape(val labelEn: String, val labelBn: String) {
    DONUT("Donut Chart", "ডোনাট চার্ট"),
    PIE("Pie Chart", "পাই চার্ট"),
    BAR("Horizontal Bars", "অনুভূমিক বার"),
    VERTICAL_BAR("Vertical Bars", "উল্লম্ব বার"),
    BUDGET_VS_ACTUAL("Budget vs Actual", "বাজেট বনাম ব্যয়"),
    STACKED("Stacked Bars", "স্ট্যাকড বার");

    fun getLabel(languageMode: LanguageMode): String {
        return when (languageMode) {
            LanguageMode.ENGLISH -> labelEn
            LanguageMode.BANGLA -> labelBn
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
        }
    }
}

data class DashboardConfig(
    val cardOrder: List<DashboardCardType> = listOf(
        DashboardCardType.FINANCIAL_OVERVIEW,
        DashboardCardType.CASH_FLOW,
        DashboardCardType.NET_EARNINGS,
        DashboardCardType.NET_WORTH,
        DashboardCardType.DAILY_SUMMARY,
        DashboardCardType.BUDGET_SUMMARY,
        DashboardCardType.FAVORITE_ACCOUNTS,
        DashboardCardType.CALENDAR_VIEW,
        DashboardCardType.QUICK_ACTIONS,
        DashboardCardType.RECENT_TRANSACTIONS
    ),
    val visibleCards: Set<DashboardCardType> = setOf(
        DashboardCardType.FINANCIAL_OVERVIEW,
        DashboardCardType.CASH_FLOW,
        DashboardCardType.NET_EARNINGS,
        DashboardCardType.NET_WORTH,
        DashboardCardType.DAILY_SUMMARY,
        DashboardCardType.BUDGET_SUMMARY,
        DashboardCardType.FAVORITE_ACCOUNTS,
        DashboardCardType.CALENDAR_VIEW,
        DashboardCardType.QUICK_ACTIONS,
        DashboardCardType.RECENT_TRANSACTIONS
    ),
    val dailySummaryMode: DailySummaryMode = DailySummaryMode.EXPENSE,
    val dailySummaryPeriod: DailySummaryPeriod = DailySummaryPeriod.LAST_7_DAYS,
    val dailyCustomStartEpoch: Long? = null,
    val dailyCustomEndEpoch: Long? = null,
    val dailyChartType: DailyChartType = DailyChartType.BAR,
    val dailyShowValues: Boolean = true,
    val dailyShowAverages: Boolean = true,
    val dailyDecimalPrecision: DecimalPrecision = DecimalPrecision.TWO_DIGITS,
    val dailyShowCurrency: Boolean = true,
    val dailyShowCurrencySymbol: Boolean = true,
    val budgetChartShape: BudgetChartShape = BudgetChartShape.DONUT,
    val budgetCategoryType: BudgetSummaryType = BudgetSummaryType.EXPENSE,
    val budgetSummaryPeriod: BudgetSummaryPeriod = BudgetSummaryPeriod.LAST_1_MONTH,
    val budgetCustomStartEpoch: Long? = null,
    val budgetCustomEndEpoch: Long? = null,
    val budgetMaxCategories: Int = 6,
    val budgetShowPercentages: Boolean = true,
    val budgetShowTodayPace: Boolean = true,
    val favoriteAccountIds: Set<Long> = emptySet(), // Manually selected favorite accounts
    val manualFavoriteAccountIds: Set<Long> = favoriteAccountIds,
    val accountActivityTimestamps: Map<Long, Long> = emptyMap(),
    val deselectedAccountTimestamps: Map<Long, Long> = emptyMap(),
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
            DashboardCardType.FINANCIAL_OVERVIEW,
            DashboardCardType.CASH_FLOW,
            DashboardCardType.NET_EARNINGS,
            DashboardCardType.NET_WORTH,
            DashboardCardType.DAILY_SUMMARY,
            DashboardCardType.BUDGET_SUMMARY,
            DashboardCardType.FAVORITE_ACCOUNTS,
            DashboardCardType.CALENDAR_VIEW,
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
        val dailyPeriod = DailySummaryPeriod.fromString(dailyPeriodStr)

        val dailyCustomStart = if (prefs.contains(KEY_DAILY_CUSTOM_START)) prefs.getLong(KEY_DAILY_CUSTOM_START, 0L).takeIf { it > 0L } else null
        val dailyCustomEnd = if (prefs.contains(KEY_DAILY_CUSTOM_END)) prefs.getLong(KEY_DAILY_CUSTOM_END, 0L).takeIf { it > 0L } else null

        val dailyChartTypeStr = prefs.getString(KEY_DAILY_CHART_TYPE, DailyChartType.BAR.name)
        val dailyChartType = runCatching { DailyChartType.valueOf(dailyChartTypeStr ?: "") }.getOrDefault(DailyChartType.BAR)

        val dailyShowValues = prefs.getBoolean(KEY_DAILY_SHOW_VALUES, true)
        val dailyShowAverages = prefs.getBoolean(KEY_DAILY_SHOW_AVERAGES, true)

        val dailyPrecisionStr = prefs.getString(KEY_DAILY_DECIMAL_PRECISION, DecimalPrecision.TWO_DIGITS.name)
        val dailyPrecision = runCatching { DecimalPrecision.valueOf(dailyPrecisionStr ?: "") }.getOrDefault(DecimalPrecision.TWO_DIGITS)

        val dailyShowCurrency = prefs.getBoolean(KEY_DAILY_SHOW_CURRENCY, true)
        val dailyShowCurrencySymbol = prefs.getBoolean(KEY_DAILY_SHOW_CURRENCY_SYMBOL, true)

        val budgetShapeStr = prefs.getString(KEY_BUDGET_SHAPE, BudgetChartShape.DONUT.name)
        val budgetShape = runCatching { BudgetChartShape.valueOf(budgetShapeStr ?: "") }.getOrDefault(BudgetChartShape.DONUT)

        val budgetCatTypeStr = prefs.getString(KEY_BUDGET_CAT_TYPE, BudgetSummaryType.EXPENSE.name)
        val budgetCatType = runCatching { BudgetSummaryType.valueOf(budgetCatTypeStr ?: "") }.getOrDefault(BudgetSummaryType.EXPENSE)

        val budgetPeriodStr = prefs.getString(KEY_BUDGET_PERIOD, BudgetSummaryPeriod.LAST_1_MONTH.name)
        val budgetPeriod = BudgetSummaryPeriod.fromString(budgetPeriodStr)

        val budgetCustomStart = if (prefs.contains(KEY_BUDGET_CUSTOM_START)) prefs.getLong(KEY_BUDGET_CUSTOM_START, 0L).takeIf { it > 0L } else null
        val budgetCustomEnd = if (prefs.contains(KEY_BUDGET_CUSTOM_END)) prefs.getLong(KEY_BUDGET_CUSTOM_END, 0L).takeIf { it > 0L } else null

        val budgetMaxCats = prefs.getInt(KEY_BUDGET_MAX_CATS, 6)
        val budgetShowPercentages = prefs.getBoolean(KEY_BUDGET_SHOW_PERCENTAGES, true)
        val budgetShowTodayPace = prefs.getBoolean(KEY_BUDGET_SHOW_TODAY_PACE, true)

        val favIdsStr = prefs.getString(KEY_FAVORITE_ACCOUNTS, null)
        val favIds = if (!favIdsStr.isNullOrEmpty()) {
            favIdsStr.split(",").mapNotNull { it.toLongOrNull() }.toSet()
        } else {
            emptySet()
        }

        val activityStr = prefs.getString(KEY_ACCOUNT_ACTIVITY_TIMESTAMPS, null)
        val activityMap = mutableMapOf<Long, Long>()
        if (!activityStr.isNullOrEmpty()) {
            activityStr.split(",").forEach { pair ->
                val parts = pair.split(":")
                if (parts.size == 2) {
                    val id = parts[0].toLongOrNull()
                    val ts = parts[1].toLongOrNull()
                    if (id != null && ts != null) {
                        activityMap[id] = ts
                    }
                }
            }
        }

        val deselectedStr = prefs.getString(KEY_DESELECTED_ACCOUNTS, null)
        val deselectedMap = mutableMapOf<Long, Long>()
        if (!deselectedStr.isNullOrEmpty()) {
            deselectedStr.split(",").forEach { pair ->
                val parts = pair.split(":")
                if (parts.size == 2) {
                    val id = parts[0].toLongOrNull()
                    val ts = parts[1].toLongOrNull()
                    if (id != null && ts != null) {
                        deselectedMap[id] = ts
                    }
                }
            }
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
            dailyCustomStartEpoch = dailyCustomStart,
            dailyCustomEndEpoch = dailyCustomEnd,
            dailyChartType = dailyChartType,
            dailyShowValues = dailyShowValues,
            dailyShowAverages = dailyShowAverages,
            dailyDecimalPrecision = dailyPrecision,
            dailyShowCurrency = dailyShowCurrency,
            dailyShowCurrencySymbol = dailyShowCurrencySymbol,
            budgetChartShape = budgetShape,
            budgetCategoryType = budgetCatType,
            budgetSummaryPeriod = budgetPeriod,
            budgetCustomStartEpoch = budgetCustomStart,
            budgetCustomEndEpoch = budgetCustomEnd,
            budgetMaxCategories = budgetMaxCats,
            budgetShowPercentages = budgetShowPercentages,
            budgetShowTodayPace = budgetShowTodayPace,
            favoriteAccountIds = favIds,
            manualFavoriteAccountIds = favIds,
            accountActivityTimestamps = activityMap,
            deselectedAccountTimestamps = deselectedMap,
            calendarDisplayMode = calMode,
            calendarShowIncome = calShowIncome,
            calendarShowExpense = calShowExpense
        )
    }

    private fun saveConfig(newConfig: DashboardConfig) {
        val activitySerialized = newConfig.accountActivityTimestamps.entries.joinToString(",") { "${it.key}:${it.value}" }
        val deselectedSerialized = newConfig.deselectedAccountTimestamps.entries.joinToString(",") { "${it.key}:${it.value}" }
        val editor = prefs.edit()
            .putString(KEY_CARD_ORDER, newConfig.cardOrder.joinToString(",") { it.id })
            .putString(KEY_VISIBLE_CARDS, newConfig.visibleCards.joinToString(",") { it.id })
            .putString(KEY_DAILY_MODE, newConfig.dailySummaryMode.name)
            .putString(KEY_DAILY_PERIOD, newConfig.dailySummaryPeriod.name)
            .putString(KEY_DAILY_CHART_TYPE, newConfig.dailyChartType.name)
            .putBoolean(KEY_DAILY_SHOW_VALUES, newConfig.dailyShowValues)
            .putBoolean(KEY_DAILY_SHOW_AVERAGES, newConfig.dailyShowAverages)
            .putString(KEY_DAILY_DECIMAL_PRECISION, newConfig.dailyDecimalPrecision.name)
            .putBoolean(KEY_DAILY_SHOW_CURRENCY, newConfig.dailyShowCurrency)
            .putBoolean(KEY_DAILY_SHOW_CURRENCY_SYMBOL, newConfig.dailyShowCurrencySymbol)
            .putString(KEY_BUDGET_SHAPE, newConfig.budgetChartShape.name)
            .putString(KEY_BUDGET_CAT_TYPE, newConfig.budgetCategoryType.name)
            .putString(KEY_BUDGET_PERIOD, newConfig.budgetSummaryPeriod.name)
            .putInt(KEY_BUDGET_MAX_CATS, newConfig.budgetMaxCategories)
            .putBoolean(KEY_BUDGET_SHOW_PERCENTAGES, newConfig.budgetShowPercentages)
            .putBoolean(KEY_BUDGET_SHOW_TODAY_PACE, newConfig.budgetShowTodayPace)
            .putString(KEY_FAVORITE_ACCOUNTS, newConfig.favoriteAccountIds.joinToString(","))
            .putString(KEY_ACCOUNT_ACTIVITY_TIMESTAMPS, activitySerialized)
            .putString(KEY_DESELECTED_ACCOUNTS, deselectedSerialized)
            .putString(KEY_CALENDAR_MODE, newConfig.calendarDisplayMode.name)
            .putBoolean(KEY_CALENDAR_SHOW_INCOME, newConfig.calendarShowIncome)
            .putBoolean(KEY_CALENDAR_SHOW_EXPENSE, newConfig.calendarShowExpense)

        if (newConfig.dailyCustomStartEpoch != null) editor.putLong(KEY_DAILY_CUSTOM_START, newConfig.dailyCustomStartEpoch) else editor.remove(KEY_DAILY_CUSTOM_START)
        if (newConfig.dailyCustomEndEpoch != null) editor.putLong(KEY_DAILY_CUSTOM_END, newConfig.dailyCustomEndEpoch) else editor.remove(KEY_DAILY_CUSTOM_END)
        if (newConfig.budgetCustomStartEpoch != null) editor.putLong(KEY_BUDGET_CUSTOM_START, newConfig.budgetCustomStartEpoch) else editor.remove(KEY_BUDGET_CUSTOM_START)
        if (newConfig.budgetCustomEndEpoch != null) editor.putLong(KEY_BUDGET_CUSTOM_END, newConfig.budgetCustomEndEpoch) else editor.remove(KEY_BUDGET_CUSTOM_END)

        editor.apply()
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
        customStartEpoch: Long? = _config.value.dailyCustomStartEpoch,
        customEndEpoch: Long? = _config.value.dailyCustomEndEpoch,
        chartType: DailyChartType = _config.value.dailyChartType,
        showValues: Boolean = _config.value.dailyShowValues,
        showAverages: Boolean = _config.value.dailyShowAverages,
        decimalPrecision: DecimalPrecision = _config.value.dailyDecimalPrecision,
        showCurrency: Boolean = _config.value.dailyShowCurrency,
        showCurrencySymbol: Boolean = _config.value.dailyShowCurrencySymbol
    ) {
        saveConfig(
            _config.value.copy(
                dailySummaryMode = mode,
                dailySummaryPeriod = period,
                dailyCustomStartEpoch = customStartEpoch,
                dailyCustomEndEpoch = customEndEpoch,
                dailyChartType = chartType,
                dailyShowValues = showValues,
                dailyShowAverages = showAverages,
                dailyDecimalPrecision = decimalPrecision,
                dailyShowCurrency = showCurrency,
                dailyShowCurrencySymbol = showCurrencySymbol
            )
        )
    }

    fun setBudgetSummarySettings(
        shape: BudgetChartShape = _config.value.budgetChartShape,
        categoryType: BudgetSummaryType = _config.value.budgetCategoryType,
        period: BudgetSummaryPeriod = _config.value.budgetSummaryPeriod,
        customStartEpoch: Long? = _config.value.budgetCustomStartEpoch,
        customEndEpoch: Long? = _config.value.budgetCustomEndEpoch,
        maxCategories: Int = _config.value.budgetMaxCategories,
        showPercentages: Boolean = _config.value.budgetShowPercentages,
        showTodayPace: Boolean = _config.value.budgetShowTodayPace
    ) {
        saveConfig(
            _config.value.copy(
                budgetChartShape = shape,
                budgetCategoryType = categoryType,
                budgetSummaryPeriod = period,
                budgetCustomStartEpoch = customStartEpoch,
                budgetCustomEndEpoch = customEndEpoch,
                budgetMaxCategories = maxCategories,
                budgetShowPercentages = showPercentages,
                budgetShowTodayPace = showTodayPace
            )
        )
    }

    fun setFavoriteAccounts(accountIds: Set<Long>) {
        val currentManual = _config.value.manualFavoriteAccountIds
        val currentDeselected = _config.value.deselectedAccountTimestamps.toMutableMap()
        val now = System.currentTimeMillis()

        // Any account that was previously a manual favorite and is now unselected gets recorded in deselected
        val newlyDeselected = currentManual - accountIds
        for (id in newlyDeselected) {
            currentDeselected[id] = now
        }

        // Selected accounts are cleared from deselected
        for (id in accountIds) {
            currentDeselected.remove(id)
        }

        val orderedSet = LinkedHashSet(accountIds)

        saveConfig(
            _config.value.copy(
                favoriteAccountIds = orderedSet,
                manualFavoriteAccountIds = orderedSet,
                deselectedAccountTimestamps = currentDeselected
            )
        )
    }

    fun onAccountActivity(accountId: Long, activityEpochMs: Long = System.currentTimeMillis()) {
        if (accountId <= 0L) return
        val currentActivity = _config.value.accountActivityTimestamps.toMutableMap()
        val prevTs = currentActivity[accountId] ?: 0L
        if (activityEpochMs > prevTs) {
            currentActivity[accountId] = activityEpochMs
            saveConfig(
                _config.value.copy(
                    accountActivityTimestamps = currentActivity
                )
            )
        }
    }

    fun addFavoriteAccount(accountId: Long) {
        val current = _config.value.manualFavoriteAccountIds
        setFavoriteAccounts(current + accountId)
    }

    fun addFavoriteAccounts(accountIds: Collection<Long>) {
        val current = _config.value.manualFavoriteAccountIds
        setFavoriteAccounts(current + accountIds)
    }

    fun toggleFavoriteAccount(accountId: Long) {
        val current = _config.value.manualFavoriteAccountIds
        if (accountId in current) {
            setFavoriteAccounts(current - accountId)
        } else {
            setFavoriteAccounts(current + accountId)
        }
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
        private const val KEY_DAILY_CUSTOM_START = "dashboard_daily_custom_start"
        private const val KEY_DAILY_CUSTOM_END = "dashboard_daily_custom_end"
        private const val KEY_DAILY_CHART_TYPE = "dashboard_daily_chart_type"
        private const val KEY_DAILY_SHOW_VALUES = "dashboard_daily_show_values"
        private const val KEY_DAILY_SHOW_AVERAGES = "dashboard_daily_show_averages"
        private const val KEY_DAILY_DECIMAL_PRECISION = "dashboard_daily_decimal_precision"
        private const val KEY_DAILY_SHOW_CURRENCY = "dashboard_daily_show_currency"
        private const val KEY_DAILY_SHOW_CURRENCY_SYMBOL = "dashboard_daily_show_currency_symbol"
        private const val KEY_BUDGET_SHAPE = "dashboard_budget_shape"
        private const val KEY_BUDGET_CAT_TYPE = "dashboard_budget_cat_type"
        private const val KEY_BUDGET_PERIOD = "dashboard_budget_period"
        private const val KEY_BUDGET_CUSTOM_START = "dashboard_budget_custom_start"
        private const val KEY_BUDGET_CUSTOM_END = "dashboard_budget_custom_end"
        private const val KEY_BUDGET_MAX_CATS = "dashboard_budget_max_cats"
        private const val KEY_BUDGET_SHOW_PERCENTAGES = "dashboard_budget_show_percentages"
        private const val KEY_BUDGET_SHOW_TODAY_PACE = "dashboard_budget_show_today_pace"
        private const val KEY_FAVORITE_ACCOUNTS = "dashboard_favorite_accounts"
        private const val KEY_ACCOUNT_ACTIVITY_TIMESTAMPS = "dashboard_account_activity_timestamps"
        private const val KEY_DESELECTED_ACCOUNTS = "dashboard_deselected_accounts"
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
