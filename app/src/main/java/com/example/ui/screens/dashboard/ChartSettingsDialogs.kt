package com.example.ui.screens.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.LanguageMode
import com.example.ui.theme.SolidPrimary
import com.example.util.BudgetChartShape
import com.example.util.BudgetSummaryType
import com.example.util.CalendarDisplayMode
import com.example.util.DailySummaryMode
import com.example.util.DailySummaryPeriod
import com.example.util.LanguageHelper

@Composable
fun DailySummarySettingsDialog(
    currentMode: DailySummaryMode,
    currentPeriod: DailySummaryPeriod,
    currentShowValues: Boolean,
    currentShowAverages: Boolean,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onSave: (mode: DailySummaryMode, period: DailySummaryPeriod, showValues: Boolean, showAverages: Boolean) -> Unit
) {
    var mode by remember { mutableStateOf(currentMode) }
    var period by remember { mutableStateOf(currentPeriod) }
    var showValues by remember { mutableStateOf(currentShowValues) }
    var showAverages by remember { mutableStateOf(currentShowAverages) }

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
                            text = LanguageHelper.getString("daily_summary_settings", languageMode),
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

                // Mode Selection (Expense / Income / Both)
                Text(
                    text = LanguageHelper.getString("display_mode", languageMode),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                DailySummaryMode.values().forEach { m ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { mode = m }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (mode == m),
                            onClick = { mode = m },
                            colors = RadioButtonDefaults.colors(selectedColor = SolidPrimary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = m.getLabel(languageMode),
                            fontSize = 13.5.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(10.dp))

                // Period Selection
                Text(
                    text = LanguageHelper.getString("time_period", languageMode),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                DailySummaryPeriod.values().forEach { p ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { period = p }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (period == p),
                            onClick = { period = p },
                            colors = RadioButtonDefaults.colors(selectedColor = SolidPrimary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = p.getLabel(languageMode),
                            fontSize = 13.5.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(10.dp))

                // Toggles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = LanguageHelper.getString("show_values_on_bars", languageMode),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Switch(
                        checked = showValues,
                        onCheckedChange = { showValues = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = SolidPrimary)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = LanguageHelper.getString("show_period_averages", languageMode),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Switch(
                        checked = showAverages,
                        onCheckedChange = { showAverages = it },
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
                        onClick = { onSave(mode, period, showValues, showAverages) },
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
    var maxCategories by remember { mutableStateOf(currentMaxCategories) }
    var showPercentages by remember { mutableStateOf(currentShowPercentages) }
    var showTodayPace by remember { mutableStateOf(currentShowTodayPace) }

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
                            text = LanguageHelper.getString("budget_chart_settings", languageMode),
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

                // Chart Shape (Donut vs Pie)
                Text(
                    text = LanguageHelper.getString("chart_type", languageMode),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                BudgetChartShape.values().take(2).forEach { s ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { shape = s }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (shape == s),
                            onClick = { shape = s },
                            colors = RadioButtonDefaults.colors(selectedColor = SolidPrimary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = s.getLabel(languageMode),
                            fontSize = 13.5.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(10.dp))

                // Category Type (Expense / Income / All)
                Text(
                    text = LanguageHelper.getString("categories_filter", languageMode),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                BudgetSummaryType.values().forEach { t ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { categoryType = t }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (categoryType == t),
                            onClick = { categoryType = t },
                            colors = RadioButtonDefaults.colors(selectedColor = SolidPrimary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = t.getLabel(languageMode),
                            fontSize = 13.5.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(10.dp))

                // Toggles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = LanguageHelper.getString("show_slice_percentages", languageMode),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Switch(
                        checked = showPercentages,
                        onCheckedChange = { showPercentages = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = SolidPrimary)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = LanguageHelper.getString("show_today_pace", languageMode),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Switch(
                        checked = showTodayPace,
                        onCheckedChange = { showTodayPace = it },
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
                        onClick = { onSave(shape, categoryType, maxCategories, showPercentages, showTodayPace) },
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
