package com.example.util

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ItemDisplayFormat(val labelEn: String, val labelBn: String) {
    TWO_LINES("Double Line (Default)", "দুই লাইন (ডিফল্ট)"),
    SINGLE_LINE("Single Line", "এক লাইন")
}

data class DisplayFormatConfig(
    val itemDisplayFormat: ItemDisplayFormat = ItemDisplayFormat.TWO_LINES
)

class DisplayFormatPreferences private constructor(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("budgeter_display_format_prefs", Context.MODE_PRIVATE)

    private val _config = MutableStateFlow(loadConfig())
    val config: StateFlow<DisplayFormatConfig> = _config.asStateFlow()

    private fun loadConfig(): DisplayFormatConfig {
        val formatName = prefs.getString(KEY_DISPLAY_FORMAT, ItemDisplayFormat.TWO_LINES.name) ?: ItemDisplayFormat.TWO_LINES.name
        val format = try {
            ItemDisplayFormat.valueOf(formatName)
        } catch (e: Exception) {
            ItemDisplayFormat.TWO_LINES
        }
        return DisplayFormatConfig(itemDisplayFormat = format)
    }

    fun setItemDisplayFormat(format: ItemDisplayFormat) {
        prefs.edit().putString(KEY_DISPLAY_FORMAT, format.name).apply()
        _config.value = _config.value.copy(itemDisplayFormat = format)
    }

    companion object {
        private const val KEY_DISPLAY_FORMAT = "key_item_display_format"

        @Volatile
        private var instance: DisplayFormatPreferences? = null

        fun getInstance(context: Context): DisplayFormatPreferences {
            return instance ?: synchronized(this) {
                instance ?: DisplayFormatPreferences(context).also { instance = it }
            }
        }
    }
}
