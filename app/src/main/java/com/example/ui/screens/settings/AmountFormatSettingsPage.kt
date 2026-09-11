package com.example.ui.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.LanguageMode
import com.example.ui.components.AppTabHeader
import com.example.ui.viewmodel.BudgetViewModel
import com.example.util.AmountFormatConfig
import com.example.util.AmountSeparatorPreset
import com.example.util.LanguageHelper
import com.example.util.NumberGroupingStyle

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AmountFormatSettingsPage(
    viewModel: BudgetViewModel,
    languageMode: LanguageMode,
    onBack: () -> Unit
) {
    val amountFormatConfig by viewModel.amountFormatConfig.collectAsStateWithLifecycle()
    val currencyConfig by viewModel.currencyConfig.collectAsStateWithLifecycle()
    val isBangla = languageMode == LanguageMode.BANGLA

    var customGroupingInput by remember(amountFormatConfig) {
        mutableStateOf(amountFormatConfig.customGroupingSeparator)
    }
    var customDecimalInput by remember(amountFormatConfig) {
        mutableStateOf(amountFormatConfig.customDecimalSeparator.ifBlank { "." })
    }
    var customStyleSelection by remember(amountFormatConfig) {
        mutableStateOf(amountFormatConfig.customGroupingStyle)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("amount_format_settings_page")
    ) {
        AppTabHeader(
            title = if (isBangla) "টাকার কমা ও সেপারেটর" else "Amount Comma Separator",
            tabIcon = Icons.Default.Numbers,
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

            // 1. Live Preview Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isBangla) "লাইভ প্রিভিউ (সব জায়গায় অনুসরণ হবে)" else "Live Preview (Applied Everywhere)",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = if (amountFormatConfig.preset == AmountSeparatorPreset.CUSTOM) {
                                    if (isBangla) "কাস্টম" else "Custom"
                                } else {
                                    if (isBangla) amountFormatConfig.preset.titleBn.substringBefore(" (") else amountFormatConfig.preset.titleEn.substringBefore(" (")
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    // Main large amount preview
                    Column {
                        Text(
                            text = LanguageHelper.formatCurrency(12345678.90, languageMode),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (isBangla) "উদাহরণ: বড় অঙ্ক (কোটি / মিলিয়ন)" else "Example: Large figure (Millions / Crores)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                        )
                    }

                    // Two smaller previews
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = LanguageHelper.formatCurrency(125450.50, languageMode),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isBangla) "মাঝারি অঙ্ক" else "Medium amount",
                                fontSize = 10.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = LanguageHelper.formatCurrency(1250.00, languageMode),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isBangla) "ছোট অঙ্ক" else "Small amount",
                                fontSize = 10.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            // 2. Common Format Suggestions Header
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = if (isBangla) "প্রস্তাবিত সাধারণ ফরম্যাটসমূহ" else "Common Format Suggestions",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (isBangla) "নিচের যেকোনো জনপ্রিয় ফরম্যাট সিলেক্ট করুন, এটি অ্যাপের সর্বত্র অনুসরণ করা হবে।"
                    else "Select a popular format below. It will be followed everywhere throughout the app.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Presets Cards List
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AmountSeparatorPreset.entries.forEach { preset ->
                    val isSelected = amountFormatConfig.preset == preset
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.setAmountFormatPreset(preset)
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        ),
                        border = BorderStroke(
                            if (isSelected) 1.5.dp else 0.5.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = if (isSelected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(20.dp)
                                )

                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = if (isBangla) preset.titleBn else preset.titleEn,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )

                                    val subtitleText = when (preset) {
                                        AmountSeparatorPreset.STANDARD_COMMA ->
                                            if (isBangla) "হাজারের জন্য কমা (,), দশমিকের জন্য ডট (.)" else "Thousands separator (,), decimal dot (.)"
                                        AmountSeparatorPreset.SOUTH_ASIAN_LAKH_CRORE ->
                                            if (isBangla) "প্রথমে ৩ অঙ্ক, এরপর প্রতি ২ অঙ্কে কমা (১২,৩৪,৫৬৭)" else "First 3 digits, then every 2 digits (12,34,567)"
                                        AmountSeparatorPreset.EUROPEAN_DOT ->
                                            if (isBangla) "হাজারের জন্য ডট (.), দশমিকের জন্য কমা (,)" else "Thousands dot (.), decimal comma (,)"
                                        AmountSeparatorPreset.SPACE_SEPARATOR ->
                                            if (isBangla) "হাজারের জন্য স্পেস ( ), দশমিকের জন্য ডট (.)" else "Thousands space ( ), decimal dot (.)"
                                        AmountSeparatorPreset.SWISS_APOSTROPHE ->
                                            if (isBangla) "হাজারের জন্য অ্যাপোস্ট্রফি ('), দশমিকের জন্য ডট (.)" else "Thousands apostrophe ('), decimal dot (.)"
                                        AmountSeparatorPreset.NO_SEPARATOR ->
                                            if (isBangla) "কোনো কমা বা সেপারেটর ছাড়াই সংখ্যা" else "Continuous digits without thousands grouping"
                                        AmountSeparatorPreset.CUSTOM ->
                                            if (isBangla) "নিজের পছন্দমতো সেপারেটর ও গ্রুপিং নির্ধারণ করুন" else "Define custom separators and grouping style"
                                    }
                                    Text(
                                        text = subtitleText,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Sample Badge
                            val sampleFormatted = if (preset == AmountSeparatorPreset.CUSTOM) {
                                LanguageHelper.formatAmountNumber(
                                    value = 1234567.89,
                                    mode = languageMode,
                                    groupingSeparator = amountFormatConfig.customGroupingSeparator,
                                    decimalSeparator = amountFormatConfig.customDecimalSeparator.ifBlank { "." },
                                    groupingStyle = amountFormatConfig.customGroupingStyle,
                                    decimalPlaces = 2
                                )
                            } else {
                                if (isBangla) preset.exampleBn else preset.exampleEn
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Text(
                                    text = sampleFormatted,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 3. Custom Format Configuration Section
            val isCustomActive = amountFormatConfig.preset == AmountSeparatorPreset.CUSTOM
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCustomActive) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                ),
                border = BorderStroke(
                    if (isCustomActive) 1.5.dp else 0.5.dp,
                    if (isCustomActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isBangla) "🛠️ কাস্টম সেপারেটর কনফিগারেশন" else "🛠️ Custom Separator Configuration",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = if (isBangla) "আপনার পছন্দমতো সেপারেটর চিহ্ন ও গ্রুপিং নির্বাচন করুন"
                                else "Choose your preferred grouping symbol & decimal separator",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (!isCustomActive) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.setCustomAmountFormat(
                                        groupingSeparator = customGroupingInput,
                                        decimalSeparator = customDecimalInput,
                                        groupingStyle = customStyleSelection
                                    )
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(if (isBangla) "সক্রিয় করুন" else "Activate", fontSize = 11.sp)
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    // A. Grouping Separator Selection
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = if (isBangla) "১. হাজারের কমা / গ্রুপিং চিহ্ন (Grouping Separator)" else "1. Thousands / Grouping Separator",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        val commonGroupingSuggestions = listOf(
                            "," to (if (isBangla) "কমা (,)" else "Comma (,)"),
                            "." to (if (isBangla) "ডট (.)" else "Dot (.)"),
                            " " to (if (isBangla) "স্পেস ( )" else "Space ( )"),
                            "'" to (if (isBangla) "অ্যাপোস্ট্রফি (')" else "Apostrophe (')"),
                            "_" to (if (isBangla) "আন্ডারস্কোর (_)" else "Underscore (_)"),
                            "" to (if (isBangla) "চিহ্ন ছাড়া" else "None")
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            commonGroupingSuggestions.forEach { (sep, label) ->
                                val isChipSelected = customGroupingInput == sep
                                FilterChip(
                                    selected = isChipSelected,
                                    onClick = {
                                        customGroupingInput = sep
                                        viewModel.setCustomAmountFormat(
                                            groupingSeparator = sep,
                                            decimalSeparator = customDecimalInput,
                                            groupingStyle = customStyleSelection
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = label,
                                            fontSize = 11.sp,
                                            fontWeight = if (isChipSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        }

                        // Custom Grouping Input
                        OutlinedTextField(
                            value = customGroupingInput,
                            onValueChange = {
                                customGroupingInput = it
                                viewModel.setCustomAmountFormat(
                                    groupingSeparator = it,
                                    decimalSeparator = customDecimalInput,
                                    groupingStyle = customStyleSelection
                                )
                            },
                            label = { Text(if (isBangla) "কাস্টম গ্রুপিং চিহ্ন টাইপ করুন" else "Type Custom Grouping Symbol") },
                            placeholder = { Text(if (isBangla) "যেমন: , বা . বা স্পেস বা ' বা _" else "e.g. , or . or space or '") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    // B. Decimal Separator Selection
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = if (isBangla) "২. দশমিক চিহ্ন (Decimal Separator)" else "2. Decimal Separator",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        val commonDecimalSuggestions = listOf(
                            "." to (if (isBangla) "ডট (.)" else "Dot (.)"),
                            "," to (if (isBangla) "কমা (,)" else "Comma (,)"),
                            "/" to (if (isBangla) "স্ল্যাশ (/)" else "Slash (/)")
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            commonDecimalSuggestions.forEach { (dec, label) ->
                                val isChipSelected = customDecimalInput == dec
                                FilterChip(
                                    selected = isChipSelected,
                                    onClick = {
                                        customDecimalInput = dec
                                        viewModel.setCustomAmountFormat(
                                            groupingSeparator = customGroupingInput,
                                            decimalSeparator = dec,
                                            groupingStyle = customStyleSelection
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = label,
                                            fontSize = 11.sp,
                                            fontWeight = if (isChipSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        }

                        // Custom Decimal Input
                        OutlinedTextField(
                            value = customDecimalInput,
                            onValueChange = {
                                customDecimalInput = it
                                viewModel.setCustomAmountFormat(
                                    groupingSeparator = customGroupingInput,
                                    decimalSeparator = it,
                                    groupingStyle = customStyleSelection
                                )
                            },
                            label = { Text(if (isBangla) "কাস্টম দশমিক চিহ্ন টাইপ করুন" else "Type Custom Decimal Symbol") },
                            placeholder = { Text(if (isBangla) "যেমন: . বা ," else "e.g. . or ,") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    // C. Grouping System / Style
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = if (isBangla) "৩. গ্রুপিং পদ্ধতি (Grouping Style)" else "3. Grouping Style",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        NumberGroupingStyle.entries.forEach { style ->
                            val isStyleSelected = customStyleSelection == style
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        customStyleSelection = style
                                        viewModel.setCustomAmountFormat(
                                            groupingSeparator = customGroupingInput,
                                            decimalSeparator = customDecimalInput,
                                            groupingStyle = style
                                        )
                                    }
                                    .padding(vertical = 6.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (isStyleSelected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (isStyleSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = if (isBangla) style.titleBn else style.titleEn,
                                    fontSize = 12.sp,
                                    fontWeight = if (isStyleSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isStyleSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // Save / Apply Button
                    Button(
                        onClick = {
                            viewModel.setCustomAmountFormat(
                                groupingSeparator = customGroupingInput,
                                decimalSeparator = customDecimalInput,
                                groupingStyle = customStyleSelection
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isBangla) "কাস্টম ফরম্যাট সেভ ও প্রয়োগ করুন" else "Save & Apply Custom Format")
                    }
                }
            }

            // 4. Reset to Default Button
            OutlinedButton(
                onClick = {
                    viewModel.resetAmountFormatToDefaults()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (isBangla) "ডিফল্ট সেটিংসে ফিরিয়ে নিন" else "Reset to Default Format", fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
