package com.example.ui.screens.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Account
import com.example.data.model.Category
import com.example.data.model.LanguageMode
import com.example.data.model.TransactionWithDetails
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidTransfer
import com.example.util.CashFlowMetricFilter
import com.example.util.DashboardChartUtils
import com.example.util.IconHelper
import com.example.util.LanguageHelper
import com.example.util.MonthlyFinancialPoint
import com.example.util.SummaryChartType
import java.util.Locale

enum class ChartTimeframe(val months: Int, val labelEn: String, val labelBn: String) {
    TWO_MONTHS(2, "2 Months", "২ মাস"),
    THREE_MONTHS(3, "3 Months", "৩ মাস"),
    FOUR_MONTHS(4, "4 Months", "৪ মাস"),
    FIVE_MONTHS(5, "5 Months", "৫ মাস"),
    SIX_MONTHS(6, "6 Months", "৬ মাস"),
    NINE_MONTHS(9, "9 Months", "৯ মাস"),
    TWELVE_MONTHS(12, "12 Months", "১২ মাস");

    fun getLabel(languageMode: LanguageMode): String =
        if (languageMode == LanguageMode.BANGLA) labelBn else labelEn

    fun getShortLabel(languageMode: LanguageMode): String =
        if (languageMode == LanguageMode.BANGLA) labelBn else "${months}M"
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardCashFlowSummaryCard(
    transactions: List<TransactionWithDetails>,
    allAccounts: List<Account>,
    allCategories: List<Category>,
    languageMode: LanguageMode,
    onNavigateToCashFlow: () -> Unit,
    modifier: Modifier = Modifier
) {
    var timeframe by remember { mutableStateOf(ChartTimeframe.THREE_MONTHS) }
    var chartType by remember { mutableStateOf(SummaryChartType.GROUPED_BAR) }
    var metricFilter by remember { mutableStateOf(CashFlowMetricFilter.INFLOW_AND_OUTFLOW) }
    var showValues by remember { mutableStateOf(true) }
    var showYAxis by remember { mutableStateOf(true) }
    var compactNumbers by remember { mutableStateOf(true) }
    var showGridLines by remember { mutableStateOf(true) }
    var showLegend by remember { mutableStateOf(true) }
    var showTable by remember { mutableStateOf(true) }

    // Account filtering state
    var selectedAccountIds by remember { mutableStateOf<Set<Long>?>(null) }

    var showTimeframeDropdown by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showAccountFilterDialog by remember { mutableStateOf(false) }
    var selectedMonthIndex by remember { mutableStateOf<Int?>(null) }

    val monthlyPoints = remember(timeframe, transactions, allAccounts, allCategories, selectedAccountIds, languageMode) {
        DashboardChartUtils.computeMonthlyPoints(
            monthCount = timeframe.months,
            transactions = transactions,
            allAccounts = allAccounts,
            allCategories = allCategories,
            currentTotalAssets = 0.0,
            currentTotalLiabilities = 0.0,
            languageMode = languageMode,
            selectedAccountIds = selectedAccountIds
        )
    }

    val outflowColor = SolidExpense
    val inflowColor = SolidIncome
    val netBlueColor = SolidTransfer

    val latestMonth = monthlyPoints.lastOrNull()
    val prevMonth = if (monthlyPoints.size >= 2) monthlyPoints[monthlyPoints.size - 2] else null

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("dashboard_cash_flow_card")
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Header: Title + Dropdown Timeframe + Account Filter Badge + Settings Button + Arrow
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Title with Dropdown Caret
                Box {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showTimeframeDropdown = true }
                            .padding(vertical = 4.dp, horizontal = 2.dp)
                    ) {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "নগদ প্রবাহ (Cash Flow)" else "Cash Flow",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Select Timeframe",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showTimeframeDropdown,
                        onDismissRequest = { showTimeframeDropdown = false }
                    ) {
                        ChartTimeframe.values().forEach { tf ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(tf.getLabel(languageMode), fontSize = 13.sp)
                                        if (timeframe == tf) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    timeframe = tf
                                    selectedMonthIndex = null
                                    showTimeframeDropdown = false
                                }
                            )
                        }
                    }
                }

                // Header Action Buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // Account filter shortcut chip button
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (selectedAccountIds != null && selectedAccountIds!!.isNotEmpty())
                            MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showAccountFilterDialog = true }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalance,
                                contentDescription = "Accounts",
                                modifier = Modifier.size(14.dp),
                                tint = if (selectedAccountIds != null && selectedAccountIds!!.isNotEmpty())
                                    MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (selectedAccountIds == null || selectedAccountIds!!.isEmpty()) {
                                    if (languageMode == LanguageMode.BANGLA) "সকল হিসাব" else "All Accounts"
                                } else {
                                    if (languageMode == LanguageMode.BANGLA) "${LanguageHelper.toBanglaDigits(selectedAccountIds!!.size.toString())}টি হিসাব" else "${selectedAccountIds!!.size} Accounts"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (selectedAccountIds != null && selectedAccountIds!!.isNotEmpty())
                                    MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = { showSettingsDialog = true },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Chart Settings",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    IconButton(
                        onClick = onNavigateToCashFlow,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "View Cash Flow Screen",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Quick Chart Type & Metric Filter Bar
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Metric filter pills
                CashFlowMetricFilter.values().forEach { filter ->
                    val isSelected = metricFilter == filter
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .clickable { metricFilter = filter }
                    ) {
                        Text(
                            text = filter.getLabel(languageMode),
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Interactive Tooltip if month selected
            if (selectedMonthIndex != null && selectedMonthIndex in monthlyPoints.indices) {
                val pt = monthlyPoints[selectedMonthIndex!!]
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${pt.getMonthFullLabel(languageMode)} ${pt.year}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (metricFilter == CashFlowMetricFilter.INFLOW_AND_OUTFLOW || metricFilter == CashFlowMetricFilter.INFLOW_ONLY) {
                                Text(
                                    text = "In: +${LanguageHelper.formatCurrency(pt.cashInflow, languageMode)}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = inflowColor
                                )
                            }
                            if (metricFilter == CashFlowMetricFilter.INFLOW_AND_OUTFLOW || metricFilter == CashFlowMetricFilter.OUTFLOW_ONLY) {
                                Text(
                                    text = "Out: -${LanguageHelper.formatCurrency(pt.cashOutflow, languageMode)}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = outflowColor
                                )
                            }
                            if (metricFilter == CashFlowMetricFilter.NET_ONLY) {
                                val netCol = if (pt.netCashFlow >= 0) inflowColor else outflowColor
                                Text(
                                    text = "Net: ${LanguageHelper.formatCurrency(pt.netCashFlow, languageMode)}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = netCol
                                )
                            }
                        }
                    }
                }
            }

            // Interactive Chart Legend
            if (showLegend) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (metricFilter == CashFlowMetricFilter.INFLOW_AND_OUTFLOW || metricFilter == CashFlowMetricFilter.INFLOW_ONLY) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(inflowColor))
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "ইনফ্লো" else "Inflow",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (metricFilter == CashFlowMetricFilter.INFLOW_AND_OUTFLOW || metricFilter == CashFlowMetricFilter.OUTFLOW_ONLY) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(outflowColor))
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "আউটফ্লো" else "Outflow",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (metricFilter == CashFlowMetricFilter.NET_ONLY || chartType == SummaryChartType.LINE) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(netBlueColor))
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "নিট ক্যাশ" else "Net Cash",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 2. Multi-Mode Chart Canvas (Grouped Bar, Stacked Bar, Line, Area)
            val maxVal = remember(monthlyPoints, metricFilter) {
                val max = when (metricFilter) {
                    CashFlowMetricFilter.INFLOW_AND_OUTFLOW ->
                        monthlyPoints.maxOfOrNull { maxOf(it.cashInflow, it.cashOutflow) } ?: 0.0
                    CashFlowMetricFilter.NET_ONLY ->
                        monthlyPoints.maxOfOrNull { Math.abs(it.netCashFlow) } ?: 0.0
                    CashFlowMetricFilter.INFLOW_ONLY ->
                        monthlyPoints.maxOfOrNull { it.cashInflow } ?: 0.0
                    CashFlowMetricFilter.OUTFLOW_ONLY ->
                        monthlyPoints.maxOfOrNull { it.cashOutflow } ?: 0.0
                }
                if (max <= 0.0) 1000.0 else max * 1.15
            }
            val scaleValues = remember(maxVal) {
                DashboardChartUtils.computeNiceScale(maxVal, 4)
            }
            val chartScaleMax = scaleValues.lastOrNull() ?: maxVal

            val axisTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            val gridLineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
            val selectionHighlight = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)

            val leftPadDp = if (showYAxis) 52.dp else 12.dp

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(175.dp)
                    .pointerInput(monthlyPoints) {
                        detectTapGestures { offset ->
                            val leftPadding = leftPadDp.toPx()
                            val rightPadding = 12.dp.toPx()
                            val plotWidth = size.width - leftPadding - rightPadding
                            val count = monthlyPoints.size
                            if (count > 0 && offset.x >= leftPadding && offset.x <= size.width - rightPadding) {
                                val colWidth = plotWidth / count
                                val tappedIndex = ((offset.x - leftPadding) / colWidth).toInt().coerceIn(0, count - 1)
                                selectedMonthIndex = if (selectedMonthIndex == tappedIndex) null else tappedIndex
                            } else {
                                selectedMonthIndex = null
                            }
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val leftPadding = leftPadDp.toPx()
                    val rightPadding = 12.dp.toPx()
                    val topPadding = 12.dp.toPx()
                    val bottomPadding = 24.dp.toPx()

                    val plotWidth = size.width - leftPadding - rightPadding
                    val plotHeight = size.height - topPadding - bottomPadding

                    // Draw Horizontal Gridlines & Y-Axis Scale Text
                    val textPaint = android.graphics.Paint().apply {
                        color = axisTextColor.toArgb()
                        textSize = 9.5.sp.toPx()
                        textAlign = android.graphics.Paint.Align.RIGHT
                        isAntiAlias = true
                    }

                    val xTextPaint = android.graphics.Paint().apply {
                        color = axisTextColor.toArgb()
                        textSize = 10.5.sp.toPx()
                        textAlign = android.graphics.Paint.Align.CENTER
                        isAntiAlias = true
                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                    }

                    val valueTextPaint = android.graphics.Paint().apply {
                        color = axisTextColor.toArgb()
                        textSize = (if (monthlyPoints.size > 6) 8f else 9.5f).sp.toPx()
                        textAlign = android.graphics.Paint.Align.CENTER
                        isAntiAlias = true
                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                    }

                    if (showGridLines || showYAxis) {
                        scaleValues.forEach { scaleVal ->
                            val yRatio = (scaleVal / chartScaleMax).toFloat().coerceIn(0f, 1f)
                            val yPos = topPadding + plotHeight * (1f - yRatio)

                            // Grid line
                            if (showGridLines) {
                                drawLine(
                                    color = gridLineColor,
                                    start = Offset(leftPadding, yPos),
                                    end = Offset(size.width - rightPadding, yPos),
                                    strokeWidth = 1.dp.toPx()
                                )
                            }

                            // Y-axis Label
                            if (showYAxis) {
                                val label = if (compactNumbers) {
                                    DashboardChartUtils.formatCompactAmount(scaleVal, languageMode)
                                } else {
                                    val formatted = String.format(Locale.US, "%,.0f", scaleVal)
                                    if (languageMode == LanguageMode.BANGLA) LanguageHelper.toBanglaDigits(formatted) else formatted
                                }
                                drawContext.canvas.nativeCanvas.drawText(
                                    label,
                                    leftPadding - 6.dp.toPx(),
                                    yPos + 3.5.dp.toPx(),
                                    textPaint
                                )
                            }
                        }
                    }

                    val count = monthlyPoints.size
                    if (count > 0) {
                        val colWidth = plotWidth / count

                        // Render based on selected chart type
                        when (chartType) {
                            SummaryChartType.GROUPED_BAR -> {
                                val barGroupWidth = colWidth * 0.72f
                                val isDual = metricFilter == CashFlowMetricFilter.INFLOW_AND_OUTFLOW
                                val singleBarWidth = if (isDual) barGroupWidth / 2.1f else barGroupWidth * 0.6f
                                val barSpacing = 2.dp.toPx()

                                monthlyPoints.forEachIndexed { index, pt ->
                                    val colCenterX = leftPadding + index * colWidth + colWidth / 2f
                                    val isSelected = selectedMonthIndex == index

                                    if (isSelected) {
                                        drawRoundRect(
                                            color = selectionHighlight,
                                            topLeft = Offset(colCenterX - colWidth / 2f + 2.dp.toPx(), topPadding),
                                            size = Size(colWidth - 4.dp.toPx(), plotHeight),
                                            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                                        )
                                    }

                                    when (metricFilter) {
                                        CashFlowMetricFilter.INFLOW_AND_OUTFLOW -> {
                                            val outHeight = (pt.cashOutflow / chartScaleMax).toFloat().coerceIn(0f, 1f) * plotHeight
                                            val inHeight = (pt.cashInflow / chartScaleMax).toFloat().coerceIn(0f, 1f) * plotHeight

                                            val outLeft = colCenterX - singleBarWidth - barSpacing / 2f
                                            val inLeft = colCenterX + barSpacing / 2f

                                            if (outHeight > 0f) {
                                                drawRoundRect(
                                                    color = outflowColor,
                                                    topLeft = Offset(outLeft, topPadding + plotHeight - outHeight),
                                                    size = Size(singleBarWidth, outHeight),
                                                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                                )
                                                if (showValues) {
                                                    val valText = DashboardChartUtils.formatCompactAmount(pt.cashOutflow, languageMode)
                                                    val yPos = (topPadding + plotHeight - outHeight - 2.dp.toPx()).coerceAtLeast(topPadding)
                                                    drawContext.canvas.nativeCanvas.drawText(valText, outLeft + singleBarWidth / 2f, yPos, valueTextPaint)
                                                }
                                            }
                                            if (inHeight > 0f) {
                                                drawRoundRect(
                                                    color = inflowColor,
                                                    topLeft = Offset(inLeft, topPadding + plotHeight - inHeight),
                                                    size = Size(singleBarWidth, inHeight),
                                                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                                )
                                                if (showValues) {
                                                    val valText = DashboardChartUtils.formatCompactAmount(pt.cashInflow, languageMode)
                                                    val yPos = (topPadding + plotHeight - inHeight - 2.dp.toPx()).coerceAtLeast(topPadding)
                                                    drawContext.canvas.nativeCanvas.drawText(valText, inLeft + singleBarWidth / 2f, yPos, valueTextPaint)
                                                }
                                            }
                                        }
                                        CashFlowMetricFilter.NET_ONLY -> {
                                            val netH = (Math.abs(pt.netCashFlow) / chartScaleMax).toFloat().coerceIn(0f, 1f) * plotHeight
                                            val netCol = if (pt.netCashFlow >= 0) inflowColor else outflowColor
                                            if (netH > 0f) {
                                                drawRoundRect(
                                                    color = netCol,
                                                    topLeft = Offset(colCenterX - singleBarWidth / 2f, topPadding + plotHeight - netH),
                                                    size = Size(singleBarWidth, netH),
                                                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                                )
                                                if (showValues) {
                                                    val valText = DashboardChartUtils.formatCompactAmount(Math.abs(pt.netCashFlow), languageMode)
                                                    val yPos = (topPadding + plotHeight - netH - 2.dp.toPx()).coerceAtLeast(topPadding)
                                                    drawContext.canvas.nativeCanvas.drawText(valText, colCenterX, yPos, valueTextPaint)
                                                }
                                            }
                                        }
                                        CashFlowMetricFilter.INFLOW_ONLY -> {
                                            val inHeight = (pt.cashInflow / chartScaleMax).toFloat().coerceIn(0f, 1f) * plotHeight
                                            if (inHeight > 0f) {
                                                drawRoundRect(
                                                    color = inflowColor,
                                                    topLeft = Offset(colCenterX - singleBarWidth / 2f, topPadding + plotHeight - inHeight),
                                                    size = Size(singleBarWidth, inHeight),
                                                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                                )
                                                if (showValues) {
                                                    val valText = DashboardChartUtils.formatCompactAmount(pt.cashInflow, languageMode)
                                                    val yPos = (topPadding + plotHeight - inHeight - 2.dp.toPx()).coerceAtLeast(topPadding)
                                                    drawContext.canvas.nativeCanvas.drawText(valText, colCenterX, yPos, valueTextPaint)
                                                }
                                            }
                                        }
                                        CashFlowMetricFilter.OUTFLOW_ONLY -> {
                                            val outHeight = (pt.cashOutflow / chartScaleMax).toFloat().coerceIn(0f, 1f) * plotHeight
                                            if (outHeight > 0f) {
                                                drawRoundRect(
                                                    color = outflowColor,
                                                    topLeft = Offset(colCenterX - singleBarWidth / 2f, topPadding + plotHeight - outHeight),
                                                    size = Size(singleBarWidth, outHeight),
                                                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                                )
                                                if (showValues) {
                                                    val valText = DashboardChartUtils.formatCompactAmount(pt.cashOutflow, languageMode)
                                                    val yPos = (topPadding + plotHeight - outHeight - 2.dp.toPx()).coerceAtLeast(topPadding)
                                                    drawContext.canvas.nativeCanvas.drawText(valText, colCenterX, yPos, valueTextPaint)
                                                }
                                            }
                                        }
                                    }

                                    // X-Axis Month Label
                                    drawContext.canvas.nativeCanvas.drawText(
                                        pt.getMonthLabel(languageMode),
                                        colCenterX,
                                        size.height - 4.dp.toPx(),
                                        xTextPaint
                                    )
                                }
                            }
                            SummaryChartType.STACKED_BAR -> {
                                val barWidth = colWidth * 0.55f
                                monthlyPoints.forEachIndexed { index, pt ->
                                    val colCenterX = leftPadding + index * colWidth + colWidth / 2f
                                    val isSelected = selectedMonthIndex == index

                                    if (isSelected) {
                                        drawRoundRect(
                                            color = selectionHighlight,
                                            topLeft = Offset(colCenterX - colWidth / 2f + 2.dp.toPx(), topPadding),
                                            size = Size(colWidth - 4.dp.toPx(), plotHeight),
                                            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                                        )
                                    }

                                    val inHeight = (pt.cashInflow / chartScaleMax).toFloat().coerceIn(0f, 1f) * plotHeight
                                    val outHeight = (pt.cashOutflow / chartScaleMax).toFloat().coerceIn(0f, 1f) * plotHeight

                                    val barLeft = colCenterX - barWidth / 2f

                                    // Draw Inflow bar (base)
                                    if (inHeight > 0f) {
                                        drawRoundRect(
                                            color = inflowColor,
                                            topLeft = Offset(barLeft, topPadding + plotHeight - inHeight),
                                            size = Size(barWidth, inHeight),
                                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                        )
                                    }
                                    // Draw Outflow bar on top
                                    if (outHeight > 0f) {
                                        val outTop = (topPadding + plotHeight - inHeight - outHeight).coerceAtLeast(topPadding)
                                        drawRoundRect(
                                            color = outflowColor,
                                            topLeft = Offset(barLeft, outTop),
                                            size = Size(barWidth, outHeight),
                                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                        )
                                    }

                                    if (showValues) {
                                        val total = pt.cashInflow + pt.cashOutflow
                                        if (total > 0) {
                                            val totalH = (total / chartScaleMax).toFloat().coerceIn(0f, 1f) * plotHeight
                                            val valText = DashboardChartUtils.formatCompactAmount(total, languageMode)
                                            val yPos = (topPadding + plotHeight - totalH - 2.dp.toPx()).coerceAtLeast(topPadding)
                                            drawContext.canvas.nativeCanvas.drawText(valText, colCenterX, yPos, valueTextPaint)
                                        }
                                    }

                                    drawContext.canvas.nativeCanvas.drawText(
                                        pt.getMonthLabel(languageMode),
                                        colCenterX,
                                        size.height - 4.dp.toPx(),
                                        xTextPaint
                                    )
                                }
                            }
                            SummaryChartType.LINE, SummaryChartType.AREA -> {
                                val inPoints = mutableListOf<Offset>()
                                val outPoints = mutableListOf<Offset>()
                                val netPoints = mutableListOf<Offset>()

                                monthlyPoints.forEachIndexed { index, pt ->
                                    val colCenterX = leftPadding + index * colWidth + colWidth / 2f

                                    val inY = topPadding + plotHeight * (1f - (pt.cashInflow / chartScaleMax).toFloat().coerceIn(0f, 1f))
                                    val outY = topPadding + plotHeight * (1f - (pt.cashOutflow / chartScaleMax).toFloat().coerceIn(0f, 1f))
                                    val netY = topPadding + plotHeight * (1f - (Math.abs(pt.netCashFlow) / chartScaleMax).toFloat().coerceIn(0f, 1f))

                                    inPoints.add(Offset(colCenterX, inY))
                                    outPoints.add(Offset(colCenterX, outY))
                                    netPoints.add(Offset(colCenterX, netY))

                                    val isSelected = selectedMonthIndex == index
                                    if (isSelected) {
                                        drawRoundRect(
                                            color = selectionHighlight,
                                            topLeft = Offset(colCenterX - colWidth / 2f + 2.dp.toPx(), topPadding),
                                            size = Size(colWidth - 4.dp.toPx(), plotHeight),
                                            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                                        )
                                    }

                                    drawContext.canvas.nativeCanvas.drawText(
                                        pt.getMonthLabel(languageMode),
                                        colCenterX,
                                        size.height - 4.dp.toPx(),
                                        xTextPaint
                                    )
                                }

                                fun drawCurve(pointsList: List<Offset>, color: Color, isArea: Boolean) {
                                    if (pointsList.isEmpty()) return
                                    val path = Path().apply {
                                        moveTo(pointsList.first().x, pointsList.first().y)
                                        for (i in 0 until pointsList.size - 1) {
                                             val p0 = pointsList[i]
                                             val p1 = pointsList[i + 1]
                                             val cx = (p0.x + p1.x) / 2f
                                             cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                                        }
                                    }

                                    if (isArea) {
                                        val fillPath = Path().apply {
                                            addPath(path)
                                            lineTo(pointsList.last().x, topPadding + plotHeight)
                                            lineTo(pointsList.first().x, topPadding + plotHeight)
                                            close()
                                        }
                                        drawPath(
                                            path = fillPath,
                                            brush = Brush.verticalGradient(
                                                colors = listOf(color.copy(alpha = 0.35f), color.copy(alpha = 0.02f)),
                                                startY = topPadding,
                                                endY = topPadding + plotHeight
                                            )
                                        )
                                    }

                                    drawPath(
                                        path = path,
                                        color = color,
                                        style = Stroke(
                                            width = 3.dp.toPx(),
                                            cap = StrokeCap.Round,
                                            join = StrokeJoin.Round
                                        )
                                    )

                                    // Draw node dots
                                    pointsList.forEach { pt ->
                                        drawCircle(
                                            color = Color.White,
                                            radius = 4.dp.toPx(),
                                            center = pt
                                        )
                                        drawCircle(
                                            color = color,
                                            radius = 2.5.dp.toPx(),
                                            center = pt
                                        )
                                    }
                                }

                                val isArea = chartType == SummaryChartType.AREA

                                when (metricFilter) {
                                    CashFlowMetricFilter.INFLOW_AND_OUTFLOW -> {
                                        drawCurve(inPoints, inflowColor, isArea)
                                        drawCurve(outPoints, outflowColor, isArea)
                                    }
                                    CashFlowMetricFilter.INFLOW_ONLY -> drawCurve(inPoints, inflowColor, isArea)
                                    CashFlowMetricFilter.OUTFLOW_ONLY -> drawCurve(outPoints, outflowColor, isArea)
                                    CashFlowMetricFilter.NET_ONLY -> drawCurve(netPoints, netBlueColor, isArea)
                                }

                                if (showValues) {
                                    if (metricFilter == CashFlowMetricFilter.INFLOW_AND_OUTFLOW || metricFilter == CashFlowMetricFilter.INFLOW_ONLY) {
                                        inPoints.forEachIndexed { i, ptPos ->
                                            if (monthlyPoints[i].cashInflow > 0) {
                                                val valText = DashboardChartUtils.formatCompactAmount(monthlyPoints[i].cashInflow, languageMode)
                                                drawContext.canvas.nativeCanvas.drawText(valText, ptPos.x, ptPos.y - 6.dp.toPx(), valueTextPaint)
                                            }
                                        }
                                    }
                                    if (metricFilter == CashFlowMetricFilter.INFLOW_AND_OUTFLOW || metricFilter == CashFlowMetricFilter.OUTFLOW_ONLY) {
                                        outPoints.forEachIndexed { i, ptPos ->
                                            if (monthlyPoints[i].cashOutflow > 0) {
                                                val valText = DashboardChartUtils.formatCompactAmount(monthlyPoints[i].cashOutflow, languageMode)
                                                drawContext.canvas.nativeCanvas.drawText(valText, ptPos.x, ptPos.y - 6.dp.toPx(), valueTextPaint)
                                            }
                                        }
                                    }
                                    if (metricFilter == CashFlowMetricFilter.NET_ONLY) {
                                        netPoints.forEachIndexed { i, ptPos ->
                                            if (monthlyPoints[i].netCashFlow != 0.0) {
                                                val valText = DashboardChartUtils.formatCompactAmount(Math.abs(monthlyPoints[i].netCashFlow), languageMode)
                                                drawContext.canvas.nativeCanvas.drawText(valText, ptPos.x, ptPos.y - 6.dp.toPx(), valueTextPaint)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. Comparison Table (Type, Prev Month, Current Month)
            if (showTable && latestMonth != null && prevMonth != null) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Table Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "ধরণ" else "Type",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1.2f)
                        )
                        Text(
                            text = prevMonth.getMonthLabel(languageMode),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.End,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = latestMonth.getMonthLabel(languageMode),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.End,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Row 1: Cash Inflow (Green Dot)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(inflowColor)
                            )
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "নগদ অন্তর্গমন" else "Cash Inflow",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = LanguageHelper.formatCurrency(prevMonth.cashInflow, languageMode),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.End,
                            color = inflowColor,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = LanguageHelper.formatCurrency(latestMonth.cashInflow, languageMode),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.End,
                            color = inflowColor,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Row 2: Cash Outflow (Orange/Coral Dot)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(outflowColor)
                            )
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "নগদ বহির্গমন" else "Cash Outflow",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "-${LanguageHelper.formatCurrency(prevMonth.cashOutflow, languageMode)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.End,
                            color = outflowColor,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "-${LanguageHelper.formatCurrency(latestMonth.cashOutflow, languageMode)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.End,
                            color = outflowColor,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Row 3: Cash Flow (Blue Dot)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(netBlueColor)
                            )
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "নিট প্রবাহ" else "Cash Flow",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        val prevNetColor = if (prevMonth.netCashFlow >= 0) inflowColor else outflowColor
                        val latestNetColor = if (latestMonth.netCashFlow >= 0) inflowColor else outflowColor

                        Text(
                            text = (if (prevMonth.netCashFlow > 0) "+" else "") + LanguageHelper.formatCurrency(prevMonth.netCashFlow, languageMode),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.End,
                            color = prevNetColor,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = (if (latestMonth.netCashFlow > 0) "+" else "") + LanguageHelper.formatCurrency(latestMonth.netCashFlow, languageMode),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.End,
                            color = latestNetColor,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }

    // Chart Settings & Customization Dialog
    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = {
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "ক্যাশ ফ্লো চার্ট সেটিংস" else "Cash Flow Chart Settings",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Chart Type Selection
                    item {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "চার্টের ধরণ" else "Chart Type",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            SummaryChartType.values().forEach { type ->
                                val isSelected = chartType == type
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { chartType = type }
                                ) {
                                    Text(
                                        text = type.getLabel(languageMode),
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Chart Values / Metric Filter
                    item {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "চার্ট মান ও ফিল্টার" else "Chart Values & Metrics",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            CashFlowMetricFilter.values().forEach { filter ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { metricFilter = filter }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    androidx.compose.material3.RadioButton(
                                        selected = metricFilter == filter,
                                        onClick = { metricFilter = filter }
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = filter.getLabel(languageMode), fontSize = 13.sp)
                                }
                            }
                        }
                    }

                    // Timeframe Selector
                    item {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "সময়কাল (মাস)" else "Timeframe (Months)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ChartTimeframe.values().forEach { tf ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (timeframe == tf) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .weight(1f, fill = false)
                                        .clickable { timeframe = tf }
                                ) {
                                    Text(
                                        text = tf.getLabel(languageMode),
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (timeframe == tf) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Axis & Display Toggles
                    item {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "অক্ষ ও ডিসপ্লে বিকল্প" else "Axis & Display Options",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = if (languageMode == LanguageMode.BANGLA) "চার্ট/বারে মান দেখান" else "Show Values on Bars / Chart", fontSize = 13.sp)
                                Switch(checked = showValues, onCheckedChange = { showValues = it })
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = if (languageMode == LanguageMode.BANGLA) "Y-অক্ষ লেবেল দেখান" else "Show Vertical (Y) Axis", fontSize = 13.sp)
                                Switch(checked = showYAxis, onCheckedChange = { showYAxis = it })
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = if (languageMode == LanguageMode.BANGLA) "সংক্ষিপ্ত সংখ্যা (Compact Axis)" else "Compact Values (e.g. 15k)", fontSize = 13.sp)
                                Switch(checked = compactNumbers, onCheckedChange = { compactNumbers = it })
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = if (languageMode == LanguageMode.BANGLA) "গ্রিড লাইন দেখান" else "Show Grid Lines", fontSize = 13.sp)
                                Switch(checked = showGridLines, onCheckedChange = { showGridLines = it })
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = if (languageMode == LanguageMode.BANGLA) "লেজেন্ড দেখান" else "Show Legend", fontSize = 13.sp)
                                Switch(checked = showLegend, onCheckedChange = { showLegend = it })
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = if (languageMode == LanguageMode.BANGLA) "তুলনামূলক টেবিল দেখান" else "Show Comparison Table", fontSize = 13.sp)
                                Switch(checked = showTable, onCheckedChange = { showTable = it })
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSettingsDialog = false }) {
                    Text(LanguageHelper.getString("done", languageMode))
                }
            }
        )
    }

    // Account Multi-Select Filter Dialog
    if (showAccountFilterDialog) {
        AccountMultiSelectFilterDialog(
            allAccounts = allAccounts,
            selectedAccountIds = selectedAccountIds,
            languageMode = languageMode,
            onDismiss = { showAccountFilterDialog = false },
            onApply = { newSelected ->
                selectedAccountIds = newSelected
                showAccountFilterDialog = false
            }
        )
    }
}

/**
 * Reusable dialog for multi-selecting accounts with search and Select All / Clear All
 */
@Composable
fun AccountMultiSelectFilterDialog(
    allAccounts: List<Account>,
    selectedAccountIds: Set<Long>?,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onApply: (Set<Long>?) -> Unit
) {
    val parentIds = remember(allAccounts) { allAccounts.mapNotNull { it.parentId }.toSet() }
    val selectableAccounts = remember(allAccounts, parentIds) {
        val leaves = allAccounts.filter { it.id !in parentIds }
        if (leaves.isNotEmpty()) leaves else allAccounts
    }

    val initialSelection = remember(selectedAccountIds, selectableAccounts) {
        if (selectedAccountIds == null || selectedAccountIds.isEmpty()) {
            selectableAccounts.map { it.id }.toSet()
        } else {
            selectedAccountIds
        }
    }
    var currentSelection by remember { mutableStateOf(initialSelection) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredAccounts = remember(selectableAccounts, searchQuery) {
        if (searchQuery.isBlank()) selectableAccounts
        else {
            val q = searchQuery.trim().lowercase()
            selectableAccounts.filter {
                it.nameEn.lowercase().contains(q) ||
                        it.nameBn.lowercase().contains(q) ||
                        it.type.name.lowercase().contains(q)
            }
        }
    }

    val parentMap = remember(allAccounts) { allAccounts.associateBy { it.id } }
    val groupedAccounts = remember(filteredAccounts, parentMap, languageMode) {
        filteredAccounts.groupBy { acc ->
            if (acc.parentId != null) {
                parentMap[acc.parentId]?.localizedName(languageMode) ?: acc.type.name
            } else {
                acc.type.name
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "হিসাব নির্বাচন করুন" else "Select Accounts for Cash Flow",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "হিসাব খুঁজুন..." else "Search accounts...",
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Select All / Clear All bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA)
                            "${LanguageHelper.toBanglaDigits(currentSelection.size.toString())}/${LanguageHelper.toBanglaDigits(selectableAccounts.size.toString())} নির্বাচিত"
                        else
                            "${currentSelection.size}/${selectableAccounts.size} selected",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        TextButton(
                            onClick = { currentSelection = selectableAccounts.map { it.id }.toSet() },
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(text = if (languageMode == LanguageMode.BANGLA) "সব নির্বাচন" else "Select All", fontSize = 12.sp)
                        }
                        TextButton(
                            onClick = { currentSelection = emptySet() },
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(text = if (languageMode == LanguageMode.BANGLA) "মুছুন" else "Clear", fontSize = 12.sp)
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

                // Account items list grouped by category / group
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    groupedAccounts.forEach { (groupName, accList) ->
                        item(key = "group_header_$groupName") {
                            val groupIds = accList.map { it.id }.toSet()
                            val allGroupSelected = groupIds.all { currentSelection.contains(it) }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .clickable {
                                        currentSelection = if (allGroupSelected) {
                                            currentSelection - groupIds
                                        } else {
                                            currentSelection + groupIds
                                        }
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = groupName,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = if (allGroupSelected) {
                                        if (languageMode == LanguageMode.BANGLA) "সব বাদ দিন" else "Deselect Group"
                                    } else {
                                        if (languageMode == LanguageMode.BANGLA) "গ্রুপ নির্বাচন" else "Select Group"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        items(accList, key = { it.id }) { acc ->
                            val isChecked = currentSelection.contains(acc.id)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        currentSelection = if (isChecked) {
                                            currentSelection - acc.id
                                        } else {
                                            currentSelection + acc.id
                                        }
                                    }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { checked ->
                                        currentSelection = if (checked) {
                                            currentSelection + acc.id
                                        } else {
                                            currentSelection - acc.id
                                        }
                                    },
                                    colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color(android.graphics.Color.parseColor(acc.colorHex.ifBlank { "#1976D2" }))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = IconHelper.getIconByName(acc.iconName),
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) acc.nameBn else acc.nameEn,
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = acc.type.name,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = LanguageHelper.getString("cancel", languageMode))
                    }

                    Button(
                        onClick = {
                            if (currentSelection.size == selectableAccounts.size) {
                                onApply(null) // null = all
                            } else {
                                onApply(currentSelection)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(text = LanguageHelper.getString("apply", languageMode))
                    }
                }
            }
        }
    }
}
