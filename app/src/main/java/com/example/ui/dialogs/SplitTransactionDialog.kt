package com.example.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Account
import com.example.data.model.Category
import com.example.data.model.CategoryType
import com.example.data.model.LanguageMode
import com.example.data.model.TransactionSplitItem
import com.example.data.model.TransactionType
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidPrimary
import com.example.util.IconHelper
import com.example.util.LanguageHelper
import java.util.Locale

internal data class EditableSplitLine(
    val id: Long = 0L,
    var type: TransactionType = TransactionType.EXPENSE,
    var categoryId: Long? = null,
    var subCategoryId: Long? = null,
    var debitAccountId: Long? = null,
    var creditAccountId: Long? = null,
    var amountText: String = "",
    var note: String = "",
    var payeeOrPayer: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitTransactionDialog(
    totalAmount: Double,
    initialItems: List<TransactionSplitItem>,
    categories: List<Category>,
    accounts: List<Account>,
    txType: TransactionType,
    defaultDebitAccountId: Long? = null,
    defaultCreditAccountId: Long? = null,
    defaultCategoryId: Long? = null,
    defaultSubCategoryId: Long? = null,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onApplySplit: (List<TransactionSplitItem>, Double) -> Unit,
    onRevertToSingle: (() -> Unit)? = null,
    onAddNewCategory: ((Category) -> Unit)? = null,
    onAddNewAccount: ((Account) -> Unit)? = null
) {
    var overallAmount by remember { mutableStateOf(totalAmount) }
    val isBangla = languageMode == LanguageMode.BANGLA
    val currencySymbol = LanguageHelper.activeCurrencyConfig.activeSymbol

    val defaultAccId = when (txType) {
        TransactionType.EXPENSE -> defaultCreditAccountId ?: accounts.firstOrNull { it.parentId != null }?.id ?: accounts.firstOrNull()?.id
        TransactionType.INCOME -> defaultDebitAccountId ?: accounts.firstOrNull { it.parentId != null }?.id ?: accounts.firstOrNull()?.id
        TransactionType.TRANSFER -> defaultCreditAccountId ?: accounts.firstOrNull { it.parentId != null }?.id ?: accounts.firstOrNull()?.id
    }

    // Initialize split lines
    val lines = remember {
        mutableStateListOf<EditableSplitLine>().apply {
            if (initialItems.isNotEmpty()) {
                addAll(
                    initialItems.map {
                        EditableSplitLine(
                            id = it.id,
                            type = it.type,
                            categoryId = it.categoryId,
                            subCategoryId = it.subCategoryId,
                            debitAccountId = it.debitAccountId,
                            creditAccountId = it.creditAccountId ?: defaultAccId,
                            amountText = if (it.amount > 0) String.format(Locale.US, "%.2f", it.amount) else "",
                            note = it.note,
                            payeeOrPayer = it.payeeOrPayer
                        )
                    }
                )
            } else {
                // Default two split rows
                val defaultCat1 = defaultCategoryId ?: categories.firstOrNull { it.parentId == null }?.id
                val defaultCat2 = categories.filter { it.parentId == null && it.id != defaultCat1 }.firstOrNull()?.id ?: defaultCat1
                val half = if (totalAmount > 0) totalAmount / 2.0 else 0.0
                val halfStr = if (half > 0) String.format(Locale.US, "%.2f", half) else ""
                val rest = if (totalAmount > 0) totalAmount - half else 0.0
                val restStr = if (rest > 0) String.format(Locale.US, "%.2f", rest) else ""

                add(
                    EditableSplitLine(
                        type = txType,
                        categoryId = defaultCat1,
                        subCategoryId = defaultSubCategoryId,
                        creditAccountId = defaultCreditAccountId ?: defaultAccId,
                        debitAccountId = defaultDebitAccountId,
                        amountText = halfStr
                    )
                )
                add(
                    EditableSplitLine(
                        type = txType,
                        categoryId = defaultCat2,
                        creditAccountId = defaultCreditAccountId ?: defaultAccId,
                        debitAccountId = defaultDebitAccountId,
                        amountText = restStr
                    )
                )
            }
        }
    }

    // Modal pickers state
    var pickingCategoryLineIndex by remember { mutableStateOf<Int?>(null) }
    var pickingAccountLineIndex by remember { mutableStateOf<Int?>(null) }

    // Computations
    val sumOfSplits = lines.sumOf { it.amountText.toDoubleOrNull() ?: 0.0 }
    val remaining = overallAmount - sumOfSplits
    val isBalanced = Math.abs(remaining) < 0.01

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.94f)
                .padding(vertical = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SolidPrimary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CallSplit,
                                contentDescription = null,
                                tint = SolidPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isBangla) "লেনদেন বিভাজন (স্প্লিট)" else "Split Transaction",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isBangla) "একাধিক ক্যাটাগরি, অ্যাকাউন্ট ও দেনা-পাওনা" else "Multi-category, multi-account & receivables",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(34.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Summary Card (Total, Allocated, Remaining)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (isBangla) "মোট বিলের পরিমাণ" else "Total Bill Amount",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "$currencySymbol ${LanguageHelper.formatNumber(overallAmount, languageMode)}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                // Split Evenly Shortcut
                                if (lines.isNotEmpty() && overallAmount > 0) {
                                    OutlinedButton(
                                        onClick = {
                                            val splitShare = overallAmount / lines.size.toDouble()
                                            val shareStr = String.format(Locale.US, "%.2f", splitShare)
                                            for (i in lines.indices) {
                                                lines[i] = lines[i].copy(amountText = shareStr)
                                            }
                                        },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text(
                                            text = if (isBangla) "সমান ভাগ" else "Split Evenly",
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                // Match Total to Sum of Splits button
                                if (!isBalanced && sumOfSplits > 0.0) {
                                    FilledTonalButton(
                                        onClick = { overallAmount = sumOfSplits },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = if (isBangla) "মোট সমন্বয়" else "Match Total",
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (isBangla) "বরাদ্দকৃত" else "Allocated",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "$currencySymbol ${LanguageHelper.formatNumber(sumOfSplits, languageMode)}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SolidPrimary
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = if (isBalanced) {
                                        if (isBangla) "স্ট্যাটাস" else "Status"
                                    } else if (remaining > 0) {
                                        if (isBangla) "বাকি রয়েছে" else "Remaining"
                                    } else {
                                        if (isBangla) "অতিরিক্ত" else "Over-allocated"
                                    },
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                if (isBalanced) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFF10B981).copy(alpha = 0.15f)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = Color(0xFF10B981),
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = if (isBangla) "সম্পূর্ণ বরাদ্দ (১০০%)" else "Balanced (100%)",
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF10B981)
                                            )
                                        }
                                    }
                                } else {
                                    val remColor = if (remaining > 0) Color(0xFFF59E0B) else SolidExpense
                                    Text(
                                        text = "$currencySymbol ${LanguageHelper.formatNumber(Math.abs(remaining), languageMode)}",
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = remColor
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable Split Items List
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(lines) { index, line ->
                        // Resolve selected category details
                        val selectedCategory = categories.firstOrNull { it.id == line.categoryId }
                        val selectedSub = categories.firstOrNull { it.id == line.subCategoryId }

                        // Resolve selected account details
                        val lineAccId = when (line.type) {
                            TransactionType.EXPENSE -> line.creditAccountId ?: defaultAccId
                            TransactionType.INCOME -> line.debitAccountId ?: defaultAccId
                            TransactionType.TRANSFER -> line.creditAccountId ?: defaultAccId
                        }
                        val selectedAccount = accounts.firstOrNull { it.id == lineAccId }

                        val lineAmt = line.amountText.toDoubleOrNull() ?: 0.0
                        val pct = if (overallAmount > 0) (lineAmt / overallAmount * 100).toInt() else 0

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                            tonalElevation = 2.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                // Line Header: Number, Type Chips, Percentage, Delete
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = CircleShape,
                                            color = SolidPrimary.copy(alpha = 0.12f),
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = "${index + 1}",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = SolidPrimary
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isBangla) "স্প্লিট #${index + 1}" else "Split #${index + 1}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        if (pct > 0) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = MaterialTheme.colorScheme.surfaceVariant
                                            ) {
                                                Text(
                                                    text = "$pct%",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }

                                    // Line Type Switcher Chips (Expense / Receivable / Income)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        val isExpenseLine = line.type == TransactionType.EXPENSE
                                        val isTransferLine = line.type == TransactionType.TRANSFER

                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isExpenseLine) SolidExpense.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                            border = if (isExpenseLine) BorderStroke(1.dp, SolidExpense.copy(alpha = 0.5f)) else null,
                                            modifier = Modifier.clickable {
                                                lines[index] = line.copy(type = TransactionType.EXPENSE)
                                            }
                                        ) {
                                            Text(
                                                text = if (isBangla) "খরচ" else "Expense",
                                                fontSize = 10.sp,
                                                fontWeight = if (isExpenseLine) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isExpenseLine) SolidExpense else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isTransferLine) Color(0xFF2563EB).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                            border = if (isTransferLine) BorderStroke(1.dp, Color(0xFF2563EB).copy(alpha = 0.5f)) else null,
                                            modifier = Modifier.clickable {
                                                lines[index] = line.copy(type = TransactionType.TRANSFER)
                                            }
                                        ) {
                                            Text(
                                                text = if (isBangla) "পাওনা/ট্রান্সফার" else "Receivable",
                                                fontSize = 10.sp,
                                                fontWeight = if (isTransferLine) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isTransferLine) Color(0xFF2563EB) else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }

                                        if (lines.size > 1) {
                                            IconButton(
                                                onClick = { lines.removeAt(index) },
                                                modifier = Modifier.size(26.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.DeleteOutline,
                                                    contentDescription = "Remove line",
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(17.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Category Selector Box
                                val catTitle = when {
                                    selectedSub != null -> "${selectedCategory?.localizedName(languageMode) ?: ""} > ${selectedSub.localizedName(languageMode)}"
                                    selectedCategory != null -> selectedCategory.localizedName(languageMode)
                                    line.type == TransactionType.TRANSFER -> if (isBangla) "পাওনাদার / ক্যাটাগরি" else "Receivable / Category"
                                    else -> if (isBangla) "ক্যাটাগরি নির্বাচন করুন" else "Select Category"
                                }

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { pickingCategoryLineIndex = index }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 7.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        if (selectedCategory != null) IconHelper.parseColorHex(selectedCategory.colorHex).copy(alpha = 0.15f)
                                                        else SolidPrimary.copy(alpha = 0.1f)
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                IconHelper.AppIcon(
                                                    iconName = selectedCategory?.iconName ?: if (line.type == TransactionType.TRANSFER) "SwapHoriz" else "Category",
                                                    modifier = Modifier.size(16.dp),
                                                    tint = if (selectedCategory != null) IconHelper.parseColorHex(selectedCategory.colorHex) else SolidPrimary
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = if (isBangla) "ক্যাটাগরি / আইটেম" else "Category / Line Purpose",
                                                    fontSize = 9.5.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text(
                                                    text = catTitle,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    color = if (selectedCategory != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                                                )
                                            }
                                        }

                                        Icon(
                                            Icons.Default.KeyboardArrowDown,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.outline,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Account Selector Box (Supporting Multi-Account splits)
                                val accTitle = selectedAccount?.localizedName(languageMode)
                                    ?: (if (isBangla) "অ্যাকাউন্ট নির্বাচন করুন" else "Select Payment Account")

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { pickingAccountLineIndex = index }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 7.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        if (selectedAccount != null) IconHelper.parseColorHex(selectedAccount.colorHex).copy(alpha = 0.15f)
                                                        else MaterialTheme.colorScheme.surfaceVariant
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                IconHelper.AppIcon(
                                                    iconName = selectedAccount?.iconName ?: "AccountBalance",
                                                    modifier = Modifier.size(16.dp),
                                                    tint = if (selectedAccount != null) IconHelper.parseColorHex(selectedAccount.colorHex) else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = if (isBangla) "পরিশোধের অ্যাকাউন্ট" else "Paid from Account",
                                                    fontSize = 9.5.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text(
                                                    text = accTitle,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    color = if (selectedAccount != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                                                )
                                            }
                                        }

                                        Icon(
                                            Icons.Default.KeyboardArrowDown,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.outline,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Amount Input Field + Fill Remaining Button
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = line.amountText,
                                        onValueChange = { newText ->
                                            val filtered = newText.filter { it.isDigit() || it == '.' }
                                            lines[index] = line.copy(amountText = filtered)
                                        },
                                        placeholder = { Text("0.00", fontSize = 13.sp) },
                                        leadingIcon = {
                                            Text(
                                                currencySymbol,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = SolidPrimary
                                            )
                                        },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                    )

                                    // Fill Remaining Shortcut
                                    if (remaining > 0.001) {
                                        OutlinedButton(
                                            onClick = {
                                                val currentLineAmt = line.amountText.toDoubleOrNull() ?: 0.0
                                                val targetAmt = currentLineAmt + remaining
                                                lines[index] = line.copy(
                                                    amountText = String.format(Locale.US, "%.2f", targetAmt)
                                                )
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            modifier = Modifier.height(48.dp)
                                        ) {
                                            Text(
                                                text = if (isBangla) "+বাকি" else "+Fill",
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Line Note / Memo
                                OutlinedTextField(
                                    value = line.note,
                                    onValueChange = { newNote ->
                                        lines[index] = line.copy(note = newNote)
                                    },
                                    placeholder = {
                                        Text(
                                            if (isBangla) "নোট / বিবরণ (যেমন: বন্ধুর অংশ / ওষুধ / কনসালটেশন)" else "Note / Description (e.g. Friend share / Medicine)",
                                            fontSize = 11.sp
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.EditNote,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.outline,
                                            modifier = Modifier.size(17.dp)
                                        )
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(46.dp)
                                )
                            }
                        }
                    }

                    // Add Split Item Button
                    item {
                        OutlinedButton(
                            onClick = {
                                val nextCat = categories.filter { it.parentId == null }.getOrNull(lines.size)
                                    ?: categories.firstOrNull { it.parentId == null }
                                val nextAcc = accounts.getOrNull(lines.size)?.id ?: defaultAccId
                                val autoFillAmount = if (remaining > 0.0) String.format(Locale.US, "%.2f", remaining) else ""
                                lines.add(
                                    EditableSplitLine(
                                        type = txType,
                                        categoryId = nextCat?.id,
                                        creditAccountId = nextAcc,
                                        amountText = autoFillAmount
                                    )
                                )
                            },
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.5.dp, SolidPrimary.copy(alpha = 0.6f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = SolidPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isBangla) "+ নতুন স্প্লিট যোগ করুন" else "+ Add Another Split",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = SolidPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onRevertToSingle != null && initialItems.size >= 2) {
                        OutlinedButton(
                            onClick = {
                                onRevertToSingle()
                                onDismiss()
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                        ) {
                            Text(
                                text = if (isBangla) "বিভাজন বাতিল" else "Remove Split",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    } else {
                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                        ) {
                            Text(
                                text = if (isBangla) "বাতিল" else "Cancel",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    val canApply = lines.size >= 2 && lines.all { (it.amountText.toDoubleOrNull() ?: 0.0) > 0.0 }
                    Button(
                        onClick = {
                            val resultItems = lines.map {
                                TransactionSplitItem(
                                    id = it.id,
                                    type = it.type,
                                    categoryId = it.categoryId,
                                    subCategoryId = it.subCategoryId,
                                    debitAccountId = it.debitAccountId,
                                    creditAccountId = it.creditAccountId ?: defaultAccId,
                                    amount = it.amountText.toDoubleOrNull() ?: 0.0,
                                    note = it.note.trim(),
                                    payeeOrPayer = it.payeeOrPayer.trim()
                                )
                            }
                            val finalTotal = if (overallAmount > 0) overallAmount else sumOfSplits
                            onApplySplit(resultItems, finalTotal)
                            onDismiss()
                        },
                        enabled = canApply,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SolidPrimary),
                        modifier = Modifier
                            .weight(1.3f)
                            .height(46.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isBangla) "স্প্লিট সংরক্ষণ করুন" else "Apply Split",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // Modal category picker for individual split line
    val catLineIdx = pickingCategoryLineIndex
    if (catLineIdx != null && catLineIdx in lines.indices) {
        val currentLine = lines[catLineIdx]
        CategoryPickerModalDialog(
            categories = categories,
            txType = currentLine.type,
            selectedCategoryId = currentLine.categoryId,
            selectedSubCategoryId = currentLine.subCategoryId,
            languageMode = languageMode,
            onCategorySelected = { catId, subCatId ->
                lines[catLineIdx] = currentLine.copy(categoryId = catId, subCategoryId = subCatId)
                pickingCategoryLineIndex = null
            },
            onAddNewCategory = { newCat ->
                onAddNewCategory?.invoke(newCat)
                lines[catLineIdx] = currentLine.copy(
                    categoryId = newCat.parentId ?: newCat.id,
                    subCategoryId = if (newCat.parentId != null) newCat.id else null
                )
                pickingCategoryLineIndex = null
            },
            onDismiss = { pickingCategoryLineIndex = null }
        )
    }

    // Modal account picker for individual split line
    val accLineIdx = pickingAccountLineIndex
    if (accLineIdx != null && accLineIdx in lines.indices) {
        val currentLine = lines[accLineIdx]
        AccountPickerModalDialog(
            accounts = accounts,
            allAccounts = accounts,
            txType = currentLine.type,
            creditAccountId = currentLine.creditAccountId ?: defaultAccId,
            debitAccountId = currentLine.debitAccountId,
            initialTarget = if (currentLine.type == TransactionType.INCOME) 1 else 0,
            languageMode = languageMode,
            onAccountSelected = { srcAccId, destAccId ->
                if (currentLine.type == TransactionType.INCOME) {
                    lines[accLineIdx] = currentLine.copy(debitAccountId = destAccId ?: srcAccId)
                } else if (currentLine.type == TransactionType.TRANSFER) {
                    lines[accLineIdx] = currentLine.copy(creditAccountId = srcAccId, debitAccountId = destAccId)
                } else {
                    lines[accLineIdx] = currentLine.copy(creditAccountId = srcAccId ?: destAccId)
                }
                pickingAccountLineIndex = null
            },
            onAddNewAccount = { newAcc ->
                onAddNewAccount?.invoke(newAcc)
                if (currentLine.type == TransactionType.INCOME) {
                    lines[accLineIdx] = currentLine.copy(debitAccountId = newAcc.id)
                } else {
                    lines[accLineIdx] = currentLine.copy(creditAccountId = newAcc.id)
                }
                pickingAccountLineIndex = null
            },
            onDismiss = { pickingAccountLineIndex = null }
        )
    }
}
