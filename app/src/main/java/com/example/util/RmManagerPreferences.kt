package com.example.util

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class RmFilterConfig(
    val includeKeyword: String = "RM",
    val excludeKeyword: String = "RM Others"
)

class RmManagerPreferences private constructor(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("budgeter_rm_manager_prefs", Context.MODE_PRIVATE)

    private val _config = MutableStateFlow(loadConfig())
    val config: StateFlow<RmFilterConfig> = _config.asStateFlow()

    private fun loadConfig(): RmFilterConfig {
        val include = prefs.getString(KEY_INCLUDE_KEYWORD, "RM") ?: "RM"
        val exclude = prefs.getString(KEY_EXCLUDE_KEYWORD, "RM Others") ?: "RM Others"
        return RmFilterConfig(
            includeKeyword = include.ifBlank { "RM" },
            excludeKeyword = exclude.ifBlank { "RM Others" }
        )
    }

    fun saveConfig(includeKeyword: String, excludeKeyword: String) {
        val cleanInclude = includeKeyword.trim().ifBlank { "RM" }
        val cleanExclude = excludeKeyword.trim()
        prefs.edit()
            .putString(KEY_INCLUDE_KEYWORD, cleanInclude)
            .putString(KEY_EXCLUDE_KEYWORD, cleanExclude)
            .apply()
        _config.value = RmFilterConfig(
            includeKeyword = cleanInclude,
            excludeKeyword = cleanExclude
        )
    }

    fun resetToDefaults() {
        saveConfig("RM", "RM Others")
    }

    fun reload() {
        _config.value = loadConfig()
    }

    companion object {
        private const val KEY_INCLUDE_KEYWORD = "rm_include_keyword"
        private const val KEY_EXCLUDE_KEYWORD = "rm_exclude_keyword"

        @Volatile
        private var INSTANCE: RmManagerPreferences? = null

        fun getInstance(context: Context): RmManagerPreferences {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: RmManagerPreferences(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
