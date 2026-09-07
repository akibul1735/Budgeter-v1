package com.example.ui.components

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Account
import com.example.data.model.Category
import com.example.data.model.CategoryType
import com.example.data.model.LanguageMode
import com.example.data.model.TransactionStatus
import com.example.ui.theme.SolidExpense
import com.example.ui.theme.SolidIncome
import com.example.util.IconHelper
import com.example.util.LanguageHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val BrandGreen = Color(0xFF16A34A)
private val BrandGreenLight = Color(0xFFE8F5E9)
private val FieldBorderColor = Color(0xFFD1D5DB)
private val LabelTextColor = Color(0xFF4B5563)
private val ValueTextColor = Color(0xFF1F2937)

/**
 * Filter window layout matching Bluecoins-style bottom sheet filter modal.
 * Includes full customization: Date Range, Categories, Accounts, Labels, Status,
 * Exclude Zero Amounts, Currency Display & Symbol, Expense Categories First, Sort By Amount,
 * Comparison Toggles, and Preset Save/Load.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BudgetFilterDialog(
    currentFilter: BudgetFilterState,
    categories: List<Category>,
    accounts: List<Account>,
    allLabels: List<String> = emptyList(),
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onApply: (BudgetFilterState) -> Unit
) {
    val context = LocalContext.current
    var tempFilter by remember { mutableStateOf(currentFilter) }

    // Sub-dialogs state
    var showDatePresetDialog by remember { mutableStateOf(false) }
    var showCategoryMultiSelect by remember { mutableStateOf(false) }
    var showAccountMultiSelect by remember { mutableStateOf(false) }
    var showLabelsMultiSelect by remember { mutableStateOf(false) }
    var showStatusMultiSelect by remember { mutableStateOf(false) }
    var showSavePresetDialog by remember { mutableStateOf(false) }
    var showLoadPresetsDialog by remember { mutableStateOf(false) }

    // Date Pickers state for custom ranges
    var showCustomStartDatePicker by remember { mutableStateOf(false) }
    var showCustomEndDatePicker by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.92f)
                    .clickable(enabled = false) {}
                    .testTag("budget_filter_dialog")
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Top Drag Handle & Action Icons Header
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp, bottom = 4.dp, start = 16.dp, end = 16.dp)
                    ) {
                        // Centered Drag Bar
                        Box(
                            modifier = Modifier
                                .width(38.dp)
                                .height(4.5.dp)
                                .align(Alignment.Center)
                                .background(Color(0xFFD1D5DB), CircleShape)
                        )

                        // Top Right Action Buttons (Reset, Save Preset, Load Presets)
                        Row(
                            modifier = Modifier.align(Alignment.CenterEnd),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 1. Reset Button
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                IconButton(
                                    onClick = {
                                        tempFilter = BudgetFilterState()
                                    },
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.RestartAlt,
                                        contentDescription = "Reset Filter",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            // 2. Save Preset Button
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                IconButton(
                                    onClick = { showSavePresetDialog = true },
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Save,
                                        contentDescription = "Save Preset",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                            }

                            // 3. Load Saved Presets Button
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                IconButton(
                                    onClick = { showLoadPresetsDialog = true },
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FolderOpen,
                                        contentDescription = "Saved Presets",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Scrollable Filter Body
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // 1. DATE RANGE FIELD
                        FilterDropdownField(
                            label = if (languageMode == LanguageMode.BANGLA) "তারিখ সীমা" else "Date Range",
                            displayValue = when (tempFilter.datePreset) {
                                BudgetDateRangePreset.THIS_MONTH -> if (languageMode == LanguageMode.BANGLA) "চলতি মাস" else "This Month"
                                BudgetDateRangePreset.LAST_MONTH -> if (languageMode == LanguageMode.BANGLA) "গত মাস" else "Last Month"
                                BudgetDateRangePreset.LAST_3_MONTHS -> if (languageMode == LanguageMode.BANGLA) "গত ৩ মাস" else "Last 3 Months"
                                BudgetDateRangePreset.LAST_6_MONTHS -> if (languageMode == LanguageMode.BANGLA) "গত ৬ মাস" else "Last 6 Months"
                                BudgetDateRangePreset.YEAR_TO_DATE -> if (languageMode == LanguageMode.BANGLA) "বছরের শুরু থেকে" else "Year to Date"
                                BudgetDateRangePreset.SAME_MONTH_LAST_YEAR -> if (languageMode == LanguageMode.BANGLA) "গত বছরের এই মাস" else "Same Month Last Year"
                                BudgetDateRangePreset.ALL_TIME -> if (languageMode == LanguageMode.BANGLA) "সব সময়" else "All Time"
                                BudgetDateRangePreset.CUSTOM -> {
                                    val start = tempFilter.customStartDateMs?.let { dateFormat.format(Date(it)) } ?: "Start"
                                    val end = tempFilter.customEndDateMs?.let { dateFormat.format(Date(it)) } ?: "End"
                                    "$start - $end"
                                }
                            },
                            onClick = { showDatePresetDialog = true }
                        )

                        // 2. CATEGORY FIELD
                        val selectedCatCount = tempFilter.selectedCategoryIds.size
                        val categoryDisplayText = if (selectedCatCount == 0) {
                            if (languageMode == LanguageMode.BANGLA) "সকল ক্যাটাগরি" else "All Categories"
                        } else {
                            val names = categories.filter { tempFilter.selectedCategoryIds.contains(it.id) }
                                .take(2)
                                .joinToString(", ") { LanguageHelper.getLocalizedName(it.nameEn, it.nameBn, languageMode) }
                            if (selectedCatCount > 2) "$names (+${selectedCatCount - 2})" else names
                        }
                        FilterSelectorWithIconButton(
                            label = if (languageMode == LanguageMode.BANGLA) "ক্যাটাগরি" else "Category",
                            displayValue = categoryDisplayText,
                            onClickMain = { showCategoryMultiSelect = true },
                            onClickIcon = { showCategoryMultiSelect = true }
                        )

                        // 3. ACCOUNT FIELD
                        val selectedAccCount = tempFilter.selectedAccountIds.size
                        val accountDisplayText = if (selectedAccCount == 0) {
                            if (languageMode == LanguageMode.BANGLA) "সকল অ্যাকাউন্ট" else "All Accounts"
                        } else {
                            val names = accounts.filter { tempFilter.selectedAccountIds.contains(it.id) }
                                .take(2)
                                .joinToString(", ") { it.localizedName(languageMode) }
                            if (selectedAccCount > 2) "$names (+${selectedAccCount - 2})" else names
                        }
                        FilterSelectorWithIconButton(
                            label = if (languageMode == LanguageMode.BANGLA) "অ্যাকাউন্ট" else "Account",
                            displayValue = accountDisplayText,
                            onClickMain = { showAccountMultiSelect = true },
                            onClickIcon = { showAccountMultiSelect = true }
                        )

                        // 4. LABELS FIELD
                        val selectedLabelsCount = tempFilter.selectedLabels.size
                        val labelsDisplayText = if (selectedLabelsCount == 0) {
                            if (languageMode == LanguageMode.BANGLA) "(কোন ফিল্টার নেই)" else "(No Filter)"
                        } else {
                            val joined = tempFilter.selectedLabels.take(2).joinToString(", ")
                            if (selectedLabelsCount > 2) "$joined (+${selectedLabelsCount - 2})" else joined
                        }
                        FilterSelectorWithIconButton(
                            label = if (languageMode == LanguageMode.BANGLA) "লেবেল" else "Labels",
                            displayValue = labelsDisplayText,
                            onClickMain = { showLabelsMultiSelect = true },
                            onClickIcon = { showLabelsMultiSelect = true }
                        )

                        // 5. STATUS FIELD
                        val selectedStatusCount = tempFilter.selectedStatusSet.size
                        val statusDisplayText = if (selectedStatusCount == 0) {
                            if (languageMode == LanguageMode.BANGLA) "(কোন ফিল্টার নেই)" else "(No Filter)"
                        } else {
                            tempFilter.selectedStatusSet.joinToString(", ") {
                                if (languageMode == LanguageMode.BANGLA) it.titleBn else it.titleEn
                            }
                        }
                        FilterSelectorWithIconButton(
                            label = if (languageMode == LanguageMode.BANGLA) "স্ট্যাটাস" else "Status",
                            displayValue = statusDisplayText,
                            onClickMain = { showStatusMultiSelect = true },
                            onClickIcon = { showStatusMultiSelect = true }
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // TOGGLES SECTION
                        // 1. Exclude zero amounts
                        FilterSwitchRow(
                            title = if (languageMode == LanguageMode.BANGLA) "শূন্য পরিমাণ বাদ দিন" else "Exclude zero amounts",
                            checked = tempFilter.excludeZeroAmounts,
                            onCheckedChange = { tempFilter = tempFilter.copy(excludeZeroAmounts = it) }
                        )

                        // 2. Display currency
                        FilterSwitchRow(
                            title = if (languageMode == LanguageMode.BANGLA) "মুদ্রা প্রদর্শন করুন" else "Display currency",
                            checked = tempFilter.displayCurrency,
                            onCheckedChange = { tempFilter = tempFilter.copy(displayCurrency = it) }
                        )

                        // 3. Display Currency Symbol
                        FilterSwitchRow(
                            title = if (languageMode == LanguageMode.BANGLA) "মুদ্রা প্রতীক প্রদর্শন করুন" else "Display Currency Symbol",
                            checked = tempFilter.displayCurrencySymbol,
                            onCheckedChange = { tempFilter = tempFilter.copy(displayCurrencySymbol = it) }
                        )

                        // 4. Show expense categories first
                        FilterSwitchRow(
                            title = if (languageMode == LanguageMode.BANGLA) "প্রথমে ব্যয়ের ক্যাটাগরি দেখান" else "Show expense categories first",
                            checked = tempFilter.showExpenseCategoriesFirst,
                            onCheckedChange = { tempFilter = tempFilter.copy(showExpenseCategoriesFirst = it) }
                        )

                        // 5. Sort by amount
                        FilterSwitchRow(
                            title = if (languageMode == LanguageMode.BANGLA) "পরিমাণ অনুযায়ী সাজান" else "Sort by amount",
                            checked = tempFilter.sortByAmount,
                            onCheckedChange = { tempFilter = tempFilter.copy(sortByAmount = it) }
                        )

                        // 6. Comparison on or off
                        FilterSwitchRow(
                            title = if (languageMode == LanguageMode.BANGLA) "তুলনামূলক বিশ্লেষণ" else "Comparison",
                            checked = tempFilter.comparisonEnabled,
                            onCheckedChange = { tempFilter = tempFilter.copy(comparisonEnabled = it) }
                        )

                        // Comparison Baseline Preset selector if enabled
                        AnimatedVisibility(
                            visible = tempFilter.comparisonEnabled,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 8.dp, end = 8.dp, bottom = 4.dp)
                            ) {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "তুলনার বেসলাইন:" else "Comparison Baseline:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = BrandGreen
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    BudgetComparisonPreset.values().forEach { preset ->
                                        val isSelected = tempFilter.comparisonPreset == preset
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { tempFilter = tempFilter.copy(comparisonPreset = preset) },
                                            label = {
                                                Text(
                                                    text = if (languageMode == LanguageMode.BANGLA) preset.titleBn else preset.titleEn,
                                                    fontSize = 11.5.sp
                                                )
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = BrandGreen,
                                                selectedLabelColor = Color.White
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        // 7. Only Budgeted Categories toggle
                        FilterSwitchRow(
                            title = if (languageMode == LanguageMode.BANGLA) "শুধুমাত্র বাজেট নির্ধারিত ক্যাটাগরি" else "Only Budgeted Categories",
                            checked = tempFilter.filterOnlyBudgeted,
                            onCheckedChange = { tempFilter = tempFilter.copy(filterOnlyBudgeted = it) }
                        )

                        // 8. Only Over-Budget Categories toggle
                        FilterSwitchRow(
                            title = if (languageMode == LanguageMode.BANGLA) "শুধুমাত্র বাজেট অতিক্রান্ত ক্যাটাগরি" else "Only Over Budget Categories",
                            checked = tempFilter.filterOnlyOverBudget,
                            onCheckedChange = { tempFilter = tempFilter.copy(filterOnlyOverBudget = it) }
                        )
                    }

                    HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 1.dp)

                    // Bottom Action Buttons: Cancel and OK matching screenshot layout
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Cancel Button
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("budget_filter_cancel_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF3F4F6),
                                contentColor = Color(0xFF374151)
                            ),
                            border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "বাতিল" else "Cancel",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // OK Button (Solid Green Pill)
                        Button(
                            onClick = { onApply(tempFilter) },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("budget_filter_apply_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrandGreen,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "ঠিক আছে" else "OK",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    // --- SUB-DIALOG 1: Date Range Presets Dialog ---
    if (showDatePresetDialog) {
        Dialog(onDismissRequest = { showDatePresetDialog = false }) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .padding(vertical = 20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "তারিখ সীমা নির্বাচন" else "Select Date Range",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        BudgetDateRangePreset.values().forEach { preset ->
                            val isSelected = tempFilter.datePreset == preset
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) BrandGreenLight else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                border = if (isSelected) BorderStroke(1.5.dp, BrandGreen) else null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        tempFilter = tempFilter.copy(datePreset = preset)
                                        if (preset != BudgetDateRangePreset.CUSTOM) {
                                            showDatePresetDialog = false
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) preset.labelBn else preset.labelEn,
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) BrandGreen else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (isSelected) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = BrandGreen, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }

                        // Custom Date Pickers if CUSTOM selected
                        if (tempFilter.datePreset == BudgetDateRangePreset.CUSTOM) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { showCustomStartDatePicker = true },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = tempFilter.customStartDateMs?.let { dateFormat.format(Date(it)) } ?: "Start Date",
                                        fontSize = 12.sp,
                                        maxLines = 1
                                    )
                                }
                                OutlinedButton(
                                    onClick = { showCustomEndDatePicker = true },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = tempFilter.customEndDateMs?.let { dateFormat.format(Date(it)) } ?: "End Date",
                                        fontSize = 12.sp,
                                        maxLines = 1
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Button(
                                onClick = { showDatePresetDialog = false },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Done", fontSize = 13.5.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    // --- SUB-DIALOG 2: Category Multi-Select Dialog ---
    if (showCategoryMultiSelect) {
        CategoryMultiSelectDialog(
            allCategories = categories,
            selectedIds = tempFilter.selectedCategoryIds,
            languageMode = languageMode,
            onDismiss = { showCategoryMultiSelect = false },
            onConfirm = { newSelected ->
                tempFilter = tempFilter.copy(selectedCategoryIds = newSelected)
                showCategoryMultiSelect = false
            }
        )
    }

    // --- SUB-DIALOG 3: Account Multi-Select Dialog ---
    if (showAccountMultiSelect) {
        AccountMultiSelectDialog(
            allAccounts = accounts,
            selectedIds = tempFilter.selectedAccountIds,
            languageMode = languageMode,
            onDismiss = { showAccountMultiSelect = false },
            onConfirm = { newSelected ->
                tempFilter = tempFilter.copy(selectedAccountIds = newSelected)
                showAccountMultiSelect = false
            }
        )
    }

    // --- SUB-DIALOG 4: Labels Multi-Select Dialog ---
    if (showLabelsMultiSelect) {
        LabelsMultiSelectDialog(
            allLabels = allLabels,
            selectedLabels = tempFilter.selectedLabels,
            languageMode = languageMode,
            onDismiss = { showLabelsMultiSelect = false },
            onConfirm = { newSelected ->
                tempFilter = tempFilter.copy(selectedLabels = newSelected)
                showLabelsMultiSelect = false
            }
        )
    }

    // --- SUB-DIALOG 5: Status Multi-Select Dialog ---
    if (showStatusMultiSelect) {
        StatusMultiSelectDialog(
            selectedStatusSet = tempFilter.selectedStatusSet,
            languageMode = languageMode,
            onDismiss = { showStatusMultiSelect = false },
            onConfirm = { newSelected ->
                tempFilter = tempFilter.copy(selectedStatusSet = newSelected)
                showStatusMultiSelect = false
            }
        )
    }

    // --- SUB-DIALOG 6: Save Preset Dialog ---
    if (showSavePresetDialog) {
        SavePresetDialog(
            currentFilter = tempFilter,
            context = context,
            languageMode = languageMode,
            onDismiss = { showSavePresetDialog = false }
        )
    }

    // --- SUB-DIALOG 7: Load Presets Dialog ---
    if (showLoadPresetsDialog) {
        LoadPresetsDialog(
            context = context,
            languageMode = languageMode,
            onDismiss = { showLoadPresetsDialog = false },
            onSelectPreset = { loadedFilter ->
                tempFilter = loadedFilter
                showLoadPresetsDialog = false
            }
        )
    }

    // Custom Date Range Pickers
    if (showCustomStartDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = tempFilter.customStartDateMs ?: System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { showCustomStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        tempFilter = tempFilter.copy(customStartDateMs = it)
                    }
                    showCustomStartDatePicker = false
                }) {
                    Text("OK", color = BrandGreen)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomStartDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showCustomEndDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = tempFilter.customEndDateMs ?: System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { showCustomEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        tempFilter = tempFilter.copy(customEndDateMs = it)
                    }
                    showCustomEndDatePicker = false
                }) {
                    Text("OK", color = BrandGreen)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomEndDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

// -----------------------------------------------------------------------------
// ACTIVE BUDGET FILTER BAR
// -----------------------------------------------------------------------------

@Composable
fun ActiveBudgetFilterBar(
    filterState: BudgetFilterState,
    onFilterChange: (BudgetFilterState) -> Unit,
    onOpenFilterDialog: () -> Unit,
    languageMode: LanguageMode,
    modifier: Modifier = Modifier
) {
    if (!filterState.isFilterActive) return

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Active count badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = BrandGreen,
                    modifier = Modifier.clickable { onOpenFilterDialog() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(Icons.Default.FilterList, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                        Text(
                            text = "${filterState.activeFilterCount} Active",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                if (filterState.datePreset != BudgetDateRangePreset.THIS_MONTH) {
                    FilterChipPill(
                        text = if (languageMode == LanguageMode.BANGLA) filterState.datePreset.labelBn else filterState.datePreset.labelEn,
                        onClear = { onFilterChange(filterState.copy(datePreset = BudgetDateRangePreset.THIS_MONTH, customStartDateMs = null, customEndDateMs = null)) }
                    )
                }

                if (filterState.selectedCategoryIds.isNotEmpty()) {
                    FilterChipPill(
                        text = "${filterState.selectedCategoryIds.size} Cats",
                        onClear = { onFilterChange(filterState.copy(selectedCategoryIds = emptySet())) }
                    )
                }

                if (filterState.selectedAccountIds.isNotEmpty()) {
                    FilterChipPill(
                        text = "${filterState.selectedAccountIds.size} Accounts",
                        onClear = { onFilterChange(filterState.copy(selectedAccountIds = emptySet())) }
                    )
                }

                if (filterState.selectedLabels.isNotEmpty()) {
                    FilterChipPill(
                        text = "${filterState.selectedLabels.size} Labels",
                        onClear = { onFilterChange(filterState.copy(selectedLabels = emptySet())) }
                    )
                }

                if (filterState.selectedStatusSet.isNotEmpty()) {
                    FilterChipPill(
                        text = "${filterState.selectedStatusSet.size} Status",
                        onClear = { onFilterChange(filterState.copy(selectedStatusSet = emptySet())) }
                    )
                }

                if (filterState.excludeZeroAmounts) {
                    FilterChipPill(
                        text = "Non-zero",
                        onClear = { onFilterChange(filterState.copy(excludeZeroAmounts = false)) }
                    )
                }

                if (filterState.sortByAmount) {
                    FilterChipPill(
                        text = "Sorted by Amt",
                        onClear = { onFilterChange(filterState.copy(sortByAmount = false)) }
                    )
                }

                if (!filterState.comparisonEnabled) {
                    FilterChipPill(
                        text = "No Comparison",
                        onClear = { onFilterChange(filterState.copy(comparisonEnabled = true)) }
                    )
                }
            }

            // Reset all text button
            TextButton(
                onClick = { onFilterChange(BudgetFilterState()) },
                modifier = Modifier.padding(start = 4.dp)
            ) {
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "রিসেট" else "Reset",
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun FilterChipPill(
    text: String,
    onClear: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(start = 8.dp, end = 4.dp, top = 2.dp, bottom = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(text = text, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove",
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier
                    .size(14.dp)
                    .clickable(onClick = onClear)
            )
        }
    }
}

// -----------------------------------------------------------------------------
// UI SUBCOMPONENTS MATCHING SCREENSHOT EXACTLY
// -----------------------------------------------------------------------------

@Composable
private fun FilterDropdownField(
    label: String,
    displayValue: String,
    onClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = LabelTextColor
        )
        Spacer(modifier = Modifier.height(5.dp))
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            border = BorderStroke(1.2.dp, FieldBorderColor),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clickable(onClick = onClick)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = displayValue,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = ValueTextColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = Color(0xFF6B7280),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun FilterSelectorWithIconButton(
    label: String,
    displayValue: String,
    onClickMain: () -> Unit,
    onClickIcon: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = LabelTextColor
        )
        Spacer(modifier = Modifier.height(5.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Main Outlined Box
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                border = BorderStroke(1.2.dp, FieldBorderColor),
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .clickable(onClick = onClickMain)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = displayValue,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = ValueTextColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = Color(0xFF6B7280),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Right Filter Icon Button (Soft Mint/Teal Container matching Screenshot)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFE2F3E7),
                modifier = Modifier
                    .size(50.dp)
                    .clickable(onClick = onClickIcon)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filter",
                        tint = Color(0xFF1E3A2F),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 14.5.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF1F2937)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = BrandGreen,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFCBD5E1),
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}

// -----------------------------------------------------------------------------
// MULTI-SELECT DIALOGS FOR CATEGORY, ACCOUNT, LABELS, STATUS
// -----------------------------------------------------------------------------

@Composable
private fun CategoryMultiSelectDialog(
    allCategories: List<Category>,
    selectedIds: Set<Long>,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onConfirm: (Set<Long>) -> Unit
) {
    var tempSelected by remember { mutableStateOf(selectedIds.toMutableSet()) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedTypeTab by remember { mutableStateOf<CategoryType?>(null) } // null = All

    val filteredList = remember(allCategories, searchQuery, selectedTypeTab) {
        allCategories.filter { cat ->
            cat.isActive &&
                    (selectedTypeTab == null || cat.type == selectedTypeTab) &&
                    (searchQuery.isBlank() ||
                            cat.nameEn.contains(searchQuery, ignoreCase = true) ||
                            cat.nameBn.contains(searchQuery, ignoreCase = true))
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "ক্যাটাগরি নির্বাচন (${tempSelected.size})" else "Select Categories (${tempSelected.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row {
                        TextButton(onClick = { tempSelected = allCategories.map { it.id }.toMutableSet() }) {
                            Text("All", fontSize = 12.sp, color = BrandGreen)
                        }
                        TextButton(onClick = { tempSelected = mutableSetOf() }) {
                            Text("Clear", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }

                // Type Tabs: All | Expense | Income
                TabRow(
                    selectedTabIndex = when (selectedTypeTab) {
                        null -> 0
                        CategoryType.EXPENSE -> 1
                        CategoryType.INCOME -> 2
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = selectedTypeTab == null,
                        onClick = { selectedTypeTab = null },
                        text = { Text("All", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedTypeTab == CategoryType.EXPENSE,
                        onClick = { selectedTypeTab = CategoryType.EXPENSE },
                        text = { Text("Expense", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedTypeTab == CategoryType.INCOME,
                        onClick = { selectedTypeTab = CategoryType.INCOME },
                        text = { Text("Income", fontSize = 12.sp) }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search category...", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Category Items List
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    items(filteredList, key = { it.id }) { category ->
                        val isChecked = tempSelected.contains(category.id)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isChecked) tempSelected.remove(category.id)
                                    else tempSelected.add(category.id)
                                }
                                .padding(vertical = 6.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { checked ->
                                    if (checked == true) tempSelected.add(category.id)
                                    else tempSelected.remove(category.id)
                                },
                                colors = CheckboxDefaults.colors(checkedColor = BrandGreen)
                            )
                            Icon(
                                imageVector = IconHelper.getIconByName(category.iconName),
                                contentDescription = null,
                                tint = if (category.type == CategoryType.EXPENSE) SolidExpense else SolidIncome,
                                modifier = Modifier
                                    .size(24.dp)
                                    .padding(end = 6.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = LanguageHelper.getLocalizedName(category.nameEn, category.nameBn, languageMode),
                                    fontSize = 14.sp,
                                    fontWeight = if (isChecked) FontWeight.SemiBold else FontWeight.Normal
                                )
                                if (category.parentId != null) {
                                    Text(
                                        text = "Subcategory",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Bottom Done Button
                Button(
                    onClick = { onConfirm(tempSelected) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Apply Selection", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun AccountMultiSelectDialog(
    allAccounts: List<Account>,
    selectedIds: Set<Long>,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onConfirm: (Set<Long>) -> Unit
) {
    var tempSelected by remember { mutableStateOf(selectedIds.toMutableSet()) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredList = remember(allAccounts, searchQuery) {
        allAccounts.filter { acc ->
            acc.isActive &&
                    (searchQuery.isBlank() ||
                            acc.nameEn.contains(searchQuery, ignoreCase = true) ||
                            acc.nameBn.contains(searchQuery, ignoreCase = true) ||
                            acc.type.name.contains(searchQuery, ignoreCase = true))
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.80f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "অ্যাকাউন্ট নির্বাচন (${tempSelected.size})" else "Select Accounts (${tempSelected.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row {
                        TextButton(onClick = { tempSelected = allAccounts.map { it.id }.toMutableSet() }) {
                            Text("All", fontSize = 12.sp, color = BrandGreen)
                        }
                        TextButton(onClick = { tempSelected = mutableSetOf() }) {
                            Text("Clear", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search account...", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Accounts List
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    items(filteredList, key = { it.id }) { account ->
                        val isChecked = tempSelected.contains(account.id)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isChecked) tempSelected.remove(account.id)
                                    else tempSelected.add(account.id)
                                }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { checked ->
                                    if (checked == true) tempSelected.add(account.id)
                                    else tempSelected.remove(account.id)
                                },
                                colors = CheckboxDefaults.colors(checkedColor = BrandGreen)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = account.localizedName(languageMode),
                                    fontSize = 14.5.sp,
                                    fontWeight = if (isChecked) FontWeight.SemiBold else FontWeight.Normal
                                )
                                Text(
                                    text = account.type.name,
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                            Text(
                                text = "BDT ${LanguageHelper.formatNumber(account.initialBalance, languageMode)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (account.initialBalance >= 0) SolidIncome else SolidExpense
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { onConfirm(tempSelected) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Apply Selection", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LabelsMultiSelectDialog(
    allLabels: List<String>,
    selectedLabels: Set<String>,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onConfirm: (Set<String>) -> Unit
) {
    var tempSelected by remember { mutableStateOf(selectedLabels.toMutableSet()) }
    var newTagInput by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }

    val availableLabels = remember(allLabels, newTagInput) {
        (allLabels + if (newTagInput.isNotBlank() && !allLabels.contains(newTagInput.trim())) listOf(newTagInput.trim()) else emptyList()).distinct().sorted()
    }

    val filteredLabels = remember(availableLabels, searchQuery) {
        if (searchQuery.isBlank()) availableLabels else availableLabels.filter { it.contains(searchQuery, ignoreCase = true) }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.75f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "লেবেল নির্বাচন (${tempSelected.size})" else "Select Labels (${tempSelected.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = { tempSelected = mutableSetOf() }) {
                        Text("Clear", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // New Tag Input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newTagInput,
                        onValueChange = { newTagInput = it },
                        placeholder = { Text("Add tag/hashtag (e.g. #trip)", fontSize = 13.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                    IconButton(
                        onClick = {
                            if (newTagInput.isNotBlank()) {
                                tempSelected.add(newTagInput.trim())
                                newTagInput = ""
                            }
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Tag", tint = BrandGreen)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Labels Chips Flow
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    if (filteredLabels.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No labels found. Add custom labels above.", fontSize = 13.sp, color = MaterialTheme.colorScheme.outline)
                        }
                    } else {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            filteredLabels.forEach { label ->
                                val isSelected = tempSelected.contains(label)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        if (isSelected) tempSelected.remove(label) else tempSelected.add(label)
                                    },
                                    label = { Text(label, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = BrandGreen,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { onConfirm(tempSelected) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Apply Labels", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun StatusMultiSelectDialog(
    selectedStatusSet: Set<TransactionStatus>,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onConfirm: (Set<TransactionStatus>) -> Unit
) {
    var tempSelected by remember { mutableStateOf(selectedStatusSet.toMutableSet()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "স্ট্যাটাস নির্বাচন" else "Select Transaction Status",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = { tempSelected = mutableSetOf() }) {
                        Text("Clear", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                TransactionStatus.values().forEach { status ->
                    val isChecked = tempSelected.contains(status)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isChecked) tempSelected.remove(status) else tempSelected.add(status)
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { checked ->
                                if (checked == true) tempSelected.add(status) else tempSelected.remove(status)
                            },
                            colors = CheckboxDefaults.colors(checkedColor = BrandGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) status.titleBn else status.titleEn,
                            fontSize = 14.5.sp,
                            fontWeight = if (isChecked) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = { onConfirm(tempSelected) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Apply Status", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// PRESETS MANAGER: SAVE & LOAD FILTER PRESETS
// -----------------------------------------------------------------------------

@Composable
private fun SavePresetDialog(
    currentFilter: BudgetFilterState,
    context: Context,
    languageMode: LanguageMode,
    onDismiss: () -> Unit
) {
    var presetName by remember { mutableStateOf("") }
    var isSaved by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(0.92f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "ফিল্টার প্রিসেট সংরক্ষণ করুন" else "Save Filter Preset",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = presetName,
                    onValueChange = { presetName = it },
                    placeholder = { Text("Preset name (e.g. Monthly Focus)", fontSize = 13.5.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (isSaved) {
                    Text(
                        text = "Preset saved successfully!",
                        color = BrandGreen,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (presetName.isNotBlank()) {
                                savePresetToPrefs(context, presetName.trim(), currentFilter)
                                isSaved = true
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadPresetsDialog(
    context: Context,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onSelectPreset: (BudgetFilterState) -> Unit
) {
    val presets = remember { loadSavedPresetNames(context) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "সংরক্ষিত প্রিসেট" else "Saved Presets",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Default Built-in Presets
                Text(
                    text = "Built-in Presets",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandGreen
                )
                Spacer(modifier = Modifier.height(6.dp))

                PresetItemRow(
                    name = "Default Monthly Budget",
                    subtitle = "This Month • All Categories & Accounts",
                    onClick = { onSelectPreset(BudgetFilterState()) }
                )
                PresetItemRow(
                    name = "Over Budget Watchlist",
                    subtitle = "Only Over-Budget Items • Sorted High to Low",
                    onClick = {
                        onSelectPreset(
                            BudgetFilterState(
                                filterOnlyOverBudget = true,
                                sortByAmount = true
                            )
                        )
                    }
                )
                PresetItemRow(
                    name = "Non-Zero Active Summary",
                    subtitle = "Exclude Zero Amounts • Currency Symbol On",
                    onClick = {
                        onSelectPreset(
                            BudgetFilterState(
                                excludeZeroAmounts = true,
                                displayCurrency = true,
                                displayCurrencySymbol = true
                            )
                        )
                    }
                )

                if (presets.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Your Custom Presets",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandGreen
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    presets.forEach { name ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val filter = getSavedPreset(context, name)
                                    if (filter != null) onSelectPreset(filter)
                                }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(name, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                Text("Custom Preset", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.outline)
                            }
                            IconButton(onClick = { deletePresetFromPrefs(context, name) }) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PresetItemRow(
    name: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(name, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
        }
    }
}

// SharedPreferences Helpers for Presets
private const val PREFS_NAME = "budget_filter_presets_store"

private fun savePresetToPrefs(context: Context, name: String, state: BudgetFilterState) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val names = prefs.getStringSet("preset_keys", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
    names.add(name)
    prefs.edit()
        .putStringSet("preset_keys", names)
        .putString("preset_${name}_datePreset", state.datePreset.name)
        .putBoolean("preset_${name}_excludeZero", state.excludeZeroAmounts)
        .putBoolean("preset_${name}_displayCurr", state.displayCurrency)
        .putBoolean("preset_${name}_displayCurrSym", state.displayCurrencySymbol)
        .putBoolean("preset_${name}_showExpenseFirst", state.showExpenseCategoriesFirst)
        .putBoolean("preset_${name}_sortByAmount", state.sortByAmount)
        .putBoolean("preset_${name}_comparisonEnabled", state.comparisonEnabled)
        .putBoolean("preset_${name}_onlyBudgeted", state.filterOnlyBudgeted)
        .putBoolean("preset_${name}_onlyOverBudget", state.filterOnlyOverBudget)
        .putString("preset_${name}_categories", state.selectedCategoryIds.joinToString(","))
        .putString("preset_${name}_accounts", state.selectedAccountIds.joinToString(","))
        .putString("preset_${name}_labels", state.selectedLabels.joinToString(","))
        .apply()
}

private fun loadSavedPresetNames(context: Context): List<String> {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return prefs.getStringSet("preset_keys", emptySet())?.toList()?.sorted() ?: emptyList()
}

private fun getSavedPreset(context: Context, name: String): BudgetFilterState? {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val datePresetName = prefs.getString("preset_${name}_datePreset", null) ?: return null
    val datePreset = try { BudgetDateRangePreset.valueOf(datePresetName) } catch (e: Exception) { BudgetDateRangePreset.THIS_MONTH }
    val excludeZero = prefs.getBoolean("preset_${name}_excludeZero", false)
    val displayCurr = prefs.getBoolean("preset_${name}_displayCurr", true)
    val displayCurrSym = prefs.getBoolean("preset_${name}_displayCurrSym", true)
    val showExpenseFirst = prefs.getBoolean("preset_${name}_showExpenseFirst", true)
    val sortByAmount = prefs.getBoolean("preset_${name}_sortByAmount", false)
    val comparisonEnabled = prefs.getBoolean("preset_${name}_comparisonEnabled", true)
    val onlyBudgeted = prefs.getBoolean("preset_${name}_onlyBudgeted", false)
    val onlyOverBudget = prefs.getBoolean("preset_${name}_onlyOverBudget", false)

    val catIdsStr = prefs.getString("preset_${name}_categories", "") ?: ""
    val catIds = catIdsStr.split(",").mapNotNull { it.toLongOrNull() }.toSet()

    val accIdsStr = prefs.getString("preset_${name}_accounts", "") ?: ""
    val accIds = accIdsStr.split(",").mapNotNull { it.toLongOrNull() }.toSet()

    val labelsStr = prefs.getString("preset_${name}_labels", "") ?: ""
    val labels = labelsStr.split(",").filter { it.isNotBlank() }.toSet()

    return BudgetFilterState(
        datePreset = datePreset,
        excludeZeroAmounts = excludeZero,
        displayCurrency = displayCurr,
        displayCurrencySymbol = displayCurrSym,
        showExpenseCategoriesFirst = showExpenseFirst,
        sortByAmount = sortByAmount,
        comparisonEnabled = comparisonEnabled,
        filterOnlyBudgeted = onlyBudgeted,
        filterOnlyOverBudget = onlyOverBudget,
        selectedCategoryIds = catIds,
        selectedAccountIds = accIds,
        selectedLabels = labels
    )
}

private fun deletePresetFromPrefs(context: Context, name: String) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val names = prefs.getStringSet("preset_keys", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
    names.remove(name)
    prefs.edit().putStringSet("preset_keys", names).remove("preset_${name}_datePreset").apply()
}
