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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
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

    // Dropdown / Sub-dialog visibility states
    var isDateRangeMenuExpanded by remember { mutableStateOf(false) }
    var showCategoryPickerDialog by remember { mutableStateOf(false) }
    var showAccountPickerDialog by remember { mutableStateOf(false) }
    var showStatusPickerDialog by remember { mutableStateOf(false) }
    var showLabelsPickerDialog by remember { mutableStateOf(false) }
    var showSavePresetDialog by remember { mutableStateOf(false) }
    var showOpenPresetDialog by remember { mutableStateOf(false) }

    // Date Pickers state for custom ranges
    var showCustomStartDatePicker by remember { mutableStateOf(false) }
    var showCustomEndDatePicker by remember { mutableStateOf(false) }

    val savedPresets = remember {
        mutableStateListOf<SavedBudgetFilterPreset>().apply {
            addAll(BudgetFilterPresetsStorage.loadPresets(context))
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f)
                .testTag("budget_filter_dialog")
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Top drag handle & 3 Circular Action Icons
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top drag handle
                    Box(
                        modifier = Modifier
                            .width(36.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FilterAlt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (languageMode == LanguageMode.BANGLA) "বাজেট ফিল্টার" else "Budget Filter",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // 3 Circular Action Icons: Reset, Save, Open
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 1. Reset Icon
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                onClick = {
                                    tempFilter = BudgetFilterState()
                                    Toast.makeText(
                                        context,
                                        if (languageMode == LanguageMode.BANGLA) "ফিল্টার রিসেট করা হয়েছে" else "Filters reset to default",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.RestartAlt,
                                        contentDescription = "Reset",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            // 2. Save Preset Icon
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                onClick = { showSavePresetDialog = true },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.BookmarkAdd,
                                        contentDescription = "Save Preset",
                                        tint = SolidIncome,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            // 3. Open Saved Presets Icon
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                onClick = { showOpenPresetDialog = true },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.FolderOpen,
                                        contentDescription = "Open Saved Filters",
                                        tint = SolidPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                // Scrollable Content Area
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. Date Range Dropdown
                    Column {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "সময়কাল (Date Range)" else "Date Range",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Box(modifier = Modifier.fillMaxWidth()) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                onClick = { isDateRangeMenuExpanded = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CalendarMonth,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        val presetLabel = when (tempFilter.datePreset) {
                                            BudgetDateRangePreset.THIS_MONTH -> if (languageMode == LanguageMode.BANGLA) "চলতি মাস" else "This Month"
                                            BudgetDateRangePreset.LAST_MONTH -> if (languageMode == LanguageMode.BANGLA) "গত মাস" else "Last Month"
                                            BudgetDateRangePreset.LAST_3_MONTHS -> if (languageMode == LanguageMode.BANGLA) "গত ৩ মাস" else "Last 3 Months"
                                            BudgetDateRangePreset.LAST_6_MONTHS -> if (languageMode == LanguageMode.BANGLA) "গত ৬ মাস" else "Last 6 Months"
                                            BudgetDateRangePreset.YEAR_TO_DATE -> if (languageMode == LanguageMode.BANGLA) "বছরের শুরু থেকে" else "Year to Date"
                                            BudgetDateRangePreset.SAME_MONTH_LAST_YEAR -> if (languageMode == LanguageMode.BANGLA) "গত বছরের এই মাস" else "Same Month Last Year"
                                            BudgetDateRangePreset.ALL_TIME -> if (languageMode == LanguageMode.BANGLA) "সব সময়" else "All Time"
                                            BudgetDateRangePreset.CUSTOM -> if (languageMode == LanguageMode.BANGLA) "নির্দিষ্ট সময়সীমা" else "Custom Range"
                                        }
                                        Text(
                                            text = presetLabel,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = isDateRangeMenuExpanded,
                                onDismissRequest = { isDateRangeMenuExpanded = false }
                            ) {
                                BudgetDateRangePreset.values().forEach { preset ->
                                    val isSelected = tempFilter.datePreset == preset
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = if (languageMode == LanguageMode.BANGLA) preset.labelBn else preset.labelEn,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                                )
                                                if (isSelected) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        },
                                        onClick = {
                                            isDateRangeMenuExpanded = false
                                            tempFilter = tempFilter.copy(datePreset = preset)
                                        }
                                    )
                                }
                            }
                        }

                        // If CUSTOM preset selected, show Custom Range configuration card
                        if (tempFilter.datePreset == BudgetDateRangePreset.CUSTOM) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "নির্দিষ্ট শুরুর ও শেষ তারিখ:" else "Select Custom Date Bounds:",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    // 1. Start Date Row
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { showCustomStartDatePicker = true }
                                            .background(MaterialTheme.colorScheme.surface)
                                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = if (languageMode == LanguageMode.BANGLA) "১. শুরুর তারিখ" else "1. Start Date",
                                                fontSize = 10.5.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            val startLabel = tempFilter.customStartDateMs?.let { DateUtils.formatDate(it, languageMode) }
                                                ?: (if (languageMode == LanguageMode.BANGLA) "তারিখ নির্বাচন করুন" else "Pick Date")
                                            Text(
                                                text = startLabel,
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.Default.EditCalendar,
                                            contentDescription = "Change Start Date",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // 2. End Date Row
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { showCustomEndDatePicker = true }
                                            .background(MaterialTheme.colorScheme.surface)
                                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = if (languageMode == LanguageMode.BANGLA) "২. শেষ তারিখ" else "2. End Date",
                                                fontSize = 10.5.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            val endLabel = tempFilter.customEndDateMs?.let { DateUtils.formatDate(it, languageMode) }
                                                ?: (if (languageMode == LanguageMode.BANGLA) "তারিখ নির্বাচন করুন" else "Pick Date")
                                            Text(
                                                text = endLabel,
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.Default.EditCalendar,
                                            contentDescription = "Change End Date",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 2. Category Dropdown
                    Column {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "ক্যাটাগরি (Category)" else "Category",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            onClick = { showCategoryPickerDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FolderOpen,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    val catLabel = if (tempFilter.selectedCategoryIds.isEmpty()) {
                                        if (languageMode == LanguageMode.BANGLA) "সকল ক্যাটাগরি (All Categories)" else "All Categories"
                                    } else {
                                        if (languageMode == LanguageMode.BANGLA) "${tempFilter.selectedCategoryIds.size} টি ক্যাটাগরি নির্বাচিত" else "${tempFilter.selectedCategoryIds.size} Categories Selected"
                                    }
                                    Text(
                                        text = catLabel,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                        modifier = Modifier.padding(end = 4.dp)
                                    ) {
                                        Text(
                                            text = if (languageMode == LanguageMode.BANGLA) "ফিল্টার" else "Filter",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    // 3. Account Dropdown
                    Column {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "অ্যাকাউন্ট (Account)" else "Account",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            onClick = { showAccountPickerDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalanceWallet,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    val accLabel = if (tempFilter.selectedAccountIds.isEmpty()) {
                                        if (languageMode == LanguageMode.BANGLA) "সব অ্যাকাউন্ট (All Accounts)" else "All Accounts"
                                    } else {
                                        if (languageMode == LanguageMode.BANGLA) "${tempFilter.selectedAccountIds.size} টি অ্যাকাউন্ট নির্বাচিত" else "${tempFilter.selectedAccountIds.size} Accounts Selected"
                                    }
                                    Text(
                                        text = accLabel,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                        modifier = Modifier.padding(end = 4.dp)
                                    ) {
                                        Text(
                                            text = if (languageMode == LanguageMode.BANGLA) "ফিল্টার" else "Filter",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    // 4. Status Dropdown
                    Column {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "স্ট্যাটাস (Status)" else "Status",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            onClick = { showStatusPickerDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    val statusLabel = if (tempFilter.selectedStatusSet.isEmpty()) {
                                        if (languageMode == LanguageMode.BANGLA) "(কোন ফিল্টার নেই)" else "(No Filter)"
                                    } else {
                                        tempFilter.selectedStatusSet.joinToString(", ") {
                                            if (languageMode == LanguageMode.BANGLA) it.titleBn else it.titleEn
                                        }
                                    }
                                    Text(
                                        text = statusLabel,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                        modifier = Modifier.padding(end = 4.dp)
                                    ) {
                                        Text(
                                            text = if (languageMode == LanguageMode.BANGLA) "ফিল্টার" else "Filter",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    // 5. Labels Dropdown
                    Column {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "লেবেল (Labels)" else "Labels",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            onClick = { showLabelsPickerDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Label,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    val labelsLabel = if (tempFilter.selectedLabels.isEmpty()) {
                                        if (languageMode == LanguageMode.BANGLA) "(কোন ফিল্টার নেই)" else "(No Filter)"
                                    } else {
                                        tempFilter.selectedLabels.joinToString(", ")
                                    }
                                    Text(
                                        text = labelsLabel,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                        modifier = Modifier.padding(end = 4.dp)
                                    ) {
                                        Text(
                                            text = if (languageMode == LanguageMode.BANGLA) "ফিল্টার" else "Filter",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    // 6. Switches in Card (matching Balance Sheet filter layout)
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            // Row 1: Exclude zero amounts
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "শূন্য পরিমাণ বাদ দিন" else "Exclude zero amounts",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Switch(
                                    checked = tempFilter.excludeZeroAmounts,
                                    onCheckedChange = { tempFilter = tempFilter.copy(excludeZeroAmounts = it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = BrandGreen
                                    )
                                )
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

                            // Row 2: Display currency
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "কারেন্সি প্রদর্শন করুন" else "Display currency",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Switch(
                                    checked = tempFilter.displayCurrency,
                                    onCheckedChange = { tempFilter = tempFilter.copy(displayCurrency = it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = BrandGreen
                                    )
                                )
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

                            // Row 3: Display currency symbol
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "মুদ্রা প্রতীক প্রদর্শন করুন" else "Display Currency Symbol",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Switch(
                                    checked = tempFilter.displayCurrencySymbol,
                                    onCheckedChange = { tempFilter = tempFilter.copy(displayCurrencySymbol = it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = BrandGreen
                                    )
                                )
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

                            // Row 4: Show expense categories first
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "প্রথমে ব্যয়ের ক্যাটাগরি দেখান" else "Show expense categories first",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Switch(
                                    checked = tempFilter.showExpenseCategoriesFirst,
                                    onCheckedChange = { tempFilter = tempFilter.copy(showExpenseCategoriesFirst = it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = BrandGreen
                                    )
                                )
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

                            // Row 5: Sort by amount
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "পরিমাণ অনুযায়ী সাজান" else "Sort by amount",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Switch(
                                    checked = tempFilter.sortByAmount,
                                    onCheckedChange = { tempFilter = tempFilter.copy(sortByAmount = it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = BrandGreen
                                    )
                                )
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

                            // Row 6: Comparison
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "তুলনামূলক বিশ্লেষণ" else "Comparison",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "বেসলাইন সময়ের সাথে ব্যয়ের তুলনা প্রদর্শন করবে" else "Show expense comparison against baseline",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                                    )
                                }
                                Switch(
                                    checked = tempFilter.comparisonEnabled,
                                    onCheckedChange = { tempFilter = tempFilter.copy(comparisonEnabled = it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = BrandGreen
                                    )
                                )
                            }

                            // Comparison Baseline Chips if enabled
                            AnimatedVisibility(
                                visible = tempFilter.comparisonEnabled,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 6.dp, bottom = 4.dp)
                                ) {
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "তুলনার বেসলাইন:" else "Comparison Baseline:",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
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

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

                            // Row 7: Only budgeted categories
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "শুধুমাত্র বাজেট নির্ধারিত ক্যাটাগরি" else "Only Budgeted Categories",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Switch(
                                    checked = tempFilter.filterOnlyBudgeted,
                                    onCheckedChange = { tempFilter = tempFilter.copy(filterOnlyBudgeted = it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = BrandGreen
                                    )
                                )
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

                            // Row 8: Only over budget categories
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "শুধুমাত্র বাজেট অতিক্রান্ত ক্যাটাগরি" else "Only Over Budget Categories",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Switch(
                                    checked = tempFilter.filterOnlyOverBudget,
                                    onCheckedChange = { tempFilter = tempFilter.copy(filterOnlyOverBudget = it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = BrandGreen
                                    )
                                )
                            }
                        }
                    }
                }

                // Fixed Bottom Action Buttons: Cancel (outlined) and OK (green filled)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("budget_filter_cancel_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "বাতিল" else "Cancel",
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = { onApply(tempFilter) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandGreen,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("budget_filter_apply_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "ঠিক আছে" else "OK",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // SUB-DIALOG 1: Category Multi-Select Picker Dialog
    // -------------------------------------------------------------------------
    if (showCategoryPickerDialog) {
        var searchQuery by remember { mutableStateOf("") }
        var selectedTypeTab by remember { mutableStateOf<CategoryType?>(null) }
        val tempSelected = remember { mutableStateListOf<Long>().apply { addAll(tempFilter.selectedCategoryIds) } }

        val filteredCategories = remember(categories, searchQuery, selectedTypeTab) {
            categories.filter { cat ->
                cat.isActive &&
                        (selectedTypeTab == null || cat.type == selectedTypeTab) &&
                        (searchQuery.isBlank() ||
                                cat.nameEn.contains(searchQuery, ignoreCase = true) ||
                                cat.nameBn.contains(searchQuery, ignoreCase = true))
            }
        }

        AlertDialog(
            onDismissRequest = { showCategoryPickerDialog = false },
            title = {
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "ক্যাটাগরি নির্বাচন করুন" else "Select Categories",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text(if (languageMode == LanguageMode.BANGLA) "অনুসন্ধান..." else "Search categories...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(6.dp))

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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = {
                            tempSelected.clear()
                            tempSelected.addAll(categories.map { it.id })
                        }) {
                            Text(if (languageMode == LanguageMode.BANGLA) "সব নির্বাচন" else "Select All", fontSize = 11.sp)
                        }
                        TextButton(onClick = {
                            tempSelected.clear()
                        }) {
                            Text(if (languageMode == LanguageMode.BANGLA) "মুছে ফেলুন" else "Clear All", fontSize = 11.sp)
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                    ) {
                        items(filteredCategories, key = { it.id }) { cat ->
                            val isChecked = tempSelected.contains(cat.id)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isChecked) tempSelected.remove(cat.id) else tempSelected.add(cat.id)
                                    }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { checked ->
                                        if (checked == true) tempSelected.add(cat.id) else tempSelected.remove(cat.id)
                                    },
                                    colors = CheckboxDefaults.colors(checkedColor = BrandGreen)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                val iconVector = IconHelper.getIconByName(cat.iconName)
                                Icon(
                                    imageVector = iconVector,
                                    contentDescription = null,
                                    tint = if (cat.type == CategoryType.EXPENSE) SolidExpense else SolidIncome,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = LanguageHelper.getLocalizedName(cat.nameEn, cat.nameBn, languageMode),
                                    fontSize = 13.sp,
                                    fontWeight = if (isChecked) FontWeight.SemiBold else FontWeight.Normal,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        tempFilter = tempFilter.copy(selectedCategoryIds = tempSelected.toSet())
                        showCategoryPickerDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGreen)
                ) {
                    Text(if (languageMode == LanguageMode.BANGLA) "প্রয়োগ করুন" else "Apply")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showCategoryPickerDialog = false }) {
                    Text(if (languageMode == LanguageMode.BANGLA) "বাতিল" else "Cancel")
                }
            }
        )
    }

    // -------------------------------------------------------------------------
    // SUB-DIALOG 2: Account Multi-Select Picker Dialog
    // -------------------------------------------------------------------------
    if (showAccountPickerDialog) {
        var searchQuery by remember { mutableStateOf("") }
        val tempSelected = remember { mutableStateListOf<Long>().apply { addAll(tempFilter.selectedAccountIds) } }

        val filteredAccounts = remember(accounts, searchQuery) {
            accounts.filter { acc ->
                searchQuery.isBlank() ||
                        acc.nameEn.contains(searchQuery, ignoreCase = true) ||
                        acc.nameBn.contains(searchQuery, ignoreCase = true)
            }
        }

        AlertDialog(
            onDismissRequest = { showAccountPickerDialog = false },
            title = {
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "অ্যাকাউন্ট নির্বাচন করুন" else "Select Accounts",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text(if (languageMode == LanguageMode.BANGLA) "অনুসন্ধান..." else "Search accounts...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = {
                            tempSelected.clear()
                            tempSelected.addAll(accounts.map { it.id })
                        }) {
                            Text(if (languageMode == LanguageMode.BANGLA) "সব নির্বাচন" else "Select All", fontSize = 11.sp)
                        }
                        TextButton(onClick = {
                            tempSelected.clear()
                        }) {
                            Text(if (languageMode == LanguageMode.BANGLA) "মুছে ফেলুন" else "Clear All", fontSize = 11.sp)
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                    ) {
                        items(filteredAccounts, key = { it.id }) { acc ->
                            val isChecked = tempSelected.contains(acc.id)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isChecked) tempSelected.remove(acc.id) else tempSelected.add(acc.id)
                                    }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { checked ->
                                        if (checked == true) tempSelected.add(acc.id) else tempSelected.remove(acc.id)
                                    },
                                    colors = CheckboxDefaults.colors(checkedColor = BrandGreen)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                val iconVector = IconHelper.getIconByName(acc.iconName)
                                Icon(
                                    imageVector = iconVector,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = acc.localizedName(languageMode),
                                    fontSize = 13.sp,
                                    fontWeight = if (isChecked) FontWeight.SemiBold else FontWeight.Normal,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        tempFilter = tempFilter.copy(selectedAccountIds = tempSelected.toSet())
                        showAccountPickerDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGreen)
                ) {
                    Text(if (languageMode == LanguageMode.BANGLA) "প্রয়োগ করুন" else "Apply")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAccountPickerDialog = false }) {
                    Text(if (languageMode == LanguageMode.BANGLA) "বাতিল" else "Cancel")
                }
            }
        )
    }

    // -------------------------------------------------------------------------
    // SUB-DIALOG 3: Status Multi-Select Picker Dialog
    // -------------------------------------------------------------------------
    if (showStatusPickerDialog) {
        val tempSelected = remember { mutableStateListOf<TransactionStatus>().apply { addAll(tempFilter.selectedStatusSet) } }

        AlertDialog(
            onDismissRequest = { showStatusPickerDialog = false },
            title = {
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "স্ট্যাটাস নির্বাচন করুন" else "Select Transaction Status",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = {
                            tempSelected.clear()
                            tempSelected.addAll(TransactionStatus.values())
                        }) {
                            Text(if (languageMode == LanguageMode.BANGLA) "সব নির্বাচন" else "Select All", fontSize = 11.sp)
                        }
                        TextButton(onClick = {
                            tempSelected.clear()
                        }) {
                            Text(if (languageMode == LanguageMode.BANGLA) "মুছে ফেলুন" else "Clear All", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    TransactionStatus.values().forEach { status ->
                        val isChecked = tempSelected.contains(status)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isChecked) tempSelected.remove(status) else tempSelected.add(status)
                                }
                                .padding(vertical = 6.dp),
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
                                fontSize = 13.5.sp,
                                fontWeight = if (isChecked) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        tempFilter = tempFilter.copy(selectedStatusSet = tempSelected.toSet())
                        showStatusPickerDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGreen)
                ) {
                    Text(if (languageMode == LanguageMode.BANGLA) "প্রয়োগ করুন" else "Apply")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showStatusPickerDialog = false }) {
                    Text(if (languageMode == LanguageMode.BANGLA) "বাতিল" else "Cancel")
                }
            }
        )
    }

    // -------------------------------------------------------------------------
    // SUB-DIALOG 4: Labels Multi-Select Picker Dialog
    // -------------------------------------------------------------------------
    if (showLabelsPickerDialog) {
        var searchQuery by remember { mutableStateOf("") }
        var newCustomLabel by remember { mutableStateOf("") }
        val tempSelected = remember { mutableStateListOf<String>().apply { addAll(tempFilter.selectedLabels) } }
        val labelList = remember(allLabels) { allLabels.toMutableList() }

        val filteredLabels = remember(labelList, searchQuery) {
            labelList.filter {
                searchQuery.isBlank() || it.contains(searchQuery, ignoreCase = true)
            }
        }

        AlertDialog(
            onDismissRequest = { showLabelsPickerDialog = false },
            title = {
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "লেবেল নির্বাচন করুন" else "Select Labels",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text(if (languageMode == LanguageMode.BANGLA) "অনুসন্ধান..." else "Search labels...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newCustomLabel,
                            onValueChange = { newCustomLabel = it },
                            placeholder = { Text("Add new label...", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = {
                                val trimmed = newCustomLabel.trim()
                                if (trimmed.isNotEmpty() && !labelList.contains(trimmed)) {
                                    labelList.add(trimmed)
                                    tempSelected.add(trimmed)
                                    newCustomLabel = ""
                                }
                            }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Label", tint = BrandGreen)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = {
                            tempSelected.clear()
                            tempSelected.addAll(labelList)
                        }) {
                            Text(if (languageMode == LanguageMode.BANGLA) "সব নির্বাচন" else "Select All", fontSize = 11.sp)
                        }
                        TextButton(onClick = {
                            tempSelected.clear()
                        }) {
                            Text(if (languageMode == LanguageMode.BANGLA) "মুছে ফেলুন" else "Clear All", fontSize = 11.sp)
                        }
                    }

                    if (filteredLabels.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No labels available",
                                fontSize = 12.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        ) {
                            items(filteredLabels, key = { it }) { label ->
                                val isChecked = tempSelected.contains(label)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (isChecked) tempSelected.remove(label) else tempSelected.add(label)
                                        }
                                        .padding(vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { checked ->
                                            if (checked == true) tempSelected.add(label) else tempSelected.remove(label)
                                        },
                                        colors = CheckboxDefaults.colors(checkedColor = BrandGreen)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = label,
                                        fontSize = 13.sp,
                                        fontWeight = if (isChecked) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        tempFilter = tempFilter.copy(selectedLabels = tempSelected.toSet())
                        showLabelsPickerDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGreen)
                ) {
                    Text(if (languageMode == LanguageMode.BANGLA) "প্রয়োগ করুন" else "Apply")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showLabelsPickerDialog = false }) {
                    Text(if (languageMode == LanguageMode.BANGLA) "বাতিল" else "Cancel")
                }
            }
        )
    }

    // -------------------------------------------------------------------------
    // SUB-DIALOG 5: Save Preset Dialog
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
