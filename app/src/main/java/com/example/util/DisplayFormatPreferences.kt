package com.example.util

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar

enum class ItemDisplayFormat(val labelEn: String, val labelBn: String) {
    TWO_LINES("Double Line (Default)", "দুই লাইন (ডিফল্ট)"),
    SINGLE_LINE("Single Line", "এক লাইন")
}

enum class DateFormatOption(
    val pattern: String,
    val titleEn: String,
    val titleBn: String,
    val example: String
) {
    D_MMM_YYYY("d MMM, yyyy", "d MMM, yyyy (d)", "৬ সেপ্টে, ২০২৬", "6 Sep, 2026"),
    DD_MMM_YYYY("dd MMM, yyyy", "DD MMM, yyyy (DD)", "০৬ সেপ্টে, ২০২৬", "06 Sep, 2026"),
    DDD_DD_MMM_YYYY("EEE, dd MMM yyyy", "DDD, DD MMM yyyy (DDD)", "রবি, ০৬ সেপ্টে ২০২৬", "Sun, 06 Sep 2026"),
    DDDD_DD_MMMM_YYYY("EEEE, dd MMMM yyyy", "dddd, DD MMMM yyyy (dddd)", "রবিবার, ০৬ সেপ্টেম্বর ২০২৬", "Sunday, 06 September 2026"),
    DDDD_D_MMMM_YYYY("EEEE, d MMMM yyyy", "dddd, d MMMM yyyy (dddd, d)", "রবিবার, ৬ সেপ্টেম্বর ২০২৬", "Sunday, 6 September 2026"),
    DD_MM_YYYY("dd/MM/yyyy", "DD/MM/YYYY", "০৬/০৯/২০২৬", "06/09/2026"),
    D_M_YYYY("d/M/yyyy", "d/M/yyyy", "৬/৯/২০২৬", "6/9/2026"),
    DD_DASH_MM_DASH_YYYY("dd-MM-yyyy", "DD-MM-YYYY", "০৬-০৯-২০২৬", "06-09-2026"),
    YYYY_MM_DD("yyyy-MM-dd", "YYYY-MM-DD (ISO)", "২০২৬-০৯-০৬", "2026-09-06"),
    MM_DD_YYYY("MM/dd/yyyy", "MM/DD/YYYY (US)", "০৯/০৬/২০২৬", "09/06/2026"),
    DD_MMMM_YYYY("dd MMMM, yyyy", "DD MMMM, YYYY", "০৬ সেপ্টেম্বর, ২০২৬", "06 September, 2026"),
    MMMM_D_YYYY("MMMM d, yyyy", "MMMM d, YYYY", "সেপ্টেম্বর ৬, ২০২৬", "September 6, 2026"),
    YYYY_SLASH_MM_SLASH_DD("yyyy/MM/dd", "YYYY/MM/DD", "২০২৬/০৯/০৬", "2026/09/06"),
    CUSTOM("CUSTOM", "Custom Format (d, DD, DDD, dddd, etc.)", "কাস্টম ফরম্যাট (d, DD, DDD, dddd)", "Custom pattern");

    companion object {
        fun fromPattern(pattern: String): DateFormatOption {
            return entries.firstOrNull { it.pattern == pattern } ?: CUSTOM
        }
    }
}

data class DisplayFormatConfig(
    val itemDisplayFormat: ItemDisplayFormat = ItemDisplayFormat.TWO_LINES,
    val dateFormatPattern: String = "dd MMM, yyyy",
    val customDateFormat: String = "",
    val firstDayOfWeek: Int = Calendar.SUNDAY
) {
    val effectiveDateFormatPattern: String
        get() = if (dateFormatPattern == "CUSTOM" && customDateFormat.isNotBlank()) customDateFormat else if (dateFormatPattern == "CUSTOM") "dd MMM, yyyy" else dateFormatPattern
}

class DisplayFormatPreferences private constructor(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("budgeter_display_format_prefs", Context.MODE_PRIVATE)

    private val _config = MutableStateFlow(loadConfig())
    val config: StateFlow<DisplayFormatConfig> = _config.asStateFlow()

    init {
        DateUtils.activeDateFormat = _config.value.effectiveDateFormatPattern
        DateUtils.activeFirstDayOfWeek = _config.value.firstDayOfWeek
    }

    private fun loadConfig(): DisplayFormatConfig {
        val formatName = prefs.getString(KEY_DISPLAY_FORMAT, ItemDisplayFormat.TWO_LINES.name) ?: ItemDisplayFormat.TWO_LINES.name
        val format = try {
            ItemDisplayFormat.valueOf(formatName)
        } catch (e: Exception) {
            ItemDisplayFormat.TWO_LINES
        }
        val datePattern = prefs.getString(KEY_DATE_FORMAT, "dd MMM, yyyy") ?: "dd MMM, yyyy"
        val customDatePattern = prefs.getString(KEY_CUSTOM_DATE_FORMAT, "") ?: ""
        val firstDay = prefs.getInt(KEY_FIRST_DAY_OF_WEEK, Calendar.SUNDAY)

        return DisplayFormatConfig(
            itemDisplayFormat = format,
            dateFormatPattern = datePattern,
            customDateFormat = customDatePattern,
            firstDayOfWeek = firstDay
        )
    }

    fun setItemDisplayFormat(format: ItemDisplayFormat) {
        prefs.edit().putString(KEY_DISPLAY_FORMAT, format.name).apply()
        _config.value = _config.value.copy(itemDisplayFormat = format)
    }

    fun setDateFormat(pattern: String, customPattern: String = "") {
        prefs.edit()
            .putString(KEY_DATE_FORMAT, pattern)
            .putString(KEY_CUSTOM_DATE_FORMAT, customPattern)
            .apply()
        val updated = _config.value.copy(dateFormatPattern = pattern, customDateFormat = customPattern)
        _config.value = updated
        DateUtils.activeDateFormat = updated.effectiveDateFormatPattern
    }

    fun setDateFormatPattern(pattern: String) {
        setDateFormat(pattern, _config.value.customDateFormat)
    }

    fun setCustomDateFormat(customPattern: String) {
        setDateFormat(DateFormatOption.CUSTOM.pattern, customPattern)
    }

    fun setFirstDayOfWeek(day: Int) {
        prefs.edit().putInt(KEY_FIRST_DAY_OF_WEEK, day).apply()
        val updated = _config.value.copy(firstDayOfWeek = day)
        _config.value = updated
        DateUtils.activeFirstDayOfWeek = day
    }

    fun resetToDefaults() {
        prefs.edit().clear().apply()
        val defaultCfg = loadConfig()
        _config.value = defaultCfg
        DateUtils.activeDateFormat = defaultCfg.effectiveDateFormatPattern
        DateUtils.activeFirstDayOfWeek = defaultCfg.firstDayOfWeek
    }

    companion object {
        private const val KEY_DISPLAY_FORMAT = "key_item_display_format"
        private const val KEY_DATE_FORMAT = "key_date_format_pattern"
        private const val KEY_CUSTOM_DATE_FORMAT = "key_custom_date_format_pattern"
        private const val KEY_FIRST_DAY_OF_WEEK = "key_first_day_of_week"

        @Volatile
        private var instance: DisplayFormatPreferences? = null

        fun getInstance(context: Context): DisplayFormatPreferences {
            return instance ?: synchronized(this) {
                instance ?: DisplayFormatPreferences(context).also { instance = it }
            }
        }
    }
}
