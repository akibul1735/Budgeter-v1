package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Account
import com.example.data.model.AccountRequirementAnalysis
import com.example.data.model.AccountRequirementItem
import com.example.data.model.CategoryAllocationAnalysis
import com.example.data.model.LanguageMode
import com.example.data.model.OtherAccountAllocationAnalysis
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidPrimary
import com.example.ui.theme.SolidTransfer
import com.example.util.IconHelper
import com.example.util.LanguageHelper

@Composable
internal fun ItemGroupHeader(
    title: String,
    count: Int,
    iconName: String,
    color: Color
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 2.dp),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val isImg = IconHelper.isDrawableIcon(iconName) || IconHelper.isCustomIcon(iconName)
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(if (isImg) Color.Transparent else color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    IconHelper.AppIcon(
                        iconName = iconName,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(if (isImg) 22.dp else 13.dp)
                    )
                }
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = color.copy(alpha = 0.12f)
            ) {
                Text(
                    text = "$count",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = color,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
internal fun SectionHeader(
    title: String,
    count: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(15.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = color.copy(alpha = 0.12f)
        ) {
            Text(
                text = "$count",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
internal fun OtherAccountAllocationCard(
    allocation: OtherAccountAllocationAnalysis,
    languageMode: LanguageMode,
    onOpenSplit: () -> Unit,
    onAddTransaction: () -> Unit
) {
    val acc = allocation.account
    val accColor = remember(acc.colorHex) {
        try {
            Color(android.graphics.Color.parseColor(acc.colorHex))
        } catch (e: Exception) {
            if (allocation.isExpense) SolidExpense else SolidIncome
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("other_acc_card_${acc.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header Row: Account Icon, Name, Type Badge, Split Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    val isImg = IconHelper.isDrawableIcon(acc.iconName) || IconHelper.isCustomIcon(acc.iconName)
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (isImg) Color.Transparent else accColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        IconHelper.AppIcon(
                            iconName = acc.iconName,
                            contentDescription = null,
                            tint = accColor,
                            modifier = Modifier.size(if (isImg) 40.dp else 22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = acc.localizedName(languageMode),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            // Type / Liability badge
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (allocation.isExpense) SolidExpense.copy(alpha = 0.12f) else SolidIncome.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = if (allocation.isExpense) (if (languageMode == LanguageMode.BANGLA) "দেনা / দায়" else "Payable")
                                    else (if (languageMode == LanguageMode.BANGLA) "পাওনা / আয়" else "Receivable"),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (allocation.isExpense) SolidExpense else SolidIncome,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = "${if (allocation.isExpense) "Due/Target" else "Expected"}: ${LanguageHelper.formatCurrency(allocation.totalBudgeted, languageMode)}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                Button(
                    onClick = onOpenSplit,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (allocation.isMultiAccount) SolidTransfer else SolidPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = if (allocation.isMultiAccount) Icons.Default.CallSplit else Icons.Default.Tune,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (allocation.isMultiAccount) "Split (${allocation.accountSplits.size})"
                        else if (allocation.accountSplits.isNotEmpty()) (if (languageMode == LanguageMode.BANGLA) "বরাদ্দকৃত" else "Assigned")
                        else (if (languageMode == LanguageMode.BANGLA) "বরাদ্দ করুন" else "Assign"),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Assigned Payment Sources Breakdown
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    if (allocation.accountSplits.isEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = SolidExpense, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "কোনো সোর্স নির্ধারিত নেই (ডিফল্ট হিসাব ব্যবহৃত হবে)" else "No payment source assigned (fallback used)",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        allocation.accountSplits.forEach { split ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val isSplitImg = IconHelper.isDrawableIcon(split.account.iconName) || IconHelper.isCustomIcon(split.account.iconName)
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(if (isSplitImg) Color.Transparent else SolidPrimary.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        IconHelper.AppIcon(
                                            iconName = split.account.iconName,
                                            contentDescription = null,
                                            tint = SolidPrimary,
                                            modifier = Modifier.size(if (isSplitImg) 16.dp else 12.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = split.account.localizedName(languageMode),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    if (allocation.isMultiAccount && split.percentageOfCategory > 0) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "(${split.percentageOfCategory.toInt()}%)",
                                            fontSize = 9.sp,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }

                                Text(
                                    text = LanguageHelper.formatCurrency(split.allocatedAmount, languageMode),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (allocation.isExpense) SolidExpense else SolidIncome
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun AccountRequirementCard(
    analysis: AccountRequirementAnalysis,
    overviewTotalAssigned: Double,
    languageMode: LanguageMode,
    onAccountClick: (Long) -> Unit,
    onAssignItem: (Account) -> Unit
) {
    val assignedPercentage = if (overviewTotalAssigned > 0) {
        (analysis.requiredExpenseAmount / overviewTotalAssigned) * 100.0
    } else 0.0
    val assignedItemCount = analysis.itemizedExpenses.size + analysis.itemizedIncomes.size

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onAccountClick(analysis.account.id) }
            .testTag("acc_req_card_${analysis.account.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Top Row: Account Name, Balance & Shortfall/Surplus Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    val isImg = IconHelper.isDrawableIcon(analysis.account.iconName) || IconHelper.isCustomIcon(analysis.account.iconName)
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(if (isImg) Color.Transparent else SolidPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        IconHelper.AppIcon(
                            iconName = analysis.account.iconName,
                            contentDescription = null,
                            tint = SolidPrimary,
                            modifier = Modifier.size(if (isImg) 42.dp else 24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = analysis.account.localizedName(languageMode),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Balance: ${LanguageHelper.formatCurrency(analysis.currentBalance, languageMode)}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            // Assigned Percentage Badge
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = SolidPrimary.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = "${String.format(java.util.Locale.US, "%.1f", assignedPercentage)}% ${if (languageMode == LanguageMode.BANGLA) "বরাদ্দ" else "Assigned"}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SolidPrimary,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                }

                // Status Badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (analysis.isShortfall) SolidExpense.copy(alpha = 0.15f)
                    else if (analysis.isSurplus) SolidIncome.copy(alpha = 0.15f)
                    else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = if (analysis.isShortfall) "Need ${LanguageHelper.formatCurrency(analysis.shortfall, languageMode)}"
                        else if (analysis.isSurplus) "Surplus ${LanguageHelper.formatCurrency(analysis.surplus, languageMode)}"
                        else "Balanced",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (analysis.isShortfall) SolidExpense else if (analysis.isSurplus) SolidIncome else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Middle Requirement Progress & Figures
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "প্রয়োজনীয় ব্যয়" else "Required Expenses",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = LanguageHelper.formatCurrency(analysis.requiredExpenseAmount, languageMode),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SolidExpense
                    )
                }

                if (analysis.expectedIncomeAmount > 0) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "প্রত্যাশিত আয়" else "Expected Income",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = "+${LanguageHelper.formatCurrency(analysis.expectedIncomeAmount, languageMode)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SolidIncome
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "কার্যকর মজুদ" else "Available Funds",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = LanguageHelper.formatCurrency(analysis.availableAmount, languageMode),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (analysis.availableAmount >= analysis.requiredExpenseAmount) SolidIncome else SolidExpense
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { analysis.fundingCoverageRatio.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (analysis.isShortfall) SolidExpense else SolidIncome,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(6.dp))

            // Bottom Action Row: Tap prompt on left, + Assign button on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onAccountClick(analysis.account.id) }
                ) {
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "বরাদ্দকৃত আইটেম দেখুন ($assignedItemCount টি) →" else "View Assigned Items ($assignedItemCount) →",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SolidPrimary
                    )
                }

                Button(
                    onClick = { onAssignItem(analysis.account) },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SolidPrimary),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 5.dp),
                    modifier = Modifier.testTag("btn_assign_item_${analysis.account.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "বরাদ্দ করুন" else "Assign Item",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
internal fun ItemizedRequirementRow(
    item: AccountRequirementItem,
    languageMode: LanguageMode
) {
    val itemColor = remember(item.colorHex) {
        try {
            Color(android.graphics.Color.parseColor(item.colorHex))
        } catch (e: Exception) {
            if (item.isExpense) SolidExpense else SolidIncome
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(itemColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = IconHelper.getIconByName(item.iconName),
                    contentDescription = null,
                    tint = itemColor,
                    modifier = Modifier.size(11.dp)
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = item.title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (item.isMultiAccountSplit) {
                    Text(
                        text = "Split (${item.splitAccountCount} sources)",
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }

        Text(
            text = (if (item.isExpense) "-" else "+") + LanguageHelper.formatCurrency(item.amount, languageMode),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (item.isExpense) SolidExpense else SolidIncome
        )
    }
}

@Composable
internal fun CategoryAllocationCard(
    allocation: CategoryAllocationAnalysis,
    isExpense: Boolean,
    languageMode: LanguageMode,
    onOpenSplit: () -> Unit
) {
    val cat = allocation.category
    val catColor = remember(cat.colorHex) {
        try {
            Color(android.graphics.Color.parseColor(cat.colorHex))
        } catch (e: Exception) {
            if (isExpense) SolidExpense else SolidIncome
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("cat_alloc_card_${cat.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header Row: Category Icon, Name, Split Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(catColor.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = IconHelper.getIconByName(cat.iconName),
                            contentDescription = null,
                            tint = catColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = cat.localizedName(languageMode),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (isExpense) {
                                "${if (languageMode == LanguageMode.BANGLA) "বাজেট" else "Budget"}: ${LanguageHelper.formatCurrency(allocation.totalBudgeted, languageMode)} • ${if (languageMode == LanguageMode.BANGLA) "অবশিষ্ট" else "Remaining"}: ${LanguageHelper.formatCurrency(allocation.totalRemaining, languageMode)}"
                            } else {
                                "${if (languageMode == LanguageMode.BANGLA) "প্রত্যাশিত" else "Expected"}: ${LanguageHelper.formatCurrency(allocation.totalBudgeted, languageMode)} • ${if (languageMode == LanguageMode.BANGLA) "অবশিষ্ট" else "Remaining"}: ${LanguageHelper.formatCurrency(allocation.totalRemaining, languageMode)}"
                            },
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Button(
                    onClick = onOpenSplit,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (allocation.isMultiAccount) SolidTransfer else SolidPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = if (allocation.isMultiAccount) Icons.Default.CallSplit else Icons.Default.Edit,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (allocation.isMultiAccount) "Split (${allocation.accountSplits.size})"
                        else if (allocation.accountSplits.isNotEmpty()) (if (languageMode == LanguageMode.BANGLA) "বরাদ্দকৃত" else "Assigned")
                        else (if (languageMode == LanguageMode.BANGLA) "বরাদ্দ করুন" else "Assign"),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Assigned Account(s) Breakdown
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    if (allocation.accountSplits.isEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = SolidExpense, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "কোনো একাউন্ট নির্ধারিত নেই (ডিফল্ট হিসাব ব্যবহৃত হবে)" else "No account assigned (fallback account will be used)",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        allocation.accountSplits.forEach { split ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val isSplitImg = IconHelper.isDrawableIcon(split.account.iconName) || IconHelper.isCustomIcon(split.account.iconName)
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(if (isSplitImg) Color.Transparent else SolidPrimary.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        IconHelper.AppIcon(
                                            iconName = split.account.iconName,
                                            contentDescription = null,
                                            tint = SolidPrimary,
                                            modifier = Modifier.size(if (isSplitImg) 16.dp else 12.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = split.account.localizedName(languageMode),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    if (allocation.isMultiAccount && split.percentageOfCategory > 0) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "(${split.percentageOfCategory.toInt()}%)",
                                            fontSize = 9.sp,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }

                                Text(
                                    text = LanguageHelper.formatCurrency(split.allocatedAmount, languageMode),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isExpense) SolidExpense else SolidIncome
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun BasisTabPill(
    selected: Boolean,
    text: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (selected) SolidPrimary else Color.Transparent,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Composable
internal fun EmptyStateCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = message, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
internal fun CompactAssignedFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    leadingIcon: (@Composable () -> Unit)? = null,
    selectedColor: Color = SolidPrimary,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(6.dp),
        color = if (selected) selectedColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.dp,
            if (selected) selectedColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        modifier = modifier.height(28.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (leadingIcon != null) {
                leadingIcon()
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) selectedColor else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
