package com.example.ui.screens.settings

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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.example.ui.theme.SolidPrimary
import com.example.ui.viewmodel.BudgetViewModel
import com.example.util.DateFormatOption
import com.example.util.DateUtils
import com.example.util.DisplayFormatConfig
import com.example.util.LanguageHelper

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DateSettingsPage(
    viewModel: BudgetViewModel,
    languageMode: LanguageMode,
    onBack: () -> Unit
) {
    val displayFormatConfig by viewModel.displayFormatConfig.collectAsStateWithLifecycle()
    val isBangla = languageMode == LanguageMode.BANGLA

    var customPatternInput by remember(displayFormatConfig) { mutableStateOf(displayFormatConfig.customDateFormat) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("date_settings_page")
    ) {
        AppTabHeader(
            title = if (isBangla) "তারিখ ও ক্যালেন্ডার" else "Date Settings",
            tabIcon = Icons.Default.CalendarToday,
            onBack = onBack,
            autoHideOnScroll = false
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(2.dp))

            // Compact Live Preview Card
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isBangla) "লাইভ প্রিভিউ (আজকের তারিখ)" else "Live Date Preview (Today)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = DateUtils.formatDate(System.currentTimeMillis(), languageMode),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Text(
                            text = displayFormatConfig.dateFormatPattern,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Compact Date Format Suggestions (Horizontal Left-Right Swipe Carousel)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isBangla) "তারিখ ফরম্যাট পছন্দ করুন" else "Select Date Format",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (isBangla) "বামে-ডানে সোয়াইপ করুন ⇄" else "Swipe left-right ⇄",
                    fontSize = 10.5.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DateFormatOption.entries.forEach { option ->
                    val isSelected = displayFormatConfig.dateFormatPattern == option.pattern
                    val title = if (isBangla) option.titleBn else option.titleEn
                    val formattedSample = if (option == DateFormatOption.CUSTOM) {
                        DateUtils.formatDate(System.currentTimeMillis(), languageMode)
                    } else {
                        try {
                            val sdf = java.text.SimpleDateFormat(option.pattern, if (isBangla) java.util.Locale("bn", "BD") else java.util.Locale.US)
                            val out = sdf.format(java.util.Date())
                            if (isBangla) LanguageHelper.toBanglaDigits(out) else out
                        } catch (_: Exception) {
                            option.example
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        border = androidx.compose.foundation.BorderStroke(
                            if (isSelected) 1.5.dp else 1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier
                            .width(180.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { viewModel.setDateFormatPattern(option.pattern) }
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = title,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                                if (isSelected) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.primary
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Active",
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier
                                                .padding(2.dp)
                                                .size(12.dp)
                                        )
                                    }
                                }
                            }

                            // Large formatted preview
                            Text(
                                text = formattedSample,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )

                            // Pattern pill
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Text(
                                    text = option.pattern,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Custom Pattern Editor (if custom is chosen)
            if (displayFormatConfig.dateFormatPattern == DateFormatOption.CUSTOM.pattern) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (isBangla) "কাস্টম ফরম্যাট প্যাটার্ন" else "Custom Format Pattern",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        OutlinedTextField(
                            value = customPatternInput,
                            onValueChange = {
                                customPatternInput = it
                                viewModel.setCustomDateFormat(it)
                            },
                            label = { Text("Pattern (e.g. dddd, d MMMM yyyy)", fontSize = 10.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "টোকেন: d (দিন), dd (০৭), ddd (রবি), dddd (রবিবার), MM (০৯), MMM (সেপ্টে), MMMM (সেপ্টেম্বর), yyyy (২০২৬)",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.outline,
                            lineHeight = 13.sp
                        )
                    }
                }
            }

            // Compact First Day of Week
            Text(
                text = if (isBangla) "সপ্তাহের প্রথম দিন" else "First Day of the Week",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val days = listOf(
                    Triple(java.util.Calendar.SUNDAY, "Sunday", "রবিবার"),
                    Triple(java.util.Calendar.MONDAY, "Monday", "সোমবার"),
                    Triple(java.util.Calendar.SATURDAY, "Saturday", "শনিবার")
                )
                days.forEach { (dayInt, nameEn, nameBn) ->
                    val isSelected = displayFormatConfig.firstDayOfWeek == dayInt
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setFirstDayOfWeek(dayInt) },
                        label = {
                            Text(
                                text = if (isBangla) nameBn else nameEn,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Timezone Settings Card
            var showTimezoneDialog by remember { mutableStateOf(false) }

            Text(
                text = if (isBangla) "টাইমজোন (সময় অঞ্চল)" else "Timezone",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            val currentTzId = displayFormatConfig.timeZoneId
            val currentTzName = when (currentTzId) {
                "Asia/Dhaka" -> if (isBangla) "বাংলাদেশ / ঢাকা (GMT+6) [ডিফল্ট]" else "Bangladesh / Dhaka (GMT+6) [Default]"
                "Asia/Kolkata" -> if (isBangla) "ভারত / কলকাতা (GMT+5:30)" else "India / Kolkata (GMT+5:30)"
                "UTC" -> "UTC (GMT+0)"
                "Asia/Dubai" -> if (isBangla) "দুবাই / সংযুক্ত আরব আমিরাত (GMT+4)" else "Dubai / UAE (GMT+4)"
                "Asia/Singapore" -> if (isBangla) "সিঙ্গাপুর (GMT+8)" else "Singapore (GMT+8)"
                "Asia/Tokyo" -> if (isBangla) "টোকিও / জাপান (GMT+9)" else "Tokyo / Japan (GMT+9)"
                "Europe/London" -> if (isBangla) "লন্ডন / যুক্তরাজ্য (GMT+0/+1)" else "London / UK (GMT+0/+1)"
                "America/New_York" -> if (isBangla) "নিউ ইয়র্ক / যুক্তরাষ্ট্র (GMT-5)" else "New York / US (GMT-5)"
                "SYSTEM" -> if (isBangla) "সিস্টেম ডিফল্ট ডিভাইস টাইমজোন" else "System Default Timezone"
                else -> currentTzId
            }

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showTimezoneDialog = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentTzName,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isBangla) "বর্তমান সময়: ${DateUtils.formatTime(System.currentTimeMillis(), languageMode)}" else "Current Time: ${DateUtils.formatTime(System.currentTimeMillis(), languageMode)}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            text = if (isBangla) "পরিবর্তন" else "Change",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            if (showTimezoneDialog) {
                var searchQuery by remember { mutableStateOf("") }
                val commonTimezones = listOf(
                    "Asia/Dhaka" to if (isBangla) "বাংলাদেশ / ঢাকা (GMT+6) [ডিফল্ট]" else "Bangladesh / Dhaka (GMT+6) [Default]",
                    "Asia/Kolkata" to if (isBangla) "ভারত / কলকাতা (GMT+5:30)" else "India / Kolkata (GMT+5:30)",
                    "UTC" to "UTC (GMT+0)",
                    "Asia/Dubai" to if (isBangla) "দুবাই / সংযুক্ত আরব আমিরাত (GMT+4)" else "Dubai / UAE (GMT+4)",
                    "Asia/Singapore" to if (isBangla) "সিঙ্গাপুর (GMT+8)" else "Singapore (GMT+8)",
                    "Asia/Tokyo" to if (isBangla) "টোকিও / জাপান (GMT+9)" else "Tokyo / Japan (GMT+9)",
                    "Europe/London" to if (isBangla) "লন্ডন / যুক্তরাজ্য (GMT+0/+1)" else "London / UK (GMT+0/+1)",
                    "America/New_York" to if (isBangla) "নিউ ইয়র্ক / যুক্তরাষ্ট্র (GMT-5)" else "New York / US (GMT-5)",
                    "SYSTEM" to (if (isBangla) "সিস্টেম ডিফল্ট ডিভাইস টাইমজোন" else "System Device Timezone")
                )

                val allAvailableIds = remember {
                    java.util.TimeZone.getAvailableIDs().sorted()
                }

                val filteredList = remember(searchQuery) {
                    if (searchQuery.isBlank()) {
                        commonTimezones
                    } else {
                        val fromCommon = commonTimezones.filter {
                            it.first.contains(searchQuery, ignoreCase = true) || it.second.contains(searchQuery, ignoreCase = true)
                        }
                        val otherIds = allAvailableIds.filter {
                            it.contains(searchQuery, ignoreCase = true) && commonTimezones.none { c -> c.first == it }
                        }.map { it to it }
                        (fromCommon + otherIds).take(40)
                    }
                }

                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { showTimezoneDialog = false },
                    title = {
                        Text(
                            text = if (isBangla) "টাইমজোন নির্বাচন করুন" else "Select Timezone",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    },
                    text = {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                label = { Text(if (isBangla) "টাইমজোন খুঁজুন..." else "Search timezone...", fontSize = 12.sp) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            androidx.compose.foundation.lazy.LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(260.dp)
                            ) {
                                items(filteredList.size) { index ->
                                    val (id, label) = filteredList[index]
                                    val isSelected = currentTzId == id
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                viewModel.setTimeZoneId(id)
                                                showTimezoneDialog = false
                                            }
                                            .padding(vertical = 10.dp, horizontal = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) SolidPrimary else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = SolidPrimary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {},
                    dismissButton = {
                        androidx.compose.material3.TextButton(onClick = { showTimezoneDialog = false }) {
                            Text(if (isBangla) "বন্ধ করুন" else "Close")
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
