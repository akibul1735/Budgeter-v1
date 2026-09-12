package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.UnfoldLess
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.activity.compose.BackHandler
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Category
import com.example.data.model.CategoryType
import com.example.data.model.LanguageMode
import com.example.data.model.Transaction
import com.example.data.model.TransactionStatus
import com.example.ui.components.BudgetTimelineTable
import com.example.ui.components.ExportMenuButton
import com.example.ui.components.LocalSetTimelineActive
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidPrimary
import com.example.util.CategoryTimelineData
import com.example.util.CategoryTimelineGroup
import com.example.util.CategoryTimelineHelper
import com.example.util.CategoryTimelineInterval
import com.example.util.CategoryTimelinePeriod
import com.example.util.CategoryTimelineRow
import com.example.util.CategoryTimelineSortOrder
import com.example.util.DateUtils
import com.example.util.ExportFormat
import com.example.util.IconHelper
import com.example.util.LanguageHelper
import com.example.util.TabExportHelper
import java.util.Calendar

data class CategoryTimelineFilterState(
    val interval: CategoryTimelineInterval = CategoryTimelineInterval.PAST_12_MONTHS,
    val customPeriodsCount: Int = 12,
    val customStartDateMs: Long? = null,
    val customEndDateMs: Long? = null,
    val selectedCategoryIds: Set<Long> = emptySet(),
    val selectedStatusSet: Set<TransactionStatus> = emptySet(),
    val excludeZeroAmounts: Boolean = false,
    val sortOrder: CategoryTimelineSortOrder = CategoryTimelineSortOrder.DEFAULT
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryTimelineScreen(
    categories: List<Category>,
    transactions: List<Transaction>,
    languageMode: LanguageMode,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    // Disable pager tab swipe when timeline is active
    val setTimelineActive = LocalSetTimelineActive.current
    DisposableEffect(Unit) {
        setTimelineActive(true)
        onDispose {
            setTimelineActive(false)
        }
    }

    BackHandler(enabled = true) {
        onBack()
    }

    var filterState by remember { mutableStateOf(CategoryTimelineFilterState()) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var activeViewTab by remember { mutableStateOf(0) } // 0: All, 1: Expenses, 2: Income
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    // Expanded states for category groups (default: expanded)
    val expandedGroupMap = remember { mutableStateMapOf<String, Boolean>() }

    // Calculate timeline data based on filters
    val timelineData = remember(
        categories,
        transactions,
        filterState,
        searchQuery,
        languageMode
    ) {
        CategoryTimelineHelper.calculateTimeline(
            categories = categories,
            transactions = transactions,
            interval = filterState.interval,
            customPeriodsCount = filterState.customPeriodsCount,
            customStartDateMs = filterState.customStartDateMs,
            customEndDateMs = filterState.customEndDateMs,
            selectedCategoryIds = filterState.selectedCategoryIds,
            selectedStatusSet = filterState.selectedStatusSet,
            excludeZeroAmounts = filterState.excludeZeroAmounts,
            searchQuery = searchQuery,
            sortOrder = filterState.sortOrder,
            languageMode = languageMode
        )
    }

    val horizontalScrollState = rememberScrollState()

    // Filter Dialog Component
    if (showFilterDialog) {
        CategoryTimelineFilterDialog(
            currentFilter = filterState,
            categories = categories,
            languageMode = languageMode,
            onDismiss = { showFilterDialog = false },
            onApply = { newFilter ->
                filterState = newFilter
                showFilterDialog = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("category_timeline_screen")
    ) {
        // --- 1. Top Header Bar ---
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("timeline_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "বাজেট টাইমলাইন" else "Budget Timeline",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        val periodSummaryText = if (timelineData.periods.isNotEmpty()) {
                            val firstLbl = timelineData.periods.first().shortLabel
                            val lastLbl = timelineData.periods.last().shortLabel
                            "$firstLbl → $lastLbl (${timelineData.periods.size} ${if (languageMode == LanguageMode.BANGLA) "মাস" else "periods"})"
                        } else {
                            if (languageMode == LanguageMode.BANGLA) filterState.interval.titleBn else filterState.interval.titleEn
                        }
                        Text(
                            text = periodSummaryText,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Search Button
                    IconButton(
                        onClick = {
                            isSearchActive = !isSearchActive
                            if (!isSearchActive) searchQuery = ""
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "Search",
                            tint = if (isSearchActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Filter Button with Badge
                    val activeFilterCount = (if (filterState.interval != CategoryTimelineInterval.PAST_12_MONTHS) 1 else 0) +
                            (if (filterState.selectedCategoryIds.isNotEmpty()) 1 else 0) +
                            (if (filterState.selectedStatusSet.isNotEmpty()) 1 else 0) +
                            (if (filterState.excludeZeroAmounts) 1 else 0) +
                            (if (filterState.sortOrder != CategoryTimelineSortOrder.DEFAULT) 1 else 0)

                    IconButton(
                        onClick = { showFilterDialog = true },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("timeline_filter_btn")
                    ) {
                        BadgedBox(badge = {
                            if (activeFilterCount > 0) {
                                Badge { Text(activeFilterCount.toString()) }
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.FilterAlt,
                                contentDescription = "Filter",
                                tint = if (activeFilterCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Export Menu Button
                    ExportMenuButton(
                        onExport = { format ->
                            TabExportHelper.exportCategoryTimeline(
                                context = context,
                                format = format,
                                data = timelineData,
                                languageMode = languageMode
                            )
                        },
                        languageMode = languageMode
                    )
                }

                // Search Bar Expandable
                AnimatedVisibility(visible = isSearchActive) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        placeholder = {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "ক্যাটাগরি খুঁজুন..." else "Search categories...",
                                fontSize = 12.sp
                            )
                        },
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                                }
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                        )
                    )
                }

                // Quick Interval Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(top = 4.dp, bottom = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val intervals = listOf(
                        CategoryTimelineInterval.PAST_12_MONTHS,
                        CategoryTimelineInterval.PAST_6_MONTHS,
                        CategoryTimelineInterval.PAST_3_MONTHS,
                        CategoryTimelineInterval.QUARTERLY,
                        CategoryTimelineInterval.YEARLY,
                        CategoryTimelineInterval.CUSTOM
                    )

                    intervals.forEach { interval ->
                        val isSelected = filterState.interval == interval
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                if (interval == CategoryTimelineInterval.CUSTOM) {
                                    showFilterDialog = true
                                } else {
                                    filterState = filterState.copy(interval = interval)
                                }
                            },
                            label = {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) interval.titleBn else interval.titleEn,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.primary
                            ),
                            border = if (isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }
        }

        // --- 2. Summary Metric Cards ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Total Income Card
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, SolidIncome.copy(alpha = 0.25f))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(SolidIncome)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "মোট আয়" else "Total Income",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = LanguageHelper.formatCurrency(timelineData.grandTotalIncome, languageMode),
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = SolidIncome,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${if (languageMode == LanguageMode.BANGLA) "গড়: " else "Avg: "}${LanguageHelper.formatCurrency(timelineData.avgIncomePerPeriod, languageMode)}",
                        fontSize = 9.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Total Expense Card
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, SolidExpense.copy(alpha = 0.25f))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(SolidExpense)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "মোট ব্যয়" else "Total Expense",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = LanguageHelper.formatCurrency(timelineData.grandTotalExpense, languageMode),
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = SolidExpense,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${if (languageMode == LanguageMode.BANGLA) "গড়: " else "Avg: "}${LanguageHelper.formatCurrency(timelineData.avgExpensePerPeriod, languageMode)}",
                        fontSize = 9.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Net Savings Card
            val isNetPositive = timelineData.grandNetSavings >= 0
            val netColor = if (isNetPositive) SolidIncome else SolidExpense
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, SolidPrimary.copy(alpha = 0.25f))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(SolidPrimary)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "নেট উদ্বৃত্ত" else "Net Surplus",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = (if (isNetPositive) "+" else "") + LanguageHelper.formatCurrency(timelineData.grandNetSavings, languageMode),
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = netColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${if (languageMode == LanguageMode.BANGLA) "গড়: " else "Avg: "}${LanguageHelper.formatCurrency(timelineData.avgNetSavingsPerPeriod, languageMode)}",
                        fontSize = 9.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // --- 3. View Mode Tab Row & Expand/Collapse All ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TabRow(
                selectedTabIndex = activeViewTab,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp)),
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                contentColor = SolidPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[activeViewTab]),
                        color = SolidPrimary
                    )
                }
            ) {
                val tabLabels = listOf(
                    if (languageMode == LanguageMode.BANGLA) "সকল (${timelineData.expenseGroups.size + timelineData.incomeGroups.size})" else "All (${timelineData.expenseGroups.size + timelineData.incomeGroups.size})",
                    if (languageMode == LanguageMode.BANGLA) "ব্যয় (${timelineData.expenseGroups.size})" else "Expenses (${timelineData.expenseGroups.size})",
                    if (languageMode == LanguageMode.BANGLA) "আয় (${timelineData.incomeGroups.size})" else "Incomes (${timelineData.incomeGroups.size})"
                )
                tabLabels.forEachIndexed { idx, label ->
                    Tab(
                        selected = activeViewTab == idx,
                        onClick = { activeViewTab = idx },
                        text = {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (activeViewTab == idx) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Expand All / Collapse All button
            val allGroups = timelineData.expenseGroups + timelineData.incomeGroups
            val areAllExpanded = allGroups.isNotEmpty() && allGroups.all { grp ->
                val key = "${grp.type.name}_${grp.groupNameEn}"
                expandedGroupMap[key] != false
            }

            IconButton(
                onClick = {
                    val targetState = !areAllExpanded
                    allGroups.forEach { grp ->
                        val key = "${grp.type.name}_${grp.groupNameEn}"
                        expandedGroupMap[key] = targetState
                    }
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (areAllExpanded) Icons.Default.UnfoldLess else Icons.Default.UnfoldMore,
                    contentDescription = if (areAllExpanded) "Collapse All" else "Expand All",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // --- 4. The Category Timeline Matrix Table ---
        val displayedExpenseGroups = if (activeViewTab == 0 || activeViewTab == 1) timelineData.expenseGroups else emptyList()
        val displayedIncomeGroups = if (activeViewTab == 0 || activeViewTab == 2) timelineData.incomeGroups else emptyList()

        if (displayedExpenseGroups.isEmpty() && displayedIncomeGroups.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "কোনো ক্যাটাগরি তথ্য পাওয়া যায়নি" else "No category timeline data available",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "অন্যান্য সময়কাল নির্বাচন করুন বা ফিল্টার পরিবর্তন করুন" else "Try adjusting the date range or clearing filters",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        } else {
            BudgetTimelineTable(
                timelineData = timelineData,
                languageMode = languageMode,
                horizontalScrollState = horizontalScrollState,
                expandedGroupMap = expandedGroupMap,
                displayedExpenseGroups = displayedExpenseGroups,
                displayedIncomeGroups = displayedIncomeGroups,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryTimelineFilterDialog(
    currentFilter: CategoryTimelineFilterState,
    categories: List<Category>,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onApply: (CategoryTimelineFilterState) -> Unit
) {
    var tempFilter by remember { mutableStateOf(currentFilter) }
    var isIntervalMenuExpanded by remember { mutableStateOf(false) }
    var isSortMenuExpanded by remember { mutableStateOf(false) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Dialog Title Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "টাইমলাইন ফিল্টার ও সেটিংস" else "Timeline Filter & Settings",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.outline)
                    }
                }
                HorizontalDivider()

                // Content Column
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. Interval / Date Range Selection
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "১. সময়কাল নির্বাচন" else "1. Date Range & Interval",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Box {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isIntervalMenuExpanded = true },
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) tempFilter.interval.titleBn else tempFilter.interval.titleEn,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Icon(Icons.Default.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }

                        DropdownMenu(
                            expanded = isIntervalMenuExpanded,
                            onDismissRequest = { isIntervalMenuExpanded = false }
                        ) {
                            CategoryTimelineInterval.values().forEach { interval ->
                                val isSelected = tempFilter.interval == interval
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = if (languageMode == LanguageMode.BANGLA) interval.titleBn else interval.titleEn,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    onClick = {
                                        isIntervalMenuExpanded = false
                                        tempFilter = tempFilter.copy(interval = interval)
                                    }
                                )
                            }
                        }
                    }

                    // Custom Date Pickers if CUSTOM selected
                    if (tempFilter.interval == CategoryTimelineInterval.CUSTOM) {
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "কাস্টম শুরুর ও শেষ তারিখ:" else "Custom Date Bounds:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                // Start Date
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { showStartDatePicker = true }
                                        .background(MaterialTheme.colorScheme.surface)
                                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = if (languageMode == LanguageMode.BANGLA) "শুরুর তারিখ" else "Start Date",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                        val startLabel = tempFilter.customStartDateMs?.let { DateUtils.formatDate(it, languageMode) }
                                            ?: (if (languageMode == LanguageMode.BANGLA) "তারিখ বাছুন" else "Pick Date")
                                        Text(text = startLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Icon(Icons.Default.EditCalendar, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                }

                                // End Date
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { showEndDatePicker = true }
                                        .background(MaterialTheme.colorScheme.surface)
                                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = if (languageMode == LanguageMode.BANGLA) "শেষ তারিখ" else "End Date",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                        val endLabel = tempFilter.customEndDateMs?.let { DateUtils.formatDate(it, languageMode) }
                                            ?: (if (languageMode == LanguageMode.BANGLA) "তারিখ বাছুন" else "Pick Date")
                                        Text(text = endLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Icon(Icons.Default.EditCalendar, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }

                    // 2. Sort Order
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "২. সাজানোর ক্রম" else "2. Sort Order",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Box {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isSortMenuExpanded = true },
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) tempFilter.sortOrder.titleBn else tempFilter.sortOrder.titleEn,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Icon(Icons.Default.ExpandMore, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        DropdownMenu(
                            expanded = isSortMenuExpanded,
                            onDismissRequest = { isSortMenuExpanded = false }
                        ) {
                            CategoryTimelineSortOrder.values().forEach { order ->
                                val isSelected = tempFilter.sortOrder == order
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = if (languageMode == LanguageMode.BANGLA) order.titleBn else order.titleEn,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    onClick = {
                                        isSortMenuExpanded = false
                                        tempFilter = tempFilter.copy(sortOrder = order)
                                    }
                                )
                            }
                        }
                    }

                    // 3. Toggles: Exclude Zero Amounts
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { tempFilter = tempFilter.copy(excludeZeroAmounts = !tempFilter.excludeZeroAmounts) }
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "শূন্য পরিমাণ ক্যাটাগরি লুকান" else "Hide Zero-Amount Categories",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "যে ক্যাটাগরিতে কোনো খরচ বা আয় হয়নি তা লুকানো থাকবে" else "Omit categories with 0 amount across all periods",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        Switch(
                            checked = tempFilter.excludeZeroAmounts,
                            onCheckedChange = { tempFilter = tempFilter.copy(excludeZeroAmounts = it) }
                        )
                    }

                    // 4. Specific Category Selection Filter
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "৩. নির্দিষ্ট ক্যাটাগরি নির্বাচন" else "3. Filter Specific Categories",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    val topCategories = categories.filter { it.parentId == null }
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "ক্যাটাগরি তালিকা (${tempFilter.selectedCategoryIds.size}টি নির্বাচিত)" else "Categories (${tempFilter.selectedCategoryIds.size} selected)",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (tempFilter.selectedCategoryIds.isNotEmpty()) {
                                    TextButton(
                                        onClick = { tempFilter = tempFilter.copy(selectedCategoryIds = emptySet()) },
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(if (languageMode == LanguageMode.BANGLA) "মুছুন" else "Clear", fontSize = 11.sp)
                                    }
                                }
                            }

                            topCategories.take(15).forEach { cat ->
                                val isChecked = cat.id in tempFilter.selectedCategoryIds
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable {
                                            val newSet = if (isChecked) tempFilter.selectedCategoryIds - cat.id
                                            else tempFilter.selectedCategoryIds + cat.id
                                            tempFilter = tempFilter.copy(selectedCategoryIds = newSet)
                                        }
                                        .padding(horizontal = 6.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { checked ->
                                            val newSet = if (checked) tempFilter.selectedCategoryIds + cat.id
                                            else tempFilter.selectedCategoryIds - cat.id
                                            tempFilter = tempFilter.copy(selectedCategoryIds = newSet)
                                        },
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = cat.localizedName(languageMode),
                                        fontSize = 12.sp,
                                        fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isChecked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider()

                // Bottom Actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            tempFilter = CategoryTimelineFilterState(interval = CategoryTimelineInterval.PAST_12_MONTHS)
                        }
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (languageMode == LanguageMode.BANGLA) "রিসেট (১২ মাস)" else "Reset (12 Mo)")
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = onDismiss) {
                            Text(if (languageMode == LanguageMode.BANGLA) "বাতিল" else "Cancel")
                        }
                        Button(
                            onClick = { onApply(tempFilter) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text(if (languageMode == LanguageMode.BANGLA) "প্রয়োগ করুন" else "Apply")
                        }
                    }
                }
            }
        }
    }

    // Custom Date Pickers
    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = tempFilter.customStartDateMs ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { ms ->
                        tempFilter = tempFilter.copy(customStartDateMs = ms)
                    }
                    showStartDatePicker = false
                }) {
                    Text(if (languageMode == LanguageMode.BANGLA) "ঠিক আছে" else "OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) {
                    Text(if (languageMode == LanguageMode.BANGLA) "বাতিল" else "Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = tempFilter.customEndDateMs ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { ms ->
                        tempFilter = tempFilter.copy(customEndDateMs = ms)
                    }
                    showEndDatePicker = false
                }) {
                    Text(if (languageMode == LanguageMode.BANGLA) "ঠিক আছে" else "OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) {
                    Text(if (languageMode == LanguageMode.BANGLA) "বাতিল" else "Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
