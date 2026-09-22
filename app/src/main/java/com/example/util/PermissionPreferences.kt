package com.example.util

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PermissionConfig(
    val hasCompletedUpdatePermissionPrompt: Boolean = false,
    val isPermissionPromptDismissed: Boolean = false,
    val promptVersion: Int = 0
)

class PermissionPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("budgeter_permission_prefs", Context.MODE_PRIVATE)

    private val _config = MutableStateFlow(loadConfig())
    val config: StateFlow<PermissionConfig> = _config.asStateFlow()

    private fun loadConfig(): PermissionConfig {
        return PermissionConfig(
            hasCompletedUpdatePermissionPrompt = prefs.getBoolean(KEY_COMPLETED_UPDATE_PROMPT, false),
            isPermissionPromptDismissed = prefs.getBoolean(KEY_PROMPT_DISMISSED, false),
            promptVersion = prefs.getInt(KEY_PROMPT_VERSION, 0)
        )
    }

    fun markPromptCompleted() {
        prefs.edit()
            .putBoolean(KEY_COMPLETED_UPDATE_PROMPT, true)
            .putBoolean(KEY_PROMPT_DISMISSED, false)
            .putInt(KEY_PROMPT_VERSION, CURRENT_PROMPT_VERSION)
            .apply()
        _config.value = loadConfig()
    }

    fun markPromptDismissed() {
        prefs.edit()
            .putBoolean(KEY_PROMPT_DISMISSED, true)
            .putInt(KEY_PROMPT_VERSION, CURRENT_PROMPT_VERSION)
            .apply()
        _config.value = loadConfig()
    }

    fun resetPrompt() {
        prefs.edit()
            .putBoolean(KEY_COMPLETED_UPDATE_PROMPT, false)
            .putBoolean(KEY_PROMPT_DISMISSED, false)
            .putInt(KEY_PROMPT_VERSION, 0)
            .apply()
        _config.value = loadConfig()
    }

    companion object {
        const val CURRENT_PROMPT_VERSION = 1
        private const val KEY_COMPLETED_UPDATE_PROMPT = "completed_update_prompt"
        private const val KEY_PROMPT_DISMISSED = "prompt_dismissed"
        private const val KEY_PROMPT_VERSION = "prompt_version"

        @Volatile
        private var INSTANCE: PermissionPreferences? = null

        fun getInstance(context: Context): PermissionPreferences {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PermissionPreferences(context).also { INSTANCE = it }
            }
        }
    }
}
