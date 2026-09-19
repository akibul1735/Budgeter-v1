package com.example.ui.components

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import java.util.Calendar
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.mutableStateListOf
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
import com.example.ui.theme.SolidPrimary
import com.example.util.DateUtils
import com.example.util.IconHelper
import com.example.util.LanguageHelper
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

private val BrandGreen = Color(0xFF2E7D32)

/**
 * Budget Filter Popup styled identically to the Material Balance Sheet Filter Dialog.
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

    var showSavePresetDialog by remember { mutableStateOf(false) }
    var showOpenPresetDialog by remember { mutableStateOf(false) }

    var showCustomStartDatePicker by remember { mutableStateOf(false) }
    var showCustomEndDatePicker by remember { mutableStateOf(false) }
    var showCustomCompareStartDatePicker by remember { mutableStateOf(false) }
    var showCustomCompareEndDatePicker by remember { mutableStateOf(false) }

    val savedPresets = remember {
        mutableStateListOf<SavedBudgetFilterPreset>().apply {
            addAll(BudgetFilterPresetsStorage.loadPresets(context))
        }
    }

    val categoryDropdownItems = remember(categories, languageMode) {
        categories.filter { it.isActive }.map { cat ->
            val catName = if (languageMode == LanguageMode.BANGLA) cat.nameBn.ifBlank { cat.nameEn } else cat.nameEn
            DropdownItem(
                id = cat.id,
                title = catName,
                subtitle = if (cat.parentId != null) "Subcategory" else "Category",
                color = try {
                    if (cat.colorHex.isNotBlank()) Color(android.graphics.Color.parseColor(cat.colorHex)) else null
                } catch (e: Exception) {
                    null
                }
            )
        }
    }

    val accountDropdownItems = remember(accounts, languageMode) {
        accounts.map { acc ->
            val accName = if (languageMode == LanguageMode.BANGLA) acc.nameBn.ifBlank { acc.nameEn } else acc.nameEn
            DropdownItem(
                id = acc.id,
                title = accName,
                subtitle = acc.type.name.lowercase().replaceFirstChar { it.uppercase() }
            )
        }
    }

    val labelDropdownItems = remember(allLabels) {
        allLabels.map { lbl ->
            DropdownItem(
                id = lbl.hashCode().toLong(),
                title = lbl
            )
        }
    }

    val statusDropdownItems = remember(languageMode) {
        TransactionStatus.entries.map { st ->
            DropdownItem(
                id = st.ordinal.toLong(),
                title = if (languageMode == LanguageMode.BANGLA) st.titleBn else st.titleEn
            )
        }
    }

    val selectedLabelIds = remember(tempFilter.selectedLabels) {
        tempFilter.selectedLabels.map { it.hashCode().toLong() }.toSet()
    }
    val selectedStatusIds = remember(tempFilter.selectedStatusSet) {
        tempFilter.selectedStatusSet.map { it.ordinal.toLong() }.toSet()
    }

    val activeCount = remember(tempFilter) {
        var count = 0
        if (tempFilter.datePreset != BudgetDateRangePreset.LAST_12_MONTHS) count++
        if (tempFilter.selectedCategoryIds.isNotEmpty()) count++
        if (tempFilter.selectedAccountIds.isNotEmpty()) count++
        if (tempFilter.selectedStatusSet.isNotEmpty()) count++
        if (tempFilter.selectedLabels.isNotEmpty()) count++
        if (tempFilter.excludeZeroAmounts) count++
        if (tempFilter.showOnlyRemainingBalance) count++
        if (tempFilter.showOnlyActual) count++
        if (tempFilter.hideEmptyGroups) count++
        if (tempFilter.showOnlyCategoriesWithoutGroups) count++
        if (tempFilter.showExpenseCategoriesFirst) count++
        if (tempFilter.sortByAmount) count++
        if (tempFilter.comparisonEnabled) count++
        if (tempFilter.filterOnlyBudgeted) count++
        if (tempFilter.filterOnlyOverBudget) count++
        count
    }

    UnifiedFilterDialogContainer(
        onDismissRequest = onDismiss,
        testTag = "budget_filter_dialog"
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Unified Header
            UnifiedFilterHeader(
                title = if (languageMode == LanguageMode.BANGLA) "বাজেট ফিল্টার" else "Budget Filter",
                activeCount = activeCount,
                languageMode = languageMode,
                onReset = {
                    tempFilter = BudgetFilterState()
                    Toast.makeText(
                        context,
                        if (languageMode == LanguageMode.BANGLA) "ফিল্টার রিসেট করা হয়েছে" else "Filters reset to default",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                onSavePreset = { showSavePresetDialog = true },
                onOpenPreset = { showOpenPresetDialog = true },
                onDismiss = onDismiss
            )

            // Scrollable Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. Date Range
                UnifiedFilterSection(
                    icon = Icons.Default.DateRange,
                    title = if (languageMode == LanguageMode.BANGLA) "সময়কাল" else "Date Range"
                ) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        BudgetDateRangePreset.values().forEach { preset ->
                            val isSelected = tempFilter.datePreset == preset
                            FilterChip(
                                selected = isSelected,
                                onClick = { tempFilter = tempFilter.copy(datePreset = preset) },
                                label = {
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) preset.labelBn else preset.labelEn,
                                        fontSize = 11.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                } else null
                            )
                        }
                    }

                    // Custom Date Range
                    if (tempFilter.datePreset == BudgetDateRangePreset.CUSTOM) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "কাস্টম সময়কাল নির্ধারণ" else "Select Custom Date Range",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { showCustomStartDatePicker = true },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.EditCalendar, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = tempFilter.customStartDateMs?.let { DateUtils.formatDate(it, languageMode) } ?: "Start Date",
                                            fontSize = 11.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    OutlinedButton(
                                        onClick = { showCustomEndDatePicker = true },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.EditCalendar, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = tempFilter.customEndDateMs?.let { DateUtils.formatDate(it, languageMode) } ?: "End Date",
                                            fontSize = 11.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. Multi-Select Dropdowns: Categories, Accounts, Status, Labels
                if (categories.isNotEmpty()) {
                    UnifiedFilterSection(
                        icon = Icons.Default.Category,
                        title = if (languageMode == LanguageMode.BANGLA) "ক্যাটাগরি" else "Categories"
                    ) {
                        UnifiedMultiSelectDropdown(
                            label = if (languageMode == LanguageMode.BANGLA) "ক্যাটাগরি নির্বাচন করুন" else "Select Categories",
                            items = categoryDropdownItems,
                            selectedIds = tempFilter.selectedCategoryIds,
                            onSelectionChanged = { tempFilter = tempFilter.copy(selectedCategoryIds = it) },
                            placeholder = if (languageMode == LanguageMode.BANGLA) "(সকল ক্যাটাগরি)" else "(All Categories)",
                            leadingIcon = Icons.Default.Category,
                            languageMode = languageMode
                        )
                    }
                }

                if (accounts.isNotEmpty()) {
                    UnifiedFilterSection(
                        icon = Icons.Default.AccountBalanceWallet,
                        title = if (languageMode == LanguageMode.BANGLA) "অ্যাকাউন্ট" else "Accounts"
                    ) {
                        UnifiedMultiSelectDropdown(
                            label = if (languageMode == LanguageMode.BANGLA) "অ্যাকাউন্ট নির্বাচন করুন" else "Select Accounts",
                            items = accountDropdownItems,
                            selectedIds = tempFilter.selectedAccountIds,
                            onSelectionChanged = { tempFilter = tempFilter.copy(selectedAccountIds = it) },
                            placeholder = if (languageMode == LanguageMode.BANGLA) "(সকল অ্যাকাউন্ট)" else "(All Accounts)",
                            leadingIcon = Icons.Default.AccountBalanceWallet,
                            languageMode = languageMode
                        )
                    }
                }

                UnifiedFilterSection(
                    icon = Icons.Default.Check,
                    title = if (languageMode == LanguageMode.BANGLA) "স্ট্যাটাস" else "Status"
                ) {
                    UnifiedMultiSelectDropdown(
                        label = if (languageMode == LanguageMode.BANGLA) "স্ট্যাটাস নির্বাচন করুন" else "Select Statuses",
                        items = statusDropdownItems,
                        selectedIds = selectedStatusIds,
                        onSelectionChanged = { newIds ->
                            val selectedStatuses = TransactionStatus.entries.filter { newIds.contains(it.ordinal.toLong()) }.toSet()
                            tempFilter = tempFilter.copy(selectedStatusSet = selectedStatuses)
                        },
                        placeholder = if (languageMode == LanguageMode.BANGLA) "(সকল স্ট্যাটাস)" else "(All Statuses)",
                        leadingIcon = Icons.Default.Check,
                        languageMode = languageMode
                    )
                }

                if (allLabels.isNotEmpty()) {
                    UnifiedFilterSection(
                        icon = Icons.Default.Label,
                        title = if (languageMode == LanguageMode.BANGLA) "লেবেল / ট্যাগ" else "Labels / Tags"
                    ) {
                        UnifiedMultiSelectDropdown(
                            label = if (languageMode == LanguageMode.BANGLA) "লেবেল নির্বাচন করুন" else "Select Labels",
                            items = labelDropdownItems,
                            selectedIds = selectedLabelIds,
                            onSelectionChanged = { newIds ->
                                val selectedStrings = allLabels.filter { newIds.contains(it.hashCode().toLong()) }.toSet()
                                tempFilter = tempFilter.copy(selectedLabels = selectedStrings)
                            },
                            placeholder = if (languageMode == LanguageMode.BANGLA) "(সকল লেবেল)" else "(All Labels)",
                            leadingIcon = Icons.Default.Label,
                            languageMode = languageMode
                        )
                    }
                }

                // 3. Comparison Controls
                UnifiedFilterSection(
                    icon = Icons.Default.TrendingUp,
                    title = if (languageMode == LanguageMode.BANGLA) "তুলনামূলক বিশ্লেষণ" else "Comparison"
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "তুলনামূলক বিশ্লেষণ সক্রিয় করুন" else "Enable Comparison",
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 12.5.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "বেসলাইন সময়ের সাথে ব্যয়ের তুলনা প্রদর্শন করবে" else "Show expense comparison against baseline",
                                        fontSize = 10.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = tempFilter.comparisonEnabled,
                                    onCheckedChange = { tempFilter = tempFilter.copy(comparisonEnabled = it) }
                                )
                            }

                            if (tempFilter.comparisonEnabled) {
                                Spacer(modifier = Modifier.height(10.dp))
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    BudgetComparisonPreset.values().forEach { compPreset ->
                                        val isSelected = tempFilter.comparisonPreset == compPreset
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { tempFilter = tempFilter.copy(comparisonPreset = compPreset) },
                                            label = {
                                                Text(
                                                    text = if (languageMode == LanguageMode.BANGLA) compPreset.titleBn else compPreset.titleEn,
                                                    fontSize = 11.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                )
                                            },
                                            leadingIcon = if (isSelected) {
                                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(13.dp)) }
                                            } else null
                                        )
                                    }
                                }

                                if (tempFilter.comparisonPreset == BudgetComparisonPreset.CUSTOM) {
                                    val sdfShort = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                                    val defaultStart = tempFilter.customCompareStartMs ?: (System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000L)
                                    val defaultEnd = tempFilter.customCompareEndMs ?: System.currentTimeMillis()

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { showCustomCompareStartDatePicker = true },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text(
                                                text = sdfShort.format(Date(defaultStart)),
                                                fontSize = 11.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        OutlinedButton(
                                            onClick = { showCustomCompareEndDatePicker = true },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text(
                                                text = sdfShort.format(Date(defaultEnd)),
                                                fontSize = 11.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. Budget Filters & Display Options
                UnifiedFilterSection(
                    icon = Icons.Default.Tune,
                    title = if (languageMode == LanguageMode.BANGLA) "অন্যান্য ফিল্টার ও প্রদর্শন বিকল্প" else "Filters & Display Options"
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Exclude zero amounts
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "শূন্য পরিমাণ বাদ দিন" else "Exclude zero amounts",
                                    fontSize = 12.5.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Switch(
                                    checked = tempFilter.excludeZeroAmounts,
                                    onCheckedChange = { isChecked ->
                                        tempFilter = tempFilter.copy(
                                            excludeZeroAmounts = isChecked,
                                            hideEmptyGroups = if (isChecked) true else tempFilter.hideEmptyGroups
                                        )
                                    }
                                )
                            }

                            // Show only remaining balance
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "শুধুমাত্র অবশিষ্ট ব্যালেন্সের ক্যাটাগরি দেখান" else "Show only remaining balance category",
                                    fontSize = 12.5.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Switch(
                                    checked = tempFilter.showOnlyRemainingBalance,
                                    onCheckedChange = { tempFilter = tempFilter.copy(showOnlyRemainingBalance = it) }
                                )
                            }

                            // Show only actual
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "শুধুমাত্র প্রকৃত খরচ/আয় দেখান" else "Show only actual",
                                    fontSize = 12.5.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Switch(
                                    checked = tempFilter.showOnlyActual,
                                    onCheckedChange = { tempFilter = tempFilter.copy(showOnlyActual = it) }
                                )
                            }

                            // Hide empty groups
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "শূন্য বা ফাঁকা গ্রুপ লুকান" else "Hide empty groups",
                                    fontSize = 12.5.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Switch(
                                    checked = tempFilter.hideEmptyGroups,
                                    onCheckedChange = { tempFilter = tempFilter.copy(hideEmptyGroups = it) }
                                )
                            }

                            // Show only categories without groups
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "গ্রুপ ছাড়া শুধুমাত্র ক্যাটাগরি দেখান" else "Show only categories without groups",
                                    fontSize = 12.5.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Switch(
                                    checked = tempFilter.showOnlyCategoriesWithoutGroups,
                                    onCheckedChange = { tempFilter = tempFilter.copy(showOnlyCategoriesWithoutGroups = it) }
                                )
                            }

                            // Only budgeted categories
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "শুধুমাত্র বাজেট নির্ধারিত ক্যাটাগরি" else "Only Budgeted Categories",
                                    fontSize = 12.5.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Switch(
                                    checked = tempFilter.filterOnlyBudgeted,
                                    onCheckedChange = { tempFilter = tempFilter.copy(filterOnlyBudgeted = it) }
                                )
                            }

                            // Only over budget categories
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "শুধুমাত্র বাজেট অতিক্রান্ত ক্যাটাগরি" else "Only Over Budget Categories",
                                    fontSize = 12.5.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Switch(
                                    checked = tempFilter.filterOnlyOverBudget,
                                    onCheckedChange = { tempFilter = tempFilter.copy(filterOnlyOverBudget = it) }
                                )
                            }

                            // Display currency symbol
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "মুদ্রা প্রতীক প্রদর্শন করুন (৳)" else "Display Currency Symbol (৳)",
                                    fontSize = 12.5.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Switch(
                                    checked = tempFilter.displayCurrencySymbol,
                                    onCheckedChange = { tempFilter = tempFilter.copy(displayCurrencySymbol = it) }
                                )
                            }

                            // Show expense categories first
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "প্রথমে ব্যয়ের ক্যাটাগরি দেখান" else "Show expense categories first",
                                    fontSize = 12.5.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Switch(
                                    checked = tempFilter.showExpenseCategoriesFirst,
                                    onCheckedChange = { tempFilter = tempFilter.copy(showExpenseCategoriesFirst = it) }
                                )
                            }

                            // Sort by amount
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "পরিমাণ অনুযায়ী সাজান" else "Sort by amount",
                                    fontSize = 12.5.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Switch(
                                    checked = tempFilter.sortByAmount,
                                    onCheckedChange = { tempFilter = tempFilter.copy(sortByAmount = it) }
                                )
                            }
                        }
                    }
                }
            }

            // Unified Footer
            UnifiedFilterFooter(
                activeCount = activeCount,
                languageMode = languageMode,
                onReset = {
                    tempFilter = BudgetFilterState()
                },
                onDismiss = onDismiss,
                onApply = { onApply(tempFilter) }
            )
        }
    }

    // -------------------------------------------------------------------------
    // Save Preset Dialog
    // -------------------------------------------------------------------------
    if (showSavePresetDialog) {
        var presetNameInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showSavePresetDialog = false },
            title = {
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "ফিল্টার প্রিসেট সংরক্ষণ" else "Save Filter Preset",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "বর্তমান ফিল্টার সেটিংস ভবিষ্যতে দ্রুত ব্যবহারের জন্য একটি নামে সংরক্ষণ করুন:" else "Save current filter settings for quick access later:",
                        fontSize = 12.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = presetNameInput,
                        onValueChange = { presetNameInput = it },
                        label = { Text(if (languageMode == LanguageMode.BANGLA) "প্রিসেটের নাম" else "Preset Name") },
                        placeholder = { Text("e.g. Monthly Review") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (presetNameInput.isNotBlank()) {
                            val newPreset = SavedBudgetFilterPreset(
                                id = UUID.randomUUID().toString(),
                                name = presetNameInput.trim(),
                                filterState = tempFilter,
                                createdAtMs = System.currentTimeMillis()
                            )
                            savedPresets.add(newPreset)
                            BudgetFilterPresetsStorage.savePresets(context, savedPresets)
                            showSavePresetDialog = false
                            Toast.makeText(
                                context,
                                if (languageMode == LanguageMode.BANGLA) "প্রিসেট সংরক্ষিত হয়েছে" else "Preset '${presetNameInput.trim()}' saved",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGreen)
                ) {
                    Text(if (languageMode == LanguageMode.BANGLA) "সংরক্ষণ করুন" else "Save")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showSavePresetDialog = false }) {
                    Text(if (languageMode == LanguageMode.BANGLA) "বাতিল" else "Cancel")
                }
            }
        )
    }

    // -------------------------------------------------------------------------
    // SUB-DIALOG 6: Open Saved Presets Dialog
    // -------------------------------------------------------------------------
    if (showOpenPresetDialog) {
        AlertDialog(
            onDismissRequest = { showOpenPresetDialog = false },
            title = {
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "সংরক্ষিত ফিল্টারসমূহ" else "Saved Filter Presets",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Built-in presets
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "ডিফল্ট প্রিসেটসমূহ" else "Built-in Presets",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clickable {
                                tempFilter = BudgetFilterState()
                                showOpenPresetDialog = false
                                Toast.makeText(context, "Default Monthly Budget loaded", Toast.LENGTH_SHORT).show()
                            }
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)) {
                            Text("Default Monthly Budget", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text("This Month • All Categories & Accounts", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clickable {
                                tempFilter = BudgetFilterState(
                                    filterOnlyOverBudget = true,
                                    sortByAmount = true
                                )
                                showOpenPresetDialog = false
                                Toast.makeText(context, "Over Budget Watchlist loaded", Toast.LENGTH_SHORT).show()
                            }
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)) {
                            Text("Over Budget Watchlist", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text("Only Over-Budget Items • Sorted High to Low", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "কাস্টম প্রিসেটসমূহ" else "Your Saved Presets",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    if (savedPresets.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "কোন সংরক্ষিত কাস্টম প্রিসেট নেই" else "No custom presets saved yet",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                        ) {
                            items(savedPresets, key = { it.id }) { presetItem ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp)
                                        .clickable {
                                            tempFilter = presetItem.filterState
                                            showOpenPresetDialog = false
                                            Toast.makeText(
                                                context,
                                                if (languageMode == LanguageMode.BANGLA) "ফিল্টার প্রয়োগ করা হয়েছে" else "Loaded preset: ${presetItem.name}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 7.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = presetItem.name,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            val datePresetLabel = if (languageMode == LanguageMode.BANGLA) presetItem.filterState.datePreset.labelBn else presetItem.filterState.datePreset.labelEn
                                            Text(
                                                text = datePresetLabel,
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                savedPresets.remove(presetItem)
                                                BudgetFilterPresetsStorage.savePresets(context, savedPresets)
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                tint = SolidExpense,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showOpenPresetDialog = false }) {
                    Text(if (languageMode == LanguageMode.BANGLA) "বন্ধ করুন" else "Close")
                }
            }
        )
    }

    // -------------------------------------------------------------------------
    // DATE PICKERS FOR CUSTOM DATES
    // -------------------------------------------------------------------------
    if (showCustomStartDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = tempFilter.customStartDateMs ?: System.currentTimeMillis()
        )
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
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = tempFilter.customEndDateMs ?: System.currentTimeMillis()
        )
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

    if (showCustomCompareStartDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = tempFilter.customCompareStartMs ?: (System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000L)
        )
        DatePickerDialog(
            onDismissRequest = { showCustomCompareStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        tempFilter = tempFilter.copy(customCompareStartMs = it)
                    }
                    showCustomCompareStartDatePicker = false
                }) {
                    Text("OK", color = BrandGreen)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomCompareStartDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showCustomCompareEndDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = tempFilter.customCompareEndMs ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showCustomCompareEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        tempFilter = tempFilter.copy(customCompareEndMs = it)
                    }
                    showCustomCompareEndDatePicker = false
                }) {
                    Text("OK", color = BrandGreen)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomCompareEndDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

// -----------------------------------------------------------------------------
// SAVED PRESETS STORAGE
// -----------------------------------------------------------------------------

data class SavedBudgetFilterPreset(
    val id: String,
    val name: String,
    val filterState: BudgetFilterState,
    val createdAtMs: Long
)

object BudgetFilterPresetsStorage {
    private const val PREFS_KEY = "saved_budget_filter_presets_v2"
    private const val PREFS_NAME = "budget_filter_presets_storage"

    fun savePresets(context: Context, presets: List<SavedBudgetFilterPreset>) {
        try {
            val jsonArray = JSONArray()
            for (p in presets) {
                val obj = JSONObject().apply {
                    put("id", p.id)
                    put("name", p.name)
                    put("createdAtMs", p.createdAtMs)
                    put("datePreset", p.filterState.datePreset.name)
                    put("customStartDateMs", p.filterState.customStartDateMs ?: -1L)
                    put("customEndDateMs", p.filterState.customEndDateMs ?: -1L)
                    put("comparisonEnabled", p.filterState.comparisonEnabled)
                    put("comparisonPreset", p.filterState.comparisonPreset.name)
                    put("excludeZeroAmounts", p.filterState.excludeZeroAmounts)
                    put("hideEmptyGroups", p.filterState.hideEmptyGroups)
                    put("showOnlyCategoriesWithoutGroups", p.filterState.showOnlyCategoriesWithoutGroups)
                    put("displayCurrency", p.filterState.displayCurrency)
                    put("displayCurrencySymbol", p.filterState.displayCurrencySymbol)
                    put("showExpenseCategoriesFirst", p.filterState.showExpenseCategoriesFirst)
                    put("sortByAmount", p.filterState.sortByAmount)
                    put("filterOnlyBudgeted", p.filterState.filterOnlyBudgeted)
                    put("filterOnlyOverBudget", p.filterState.filterOnlyOverBudget)
                    put("categoryIds", JSONArray(p.filterState.selectedCategoryIds))
                    put("accountIds", JSONArray(p.filterState.selectedAccountIds))
                    put("labels", JSONArray(p.filterState.selectedLabels))
                    put("statuses", JSONArray(p.filterState.selectedStatusSet.map { it.name }))
                }
                jsonArray.put(obj)
            }
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(PREFS_KEY, jsonArray.toString())
                .apply()
        } catch (_: Exception) {}
    }

    fun loadPresets(context: Context): List<SavedBudgetFilterPreset> {
        return try {
            val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(PREFS_KEY, null) ?: return emptyList()
            val jsonArray = JSONArray(raw)
            val list = mutableListOf<SavedBudgetFilterPreset>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val id = obj.optString("id", UUID.randomUUID().toString())
                val name = obj.optString("name", "Preset")
                val createdAt = obj.optLong("createdAtMs", System.currentTimeMillis())

                val datePresetName = obj.optString("datePreset", BudgetDateRangePreset.THIS_MONTH.name)
                val datePreset = try { BudgetDateRangePreset.valueOf(datePresetName) } catch (_: Exception) { BudgetDateRangePreset.THIS_MONTH }

                val startMs = obj.optLong("customStartDateMs", -1L).takeIf { it != -1L }
                val endMs = obj.optLong("customEndDateMs", -1L).takeIf { it != -1L }

                val compEnabled = obj.optBoolean("comparisonEnabled", false)
                val compPresetName = obj.optString("comparisonPreset", BudgetComparisonPreset.LAST_MONTH.name)
                val compPreset = try { BudgetComparisonPreset.valueOf(compPresetName) } catch (_: Exception) { BudgetComparisonPreset.LAST_MONTH }

                val excludeZero = obj.optBoolean("excludeZeroAmounts", false)
                val hideEmpty = obj.optBoolean("hideEmptyGroups", true)
                val onlyWithoutGroups = obj.optBoolean("showOnlyCategoriesWithoutGroups", false)
                val dispCurr = obj.optBoolean("displayCurrency", true)
                val dispSym = obj.optBoolean("displayCurrencySymbol", true)
                val expFirst = obj.optBoolean("showExpenseCategoriesFirst", true)
                val sortAmt = obj.optBoolean("sortByAmount", false)
                val onlyBud = obj.optBoolean("filterOnlyBudgeted", false)
                val onlyOver = obj.optBoolean("filterOnlyOverBudget", false)

                val catIds = mutableSetOf<Long>()
                obj.optJSONArray("categoryIds")?.let { arr ->
                    for (j in 0 until arr.length()) catIds.add(arr.getLong(j))
                }

                val accIds = mutableSetOf<Long>()
                obj.optJSONArray("accountIds")?.let { arr ->
                    for (j in 0 until arr.length()) accIds.add(arr.getLong(j))
                }

                val labels = mutableSetOf<String>()
                obj.optJSONArray("labels")?.let { arr ->
                    for (j in 0 until arr.length()) labels.add(arr.getString(j))
                }

                val statuses = mutableSetOf<TransactionStatus>()
                obj.optJSONArray("statuses")?.let { arr ->
                    for (j in 0 until arr.length()) {
                        try { statuses.add(TransactionStatus.valueOf(arr.getString(j))) } catch (_: Exception) {}
                    }
                }

                list.add(
                    SavedBudgetFilterPreset(
                        id = id,
                        name = name,
                        filterState = BudgetFilterState(
                            datePreset = datePreset,
                            customStartDateMs = startMs,
                            customEndDateMs = endMs,
                            comparisonEnabled = compEnabled,
                            comparisonPreset = compPreset,
                            selectedCategoryIds = catIds,
                            selectedAccountIds = accIds,
                            selectedLabels = labels,
                            selectedStatusSet = statuses,
                            excludeZeroAmounts = excludeZero,
                            hideEmptyGroups = hideEmpty,
                            showOnlyCategoriesWithoutGroups = onlyWithoutGroups,
                            displayCurrency = dispCurr,
                            displayCurrencySymbol = dispSym,
                            showExpenseCategoriesFirst = expFirst,
                            sortByAmount = sortAmt,
                            filterOnlyBudgeted = onlyBud,
                            filterOnlyOverBudget = onlyOver
                        ),
                        createdAtMs = createdAt
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
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

                if (filterState.comparisonEnabled) {
                    FilterChipPill(
                        text = if (languageMode == LanguageMode.BANGLA) "তুলনা সক্রিয়" else "Comparison On",
                        onClear = { onFilterChange(filterState.copy(comparisonEnabled = false)) }
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
