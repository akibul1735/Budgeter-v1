package com.example.util

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class WidgetPreferences private constructor(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _isPrivacyEnabled = MutableStateFlow(prefs.getBoolean(KEY_PRIVACY_ENABLED, false))
    val isPrivacyEnabled: StateFlow<Boolean> = _isPrivacyEnabled.asStateFlow()

    fun isPrivacyEnabled(): Boolean = prefs.getBoolean(KEY_PRIVACY_ENABLED, false)

    fun togglePrivacy(): Boolean {
        val newState = !isPrivacyEnabled()
        prefs.edit().putBoolean(KEY_PRIVACY_ENABLED, newState).apply()
        _isPrivacyEnabled.value = newState
        return newState
    }

    fun setPrivacyEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_PRIVACY_ENABLED, enabled).apply()
        _isPrivacyEnabled.value = enabled
    }

    fun reload() {
        _isPrivacyEnabled.value = isPrivacyEnabled()
    }

    companion object {
        private const val PREFS_NAME = "budgeter_widget_prefs"
        private const val KEY_PRIVACY_ENABLED = "widget_privacy_enabled"

        @Volatile
        private var INSTANCE: WidgetPreferences? = null

        fun getInstance(context: Context): WidgetPreferences {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: WidgetPreferences(context).also { INSTANCE = it }
            }
        }
    }
}
