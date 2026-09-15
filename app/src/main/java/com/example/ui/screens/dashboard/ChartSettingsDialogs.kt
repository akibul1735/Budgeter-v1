package com.example.ui.screens.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.DonutLarge
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.StackedBarChart
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.LanguageMode
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidPrimary
import com.example.util.BudgetChartShape
import com.example.util.BudgetSummaryType
import com.example.util.CalendarDisplayMode
import com.example.util.DailyChartType
import com.example.util.DailySummaryMode
import com.example.util.DailySummaryPeriod
import com.example.util.DecimalPrecision
import com.example.util.LanguageHelper

@Composable
fun DailySummarySettingsDialog(
    currentMode: DailySummaryMode,
    currentPeriod: DailySummaryPeriod,
    currentChartType: DailyChartType = DailyChartType.BAR,
    currentShowValues: Boolean,
    currentShowAverages: Boolean,
    currentDecimalPrecision: DecimalPrecision = DecimalPrecision.TWO_DIGITS,
    currentShowCurrency: Boolean = true,
    currentShowCurrencySymbol: Boolean = true,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onSave: (
        mode: DailySummaryMode,
        period: DailySummaryPeriod,
        chartType: DailyChartType,
        showValues: Boolean,
        showAverages: Boolean,
        decimalPrecision: DecimalPrecision,
        showCurrency: Boolean,
        showCurrencySymbol: Boolean
    ) -> Unit
) {
    var mode by remember { mutableStateOf(currentMode) }
    var period by remember { mutableStateOf(currentPeriod) }
    var chartType by remember { mutableStateOf(currentChartType) }
    var showValues by remember { mutableStateOf(currentShowValues) }
    var showAverages by remember { mutableStateOf(currentShowAverages) }
    var decimalPrecision by remember { mutableStateOf(currentDecimalPrecision) }
    var showCurrency by remember { mutableStateOf(currentShowCurrency) }
    var showCurrencySymbol by remember { mutableStateOf(currentShowCurrencySymbol) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = LanguageHelper.getString("daily_summary_settings", languageMode),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 1. Chart Type Selector (Bar, Line, Area, Stepped)
                SettingsSectionLabel(
                    title = if (languageMode == LanguageMode.BANGLA) "চার্টের ধরন" else "Chart Type"
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DailyChartType.values().forEach { ct ->
                        val isSelected = chartType == ct
                        val icon: ImageVector = when (ct) {
                            DailyChartType.BAR -> Icons.Default.BarChart
                            DailyChartType.LINE -> Icons.Default.ShowChart
                            DailyChartType.AREA -> Icons.AutoMirrored.Filled.TrendingUp
                            DailyChartType.STEPPED -> Icons.Default.StackedBarChart
                        }
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { chartType = ct },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            border = BorderStroke(
                                if (isSelected) 1.5.dp else 0.5.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = ct.getLabel(languageMode),
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 2. Display Mode (Expense, Income, Both)
                SettingsSectionLabel(
                    title = LanguageHelper.getString("display_mode", languageMode)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DailySummaryMode.values().forEach { m ->
                        val isSelected = mode == m
                        val accentColor = when (m) {
                            DailySummaryMode.EXPENSE -> SolidExpense
                            DailySummaryMode.INCOME -> SolidIncome
                            DailySummaryMode.BOTH -> MaterialTheme.colorScheme.primary
                        }
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { mode = m },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) accentColor.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            border = BorderStroke(
                                if (isSelected) 1.5.dp else 0.5.dp,
                                if (isSelected) accentColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            )
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = m.getLabel(languageMode),
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 3. Time Period (7 Days, 14 Days, 30 Days, This Month)
                SettingsSectionLabel(
                    title = LanguageHelper.getString("time_period", languageMode)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    val periods = DailySummaryPeriod.values()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (i in 0..1) {
                            val p = periods[i]
                            val isSelected = period == p
                            ModernChipItem(
                                label = p.getLabel(languageMode),
                                isSelected = isSelected,
                                modifier = Modifier.weight(1f),
                                onClick = { period = p }
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (i in 2..3) {
                            val p = periods[i]
                            val isSelected = period == p
                            ModernChipItem(
                                label = p.getLabel(languageMode),
                                isSelected = isSelected,
                                modifier = Modifier.weight(1f),
                                onClick = { period = p }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 4. Decimal Precision
                SettingsSectionLabel(
                    title = LanguageHelper.getString("decimal_precision", languageMode)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DecimalPrecision.values().forEach { dp ->
                        val isSelected = decimalPrecision == dp
                        ModernChipItem(
                            label = dp.getLabel(languageMode),
                            isSelected = isSelected,
                            modifier = Modifier.weight(1f),
                            onClick = { decimalPrecision = dp }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 5. Display Toggles Card
                SettingsSectionLabel(
                    title = if (languageMode == LanguageMode.BANGLA) "প্রদর্শন অপশন" else "Display Options"
                )
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                        ModernToggleRow(
                            label = LanguageHelper.getString("show_currency", languageMode),
                            checked = showCurrency,
                            onCheckedChange = { showCurrency = it }
                        )

                        if (showCurrency) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                            ModernToggleRow(
                                label = LanguageHelper.getString("show_currency_symbol", languageMode),
                                checked = showCurrencySymbol,
                                onCheckedChange = { showCurrencySymbol = it }
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                        ModernToggleRow(
                            label = LanguageHelper.getString("show_chart_values", languageMode),
                            checked = showValues,
                            onCheckedChange = { showValues = it }
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                        ModernToggleRow(
                            label = LanguageHelper.getString("show_averages", languageMode),
                            checked = showAverages,
                            onCheckedChange = { showAverages = it }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action Buttons Footer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = LanguageHelper.getString("cancel", languageMode),
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            onSave(
                                mode,
                                period,
                                chartType,
                                showValues,
                                showAverages,
                                decimalPrecision,
                                showCurrency,
                                showCurrencySymbol
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = LanguageHelper.getString("apply", languageMode),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BudgetSummarySettingsDialog(
    currentShape: BudgetChartShape,
    currentCategoryType: BudgetSummaryType,
    currentMaxCategories: Int,
    currentShowPercentages: Boolean,
    currentShowTodayPace: Boolean,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onSave: (shape: BudgetChartShape, categoryType: BudgetSummaryType, maxCategories: Int, showPercentages: Boolean, showTodayPace: Boolean) -> Unit
) {
    var shape by remember { mutableStateOf(currentShape) }
    var categoryType by remember { mutableStateOf(currentCategoryType) }
    var maxCategories by remember { mutableIntStateOf(currentMaxCategories) }
    var showPercentages by remember { mutableStateOf(currentShowPercentages) }
    var showTodayPace by remember { mutableStateOf(currentShowTodayPace) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = LanguageHelper.getString("budget_chart_settings", languageMode),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 1. Chart Shape / Visualization Style (2x3 Grid of tiles with icons)
                SettingsSectionLabel(
                    title = LanguageHelper.getString("chart_type", languageMode)
                )
                Spacer(modifier = Modifier.height(6.dp))

                val shapes = BudgetChartShape.values()
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (rowIdx in 0 until (shapes.size + 1) / 2) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val first = shapes[rowIdx * 2]
                            val second = if (rowIdx * 2 + 1 < shapes.size) shapes[rowIdx * 2 + 1] else null

                            ChartShapeTile(
                                shape = first,
                                isSelected = (shape == first),
                                languageMode = languageMode,
                                modifier = Modifier.weight(1f),
                                onClick = { shape = first }
                            )

                            if (second != null) {
                                ChartShapeTile(
                                    shape = second,
                                    isSelected = (shape == second),
                                    languageMode = languageMode,
                                    modifier = Modifier.weight(1f),
                                    onClick = { shape = second }
                                )
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 2. Category Filter (Expense, Income, All)
                SettingsSectionLabel(
                    title = LanguageHelper.getString("filter_by_type", languageMode)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    BudgetSummaryType.values().forEach { t ->
                        val isSelected = categoryType == t
                        val accentColor = when (t) {
                            BudgetSummaryType.EXPENSE -> SolidExpense
                            BudgetSummaryType.INCOME -> SolidIncome
                            BudgetSummaryType.ALL -> MaterialTheme.colorScheme.primary
                        }
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { categoryType = t },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) accentColor.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            border = BorderStroke(
                                if (isSelected) 1.5.dp else 0.5.dp,
                                if (isSelected) accentColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            )
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = t.getLabel(languageMode),
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 3. Max Categories Count
                SettingsSectionLabel(
                    title = LanguageHelper.getString("max_categories", languageMode)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(4, 6, 8, 10, 20).forEach { count ->
                        val isSelected = maxCategories == count
                        val label = if (count == 20) (if (languageMode == LanguageMode.BANGLA) "সব" else "All") else count.toString()
                        ModernChipItem(
                            label = label,
                            isSelected = isSelected,
                            modifier = Modifier.weight(1f),
                            onClick = { maxCategories = count }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 4. Display Toggles Card
                SettingsSectionLabel(
                    title = if (languageMode == LanguageMode.BANGLA) "প্রদর্শন অপশন" else "Display Options"
                )
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                        ModernToggleRow(
                            label = LanguageHelper.getString("show_slice_percentages", languageMode),
                            checked = showPercentages,
                            onCheckedChange = { showPercentages = it }
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                        ModernToggleRow(
                            label = LanguageHelper.getString("show_today_pace", languageMode),
                            checked = showTodayPace,
                            onCheckedChange = { showTodayPace = it }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action Buttons Footer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = LanguageHelper.getString("cancel", languageMode),
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = { onSave(shape, categoryType, maxCategories, showPercentages, showTodayPace) },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = LanguageHelper.getString("apply", languageMode),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarSettingsDialog(
    currentDisplayMode: CalendarDisplayMode,
    currentShowIncome: Boolean,
    currentShowExpense: Boolean,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onSave: (mode: CalendarDisplayMode, showIncome: Boolean, showExpense: Boolean) -> Unit
) {
    var displayMode by remember { mutableStateOf(currentDisplayMode) }
    var showIncome by remember { mutableStateOf(currentShowIncome) }
    var showExpense by remember { mutableStateOf(currentShowExpense) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = LanguageHelper.getString("calendar_settings", languageMode),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Display Mode (Amounts vs Dots)
                Text(
                    text = LanguageHelper.getString("display_style", languageMode),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                CalendarDisplayMode.values().forEach { dm ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { displayMode = dm }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (displayMode == dm),
                            onClick = { displayMode = dm },
                            colors = RadioButtonDefaults.colors(selectedColor = SolidPrimary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = dm.getLabel(languageMode),
                            fontSize = 13.5.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(10.dp))

                // Toggles: Show Income & Show Expense
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = LanguageHelper.getString("show_income_badges", languageMode),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Switch(
                        checked = showIncome,
                        onCheckedChange = { showIncome = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = SolidPrimary)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = LanguageHelper.getString("show_expense_badges", languageMode),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Switch(
                        checked = showExpense,
                        onCheckedChange = { showExpense = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = SolidPrimary)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(8.dp)) {
                        Text(LanguageHelper.getString("cancel", languageMode))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = { onSave(displayMode, showIncome, showExpense) },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SolidPrimary)
                    ) {
                        Text(LanguageHelper.getString("apply", languageMode))
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionLabel(title: String) {
    Text(
        text = title,
        fontSize = 12.5.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun ModernChipItem(
    label: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(
            if (isSelected) 1.5.dp else 0.5.dp,
            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )
    ) {
        Box(
            modifier = Modifier.padding(vertical = 7.dp, horizontal = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = 11.5.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ChartShapeTile(
    shape: BudgetChartShape,
    isSelected: Boolean,
    languageMode: LanguageMode,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val icon: ImageVector = when (shape) {
        BudgetChartShape.DONUT -> Icons.Default.DonutLarge
        BudgetChartShape.PIE -> Icons.Default.PieChart
        BudgetChartShape.BAR -> Icons.Default.BarChart
        BudgetChartShape.VERTICAL_BAR -> Icons.Default.Leaderboard
        BudgetChartShape.BUDGET_VS_ACTUAL -> Icons.Default.CompareArrows
        BudgetChartShape.STACKED -> Icons.Default.StackedBarChart
    }

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(
            if (isSelected) 1.5.dp else 0.5.dp,
            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = shape.getLabel(languageMode),
                fontSize = 11.5.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ModernToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.5.sp,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.size(width = 44.dp, height = 24.dp)
        )
    }
}
