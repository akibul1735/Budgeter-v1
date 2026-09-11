package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.LanguageMode
import com.example.data.model.TransactionType
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidPrimary
import com.example.util.ColumnMapping
import com.example.util.CsvImportPreview
import com.example.util.ParsedCsvRow
import com.example.util.UnsupportedRow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CsvImportPreviewDialog(
    preview: CsvImportPreview,
    languageMode: LanguageMode,
    isImporting: Boolean,
    onConfirmImport: (
        skipDuplicates: Boolean,
        autoCreateEntities: Boolean,
        customHeaderMap: Map<String, Int>?,
        repairedRows: List<ParsedCsvRow>,
        autoRepairUnsupported: Boolean
    ) -> Unit,
    onRemapRequested: ((customHeaderMap: Map<String, Int>) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    var skipDuplicates by remember { mutableStateOf(true) }
    var autoCreateEntities by remember { mutableStateOf(true) }
    var autoRepairUnsupported by remember { mutableStateOf(false) }
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    // Map of line number to repaired candidate row
    val repairedRowsMap = remember { mutableStateMapOf<Int, ParsedCsvRow>() }
    var editingUnsupportedRow by remember { mutableStateOf<UnsupportedRow?>(null) }

    // Header mapping state (appFieldKey -> columnIndex)
    val currentHeaderMap = remember(preview.detectedHeaderMap) {
        mutableStateMapOf<String, Int>().apply {
            putAll(preview.detectedHeaderMap)
        }
    }

    val baseCount = if (skipDuplicates) {
        (preview.validRows - preview.duplicateRows).coerceAtLeast(0)
    } else {
        preview.validRows
    }
    val repairedCount = repairedRowsMap.size
    val autoRepairedCount = if (autoRepairUnsupported) (preview.unsupportedRowsCount - repairedCount).coerceAtLeast(0) else 0
    val effectiveImportCount = baseCount + repairedCount + autoRepairedCount

    val tabs = listOf(
        if (languageMode == LanguageMode.BANGLA) "ওভারভিউ" else "Overview",
        if (languageMode == LanguageMode.BANGLA) "কলাম ম্যাপিং (${preview.columnMappings.size})" else "Column Mapping (${preview.columnMappings.size})",
        if (languageMode == LanguageMode.BANGLA) "নমুনা ডেটা (${preview.validRows})" else "Sample Data (${preview.validRows})",
        if (languageMode == LanguageMode.BANGLA) "সমস্যাযুক্ত সারি (${preview.unsupportedRowsCount})" else "Unsupported (${preview.unsupportedRowsCount})"
    )

    Dialog(
        onDismissRequest = { if (!isImporting) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .heightIn(max = 700.dp)
                .padding(vertical = 16.dp)
                .testTag("csv_import_preview_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // 1. Dialog Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = SolidIncome.copy(alpha = 0.15f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.TableChart,
                                    contentDescription = null,
                                    tint = SolidIncome,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "CSV / এক্সেল ইম্পোর্ট প্রিভিউ" else "CSV / Excel Import Preview",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA)
                                    "${preview.totalRows} টি সারি • ${preview.validRows} টি বৈধ • ${preview.unsupportedRowsCount} টি সমস্যাযুক্ত"
                                else
                                    "${preview.totalRows} total rows • ${preview.validRows} valid • ${preview.unsupportedRowsCount} unsupported",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        enabled = !isImporting,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 2. Navigation Tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    edgePadding = 0.dp,
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = Color.Transparent,
                    divider = {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 11.sp,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (index == 3 && preview.unsupportedRowsCount > 0 && selectedTabIndex != 3) SolidExpense
                                    else if (selectedTabIndex == index) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outline
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 3. Tab Content
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    when (selectedTabIndex) {
                        0 -> OverviewTabContent(
                            preview = preview,
                            languageMode = languageMode,
                            skipDuplicates = skipDuplicates,
                            onSkipDuplicatesChanged = { skipDuplicates = it },
                            autoCreateEntities = autoCreateEntities,
                            onAutoCreateEntitiesChanged = { autoCreateEntities = it },
                            autoRepairUnsupported = autoRepairUnsupported,
                            onAutoRepairUnsupportedChanged = { autoRepairUnsupported = it },
                            repairedRowsCount = repairedRowsMap.size,
                            onGoToMapping = { selectedTabIndex = 1 },
                            onGoToUnsupported = { selectedTabIndex = 3 }
                        )
                        1 -> ColumnMappingTabContent(
                            preview = preview,
                            languageMode = languageMode,
                            currentHeaderMap = currentHeaderMap,
                            onUpdateHeaderMap = { fieldKey, colIndex ->
                                if (colIndex >= 0) {
                                    currentHeaderMap[fieldKey] = colIndex
                                } else {
                                    currentHeaderMap.remove(fieldKey)
                                }
                                onRemapRequested?.invoke(currentHeaderMap.toMap())
                            }
                        )
                        2 -> SampleRowsTabContent(
                            preview = preview,
                            languageMode = languageMode
                        )
                        3 -> UnsupportedRowsTabContent(
                            preview = preview,
                            languageMode = languageMode,
                            repairedRowsMap = repairedRowsMap,
                            onGoToMapping = { selectedTabIndex = 1 },
                            onQuickFixClicked = { row -> editingUnsupportedRow = row },
                            onAutoRepairAll = {
                                preview.unsupportedRows.forEach { item ->
                                    val candidate = item.candidateRow ?: ParsedCsvRow(
                                        rawLineNumber = item.lineNumber,
                                        type = TransactionType.EXPENSE,
                                        dateEpochMs = System.currentTimeMillis(),
                                        dateFormatted = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()),
                                        timeFormatted = "12:00:00",
                                        name = "Transaction #${item.lineNumber}",
                                        amount = 100.0,
                                        rawAmount = "100.00",
                                        currency = "BDT",
                                        exchangeRate = 1.0,
                                        categoryGroup = "General",
                                        category = "General Expense",
                                        accountGroup = "Cash",
                                        account = "Cash",
                                        notes = "Auto-repaired from unsupported row",
                                        labels = "",
                                        status = "CLEARED",
                                        isValid = true
                                    )
                                    repairedRowsMap[item.lineNumber] = candidate
                                }
                                autoRepairUnsupported = true
                            },
                            onRemoveRepairedRow = { lineNo ->
                                repairedRowsMap.remove(lineNo)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 4. Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !isImporting,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (languageMode == LanguageMode.BANGLA) "বাতিল" else "Cancel")
                    }

                    Button(
                        onClick = {
                            onConfirmImport(
                                skipDuplicates,
                                autoCreateEntities,
                                currentHeaderMap.toMap(),
                                repairedRowsMap.values.toList(),
                                autoRepairUnsupported
                            )
                        },
                        enabled = !isImporting && effectiveImportCount > 0,
                        colors = ButtonDefaults.buttonColors(containerColor = SolidIncome),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.4f)
                    ) {
                        if (isImporting) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (languageMode == LanguageMode.BANGLA) "ইম্পোর্ট হচ্ছে..." else "Importing...")
                        } else {
                            Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "ইম্পোর্ট করুন ($effectiveImportCount)" else "Import ($effectiveImportCount)",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    // Quick-Fix Row Dialog for editing unsupported rows
    editingUnsupportedRow?.let { item ->
        QuickFixRowDialog(
            unsupportedRow = item,
            existingRepaired = repairedRowsMap[item.lineNumber],
            languageMode = languageMode,
            onSaveFix = { fixedRow ->
                repairedRowsMap[item.lineNumber] = fixedRow
                editingUnsupportedRow = null
            },
            onDismiss = { editingUnsupportedRow = null }
        )
    }
}

// -------------------------------------------------------------
// TAB 0: OVERVIEW & OPTIONS
// -------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OverviewTabContent(
    preview: CsvImportPreview,
    languageMode: LanguageMode,
    skipDuplicates: Boolean,
    onSkipDuplicatesChanged: (Boolean) -> Unit,
    autoCreateEntities: Boolean,
    onAutoCreateEntitiesChanged: (Boolean) -> Unit,
    autoRepairUnsupported: Boolean,
    onAutoRepairUnsupportedChanged: (Boolean) -> Unit,
    repairedRowsCount: Int,
    onGoToMapping: () -> Unit,
    onGoToUnsupported: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Stats Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                StatMiniCard(
                    label = if (languageMode == LanguageMode.BANGLA) "মোট সারি" else "Total Rows",
                    value = "${preview.totalRows}",
                    color = SolidPrimary,
                    modifier = Modifier.weight(1f)
                )
                StatMiniCard(
                    label = if (languageMode == LanguageMode.BANGLA) "বৈধ রেকর্ড" else "Valid",
                    value = "${preview.validRows}",
                    color = SolidIncome,
                    modifier = Modifier.weight(1f)
                )
                StatMiniCard(
                    label = if (languageMode == LanguageMode.BANGLA) "ডুপ্লিকেট" else "Duplicates",
                    value = "${preview.duplicateRows}",
                    color = if (preview.duplicateRows > 0) Color(0xFFF59E0B) else MaterialTheme.colorScheme.outline,
                    modifier = Modifier.weight(1f)
                )
                StatMiniCard(
                    label = if (languageMode == LanguageMode.BANGLA) "ত্রুটিযুক্ত" else "Unsupported",
                    value = "${preview.unsupportedRowsCount}",
                    color = if (preview.unsupportedRowsCount > 0) SolidExpense else MaterialTheme.colorScheme.outline,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Unsupported Rows Alert & Workaround Action Card
        if (preview.unsupportedRowsCount > 0) {
            item {
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = if (repairedRowsCount > 0 || autoRepairUnsupported) SolidIncome.copy(alpha = 0.12f) else SolidExpense.copy(alpha = 0.1f)),
                    modifier = Modifier.fillMaxWidth().clickable { onGoToUnsupported() }
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (repairedRowsCount > 0 || autoRepairUnsupported) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (repairedRowsCount > 0 || autoRepairUnsupported) SolidIncome else SolidExpense,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (repairedRowsCount > 0 || autoRepairUnsupported) {
                                        if (languageMode == LanguageMode.BANGLA) "$repairedRowsCount টি সমস্যাযুক্ত সারি মেরামত করা হয়েছে" else "$repairedRowsCount unsupported rows repaired & supported"
                                    } else {
                                        if (languageMode == LanguageMode.BANGLA) "${preview.unsupportedRowsCount} টি সারিতে ত্রুটি রয়েছে (সমাধান অপশন উপলব্ধ)" else "${preview.unsupportedRowsCount} rows have errors (Workaround available)"
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (repairedRowsCount > 0 || autoRepairUnsupported) SolidIncome else SolidExpense
                                )
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA)
                                        "স্বয়ংক্রিয় সমাধান বা কাস্টম মান দিতে এখানে ট্যাপ করুন"
                                    else
                                        "Tap to auto-repair with smart fallbacks or quick-fix rows",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = if (repairedRowsCount > 0 || autoRepairUnsupported) SolidIncome else SolidExpense,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        // Column Mapping Summary Snippet
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "কলাম ম্যাপিং সারাংশ" else "Column Mapping Summary",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "পরিবর্তন করুন ›" else "Customize ›",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { onGoToMapping() }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        preview.columnMappings.forEach { mapping ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = mapping.csvHeaderName,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = mapping.targetAppColumnKey ?: "Unmapped",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Auto Create Entities
        if (preview.newCategories.isNotEmpty() || preview.newAccounts.isNotEmpty() || preview.newCategoryGroups.isNotEmpty() || preview.newAccountGroups.isNotEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = SolidIncome, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "নতুন উপাদান তৈরি হবে" else "New Entities to Auto-Create",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        if (preview.newCategoryGroups.isNotEmpty() || preview.newCategories.isNotEmpty()) {
                            Text(
                                text = "Categories (${preview.newCategories.size + preview.newCategoryGroups.size}):",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SolidIncome
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                preview.newCategoryGroups.take(6).forEach { group ->
                                    Surface(shape = RoundedCornerShape(4.dp), color = SolidIncome.copy(alpha = 0.12f)) {
                                        Text("📁 $group", fontSize = 10.sp, color = SolidIncome, modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                                    }
                                }
                                preview.newCategories.take(8).forEach { (group, cat) ->
                                    Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)) {
                                        Text(if (group.isNotEmpty()) "$group › $cat" else cat, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }

                        if (preview.newAccountGroups.isNotEmpty() || preview.newAccounts.isNotEmpty()) {
                            Text(
                                text = "Accounts (${preview.newAccounts.size + preview.newAccountGroups.size}):",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SolidPrimary
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                preview.newAccountGroups.take(6).forEach { group ->
                                    Surface(shape = RoundedCornerShape(4.dp), color = SolidPrimary.copy(alpha = 0.15f)) {
                                        Text("🏛️ $group", fontSize = 10.sp, color = SolidPrimary, modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                                    }
                                }
                                preview.newAccounts.take(8).forEach { (group, acc) ->
                                    Surface(shape = RoundedCornerShape(4.dp), color = SolidPrimary.copy(alpha = 0.1f)) {
                                        Text(if (group.isNotEmpty()) "$group › $acc" else acc, fontSize = 10.sp, color = SolidPrimary, modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Import Settings Checkboxes & Workaround Option
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = skipDuplicates, onCheckedChange = onSkipDuplicatesChanged)
                        Column(modifier = Modifier.padding(start = 4.dp)) {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "ডুপ্লিকেট লেনদেন এড়িয়ে চলুন" else "Skip duplicate transactions",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "একই তারিখ ও পরিমাণের ডাটা পুনরায় যুক্ত হবে না" else "Prevents duplicate entries from creating redundant records",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = autoCreateEntities, onCheckedChange = onAutoCreateEntitiesChanged)
                        Column(modifier = Modifier.padding(start = 4.dp)) {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "অনুপস্থিত ক্যাটাগরি ও অ্যাকাউন্ট স্বয়ংক্রিয় তৈরি করুন" else "Auto-create missing categories & accounts",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "CSV-এর নতুন গ্রুপ ও নাম স্বয়ংক্রিয় সংরক্ষিত হবে" else "Preserves original CSV group and hierarchy names",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    if (preview.unsupportedRowsCount > 0) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(checked = autoRepairUnsupported, onCheckedChange = onAutoRepairUnsupportedChanged)
                            Column(modifier = Modifier.padding(start = 4.dp)) {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "অসমর্থিত সারি স্বয়ংক্রিয় মেরামত ও ইম্পোর্ট করুন" else "Auto-repair & import unsupported rows",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "অনুপস্থিত তারিখ বা পরিমাণের ক্ষেত্রে ডিফল্ট মান ব্যবহার করে সম্পূর্ণ ফাইল সাপোর্ট করবে" else "Applies smart fallback dates/amounts so no record is lost",
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

// -------------------------------------------------------------
// TAB 1: COLUMN MAPPING INSPECTOR & EDITOR
// -------------------------------------------------------------
@Composable
private fun ColumnMappingTabContent(
    preview: CsvImportPreview,
    languageMode: LanguageMode,
    currentHeaderMap: Map<String, Int>,
    onUpdateHeaderMap: (fieldKey: String, colIndex: Int) -> Unit
) {
    val supportedFields = listOf(
        Triple("date", "Date / Transaction Date", Icons.Default.CalendarToday),
        Triple("amount", "Amount / Value", Icons.Default.Payments),
        Triple("type", "Type (Income/Expense/Transfer)", Icons.Default.SwapHoriz),
        Triple("category", "Category", Icons.Default.Category),
        Triple("category_group", "Category Group / Parent", Icons.Default.Category),
        Triple("account", "Account", Icons.Default.AccountBalance),
        Triple("account_groups", "Account Group / Parent", Icons.Default.AccountBalance),
        Triple("name", "Name / Payee / Payer", Icons.Default.Description),
        Triple("notes", "Notes / Description", Icons.Default.Description),
        Triple("set_time", "Time", Icons.Default.CalendarToday),
        Triple("labels", "Labels / Tags", Icons.Default.Description),
        Triple("status", "Status / Cleared", Icons.Default.CheckCircle),
        Triple("currency", "Currency Code", Icons.Default.Payments),
        Triple("exchange_rate", "Exchange Rate", Icons.Default.Payments)
    )

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.HelpOutline, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA)
                            "আপনার CSV কলামের সাথে Budgeter-এর ডাটা ফিল্ডের ম্যাপিং নিশ্চিত করুন। প্রয়োজনে পরিবর্তন করুন।"
                        else
                            "Map your CSV headers to Budgeter app fields (e.g. Category > Category, Accounts > Account).",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        items(supportedFields) { (fieldKey, fieldLabel, icon) ->
            val mappedIndex = currentHeaderMap[fieldKey]
            val mappedColName = if (mappedIndex != null && mappedIndex >= 0 && mappedIndex < preview.rawHeaders.size) {
                "Col #${mappedIndex + 1}: ${preview.rawHeaders[mappedIndex]}"
            } else if (mappedIndex != null) {
                "Col #${mappedIndex + 1}"
            } else {
                if (languageMode == LanguageMode.BANGLA) "(কোনো কলাম নেই / স্কিপ)" else "(None / Skip)"
            }

            var dropdownExpanded by remember { mutableStateOf(false) }

            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (mappedIndex != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = fieldLabel,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "App Field: $fieldKey",
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    // Column Selector Dropdown Trigger
                    Box {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (mappedIndex != null) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (mappedIndex != null) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                                else Color.Transparent
                            ),
                            modifier = Modifier.clickable { dropdownExpanded = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = mappedColName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (mappedIndex != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.widthIn(max = 130.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(16.dp))
                            }
                        }

                        DropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(if (languageMode == LanguageMode.BANGLA) "(কোনোটিই নয় / স্কিপ)" else "(None / Skip)", fontSize = 12.sp) },
                                onClick = {
                                    onUpdateHeaderMap(fieldKey, -1)
                                    dropdownExpanded = false
                                }
                            )
                            preview.rawHeaders.forEachIndexed { colIdx, rawHeaderName ->
                                DropdownMenuItem(
                                    text = { Text("Col #${colIdx + 1}: $rawHeaderName", fontSize = 12.sp, fontWeight = if (mappedIndex == colIdx) FontWeight.Bold else FontWeight.Normal) },
                                    onClick = {
                                        onUpdateHeaderMap(fieldKey, colIdx)
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 2: SAMPLE DATA ROWS PREVIEW
// -------------------------------------------------------------
@Composable
private fun SampleRowsTabContent(
    preview: CsvImportPreview,
    languageMode: LanguageMode
) {
    if (preview.sampleRows.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
            Text(
                text = if (languageMode == LanguageMode.BANGLA) "কোনো বৈধ রেকর্ড পাওয়া যায়নি" else "No valid sample records found",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.outline
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    text = if (languageMode == LanguageMode.BANGLA)
                        "প্রথম ${preview.sampleRows.size} টি বৈধ লেনদেনের নমুনা রূপান্তর:"
                    else
                        "First ${preview.sampleRows.size} valid parsed transaction previews:",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            items(preview.sampleRows) { row ->
                SampleRowCard(row = row)
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 3: UNSUPPORTED / FAILED ROWS INSPECTOR & SUGGESTIONS & WORKAROUNDS
// -------------------------------------------------------------
@Composable
private fun UnsupportedRowsTabContent(
    preview: CsvImportPreview,
    languageMode: LanguageMode,
    repairedRowsMap: Map<Int, ParsedCsvRow>,
    onGoToMapping: () -> Unit,
    onQuickFixClicked: (UnsupportedRow) -> Unit,
    onAutoRepairAll: () -> Unit,
    onRemoveRepairedRow: (Int) -> Unit
) {
    if (preview.unsupportedRows.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(40.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SolidIncome, modifier = Modifier.size(36.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "সবগুলো সারি সফলভাবে সমর্থিত!" else "All rows are valid and supported!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = SolidIncome
                )
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "কোনো অকার্যকর বা ত্রুটিপূর্ণ সারি পাওয়া যায়নি।" else "No errors or invalid rows detected in CSV.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (repairedRowsMap.size == preview.unsupportedRowsCount)
                            SolidIncome.copy(alpha = 0.08f)
                        else
                            SolidExpense.copy(alpha = 0.08f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if (repairedRowsMap.isNotEmpty()) Icons.Default.AutoFixHigh else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (repairedRowsMap.isNotEmpty()) SolidIncome else SolidExpense,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA)
                                        "${preview.unsupportedRowsCount} টি অকার্যকর সারি (${repairedRowsMap.size} টি মেরামতকৃত)"
                                    else
                                        "${preview.unsupportedRowsCount} unsupported rows (${repairedRowsMap.size} repaired)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (repairedRowsMap.isNotEmpty()) SolidIncome else SolidExpense
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA)
                                "অনুপস্থিত বা অমিল ফিল্ডের কারণে এই সারিগুলো সরাসরি পার্স হয়নি। আপনি প্রতিটি সারির জন্য কুইক-ফিক্স ওয়ার্কঅ্যারাউন্ড প্রয়োগ করতে পারেন অথবা স্বয়ংক্রিয়ভাবে মেরামত করতে পারেন।"
                            else
                                "These rows failed strict parsing. You can manually quick-fix any row or auto-repair all of them with smart fallbacks to support importing them.",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onAutoRepairAll,
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SolidPrimary),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp).weight(1f)
                            ) {
                                Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "সবগুলো মেরামত করুন" else "Auto-Repair All",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            OutlinedButton(
                                onClick = onGoToMapping,
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp).weight(1f)
                            ) {
                                Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "কলাম ম্যাপিং" else "Column Mapping",
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }

            items(preview.unsupportedRows) { item ->
                UnsupportedRowCard(
                    item = item,
                    repairedRow = repairedRowsMap[item.lineNumber],
                    languageMode = languageMode,
                    onQuickFix = { onQuickFixClicked(item) },
                    onRevertRepair = { onRemoveRepairedRow(item.lineNumber) }
                )
            }
        }
    }
}

@Composable
private fun UnsupportedRowCard(
    item: UnsupportedRow,
    repairedRow: ParsedCsvRow?,
    languageMode: LanguageMode,
    onQuickFix: () -> Unit,
    onRevertRepair: () -> Unit
) {
    val isRepaired = repairedRow != null

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isRepaired)
                SolidIncome.copy(alpha = 0.08f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isRepaired) SolidIncome.copy(alpha = 0.4f) else SolidExpense.copy(alpha = 0.25f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (isRepaired) SolidIncome.copy(alpha = 0.15f) else SolidExpense.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "সারি #${item.lineNumber}" else "Row #${item.lineNumber}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isRepaired) SolidIncome else SolidExpense,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (isRepaired) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = SolidIncome.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "✓ মেরামতকৃত" else "✓ Repaired",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = SolidIncome,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Text(
                    text = item.reason,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isRepaired) SolidIncome else SolidExpense,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Suggestion Box
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Lightbulb, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = item.suggestion,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 13.sp
                    )
                }
            }

            // If repaired, show the repaired transaction values
            if (repairedRow != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = SolidIncome.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(6.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${repairedRow.name} • ${repairedRow.type.name} • ৳${String.format(Locale.US, "%.2f", repairedRow.amount)}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SolidIncome
                            )
                            Text(
                                text = "${repairedRow.dateFormatted} | ${repairedRow.category} | ${repairedRow.account}",
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }

            if (item.rawTokens.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Raw CSV: [${item.rawTokens.joinToString(", ")}]",
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Workaround Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isRepaired) {
                    OutlinedButton(
                        onClick = onRevertRepair,
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(26.dp)
                    ) {
                        Text(if (languageMode == LanguageMode.BANGLA) "পূর্বাবস্থায় ফেরান" else "Revert", fontSize = 10.sp)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }

                Button(
                    onClick = onQuickFix,
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRepaired) MaterialTheme.colorScheme.secondary else SolidPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier.height(26.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isRepaired) {
                            if (languageMode == LanguageMode.BANGLA) "সম্পাদনা করুন" else "Edit Fix"
                        } else {
                            if (languageMode == LanguageMode.BANGLA) "ওয়ার্কঅ্যারাউন্ড / ফিক্স" else "Quick Fix Workaround"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// QUICK-FIX WORKAROUND DIALOG FOR UNSUPPORTED ROWS
// -------------------------------------------------------------
@Composable
private fun QuickFixRowDialog(
    unsupportedRow: UnsupportedRow,
    existingRepaired: ParsedCsvRow?,
    languageMode: LanguageMode,
    onSaveFix: (ParsedCsvRow) -> Unit,
    onDismiss: () -> Unit
) {
    val initial = existingRepaired ?: unsupportedRow.candidateRow ?: ParsedCsvRow(
        rawLineNumber = unsupportedRow.lineNumber,
        type = TransactionType.EXPENSE,
        dateEpochMs = System.currentTimeMillis(),
        dateFormatted = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()),
        timeFormatted = "12:00:00",
        name = "Transaction #${unsupportedRow.lineNumber}",
        amount = 100.0,
        rawAmount = "100.00",
        currency = "BDT",
        exchangeRate = 1.0,
        categoryGroup = "General",
        category = "General Expense",
        accountGroup = "Cash",
        account = "Cash",
        notes = "Fixed from unsupported row",
        labels = "",
        status = "CLEARED",
        isValid = true
    )

    var selectedType by remember { mutableStateOf(initial.type) }
    var amountText by remember { mutableStateOf(initial.amount.toString()) }
    var nameText by remember { mutableStateOf(initial.name) }
    var dateText by remember { mutableStateOf(initial.dateFormatted) }
    var categoryText by remember { mutableStateOf(initial.category) }
    var accountText by remember { mutableStateOf(initial.account) }
    var notesText by remember { mutableStateOf(initial.notes) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoFixHigh, contentDescription = null, tint = SolidPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "সারি #${unsupportedRow.lineNumber} মেরামত করুন" else "Fix Row #${unsupportedRow.lineNumber}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SolidExpense.copy(alpha = 0.08f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${unsupportedRow.reason} • ${unsupportedRow.suggestion}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Type selector
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            TransactionType.values().forEach { type ->
                                FilterChip(
                                    selected = selectedType == type,
                                    onClick = { selectedType = type },
                                    label = { Text(type.name, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = when (type) {
                                            TransactionType.EXPENSE -> SolidExpense.copy(alpha = 0.2f)
                                            TransactionType.INCOME -> SolidIncome.copy(alpha = 0.2f)
                                            TransactionType.TRANSFER -> SolidPrimary.copy(alpha = 0.2f)
                                        }
                                    )
                                )
                            }
                        }
                    }

                    // Amount & Date
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = amountText,
                                onValueChange = { amountText = it },
                                label = { Text(if (languageMode == LanguageMode.BANGLA) "পরিমাণ" else "Amount", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = dateText,
                                onValueChange = { dateText = it },
                                label = { Text(if (languageMode == LanguageMode.BANGLA) "তারিখ (YYYY-MM-DD)" else "Date (YYYY-MM-DD)", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                    }

                    // Name
                    item {
                        OutlinedTextField(
                            value = nameText,
                            onValueChange = { nameText = it },
                            label = { Text(if (languageMode == LanguageMode.BANGLA) "লেনদেনের নাম / বিবরণ" else "Transaction Name / Payee", fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    // Category & Account
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = categoryText,
                                onValueChange = { categoryText = it },
                                label = { Text(if (languageMode == LanguageMode.BANGLA) "ক্যাটাগরি" else "Category", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = accountText,
                                onValueChange = { accountText = it },
                                label = { Text(if (languageMode == LanguageMode.BANGLA) "অ্যাকাউন্ট" else "Account", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                    }

                    // Notes
                    item {
                        OutlinedTextField(
                            value = notesText,
                            onValueChange = { notesText = it },
                            label = { Text(if (languageMode == LanguageMode.BANGLA) "নোট" else "Notes", fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (languageMode == LanguageMode.BANGLA) "বাতিল" else "Cancel")
                    }

                    Button(
                        onClick = {
                            val parsedAmount = amountText.trim()
                                .replace('০', '0').replace('১', '1').replace('২', '2')
                                .replace('৩', '3').replace('৪', '4').replace('৫', '5')
                                .replace('৬', '6').replace('৭', '7').replace('৮', '8')
                                .replace('৯', '9')
                                .toDoubleOrNull() ?: 100.0

                            val fixedRow = initial.copy(
                                type = selectedType,
                                amount = parsedAmount,
                                rawAmount = parsedAmount.toString(),
                                name = nameText.trim().ifEmpty { "Transaction #${unsupportedRow.lineNumber}" },
                                dateFormatted = dateText.trim().ifEmpty { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) },
                                category = categoryText.trim().ifEmpty { "General Expense" },
                                account = accountText.trim().ifEmpty { "Cash" },
                                notes = notesText.trim(),
                                isValid = true
                            )
                            onSaveFix(fixedRow)
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SolidIncome),
                        modifier = Modifier.weight(1.3f)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "সারি সাপোর্ট করুন" else "Apply Workaround",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatMiniCard(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = color)
            Text(label, fontSize = 9.sp, color = color.copy(alpha = 0.9f), maxLines = 1)
        }
    }
}

@Composable
private fun SampleRowCard(row: ParsedCsvRow) {
    val isReverted = row.amount < 0
    val isPositiveEffect = when (row.type) {
        TransactionType.EXPENSE -> isReverted
        TransactionType.INCOME -> !isReverted
        TransactionType.TRANSFER -> false
    }
    val typeColor = when (row.type) {
        TransactionType.EXPENSE -> SolidExpense
        TransactionType.INCOME -> SolidIncome
        TransactionType.TRANSFER -> SolidPrimary
    }
    val amountColor = when (row.type) {
        TransactionType.TRANSFER -> SolidPrimary
        TransactionType.EXPENSE -> if (isReverted) SolidIncome else SolidExpense
        TransactionType.INCOME -> if (isReverted) SolidExpense else SolidIncome
    }
    val sign = when (row.type) {
        TransactionType.TRANSFER -> ""
        else -> if (isPositiveEffect) "+" else "−"
    }

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (row.isDuplicate) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = typeColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = row.type.name,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = typeColor,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                    if (isReverted) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = amountColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "Reversal",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = amountColor,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${row.dateFormatted} ${row.timeFormatted}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Text(
                    text = "$sign ৳${String.format(Locale.US, "%.2f", Math.abs(row.amount))}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = amountColor
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = row.name.ifEmpty { "Transaction" },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                if (row.isDuplicate) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = SolidExpense.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "Duplicate",
                            fontSize = 9.sp,
                            color = SolidExpense,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Category & Account Tags
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (row.category.isNotBlank() || row.categoryGroup.isNotBlank()) {
                    Text(
                        text = "📁 ${if (row.categoryGroup.isNotBlank()) "${row.categoryGroup} › " else ""}${row.category}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.outline,
                        maxLines = 1
                    )
                }
                if (row.account.isNotBlank() || row.accountGroup.isNotBlank()) {
                    Text(
                        text = "💳 ${if (row.accountGroup.isNotBlank()) "${row.accountGroup} › " else ""}${row.account}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.outline,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
