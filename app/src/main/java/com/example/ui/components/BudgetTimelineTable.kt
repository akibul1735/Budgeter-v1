package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LanguageMode
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.util.CategoryTimelineData
import com.example.util.CategoryTimelineGroup
import com.example.util.LanguageHelper
import com.example.util.TimelineViewOption

/**
 * Budget Timeline Table with frozen first column (Category/Group names) and
 * synchronized horizontally scrollable period columns.
 */
@Composable
fun BudgetTimelineTable(
    timelineData: CategoryTimelineData,
    languageMode: LanguageMode,
    horizontalScrollState: ScrollState,
    expandedGroupMap: SnapshotStateMap<String, Boolean>,
    displayedExpenseGroups: List<CategoryTimelineGroup>,
    displayedIncomeGroups: List<CategoryTimelineGroup>,
    viewOption: TimelineViewOption = TimelineViewOption.ALL,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. Table Header (Frozen Category Col + Horizontally Scrollable Periods)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Frozen Category Header Cell
                    Box(
                        modifier = Modifier
                            .width(155.dp)
                            .fillMaxHeight()
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "ক্যাটাগরি" else "Category",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    // Period Column Headers
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .horizontalScroll(horizontalScrollState),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        timelineData.periods.forEach { period ->
                            Box(
                                modifier = Modifier
                                    .width(92.dp)
                                    .fillMaxHeight()
                                    .padding(horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = period.shortLabel,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                            }
                            VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        }

                        // Total Header
                        Box(
                            modifier = Modifier
                                .width(98.dp)
                                .fillMaxHeight()
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
                                .padding(horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "সর্বমোট" else "Total",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                        // Average Header
                        Box(
                            modifier = Modifier
                                .width(92.dp)
                                .fillMaxHeight()
                                .padding(horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "গড়/মাস" else "Avg/Period",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }

            // 2. Expenses Section
            if (displayedExpenseGroups.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp)
                            .background(SolidExpense.copy(alpha = 0.12f)),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(155.dp)
                                .fillMaxHeight()
                                .padding(horizontal = 8.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "📉 ব্যয়ের খাত" else "📉 Expenses",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SolidExpense
                            )
                        }
                        VerticalDivider(color = SolidExpense.copy(alpha = 0.2f))

                        // Expense Totals Row
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .horizontalScroll(horizontalScrollState),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            timelineData.totalExpenseByPeriod.forEach { amount ->
                                Box(
                                    modifier = Modifier
                                        .width(92.dp)
                                        .fillMaxHeight()
                                        .padding(horizontal = 6.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Text(
                                        text = if (amount > 0) LanguageHelper.formatCurrency(amount, languageMode) else "—",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SolidExpense,
                                        maxLines = 1
                                    )
                                }
                                VerticalDivider(color = SolidExpense.copy(alpha = 0.15f))
                            }

                            Box(
                                modifier = Modifier
                                    .width(98.dp)
                                    .fillMaxHeight()
                                    .background(SolidExpense.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Text(
                                    text = LanguageHelper.formatCurrency(timelineData.grandTotalExpense, languageMode),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SolidExpense,
                                    maxLines = 1
                                )
                            }
                            VerticalDivider(color = SolidExpense.copy(alpha = 0.15f))

                            Box(
                                modifier = Modifier
                                    .width(92.dp)
                                    .fillMaxHeight()
                                    .padding(horizontal = 6.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Text(
                                    text = LanguageHelper.formatCurrency(timelineData.avgExpensePerPeriod, languageMode),
                                    fontSize = 10.5.sp,
                                    color = SolidExpense,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                    HorizontalDivider(color = SolidExpense.copy(alpha = 0.2f))
                }

                displayedExpenseGroups.forEach { grp ->
                    val key = "${grp.type.name}_${grp.groupNameEn}"
                    val isExpanded = expandedGroupMap[key] != false

                    // If in ALL or GROUPS mode, render group row
                    if (viewOption == TimelineViewOption.ALL || viewOption == TimelineViewOption.GROUPS) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(38.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Category Group Name (Frozen)
                                Row(
                                    modifier = Modifier
                                        .width(155.dp)
                                        .fillMaxHeight()
                                        .clickable(enabled = viewOption == TimelineViewOption.ALL) {
                                            expandedGroupMap[key] = !isExpanded
                                        }
                                        .padding(horizontal = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (viewOption == TimelineViewOption.ALL) {
                                        Icon(
                                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                    }
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) grp.groupNameBn else grp.groupNameEn,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                                // Period Values (Scrollable)
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .horizontalScroll(horizontalScrollState),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    grp.groupAmountsByPeriod.forEach { amount ->
                                        Box(
                                            modifier = Modifier
                                                .width(92.dp)
                                                .fillMaxHeight()
                                                .padding(horizontal = 6.dp),
                                            contentAlignment = Alignment.CenterEnd
                                        ) {
                                            Text(
                                                text = if (amount > 0) LanguageHelper.formatCurrency(amount, languageMode) else "—",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (amount > 0) SolidExpense else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                                maxLines = 1
                                            )
                                        }
                                        VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                                    }

                                    // Total
                                    Box(
                                        modifier = Modifier
                                            .width(98.dp)
                                            .fillMaxHeight()
                                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f))
                                            .padding(horizontal = 6.dp),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Text(
                                            text = LanguageHelper.formatCurrency(grp.totalAmount, languageMode),
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = SolidExpense,
                                            maxLines = 1
                                        )
                                    }
                                    VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                                    // Average
                                    Box(
                                        modifier = Modifier
                                            .width(92.dp)
                                            .fillMaxHeight()
                                            .padding(horizontal = 6.dp),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Text(
                                            text = LanguageHelper.formatCurrency(grp.averageAmount, languageMode),
                                            fontSize = 10.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        }
                    }

                    // Subcategory Rows (if ALL and expanded, or if ITEMS mode)
                    if ((viewOption == TimelineViewOption.ALL && isExpanded) || viewOption == TimelineViewOption.ITEMS) {
                        items(grp.subCategories) { sub ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(34.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Subcategory Name (Frozen)
                                Row(
                                    modifier = Modifier
                                        .width(155.dp)
                                        .fillMaxHeight()
                                        .padding(start = if (viewOption == TimelineViewOption.ALL) 16.dp else 8.dp, end = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "•",
                                        fontSize = 12.sp,
                                        color = SolidExpense,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = sub.category.localizedName(languageMode),
                                        fontSize = 10.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f))

                                // Period Values (Scrollable)
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .horizontalScroll(horizontalScrollState),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    sub.amountsByPeriod.forEach { amount ->
                                        Box(
                                            modifier = Modifier
                                                .width(92.dp)
                                                .fillMaxHeight()
                                                .padding(horizontal = 6.dp),
                                            contentAlignment = Alignment.CenterEnd
                                        ) {
                                            Text(
                                                text = if (amount > 0) LanguageHelper.formatCurrency(amount, languageMode) else "—",
                                                fontSize = 10.5.sp,
                                                color = if (amount > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                                maxLines = 1
                                            )
                                        }
                                        VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f))
                                    }

                                    // Total
                                    Box(
                                        modifier = Modifier
                                            .width(98.dp)
                                            .fillMaxHeight()
                                            .padding(horizontal = 6.dp),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Text(
                                            text = LanguageHelper.formatCurrency(sub.totalAmount, languageMode),
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (sub.totalAmount > 0) SolidExpense else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                            maxLines = 1
                                        )
                                    }
                                    VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f))

                                    // Average
                                    Box(
                                        modifier = Modifier
                                            .width(92.dp)
                                            .fillMaxHeight()
                                            .padding(horizontal = 6.dp),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Text(
                                            text = LanguageHelper.formatCurrency(sub.averageAmount, languageMode),
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.outline,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f))
                        }
                    }
                }
            }

            // 3. Incomes Section
            if (displayedIncomeGroups.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp)
                            .background(SolidIncome.copy(alpha = 0.12f)),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(155.dp)
                                .fillMaxHeight()
                                .padding(horizontal = 8.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "📈 আয়ের খাত" else "📈 Incomes",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SolidIncome
                            )
                        }
                        VerticalDivider(color = SolidIncome.copy(alpha = 0.2f))

                        // Income Totals Row
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .horizontalScroll(horizontalScrollState),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            timelineData.totalIncomeByPeriod.forEach { amount ->
                                Box(
                                    modifier = Modifier
                                        .width(92.dp)
                                        .fillMaxHeight()
                                        .padding(horizontal = 6.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Text(
                                        text = if (amount > 0) LanguageHelper.formatCurrency(amount, languageMode) else "—",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SolidIncome,
                                        maxLines = 1
                                    )
                                }
                                VerticalDivider(color = SolidIncome.copy(alpha = 0.15f))
                            }

                            Box(
                                modifier = Modifier
                                    .width(98.dp)
                                    .fillMaxHeight()
                                    .background(SolidIncome.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Text(
                                    text = LanguageHelper.formatCurrency(timelineData.grandTotalIncome, languageMode),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SolidIncome,
                                    maxLines = 1
                                )
                            }
                            VerticalDivider(color = SolidIncome.copy(alpha = 0.15f))

                            Box(
                                modifier = Modifier
                                    .width(92.dp)
                                    .fillMaxHeight()
                                    .padding(horizontal = 6.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Text(
                                    text = LanguageHelper.formatCurrency(timelineData.avgIncomePerPeriod, languageMode),
                                    fontSize = 10.5.sp,
                                    color = SolidIncome,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                    HorizontalDivider(color = SolidIncome.copy(alpha = 0.2f))
                }

                displayedIncomeGroups.forEach { grp ->
                    val key = "${grp.type.name}_${grp.groupNameEn}"
                    val isExpanded = expandedGroupMap[key] != false

                    // If in ALL or GROUPS mode, render group row
                    if (viewOption == TimelineViewOption.ALL || viewOption == TimelineViewOption.GROUPS) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(38.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Category Group Name (Frozen)
                                Row(
                                    modifier = Modifier
                                        .width(155.dp)
                                        .fillMaxHeight()
                                        .clickable(enabled = viewOption == TimelineViewOption.ALL) {
                                            expandedGroupMap[key] = !isExpanded
                                        }
                                        .padding(horizontal = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (viewOption == TimelineViewOption.ALL) {
                                        Icon(
                                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                    }
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) grp.groupNameBn else grp.groupNameEn,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                                // Period Values (Scrollable)
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .horizontalScroll(horizontalScrollState),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    grp.groupAmountsByPeriod.forEach { amount ->
                                        Box(
                                            modifier = Modifier
                                                .width(92.dp)
                                                .fillMaxHeight()
                                                .padding(horizontal = 6.dp),
                                            contentAlignment = Alignment.CenterEnd
                                        ) {
                                            Text(
                                                text = if (amount > 0) LanguageHelper.formatCurrency(amount, languageMode) else "—",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (amount > 0) SolidIncome else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                                maxLines = 1
                                            )
                                        }
                                        VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                                    }

                                    // Total
                                    Box(
                                        modifier = Modifier
                                            .width(98.dp)
                                            .fillMaxHeight()
                                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f))
                                            .padding(horizontal = 6.dp),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Text(
                                            text = LanguageHelper.formatCurrency(grp.totalAmount, languageMode),
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = SolidIncome,
                                            maxLines = 1
                                        )
                                    }
                                    VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                                    // Average
                                    Box(
                                        modifier = Modifier
                                            .width(92.dp)
                                            .fillMaxHeight()
                                            .padding(horizontal = 6.dp),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Text(
                                            text = LanguageHelper.formatCurrency(grp.averageAmount, languageMode),
                                            fontSize = 10.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        }
                    }

                    // Subcategory Rows (if ALL and expanded, or if ITEMS mode)
                    if ((viewOption == TimelineViewOption.ALL && isExpanded) || viewOption == TimelineViewOption.ITEMS) {
                        items(grp.subCategories) { sub ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(34.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Subcategory Name (Frozen)
                                Row(
                                    modifier = Modifier
                                        .width(155.dp)
                                        .fillMaxHeight()
                                        .padding(start = if (viewOption == TimelineViewOption.ALL) 16.dp else 8.dp, end = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "•",
                                        fontSize = 12.sp,
                                        color = SolidIncome,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = sub.category.localizedName(languageMode),
                                        fontSize = 10.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f))

                                // Period Values (Scrollable)
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .horizontalScroll(horizontalScrollState),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    sub.amountsByPeriod.forEach { amount ->
                                        Box(
                                            modifier = Modifier
                                                .width(92.dp)
                                                .fillMaxHeight()
                                                .padding(horizontal = 6.dp),
                                            contentAlignment = Alignment.CenterEnd
                                        ) {
                                            Text(
                                                text = if (amount > 0) LanguageHelper.formatCurrency(amount, languageMode) else "—",
                                                fontSize = 10.5.sp,
                                                color = if (amount > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                                maxLines = 1
                                            )
                                        }
                                        VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f))
                                    }

                                    // Total
                                    Box(
                                        modifier = Modifier
                                            .width(98.dp)
                                            .fillMaxHeight()
                                            .padding(horizontal = 6.dp),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Text(
                                            text = LanguageHelper.formatCurrency(sub.totalAmount, languageMode),
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (sub.totalAmount > 0) SolidIncome else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                            maxLines = 1
                                        )
                                    }
                                    VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f))

                                    // Average
                                    Box(
                                        modifier = Modifier
                                            .width(92.dp)
                                            .fillMaxHeight()
                                            .padding(horizontal = 6.dp),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Text(
                                            text = LanguageHelper.formatCurrency(sub.averageAmount, languageMode),
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.outline,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f))
                        }
                    }
                }
            }

            // 4. Summary Rows (Total Income, Total Expense, Net Earnings)
            item {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)

                // Total Expenses Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .background(SolidExpense.copy(alpha = 0.08f)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(155.dp)
                            .fillMaxHeight()
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "মোট ব্যয়" else "Total Expenses",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SolidExpense
                        )
                    }
                    VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .horizontalScroll(horizontalScrollState),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        timelineData.totalExpenseByPeriod.forEach { amount ->
                            Box(
                                modifier = Modifier
                                    .width(92.dp)
                                    .fillMaxHeight()
                                    .padding(horizontal = 6.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Text(
                                    text = LanguageHelper.formatCurrency(amount, languageMode),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SolidExpense,
                                    maxLines = 1
                                )
                            }
                            VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                        }

                        // Grand Total Expense
                        Box(
                            modifier = Modifier
                                .width(98.dp)
                                .fillMaxHeight()
                                .background(SolidExpense.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Text(
                                text = LanguageHelper.formatCurrency(timelineData.grandTotalExpense, languageMode),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SolidExpense,
                                maxLines = 1
                            )
                        }
                        VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                        // Avg Expense
                        Box(
                            modifier = Modifier
                                .width(92.dp)
                                .fillMaxHeight()
                                .padding(horizontal = 6.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Text(
                                text = LanguageHelper.formatCurrency(timelineData.avgExpensePerPeriod, languageMode),
                                fontSize = 10.5.sp,
                                color = SolidExpense,
                                maxLines = 1
                            )
                        }
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                // Total Income Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .background(SolidIncome.copy(alpha = 0.08f)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(155.dp)
                            .fillMaxHeight()
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "মোট আয়" else "Total Income",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SolidIncome
                        )
                    }
                    VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .horizontalScroll(horizontalScrollState),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        timelineData.totalIncomeByPeriod.forEach { amount ->
                            Box(
                                modifier = Modifier
                                    .width(92.dp)
                                    .fillMaxHeight()
                                    .padding(horizontal = 6.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Text(
                                    text = LanguageHelper.formatCurrency(amount, languageMode),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SolidIncome,
                                    maxLines = 1
                                )
                            }
                            VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                        }

                        // Grand Total Income
                        Box(
                            modifier = Modifier
                                .width(98.dp)
                                .fillMaxHeight()
                                .background(SolidIncome.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Text(
                                text = LanguageHelper.formatCurrency(timelineData.grandTotalIncome, languageMode),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SolidIncome,
                                maxLines = 1
                            )
                        }
                        VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                        // Avg Income
                        Box(
                            modifier = Modifier
                                .width(92.dp)
                                .fillMaxHeight()
                                .padding(horizontal = 6.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Text(
                                text = LanguageHelper.formatCurrency(timelineData.avgIncomePerPeriod, languageMode),
                                fontSize = 10.5.sp,
                                color = SolidIncome,
                                maxLines = 1
                            )
                        }
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                // Net Earnings Row
                val isNetPositive = timelineData.grandNetSavings >= 0
                val netColor = if (isNetPositive) SolidIncome else SolidExpense
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .background(netColor.copy(alpha = 0.12f)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(155.dp)
                            .fillMaxHeight()
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "নেট আয় (Net Earnings)" else "Net Earnings",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = netColor
                        )
                    }
                    VerticalDivider(color = netColor.copy(alpha = 0.3f))

                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .horizontalScroll(horizontalScrollState),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        timelineData.netSavingsByPeriod.forEach { amount ->
                            val isPos = amount >= 0
                            val col = if (isPos) SolidIncome else SolidExpense
                            Box(
                                modifier = Modifier
                                    .width(92.dp)
                                    .fillMaxHeight()
                                    .padding(horizontal = 6.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Text(
                                    text = (if (isPos && amount > 0) "+" else "") + LanguageHelper.formatCurrency(amount, languageMode),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = col,
                                    maxLines = 1
                                )
                            }
                            VerticalDivider(color = netColor.copy(alpha = 0.2f))
                        }

                        // Grand Net Earnings
                        Box(
                            modifier = Modifier
                                .width(98.dp)
                                .fillMaxHeight()
                                .background(netColor.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Text(
                                text = (if (isNetPositive) "+" else "") + LanguageHelper.formatCurrency(timelineData.grandNetSavings, languageMode),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = netColor,
                                maxLines = 1
                            )
                        }
                        VerticalDivider(color = netColor.copy(alpha = 0.2f))

                        // Avg Net Earnings
                        Box(
                            modifier = Modifier
                                .width(92.dp)
                                .fillMaxHeight()
                                .padding(horizontal = 6.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Text(
                                text = (if (timelineData.avgNetSavingsPerPeriod >= 0) "+" else "") + LanguageHelper.formatCurrency(timelineData.avgNetSavingsPerPeriod, languageMode),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = netColor,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
