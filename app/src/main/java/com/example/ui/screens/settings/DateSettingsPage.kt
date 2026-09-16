package com.example.ui.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.util.DateFormatOption
import com.example.util.DateUtils
import com.example.util.LanguageHelper
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DateSettingsPage(
    viewModel: BudgetViewModel,
    languageMode: LanguageMode,
    onBack: () -> Unit
) {
    val displayFormatConfig by viewModel.displayFormatConfig.collectAsStateWithLifecycle()
    val isBangla = languageMode == LanguageMode.BANGLA

    var showDateFormatSheet by remember { mutableStateOf(false) }
    var showFirstDaySheet by remember { mutableStateOf(false) }
    var showCustomPatternCard by remember {
        mutableStateOf(displayFormatConfig.dateFormatPattern == DateFormatOption.CUSTOM.pattern)
    }
    var customPatternInput by remember(displayFormatConfig) {
        mutableStateOf(displayFormatConfig.customDateFormat)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("date_settings_page")
    ) {
        AppTabHeader(
            title = if (isBangla) "তারিখ ও সময়" else "Date & Time Setup",
            tabIcon = Icons.Default.CalendarToday,
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isBangla) "লাইভ তারিখ ও সময় প্রিভিউ" else "Live Date & Time Preview",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = DateUtils.formatDate(System.currentTimeMillis(), languageMode),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isBangla) "প্যাটার্ন: ${displayFormatConfig.dateFormatPattern}" else "Pattern: ${displayFormatConfig.dateFormatPattern}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = when (displayFormatConfig.firstDayOfWeek) {
                                    Calendar.SATURDAY -> if (isBangla) "শনিবার" else "Sat"
                                    Calendar.SUNDAY -> if (isBangla) "রবিবার" else "Sun"
                                    Calendar.MONDAY -> if (isBangla) "সোমবার" else "Mon"
                                    Calendar.TUESDAY -> if (isBangla) "মঙ্গলবার" else "Tue"
                                    Calendar.WEDNESDAY -> if (isBangla) "বুধবার" else "Wed"
                                    Calendar.THURSDAY -> if (isBangla) "বৃহস্পতিবার" else "Thu"
                                    Calendar.FRIDAY -> if (isBangla) "শুক্রবার" else "Fri"
                                    else -> if (isBangla) "শনিবার" else "Sat"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // 2. Section: Date Configuration
            Text(
                text = if (isBangla) "তারিখ ও সময় কনফিগারেশন" else "Date & Time Configuration",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Date Format Option Row (Opens Modal Bottom Sheet)
            val selectedOption = DateFormatOption.entries.find { it.pattern == displayFormatConfig.dateFormatPattern }
            val currentFormatTitle = if (selectedOption != null) {
                if (isBangla) selectedOption.titleBn else selectedOption.titleEn
            } else {
                if (isBangla) "কাস্টম ফরম্যাট" else "Custom Format"
            }

            OutlinedCard(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDateFormatSheet = true }
                    .testTag("date_format_row")
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
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isBangla) "তারিখ ফরম্যাট" else "Date Format",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$currentFormatTitle • ${displayFormatConfig.dateFormatPattern}",
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

            // First Day of the Week Row (Opens Modal Bottom Sheet)
            val firstDayTitle = when (displayFormatConfig.firstDayOfWeek) {
                Calendar.SATURDAY -> if (isBangla) "শনিবার (বাংলাদেশ/মধ্যপ্রাচ্য)" else "Saturday (Bangladesh/Middle East)"
                Calendar.SUNDAY -> if (isBangla) "রবিবার (আমেরিকান স্ট্যান্ডার্ড)" else "Sunday (Americas Standard)"
                Calendar.MONDAY -> if (isBangla) "সোমবার (আন্তর্জাতিক/আইএসও)" else "Monday (ISO/European Standard)"
                Calendar.TUESDAY -> if (isBangla) "মঙ্গলবার" else "Tuesday"
                Calendar.WEDNESDAY -> if (isBangla) "বুধবার" else "Wednesday"
                Calendar.THURSDAY -> if (isBangla) "বৃহস্পতিবার" else "Thursday"
                Calendar.FRIDAY -> if (isBangla) "শুক্রবার" else "Friday"
                else -> if (isBangla) "শনিবার" else "Saturday"
            }

            OutlinedCard(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showFirstDaySheet = true }
                    .testTag("first_day_of_week_row")
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
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isBangla) "সপ্তাহের প্রথম দিন" else "First Day of the Week",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = firstDayTitle,
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

            // 12-Hour vs 24-Hour Switch Card
            OutlinedCard(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isBangla) "১২-ঘণ্টা সময় ফরম্যাট" else "12-Hour Time Format",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (displayFormatConfig.isTimeFormat12Hour) {
                                if (isBangla) "সক্রিয় (০২:৩০ PM)" else "Active (02:30 PM)"
                            } else {
                                if (isBangla) "২৪-ঘণ্টা সক্রিয় (১৪:৩০)" else "24-Hour Active (14:30)"
                            },
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    Switch(
                        checked = displayFormatConfig.isTimeFormat12Hour,
                        onCheckedChange = { viewModel.setTimeFormat12Hour(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }

            // Custom Pattern Editor Card
            OutlinedCard(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showCustomPatternCard = !showCustomPatternCard }
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.EditCalendar,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isBangla) "কাস্টম তারিখ প্যাটার্ন" else "Custom Date Pattern",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (displayFormatConfig.dateFormatPattern == DateFormatOption.CUSTOM.pattern) {
                                    if (isBangla) "সক্রিয় (${displayFormatConfig.customDateFormat})" else "Active (${displayFormatConfig.customDateFormat})"
                                } else {
                                    if (isBangla) "প্যাটার্ন তৈরি বা এডিট করুন" else "Build custom date format tokens"
                                },
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.outline,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Icon(
                            imageVector = if (showCustomPatternCard) Icons.Default.Close else Icons.Default.Tune,
                            contentDescription = "Toggle Custom Editor",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    AnimatedVisibility(visible = showCustomPatternCard) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                            OutlinedTextField(
                                value = customPatternInput,
                                onValueChange = { customPatternInput = it },
                                label = { Text(if (isBangla) "প্যাটার্ন (যেমন: dd MMM, Y: DDDD)" else "Pattern (e.g. dd MMM, Y: DDDD)", fontSize = 11.sp) },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Real-time Live Preview Card
                            val customLivePreview = remember(customPatternInput, languageMode) {
                                if (customPatternInput.isNotBlank()) {
                                    try {
                                        DateUtils.formatDate(System.currentTimeMillis(), languageMode, customPatternInput)
                                    } catch (e: Exception) {
                                        "..."
                                    }
                                } else {
                                    DateUtils.formatDate(System.currentTimeMillis(), languageMode)
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (isBangla) "লাইভ আউটপুট:" else "Live Preview:",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = customLivePreview,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }

                            // Quick Token Helper Chips
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = if (isBangla) "টোকেন যোগ করুন:" else "Quick Insert Tokens:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    val tokens = listOf(
                                        "d" to "d",
                                        "DD" to "dd",
                                        "DDD" to "ddd",
                                        "DDDD" to "dddd",
                                        "MM" to "MM",
                                        "MMM" to "MMM",
                                        "MMMM" to "MMMM",
                                        "YYYY" to "yyyy",
                                        "dd MMM, Y: DDDD" to "dd MMM, Y: DDDD"
                                    )
                                    tokens.forEach { (label, tokenValue) ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                                            modifier = Modifier.clickable {
                                                customPatternInput = if (tokenValue == "dd MMM, Y: DDDD") {
                                                    tokenValue
                                                } else if (customPatternInput.isBlank()) {
                                                    tokenValue
                                                } else {
                                                    "$customPatternInput $tokenValue"
                                                }
                                            }
                                        ) {
                                            Text(
                                                text = "+ $label",
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Text(
                                text = if (isBangla)
                                    "টোকেন গাইড: d (দিন), dd (০৭), ddd/DDD (রবি), dddd/DDDD (রবিবার), MM (০৯), MMM (সেপ্টে), MMMM (সেপ্টেম্বর), yyyy/Y (২০২৬)"
                                else
                                    "Tokens: d (day), dd (07), ddd/DDD (Sun), dddd/DDDD (Sunday), MM (09), MMM (Sep), MMMM (September), yyyy/Y (2026)",
                                fontSize = 10.5.sp,
                                color = MaterialTheme.colorScheme.outline,
                                lineHeight = 14.sp
                            )

                            Button(
                                onClick = {
                                    viewModel.setCustomDateFormat(customPatternInput)
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isBangla) "কাস্টম প্যাটার্ন প্রয়োগ করুন" else "Apply Custom Pattern",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            // Reset Button
            OutlinedButton(
                onClick = {
                    viewModel.setDateFormatPattern("dd MMMM yyyy")
                    viewModel.setFirstDayOfWeek(Calendar.SATURDAY)
                    viewModel.setTimeFormat12Hour(true)
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
                    text = if (isBangla) "ডিফল্ট তারিখ সেটিংসে রিসেট করুন" else "Reset to Default Date Settings",
                    fontSize = 12.5.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Modal Bottom Sheet: Date Format Picker
    if (showDateFormatSheet) {
        ModalBottomSheet(
            onDismissRequest = { showDateFormatSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .padding(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isBangla) "তারিখ ফরম্যাট নির্বাচন" else "Select Date Format",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isBangla) "অ্যাপে ব্যবহারের জন্য তারিখ শৈলী বেছে নিন" else "Choose date display style across the app",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    IconButton(onClick = { showDateFormatSheet = false }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                DateFormatOption.entries.forEach { option ->
                    val isSelected = displayFormatConfig.dateFormatPattern == option.pattern
                    val title = if (isBangla) option.titleBn else option.titleEn
                    val sampleDate = if (option == DateFormatOption.CUSTOM) {
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
                                viewModel.setDateFormatPattern(option.pattern)
                                showDateFormatSheet = false
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
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
                                        text = sampleDate,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ) {
                                Text(
                                    text = option.pattern,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet: First Day of the Week Picker
    if (showFirstDaySheet) {
        ModalBottomSheet(
            onDismissRequest = { showFirstDaySheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .padding(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isBangla) "সপ্তাহের প্রথম দিন নির্ধারণ" else "First Day of the Week",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isBangla) "ক্যালেন্ডার ও সাপ্তাহিক হিসাবের শুরুর দিন" else "Determines start day for calendar views and weekly reports",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    IconButton(onClick = { showFirstDaySheet = false }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                val days = listOf(
                    Triple(Calendar.SATURDAY, if (isBangla) "শনিবার (Saturday)" else "Saturday", if (isBangla) "বাংলাদেশ ও মধ্যপ্রাচ্য স্ট্যান্ডার্ড" else "Standard for Bangladesh & Middle East"),
                    Triple(Calendar.SUNDAY, if (isBangla) "রবিবার (Sunday)" else "Sunday", if (isBangla) "যুক্তরাষ্ট্র, কানাডা ও জাপান স্ট্যান্ডার্ড" else "Standard for US, Canada & Japan"),
                    Triple(Calendar.MONDAY, if (isBangla) "সোমবার (Monday)" else "Monday", if (isBangla) "ইউরোপ ও আন্তর্জাতিক আইএসও" else "Standard for Europe & ISO standard"),
                    Triple(Calendar.TUESDAY, if (isBangla) "মঙ্গলবার (Tuesday)" else "Tuesday", if (isBangla) "মঙ্গলবার থেকে সপ্তাহ শুরু" else "Start week on Tuesday"),
                    Triple(Calendar.WEDNESDAY, if (isBangla) "বুধবার (Wednesday)" else "Wednesday", if (isBangla) "বুধবার থেকে সপ্তাহ শুরু" else "Start week on Wednesday"),
                    Triple(Calendar.THURSDAY, if (isBangla) "বৃহস্পতিবার (Thursday)" else "Thursday", if (isBangla) "বৃহস্পতিবার থেকে সপ্তাহ শুরু" else "Start week on Thursday"),
                    Triple(Calendar.FRIDAY, if (isBangla) "শুক্রবার (Friday)" else "Friday", if (isBangla) "শুক্রবার থেকে সপ্তাহ শুরু" else "Start week on Friday")
                )

                days.forEach { (dayInt, title, desc) ->
                    val isSelected = displayFormatConfig.firstDayOfWeek == dayInt

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
                                viewModel.setFirstDayOfWeek(dayInt)
                                showFirstDaySheet = false
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
            }
        }
    }
}
