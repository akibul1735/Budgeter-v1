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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
    var categoryId: Long? = null,
    var subCategoryId: Long? = null,
    var amountText: String = "",
    var note: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitTransactionDialog(
    totalAmount: Double,
    initialItems: List<TransactionSplitItem>,
    categories: List<Category>,
    txType: TransactionType,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onApplySplit: (List<TransactionSplitItem>, Double) -> Unit,
    onRevertToSingle: (() -> Unit)? = null
) {
    var overallAmount by remember { mutableStateOf(totalAmount) }
    val isBangla = languageMode == LanguageMode.BANGLA
    val currencySymbol = LanguageHelper.activeCurrencyConfig.activeSymbol

    // Initialize lines
    val lines = remember {
        mutableStateListOf<EditableSplitLine>().apply {
            if (initialItems.isNotEmpty()) {
                addAll(
                    initialItems.map {
                        EditableSplitLine(
                            id = it.id,
                            categoryId = it.categoryId,
                            subCategoryId = it.subCategoryId,
                            amountText = if (it.amount > 0) String.format(Locale.US, "%.2f", it.amount) else "",
                            note = it.note
                        )
                    }
                )
            } else {
                // Default two empty split rows
                val defaultCat1 = categories.firstOrNull { it.parentId == null }?.id
                val defaultCat2 = categories.filter { it.parentId == null }.getOrNull(1)?.id ?: defaultCat1
                val half = if (totalAmount > 0) totalAmount / 2.0 else 0.0
                val halfStr = if (half > 0) String.format(Locale.US, "%.2f", half) else ""
                val rest = if (totalAmount > 0) totalAmount - half else 0.0
                val restStr = if (rest > 0) String.format(Locale.US, "%.2f", rest) else ""

                add(EditableSplitLine(categoryId = defaultCat1, amountText = halfStr))
                add(EditableSplitLine(categoryId = defaultCat2, amountText = restStr))
            }
        }
    }

    // Modal category picker state
    var pickingLineIndex by remember { mutableStateOf<Int?>(null) }

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
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .padding(vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                // Top App Bar / Header
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
                                text = if (isBangla) "লেনদেন বিভাজন" else "Split Transaction",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isBangla) "${lines.size}টি ক্যাটাগরিতে বিভক্ত" else "Split across ${lines.size} categories",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(34.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Summary Card (Total, Allocated, Remaining)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (isBangla) "মোট লেনদেন পরিমাণ" else "Total Transaction Amount",
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "$currencySymbol ${LanguageHelper.formatNumber(overallAmount, languageMode)}",
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // Match Total to Sum of Splits button
                            if (!isBalanced && sumOfSplits > 0.0) {
                                FilledTonalButton(
                                    onClick = { overallAmount = sumOfSplits },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isBangla) "মোট সমন্বয়" else "Match Total",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        Spacer(modifier = Modifier.height(10.dp))

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
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFF10B981).copy(alpha = 0.15f)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = Color(0xFF10B981),
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (isBangla) "সম্পূর্ণ বরাদ্দ (১০০%)" else "Balanced (100%)",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF10B981)
                                            )
                                        }
                                    }
                                } else {
                                    val remColor = if (remaining > 0) Color(0xFFF59E0B) else SolidExpense
                                    Text(
                                        text = "$currencySymbol ${LanguageHelper.formatNumber(Math.abs(remaining), languageMode)}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = remColor
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

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
                                // Line Header: Number, Percentage, Delete
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
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (isBangla) "স্প্লিট #${index + 1}" else "Split Item #${index + 1}",
                                            fontSize = 12.5.sp,
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
                                                    fontSize = 10.5.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }

                                    if (lines.size > 1) {
                                        IconButton(
                                            onClick = { lines.removeAt(index) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.DeleteOutline,
                                                contentDescription = "Remove line",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Category Selector Box
                                val catTitle = when {
                                    selectedSub != null -> "${selectedCategory?.localizedName(languageMode) ?: ""} > ${selectedSub.localizedName(languageMode)}"
                                    selectedCategory != null -> selectedCategory.localizedName(languageMode)
                                    else -> if (isBangla) "ক্যাটাগরি নির্বাচন করুন" else "Select Category"
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { pickingLineIndex = index }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(30.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        if (selectedCategory != null) IconHelper.parseColorHex(selectedCategory.colorHex).copy(alpha = 0.15f)
                                                        else MaterialTheme.colorScheme.surfaceVariant
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                IconHelper.AppIcon(
                                                    iconName = selectedCategory?.iconName ?: "Category",
                                                    modifier = Modifier.size(17.dp),
                                                    tint = if (selectedCategory != null) IconHelper.parseColorHex(selectedCategory.colorHex) else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = catTitle,
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.Medium,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                color = if (selectedCategory != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                                            )
                                        }

                                        Icon(
                                            Icons.Default.KeyboardArrowDown,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.outline,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

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
                                            .height(50.dp)
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

                                Spacer(modifier = Modifier.height(8.dp))

                                // Line Note / Memo
                                OutlinedTextField(
                                    value = line.note,
                                    onValueChange = { newNote ->
                                        lines[index] = line.copy(note = newNote)
                                    },
                                    placeholder = {
                                        Text(
                                            if (isBangla) "নোট / বিবরণ (ঐচ্ছিক)" else "Note / Memo (optional)",
                                            fontSize = 12.sp
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.EditNote,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.outline,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
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
                                val autoFillAmount = if (remaining > 0.0) String.format(Locale.US, "%.2f", remaining) else ""
                                lines.add(
                                    EditableSplitLine(
                                        categoryId = nextCat?.id,
                                        amountText = autoFillAmount
                                    )
                                )
                            },
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.5.dp, SolidPrimary.copy(alpha = 0.6f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = SolidPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isBangla) "+ নতুন স্প্লিট যোগ করুন" else "+ Add Another Split",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = SolidPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

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
                                    categoryId = it.categoryId,
                                    subCategoryId = it.subCategoryId,
                                    amount = it.amountText.toDoubleOrNull() ?: 0.0,
                                    note = it.note.trim()
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
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // Modal category picker for individual split line
    val lineIdx = pickingLineIndex
    if (lineIdx != null && lineIdx in lines.indices) {
        CategoryPickerModalDialog(
            categories = categories,
            txType = txType,
            selectedCategoryId = lines[lineIdx].categoryId,
            selectedSubCategoryId = lines[lineIdx].subCategoryId,
            languageMode = languageMode,
            onCategorySelected = { catId, subCatId ->
                lines[lineIdx] = lines[lineIdx].copy(categoryId = catId, subCategoryId = subCatId)
                pickingLineIndex = null
            },
            onAddNewCategory = { newCat ->
                lines[lineIdx] = lines[lineIdx].copy(
                    categoryId = newCat.parentId ?: newCat.id,
                    subCategoryId = if (newCat.parentId != null) newCat.id else null
                )
                pickingLineIndex = null
            },
            onDismiss = { pickingLineIndex = null }
        )
    }
}
