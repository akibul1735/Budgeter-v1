package com.example.ui.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.example.util.DateUtils
import com.example.util.LanguageHelper
import java.text.SimpleDateFormat
import java.util.*

enum class DailyPeriodFilter(val labelEn: String, val labelBn: String, val days: Int) {
    LAST_7_DAYS("Last 7 Days", "বিগত ৭ দিন", 7),
    LAST_14_DAYS("Last 14 Days", "বিগত ১৪ দিন", 14),
    THIS_MONTH("This Month", "চলতি মাস", 30),
    LAST_30_DAYS("Last 30 Days", "বিগত ৩০ দিন", 30)
}

data class DayDetailItem(
    val dateEpochMs: Long,
    val dayLabel: String,
    val dateNum: Int,
    val fullDateString: String,
    val totalExpense: Double,
    val totalIncome: Double,
    val transactions: List<TransactionWithDetails>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailySummaryDetailDialog(
    transactions: List<TransactionWithDetails>,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onTransactionClick: ((TransactionWithDetails) -> Unit)? = null
) {
    var selectedPeriod by remember { mutableStateOf(DailyPeriodFilter.THIS_MONTH) }
    var selectedDayKey by remember { mutableStateOf<String?>(null) }
    var showPeriodDropdown by remember { mutableStateOf(false) }

    // Build day items
    val dayItems = remember(transactions, selectedPeriod) {
        val calendar = Calendar.getInstance()
        val now = Calendar.getInstance()
        val daysToInclude = selectedPeriod.days

        val items = mutableListOf<DayDetailItem>()
        val sdf = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())
        val dayNameSdf = SimpleDateFormat("EEE", Locale.getDefault())

        val startCal = Calendar.getInstance().apply {
            if (selectedPeriod == DailyPeriodFilter.THIS_MONTH) {
                set(Calendar.DAY_OF_MONTH, 1)
            } else {
                add(Calendar.DAY_OF_YEAR, -(daysToInclude - 1))
            }
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val endCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }

        val periodTxs = transactions.filter {
            it.transaction.dateEpochMs in startCal.timeInMillis..endCal.timeInMillis &&
                    it.transaction.status != TransactionStatus.VOID
        }

        val tempCal = startCal.clone() as Calendar
        while (!tempCal.after(endCal)) {
            val dCal = tempCal.clone() as Calendar
            val dStart = dCal.apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val dEnd = dCal.apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.timeInMillis

            val dayTxs = periodTxs.filter { it.transaction.dateEpochMs in dStart..dEnd }
            val expense = dayTxs.filter { it.transaction.type == TransactionType.EXPENSE }.sumOf { it.transaction.amount }
            val income = dayTxs.filter { it.transaction.type == TransactionType.INCOME }.sumOf { it.transaction.amount }

            items.add(
                DayDetailItem(
                    dateEpochMs = dStart,
                    dayLabel = dayNameSdf.format(Date(dStart)),
                    dateNum = tempCal.get(Calendar.DAY_OF_MONTH),
                    fullDateString = sdf.format(Date(dStart)),
                    totalExpense = expense,
                    totalIncome = income,
                    transactions = dayTxs
                )
            )

            tempCal.add(Calendar.DAY_OF_YEAR, 1)
        }

        items
    }

    val totalPeriodExpense = remember(dayItems) { dayItems.sumOf { it.totalExpense } }
    val totalPeriodIncome = remember(dayItems) { dayItems.sumOf { it.totalIncome } }
    val activeDaysCount = remember(dayItems) { dayItems.count { it.totalExpense > 0 || it.totalIncome > 0 }.coerceAtLeast(1) }
    val dailyAvgExpense = totalPeriodExpense / dayItems.size.coerceAtLeast(1)
    val maxExpenseDay = remember(dayItems) { dayItems.maxByOrNull { it.totalExpense } }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("daily_summary_detail_screen"),
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
                            text = LanguageHelper.getString("daily_summary", languageMode),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Period Dropdown Selector
                    Box {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.clickable { showPeriodDropdown = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) selectedPeriod.labelBn else selectedPeriod.labelEn,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showPeriodDropdown,
                            onDismissRequest = { showPeriodDropdown = false }
                        ) {
                            DailyPeriodFilter.values().forEach { filter ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = if (languageMode == LanguageMode.BANGLA) filter.labelBn else filter.labelEn,
                                            fontWeight = if (selectedPeriod == filter) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        selectedPeriod = filter
                                        showPeriodDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(vertical = 14.dp)
                ) {
                    // Metric Summary Grid
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Total Expense Card
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                color = SolidExpenseContainer.copy(alpha = 0.7f),
                                border = BorderStroke(1.dp, SolidExpense.copy(alpha = 0.3f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = LanguageHelper.getString("expense", languageMode),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = SolidExpense
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = LanguageHelper.formatCurrency(totalPeriodExpense, languageMode),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SolidOnExpenseContainer
                                    )
                                }
                            }

                            // Daily Average Expense
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "দৈনিক গড় ব্যয়" else "Daily Average",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = LanguageHelper.formatCurrency(dailyAvgExpense, languageMode),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    // Bar Chart Visualization
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "দৈনিক ব্যয়ের চার্ট" else "Daily Expense Trend",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                val maxExp = dayItems.maxOfOrNull { it.totalExpense }?.coerceAtLeast(100.0) ?: 100.0
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(130.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    dayItems.forEach { day ->
                                        val barHeightRatio = (day.totalExpense / maxExp).toFloat().coerceIn(0.04f, 1f)
                                        val isSelected = selectedDayKey == day.fullDateString

                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable {
                                                    selectedDayKey = if (isSelected) null else day.fullDateString
                                                }
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxHeight(barHeightRatio)
                                                    .width(if (dayItems.size > 14) 6.dp else 14.dp)
                                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                                    .background(if (isSelected) SolidPrimary else SolidExpense.copy(alpha = if (day.totalExpense > 0) 0.85f else 0.15f))
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "${day.dateNum}",
                                                fontSize = 9.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) SolidPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Chronological Day-by-Day List
                    item {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "দিনভিত্তিক লেনদেন বিবরণী" else "Day by Day Breakdown",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }

                    items(dayItems.reversed()) { day ->
                        val isExpanded = selectedDayKey == day.fullDateString || day.totalExpense > 0
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (selectedDayKey == day.fullDateString) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            border = BorderStroke(1.dp, if (selectedDayKey == day.fullDateString) SolidPrimary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    selectedDayKey = if (selectedDayKey == day.fullDateString) null else day.fullDateString
                                }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.surface,
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                        ) {
                                            Text(
                                                text = "${day.dateNum}",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = day.fullDateString,
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "${day.transactions.size} transactions",
                                                fontSize = 10.5.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        if (day.totalExpense > 0) {
                                            Text(
                                                text = "-${LanguageHelper.formatCurrency(day.totalExpense, languageMode)}",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SolidExpense
                                            )
                                        }
                                        if (day.totalIncome > 0) {
                                            Text(
                                                text = "+${LanguageHelper.formatCurrency(day.totalIncome, languageMode)}",
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = SolidIncome
                                            )
                                        }
                                        if (day.totalExpense == 0.0 && day.totalIncome == 0.0) {
                                            Text(
                                                text = "-",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        }
                                    }
                                }

                                // Expanded transaction details for this day
                                if (selectedDayKey == day.fullDateString && day.transactions.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                    Spacer(modifier = Modifier.height(8.dp))

                                    day.transactions.forEach { tx ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(6.dp))
                                                .clickable { onTransactionClick?.invoke(tx) }
                                                .padding(vertical = 4.dp, horizontal = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(6.dp)
                                                        .clip(CircleShape)
                                                        .background(if (tx.transaction.type == TransactionType.EXPENSE) SolidExpense else SolidIncome)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = tx.category?.localizedName(languageMode) ?: tx.transaction.payeeOrPayer.ifBlank { tx.transaction.note }.ifBlank { "Transaction" },
                                                    fontSize = 11.5.sp,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            Text(
                                                text = LanguageHelper.formatCurrency(tx.transaction.amount, languageMode),
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (tx.transaction.type == TransactionType.EXPENSE) SolidExpense else SolidIncome
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
