package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.LanguageMode

/**
 * Dedicated Sort Dialog for Budget Tracking and Budget categories.
 * Allows sorting by Budget Amount, Actual Spent/Earned, Remaining Balance,
 * Utilization %, and Name in both High-to-Low and Low-to-High directions.
 */
@Composable
fun BudgetSortDialog(
    currentSortOrder: BudgetSortOrder,
    isIncome: Boolean = false,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onSelectSort: (BudgetSortOrder) -> Unit
) {
    var selectedOrder by remember { mutableStateOf(currentSortOrder) }
    val isBn = languageMode == LanguageMode.BANGLA

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .heightIn(max = 680.dp)
                .testTag("budget_sort_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Sort,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = if (isBn) "সাজানোর বিকল্প" else "Sort Categories",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isBn) "পছন্দসই ক্রমে বাজেট সাজান" else "Choose ordering for categories",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable Sort Options List
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 1. Budget Amount (High to Low / Low to High)
                    SortSectionHeader(
                        title = if (isIncome) {
                            if (isBn) "আয় লক্ষ্য" else "Target Amount"
                        } else {
                            if (isBn) "বাজেট পরিমাণ" else "Budget Amount"
                        },
                        icon = Icons.Default.AccountBalanceWallet
                    )
                    SortOptionRow(
                        title = BudgetSortOrder.BUDGET_DESC.getLabel(isIncome, languageMode),
                        subtitle = if (isBn) "সর্বোচ্চ বাজেট আগে" else "Highest budget first",
                        isSelected = selectedOrder == BudgetSortOrder.BUDGET_DESC,
                        icon = Icons.AutoMirrored.Filled.TrendingDown,
                        onClick = { selectedOrder = BudgetSortOrder.BUDGET_DESC }
                    )
                    SortOptionRow(
                        title = BudgetSortOrder.BUDGET_ASC.getLabel(isIncome, languageMode),
                        subtitle = if (isBn) "সর্বনিম্ন বাজেট আগে" else "Lowest budget first",
                        isSelected = selectedOrder == BudgetSortOrder.BUDGET_ASC,
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        onClick = { selectedOrder = BudgetSortOrder.BUDGET_ASC }
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // 2. Actual (Spent / Earned)
                    SortSectionHeader(
                        title = if (isIncome) {
                            if (isBn) "অর্জিত আয়" else "Actual Earned"
                        } else {
                            if (isBn) "প্রকৃত ব্যয়" else "Actual Spent"
                        },
                        icon = Icons.Default.SwapVert
                    )
                    SortOptionRow(
                        title = BudgetSortOrder.SPENT_DESC.getLabel(isIncome, languageMode),
                        subtitle = if (isBn) "সর্বাধিক লেনদেনকৃত আগে" else "Highest amount first",
                        isSelected = selectedOrder == BudgetSortOrder.SPENT_DESC || selectedOrder == BudgetSortOrder.AMOUNT_DESC,
                        icon = Icons.AutoMirrored.Filled.TrendingDown,
                        onClick = { selectedOrder = BudgetSortOrder.SPENT_DESC }
                    )
                    SortOptionRow(
                        title = BudgetSortOrder.SPENT_ASC.getLabel(isIncome, languageMode),
                        subtitle = if (isBn) "সর্বনিম্ন লেনদেনকৃত আগে" else "Lowest amount first",
                        isSelected = selectedOrder == BudgetSortOrder.SPENT_ASC || selectedOrder == BudgetSortOrder.AMOUNT_ASC,
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        onClick = { selectedOrder = BudgetSortOrder.SPENT_ASC }
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // 3. Remaining
                    SortSectionHeader(
                        title = if (isIncome) {
                            if (isBn) "অর্জনের বাকি" else "Remaining to Earn"
                        } else {
                            if (isBn) "অবশিষ্ট বাজেট" else "Remaining Budget"
                        },
                        icon = Icons.Default.AccountBalanceWallet
                    )
                    SortOptionRow(
                        title = BudgetSortOrder.REMAINING_DESC.getLabel(isIncome, languageMode),
                        subtitle = if (isBn) "সবচেয়ে বেশি অবশিষ্ট আগে" else "Most remaining balance first",
                        isSelected = selectedOrder == BudgetSortOrder.REMAINING_DESC,
                        icon = Icons.AutoMirrored.Filled.TrendingDown,
                        onClick = { selectedOrder = BudgetSortOrder.REMAINING_DESC }
                    )
                    SortOptionRow(
                        title = BudgetSortOrder.REMAINING_ASC.getLabel(isIncome, languageMode),
                        subtitle = if (isBn) "সবচেয়ে কম অবশিষ্ট / ঘাটতি আগে" else "Least remaining or deficit first",
                        isSelected = selectedOrder == BudgetSortOrder.REMAINING_ASC,
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        onClick = { selectedOrder = BudgetSortOrder.REMAINING_ASC }
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // 4. Utilization / % Progress
                    SortSectionHeader(
                        title = if (isBn) "ব্যবহারের হার (%)" else "Utilization %",
                        icon = Icons.Default.PieChart
                    )
                    SortOptionRow(
                        title = BudgetSortOrder.UTILIZATION_DESC.getLabel(isIncome, languageMode),
                        subtitle = if (isBn) "সর্বোচ্চ অগ্রগতি / ব্যবহারের হার আগে" else "Highest % progress first",
                        isSelected = selectedOrder == BudgetSortOrder.UTILIZATION_DESC,
                        icon = Icons.AutoMirrored.Filled.TrendingDown,
                        onClick = { selectedOrder = BudgetSortOrder.UTILIZATION_DESC }
                    )
                    SortOptionRow(
                        title = BudgetSortOrder.UTILIZATION_ASC.getLabel(isIncome, languageMode),
                        subtitle = if (isBn) "সর্বনিম্ন অগ্রগতি আগে" else "Lowest % progress first",
                        isSelected = selectedOrder == BudgetSortOrder.UTILIZATION_ASC,
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        onClick = { selectedOrder = BudgetSortOrder.UTILIZATION_ASC }
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // 5. Alphabetical & Default
                    SortSectionHeader(
                        title = if (isBn) "অন্যান্য ক্রম" else "Other Ordering",
                        icon = Icons.Default.SortByAlpha
                    )
                    SortOptionRow(
                        title = BudgetSortOrder.NAME_ASC.getLabel(isIncome, languageMode),
                        subtitle = if (isBn) "বর্ণানুক্রমিক (A থেকে Z)" else "Alphabetical A to Z",
                        isSelected = selectedOrder == BudgetSortOrder.NAME_ASC,
                        icon = Icons.Default.SortByAlpha,
                        onClick = { selectedOrder = BudgetSortOrder.NAME_ASC }
                    )
                    SortOptionRow(
                        title = BudgetSortOrder.NAME_DESC.getLabel(isIncome, languageMode),
                        subtitle = if (isBn) "বর্ণানুক্রমিক (Z থেকে A)" else "Alphabetical Z to A",
                        isSelected = selectedOrder == BudgetSortOrder.NAME_DESC,
                        icon = Icons.Default.SortByAlpha,
                        onClick = { selectedOrder = BudgetSortOrder.NAME_DESC }
                    )
                    SortOptionRow(
                        title = BudgetSortOrder.DEFAULT.getLabel(isIncome, languageMode),
                        subtitle = if (isBn) "মূল ডিফল্ট সাজানো ক্রম" else "Original custom order",
                        isSelected = selectedOrder == BudgetSortOrder.DEFAULT,
                        icon = Icons.Default.RestartAlt,
                        onClick = { selectedOrder = BudgetSortOrder.DEFAULT }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(12.dp))

                // Footer Actions: Reset & Apply
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            selectedOrder = BudgetSortOrder.DEFAULT
                            onSelectSort(BudgetSortOrder.DEFAULT)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isBn) "রিসেট" else "Reset",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Button(
                        onClick = {
                            onSelectSort(selectedOrder)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("apply_budget_sort_btn"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isBn) "প্রয়োগ করুন" else "Apply",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SortSectionHeader(
    title: String,
    icon: ImageVector
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun SortOptionRow(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    icon: ImageVector,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
    val bgColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = bgColor,
        border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 13.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.primary,
                    unselectedColor = MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
