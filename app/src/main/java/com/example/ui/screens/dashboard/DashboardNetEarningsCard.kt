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
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.window.Dialog
import com.example.data.model.Account
import com.example.data.model.Category
import com.example.data.model.LanguageMode
import com.example.data.model.TransactionWithDetails
import com.example.util.DashboardChartUtils
import com.example.util.IconHelper
import com.example.util.LanguageHelper
import com.example.util.NetEarningsMetricFilter
import com.example.util.SummaryChartType
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardNetEarningsCard(
    transactions: List<TransactionWithDetails>,
    allAccounts: List<Account>,
    allCategories: List<Category>,
    languageMode: LanguageMode,
    onNetEarningsClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var timeframe by remember { mutableStateOf(ChartTimeframe.THREE_MONTHS) }
    var chartType by remember { mutableStateOf(SummaryChartType.GROUPED_BAR) }
    var metricFilter by remember { mutableStateOf(NetEarningsMetricFilter.INCOME_AND_EXPENSE) }
    var showYAxis by remember { mutableStateOf(true) }
    var compactNumbers by remember { mutableStateOf(true) }
    var showGridLines by remember { mutableStateOf(true) }
    var showLegend by remember { mutableStateOf(true) }
    var showTable by remember { mutableStateOf(true) }

    // Account and Category filters
    var selectedAccountIds by remember { mutableStateOf<Set<Long>?>(null) }
    var selectedCategoryIds by remember { mutableStateOf<Set<Long>?>(null) }

    var showTimeframeDropdown by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showAccountFilterDialog by remember { mutableStateOf(false) }
    var showCategoryFilterDialog by remember { mutableStateOf(false) }
    var selectedMonthIndex by remember { mutableStateOf<Int?>(null) }

    val monthlyPoints = remember(timeframe, transactions, allAccounts, allCategories, selectedAccountIds, selectedCategoryIds, languageMode) {
        DashboardChartUtils.computeMonthlyPoints(
            monthCount = timeframe.months,
            transactions = transactions,
            allAccounts = allAccounts,
            allCategories = allCategories,
            currentTotalAssets = 0.0,
            currentTotalLiabilities = 0.0,
            languageMode = languageMode,
            selectedAccountIds = selectedAccountIds,
            selectedCategoryIds = selectedCategoryIds
        )
    }

    val expenseColor = Color(0xFFE83F6F) // Magenta / Coral Pink
    val incomeColor = Color(0xFF00C988)  // Emerald / Teal Green
    val netBlueColor = Color(0xFF38BDF8) // Cyan / Blue

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
            .testTag("dashboard_net_earnings_card")
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Header: Title + Dropdown Timeframe + Filter Badges + Settings
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
                            text = if (languageMode == LanguageMode.BANGLA) "নিট আয় (Net Earnings)" else "Net Earnings",
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
                    // Accounts Filter chip
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
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalance,
                                contentDescription = "Accounts",
                                modifier = Modifier.size(13.dp),
                                tint = if (selectedAccountIds != null && selectedAccountIds!!.isNotEmpty())
                                    MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (selectedAccountIds == null || selectedAccountIds!!.isEmpty()) {
                                    if (languageMode == LanguageMode.BANGLA) "হিসাব" else "Acc"
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

                    // Category Filter chip
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (selectedCategoryIds != null && selectedCategoryIds!!.isNotEmpty())
                            MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showCategoryFilterDialog = true }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Category,
                                contentDescription = "Categories",
                                modifier = Modifier.size(13.dp),
                                tint = if (selectedCategoryIds != null && selectedCategoryIds!!.isNotEmpty())
                                    MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (selectedCategoryIds == null || selectedCategoryIds!!.isEmpty()) {
                                    if (languageMode == LanguageMode.BANGLA) "ক্যাটেগরি" else "Cat"
                                } else {
                                    "${selectedCategoryIds!!.size}"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (selectedCategoryIds != null && selectedCategoryIds!!.isNotEmpty())
                                    MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
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
                        onClick = onNetEarningsClick,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Details",
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
                NetEarningsMetricFilter.values().forEach { filter ->
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
                            if (metricFilter == NetEarningsMetricFilter.INCOME_AND_EXPENSE || metricFilter == NetEarningsMetricFilter.INCOME_ONLY) {
                                Text(
                                    text = "Inc: +${LanguageHelper.formatCurrency(pt.income, languageMode)}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = incomeColor
                                )
                            }
                            if (metricFilter == NetEarningsMetricFilter.INCOME_AND_EXPENSE || metricFilter == NetEarningsMetricFilter.EXPENSE_ONLY) {
                                Text(
                                    text = "Exp: -${LanguageHelper.formatCurrency(pt.expense, languageMode)}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = expenseColor
                                )
                            }
                            if (metricFilter == NetEarningsMetricFilter.NET_ONLY) {
                                val netCol = if (pt.netEarnings >= 0) incomeColor else expenseColor
                                Text(
                                    text = "Net: ${LanguageHelper.formatCurrency(pt.netEarnings, languageMode)}",
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
                    if (metricFilter == NetEarningsMetricFilter.INCOME_AND_EXPENSE || metricFilter == NetEarningsMetricFilter.INCOME_ONLY) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(incomeColor))
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "আয়" else "Income",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (metricFilter == NetEarningsMetricFilter.INCOME_AND_EXPENSE || metricFilter == NetEarningsMetricFilter.EXPENSE_ONLY) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(expenseColor))
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "ব্যয়" else "Expense",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (metricFilter == NetEarningsMetricFilter.NET_ONLY || chartType == SummaryChartType.LINE) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(netBlueColor))
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "নিট আয়" else "Net Earnings",
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
                    NetEarningsMetricFilter.INCOME_AND_EXPENSE ->
                        monthlyPoints.maxOfOrNull { maxOf(it.income, it.expense) } ?: 0.0
                    NetEarningsMetricFilter.NET_ONLY ->
                        monthlyPoints.maxOfOrNull { Math.abs(it.netEarnings) } ?: 0.0
                    NetEarningsMetricFilter.INCOME_ONLY ->
                        monthlyPoints.maxOfOrNull { it.income } ?: 0.0
                    NetEarningsMetricFilter.EXPENSE_ONLY ->
                        monthlyPoints.maxOfOrNull { it.expense } ?: 0.0
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
                                val isDual = metricFilter == NetEarningsMetricFilter.INCOME_AND_EXPENSE
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
                                        NetEarningsMetricFilter.INCOME_AND_EXPENSE -> {
                                            val expHeight = (pt.expense / chartScaleMax).toFloat().coerceIn(0f, 1f) * plotHeight
                                            val incHeight = (pt.income / chartScaleMax).toFloat().coerceIn(0f, 1f) * plotHeight

                                            val expLeft = colCenterX - singleBarWidth - barSpacing / 2f
                                            val incLeft = colCenterX + barSpacing / 2f

                                            if (expHeight > 0f) {
                                                drawRoundRect(
                                                    color = expenseColor,
                                                    topLeft = Offset(expLeft, topPadding + plotHeight - expHeight),
                                                    size = Size(singleBarWidth, expHeight),
                                                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                                )
                                            }
                                            if (incHeight > 0f) {
                                                drawRoundRect(
                                                    color = incomeColor,
                                                    topLeft = Offset(incLeft, topPadding + plotHeight - incHeight),
                                                    size = Size(singleBarWidth, incHeight),
                                                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                                )
                                            }
                                        }
                                        NetEarningsMetricFilter.NET_ONLY -> {
                                            val netH = (Math.abs(pt.netEarnings) / chartScaleMax).toFloat().coerceIn(0f, 1f) * plotHeight
                                            val netCol = if (pt.netEarnings >= 0) incomeColor else expenseColor
                                            if (netH > 0f) {
                                                drawRoundRect(
                                                    color = netCol,
                                                    topLeft = Offset(colCenterX - singleBarWidth / 2f, topPadding + plotHeight - netH),
                                                    size = Size(singleBarWidth, netH),
                                                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                                )
                                            }
                                        }
                                        NetEarningsMetricFilter.INCOME_ONLY -> {
                                            val incHeight = (pt.income / chartScaleMax).toFloat().coerceIn(0f, 1f) * plotHeight
                                            if (incHeight > 0f) {
                                                drawRoundRect(
                                                    color = incomeColor,
                                                    topLeft = Offset(colCenterX - singleBarWidth / 2f, topPadding + plotHeight - incHeight),
                                                    size = Size(singleBarWidth, incHeight),
                                                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                                )
                                            }
                                        }
                                        NetEarningsMetricFilter.EXPENSE_ONLY -> {
                                            val expHeight = (pt.expense / chartScaleMax).toFloat().coerceIn(0f, 1f) * plotHeight
                                            if (expHeight > 0f) {
                                                drawRoundRect(
                                                    color = expenseColor,
                                                    topLeft = Offset(colCenterX - singleBarWidth / 2f, topPadding + plotHeight - expHeight),
                                                    size = Size(singleBarWidth, expHeight),
                                                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                                )
                                            }
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

                                    val incHeight = (pt.income / chartScaleMax).toFloat().coerceIn(0f, 1f) * plotHeight
                                    val expHeight = (pt.expense / chartScaleMax).toFloat().coerceIn(0f, 1f) * plotHeight
                                    val barLeft = colCenterX - barWidth / 2f

                                    if (incHeight > 0f) {
                                        drawRoundRect(
                                            color = incomeColor,
                                            topLeft = Offset(barLeft, topPadding + plotHeight - incHeight),
                                            size = Size(barWidth, incHeight),
                                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                        )
                                    }
                                    if (expHeight > 0f) {
                                        val expTop = (topPadding + plotHeight - incHeight - expHeight).coerceAtLeast(topPadding)
                                        drawRoundRect(
                                            color = expenseColor,
                                            topLeft = Offset(barLeft, expTop),
                                            size = Size(barWidth, expHeight),
                                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                        )
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
                                val incPoints = mutableListOf<Offset>()
                                val expPoints = mutableListOf<Offset>()
                                val netPoints = mutableListOf<Offset>()

                                monthlyPoints.forEachIndexed { index, pt ->
                                    val colCenterX = leftPadding + index * colWidth + colWidth / 2f

                                    val incY = topPadding + plotHeight * (1f - (pt.income / chartScaleMax).toFloat().coerceIn(0f, 1f))
                                    val expY = topPadding + plotHeight * (1f - (pt.expense / chartScaleMax).toFloat().coerceIn(0f, 1f))
                                    val netY = topPadding + plotHeight * (1f - (Math.abs(pt.netEarnings) / chartScaleMax).toFloat().coerceIn(0f, 1f))

                                    incPoints.add(Offset(colCenterX, incY))
                                    expPoints.add(Offset(colCenterX, expY))
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

                                    pointsList.forEach { pt ->
                                        drawCircle(color = Color.White, radius = 4.dp.toPx(), center = pt)
                                        drawCircle(color = color, radius = 2.5.dp.toPx(), center = pt)
                                    }
                                }

                                val isArea = chartType == SummaryChartType.AREA

                                when (metricFilter) {
                                    NetEarningsMetricFilter.INCOME_AND_EXPENSE -> {
                                        drawCurve(incPoints, incomeColor, isArea)
                                        drawCurve(expPoints, expenseColor, isArea)
                                    }
                                    NetEarningsMetricFilter.INCOME_ONLY -> drawCurve(incPoints, incomeColor, isArea)
                                    NetEarningsMetricFilter.EXPENSE_ONLY -> drawCurve(expPoints, expenseColor, isArea)
                                    NetEarningsMetricFilter.NET_ONLY -> drawCurve(netPoints, netBlueColor, isArea)
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

                    // Row 1: Income
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
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(incomeColor))
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "আয়" else "Income",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = LanguageHelper.formatCurrency(prevMonth.income, languageMode),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.End,
                            color = incomeColor,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = LanguageHelper.formatCurrency(latestMonth.income, languageMode),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.End,
                            color = incomeColor,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Row 2: Expense
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
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(expenseColor))
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "ব্যয়" else "Expense",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "-${LanguageHelper.formatCurrency(prevMonth.expense, languageMode)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.End,
                            color = expenseColor,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "-${LanguageHelper.formatCurrency(latestMonth.expense, languageMode)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.End,
                            color = expenseColor,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Row 3: Net Earnings
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
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(netBlueColor))
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "নিট আয়" else "Net Earnings",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        val prevNetColor = if (prevMonth.netEarnings >= 0) incomeColor else expenseColor
                        val latestNetColor = if (latestMonth.netEarnings >= 0) incomeColor else expenseColor

                        Text(
                            text = (if (prevMonth.netEarnings > 0) "+" else "") + LanguageHelper.formatCurrency(prevMonth.netEarnings, languageMode),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.End,
                            color = prevNetColor,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = (if (latestMonth.netEarnings > 0) "+" else "") + LanguageHelper.formatCurrency(latestMonth.netEarnings, languageMode),
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

    // Settings Dialog
    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = {
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "নিট আয় চার্ট সেটিংস" else "Net Earnings Chart Settings",
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
                            NetEarningsMetricFilter.values().forEach { filter ->
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
                            text = if (languageMode == LanguageMode.BANGLA) "সময়কাল (মাস)" else "Timeframe (Months)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            ChartTimeframe.values().forEach { tf ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (timeframe == tf) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { timeframe = tf }
                                ) {
                                    Text(
                                        text = tf.getLabel(languageMode),
                                        fontSize = 10.5.sp,
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

    // Category Multi-Select Filter Dialog
    if (showCategoryFilterDialog) {
        CategoryMultiSelectFilterDialog(
            allCategories = allCategories,
            selectedCategoryIds = selectedCategoryIds,
            languageMode = languageMode,
            onDismiss = { showCategoryFilterDialog = false },
            onApply = { newSelected ->
                selectedCategoryIds = newSelected
                showCategoryFilterDialog = false
            }
        )
    }
}

/**
 * Reusable dialog for multi-selecting categories with search and Select All / Clear All
 */
@Composable
fun CategoryMultiSelectFilterDialog(
    allCategories: List<Category>,
    selectedCategoryIds: Set<Long>?,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onApply: (Set<Long>?) -> Unit
) {
    val initialSelection = remember(selectedCategoryIds, allCategories) {
        if (selectedCategoryIds == null || selectedCategoryIds.isEmpty()) {
            allCategories.map { it.id }.toSet()
        } else {
            selectedCategoryIds
        }
    }
    var currentSelection by remember { mutableStateOf(initialSelection) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredCategories = remember(allCategories, searchQuery) {
        if (searchQuery.isBlank()) allCategories
        else {
            val q = searchQuery.trim().lowercase()
            allCategories.filter {
                it.nameEn.lowercase().contains(q) ||
                        it.nameBn.lowercase().contains(q) ||
                        it.type.name.lowercase().contains(q)
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
                        text = if (languageMode == LanguageMode.BANGLA) "ক্যাটেগরি নির্বাচন করুন" else "Select Categories",
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
                            text = if (languageMode == LanguageMode.BANGLA) "ক্যাটেগরি খুঁজুন..." else "Search categories...",
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
                            "${LanguageHelper.toBanglaDigits(currentSelection.size.toString())}/${LanguageHelper.toBanglaDigits(allCategories.size.toString())} নির্বাচিত"
                        else
                            "${currentSelection.size}/${allCategories.size} selected",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        TextButton(
                            onClick = { currentSelection = allCategories.map { it.id }.toSet() },
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

                // Category items list
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filteredCategories, key = { it.id }) { cat ->
                        val isChecked = currentSelection.contains(cat.id)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    currentSelection = if (isChecked) {
                                        currentSelection - cat.id
                                    } else {
                                        currentSelection + cat.id
                                    }
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { checked ->
                                    currentSelection = if (checked) {
                                        currentSelection + cat.id
                                    } else {
                                        currentSelection - cat.id
                                    }
                                },
                                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.secondary)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(android.graphics.Color.parseColor(cat.colorHex.ifBlank { "#E83F6F" }))),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = IconHelper.getIconByName(cat.iconName),
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) cat.nameBn else cat.nameEn,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = cat.type.name,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
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
                            if (currentSelection.size == allCategories.size) {
                                onApply(null) // null = all
                            } else {
                                onApply(currentSelection)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text(text = LanguageHelper.getString("apply", languageMode))
                    }
                }
            }
        }
    }
}
