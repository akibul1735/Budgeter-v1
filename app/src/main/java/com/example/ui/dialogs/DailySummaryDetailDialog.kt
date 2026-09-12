package com.example.ui.dialogs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

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
    NET_FLOW("Net Flow", "প্রবাহ")
}

enum class DailyBreakdownFilter(val labelEn: String, val labelBn: String) {
    ALL("All Days", "সব দিন"),
    SPENT_ONLY("Spending Only", "ব্যয়ের দিন"),
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
    val monthShort: String,
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

/**
 * Calculates a "nice" scale with whole/rounded tick numbers for the Y-axis.
 */
private fun calculateNiceScale(maxValue: Double, tickCount: Int = 3): List<Double> {
    if (maxValue <= 0.0) return listOf(0.0, 50.0, 100.0)
    val rawStep = maxValue / tickCount
    val magnitude = 10.0.pow(floor(log10(rawStep)))
    val normalized = rawStep / magnitude
    val niceNormalized = when {
        normalized <= 1.0 -> 1.0
        normalized <= 2.0 -> 2.0
        normalized <= 2.5 -> 2.5
        normalized <= 5.0 -> 5.0
        else -> 10.0
    }
    val niceStep = (niceNormalized * magnitude).coerceAtLeast(1.0)
    val niceMax = ceil(maxValue / niceStep) * niceStep
    val ticks = mutableListOf<Double>()
    var current = 0.0
    while (current <= niceMax + 0.001) {
        ticks.add(current)
        current += niceStep
    }
    return ticks
}

private fun formatRoundedAxisNumber(value: Double): String {
    val rounded = value.roundToLong()
    return when {
        rounded >= 1_000_000 -> "${rounded / 1_000_000}M"
        rounded >= 1_000 -> "${rounded / 1_000}k"
        else -> "$rounded"
    }
}

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

    // 1. Filter Options State
    var selectedPeriod by remember { mutableStateOf(DailyPeriodFilter.THIS_MONTH) }
    var periodOffset by remember { mutableStateOf(0) }
    var graphMode by remember { mutableStateOf(DailyGraphMode.EXPENSE) }
    var breakdownFilter by remember { mutableStateOf(DailyBreakdownFilter.ALL) }
    var breakdownSort by remember { mutableStateOf(DailyBreakdownSort.NEWEST) }
    var searchQuery by remember { mutableStateOf("") }
    var showSortMenu by remember { mutableStateOf(false) }

    // Clickable Chart Selection State
    var selectedDayKey by remember {
        mutableStateOf(
            if (initialSelectedDateEpoch != null) {
                DateUtils.formatDate(initialSelectedDateEpoch, languageMode)
            } else null
        )
    }

    // Expandable states for table rows
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
        val monthSdf = SimpleDateFormat("MMM", Locale.getDefault())
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
                    monthShort = monthSdf.format(Date(dStart)),
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

    // 2. Key Statistics Calculations
    val totalPeriodExpense = remember(dayItems) { dayItems.sumOf { it.expense } }
    val totalPeriodIncome = remember(dayItems) { dayItems.sumOf { it.income } }
    val netCashflow = totalPeriodIncome - totalPeriodExpense
    val totalDaysCount = dayItems.size.coerceAtLeast(1)
    val dailyAvgExpense = totalPeriodExpense / totalDaysCount
    val spentDaysCount = remember(dayItems) { dayItems.count { it.expense > 0 } }
    val zeroSpendDaysCount = remember(dayItems) { dayItems.count { it.expense == 0.0 } }
    val peakExpenseDay = remember(dayItems) { dayItems.maxByOrNull { it.expense } }
    val totalTxCount = remember(dayItems) { dayItems.sumOf { it.transactions.size } }

    val savingsRate = remember(totalPeriodIncome, netCashflow) {
        if (totalPeriodIncome > 0) {
            ((netCashflow / totalPeriodIncome) * 100.0).coerceIn(-100.0, 100.0)
        } else if (totalPeriodExpense > 0) -100.0 else 0.0
    }

    // Filtered & Sorted Day Items for Table View
    val displayedDayItems = remember(dayItems, breakdownFilter, breakdownSort, searchQuery) {
        var filtered = dayItems.asSequence()

        when (breakdownFilter) {
            DailyBreakdownFilter.ALL -> {}
            DailyBreakdownFilter.SPENT_ONLY -> filtered = filtered.filter { it.expense > 0 }
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

    // Focused day item for interactive click/inspector
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
                appendLine("🏆 Peak Spend Day: ${peakExpenseDay.fullDateString} (${LanguageHelper.formatCurrency(peakExpenseDay.expense, languageMode)})")
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
                                text = if (languageMode == LanguageMode.BANGLA) "দৈনিক সারসংক্ষেপ" else "Daily Summary",
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

                // Scrollable Body organized into 4 Clean Sections
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
                ) {
                    // SECTION 1: Filter Options
                    item(key = "section_filters") {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                // Horizontal Period Filter Chips
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

                                // Date Range Stepper Navigator (< Date Range >)
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
                                                modifier = Modifier.size(13.dp)
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
                                                    text = if (languageMode == LanguageMode.BANGLA) "চলতি" else "Current",
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

                    // SECTION 2: Key Statistics (Scannable 2x2 Grid)
                    item(key = "section_key_statistics") {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Total Expense Card
                                ModernInsightCard(
                                    title = LanguageHelper.getString("expense", languageMode),
                                    amount = totalPeriodExpense,
                                    subtitle = if (languageMode == LanguageMode.BANGLA) "দৈনিক গড়: ${LanguageHelper.formatCurrency(dailyAvgExpense, languageMode)}" else "Daily Avg: ${LanguageHelper.formatCurrency(dailyAvgExpense, languageMode)}",
                                    badgeText = "-${LanguageHelper.formatCurrency(totalPeriodExpense, languageMode)}",
                                    icon = Icons.AutoMirrored.Filled.TrendingDown,
                                    accentColor = SolidExpense,
                                    containerColor = SolidExpenseContainer.copy(alpha = 0.5f),
                                    languageMode = languageMode,
                                    modifier = Modifier.weight(1f)
                                )

                                // Total Income Card
                                ModernInsightCard(
                                    title = LanguageHelper.getString("income", languageMode),
                                    amount = totalPeriodIncome,
                                    subtitle = if (languageMode == LanguageMode.BANGLA) "$totalTxCount টি লেনদেন" else "$totalTxCount transactions",
                                    badgeText = "+${LanguageHelper.formatCurrency(totalPeriodIncome, languageMode)}",
                                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                                    accentColor = SolidIncome,
                                    containerColor = SolidIncomeContainer.copy(alpha = 0.5f),
                                    languageMode = languageMode,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Net Cashflow Card
                                ModernInsightCard(
                                    title = if (languageMode == LanguageMode.BANGLA) "নেট ক্যাশফ্লো" else "Net Cash Flow",
                                    amount = netCashflow,
                                    subtitle = if (savingsRate >= 0) "${String.format(Locale.US, "%.1f", savingsRate)}% ${if (languageMode == LanguageMode.BANGLA) "সঞ্চয় হার" else "savings rate"}" else if (languageMode == LanguageMode.BANGLA) "ঘাটতি সময়কাল" else "Deficit period",
                                    badgeText = if (netCashflow >= 0) "+${LanguageHelper.formatCurrency(netCashflow, languageMode)}" else LanguageHelper.formatCurrency(netCashflow, languageMode),
                                    icon = Icons.Default.AccountBalanceWallet,
                                    accentColor = if (netCashflow >= 0) SolidIncome else SolidExpense,
                                    containerColor = if (netCashflow >= 0) SolidIncomeContainer.copy(alpha = 0.35f) else SolidExpenseContainer.copy(alpha = 0.35f),
                                    languageMode = languageMode,
                                    modifier = Modifier.weight(1f)
                                )

                                // Highlights (Peak spend + Zero-spend count)
                                Card(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = if (languageMode == LanguageMode.BANGLA) "গুরুত্বপূর্ণ তথ্য" else "Summary Stats",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Icon(
                                                imageVector = Icons.Default.Insights,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = if (languageMode == LanguageMode.BANGLA) "$zeroSpendDaysCount দিন ব্যয়হীন" else "$zeroSpendDaysCount no-spend days",
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SolidIncome
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        if (peakExpenseDay != null && peakExpenseDay.expense > 0) {
                                            Text(
                                                text = if (languageMode == LanguageMode.BANGLA) "সর্বোচ্চ: ${peakExpenseDay.dateNum} তারিখ (${LanguageHelper.formatCurrency(peakExpenseDay.expense, languageMode)})"
                                                else "Peak: ${peakExpenseDay.monthShort} ${peakExpenseDay.dateNum} (${LanguageHelper.formatCurrency(peakExpenseDay.expense, languageMode)})",
                                                fontSize = 10.sp,
                                                color = SolidExpense,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        } else {
                                            Text(
                                                text = if (languageMode == LanguageMode.BANGLA) "$spentDaysCount দিন ব্যয় হয়েছে" else "$spentDaysCount spending days",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // SECTION 3: Clickable Charts
                    item(key = "section_clickable_chart") {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                // Chart Mode Tabs & Title
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = if (languageMode == LanguageMode.BANGLA) "দৈনিক চার্ট" else "Daily Flow Chart",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (languageMode == LanguageMode.BANGLA) "বিশদ তথ্যের জন্য বারে ট্যাপ করুন" else "Tap a bar to inspect details",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    // Mode Control Chips (Expense | Income | Net Flow)
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

                                Spacer(modifier = Modifier.height(12.dp))

                                // Calculate scale with rounded whole numbers for Y-axis
                                val rawPeak = remember(dayItems, graphMode) {
                                    val peak = when (graphMode) {
                                        DailyGraphMode.EXPENSE -> dayItems.maxOfOrNull { it.expense } ?: 100.0
                                        DailyGraphMode.INCOME -> dayItems.maxOfOrNull { it.income } ?: 100.0
                                        DailyGraphMode.NET_FLOW -> dayItems.maxOfOrNull { abs(it.net) } ?: 100.0
                                    }
                                    peak.coerceAtLeast(100.0)
                                }
                                val niceTicks = remember(rawPeak) { calculateNiceScale(rawPeak, 3) }
                                val chartMax = niceTicks.last()
                                val isDark = isSystemInDarkTheme()

                                // Canvas Chart with Clickability & Y-axis rounded numbers
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                ) {
                                    Canvas(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .pointerInput(dayItems) {
                                                detectTapGestures { offset ->
                                                    if (dayItems.isNotEmpty()) {
                                                        val yAxisWidth = 36.dp.toPx()
                                                        val chartWidth = size.width - yAxisWidth
                                                        if (offset.x >= yAxisWidth) {
                                                            val barAreaX = offset.x - yAxisWidth
                                                            val slotW = chartWidth / dayItems.size
                                                            val idx = (barAreaX / slotW).toInt().coerceIn(0, dayItems.size - 1)
                                                            val clickedDay = dayItems[idx]
                                                            selectedDayKey = if (selectedDayKey == clickedDay.fullDateString) null else clickedDay.fullDateString
                                                        }
                                                    }
                                                }
                                            }
                                    ) {
                                        val totalWidth = size.width
                                        val totalHeight = size.height
                                        val yAxisWidth = 36.dp.toPx()
                                        val bottomAxisH = 24.dp.toPx()
                                        val topPadding = 10.dp.toPx()
                                        val chartAreaH = totalHeight - bottomAxisH - topPadding
                                        val chartAreaW = totalWidth - yAxisWidth
                                        val baselineY = totalHeight - bottomAxisH

                                        val count = dayItems.size.coerceAtLeast(1)
                                        val slotW = chartAreaW / count

                                        // Paints
                                        val axisPaint = android.graphics.Paint().apply {
                                            color = if (isDark) android.graphics.Color.GRAY else android.graphics.Color.DKGRAY
                                            textSize = 9.sp.toPx()
                                            textAlign = android.graphics.Paint.Align.RIGHT
                                            isAntiAlias = true
                                        }

                                        val xLabelPaint = android.graphics.Paint().apply {
                                            color = if (isDark) android.graphics.Color.LTGRAY else android.graphics.Color.GRAY
                                            textSize = 8.5.sp.toPx()
                                            textAlign = android.graphics.Paint.Align.CENTER
                                            isAntiAlias = true
                                        }

                                        val selectedXLabelPaint = android.graphics.Paint().apply {
                                            color = if (isDark) android.graphics.Color.WHITE else android.graphics.Color.BLACK
                                            textSize = 9.sp.toPx()
                                            textAlign = android.graphics.Paint.Align.CENTER
                                            isFakeBoldText = true
                                            isAntiAlias = true
                                        }

                                        // 1. Draw horizontal grid lines & rounded Y-axis labels
                                        niceTicks.forEach { tick ->
                                            val ratio = (tick / chartMax).toFloat()
                                            val y = baselineY - (ratio * chartAreaH)
                                            // Grid line
                                            drawLine(
                                                color = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.07f),
                                                start = Offset(yAxisWidth, y),
                                                end = Offset(totalWidth, y),
                                                strokeWidth = 1.dp.toPx()
                                            )
                                            // Y label
                                            val labelStr = formatRoundedAxisNumber(tick)
                                            drawContext.canvas.nativeCanvas.drawText(
                                                labelStr,
                                                yAxisWidth - 6.dp.toPx(),
                                                y + 3.dp.toPx(),
                                                axisPaint
                                            )
                                        }

                                        // Baseline line
                                        drawLine(
                                            color = if (isDark) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.2f),
                                            start = Offset(yAxisWidth, baselineY),
                                            end = Offset(totalWidth, baselineY),
                                            strokeWidth = 1.dp.toPx()
                                        )

                                        // 2. Draw Bars for Each Day
                                        dayItems.forEachIndexed { i, day ->
                                            val centerX = yAxisWidth + slotW * i + slotW / 2f
                                            val isSelected = selectedDayKey == day.fullDateString

                                            // Draw selection highlight background beam if selected
                                            if (isSelected) {
                                                drawRoundRect(
                                                    color = primaryColor.copy(alpha = 0.16f),
                                                    topLeft = Offset(centerX - slotW / 2f + 1.dp.toPx(), topPadding),
                                                    size = Size(slotW - 2.dp.toPx(), chartAreaH + bottomAxisH),
                                                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                                                )
                                            }

                                            // Value and height based on graphMode
                                            val value = when (graphMode) {
                                                DailyGraphMode.EXPENSE -> day.expense
                                                DailyGraphMode.INCOME -> day.income
                                                DailyGraphMode.NET_FLOW -> abs(day.net)
                                            }

                                            if (value > 0) {
                                                val barH = (value / chartMax * chartAreaH).toFloat().coerceAtLeast(3.dp.toPx())
                                                val barW = (slotW * 0.58f).coerceIn(4.dp.toPx(), 22.dp.toPx())
                                                val barLeft = centerX - barW / 2f
                                                val barTop = baselineY - barH

                                                val barColor = when (graphMode) {
                                                    DailyGraphMode.EXPENSE -> if (isSelected) SolidPrimary else SolidExpense
                                                    DailyGraphMode.INCOME -> if (isSelected) SolidPrimary else SolidIncome
                                                    DailyGraphMode.NET_FLOW -> if (isSelected) SolidPrimary else if (day.net >= 0) SolidIncome else SolidExpense
                                                }

                                                drawRoundRect(
                                                    color = barColor,
                                                    topLeft = Offset(barLeft, barTop),
                                                    size = Size(barW, barH),
                                                    cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                                                )
                                            }

                                            // Draw X Axis label (draw every day if count <= 15, or every 2/3/5 days if many)
                                            val shouldDrawXLabel = when {
                                                count <= 14 -> true
                                                count <= 21 -> i % 2 == 0 || isSelected
                                                else -> i % 4 == 0 || i == count - 1 || isSelected
                                            }

                                            if (shouldDrawXLabel) {
                                                val p = if (isSelected) selectedXLabelPaint else xLabelPaint
                                                drawContext.canvas.nativeCanvas.drawText(
                                                    "${day.dateNum}",
                                                    centerX,
                                                    baselineY + 14.dp.toPx(),
                                                    p
                                                )
                                            }
                                        }
                                    }
                                }

                                // Legend row
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    when (graphMode) {
                                                        DailyGraphMode.EXPENSE -> SolidExpense
                                                        DailyGraphMode.INCOME -> SolidIncome
                                                        DailyGraphMode.NET_FLOW -> SolidIncome
                                                    }
                                                )
                                        )
                                        Text(
                                            text = if (languageMode == LanguageMode.BANGLA) graphMode.labelBn else graphMode.labelEn,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    if (dailyAvgExpense > 0 && graphMode == DailyGraphMode.EXPENSE) {
                                        Text(
                                            text = "${if (languageMode == LanguageMode.BANGLA) "গড়: " else "Avg: "}${LanguageHelper.formatCurrency(dailyAvgExpense, languageMode)}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Tapped Day Detailed Inspector (Directly below chart)
                    if (focusedDayItem != null) {
                        item(key = "tapped_day_inspector") {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
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
                                                modifier = Modifier.size(34.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = "${focusedDayItem.dateNum}",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 14.sp,
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
                                                    text = "${focusedDayItem.dayOfWeekFull} • ${focusedDayItem.transactions.size} ${if (languageMode == LanguageMode.BANGLA) "টি লেনদেন" else "transactions"}",
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
                                            Text(
                                                text = if (languageMode == LanguageMode.BANGLA) "ব্যয়" else "Expense",
                                                fontSize = 10.sp,
                                                color = SolidExpense
                                            )
                                            Text(
                                                text = "-${LanguageHelper.formatCurrency(focusedDayItem.expense, languageMode)}",
                                                fontSize = 13.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SolidExpense
                                            )
                                        }
                                        Column {
                                            Text(
                                                text = if (languageMode == LanguageMode.BANGLA) "আয়" else "Income",
                                                fontSize = 10.sp,
                                                color = SolidIncome
                                            )
                                            Text(
                                                text = "+${LanguageHelper.formatCurrency(focusedDayItem.income, languageMode)}",
                                                fontSize = 13.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SolidIncome
                                            )
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = if (languageMode == LanguageMode.BANGLA) "নেট প্রবাহ" else "Net Flow",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            val netColor = if (focusedDayItem.net >= 0) SolidIncome else SolidExpense
                                            Text(
                                                text = if (focusedDayItem.net >= 0) "+${LanguageHelper.formatCurrency(focusedDayItem.net, languageMode)}" else LanguageHelper.formatCurrency(focusedDayItem.net, languageMode),
                                                fontSize = 13.5.sp,
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
                                            Text(
                                                text = if (languageMode == LanguageMode.BANGLA) "নিচের টেবিলে লেনদেন দেখুন" else "View in Table Below ↓",
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // SECTION 4: Table View Header & Controls
                    item(key = "section_table_header") {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "দৈনিক হিসাবের টেবিল" else "Daily Summary Table",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                // Sort Selector
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
                                                imageVector = Icons.Default.Sort,
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

                            // Table Filter Chips (All Days | Spending Only | No-Spend)
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

                            // Table Search Bar
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

                            // Table Column Headers Row
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "তারিখ" else "Date",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.weight(1.3f)
                                    )
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "ব্যয়" else "Expense",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SolidExpense,
                                        textAlign = TextAlign.End,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "আয়" else "Income",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SolidIncome,
                                        textAlign = TextAlign.End,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "নেট" else "Net",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.End,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    // SECTION 4 (Continued): Table Rows
                    if (displayedDayItems.isEmpty()) {
                        item(key = "empty_table") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "কোনো তথ্য পাওয়া যায়নি" else "No entries found",
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
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelectedOnChart) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                                    else MaterialTheme.colorScheme.surface
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelectedOnChart) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                    else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateContentSize(animationSpec = tween(200))
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    // Row data
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                expandedDayKeys[day.fullDateString] = !isExpanded
                                                selectedDayKey = day.fullDateString
                                            }
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Date Column
                                        Row(
                                            modifier = Modifier.weight(1.3f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (day.isToday) MaterialTheme.colorScheme.primary
                                                else if (day.expense == 0.0) SolidIncome.copy(alpha = 0.15f)
                                                else MaterialTheme.colorScheme.surfaceVariant,
                                                modifier = Modifier.size(26.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = "${day.dateNum}",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (day.isToday) MaterialTheme.colorScheme.onPrimary
                                                        else if (day.expense == 0.0) SolidIncome
                                                        else MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Column {
                                                Text(
                                                    text = day.dayOfWeekShort,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = if (day.isWeekend) SolidExpense else MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "${day.transactions.size} txs",
                                                    fontSize = 9.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        // Expense Column
                                        Text(
                                            text = if (day.expense > 0) "-${LanguageHelper.formatCurrency(day.expense, languageMode)}" else "—",
                                            fontSize = 11.5.sp,
                                            fontWeight = if (day.expense > 0) FontWeight.Bold else FontWeight.Normal,
                                            color = if (day.expense > 0) SolidExpense else MaterialTheme.colorScheme.outline,
                                            textAlign = TextAlign.End,
                                            modifier = Modifier.weight(1f)
                                        )

                                        // Income Column
                                        Text(
                                            text = if (day.income > 0) "+${LanguageHelper.formatCurrency(day.income, languageMode)}" else "—",
                                            fontSize = 11.5.sp,
                                            fontWeight = if (day.income > 0) FontWeight.Bold else FontWeight.Normal,
                                            color = if (day.income > 0) SolidIncome else MaterialTheme.colorScheme.outline,
                                            textAlign = TextAlign.End,
                                            modifier = Modifier.weight(1f)
                                        )

                                        // Net Column
                                        val netColor = when {
                                            day.net > 0 -> SolidIncome
                                            day.net < 0 -> SolidExpense
                                            else -> MaterialTheme.colorScheme.outline
                                        }
                                        Text(
                                            text = when {
                                                day.net > 0 -> "+${LanguageHelper.formatCurrency(day.net, languageMode)}"
                                                day.net < 0 -> LanguageHelper.formatCurrency(day.net, languageMode)
                                                else -> "—"
                                            },
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = netColor,
                                            textAlign = TextAlign.End,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }

                                    // Inline transactions expander
                                    if (isExpanded && day.transactions.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                                        Spacer(modifier = Modifier.height(4.dp))

                                        day.transactions.forEach { txItem ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .clickable { onTransactionClick?.invoke(txItem) }
                                                    .padding(vertical = 5.dp, horizontal = 4.dp),
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
                                                            fontSize = 11.5.sp,
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
                                                                fontSize = 9.5.sp,
                                                                color = if (onAccountClick != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                                modifier = if (onAccountClick != null) Modifier.clickable { onAccountClick(txAccount) } else Modifier
                                                            )
                                                        }
                                                    }
                                                }

                                                Text(
                                                    text = (if (txItem.transaction.type == TransactionType.EXPENSE) "-" else "+") +
                                                            LanguageHelper.formatCurrency(txItem.transaction.amount, languageMode),
                                                    fontSize = 12.sp,
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
                fontSize = 15.sp,
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
