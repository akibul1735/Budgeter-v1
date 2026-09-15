package com.example.ui.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import com.example.util.BudgetChartShape
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.util.LanguageHelper
import java.util.Calendar
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

enum class BudgetSummaryViewTab {
    CHART,
    TABLE
}

enum class SummaryDateRange(val labelEn: String, val labelBn: String) {
    THIS_MONTH("This Month", "চলতি মাস"),
    LAST_MONTH("Last Month", "গত মাস"),
    LAST_3_MONTHS("Last 3 Months", "বিগত ৩ মাস"),
    THIS_YEAR("This Year", "চলতি বছর"),
    ALL_TIME("All Time", "সর্বকালীন")
}

enum class SummaryGrouping(val labelEn: String, val labelBn: String) {
    BY_CATEGORY("By Category", "ক্যাটাগরি অনুযায়ী"),
    BY_PARENT_CATEGORY("By Parent Category", "মূল ক্যাটাগরি অনুযায়ী"),
    BY_ACCOUNT("By Account", "অ্যাকাউন্ট অনুযায়ী")
}

private val PALETTE = listOf(
    Color(0xFFF43F5E), // Vivid Rose/Pink
    Color(0xFF3B82F6), // Blue
    Color(0xFF10B981), // Emerald
    Color(0xFFFB923C), // Orange
    Color(0xFFC084FC), // Purple
    Color(0xFF6366F1), // Indigo
    Color(0xFFFBBF24), // Amber
    Color(0xFF14B8A6), // Teal
    Color(0xFFEC4899), // Pink
    Color(0xFF8B5CF6), // Violet
    Color(0xFF06B6D4), // Cyan
    Color(0xFF94A3B8)  // Slate
)

data class SummarySliceItem(
    val id: Long?,
    val name: String,
    val amount: Double,
    val budgetedAmount: Double,
    val percentage: Float,
    val color: Color,
    val transactionCount: Int,
    val transactions: List<TransactionWithDetails>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetSummaryPreviewDialog(
    transactions: List<TransactionWithDetails>,
    allCategories: List<Category>,
    monthlyBudgets: List<MonthlyBudget>,
    accounts: List<Account>,
    languageMode: LanguageMode,
    initialTransactionType: TransactionType = TransactionType.EXPENSE,
    onDismiss: () -> Unit,
    onTransactionClick: ((TransactionWithDetails) -> Unit)? = null
) {
    var selectedTab by remember { mutableStateOf(BudgetSummaryViewTab.CHART) }
    var selectedChartShape by remember { mutableStateOf(BudgetChartShape.DONUT) }
    var showValuesOnBars by remember { mutableStateOf(true) }
    var selectedTxType by remember { mutableStateOf(initialTransactionType) }
    var selectedDateRange by remember { mutableStateOf(SummaryDateRange.THIS_MONTH) }
    var selectedGrouping by remember { mutableStateOf(SummaryGrouping.BY_CATEGORY) }
    var selectedItemKey by remember { mutableStateOf<Long?>(null) }

    var showTxTypeDropdown by remember { mutableStateOf(false) }
    var showDateRangeDropdown by remember { mutableStateOf(false) }
    var showGroupingDropdown by remember { mutableStateOf(false) }

    // Date range filter computation
    val filteredTransactions = remember(transactions, selectedDateRange, selectedTxType) {
        val now = Calendar.getInstance()
        val currentYear = now.get(Calendar.YEAR)
        val currentMonth = now.get(Calendar.MONTH) // 0-based

        val (startEpoch, endEpoch) = when (selectedDateRange) {
            SummaryDateRange.THIS_MONTH -> {
                val c1 = Calendar.getInstance().apply {
                    set(currentYear, currentMonth, 1, 0, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val c2 = Calendar.getInstance().apply {
                    set(currentYear, currentMonth, getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59)
                    set(Calendar.MILLISECOND, 999)
                }
                c1.timeInMillis to c2.timeInMillis
            }
            SummaryDateRange.LAST_MONTH -> {
                val c1 = Calendar.getInstance().apply {
                    add(Calendar.MONTH, -1)
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val c2 = Calendar.getInstance().apply {
                    add(Calendar.MONTH, -1)
                    set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }
                c1.timeInMillis to c2.timeInMillis
            }
            SummaryDateRange.LAST_3_MONTHS -> {
                val c1 = Calendar.getInstance().apply {
                    add(Calendar.MONTH, -2)
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val c2 = Calendar.getInstance().apply {
                    set(currentYear, currentMonth, getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59)
                    set(Calendar.MILLISECOND, 999)
                }
                c1.timeInMillis to c2.timeInMillis
            }
            SummaryDateRange.THIS_YEAR -> {
                val c1 = Calendar.getInstance().apply {
                    set(currentYear, Calendar.JANUARY, 1, 0, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val c2 = Calendar.getInstance().apply {
                    set(currentYear, Calendar.DECEMBER, 31, 23, 59, 59)
                    set(Calendar.MILLISECOND, 999)
                }
                c1.timeInMillis to c2.timeInMillis
            }
            SummaryDateRange.ALL_TIME -> 0L to Long.MAX_VALUE
        }

        transactions.filter {
            it.transaction.dateEpochMs in startEpoch..endEpoch &&
                    it.transaction.type == selectedTxType
        }
    }

    val totalActual = remember(filteredTransactions) {
        filteredTransactions.sumOf { it.transaction.amount }
    }

    // Process slices according to grouping
    val summaryItems = remember(filteredTransactions, selectedGrouping, allCategories, accounts, monthlyBudgets, totalActual) {
        if (filteredTransactions.isEmpty() || totalActual <= 0) {
            emptyList()
        } else {
            when (selectedGrouping) {
                SummaryGrouping.BY_CATEGORY -> {
                    val map = mutableMapOf<Long?, MutableList<TransactionWithDetails>>()
                    filteredTransactions.forEach { tx ->
                        val catId = tx.category?.id
                        map.getOrPut(catId) { mutableListOf() }.add(tx)
                    }

                    val sorted = map.toList().sortedByDescending { it.second.sumOf { tx -> tx.transaction.amount } }
                    sorted.mapIndexed { idx, (catId, txList) ->
                        val cat = allCategories.find { it.id == catId }
                        val name = cat?.localizedName(languageMode) ?: (if (catId == null) "Others" else "Uncategorized")
                        val amt = txList.sumOf { it.transaction.amount }
                        val pct = if (totalActual > 0) (amt / totalActual * 100).toFloat() else 0f
                        val bgt = monthlyBudgets.find { it.itemId == catId && it.isEnabled }?.budgetedAmount ?: 0.0
                        SummarySliceItem(
                            id = catId,
                            name = name,
                            amount = amt,
                            budgetedAmount = bgt,
                            percentage = pct,
                            color = PALETTE[idx % PALETTE.size],
                            transactionCount = txList.size,
                            transactions = txList
                        )
                    }
                }
                SummaryGrouping.BY_PARENT_CATEGORY -> {
                    val map = mutableMapOf<String, MutableList<TransactionWithDetails>>()
                    filteredTransactions.forEach { tx ->
                        val parentCat = if (tx.category?.parentId != null) allCategories.find { it.id == tx.category.parentId } else tx.category
                        val parent = parentCat?.localizedName(languageMode) ?: "General"
                        map.getOrPut(parent) { mutableListOf() }.add(tx)
                    }
                    val sorted = map.toList().sortedByDescending { it.second.sumOf { tx -> tx.transaction.amount } }
                    sorted.mapIndexed { idx, (parentName, txList) ->
                        val amt = txList.sumOf { it.transaction.amount }
                        val pct = if (totalActual > 0) (amt / totalActual * 100).toFloat() else 0f
                        SummarySliceItem(
                            id = parentName.hashCode().toLong(),
                            name = parentName,
                            amount = amt,
                            budgetedAmount = 0.0,
                            percentage = pct,
                            color = PALETTE[idx % PALETTE.size],
                            transactionCount = txList.size,
                            transactions = txList
                        )
                    }
                }
                SummaryGrouping.BY_ACCOUNT -> {
                    val map = mutableMapOf<Long?, MutableList<TransactionWithDetails>>()
                    filteredTransactions.forEach { tx ->
                        val accId = if (selectedTxType == TransactionType.EXPENSE) tx.transaction.creditAccountId else tx.transaction.debitAccountId
                        map.getOrPut(accId) { mutableListOf() }.add(tx)
                    }
                    val sorted = map.toList().sortedByDescending { it.second.sumOf { tx -> tx.transaction.amount } }
                    sorted.mapIndexed { idx, (accId, txList) ->
                        val acc = accounts.find { it.id == accId }
                        val name = acc?.localizedName(languageMode) ?: "Account"
                        val amt = txList.sumOf { it.transaction.amount }
                        val pct = if (totalActual > 0) (amt / totalActual * 100).toFloat() else 0f
                        SummarySliceItem(
                            id = accId,
                            name = name,
                            amount = amt,
                            budgetedAmount = 0.0,
                            percentage = pct,
                            color = PALETTE[idx % PALETTE.size],
                            transactionCount = txList.size,
                            transactions = txList
                        )
                    }
                }
            }
        }
    }

    // Selected item for banner header
    val activeSelectedItem = remember(summaryItems, selectedItemKey) {
        if (selectedItemKey == null) null else summaryItems.find { it.id == selectedItemKey }
    }

    val displayActual = activeSelectedItem?.amount ?: totalActual
    val displayBudget = activeSelectedItem?.budgetedAmount ?: monthlyBudgets.filter { it.isEnabled }.sumOf { it.budgetedAmount }
    val displayTitle = activeSelectedItem?.name ?: if (languageMode == LanguageMode.BANGLA) "সব ক্যাটাগরি" else "All Categories"
    val displayColor = activeSelectedItem?.color ?: MaterialTheme.colorScheme.primary

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("budget_summary_preview_screen"),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
            ) {
                // Top App Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = LanguageHelper.getString("budget_summary", languageMode),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { /* Quick filter toggle */ }) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Filter",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { /* Print / Export report */ }) {
                            Icon(
                                imageVector = Icons.Default.Print,
                                contentDescription = "Print / Export",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    // Highlight Banner (Matching screenshot)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        // Left Category Pill
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = displayColor.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, displayColor.copy(alpha = 0.6f)),
                            modifier = Modifier
                                .clickable { selectedItemKey = null }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(displayColor)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = displayTitle,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (selectedItemKey != null) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear Selection",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }

                        // Right Actual vs Budget stats
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Actual: ${LanguageHelper.formatCurrency(displayActual, languageMode)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Budget: ${LanguageHelper.formatCurrency(displayBudget, languageMode)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // View Mode: CHART or TABLE
                    if (selectedTab == BudgetSummaryViewTab.CHART) {
                        // Chart Type Selector & Values Toggle Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Chart Type Selector Chips
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .padding(2.dp),
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                BudgetChartShape.values().forEach { shape ->
                                    val isSelected = selectedChartShape == shape
                                    val icon = when (shape) {
                                        BudgetChartShape.DONUT -> Icons.Default.DonutLarge
                                        BudgetChartShape.PIE -> Icons.Default.PieChart
                                        BudgetChartShape.BAR -> Icons.Default.FormatAlignLeft
                                        BudgetChartShape.VERTICAL_BAR -> Icons.Default.BarChart
                                        BudgetChartShape.BUDGET_VS_ACTUAL -> Icons.Default.CompareArrows
                                        BudgetChartShape.STACKED -> Icons.Default.StackedBarChart
                                    }
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                            .clickable { selectedChartShape = shape }
                                            .padding(vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = shape.getLabel(languageMode),
                                            modifier = Modifier.size(16.dp),
                                            tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Values on Bars / Slices Toggle
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (showValuesOnBars) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = BorderStroke(0.5.dp, if (showValuesOnBars) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                modifier = Modifier.clickable { showValuesOnBars = !showValuesOnBars }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (showValuesOnBars) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null,
                                        tint = if (showValuesOnBars) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "মান" else "Values",
                                        fontSize = 10.5.sp,
                                        fontWeight = if (showValuesOnBars) FontWeight.Bold else FontWeight.Normal,
                                        color = if (showValuesOnBars) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }

                        if (summaryItems.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = LanguageHelper.getString("no_transactions", languageMode),
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        } else {
                            when (selectedChartShape) {
                                BudgetChartShape.DONUT, BudgetChartShape.PIE -> {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Interactive Donut / Pie Canvas
                                        Box(
                                            modifier = Modifier
                                                .size(190.dp)
                                                .padding(4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            InteractivePieChart(
                                                items = summaryItems,
                                                selectedId = selectedItemKey,
                                                isDonut = selectedChartShape == BudgetChartShape.DONUT,
                                                showPercentages = showValuesOnBars,
                                                languageMode = languageMode,
                                                onSliceClick = { item ->
                                                    selectedItemKey = if (selectedItemKey == item.id) null else item.id
                                                }
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        // Side Legend List
                                        LazyColumn(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(200.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            items(summaryItems) { item ->
                                                val isSelected = selectedItemKey == item.id
                                                Surface(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .clickable {
                                                            selectedItemKey = if (isSelected) null else item.id
                                                        },
                                                    color = if (isSelected) item.color.copy(alpha = 0.2f) else Color.Transparent,
                                                    border = if (isSelected) BorderStroke(1.dp, item.color) else null
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(9.dp)
                                                                .clip(CircleShape)
                                                                .background(item.color)
                                                        )
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text(
                                                            text = item.name,
                                                            fontSize = 11.5.sp,
                                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                            color = MaterialTheme.colorScheme.onSurface,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis,
                                                            modifier = Modifier.weight(1f)
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(
                                                            text = "${item.percentage.toInt()}%",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                BudgetChartShape.BAR -> {
                                    // Horizontal Ranked Bar Chart
                                    LazyColumn(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(220.dp)
                                            .padding(horizontal = 16.dp, vertical = 4.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        items(summaryItems) { item ->
                                            val isSelected = selectedItemKey == item.id
                                            Surface(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .clickable {
                                                        selectedItemKey = if (isSelected) null else item.id
                                                    },
                                                color = if (isSelected) item.color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                                border = if (isSelected) BorderStroke(1.dp, item.color) else null
                                            ) {
                                                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            modifier = Modifier.weight(1f)
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
                                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                                color = MaterialTheme.colorScheme.onSurface,
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis
                                                            )
                                                        }
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Text(
                                                                text = LanguageHelper.formatCurrency(item.amount, languageMode),
                                                                fontSize = 12.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = MaterialTheme.colorScheme.onSurface
                                                            )
                                                            Spacer(modifier = Modifier.width(6.dp))
                                                            Surface(
                                                                shape = RoundedCornerShape(6.dp),
                                                                color = item.color.copy(alpha = 0.2f)
                                                            ) {
                                                                Text(
                                                                    text = "${String.format(Locale.US, "%.1f", item.percentage)}%",
                                                                    fontSize = 10.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = item.color,
                                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                                                )
                                                            }
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.height(5.dp))
                                                    // Bar Track & Fill
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .height(8.dp)
                                                            .clip(RoundedCornerShape(4.dp))
                                                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxWidth((item.percentage / 100f).coerceIn(0.02f, 1f))
                                                                .fillMaxHeight()
                                                                .clip(RoundedCornerShape(4.dp))
                                                                .background(item.color)
                                                        )
                                                    }
                                                    if (showValuesOnBars && item.budgetedAmount > 0) {
                                                        Spacer(modifier = Modifier.height(2.dp))
                                                        Text(
                                                            text = "Budget: ${LanguageHelper.formatCurrency(item.budgetedAmount, languageMode)} • ${item.transactionCount} txs",
                                                            fontSize = 9.5.sp,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                BudgetChartShape.VERTICAL_BAR -> {
                                    // Vertical Bar Canvas
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(220.dp)
                                            .padding(horizontal = 16.dp, vertical = 4.dp)
                                    ) {
                                        VerticalBarSummaryChart(
                                            items = summaryItems,
                                            selectedId = selectedItemKey,
                                            showValues = showValuesOnBars,
                                            languageMode = languageMode,
                                            onItemClick = { item ->
                                                selectedItemKey = if (selectedItemKey == item.id) null else item.id
                                            }
                                        )
                                    }
                                }

                                BudgetChartShape.BUDGET_VS_ACTUAL -> {
                                    // Budget vs Actual Comparison Dual Bars
                                    LazyColumn(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(220.dp)
                                            .padding(horizontal = 16.dp, vertical = 4.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(summaryItems) { item ->
                                            val isSelected = selectedItemKey == item.id
                                            val bgt = item.budgetedAmount
                                            val hasBudget = bgt > 0
                                            val overBudget = hasBudget && item.amount > bgt
                                            val remaining = bgt - item.amount

                                            Surface(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .clickable {
                                                        selectedItemKey = if (isSelected) null else item.id
                                                    },
                                                color = if (isSelected) item.color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                                border = if (isSelected) BorderStroke(1.dp, item.color) else null
                                            ) {
                                                Column(modifier = Modifier.padding(10.dp)) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            modifier = Modifier.weight(1f)
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
                                                                fontWeight = FontWeight.Bold,
                                                                color = MaterialTheme.colorScheme.onSurface,
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis
                                                            )
                                                        }

                                                        if (hasBudget) {
                                                            Surface(
                                                                shape = RoundedCornerShape(6.dp),
                                                                color = if (overBudget) SolidExpenseContainer.copy(alpha = 0.8f) else SolidIncomeContainer.copy(alpha = 0.8f)
                                                            ) {
                                                                Text(
                                                                    text = if (overBudget) "Over: -${LanguageHelper.formatCurrency(item.amount - bgt, languageMode)}"
                                                                    else "Left: ${LanguageHelper.formatCurrency(remaining, languageMode)}",
                                                                    fontSize = 10.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = if (overBudget) SolidExpense else SolidIncome,
                                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                                )
                                                            }
                                                        }
                                                    }

                                                    Spacer(modifier = Modifier.height(6.dp))

                                                    // Spent Bar
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Text(
                                                            text = "Spent",
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Medium,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                            modifier = Modifier.width(42.dp)
                                                        )
                                                        Box(
                                                            modifier = Modifier
                                                                .weight(1f)
                                                                .height(7.dp)
                                                                .clip(RoundedCornerShape(3.dp))
                                                                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                                        ) {
                                                            val maxBase = if (hasBudget) Math.max(item.amount, bgt) else item.amount
                                                            val fillRatio = if (maxBase > 0) (item.amount / maxBase).toFloat().coerceIn(0.03f, 1f) else 0f
                                                            Box(
                                                                modifier = Modifier
                                                                    .fillMaxWidth(fillRatio)
                                                                    .fillMaxHeight()
                                                                    .clip(RoundedCornerShape(3.dp))
                                                                    .background(if (overBudget) SolidExpense else item.color)
                                                            )
                                                        }
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(
                                                            text = LanguageHelper.formatCurrency(item.amount, languageMode),
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                    }

                                                    // Budget Limit Bar (if budgeted)
                                                    if (hasBudget) {
                                                        Spacer(modifier = Modifier.height(3.dp))
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            modifier = Modifier.fillMaxWidth()
                                                        ) {
                                                            Text(
                                                                text = "Budget",
                                                                fontSize = 10.sp,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                                modifier = Modifier.width(42.dp)
                                                            )
                                                            Box(
                                                                modifier = Modifier
                                                                    .weight(1f)
                                                                    .height(7.dp)
                                                                    .clip(RoundedCornerShape(3.dp))
                                                                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                                            ) {
                                                                val maxBase = Math.max(item.amount, bgt)
                                                                val fillRatio = if (maxBase > 0) (bgt / maxBase).toFloat().coerceIn(0.03f, 1f) else 0f
                                                                Box(
                                                                    modifier = Modifier
                                                                        .fillMaxWidth(fillRatio)
                                                                        .fillMaxHeight()
                                                                        .clip(RoundedCornerShape(3.dp))
                                                                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
                                                                )
                                                            }
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            Text(
                                                                text = LanguageHelper.formatCurrency(bgt, languageMode),
                                                                fontSize = 10.5.sp,
                                                                fontWeight = FontWeight.Medium,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                BudgetChartShape.STACKED -> {
                                    // Stacked Proportion Bar + Legend
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(220.dp)
                                            .padding(horizontal = 16.dp, vertical = 6.dp)
                                    ) {
                                        // Proportion Segmented Bar
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(26.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                        ) {
                                            summaryItems.forEach { item ->
                                                val weight = (item.percentage / 100f).coerceAtLeast(0.01f)
                                                val isSelected = selectedItemKey == item.id
                                                Box(
                                                    modifier = Modifier
                                                        .weight(weight)
                                                        .fillMaxHeight()
                                                        .background(if (isSelected) item.color else item.color.copy(alpha = 0.85f))
                                                        .clickable {
                                                            selectedItemKey = if (isSelected) null else item.id
                                                        },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    if (showValuesOnBars && item.percentage >= 10f) {
                                                        Text(
                                                            text = "${item.percentage.toInt()}%",
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color.White
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        // Legend items grid/list
                                        LazyColumn(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(5.dp)
                                        ) {
                                            items(summaryItems) { item ->
                                                val isSelected = selectedItemKey == item.id
                                                Surface(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .clickable {
                                                            selectedItemKey = if (isSelected) null else item.id
                                                        },
                                                    color = if (isSelected) item.color.copy(alpha = 0.2f) else Color.Transparent
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                                        verticalAlignment = Alignment.CenterVertically
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
                                                            fontSize = 11.5.sp,
                                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                            color = MaterialTheme.colorScheme.onSurface,
                                                            modifier = Modifier.weight(1f)
                                                        )
                                                        Text(
                                                            text = LanguageHelper.formatCurrency(item.amount, languageMode),
                                                            fontSize = 11.5.sp,
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text(
                                                            text = "(${item.percentage.toInt()}%)",
                                                            fontSize = 11.sp,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // TABLE VIEW
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Table Header
                            item {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Category",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.weight(1.3f)
                                        )
                                        Text(
                                            text = "Actual",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.End,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = "Budget",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.End,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = "%",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.End,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.weight(0.6f)
                                        )
                                    }
                                }
                            }

                            items(summaryItems) { item ->
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (selectedItemKey == item.id) item.color.copy(alpha = 0.15f) else Color.Transparent,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedItemKey = if (selectedItemKey == item.id) null else item.id
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1.3f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(7.dp)
                                                    .clip(CircleShape)
                                                    .background(item.color)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = item.name,
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Text(
                                            text = LanguageHelper.formatCurrency(item.amount, languageMode),
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            textAlign = TextAlign.End,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = if (item.budgetedAmount > 0) LanguageHelper.formatCurrency(item.budgetedAmount, languageMode) else "-",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Normal,
                                            textAlign = TextAlign.End,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = "${item.percentage.toInt()}%",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.End,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(0.6f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 3 Filter Dropdown Boxes (Matching screenshot style)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // 1. Transaction Type Dropdown
                        Column {
                            Text(
                                text = "Transaction Type",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { showTxTypeDropdown = true },
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = when (selectedTxType) {
                                                TransactionType.EXPENSE -> "Expense"
                                                TransactionType.INCOME -> "Income"
                                                TransactionType.TRANSFER -> "Transfer"
                                            },
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                DropdownMenu(
                                    expanded = showTxTypeDropdown,
                                    onDismissRequest = { showTxTypeDropdown = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Expense", fontWeight = if (selectedTxType == TransactionType.EXPENSE) FontWeight.Bold else FontWeight.Normal) },
                                        onClick = {
                                            selectedTxType = TransactionType.EXPENSE
                                            selectedItemKey = null
                                            showTxTypeDropdown = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Income", fontWeight = if (selectedTxType == TransactionType.INCOME) FontWeight.Bold else FontWeight.Normal) },
                                        onClick = {
                                            selectedTxType = TransactionType.INCOME
                                            selectedItemKey = null
                                            showTxTypeDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        // 2. Date Range Dropdown
                        Column {
                            Text(
                                text = "Date Range",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { showDateRangeDropdown = true },
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (languageMode == LanguageMode.BANGLA) selectedDateRange.labelBn else selectedDateRange.labelEn,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                DropdownMenu(
                                    expanded = showDateRangeDropdown,
                                    onDismissRequest = { showDateRangeDropdown = false }
                                ) {
                                    SummaryDateRange.values().forEach { range ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = if (languageMode == LanguageMode.BANGLA) range.labelBn else range.labelEn,
                                                    fontWeight = if (selectedDateRange == range) FontWeight.Bold else FontWeight.Normal
                                                )
                                            },
                                            onClick = {
                                                selectedDateRange = range
                                                selectedItemKey = null
                                                showDateRangeDropdown = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // 3. Chart Type / Grouping Dropdown
                        Column {
                            Text(
                                text = "Chart Type",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { showGroupingDropdown = true },
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (languageMode == LanguageMode.BANGLA) selectedGrouping.labelBn else selectedGrouping.labelEn,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                DropdownMenu(
                                    expanded = showGroupingDropdown,
                                    onDismissRequest = { showGroupingDropdown = false }
                                ) {
                                    SummaryGrouping.values().forEach { grouping ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = if (languageMode == LanguageMode.BANGLA) grouping.labelBn else grouping.labelEn,
                                                    fontWeight = if (selectedGrouping == grouping) FontWeight.Bold else FontWeight.Normal
                                                )
                                            },
                                            onClick = {
                                                selectedGrouping = grouping
                                                selectedItemKey = null
                                                showGroupingDropdown = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Bottom Tab Bar (Chart / Table) matching screenshot
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Chart Tab
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedTab = BudgetSummaryViewTab.CHART }
                                .padding(horizontal = 24.dp, vertical = 4.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (selectedTab == BudgetSummaryViewTab.CHART) Color(0xFF2E7D32) else Color.Transparent,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BarChart,
                                    contentDescription = "Chart",
                                    tint = if (selectedTab == BudgetSummaryViewTab.CHART) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp).size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Chart",
                                fontSize = 11.sp,
                                fontWeight = if (selectedTab == BudgetSummaryViewTab.CHART) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == BudgetSummaryViewTab.CHART) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Table Tab
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedTab = BudgetSummaryViewTab.TABLE }
                                .padding(horizontal = 24.dp, vertical = 4.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (selectedTab == BudgetSummaryViewTab.TABLE) Color(0xFF2E7D32) else Color.Transparent,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TableChart,
                                    contentDescription = "Table",
                                    tint = if (selectedTab == BudgetSummaryViewTab.TABLE) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp).size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Table",
                                fontSize = 11.sp,
                                fontWeight = if (selectedTab == BudgetSummaryViewTab.TABLE) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == BudgetSummaryViewTab.TABLE) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Interactive Donut & Pie Chart with slice labels, percentages on slices, and click hit testing.
 */
@Composable
private fun InteractivePieChart(
    items: List<SummarySliceItem>,
    selectedId: Long?,
    isDonut: Boolean = true,
    showPercentages: Boolean = true,
    languageMode: LanguageMode = LanguageMode.ENGLISH,
    onSliceClick: (SummarySliceItem) -> Unit
) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val totalAmount = remember(items) { items.sumOf { it.amount } }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(items) {
                detectTapGestures { tapOffset ->
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val dx = tapOffset.x - center.x
                    val dy = tapOffset.y - center.y
                    val radius = size.width.coerceAtMost(size.height) / 2f
                    val touchDist = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()

                    val innerCutoutRadius = if (isDonut) radius * 0.55f else 0f
                    if (touchDist in innerCutoutRadius..radius + 15f) {
                        var angle = Math.toDegrees(Math.atan2(dy.toDouble(), dx.toDouble())).toFloat()
                        if (angle < 0) angle += 360f

                        var angleFromStart = angle - (-90f)
                        if (angleFromStart < 0) angleFromStart += 360f
                        if (angleFromStart >= 360f) angleFromStart -= 360f

                        var runningAngle = 0f
                        for (item in items) {
                            val sweep = (item.percentage / 100f) * 360f
                            if (angleFromStart >= runningAngle && angleFromStart <= runningAngle + sweep) {
                                onSliceClick(item)
                                break
                            }
                            runningAngle += sweep
                        }
                    }
                }
            }
    ) {
        val diameter = size.minDimension
        val radius = diameter / 2f
        val center = Offset(size.width / 2f, size.height / 2f)
        val innerRadius = if (isDonut) radius * 0.55f else 0f

        var startAngle = -90f

        val sliceTextPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 9.sp.toPx()
            textAlign = android.graphics.Paint.Align.CENTER
            isFakeBoldText = true
            isAntiAlias = true
            setShadowLayer(3f, 0f, 0f, android.graphics.Color.BLACK)
        }

        items.forEach { item ->
            val sweepAngle = (item.percentage / 100f) * 360f
            val isSelected = selectedId == item.id
            val extraRadius = if (isSelected) 8f else 0f

            drawArc(
                color = item.color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                topLeft = Offset(center.x - radius - extraRadius, center.y - radius - extraRadius),
                size = Size((radius + extraRadius) * 2, (radius + extraRadius) * 2),
                style = Fill
            )

            // Draw white border separator between slices
            drawArc(
                color = Color.White.copy(alpha = 0.4f),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                topLeft = Offset(center.x - radius - extraRadius, center.y - radius - extraRadius),
                size = Size((radius + extraRadius) * 2, (radius + extraRadius) * 2),
                style = Stroke(width = 1.5f)
            )

            // Draw percentage on slice if broad enough
            if (showPercentages && item.percentage >= 8f) {
                val midAngle = Math.toRadians((startAngle + sweepAngle / 2f).toDouble())
                val labelDistance = if (isDonut) (radius + innerRadius) / 2f else radius * 0.65f
                val lx = center.x + (labelDistance * cos(midAngle)).toFloat()
                val ly = center.y + (labelDistance * sin(midAngle)).toFloat() + 3.dp.toPx()

                drawContext.canvas.nativeCanvas.drawText(
                    "${item.percentage.toInt()}%",
                    lx,
                    ly,
                    sliceTextPaint
                )
            }

            startAngle += sweepAngle
        }

        // Cut out center hole if Donut
        if (isDonut) {
            val holeColor = if (isDark) Color(0xFF1E1E1E) else Color.White
            drawCircle(
                color = holeColor,
                radius = innerRadius,
                center = center
            )

            // Draw center text
            val centerHeaderPaint = android.graphics.Paint().apply {
                color = if (isDark) android.graphics.Color.LTGRAY else android.graphics.Color.GRAY
                textSize = 9.sp.toPx()
                textAlign = android.graphics.Paint.Align.CENTER
                isAntiAlias = true
            }
            val centerTotalPaint = android.graphics.Paint().apply {
                color = if (isDark) android.graphics.Color.WHITE else android.graphics.Color.BLACK
                textSize = 10.sp.toPx()
                textAlign = android.graphics.Paint.Align.CENTER
                isFakeBoldText = true
                isAntiAlias = true
            }

            drawContext.canvas.nativeCanvas.drawText(
                if (languageMode == LanguageMode.BANGLA) "মোট" else "Total",
                center.x,
                center.y - 4.dp.toPx(),
                centerHeaderPaint
            )
            drawContext.canvas.nativeCanvas.drawText(
                LanguageHelper.formatCurrency(totalAmount, languageMode),
                center.x,
                center.y + 11.dp.toPx(),
                centerTotalPaint
            )
        }
    }
}

/**
 * Vertical Bar Chart for Budget Summary Breakdown.
 */
@Composable
private fun VerticalBarSummaryChart(
    items: List<SummarySliceItem>,
    selectedId: Long?,
    showValues: Boolean,
    languageMode: LanguageMode,
    onItemClick: (SummarySliceItem) -> Unit
) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val peakAmount = remember(items) { (items.maxOfOrNull { it.amount } ?: 100.0).coerceAtLeast(10.0) }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(items) {
                detectTapGestures { offset ->
                    if (items.isNotEmpty()) {
                        val yAxisW = 32.dp.toPx()
                        val chartW = size.width - yAxisW
                        if (offset.x >= yAxisW) {
                            val slotW = chartW / items.size
                            val idx = ((offset.x - yAxisW) / slotW).toInt().coerceIn(0, items.size - 1)
                            onItemClick(items[idx])
                        }
                    }
                }
            }
    ) {
        val totalWidth = size.width
        val totalHeight = size.height
        val yAxisWidth = 32.dp.toPx()
        val bottomAxisH = 28.dp.toPx()
        val topPadding = 18.dp.toPx()
        val chartAreaH = totalHeight - bottomAxisH - topPadding
        val chartAreaW = totalWidth - yAxisWidth
        val baselineY = totalHeight - bottomAxisH

        val count = items.size.coerceAtLeast(1)
        val slotW = chartAreaW / count
        val barW = (slotW * 0.65f).coerceIn(12.dp.toPx(), 36.dp.toPx())

        val valuePaint = android.graphics.Paint().apply {
            color = if (isDark) android.graphics.Color.WHITE else android.graphics.Color.DKGRAY
            textSize = 8.sp.toPx()
            textAlign = android.graphics.Paint.Align.CENTER
            isFakeBoldText = true
            isAntiAlias = true
        }

        val labelPaint = android.graphics.Paint().apply {
            color = if (isDark) android.graphics.Color.LTGRAY else android.graphics.Color.GRAY
            textSize = 8.5.sp.toPx()
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }

        // Draw baseline
        drawLine(
            color = if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.15f),
            start = Offset(yAxisWidth, baselineY),
            end = Offset(totalWidth, baselineY),
            strokeWidth = 1.dp.toPx()
        )

        items.forEachIndexed { i, item ->
            val isSelected = selectedId == item.id
            val ratio = (item.amount / peakAmount).toFloat().coerceIn(0.02f, 1f)
            val barH = ratio * chartAreaH
            val centerX = yAxisWidth + (i * slotW) + (slotW / 2f)
            val barTop = baselineY - barH

            // Draw Bar with rounded top
            drawRoundRect(
                color = if (isSelected) item.color else item.color.copy(alpha = 0.85f),
                topLeft = Offset(centerX - barW / 2f, barTop),
                size = Size(barW, barH),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )

            if (isSelected) {
                drawRoundRect(
                    color = Color.White,
                    topLeft = Offset(centerX - barW / 2f, barTop),
                    size = Size(barW, barH),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }

            // Value text on top of bar
            if (showValues) {
                val valStr = "${item.percentage.toInt()}%"
                drawContext.canvas.nativeCanvas.drawText(
                    valStr,
                    centerX,
                    barTop - 4.dp.toPx(),
                    valuePaint
                )
            }

            // Label text below baseline
            val displayShort = if (item.name.length > 5) item.name.take(4) + ".." else item.name
            drawContext.canvas.nativeCanvas.drawText(
                displayShort,
                centerX,
                baselineY + 14.dp.toPx(),
                labelPaint
            )
        }
    }
}
