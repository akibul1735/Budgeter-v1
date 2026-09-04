package com.example.util

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Tag
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.data.model.LanguageMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppTab(
    val id: String,
    val defaultTitleEn: String,
    val defaultTitleBn: String
) {
    MAIN("main", "Main", "মূল ড্যাশবোর্ড"),
    TRANSACTIONS("transactions", "Transactions", "লেনদেন"),
    BALANCE_SHEET("balance_sheet", "Balance Sheet", "ব্যালেন্স শীট"),
    ACCOUNTS("accounts", "Accounts", "অ্যাকাউন্টস"),
    BUDGET("budget", "Budget", "বাজেট"),
    CATEGORIES("categories", "Categories", "ক্যাটাগরি"),
    NET_EARNINGS("net_earnings", "Net Earnings", "নেট আয় ও রিপোর্ট"),
    LABELS("labels", "Labels", "লেবেল"),
    ITEMS_SUMMARY("items_summary", "Items Summary", "আইটেম সামারি"),
    REMINDERS("reminders", "Reminders", "রিমাইন্ডার ও বিল");

    val icon: ImageVector
        get() = when (this) {
            MAIN -> Icons.Default.Dashboard
            TRANSACTIONS -> Icons.AutoMirrored.Filled.ReceiptLong
            BALANCE_SHEET -> Icons.Default.AccountBalance
            ACCOUNTS -> Icons.Default.AccountBalanceWallet
            BUDGET -> Icons.Default.ShoppingBag
            CATEGORIES -> Icons.Default.Category
            NET_EARNINGS -> Icons.Default.Assignment
            LABELS -> Icons.Default.Tag
            ITEMS_SUMMARY -> Icons.Default.Bookmark
            REMINDERS -> Icons.Default.Alarm
        }

    fun getTitle(languageMode: LanguageMode): String {
        return when (languageMode) {
            LanguageMode.ENGLISH -> defaultTitleEn
            LanguageMode.BANGLA -> defaultTitleBn
            LanguageMode.BILINGUAL -> "$defaultTitleEn / $defaultTitleBn"
        }
    }
}

enum class TabPosition {
    TOP,
    BOTTOM
}

data class NavigationTabConfig(
    val position: TabPosition = TabPosition.BOTTOM,
    val allTabsOrder: List<AppTab> = AppTab.values().toList(),
    val enabledTabs: Set<AppTab> = AppTab.values().toSet()
) {
    val visibleTabs: List<AppTab>
        get() = allTabsOrder.filter { enabledTabs.contains(it) }

    fun isTabEnabled(tab: AppTab): Boolean = enabledTabs.contains(tab)
}

class TabPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("budgeter_tab_prefs", Context.MODE_PRIVATE)

    private val _config = MutableStateFlow(loadConfig())
    val config: StateFlow<NavigationTabConfig> = _config.asStateFlow()

    private fun loadConfig(): NavigationTabConfig {
        val positionStr = prefs.getString(KEY_POSITION, TabPosition.BOTTOM.name) ?: TabPosition.BOTTOM.name
        val position = try {
            TabPosition.valueOf(positionStr)
        } catch (_: Exception) {
            TabPosition.BOTTOM
        }

        val orderStr = prefs.getString(KEY_TABS_ORDER, null)
        val allTabsOrder = if (!orderStr.isNullOrBlank()) {
            val loaded = orderStr.split(",")
                .mapNotNull { name -> try { AppTab.valueOf(name.trim()) } catch (_: Exception) { null } }
            val missing = AppTab.values().filter { !loaded.contains(it) }
            loaded + missing
        } else {
            AppTab.values().toList()
        }

        val enabledStr = prefs.getString(KEY_ENABLED_TABS, null)
        val enabledTabs = if (!enabledStr.isNullOrBlank()) {
            val loaded = enabledStr.split(",")
                .mapNotNull { name -> try { AppTab.valueOf(name.trim()) } catch (_: Exception) { null } }
                .toSet()
            if (loaded.isNotEmpty()) {
                val missingNewTabs = AppTab.values().filter { !loaded.contains(it) && (it == AppTab.ACCOUNTS || it == AppTab.CATEGORIES) }
                loaded + missingNewTabs
            } else setOf(AppTab.MAIN)
        } else {
            AppTab.values().toSet()
        }

        return NavigationTabConfig(
            position = position,
            allTabsOrder = allTabsOrder,
            enabledTabs = enabledTabs
        )
    }

    fun updateConfig(newConfig: NavigationTabConfig) {
        // Enforce at least one tab enabled
        val safeEnabled = if (newConfig.enabledTabs.isEmpty()) setOf(AppTab.MAIN) else newConfig.enabledTabs
        val finalConfig = newConfig.copy(enabledTabs = safeEnabled)

        prefs.edit()
            .putString(KEY_POSITION, finalConfig.position.name)
            .putString(KEY_TABS_ORDER, finalConfig.allTabsOrder.joinToString(",") { it.name })
            .putString(KEY_ENABLED_TABS, finalConfig.enabledTabs.joinToString(",") { it.name })
            .apply()

        _config.value = finalConfig
    }

    fun setPosition(position: TabPosition) {
        updateConfig(_config.value.copy(position = position))
    }

    fun toggleTab(tab: AppTab, enabled: Boolean): Boolean {
        val current = _config.value
        val currentEnabled = current.enabledTabs.toMutableSet()
        if (enabled) {
            currentEnabled.add(tab)
        } else {
            if (currentEnabled.size <= 1 && currentEnabled.contains(tab)) {
                // Cannot disable the last remaining tab
                return false
            }
            currentEnabled.remove(tab)
        }
        updateConfig(current.copy(enabledTabs = currentEnabled))
        return true
    }

    fun reorderTab(fromIndex: Int, toIndex: Int) {
        val current = _config.value
        val list = current.allTabsOrder.toMutableList()
        if (fromIndex in list.indices && toIndex in list.indices && fromIndex != toIndex) {
            val item = list.removeAt(fromIndex)
            list.add(toIndex, item)
            updateConfig(current.copy(allTabsOrder = list))
        }
    }

    fun resetToDefaults() {
        val defaults = NavigationTabConfig(
            position = TabPosition.BOTTOM,
            allTabsOrder = AppTab.values().toList(),
            enabledTabs = AppTab.values().toSet()
        )
        updateConfig(defaults)
    }

    companion object {
        private const val KEY_POSITION = "tab_nav_position"
        private const val KEY_TABS_ORDER = "tab_nav_order"
        private const val KEY_ENABLED_TABS = "tab_nav_enabled"

        @Volatile
        private var instance: TabPreferences? = null

        fun getInstance(context: Context): TabPreferences {
            return instance ?: synchronized(this) {
                instance ?: TabPreferences(context.applicationContext).also { instance = it }
            }
        }
    }
}
