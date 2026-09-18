package com.example.ui.screens.dashboard

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Account
import com.example.data.model.Category
import com.example.data.model.LanguageMode
import com.example.data.model.TransactionWithDetails
import com.example.util.DashboardChartUtils
import com.example.util.LanguageHelper
import com.example.util.NetWorthMetricFilter
import com.example.util.SummaryChartType
import java.util.Locale

enum class NetWorthTimeframe(val months: Int, val labelEn: String, val labelBn: String) {
    SIX_MONTHS(6, "6 Months", "৬ মাস"),
    TWELVE_MONTHS(12, "1 Year", "১ বছর"),
    TWENTY_FOUR_MONTHS(24, "2 Years", "২ বছর");

    fun getLabel(languageMode: LanguageMode): String =
        if (languageMode == LanguageMode.BANGLA) labelBn else labelEn
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardNetWorthTrendCard(
    transactions: List<TransactionWithDetails>,
    allAccounts: List<Account>,
    allCategories: List<Category>,
    calculatedAssets: Double,
    calculatedLiabilities: Double,
    calculatedNetWorth: Double,
    languageMode: LanguageMode,
    onNetWorthClick: () -> Unit = {},
    onAssetsClick: () -> Unit = {},
    onLiabilitiesClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var timeframe by remember { mutableStateOf(NetWorthTimeframe.SIX_MONTHS) }
    var chartType by remember { mutableStateOf(SummaryChartType.LINE) }
    var metricFilter by remember { mutableStateOf(NetWorthMetricFilter.ALL) }
    var showYAxis by remember { mutableStateOf(true) }
    var compactNumbers by remember { mutableStateOf(true) }
    var showGridLines by remember { mutableStateOf(true) }
    var showLegend by remember { mutableStateOf(true) }
    var showTable by remember { mutableStateOf(true) }

    // Account filter state
    var selectedAccountIds by remember { mutableStateOf<Set<Long>?>(null) }

    var showTimeframeDropdown by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showAccountFilterDialog by remember { mutableStateOf(false) }
    var selectedMonthIndex by remember { mutableStateOf<Int?>(null) }

    val monthlyPoints = remember(timeframe, transactions, allAccounts, allCategories, calculatedAssets, calculatedLiabilities, selectedAccountIds, languageMode) {
        DashboardChartUtils.computeMonthlyPoints(
            monthCount = timeframe.months,
            transactions = transactions,
            allAccounts = allAccounts,
            allCategories = allCategories,
            currentTotalAssets = calculatedAssets,
            currentTotalLiabilities = calculatedLiabilities,
            languageMode = languageMode,
            selectedAccountIds = selectedAccountIds
        )
    }

    val assetsColor = Color(0xFF00C988)     // Emerald Green
    val liabilitiesColor = Color(0xFFE83F6F) // Coral Pink / Magenta
    val netWorthColor = Color(0xFF6366F1)   // Indigo / Purple Accent

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
            .testTag("dashboard_net_worth_trend_card")
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Header: Title + Dropdown Timeframe + Account Filter Badge + Settings
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
                            text = if (languageMode == LanguageMode.BANGLA) "নিট সম্পদ ট্রেন্ড" else "Net Worth Trend",
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
                        NetWorthTimeframe.values().forEach { tf ->
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

                // Action Buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // Account filter button
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
                                    if (languageMode == LanguageMode.BANGLA) "সকল হিসাব" else "All"
                                } else {
                                    "${selectedAccountIds!!.size}"
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
                        onClick = onNetWorthClick,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Net Worth Details",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Quick Metric Filter Bar
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                NetWorthMetricFilter.values().forEach { filter ->
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
                            if (metricFilter == NetWorthMetricFilter.ALL || metricFilter == NetWorthMetricFilter.ASSETS_ONLY) {
                                Text(
                                    text = "Ast: ${LanguageHelper.formatCurrency(pt.assets, languageMode)}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = assetsColor
                                )
                            }
                            if (metricFilter == NetWorthMetricFilter.ALL || metricFilter == NetWorthMetricFilter.LIABILITIES_ONLY) {
                                Text(
                                    text = "Liab: ${LanguageHelper.formatCurrency(pt.liabilities, languageMode)}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = liabilitiesColor
                                )
                            }
                            if (metricFilter == NetWorthMetricFilter.ALL || metricFilter == NetWorthMetricFilter.NET_WORTH_ONLY) {
                                Text(
                                    text = "NW: ${LanguageHelper.formatCurrency(pt.netWorth, languageMode)}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = netWorthColor
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
                    if (metricFilter == NetWorthMetricFilter.ALL || metricFilter == NetWorthMetricFilter.NET_WORTH_ONLY) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(netWorthColor))
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "নিট সম্পদ" else "Net Worth",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (metricFilter == NetWorthMetricFilter.ALL || metricFilter == NetWorthMetricFilter.ASSETS_ONLY) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(assetsColor))
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "মোট সম্পদ" else "Assets",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (metricFilter == NetWorthMetricFilter.ALL || metricFilter == NetWorthMetricFilter.LIABILITIES_ONLY) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(liabilitiesColor))
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "মোট দায়" else "Liabilities",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 2. Chart Canvas
            val maxVal = remember(monthlyPoints, metricFilter) {
                val max = when (metricFilter) {
                    NetWorthMetricFilter.ALL ->
                        monthlyPoints.maxOfOrNull { maxOf(it.assets, it.liabilities, it.netWorth) } ?: 0.0
                    NetWorthMetricFilter.NET_WORTH_ONLY ->
                        monthlyPoints.maxOfOrNull { it.netWorth } ?: 0.0
                    NetWorthMetricFilter.ASSETS_ONLY ->
                        monthlyPoints.maxOfOrNull { it.assets } ?: 0.0
                    NetWorthMetricFilter.LIABILITIES_ONLY ->
                        monthlyPoints.maxOfOrNull { it.liabilities } ?: 0.0
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

                    val textPaint = android.graphics.Paint().apply {
                        color = axisTextColor.hashCode()
                        textSize = 9.5.sp.toPx()
                        textAlign = android.graphics.Paint.Align.RIGHT
                        isAntiAlias = true
                    }

                    val xTextPaint = android.graphics.Paint().apply {
                        color = axisTextColor.hashCode()
                        textSize = 10.5.sp.toPx()
                        textAlign = android.graphics.Paint.Align.CENTER
                        isAntiAlias = true
                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                    }

                    if (showGridLines || showYAxis) {
                        scaleValues.forEach { scaleVal ->
                            val yRatio = (scaleVal / chartScaleMax).toFloat().coerceIn(0f, 1f)
                            val yPos = topPadding + plotHeight * (1f - yRatio)

                            if (showGridLines) {
                                drawLine(
                                    color = gridLineColor,
                                    start = Offset(leftPadding, yPos),
                                    end = Offset(size.width - rightPadding, yPos),
                                    strokeWidth = 1.dp.toPx()
                                )
                            }

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

                        when (chartType) {
                            SummaryChartType.GROUPED_BAR -> {
                                val barGroupWidth = colWidth * 0.72f
                                val numBars = when (metricFilter) {
                                    NetWorthMetricFilter.ALL -> 3
                                    else -> 1
                                }
                                val singleBarWidth = barGroupWidth / (numBars + 0.3f)
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
                                        NetWorthMetricFilter.ALL -> {
                                            val astH = (pt.assets / chartScaleMax).toFloat().coerceIn(0f, 1f) * plotHeight
                                            val liabH = (pt.liabilities / chartScaleMax).toFloat().coerceIn(0f, 1f) * plotHeight
                                            val nwH = (pt.netWorth / chartScaleMax).toFloat().coerceIn(0f, 1f) * plotHeight

                                            val left1 = colCenterX - 1.5f * singleBarWidth - barSpacing
                                            val left2 = colCenterX - 0.5f * singleBarWidth
                                            val left3 = colCenterX + 0.5f * singleBarWidth + barSpacing

                                            if (astH > 0f) drawRoundRect(color = assetsColor, topLeft = Offset(left1, topPadding + plotHeight - astH), size = Size(singleBarWidth, astH), cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()))
                                            if (liabH > 0f) drawRoundRect(color = liabilitiesColor, topLeft = Offset(left2, topPadding + plotHeight - liabH), size = Size(singleBarWidth, liabH), cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()))
                                            if (nwH > 0f) drawRoundRect(color = netWorthColor, topLeft = Offset(left3, topPadding + plotHeight - nwH), size = Size(singleBarWidth, nwH), cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()))
                                        }
                                        NetWorthMetricFilter.NET_WORTH_ONLY -> {
                                            val nwH = (pt.netWorth / chartScaleMax).toFloat().coerceIn(0f, 1f) * plotHeight
                                            if (nwH > 0f) drawRoundRect(color = netWorthColor, topLeft = Offset(colCenterX - singleBarWidth / 2f, topPadding + plotHeight - nwH), size = Size(singleBarWidth, nwH), cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()))
                                        }
                                        NetWorthMetricFilter.ASSETS_ONLY -> {
                                            val astH = (pt.assets / chartScaleMax).toFloat().coerceIn(0f, 1f) * plotHeight
                                            if (astH > 0f) drawRoundRect(color = assetsColor, topLeft = Offset(colCenterX - singleBarWidth / 2f, topPadding + plotHeight - astH), size = Size(singleBarWidth, astH), cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()))
                                        }
                                        NetWorthMetricFilter.LIABILITIES_ONLY -> {
                                            val liabH = (pt.liabilities / chartScaleMax).toFloat().coerceIn(0f, 1f) * plotHeight
                                            if (liabH > 0f) drawRoundRect(color = liabilitiesColor, topLeft = Offset(colCenterX - singleBarWidth / 2f, topPadding + plotHeight - liabH), size = Size(singleBarWidth, liabH), cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()))
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

                                    val astH = (pt.assets / chartScaleMax).toFloat().coerceIn(0f, 1f) * plotHeight
                                    val liabH = (pt.liabilities / chartScaleMax).toFloat().coerceIn(0f, 1f) * plotHeight
                                    val barLeft = colCenterX - barWidth / 2f

                                    if (astH > 0f) {
                                        drawRoundRect(color = assetsColor, topLeft = Offset(barLeft, topPadding + plotHeight - astH), size = Size(barWidth, astH), cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()))
                                    }
                                    if (liabH > 0f) {
                                        val liabTop = (topPadding + plotHeight - astH - liabH).coerceAtLeast(topPadding)
                                        drawRoundRect(color = liabilitiesColor, topLeft = Offset(barLeft, liabTop), size = Size(barWidth, liabH), cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()))
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
                                val astPoints = mutableListOf<Offset>()
                                val liabPoints = mutableListOf<Offset>()
                                val nwPoints = mutableListOf<Offset>()

                                monthlyPoints.forEachIndexed { index, pt ->
                                    val colCenterX = leftPadding + index * colWidth + colWidth / 2f

                                    val astY = topPadding + plotHeight * (1f - (pt.assets / chartScaleMax).toFloat().coerceIn(0f, 1f))
                                    val liabY = topPadding + plotHeight * (1f - (pt.liabilities / chartScaleMax).toFloat().coerceIn(0f, 1f))
                                    val nwY = topPadding + plotHeight * (1f - (pt.netWorth / chartScaleMax).toFloat().coerceIn(0f, 1f))

                                    astPoints.add(Offset(colCenterX, astY))
                                    liabPoints.add(Offset(colCenterX, liabY))
                                    nwPoints.add(Offset(colCenterX, nwY))

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

                                    pointsList.forEach { pt ->
                                        drawCircle(color = Color.White, radius = 4.dp.toPx(), center = pt)
                                        drawCircle(color = color, radius = 2.5.dp.toPx(), center = pt)
                                    }
                                }

                                val isArea = chartType == SummaryChartType.AREA

                                when (metricFilter) {
                                    NetWorthMetricFilter.ALL -> {
                                        drawCurve(astPoints, assetsColor, isArea)
                                        drawCurve(liabPoints, liabilitiesColor, isArea)
                                        drawCurve(nwPoints, netWorthColor, isArea)
                                    }
                                    NetWorthMetricFilter.NET_WORTH_ONLY -> drawCurve(nwPoints, netWorthColor, isArea)
                                    NetWorthMetricFilter.ASSETS_ONLY -> drawCurve(astPoints, assetsColor, isArea)
                                    NetWorthMetricFilter.LIABILITIES_ONLY -> drawCurve(liabPoints, liabilitiesColor, isArea)
                                }
                            }
                        }
                    }
                }
            }

            // 3. Comparison Table
            if (showTable && latestMonth != null && prevMonth != null) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "বিবরণ" else "Item",
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

                    // Row 1: Assets
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
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(assetsColor))
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "মোট সম্পদ" else "Total Assets",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = LanguageHelper.formatCurrency(prevMonth.assets, languageMode),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.End,
                            color = assetsColor,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = LanguageHelper.formatCurrency(latestMonth.assets, languageMode),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.End,
                            color = assetsColor,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Row 2: Liabilities
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
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(liabilitiesColor))
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "মোট দায়" else "Total Liabilities",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "-${LanguageHelper.formatCurrency(prevMonth.liabilities, languageMode)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.End,
                            color = liabilitiesColor,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "-${LanguageHelper.formatCurrency(latestMonth.liabilities, languageMode)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.End,
                            color = liabilitiesColor,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Row 3: Net Worth
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
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(netWorthColor))
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "নিট সম্পদ" else "Net Worth",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = LanguageHelper.formatCurrency(prevMonth.netWorth, languageMode),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.End,
                            color = netWorthColor,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = LanguageHelper.formatCurrency(latestMonth.netWorth, languageMode),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.End,
                            color = netWorthColor,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }

    // Settings Dialog
    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = {
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "নিট সম্পদ চার্ট সেটিংস" else "Net Worth Chart Settings",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Chart Type
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

                    // Metric Filter
                    item {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "চার্ট মান ও ফিল্টার" else "Chart Values & Metrics",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            NetWorthMetricFilter.values().forEach { filter ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { metricFilter = filter }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = metricFilter == filter,
                                        onClick = { metricFilter = filter }
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = filter.getLabel(languageMode), fontSize = 13.sp)
                                }
                            }
                        }
                    }

                    // Timeframe
                    item {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "সময়কাল" else "Timeframe",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            NetWorthTimeframe.values().forEach { tf ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (timeframe == tf) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { timeframe = tf }
                                ) {
                                    Text(
                                        text = tf.getLabel(languageMode),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (timeframe == tf) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Toggles
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
