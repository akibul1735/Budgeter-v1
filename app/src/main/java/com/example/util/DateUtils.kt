package com.example.util

import com.example.data.model.LanguageMode
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {

    private val banglaMonths = arrayOf(
        "জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন",
        "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর"
    )

    fun formatDate(epochMs: Long, mode: LanguageMode): String {
        val cal = Calendar.getInstance().apply { timeInMillis = epochMs }
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val monthIdx = cal.get(Calendar.MONTH)
        val year = cal.get(Calendar.YEAR)

        return when (mode) {
            LanguageMode.ENGLISH -> {
                val sdf = SimpleDateFormat("dd MMM, yyyy", Locale.US)
                sdf.format(Date(epochMs))
            }
            LanguageMode.BANGLA -> {
                val dayBn = LanguageHelper.toBanglaDigits(day.toString())
                val monthBn = banglaMonths[monthIdx]
                val yearBn = LanguageHelper.toBanglaDigits(year.toString())
                "$dayBn $monthBn, $yearBn"
            }
            LanguageMode.BILINGUAL -> {
                val sdf = SimpleDateFormat("dd MMM", Locale.US)
                val dayBn = LanguageHelper.toBanglaDigits(day.toString())
                val monthBn = banglaMonths[monthIdx]
                "${sdf.format(Date(epochMs))} ($dayBn $monthBn)"
            }
        }
    }

    fun formatShortDate(epochMs: Long, mode: LanguageMode): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.US)
        val formatted = sdf.format(Date(epochMs))
        return if (mode == LanguageMode.BANGLA) {
            LanguageHelper.toBanglaDigits(formatted)
        } else {
            formatted
        }
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

    fun getEndOfMonth(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        return cal.timeInMillis
    }

    fun getStartOfDay(epochMs: Long = System.currentTimeMillis()): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = epochMs }
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
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
