package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.example.util.CsvImportPreview
import com.example.util.ParsedCsvRow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CsvImportPreviewDialog(
    preview: CsvImportPreview,
    languageMode: LanguageMode,
    isImporting: Boolean,
    onConfirmImport: (skipDuplicates: Boolean, autoCreateEntities: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var skipDuplicates by remember { mutableStateOf(true) }
    var autoCreateEntities by remember { mutableStateOf(true) }

    val effectiveImportCount = if (skipDuplicates) {
        preview.validRows - preview.duplicateRows
    } else {
        preview.validRows
    }

    Dialog(
        onDismissRequest = { if (!isImporting) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = 680.dp)
                .padding(vertical = 16.dp)
                .testTag("csv_import_preview_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Dialog Header
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
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "CSV ইম্পোর্ট প্রিভিউ" else "CSV Import Preview",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA)
                                    "${preview.totalRows} টি লেনদেন সনাক্ত করা হয়েছে"
                                else
                                    "${preview.totalRows} rows detected in CSV",
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

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable Content
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 1. Stats Summary Cards
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatMiniCard(
                                label = if (languageMode == LanguageMode.BANGLA) "মোট সারি" else "Total Rows",
                                value = "${preview.totalRows}",
                                color = SolidPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            StatMiniCard(
                                label = if (languageMode == LanguageMode.BANGLA) "বৈধ রেকর্ড" else "Valid Records",
                                value = "${preview.validRows}",
                                color = SolidIncome,
                                modifier = Modifier.weight(1f)
                            )
                            StatMiniCard(
                                label = if (languageMode == LanguageMode.BANGLA) "সম্ভাব্য ডুপ্লিকেট" else "Duplicates",
                                value = "${preview.duplicateRows}",
                                color = if (preview.duplicateRows > 0) SolidExpense else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // 2. Newly Discovered Entities (Categories / Accounts)
                    if (preview.newCategories.isNotEmpty() || preview.newAccounts.isNotEmpty() || preview.newCategoryGroups.isNotEmpty() || preview.newAccountGroups.isNotEmpty()) {
                        item {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Info,
                                            contentDescription = null,
                                            tint = SolidPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (languageMode == LanguageMode.BANGLA) "নতুন উপাদান তৈরি হবে" else "New Entities to Auto-Create",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    if (preview.newCategoryGroups.isNotEmpty() || preview.newCategories.isNotEmpty()) {
                                        Text(
                                            text = "Categories (${preview.newCategories.size + preview.newCategoryGroups.size}):",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            preview.newCategoryGroups.take(6).forEach { group ->
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = SolidIncome.copy(alpha = 0.12f)
                                                ) {
                                                    Text(
                                                        text = "📁 $group",
                                                        fontSize = 10.sp,
                                                        color = SolidIncome,
                                                        fontWeight = FontWeight.Medium,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                            preview.newCategories.take(8).forEach { (group, cat) ->
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                                ) {
                                                    Text(
                                                        text = if (group.isNotEmpty()) "$group › $cat" else cat,
                                                        fontSize = 10.sp,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }

                                    if (preview.newAccountGroups.isNotEmpty() || preview.newAccounts.isNotEmpty()) {
                                        Text(
                                            text = "Accounts (${preview.newAccounts.size + preview.newAccountGroups.size}):",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = SolidPrimary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            preview.newAccountGroups.take(6).forEach { group ->
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = SolidPrimary.copy(alpha = 0.15f)
                                                ) {
                                                    Text(
                                                        text = "🏛️ $group",
                                                        fontSize = 10.sp,
                                                        color = SolidPrimary,
                                                        fontWeight = FontWeight.Medium,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                            preview.newAccounts.take(8).forEach { (group, acc) ->
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = SolidPrimary.copy(alpha = 0.1f)
                                                ) {
                                                    Text(
                                                        text = if (group.isNotEmpty()) "$group › $acc" else acc,
                                                        fontSize = 10.sp,
                                                        color = SolidPrimary,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 3. Import Configuration Options
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = skipDuplicates,
                                        onCheckedChange = { skipDuplicates = it }
                                    )
                                    Column(modifier = Modifier.padding(start = 4.dp)) {
                                        Text(
                                            text = if (languageMode == LanguageMode.BANGLA) "ডুপ্লিকেট লেনদেন এড়িয়ে চলুন" else "Skip duplicate transactions",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = if (languageMode == LanguageMode.BANGLA)
                                                "একই তারিখ ও পরিমাণের লেনদেন পুনরায় ইম্পোর্ট হবে না"
                                            else
                                                "Prevents importing entries matching existing date, amount, and account",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }

                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = autoCreateEntities,
                                        onCheckedChange = { autoCreateEntities = it }
                                    )
                                    Column(modifier = Modifier.padding(start = 4.dp)) {
                                        Text(
                                            text = if (languageMode == LanguageMode.BANGLA) "অনুপস্থিত ক্যাটাগরি ও অ্যাকাউন্ট স্বয়ংক্রিয় তৈরি করুন" else "Auto-create missing categories & accounts",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = if (languageMode == LanguageMode.BANGLA)
                                                "CSV-এর ক্যাটাগরি ও অ্যাকাউন্ট গ্রুপ স্বয়ংক্রিয় সংরক্ষিত হবে"
                                            else
                                                "Preserves CSV groups and hierarchies seamlessly",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 4. Sample Rows Preview List
                    item {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "নমুনা ডেটা প্রিভিউ (প্রথম কয়েকটি):" else "Sample Rows Preview (First few):",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    items(preview.sampleRows) { row ->
                        SampleRowCard(row = row)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Action Buttons
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
                            onConfirmImport(skipDuplicates, autoCreateEntities)
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
                .padding(vertical = 8.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
            Text(label, fontSize = 10.sp, color = color.copy(alpha = 0.85f), maxLines = 1)
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
