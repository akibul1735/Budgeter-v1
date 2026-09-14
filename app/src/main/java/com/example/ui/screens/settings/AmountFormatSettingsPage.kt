package com.example.ui.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmountFormatSettingsPage(
    viewModel: BudgetViewModel,
    languageMode: LanguageMode,
    onBack: () -> Unit
) {
    val amountFormatConfig by viewModel.amountFormatConfig.collectAsStateWithLifecycle()
    val isBangla = languageMode == LanguageMode.BANGLA

    var showPresetPickerSheet by remember { mutableStateOf(false) }
    var showGroupingStyleSheet by remember { mutableStateOf(false) }
    var showCustomEditor by remember { mutableStateOf(amountFormatConfig.preset == AmountSeparatorPreset.CUSTOM) }

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
            title = if (isBangla) "টাকার কমা ও সেপারেটর" else "Amount Formatting",
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

            // 1. Live Preview Hero Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
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
                            text = if (isBangla) "লাইভ প্রিভিউ" else "Live Format Preview",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = if (amountFormatConfig.preset == AmountSeparatorPreset.CUSTOM) "Custom" else amountFormatConfig.preset.name,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    val previewLargeNumber = 1234567.89
                    val formattedLarge = LanguageHelper.formatCurrency(previewLargeNumber, languageMode)
                    val formattedSmall = LanguageHelper.formatCurrency(450.0, languageMode)
                    val formattedTenK = LanguageHelper.formatCurrency(10500.5, languageMode)

                    Text(
                        text = formattedLarge,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                                Text(
                                    text = if (isBangla) "ছোট অঙ্ক" else "Small Amount",
                                    fontSize = 10.5.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                Text(
                                    text = formattedSmall,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                                Text(
                                    text = if (isBangla) "মাঝারি অঙ্ক" else "Medium Amount",
                                    fontSize = 10.5.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                Text(
                                    text = formattedTenK,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // 2. Primary Configuration Section
            Text(
                text = if (isBangla) "সংখ্যা ও কমা সেটিংস" else "Formatting & Grouping Options",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Preset Picker Row
            val currentPresetTitle = if (isBangla) amountFormatConfig.preset.titleBn else amountFormatConfig.preset.titleEn
            val currentPresetExample = if (isBangla) amountFormatConfig.preset.exampleBn else amountFormatConfig.preset.exampleEn

            OutlinedCard(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showPresetPickerSheet = true }
                    .testTag("preset_picker_row")
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
                            Icon(
                                imageVector = Icons.Default.FormatListNumbered,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isBangla) "কমা ও সেপারেটর প্রিসেট" else "Separator Preset",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$currentPresetTitle ($currentPresetExample)",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Select",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Number Grouping Style Row
            val currentGroupingTitle = when (amountFormatConfig.effectiveGroupingStyle) {
                NumberGroupingStyle.SOUTH_ASIAN -> if (isBangla) "দক্ষিণ এশীয় (লক্ষ / কোটি)" else "South Asian (Lakh / Crore)"
                NumberGroupingStyle.STANDARD_3 -> if (isBangla) "আন্তর্জাতিক (হাজার / মিলিয়ন)" else "Western Standard (Thousand / Million)"
                NumberGroupingStyle.MYRIAD_4 -> if (isBangla) "৪ অঙ্ক গ্রুপিং (Myriad / Wan)" else "4-Digit Grouping (Myriad / Wan)"
                NumberGroupingStyle.NONE -> if (isBangla) "কোনো গ্রুপিং নেই" else "No Grouping"
            }

            OutlinedCard(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showGroupingStyleSheet = true }
                    .testTag("grouping_style_row")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Numbers,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isBangla) "সংখ্যা বিন্যাস পদ্ধতি" else "Number Grouping Style",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = currentGroupingTitle,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Select",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Custom Separator Card
            OutlinedCard(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showCustomEditor = !showCustomEditor }
                    .testTag("custom_separator_card")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isBangla) "কাস্টম সেপারেটর কনফিগার" else "Custom Separators Builder",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (amountFormatConfig.preset == AmountSeparatorPreset.CUSTOM) {
                                    if (isBangla) "সক্রিয় (গ্রুপিং: '${amountFormatConfig.customGroupingSeparator}', দশমিক: '${amountFormatConfig.customDecimalSeparator}')"
                                    else "Active (Grouping: '${amountFormatConfig.customGroupingSeparator}', Decimal: '${amountFormatConfig.customDecimalSeparator}')"
                                } else {
                                    if (isBangla) "নিজের পছন্দমতো কমা ও ডট নির্ধারণ করুন" else "Customize thousand & decimal symbols"
                                },
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.outline,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Icon(
                            imageVector = if (showCustomEditor) Icons.Default.Close else Icons.Default.Edit,
                            contentDescription = "Toggle Custom Editor",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    AnimatedVisibility(visible = showCustomEditor) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = customGroupingInput,
                                    onValueChange = { customGroupingInput = it },
                                    label = { Text(if (isBangla) "হাজার সেপারেটর" else "Thousand Sep", fontSize = 11.sp) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                    ),
                                    modifier = Modifier.weight(1f)
                                )

                                OutlinedTextField(
                                    value = customDecimalInput,
                                    onValueChange = { customDecimalInput = it },
                                    label = { Text(if (isBangla) "দশমিক প্রতীক" else "Decimal Sep", fontSize = 11.sp) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // Style selection for custom
                            Text(
                                text = if (isBangla) "গ্রুপিং পদ্ধতি:" else "Grouping Pattern:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = customStyleSelection == NumberGroupingStyle.SOUTH_ASIAN,
                                    onClick = { customStyleSelection = NumberGroupingStyle.SOUTH_ASIAN },
                                    label = { Text(if (isBangla) "লক্ষ / কোটি" else "Lakh / Crore", fontSize = 12.sp) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                                FilterChip(
                                    selected = customStyleSelection == NumberGroupingStyle.STANDARD_3,
                                    onClick = { customStyleSelection = NumberGroupingStyle.STANDARD_3 },
                                    label = { Text(if (isBangla) "হাজার / মিলিয়ন" else "Thousand / Million", fontSize = 12.sp) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Button(
                                onClick = {
                                    viewModel.setCustomAmountFormat(
                                        customGroupingInput,
                                        customDecimalInput.ifBlank { "." },
                                        customStyleSelection
                                    )
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isBangla) "কাস্টম ফরম্যাট প্রয়োগ করুন" else "Apply Custom Format",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            // 3. Reset Button
            OutlinedButton(
                onClick = {
                    viewModel.resetAmountFormatToDefaults()
                },
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.RestartAlt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isBangla) "ডিফল্ট ফরম্যাটে রিসেট করুন" else "Reset to Defaults",
                    fontSize = 12.5.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Preset Selection BottomSheet
        if (showPresetPickerSheet) {
            ModalBottomSheet(
                onDismissRequest = { showPresetPickerSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isBangla) "কমা ও সেপারেটর প্রিসেট নির্বাচন" else "Select Amount Separator Preset",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isBangla) "আপনার পছন্দসই প্রিসেট সিলেক্ট করুন" else "Choose predefined formatting style",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        IconButton(onClick = { showPresetPickerSheet = false }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    val presets = AmountSeparatorPreset.entries.filter { it != AmountSeparatorPreset.CUSTOM }

                    presets.forEach { preset ->
                        val isSelected = amountFormatConfig.preset == preset
                        val title = if (isBangla) preset.titleBn else preset.titleEn
                        val example = if (isBangla) preset.exampleBn else preset.exampleEn

                        OutlinedCard(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(
                                if (isSelected) 1.5.dp else 1.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setAmountFormatPreset(preset)
                                    showPresetPickerSheet = false
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = if (isSelected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                                        contentDescription = null,
                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(20.dp)
                                    )

                                    Column {
                                        Text(
                                            text = title,
                                            fontSize = 14.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${if (isBangla) "উদাহরণ: " else "Example: "} $example",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // Grouping Style BottomSheet
        if (showGroupingStyleSheet) {
            ModalBottomSheet(
                onDismissRequest = { showGroupingStyleSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isBangla) "সংখ্যা বিন্যাস পদ্ধতি নির্বাচন" else "Select Number Grouping Style",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isBangla) "লক্ষ/কোটি বা মিলিয়ন/বিলিয়ন পদ্ধতি বেছে নিন" else "Select thousand grouping rule",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        IconButton(onClick = { showGroupingStyleSheet = false }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    val styles = listOf(
                        Triple(
                            NumberGroupingStyle.SOUTH_ASIAN,
                            if (isBangla) "দক্ষিণ এশীয় (Lakh / Crore)" else "South Asian (Lakh / Crore)",
                            if (isBangla) "প্রথমে ৩ অঙ্ক, পরে প্রতি ২ অঙ্কে কমা (যেমন: ১২,৩৪,৫৬৭.৮৯)" else "First 3 digits, then every 2 digits (e.g. 12,34,567.89)"
                        ),
                        Triple(
                            NumberGroupingStyle.STANDARD_3,
                            if (isBangla) "আন্তর্জাতিক (Thousand / Million)" else "Western (Thousand / Million)",
                            if (isBangla) "প্রতি ৩ অঙ্কে নিয়মিত কমা (যেমন: 1,234,567.89)" else "Every 3 digits (e.g. 1,234,567.89)"
                        ),
                        Triple(
                            NumberGroupingStyle.MYRIAD_4,
                            if (isBangla) "৪ অঙ্ক গ্রুপিং (Myriad / Wan)" else "4-Digit Grouping (Myriad / Wan)",
                            if (isBangla) "প্রতি ৪ অঙ্কে নিয়মিত কমা (যেমন: 123,4567.89)" else "Every 4 digits (e.g. 123,4567.89)"
                        ),
                        Triple(
                            NumberGroupingStyle.NONE,
                            if (isBangla) "কোনো গ্রুপিং নেই" else "No Grouping",
                            if (isBangla) "কোনো কমা ছাড়াই শুধু সংখ্যা (যেমন: 1234567.89)" else "Plain number without comma (e.g. 1234567.89)"
                        )
                    )

                    styles.forEach { (style, title, desc) ->
                        val isSelected = amountFormatConfig.effectiveGroupingStyle == style

                        OutlinedCard(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(
                                if (isSelected) 1.5.dp else 1.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setCustomAmountFormat(
                                        amountFormatConfig.effectiveGroupingSeparator,
                                        amountFormatConfig.effectiveDecimalSeparator,
                                        style
                                    )
                                    showGroupingStyleSheet = false
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = if (isSelected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                                        contentDescription = null,
                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(20.dp)
                                    )

                                    Column {
                                        Text(
                                            text = title,
                                            fontSize = 14.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = desc,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
