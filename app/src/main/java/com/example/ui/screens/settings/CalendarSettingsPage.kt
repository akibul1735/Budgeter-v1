package com.example.ui.screens.settings

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.LanguageMode
import com.example.ui.components.AppTabHeader
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.ui.viewmodel.BudgetViewModel
import com.example.util.CalendarDisplayMode
import com.example.util.DashboardConfig

@Composable
fun CalendarSettingsPage(
    viewModel: BudgetViewModel,
    languageMode: LanguageMode,
    onBack: () -> Unit
) {
    val dashboardConfig by viewModel.dashboardConfig.collectAsStateWithLifecycle()
    val isBangla = languageMode == LanguageMode.BANGLA

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("calendar_settings_page")
    ) {
        AppTabHeader(
            title = if (isBangla) "ক্যালেন্ডার ও ডিসপ্লে" else "Calendar Settings",
            tabIcon = Icons.Default.DateRange,
            onBack = onBack,
            autoHideOnScroll = false
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Calendar Cell Preview Box
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isBangla) "ক্যালেন্ডার দিনের সেল প্রিভিউ" else "Calendar Day Cell Live Preview",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Mock Day Cell
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 2.dp,
                        modifier = Modifier.size(72.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (isBangla) "০৭" else "07",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            if (dashboardConfig.calendarDisplayMode == CalendarDisplayMode.DOTS) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (dashboardConfig.calendarShowIncome) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(SolidIncome, CircleShape)
                                        )
                                    }
                                    if (dashboardConfig.calendarShowExpense) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(SolidExpense, CircleShape)
                                        )
                                    }
                                }
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    if (dashboardConfig.calendarShowIncome) {
                                        Text("+৳ 5k", fontSize = 8.sp, color = SolidIncome, fontWeight = FontWeight.Bold)
                                    }
                                    if (dashboardConfig.calendarShowExpense) {
                                        Text("-৳ 2k", fontSize = 8.sp, color = SolidExpense, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isBangla) "মাসিক ড্যাশবোর্ড ও ক্যালেন্ডার ভিউতে এটি প্রতিফলিত হবে।" else "Applied directly to your Monthly Dashboard & Calendar Matrix.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }

            // Indicator Mode Selector
            Text(
                text = if (isBangla) "দিনের নির্দেশক মোড" else "Day Indicator Mode",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Dots Option
                val isDotsSelected = dashboardConfig.calendarDisplayMode == CalendarDisplayMode.DOTS
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDotsSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isDotsSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            viewModel.setCalendarSettings(
                                CalendarDisplayMode.DOTS,
                                dashboardConfig.calendarShowIncome,
                                dashboardConfig.calendarShowExpense
                            )
                        }
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.size(8.dp).background(SolidIncome, CircleShape))
                            Box(modifier = Modifier.size(8.dp).background(SolidExpense, CircleShape))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isBangla) "ডট ইনডিকেটর" else "Dots Indicator",
                            fontSize = 12.sp,
                            fontWeight = if (isDotsSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isDotsSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isBangla) "রঙিন বিন্দু" else "Colored dots",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                // Amounts Option
                val isAmountsSelected = dashboardConfig.calendarDisplayMode == CalendarDisplayMode.AMOUNTS
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isAmountsSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isAmountsSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            viewModel.setCalendarSettings(
                                CalendarDisplayMode.AMOUNTS,
                                dashboardConfig.calendarShowIncome,
                                dashboardConfig.calendarShowExpense
                            )
                        }
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Numbers,
                            contentDescription = null,
                            tint = if (isAmountsSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isBangla) "টাকার পরিমাণ ব্যাজ" else "Amount Badges",
                            fontSize = 12.sp,
                            fontWeight = if (isAmountsSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isAmountsSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isBangla) "সংখ্যার ব্যাজ" else "Numeric sums",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            // Toggles for Income and Expense
            Text(
                text = if (isBangla) "প্রদর্শিত তথ্য নির্বাচন" else "Visible Data Types",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Show Income
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(modifier = Modifier.size(10.dp).background(SolidIncome, CircleShape))
                            Text(
                                text = if (isBangla) "ক্যালেন্ডারে আয় দেখান" else "Show Income in Calendar",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Switch(
                            checked = dashboardConfig.calendarShowIncome,
                            onCheckedChange = {
                                viewModel.setCalendarSettings(
                                    dashboardConfig.calendarDisplayMode,
                                    it,
                                    dashboardConfig.calendarShowExpense
                                )
                            }
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    // Show Expense
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(modifier = Modifier.size(10.dp).background(SolidExpense, CircleShape))
                            Text(
                                text = if (isBangla) "ক্যালেন্ডারে খরচ দেখান" else "Show Expenses in Calendar",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Switch(
                            checked = dashboardConfig.calendarShowExpense,
                            onCheckedChange = {
                                viewModel.setCalendarSettings(
                                    dashboardConfig.calendarDisplayMode,
                                    dashboardConfig.calendarShowIncome,
                                    it
                                )
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
