package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.ViewColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Account
import com.example.data.model.Category
import com.example.data.model.LanguageMode
import com.example.data.model.TransactionType
import com.example.data.model.TransactionWithDetails
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidPrimary
import com.example.util.CsvColumn
import com.example.util.CsvExportConfig
import com.example.util.CsvExportDateRange
import com.example.util.CsvManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CsvExportDialog(
    allTransactions: List<TransactionWithDetails>,
    allAccounts: List<Account>,
    allCategories: List<Category>,
    languageMode: LanguageMode,
    onSaveToFile: (CsvExportConfig) -> Unit,
    onShareCsv: (CsvExportConfig) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedDateRange by remember { mutableStateOf(CsvExportDateRange.ALL_TIME) }
    var customStartDate by remember { mutableStateOf(System.currentTimeMillis() - 30L * 86400000L) }
    var customEndDate by remember { mutableStateOf(System.currentTimeMillis()) }

    var selectedTypes by remember {
        mutableStateOf(
            setOf(
                TransactionType.EXPENSE,
                TransactionType.INCOME,
                TransactionType.TRANSFER
            )
        )
    }

    var selectedColumns by remember {
        mutableStateOf(CsvColumn.entries.toSet())
    }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val exportConfig by remember {
        derivedStateOf {
            CsvExportConfig(
                dateRangeType = selectedDateRange,
                customStartDate = customStartDate,
                customEndDate = customEndDate,
                selectedTypes = selectedTypes,
                includedColumns = selectedColumns
            )
        }
    }

    val filteredTransactions by remember {
        derivedStateOf {
            CsvManager.filterTransactions(allTransactions, exportConfig)
        }
    }

    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = 700.dp)
                .padding(vertical = 16.dp)
                .testTag("csv_export_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = SolidPrimary.copy(alpha = 0.15f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.FileDownload,
                                    contentDescription = null,
                                    tint = SolidPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "CSV এক্সপোর্ট কাস্টমাইজ" else "Export Transactions (CSV)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA)
                                    "${filteredTransactions.size} টি লেনদেন এক্সপোর্ট হবে"
                                else
                                    "${filteredTransactions.size} transactions will be exported",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable Customization Content
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. DATE RANGE
                    item {
                        SectionCard(
                            title = if (languageMode == LanguageMode.BANGLA) "১. সময়সীমা নির্বাচন (Date Range)" else "1. Date Range",
                            icon = Icons.Default.DateRange
                        ) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                CsvExportDateRange.entries.forEach { range ->
                                    val isSelected = selectedDateRange == range
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedDateRange = range },
                                        label = {
                                            Text(
                                                text = if (languageMode == LanguageMode.BANGLA) range.labelBn else range.labelEn,
                                                fontSize = 11.sp
                                            )
                                        },
                                        leadingIcon = if (isSelected) {
                                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp)) }
                                        } else null,
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = SolidPrimary.copy(alpha = 0.15f),
                                            selectedLabelColor = SolidPrimary
                                        )
                                    )
                                }
                            }

                            if (selectedDateRange == CsvExportDateRange.CUSTOM_RANGE) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { showStartDatePicker = true },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("From: ${dateFormat.format(Date(customStartDate))}", fontSize = 11.sp)
                                    }
                                    OutlinedButton(
                                        onClick = { showEndDatePicker = true },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("To: ${dateFormat.format(Date(customEndDate))}", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }

                    // 2. TRANSACTION TYPES
                    item {
                        SectionCard(
                            title = if (languageMode == LanguageMode.BANGLA) "২. লেনদেনের ধরন (Transaction Types)" else "2. Transaction Types",
                            icon = Icons.Default.FilterList
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val types = listOf(
                                    Triple(TransactionType.EXPENSE, "Expense", SolidExpense),
                                    Triple(TransactionType.INCOME, "Income", SolidIncome),
                                    Triple(TransactionType.TRANSFER, "Transfer", SolidPrimary)
                                )

                                types.forEach { (type, label, color) ->
                                    val isSelected = selectedTypes.contains(type)
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            selectedTypes = if (isSelected) {
                                                if (selectedTypes.size > 1) selectedTypes - type else selectedTypes
                                            } else {
                                                selectedTypes + type
                                            }
                                        },
                                        label = { Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                                        leadingIcon = if (isSelected) {
                                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp), tint = color) }
                                        } else null,
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = color.copy(alpha = 0.15f),
                                            selectedLabelColor = color
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    // 3. COLUMNS PICKER
                    item {
                        SectionCard(
                            title = if (languageMode == LanguageMode.BANGLA) "৩. কোন কলামগুলো এক্সপোর্ট করবেন (Columns)" else "3. Select Columns to Export",
                            icon = Icons.Default.ViewColumn,
                            trailingAction = {
                                Row {
                                    TextButton(
                                        onClick = { selectedColumns = CsvColumn.entries.toSet() },
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                                    ) {
                                        Text("All", fontSize = 11.sp)
                                    }
                                    TextButton(
                                        onClick = {
                                            selectedColumns = setOf(
                                                CsvColumn.TYPE,
                                                CsvColumn.DATE,
                                                CsvColumn.NAME,
                                                CsvColumn.AMOUNT,
                                                CsvColumn.CATEGORY,
                                                CsvColumn.ACCOUNT
                                            )
                                        },
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                                    ) {
                                        Text("Compact", fontSize = 11.sp)
                                    }
                                }
                            }
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                CsvColumn.entries.chunked(2).forEach { pair ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        pair.forEach { col ->
                                            val isChecked = selectedColumns.contains(col)
                                            Row(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .clickable {
                                                        selectedColumns = if (isChecked) {
                                                            if (selectedColumns.size > 1) selectedColumns - col else selectedColumns
                                                        } else {
                                                            selectedColumns + col
                                                        }
                                                    }
                                                    .padding(vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Checkbox(
                                                    checked = isChecked,
                                                    onCheckedChange = { checked ->
                                                        selectedColumns = if (checked) {
                                                            selectedColumns + col
                                                        } else {
                                                            if (selectedColumns.size > 1) selectedColumns - col else selectedColumns
                                                        }
                                                    },
                                                    modifier = Modifier.size(28.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = col.header,
                                                    fontSize = 11.sp,
                                                    fontWeight = if (isChecked) FontWeight.SemiBold else FontWeight.Normal,
                                                    color = if (isChecked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                                                )
                                            }
                                        }
                                        if (pair.size == 1) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Export Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { onShareCsv(exportConfig) },
                        enabled = filteredTransactions.isNotEmpty(),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (languageMode == LanguageMode.BANGLA) "শেয়ার করুন" else "Share CSV", fontSize = 12.sp)
                    }

                    Button(
                        onClick = { onSaveToFile(exportConfig) },
                        enabled = filteredTransactions.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = SolidPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.3f)
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "সংরক্ষণ করুন (${filteredTransactions.size})" else "Save CSV (${filteredTransactions.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }

    // Date Pickers
    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = customStartDate)
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { customStartDate = it }
                        showStartDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = customEndDate)
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { customEndDate = it }
                        showEndDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    trailingAction: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                trailingAction?.invoke()
            }
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}
