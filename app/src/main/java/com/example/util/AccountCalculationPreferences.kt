package com.example.util

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

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

    companion object {
        private const val KEY_CALC_SETTINGS = "account_calc_settings_json"

        @Volatile
        private var instance: AccountCalculationPreferences? = null

        fun getInstance(context: Context): AccountCalculationPreferences {
            return instance ?: synchronized(this) {
                instance ?: AccountCalculationPreferences(context.applicationContext).also { instance = it }
            }
        }
    }
}
