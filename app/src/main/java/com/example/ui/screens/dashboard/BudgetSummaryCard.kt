package com.example.ui.screens.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Category
import com.example.data.model.LanguageMode
import com.example.data.model.MonthlyBudget
import com.example.data.model.TransactionType
import com.example.data.model.TransactionWithDetails
import com.example.util.BudgetChartShape
import com.example.util.BudgetSummaryType
import com.example.util.LanguageHelper
import java.util.Calendar
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

data class CategorySpendItem(
    val categoryId: Long?,
    val name: String,
    val amount: Double,
    val percentage: Float,
    val color: Color
)

private val CHART_COLORS = listOf(
    Color(0xFFF43F5E), // Vivid Pink/Red (matching screenshot dominant slice)
    Color(0xFF38BDF8), // Light Sky Blue
    Color(0xFF10B981), // Emerald Green
    Color(0xFFFB923C), // Warm Orange
    Color(0xFFC084FC), // Lavender / Purple
    Color(0xFF6366F1), // Indigo
    Color(0xFFFBBF24), // Amber / Yellow
    Color(0xFF14B8A6), // Teal
    Color(0xFFEC4899), // Pink
    Color(0xFF94A3B8)  // Slate (Others)
)

@Composable
fun BudgetSummaryCard(
    transactions: List<TransactionWithDetails>,
    allCategories: List<Category>,
    monthlyBudgets: List<MonthlyBudget>,
    chartShape: BudgetChartShape,
    categoryType: BudgetSummaryType,
    maxCategories: Int,
    showPercentages: Boolean,
    showTodayPace: Boolean,
    languageMode: LanguageMode,
    onCategoryTypeChange: (BudgetSummaryType) -> Unit,
    onOpenSettings: () -> Unit
) {
    var showDropdownMenu by remember { mutableStateOf(false) }

    // Current month bounds
    val calendar = Calendar.getInstance()
    val currentDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
    val maxDaysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val todayPaceRatio = (currentDayOfMonth.toFloat() / maxDaysInMonth.toFloat()).coerceIn(0f, 1f)

    val currentYear = calendar.get(Calendar.YEAR)
    val currentMonth = calendar.get(Calendar.MONTH) + 1

    // Filter transactions for current month
    val monthStartEpoch = remember(currentYear, currentMonth) {
        val c = Calendar.getInstance()
        c.set(currentYear, currentMonth - 1, 1, 0, 0, 0)
        c.set(Calendar.MILLISECOND, 0)
        c.timeInMillis
    }
    val monthEndEpoch = remember(currentYear, currentMonth) {
        val c = Calendar.getInstance()
        c.set(currentYear, currentMonth - 1, maxDaysInMonth, 23, 59, 59)
        c.set(Calendar.MILLISECOND, 999)
        c.timeInMillis
    }

    val currentMonthTxs = remember(transactions, monthStartEpoch, monthEndEpoch) {
        transactions.filter { it.transaction.dateEpochMs in monthStartEpoch..monthEndEpoch }
    }

    // Target transaction types based on selected category type
    val filteredTxs = remember(currentMonthTxs, categoryType) {
        when (categoryType) {
            BudgetSummaryType.EXPENSE -> currentMonthTxs.filter { it.transaction.type == TransactionType.EXPENSE }
            BudgetSummaryType.INCOME -> currentMonthTxs.filter { it.transaction.type == TransactionType.INCOME }
            BudgetSummaryType.ALL -> currentMonthTxs.filter { it.transaction.type != TransactionType.TRANSFER }
        }
    }

    val totalSpent = remember(filteredTxs) { filteredTxs.sumOf { it.transaction.amount } }

    // Total Budget calculation for this month
    val totalBudgetAmount = remember(monthlyBudgets, categoryType) {
        val relevantBudgets = if (categoryType == BudgetSummaryType.EXPENSE) {
            monthlyBudgets.filter { it.isEnabled }
        } else {
            monthlyBudgets.filter { it.isEnabled }
        }
        val sum = relevantBudgets.sumOf { it.budgetedAmount }
        if (sum > 0) sum else 5000.0 // Default baseline if not budgeted yet
    }

    // Compute category breakdown
    val categoryItems = remember(filteredTxs, allCategories, maxCategories, totalSpent, languageMode) {
        if (totalSpent <= 0) {
            emptyList()
        } else {
            val groupMap = mutableMapOf<Long?, Double>()
            filteredTxs.forEach { tx ->
                val catId = tx.category?.id
                groupMap[catId] = (groupMap[catId] ?: 0.0) + tx.transaction.amount
            }

            val sorted = groupMap.toList().sortedByDescending { it.second }
            val topList = sorted.take(maxCategories)
            val remainder = sorted.drop(maxCategories).sumOf { it.second }

            val result = mutableListOf<CategorySpendItem>()
            topList.forEachIndexed { index, (catId, amt) ->
                val cat = allCategories.find { it.id == catId }
                val name = cat?.localizedName(languageMode) ?: (if (catId == null) "Others" else "Category")
                val pct = (amt / totalSpent * 100f).toFloat()
                result.add(
                    CategorySpendItem(
                        categoryId = catId,
                        name = name,
                        amount = amt,
                        percentage = pct,
                        color = CHART_COLORS[index % CHART_COLORS.size]
                    )
                )
            }

            if (remainder > 0) {
                val pct = (remainder / totalSpent * 100f).toFloat()
                result.add(
                    CategorySpendItem(
                        categoryId = null,
                        name = "Others",
                        amount = remainder,
                        percentage = pct,
                        color = CHART_COLORS.last()
                    )
                )
            }
            result
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("budget_summary_card"),
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
                Box {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showDropdownMenu = true }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = LanguageHelper.getString("budget_summary", languageMode),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Select Category Type",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showDropdownMenu,
                        onDismissRequest = { showDropdownMenu = false }
                    ) {
                        BudgetSummaryType.values().forEach { t ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = t.getLabel(languageMode),
                                        fontWeight = if (t == categoryType) FontWeight.Bold else FontWeight.Normal,
                                        color = if (t == categoryType) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = {
                                    onCategoryTypeChange(t)
                                    showDropdownMenu = false
                                }
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Budget Summary Chart Settings",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Chart & Legend Row
            if (categoryItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = LanguageHelper.getString("no_transactions", languageMode),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Donut / Pie Canvas Chart on Left
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(140.dp)) {
                            val strokeWidthPx = if (chartShape == BudgetChartShape.DONUT) 36.dp.toPx() else 0f
                            val radius = size.minDimension / 2f
                            val centerOffset = Offset(size.width / 2f, size.height / 2f)

                            var startAngle = -90f
                            val textPaint = android.graphics.Paint().apply {
                                color = android.graphics.Color.WHITE
                                textSize = 10.sp.toPx()
                                textAlign = android.graphics.Paint.Align.CENTER
                                isFakeBoldText = true
                                isAntiAlias = true
                            }

                            categoryItems.forEach { item ->
                                val sweepAngle = (item.percentage / 100f) * 360f
                                if (sweepAngle > 0.5f) {
                                    if (chartShape == BudgetChartShape.DONUT) {
                                        drawArc(
                                            color = item.color,
                                            startAngle = startAngle,
                                            sweepAngle = sweepAngle,
                                            useCenter = false,
                                            topLeft = Offset(centerOffset.x - radius + strokeWidthPx / 2f, centerOffset.y - radius + strokeWidthPx / 2f),
                                            size = Size(radius * 2 - strokeWidthPx, radius * 2 - strokeWidthPx),
                                            style = Stroke(width = strokeWidthPx, cap = StrokeCap.Butt)
                                        )
                                    } else {
                                        drawArc(
                                            color = item.color,
                                            startAngle = startAngle,
                                            sweepAngle = sweepAngle,
                                            useCenter = true,
                                            topLeft = Offset(centerOffset.x - radius, centerOffset.y - radius),
                                            size = Size(radius * 2, radius * 2),
                                            style = Fill
                                        )
                                    }

                                    // Draw percentage inside slice if large enough
                                    if (showPercentages && item.percentage >= 6f) {
                                        val midAngle = Math.toRadians((startAngle + sweepAngle / 2f).toDouble())
                                        val labelRadius = if (chartShape == BudgetChartShape.DONUT) radius - strokeWidthPx / 2f else radius * 0.65f
                                        val labelX = (centerOffset.x + labelRadius * cos(midAngle)).toFloat()
                                        val labelY = (centerOffset.y + labelRadius * sin(midAngle)).toFloat() + 3.dp.toPx()

                                        drawContext.canvas.nativeCanvas.drawText(
                                            "${item.percentage.toInt()}%",
                                            labelX,
                                            labelY,
                                            textPaint
                                        )
                                    }

                                    startAngle += sweepAngle
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Legend on Right
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        categoryItems.forEach { item ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(item.color)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = item.name,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bottom Section: Header and Progress Bar matching screenshot
            val labelText = when (categoryType) {
                BudgetSummaryType.EXPENSE -> "EXPENSE"
                BudgetSummaryType.INCOME -> "INCOME"
                BudgetSummaryType.ALL -> "TOTAL ACTIVITY"
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = labelText,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF10B981) // Green label matching screenshot
                )
                Text(
                    text = "BDT ${String.format(Locale.US, "%,.2f", totalSpent)}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFEA580C) // Amber / Orange total text
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Progress Bar with Percentage and Over/Under text
            val budgetRatio = if (totalBudgetAmount > 0) (totalSpent / totalBudgetAmount).toFloat() else 0f
            val pctInt = (budgetRatio * 100).toInt()
            val diff = totalSpent - totalBudgetAmount

            val progressText = if (diff > 0) {
                "$pctInt%  BDT ${String.format(Locale.US, "%,.2f", diff)} over ${String.format(Locale.US, "%,.0f", totalBudgetAmount)}"
            } else {
                "$pctInt%  BDT ${String.format(Locale.US, "%,.2f", kotlin.math.abs(diff))} remaining of ${String.format(Locale.US, "%,.0f", totalBudgetAmount)}"
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$pctInt%",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (diff > 0) {
                        "BDT ${String.format(Locale.US, "%,.2f", diff)} over ${String.format(Locale.US, "%,.0f", totalBudgetAmount)}"
                    } else {
                        "BDT ${String.format(Locale.US, "%,.2f", kotlin.math.abs(diff))} left of ${String.format(Locale.US, "%,.0f", totalBudgetAmount)}"
                    },
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Custom Progress Bar with TODAY pace indicator
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxWidth().height(28.dp)) {
                    val w = size.width
                    val h = 10.dp.toPx()
                    val barTop = 4.dp.toPx()

                    // Background Track
                    drawRoundRect(
                        color = Color(0xFFE2E8F0),
                        topLeft = Offset(0f, barTop),
                        size = Size(w, h),
                        cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx())
                    )

                    // Fill Bar (Orange / Red gradient)
                    val fillWidth = (w * (budgetRatio.coerceIn(0f, 1f))).coerceAtLeast(6.dp.toPx())
                    val barColor = if (budgetRatio > 1.0f) Color(0xFFEA580C) else Color(0xFFF59E0B)
                    drawRoundRect(
                        color = barColor,
                        topLeft = Offset(0f, barTop),
                        size = Size(fillWidth, h),
                        cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx())
                    )

                    // TODAY Marker
                    if (showTodayPace) {
                        val todayX = w * todayPaceRatio
                        drawLine(
                            color = Color(0xFF10B981), // Emerald vertical line
                            start = Offset(todayX, 0f),
                            end = Offset(todayX, barTop + h + 4.dp.toPx()),
                            strokeWidth = 2.dp.toPx()
                        )

                        val todayPaint = android.graphics.Paint().apply {
                            color = android.graphics.Color.parseColor("#10B981")
                            textSize = 8.5.sp.toPx()
                            isFakeBoldText = true
                            textAlign = android.graphics.Paint.Align.LEFT
                            isAntiAlias = true
                        }

                        drawContext.canvas.nativeCanvas.drawText(
                            "TODAY",
                            (todayX + 4.dp.toPx()).coerceAtMost(w - 36.dp.toPx()),
                            barTop + h + 10.dp.toPx(),
                            todayPaint
                        )
                    }
                }
            }
        }
    }
}
