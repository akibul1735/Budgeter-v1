package com.example.util

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

enum class AccountInclusionViewMode(val titleEn: String, val titleBn: String) {
    ALL_ACTIVE("All Active", "সকল সক্রিয়"),
    ALL_INCLUDED("All Included", "সকল অন্তর্ভুক্ত")
}

data class AccountCalcSetting(
    val isIncluded: Boolean = true,
    val adjustmentAmount: Double = 0.0
)

data class AccountCalcConfig(
    val settings: Map<Long, AccountCalcSetting> = emptyMap()
) {
    fun isIncluded(accountId: Long): Boolean {
        return settings[accountId]?.isIncluded ?: true
    }

    fun getAdjustment(accountId: Long): Double {
        return settings[accountId]?.adjustmentAmount ?: 0.0
    }

    fun getSetting(accountId: Long): AccountCalcSetting {
        return settings[accountId] ?: AccountCalcSetting()
    }

    fun getEffectiveBalance(accountId: Long, actualBalance: Double): Double {
        val setting = getSetting(accountId)
        if (!setting.isIncluded) return 0.0
        return actualBalance + setting.adjustmentAmount
    }

    val hasAnyCustomizations: Boolean
        get() = settings.values.any { !it.isIncluded || it.adjustmentAmount != 0.0 }
}

class AccountCalculationPreferences private constructor(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("budgeter_account_calc_prefs", Context.MODE_PRIVATE)

    private val _config = MutableStateFlow(loadConfig())
    val config: StateFlow<AccountCalcConfig> = _config.asStateFlow()

    private val _inclusionViewMode = MutableStateFlow(loadInclusionViewMode())
    val inclusionViewMode: StateFlow<AccountInclusionViewMode> = _inclusionViewMode.asStateFlow()

    fun getInclusionViewMode(): AccountInclusionViewMode {
        return loadInclusionViewMode()
    }

    private fun loadInclusionViewMode(): AccountInclusionViewMode {
        val modeStr = prefs.getString(KEY_INCLUSION_VIEW_MODE, null)
        return if (modeStr == AccountInclusionViewMode.ALL_ACTIVE.name) AccountInclusionViewMode.ALL_ACTIVE
        else AccountInclusionViewMode.ALL_INCLUDED
    }

    fun setInclusionViewMode(mode: AccountInclusionViewMode) {
        prefs.edit().putString(KEY_INCLUSION_VIEW_MODE, mode.name).apply()
        _inclusionViewMode.value = mode
    }

    private fun loadConfig(): AccountCalcConfig {
        val jsonStr = prefs.getString(KEY_CALC_SETTINGS, null) ?: return AccountCalcConfig()
        return try {
            val json = JSONObject(jsonStr)
            val map = mutableMapOf<Long, AccountCalcSetting>()
            val keys = json.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val id = key.toLongOrNull() ?: continue
                val obj = json.getJSONObject(key)
                val isIncluded = obj.optBoolean("isIncluded", true)
                val adj = obj.optDouble("adjustmentAmount", 0.0)
                map[id] = AccountCalcSetting(isIncluded = isIncluded, adjustmentAmount = adj)
            }
            AccountCalcConfig(settings = map)
        } catch (_: Exception) {
            AccountCalcConfig()
        }
    }

    fun setIncludeStatus(accountId: Long, isIncluded: Boolean) {
        val current = _config.value.settings.toMutableMap()
        val prev = current[accountId] ?: AccountCalcSetting()
        current[accountId] = prev.copy(isIncluded = isIncluded)
        saveConfig(current)
    }

    fun setAdjustment(accountId: Long, adjustment: Double) {
        val current = _config.value.settings.toMutableMap()
        val prev = current[accountId] ?: AccountCalcSetting()
        current[accountId] = prev.copy(adjustmentAmount = adjustment)
        saveConfig(current)
    }

    fun setSetting(accountId: Long, isIncluded: Boolean, adjustment: Double) {
        val current = _config.value.settings.toMutableMap()
        current[accountId] = AccountCalcSetting(isIncluded = isIncluded, adjustmentAmount = adjustment)
        saveConfig(current)
    }

    fun resetAll() {
        prefs.edit().remove(KEY_CALC_SETTINGS).apply()
        _config.value = AccountCalcConfig()
    }

    fun resetAccount(accountId: Long) {
        val current = _config.value.settings.toMutableMap()
        current.remove(accountId)
        saveConfig(current)
    }

    private fun saveConfig(map: Map<Long, AccountCalcSetting>) {
        val json = JSONObject()
        for ((id, setting) in map) {
            // Only persist if modified from defaults
            if (!setting.isIncluded || setting.adjustmentAmount != 0.0) {
                val obj = JSONObject()
                obj.put("isIncluded", setting.isIncluded)
                obj.put("adjustmentAmount", setting.adjustmentAmount)
                json.put(id.toString(), obj)
            }
        }
        prefs.edit().putString(KEY_CALC_SETTINGS, json.toString()).apply()
        _config.value = AccountCalcConfig(settings = map)
    }

    fun reload() {
        _config.value = loadConfig()
    }

    companion object {
        private const val KEY_CALC_SETTINGS = "account_calc_settings_json"
        private const val KEY_INCLUSION_VIEW_MODE = "account_inclusion_view_mode"

        @Volatile
        private var instance: AccountCalculationPreferences? = null

        fun getInstance(context: Context): AccountCalculationPreferences {
            return instance ?: synchronized(this) {
                instance ?: AccountCalculationPreferences(context.applicationContext).also { instance = it }
            }
        }
    }
}
