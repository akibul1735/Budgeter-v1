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

enum class NetWorthTimeframe(val months: Int, val labelEn: String, val labelBn: String) {
    SIX_MONTHS(6, "6 Months", "৬ মাস"),
    TWELVE_MONTHS(12, "1 Year", "১ বছর"),
    TWENTY_FOUR_MONTHS(24, "2 Years", "২ বছর");

    fun getLabel(languageMode: LanguageMode): String =
        if (languageMode == LanguageMode.BANGLA) labelBn else labelEn
}

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
    var showTimeframeDropdown by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var showTable by remember { mutableStateOf(true) }
    var selectedMonthIndex by remember { mutableStateOf<Int?>(null) }

    val monthlyPoints = remember(timeframe, transactions, allAccounts, allCategories, calculatedAssets, calculatedLiabilities, languageMode) {
        DashboardChartUtils.computeMonthlyPoints(
            monthCount = timeframe.months,
            transactions = transactions,
            allAccounts = allAccounts,
            allCategories = allCategories,
            currentTotalAssets = calculatedAssets,
            currentTotalLiabilities = calculatedLiabilities,
            languageMode = languageMode
        )
    }

    val assetsColor = Color(0xFF00C988)      // Emerald / Teal Green
    val liabilitiesColor = Color(0xFFE83F6F) // Magenta / Coral Pink
    val netWorthColor = Color(0xFF38BDF8)    // Cyan / Blue

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
            .testTag("dashboard_net_worth_card")
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Header: Title + Dropdown Timeframe + Filter Icon
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
                            text = if (languageMode == LanguageMode.BANGLA) "নেট ওয়ার্থ (Net Worth)" else "Net Worth",
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

                // Filter / Customize Button & Navigate Action
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { showFilterDialog = true },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Customize Chart",
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
                            contentDescription = "View Net Worth Details",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
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
                        Column {
                            Text(
                                text = "${pt.getMonthFullLabel(languageMode)} ${pt.year}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "NW: ${LanguageHelper.formatCurrency(pt.netWorth, languageMode)}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = netWorthColor
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Assets: ${LanguageHelper.formatCurrency(pt.assets, languageMode)}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = assetsColor
                            )
                            Text(
                                text = "Liab: -${LanguageHelper.formatCurrency(pt.liabilities, languageMode)}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = liabilitiesColor
                            )
                        }
                    }
                }
            }

            // 2. Multi-Line Trend Chart with Circular Markers Canvas
            val maxVal = remember(monthlyPoints) {
                val max = monthlyPoints.maxOfOrNull { maxOf(it.assets, it.liabilities, Math.abs(it.netWorth)) } ?: 0.0
                if (max <= 0.0) 1000.0 else max * 1.15
            }
            val scaleValues = remember(maxVal) {
                DashboardChartUtils.computeNiceScale(maxVal, 4)
            }
            val chartScaleMax = scaleValues.lastOrNull() ?: maxVal

            val axisTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            val gridLineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
            val selectionHighlight = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(175.dp)
                    .pointerInput(monthlyPoints) {
                        detectTapGestures { offset ->
                            val leftPadding = 52.dp.toPx()
                            val rightPadding = 12.dp.toPx()
                            val plotWidth = size.width - leftPadding - rightPadding
                            val count = monthlyPoints.size
                            if (count > 0 && offset.x >= leftPadding && offset.x <= size.width - rightPadding) {
                                val colWidth = if (count > 1) plotWidth / (count - 1) else plotWidth
                                val tappedIndex = (((offset.x - leftPadding) + (colWidth / 2f)) / colWidth).toInt().coerceIn(0, count - 1)
                                selectedMonthIndex = if (selectedMonthIndex == tappedIndex) null else tappedIndex
                            } else {
                                selectedMonthIndex = null
                            }
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxWidth().height(175.dp)) {
                    val leftPadding = 52.dp.toPx()
                    val rightPadding = 12.dp.toPx()
                    val topPadding = 12.dp.toPx()
                    val bottomPadding = 24.dp.toPx()

                    val plotWidth = size.width - leftPadding - rightPadding
                    val plotHeight = size.height - topPadding - bottomPadding

                    // Draw Horizontal Gridlines & Y-Axis Scale Text
                    val textPaint = android.graphics.Paint().apply {
                        color = axisTextColor.hashCode()
                        textSize = 9.5.sp.toPx()
                        textAlign = android.graphics.Paint.Align.RIGHT
                        isAntiAlias = true
                    }

                    val xTextPaint = android.graphics.Paint().apply {
                        color = axisTextColor.hashCode()
                        textSize = 10.sp.toPx()
                        textAlign = android.graphics.Paint.Align.CENTER
                        isAntiAlias = true
                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                    }

                    scaleValues.forEach { scaleVal ->
                        val yRatio = (scaleVal / chartScaleMax).toFloat().coerceIn(0f, 1f)
                        val yPos = topPadding + plotHeight * (1f - yRatio)

                        // Grid line
                        drawLine(
                            color = gridLineColor,
                            start = Offset(leftPadding, yPos),
                            end = Offset(size.width - rightPadding, yPos),
                            strokeWidth = 1.dp.toPx()
                        )

                        // Label
                        val label = DashboardChartUtils.formatCompactAmount(scaleVal, languageMode)
                        drawContext.canvas.nativeCanvas.drawText(
                            label,
                            leftPadding - 8.dp.toPx(),
                            yPos + 3.5.dp.toPx(),
                            textPaint
                        )
                    }

                    // Draw Trend Lines
                    val count = monthlyPoints.size
                    if (count > 0) {
                        val stepX = if (count > 1) plotWidth / (count - 1) else plotWidth

                        // Highlight selected point column
                        if (selectedMonthIndex != null && selectedMonthIndex in 0 until count) {
                            val colX = leftPadding + selectedMonthIndex!! * stepX
                            drawLine(
                                color = selectionHighlight.copy(alpha = 0.5f),
                                start = Offset(colX, topPadding),
                                end = Offset(colX, topPadding + plotHeight),
                                strokeWidth = 18.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        }

                        val assetsPath = Path()
                        val liabilitiesPath = Path()
                        val netWorthPath = Path()

                        val assetPoints = mutableListOf<Offset>()
                        val liabilityPoints = mutableListOf<Offset>()
                        val netWorthPoints = mutableListOf<Offset>()

                        monthlyPoints.forEachIndexed { index, pt ->
                            val xPos = leftPadding + index * stepX

                            val assetY = topPadding + plotHeight * (1f - (pt.assets / chartScaleMax).toFloat().coerceIn(0f, 1f))
                            val liabY = topPadding + plotHeight * (1f - (pt.liabilities / chartScaleMax).toFloat().coerceIn(0f, 1f))
                            val nwY = topPadding + plotHeight * (1f - (pt.netWorth.coerceAtLeast(0.0) / chartScaleMax).toFloat().coerceIn(0f, 1f))

                            val aPt = Offset(xPos, assetY)
                            val lPt = Offset(xPos, liabY)
                            val nPt = Offset(xPos, nwY)

                            assetPoints.add(aPt)
                            liabilityPoints.add(lPt)
                            netWorthPoints.add(nPt)

                            if (index == 0) {
                                assetsPath.moveTo(aPt.x, aPt.y)
                                liabilitiesPath.moveTo(lPt.x, lPt.y)
                                netWorthPath.moveTo(nPt.x, nPt.y)
                            } else {
                                assetsPath.lineTo(aPt.x, aPt.y)
                                liabilitiesPath.lineTo(lPt.x, lPt.y)
                                netWorthPath.lineTo(nPt.x, nPt.y)
                            }

                            // Draw X-Axis label
                            val shouldDrawLabel = when {
                                count <= 6 -> true
                                count <= 12 -> index % 2 == 0 || index == count - 1
                                else -> index % 3 == 0 || index == count - 1
                            }
                            if (shouldDrawLabel) {
                                drawContext.canvas.nativeCanvas.drawText(
                                    pt.getMonthLabel(languageMode),
                                    xPos,
                                    size.height - 4.dp.toPx(),
                                    xTextPaint
                                )
                            }
                        }

                        // Draw Lines
                        drawPath(
                            path = liabilitiesPath,
                            color = liabilitiesColor,
                            style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )
                        drawPath(
                            path = assetsPath,
                            color = assetsColor,
                            style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )
                        drawPath(
                            path = netWorthPath,
                            color = netWorthColor,
                            style = Stroke(width = 2.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )

                        // Draw Dot Markers
                        liabilityPoints.forEach { pt ->
                            drawCircle(color = Color.White, radius = 4.dp.toPx(), center = pt)
                            drawCircle(color = liabilitiesColor, radius = 2.8.dp.toPx(), center = pt)
                        }
                        assetPoints.forEach { pt ->
                            drawCircle(color = Color.White, radius = 4.dp.toPx(), center = pt)
                            drawCircle(color = assetsColor, radius = 2.8.dp.toPx(), center = pt)
                        }
                        netWorthPoints.forEach { pt ->
                            drawCircle(color = Color.White, radius = 4.5.dp.toPx(), center = pt)
                            drawCircle(color = netWorthColor, radius = 3.2.dp.toPx(), center = pt)
                        }
                    }
                }
            }

            // 3. Comparison Table (Type, Prev Month, Current Month) exactly matching the screenshot
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

                    // Row 1: Assets (Green Dot)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAssetsClick() },
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
                                    .background(assetsColor)
                            )
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "সম্পদ (Assets)" else "Assets",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal,
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

                    // Row 2: Liabilities (Orange/Coral Dot)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLiabilitiesClick() },
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
                                    .background(liabilitiesColor)
                            )
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "দায় (Liabilities)" else "Liabilities",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal,
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

                    // Row 3: Net Worth (Blue Dot)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNetWorthClick() },
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
                                    .background(netWorthColor)
                            )
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "নেট ওয়ার্থ" else "Net Worth",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        val prevNetColor = if (prevMonth.netWorth >= 0) netWorthColor else liabilitiesColor
                        val latestNetColor = if (latestMonth.netWorth >= 0) netWorthColor else liabilitiesColor

                        Text(
                            text = LanguageHelper.formatCurrency(prevMonth.netWorth, languageMode),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.End,
                            color = prevNetColor,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = LanguageHelper.formatCurrency(latestMonth.netWorth, languageMode),
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

    // Filter & Customization Dialog
    if (showFilterDialog) {
        AlertDialog(
            onDismissRequest = { showFilterDialog = false },
            title = {
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "নেট ওয়ার্থ চার্ট সেটিংস" else "Net Worth Chart Settings",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "সময়কাল" else "Timeframe",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
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

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "তুলনামূলক টেবিল দেখান" else "Show Comparison Table",
                            fontSize = 13.sp
                        )
                        Switch(
                            checked = showTable,
                            onCheckedChange = { showTable = it }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFilterDialog = false }) {
                    Text(LanguageHelper.getString("done", languageMode))
                }
            }
        )
    }
}
