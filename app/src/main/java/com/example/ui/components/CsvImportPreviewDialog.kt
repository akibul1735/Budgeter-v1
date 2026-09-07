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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CsvImportPreviewDialog(
    preview: CsvImportPreview,
    languageMode: LanguageMode,
    isImporting: Boolean,
    onConfirmImport: (skipDuplicates: Boolean, autoCreateEntities: Boolean, customHeaderMap: Map<String, Int>?) -> Unit,
    onRemapRequested: ((customHeaderMap: Map<String, Int>) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    var skipDuplicates by remember { mutableStateOf(true) }
    var autoCreateEntities by remember { mutableStateOf(true) }
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    // Header mapping state (appFieldKey -> columnIndex)
    val currentHeaderMap = remember(preview.detectedHeaderMap) {
        mutableStateMapOf<String, Int>().apply {
            putAll(preview.detectedHeaderMap)
        }
    }

    val effectiveImportCount = if (skipDuplicates) {
        (preview.validRows - preview.duplicateRows).coerceAtLeast(0)
    } else {
        preview.validRows
    }

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
                            onGoToMapping = { selectedTabIndex = 1 }
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
                            onConfirmImport(skipDuplicates, autoCreateEntities, currentHeaderMap.toMap())
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

        // Unsupported Rows Alert Card (if any)
        if (preview.unsupportedRowsCount > 0) {
            item {
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = SolidExpense.copy(alpha = 0.1f)),
                    modifier = Modifier.fillMaxWidth().clickable { onGoToUnsupported() }
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = SolidExpense, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA)
                                        "${preview.unsupportedRowsCount} টি সারি ফরম্যাটের কারণে ইম্পোর্ট হবে না"
                                    else
                                        "${preview.unsupportedRowsCount} rows cannot be imported",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SolidExpense
                                )
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA)
                                        "কারণ ও সমাধানের পরামর্শ দেখতে ক্লিক করুন"
                                    else
                                        "Tap to view specific reasons & suggestions",
                                    fontSize = 10.sp,
                                    color = SolidExpense.copy(alpha = 0.8f)
                                )
                            }
                        }
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = SolidExpense, modifier = Modifier.size(14.dp))
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

        // Import Settings Checkboxes
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
// TAB 3: UNSUPPORTED / FAILED ROWS INSPECTOR & SUGGESTIONS
// -------------------------------------------------------------
@Composable
private fun UnsupportedRowsTabContent(
    preview: CsvImportPreview,
    languageMode: LanguageMode,
    onGoToMapping: () -> Unit
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
                    colors = CardDefaults.cardColors(containerColor = SolidExpense.copy(alpha = 0.08f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = SolidExpense, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA)
                                    "${preview.unsupportedRowsCount} টি অকার্যকর সারি চিহ্নিত হয়েছে"
                                else
                                    "${preview.unsupportedRowsCount} unsupported rows detected",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SolidExpense
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA)
                                "নিচের সারিগুলো তারিখ/পরিমাণ অনুপস্থিতি অথবা কলাম ম্যাপিং অসঙ্গতির কারণে বাদ পড়েছে। কলাম ম্যাপিং ঠিক করতে ট্যাবে ক্লিক করুন।"
                            else
                                "These rows failed validation (e.g. missing amount or unrecognized date). You can re-map columns to resolve them.",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedButton(
                            onClick = onGoToMapping,
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier.height(26.dp)
                        ) {
                            Text(if (languageMode == LanguageMode.BANGLA) "কলাম ম্যাপিং পরিবর্তন করুন" else "Fix Column Mapping", fontSize = 10.sp)
                        }
                    }
                }
            }

            items(preview.unsupportedRows) { item ->
                UnsupportedRowCard(item = item, languageMode = languageMode)
            }
        }
    }
}

@Composable
private fun UnsupportedRowCard(
    item: UnsupportedRow,
    languageMode: LanguageMode
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, SolidExpense.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = SolidExpense.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "সারি #${item.lineNumber}" else "Row #${item.lineNumber}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = SolidExpense,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Text(
                    text = item.reason,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SolidExpense,
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
    val typeColor = when (row.type) {
        TransactionType.EXPENSE -> SolidExpense
        TransactionType.INCOME -> SolidIncome
        TransactionType.TRANSFER -> SolidPrimary
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
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${row.dateFormatted} ${row.timeFormatted}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Text(
                    text = "${if (row.type == TransactionType.EXPENSE) "-" else "+"} ৳${String.format(Locale.US, "%.2f", row.amount)}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = typeColor
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
