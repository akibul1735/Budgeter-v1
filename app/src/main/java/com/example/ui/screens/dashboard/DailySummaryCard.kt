package com.example.ui.screens.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LanguageMode
import com.example.data.model.TransactionType
import com.example.data.model.TransactionWithDetails
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidPrimary
import com.example.util.DailySummaryMode
import com.example.util.DailySummaryPeriod
import com.example.util.LanguageHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.max

data class DaySummaryData(
    val dateEpochMs: Long,
    val dayLabel: String,
    val fullDateLabel: String,
    val expense: Double,
    val income: Double
)

@Composable
fun DailySummaryCard(
    transactions: List<TransactionWithDetails>,
    mode: DailySummaryMode,
    period: DailySummaryPeriod,
    showValues: Boolean,
    showAverages: Boolean,
    languageMode: LanguageMode,
    onModeChange: (DailySummaryMode) -> Unit,
    onPeriodChange: (DailySummaryPeriod) -> Unit,
    onOpenSettings: () -> Unit,
    onDayClick: (DaySummaryData) -> Unit = {}
) {
    var showDropdownMenu by remember { mutableStateOf(false) }

    // Calculate daily data based on period
    val calendar = Calendar.getInstance()
    val todayEpoch = calendar.timeInMillis
    
    val dayCount = when (period) {
        DailySummaryPeriod.LAST_7_DAYS -> 7
        DailySummaryPeriod.LAST_14_DAYS -> 14
        DailySummaryPeriod.LAST_30_DAYS -> 30
        DailySummaryPeriod.THIS_MONTH -> {
            val maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
            currentDay.coerceAtLeast(7)
        }
    }

    val dailyDataList = remember(transactions, period, dayCount) {
        val list = mutableListOf<DaySummaryData>()
        val cal = Calendar.getInstance()
        
        for (i in (dayCount - 1) downTo 0) {
            cal.timeInMillis = todayEpoch
            cal.add(Calendar.DAY_OF_YEAR, -i)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val startOfDay = cal.timeInMillis
            
            cal.set(Calendar.HOUR_OF_DAY, 23)
            cal.set(Calendar.MINUTE, 59)
            cal.set(Calendar.SECOND, 59)
            cal.set(Calendar.MILLISECOND, 999)
            val endOfDay = cal.timeInMillis

            val dayTxs = transactions.filter {
                it.transaction.dateEpochMs in startOfDay..endOfDay
            }

            val exp = dayTxs.filter { it.transaction.type == TransactionType.EXPENSE }.sumOf { it.transaction.amount }
            val inc = dayTxs.filter { it.transaction.type == TransactionType.INCOME }.sumOf { it.transaction.amount }

            val dayFormat = SimpleDateFormat("EEE", Locale.ENGLISH)
            val fullFormat = SimpleDateFormat("dd MMM", Locale.ENGLISH)
            val dayName = dayFormat.format(Date(startOfDay))
            val fullDate = fullFormat.format(Date(startOfDay))

            list.add(
                DaySummaryData(
                    dateEpochMs = startOfDay,
                    dayLabel = dayName,
                    fullDateLabel = fullDate,
                    expense = exp,
                    income = inc
                )
            )
        }
        list
    }

    // Calculate averages (7-day and 30-day)
    val now = System.currentTimeMillis()
    val sevenDaysAgo = now - 7L * 24 * 60 * 60 * 1000
    val thirtyDaysAgo = now - 30L * 24 * 60 * 60 * 1000

    val sevenDaysTxs = transactions.filter { it.transaction.dateEpochMs in sevenDaysAgo..now }
    val thirtyDaysTxs = transactions.filter { it.transaction.dateEpochMs in thirtyDaysAgo..now }

    val sevenDaysAvg = when (mode) {
        DailySummaryMode.EXPENSE -> sevenDaysTxs.filter { it.transaction.type == TransactionType.EXPENSE }.sumOf { it.transaction.amount } / 7.0
        DailySummaryMode.INCOME -> sevenDaysTxs.filter { it.transaction.type == TransactionType.INCOME }.sumOf { it.transaction.amount } / 7.0
        DailySummaryMode.BOTH -> (sevenDaysTxs.filter { it.transaction.type == TransactionType.INCOME }.sumOf { it.transaction.amount } -
                sevenDaysTxs.filter { it.transaction.type == TransactionType.EXPENSE }.sumOf { it.transaction.amount }) / 7.0
    }

    val thirtyDaysAvg = when (mode) {
        DailySummaryMode.EXPENSE -> thirtyDaysTxs.filter { it.transaction.type == TransactionType.EXPENSE }.sumOf { it.transaction.amount } / 30.0
        DailySummaryMode.INCOME -> thirtyDaysTxs.filter { it.transaction.type == TransactionType.INCOME }.sumOf { it.transaction.amount } / 30.0
        DailySummaryMode.BOTH -> (thirtyDaysTxs.filter { it.transaction.type == TransactionType.INCOME }.sumOf { it.transaction.amount } -
                thirtyDaysTxs.filter { it.transaction.type == TransactionType.EXPENSE }.sumOf { it.transaction.amount }) / 30.0
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("daily_summary_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Title with Dropdown and Settings Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Title and Dropdown Arrow
                Box {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showDropdownMenu = true }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = LanguageHelper.getString("daily_summary", languageMode),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Select Mode or Period",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showDropdownMenu,
                        onDismissRequest = { showDropdownMenu = false }
                    ) {
                        Text(
                            text = "  Mode",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        DailySummaryMode.values().forEach { m ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = m.getLabel(languageMode),
                                        fontWeight = if (m == mode) FontWeight.Bold else FontWeight.Normal,
                                        color = if (m == mode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = {
                                    onModeChange(m)
                                    showDropdownMenu = false
                                }
                            )
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        Text(
                            text = "  Period",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        DailySummaryPeriod.values().forEach { p ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = p.getLabel(languageMode),
                                        fontWeight = if (p == period) FontWeight.Bold else FontWeight.Normal,
                                        color = if (p == period) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = {
                                    onPeriodChange(p)
                                    showDropdownMenu = false
                                }
                            )
                        }
                    }
                }

                // Chart Settings Icon
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Daily Summary Chart Settings",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bar Chart Canvas View
            val maxAmount = remember(dailyDataList, mode) {
                val values = dailyDataList.map {
                    when (mode) {
                        DailySummaryMode.EXPENSE -> it.expense
                        DailySummaryMode.INCOME -> it.income
                        DailySummaryMode.BOTH -> max(it.expense, it.income)
                    }
                }
                val m = values.maxOrNull() ?: 1.0
                if (m <= 0.0) 100.0 else m
            }

            val expenseColor = Color(0xFFF43F5E) // Modern Pink/Coral matching screenshot
            val incomeColor = Color(0xFF10B981) // Modern Emerald Green
            val textColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
            val isDark = MaterialTheme.colorScheme.background.red < 0.5f

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .padding(horizontal = 4.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                    val width = size.width
                    val height = size.height
                    val bottomAxisHeight = 24.dp.toPx()
                    val chartHeight = height - bottomAxisHeight - 20.dp.toPx()
                    val count = dailyDataList.size
                    val slotWidth = width / count

                    // Draw baseline
                    drawLine(
                        color = textColor.copy(alpha = 0.2f),
                        start = Offset(0f, height - bottomAxisHeight),
                        end = Offset(width, height - bottomAxisHeight),
                        strokeWidth = 1.dp.toPx()
                    )

                    val paint = android.graphics.Paint().apply {
                        this.color = if (isDark) android.graphics.Color.WHITE else android.graphics.Color.DKGRAY
                        this.textSize = 9.sp.toPx()
                        this.textAlign = android.graphics.Paint.Align.CENTER
                        this.isAntiAlias = true
                    }

                    val labelPaint = android.graphics.Paint().apply {
                        this.color = if (isDark) android.graphics.Color.LTGRAY else android.graphics.Color.GRAY
                        this.textSize = 10.sp.toPx()
                        this.textAlign = android.graphics.Paint.Align.CENTER
                        this.isAntiAlias = true
                    }

                    dailyDataList.forEachIndexed { index, day ->
                        val centerX = slotWidth * index + slotWidth / 2f
                        val baseBottom = height - bottomAxisHeight

                        // Draw Day Label below baseline
                        drawContext.canvas.nativeCanvas.drawText(
                            day.dayLabel,
                            centerX,
                            baseBottom + 16.dp.toPx(),
                            labelPaint
                        )

                        when (mode) {
                            DailySummaryMode.EXPENSE -> {
                                if (day.expense > 0) {
                                    val barH = (day.expense / maxAmount * chartHeight).toFloat().coerceAtLeast(4.dp.toPx())
                                    val barW = (slotWidth * 0.48f).coerceIn(12.dp.toPx(), 36.dp.toPx())
                                    val barLeft = centerX - barW / 2f
                                    val barTop = baseBottom - barH

                                    drawRoundRect(
                                        color = expenseColor,
                                        topLeft = Offset(barLeft, barTop),
                                        size = Size(barW, barH),
                                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                    )

                                    if (showValues) {
                                        val formatted = if (day.expense >= 1000) "BDT ${(day.expense).toInt()}" else "BDT ${(day.expense).toInt()}"
                                        drawContext.canvas.nativeCanvas.drawText(
                                            formatted,
                                            centerX,
                                            barTop - 4.dp.toPx(),
                                            paint
                                        )
                                    }
                                }
                            }
                            DailySummaryMode.INCOME -> {
                                if (day.income > 0) {
                                    val barH = (day.income / maxAmount * chartHeight).toFloat().coerceAtLeast(4.dp.toPx())
                                    val barW = (slotWidth * 0.48f).coerceIn(12.dp.toPx(), 36.dp.toPx())
                                    val barLeft = centerX - barW / 2f
                                    val barTop = baseBottom - barH

                                    drawRoundRect(
                                        color = incomeColor,
                                        topLeft = Offset(barLeft, barTop),
                                        size = Size(barW, barH),
                                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                    )

                                    if (showValues) {
                                        val formatted = "BDT ${(day.income).toInt()}"
                                        drawContext.canvas.nativeCanvas.drawText(
                                            formatted,
                                            centerX,
                                            barTop - 4.dp.toPx(),
                                            paint
                                        )
                                    }
                                }
                            }
                            DailySummaryMode.BOTH -> {
                                val barW = (slotWidth * 0.32f).coerceIn(8.dp.toPx(), 18.dp.toPx())
                                val gap = 2.dp.toPx()

                                // Income bar on left
                                if (day.income > 0) {
                                    val incH = (day.income / maxAmount * chartHeight).toFloat().coerceAtLeast(4.dp.toPx())
                                    val incLeft = centerX - barW - gap / 2f
                                    val incTop = baseBottom - incH

                                    drawRoundRect(
                                        color = incomeColor,
                                        topLeft = Offset(incLeft, incTop),
                                        size = Size(barW, incH),
                                        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                                    )
                                }

                                // Expense bar on right
                                if (day.expense > 0) {
                                    val expH = (day.expense / maxAmount * chartHeight).toFloat().coerceAtLeast(4.dp.toPx())
                                    val expLeft = centerX + gap / 2f
                                    val expTop = baseBottom - expH

                                    drawRoundRect(
                                        color = expenseColor,
                                        topLeft = Offset(expLeft, expTop),
                                        size = Size(barW, expH),
                                        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                                    )
                                }

                                if (showValues && (day.income > 0 || day.expense > 0)) {
                                    val maxH = max(
                                        if (day.income > 0) (day.income / maxAmount * chartHeight).toFloat() else 0f,
                                        if (day.expense > 0) (day.expense / maxAmount * chartHeight).toFloat() else 0f
                                    )
                                    val net = day.income - day.expense
                                    val formatted = if (net >= 0) "+${net.toInt()}" else "${net.toInt()}"
                                    drawContext.canvas.nativeCanvas.drawText(
                                        formatted,
                                        centerX,
                                        baseBottom - maxH - 4.dp.toPx(),
                                        paint
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bottom Section: Mode label and Averages
            if (showAverages) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = when (mode) {
                            DailySummaryMode.EXPENSE -> "Expense"
                            DailySummaryMode.INCOME -> "Income"
                            DailySummaryMode.BOTH -> "Income & Expense"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "7 days average",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        val prefix7 = if (mode == DailySummaryMode.EXPENSE) "-BDT " else if (mode == DailySummaryMode.INCOME) "BDT " else if (sevenDaysAvg >= 0) "+BDT " else "-BDT "
                        val color7 = if (mode == DailySummaryMode.EXPENSE) Color(0xFFF43F5E) else if (mode == DailySummaryMode.INCOME) Color(0xFF10B981) else if (sevenDaysAvg >= 0) Color(0xFF10B981) else Color(0xFFF43F5E)
                        Text(
                            text = "$prefix7${String.format(Locale.US, "%,.2f", kotlin.math.abs(sevenDaysAvg))}",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = color7
                        )
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "30 days average",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        val prefix30 = if (mode == DailySummaryMode.EXPENSE) "-BDT " else if (mode == DailySummaryMode.INCOME) "BDT " else if (thirtyDaysAvg >= 0) "+BDT " else "-BDT "
                        val color30 = if (mode == DailySummaryMode.EXPENSE) Color(0xFFF43F5E) else if (mode == DailySummaryMode.INCOME) Color(0xFF10B981) else if (thirtyDaysAvg >= 0) Color(0xFF10B981) else Color(0xFFF43F5E)
                        Text(
                            text = "$prefix30${String.format(Locale.US, "%,.2f", kotlin.math.abs(thirtyDaysAvg))}",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = color30
                        )
                    }
                }
            }
        }
    }
}
