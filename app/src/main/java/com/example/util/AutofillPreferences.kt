package com.example.util

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AutofillConfig(
    val autofillCategory: Boolean = true,
    val autofillAccount: Boolean = true,
    val autofillAmount: Boolean = false,
    val autofillNotes: Boolean = false,
    val autofillLabel: Boolean = true
)

class AutofillPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("budgeter_autofill_prefs", Context.MODE_PRIVATE)

    private val _config = MutableStateFlow(loadConfig())
    val config: StateFlow<AutofillConfig> = _config.asStateFlow()

    private fun loadConfig(): AutofillConfig {
        return AutofillConfig(
            autofillCategory = prefs.getBoolean(KEY_CATEGORY, true),
            autofillAccount = prefs.getBoolean(KEY_ACCOUNT, true),
            autofillAmount = prefs.getBoolean(KEY_AMOUNT, false),
            autofillNotes = prefs.getBoolean(KEY_NOTES, false),
            autofillLabel = prefs.getBoolean(KEY_LABEL, true)
        )
    }

    fun updateConfig(newConfig: AutofillConfig) {
        prefs.edit()
            .putBoolean(KEY_CATEGORY, newConfig.autofillCategory)
            .putBoolean(KEY_ACCOUNT, newConfig.autofillAccount)
            .putBoolean(KEY_AMOUNT, newConfig.autofillAmount)
            .putBoolean(KEY_NOTES, newConfig.autofillNotes)
            .putBoolean(KEY_LABEL, newConfig.autofillLabel)
            .apply()
        _config.value = newConfig
    }

    companion object {
        private const val KEY_CATEGORY = "autofill_category"
        private const val KEY_ACCOUNT = "autofill_account"
        private const val KEY_AMOUNT = "autofill_amount"
        private const val KEY_NOTES = "autofill_notes"
        private const val KEY_LABEL = "autofill_label"

        @Volatile
        private var instance: AutofillPreferences? = null

        fun getInstance(context: Context): AutofillPreferences {
            return instance ?: synchronized(this) {
                instance ?: AutofillPreferences(context.applicationContext).also { instance = it }
            }
        }
    }
}
