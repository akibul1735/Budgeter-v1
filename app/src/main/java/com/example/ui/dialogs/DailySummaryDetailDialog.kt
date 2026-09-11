package com.example.ui.dialogs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.util.DateUtils
import com.example.util.LanguageHelper
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import kotlin.math.max

enum class DailyPeriodFilter(val labelEn: String, val labelBn: String, val days: Int) {
    LAST_7_DAYS("7 Days", "৭ দিন", 7),
    LAST_14_DAYS("14 Days", "১৪ দিন", 14),
    THIS_MONTH("This Month", "চলতি মাস", 30),
    LAST_30_DAYS("30 Days", "৩০ দিন", 30),
    LAST_MONTH("Last Month", "গত মাস", 30)
}

enum class DailyGraphMode(val labelEn: String, val labelBn: String) {
    EXPENSE("Expense", "ব্যয়"),
    INCOME("Income", "আয়"),
    NET_FLOW("Net Flow", "প্রবাহ"),
    BOTH("Both", "উভয়")
}

enum class DailyBreakdownFilter(val labelEn: String, val labelBn: String) {
    ALL("All Days", "সব দিন"),
    SPENT_ONLY("Spending Days", "ব্যয়ের দিন"),
    ABOVE_AVG("Above Avg", "গড়ের বেশি"),
    ZERO_SPEND("No-Spend", "ব্যয়হীন দিন")
}

enum class DailyBreakdownSort(val labelEn: String, val labelBn: String) {
    NEWEST("Newest", "নতুন"),
    OLDEST("Oldest", "পুরাতন"),
    HIGHEST_EXPENSE("Highest Spend", "সর্বোচ্চ ব্যয়"),
    HIGHEST_INCOME("Highest Income", "সর্বোচ্চ আয়")
}

data class DailySummaryItem(
    val dateEpochMs: Long,
    val dateNum: Int,
    val dayOfWeekShort: String,
    val dayOfWeekFull: String,
    val fullDateString: String,
    val isWeekend: Boolean,
    val isToday: Boolean,
    val expense: Double,
    val income: Double,
    val net: Double,
    val transactions: List<TransactionWithDetails>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailySummaryDetailDialog(
    transactions: List<TransactionWithDetails>,
    languageMode: LanguageMode,
    allCategories: List<Category> = emptyList(),
    accounts: List<Account> = emptyList(),
    initialSelectedDateEpoch: Long? = null,
    onDismiss: () -> Unit,
    onTransactionClick: ((TransactionWithDetails) -> Unit)? = null,
    onAccountClick: ((Account) -> Unit)? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var selectedPeriod by remember { mutableStateOf(DailyPeriodFilter.THIS_MONTH) }
    var periodOffset by remember { mutableStateOf(0) }
    var graphMode by remember { mutableStateOf(DailyGraphMode.EXPENSE) }
    var breakdownFilter by remember { mutableStateOf(DailyBreakdownFilter.ALL) }
    var breakdownSort by remember { mutableStateOf(DailyBreakdownSort.NEWEST) }
    var searchQuery by remember { mutableStateOf("") }
    var showSortMenu by remember { mutableStateOf(false) }

    // Selected day for interactive chart inspection & focus
    var selectedDayKey by remember {
        mutableStateOf(
            if (initialSelectedDateEpoch != null) {
                DateUtils.formatDate(initialSelectedDateEpoch, languageMode)
            } else null
        )
    }

    // Expanded accordion states for day rows
    val expandedDayKeys = remember { mutableStateMapOf<String, Boolean>() }

    // Calculate start/end dates based on selectedPeriod and periodOffset
    val (startCal, endCal, periodRangeLabel) = remember(selectedPeriod, periodOffset, languageMode) {
        val start = Calendar.getInstance()
        val end = Calendar.getInstance()

        when (selectedPeriod) {
            DailyPeriodFilter.THIS_MONTH -> {
                start.add(Calendar.MONTH, periodOffset)
                start.set(Calendar.DAY_OF_MONTH, 1)
                start.set(Calendar.HOUR_OF_DAY, 0)
                start.set(Calendar.MINUTE, 0)
                start.set(Calendar.SECOND, 0)
                start.set(Calendar.MILLISECOND, 0)

                end.timeInMillis = start.timeInMillis
                end.set(Calendar.DAY_OF_MONTH, end.getActualMaximum(Calendar.DAY_OF_MONTH))
                end.set(Calendar.HOUR_OF_DAY, 23)
                end.set(Calendar.MINUTE, 59)
                end.set(Calendar.SECOND, 59)
                end.set(Calendar.MILLISECOND, 999)
            }
            DailyPeriodFilter.LAST_MONTH -> {
                start.add(Calendar.MONTH, -1 + periodOffset)
                start.set(Calendar.DAY_OF_MONTH, 1)
                start.set(Calendar.HOUR_OF_DAY, 0)
                start.set(Calendar.MINUTE, 0)
                start.set(Calendar.SECOND, 0)
                start.set(Calendar.MILLISECOND, 0)

                end.timeInMillis = start.timeInMillis
                end.set(Calendar.DAY_OF_MONTH, end.getActualMaximum(Calendar.DAY_OF_MONTH))
                end.set(Calendar.HOUR_OF_DAY, 23)
                end.set(Calendar.MINUTE, 59)
                end.set(Calendar.SECOND, 59)
                end.set(Calendar.MILLISECOND, 999)
            }
            else -> {
                val days = selectedPeriod.days
                end.add(Calendar.DAY_OF_YEAR, periodOffset * days)
                end.set(Calendar.HOUR_OF_DAY, 23)
                end.set(Calendar.MINUTE, 59)
                end.set(Calendar.SECOND, 59)
                end.set(Calendar.MILLISECOND, 999)

                start.timeInMillis = end.timeInMillis
                start.add(Calendar.DAY_OF_YEAR, -(days - 1))
                start.set(Calendar.HOUR_OF_DAY, 0)
                start.set(Calendar.MINUTE, 0)
                start.set(Calendar.SECOND, 0)
                start.set(Calendar.MILLISECOND, 0)
            }
        }

        val startStr = DateUtils.formatDate(start.timeInMillis, languageMode)
        val endStr = DateUtils.formatDate(end.timeInMillis, languageMode)
        Triple(start, end, "$startStr – $endStr")
    }

    // Build day items
    val dayItems = remember(transactions, startCal, endCal, languageMode) {
        val items = mutableListOf<DailySummaryItem>()
        val dayNameSdf = SimpleDateFormat("EEE", Locale.getDefault())
        val dayNameFullSdf = SimpleDateFormat("EEEE", Locale.getDefault())
        val todayCal = Calendar.getInstance()

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

            val dow = tempCal.get(Calendar.DAY_OF_WEEK)
            val isWeekend = dow == Calendar.FRIDAY || dow == Calendar.SATURDAY
            val isToday = tempCal.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR) &&
                    tempCal.get(Calendar.DAY_OF_YEAR) == todayCal.get(Calendar.DAY_OF_YEAR)

            items.add(
                DailySummaryItem(
                    dateEpochMs = dStart,
                    dateNum = tempCal.get(Calendar.DAY_OF_MONTH),
                    dayOfWeekShort = dayNameSdf.format(Date(dStart)),
                    dayOfWeekFull = dayNameFullSdf.format(Date(dStart)),
                    fullDateString = DateUtils.formatDate(dStart, languageMode),
                    isWeekend = isWeekend,
                    isToday = isToday,
                    expense = expense,
                    income = income,
                    net = income - expense,
                    transactions = dayTxs
                )
            )

            tempCal.add(Calendar.DAY_OF_YEAR, 1)
        }

        items
    }

    // Performance Metrics Calculations
    val totalPeriodExpense = remember(dayItems) { dayItems.sumOf { it.expense } }
    val totalPeriodIncome = remember(dayItems) { dayItems.sumOf { it.income } }
    val netCashflow = totalPeriodIncome - totalPeriodExpense
    val totalDaysCount = dayItems.size.coerceAtLeast(1)
    val dailyAvgExpense = totalPeriodExpense / totalDaysCount
    val dailyAvgIncome = totalPeriodIncome / totalDaysCount
    val spentDaysCount = remember(dayItems) { dayItems.count { it.expense > 0 } }
    val zeroSpendDaysCount = remember(dayItems) { dayItems.count { it.expense == 0.0 } }
    val spikeDaysCount = remember(dayItems, dailyAvgExpense) { dayItems.count { it.expense > dailyAvgExpense } }
    val peakExpenseDay = remember(dayItems) { dayItems.maxByOrNull { it.expense } }
    val peakIncomeDay = remember(dayItems) { dayItems.maxByOrNull { it.income } }
    val totalTxCount = remember(dayItems) { dayItems.sumOf { it.transactions.size } }

    val savingsRate = remember(totalPeriodIncome, netCashflow) {
        if (totalPeriodIncome > 0) {
            ((netCashflow / totalPeriodIncome) * 100.0).coerceIn(-100.0, 100.0)
        } else if (totalPeriodExpense > 0) -100.0 else 0.0
    }

    // Top categories distribution
    val topCategories = remember(dayItems, languageMode) {
        val expTxs = dayItems.flatMap { it.transactions }.filter { it.transaction.type == TransactionType.EXPENSE }
        val totalExp = expTxs.sumOf { it.transaction.amount }
        if (totalExp <= 0.0) {
            emptyList()
        } else {
            expTxs.groupBy { it.category?.id ?: -1L }
                .map { (catId, txs) ->
                    val sample = txs.first()
                    val catName = sample.category?.localizedName(languageMode)
                        ?: sample.transaction.payeeOrPayer.ifBlank { "Other" }
                    val catAmount = txs.sumOf { it.transaction.amount }
                    val catPct = (catAmount / totalExp) * 100.0
                    val catColor = sample.category?.let { cat ->
                        runCatching { Color(android.graphics.Color.parseColor(cat.colorHex)) }.getOrDefault(SolidExpense)
                    } ?: SolidExpense
                    Triple(catName, catAmount to catPct, catColor)
                }
                .sortedByDescending { it.second.first }
                .take(5)
        }
    }

    // Weekday Spending Heatmap (Mon to Sun)
    val weekdaySpendStats = remember(dayItems, languageMode) {
        val cal = Calendar.getInstance()
        val daysDefinition = listOf(
            Calendar.MONDAY to ("Mon" to "সোম"),
            Calendar.TUESDAY to ("Tue" to "মঙ্গল"),
            Calendar.WEDNESDAY to ("Wed" to "বুধ"),
            Calendar.THURSDAY to ("Thu" to "বৃহঃ"),
            Calendar.FRIDAY to ("Fri" to "শুক্র"),
            Calendar.SATURDAY to ("Sat" to "শনি"),
            Calendar.SUNDAY to ("Sun" to "রবি")
        )
        daysDefinition.map { (dow, labels) ->
            val matching = dayItems.filter {
                cal.timeInMillis = it.dateEpochMs
                cal.get(Calendar.DAY_OF_WEEK) == dow
            }
            val sum = matching.sumOf { it.expense }
            val avg = if (matching.isNotEmpty()) sum / matching.size else 0.0
            val label = if (languageMode == LanguageMode.BANGLA) labels.second else labels.first
            Triple(label, avg, matching.size)
        }
    }

    // Filtered & Sorted Day Items for the Accordion Breakdown
    val displayedDayItems = remember(dayItems, breakdownFilter, breakdownSort, searchQuery) {
        var filtered = dayItems.asSequence()

        when (breakdownFilter) {
            DailyBreakdownFilter.ALL -> {}
            DailyBreakdownFilter.SPENT_ONLY -> filtered = filtered.filter { it.expense > 0 }
            DailyBreakdownFilter.ABOVE_AVG -> filtered = filtered.filter { it.expense > dailyAvgExpense }
            DailyBreakdownFilter.ZERO_SPEND -> filtered = filtered.filter { it.expense == 0.0 }
        }

        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase()
            filtered = filtered.filter { day ->
                day.fullDateString.lowercase().contains(q) ||
                        day.dayOfWeekFull.lowercase().contains(q) ||
                        day.transactions.any { tx ->
                            (tx.category?.nameEn?.lowercase()?.contains(q) == true) ||
                                    (tx.category?.nameBn?.lowercase()?.contains(q) == true) ||
                                    tx.transaction.payeeOrPayer.lowercase().contains(q) ||
                                    tx.transaction.note.lowercase().contains(q)
                        }
            }
        }

        val list = filtered.toList()
        when (breakdownSort) {
            DailyBreakdownSort.NEWEST -> list.reversed()
            DailyBreakdownSort.OLDEST -> list
            DailyBreakdownSort.HIGHEST_EXPENSE -> list.sortedByDescending { it.expense }
            DailyBreakdownSort.HIGHEST_INCOME -> list.sortedByDescending { it.income }
        }
    }

    // Selected day focused item
    val focusedDayItem = remember(selectedDayKey, dayItems) {
        dayItems.find { it.fullDateString == selectedDayKey }
    }

    val primaryColor = MaterialTheme.colorScheme.primary

    // Copy Summary to Clipboard
    fun copySummaryReport() {
        val periodText = if (languageMode == LanguageMode.BANGLA) selectedPeriod.labelBn else selectedPeriod.labelEn
        val report = buildString {
            appendLine("📊 Daily Summary ($periodText)")
            appendLine("📅 $periodRangeLabel")
            appendLine("🔴 Total Expense: ${LanguageHelper.formatCurrency(totalPeriodExpense, languageMode)}")
            appendLine("🟢 Total Income: ${LanguageHelper.formatCurrency(totalPeriodIncome, languageMode)}")
            appendLine("💰 Net Flow: ${LanguageHelper.formatCurrency(netCashflow, languageMode)} (${String.format(Locale.US, "%.1f", savingsRate)}%)")
            appendLine("📈 Daily Average: ${LanguageHelper.formatCurrency(dailyAvgExpense, languageMode)}/day")
            if (peakExpenseDay != null && peakExpenseDay.expense > 0) {
                appendLine("🏆 Peak Day: ${peakExpenseDay.fullDateString} (${LanguageHelper.formatCurrency(peakExpenseDay.expense, languageMode)})")
            }
            appendLine("🛡️ No-Spend Days: $zeroSpendDaysCount of $totalDaysCount days")
        }
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Daily Summary", report)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(
            context,
            if (languageMode == LanguageMode.BANGLA) "সারসংক্ষেপ কপি করা হয়েছে" else "Summary copied to clipboard",
            Toast.LENGTH_SHORT
        ).show()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("daily_summary_detail_screen"),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                // Top App Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "দৈনিক সারসংক্ষেপ বিশ্লেষণ" else "Daily Summary Analytics",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = periodRangeLabel,
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { copySummaryReport() }) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share or Copy Summary",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

                // Scrollable Content
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
                ) {
                    // Time Horizon Selector & Navigation Controls
                    item(key = "period_controls") {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                // Horizontal Filter Chips
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    DailyPeriodFilter.values().forEach { filter ->
                                        val isSelected = selectedPeriod == filter
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = {
                                                selectedPeriod = filter
                                                periodOffset = 0
                                                selectedDayKey = null
                                            },
                                            label = {
                                                Text(
                                                    text = if (languageMode == LanguageMode.BANGLA) filter.labelBn else filter.labelEn,
                                                    fontSize = 12.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                )
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                            )
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Period Step Navigator (< Date Range >)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = {
                                            periodOffset -= 1
                                            selectedDayKey = null
                                        },
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowLeft,
                                            contentDescription = "Previous Period",
                                            tint = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CalendarToday,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = periodRangeLabel,
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (periodOffset != 0) {
                                            TextButton(
                                                onClick = {
                                                    periodOffset = 0
                                                    selectedDayKey = null
                                                },
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = if (languageMode == LanguageMode.BANGLA) "বর্তমান" else "Current",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                        IconButton(
                                            onClick = {
                                                if (periodOffset < 0) {
                                                    periodOffset += 1
                                                    selectedDayKey = null
                                                }
                                            },
                                            enabled = periodOffset < 0,
                                            modifier = Modifier.size(34.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.KeyboardArrowRight,
                                                contentDescription = "Next Period",
                                                tint = if (periodOffset < 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outlineVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Key Insights 2x2 Metric Grid
                    item(key = "metric_grid") {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Total Expense Card
                                ModernInsightCard(
                                    title = LanguageHelper.getString("expense", languageMode),
                                    amount = totalPeriodExpense,
                                    subtitle = "$totalTxCount txs in period",
                                    badgeText = "-${LanguageHelper.formatCurrency(totalPeriodExpense, languageMode)}",
                                    icon = Icons.AutoMirrored.Filled.TrendingDown,
                                    accentColor = SolidExpense,
                                    containerColor = SolidExpenseContainer.copy(alpha = 0.55f),
                                    languageMode = languageMode,
                                    modifier = Modifier.weight(1f)
                                )

                                // Total Income Card
                                ModernInsightCard(
                                    title = LanguageHelper.getString("income", languageMode),
                                    amount = totalPeriodIncome,
                                    subtitle = if (netCashflow >= 0) "+${LanguageHelper.formatCurrency(netCashflow, languageMode)} net" else "${LanguageHelper.formatCurrency(netCashflow, languageMode)} net",
                                    badgeText = "+${LanguageHelper.formatCurrency(totalPeriodIncome, languageMode)}",
                                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                                    accentColor = SolidIncome,
                                    containerColor = SolidIncomeContainer.copy(alpha = 0.55f),
                                    languageMode = languageMode,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Daily Average Expense
                                ModernInsightCard(
                                    title = if (languageMode == LanguageMode.BANGLA) "দৈনিক গড় ব্যয়" else "Daily Average",
                                    amount = dailyAvgExpense,
                                    subtitle = if (languageMode == LanguageMode.BANGLA) "মাসিক প্রক্ষেপণ: ${LanguageHelper.formatCurrency(dailyAvgExpense * 30, languageMode)}" else "30d est: ${LanguageHelper.formatCurrency(dailyAvgExpense * 30, languageMode)}",
                                    badgeText = "${LanguageHelper.formatCurrency(dailyAvgExpense, languageMode)}/d",
                                    icon = Icons.Default.Speed,
                                    accentColor = SolidAmber,
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                    languageMode = languageMode,
                                    modifier = Modifier.weight(1f)
                                )

                                // Net Flow & Savings Rate
                                ModernInsightCard(
                                    title = if (languageMode == LanguageMode.BANGLA) "সঞ্চয় হার / নেট" else "Net Savings",
                                    amount = netCashflow,
                                    subtitle = if (savingsRate >= 0) "${String.format(Locale.US, "%.1f", savingsRate)}% savings rate" else "Deficit period",
                                    badgeText = if (netCashflow >= 0) "+${LanguageHelper.formatCurrency(netCashflow, languageMode)}" else LanguageHelper.formatCurrency(netCashflow, languageMode),
                                    icon = Icons.Default.AccountBalanceWallet,
                                    accentColor = if (netCashflow >= 0) SolidIncome else SolidExpense,
                                    containerColor = if (netCashflow >= 0) SolidIncomeContainer.copy(alpha = 0.35f) else SolidExpenseContainer.copy(alpha = 0.35f),
                                    languageMode = languageMode,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // Key Highlights Quick Badges Ribbon
                    item(key = "highlights_ribbon") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (peakExpenseDay != null && peakExpenseDay.expense > 0) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                    modifier = Modifier.clickable {
                                        selectedDayKey = peakExpenseDay.fullDateString
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = "🏆", fontSize = 13.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Text(
                                                text = if (languageMode == LanguageMode.BANGLA) "সর্বোচ্চ ব্যয়ের দিন" else "Peak Spend Day",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "${peakExpenseDay.dateNum} ${peakExpenseDay.dayOfWeekShort} • ${LanguageHelper.formatCurrency(peakExpenseDay.expense, languageMode)}",
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SolidExpense
                                            )
                                        }
                                    }
                                }
                            }

                            // No-Spend Days Badge
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                modifier = Modifier.clickable {
                                    breakdownFilter = DailyBreakdownFilter.ZERO_SPEND
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "🛡️", fontSize = 13.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = if (languageMode == LanguageMode.BANGLA) "ব্যয়হীন দিন" else "Zero-Spend Days",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        val pctZero = if (totalDaysCount > 0) (zeroSpendDaysCount * 100) / totalDaysCount else 0
                                        Text(
                                            text = "$zeroSpendDaysCount days ($pctZero%)",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SolidIncome
                                        )
                                    }
                                }
                            }

                            // Spikes above average badge
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                modifier = Modifier.clickable {
                                    breakdownFilter = DailyBreakdownFilter.ABOVE_AVG
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "📈", fontSize = 13.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = if (languageMode == LanguageMode.BANGLA) "গড়ের ঊর্ধ্বে দিন" else "Above-Avg Spikes",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "$spikeDaysCount days > avg",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Modern Interactive Graph Card
                    item(key = "interactive_graph") {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                // Graph Mode Tabs Header
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "দৈনিক আর্থিক গতিধারা" else "Daily Flow Trend",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    // Mode Segmented Control
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(MaterialTheme.colorScheme.surface)
                                            .padding(2.dp),
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        DailyGraphMode.values().forEach { mode ->
                                            val isSelected = graphMode == mode
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(
                                                        if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                                                    )
                                                    .clickable { graphMode = mode }
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = if (languageMode == LanguageMode.BANGLA) mode.labelBn else mode.labelEn,
                                                    fontSize = 11.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Dynamic Canvas Chart
                                val maxChartValue = remember(dayItems, graphMode) {
                                    val peak = when (graphMode) {
                                        DailyGraphMode.EXPENSE -> dayItems.maxOfOrNull { it.expense } ?: 100.0
                                        DailyGraphMode.INCOME -> dayItems.maxOfOrNull { it.income } ?: 100.0
                                        DailyGraphMode.NET_FLOW -> dayItems.maxOfOrNull { abs(it.net) } ?: 100.0
                                        DailyGraphMode.BOTH -> max(
                                            dayItems.maxOfOrNull { it.expense } ?: 100.0,
                                            dayItems.maxOfOrNull { it.income } ?: 100.0
                                        )
                                    }
                                    peak.coerceAtLeast(100.0)
                                }

                                val isDark = isSystemInDarkTheme()

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(190.dp)
                                ) {
                                    Canvas(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .pointerInput(dayItems) {
                                                detectTapGestures { offset ->
                                                    if (dayItems.isNotEmpty()) {
                                                        val slotW = size.width / dayItems.size
                                                        val idx = (offset.x / slotW).toInt().coerceIn(0, dayItems.size - 1)
                                                        val clickedDay = dayItems[idx]
                                                        selectedDayKey = if (selectedDayKey == clickedDay.fullDateString) null else clickedDay.fullDateString
                                                    }
                                                }
                                            }
                                    ) {
                                        val width = size.width
                                        val height = size.height
                                        val bottomAxisH = 26.dp.toPx()
                                        val chartAreaH = height - bottomAxisH - 14.dp.toPx()
                                        val count = dayItems.size.coerceAtLeast(1)
                                        val slotW = width / count
                                        val baselineY = height - bottomAxisH

                                        // Draw subtle grid line for zero baseline
                                        drawLine(
                                            color = if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.15f),
                                            start = Offset(0f, baselineY),
                                            end = Offset(width, baselineY),
                                            strokeWidth = 1.dp.toPx()
                                        )

                                        // Draw Average Line if Expense or Income mode
                                        val avgToDraw = when (graphMode) {
                                            DailyGraphMode.EXPENSE -> dailyAvgExpense
                                            DailyGraphMode.INCOME -> dailyAvgIncome
                                            else -> 0.0
                                        }

                                        if (avgToDraw > 0 && avgToDraw <= maxChartValue) {
                                            val avgY = baselineY - (avgToDraw / maxChartValue * chartAreaH).toFloat()
                                            drawLine(
                                                color = SolidAmber.copy(alpha = 0.7f),
                                                start = Offset(0f, avgY),
                                                end = Offset(width, avgY),
                                                strokeWidth = 1.5.dp.toPx(),
                                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                                            )
                                        }

                                        val labelPaint = android.graphics.Paint().apply {
                                            this.color = if (isDark) android.graphics.Color.LTGRAY else android.graphics.Color.GRAY
                                            this.textSize = 9.sp.toPx()
                                            this.textAlign = android.graphics.Paint.Align.CENTER
                                            this.isAntiAlias = true
                                        }

                                        val selectedLabelPaint = android.graphics.Paint().apply {
                                            this.color = if (isDark) android.graphics.Color.WHITE else android.graphics.Color.BLACK
                                            this.textSize = 9.5.sp.toPx()
                                            this.textAlign = android.graphics.Paint.Align.CENTER
                                            this.isFakeBoldText = true
                                            this.isAntiAlias = true
                                        }

                                        // Draw individual day bars
                                        dayItems.forEachIndexed { i, day ->
                                            val centerX = slotW * i + slotW / 2f
                                            val isSelected = selectedDayKey == day.fullDateString

                                            // Draw X Axis date label
                                            val p = if (isSelected) selectedLabelPaint else labelPaint
                                            drawContext.canvas.nativeCanvas.drawText(
                                                "${day.dateNum}",
                                                centerX,
                                                baselineY + 14.dp.toPx(),
                                                p
                                            )

                                            // Draw selection highlight background beam if selected
                                            if (isSelected) {
                                                drawRoundRect(
                                                    color = primaryColor.copy(alpha = 0.12f),
                                                    topLeft = Offset(centerX - slotW / 2f + 1.dp.toPx(), 0f),
                                                    size = Size(slotW - 2.dp.toPx(), height),
                                                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                                                )
                                            }

                                            when (graphMode) {
                                                DailyGraphMode.EXPENSE -> {
                                                    val expH = if (day.expense > 0) {
                                                        (day.expense / maxChartValue * chartAreaH).toFloat().coerceAtLeast(3.dp.toPx())
                                                    } else 0f

                                                    if (expH > 0) {
                                                        val barW = (slotW * 0.52f).coerceIn(6.dp.toPx(), 28.dp.toPx())
                                                        val barLeft = centerX - barW / 2f
                                                        val barTop = baselineY - expH

                                                        val barBrush = Brush.verticalGradient(
                                                            colors = listOf(
                                                                if (isSelected) SolidPrimary else SolidExpense,
                                                                if (isSelected) SolidPrimary.copy(alpha = 0.7f) else SolidExpenseDark.copy(alpha = 0.75f)
                                                            ),
                                                            startY = barTop,
                                                            endY = baselineY
                                                        )

                                                        drawRoundRect(
                                                            brush = barBrush,
                                                            topLeft = Offset(barLeft, barTop),
                                                            size = Size(barW, expH),
                                                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                                        )
                                                    }
                                                }

                                                DailyGraphMode.INCOME -> {
                                                    val incH = if (day.income > 0) {
                                                        (day.income / maxChartValue * chartAreaH).toFloat().coerceAtLeast(3.dp.toPx())
                                                    } else 0f

                                                    if (incH > 0) {
                                                        val barW = (slotW * 0.52f).coerceIn(6.dp.toPx(), 28.dp.toPx())
                                                        val barLeft = centerX - barW / 2f
                                                        val barTop = baselineY - incH

                                                        val barBrush = Brush.verticalGradient(
                                                            colors = listOf(
                                                                if (isSelected) SolidPrimary else SolidIncome,
                                                                if (isSelected) SolidPrimary.copy(alpha = 0.7f) else SolidIncomeDark.copy(alpha = 0.75f)
                                                            ),
                                                            startY = barTop,
                                                            endY = baselineY
                                                        )

                                                        drawRoundRect(
                                                            brush = barBrush,
                                                            topLeft = Offset(barLeft, barTop),
                                                            size = Size(barW, incH),
                                                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                                        )
                                                    }
                                                }

                                                DailyGraphMode.NET_FLOW -> {
                                                    val netH = if (day.net != 0.0) {
                                                        (abs(day.net) / maxChartValue * chartAreaH).toFloat().coerceAtLeast(3.dp.toPx())
                                                    } else 0f

                                                    if (netH > 0) {
                                                        val barW = (slotW * 0.52f).coerceIn(6.dp.toPx(), 28.dp.toPx())
                                                        val barLeft = centerX - barW / 2f
                                                        val barTop = baselineY - netH
                                                        val barColor = if (day.net >= 0) SolidIncome else SolidExpense

                                                        drawRoundRect(
                                                            color = if (isSelected) SolidPrimary else barColor,
                                                            topLeft = Offset(barLeft, barTop),
                                                            size = Size(barW, netH),
                                                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                                        )
                                                    }
                                                }

                                                DailyGraphMode.BOTH -> {
                                                    val barW = (slotW * 0.32f).coerceIn(3.dp.toPx(), 14.dp.toPx())
                                                    val gap = 1.5.dp.toPx()

                                                    // Income (Left)
                                                    if (day.income > 0) {
                                                        val incH = (day.income / maxChartValue * chartAreaH).toFloat().coerceAtLeast(3.dp.toPx())
                                                        drawRoundRect(
                                                            color = if (isSelected) SolidPrimary else SolidIncome,
                                                            topLeft = Offset(centerX - barW - gap / 2f, baselineY - incH),
                                                            size = Size(barW, incH),
                                                            cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                                                        )
                                                    }

                                                    // Expense (Right)
                                                    if (day.expense > 0) {
                                                        val expH = (day.expense / maxChartValue * chartAreaH).toFloat().coerceAtLeast(3.dp.toPx())
                                                        drawRoundRect(
                                                            color = if (isSelected) SolidPrimary else SolidExpense,
                                                            topLeft = Offset(centerX + gap / 2f, baselineY - expH),
                                                            size = Size(barW, expH),
                                                            cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // Legend & Average Indicator
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (graphMode == DailyGraphMode.EXPENSE || graphMode == DailyGraphMode.BOTH) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .clip(CircleShape)
                                                        .background(SolidExpense)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(text = "Expense", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                        if (graphMode == DailyGraphMode.INCOME || graphMode == DailyGraphMode.BOTH) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .clip(CircleShape)
                                                        .background(SolidIncome)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(text = "Income", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                    }

                                    if (dailyAvgExpense > 0 && (graphMode == DailyGraphMode.EXPENSE || graphMode == DailyGraphMode.BOTH)) {
                                        Text(
                                            text = "Avg: ${LanguageHelper.formatCurrency(dailyAvgExpense, languageMode)}",
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = SolidAmber
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Selected Day Inspector Floating Card (Animated)
                    if (focusedDayItem != null) {
                        item(key = "selected_day_inspector") {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)),
                                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = "${focusedDayItem.dateNum}",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 15.sp,
                                                        color = MaterialTheme.colorScheme.onPrimary
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = focusedDayItem.fullDateString,
                                                    fontSize = 13.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "${focusedDayItem.dayOfWeekFull} • ${focusedDayItem.transactions.size} transactions",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        IconButton(
                                            onClick = { selectedDayKey = null },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Clear Selection",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(text = "Expense", fontSize = 10.sp, color = SolidExpense)
                                            Text(
                                                text = "-${LanguageHelper.formatCurrency(focusedDayItem.expense, languageMode)}",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SolidExpense
                                            )
                                        }
                                        Column {
                                            Text(text = "Income", fontSize = 10.sp, color = SolidIncome)
                                            Text(
                                                text = "+${LanguageHelper.formatCurrency(focusedDayItem.income, languageMode)}",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SolidIncome
                                            )
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(text = "Net Flow", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            val netColor = if (focusedDayItem.net >= 0) SolidIncome else SolidExpense
                                            Text(
                                                text = if (focusedDayItem.net >= 0) "+${LanguageHelper.formatCurrency(focusedDayItem.net, languageMode)}" else LanguageHelper.formatCurrency(focusedDayItem.net, languageMode),
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = netColor
                                            )
                                        }
                                    }

                                    if (focusedDayItem.transactions.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        TextButton(
                                            onClick = {
                                                expandedDayKeys[focusedDayItem.fullDateString] = true
                                                coroutineScope.launch {
                                                    listState.animateScrollToItem(4)
                                                }
                                            },
                                            modifier = Modifier.align(Alignment.End),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text("Show Transactions Below", fontSize = 11.5.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Top Spending Categories Breakdown
                    if (topCategories.isNotEmpty()) {
                        item(key = "top_categories") {
                            Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (languageMode == LanguageMode.BANGLA) "শীর্ষ ব্যয়ের খাতসমূহ" else "Top Expense Categories",
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${topCategories.size} categories",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    topCategories.forEach { (catName, amountPair, catColor) ->
                                        val (amount, pct) = amountPair
                                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
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
                                                            .background(catColor)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = catName,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = LanguageHelper.formatCurrency(amount, languageMode),
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "(${String.format(Locale.US, "%.0f", pct)}%)",
                                                        fontSize = 10.5.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(3.dp))
                                            LinearProgressIndicator(
                                                progress = { (pct / 100f).toFloat().coerceIn(0f, 1f) },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(5.dp)
                                                    .clip(RoundedCornerShape(3.dp)),
                                                color = catColor,
                                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Weekday Spending Heatmap (Pattern by day of week)
                    item(key = "weekday_heatmap") {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "সপ্তাহের দিনভিত্তিক ব্যয়ের ধরন" else "Day-of-Week Spending Habit",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                val maxWeekdayAvg = weekdaySpendStats.maxOfOrNull { it.second }?.coerceAtLeast(10.0) ?: 10.0

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    weekdaySpendStats.forEach { (label, avg, count) ->
                                        val heightRatio = (avg / maxWeekdayAvg).toFloat().coerceIn(0.06f, 1f)
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .height(60.dp)
                                                    .width(16.dp),
                                                contentAlignment = Alignment.BottomCenter
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxHeight(heightRatio)
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                                        .background(
                                                            if (avg >= maxWeekdayAvg * 0.85f && avg > 0) SolidExpense else SolidPrimary.copy(alpha = 0.7f)
                                                        )
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = label,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = if (avg > 0) LanguageHelper.formatCurrency(avg, languageMode) else "-",
                                                fontSize = 8.5.sp,
                                                color = MaterialTheme.colorScheme.outline,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Breakdown Filter Chips & Search Bar
                    item(key = "breakdown_header") {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "দিনভিত্তিক লেনদেন বিবরণী" else "Day by Day Breakdown",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                // Sort Menu Trigger
                                Box {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                        modifier = Modifier.clickable { showSortMenu = true }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.FilterList,
                                                contentDescription = "Sort",
                                                modifier = Modifier.size(14.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (languageMode == LanguageMode.BANGLA) breakdownSort.labelBn else breakdownSort.labelEn,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }

                                    DropdownMenu(
                                        expanded = showSortMenu,
                                        onDismissRequest = { showSortMenu = false }
                                    ) {
                                        DailyBreakdownSort.values().forEach { s ->
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        text = if (languageMode == LanguageMode.BANGLA) s.labelBn else s.labelEn,
                                                        fontWeight = if (breakdownSort == s) FontWeight.Bold else FontWeight.Normal
                                                    )
                                                },
                                                onClick = {
                                                    breakdownSort = s
                                                    showSortMenu = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            // Filter Chips Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                DailyBreakdownFilter.values().forEach { f ->
                                    val isSelected = breakdownFilter == f
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { breakdownFilter = f },
                                        label = {
                                            Text(
                                                text = if (languageMode == LanguageMode.BANGLA) f.labelBn else f.labelEn,
                                                fontSize = 11.sp
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    )
                                }
                            }

                            // Inline Search Bar
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = {
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "তারিখ, খাত বা বিবরণ দিয়ে খুঁজুন..." else "Search date, payee or note...",
                                        fontSize = 12.sp
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Clear",
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            )
                        }
                    }

                    // Day Items List
                    if (displayedDayItems.isEmpty()) {
                        item(key = "empty_breakdown") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "কোনো লেনদেন পাওয়া যায়নি" else "No transactions match this filter",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        items(
                            items = displayedDayItems,
                            key = { it.fullDateString }
                        ) { day ->
                            val isSelectedOnChart = selectedDayKey == day.fullDateString
                            val isExpanded = expandedDayKeys[day.fullDateString] == true || isSelectedOnChart

                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelectedOnChart) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelectedOnChart) MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                    else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateContentSize(animationSpec = tween(250))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    // Header Row
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                expandedDayKeys[day.fullDateString] = !isExpanded
                                                selectedDayKey = day.fullDateString
                                            },
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            // Date Circle Badge
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = when {
                                                    day.expense > dailyAvgExpense && day.expense > 0 -> SolidExpenseContainer.copy(alpha = 0.8f)
                                                    day.expense == 0.0 -> SolidIncomeContainer.copy(alpha = 0.8f)
                                                    else -> MaterialTheme.colorScheme.surface
                                                },
                                                border = BorderStroke(
                                                    1.dp,
                                                    when {
                                                        day.expense > dailyAvgExpense && day.expense > 0 -> SolidExpense.copy(alpha = 0.4f)
                                                        day.expense == 0.0 -> SolidIncome.copy(alpha = 0.4f)
                                                        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                                    }
                                                ),
                                                modifier = Modifier.size(38.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = "${day.dateNum}",
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = when {
                                                            day.expense > dailyAvgExpense && day.expense > 0 -> SolidOnExpenseContainer
                                                            day.expense == 0.0 -> SolidOnIncomeContainer
                                                            else -> MaterialTheme.colorScheme.onSurface
                                                        }
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.width(10.dp))

                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = day.dayOfWeekShort,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = day.fullDateString,
                                                        fontSize = 11.5.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    if (day.isToday) {
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Surface(
                                                            shape = RoundedCornerShape(4.dp),
                                                            color = MaterialTheme.colorScheme.primary,
                                                            modifier = Modifier.padding(horizontal = 2.dp)
                                                        ) {
                                                            Text(
                                                                text = "TODAY",
                                                                fontSize = 8.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = MaterialTheme.colorScheme.onPrimary,
                                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                            )
                                                        }
                                                    }
                                                }

                                                Text(
                                                    text = "${day.transactions.size} transactions",
                                                    fontSize = 10.5.sp,
                                                    color = MaterialTheme.colorScheme.outline
                                                )
                                            }
                                        }

                                        // Right side totals & chevron
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Column(horizontalAlignment = Alignment.End) {
                                                if (day.expense > 0) {
                                                    Text(
                                                        text = "-${LanguageHelper.formatCurrency(day.expense, languageMode)}",
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = SolidExpense
                                                    )
                                                }
                                                if (day.income > 0) {
                                                    Text(
                                                        text = "+${LanguageHelper.formatCurrency(day.income, languageMode)}",
                                                        fontSize = 11.5.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = SolidIncome
                                                    )
                                                }
                                                if (day.expense == 0.0 && day.income == 0.0) {
                                                    Text(
                                                        text = "No spend",
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.outline
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(
                                                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }

                                    // Expandable Transaction List for this day
                                    if (isExpanded && day.transactions.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                        Spacer(modifier = Modifier.height(6.dp))

                                        day.transactions.forEach { txItem ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .clickable { onTransactionClick?.invoke(txItem) }
                                                    .padding(vertical = 6.dp, horizontal = 4.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    val catColor = txItem.category?.let { cat ->
                                                        runCatching { Color(android.graphics.Color.parseColor(cat.colorHex)) }.getOrDefault(if (txItem.transaction.type == TransactionType.EXPENSE) SolidExpense else SolidIncome)
                                                    } ?: if (txItem.transaction.type == TransactionType.EXPENSE) SolidExpense else SolidIncome

                                                    Box(
                                                        modifier = Modifier
                                                            .size(8.dp)
                                                            .clip(CircleShape)
                                                            .background(catColor)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))

                                                    Column {
                                                        Text(
                                                            text = txItem.category?.localizedName(languageMode)
                                                                ?: txItem.transaction.payeeOrPayer.ifBlank { txItem.transaction.note }.ifBlank { "Transaction" },
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = MaterialTheme.colorScheme.onSurface,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                        val txAccount = if (txItem.transaction.type == TransactionType.EXPENSE) {
                                                            txItem.creditAccount ?: txItem.debitAccount
                                                        } else {
                                                            txItem.debitAccount ?: txItem.creditAccount
                                                        }
                                                        if (txAccount != null) {
                                                            Text(
                                                                text = txAccount.localizedName(languageMode),
                                                                fontSize = 10.sp,
                                                                color = if (onAccountClick != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                                modifier = if (onAccountClick != null) Modifier.clickable { onAccountClick(txAccount) } else Modifier
                                                            )
                                                        }
                                                    }
                                                }

                                                Text(
                                                    text = (if (txItem.transaction.type == TransactionType.EXPENSE) "-" else "+") +
                                                            LanguageHelper.formatCurrency(txItem.transaction.amount, languageMode),
                                                    fontSize = 12.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (txItem.transaction.type == TransactionType.EXPENSE) SolidExpense else SolidIncome
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
}

@Composable
private fun ModernInsightCard(
    title: String,
    amount: Double,
    subtitle: String,
    badgeText: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    containerColor: Color,
    languageMode: LanguageMode,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = LanguageHelper.formatCurrency(abs(amount), languageMode),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
