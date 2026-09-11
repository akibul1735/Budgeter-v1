package com.example.util

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class NumberGroupingStyle(val titleEn: String, val titleBn: String) {
    STANDARD_3("3 Digits (Thousands / Millions)", "৩ অঙ্ক পর পর (হাজার / মিলিয়ন)"),
    SOUTH_ASIAN("3 then 2 Digits (Lakh / Crore)", "৩ এরপর ২ অঙ্ক পর পর (লাখ / কোটি)"),
    MYRIAD_4("4 Digits (Myriad)", "৪ অঙ্ক পর পর (Wan / Myriad)"),
    NONE("No Grouping", "কোনো গ্রুপিং নেই")
}

enum class AmountSeparatorPreset(
    val id: String,
    val titleEn: String,
    val titleBn: String,
    val groupingSeparator: String,
    val decimalSeparator: String,
    val groupingStyle: NumberGroupingStyle,
    val exampleEn: String,
    val exampleBn: String
) {
    STANDARD_COMMA(
        id = "STANDARD_COMMA",
        titleEn = "Standard (1,234,567.89)",
        titleBn = "আন্তর্জাতিক মানক (১,২৩৪,৫৬৭.৮৯)",
        groupingSeparator = ",",
        decimalSeparator = ".",
        groupingStyle = NumberGroupingStyle.STANDARD_3,
        exampleEn = "1,234,567.89",
        exampleBn = "১,২৩৪,৫৬৭.৮৯"
    ),
    SOUTH_ASIAN_LAKH_CRORE(
        id = "SOUTH_ASIAN_LAKH_CRORE",
        titleEn = "South Asian Lakh/Crore (12,34,567.89)",
        titleBn = "বাংলাদেশি / ভারতীয় লাখ-কোটি (১২,৩৪,৫৬৭.৮৯)",
        groupingSeparator = ",",
        decimalSeparator = ".",
        groupingStyle = NumberGroupingStyle.SOUTH_ASIAN,
        exampleEn = "12,34,567.89",
        exampleBn = "১২,৩৪,৫৬৭.৮৯"
    ),
    EUROPEAN_DOT(
        id = "EUROPEAN_DOT",
        titleEn = "European (1.234.567,89)",
        titleBn = "ইউরোপীয় (১.২৩৪.৫৬৭,৮৯)",
        groupingSeparator = ".",
        decimalSeparator = ",",
        groupingStyle = NumberGroupingStyle.STANDARD_3,
        exampleEn = "1.234.567,89",
        exampleBn = "১.২৩৪.৫৬৭,৮৯"
    ),
    SPACE_SEPARATOR(
        id = "SPACE_SEPARATOR",
        titleEn = "Space / SI (1 234 567.89)",
        titleBn = "স্পেস সেপারেটর (১ ২৩৪ ৫৬৭.৮৯)",
        groupingSeparator = " ",
        decimalSeparator = ".",
        groupingStyle = NumberGroupingStyle.STANDARD_3,
        exampleEn = "1 234 567.89",
        exampleBn = "১ ২৩৪ ৫৬৭.৮৯"
    ),
    SWISS_APOSTROPHE(
        id = "SWISS_APOSTROPHE",
        titleEn = "Apostrophe / Swiss (1'234'567.89)",
        titleBn = "অ্যাপোস্ট্রফি (১'২৩৪'৫৬৭.৮৯)",
        groupingSeparator = "'",
        decimalSeparator = ".",
        groupingStyle = NumberGroupingStyle.STANDARD_3,
        exampleEn = "1'234'567.89",
        exampleBn = "১'২৩৪'৫৬৭.৮৯"
    ),
    NO_SEPARATOR(
        id = "NO_SEPARATOR",
        titleEn = "None / Plain (1234567.89)",
        titleBn = "সেপারেটর ছাড়া (১২৩৪৫৬৭.৮৯)",
        groupingSeparator = "",
        decimalSeparator = ".",
        groupingStyle = NumberGroupingStyle.NONE,
        exampleEn = "1234567.89",
        exampleBn = "১২৩৪৫৬৭.৮৯"
    ),
    CUSTOM(
        id = "CUSTOM",
        titleEn = "Custom Format",
        titleBn = "কাস্টম ফরম্যাট",
        groupingSeparator = ",",
        decimalSeparator = ".",
        groupingStyle = NumberGroupingStyle.STANDARD_3,
        exampleEn = "Custom",
        exampleBn = "কাস্টম"
    );

    companion object {
        fun fromId(id: String): AmountSeparatorPreset {
            return entries.firstOrNull { it.id == id } ?: STANDARD_COMMA
        }
    }
}

data class AmountFormatConfig(
    val preset: AmountSeparatorPreset = AmountSeparatorPreset.STANDARD_COMMA,
    val customGroupingSeparator: String = ",",
    val customDecimalSeparator: String = ".",
    val customGroupingStyle: NumberGroupingStyle = NumberGroupingStyle.STANDARD_3
) {
    val effectiveGroupingSeparator: String
        get() = if (preset == AmountSeparatorPreset.CUSTOM) customGroupingSeparator else preset.groupingSeparator

    val effectiveDecimalSeparator: String
        get() = if (preset == AmountSeparatorPreset.CUSTOM) {
            if (customDecimalSeparator.isNotEmpty()) customDecimalSeparator else "."
        } else preset.decimalSeparator

    val effectiveGroupingStyle: NumberGroupingStyle
        get() = if (preset == AmountSeparatorPreset.CUSTOM) customGroupingStyle else preset.groupingStyle
}

class AmountFormatPreferences private constructor(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("budgeter_amount_format_prefs", Context.MODE_PRIVATE)

    private val _config = MutableStateFlow(loadConfig())
    val config: StateFlow<AmountFormatConfig> = _config.asStateFlow()

    init {
        LanguageHelper.updateAmountFormatConfig(_config.value)
    }

    private fun loadConfig(): AmountFormatConfig {
        val presetId = prefs.getString(KEY_PRESET, AmountSeparatorPreset.STANDARD_COMMA.id) ?: AmountSeparatorPreset.STANDARD_COMMA.id
        val preset = AmountSeparatorPreset.fromId(presetId)
        val customGrouping = prefs.getString(KEY_CUSTOM_GROUPING, ",") ?: ","
        val customDecimal = prefs.getString(KEY_CUSTOM_DECIMAL, ".") ?: "."
        val customStyleName = prefs.getString(KEY_CUSTOM_STYLE, NumberGroupingStyle.STANDARD_3.name) ?: NumberGroupingStyle.STANDARD_3.name
        val customStyle = try {
            NumberGroupingStyle.valueOf(customStyleName)
        } catch (e: Exception) {
            NumberGroupingStyle.STANDARD_3
        }

        return AmountFormatConfig(
            preset = preset,
            customGroupingSeparator = customGrouping,
            customDecimalSeparator = customDecimal,
            customGroupingStyle = customStyle
        )
    }

    fun setPreset(preset: AmountSeparatorPreset) {
        prefs.edit().putString(KEY_PRESET, preset.id).apply()
        val updated = _config.value.copy(preset = preset)
        _config.value = updated
        LanguageHelper.updateAmountFormatConfig(updated)
    }

    fun setCustomConfig(
        groupingSeparator: String,
        decimalSeparator: String,
        groupingStyle: NumberGroupingStyle
    ) {
        prefs.edit()
            .putString(KEY_PRESET, AmountSeparatorPreset.CUSTOM.id)
            .putString(KEY_CUSTOM_GROUPING, groupingSeparator)
            .putString(KEY_CUSTOM_DECIMAL, decimalSeparator)
            .putString(KEY_CUSTOM_STYLE, groupingStyle.name)
            .apply()
        val updated = AmountFormatConfig(
            preset = AmountSeparatorPreset.CUSTOM,
            customGroupingSeparator = groupingSeparator,
            customDecimalSeparator = decimalSeparator,
            customGroupingStyle = groupingStyle
        )
        _config.value = updated
        LanguageHelper.updateAmountFormatConfig(updated)
    }

    fun updateConfig(config: AmountFormatConfig) {
        prefs.edit()
            .putString(KEY_PRESET, config.preset.id)
            .putString(KEY_CUSTOM_GROUPING, config.customGroupingSeparator)
            .putString(KEY_CUSTOM_DECIMAL, config.customDecimalSeparator)
            .putString(KEY_CUSTOM_STYLE, config.customGroupingStyle.name)
            .apply()
        _config.value = config
        LanguageHelper.updateAmountFormatConfig(config)
    }

    fun resetToDefaults() {
        prefs.edit().clear().apply()
        val def = AmountFormatConfig()
        _config.value = def
        LanguageHelper.updateAmountFormatConfig(def)
    }

    companion object {
        private const val KEY_PRESET = "amount_preset"
        private const val KEY_CUSTOM_GROUPING = "amount_custom_grouping"
        private const val KEY_CUSTOM_DECIMAL = "amount_custom_decimal"
        private const val KEY_CUSTOM_STYLE = "amount_custom_style"

        @Volatile
        private var INSTANCE: AmountFormatPreferences? = null

        fun getInstance(context: Context): AmountFormatPreferences {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AmountFormatPreferences(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
