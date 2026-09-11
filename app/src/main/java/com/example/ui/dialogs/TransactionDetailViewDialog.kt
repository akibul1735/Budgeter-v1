package com.example.ui.dialogs

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
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
import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionType
import com.example.data.model.TransactionWithDetails
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidExpenseContainer
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidIncomeContainer
import com.example.ui.theme.SolidPrimary
import com.example.ui.theme.SolidPrimaryContainer
import com.example.util.DateUtils
import com.example.util.IconHelper
import com.example.util.LanguageHelper

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TransactionDetailViewDialog(
    item: TransactionWithDetails,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onEdit: (Transaction) -> Unit,
    onShowSimilar: (String) -> Unit
) {
    val tx = item.transaction
    val context = LocalContext.current

    val isExpense = tx.type == TransactionType.EXPENSE
    val isIncome = tx.type == TransactionType.INCOME
    val isTransfer = tx.type == TransactionType.TRANSFER

    val amtColor = when {
        isTransfer -> SolidPrimary
        isIncome -> if (tx.amount >= 0) SolidIncome else SolidExpense
        else -> if (tx.amount >= 0) SolidExpense else SolidIncome
    }

    val typeLabel = when (tx.type) {
        TransactionType.EXPENSE -> if (languageMode == LanguageMode.BANGLA) "খরচ (Expense)" else "Expense"
        TransactionType.INCOME -> if (languageMode == LanguageMode.BANGLA) "আয় (Income)" else "Income"
        TransactionType.TRANSFER -> if (languageMode == LanguageMode.BANGLA) "স্থানান্তর (Transfer)" else "Transfer"
    }

    val typeIcon: ImageVector = when (tx.type) {
        TransactionType.EXPENSE -> Icons.AutoMirrored.Filled.TrendingDown
        TransactionType.INCOME -> Icons.AutoMirrored.Filled.TrendingUp
        TransactionType.TRANSFER -> Icons.Default.SwapHoriz
    }

    val typeBg = when (tx.type) {
        TransactionType.EXPENSE -> SolidExpenseContainer
        TransactionType.INCOME -> SolidIncomeContainer
        TransactionType.TRANSFER -> SolidPrimaryContainer
    }

    val sign = when {
        isTransfer -> ""
        isIncome -> if (tx.amount >= 0) "+" else "−"
        else -> if (tx.amount >= 0) "−" else "+"
    }

    // Determine query string for "Show Similar transactions"
    val similarQuery = when {
        tx.payeeOrPayer.isNotBlank() -> tx.payeeOrPayer
        item.subCategory != null -> item.subCategory.nameEn
        item.category != null -> item.category.nameEn
        else -> ""
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            shadowElevation = 10.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 480.dp)
                .padding(vertical = 24.dp)
                .testTag("transaction_view_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                // Top Row: Type Pill + Status + Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Type Pill
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = typeBg,
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = typeIcon,
                                contentDescription = null,
                                tint = amtColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = typeLabel,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = amtColor
                            )
                        }
                    }

                    // Status Badge if available
                    if (tx.status != TransactionStatus.NONE) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                if (tx.status == TransactionStatus.CLEARED || tx.status == TransactionStatus.RECONCILED) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                val statusTitle = if (languageMode == LanguageMode.BANGLA) tx.status.titleBn else tx.status.titleEn
                                Text(
                                    text = statusTitle,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }

                    // Close Button
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

                Spacer(modifier = Modifier.height(16.dp))

                // Hero Amount Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .padding(vertical = 16.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$sign${LanguageHelper.formatCurrency(kotlin.math.abs(tx.amount), languageMode)}",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = amtColor,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    val formattedDateTime = "${DateUtils.formatDate(tx.dateEpochMs, languageMode)} • ${DateUtils.formatTime(tx.dateEpochMs, languageMode)}"
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formattedDateTime,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Scrollable Details Body
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 1. Payee / Title
                    if (tx.payeeOrPayer.isNotBlank()) {
                        DetailItemRow(
                            icon = Icons.Default.Person,
                            label = if (languageMode == LanguageMode.BANGLA) "পেয়ী / প্রাপক" else "Payee / Name",
                            value = tx.payeeOrPayer
                        )
                    }

                    // 2. Category & Subcategory
                    if (item.category != null || item.subCategory != null) {
                        val catName = item.category?.localizedName(languageMode) ?: ""
                        val subName = item.subCategory?.localizedName(languageMode)
                        val catDisplay = if (subName != null && subName != catName) "$catName  ➔  $subName" else catName

                        DetailItemRow(
                            icon = IconHelper.getIconByName(item.category?.iconName ?: "Category"),
                            label = if (languageMode == LanguageMode.BANGLA) "ক্যাটাগরি" else "Category",
                            value = catDisplay
                        )
                    }

                    // 3. Account(s) with ⩓ / ⩔ Carets
                    val accountDisplay = when (tx.type) {
                        TransactionType.EXPENSE -> {
                            val name = item.creditAccount?.localizedName(languageMode) ?: "-"
                            val caret = if (tx.amount >= 0) "⩔" else "⩓"
                            "$name  $caret"
                        }
                        TransactionType.INCOME -> {
                            val name = item.debitAccount?.localizedName(languageMode) ?: "-"
                            val caret = if (tx.amount >= 0) "⩓" else "⩔"
                            "$name  $caret"
                        }
                        TransactionType.TRANSFER -> {
                            val from = item.creditAccount?.localizedName(languageMode) ?: "-"
                            val to = item.debitAccount?.localizedName(languageMode) ?: "-"
                            "$from (⩔)  ➔  $to (⩓)"
                        }
                    }

                    DetailItemRow(
                        icon = Icons.Default.AccountBalance,
                        label = if (languageMode == LanguageMode.BANGLA) "অ্যাকাউন্ট" else "Account",
                        value = accountDisplay,
                        valueColor = MaterialTheme.colorScheme.onSurface
                    )

                    // 4. Labels / Tags
                    if (tx.referenceNo.isNotBlank()) {
                        val labelList = tx.referenceNo.split(",").map { it.trim() }.filter { it.isNotBlank() }
                        if (labelList.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Label,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "লেবেল" else "Labels",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        labelList.forEach { lbl ->
                                            Surface(
                                                shape = CircleShape,
                                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                                                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                            ) {
                                                Text(
                                                    text = "#$lbl",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 5. Notes / Memo
                    if (tx.note.isNotBlank()) {
                        DetailItemRow(
                            icon = Icons.AutoMirrored.Filled.Notes,
                            label = if (languageMode == LanguageMode.BANGLA) "নোট" else "Note",
                            value = tx.note
                        )
                    }

                    // 6. Attachment indicator
                    if (tx.attachmentUri.isNotBlank()) {
                        DetailItemRow(
                            icon = Icons.Default.AttachFile,
                            label = if (languageMode == LanguageMode.BANGLA) "সংযুক্তি" else "Attachment",
                            value = if (languageMode == LanguageMode.BANGLA) "ফাইল সংযুক্ত রয়েছে" else "File attached"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Actions: "Show Similar transactions" text button (left) and Edit button (right)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Show Similar Transactions Text Button
                    if (similarQuery.isNotBlank()) {
                        TextButton(
                            onClick = {
                                onShowSimilar(similarQuery)
                                Toast.makeText(
                                    context,
                                    if (languageMode == LanguageMode.BANGLA) "অনুরূপ লেনদেন দেখানো হচ্ছে: $similarQuery" else "Showing similar transactions for: $similarQuery",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            modifier = Modifier.testTag("show_similar_transactions_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "অনুরূপ লেনদেন দেখুন" else "Show Similar transactions",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    // Bottom Right: Edit Button
                    Button(
                        onClick = {
                            onEdit(tx)
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.testTag("edit_transaction_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "সম্পাদনা" else "Edit",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailItemRow(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                fontSize = 13.5.sp,
                color = valueColor,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
