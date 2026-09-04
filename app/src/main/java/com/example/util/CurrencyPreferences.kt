package com.example.util

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class CurrencyDisplayMode(val titleEn: String, val titleBn: String) {
    SYMBOL_ONLY("Symbol (৳)", "প্রতীক (৳)"),
    CODE_ONLY("Code (BDT)", "কোড (BDT)"),
    CODE_AND_SYMBOL("Code & Symbol (BDT ৳)", "কোড ও প্রতীক (BDT ৳)"),
    NONE("Hide Currency", "মুদ্রা লুকান")
}

data class CurrencyItem(
    val code: String,
    val symbol: String,
    val nameEn: String,
    val nameBn: String
)

data class CurrencyConfig(
    val selectedCode: String = "BDT",
    val selectedSymbol: String = "৳",
    val displayMode: CurrencyDisplayMode = CurrencyDisplayMode.SYMBOL_ONLY,
    val customSymbol: String = "",
    val customCode: String = ""
) {
    val activeSymbol: String
        get() = if (customSymbol.isNotBlank()) customSymbol else selectedSymbol

    val activeCode: String
        get() = if (customCode.isNotBlank()) customCode else selectedCode
}

class CurrencyPreferences private constructor(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("budgeter_currency_prefs", Context.MODE_PRIVATE)

    private val _config = MutableStateFlow(loadConfig())
    val config: StateFlow<CurrencyConfig> = _config.asStateFlow()

    init {
        LanguageHelper.updateCurrencyConfig(_config.value)
    }

    private fun loadConfig(): CurrencyConfig {
        val code = prefs.getString(KEY_CODE, "BDT") ?: "BDT"
        val symbol = prefs.getString(KEY_SYMBOL, "৳") ?: "৳"
        val modeStr = prefs.getString(KEY_MODE, CurrencyDisplayMode.SYMBOL_ONLY.name) ?: CurrencyDisplayMode.SYMBOL_ONLY.name
        val mode = try { CurrencyDisplayMode.valueOf(modeStr) } catch (e: Exception) { CurrencyDisplayMode.SYMBOL_ONLY }
        val customSymbol = prefs.getString(KEY_CUSTOM_SYMBOL, "") ?: ""
        val customCode = prefs.getString(KEY_CUSTOM_CODE, "") ?: ""
        return CurrencyConfig(
            selectedCode = code,
            selectedSymbol = symbol,
            displayMode = mode,
            customSymbol = customSymbol,
            customCode = customCode
        )
    }

    fun setCurrency(currency: CurrencyItem) {
        prefs.edit()
            .putString(KEY_CODE, currency.code)
            .putString(KEY_SYMBOL, currency.symbol)
            .apply()
        val newConfig = _config.value.copy(
            selectedCode = currency.code,
            selectedSymbol = currency.symbol,
            customSymbol = "",
            customCode = ""
        )
        _config.value = newConfig
        LanguageHelper.updateCurrencyConfig(newConfig)
    }

    fun setDisplayMode(mode: CurrencyDisplayMode) {
        prefs.edit().putString(KEY_MODE, mode.name).apply()
        val newConfig = _config.value.copy(displayMode = mode)
        _config.value = newConfig
        LanguageHelper.updateCurrencyConfig(newConfig)
    }

    fun setCustomCurrency(code: String, symbol: String) {
        prefs.edit()
            .putString(KEY_CUSTOM_CODE, code)
            .putString(KEY_CUSTOM_SYMBOL, symbol)
            .apply()
        val newConfig = _config.value.copy(
            customCode = code,
            customSymbol = symbol
        )
        _config.value = newConfig
        LanguageHelper.updateCurrencyConfig(newConfig)
    }

    companion object {
        private const val KEY_CODE = "currency_code"
        private const val KEY_SYMBOL = "currency_symbol"
        private const val KEY_MODE = "currency_display_mode"
        private const val KEY_CUSTOM_SYMBOL = "currency_custom_symbol"
        private const val KEY_CUSTOM_CODE = "currency_custom_code"

        val POPULAR_CURRENCIES = listOf(
            CurrencyItem("BDT", "৳", "Bangladeshi Taka", "বাংলাদেশি টাকা"),
            CurrencyItem("USD", "$", "US Dollar", "মার্কিন ডলার"),
            CurrencyItem("EUR", "€", "Euro", "ইউরো"),
            CurrencyItem("GBP", "£", "British Pound", "ব্রিটিশ পাউন্ড"),
            CurrencyItem("INR", "₹", "Indian Rupee", "ভারতীয় রুপি"),
            CurrencyItem("CAD", "C$", "Canadian Dollar", "কানাডিয়ান ডলার"),
            CurrencyItem("AUD", "A$", "Australian Dollar", "অস্ট্রেলিয়ান ডলার"),
            CurrencyItem("SAR", "﷼", "Saudi Riyal", "সৌদি রিয়াল"),
            CurrencyItem("AED", "د.إ", "UAE Dirham", "ইউএই দিরহাম"),
            CurrencyItem("JPY", "¥", "Japanese Yen", "জাপানি ইয়েন"),
            CurrencyItem("CNY", "¥", "Chinese Yuan", "চীনা ইউয়ান"),
            CurrencyItem("MYR", "RM", "Malaysian Ringgit", "মালয়েশিয়ান রিঙ্গিত"),
            CurrencyItem("SGD", "S$", "Singapore Dollar", "সিঙ্গাপুর ডলার"),
            CurrencyItem("KWD", "KD", "Kuwaiti Dinar", "কুয়েতি দিনার"),
            CurrencyItem("QAR", "QR", "Qatari Riyal", "কাতারি রিয়াল"),
            CurrencyItem("TRY", "₺", "Turkish Lira", "তুর্কি লিরা"),
            CurrencyItem("PKR", "₨", "Pakistani Rupee", "পাকিস্তানি রুপি"),
            CurrencyItem("PHP", "₱", "Philippine Peso", "ফিলিপাইন পেসো"),
            CurrencyItem("KRW", "₩", "South Korean Won", "দক্ষিণ কোরিয়ান ওন"),
            CurrencyItem("IDR", "Rp", "Indonesian Rupiah", "ইন্দোনেশিয়ান রূপিয়া"),
            CurrencyItem("BRL", "R$", "Brazilian Real", "ব্রাজিলিয়ান রিয়াল"),
            CurrencyItem("ZAR", "R", "South African Rand", "দক্ষিণ আফ্রিকান র‍্যান্ড")
        )

        @Volatile
        private var INSTANCE: CurrencyPreferences? = null

        fun getInstance(context: Context): CurrencyPreferences {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: CurrencyPreferences(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
