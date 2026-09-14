package com.example.ui.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.LanguageMode
import com.example.ui.components.AppTabHeader
import com.example.ui.theme.SolidPrimary
import com.example.ui.viewmodel.BudgetViewModel
import com.example.util.CurrencyConfig
import com.example.util.CurrencyDisplayMode
import com.example.util.CurrencyItem
import com.example.util.CurrencyPreferences
import com.example.util.LanguageHelper

private data class WorldCurrency(
    val code: String,
    val symbol: String,
    val nameEn: String,
    val nameBn: String,
    val region: String
) {
    fun toCurrencyItem(): CurrencyItem = CurrencyItem(code, symbol, nameEn, nameBn)
}

private val ALL_WORLD_CURRENCIES = listOf(
    WorldCurrency("BDT", "৳", "Bangladeshi Taka", "বাংলাদেশি টাকা", "South Asia"),
    WorldCurrency("USD", "$", "US Dollar", "মার্কিন ডলার", "Americas"),
    WorldCurrency("EUR", "€", "Euro", "ইউরো", "Europe"),
    WorldCurrency("GBP", "£", "British Pound", "ব্রিটিশ পাউন্ড", "Europe"),
    WorldCurrency("INR", "₹", "Indian Rupee", "ভারতীয় রুপি", "South Asia"),
    WorldCurrency("SAR", "﷼", "Saudi Riyal", "সৌদি রিয়াল", "Middle East"),
    WorldCurrency("AED", "د.إ", "UAE Dirham", "ইউএই দিরহাম", "Middle East"),
    WorldCurrency("CAD", "C$", "Canadian Dollar", "কানাডিয়ান ডলার", "Americas"),
    WorldCurrency("AUD", "A$", "Australian Dollar", "অস্ট্রেলিয়ান ডলার", "Americas & Others"),
    WorldCurrency("JPY", "¥", "Japanese Yen", "জাপানি ইয়েন", "Asia"),
    WorldCurrency("CNY", "¥", "Chinese Yuan", "চীনা ইউয়ান", "Asia"),
    WorldCurrency("MYR", "RM", "Malaysian Ringgit", "মালয়েশিয়ান রিঙ্গিত", "Asia"),
    WorldCurrency("SGD", "S$", "Singapore Dollar", "সিঙ্গাপুর ডলার", "Asia"),
    WorldCurrency("KWD", "KD", "Kuwaiti Dinar", "কুয়েতি দিনার", "Middle East"),
    WorldCurrency("QAR", "QR", "Qatari Riyal", "কাতারি রিয়াল", "Middle East"),
    WorldCurrency("OMR", "OMR", "Omani Rial", "ওমানি রিয়াল", "Middle East"),
    WorldCurrency("BHD", "BD", "Bahraini Dinar", "বাহরাইনি দিনার", "Middle East"),
    WorldCurrency("TRY", "₺", "Turkish Lira", "তুর্কি লিরা", "Europe"),
    WorldCurrency("PKR", "₨", "Pakistani Rupee", "পাকিস্তানি রুপি", "South Asia"),
    WorldCurrency("NPR", "रू", "Nepalese Rupee", "নেপালি রুপি", "South Asia"),
    WorldCurrency("LKR", "Rs", "Sri Lankan Rupee", "শ্রীলঙ্কান রুপি", "South Asia"),
    WorldCurrency("PHP", "₱", "Philippine Peso", "ফিলিপাইন পেসো", "Asia"),
    WorldCurrency("KRW", "₩", "South Korean Won", "দক্ষিণ কোরিয়ান ওন", "Asia"),
    WorldCurrency("IDR", "Rp", "Indonesian Rupiah", "ইন্দোনেশিয়ান রূপিয়া", "Asia"),
    WorldCurrency("THB", "฿", "Thai Baht", "থাই বাথ", "Asia"),
    WorldCurrency("VND", "₫", "Vietnamese Dong", "ভিয়েতনামিজ ডং", "Asia"),
    WorldCurrency("CHF", "CHF", "Swiss Franc", "সুইস ফ্রাঁ", "Europe"),
    WorldCurrency("RUB", "₽", "Russian Ruble", "রাশিয়ান রুবল", "Europe"),
    WorldCurrency("BRL", "R$", "Brazilian Real", "ব্রাজিলিয়ান রিয়াল", "Americas"),
    WorldCurrency("ZAR", "R", "South African Rand", "দক্ষিণ আফ্রিকান র‍্যান্ড", "Americas & Others"),
    WorldCurrency("EGP", "E£", "Egyptian Pound", "মিশরীয় পাউন্ড", "Middle East"),
    WorldCurrency("NGN", "₦", "Nigerian Naira", "নাইজেরিয়ান নাইরা", "Americas & Others")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencySettingsPage(
    viewModel: BudgetViewModel,
    languageMode: LanguageMode,
    onBack: () -> Unit
) {
    val currencyConfig by viewModel.currencyConfig.collectAsStateWithLifecycle()
    val isBangla = languageMode == LanguageMode.BANGLA

    // Modal Sheet State for the Searchable Currency Picker
    var showCurrencyPicker by remember { mutableStateOf(false) }
    var showDisplayModeSheet by remember { mutableStateOf(false) }
    var showCustomCurrencyCard by remember { mutableStateOf(currencyConfig.customCode.isNotBlank() || currencyConfig.customSymbol.isNotBlank()) }

    var customCodeInput by remember(currencyConfig) { mutableStateOf(currencyConfig.customCode) }
    var customSymbolInput by remember(currencyConfig) { mutableStateOf(currencyConfig.customSymbol) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("currency_settings_page")
    ) {
        AppTabHeader(
            title = if (isBangla) "মুদ্রা ও প্রতীক" else "Currency Setup",
            tabIcon = Icons.Default.CurrencyExchange,
            onBack = onBack,
            autoHideOnScroll = false
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(modifier = Modifier.height(2.dp))

            // 1. Live Preview Hero Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isBangla) "লাইভ কারেন্সি প্রিভিউ" else "Active Currency Preview",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = LanguageHelper.formatCurrency(54250.00, languageMode),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = currencyConfig.activeSymbol,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = currencyConfig.activeCode,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // 2. Section: Currency Configuration
            Text(
                text = if (isBangla) "মুদ্রা কনফিগারেশন" else "Currency Configuration",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Child 1: Default Currency (Opens Searchable Currency Picker)
            val currentItem = ALL_WORLD_CURRENCIES.find { it.code == currencyConfig.activeCode }
            val currentCurrencyName = if (currentItem != null) {
                if (isBangla) currentItem.nameBn else currentItem.nameEn
            } else {
                if (isBangla) "কাস্টম মুদ্রা" else "Custom Currency"
            }

            OutlinedCard(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showCurrencyPicker = true }
                    .testTag("default_currency_row")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = currencyConfig.activeSymbol,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isBangla) "ডিফল্ট মুদ্রা" else "Default Currency",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${currencyConfig.activeCode} • $currentCurrencyName",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = if (isBangla) "পরিবর্তন" else "Change",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                        modifier = Modifier.size(13.dp)
                    )
                }
            }

            // Child 2: Display Mode & Placement (Opens Placement Selector)
            val modeTitle = when (currencyConfig.displayMode) {
                CurrencyDisplayMode.SYMBOL_ONLY -> if (isBangla) "শুধু প্রতীক (${currencyConfig.activeSymbol}500)" else "Symbol Only (${currencyConfig.activeSymbol}500)"
                CurrencyDisplayMode.CODE_ONLY -> if (isBangla) "শুধু কোড (${currencyConfig.activeCode} 500)" else "Code Only (${currencyConfig.activeCode} 500)"
                CurrencyDisplayMode.CODE_AND_SYMBOL -> if (isBangla) "উভয় (${currencyConfig.activeCode} ${currencyConfig.activeSymbol}500)" else "Code & Symbol (${currencyConfig.activeCode} ${currencyConfig.activeSymbol}500)"
                CurrencyDisplayMode.NONE -> if (isBangla) "মুদ্রা প্রতীক লুকান (500)" else "Hide Currency (500)"
            }

            OutlinedCard(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDisplayModeSheet = true }
                    .testTag("display_mode_row")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isBangla) "মুদ্রা প্রদর্শনের ধরন (প্লেসমেন্ট)" else "Display Mode & Placement",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = modeTitle,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                        modifier = Modifier.size(13.dp)
                    )
                }
            }

            // Child 3: Custom Symbol & Code
            OutlinedCard(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showCustomCurrencyCard = !showCustomCurrencyCard },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (isBangla) "কাস্টম মুদ্রা ও প্রতীক" else "Custom Currency & Symbol",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (currencyConfig.customCode.isNotBlank() || currencyConfig.customSymbol.isNotBlank())
                                        "${currencyConfig.customCode} (${currencyConfig.customSymbol})"
                                    else
                                        if (isBangla) "প্রয়োজন অনুসারে নিজস্ব কোড বা প্রতীক লিখুন" else "Enter your own custom code or symbol",
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }

                        Icon(
                            imageVector = if (showCustomCurrencyCard) Icons.Default.Close else Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                            modifier = Modifier.size(13.dp)
                        )
                    }

                    AnimatedVisibility(
                        visible = showCustomCurrencyCard,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = customCodeInput,
                                    onValueChange = {
                                        customCodeInput = it
                                        viewModel.setCustomCurrency(it, customSymbolInput)
                                    },
                                    label = { Text(if (isBangla) "কোড (যেমন BDT)" else "Code (e.g. BDT)", fontSize = 11.sp) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = customSymbolInput,
                                    onValueChange = {
                                        customSymbolInput = it
                                        viewModel.setCustomCurrency(customCodeInput, it)
                                    },
                                    label = { Text(if (isBangla) "প্রতীক (যেমন ৳)" else "Symbol (e.g. ৳)", fontSize = 11.sp) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            if (customCodeInput.isNotBlank() || customSymbolInput.isNotBlank()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            customCodeInput = ""
                                            customSymbolInput = ""
                                            viewModel.setCustomCurrency("", "")
                                        },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(if (isBangla) "রিসেট করুন" else "Clear Custom", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // =========================================================================
    // POLISHED SEARCHABLE CURRENCY PICKER MODAL BOTTOM SHEET
    // =========================================================================
    if (showCurrencyPicker) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showCurrencyPicker = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            CurrencyPickerContent(
                activeCode = currencyConfig.activeCode,
                languageMode = languageMode,
                onSelectCurrency = { selected ->
                    viewModel.setCurrency(selected.toCurrencyItem())
                    showCurrencyPicker = false
                },
                onClose = { showCurrencyPicker = false }
            )
        }
    }

    // =========================================================================
    // DISPLAY MODE PICKER MODAL BOTTOM SHEET
    // =========================================================================
    if (showDisplayModeSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showDisplayModeSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isBangla) "মুদ্রা প্রদর্শনের ধরন নির্বাচন করুন" else "Select Display Mode & Placement",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = { showDisplayModeSheet = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                CurrencyDisplayMode.values().forEach { mode ->
                    val isSelected = currencyConfig.displayMode == mode
                    val title = when (mode) {
                        CurrencyDisplayMode.SYMBOL_ONLY -> if (isBangla) "শুধু প্রতীক" else "Symbol Only"
                        CurrencyDisplayMode.CODE_ONLY -> if (isBangla) "শুধু কোড" else "Code Only"
                        CurrencyDisplayMode.CODE_AND_SYMBOL -> if (isBangla) "উভয় (কোড ও প্রতীক)" else "Code & Symbol"
                        CurrencyDisplayMode.NONE -> if (isBangla) "কোনোটি না (মুদ্রা লুকান)" else "Hide Currency"
                    }
                    val sample = when (mode) {
                        CurrencyDisplayMode.SYMBOL_ONLY -> "${currencyConfig.activeSymbol}54,250"
                        CurrencyDisplayMode.CODE_ONLY -> "${currencyConfig.activeCode} 54,250"
                        CurrencyDisplayMode.CODE_AND_SYMBOL -> "${currencyConfig.activeCode} ${currencyConfig.activeSymbol}54,250"
                        CurrencyDisplayMode.NONE -> "54,250"
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                viewModel.setCurrencyDisplayMode(mode)
                                showDisplayModeSheet = false
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    viewModel.setCurrencyDisplayMode(mode)
                                    showDisplayModeSheet = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = title,
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Example: $sample",
                                    fontSize = 11.5.sp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

/**
 * Polished Searchable Currency Picker Content
 * Features a top Search Bar, Region Filter Chips, and list of world currencies with instant selection.
 */
@Composable
private fun CurrencyPickerContent(
    activeCode: String,
    languageMode: LanguageMode,
    onSelectCurrency: (WorldCurrency) -> Unit,
    onClose: () -> Unit
) {
    val isBangla = languageMode == LanguageMode.BANGLA
    var searchQuery by remember { mutableStateOf("") }
    var selectedRegionFilter by remember { mutableStateOf<String?>(null) }

    val regions = listOf("All", "Popular", "South Asia", "Middle East", "Asia", "Europe", "Americas")

    val filteredCurrencies by remember(searchQuery, selectedRegionFilter) {
        derivedStateOf {
            ALL_WORLD_CURRENCIES.filter { item ->
                val matchesSearch = if (searchQuery.isBlank()) {
                    true
                } else {
                    val query = searchQuery.trim().lowercase()
                    item.code.lowercase().contains(query) ||
                            item.symbol.lowercase().contains(query) ||
                            item.nameEn.lowercase().contains(query) ||
                            item.nameBn.lowercase().contains(query)
                }

                val matchesRegion = when (selectedRegionFilter) {
                    null, "All" -> true
                    "Popular" -> item.code in listOf("BDT", "USD", "EUR", "GBP", "INR", "SAR", "AED", "CAD", "AUD", "JPY", "MYR", "SGD")
                    "Americas" -> item.region.contains("Americas")
                    else -> item.region.contains(selectedRegionFilter ?: "")
                }

                matchesSearch && matchesRegion
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.85f)
            .padding(horizontal = 16.dp)
            .testTag("currency_picker_modal")
    ) {
        // Top Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (isBangla) "মুদ্রা নির্বাচন করুন" else "Select Default Currency",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isBangla) "যেকোনো মুদ্রা খুঁজতে নাম বা কোড লিখুন" else "Search by currency name, code, or symbol",
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = {
                Text(
                    text = if (isBangla) "মুদ্রা খুঁজুন (যেমন BDT, USD, ৳, Taka)..." else "Search currency (e.g. BDT, USD, ৳, Taka)...",
                    fontSize = 13.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("currency_search_input")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Region Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            regions.forEach { region ->
                val isSelected = (selectedRegionFilter == null && region == "All") || (selectedRegionFilter == region)
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        selectedRegionFilter = if (region == "All") null else region
                    },
                    label = {
                        Text(
                            text = when (region) {
                                "All" -> if (isBangla) "সকল" else "All"
                                "Popular" -> if (isBangla) "জনপ্রিয়" else "Popular"
                                "South Asia" -> if (isBangla) "দক্ষিণ এশিয়া" else "South Asia"
                                "Middle East" -> if (isBangla) "মধ্যপ্রাচ্য" else "Middle East"
                                "Asia" -> if (isBangla) "এশিয়া" else "Asia"
                                "Europe" -> if (isBangla) "ইউরোপ" else "Europe"
                                "Americas" -> if (isBangla) "আমেরিকা" else "Americas"
                                else -> region
                            },
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Currency Items List
        if (filteredCurrencies.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CurrencyExchange,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(42.dp)
                    )
                    Text(
                        text = if (isBangla) "কোনো মুদ্রা পাওয়া যায়নি" else "No matching currency found",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isBangla) "'$searchQuery' দিয়ে কাস্টম মুদ্রায় সেট করতে পারেন" else "You can enter '$searchQuery' in Custom Currency",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(filteredCurrencies, key = { it.code }) { item ->
                    val isSelected = item.code == activeCode
                    val displayName = if (isBangla) item.nameBn else item.nameEn

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectCurrency(item) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 11.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Circular symbol avatar
                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = item.symbol,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = item.code,
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "(${item.symbol})",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                                Text(
                                    text = displayName,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
