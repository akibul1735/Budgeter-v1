package com.example.ui.screens.dashboard

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Account
import com.example.data.model.Category
import com.example.data.model.LanguageMode
import com.example.data.model.TransactionWithDetails
import com.example.util.CashFlowHelper
import com.example.util.CashFlowPeriodPreset
import com.example.util.LanguageHelper
import java.util.Locale

@Composable
fun DashboardCashFlowSummaryCard(
    transactions: List<TransactionWithDetails>,
    allAccounts: List<Account>,
    allCategories: List<Category>,
    languageMode: LanguageMode,
    onNavigateToCashFlow: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPreset by remember { mutableStateOf(CashFlowPeriodPreset.THIS_MONTH) }
    var showPresetDropdown by remember { mutableStateOf(false) }

    val dateRange = remember(selectedPreset) {
        CashFlowHelper.getDateRangeForPreset(selectedPreset, 0L, 0L)
    }

    val summary = remember(transactions, allAccounts, allCategories, dateRange, languageMode) {
        CashFlowHelper.calculateCashFlow(
            allTransactions = transactions,
            allAccounts = allAccounts,
            allCategories = allCategories,
            startMs = dateRange.first,
            endMs = dateRange.second,
            languageMode = languageMode
        )
    }

    val positiveGreen = Color(0xFF2E7D32)
    val negativeRed = Color(0xFFD32F2F)
    val isPositive = summary.netCashFlow >= 0
    val netColor = if (isPositive) positiveGreen else negativeRed

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("dashboard_cash_flow_card")
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Header: Icon + Title + Period Selector + Jump Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .clickable { onNavigateToCashFlow() }
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "নগদ প্রবাহ (Cash Flow)" else "Cash Flow Summary",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "আসল নগদ তহবিলের বিবরণী" else "Liquid Funds Inflow & Outflow",
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // Period Selector Pill
                    Box {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.clickable { showPresetDropdown = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = selectedPreset.getTitle(languageMode),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showPresetDropdown,
                            onDismissRequest = { showPresetDropdown = false }
                        ) {
                            listOf(
                                CashFlowPeriodPreset.THIS_MONTH,
                                CashFlowPeriodPreset.LAST_MONTH,
                                CashFlowPeriodPreset.LAST_3_MONTHS,
                                CashFlowPeriodPreset.THIS_YEAR,
                                CashFlowPeriodPreset.ALL_TIME
                            ).forEach { preset ->
                                DropdownMenuItem(
                                    text = { Text(preset.getTitle(languageMode), fontSize = 12.sp) },
                                    onClick = {
                                        selectedPreset = preset
                                        showPresetDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    // Navigate to Cash Flow Full Screen
                    IconButton(
                        onClick = onNavigateToCashFlow,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "View Cash Flow",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // 2. Net Cash Flow & Surplus/Deficit Banner
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = netColor.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, netColor.copy(alpha = 0.25f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToCashFlow() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "নিট নগদ প্রবাহ" else "Net Cash Flow",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = (if (isPositive) "+" else "") + LanguageHelper.formatCurrency(summary.netCashFlow, languageMode),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = netColor
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = netColor.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = if (isPositive) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                                contentDescription = null,
                                tint = netColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (isPositive) {
                                    if (languageMode == LanguageMode.BANGLA) "উদ্বৃত্ত" else "Surplus"
                                } else {
                                    if (languageMode == LanguageMode.BANGLA) "ঘাটতি" else "Deficit"
                                },
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = netColor
                            )
                        }
                    }
                }
            }

            // 3. 3-Way Metrics Breakdown (Inflow | Outflow | Burn Rate)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Inflow
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "আগমন (+)" else "Inflow (+)",
                        fontSize = 10.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "+" + LanguageHelper.formatCurrency(summary.totalInflow, languageMode),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = positiveGreen,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Outflow
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "নির্গমন (−)" else "Outflow (−)",
                        fontSize = 10.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "−" + LanguageHelper.formatCurrency(summary.totalOutflow, languageMode),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = negativeRed,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Daily Burn Rate / Runway
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "দৈনিক ব্যয়" else "Burn Rate",
                        fontSize = 10.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = LanguageHelper.formatCurrency(summary.dailyAverageBurnRate, languageMode) + (if (languageMode == LanguageMode.BANGLA) "/দিন" else "/d"),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

            // 4. Inflow vs Outflow Comparison Chart (Periodic Bars)
            if (summary.periodicBars.isNotEmpty() && summary.periodicBars.any { it.inflow > 0 || it.outflow > 0 }) {
                val maxVal = remember(summary.periodicBars) {
                    val m = summary.periodicBars.maxOfOrNull { Math.max(it.inflow, it.outflow) } ?: 100.0
                    if (m <= 0) 100.0 else m
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToCashFlow() }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "প্রবাহ ট্রেন্ড চার্ট" else "Flow Trend Comparison",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Mini Legend
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(positiveGreen))
                                Text(if (languageMode == LanguageMode.BANGLA) "আয়" else "In", fontSize = 9.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(negativeRed))
                                Text(if (languageMode == LanguageMode.BANGLA) "ব্যয়" else "Out", fontSize = 9.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .padding(vertical = 2.dp)
                    ) {
                        val barCount = summary.periodicBars.size
                        val barWidth = (size.width / (barCount * 2.8f)).coerceAtMost(16.dp.toPx())
                        val groupSpacing = size.width / barCount
                        val chartHeight = size.height - 14.dp.toPx()

                        // Base horizontal line
                        drawLine(
                            color = Color.LightGray.copy(alpha = 0.4f),
                            start = Offset(0f, chartHeight),
                            end = Offset(size.width, chartHeight),
                            strokeWidth = 1.dp.toPx()
                        )

                        summary.periodicBars.forEachIndexed { idx, bar ->
                            val groupCenter = idx * groupSpacing + groupSpacing / 2f

                            // Inflow Bar
                            val inH = ((bar.inflow / maxVal) * chartHeight).toFloat().coerceIn(0f, chartHeight)
                            if (inH > 0f) {
                                drawRoundRect(
                                    color = positiveGreen,
                                    topLeft = Offset(groupCenter - barWidth - 1.5.dp.toPx(), chartHeight - inH),
                                    size = Size(barWidth, inH),
                                    cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                                )
                            }

                            // Outflow Bar
                            val outH = ((bar.outflow / maxVal) * chartHeight).toFloat().coerceIn(0f, chartHeight)
                            if (outH > 0f) {
                                drawRoundRect(
                                    color = negativeRed,
                                    topLeft = Offset(groupCenter + 1.5.dp.toPx(), chartHeight - outH),
                                    size = Size(barWidth, outH),
                                    cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                                )
                            }
                        }
                    }

                    // Bar Labels
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        summary.periodicBars.forEach { bar ->
                            Text(
                                text = bar.label,
                                fontSize = 9.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                }
            } else {
                // Compact Runway & Closing Balance Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToCashFlow() },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "সমাপনী নগদ ব্যালেন্স: " else "Closing Cash: ",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = LanguageHelper.formatCurrency(summary.closingBalance, languageMode),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    val runwayLabel = if (summary.runwayDays >= 999) {
                        if (languageMode == LanguageMode.BANGLA) "রানওয়ে >৩ বছর" else "Runway >3 yrs"
                    } else {
                        if (languageMode == LanguageMode.BANGLA) "রানওয়ে: ${LanguageHelper.toBanglaDigits(summary.runwayDays.toString())} দিন" else "Runway: ${summary.runwayDays}d"
                    }
                    Text(
                        text = runwayLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (summary.runwayDays < 30) negativeRed else positiveGreen
                    )
                }
            }
        }
    }
}
