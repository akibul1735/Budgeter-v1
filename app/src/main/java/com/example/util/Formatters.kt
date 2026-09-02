package com.example.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object Formatters {

    val supportedCurrencies = listOf(
        CurrencyInfo("USD", "$", "US Dollar", 1.0),
        CurrencyInfo("EUR", "€", "Euro", 0.92),
        CurrencyInfo("GBP", "£", "British Pound", 0.79),
        CurrencyInfo("JPY", "¥", "Japanese Yen", 155.0),
        CurrencyInfo("CAD", "CA$", "Canadian Dollar", 1.36),
        CurrencyInfo("AUD", "A$", "Australian Dollar", 1.52),
        CurrencyInfo("INR", "₹", "Indian Rupee", 83.5)
    )

    fun formatCurrency(amount: Double, currencyCode: String = "USD"): String {
        val curr = supportedCurrencies.find { it.code == currencyCode } ?: supportedCurrencies.first()
        val convertedAmount = amount * curr.exchangeRateToUSD
        val formatter = NumberFormat.getCurrencyInstance(Locale.US)
        formatter.currency = java.util.Currency.getInstance(if (curr.code in listOf("USD", "EUR", "GBP", "JPY", "CAD", "AUD", "INR")) curr.code else "USD")
        return try {
            formatter.format(convertedAmount)
        } catch (e: Exception) {
            "${curr.symbol}${String.format(Locale.US, "%,.2f", convertedAmount)}"
        }
    }

    fun formatDate(epochMs: Long, pattern: String = "MMM dd, yyyy"): String {
        val sdf = SimpleDateFormat(pattern, Locale.getDefault())
        return sdf.format(Date(epochMs))
    }

    fun formatTime(epochMs: Long): String {
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        return sdf.format(Date(epochMs))
    }

    fun formatRelativeDate(epochMs: Long): String {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply { timeInMillis = epochMs }

        val isToday = now.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)

        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val isYesterday = yesterday.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                yesterday.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)

        return when {
            isToday -> "Today"
            isYesterday -> "Yesterday"
            else -> formatDate(epochMs, "EEE, MMM d")
        }
    }
}

data class CurrencyInfo(
    val code: String,
    val symbol: String,
    val name: String,
    val exchangeRateToUSD: Double
)
