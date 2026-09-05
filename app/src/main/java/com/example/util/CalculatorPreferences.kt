package com.example.util

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CalculatorPreferences private constructor(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("budgeter_calculator_prefs", Context.MODE_PRIVATE)

    private val defaultPercentages = listOf("1.85%", "1.5%", "5%", "7.5%", "10%", "15%", "20%")

    private val _frequentPercentages = MutableStateFlow(loadPercentages())
    val frequentPercentages: StateFlow<List<String>> = _frequentPercentages.asStateFlow()

    private fun loadPercentages(): List<String> {
        val stored = prefs.getString(KEY_FREQUENT_PCT, null)
        if (stored.isNullOrBlank()) {
            return defaultPercentages
        }
        return try {
            val list = stored.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            if (list.isEmpty()) defaultPercentages else list
        } catch (e: Exception) {
            defaultPercentages
        }
    }

    fun addFrequentPercentage(percentage: String) {
        val clean = percentage.trim().let { if (!it.endsWith("%")) "$it%" else it }
        val num = clean.removeSuffix("%").toDoubleOrNull() ?: return
        if (num <= 0) return

        val formatted = if (num % 1.0 == 0.0) "${num.toInt()}%" else "$num%"
        val currentList = _frequentPercentages.value.toMutableList()

        currentList.remove(formatted)
        currentList.add(0, formatted)

        // Keep top 12
        val updated = currentList.take(12)
        _frequentPercentages.value = updated

        prefs.edit().putString(KEY_FREQUENT_PCT, updated.joinToString(",")).apply()
    }

    companion object {
        private const val KEY_FREQUENT_PCT = "key_frequent_percentages"

        @Volatile
        private var instance: CalculatorPreferences? = null

        fun getInstance(context: Context): CalculatorPreferences {
            return instance ?: synchronized(this) {
                instance ?: CalculatorPreferences(context.applicationContext).also { instance = it }
            }
        }
    }
}
