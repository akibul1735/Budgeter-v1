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
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.util.LanguageHelper
import java.util.Calendar
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
                        // Pie Chart and Side Legend
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
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Interactive Pie Chart Canvas
                                Box(
                                    modifier = Modifier
                                        .size(190.dp)
                                        .padding(4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    InteractivePieChart(
                                        items = summaryItems,
                                        selectedId = selectedItemKey,
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
 * Interactive Pie Chart with slice labels and click hit testing.
 */
@Composable
private fun InteractivePieChart(
    items: List<SummarySliceItem>,
    selectedId: Long?,
    onSliceClick: (SummarySliceItem) -> Unit
) {
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

                    if (touchDist <= radius) {
                        var angle = Math.toDegrees(Math.atan2(dy.toDouble(), dx.toDouble())).toFloat()
                        if (angle < 0) angle += 360f

                        var currentStart = -90f
                        if (currentStart < 0) currentStart += 360f

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

        var startAngle = -90f

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

            startAngle += sweepAngle
        }
    }
}
