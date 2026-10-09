package com.example.ui.dialogs

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Account
import com.example.data.model.Category
import com.example.data.model.LanguageMode
import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionType
import com.example.ui.screens.LedgerDatePreset
import com.example.ui.screens.LedgerRowStyle
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.ui.theme.SolidPrimary
import com.example.ui.theme.SolidTransfer
import com.example.util.DateUtils
import com.example.util.IconHelper

/**
 * Modern, tab-specific popup filter dialog for Transactions.
 * Uses Budget Maker filter dialog as the primary visual and interactive reference.
 * Tailored with relevant transaction filters: Type, Date presets, Categories, Accounts,
 * Amount Range, Status, Note conditions, and Display Options.
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TransactionFilterDialog(
    currentFilter: TransactionTabFilter,
    categories: List<Category>,
    accounts: List<Account>,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onApplyFilter: (TransactionTabFilter) -> Unit
) {
    val context = LocalContext.current
    val isBangla = languageMode == LanguageMode.BANGLA
    val accentColor = SolidPrimary

    var tempFilter by remember { mutableStateOf(currentFilter) }

    // Date picker dialog states for custom date range
    var showCustomStartDatePicker by remember { mutableStateOf(false) }
    var showCustomEndDatePicker by remember { mutableStateOf(false) }

    val activeCount = tempFilter.activeCount

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
                .fillMaxHeight(0.88f)
                .testTag("transaction_filter_dialog")
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header (Matching BudgetMakerFilterDialog)
                Surface(
                    color = accentColor.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
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
                                color = accentColor.copy(alpha = 0.20f),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                                        contentDescription = null,
                                        tint = accentColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = if (isBangla) "লেনদেন ফিল্টার" else "Transaction Filter",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (activeCount > 0) {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = accentColor,
                                            modifier = Modifier.padding(start = 2.dp)
                                        ) {
                                            Text(
                                                text = "$activeCount",
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = if (isBangla) "ধরণ, তারিখ, ক্যাটাগরি ও পরিমাণ ফিল্টার" else "Filter type, date, categories & amounts",
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.outline,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            TextButton(
                                onClick = {
                                    tempFilter = TransactionTabFilter(
                                        rowStyle = tempFilter.rowStyle,
                                        displaySettings = tempFilter.displaySettings
                                    )
                                    Toast.makeText(
                                        context,
                                        if (isBangla) "ফিল্টার রিসেট করা হয়েছে" else "Filters reset",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                modifier = Modifier.testTag("filter_dialog_reset_button")
                            ) {
                                Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = if (isBangla) "রিসেট" else "Reset",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier.size(32.dp).testTag("filter_dialog_close_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

                // Scrollable Body
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. Transaction Type Section
                    TransactionTypeCard(
                        selectedTypes = tempFilter.selectedTypes,
                        onTypesChange = { tempFilter = tempFilter.copy(selectedTypes = it) },
                        accentColor = accentColor,
                        isBangla = isBangla
                    )

                    // 2. Date Range Presets & Custom Dates
                    DateRangeFilterCard(
                        selectedPreset = tempFilter.datePreset,
                        customStartDateMs = tempFilter.customStartDateMs,
                        customEndDateMs = tempFilter.customEndDateMs,
                        onPresetChange = { tempFilter = tempFilter.copy(datePreset = it) },
                        onOpenCustomStartPicker = { showCustomStartDatePicker = true },
                        onOpenCustomEndPicker = { showCustomEndDatePicker = true },
                        accentColor = accentColor,
                        isBangla = isBangla
                    )

                    // 3. Amount Range Card with Quick Presets
                    AmountRangeCard(
                        title = if (isBangla) "লেনদেন পরিমাণ রেঞ্জ (৳)" else "Amount Range (৳)",
                        minAmount = tempFilter.minAmount,
                        maxAmount = tempFilter.maxAmount,
                        onMinChange = { tempFilter = tempFilter.copy(minAmount = it) },
                        onMaxChange = { tempFilter = tempFilter.copy(maxAmount = it) },
                        isBangla = isBangla,
                        accentColor = accentColor
                    )

                    // 4. Categories & Groups Section
                    val activeCategories = remember(categories) { categories.filter { it.isActive } }
                    CategoryFilterSection(
                        sectionTitle = if (isBangla) "ক্যাটাগরি ও সাব-ক্যাটাগরি" else "Categories & Sub-Categories",
                        sectionSubtitle = if (isBangla) "নির্দিষ্ট ক্যাটাগরি বা গ্রুপ নির্বাচন করুন" else "Filter by specific categories or groups",
                        categories = activeCategories,
                        selectedItemIds = tempFilter.selectedCategoryIds,
                        selectedGroupNames = tempFilter.selectedCategoryGroupNames,
                        onItemIdsChange = { tempFilter = tempFilter.copy(selectedCategoryIds = it) },
                        onGroupNamesChange = { tempFilter = tempFilter.copy(selectedCategoryGroupNames = it) },
                        accentColor = accentColor,
                        isBangla = isBangla
                    )

                    // 5. Accounts & Groups Section
                    val activeAccounts = remember(accounts) { accounts.filter { it.isActive } }
                    AccountFilterSection(
                        sectionTitle = if (isBangla) "একাউন্ট ও ওয়ালেট" else "Accounts & Wallets",
                        sectionSubtitle = if (isBangla) "নির্দিষ্ট একাউন্ট বা গ্রুপ নির্বাচন করুন" else "Filter by source or destination accounts",
                        accounts = activeAccounts,
                        selectedItemIds = tempFilter.selectedAccountIds,
                        selectedGroupNames = tempFilter.selectedAccountGroupNames,
                        onItemIdsChange = { tempFilter = tempFilter.copy(selectedAccountIds = it) },
                        onGroupNamesChange = { tempFilter = tempFilter.copy(selectedAccountGroupNames = it) },
                        accentColor = accentColor,
                        isBangla = isBangla
                    )

                    // 6. Transaction Status Card
                    TransactionStatusCard(
                        selectedStatuses = tempFilter.selectedStatuses,
                        onStatusesChange = { tempFilter = tempFilter.copy(selectedStatuses = it) },
                        accentColor = accentColor,
                        isBangla = isBangla
                    )

                    // 7. Special Conditions Card
                    TransactionConditionsCard(
                        filter = tempFilter,
                        onFilterChange = { tempFilter = it },
                        accentColor = accentColor,
                        isBangla = isBangla
                    )

                    // 8. Display & Layout Preferences
                    TransactionDisplayOptionsCard(
                        rowStyle = tempFilter.rowStyle,
                        onRowStyleChange = { tempFilter = tempFilter.copy(rowStyle = it) },
                        displaySettings = tempFilter.displaySettings,
                        onDisplaySettingsChange = { tempFilter = tempFilter.copy(displaySettings = it) },
                        accentColor = accentColor,
                        isBangla = isBangla
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

                // Footer Actions (Matching BudgetMakerFilterDialog)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("filter_dialog_cancel_button")
                    ) {
                        Text(if (isBangla) "বাতিল" else "Cancel")
                    }

                    Button(
                        onClick = {
                            onApplyFilter(tempFilter)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("filter_dialog_apply_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text(
                                text = if (isBangla) "ফিল্টার প্রয়োগ করুন" else "Apply Filter",
                                fontWeight = FontWeight.Bold
                            )
                            if (activeCount > 0) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color.White.copy(alpha = 0.25f),
                                    modifier = Modifier.padding(start = 2.dp)
                                ) {
                                    Text(
                                        text = "$activeCount",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Custom Date Pickers
    if (showCustomStartDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = if (tempFilter.customStartDateMs > 0) tempFilter.customStartDateMs else System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showCustomStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        tempFilter = tempFilter.copy(customStartDateMs = it, datePreset = LedgerDatePreset.CUSTOM)
                    }
                    showCustomStartDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showCustomStartDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(
                state = datePickerState,
                title = { Text(if (isBangla) "শুরুর তারিখ নির্বাচন করুন" else "Select Start Date", modifier = Modifier.padding(16.dp)) }
            )
        }
    }

    if (showCustomEndDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = if (tempFilter.customEndDateMs > 0) tempFilter.customEndDateMs else System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showCustomEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        tempFilter = tempFilter.copy(customEndDateMs = it, datePreset = LedgerDatePreset.CUSTOM)
                    }
                    showCustomEndDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showCustomEndDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(
                state = datePickerState,
                title = { Text(if (isBangla) "শেষ তারিখ নির্বাচন করুন" else "Select End Date", modifier = Modifier.padding(16.dp)) }
            )
        }
    }
}

/**
 * Transaction Type Card with Expense, Income, and Transfer toggle chips.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TransactionTypeCard(
    selectedTypes: Set<TransactionType>,
    onTypesChange: (Set<TransactionType>) -> Unit,
    accentColor: Color,
    isBangla: Boolean
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isBangla) "লেনদেনের ধরণ" else "Transaction Types",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isBangla) "একাধিক বা সব ধরণ নির্বাচন করতে পারেন" else "Filter by expense, income or transfer",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                if (selectedTypes.isNotEmpty()) {
                    TextButton(onClick = { onTypesChange(emptySet()) }) {
                        Text(if (isBangla) "সব দেখান" else "Show All", fontSize = 11.5.sp, color = accentColor)
                    }
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val types = listOf(
                    Triple(TransactionType.EXPENSE, if (isBangla) "ব্যয়" else "Expense", SolidExpense),
                    Triple(TransactionType.INCOME, if (isBangla) "আয়" else "Income", SolidIncome),
                    Triple(TransactionType.TRANSFER, if (isBangla) "স্থানান্তর" else "Transfer", SolidTransfer)
                )

                types.forEach { (type, label, color) ->
                    val isSelected = selectedTypes.contains(type)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            val newSet = selectedTypes.toMutableSet()
                            if (isSelected) newSet.remove(type) else newSet.add(type)
                            onTypesChange(newSet)
                        },
                        label = { Text(label, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                        leadingIcon = {
                            Icon(
                                imageVector = when (type) {
                                    TransactionType.EXPENSE -> Icons.Default.TrendingDown
                                    TransactionType.INCOME -> Icons.Default.TrendingUp
                                    TransactionType.TRANSFER -> Icons.Default.SwapHoriz
                                },
                                contentDescription = null,
                                modifier = Modifier.size(15.dp),
                                tint = if (isSelected) color else MaterialTheme.colorScheme.outline
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = color.copy(alpha = 0.16f),
                            selectedLabelColor = color,
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = BorderStroke(
                            0.7.dp,
                            if (isSelected) color else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
        }
    }
}

/**
 * Date Range Presets & Custom Range Card.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DateRangeFilterCard(
    selectedPreset: LedgerDatePreset,
    customStartDateMs: Long,
    customEndDateMs: Long,
    onPresetChange: (LedgerDatePreset) -> Unit,
    onOpenCustomStartPicker: () -> Unit,
    onOpenCustomEndPicker: () -> Unit,
    accentColor: Color,
    isBangla: Boolean
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isBangla) "তারিখ ও সময়সীমা" else "Date & Time Range",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isBangla) "নির্দিষ্ট প্রিসেট বা কাস্টম তারিখ নির্বাচন করুন" else "Preset periods or custom start/end dates",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                if (selectedPreset != LedgerDatePreset.LAST_12_MONTHS) {
                    TextButton(onClick = { onPresetChange(LedgerDatePreset.LAST_12_MONTHS) }) {
                        Text(if (isBangla) "রিসেট" else "Reset", fontSize = 11.5.sp, color = accentColor)
                    }
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val presets = listOf(
                    Pair(LedgerDatePreset.THIS_MONTH, if (isBangla) "এই মাস" else "This Month"),
                    Pair(LedgerDatePreset.LAST_MONTH, if (isBangla) "গত মাস" else "Last Month"),
                    Pair(LedgerDatePreset.LAST_12_MONTHS, if (isBangla) "গত ১২ মাস" else "Last 12 Mos"),
                    Pair(LedgerDatePreset.THIS_YEAR, if (isBangla) "এই বছর" else "This Year"),
                    Pair(LedgerDatePreset.TODAY, if (isBangla) "আজ" else "Today"),
                    Pair(LedgerDatePreset.THIS_WEEK, if (isBangla) "এই সপ্তাহ" else "This Week"),
                    Pair(LedgerDatePreset.THIS_QUARTER, if (isBangla) "এই ত্রৈমাসিক" else "This Quarter"),
                    Pair(LedgerDatePreset.ALL_TIME, if (isBangla) "সব সময়" else "All Time"),
                    Pair(LedgerDatePreset.CUSTOM, if (isBangla) "কাস্টম..." else "Custom...")
                )

                presets.forEach { (preset, label) ->
                    val isSelected = selectedPreset == preset
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) accentColor.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(0.6.dp, if (isSelected) accentColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        modifier = Modifier.clickable { onPresetChange(preset) }
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            // Custom Range inputs if CUSTOM selected
            if (selectedPreset == LedgerDatePreset.CUSTOM) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(0.6.dp, accentColor.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onOpenCustomStartPicker() }
                        ) {
                            Text(if (isBangla) "শুরু" else "From", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.outline)
                            val sText = if (customStartDateMs > 0) DateUtils.formatDate(customStartDateMs, if (isBangla) LanguageMode.BANGLA else LanguageMode.ENGLISH) else "Select..."
                            Text(sText, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = accentColor)
                        }

                        Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onOpenCustomEndPicker() }
                                .padding(start = 12.dp)
                        ) {
                            Text(if (isBangla) "শেষ" else "To", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.outline)
                            val eText = if (customEndDateMs > 0) DateUtils.formatDate(customEndDateMs, if (isBangla) LanguageMode.BANGLA else LanguageMode.ENGLISH) else "Select..."
                            Text(eText, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = accentColor)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Amount Range Card with presets and custom inputs.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AmountRangeCard(
    title: String,
    minAmount: Double?,
    maxAmount: Double?,
    onMinChange: (Double?) -> Unit,
    onMaxChange: (Double?) -> Unit,
    isBangla: Boolean,
    accentColor: Color
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = MaterialTheme.colorScheme.onSurface)
                if (minAmount != null || maxAmount != null) {
                    TextButton(onClick = {
                        onMinChange(null)
                        onMaxChange(null)
                    }) {
                        Text(if (isBangla) "সাফ করুন" else "Clear", fontSize = 11.5.sp, color = accentColor)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = minAmount?.toInt()?.toString() ?: "",
                    onValueChange = { onMinChange(it.toDoubleOrNull()) },
                    label = { Text(if (isBangla) "সর্বনিম্ন ৳" else "Min ৳", fontSize = 11.5.sp) },
                    placeholder = { Text("0", fontSize = 12.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier.weight(1f).height(52.dp)
                )

                OutlinedTextField(
                    value = maxAmount?.toInt()?.toString() ?: "",
                    onValueChange = { onMaxChange(it.toDoubleOrNull()) },
                    label = { Text(if (isBangla) "সর্বোচ্চ ৳" else "Max ৳", fontSize = 11.5.sp) },
                    placeholder = { Text("50000", fontSize = 12.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier.weight(1f).height(52.dp)
                )
            }

            // Quick preset chips
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val presets = listOf(
                    Triple(0.0, 500.0, if (isBangla) "< ৫০০ ৳" else "< 500 ৳"),
                    Triple(500.0, 2000.0, if (isBangla) "৫০০ - ২k ৳" else "500 - 2k ৳"),
                    Triple(2000.0, 10000.0, if (isBangla) "২k - ১০k ৳" else "2k - 10k ৳"),
                    Triple(10000.0, 50000.0, if (isBangla) "১০k - ৫০k ৳" else "10k - 50k ৳"),
                    Triple(50000.0, null, if (isBangla) "> ৫০k ৳" else "> 50k ৳")
                )

                presets.forEach { (min, max, label) ->
                    val isSelected = minAmount == min && maxAmount == max
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) accentColor.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(0.6.dp, if (isSelected) accentColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        modifier = Modifier.clickable {
                            if (isSelected) {
                                onMinChange(null)
                                onMaxChange(null)
                            } else {
                                onMinChange(min)
                                onMaxChange(max)
                            }
                        }
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Category & Group filter section with search and expand.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoryFilterSection(
    sectionTitle: String,
    sectionSubtitle: String,
    categories: List<Category>,
    selectedItemIds: Set<Long>,
    selectedGroupNames: Set<String>,
    onItemIdsChange: (Set<Long>) -> Unit,
    onGroupNamesChange: (Set<String>) -> Unit,
    accentColor: Color,
    isBangla: Boolean
) {
    var searchQuery by remember { mutableStateOf("") }
    val parentMap = remember(categories) {
        categories.filter { it.parentId == null }.associateBy { it.id }
    }
    val childCategories = remember(categories) {
        categories.filter { it.parentId != null }
    }
    val standaloneCategories = remember(categories) {
        val parentIdsWithChildren = childCategories.mapNotNull { it.parentId }.toSet()
        categories.filter { it.parentId == null && !parentIdsWithChildren.contains(it.id) }
    }

    val groupToItemsMap = remember(categories, parentMap, childCategories, standaloneCategories) {
        val map = mutableMapOf<String, MutableList<Category>>()
        childCategories.forEach { child ->
            val parentName = parentMap[child.parentId]?.let {
                if (isBangla && it.nameBn.isNotBlank()) it.nameBn else it.nameEn
            } ?: (if (isBangla) "অন্যান্য" else "Other")
            map.getOrPut(parentName) { mutableListOf() }.add(child)
        }
        standaloneCategories.forEach { standalone ->
            val groupKey = if (isBangla) "প্রধান ক্যাটাগরি" else "Main Categories"
            map.getOrPut(groupKey) { mutableListOf() }.add(standalone)
        }
        map
    }

    val filteredGroups = remember(groupToItemsMap, searchQuery) {
        if (searchQuery.isBlank()) {
            groupToItemsMap
        } else {
            val q = searchQuery.trim().lowercase()
            groupToItemsMap.mapValues { entry ->
                entry.value.filter { cat ->
                    cat.nameEn.lowercase().contains(q) ||
                    cat.nameBn.lowercase().contains(q) ||
                    entry.key.lowercase().contains(q)
                }
            }.filter { it.value.isNotEmpty() }
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = sectionTitle, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = MaterialTheme.colorScheme.onSurface)
                    Text(text = sectionSubtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                }
                if (selectedItemIds.isNotEmpty() || selectedGroupNames.isNotEmpty()) {
                    TextButton(onClick = {
                        onItemIdsChange(emptySet())
                        onGroupNamesChange(emptySet())
                    }) {
                        Text(if (isBangla) "সাফ করুন" else "Clear", fontSize = 11.5.sp, color = accentColor)
                    }
                }
            }

            // Search input
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface),
                        cursorBrush = SolidColor(accentColor),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = if (isBangla) "ক্যাটাগরি বা গ্রুপ খুঁজুন..." else "Search category or group...",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                            innerTextField()
                        },
                        modifier = Modifier.weight(1f)
                    )
                    if (searchQuery.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(18.dp).clickable { searchQuery = "" }
                        )
                    }
                }
            }

            // Expandable groups
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                filteredGroups.forEach { (groupName, items) ->
                    val isGroupSelected = selectedGroupNames.contains(groupName)
                    val allItemsSelected = items.isNotEmpty() && items.all { selectedItemIds.contains(it.id) }
                    val someItemsSelected = items.any { selectedItemIds.contains(it.id) }
                    var isExpanded by remember { mutableStateOf(false) }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            0.7.dp,
                            if (isGroupSelected || someItemsSelected) accentColor.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f).clickable { isExpanded = !isExpanded }
                                ) {
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = groupName,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.5.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                    ) {
                                        Text(
                                            text = "${items.size}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.outline,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }

                                Checkbox(
                                    checked = isGroupSelected || allItemsSelected,
                                    onCheckedChange = { checked ->
                                        val newGroupSet = selectedGroupNames.toMutableSet()
                                        val newItemSet = selectedItemIds.toMutableSet()
                                        if (checked) {
                                            newGroupSet.add(groupName)
                                            items.forEach { newItemSet.add(it.id) }
                                        } else {
                                            newGroupSet.remove(groupName)
                                            items.forEach { newItemSet.remove(it.id) }
                                        }
                                        onGroupNamesChange(newGroupSet)
                                        onItemIdsChange(newItemSet)
                                    },
                                    colors = CheckboxDefaults.colors(checkedColor = accentColor),
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            AnimatedVisibility(visible = isExpanded) {
                                Column(modifier = Modifier.padding(top = 8.dp, start = 8.dp)) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        items.forEach { cat ->
                                            val isCatSelected = selectedItemIds.contains(cat.id)
                                            val catName = if (isBangla && cat.nameBn.isNotBlank()) cat.nameBn else cat.nameEn
                                            FilterChip(
                                                selected = isCatSelected,
                                                onClick = {
                                                    val newSet = selectedItemIds.toMutableSet()
                                                    if (isCatSelected) newSet.remove(cat.id) else newSet.add(cat.id)
                                                    onItemIdsChange(newSet)
                                                },
                                                label = { Text(catName, fontSize = 11.5.sp) },
                                                leadingIcon = {
                                                    IconHelper.AppIcon(
                                                        iconName = cat.iconName,
                                                        fallbackName = catName,
                                                        contentDescription = catName,
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = accentColor.copy(alpha = 0.15f),
                                                    selectedLabelColor = accentColor,
                                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                                ),
                                                border = BorderStroke(
                                                    0.6.dp,
                                                    if (isCatSelected) accentColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                                ),
                                                modifier = Modifier.height(28.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Account & Group filter section with search and expand.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AccountFilterSection(
    sectionTitle: String,
    sectionSubtitle: String,
    accounts: List<Account>,
    selectedItemIds: Set<Long>,
    selectedGroupNames: Set<String>,
    onItemIdsChange: (Set<Long>) -> Unit,
    onGroupNamesChange: (Set<String>) -> Unit,
    accentColor: Color,
    isBangla: Boolean
) {
    var searchQuery by remember { mutableStateOf("") }
    val parentMap = remember(accounts) {
        accounts.filter { it.parentId == null }.associateBy { it.id }
    }
    val childAccounts = remember(accounts) {
        accounts.filter { it.parentId != null }
    }
    val standaloneAccounts = remember(accounts) {
        val parentIdsWithChildren = childAccounts.mapNotNull { it.parentId }.toSet()
        accounts.filter { it.parentId == null && !parentIdsWithChildren.contains(it.id) }
    }

    val groupToItemsMap = remember(accounts, parentMap, childAccounts, standaloneAccounts) {
        val map = mutableMapOf<String, MutableList<Account>>()
        childAccounts.forEach { child ->
            val parentName = parentMap[child.parentId]?.let {
                if (isBangla && it.nameBn.isNotBlank()) it.nameBn else it.nameEn
            } ?: (if (isBangla) "অন্যান্য" else "Other")
            map.getOrPut(parentName) { mutableListOf() }.add(child)
        }
        standaloneAccounts.forEach { standalone ->
            val groupKey = if (isBangla) "সাধারণ একাউন্ট" else "General Accounts"
            map.getOrPut(groupKey) { mutableListOf() }.add(standalone)
        }
        map
    }

    val filteredGroups = remember(groupToItemsMap, searchQuery) {
        if (searchQuery.isBlank()) {
            groupToItemsMap
        } else {
            val q = searchQuery.trim().lowercase()
            groupToItemsMap.mapValues { entry ->
                entry.value.filter { acc ->
                    acc.nameEn.lowercase().contains(q) ||
                    acc.nameBn.lowercase().contains(q) ||
                    entry.key.lowercase().contains(q)
                }
            }.filter { it.value.isNotEmpty() }
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = sectionTitle, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = MaterialTheme.colorScheme.onSurface)
                    Text(text = sectionSubtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                }
                if (selectedItemIds.isNotEmpty() || selectedGroupNames.isNotEmpty()) {
                    TextButton(onClick = {
                        onItemIdsChange(emptySet())
                        onGroupNamesChange(emptySet())
                    }) {
                        Text(if (isBangla) "সাফ করুন" else "Clear", fontSize = 11.5.sp, color = accentColor)
                    }
                }
            }

            // Search input
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface),
                        cursorBrush = SolidColor(accentColor),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = if (isBangla) "একাউন্ট বা গ্রুপ খুঁজুন..." else "Search account or group...",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                            innerTextField()
                        },
                        modifier = Modifier.weight(1f)
                    )
                    if (searchQuery.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(18.dp).clickable { searchQuery = "" }
                        )
                    }
                }
            }

            // Expandable groups
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                filteredGroups.forEach { (groupName, items) ->
                    val isGroupSelected = selectedGroupNames.contains(groupName)
                    val allItemsSelected = items.isNotEmpty() && items.all { selectedItemIds.contains(it.id) }
                    val someItemsSelected = items.any { selectedItemIds.contains(it.id) }
                    var isExpanded by remember { mutableStateOf(false) }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            0.7.dp,
                            if (isGroupSelected || someItemsSelected) accentColor.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f).clickable { isExpanded = !isExpanded }
                                ) {
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = groupName,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.5.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                    ) {
                                        Text(
                                            text = "${items.size}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.outline,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }

                                Checkbox(
                                    checked = isGroupSelected || allItemsSelected,
                                    onCheckedChange = { checked ->
                                        val newGroupSet = selectedGroupNames.toMutableSet()
                                        val newItemSet = selectedItemIds.toMutableSet()
                                        if (checked) {
                                            newGroupSet.add(groupName)
                                            items.forEach { newItemSet.add(it.id) }
                                        } else {
                                            newGroupSet.remove(groupName)
                                            items.forEach { newItemSet.remove(it.id) }
                                        }
                                        onGroupNamesChange(newGroupSet)
                                        onItemIdsChange(newItemSet)
                                    },
                                    colors = CheckboxDefaults.colors(checkedColor = accentColor),
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            AnimatedVisibility(visible = isExpanded) {
                                Column(modifier = Modifier.padding(top = 8.dp, start = 8.dp)) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        items.forEach { acc ->
                                            val isAccSelected = selectedItemIds.contains(acc.id)
                                            val accName = if (isBangla && acc.nameBn.isNotBlank()) acc.nameBn else acc.nameEn
                                            FilterChip(
                                                selected = isAccSelected,
                                                onClick = {
                                                    val newSet = selectedItemIds.toMutableSet()
                                                    if (isAccSelected) newSet.remove(acc.id) else newSet.add(acc.id)
                                                    onItemIdsChange(newSet)
                                                },
                                                label = { Text(accName, fontSize = 11.5.sp) },
                                                leadingIcon = {
                                                    IconHelper.AppIcon(
                                                        iconName = acc.iconName,
                                                        fallbackName = accName,
                                                        contentDescription = accName,
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = accentColor.copy(alpha = 0.15f),
                                                    selectedLabelColor = accentColor,
                                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                                ),
                                                border = BorderStroke(
                                                    0.6.dp,
                                                    if (isAccSelected) accentColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                                ),
                                                modifier = Modifier.height(28.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Transaction Status Card.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TransactionStatusCard(
    selectedStatuses: Set<TransactionStatus>,
    onStatusesChange: (Set<TransactionStatus>) -> Unit,
    accentColor: Color,
    isBangla: Boolean
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isBangla) "লেনদেনের স্ট্যাটাস" else "Transaction Status",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isBangla) "ক্লিয়ার্ড, মুলতুবি বা বাতিলকৃত এন্ট্রি ফিল্টার করুন" else "Filter cleared, pending or void entries",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                if (selectedStatuses.isNotEmpty()) {
                    TextButton(onClick = { onStatusesChange(emptySet()) }) {
                        Text(if (isBangla) "সব দেখান" else "Show All", fontSize = 11.5.sp, color = accentColor)
                    }
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val statuses = listOf(
                    Pair(TransactionStatus.CLEARED, if (isBangla) "ক্লিয়ার্ড (সম্পন্ন)" else "Cleared"),
                    Pair(TransactionStatus.RECONCILED, if (isBangla) "রিকনসাইল্ড" else "Reconciled"),
                    Pair(TransactionStatus.VOID, if (isBangla) "ভয়েড (বাতিল)" else "Void"),
                    Pair(TransactionStatus.NONE, if (isBangla) "কোনটি নয় (সাধারণ)" else "Standard / None")
                )

                statuses.forEach { (status, label) ->
                    val isSelected = selectedStatuses.contains(status)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            val newSet = selectedStatuses.toMutableSet()
                            if (isSelected) newSet.remove(status) else newSet.add(status)
                            onStatusesChange(newSet)
                        },
                        label = { Text(label, fontSize = 11.5.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = accentColor.copy(alpha = 0.16f),
                            selectedLabelColor = accentColor,
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = BorderStroke(
                            0.7.dp,
                            if (isSelected) accentColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
        }
    }
}

/**
 * Transaction specific conditions (matching BudgetMaker Conditions style).
 */
@Composable
private fun TransactionConditionsCard(
    filter: TransactionTabFilter,
    onFilterChange: (TransactionTabFilter) -> Unit,
    accentColor: Color,
    isBangla: Boolean
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = if (isBangla) "বিশেষ শর্ত ও নোট ফিল্টার" else "Special Conditions & Notes",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            ConditionRowItem(
                title = if (isBangla) "শুধু নোট বা বিবরণযুক্ত লেনদেন" else "Has Notes / Description Only",
                subtitle = if (isBangla) "যেসব লেনদেনে নোট বা মেমো সংযুক্ত আছে" else "Transactions with attached notes or remarks",
                checked = filter.onlyWithReceiptOrNote,
                onCheckedChange = { onFilterChange(filter.copy(onlyWithReceiptOrNote = it, onlyWithoutNote = false)) },
                accentColor = accentColor,
                icon = Icons.Default.Description
            )

            ConditionRowItem(
                title = if (isBangla) "নোটহীন লেনদেন" else "Without Notes Only",
                subtitle = if (isBangla) "যেসব লেনদেনে কোনো নোট বা মন্তব্য নেই" else "Transactions with empty note fields",
                checked = filter.onlyWithoutNote,
                onCheckedChange = { onFilterChange(filter.copy(onlyWithoutNote = it, onlyWithReceiptOrNote = false)) },
                accentColor = accentColor,
                icon = Icons.Default.Tune
            )

            ConditionRowItem(
                title = if (isBangla) "শুধু রিভার্সাল / রিভার্ট লেনদেন" else "Reversals / Adjustments Only",
                subtitle = if (isBangla) "ফেরত বা রিভার্সকৃত ঋণাত্মক এন্ট্রি" else "Negative or reverted adjustment entries",
                checked = filter.onlyReversals,
                onCheckedChange = { onFilterChange(filter.copy(onlyReversals = it)) },
                accentColor = accentColor,
                icon = Icons.Default.SwapHoriz
            )

            ConditionRowItem(
                title = if (isBangla) "শুধু বড় অংকের লেনদেন (≥ ১০,০০০ ৳)" else "High Value Transactions (≥ 10,000 ৳)",
                subtitle = if (isBangla) "১০,০০০ টাকার সমান বা বেশি বড় অংকের লেনদেন" else "Transactions equal to or exceeding ৳10,000",
                checked = filter.onlyHighValue,
                onCheckedChange = { onFilterChange(filter.copy(onlyHighValue = it)) },
                accentColor = accentColor,
                icon = Icons.Default.MonetizationOn
            )
        }
    }
}

/**
 * Display & Presentation options card.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TransactionDisplayOptionsCard(
    rowStyle: LedgerRowStyle,
    onRowStyleChange: (LedgerRowStyle) -> Unit,
    displaySettings: TransactionDisplaySettings,
    onDisplaySettingsChange: (TransactionDisplaySettings) -> Unit,
    accentColor: Color,
    isBangla: Boolean
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = if (isBangla) "প্রদর্শন পছন্দসমূহ ও রো স্টাইল" else "Display & Layout Preferences",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Row style selector
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = if (isBangla) "রো স্টাইল:" else "Row Density:",
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.outline
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val styles = listOf(
                        Pair(LedgerRowStyle.STANDARD, if (isBangla) "স্ট্যান্ডার্ড" else "Standard"),
                        Pair(LedgerRowStyle.COMPACT, if (isBangla) "কমপ্যাক্ট" else "Compact"),
                        Pair(LedgerRowStyle.DETAILED, if (isBangla) "বিস্তারিত" else "Detailed")
                    )
                    styles.forEach { (style, label) ->
                        val isSelected = rowStyle == style
                        FilterChip(
                            selected = isSelected,
                            onClick = { onRowStyleChange(style) },
                            label = { Text(label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = accentColor.copy(alpha = 0.16f),
                                selectedLabelColor = accentColor
                            )
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

            ConditionRowItem(
                title = if (isBangla) "মোট পরিমাণ প্রদর্শন করুন" else "Show Filtered Total Amount",
                subtitle = if (isBangla) "ফিল্টার বারে মোট আয়ের/ব্যয়ের সারসংক্ষেপ দেখান" else "Display net total amount badge in filter bar",
                checked = displaySettings.showTotalAmount,
                onCheckedChange = { onDisplaySettingsChange(displaySettings.copy(showTotalAmount = it)) },
                accentColor = accentColor,
                icon = Icons.Default.FilterAlt
            )

            ConditionRowItem(
                title = if (isBangla) "মোটে স্থানান্তর অন্তর্ভুক্ত করুন" else "Include Transfers in Net Total",
                subtitle = if (isBangla) "মোট হিসাব করার সময় স্থানান্তর লেনদেন যোগ করুন" else "Factor transfer amounts into net calculation",
                checked = displaySettings.showTransfersInTotal,
                onCheckedChange = { onDisplaySettingsChange(displaySettings.copy(showTransfersInTotal = it)) },
                accentColor = accentColor,
                icon = Icons.Default.SwapHoriz
            )

            ConditionRowItem(
                title = if (isBangla) "পুরাতন তারিখ আগে দেখান" else "Show Oldest Date First",
                subtitle = if (isBangla) "তারিখের ঊর্ধ্বক্রম অনুযায়ী লেনদেন সাজান" else "Sort chronological oldest to newest",
                checked = displaySettings.showOldestDateFirst,
                onCheckedChange = { onDisplaySettingsChange(displaySettings.copy(showOldestDateFirst = it)) },
                accentColor = accentColor,
                icon = Icons.Default.CalendarMonth
            )
        }
    }
}

/**
 * Reusable condition row with title, description, and Checkbox (Matching BudgetMakerFilterDialog).
 */
@Composable
private fun ConditionRowItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    accentColor: Color,
    icon: ImageVector? = null
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (checked) accentColor.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            0.6.dp,
            if (checked) accentColor.copy(alpha = 0.45f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onCheckedChange(!checked) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (icon != null) {
                    Surface(
                        shape = RoundedCornerShape(7.dp),
                        color = if (checked) accentColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (checked) accentColor else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontWeight = if (checked) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 12.sp,
                        color = if (checked) accentColor else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        fontSize = 10.5.sp,
                        color = MaterialTheme.colorScheme.outline,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Checkbox(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(checkedColor = accentColor),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
