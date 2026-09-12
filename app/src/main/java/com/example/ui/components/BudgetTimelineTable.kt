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
import com.example.ui.theme.SolidPrimary
import com.example.util.CategoryTimelineData
import com.example.util.CategoryTimelineGroup
import com.example.util.LanguageHelper

/**
 * Unified Budget Timeline Table where the Category column and all period data columns
 * scroll horizontally together as a single unified section, ensuring category rows and
 * monthly values are always aligned.
 */
@Composable
fun BudgetTimelineTable(
    timelineData: CategoryTimelineData,
    languageMode: LanguageMode,
    horizontalScrollState: ScrollState,
    expandedGroupMap: SnapshotStateMap<String, Boolean>,
    displayedExpenseGroups: List<CategoryTimelineGroup>,
    displayedIncomeGroups: List<CategoryTimelineGroup>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(horizontalScrollState)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxHeight()
            ) {
                // 1. Table Header
                item {
                    Row(
                        modifier = Modifier
                            .height(40.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Category Header Cell
                        Box(
                            modifier = Modifier
                                .width(160.dp)
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
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }

                // 2. Expenses Section
                if (displayedExpenseGroups.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(30.dp)
                                .background(SolidExpense.copy(alpha = 0.12f)),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "📉 ব্যয়ের খাত" else "📉 Expenses",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SolidExpense,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                        HorizontalDivider(color = SolidExpense.copy(alpha = 0.2f))
                    }

                    displayedExpenseGroups.forEach { grp ->
                        val key = "${grp.type.name}_${grp.groupNameEn}"
                        val isExpanded = expandedGroupMap[key] != false

                        // Group Parent Row
                        item {
                            Row(
                                modifier = Modifier
                                    .height(38.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Category Name
                                Row(
                                    modifier = Modifier
                                        .width(160.dp)
                                        .fillMaxHeight()
                                        .clickable { expandedGroupMap[key] = !isExpanded }
                                        .padding(horizontal = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
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

                                // Period Values
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
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        }

                        // Subcategory Rows
                        if (isExpanded) {
                            items(grp.subCategories) { sub ->
                                Row(
                                    modifier = Modifier.height(34.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Subcategory Name
                                    Row(
                                        modifier = Modifier
                                            .width(160.dp)
                                            .fillMaxHeight()
                                            .padding(start = 18.dp, end = 6.dp),
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

                                    // Period Values
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
                                .height(30.dp)
                                .background(SolidIncome.copy(alpha = 0.12f)),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "📈 আয়ের খাত" else "📈 Incomes",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SolidIncome,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                        HorizontalDivider(color = SolidIncome.copy(alpha = 0.2f))
                    }

                    displayedIncomeGroups.forEach { grp ->
                        val key = "${grp.type.name}_${grp.groupNameEn}"
                        val isExpanded = expandedGroupMap[key] != false

                        // Group Parent Row
                        item {
                            Row(
                                modifier = Modifier
                                    .height(38.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Category Name
                                Row(
                                    modifier = Modifier
                                        .width(160.dp)
                                        .fillMaxHeight()
                                        .clickable { expandedGroupMap[key] = !isExpanded }
                                        .padding(horizontal = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
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

                                // Period Values
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
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        }

                        // Subcategory Rows
                        if (isExpanded) {
                            items(grp.subCategories) { sub ->
                                Row(
                                    modifier = Modifier.height(34.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Subcategory Name
                                    Row(
                                        modifier = Modifier
                                            .width(160.dp)
                                            .fillMaxHeight()
                                            .padding(start = 18.dp, end = 6.dp),
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

                                    // Period Values
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
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f))
                            }
                        }
                    }
                }

                // 4. Summary Rows (Total Income, Total Expense, Net Surplus)
                item {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)

                    // Total Income Row
                    Row(
                        modifier = Modifier
                            .height(36.dp)
                            .background(SolidIncome.copy(alpha = 0.08f)),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(160.dp)
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
                                fontWeight = FontWeight.Bold,
                                color = SolidIncome,
                                maxLines = 1
                            )
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    // Total Expense Row
                    Row(
                        modifier = Modifier
                            .height(36.dp)
                            .background(SolidExpense.copy(alpha = 0.08f)),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(160.dp)
                                .fillMaxHeight()
                                .padding(horizontal = 8.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "মোট ব্যয়" else "Total Expense",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SolidExpense
                            )
                        }
                        VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

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
                                fontWeight = FontWeight.Bold,
                                color = SolidExpense,
                                maxLines = 1
                            )
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    // Net Surplus Row
                    Row(
                        modifier = Modifier
                            .height(40.dp)
                            .background(SolidPrimary.copy(alpha = 0.12f)),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(160.dp)
                                .fillMaxHeight()
                                .padding(horizontal = 8.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "নেট উদ্বৃত্ত" else "Net Surplus",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SolidPrimary
                            )
                        }
                        VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                        timelineData.netSavingsByPeriod.forEach { amount ->
                            val isPos = amount >= 0
                            Box(
                                modifier = Modifier
                                    .width(92.dp)
                                    .fillMaxHeight()
                                    .padding(horizontal = 6.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Text(
                                    text = (if (isPos) "+" else "") + LanguageHelper.formatCurrency(amount, languageMode),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isPos) SolidIncome else SolidExpense,
                                    maxLines = 1
                                )
                            }
                            VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                        }

                        // Grand Net Surplus
                        val isGrandPos = timelineData.grandNetSavings >= 0
                        Box(
                            modifier = Modifier
                                .width(98.dp)
                                .fillMaxHeight()
                                .background(SolidPrimary.copy(alpha = 0.25f))
                                .padding(horizontal = 6.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Text(
                                text = (if (isGrandPos) "+" else "") + LanguageHelper.formatCurrency(timelineData.grandNetSavings, languageMode),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isGrandPos) SolidIncome else SolidExpense,
                                maxLines = 1
                            )
                        }
                        VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                        // Avg Net Surplus
                        val isAvgPos = timelineData.avgNetSavingsPerPeriod >= 0
                        Box(
                            modifier = Modifier
                                .width(92.dp)
                                .fillMaxHeight()
                                .padding(horizontal = 6.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Text(
                                text = (if (isAvgPos) "+" else "") + LanguageHelper.formatCurrency(timelineData.avgNetSavingsPerPeriod, languageMode),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isAvgPos) SolidIncome else SolidExpense,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
