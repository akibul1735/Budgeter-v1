package com.example.util

import com.example.data.model.LanguageMode
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {

    @Volatile
    var activeDateFormat: String = "dd MMM, yyyy"

    @Volatile
    var activeFirstDayOfWeek: Int = Calendar.SUNDAY

    private val banglaMonths = arrayOf(
        "জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন",
        "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর"
    )

    private val banglaMonthsShort = arrayOf(
        "জানু", "ফেব্রু", "মার্চ", "এপ্রিল", "মে", "জুন",
        "জুলাই", "আগস্ট", "সেপ্টে", "অক্টো", "নভে", "ডিসে"
    )

    private val banglaDaysOfWeek = arrayOf(
        "রবি", "সোম", "মঙ্গল", "বুধ", "বৃহঃ", "শুক্র", "শনি"
    )

    private val banglaDaysOfWeekFull = arrayOf(
        "রবিবার", "সোমবার", "মঙ্গলবার", "বুধবার", "বৃহস্পতিবার", "শুক্রবার", "শনিবার"
    )

    /**
     * Normalizes user-entered pattern tokens to standard Java SimpleDateFormat pattern:
     * - dddd / DDDD -> EEEE (Full day of week, e.g., Sunday / রবিবার)
     * - ddd / DDD -> EEE (Short day of week, e.g., Sun / রবি)
     * - DD -> dd (2-digit day of month, e.g., 06)
     * - d / D -> d (1-digit day of month, e.g., 6)
     * - YYYY -> yyyy, YY -> yy
     */
    fun normalizePattern(pattern: String): String {
        if (pattern.isBlank()) return "dd MMM, yyyy"

        var p = pattern

        // Replace 4-letter day of week (dddd, DDDD) with placeholder
        p = p.replace("dddd", "\u0001")
            .replace("DDDD", "\u0001")

        // Replace 3-letter day of week (ddd, DDD) with placeholder
        p = p.replace("ddd", "\u0002")
            .replace("DDD", "\u0002")

        // Replace 2-digit day of month (DD, dd) with placeholder
        p = p.replace("DD", "\u0003")
            .replace("dd", "\u0003")

        // Replace 1-digit day of month (d, D) with placeholder (avoiding replacing within other words)
        p = p.replace("D", "\u0004")
            .replace("d", "\u0004")

        // Replace year tokens (YYYY -> yyyy, YY -> yy)
        p = p.replace("YYYY", "yyyy")
            .replace("YY", "yy")

        // Restore normalized SimpleDateFormat tokens
        p = p.replace("\u0001", "EEEE") // Sunday
        p = p.replace("\u0002", "EEE")  // Sun
        p = p.replace("\u0003", "dd")   // 06
        p = p.replace("\u0004", "d")    // 6

        return p
    }

    fun formatDayHeader(epochMs: Long, mode: LanguageMode): String {
        val cal = Calendar.getInstance().apply { timeInMillis = epochMs }
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1 = Sunday ... 7 = Saturday
        val formattedDate = formatDate(epochMs, mode)

        return when (mode) {
            LanguageMode.BANGLA -> {
                val dayNameBn = banglaDaysOfWeekFull[dayOfWeek - 1]
                "$dayNameBn, $formattedDate"
            }
            LanguageMode.ENGLISH -> {
                val dayNamesEn = arrayOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
                val dayName = dayNamesEn[dayOfWeek - 1]
                "$dayName, $formattedDate"
            }
        }
    }

    fun formatTime(epochMs: Long, mode: LanguageMode): String {
        val sdf = SimpleDateFormat("h:mm a", Locale.US)
        val formatted = sdf.format(Date(epochMs))
        return if (mode == LanguageMode.BANGLA) {
            LanguageHelper.toBanglaDigits(formatted)
                .replace("AM", "এএম")
                .replace("PM", "পিএম")
        } else {
            formatted
        }
    }

    fun formatDateFull(epochMs: Long, mode: LanguageMode): String {
        return formatDate(epochMs, mode)
    }

    fun formatDate(epochMs: Long, mode: LanguageMode, customPattern: String? = null): String {
        val rawPattern = customPattern ?: activeDateFormat
        val pattern = normalizePattern(rawPattern)
        val sdf = try {
            SimpleDateFormat(pattern, Locale.US)
        } catch (e: Exception) {
            SimpleDateFormat("dd MMM, yyyy", Locale.US)
        }
        val formattedEn = sdf.format(Date(epochMs))

        if (mode == LanguageMode.ENGLISH) {
            return formattedEn
        }

        // Translate to Bangla
        var result = formattedEn
        val enMonths = arrayOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
        val enMonthsShort = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        val enDays = arrayOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
        val enDaysShort = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

        for (i in 0..6) {
            result = result.replace(enDays[i], banglaDaysOfWeekFull[i])
            result = result.replace(enDaysShort[i], banglaDaysOfWeek[i])
        }
        for (i in 0..11) {
            result = result.replace(enMonths[i], banglaMonths[i])
            result = result.replace(enMonthsShort[i], banglaMonthsShort[i])
        }

        return LanguageHelper.toBanglaDigits(result)
    }

    fun formatShortDate(epochMs: Long, mode: LanguageMode): String {
        return formatDate(epochMs, mode)
    }

    fun formatSyncTimestamp(epochMs: Long, mode: LanguageMode): String {
        if (epochMs <= 0L) {
            return if (mode == LanguageMode.BANGLA) "কখনও নয়" else "Never"
        }
        val diff = System.currentTimeMillis() - epochMs
        if (diff < 60_000L) {
            return if (mode == LanguageMode.BANGLA) "এইমাত্র" else "Just now"
        }
        val datePart = formatDate(epochMs, mode, "dd MMM")
        val timePart = formatTime(epochMs, mode)
        return "$datePart, $timePart"
    }

    fun getStartOfMonth(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun getStartOfMonth(year: Int, month: Int): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month - 1)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun getEndOfMonth(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        return cal.timeInMillis
    }

    fun getEndOfMonth(year: Int, month: Int): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month - 1)
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        return cal.timeInMillis
    }

    fun getMonthName(month: Int, mode: LanguageMode): String {
        val monthNamesEn = arrayOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
        val monthIdx = (month - 1).coerceIn(0, 11)
        return when (mode) {
            LanguageMode.BANGLA -> banglaMonths[monthIdx]
            LanguageMode.ENGLISH -> monthNamesEn[monthIdx]
        }
    }

    fun formatMonthYear(year: Int, month: Int, mode: LanguageMode): String {
        val monthNamesEn = arrayOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
        val monthIdx = (month - 1).coerceIn(0, 11)
        return when (mode) {
            LanguageMode.BANGLA -> "${banglaMonths[monthIdx]} ${LanguageHelper.toBanglaDigits(year.toString())}"
            LanguageMode.ENGLISH -> "${monthNamesEn[monthIdx]} $year"
        }
    }

    fun getStartOfDay(epochMs: Long = System.currentTimeMillis()): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = epochMs }
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun isSameDay(epochMs1: Long, epochMs2: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { timeInMillis = epochMs1 }
        val cal2 = Calendar.getInstance().apply { timeInMillis = epochMs2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    fun calculateNextDueDate(currentDueDate: Long, recurrence: com.example.data.model.RecurrencePeriod): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = currentDueDate }
        when (recurrence) {
            com.example.data.model.RecurrencePeriod.DAILY -> cal.add(Calendar.DAY_OF_YEAR, 1)
            com.example.data.model.RecurrencePeriod.WEEKLY -> cal.add(Calendar.WEEK_OF_YEAR, 1)
            com.example.data.model.RecurrencePeriod.MONTHLY -> cal.add(Calendar.MONTH, 1)
            com.example.data.model.RecurrencePeriod.YEARLY -> cal.add(Calendar.YEAR, 1)
        }
        return cal.timeInMillis
    }
}
