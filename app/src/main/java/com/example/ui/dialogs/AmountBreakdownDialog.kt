package com.example.ui.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.LanguageMode
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.data.model.TransactionWithDetails
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidPrimary
import com.example.ui.theme.SolidTransfer
import com.example.util.DateUtils
import com.example.util.IconHelper
import com.example.util.LanguageHelper

/**
 * Data model for describing how any amount in the app is calculated and what transactions compose it.
 */
data class AmountDetailInfo(
    val title: String,
    val totalAmount: Double,
    val subtitle: String? = null,
    val formulaExplanation: String? = null,
    val formulaSteps: List<FormulaStep> = emptyList(),
    val relatedTransactions: List<TransactionWithDetails> = emptyList(),
    val relatedBreakdownItems: List<BreakdownItem> = emptyList(),
    val isPositiveGood: Boolean = true,
    val statusTag: String? = null,
    val customBadgeColor: Color? = null
)

data class FormulaStep(
    val label: String,
    val amount: Double,
    val operator: String = "+", // "+", "−", "×", "=", etc.
    val isHighlighted: Boolean = false,
    val note: String? = null
)

data class BreakdownItem(
    val name: String,
    val amount: Double,
    val percentage: Double? = null,
    val iconName: String? = null,
    val color: Color? = null,
    val count: Int? = null
)

@Composable
fun AmountBreakdownDialog(
    info: AmountDetailInfo,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onTransactionClick: ((Transaction) -> Unit)? = null
) {
    var selectedTab by remember {
        mutableIntStateOf(
            if (info.formulaSteps.isNotEmpty() || info.formulaExplanation != null || info.relatedBreakdownItems.isNotEmpty()) 0 else 1
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.85f)
                .testTag("amount_breakdown_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Header with Title and Close Button
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(SolidPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Calculate,
                                    contentDescription = null,
                                    tint = SolidPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = info.title,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (!info.subtitle.isNullOrBlank()) {
                                    Text(
                                        text = info.subtitle,
                                        fontSize = 11.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Amount Highlight Banner
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp, horizontal = 18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "হিসাবকৃত পরিমাণ" else "Total Calculated Amount",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = LanguageHelper.formatCurrency(info.totalAmount, languageMode),
                                fontSize = 26.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = when {
                                    info.customBadgeColor != null -> info.customBadgeColor
                                    info.totalAmount < 0 -> SolidExpense
                                    else -> MaterialTheme.colorScheme.onSurface
                                }
                            )
                            if (!info.statusTag.isNullOrBlank()) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = info.customBadgeColor ?: if (info.totalAmount >= 0) SolidIncome else SolidExpense
                                ) {
                                    Text(
                                        text = info.statusTag,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                val hasCalc = info.formulaSteps.isNotEmpty() || info.formulaExplanation != null || info.relatedBreakdownItems.isNotEmpty()
                val hasTx = info.relatedTransactions.isNotEmpty()

                if (hasCalc && hasTx) {
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Functions, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "হিসাব সূত্র" else "Calculation",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${if (languageMode == LanguageMode.BANGLA) "লেনদেন" else "Transactions"} (${info.relatedTransactions.size})",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        )
                    }
                }

                // Content body
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    if (selectedTab == 0 && hasCalc) {
                        CalculationBreakdownView(
                            info = info,
                            languageMode = languageMode
                        )
                    } else if (hasTx) {
                        TransactionsBreakdownView(
                            transactions = info.relatedTransactions,
                            languageMode = languageMode,
                            onTransactionClick = onTransactionClick
                        )
                    } else if (hasCalc) {
                        CalculationBreakdownView(
                            info = info,
                            languageMode = languageMode
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "কোন অতিরিক্ত বিবরণ নেই" else "No contributing items recorded.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                // Bottom Action Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(onClick = onDismiss),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "ঠিক আছে" else "Close",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CalculationBreakdownView(
    info: AmountDetailInfo,
    languageMode: LanguageMode
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (!info.formulaExplanation.isNullOrBlank()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = info.formulaExplanation,
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        if (info.formulaSteps.isNotEmpty()) {
            item {
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "ধাপে ধাপে হিসাব প্রক্রিয়া" else "Step-by-Step Breakdown",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            items(info.formulaSteps) { step ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = if (step.isHighlighted) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    border = if (step.isHighlighted) BorderStroke(1.dp, SolidPrimary.copy(alpha = 0.5f)) else null
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Surface(
                                shape = CircleShape,
                                color = if (step.operator == "=") SolidPrimary else MaterialTheme.colorScheme.surface,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = step.operator,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (step.operator == "=") Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = step.label,
                                    fontSize = 13.sp,
                                    fontWeight = if (step.isHighlighted) FontWeight.Bold else FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (!step.note.isNullOrBlank()) {
                                    Text(
                                        text = step.note,
                                        fontSize = 10.5.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }

                        Text(
                            text = LanguageHelper.formatCurrency(step.amount, languageMode),
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                step.operator == "−" -> SolidExpense
                                step.operator == "+" -> SolidIncome
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
            }
        }

        if (info.relatedBreakdownItems.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "উপাদান ভিত্তিক বিস্তারিত" else "Component Breakdown",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            items(info.relatedBreakdownItems) { item ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            if (item.iconName != null) {
                                Icon(
                                    imageVector = IconHelper.getIconByName(item.iconName),
                                    contentDescription = null,
                                    tint = item.color ?: MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Column {
                                Text(
                                    text = item.name,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (item.count != null && item.count > 0) {
                                    Text(
                                        text = "${item.count} ${if (languageMode == LanguageMode.BANGLA) "টি এন্ট্রি" else "entries"}",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = LanguageHelper.formatCurrency(item.amount, languageMode),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = item.color ?: MaterialTheme.colorScheme.onSurface
                            )
                            if (item.percentage != null) {
                                Text(
                                    text = String.format("%.1f%%", item.percentage),
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.outline
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
private fun TransactionsBreakdownView(
    transactions: List<TransactionWithDetails>,
    languageMode: LanguageMode,
    onTransactionClick: ((Transaction) -> Unit)?
) {
    if (transactions.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (languageMode == LanguageMode.BANGLA) "কোন সম্পর্কিত লেনদেন পাওয়া যায়নি" else "No related transactions found.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.outline
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(transactions, key = { it.transaction.id }) { txWithDetails ->
                val tx = txWithDetails.transaction
                val typeColor = when (tx.type) {
                    TransactionType.EXPENSE -> SolidExpense
                    TransactionType.INCOME -> SolidIncome
                    TransactionType.TRANSFER -> SolidTransfer
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(enabled = onTransactionClick != null) {
                            onTransactionClick?.invoke(tx)
                        },
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(typeColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (tx.type) {
                                        TransactionType.EXPENSE -> Icons.AutoMirrored.Filled.TrendingDown
                                        TransactionType.INCOME -> Icons.AutoMirrored.Filled.TrendingUp
                                        TransactionType.TRANSFER -> Icons.Default.SwapHoriz
                                    },
                                    contentDescription = null,
                                    tint = typeColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Text(
                                    text = if (tx.payeeOrPayer.isNotBlank()) tx.payeeOrPayer else txWithDetails.category?.nameEn ?: "Transaction",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = DateUtils.formatShortDate(tx.dateEpochMs, languageMode),
                                        fontSize = 10.5.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                    val accName = txWithDetails.debitAccount?.localizedName(languageMode)
                                        ?: txWithDetails.creditAccount?.localizedName(languageMode)
                                    if (!accName.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "•", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = accName,
                                            fontSize = 10.5.sp,
                                            color = MaterialTheme.colorScheme.outline,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }

                        Text(
                            text = (if (tx.type == TransactionType.EXPENSE) "−" else if (tx.type == TransactionType.INCOME) "+" else "") +
                                    LanguageHelper.formatCurrency(tx.amount, languageMode),
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = typeColor
                        )
                    }
                }
            }
        }
    }
}
