package com.example.ui.screens.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LanguageMode
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.data.model.TransactionWithDetails
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidExpenseContainer
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidIncomeContainer
import com.example.ui.theme.SolidPrimary
import com.example.ui.theme.SolidPrimaryContainer
import com.example.ui.theme.SolidTransfer
import com.example.util.CalendarDisplayMode
import com.example.util.DateUtils
import com.example.util.IconHelper
import com.example.util.LanguageHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class CalendarDayInfo(
    val dayOfMonth: Int,
    val dateEpochMs: Long,
    val isCurrentMonth: Boolean,
    val isToday: Boolean,
    val income: Double,
    val expense: Double,
    val transactions: List<TransactionWithDetails>
)

@Composable
fun CalendarSummaryCard(
    transactions: List<TransactionWithDetails>,
    displayMode: CalendarDisplayMode,
    showIncome: Boolean,
    showExpense: Boolean,
    languageMode: LanguageMode,
    onOpenSettings: () -> Unit,
    onTransactionClick: (Transaction) -> Unit = {}
) {
    val todayCal = Calendar.getInstance()
    val todayYear = todayCal.get(Calendar.YEAR)
    val todayMonth = todayCal.get(Calendar.MONTH)
    val todayDay = todayCal.get(Calendar.DAY_OF_MONTH)

    var currentViewCal by remember { mutableStateOf(Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, 1) }) }
    var selectedDayEpoch by remember { mutableStateOf<Long?>(null) }

    val viewYear = currentViewCal.get(Calendar.YEAR)
    val viewMonth = currentViewCal.get(Calendar.MONTH)

    val monthName = remember(viewYear, viewMonth) {
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.ENGLISH)
        sdf.format(currentViewCal.time)
    }

    // Build the grid of days for the month view
    val calendarDays = remember(viewYear, viewMonth, transactions) {
        val days = mutableListOf<CalendarDayInfo>()
        val cal = Calendar.getInstance()
        cal.set(viewYear, viewMonth, 1, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)

        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1=Sunday, 2=Monday...
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        // Previous month padding
        val prevCal = Calendar.getInstance()
        prevCal.set(viewYear, viewMonth, 1)
        prevCal.add(Calendar.MONTH, -1)
        val daysInPrevMonth = prevCal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val prevPadding = firstDayOfWeek - 1

        for (i in (daysInPrevMonth - prevPadding + 1)..daysInPrevMonth) {
            prevCal.set(Calendar.DAY_OF_MONTH, i)
            prevCal.set(Calendar.HOUR_OF_DAY, 0)
            prevCal.set(Calendar.MINUTE, 0)
            prevCal.set(Calendar.SECOND, 0)
            prevCal.set(Calendar.MILLISECOND, 0)
            val startMs = prevCal.timeInMillis
            prevCal.set(Calendar.HOUR_OF_DAY, 23)
            prevCal.set(Calendar.MINUTE, 59)
            prevCal.set(Calendar.SECOND, 59)
            val endMs = prevCal.timeInMillis

            val dayTxs = transactions.filter { it.transaction.dateEpochMs in startMs..endMs }
            val inc = dayTxs.filter { it.transaction.type == TransactionType.INCOME }.sumOf { it.transaction.amount }
            val exp = dayTxs.filter { it.transaction.type == TransactionType.EXPENSE }.sumOf { it.transaction.amount }

            days.add(
                CalendarDayInfo(
                    dayOfMonth = i,
                    dateEpochMs = startMs,
                    isCurrentMonth = false,
                    isToday = false,
                    income = inc,
                    expense = exp,
                    transactions = dayTxs
                )
            )
        }

        // Current month days
        for (day in 1..daysInMonth) {
            cal.set(viewYear, viewMonth, day, 0, 0, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val startMs = cal.timeInMillis
            cal.set(viewYear, viewMonth, day, 23, 59, 59)
            cal.set(Calendar.MILLISECOND, 999)
            val endMs = cal.timeInMillis

            val dayTxs = transactions.filter { it.transaction.dateEpochMs in startMs..endMs }
            val inc = dayTxs.filter { it.transaction.type == TransactionType.INCOME }.sumOf { it.transaction.amount }
            val exp = dayTxs.filter { it.transaction.type == TransactionType.EXPENSE }.sumOf { it.transaction.amount }
            val isToday = (viewYear == todayYear && viewMonth == todayMonth && day == todayDay)

            days.add(
                CalendarDayInfo(
                    dayOfMonth = day,
                    dateEpochMs = startMs,
                    isCurrentMonth = true,
                    isToday = isToday,
                    income = inc,
                    expense = exp,
                    transactions = dayTxs
                )
            )
        }

        // Next month padding to complete 7-day rows (multiples of 7)
        val remaining = (7 - (days.size % 7)) % 7
        val nextCal = Calendar.getInstance()
        nextCal.set(viewYear, viewMonth, 1)
        nextCal.add(Calendar.MONTH, 1)
        for (day in 1..remaining) {
            nextCal.set(Calendar.DAY_OF_MONTH, day)
            nextCal.set(Calendar.HOUR_OF_DAY, 0)
            nextCal.set(Calendar.MINUTE, 0)
            nextCal.set(Calendar.SECOND, 0)
            nextCal.set(Calendar.MILLISECOND, 0)
            val startMs = nextCal.timeInMillis
            nextCal.set(Calendar.HOUR_OF_DAY, 23)
            nextCal.set(Calendar.MINUTE, 59)
            nextCal.set(Calendar.SECOND, 59)
            val endMs = nextCal.timeInMillis

            val dayTxs = transactions.filter { it.transaction.dateEpochMs in startMs..endMs }
            val inc = dayTxs.filter { it.transaction.type == TransactionType.INCOME }.sumOf { it.transaction.amount }
            val exp = dayTxs.filter { it.transaction.type == TransactionType.EXPENSE }.sumOf { it.transaction.amount }

            days.add(
                CalendarDayInfo(
                    dayOfMonth = day,
                    dateEpochMs = startMs,
                    isCurrentMonth = false,
                    isToday = false,
                    income = inc,
                    expense = exp,
                    transactions = dayTxs
                )
            )
        }

        days
    }

    val selectedDayInfo = remember(selectedDayEpoch, calendarDays) {
        calendarDays.find { it.dateEpochMs == selectedDayEpoch }
    }

    // Monthly totals for top status
    val currentMonthDays = remember(calendarDays) { calendarDays.filter { it.isCurrentMonth } }
    val monthIncome = remember(currentMonthDays) { currentMonthDays.sumOf { it.income } }
    val monthExpense = remember(currentMonthDays) { currentMonthDays.sumOf { it.expense } }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("calendar_summary_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Month Selector & Settings Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = monthName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            val c = (currentViewCal.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
                            currentViewCal = c
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Previous Month",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = {
                            val c = (currentViewCal.clone() as Calendar).apply { add(Calendar.MONTH, 1) }
                            currentViewCal = c
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Next Month",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Calendar Settings",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Month summary badges: Total Income & Total Expense
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (showIncome) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(SolidIncomeContainer.copy(alpha = 0.6f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Inc: +${LanguageHelper.formatCurrency(monthIncome, languageMode)}",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF065F46)
                        )
                    }
                }

                if (showExpense) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(SolidExpenseContainer.copy(alpha = 0.6f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Exp: -${LanguageHelper.formatCurrency(monthExpense, languageMode)}",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF991B1B)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Weekday Headers
            val weekdays = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
            Row(modifier = Modifier.fillMaxWidth()) {
                weekdays.forEach { w ->
                    Text(
                        text = w,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.outline,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Month Days Grid (7 columns)
            val rows = calendarDays.chunked(7)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                rows.forEach { week ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        week.forEach { dayInfo ->
                            val isSelected = (dayInfo.dateEpochMs == selectedDayEpoch)
                            
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        when {
                                            isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
                                            dayInfo.isToday -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
                                            else -> Color.Transparent
                                        }
                                    )
                                    .then(
                                        if (dayInfo.isToday && !isSelected) {
                                            Modifier.border(1.dp, SolidPrimary.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                        } else Modifier
                                    )
                                    .clickable {
                                        selectedDayEpoch = if (selectedDayEpoch == dayInfo.dateEpochMs) null else dayInfo.dateEpochMs
                                    }
                                    .padding(2.dp),
                                contentAlignment = Alignment.TopCenter
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    // Day Number
                                    Text(
                                        text = "${dayInfo.dayOfMonth}",
                                        fontSize = 11.sp,
                                        fontWeight = if (dayInfo.isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = when {
                                            isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                                            dayInfo.isToday -> SolidPrimary
                                            !dayInfo.isCurrentMonth -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                            else -> MaterialTheme.colorScheme.onSurface
                                        }
                                    )

                                    if (displayMode == CalendarDisplayMode.AMOUNTS) {
                                        // Income Text
                                        if (showIncome && dayInfo.income > 0) {
                                            Text(
                                                text = "+${formatCompact(dayInfo.income)}",
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF10B981),
                                                maxLines = 1,
                                                overflow = TextOverflow.Clip
                                            )
                                        }
                                        // Expense Text
                                        if (showExpense && dayInfo.expense > 0) {
                                            Text(
                                                text = "-${formatCompact(dayInfo.expense)}",
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFEF4444),
                                                maxLines = 1,
                                                overflow = TextOverflow.Clip
                                            )
                                        }
                                    } else {
                                        // Color Dots Mode
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(top = 4.dp)
                                        ) {
                                            if (showIncome && dayInfo.income > 0) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(5.dp)
                                                        .clip(CircleShape)
                                                        .background(Color(0xFF10B981))
                                                )
                                            }
                                            if (showExpense && dayInfo.expense > 0) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(5.dp)
                                                        .clip(CircleShape)
                                                        .background(Color(0xFFEF4444))
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

            // Selected Day Transactions Preview
            AnimatedVisibility(visible = selectedDayInfo != null) {
                selectedDayInfo?.let { day ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(12.dp)
                    ) {
                        val sdf = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.ENGLISH)
                        val fullDateStr = sdf.format(Date(day.dateEpochMs))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = fullDateStr,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${day.transactions.size} tx",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (day.income > 0) {
                                Text(
                                    text = "Income: +${LanguageHelper.formatCurrency(day.income, languageMode)}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF10B981)
                                )
                            }
                            if (day.expense > 0) {
                                Text(
                                    text = "Expense: -${LanguageHelper.formatCurrency(day.expense, languageMode)}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFEF4444)
                                )
                            }
                        }

                        if (day.transactions.isNotEmpty()) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 6.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                day.transactions.forEach { txItem ->
                                    val tx = txItem.transaction
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable { onTransactionClick(tx) }
                                            .padding(vertical = 4.dp, horizontal = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                            val iconName = txItem.category?.iconName ?: if (tx.type == TransactionType.TRANSFER) "SwapHoriz" else "Payments"
                                            Icon(
                                                imageVector = IconHelper.getIconByName(iconName),
                                                contentDescription = null,
                                                tint = when (tx.type) {
                                                    TransactionType.EXPENSE -> SolidExpense
                                                    TransactionType.INCOME -> SolidIncome
                                                    TransactionType.TRANSFER -> SolidTransfer
                                                },
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = txItem.category?.localizedName(languageMode) ?: tx.type.name,
                                                fontSize = 12.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        val isPositiveEffect = when (tx.type) {
                                            TransactionType.EXPENSE -> tx.amount < 0
                                            TransactionType.INCOME -> tx.amount >= 0
                                            TransactionType.TRANSFER -> false
                                        }
                                        val isNegativeEffect = when (tx.type) {
                                            TransactionType.EXPENSE -> tx.amount >= 0
                                            TransactionType.INCOME -> tx.amount < 0
                                            TransactionType.TRANSFER -> false
                                        }
                                        val sign = when {
                                            tx.type == TransactionType.TRANSFER -> ""
                                            isPositiveEffect -> "+"
                                            isNegativeEffect -> "-"
                                            else -> ""
                                        }
                                        val amtColor = when {
                                            tx.type == TransactionType.TRANSFER -> SolidTransfer
                                            isPositiveEffect -> SolidIncome
                                            else -> SolidExpense
                                        }
                                        Text(
                                            text = "$sign${LanguageHelper.formatCurrency(Math.abs(tx.amount), languageMode)}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = amtColor
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

private fun formatCompact(amount: Double): String {
    return when {
        amount >= 1_000_000 -> String.format(Locale.US, "%.1fM", amount / 1_000_000)
        amount >= 1_000 -> String.format(Locale.US, "%.1fk", amount / 1_000)
        else -> "${amount.toInt()}"
    }
}
