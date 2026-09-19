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
import androidx.compose.material.icons.filled.TrendingDown
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
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

private val BrandGreen = Color(0xFF2E7D32)

data class SavedNetEarningsFilterPreset(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val filterState: NetEarningsFilterState,
    val createdAtMs: Long = System.currentTimeMillis()
)

object NetEarningsFilterPresetsStorage {
    private const val PREFS_NAME = "net_earnings_filter_prefs"
    private const val KEY_PRESETS = "net_earnings_filter_presets"

    fun savePresets(context: Context, presets: List<SavedNetEarningsFilterPreset>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonArray = JSONArray()
        for (p in presets) {
            val obj = JSONObject().apply {
                put("id", p.id)
                put("name", p.name)
                put("datePreset", p.filterState.datePreset.name)
                put("customStartDateMs", p.filterState.customStartDateMs ?: -1L)
                put("customEndDateMs", p.filterState.customEndDateMs ?: -1L)
                put("comparisonEnabled", p.filterState.comparisonEnabled)
                put("comparisonPreset", p.filterState.comparisonPreset.name)
                put("flowScope", p.filterState.flowScope.name)
                put("includeTransfers", p.filterState.includeTransfers)
                put("categoryIds", JSONArray(p.filterState.selectedCategoryIds))
                put("accountIds", JSONArray(p.filterState.selectedAccountIds))
                put("labels", JSONArray(p.filterState.selectedLabels))
                put("statusList", JSONArray(p.filterState.selectedStatusSet.map { it.name }))
                put("excludeZeroAmounts", p.filterState.excludeZeroAmounts)
                put("hideEmptyGroups", p.filterState.hideEmptyGroups)
                put("showOnlyCategoriesWithoutGroups", p.filterState.showOnlyCategoriesWithoutGroups)
                put("displayCurrency", p.filterState.displayCurrency)
                put("displayCurrencySymbol", p.filterState.displayCurrencySymbol)
                put("sortOrder", p.filterState.sortOrder.name)
                put("createdAt", p.createdAtMs)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString(KEY_PRESETS, jsonArray.toString()).apply()
    }

    fun loadPresets(context: Context): List<SavedNetEarningsFilterPreset> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonStr = prefs.getString(KEY_PRESETS, null) ?: return emptyList()
        return try {
            val jsonArray = JSONArray(jsonStr)
            val list = mutableListOf<SavedNetEarningsFilterPreset>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val id = obj.optString("id", UUID.randomUUID().toString())
                val name = obj.optString("name", "Preset $i")
                val datePreset = runCatching { BudgetDateRangePreset.valueOf(obj.getString("datePreset")) }.getOrDefault(BudgetDateRangePreset.THIS_MONTH)
                val sMs = obj.optLong("customStartDateMs", -1L).takeIf { it != -1L }
                val eMs = obj.optLong("customEndDateMs", -1L).takeIf { it != -1L }
                val comp = obj.optBoolean("comparisonEnabled", false)
                val compPreset = runCatching { BudgetComparisonPreset.valueOf(obj.getString("comparisonPreset")) }.getOrDefault(BudgetComparisonPreset.LAST_MONTH)
                val flowScope = runCatching { NetEarningsFlowScope.valueOf(obj.getString("flowScope")) }.getOrDefault(NetEarningsFlowScope.ALL)
                val includeTransfers = obj.optBoolean("includeTransfers", false)

                val catArr = obj.optJSONArray("categoryIds") ?: JSONArray()
                val catSet = mutableSetOf<Long>()
                for (c in 0 until catArr.length()) catSet.add(catArr.getLong(c))

                val accArr = obj.optJSONArray("accountIds") ?: JSONArray()
                val accSet = mutableSetOf<Long>()
                for (a in 0 until accArr.length()) accSet.add(accArr.getLong(a))

                val lblArr = obj.optJSONArray("labels") ?: JSONArray()
                val lblSet = mutableSetOf<String>()
                for (l in 0 until lblArr.length()) lblSet.add(lblArr.getString(l))

                val stArr = obj.optJSONArray("statusList") ?: JSONArray()
                val stSet = mutableSetOf<TransactionStatus>()
                for (s in 0 until stArr.length()) {
                    runCatching { TransactionStatus.valueOf(stArr.getString(s)) }.getOrNull()?.let { stSet.add(it) }
                }

                val exclZero = obj.optBoolean("excludeZeroAmounts", true)
                val hideEmpty = obj.optBoolean("hideEmptyGroups", true)
                val onlyWithoutGroups = obj.optBoolean("showOnlyCategoriesWithoutGroups", false)
                val dispCurr = obj.optBoolean("displayCurrency", true)
                val dispSym = obj.optBoolean("displayCurrencySymbol", true)
                val sortOrder = runCatching { NetEarningsSortOrder.valueOf(obj.getString("sortOrder")) }.getOrDefault(NetEarningsSortOrder.NET_DESC)
                val createdAt = obj.optLong("createdAt", System.currentTimeMillis())

                list.add(
                    SavedNetEarningsFilterPreset(
                        id = id,
                        name = name,
                        filterState = NetEarningsFilterState(
                            datePreset = datePreset,
                            customStartDateMs = sMs,
                            customEndDateMs = eMs,
                            comparisonEnabled = comp,
                            comparisonPreset = compPreset,
                            flowScope = flowScope,
                            includeTransfers = includeTransfers,
                            selectedCategoryIds = catSet,
                            selectedAccountIds = accSet,
                            selectedLabels = lblSet,
                            selectedStatusSet = stSet,
                            excludeZeroAmounts = exclZero,
                            hideEmptyGroups = hideEmpty,
                            showOnlyCategoriesWithoutGroups = onlyWithoutGroups,
                            displayCurrency = dispCurr,
                            displayCurrencySymbol = dispSym,
                            sortOrder = sortOrder
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

/**
 * Net Earnings Filter Dialog with specialized cash-flow, net result, and comparison options.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NetEarningsFilterDialog(
    currentFilter: NetEarningsFilterState,
    categories: List<Category>,
    accounts: List<Account>,
    allLabels: List<String> = emptyList(),
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onApply: (NetEarningsFilterState) -> Unit
) {
    val context = LocalContext.current
    var tempFilter by remember { mutableStateOf(currentFilter) }

    var showSavePresetDialog by remember { mutableStateOf(false) }
    var showOpenPresetDialog by remember { mutableStateOf(false) }

    var showCustomStartDatePicker by remember { mutableStateOf(false) }
    var showCustomEndDatePicker by remember { mutableStateOf(false) }

    val savedPresets = remember {
        mutableStateListOf<SavedNetEarningsFilterPreset>().apply {
            addAll(NetEarningsFilterPresetsStorage.loadPresets(context))
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

    // Convert Set<String> for labels and Set<TransactionStatus> for status to ID sets
    val selectedLabelIds = remember(tempFilter.selectedLabels) {
        tempFilter.selectedLabels.map { it.hashCode().toLong() }.toSet()
    }
    val selectedStatusIds = remember(tempFilter.selectedStatusSet) {
        tempFilter.selectedStatusSet.map { it.ordinal.toLong() }.toSet()
    }

    UnifiedFilterDialogContainer(
        onDismissRequest = onDismiss,
        testTag = "net_earnings_filter_dialog"
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Unified Header
            UnifiedFilterHeader(
                title = if (languageMode == LanguageMode.BANGLA) "নিট আয় ফিল্টার" else "Net Earnings Filter",
                activeCount = tempFilter.activeFilterCount,
                languageMode = languageMode,
                onReset = {
                    tempFilter = NetEarningsFilterState()
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

                // 2. Cash Flow & Surplus Scope
                UnifiedFilterSection(
                    icon = Icons.Default.Tune,
                    title = if (languageMode == LanguageMode.BANGLA) "আর্থিক প্রবাহ ও উদ্বৃত্ত" else "Cash Flow Scope"
                ) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        NetEarningsFlowScope.values().forEach { scope ->
                            val isSelected = tempFilter.flowScope == scope
                            FilterChip(
                                selected = isSelected,
                                onClick = { tempFilter = tempFilter.copy(flowScope = scope) },
                                label = {
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) scope.labelBn else scope.labelEn,
                                        fontSize = 11.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(13.dp)) }
                                } else null
                            )
                        }
                    }

                    // Include Transfers Toggle
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "ট্রান্সফার লেনদেন অন্তর্ভুক্ত করুন" else "Include Account Transfers",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "হিসাবসমূহের মধ্যকার লেনদেন ক্যাশ ফ্লোতে গণ্য করুন" else "Include internal transfers in cash flow",
                                    fontSize = 10.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = tempFilter.includeTransfers,
                                onCheckedChange = { tempFilter = tempFilter.copy(includeTransfers = it) }
                            )
                        }
                    }
                }

                // 3. Comparison Period Controls
                UnifiedFilterSection(
                    icon = Icons.Default.TrendingUp,
                    title = if (languageMode == LanguageMode.BANGLA) "সময়ের সাথে তুলনা" else "Period Comparison"
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
                                        text = if (languageMode == LanguageMode.BANGLA) "তুলনামূলক বিশ্লেষণ সক্রিয় করুন" else "Enable Period Comparison",
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 12.5.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) "পূর্ববর্তী মাস বা বছরের সাথে নিট আয়ের তুলনা" else "Compare Net Earnings with previous baseline",
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
                            }
                        }
                    }
                }

                // 4. Multi-Select Dropdowns: Categories, Accounts, Labels, Status
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

                UnifiedFilterSection(
                    icon = Icons.Default.Check,
                    title = if (languageMode == LanguageMode.BANGLA) "লেনদেনের স্ট্যাটাস" else "Transaction Status"
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

                // 5. Net Earnings Sorting Options
                UnifiedFilterSection(
                    icon = Icons.Default.Sort,
                    title = if (languageMode == LanguageMode.BANGLA) "সাজানোর ক্রম" else "Sort Order"
                ) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        NetEarningsSortOrder.values().forEach { order ->
                            val isSelected = tempFilter.sortOrder == order
                            FilterChip(
                                selected = isSelected,
                                onClick = { tempFilter = tempFilter.copy(sortOrder = order) },
                                label = {
                                    Text(
                                        text = if (languageMode == LanguageMode.BANGLA) order.titleBn else order.titleEn,
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
                }

                // 6. Presentation & Exclusion Toggles
                UnifiedFilterSection(
                    icon = Icons.Default.Tune,
                    title = if (languageMode == LanguageMode.BANGLA) "অন্যান্য বিকল্প" else "Other Options"
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
                            // Exclude Zero Amounts
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "শূন্য পরিমাণ বাদ দিন" else "Exclude Zero Amounts",
                                    fontSize = 12.5.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Switch(
                                    checked = tempFilter.excludeZeroAmounts,
                                    onCheckedChange = { tempFilter = tempFilter.copy(excludeZeroAmounts = it) }
                                )
                            }

                            // Hide Empty Groups
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (languageMode == LanguageMode.BANGLA) "লেনদেনহীন ক্যাটাগরি লুকান" else "Hide Empty Groups",
                                    fontSize = 12.5.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Switch(
                                    checked = tempFilter.hideEmptyGroups,
                                    onCheckedChange = { tempFilter = tempFilter.copy(hideEmptyGroups = it) }
                                )
                            }

                            // Show Currency Symbol
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
                        }
                    }
                }
            }

            // Unified Footer
            UnifiedFilterFooter(
                activeCount = tempFilter.activeFilterCount,
                languageMode = languageMode,
                onReset = {
                    tempFilter = NetEarningsFilterState()
                },
                onDismiss = onDismiss,
                onApply = { onApply(tempFilter) }
            )
        }
    }

    // Save Preset Dialog
    if (showSavePresetDialog) {
        var presetNameInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showSavePresetDialog = false },
            title = { Text(if (languageMode == LanguageMode.BANGLA) "ফিল্টার প্রিসেট সংরক্ষণ করুন" else "Save Filter Preset") },
            text = {
                OutlinedTextField(
                    value = presetNameInput,
                    onValueChange = { presetNameInput = it },
                    label = { Text(if (languageMode == LanguageMode.BANGLA) "প্রিসেট নাম" else "Preset Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val name = presetNameInput.trim().ifEmpty { "Preset ${savedPresets.size + 1}" }
                        val newPreset = SavedNetEarningsFilterPreset(
                            name = name,
                            filterState = tempFilter
                        )
                        savedPresets.add(0, newPreset)
                        NetEarningsFilterPresetsStorage.savePresets(context, savedPresets)
                        showSavePresetDialog = false
                        Toast.makeText(
                            context,
                            if (languageMode == LanguageMode.BANGLA) "প্রিসেট সংরক্ষিত হয়েছে" else "Preset saved",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SolidPrimary)
                ) {
                    Text(LanguageHelper.getString("save", languageMode))
                }
            },
            dismissButton = {
                TextButton(onClick = { showSavePresetDialog = false }) {
                    Text(LanguageHelper.getString("cancel", languageMode))
                }
            }
        )
    }

    // 6. Open Preset Dialog
    if (showOpenPresetDialog) {
        AlertDialog(
            onDismissRequest = { showOpenPresetDialog = false },
            title = { Text(if (languageMode == LanguageMode.BANGLA) "সংরক্ষিত ফিল্টারসমূহ" else "Saved Filter Presets") },
            text = {
                if (savedPresets.isEmpty()) {
                    Text(
                        text = if (languageMode == LanguageMode.BANGLA) "কোনো সংরক্ষিত ফিল্টার নেই" else "No saved presets found.",
                        color = MaterialTheme.colorScheme.outline
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(savedPresets, key = { it.id }) { preset ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        tempFilter = preset.filterState
                                        showOpenPresetDialog = false
                                        Toast.makeText(
                                            context,
                                            if (languageMode == LanguageMode.BANGLA) "${preset.name} লোড করা হয়েছে" else "Loaded ${preset.name}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = preset.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(
                                            text = "${preset.filterState.activeFilterCount} active filters",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            savedPresets.remove(preset)
                                            NetEarningsFilterPresetsStorage.savePresets(context, savedPresets)
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = SolidExpense, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showOpenPresetDialog = false }) {
                    Text(if (languageMode == LanguageMode.BANGLA) "বন্ধ করুন" else "Close")
                }
            }
        )
    }

    // Date Pickers
    if (showCustomStartDatePicker) {
        val dateState = rememberDatePickerState(initialSelectedDateMillis = tempFilter.customStartDateMs ?: System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { showCustomStartDatePicker = false },
            confirmButton = {
                Button(onClick = {
                    dateState.selectedDateMillis?.let { ms ->
                        tempFilter = tempFilter.copy(customStartDateMs = ms)
                    }
                    showCustomStartDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomStartDatePicker = false }) {
                    Text(LanguageHelper.getString("cancel", languageMode))
                }
            }
        ) {
            DatePicker(state = dateState)
        }
    }

    if (showCustomEndDatePicker) {
        val dateState = rememberDatePickerState(initialSelectedDateMillis = tempFilter.customEndDateMs ?: System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { showCustomEndDatePicker = false },
            confirmButton = {
                Button(onClick = {
                    dateState.selectedDateMillis?.let { ms ->
                        tempFilter = tempFilter.copy(customEndDateMs = ms)
                    }
                    showCustomEndDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomEndDatePicker = false }) {
                    Text(LanguageHelper.getString("cancel", languageMode))
                }
            }
        ) {
            DatePicker(state = dateState)
        }
    }
}

@Composable
private fun FilterEntityRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    countText: String,
    isActive: Boolean,
    onClear: () -> Unit,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (isActive) SolidPrimary else MaterialTheme.colorScheme.outlineVariant),
        color = if (isActive) SolidPrimary.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isActive) SolidPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        text = countText,
                        fontSize = 10.5.sp,
                        color = if (isActive) SolidPrimary else MaterialTheme.colorScheme.outline
                    )
                }
            }

            if (isActive) {
                IconButton(onClick = onClear, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(16.dp))
                }
            } else {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
            }
        }
    }
}

// -----------------------------------------------------------------------------
// ACTIVE NET EARNINGS FILTER BAR COMPONENT
// -----------------------------------------------------------------------------

@Composable
fun ActiveNetEarningsFilterBar(
    filterState: NetEarningsFilterState,
    onFilterChange: (NetEarningsFilterState) -> Unit,
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
                    NetFilterChipPill(
                        text = if (languageMode == LanguageMode.BANGLA) filterState.datePreset.labelBn else filterState.datePreset.labelEn,
                        onClear = { onFilterChange(filterState.copy(datePreset = BudgetDateRangePreset.THIS_MONTH, customStartDateMs = null, customEndDateMs = null)) }
                    )
                }

                if (filterState.flowScope != NetEarningsFlowScope.ALL) {
                    NetFilterChipPill(
                        text = if (languageMode == LanguageMode.BANGLA) filterState.flowScope.labelBn else filterState.flowScope.labelEn,
                        onClear = { onFilterChange(filterState.copy(flowScope = NetEarningsFlowScope.ALL)) }
                    )
                }

                if (filterState.includeTransfers) {
                    NetFilterChipPill(
                        text = if (languageMode == LanguageMode.BANGLA) "ট্রান্সফার সহ" else "Transfers Incl",
                        onClear = { onFilterChange(filterState.copy(includeTransfers = false)) }
                    )
                }

                if (filterState.selectedCategoryIds.isNotEmpty()) {
                    NetFilterChipPill(
                        text = "${filterState.selectedCategoryIds.size} Cats",
                        onClear = { onFilterChange(filterState.copy(selectedCategoryIds = emptySet())) }
                    )
                }

                if (filterState.selectedAccountIds.isNotEmpty()) {
                    NetFilterChipPill(
                        text = "${filterState.selectedAccountIds.size} Accs",
                        onClear = { onFilterChange(filterState.copy(selectedAccountIds = emptySet())) }
                    )
                }

                if (filterState.selectedLabels.isNotEmpty()) {
                    NetFilterChipPill(
                        text = "${filterState.selectedLabels.size} Labels",
                        onClear = { onFilterChange(filterState.copy(selectedLabels = emptySet())) }
                    )
                }

                if (filterState.selectedStatusSet.isNotEmpty()) {
                    NetFilterChipPill(
                        text = "${filterState.selectedStatusSet.size} Status",
                        onClear = { onFilterChange(filterState.copy(selectedStatusSet = emptySet())) }
                    )
                }

                if (filterState.comparisonEnabled) {
                    NetFilterChipPill(
                        text = if (languageMode == LanguageMode.BANGLA) "তুলনা সক্রিয়" else "Compare On",
                        onClear = { onFilterChange(filterState.copy(comparisonEnabled = false)) }
                    )
                }
            }

            TextButton(
                onClick = { onFilterChange(NetEarningsFilterState()) },
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
private fun NetFilterChipPill(
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
// PICKER DIALOG IMPLEMENTATIONS
// -----------------------------------------------------------------------------

@Composable
private fun NetEarningsCategoryPickerDialog(
    categories: List<Category>,
    selectedIds: Set<Long>,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onConfirm: (Set<Long>) -> Unit
) {
    val tempSet = remember { mutableStateListOf<Long>().apply { addAll(selectedIds) } }
    var searchQuery by remember { mutableStateOf("") }

    val filteredCats = remember(categories, searchQuery) {
        if (searchQuery.isBlank()) categories
        else {
            val q = searchQuery.trim().lowercase()
            categories.filter { it.nameEn.lowercase().contains(q) || it.nameBn.lowercase().contains(q) }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (languageMode == LanguageMode.BANGLA) "ক্যাটাগরি নির্বাচন করুন" else "Select Categories") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(if (languageMode == LanguageMode.BANGLA) "খুঁজুন..." else "Search...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = {
                        tempSet.clear()
                        tempSet.addAll(categories.map { it.id })
                    }) {
                        Text(if (languageMode == LanguageMode.BANGLA) "সব নির্বাচন" else "Select All", fontSize = 11.sp)
                    }
                    TextButton(onClick = { tempSet.clear() }) {
                        Text(if (languageMode == LanguageMode.BANGLA) "সব বাতিল" else "Clear All", fontSize = 11.sp)
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                ) {
                    items(filteredCats, key = { it.id }) { cat ->
                        val isChecked = tempSet.contains(cat.id)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isChecked) tempSet.remove(cat.id)
                                    else tempSet.add(cat.id)
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { chk ->
                                    if (chk) tempSet.add(cat.id) else tempSet.remove(cat.id)
                                },
                                colors = CheckboxDefaults.colors(checkedColor = SolidPrimary)
                            )
                            IconHelper.AppIcon(
                                iconName = cat.iconName,
                                contentDescription = null,
                                tint = if (cat.type == CategoryType.EXPENSE) SolidExpense else SolidIncome,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = cat.localizedName(languageMode),
                                fontSize = 13.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(tempSet.toSet()) },
                colors = ButtonDefaults.buttonColors(containerColor = SolidPrimary)
            ) {
                Text(LanguageHelper.getString("done", languageMode))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(LanguageHelper.getString("cancel", languageMode))
            }
        }
    )
}

@Composable
private fun NetEarningsAccountPickerDialog(
    accounts: List<Account>,
    selectedIds: Set<Long>,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onConfirm: (Set<Long>) -> Unit
) {
    val tempSet = remember { mutableStateListOf<Long>().apply { addAll(selectedIds) } }
    var searchQuery by remember { mutableStateOf("") }

    val filteredAccs = remember(accounts, searchQuery) {
        if (searchQuery.isBlank()) accounts
        else {
            val q = searchQuery.trim().lowercase()
            accounts.filter { it.nameEn.lowercase().contains(q) || it.nameBn.lowercase().contains(q) }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (languageMode == LanguageMode.BANGLA) "অ্যাকাউন্ট নির্বাচন করুন" else "Select Accounts") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(if (languageMode == LanguageMode.BANGLA) "খুঁজুন..." else "Search...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = {
                        tempSet.clear()
                        tempSet.addAll(accounts.map { it.id })
                    }) {
                        Text(if (languageMode == LanguageMode.BANGLA) "সব নির্বাচন" else "Select All", fontSize = 11.sp)
                    }
                    TextButton(onClick = { tempSet.clear() }) {
                        Text(if (languageMode == LanguageMode.BANGLA) "সব বাতিল" else "Clear All", fontSize = 11.sp)
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                ) {
                    items(filteredAccs, key = { it.id }) { acc ->
                        val isChecked = tempSet.contains(acc.id)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isChecked) tempSet.remove(acc.id)
                                    else tempSet.add(acc.id)
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { chk ->
                                    if (chk) tempSet.add(acc.id) else tempSet.remove(acc.id)
                                },
                                colors = CheckboxDefaults.colors(checkedColor = SolidPrimary)
                            )
                            IconHelper.AppIcon(
                                iconName = acc.iconName,
                                contentDescription = null,
                                tint = SolidPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = acc.localizedName(languageMode),
                                fontSize = 13.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(tempSet.toSet()) },
                colors = ButtonDefaults.buttonColors(containerColor = SolidPrimary)
            ) {
                Text(LanguageHelper.getString("done", languageMode))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(LanguageHelper.getString("cancel", languageMode))
            }
        }
    )
}

@Composable
private fun NetEarningsStatusPickerDialog(
    selectedStatus: Set<TransactionStatus>,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onConfirm: (Set<TransactionStatus>) -> Unit
) {
    val tempSet = remember { mutableStateListOf<TransactionStatus>().apply { addAll(selectedStatus) } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (languageMode == LanguageMode.BANGLA) "লেনদেন স্ট্যাটাস" else "Transaction Status") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TransactionStatus.values().forEach { st ->
                    val isChecked = tempSet.contains(st)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isChecked) tempSet.remove(st) else tempSet.add(st)
                            }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { chk ->
                                if (chk) tempSet.add(st) else tempSet.remove(st)
                            },
                            colors = CheckboxDefaults.colors(checkedColor = SolidPrimary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = st.name, fontSize = 14.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(tempSet.toSet()) },
                colors = ButtonDefaults.buttonColors(containerColor = SolidPrimary)
            ) {
                Text(LanguageHelper.getString("done", languageMode))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(LanguageHelper.getString("cancel", languageMode))
            }
        }
    )
}

@Composable
private fun NetEarningsLabelsPickerDialog(
    allLabels: List<String>,
    selectedLabels: Set<String>,
    languageMode: LanguageMode,
    onDismiss: () -> Unit,
    onConfirm: (Set<String>) -> Unit
) {
    val tempSet = remember { mutableStateListOf<String>().apply { addAll(selectedLabels) } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (languageMode == LanguageMode.BANGLA) "লেবেল / ট্যাগ নির্বাচন" else "Select Labels") },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            ) {
                items(allLabels) { lbl ->
                    val isChecked = tempSet.contains(lbl)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isChecked) tempSet.remove(lbl) else tempSet.add(lbl)
                            }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { chk ->
                                if (chk) tempSet.add(lbl) else tempSet.remove(lbl)
                            },
                            colors = CheckboxDefaults.colors(checkedColor = SolidPrimary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = lbl, fontSize = 13.5.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(tempSet.toSet()) },
                colors = ButtonDefaults.buttonColors(containerColor = SolidPrimary)
            ) {
                Text(LanguageHelper.getString("done", languageMode))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(LanguageHelper.getString("cancel", languageMode))
            }
        }
    )
}
