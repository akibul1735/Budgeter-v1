package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LanguageMode
import com.example.data.model.SavingsGoal
import com.example.data.model.SavingsGoalWithDetails
import com.example.data.model.SavingsSummary
import com.example.data.repository.AccountWithBalance
import com.example.ui.components.AppTabHeader
import com.example.ui.dialogs.AddEditSavingsGoalDialog
import com.example.ui.dialogs.QuickAllocateGoalDialog
import com.example.util.IconHelper
import com.example.util.LanguageHelper
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

enum class GoalFilterType {
    ALL,
    ACTIVE,
    COMPLETED,
    DEFICIT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingsGoalsScreen(
    goalsWithDetails: List<SavingsGoalWithDetails>,
    savingsSummary: SavingsSummary,
    accountsWithBalances: List<AccountWithBalance>,
    languageMode: LanguageMode,
    onSaveGoal: (SavingsGoal, List<Pair<Long, Double>>) -> Unit,
    onDeleteGoal: (Long) -> Unit,
    onToggleCompleted: (Long, Boolean) -> Unit,
    onUpdateAllocation: (Long, Long, Double) -> Unit,
    onOpenDrawer: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf(GoalFilterType.ALL) }
    var goalToEdit by remember { mutableStateOf<SavingsGoalWithDetails?>(null) }
    var goalToAllocate by remember { mutableStateOf<SavingsGoalWithDetails?>(null) }
    var goalToDelete by remember { mutableStateOf<SavingsGoalWithDetails?>(null) }
    var showAddGoalDialog by remember { mutableStateOf(false) }

    val filteredGoals = remember(goalsWithDetails, selectedFilter) {
        when (selectedFilter) {
            GoalFilterType.ALL -> goalsWithDetails
            GoalFilterType.ACTIVE -> goalsWithDetails.filter { !it.goal.isCompleted }
            GoalFilterType.COMPLETED -> goalsWithDetails.filter { it.goal.isCompleted }
            GoalFilterType.DEFICIT -> goalsWithDetails.filter { it.hasDeficit }
        }
    }

    val deficitGoalsCount = remember(goalsWithDetails) {
        goalsWithDetails.count { it.hasDeficit }
    }

    val listState = rememberLazyListState()
    var isFabVisible by remember { mutableStateOf(true) }
    var previousIndex by remember { mutableIntStateOf(0) }
    var previousScrollOffset by remember { mutableIntStateOf(0) }

    LaunchedEffect(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
        val currentIndex = listState.firstVisibleItemIndex
        val currentOffset = listState.firstVisibleItemScrollOffset

        if (currentIndex > previousIndex || (currentIndex == previousIndex && currentOffset > previousScrollOffset + 12)) {
            // Scrolling down -> hide FAB
            isFabVisible = false
        } else if (currentIndex < previousIndex || (currentIndex == previousIndex && currentOffset < previousScrollOffset - 12)) {
            // Scrolling up -> show FAB
            isFabVisible = true
        }

        previousIndex = currentIndex
        previousScrollOffset = currentOffset
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp)
    ) {
        AppTabHeader(
            title = LanguageHelper.getString("savings_goals", languageMode),
            onOpenDrawer = onOpenDrawer,
            actions = {
                IconButton(
                    onClick = { showAddGoalDialog = true },
                    modifier = Modifier.testTag("topbar_add_goal_btn")
                ) {
                    Icon(
                        Icons.Default.AddCircleOutline,
                        contentDescription = "Add Goal",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        )

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 6.dp, bottom = 80.dp)
            ) {
                // 1. Summary Card with Graph
                item {
                    SavingsSummaryCardWithGraph(
                        summary = savingsSummary,
                        goals = goalsWithDetails.filter { !it.goal.isCompleted },
                        languageMode = languageMode
                    )
                }

                // 2. Deficit Alert Banner if any account has been overspent eating into allocated funds
                if (savingsSummary.totalDeficit > 0.001) {
                    item {
                        SavingsDeficitAlertBanner(
                            totalDeficit = savingsSummary.totalDeficit,
                            deficitCount = deficitGoalsCount,
                            languageMode = languageMode,
                            onViewDeficits = { selectedFilter = GoalFilterType.DEFICIT }
                        )
                    }
                }

                // 3. Filter Chips
                item {
                    GoalFilterRow(
                        selectedFilter = selectedFilter,
                        onSelectFilter = { selectedFilter = it },
                        totalCount = goalsWithDetails.size,
                        activeCount = savingsSummary.activeGoalsCount,
                        completedCount = savingsSummary.completedGoalsCount,
                        deficitCount = deficitGoalsCount,
                        languageMode = languageMode
                    )
                }

                // 4. Goal List
                if (filteredGoals.isEmpty()) {
                    item {
                        EmptyGoalsCard(
                            filter = selectedFilter,
                            languageMode = languageMode,
                            onAddGoal = { showAddGoalDialog = true }
                        )
                    }
                } else {
                    items(filteredGoals, key = { it.goal.id }) { goalItem ->
                        SavingsGoalCard(
                            goalWithDetails = goalItem,
                            languageMode = languageMode,
                            onEdit = { goalToEdit = goalItem },
                            onDelete = { goalToDelete = goalItem },
                            onToggleCompleted = { isComp -> onToggleCompleted(goalItem.goal.id, isComp) },
                            onQuickAllocate = { goalToAllocate = goalItem }
                        )
                    }
                }
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = isFabVisible,
                enter = slideInVertically(initialOffsetY = { it * 2 }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it * 2 }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 16.dp, end = 4.dp)
            ) {
                ExtendedFloatingActionButton(
                    onClick = { showAddGoalDialog = true },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text(LanguageHelper.getString("add_goal", languageMode), fontWeight = FontWeight.SemiBold) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("add_savings_goal_fab")
                )
            }
        }
    }

    // Add Goal Dialog
    if (showAddGoalDialog) {
        AddEditSavingsGoalDialog(
            goalWithDetails = null,
            accountsWithBalances = accountsWithBalances,
            languageMode = languageMode,
            onDismiss = { showAddGoalDialog = false },
            onSave = { newGoal, allocations ->
                onSaveGoal(newGoal, allocations)
                showAddGoalDialog = false
            }
        )
    }

    // Edit Goal Dialog
    goalToEdit?.let { item ->
        AddEditSavingsGoalDialog(
            goalWithDetails = item,
            accountsWithBalances = accountsWithBalances,
            languageMode = languageMode,
            onDismiss = { goalToEdit = null },
            onSave = { updatedGoal, allocations ->
                onSaveGoal(updatedGoal, allocations)
                goalToEdit = null
            }
        )
    }

    // Quick Allocate Dialog
    goalToAllocate?.let { item ->
        QuickAllocateGoalDialog(
            goalWithDetails = item,
            accountsWithBalances = accountsWithBalances,
            languageMode = languageMode,
            onDismiss = { goalToAllocate = null },
            onSaveAllocation = { accId, amt ->
                onUpdateAllocation(item.goal.id, accId, amt)
                goalToAllocate = null
            }
        )
    }

    // Delete Confirmation Dialog
    goalToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { goalToDelete = null },
            title = {
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "সঞ্চয় লক্ষ্য মুছে ফেলতে চান?" else "Delete Savings Goal?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) {
                        "\"${LanguageHelper.getLocalizedName(item.goal.name, item.goal.nameBn, languageMode)}\" লক্ষ্যটি মুছে ফেলা হবে। হিসাবের টাকা যথারীতি অক্ষত থাকবে।"
                    } else {
                        "Are you sure you want to delete \"${LanguageHelper.getLocalizedName(item.goal.name, item.goal.nameBn, languageMode)}\"? Your actual account balances will remain unchanged."
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteGoal(item.goal.id)
                        goalToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(LanguageHelper.getString("delete", languageMode))
                }
            },
            dismissButton = {
                TextButton(onClick = { goalToDelete = null }) {
                    Text(LanguageHelper.getString("cancel", languageMode))
                }
            }
        )
    }
}

/**
 * Modern Summary Card with interactive Graph and key financial targets
 */
@Composable
private fun SavingsSummaryCardWithGraph(
    summary: SavingsSummary,
    goals: List<SavingsGoalWithDetails>,
    languageMode: LanguageMode
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("savings_summary_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Savings,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "সঞ্চয় লক্ষ্য সারসংক্ষেপ" else "Savings Overview",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "লক্ষ্যমাত্রা ও অর্জিত সঞ্চয়" else "Targets & Allocated Funds",
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                // Overall progress badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "${LanguageHelper.formatNumber(summary.overallProgressPercent.toDouble(), languageMode, false)}%",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                }
            }

            // Key Metrics 3-Column Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryMetricItem(
                    label = LanguageHelper.getString("target_amount", languageMode),
                    value = LanguageHelper.formatCurrency(summary.totalTarget, languageMode),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                SummaryMetricItem(
                    label = LanguageHelper.getString("saved_amount", languageMode),
                    value = LanguageHelper.formatCurrency(summary.totalSaved, languageMode),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                SummaryMetricItem(
                    label = LanguageHelper.getString("remaining_amount", languageMode),
                    value = LanguageHelper.formatCurrency(summary.totalRemaining, languageMode),
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )
            }

            // Custom Graph Canvas
            SavingsGoalsGraphView(
                summary = summary,
                goals = goals,
                languageMode = languageMode
            )
        }
    }
}

@Composable
private fun SummaryMetricItem(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 2.dp)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.outline,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(1.dp))
        Text(
            text = value,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Visual Canvas Chart displaying multi-goal distribution and progress bars
 */
@Composable
private fun SavingsGoalsGraphView(
    summary: SavingsSummary,
    goals: List<SavingsGoalWithDetails>,
    languageMode: LanguageMode
) {
    val animatedOverallProgress by animateFloatAsState(
        targetValue = (summary.overallProgressPercent / 100f).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 800),
        label = "overallProgressAnim"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (languageMode == LanguageMode.BANGLA) "সামগ্রিক অগ্রগতি বার" else "Overall Allocation Progress",
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${LanguageHelper.formatCurrency(summary.totalSaved, languageMode)} / ${LanguageHelper.formatCurrency(summary.totalTarget, languageMode)}",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.outline
            )
        }

        // Horizontal Stacked / Segmented Progress Bar
        val primaryColor = MaterialTheme.colorScheme.primary
        val trackColor = MaterialTheme.colorScheme.surfaceVariant
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // Background track
            drawRect(
                color = trackColor,
                topLeft = Offset.Zero,
                size = Size(canvasWidth, canvasHeight)
            )

            if (goals.isEmpty() || summary.totalTarget <= 0) {
                // Single overall progress bar if no specific goals
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(primaryColor, primaryColor.copy(alpha = 0.8f))
                    ),
                    topLeft = Offset.Zero,
                    size = Size(canvasWidth * animatedOverallProgress, canvasHeight)
                )
            } else {
                // Segmented contribution per active goal
                var currentX = 0f
                goals.forEach { item ->
                    val goalSaved = item.effectiveSaved
                    if (goalSaved > 0) {
                        val segmentFraction = (goalSaved / summary.totalTarget).toFloat().coerceIn(0f, 1f)
                        val segmentWidth = canvasWidth * segmentFraction * (animatedOverallProgress / (if (summary.overallProgressPercent > 0) summary.overallProgressPercent / 100f else 1f).coerceAtLeast(0.001f))
                        
                        val segColor = try {
                            Color(android.graphics.Color.parseColor(item.goal.colorHex))
                        } catch (_: Exception) {
                            primaryColor
                        }

                        drawRect(
                            color = segColor,
                            topLeft = Offset(currentX, 0f),
                            size = Size(segmentWidth, canvasHeight)
                        )
                        currentX += segmentWidth
                    }
                }
            }
        }

        // Active Goals Legend Chips
        if (goals.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(top = 1.dp)
            ) {
                items(goals.take(5)) { item ->
                    val dotColor = try {
                        Color(android.graphics.Color.parseColor(item.goal.colorHex))
                    } catch (_: Exception) {
                        MaterialTheme.colorScheme.primary
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(dotColor)
                        )
                        Text(
                            text = LanguageHelper.getLocalizedName(item.goal.name, item.goal.nameBn, languageMode),
                            fontSize = 9.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                        Text(
                            text = "${LanguageHelper.formatNumber(item.progressPercent.toDouble(), languageMode, false)}%",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = dotColor
                        )
                    }
                }
            }
        }
    }
}

/**
 * Deficit / Shortfall Warning Banner
 */
@Composable
private fun SavingsDeficitAlertBanner(
    totalDeficit: Double,
    deficitCount: Int,
    languageMode: LanguageMode,
    onViewDeficits: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.85f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("savings_deficit_alert_banner")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.WarningAmber,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onError,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "সঞ্চয় বরাদ্দে ঘাটতি!" else "Savings Deficit / Overdraft Detected",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) {
                        "হিসাবের ব্যালেন্স খরচ হয়ে যাওয়ায় ${LanguageHelper.toBanglaDigits(deficitCount.toString())}টি লক্ষ্যে মোট ${LanguageHelper.formatCurrency(totalDeficit, languageMode)} ঘাটতি রয়েছে।"
                    } else {
                        "Account balance was spent: ${LanguageHelper.formatCurrency(totalDeficit, languageMode)} total shortfall across $deficitCount goal(s)."
                    },
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.85f)
                )
            }

            FilledTonalButton(
                onClick = onViewDeficits,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "দেখুন" else "Review",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Filter row (All, Active, Completed, Deficit)
 */
@Composable
private fun GoalFilterRow(
    selectedFilter: GoalFilterType,
    onSelectFilter: (GoalFilterType) -> Unit,
    totalCount: Int,
    activeCount: Int,
    completedCount: Int,
    deficitCount: Int,
    languageMode: LanguageMode
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        item {
            FilterChip(
                selected = selectedFilter == GoalFilterType.ALL,
                onClick = { onSelectFilter(GoalFilterType.ALL) },
                label = {
                    Text(
                        "${if (languageMode == LanguageMode.BANGLA) "সকল" else "All"} ($totalCount)"
                    )
                },
                shape = RoundedCornerShape(10.dp)
            )
        }
        item {
            FilterChip(
                selected = selectedFilter == GoalFilterType.ACTIVE,
                onClick = { onSelectFilter(GoalFilterType.ACTIVE) },
                label = {
                    Text(
                        "${if (languageMode == LanguageMode.BANGLA) "চলমান" else "Active"} ($activeCount)"
                    )
                },
                shape = RoundedCornerShape(10.dp)
            )
        }
        item {
            FilterChip(
                selected = selectedFilter == GoalFilterType.COMPLETED,
                onClick = { onSelectFilter(GoalFilterType.COMPLETED) },
                label = {
                    Text(
                        "${if (languageMode == LanguageMode.BANGLA) "অর্জিত" else "Completed"} ($completedCount)"
                    )
                },
                shape = RoundedCornerShape(10.dp)
            )
        }
        if (deficitCount > 0) {
            item {
                FilterChip(
                    selected = selectedFilter == GoalFilterType.DEFICIT,
                    onClick = { onSelectFilter(GoalFilterType.DEFICIT) },
                    label = {
                        Text(
                            "${if (languageMode == LanguageMode.BANGLA) "ঘাটতি" else "Deficit"} ($deficitCount)"
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }
    }
}

/**
 * Individual Savings Goal Card
 */
@Composable
private fun SavingsGoalCard(
    goalWithDetails: SavingsGoalWithDetails,
    languageMode: LanguageMode,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleCompleted: (Boolean) -> Unit,
    onQuickAllocate: () -> Unit
) {
    val goal = goalWithDetails.goal
    val isCompleted = goal.isCompleted
    val progress = goalWithDetails.progressPercent
    val hasDeficit = goalWithDetails.hasDeficit

    val accentColor = try {
        Color(android.graphics.Color.parseColor(goal.colorHex))
    } catch (_: Exception) {
        MaterialTheme.colorScheme.primary
    }

    var expandedAccounts by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (hasDeficit) 1.5.dp else 1.dp,
            color = if (hasDeficit) MaterialTheme.colorScheme.error.copy(alpha = 0.8f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("savings_goal_card_${goal.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Card Header: Icon + Title + Target Date + Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = accentColor.copy(alpha = 0.15f),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            IconHelper.AppIcon(
                                iconName = goal.iconName,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = LanguageHelper.getLocalizedName(goal.name, goal.nameBn, languageMode),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            if (isCompleted) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "অর্জিত" else "Reached",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }

                        // Target Date display
                        if (goal.targetDate > 0L) {
                            val daysLeft = TimeUnit.MILLISECONDS.toDays(goal.targetDate - System.currentTimeMillis())
                            val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(goal.targetDate))
                            val daysText = when {
                                daysLeft < 0 -> if (languageMode == LanguageMode.BANGLA) "সময় উত্তীর্ণ" else "Past deadline"
                                daysLeft == 0L -> if (languageMode == LanguageMode.BANGLA) "আজ শেষ দিন" else "Due today"
                                else -> if (languageMode == LanguageMode.BANGLA) "${LanguageHelper.toBanglaDigits(daysLeft.toString())} দিন বাকি" else "$daysLeft days left"
                            }
                            Text(
                                text = "$dateStr • $daysText",
                                fontSize = 10.5.sp,
                                color = if (daysLeft < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Actions Menu
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onToggleCompleted(!isCompleted) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = "Toggle Complete",
                            tint = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    var showMenu by remember { mutableStateOf(false) }
                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Options")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(LanguageHelper.getString("edit", languageMode)) },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    onEdit()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(LanguageHelper.getString("quick_allocate", languageMode)) },
                                leadingIcon = { Icon(Icons.Default.Savings, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    onQuickAllocate()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(LanguageHelper.getString("delete", languageMode), color = MaterialTheme.colorScheme.error) },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    showMenu = false
                                    onDelete()
                                }
                            )
                        }
                    }
                }
            }

            // Progress Bar & Percentage
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = LanguageHelper.formatCurrency(goalWithDetails.effectiveSaved, languageMode),
                            fontSize = 15.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (hasDeficit) MaterialTheme.colorScheme.error else accentColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (hasDeficit) {
                            Text(
                                text = "${if (languageMode == LanguageMode.BANGLA) "বরাদ্দ ছিল:" else "Allocated:"} ${LanguageHelper.formatCurrency(goalWithDetails.totalAllocated, languageMode)}",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.outline,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "${LanguageHelper.formatNumber(progress.toDouble(), languageMode, false)}%",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor,
                            maxLines = 1
                        )
                        Text(
                            text = "${if (languageMode == LanguageMode.BANGLA) "লক্ষ্য:" else "of"} ${LanguageHelper.formatCurrency(goal.targetAmount, languageMode)}",
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.outline,
                            maxLines = 1
                        )
                    }
                }

                // Progress Bar
                LinearProgressIndicator(
                    progress = { (progress / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (hasDeficit) MaterialTheme.colorScheme.error else accentColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            // Deficit Alert Sub-banner inside card
            if (hasDeficit) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) {
                                "হিসাবে ব্যালেন্স কম থাকায় ${LanguageHelper.formatCurrency(goalWithDetails.totalDeficit, languageMode)} ঘাটতি রয়েছে।"
                            } else {
                                "Shortfall of ${LanguageHelper.formatCurrency(goalWithDetails.totalDeficit, languageMode)} due to account spending."
                            },
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 14.sp
                        )
                    }
                }
            }

            // Linked Accounts toggle header
            if (goalWithDetails.allocations.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandedAccounts = !expandedAccounts }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${LanguageHelper.getString("linked_accounts", languageMode)} (${goalWithDetails.allocations.size})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Icon(
                            imageVector = if (expandedAccounts) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                AnimatedVisibility(visible = expandedAccounts) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        goalWithDetails.allocations.forEach { allocWithAcc ->
                            val acc = allocWithAcc.account
                            val allocAmt = allocWithAcc.allocation.allocatedAmount
                            val actualFunded = allocWithAcc.actualFundedAmount
                            val isDeficit = !allocWithAcc.isFunded

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                                border = if (isDeficit) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)) else null,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.weight(1f, fill = false)
                                    ) {
                                        IconHelper.AppIcon(
                                            iconName = acc.iconName,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = LanguageHelper.getLocalizedName(acc.nameEn, acc.nameBn, languageMode),
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = LanguageHelper.formatCurrency(allocAmt, languageMode),
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDeficit) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1
                                        )
                                        if (isDeficit) {
                                            Text(
                                                text = "${if (languageMode == LanguageMode.BANGLA) "হিসাব স্থিতি:" else "Live Bal:"} ${LanguageHelper.formatCurrency(allocWithAcc.accountCurrentBalance, languageMode)}",
                                                fontSize = 9.5.sp,
                                                color = MaterialTheme.colorScheme.error,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Quick Allocate / Add Funds Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onQuickAllocate,
                    modifier = Modifier.weight(1f, fill = false),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = LanguageHelper.getString("quick_allocate", languageMode),
                        fontSize = 11.5.sp,
                        maxLines = 1
                    )
                }

                if (goal.notes.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Text(
                            text = goal.notes,
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.outline,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Empty state card when no goals exist
 */
@Composable
private fun EmptyGoalsCard(
    filter: GoalFilterType,
    languageMode: LanguageMode,
    onAddGoal: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Savings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Text(
                text = when (filter) {
                    GoalFilterType.DEFICIT -> if (languageMode == LanguageMode.BANGLA) "কোনো ঘাটতি নেই! সব সঞ্চয় সুরক্ষিত।" else "No deficits! All savings are fully funded."
                    GoalFilterType.COMPLETED -> if (languageMode == LanguageMode.BANGLA) "কোনো সম্পন্ন লক্ষ্য নেই।" else "No completed goals yet."
                    else -> if (languageMode == LanguageMode.BANGLA) "কোনো সঞ্চয় লক্ষ্য তৈরি করা হয়নি" else "No savings goals created yet"
                },
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = if (languageMode == LanguageMode.BANGLA) {
                    "আপনার স্বপ্নের কেনাকাটা, জরুরি তহবিল বা ভ্যাকেশনের জন্য সঞ্চয় লক্ষ্য তৈরি করুন এবং বিদ্যমান একাউন্ট থেকে অর্থ বরাদ্দ করুন।"
                } else {
                    "Set up financial goals for your dream purchases or emergency funds and allocate money from your existing accounts."
                },
                fontSize = 12.5.sp,
                color = MaterialTheme.colorScheme.outline,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            if (filter == GoalFilterType.ALL || filter == GoalFilterType.ACTIVE) {
                Button(
                    onClick = onAddGoal,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(LanguageHelper.getString("add_savings_goal", languageMode))
                }
            }
        }
    }
}
