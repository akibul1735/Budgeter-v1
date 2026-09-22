package com.example.ui.screens.settings

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.LanguageMode
import com.example.data.model.TransactionType
import com.example.ui.viewmodel.BudgetViewModel
import com.example.util.WidgetPreferences
import com.example.widget.BudgetMeterWidgetProvider
import com.example.widget.QuickActionWidgetProvider
import com.example.widget.RecentTransactionsWidgetProvider
import com.example.widget.SavingsGoalWidgetProvider
import com.example.widget.WidgetUpdateHelper
import kotlinx.coroutines.launch
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetsSettingsPage(
    viewModel: BudgetViewModel,
    languageMode: LanguageMode,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val widgetPrefs = remember { WidgetPreferences.getInstance(context) }
    val isPrivacyEnabled by widgetPrefs.isPrivacyEnabled.collectAsState()

    val overview by viewModel.financialOverview.collectAsStateWithLifecycle()
    val currencyConfig by viewModel.currencyConfig.collectAsStateWithLifecycle()
    val recentTransactions by viewModel.transactionsWithDetails.collectAsStateWithLifecycle()
    val savingsGoals by viewModel.savingsGoalsWithDetails.collectAsStateWithLifecycle()
    val topGoal = savingsGoals.firstOrNull { !it.goal.isCompleted }

    val currencySymbol = currencyConfig.activeSymbol
    val decimalFormat = remember { DecimalFormat("#,##0.00") }
    val compactFormat = remember { DecimalFormat("#,##0") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Widgets,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "হোম স্ক্রিন উইজেট" else "Home Screen Widgets",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        WidgetUpdateHelper.updateAllWidgets(context)
                        Toast.makeText(context, "Widgets refreshed", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Refresh Widgets",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Intro & Instructions Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "প্রিমিয়াম হোম স্ক্রিন উইজেট" else "Premium Home Widgets",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA)
                                    "আপনার ফোনের হোম স্ক্রিন থেকে দ্রুত লেনদেন রেকর্ড করুন, রিয়েল-টাইম বাজেট ও লক্ষ্য অগ্রগতি ট্র্যাক করুন। সরাসরি পিন করতে নিচের বোতামে চাপুন অথবা হোম স্ক্রিনে লং-প্রেস করে 'Widgets' নির্বাচন করুন।"
                                else
                                    "Track live balance, safe-to-spend limits, and record transactions directly from your home screen. Tap 'Pin Widget' below or long-press your home screen and select Budgeter from Widgets.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Global Widget Controls Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "ব্যালেন্স গোপনীয়তা মোড" else "Widget Privacy Mode",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA)
                                        "হোম স্ক্রিন উইজেটে টাকার পরিমাণ আড়াল করে বিন্দু (••••) দেখাবে"
                                    else
                                        "Mask sensitive balance numbers with dots on home screen widgets",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = isPrivacyEnabled,
                                onCheckedChange = {
                                    widgetPrefs.setPrivacyEnabled(it)
                                    WidgetUpdateHelper.updateAllWidgets(context)
                                }
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "সব উইজেট সিঙ্ক করুন" else "Sync All Widgets",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "হোম স্ক্রিনে সর্বশেষ ডেটা তাৎক্ষণিক আপডেট করুন" else "Force immediate update of all active widgets",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            OutlinedButton(
                                onClick = {
                                    WidgetUpdateHelper.updateAllWidgets(context)
                                    Toast.makeText(context, "Widgets synced successfully", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (languageMode == LanguageMode.BANGLA) "সিঙ্ক" else "Sync")
                            }
                        }
                    }
                }
            }

            // WIDGET 1: Quick Action & Balance Hub (4x2)
            item {
                WidgetPreviewCard(
                    title = if (languageMode == LanguageMode.BANGLA) "কুইক অ্যাকশন ও ব্যালেন্স হাব (৪x২)" else "Quick Action & Balance Hub (4x2)",
                    description = if (languageMode == LanguageMode.BANGLA)
                        "নেট ব্যালেন্স, চলতি মাসের আয় ও ব্যয়ের হিসাব এবং ১-ট্যাপে খরচ, আয় ও ট্রান্সফার যোগ করার বোতাম।"
                    else
                        "Real-time net balance, monthly income & expenses, and 1-tap rapid entry buttons.",
                    onPinWidget = {
                        pinWidget(context, QuickActionWidgetProvider::class.java)
                    }
                ) {
                    // Live Mockup of Quick Action Widget
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFF1A1C1E))
                            .border(1.dp, Color(0xFF2D3135), RoundedCornerShape(18.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            // Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.PieChart,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Budgeter",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Row {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = Color(0xFF94A3B8),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        tint = Color(0xFF94A3B8),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Balance & Monthly Stats
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "NET BALANCE",
                                        color = Color(0xFF64748B),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (isPrivacyEnabled) "••••••" else "$currencySymbol ${decimalFormat.format(overview.netWorth)}",
                                        color = Color.White,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.TrendingUp,
                                            contentDescription = null,
                                            tint = Color(0xFF10B981),
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isPrivacyEnabled) "+$currencySymbol •••" else "+$currencySymbol ${compactFormat.format(overview.monthlyIncome)}",
                                            color = Color(0xFF10B981),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.TrendingDown,
                                            contentDescription = null,
                                            tint = Color(0xFFEF4444),
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isPrivacyEnabled) "-$currencySymbol •••" else "-$currencySymbol ${compactFormat.format(overview.monthlyExpense)}",
                                            color = Color(0xFFEF4444),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Quick Action Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                MockActionButton(
                                    modifier = Modifier.weight(1f),
                                    text = "Expense",
                                    icon = Icons.Default.Add,
                                    color = Color(0xFFEF4444)
                                )
                                MockActionButton(
                                    modifier = Modifier.weight(1f),
                                    text = "Income",
                                    icon = Icons.Default.Add,
                                    color = Color(0xFF10B981)
                                )
                                MockActionButton(
                                    modifier = Modifier.weight(1f),
                                    text = "⇄ Transfer",
                                    icon = null,
                                    color = Color(0xFF38BDF8)
                                )
                            }
                        }
                    }
                }
            }

            // WIDGET 2: Monthly Budget & Safe-to-Spend Meter (4x2)
            item {
                WidgetPreviewCard(
                    title = if (languageMode == LanguageMode.BANGLA) "বাজেট ও সেইফ-টু-স্পেন্ড মিটার (৪x২)" else "Budget & Safe-to-Spend Meter (4x2)",
                    description = if (languageMode == LanguageMode.BANGLA)
                        "চলতি মাসের বাজেট খরচ, ব্যবহৃত শতাংশের প্রগ্রেস বার এবং দৈনিক নিরাপদ ব্যয়ের সীমা।"
                    else
                        "Monthly budget consumption, dynamic progress track, and daily safe-to-spend allowance.",
                    onPinWidget = {
                        pinWidget(context, BudgetMeterWidgetProvider::class.java)
                    }
                ) {
                    val spent = overview.monthlyExpense
                    val totalBudget = overview.totalExpenseBudget
                    val percent = if (totalBudget > 0.0) ((spent / totalBudget) * 100.0).toInt() else 0
                    val remaining = (totalBudget - spent).coerceAtLeast(0.0)
                    val safePerDay = remaining / 30.0

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFF1A1C1E))
                            .border(1.dp, Color(0xFF2D3135), RoundedCornerShape(18.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "MONTHLY BUDGET",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF23282C))
                                        .border(1.dp, Color(0xFF363C42), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (percent > 100) "Over Budget" else if (percent >= 85) "Warning" else "On Track",
                                        color = if (percent > 100) Color(0xFFEF4444) else if (percent >= 85) Color(0xFFF59E0B) else Color(0xFF10B981),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = "$currencySymbol ${compactFormat.format(spent)}",
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "/ $currencySymbol ${compactFormat.format(totalBudget)}",
                                        color = Color(0xFF64748B),
                                        fontSize = 13.sp
                                    )
                                }
                                Text(
                                    text = "$percent%",
                                    color = Color(0xFF10B981),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            LinearProgressIndicator(
                                progress = { (percent.toFloat() / 100f).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = if (percent > 100) Color(0xFFEF4444) else Color(0xFF10B981),
                                trackColor = Color(0xFF2A2F35)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "SAFE TO SPEND / DAY",
                                        color = Color(0xFF64748B),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "$currencySymbol ${compactFormat.format(safePerDay)} / day",
                                        color = Color(0xFF38BDF8),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF282E33))
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            tint = Color(0xFF10B981),
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Add",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // WIDGET 3: Recent Transactions Live Feed (4x3)
            item {
                WidgetPreviewCard(
                    title = if (languageMode == LanguageMode.BANGLA) "লাইভ সাম্প্রতিক লেনদেন ফিড (৪x৩)" else "Recent Transactions Feed (4x3)",
                    description = if (languageMode == LanguageMode.BANGLA)
                        "হোম স্ক্রিনে সরাসরি স্ক্রোলযোগ্য সাম্প্রতিক লেনদেনের তালিকা ও ব্যয়ের বিবরণ।"
                    else
                        "Scrollable live feed of your latest transactions with category info and color-coded amounts.",
                    onPinWidget = {
                        pinWidget(context, RecentTransactionsWidgetProvider::class.java)
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFF1A1C1E))
                            .border(1.dp, Color(0xFF2D3135), RoundedCornerShape(18.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "RECENT TRANSACTIONS",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Row {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        tint = Color(0xFF94A3B8),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            val displayList = recentTransactions.take(3)
                            if (displayList.isEmpty()) {
                                Text(
                                    text = "No recent transactions",
                                    color = Color(0xFF64748B),
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(vertical = 12.dp)
                                )
                            } else {
                                displayList.forEach { txItem ->
                                    val tx = txItem.transaction
                                    val title = tx.payeeOrPayer.ifBlank { txItem.category?.nameEn ?: (if (tx.note.isNotBlank()) tx.note else "Transaction") }
                                    val isIncome = tx.type == TransactionType.INCOME
                                    val isTransfer = tx.type == TransactionType.TRANSFER

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = if (isIncome) Icons.Default.TrendingUp else if (isTransfer) Icons.Default.Refresh else Icons.Default.TrendingDown,
                                                contentDescription = null,
                                                tint = if (isIncome) Color(0xFF10B981) else if (isTransfer) Color(0xFF38BDF8) else Color(0xFFEF4444),
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = title,
                                                    color = Color.White,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                Text(
                                                    text = txItem.debitAccount?.nameEn ?: txItem.creditAccount?.nameEn ?: "Account",
                                                    color = Color(0xFF64748B),
                                                    fontSize = 10.sp
                                                )
                                            }
                                        }

                                        Text(
                                            text = if (isPrivacyEnabled) "•••" else "${if (isIncome) "+" else if (isTransfer) "⇄" else "-"}$currencySymbol ${decimalFormat.format(tx.amount)}",
                                            color = if (isIncome) Color(0xFF10B981) else if (isTransfer) Color(0xFF38BDF8) else Color(0xFFEF4444),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // WIDGET 4: Savings Goal Tracker (2x2)
            item {
                WidgetPreviewCard(
                    title = if (languageMode == LanguageMode.BANGLA) "সঞ্চয় লক্ষ্য ট্র্যাকার (২x২)" else "Active Savings Goal Tracker (2x2)",
                    description = if (languageMode == LanguageMode.BANGLA)
                        "আপনার প্রধান সক্রিয় সঞ্চয় লক্ষ্যের অগ্রগতি, জমানো পরিমাণ এবং বাকি টাকার সার্বক্ষণিক ট্র্যাকিং।"
                    else
                        "Compact 2x2 widget monitoring your primary active savings goal target and accumulated amount.",
                    onPinWidget = {
                        pinWidget(context, SavingsGoalWidgetProvider::class.java)
                    }
                ) {
                    val goalTitle = topGoal?.goal?.name ?: "Emergency Fund"
                    val goalTarget = topGoal?.goal?.targetAmount ?: 50000.0
                    val goalSaved = topGoal?.effectiveSaved ?: 15000.0
                    val goalPercent = if (goalTarget > 0) ((goalSaved / goalTarget) * 100.0).toInt().coerceIn(0, 100) else 0

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFF1A1C1E))
                            .border(1.dp, Color(0xFF2D3135), RoundedCornerShape(18.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "SAVINGS GOAL",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    imageVector = Icons.Default.Savings,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = goalTitle,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = "$currencySymbol ${compactFormat.format(goalSaved)}",
                                        color = Color(0xFF10B981),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "/ $currencySymbol ${compactFormat.format(goalTarget)}",
                                        color = Color(0xFF64748B),
                                        fontSize = 11.sp
                                    )
                                }
                                Text(
                                    text = "$goalPercent%",
                                    color = Color(0xFF38BDF8),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            LinearProgressIndicator(
                                progress = { (goalPercent.toFloat() / 100f).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = Color(0xFF10B981),
                                trackColor = Color(0xFF2A2F35)
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "$currencySymbol ${compactFormat.format(goalTarget - goalSaved)} to go",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun WidgetPreviewCard(
    title: String,
    description: String,
    onPinWidget: () -> Unit,
    previewContent: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Widget Interactive Mockup
            previewContent()

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onPinWidget,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.PushPin,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Pin to Home Screen", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun MockActionButton(
    modifier: Modifier = Modifier,
    text: String,
    icon: ImageVector?,
    color: Color
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF282E33))
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
            }
            Text(
                text = text,
                color = color,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun pinWidget(context: Context, providerClass: Class<*>) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val myProvider = ComponentName(context, providerClass)

        if (appWidgetManager.isRequestPinAppWidgetSupported) {
            appWidgetManager.requestPinAppWidget(myProvider, null, null)
            Toast.makeText(context, "Adding widget to home screen...", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(
                context,
                "Your launcher does not support direct pinning. Please long-press on your home screen and choose 'Widgets' -> 'Budgeter'.",
                Toast.LENGTH_LONG
            ).show()
        }
    } else {
        Toast.makeText(
            context,
            "Please long-press on your home screen and choose 'Widgets' -> 'Budgeter'.",
            Toast.LENGTH_LONG
        ).show()
    }
}
